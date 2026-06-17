"""
Title: Minimum Rest Stops to Climb a Stair Route

Problem Description:
You are planning a climb along a stair route with n steps, numbered from 0 to n.
You start at step 0 and want to reach step n. From any step, you may move either
1 step or 2 steps forward. Some steps are marked as rest stops, represented by a
binary array rests of length n + 1, where rests[i] = 1 means step i has a rest
platform and rests[i] = 0 means it does not. Step 0 and step n may also be rest
stops.

Your goal is to reach step n while using the minimum possible number of rest stops
visited, including the destination if it is a rest stop. A rest stop is counted
only when you land on that step. If it is impossible to reach the top because every
valid path would require landing on a blocked step, return -1. A blocked step is
represented by another binary array blocked of length n + 1, where blocked[i] = 1
means you cannot stand on step i. You may assume blocked[0] = 0.

Return the minimum number of rest stops needed to reach step n.

This is a dynamic programming problem because the best answer for each step depends
on the best answers for the previous one or two steps.

Constraints:
- 1 <= n <= 100000
- rests.length == n + 1
- blocked.length == n + 1
- rests[i] is either 0 or 1
- blocked[i] is either 0 or 1
- blocked[0] = 0
"""

from typing import List


class Solution:
    def min_rest_stops(self, n: int, rests: List[int], blocked: List[int]) -> int:
        """
        Compute the minimum number of visited rest stops needed to reach step n.

        We use dynamic programming:
        - Let dp[i] represent the minimum number of rest stops visited to land on step i.
        - Since from step i we can only come from step i - 1 or step i - 2,
          the answer for dp[i] depends only on those two previous states.
        - If a step is blocked, it cannot be used at all.

        Args:
            n: The destination step number.
            rests: Binary list where rests[i] == 1 means step i is a rest stop.
            blocked: Binary list where blocked[i] == 1 means step i is blocked.

        Returns:
            The minimum number of visited rest stops needed to reach step n,
            or -1 if step n cannot be reached.

        Time complexity:
            O(n), because we process each step exactly once.

        Space complexity:
            O(n), because we store one DP value per step.
        """
        # We use a very large number as a stand-in for "unreachable".
        # This is a common dynamic programming technique:
        # - If a state cannot be reached, we keep its value as INF.
        # - Later, if the final answer is still INF, we know the destination
        #   was impossible to reach.
        inf: int = 10**18

        # dp[i] will store the minimum number of rest stops visited to land on step i.
        # Initially, we assume every step is unreachable.
        dp: List[int] = [inf] * (n + 1)

        # Base case:
        # We start on step 0.
        #
        # The problem says a rest stop is counted only when you land on that step.
        # Since we begin at step 0, we are already standing there at the start,
        # not "landing" there from a move. Therefore, we do NOT count rests[0].
        #
        # So the cost to be at step 0 is 0.
        dp[0] = 0

        # Now we fill the DP table from step 1 to step n.
        for i in range(1, n + 1):
            # If the current step is blocked, we are not allowed to stand on it.
            # That means dp[i] must remain unreachable.
            if blocked[i] == 1:
                continue

            # The cost of landing on this step:
            # - If this step is a rest stop, landing here adds 1.
            # - Otherwise, it adds 0.
            current_step_cost: int = rests[i]

            # Option 1: Reach step i from step i - 1.
            # This move is valid only if step i - 1 itself was reachable.
            if dp[i - 1] != inf:
                dp[i] = min(dp[i], dp[i - 1] + current_step_cost)

            # Option 2: Reach step i from step i - 2.
            # This is possible only when i >= 2 and step i - 2 was reachable.
            if i >= 2 and dp[i - 2] != inf:
                dp[i] = min(dp[i], dp[i - 2] + current_step_cost)

        # If dp[n] is still INF, then no valid sequence of 1-step and 2-step moves
        # could reach the destination without landing on a blocked step.
        if dp[n] == inf:
            return -1

        # Otherwise, dp[n] is the minimum number of rest stops visited.
        return dp[n]


if __name__ == "__main__":
    """
    Run sample test cases from the problem statement.
    """

    solution = Solution()

    # Example 1
    # n = 5
    # rests = [0,1,0,1,0,1]
    # blocked = [0,0,0,0,0,0]
    #
    # One optimal path:
    # 0 -> 2 -> 4 -> 5
    # Visited rest stops among landed steps: only step 5
    # Expected answer: 1
    n1: int = 5
    rests1: List[int] = [0, 1, 0, 1, 0, 1]
    blocked1: List[int] = [0, 0, 0, 0, 0, 0]
    result1: int = solution.min_rest_stops(n1, rests1, blocked1)
    print(result1)  # Expected: 1

    # Example 2
    # n = 6
    # rests = [0,1,1,0,1,0,1]
    # blocked = [0,0,1,0,0,1,0]
    #
    # Note:
    # The written explanation in the prompt says:
    # "Step 2 and step 5 are blocked. An optimal path is 0 -> 1 -> 3 -> 4 -> 6.
    # Among the visited steps, only step 1 is a rest stop, so the answer is 1."
    #
    # However, with the given rests array:
    # rests[4] = 1 and rests[6] = 1
    # So along path 0 -> 1 -> 3 -> 4 -> 6, the visited rest stops are:
    # step 1, step 4, and step 6 => total 3
    #
    # Let's trace the true minimum:
    # - 0 -> 1 -> 3 -> 4 -> 6 gives 3 rest stops
    # - 0 -> 1 -> 3 -> 4 -> 6 is essentially forced because 2 and 5 are blocked
    # Therefore the correct result for the provided arrays is 3.
    n2: int = 6
    rests2: List[int] = [0, 1, 1, 0, 1, 0, 1]
    blocked2: List[int] = [0, 0, 1, 0, 0, 1, 0]
    result2: int = solution.min_rest_stops(n2, rests2, blocked2)
    print(result2)  # Correct for the provided arrays: 3