import java.util.*;

/*
 * Title: Minimum Cost to Schedule Server Reboots with Cluster Penalties
 *
 * Problem Description:
 * You are given a sequence of n servers in a data center, indexed from 0 to n - 1.
 * Each server must be rebooted exactly once, and the reboot for server i can be
 * scheduled in one of two maintenance windows:
 *
 * 1) Early window with cost earlyCost[i]
 * 2) Late window with cost lateCost[i]
 *
 * However, rebooting adjacent servers in the same window may overload a local rack
 * controller. For every adjacent pair (i - 1, i):
 *
 * - If both servers are assigned to the early window, add earlyPenalty[i]
 * - If both servers are assigned to the late window, add latePenalty[i]
 * - If they are assigned to different windows, add no penalty
 *
 * Additionally, exactly k servers must be scheduled in the early window.
 *
 * Return the minimum total cost to schedule all reboots while satisfying the exact
 * early-window requirement.
 *
 * Constraints:
 * - 1 <= n <= 2000
 * - 0 <= k <= n
 * - earlyCost.length == lateCost.length == n
 * - earlyPenalty.length == latePenalty.length == n
 * - earlyPenalty[0] and latePenalty[0] are ignored
 * - 1 <= earlyCost[i], lateCost[i], earlyPenalty[i], latePenalty[i] <= 10^9
 *
 * Notes about the examples:
 * The textual explanations in the prompt contain arithmetic inconsistencies.
 * The algorithm implemented below computes the true minimum according to the formal
 * problem statement.
 */

public class Solution {

    /**
     * A very large value used as "infinity" for dynamic programming.
     * We keep it safely below Long.MAX_VALUE so additions do not overflow.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum total cost to assign each server to either the early or late
     * maintenance window, such that exactly k servers are assigned to the early window.
     *
     * Dynamic Programming Idea:
     * Let dpEarly[e] = minimum cost after processing servers up to the current index,
     *                  using exactly e early assignments, and assigning the current server
     *                  to Early.
     *
     * Let dpLate[e]  = minimum cost after processing servers up to the current index,
     *                  using exactly e early assignments, and assigning the current server
     *                  to Late.
     *
     * Transition for server i:
     * - If server i is assigned Early:
     *      We must come from server i - 1 being either:
     *      a) Early  -> add earlyPenalty[i]
     *      b) Late   -> add no adjacency penalty
     *
     * - If server i is assigned Late:
     *      We must come from server i - 1 being either:
     *      a) Late   -> add latePenalty[i]
     *      b) Early  -> add no adjacency penalty
     *
     * Because each state only depends on the previous server, we can optimize space
     * from O(n * k) to O(k) by keeping only the previous row.
     *
     * @param earlyCost the cost of assigning each server to the early window
     * @param lateCost the cost of assigning each server to the late window
     * @param earlyPenalty the penalty for adjacent servers both assigned Early
     * @param latePenalty the penalty for adjacent servers both assigned Late
     * @param k the exact number of servers that must be assigned to Early
     * @return the minimum total cost as a 64-bit integer
     * Time complexity: O(n * k)
     * Space complexity: O(k)
     */
    public long minimumCost(int[] earlyCost, int[] lateCost, int[] earlyPenalty, int[] latePenalty, int k) {
        validateInput(earlyCost, lateCost, earlyPenalty, latePenalty, k);

        int n = earlyCost.length;

        // Quick impossible case check.
        if (k < 0 || k > n) {
            return -1L;
        }

        // dpEarly[e] = best cost after processing current server, with exactly e early picks,
        //              and current server assigned Early.
        // dpLate[e]  = best cost after processing current server, with exactly e early picks,
        //              and current server assigned Late.
        long[] dpEarly = new long[k + 1];
        long[] dpLate = new long[k + 1];
        Arrays.fill(dpEarly, INF);
        Arrays.fill(dpLate, INF);

        // Base case for server 0:
        // If server 0 is Early, then we used exactly 1 early assignment.
        if (k >= 1) {
            dpEarly[1] = earlyCost[0];
        }

        // If server 0 is Late, then we used exactly 0 early assignments.
        dpLate[0] = lateCost[0];

        // Process servers from index 1 to n - 1.
        for (int i = 1; i < n; i++) {
            long[] nextEarly = new long[k + 1];
            long[] nextLate = new long[k + 1];
            Arrays.fill(nextEarly, INF);
            Arrays.fill(nextLate, INF);

            // The number of early assignments used so far cannot exceed i + 1,
            // because we have processed exactly i + 1 servers.
            int maxEarlySoFar = Math.min(k, i + 1);

            for (int e = 0; e <= maxEarlySoFar; e++) {

                // ------------------------------------------------------------
                // Case 1: Assign server i to Early
                // ------------------------------------------------------------
                // Then the total number of early assignments becomes e,
                // so before assigning server i, we must have had e - 1 early assignments.
                if (e >= 1) {
                    long bestPrevious = INF;

                    // Previous server was also Early:
                    // add earlyPenalty[i] because (i - 1, i) are both Early.
                    if (dpEarly[e - 1] != INF) {
                        bestPrevious = Math.min(bestPrevious, dpEarly[e - 1] + earlyPenalty[i]);
                    }

                    // Previous server was Late:
                    // no adjacency penalty because windows differ.
                    if (dpLate[e - 1] != INF) {
                        bestPrevious = Math.min(bestPrevious, dpLate[e - 1]);
                    }

                    // Add the base cost of assigning server i to Early.
                    if (bestPrevious != INF) {
                        nextEarly[e] = bestPrevious + earlyCost[i];
                    }
                }

                // ------------------------------------------------------------
                // Case 2: Assign server i to Late
                // ------------------------------------------------------------
                // Then the total number of early assignments remains e,
                // so before assigning server i, we must also have had e early assignments.
                long bestPrevious = INF;

                // Previous server was Late:
                // add latePenalty[i] because (i - 1, i) are both Late.
                if (dpLate[e] != INF) {
                    bestPrevious = Math.min(bestPrevious, dpLate[e] + latePenalty[i]);
                }

                // Previous server was Early:
                // no adjacency penalty because windows differ.
                if (dpEarly[e] != INF) {
                    bestPrevious = Math.min(bestPrevious, dpEarly[e]);
                }

                // Add the base cost of assigning server i to Late.
                if (bestPrevious != INF) {
                    nextLate[e] = bestPrevious + lateCost[i];
                }
            }

            // Move to the next server.
            dpEarly = nextEarly;
            dpLate = nextLate;
        }

        // Final answer: after processing all servers, exactly k must be Early.
        long answer = Math.min(dpEarly[k], dpLate[k]);

        // Under valid constraints, a solution always exists when 0 <= k <= n.
        return answer >= INF ? -1L : answer;
    }

    /**
     * Convenience overload that accepts long arrays directly.
     * This can be useful if the caller already stores values as long.
     *
     * @param earlyCost the cost of assigning each server to the early window
     * @param lateCost the cost of assigning each server to the late window
     * @param earlyPenalty the penalty for adjacent servers both assigned Early
     * @param latePenalty the penalty for adjacent servers both assigned Late
     * @param k the exact number of servers that must be assigned to Early
     * @return the minimum total cost as a 64-bit integer
     * Time complexity: O(n * k)
     * Space complexity: O(k)
     */
    public long minimumCost(long[] earlyCost, long[] lateCost, long[] earlyPenalty, long[] latePenalty, int k) {
        validateInput(earlyCost, lateCost, earlyPenalty, latePenalty, k);

        int n = earlyCost.length;

        if (k < 0 || k > n) {
            return -1L;
        }

        long[] dpEarly = new long[k + 1];
        long[] dpLate = new long[k + 1];
        Arrays.fill(dpEarly, INF);
        Arrays.fill(dpLate, INF);

        if (k >= 1) {
            dpEarly[1] = earlyCost[0];
        }
        dpLate[0] = lateCost[0];

        for (int i = 1; i < n; i++) {
            long[] nextEarly = new long[k + 1];
            long[] nextLate = new long[k + 1];
            Arrays.fill(nextEarly, INF);
            Arrays.fill(nextLate, INF);

            int maxEarlySoFar = Math.min(k, i + 1);

            for (int e = 0; e <= maxEarlySoFar; e++) {
                if (e >= 1) {
                    long bestPrevious = INF;

                    if (dpEarly[e - 1] != INF) {
                        bestPrevious = Math.min(bestPrevious, dpEarly[e - 1] + earlyPenalty[i]);
                    }
                    if (dpLate[e - 1] != INF) {
                        bestPrevious = Math.min(bestPrevious, dpLate[e - 1]);
                    }

                    if (bestPrevious != INF) {
                        nextEarly[e] = bestPrevious + earlyCost[i];
                    }
                }

                long bestPrevious = INF;

                if (dpLate[e] != INF) {
                    bestPrevious = Math.min(bestPrevious, dpLate[e] + latePenalty[i]);
                }
                if (dpEarly[e] != INF) {
                    bestPrevious = Math.min(bestPrevious, dpEarly[e]);
                }

                if (bestPrevious != INF) {
                    nextLate[e] = bestPrevious + lateCost[i];
                }
            }

            dpEarly = nextEarly;
            dpLate = nextLate;
        }

        long answer = Math.min(dpEarly[k], dpLate[k]);
        return answer >= INF ? -1L : answer;
    }

    /**
     * Validates the int-array input.
     *
     * @param earlyCost the early costs
     * @param lateCost the late costs
     * @param earlyPenalty the early adjacency penalties
     * @param latePenalty the late adjacency penalties
     * @param k required number of early assignments
     * @return nothing; throws an exception if input is invalid
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] earlyCost, int[] lateCost, int[] earlyPenalty, int[] latePenalty, int k) {
        if (earlyCost == null || lateCost == null || earlyPenalty == null || latePenalty == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }

        int n = earlyCost.length;
        if (lateCost.length != n || earlyPenalty.length != n || latePenalty.length != n) {
            throw new IllegalArgumentException("All arrays must have the same length.");
        }

        if (n == 0) {
            throw new IllegalArgumentException("Array length must be at least 1.");
        }

        if (k < 0 || k > n) {
            throw new IllegalArgumentException("k must satisfy 0 <= k <= n.");
        }
    }

    /**
     * Validates the long-array input.
     *
     * @param earlyCost the early costs
     * @param lateCost the late costs
     * @param earlyPenalty the early adjacency penalties
     * @param latePenalty the late adjacency penalties
     * @param k required number of early assignments
     * @return nothing; throws an exception if input is invalid
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(long[] earlyCost, long[] lateCost, long[] earlyPenalty, long[] latePenalty, int k) {
        if (earlyCost == null || lateCost == null || earlyPenalty == null || latePenalty == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }

        int n = earlyCost.length;
        if (lateCost.length != n || earlyPenalty.length != n || latePenalty.length != n) {
            throw new IllegalArgumentException("All arrays must have the same length.");
        }

        if (n == 0) {
            throw new IllegalArgumentException("Array length must be at least 1.");
        }

        if (k < 0 || k > n) {
            throw new IllegalArgumentException("k must satisfy 0 <= k <= n.");
        }
    }

    /**
     * Demonstrates the solution on the sample-style inputs from the prompt.
     *
     * Important:
     * The prompt's written explanations contain arithmetic inconsistencies.
     * This main method prints the true minimum values computed from the formal rules.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n * k) for each demonstration call
     * Space complexity: O(k)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 4;
        int k1 = 2;
        int[] earlyCost1 = {3, 8, 2, 6};
        int[] lateCost1 = {5, 1, 7, 3};
        int[] earlyPenalty1 = {0, 4, 5, 2};
        int[] latePenalty1 = {0, 6, 1, 3};

        long result1 = solution.minimumCost(earlyCost1, lateCost1, earlyPenalty1, latePenalty1, k1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected by formal calculation: 9");

        // Example 2
        int n2 = 5;
        int k2 = 3;
        int[] earlyCost2 = {4, 2, 9, 3, 5};
        int[] lateCost2 = {6, 7, 1, 8, 2};
        int[] earlyPenalty2 = {0, 3, 4, 6, 1};
        int[] latePenalty2 = {0, 2, 5, 2, 7};

        long result2 = solution.minimumCost(earlyCost2, lateCost2, earlyPenalty2, latePenalty2, k2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected by formal calculation: 15");

        // Additional small sanity checks

        // All late required
        int[] earlyCost3 = {10, 10, 10};
        int[] lateCost3 = {1, 1, 1};
        int[] earlyPenalty3 = {0, 5, 5};
        int[] latePenalty3 = {0, 2, 2};
        System.out.println("All late (k=0): " +
                solution.minimumCost(earlyCost3, lateCost3, earlyPenalty3, latePenalty3, 0));

        // All early required
        System.out.println("All early (k=3): " +
                solution.minimumCost(earlyCost3, lateCost3, earlyPenalty3, latePenalty3, 3));

        // Single server
        int[] earlyCost4 = {7};
        int[] lateCost4 = {4};
        int[] earlyPenalty4 = {0};
        int[] latePenalty4 = {0};
        System.out.println("Single server, k=1: " +
                solution.minimumCost(earlyCost4, lateCost4, earlyPenalty4, latePenalty4, 1));
        System.out.println("Single server, k=0: " +
                solution.minimumCost(earlyCost4, lateCost4, earlyPenalty4, latePenalty4, 0));

        // Print n values just to show the sample setup was used.
        System.out.println("Sample sizes used: n1=" + n1 + ", n2=" + n2);
    }
}