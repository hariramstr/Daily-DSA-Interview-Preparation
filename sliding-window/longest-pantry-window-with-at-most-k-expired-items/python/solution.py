"""
Title: Longest Pantry Window With At Most K Expired Items

Problem Description:
A grocery store tracks a row of pantry items in the order they were scanned during an inspection.
Each item is marked as either fresh or expired. You are given an array `items` where `items[i] = 0`
means the `i`th item is fresh and `items[i] = 1` means it is expired. You are also given an integer
`k`, representing the maximum number of expired items the manager is willing to temporarily tolerate
inside one continuous inspection segment.

Your task is to return the length of the longest contiguous segment of `items` that contains at most
`k` expired items.

In other words, find the largest window of consecutive entries where the number of `1`s is less than
or equal to `k`.

This is an interview-style sliding window problem. A good solution should scan the array efficiently
without checking every possible subarray.

Constraints:
- 1 <= items.length <= 100000
- items[i] is either 0 or 1
- 0 <= k <= items.length

Example 1:
Input: items = [0,1,0,0,1,0,0], k = 1
Output: 4

Example 2:
Input: items = [1,1,0,0,0,1,0], k = 2
Output: 6
"""

from typing import List


class Solution:
    def longest_pantry_window(self, items: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous segment containing at most k expired items.

        Args:
            items: A list of integers where 0 means fresh and 1 means expired.
            k: The maximum number of expired items allowed inside the window.

        Returns:
            The maximum length of a contiguous segment with at most k expired items.

        Time Complexity:
            O(n), where n is the length of items, because each pointer moves at most n times.

        Space Complexity:
            O(1), because only a few extra variables are used.
        """
        # `left` is the starting index of our current sliding window.
        # We will expand the window by moving `right`, and shrink it by moving `left`
        # whenever the window becomes invalid.
        left: int = 0

        # This variable stores how many expired items (value 1) are currently inside
        # the active window from `left` to `right`.
        expired_count: int = 0

        # This variable keeps track of the best (largest) valid window length found so far.
        max_length: int = 0

        # We move `right` from the beginning of the array to the end.
        # At each step, we include items[right] into the current window.
        for right in range(len(items)):
            # If the new item is expired, increase the count of expired items
            # inside the current window.
            if items[right] == 1:
                expired_count += 1

            # At this point, the window is items[left:right+1].
            # If expired_count is greater than k, the window is invalid.
            # We must shrink it from the left side until it becomes valid again.
            while expired_count > k:
                # Before moving `left` forward, check whether the item leaving
                # the window is expired. If it is, reduce expired_count because
                # that expired item is no longer inside the window.
                if items[left] == 1:
                    expired_count -= 1

                # Move the left boundary one step to the right to shrink the window.
                left += 1

            # Now the window is guaranteed to be valid:
            # it contains at most k expired items.
            # Compute its length.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any
            # previously seen valid window.
            if current_length > max_length:
                max_length = current_length

        # After scanning the entire array, max_length stores the answer.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    items1: List[int] = [0, 1, 0, 0, 1, 0, 0]
    k1: int = 1
    result1: int = solution.longest_pantry_window(items1, k1)
    print(result1)  # Expected: 4

    # Example 2
    items2: List[int] = [1, 1, 0, 0, 0, 1, 0]
    k2: int = 2
    result2: int = solution.longest_pantry_window(items2, k2)
    print(result2)  # Expected: 6

    # Additional quick checks
    items3: List[int] = [0, 0, 0, 0]
    k3: int = 0
    result3: int = solution.longest_pantry_window(items3, k3)
    print(result3)  # Expected: 4

    items4: List[int] = [1, 1, 1, 1]
    k4: int = 2
    result4: int = solution.longest_pantry_window(items4, k4)
    print(result4)  # Expected: 2