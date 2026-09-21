"""
Title: Count Team Pairings Within Experience Gap

Problem Description:
You are given an integer array experience where experience[i] is the number of years
of experience of the i-th engineer, and an integer gap. A pair of engineers (i, j)
is considered compatible if i < j and the absolute difference between their
experience values is less than or equal to gap.

Return the total number of compatible pairs.

A straightforward O(n^2) solution checks every pair, but that is too slow for large
inputs. Your task is to design an efficient algorithm using sorting and a
two-pointers technique.

Because the pair condition depends only on the difference between two values, the
array may be reordered during processing. However, the final answer should count
pairs from the original set of engineers, not based on their positions after sorting.

Constraints:
- 1 <= experience.length <= 200000
- 0 <= experience[i] <= 1000000000
- 0 <= gap <= 1000000000
- The answer may be large, so use a 64-bit integer type where needed.

Example 1:
Input: experience = [1, 3, 4, 7], gap = 3
Output: 4

Example 2:
Input: experience = [5, 5, 5, 8, 10], gap = 0
Output: 3
"""

from typing import List


class Solution:
    def count_compatible_pairs(self, experience: List[int], gap: int) -> int:
        """
        Count the number of engineer pairs whose experience difference is at most gap.

        Args:
            experience: List of engineer experience values.
            gap: Maximum allowed absolute difference for a compatible pair.

        Returns:
            Total number of compatible pairs.

        Time complexity:
            O(n log n), due to sorting. The two-pointer scan is O(n).

        Space complexity:
            O(1) or O(log n) extra space beyond the sort implementation,
            depending on Python's internal sorting behavior.
        """
        # We are allowed to reorder the values because compatibility depends only on
        # the experience numbers themselves, not on the original positions.
        # Sorting is the key step that makes the two-pointer strategy possible.
        #
        # After sorting:
        # - For any indices left < right, we know experience[right] >= experience[left].
        # - Therefore, the absolute difference becomes:
        #       abs(experience[right] - experience[left])
        #   which is simply:
        #       experience[right] - experience[left]
        #
        # This removes the need to check both directions and lets us maintain a
        # sliding window of valid values.
        experience.sort()

        # This variable will store the total number of valid pairs.
        # In Python, int automatically handles large values, so it is safe even when
        # the number of pairs is very large.
        total_pairs: int = 0

        # 'left' marks the beginning of the current valid window.
        # For each 'right', we will move 'left' forward until the difference
        # between experience[right] and experience[left] is <= gap.
        left: int = 0

        # We now expand the window one engineer at a time using 'right'.
        for right in range(len(experience)):
            # If the current window is invalid, move 'left' forward until it becomes valid.
            #
            # Why this works:
            # - The array is sorted.
            # - If experience[right] - experience[left] > gap, then engineer 'left'
            #   is too far away in value from engineer 'right'.
            # - Any even earlier index would also be too far away, so the only fix
            #   is to move 'left' forward.
            while experience[right] - experience[left] > gap:
                left += 1

            # At this point, the window [left, right] is valid, meaning:
            #   experience[right] - experience[left] <= gap
            #
            # Because the array is sorted, every index k in [left, right - 1] also
            # satisfies:
            #   experience[right] - experience[k] <= gap
            #
            # So engineer 'right' can form a valid pair with every engineer from
            # 'left' through 'right - 1'.
            #
            # Number of such engineers:
            #   right - left
            #
            # We add that count directly instead of checking each pair one by one.
            total_pairs += right - left

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    experience_1: List[int] = [1, 3, 4, 7]
    gap_1: int = 3
    result_1: int = solution.count_compatible_pairs(experience_1, gap_1)
    print(result_1)  # Expected: 4

    # Example 2
    experience_2: List[int] = [5, 5, 5, 8, 10]
    gap_2: int = 0
    result_2: int = solution.count_compatible_pairs(experience_2, gap_2)
    print(result_2)  # Expected: 3

    # Additional quick checks
    experience_3: List[int] = [2]
    gap_3: int = 5
    result_3: int = solution.count_compatible_pairs(experience_3, gap_3)
    print(result_3)  # Expected: 0

    experience_4: List[int] = [1, 2, 3, 4, 5]
    gap_4: int = 1
    result_4: int = solution.count_compatible_pairs(experience_4, gap_4)
    print(result_4)  # Expected: 4