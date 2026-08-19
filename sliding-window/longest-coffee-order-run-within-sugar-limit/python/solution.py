"""
Title: Longest Coffee Order Run Within Sugar Limit

Problem Description:
A coffee shop records the sugar added to each drink in the order it was prepared.
You are given an integer array sugars where sugars[i] is the number of sugar packets
used in the i-th order, and an integer limit representing the maximum total sugar allowed.

Find the length of the longest contiguous sequence of orders whose total sugar does not
exceed limit.

In other words, you must choose a subarray sugars[left...right] such that the sum of
its values is less than or equal to limit, and the number of elements in that subarray
is as large as possible.

This problem models a common interview pattern: maintaining a valid moving window while
scanning the array from left to right. Since all sugar counts are non-negative, once a
window exceeds the limit, you can safely move the left pointer forward until the window
becomes valid again.

Return the maximum number of consecutive orders that can fit within the sugar limit.

Constraints:
- 1 <= sugars.length <= 100000
- 0 <= sugars[i] <= 10000
- 0 <= limit <= 1000000000

Example 1:
Input: sugars = [1, 2, 1, 1, 3], limit = 4
Output: 3
Explanation: The longest valid contiguous run is [1, 2, 1] or [2, 1, 1], both with
total sugar 4 and length 3.

Example 2:
Input: sugars = [4, 1, 1, 1, 2], limit = 3
Output: 3
Explanation: The first order alone exceeds the limit, so it cannot be part of any valid
window. The longest valid run is [1, 1, 1], which has total sugar 3.
"""

from typing import List


class Solution:
    def longest_coffee_order_run(self, sugars: List[int], limit: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most limit.

        Args:
            sugars: A list of non-negative integers where each value represents
                the sugar packets used in one coffee order.
            limit: The maximum allowed total sugar for a valid contiguous run.

        Returns:
            The length of the longest contiguous sequence of orders whose total
            sugar does not exceed limit.

        Time Complexity:
            O(n), where n is the length of sugars.
            Each element is added to the window once and removed from the window
            at most once.

        Space Complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The left pointer marks the beginning of our current sliding window.
        # We will expand the window to the right one element at a time.
        left: int = 0

        # current_sum stores the total sugar inside the current window
        # sugars[left:right+1].
        current_sum: int = 0

        # best_length keeps track of the longest valid window seen so far.
        best_length: int = 0

        # We move the right pointer from left to right across the entire array.
        # At each step, we include sugars[right] in the current window.
        for right in range(len(sugars)):
            # Add the new order's sugar amount into the running total because
            # the window is now being expanded to include this order.
            current_sum += sugars[right]

            # If the window becomes invalid (sum exceeds limit), we must shrink
            # it from the left until it becomes valid again.
            #
            # Why is this safe?
            # Because all values are non-negative. That means adding more elements
            # can only keep the sum the same or increase it, never decrease it.
            # So once the sum is too large, the only way to fix it is to move
            # the left boundary forward and remove elements.
            while current_sum > limit and left <= right:
                # Remove the sugar count at the left edge because that order is
                # no longer part of the window after we move left forward.
                current_sum -= sugars[left]

                # Advance the left pointer to shrink the window.
                left += 1

            # At this point, the window sugars[left:right+1] is guaranteed to be valid:
            # its sum is <= limit.
            #
            # We now compute its length. Since both ends are inclusive, the length is:
            # right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any valid
            # window we have seen before.
            if current_length > best_length:
                best_length = current_length

        # After scanning the full array, best_length contains the maximum valid
        # contiguous run length.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    sugars1: List[int] = [1, 2, 1, 1, 3]
    limit1: int = 4
    result1: int = solution.longest_coffee_order_run(sugars1, limit1)
    print("Example 1:")
    print(f"sugars = {sugars1}, limit = {limit1}")
    print(f"Output: {result1}")
    print("Expected: 3")
    print()

    # Example 2
    sugars2: List[int] = [4, 1, 1, 1, 2]
    limit2: int = 3
    result2: int = solution.longest_coffee_order_run(sugars2, limit2)
    print("Example 2:")
    print(f"sugars = {sugars2}, limit = {limit2}")
    print(f"Output: {result2}")
    print("Expected: 3")
    print()

    # Additional beginner-friendly test cases
    sugars3: List[int] = [0, 0, 0, 0]
    limit3: int = 0
    result3: int = solution.longest_coffee_order_run(sugars3, limit3)
    print("Additional Test 1:")
    print(f"sugars = {sugars3}, limit = {limit3}")
    print(f"Output: {result3}")
    print("Expected: 4")
    print()

    sugars4: List[int] = [5]
    limit4: int = 4
    result4: int = solution.longest_coffee_order_run(sugars4, limit4)
    print("Additional Test 2:")
    print(f"sugars = {sugars4}, limit = {limit4}")
    print(f"Output: {result4}")
    print("Expected: 0")
    print()

    sugars5: List[int] = [2, 2, 2]
    limit5: int = 6
    result5: int = solution.longest_coffee_order_run(sugars5, limit5)
    print("Additional Test 3:")
    print(f"sugars = {sugars5}, limit = {limit5}")
    print(f"Output: {result5}")
    print("Expected: 3")