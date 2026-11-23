"""
Title: Longest Snack Stall Run with Limited Freshness Range

Problem Description:
You are given an array freshness where freshness[i] represents the freshness score
of the i-th snack stall along a street. A tourist wants to visit a contiguous run
of stalls, but only if the stalls in that run are reasonably consistent in quality.

A run is considered valid if the difference between the maximum freshness score and
the minimum freshness score inside the run is at most limit.

Return the length of the longest valid contiguous run of stalls.

In other words, find the maximum window size such that for some subarray
freshness[l..r], max(freshness[l..r]) - min(freshness[l..r]) <= limit.

This problem should be solved efficiently for large inputs. A brute-force solution
that checks every subarray will be too slow. We use a sliding window together with
two monotonic deques to maintain the current window's minimum and maximum values
in O(1) amortized time per element.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_valid_run(self, freshness: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray where
        max(subarray) - min(subarray) <= limit.

        Args:
            freshness: List of freshness scores for snack stalls.
            limit: Maximum allowed difference between the maximum and minimum
                freshness values inside a valid run.

        Returns:
            The maximum length of a valid contiguous run.

        Time complexity:
            O(n), where n is the length of freshness.
            Each element is added to and removed from each deque at most once.

        Space complexity:
            O(n) in the worst case for the deques.
        """
        # These deques will store indices, not values.
        #
        # Why store indices?
        # - We need to know whether an element has moved out of the current window.
        # - If we only stored values, we could not reliably remove the correct one
        #   when the left side of the window moves forward.
        #
        # max_deque:
        # - Maintains indices of elements in decreasing order of freshness values.
        # - The front always points to the maximum value in the current window.
        #
        # min_deque:
        # - Maintains indices of elements in increasing order of freshness values.
        # - The front always points to the minimum value in the current window.
        max_deque: Deque[int] = deque()
        min_deque: Deque[int] = deque()

        # left is the starting index of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window by moving right from left to right across the array.
        for right, value in enumerate(freshness):
            # ------------------------------------------------------------
            # Step 1: Insert the current element into max_deque
            # ------------------------------------------------------------
            # We want max_deque to remain decreasing by value.
            #
            # If the current value is greater than values at the back of the deque,
            # those smaller values can never become the maximum for this window
            # or any future window that includes the current element.
            #
            # So we remove them from the back.
            while max_deque and freshness[max_deque[-1]] < value:
                max_deque.pop()

            # Add the current index to the back.
            max_deque.append(right)

            # ------------------------------------------------------------
            # Step 2: Insert the current element into min_deque
            # ------------------------------------------------------------
            # We want min_deque to remain increasing by value.
            #
            # If the current value is smaller than values at the back of the deque,
            # those larger values can never become the minimum for this window
            # or any future window that includes the current element.
            #
            # So we remove them from the back.
            while min_deque and freshness[min_deque[-1]] > value:
                min_deque.pop()

            # Add the current index to the back.
            min_deque.append(right)

            # ------------------------------------------------------------
            # Step 3: Shrink the window while it is invalid
            # ------------------------------------------------------------
            # The current maximum is at freshness[max_deque[0]]
            # The current minimum is at freshness[min_deque[0]]
            #
            # If max - min > limit, the window is invalid and we must move
            # the left boundary to the right until the window becomes valid again.
            while freshness[max_deque[0]] - freshness[min_deque[0]] > limit:
                # If the leftmost index is exactly the index at the front of
                # max_deque, that means the current maximum is leaving the window,
                # so we remove it from the deque.
                if max_deque[0] == left:
                    max_deque.popleft()

                # Similarly, if the leftmost index is the front of min_deque,
                # the current minimum is leaving the window, so remove it.
                if min_deque[0] == left:
                    min_deque.popleft()

                # Move the left boundary right by one position.
                left += 1

            # ------------------------------------------------------------
            # Step 4: Update the best answer
            # ------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed to be valid.
            # Its length is right - left + 1.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # freshness = [8, 6, 7, 9, 10, 5], limit = 3
    #
    # Valid length-4 run:
    # [8, 6, 7, 9] -> max = 9, min = 6, difference = 3, valid
    #
    # [6, 7, 9, 10] -> max = 10, min = 6, difference = 4, invalid
    #
    # Therefore, the correct answer is 4.
    freshness_1: List[int] = [8, 6, 7, 9, 10, 5]
    limit_1: int = 3
    result_1: int = solution.longest_valid_run(freshness_1, limit_1)
    print("Example 1 Result:", result_1)  # Expected: 4

    # Example 2
    # freshness = [4, 4, 4, 2, 3, 5, 6], limit = 1
    #
    # One longest valid run:
    # [4, 4, 4] -> max = 4, min = 4, difference = 0, valid
    #
    # No valid run longer than 3 exists under the given limit.
    # Therefore, the correct answer is 3.
    freshness_2: List[int] = [4, 4, 4, 2, 3, 5, 6]
    limit_2: int = 1
    result_2: int = solution.longest_valid_run(freshness_2, limit_2)
    print("Example 2 Result:", result_2)  # Expected: 3

    # Additional quick sanity checks
    freshness_3: List[int] = [1]
    limit_3: int = 0
    result_3: int = solution.longest_valid_run(freshness_3, limit_3)
    print("Additional Test 1 Result:", result_3)  # Expected: 1

    freshness_4: List[int] = [1, 2, 3, 4, 5]
    limit_4: int = 2
    result_4: int = solution.longest_valid_run(freshness_4, limit_4)
    print("Additional Test 2 Result:", result_4)  # Expected: 3