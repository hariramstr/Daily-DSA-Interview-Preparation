"""
Title: Longest Session Window With Pairwise Latency Gap Limit

Problem Description:
You are given an array `latency` where `latency[i]` is the measured response time of the
`i`-th request in a production session, and an integer `limit`.

A contiguous block of requests is called stable if for every pair of requests inside that
block, the absolute difference between their latencies is at most `limit`.

An equivalent and much more useful way to state the same condition is:
for a window latency[l..r] to be stable, we must have

    max(latency[l..r]) - min(latency[l..r]) <= limit

Your task is to return the length of the longest stable contiguous window.

Constraints:
- 1 <= latency.length <= 200000
- 0 <= latency[i] <= 10^9
- 0 <= limit <= 10^9

Examples:
1)
Input: latency = [8, 2, 4, 7], limit = 4
Output: 2

2)
Input: latency = [10, 1, 2, 4, 7, 2], limit = 5
Output: 4

Efficient Approach:
A brute-force solution would be too slow because it would repeatedly recompute the minimum
and maximum for many subarrays.

The intended efficient solution uses:
- A sliding window
- A monotonic increasing deque to track the current minimum
- A monotonic decreasing deque to track the current maximum

This allows us to maintain the min and max of the current window in O(1) amortized time
per element, leading to an overall O(n) solution.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_stable_window(self, latency: List[int], limit: int) -> int:
        """
        Return the length of the longest contiguous window where
        max(window) - min(window) <= limit.

        Args:
            latency: List of request latencies.
            limit: Maximum allowed difference between the largest and smallest
                values inside a stable window.

        Returns:
            The maximum length of a stable contiguous subarray.

        Time Complexity:
            O(n), where n is the length of latency.
            Each element is added to and removed from each deque at most once.

        Space Complexity:
            O(n) in the worst case for the deques.
        """
        # These two deques will store indices, not values.
        #
        # Why store indices instead of raw values?
        # - We need to know whether an element has moved out of the left side
        #   of the sliding window.
        # - If we only stored values, duplicates would make removal ambiguous.
        #
        # min_deque:
        #   Maintains indices whose corresponding values are in increasing order.
        #   Therefore, the front always points to the minimum value in the window.
        #
        # max_deque:
        #   Maintains indices whose corresponding values are in decreasing order.
        #   Therefore, the front always points to the maximum value in the window.
        min_deque: Deque[int] = deque()
        max_deque: Deque[int] = deque()

        # left marks the beginning of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length seen so far.
        best: int = 0

        # We expand the window by moving right from left to right across the array.
        for right, value in enumerate(latency):
            # ---------------------------------------------------------------
            # Step 1: Insert the new element into the min deque.
            # ---------------------------------------------------------------
            # We want min_deque to remain increasing by value.
            #
            # If the new value is smaller than values at the back, those larger
            # values can never become the minimum for any future window that
            # includes this new value, because:
            # - they are older (more to the left)
            # - they are larger
            #
            # So we remove them from the back.
            while min_deque and latency[min_deque[-1]] > value:
                min_deque.pop()

            # Add the current index after removing all worse candidates.
            min_deque.append(right)

            # ---------------------------------------------------------------
            # Step 2: Insert the new element into the max deque.
            # ---------------------------------------------------------------
            # We want max_deque to remain decreasing by value.
            #
            # If the new value is larger than values at the back, those smaller
            # values can never become the maximum for any future window that
            # includes this new value.
            while max_deque and latency[max_deque[-1]] < value:
                max_deque.pop()

            # Add the current index after removing all worse candidates.
            max_deque.append(right)

            # ---------------------------------------------------------------
            # Step 3: Shrink the window from the left while it is invalid.
            # ---------------------------------------------------------------
            # The current window is [left, right].
            #
            # Because:
            # - min_deque[0] is the index of the minimum value in the window
            # - max_deque[0] is the index of the maximum value in the window
            #
            # We can check validity in O(1):
            #   latency[max_deque[0]] - latency[min_deque[0]] <= limit
            #
            # If this condition fails, the window is not stable, so we must move
            # left forward until it becomes valid again.
            while latency[max_deque[0]] - latency[min_deque[0]] > limit:
                # If the leftmost index in the current window is exactly the one
                # stored at the front of min_deque, then once left moves forward,
                # that index is no longer inside the window and must be removed.
                if min_deque[0] == left:
                    min_deque.popleft()

                # Similarly for max_deque.
                if max_deque[0] == left:
                    max_deque.popleft()

                # Move the left boundary rightward by one position.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: Update the best answer.
            # ---------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed to be valid.
            # Its length is right - left + 1.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def longestSubarray(self, latency: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-platform method name.

        Args:
            latency: List of request latencies.
            limit: Maximum allowed difference between max and min in a window.

        Returns:
            The length of the longest stable contiguous window.

        Time Complexity:
            O(n)

        Space Complexity:
            O(n)
        """
        return self.longest_stable_window(latency, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    latency1: List[int] = [8, 2, 4, 7]
    limit1: int = 4
    result1: int = solution.longest_stable_window(latency1, limit1)
    print("Example 1:")
    print(f"latency = {latency1}, limit = {limit1}")
    print(f"Output = {result1}")
    print("Expected = 2")
    print()

    # Example 2
    latency2: List[int] = [10, 1, 2, 4, 7, 2]
    limit2: int = 5
    result2: int = solution.longest_stable_window(latency2, limit2)
    print("Example 2:")
    print(f"latency = {latency2}, limit = {limit2}")
    print(f"Output = {result2}")
    print("Expected = 4")
    print()

    # Additional quick sanity checks
    extra_tests: List[tuple[List[int], int]] = [
        ([1], 0),
        ([4, 4, 4, 4], 0),
        ([1, 5, 6, 7, 8, 10, 6, 5, 6], 4),
        ([1, 2, 3, 4, 5], 3),
    ]

    print("Additional Tests:")
    for arr, lim in extra_tests:
        print(f"latency = {arr}, limit = {lim}, longest stable window = {solution.longest_stable_window(arr, lim)}")