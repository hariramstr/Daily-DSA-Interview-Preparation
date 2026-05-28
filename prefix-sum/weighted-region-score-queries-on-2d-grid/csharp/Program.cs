/*
 * Title: Weighted Region Score Queries on a 2D Grid
 * Difficulty: Hard
 * Topic: Prefix Sum
 *
 * Problem Description:
 * You are given an m x n integer matrix `grid` representing a map of terrain values,
 * and a list of queries. Each query is of the form [r1, c1, r2, c2, threshold],
 * where (r1, c1) is the top-left corner and (r2, c2) is the bottom-right corner
 * of a rectangular region.
 *
 * For each query, compute the "weighted region score": the sum of all elements in
 * the subgrid grid[r1..r2][c1..c2] that are STRICTLY GREATER THAN threshold.
 *
 * Because threshold values differ per query, a single static 2D prefix sum is not
 * sufficient. We must pre-process the grid so each query runs in better than O(m*n).
 *
 * Approach:
 *   1. Collect all unique cell values and sort them.
 *   2. For each unique value v (in sorted order), build a 2D prefix sum array that
 *      accumulates only cells whose value equals v. Call this layer[v].
 *   3. Build a "cumulative layer" prefix sum: for each sorted unique value index k,
 *      the prefix sum at layer k represents the sum of all cells whose value is
 *      <= sortedValues[k].
 *   4. For a query with threshold T, binary-search to find the largest unique value
 *      <= T, then use the corresponding cumulative 2D prefix sum to answer the
 *      rectangle query in O(1).
 *   5. The answer = (total sum of rectangle) - (sum of values <= threshold in rectangle).
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Method: WeightedRegionScoreQueries
     *
     * Time Complexity:
     *   Pre-processing: O(m * n * log(m*n))  — sorting cells + building layered prefix sums
     *   Per query:      O(log(m*n))           — binary search over unique values
     *   Total:          O(m*n*log(m*n) + Q*log(m*n))  where Q = number of queries
     *
     * Space Complexity:
     *   O(U * m * n) where U = number of unique values (at most m*n = 90,000)
     *   In practice we store cumulative prefix sums, so U layers of (m+1)*(n+1) arrays.
     *   Worst case ~90,000 * 301 * 301 ≈ too large if stored naively.
     *
     *   OPTIMIZED SPACE: Instead of storing one prefix-sum array per unique value,
     *   we store only ONE cumulative 2D prefix-sum array per unique value, but we
     *   reuse a running total. We keep a List of (value, prefixSumArray) snapshots.
     *   Since U ≤ m*n = 90,000 and each array is (m+1)*(n+1) ints, worst-case memory
     *   is large. For the given constraints (m,n ≤ 300, values in [-10^4,10^4]),
     *   U ≤ 20,001 distinct integers. We store U snapshots of 301×301 longs.
     *
     *   Practical note: For competitive use with m,n=300 and 20001 unique values,
     *   this would be ~20001 * 301 * 301 * 8 bytes ≈ 14 GB — too large.
     *
     *   BETTER APPROACH (implemented below):
     *   - Sort cells by value.
     *   - Process queries offline sorted by threshold.
     *   - Maintain a single running 2D prefix sum, updating it as we sweep values.
     *   - Answer each query when the sweep reaches its threshold.
     *   This gives O((m*n + Q) * log(...)) time and O(m*n + Q) space.
     */
    public long[] WeightedRegionScoreQueries(int[][] grid, int[][] queries)
    {
        // ── Step 1: Read grid dimensions ──────────────────────────────────────
        // We need m (rows) and n (cols) throughout the algorithm.
        int m = grid.Length;
        int n = grid[0].Length;

        // ── Step 2: Flatten all grid cells into a list of (value, row, col) ──
        // We need to process cells in sorted order of their values.
        // By flattening, we can sort all cells globally by value in one pass.
        var cells = new List<(int val, int r, int c)>(m * n);
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++)
                cells.Add((grid[r][c], r, c));

        // Sort cells by value ascending.
        // Why? We want to sweep from smallest to largest value so we can
        // incrementally build a prefix sum of "cells with value <= threshold".
        cells.Sort((a, b) => a.val.CompareTo(b.val));

        // ── Step 3: Compute the TOTAL 2D prefix sum of the entire grid ────────
        // This lets us find the sum of ANY rectangle in O(1).
        // prefix[r+1][c+1] = sum of grid[0..r][0..c]
        // We use (m+1) x (n+1) to avoid boundary checks (row 0 and col 0 are 0).
        var totalPrefix = new long[m + 1, n + 1];
        for (int r = 0; r < m; r++)
            for (int c = 0; c < n; c++)
                totalPrefix[r + 1, c + 1] = grid[r][c]
                    + totalPrefix[r, c + 1]
                    + totalPrefix[r + 1, c]
                    - totalPrefix[r, c];
        // Inclusion-exclusion: add top and left, subtract the overlap (top-left corner).

        // ── Step 4: Prepare queries for offline processing ────────────────────
        // "Offline" means we don't answer queries in their original order.
        // Instead, we sort queries by threshold so we can sweep cells in value order
        // and answer each query at the right moment.
        //
        // We store the original index so we can place answers back in order.
        int Q = queries.Length;
        var queryOrder = new int[Q];
        for (int i = 0; i < Q; i++) queryOrder[i] = i;

        // Sort query indices by their threshold value (queries[i][4]).
        Array.Sort(queryOrder, (a, b) => queries[a][4].CompareTo(queries[b][4]));

        // ── Step 5: Initialize the "running" 2D prefix sum ────────────────────
        // This prefix sum will accumulate values of cells as we sweep.
        // runningPrefix[r+1][c+1] = sum of values of cells processed so far
        //                           that lie in grid[0..r][0..c].
        // Initially all zeros (no cells processed yet).
        var runningPrefix = new long[m + 1, n + 1];

        // ── Step 6: Sweep cells by value and answer queries by threshold ───────
        // We maintain a pointer `cellIdx` into the sorted cells list.
        // For each query (in threshold order), we add all cells with value <= threshold
        // to runningPrefix, then answer the query.
        var answers = new long[Q];
        int cellIdx = 0;
        int totalCells = cells.Count;

        foreach (int qi in queryOrder)
        {
            // Extract query parameters.
            int r1 = queries[qi][0];
            int c1 = queries[qi][1];
            int r2 = queries[qi][2];
            int c2 = queries[qi][3];
            int threshold = queries[qi][4];

            // ── Step 6a: Add all cells with value <= threshold to runningPrefix ──
            // We advance cellIdx while the current cell's value <= threshold.
            // For each such cell, we do a "point update" on the 2D prefix sum.
            //
            // IMPORTANT: A true 2D prefix sum isn't directly updatable in O(1).
            // Instead, we use a 2D Binary Indexed Tree (Fenwick Tree) for point
            // updates and range queries, OR we rebuild the prefix sum after all
            // insertions for this threshold batch.
            //
            // Since queries are sorted by threshold, we process cells in batches.
            // We collect all cells for this batch, then rebuild the prefix sum once.
            // Rebuilding is O(m*n) per batch — but batches share work across queries.
            // Total rebuilds = number of distinct thresholds among queries ≤ Q.
            // Worst case O(Q * m * n) which is too slow for Q=10^5, m=n=300.
            //
            // BETTER: Use a 2D Fenwick Tree for O(log m * log n) point updates
            // and rectangle queries. Total: O(m*n*log m*log n + Q*log m*log n).
            //
            // We implement a 2D Fenwick Tree below.
            while (cellIdx < totalCells && cells[cellIdx].val <= threshold)
            {
                var (val, r, c) = cells[cellIdx];
                // Update the Fenwick tree at position (r, c) with `val`.
                // (Fenwick tree is 1-indexed, so we use r+1, c+1.)
                FenwickUpdate(runningPrefix, m, n, r + 1, c + 1, val);
                cellIdx++;
            }

            // ── Step 6b: Query the rectangle [r1,c1] to [r2,c2] ──────────────
            // sumLeThreshold = sum of cells in rectangle with value <= threshold.
            long sumLeThreshold = FenwickQuery(runningPrefix, r2 + 1, c2 + 1)
                                - FenwickQuery(runningPrefix, r1,     c2 + 1)
                                - FenwickQuery(runningPrefix, r2 + 1, c1    )
                                + FenwickQuery(runningPrefix, r1,     c1    );

            // ── Step 6c: Compute total sum of the rectangle ───────────────────
            // Using the precomputed totalPrefix (standard 2D prefix sum).
            long totalSum = totalPrefix[r2 + 1, c2 + 1]
                          - totalPrefix[r1,     c2 + 1]
                          - totalPrefix[r2 + 1, c1    ]
                          + totalPrefix[r1,     c1    ];

            // ── Step 6d: The answer is total minus the sum of values <= threshold ─
            // We want values STRICTLY GREATER THAN threshold.
            // sum(values > threshold) = totalSum - sum(values <= threshold)
            answers[qi] = totalSum - sumLeThreshold;
        }

        return answers;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2D Fenwick Tree (Binary Indexed Tree) Helpers
    //
    // A Fenwick Tree supports:
    //   - Point update: add a value at position (r, c)  → O(log m * log n)
    //   - Prefix query: sum of all values in [1..r][1..c] → O(log m * log n)
    //
    // The tree is stored in a 2D array `tree` of size (m+1) x (n+1).
    // Indices are 1-based.
    //
    // How it works:
    //   Each index i in a Fenwick tree is responsible for a range of indices
    //   determined by the lowest set bit of i (i & -i).
    //   Update: propagate upward by adding (i & -i) to i.
    //   Query:  accumulate downward by subtracting (i & -i) from i.
    // ─────────────────────────────────────────────────────────────────────────

    /// <summary>
    /// Point update: add `val` to position (r, c) in the 2D Fenwick tree.
    /// `r` and `c` are 1-based indices.
    /// </summary>
    private void FenwickUpdate(long[,] tree, int m, int n, int r, int c, long val)
    {
        // Outer loop: walk up the rows using Fenwick tree logic.
        for (int i = r; i <= m; i += i & -i)
        {
            // Inner loop: walk up the columns using Fenwick tree logic.
            for (int j = c; j <= n; j += j & -j)
            {
                tree[i, j] += val;
            }
        }
    }

    /// <summary>
    /// Prefix query: return the sum of all values in [1..r][1..c].
    /// `r` and `c` are 1-based indices.
    /// </summary>
    private long FenwickQuery(long[,] tree, int r, int c)
    {
        long sum = 0;
        // Outer loop: walk down the rows.
        for (int i = r; i > 0; i -= i & -i)
        {
            // Inner loop: walk down the columns.
            for (int j = c; j > 0; j -= j & -j)
            {
                sum += tree[i, j];
            }
        }
        return sum;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// grid = [[1,3,5],[2,4,6],[7,8,9]]
// queries = [[0,0,1,2,3], [1,1,2,2,5]]
// Expected output: [15, 23]
//
// Trace:
//   Query 1: rect [0,0]-[1,2] → values {1,3,5,2,4,6}
//            threshold=3 → values > 3: {5,4,6} → sum=15 ✓
//   Query 2: rect [1,1]-[2,2] → values {4,6,8,9}
//            threshold=5 → values > 5: {6,8,9} → sum=23 ✓

int[][] grid1 = new int[][]
{
    new int[] { 1, 3, 5 },
    new int[] { 2, 4, 6 },
    new int[] { 7, 8, 9 }
};

int[][] queries1 = new int[][]
{
    new int[] { 0, 0, 1, 2, 3 },
    new int[] { 1, 1, 2, 2, 5 }
};

long[] result1 = solution.WeightedRegionScoreQueries(grid1, queries1);
Console.WriteLine("Example 1:");
Console.WriteLine("Expected: [15, 23]");
Console.Write("Got:      [");
Console.Write(string.Join(", ", result1));
Console.WriteLine("]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// grid = [[10,-2],[-5,7]]
// queries = [[0,0,1,1,0], [0,0,0,1,9]]
// Expected output: [17, 10]
//
// Trace:
//   Query