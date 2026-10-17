"""
Title: Count Camera Triples Within Zoom Difference

Problem Description:
You are given an integer array zoomLevels representing the zoom capability of different
cameras in a warehouse. You want to form inspection bundles of exactly three distinct
cameras. A bundle is considered compatible if the difference between the highest zoom
level and the lowest zoom level in that bundle is less than or equal to a given integer
maxGap.

Return the number of compatible bundles.

Two bundles are different if they use a different set of indices, even if the zoom values
are the same. The input array is not guaranteed to be sorted.

Your task is to design an efficient solution. A brute-force O(n^3) approach will be too
slow for the largest inputs. The intended solution uses sorting together with a two-pointer
or sliding-window idea to count, for each right endpoint, how many earlier cameras can join
it while keeping the zoom range within maxGap.

Constraints:
- 3 <= zoomLevels.length <= 100000
- 0 <= zoomLevels[i] <= 1000000000
- 0 <= maxGap <= 1000000000
- The answer can be large, so return it as a 64-bit integer value

Example 1:
Input: zoomLevels = [4, 1, 3, 6, 5], maxGap = 2
Output: 3

Example 2:
Input: zoomLevels = [2, 2, 2, 2], maxGap = 0
Output: 4

Note:
You only need to return the count, not the actual triples.
"""

from typing import List


class Solution:
    def _choose_two(self, count: int) -> int:
        """
        Compute the number of ways to choose exactly 2 items from `count` items.

        This helper is used because, after sorting and maintaining a valid window,
        if we decide that the current element is the largest element in the triple,
        then we only need to choose any 2 earlier elements from the valid window.

        Args:
            count: Number of available items.

        Returns:
            The value of C(count, 2), which equals count * (count - 1) // 2.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        if count < 2:
            return 0
        return count * (count - 1) // 2

    def count_compatible_bundles(self, zoomLevels: List[int], maxGap: int) -> int:
        """
        Count the number of triples of distinct indices such that the difference
        between the maximum and minimum zoom level in the triple is at most maxGap.

        Core idea:
        1. Sort the zoom levels.
        2. Use a sliding window with two pointers.
        3. For each right endpoint `r`, move the left endpoint `l` forward until
           the window [l, r] satisfies:
               zoomLevels[r] - zoomLevels[l] <= maxGap
        4. Now every element from l to r can coexist with zoomLevels[r] in a valid
           range. If we force zoomLevels[r] to be the largest element in the triple,
           then we only need to choose any 2 elements from the previous elements
           inside the window, which is:
               C(r - l, 2)

        Why this works:
        - After sorting, the largest value in a triple is at the rightmost chosen index,
          and the smallest value is at the leftmost chosen index.
        - If the full window [l, r] satisfies the maxGap condition, then any subset
          of that window also satisfies it, because its min/max range can only be
          smaller or equal.
        - Each valid triple is counted exactly once: when its largest index is used
          as `r`.

        Args:
            zoomLevels: List of camera zoom levels.
            maxGap: Maximum allowed difference between highest and lowest zoom levels
                in a compatible triple.

        Returns:
            The total number of compatible triples as an integer.

        Time complexity:
            O(n log n) due to sorting, plus O(n) for the sliding window scan.

        Space complexity:
            O(n) in Python because `sorted(...)` creates a new list.
        """
        # Step 1:
        # Sort the array so that values are in non-decreasing order.
        #
        # This is the key transformation that makes the problem manageable.
        # Once sorted:
        # - The minimum value in any chosen group is the leftmost chosen element.
        # - The maximum value in any chosen group is the rightmost chosen element.
        #
        # That means we can reason about the range of a triple using only endpoints.
        sorted_levels: List[int] = sorted(zoomLevels)

        # This will store the final number of valid triples.
        total_triplets: int = 0

        # `left` is the start of our sliding window.
        # For each `right`, we will move `left` only forward, never backward.
        # This gives us linear-time scanning after sorting.
        left: int = 0

        # Step 2:
        # Iterate through each position as the potential rightmost element
        # (equivalently, the maximum zoom level) of a valid triple.
        for right in range(len(sorted_levels)):
            # Step 3:
            # Shrink the window from the left until the current window [left, right]
            # satisfies the required condition:
            #     sorted_levels[right] - sorted_levels[left] <= maxGap
            #
            # Why do we do this?
            # Because if the difference is too large, then any triple using both
            # endpoints would be invalid. We need the smallest possible `left`
            # that still keeps the window valid.
            while sorted_levels[right] - sorted_levels[left] > maxGap:
                left += 1

            # Step 4:
            # Now the window [left, right] is valid.
            #
            # We want to count triples where:
            # - `right` is definitely included
            # - the other two elements are chosen from indices [left, right - 1]
            #
            # How many earlier elements are available?
            #     available = right - left
            #
            # From those available elements, choose any 2:
            #     C(available, 2)
            #
            # This counts exactly the triples whose maximum element is at index `right`.
            available_before_right: int = right - left
            total_triplets += self._choose_two(available_before_right)

        return total_triplets


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Input: [4, 1, 3, 6, 5], maxGap = 2
    # Sorted: [1, 3, 4, 5, 6]
    # Valid triples by values:
    # - (3, 4, 5)
    # - (3, 4, 6)
    # - (4, 5, 6)
    # Expected output: 3
    zoom_levels_1: List[int] = [4, 1, 3, 6, 5]
    max_gap_1: int = 2
    result_1: int = solution.count_compatible_bundles(zoom_levels_1, max_gap_1)
    print("Example 1 Result:", result_1)

    # Example 2:
    # Input: [2, 2, 2, 2], maxGap = 0
    # Every triple is valid because all values are equal.
    # Number of ways to choose 3 out of 4 = 4
    # Expected output: 4
    zoom_levels_2: List[int] = [2, 2, 2, 2]
    max_gap_2: int = 0
    result_2: int = solution.count_compatible_bundles(zoom_levels_2, max_gap_2)
    print("Example 2 Result:", result_2)

    # Additional quick sanity check:
    # Input: [1, 10, 20], maxGap = 5
    # Only one possible triple exists, and its range is 19, so it is invalid.
    # Expected output: 0
    zoom_levels_3: List[int] = [1, 10, 20]
    max_gap_3: int = 5
    result_3: int = solution.count_compatible_bundles(zoom_levels_3, max_gap_3)
    print("Additional Check Result:", result_3)