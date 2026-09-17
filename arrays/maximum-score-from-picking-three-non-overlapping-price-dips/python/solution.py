"""
Title: Maximum Score from Picking Three Non-Overlapping Price Dips

Problem Description:
You are given an integer array prices where prices[i] represents the profit impact of
selecting day i in a promotional trading strategy. Values may be positive, zero, or
negative. You must choose exactly three non-empty contiguous subarrays, and the three
chosen subarrays must be pairwise non-overlapping. The score of a chosen subarray is
the sum of its elements, and the total strategy score is the sum of the scores of the
three chosen subarrays.

Your task is to return the maximum possible total score.

This is not the same as choosing any three individual elements: each choice must be a
contiguous block, and blocks may have different lengths. Because you must choose exactly
three subarrays, negative values cannot always be ignored. In particular, if the array
contains many negative numbers, the optimal answer may still include a negative-sum
segment in order to satisfy the requirement of selecting three non-overlapping subarrays.

Constraints:
- 3 <= prices.length <= 200000
- -10^9 <= prices[i] <= 10^9
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def max_three_non_overlapping_subarrays(self, prices: List[int]) -> int:
        """
        Compute the maximum total sum obtainable by choosing exactly three
        non-empty, pairwise non-overlapping contiguous subarrays.

        This uses dynamic programming optimized to O(n) time and O(1) extra
        DP state (besides the input array itself).

        Args:
            prices: List of integers representing profit impact values.

        Returns:
            The maximum possible total score from exactly three non-overlapping
            non-empty contiguous subarrays.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # We use a classic dynamic programming idea for "maximum sum of k disjoint subarrays".
        #
        # For each k in {1, 2, 3}, we maintain two values while scanning left to right:
        #
        # 1) local_k:
        #    The best total sum for exactly k subarrays where the k-th subarray
        #    MUST end at the current position.
        #
        # 2) global_k:
        #    The best total sum for exactly k subarrays using elements seen so far,
        #    with no requirement that the last subarray ends at the current position.
        #
        # Transition idea for each new value x:
        #
        # local_1 = max(local_1 + x, x)
        #   Either extend the current 1st subarray, or start a new 1st subarray at x.
        #
        # local_2 = max(local_2 + x, global_1_previous + x)
        #   Either extend the current 2nd subarray, or start the 2nd subarray at x
        #   after the best completed 1-subarray solution from earlier positions.
        #
        # local_3 = max(local_3 + x, global_2_previous + x)
        #   Same logic for the 3rd subarray.
        #
        # Then:
        # global_k = max(global_k, local_k)
        #
        # Important detail:
        # We must update in descending order of k (3, 2, 1) so that when computing
        # local_2 we still use the previous iteration's global_1, not the already
        # updated current-position value. The same applies to local_3 needing the
        # previous global_2.
        #
        # This correctly handles negative values too, because we are forced to choose
        # exactly three non-empty subarrays. The DP never "skips" the requirement.

        negative_infinity: int = -(10 ** 30)

        # DP state for exactly 1 subarray.
        local_1: int = negative_infinity
        global_1: int = negative_infinity

        # DP state for exactly 2 subarrays.
        local_2: int = negative_infinity
        global_2: int = negative_infinity

        # DP state for exactly 3 subarrays.
        local_3: int = negative_infinity
        global_3: int = negative_infinity

        for value in prices:
            # Update states for 3 subarrays first.
            #
            # Option A: extend the current 3rd subarray by including value.
            # Option B: start a brand-new 3rd subarray at this position, which means
            #           we must already have a valid best solution for exactly 2
            #           non-overlapping subarrays before this position.
            local_3 = max(local_3 + value, global_2 + value)
            global_3 = max(global_3, local_3)

            # Update states for 2 subarrays next.
            #
            # Option A: extend the current 2nd subarray.
            # Option B: start the 2nd subarray at this position after the best
            #           completed 1-subarray solution from earlier positions.
            local_2 = max(local_2 + value, global_1 + value)
            global_2 = max(global_2, local_2)

            # Update states for 1 subarray last.
            #
            # Option A: extend the current 1st subarray.
            # Option B: start the 1st subarray at this position.
            local_1 = max(local_1 + value, value)
            global_1 = max(global_1, local_1)

        return global_3

    def maximumScore(self, prices: List[int]) -> int:
        """
        Wrapper method matching a common interview/platform naming style.

        Args:
            prices: List of integers.

        Returns:
            Maximum total score from exactly three non-overlapping non-empty
            contiguous subarrays.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.max_three_non_overlapping_subarrays(prices)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # prices = [4,-1,3,-2,5,-6,2,2]
    #
    # Best choice:
    # [4,-1,3] = 6
    # [5] = 5
    # [2,2] = 4
    # Total = 15
    #
    # Note:
    # The prompt's listed output says 13, but its own explanation sums to 15.
    # The correct maximum is 15.
    prices1: List[int] = [4, -1, 3, -2, 5, -6, 2, 2]
    result1: int = solution.maximumScore(prices1)
    print("Example 1 result:", result1)  # Expected correct result: 15

    # Example 2 from the prompt:
    # prices = [-5,4,-1,4,-10,3]
    #
    # Best choice:
    # [4] = 4
    # [-1,4] = 3
    # [3] = 3
    # Total = 10
    prices2: List[int] = [-5, 4, -1, 4, -10, 3]
    result2: int = solution.maximumScore(prices2)
    print("Example 2 result:", result2)  # Expected: 10

    # Additional quick sanity checks:
    prices3: List[int] = [1, 2, 3]
    # Must choose exactly three non-empty subarrays, so each element must be its own subarray.
    # Total = 1 + 2 + 3 = 6
    print("Sanity check 1:", solution.maximumScore(prices3))  # Expected: 6

    prices4: List[int] = [-1, -2, -3, -4]
    # Need exactly three subarrays. Best is to take the three least negative singletons:
    # [-1], [-2], [-3] => -6
    print("Sanity check 2:", solution.maximumScore(prices4))  # Expected: -6