"""
Title: Maximum Study Points Without Consecutive Hard Chapters

Problem Description:
You are preparing for an exam and have a list of chapters to study in order.
Each chapter gives you a certain number of study points if you choose to review it.
However, reviewing two adjacent chapters in the same session is too tiring because
both require full concentration. To keep your session manageable, you may not choose
two consecutive chapters.

Given an integer array points where points[i] is the number of study points earned
by reviewing chapter i, return the maximum total study points you can earn.

You may choose to skip any chapter, and you do not need to review the last chapter.
This is an optimization problem where each decision depends on previous choices,
making it a good fit for dynamic programming.

Constraints:
- 1 <= points.length <= 100
- 0 <= points[i] <= 1000

Example 1:
Input: points = [3, 2, 5, 10, 7]
Output: 15
Explanation: Review chapters with points 3, 5, and 7. Their indices are not adjacent,
so the total is 15.

Example 2:
Input: points = [2, 1, 4, 9]
Output: 11
Explanation: The best choice is to review chapters with points 2 and 9 for a total of 11.
Choosing 1 and 9 gives 10, and choosing 2 and 4 gives 6.

Goal:
Compute the maximum achievable total efficiently using dynamic programming.
"""

from typing import List


class Solution:
    def max_study_points(self, points: List[int]) -> int:
        """
        Compute the maximum study points that can be earned without choosing
        two consecutive chapters.

        Args:
            points: A list where points[i] is the study points from chapter i.

        Returns:
            The maximum total study points possible under the non-consecutive rule.

        Time Complexity:
            O(n), where n is the number of chapters, because we process each chapter once.

        Space Complexity:
            O(1), because we only store a constant amount of extra state.
        """
        # This problem is the classic "maximum sum of non-adjacent elements" problem.
        #
        # At every chapter, we have exactly two choices:
        # 1. Skip the current chapter
        # 2. Review the current chapter
        #
        # If we skip the current chapter:
        # - Our total remains whatever the best total was up to the previous chapter.
        #
        # If we review the current chapter:
        # - We are NOT allowed to review the previous chapter.
        # - So we add the current chapter's points to the best total from two chapters back.
        #
        # Therefore, for each position i:
        # best[i] = max(best[i - 1], best[i - 2] + points[i])
        #
        # Instead of storing the entire DP array, we only need the last two results:
        # - prev_one: best result up to the previous chapter
        # - prev_two: best result up to two chapters before
        #
        # This reduces space usage from O(n) to O(1).

        # Best total up to chapter i - 2.
        prev_two: int = 0

        # Best total up to chapter i - 1.
        prev_one: int = 0

        # Process each chapter in order.
        for chapter_points in points:
            # Option 1: skip this chapter.
            # Then the best total is simply the best total we already had
            # up to the previous chapter.
            skip_current: int = prev_one

            # Option 2: review this chapter.
            # Then we must add its points to the best total from two chapters ago,
            # because reviewing adjacent chapters is not allowed.
            take_current: int = prev_two + chapter_points

            # Choose the better of the two options.
            current_best: int = max(skip_current, take_current)

            # Move the DP window forward:
            # - The old prev_one becomes the new prev_two
            # - The newly computed current_best becomes the new prev_one
            prev_two = prev_one
            prev_one = current_best

        # After processing all chapters, prev_one stores the best possible answer.
        return prev_one

    def rob(self, points: List[int]) -> int:
        """
        Alias method that solves the same problem.

        Args:
            points: A list where points[i] is the study points from chapter i.

        Returns:
            The maximum total study points possible under the non-consecutive rule.

        Time Complexity:
            O(n), where n is the number of chapters.

        Space Complexity:
            O(1), using only constant extra memory.
        """
        return self.max_study_points(points)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    points1: List[int] = [3, 2, 5, 10, 7]
    result1: int = solution.max_study_points(points1)
    print(f"Input: {points1}")
    print(f"Output: {result1}")
    # Expected: 15
    # Trace:
    # - Choose 3 (index 0)
    # - Skip 2 (index 1)
    # - Choose 5 (index 2)
    # - Skip 10 (index 3) if choosing 7 later, or compare alternatives carefully
    # Best valid total is 3 + 5 + 7 = 15
    print("Expected: 15")
    print()

    # Sample input 2 from the problem statement.
    points2: List[int] = [2, 1, 4, 9]
    result2: int = solution.max_study_points(points2)
    print(f"Input: {points2}")
    print(f"Output: {result2}")
    # Expected: 11
    # Trace:
    # - Choose 2 (index 0)
    # - Skip 1 (index 1)
    # - Skip 4 (index 2)
    # - Choose 9 (index 3)
    # Total = 2 + 9 = 11
    print("Expected: 11")