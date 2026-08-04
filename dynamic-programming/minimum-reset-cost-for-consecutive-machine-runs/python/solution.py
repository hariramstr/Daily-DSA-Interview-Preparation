"""
Title: Minimum Reset Cost for Consecutive Machine Runs

Problem Description:
A factory must process a sequence of n jobs in the given order. Each job i requires the machine
to run in one of several supported modes, represented by an integer modes[i].

The machine may continue running in the same mode for the next job at no extra cost.
However, if the next job uses a different mode, the factory must pay a reset cost.

You are also given an array resetCost where resetCost[x] is the cost to switch the machine
into mode x. Switching from any mode a to a different mode b always costs resetCost[b].
The initial machine state is undefined, so starting the first job in mode x also costs resetCost[x].

Before processing begins, the factory may upgrade at most k jobs.
Upgrading job i allows it to be processed in any mode you choose, not just modes[i].
Each upgraded job still occupies its position in the sequence, but you may assign it any mode
in order to reduce the total reset cost.

Return the minimum possible total cost to process all jobs.

In other words, you may change the required mode of up to k positions to arbitrary modes,
and you want to minimize the sum of start/switch costs across the full sequence.

Constraints:
- 1 <= n <= 2000
- 1 <= k <= n
- 1 <= modes[i] <= m
- 1 <= m <= 100
- resetCost.length == m + 1, where index 0 is unused
- 1 <= resetCost[x] <= 10^4
"""

from typing import List


class Solution:
    def min_reset_cost(self, modes: List[int], k: int, resetCost: List[int]) -> int:
        """
        Compute the minimum total start/switch cost after upgrading at most k jobs.

        We use dynamic programming over the sequence:
        - Process jobs from left to right.
        - Track how many upgrades have been used so far.
        - Track the machine mode used for the current job.
        - For each position, either:
            1) Keep the original required mode (no upgrade if chosen mode equals modes[i])
            2) Upgrade this job and assign it any mode (costs 1 upgrade if chosen mode differs)

        State meaning:
        dp[u][x] = minimum total cost after processing the current prefix,
                   using exactly u upgrades,
                   and ending with machine in mode x for the current job.

        Transition:
        To place current job in mode x:
        - If previous job also ended in x, no extra reset cost.
        - Otherwise, switching into x costs resetCost[x].
        - If x != modes[i], this job must be upgraded.

        To make transitions efficient, for each upgrade count we keep:
        - the best previous value among all ending modes
        - and the best previous value ending specifically in mode x

        Then:
        new_dp[u + upgrade_needed][x] =
            min(
                previous_same_mode_cost,             # stay in x, no switch cost
                previous_best_any_mode + resetCost[x]  # switch/start into x
            )

        Args:
            modes: List of required modes for each job.
            k: Maximum number of jobs that may be upgraded.
            resetCost: resetCost[x] is the cost to switch/start into mode x. Index 0 unused.

        Returns:
            Minimum possible total cost.

        Time complexity:
            O(n * k * m), where:
            - n = number of jobs
            - k = max upgrades
            - m = number of modes

        Space complexity:
            O(k * m)
        """
        n: int = len(modes)
        m: int = len(resetCost) - 1
        inf: int = 10**18

        # dp[u][x]:
        # Minimum cost after processing jobs up to the current position,
        # using exactly u upgrades, and ending with machine mode x.
        #
        # We use 1-based indexing for modes because resetCost is also 1-based.
        dp: List[List[int]] = [[inf] * (m + 1) for _ in range(k + 1)]

        # -----------------------------
        # Initialize for the first job.
        # -----------------------------
        # For the first job, there is no "previous mode".
        # Starting directly in mode x costs resetCost[x].
        #
        # If x == modes[0], no upgrade is needed.
        # If x != modes[0], we must spend 1 upgrade to change this job into mode x.
        first_required: int = modes[0]
        for x in range(1, m + 1):
            upgrades_used: int = 0 if x == first_required else 1
            if upgrades_used <= k:
                dp[upgrades_used][x] = resetCost[x]

        # ------------------------------------------------------------
        # Process each remaining job one by one using rolling updates.
        # ------------------------------------------------------------
        for i in range(1, n):
            required_mode: int = modes[i]

            # next_dp will store states after processing job i.
            next_dp: List[List[int]] = [[inf] * (m + 1) for _ in range(k + 1)]

            # For each possible number of upgrades already used,
            # we want to transition from dp[u][*] to next_dp[*][*].
            for u in range(k + 1):
                # ---------------------------------------------------------
                # For this fixed upgrade count u, precompute:
                # 1) best1_val = smallest dp[u][mode]
                # 2) best1_mode = mode achieving best1_val
                # 3) best2_val = second smallest dp[u][mode]
                #
                # Why do we need this?
                # If we want to end current job in mode x:
                # - staying in x from previous x costs dp[u][x]
                # - switching from any different previous mode costs
                #       min_{y != x}(dp[u][y]) + resetCost[x]
                #
                # With best1/best2, we can answer min_{y != x} quickly:
                # - if best1_mode != x, use best1_val
                # - else use best2_val
                # ---------------------------------------------------------
                best1_val: int = inf
                best1_mode: int = -1
                best2_val: int = inf

                for mode in range(1, m + 1):
                    value: int = dp[u][mode]
                    if value < best1_val:
                        best2_val = best1_val
                        best1_val = value
                        best1_mode = mode
                    elif value < best2_val:
                        best2_val = value

                # If no state is reachable for this u, skip it.
                if best1_val == inf:
                    continue

                # ---------------------------------------------------------
                # Try assigning the current job to every possible mode x.
                #
                # If x == required_mode:
                #   no upgrade needed for this job
                # Else:
                #   this job must be upgraded, so +1 upgrade
                #
                # Cost to end in x:
                #   min(
                #       dp[u][x],                         # continue in same mode
                #       min_{y != x}(dp[u][y]) + resetCost[x]  # switch into x
                #   )
                # ---------------------------------------------------------
                for x in range(1, m + 1):
                    extra_upgrade: int = 0 if x == required_mode else 1
                    new_u: int = u + extra_upgrade
                    if new_u > k:
                        continue

                    # Option 1: previous job already ended in mode x,
                    # so we keep the machine in x and pay no reset cost.
                    stay_cost: int = dp[u][x]

                    # Option 2: previous job ended in some different mode,
                    # so we must switch into x and pay resetCost[x].
                    if best1_mode != x:
                        switch_from_other: int = best1_val + resetCost[x]
                    else:
                        switch_from_other = best2_val + resetCost[x]

                    best_transition: int = min(stay_cost, switch_from_other)

                    if best_transition < next_dp[new_u][x]:
                        next_dp[new_u][x] = best_transition

            dp = next_dp

        # ---------------------------------------------------------
        # Final answer:
        # After all jobs are processed, we may have used any number
        # of upgrades from 0 to k, and may end in any mode.
        # We take the minimum reachable cost among all such states.
        # ---------------------------------------------------------
        answer: int = inf
        for u in range(k + 1):
            row_min: int = min(dp[u][1:])
            if row_min < answer:
                answer = row_min

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    modes1: List[int] = [1, 2, 2, 3]
    k1: int = 1
    reset_cost1: List[int] = [0, 5, 2, 7]
    result1: int = solution.min_reset_cost(modes1, k1, reset_cost1)
    print("Example 1 Result:", result1)  # Expected: 7

    # Example 2
    modes2: List[int] = [4, 1, 4, 1, 4]
    k2: int = 2
    reset_cost2: List[int] = [0, 3, 6, 8, 2]
    result2: int = solution.min_reset_cost(modes2, k2, reset_cost2)
    print("Example 2 Result:", result2)  # Expected: 2

    # Additional quick sanity checks
    modes3: List[int] = [2, 2, 2]
    k3: int = 0
    reset_cost3: List[int] = [0, 5, 3]
    result3: int = solution.min_reset_cost(modes3, k3, reset_cost3)
    print("Sanity Check 1 Result:", result3)  # Expected: 3

    modes4: List[int] = [1, 2, 1]
    k4: int = 1
    reset_cost4: List[int] = [0, 4, 1]
    result4: int = solution.min_reset_cost(modes4, k4, reset_cost4)
    print("Sanity Check 2 Result:", result4)  # One optimal result: 1 by converting all effectively to mode 2 with one upgrade at first/last not enough, so expected 5? Let's print actual.