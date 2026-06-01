```java
/*
 * Title: Maximum Sum Rectangle with At Most K Negatives
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * Given a 2D integer matrix `grid` of size m x n and an integer k, find the maximum sum
 * of any non-empty rectangular subarray such that the rectangle contains AT MOST k negative numbers.
 *
 * A rectangular subarray is defined by choosing two rows r1 <= r2 and two columns c1 <= c2,
 * and summing all elements grid[i][j] where r1 <= i <= r2 and c1 <= j <= c2.
 *
 * Return the maximum possible sum satisfying the constraint.
 * It is guaranteed that at least one valid rectangle exists.
 *
 * Constraints:
 * - 1 <= m, n <= 75
 * - -500 <= grid[i][j] <= 500
 * - 0 <= k <= m * n
 *
 * Example 1:
 * Input: grid = [[1, -2, 3], [-1, 4, -1], [2, -3, 5]], k = 1
 * Output: 11
 *
 * Example 2:
 * Input: grid = [[-3, -2], [-1, -4]], k = 2
 * Output: -1
 */

import java.util.*;

/**
 * Solution class for Maximum Sum Rectangle with At Most K Negatives problem.
 *
 * <p>Core Approach:
 * We fix two row boundaries (r1, r2) and compress the 2D problem into a 1D problem
 * by summing columns between those rows. Then for each pair of row boundaries,
 * we find the maximum subarray sum with at most k negatives in the 1D compressed array.
 *
 * <p>For the 1D subproblem, we use prefix sums combined with a sliding window / two-pointer
 * approach that tracks the count of negatives in the current window.
 */
public class Solution {

    /**
     * Finds the maximum sum rectangle in the grid with at most k negative numbers.
     *
     * <p>Algorithm Overview:
     * 1. Fix the top row (r1) and bottom row (r2) — O(m^2) pairs.
     * 2. For each (r1, r2) pair, compress columns into a 1D array by summing values
     *    in each column from row r1 to r2.
     * 3. Also compress a "negative count" array: how many negatives exist in each column
     *    from r1 to r2.
     * 4. Use a two-pointer (sliding window) approach on the 1D arrays to find the
     *    maximum subarray sum with at most k negatives.
     * 5. Track the global maximum across all (r1, r2) pairs.
     *
     * @param grid The 2D integer matrix of size m x n
     * @param k    The maximum number of negative numbers allowed in the rectangle
     * @return The maximum sum of any valid rectangle with at most k negatives
     *
     * Time Complexity:  O(m^2 * n) — m^2 row pairs, each requiring O(n) sliding window pass
     * Space Complexity: O(m * n) for prefix sum arrays, O(n) for compressed 1D arrays
     */
    public int maxSumWithKNegatives(int[][] grid, int k) {
        int m = grid.length;
        int n = grid[0].length;

        // -----------------------------------------------------------------------
        // Step 1: Precompute prefix sums for both values and negative counts.
        //
        // prefixSum[i][j]  = sum of grid[0..i-1][0..j-1]  (1-indexed prefix)
        // prefixNeg[i][j]  = count of negatives in grid[0..i-1][0..j-1]
        //
        // Using 1-indexed prefix arrays makes range queries cleaner:
        //   sum from (r1,c1) to (r2,c2) =
        //     prefixSum[r2+1][c2+1] - prefixSum[r1][c2+1]
        //     - prefixSum[r2+1][c1] + prefixSum[r1][c1]
        // -----------------------------------------------------------------------
        int[][] prefixSum = new int[m + 1][n + 1];
        int[][] prefixNeg = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                // 2D prefix sum using inclusion-exclusion
                prefixSum[i][j] = grid[i - 1][j - 1]
                        + prefixSum[i - 1][j]
                        + prefixSum[i][j - 1]
                        - prefixSum[i - 1][j - 1];

                // 2D prefix count of negatives
                int isNeg = (grid[i - 1][j - 1] < 0) ? 1 : 0;
                prefixNeg[i][j] = isNeg
                        + prefixNeg[i - 1][j]
                        + prefixNeg[i][j - 1]
                        - prefixNeg[i - 1][j - 1];
            }
        }

        // -----------------------------------------------------------------------
        // Step 2: Initialize the answer to the smallest possible value.
        // We'll update it as we find valid rectangles.
        // -----------------------------------------------------------------------
        int maxSum = Integer.MIN_VALUE;

        // -----------------------------------------------------------------------
        // Step 3: Enumerate all pairs of row boundaries (r1, r2).
        // r1 and r2 are 0-indexed row indices.
        // -----------------------------------------------------------------------
        for (int r1 = 0; r1 < m; r1++) {
            for (int r2 = r1; r2 < m; r2++) {

                // ---------------------------------------------------------------
                // Step 4: For this (r1, r2) row band, use a two-pointer approach
                // on columns to find the maximum subarray sum with at most k negatives.
                //
                // We use two pointers: left (c1) and right (c2).
                // We expand c2 to the right, and when the negative count exceeds k,
                // we advance c1 to shrink the window from the left.
                // ---------------------------------------------------------------

                // left pointer for the sliding window
                int left = 0;

                for (int right = 0; right < n; right++) {
                    // -----------------------------------------------------------
                    // Query: how many negatives are in the rectangle
                    //        rows [r1..r2], cols [left..right]?
                    // Using the 2D prefix negative count array (1-indexed):
                    //   negCount = prefixNeg[r2+1][right+1] - prefixNeg[r1][right+1]
                    //            - prefixNeg[r2+1][left]   + prefixNeg[r1][left]
                    // -----------------------------------------------------------
                    int negCount = prefixNeg[r2 + 1][right + 1]
                            - prefixNeg[r1][right + 1]
                            - prefixNeg[r2 + 1][left]
                            + prefixNeg[r1][left];

                    // -----------------------------------------------------------
                    // If the window [left..right] has more than k negatives,
                    // we must shrink from the left until the count is <= k.
                    // -----------------------------------------------------------
                    while (negCount > k && left <= right) {
                        left++;
                        // Recompute negCount for the new window [left..right]
                        negCount = prefixNeg[r2 + 1][right + 1]
                                - prefixNeg[r1][right + 1]
                                - prefixNeg[r2 + 1][left]
                                + prefixNeg[r1][left];
                    }

                    // -----------------------------------------------------------
                    // Now the window [left..right] has at most k negatives.
                    // But we want the MAXIMUM sum subarray within [left..right]
                    // that also has at most k negatives.
                    //
                    // Key insight: The full window [left..right] has at most k negatives.
                    // Any sub-window [c1..right] where c1 >= left also has at most k negatives
                    // (fewer negatives, since we're taking a subset).
                    //
                    // So we want to maximize the sum of [c1..right] for c1 in [left..right].
                    // This is equivalent to: maximize
                    //   rectSum(r1, r2, c1, right) = prefixSum query
                    //
                    // To maximize sum([c1..right]) = totalSum([left..right]) - sum([left..c1-1])
                    // We want to MINIMIZE sum([left..c1-1]) over all c1 in [left..right].
                    //
                    // We iterate c1 from left to right, computing prefix sums and
                    // tracking the minimum prefix sum seen so far.
                    // -----------------------------------------------------------

                    // Find the minimum prefix sum for columns [left..c1-1] as c1 varies
                    // We'll scan all possible left endpoints c1 from left to right
                    int minPrefixSum = 0; // represents "no columns to the left of c1=left"
                    // Actually, let's compute this properly:
                    // rectSum(r1, r2, c1, right) for c1 in [left..right]

                    // The sum of rectangle (r1, r2, c1, right):
                    //   = prefixSum[r2+1][right+1] - prefixSum[r1][right+1]
                    //     - prefixSum[r2+1][c1] + prefixSum[r1][c1]
                    //
                    // To maximize this over c1 in [left..right], we need to minimize:
                    //   prefixSum[r2+1][c1] - prefixSum[r1][c1]
                    // which is the "column prefix sum" for the row band [r1..r2].

                    // Compute the column prefix sum for the row band at each column index
                    // colBandPrefix[c] = sum of column c in rows [r1..r2]
                    //                  = prefixSum[r2+1][c+1] - prefixSum[r1][c+1]
                    //                    - prefixSum[r2+1][c] + prefixSum[r1][c]
                    // But for the prefix approach, we need cumulative sums.

                    // Let bandPrefix(c) = prefixSum[r2+1][c] - prefixSum[r1][c]
                    // Then rectSum(r1, r2, c1, right) = bandPrefix(right+1) - bandPrefix(c1)
                    // Maximize over c1 in [left..right]: minimize bandPrefix(c1)

                    int bandPrefixRight = prefixSum[r2 + 1][right + 1] - prefixSum[r1][right + 1];
                    int minBandPrefix = Integer.MAX_VALUE;

                    for (int c1 = left; c1 <= right; c1++) {
                        int bandPrefixC1 = prefixSum[r2 + 1][c1] - prefixSum[r1][c1];
                        if (bandPrefixC1 < minBandPrefix) {
                            minBandPrefix = bandPrefixC1;
                        }
                    }

                    // The best rectangle ending at column 'right' with left boundary >= left
                    int bestSum = bandPrefixRight - minBandPrefix;
                    if (bestSum > maxSum) {
                        maxSum = bestSum;
                    }
                }
            }
        }

        return maxSum;
    }

    /**
     * Alternative cleaner brute-force solution for verification.
     * Checks all O(m^2 * n^2) rectangles and picks the best valid one.
     *
     * @param grid The 2D integer matrix
     * @param k    Maximum allowed negatives
     * @return Maximum sum with at most k negatives
     *
     * Time Complexity:  O(m^2 * n^2) — enumerate all rectangles
     * Space Complexity: O(m * n) for prefix arrays
     */
    public int maxSumBruteForce(int[][] grid, int k) {
        int m = grid.length;
        int n = grid[0].length;

        // Build 2D prefix sum and prefix negative count arrays
        int[][] prefixSum = new int[m + 1][n + 1];
        int[][] prefixNeg = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                prefixSum[i][j] = grid[i - 1][j - 1]
                        + prefixSum[i - 1][j]
                        + prefixSum[i][j - 1]
                        - prefixSum[i - 1][j - 1];

                int isNeg = (grid[i - 1][j - 1] < 0) ? 1 : 0;
                prefixNeg[i][j] = isNeg
                        + prefixNeg[i - 1][j]
                        + prefixNeg[i][j - 1]
                        - prefixNeg[i - 1][j - 1];
            }
        }

        int maxSum = Integer.MIN_VALUE;

        // Enumerate all rectangles (r1, c1) to (r2, c2)
        for (int r1 = 0; r1 < m; r1++) {
            for (int r2 = r1; r2 < m; r2++) {
                for (int c1 = 0; c1 < n; c1++) {
                    for (int c2 = c1; c2 < n; c2++) {
                        // Count negatives in this rectangle
                        int negCount = prefixNeg[r2 + 1][c2 + 1]
                                - prefixNeg[r1][c2 + 1]
                                - prefixNeg[r2 + 1][c1]
                                + prefixNeg[r1][c1];

                        if (negCount <= k) {
                            // Compute sum of this rectangle
                            int rectSum = prefixSum[r2 + 1][c2 + 1]
                                    - prefixSum[r1][c2 + 1]
                                    - prefixSum[r2 + 1][c1]
                                    + prefixSum[r1][c1];

                            if (rectSum > maxSum) {
                                maxSum = rectSum;
                            }
                        }
                    }
                }
            }
        }

        return maxSum;
    }

    /**
     * Main method to demonstrate and test the solution with sample inputs.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // -----------------------------------------------------------------------
        // Test Case 1:
        // grid = [[1, -2, 3], [-1, 4, -1], [2, -3, 5]], k = 1
        // Expected Output: 11
        //
        // Let's verify: rows [0..2], cols [0..2] full grid sum = 1-2+3-1+4-1+2-3+5 = 8
        // with 4 negatives — invalid for k=1.
        //
        // rows [1..2], cols [0..2]: -1+4-1+2-3+5 = 6, negatives: -1,-1,-3 = 3 — invalid.
        // rows [0..2], col [2]: 3+(-1)+5 = 7, negatives: 1 — valid!