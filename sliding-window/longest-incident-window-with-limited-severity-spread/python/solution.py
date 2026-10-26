"""
Title: Longest Incident Window With Limited Severity Spread

Problem Description:
You are given an array `severity` where `severity[i]` is the severity score of the
`i`-th incident in a time-ordered monitoring stream, and an integer `limit`.

A contiguous incident window is considered stable if the difference between the
maximum severity and the minimum severity inside that window is at most `limit`.

Your task is to return the length of the longest stable contiguous window.

Formally, find the maximum value of `r - l + 1` such that for some indices
`0 <= l <= r < n`, we have:
`max(severity[l..r]) - min(severity[l..r]) <= limit`.

Constraints:
- 1 <= severity.length <= 200000
- 0 <= severity[i] <= 1000000000
- 0 <= limit <= 1000000000

Examples:
1)
Input: severity = [4, 7, 5, 6, 8, 3, 4], limit = 3
Correct Output: 4

2)
Input: severity = [10, 10, 9, 8, 7, 7, 8], limit = 2
Correct Output: 5
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_stable_window(self, severity: List[int], limit: int) -> int:
        """
        Return the length of the longest contiguous window where
        max(window) - min(window) <= limit.

        Args:
            severity: List of incident severity scores in time order.
            limit: Maximum allowed difference between the largest and smallest
                severity inside a valid window.

        Returns:
            The maximum length of a stable contiguous window.

        Time Complexity:
            O(n), where n is the length of severity.
            Each element is added to and removed from each deque at most once.

        Space Complexity:
            O(n) in the worst case for the deques.
        """
        # We use the classic sliding window technique:
        #
        # - Expand the right end of the window one step at a time.
        # - Keep track of the current window's minimum and maximum efficiently.
        # - If the window becomes invalid (max - min > limit), move the left end
        #   forward until the window becomes valid again.
        #
        # The challenge is maintaining min and max quickly.
        # Recomputing min/max for every window would be too slow: O(n^2) worst case.
        #
        # To solve this efficiently, we use two monotonic deques:
        #
        # 1) min_deque:
        #    - Stores indices of elements in increasing order of values.
        #    - The front always points to the minimum value in the current window.
        #
        # 2) max_deque:
        #    - Stores indices of elements in decreasing order of values.
        #    - The front always points to the maximum value in the current window.
        #
        # Why store indices instead of values?
        # Because when the left side of the window moves forward, we need to know
        # whether the element leaving the window is currently sitting at the front
        # of one of the deques. Indices make that easy to check.

        min_deque: Deque[int] = deque()
        max_deque: Deque[int] = deque()

        # left is the start index of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length seen so far.
        best: int = 0

        # Iterate with right as the end index of the current window.
        for right, value in enumerate(severity):
            # ---------------------------------------------------------------
            # Step 1: Insert the new element into min_deque.
            # ---------------------------------------------------------------
            # We want min_deque to remain increasing by severity value.
            #
            # If the new value is smaller than values at the back, those larger
            # values can never become the minimum for any future window that also
            # includes the new value, so we remove them.
            while min_deque and severity[min_deque[-1]] > value:
                min_deque.pop()
            min_deque.append(right)

            # ---------------------------------------------------------------
            # Step 2: Insert the new element into max_deque.
            # ---------------------------------------------------------------
            # We want max_deque to remain decreasing by severity value.
            #
            # If the new value is larger than values at the back, those smaller
            # values can never become the maximum for any future window that also
            # includes the new value, so we remove them.
            while max_deque and severity[max_deque[-1]] < value:
                max_deque.pop()
            max_deque.append(right)

            # ---------------------------------------------------------------
            # Step 3: Shrink the window from the left while it is invalid.
            # ---------------------------------------------------------------
            # The current minimum is severity[min_deque[0]]
            # The current maximum is severity[max_deque[0]]
            #
            # If max - min > limit, the window is not stable, so we must move
            # left forward until the condition becomes valid again.
            while severity[max_deque[0]] - severity[min_deque[0]] > limit:
                # If the leftmost index is exactly the one at the front of
                # min_deque, it is leaving the window, so remove it.
                if min_deque[0] == left:
                    min_deque.popleft()

                # If the leftmost index is exactly the one at the front of
                # max_deque, it is leaving the window, so remove it.
                if max_deque[0] == left:
                    max_deque.popleft()

                # Move the left boundary rightward by one position.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: The window [left, right] is now valid.
            # Update the best answer.
            # ---------------------------------------------------------------
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def longestSubarray(self, severity: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-platform method name.

        Args:
            severity: List of incident severity scores.
            limit: Maximum allowed spread in a valid window.

        Returns:
            The maximum valid window length.

        Time Complexity:
            O(n)

        Space Complexity:
            O(n)
        """
        return self.longest_stable_window(severity, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Correct reasoning:
    # [4, 7, 5, 6] -> max=7, min=4, spread=3, length=4 (valid)
    # [7, 5, 6, 8] -> max=8, min=5, spread=3, length=4 (valid)
    # No valid window of length 5 exists.
    severity_1: List[int] = [4, 7, 5, 6, 8, 3, 4]
    limit_1: int = 3
    result_1: int = solution.longest_stable_window(severity_1, limit_1)
    print(result_1)  # Expected: 4

    # Example 2 from the prompt.
    # Correct reasoning:
    # [9, 8, 7, 7, 8] -> max=9, min=7, spread=2, length=5 (valid)
    # No valid window of length 6 exists.
    severity_2: List[int] = [10, 10, 9, 8, 7, 7, 8]
    limit_2: int = 2
    result_2: int = solution.longest_stable_window(severity_2, limit_2)
    print(result_2)  # Expected: 5

    # Additional quick sanity checks.
    severity_3: List[int] = [1]
    limit_3: int = 0
    print(solution.longest_stable_window(severity_3, limit_3))  # Expected: 1

    severity_4: List[int] = [8, 2, 4, 7]
    limit_4: int = 4
    print(solution.longest_stable_window(severity_4, limit_4))  # Expected: 2