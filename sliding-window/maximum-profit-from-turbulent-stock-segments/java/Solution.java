```java
/*
 * Title: Maximum Profit from Turbulent Stock Segments
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an integer array `prices` representing daily stock prices, and two integers `k` and `threshold`.
 *
 * A subarray is called **turbulent** if the absolute difference between every pair of consecutive elements
 * in the subarray is strictly greater than `threshold`. More formally, a subarray `prices[l..r]` is turbulent
 * if for every index `i` in `[l, r-1]`, `|prices[i+1] - prices[i]| > threshold`.
 *
 * Your goal is to find the **maximum total profit** achievable by selecting at most `k` non-overlapping
 * turbulent subarrays, where the profit of a subarray `prices[l..r]` is defined as
 * max(prices[l..r]) - min(prices[l..r]).
 *
 * Return the maximum total profit. If no turbulent subarray of length at least 2 exists, return 0.
 *
 * Constraints:
 * - 1 <= prices.length <= 10^5
 * - 0 <= prices[i] <= 10^9
 * - 1 <= k <= 100
 * - 0 <= threshold <= 10^9
 */

import java.util.*;

public class Solution {

    /**
     * Finds the maximum total profit from selecting at most k non-overlapping turbulent subarrays.
     *
     * <p>Approach:
     * 1. First, identify all maximal turbulent segments in the prices array.
     * 2. For each maximal turbulent segment, enumerate all valid sub-segments (length >= 2)
     *    and compute their profits.
     * 3. Use dynamic programming to select at most k non-overlapping segments with maximum total profit.
     *
     * <p>Key insight: We decompose the problem into:
     *   a) Finding all candidate turbulent subarrays (represented as [left, right, profit])
     *   b) Selecting at most k non-overlapping ones with maximum total profit using DP
     *      (similar to the "weighted job scheduling" problem)
     *
     * @param prices    the array of daily stock prices
     * @param k         the maximum number of non-overlapping turbulent subarrays to select
     * @param threshold the minimum absolute difference required between consecutive elements
     * @return the maximum total profit achievable
     *
     * Time complexity: O(n^2 * k) in the worst case due to segment enumeration and DP,
     *                  but in practice much faster due to turbulence constraints.
     * Space complexity: O(n^2) for storing candidate segments (worst case), O(n*k) for DP.
     */
    public int maxProfit(int[] prices, int k, int threshold) {
        int n = prices.length;

        // Step 1: Find all maximal turbulent segments.
        // A maximal turbulent segment is a contiguous subarray where every consecutive
        // pair has absolute difference > threshold, and it cannot be extended further.
        List<int[]> maximalSegments = new ArrayList<>(); // each entry: [start, end] (inclusive indices)

        int segStart = 0;
        for (int i = 1; i <= n; i++) {
            // Check if the current position breaks the turbulence condition
            boolean breaksTurbulence = (i == n) || (Math.abs(prices[i] - prices[i - 1]) <= threshold);

            if (breaksTurbulence) {
                // The maximal turbulent segment is prices[segStart..i-1]
                if (i - 1 > segStart) {
                    // Segment has length >= 2, so it's a valid turbulent segment
                    maximalSegments.add(new int[]{segStart, i - 1});
                }
                // Start a new potential segment from position i
                segStart = i;
            }
        }

        // Step 2: For each maximal turbulent segment, enumerate all sub-segments of length >= 2.
        // For a sub-segment prices[l..r], profit = max(prices[l..r]) - min(prices[l..r]).
        // We store each candidate as [left, right, profit].
        List<int[]> candidates = new ArrayList<>();

        for (int[] seg : maximalSegments) {
            int l = seg[0];
            int r = seg[1];

            // Enumerate all sub-segments [sl, sr] within [l, r]
            for (int sl = l; sl < r; sl++) {
                int curMin = prices[sl];
                int curMax = prices[sl];

                for (int sr = sl + 1; sr <= r; sr++) {
                    // Extend the sub-segment to include prices[sr]
                    curMin = Math.min(curMin, prices[sr]);
                    curMax = Math.max(curMax, prices[sr]);

                    int profit = curMax - curMin;
                    // Only add if profit > 0 (i.e., not all same values)
                    if (profit > 0) {
                        candidates.add(new int[]{sl, sr, profit});
                    }
                }
            }
        }

        // If no valid candidates, return 0
        if (candidates.isEmpty()) {
            return 0;
        }

        // Step 3: Sort candidates by their right endpoint (end index).
        // This is needed for the weighted interval scheduling DP.
        candidates.sort((a, b) -> a[1] != b[1] ? a[1] - b[1] : a[0] - b[0]);

        int m = candidates.size();

        // Step 4: Dynamic Programming for weighted interval scheduling with at most k selections.
        // dp[i][j] = maximum profit considering the first i candidates and selecting exactly j of them.
        // We want max over j=0..k of dp[m][j].
        //
        // Transition:
        // For candidate i (0-indexed), we can either:
        //   - Skip it: dp[i+1][j] = max(dp[i+1][j], dp[i][j])
        //   - Take it: find the latest candidate p such that candidates[p].right < candidates[i].left
        //              dp[i+1][j+1] = max(dp[i+1][j+1], dp[p+1][j] + candidates[i].profit)
        //
        // We use 1-indexed candidates for convenience.

        // dp[i][j] = best profit using first i candidates, picking exactly j
        // Use long to avoid overflow with large profits
        long[][] dp = new long[m + 1][k + 1];

        // Initialize all to -infinity except dp[0][0] = 0
        for (long[] row : dp) {
            Arrays.fill(row, Long.MIN_VALUE / 2);
        }
        dp[0][0] = 0;

        // Precompute for each candidate i, the index of the latest candidate j such that
        // candidates[j].right < candidates[i].left (i.e., they don't overlap).
        // We use binary search on the sorted candidates array.
        int[] prev = new int[m]; // prev[i] = index (1-based) of latest non-overlapping candidate before i

        for (int i = 0; i < m; i++) {
            int leftBound = candidates.get(i)[0]; // left index of candidate i

            // Binary search: find the rightmost candidate j (0-indexed) such that candidates[j][1] < leftBound
            int lo = 0, hi = i - 1, best = -1;
            while (lo <= hi) {
                int mid = (lo + hi) / 2;
                if (candidates.get(mid)[1] < leftBound) {
                    best = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            // prev[i] is 1-based index: best+1 (since dp is 1-indexed)
            // If best == -1, no non-overlapping candidate exists before i, so prev[i] = 0
            prev[i] = best + 1; // 1-based: dp[prev[i]][j] is the state after considering prev[i] candidates
        }

        // Fill DP table
        for (int i = 0; i < m; i++) {
            // Option 1: Skip candidate i
            // dp[i+1][j] = max(dp[i+1][j], dp[i][j]) for all j
            for (int j = 0; j <= k; j++) {
                if (dp[i][j] != Long.MIN_VALUE / 2) {
                    dp[i + 1][j] = Math.max(dp[i + 1][j], dp[i][j]);
                }
            }

            // Option 2: Take candidate i
            // We need dp[prev[i]][j] + candidates[i].profit -> dp[i+1][j+1]
            int profitI = candidates.get(i)[2];
            int prevIdx = prev[i]; // 1-based index into dp

            for (int j = 0; j < k; j++) {
                if (dp[prevIdx][j] != Long.MIN_VALUE / 2) {
                    dp[i + 1][j + 1] = Math.max(dp[i + 1][j + 1], dp[prevIdx][j] + profitI);
                }
            }
        }

        // Step 5: Find the maximum profit over all j from 0 to k
        long answer = 0;
        for (int j = 0; j <= k; j++) {
            if (dp[m][j] != Long.MIN_VALUE / 2) {
                answer = Math.max(answer, dp[m][j]);
            }
        }

        return (int) answer;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // prices = [9, 4, 8, 2, 10, 3, 7], k = 2, threshold = 2
        // Expected output: 16
        int[] prices1 = {9, 4, 8, 2, 10, 3, 7};
        int k1 = 2;
        int threshold1 = 2;
        int result1 = solution.maxProfit(prices1, k1, threshold1);
        System.out.println("Example 1:");
        System.out.println("prices = " + Arrays.toString(prices1));
        System.out.println("k = " + k1 + ", threshold = " + threshold1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 16");
        System.out.println();

        // Example 2:
        // prices = [5, 5, 5, 5], k = 1, threshold = 0
        // Expected output: 0
        int[] prices2 = {5, 5, 5, 5};
        int k2 = 1;
        int threshold2 = 0;
        int result2 = solution.maxProfit(prices2, k2, threshold2);
        System.out.println("Example 2:");
        System.out.println("prices = " + Arrays.toString(prices2));
        System.out.println("k = " + k2 + ", threshold = " + threshold2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional test case:
        // prices = [1, 10, 1, 10], k = 1, threshold = 5
        // Turbulent: all consecutive diffs are 9 > 5
        // Best single subarray: [1,10,1,10] -> profit = 10-1 = 9
        int[] prices3 = {1, 10, 1, 10};
        int k3 = 1;
        int threshold3 = 5;
        int result3 = solution.maxProfit(prices3, k3, threshold3);
        System.out.println("Additional Test 1:");
        System.out.println("prices = " + Arrays.toString(prices3));
        System.out.println("k = " + k3 + ", threshold = " + threshold3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 9");
        System.out.println();

        // Additional test case:
        // prices = [1, 10, 1, 10], k = 2, threshold = 5
        // We can pick [1,10] profit=9 and [1,10] profit=9 -> total 18
        // But they must be non-overlapping: [0,1] and [2,3] -> 9+9=18
        int[] prices4 = {1, 10, 1, 10};
        int k4 = 2;
        int threshold4 = 5;
        int result4 = solution.maxProfit(prices4, k4, threshold4);
        System.out.println("Additional Test 2:");
        System.out.println("prices = " + Arrays.toString(prices4));
        System.out.println("k = " + k4 + ", threshold = " + threshold4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 18");
        System.out.println();

        // Additional test case: single element
        // prices = [5], k = 1, threshold = 0
        // No subarray of length >= 2 exists
        int[] prices5 = {5};
        int k5 = 1;
        int threshold5 = 0;
        int result5 = solution.maxProfit(prices5, k5, threshold5);
        System.out.println("Additional Test 3 (single element):");
        System.out.println("prices = " + Arrays.toString(prices5));
        System.out.println("k = " + k5 + ", threshold = " + threshold5);
        System.out.println("Output: " + result5);
        System.out.println("Expected: 0");
        System.out.println();

        // Trace through Example 1 manually:
        // prices = [9, 4, 8, 2, 10, 3, 7], threshold = 2
        // Check consecutive diffs:
        // |4-9|=5>2 ✓, |8-4|=4>2 ✓, |2-8|=6>2 ✓, |10-2|=8>2 ✓, |3-10|=7>2 ✓, |7-3|=4>2 ✓
        // All diffs > 2, so the entire array [0..6] is one maximal turbulent segment.
        //
        // We need to find the best 2 non-overlapping sub-segments.
        // The problem says the answer is 16.
        // Let's verify: what combination gives 16?
        // [9,4,8,2,10,3] (indices 0-5): max=10, min=2, profit=8
        // [7] - only 1 element, not valid
        // [4,8,2,10,3,7] (indices 1-6): max=10, min=2, profit=8
        // [9] + [4,8,2,10,3,7]: 0 + 8 = 8
        // [9,4] (profit=5) + [8,2,10,3,7] (profit=8) = 13
        // [9,4,8] (profit=7) + [2,10,3,7] (profit=8) = 15
        // [9,4,8,2] (profit=7) + [10,3,7] (profit=7) = 14
        // [9,4,8,2,10] (profit=8) + [3,7] (profit=4) = 12
        // [4,8,2,10] (profit=8) + [3