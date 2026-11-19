"""
Minimum Cost to Stabilize a Conveyor Belt with Optional Buffer Pads

Problem Description:
A warehouse conveyor belt is divided into n consecutive segments. Segment i has an
instability value instability[i], which may be positive, zero, or negative.
To reduce vibration, the belt must be fully covered from left to right using repair
operations. You may apply two kinds of operations:

1. Standard repair on a single segment i, costing singleCost[i].
2. Buffered repair on exactly two adjacent segments i and i+1, costing pairCost[i].
   A buffered repair may only be used if the absolute difference between
   instability[i] and instability[i+1] is at most limit.

In addition, the engineering team allows up to k reinforcement coupons.
Each coupon can be spent on any one repair operation (single or buffered) and reduces
that operation's cost by discount, but the final cost of that operation cannot go below 0.

Your task is to compute the minimum total cost needed to cover every segment exactly once.

Two repairs cannot overlap, and every segment must belong to exactly one chosen operation.

Return the minimum possible total cost.

Typical dynamic programming idea:
- Let dp[i][c] represent the minimum cost to cover the first i segments
  using exactly c coupons.
- From position i, we can:
  * cover segment i alone
  * cover segments i and i+1 together if the pair is valid
- For each operation, we may either use a coupon on it or not.
"""

from typing import List


class Solution:
    def min_cost_to_stabilize(
        self,
        instability: List[int],
        singleCost: List[int],
        pairCost: List[int],
        limit: int,
        k: int,
        discount: int,
    ) -> int:
        """
        Compute the minimum total repair cost to cover all segments exactly once.

        Args:
            instability: Instability values for each segment.
            singleCost: Cost to repair each segment individually.
            pairCost: Cost to repair adjacent pairs (i, i+1).
            limit: Maximum allowed absolute instability difference for a pair repair.
            k: Maximum number of coupons available.
            discount: Amount subtracted from one operation when a coupon is used.

        Returns:
            The minimum possible total cost.

        Time complexity:
            O(n * k)

        Space complexity:
            O(n * k)
        """
        n: int = len(instability)

        # We define a very large number to represent "currently unreachable".
        # Using a large integer is standard in dynamic programming minimization problems.
        inf: int = 10**30

        # dp[i][used] means:
        #   minimum cost to cover exactly the first i segments
        #   while using exactly 'used' coupons.
        #
        # Important indexing detail:
        # - i ranges from 0 to n
        # - "first i segments" means segments [0, 1, ..., i-1] are already covered
        # - so the next uncovered segment is i
        #
        # Example:
        # - dp[0][0] = 0 because covering zero segments costs zero
        # - dp[n][used] is a complete covering of the whole belt
        dp: List[List[int]] = [[inf] * (k + 1) for _ in range(n + 1)]
        dp[0][0] = 0

        # Precompute whether each adjacent pair is valid.
        #
        # valid_pair[i] corresponds to the operation covering segments i and i+1.
        # It is allowed only when:
        #   abs(instability[i] - instability[i+1]) <= limit
        #
        # This avoids recomputing the same absolute difference many times inside the DP.
        valid_pair: List[bool] = [False] * max(0, n - 1)
        for i in range(n - 1):
            valid_pair[i] = abs(instability[i] - instability[i + 1]) <= limit

        # Main DP transition:
        # We iterate over every prefix length i and every coupon count used.
        # From each reachable state, we try all legal next operations.
        for i in range(n):
            for used in range(k + 1):
                current_cost: int = dp[i][used]

                # If this state is unreachable, skip it.
                if current_cost == inf:
                    continue

                # ------------------------------------------------------------
                # Option 1: Repair segment i alone using a standard repair
                # ------------------------------------------------------------
                #
                # This covers exactly one new segment, so we move from i to i+1.
                #
                # Case 1A: Do not use a coupon on this operation.
                next_cost_without_coupon: int = current_cost + singleCost[i]
                if next_cost_without_coupon < dp[i + 1][used]:
                    dp[i + 1][used] = next_cost_without_coupon

                # Case 1B: Use one coupon on this single repair, if available.
                #
                # The discounted operation cost cannot go below zero.
                if used < k:
                    discounted_single: int = max(0, singleCost[i] - discount)
                    next_cost_with_coupon: int = current_cost + discounted_single
                    if next_cost_with_coupon < dp[i + 1][used + 1]:
                        dp[i + 1][used + 1] = next_cost_with_coupon

                # ------------------------------------------------------------
                # Option 2: Repair segments i and i+1 together using a buffer
                # ------------------------------------------------------------
                #
                # This is only possible if:
                # - there is a next segment (i + 1 < n)
                # - the pair satisfies the instability limit condition
                #
                # This covers two new segments, so we move from i to i+2.
                if i + 1 < n and valid_pair[i]:
                    # Case 2A: Do not use a coupon on the pair repair.
                    next_pair_cost_without_coupon: int = current_cost + pairCost[i]
                    if next_pair_cost_without_coupon < dp[i + 2][used]:
                        dp[i + 2][used] = next_pair_cost_without_coupon

                    # Case 2B: Use one coupon on the pair repair, if available.
                    if used < k:
                        discounted_pair: int = max(0, pairCost[i] - discount)
                        next_pair_cost_with_coupon: int = current_cost + discounted_pair
                        if next_pair_cost_with_coupon < dp[i + 2][used + 1]:
                            dp[i + 2][used + 1] = next_pair_cost_with_coupon

        # We are allowed to use up to k coupons, not necessarily exactly k.
        # Therefore the answer is the minimum among all dp[n][used].
        answer: int = min(dp[n])
        return answer

    def minimumCost(
        self,
        instability: List[int],
        singleCost: List[int],
        pairCost: List[int],
        limit: int,
        k: int,
        discount: int,
    ) -> int:
        """
        Wrapper method matching a common interview/platform style naming convention.

        Args:
            instability: Instability values for each segment.
            singleCost: Cost to repair each segment individually.
            pairCost: Cost to repair adjacent pairs.
            limit: Maximum allowed instability difference for pair repair.
            k: Maximum number of coupons.
            discount: Discount amount per coupon.

        Returns:
            The minimum possible total cost.

        Time complexity:
            O(n * k)

        Space complexity:
            O(n * k)
        """
        return self.min_cost_to_stabilize(
            instability, singleCost, pairCost, limit, k, discount
        )


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    #
    # Careful verification:
    # instability = [4, 7, 6, 6]
    # valid pairs:
    # - (0,1): |4-7| = 3 > 2 => invalid
    # - (1,2): |7-6| = 1 <= 2 => valid, cost 20
    # - (2,3): |6-6| = 0 <= 2 => valid, cost 6
    #
    # Best plan:
    # - segment 0 alone with coupon: max(0, 8-3) = 5
    # - segment 1 alone: 5
    # - segments 2 and 3 paired: 6
    # Total = 16
    instability1 = [4, 7, 6, 6]
    single_cost1 = [8, 5, 7, 4]
    pair_cost1 = [9, 20, 6]
    limit1 = 2
    k1 = 1
    discount1 = 3
    result1 = solution.minimumCost(
        instability1, single_cost1, pair_cost1, limit1, k1, discount1
    )
    print(result1)  # Expected: 16

    # Example 2 from the prompt.
    #
    # valid pairs:
    # - (0,1): |3-3| = 0 <= 1 => valid, cost 7
    # - (1,2): |3-10| = 7 > 1 => invalid
    # - (2,3): |10-11| = 1 <= 1 => valid, cost 6
    # - (3,4): |11-12| = 1 <= 1 => valid, cost 9
    #
    # Best plan:
    # - pair (0,1) with coupon => 7-4 = 3
    # - pair (2,3) with coupon => 6-4 = 2
    # - single 4 => 5
    # Total = 10
    instability2 = [3, 3, 10, 11, 12]
    single_cost2 = [6, 6, 8, 5, 5]
    pair_cost2 = [7, 50, 6, 9]
    limit2 = 1
    k2 = 2
    discount2 = 4
    result2 = solution.minimumCost(
        instability2, single_cost2, pair_cost2, limit2, k2, discount2
    )
    print(result2)  # Expected: 10