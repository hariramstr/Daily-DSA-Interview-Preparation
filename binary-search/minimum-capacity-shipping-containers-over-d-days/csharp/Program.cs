/*
 * Title: Minimum Capacity Shipping Containers Over D Days
 *
 * Problem Description:
 * A shipping company needs to transport a sequence of cargo containers across a route.
 * Each container has a weight, and containers must be loaded onto ships in the order
 * they appear (you cannot reorder them). Each day, a single ship makes one trip and
 * can carry at most `capacity` total weight. You must ship all containers within
 * exactly D days.
 *
 * Given an integer array `weights` where `weights[i]` is the weight of the i-th
 * container, and an integer `D` representing the number of days available, return
 * the minimum ship capacity needed to ship all containers within D days.
 *
 * Note: Each container must be placed on a ship, and the order of containers cannot
 * be changed. A ship cannot carry more than its capacity on any single day.
 *
 * Constraints:
 *   - 1 <= D <= weights.length <= 50000
 *   - 1 <= weights[i] <= 500
 *
 * Example 1:
 *   Input:  weights = [3, 2, 2, 4, 1, 4], D = 3
 *   Output: 6
 *   Explanation: Day 1 ships [3,2], Day 2 ships [2,4], Day 3 ships [1,4].
 *
 * Example 2:
 *   Input:  weights = [1, 2, 3, 4, 5], D = 2
 *   Output: 9
 *   Explanation: Day 1 ships [1,2,3], Day 2 ships [4,5].
 */

// ─────────────────────────────────────────────────────────────────────────────
// KEY INSIGHT (read this before diving into code):
//
//  The answer (minimum capacity) lies somewhere in the range:
//    LOW  = max(weights)   → the ship must be able to carry the heaviest single
//                            container, otherwise it can never be shipped.
//    HIGH = sum(weights)   → if the ship can carry everything at once, we only
//                            need 1 day (the absolute upper bound).
//
//  Within that range the feasibility function is MONOTONE:
//    • If capacity C is enough to ship in ≤ D days, then C+1 is also enough.
//    • If capacity C is NOT enough, then C-1 is also not enough.
//
//  This monotone property lets us use BINARY SEARCH on the answer space instead
//  of trying every possible capacity one by one.
// ─────────────────────────────────────────────────────────────────────────────

public class Solution
{
    // -------------------------------------------------------------------------
    // Time Complexity:  O(N * log(S))
    //   where N = number of containers, S = sum(weights) - max(weights)
    //   The binary search runs O(log S) iterations; each iteration calls
    //   CanShipInDDays which is O(N).
    //
    // Space Complexity: O(1)  — only a handful of integer variables are used;
    //   no extra data structures proportional to input size.
    // -------------------------------------------------------------------------
    public int ShipWithinDays(int[] weights, int D)
    {
        // ── STEP 1: Establish the search boundaries ───────────────────────────
        //
        // 'low' is the minimum possible capacity we would ever consider.
        //   Why max(weights)?  Because every container must fit on the ship by
        //   itself (in the worst case it occupies an entire day alone).
        //   If the capacity were less than the heaviest container, that container
        //   could never be loaded — so this is a hard lower bound.
        //
        // 'high' is the maximum possible capacity we would ever need.
        //   Why sum(weights)?  With this capacity the ship can carry ALL containers
        //   in a single trip (1 day), which is always feasible.  We never need
        //   more than this, so it is a safe upper bound.

        int low = 0;   // will become max(weights)
        int high = 0;  // will become sum(weights)

        foreach (int w in weights)
        {
            // Track the maximum single weight for the lower bound
            if (w > low) low = w;

            // Accumulate total weight for the upper bound
            high += w;
        }

        // At this point:
        //   low  = max element in weights
        //   high = sum of all elements in weights
        // Our answer is guaranteed to be somewhere in [low, high].

        // ── STEP 2: Binary search on the capacity value ───────────────────────
        //
        // Classic "find the leftmost value that satisfies a condition" pattern:
        //   • If the mid-point capacity CAN ship everything in ≤ D days,
        //     record it as a candidate answer and try to go lower (right = mid).
        //   • If it CANNOT, we need more capacity (left = mid + 1).
        //
        // The loop ends when left == right, which is our minimum valid capacity.

        while (low < high)
        {
            // Pick the midpoint capacity to test.
            // Using low + (high - low) / 2 instead of (low + high) / 2 avoids
            // integer overflow (safe practice even though values are small here).
            int mid = low + (high - low) / 2;

            // ── STEP 3: Check feasibility for this candidate capacity ─────────
            //
            // Ask: "Can we ship all containers within D days if the ship's
            //       capacity is exactly 'mid'?"
            if (CanShipInDDays(weights, D, mid))
            {
                // 'mid' works!  But maybe something smaller also works.
                // Shrink the search space from the right side.
                // We keep 'mid' as a possible answer by setting high = mid
                // (not mid - 1), so we don't accidentally skip the optimum.
                high = mid;
            }
            else
            {
                // 'mid' is too small — we need strictly more capacity.
                // Eliminate 'mid' and everything below it.
                low = mid + 1;
            }
        }

        // ── STEP 4: Return the result ─────────────────────────────────────────
        //
        // When the loop exits, low == high and both point to the smallest
        // capacity that allows shipping within D days.
        return low;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper method: CanShipInDDays
    //
    // Simulates the greedy loading strategy:
    //   Load containers onto today's ship one by one (in order).
    //   When the next container would exceed the capacity, start a new day.
    //
    // Returns true  if all containers can be shipped within D days.
    // Returns false if more than D days would be required.
    //
    // Time Complexity: O(N)  — single pass through the weights array.
    // ─────────────────────────────────────────────────────────────────────────
    private bool CanShipInDDays(int[] weights, int D, int capacity)
    {
        // 'daysNeeded' counts how many ship trips (days) are required.
        // We always need at least 1 day to ship anything.
        int daysNeeded = 1;

        // 'currentLoad' tracks the total weight loaded onto today's ship so far.
        int currentLoad = 0;

        foreach (int w in weights)
        {
            // Check if adding this container would exceed today's capacity.
            if (currentLoad + w > capacity)
            {
                // It would overflow — start a new day (new ship trip).
                daysNeeded++;

                // Reset the load for the new day; this container goes first.
                currentLoad = 0;

                // Early exit optimisation:
                // If we already need more days than allowed, no need to continue.
                if (daysNeeded > D) return false;
            }

            // Load the container onto today's ship.
            currentLoad += w;
        }

        // All containers have been assigned to a day within the D-day limit.
        return true;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DEMO / TEST CODE  (top-level statements — no Main method needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// weights = [3, 2, 2, 4, 1, 4], D = 3
// Expected output: 6
//
// Trace of binary search:
//   low=4 (max), high=16 (sum)
//   mid=10 → CanShip? Day1:[3,2,2,4]→11>10 so Day1:[3,2,2]=7, Day2:[4,1,4]=9>10
//             Actually let's be precise:
//             currentLoad=0 → +3=3 → +2=5 → +2=7 → +4=11>10 → new day(2), load=4
//             → +1=5 → +4=9 → end. daysNeeded=2 ≤ 3 → true → high=10
//   mid=7  → Day1:[3,2,2]=7, Day2:[4,1]=5 (4+1=5, +4=9>7 new day), Day3:[4]=4
//             daysNeeded=3 ≤ 3 → true → high=7
//   mid=5  → Day1:[3,2]=5, Day2:[2]=2(+4=6>5 new day), Day3:[4]=4(+1=5,+4=9>5)
//             Day4 needed → daysNeeded=4 > 3 → false → low=6
//   low==high==6 → return 6  ✓

int[] weights1 = { 3, 2, 2, 4, 1, 4 };
int D1 = 3;
int result1 = solution.ShipWithinDays(weights1, D1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  weights = [{string.Join(", ", weights1)}], D = {D1}");
Console.WriteLine($"  Minimum capacity = {result1}");
Console.WriteLine($"  Expected         = 6");
Console.WriteLine($"  Correct?         = {result1 == 6}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// weights = [1, 2, 3, 4, 5], D = 2
// Expected output: 9
//
// Trace:
//   low=5 (max), high=15 (sum)
//   mid=10 → Day1:[1,2,3,4]=10, Day2:[5]=5 → daysNeeded=2 ≤ 2 → true → high=10
//   mid=7  → Day1:[1,2,3]=6(+4=10>7 new day), Day2:[4]=4(+5=9>7 new day), Day3:[5]
//             daysNeeded=3 > 2 → false → low=8
//   mid=9  → Day1:[1,2,3]=6(+4=10>9 new day), Day2:[4,5]=9 → daysNeeded=2 ≤ 2 → true → high=9
//   low==high==9 → return 9  ✓

int[] weights2 = { 1, 2, 3, 4, 5 };
int D2 = 2;
int result2 = solution.ShipWithinDays(weights2, D2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  weights = [{string.Join(", ", weights2)}], D = {D2}");
Console.WriteLine($"  Minimum capacity = {result2}");
Console.WriteLine($"  Expected         = 9");
Console.WriteLine($"  Correct?         = {result2 == 9}");
Console.WriteLine();

// ── Additional edge-case tests ────────────────────────────────────────────────

// Edge case: D equals number of containers → each container gets its own day
// Minimum capacity = max(weights)
int[] weights3 = { 1, 2, 3, 4, 5 };
int D3 = 5;
int result3 = solution.ShipWithinDays(weights3, D3);
Console.WriteLine($"Edge Case (D == N, one container per day):");
Console.WriteLine($"  weights = [{string.Join(", ", weights3)}], D = {D3}");
Console.WriteLine($"  Minimum capacity = {result3}");
Console.WriteLine($"  Expected         = 5  (max weight)");
Console.WriteLine($"  Correct?         = {result3 == 5}");
Console.WriteLine();

// Edge case: D == 1 → ship must carry everything in one trip
// Minimum capacity = sum(weights)
int[] weights4 = { 3, 3, 3 };
int D4 = 1;
int result4 = solution.ShipWithinDays(weights4, D4);
Console.WriteLine($"Edge Case (D == 1, all in one trip):");
Console.WriteLine($"  weights = [{string.Join(", ", weights4)}], D = {D4}");
Console.WriteLine($"  Minimum capacity = {result4}");
Console.WriteLine($"  Expected         = 9  (sum of weights)");
Console.WriteLine($"  Correct?         = {result4 == 9}");
Console.WriteLine();

// Edge case: single container
int[] weights5 = { 7 };
int D5 = 1;
int result5 = solution.ShipWithinDays(weights5, D5);
Console.WriteLine($"Edge Case (single container):");
Console.WriteLine($"  weights = [{string.Join(", ", weights5)}], D = {D5}");
Console.WriteLine($"  Minimum capacity = {result5}");
Console.WriteLine($"  Expected         = 7");
Console.WriteLine($"  Correct?         = {result5 == 7}");