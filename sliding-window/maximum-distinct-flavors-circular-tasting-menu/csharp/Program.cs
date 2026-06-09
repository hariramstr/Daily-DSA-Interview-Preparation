/*
 * Maximum Distinct Flavors in a Circular Tasting Menu
 * =====================================================
 * A restaurant offers a circular tasting menu with n dishes arranged in a circle,
 * where each dish has a flavor profile represented by an integer in flavors[].
 * A group of k guests will each receive a contiguous segment of k dishes from this
 * circular arrangement (wrapping around if necessary). However, due to dietary
 * restrictions, the segment must contain at most m repeated flavor values
 * (i.e., the count of any single flavor within the chosen segment must not exceed m).
 *
 * Your task is to find the maximum number of distinct flavor values achievable in
 * any valid contiguous circular segment of exactly k dishes, subject to the constraint
 * that no single flavor appears more than m times within the segment.
 * If no valid segment exists, return -1.
 *
 * Constraints:
 *   1 <= n <= 100000
 *   1 <= k <= n
 *   1 <= m <= k
 *   1 <= flavors[i] <= 100000
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Finds the maximum number of distinct flavors in any valid circular window
    /// of exactly k dishes, where no single flavor appears more than m times.
    ///
    /// Time Complexity:  O(n)  — we iterate over the doubled array once with a
    ///                           fixed-size sliding window of length k.
    /// Space Complexity: O(n)  — the doubled array plus the frequency dictionary
    ///                           which holds at most k distinct entries.
    /// </summary>
    public int MaxDistinctFlavors(int[] flavors, int k, int m)
    {
        // ── Step 1: Handle the circular nature by "doubling" the array ──────────
        // Because the menu is circular, a window can wrap around the end back to
        // the beginning. A classic trick is to concatenate the array with itself.
        // Any contiguous window of length k in this doubled array corresponds to
        // a valid circular window in the original array.
        // We only need positions 0 .. (n-1) as starting points, so the doubled
        // array of length 2*n is sufficient.
        int n = flavors.Length;

        // Build the doubled array: [flavors[0], ..., flavors[n-1], flavors[0], ..., flavors[n-1]]
        int[] doubled = new int[2 * n];
        for (int i = 0; i < n; i++)
        {
            doubled[i]     = flavors[i];   // first copy
            doubled[i + n] = flavors[i];   // second copy (handles wrap-around)
        }

        // ── Step 2: Prepare the sliding window data structures ──────────────────
        // We use a Dictionary<int, int> to track how many times each flavor
        // appears in the current window of size k.
        //
        // Why a dictionary?  Flavor values can be up to 100,000, so an array
        // indexed by flavor value would waste memory; a dictionary only stores
        // flavors actually present in the current window.
        var freq = new Dictionary<int, int>();

        // 'violators' counts how many distinct flavors currently exceed the
        // allowed maximum m.  A window is "valid" when violators == 0.
        int violators = 0;

        // 'distinctCount' tracks how many distinct flavors are in the window.
        int distinctCount = 0;

        // 'answer' stores the best (maximum) distinct count seen so far across
        // all valid windows.  We start at -1 to detect "no valid window found".
        int answer = -1;

        // ── Step 3: Seed the first window (indices 0 .. k-1) ───────────────────
        // Before entering the main sliding loop we manually add the first k
        // elements so that the window is fully populated from the start.
        for (int i = 0; i < k; i++)
        {
            AddFlavor(doubled[i], freq, ref distinctCount, ref violators, m);
        }

        // Check whether this very first window is already valid.
        if (violators == 0)
            answer = distinctCount;

        // ── Step 4: Slide the window across the doubled array ───────────────────
        // The right edge of the window moves from index k to index (2*n - 1).
        // The left edge (the element being removed) is always (right - k).
        //
        // We only consider windows whose LEFT edge starts at indices 0 .. n-1,
        // because starting at index n or beyond would duplicate a window we
        // already saw (the circular array has only n distinct starting positions).
        for (int right = k; right < 2 * n; right++)
        {
            // ── 4a: The left index of the window that is about to slide out ──
            int left = right - k; // this element leaves the window

            // We only need to evaluate windows that start at indices 0 .. n-1.
            // The window starting at 'left' is valid to consider only when
            // left < n (i.e., the starting position is within the original array).
            // After adding the new right element and removing the old left element
            // the window covers [left+1 .. right], which starts at left+1.
            // So we check left+1 < n, i.e., left < n-1  →  left <= n-2.
            // Equivalently, right <= n - 1 + k - 1 = n + k - 2.
            // But it is simpler to just guard with left < n after the update.

            // ── 4b: Add the new right element into the window ────────────────
            AddFlavor(doubled[right], freq, ref distinctCount, ref violators, m);

            // ── 4c: Remove the element that just left the window ─────────────
            RemoveFlavor(doubled[left], freq, ref distinctCount, ref violators, m);

            // ── 4d: The window now covers indices [left+1 .. right] ──────────
            // Its starting index in the original array is (left + 1).
            // We only care about starting indices 0 .. n-1.
            int windowStart = left + 1;
            if (windowStart >= n)
                break; // all n distinct starting positions have been evaluated

            // ── 4e: If the window is valid, update the answer ────────────────
            if (violators == 0)
            {
                // This window satisfies the constraint (no flavor exceeds m).
                // Record the distinct count if it is the best so far.
                if (distinctCount > answer)
                    answer = distinctCount;
            }
        }

        // ── Step 5: Return the result ───────────────────────────────────────────
        // 'answer' is -1 if no valid window was ever found, otherwise it holds
        // the maximum distinct flavor count across all valid windows.
        return answer;
    }

    // ── Helper: AddFlavor ───────────────────────────────────────────────────────
    // Increments the frequency of 'flavor' in the window and updates the
    // distinctCount and violators counters accordingly.
    private static void AddFlavor(
        int flavor,
        Dictionary<int, int> freq,
        ref int distinctCount,
        ref int violators,
        int m)
    {
        // Retrieve current count (0 if not present).
        freq.TryGetValue(flavor, out int count);

        if (count == 0)
        {
            // This flavor is new to the window → one more distinct flavor.
            distinctCount++;
        }

        // Increment the frequency.
        freq[flavor] = count + 1;

        if (count + 1 == m + 1)
        {
            // The flavor just crossed the threshold from m to m+1 occurrences,
            // so it becomes a new violator.
            violators++;
        }
    }

    // ── Helper: RemoveFlavor ────────────────────────────────────────────────────
    // Decrements the frequency of 'flavor' in the window and updates the
    // distinctCount and violators counters accordingly.
    private static void RemoveFlavor(
        int flavor,
        Dictionary<int, int> freq,
        ref int distinctCount,
        ref int violators,
        int m)
    {
        // The flavor must be present; retrieve its count.
        int count = freq[flavor];

        if (count == m + 1)
        {
            // The flavor was a violator (count > m) and is now dropping back to m,
            // so it is no longer a violator.
            violators--;
        }

        if (count == 1)
        {
            // The flavor is leaving the window entirely → one fewer distinct flavor.
            distinctCount--;
            freq.Remove(flavor); // keep the dictionary clean
        }
        else
        {
            freq[flavor] = count - 1;
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// flavors = [1, 2, 1, 3, 2, 4, 1], k = 4, m = 1
// Expected output: 4
// Explanation: The window [3, 2, 4, 1] (indices 3-6) has 4 distinct flavors,
// each appearing exactly once, which satisfies m = 1.
int[] flavors1 = { 1, 2, 1, 3, 2, 4, 1 };
int result1 = solution.MaxDistinctFlavors(flavors1, k: 4, m: 1);
Console.WriteLine($"Example 1: flavors=[1,2,1,3,2,4,1], k=4, m=1");
Console.WriteLine($"  Result   : {result1}");
Console.WriteLine($"  Expected : 4");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// flavors = [5, 5, 5, 5], k = 3, m = 1
// Expected output: -1
// Explanation: Every window of length 3 contains flavor 5 at least twice,
// violating m = 1.  No valid window exists.
int[] flavors2 = { 5, 5, 5, 5 };
int result2 = solution.MaxDistinctFlavors(flavors2, k: 3, m: 1);
Console.WriteLine($"Example 2: flavors=[5,5,5,5], k=3, m=1");
Console.WriteLine($"  Result   : {result2}");
Console.WriteLine($"  Expected : -1");
Console.WriteLine();

// ── Additional Test: wrap-around window ──────────────────────────────────────
// flavors = [3, 1, 2, 4], k = 3, m = 1
// Windows (circular):
//   [3,1,2] → 3 distinct, all once  ✓  → 3
//   [1,2,4] → 3 distinct, all once  ✓  → 3
//   [2,4,3] → 3 distinct, all once  ✓  → 3  (wrap: indices 2,3,0)
//   [4,3,1] → 3 distinct, all once  ✓  → 3  (wrap: indices 3,0,1)
// Expected output: 3
int[] flavors3 = { 3, 1, 2, 4 };
int result3 = solution.MaxDistinctFlavors(flavors3, k: 3, m: 1);
Console.WriteLine($"Additional Test: flavors=[3,1,2,4], k=3, m=1");
Console.WriteLine($"  Result   : {result3}");
Console.WriteLine($"  Expected : 3");
Console.WriteLine();

// ── Additional Test: m > 1 ───────────────────────────────────────────────────
// flavors = [1, 2, 1, 2, 3], k = 4, m = 2
// Windows:
//   [1,2,1,2] → 2 distinct, each twice  ✓  → 2
//   [2,1,2,3] → 3 distinct (2 twice, 1 once, 3 once) ✓ → 3
//   [1,2,3,1] → 3 distinct (1 twice, 2 once, 3 once) ✓ → 3  (wrap: 2,3,4,0)
//   [2,3,1,2] → 3 distinct (2 twice, 3 once, 1 once) ✓ → 3  (wrap: 3,4,0,1)
//   [3,1,2,1] → 3 distinct (1 twice, 2 once, 3 once) ✓ → 3  (wrap: 4,0,1,2)
// Expected output: 3
int[] flavors4 = { 1, 2, 1, 2, 3 };
int result4 = solution.MaxDistinctFlavors(flavors4, k: 4, m: 2);
Console.WriteLine($"Additional Test: flavors=[1,2,1,2,3], k=4, m=2");
Console.WriteLine($"  Result   : {result4}");
Console.WriteLine($"  Expected : 3");
Console.WriteLine();

// ── Additional Test: single element ──────────────────────────────────────────
// flavors = [7], k = 1, m = 1
// Only one window: [7] → 1 distinct, appears once ✓ → 1
// Expected output: 1
int[] flavors5 = { 7 };
int result5 = solution.MaxDistinctFlavors(flavors5, k: 1, m: 1);
Console.WriteLine($"Additional Test: flavors=[7], k=1, m=1");
Console.WriteLine($"  Result   : {result5}");
Console.WriteLine($"  Expected : 1");