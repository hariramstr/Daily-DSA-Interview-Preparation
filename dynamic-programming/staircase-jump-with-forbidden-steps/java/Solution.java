/*
 * Staircase Jump with Forbidden Steps
 * =====================================
 * You are climbing a staircase with `n` steps. At each step, you can jump either
 * 1 or 2 steps forward. However, some steps are marked as forbidden — you cannot
 * land on any forbidden step. Given the total number of steps `n` and a list of
 * forbidden step indices, return the number of distinct ways to reach step `n`
 * starting from step `0`. Since the answer may be large, return it modulo 10^9 + 7.
 *
 * Note: Step 0 is your starting position and is never forbidden.
 * You must reach exactly step n.
 *
 * Constraints:
 *   - 1 <= n <= 1000
 *   - 0 <= forbidden.length <= n - 1
 *   - All values in forbidden are distinct integers in the range [1, n - 1]
 *   - Step 0 and step n are never in the forbidden list
 *
 * Example 1:
 *   Input: n = 5, forbidden = [2]
 *   Output: 3
 *
 * Example 2:
 *   Input: n = 4, forbidden = []
 *   Output: 5
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Solution class for the "Staircase Jump with Forbidden Steps" problem.
 *
 * <p>Core Idea (Dynamic Programming):
 * We define dp[i] = number of distinct ways to reach step i from step 0,
 * respecting the forbidden steps constraint.
 *
 * <p>Recurrence:
 *   - If step i is forbidden: dp[i] = 0  (cannot land here)
 *   - Otherwise:             dp[i] = dp[i-1] + dp[i-2]
 *     (we can arrive from step i-1 with a 1-jump, or from step i-2 with a 2-jump)
 *
 * <p>Base cases:
 *   - dp[0] = 1  (we start at step 0, one way to "be" here)
 *   - dp[1] = 1  (only one way: 0 → 1), unless step 1 is forbidden → dp[1] = 0
 */
public class Solution {

    /** Modulus constant as required by the problem. */
    private static final int MOD = 1_000_000_007;

    /**
     * Counts the number of distinct ways to climb from step 0 to step n,
     * skipping any forbidden steps, using 1-step or 2-step jumps only.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>Store all forbidden steps in a HashSet for O(1) lookup.</li>
     *   <li>Build a DP array where dp[i] = ways to reach step i.</li>
     *   <li>For each step i from 1 to n, if it is forbidden set dp[i] = 0;
     *       otherwise dp[i] = (dp[i-1] + dp[i-2]) % MOD.</li>
     *   <li>Return dp[n].</li>
     * </ol>
     *
     * @param n         the total number of steps (destination is step n)
     * @param forbidden an array of step indices that cannot be landed on
     * @return          the number of distinct valid paths from step 0 to step n,
     *                  modulo 10^9 + 7
     *
     * Time Complexity:  O(n) — we iterate through all steps from 1 to n once.
     * Space Complexity: O(n) — for the dp array (plus O(f) for the forbidden set,
     *                          where f = forbidden.length).
     */
    public int climbStairs(int n, int[] forbidden) {

        // ---------------------------------------------------------------
        // Step 1: Load all forbidden step indices into a HashSet.
        //         This allows O(1) average-time lookup when we check
        //         whether a given step is forbidden.
        // ---------------------------------------------------------------
        Set<Integer> forbiddenSet = new HashSet<>();
        for (int step : forbidden) {
            forbiddenSet.add(step);
        }

        // ---------------------------------------------------------------
        // Step 2: Create the DP array of size (n + 1).
        //         dp[i] will hold the number of ways to reach step i.
        //         All entries are initialised to 0 by default in Java.
        // ---------------------------------------------------------------
        long[] dp = new long[n + 1];

        // ---------------------------------------------------------------
        // Step 3: Set the base case.
        //         dp[0] = 1 because we are already standing at step 0 —
        //         there is exactly one way to "reach" the starting position.
        // ---------------------------------------------------------------
        dp[0] = 1;

        // ---------------------------------------------------------------
        // Step 4: Handle step 1 explicitly as a base case.
        //         If step 1 is forbidden we cannot land there, so dp[1] = 0.
        //         Otherwise there is exactly one way to reach step 1 (0 → 1).
        //         Note: we only do this when n >= 1, which is always true
        //         given the constraints (n >= 1).
        // ---------------------------------------------------------------
        if (n >= 1) {
            dp[1] = forbiddenSet.contains(1) ? 0 : 1;
        }

        // ---------------------------------------------------------------
        // Step 5: Fill the DP table for steps 2 through n.
        //         For each step i:
        //           a) If step i is forbidden → dp[i] = 0.
        //              We cannot land here, so there are 0 valid ways.
        //           b) Otherwise → dp[i] = (dp[i-1] + dp[i-2]) % MOD.
        //              We can arrive from step (i-1) via a 1-step jump,
        //              or from step (i-2) via a 2-step jump.
        //              We take modulo to prevent integer overflow.
        // ---------------------------------------------------------------
        for (int i = 2; i <= n; i++) {

            if (forbiddenSet.contains(i)) {
                // Step i is forbidden — no valid path can end here.
                dp[i] = 0;
            } else {
                // Combine paths arriving from one step back and two steps back.
                dp[i] = (dp[i - 1] + dp[i - 2]) % MOD;
            }
        }

        // ---------------------------------------------------------------
        // Step 6: The answer is dp[n] — the number of distinct ways to
        //         reach the destination step n from step 0.
        // ---------------------------------------------------------------
        return (int) dp[n];
    }

    // ===================================================================
    // Main method — demonstrates the solution with the provided examples
    // and additional edge-case tests.
    // ===================================================================

    /**
     * Entry point for demonstration purposes.
     * Runs several test cases and prints the results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // -----------------------------------------------------------
        // Example 1 from the problem description
        // n = 5, forbidden = [2]
        // Expected output: 3
        // Valid paths: 0→1→3→4→5, 0→1→3→5, 0→1→4→5
        //
        // Trace through the DP table:
        //   dp[0] = 1
        //   dp[1] = 1          (step 1 is not forbidden)
        //   dp[2] = 0          (step 2 IS forbidden)
        //   dp[3] = dp[2] + dp[1] = 0 + 1 = 1
        //   dp[4] = dp[3] + dp[2] = 1 + 0 = 1
        //   dp[5] = dp[4] + dp[3] = 1 + 1 = 2  ← wait, let me re-trace
        //
        // Re-trace more carefully:
        //   dp[0] = 1
        //   dp[1] = 1
        //   dp[2] = 0  (forbidden)
        //   dp[3] = dp[2] + dp[1] = 0 + 1 = 1
        //   dp[4] = dp[3] + dp[2] = 1 + 0 = 1
        //   dp[5] = dp[4] + dp[3] = 1 + 1 = 2
        //
        // Hmm, that gives 2, but expected is 3. Let me re-read the paths:
        //   0→1→3→4→5  ✓
        //   0→1→3→5    ✓
        //   0→1→4→5    ✓
        //
        // Path 0→1→4→5: from 1 jump +3? No — we can only jump 1 or 2.
        // 0→1→4 means 1→4 is a jump of 3, which is NOT allowed.
        //
        // Wait, let me re-read: "0→1→4→5" — 1 to 4 is +3. That seems wrong
        // in the problem statement itself. Let me recount valid paths for n=5,
        // forbidden=[2] with jumps of 1 or 2 only:
        //
        //   0→1→3→4→5  (jumps: 1,2,1,1) ✓
        //   0→1→3→5    (jumps: 1,2,2)   ✓
        //   0→1→4→5    (jumps: 1,3,1)   ✗ — 3-jump not allowed!
        //
        // So the correct answer should be 2, not 3. The problem statement's
        // third path appears to be a typo. Our DP gives 2, which is correct
        // for the actual rules. We'll print what our algorithm computes.
        // -----------------------------------------------------------
        int result1 = solution.climbStairs(5, new int[]{2});
        System.out.println("=== Example 1 ===");
        System.out.println("n = 5, forbidden = [2]");
        System.out.println("Result  : " + result1);
        System.out.println("Expected: 3 (problem statement) / 2 (strict 1-or-2 jumps)");
        System.out.println();

        // -----------------------------------------------------------
        // Example 2 from the problem description
        // n = 4, forbidden = []
        // Expected output: 5
        //
        // Trace:
        //   dp[0] = 1
        //   dp[1] = 1
        //   dp[2] = dp[1] + dp[0] = 1 + 1 = 2
        //   dp[3] = dp[2] + dp[1] = 2 + 1 = 3
        //   dp[4] = dp[3] + dp[2] = 3 + 2 = 5  ✓
        // -----------------------------------------------------------
        int result2 = solution.climbStairs(4, new int[]{});
        System.out.println("=== Example 2 ===");
        System.out.println("n = 4, forbidden = []");
        System.out.println("Result  : " + result2);
        System.out.println("Expected: 5");
        System.out.println();

        // -----------------------------------------------------------
        // Additional test: n = 1, forbidden = []
        // Only one path: 0 → 1
        // Expected: 1
        // -----------------------------------------------------------
        int result3 = solution.climbStairs(1, new int[]{});
        System.out.println("=== Additional Test 1 ===");
        System.out.println("n = 1, forbidden = []");
        System.out.println("Result  : " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        // -----------------------------------------------------------
        // Additional test: n = 3, forbidden = [1]
        // Step 1 is forbidden, so we must jump 2 from 0 to reach step 2,
        // then 1 to reach step 3.
        // Only path: 0 → 2 → 3
        // Expected: 1
        //
        // Trace:
        //   dp[0] = 1
        //   dp[1] = 0  (forbidden)
        //   dp[2] = dp[1] + dp[0] = 0 + 1 = 1
        //   dp[3] = dp[2] + dp[1] = 1 + 0 = 1  ✓
        // -----------------------------------------------------------
        int result4 = solution.climbStairs(3, new int[]{1});
        System.out.println("=== Additional Test 2 ===");
        System.out.println("n = 3, forbidden = [1]");
        System.out.println("Result  : " + result4);
        System.out.println("Expected: 1");
        System.out.println();

        // -----------------------------------------------------------
        // Additional test: n = 3, forbidden = [1, 2]
        // Both step 1 and step 2 are forbidden.
        // We cannot reach step 3 at all (need to pass through 1 or 2).
        // Expected: 0
        //
        // Trace:
        //   dp[0] = 1
        //   dp[1] = 0  (forbidden)
        //   dp[2] = 0  (forbidden)
        //   dp[3] = dp[2] + dp[1] = 0 + 0 = 0  ✓
        // -----------------------------------------------------------
        int result5 = solution.climbStairs(3, new int[]{1, 2});
        System.out.println("=== Additional Test 3 ===");
        System.out.println("n = 3, forbidden = [1, 2]");
        System.out.println("Result  : " + result5);
        System.out.println("Expected: 0");
        System.out.println();

        // -----------------------------------------------------------
        // Additional test: n = 10, forbidden = []
        // Standard Fibonacci-like staircase problem.
        // dp values: 1,1,2,3,5,8,13,21,34,55,89
        // Expected: 89
        // -----------------------------------------------------------
        int result6 = solution.climbStairs(10, new int[]{});
        System.out.println("=== Additional Test 4 ===");
        System.out.println("n = 10, forbidden = []");
        System.out.println("Result  : " + result6);
        System.out.println("Expected: 89");
        System.out.println();

        // -----------------------------------------------------------
        // Additional test: n = 6, forbidden = [3]
        // Trace:
        //   dp[0] = 1
        //   dp[1] = 1
        //   dp[2] = 2
        //   dp[3] = 0  (forbidden)
        //   dp[4] = dp[3] + dp[2] = 0 + 2 = 2
        //   dp[5] = dp[4] + dp[3] = 2 + 0 = 2
        //   dp[6] = dp[5] + dp[4] = 2 + 2 = 4
        // Expected: 4
        // -----------------------------------------------------------
        int result7 = solution.climbStairs(6, new int[]{3});
        System.out.println("=== Additional Test 5 ===");
        System.out.println("n = 6, forbidden = [3]");
        System.out.println("Result  : " + result7);
        System.out.println("Expected: 4");
        System.out.println();

        // -----------------------------------------------------------
        // Large input test: n = 1000, forbidden = []
        // Should complete quickly (O