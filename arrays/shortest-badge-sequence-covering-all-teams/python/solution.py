"""
Title: Shortest Badge Sequence Covering All Teams

Problem Description:
A company records the order of employee badge scans during a large conference.
Each scan is represented by an integer team ID in the array scans, where scans[i]
is the team of the i-th person who entered a room. You are also given an integer
m representing the total number of distinct teams, labeled from 1 to m.

Find the length of the shortest contiguous subarray of scans that contains at
least one badge scan from every team 1 through m. If no such subarray exists,
return -1.

This problem models finding the smallest time window in which all required groups
were represented. The array may contain repeated team IDs, and some teams may
appear many times while others appear rarely. The solution should be efficient
enough for large inputs.

Return only the minimum length, not the subarray itself.

Constraints:
- 1 <= scans.length <= 200000
- 1 <= m <= 100000
- 1 <= scans[i] <= m
- The input array may be unsorted
- If one or more team IDs from 1 to m never appear in scans, the answer is -1

Example 1:
Input: scans = [2, 1, 3, 2, 4, 1, 3], m = 4
Output: 4
Explanation: The shortest valid subarray is [1, 3, 2, 4], which contains teams
1, 2, 3, and 4.

Example 2:
Input: scans = [1, 2, 2, 1, 3], m = 4
Output: -1
Explanation: Team 4 never appears, so it is impossible to form a contiguous
subarray containing all teams.
"""

from typing import List


class Solution:
    def shortest_badge_sequence(self, scans: List[int], m: int) -> int:
        """
        Find the length of the shortest contiguous subarray that contains
        every team ID from 1 through m at least once.

        Args:
            scans: List of badge scan team IDs.
            m: Total number of required teams, labeled from 1 to m.

        Returns:
            The minimum length of a contiguous subarray covering all teams,
            or -1 if such a subarray does not exist.

        Time complexity:
            O(n), where n is the length of scans, because each pointer moves
            across the array at most once.

        Space complexity:
            O(m), for storing frequency counts of teams currently inside
            the sliding window.
        """
        # If there are fewer total scans than required distinct teams,
        # it is impossible to cover all teams even in the best case.
        if len(scans) < m:
            return -1

        # We use a frequency array to count how many times each team appears
        # inside the current sliding window.
        #
        # Why an array instead of a dictionary?
        # - Team IDs are guaranteed to be in the range 1..m.
        # - An array gives O(1) access with very low overhead.
        # - This is efficient and simple for beginners to follow.
        #
        # Index 0 is unused so that team ID x maps directly to counts[x].
        counts: List[int] = [0] * (m + 1)

        # 'covered_teams' tracks how many distinct required teams are currently
        # present in the window at least once.
        #
        # Example:
        # If the current window contains teams {1, 2, 4}, then covered_teams = 3.
        covered_teams: int = 0

        # 'left' is the left boundary of the sliding window.
        left: int = 0

        # 'best_length' stores the shortest valid window length found so far.
        # We start with infinity so any real valid window will be smaller.
        best_length: int = float("inf")

        # Expand the window by moving 'right' from left to right across the array.
        for right, team in enumerate(scans):
            # Add the current team at position 'right' into the window.
            counts[team] += 1

            # If this team count became 1, that means this team was not previously
            # present in the window and is now newly covered.
            if counts[team] == 1:
                covered_teams += 1

            # Once the window covers all m teams, we try to shrink it from the left
            # as much as possible while still remaining valid.
            #
            # This is the key sliding window idea:
            # - Expand until valid
            # - Shrink while valid
            # - Record the smallest valid window seen
            while covered_teams == m:
                # Current window is scans[left:right+1]
                current_length: int = right - left + 1

                # Update the best answer if this valid window is smaller.
                if current_length < best_length:
                    best_length = current_length

                # We now try to remove scans[left] from the window to see whether
                # we can make the window even smaller.
                left_team: int = scans[left]
                counts[left_team] -= 1

                # If the count becomes 0, then removing this team means the window
                # no longer contains that required team, so the window stops being valid.
                if counts[left_team] == 0:
                    covered_teams -= 1

                # Move the left boundary one step to the right.
                left += 1

        # If best_length was never updated, then no valid window existed.
        if best_length == float("inf"):
            return -1

        return best_length


if __name__ == "__main__":
    solution = Solution()

    scans1: List[int] = [2, 1, 3, 2, 4, 1, 3]
    m1: int = 4
    result1: int = solution.shortest_badge_sequence(scans1, m1)
    print(result1)  # Expected: 4

    scans2: List[int] = [1, 2, 2, 1, 3]
    m2: int = 4
    result2: int = solution.shortest_badge_sequence(scans2, m2)
    print(result2)  # Expected: -1