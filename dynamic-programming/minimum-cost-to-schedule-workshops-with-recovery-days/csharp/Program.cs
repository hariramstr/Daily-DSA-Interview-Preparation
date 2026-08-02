/*
Title: Minimum Cost to Schedule Workshops with Recovery Days

Problem Description:
You are organizing a training program over N calendar days. On day i, you may choose to run a workshop and earn value[i] participants, but running a workshop also increases fatigue. After holding a workshop on day i, you must leave the next cooldown[i] days empty as recovery days before scheduling another workshop. In other words, if you run a workshop on day i, the next workshop can be scheduled no earlier than day i + cooldown[i] + 1.

Each day also has a fixed operating cost cost[i] if you choose to run the workshop that day. Your goal is to reach at least target total participants while minimizing the total operating cost. You may skip any days, and you are not required to use all days. If it is impossible to reach at least target participants, return -1.

Design an algorithm to compute the minimum total cost.

Constraints:
- 1 <= N <= 200
- 1 <= target <= 5000
- 1 <= value[i] <= 100
- 1 <= cost[i] <= 1000
- 0 <= cooldown[i] < N

Important note about the examples:
The written explanations in the prompt contain contradictions. For example:
- In Example 1, day 0 and day 3 gives 6 + 3 = 9, not enough.
- Day 1 and day 2 is valid because cooldown[1] = 0, giving 11 participants for cost 8.
- Day 0 and day 2 is also valid because cooldown[0] = 1 blocks only day 1, giving 13 participants for cost 11.
So the correct minimum for Example 1 is 8.

- In Example 2, with value = [5,8,4], cost = [4,9,3], cooldown = [2,1,0], target = 13:
  day 1 and day 2 is NOT valid because cooldown[1] = 1 blocks day 2.
  day 0 blocks both day 1 and day 2.
  Therefore reaching 13 is impossible, so the correct answer is -1.

This solution follows the actual scheduling rule exactly and therefore returns:
- Example 1 => 8
- Example 2 => -1
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(N * target)

    Explanation:
    - We process each day once.
    - For each day, we consider every capped participant total from 0 to target.
    - Each transition is O(1), so total work is O(N * target).

    Space Complexity:
    O(N * target)

    Explanation:
    - dp[day, participants] stores the minimum cost needed to be at "day"
      having accumulated exactly "participants" participants, where participants
      is capped at target.
    - There are (N + 1) * (target + 1) states.
    */
    public int MinCostToReachTarget(int[] value, int[] cost, int[] cooldown, int target)
    {
        int n = value.Length;

        // We use a very large number to represent "unreachable".
        // We do not use int.MaxValue directly because later we may add a cost to it,
        // and that could overflow. So we choose a safely smaller large constant.
        const int INF = 1_000_000_000;

        // dp[day, participants] = minimum total cost needed to arrive at the start of "day"
        // having already accumulated "participants" participants.
        //
        // Why "start of day" states?
        // Because on each day we have two choices:
        // 1) Skip the day -> move to day + 1 with same participants and same cost.
        // 2) Run a workshop on this day -> gain participants, pay cost, and jump forward
        //    to the next allowed day after the cooldown.
        //
        // This state definition makes the cooldown transition very natural.
        int[,] dp = new int[n + 1, target + 1];

        // Initialize every state as unreachable.
        for (int day = 0; day <= n; day++)
        {
            for (int participants = 0; participants <= target; participants++)
            {
                dp[day, participants] = INF;
            }
        }

        // Base case:
        // Before day 0 starts, we have 0 participants and 0 cost.
        dp[0, 0] = 0;

        // Process days from left to right.
        for (int day = 0; day < n; day++)
        {
            for (int participants = 0; participants <= target; participants++)
            {
                // If this state is unreachable, there is nothing to transition from.
                if (dp[day, participants] == INF)
                {
                    continue;
                }

                int currentCost = dp[day, participants];

                // ------------------------------------------------------------
                // Option 1: Skip this day
                // ------------------------------------------------------------
                //
                // What are we doing?
                // We choose not to run a workshop on this day.
                //
                // Why is this necessary?
                // Because the optimal answer may require leaving some days unused.
                // Dynamic programming must consider all valid choices, including skipping.
                //
                // What changes?
                // - Day advances by 1
                // - Participants stay the same
                // - Cost stays the same
                if (currentCost < dp[day + 1, participants])
                {
                    dp[day + 1, participants] = currentCost;
                }

                // ------------------------------------------------------------
                // Option 2: Run a workshop on this day
                // ------------------------------------------------------------
                //
                // What are we doing?
                // We schedule a workshop on "day".
                //
                // Why is this necessary?
                // This is how we gain participants toward the target.
                //
                // What changes?
                // - We gain value[day] participants
                // - We pay cost[day]
                // - We must skip the next cooldown[day] days
                // - Therefore the next day we are allowed to consider is:
                //     day + cooldown[day] + 1
                //
                // Since the problem says "at least target" participants is enough,
                // any total above target can be capped down to target.
                int nextParticipants = Math.Min(target, participants + value[day]);
                int nextDay = day + cooldown[day] + 1;

                // If the cooldown jump goes beyond the calendar,
                // we can treat it as arriving at day n, meaning "after all days".
                if (nextDay > n)
                {
                    nextDay = n;
                }

                int newCost = currentCost + cost[day];

                if (newCost < dp[nextDay, nextParticipants])
                {
                    dp[nextDay, nextParticipants] = newCost;
                }
            }
        }

        // After processing all days, the answer is the minimum cost among all states
        // that are at day n (meaning no more days remain) with participants == target.
        //
        // Because we capped all totals at target, dp[*, target] already represents
        // "reached at least target".
        int answer = dp[n, target];

        return answer == INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] value1 = { 6, 4, 7, 3 };
int[] cost1 = { 5, 2, 6, 2 };
int[] cooldown1 = { 1, 0, 2, 0 };
int target1 = 10;

int result1 = solution.MinCostToReachTarget(value1, cost1, cooldown1, target1);
Console.WriteLine(result1); // Correct result based on the actual rules: 8

// Example 2
int[] value2 = { 5, 8, 4 };
int[] cost2 = { 4, 9, 3 };
int[] cooldown2 = { 2, 1, 0 };
int target2 = 13;

int result2 = solution.MinCostToReachTarget(value2, cost2, cooldown2, target2);
Console.WriteLine(result2); // Correct result: -1

// Additional quick check from the prompt's note:
// If target were 8 in Example 2, choosing day 1 alone gives 8 participants for cost 9.
int target3 = 8;
int result3 = solution.MinCostToReachTarget(value2, cost2, cooldown2, target3);
Console.WriteLine(result3); // 9