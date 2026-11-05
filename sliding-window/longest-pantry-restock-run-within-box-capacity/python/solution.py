"""
Title: Longest Pantry Restock Run Within Box Capacity

Problem Description:
A grocery warehouse packs a sequence of pantry items into a single moving box belt.
The items must stay in their original order, and the packer wants to choose one
contiguous run of items to place into a box. Each item has a size value in sizes[i],
and the total size of all chosen items must be less than or equal to capacity.

Your task is to return the length of the longest contiguous subarray whose sum is
at most capacity.

If multiple valid runs have the same maximum length, you only need to return the
length, not the subarray itself. If no item can fit in the box, return 0.

This problem is designed to be solved efficiently using a sliding window because
all item sizes are positive. As you expand the right end of the window, the total
size increases. If the total becomes too large, move the left end forward until
the window becomes valid again.

Constraints:
- 1 <= sizes.length <= 100000
- 1 <= sizes[i] <= 10000
- 1 <= capacity <= 1000000000
- All values in sizes are positive integers.

Example 1:
Input: sizes = [2, 1, 3, 2, 1], capacity = 6
Output: 3
Explanation: The longest valid contiguous run is [1, 3, 2] or [3, 2, 1], both with total size 6.

Example 2:
Input: sizes = [5, 4, 2, 1], capacity = 3
Output: 2
Explanation: Item 5 and item 4 are too large by themselves. The longest valid run is [2, 1], which has length 2.
"""

from typing import List


class Solution:
    def longest_pantry_restock_run(self, sizes: List[int], capacity: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is at most capacity.

        This uses the sliding window technique, which works efficiently here because
        all values in sizes are positive. That positivity is important: when we move
        the right pointer forward, the sum can only stay the same or increase, and
        when we move the left pointer forward, the sum can only stay the same or decrease.
        That predictable behavior allows us to maintain a valid window in linear time.

        Args:
            sizes: A list of positive integers representing item sizes.
            capacity: The maximum allowed sum for a contiguous run.

        Returns:
            The maximum length of a contiguous subarray with sum <= capacity.

        Time complexity:
            O(n), where n is the length of sizes.
            Each element is added to the window once and removed from the window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # This pointer marks the beginning of the current sliding window.
        # The window will always represent a contiguous subarray sizes[left:right+1].
        left: int = 0

        # This variable stores the sum of the current window.
        # We update it incrementally instead of recomputing sums repeatedly,
        # which keeps the algorithm efficient.
        current_sum: int = 0

        # This will store the best (maximum) valid window length found so far.
        max_length: int = 0

        # We expand the window one element at a time by moving the right pointer
        # from the start of the array to the end.
        for right in range(len(sizes)):
            # Add the new rightmost element into the current window sum.
            current_sum += sizes[right]

            # If the window sum is now too large, we must shrink the window
            # from the left until the sum becomes valid again.
            #
            # We use a while loop, not an if statement, because adding one large
            # element may require removing multiple elements from the left before
            # the total is <= capacity again.
            while current_sum > capacity and left <= right:
                # Remove the element at the left edge from the running sum,
                # because that element is no longer part of the window after
                # we move the left pointer forward.
                current_sum -= sizes[left]

                # Move the left edge one step to the right to shrink the window.
                left += 1

            # At this point, the window is guaranteed to be valid:
            # current_sum <= capacity
            #
            # So we can safely compute its length and compare it with the best
            # answer seen so far.
            window_length: int = right - left + 1

            # Update the maximum length if the current valid window is longer.
            if window_length > max_length:
                max_length = window_length

        # If no single item could fit, max_length will remain 0.
        # Otherwise, it will contain the length of the longest valid run.
        return max_length

    def longestSubarrayWithinCapacity(self, sizes: List[int], capacity: int) -> int:
        """
        Wrapper method using a more generic interview-style name.

        Args:
            sizes: A list of positive integers representing item sizes.
            capacity: The maximum allowed sum for a contiguous run.

        Returns:
            The maximum length of a contiguous subarray with sum <= capacity.

        Time complexity:
            O(n), where n is the number of items.

        Space complexity:
            O(1).
        """
        return self.longest_pantry_restock_run(sizes, capacity)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # sizes = [2, 1, 3, 2, 1], capacity = 6
    # Valid longest runs include [1, 3, 2] and [3, 2, 1], both length 3.
    sizes1: List[int] = [2, 1, 3, 2, 1]
    capacity1: int = 6
    result1: int = solution.longest_pantry_restock_run(sizes1, capacity1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2:
    # sizes = [5, 4, 2, 1], capacity = 3
    # Items 5 and 4 are too large alone, but [2, 1] fits with length 2.
    sizes2: List[int] = [5, 4, 2, 1]
    capacity2: int = 3
    result2: int = solution.longest_pantry_restock_run(sizes2, capacity2)
    print("Example 2 Result:", result2)  # Expected: 2

    # Additional quick checks for beginner-friendly demonstration.
    sizes3: List[int] = [1, 1, 1, 1]
    capacity3: int = 2
    result3: int = solution.longest_pantry_restock_run(sizes3, capacity3)
    print("Additional Example 3 Result:", result3)  # Expected: 2

    sizes4: List[int] = [10, 20, 30]
    capacity4: int = 5
    result4: int = solution.longest_pantry_restock_run(sizes4, capacity4)
    print("Additional Example 4 Result:", result4)  # Expected: 0