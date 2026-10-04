"""
Title: Maximum Gain from One Circular Shift Window

Problem Description:
A factory runs in repeating shifts, so its hourly performance data is considered
circular: after the last hour, the next hour is the first hour again. You are
given an integer array gain where gain[i] is the net productivity change during
hour i. Positive values increase total output, and negative values reduce it.

Choose exactly one non-empty contiguous block of hours to analyze, where the
block may wrap from the end of the array back to the beginning because the
schedule is circular. Return the maximum possible sum of the chosen block.

In other words, you must find the largest sum among all non-empty circular
subarrays of gain.

This problem is common in systems that monitor repeating schedules, rotating
buffers, or cyclic sensor logs. A valid solution should handle both standard
subarrays and wrap-around subarrays efficiently.

Constraints:
- 1 <= gain.length <= 100000
- -100000 <= gain[i] <= 100000
- The answer fits in a 32-bit signed integer.

Example 1:
Input: gain = [5, -3, 5]
Output: 10
Explanation: The best circular block is [5] from the end together with [5]
from the beginning, for a total of 10.

Example 2:
Input: gain = [-2, -3, -1]
Output: -1
Explanation: All values are negative, so the best non-empty block is the single
hour with value -1.
"""

from typing import List


class Solution:
    def maxSubarraySumCircular(self, gain: List[int]) -> int:
        """
        Compute the maximum sum of a non-empty circular subarray.

        The array is circular, so the best answer can come from:
        1. A normal non-wrapping subarray entirely inside the array.
        2. A wrapping subarray that uses the end of the array and the beginning.

        Args:
            gain: List of integers representing hourly productivity changes.

        Returns:
            The maximum possible sum among all non-empty circular subarrays.

        Time complexity:
            O(n), where n is the length of gain.

        Space complexity:
            O(1), because only a fixed number of variables are used.
        """
        # We will solve this using a very important observation:
        #
        # The best circular subarray is either:
        #
        # A) A standard subarray that does NOT wrap around.
        #    This can be found using Kadane's algorithm for maximum subarray sum.
        #
        # B) A wrapping subarray that DOES wrap around.
        #    A wrapping subarray can be thought of as:
        #       total_sum - (some middle subarray we exclude)
        #
        #    Why does this work?
        #    If we take a subarray that wraps, it means we are taking:
        #       suffix of the array + prefix of the array
        #    The only part we are NOT taking is one contiguous middle segment.
        #    So:
        #       wrapping_sum = total_sum - minimum_subarray_sum
        #
        # Therefore, the final answer is:
        #    max(max_subarray_sum, total_sum - min_subarray_sum)
        #
        # However, there is one critical edge case:
        # If all numbers are negative, then:
        #    min_subarray_sum == total_sum
        # and:
        #    total_sum - min_subarray_sum == 0
        #
        # But 0 would correspond to choosing an empty subarray, which is NOT allowed.
        # In that case, we must return the normal maximum subarray sum instead,
        # which will be the largest (least negative) single element.

        # Initialize total sum of the array.
        total_sum: int = 0

        # These variables are for Kadane's algorithm to find the maximum subarray sum.
        #
        # current_max:
        #   The best subarray sum ending exactly at the current position.
        #
        # best_max:
        #   The best subarray sum found anywhere so far.
        current_max: int = gain[0]
        best_max: int = gain[0]

        # These variables are for a "minimum Kadane" to find the minimum subarray sum.
        #
        # current_min:
        #   The minimum subarray sum ending exactly at the current position.
        #
        # best_min:
        #   The minimum subarray sum found anywhere so far.
        current_min: int = gain[0]
        best_min: int = gain[0]

        # Traverse through every value in the array once.
        for i, value in enumerate(gain):
            # Add the current value into the total array sum.
            total_sum += value

            # For the first element, we already used it to initialize all Kadane values.
            # So we skip the update formulas for index 0 to avoid duplicating work.
            if i == 0:
                continue

            # -----------------------------
            # Update maximum subarray logic
            # -----------------------------
            #
            # At this position, the best subarray ending here is either:
            # 1. Start fresh from the current value alone
            # 2. Extend the previous best-ending-here subarray
            #
            # So:
            #   current_max = max(value, current_max + value)
            #
            # This is the core of Kadane's algorithm.
            current_max = max(value, current_max + value)

            # Update the global best maximum subarray sum seen so far.
            best_max = max(best_max, current_max)

            # -----------------------------
            # Update minimum subarray logic
            # -----------------------------
            #
            # This is the mirror image of Kadane's algorithm.
            # At this position, the minimum subarray ending here is either:
            # 1. Start fresh from the current value alone
            # 2. Extend the previous minimum-ending-here subarray
            #
            # So:
            #   current_min = min(value, current_min + value)
            current_min = min(value, current_min + value)

            # Update the global best minimum subarray sum seen so far.
            best_min = min(best_min, current_min)

        # If best_max is negative, that means every element is negative.
        #
        # Why?
        # Because if there were any non-negative or positive opportunity,
        # Kadane's maximum would not stay below 0 unless all values were negative.
        #
        # In this case, the correct answer is simply the largest element,
        # which is exactly best_max.
        #
        # We must NOT use:
        #   total_sum - best_min
        # because that would become 0, representing an empty subarray.
        if best_max < 0:
            return best_max

        # Compute the best wrapping subarray sum.
        #
        # This means:
        #   take the whole array
        #   remove the minimum-sum contiguous middle segment
        #
        # The remaining elements form a valid circular subarray.
        circular_max: int = total_sum - best_min

        # The final answer is the better of:
        # 1. Best normal subarray
        # 2. Best circular (wrapping) subarray
        return max(best_max, circular_max)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Sample input 1 from the problem statement.
    gain1: List[int] = [5, -3, 5]
    result1: int = solution.maxSubarraySumCircular(gain1)
    print(f"Input: {gain1}")
    print(f"Output: {result1}")
    print("Expected: 10")
    print()

    # Sample input 2 from the problem statement.
    gain2: List[int] = [-2, -3, -1]
    result2: int = solution.maxSubarraySumCircular(gain2)
    print(f"Input: {gain2}")
    print(f"Output: {result2}")
    print("Expected: -1")
    print()

    # Additional beginner-friendly checks.
    extra_cases: List[List[int]] = [
        [1, -2, 3, -2],     # Expected 3
        [3, -1, 2, -1],     # Expected 4
        [3, -2, 2, -3],     # Expected 3
        [2],                # Expected 2
        [-5],               # Expected -5
    ]

    for case in extra_cases:
        print(f"Input: {case}")
        print(f"Output: {solution.maxSubarraySumCircular(case)}")
        print()