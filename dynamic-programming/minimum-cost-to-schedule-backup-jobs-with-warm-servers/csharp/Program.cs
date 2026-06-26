/*
Title: Minimum Cost to Schedule Backup Jobs with Warm Servers

Problem Description:
A company must run a sequence of nightly backup jobs in the given order. There are n jobs, and job i requires load[i] units of work. The jobs are processed by a single server pool that can be in one of two states before each job starts: cold or warm.

If the server pool is cold before job i, processing that job costs coldCost[i]. After the job finishes, the server becomes warm. If the server pool is already warm before job i, processing that job costs warmCost[i]. However, keeping the server warm between two consecutive jobs incurs an idle maintenance cost keep[i], which is paid only if you choose to keep the server warm after finishing job i so that job i+1 can start warm. At any point after finishing a job, you may instead shut the server down for free, causing the next job to start cold.

Your task is to compute the minimum total cost to process all jobs in order.

Formally, for each job i:
- If job i starts cold, pay coldCost[i].
- If job i starts warm, pay warmCost[i].
- After job i, for i < n - 1, you may pay keep[i] to keep the server warm for the next job, or pay nothing and let it become cold.
- Before the first job, the server is cold.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 100000
- 1 <= coldCost[i], warmCost[i], keep[i] <= 10^9
- coldCost.length == warmCost.length == n
- keep.length == n - 1
- Jobs must be processed in the original order
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Explanation of the approach:
    We use dynamic programming with only two running states.

    Before each job i starts, the server can be in exactly one of two states:
    1. Cold
    2. Warm

    Let:
    - dpCold = minimum total cost so far such that the NEXT job starts cold
    - dpWarm = minimum total cost so far such that the NEXT job starts warm

    We process jobs from left to right.
    For each job, we compute the best possible cost after finishing that job, and then decide
    whether the next job should start cold or warm.

    Important observation:
    After any job finishes, the server is physically warm immediately.
    But for the next job, we may:
    - shut it down for free -> next job starts cold
    - pay keep[i] -> next job starts warm

    Since only the next starting state matters, we never need more than two numbers.
    */
    public long MinCost(int[] coldCost, int[] warmCost, int[] keep)
    {
        int n = coldCost.Length;

        // Before the very first job:
        // - The server is guaranteed to be cold.
        // So the cost for "job 0 starts cold" is 0 accumulated so far.
        // - It is impossible for job 0 to start warm, so we set that state to a very large number.
        long dpCold = 0;
        long dpWarm = long.MaxValue / 4;

        // We will process each job in order.
        for (int i = 0; i < n; i++)
        {
            // Step 1:
            // Compute the minimum cost to RUN the current job i depending on the state
            // the server is in BEFORE the job starts.
            //
            // If job i starts cold:
            //   total cost = previous "next job starts cold" state + coldCost[i]
            //
            // If job i starts warm:
            //   total cost = previous "next job starts warm" state + warmCost[i]
            //
            // These two values represent the cost immediately AFTER job i finishes,
            // before deciding whether to keep the server warm for the next job.
            long costIfStartCold = dpCold + coldCost[i];
            long costIfStartWarm = dpWarm + warmCost[i];

            // Step 2:
            // Among all ways to reach the end of job i, find the cheapest total cost.
            //
            // Why do we take the minimum here?
            // Because after job i finishes, the server is warm regardless of how the job started.
            // So from this point onward, only the total cost matters.
            long bestAfterRunningJob = Math.Min(costIfStartCold, costIfStartWarm);

            // Step 3:
            // If this is the last job, there is no "next job" to prepare for.
            // So the answer is simply the cheapest way to finish this final job.
            if (i == n - 1)
            {
                return bestAfterRunningJob;
            }

            // Step 4:
            // Decide the state before the NEXT job (job i+1).
            //
            // Option A: Shut down for free.
            // Then the next job starts cold.
            long nextDpCold = bestAfterRunningJob;

            // Option B: Keep the server warm by paying keep[i].
            // Then the next job starts warm.
            long nextDpWarm = bestAfterRunningJob + keep[i];

            // Step 5:
            // Move the DP window forward.
            //
            // These values now describe the minimum cost so far such that
            // the next job starts in the corresponding state.
            dpCold = nextDpCold;
            dpWarm = nextDpWarm;
        }

        // The loop always returns on the last iteration, so this line is unreachable.
        // It is included only to satisfy the compiler.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] coldCost1 = { 8, 7, 9 };
int[] warmCost1 = { 5, 3, 4 };
int[] keep1 = { 2, 6 };
long result1 = solution.MinCost(coldCost1, warmCost1, keep1);
Console.WriteLine(result1); // Expected: 18

// Example 2
int[] coldCost2 = { 4, 10, 6, 8 };
int[] warmCost2 = { 2, 1, 3, 2 };
int[] keep2 = { 7, 2, 10 };
long result2 = solution.MinCost(coldCost2, warmCost2, keep2);
Console.WriteLine(result2); // Correct result for these inputs: 19

// Additional small sanity check
int[] coldCost3 = { 5 };
int[] warmCost3 = { 1 };
int[] keep3 = Array.Empty<int>();
long result3 = solution.MinCost(coldCost3, warmCost3, keep3);
Console.WriteLine(result3); // Expected: 5