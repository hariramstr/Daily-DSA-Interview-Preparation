/*
Title: Maximum Emergency Detour Sum in a Hazard Tree
Difficulty: Hard
Topic: Trees

Problem Description:
You are given an undirected tree with n facilities numbered from 0 to n - 1. Each facility i has an integer risk value risk[i], which may be positive, zero, or negative. A response team starts at facility 0, the command center. During an emergency, the team may choose exactly one simple detour route that begins at any node on the path from the root to some node u, then continues downward to some descendant v without revisiting a node. In other words, the chosen route must be a contiguous downward path in the rooted tree (rooted at 0), and it may start at any ancestor and end at any descendant in that ancestor's subtree.

The score of a detour is the sum of risk values on all nodes in that route. However, the route is only considered valid if along the route, no risk value appears more than twice, and at most one distinct risk value is allowed to appear exactly twice. All other values on the route must appear exactly once.

Return the maximum possible score among all valid downward paths.

Constraints:
- 1 <= n <= 200000
- edges.length == n - 1
- 0 <= ui, vi < n
- edges forms a valid tree
- -10^9 <= risk[i] <= 10^9

Examples:
1)
n = 7
edges = [[0,1],[0,2],[1,3],[1,4],[2,5],[2,6]]
risk = [5,2,5,1,2,3,4]
Best valid route: 0 -> 2 -> 6
Sum = 5 + 5 + 4 = 14

2)
n = 6
edges = [[0,1],[1,2],[2,3],[3,4],[4,5]]
risk = [4,-2,4,7,7,1]
Best valid route: 2 -> 3 -> 4 -> 5
Values = [4,7,7,1]
Sum = 19

Key observation:
For every node v, any valid downward path ending at v is a suffix of the current root-to-v path.
So while doing DFS, we maintain the current root-to-node path and ask:
among all valid suffixes ending at the current node, which one has maximum sum?

Validity of a suffix [l..r]:
- no value appears more than twice
- at most one value appears exactly twice

This can be enforced by a left boundary L:
all suffixes starting at index >= L are valid, and any suffix starting before L is invalid.

Then for current endpoint r, the best valid suffix sum is:
prefix[r + 1] - min(prefix[i]) for i in [L..r]
So we need:
1) dynamic maintenance of L while DFS enters/exits nodes
2) fast query of minimum prefix value on a suffix of the current DFS stack

We use:
- rollback-able frequency tracking for values on the current path window
- a segment tree over prefix indices of the current DFS path to query min prefix on [L..depth]
- iterative DFS to avoid recursion depth issues for n up to 200000
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
    O(n log n)

    Why:
    - We visit each node once in DFS.
    - For each enter/exit event, we do O(log n) segment tree work.
    - Frequency updates and rollback bookkeeping are O(1) amortized per event.

    Space Complexity:
    O(n)

    Why:
    - Adjacency list stores O(n) edges for a tree.
    - DFS path arrays, prefix sums, rollback stacks, and segment tree are all O(n).
    */
    public long MaximumEmergencyDetourSum(int n, int[][] edges, int[] risk)
    {
        // -----------------------------
        // Step 1: Build adjacency list.
        // -----------------------------
        // We root the tree at node 0, but the input is undirected.
        // So we first build the usual adjacency list representation.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++) graph[i] = new List<int>();
        foreach (var e in edges)
        {
            int u = e[0], v = e[1];
            graph[u].Add(v);
            graph[v].Add(u);
        }

        // ---------------------------------------------------------
        // Step 2: Coordinate-compress risk values.
        // ---------------------------------------------------------
        // Risk values can be as large as 1e9 or as small as -1e9.
        // We only care about equality / frequency of values on the path,
        // so we compress them into ids 0..m-1.
        var sorted = new int[n];
        Array.Copy(risk, sorted, n);
        Array.Sort(sorted);

        var unique = new List<int>();
        for (int i = 0; i < n; i++)
        {
            if (i == 0 || sorted[i] != sorted[i - 1]) unique.Add(sorted[i]);
        }

        int[] compressed = new int[n];
        for (int i = 0; i < n; i++)
        {
            compressed[i] = LowerBound(unique, risk[i]);
        }

        int m = unique.Count;

        // -------------------------------------------------------------------
        // Step 3: Data structures for the current root-to-current-node path.
        // -------------------------------------------------------------------
        // pathValues[depth] = compressed risk value at that depth
        // prefix[0] = 0
        // prefix[k] = sum of first k nodes on current path
        //
        // If current node is at depth d, then current path has d+1 nodes,
        // and current endpoint prefix index is d+1.
        //
        // A suffix path from depth l to depth d has sum:
        // prefix[d+1] - prefix[l]
        //
        // Among valid l >= L, best sum is achieved by minimizing prefix[l].
        int[] pathValues = new int[n];
        long[] prefix = new long[n + 1];

        // -------------------------------------------------------------
        // Step 4: Segment tree for minimum prefix on active path indices.
        // -------------------------------------------------------------
        // We need to query:
        // min(prefix[i]) for i in [L..depth]
        //
        // Note carefully:
        // if current node is at depth d, then a suffix starting at depth l
        // uses prefix[l] as the "before start" prefix.
        //
        // So valid start depths are l in [L..d], and we query prefix indices [L..d].
        //
        // We activate prefix[depth] when we are at that depth.
        // prefix[depth+1] belongs to the current full path sum, not a start index.
        var seg = new MinSegmentTree(n + 1);

        // ----------------------------------------------------------------
        // Step 5: Frequency bookkeeping for the current valid sliding window.
        // ----------------------------------------------------------------
        // We maintain a left boundary L such that:
        // the suffix path from depth L to current depth is valid,
        // and L is the smallest such boundary after enforcing constraints.
        //
        // counts[val] = frequency of that value in current window [L..depth]
        //
        // duplicateKinds = how many distinct values currently appear exactly twice
        //
        // Valid window condition:
        // - no count > 2
        // - duplicateKinds <= 1
        //
        // When we append a new node at the right end, the window may become invalid.
        // Then we move L rightward until it becomes valid again.
        int[] counts = new int[m];
        int duplicateKinds = 0;
        int left = 0;

        // --------------------------------------------------------------------
        // Step 6: Rollback support.
        // --------------------------------------------------------------------
        // DFS branches require us to restore the exact state after returning
        // from a child subtree.
        //
        // We therefore save snapshots before entering a node:
        // - previous left boundary
        // - previous duplicateKinds
        // - all count changes caused while entering that node and while shrinking
        //
        // On exit, we undo those changes in reverse order.
        var countChangeValue = new List<int>();
        var countChangeOld = new List<int>();

        // Snapshot stacks indexed by DFS frame nesting.
        var snapLeft = new Stack<int>();
        var snapDuplicateKinds = new Stack<int>();
        var snapChangeSize = new Stack<int>();

        void SetCount(int valueId, int newCount)
        {
            // Save old count so we can rollback later.
            countChangeValue.Add(valueId);
            countChangeOld.Add(counts[valueId]);

            // Update duplicateKinds based on transition old -> new.
            int oldCount = counts[valueId];
            if (oldCount == 2) duplicateKinds--;
            counts[valueId] = newCount;
            if (newCount == 2) duplicateKinds++;
        }

        void AddValueToWindow(int valueId)
        {
            SetCount(valueId, counts[valueId] + 1);
        }

        void RemoveValueFromWindow(int valueId)
        {
            SetCount(valueId, counts[valueId] - 1);
        }

        bool WindowInvalid(int valueIdJustAdded)
        {
            // The window is invalid if:
            // - the just-added value exceeds count 2
            // - or more than one distinct value has count 2
            //
            // Why checking only the just-added value for >2 is enough:
            // before adding, the window was valid.
            // Therefore no value had count > 2.
            // Only the newly added value could have increased beyond 2.
            return counts[valueIdJustAdded] > 2 || duplicateKinds > 1;
        }

        void RollbackToSnapshot()
        {
            int targetSize = snapChangeSize.Pop();
            left = snapLeft.Pop();
            duplicateKinds = snapDuplicateKinds.Pop();

            while (countChangeValue.Count > targetSize)
            {
                int idx = countChangeValue.Count - 1;
                int valueId = countChangeValue[idx];
                int oldCount = countChangeOld[idx];

                // Remove current contribution from duplicateKinds.
                if (counts[valueId] == 2) duplicateKinds--;
                counts[valueId] = oldCount;
                if (oldCount == 2) duplicateKinds++;

                countChangeValue.RemoveAt(idx);
                countChangeOld.RemoveAt(idx);
            }
        }

        // ---------------------------------------------------------
        // Step 7: Iterative DFS.
        // ---------------------------------------------------------
        // We avoid recursion because n can be 200000, which can overflow
        // the call stack in C#.
        //
        // Each frame stores:
        // node, parent, next-neighbor-index, depth, entered?
        var nodeStack = new int[n];
        var parentStack = new int[n];
        var nextIndexStack = new int[n];
        var depthStack = new int[n];
        var enteredStack = new bool[n];
        int top = 0;

        nodeStack[0] = 0;
        parentStack[0] = -1;
        nextIndexStack[0] = 0;
        depthStack[0] = 0;
        enteredStack[0] = false;

        long answer = long.MinValue;

        while (top >= 0)
        {
            int node = nodeStack[top];
            int parent = parentStack[top];
            int depth = depthStack[top];

            if (!enteredStack[top])
            {
                enteredStack[top] = true;

                // ------------------------------------------------------------
                // ENTER NODE
                // ------------------------------------------------------------
                // Save snapshot so all modifications done for this node
                // can be rolled back when we leave this subtree.
                snapLeft.Push(left);
                snapDuplicateKinds.Push(duplicateKinds);
                snapChangeSize.Push(countChangeValue.Count);

                // Record this node on the current root-to-node path.
                pathValues[depth] = compressed[node];

                // Build prefix sum for current path.
                // prefix[depth + 1] = sum of nodes from depth 0..depth
                prefix[depth + 1] = prefix[depth] + risk[node];

                // Activate prefix[depth] as a possible start boundary.
                //
                // Why prefix[depth], not prefix[depth+1]?
                // A path starting at current node depth uses prefix[depth]
                // as the "sum before the path starts".
                seg.Update(depth, prefix[depth]);

                // Extend the current window [left..depth] by including this node.
                AddValueToWindow(pathValues[depth]);

                // If constraints are violated, move left boundary rightward
                // until the suffix becomes valid again.
                //
                // Every time we move left, we remove the value at old left
                // from the window.
                while (WindowInvalid(pathValues[depth]))
                {
                    RemoveValueFromWindow(pathValues[left]);
                    left++;
                }

                // Now [left..depth] is the smallest valid suffix ending here.
                //
                // Every suffix starting at l >= left is also valid,
                // because removing nodes from the left cannot increase any count.
                //
                // So the best valid path ending at this node is obtained by:
                // current path sum prefix[depth+1]
                // minus the minimum prefix[startDepth] over startDepth in [left..depth]
                long minPrefix = seg.Query(left, depth);
                long bestEndingHere = prefix[depth + 1] - minPrefix;
                if (bestEndingHere > answer) answer = bestEndingHere;
            }

            if (nextIndexStack[top] < graph[node].Count)
            {
                int nei = graph[node][nextIndexStack[top]++];
                if (nei == parent) continue;

                top++;
                nodeStack[top] = nei;
                parentStack[top] = node;
                nextIndexStack[top] = 0;
                depthStack[top] = depth + 1;
                enteredStack[top] = false;
            }
            else
            {
                // ------------------------------------------------------------
                // EXIT NODE
                // ------------------------------------------------------------
                // Deactivate prefix[depth] because this prefix index is no longer
                // on the active DFS path after we return to the parent.
                seg.Update(depth, long.MaxValue / 4);

                // Restore all frequency / left-boundary changes done while
                // processing this node and its subtree.
                RollbackToSnapshot();

                top--;
            }
        }

        return answer;
    }

    private static int LowerBound(List<int> arr, int target)
    {
        int l = 0, r = arr.Count;
        while (l < r)
        {
            int mid = l + ((r - l) >> 1);
            if (arr[mid] < target) l = mid + 1;
            else r = mid;
        }
        return l;
    }

    private class MinSegmentTree
    {
        private readonly int size;
        private readonly long[] tree;
        private const long INF = long.MaxValue / 4;

        public MinSegmentTree(int n)
        {
            size = 1;
            while (size < n) size <<= 1;
            tree = new long[size << 1];
            Array.Fill(tree, INF);
        }

        public void Update(int index, long value)
        {
            int pos = index + size;
            tree[pos] = value;
            pos >>= 1;
            while (pos > 0)
            {
                tree[pos] = Math.Min(tree[pos << 1], tree[(pos << 1) | 1]);
                pos >>= 1;
            }
        }

        public long Query(int left, int right)
        {
            long res = INF;
            int l = left + size;
            int r = right + size;

            while (l <= r)
            {
                if ((l & 1) == 1) res = Math.Min(res, tree[l++]);
                if ((r & 1) == 0) res = Math.Min(res, tree[r--]);
                l >>= 1;
                r >>= 1;
            }

            return res;
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------
var solution = new Solution();

// Example 1
int n1 = 7;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 },
    new[] { 2, 5 },
    new[] { 2, 6 }
};
int[] risk1 = { 5, 2, 5, 1, 2, 3, 4 };
long result1 = solution.MaximumEmergencyDetourSum(n1, edges1, risk1);
Console.WriteLine(result1); // Expected: 14

// Example 2
int n2 = 6;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 3, 4 },
    new[] { 4, 5 }
};
int[] risk2 = { 4, -2, 4, 7, 7, 1 };
long result2 = solution.MaximumEmergencyDetourSum(n2, edges2, risk2);
Console.WriteLine(result2); // Expected: 19