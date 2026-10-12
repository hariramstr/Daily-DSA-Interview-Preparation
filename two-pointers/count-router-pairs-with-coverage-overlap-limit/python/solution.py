"""
Title: Count Router Pairs With Coverage Overlap Limit

Problem Description:
You are given a sorted array `positions` where `positions[i]` is the location of the
i-th Wi-Fi router installed along a straight road. For network planning, two routers
are considered a compatible pair if the distance between them is at most `maxGap`.

Your task is to count how many index pairs `(i, j)` with `0 <= i < j < n` satisfy:
    positions[j] - positions[i] <= maxGap

Because the router locations are already sorted, a brute-force solution that checks
all pairs would be too slow for large inputs. Design an efficient algorithm using
the two-pointers technique.

Return the total number of compatible pairs.

Constraints:
- 1 <= positions.length <= 2 * 10^5
- 0 <= positions[i] <= 10^9
- positions is sorted in non-decreasing order
- 0 <= maxGap <= 10^9
- The answer may exceed 32-bit integer range, so use a 64-bit integer type when needed.

Examples:
1) positions = [1, 3, 4, 9, 10], maxGap = 3
   Output: 4

2) positions = [2, 2, 5, 6, 8], maxGap = 2
   Correct Output: 3
"""

from typing import List


class Solution:
    def count_compatible_pairs(self, positions: List[int], maxGap: int) -> int:
        """
        Count how many index pairs (i, j) with i < j satisfy
        positions[j] - positions[i] <= maxGap.

        This uses the two-pointers / sliding window technique on the already sorted
        array. For each left index, we expand the right pointer as far as possible
        while the distance condition remains valid. Then we count how many indices
        between left+1 and right-1 can pair with left.

        Args:
            positions: Sorted list of router positions.
            maxGap: Maximum allowed distance between two routers.

        Returns:
            Total number of valid index pairs.

        Time complexity:
            O(n), because each pointer moves from left to right at most n times.

        Space complexity:
            O(1), excluding the input array.
        """
        n: int = len(positions)

        # This variable stores the final answer.
        # In Python, int automatically supports large values, so it safely handles
        # results larger than 32-bit integer range.
        total_pairs: int = 0

        # `right` is the second pointer.
        # It represents the first index that is currently OUTSIDE the valid window
        # for the current `left`.
        #
        # More specifically:
        # - all indices in [left + 1, right - 1] are valid partners for `left`
        # - index `right` is either out of bounds or invalid
        #
        # We start it at 0 and maintain the invariant that `right >= left + 1`
        # as we iterate.
        right: int = 0

        # We move `left` from the beginning to the end of the array.
        # For each `left`, we determine how far `right` can go while keeping:
        # positions[right] - positions[left] <= maxGap
        for left in range(n):
            # Ensure the right pointer is never behind the next possible partner.
            # If right <= left, then there are no candidate j values yet, so we
            # move right to left + 1.
            if right < left + 1:
                right = left + 1

            # Expand the right pointer as long as:
            # 1) it is still inside the array, and
            # 2) the pair (left, right) is valid
            #
            # Because the array is sorted, once positions[right] - positions[left]
            # becomes too large, every later index will also be too large for this
            # same `left`. That is exactly why the two-pointers technique works here.
            while right < n and positions[right] - positions[left] <= maxGap:
                right += 1

            # At this point:
            # - right is the first invalid index for this left, OR right == n
            # - therefore valid partners are:
            #   left + 1, left + 2, ..., right - 1
            #
            # Count of valid partners:
            #   (right - 1) - (left + 1) + 1 = right - left - 1
            #
            # If there are no valid partners, this expression becomes 0.
            total_pairs += right - left - 1

        return total_pairs

    def solve(self, positions: List[int], maxGap: int) -> int:
        """
        Wrapper method to compute the number of compatible router pairs.

        Args:
            positions: Sorted list of router positions.
            maxGap: Maximum allowed distance between two routers.

        Returns:
            Number of compatible pairs.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.count_compatible_pairs(positions, maxGap)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    positions1: List[int] = [1, 3, 4, 9, 10]
    max_gap1: int = 3
    result1: int = solution.solve(positions1, max_gap1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    # Important note:
    # The problem statement text contains some ambiguity, but after checking all pairs:
    # positions = [2, 2, 5, 6, 8], maxGap = 2
    # Valid pairs are:
    # (0,1): 2 - 2 = 0
    # (2,3): 6 - 5 = 1
    # (3,4): 8 - 6 = 2
    # Total = 3
    positions2: List[int] = [2, 2, 5, 6, 8]
    max_gap2: int = 2
    result2: int = solution.solve(positions2, max_gap2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick sanity checks for beginners:
    positions3: List[int] = [5]
    max_gap3: int = 10
    result3: int = solution.solve(positions3, max_gap3)
    print("Single Router Result:", result3)  # Expected: 0

    positions4: List[int] = [1, 1, 1, 1]
    max_gap4: int = 0
    result4: int = solution.solve(positions4, max_gap4)
    print("All Equal Positions Result:", result4)  # Expected: 6

    positions5: List[int] = [1, 10, 20, 30]
    max_gap5: int = 5
    result5: int = solution.solve(positions5, max_gap5)
    print("No Valid Pairs Result:", result5)  # Expected: 0