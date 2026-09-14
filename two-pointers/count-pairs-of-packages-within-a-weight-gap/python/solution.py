"""
Title: Count Pairs of Packages Within a Weight Gap

Problem Description:
You are given an integer array `weights` where `weights[i]` is the weight of the `i`th
package, and two integers `lowGap` and `highGap`. A pair of packages `(i, j)` is
considered compatible if `i < j` and the absolute difference between their weights is
between `lowGap` and `highGap`, inclusive.

In other words, a pair is valid if:
    lowGap <= |weights[i] - weights[j]| <= highGap

Return the number of compatible pairs.

A straightforward O(n^2) solution checks every pair, but that is too slow for large
inputs. We need an efficient solution for arrays with up to 10^5 elements.

Key idea:
- Sort the weights first.
- After sorting, for any pair with i < j, we know:
      weights[j] - weights[i] >= 0
  so the absolute value is no longer needed.
- Count how many pairs have difference <= highGap.
- Count how many pairs have difference < lowGap.
- Subtract the second count from the first count.
  This leaves exactly the number of pairs whose difference is in [lowGap, highGap].

This uses a classic two-pointer counting strategy.
"""

from typing import List


class Solution:
    def _count_pairs_with_diff_at_most(self, sorted_weights: List[int], limit: int) -> int:
        """
        Count how many index pairs (i, j), with i < j, satisfy:
            sorted_weights[j] - sorted_weights[i] <= limit

        Because the array is sorted, we can use a sliding window / two-pointer approach.

        Args:
            sorted_weights: The input weights sorted in non-decreasing order.
            limit: The maximum allowed difference.

        Returns:
            The number of pairs whose difference is at most `limit`.

        Time complexity:
            O(n), where n is the length of sorted_weights.

        Space complexity:
            O(1) extra space, not counting the input list.
        """
        # If the limit is negative, no non-negative difference can be <= limit.
        # This is especially useful when lowGap = 0 and we later compute
        # count(diff < lowGap) as count(diff <= lowGap - 1) = count(diff <= -1),
        # which should correctly be 0.
        if limit < 0:
            return 0

        n: int = len(sorted_weights)

        # `left` marks the beginning of the current valid window.
        # For each `right`, we want the smallest `left` such that:
        #     sorted_weights[right] - sorted_weights[left] <= limit
        #
        # Then every index from left to right - 1 forms a valid pair with `right`.
        left: int = 0

        # This will accumulate the total number of valid pairs.
        total_pairs: int = 0

        # Move `right` from left to right across the array.
        for right in range(n):
            # While the current window violates the allowed maximum difference,
            # move `left` forward to shrink the window.
            #
            # Why this works:
            # - The array is sorted.
            # - If sorted_weights[right] - sorted_weights[left] > limit,
            #   then this `left` is too far away from `right`.
            # - Increasing `left` reduces the difference because the left value
            #   becomes larger or stays the same.
            while sorted_weights[right] - sorted_weights[left] > limit:
                left += 1

            # At this point, all indices k in [left, right - 1] satisfy:
            #     sorted_weights[right] - sorted_weights[k] <= limit
            #
            # Number of such indices is:
            #     right - left
            #
            # We add that many pairs ending at `right`.
            total_pairs += right - left

        return total_pairs

    def count_compatible_pairs(self, weights: List[int], lowGap: int, highGap: int) -> int:
        """
        Return the number of compatible pairs of packages.

        A pair (i, j) is compatible if i < j and:
            lowGap <= |weights[i] - weights[j]| <= highGap

        Strategy:
        1. Sort the weights.
        2. In sorted order, absolute difference becomes simple subtraction for i < j:
               |weights[j] - weights[i]| = weights[j] - weights[i]
        3. Count pairs with difference <= highGap.
        4. Count pairs with difference < lowGap, which is the same as
           difference <= lowGap - 1.
        5. Subtract:
               valid = count(diff <= highGap) - count(diff <= lowGap - 1)

        Args:
            weights: List of package weights.
            lowGap: Minimum allowed difference, inclusive.
            highGap: Maximum allowed difference, inclusive.

        Returns:
            The number of compatible pairs.

        Time complexity:
            O(n log n), due to sorting. The two-pointer counting passes are O(n).

        Space complexity:
            O(n) in Python for the sorted copy created by `sorted(...)`.
        """
        # Step 1: Sort the weights.
        #
        # Why sorting helps:
        # - In the original unsorted array, absolute differences are awkward because
        #   either element could be larger.
        # - After sorting, for indices i < j:
        #       sorted_weights[j] >= sorted_weights[i]
        #   so:
        #       |sorted_weights[j] - sorted_weights[i]| = sorted_weights[j] - sorted_weights[i]
        #
        # This transforms the problem into counting ordered index pairs in a sorted list.
        sorted_weights: List[int] = sorted(weights)

        # Step 2: Count all pairs whose difference is at most highGap.
        #
        # This includes:
        # - all pairs with difference in [0, highGap]
        # - and therefore includes all valid pairs we want
        pairs_up_to_high: int = self._count_pairs_with_diff_at_most(sorted_weights, highGap)

        # Step 3: Count all pairs whose difference is strictly less than lowGap.
        #
        # "difference < lowGap" is equivalent to "difference <= lowGap - 1"
        # because differences are integers.
        #
        # These pairs are too small and must be excluded.
        pairs_below_low: int = self._count_pairs_with_diff_at_most(sorted_weights, lowGap - 1)

        # Step 4: Subtract to keep only pairs in the inclusive range [lowGap, highGap].
        #
        # This is a standard counting trick:
        #     count(lowGap <= diff <= highGap)
        #   = count(diff <= highGap) - count(diff < lowGap)
        #
        # Since we counted diff < lowGap as diff <= lowGap - 1, subtraction is exact.
        return pairs_up_to_high - pairs_below_low


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    weights1: List[int] = [4, 1, 7, 3]
    low_gap1: int = 2
    high_gap1: int = 4
    result1: int = solution.count_compatible_pairs(weights1, low_gap1, high_gap1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    weights2: List[int] = [5, 5, 8, 10]
    low_gap2: int = 0
    high_gap2: int = 3
    result2: int = solution.count_compatible_pairs(weights2, low_gap2, high_gap2)
    print("Example 2 Result:", result2)  # Expected: 5

    # Additional quick checks
    weights3: List[int] = [1]
    low_gap3: int = 0
    high_gap3: int = 10
    result3: int = solution.count_compatible_pairs(weights3, low_gap3, high_gap3)
    print("Single element Result:", result3)  # Expected: 0

    weights4: List[int] = [2, 2, 2]
    low_gap4: int = 0
    high_gap4: int = 0
    result4: int = solution.count_compatible_pairs(weights4, low_gap4, high_gap4)
    print("All equal weights Result:", result4)  # Expected: 3