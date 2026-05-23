/*
 * Title: Minimum Maximum Distance Between Placed Towers
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given a list of n candidate positions along a straight road, represented as a
 * sorted array of distinct integers `positions`. You must place exactly k communication
 * towers among these candidate positions. Your goal is to minimize the maximum distance
 * between any two consecutively placed towers.
 *
 * The first position positions[0] and the last position positions[n-1] must always have
 * a tower. Place the remaining k-2 towers at any of the other candidate positions to
 * minimize the maximum gap between any two adjacent towers.
 *
 * Return the minimum possible value of the maximum distance between any two consecutively
 * placed towers, rounded to 5 decimal places.
 *
 * Key Insight:
 * Binary search on the answer (the maximum allowed gap). For a given maximum gap D,
 * check if we can place exactly k towers (including first and last) such that no
 * consecutive gap exceeds D. We greedily count how many towers are needed.
 *
 * Example 1: positions = [1,3,6,7,12,19], k=3
 *   We must place towers at 1 and 19 (first and last), plus 1 more.
 *   Binary search finds the minimum max gap = 9.00000
 *   (Place at 1, 12, 19 → gaps 11, 7 → max=11; or try other combos)
 *   Wait - let me re-examine: with k=3, we place at positions[0]=1, one middle, positions[5]=19
 *   Best middle choices from {3,6,7,12}:
 *     1,3,19 → max(2,16)=16
 *     1,6,19 → max(5,13)=13
 *     1,7,19 → max(6,12)=12
 *     1,12,19 → max(11,7)=11
 *   Minimum max gap = 11. But problem says 9.00000... 
 *   After careful re-reading: the problem statement's own explanation is contradictory.
 *   The correct answer for example 1 based on valid candidate positions is 11.00000.
 *   We will implement the correct algorithm and trust the math.
 */

using System;
using System.Collections.Generic;

/// <summary>
/// Solution class for Minimum Maximum Distance Between Placed Towers.
/// Uses binary search on the answer combined with a greedy feasibility check.
/// </summary>
public class Solution
{
    /// <summary>
    /// Finds the minimum possible maximum distance between consecutively placed towers.
    ///
    /// Time Complexity:  O(n * log(maxGap / epsilon))
    ///   - Binary search runs ~50 iterations for floating-point precision
    ///   - Each feasibility check is O(n) scanning through positions
    ///   - Total: O(n * 50) = O(n)
    ///
    /// Space Complexity: O(1) — only a few variables, no extra data structures
    /// </summary>
    /// <param name="positions">Sorted array of candidate tower positions</param>
    /// <param name="k">Number of towers to place (including first and last)</param>
    /// <returns>Minimum possible maximum gap, rounded to 5 decimal places</returns>
    public double MinimizeMaxDistance(int[] positions, int k)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Understand the search space
        // -----------------------------------------------------------------------
        // The answer (minimum possible maximum gap) lies somewhere between:
        //   - Low bound: 0.0 (theoretical minimum, not achievable unless k is huge)
        //   - High bound: positions[n-1] - positions[0] (the total span, when k=2)
        //
        // We binary search on this range to find the smallest D such that
        // we can place k towers with no gap exceeding D.
        // -----------------------------------------------------------------------

        int n = positions.Length;

        // The minimum possible answer is 0 (lower bound for binary search)
        double lo = 0.0;

        // The maximum possible answer is the full span (when only 2 towers: first and last)
        double hi = positions[n - 1] - positions[0];

        // -----------------------------------------------------------------------
        // STEP 2: Binary search on the answer
        // -----------------------------------------------------------------------
        // We perform ~100 iterations of binary search on floating-point values.
        // This gives us precision of (hi - lo) / 2^100, which is far beyond 5 decimal places.
        // Using iteration count instead of epsilon comparison is more reliable.
        // -----------------------------------------------------------------------

        // 100 iterations gives precision well beyond 5 decimal places
        for (int iter = 0; iter < 100; iter++)
        {
            // Calculate the midpoint of our current search range
            double mid = (lo + hi) / 2.0;

            // Check if it's FEASIBLE to place k towers with max gap <= mid
            if (CanPlace(positions, k, mid))
            {
                // If feasible, maybe we can do even better (smaller max gap)
                // So we search the lower half
                hi = mid;
            }
            else
            {
                // If not feasible, we need a larger allowed gap
                // So we search the upper half
                lo = mid;
            }
        }

        // -----------------------------------------------------------------------
        // STEP 3: Round to 5 decimal places and return
        // -----------------------------------------------------------------------
        // After binary search converges, hi (or lo) holds our answer.
        // We round to 5 decimal places as required.
        // -----------------------------------------------------------------------

        return Math.Round(hi, 5);
    }

    /// <summary>
    /// Greedy feasibility check: Can we place exactly k towers (first and last fixed)
    /// such that no consecutive gap exceeds maxGap?
    ///
    /// Strategy: Start at positions[0]. Greedily jump as far as possible
    /// (to the rightmost candidate position still within maxGap).
    /// Count how many towers we need. If count <= k, it's feasible.
    ///
    /// Why greedy works: Placing a tower as far right as possible (while staying
    /// within maxGap) leaves the most room for future towers — it never hurts
    /// to delay placing a tower as long as possible.
    /// </summary>
    /// <param name="positions">Sorted candidate positions</param>
    /// <param name="k">Maximum number of towers we're allowed to use</param>
    /// <param name="maxGap">The maximum allowed gap between consecutive towers</param>
    /// <returns>True if k towers suffice to cover all gaps within maxGap</returns>
    private bool CanPlace(int[] positions, int k, double maxGap)
    {
        // -----------------------------------------------------------------------
        // GREEDY APPROACH:
        // We start at the first position (mandatory tower).
        // We scan right and find the furthest candidate position we can reach
        // within maxGap. We place a tower there, and repeat until we reach the end.
        // Count total towers used. If <= k, return true.
        // -----------------------------------------------------------------------

        int n = positions.Length;

        // We always start with a tower at positions[0]
        // towersUsed starts at 1 (for the first tower)
        int towersUsed = 1;

        // 'lastTower' tracks the position of the most recently placed tower
        double lastTower = positions[0];

        // Scan through all positions from left to right
        for (int i = 1; i < n; i++)
        {
            // Check if the gap from the last placed tower to this position exceeds maxGap
            if (positions[i] - lastTower > maxGap + 1e-9)
            {
                // ---------------------------------------------------------------
                // We MUST place a tower before positions[i] because the gap is too large.
                // The best choice is positions[i-1] — the last valid position we passed.
                // This is the greedy "place as far right as possible" strategy.
                // ---------------------------------------------------------------

                // Place tower at positions[i-1] (the previous position)
                // Note: positions[i-1] was within maxGap of lastTower (otherwise
                // we would have placed a tower even earlier)
                lastTower = positions[i - 1];
                towersUsed++;

                // If we've already used more towers than allowed, it's infeasible
                if (towersUsed > k)
                    return false;

                // Now re-check positions[i] against the newly placed tower
                // If even positions[i] is too far from positions[i-1], it's infeasible
                // (This handles cases where consecutive candidates are farther than maxGap)
                if (positions[i] - lastTower > maxGap + 1e-9)
                {
                    // Even adjacent candidates are too far apart — impossible
                    return false;
                }
            }
        }

        // -----------------------------------------------------------------------
        // After scanning all positions, we must ensure the last position has a tower.
        // The last position positions[n-1] must always have a tower.
        // If lastTower != positions[n-1], we need one more tower there.
        // -----------------------------------------------------------------------

        // Check if we need to add the final mandatory tower at positions[n-1]
        if (Math.Abs(lastTower - positions[n - 1]) > 1e-9)
        {
            towersUsed++;
        }

        // It's feasible if we used at most k towers
        return towersUsed <= k;
    }
}

// =============================================================================
// DEMO / TEST CODE
// =============================================================================
// Top-level statements: no Main method needed in .NET 6+

Console.WriteLine("=== Minimum Maximum Distance Between Placed Towers ===");
Console.WriteLine();

var solution = new Solution();

// -----------------------------------------------------------------------
// Example 1: positions = [1, 3, 6, 7, 12, 19], k = 3
// Must place towers at 1 and 19, plus 1 more from {3, 6, 7, 12}
// Best option: 1, 12, 19 → gaps = 11, 7 → max = 11
// Expected output: 11.00000
// -----------------------------------------------------------------------
int[] positions1 = { 1, 3, 6, 7, 12, 19 };
int k1 = 3;
double result1 = solution.MinimizeMaxDistance(positions1, k1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  positions = [1, 3, 6, 7, 12, 19], k = {k1}");
Console.WriteLine($"  Candidate placements (must include 1 and 19, plus 1 more):");
Console.WriteLine($"    1, 3,  19 → max gap = 16");
Console.WriteLine($"    1, 6,  19 → max gap = 13");
Console.WriteLine($"    1, 7,  19 → max gap = 12");
Console.WriteLine($"    1, 12, 19 → max gap = 11  ← optimal");
Console.WriteLine($"  Result: {result1:F5}");
Console.WriteLine($"  Expected: 11.00000");
Console.WriteLine();

// -----------------------------------------------------------------------
// Example 2: positions = [0, 5, 10, 15, 20], k = 4
// Must place at 0 and 20, plus 2 more from {5, 10, 15}
// Best: 0, 5, 15, 20 → gaps 5, 10, 5 → max=10
//   or: 0, 10, 15, 20 → gaps 10, 5, 5 → max=10
//   or: 0, 5, 10, 20 → gaps 5, 5, 10 → max=10
// Expected output: 10.00000
// -----------------------------------------------------------------------
int[] positions2 = { 0, 5, 10, 15, 20 };
int k2 = 4;
double result2 = solution.MinimizeMaxDistance(positions2, k2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  positions = [0, 5, 10, 15, 20], k = {k2}");
Console.WriteLine($"  Best placement: 0, 5, 10, 20 → gaps 5, 5, 10 → max=10");
Console.WriteLine($"  Result: {result2:F5}");
Console.WriteLine($"  Expected: 10.00000");
Console.WriteLine();

// -----------------------------------------------------------------------
// Example 3: k = n (place tower at every position)
// positions = [0, 5, 10, 15, 20], k = 5
// All positions get a tower → gaps = 5, 5, 5, 5 → max = 5
// Expected output: 5.00000
// -----------------------------------------------------------------------
int[] positions3 = { 0, 5, 10, 15, 20 };
int k3 = 5;
double result3 = solution.MinimizeMaxDistance(positions3, k3);
Console.WriteLine($"Example 3 (k = n, all positions used):");
Console.WriteLine($"  positions = [0, 5, 10, 15, 20], k = {k3}");
Console.WriteLine($"  All positions used → gaps = 5, 5, 5, 5 → max = 5");
Console.WriteLine($"  Result: {result3:F5}");
Console.WriteLine($"  Expected: 5.00000");
Console.WriteLine();

// -----------------------------------------------------------------------
// Example 4: k = 2 (only first and last)
// positions = [1, 3, 6, 7, 12, 19], k = 2
// Only towers at 1 and 19 → gap = 18
// Expected output: 18.00000
// -----------------------------------------------------------------------
int[] positions4 = { 1, 3, 6, 7, 12, 19 };
int k4 = 2;
double result4 = solution.MinimizeMaxDistance(positions4, k4);
Console.WriteLine($"Example 4 (k = 2, only endpoints):");
Console.WriteLine($"  positions = [1, 3, 6, 7, 12, 19], k = {k4}");
Console.WriteLine($"  Only towers at 1 and 19 → gap = 18");
Console.WriteLine($"  Result: {result4:F5}");
Console.WriteLine($"  Expected: 18.00000");
Console.WriteLine();

// -----------------------------------------------------------------------
// Example 5: Large gap scenario
// positions = [0, 100], k = 2
// Only two positions, must use both → gap = 100
// Expected output: 100.00000
// -----------------------------------------------------------------------
int[] positions5 = { 0, 100 };
int k5 = 2;
double result5 = solution.MinimizeMaxDistance(positions5, k5);
Console.WriteLine($"Example 5 (two positions only):");
Console.WriteLine($"  positions = [0, 100], k = {k5}");
Console.WriteLine($"  Only towers at 0 and 100 → gap = 100");
Console.WriteLine($"  Result: {result5:F5}");
Console.WriteLine($"  Expected: 100.00000");
Console.WriteLine();

// -----------------------------------------------------------------------
// Example 6: Verify with a case where optimal split is non-trivial
// positions = [0, 3, 6, 10], k = 3
// Must place at 0 and 10, plus 1 more from {3, 6}
//   0, 3, 10 → gaps 3, 7 → max=7
//   0, 6, 10 → gaps 6, 4 → max=6  ← optimal
// Expected output: 6.00000
// -----------------------------------------------------------------------
int[] positions6 = { 0, 3, 6, 10 };
int k6 = 3;
double result6 = solution.MinimizeMaxDistance(positions6, k6);
Console.WriteLine($"Example 6 (non-trivial optimal split):");
Console.WriteLine($"  positions = [0, 3, 6, 10], k = {k6}");