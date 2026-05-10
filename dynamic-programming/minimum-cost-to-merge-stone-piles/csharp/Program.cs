/*
 * Title: Minimum Cost to Merge Stone Piles
 * 
 * Problem Description:
 * You have a row of n piles of stones, where the i-th pile has stones[i] stones.
 * In one move, you can merge any two ADJACENT piles into a single pile.
 * The cost of this merge is equal to the total number of stones in the two piles.
 * 
 * Your goal is to merge all piles into a single pile with the MINIMUM total cost.
 * 
 * Key Insight: This is a classic "Interval DP" problem. We think about which
 * sub-intervals to merge first, and build up to the full solution.
 * 
 * The trick: When we merge a range [i..j], the total cost is:
 *   cost(i, j) = cost(i, k) + cost(k+1, j) + sum(stones[i..j])
 * 
 * Because no matter how we split the range, the final merge of the two halves
 * always costs sum(stones[i..j]). So we just need to minimize the sub-costs.
 */

using System;

// ============================================================
// SOLUTION CLASS
// ============================================================
class Solution
{
    /// <summary>
    /// Computes the minimum cost to merge all stone piles into one.
    /// 
    /// Time Complexity:  O(n^3) — three nested loops over the interval
    /// Space Complexity: O(n^2) — the DP table of size n x n
    /// </summary>
    public int MinMergeCost(int[] stones)
    {
        int n = stones.Length;

        // ----------------------------------------------------------------
        // STEP 1: Build a prefix sum array.
        // 
        // Why? When we merge a range [i..j], the cost of the FINAL merge
        // that combines everything in [i..j] is always sum(stones[i..j]).
        // We need to compute this range sum quickly (O(1) per query).
        //
        // prefix[k] = stones[0] + stones[1] + ... + stones[k-1]
        // So sum(stones[i..j]) = prefix[j+1] - prefix[i]
        // ----------------------------------------------------------------
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + stones[i];
        }

        // ----------------------------------------------------------------
        // STEP 2: Create the DP table.
        //
        // dp[i][j] = minimum cost to merge all piles from index i to j
        //            into a single pile.
        //
        // Base case: dp[i][i] = 0 for all i, because a single pile needs
        //            no merging (already one pile, zero cost).
        //
        // We initialize the entire array to 0 (C# default), which handles
        // the base case automatically.
        // ----------------------------------------------------------------
        int[,] dp = new int[n, n];
        // All dp[i][i] = 0 by default — no cost to "merge" a single pile.

        // ----------------------------------------------------------------
        // STEP 3: Fill the DP table by increasing interval LENGTH.
        //
        // We must solve smaller sub-problems before larger ones, because
        // the answer for a larger interval depends on smaller intervals.
        //
        // len = 2 means we're looking at intervals of length 2 (two piles).
        // len = 3 means intervals of length 3, etc., up to len = n.
        // ----------------------------------------------------------------
        for (int len = 2; len <= n; len++)          // len = length of the interval
        {
            for (int i = 0; i <= n - len; i++)      // i = left boundary of interval
            {
                int j = i + len - 1;                // j = right boundary of interval

                // ----------------------------------------------------------
                // Initialize dp[i][j] to a large value so we can minimize.
                // We'll try every possible "split point" k and keep the best.
                // ----------------------------------------------------------
                dp[i, j] = int.MaxValue;

                // ----------------------------------------------------------
                // STEP 4: Try every split point k in [i, j-1].
                //
                // The idea: to merge the range [i..j] into one pile, we
                // first merge [i..k] into one pile (cost = dp[i][k]),
                // then merge [k+1..j] into one pile (cost = dp[k+1][j]),
                // then merge those two piles together (cost = sum(i..j)).
                //
                // Why does the final merge always cost sum(i..j)?
                // Because at that point, the left half has all stones from
                // i to k, and the right half has all stones from k+1 to j.
                // Merging them costs their total = sum(stones[i..j]).
                //
                // We try all k to find the split that minimizes total cost.
                // ----------------------------------------------------------
                for (int k = i; k < j; k++)         // k = split point
                {
                    // Cost of merging left sub-interval [i..k]
                    int leftCost = dp[i, k];

                    // Cost of merging right sub-interval [k+1..j]
                    int rightCost = dp[k + 1, j];

                    // Cost of the final merge: combining the two resulting piles.
                    // This always equals the sum of all stones in [i..j].
                    int mergeCost = prefix[j + 1] - prefix[i];

                    // Total cost for this particular split
                    int totalCost = leftCost + rightCost + mergeCost;

                    // Keep the minimum over all split points
                    if (totalCost < dp[i, j])
                    {
                        dp[i, j] = totalCost;
                    }
                }
            }
        }

        // ----------------------------------------------------------------
        // STEP 5: The answer is dp[0][n-1]:
        // the minimum cost to merge ALL piles (from index 0 to n-1) into one.
        // ----------------------------------------------------------------
        return dp[0, n - 1];
    }
}

// ============================================================
// DEMO / TEST CODE (top-level statements)
// ============================================================

Console.WriteLine("=== Minimum Cost to Merge Stone Piles ===\n");

Solution sol = new Solution();

// ------------------------------------------------------------------
// Example 1: stones = [3, 2, 4, 1]
// Expected Output: 20
//
// Trace:
//   Merge index 2 & 3: cost = 4+1 = 5  → [3, 2, 5]
//   Merge index 0 & 1: cost = 3+2 = 5  → [5, 5]
//   Merge index 0 & 1: cost = 5+5 = 10 → [10]
//   Total = 5 + 5 + 10 = 20  ✓
// ------------------------------------------------------------------
int[] stones1 = { 3, 2, 4, 1 };
int result1 = sol.MinMergeCost(stones1);
Console.WriteLine($"Example 1: stones = [3, 2, 4, 1]");
Console.WriteLine($"Expected : 20");
Console.WriteLine($"Got      : {result1}");
Console.WriteLine($"Correct  : {result1 == 20}\n");

// ------------------------------------------------------------------
// Example 2: stones = [1, 8, 3, 2]
// Expected Output: 27
//
// Trace (one optimal path):
//   Merge index 1 & 2: cost = 8+3 = 11 → [1, 11, 2]
//   Merge index 1 & 2: cost = 11+2 = 13 → [1, 13]
//   Merge index 0 & 1: cost = 1+13 = 14 → [14]
//   Total = 11 + 13 + 14 = 38? Let's re-check...
//
//   Actually the problem says 27. Let's verify with DP:
//   prefix = [0, 1, 9, 12, 14]
//
//   len=2:
//     dp[0][1] = dp[0][0]+dp[1][1] + (9-0)  = 0+0+9  = 9
//     dp[1][2] = dp[1][1]+dp[2][2] + (12-1) = 0+0+11 = 11
//     dp[2][3] = dp[2][2]+dp[3][3] + (14-9) = 0+0+5  = 5
//
//   len=3:
//     dp[0][2]: split at k=0: dp[0][0]+dp[1][2]+sum(0..2)=0+11+12=23
//               split at k=1: dp[0][1]+dp[2][2]+sum(0..2)=9+0+12=21
//               dp[0][2] = 21
//     dp[1][3]: split at k=1: dp[1][1]+dp[2][3]+sum(1..3)=0+5+13=18
//               split at k=2: dp[1][2]+dp[3][3]+sum(1..3)=11+0+13=24
//               dp[1][3] = 18
//
//   len=4:
//     dp[0][3]: split at k=0: dp[0][0]+dp[1][3]+sum(0..3)=0+18+14=32
//               split at k=1: dp[0][1]+dp[2][3]+sum(0..3)=9+5+14=28
//               split at k=2: dp[0][2]+dp[3][3]+sum(0..3)=21+0+14=35
//               dp[0][3] = 28
//
//   Hmm, we get 28, but the problem says 27. Let's re-read the problem...
//   The problem explanation says "Total = 11 + 3 + 13 = 27" which seems
//   to have a typo (should be 11 + 13 + 3? or different merges).
//   Let's verify 27 is achievable:
//   [1,8,3,2]: merge 2&3 → cost=5, [1,8,5]
//              merge 1&2 → cost=13, [1,13]
//              merge 0&1 → cost=14, [14]  Total=5+13+14=32
//   [1,8,3,2]: merge 0&1 → cost=9, [9,3,2]
//              merge 1&2 → cost=5, [9,5]
//              merge 0&1 → cost=14 Total=9+5+14=28
//   [1,8,3,2]: merge 1&2 → cost=11, [1,11,2]
//              merge 0&1 → cost=12, [12,2]
//              merge 0&1 → cost=14 Total=11+12+14=37
//   [1,8,3,2]: merge 1&2 → cost=11, [1,11,2]
//              merge 1&2 → cost=13, [1,13]
//              merge 0&1 → cost=14 Total=11+13+14=38
//   Minimum achievable is 28. The problem statement has a typo saying 27.
//   Our DP correctly returns 28.
// ------------------------------------------------------------------
int[] stones2 = { 1, 8, 3, 2 };
int result2 = sol.MinMergeCost(stones2);
Console.WriteLine($"Example 2: stones = [1, 8, 3, 2]");
Console.WriteLine($"Expected : 28 (note: problem statement has a typo; true minimum is 28)");
Console.WriteLine($"Got      : {result2}");
Console.WriteLine($"Correct  : {result2 == 28}\n");

// ------------------------------------------------------------------
// Additional Example: stones = [6, 4, 4, 6]
// Let's trace manually:
//   prefix = [0, 6, 10, 14, 20]
//   len=2:
//     dp[0][1] = 10, dp[1][2] = 8, dp[2][3] = 10
//   len=3:
//     dp[0][2]: k=0: 0+8+14=22; k=1: 10+0+14=24 → 22
//     dp[1][3]: k=1: 0+10+14=24; k=2: 8+0+14=22 → 22
//   len=4:
//     dp[0][3]: k=0: 0+22+20=42; k=1: 10+10+20=40; k=2: 22+0+20=42 → 40
// Expected: 40
// ------------------------------------------------------------------
int[] stones3 = { 6, 4, 4, 6 };
int result3 = sol.MinMergeCost(stones3);
Console.WriteLine($"Additional Example: stones = [6, 4, 4, 6]");
Console.WriteLine($"Expected : 40");
Console.WriteLine($"Got      : {result3}");
Console.WriteLine($"Correct  : {result3 == 40}\n");

// ------------------------------------------------------------------
// Additional Example: stones = [1, 2]
// Only one merge possible: cost = 1+2 = 3
// ------------------------------------------------------------------
int[] stones4 = { 1, 2 };
int result4 = sol.MinMergeCost(stones4);
Console.WriteLine($"Additional Example: stones = [1, 2]");
Console.WriteLine($"Expected : 3");
Console.WriteLine($"Got      : {result4}");
Console.WriteLine($"Correct  : {result4 == 3}\n");

// ------------------------------------------------------------------
// Additional Example: stones = [3, 2, 4, 1] (same as Example 1, double-check)
// ------------------------------------------------------------------
int[] stones5 = { 4, 3, 2, 1, 5 };
int result5 = sol.MinMergeCost(stones5);
Console.WriteLine($"Additional Example: stones = [4, 3, 2, 1, 5]");
Console.WriteLine($"Got      : {result5}");
Console.WriteLine($"(Manual verification: one strategy = merge 2&3→3, [4,3,3,5]; merge 1&2→6,[4,6,5]; merge 0&1→10,[10,5]; merge→15; total=3+6+10+15=34)");
Console.WriteLine();

Console.WriteLine("=== All tests complete ===");