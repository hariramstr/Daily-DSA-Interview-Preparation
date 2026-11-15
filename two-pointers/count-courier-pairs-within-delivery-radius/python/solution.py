"""
Title: Count Courier Pairs Within Delivery Radius

Problem Description:
A logistics company stores the home coordinates of its couriers along a straight road.
You are given an integer array `positions`, where `positions[i]` is the location of the
`i`th courier on that road, and an integer `radius`.

Two couriers can be assigned to the same local dispatch zone if the absolute difference
between their positions is less than or equal to `radius`.

Your task is to return the total number of distinct courier pairs `(i, j)` such that
`i < j` and `|positions[i] - positions[j]| <= radius`.

The input array is not guaranteed to be sorted. Because the number of couriers can be
large, an `O(n^2)` solution may time out. Design an efficient algorithm using sorting
and the two-pointers technique.

Constraints:
- 1 <= positions.length <= 200000
- -10^9 <= positions[i] <= 10^9
- 0 <= radius <= 10^9
- Return the answer as a 64-bit integer if your language requires it, since the number
  of valid pairs may exceed 32-bit range.

Example 1:
Input: positions = [4, 1, 7, 3, 9], radius = 3
Output: 4
Explanation:
After sorting, positions = [1, 3, 4, 7, 9].
Valid pairs are (1,3), (1,4), (3,4), and (7,9).

Example 2:
Input: positions = [10, 10, 10, 15], radius = 0
Output: 3
Explanation:
Only couriers at the exact same location can form a valid pair.
The three couriers at position 10 create 3 pairs.
"""

from typing import List


class Solution:
    def count_courier_pairs(self, positions: List[int], radius: int) -> int:
        """
        Count the number of distinct courier pairs whose distance is at most `radius`.

        The method first sorts the positions so that nearby values become adjacent.
        Then it uses the two-pointers / sliding window technique to count, for each
        right endpoint, how many left endpoints form a valid pair.

        Args:
            positions: A list of courier positions along a straight road.
            radius: The maximum allowed absolute distance between two couriers.

        Returns:
            The total number of valid distinct pairs as an integer.

        Time complexity:
            O(n log n), due to sorting. The two-pointer scan itself is O(n).

        Space complexity:
            O(n) in Python because `sorted()` creates a new list.
        """
        # Step 1: Sort the positions.
        #
        # Why sorting helps:
        # The condition we care about is:
        #     |positions[i] - positions[j]| <= radius
        #
        # After sorting, if we ensure i < j in the sorted array, then:
        #     sorted_positions[j] >= sorted_positions[i]
        #
        # So the absolute value is no longer necessary:
        #     |sorted_positions[j] - sorted_positions[i]|
        # becomes
        #     sorted_positions[j] - sorted_positions[i]
        #
        # This makes it much easier to maintain a valid window of positions.
        sorted_positions: List[int] = sorted(positions)

        # This variable will store the final number of valid pairs.
        #
        # In Python, integers automatically grow as needed, so we do not need
        # special 64-bit handling. Still, conceptually, this answer may be large,
        # up to roughly n * (n - 1) / 2.
        total_pairs: int = 0

        # `left` marks the beginning of the current valid window.
        #
        # For each `right`, we want the smallest `left` such that:
        #     sorted_positions[right] - sorted_positions[left] <= radius
        #
        # Then every index from `left` to `right - 1` forms a valid pair with `right`.
        left: int = 0

        # Step 2: Expand the window using `right`.
        #
        # We treat each index as the "right endpoint" of a window.
        # For each right, we move left forward only when the window becomes invalid.
        #
        # Important efficiency idea:
        # `left` never moves backward, only forward.
        # Therefore, across the whole loop, `left` moves at most n times total.
        for right in range(len(sorted_positions)):
            # Step 3: Shrink the window from the left until it becomes valid.
            #
            # If the distance between the current rightmost value and the leftmost
            # value is too large, then the pair is invalid, and so are any windows
            # starting even further left. Therefore we must move `left` forward.
            while sorted_positions[right] - sorted_positions[left] > radius:
                left += 1

            # Step 4: Count how many valid pairs end at `right`.
            #
            # At this point, the window [left, right] is valid, meaning:
            #     sorted_positions[right] - sorted_positions[left] <= radius
            #
            # Because the array is sorted, every index k in [left, right - 1] also
            # satisfies:
            #     sorted_positions[right] - sorted_positions[k] <= radius
            #
            # So the number of valid pairs with `right` as the second element is:
            #     right - left
            #
            # Example:
            # If left = 2 and right = 5, then valid partners are indices:
            #     2, 3, 4
            # Count = 5 - 2 = 3
            total_pairs += right - left

        return total_pairs

    def countPairs(self, positions: List[int], radius: int) -> int:
        """
        Compatibility wrapper that calls the main counting method.

        Args:
            positions: A list of courier positions.
            radius: Maximum allowed distance between two couriers.

        Returns:
            The total number of valid distinct pairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return self.count_courier_pairs(positions, radius)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solver = Solution()

    # Example 1 from the problem statement.
    #
    # Original input:
    # positions = [4, 1, 7, 3, 9], radius = 3
    #
    # After sorting:
    # [1, 3, 4, 7, 9]
    #
    # Valid pairs:
    # (1,3), (1,4), (3,4), (7,9)
    # Total = 4
    positions1: List[int] = [4, 1, 7, 3, 9]
    radius1: int = 3
    result1: int = solver.count_courier_pairs(positions1, radius1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2 from the problem statement.
    #
    # Original input:
    # positions = [10, 10, 10, 15], radius = 0
    #
    # Only equal positions can pair because radius is 0.
    # The three 10s form:
    # C(3, 2) = 3 pairs
    positions2: List[int] = [10, 10, 10, 15]
    radius2: int = 0
    result2: int = solver.count_courier_pairs(positions2, radius2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional small sanity checks for beginners.

    # Single courier: no pair can be formed.
    positions3: List[int] = [5]
    radius3: int = 10
    result3: int = solver.count_courier_pairs(positions3, radius3)
    print("Single Courier Result:", result3)  # Expected: 0

    # All couriers close enough to each other.
    # Sorted: [1, 2, 3]
    # All pairs valid: (1,2), (1,3), (2,3) => 3
    positions4: List[int] = [3, 1, 2]
    radius4: int = 2
    result4: int = solver.count_courier_pairs(positions4, radius4)
    print("All Valid Pairs Result:", result4)  # Expected: 3