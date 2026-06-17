import java.util.*;

/*
Problem Title: Minimum Rest Stops to Climb a Stair Route

Problem Description:
You are planning a climb along a stair route with n steps, numbered from 0 to n.
You start at step 0 and want to reach step n. From any step, you may move either
1 step or 2 steps forward. Some steps are marked as rest stops, represented by a
binary array rests of length n + 1, where rests[i] = 1 means step i has a rest
platform and rests[i] = 0 means it does not. Step 0 and step n may also be rest stops.

Your goal is to reach step n while using the minimum possible number of rest stops
visited, including the destination if it is a rest stop. A rest stop is counted only
when you land on that step. If it is impossible to reach the top because every valid
path would require landing on a blocked step, return -1. A blocked step is represented
by another binary array blocked of length n + 1, where blocked[i] = 1 means you cannot
stand on step i. You may assume blocked[0] = 0.

Return the minimum number of rest stops needed to reach step n.

This is a dynamic programming problem because the best answer for each step depends on
the best answers for the previous one or two steps.

Constraints:
- 1 <= n <= 100000
- rests.length == n + 1
- blocked.length == n + 1
- rests[i] is either 0 or 1
- blocked[i] is either 0 or 1
- blocked[0] = 0

Example 1:
Input: n = 5, rests = [0,1,0,1,0,1], blocked = [0,0,0,0,0,0]
Output: 1
Explanation: One optimal path is 0 -> 2 -> 4 -> 5. Only step 5 is a rest stop,
so the total number of visited rest stops is 1.

Example 2:
Input: n = 6, rests = [0,1,1,0,1,0,1], blocked = [0,0,1,0,0,1,0]
Output: 1
Explanation: Step 2 and step 5 are blocked. An optimal path is 0 -> 1 -> 3 -> 4 -> 6.
Among the visited steps, only step 1 is a rest stop, so the answer is 1.
*/

public class Solution {

    /**
     * Computes the minimum number of rest stops visited while climbing from step 0 to step n.
     *
     * The climber may move either 1 or 2 steps at a time.
     * A step cannot be used if it is blocked.
     * A rest stop contributes 1 to the total only when that step is landed on.
     *
     * Dynamic Programming Idea:
     * Let dp[i] be the minimum number of rest stops needed to reach step i.
     * Then:
     * dp[i] = min(dp[i - 1], dp[i - 2]) + rests[i], if step i is not blocked
     *
     * If both previous states are unreachable, then step i is unreachable.
     *
     * @param n the destination step number
     * @param rests binary array where rests[i] = 1 means step i is a rest stop
     * @param blocked binary array where blocked[i] = 1 means step i cannot be stood on
     * @return the minimum number of rest stops needed to reach step n, or -1 if impossible
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minRestStops(int n, int[] rests, int[] blocked) {
        // A large value used to represent "unreachable".
        // We choose a safe large number much bigger than any possible answer.
        final int INF = 1_000_000_000;

        // dp[i] will store the minimum number of rest stops needed to reach step i.
        int[] dp = new int[n + 1];
        Arrays.fill(dp, INF);

        // Starting point:
        // We are already standing on step 0 at the beginning.
        // The problem says a rest stop is counted only when you land on that step.
        // Since we start there rather than land there from a move, we do NOT count rests[0].
        dp[0] = 0;

        // Process every step from 1 to n.
        for (int i = 1; i <= n; i++) {
            // If the current step is blocked, we cannot stand on it at all.
            // Therefore, it remains unreachable.
            if (blocked[i] == 1) {
                dp[i] = INF;
                continue;
            }

            // We want to find the best way to arrive at step i.
            // Since allowed moves are only +1 or +2, the only possible previous steps are:
            // - i - 1
            // - i - 2
            int bestPrevious = INF;

            // Check if we can come from step i - 1.
            if (i - 1 >= 0) {
                bestPrevious = Math.min(bestPrevious, dp[i - 1]);
            }

            // Check if we can come from step i - 2.
            if (i - 2 >= 0) {
                bestPrevious = Math.min(bestPrevious, dp[i - 2]);
            }

            // If neither previous step is reachable, then current step is also unreachable.
            if (bestPrevious == INF) {
                dp[i] = INF;
            } else {
                // Otherwise, we can reach this step.
                // If this step is a rest stop, landing here adds 1.
                // If not, it adds 0.
                dp[i] = bestPrevious + rests[i];
            }
        }

        // If destination step n is still unreachable, return -1.
        if (dp[n] == INF) {
            return -1;
        }

        // Otherwise return the minimum number of rest stops needed.
        return dp[n];
    }

    /**
     * Space-optimized version of the dynamic programming solution.
     *
     * Instead of storing the entire dp array, we only keep the last two DP values,
     * because each state depends only on the previous two states.
     *
     * This method is included as an additional educational version.
     *
     * @param n the destination step number
     * @param rests binary array where rests[i] = 1 means step i is a rest stop
     * @param blocked binary array where blocked[i] = 1 means step i cannot be stood on
     * @return the minimum number of rest stops needed to reach step n, or -1 if impossible
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minRestStopsOptimized(int n, int[] rests, int[] blocked) {
        final int INF = 1_000_000_000;

        // prev2 represents dp[i - 2]
        // prev1 represents dp[i - 1]
        int prev2 = INF;
        int prev1 = 0; // dp[0] = 0

        // Handle steps from 1 to n one by one.
        for (int i = 1; i <= n; i++) {
            int current;

            // If blocked, current step is unreachable.
            if (blocked[i] == 1) {
                current = INF;
            } else {
                // Find the better of the two possible previous steps.
                int bestPrevious = prev1;
                if (i - 2 >= 0) {
                    bestPrevious = Math.min(bestPrevious, prev2);
                }

                // If both are unreachable, current is unreachable.
                if (bestPrevious == INF) {
                    current = INF;
                } else {
                    current = bestPrevious + rests[i];
                }
            }

            // Shift the window forward:
            // old prev1 becomes new prev2
            // current becomes new prev1
            prev2 = prev1;
            prev1 = current;
        }

        return prev1 == INF ? -1 : prev1;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(n) for the main DP method
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 5;
        int[] rests1 = {0, 1, 0, 1, 0, 1};
        int[] blocked1 = {0, 0, 0, 0, 0, 0};
        int result1 = solution.minRestStops(n1, rests1, blocked1);

        System.out.println("Example 1");
        System.out.println("n = " + n1);
        System.out.println("rests = " + solution.arrayToString(rests1));
        System.out.println("blocked = " + solution.arrayToString(blocked1));
        System.out.println("Minimum rest stops = " + result1);
        System.out.println("Expected = 1");
        System.out.println();

        // Example 2
        int n2 = 6;
        int[] rests2 = {0, 1, 1, 0, 1, 0, 1};
        int[] blocked2 = {0, 0, 1, 0, 0, 1, 0};
        int result2 = solution.minRestStops(n2, rests2, blocked2);

        System.out.println("Example 2");
        System.out.println("n = " + n2);
        System.out.println("rests = " + solution.arrayToString(rests2));
        System.out.println("blocked = " + solution.arrayToString(blocked2));
        System.out.println("Minimum rest stops = " + result2);
        System.out.println("Expected = 1");
        System.out.println();

        // Additional example: impossible case
        int n3 = 4;
        int[] rests3 = {0, 0, 1, 0, 1};
        int[] blocked3 = {0, 1, 1, 0, 0};
        int result3 = solution.minRestStops(n3, rests3, blocked3);

        System.out.println("Additional Example 3 (Impossible Case)");
        System.out.println("n = " + n3);
        System.out.println("rests = " + solution.arrayToString(rests3));
        System.out.println("blocked = " + solution.arrayToString(blocked3));
        System.out.println("Minimum rest stops = " + result3);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional example: destination is a rest stop and must be counted
        int n4 = 3;
        int[] rests4 = {1, 0, 0, 1};
        int[] blocked4 = {0, 0, 0, 0};
        int result4 = solution.minRestStops(n4, rests4, blocked4);

        System.out.println("Additional Example 4");
        System.out.println("n = " + n4);
        System.out.println("rests = " + solution.arrayToString(rests4));
        System.out.println("blocked = " + solution.arrayToString(blocked4));
        System.out.println("Minimum rest stops = " + result4);
        System.out.println("Expected = 1");
        System.out.println();

        // Show that the optimized version gives the same answers
        System.out.println("Optimized method checks:");
        System.out.println("Example 1 optimized = " + solution.minRestStopsOptimized(n1, rests1, blocked1));
        System.out.println("Example 2 optimized = " + solution.minRestStopsOptimized(n2, rests2, blocked2));
        System.out.println("Example 3 optimized = " + solution.minRestStopsOptimized(n3, rests3, blocked3));
        System.out.println("Example 4 optimized = " + solution.minRestStopsOptimized(n4, rests4, blocked4));
    }
}