"""
Title: Longest Reading Lamp Stretch Within Battery Budget

Problem Description:
A student uses a rechargeable reading lamp while studying late at night.
You are given an integer array batteryUse where batteryUse[i] is the number
of battery units consumed during minute i, and an integer budget representing
the maximum total battery units the lamp can spend before it must be recharged.

Your task is to find the length of the longest contiguous block of minutes
during which the total battery consumption is less than or equal to budget.

In other words, return the maximum size of a subarray whose sum does not
exceed budget.

This problem is meant to be solved efficiently using a sliding window.
Since all battery usage values are non-negative, once a window exceeds the
budget, moving the left side forward is enough to restore validity.

Constraints:
- 1 <= batteryUse.length <= 100000
- 0 <= batteryUse[i] <= 10000
- 0 <= budget <= 1000000000

Example 1:
Input: batteryUse = [2, 1, 3, 2, 1], budget = 5
Output: 2

Example 2:
Input: batteryUse = [0, 1, 1, 0, 2, 1], budget = 3
Output: 4
"""

from typing import List


class Solution:
    def longest_reading_lamp_stretch(self, battery_use: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most budget.

        Args:
            battery_use: A list of non-negative integers where each value represents
                battery units consumed in one minute.
            budget: The maximum allowed total battery consumption for a valid window.

        Returns:
            The length of the longest contiguous subarray with sum <= budget.

        Time complexity:
            O(n), where n is the length of battery_use, because each element is added
            to the window once and removed from the window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The sliding window technique works perfectly here because:
        # 1. All values are non-negative.
        # 2. If the current window sum becomes too large, moving the left pointer
        #    to the right can only decrease (or keep) the sum.
        #
        # This property is exactly what makes sliding window efficient for this problem.

        # Left boundary of the current window.
        left: int = 0

        # Running sum of the current window battery_use[left:right+1].
        current_sum: int = 0

        # Best answer found so far: the maximum valid window length.
        max_length: int = 0

        # We expand the window by moving the right pointer one step at a time.
        for right in range(len(battery_use)):
            # Include the new rightmost element in the current window.
            current_sum += battery_use[right]

            # If the window is invalid (sum exceeds budget), we must shrink it
            # from the left until it becomes valid again.
            #
            # Why a while loop instead of an if statement?
            # Because removing just one element may still leave the sum > budget.
            # We keep shrinking until the window satisfies the condition.
            while current_sum > budget and left <= right:
                current_sum -= battery_use[left]
                left += 1

            # At this point, the window from left to right is guaranteed valid:
            # current_sum <= budget
            #
            # So we compute its length and update the best answer if needed.
            current_window_length: int = right - left + 1
            if current_window_length > max_length:
                max_length = current_window_length

        # After checking every possible right boundary, max_length stores
        # the longest valid contiguous block.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    battery_use_1: List[int] = [2, 1, 3, 2, 1]
    budget_1: int = 5
    result_1: int = solution.longest_reading_lamp_stretch(battery_use_1, budget_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2
    battery_use_2: List[int] = [0, 1, 1, 0, 2, 1]
    budget_2: int = 3
    result_2: int = solution.longest_reading_lamp_stretch(battery_use_2, budget_2)
    print("Example 2 Result:", result_2)  # Expected: 4

    # Additional simple checks
    battery_use_3: List[int] = [1, 2, 3]
    budget_3: int = 0
    result_3: int = solution.longest_reading_lamp_stretch(battery_use_3, budget_3)
    print("Additional Check 1:", result_3)  # Expected: 0

    battery_use_4: List[int] = [0, 0, 0]
    budget_4: int = 0
    result_4: int = solution.longest_reading_lamp_stretch(battery_use_4, budget_4)
    print("Additional Check 2:", result_4)  # Expected: 3