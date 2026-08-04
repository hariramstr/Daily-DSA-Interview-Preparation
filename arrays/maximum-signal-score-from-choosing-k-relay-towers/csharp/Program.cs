/*
Title: Maximum Signal Score from Choosing K Relay Towers

Problem Description:
You are given an integer array heights where heights[i] is the elevation of the i-th relay tower along a straight highway.
You must choose exactly k towers, keeping their original left-to-right order.

If the chosen tower indices are i1 < i2 < ... < ik, then the total signal score is:

score = min(heights[i1], heights[i2]) +
        min(heights[i2], heights[i3]) +
        ...
        min(heights[i(k-1)], heights[ik])

Your task is to return the maximum possible signal score.

This is not the same as choosing a contiguous subarray: you may skip any number of towers between two chosen towers,
but the relative order must remain unchanged.

Constraints:
- 2 <= heights.length <= 200000
- 1 <= heights[i] <= 1000000000
- 2 <= k <= min(heights.length, 200)
- The answer may exceed 32-bit signed integer range
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n * k * log n)

    More specifically:
    - We process the array once from left to right.
    - For each possible chosen-count t from 2 to k, we maintain a Fenwick tree over compressed heights.
    - Each update/query is O(log n), and we do this for each element and each t.
    - Since k <= 200, this is practical.

    Space Complexity:
    O(k * n)

    More specifically:
    - Height compression uses O(n).
    - We keep one Fenwick tree per selection count t = 1..k.
    - Each Fenwick tree stores one value per compressed height rank.
    */
    public long MaxSignalScore(int[] heights, int k)
    {
        int n = heights.Length;

        // ------------------------------------------------------------
        // STEP 1: Coordinate-compress the heights.
        //
        // Why do we need this?
        // Heights can be as large as 1,000,000,000, which is too large to use directly
        // as Fenwick tree indices.
        //
        // Coordinate compression maps each distinct height to a rank in [1..m],
        // preserving order:
        //   smaller height -> smaller rank
        //
        // This lets us query:
        // - all previous heights <= current height
        // - all previous heights > current height
        //
        // efficiently using Fenwick trees.
        // ------------------------------------------------------------
        int[] sorted = new int[n];
        Array.Copy(heights, sorted, n);
        Array.Sort(sorted);

        int m = 0;
        for (int i = 0; i < n; i++)
        {
            if (i == 0 || sorted[i] != sorted[i - 1])
            {
                sorted[m++] = sorted[i];
            }
        }

        int[] ranks = new int[n];
        for (int i = 0; i < n; i++)
        {
            ranks[i] = LowerBound(sorted, m, heights[i]) + 1; // Fenwick trees are 1-based
        }

        // ------------------------------------------------------------
        // STEP 2: Prepare Fenwick trees.
        //
        // We want dynamic programming:
        //
        // dp[t][i] = best score of choosing exactly t towers, with the t-th chosen tower at index i.
        //
        // Transition:
        // dp[t][i] = max over j < i of (dp[t-1][j] + min(heights[j], heights[i]))
        //
        // Split by comparing heights[j] and heights[i]:
        //
        // Case A: heights[j] <= heights[i]
        //   contribution = dp[t-1][j] + heights[j]
        //
        // Case B: heights[j] > heights[i]
        //   contribution = dp[t-1][j] + heights[i]
        //
        // So for each t and current i, we need:
        //
        // 1) max(dp[t-1][j] + heights[j]) over previous j with heights[j] <= heights[i]
        // 2) max(dp[t-1][j]) over previous j with heights[j] > heights[i], then add heights[i]
        //
        // This is exactly why we maintain two Fenwick trees for each t:
        //
        // lessOrEqualTree[t]:
        //   indexed by height rank
        //   stores max(dp[t][j] + heights[j]) at that rank
        //
        // allTree[t]:
        //   indexed by reversed height rank
        //   stores max(dp[t][j]) so we can query suffix maxima for heights > current
        //
        // Important detail:
        // We process indices from left to right.
        // Therefore, when we query trees before updating with current index i,
        // the trees contain only previous indices j < i.
        // That automatically enforces the order constraint.
        // ------------------------------------------------------------
        FenwickMax[] lessOrEqualTrees = new FenwickMax[k + 1];
        FenwickMax[] greaterTrees = new FenwickMax[k + 1];

        for (int t = 1; t <= k; t++)
        {
            lessOrEqualTrees[t] = new FenwickMax(m);
            greaterTrees[t] = new FenwickMax(m);
        }

        const long NEG_INF = long.MinValue / 4;
        long answer = 0;

        // ------------------------------------------------------------
        // STEP 3: Scan towers from left to right.
        //
        // For each tower i, we compute dp values for choosing this tower as:
        // - the 1st chosen tower
        // - the 2nd chosen tower
        // - ...
        // - up to the k-th chosen tower
        //
        // We must be careful with update order:
        // If we compute dp[t] for current i, it must only use dp[t-1] from earlier indices.
        // Therefore:
        // - first compute all dp[t] for current i using existing trees
        // - then update trees with these newly computed values
        //
        // This avoids accidentally using the same index multiple times.
        // ------------------------------------------------------------
        long[] currentDp = new long[k + 1];

        for (int i = 0; i < n; i++)
        {
            int h = heights[i];
            int rank = ranks[i];

            // Initialize all dp states for this index as impossible.
            for (int t = 1; t <= k; t++)
            {
                currentDp[t] = NEG_INF;
            }

            // --------------------------------------------------------
            // Base case:
            // Choosing exactly 1 tower ending at i has score 0,
            // because there are no adjacent pairs yet.
            // --------------------------------------------------------
            currentDp[1] = 0;

            // --------------------------------------------------------
            // Compute transitions for t = 2..k.
            //
            // We cannot choose more than i+1 towers from first i+1 positions,
            // so upper bound is min(k, i+1).
            // --------------------------------------------------------
            int maxT = Math.Min(k, i + 1);

            for (int t = 2; t <= maxT; t++)
            {
                // ----------------------------------------------------
                // Query 1:
                // Best previous j with heights[j] <= h
                //
                // Stored value is dp[t-1][j] + heights[j]
                // Since min(heights[j], h) = heights[j] in this case,
                // this directly gives a candidate score.
                // ----------------------------------------------------
                long bestFromSmallerOrEqual = lessOrEqualTrees[t - 1].Query(rank);

                // ----------------------------------------------------
                // Query 2:
                // Best previous j with heights[j] > h
                //
                // Stored value is dp[t-1][j]
                // Since min(heights[j], h) = h in this case,
                // candidate becomes bestPrevious + h.
                //
                // To query "heights > h" with a Fenwick prefix structure,
                // we store values by reversed rank:
                //
                // reversedRank = m - rank + 1
                //
                // Then all original ranks > rank become a prefix in reversed order.
                // Specifically, count of ranks strictly greater than rank is (m - rank).
                // ----------------------------------------------------
                long bestFromGreater = greaterTrees[t - 1].Query(m - rank);

                long best = NEG_INF;

                if (bestFromSmallerOrEqual != NEG_INF)
                {
                    best = Math.Max(best, bestFromSmallerOrEqual);
                }

                if (bestFromGreater != NEG_INF)
                {
                    best = Math.Max(best, bestFromGreater + h);
                }

                currentDp[t] = best;
            }

            // --------------------------------------------------------
            // STEP 4: After all dp values for current index are computed,
            // update the Fenwick trees so future indices can use them.
            //
            // For each t:
            // - lessOrEqualTrees[t] receives dp[t][i] + heights[i] at rank(height[i])
            // - greaterTrees[t] receives dp[t][i] at reversedRank(height[i])
            //
            // Why these exact stored values?
            //
            // lessOrEqualTrees:
            //   used when future currentHeight >= heights[i]
            //   then min(heights[i], futureHeight) = heights[i]
            //   so we need dp + heights[i]
            //
            // greaterTrees:
            //   used when future currentHeight < heights[i]
            //   then min(heights[i], futureHeight) = futureHeight
            //   so we only need dp, and futureHeight is added at query time
            // --------------------------------------------------------
            for (int t = 1; t <= maxT; t++)
            {
                if (currentDp[t] == NEG_INF)
                {
                    continue;
                }

                lessOrEqualTrees[t].Update(rank, currentDp[t] + h);

                int reversedRank = m - rank + 1;
                greaterTrees[t].Update(reversedRank, currentDp[t]);

                if (t == k)
                {
                    answer = Math.Max(answer, currentDp[t]);
                }
            }
        }

        return answer;
    }

    // Standard lower_bound:
    // returns first index in arr[0..length) where arr[index] >= target
    private int LowerBound(int[] arr, int length, int target)
    {
        int left = 0;
        int right = length;

        while (left < right)
        {
            int mid = left + ((right - left) >> 1);
            if (arr[mid] < target)
            {
                left = mid + 1;
            }
            else
            {
                right = mid;
            }
        }

        return left;
    }

    // ------------------------------------------------------------
    // Fenwick tree (Binary Indexed Tree) for prefix maximum queries.
    //
    // Supported operations:
    // - Update(index, value): tree[index] = max(tree[index], value)
    // - Query(index): maximum value in range [1..index]
    //
    // Why Fenwick tree?
    // We need many online prefix-maximum operations while scanning left-to-right.
    // Fenwick tree gives O(log n) per operation and is simpler than a segment tree.
    // ------------------------------------------------------------
    private class FenwickMax
    {
        private readonly long[] tree;
        private const long NEG_INF = long.MinValue / 4;

        public FenwickMax(int size)
        {
            tree = new long[size + 2];
            for (int i = 0; i < tree.Length; i++)
            {
                tree[i] = NEG_INF;
            }
        }

        public void Update(int index, long value)
        {
            int n = tree.Length - 1;
            while (index <= n)
            {
                if (value > tree[index])
                {
                    tree[index] = value;
                }
                index += index & -index;
            }
        }

        public long Query(int index)
        {
            long result = NEG_INF;
            while (index > 0)
            {
                if (tree[index] > result)
                {
                    result = tree[index];
                }
                index -= index & -index;
            }
            return result;
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] heights1 = { 5, 1, 4, 6, 3 };
int k1 = 3;
long result1 = solution.MaxSignalScore(heights1, k1);
Console.WriteLine(result1); // Expected: 8

// Example 2
int[] heights2 = { 2, 7, 3, 9, 5, 8 };
int k2 = 4;
long result2 = solution.MaxSignalScore(heights2, k2);
Console.WriteLine(result2); // Expected: 17

// Additional quick sanity check
int[] heights3 = { 1, 2, 3, 4 };
int k3 = 2;
long result3 = solution.MaxSignalScore(heights3, k3);
Console.WriteLine(result3); // Best is choose 3 and 4 => min(3,4)=3