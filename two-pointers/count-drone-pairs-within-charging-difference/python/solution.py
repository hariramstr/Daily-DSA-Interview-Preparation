"""
Title: Count Drone Pairs Within Charging Difference

Problem Description:
A robotics team maintains a sorted list of battery charge levels for idle delivery drones.
To form a temporary relay pair, two drones can be matched only if the absolute difference
between their charge levels is at most maxDiff.

Given an integer array charge sorted in non-decreasing order, return the number of index
pairs (i, j) such that:
    0 <= i < j < charge.length
and
    charge[j] - charge[i] <= maxDiff

Because the number of valid pairs can be large, the algorithm must be efficient for large
inputs. A brute-force O(n^2) solution is too slow under the full constraints.

Constraints:
- 1 <= charge.length <= 2 * 10^5
- 0 <= charge[i] <= 10^9
- charge is sorted in non-decreasing order
- 0 <= maxDiff <= 10^9
- The answer may exceed 32-bit integer range, so use a 64-bit integer type if needed

Examples:
1)
Input: charge = [10, 12, 13, 17], maxDiff = 3
Output: 3
Explanation:
Valid pairs are:
- (10, 12)
- (10, 13)
- (12, 13)

2)
Input: charge = [4, 4, 5, 8, 9], maxDiff = 1
Output: 4
Explanation:
Valid pairs are:
- the pair formed by the two 4s
- both pairs (4, 5)
- (8, 9)
"""

from typing import List


class Solution:
    def count_drone_pairs(self, charge: List[int], maxDiff: int) -> int:
        """
        Count the number of valid index pairs (i, j) such that i < j and
        charge[j] - charge[i] <= maxDiff.

        Args:
            charge: A list of drone charge levels sorted in non-decreasing order.
            maxDiff: The maximum allowed difference between two paired drones.

        Returns:
            The total number of valid pairs.

        Time Complexity:
            O(n), where n is the length of charge.
            Each pointer moves from left to right at most n times.

        Space Complexity:
            O(1), ignoring the input array.
        """
        # We will use the classic "two pointers" / "sliding window" technique.
        #
        # Why this works:
        # - The array is already sorted.
        # - For a fixed right endpoint j, if charge[j] - charge[i] <= maxDiff
        #   for some i, then every index k with i <= k < j also satisfies
        #   charge[j] - charge[k] <= maxDiff, because charge[k] >= charge[i].
        #
        # This means that for each right pointer, once we find the smallest valid
        # left pointer, all indices between left and right - 1 form valid pairs
        # with right.
        #
        # Instead of checking every pair explicitly, we count them in bulk.

        n: int = len(charge)

        # left marks the beginning of the current valid window.
        left: int = 0

        # total_pairs stores the answer.
        # In Python, int automatically handles large values, so it is safe even if
        # the result exceeds 32-bit integer range.
        total_pairs: int = 0

        # Move right from left to right across the array.
        for right in range(n):
            # The current window is intended to satisfy:
            # charge[right] - charge[left] <= maxDiff
            #
            # If the difference is too large, we must move left forward until the
            # window becomes valid again.
            #
            # Because the array is sorted, increasing left can only decrease
            # charge[right] - charge[left], which is exactly what we need.
            while charge[right] - charge[left] > maxDiff:
                left += 1

            # At this point, left is the smallest index such that:
            # charge[right] - charge[left] <= maxDiff
            #
            # Therefore, every index from left to right - 1 can pair with right.
            #
            # Number of such indices:
            #   right - left
            #
            # Example:
            # if left = 2 and right = 5, then valid partners for index 5 are:
            #   2, 3, 4
            # which is 5 - 2 = 3 indices.
            total_pairs += right - left

        return total_pairs

    def countPairs(self, charge: List[int], maxDiff: int) -> int:
        """
        Wrapper method matching a compact interview-style naming convention.

        Args:
            charge: A list of drone charge levels sorted in non-decreasing order.
            maxDiff: The maximum allowed difference between two paired drones.

        Returns:
            The total number of valid pairs.

        Time Complexity:
            O(n)

        Space Complexity:
            O(1)
        """
        return self.count_drone_pairs(charge, maxDiff)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    charge1: List[int] = [10, 12, 13, 17]
    max_diff1: int = 3
    result1: int = solution.count_drone_pairs(charge1, max_diff1)
    print("Example 1:")
    print("charge =", charge1)
    print("maxDiff =", max_diff1)
    print("Output =", result1)
    print("Expected = 3")
    print()

    # Manual verification for Example 1:
    # Pairs:
    # (10,12) -> diff 2, valid
    # (10,13) -> diff 3, valid
    # (10,17) -> diff 7, invalid
    # (12,13) -> diff 1, valid
    # (12,17) -> diff 5, invalid
    # (13,17) -> diff 4, invalid
    # Total = 3

    # Example 2
    charge2: List[int] = [4, 4, 5, 8, 9]
    max_diff2: int = 1
    result2: int = solution.count_drone_pairs(charge2, max_diff2)
    print("Example 2:")
    print("charge =", charge2)
    print("maxDiff =", max_diff2)
    print("Output =", result2)
    print("Expected = 4")
    print()

    # Manual verification for Example 2:
    # Pairs:
    # (4,4) -> diff 0, valid
    # (4,5) -> diff 1, valid
    # (4,8) -> diff 4, invalid
    # (4,9) -> diff 5, invalid
    # (4,5) -> diff 1, valid   <- second 4 with 5
    # (4,8) -> diff 4, invalid
    # (4,9) -> diff 5, invalid
    # (5,8) -> diff 3, invalid
    # (5,9) -> diff 4, invalid
    # (8,9) -> diff 1, valid
    # Total = 4

    # Additional small sanity check
    charge3: List[int] = [1]
    max_diff3: int = 0
    result3: int = solution.count_drone_pairs(charge3, max_diff3)
    print("Additional Test:")
    print("charge =", charge3)
    print("maxDiff =", max_diff3)
    print("Output =", result3)
    print("Expected = 0")