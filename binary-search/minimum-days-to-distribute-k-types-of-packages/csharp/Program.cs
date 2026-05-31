/*
 * Title: Minimum Days to Distribute K Types of Packages
 * 
 * Problem Description:
 * A warehouse has n workers and m package types. Each package type i has a weight weights[i]
 * and a count counts[i] representing how many packages of that type exist. Each worker can
 * carry packages on a given day, but the total weight they carry must not exceed capacity C.
 * On each day, every worker makes exactly one trip, and workers can only carry packages of
 * the same type in a single trip.
 *
 * You must ensure that at least k distinct package types are fully distributed in the minimum
 * number of days. Binary search on the number of days d. For a given d days, determine the
 * maximum number of distinct package types that can be fully distributed using n workers each
 * carrying up to C weight per day.
 *
 * Constraints:
 * - 1 <= n <= 10^4
 * - 1 <= m <= 10^5
 * - 1 <= k <= m
 * - 1 <= weights[i] <= C <= 10^6
 * - 1 <= counts[i] <= 10^9
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Method: MinDays
     *
     * Time Complexity:  O(m * log(maxDays))
     *   - Binary search runs O(log(maxDays)) iterations.
     *   - Each iteration calls CanDistribute which is O(m) (sort is done once outside).
     *   - maxDays is at most ceil(max(counts[i]) / (n * floor(C / weights[i])))
     *     which is bounded by counts[i] (up to 10^9), so log factor ≈ 30.
     *
     * Space Complexity: O(m)
     *   - We store the precomputed "days needed per type" array of length m.
     */
    public int MinDays(int n, int C, int[] weights, int[] counts, int k)
    {
        // ── Step 1: Understand the core formula ──────────────────────────────
        // For package type i:
        //   - Each worker can carry floor(C / weights[i]) packages per trip.
        //   - With n workers working together on type i for d days, the total
        //     packages delivered = n * floor(C / weights[i]) * d.
        //   - We need this to be >= counts[i].
        //   - So the minimum days to fully distribute type i is:
        //       daysNeeded[i] = ceil(counts[i] / (n * floor(C / weights[i])))
        //
        // Why this formula?
        //   Each day, ALL n workers can be assigned to the same package type,
        //   each carrying floor(C / weights[i]) packages. So per day throughput
        //   for type i = n * floor(C / weights[i]).
        //   We want the minimum d such that d * throughput >= counts[i].
        //   That is d = ceil(counts[i] / throughput).

        int m = weights.Length; // number of package types

        // ── Step 2: Precompute days needed for each package type ─────────────
        // We compute how many days it takes to fully distribute each type
        // if ALL n workers focus on that type every day.
        // This is the minimum possible days for that type (best-case assignment).
        //
        // Note: if floor(C / weights[i]) == 0, that means a single worker cannot
        // even carry one package of type i. The problem guarantees weights[i] <= C,
        // so floor(C / weights[i]) >= 1 always.

        long[] daysNeeded = new long[m];

        for (int i = 0; i < m; i++)
        {
            // How many packages can one worker carry per trip for type i?
            long packagesPerWorkerPerDay = C / weights[i];

            // Total throughput per day if all n workers handle type i
            long throughputPerDay = (long)n * packagesPerWorkerPerDay;

            // Ceiling division: ceil(counts[i] / throughputPerDay)
            // Formula: ceil(a / b) = (a + b - 1) / b  (integer arithmetic)
            daysNeeded[i] = (counts[i] + throughputPerDay - 1) / throughputPerDay;
        }

        // ── Step 3: Sort daysNeeded in ascending order ───────────────────────
        // Why sort?
        //   We want to pick the k package types that can be distributed in the
        //   fewest days. If we sort by daysNeeded, the first k entries are the
        //   k "easiest" types to distribute. The answer is daysNeeded[k-1] after
        //   sorting (the k-th smallest value), because:
        //     - In daysNeeded[k-1] days, we can distribute at least k types
        //       (the k types with the smallest daysNeeded values).
        //     - We cannot do it in fewer days (the k-th easiest type requires
        //       exactly daysNeeded[k-1] days).
        //
        // This insight means we don't even need binary search — sorting gives
        // us the answer directly! The minimum number of days to distribute
        // at least k types is simply the k-th smallest value in daysNeeded[].

        Array.Sort(daysNeeded);

        // ── Step 4: Return the k-th smallest daysNeeded value ────────────────
        // After sorting, daysNeeded[0] <= daysNeeded[1] <= ... <= daysNeeded[m-1].
        // To distribute AT LEAST k types, we pick the k easiest types.
        // The bottleneck is the k-th easiest type, which needs daysNeeded[k-1] days.
        //
        // Why is this correct?
        //   - In daysNeeded[k-1] days, types 0..k-1 (sorted) are all fully distributed.
        //   - In fewer days (say daysNeeded[k-1] - 1), the k-th type is NOT done,
        //     and only k-1 types (at most) are fully distributed.
        //
        // We cast to int since the problem expects an int return; the value fits
        // because daysNeeded[i] <= counts[i] <= 10^9 which fits in int (barely),
        // but to be safe we return as long cast to int. Actually counts[i] up to
        // 10^9 and n*packagesPerWorkerPerDay >= 1, so daysNeeded <= 10^9 which
        // fits in int (max int ~2.1*10^9). We'll return long to be safe.

        return (int)daysNeeded[k - 1];
    }

    /*
     * Alternative explicit Binary Search approach (kept for educational purposes).
     * This mirrors the problem statement's hint to "binary search on d".
     *
     * Time Complexity:  O(m * log(maxDays))  — same as above
     * Space Complexity: O(m)
     */
    public int MinDaysBinarySearch(int n, int C, int[] weights, int[] counts, int k)
    {
        int m = weights.Length;

        // ── Step A: Precompute per-type throughput ───────────────────────────
        // throughput[i] = packages delivered per day for type i with all n workers
        long[] throughput = new long[m];
        for (int i = 0; i < m; i++)
        {
            throughput[i] = (long)n * (C / weights[i]);
        }

        // ── Step B: Define binary search bounds ──────────────────────────────
        // Lower bound: 1 day (optimistic minimum)
        // Upper bound: the maximum days any single type could need
        //   = max over all i of ceil(counts[i] / throughput[i])
        long lo = 1;
        long hi = 0;
        for (int i = 0; i < m; i++)
        {
            long needed = (counts[i] + throughput[i] - 1) / throughput[i];
            if (needed > hi) hi = needed;
        }

        // ── Step C: Binary search on number of days d ────────────────────────
        // Invariant:
        //   - lo is a candidate answer (might be feasible or not yet checked)
        //   - hi is always feasible (we can distribute all m types in hi days,
        //     so certainly k types)
        // We search for the smallest d such that >= k types can be distributed.

        while (lo < hi)
        {
            long mid = lo + (hi - lo) / 2;

            // Count how many types can be fully distributed in 'mid' days
            int typesDistributed = 0;
            for (int i = 0; i < m; i++)
            {
                // Type i is fully distributed in mid days if:
                // throughput[i] * mid >= counts[i]
                if (throughput[i] * mid >= counts[i])
                {
                    typesDistributed++;
                }
            }

            if (typesDistributed >= k)
            {
                // mid days is feasible — try fewer days
                hi = mid;
            }
            else
            {
                // mid days is not enough — need more days
                lo = mid + 1;
            }
        }

        // lo == hi is the minimum number of days
        return (int)lo;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

Console.WriteLine("=== Minimum Days to Distribute K Types of Packages ===");
Console.WriteLine();

// ── Example 1 ────────────────────────────────────────────────────────────────
// n=3, C=10, weights=[4,6,3], counts=[12,6,9], k=2
// Expected Output: 2
//
// Trace:
//   Type 0: packagesPerWorkerPerDay = floor(10/4) = 2
//           throughput = 3 * 2 = 6 packages/day
//           daysNeeded = ceil(12/6) = 2
//   Type 1: packagesPerWorkerPerDay = floor(10/6) = 1
//           throughput = 3 * 1 = 3 packages/day
//           daysNeeded = ceil(6/3) = 2
//   Type 2: packagesPerWorkerPerDay = floor(10/3) = 3
//           throughput = 3 * 3 = 9 packages/day
//           daysNeeded = ceil(9/9) = 1
//
//   daysNeeded (sorted) = [1, 2, 2]
//   k=2 → answer = daysNeeded[1] = 2  ✓

int n1 = 3, C1 = 10, k1 = 2;
int[] weights1 = { 4, 6, 3 };
int[] counts1  = { 12, 6, 9 };

int result1 = solution.MinDays(n1, C1, weights1, counts1, k1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  n={n1}, C={C1}, weights=[{string.Join(",", weights1)}], counts=[{string.Join(",", counts1)}], k={k1}");
Console.WriteLine($"  Result (sorting approach):        {result1}");
Console.WriteLine($"  Result (binary search approach):  {solution.MinDaysBinarySearch(n1, C1, weights1, counts1, k1)}");
Console.WriteLine($"  Expected: 2");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// n=2, C=5, weights=[5,3,4], counts=[10,15,8], k=3
// Expected Output: 5
//
// Trace:
//   Type 0: packagesPerWorkerPerDay = floor(5/5) = 1
//           throughput = 2 * 1 = 2 packages/day
//           daysNeeded = ceil(10/2) = 5
//   Type 1: packagesPerWorkerPerDay = floor(5/3) = 1
//           throughput = 2 * 1 = 2 packages/day
//           daysNeeded = ceil(15/2) = 8
//   Type 2: packagesPerWorkerPerDay = floor(5/4) = 1
//           throughput = 2 * 1 = 2 packages/day
//           daysNeeded = ceil(8/2) = 4
//
//   daysNeeded (sorted) = [4, 5, 8]
//   k=3 → answer = daysNeeded[2] = 8?
//
//   Wait — the problem says expected is 5. Let's re-read the explanation:
//   "To get 3 types, pick types 0,2 and one more; minimum days needed is 5."
//   That means we pick the 3 types with smallest daysNeeded: [4, 5, 8].
//   The bottleneck is the 3rd = 8? But expected is 5...
//
//   Re-reading the problem explanation more carefully:
//   "Type 0: ceil(10/(2*1))=5 days. Type 1: ceil(15/(2*1))=8 days. Type 2: ceil(8/(2*1))=4 days."
//   So daysNeeded = [5, 8, 4], sorted = [4, 5, 8].
//   k=3 means we need ALL 3 types → answer = daysNeeded[2] = 8.
//   But the problem says 5...
//
//   Hmm, let me re-read: "To get 3 types, pick types 0,2 and one more; minimum days needed is 5."
//   This is contradictory if k=3 requires all 3 types. The explanation says pick types 0 and 2
//   (days 5 and 4) "and one more" — but there are only 3 types total, so "one more" = type 1 (8 days).
//   That would give 8, not 5.
//
//   Perhaps the problem statement's Example 2 explanation has an error, OR k=2 not k=3.
//   Let's check: if k=2, sorted=[4,5,8], answer=daysNeeded[1]=5. That matches!
//   The problem says k=3 but the answer 5 corresponds to k=2.
//   We'll trust the mathematical derivation: our algorithm gives the correct answer
//   for the formula. We'll show both k=2 and k=3 results.

int n2 = 2, C2 = 5, k2 = 3;
int[] weights2 = { 5, 3, 4 };
int[] counts2  = { 10, 15, 8 };

// For k=2 (which matches the stated answer of 5):
int result2_k2 = solution.MinDays(n2, C2, weights2, counts2, 2);
int result2_k3 = solution.MinDays(n2, C2, weights2, counts2, k2);

Console.WriteLine($"Example 2:");
Console.WriteLine($"  n={n2}, C={C2}, weights=[{string.Join(",", weights2)}], counts=[{string.Join(",", counts2)}]");
Console.WriteLine($"  daysNeeded per type: [5, 8, 4] → sorted: [4, 5, 8]");
Console.WriteLine($"  Result for k=2 (sorting): {result2_k2}  (matches stated answer of 