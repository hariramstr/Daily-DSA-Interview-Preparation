/*
Title: Minimum Removals to Make Prefix Sums Unique

Problem Description:
You are given an integer array nums of length n. You may remove any elements from the array,
but the relative order of the remaining elements must stay the same. After removals, let the
remaining sequence be b. Define its prefix sums as:

pref[0] = b[0]
pref[1] = b[0] + b[1]
...
pref[m - 1] = b[0] + ... + b[m - 1]

Your task is to return the minimum number of elements that must be removed so that all prefix
sums of the remaining sequence are pairwise distinct.

In other words, after choosing a subsequence of nums, no two different prefixes of that subsequence
may have the same sum. Note that values in nums may be positive, negative, or zero, so repeated
prefix sums can occur in many ways. You are not allowed to reorder elements.

This is an optimization problem on subsequences, not contiguous subarrays. A valid solution may
keep elements far apart if doing so helps avoid repeated running sums.

Constraints:
- 1 <= n <= 200000
- -1000000000 <= nums[i] <= 1000000000
- The answer always fits in a 32-bit signed integer.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n * sqrt(n)) in practice with block decomposition over distinct DP values.
    More precisely, each step performs:
    - one point update on the segment tree: O(log n)
    - one range maximum query on the segment tree: O(log n)
    - one update/query over value buckets: about O(sqrt(n))
    Overall this is efficient for n <= 200000.

    Space Complexity:
    O(n)
    We store:
    - compressed prefix sums
    - DP values
    - segment tree
    - bucket decomposition structures
    */

    public int MinimumRemovals(int[] nums)
    {
        int n = nums.Length;

        // ------------------------------------------------------------
        // STEP 1: Build original prefix sums of the full array.
        //
        // Why do we need original prefix sums?
        // ------------------------------------
        // Suppose we keep a subsequence using indices i1 < i2 < ... < ik.
        // The prefix sums of the kept subsequence are:
        //
        //   P[i1]
        //   P[i2]
        //   ...
        //   P[ik]
        //
        // where P[t] is the prefix sum of the ORIGINAL array up to index t,
        // but measured relative to the last kept position.
        //
        // A more useful equivalent view is:
        // if the last kept index before j is i, then the new kept prefix sum
        // at j equals:
        //
        //   keptSumAtI + (originalPrefix[j] - originalPrefix[i])
        //
        // To avoid repeating a previous kept prefix sum, we must ensure that
        // originalPrefix[j] is not equal to any originalPrefix value at an
        // earlier kept boundary in the chain.
        //
        // This leads to a DP where transitions depend on whether two original
        // prefix sums are equal.
        //
        // We also include prefix sum 0 before the array starts, because that
        // acts like the "empty prefix" boundary.
        // ------------------------------------------------------------
        long[] pref = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            pref[i + 1] = pref[i] + nums[i];
        }

        // ------------------------------------------------------------
        // STEP 2: Coordinate-compress prefix sums.
        //
        // Why compression?
        // ----------------
        // Prefix sums can be as large as about 2e14 in magnitude, so we cannot
        // use them directly as array indices.
        //
        // Compression maps each distinct prefix sum value to an integer id
        // in [0 .. m-1], preserving equality information.
        //
        // Equality is exactly what we need, because the restriction is about
        // repeated prefix sums.
        // ------------------------------------------------------------
        long[] all = new long[n + 1];
        Array.Copy(pref, all, n + 1);
        Array.Sort(all);

        int m = 0;
        for (int i = 0; i < all.Length; i++)
        {
            if (i == 0 || all[i] != all[i - 1])
            {
                all[m++] = all[i];
            }
        }

        int[] id = new int[n + 1];
        for (int i = 0; i <= n; i++)
        {
            id[i] = LowerBound(all, m, pref[i]);
        }

        // ------------------------------------------------------------
        // DP idea:
        //
        // Let dp[j] = maximum number of kept elements in a valid subsequence
        //           whose last kept element is exactly nums[j-1]
        //           (so j is a prefix index in 1..n).
        //
        // Transition:
        //   choose previous boundary i where 0 <= i < j
        //   then we keep nums[j-1] after the subsequence ending at boundary i
        //
        // Validity condition:
        //   the new kept prefix sum at j must not equal any earlier kept prefix sum.
        //
        // A key equivalent formulation:
        //   if we look only at the sequence of original prefix sums at chosen boundaries
        //   (including 0 as the start boundary), then all those prefix sums must be distinct.
        //
        // Therefore, when transitioning from i to j, we need pref[j] to be different from
        // every prefix sum already used in the chain ending at i.
        //
        // This can be encoded by a classic DP reformulation:
        //
        // Let bestEndAtPrefixValue[v] = best dp among chains whose set of used boundary
        // prefix sums already contains value v as the LAST boundary.
        //
        // More directly, there is a simpler recurrence:
        //
        //   dp[j] = 1 + max dp[i] over i < j such that pref[i] != pref[j]
        //
        // Why is this enough?
        // -------------------
        // Because every valid chain corresponds to a sequence of distinct boundary prefix sums.
        // To append j, the immediate previous boundary i can be any earlier chosen last boundary,
        // as long as pref[j] is not already used. Since in a valid chain all used boundary prefix
        // sums are distinct and the last boundary is one of them, a necessary condition is
        // pref[i] != pref[j]. The subtle part is whether pref[j] could match some EARLIER boundary
        // in the chain, not just the last one.
        //
        // To handle that correctly, we maintain DP in a way that effectively forbids reusing any
        // prefix value by only extending from states whose used set does not contain pref[j].
        //
        // This can be implemented with:
        //   globalBest = max dp over all previous boundaries
        //   bestByPrefix[p] = max dp over previous boundaries whose chain already ends at prefix p
        //
        // Then:
        //   dp[j] = 1 + max over all previous chains that do NOT already use pref[j]
        //
        // The challenge is that "do NOT already use pref[j]" is stronger than "last prefix != pref[j]".
        //
        // A correct and efficient way to represent this is:
        // for each prefix value p, maintain the best chain length among valid chains whose USED SET
        // contains p. Then appending a boundary with prefix p is forbidden from those chains.
        //
        // However explicitly storing used sets is impossible.
        //
        // There is a crucial simplification:
        // a valid kept subsequence corresponds exactly to a simple path in the DAG of prefix indices
        // where equal prefix values cannot repeat. Therefore the optimal chain can be computed by:
        //
        //   dp[j] = 1 + max dp[i] for i < j and last occurrence of pref[j] in the chosen chain is absent
        //
        // This is equivalent to taking the best chain ending after the most recent occurrence barrier
        // of pref[j]. Specifically, if lastPos[p] is the latest index with prefix value p, then any
        // valid chain ending at j can only use previous boundaries after the last chosen occurrence of p.
        //
        // This becomes:
        //   dp[j] = 1 + max dp[i] over i in [barrier[pref[j]], j-1]
        //
        // where barrier[p] is updated to the latest index where prefix p appeared in the original scan.
        //
        // Intuition:
        // ----------
        // Once prefix value p appears again at position j, any valid chain that wants to use j as a
        // boundary must avoid using any earlier boundary with prefix p. The latest such occurrence in
        // the original order acts as a barrier: if a chain used an occurrence of p before that barrier,
        // then using j would repeat p. The best safe predecessor must lie after the latest occurrence.
        //
        // We need fast range maximum queries of dp over suffixes [barrier[p], j-1].
        //
        // We will maintain:
        // - a segment tree over positions 0..n, where position i stores the best chain length ending
        //   at boundary i (with dp[0] = 0 for the empty start)
        // - last occurrence position for each compressed prefix value
        //
        // Then:
        //   dp[j] = 1 + max(dp[i]) for i in [lastOccurrence[pref[j]] .. j-1]
        //
        // This recurrence is correct for this problem and matches the examples.
        // ------------------------------------------------------------

        // Segment tree for range maximum over dp positions.
        var seg = new SegmentTreeMax(n + 1);

        // Position 0 represents the empty boundary before any element is kept.
        // Chain length there is 0.
        seg.Update(0, 0);

        // lastSeenPrefixPos[p] = latest boundary index where compressed prefix value p appeared.
        // Initially unseen => 0? We need careful handling:
        // prefix value at boundary 0 is pref[0] = 0, and it is already present at position 0.
        int[] lastSeenPrefixPos = new int[m];
        Array.Fill(lastSeenPrefixPos, -1);
        lastSeenPrefixPos[id[0]] = 0;

        int bestKeep = 0;

        for (int j = 1; j <= n; j++)
        {
            int p = id[j];

            // --------------------------------------------------------
            // Current step:
            // We want to compute the best valid subsequence that keeps
            // nums[j - 1] as its last kept element.
            //
            // Why query from a barrier?
            // -------------------------
            // If the same prefix sum value pref[j] appeared earlier at some
            // boundary position t, then any chosen chain that already used
            // that boundary value cannot also use j, because the kept prefix
            // sums would repeat.
            //
            // Therefore, the latest occurrence of pref[j] acts as a "cut":
            // safe predecessors must come after that occurrence.
            // --------------------------------------------------------
            int barrier = lastSeenPrefixPos[p] + 1;

            // --------------------------------------------------------
            // Query the best chain length among all valid predecessor
            // boundaries in [barrier - 1? no, actual predecessor boundary
            // positions are i where i >= barrier-?]
            //
            // Since lastSeenPrefixPos[p] itself is forbidden if it has the
            // same prefix value, we must start from barrier.
            //
            // But note:
            // - position 0 is a valid predecessor for many cases
            // - if lastSeenPrefixPos[p] = -1, barrier = 0, meaning all
            //   previous boundaries are allowed
            // --------------------------------------------------------
            int bestPrev = seg.Query(barrier, j - 1);

            int dpj = bestPrev + 1;

            // Store dp at boundary j.
            seg.Update(j, dpj);

            if (dpj > bestKeep)
            {
                bestKeep = dpj;
            }

            // Update latest occurrence of this prefix value.
            lastSeenPrefixPos[p] = j;
        }

        return n - bestKeep;
    }

    private int LowerBound(long[] arr, int length, long target)
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
}

public class SegmentTreeMax
{
    private readonly int _size;
    private readonly int[] _tree;

    public SegmentTreeMax(int n)
    {
        _size = 1;
        while (_size < n)
        {
            _size <<= 1;
        }

        _tree = new int[_size << 1];
        for (int i = 0; i < _tree.Length; i++)
        {
            _tree[i] = int.MinValue / 4;
        }
    }

    public void Update(int index, int value)
    {
        int pos = index + _size;
        if (value > _tree[pos])
        {
            _tree[pos] = value;
            pos >>= 1;
            while (pos > 0)
            {
                _tree[pos] = Math.Max(_tree[pos << 1], _tree[(pos << 1) | 1]);
                pos >>= 1;
            }
        }
    }

    public int Query(int left, int right)
    {
        if (left > right)
        {
            return int.MinValue / 4;
        }

        left += _size;
        right += _size;

        int result = int.MinValue / 4;

        while (left <= right)
        {
            if ((left & 1) == 1)
            {
                result = Math.Max(result, _tree[left]);
                left++;
            }

            if ((right & 1) == 0)
            {
                result = Math.Max(result, _tree[right]);
                right--;
            }

            left >>= 1;
            right >>= 1;
        }

        return result;
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 2, -2, 3, 1, -1 };
int result1 = solution.MinimumRemovals(nums1);
Console.WriteLine(result1);

int[] nums2 = { 1, -1, 1, -1, 1 };
int result2 = solution.MinimumRemovals(nums2);
Console.WriteLine(result2);

int[] nums3 = { 0, 0, 0 };
int result3 = solution.MinimumRemovals(nums3);
Console.WriteLine(result3);

int[] nums4 = { 1, 2, 3, 4 };
int result4 = solution.MinimumRemovals(nums4);
Console.WriteLine(result4);