/*
Title: Minimum Cost to Schedule Factory Maintenance With Team Cooldowns

Problem Description:
A factory must perform maintenance on a sequence of n machines over n consecutive days.
On day i, exactly one maintenance team must be assigned to machine i.

There are 3 available teams:
0 = Electrical
1 = Mechanical
2 = Software

Assigning team t to machine i has a known cost cost[i][t].

Cooldown rule:
If a team is used on day i, that same team cannot be used again on day i + 1 or day i + 2.
So, the team chosen today must be different from the teams used on the previous two days.

Goal:
Compute the minimum total maintenance cost to complete all n days while satisfying the rule.
If no valid schedule exists, return -1.

Important note about the examples in the prompt:
The written explanations contain inconsistencies, but the rule itself is clear:
- Day i team must differ from day i-1 team
- Day i team must also differ from day i-2 team

With exactly 3 teams, from day 2 onward the choice is forced:
it must be the third team different from the previous two.

This solution correctly follows the rule definition.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * 3 * 4 * 3) which simplifies to O(n), because the number of teams is fixed at 3
    and the number of "previous-day state" combinations is also constant.

    Space Complexity:
    O(1) auxiliary space, because we only keep DP values for the previous day and current day.
    The number of states is constant.
    */
    public long MinMaintenanceCost(int[][] cost)
    {
        // -----------------------------
        // Step 1: Basic input handling
        // -----------------------------
        // We guard against null or empty input.
        // The problem constraints say n >= 1, but adding this check makes the method safer.
        if (cost == null || cost.Length == 0)
        {
            return -1;
        }

        int n = cost.Length;

        // We will use a very large number as "infinity".
        // Any state that is impossible will keep this value.
        long INF = long.MaxValue / 4;

        // -------------------------------------------------------------------------
        // Step 2: Define the DP state in a beginner-friendly way
        // -------------------------------------------------------------------------
        // Because today's valid choice depends on the previous TWO days,
        // our DP state must remember those two days.
        //
        // Let:
        //   prev1 = team used on day i-1
        //   prev2 = team used on day i-2
        //
        // Since there are only 3 real teams (0,1,2), we also need a special value
        // to represent "no team yet" for the beginning of the schedule.
        //
        // We will use:
        //   3 = NONE
        //
        // So each state is:
        //   dp[prev1, prev2] = minimum total cost after processing some prefix of days,
        //                      where the most recent team is prev1
        //                      and the second most recent team is prev2.
        //
        // Why this works:
        // To decide whether we can place team t on the next day,
        // we only need to know the teams used on the previous two days.
        //
        // Number of possible values for prev1 and prev2:
        //   0, 1, 2, 3  => 4 possibilities each
        // So total states = 4 * 4 = 16, which is tiny and constant.
        const int NONE = 3;

        long[,] dp = new long[4, 4];
        long[,] next = new long[4, 4];

        // Initialize all states as impossible.
        for (int a = 0; a < 4; a++)
        {
            for (int b = 0; b < 4; b++)
            {
                dp[a, b] = INF;
                next[a, b] = INF;
            }
        }

        // Before processing any day:
        // - there is no previous day
        // - there is no day before that either
        // So the starting state is (NONE, NONE) with cost 0.
        dp[NONE, NONE] = 0;

        // ------------------------------------------------------------
        // Step 3: Process each day one by one using dynamic programming
        // ------------------------------------------------------------
        for (int day = 0; day < n; day++)
        {
            // Before filling transitions for this day,
            // reset the "next" table to INF because we are computing fresh values.
            for (int a = 0; a < 4; a++)
            {
                for (int b = 0; b < 4; b++)
                {
                    next[a, b] = INF;
                }
            }

            // Try every previously reachable state.
            for (int prev1 = 0; prev1 < 4; prev1++)
            {
                for (int prev2 = 0; prev2 < 4; prev2++)
                {
                    long currentCost = dp[prev1, prev2];

                    // If this state was never reached, skip it.
                    if (currentCost >= INF)
                    {
                        continue;
                    }

                    // ---------------------------------------------------------
                    // Step 3a: Try assigning each of the 3 teams on this day
                    // ---------------------------------------------------------
                    for (int team = 0; team < 3; team++)
                    {
                        // Cooldown rule:
                        // The chosen team must be different from the team used yesterday (prev1)
                        // and also different from the team used two days ago (prev2).
                        //
                        // If prev1 or prev2 is NONE, the comparison naturally does not block anything
                        // because team is only 0..2.
                        if (team == prev1 || team == prev2)
                        {
                            continue;
                        }

                        // If we choose "team" today:
                        // - it becomes the new most recent team
                        // - the old prev1 becomes the new second most recent team
                        int newPrev1 = team;
                        int newPrev2 = prev1;

                        long candidate = currentCost + cost[day][team];

                        // Keep the best (minimum) cost for the resulting state.
                        if (candidate < next[newPrev1, newPrev2])
                        {
                            next[newPrev1, newPrev2] = candidate;
                        }
                    }
                }
            }

            // Move to the next day:
            // the states we just computed become the current DP table.
            var temp = dp;
            dp = next;
            next = temp;
        }

        // -------------------------------------------------------
        // Step 4: Extract the best answer among all final states
        // -------------------------------------------------------
        long answer = INF;

        for (int prev1 = 0; prev1 < 4; prev1++)
        {
            for (int prev2 = 0; prev2 < 4; prev2++)
            {
                if (dp[prev1, prev2] < answer)
                {
                    answer = dp[prev1, prev2];
                }
            }
        }

        // If answer stayed INF, no valid schedule exists.
        // This can happen, for example, when n >= 4 and only 3 teams are available?
        // Actually with 3 teams, valid schedules do exist for all n:
        // once the first two distinct teams are chosen, the third is forced, then the pattern continues.
        // Still, we keep this check because it is the correct general DP behavior.
        return answer >= INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt.
// The prompt's stated output/explanation is inconsistent with the cooldown rule.
// We will compute the correct answer according to the actual rule:
// day i team must differ from day i-1 and day i-2.
int[][] cost1 =
{
    new[] { 5, 1, 4 },
    new[] { 2, 3, 6 },
    new[] { 7, 2, 5 },
    new[] { 4, 6, 3 }
};

long result1 = solution.MinMaintenanceCost(cost1);
Console.WriteLine("Example 1 result: " + result1);

// Example 2 from the prompt.
// With only 2 days, the rule means the same team cannot be used on consecutive days.
// The minimum is Mechanical on day 0 (2) and Electrical on day 1 (1) => total 3.
int[][] cost2 =
{
    new[] { 3, 2, 7 },
    new[] { 5, 1, 4 }
};

long result2 = solution.MinMaintenanceCost(cost2);
Console.WriteLine("Example 2 result: " + result2);

// Additional small sanity checks:

// n = 1: simply choose the cheapest team on the only day.
int[][] cost3 =
{
    new[] { 9, 4, 6 }
};
Console.WriteLine("Single day result: " + solution.MinMaintenanceCost(cost3));

// n = 3: all three days must use all different teams.
int[][] cost4 =
{
    new[] { 1, 100, 100 },
    new[] { 100, 1, 100 },
    new[] { 100, 100, 1 }
};
Console.WriteLine("Three day result: " + solution.MinMaintenanceCost(cost4));