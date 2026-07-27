"""
Title: Maximum Loyalty Points from Skipping Consecutive Cafes

Problem Description:
A commuter passes a row of cafes on the way to work. Each cafe offers a certain
number of loyalty points if visited that day. However, visiting two neighboring
cafes on the same trip takes too much time, so the commuter is not allowed to
collect points from two consecutive cafes.

You are given an integer array `points` where `points[i]` is the number of
loyalty points available at the `i`-th cafe. Return the maximum total number of
points the commuter can collect while following the rule that no two chosen
cafes are adjacent.

This is a classic decision problem with a simple dynamic programming pattern:
at each cafe, either skip it and keep the best total so far, or visit it and
add its points to the best total from two cafes earlier.

Constraints:
- 1 <= points.length <= 100
- 0 <= points[i] <= 1000

Example 1:
Input: points = [5, 1, 2, 10]
Output: 15
Explanation: Visit cafe 0 and cafe 3 for a total of 5 + 10 = 15.
Visiting cafes 2 and 3 together is not allowed because they are adjacent.

Example 2:
Input: points = [2, 7, 9, 3, 1]
Output: 12
Explanation: The best choice is to visit cafes 0, 2, and 4.
The total is 2 + 9 + 1 = 12.
"""

from typing import List


class Solution:
    def max_loyalty_points(self, points: List[int]) -> int:
        """
        Compute the maximum loyalty points that can be collected without
        choosing two adjacent cafes.

        Args:
            points: A list of non-negative integers where points[i] represents
                the loyalty points available at the i-th cafe.

        Returns:
            The maximum total points obtainable while never selecting two
            consecutive cafes.

        Time complexity:
            O(n), where n is the number of cafes, because we process each cafe once.

        Space complexity:
            O(1), because we only store a constant amount of dynamic programming state.
        """
        # We will solve this using dynamic programming with space optimization.
        #
        # Core idea:
        # For each cafe, we have exactly two choices:
        #
        # 1. Skip the current cafe:
        #    - Then our best total stays the same as the best answer up to the
        #      previous cafe.
        #
        # 2. Visit the current cafe:
        #    - Then we are NOT allowed to visit the previous cafe.
        #    - So we add the current cafe's points to the best answer up to
        #      two cafes earlier.
        #
        # This gives the recurrence:
        # best[i] = max(best[i - 1], best[i - 2] + points[i])
        #
        # Instead of storing the entire DP array, we only keep the last two
        # values because each new state depends only on those two.

        # This variable will represent:
        # "the best total points we can collect up to the cafe two positions back"
        #
        # At the start, before processing any cafe, that value is 0 because
        # there are no cafes yet.
        prev_two: int = 0

        # This variable will represent:
        # "the best total points we can collect up to the previous cafe"
        #
        # Initially, before processing any cafe, this is also 0.
        prev_one: int = 0

        # We now walk through each cafe from left to right.
        # For every cafe, we decide whether taking it or skipping it gives
        # a better total.
        for current_points in points:
            # Option 1: Skip the current cafe.
            #
            # If we skip it, then our best total does not change from the
            # best answer we already had up to the previous cafe.
            skip_current: int = prev_one

            # Option 2: Visit the current cafe.
            #
            # If we visit it, we must avoid the immediately previous cafe.
            # Therefore, we add the current cafe's points to the best total
            # from two cafes back.
            take_current: int = prev_two + current_points

            # The best answer including consideration of this cafe is whichever
            # of the two options gives a larger total.
            current_best: int = max(skip_current, take_current)

            # Now we shift our DP window forward:
            #
            # - The old "prev_one" becomes the new "prev_two"
            # - The newly computed "current_best" becomes the new "prev_one"
            #
            # This keeps our rolling state correct for the next iteration.
            prev_two = prev_one
            prev_one = current_best

        # After processing all cafes, prev_one stores the best possible answer
        # for the entire list.
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    sample_points_1: List[int] = [5, 1, 2, 10]
    result_1: int = solution.max_loyalty_points(sample_points_1)
    print(f"Input: {sample_points_1}")
    print(f"Maximum loyalty points: {result_1}")
    print("Expected: 15")
    print()

    sample_points_2: List[int] = [2, 7, 9, 3, 1]
    result_2: int = solution.max_loyalty_points(sample_points_2)
    print(f"Input: {sample_points_2}")
    print(f"Maximum loyalty points: {result_2}")
    print("Expected: 12")
    print()

    extra_sample_1: List[int] = [4]
    result_3: int = solution.max_loyalty_points(extra_sample_1)
    print(f"Input: {extra_sample_1}")
    print(f"Maximum loyalty points: {result_3}")
    print("Expected: 4")
    print()

    extra_sample_2: List[int] = [0, 0, 0, 0]
    result_4: int = solution.max_loyalty_points(extra_sample_2)
    print(f"Input: {extra_sample_2}")
    print(f"Maximum loyalty points: {result_4}")
    print("Expected: 0")