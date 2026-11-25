"""
Title: Minimum Cost to Curate a Museum Exhibit Path

Problem Description:
A museum is preparing a guided exhibit path through a linear hallway of display rooms.
The hallway has n rooms numbered from 0 to n - 1, and the guide must start in room 0
and finish in room n - 1. From room i, the guide may move to room i + 1 or skip ahead
to room i + 2, as long as the destination room exists.

Each room i has a curation cost cost[i], representing the effort required to prepare
that room for the tour if the guide enters it. However, some rooms are marked as
premium rooms. If two premium rooms are visited consecutively in the tour path, an
additional transition fee penalty is charged because the guide team must perform a
special reset between them.

You are given:
- an integer array cost where cost[i] is the curation cost of room i,
- a binary array premium where premium[i] is 1 if room i is premium, otherwise 0,
- an integer penalty.

Return the minimum total cost to create a valid tour path from room 0 to room n - 1.
The total cost includes the cost of every visited room, plus penalty for every
consecutive pair of visited rooms in the chosen path where both rooms are premium.

A pair of visited rooms is consecutive if they are adjacent in the chosen path, even
if their indices differ by 2 due to a skip.

Constraints:
- 2 <= n <= 100000
- cost.length == premium.length == n
- 0 <= cost[i] <= 100000
- premium[i] is either 0 or 1
- 0 <= penalty <= 100000

Dynamic Programming Idea:
Let dp[i] be the minimum total cost needed to reach room i.
To arrive at room i, the previous visited room can only be:
- i - 1
- i - 2

So:
dp[i] = cost[i] + min(
    dp[i - 1] + extra_penalty_if_both_premium(i - 1, i),
    dp[i - 2] + extra_penalty_if_both_premium(i - 2, i)
)

Base cases:
- dp[0] = cost[0]
- dp[1] = cost[1] + dp[0] + possible penalty between 0 and 1

This yields an O(n) time solution.
"""

from typing import List


class Solution:
    def _transition_penalty(
        self,
        prev_room: int,
        curr_room: int,
        premium: List[int],
        penalty: int,
    ) -> int:
        """
        Compute the extra penalty for moving from one visited room to the next.

        Args:
            prev_room: Index of the previously visited room.
            curr_room: Index of the current room being entered.
            premium: Binary list indicating whether each room is premium.
            penalty: Extra cost charged when two consecutive visited rooms are premium.

        Returns:
            The penalty value if both rooms are premium, otherwise 0.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return penalty if premium[prev_room] == 1 and premium[curr_room] == 1 else 0

    def min_cost_exhibit_path(
        self,
        cost: List[int],
        premium: List[int],
        penalty: int,
    ) -> int:
        """
        Return the minimum total cost to travel from room 0 to room n - 1.

        The guide may move from room i to room i + 1 or i + 2.
        Every visited room contributes its room cost.
        Additionally, every consecutive visited pair contributes the given penalty
        if both rooms in that pair are premium.

        Args:
            cost: List where cost[i] is the curation cost of room i.
            premium: List where premium[i] is 1 if room i is premium, else 0.
            penalty: Extra fee for a consecutive premium-to-premium transition.

        Returns:
            The minimum total cost of a valid path from room 0 to room n - 1.

        Time complexity:
            O(n), because each room is processed once and each step does O(1) work.

        Space complexity:
            O(n), due to the dynamic programming array.
        """
        n: int = len(cost)

        # dp[i] will store the minimum total cost required to reach room i.
        #
        # Why this works:
        # To reach room i, the guide can only come from room i - 1 or room i - 2.
        # That means the best way to reach i must be built from one of those two
        # already-solved subproblems. This is the classic dynamic programming pattern:
        # "optimal solution to current state depends on optimal solutions to smaller states."
        dp: List[int] = [0] * n

        # Base case for room 0:
        # The path must start at room 0, so we must pay its curation cost.
        # There is no previous room, so no transition penalty applies here.
        dp[0] = cost[0]

        # Base case for room 1:
        # Since the guide starts at room 0, the only way to reach room 1 is:
        # 0 -> 1
        #
        # So total cost is:
        # cost to reach room 0
        # + cost of room 1
        # + possible premium-to-premium penalty between rooms 0 and 1
        dp[1] = (
            dp[0]
            + cost[1]
            + self._transition_penalty(0, 1, premium, penalty)
        )

        # Process rooms from left to right, starting at room 2.
        # For each room i, there are exactly two legal ways to enter:
        #   1) from room i - 1
        #   2) from room i - 2
        #
        # We compute both candidate costs and keep the smaller one.
        for i in range(2, n):
            # Option 1: arrive from room i - 1
            #
            # Total cost for this option:
            # - best cost to reach room i - 1
            # - plus cost of entering room i
            # - plus penalty if both (i - 1) and i are premium
            from_prev_one: int = (
                dp[i - 1]
                + cost[i]
                + self._transition_penalty(i - 1, i, premium, penalty)
            )

            # Option 2: arrive from room i - 2
            #
            # Even though the indices differ by 2, these two rooms become consecutive
            # in the chosen path because room i - 1 is skipped. Therefore, the same
            # premium-to-premium penalty rule still applies to the pair (i - 2, i).
            from_prev_two: int = (
                dp[i - 2]
                + cost[i]
                + self._transition_penalty(i - 2, i, premium, penalty)
            )

            # Choose the cheaper of the two valid ways to reach room i.
            dp[i] = min(from_prev_one, from_prev_two])

        # The problem asks for the minimum total cost to finish at room n - 1.
        # That value has now been fully computed.
        return dp[n - 1]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    cost1: List[int] = [4, 2, 7, 3, 5]
    premium1: List[int] = [1, 0, 1, 1, 0]
    penalty1: int = 6
    result1: int = solution.min_cost_exhibit_path(cost1, premium1, penalty1)
    print(result1)  # Expected: 14

    # Example 2
    cost2: List[int] = [1, 9, 2, 8, 1, 3]
    premium2: List[int] = [1, 1, 0, 1, 1, 1]
    penalty2: int = 4
    result2: int = solution.min_cost_exhibit_path(cost2, premium2, penalty2)
    print(result2)  # Expected: 11