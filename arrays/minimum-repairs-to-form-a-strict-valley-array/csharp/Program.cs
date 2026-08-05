/*
Title: Minimum Repairs to Form a Strict Valley Array

Problem Description:
You are given an integer array nums of length n. A strict valley array is an array for which there exists an index p, where 0 < p < n - 1, such that values strictly decrease from the left up to p and then strictly increase after p. In other words:

nums[0] > nums[1] > ... > nums[p] < nums[p+1] < ... < nums[n-1]

The index p is called the valley position.

In one repair operation, you may change any single element to any integer value. Your task is to return the minimum number of repair operations needed to transform nums into a strict valley array.

You are not asked to construct the final array, only to compute the minimum number of elements that must be modified.

A position can remain unchanged only if its original value is compatible with some valid strict valley configuration. Because changed values may be set arbitrarily, the main challenge is to keep the largest possible set of original elements while preserving order and strict inequalities around one valley position.

Constraints:
- 3 <= n <= 200000
- -10^9 <= nums[i] <= 10^9
- The answer must be computed in O(n log n) time or better.

Examples:
1) nums = [9, 7, 5, 6, 8]
   Output: 0

2) nums = [4, 4, 3, 2, 5, 5]
   Output: 2
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
    O(n log n)

    Space Complexity:
    O(n)

    High-level idea:
    We want to keep as many original positions unchanged as possible.

    A final strict valley array has:
    - a strictly decreasing prefix ending at valley position p
    - a strictly increasing suffix starting at p

    If we decide that index p is the valley position and we keep nums[p] unchanged,
    then:
    - on the left side, any kept unchanged positions must form a strictly decreasing
      sequence ending at nums[p]
    - on the right side, any kept unchanged positions must form a strictly increasing
      sequence starting at nums[p]

    So for every index i, we compute:
    1) leftKeep[i]  = maximum number of unchanged elements in a strictly decreasing
                      subsequence that ends exactly at i
    2) rightKeep[i] = maximum number of unchanged elements in a strictly increasing
                      subsequence that starts exactly at i

    Then if i is chosen as the valley and kept unchanged, the total unchanged count is:
        leftKeep[i] + rightKeep[i] - 1
    (-1 because nums[i] is counted in both parts)

    The answer is:
        n - max over valid valley positions i (leftKeep[i] + rightKeep[i] - 1)

    The main challenge is computing these values in O(n log n).
    We do that with coordinate compression + Fenwick tree for range maximum queries.
    */
    public int MinimumRepairs(int[] nums)
    {
        int n = nums.Length;

        // ------------------------------------------------------------
        // STEP 1: Coordinate compression
        // ------------------------------------------------------------
        // Why do we need this?
        // The values can be as small as -1e9 and as large as 1e9.
        // Fenwick trees work best on compact indices like 1..m.
        //
        // Coordinate compression maps each distinct value to a rank:
        // smallest value -> 1, next -> 2, ...
        //
        // Important:
        // We only care about relative ordering (<, >), not actual magnitudes.
        // So compression preserves everything we need.
        // ------------------------------------------------------------
        int[] sorted = new int[n];
        Array.Copy(nums, sorted, n);
        Array.Sort(sorted);

        int m = 0;
        for (int i = 0; i < n; i++)
        {
            if (i == 0 || sorted[i] != sorted[i - 1])
            {
                sorted[m++] = sorted[i];
            }
        }

        int[] rank = new int[n];
        for (int i = 0; i < n; i++)
        {
            rank[i] = LowerBound(sorted, m, nums[i]) + 1; // 1-based rank
        }

        // ------------------------------------------------------------
        // STEP 2: Compute leftKeep[i]
        // ------------------------------------------------------------
        // leftKeep[i] = length of the longest strictly decreasing subsequence
        //               that ends exactly at index i.
        //
        // If we want a decreasing subsequence ending at nums[i], then the previous
        // kept value must be strictly greater than nums[i].
        //
        // Standard trick:
        // A decreasing subsequence in original values is the same as an increasing
        // subsequence if we reverse the value order.
        //
        // We define:
        //   reversedRank = m - rank[i] + 1
        //
        // Then "previous value > current value" becomes
        // "previous reversedRank < current reversedRank".
        //
        // So we can compute:
        //   leftKeep[i] = 1 + max(dp of earlier indices with reversedRank smaller)
        //
        // Fenwick tree stores prefix maximums over reversed ranks.
        // ------------------------------------------------------------
        int[] leftKeep = new int[n];
        var bitLeft = new FenwickMax(m);

        for (int i = 0; i < n; i++)
        {
            int reversedRank = m - rank[i] + 1;

            // Query the best subsequence length among earlier elements
            // whose reversed rank is strictly smaller.
            // That corresponds exactly to earlier values strictly greater than nums[i].
            int bestBefore = bitLeft.Query(reversedRank - 1);

            leftKeep[i] = bestBefore + 1;

            // Update the Fenwick tree with the subsequence ending at i.
            bitLeft.Update(reversedRank, leftKeep[i]);
        }

        // ------------------------------------------------------------
        // STEP 3: Compute rightKeep[i]
        // ------------------------------------------------------------
        // rightKeep[i] = length of the longest strictly increasing subsequence
        //                that starts exactly at index i.
        //
        // We process from right to left.
        //
        // If we want an increasing subsequence starting at nums[i], then the next
        // kept value must be strictly greater than nums[i].
        //
        // Again we use reversed ranks:
        // values strictly greater than nums[i] correspond to reversed ranks
        // strictly smaller than reversedRank(i).
        //
        // So:
        //   rightKeep[i] = 1 + max(dp of later indices with value > nums[i])
        //                = 1 + max(dp of later indices with smaller reversedRank)
        //
        // Fenwick tree again gives us prefix maximums.
        // ------------------------------------------------------------
        int[] rightKeep = new int[n];
        var bitRight = new FenwickMax(m);

        for (int i = n - 1; i >= 0; i--)
        {
            int reversedRank = m - rank[i] + 1;

            int bestAfter = bitRight.Query(reversedRank - 1);

            rightKeep[i] = bestAfter + 1;

            bitRight.Update(reversedRank, rightKeep[i]);
        }

        // ------------------------------------------------------------
        // STEP 4: Try every valid valley position
        // ------------------------------------------------------------
        // A valley position must satisfy:
        //   0 < p < n - 1
        // because there must be at least one element on each side.
        //
        // Also, to have a genuine decreasing left side and increasing right side,
        // we need:
        //   leftKeep[p] >= 2   (some kept element before p, plus p itself)
        //   rightKeep[p] >= 2  (p itself, plus some kept element after p)
        //
        // If those conditions hold, then choosing p as the valley and keeping
        // the best left and right subsequences gives:
        //   unchanged = leftKeep[p] + rightKeep[p] - 1
        //
        // We subtract 1 because p is counted in both values.
        // ------------------------------------------------------------
        int maxUnchanged = 0;

        for (int p = 1; p <= n - 2; p++)
        {
            if (leftKeep[p] >= 2 && rightKeep[p] >= 2)
            {
                int unchanged = leftKeep[p] + rightKeep[p] - 1;
                if (unchanged > maxUnchanged)
                {
                    maxUnchanged = unchanged;
                }
            }
        }

        // Since n >= 3 and we can always modify elements arbitrarily,
        // a valley is always achievable.
        // The minimum repairs is total length minus maximum unchanged positions.
        return n - maxUnchanged;
    }

    // Standard lower_bound:
    // returns the first index in arr[0..length) where arr[index] >= target
    private int LowerBound(int[] arr, int length, int target)
    {
        int left = 0;
        int right = length;
        while (left < right)
        {
            int mid = left + (right - left) / 2;
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
    // Why Fenwick tree?
    // We need many operations of the form:
    // - Update one position with a value: tree[pos] = max(tree[pos], value)
    // - Query maximum on prefix [1..pos]
    //
    // Each operation is O(log n), which is fast enough for n up to 200000.
    // ------------------------------------------------------------
    private class FenwickMax
    {
        private readonly int[] tree;

        public FenwickMax(int size)
        {
            tree = new int[size + 2];
        }

        public void Update(int index, int value)
        {
            while (index < tree.Length)
            {
                if (value > tree[index])
                {
                    tree[index] = value;
                }
                index += index & -index;
            }
        }

        public int Query(int index)
        {
            int result = 0;
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

int[] nums1 = { 9, 7, 5, 6, 8 };
int result1 = solution.MinimumRepairs(nums1);
Console.WriteLine(result1); // Expected: 0

int[] nums2 = { 4, 4, 3, 2, 5, 5 };
int result2 = solution.MinimumRepairs(nums2);
Console.WriteLine(result2); // Expected: 2

int[] nums3 = { 1, 2, 3 };
int result3 = solution.MinimumRepairs(nums3);
Console.WriteLine(result3); // One possible output: 2

int[] nums4 = { 5, 4, 3, 2, 1 };
int result4 = solution.MinimumRepairs(nums4);
Console.WriteLine(result4); // Needs repairs on right side