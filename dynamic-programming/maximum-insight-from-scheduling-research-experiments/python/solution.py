"""
Title: Maximum Insight from Scheduling Research Experiments

Problem Description:
A research lab has planned n experiments over the next several days. Experiment i must be
started on or before day deadline[i], requires exactly duration[i] consecutive days to complete,
and yields insight[i] points if it is fully completed by its deadline. Only one experiment can
run on any given day, and once an experiment starts, it cannot be interrupted.

You may choose any subset of experiments and schedule them in any order, as long as every chosen
experiment finishes no later than its own deadline. Your task is to compute the maximum total
insight that can be obtained.

Unlike simple interval scheduling, each experiment can be reordered relative to the others, and
feasibility depends on the total occupied time before each chosen deadline. This makes greedy
choices insufficient in many cases.

Return the maximum possible sum of insight points.

Constraints:
- 1 <= n <= 200
- 1 <= duration[i] <= 200
- 1 <= deadline[i] <= 2000
- 1 <= insight[i] <= 10^6
- The answer fits in a 64-bit signed integer.
"""

from typing import List, Tuple


class Solution:
    def max_insight(self, duration: List[int], deadline: List[int], insight: List[int]) -> int:
        """
        Compute the maximum total insight obtainable by selecting and scheduling
        a subset of experiments so that each chosen experiment finishes by its deadline.

        The key idea is dynamic programming after sorting experiments by deadline.
        For each possible total time used so far, we store the best total insight
        achievable while remaining feasible.

        Args:
            duration: List where duration[i] is the number of consecutive days
                required by experiment i.
            deadline: List where deadline[i] is the latest day by which experiment i
                must be completed.
            insight: List where insight[i] is the reward gained if experiment i is
                completed by its deadline.

        Returns:
            The maximum total insight as an integer.

        Time complexity:
            O(n * D), where n is the number of experiments and D is the maximum deadline.

        Space complexity:
            O(D), where D is the maximum deadline.
        """
        # If there are no experiments, the best total insight is 0.
        # The constraints guarantee n >= 1, but this guard keeps the method robust.
        if not duration:
            return 0

        # ---------------------------------------------------------------------
        # STEP 1: Combine the three input arrays into a single list of tuples.
        #
        # Each tuple is:
        #   (deadline, duration, insight)
        #
        # Why do we sort by deadline?
        # In scheduling problems with "must finish by deadline" constraints,
        # sorting by deadline is a classic and very important trick.
        #
        # Once experiments are processed in nondecreasing deadline order,
        # any feasible subset of the first k experiments can be arranged so that
        # their completion times respect these sorted deadlines.
        #
        # This transforms the problem into a knapsack-like DP:
        #   - "weight"  = duration
        #   - "value"   = insight
        #   - capacity is not fixed globally in the usual way; instead, each item
        #     can only be placed if the resulting completion time does not exceed
        #     that item's own deadline.
        # ---------------------------------------------------------------------
        experiments: List[Tuple[int, int, int]] = sorted(
            zip(deadline, duration, insight)
        )

        # The DP time dimension only needs to go up to the largest deadline,
        # because finishing after the largest deadline can never help.
        max_deadline: int = max(deadline)

        # ---------------------------------------------------------------------
        # STEP 2: Create the DP array.
        #
        # dp[t] = maximum total insight achievable using some subset of the
        #         processed experiments such that:
        #         - total occupied time is exactly t
        #         - the chosen subset is schedulable feasibly with respect to
        #           the deadlines of those processed experiments
        #
        # If a state is impossible, we store a very negative number.
        #
        # Why exact time instead of "at most time"?
        # Exact-time DP is standard for 0/1 knapsack-style transitions because
        # it makes updates precise and avoids accidental reuse of the same item.
        #
        # Base case:
        #   dp[0] = 0
        # because using 0 days gives 0 insight and is always feasible.
        # ---------------------------------------------------------------------
        negative_infinity: int = -10**30
        dp: List[int] = [negative_infinity] * (max_deadline + 1)
        dp[0] = 0

        # ---------------------------------------------------------------------
        # STEP 3: Process each experiment one by one.
        #
        # For an experiment with:
        #   deadline = d
        #   duration = p
        #   insight  = v
        #
        # We consider taking it as the LAST experiment among the chosen subset
        # from the processed prefix.
        #
        # If before taking it we had already used 't - p' days feasibly,
        # then after adding this experiment we use exactly 't' days.
        #
        # This is only allowed if:
        #   t <= d
        # because this experiment must finish by its own deadline.
        #
        # We iterate time backwards to ensure each experiment is used at most once.
        # This is the standard 0/1 knapsack update direction.
        # ---------------------------------------------------------------------
        for d, p, v in experiments:
            # We only need to consider completion times up to this experiment's deadline.
            # Any completion time larger than d would violate this experiment's constraint.
            for t in range(d, p - 1, -1):
                # If dp[t - p] is reachable, then we can append this experiment
                # and finish at time t.
                if dp[t - p] != negative_infinity:
                    candidate: int = dp[t - p] + v

                    # Keep the better of:
                    #   - not taking this experiment for exact time t
                    #   - taking this experiment and ending at exact time t
                    if candidate > dp[t]:
                        dp[t] = candidate

        # ---------------------------------------------------------------------
        # STEP 4: The answer is the best insight over all feasible finishing times.
        #
        # We do NOT require using all available days.
        # Any exact total time t from 0..max_deadline is acceptable.
        # Therefore, the final answer is simply the maximum value in dp.
        # ---------------------------------------------------------------------
        return max(dp)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    duration1 = [2, 1, 2]
    deadline1 = [2, 2, 3]
    insight1 = [8, 4, 7]
    result1 = solution.max_insight(duration1, deadline1, insight1)
    print("Example 1 Output:", result1)

    # Example 2
    duration2 = [3, 1, 2, 2]
    deadline2 = [3, 4, 5, 6]
    insight2 = [10, 3, 9, 8]
    result2 = solution.max_insight(duration2, deadline2, insight2)
    print("Example 2 Output:", result2)