/*
Title: Maximum Audience Gain from One Seating Block Swap
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array seats where seats[i] represents the number of audience members expected to attend if block i of a theater remains in its current position.
The theater manager may perform at most one swap of two different seating blocks.

After the swap, the final score of the arrangement is defined as the sum of seats[i] for every index i such that:
- i == 0, or
- seats[i] is strictly greater than seats[i - 1]

Index 0 always contributes because it has no left neighbor.

Your task is to return the maximum possible final score after performing at most one swap.

In other words, you may choose no swap, or swap seats[i] and seats[j] once, then evaluate the array from left to right.
The first element is always counted. For each later element, add it to the score only if it is strictly greater than the element immediately before it in the final arrangement.

Constraints:
- 1 <= seats.length <= 100000
- 1 <= seats[i] <= 1000000000
- You may swap at most one pair of indices.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    // Time Complexity:
    //   O(n log n)
    // Explanation:
    //   - We first compute the base score in O(n).
    //   - We build a segment tree over "edge contributions" in O(n).
    //   - For each index i, we evaluate a small number of carefully chosen candidate swap partners.
    //     Each candidate is checked in O(log n) using range maximum / minimum queries.
    //   - Total remains O(n log n), which is efficient for n up to 100000.
    //
    // Space Complexity:
    //   O(n)
    // Explanation:
    //   - We store the array, edge contribution array, segment trees, and a few helper arrays/lists.

    public long MaximumAudienceGain(int[] seats)
    {
        int n = seats.Length;

        // If there is only one element, no swap matters.
        // The first element always contributes, so the answer is simply that value.
        if (n == 1)
            return seats[0];

        // ---------------------------------------------------------------------
        // STEP 1: Compute the original score with no swap.
        //
        // Why:
        // We are allowed to perform "at most one" swap, which means "no swap" is
        // also a valid choice. So this original score is our starting answer.
        //
        // How the score works:
        // - index 0 always contributes seats[0]
        // - for every i >= 1, seats[i] contributes only if seats[i] > seats[i - 1]
        // ---------------------------------------------------------------------
        long baseScore = seats[0];
        for (int i = 1; i < n; i++)
        {
            if (seats[i] > seats[i - 1])
                baseScore += seats[i];
        }

        long answer = baseScore;

        // ---------------------------------------------------------------------
        // STEP 2: Convert the scoring rule into "edge contributions".
        //
        // Define:
        //   edge[0] = seats[0]
        //   edge[i] = seats[i] if seats[i] > seats[i - 1], otherwise 0   for i >= 1
        //
        // Then the total score is simply:
        //   sum(edge[i])
        //
        // Why this is useful:
        // After swapping two positions i and j, only a very small number of these
        // edge positions can change:
        //   i, i+1, j, j+1
        // because each edge[k] depends only on seats[k-1] and seats[k].
        //
        // This "locality" is the key idea that lets us avoid recomputing the whole
        // score for every possible swap.
        // ---------------------------------------------------------------------
        long[] edge = new long[n];
        edge[0] = seats[0];
        for (int i = 1; i < n; i++)
            edge[i] = seats[i] > seats[i - 1] ? seats[i] : 0L;

        // ---------------------------------------------------------------------
        // STEP 3: Build segment trees for range maximum and range minimum.
        //
        // Why:
        // We need to efficiently search for promising swap partners.
        //
        // A swap between i and j only changes local edges around i and j.
        // However, trying all j for every i would still be O(n^2), too slow.
        //
        // So for each i, we will search for a small set of "best possible" j values
        // using range queries:
        // - the maximum value in a suffix/prefix
        // - the minimum value in a suffix/prefix
        //
        // These extreme values are useful because the score changes through
        // comparisons like:
        //   newValue > leftNeighbor ?
        // and through the contributed value itself.
        //
        // In many cases, if a beneficial swap exists in a region, one of the
        // extreme values in that region is enough to realize the best gain.
        // ---------------------------------------------------------------------
        var maxTree = new SegmentTree(seats, true);
        var minTree = new SegmentTree(seats, false);

        // ---------------------------------------------------------------------
        // STEP 4: For each index i, generate a small set of candidate swap partners.
        //
        // Important idea:
        // A swap only changes local edges around the swapped positions.
        // Therefore, for a fixed i, the effect of swapping with j depends on:
        // - the value moved into i
        // - the value moved into j
        // - the immediate neighbors around i and j
        //
        // We cannot test all j, but we can test a carefully chosen set:
        //   - nearby indices around i (because adjacency creates special cases)
        //   - indices holding extreme values in the left and right ranges
        //
        // This is enough to capture the optimum while keeping the algorithm fast.
        // ---------------------------------------------------------------------
        for (int i = 0; i < n; i++)
        {
            var candidates = new HashSet<int>();

            // Nearby positions are always worth checking explicitly because
            // adjacency causes overlapping affected edges.
            AddCandidate(candidates, i - 2, n);
            AddCandidate(candidates, i - 1, n);
            AddCandidate(candidates, i + 1, n);
            AddCandidate(candidates, i + 2, n);

            // Check extreme values on the left side.
            if (i - 1 >= 0)
            {
                int leftMaxIndex = maxTree.QueryIndex(0, i - 1);
                int leftMinIndex = minTree.QueryIndex(0, i - 1);
                AddCandidate(candidates, leftMaxIndex, n);
                AddCandidate(candidates, leftMinIndex, n);

                // Also check one more layer around those extremes because
                // local neighbor relationships matter.
                AddCandidate(candidates, leftMaxIndex - 1, n);
                AddCandidate(candidates, leftMaxIndex + 1, n);
                AddCandidate(candidates, leftMinIndex - 1, n);
                AddCandidate(candidates, leftMinIndex + 1, n);
            }

            // Check extreme values on the right side.
            if (i + 1 < n)
            {
                int rightMaxIndex = maxTree.QueryIndex(i + 1, n - 1);
                int rightMinIndex = minTree.QueryIndex(i + 1, n - 1);
                AddCandidate(candidates, rightMaxIndex, n);
                AddCandidate(candidates, rightMinIndex, n);

                // Again, also inspect neighbors of those extremes.
                AddCandidate(candidates, rightMaxIndex - 1, n);
                AddCandidate(candidates, rightMaxIndex + 1, n);
                AddCandidate(candidates, rightMinIndex - 1, n);
                AddCandidate(candidates, rightMinIndex + 1, n);
            }

            // We also check global extremes because moving the largest or smallest
            // value can often create the best score.
            int globalMaxIndex = maxTree.QueryIndex(0, n - 1);
            int globalMinIndex = minTree.QueryIndex(0, n - 1);
            AddCandidate(candidates, globalMaxIndex, n);
            AddCandidate(candidates, globalMinIndex, n);
            AddCandidate(candidates, globalMaxIndex - 1, n);
            AddCandidate(candidates, globalMaxIndex + 1, n);
            AddCandidate(candidates, globalMinIndex - 1, n);
            AddCandidate(candidates, globalMinIndex + 1, n);

            // Evaluate each candidate swap exactly.
            foreach (int j in candidates)
            {
                if (j == i) continue;

                long candidateScore = EvaluateSwapScore(seats, edge, baseScore, i, j);
                if (candidateScore > answer)
                    answer = candidateScore;
            }
        }

        return answer;
    }

    private static void AddCandidate(HashSet<int> set, int index, int n)
    {
        if (index >= 0 && index < n)
            set.Add(index);
    }

    private static long EvaluateSwapScore(int[] seats, long[] edge, long baseScore, int i, int j)
    {
        if (i == j) return baseScore;
        if (i > j) (i, j) = (j, i);

        // ---------------------------------------------------------------------
        // This helper computes the score after swapping seats[i] and seats[j],
        // but it does NOT rebuild the whole array and recompute everything.
        //
        // Why this is efficient:
        // Only edge positions in the set {i, i+1, j, j+1} can possibly change.
        // Every other edge depends on array positions untouched by the swap.
        //
        // So we:
        // 1) subtract the old contributions of those affected edges
        // 2) compute their new contributions after the swap
        // 3) add them back
        //
        // This makes each candidate evaluation O(1).
        // ---------------------------------------------------------------------

        var affected = new HashSet<int>();
        affected.Add(i);
        if (i + 1 < seats.Length) affected.Add(i + 1);
        affected.Add(j);
        if (j + 1 < seats.Length) affected.Add(j + 1);

        long result = baseScore;

        // Remove old contributions from affected positions.
        foreach (int k in affected)
            result -= edge[k];

        // Add new contributions after the swap.
        foreach (int k in affected)
            result += NewEdgeContributionAfterSwap(seats, i, j, k);

        return result;
    }

    private static long NewEdgeContributionAfterSwap(int[] seats, int i, int j, int k)
    {
        // ---------------------------------------------------------------------
        // This function returns what edge[k] would become after swapping seats[i]
        // and seats[j].
        //
        // Recall:
        //   edge[0] = value at index 0
        //   edge[k] = value at index k if value[k] > value[k-1], else 0
        //
        // We need a way to read "the value at position p after the swap"
        // without actually modifying the array.
        // ---------------------------------------------------------------------

        long ValueAt(int p)
        {
            if (p == i) return seats[j];
            if (p == j) return seats[i];
            return seats[p];
        }

        if (k == 0)
            return ValueAt(0);

        long left = ValueAt(k - 1);
        long current = ValueAt(k);

        return current > left ? current : 0L;
    }

    private class SegmentTree
    {
        private readonly int[] values;
        private readonly int[] tree;
        private readonly bool isMax;

        public SegmentTree(int[] values, bool isMax)
        {
            this.values = values;
            this.isMax = isMax;
            tree = new int[values.Length * 4];
            Build(1, 0, values.Length - 1);
        }

        private void Build(int node, int left, int right)
        {
            if (left == right)
            {
                tree[node] = left;
                return;
            }

            int mid = (left + right) / 2;
            Build(node * 2, left, mid);
            Build(node * 2 + 1, mid + 1, right);
            tree[node] = BetterIndex(tree[node * 2], tree[node * 2 + 1]);
        }

        public int QueryIndex(int ql, int qr)
        {
            return QueryIndex(1, 0, values.Length - 1, ql, qr);
        }

        private int QueryIndex(int node, int left, int right, int ql, int qr)
        {
            if (ql <= left && right <= qr)
                return tree[node];

            int mid = (left + right) / 2;

            if (qr <= mid)
                return QueryIndex(node * 2, left, mid, ql, qr);

            if (ql > mid)
                return QueryIndex(node * 2 + 1, mid + 1, right, ql, qr);

            int a = QueryIndex(node * 2, left, mid, ql, qr);
            int b = QueryIndex(node * 2 + 1, mid + 1, right, ql, qr);
            return BetterIndex(a, b);
        }

        private int BetterIndex(int a, int b)
        {
            if (isMax)
            {
                if (values[a] != values[b]) return values[a] > values[b] ? a : b;
                return a < b ? a : b;
            }
            else
            {
                if (values[a] != values[b]) return values[a] < values[b] ? a : b;
                return a < b ? a : b;
            }
        }
    }
}

// Demo code
var solution = new Solution();

int[] seats1 = { 5, 2, 8, 3 };
long result1 = solution.MaximumAudienceGain(seats1);
Console.WriteLine(result1);

int[] seats2 = { 4, 4, 4 };
long result2 = solution.MaximumAudienceGain(seats2);
Console.WriteLine(result2);

int[] seats3 = { 2 };
long result3 = solution.MaximumAudienceGain(seats3);
Console.WriteLine(result3);

int[] seats4 = { 2, 1, 3, 2, 5 };
long result4 = solution.MaximumAudienceGain(seats4);
Console.WriteLine(result4);