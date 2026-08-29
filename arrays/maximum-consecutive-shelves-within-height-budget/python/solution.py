"""
Title: Maximum Consecutive Shelves Within Height Budget

Problem Description:
A warehouse manager wants to display a consecutive block of products on one long shelf.
The products are already arranged in a fixed order, and product i has height heights[i].
To make the display look neat, the manager may choose any contiguous subarray of products,
but the difference between the tallest and shortest product in that chosen block must be
at most limit.

Your task is to return the length of the longest contiguous block that can be selected
while satisfying this height budget.

Formally, find the maximum length of a subarray heights[l..r] such that:
max(heights[l..r]) - min(heights[l..r]) <= limit.

The order of products cannot be changed, and you must choose a contiguous segment.

Constraints:
- 1 <= heights.length <= 100000
- 1 <= heights[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: heights = [4, 7, 6, 8, 5, 9], limit = 3
Output: 4
Explanation: One valid longest block is [7, 6, 8, 5]. Its maximum is 8 and minimum is 5,
so the difference is 3. No contiguous block of length 5 satisfies the condition.

Example 2:
Input: heights = [10, 1, 2, 4, 7, 2], limit = 5
Output: 4
Explanation: The longest valid block is [2, 4, 7, 2]. Its maximum is 7 and minimum is 2,
so the difference is 5.

A solution better than O(n^2) is expected for full credit. Be careful when the current
window becomes invalid after adding a new element; you may need to shrink the left side
while efficiently tracking both the minimum and maximum values in the current window.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_subarray(self, heights: List[int], limit: int) -> int:
        """
        Find the maximum length of a contiguous subarray such that the difference
        between the maximum and minimum values in that subarray is at most limit.

        Args:
            heights: List of product heights in fixed order.
            limit: Maximum allowed difference between tallest and shortest product
                inside the chosen contiguous block.

        Returns:
            The length of the longest valid contiguous subarray.

        Time complexity:
            O(n), where n is the length of heights.
            Each index is added to and removed from each deque at most once.

        Space complexity:
            O(n) in the worst case for the deques.
        """
        # We use the "sliding window" technique:
        #
        # - The right pointer expands the window one element at a time.
        # - If the window becomes invalid (max - min > limit), we move the left
        #   pointer rightward until the window becomes valid again.
        #
        # The challenge is efficiently knowing the current minimum and maximum
        # inside the window. Recomputing min/max every time would be too slow.
        #
        # To solve that, we maintain two deques:
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
        # whether the element leaving the window is currently at the front of one
        # of the deques. Indices make that easy to check.

        min_deque: Deque[int] = deque()
        max_deque: Deque[int] = deque()

        # left marks the start of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length seen so far.
        best: int = 0

        # We expand the window by moving right from 0 to len(heights) - 1.
        for right, value in enumerate(heights):
            # ---------------------------------------------------------------
            # Step 1: Insert the new element into min_deque.
            # ---------------------------------------------------------------
            # min_deque must remain increasing by value.
            #
            # If the new value is smaller than elements at the back, those larger
            # elements can never become the minimum for any future window that
            # includes this new value, because:
            # - they are to the left of the new value, and
            # - they are larger than the new value.
            #
            # So we remove them from the back.
            while min_deque and heights[min_deque[-1]] > value:
                min_deque.pop()

            # Now append the current index.
            min_deque.append(right)

            # ---------------------------------------------------------------
            # Step 2: Insert the new element into max_deque.
            # ---------------------------------------------------------------
            # max_deque must remain decreasing by value.
            #
            # If the new value is larger than elements at the back, those smaller
            # elements can never become the maximum for any future window that
            # includes this new value, because:
            # - they are to the left of the new value, and
            # - they are smaller than the new value.
            #
            # So we remove them from the back.
            while max_deque and heights[max_deque[-1]] < value:
                max_deque.pop()

            # Now append the current index.
            max_deque.append(right)

            # ---------------------------------------------------------------
            # Step 3: Shrink the window from the left while it is invalid.
            # ---------------------------------------------------------------
            # The current minimum is at heights[min_deque[0]].
            # The current maximum is at heights[max_deque[0]].
            #
            # If max - min > limit, the window is invalid and we must move left.
            while heights[max_deque[0]] - heights[min_deque[0]] > limit:
                # If the leftmost index is exactly the index at the front of
                # min_deque, then that minimum element is leaving the window,
                # so we remove it from min_deque.
                if min_deque[0] == left:
                    min_deque.popleft()

                # Similarly, if the leftmost index is at the front of max_deque,
                # then that maximum element is leaving the window.
                if max_deque[0] == left:
                    max_deque.popleft()

                # Move the left boundary right by one position.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: Update the best answer.
            # ---------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed valid.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # heights = [4, 7, 6, 8, 5, 9], limit = 3
    # Expected output: 4
    heights1: List[int] = [4, 7, 6, 8, 5, 9]
    limit1: int = 3
    result1: int = solution.longest_subarray(heights1, limit1)
    print(f"Input: heights = {heights1}, limit = {limit1}")
    print(f"Output: {result1}")
    print("Expected: 4")
    print()

    # Example 2:
    # heights = [10, 1, 2, 4, 7, 2], limit = 5
    # Expected output: 4
    heights2: List[int] = [10, 1, 2, 4, 7, 2]
    limit2: int = 5
    result2: int = solution.longest_subarray(heights2, limit2)
    print(f"Input: heights = {heights2}, limit = {limit2}")
    print(f"Output: {result2}")
    print("Expected: 4")
    print()

    # Additional quick sanity checks
    extra_tests = [
        ([8], 0, 1),
        ([1, 1, 1, 1], 0, 4),
        ([1, 5, 6, 7, 8, 10, 6, 5, 6], 4, 5),
        ([2, 2, 2, 4, 4, 2, 2], 2, 7),
    ]

    for heights, limit, expected in extra_tests:
        result = solution.longest_subarray(heights, limit)
        print(f"Input: heights = {heights}, limit = {limit}")
        print(f"Output: {result}")
        print(f"Expected: {expected}")
        print()