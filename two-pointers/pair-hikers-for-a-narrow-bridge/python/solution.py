"""
Title: Pair Hikers for a Narrow Bridge

Problem Description:
A hiking club needs to send people across a narrow suspension bridge. For safety,
at most two hikers can be on the bridge at the same time, and the combined weight
of any pair on the bridge cannot exceed a given limit. Each hiker must cross
exactly once. If a hiker cannot be paired safely, they must cross alone.

You are given an integer array weights where weights[i] is the weight of the
i-th hiker, and an integer limit representing the maximum total weight allowed
for one crossing. Return the minimum number of crossings needed to get everyone
across.

This is a practical optimization problem: using too many solo crossings wastes
time, but unsafe pairings are not allowed. An efficient solution should take
advantage of sorting and a two-pointer strategy to decide whether the lightest
remaining hiker can share a crossing with the heaviest remaining hiker.

Constraints:
- 1 <= weights.length <= 50000
- 1 <= weights[i] <= limit <= 100000
- Every hiker must be assigned to exactly one crossing
- A crossing may contain one or two hikers only

Example 1:
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation: One optimal plan is [50, 50], [70], [80]. So the minimum number
of crossings is 3.

Example 2:
Input: weights = [40, 60, 60, 40, 30], limit = 90
Output: 3
Explanation: One optimal plan is [30, 60], [40, 40], [60]. No arrangement can
do it in fewer than 3 crossings.
"""

from typing import List


class Solution:
    def min_crossings(self, weights: List[int], limit: int) -> int:
        """
        Compute the minimum number of bridge crossings needed.

        The method sorts the hikers by weight and then uses a two-pointer strategy:
        try to pair the lightest remaining hiker with the heaviest remaining hiker.
        If they fit within the weight limit, send them together. Otherwise, the
        heaviest hiker must cross alone.

        Args:
            weights: A list of integers where each value is a hiker's weight.
            limit: The maximum allowed total weight for one crossing.

        Returns:
            The minimum number of crossings required so every hiker crosses exactly once.

        Time complexity:
            O(n log n), due to sorting the list of weights.

        Space complexity:
            O(n) in Python's practical sorting behavior if considering the sorted copy.
            If sorting in place is considered, the extra pointer usage is O(1).
        """
        # We sort the weights so that:
        # - the lightest remaining hiker is easy to access from the left side
        # - the heaviest remaining hiker is easy to access from the right side
        #
        # This ordering is the key to the greedy strategy:
        # If the heaviest person cannot pair with the lightest person,
        # then the heaviest person cannot pair with anyone at all
        # (because everyone else is heavier than the lightest).
        #
        # Therefore, in that case, the heaviest must go alone.
        sorted_weights: List[int] = sorted(weights)

        # left points to the lightest hiker not yet assigned to a crossing.
        left: int = 0

        # right points to the heaviest hiker not yet assigned to a crossing.
        right: int = len(sorted_weights) - 1

        # This will count how many crossings we use in total.
        crossings: int = 0

        # Continue until all hikers have been assigned.
        #
        # The condition left <= right means:
        # - if left < right, there are at least two hikers left
        # - if left == right, exactly one hiker remains and must cross alone
        while left <= right:
            # Every iteration creates exactly one crossing.
            # That crossing always includes the heaviest remaining hiker
            # because we process from the right side inward.
            crossings += 1

            # Check whether the lightest and heaviest remaining hikers
            # can safely cross together.
            #
            # Why do we try this pair?
            # Because pairing the heaviest with the lightest gives the best
            # chance to fit within the limit while still using up the heaviest hiker.
            # This is the standard greedy choice for this problem.
            if sorted_weights[left] + sorted_weights[right] <= limit:
                # They can share one crossing.
                #
                # So we move both pointers:
                # - left moves rightward because the lightest hiker is now assigned
                # - right moves leftward because the heaviest hiker is now assigned
                left += 1
                right -= 1
            else:
                # They cannot share a crossing.
                #
                # Important reasoning:
                # If the heaviest hiker cannot pair with the lightest hiker,
                # then the heaviest hiker cannot pair with any other remaining hiker,
                # because every other remaining hiker is at least as heavy as the lightest.
                #
                # Therefore, the heaviest hiker must cross alone.
                # We only move the right pointer because only that hiker is assigned.
                right -= 1

        # After the loop ends, every hiker has been assigned to exactly one crossing.
        return crossings


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    weights1: List[int] = [70, 50, 80, 50]
    limit1: int = 100
    result1: int = solution.min_crossings(weights1, limit1)
    print("Example 1:")
    print(f"weights = {weights1}, limit = {limit1}")
    print(f"Minimum crossings = {result1}")
    print("Expected = 3")
    print()

    # Example 2
    weights2: List[int] = [40, 60, 60, 40, 30]
    limit2: int = 90
    result2: int = solution.min_crossings(weights2, limit2)
    print("Example 2:")
    print(f"weights = {weights2}, limit = {limit2}")
    print(f"Minimum crossings = {result2}")
    print("Expected = 3")
    print()

    # Additional beginner-friendly checks
    extra_weights1: List[int] = [50]
    extra_limit1: int = 100
    print("Extra Test 1:")
    print(f"weights = {extra_weights1}, limit = {extra_limit1}")
    print(f"Minimum crossings = {solution.min_crossings(extra_weights1, extra_limit1)}")
    print("Expected = 1")
    print()

    extra_weights2: List[int] = [30, 30, 30, 30]
    extra_limit2: int = 60
    print("Extra Test 2:")
    print(f"weights = {extra_weights2}, limit = {extra_limit2}")
    print(f"Minimum crossings = {solution.min_crossings(extra_weights2, extra_limit2)}")
    print("Expected = 2")