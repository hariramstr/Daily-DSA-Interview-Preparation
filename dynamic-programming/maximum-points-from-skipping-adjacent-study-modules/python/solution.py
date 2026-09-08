"""
Title: Maximum Points from Skipping Adjacent Study Modules

Problem Description:
You are preparing for a certification exam. The course platform shows a list of study
modules, and each module gives a certain number of points if you complete it.
However, completing two adjacent modules on the same day causes too much fatigue,
so you are not allowed to complete both module i and module i + 1.

Given an integer array points where points[i] is the score earned by completing the
i-th module, return the maximum total points you can earn under this rule.

You may choose to skip any module. If the array is empty, the answer is 0.

This is a classic dynamic programming decision process: for each module, you either
skip it and keep the best score so far, or complete it and add its points to the
best score from two modules earlier. Your task is to compute the best possible total.

Constraints:
- 0 <= points.length <= 100
- 0 <= points[i] <= 1000

Example 1:
Input: points = [4, 2, 7, 3, 9]
Output: 20
Explanation: Complete modules with scores 4, 7, and 9. These are not adjacent,
and the total is 4 + 7 + 9 = 20.

Example 2:
Input: points = [5, 1, 1, 5]
Output: 10
Explanation: Complete the first and last modules. Completing both middle modules
would block better choices. The maximum total is 10.
"""

from typing import List


class Solution:
    def max_points(self, points: List[int]) -> int:
        """
        Compute the maximum total points that can be earned without taking
        two adjacent modules.

        Args:
            points: A list where points[i] is the score of the i-th module.

        Returns:
            The maximum total points obtainable under the non-adjacent rule.

        Time complexity:
            O(n), where n is the number of modules, because we process each
            module exactly once.

        Space complexity:
            O(n), because we store a dynamic programming array of size n + 1.
        """
        # If there are no modules at all, there is nothing to complete,
        # so the best possible score is simply 0.
        if not points:
            return 0

        # Store the number of modules for convenience.
        n: int = len(points)

        # Create a DP array where:
        # dp[i] represents the maximum points we can earn by considering
        # the first i modules.
        #
        # Important indexing idea:
        # - dp[0] means "considering zero modules" -> answer is 0
        # - dp[1] means "considering only the first module"
        #
        # This 1-based DP setup makes the transition easier to read because
        # when we are deciding about the current module, we can naturally
        # refer to:
        # - dp[i - 1] -> skip current module
        # - dp[i - 2] + points[i - 1] -> take current module
        dp: List[int] = [0] * (n + 1)

        # Base case for the first module:
        # If we only consider the first module, the best we can do is either:
        # - take it, earning points[0]
        # - or skip it, earning 0
        #
        # Since points[i] is guaranteed to be non-negative, taking it is at
        # least as good as skipping it. So dp[1] = points[0].
        dp[1] = points[0]

        # Process modules from the second one onward.
        # Here, i means "we are filling dp[i]", which corresponds to the
        # module at index i - 1 in the original points list.
        for i in range(2, n + 1):
            # Option 1: Skip the current module.
            # If we skip it, then the best score remains whatever we already
            # achieved from the first i - 1 modules.
            skip_current: int = dp[i - 1]

            # Option 2: Take the current module.
            # If we take module i - 1, we are not allowed to take the
            # immediately previous module i - 2.
            #
            # Therefore, the best score in this case is:
            # best score from the first i - 2 modules + current module's points
            take_current: int = dp[i - 2] + points[i - 1]

            # Choose the better of the two options.
            # This is the core dynamic programming decision:
            # at each step, we keep the best possible answer seen so far.
            dp[i] = max(skip_current, take_current)

        # After processing all modules, dp[n] contains the answer for the
        # entire list.
        return dp[n]

    def max_points_optimized(self, points: List[int]) -> int:
        """
        Compute the maximum total points that can be earned without taking
        two adjacent modules, using optimized constant extra space.

        Args:
            points: A list where points[i] is the score of the i-th module.

        Returns:
            The maximum total points obtainable under the non-adjacent rule.

        Time complexity:
            O(n), where n is the number of modules.

        Space complexity:
            O(1), because we only keep track of two previous DP states.
        """
        # This method is an optimized version of the same dynamic programming idea.
        # Instead of storing the entire DP table, we only store the values we
        # actually need:
        #
        # - prev_two: best answer up to two modules ago
        # - prev_one: best answer up to the previous module
        #
        # This works because the recurrence only depends on the two previous states.

        # Best score considering zero modules.
        prev_two: int = 0

        # Best score considering modules processed so far.
        prev_one: int = 0

        # Go through each module score one by one.
        for score in points:
            # If we take the current module, we must add its score to the best
            # result from two modules ago.
            take_current: int = prev_two + score

            # If we skip the current module, we keep the best result from the
            # previous module.
            skip_current: int = prev_one

            # The new best result after considering this module is the better
            # of taking or skipping it.
            current: int = max(take_current, skip_current)

            # Shift the window forward:
            # - what used to be prev_one becomes prev_two
            # - current becomes the new prev_one
            prev_two = prev_one
            prev_one = current

        # After processing all modules, prev_one holds the final answer.
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    points1: List[int] = [4, 2, 7, 3, 9]
    result1: int = solution.max_points(points1)
    print(f"Input: {points1}")
    print(f"Maximum points: {result1}")
    print("Expected: 20")
    print()

    # Sample input 2 from the problem statement.
    points2: List[int] = [5, 1, 1, 5]
    result2: int = solution.max_points(points2)
    print(f"Input: {points2}")
    print(f"Maximum points: {result2}")
    print("Expected: 10")
    print()

    # Additional edge case: empty list.
    points3: List[int] = []
    result3: int = solution.max_points(points3)
    print(f"Input: {points3}")
    print(f"Maximum points: {result3}")
    print("Expected: 0")
    print()

    # Also demonstrate the optimized version produces the same results.
    print("Optimized version checks:")
    print(solution.max_points_optimized(points1))  # Expected: 20
    print(solution.max_points_optimized(points2))  # Expected: 10
    print(solution.max_points_optimized(points3))  # Expected: 0