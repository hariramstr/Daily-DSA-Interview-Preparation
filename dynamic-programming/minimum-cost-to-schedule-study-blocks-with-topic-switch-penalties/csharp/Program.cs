/*
Title: Minimum Cost to Schedule Study Blocks With Topic Switch Penalties

Problem Description:
You are preparing a study plan for the next n days. On day i, you must study exactly one of m topics.
The effectiveness cost of studying topic j on day i is given by cost[i][j].

In addition, changing from one topic to another between consecutive days causes a mental context-switch penalty.
The penalty for switching from topic a on day i - 1 to topic b on day i is given by switchCost[a][b].
Staying on the same topic may also have a penalty, which is included in the matrix as switchCost[a][a].

Your task is to compute the minimum total cost to complete the full study plan over all n days.
The total cost is the sum of the daily study costs plus the switch penalties between consecutive days.
You may start with any topic on day 0, and there is no penalty before the first day.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 200
- 1 <= m <= 100
- cost.length == n
- cost[i].length == m
- switchCost.length == m
- switchCost[a].length == m
- 0 <= cost[i][j] <= 10^4
- 0 <= switchCost[a][b] <= 10^4
- The answer fits in a 64-bit signed integer.

Important note about the examples:
The written explanations in the prompt contain arithmetic inconsistencies.
For example 1, choosing topic 1 on all three days gives:
day 0: cost[0][1] = 1
day 1: switchCost[1][1] + cost[1][1] = 0 + 3
day 2: switchCost[1][1] + cost[2][1] = 0 + 2
Total = 1 + 0 + 3 + 0 + 2 = 6

For example 2, the true minimum computed by the required recurrence is 16:
topic sequence: 1 -> 1 -> 1 -> 0
Total = 3 + (0 + 7) + (0 + 2) + (1 + 5) = 18
But an even better sequence is 1 -> 0 -> 0 -> 0:
Total = 3 + (1 + 6) + (0 + 4) + (0 + 5) = 19
And the actual minimum over all possibilities is 16 with sequence 1 -> 1 -> 1 -> 1:
Total = 3 + (0 + 7) + (0 + 2) + (0 + 9) = 21? Not minimum.
Let's compute correctly by DP:
Day 0: [8, 3]
Day 1: topic 0 = min(8+0, 3+1)+6 = 10
       topic 1 = min(8+10, 3+0)+7 = 10
Day 2: topic 0 = min(10+0, 10+1)+4 = 14
       topic 1 = min(10+10, 10+0)+2 = 12
Day 3: topic 0 = min(14+0, 12+1)+5 = 18
       topic 1 = min(14+10, 12+0)+9 = 21
So the minimum for that exact input is 18, not 15.

Because correctness is mandatory, the algorithm below implements the mathematically correct dynamic programming solution.
The demo prints the computed answers for the provided inputs.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * m * m)
      Explanation:
      For each day after the first (there are n - 1 such days),
      for each current topic (m choices),
      we try every possible previous topic (m choices).
      That gives O(n * m^2).

    Space Complexity:
    - O(m)
      Explanation:
      We only keep the DP values for the previous day and the current day,
      instead of storing the entire n x m table.
    */
    public long MinTotalCost(int[][] cost, int[][] switchCost)
    {
        // ------------------------------------------------------------
        // Step 1: Read the basic dimensions of the problem.
        // ------------------------------------------------------------
        // n = number of days
        // m = number of topics
        //
        // We assume the input follows the constraints:
        // - cost has n rows
        // - each row has m columns
        // - switchCost is an m x m matrix
        //
        // These dimensions are needed so we know how large our DP arrays
        // should be and how many loops to run.
        int n = cost.Length;
        int m = cost[0].Length;

        // ------------------------------------------------------------
        // Step 2: Create the DP array for day 0.
        // ------------------------------------------------------------
        // Meaning of prev[topic]:
        // "The minimum total cost to finish all scheduling decisions up to
        // day 0, if we choose 'topic' on day 0."
        //
        // Since there is NO switch penalty before the first day,
        // the total cost for day 0 is simply the study cost on day 0.
        //
        // So:
        // prev[topic] = cost[0][topic]
        //
        // We use long instead of int because the total answer can grow large,
        // and the problem explicitly says the answer fits in a 64-bit signed integer.
        long[] prev = new long[m];

        for (int topic = 0; topic < m; topic++)
        {
            prev[topic] = cost[0][topic];
        }

        // ------------------------------------------------------------
        // Step 3: Process each later day one by one.
        // ------------------------------------------------------------
        // For every day from 1 to n - 1, we compute a new DP array "curr".
        //
        // Meaning of curr[currentTopic]:
        // "The minimum total cost to schedule days 0..day,
        // if we choose currentTopic on this day."
        //
        // To compute this, we must decide which topic was chosen yesterday.
        // If yesterday's topic was prevTopic, then:
        //
        // total cost =
        //   (best cost up to yesterday ending with prevTopic)
        //   + (switch penalty from prevTopic to currentTopic)
        //   + (study cost of currentTopic on this day)
        //
        // So the recurrence is:
        // curr[currentTopic] =
        //   min over all prevTopic:
        //     prev[prevTopic] + switchCost[prevTopic][currentTopic] + cost[day][currentTopic]
        //
        // This is the heart of the dynamic programming solution.
        for (int day = 1; day < n; day++)
        {
            long[] curr = new long[m];

            // --------------------------------------------------------
            // Step 3a: Compute the answer for each possible topic today.
            // --------------------------------------------------------
            for (int currentTopic = 0; currentTopic < m; currentTopic++)
            {
                // We want the minimum possible value among all previous topics.
                // Start with a very large number so any real candidate will be smaller.
                long best = long.MaxValue;

                // ----------------------------------------------------
                // Step 3b: Try every possible topic from the previous day.
                // ----------------------------------------------------
                // This brute-force transition is acceptable because:
                // n <= 200 and m <= 100
                // Therefore O(n * m * m) is fast enough.
                for (int prevTopic = 0; prevTopic < m; prevTopic++)
                {
                    // Candidate total if we came from prevTopic yesterday
                    // and choose currentTopic today.
                    long candidate =
                        prev[prevTopic] +
                        switchCost[prevTopic][currentTopic] +
                        cost[day][currentTopic];

                    // Keep the smallest candidate because we want
                    // the minimum total cost.
                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                // After checking all possible previous topics,
                // "best" is the true minimum cost for ending today
                // with currentTopic.
                curr[currentTopic] = best;
            }

            // --------------------------------------------------------
            // Step 3c: Move to the next day.
            // --------------------------------------------------------
            // The current day's results become the "previous day" results
            // for the next iteration.
            prev = curr;
        }

        // ------------------------------------------------------------
        // Step 4: Extract the final answer.
        // ------------------------------------------------------------
        // After processing all days, prev[topic] means:
        // "Minimum total cost to finish all n days, ending with topic."
        //
        // The problem does NOT require us to end with any specific topic,
        // so we simply take the minimum over all ending topics.
        long answer = long.MaxValue;

        for (int topic = 0; topic < m; topic++)
        {
            if (prev[topic] < answer)
            {
                answer = prev[topic];
            }
        }

        // ------------------------------------------------------------
        // Step 5: Return the minimum total cost.
        // ------------------------------------------------------------
        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[][] cost1 =
{
    new[] { 4, 1, 7 },
    new[] { 2, 3, 6 },
    new[] { 5, 2, 4 }
};

int[][] switchCost1 =
{
    new[] { 0, 5, 2 },
    new[] { 3, 0, 4 },
    new[] { 1, 2, 0 }
};

long result1 = solution.MinTotalCost(cost1, switchCost1);
Console.WriteLine("Example 1 Result: " + result1); // Expected mathematically correct result: 6

// Example 2
int[][] cost2 =
{
    new[] { 8, 3 },
    new[] { 6, 7 },
    new[] { 4, 2 },
    new[] { 5, 9 }
};

int[][] switchCost2 =
{
    new[] { 0, 10 },
    new[] { 1, 0 }
};

long result2 = solution.MinTotalCost(cost2, switchCost2);
Console.WriteLine("Example 2 Result: " + result2); // For the exact given input, the correct DP result is 18

// Additional small sanity check
int[][] cost3 =
{
    new[] { 5 }
};

int[][] switchCost3 =
{
    new[] { 7 }
};

long result3 = solution.MinTotalCost(cost3, switchCost3);
Console.WriteLine("Single Topic / Single Day Result: " + result3); // Expected: 5