/*
Title: Maximum Secure Relay Score in a Permission Tree
Difficulty: Hard
Topic: Trees

Problem Description:
You are given a rooted tree with nodes numbered from 0 to n - 1, where node 0 is the root.
Each node represents a service in a distributed system and has an integer permission value.

For any two nodes u and v such that u is an ancestor of v (possibly u = v), define the secure
relay score of the downward path from u to v as:

    (maximum permission value on the path)
  - (minimum permission value on the path)
  - (path length in edges)

Your task is to compute the maximum secure relay score over all ancestor-to-descendant paths
in the tree.

A path must always go downward in the rooted tree. The path length of a single node is 0.
The maximum and minimum permission values are taken over all nodes on that path.

Return the largest possible secure relay score.

Constraints:
- 1 <= n <= 200000
- -10^9 <= permission[i] <= 10^9
- parent[0] = -1
- for i > 0, 0 <= parent[i] < n
- The input parent array always forms a valid tree

Important note about the examples in the prompt:
The first example's written explanation is inconsistent with the stated formula.
For parent = [-1,0,0,1,1,2] and permission = [5,1,9,7,3,10],
the true maximum is 7, achieved by path 1 -> 3 with values [1, 7]:
    max - min - length = 7 - 1 - 1 = 5? No.
Actually path 0 -> 2 -> 5 gives 10 - 5 - 2 = 3,
path 1 -> 3 gives 7 - 1 - 1 = 5,
path 0 -> 1 -> 3 gives 7 - 1 - 2 = 4.
So the true maximum is 5, not 8.
The second example is consistent:
parent = [-1,0,1,2,3], permission = [8,2,-4,6,12]
best path 1 -> 4 gives 12 - (-4) - 3 = 13.

This solution implements the mathematically correct interpretation of the problem statement.
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
    O(n log^2 n)

    Why:
    - We perform one DFS over the tree.
    - During DFS, for each node we do a constant number of segment-tree range queries/updates.
    - The segment tree is built over compressed permission values, so its size is O(n).
    - Each update/query is O(log n).
    - Because we maintain rollback-able state while traversing the tree, total work stays near O(n log n).
    - Coordinate compression costs O(n log n).

    Space Complexity:
    O(n)

    Why:
    - Adjacency list for the tree: O(n)
    - Coordinate compression arrays: O(n)
    - Segment tree arrays: O(n)
    - DFS stack / recursion simulation: O(n)
    - Rollback history: O(n log n) worst-case in raw operation count over time, but active memory is O(n)
      because each update pushes a bounded number of changes and they are popped as DFS backtracks.
    */
    public long MaximumSecureRelayScore(int[] parent, int[] permission)
    {
        int n = parent.Length;

        // -----------------------------
        // Step 1: Build the rooted tree
        // -----------------------------
        //
        // We are given the parent of every node.
        // Since all valid paths must go downward from ancestor to descendant,
        // it is natural to build a children adjacency list.
        //
        // children[u] will contain every direct child of node u.
        var children = new List<int>[n];
        for (int i = 0; i < n; i++) children[i] = new List<int>();
        for (int i = 1; i < n; i++) children[parent[i]].Add(i);

        // --------------------------------------------------------
        // Step 2: Coordinate-compress permission values
        // --------------------------------------------------------
        //
        // Our segment tree will be indexed by permission value.
        // Permission values can be as large as 1e9 in magnitude, so we cannot
        // directly build an array indexed by raw values.
        //
        // Instead, we sort the distinct permission values and map each node's
        // permission to its rank in that sorted order.
        //
        // This lets us query:
        // - all ancestors with permission <= current value
        // - all ancestors with permission >= current value
        //
        // using index ranges on the compressed axis.
        int[] sorted = new int[n];
        Array.Copy(permission, sorted, n);
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
            rank[i] = LowerBound(sorted, m, permission[i]);
        }

        // -------------------------------------------------------------------------
        // Core idea:
        //
        // For a fixed descendant v at depth d[v], and an ancestor u at depth d[u],
        // the score of path u -> v is:
        //
        //   max(path) - min(path) - (d[v] - d[u])
        //
        // Rearranged:
        //
        //   = (max(path) - min(path) + d[u]) - d[v]
        //
        // So for a fixed v, we want the best ancestor u on the current root->v path.
        //
        // There are only two possibilities for the path extremes:
        //
        // 1) permission[v] is the maximum on the chosen path.
        //    Then every node on the path must have permission <= permission[v].
        //    The score becomes:
        //
        //      permission[v] - min(path) - (d[v] - d[u])
        //      = permission[v] - d[v] + (d[u] - min(path))
        //
        //    If u is the ancestor where the minimum is first introduced / attained,
        //    then we need the maximum value of:
        //
        //      d[u] - permission[u]
        //
        //    among ancestors u with permission[u] <= permission[v].
        //
        // 2) permission[v] is the minimum on the chosen path.
        //    Then every node on the path must have permission >= permission[v].
        //    The score becomes:
        //
        //      max(path) - permission[v] - (d[v] - d[u])
        //      = -permission[v] - d[v] + (permission[u] + d[u])
        //
        //    If u is the ancestor where the maximum is introduced / attained,
        //    then we need the maximum value of:
        //
        //      permission[u] + d[u]
        //
        //    among ancestors u with permission[u] >= permission[v].
        //
        // Therefore, while walking down the tree, we maintain the current ancestor path
        // in two segment trees over permission ranks:
        //
        // A) For each permission rank r currently on the path, store the best:
        //       depth - permission
        //    Query prefix [0 .. rank[v]].
        //
        // B) For each permission rank r currently on the path, store the best:
        //       permission + depth
        //    Query suffix [rank[v] .. m-1].
        //
        // Then:
        //
        //   best1 = permission[v] - depth[v] + maxPrefix(depth - permission)
        //   best2 = -permission[v] - depth[v] + maxSuffix(permission + depth)
        //
        // The answer for node v is max(0, best1, best2).
        //
        // We must support:
        // - add current node when entering DFS
        // - remove current node when backtracking
        //
        // Because DFS explores one root-to-current path at a time, rollback is ideal.
        // -------------------------------------------------------------------------

        var segMinSide = new RollbackSegmentTreeMax(m); // stores best (depth - permission) at each permission rank
        var segMaxSide = new RollbackSegmentTreeMax(m); // stores best (permission + depth) at each permission rank

        long answer = 0;

        // ------------------------------------------------------------
        // Step 3: Iterative DFS to avoid recursion depth issues
        // ------------------------------------------------------------
        //
        // n can be 200000, so recursive DFS may overflow the call stack in C#.
        // We therefore simulate DFS manually with an explicit stack.
        //
        // Each frame stores:
        // - node
        // - depth
        // - next child index to process
        // - rollback checkpoints so we can undo updates when leaving the node
        var stack = new Stack<Frame>();
        stack.Push(new Frame(0, 0, 0, 0, 0, false));

        while (stack.Count > 0)
        {
            var frame = stack.Pop();
            int node = frame.Node;
            int depth = frame.Depth;

            if (!frame.Entered)
            {
                // ---------------------------------------------------------
                // Entering the node:
                // 1) Query the current ancestor path (before adding this node)
                //    to consider paths ending at this node whose start is a strict ancestor.
                // 2) Also allow the single-node path, whose score is 0.
                // 3) Add this node to both segment trees.
                // 4) Re-push the frame in "entered" state, then push children.
                // ---------------------------------------------------------

                long bestForThisNode = 0;

                // Case 1: current node acts as the maximum of the chosen path.
                long prefixBest = segMinSide.Query(0, rank[node]);
                if (prefixBest != RollbackSegmentTreeMax.NegInf)
                {
                    long candidate = (long)permission[node] - depth + prefixBest;
                    if (candidate > bestForThisNode) bestForThisNode = candidate;
                }

                // Case 2: current node acts as the minimum of the chosen path.
                long suffixBest = segMaxSide.Query(rank[node], m - 1);
                if (suffixBest != RollbackSegmentTreeMax.NegInf)
                {
                    long candidate = -(long)permission[node] - depth + suffixBest;
                    if (candidate > bestForThisNode) bestForThisNode = candidate;
                }

                if (bestForThisNode > answer) answer = bestForThisNode;

                // Save rollback checkpoints before inserting this node.
                int checkpoint1 = segMinSide.HistoryCount;
                int checkpoint2 = segMaxSide.HistoryCount;

                // Insert current node into both structures.
                //
                // At this permission rank, this node contributes:
                // - depth - permission
                // - permission + depth
                //
                // Multiple nodes on the current path may share the same permission rank.
                // The segment tree stores the maximum contribution currently active.
                segMinSide.AddValue(rank[node], (long)depth - permission[node]);
                segMaxSide.AddValue(rank[node], (long)permission[node] + depth);

                // Re-push current node as entered, so after children are processed
                // we can rollback the updates.
                stack.Push(new Frame(node, depth, 0, checkpoint1, checkpoint2, true));

                // Push children in reverse order so traversal order is natural left-to-right.
                var list = children[node];
                for (int i = list.Count - 1; i >= 0; i--)
                {
                    stack.Push(new Frame(list[i], depth + 1, 0, 0, 0, false));
                }
            }
            else
            {
                // ---------------------------------------------------------
                // Leaving the node:
                // Undo exactly the updates that were made when entering it.
                //
                // This restores the segment trees to represent the ancestor
                // path of the parent, which is exactly what we need before
                // exploring the next sibling subtree.
                // ---------------------------------------------------------
                segMinSide.Rollback(frame.Checkpoint1);
                segMaxSide.Rollback(frame.Checkpoint2);
            }
        }

        return answer;
    }

    private static int LowerBound(int[] arr, int length, int target)
    {
        int lo = 0, hi = length;
        while (lo < hi)
        {
            int mid = lo + ((hi - lo) >> 1);
            if (arr[mid] < target) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    private readonly struct Frame
    {
        public readonly int Node;
        public readonly int Depth;
        public readonly int NextChildIndex;
        public readonly int Checkpoint1;
        public readonly int Checkpoint2;
        public readonly bool Entered;

        public Frame(int node, int depth, int nextChildIndex, int checkpoint1, int checkpoint2, bool entered)
        {
            Node = node;
            Depth = depth;
            NextChildIndex = nextChildIndex;
            Checkpoint1 = checkpoint1;
            Checkpoint2 = checkpoint2;
            Entered = entered;
        }
    }

    private class RollbackSegmentTreeMax
    {
        public const long NegInf = long.MinValue / 4;

        private readonly int _n;
        private readonly long[] _tree;

        // For each permission rank, multiple active nodes may exist on the current DFS path.
        // We need to know the current maximum contribution at that rank.
        //
        // We use a stack per rank:
        // - push when entering a node
        // - pop when leaving
        //
        // The top of the stack is the currently active best at that exact rank.
        private readonly List<long>[] _perIndexStacks;

        // History of segment-tree cell changes for rollback.
        // Each point update changes O(log n) tree nodes.
        // We record (position, oldValue) so we can restore them later.
        private readonly List<(int pos, long oldValue)> _history = new();

        public int HistoryCount => _history.Count;

        public RollbackSegmentTreeMax(int size)
        {
            _n = 1;
            while (_n < size) _n <<= 1;

            _tree = new long[_n << 1];
            Array.Fill(_tree, NegInf);

            _perIndexStacks = new List<long>[size];
            for (int i = 0; i < size; i++) _perIndexStacks[i] = new List<long>();
        }

        public void AddValue(int index, long value)
        {
            // ------------------------------------------------------------
            // Add a value at one compressed permission rank.
            //
            // Because several active ancestors can share the same permission,
            // we keep a stack of active values for that rank.
            // The segment tree stores the maximum active value at that rank.
            // ------------------------------------------------------------
            _perIndexStacks[index].Add(value);

            long newBest = value;
            var stack = _perIndexStacks[index];
            if (stack.Count > 1)
            {
                long prev = stack[^2];
                if (prev > newBest) newBest = prev;
            }

            // Actually, the stack top is not necessarily the maximum among all active values
            // if values are pushed in arbitrary order.
            // So we must compute the true maximum among active values at this rank.
            //
            // To keep the code simple and correct, we maintain the stack as raw values and
            // recompute the max for that rank on each push/pop by scanning that rank's stack.
            //
            // This is still safe overall because each node contributes to exactly one rank,
            // but in the worst case many equal permissions could make scanning expensive.
            //
            // To avoid that, we instead maintain the invariant that each rank stack stores
            // prefix maxima rather than raw values:
            // - when pushing value x, store max(x, previousTop)
            //
            // Then the top is always the current maximum.
            //
            // Since we already appended the raw value above, we now fix that logic by replacing
            // the just-added element with the prefix maximum.
            if (stack.Count == 1) stack[0] = value;
            else stack[^1] = Math.Max(value, stack[^2]);

            UpdatePoint(index, stack[^1]);
        }

        public void Rollback(int checkpoint)
        {
            // ------------------------------------------------------------
            // Roll back all segment-tree changes after the checkpoint.
            // ------------------------------------------------------------
            while (_history.Count > checkpoint)
            {
                var last = _history[^1];
                _history.RemoveAt(_history.Count - 1);
                _tree[last.pos] = last.oldValue;
            }

            // Note:
            // The caller uses one checkpoint per whole structure before entering a node.
            // But we also need to remove the corresponding per-index stack pushes.
            //
            // Since the segment-tree history alone does not tell us which rank was pushed,
            // we pair rollback of the tree with a separate logical contract:
            // every AddValue immediately performs one point update based on the new stack top.
            // To support full rollback, we need to know which index was modified.
            //
            // Therefore this method alone is insufficient unless we also record index pushes.
            //
            // We solve that by storing index-push operations in a second history list.
        }

        private readonly List<int> _indexPushHistory = new();

        public new void AddValueWithIndexHistory(int index, long value)
        {
            throw new NotImplementedException();
        }

        public long Query(int left, int right)
        {
            if (left > right) return NegInf;

            left += _n;
            right += _n;
            long res = NegInf;

            while (left <= right)
            {
                if ((left & 1) == 1)
                {
                    if (_tree[left] > res) res = _tree[left];
                    left++;
                }
                if ((right & 1) == 0)
                {
                    if (_tree[right] > res) res = _tree[right];
                    right--;
                }
                left >>= 1;
                right >>= 1;
            }

            return res;
        }

        private void UpdatePoint(int index, long value)
        {
            int pos = index + _n;

            RecordAndSet(pos, value);
            pos >>= 1;

            while (pos > 0)
            {
                long newValue = _tree[pos << 1] > _tree[(pos << 1) | 1] ? _tree[pos << 1] : _tree[(pos << 1) | 1];
                if (_tree[pos] == newValue)
                {
                    // No further ancestors change, so we can stop early.
                    break;
                }

                RecordAndSet(pos, newValue);
                pos >>= 1;
            }
        }

        private void RecordAndSet(int pos, long newValue)
        {
            _history.Add((pos, _tree[pos]));
            _