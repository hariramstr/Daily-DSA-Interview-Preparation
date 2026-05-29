"""
First Spoiled Item on Sorted Shelf
===================================
Difficulty: Easy
Topic: Binary Search

Problem Description:
A grocery store arranges items on a shelf sorted by their expiration dates in ascending order
(oldest expiration date first). Each item has an integer expiration date represented as the
number of days from today. An item is considered 'spoiled' if its expiration date is less than
or equal to 0 (meaning it has already expired or expires today).

Given a sorted array `expirations` of integers representing the expiration dates of items on
the shelf, return the index of the **first non-spoiled item** (i.e., the first item with an
expiration date strictly greater than 0). If all items are spoiled, return -1.
If no items are spoiled, return 0.

You must solve this in O(log n) time complexity.

Constraints:
- 1 <= expirations.length <= 10^5
- -10^4 <= expirations[i] <= 10^4
- The array is sorted in non-decreasing order.

Examples:
- Input: [-5, -3, -1, 0, 2, 4, 7] -> Output: 4
- Input: [1, 3, 5, 8]             -> Output: 0
- Input: [-4, -2, 0]              -> Output: -1
"""

from typing import List


class Solution:
    def firstNonSpoiled(self, expirations: List[int]) -> int:
        """
        Find the index of the first non-spoiled item using binary search.

        An item is 'spoiled' if its expiration date is <= 0.
        We want the first index where expirations[index] > 0.

        Args:
            expirations (List[int]): A sorted (non-decreasing) list of expiration dates.

        Returns:
            int: The index of the first non-spoiled item, or -1 if all items are spoiled.

        Time Complexity:  O(log n) — Binary search halves the search space each iteration.
        Space Complexity: O(1)    — Only a constant number of variables are used.
        """

        # -----------------------------------------------------------------------
        # EDGE CASE CHECK #1: If the very first element is already > 0,
        # then NO items are spoiled at all. Return 0 immediately.
        # Since the array is sorted in ascending order, if the smallest value
        # (index 0) is already > 0, every element must also be > 0.
        # -----------------------------------------------------------------------
        if expirations[0] > 0:
            return 0

        # -----------------------------------------------------------------------
        # EDGE CASE CHECK #2: If the very last element is <= 0,
        # then ALL items are spoiled. Return -1 immediately.
        # Since the array is sorted, if the largest value (last index) is <= 0,
        # every element must also be <= 0.
        # -----------------------------------------------------------------------
        if expirations[-1] <= 0:
            return -1

        # -----------------------------------------------------------------------
        # BINARY SEARCH SETUP
        # At this point we know:
        #   - expirations[0] <= 0  (at least the first item is spoiled)
        #   - expirations[-1] > 0  (at least the last item is NOT spoiled)
        # So the answer definitely exists somewhere in the middle.
        #
        # We use two pointers:
        #   left  — starts at 0 (known spoiled region)
        #   right — starts at len(expirations) - 1 (known non-spoiled region)
        #
        # Goal: narrow down to the FIRST index where the value is > 0.
        # -----------------------------------------------------------------------
        left: int = 0
        right: int = len(expirations) - 1

        # -----------------------------------------------------------------------
        # BINARY SEARCH LOOP
        # We continue as long as there is more than one candidate index.
        # The loop invariant is:
        #   - expirations[left]  <= 0  (left is always in the spoiled zone)
        #   - expirations[right] > 0   (right is always in the non-spoiled zone)
        # When left + 1 == right, 'right' is our answer (the first non-spoiled index).
        # -----------------------------------------------------------------------
        while left + 1 < right:
            # Calculate the midpoint to avoid integer overflow
            # (Python handles big integers natively, but this is good practice)
            mid: int = left + (right - left) // 2

            # ------------------------------------------------------------------
            # CHECK THE MIDPOINT:
            # If expirations[mid] <= 0, the midpoint is still in the spoiled zone.
            # We can safely move 'left' up to 'mid' because everything up to
            # and including 'mid' is spoiled.
            # ------------------------------------------------------------------
            if expirations[mid] <= 0:
                left = mid  # mid is spoiled, so the answer is to the right of mid

            # ------------------------------------------------------------------
            # Otherwise, expirations[mid] > 0, meaning 'mid' is non-spoiled.
            # The first non-spoiled item could be 'mid' itself or something
            # to its LEFT. Move 'right' down to 'mid' to keep searching left.
            # ------------------------------------------------------------------
            else:
                right = mid  # mid is non-spoiled, but there might be an earlier one

        # -----------------------------------------------------------------------
        # RESULT
        # When the loop ends, left + 1 == right.
        # By our invariant:
        #   - expirations[left]  <= 0  → still spoiled
        #   - expirations[right] > 0   → first non-spoiled
        # Therefore, 'right' is the index of the first non-spoiled item.
        # -----------------------------------------------------------------------
        return right


# ---------------------------------------------------------------------------
# MAIN BLOCK — Demonstrates the solution with all provided examples
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solver = Solution()

    # -----------------------------------------------------------------------
    # Example 1:
    # expirations = [-5, -3, -1, 0, 2, 4, 7]
    # Indices:         0   1   2  3  4  5  6
    # Items at indices 0-3 have expiration <= 0 (spoiled).
    # First non-spoiled item is at index 4 (expiration = 2).
    # Expected Output: 4
    # -----------------------------------------------------------------------
    expirations1 = [-5, -3, -1, 0, 2, 4, 7]
    result1 = solver.firstNonSpoiled(expirations1)
    print(f"Example 1: expirations = {expirations1}")
    print(f"  Output: {result1}  (Expected: 4)")
    print()

    # -----------------------------------------------------------------------
    # Example 2:
    # expirations = [1, 3, 5, 8]
    # All items have expiration > 0, so the first non-spoiled item is at index 0.
    # Expected Output: 0
    # -----------------------------------------------------------------------
    expirations2 = [1, 3, 5, 8]
    result2 = solver.firstNonSpoiled(expirations2)
    print(f"Example 2: expirations = {expirations2}")
    print(f"  Output: {result2}  (Expected: 0)")
    print()

    # -----------------------------------------------------------------------
    # Example 3:
    # expirations = [-4, -2, 0]
    # All items have expiration <= 0 (all spoiled), so return -1.
    # Expected Output: -1
    # -----------------------------------------------------------------------
    expirations3 = [-4, -2, 0]
    result3 = solver.firstNonSpoiled(expirations3)
    print(f"Example 3: expirations = {expirations3}")
    print(f"  Output: {result3}  (Expected: -1)")
    print()

    # -----------------------------------------------------------------------
    # Additional Edge Case: Single element that is spoiled
    # Expected Output: -1
    # -----------------------------------------------------------------------
    expirations4 = [0]
    result4 = solver.firstNonSpoiled(expirations4)
    print(f"Edge Case 1 (single spoiled): expirations = {expirations4}")
    print(f"  Output: {result4}  (Expected: -1)")
    print()

    # -----------------------------------------------------------------------
    # Additional Edge Case: Single element that is NOT spoiled
    # Expected Output: 0
    # -----------------------------------------------------------------------
    expirations5 = [5]
    result5 = solver.firstNonSpoiled(expirations5)
    print(f"Edge Case 2 (single fresh): expirations = {expirations5}")
    print(f"  Output: {result5}  (Expected: 0)")
    print()

    # -----------------------------------------------------------------------
    # Additional Edge Case: Boundary — last item is the only non-spoiled one
    # expirations = [-3, -2, -1, 0, 1]
    # Expected Output: 4
    # -----------------------------------------------------------------------
    expirations6 = [-3, -2, -1, 0, 1]
    result6 = solver.firstNonSpoiled(expirations6)
    print(f"Edge Case 3 (only last is fresh): expirations = {expirations6}")
    print(f"  Output: {result6}  (Expected: 4)")