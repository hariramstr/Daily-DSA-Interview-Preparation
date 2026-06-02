/*
 * Title: Total Rainfall Between Two Checkpoints
 * Difficulty: Easy
 * Topic: Prefix Sum
 *
 * Problem Description:
 * A weather station has recorded daily rainfall measurements along a straight highway.
 * There are n checkpoints numbered from 0 to n-1, and rainfall[i] represents the amount
 * of rainfall (in millimeters) recorded at checkpoint i on a given day.
 *
 * Given an integer array rainfall of length n and a 2D array queries where
 * queries[j] = [left, right], for each query find the total rainfall recorded between
 * checkpoint left and checkpoint right, inclusive.
 *
 * Return an integer array answer where answer[j] is the total rainfall for the j-th query.
 *
 * Constraints:
 * - 1 <= n <= 10^5
 * - 0 <= rainfall[i] <= 1000
 * - 1 <= queries.length <= 10^5
 * - 0 <= left <= right < n
 *
 * Example 1:
 * Input:  rainfall = [3, 1, 4, 1, 5, 9, 2, 6], queries = [[1,4],[0,6],[3,3]]
 * Output: [11, 25, 1]
 *
 * Example 2:
 * Input:  rainfall = [0, 2, 7, 3, 5], queries = [[0,4],[2,3]]
 * Output: [17, 10]
 */

using System;
using System.Text;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the Prefix Sum algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Computes the total rainfall for each [left, right] query using a prefix sum array.
    ///
    /// Time Complexity:  O(n + q)  — O(n) to build the prefix array, O(1) per query → O(q) total
    /// Space Complexity: O(n)      — we allocate one extra array of size n+1 for prefix sums
    ///
    /// Why Prefix Sum?
    /// ───────────────
    /// A naïve approach would loop from index `left` to `right` for every query, giving
    /// O(n * q) time — up to 10^10 operations for the maximum constraints, which is far
    /// too slow.
    ///
    /// The Prefix Sum technique pre-computes cumulative totals so that any range sum can
    /// be answered in O(1) using simple subtraction:
    ///     rangeSum(left, right) = prefix[right + 1] - prefix[left]
    /// </summary>
    public int[] TotalRainfall(int[] rainfall, int[][] queries)
    {
        // ── Step 1: Determine the length of the rainfall array ──────────────
        // We need n to size our prefix array correctly.
        int n = rainfall.Length;

        // ── Step 2: Build the prefix sum array ──────────────────────────────
        // We create an array of size (n + 1) instead of n.
        // The extra slot at index 0 acts as a "sentinel" with value 0.
        // This simplifies the range-sum formula and avoids special-casing
        // queries that start at index 0.
        //
        // Definition:
        //   prefix[0]   = 0                          (sentinel / base case)
        //   prefix[i+1] = prefix[i] + rainfall[i]   for i in [0, n-1]
        //
        // After building:
        //   prefix[k] = rainfall[0] + rainfall[1] + ... + rainfall[k-1]
        //             = sum of the first k elements (indices 0 through k-1)
        //
        // Example (rainfall = [3,1,4,1,5,9,2,6]):
        //   prefix = [0, 3, 4, 8, 9, 14, 23, 25, 31]
        //             ^                               ^
        //          sentinel                    total of all elements
        int[] prefix = new int[n + 1];

        // prefix[0] is already 0 by default in C# — no explicit assignment needed.
        for (int i = 0; i < n; i++)
        {
            // Each prefix[i+1] accumulates the running total up to and including
            // rainfall[i].  We shift by +1 so that prefix[0] stays 0.
            prefix[i + 1] = prefix[i] + rainfall[i];
        }

        // ── Step 3: Answer each query in O(1) ───────────────────────────────
        // For a query [left, right] we want:
        //   rainfall[left] + rainfall[left+1] + ... + rainfall[right]
        //
        // Using our prefix array:
        //   = prefix[right + 1] - prefix[left]
        //
        // Why does this work?
        //   prefix[right + 1] = sum of elements at indices 0 … right
        //   prefix[left]      = sum of elements at indices 0 … left-1
        //   Subtracting removes the part before `left`, leaving exactly
        //   the sum from `left` to `right`.
        //
        // Trace for Example 1, query [1, 4]:
        //   prefix[5] - prefix[1] = 14 - 3 = 11  ✓  (1+4+1+5 = 11)
        //
        // Trace for Example 1, query [0, 6]:
        //   prefix[7] - prefix[0] = 25 - 0 = 25  ✓  (3+1+4+1+5+9+2 = 25)
        //
        // Trace for Example 1, query [3, 3]:
        //   prefix[4] - prefix[3] = 9 - 8 = 1    ✓  (just rainfall[3] = 1)
        int[] answer = new int[queries.Length];

        for (int j = 0; j < queries.Length; j++)
        {
            // Extract the left and right boundaries of this query.
            int left  = queries[j][0];
            int right = queries[j][1];

            // Apply the O(1) range-sum formula.
            // prefix[right + 1] includes rainfall[right]; prefix[left] excludes it.
            answer[j] = prefix[right + 1] - prefix[left];
        }

        // ── Step 4: Return the completed answers array ───────────────────────
        return answer;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

// Helper: formats an int[] as "[a, b, c, ...]" for readable console output.
static string ArrayToString(int[] arr)
{
    var sb = new StringBuilder("[");
    for (int i = 0; i < arr.Length; i++)
    {
        sb.Append(arr[i]);
        if (i < arr.Length - 1) sb.Append(", ");
    }
    sb.Append(']');
    return sb.ToString();
}

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
Console.WriteLine("=== Example 1 ===");
int[] rainfall1 = [3, 1, 4, 1, 5, 9, 2, 6];
int[][] queries1 = [[1, 4], [0, 6], [3, 3]];

int[] result1 = solution.TotalRainfall(rainfall1, queries1);

Console.WriteLine($"Rainfall : {ArrayToString(rainfall1)}");
Console.WriteLine($"Queries  : [[1,4], [0,6], [3,3]]");
Console.WriteLine($"Output   : {ArrayToString(result1)}");
Console.WriteLine($"Expected : [11, 25, 1]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
Console.WriteLine("=== Example 2 ===");
int[] rainfall2 = [0, 2, 7, 3, 5];
int[][] queries2 = [[0, 4], [2, 3]];

int[] result2 = solution.TotalRainfall(rainfall2, queries2);

Console.WriteLine($"Rainfall : {ArrayToString(rainfall2)}");
Console.WriteLine($"Queries  : [[0,4], [2,3]]");
Console.WriteLine($"Output   : {ArrayToString(result2)}");
Console.WriteLine($"Expected : [17, 10]");
Console.WriteLine();

// ── Edge-case: single element, single query ───────────────────────────────────
Console.WriteLine("=== Edge Case: single element ===");
int[] rainfall3 = [42];
int[][] queries3 = [[0, 0]];

int[] result3 = solution.TotalRainfall(rainfall3, queries3);

Console.WriteLine($"Rainfall : {ArrayToString(rainfall3)}");
Console.WriteLine($"Queries  : [[0,0]]");
Console.WriteLine($"Output   : {ArrayToString(result3)}");
Console.WriteLine($"Expected : [42]");