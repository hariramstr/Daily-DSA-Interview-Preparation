"""
Title: Find the First Repeated Cart Item

Problem Description:
You are given an integer array items where each value represents the product ID of an
item scanned into an online shopping cart, in the exact order the scans happened.
Your task is to return the first product ID that appears more than once while scanning
from left to right.

In other words, as you read the array from the beginning, return the first item whose
current scan is a repeat of an item seen earlier. If no product ID is repeated, return -1.

This problem models a common event-processing task: detecting the earliest duplicate
action in a stream. The answer is not necessarily the smallest repeated value, and it is
not the value with the highest frequency. It is specifically the value whose second
appearance happens earliest in the array.

Constraints:
- 1 <= items.length <= 100000
- 1 <= items[i] <= 1000000000

Example 1:
Input: items = [42, 17, 9, 17, 42]
Output: 17
Explanation:
While scanning left to right, 42 appears first, then 17, then 9. The next value is 17,
which is the first repeated item encountered.

Example 2:
Input: items = [5, 8, 3, 1]
Output: -1
Explanation:
Every item appears exactly once, so there is no repeated cart item.
"""

from typing import List, Set


class Solution:
    def first_repeated_item(self, items: List[int]) -> int:
        """
        Return the first product ID whose second appearance is encountered earliest
        while scanning the list from left to right.

        Args:
            items: A list of integers representing scanned product IDs.

        Returns:
            The first repeated product ID encountered during a left-to-right scan,
            or -1 if no product ID repeats.

        Time Complexity:
            O(n), where n is the number of items in the list, because each item is
            processed once and set lookups/inserts are O(1) on average.

        Space Complexity:
            O(n) in the worst case, if all items are unique and must be stored in
            the set of seen product IDs.
        """
        # We use a set to store product IDs that have already been seen.
        #
        # Why a set?
        # - We need to quickly answer the question:
        #   "Have we seen this product ID before?"
        # - A set provides average O(1) lookup time.
        # - This is much faster than checking a list each time, which would take O(n)
        #   per lookup and lead to an O(n^2) solution in the worst case.
        seen: Set[int] = set()

        # We now scan the array from left to right exactly once.
        #
        # This order is extremely important because the problem does NOT ask for:
        # - the smallest repeated value
        # - the most frequent value
        # - the first value that appears twice if sorted
        #
        # Instead, it asks for the first item whose current appearance is a repeat
        # during the original scan order.
        for item in items:
            # For each scanned item, first check whether it is already in the set.
            #
            # If it is already present, that means:
            # - we saw this product ID earlier
            # - the current scan is the second (or later) occurrence
            # - because we are scanning left to right, this is the earliest repeated
            #   item encountered so far
            #
            # Therefore, we can immediately return it.
            if item in seen:
                return item

            # If the item was not seen before, we add it to the set so future scans
            # can detect it as a repeat.
            seen.add(item)

        # If we finish the entire loop without returning, then no item was repeated.
        # According to the problem statement, we must return -1 in that case.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    items1: List[int] = [42, 17, 9, 17, 42]
    result1: int = solution.first_repeated_item(items1)
    print(f"Input: {items1}")
    print(f"Output: {result1}")
    # Expected: 17
    #
    # Trace:
    # - seen = {}
    # - read 42 -> not seen, add it
    # - read 17 -> not seen, add it
    # - read 9  -> not seen, add it
    # - read 17 -> already seen, return 17

    print()

    # Example 2 from the problem statement
    items2: List[int] = [5, 8, 3, 1]
    result2: int = solution.first_repeated_item(items2)
    print(f"Input: {items2}")
    print(f"Output: {result2}")
    # Expected: -1
    #
    # Trace:
    # - seen = {}
    # - read 5 -> not seen, add it
    # - read 8 -> not seen, add it
    # - read 3 -> not seen, add it
    # - read 1 -> not seen, add it
    # - end of list reached, so return -1

    print()

    # Additional beginner-friendly test cases
    items3: List[int] = [7, 7, 2, 3]
    result3: int = solution.first_repeated_item(items3)
    print(f"Input: {items3}")
    print(f"Output: {result3}")
    # Expected: 7

    print()

    items4: List[int] = [1]
    result4: int = solution.first_repeated_item(items4)
    print(f"Input: {items4}")
    print(f"Output: {result4}")
    # Expected: -1