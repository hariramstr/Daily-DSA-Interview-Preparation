/*
 * Title: Minimum Capacity Courier Fleet for Weighted Deliveries
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A logistics company needs to deliver `n` packages arranged in a fixed route order.
 * Each package has a weight given by `weights[i]`. You have a fleet of `k` couriers,
 * and each courier must take a contiguous segment of packages from the route
 * (i.e., the packages cannot be reordered). All `k` couriers operate simultaneously,
 * and each courier's load is the sum of weights of the packages assigned to them.
 *
 * However, there is an additional constraint: each courier can only carry packages
 * if the number of packages assigned to them does not exceed a given limit `maxPkgs`.
 * In other words, every contiguous segment assigned to a courier must contain at most
 * `maxPkgs` packages.
 *
 * Your goal is to minimize the maximum load carried by any single courier, while ensuring:
 * 1. All packages are delivered.
 * 2. Each courier receives a contiguous, non-empty segment.
 * 3. No courier carries more than `maxPkgs` packages.
 *
 * Return the minimum possible value of the maximum load among all couriers.
 * If it is impossible to partition the packages into exactly `k` non-empty contiguous
 * segments each with at most `maxPkgs` packages, return `-1`.
 *
 * Constraints:
 * - 1 <= n <= 10^5
 * - 1 <= k <= n
 * - 1 <= maxPkgs <= n
 * - 1 <= weights[i] <= 10^4
 */

using System;
using System.Linq;

// ─────────────────────────────────────────────────────────────────────────────
// SOLUTION CLASS
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Method: MinimumMaxLoad
     *
     * Time Complexity:  O(n * log(S))  where S = sum of all weights
     *                   - Binary search runs O(log S) iterations
     *                   - Each feasibility check is O(n)
     *
     * Space Complexity: O(1)  (only a handful of scalar variables; no extra arrays)
     *
     * HIGH-LEVEL IDEA — Binary Search on the Answer
     * ─────────────────────────────────────────────
     * Instead of trying every possible partition directly (which would be
     * exponential), we ask a simpler YES/NO question:
     *
     *   "Can we split the packages into exactly k contiguous, non-empty segments
     *    such that every segment has weight ≤ capacity AND ≤ maxPkgs packages?"
     *
     * If we can answer that question quickly, we can binary-search over all
     * possible values of `capacity` (from the minimum sensible value to the
     * maximum sensible value) and find the smallest one that still yields "YES".
     *
     * Binary search bounds:
     *   lo = max(weights)          — a courier must carry at least the heaviest
     *                                single package (since segments are non-empty
     *                                and every package must be assigned).
     *   hi = sum(weights)          — one courier takes everything (upper bound).
     *
     * Feasibility check (IsFeasible):
     *   Greedily assign packages to the current courier until adding the next
     *   package would either exceed `capacity` or exceed `maxPkgs`.  When that
     *   happens, start a new courier.  Count how many couriers we need.
     *   If the count is ≤ k, the capacity is feasible.
     *   (Fewer couriers than k is also fine — we can always split a segment
     *    further to use exactly k couriers, as long as n >= k.)
     */
    public int MinimumMaxLoad(int[] weights, int k, int maxPkgs)
    {
        int n = weights.Length;

        // ── STEP 1: Quick impossibility checks ──────────────────────────────
        //
        // Why check early?  Saves us from running binary search on an
        // inherently impossible input.
        //
        // Case A: We need at least k packages (one per courier), but n < k.
        //         (The problem guarantees k <= n, but let's be safe.)
        if (n < k)
            return -1;

        // Case B: Each courier can carry at most maxPkgs packages.
        //         With k couriers the maximum coverage is k * maxPkgs packages.
        //         If n > k * maxPkgs, we can never cover all packages.
        //
        //         Example 2: n=5, k=3, maxPkgs=1  →  3*1=3 < 5  →  impossible.
        if ((long)k * maxPkgs < n)   // use long to avoid int overflow
            return -1;

        // ── STEP 2: Establish binary-search bounds ───────────────────────────
        //
        // lo: the minimum possible answer.
        //     Every courier must receive at least one package, so the heaviest
        //     single package sets a hard lower bound on the maximum load.
        //
        // hi: the maximum possible answer.
        //     If one courier takes all packages (k=1 scenario), the load equals
        //     the total weight.  This is always feasible when k=1 and n<=maxPkgs.
        //     We use it as a safe upper bound regardless.
        int lo = weights.Max();          // heaviest single package
        long hi = weights.Sum();         // total weight of all packages (use long to be safe)

        // Edge case: if lo itself is already the answer (e.g., k == n, each
        // courier gets exactly one package), the binary search will find it.

        // ── STEP 3: Binary search ────────────────────────────────────────────
        //
        // We search for the smallest `capacity` such that IsFeasible returns true.
        // Classic "find first true" binary search pattern:
        //   - If mid is feasible, try smaller  (hi = mid)
        //   - If mid is not feasible, try larger (lo = mid + 1)
        // Loop ends when lo == hi, which is our answer.
        while (lo < hi)
        {
            // mid is the candidate maximum load we are testing
            long mid = lo + (hi - lo) / 2;  // avoids overflow compared to (lo+hi)/2

            if (IsFeasible(weights, k, maxPkgs, mid))
            {
                // mid works → maybe we can do even better (smaller capacity)
                hi = mid;
            }
            else
            {
                // mid doesn't work → we need at least mid+1
                lo = (int)(mid + 1);
            }
        }

        // ── STEP 4: Return the answer ────────────────────────────────────────
        //
        // lo == hi at this point, and it is the smallest feasible capacity.
        return (int)lo;
    }

    /*
     * Helper Method: IsFeasible
     *
     * Answers the question: "Can we partition `weights` into exactly k (or fewer)
     * contiguous, non-empty segments where each segment has:
     *   - total weight  ≤ capacity, AND
     *   - package count ≤ maxPkgs?"
     *
     * Algorithm: Greedy left-to-right scan.
     *
     * Why greedy works here?
     *   We want to use as FEW couriers as possible (to check if k suffice).
     *   The greedy strategy of "extend the current segment as far as possible
     *   before starting a new one" minimises the number of couriers used.
     *   If even this minimal count exceeds k, no valid partition exists for
     *   this capacity.
     *
     * Time: O(n)
     */
    private bool IsFeasible(int[] weights, int k, int maxPkgs, long capacity)
    {
        // couriersNeeded: how many couriers the greedy approach requires.
        // We always need at least 1 courier for the first segment.
        int couriersNeeded = 1;

        // currentLoad: total weight assigned to the current courier so far.
        long currentLoad = 0;

        // currentCount: number of packages assigned to the current courier so far.
        int currentCount = 0;

        // Iterate over every package in route order.
        for (int i = 0; i < weights.Length; i++)
        {
            int w = weights[i];

            // Check whether adding this package to the current courier is allowed.
            // Two constraints must BOTH be satisfied:
            //   1. currentLoad + w <= capacity   (weight limit)
            //   2. currentCount + 1 <= maxPkgs   (package count limit)
            bool weightOk = currentLoad + w <= capacity;
            bool countOk  = currentCount + 1 <= maxPkgs;

            if (weightOk && countOk)
            {
                // Safe to add this package to the current courier's segment.
                currentLoad  += w;
                currentCount += 1;
            }
            else
            {
                // Cannot add this package to the current courier.
                // Start a new courier for this package.
                couriersNeeded++;
                currentLoad  = w;   // new courier starts with this package
                currentCount = 1;

                // If a single package already exceeds capacity, this capacity
                // value is impossible (binary search will move lo up).
                // We detect this implicitly: couriersNeeded will grow beyond k,
                // or we can check directly:
                if (w > capacity)
                    return false;
            }
        }

        // The partition is feasible if we needed at most k couriers.
        // (Using fewer than k couriers is fine — we could always split a
        //  segment to use exactly k, since n >= k is guaranteed.)
        return couriersNeeded <= k;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DEMO / TEST CODE  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

Console.WriteLine("=== Minimum Capacity Courier Fleet for Weighted Deliveries ===");
Console.WriteLine();

// ── Example 1 ────────────────────────────────────────────────────────────────
// weights = [3, 2, 8, 5, 1, 7, 4], k = 3, maxPkgs = 4
// Expected output: 13
//
// Trace:
//   lo = max(weights) = 8
//   hi = sum(weights) = 30
//
//   Binary search iterations:
//     mid=19 → IsFeasible? Greedy: [3,2,8,5]=18≤19,4pkgs≤4 | [1,7,4]=12≤19,3pkgs≤4
//              couriersNeeded=2 ≤ 3 → feasible → hi=19
//     mid=13 → IsFeasible? Greedy:
//              Courier1: 3(ok), 3+2=5(ok), 5+8=13(ok,3pkgs), 13+5=18>13 → new courier
//              Courier2: 5(ok), 5+1=6(ok), 6+7=13(ok,3pkgs), 13+4=17>13 → new courier
//              Courier3: 4(ok,1pkg)
//              couriersNeeded=3 ≤ 3 → feasible → hi=13
//     mid=10 → IsFeasible? Greedy:
//              Courier1: 3,3+2=5,5+8=13>10 → new courier (after 3+2=5, add 8: 5+8=13>10)
//              Actually: 3(ok),3+2=5(ok),5+8=13>10 → new courier
//              Courier2: 8(ok),8+5=13>10 → new courier
//              Courier3: 5(ok),5+1=6(ok),6+7=13>10 → new courier
//              Courier4: 7(ok),7+4=11>10 → new courier
//              Courier5: 4(ok)
//              couriersNeeded=5 > 3 → not feasible → lo=11
//     mid=12 → IsFeasible? Greedy:
//              Courier1: 3,5,13>12 → new courier after [3,2]
//              Wait: 3(ok),3+2=5(ok),5+8=13>12 → new courier
//              Courier2: 8(ok),8+5=13>12 → new courier
//              Courier3: 5,5+1=6,6+7=13>12 → new courier
//              Courier4: 7,7+4=11≤12 → ok
//              couriersNeeded=4 > 3 → not feasible → lo=13
//     lo==hi==13 → answer = 13  ✓

int[] weights1 = { 3, 2, 8, 5, 1, 7, 4 };
int k1 = 3, maxPkgs1 = 4;
int result1 = solution.MinimumMaxLoad(weights1, k1, maxPkgs1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  weights   = [{string.Join(", ", weights1)}]");
Console.WriteLine($"  k         = {k1}");
Console.WriteLine($"  maxPkgs   = {maxPkgs1}");
Console.WriteLine($"  Result    = {result1}   (Expected: 13)");
Console.WriteLine($"  Correct?  {result1 == 13}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// weights = [10, 10, 10, 10, 10], k = 3, maxPkgs = 1
// Expected output: -1
//
// Trace:
//   k * maxPkgs = 3 * 1 = 3 < n = 5  →  return -1  ✓

int[] weights2 = { 10, 10, 10, 10, 10 };
int k2 = 3, maxPkgs2 = 1;
int result2 = solution.MinimumMaxLoad(weights2, k2, maxPkgs2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  weights   = [{string.Join(", ", weights2)}]");
Console.WriteLine($"  k         = {k2}");
Console.WriteLine($"  maxPkgs   = {maxPkgs2}");
Console.WriteLine($"  Result    = {result2}   (Expected: -1)");
Console.WriteLine($"  Correct?  {result2 == -1}");
Console.WriteLine();

// ── Additional Test Cases ─────────────────────────────────────────────────────

// Test 3: k == n (each courier gets exactly one package)
// weights = [5, 3, 7, 2], k = 4, maxPkgs = 1
// Each courier gets 1 package. Max load = max(weights) = 7.
int[] weights3 = { 5, 3, 7, 2 };
int k3 = 4, maxPkgs3 = 1;
int result3 = solution.MinimumMaxLoad(weights3, k3, maxPkgs3);
Console.WriteLine($"Test 3 (k == n, each courier gets 1 package):");
Console.WriteLine($"  weights   = [{string.Join(", ", weights3)}]");
Console.WriteLine($"  k         = {k3}");
Console.WriteLine($"  maxPkgs   = {maxPkgs3}");
Console.WriteLine($"  Result    = {result3}   (Expected: 7)");
Console.WriteLine($"  Correct?  {