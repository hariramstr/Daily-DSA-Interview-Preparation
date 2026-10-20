/*
Title: Maximum Recovered Signal by Cutting One Corrupted Branch

Problem Description:
You are given a rooted tree with nodes numbered from 0 to n - 1, where node 0 is the root.
Each node i has an integer signal value value[i], which may be positive, zero, or negative.

A monitoring system may disconnect exactly one subtree by cutting the edge between some node x
and its parent. If you cut at node x, then the entire subtree rooted at x is removed from the tree.
The root cannot be cut because it has no parent.

After at most one cut, the recovered signal of the system is defined as the maximum root-to-leaf
path sum in the remaining tree. If no cut is performed, the tree stays unchanged.

Your task is to return the largest possible recovered signal.

Important note:
After removing a subtree, some internal node may become a leaf if all of its children were removed.
The remaining structure is still a rooted tree containing node 0.

Constraints:
- 2 <= n <= 2 * 10^5
- edges.length == n - 1
- -10^9 <= value[i] <= 10^9
- The answer fits in signed 64-bit integer

Key idea:
We compute, for every node:
1) The best root-to-leaf sum inside its current subtree if we do NOT cut anything below it.
2) The best answer obtainable in its subtree if we are allowed to perform at most one cut somewhere
   inside that subtree.

This is a tree DP.

Definitions:
- down[u]:
    Maximum path sum from node u down to any leaf in u's subtree, with no cuts.
    If u is a leaf, down[u] = value[u].

- bestWithCut[u]:
    Maximum root-to-leaf sum starting at u and staying inside u's subtree after performing
    at most one cut somewhere inside u's subtree.

Transition:
Suppose u has children c1, c2, ..., ck.

Without any cut:
    down[u] = value[u] + max(down[child])

With at most one cut:
There are two possibilities:
A) The cut happens inside exactly one child's subtree v.
   Then the path from u must continue through v, and that child contributes bestWithCut[v].
   So candidate = value[u] + bestWithCut[v].

B) We cut away all children except we keep none on the chosen path, making u itself a leaf.
   This is only possible if u originally had at least one child, because then we can cut one child
   subtree and if that was the last remaining child on the chosen branch, u becomes a leaf.
   But with only one cut total, we cannot remove multiple children. Therefore "u becomes a leaf"
   is possible only when u has exactly one child and we cut that child.
   In that case candidate = value[u].

However, there is a more direct and correct way:
For node u, after at most one cut in its subtree, the resulting best path from u is:
- either no cut at all: down[u]
- or choose one child x to cut entirely. Then among the remaining children, the path can go through
  the best remaining child, or if no children remain, u becomes a leaf.

So if we know all children's down values, then cutting child x gives:
    value[u] + max( best down among children except x, 0-as-leaf-if-no-child-remains )
where "0-as-leaf-if-no-child-remains" means if after removing x there are no children left,
the continuation contribution is 0.

This means for each node we only need the largest and second-largest child down values.
Then:
- no cut candidate: value[u] + largestChildDown (or value[u] if leaf)
- cut child x candidate:
      value[u] + (best child down excluding x, or 0 if none remain)

But the single cut may also happen deeper inside one child subtree instead of cutting that child itself.
So the full recurrence is:
    bestWithCut[u] = max(
        value[u] + max over children v of bestWithCut[v],   // cut deeper in chosen child subtree
        value[u] + max over children x of best child down excluding x or 0 if none remain, // cut child x itself
        down[u] // no cut
    )

This can be simplified by computing:
- best child bestWithCut
- top two child down values

We process nodes in postorder iteratively to avoid recursion depth issues on large trees.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of complexity:
    - We build the adjacency list / children list in O(n).
    - We perform one iterative DFS/BFS-style traversal to obtain an order in O(n).
    - We process each node once in reverse order.
    - For each node, we scan its children a constant number of times overall.
      Since the total number of child edges is n - 1, the total work is O(n).
    */
    public long MaximumRecoveredSignal(int[] value, int[][] edges)
    {
        int n = value.Length;

        // children[u] will store all direct children of node u.
        // Because the tree is rooted at 0 and edges are given as undirected pairs in general,
        // we first build an undirected graph, then root it at 0.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
            graph[i] = new List<int>();

        foreach (var e in edges)
        {
            int a = e[0];
            int b = e[1];
            graph[a].Add(b);
            graph[b].Add(a);
        }

        // parent[u] stores the parent of u in the rooted tree.
        // We initialize with -2 meaning "unvisited".
        var parent = new int[n];
        Array.Fill(parent, -2);
        parent[0] = -1;

        // order will store nodes in traversal order.
        // Reversing this order gives us a postorder-like processing sequence:
        // children are processed before their parent.
        var order = new int[n];
        int orderCount = 0;

        // Iterative DFS stack to avoid recursion depth problems for n up to 2e5.
        var stack = new Stack<int>();
        stack.Push(0);

        while (stack.Count > 0)
        {
            int u = stack.Pop();
            order[orderCount++] = u;

            foreach (int v in graph[u])
            {
                if (parent[v] != -2) continue;
                parent[v] = u;
                stack.Push(v);
            }
        }

        // Build explicit children lists from parent[].
        var children = new List<int>[n];
        for (int i = 0; i < n; i++)
            children[i] = new List<int>();

        for (int u = 1; u < n; u++)
            children[parent[u]].Add(u);

        // down[u]:
        // Maximum path sum from u to any leaf in u's subtree with NO cut.
        var down = new long[n];

        // bestWithCut[u]:
        // Maximum path sum from u to any leaf in u's subtree with AT MOST ONE cut
        // somewhere inside u's subtree.
        var bestWithCut = new long[n];

        // Process nodes in reverse traversal order so children are ready before parent.
        for (int idx = n - 1; idx >= 0; idx--)
        {
            int u = order[idx];

            // If u has no children, then u is already a leaf.
            // The only root-to-leaf path starting at u is just [u].
            if (children[u].Count == 0)
            {
                down[u] = value[u];
                bestWithCut[u] = value[u];
                continue;
            }

            // We need several pieces of information from the children:
            //
            // 1) largest and second-largest down[child]
            //    Why?
            //    If we decide to cut away one direct child x of u, then the best remaining path
            //    from u must use the best child OTHER THAN x. If x was the best child, we need
            //    the second-best. If x was not the best child, we can still use the best.
            //
            // 2) maximum bestWithCut[child]
            //    Why?
            //    The single cut might happen deeper inside one child's subtree rather than cutting
            //    that child directly from u.
            long maxDown1 = long.MinValue; // best child down
            long maxDown2 = long.MinValue; // second best child down
            int maxDownChild = -1;

            long maxChildBestWithCut = long.MinValue;

            foreach (int v in children[u])
            {
                long d = down[v];

                if (d > maxDown1)
                {
                    maxDown2 = maxDown1;
                    maxDown1 = d;
                    maxDownChild = v;
                }
                else if (d > maxDown2)
                {
                    maxDown2 = d;
                }

                if (bestWithCut[v] > maxChildBestWithCut)
                    maxChildBestWithCut = bestWithCut[v];
            }

            // Step 1: compute down[u] (no cut at all).
            // We must continue into one of the children because u is not a leaf in the original tree.
            down[u] = (long)value[u] + maxDown1;

            // Step 2: candidate where the cut happens deeper inside one chosen child subtree.
            // Then the path from u continues through that child, but that child subtree already
            // optimally used the one allowed cut.
            long candidateCutDeeper = (long)value[u] + maxChildBestWithCut;

            // Step 3: candidate where we cut one direct child x of u.
            //
            // If we cut child x, then the path from u can continue through the best remaining child.
            // If no child remains after cutting x, then u becomes a leaf and contributes only value[u].
            //
            // We must try all children x conceptually, but we can do it in O(number of children)
            // using maxDown1/maxDown2.
            long candidateCutDirectChild = long.MinValue;

            foreach (int x in children[u])
            {
                long bestRemainingChildDown;

                // If x is the child that provided the best down value, then after removing x
                // the best remaining child is the second-best one.
                // Otherwise, the best remaining child is still the best one.
                if (x == maxDownChild)
                    bestRemainingChildDown = maxDown2;
                else
                    bestRemainingChildDown = maxDown1;

                // If there is no remaining child, u becomes a leaf.
                // In that case the continuation contribution is 0.
                long continuation = bestRemainingChildDown == long.MinValue ? 0L : bestRemainingChildDown;

                long candidate = (long)value[u] + continuation;
                if (candidate > candidateCutDirectChild)
                    candidateCutDirectChild = candidate;
            }

            // Step 4: combine all possibilities.
            //
            // - down[u]               : perform no cut anywhere in u's subtree
            // - candidateCutDeeper    : use the cut somewhere deeper below one child
            // - candidateCutDirectChild: cut one direct child of u
            bestWithCut[u] = Math.Max(down[u], Math.Max(candidateCutDeeper, candidateCutDirectChild));
        }

        // The whole remaining tree must always contain the root 0.
        // Therefore the final answer is the best root-to-leaf sum starting from root
        // after at most one cut in the whole tree.
        return bestWithCut[0];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] value1 = { 5, 4, -10, 3, 2, 7 };
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 },
    new[] { 2, 5 }
};
Console.WriteLine(solution.MaximumRecoveredSignal(value1, edges1)); // Expected: 12

// Example 2
int[] value2 = { 8, -5, 6, -2, 4, -1, 3 };
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 },
    new[] { 2, 5 },
    new[] { 5, 6 }
};
Console.WriteLine(solution.MaximumRecoveredSignal(value2, edges2)); // Expected: 16