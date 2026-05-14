/*
 * Title: Minimum Days to Deliver All Packages
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A delivery company has `n` packages lined up in a row, where `packages[i]` represents
 * the weight of the i-th package. A delivery truck can carry packages on any given day,
 * but it has two constraints:
 *
 * 1. The truck can carry at most `k` consecutive packages per day (contiguous in original order).
 * 2. The truck can carry packages with a total weight of at most `capacity` per trip.
 *
 * Packages must be delivered in order (you cannot skip a package and come back to it later).
 * Each day, the truck picks the next batch of consecutive packages starting where it left off,
 * subject to both constraints above.
 *
 * Given the array `packages`, an integer `k`, and an integer `capacity`, return the
 * minimum number of days needed to deliver all packages.
 *
 * Constraints:
 * - 1 <= packages.length <= 10^5
 * - 1 <= packages[i] <= 500
 * - 1 <= k <= packages.length
 * - 1 <= capacity <= 10^7
 * - It is guaranteed that each individual package weight does not exceed capacity.
 *
 * Example 1:
 * Input: packages = [3, 2, 4, 1, 5, 2, 3], k = 3, capacity = 7
 * Output: 3
 * Wait — let's trace: greedy simulation:
 *   Day 1: start=0, take up to k=3 packages within capacity=7
 *     [3]=3, [3,2]=5, [3,2,4]=9>7 → take [3,2], next=2
 *   Day 2: start=2, [4]=4, [4,1]=5, [4,1,5]=10>7 → take [4,1], next=4
 *   Day 3: start=4, [5]=5, [5,2]=7, [5,2,3]=10>7 → take [5,2], next=6
 *   Day 4: start=6, [3]=3 → take [3], done
 *   Total = 4 days
 * Hmm, but problem says output=3. Let me re-read...
 * Actually the problem says "binary search on number of days" — but the greedy simulation
 * gives 4 for example 1. The problem statement itself shows confusion in the explanation.
 * Let's verify example 2: packages=[1,2,3,4,5], k=2, capacity=6
 *   Day 1: [1]=1,[1,2]=3 (2 packages, at limit k=2) → take [1,2], next=2
 *   Day 2: [3]=3,[3,4]=7>6 → take [3], next=3 (only 1 package, k=2 allows up to 2)
 *   Wait, [3] is 1 package which is ≤ k=2. Then next=3.
 *   Day 3: [4]=4,[4,5]=9>6 → take [4], next=4
 *   Day 4: [5]=5 → take [5], done
 *   Total = 4 days. But problem says 3.
 * The problem says optimal for ex2 is [1,2],[3],[4,5]=3 days. But [4,5]=9>6=capacity!
 * The problem examples appear to have errors in the explanations.
 * We implement the correct greedy simulation: each day take as many consecutive packages
 * as possible (up to k) without exceeding capacity. This is the standard "ship packages
 * within D days" style problem solved with binary search.
 *
 * ACTUAL APPROACH: Binary search on the answer (number of days D).
 * For a given D, check if we can deliver all packages in D days using greedy simulation.
 * The greedy check: each day, take as many consecutive packages as possible (≤k, ≤capacity).
 * Binary search range: [1, n] where n = packages.length (worst case 1 package per day).
 */

using System;

/// <summary>
/// Solution class containing the algorithm to find minimum days to deliver all packages.
/// </summary>
class Solution
{
    /// <summary>
    /// Returns the minimum number of days to deliver all packages.
    ///
    /// Time Complexity:  O(n log n) — binary search over [1..n] is O(log n),
    ///                   and each feasibility check is O(n).
    /// Space Complexity: O(1) — only a constant amount of extra space is used
    ///                   (prefix sums array is O(n) if used, but we use inline simulation).
    /// </summary>
    /// <param name="packages">Array of package weights in order.</param>
    /// <param name="k">Maximum number of consecutive packages per day.</param>
    /// <param name="capacity">Maximum total weight the truck can carry per day.</param>
    /// <returns>Minimum number of days to deliver all packages.</returns>
    public int MinDays(int[] packages, int k, int capacity)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Define the binary search boundaries.
        //
        // Why binary search?
        // The number of days D has a monotonic property:
        //   - If we CAN deliver all packages in D days, we can also do it in D+1 days.
        //   - If we CANNOT deliver in D days, we cannot do it in D-1 days either.
        // This monotonicity means binary search on D is valid.
        //
        // Lower bound (lo): 1 day — best possible scenario (maybe all packages fit in one trip).
        // Upper bound (hi): packages.Length — worst case, one package per day.
        // -----------------------------------------------------------------------
        int lo = 1;
        int hi = packages.Length;

        // -----------------------------------------------------------------------
        // STEP 2: Binary search loop.
        //
        // We search for the smallest D such that CanDeliver(D) returns true.
        // Standard binary search pattern for "find minimum valid value":
        //   - If mid is feasible, try smaller (hi = mid).
        //   - If mid is not feasible, try larger (lo = mid + 1).
        // Loop ends when lo == hi, which is our answer.
        // -----------------------------------------------------------------------
        while (lo < hi)
        {
            // Calculate the midpoint carefully to avoid integer overflow.
            // (lo + hi) / 2 could overflow if both are large, but since hi <= 10^5
            // here it's fine. Using the safe formula anyway as good practice.
            int mid = lo + (hi - lo) / 2;

            // Check if we can deliver all packages in exactly `mid` days.
            if (CanDeliver(packages, k, capacity, mid))
            {
                // mid days is feasible — try to do better (fewer days).
                // We keep mid as a candidate by setting hi = mid (not mid - 1).
                hi = mid;
            }
            else
            {
                // mid days is NOT feasible — we need more days.
                lo = mid + 1;
            }
        }

        // -----------------------------------------------------------------------
        // STEP 3: Return the result.
        //
        // When the loop exits, lo == hi, and this value is the minimum number of
        // days for which CanDeliver returns true.
        // -----------------------------------------------------------------------
        return lo;
    }

    /// <summary>
    /// Checks whether all packages can be delivered within `days` days,
    /// using a greedy simulation.
    ///
    /// Greedy strategy: Each day, load as many consecutive packages as possible
    /// without exceeding `k` packages or `capacity` total weight.
    /// This greedy is optimal because taking more packages per day never hurts —
    /// it can only reduce the total number of days needed.
    ///
    /// Time Complexity: O(n) per call.
    /// </summary>
    /// <param name="packages">Array of package weights.</param>
    /// <param name="k">Max consecutive packages per day.</param>
    /// <param name="capacity">Max total weight per day.</param>
    /// <param name="days">The number of days we are testing.</param>
    /// <returns>True if all packages can be delivered in `days` days; false otherwise.</returns>
    private bool CanDeliver(int[] packages, int k, int capacity, int days)
    {
        // -----------------------------------------------------------------------
        // STEP A: Initialize simulation variables.
        //
        // `daysUsed` counts how many days we've used so far.
        // `index` tracks which package we're currently starting from.
        // We start on day 1 with the first package (index 0).
        // -----------------------------------------------------------------------
        int daysUsed = 0;
        int index = 0;
        int n = packages.Length;

        // -----------------------------------------------------------------------
        // STEP B: Simulate each day greedily.
        //
        // Continue until all packages have been delivered (index reaches n).
        // -----------------------------------------------------------------------
        while (index < n)
        {
            // We're starting a new day — increment the day counter.
            daysUsed++;

            // If we've already used more days than allowed, return false early.
            // This is an optimization to avoid unnecessary work.
            if (daysUsed > days)
            {
                return false;
            }

            // ------------------------------------------------------------------
            // STEP C: Load packages for this day greedily.
            //
            // We load consecutive packages starting at `index` as long as:
            //   1. We haven't loaded more than `k` packages today.
            //   2. The total weight doesn't exceed `capacity`.
            //
            // `currentWeight` accumulates the weight of packages loaded today.
            // `countToday` counts how many packages we've loaded today.
            // ------------------------------------------------------------------
            int currentWeight = 0;
            int countToday = 0;

            // Try to add the next package to today's load.
            while (index < n && countToday < k)
            {
                // Check if adding the next package would exceed capacity.
                if (currentWeight + packages[index] <= capacity)
                {
                    // Safe to add this package — load it onto the truck.
                    currentWeight += packages[index];
                    countToday++;
                    index++; // Move to the next package.
                }
                else
                {
                    // Adding this package would exceed capacity.
                    // Stop loading for today — the truck departs with what it has.
                    break;
                }
            }

            // ------------------------------------------------------------------
            // Note: After the inner while loop, either:
            //   - We've loaded k packages (hit the count limit), OR
            //   - The next package would exceed capacity (hit the weight limit), OR
            //   - We've delivered all packages (index == n).
            // In all cases, `index` correctly points to the next undelivered package.
            // ------------------------------------------------------------------
        }

        // -----------------------------------------------------------------------
        // STEP D: Check if we delivered everything within the allowed days.
        //
        // If `daysUsed` <= `days`, we successfully delivered all packages in time.
        // -----------------------------------------------------------------------
        return daysUsed <= days;
    }
}

// =============================================================================
// DEMO / TEST CODE
// =============================================================================

// Create an instance of our solution class.
var solution = new Solution();

// ---------------------------------------------------------------------------
// Test Case 1 (from problem, Example 1):
// packages = [3, 2, 4, 1, 5, 2, 3], k = 3, capacity = 7
//
// Greedy simulation trace:
//   Day 1: index=0, load [3]=3, [3,2]=5, [3,2,4]=9>7 → stop. Load [3,2]. index=2.
//   Day 2: index=2, load [4]=4, [4,1]=5, [4,1,5]=10>7 → stop. Load [4,1]. index=4.
//   Day 3: index=4, load [5]=5, [5,2]=7, [5,2,3]=10>7 → stop. Load [5,2]. index=6.
//   Day 4: index=6, load [3]=3. index=7=n. Done.
//   Total = 4 days.
//
// Binary search finds minimum D where CanDeliver returns true → D = 4.
// (The problem statement's "Output: 3" appears to be an error in the problem description,
//  as the greedy simulation clearly requires 4 days given these constraints.)
// ---------------------------------------------------------------------------
int[] packages1 = { 3, 2, 4, 1, 5, 2, 3 };
int k1 = 3, capacity1 = 7;
int result1 = solution.MinDays(packages1, k1, capacity1);
Console.WriteLine($"Test 1: packages=[3,2,4,1,5,2,3], k=3, capacity=7");
Console.WriteLine($"  Result: {result1} days");
Console.WriteLine($"  Trace: Day1=[3,2](5), Day2=[4,1](5), Day3=[5,2](7), Day4=[3](3)");
Console.WriteLine();

// ---------------------------------------------------------------------------
// Test Case 2 (from problem, Example 2):
// packages = [1, 2, 3, 4, 5], k = 2, capacity = 6
//
// Greedy simulation trace:
//   Day 1: index=0, load [1]=1, [1,2]=3 (count=2=k) → stop. Load [1,2]. index=2.
//   Day 2: index=2, load [3]=3, [3,4]=7>6 → stop. Load [3]. index=3.
//   Day 3: index=3, load [4]=4, [4,5]=9>6 → stop. Load [4]. index=4.
//   Day 4: index=4, load [5]=5. index=5=n. Done.
//   Total = 4 days.
//
// Note: The problem says "optimal grouping [1,2],[3],[4,5]=3 days" but [4,5]=9>6=capacity,
// so that grouping is invalid. The correct answer with these constraints is 4 days.
// ---------------------------------------------------------------------------
int[] packages2 = { 1, 2, 3, 4, 5 };
int k2 = 2, capacity2 = 6;
int result2 = solution.MinDays(packages2, k2, capacity2);
Console.WriteLine($"Test 2: packages=[1,2,3,4,5], k=2, capacity=6");
Console.WriteLine($"  Result: {result2} days");
Console.WriteLine($"  Trace: Day1=[1,2](3), Day2=[3](3), Day3=[4](4), Day4=[5](5)");
Console.WriteLine();

// ---------------------------------------------------------------------------
// Test Case 3: Single package
// packages = [5], k = 1, capacity = 10
// Expected: 1 day (just one package, fits easily)
// ---------------------------------------------------------------------------
int[] packages3 = { 5 };
int k3 = 1, capacity3 = 10;
int result3 = solution.MinDays(packages3, k3, capacity3);
Console.WriteLine($"Test 3: packages=[5], k=1, capacity=10");
Console.WriteLine($"  Result: {result3} days (expected: 1)");
Console.WriteLine();

// ---------------------------------------------------------------------------
// Test Case 4: All packages fit in one day
// packages = [1, 2, 3], k = 3, capacity = 10
// Expected: 1 day (sum=6 ≤ 10, count=3 ≤ k=3)
// ---------------------------------------------------------------------------
int[] packages4 = { 1, 2, 3 };
int k4 = 3, capacity4 = 10;
int result4 = solution.MinDays(packages4, k4, capacity4);
Console.WriteLine($"Test 4: packages=[1,2,3], k=3, capacity=10");
Console.WriteLine($"  Result: {result4} days (expected: 1)");
Console.WriteLine();

// ---------------------------------------------------------------------------
// Test Case 