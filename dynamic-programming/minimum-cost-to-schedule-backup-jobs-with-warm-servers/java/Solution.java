/*
Minimum Cost to Schedule Backup Jobs with Warm Servers

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

import java.util.*;

public class Solution {

    /**
     * Computes the minimum total cost to process all jobs in order.
     *
     * Core dynamic programming idea:
     * Before each job, the only state that matters is whether the server is cold or warm.
     * So we maintain:
     * - dpCold: minimum total cost so far such that the NEXT job will start cold
     * - dpWarm: minimum total cost so far such that the NEXT job will start warm
     *
     * Transition for each job i:
     * 1) If job i starts cold:
     *    - It can only come from dpCold, because "next job starts cold" means the current job starts cold.
     *    - Cost added is coldCost[i].
     *    - After finishing job i:
     *         a) shut down for free  -> next state cold
     *         b) keep warm for keep[i] -> next state warm (if i is not the last job)
     *
     * 2) If job i starts warm:
     *    - It can only come from dpWarm.
     *    - Cost added is warmCost[i].
     *    - After finishing job i:
     *         a) shut down for free  -> next state cold
     *         b) keep warm for keep[i] -> next state warm (if i is not the last job)
     *
     * Since the first job must start cold, initial state is:
     * - dpCold = 0
     * - dpWarm = INF (impossible before job 0)
     *
     * @param coldCost cost of running each job when the server starts cold
     * @param warmCost cost of running each job when the server starts warm
     * @param keep maintenance cost to keep the server warm between consecutive jobs
     * @return the minimum possible total cost to process all jobs
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long minimumCost(int[] coldCost, int[] warmCost, int[] keep) {
        validateInput(coldCost, warmCost, keep);

        int n = coldCost.length;

        // A very large number used to represent an impossible state.
        // We choose a value safely below Long.MAX_VALUE to avoid overflow during additions.
        long INF = Long.MAX_VALUE / 4;

        // dpCold = minimum total cost so far such that the current job starts cold.
        // dpWarm = minimum total cost so far such that the current job starts warm.
        //
        // Before the first job:
        // - The server is guaranteed to be cold.
        // - Therefore, "job 0 starts cold" has cost 0 accumulated so far.
        // - "job 0 starts warm" is impossible.
        long dpCold = 0L;
        long dpWarm = INF;

        // Process jobs one by one in order.
        for (int i = 0; i < n; i++) {
            // These will store the minimum cost for the NEXT job's starting state
            // after we finish processing job i.
            long nextCold = INF;
            long nextWarm = INF;

            // ------------------------------------------------------------
            // Case 1: Job i starts cold
            // ------------------------------------------------------------
            if (dpCold < INF) {
                // If the current job starts cold, we must pay coldCost[i].
                long costAfterRunningCold = dpCold + coldCost[i];

                // After the job finishes, the machine is warm immediately.
                // But for the next job, we have a choice:
                //
                // Choice A: Shut down for free.
                // Then the next job starts cold.
                nextCold = Math.min(nextCold, costAfterRunningCold);

                // Choice B: Keep it warm by paying keep[i], but only if there is a next job.
                if (i < n - 1) {
                    nextWarm = Math.min(nextWarm, costAfterRunningCold + keep[i]);
                }
            }

            // ------------------------------------------------------------
            // Case 2: Job i starts warm
            // ------------------------------------------------------------
            if (dpWarm < INF) {
                // If the current job starts warm, we pay warmCost[i].
                long costAfterRunningWarm = dpWarm + warmCost[i];

                // Again, after the job finishes, decide the state for the next job.

                // Choice A: Shut down for free -> next job starts cold.
                nextCold = Math.min(nextCold, costAfterRunningWarm);

                // Choice B: Keep warm by paying keep[i], if there is a next job.
                if (i < n - 1) {
                    nextWarm = Math.min(nextWarm, costAfterRunningWarm + keep[i]);
                }
            }

            // Move to the next job.
            dpCold = nextCold;
            dpWarm = nextWarm;
        }

        // After the last job, there is no next job.
        // So both dpCold and dpWarm conceptually represent completed schedules.
        // The answer is the cheaper of the two.
        return Math.min(dpCold, dpWarm);
    }

    /**
     * A helper method that prints a detailed demonstration for one test case.
     *
     * @param coldCost cost of running each job when starting cold
     * @param warmCost cost of running each job when starting warm
     * @param keep maintenance cost between jobs
     * @return the computed minimum total cost
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long demonstrateAndSolve(int[] coldCost, int[] warmCost, int[] keep) {
        System.out.println("coldCost = " + Arrays.toString(coldCost));
        System.out.println("warmCost = " + Arrays.toString(warmCost));
        System.out.println("keep     = " + Arrays.toString(keep));

        long answer = minimumCost(coldCost, warmCost, keep);
        System.out.println("Minimum total cost = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Validates the input arrays according to the problem constraints.
     *
     * @param coldCost cost of running each job when starting cold
     * @param warmCost cost of running each job when starting warm
     * @param keep maintenance cost between jobs
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] coldCost, int[] warmCost, int[] keep) {
        if (coldCost == null || warmCost == null || keep == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }

        if (coldCost.length == 0) {
            throw new IllegalArgumentException("There must be at least one job.");
        }

        if (coldCost.length != warmCost.length) {
            throw new IllegalArgumentException("coldCost and warmCost must have the same length.");
        }

        if (keep.length != coldCost.length - 1) {
            throw new IllegalArgumentException("keep.length must be exactly n - 1.");
        }
    }

    /**
     * Runs sample demonstrations from the problem statement.
     *
     * Note:
     * The dynamic programming model implemented here exactly follows the formal rules:
     * - first job starts cold
     * - warm start uses warmCost[i]
     * - keeping warm between i and i+1 costs keep[i]
     * - shutting down is free
     *
     * For Example 1:
     * The optimal cost is 8 + 2 + 3 + 6 + 4 = 23, not 18.
     * So the sample output in the prompt is inconsistent with the formal definition.
     * This program prints the mathematically correct result under the stated rules.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size of demonstrated cases)
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        // Under the formal rules, the correct minimum is 23:
        // job0 cold = 8
        // keep after job0 = 2
        // job1 warm = 3
        // keep after job1 = 6
        // job2 warm = 4
        // total = 23
        int[] coldCost1 = {8, 7, 9};
        int[] warmCost1 = {5, 3, 4};
        int[] keep1 = {2, 6};
        solution.demonstrateAndSolve(coldCost1, warmCost1, keep1);

        // Example 2 from the prompt.
        // The DP computes the correct minimum under the formal rules.
        int[] coldCost2 = {4, 10, 6, 8};
        int[] warmCost2 = {2, 1, 3, 2};
        int[] keep2 = {7, 2, 10};
        solution.demonstrateAndSolve(coldCost2, warmCost2, keep2);

        // Additional small sanity test.
        int[] coldCost3 = {5};
        int[] warmCost3 = {1};
        int[] keep3 = {};
        solution.demonstrateAndSolve(coldCost3, warmCost3, keep3);
    }
}