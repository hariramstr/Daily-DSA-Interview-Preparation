"""
Title: Closest Badge Pair Across Two Sorted Gates

Problem Description:
A company campus has two security gates, Gate A and Gate B. Each gate logs badge IDs
in non-decreasing order as employees pass through. You are given two sorted integer
arrays `gateA` and `gateB`, where each value represents a badge ID seen at that gate.
The same badge ID may appear multiple times in either array.

Your task is to find one pair of badge IDs `(a, b)` such that `a` comes from `gateA`,
`b` comes from `gateB`, and the absolute difference `|a - b|` is as small as possible.
Return that minimum absolute difference.

If either array is empty, return `-1` because no cross-gate pair can be formed.

This problem should be solved efficiently by taking advantage of the fact that both
arrays are already sorted. A brute-force solution that compares every pair may be too
slow for large inputs.

Constraints:
- 0 <= gateA.length, gateB.length <= 2 * 10^5
- -10^9 <= gateA[i], gateB[i] <= 10^9
- gateA and gateB are each sorted in non-decreasing order
- Duplicate values are allowed

Example 1:
Input: gateA = [3, 8, 15, 21], gateB = [1, 9, 18, 30]
Output: 1
Explanation: The closest cross-gate pair is `(8, 9)`, so the minimum absolute difference is `1`.

Example 2:
Input: gateA = [-10, -4, 0, 7], gateB = [-6, -3, 2, 11]
Output: 1
Explanation: There are several close pairs. The best valid pair is `(-4, -3)`,
giving a difference of `1`.
"""

from typing import List


class Solution:
    def min_badge_difference(self, gateA: List[int], gateB: List[int]) -> int:
        """
        Find the minimum absolute difference between one value from gateA and one value from gateB.

        Args:
            gateA: A sorted list of integers representing badge IDs seen at Gate A.
            gateB: A sorted list of integers representing badge IDs seen at Gate B.

        Returns:
            The minimum absolute difference between any cross-gate pair.
            Returns -1 if either input list is empty.

        Time complexity:
            O(len(gateA) + len(gateB))
            Each pointer moves forward at most once through its array.

        Space complexity:
            O(1)
            Only a few variables are used regardless of input size.
        """
        # If either array is empty, we cannot form a pair with one element from each gate.
        # The problem explicitly asks us to return -1 in this case.
        if not gateA or not gateB:
            return -1

        # We use two pointers:
        # - i points into gateA
        # - j points into gateB
        #
        # Because both arrays are already sorted, we can compare the current values and
        # decide which pointer to move in a way that never misses the optimal answer.
        i: int = 0
        j: int = 0

        # Start with a very large answer.
        # We will keep shrinking this whenever we find a smaller absolute difference.
        min_diff: int = float("inf")

        # Continue while both pointers are still inside their arrays.
        #
        # Why this works:
        # At each step, we compare gateA[i] and gateB[j].
        # Suppose gateA[i] < gateB[j].
        # Then any earlier element in gateA is already behind us, and moving j forward
        # would only keep gateB[j] the same or make it larger, which cannot improve the
        # difference with the smaller gateA[i] as effectively as moving i forward might.
        #
        # So:
        # - If gateA[i] < gateB[j], move i forward.
        # - If gateA[i] > gateB[j], move j forward.
        # - If they are equal, the difference is 0, which is the best possible answer,
        #   so we can return immediately.
        while i < len(gateA) and j < len(gateB):
            a_value: int = gateA[i]
            b_value: int = gateB[j]

            # Compute the current absolute difference for this pair.
            current_diff: int = abs(a_value - b_value)

            # Update the best answer seen so far.
            if current_diff < min_diff:
                min_diff = current_diff

            # If the values are exactly equal, then the absolute difference is 0.
            # No answer can be better than 0, so we can stop early.
            if min_diff == 0:
                return 0

            # Move the pointer that points to the smaller value.
            #
            # Detailed reasoning:
            # - If a_value is smaller, then to possibly get closer to b_value,
            #   we need a larger value from gateA, so increment i.
            # - If b_value is smaller, then to possibly get closer to a_value,
            #   we need a larger value from gateB, so increment j.
            if a_value < b_value:
                i += 1
            else:
                j += 1

        # Once one pointer reaches the end, no more cross-gate pairs can be formed.
        # The smallest difference found during the scan is the answer.
        return min_diff


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    gateA1: List[int] = [3, 8, 15, 21]
    gateB1: List[int] = [1, 9, 18, 30]
    result1: int = solution.min_badge_difference(gateA1, gateB1)
    print("Example 1 result:", result1)  # Expected: 1

    # Example 2 from the problem statement
    gateA2: List[int] = [-10, -4, 0, 7]
    gateB2: List[int] = [-6, -3, 2, 11]
    result2: int = solution.min_badge_difference(gateA2, gateB2)
    print("Example 2 result:", result2)  # Expected: 1

    # Additional sample: one array empty
    gateA3: List[int] = []
    gateB3: List[int] = [5, 10]
    result3: int = solution.min_badge_difference(gateA3, gateB3)
    print("Empty array result:", result3)  # Expected: -1

    # Additional sample: exact match exists
    gateA4: List[int] = [1, 4, 7, 10]
    gateB4: List[int] = [2, 4, 8]
    result4: int = solution.min_badge_difference(gateA4, gateB4)
    print("Exact match result:", result4)  # Expected: 0