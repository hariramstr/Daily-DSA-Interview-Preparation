"""
Title: Minimum Cost to Schedule Server Reboots with Cluster Penalties

Problem Description:
You are given a sequence of n servers in a data center, indexed from 0 to n - 1.
Each server must be rebooted exactly once, and the reboot for server i can be
scheduled in one of two maintenance windows:

1. Early window with cost earlyCost[i]
2. Late window with cost lateCost[i]

However, rebooting adjacent servers in the same window may overload a local rack
controller. For every adjacent pair (i - 1, i):

- If both servers are assigned to the early window, add earlyPenalty[i]
- If both servers are assigned to the late window, add latePenalty[i]
- If they are assigned to different windows, add no penalty

Additionally, exactly k servers must be scheduled in the early window.

Goal:
Choose Early/Late for every server so that exactly k servers are Early and the
total cost is minimized.

Total cost =
    sum of chosen reboot costs
    + sum of adjacency penalties for neighboring servers assigned to the same window

Constraints:
- 1 <= n <= 2000
- 0 <= k <= n
- earlyCost.length == lateCost.length == n
- earlyPenalty.length == latePenalty.length == n
- earlyPenalty[0] and latePenalty[0] are ignored
- 1 <= earlyCost[i], lateCost[i], earlyPenalty[i], latePenalty[i] <= 10^9

Important note about the examples:
The written explanations contain arithmetic inconsistencies, but the actual
minimum values are:
- Example 1 => 9
- Example 2 => 15
"""

from typing import List


class Solution:
    def minimum_reboot_cost(
        self,
        n: int,
        k: int,
        earlyCost: List[int],
        lateCost: List[int],
        earlyPenalty: List[int],
        latePenalty: List[int],
    ) -> int:
        """
        Compute the minimum total cost to schedule exactly k servers in the early window.

        We use dynamic programming where we process servers from left to right.
        The key observation is that the penalty for server i depends only on the
        choice made for server i and the immediately previous server i - 1.
        Therefore, the DP state only needs to remember:
        - how many early-window assignments have been used so far
        - whether the previous server was assigned Early or Late

        Args:
            n: Number of servers.
            k: Exact number of servers that must be assigned to the early window.
            earlyCost: Cost of assigning each server to the early window.
            lateCost: Cost of assigning each server to the late window.
            earlyPenalty: Penalty when both i - 1 and i are Early.
            latePenalty: Penalty when both i - 1 and i are Late.

        Returns:
            The minimum possible total cost as an integer.

        Time complexity:
            O(n * k)

        Space complexity:
            O(k)
        """
        # A very large number used to represent an unreachable DP state.
        # Python integers are unbounded, so this is safe.
        inf: int = 10**30

        # Edge case:
        # If k is outside the valid range, there is no feasible assignment.
        # The problem constraints guarantee 0 <= k <= n, but this check makes
        # the method more robust and beginner-friendly.
        if k < 0 or k > n:
            return -1

        # ---------------------------------------------------------------------
        # DP IDEA
        # ---------------------------------------------------------------------
        # We maintain two 1D arrays for the current processed prefix:
        #
        # dp_early[e] = minimum cost after processing some prefix of servers,
        #               using exactly e early assignments,
        #               and assigning the LAST processed server to Early.
        #
        # dp_late[e]  = same idea, but the LAST processed server is Late.
        #
        # Why is this enough?
        # Because when we decide the assignment of the next server, the only
        # adjacency penalty we may need to add depends on whether the previous
        # server was Early or Late. So remembering only the last choice is
        # sufficient.
        #
        # This is a classic "DP with last state" pattern.
        # ---------------------------------------------------------------------

        # Initialize DP arrays for "before processing any server".
        # At this moment, no state is valid yet.
        dp_early: List[int] = [inf] * (k + 1)
        dp_late: List[int] = [inf] * (k + 1)

        # ---------------------------------------------------------------------
        # BASE CASE: process server 0 separately
        # ---------------------------------------------------------------------
        # If server 0 is assigned Early:
        # - cost is earlyCost[0]
        # - number of early assignments used becomes 1
        # - no adjacency penalty exists because there is no previous server
        if k >= 1:
            dp_early[1] = earlyCost[0]

        # If server 0 is assigned Late:
        # - cost is lateCost[0]
        # - number of early assignments used remains 0
        # - no adjacency penalty exists
        dp_late[0] = lateCost[0]

        # ---------------------------------------------------------------------
        # PROCESS SERVERS 1 TO n - 1
        # ---------------------------------------------------------------------
        # For each server i, we build new DP arrays from the previous ones.
        # We consider both choices for server i:
        #   1. Assign Early
        #   2. Assign Late
        #
        # For each choice, we transition from both possible previous-last states:
        #   - previous server was Early
        #   - previous server was Late
        #
        # If the current choice matches the previous choice, we add the
        # corresponding adjacency penalty.
        # ---------------------------------------------------------------------
        for i in range(1, n):
            next_early: List[int] = [inf] * (k + 1)
            next_late: List[int] = [inf] * (k + 1)

            # The number of early assignments used after processing server i
            # can never exceed i + 1, and also cannot exceed k.
            max_early_used: int = min(i + 1, k)

            for used_early in range(0, max_early_used + 1):
                # -------------------------------------------------------------
                # OPTION 1: assign server i to Early
                # -------------------------------------------------------------
                # Then the previous prefix must have used exactly used_early - 1
                # early assignments, because current server contributes one more.
                if used_early >= 1:
                    prev_early_count: int = used_early - 1

                    # Transition from previous state ending in Early:
                    # previous server was Early, current is also Early
                    # => add earlyCost[i] + earlyPenalty[i]
                    if dp_early[prev_early_count] != inf:
                        candidate: int = (
                            dp_early[prev_early_count]
                            + earlyCost[i]
                            + earlyPenalty[i]
                        )
                        if candidate < next_early[used_early]:
                            next_early[used_early] = candidate

                    # Transition from previous state ending in Late:
                    # previous server was Late, current is Early
                    # => add only earlyCost[i], no adjacency penalty
                    if dp_late[prev_early_count] != inf:
                        candidate = dp_late[prev_early_count] + earlyCost[i]
                        if candidate < next_early[used_early]:
                            next_early[used_early] = candidate

                # -------------------------------------------------------------
                # OPTION 2: assign server i to Late
                # -------------------------------------------------------------
                # Then the previous prefix must already have used exactly
                # used_early early assignments.
                prev_early_count = used_early

                # Transition from previous state ending in Early:
                # previous server was Early, current is Late
                # => add only lateCost[i], no adjacency penalty
                if dp_early[prev_early_count] != inf:
                    candidate = dp_early[prev_early_count] + lateCost[i]
                    if candidate < next_late[used_early]:
                        next_late[used_early] = candidate

                # Transition from previous state ending in Late:
                # previous server was Late, current is also Late
                # => add lateCost[i] + latePenalty[i]
                if dp_late[prev_early_count] != inf:
                    candidate = (
                        dp_late[prev_early_count]
                        + lateCost[i]
                        + latePenalty[i]
                    )
                    if candidate < next_late[used_early]:
                        next_late[used_early] = candidate

            # Move to the next iteration:
            # the "next" arrays become the current DP arrays.
            dp_early = next_early
            dp_late = next_late

        # ---------------------------------------------------------------------
        # FINAL ANSWER
        # ---------------------------------------------------------------------
        # After processing all n servers, we need exactly k early assignments.
        # The last server may be either Early or Late, so we take the minimum.
        answer: int = min(dp_early[k], dp_late[k])

        # Under valid constraints, a feasible solution always exists.
        # Still, we keep a defensive check.
        return answer if answer != inf else -1

    def solve(
        self,
        n: int,
        k: int,
        earlyCost: List[int],
        lateCost: List[int],
        earlyPenalty: List[int],
        latePenalty: List[int],
    ) -> int:
        """
        Wrapper method that calls the main dynamic programming solution.

        Args:
            n: Number of servers.
            k: Exact number of servers that must be assigned to the early window.
            earlyCost: Cost of assigning each server to the early window.
            lateCost: Cost of assigning each server to the late window.
            earlyPenalty: Penalty when both i - 1 and i are Early.
            latePenalty: Penalty when both i - 1 and i are Late.

        Returns:
            The minimum possible total cost.

        Time complexity:
            O(n * k)

        Space complexity:
            O(k)
        """
        return self.minimum_reboot_cost(
            n, k, earlyCost, lateCost, earlyPenalty, latePenalty
        )


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # The problem statement's arithmetic contains a contradiction.
    # Let's verify the true minimum:
    #
    # Assignment [Early, Late, Early, Late]
    # Base cost = 3 + 1 + 2 + 3 = 9
    # No same-window adjacent pairs => no penalties
    # Exactly 2 early assignments
    # Total = 9
    n1 = 4
    k1 = 2
    earlyCost1 = [3, 8, 2, 6]
    lateCost1 = [5, 1, 7, 3]
    earlyPenalty1 = [0, 4, 5, 2]
    latePenalty1 = [0, 6, 1, 3]
    result1 = solution.solve(n1, k1, earlyCost1, lateCost1, earlyPenalty1, latePenalty1)
    print(result1)  # Expected: 9

    # Example 2
    # The statement says output 18, but its own first candidate has total 15,
    # which is better. So the correct minimum is 15.
    #
    # Assignment [Early, Early, Late, Early, Late]
    # Base cost = 4 + 2 + 1 + 3 + 2 = 12
    # Penalty for servers 0 and 1 both Early => +3
    # Total = 15
    n2 = 5
    k2 = 3
    earlyCost2 = [4, 2, 9, 3, 5]
    lateCost2 = [6, 7, 1, 8, 2]
    earlyPenalty2 = [0, 3, 4, 6, 1]
    latePenalty2 = [0, 2, 5, 2, 7]
    result2 = solution.solve(n2, k2, earlyCost2, lateCost2, earlyPenalty2, latePenalty2)
    print(result2)  # Expected: 15