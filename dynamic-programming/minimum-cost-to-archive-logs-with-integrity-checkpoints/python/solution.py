"""
Title: Minimum Cost to Archive Logs with Integrity Checkpoints

Problem Description:
A company stores a sequence of daily log batches in order. You are given an array
`size` where `size[i]` is the size of the `i`-th batch, and an array `risk` where
`risk[i]` is the corruption risk score of that batch. The company wants to partition
the batches into exactly `k` contiguous archive files.

For each archive file covering batches from index `l` to `r` inclusive, its storage
cost is defined as:

    (sum of size[l..r]) * (maximum risk[l..r])

In addition, every archive file except the last must end with an integrity checkpoint.
The company has a list of allowed checkpoint positions given by a binary string
`checkpoint`, where `checkpoint[i] = '1'` means a file is allowed to end at batch `i`.
The last archive file must always end at the final batch `n - 1`, even if
`checkpoint[n - 1] = '0'`.

Return the minimum total storage cost to partition all batches into exactly `k`
contiguous archive files while respecting the checkpoint rule. If it is impossible,
return `-1`.

A partition is valid if every file is non-empty, files cover all batches exactly once,
and for the first `k - 1` files the ending index must be an allowed checkpoint.

Constraints:
- 1 <= n <= 2000
- 1 <= k <= min(n, 50)
- 1 <= size[i] <= 10^6
- 1 <= risk[i] <= 10^6
- checkpoint.length == n
- checkpoint[i] is either '0' or '1'
"""

from typing import List


class Solution:
    def minimum_archive_cost(self, size: List[int], risk: List[int], checkpoint: str, k: int) -> int:
        """
        Compute the minimum total cost to split the batches into exactly k contiguous files.

        The dynamic programming state is:
            dp_prev[i] = minimum cost to partition the prefix [0..i-1] into (parts - 1) files
        and we compute:
            dp_curr[r + 1] = minimum cost to partition the prefix [0..r] into parts files

        A transition chooses the last file as [l..r], so:
            dp_curr[r + 1] = min(dp_prev[l] + cost(l, r))

        Validity rules:
        - For intermediate stages (parts < k), the current file must end at an allowed checkpoint.
        - For the final stage (parts == k), the only meaningful final state is ending at n - 1.

        To make transitions efficient enough, for each fixed number of parts we scan the right
        endpoint r from left to right and maintain a compressed set of candidate starting points.
        This compression groups starts by the maximum risk on [l..r]. For each distinct maximum
        risk value, we keep only the minimum value of:
            dp_prev[l] - prefix_size[l] * max_risk
        because:
            dp_prev[l] + (prefix_size[r + 1] - prefix_size[l]) * max_risk
          = prefix_size[r + 1] * max_risk + (dp_prev[l] - prefix_size[l] * max_risk)

        This is a standard optimization pattern for interval DP with "sum * max" cost.

        Args:
            size: List of batch sizes.
            risk: List of batch risk scores.
            checkpoint: Binary string of allowed internal file endings.
            k: Exact number of contiguous files required.

        Returns:
            The minimum total cost, or -1 if impossible.

        Time complexity:
            O(k * n^2) in the worst case, but with strong practical compression of states
            while scanning each layer. Given n <= 2000 and k <= 50, this approach is designed
            to be efficient enough in Python with careful implementation.

        Space complexity:
            O(n) for DP arrays plus O(n) temporary compressed states.
        """
        n: int = len(size)

        # Basic impossibility check:
        # We need exactly k non-empty files, so k cannot exceed n.
        if k > n:
            return -1

        # Another quick impossibility check:
        # The first k - 1 files must end at allowed checkpoints.
        # Therefore we need at least k - 1 allowed checkpoint positions among indices [0..n-2].
        allowed_internal_count: int = sum(1 for i in range(n - 1) if checkpoint[i] == "1")
        if allowed_internal_count < k - 1:
            return -1

        # Prefix sums of sizes let us compute sum(size[l..r]) in O(1):
        # prefix_size[x] = sum of size[0..x-1]
        prefix_size: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix_size[i + 1] = prefix_size[i] + size[i]

        # A large sentinel value for "unreachable".
        inf: int = 10**30

        # dp_prev[x] means:
        # minimum cost to partition the first x batches (indices [0..x-1])
        # into the previous number of parts.
        #
        # Initially, partitioning zero batches into zero files costs 0.
        # Any non-zero prefix with zero files is impossible.
        dp_prev: List[int] = [inf] * (n + 1)
        dp_prev[0] = 0

        # We build the answer layer by layer:
        # parts = 1 means one file,
        # parts = 2 means two files, etc.
        for parts in range(1, k + 1):
            # Fresh DP array for this layer.
            dp_curr: List[int] = [inf] * (n + 1)

            # The earliest possible ending index r for `parts` files is parts - 1
            # because each file must be non-empty.
            #
            # The latest possible ending index r depends on how many elements must remain
            # for the remaining files:
            # after placing `parts` files ending at r, there must remain at least
            # (k - parts) elements for the remaining files.
            #
            # So:
            #   r <= n - (k - parts) - 1
            #
            # This pruning avoids computing states that can never lead to a full valid partition.
            r_start: int = parts - 1
            r_end: int = n - (k - parts) - 1

            # We maintain a compressed list of groups while sweeping r from left to right.
            #
            # Each group is a pair:
            #   (max_risk_value, best_adjusted_value)
            #
            # Interpretation:
            # among some set of candidate starts l, all of them currently produce the same
            # maximum risk on interval [l..r], namely max_risk_value.
            #
            # For that group we only need the minimum:
            #   best_adjusted_value = min(dp_prev[l] - prefix_size[l] * max_risk_value)
            #
            # Then the best transition contributed by this group to current r is:
            #   prefix_size[r + 1] * max_risk_value + best_adjusted_value
            #
            # Why compression works:
            # when we extend r by one position, the interval maximum for many starts may
            # become the new risk[r]. If risk[r] is larger than previous group maxima,
            # those groups merge into one new group with maximum risk[r].
            groups: List[List[int]] = []

            # Sweep the right endpoint of the last file.
            for r in range(r_start, r_end + 1):
                # New possible start for the last file is l = r.
                # This corresponds to a length-1 last segment [r..r].
                #
                # It is only valid if the previous prefix of length r can be partitioned
                # into exactly (parts - 1) files.
                l: int = r
                if dp_prev[l] < inf:
                    new_max: int = risk[r]
                    new_best: int = dp_prev[l] - prefix_size[l] * new_max

                    # Merge with previous groups while their max risk is <= new_max.
                    #
                    # Reason:
                    # for any earlier start represented by such a group, once we extend
                    # the interval to include index r, the maximum on [start..r] becomes
                    # new_max as well. So those groups collapse into the new group.
                    while groups and groups[-1][0] <= new_max:
                        if groups[-1][1] < new_best:
                            new_best = groups[-1][1]
                        groups.pop()

                    groups.append([new_max, new_best])

                # Now compute dp_curr[r + 1] if ending at r is allowed for this stage.
                #
                # Rule:
                # - If this is not the final file (parts < k), then the file ending at r
                #   must be an allowed checkpoint, so checkpoint[r] must be '1'.
                # - If this is the final file (parts == k), then the only relevant final
                #   answer is at r == n - 1. We still compute only that valid endpoint.
                valid_end: bool
                if parts < k:
                    valid_end = checkpoint[r] == "1"
                else:
                    valid_end = r == n - 1

                if valid_end and groups:
                    total_prefix_size: int = prefix_size[r + 1]

                    # Evaluate all compressed groups and take the minimum.
                    #
                    # Each group contributes:
                    #   total_prefix_size * max_risk + best_adjusted
                    best_cost: int = inf
                    for max_risk_value, best_adjusted_value in groups:
                        candidate: int = total_prefix_size * max_risk_value + best_adjusted_value
                        if candidate < best_cost:
                            best_cost = candidate

                    dp_curr[r + 1] = best_cost

            # Move to the next layer.
            dp_prev = dp_curr

        answer: int = dp_prev[n]
        return -1 if answer >= inf else answer


if __name__ == "__main__":
    solver = Solution()

    # Example 1 from the prompt.
    #
    # Note:
    # The textual explanation in the prompt is internally inconsistent:
    # it says checkpoint = "1010", which means allowed checkpoints are indices 0 and 2,
    # but then it discusses a partition ending at index 1 as if it were allowed in one place.
    #
    # Under the stated checkpoint string "1010":
    # - [4] | [2,7,3] is valid, cost = 4*5 + 12*4 = 68
    # - [4,2,7] | [3] is valid, cost = 13*5 + 3*2 = 71
    # So the correct minimum is 68.
    size1 = [4, 2, 7, 3]
    risk1 = [5, 1, 4, 2]
    checkpoint1 = "1010"
    k1 = 2
    print(solver.minimum_archive_cost(size1, risk1, checkpoint1, k1))  # Expected: 68

    # Example 2 from the prompt.
    #
    # Again, the prompt's explanation is inconsistent with the checkpoint string "01010":
    # allowed internal checkpoints are indices 1 and 3, but partitioning into 3 files
    # would require two cuts, and the only possible cut pair is (1, 3), yielding:
    # [3,6] | [2,5] | [4]
    # cost = 9*7 + 7*5 + 4*6 = 63 + 35 + 24 = 122
    #
    # So the mathematically correct result for the stated input is 122.
    size2 = [3, 6, 2, 5, 4]
    risk2 = [2, 7, 1, 3, 6]
    checkpoint2 = "01010"
    k2 = 3
    print(solver.minimum_archive_cost(size2, risk2, checkpoint2, k2))  # Correct for stated input: 122

    # Additional small sanity checks.
    print(solver.minimum_archive_cost([5], [7], "0", 1))  # Expected: 35
    print(solver.minimum_archive_cost([1, 2, 3], [3, 2, 1], "000", 2))  # Expected: -1