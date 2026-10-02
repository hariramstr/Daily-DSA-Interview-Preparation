"""
Title: Count Bookend Pairs Under Shelf Length

Problem Description:
A library is arranging decorative bookends in a display. You are given an integer
array lengths where lengths[i] is the length of the i-th bookend, and an integer
shelfLimit representing the maximum total length that can fit comfortably on one
shelf section. Two different bookends can be placed together if their combined
length is less than or equal to shelfLimit.

Return the number of distinct pairs of bookends (i, j) such that
0 <= i < j < lengths.length and lengths[i] + lengths[j] <= shelfLimit.

The expected efficient approach is:
1. Sort the array.
2. Use two pointers to count all valid pairs in O(n log n) time overall.

Constraints:
- 1 <= lengths.length <= 100000
- 1 <= lengths[i] <= 1000000000
- 1 <= shelfLimit <= 2000000000
- The answer can be large, so use a 64-bit integer type if needed.

Example 1:
Input: lengths = [1, 3, 2, 2], shelfLimit = 4
Output: 4

Example 2:
Input: lengths = [5, 1, 4, 2], shelfLimit = 5
Output: 2
"""

from typing import List


class Solution:
    def count_bookend_pairs(self, lengths: List[int], shelfLimit: int) -> int:
        """
        Count the number of distinct index pairs whose values sum to at most shelfLimit.

        Args:
            lengths: A list of positive integers representing bookend lengths.
            shelfLimit: The maximum allowed combined length for a valid pair.

        Returns:
            The total number of distinct pairs (i, j) with i < j such that
            lengths[i] + lengths[j] <= shelfLimit.

        Time complexity:
            O(n log n), because we sort the array first, then scan it once with two pointers.

        Space complexity:
            O(n) in Python because sorted(lengths) creates a new list.
            The two-pointer scan itself uses O(1) extra space.
        """
        # We sort the values so that we can reason about pair sums efficiently.
        #
        # Why sorting helps:
        # - If the array is sorted and we know that sorted_lengths[left] + sorted_lengths[right]
        #   is valid (<= shelfLimit), then every element between left and right-1 paired with
        #   sorted_lengths[left] will also be valid, because those elements are <= sorted_lengths[right].
        # - This allows us to count many pairs at once instead of checking each pair individually.
        #
        # This is the key idea that improves the brute-force O(n^2) approach.
        sorted_lengths: List[int] = sorted(lengths)

        # Initialize two pointers:
        # - left starts at the smallest value
        # - right starts at the largest value
        #
        # We will move these pointers inward based on whether the current pair is valid.
        left: int = 0
        right: int = len(sorted_lengths) - 1

        # This variable stores the total number of valid pairs found.
        # In Python, int automatically handles large values, so it is safe for big answers.
        pair_count: int = 0

        # Continue until the two pointers cross.
        # We require left < right because a pair must use two different indices.
        while left < right:
            # Compute the sum of the current smallest remaining value
            # and the current largest remaining value.
            current_sum: int = sorted_lengths[left] + sorted_lengths[right]

            # Case 1: The pair is valid.
            if current_sum <= shelfLimit:
                # Because the array is sorted:
                # sorted_lengths[left] + sorted_lengths[right] <= shelfLimit
                #
                # Then for every index k where left < k <= right,
                # we also have:
                # sorted_lengths[left] + sorted_lengths[k] <= shelfLimit
                #
                # Why?
                # Because sorted_lengths[k] <= sorted_lengths[right].
                #
                # So the element at 'left' can form a valid pair with every element
                # from left+1 through right.
                #
                # Number of such pairs:
                # right - left
                pair_count += right - left

                # After counting all pairs that use sorted_lengths[left],
                # we move left forward to consider the next smallest element.
                #
                # We do NOT need to test those counted pairs individually,
                # because sorting guarantees they are all valid.
                left += 1
            else:
                # Case 2: The pair is too large.
                #
                # sorted_lengths[left] + sorted_lengths[right] > shelfLimit
                #
                # Since sorted_lengths[right] is the largest remaining value,
                # pairing it with any element from left to right-1 that is >= sorted_lengths[left]
                # will not make the sum smaller enough unless we reduce the right side.
                #
                # Therefore, we move 'right' leftward to try a smaller value.
                right -= 1

        # When the loop ends, all valid pairs have been counted.
        return pair_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    lengths1: List[int] = [1, 3, 2, 2]
    shelf_limit1: int = 4
    result1: int = solution.count_bookend_pairs(lengths1, shelf_limit1)
    print("Example 1:")
    print(f"lengths = {lengths1}, shelfLimit = {shelf_limit1}")
    print(f"Output: {result1}")
    print("Expected: 4")
    print()

    # Example 2
    lengths2: List[int] = [5, 1, 4, 2]
    shelf_limit2: int = 5
    result2: int = solution.count_bookend_pairs(lengths2, shelf_limit2)
    print("Example 2:")
    print(f"lengths = {lengths2}, shelfLimit = {shelf_limit2}")
    print(f"Output: {result2}")
    print("Expected: 2")
    print()

    # Additional quick sanity checks
    lengths3: List[int] = [2, 2, 2]
    shelf_limit3: int = 4
    result3: int = solution.count_bookend_pairs(lengths3, shelf_limit3)
    print("Additional Test 1:")
    print(f"lengths = {lengths3}, shelfLimit = {shelf_limit3}")
    print(f"Output: {result3}")
    print("Expected: 3")
    print()

    lengths4: List[int] = [10]
    shelf_limit4: int = 10
    result4: int = solution.count_bookend_pairs(lengths4, shelf_limit4)
    print("Additional Test 2:")
    print(f"lengths = {lengths4}, shelfLimit = {shelf_limit4}")
    print(f"Output: {result4}")
    print("Expected: 0")