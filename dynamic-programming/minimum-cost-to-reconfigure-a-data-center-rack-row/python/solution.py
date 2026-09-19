"""
Title: Minimum Cost to Reconfigure a Data Center Rack Row

Problem Description:
A data center has a row of n server racks, numbered from 0 to n - 1. Each rack must end up
assigned to exactly one power profile: profile A, profile B, or profile C. Reconfiguring
rack i to profile p has a given cost cost[i][p]. However, safety rules also impose
structure on the final row.

The row must be partitioned into exactly k contiguous zones. Inside each zone, all racks
must use the same power profile. Adjacent zones must use different profiles. In addition,
the length of every zone must be between minLen and maxLen inclusive.

Your task is to compute the minimum total reconfiguration cost to assign profiles to all
racks while satisfying all rules. If no valid partition exists, return -1.

Formally, choose exactly k segments that cover the entire array without overlap, where each
segment is contiguous, each segment length is in [minLen, maxLen], every segment is assigned
one of the 3 profiles, all racks in a segment share that profile, and neighboring segments
have different profiles. The cost of a segment from l to r using profile p is the sum of
cost[i][p] for l <= i <= r.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 5000
- 1 <= k <= 200
- cost.length == n
- cost[i].length == 3
- 0 <= cost[i][p] <= 10^9
- 1 <= minLen <= maxLen <= n
"""

from typing import List


class Solution:
    def minimum_reconfiguration_cost(
        self,
        n: int,
        cost: List[List[int]],
        k: int,
        minLen: int,
        maxLen: int,
    ) -> int:
        """
        Compute the minimum total cost to partition the rack row into exactly k valid zones.

        The algorithm uses dynamic programming with a sliding-window minimum optimization.
        We process zones one by one. For each possible ending position and chosen profile,
        we compute the best cost of ending the current zone there. The key optimization is
        that for each profile we only need the best previous DP value among the other two
        profiles over a valid range of previous cut positions.

        Args:
            n: Number of racks.
            cost: cost[i][p] is the cost to assign rack i to profile p, where p in {0,1,2}.
            k: Exact number of contiguous zones required.
            minLen: Minimum allowed zone length.
            maxLen: Maximum allowed zone length.

        Returns:
            The minimum total reconfiguration cost, or -1 if no valid partition exists.

        Time complexity:
            O(n * k)

        Space complexity:
            O(n)
        """
        # ------------------------------------------------------------
        # Quick feasibility check based only on lengths.
        #
        # If we need exactly k segments, and each segment length must be
        # between minLen and maxLen, then the total covered length must
        # satisfy:
        #     k * minLen <= n <= k * maxLen
        #
        # If this is false, no partition can possibly exist, so we can
        # return immediately.
        # ------------------------------------------------------------
        if k * minLen > n or k * maxLen < n:
            return -1

        # ------------------------------------------------------------
        # Prefix sums for each of the 3 profiles.
        #
        # prefix[p][i] = total cost of assigning racks [0 .. i-1] to profile p
        #
        # This lets us query the cost of any segment [l .. r] in O(1):
        #     segment_cost = prefix[p][r + 1] - prefix[p][l]
        #
        # We use 1-based prefix indexing because it makes range formulas
        # clean and standard.
        # ------------------------------------------------------------
        prefix: List[List[int]] = [[0] * (n + 1) for _ in range(3)]
        for i in range(n):
            for p in range(3):
                prefix[p][i + 1] = prefix[p][i] + cost[i][p]

        # A very large number used as "infinity" for impossible states.
        inf: int = 10**30

        # ------------------------------------------------------------
        # DP definition:
        #
        # After processing exactly t zones, let:
        #     prev[p][i] = minimum cost to cover exactly the first i racks
        #                  using exactly t zones, where the t-th (last)
        #                  zone uses profile p.
        #
        # Here i ranges from 0 to n, meaning "covered prefix length".
        # So if i = 7, we have assigned racks [0..6].
        #
        # For t = 0, the only valid state is covering 0 racks with cost 0.
        # There is no real "last profile" for zero zones, so we handle the
        # first zone separately below.
        # ------------------------------------------------------------
        prev: List[List[int]] = [[inf] * (n + 1) for _ in range(3)]

        # ------------------------------------------------------------
        # Build DP layer by layer: t = 1 .. k
        # ------------------------------------------------------------
        for t in range(1, k + 1):
            # Current DP layer for exactly t zones.
            curr: List[List[int]] = [[inf] * (n + 1) for _ in range(3)]

            # --------------------------------------------------------
            # Special handling for the first zone.
            #
            # If t == 1, then there is no previous zone and therefore no
            # adjacency restriction yet. We simply choose one segment
            # [0 .. i-1] whose length i is within [minLen, maxLen].
            #
            # For each profile p:
            #     curr[p][i] = cost of assigning racks [0 .. i-1] to p
            # --------------------------------------------------------
            if t == 1:
                start_len: int = minLen
                end_len: int = min(maxLen, n)
                for i in range(start_len, end_len + 1):
                    for p in range(3):
                        curr[p][i] = prefix[p][i]
                prev = curr
                continue

            # --------------------------------------------------------
            # For t >= 2, we need transitions from the previous layer.
            #
            # Suppose the current t-th zone ends at position i (meaning it
            # covers some suffix of the first i racks), and uses profile p.
            #
            # Let j be the starting covered length before this zone begins.
            # Then the current zone is racks [j .. i-1], so its length is:
            #     i - j
            #
            # Validity requires:
            #     minLen <= i - j <= maxLen
            # which means:
            #     i - maxLen <= j <= i - minLen
            #
            # Also, the previous zone's profile must be different from p.
            #
            # Therefore:
            #     curr[p][i] =
            #         min over valid j and q != p of
            #             prev[q][j] + segment_cost(j, i-1, p)
            #
            # Since segment_cost(j, i-1, p) =
            #     prefix[p][i] - prefix[p][j],
            #
            # we can rewrite:
            #     curr[p][i] =
            #         prefix[p][i] +
            #         min over valid j and q != p of
            #             (prev[q][j] - prefix[p][j])
            #
            # For fixed t and p, as i increases, the valid j range is a
            # sliding window. So we maintain the minimum value of:
            #     min(prev[q1][j], prev[q2][j]) - prefix[p][j]
            # over the current valid j window.
            #
            # Because there are only 3 profiles, for each p the "other two"
            # profiles are easy to identify.
            #
            # We use a monotonic deque implemented with arrays and a head
            # index to support:
            #   - adding a new candidate j
            #   - removing expired j values
            #   - querying the minimum candidate
            #
            # Each j enters and leaves once, so total work per (t, p) is O(n).
            # --------------------------------------------------------
            for p in range(3):
                other_profiles: List[int] = [q for q in range(3) if q != p]

                # These arrays store the deque contents:
                #   deque_idx[m] = candidate position j
                #   deque_val[m] = corresponding value:
                #                  min(prev[q][j] for q != p) - prefix[p][j]
                #
                # We use simple Python lists plus head/tail pointers for speed.
                deque_idx: List[int] = []
                deque_val: List[int] = []
                head: int = 0

                # ----------------------------------------------------
                # Iterate over all possible covered lengths i.
                #
                # However, not every i can be valid for exactly t zones.
                # Still, scanning all i from 0..n keeps the sliding-window
                # logic straightforward and remains O(n).
                # ----------------------------------------------------
                for i in range(0, n + 1):
                    # ------------------------------------------------
                    # Add the new left boundary candidate j = i - minLen
                    # because this is the newest j that becomes valid when
                    # considering zones ending at i.
                    #
                    # Only add if j >= 0.
                    # ------------------------------------------------
                    add_j: int = i - minLen
                    if add_j >= 0:
                        best_prev_other: int = min(
                            prev[other_profiles[0]][add_j],
                            prev[other_profiles[1]][add_j],
                        )

                        if best_prev_other < inf:
                            candidate_value: int = best_prev_other - prefix[p][add_j]

                            # Maintain deque in increasing order of candidate_value.
                            # Remove all worse-or-equal values from the back because
                            # they will never be useful while this new smaller value
                            # remains in the window.
                            while len(deque_val) > head and deque_val[-1] >= candidate_value:
                                deque_val.pop()
                                deque_idx.pop()

                            deque_val.append(candidate_value)
                            deque_idx.append(add_j)

                    # ------------------------------------------------
                    # Remove expired candidates.
                    #
                    # Valid j must satisfy:
                    #     j >= i - maxLen
                    #
                    # So any j < i - maxLen is too old and must be removed
                    # from the front of the deque.
                    # ------------------------------------------------
                    min_valid_j: int = i - maxLen
                    while head < len(deque_idx) and deque_idx[head] < min_valid_j:
                        head += 1

                    # ------------------------------------------------
                    # If the deque is non-empty, its front holds the minimum
                    # candidate value over all currently valid j.
                    #
                    # Then:
                    #     curr[p][i] = prefix[p][i] + deque_min
                    #
                    # This automatically enforces:
                    #   - exact previous zone count t-1
                    #   - different adjacent profiles
                    #   - current zone length within [minLen, maxLen]
                    # ------------------------------------------------
                    if head < len(deque_val):
                        curr[p][i] = prefix[p][i] + deque_val[head]

            # Move to the next layer.
            prev = curr

        # ------------------------------------------------------------
        # Final answer:
        #
        # We need exactly k zones covering all n racks, and the last zone
        # may use any of the 3 profiles.
        # ------------------------------------------------------------
        answer: int = min(prev[0][n], prev[1][n], prev[2][n])
        return -1 if answer >= inf else answer

    def solve(
        self,
        n: int,
        cost: List[List[int]],
        k: int,
        minLen: int,
        maxLen: int,
    ) -> int:
        """
        Wrapper method matching the problem's required computation.

        Args:
            n: Number of racks.
            cost: Reconfiguration costs for each rack and profile.
            k: Exact number of zones.
            minLen: Minimum zone length.
            maxLen: Maximum zone length.

        Returns:
            Minimum valid total cost, or -1 if impossible.

        Time complexity:
            O(n * k)

        Space complexity:
            O(n)
        """
        return self.minimum_reconfiguration_cost(n, cost, k, minLen, maxLen)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 6
    cost1 = [
        [1, 5, 3],
        [2, 4, 6],
        [7, 1, 2],
        [3, 8, 4],
        [6, 2, 5],
        [4, 3, 1],
    ]
    k1 = 3
    min_len1 = 2
    max_len1 = 2
    result1 = solution.solve(n1, cost1, k1, min_len1, max_len1)
    print(result1)

    # Example 2
    n2 = 5
    cost2 = [
        [3, 1, 9],
        [2, 5, 4],
        [8, 2, 3],
        [6, 1, 7],
        [4, 3, 2],
    ]
    k2 = 2
    min_len2 = 3
    max_len2 = 3
    result2 = solution.solve(n2, cost2, k2, min_len2, max_len2)
    print(result2)