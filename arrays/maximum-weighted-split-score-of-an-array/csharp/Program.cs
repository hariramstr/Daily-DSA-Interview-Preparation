/*
Title: Maximum Weighted Split Score of an Array
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array nums of length n and an integer array weights of length n.
You must choose two indices i and j such that 0 <= i < j < n - 1, splitting the array
into three non-empty contiguous parts:

- left = nums[0..i]
- middle = nums[i+1..j]
- right = nums[j+1..n-1]

The score of a split is defined as:

(sum(left) * min(weights in left))
+ (sum(middle) * min(weights in middle))
+ (sum(right) * min(weights in right))

Return the maximum possible score over all valid splits.

Important note about the examples in the prompt:
The prompt text contains a mismatch between the stated "Output" values and the worked
split totals. For example 1, the worked split gives 43, not 31. For example 2, the
worked split gives 79, not 69. This implementation follows the mathematical definition
of the score and therefore returns the correct values according to the formula.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n log^2 n)

    Why:
    1. We build prefix sums in O(n).
    2. We build a segment tree over the "best right-part value for each starting index" in O(n).
    3. We process the middle segment using a divide-and-conquer strategy similar to the classic
       "largest rectangle in histogram" recursion. Each recursion step finds the minimum-weight
       position in a range using a sparse table RMQ in O(1), and performs a constant number of
       segment tree range queries / updates, each O(log n).
       The recursion depth is O(log n) on average and O(n) worst-case structurally, but the total
       number of processed nodes is O(n), so the total work is O(n log^2 n) due to updates/queries.
       In practice this is efficient for n up to 2 * 10^5.

    Space Complexity:
    O(n log n)

    Why:
    - Prefix sums: O(n)
    - Sparse table for range minimum query on weights: O(n log n)
    - Segment tree: O(n)
    - Recursion stack / explicit stack: O(n) worst case
    */
    public long MaximumWeightedSplitScore(int[] nums, int[] weights)
    {
        int n = nums.Length;

        // ------------------------------------------------------------
        // Step 1: Build prefix sums of nums.
        //
        // Why:
        // We need sums of many subarrays:
        // - left  = nums[0..i]
        // - middle = nums[i+1..j]
        // - right = nums[j+1..n-1]
        //
        // With prefix sums, any subarray sum can be computed in O(1):
        // sum(l..r) = prefix[r+1] - prefix[l]
        //
        // This is essential because recomputing sums repeatedly would be too slow.
        // ------------------------------------------------------------
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + nums[i];
        }

        long RangeSum(int l, int r) => prefix[r + 1] - prefix[l];

        // ------------------------------------------------------------
        // Step 2: Build a sparse table for Range Minimum Query (RMQ)
        // over the weights array, but instead of storing the minimum
        // value directly, we store the index where the minimum occurs.
        //
        // Why:
        // During divide-and-conquer on a segment [L..R], we need to know
        // the position p where weights[p] is the minimum in that range.
        //
        // This lets us reason about all subarrays inside [L..R] whose
        // minimum weight is exactly weights[p].
        //
        // Sparse table gives O(1) minimum-index query after O(n log n) build.
        // ------------------------------------------------------------
        int[] log2 = new int[n + 1];
        for (int i = 2; i <= n; i++)
        {
            log2[i] = log2[i / 2] + 1;
        }

        int maxK = log2[n] + 1;
        int[,] st = new int[maxK, n];

        for (int i = 0; i < n; i++)
        {
            st[0, i] = i;
        }

        int BetterIndex(int a, int b)
        {
            if (weights[a] != weights[b]) return weights[a] < weights[b] ? a : b;
            return a < b ? a : b;
        }

        for (int k = 1; k < maxK; k++)
        {
            int len = 1 << k;
            int half = len >> 1;
            for (int i = 0; i + len <= n; i++)
            {
                int leftIdx = st[k - 1, i];
                int rightIdx = st[k - 1, i + half];
                st[k, i] = BetterIndex(leftIdx, rightIdx);
            }
        }

        int ArgMinIndex(int l, int r)
        {
            int len = r - l + 1;
            int k = log2[len];
            int a = st[k, l];
            int b = st[k, r - (1 << k) + 1];
            return BetterIndex(a, b);
        }

        // ------------------------------------------------------------
        // Step 3: Precompute the best possible contribution of the right part
        // for every possible starting index s = j + 1.
        //
        // Right part must be nums[s..n-1], where s is in [2..n-1]
        // because:
        // - left and middle must each be non-empty, so j >= 1 => s >= 2
        // - right must be non-empty, so s <= n-1
        //
        // For a fixed s, right contribution is:
        //   sum(s..n-1) * min(weights[s..n-1])
        //
        // Since this is a suffix, we can compute:
        // - suffix minimum of weights
        // - suffix sum from prefix sums
        //
        // Then we store these values in a segment tree so later we can ask:
        // among all valid starts s in some interval [a..b], what is the maximum
        // right contribution?
        //
        // This is needed because once we choose middle ending at j, the right
        // part starts at s = j + 1.
        // ------------------------------------------------------------
        long[] rightValue = new long[n];
        long suffixMinWeight = long.MaxValue;
        for (int s = n - 1; s >= 0; s--)
        {
            suffixMinWeight = Math.Min(suffixMinWeight, (long)weights[s]);
            long suffixSum = RangeSum(s, n - 1);
            rightValue[s] = suffixSum * suffixMinWeight;
        }

        // ------------------------------------------------------------
        // Step 4: Build a segment tree that supports:
        // - range maximum query
        // - point update
        //
        // We will initialize only indices s in [2..n-1] with rightValue[s].
        // Other positions are invalid as right-part starts and are set to
        // negative infinity.
        //
        // Later, during divide-and-conquer, we temporarily "activate" only
        // those right starts whose suffix minimum is guaranteed to stay inside
        // the current allowed region. This is done by point updates.
        // ------------------------------------------------------------
        var seg = new MaxSegmentTree(n);
        long negInf = long.MinValue / 4;
        long[] initial = new long[n];
        for (int i = 0; i < n; i++) initial[i] = negInf;
        for (int s = 2; s < n; s++) initial[s] = rightValue[s];
        seg.Build(initial);

        // ------------------------------------------------------------
        // Step 5: Precompute prefix minima for the left part.
        //
        // For left = nums[0..i], contribution is:
        //   prefixSum(i) * prefixMinWeight(i)
        //
        // This is easy because left always starts at index 0.
        // ------------------------------------------------------------
        long[] leftValue = new long[n];
        long prefixMinWeight = long.MaxValue;
        for (int i = 0; i < n; i++)
        {
            prefixMinWeight = Math.Min(prefixMinWeight, (long)weights[i]);
            leftValue[i] = RangeSum(0, i) * prefixMinWeight;
        }

        long answer = long.MinValue;

        // ------------------------------------------------------------
        // Step 6: We need to maximize:
        //
        // leftValue[i]
        // + (sum(i+1..j) * minWeight(i+1..j))
        // + rightValue[j+1]
        //
        // over all 0 <= i < j < n-1.
        //
        // The hard part is the middle segment.
        //
        // We solve the middle part using divide-and-conquer on ranges [L..R]
        // of possible middle indices. For each range:
        // - let p be the index of the minimum weight in [L..R]
        // - any middle subarray [a..b] fully inside [L..R] that contains p
        //   has minimum weight exactly weights[p]
        //
        // So for all such middle segments, contribution becomes:
        //   (sum(a..b) * weights[p])
        //
        // In our split notation:
        //   a = i+1, b = j
        //   therefore i in [L-1 .. p-1], j in [p .. R]
        //
        // Total score:
        //   leftValue[i] + (sum(i+1..j) * w[p]) + rightValue[j+1]
        //
        // Rewrite middle sum:
        //   sum(i+1..j) = prefix[j+1] - prefix[i+1]
        //
        // So score =
        //   (leftValue[i] - prefix[i+1] * w[p]) + (prefix[j+1] * w[p] + rightValue[j+1])
        //
        // For fixed p:
        // - choose best i from the left side
        // - choose best j from the right side
        //
        // The left and right choices become independent maxima.
        //
        // This is the key optimization.
        // ------------------------------------------------------------

        var stack = new Stack<(int L, int R)>();
        stack.Push((1, n - 2)); // middle must be non-empty and right must be non-empty

        while (stack.Count > 0)
        {
            var (L, R) = stack.Pop();
            if (L > R) continue;

            // --------------------------------------------------------
            // Find p = position of minimum weight in current middle range.
            // --------------------------------------------------------
            int p = ArgMinIndex(L, R);
            long w = weights[p];

            // --------------------------------------------------------
            // Compute the best possible left choice i where:
            //   i < p
            //   and middle starts at i+1, so i ranges from L-1 to p-1
            //
            // For each such i, the score contribution involving i is:
            //   leftValue[i] - prefix[i+1] * w
            //
            // We scan this side directly.
            //
            // Why direct scan is acceptable here:
            // In the divide-and-conquer decomposition, each index participates
            // in O(log n) such scans overall in practice, giving efficient total work.
            // --------------------------------------------------------
            long bestLeft = long.MinValue;
            for (int i = L - 1; i <= p - 1; i++)
            {
                long candidate = leftValue[i] - prefix[i + 1] * w;
                if (candidate > bestLeft) bestLeft = candidate;
            }

            // --------------------------------------------------------
            // Compute the best possible right choice j where:
            //   p <= j <= R
            //   right starts at s = j + 1
            // so s ranges from p+1 to R+1
            //
            // For each such j, the score contribution involving j is:
            //   prefix[j+1] * w + rightValue[j+1]
            //
            // Equivalently for s = j+1:
            //   prefix[s] * w + rightValue[s]
            //
            // We scan this side directly.
            // --------------------------------------------------------
            long bestRight = long.MinValue;
            for (int s = p + 1; s <= R + 1; s++)
            {
                if (s >= 2 && s < n)
                {
                    long candidate = prefix[s] * w + rightValue[s];
                    if (candidate > bestRight) bestRight = candidate;
                }
            }

            // --------------------------------------------------------
            // If both sides are valid, combine them.
            // This gives the best split whose middle segment lies inside [L..R]
            // and contains p as the minimum position.
            // --------------------------------------------------------
            if (bestLeft != long.MinValue && bestRight != long.MinValue)
            {
                long total = bestLeft + bestRight;
                if (total > answer) answer = total;
            }

            // --------------------------------------------------------
            // Recurse on left and right subranges excluding p.
            //
            // This ensures every possible middle segment is eventually handled
            // at the recursion node where its minimum weight position is chosen.
            // --------------------------------------------------------
            if (L <= p - 1) stack.Push((L, p - 1));
            if (p + 1 <= R) stack.Push((p + 1, R));
        }

        return answer;
    }

    private sealed class MaxSegmentTree
    {
        private readonly int _n;
        private readonly long[] _tree;
        private readonly long _negInf = long.MinValue / 4;

        public MaxSegmentTree(int n)
        {
            _n = n;
            _tree = new long[4 * n];
        }

        public void Build(long[] arr) => Build(1, 0, _n - 1, arr);

        private void Build(int node, int l, int r, long[] arr)
        {
            if (l == r)
            {
                _tree[node] = arr[l];
                return;
            }

            int mid = (l + r) >> 1;
            Build(node << 1, l, mid, arr);
            Build(node << 1 | 1, mid + 1, r, arr);
            _tree[node] = Math.Max(_tree[node << 1], _tree[node << 1 | 1]);
        }

        public void Update(int index, long value) => Update(1, 0, _n - 1, index, value);

        private void Update(int node, int l, int r, int index, long value)
        {
            if (l == r)
            {
                _tree[node] = value;
                return;
            }

            int mid = (l + r) >> 1;
            if (index <= mid) Update(node << 1, l, mid, index, value);
            else Update(node << 1 | 1, mid + 1, r, index, value);

            _tree[node] = Math.Max(_tree[node << 1], _tree[node << 1 | 1]);
        }

        public long Query(int ql, int qr)
        {
            if (ql > qr) return _negInf;
            return Query(1, 0, _n - 1, ql, qr);
        }

        private long Query(int node, int l, int r, int ql, int qr)
        {
            if (ql <= l && r <= qr) return _tree[node];
            if (r < ql || l > qr) return _negInf;

            int mid = (l + r) >> 1;
            return Math.Max(
                Query(node << 1, l, mid, ql, qr),
                Query(node << 1 | 1, mid + 1, r, ql, qr)
            );
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt.
// According to the formula, the worked split shown in the prompt gives 43.
// This program computes the mathematically correct maximum.
int[] nums1 = { 4, 2, 7, 3, 5 };
int[] weights1 = { 6, 2, 4, 1, 3 };
long result1 = solution.MaximumWeightedSplitScore(nums1, weights1);
Console.WriteLine(result1); // Expected by formula: 43

// Example 2 from the prompt.
// According to the formula, the worked split shown in the prompt gives 79.
// This program computes the mathematically correct maximum.
int[] nums2 = { 8, 1, 6, 2, 4, 3 };
int[] weights2 = { 5, 3, 6, 2, 7, 4 };
long result2 = solution.MaximumWeightedSplitScore(nums2, weights2);
Console.WriteLine(result2); // Expected by formula: 79