"""
Title: Longest Sensor Drift Window Within Calibration Budget

Problem Description:
A factory records a sequence of integer sensor readings over time. Engineers want to
analyze the longest contiguous time window that can be considered stable enough to
recalibrate together. A window is valid if the difference between its highest reading
and lowest reading is at most `budget`.

Given an integer array `readings` and an integer `budget`, return the length of the
longest contiguous subarray such that:

    max(readings[l..r]) - min(readings[l..r]) <= budget

This models a real monitoring system where readings may fluctuate, but only within a
limited tolerance before a recalibration job must be split into smaller batches.

Constraints:
- 1 <= readings.length <= 200000
- -10^9 <= readings[i] <= 10^9
- 0 <= budget <= 10^9

Examples:
1)
Input: readings = [8, 10, 9, 12, 7, 8], budget = 3
Output: 3

2)
Input: readings = [4, 4, 5, 6, 6, 3, 4], budget = 2
Output: 5
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_stable_window(self, readings: List[int], budget: int) -> int:
        """
        Find the length of the longest contiguous subarray where the difference
        between the maximum and minimum values is at most the given budget.

        Args:
            readings: List of integer sensor readings.
            budget: Maximum allowed difference between max and min in a valid window.

        Returns:
            The maximum length of a contiguous valid window.

        Time Complexity:
            O(n), where n is the number of readings.
            Each index is added to and removed from each deque at most once.

        Space Complexity:
            O(n) in the worst case for the deques.
        """
        # We use the classic sliding window technique:
        #
        # - "left" marks the beginning of the current window.
        # - We expand the window by moving "right" from left to right.
        # - If the window becomes invalid, we shrink it from the left until it is valid again.
        #
        # The challenge is checking the current window's minimum and maximum efficiently.
        # A naive approach would scan the whole window each time, which would be too slow.
        #
        # To solve that efficiently, we maintain two monotonic deques:
        #
        # 1) max_deque:
        #    - Stores indices of elements in decreasing order of their values.
        #    - The front always points to the maximum value in the current window.
        #
        # 2) min_deque:
        #    - Stores indices of elements in increasing order of their values.
        #    - The front always points to the minimum value in the current window.
        #
        # Why store indices instead of values?
        # - Because when the left side of the window moves forward, we need to know
        #   whether the element leaving the window is currently sitting at the front
        #   of one of the deques. Indices let us check that directly.

        n: int = len(readings)

        # Deque for tracking candidates for the maximum value in the current window.
        max_deque: Deque[int] = deque()

        # Deque for tracking candidates for the minimum value in the current window.
        min_deque: Deque[int] = deque()

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far.
        best_length: int = 0

        # Move the right boundary one step at a time.
        for right in range(n):
            current_value: int = readings[right]

            # ------------------------------------------------------------
            # Step 1: Insert the new reading into max_deque
            # ------------------------------------------------------------
            # We want max_deque to remain decreasing by value.
            #
            # If the new value is greater than values at the back of the deque,
            # those smaller values can never become the maximum for this window
            # or any future window that includes the new value, because:
            # - they are smaller
            # - they are also older (further left)
            #
            # So we remove them from the back.
            while max_deque and readings[max_deque[-1]] < current_value:
                max_deque.pop()

            # Add the current index to the back.
            max_deque.append(right)

            # ------------------------------------------------------------
            # Step 2: Insert the new reading into min_deque
            # ------------------------------------------------------------
            # We want min_deque to remain increasing by value.
            #
            # If the new value is smaller than values at the back of the deque,
            # those larger values can never become the minimum for this window
            # or any future window that includes the new value, because:
            # - they are larger
            # - they are also older
            #
            # So we remove them from the back.
            while min_deque and readings[min_deque[-1]] > current_value:
                min_deque.pop()

            # Add the current index to the back.
            min_deque.append(right)

            # ------------------------------------------------------------
            # Step 3: Shrink the window while it is invalid
            # ------------------------------------------------------------
            # The current maximum is at readings[max_deque[0]]
            # The current minimum is at readings[min_deque[0]]
            #
            # If max - min > budget, the window is invalid and we must move
            # the left boundary to the right until the window becomes valid again.
            while readings[max_deque[0]] - readings[min_deque[0]] > budget:
                # If the element leaving the window is exactly the index stored
                # at the front of max_deque, remove it because it is no longer
                # inside the window after left moves forward.
                if max_deque[0] == left:
                    max_deque.popleft()

                # Similarly, remove it from min_deque if needed.
                if min_deque[0] == left:
                    min_deque.popleft()

                # Actually shrink the window.
                left += 1

            # ------------------------------------------------------------
            # Step 4: Update the best valid window length
            # ------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed valid.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        return best_length

    def longestSubarray(self, readings: List[int], budget: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            readings: List of integer sensor readings.
            budget: Maximum allowed difference between max and min in a valid window.

        Returns:
            The maximum length of a contiguous valid window.

        Time Complexity:
            O(n), where n is the number of readings.

        Space Complexity:
            O(n) in the worst case.
        """
        return self.longest_stable_window(readings, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    readings1: List[int] = [8, 10, 9, 12, 7, 8]
    budget1: int = 3
    result1: int = solution.longest_stable_window(readings1, budget1)
    print(result1)  # Expected: 3

    # Example 2
    readings2: List[int] = [4, 4, 5, 6, 6, 3, 4]
    budget2: int = 2
    result2: int = solution.longest_stable_window(readings2, budget2)
    print(result2)  # Expected: 5

    # Additional quick checks
    readings3: List[int] = [1]
    budget3: int = 0
    result3: int = solution.longest_stable_window(readings3, budget3)
    print(result3)  # Expected: 1

    readings4: List[int] = [1, 2, 3, 4]
    budget4: int = 0
    result4: int = solution.longest_stable_window(readings4, budget4)
    print(result4)  # Expected: 1