/*
Title: Maximum Score from Choosing a Guarded Middle Segment

Problem Description:
You are given an integer array nums of length n and two non-negative integers L and R, where 0 <= L, R < n.
You must choose exactly one non-empty contiguous subarray nums[i..j] as your active segment.
The segment is considered valid only if there are at least L elements strictly to its left and at least R elements strictly to its right.
In other words, the chosen segment must satisfy:
    i >= L
    j <= n - 1 - R

The score of a valid segment is defined as:
    (minimum value inside the segment) * (sum of all values inside the segment)

Your task is to return the maximum possible score among all valid segments.

Important notes:
- Negative numbers are allowed.
- The answer may exceed 32-bit integer range, so 64-bit arithmetic is required.
- n can be as large as 2 * 10^5, so an O(n^2) solution is too slow.

High-level idea of the efficient solution:
1. For every index k, treat nums[k] as the minimum of the chosen segment.
2. Using a monotonic stack, compute the maximal interval [leftBound[k], rightBound[k]]
   in which nums[k] is the minimum.
3. Inside that interval, we must choose a valid subarray [i..j] that:
   - contains k
   - respects the global guard constraints
   - maximizes the segment sum if nums[k] >= 0
   - minimizes the segment sum if nums[k] < 0
   because score = nums[k] * sum(segment)
4. This becomes a prefix-sum optimization problem:
   sum(i..j) = pref[j+1] - pref[i]
   with constrained ranges for i and j.
5. We solve those range max/min prefix queries offline using a segment tree over compressed prefix values.

Tie-handling for equal values:
- We use:
    previous strictly smaller element
    next smaller-or-equal element
  This assigns each subarray to exactly one pivot index k and avoids double counting.

This implementation is written to be beginner-friendly and heavily commented.
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    /*
    Time Complexity:
    - Building prefix sums: O(n)
    - Monotonic stack boundaries: O(n)
    - Coordinate compression: O(n log n)
    - Offline processing with segment tree: O(n log n)
    Overall: O(n log n)

    Space Complexity:
    - Prefix sums, boundaries, helper arrays, events, segment tree: O(n)

    The method returns the maximum score among all valid segments.
    */
    public long MaximumScore(int[] nums, int L, int R)
    {
        int n = nums.Length;

        // ------------------------------------------------------------
        // Step 1: Determine the globally allowed start and end indices.
        //
        // A valid segment [i..j] must satisfy:
        //   i >= L
        //   j <= n - 1 - R
        //
        // Let:
        //   startMin = L
        //   endMax   = n - 1 - R
        //
        // Since the problem guarantees at least one valid segment,
        // we know startMin <= endMax.
        // ------------------------------------------------------------
        int startMin = L;
        int endMax = n - 1 - R;

        // ------------------------------------------------------------
        // Step 2: Build prefix sums.
        //
        // pref[t] = sum of nums[0..t-1]
        // Therefore:
        //   sum(i..j) = pref[j + 1] - pref[i]
        //
        // We use long because values and sums can be large.
        // ------------------------------------------------------------
        long[] pref = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            pref[i + 1] = pref[i] + nums[i];
        }

        // ------------------------------------------------------------
        // Step 3: For each index k, compute the maximal interval where
        // nums[k] is the designated minimum.
        //
        // We compute:
        //   leftLess[k]  = index of previous element strictly smaller than nums[k]
        //   rightLessEq[k] = index of next element smaller than or equal to nums[k]
        //
        // Then nums[k] is the minimum for any subarray [i..j] such that:
        //   leftLess[k] < i <= k <= j < rightLessEq[k]
        //
        // Equivalently:
        //   i in [leftLess[k] + 1, k]
        //   j in [k, rightLessEq[k] - 1]
        //
        // Why this exact tie rule?
        // - It ensures every subarray is assigned to exactly one pivot k.
        // - This is a standard trick when handling equal values.
        // ------------------------------------------------------------
        int[] leftLess = new int[n];
        int[] rightLessEq = new int[n];

        var stack = new Stack<int>();

        // Previous strictly smaller
        for (int i = 0; i < n; i++)
        {
            while (stack.Count > 0 && nums[stack.Peek()] >= nums[i])
            {
                stack.Pop();
            }

            leftLess[i] = stack.Count == 0 ? -1 : stack.Peek();
            stack.Push(i);
        }

        stack.Clear();

        // Next smaller-or-equal
        for (int i = n - 1; i >= 0; i--)
        {
            while (stack.Count > 0 && nums[stack.Peek()] > nums[i])
            {
                stack.Pop();
            }

            rightLessEq[i] = stack.Count == 0 ? n : stack.Peek();
            stack.Push(i);
        }

        // ------------------------------------------------------------
        // Step 4: For each pivot k, translate all constraints into
        // allowed ranges for prefix indices.
        //
        // We want a valid subarray [i..j] such that:
        //   - it contains k
        //   - nums[k] is the designated minimum
        //   - it satisfies the guard constraints
        //
        // Start index i must satisfy:
        //   i >= leftLess[k] + 1   (to keep nums[k] minimum)
        //   i <= k                 (must contain k)
        //   i >= startMin          (guard constraint)
        //
        // So:
        //   iLow  = max(leftLess[k] + 1, startMin)
        //   iHigh = k
        //
        // End index j must satisfy:
        //   j >= k                 (must contain k)
        //   j <= rightLessEq[k] - 1 (to keep nums[k] minimum)
        //   j <= endMax            (guard constraint)
        //
        // So:
        //   jLow  = k
        //   jHigh = min(rightLessEq[k] - 1, endMax)
        //
        // In prefix-sum form:
        //   sum(i..j) = pref[j+1] - pref[i]
        //
        // Let:
        //   x = i        -> x in [iLow, iHigh]
        //   y = j + 1    -> y in [jLow + 1, jHigh + 1]
        //
        // Then:
        //   sum = pref[y] - pref[x]
        //
        // If nums[k] >= 0, we want the maximum possible sum.
        //   maximize pref[y] - pref[x]
        //   = (max pref[y]) - (min pref[x])
        //
        // If nums[k] < 0, we want the minimum possible sum,
        // because negative * more negative = larger positive.
        //   minimize pref[y] - pref[x]
        //   = (min pref[y]) - (max pref[x])
        //
        // So each pivot k needs:
        //   over one range of prefix indices x: min or max pref[x]
        //   over another range of prefix indices y: min or max pref[y]
        //
        // This is now a range query problem over prefix values.
        // ------------------------------------------------------------

        // ------------------------------------------------------------
        // Step 5: Coordinate compression of prefix sums.
        //
        // Our segment tree will be indexed by prefix value rank, not by
        // original prefix index. This lets us query:
        //   among active prefix indices in some index range,
        //   what is the minimum / maximum prefix value?
        //
        // Since prefix sums are long and can be large, compression maps
        // each distinct prefix sum to a compact integer [0..m-1].
        // ------------------------------------------------------------
        long[] allPrefValues = new long[n + 1];
        Array.Copy(pref, allPrefValues, n + 1);
        Array.Sort(allPrefValues);

        int m = 0;
        for (int i = 0; i < allPrefValues.Length; i++)
        {
            if (i == 0 || allPrefValues[i] != allPrefValues[i - 1])
            {
                allPrefValues[m++] = allPrefValues[i];
            }
        }

        long[] compressedValues = new long[m];
        Array.Copy(allPrefValues, compressedValues, m);

        int[] prefRank = new int[n + 1];
        for (int i = 0; i <= n; i++)
        {
            prefRank[i] = LowerBound(compressedValues, pref[i]);
        }

        // ------------------------------------------------------------
        // Step 6: Prepare offline queries.
        //
        // We need four kinds of range queries over prefix indices:
        //
        // For x-range [iLow..iHigh]:
        //   - minimum pref[x]
        //   - maximum pref[x]
        //
        // For y-range [jLow+1..jHigh+1]:
        //   - minimum pref[y]
        //   - maximum pref[y]
        //
        // We solve all these with the same offline engine:
        //
        // Given many queries [l..r], compute:
        //   - min prefix value in that index range
        //   - max prefix value in that index range
        //
        // We process queries grouped by right endpoint r.
        // As we sweep r from left to right, we "activate" prefix index r.
        // The segment tree stores, for each prefix value rank, the latest
        // active index where that value appears.
        //
        // Then for a query [l..r]:
        //   - a prefix value is present in the range iff its latest active
        //     index is >= l
        //   - to find the minimum value present, search the leftmost rank
        //     whose stored latest index >= l
        //   - to find the maximum value present, search the rightmost rank
        //     whose stored latest index >= l
        //
        // This is a neat offline trick:
        //   "latest occurrence >= l" means "there exists an occurrence in [l..r]"
        // ------------------------------------------------------------

        int[] xL = new int[n];
        int[] xR = new int[n];
        int[] yL = new int[n];
        int[] yR = new int[n];
        bool[] feasible = new bool[n];

        for (int k = 0; k < n; k++)
        {
            int iLow = Math.Max(leftLess[k] + 1, startMin);
            int iHigh = k;

            int jLow = k;
            int jHigh = Math.Min(rightLessEq[k] - 1, endMax);

            int yyLow = jLow + 1;
            int yyHigh = jHigh + 1;

            xL[k] = iLow;
            xR[k] = iHigh;
            yL[k] = yyLow;
            yR[k] = yyHigh;

            feasible[k] = (iLow <= iHigh) && (yyLow <= yyHigh);
        }

        long[] minX = new long[n];
        long[] maxX = new long[n];
        long[] minY = new long[n];
        long[] maxY = new long[n];

        FillRangeMinMax(pref, prefRank, compressedValues, xL, xR, feasible, minX, maxX);
        FillRangeMinMax(pref, prefRank, compressedValues, yL, yR, feasible, minY, maxY);

        // ------------------------------------------------------------
        // Step 7: Evaluate the best score for each pivot k.
        //
        // If nums[k] >= 0:
        //   bestSum = maxY[k] - minX[k]
        //
        // If nums[k] < 0:
        //   bestSum = minY[k] - maxX[k]
        //
        // score = nums[k] * bestSum
        //
        // Take the maximum over all feasible pivots.
        // ------------------------------------------------------------
        long answer = long.MinValue;

        for (int k = 0; k < n; k++)
        {
            if (!feasible[k]) continue;

            long bestSum;
            if (nums[k] >= 0)
            {
                bestSum = maxY[k] - minX[k];
            }
            else
            {
                bestSum = minY[k] - maxX[k];
            }

            long score = (long)nums[k] * bestSum;
            if (score > answer) answer = score;
        }

        return answer;
    }

    // ------------------------------------------------------------
    // This helper computes, for each query range [L[i]..R[i]]:
    //   outMin[i] = minimum prefix value in that index range
    //   outMax[i] = maximum prefix value in that index range
    //
    // It uses the offline "latest occurrence by value rank" trick.
    // ------------------------------------------------------------
    private void FillRangeMinMax(
        long[] pref,
        int[] prefRank,
        long[] compressedValues,
        int[] L,
        int[] R,
        bool[] feasible,
        long[] outMin,
        long[] outMax)
    {
        int count = L.Length;
        int prefixCount = pref.Length;
        int m = compressedValues.Length;

        var queriesByRight = new List<int>[prefixCount];
        for (int i = 0; i < prefixCount; i++)
        {
            queriesByRight[i] = new List<int>();
        }

        for (int i = 0; i < count; i++)
        {
            if (feasible[i])
            {
                queriesByRight[R[i]].Add(i);
            }
        }

        var seg = new MaxSegmentTree(m);

        // Sweep right endpoint from left to right.
        for (int r = 0; r < prefixCount; r++)
        {
            // Activate prefix index r.
            // For its value rank, store the latest active index.
            seg.Update(prefRank[r], r);

            // Answer all queries whose right endpoint is exactly r.
            foreach (int q in queriesByRight[r])
            {
                int left = L[q];

                // Find the smallest prefix value rank that appears in [left..r].
                int minRank = seg.FindFirstAtLeast(left);
                // Find the largest prefix value rank that appears in [left..r].
                int maxRank = seg.FindLastAtLeast(left);

                outMin[q] = compressedValues[minRank];
                outMax[q] = compressedValues[maxRank];
            }
        }
    }

    private int LowerBound(long[] arr, long target)
    {
        int lo = 0;
        int hi = arr.Length;
        while (lo < hi)
        {
            int mid = lo + ((hi - lo) >> 1);
            if (arr[mid] < target) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    // ------------------------------------------------------------
    // Segment tree storing, for each prefix-value rank:
    //   the latest active prefix index having that value.
    //
    // Tree node value = maximum latest index in that node's range.
    //
    // This supports:
    //   Update(rank, index)
    //   FindFirstAtLeast(leftBound)
    //   FindLastAtLeast(leftBound)
    //
    // Meaning:
    //   Find the smallest / largest rank whose stored latest index >= leftBound.
    // ------------------------------------------------------------
    private class MaxSegmentTree
    {
        private readonly int size;
        private readonly int[] tree;

        public MaxSegmentTree(int n)
        {
            size = 1;
            while (size < n) size <<= 1;

            tree = new int[size << 1];
            Array.Fill(tree, -1);
        }

        public void Update(int pos, int value)
        {
            int idx = pos + size;
            tree[idx] = value;
            idx >>= 1;

            while (idx > 0)
            {
                tree[idx] = Math.Max(tree[idx << 1], tree[(idx << 1) | 1]);
                idx >>= 1;
            }
        }

        public int FindFirstAtLeast(int threshold)
        {
            if (tree[1] < threshold) return -1;
            int idx = 1;
            int left = 0;
            int right = size - 1;

            while (left != right)
            {
                int mid = left + ((right - left) >> 1);
                int leftChild = idx << 1;

                if (tree[leftChild] >= threshold)
                {
                    idx = leftChild;
                    right = mid;
                }
                else
                {
                    idx = leftChild | 1;
                    left = mid + 1;
                }
            }

            return left;
        }

        public int FindLastAtLeast(int threshold)
        {
            if (tree[1] < threshold) return -1;
            int idx = 1;
            int left = 0;
            int right = size - 1;

            while (left != right)
            {
                int mid = left + ((right - left) >> 1);
                int rightChild = (idx << 1) | 1;

                if (tree[rightChild] >= threshold)
                {
                    idx = rightChild;
                    left = mid + 1;
                }
                else
                {
                    idx = idx << 1;
                    right = mid;
                }
            }

            return left;
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt text contains contradictory explanation.
// According to the formal validity rule:
// nums = [5,2,4,3,6], L = 1, R = 1
// valid starts: i >= 1
// valid ends:   j <= 3
// So the best valid segment is [2,4,3] with score 2 * 9 = 18.
int[] nums1 = { 5, 2, 4, 3, 6 };
int L1 = 1, R1 = 1;
long result1 = solution.MaximumScore(nums1, L1, R1);
Console.WriteLine(result1); // Expected by formal rule: 18

// Example 2
// Best segment is [5], score = 5 * 