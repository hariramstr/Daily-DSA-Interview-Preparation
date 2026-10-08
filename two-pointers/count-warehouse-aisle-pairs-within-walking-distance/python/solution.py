"""
Title: Count Warehouse Aisle Pairs Within Walking Distance

Problem Description:
A warehouse stores picking stations along one long aisle. You are given an integer
array positions where positions[i] is the location of the i-th station measured in
meters from the start of the aisle. You are also given an integer maxDistance.

Two stations form a valid pair if the absolute difference between their positions is
less than or equal to maxDistance. Your task is to return the total number of
distinct valid pairs (i, j) such that i < j.

The input array is not guaranteed to be sorted. An efficient solution is expected
for large inputs, so a brute-force O(n^2) approach may time out. This problem is
intended to test whether you can combine sorting with a two-pointers scanning
strategy to count many pairs at once.

Return the number of valid pairs.

Constraints:
- 1 <= positions.length <= 200000
- -10^9 <= positions[i] <= 10^9
- 0 <= maxDistance <= 10^9
- The answer can be as large as n * (n - 1) / 2, so use a 64-bit integer type
  where needed.
"""

from typing import List


class Solution:
    def count_valid_pairs(self, positions: List[int], maxDistance: int) -> int:
        """
        Count the number of distinct station pairs whose distance is at most maxDistance.

        The algorithm first sorts the positions so that distance checks become easier.
        Then it uses a sliding window / two-pointers technique:
        - Expand the right pointer one step at a time.
        - Move the left pointer forward whenever the current window becomes invalid.
        - For each right pointer position, every index between left and right - 1 forms
          a valid pair with right, so we can count many pairs at once.

        Args:
            positions: List of station positions along the aisle.
            maxDistance: Maximum allowed distance between two stations.

        Returns:
            Total number of valid distinct pairs.

        Time complexity:
            O(n log n) due to sorting, plus O(n) for the two-pointer scan.

        Space complexity:
            O(n) in Python because sorted() creates a new list.
        """
        # Step 1: Sort the positions.
        #
        # Why sorting helps:
        # - The original array can be in any order.
        # - After sorting, if we fix a right endpoint, then all values to its left are
        #   in non-decreasing order.
        # - This means once a left index is too far from the right index, every even
        #   smaller index will also be too far. That property is exactly what makes
        #   the two-pointers technique efficient.
        sorted_positions: List[int] = sorted(positions)

        # This variable will store the total number of valid pairs.
        #
        # In Python, int automatically grows as needed, so it safely handles values
        # larger than 32-bit integer range. This is important because the number of
        # pairs can be as large as n * (n - 1) // 2.
        total_pairs: int = 0

        # The left pointer marks the smallest index that can still form a valid pair
        # with the current right pointer.
        left: int = 0

        # Step 2: Scan with the right pointer.
        #
        # For each right index:
        # - We ensure the window [left, right] satisfies:
        #       sorted_positions[right] - sorted_positions[left] <= maxDistance
        # - If it does not, we move left forward until it does.
        # - Once valid, every index from left to right - 1 forms a valid pair with right.
        #
        # Why is that true?
        # Because the array is sorted:
        # - sorted_positions[right] - sorted_positions[left] <= maxDistance
        # - Then for any k where left <= k < right:
        #       sorted_positions[right] - sorted_positions[k]
        #   is even smaller or equal, so it is also <= maxDistance.
        for right in range(len(sorted_positions)):
            # Step 2a: Shrink the window from the left until the current pair range
            # becomes valid.
            #
            # If the distance between the current rightmost station and the current
            # leftmost station is too large, then left cannot participate in any valid
            # pair with this right. So we move left forward.
            while sorted_positions[right] - sorted_positions[left] > maxDistance:
                left += 1

            # Step 2b: Count how many valid pairs end at index "right".
            #
            # All indices in [left, right - 1] can pair with right.
            # The number of such indices is:
            #     right - left
            #
            # Example:
            # If left = 2 and right = 5, then valid partners are indices 2, 3, 4.
            # Count = 5 - 2 = 3.
            total_pairs += right - left

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    positions1: List[int] = [8, 1, 4, 10, 6]
    max_distance1: int = 3
    result1: int = solution.count_valid_pairs(positions1, max_distance1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    positions2: List[int] = [5, 5, 5, 9]
    max_distance2: int = 0
    result2: int = solution.count_valid_pairs(positions2, max_distance2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick checks
    positions3: List[int] = [1]
    max_distance3: int = 10
    result3: int = solution.count_valid_pairs(positions3, max_distance3)
    print("Single Station Result:", result3)  # Expected: 0

    positions4: List[int] = [1, 2, 3, 4]
    max_distance4: int = 10
    result4: int = solution.count_valid_pairs(positions4, max_distance4)
    print("All Pairs Valid Result:", result4)  # Expected: 6