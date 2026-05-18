/*
 * ============================================================
 * Title: Staircase Jump with Forbidden Steps
 * ============================================================
 * Problem Description:
 * You are climbing a staircase with `n` steps. At each step,
 * you can jump either 1 or 2 steps forward. However, some steps
 * are marked as forbidden — you cannot land on any forbidden step.
 * Given the total number of steps `n` and a list of forbidden step
 * indices, return the number of distinct ways to reach step `n`
 * starting from step `0`. Since the answer may be large, return it
 * modulo 10^9 + 7.
 *
 * Note: Step 0 is your starting position and is never forbidden.
 *       You must reach exactly step n.
 *
 * Constraints:
 *   - 1 <= n <= 1000
 *   - 0 <= forbidden.length <= n - 1
 *   - All values in forbidden are distinct integers in [1, n-1]
 *   - Step 0 and step n are never in the forbidden list
 *
 * Example 1:
 *   Input:  n = 5, forbidden = [2]
 *   Output: 3
 *   Paths: 0→1→3→4→5, 0→1→3→5, 0→1→4→5
 *
 * Example 2:
 *   Input:  n = 4, forbidden = []
 *   Output: 5
 *   Paths: 0→1→2→3→4, 0→1→2→4, 0→1→3→4, 0→2→3→4, 0→2→4
 * ============================================================
 */

using System;
using System.Collections.Generic;

// ============================================================
// Solution Class
// ============================================================
public class StaircaseJumpSolution
{
    /// <summary>
    /// Counts the number of distinct ways to climb from step 0 to step n,
    /// jumping 1 or 2 steps at a time, while avoiding forbidden steps.
    ///
    /// Time Complexity:  O(n) — we fill a DP table of size n+1, each cell in O(1)
    /// Space Complexity: O(n) — we store the DP table of size n+1, plus a HashSet for forbidden steps
    /// </summary>
    /// <param name="n">The target step to reach.</param>
    /// <param name="forbidden">Array of step indices that cannot be landed on.</param>
    /// <returns>Number of distinct valid paths modulo 10^9 + 7.</returns>
    public int CountWays(int n, int[] forbidden)
    {
        // -------------------------------------------------------
        // STEP 1: Define the modulus constant.
        // Since the number of paths can grow exponentially, we take
        // all results modulo 10^9 + 7 (a common large prime used in
        // competitive programming to keep numbers manageable).
        // -------------------------------------------------------
        const int MOD = 1_000_000_007;

        // -------------------------------------------------------
        // STEP 2: Store forbidden steps in a HashSet for O(1) lookup.
        // Using a HashSet instead of a list means we can check
        // "is step X forbidden?" in constant time rather than
        // scanning the entire forbidden array each time.
        // -------------------------------------------------------
        HashSet<int> forbiddenSet = new HashSet<int>(forbidden);

        // -------------------------------------------------------
        // STEP 3: Create the DP (Dynamic Programming) table.
        // dp[i] = number of distinct ways to reach step i from step 0.
        //
        // Why DP?
        //   At any step i, we could have arrived from step i-1 (1-step jump)
        //   or from step i-2 (2-step jump). So:
        //       dp[i] = dp[i-1] + dp[i-2]
        //   This is essentially a Fibonacci-like recurrence, but with
        //   the twist that forbidden steps have dp[i] = 0 (unreachable).
        //
        // We allocate n+1 slots: indices 0 through n (inclusive).
        // -------------------------------------------------------
        long[] dp = new long[n + 1];

        // -------------------------------------------------------
        // STEP 4: Base case — there is exactly 1 way to be at step 0
        // (we start there; no moves needed).
        // -------------------------------------------------------
        dp[0] = 1;

        // -------------------------------------------------------
        // STEP 5: Fill the DP table from step 1 to step n.
        // For each step i, we determine how many ways we can reach it.
        // -------------------------------------------------------
        for (int i = 1; i <= n; i++)
        {
            // ---------------------------------------------------
            // STEP 5a: If step i is forbidden, we cannot land here.
            // Set dp[i] = 0 and skip to the next step.
            // This ensures no paths "pass through" a forbidden step.
            // ---------------------------------------------------
            if (forbiddenSet.Contains(i))
            {
                dp[i] = 0;
                continue; // move on; forbidden steps contribute nothing
            }

            // ---------------------------------------------------
            // STEP 5b: Count ways arriving from step i-1 (1-step jump).
            // If i-1 >= 0 (always true since i >= 1), we can come
            // from step i-1. Add dp[i-1] to dp[i].
            // ---------------------------------------------------
            dp[i] = dp[i - 1]; // ways from a 1-step jump

            // ---------------------------------------------------
            // STEP 5c: Count ways arriving from step i-2 (2-step jump).
            // Only valid if i-2 >= 0, i.e., i >= 2.
            // Add dp[i-2] to dp[i].
            // ---------------------------------------------------
            if (i >= 2)
            {
                dp[i] = (dp[i] + dp[i - 2]) % MOD;
            }

            // ---------------------------------------------------
            // STEP 5d: Apply modulo to keep the number within bounds.
            // We apply MOD after adding dp[i-1] as well (for safety,
            // even though dp[i-1] is already reduced, the sum could
            // theoretically overflow without the cast to long).
            // ---------------------------------------------------
            dp[i] %= MOD;
        }

        // -------------------------------------------------------
        // STEP 6: Return the answer.
        // dp[n] holds the total number of distinct valid paths
        // from step 0 to step n, modulo 10^9 + 7.
        // -------------------------------------------------------
        return (int)dp[n];
    }
}

// ============================================================
// Demo / Test Code (top-level statements)
// ============================================================

Console.WriteLine("==============================================");
Console.WriteLine("   Staircase Jump with Forbidden Steps Demo  ");
Console.WriteLine("==============================================\n");

var solution = new StaircaseJumpSolution();

// ----------------------------------------------------------
// Example 1: n = 5, forbidden = [2]
// Expected Output: 3
// Trace:
//   dp[0] = 1  (base case)
//   dp[1] = dp[0] = 1                  (step 1 is reachable)
//   dp[2] = 0                          (step 2 is FORBIDDEN)
//   dp[3] = dp[2] + dp[1] = 0 + 1 = 1 (can come from step 1 via 2-jump, or step 2 via 1-jump but dp[2]=0)
//   dp[4] = dp[3] + dp[2] = 1 + 0 = 1
//   dp[5] = dp[4] + dp[3] = 1 + 1 = 2
// Wait — let me re-trace carefully:
//   dp[0] = 1
//   dp[1] = dp[0] = 1
//   dp[2] = 0  (forbidden)
//   dp[3] = dp[2] + dp[1] = 0 + 1 = 1
//   dp[4] = dp[3] + dp[2] = 1 + 0 = 1
//   dp[5] = dp[4] + dp[3] = 1 + 1 = 2
// Hmm, that gives 2, but expected is 3.
// Let me re-check the paths: 0→1→3→4→5, 0→1→3→5, 0→1→4→5
//   0→1→3→4→5: uses steps 1,3,4,5  ✓
//   0→1→3→5:   uses steps 1,3,5    ✓
//   0→1→4→5:   uses steps 1,4,5    ✓
// dp[1]=1, dp[3]=dp[2]+dp[1]=0+1=1, dp[4]=dp[3]+dp[2]=1+0=1, dp[5]=dp[4]+dp[3]=1+1=2
// But path 0→1→4→5 goes 0→1 (1-jump), 1→4? That's a 3-jump! Not allowed.
// Actually 0→1→4 means 1→4 is a jump of 3 — that's invalid with only 1 or 2 step jumps.
// So the problem statement example seems to have an error, OR I'm misreading.
// Re-reading: 0→1→4→5: 0 to 1 (+1), 1 to 4 (+3)? That can't be right with jumps of 1 or 2.
// Actually wait: maybe the example in the problem is wrong, or maybe it's 0→1→2→4→5 but 2 is forbidden.
// Let me just verify my DP gives the correct mathematical answer. With n=5, forbidden=[2]:
// Valid paths using only +1 or +2 jumps, avoiding step 2:
//   0→1→3→4→5 ✓
//   0→1→3→5   ✓
// That's only 2. My DP gives 2. The problem's example 3rd path seems incorrect.
// I'll trust the DP logic and note the discrepancy.
// ----------------------------------------------------------

int result1 = solution.CountWays(5, new int[] { 2 });
Console.WriteLine($"Example 1: n=5, forbidden=[2]");
Console.WriteLine($"  Result  : {result1}");
Console.WriteLine($"  Expected: 3 (problem statement) / 2 (verified by DP trace)");
Console.WriteLine($"  Valid paths (only +1/+2 jumps, no step 2):");
Console.WriteLine($"    0→1→3→4→5");
Console.WriteLine($"    0→1→3→5");
Console.WriteLine();

// ----------------------------------------------------------
// Example 2: n = 4, forbidden = []
// Expected Output: 5
// Trace (standard Fibonacci staircase):
//   dp[0] = 1
//   dp[1] = dp[0] = 1
//   dp[2] = dp[1] + dp[0] = 1 + 1 = 2
//   dp[3] = dp[2] + dp[1] = 2 + 1 = 3
//   dp[4] = dp[3] + dp[2] = 3 + 2 = 5  ✓
// ----------------------------------------------------------

int result2 = solution.CountWays(4, new int[] { });
Console.WriteLine($"Example 2: n=4, forbidden=[]");
Console.WriteLine($"  Result  : {result2}");
Console.WriteLine($"  Expected: 5  ✓");
Console.WriteLine($"  Valid paths:");
Console.WriteLine($"    0→1→2→3→4");
Console.WriteLine($"    0→1→2→4");
Console.WriteLine($"    0→1→3→4");
Console.WriteLine($"    0→2→3→4");
Console.WriteLine($"    0→2→4");
Console.WriteLine();

// ----------------------------------------------------------
// Additional Test: n = 1, forbidden = []
// Expected: 1 (only path: 0→1)
// ----------------------------------------------------------
int result3 = solution.CountWays(1, new int[] { });
Console.WriteLine($"Additional Test: n=1, forbidden=[]");
Console.WriteLine($"  Result  : {result3}");
Console.WriteLine($"  Expected: 1  ✓");
Console.WriteLine();

// ----------------------------------------------------------
// Additional Test: n = 6, forbidden = [1, 3]
// Trace:
//   dp[0] = 1
//   dp[1] = 0  (forbidden)
//   dp[2] = dp[1] + dp[0] = 0 + 1 = 1
//   dp[3] = 0  (forbidden)
//   dp[4] = dp[3] + dp[2] = 0 + 1 = 1
//   dp[5] = dp[4] + dp[3] = 1 + 0 = 1
//   dp[6] = dp[5] + dp[4] = 1 + 1 = 2
// Paths: 0→2→4→5→6, 0→2→4→6
// ----------------------------------------------------------
int result4 = solution.CountWays(6, new int[] { 1, 3 });
Console.WriteLine($"Additional Test: n=6, forbidden=[1,3]");
Console.WriteLine($"  Result  : {result4}");
Console.WriteLine($"  Expected: 2");
Console.WriteLine($"  Valid paths:");
Console.WriteLine($"    0→2→4→5→6");
Console.WriteLine($"    0→2→4→6");
Console.WriteLine();

// ----------------------------------------------------------
// Additional Test: Large n to verify modulo works
// n = 1000, forbidden = []
// ----------------------------------------------------------
int result5 = solution.CountWays(1000, new int[] { });
Console.WriteLine($"Additional Test: n=1000, forbidden=[]");
Console.WriteLine($"  Result (mod 10^9+7): {result5}");
Console.WriteLine($"  (Large Fibonacci number, modulo applied correctly)");
Console.WriteLine();

Console.WriteLine("==============================================");
Console.WriteLine("              All tests complete!            ");
Console.WriteLine("==============================================");