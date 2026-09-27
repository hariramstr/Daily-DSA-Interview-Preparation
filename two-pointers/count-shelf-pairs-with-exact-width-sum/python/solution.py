"""
Title: Count Shelf Pairs With Exact Width Sum

Problem Description:
You are given an integer array widths representing the widths of wooden shelves
currently stored in a warehouse. The array is not guaranteed to be sorted.
You are also given an integer targetWidth.

A pair of shelves (i, j) is considered valid if:
- i < j
- widths[i] + widths[j] == targetWidth

Your task is to return the total number of valid index pairs.

Because the warehouse may contain many shelves with the same width, duplicate
values must be handled correctly. For example, if four shelves have width 2
and targetWidth is 4, then they form 6 distinct pairs because every choice of
two different indices counts.

Design an efficient solution using sorting and the two-pointer technique.
A brute-force O(n^2) solution will be too slow for the largest inputs.

Constraints:
- 1 <= widths.length <= 2 * 10^5
- -10^9 <= widths[i] <= 10^9
- -2 * 10^9 <= targetWidth <= 2 * 10^9
- The answer fits in a 64-bit signed integer.
"""

from typing import List


class Solution:
    def count_shelf_pairs(self, widths: List[int], target_width: int) -> int:
        """
        Count the number of index pairs whose values sum to target_width.

        This method sorts the array and then uses the two-pointer technique
        to count all valid pairs efficiently, including duplicates.

        Args:
            widths: A list of shelf widths.
            target_width: The required sum for a valid pair.

        Returns:
            The total number of valid index pairs.

        Time complexity:
            O(n log n), due to sorting. The two-pointer scan is O(n).

        Space complexity:
            O(n) in Python in the practical sense because sorted(widths)
            creates a new list. The pointer scan itself uses O(1) extra space.
        """
        # We sort the input so that we can use the two-pointer strategy.
        # Why sorting helps:
        # - If the current sum is too small, we know we must move the left pointer right
        #   to increase the sum.
        # - If the current sum is too large, we know we must move the right pointer left
        #   to decrease the sum.
        # This ordered structure is exactly what makes the two-pointer approach efficient.
        sorted_widths: List[int] = sorted(widths)

        # Initialize two pointers:
        # - left starts at the beginning of the sorted list
        # - right starts at the end of the sorted list
        left: int = 0
        right: int = len(sorted_widths) - 1

        # This will store the total number of valid index pairs.
        total_pairs: int = 0

        # Continue while the two pointers refer to different positions.
        # We need left < right because a pair must use two different indices.
        while left < right:
            current_sum: int = sorted_widths[left] + sorted_widths[right]

            # Case 1:
            # The sum is too small.
            # Since the array is sorted, increasing the left pointer is the only move
            # that can potentially increase the sum enough to reach target_width.
            if current_sum < target_width:
                left += 1

            # Case 2:
            # The sum is too large.
            # Since the array is sorted, decreasing the right pointer is the only move
            # that can potentially reduce the sum enough to reach target_width.
            elif current_sum > target_width:
                right -= 1

            # Case 3:
            # We found values at left and right whose sum equals target_width.
            else:
                # There are two important duplicate-handling situations:
                #
                # Situation A:
                # sorted_widths[left] == sorted_widths[right]
                # Example: [2, 2, 2, 2] and target = 4
                #
                # In this case, every value between left and right is the same.
                # If there are k such elements, then the number of ways to choose
                # 2 different indices is:
                #   k * (k - 1) // 2
                #
                # After counting them, we are done because all remaining elements
                # in this segment have been fully accounted for.
                if sorted_widths[left] == sorted_widths[right]:
                    count_same: int = right - left + 1
                    total_pairs += count_same * (count_same - 1) // 2
                    break

                # Situation B:
                # The left value and right value are different, but together they
                # form the target sum.
                #
                # Because duplicates may exist on either side, we must count how many
                # copies of the left value appear consecutively, and how many copies
                # of the right value appear consecutively.
                #
                # Then every left duplicate can pair with every right duplicate.
                # If left_count = 3 and right_count = 2, then these contribute 3 * 2 = 6 pairs.
                left_value: int = sorted_widths[left]
                right_value: int = sorted_widths[right]

                left_count: int = 0
                while left <= right and sorted_widths[left] == left_value:
                    left_count += 1
                    left += 1

                right_count: int = 0
                while right >= left and sorted_widths[right] == right_value:
                    right_count += 1
                    right -= 1

                total_pairs += left_count * right_count

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # widths = [1, 5, 3, 3, 2, 4], targetWidth = 6
    # Valid pairs by value:
    # - 1 + 5
    # - 2 + 4
    # - 3 + 3
    # Total = 3
    widths_1: List[int] = [1, 5, 3, 3, 2, 4]
    target_1: int = 6
    result_1: int = solution.count_shelf_pairs(widths_1, target_1)
    print("Example 1 Result:", result_1)  # Expected: 3

    # Example 2:
    # widths = [2, 2, 2, 2, 3, 1], targetWidth = 4
    # Only 2 + 2 works.
    # There are four 2s, so number of index pairs is:
    # 4 choose 2 = 6
    widths_2: List[int] = [2, 2, 2, 2, 3, 1]
    target_2: int = 4
    result_2: int = solution.count_shelf_pairs(widths_2, target_2)
    print("Example 2 Result:", result_2)  # Expected: 6

    # Additional quick checks for beginner-friendly demonstration.
    widths_3: List[int] = [0, 0, 0]
    target_3: int = 0
    result_3: int = solution.count_shelf_pairs(widths_3, target_3)
    print("Additional Example 3 Result:", result_3)  # Expected: 3

    widths_4: List[int] = [-1, 7, 2, 4, 3, 5]
    target_4: int = 6
    result_4: int = solution.count_shelf_pairs(widths_4, target_4)
    print("Additional Example 4 Result:", result_4)  # Expected: 2 (-1,7) and (2,4)