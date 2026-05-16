/*
 * Title: Minimum Unique Colors in Every Window
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array `colors` of length `n`, where colors[i] represents the color
 * of the i-th paint bucket (as a positive integer). You are also given an integer `k`
 * representing the size of a sliding window.
 *
 * For each contiguous subarray of size `k`, compute the number of DISTINCT colors present.
 * Return the MINIMUM number of distinct colors found across all windows of size `k`.
 *
 * Additionally, return the STARTING INDEX of the first window that achieves this minimum.
 * If multiple windows tie for the minimum, return the smallest starting index.
 *
 * Your solution must run in O(n) time.
 *
 * Constraints:
 *   1 <= k <= n <= 100000
 *   1 <= colors[i] <= 10^6
 *
 * Example 1:
 *   Input:  colors = [1, 2, 1, 3, 2, 1, 1], k = 3
 *   Output: [2, 0]
 *   Explanation:
 *     Window [1,2,1] at index 0 → 2 distinct
 *     Window [2,1,3] at index 1 → 3 distinct
 *     Window [1,3,2] at index 2 → 3 distinct
 *     Window [3,2,1] at index 3 → 3 distinct
 *     Window [2,1,1] at index 4 → 2 distinct
 *     Minimum = 2, first achieved at index 0 → output [2, 0]
 *
 * Example 2:
 *   Input:  colors = [4, 4, 4, 1, 2, 3], k = 2
 *   Output: [1, 0]
 *   Explanation:
 *     Window [4,4] at index 0 → 1 distinct
 *     Window [4,4] at index 1 → 1 distinct
 *     Window [4,1] at index 2 → 2 distinct
 *     Window [1,2] at index 3 → 2 distinct
 *     Window [2,3] at index 4 → 2 distinct
 *     Minimum = 1, first achieved at index 0 → output [1, 0]
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Finds the minimum number of distinct colors in any window of size k,
    /// and the starting index of the first such window.
    ///
    /// Time Complexity:  O(n)  — each element is added once and removed once
    /// Space Complexity: O(k)  — the frequency dictionary holds at most k entries
    ///                           (one per element currently inside the window)
    /// </summary>
    /// <param name="colors">Array of paint-bucket colors (positive integers)</param>
    /// <param name="k">Window size</param>
    /// <returns>int[2] where [0] = minimum distinct count, [1] = first window start index</returns>
    public int[] MinUniqueColors(int[] colors, int k)
    {
        // ── STEP 1: Edge-case guard ───────────────────────────────────────────
        // If the array is empty or k is 0 there is nothing to process.
        // Returning early avoids index-out-of-range errors below.
        if (colors == null || colors.Length == 0 || k == 0)
            return new int[] { 0, 0 };

        int n = colors.Length;

        // ── STEP 2: Choose the right data structure ───────────────────────────
        // We need to track HOW MANY TIMES each color appears inside the current
        // window so that we know when a color is truly "gone" (count drops to 0).
        //
        // Dictionary<int, int>  colorCount
        //   key   = color value
        //   value = number of times that color appears in the current window
        //
        // Why a dictionary and not an array?
        //   Colors can be up to 10^6, so a direct-address array would waste memory
        //   when n is small. A dictionary uses only as much space as needed (O(k)).
        Dictionary<int, int> colorCount = new Dictionary<int, int>();

        // ── STEP 3: Initialise result trackers ────────────────────────────────
        // We will update these as we slide the window across the array.
        int minDistinct   = int.MaxValue; // best (smallest) distinct count seen so far
        int bestStartIdx  = 0;            // starting index of the first window with minDistinct

        // ── STEP 4: Build the FIRST window (indices 0 .. k-1) ─────────────────
        // Before we start sliding we need a fully populated window to compare against.
        // We add the first k elements to the frequency map.
        for (int i = 0; i < k; i++)
        {
            // Increment the frequency of colors[i].
            // GetValueOrDefault returns 0 if the key is not yet present.
            colorCount[colors[i]] = colorCount.GetValueOrDefault(colors[i], 0) + 1;
        }

        // The number of distinct colors in the first window equals the number of
        // keys currently in the dictionary (every key has count >= 1).
        int currentDistinct = colorCount.Count;

        // Record this as our initial best result.
        minDistinct  = currentDistinct;
        bestStartIdx = 0;

        // ── STEP 5: Slide the window from index 1 to index (n - k) ───────────
        // At each step:
        //   • The window moves one position to the right.
        //   • The element that just LEFT the window is colors[i - 1]  (left edge).
        //   • The element that just ENTERED the window is colors[i + k - 1] (right edge).
        //
        // We update the frequency map for both events, then check the distinct count.
        for (int i = 1; i <= n - k; i++)
        {
            // ── 5a: Remove the element that is leaving the window ─────────────
            // The element leaving is at position (i - 1), which was the left edge
            // of the previous window.
            int leaving = colors[i - 1];

            // Decrease its frequency by 1.
            colorCount[leaving]--;

            // If the frequency drops to 0 the color is no longer in the window.
            // We REMOVE it from the dictionary so that colorCount.Count accurately
            // reflects the number of DISTINCT colors currently present.
            if (colorCount[leaving] == 0)
            {
                colorCount.Remove(leaving);
                // One fewer distinct color in the window.
                currentDistinct--;
            }

            // ── 5b: Add the element that is entering the window ───────────────
            // The new right-edge element is at position (i + k - 1).
            int entering = colors[i + k - 1];

            // Check whether this color was already present in the window.
            bool wasAbsent = !colorCount.ContainsKey(entering);

            // Increase its frequency by 1.
            colorCount[entering] = colorCount.GetValueOrDefault(entering, 0) + 1;

            // If the color was not present before, the distinct count increases.
            if (wasAbsent)
                currentDistinct++;

            // ── 5c: Update the global minimum ────────────────────────────────
            // We only update when we find a STRICTLY smaller distinct count.
            // This guarantees we keep the FIRST (smallest) starting index in case
            // of ties, because we process windows left-to-right and never overwrite
            // an equal result.
            if (currentDistinct < minDistinct)
            {
                minDistinct  = currentDistinct;
                bestStartIdx = i;
            }
        }

        // ── STEP 6: Return the answer ─────────────────────────────────────────
        // [0] = minimum distinct color count across all windows
        // [1] = starting index of the first window that achieves that minimum
        return new int[] { minDistinct, bestStartIdx };
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code  (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// colors = [1, 2, 1, 3, 2, 1, 1],  k = 3
// Windows:
//   index 0: [1,2,1]   → 2 distinct
//   index 1: [2,1,3]   → 3 distinct
//   index 2: [1,3,2]   → 3 distinct
//   index 3: [3,2,1]   → 3 distinct
//   index 4: [2,1,1]   → 2 distinct
// Minimum = 2, first at index 0  →  expected output: [2, 0]
int[] colors1 = { 1, 2, 1, 3, 2, 1, 1 };
int   k1      = 3;
int[] result1 = sol.MinUniqueColors(colors1, k1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input : colors = [{string.Join(", ", colors1)}], k = {k1}");
Console.WriteLine($"  Output: [{result1[0]}, {result1[1]}]");
Console.WriteLine($"  Expected: [2, 0]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// colors = [4, 4, 4, 1, 2, 3],  k = 2
// Windows:
//   index 0: [4,4]  → 1 distinct
//   index 1: [4,4]  → 1 distinct
//   index 2: [4,1]  → 2 distinct
//   index 3: [1,2]  → 2 distinct
//   index 4: [2,3]  → 2 distinct
// Minimum = 1, first at index 0  →  expected output: [1, 0]
int[] colors2 = { 4, 4, 4, 1, 2, 3 };
int   k2      = 2;
int[] result2 = sol.MinUniqueColors(colors2, k2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input : colors = [{string.Join(", ", colors2)}], k = {k2}");
Console.WriteLine($"  Output: [{result2[0]}, {result2[1]}]");
Console.WriteLine($"  Expected: [1, 0]");
Console.WriteLine();

// ── Extra test: window equals full array ─────────────────────────────────────
// colors = [5, 5, 5],  k = 3
// Only one window: [5,5,5] → 1 distinct  →  expected output: [1, 0]
int[] colors3 = { 5, 5, 5 };
int   k3      = 3;
int[] result3 = sol.MinUniqueColors(colors3, k3);
Console.WriteLine("Extra test (k == n):");
Console.WriteLine($"  Input : colors = [{string.Join(", ", colors3)}], k = {k3}");
Console.WriteLine($"  Output: [{result3[0]}, {result3[1]}]");
Console.WriteLine($"  Expected: [1, 0]");
Console.WriteLine();

// ── Extra test: all distinct colors ──────────────────────────────────────────
// colors = [1, 2, 3, 4, 5],  k = 3
// Windows:
//   index 0: [1,2,3] → 3 distinct
//   index 1: [2,3,4] → 3 distinct
//   index 2: [3,4,5] → 3 distinct
// Minimum = 3, first at index 0  →  expected output: [3, 0]
int[] colors4 = { 1, 2, 3, 4, 5 };
int   k4      = 3;
int[] result4 = sol.MinUniqueColors(colors4, k4);
Console.WriteLine("Extra test (all distinct):");
Console.WriteLine($"  Input : colors = [{string.Join(", ", colors4)}], k = {k4}");
Console.WriteLine($"  Output: [{result4[0]}, {result4[1]}]");
Console.WriteLine($"  Expected: [3, 0]");