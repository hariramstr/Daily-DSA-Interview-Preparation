"""
Title: Longest Workout Plan Within Heart Rate Drift

Problem Description:
You are given an array heartRate where heartRate[i] is the athlete's heart rate
recorded during the i-th minute of a workout. A workout segment is considered
stable if the difference between the maximum and minimum heart rate inside that
contiguous segment is at most limit.

Your task is to return the length of the longest stable contiguous segment.

In other words, find the maximum window size such that for some indices l and r,
the subarray heartRate[l...r] satisfies:
max(heartRate[l...r]) - min(heartRate[l...r]) <= limit.

This problem models fitness tracking systems that try to identify the longest
period of steady exertion without large spikes or drops in heart rate.

Constraints:
- 1 <= heartRate.length <= 100000
- 1 <= heartRate[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: heartRate = [120, 123, 121, 126, 124, 122], limit = 4
Output: 3

Example 2:
Input: heartRate = [98, 100, 101, 99, 102, 100, 99], limit = 3
Output: 5
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_stable_segment(self, heartRate: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous segment where the difference
        between the maximum and minimum values is at most limit.

        Args:
            heartRate: List of heart rate values recorded each minute.
            limit: Maximum allowed difference between max and min in a valid window.

        Returns:
            The maximum length of a stable contiguous segment.

        Time Complexity:
            O(n), where n is the length of heartRate.
            Each index is added to and removed from each deque at most once.

        Space Complexity:
            O(n) in the worst case for the deques.
        """
        # We use the classic sliding window technique:
        #
        # - "left" marks the beginning of the current window.
        # - We move "right" from left to right, expanding the window one element at a time.
        # - If the window becomes invalid (max - min > limit), we shrink it from the left.
        #
        # The challenge is to know the current window's minimum and maximum efficiently.
        # Doing that by scanning the window every time would be too slow: O(n^2) worst case.
        #
        # To solve this efficiently, we maintain two monotonic deques:
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
        # Because when the window moves forward, we need to know whether the oldest
        # element has fallen out of the window. Indices let us check that directly.

        max_deque: Deque[int] = deque()
        min_deque: Deque[int] = deque()

        left: int = 0
        best_length: int = 0

        # Expand the window by moving "right" across the array.
        for right, value in enumerate(heartRate):
            # ---------------------------------------------------------------
            # Step 1: Insert the new element into max_deque.
            # ---------------------------------------------------------------
            # We want max_deque to remain decreasing by value.
            # So while the last element in the deque is smaller than the current value,
            # it can never become the maximum for this or any future window that includes
            # the current value. Therefore, we remove it.
            while max_deque and heartRate[max_deque[-1]] < value:
                max_deque.pop()

            # Add the current index to the back.
            max_deque.append(right)

            # ---------------------------------------------------------------
            # Step 2: Insert the new element into min_deque.
            # ---------------------------------------------------------------
            # We want min_deque to remain increasing by value.
            # So while the last element in the deque is larger than the current value,
            # it can never become the minimum for this or any future window that includes
            # the current value. Therefore, we remove it.
            while min_deque and heartRate[min_deque[-1]] > value:
                min_deque.pop()

            # Add the current index to the back.
            min_deque.append(right)

            # ---------------------------------------------------------------
            # Step 3: Shrink the window while it is invalid.
            # ---------------------------------------------------------------
            # The current maximum is at heartRate[max_deque[0]]
            # The current minimum is at heartRate[min_deque[0]]
            #
            # If max - min > limit, the window is not stable, so we must move "left"
            # forward until the condition becomes valid again.
            while heartRate[max_deque[0]] - heartRate[min_deque[0]] > limit:
                # If the index at the front of max_deque is exactly "left",
                # that means the maximum element is leaving the window.
                # Remove it from the deque.
                if max_deque[0] == left:
                    max_deque.popleft()

                # If the index at the front of min_deque is exactly "left",
                # that means the minimum element is leaving the window.
                # Remove it from the deque.
                if min_deque[0] == left:
                    min_deque.popleft()

                # Move the left boundary of the window forward by one.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: Update the best answer.
            # ---------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed to be valid.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        return best_length

    def longestSubarray(self, heartRate: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            heartRate: List of heart rate values recorded each minute.
            limit: Maximum allowed difference between max and min in a valid window.

        Returns:
            The maximum length of a stable contiguous segment.

        Time Complexity:
            O(n), where n is the length of heartRate.

        Space Complexity:
            O(n) in the worst case.
        """
        return self.longest_stable_segment(heartRate, limit)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1
    heart_rate_1: List[int] = [120, 123, 121, 126, 124, 122]
    limit_1: int = 4
    result_1: int = solution.longest_stable_segment(heart_rate_1, limit_1)
    print("Example 1 Result:", result_1)  # Expected: 3

    # Sample input 2
    heart_rate_2: List[int] = [98, 100, 101, 99, 102, 100, 99]
    limit_2: int = 3
    result_2: int = solution.longest_stable_segment(heart_rate_2, limit_2)
    print("Example 2 Result:", result_2)  # Expected: 5

    # Additional quick checks
    heart_rate_3: List[int] = [5]
    limit_3: int = 0
    result_3: int = solution.longest_stable_segment(heart_rate_3, limit_3)
    print("Additional Check 1:", result_3)  # Expected: 1

    heart_rate_4: List[int] = [1, 1, 1, 1]
    limit_4: int = 0
    result_4: int = solution.longest_stable_segment(heart_rate_4, limit_4)
    print("Additional Check 2:", result_4)  # Expected: 4