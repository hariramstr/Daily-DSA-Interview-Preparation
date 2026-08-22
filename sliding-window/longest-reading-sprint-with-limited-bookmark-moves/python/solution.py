"""
Title: Longest Reading Sprint With Limited Bookmark Moves

Problem Description:
You are given an array `pages` where `pages[i]` is the number of pages in the `i`th
chapter of an online course, in the order they must be read. A student wants to
complete the longest possible contiguous reading sprint. However, switching between
chapters with very different lengths is mentally expensive.

For any contiguous sprint `pages[l..r]`, define its effort as:
    max(pages[l..r]) - min(pages[l..r])

The student can only maintain focus if this effort is at most `limit`.

Return the length of the longest contiguous subarray whose effort does not exceed
`limit`.

In other words, find the maximum number of consecutive chapters such that the
difference between the largest and smallest chapter lengths in that window is at
most `limit`.

A correct solution is expected to use a sliding window approach efficiently, since
the input size can be large.

Constraints:
- 1 <= pages.length <= 100000
- 1 <= pages[i] <= 1000000000
- 0 <= limit <= 1000000000
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_reading_sprint(self, pages: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray where
        max(window) - min(window) <= limit.

        Args:
            pages: List of chapter page counts.
            limit: Maximum allowed difference between the largest and smallest
                values inside a valid window.

        Returns:
            The maximum length of a contiguous valid reading sprint.

        Time Complexity:
            O(n), where n is the length of pages.
            Each element is added to and removed from each deque at most once.

        Space Complexity:
            O(n) in the worst case for the deques.
        """
        # We will use the classic sliding window technique:
        #
        # - Expand the right side of the window one chapter at a time.
        # - Keep track of the current window's minimum and maximum efficiently.
        # - If the window becomes invalid (max - min > limit), move the left side
        #   forward until the window becomes valid again.
        #
        # The main challenge is getting the current min and max quickly.
        # If we recomputed min(window) and max(window) every time, that could
        # take O(n) per step, which would be too slow for n up to 100000.
        #
        # To solve that, we use two monotonic deques:
        #
        # 1) max_deque:
        #    - Stores values in decreasing order.
        #    - The front always contains the maximum value in the current window.
        #
        # 2) min_deque:
        #    - Stores values in increasing order.
        #    - The front always contains the minimum value in the current window.
        #
        # Why deques?
        # - We need fast insertion/removal from both ends.
        # - Each value enters and leaves each deque at most once, giving O(n) total.

        max_deque: Deque[int] = deque()
        min_deque: Deque[int] = deque()

        # left marks the start of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # Iterate with right as the end of the current window.
        for right, value in enumerate(pages):
            # ---------------------------------------------------------------
            # Step 1: Add the new value to max_deque.
            # ---------------------------------------------------------------
            # We want max_deque to remain in decreasing order.
            # That means:
            # - While the last value in max_deque is smaller than the new value,
            #   it can never become the maximum for this or any future window
            #   that also contains the new value.
            # - So we remove it from the back.
            while max_deque and max_deque[-1] < value:
                max_deque.pop()
            max_deque.append(value)

            # ---------------------------------------------------------------
            # Step 2: Add the new value to min_deque.
            # ---------------------------------------------------------------
            # We want min_deque to remain in increasing order.
            # That means:
            # - While the last value in min_deque is larger than the new value,
            #   it can never become the minimum for this or any future window
            #   that also contains the new value.
            # - So we remove it from the back.
            while min_deque and min_deque[-1] > value:
                min_deque.pop()
            min_deque.append(value)

            # ---------------------------------------------------------------
            # Step 3: Shrink the window from the left while it is invalid.
            # ---------------------------------------------------------------
            # The current maximum is at max_deque[0].
            # The current minimum is at min_deque[0].
            #
            # If their difference is greater than limit, the window is invalid.
            # We must move left forward until the window becomes valid again.
            while max_deque[0] - min_deque[0] > limit:
                # The value leaving the window is pages[left].
                outgoing: int = pages[left]

                # If the outgoing value equals the current maximum at the front
                # of max_deque, then that front element is no longer inside the
                # window and must be removed.
                if outgoing == max_deque[0]:
                    max_deque.popleft()

                # Similarly, if the outgoing value equals the current minimum at
                # the front of min_deque, remove it.
                if outgoing == min_deque[0]:
                    min_deque.popleft()

                # Move the left boundary rightward by one position.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: Update the best answer.
            # ---------------------------------------------------------------
            # At this point, the window [left..right] is guaranteed valid.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def longestSubarray(self, pages: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            pages: List of chapter page counts.
            limit: Maximum allowed difference between max and min in a window.

        Returns:
            The maximum valid contiguous subarray length.

        Time Complexity:
            O(n), where n is the length of pages.

        Space Complexity:
            O(n) in the worst case.
        """
        return self.longest_reading_sprint(pages, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # pages = [12, 15, 14, 10, 13, 18], limit = 5
    # Longest valid window is [12, 15, 14, 10, 13]
    # max = 15, min = 10, difference = 5 -> valid
    # length = 5
    pages1: List[int] = [12, 15, 14, 10, 13, 18]
    limit1: int = 5
    result1: int = solution.longest_reading_sprint(pages1, limit1)
    print(f"Example 1 result: {result1}")  # Expected: 5

    # Example 2
    # pages = [7, 7, 7, 20, 21, 22], limit = 2
    # Valid longest windows include [7, 7, 7] and [20, 21, 22]
    # length = 3
    pages2: List[int] = [7, 7, 7, 20, 21, 22]
    limit2: int = 2
    result2: int = solution.longest_reading_sprint(pages2, limit2)
    print(f"Example 2 result: {result2}")  # Expected: 3

    # Additional quick sanity checks
    pages3: List[int] = [5]
    limit3: int = 0
    result3: int = solution.longest_reading_sprint(pages3, limit3)
    print(f"Single chapter result: {result3}")  # Expected: 1

    pages4: List[int] = [1, 10, 1, 10, 1]
    limit4: int = 0
    result4: int = solution.longest_reading_sprint(pages4, limit4)
    print(f"Alternating values result: {result4}")  # Expected: 1)