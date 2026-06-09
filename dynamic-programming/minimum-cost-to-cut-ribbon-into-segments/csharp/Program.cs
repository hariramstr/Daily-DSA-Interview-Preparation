/*
 * ============================================================
 * Title: Minimum Cost to Cut a Ribbon into Segments
 * ============================================================
 * Problem Description:
 * You have a ribbon of length n and an array cuts[] where cuts[i]
 * represents a position along the ribbon where a cut can be made.
 * Each cut at position cuts[i] has an associated cost costs[i].
 * You want to divide the ribbon into exactly k segments by making
 * exactly k-1 cuts.
 *
 * The cost of making a cut depends only on the specific cut chosen
 * (not on the order in which cuts are made). Your goal is to select
 * exactly k-1 cuts from the available options such that the total
 * cost is minimized.
 *
 * Return the minimum total cost to divide the ribbon into exactly k
 * segments. If it is impossible to make exactly k-1 cuts (i.e., there
 * are fewer than k-1 available cut positions), return -1.
 *
 * Constraints:
 *   - 2 <= k <= 100
 *   - 1 <= cuts.length <= 300
 *   - cuts.length == costs.length
 *   - All positions in cuts are distinct.
 *   - 1 <= costs[i] <= 10^4
 *
 * Example 1:
 *   Input:  cuts = [2, 5, 7, 9], costs = [3, 8, 2, 6], k = 3
 *   Output: 5
 *   Explanation: Choose cut at position 2 (cost 3) and position 7
 *                (cost 2). Total = 5.
 *
 * Example 2:
 *   Input:  cuts = [1, 4, 6], costs = [10, 5, 7], k = 4
 *   Output: 22
 *   Explanation: k-1 = 3 cuts needed, exactly 3 available, must use
 *                all. Total = 10 + 5 + 7 = 22.
 * ============================================================
 */

// ---------------------------------------------------------------
// Solution Class
// ---------------------------------------------------------------
public class Solution
{
    /// <summary>
    /// Finds the minimum cost to divide a ribbon into exactly k segments
    /// by selecting exactly k-1 cuts from the provided cut positions.
    ///
    /// Approach: Classic 0/1 Knapsack (subset selection) DP
    ///   - We need to choose exactly (k-1) items from n items.
    ///   - Each item has a "cost" (which we want to MINIMIZE).
    ///   - This is a "minimum cost exact-count knapsack" problem.
    ///
    /// DP Definition:
    ///   dp[j] = minimum total cost to make exactly j cuts
    ///           using the cuts considered so far.
    ///
    /// We use a 1D DP array of size k (indices 0..k-1).
    ///   dp[0] = 0   (zero cuts, zero cost — always achievable)
    ///   dp[j] = +Infinity initially for j > 0 (not yet achievable)
    ///
    /// For each cut i (with cost c):
    ///   Iterate j from (k-1) down to 1  [standard 0/1 knapsack direction]
    ///   dp[j] = min(dp[j], dp[j-1] + c)
    ///
    /// After processing all cuts:
    ///   If dp[k-1] is still +Infinity => impossible => return -1
    ///   Otherwise return dp[k-1].
    ///
    /// Time Complexity:  O(n * k)  where n = cuts.Length
    /// Space Complexity: O(k)      for the 1D DP array
    /// </summary>
    public int MinCostToCutRibbon(int[] cuts, int[] costs, int k)
    {
        // -------------------------------------------------------
        // Step 1: Determine how many cuts we need to make.
        // To create k segments we need exactly k-1 cuts.
        // -------------------------------------------------------
        int cutsNeeded = k - 1;

        // -------------------------------------------------------
        // Step 2: Edge case — if we need 0 cuts (k == 1), the
        // ribbon is already one segment. Cost is 0.
        // -------------------------------------------------------
        if (cutsNeeded == 0)
            return 0;

        // -------------------------------------------------------
        // Step 3: Edge case — if there are fewer available cut
        // positions than cuts needed, it's impossible.
        // Return -1 immediately.
        // -------------------------------------------------------
        int n = cuts.Length; // total number of available cut positions
        if (n < cutsNeeded)
            return -1;

        // -------------------------------------------------------
        // Step 4: Initialize the DP array.
        //
        // dp[j] represents the MINIMUM total cost to make exactly
        // j cuts using a subset of the cuts we've processed so far.
        //
        // Size: cutsNeeded + 1  (indices 0 through cutsNeeded)
        //
        // dp[0] = 0:
        //   Making 0 cuts costs nothing — this is our base case.
        //   It is always achievable regardless of which cuts exist.
        //
        // dp[j] = int.MaxValue / 2 for j > 0:
        //   We use int.MaxValue / 2 (a large sentinel) instead of
        //   int.MaxValue to avoid integer overflow when we do
        //   dp[j-1] + cost later. This sentinel means "not yet
        //   achievable with the cuts seen so far."
        // -------------------------------------------------------
        int infinity = int.MaxValue / 2; // safe large value to avoid overflow
        int[] dp = new int[cutsNeeded + 1];

        // Fill all entries with "infinity" first
        for (int j = 0; j <= cutsNeeded; j++)
            dp[j] = infinity;

        // Base case: 0 cuts costs 0
        dp[0] = 0;

        // -------------------------------------------------------
        // Step 5: Process each available cut one by one.
        //
        // This is the standard 0/1 Knapsack loop structure.
        // We iterate over each "item" (cut) and decide whether to
        // include it or not.
        //
        // Why 0/1 Knapsack?
        //   Each cut position can be used AT MOST ONCE (you can't
        //   cut the same position twice). The 0/1 knapsack ensures
        //   each item is considered only once per solution.
        // -------------------------------------------------------
        for (int i = 0; i < n; i++)
        {
            // The cost of making this particular cut
            int cutCost = costs[i];

            // ---------------------------------------------------
            // Step 6: Update the DP array in REVERSE order.
            //
            // Why reverse (from cutsNeeded down to 1)?
            //   In a 0/1 knapsack, iterating in reverse ensures
            //   that each cut is used AT MOST ONCE per solution.
            //   If we iterated forward, we might use the same cut
            //   multiple times (which would turn it into an
            //   unbounded knapsack — not what we want here).
            //
            // Transition:
            //   dp[j] = min(dp[j],          <- don't use cut i
            //               dp[j-1] + cutCost) <- use cut i
            //
            // In plain English:
            //   "The minimum cost to make exactly j cuts is either:
            //    (a) the current best without using cut i, OR
            //    (b) the best way to make j-1 cuts (not using cut i)
            //        plus the cost of cut i."
            // ---------------------------------------------------
            for (int j = cutsNeeded; j >= 1; j--)
            {
                // Only update if dp[j-1] is reachable (not infinity).
                // This prevents propagating unreachable states.
                if (dp[j - 1] < infinity)
                {
                    dp[j] = Math.Min(dp[j], dp[j - 1] + cutCost);
                }
            }
        }

        // -------------------------------------------------------
        // Step 7: Check the result.
        //
        // dp[cutsNeeded] now holds the minimum cost to make exactly
        // (k-1) cuts using a subset of the available cut positions.
        //
        // If it's still "infinity", it means we couldn't form
        // exactly (k-1) cuts — return -1.
        // Otherwise, return the computed minimum cost.
        // -------------------------------------------------------
        if (dp[cutsNeeded] >= infinity)
            return -1;

        return dp[cutsNeeded];
    }
}

// ---------------------------------------------------------------
// Demo / Test Code (Top-Level Statements)
// ---------------------------------------------------------------

var solution = new Solution();

// -------------------------------------------------------------------
// Example 1:
//   cuts  = [2, 5, 7, 9]
//   costs = [3, 8, 2, 6]
//   k     = 3
//
// We need k-1 = 2 cuts.
// Available cuts with costs:
//   position 2 -> cost 3
//   position 5 -> cost 8
//   position 7 -> cost 2
//   position 9 -> cost 6
//
// Best 2 cuts: position 2 (cost 3) + position 7 (cost 2) = 5
// Expected Output: 5
// -------------------------------------------------------------------
int[] cuts1  = { 2, 5, 7, 9 };
int[] costs1 = { 3, 8, 2, 6 };
int k1 = 3;
int result1 = solution.MinCostToCutRibbon(cuts1, costs1, k1);
Console.WriteLine("=== Example 1 ===");
Console.WriteLine($"cuts  = [{string.Join(", ", cuts1)}]");
Console.WriteLine($"costs = [{string.Join(", ", costs1)}]");
Console.WriteLine($"k     = {k1}");
Console.WriteLine($"Result: {result1}");   // Expected: 5
Console.WriteLine();

// -------------------------------------------------------------------
// Example 2:
//   cuts  = [1, 4, 6]
//   costs = [10, 5, 7]
//   k     = 4
//
// We need k-1 = 3 cuts.
// Exactly 3 cuts available, so we must use all of them.
// Total cost = 10 + 5 + 7 = 22
// Expected Output: 22
// -------------------------------------------------------------------
int[] cuts2  = { 1, 4, 6 };
int[] costs2 = { 10, 5, 7 };
int k2 = 4;
int result2 = solution.MinCostToCutRibbon(cuts2, costs2, k2);
Console.WriteLine("=== Example 2 ===");
Console.WriteLine($"cuts  = [{string.Join(", ", cuts2)}]");
Console.WriteLine($"costs = [{string.Join(", ", costs2)}]");
Console.WriteLine($"k     = {k2}");
Console.WriteLine($"Result: {result2}");   // Expected: 22
Console.WriteLine();

// -------------------------------------------------------------------
// Example 3 (Impossible Case):
//   cuts  = [3, 7]
//   costs = [4, 9]
//   k     = 5
//
// We need k-1 = 4 cuts, but only 2 are available.
// Expected Output: -1
// -------------------------------------------------------------------
int[] cuts3  = { 3, 7 };
int[] costs3 = { 4, 9 };
int k3 = 5;
int result3 = solution.MinCostToCutRibbon(cuts3, costs3, k3);
Console.WriteLine("=== Example 3 (Impossible) ===");
Console.WriteLine($"cuts  = [{string.Join(", ", cuts3)}]");
Console.WriteLine($"costs = [{string.Join(", ", costs3)}]");
Console.WriteLine($"k     = {k3}");
Console.WriteLine($"Result: {result3}");   // Expected: -1
Console.WriteLine();

// -------------------------------------------------------------------
// Example 4 (k == 1, no cuts needed):
//   cuts  = [2, 5]
//   costs = [3, 8]
//   k     = 1
//
// We need k-1 = 0 cuts. The ribbon is already 1 segment.
// Expected Output: 0
// -------------------------------------------------------------------
int[] cuts4  = { 2, 5 };
int[] costs4 = { 3, 8 };
int k4 = 1;
int result4 = solution.MinCostToCutRibbon(cuts4, costs4, k4);
Console.WriteLine("=== Example 4 (k=1, no cuts needed) ===");
Console.WriteLine($"cuts  = [{string.Join(", ", cuts4)}]");
Console.WriteLine($"costs = [{string.Join(", ", costs4)}]");
Console.WriteLine($"k     = {k4}");
Console.WriteLine($"Result: {result4}");   // Expected: 0
Console.WriteLine();

// -------------------------------------------------------------------
// Example 5 (Single cut selection from many):
//   cuts  = [1, 2, 3, 4, 5]
//   costs = [9, 1, 9, 9, 9]
//   k     = 2
//
// We need k-1 = 1 cut. Cheapest cut is at position 2 (cost 1).
// Expected Output: 1
// -------------------------------------------------------------------
int[] cuts5  = { 1, 2, 3, 4, 5 };
int[] costs5 = { 9, 1, 9, 9, 9 };
int k5 = 2;
int result5 = solution.MinCostToCutRibbon(cuts5, costs5, k5);
Console.WriteLine("=== Example 5 (Pick cheapest single cut) ===");
Console.WriteLine($"cuts  = [{string.Join(", ", cuts5)}]");
Console.WriteLine($"costs = [{string.Join(", ", costs5)}]");
Console.WriteLine($"k     = {k5}");
Console.WriteLine($"Result: {result5}");   // Expected: 1
Console.WriteLine();