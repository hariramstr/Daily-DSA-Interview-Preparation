"""
Title: Count Compatible Ski Lift Pairs

Problem Description:
A ski resort records the heights of guests waiting in a single line. For safety, the
resort wants to place some guests together on a two-person lift seat, but only if the
difference between their heights is at most `maxGap` centimeters.

You are given an integer array `heights` where `heights[i]` is the height of the `i`th
guest, and an integer `maxGap`.

Your task is to count how many distinct pairs of guests `(i, j)` with `i < j` can safely
share a lift seat, meaning `|heights[i] - heights[j]| <= maxGap`.

The array is not guaranteed to be sorted. An efficient solution is expected for large
inputs, so a quadratic scan over all pairs will be too slow. Sorting plus a two-pointers
strategy allows us to count valid pairs efficiently.

Return the total number of valid pairs.

Constraints:
- 1 <= heights.length <= 2 * 10^5
- 1 <= heights[i] <= 10^9
- 0 <= maxGap <= 10^9
- The answer may be large, so use a 64-bit integer type if needed.
"""

from typing import List


class Solution:
    def count_compatible_pairs(self, heights: List[int], maxGap: int) -> int:
        """
        Count the number of distinct guest pairs whose height difference is at most maxGap.

        The method sorts the heights first. After sorting, for every right endpoint of a
        window, we move the left endpoint forward until the window satisfies the allowed
        maximum difference. Then every index between left and right - 1 forms a valid pair
        with right, so we can count them in bulk.

        Args:
            heights: List of guest heights.
            maxGap: Maximum allowed absolute difference between two heights.

        Returns:
            Total number of valid pairs.

        Time complexity:
            O(n log n), due to sorting. The two-pointer scan itself is O(n).

        Space complexity:
            O(n) in Python because sorted() creates a new list.
        """
        # Step 1: Sort the heights.
        #
        # Why sort?
        # In the original unsorted order, checking whether two values differ by at most
        # maxGap does not give us any easy structure to exploit. After sorting:
        #
        #   sorted_heights[left] <= sorted_heights[left + 1] <= ... <= sorted_heights[right]
        #
        # This means the largest value in a window is always at the right end, and the
        # smallest value is always at the left end. Therefore, to know whether every pair
        # involving the right endpoint is valid, we only need to check:
        #
        #   sorted_heights[right] - sorted_heights[left] <= maxGap
        #
        # If that is true, then all elements between left and right are also within maxGap
        # of sorted_heights[right], because they lie between the two values.
        sorted_heights: List[int] = sorted(heights)

        # Step 2: Initialize the sliding window and the answer.
        #
        # left:
        #   The smallest index in the current valid window.
        #
        # total_pairs:
        #   Running count of all valid pairs found so far.
        #
        # We use int in Python, which automatically handles large values.
        left: int = 0
        total_pairs: int = 0

        # Step 3: Expand the window using 'right'.
        #
        # We treat each position 'right' as the second member of a pair. Then we find the
        # earliest position 'left' such that every element from left to right can pair with
        # sorted_heights[right] while respecting the maxGap rule.
        for right in range(len(sorted_heights)):
            # Step 3a: Shrink the window from the left until it becomes valid.
            #
            # Because the array is sorted, the maximum difference inside the current window
            # is exactly:
            #
            #   sorted_heights[right] - sorted_heights[left]
            #
            # If this difference is too large, then sorted_heights[left] cannot form a valid
            # pair with sorted_heights[right], and neither can any even smaller left index.
            # So we move left forward.
            while sorted_heights[right] - sorted_heights[left] > maxGap:
                left += 1

            # Step 3b: Count how many valid pairs end at 'right'.
            #
            # After the while-loop finishes, the window [left, right] is valid in the sense
            # that every element from left through right - 1 can pair with right.
            #
            # Number of such elements:
            #
            #   right - left
            #
            # These correspond to pairs:
            #   (left, right), (left + 1, right), ..., (right - 1, right)
            #
            # We add them all at once instead of checking each pair individually.
            total_pairs += right - left

        # Step 4: Return the final count.
        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    #
    # heights = [150, 160, 153, 168], maxGap = 5
    #
    # Let's verify carefully:
    # Pairs in original values:
    # - (150, 160) -> 10, invalid
    # - (150, 153) -> 3, valid
    # - (150, 168) -> 18, invalid
    # - (160, 153) -> 7, invalid
    # - (160, 168) -> 8, invalid
    # - (153, 168) -> 15, invalid
    #
    # So the correct answer is 1.
    heights1: List[int] = [150, 160, 153, 168]
    max_gap1: int = 5
    result1: int = solution.count_compatible_pairs(heights1, max_gap1)
    print(f"Example 1 result: {result1}")  # Expected: 1

    # Example 2 from the prompt:
    #
    # heights = [172, 175, 171, 169, 174], maxGap = 3
    #
    # Sorted: [169, 171, 172, 174, 175]
    #
    # Valid pairs:
    # - (169, 171) -> 2
    # - (169, 172) -> 3
    # - (169, 174) -> 5, invalid
    # - (169, 175) -> 6, invalid
    # - (171, 172) -> 1
    # - (171, 174) -> 3
    # - (171, 175) -> 4, invalid
    # - (172, 174) -> 2
    # - (172, 175) -> 3
    # - (174, 175) -> 1
    #
    # Total valid pairs = 7.
    #
    # Note:
    # The prompt states 8, but that list incorrectly includes (171, 175), whose gap is 4.
    # Therefore the mathematically correct answer is 7, and the algorithm below returns 7.
    heights2: List[int] = [172, 175, 171, 169, 174]
    max_gap2: int = 3
    result2: int = solution.count_compatible_pairs(heights2, max_gap2)
    print(f"Example 2 result: {result2}")  # Correct expected: 7

    # Additional quick sanity checks:
    heights3: List[int] = [100]
    max_gap3: int = 0
    result3: int = solution.count_compatible_pairs(heights3, max_gap3)
    print(f"Single guest result: {result3}")  # Expected: 0

    heights4: List[int] = [150, 150, 150]
    max_gap4: int = 0
    result4: int = solution.count_compatible_pairs(heights4, max_gap4)
    print(f"All equal heights result: {result4}")  # Expected: 3

    heights5: List[int] = [140, 145, 150, 155]
    max_gap5: int = 100
    result5: int = solution.count_compatible_pairs(heights5, max_gap5)
    print(f"All pairs valid result: {result5}")  # Expected: 6