"""
Title: Maximize Scenic Stops Within Fuel Budget

Problem Description:
You are planning a road trip along a straight highway. There are n scenic stops placed
in increasing order of distance from the start, given by the array miles, where
miles[i] is the mile marker of the i-th stop. Visiting a group of consecutive scenic
stops is only practical if the distance between the farthest and nearest stop in that
group does not exceed your extra sightseeing fuel budget budget.

Your task is to return the maximum number of consecutive scenic stops you can include
in a single sightseeing segment such that:

    miles[right] - miles[left] <= budget

for the chosen segment [left, right].

This models the idea that once you commit to a detour region, you only want stops
clustered closely enough together to avoid wasting fuel driving back and forth.
You may choose any contiguous block of stops from the sorted list, including a block
of length 1.

Constraints:
- 1 <= n <= 100000
- 0 <= miles[i] <= 10^9
- miles is sorted in non-decreasing order
- 0 <= budget <= 10^9

Examples:
1)
Input: miles = [2, 5, 6, 7, 11, 15], budget = 4
Output: 3

2)
Input: miles = [1, 1, 3, 4, 8, 9, 10], budget = 2
Output: 3
"""

from typing import List


class Solution:
    def max_scenic_stops(self, miles: List[int], budget: int) -> int:
        """
        Return the maximum size of a contiguous segment of scenic stops whose
        farthest and nearest mile markers differ by at most budget.

        Args:
            miles: A sorted list of mile markers for scenic stops.
            budget: The maximum allowed distance between the leftmost and rightmost
                stop in the chosen segment.

        Returns:
            The largest number of consecutive scenic stops that fit within the budget.

        Time complexity:
            O(n), because each pointer moves from left to right at most once.

        Space complexity:
            O(1), because only a few extra variables are used.
        """
        # We use the classic "sliding window" / "two pointers" technique.
        #
        # Why this works:
        # - The array is already sorted in non-decreasing order.
        # - For any current window [left, right], the smallest value is miles[left]
        #   and the largest value is miles[right].
        # - Therefore, the window is valid exactly when:
        #       miles[right] - miles[left] <= budget
        #
        # Since the array is sorted, if a window becomes invalid after moving right
        # forward, the only way to restore validity is to move left forward as well.
        #
        # This gives us an efficient O(n) solution.

        n: int = len(miles)

        # The left boundary of our current window.
        left: int = 0

        # This will store the best (largest) valid window length found so far.
        best: int = 0

        # Expand the window by moving the right pointer one step at a time.
        for right in range(n):
            # At this moment, we are trying to include miles[right] in the window.
            #
            # The window is now [left, right].
            # It may or may not be valid after adding this new stop.

            # If the current window violates the budget, shrink it from the left
            # until it becomes valid again.
            #
            # We use a while loop, not an if statement, because moving left once
            # may still leave the window invalid. We must keep shrinking until:
            #     miles[right] - miles[left] <= budget
            while miles[right] - miles[left] > budget:
                left += 1

            # Now the window [left, right] is guaranteed to be valid.
            #
            # Its length is:
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any
            # previously seen valid window.
            if current_length > best:
                best = current_length

        # By the end of the loop, best contains the maximum valid segment length.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    miles1: List[int] = [2, 5, 6, 7, 11, 15]
    budget1: int = 4
    result1: int = solution.max_scenic_stops(miles1, budget1)
    print("Example 1:")
    print(f"miles = {miles1}")
    print(f"budget = {budget1}")
    print(f"Output = {result1}")
    print("Expected = 3")
    print()

    # Example 2
    miles2: List[int] = [1, 1, 3, 4, 8, 9, 10]
    budget2: int = 2
    result2: int = solution.max_scenic_stops(miles2, budget2)
    print("Example 2:")
    print(f"miles = {miles2}")
    print(f"budget = {budget2}")
    print(f"Output = {result2}")
    print("Expected = 3")
    print()

    # Additional beginner-friendly checks
    extra_tests = [
        ([5], 0, 1),                  # Single stop always works
        ([1, 2, 3, 4], 10, 4),        # Entire array fits
        ([1, 10, 20, 30], 0, 1),      # Only identical values could pair; here max is 1
        ([1, 1, 1, 1], 0, 4),         # All equal, all fit
        ([0, 2, 4, 6, 8], 3, 2),      # Best windows like [0,2] or [2,4]
    ]

    print("Additional Tests:")
    for miles, budget, expected in extra_tests:
        output = solution.max_scenic_stops(miles, budget)
        print(f"miles = {miles}, budget = {budget}, output = {output}, expected = {expected}")