import java.util.*;

/*
Problem Title: Minimum Cost to Schedule Factory Maintenance With Team Cooldowns

Problem Description:
A factory must perform maintenance on a sequence of n machines over n consecutive days.
On day i, exactly one maintenance team must be assigned to machine i.

There are 3 available teams:
1. Electrical
2. Mechanical
3. Software

Assigning team t to machine i has a known cost cost[i][t].

Cooldown Rule:
If a team is used on day i, that same team cannot be used again on day i + 1 or day i + 2.
So, the team chosen today must be different from the teams used on the previous two days.

Goal:
Compute the minimum total maintenance cost to complete all n days while satisfying the cooldown rule.
If no valid schedule exists, return -1.

Why Dynamic Programming:
The best choice for the current day depends on the teams selected on the previous two days.
That means the state must remember enough history to enforce the cooldown rule efficiently.

Constraints:
- 1 <= n <= 100000
- cost.length == n
- cost[i].length == 3
- 1 <= cost[i][t] <= 1000000
*/

public class Solution {

    /**
     * Computes the minimum total maintenance cost while respecting the cooldown rule.
     *
     * The key idea is dynamic programming over the last two chosen teams.
     *
     * State definition:
     * dp[prev2][prev1] = minimum total cost after processing some prefix of days,
     * where:
     * - prev2 is the team used two days ago
     * - prev1 is the team used one day ago
     *
     * Because there are only 3 teams, the number of possible states is tiny:
     * 4 x 4 if we include a special sentinel value meaning "no team yet".
     *
     * Sentinel:
     * We use team index 3 to mean "no previous team" for the first two days.
     *
     * Transition:
     * For each state (prev2, prev1), try assigning current team cur.
     * The assignment is valid only if:
     * - cur != prev1
     * - cur != prev2
     *
     * Then the next state becomes:
     * (prev1, cur)
     *
     * Important correctness note:
     * With exactly 3 teams and a cooldown of 2 days, from day 2 onward the choice is often forced
     * by the previous two teams. Still, dynamic programming is the clean and general way to solve it.
     *
     * Also note:
     * The problem statement's Example 1 claims output 8, but that is impossible under the stated rule.
     * For 4 days with 3 teams and cooldown 2, the valid schedules are permutations that repeat every 3 days.
     * Exhaustive checking shows the true minimum for the provided matrix is 11.
     * This method returns the correct value according to the stated rules.
     *
     * @param cost cost[i][t] is the cost of assigning team t to day i, where t is 0..2
     * @return the minimum total cost, or -1 if no valid schedule exists
     * Time complexity: O(n), because each day processes only a constant number of states/transitions
     * Space complexity: O(1), because the DP state size is constant
     */
    public long minimumMaintenanceCost(int[][] cost) {
        if (cost == null || cost.length == 0) {
            return -1;
        }

        int n = cost.length;

        for (int i = 0; i < n; i++) {
            if (cost[i] == null || cost[i].length != 3) {
                throw new IllegalArgumentException("Each cost row must contain exactly 3 values.");
            }
        }

        // There are 3 real teams: 0, 1, 2.
        // We use 3 as a sentinel meaning "no team yet".
        final int NONE = 3;

        // A very large number used to represent "unreachable".
        // We use long because total cost can be as large as 100000 * 1000000 = 1e11.
        final long INF = Long.MAX_VALUE / 4;

        // dp[a][b] means:
        // after processing the current prefix of days,
        // the team used two days ago is 'a',
        // and the team used one day ago is 'b',
        // with minimum total cost equal to dp[a][b].
        long[][] dp = new long[4][4];
        long[][] next = new long[4][4];

        // Initialize all states as unreachable.
        for (int i = 0; i < 4; i++) {
            Arrays.fill(dp[i], INF);
            Arrays.fill(next[i], INF);
        }

        // Before processing any day, there are no previous teams.
        dp[NONE][NONE] = 0L;

        // Process each day one by one.
        for (int day = 0; day < n; day++) {
            // Reset next-layer DP table to unreachable before filling it.
            for (int i = 0; i < 4; i++) {
                Arrays.fill(next[i], INF);
            }

            // Try extending every currently reachable state.
            for (int prev2 = 0; prev2 < 4; prev2++) {
                for (int prev1 = 0; prev1 < 4; prev1++) {
                    long currentCost = dp[prev2][prev1];

                    // If this state is unreachable, skip it.
                    if (currentCost == INF) {
                        continue;
                    }

                    // Try assigning each of the 3 teams on the current day.
                    for (int team = 0; team < 3; team++) {
                        // Cooldown rule:
                        // The current team must be different from the previous day's team
                        // and also different from the team used two days ago.
                        if (team == prev1 || team == prev2) {
                            continue;
                        }

                        long newCost = currentCost + cost[day][team];

                        // After choosing 'team' today:
                        // - yesterday's team becomes the new "two days ago"
                        // - today's team becomes the new "one day ago"
                        if (newCost < next[prev1][team]) {
                            next[prev1][team] = newCost;
                        }
                    }
                }
            }

            // Move to the next day.
            long[][] temp = dp;
            dp = next;
            next = temp;
        }

        // The answer is the minimum reachable value among all ending states.
        long answer = INF;
        for (int prev2 = 0; prev2 < 4; prev2++) {
            for (int prev1 = 0; prev1 < 4; prev1++) {
                answer = Math.min(answer, dp[prev2][prev1]);
            }
        }

        return answer == INF ? -1 : answer;
    }

    /**
     * A convenience wrapper that returns the answer as int when it fits,
     * or -1 if no valid schedule exists.
     *
     * This method is included for beginner-friendliness and API flexibility.
     * Internally, the main algorithm uses long to safely handle large sums.
     *
     * @param cost cost[i][t] is the cost of assigning team t to day i
     * @return the minimum total cost as an int if valid, otherwise -1
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minimumMaintenanceCostAsInt(int[][] cost) {
        long result = minimumMaintenanceCost(cost);
        if (result < 0) {
            return -1;
        }
        if (result > Integer.MAX_VALUE) {
            throw new ArithmeticException("Result exceeds int range. Use minimumMaintenanceCost instead.");
        }
        return (int) result;
    }

    /**
     * Prints a cost matrix in a readable format.
     *
     * @param cost the cost matrix
     * @return a string representation of the matrix
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String matrixToString(int[][] cost) {
        return Arrays.deepToString(cost);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * Important note about Example 1:
     * The problem statement says the output is 8, but that contradicts the cooldown rule.
     * Under the stated rule, the correct minimum is 11.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm runs
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] cost1 = {
            {5, 1, 4},
            {2, 3, 6},
            {7, 2, 5},
            {4, 6, 3}
        };

        int[][] cost2 = {
            {3, 2, 7},
            {5, 1, 4}
        };

        int[][] cost3 = {
            {8}
        };

        int[][] cost4 = {
            {1, 100, 100},
            {100, 1, 100},
            {100, 100, 1}
        };

        int[][] cost5 = {
            {1, 2, 3}
        };

        System.out.println("Example 1 input: " + solution.matrixToString(cost1));
        System.out.println("Computed output: " + solution.minimumMaintenanceCost(cost1));
        System.out.println("Note: Under the stated cooldown rule, the correct minimum is 11, not 8.");
        System.out.println();

        System.out.println("Example 2 input: " + solution.matrixToString(cost2));
        System.out.println("Computed output: " + solution.minimumMaintenanceCost(cost2));
        System.out.println("Expected under the stated rule: 3");
        System.out.println();

        System.out.println("Additional test 1 (invalid matrix shape example avoided in execution).");
        System.out.println();

        System.out.println("Additional test 2 input: " + solution.matrixToString(cost4));
        System.out.println("Computed output: " + solution.minimumMaintenanceCost(cost4));
        System.out.println("Expected: 3");
        System.out.println();

        System.out.println("Additional test 3 input: " + solution.matrixToString(cost5));
        System.out.println("Computed output: " + solution.minimumMaintenanceCost(cost5));
        System.out.println("Expected: 1");
    }
}