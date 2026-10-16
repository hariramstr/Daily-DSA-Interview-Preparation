/*
Title: Minimum Relabels to Make Root Paths Increasing

Problem Description:
You are given a rooted tree with nodes numbered from 0 to n - 1, rooted at node 0.
Each node i has an integer label value[i].

You may relabel any node to any integer value, and changing the label of one node counts
as one relabel operation regardless of the new value chosen.

A tree is called root-path increasing if for every node, the sequence of labels along the
path from the root to that node is strictly increasing. Equivalently, for every edge
parent -> child, the final label of the child must be strictly greater than the final label
of the parent, and this must hold consistently on all root-to-node paths.

Return the minimum number of nodes that must be relabeled so the entire tree becomes
root-path increasing.

Important notes:
- You are allowed to keep a node's original label if it already fits the final assignment.
- Relabeled values do not need to come from the original set of labels.
- The goal is to maximize how many original labels can remain unchanged while still allowing
  a strictly increasing assignment on every root-to-node path.

Key observation:
A set of nodes can keep their original labels if and only if for every ancestor-descendant
pair among those kept nodes, the ancestor's original value is strictly smaller than the
descendant's original value.

Why?
- If two kept nodes lie on the same root-to-node path, all labels between them can always be
  reassigned as needed, because relabeled values may be any integers.
- Therefore, the only thing that can block feasibility is when two kept nodes on one path
  already violate strict increase.
- So we want the largest subset of nodes such that along every root-to-node path, the kept
  values form a strictly increasing sequence.

This becomes:
Find the maximum number of nodes we can keep unchanged so that on every root-to-node path,
kept values are strictly increasing.
Answer = n - maximumKeepable.

Algorithm idea:
We perform a DFS from the root while maintaining the current strictly increasing sequence of
kept values on the active root-to-current path, exactly like the classic LIS (Longest
Increasing Subsequence) technique.

For the current node value x:
- Let pos = lower_bound(tails, x), where tails stores the minimum possible ending value for
  an increasing sequence of each length on the current root path.
- If pos == tails.Count, we can extend the best sequence by keeping this node.
- Otherwise, we can replace tails[pos] with x, meaning:
  among all increasing kept-node sequences of length pos + 1 on this path, we now know one
  with a smaller ending value, which is always at least as good for future descendants.

The length of tails at any node equals the maximum number of keepable nodes on the current
root-to-node path. Across the whole tree, because DFS backtracks and restores state, this
correctly computes the best keepable count achievable on every path-consistent choice.

Most importantly:
A node's keep/skip decision only interacts with its ancestors on the same path, never with
nodes in other branches. That is exactly why the path-LIS state is sufficient.

Example 1:
Tree:
0(3)
├─ 1(4)
│  ├─ 3(5)
│  └─ 4(6)
└─ 2(2)

Path to node 4 has values 3,4,6 => increasing length 3.
The largest keepable set overall is {0,1,3,4} of size 4.
So answer = 5 - 4 = 1.

Example 2:
Path 0->1->2->3 has values 8,3,4,10.
The longest strictly increasing subsequence on that path has length 2: for example 8,10 or 3,4,10 is not valid with root-path ancestor consistency if root 8 is also kept before 3.
Across the whole tree, the best globally keepable set has size 4, so answer = 6 - 4 = 2.

This implementation is iterative to avoid recursion-depth issues for n up to 2 * 10^5.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the tree: O(n)
    - DFS traversal: O(n)
    - For each node, one binary search on the current LIS tails array: O(log n)
    Total: O(n log n)

    Space Complexity:
    - Adjacency list: O(n)
    - DFS stack: O(n)
    - LIS tails array: O(n) in the worst case
    Total: O(n)
    */
    public int MinimumRelabels(int n, int[][] edges, int[] value)
    {
        // -----------------------------
        // Step 1: Build the undirected tree as an adjacency list.
        // -----------------------------
        // We store neighbors for each node.
        // Since the input is a tree, there are exactly n - 1 edges and the graph is connected.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        foreach (var e in edges)
        {
            int u = e[0];
            int v = e[1];
            graph[u].Add(v);
            graph[v].Add(u);
        }

        // -----------------------------
        // Step 2: Prepare the LIS-style state for the current root-to-current DFS path.
        // -----------------------------
        // tails[len - 1] = the minimum possible ending value of a strictly increasing
        // sequence of kept nodes of length len on the CURRENT active DFS path.
        //
        // This is the standard patience-sorting / LIS optimization structure.
        //
        // Why this works here:
        // - We only care about ancestor constraints on the current path.
        // - Different branches are independent once we backtrack.
        var tails = new List<int>();

        // bestKeep will store the maximum number of nodes we can keep unchanged
        // over all root-to-node paths / all path-consistent choices encountered.
        int bestKeep = 0;

        // -----------------------------
        // Step 3: Iterative DFS with explicit enter/exit events.
        // -----------------------------
        // We avoid recursion because n can be as large as 200,000, and deep recursion
        // could cause a stack overflow in C#.
        //
        // For each node, we need to:
        //   - "enter" the node: apply its value to the LIS state
        //   - process children
        //   - "exit" the node: undo the LIS modification so sibling branches see the
        //     correct path state
        //
        // To support undo, each stack frame stores:
        //   node, parent, phase, positionChanged, oldValue, appendedFlag
        //
        // phase = 0 => entering node
        // phase = 1 => exiting node
        var stack = new Stack<Frame>();
        stack.Push(new Frame(0, -1, 0, -1, 0, false));

        while (stack.Count > 0)
        {
            var frame = stack.Pop();

            if (frame.Phase == 0)
            {
                int node = frame.Node;
                int parent = frame.Parent;
                int x = value[node];

                // -----------------------------------------
                // Step 3a: Find where x fits in the current tails array.
                // -----------------------------------------
                // We need the first position with tails[pos] >= x.
                // This is lower_bound for strict increasing subsequences.
                //
                // Why lower_bound?
                // - If there is a tail >= x, replacing it with x gives a sequence of the
                //   same length but with a smaller ending value, which is better for future nodes.
                // - If no such tail exists, x extends the longest sequence.
                int pos = LowerBound(tails, x);

                bool appended;
                int oldValue = 0;

                if (pos == tails.Count)
                {
                    // x is larger than every current tail, so we can extend the sequence.
                    tails.Add(x);
                    appended = true;
                }
                else
                {
                    // x improves the best ending value for sequences of length pos + 1.
                    oldValue = tails[pos];
                    tails[pos] = x;
                    appended = false;
                }

                // After incorporating this node, tails.Count equals the maximum number
                // of nodes we can keep unchanged on the current root-to-this-node path.
                if (tails.Count > bestKeep)
                {
                    bestKeep = tails.Count;
                }

                // Push exit event first so it runs after all children are processed.
                stack.Push(new Frame(node, parent, 1, pos, oldValue, appended));

                // Then push children enter events.
                // We skip the parent because the graph is undirected.
                foreach (int nei in graph[node])
                {
                    if (nei == parent) continue;
                    stack.Push(new Frame(nei, node, 0, -1, 0, false));
                }
            }
            else
            {
                // -----------------------------------------
                // Step 3b: Undo the LIS modification made when entering this node.
                // -----------------------------------------
                // This restoration is essential because each DFS branch must see exactly
                // the state of the path from the root to its own parent, not changes from
                // previously processed sibling branches.
                if (frame.Appended)
                {
                    tails.RemoveAt(tails.Count - 1);
                }
                else
                {
                    tails[frame.PositionChanged] = frame.OldValue;
                }
            }
        }

        // We keep bestKeep nodes unchanged, so the minimum number of relabels is:
        return n - bestKeep;
    }

    // Standard lower_bound:
    // returns the first index i such that list[i] >= target.
    private int LowerBound(List<int> list, int target)
    {
        int left = 0;
        int right = list.Count;

        while (left < right)
        {
            int mid = left + ((right - left) >> 1);
            if (list[mid] < target)
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

    private readonly struct Frame
    {
        public int Node { get; }
        public int Parent { get; }
        public int Phase { get; }
        public int PositionChanged { get; }
        public int OldValue { get; }
        public bool Appended { get; }

        public Frame(int node, int parent, int phase, int positionChanged, int oldValue, bool appended)
        {
            Node = node;
            Parent = parent;
            Phase = phase;
            PositionChanged = positionChanged;
            OldValue = oldValue;
            Appended = appended;
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int n1 = 5;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 }
};
int[] value1 = { 3, 4, 2, 5, 6 };
int result1 = solution.MinimumRelabels(n1, edges1, value1);
Console.WriteLine(result1); // Expected: 1

// Example 2
int n2 = 6;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 1, 4 },
    new[] { 4, 5 }
};
int[] value2 = { 8, 3, 4, 10, 9, 11 };
int result2 = solution.MinimumRelabels(n2, edges2, value2);
Console.WriteLine(result2); // Expected: 2

// Additional quick sanity checks

// Single node: already valid, no relabel needed.
int n3 = 1;
int[][] edges3 = Array.Empty<int[]>();
int[] value3 = { 42 };
Console.WriteLine(solution.MinimumRelabels(n3, edges3, value3)); // Expected: 0

// Chain: 5 -> 4 -> 3, only one can stay if values strictly decrease.
int n4 = 3;
int[][] edges4 =
{
    new[] { 0, 1 },
    new[] { 1, 2 }
};
int[] value4 = { 5, 4, 3 };
Console.WriteLine(solution.MinimumRelabels(n4, edges4, value4)); // Expected: 2