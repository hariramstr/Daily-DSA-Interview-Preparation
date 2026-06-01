/*
 * Maximum Sum Rectangle with At Most K Negatives
 * ===============================================
 * Given a 2D integer matrix `grid` of size m x n and an integer k,
 * find the maximum sum of any non-empty rectangular subarray such that
 * the rectangle contains AT MOST k negative numbers.
 *
 * A rectangular subarray is defined by choosing two rows r1 <= r2 and
 * two columns c1 <= c2, and summing all elements grid[i][j] where
 * r1 <= i <= r2 and c1 <= j <= c2.
 *
 * Return the maximum possible sum satisfying the constraint.
 * It is guaranteed that at least one valid rectangle exists.
 *
 * Constraints:
 *   1 <= m, n <= 75
 *   -500 <= grid[i][j] <= 500
 *   0 <= k <= m * n
 *
 * Approach:
 *   We fix the top row (r1) and bottom row (r2), then compress the 2D
 *   problem into a 1D problem by summing columns between r1 and r2.
 *   For each pair (r1, r2), we have a 1D array of column sums and a
 *   1D array of negative counts per column. We then use a sliding-window
 *   or prefix-sum approach to find the maximum subarray sum with at most
 *   k negatives in O(n) or O(n log n) time.
 */

using System;
using System.Collections.Generic;

/// <summary>
/// Solution class encapsulating the algorithm for Maximum Sum Rectangle
/// with At Most K Negatives.
/// </summary>
class Solution
{
    /// <summary>
    /// Finds the maximum sum rectangle in the grid with at most k negative numbers.
    ///
    /// Time Complexity:  O(m^2 * n)  — we iterate over all O(m^2) row pairs,
    ///                   and for each pair we do an O(n) sliding-window scan.
    ///                   Overall: O(m^2 * n).
    ///
    /// Space Complexity: O(m * n) for prefix sums and negative count arrays,
    ///                   plus O(n) for the 1D working arrays.
    /// </summary>
    public int MaxSumWithKNegatives(int[][] grid, int k)
    {
        int m = grid.Length;       // number of rows
        int n = grid[0].Length;    // number of columns

        // ---------------------------------------------------------------
        // STEP 1: Build 2D prefix sums for both VALUES and NEGATIVE COUNTS
        // ---------------------------------------------------------------
        // prefixSum[i][j]  = sum of all elements in the rectangle
        //                    from (0,0) to (i-1, j-1)  (1-indexed borders)
        // prefixNeg[i][j]  = count of negative numbers in that same rectangle
        //
        // Using 1-indexed prefix arrays avoids boundary checks (row 0 / col 0
        // act as zero-padding).
        //
        // The standard 2D prefix sum formula:
        //   P[i][j] = grid[i-1][j-1]
        //           + P[i-1][j] + P[i][j-1] - P[i-1][j-1]
        //
        // This lets us query any rectangle sum in O(1):
        //   sum(r1,c1,r2,c2) = P[r2+1][c2+1] - P[r1][c2+1]
        //                    - P[r2+1][c1]   + P[r1][c1]

        int[][] prefixSum = new int[m + 1][];
        int[][] prefixNeg = new int[m + 1][];
        for (int i = 0; i <= m; i++)
        {
            prefixSum[i] = new int[n + 1];
            prefixNeg[i] = new int[n + 1];
        }

        for (int i = 1; i <= m; i++)
        {
            for (int j = 1; j <= n; j++)
            {
                int val = grid[i - 1][j - 1];

                // Inclusion-exclusion for 2D prefix sum
                prefixSum[i][j] = val
                                 + prefixSum[i - 1][j]
                                 + prefixSum[i][j - 1]
                                 - prefixSum[i - 1][j - 1];

                // Same inclusion-exclusion for negative count
                prefixNeg[i][j] = (val < 0 ? 1 : 0)
                                 + prefixNeg[i - 1][j]
                                 + prefixNeg[i][j - 1]
                                 - prefixNeg[i - 1][j - 1];
            }
        }

        // ---------------------------------------------------------------
        // STEP 2: Iterate over all pairs of rows (r1, r2)
        // ---------------------------------------------------------------
        // By fixing the top row r1 and bottom row r2, we reduce the 2D
        // problem to a 1D problem:
        //   colSum[j]  = sum of column j from row r1 to r2
        //   colNeg[j]  = number of negatives in column j from row r1 to r2
        //
        // We want to find the maximum subarray sum over columns c1..c2
        // such that the total negatives in that column range <= k.
        //
        // We use a TWO-POINTER / SLIDING WINDOW approach on columns:
        //   - Maintain a window [left, right] of columns.
        //   - Expand right; if negatives exceed k, shrink left.
        //   - Within the valid window, find the maximum subarray sum
        //     using a variant of Kadane's algorithm restricted to the window.
        //
        // Actually, the cleanest O(n) approach for each row pair is:
        //   Use two pointers to maintain a window where negatives <= k,
        //   and inside that window track the best subarray sum with
        //   a running Kadane's that resets when the left pointer moves.
        //
        // We implement a clean O(n) sliding window + Kadane hybrid below.

        int answer = int.MinValue; // will hold our final answer

        // r1 and r2 are 1-indexed to match our prefix arrays
        for (int r1 = 1; r1 <= m; r1++)
        {
            for (int r2 = r1; r2 <= m; r2++)
            {
                // -----------------------------------------------------------
                // STEP 3: Build the 1D column-sum and column-neg arrays
                //         for the current row band [r1, r2].
                // -----------------------------------------------------------
                // colSum[j] = sum of grid values in column j (0-indexed)
                //             from row r1-1 to row r2-1 (0-indexed grid).
                // colNeg[j] = count of negatives in that column segment.
                //
                // Using prefix arrays, this is O(1) per column:
                //   colSum[j] = prefixSum[r2][j+1] - prefixSum[r1-1][j+1]
                //               - prefixSum[r2][j]  + prefixSum[r1-1][j]
                // But since we only need a single column (width 1):
                //   colSum[j] = prefixSum[r2][j+1] - prefixSum[r1-1][j+1]
                //             - prefixSum[r2][j]   + prefixSum[r1-1][j]
                // Simplifies to the column strip sum.

                int[] colSum = new int[n];
                int[] colNeg = new int[n];

                for (int j = 0; j < n; j++)
                {
                    // Rectangle from (r1,j+1) to (r2,j+1) in 1-indexed prefix
                    // = single column j (0-indexed) from row r1-1 to r2-1
                    colSum[j] = prefixSum[r2][j + 1] - prefixSum[r1 - 1][j + 1]
                              - prefixSum[r2][j]     + prefixSum[r1 - 1][j];

                    colNeg[j] = prefixNeg[r2][j + 1] - prefixNeg[r1 - 1][j + 1]
                              - prefixNeg[r2][j]     + prefixNeg[r1 - 1][j];
                }

                // -----------------------------------------------------------
                // STEP 4: Sliding window + Kadane's to find max subarray sum
                //         with at most k negatives in the 1D array.
                // -----------------------------------------------------------
                // We use two pointers: `left` and `right` both moving over columns.
                // `negCount` tracks negatives in the current window [left, right].
                // `currentSum` is the Kadane running sum starting from `left`.
                //
                // Key insight for Kadane inside a sliding window:
                //   When we must advance `left` (because negatives > k),
                //   we subtract colSum[left] from currentSum and also check
                //   if currentSum becomes negative — if so, reset to 0 and
                //   update left to right+1 (start fresh from next position).
                //
                // However, a subtle issue: standard Kadane resets when sum < 0,
                // but here we also reset when negatives exceed k. We need to
                // handle both constraints simultaneously.
                //
                // Clean implementation:
                //   - `left` is the start of the current subarray candidate.
                //   - `currentSum` = sum of colSum[left..right].
                //   - `negCount`   = negatives in colSum[left..right].
                //   - When negCount > k, advance left until negCount <= k,
                //     subtracting from currentSum.
                //   - After ensuring negCount <= k, also apply Kadane's reset:
                //     if currentSum < 0, reset currentSum = 0 and left = right+1.
                //   - Update answer with currentSum (only if right >= left, i.e.,
                //     we have a non-empty subarray).

                int left = 0;
                int currentSum = 0;
                int negCount = 0;

                for (int right = 0; right < n; right++)
                {
                    // Expand window to include column `right`
                    currentSum += colSum[right];
                    negCount   += colNeg[right];

                    // Shrink from the left while we have too many negatives
                    while (negCount > k && left <= right)
                    {
                        currentSum -= colSum[left];
                        negCount   -= colNeg[left];
                        left++;
                    }

                    // At this point negCount <= k (constraint satisfied).
                    // Apply Kadane's reset: if the running sum is negative,
                    // it's better to start fresh from the next column.
                    if (currentSum < 0)
                    {
                        currentSum = 0;
                        left = right + 1; // next iteration starts a new subarray
                        negCount = 0;     // reset negative count too
                    }
                    else
                    {
                        // We have a valid non-empty subarray with sum = currentSum
                        // (it spans at least column `right` since currentSum >= 0
                        //  and we haven't reset yet — but we must ensure left <= right)
                        if (left <= right || currentSum > 0)
                        {
                            answer = Math.Max(answer, currentSum);
                        }
                    }
                }

                // -----------------------------------------------------------
                // STEP 5: Handle the case where ALL column sums are negative
                //         but we still need to pick at least one element.
                // -----------------------------------------------------------
                // The Kadane reset above might skip valid (negative) answers
                // when every possible subarray has a negative sum but we're
                // forced to pick something (e.g., Example 2).
                //
                // To handle this, we do a separate O(n^2) scan for the best
                // single-element or multi-element subarray when all sums are
                // negative, using the prefix approach for correctness.
                //
                // Actually, let's do a complete O(n^2) scan for each row pair
                // using prefix sums on colSum — this is O(m^2 * n^2) worst case
                // but m,n <= 75 so 75^4 / something... that's too slow.
                //
                // Better: do a separate O(n) pass to find the maximum single
                // column value (with negCount <= k) as a fallback.
                //
                // We'll track the best single-column value here:
                for (int j = 0; j < n; j++)
                {
                    if (colNeg[j] <= k)
                    {
                        answer = Math.Max(answer, colSum[j]);
                    }
                }
            }
        }

        return answer;
    }

    /// <summary>
    /// Alternative cleaner implementation using pure O(n^2) inner loop
    /// with O(1) rectangle queries via prefix sums.
    /// Time:  O(m^2 * n^2)  — acceptable for m,n <= 75 (75^4 ≈ 31M ops)
    /// Space: O(m * n)
    ///
    /// This is the CORRECT reference implementation we use to verify.
    /// </summary>
    public int MaxSumWithKNegativesBrute(int[][] grid, int k)
    {
        int m = grid.Length;
        int n = grid[0].Length;

        // Build 2D prefix sums (1-indexed)
        int[][] ps  = new int[m + 1][];
        int[][] pn  = new int[m + 1][];
        for (int i = 0; i <= m; i++)
        {
            ps[i] = new int[n + 1];
            pn[i] = new int[n + 1];
        }
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
            {
                int v = grid[i - 1][j - 1];
                ps[i][j] = v + ps[i-1][j] + ps[i][j-1] - ps[i-1][j-1];
                pn[i][j] = (v < 0 ? 1 : 0) + pn[i-1][j] + pn[i][j-1] - pn[i-1][j-1];
            }

        int ans = int.MinValue;

        // Try every rectangle (r1,c1) to (r2,c2) in 1-indexed prefix coords
        for (int r1 = 1; r1 <= m; r1++)
        for (int r2 = r1; r2 <= m; r2++)
        for (int c1 = 1; c1 <= n; c1++)
        for (int c2 = c1; c2 <= n; c2++)
        {
            // O(1) rectangle query
            int negatives = pn[r2][c2] - pn[r1-1][c2] - pn[r2][c1-1] + pn[r1-1][c1-1];
            if (negatives <= k)
            {
                int sum = ps[r2][c2] - ps[r1-1][c2] - ps[r2][c1-1] + ps[r1-1][c1-1];
                ans = Math.Max(ans, sum);
            }
        }

        return ans;
    }
}

// =============================================================================
// DEMO / TEST CODE
// =============================================================================

var sol = new Solution();

// -----------------------------------------------------------------------
// Example 1
// grid = [[1, -2, 3], [-1, 4, -1], [2, -3, 5]], k = 1
// Expected output: 11
//