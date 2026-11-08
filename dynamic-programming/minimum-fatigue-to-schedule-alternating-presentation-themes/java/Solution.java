import java.util.*;

/*
 * Title: Minimum Fatigue to Schedule Alternating Presentation Themes
 *
 * Problem Description:
 * A conference organizer is preparing a sequence of n presentation slots.
 * For each slot i, there are exactly three candidate talks available:
 * a Technical talk, a Product talk, and a Research talk.
 * Choosing candidate type t for slot i adds fatigue cost cost[i][t].
 *
 * The organizer wants the final schedule to feel varied.
 * A schedule is considered valid if it satisfies both rules:
 * 1. No two consecutive slots use the same theme.
 * 2. For every three consecutive scheduled slots, the three themes cannot be all distinct.
 *    In other words, among any window of length 3, at least one theme must repeat.
 *
 * Your task is to return the minimum total fatigue needed to fill all n slots with a valid schedule.
 * If no valid schedule exists, return -1.
 *
 * Notes:
 * - For n = 1, any single theme is valid.
 * - For n = 2, any pair of different themes is valid.
 * - For n >= 3, the second rule heavily constrains transitions and suggests a DP state
 *   based on the last two chosen themes.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - cost.length == n
 * - cost[i].length == 3
 * - 1 <= cost[i][t] <= 10^9
 * - The answer may exceed 32-bit integer range
 */

public class Solution {

    /**
     * Computes the minimum total fatigue needed to build a valid schedule.
     *
     * Core idea:
     * We use dynamic programming on the last two chosen themes.
     *
     * Why last two themes are enough:
     * - Rule 1 (adjacent themes differ) only depends on the immediately previous theme.
     * - Rule 2 (every length-3 window must not contain three distinct themes) only depends
     *   on the previous two themes and the new theme we want to append.
     *
     * DP state:
     * dp[a][b] = minimum total cost for scheduling slots processed so far,
     *            where the last two chosen themes are a, b.
     *
     * Transition:
     * From state (a, b), try to append theme c.
     * Valid only if:
     * - b != c   (adjacent themes differ)
     * - NOT (a, b, c are all distinct)
     *
     * Since a != b is already guaranteed by construction, and b != c is required,
     * the only way for (a, b, c) to NOT be all distinct is to force c == a.
     *
     * Therefore for n >= 3, every valid continuation from (a, b) is uniquely determined:
     * the next theme must equal a.
     *
     * This means valid schedules for length >= 2 must alternate like:
     * x, y, x, y, x, y, ...
     * with x != y.
     *
     * @param n the number of presentation slots
     * @param cost cost[i][t] is the fatigue cost of choosing theme t for slot i
     * @return the minimum total fatigue for a valid schedule, or -1 if impossible
     * Time complexity: O(n), because the number of states is constant (3 x 3) and each slot
     * processes only a constant number of transitions.
     * Space complexity: O(1), ignoring the input array, because we store only two 3x3 DP tables.
     */
    public long minimumFatigue(int n, int[][] cost) {
        if (n <= 0 || cost == null || cost.length != n) {
            return -1;
        }

        for (int i = 0; i < n; i++) {
            if (cost[i] == null || cost[i].length != 3) {
                return -1;
            }
        }

        // Base case: with one slot, any single theme is valid.
        // We simply choose the cheapest theme for slot 0.
        if (n == 1) {
            long answer = Long.MAX_VALUE;
            for (int theme = 0; theme < 3; theme++) {
                answer = Math.min(answer, (long) cost[0][theme]);
            }
            return answer;
        }

        // We use a large sentinel value to represent "unreachable".
        // Long.MAX_VALUE itself is risky because adding a cost could overflow,
        // so we use a safely smaller large number.
        final long INF = Long.MAX_VALUE / 4;

        // dp[a][b] means:
        // after scheduling the first 2 slots, slot 0 uses theme a and slot 1 uses theme b,
        // and the total cost is stored here.
        //
        // Only states with a != b are valid because adjacent themes must differ.
        long[][] dp = new long[3][3];
        for (int a = 0; a < 3; a++) {
            Arrays.fill(dp[a], INF);
        }

        // Initialize all valid schedules of length 2.
        for (int first = 0; first < 3; first++) {
            for (int second = 0; second < 3; second++) {
                if (first == second) {
                    continue; // adjacent equal themes are forbidden
                }
                dp[first][second] = (long) cost[0][first] + cost[1][second];
            }
        }

        // Process slots from index 2 onward.
        for (int i = 2; i < n; i++) {
            long[][] next = new long[3][3];
            for (int a = 0; a < 3; a++) {
                Arrays.fill(next[a], INF);
            }

            // Try to extend every reachable state (prevPrevTheme = a, prevTheme = b).
            for (int a = 0; a < 3; a++) {
                for (int b = 0; b < 3; b++) {
                    long currentCost = dp[a][b];
                    if (currentCost >= INF) {
                        continue; // unreachable state
                    }

                    // We now choose theme c for the current slot i.
                    //
                    // Conditions:
                    // 1) b != c  (adjacent themes differ)
                    // 2) (a, b, c) are NOT all distinct
                    //
                    // Because a != b always holds in valid states, and b != c is required,
                    // the only way to avoid all three being distinct is c == a.
                    //
                    // So there is exactly one possible valid next theme:
                    int c = a;

                    // Check adjacency rule explicitly for clarity.
                    // Since a != b in valid states, c == a automatically implies c != b.
                    if (b == c) {
                        continue;
                    }

                    long candidate = currentCost + cost[i][c];

                    // After appending c, the new last two themes become (b, c).
                    if (candidate < next[b][c]) {
                        next[b][c] = candidate;
                    }
                }
            }

            dp = next;
        }

        // The answer is the minimum reachable cost among all ending pairs.
        long answer = INF;
        for (int a = 0; a < 3; a++) {
            for (int b = 0; b < 3; b++) {
                answer = Math.min(answer, dp[a][b]);
            }
        }

        return answer >= INF ? -1 : answer;
    }

    /**
     * A second public method that solves the same problem using a simplified observation.
     *
     * Observation:
     * Once the first two themes are chosen and they are different, every later theme is forced:
     * slot 2 must equal slot 0,
     * slot 3 must equal slot 1,
     * slot 4 must equal slot 2 = slot 0,
     * and so on.
     *
     * So every valid schedule is simply an alternating pattern between two distinct themes:
     * x, y, x, y, x, y, ...
     *
     * We can therefore try all ordered pairs (x, y) with x != y, compute the total cost
     * of that alternating pattern, and take the minimum.
     *
     * There are only 6 ordered pairs, so this is also O(n).
     *
     * @param n the number of presentation slots
     * @param cost cost[i][t] is the fatigue cost of choosing theme t for slot i
     * @return the minimum total fatigue for a valid schedule, or -1 if impossible
     * Time complexity: O(n), because we test only 6 alternating patterns.
     * Space complexity: O(1).
     */
    public long minimumFatigueByPattern(int n, int[][] cost) {
        if (n <= 0 || cost == null || cost.length != n) {
            return -1;
        }

        for (int i = 0; i < n; i++) {
            if (cost[i] == null || cost[i].length != 3) {
                return -1;
            }
        }

        if (n == 1) {
            return Math.min(cost[0][0], Math.min(cost[0][1], cost[0][2]));
        }

        long answer = Long.MAX_VALUE;

        // Try every ordered pair of distinct themes (x, y).
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (x == y) {
                    continue;
                }

                long total = 0L;

                // Build the forced alternating schedule:
                // even index -> x, odd index -> y
                for (int i = 0; i < n; i++) {
                    int theme = (i % 2 == 0) ? x : y;
                    total += cost[i][theme];
                }

                answer = Math.min(answer, total);
            }
        }

        return answer == Long.MAX_VALUE ? -1 : answer;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * This method also cross-checks the DP method and the pattern-based method
     * to show they produce the same result.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size used in the demo).
     * Space complexity: O(1), excluding the sample input arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 4;
        int[][] cost1 = {
            {3, 2, 7},
            {5, 1, 4},
            {2, 6, 3},
            {8, 2, 5}
        };

        long result1 = solution.minimumFatigue(n1, cost1);
        long result1Pattern = solution.minimumFatigueByPattern(n1, cost1);

        System.out.println(result1);
        System.out.println(result1Pattern);

        // Expected: 8
        // Valid optimal schedule: [0, 1, 0, 1]
        // Cost = 3 + 1 + 2 + 2 = 8

        // Example 2
        int n2 = 3;
        int[][] cost2 = {
            {1, 10, 10},
            {10, 1, 10},
            {10, 10, 1}
        };

        long result2 = solution.minimumFatigue(n2, cost2);
        long result2Pattern = solution.minimumFatigueByPattern(n2, cost2);

        System.out.println(result2);
        System.out.println(result2Pattern);

        // Expected: 12
        // Explanation:
        // A valid schedule for length 3 must alternate: x, y, x with x != y.
        // The best is [0,1,0] with cost 1 + 1 + 10 = 12.
        //
        // Note:
        // The problem statement's sample output says -1, but that is incorrect.
        // Example:
        // [0,1,0] satisfies:
        // - adjacent themes differ
        // - in the only length-3 window, theme 0 repeats, so the three themes are not all distinct
        //
        // Therefore the correct answer for Example 2 is 12.

        // Additional quick sanity checks

        // n = 1: choose cheapest single theme
        int n3 = 1;
        int[][] cost3 = {
            {9, 4, 7}
        };
        System.out.println(solution.minimumFatigue(n3, cost3)); // Expected: 4

        // n = 2: any pair of different themes is valid
        int n4 = 2;
        int[][] cost4 = {
            {5, 1, 9},
            {2, 8, 3}
        };
        System.out.println(solution.minimumFatigue(n4, cost4)); // Expected: 3 via [1,0]
    }
}