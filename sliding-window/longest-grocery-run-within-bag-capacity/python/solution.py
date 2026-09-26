"""
Title: Longest Grocery Run Within Bag Capacity

Problem Description:
You are given an array weights where weights[i] represents the weight of the i-th
grocery item picked up in order while walking through a store. You also have an
integer capacity representing the maximum total weight that can fit in your shopping
bag at one time.

Your task is to find the length of the longest contiguous sequence of items you can
pick such that the sum of their weights is less than or equal to capacity. In other
words, choose a subarray with the largest possible length whose total weight does
not exceed the bag limit.

This is a realistic sliding window problem because all item weights are non-negative,
so if a window becomes too heavy, you can move its left boundary forward until it
becomes valid again.

Return the maximum number of consecutive items that can fit in the bag.

Constraints:
- 1 <= weights.length <= 100000
- 0 <= weights[i] <= 10000
- 0 <= capacity <= 1000000000

Example 1:
Input: weights = [2, 1, 3, 2, 1], capacity = 5
Output: 2
Explanation: Valid contiguous runs include [2,1], [3,2], and [2,1]. Each has total
weight 5 or less and length 2. No length-3 subarray fits within the capacity.

Example 2:
Input: weights = [1, 1, 1, 1, 2], capacity = 4
Output: 4
Explanation: The subarray [1,1,1,1] has total weight 4, so the answer is 4. The full
array has total weight 6, which exceeds the capacity.

Efficient Approach:
Use a sliding window with two pointers:
- Expand the right pointer to include more items.
- If the total weight exceeds capacity, move the left pointer forward until the
  window becomes valid again.
- Track the maximum valid window length seen so far.
"""

from typing import List


class Solution:
    def longest_grocery_run(self, weights: List[int], capacity: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is <= capacity.

        Args:
            weights: A list of non-negative integers representing item weights.
            capacity: The maximum allowed sum for any chosen contiguous run.

        Returns:
            The length of the longest contiguous subarray with sum <= capacity.

        Time Complexity:
            O(n), where n is the number of items in weights.
            Each element is added to the window once and removed at most once.

        Space Complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The left pointer marks the beginning of the current sliding window.
        # We will move it to the right whenever the current window becomes too heavy.
        left: int = 0

        # This variable stores the sum of the weights currently inside the window
        # from index left to index right, inclusive.
        current_sum: int = 0

        # This keeps track of the best (largest) valid window length found so far.
        max_length: int = 0

        # We iterate with the right pointer from left to right across the array.
        # At each step, we try to include weights[right] in the current window.
        for right in range(len(weights)):
            # Add the new item at position right into the running window sum.
            current_sum += weights[right]

            # If adding this item makes the total exceed capacity, the window is invalid.
            # Because all weights are non-negative, the only way to reduce the sum is
            # to move the left boundary forward and remove items from the left side.
            #
            # We keep shrinking until the window sum is valid again.
            while current_sum > capacity and left <= right:
                # Remove the item currently at the left edge from the window sum.
                current_sum -= weights[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window [left, right] is guaranteed to have
            # total weight <= capacity.
            #
            # So we can safely compute its length and compare it with the best answer.
            current_length: int = right - left + 1

            # Update the maximum length if this valid window is longer.
            if current_length > max_length:
                max_length = current_length

        # After scanning the full array, max_length holds the answer.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    weights1: List[int] = [2, 1, 3, 2, 1]
    capacity1: int = 5
    result1: int = solution.longest_grocery_run(weights1, capacity1)
    print("Example 1:")
    print(f"weights = {weights1}")
    print(f"capacity = {capacity1}")
    print(f"Output = {result1}")
    print("Expected = 2")
    print()

    # Example 2
    weights2: List[int] = [1, 1, 1, 1, 2]
    capacity2: int = 4
    result2: int = solution.longest_grocery_run(weights2, capacity2)
    print("Example 2:")
    print(f"weights = {weights2}")
    print(f"capacity = {capacity2}")
    print(f"Output = {result2}")
    print("Expected = 4")
    print()

    # Additional beginner-friendly test cases

    # Single item that fits
    weights3: List[int] = [4]
    capacity3: int = 4
    result3: int = solution.longest_grocery_run(weights3, capacity3)
    print("Additional Test 1:")
    print(f"weights = {weights3}")
    print(f"capacity = {capacity3}")
    print(f"Output = {result3}")
    print("Expected = 1")
    print()

    # Single item that does not fit
    weights4: List[int] = [5]
    capacity4: int = 4
    result4: int = solution.longest_grocery_run(weights4, capacity4)
    print("Additional Test 2:")
    print(f"weights = {weights4}")
    print(f"capacity = {capacity4}")
    print(f"Output = {result4}")
    print("Expected = 0")
    print()

    # All zeros should always fit
    weights5: List[int] = [0, 0, 0, 0]
    capacity5: int = 0
    result5: int = solution.longest_grocery_run(weights5, capacity5)
    print("Additional Test 3:")
    print(f"weights = {weights5}")
    print(f"capacity = {capacity5}")
    print(f"Output = {result5}")
    print("Expected = 4")