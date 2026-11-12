"""
Title: Longest Badge Segment With At Most K Team Switches

Problem Description:
You are given an array teams where teams[i] is the team ID shown on the i-th badge scan
in chronological order. A security analyst wants to find the longest contiguous time
segment that is operationally stable, where the number of times the team ID changes
between adjacent scans is at most k.

Formally, for a subarray teams[l...r], count how many indices i satisfy l < i <= r
and teams[i] != teams[i - 1]. This count is called the number of team switches inside
the segment. Your task is to return the maximum possible length of a contiguous segment
whose number of team switches is at most k.

This is not the same as counting distinct team IDs. For example, the segment
[4, 4, 7, 7, 4] has 2 team switches, even though it contains only 2 distinct values.

Return the length of the longest valid segment.

Constraints:
- 1 <= teams.length <= 200000
- 0 <= k < teams.length
- 1 <= teams[i] <= 1000000000

Example 1:
Input: teams = [5,5,2,2,2,9,9,5], k = 2
Output: 7

Example 2:
Input: teams = [1,3,1,3,1,3], k = 3
Output: 4

A solution that runs in linear time is expected.
"""

from typing import List


class Solution:
    def longest_stable_segment(self, teams: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous segment with at most k team switches.

        A "team switch" happens inside a segment whenever two adjacent values differ.
        We use a sliding window to maintain the longest valid segment while scanning
        the array from left to right.

        Args:
            teams: List of team IDs in chronological order.
            k: Maximum allowed number of team switches inside the segment.

        Returns:
            The length of the longest contiguous valid segment.

        Time complexity:
            O(n), where n is the length of teams, because each pointer moves at most n times.

        Space complexity:
            O(1), because we use only a few variables regardless of input size.
        """
        n: int = len(teams)

        # Edge case:
        # If the array has only one element, the answer must be 1 because any single
        # element segment has zero switches.
        if n == 1:
            return 1

        # We will use the classic sliding window pattern:
        #
        # - "left" marks the start of the current window.
        # - "right" marks the end of the current window as we expand it.
        # - "switches" stores how many adjacent changes exist inside teams[left:right+1].
        #
        # Important observation:
        # A switch is determined by an adjacent pair (i-1, i) where teams[i] != teams[i-1].
        # So when we extend the window to the right by including index "right",
        # we only need to check whether teams[right] != teams[right - 1].
        #
        # Similarly, when we move "left" forward, we may remove exactly one adjacent pair
        # from the window: the pair (left, left + 1). If those two values were different,
        # then the switch count decreases by 1.
        left: int = 0
        switches: int = 0
        best: int = 1

        # Move the right boundary one step at a time.
        for right in range(n):
            # If right > 0, then the new element teams[right] forms an adjacent pair
            # with teams[right - 1]. If they differ, we have introduced one additional
            # switch into the current window.
            #
            # Why this works:
            # The window is always contiguous. The only new adjacency created when
            # extending the window is between the previous last element and the new one.
            if right > 0 and teams[right] != teams[right - 1]:
                switches += 1

            # If the number of switches is too large, the window is invalid.
            # We must shrink it from the left until it becomes valid again.
            #
            # While shrinking:
            # - The pair that may leave the window is (left, left + 1).
            # - If teams[left] != teams[left + 1], then that pair contributed one switch.
            #   Once left moves forward, that pair is no longer fully inside the window,
            #   so we subtract 1 from switches.
            #
            # We repeat until switches <= k.
            while switches > k:
                # Before moving left, check whether the adjacent pair at the left edge
                # is a switch that will be removed from the window.
                if left + 1 <= right and teams[left] != teams[left + 1]:
                    switches -= 1

                # Actually shrink the window.
                left += 1

            # At this point, the window teams[left:right+1] is valid:
            # it contains at most k switches.
            current_length: int = right - left + 1

            # Update the best answer found so far.
            if current_length > best:
                best = current_length

        return best

    def longestBadgeSegmentWithAtMostKTeamSwitches(self, teams: List[int], k: int) -> int:
        """
        Wrapper method matching the problem statement naming style.

        Args:
            teams: List of team IDs in chronological order.
            k: Maximum allowed number of team switches inside the segment.

        Returns:
            The length of the longest contiguous valid segment.

        Time complexity:
            O(n), where n is the length of teams.

        Space complexity:
            O(1).
        """
        return self.longest_stable_segment(teams, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # teams = [5,5,2,2,2,9,9,5], k = 2
    # Switches across the full array:
    # 5->5 (no), 5->2 (yes), 2->2 (no), 2->2 (no), 2->9 (yes), 9->9 (no), 9->5 (yes)
    # Total = 3 switches in the full array, so length 8 is invalid.
    # The segment [5,5,2,2,2,9,9] has exactly 2 switches and length 7, so answer is 7.
    teams1: List[int] = [5, 5, 2, 2, 2, 9, 9, 5]
    k1: int = 2
    result1: int = solution.longestBadgeSegmentWithAtMostKTeamSwitches(teams1, k1)
    print("Example 1 Result:", result1)  # Expected: 7

    # Example 2:
    # teams = [1,3,1,3,1,3], k = 3
    # Every adjacent pair is a switch.
    # A segment of length L has L-1 switches here.
    # So we need L-1 <= 3 => L <= 4.
    # Therefore the answer is 4.
    teams2: List[int] = [1, 3, 1, 3, 1, 3]
    k2: int = 3
    result2: int = solution.longestBadgeSegmentWithAtMostKTeamSwitches(teams2, k2)
    print("Example 2 Result:", result2)  # Expected: 4

    # Additional beginner-friendly checks:

    # Single element: no switches possible.
    teams3: List[int] = [42]
    k3: int = 0
    result3: int = solution.longestBadgeSegmentWithAtMostKTeamSwitches(teams3, k3)
    print("Single Element Result:", result3)  # Expected: 1

    # No switches allowed, so we want the longest run of equal values.
    teams4: List[int] = [7, 7, 7, 2, 2, 3, 3, 3, 3]
    k4: int = 0
    result4: int = solution.longestBadgeSegmentWithAtMostKTeamSwitches(teams4, k4)
    print("No Switches Allowed Result:", result4)  # Expected: 4

    # Entire array valid if k is large enough.
    teams5: List[int] = [4, 4, 7, 7, 4]
    k5: int = 2
    result5: int = solution.longestBadgeSegmentWithAtMostKTeamSwitches(teams5, k5)
    print("Whole Array Valid Result:", result5)  # Expected: 5