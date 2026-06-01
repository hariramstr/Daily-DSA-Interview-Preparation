/*
 * Title: Average Score of Student Segments
 * Difficulty: Easy
 * Topic: Prefix Sum
 *
 * Problem Description:
 * A teacher has recorded the scores of n students in a linear array scores.
 * She wants to evaluate the performance of students over multiple contiguous
 * segments of the class roster. For each query, she provides a range [left, right]
 * (0-indexed, inclusive) and wants to know the average score (as a floating point
 * number) of students in that segment.
 *
 * Given an integer array scores and a 2D integer array queries where
 * queries[i] = [left, right], return an array of floating point numbers where
 * the i-th element is the average score of students from index left to right (inclusive).
 *
 * Constraints:
 * - 1 <= scores.length <= 10^5
 * - 0 <= scores[i] <= 100
 * - 1 <= queries.length <= 10^4
 * - 0 <= queries[i][0] <= queries[i][1] < scores.length
 */

using System;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Computes the average score for each [left, right] query using a prefix sum array.
    ///
    /// Time Complexity:  O(n + q)
    ///   - O(n) to build the prefix sum array (one pass over scores)
    ///   - O(q) to answer all queries (one O(1) lookup per query)
    ///   - Total is linear in the combined size of input, far better than the
    ///     naive O(n * q) approach that re-sums the range for every query.
    ///
    /// Space Complexity: O(n + q)
    ///   - O(n) for the prefix sum array
    ///   - O(q) for the output array
    ///   - No other significant auxiliary storage is used.
    /// </summary>
    public double[] AverageScoreOfSegments(int[] scores, int[][] queries)
    {
        // ── Step 1: Determine the length of the scores array ──────────────────
        // We need this to size the prefix sum array correctly.
        int n = scores.Length;

        // ── Step 2: Build the prefix sum array ────────────────────────────────
        // A prefix sum array (also called a cumulative sum array) stores at each
        // index i the total sum of all elements from scores[0] up to scores[i-1].
        //
        // We make it size (n + 1) so that:
        //   prefix[0] = 0          (empty prefix — a convenient sentinel value)
        //   prefix[1] = scores[0]
        //   prefix[2] = scores[0] + scores[1]
        //   ...
        //   prefix[i] = scores[0] + scores[1] + ... + scores[i-1]
        //
        // Why size n+1 instead of n?
        //   Using an extra slot at index 0 (set to 0) lets us compute any range
        //   sum with a single subtraction:
        //       sum(left, right) = prefix[right + 1] - prefix[left]
        //   Without the sentinel we would need a special case when left == 0.
        long[] prefix = new long[n + 1];

        // prefix[0] is already 0 by default (C# zero-initialises arrays).
        for (int i = 0; i < n; i++)
        {
            // Each prefix entry is the previous prefix entry plus the current score.
            // We use long to avoid integer overflow when scores are large and n is 10^5.
            // Maximum possible sum: 10^5 * 100 = 10,000,000 — fits in int, but long
            // is a safe habit for prefix sums.
            prefix[i + 1] = prefix[i] + scores[i];
        }

        // ── Step 3: Allocate the output array ─────────────────────────────────
        // One double result per query.
        int q = queries.Length;
        double[] results = new double[q];

        // ── Step 4: Answer each query in O(1) using the prefix sum ────────────
        for (int i = 0; i < q; i++)
        {
            // Extract the left and right boundaries of the current query.
            int left  = queries[i][0];
            int right = queries[i][1];

            // Compute the sum of scores[left..right] (inclusive) using the formula:
            //   rangeSum = prefix[right + 1] - prefix[left]
            //
            // Why does this work?
            //   prefix[right + 1] = sum of scores[0..right]
            //   prefix[left]      = sum of scores[0..left-1]
            //   Subtracting removes the part before 'left', leaving scores[left..right].
            long rangeSum = prefix[right + 1] - prefix[left];

            // Compute the number of students in this segment.
            // right - left + 1 gives the count of indices from left to right inclusive.
            int count = right - left + 1;

            // Divide to get the average.
            // Casting to double ensures we get a floating-point result, not integer division.
            results[i] = (double)rangeSum / count;
        }

        // ── Step 5: Return the completed results array ────────────────────────
        return results;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Driver Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Solution solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// scores  = [80, 90, 70, 60, 85]
// queries = [[0,2], [1,4]]
//
// Prefix array (size 6):
//   prefix[0] = 0
//   prefix[1] = 80
//   prefix[2] = 170
//   prefix[3] = 240
//   prefix[4] = 300
//   prefix[5] = 385
//
// Query [0,2]: rangeSum = prefix[3] - prefix[0] = 240 - 0 = 240; count = 3; avg = 80.0  ✓
// Query [1,4]: rangeSum = prefix[5] - prefix[1] = 385 - 80 = 305; count = 4; avg = 76.25 ✓

int[] scores1   = { 80, 90, 70, 60, 85 };
int[][] queries1 = { new[] { 0, 2 }, new[] { 1, 4 } };

double[] result1 = solution.AverageScoreOfSegments(scores1, queries1);

Console.WriteLine("=== Example 1 ===");
Console.WriteLine($"scores  = [{string.Join(", ", scores1)}]");
Console.WriteLine($"queries = [[0,2], [1,4]]");
Console.Write("Output  = [");
for (int i = 0; i < result1.Length; i++)
{
    Console.Write(result1[i].ToString("F2"));
    if (i < result1.Length - 1) Console.Write(", ");
}
Console.WriteLine("]");
Console.WriteLine("Expected: [80.00, 76.25]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// scores  = [50, 100, 40, 90, 20]
// queries = [[0,4], [2,3]]
//
// Prefix array (size 6):
//   prefix[0] = 0
//   prefix[1] = 50
//   prefix[2] = 150
//   prefix[3] = 190
//   prefix[4] = 280
//   prefix[5] = 300
//
// Query [0,4]: rangeSum = prefix[5] - prefix[0] = 300 - 0 = 300; count = 5; avg = 60.0  ✓
// Query [2,3]: rangeSum = prefix[4] - prefix[2] = 280 - 150 = 130; count = 2; avg = 65.0 ✓

int[] scores2    = { 50, 100, 40, 90, 20 };
int[][] queries2 = { new[] { 0, 4 }, new[] { 2, 3 } };

double[] result2 = solution.AverageScoreOfSegments(scores2, queries2);

Console.WriteLine("=== Example 2 ===");
Console.WriteLine($"scores  = [{string.Join(", ", scores2)}]");
Console.WriteLine($"queries = [[0,4], [2,3]]");
Console.Write("Output  = [");
for (int i = 0; i < result2.Length; i++)
{
    Console.Write(result2[i].ToString("F2"));
    if (i < result2.Length - 1) Console.Write(", ");
}
Console.WriteLine("]");
Console.WriteLine("Expected: [60.00, 65.00]");
Console.WriteLine();

// ── Edge Case: single-element query ──────────────────────────────────────────
int[] scores3    = { 42 };
int[][] queries3 = { new[] { 0, 0 } };

double[] result3 = solution.AverageScoreOfSegments(scores3, queries3);

Console.WriteLine("=== Edge Case: single element ===");
Console.WriteLine($"scores  = [42]");
Console.WriteLine($"queries = [[0,0]]");
Console.WriteLine($"Output  = [{result3[0]:F2}]");
Console.WriteLine("Expected: [42.00]");