"""
Title: Minimum Energy to Finish a Workout Plan

Problem Description:
You are given a workout plan represented by an integer array energy, where energy[i]
is the energy cost of completing exercise i. You start before the first exercise and
want to finish by reaching just beyond the last exercise. On each move, you may
complete either the next 1 exercise or the next 2 exercises. If you land on an
exercise, you must pay its energy cost. Your goal is to find the minimum total energy
needed to finish the plan.

For example, if you skip directly over an exercise by taking a 2-exercise move, you do
not pay the cost of the skipped exercise. This means you should choose a path that
minimizes the sum of the costs of the exercises you actually land on.

Return the minimum total energy required to finish the workout plan.

Constraints:
- 2 <= energy.length <= 1000
- 0 <= energy[i] <= 999
- The answer fits in a 32-bit integer.

Example 1:
Input: energy = [4, 1, 6, 2]
Output: 3
Explanation: One optimal path is to land on exercise 1 (cost 1), then exercise 3
(cost 2), then finish. Total energy = 1 + 2 = 3.

Example 2:
Input: energy = [3, 5, 2, 1, 4]
Output: 6
Explanation: One optimal path is to land on exercise 0 (cost 3), then exercise 2
(cost 2), then exercise 3 (cost 1), then finish. Total energy = 6.

This is a classic dynamic programming problem because the minimum energy needed to
reach a position depends only on the minimum energy needed to reach the previous one
or two positions. A solution with linear time and constant or linear extra space is
expected.
"""

from typing import List


class Solution:
    def min_energy(self, energy: List[int]) -> int:
        """
        Compute the minimum total energy required to finish the workout plan.

        We use dynamic programming with constant extra space. The key idea is:
        to land on exercise i, we must have come from exercise i - 1 or i - 2.
        Therefore, the minimum cost to reach exercise i depends only on the
        minimum costs to reach those two previous exercises.

        Args:
            energy: A list where energy[i] is the cost paid if we land on exercise i.

        Returns:
            The minimum total energy needed to move from before the first exercise
            to just beyond the last exercise.

        Time complexity:
            O(n), where n is the number of exercises.

        Space complexity:
            O(1), because we store only the last two DP values.
        """
        n: int = len(energy)

        # Special note about the model:
        # We start "before" index 0, and the goal is to reach position n
        # (just beyond the last exercise).
        #
        # You can move 1 or 2 positions at a time.
        # You only pay when you LAND on an actual exercise index.
        # You do NOT pay anything for the starting position or the finishing position.
        #
        # This is exactly the same recurrence pattern as the classic
        # "min cost climbing stairs" problem.

        # dp[i] concept:
        # Let dp[i] mean the minimum energy needed to land on exercise i.
        #
        # Then:
        # dp[0] = energy[0]
        # dp[1] = energy[1]
        #
        # Why is dp[1] = energy[1]?
        # Because from the start, we are allowed to move 2 exercises at once,
        # so we can jump directly to exercise 1 and pay only energy[1].
        #
        # For i >= 2:
        # dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])
        #
        # Finally, to finish beyond the last exercise:
        # We can come from the last exercise (n - 1) or the second-last (n - 2),
        # because the final move can also be 1 or 2 exercises.
        # So the answer is:
        # min(dp[n - 1], dp[n - 2])

        # Base case for the first exercise:
        prev2: int = energy[0]

        # Base case for the second exercise:
        prev1: int = energy[1]

        # If there are only 2 exercises, we can finish by stepping on either
        # exercise 0 then finish, or exercise 1 then finish.
        # The cheaper of those two is the answer.
        if n == 2:
            return min(prev2, prev1)

        # Process exercises from index 2 onward.
        for i in range(2, n):
            # To land on exercise i, we must come from i - 1 or i - 2.
            # We choose the cheaper path, then add the cost of landing on i.
            current: int = energy[i] + min(prev1, prev2)

            # Shift our rolling DP window forward:
            # - prev2 becomes the old prev1
            # - prev1 becomes the newly computed current
            prev2, prev1 = prev1, current

        # After computing all landing costs:
        # - prev1 holds dp[n - 1]
        # - prev2 holds dp[n - 2]
        #
        # To finish, we can jump from either of those positions to "beyond the end".
        return min(prev1, prev2)

    def minEnergy(self, energy: List[int]) -> int:
        """
        Wrapper method using camelCase naming for compatibility with common
        coding platform conventions.

        Args:
            energy: A list where energy[i] is the cost paid if we land on exercise i.

        Returns:
            The minimum total energy needed to finish the workout plan.

        Time complexity:
            O(n), where n is the number of exercises.

        Space complexity:
            O(1).
        """
        return self.min_energy(energy)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # energy = [4, 1, 6, 2]
    # Possible optimal path:
    # start -> 1 (pay 1) -> 3 (pay 2) -> finish
    # total = 3
    energy1: List[int] = [4, 1, 6, 2]
    result1: int = solution.minEnergy(energy1)
    print(f"Input: {energy1}")
    print(f"Minimum energy: {result1}")
    print("Expected: 3")
    print()

    # Example 2:
    # energy = [3, 5, 2, 1, 4]
    # One optimal path:
    # start -> 0 (pay 3) -> 2 (pay 2) -> 3 (pay 1) -> finish
    # total = 6
    energy2: List[int] = [3, 5, 2, 1, 4]
    result2: int = solution.minEnergy(energy2)
    print(f"Input: {energy2}")
    print(f"Minimum energy: {result2}")
    print("Expected: 6")
    print()

    # Additional beginner-friendly test cases:
    energy3: List[int] = [10, 15]
    result3: int = solution.minEnergy(energy3)
    print(f"Input: {energy3}")
    print(f"Minimum energy: {result3}")
    print("Expected: 10")
    print()

    energy4: List[int] = [1, 100, 1, 1, 1, 100, 1, 1, 100, 1]
    result4: int = solution.minEnergy(energy4)
    print(f"Input: {energy4}")
    print(f"Minimum energy: {result4}")
    print("Expected: 6")