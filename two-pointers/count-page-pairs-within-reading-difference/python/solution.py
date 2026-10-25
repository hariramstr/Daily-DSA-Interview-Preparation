"""
Title: Count Page Pairs Within Reading Difference

Problem Description:
You are given an array `pages` where `pages[i]` is the number of pages in the `i`th booklet.
You are also given an integer `k`. Two booklets form a compatible reading pair if the
absolute difference between their page counts is less than or equal to `k`.

Your task is to return the total number of index pairs `(i, j)` such that
`0 <= i < j < pages.length` and `|pages[i] - pages[j]| <= k`.

A straightforward solution checks every pair, but that is too slow for large inputs.
A more efficient approach uses sorting and two pointers.

Important note:
Because we only need the number of valid pairs, not the original pair positions,
we are free to sort the array first.

Constraints:
- 1 <= pages.length <= 100000
- 0 <= pages[i] <= 1000000000
- 0 <= k <= 1000000000
- The answer may be large, so a 64-bit integer type is needed in some languages
"""

from typing import List


class Solution:
    def count_compatible_pairs(self, pages: List[int], k: int) -> int:
        """
        Count how many index pairs have page-count difference at most k.

        The method sorts the page counts, then uses a sliding window / two-pointer
        technique to count, for each right endpoint, how many earlier values are
        close enough to form a valid pair.

        Args:
            pages: A list where pages[i] is the number of pages in the i-th booklet.
            k: The maximum allowed absolute difference between two page counts.

        Returns:
            The total number of valid pairs (i, j) with i < j and
            |pages[i] - pages[j]| <= k.

        Time complexity:
            O(n log n), due to sorting. The two-pointer scan itself is O(n).

        Space complexity:
            O(n) in Python because sorted(pages) creates a new list.
        """
        # Step 1: Sort the page counts.
        #
        # Why sort?
        # In an unsorted array, checking whether many pairs satisfy
        # |pages[i] - pages[j]| <= k is difficult to do efficiently.
        #
        # After sorting:
        # - The values are in non-decreasing order.
        # - For any pair with left < right, the absolute difference becomes:
        #       sorted_pages[right] - sorted_pages[left]
        #   because the right value is always >= the left value.
        #
        # This removes the need to use abs(...) and gives us a monotonic structure
        # that works perfectly with two pointers.
        sorted_pages: List[int] = sorted(pages)

        # This will store the final number of valid pairs.
        total_pairs: int = 0

        # `left` marks the smallest index currently allowed in the valid window.
        #
        # For each `right`, we want all indices from `left` to `right - 1`
        # to satisfy:
        #     sorted_pages[right] - sorted_pages[index] <= k
        #
        # If the difference becomes too large, we move `left` forward.
        left: int = 0

        # Step 2: Expand the window using `right`.
        #
        # We treat each position as the second element of a pair.
        # Then we count how many earlier elements can pair with it.
        for right in range(len(sorted_pages)):
            # Step 3: Shrink the window from the left while it is invalid.
            #
            # Since the array is sorted, if:
            #     sorted_pages[right] - sorted_pages[left] > k
            # then index `left` is too far away in value to pair with `right`.
            #
            # Also, because `left` is the smallest value in the current window,
            # it must be removed first. Once it is removed, we test again.
            #
            # This loop keeps moving `left` until the window becomes valid.
            while sorted_pages[right] - sorted_pages[left] > k:
                left += 1

            # Step 4: Count how many valid pairs end at `right`.
            #
            # At this moment, all indices in the range:
            #     left, left + 1, ..., right - 1
            # satisfy:
            #     sorted_pages[right] - sorted_pages[index] <= k
            #
            # Therefore, each of those indices forms one valid pair with `right`.
            #
            # Number of such indices:
            #     right - left
            #
            # Example:
            # If left = 2 and right = 5, then valid earlier indices are 2, 3, 4
            # which is exactly 5 - 2 = 3 indices.
            total_pairs += right - left

        # Step 5: Return the total number of valid pairs found.
        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    pages1: List[int] = [12, 15, 10, 18]
    k1: int = 3
    result1: int = solution.count_compatible_pairs(pages1, k1)
    print("Example 1:")
    print(f"pages = {pages1}, k = {k1}")
    print(f"Output: {result1}")
    print("Expected: 4")
    print()

    # Example 2
    pages2: List[int] = [5, 5, 5, 9]
    k2: int = 0
    result2: int = solution.count_compatible_pairs(pages2, k2)
    print("Example 2:")
    print(f"pages = {pages2}, k = {k2}")
    print(f"Output: {result2}")
    print("Expected: 3")
    print()

    # Additional beginner-friendly checks
    pages3: List[int] = [1]
    k3: int = 10
    result3: int = solution.count_compatible_pairs(pages3, k3)
    print("Additional Test 1:")
    print(f"pages = {pages3}, k = {k3}")
    print(f"Output: {result3}")
    print("Expected: 0")
    print()

    pages4: List[int] = [1, 2, 3, 4]
    k4: int = 1
    result4: int = solution.count_compatible_pairs(pages4, k4)
    print("Additional Test 2:")
    print(f"pages = {pages4}, k = {k4}")
    print(f"Output: {result4}")
    print("Expected: 3")
    print()

    pages5: List[int] = [7, 7, 7, 7]
    k5: int = 0
    result5: int = solution.count_compatible_pairs(pages5, k5)
    print("Additional Test 3:")
    print(f"pages = {pages5}, k = {k5}")
    print(f"Output: {result5}")
    print("Expected: 6")