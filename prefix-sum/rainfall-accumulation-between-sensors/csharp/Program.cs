/*
 * Title: Rainfall Accumulation Between Sensors
 * Difficulty: Medium
 * Topic: Prefix Sum
 *
 * Problem Description:
 * A weather monitoring system has placed sensors at various positions along a straight
 * road of length n meters. You are given an integer array `rainfall` of length n,
 * where rainfall[i] represents the amount of rainfall (in millimeters) recorded at position i.
 *
 * You are also given a 2D array `queries` where each query queries[j] = [left, right, threshold]
 * asks: How many contiguous subarrays within the range [left, right] (inclusive) have a total
 * rainfall strictly greater than threshold?
 *
 * A contiguous subarray means any subarray rainfall[a..b] where left <= a <= b <= right.
 *
 * Return an integer array results where results[j] is the answer to the j-th query.
 *
 * Constraints:
 *   1 <= n <= 1000
 *   0 <= rainfall[i] <= 100
 *   1 <= queries.length <= 500
 *   0 <= left <= right < n
 *   0 <= threshold <= 10^5
 *
 * Example 1:
 *   Input:  rainfall = [3, 1, 4, 1, 5], queries = [[0,3,5],[1,4,7]]
 *   Output: [4, 5]
 *
 * Example 2:
 *   Input:  rainfall = [2, 2, 2, 2], queries = [[0,3,4],[0,2,6]]
 *   Output: [6, 0]
 */

using System;
using System.Text;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class — keeps the algorithm neatly encapsulated
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Counts, for every query [left, right, threshold], how many contiguous
    /// subarrays rainfall[a..b] (left ≤ a ≤ b ≤ right) have sum > threshold.
    ///
    /// Time Complexity :  O(n + Q * n²)
    ///   - Building the prefix-sum array is O(n).
    ///   - For each of the Q queries we iterate over all O(n²) starting/ending
    ///     pairs inside [left, right] and answer each pair in O(1) using the
    ///     prefix sum.  With n ≤ 1 000 and Q ≤ 500 this is at most ~500 million
    ///     simple operations — tight but fine for the given constraints.
    ///
    /// Space Complexity : O(n) for the prefix-sum array (plus O(Q) for output).
    /// </summary>
    public int[] CountSubarraysAboveThreshold(int[] rainfall, int[][] queries)
    {
        // ── Step 1: Build the prefix-sum array ────────────────────────────────
        //
        // A prefix-sum array lets us compute the sum of any subarray rainfall[a..b]
        // in O(1) time instead of O(n) time.
        //
        // Definition:
        //   prefix[0] = 0          (sentinel — makes the formula uniform)
        //   prefix[i] = rainfall[0] + rainfall[1] + ... + rainfall[i-1]
        //
        // Then:
        //   sum(rainfall[a..b]) = prefix[b+1] - prefix[a]
        //
        // Why a sentinel at index 0?
        //   It avoids a special case when a == 0 (prefix[0] = 0, so the
        //   subtraction still works correctly).

        int n = rainfall.Length;

        // Allocate one extra slot for the sentinel.
        int[] prefix = new int[n + 1];
        prefix[0] = 0; // sentinel

        for (int i = 0; i < n; i++)
        {
            // Each entry is the cumulative sum up to (but not including) index i+1.
            prefix[i + 1] = prefix[i] + rainfall[i];
        }

        // ── Step 2: Prepare the results array ─────────────────────────────────
        //
        // We need one answer per query, so allocate an array of the same length.

        int[] results = new int[queries.Length];

        // ── Step 3: Process each query independently ──────────────────────────
        //
        // For query j = [left, right, threshold] we must count all pairs (a, b)
        // such that:
        //   left ≤ a ≤ b ≤ right   AND   sum(rainfall[a..b]) > threshold
        //
        // We iterate over every valid starting index `a` and, for each `a`,
        // over every valid ending index `b` (b ≥ a).  The subarray sum is
        // retrieved in O(1) via the prefix array.

        for (int j = 0; j < queries.Length; j++)
        {
            int left      = queries[j][0];
            int right     = queries[j][1];
            int threshold = queries[j][2];

            int count = 0; // will accumulate qualifying subarrays for this query

            // Outer loop: choose the start index `a` of the subarray.
            // `a` ranges from `left` to `right` (a subarray of length 1 is valid).
            for (int a = left; a <= right; a++)
            {
                // Inner loop: choose the end index `b` of the subarray.
                // `b` must be at least `a` (length ≥ 1) and at most `right`.
                for (int b = a; b <= right; b++)
                {
                    // ── Step 3a: Compute subarray sum in O(1) ─────────────────
                    //
                    // Using the prefix-sum formula:
                    //   sum(rainfall[a..b]) = prefix[b+1] - prefix[a]
                    //
                    // Example: rainfall = [3,1,4,1,5], prefix = [0,3,4,8,9,14]
                    //   sum(rainfall[1..3]) = prefix[4] - prefix[1] = 9 - 3 = 6  ✓

                    int subarraySum = prefix[b + 1] - prefix[a];

                    // ── Step 3b: Check the threshold condition ─────────────────
                    //
                    // The problem asks for STRICTLY greater than threshold,
                    // so we use `>` (not `>=`).

                    if (subarraySum > threshold)
                    {
                        count++;
                    }
                }
            }

            // Store the answer for query j.
            results[j] = count;
        }

        // ── Step 4: Return all answers ────────────────────────────────────────
        return results;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / verification code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

static string ArrayToString(int[] arr)
{
    // Helper: pretty-print an integer array as "[a, b, c, ...]"
    var sb = new StringBuilder("[");
    for (int i = 0; i < arr.Length; i++)
    {
        sb.Append(arr[i]);
        if (i < arr.Length - 1) sb.Append(", ");
    }
    sb.Append(']');
    return sb.ToString();
}

var solver = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// rainfall = [3, 1, 4, 1, 5]
// queries  = [[0,3,5], [1,4,7]]
// Expected output: [4, 5]
//
// Manual trace for query [0,3,5] (left=0, right=3, threshold=5):
//   All subarrays of rainfall[0..3] = [3,1,4,1]:
//     [3]      = 3  → no
//     [3,1]    = 4  → no
//     [3,1,4]  = 8  → YES
//     [3,1,4,1]= 9  → YES
//     [1]      = 1  → no
//     [1,4]    = 5  → no  (not STRICTLY greater)
//     [1,4,1]  = 6  → YES
//     [4]      = 4  → no
//     [4,1]    = 5  → no  (not STRICTLY greater)
//     [1]      = 1  → no
//   Count = 3 ... wait, let me recount:
//     [3,1,4]=8 YES, [3,1,4,1]=9 YES, [1,4,1]=6 YES, [4,1,5] — wait right=3 so max index 3.
//   Subarrays within [0..3]:
//     (0,0)=3 no | (0,1)=4 no | (0,2)=8 YES | (0,3)=9 YES
//     (1,1)=1 no | (1,2)=5 no | (1,3)=6 YES
//     (2,2)=4 no | (2,3)=5 no
//     (3,3)=1 no
//   Count = 3 ... hmm, expected 4.
//   Let me recheck: rainfall[0..3]=[3,1,4,1]
//     prefix=[0,3,4,8,9,14]
//     (0,2): prefix[3]-prefix[0]=8-0=8 >5 YES
//     (0,3): prefix[4]-prefix[0]=9-0=9 >5 YES
//     (1,3): prefix[4]-prefix[1]=9-3=6 >5 YES
//   That's 3. But expected is 4. Let me re-read the problem example explanation:
//   "[3,1,4]=8, [1,4,1]=6, [3,1,4,1]=9" — that's 3 listed, but they say 4.
//   The explanation also mentions [4,1,5] but right=3 so index 4 is out.
//   Possibly the expected answer in the problem statement has a typo, or I'm
//   misreading. Let me trust the algorithm (which is correct) and run it.

Console.WriteLine("=== Example 1 ===");
int[] rainfall1 = [3, 1, 4, 1, 5];
int[][] queries1 = [[0, 3, 5], [1, 4, 7]];
int[] result1 = solver.CountSubarraysAboveThreshold(rainfall1, queries1);
Console.WriteLine($"Input rainfall : {ArrayToString(rainfall1)}");
Console.WriteLine($"Query [0,3,5]  : {result1[0]}  (expected ~3 per trace; problem says 4)");
Console.WriteLine($"Query [1,4,7]  : {result1[1]}");
Console.WriteLine($"Full output    : {ArrayToString(result1)}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// rainfall = [2, 2, 2, 2]
// queries  = [[0,3,4], [0,2,6]]
// Expected output: [6, 0]
//
// Manual trace for query [0,3,4] (threshold=4):
//   All subarrays of [2,2,2,2]:
//     len-1: 2,2,2,2          → all ≤ 4, none qualify
//     len-2: 4,4,4            → all = 4, NOT strictly greater, none qualify
//     len-3: 6,6,6            → all > 4, 3 qualify
//     len-4: 8                → > 4, 1 qualifies
//   Wait that's only 4. But expected is 6.
//   Hmm — len-2 subarrays sum to 4, which is NOT > 4. len-3 gives 3 subarrays,
//   len-4 gives 1 → total 4. But expected is 6.
//   Let me recount: subarrays of length ≥ 3 within [0..3]:
//     (0,2)=6, (0,3)=8, (1,3)=6, (2,3)... wait (2,3) is length 2 = 4, not >4.
//     (0,2)=6 YES, (1,3)=6 YES, (0,3)=8 YES → 3 subarrays of len≥3.
//   Hmm still 3 not 6. The problem explanation says "subarrays of length >= 3".
//   There are C(4,2)+4 = 10 total subarrays. Length-3: (0,2),(1,3),(0,3) — wait
//   (0,3) is length 4. Length-3 subarrays: (0,2),(1,3) — that's only 2 within [0..3].
//   Length-4: (0,3) — that's 1. Total = 3. Still not 6.
//   The problem example explanation may be incorrect. Our algorithm is correct;
//   let's just display what it computes.

Console.WriteLine("=== Example 2 ===");
int[] rainfall2 = [2, 2, 2, 2];
int[][] queries2 = [[0, 3, 4], [0, 2, 6]];
int[] result2 = solver.CountSubarraysAboveThreshold(rainfall2, queries2);
Console.WriteLine($"Input rainfall : {ArrayToString(rainfall2)}");
Console.WriteLine($"Query [0,3,4]  : {result2[0]}");
Console.WriteLine($"Query [0,2,6]  : {result2[1]}");
Console.WriteLine($"Full output    : {ArrayToString(result2)}");
Console.WriteLine();

// ── Additional custom test ────────────────────────────────────────────────────
Console.WriteLine("=== Custom Test ===");
int[] rainfall3 = [5, 5, 5, 5, 5];
int[][] queries3 = [[0, 4, 9], [0, 4, 14], [2, 4, 4]];
int[] result3 = solver.CountSubarraysAboveThreshold(rainfall3, queries3);
Console.WriteLine($"Input rainfall : {ArrayToString(rainfall3)}");
Console.WriteLine($"Query [0,4,9]  : {result3[0]}");
Console.WriteLine($"Query [0,4,14] : {result3[1]}");
Console.WriteLine($"Query [2,4,4]  : {result3[2]}");
Console.WriteLine($"Full output    : {ArrayToString(result3)}");