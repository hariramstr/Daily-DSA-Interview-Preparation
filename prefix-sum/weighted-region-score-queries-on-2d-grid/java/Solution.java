```java
/*
 * Title: Weighted Region Score Queries on a 2D Grid
 *
 * Problem Description:
 * You are given an m x n integer matrix `grid` representing a map of terrain values,
 * and a list of queries. Each query is of the form [r1, c1, r2, c2, threshold],
 * where (r1, c1) is the top-left corner and (r2, c2) is the bottom-right corner
 * of a rectangular region.
 *
 * For each query, you must compute the weighted region score: the sum of all elements
 * in the subgrid grid[r1..r2][c1..c2] that are strictly greater than threshold.
 * Return an array of integers where the i-th element is the weighted region score
 * for the i-th query.
 *
 * Because threshold values differ per query, a single static 2D prefix sum is not
 * sufficient. You must design a solution that pre-processes the grid efficiently so
 * that each query can be answered in better than O(m * n) time.
 *
 * Approach: Sort and group cells by value, build layered prefix sums indexed by
 * sorted unique values, and use binary search to find the appropriate layer per query.
 *
 * Constraints:
 * - 1 <= m, n <= 300
 * - -10^4 <= grid[i][j] <= 10^4
 * - 1 <= queries.length <= 10^5
 * - 0 <= r1 <= r2 < m
 * - 0 <= c1 <= c2 < n
 * - -10^4 <= threshold <= 10^4
 */

import java.util.*;

/**
 * Solution class for Weighted Region Score Queries on a 2D Grid.
 *
 * <p>Core Idea:
 * We want to answer queries of the form: "sum of elements in rectangle [r1,c1,r2,c2]
 * that are strictly greater than threshold."
 *
 * <p>Strategy - Layered Prefix Sums:
 * 1. Collect all unique cell values and sort them.
 * 2. For each unique value v (in sorted order), build a 2D prefix sum array that
 *    accumulates only cells with value == v.
 * 3. Store these prefix sum layers in a list, indexed by sorted unique values.
 * 4. For a query with threshold T, we need the sum of all cells > T in the rectangle.
 *    Using binary search, find the first unique value strictly greater than T,
 *    then sum up all layers from that index onward using the prefix sum arrays.
 *
 * <p>To efficiently sum multiple layers, we maintain a "cumulative" prefix sum:
 * cumulativePrefix[k][i][j] = sum of prefix sums for layers 0..k at position (i,j).
 * This allows us to answer "sum of all values > T in rectangle" in O(1) per query
 * after O(m*n*K) preprocessing, where K = number of unique values.
 */
public class Solution {

    // -------------------------------------------------------------------------
    // Fields for the layered prefix sum structure
    // -------------------------------------------------------------------------

    /** Sorted list of unique values found in the grid. */
    private int[] sortedUniqueValues;

    /**
     * cumulativePrefixSum[k][i][j] represents the 2D prefix sum of all grid cells
     * whose value equals sortedUniqueValues[k] or any value with index < k,
     * i.e., it's a running total of prefix sums from layer 0 up to layer k.
     *
     * More precisely, for layer k:
     *   layerPrefix[k][i][j] = prefix sum of cells with value == sortedUniqueValues[k]
     *   cumulativePrefixSum[k][i][j] = sum of layerPrefix[0..k][i][j]
     *
     * This lets us answer "sum of all values in layers k..K-1 in a rectangle"
     * by using: totalSum - cumulativePrefixSum[k-1] query result.
     */
    private long[][][] cumulativePrefixSum;

    /** Number of rows in the grid. */
    private int m;

    /** Number of columns in the grid. */
    private int n;

    // -------------------------------------------------------------------------
    // Main preprocessing method
    // -------------------------------------------------------------------------

    /**
     * Preprocesses the grid to enable efficient range queries.
     *
     * <p>Steps:
     * 1. Collect all unique values from the grid and sort them.
     * 2. For each unique value, build a 2D prefix sum of cells equal to that value.
     * 3. Accumulate these prefix sums into a cumulative structure so that
     *    cumulativePrefixSum[k] = sum of layers 0 through k.
     *
     * @param grid the 2D integer grid of terrain values
     * @return nothing (modifies internal state)
     *
     * Time complexity: O(m * n * K) where K = number of unique values (at most m*n)
     * Space complexity: O(m * n * K) for storing all prefix sum layers
     */
    public void preprocess(int[][] grid) {
        this.m = grid.length;
        this.n = grid[0].length;

        // Step 1: Collect all unique values using a TreeSet (auto-sorted)
        TreeSet<Integer> uniqueSet = new TreeSet<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                uniqueSet.add(grid[i][j]);
            }
        }

        // Convert to sorted array for indexed access and binary search
        sortedUniqueValues = new int[uniqueSet.size()];
        int idx = 0;
        for (int val : uniqueSet) {
            sortedUniqueValues[idx++] = val;
        }

        int K = sortedUniqueValues.length; // number of unique values

        // Step 2: Build cumulative prefix sum layers
        // cumulativePrefixSum has dimensions [K][m+1][n+1]
        // We use (m+1) x (n+1) to simplify boundary conditions (1-indexed prefix sums)
        cumulativePrefixSum = new long[K][m + 1][n + 1];

        // Map each unique value to its index for quick lookup
        // (We'll use binary search instead, but a map helps during construction)
        Map<Integer, Integer> valueToIndex = new HashMap<>();
        for (int k = 0; k < K; k++) {
            valueToIndex.put(sortedUniqueValues[k], k);
        }

        // Step 3: For each layer k, compute the 2D prefix sum of cells == sortedUniqueValues[k]
        // We'll build each layer's raw prefix sum first, then accumulate

        // Temporary array to hold the current layer's prefix sum before accumulation
        long[][] layerPrefix = new long[m + 1][n + 1];

        for (int k = 0; k < K; k++) {
            int targetValue = sortedUniqueValues[k];

            // Reset layerPrefix for this layer
            // (We reuse the array, so we must clear it)
            for (int i = 0; i <= m; i++) {
                Arrays.fill(layerPrefix[i], 0L);
            }

            // Build 2D prefix sum for cells with value == targetValue
            // Using 1-indexed prefix sum: prefix[i][j] = sum of cells in rows [1..i], cols [1..j]
            for (int i = 1; i <= m; i++) {
                for (int j = 1; j <= n; j++) {
                    // Cell value at 0-indexed position (i-1, j-1)
                    long cellContribution = (grid[i - 1][j - 1] == targetValue) ? targetValue : 0L;

                    // Standard 2D prefix sum formula:
                    // prefix[i][j] = cell + prefix[i-1][j] + prefix[i][j-1] - prefix[i-1][j-1]
                    layerPrefix[i][j] = cellContribution
                            + layerPrefix[i - 1][j]
                            + layerPrefix[i][j - 1]
                            - layerPrefix[i - 1][j - 1];
                }
            }

            // Step 4: Accumulate into cumulativePrefixSum
            // cumulativePrefixSum[k][i][j] = cumulativePrefixSum[k-1][i][j] + layerPrefix[i][j]
            for (int i = 0; i <= m; i++) {
                for (int j = 0; j <= n; j++) {
                    if (k == 0) {
                        // First layer: cumulative equals this layer's prefix sum
                        cumulativePrefixSum[k][i][j] = layerPrefix[i][j];
                    } else {
                        // Add current layer's prefix sum to the previous cumulative
                        cumulativePrefixSum[k][i][j] = cumulativePrefixSum[k - 1][i][j] + layerPrefix[i][j];
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Query helper: rectangle sum from a specific prefix sum layer
    // -------------------------------------------------------------------------

    /**
     * Computes the rectangle sum from a given 2D prefix sum array.
     *
     * <p>Uses the standard inclusion-exclusion formula for 2D prefix sums:
     * sum(r1,c1,r2,c2) = prefix[r2+1][c2+1] - prefix[r1][c2+1] - prefix[r2+1][c1] + prefix[r1][c1]
     *
     * @param prefix the 2D prefix sum array (1-indexed, size (m+1) x (n+1))
     * @param r1     top-left row (0-indexed in original grid)
     * @param c1     top-left column (0-indexed in original grid)
     * @param r2     bottom-right row (0-indexed in original grid)
     * @param c2     bottom-right column (0-indexed in original grid)
     * @return the sum of values in the rectangle [r1,c1] to [r2,c2]
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    private long rectangleSum(long[][] prefix, int r1, int c1, int r2, int c2) {
        // Convert to 1-indexed: the rectangle [r1..r2][c1..c2] in 0-indexed
        // corresponds to [r1+1..r2+1][c1+1..c2+1] in 1-indexed prefix sum
        // Inclusion-exclusion:
        return prefix[r2 + 1][c2 + 1]
                - prefix[r1][c2 + 1]
                - prefix[r2 + 1][c1]
                + prefix[r1][c1];
    }

    // -------------------------------------------------------------------------
    // Main query answering method
    // -------------------------------------------------------------------------

    /**
     * Answers all queries using the preprocessed layered prefix sums.
     *
     * <p>For each query [r1, c1, r2, c2, threshold]:
     * 1. Binary search in sortedUniqueValues to find the first index k where
     *    sortedUniqueValues[k] > threshold.
     * 2. If no such k exists, the answer is 0 (no values exceed threshold).
     * 3. Otherwise, the answer is:
     *    totalRectangleSum - sum of layers 0..(k-1) in the rectangle
     *    = cumulativePrefixSum[K-1] rectangle sum - cumulativePrefixSum[k-1] rectangle sum
     *
     * @param grid    the original 2D grid (used only for dimensions here)
     * @param queries array of queries, each [r1, c1, r2, c2, threshold]
     * @return array of long values, one per query
     *
     * Time complexity: O(Q * log K) per query after preprocessing,
     *                  where Q = number of queries, K = unique values count
     * Space complexity: O(Q) for the result array
     */
    public long[] processQueries(int[][] grid, int[][] queries) {
        int Q = queries.length;
        long[] results = new long[Q];
        int K = sortedUniqueValues.length;

        for (int q = 0; q < Q; q++) {
            int r1 = queries[q][0];
            int c1 = queries[q][1];
            int r2 = queries[q][2];
            int c2 = queries[q][3];
            int threshold = queries[q][4];

            // Step 1: Binary search for the first index k where sortedUniqueValues[k] > threshold
            // We want the leftmost position where value > threshold
            // This is equivalent to finding the insertion point of (threshold + 1)
            // using Arrays.binarySearch or manual binary search

            int lo = 0, hi = K; // hi is exclusive
            while (lo < hi) {
                int mid = lo + (hi - lo) / 2;
                if (sortedUniqueValues[mid] <= threshold) {
                    // This value is <= threshold, so move right
                    lo = mid + 1;
                } else {
                    // This value is > threshold, could be our answer, try left
                    hi = mid;
                }
            }
            // After the loop, lo = first index where sortedUniqueValues[lo] > threshold
            int firstValidLayerIndex = lo;

            // Step 2: If no valid layer exists, answer is 0
            if (firstValidLayerIndex >= K) {
                results[q] = 0L;
                continue;
            }

            // Step 3: Compute the sum of all layers from firstValidLayerIndex to K-1
            // in the rectangle [r1,c1,r2,c2]
            //
            // Total sum of all layers in rectangle:
            long totalSum = rectangleSum(cumulativePrefixSum[K - 1], r1, c1, r2, c2);

            // Sum of layers 0 to (firstValidLayerIndex - 1) in rectangle:
            long excludedSum = 0L;
            if (firstValidLayerIndex > 0) {
                excludedSum = rectangleSum(cumulativePrefixSum[firstValidLayerIndex - 1], r1, c1, r2, c2);
            }

            // The answer is the sum of layers firstValidLayerIndex..K-1
            results[q] = totalSum - excludedSum;
        }

        return results;
    }

    // -------------------------------------------------------------------------
    // Convenience wrapper method
    // -------------------------------------------------------------------------

    /**
     * Main entry point for solving the weighted region score queries problem.
     *
     * <p>This method preprocesses the grid and then answers all queries.
     *
     * @param grid    the m x n integer grid of terrain values
     * @param queries list of queries, each [r1, c1, r2, c2, threshold]
     * @return array of long values representing the weighted region score for each query
     *
     * Time complexity: O(m * n * K + Q * log K) where K = unique values, Q = queries
     * Space complexity: O(m * n * K) for the layered prefix sums
     */
    public long[] solve(int[][] grid, int[][] queries) {
        // Step 1: Preprocess the grid to build layered prefix sums
        preprocess(grid);

        // Step 2: Answer all queries using the preprocessed structure
        return processQueries(grid, queries);
    }

    // -------------------------------------------------------------------------
    // Main method for demonstration
    // -------------------------------------------------------------------------

    /**
     * Demonstrates the solution with the provided examples.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        