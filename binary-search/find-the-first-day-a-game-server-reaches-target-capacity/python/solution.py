"""
Title: Find the First Day a Game Server Reaches Target Capacity

Problem Description:
A gaming platform records the total number of active players on a server at the end
of each day. Because the game is steadily growing, the daily totals are sorted in
non-decreasing order. You are given an integer array `players`, where `players[i]`
is the active player count on day `i`, and an integer `target` representing a
required server capacity milestone.

Your task is to return the index of the first day on which the server reaches or
exceeds `target` players. If the server never reaches `target`, return `-1`.

This problem should be solved efficiently using binary search rather than scanning
from left to right. Since the array is already sorted, you can repeatedly discard
half of the remaining search space.

Return the smallest index `i` such that `players[i] >= target`. If no such index
exists, return `-1`.

Constraints:
- 1 <= players.length <= 100000
- 0 <= players[i] <= 1000000000
- players is sorted in non-decreasing order
- 0 <= target <= 1000000000

Example 1:
Input: players = [120, 150, 150, 210, 260], target = 151
Output: 3
Explanation: Day 3 is the first day with at least 151 active players, since
players[3] = 210.

Example 2:
Input: players = [40, 80, 80, 95], target = 100
Output: -1
Explanation: The server never reaches 100 active players, so the answer is -1.
"""

from typing import List


class Solution:
    def first_day_reaching_target(self, players: List[int], target: int) -> int:
        """
        Find the smallest index where players[index] is greater than or equal to target.

        This uses binary search on the sorted `players` array to efficiently locate
        the first valid day instead of checking every day one by one.

        Args:
            players: A sorted list of daily active player counts.
            target: The required player count milestone.

        Returns:
            The smallest index i such that players[i] >= target, or -1 if no such
            index exists.

        Time complexity:
            O(log n), where n is the length of players.

        Space complexity:
            O(1), because only a constant amount of extra space is used.
        """
        # We will search within the full valid index range of the array.
        # `left` points to the beginning of the current search space.
        # `right` points to the end of the current search space.
        left: int = 0
        right: int = len(players) - 1

        # This variable will store the best answer found so far.
        # We start with -1 to mean "not found yet".
        answer: int = -1

        # Continue searching while there is still a valid range to inspect.
        # The condition `left <= right` means there is at least one candidate index left.
        while left <= right:
            # Compute the middle index of the current search range.
            # We use this form instead of (left + right) // 2 as a standard safe pattern.
            mid: int = left + (right - left) // 2

            # If the value at `mid` is large enough, then `mid` is a valid candidate.
            # However, it may not be the FIRST such index.
            # So:
            # 1. Save `mid` as the current best answer.
            # 2. Move left to continue searching for an earlier valid index.
            if players[mid] >= target:
                answer = mid
                right = mid - 1
            else:
                # If players[mid] is still smaller than target, then every index
                # at or before `mid` is also not good enough because the array is sorted.
                # Therefore, we discard the left half including `mid`
                # and search only in the right half.
                left = mid + 1

        # If we found at least one valid index, `answer` holds the first one.
        # Otherwise it remains -1.
        return answer

    def search_insert_style(self, players: List[int], target: int) -> int:
        """
        Alternative binary search implementation that finds the leftmost insertion point
        for target, then checks whether that position is within bounds.

        Args:
            players: A sorted list of daily active player counts.
            target: The required player count milestone.

        Returns:
            The smallest index i such that players[i] >= target, or -1 if no such
            index exists.

        Time complexity:
            O(log n), where n is the length of players.

        Space complexity:
            O(1), because only a constant amount of extra space is used.
        """
        # In this version, we use a half-open interval [left, right),
        # where `left` is inclusive and `right` is exclusive.
        left: int = 0
        right: int = len(players)

        # The goal is to find the first position where `target` could be inserted
        # without breaking the sorted order. That position is exactly the first index
        # where players[index] >= target.
        while left < right:
            mid: int = left + (right - left) // 2

            # If the middle value is less than target, then this position cannot be
            # the answer, and neither can anything to its left.
            if players[mid] < target:
                left = mid + 1
            else:
                # Otherwise, mid might be the answer, so keep it in the search range
                # and continue searching to the left.
                right = mid

        # After the loop, `left` is the first index where players[left] >= target
        # would be true, if such an index exists.
        if left == len(players):
            return -1

        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # players = [120, 150, 150, 210, 260], target = 151
    # Expected output: 3
    players1: List[int] = [120, 150, 150, 210, 260]
    target1: int = 151
    result1: int = solution.first_day_reaching_target(players1, target1)
    print(f"Example 1 result: {result1}")  # Expected: 3

    # Example 2:
    # players = [40, 80, 80, 95], target = 100
    # Expected output: -1
    players2: List[int] = [40, 80, 80, 95]
    target2: int = 100
    result2: int = solution.first_day_reaching_target(players2, target2)
    print(f"Example 2 result: {result2}")  # Expected: -1

    # Additional beginner-friendly checks:
    players3: List[int] = [10, 20, 30, 40]
    target3: int = 10
    result3: int = solution.first_day_reaching_target(players3, target3)
    print(f"Additional test 1 result: {result3}")  # Expected: 0

    players4: List[int] = [5, 5, 5, 5]
    target4: int = 5
    result4: int = solution.first_day_reaching_target(players4, target4)
    print(f"Additional test 2 result: {result4}")  # Expected: 0

    players5: List[int] = [1, 3, 7, 9, 12]
    target5: int = 8
    result5: int = solution.first_day_reaching_target(players5, target5)
    print(f"Additional test 3 result: {result5}")  # Expected: 3

    # Also demonstrate the alternative method produces the same answers.
    print("Alternative method checks:")
    print(solution.search_insert_style(players1, target1))  # Expected: 3
    print(solution.search_insert_style(players2, target2))  # Expected: -1