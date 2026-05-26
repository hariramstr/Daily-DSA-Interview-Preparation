"""
Title: Squeeze Water Between Ice Walls
Difficulty: Easy
Topic: Two Pointers

Problem Description:
You are given an array `heights` representing the heights of ice walls standing upright in a row.
A bucket of water is placed between any two walls at positions `i` and `j` (where `i < j`).
The amount of water the bucket can hold is determined by the shorter of the two walls multiplied
by the number of gaps between them: min(heights[i], heights[j]) * (j - i).

Your task is to find the maximum amount of water that can be held between any two walls.

Note: This problem is about choosing the optimal pair of walls, not filling every gap between them.

Constraints:
- 2 <= heights.length <= 10^5
- 1 <= heights[i] <= 10^4

Example 1:
Input: heights = [2, 7, 4, 1, 6, 3]
Output: 18
Explanation: Using walls at index 1 (height 7) and index 4 (height 6),
             water = min(7, 6) * (4 - 1) = 6 * 3 = 18.

Example 2:
Input: heights = [3, 1, 2, 4, 5]
Output: 12
Explanation: Using walls at index 0 (height 3) and index 4 (height 5),
             water = min(3, 5) * (4 - 0) = 3 * 4 = 12.
"""

from typing import List


class Solution:
    def max_water(self, heights: List[int]) -> int:
        """
        Find the maximum amount of water that can be held between any two walls.

        Uses a two-pointer approach: start with pointers at both ends of the array.
        At each step, calculate the water between the two pointers, update the maximum,
        then move the pointer pointing to the shorter wall inward. This greedy strategy
        works because moving the taller wall inward can only decrease or maintain the
        width while the height is still limited by the shorter wall — so we gain nothing.
        Moving the shorter wall inward gives us a chance to find a taller wall that
        might increase the total water.

        Args:
            heights (List[int]): A list of integers representing wall heights.

        Returns:
            int: The maximum amount of water that can be held between any two walls.

        Time Complexity:  O(n) — We traverse the array at most once with two pointers.
        Space Complexity: O(1) — Only a constant number of variables are used.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Initialize two pointers at the far left and far right ends.
        # -----------------------------------------------------------------------
        # We start with the widest possible container. The idea is that we want
        # to maximize both width (j - i) and height (min of the two walls).
        # Starting from both ends gives us the maximum possible width initially.
        left = 0                    # Left pointer starts at index 0
        right = len(heights) - 1   # Right pointer starts at the last index

        # -----------------------------------------------------------------------
        # STEP 2: Initialize the variable to track the maximum water found so far.
        # -----------------------------------------------------------------------
        max_water = 0  # We haven't examined any pair yet, so start at 0

        # -----------------------------------------------------------------------
        # STEP 3: Loop until the two pointers meet.
        # -----------------------------------------------------------------------
        # As long as left is strictly less than right, there is a valid pair of
        # walls to consider (a container needs at least two distinct positions).
        while left < right:

            # -------------------------------------------------------------------
            # STEP 3a: Calculate the height of the current container.
            # -------------------------------------------------------------------
            # The water level is limited by the SHORTER of the two walls.
            # Even if one wall is very tall, water will spill over the shorter one.
            current_height = min(heights[left], heights[right])

            # -------------------------------------------------------------------
            # STEP 3b: Calculate the width of the current container.
            # -------------------------------------------------------------------
            # The width is simply the distance (number of gaps) between the two walls.
            current_width = right - left

            # -------------------------------------------------------------------
            # STEP 3c: Calculate the water held by this pair of walls.
            # -------------------------------------------------------------------
            # Water = height * width, as defined in the problem.
            current_water = current_height * current_width

            # -------------------------------------------------------------------
            # STEP 3d: Update the maximum water if this pair holds more water.
            # -------------------------------------------------------------------
            # We keep a running maximum across all pairs we've examined.
            max_water = max(max_water, current_water)

            # -------------------------------------------------------------------
            # STEP 3e: Move the pointer pointing to the SHORTER wall inward.
            # -------------------------------------------------------------------
            # WHY? Because the water is limited by the shorter wall. If we move
            # the taller wall inward, we definitely reduce the width AND the height
            # is still capped by the same (or shorter) wall — so we can't do better.
            # By moving the shorter wall inward, we give ourselves a chance to find
            # a taller wall that could increase the water amount despite the reduced width.
            #
            # If both walls are equal height, it doesn't matter which pointer we move;
            # here we move the left pointer (an arbitrary but consistent choice).
            if heights[left] <= heights[right]:
                # The left wall is shorter (or equal), so move left pointer right
                left += 1
            else:
                # The right wall is shorter, so move right pointer left
                right -= 1

        # -----------------------------------------------------------------------
        # STEP 4: Return the maximum water found after examining all useful pairs.
        # -----------------------------------------------------------------------
        return max_water


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
# Example 1: heights = [2, 7, 4, 1, 6, 3]
#
#   Iteration 1: left=0 (h=2), right=5 (h=3)
#     water = min(2,3) * (5-0) = 2 * 5 = 10, max_water=10
#     heights[left]=2 <= heights[right]=3 → move left to 1
#
#   Iteration 2: left=1 (h=7), right=5 (h=3)
#     water = min(7,3) * (5-1) = 3 * 4 = 12, max_water=12
#     heights[left]=7 > heights[right]=3 → move right to 4
#
#   Iteration 3: left=1 (h=7), right=4 (h=6)
#     water = min(7,6) * (4-1) = 6 * 3 = 18, max_water=18
#     heights[left]=7 > heights[right]=6 → move right to 3
#
#   Iteration 4: left=1 (h=7), right=3 (h=1)
#     water = min(7,1) * (3-1) = 1 * 2 = 2, max_water=18
#     heights[left]=7 > heights[right]=1 → move right to 2
#
#   Iteration 5: left=1 (h=7), right=2 (h=4)
#     water = min(7,4) * (2-1) = 4 * 1 = 4, max_water=18
#     heights[left]=7 > heights[right]=4 → move right to 1
#
#   Now left=1 == right=1 → loop ends
#   Result: 18 ✓ (matches expected output)
#
# -----------------------------------------------------------------------------
# Example 2: heights = [3, 1, 2, 4, 5]
#
#   Iteration 1: left=0 (h=3), right=4 (h=5)
#     water = min(3,5) * (4-0) = 3 * 4 = 12, max_water=12
#     heights[left]=3 <= heights[right]=5 → move left to 1
#
#   Iteration 2: left=1 (h=1), right=4 (h=5)
#     water = min(1,5) * (4-1) = 1 * 3 = 3, max_water=12
#     heights[left]=1 <= heights[right]=5 → move left to 2
#
#   Iteration 3: left=2 (h=2), right=4 (h=5)
#     water = min(2,5) * (4-2) = 2 * 2 = 4, max_water=12
#     heights[left]=2 <= heights[right]=5 → move left to 3
#
#   Iteration 4: left=3 (h=4), right=4 (h=5)
#     water = min(4,5) * (4-3) = 4 * 1 = 4, max_water=12
#     heights[left]=4 <= heights[right]=5 → move left to 4
#
#   Now left=4 == right=4 → loop ends
#   Result: 12 ✓ (matches expected output)
# =============================================================================


if __name__ == "__main__":
    # Create an instance of the Solution class
    solution = Solution()

    # -------------------------------------------------------------------------
    # Test Case 1 (from problem description)
    # -------------------------------------------------------------------------
    heights1 = [2, 7, 4, 1, 6, 3]
    result1 = solution.max_water(heights1)
    print("=" * 50)
    print("Test Case 1:")
    print(f"  Input:    heights = {heights1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: 18")
    print(f"  Pass:     {result1 == 18}")

    # -------------------------------------------------------------------------
    # Test Case 2 (from problem description)
    # -------------------------------------------------------------------------
    heights2 = [3, 1, 2, 4, 5]
    result2 = solution.max_water(heights2)
    print("=" * 50)
    print("Test Case 2:")
    print(f"  Input:    heights = {heights2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: 12")
    print(f"  Pass:     {result2 == 12}")

    # -------------------------------------------------------------------------
    # Additional Test Case 3: Two walls only (minimum input size)
    # -------------------------------------------------------------------------
    heights3 = [5, 3]
    result3 = solution.max_water(heights3)
    print("=" * 50)
    print("Test Case 3 (two walls only):")
    print(f"  Input:    heights = {heights3}")
    print(f"  Output:   {result3}")
    # water = min(5, 3) * (1 - 0) = 3 * 1 = 3
    print(f"  Expected: 3")
    print(f"  Pass:     {result3 == 3}")

    # -------------------------------------------------------------------------
    # Additional Test Case 4: All walls same height
    # -------------------------------------------------------------------------
    heights4 = [4, 4, 4, 4]
    result4 = solution.max_water(heights4)
    print("=" * 50)
    print("Test Case 4 (all same height):")
    print(f"  Input:    heights = {heights4}")
    print(f"  Output:   {result4}")
    # Best pair: index 0 and index 3 → min(4,4) * 3 = 12
    print(f"  Expected: 12")
    print(f"  Pass:     {result4 == 12}")

    # -------------------------------------------------------------------------
    # Additional Test Case 5: Increasing heights
    # -------------------------------------------------------------------------
    heights5 = [1, 2, 3, 4, 5]
    result5 = solution.max_water(heights5)
    print("=" * 50)
    print("Test Case 5 (increasing heights):")
    print(f"  Input:    heights = {heights5}")
    print(f"  Output:   {result5}")
    # Best pair: index 0 (h=1) and index 4 (h=5) → min(1,5)*4=4
    #            index 1 (h=2) and index 4 (h=5) → min(2,5)*3=6
    #            index 2 (h=3) and index 4 (h=5) → min(3,5)*2=6
    #            index 3 (h=4) and index 4 (h=5) → min(4,5)*1=4
    # Maximum = 6
    print(f"  Expected: 6")
    print(f"  Pass:     {result5 == 6}")

    print("=" * 50)