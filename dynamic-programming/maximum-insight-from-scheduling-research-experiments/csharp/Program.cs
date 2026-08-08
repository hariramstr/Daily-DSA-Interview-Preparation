/*
Title: Maximum Insight from Scheduling Research Experiments

Problem Description:
A research lab has planned n experiments over the next several days. Experiment i must be started on or before day deadline[i], requires exactly duration[i] consecutive days to complete, and yields insight[i] points if it is fully completed by its deadline. Only one experiment can run on any given day, and once an experiment starts, it cannot be interrupted.

You may choose any subset of experiments and schedule them in any order, as long as every chosen experiment finishes no later than its own deadline. Your task is to compute the maximum total insight that can be obtained.

Unlike simple interval scheduling, each experiment can be reordered relative to the others, and feasibility depends on the total occupied time before each chosen deadline. This makes greedy choices insufficient in many cases.

Return the maximum possible sum of insight points.

Constraints:
- 1 <= n <= 200
- 1 <= duration[i] <= 200
- 1 <= deadline[i] <= 2000
- 1 <= insight[i] <= 10^6
- The answer fits in a 64-bit signed integer.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private sealed class Experiment
    {
        public int Duration;
        public int Deadline;
        public long Insight;
    }

    /*
    Time Complexity:
    - Sorting the experiments by deadline takes O(n log n)
    - The dynamic programming transition processes each experiment across all possible times up to maxDeadline
      which takes O(n * maxDeadline)
    - Overall: O(n log n + n * maxDeadline)

    Space Complexity:
    - We use a 1-dimensional DP array of size maxDeadline + 1
    - Overall: O(maxDeadline)

    Beginner-friendly idea:
    We sort experiments by deadline, then use dynamic programming where:
    dp[t] = maximum total insight achievable if the total occupied time is exactly t days
            after considering some prefix of experiments, while keeping all chosen experiments feasible.

    Why sorting by deadline works:
    If a set of jobs can be scheduled feasibly on a single machine with deadlines and durations,
    then scheduling them in nondecreasing deadline order is sufficient to test feasibility.
    So after sorting by deadline, when we decide to place an experiment as the "last chosen one so far",
    we only need to ensure its completion time t does not exceed its own deadline.
    */
    public long MaxInsight(int[] duration, int[] deadline, int[] insight)
    {
        int n = duration.Length;

        // Step 1:
        // Build a list of experiment objects so the three separate arrays become easier to work with.
        // This improves readability because each experiment's duration, deadline, and insight stay together.
        var experiments = new List<Experiment>(n);
        int maxDeadline = 0;

        for (int i = 0; i < n; i++)
        {
            experiments.Add(new Experiment
            {
                Duration = duration[i],
                Deadline = deadline[i],
                Insight = insight[i]
            });

            if (deadline[i] > maxDeadline)
            {
                maxDeadline = deadline[i];
            }
        }

        // Step 2:
        // Sort experiments by increasing deadline.
        //
        // Why this is necessary:
        // In scheduling problems with processing times and deadlines on one machine,
        // if a chosen subset is feasible, then there exists a feasible order by nondecreasing deadlines.
        // That means after sorting, when we process experiments from left to right,
        // we can safely think of the current experiment as being scheduled after previously chosen ones.
        experiments.Sort((a, b) => a.Deadline.CompareTo(b.Deadline));

        // Step 3:
        // Create the DP array.
        //
        // dp[t] means:
        // "The maximum total insight we can obtain using some subset of the experiments processed so far,
        //  such that the total time used is exactly t days, and all chosen experiments meet their deadlines."
        //
        // We initialize all states as impossible except dp[0] = 0.
        //
        // We use a very negative number to represent "impossible".
        long impossible = long.MinValue / 4;
        long[] dp = new long[maxDeadline + 1];
        Array.Fill(dp, impossible);
        dp[0] = 0;

        // Step 4:
        // Process each experiment one by one.
        foreach (var exp in experiments)
        {
            // We iterate time backwards.
            //
            // Why backwards?
            // This is the standard 0/1 knapsack technique.
            // It ensures each experiment is used at most once.
            //
            // If we iterated forward, we might accidentally use the same experiment multiple times
            // within the same iteration.
            for (int t = exp.Deadline; t >= exp.Duration; t--)
            {
                // If dp[t - exp.Duration] is impossible, then we cannot build a valid schedule
                // that uses exactly (t - duration) days before adding this experiment.
                if (dp[t - exp.Duration] == impossible)
                {
                    continue;
                }

                // Candidate value if we choose this experiment and make it finish exactly at time t.
                //
                // Why is checking t <= exp.Deadline enough?
                // Because the loop only considers t up to exp.Deadline.
                // Since experiments are processed in sorted deadline order,
                // the previous chosen experiments already form a feasible schedule within t - duration days,
                // and placing this experiment last makes it finish at time t.
                long candidate = dp[t - exp.Duration] + exp.Insight;

                // Keep the better of:
                // - not taking this experiment for total time t
                // - taking this experiment and ending at time t
                if (candidate > dp[t])
                {
                    dp[t] = candidate;
                }
            }
        }

        // Step 5:
        // The answer is the best value among all feasible finishing times.
        //
        // We do not require using all available time.
        // Any exact total time t from 0 to maxDeadline is acceptable.
        long answer = 0;
        for (int t = 0; t <= maxDeadline; t++)
        {
            if (dp[t] > answer)
            {
                answer = dp[t];
            }
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
// Note:
// The textual explanation in the prompt is inconsistent, but the mathematically correct answer
// for these arrays is 11:
// - Choose experiment with duration 1, deadline 2, insight 4
// - Then choose experiment with duration 2, deadline 3, insight 7
// Total time = 3, both meet deadlines, total insight = 11
//
// The DP below correctly computes the true optimum for the given arrays.
int[] duration1 = { 2, 1, 2 };
int[] deadline1 = { 2, 2, 3 };
int[] insight1 = { 8, 4, 7 };
long result1 = solution.MaxInsight(duration1, deadline1, insight1);
Console.WriteLine(result1);

// Example 2
int[] duration2 = { 3, 1, 2, 2 };
int[] deadline2 = { 3, 4, 5, 6 };
int[] insight2 = { 10, 3, 9, 8 };
long result2 = solution.MaxInsight(duration2, deadline2, insight2);
Console.WriteLine(result2);