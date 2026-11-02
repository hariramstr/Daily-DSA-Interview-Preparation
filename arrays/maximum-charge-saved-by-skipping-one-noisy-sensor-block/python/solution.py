"""
Title: Maximum Charge Saved by Skipping One Noisy Sensor Block

Problem Description:
You are given an integer array readings where readings[i] represents the net battery
impact of processing the i-th sensor reading in order. A positive value means that
processing that reading recharges the system by that amount, while a negative value
means it consumes battery. The device must process the stream from left to right, but
it is allowed to skip exactly one contiguous block of readings. Skipping a block means
none of the values inside that block contribute to the final total. The skipped block
may be empty only if you choose not to skip anything.

Your task is to return the maximum possible total battery impact after processing the
array and optionally skipping one contiguous block.

In other words, choose indices l and r with 0 <= l <= r < n and remove readings[l...r],
or choose to remove nothing, so that the sum of the remaining elements is as large as
possible.

A correct solution must handle very large arrays efficiently.

Constraints:
- 1 <= readings.length <= 200000
- -1000000000 <= readings[i] <= 1000000000
- The answer fits in a signed 64-bit integer.

Example 1:
Input: readings = [4, -7, 3, 6, -2, 5]
Output: 16
Explanation: Skip the block [-7]. The remaining sum is 4 + 3 + 6 - 2 + 5 = 16.

Example 2:
Input: readings = [-5, -2, -8]
Output: 0
Explanation: Skip the entire array. The remaining sum is 0, which is better than
keeping any negative values.

Key Insight:
If the total sum of the array is S, and we remove a contiguous block whose sum is X,
then the remaining sum becomes:

    S - X

So to maximize the remaining sum, we want to remove the contiguous block with the
smallest possible sum.

There are three possibilities:
1. Remove nothing -> remaining sum = total sum
2. Remove some proper subarray -> remaining sum = total sum - minimum_subarray_sum
3. Remove the entire array -> remaining sum = 0

A single pass variant of Kadane's algorithm can find the minimum subarray sum in O(n).
"""

from typing import List


class Solution:
    def max_battery_impact_after_skip(self, readings: List[int]) -> int:
        """
        Compute the maximum possible total battery impact after optionally skipping
        one contiguous block of readings.

        The method works by:
        1. Computing the total sum of the array.
        2. Finding the minimum-sum contiguous subarray.
        3. Comparing:
           - keeping everything
           - removing the minimum-sum subarray
           - removing the entire array

        Args:
            readings: List of integers representing battery impact per reading.

        Returns:
            The maximum possible remaining sum after removing at most one contiguous block.

        Time complexity:
            O(n), where n is the number of readings.

        Space complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # First, compute the total sum of all readings.
        # This represents the result if we choose to remove nothing.
        total_sum: int = sum(readings)

        # We now need the minimum-sum contiguous subarray.
        #
        # Why?
        # If we remove a block with sum X, the remaining total becomes total_sum - X.
        # To make this as large as possible, X should be as small as possible.
        #
        # So the problem becomes:
        # "Find the contiguous subarray with the minimum sum."
        #
        # We can do this with a Kadane-style dynamic programming scan.
        #
        # current_min_ending_here:
        #   The minimum sum of any subarray that MUST end at the current index.
        #
        # min_subarray_sum:
        #   The best (smallest) subarray sum seen anywhere so far.
        current_min_ending_here: int = readings[0]
        min_subarray_sum: int = readings[0]

        # Process the array from left to right starting at index 1,
        # because index 0 was already used to initialize the DP state.
        for value in readings[1:]:
            # At each position, we have two choices:
            #
            # 1. Start a brand-new subarray at this element alone.
            #    Sum would be just "value".
            #
            # 2. Extend the previous minimum-ending-here subarray by adding this value.
            #    Sum would be current_min_ending_here + value.
            #
            # Since we want the minimum possible sum, we take the smaller of the two.
            current_min_ending_here = min(value, current_min_ending_here + value)

            # Update the global minimum subarray sum if the current ending subarray
            # is even smaller than anything we have seen before.
            min_subarray_sum = min(min_subarray_sum, current_min_ending_here)

        # Candidate 1: remove nothing.
        # This is allowed because the skipped block may be empty.
        keep_all: int = total_sum

        # Candidate 2: remove the minimum-sum contiguous subarray.
        # This is the best non-empty block to remove.
        remove_min_block: int = total_sum - min_subarray_sum

        # Candidate 3: remove the entire array.
        # This always leaves 0.
        #
        # This case is especially important when all numbers are negative.
        # Example: [-5, -2, -8]
        # total_sum = -15
        # min_subarray_sum = -15 (the whole array)
        # remove_min_block = 0
        # So the answer becomes 0.
        remove_all: int = 0

        # The final answer is the best among all allowed choices.
        return max(keep_all, remove_min_block, remove_all)

    def maximumChargeSavedBySkippingOneNoisySensorBlock(self, readings: List[int]) -> int:
        """
        Wrapper method matching a descriptive problem-style name.

        Args:
            readings: List of integers representing battery impact per reading.

        Returns:
            The maximum possible remaining battery impact after optionally removing
            one contiguous block.

        Time complexity:
            O(n), where n is the number of readings.

        Space complexity:
            O(1).
        """
        return self.max_battery_impact_after_skip(readings)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # readings = [4, -7, 3, 6, -2, 5]
    #
    # Total sum = 4 + (-7) + 3 + 6 + (-2) + 5 = 9
    #
    # Minimum-sum contiguous subarray is [-7], sum = -7
    #
    # If we remove [-7], remaining sum = 9 - (-7) = 16
    #
    # This matches the expected output.
    readings1: List[int] = [4, -7, 3, 6, -2, 5]
    result1: int = solution.max_battery_impact_after_skip(readings1)
    print(f"Input: {readings1}")
    print(f"Output: {result1}")
    print("Expected: 16")
    print()

    # Example 2:
    # readings = [-5, -2, -8]
    #
    # Total sum = -15
    #
    # Minimum-sum contiguous subarray is the whole array, sum = -15
    #
    # Removing the whole array leaves 0
    #
    # This matches the expected output.
    readings2: List[int] = [-5, -2, -8]
    result2: int = solution.max_battery_impact_after_skip(readings2)
    print(f"Input: {readings2}")
    print(f"Output: {result2}")
    print("Expected: 0")
    print()

    # Additional beginner-friendly checks:

    # If all values are positive, removing nothing is best.
    readings3: List[int] = [2, 4, 6]
    result3: int = solution.max_battery_impact_after_skip(readings3)
    print(f"Input: {readings3}")
    print(f"Output: {result3}")
    print("Expected: 12")
    print()

    # If there is a negative block in the middle, removing it helps.
    readings4: List[int] = [10, -3, -4, 8]
    result4: int = solution.max_battery_impact_after_skip(readings4)
    print(f"Input: {readings4}")
    print(f"Output: {result4}")
    print("Expected: 18")
    print()

    # Single-element cases.
    readings5: List[int] = [7]
    result5: int = solution.max_battery_impact_after_skip(readings5)
    print(f"Input: {readings5}")
    print(f"Output: {result5}")
    print("Expected: 7")
    print()

    readings6: List[int] = [-7]
    result6: int = solution.max_battery_impact_after_skip(readings6)
    print(f"Input: {readings6}")
    print(f"Output: {result6}")
    print("Expected: 0")