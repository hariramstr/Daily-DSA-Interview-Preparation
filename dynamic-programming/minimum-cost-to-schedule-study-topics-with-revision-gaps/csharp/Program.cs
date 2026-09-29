/*
Title: Minimum Cost to Schedule Study Topics with Revision Gaps
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You are preparing a study plan for an exam over n days. On day i, you must study exactly one topic chosen from m possible topics. The cost of studying topic j on day i is given by costs[i][j]. However, repeatedly studying the same topic too soon is mentally exhausting, so a topic can only be chosen again if at least gap[j] full days have passed since the last day that same topic was studied.

More formally, if topic j is studied on day a and again on day b where a < b, then b - a - 1 must be at least gap[j]. Equivalently, topic j cannot be used again within the next gap[j] days after it is chosen.

Return the minimum total cost to complete all n days, or -1 if it is impossible to build a valid schedule.

This is a dynamic programming problem because the best choice for the current day depends on which topics were used recently and when they become available again. A correct solution should efficiently explore valid schedules without brute-forcing all possible sequences.

Constraints:
- 1 <= n <= 100
- 1 <= m <= 8
- costs.length == n
- costs[i].length == m
- 1 <= costs[i][j] <= 10^4
- 0 <= gap[j] <= 7

Important note about Example 1:
The textual explanation in the prompt contains an inconsistency:
- It claims the schedule topic 0, topic 1, topic 1, topic 0 is valid with total cost 10.
- But topic 0 has gap 1, so using topic 0 on day 0 and day 3 is valid, while topic 1 has gap 0, so consecutive use is allowed.
- The cost of topic 0, topic 1, topic 1, topic 0 is actually 3 + 2 + 4 + 1 = 10, which is valid.
So the expected output 10 is correct.

We will implement a dynamic programming solution that tracks, for each topic, how many more days it remains blocked before it can be used again.
Because:
- m <= 8
- each gap[j] <= 7
the state space is manageable.

State idea:
For each day, the DP state stores a vector "cooldowns":
cooldowns[j] = how many more days topic j must wait before it can be chosen today.
- cooldowns[j] == 0 means topic j is available today.
- cooldowns[j] > 0 means topic j is still blocked.

Transition:
If we choose topic t today, then for tomorrow:
- topic t gets cooldown = gap[t]
- every other topic's cooldown decreases by 1, but not below 0

This exactly models the rule:
after using topic t, it cannot be used for the next gap[t] full days.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    Let S be the number of reachable cooldown states.
    For each day, for each reachable state, we try all m topics.
    So the complexity is O(n * S * m).

    Because m <= 8 and each cooldown value is in [0..7], the theoretical maximum
    number of states is at most 8^8 = 16,777,216, but in practice the reachable
    state count is much smaller due to the transition structure and small n <= 100.

    Space Complexity:
    O(S), because for each day we only keep the current layer of DP states and the next layer.
    */
    public int MinCostSchedule(int[][] costs, int[] gap)
    {
        int n = costs.Length;
        int m = gap.Length;

        // We use a dictionary for dynamic programming over states.
        //
        // Key:
        //   a compact string encoding of the cooldown array
        //
        // Value:
        //   the minimum total cost needed to reach that exact cooldown state
        //   after processing some number of days.
        //
        // Why a dictionary?
        // - We do not want to allocate a giant multidimensional array for all possible states.
        // - Many states are never reached.
        // - A dictionary lets us store only states that actually appear.
        var current = new Dictionary<string, int>();

        // At the very beginning, before day 0, no topic has been used yet.
        // Therefore every topic is immediately available.
        //
        // So the initial cooldown vector is all zeros.
        int[] initialCooldowns = new int[m];
        string initialKey = Encode(initialCooldowns);

        // Cost is 0 because we have not studied anything yet.
        current[initialKey] = 0;

        // Process days one by one.
        for (int day = 0; day < n; day++)
        {
            // next will store all states reachable after making a choice for this day.
            var next = new Dictionary<string, int>();

            // We iterate through every DP state that is reachable at the start of this day.
            foreach (var entry in current)
            {
                string stateKey = entry.Key;
                int costSoFar = entry.Value;

                // Decode the compact string back into the cooldown array
                // so we can inspect which topics are available today.
                int[] cooldowns = Decode(stateKey, m);

                // Try choosing each topic for the current day.
                for (int topic = 0; topic < m; topic++)
                {
                    // A topic can be chosen today only if its cooldown is 0.
                    // That means it is not blocked by recent usage.
                    if (cooldowns[topic] != 0)
                    {
                        continue;
                    }

                    // Build the cooldown state for the NEXT day after choosing this topic today.
                    //
                    // Step 1:
                    // Every topic that is currently blocked moves one day closer to becoming available.
                    // So we decrease each positive cooldown by 1.
                    //
                    // Why do this?
                    // Because one full day (today) has now passed.
                    int[] nextCooldowns = new int[m];
                    for (int j = 0; j < m; j++)
                    {
                        if (cooldowns[j] > 0)
                        {
                            nextCooldowns[j] = cooldowns[j] - 1;
                        }
                        else
                        {
                            nextCooldowns[j] = 0;
                        }
                    }

                    // Step 2:
                    // The topic we choose today becomes blocked for the next gap[topic] days.
                    //
                    // Important subtle point:
                    // The cooldown array we store is always interpreted "at the start of the next day".
                    // If gap[topic] = 0, then it is immediately available tomorrow.
                    // If gap[topic] = 1, then it is blocked tomorrow and available the day after.
                    // Therefore setting nextCooldowns[topic] = gap[topic] is exactly correct.
                    nextCooldowns[topic] = gap[topic];

                    // Compute the new total cost after studying this topic today.
                    int newCost = costSoFar + costs[day][topic];

                    // Encode the next cooldown vector so it can be used as a dictionary key.
                    string nextKey = Encode(nextCooldowns);

                    // Standard DP relaxation:
                    // If this state has not been seen before, store it.
                    // If it has been seen, keep only the smaller cost.
                    if (!next.TryGetValue(nextKey, out int existingCost) || newCost < existingCost)
                    {
                        next[nextKey] = newCost;
                    }
                }
            }

            // Move to the next day.
            current = next;

            // Early exit:
            // If no states are reachable after some day, then it is impossible
            // to complete the schedule.
            if (current.Count == 0)
            {
                return -1;
            }
        }

        // After processing all n days, every remaining state represents a valid full schedule.
        // We simply take the minimum cost among them.
        int answer = int.MaxValue;
        foreach (var entry in current)
        {
            if (entry.Value < answer)
            {
                answer = entry.Value;
            }
        }

        return answer == int.MaxValue ? -1 : answer;
    }

    // Encodes the cooldown array into a compact string key.
    //
    // Since each cooldown is between 0 and 7, we can safely store each value
    // as a single character '0'..'7'.
    //
    // Example:
    // [0, 2, 1] -> "021"
    private string Encode(int[] cooldowns)
    {
        char[] chars = new char[cooldowns.Length];
        for (int i = 0; i < cooldowns.Length; i++)
        {
            chars[i] = (char)('0' + cooldowns[i]);
        }
        return new string(chars);
    }

    // Decodes the string key back into the cooldown array.
    private int[] Decode(string key, int m)
    {
        int[] cooldowns = new int[m];
        for (int i = 0; i < m; i++)
        {
            cooldowns[i] = key[i] - '0';
        }
        return cooldowns;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] costs1 =
{
    new[] { 3, 8 },
    new[] { 5, 2 },
    new[] { 6, 4 },
    new[] { 1, 7 }
};
int[] gap1 = { 1, 0 };
int result1 = solution.MinCostSchedule(costs1, gap1);
Console.WriteLine(result1); // Expected: 10

// Example 2
int[][] costs2 =
{
    new[] { 4, 1 },
    new[] { 2, 3 },
    new[] { 5, 6 }
};
int[] gap2 = { 2, 2 };
int result2 = solution.MinCostSchedule(costs2, gap2);
Console.WriteLine(result2); // Expected: -1

// Additional small sanity check
int[][] costs3 =
{
    new[] { 5, 1, 9 },
    new[] { 4, 2, 8 },
    new[] { 3, 7, 1 }
};
int[] gap3 = { 0, 1, 0 };
int result3 = solution.MinCostSchedule(costs3, gap3);
Console.WriteLine(result3);