/*
Title: Maximum Toll Collected by Closing One Ancestral Gate

Problem Description:
You are given a rooted tree with n checkpoints numbered from 0 to n - 1, where node 0 is the capital.
Each checkpoint i has a non-negative toll value toll[i]. A tourist route is any simple path from the
capital to a leaf, and the revenue of a route is the sum of toll values on the nodes along that path.

The city may permanently close exactly one checkpoint x, with the restriction that x cannot be the capital.
Closing x removes x and every node in the subtree rooted at x, so every route passing through x becomes
unavailable. After the closure, the city collects revenue only from the remaining capital-to-leaf routes.
Your task is to choose the checkpoint to close so that the total remaining revenue across all surviving
capital-to-leaf routes is maximized.

More formally, let S be the sum of revenues of all root-to-leaf paths in the original tree. If subtree(x)
is removed, every root-to-leaf path that enters x disappears entirely. Return the maximum possible total
revenue of the remaining routes.

The answer can be large, so use 64-bit integers.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Idea:
    1. Every root-to-leaf path contributes its full path sum to the total answer S.
    2. If we remove subtree(x), then every root-to-leaf path that passes through x disappears.
    3. So for each node x, we need to know:
       - how much total revenue is contributed by all root-to-leaf paths that pass through x
    4. Then:
       remainingRevenueAfterClosingX = totalRevenueAllPaths - removedRevenueForX
    5. We maximize this over all x != 0.

    Key observation:
    For a fixed node x, every leaf inside subtree(x) corresponds to exactly one root-to-leaf path
    that passes through x. Therefore, the revenue removed by deleting subtree(x) is the sum of the
    full root-to-leaf path sums for all leaves in subtree(x).

    So we compute:
    - rootToNodeSum[u] = sum of tolls on path from root to u
    - if u is a leaf, then rootToNodeSum[u] is one complete route revenue
    - removed[u] = sum of route revenues of all leaves in subtree(u)

    Then answer = max over u != 0 of (totalRoutesRevenue - removed[u])

    We do this iteratively to avoid recursion depth issues for n up to 2 * 10^5.
    */
    public long MaximumRemainingRevenue(int[] parent, int[] toll)
    {
        int n = parent.Length;

        // Build children adjacency list for the rooted tree.
        // Since parent[i] is given, this is straightforward:
        // for every node i > 0, add i as a child of parent[i].
        List<int>[] children = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            children[i] = new List<int>();
        }

        for (int i = 1; i < n; i++)
        {
            children[parent[i]].Add(i);
        }

        // rootToNodeSum[u] = sum of tolls from root (0) to node u.
        long[] rootToNodeSum = new long[n];

        // We need a traversal order from root to leaves to compute rootToNodeSum.
        // We also store the order so we can later process nodes in reverse order
        // for a bottom-up subtree DP.
        int[] order = new int[n];
        int orderCount = 0;

        // Iterative DFS stack to avoid recursion stack overflow on deep trees.
        Stack<int> stack = new Stack<int>();
        stack.Push(0);
        rootToNodeSum[0] = toll[0];

        while (stack.Count > 0)
        {
            int u = stack.Pop();
            order[orderCount++] = u;

            // Propagate prefix path sums to children.
            foreach (int v in children[u])
            {
                rootToNodeSum[v] = rootToNodeSum[u] + toll[v];
                stack.Push(v);
            }
        }

        // removed[u] = total revenue of all root-to-leaf routes that pass through u,
        // which is exactly the sum of complete route revenues for all leaves in subtree(u).
        long[] removed = new long[n];

        long totalRevenue = 0;

        // Process in reverse topological/tree order so children are ready before parent.
        for (int i = n - 1; i >= 0; i--)
        {
            int u = order[i];

            // If a node has no children, it is a leaf.
            // Then subtree(u) contains exactly one leaf route ending at u,
            // whose full route revenue is rootToNodeSum[u].
            if (children[u].Count == 0)
            {
                removed[u] = rootToNodeSum[u];
            }
            else
            {
                long sum = 0;

                // For an internal node, every leaf route in its subtree belongs to exactly
                // one child subtree, so we sum removed[child] over all children.
                foreach (int v in children[u])
                {
                    sum += removed[v];
                }

                removed[u] = sum;
            }

            // For the root, removed[root] equals the total revenue of all original routes,
            // because every route passes through the root.
            if (u == 0)
            {
                totalRevenue = removed[u];
            }
        }

        // We must close exactly one non-root node.
        long best = 0;

        for (int u = 1; u < n; u++)
        {
            long remaining = totalRevenue - removed[u];
            if (remaining > best)
            {
                best = remaining;
            }
        }

        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] parent1 = { -1, 0, 0, 1, 1, 2 };
int[] toll1 = { 5, 2, 4, 3, 1, 6 };
long result1 = solution.MaximumRemainingRevenue(parent1, toll1);
Console.WriteLine(result1); // Correct result for this input is 25

// Example 2
int[] parent2 = { -1, 0, 0, 1, 3, 3, 2 };
int[] toll2 = { 1, 10, 2, 5, 4, 6, 8 };
long result2 = solution.MaximumRemainingRevenue(parent2, toll2);
Console.WriteLine(result2); // Correct result for this input is 42