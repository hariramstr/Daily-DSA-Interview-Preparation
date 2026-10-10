/*
Title: Minimum Cost to Schedule Study Topics With Revision Gaps

Problem Description:
You are preparing for an exam over the next n days. On day i, you may either study exactly one topic or skip the day.
There are m topics, numbered from 0 to m - 1. Studying topic j on day i gives you a learning cost cost[i][j].
Lower cost means that topic is easier to study on that day because of your energy, available notes, or class schedule.

However, to retain information properly, each topic j has a required revision gap gap[j].
If you study topic j on some day d, then the next time you study the same topic must be at least gap[j] + 1 days later.
In other words, if you last studied topic j on day p, then you may study it again on day d only if d - p > gap[j].

You are also given an array need where need[j] is the exact number of times topic j must be studied by the end of day n - 1.
You may skip any number of days, but all required study sessions must be completed.
Return the minimum total learning cost, or -1 if it is impossible.

Constraints:
- 1 <= n <= 30
- 1 <= m <= 5
- 0 <= need[j] <= n
- 0 <= gap[j] <= n
- 1 <= cost[i][j] <= 10^4
- Sum of need[j] over all topics is at most n
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let S = product over all topics j of (need[j] + 1), which is the number of possible "how many times studied so far" states.
    Let G = product over all topics j of (min(gap[j], n) + 2), which is the number of possible cooldown/age states.
    The dynamic programming explores at most O(n * S * G * (m + 1)) transitions.

    Because:
    - n <= 30
    - m <= 5
    - total required sessions <= n
    this state-space approach is practical for the intended small-to-moderate number of topics.

    Space Complexity:
    O(S * G) for the DP dictionary of states for one day, plus the next-day dictionary.
    */
    public int MinimumCost(int n, int m, int[][] cost, int[] need, int[] gap)
    {
        // -----------------------------
        // STEP 1: Quick feasibility checks
        // -----------------------------
        // Before doing any dynamic programming, we can reject obviously impossible cases.
        // This is not required for correctness, but it helps avoid unnecessary work.
        //
        // First, if the total number of required study sessions is more than the number of days,
        // then it is impossible because we can study at most one topic per day.
        int totalNeed = need.Sum();
        if (totalNeed > n)
        {
            return -1;
        }

        // For each topic independently, check whether it can fit into n days even if no other topics existed.
        // If a topic must be studied need[j] times, and each pair of consecutive studies must be more than gap[j] apart,
        // then the minimum span needed is:
        //
        // first study day
        // then each next study must be at least gap[j] + 1 days later
        //
        // So the earliest possible schedule uses days:
        // d, d + (gap[j] + 1), d + 2*(gap[j] + 1), ...
        //
        // The total span from first to last is (need[j] - 1) * (gap[j] + 1).
        // This must fit within n days, meaning:
        // (need[j] - 1) * (gap[j] + 1) <= n - 1
        for (int j = 0; j < m; j++)
        {
            if (need[j] > 0)
            {
                long minSpan = (long)(need[j] - 1) * (gap[j] + 1);
                if (minSpan > n - 1)
                {
                    return -1;
                }
            }
        }

        // -----------------------------
        // STEP 2: State representation design
        // -----------------------------
        // We process days from left to right.
        //
        // At any day, to make future decisions correctly, we need to know:
        // 1) How many times each topic has already been studied.
        // 2) For each topic, whether it is currently allowed to be studied today.
        //
        // For (1), we store count[j] = number of completed study sessions for topic j so far.
        //
        // For (2), we need enough information about the last study day of each topic.
        // A convenient way is to store an "age" value:
        //
        // age[j] = number of days since topic j was last studied, capped at gap[j] + 1
        //
        // Interpretation:
        // - If topic j has never been studied, we also treat it as "available now".
        // - If age[j] > gap[j], then topic j may be studied today.
        // - If age[j] <= gap[j], then topic j is still in its mandatory waiting period.
        //
        // To keep the state finite and compact:
        // - We encode "available / never studied / sufficiently old" as age = gap[j] + 1
        // - Otherwise age is in [0 .. gap[j]]
        //
        // So each topic has exactly gap[j] + 2 possible age values:
        // 0, 1, 2, ..., gap[j], gap[j] + 1
        //
        // This is enough because once the age exceeds gap[j], all larger values behave the same:
        // the topic is simply available again.
        //
        // We will encode the full multi-topic state into a single long integer so it can be used
        // as a dictionary key efficiently.

        // -----------------------------
        // STEP 3: Precompute mixed-radix bases for compact encoding
        // -----------------------------
        // We encode:
        // - counts first
        // - then ages
        //
        // Mixed-radix encoding means each position has its own base.
        // For counts, baseCount[j] = need[j] + 1 because count can be 0..need[j].
        // For ages,  baseAge[j]   = gap[j] + 2 because age can be 0..gap[j]+1.
        //
        // This lets us pack all values into one long key and later decode them.
        long[] countMultiplier = new long[m];
        long[] ageMultiplier = new long[m];

        long currentBase = 1;
        for (int j = 0; j < m; j++)
        {
            countMultiplier[j] = currentBase;
            currentBase *= (need[j] + 1L);
        }

        for (int j = 0; j < m; j++)
        {
            ageMultiplier[j] = currentBase;
            currentBase *= (gap[j] + 2L);
        }

        // -----------------------------
        // STEP 4: Build the initial state
        // -----------------------------
        // Initially:
        // - no topic has been studied yet => count[j] = 0
        // - every topic is available to study immediately
        //
        // As explained above, "available" is represented by age[j] = gap[j] + 1.
        long initialState = 0;
        for (int j = 0; j < m; j++)
        {
            int initialAge = gap[j] + 1;
            initialState += ageMultiplier[j] * initialAge;
        }

        // DP dictionary:
        // key   = encoded state
        // value = minimum total cost to reach this state after processing some number of days
        //
        // We only keep states for the current day frontier, then build the next day's frontier.
        var dp = new Dictionary<long, int>
        {
            [initialState] = 0
        };

        // Reusable arrays to avoid repeated allocations during decoding/transition generation.
        int[] counts = new int[m];
        int[] ages = new int[m];
        int[] nextCounts = new int[m];
        int[] nextAges = new int[m];

        // -----------------------------
        // STEP 5: Process each day
        // -----------------------------
        // On each day, from every current state, we have:
        // - one "skip" transition
        // - up to m "study topic j" transitions, if allowed and still needed
        //
        // We update the DP to represent all possibilities after this day.
        for (int day = 0; day < n; day++)
        {
            var nextDp = new Dictionary<long, int>();

            foreach (var entry in dp)
            {
                long state = entry.Key;
                int currentCost = entry.Value;

                // -----------------------------------------
                // STEP 5A: Decode the compact state
                // -----------------------------------------
                // We need the per-topic counts and ages in normal array form
                // so we can reason about valid actions.
                DecodeState(state, m, need, gap, countMultiplier, ageMultiplier, counts, ages);

                // -----------------------------------------
                // STEP 5B: Optional pruning by remaining days
                // -----------------------------------------
                // If from this state there are more required sessions left than remaining days,
                // then this state can never lead to a complete schedule.
                int doneSoFar = 0;
                for (int j = 0; j < m; j++)
                {
                    doneSoFar += counts[j];
                }

                int remainingSessions = totalNeed - doneSoFar;
                int remainingDaysIncludingToday = n - day;

                if (remainingSessions > remainingDaysIncludingToday)
                {
                    continue;
                }

                // -----------------------------------------
                // STEP 5C: Transition 1 = Skip today
                // -----------------------------------------
                // If we skip:
                // - counts do not change
                // - every age increases by 1, but is capped at gap[j] + 1
                //
                // Why cap?
                // Because any age larger than gap[j] behaves the same: the topic is available.
                for (int j = 0; j < m; j++)
                {
                    nextCounts[j] = counts[j];
                    nextAges[j] = Math.Min(gap[j] + 1, ages[j] + 1);
                }

                long skipState = EncodeState(m, need, gap, countMultiplier, ageMultiplier, nextCounts, nextAges);
                Relax(nextDp, skipState, currentCost);

                // -----------------------------------------
                // STEP 5D: Transition 2 = Study one topic today
                // -----------------------------------------
                // We try each topic j as the topic to study today.
                for (int topic = 0; topic < m; topic++)
                {
                    // We cannot study topic if we already completed all required sessions for it.
                    if (counts[topic] >= need[topic])
                    {
                        continue;
                    }

                    // Topic is allowed today only if its age is strictly greater than gap[topic].
                    // In our capped representation, that means age == gap[topic] + 1.
                    if (ages[topic] <= gap[topic])
                    {
                        continue;
                    }

                    // Build the next state after studying this topic today.
                    //
                    // Counts:
                    // - selected topic count increases by 1
                    // - all others stay the same
                    //
                    // Ages:
                    // - selected topic age becomes 0 because it was just studied today
                    // - all other topic ages increase by 1, capped
                    for (int j = 0; j < m; j++)
                    {
                        nextCounts[j] = counts[j];
                        nextAges[j] = Math.Min(gap[j] + 1, ages[j] + 1);
                    }

                    nextCounts[topic]++;
                    nextAges[topic] = 0;

                    // Additional pruning:
                    // After taking this action, check if the remaining sessions can still fit
                    // into the remaining future days.
                    int newDone = doneSoFar + 1;
                    int newRemainingSessions = totalNeed - newDone;
                    int futureDays = n - (day + 1);

                    if (newRemainingSessions > futureDays)
                    {
                        continue;
                    }

                    long studyState = EncodeState(m, need, gap, countMultiplier, ageMultiplier, nextCounts, nextAges);
                    int newCost = currentCost + cost[day][topic];
                    Relax(nextDp, studyState, newCost);
                }
            }

            dp = nextDp;
        }

        // -----------------------------
        // STEP 6: Extract the answer
        // -----------------------------
        // After processing all n days, we need a state where every topic has been studied exactly need[j] times.
        // Ages can be anything, because only the final counts matter.
        int answer = int.MaxValue;

        foreach (var entry in dp)
        {
            long state = entry.Key;
            int totalCostForState = entry.Value;

            DecodeCountsOnly(state, m, need, countMultiplier, counts);

            bool complete = true;
            for (int j = 0; j < m; j++)
            {
                if (counts[j] != need[j])
                {
                    complete = false;
                    break;
                }
            }

            if (complete)
            {
                answer = Math.Min(answer, totalCostForState);
            }
        }

        return answer == int.MaxValue ? -1 : answer;
    }

    private static void Relax(Dictionary<long, int> dp, long state, int cost)
    {
        if (dp.TryGetValue(state, out int existing))
        {
            if (cost < existing)
            {
                dp[state] = cost;
            }
        }
        else
        {
            dp[state] = cost;
        }
    }

    private static long EncodeState(
        int m,
        int[] need,
        int[] gap,
        long[] countMultiplier,
        long[] ageMultiplier,
        int[] counts,
        int[] ages)
    {
        long state = 0;

        for (int j = 0; j < m; j++)
        {
            state += countMultiplier[j] * counts[j];
        }

        for (int j = 0; j < m; j++)
        {
            state += ageMultiplier[j] * ages[j];
        }

        return state;
    }

    private static void DecodeState(
        long state,
        int m,
        int[] need,
        int[] gap,
        long[] countMultiplier,
        long[] ageMultiplier,
        int[] counts,
        int[] ages)
    {
        // Decode counts.
        for (int j = 0; j < m; j++)
        {
            counts[j] = (int)((state / countMultiplier[j]) % (need[j] + 1L));
        }

        // Decode ages.
        for (int j = 0; j < m; j++)
        {
            ages[j] = (int)((state / ageMultiplier[j]) % (gap[j] + 2L));
        }
    }

    private static void DecodeCountsOnly(
        long state,
        int m,
        int[] need,
        long[] countMultiplier,
        int[] counts)
    {
        for (int j = 0; j < m; j++)
        {
            counts[j] = (int)((state / countMultiplier[j]) % (need[j] + 1L));
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Example 1
int n1 = 5;
int m1 = 2;
int[][] cost1 =
{
    new[] { 3, 8 },
    new[] { 2, 5 },
    new[] { 4, 1 },
    new[] { 6, 3 },
    new[] { 2, 7 }
};
int[] need1 = { 2, 1 };
int[] gap1 = { 1, 0 };

var solution = new Solution();
int result1 = solution.MinimumCost(n1, m1, cost1, need1, gap1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int n2 = 4;
int m2 = 2;
int[][] cost2 =
{
    new[] { 5, 2 },
    new[] { 4, 3 },
    new[] { 3, 6 },
    new[] { 2, 1 }
};
int[] need2 = { 2, 2 };
int[] gap2 = { 2, 1 };

int result2 = solution.MinimumCost(n2, m2, cost2, need2, gap2);
Console.WriteLine(result2); // Expected: -1