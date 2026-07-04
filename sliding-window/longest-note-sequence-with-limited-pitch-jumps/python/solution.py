"""
Title: Longest Note Sequence With Limited Pitch Jumps

Problem Description:
A music learning app records a student's practice session as an array of integers
called notes, where notes[i] is the pitch played at time i. The app wants to find
the longest contiguous segment that is still considered smooth enough to review as
a single phrase. A segment is smooth if the difference between the highest pitch
and the lowest pitch inside that segment is at most limit.

Your task is to return the length of the longest contiguous subarray of notes such
that max(notes[l..r]) - min(notes[l..r]) <= limit.

This is a realistic streaming-style problem: as you extend the right end of the
window, the valid left boundary may need to move forward to keep the pitch range
within the allowed jump limit. An efficient solution is expected.

Constraints:
- 1 <= notes.length <= 200000
- 0 <= notes[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: notes = [12, 14, 13, 18, 15, 16], limit = 3
Output: 3

Example 2:
Input: notes = [7, 7, 8, 9, 6, 7, 8], limit = 2
Output: 4
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_smooth_segment(self, notes: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray where the difference
        between the maximum and minimum values is at most limit.

        Args:
            notes: List of integer pitches recorded over time.
            limit: Maximum allowed difference between highest and lowest pitch
                inside a valid segment.

        Returns:
            The maximum length of a contiguous smooth segment.

        Time complexity:
            O(n), where n is the length of notes. Each element is added to and
            removed from each deque at most once.

        Space complexity:
            O(n) in the worst case for the deques.
        """
        # We use the classic "sliding window" technique:
        #
        # - The right pointer expands the window one note at a time.
        # - If the window becomes invalid (max - min > limit),
        #   we move the left pointer forward until it becomes valid again.
        #
        # The challenge is computing the current window's minimum and maximum
        # efficiently while the window changes.
        #
        # A naive approach would scan the whole window each time, which would be
        # too slow: O(n^2) in the worst case.
        #
        # To solve this efficiently, we maintain two monotonic deques:
        #
        # 1) min_deque:
        #    - Stores values in increasing order.
        #    - The front always contains the minimum value in the current window.
        #
        # 2) max_deque:
        #    - Stores values in decreasing order.
        #    - The front always contains the maximum value in the current window.
        #
        # Why deques?
        # - We need fast insertion/removal from both ends.
        # - Each note enters and leaves each deque at most once, giving O(n) total.

        min_deque: Deque[int] = deque()
        max_deque: Deque[int] = deque()

        # left marks the beginning of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # Iterate with right as the end of the window.
        for right, value in enumerate(notes):
            # ---------------------------------------------------------------
            # Step 1: Insert the new value into min_deque.
            # ---------------------------------------------------------------
            # We want min_deque to remain increasing.
            #
            # If the last value in min_deque is greater than the new value,
            # that last value can never become the minimum for any future window
            # that includes the new value, because:
            # - it is larger than the new value
            # - it is also older than the new value
            #
            # So we remove all larger values from the back.
            while min_deque and min_deque[-1] > value:
                min_deque.pop()
            min_deque.append(value)

            # ---------------------------------------------------------------
            # Step 2: Insert the new value into max_deque.
            # ---------------------------------------------------------------
            # We want max_deque to remain decreasing.
            #
            # If the last value in max_deque is smaller than the new value,
            # that last value can never become the maximum for any future window
            # that includes the new value, because:
            # - it is smaller than the new value
            # - it is also older than the new value
            #
            # So we remove all smaller values from the back.
            while max_deque and max_deque[-1] < value:
                max_deque.pop()
            max_deque.append(value)

            # ---------------------------------------------------------------
            # Step 3: Shrink the window from the left while it is invalid.
            # ---------------------------------------------------------------
            # The current minimum is min_deque[0].
            # The current maximum is max_deque[0].
            #
            # If max - min > limit, the window is not smooth enough, so we must
            # move left forward until the condition becomes valid again.
            while max_deque[0] - min_deque[0] > limit:
                # The value leaving the window is notes[left].
                leaving_value: int = notes[left]

                # If the leaving value equals the current minimum at the front
                # of min_deque, we must remove it because it is no longer inside
                # the window after left moves forward.
                if leaving_value == min_deque[0]:
                    min_deque.popleft()

                # Similarly, if the leaving value equals the current maximum at
                # the front of max_deque, remove it from max_deque.
                if leaving_value == max_deque[0]:
                    max_deque.popleft()

                # Move the left boundary forward by one position.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: Update the best answer.
            # ---------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed valid.
            # Its length is right - left + 1.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def longestSubarray(self, notes: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            notes: List of integer pitches recorded over time.
            limit: Maximum allowed difference between highest and lowest pitch
                inside a valid segment.

        Returns:
            The maximum length of a contiguous smooth segment.

        Time complexity:
            O(n), where n is the length of notes.

        Space complexity:
            O(n) in the worst case.
        """
        return self.longest_smooth_segment(notes, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    notes1: List[int] = [12, 14, 13, 18, 15, 16]
    limit1: int = 3
    result1: int = solution.longest_smooth_segment(notes1, limit1)
    print(result1)  # Expected: 3

    # Example 2
    notes2: List[int] = [7, 7, 8, 9, 6, 7, 8]
    limit2: int = 2
    result2: int = solution.longest_smooth_segment(notes2, limit2)
    print(result2)  # Expected: 4

    # Additional quick checks
    notes3: List[int] = [5]
    limit3: int = 0
    result3: int = solution.longest_smooth_segment(notes3, limit3)
    print(result3)  # Expected: 1

    notes4: List[int] = [1, 1, 1, 1]
    limit4: int = 0
    result4: int = solution.longest_smooth_segment(notes4, limit4)
    print(result4)  # Expected: 4