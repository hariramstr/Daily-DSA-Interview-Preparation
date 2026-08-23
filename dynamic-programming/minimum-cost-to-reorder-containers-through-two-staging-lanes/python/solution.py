"""
Minimum Cost to Reorder Containers Through Two Staging Lanes

A port receives containers in a fixed arrival order. Each container has a positive
integer weight. Before loading them onto a ship, the port may route every arriving
container into exactly one of two staging lanes, A or B. Containers assigned to the
same lane must remain in their original relative order. After all containers are
assigned, the ship is loaded by repeatedly taking the front container from either lane
until both lanes are empty.

The final loading order must be nondecreasing by weight. If a container of weight w is
placed immediately after a container of weight p in the final sequence, the loading cost
increases by |w - p|. The first loaded container adds no cost.

Task:
Compute the minimum possible total loading cost, or return -1 if no valid nondecreasing
loading order can be formed using exactly these two staging lanes.

Equivalent view:
Partition the original sequence into two subsequences, preserving order within each
subsequence, so that they can be merged into one nondecreasing sequence. Among all such
feasible partitions and merges, minimize the sum of absolute differences between
consecutive loaded weights.

Constraints:
- 1 <= n <= 3000
- 1 <= weights[i] <= 10^9
- An O(n^2) dynamic programming solution is expected.
"""

from typing import List


class Solution:
    def minimum_loading_cost(self, weights: List[int]) -> int:
        """
        Compute the minimum total loading cost using two staging lanes.

        Key idea:
        A sequence can be produced by merging two lane sequences if and only if the
        original sequence can be colored with two colors so that each color class is
        nondecreasing. While building such a partition from left to right, we only need
        to know the last element placed in each lane.

        Dynamic programming state:
        After processing the first i containers, suppose the last container placed into
        one lane is the i-th container itself, and the last container placed into the
        other lane is some earlier index j (or 0 meaning that lane is still empty).
        Let dp[j] be the minimum possible final loading cost of the fully merged,
        globally nondecreasing sequence formed by those first i containers.

        Why the cost is easy:
        Any valid final loading order must be nondecreasing and must contain exactly the
        same multiset of weights. Therefore the minimum possible sum of absolute
        differences between consecutive loaded weights is simply the sum of increases
        between consecutive values in the sorted order of all weights, which telescopes
        to max(weights) - min(weights), provided a feasible partition exists.
        So the real challenge is feasibility. We still keep the DP in cost form to match
        the problem statement, but every valid extension preserves the same eventual cost
        structure. In practice, we can detect feasibility with O(n^2) DP and then return
        max - min.

        Transition:
        Let current index be i (1-based in the explanation, 0-based in code).
        Previous processed prefix length is i-1.

        A previous state is represented by:
        - one lane ends at index prev_last = i-1
        - the other lane ends at index j

        We now place weights[i] into one of the two lanes:

        1) Put weights[i] into the lane whose current last element is weights[i-1].
           This is allowed if weights[i-1] <= weights[i].
           New state becomes "current lane last is i, other lane last is j".

        2) Put weights[i] into the other lane whose current last element is weights[j]
           (or empty if j == -1).
           This is allowed if that lane is empty, or weights[j] <= weights[i].
           New state becomes "current lane last is i, other lane last is i-1".

        We only need boolean feasibility, but we store large/small values in a DP array
        for clarity and future extensibility.

        Args:
            weights: List of positive integer container weights.

        Returns:
            Minimum total loading cost if feasible, otherwise -1.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n)
        """
        n: int = len(weights)

        # A single container is always feasible.
        # It contributes no cost because the first loaded container adds nothing.
        if n <= 1:
            return 0

        # We use a large sentinel value to represent an impossible DP state.
        inf: int = 10**30

        # dp[j] after processing up to index i means:
        # - one lane's last assigned container is exactly index i
        # - the other lane's last assigned container is index j
        # - dp[j] is finite if such an assignment is feasible
        #
        # We use j in [0, n-1]. There is also a special "empty other lane" case.
        # To keep indexing simple, we store that special case at j = n.
        empty_idx: int = n
        dp: List[int] = [inf] * (n + 1)

        # Base case: after processing only weights[0],
        # one lane contains that element, the other lane is empty.
        dp[empty_idx] = 0

        # Process each next container from left to right.
        for i in range(1, n):
            new_dp: List[int] = [inf] * (n + 1)

            # We iterate over every possible "other lane last index" from the previous step.
            for j in range(n + 1):
                if dp[j] == inf:
                    # This previous state is impossible, so skip it.
                    continue

                # ---------------------------------------------------------------
                # Option 1:
                # Put weights[i] into the same lane whose last element is weights[i - 1].
                #
                # Previous state structure:
                # - lane X ends at i - 1
                # - lane Y ends at j (or is empty if j == empty_idx)
                #
                # If weights[i - 1] <= weights[i], then appending to lane X keeps lane X
                # nondecreasing. Lane Y is unchanged.
                #
                # New state:
                # - lane X now ends at i
                # - lane Y still ends at j
                # ---------------------------------------------------------------
                if weights[i - 1] <= weights[i]:
                    new_dp[j] = min(new_dp[j], dp[j])

                # ---------------------------------------------------------------
                # Option 2:
                # Put weights[i] into the other lane, the one whose last element is j.
                #
                # This is allowed if:
                # - that lane is empty, or
                # - weights[j] <= weights[i]
                #
                # After doing this, the lane that used to end at i - 1 becomes the
                # "other lane" in the canonical representation, because the lane
                # receiving the current item must be the one ending at i.
                #
                # So the new "other lane last index" becomes i - 1.
                # ---------------------------------------------------------------
                if j == empty_idx or weights[j] <= weights[i]:
                    new_dp[i - 1] = min(new_dp[i - 1], dp[j])

            dp = new_dp

        # If every state is impossible after processing all containers,
        # then no partition into two nondecreasing subsequences exists.
        feasible: bool = any(value != inf for value in dp)
        if not feasible:
            return -1

        # Important observation:
        # Once feasibility is established, the final loaded sequence can be any
        # nondecreasing arrangement consistent with the two lanes, but because it must
        # contain exactly the same multiset of weights, the minimum possible sum of
        # consecutive absolute differences over a nondecreasing full sequence is simply:
        #
        # (sorted_w[1] - sorted_w[0]) + (sorted_w[2] - sorted_w[1]) + ... =
        # sorted_w[-1] - sorted_w[0]
        #
        # Equal values contribute 0 naturally.
        return max(weights) - min(weights)

    def solve(self, weights: List[int]) -> int:
        """
        Wrapper method matching a typical interview / platform style API.

        Args:
            weights: List of container weights.

        Returns:
            Minimum loading cost, or -1 if impossible.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n)
        """
        return self.minimum_loading_cost(weights)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, 1, 3, 2],
        [3, 1, 2, 1],
        [1],
        [1, 2, 3, 4],
        [4, 3, 2, 1],
        [2, 1, 2, 3],
    ]

    for arr in sample_inputs:
        result = solution.solve(arr)
        print(f"weights = {arr} -> {result}")