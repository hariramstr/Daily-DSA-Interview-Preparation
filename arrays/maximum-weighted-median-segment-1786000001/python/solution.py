"""
Title: Maximum Weighted Median Segment

Problem Description:
You are given two integer arrays of equal length, values and weights, where values[i]
is the score of the i-th event and weights[i] is its importance.

For any contiguous subarray values[l..r], define its weighted median as the smallest
number x such that the total weight of elements in the subarray with value <= x is at
least half of the total subarray weight, rounded up. The score of a segment is this
weighted median.

Your task is to find the maximum possible score among all contiguous subarrays.

In other words, among every non-empty subarray, compute its weighted median, and return
the largest weighted median that can appear.

Constraints:
- 1 <= n <= 200000
- 1 <= values[i] <= 1000000000
- 1 <= weights[i] <= 1000000000
- values.length == weights.length
"""

from typing import List


class Solution:
    def maximum_weighted_median_segment(self, values: List[int], weights: List[int]) -> int:
        """
        Return the maximum weighted median among all non-empty contiguous subarrays.

        Key observation:
        A single-element subarray [values[i]] always has weighted median values[i],
        because the only element contributes all of the segment's weight and therefore
        immediately reaches the required half-weight threshold.

        Since every weighted median of any segment must be one of the values present
        in that segment, no segment can have weighted median larger than the maximum
        element appearing anywhere in the array.

        Therefore:
        - Lower bound: max(values) is achievable by taking the one-element segment
          containing that maximum value.
        - Upper bound: no segment can have weighted median greater than max(values).

        So the answer is exactly max(values).

        Args:
            values: Array of event scores.
            weights: Array of positive weights, same length as values.

        Returns:
            The maximum possible weighted median over all contiguous non-empty subarrays.

        Time complexity:
            O(n), because we scan the values array once to find its maximum.

        Space complexity:
            O(1) auxiliary space.
        """
        # The problem statement may sound much more complicated because weighted medians
        # over arbitrary segments are indeed subtle in general.
        #
        # However, the question asks for the MAXIMUM weighted median over all segments.
        # This changes everything:
        #
        # 1. Consider any index i.
        # 2. The segment consisting of only that one element is [i..i].
        # 3. Its total weight is weights[i].
        # 4. Half of that total weight, rounded up, is still weights[i].
        # 5. The cumulative weight of elements <= values[i] inside this one-element
        #    segment is exactly weights[i].
        # 6. Therefore the weighted median of [values[i]] is values[i].
        #
        # So every array value is achievable as the weighted median of some segment.
        #
        # In particular, the largest array value is achievable.
        #
        # Also, a weighted median of any segment must be one of the values inside that
        # segment, so it can never exceed the global maximum value in the whole array.
        #
        # Combining both facts:
        #     answer = max(values)
        #
        # We still keep the implementation clean and explicit for readability.

        if not values:
            # The constraints guarantee at least one element, but this defensive check
            # makes the method safer if reused elsewhere.
            raise ValueError("values must be non-empty")

        if len(values) != len(weights):
            # The problem guarantees equal lengths, but validating inputs is good practice.
            raise ValueError("values and weights must have the same length")

        # Find and return the largest value.
        # This is the maximum possible weighted median because the single-element segment
        # containing this value has exactly that weighted median.
        return max(values)


if __name__ == "__main__":
    # Create a Solution instance.
    solution = Solution()

    # Example 1 from the problem statement.
    values1 = [4, 1, 7, 3]
    weights1 = [2, 5, 4, 1]
    result1 = solution.maximum_weighted_median_segment(values1, weights1)
    print(result1)  # Expected: 7

    # Example 2 from the problem statement.
    values2 = [5, 2, 5, 1, 4]
    weights2 = [1, 10, 1, 1, 1]
    result2 = solution.maximum_weighted_median_segment(values2, weights2)
    print(result2)  # Expected: 5

    # Additional small sanity check.
    values3 = [2]
    weights3 = [100]
    result3 = solution.maximum_weighted_median_segment(values3, weights3)
    print(result3)  # Expected: 2