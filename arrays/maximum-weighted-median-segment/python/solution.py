"""
Title: Maximum Weighted Median Segment

Problem Description:
You are given two integer arrays of equal length, values and weights, where values[i]
is the score recorded at position i and weights[i] is the importance of that position.
You must choose one contiguous subarray values[l..r]. The score of a chosen subarray is
defined as the weighted median of its elements, using the corresponding weights
weights[l..r].

A number x is a weighted median of the subarray if:
- the total weight of elements strictly smaller than x is less than half of the
  subarray's total weight, and
- the total weight of elements strictly greater than x is also less than half of the
  subarray's total weight.

If multiple values satisfy this condition, the weighted median of the subarray is the
smallest such value.

Your task is to return the maximum possible weighted median among all contiguous
subarrays.

Constraints:
- 1 <= n == values.length == weights.length <= 200000
- 1 <= values[i] <= 1000000000
- 1 <= weights[i] <= 1000000000
- The answer must fit in a 64-bit signed integer
"""

from typing import List


class Solution:
    def maximum_weighted_median(self, values: List[int], weights: List[int]) -> int:
        """
        Return the maximum possible weighted median among all contiguous subarrays.

        Key observation:
        A single-element subarray [values[i]] always has weighted median values[i],
        because:
        - weight of elements strictly smaller than values[i] inside that subarray is 0
        - weight of elements strictly greater than values[i] inside that subarray is 0
        - both are strictly less than half of the total weight

        Therefore every array value is achievable as a weighted median, simply by taking
        the corresponding one-element subarray. Since no weighted median can exceed the
        maximum value present in the array, the answer is exactly max(values).

        Args:
            values: Array of scores.
            weights: Array of positive weights, same length as values.

        Returns:
            The maximum possible weighted median.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # Defensive check for completeness. The problem guarantees valid input,
        # but this makes the method safer and easier to understand for beginners.
        if not values or len(values) != len(weights):
            raise ValueError("values and weights must be non-empty and have the same length")

        # Because any single element forms a valid subarray whose weighted median is
        # exactly that element's value, the best possible answer is simply the largest
        # value that appears anywhere in the array.
        return max(values)


if __name__ == "__main__":
    """
    Simple manual test runner.

    We verify the examples from the statement:
    1) values = [4, 1, 7, 3], weights = [2, 5, 3, 1]
       Single-element subarray [7] has weighted median 7, so answer is 7.

    2) values = [5, 2, 6, 2, 4], weights = [1, 4, 2, 3, 5]
       Single-element subarray [6] has weighted median 6, so answer is 6.
    """
    solution = Solution()

    values1 = [4, 1, 7, 3]
    weights1 = [2, 5, 3, 1]
    result1 = solution.maximum_weighted_median(values1, weights1)
    print(result1)  # Expected: 7

    values2 = [5, 2, 6, 2, 4]
    weights2 = [1, 4, 2, 3, 5]
    result2 = solution.maximum_weighted_median(values2, weights2)
    print(result2)  # Expected: 6