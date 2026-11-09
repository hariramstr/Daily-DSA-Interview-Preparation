"""
Title: Count Concert Duos Within Stage Width

Problem Description:
A concert organizer wants to place pairs of performers on the same movable stage.
Each performer has a preferred standing position represented by an integer in the
array positions. Two performers can share the stage if the absolute difference
between their preferred positions is less than or equal to widthLimit.

Given an integer array positions and an integer widthLimit, return the number of
index pairs (i, j) such that i < j and |positions[i] - positions[j]| <= widthLimit.

The array is not guaranteed to be sorted. A correct and efficient solution is
expected for large inputs, so a brute-force O(n^2) approach may time out.
Think about how sorting and a two-pointers sweep can help count many valid pairs
at once.

Constraints:
- 1 <= positions.length <= 200000
- -1000000000 <= positions[i] <= 1000000000
- 0 <= widthLimit <= 1000000000
- The answer may be larger than 32-bit integer range, so use a 64-bit integer
  type where necessary.

Example 1:
Input: positions = [1, 3, 4, 9], widthLimit = 3
Output: 3
Explanation: Valid pairs are (1,3), (1,4), and (3,4). Any pair involving 9
exceeds the allowed stage width.

Example 2:
Input: positions = [8, 2, 2, 5, 11], widthLimit = 0
Output: 1
Explanation: Only the two performers with preferred position 2 can share the
stage, because widthLimit is 0 and equal positions are required.

Goal:
Count all valid pairs efficiently.
"""

from typing import List


class Solution:
    def count_concert_duos(self, positions: List[int], width_limit: int) -> int:
        """
        Count how many index pairs (i, j) satisfy i < j and
        |positions[i] - positions[j]| <= width_limit.

        The method sorts the positions first, then uses a sliding window /
        two-pointers technique to count valid pairs in linear time after sorting.

        Args:
            positions: List of performer preferred positions.
            width_limit: Maximum allowed absolute difference between two positions.

        Returns:
            Total number of valid distinct index pairs.

        Time complexity:
            O(n log n), due to sorting. The two-pointer sweep itself is O(n).

        Space complexity:
            O(n) in Python in practice because sorting may use additional memory.
            If considering only explicit extra variables beyond the sorted data
            arrangement, the sweep uses O(1) extra space.
        """
        # If there are fewer than 2 performers, no pair can be formed.
        if len(positions) < 2:
            return 0

        # Sorting is the key idea that makes the two-pointer approach possible.
        #
        # Why sort?
        # ----------
        # In an unsorted array, checking whether many pairs satisfy the distance
        # condition is difficult without comparing many combinations directly.
        #
        # After sorting:
        # - For any indices left < right, we know sorted_positions[right] is
        #   greater than or equal to sorted_positions[left].
        # - Therefore, the absolute difference becomes:
        #       |a[right] - a[left]| = a[right] - a[left]
        #   because the right value is never smaller than the left value.
        # - This removes the need to consider both directions.
        #
        # Once sorted, if a window [left, right] satisfies:
        #       sorted_positions[right] - sorted_positions[left] <= width_limit
        # then every element between left and right can pair with right as well,
        # as long as it is inside the valid window.
        sorted_positions: List[int] = sorted(positions)

        # This variable stores the final count of valid pairs.
        #
        # In Python, int automatically grows as needed, so it safely handles
        # values larger than 32-bit integer range.
        total_pairs: int = 0

        # 'left' marks the beginning of the current valid window.
        #
        # For each 'right', we will move 'left' forward only when needed to make
        # sure the window satisfies:
        #       sorted_positions[right] - sorted_positions[left] <= width_limit
        #
        # Important property:
        # - 'left' never moves backward.
        # - Because of that, the whole sweep is O(n), not O(n^2).
        left: int = 0

        # We expand the window by moving 'right' from left to right.
        for right in range(len(sorted_positions)):
            # While the current window is invalid, shrink it from the left.
            #
            # The window is invalid when the smallest value in the window
            # (sorted_positions[left]) is too far from the largest value
            # (sorted_positions[right]).
            #
            # Because the array is sorted, if the leftmost value is too far away,
            # then it cannot form a valid pair with the current 'right'.
            # So we move 'left' forward until the window becomes valid again.
            while sorted_positions[right] - sorted_positions[left] > width_limit:
                left += 1

            # At this point, the window [left, right] is valid.
            #
            # That means:
            #   sorted_positions[right] - sorted_positions[left] <= width_limit
            #
            # Since the array is sorted, every index k with left <= k < right
            # also satisfies:
            #   sorted_positions[right] - sorted_positions[k] <= width_limit
            #
            # Why?
            # Because sorted_positions[k] >= sorted_positions[left], so replacing
            # sorted_positions[left] with sorted_positions[k] can only make the
            # difference smaller or equal.
            #
            # Therefore, the current 'right' can form valid pairs with all
            # previous elements inside the window.
            #
            # Number of such elements:
            #   right - left
            #
            # Example:
            #   If left = 2 and right = 5, then valid partners for index 5 are:
            #   2, 3, 4  -> exactly 5 - 2 = 3 elements.
            total_pairs += right - left

        return total_pairs

    def countPairs(self, positions: List[int], widthLimit: int) -> int:
        """
        Wrapper method using the exact parameter naming style from the prompt.

        Args:
            positions: List of performer preferred positions.
            widthLimit: Maximum allowed absolute difference between two positions.

        Returns:
            Total number of valid distinct index pairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n) in Python in practice due to sorting behavior.
        """
        return self.count_concert_duos(positions, widthLimit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    positions_1: List[int] = [1, 3, 4, 9]
    width_limit_1: int = 3
    result_1: int = solution.countPairs(positions_1, width_limit_1)
    print("Example 1 Result:", result_1)  # Expected: 3

    # Example 2
    positions_2: List[int] = [8, 2, 2, 5, 11]
    width_limit_2: int = 0
    result_2: int = solution.countPairs(positions_2, width_limit_2)
    print("Example 2 Result:", result_2)  # Expected: 1

    # Additional quick checks
    positions_3: List[int] = [5]
    width_limit_3: int = 10
    result_3: int = solution.countPairs(positions_3, width_limit_3)
    print("Single Performer Result:", result_3)  # Expected: 0

    positions_4: List[int] = [1, 1, 1, 1]
    width_limit_4: int = 0
    result_4: int = solution.countPairs(positions_4, width_limit_4)
    print("All Equal Positions Result:", result_4)  # Expected: 6