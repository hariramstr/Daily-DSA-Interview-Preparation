/*
Title: Minimum Cost to Place Review Gates in a Dependency Tree

Problem Description:
You are given a software dependency tree with n modules numbered from 0 to n - 1, rooted at module 0.
Each module has a non-negative installation risk cost risk[i].

A review gate can be placed on any module i, paying risk[i].
A gate on module i protects:
- module i itself
- its parent
- all of its direct children

Your task is to place review gates so that every module in the tree is protected by at least one gate,
while minimizing the total cost.

Formally:
You are given an array risk of length n and an edge list edges of length n - 1 describing an undirected tree rooted at 0.
If a gate is placed at node i, then node i, every neighbor of i at distance 1 upward or downward along the rooted tree,
are considered protected. Every node must end up protected.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 100000
- 0 <= risk[i] <= 1000000000
- edges.length == n - 1
- edges represents a valid tree
- The answer fits in a 64-bit signed integer

Examples:
1)
Input: risk = [5,2,4,6], edges = [[0,1],[1,2],[1,3]]
Output: 2

2)
Input: risk = [7,3,8,2,5,1], edges = [[0,1],[0,2],[1,3],[1,4],[2,5]]
Output: 4
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Time Complexity: O(n)
    // Space Complexity: O(n)
    //
    // We perform a tree dynamic programming traversal.
    // For every node u, we compute three DP states:
    //
    // dp0[u] = minimum cost for the subtree of u when:
    //          - u has a gate
    //          - therefore u is protected by itself
    //          - u also protects all of its children
    //
    // dp1[u] = minimum cost for the subtree of u when:
    //          - u does NOT have a gate
    //          - u is protected by its parent
    //          - therefore children are NOT protected by u, so each child must handle itself internally
    //
    // dp2[u] = minimum cost for the subtree of u when:
    //          - u does NOT have a gate
    //          - u is NOT protected by parent
    //          - therefore u must be protected by at least one of its children having a gate
    //
    // Why these states are enough:
    // A node can only be protected by:
    // 1) itself
    // 2) its parent
    // 3) one of its children
    //
    // Since protection only reaches distance 1 in the rooted tree, these three cases fully describe
    // how the current node gets covered from outside/inside, and they are enough to combine subtrees correctly.
    public long MinimumCost(int[] risk, int[][] edges)
    {
        int n = risk.Length;

        // Special case: single node tree.
        // The only way to protect node 0 is to place a gate on it.
        if (n == 1)
            return risk[0];

        // Build adjacency list for the undirected tree.
        // We use List<int>[] because it is simple and efficient for sparse graphs like trees.
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

        // We root the tree at node 0.
        // To avoid recursion depth issues on n = 100000, we do an iterative DFS/BFS-style parent build,
        // then process nodes in reverse order so children are handled before parents.
        int[] parent = new int[n];
        Array.Fill(parent, -2); // -2 means unvisited
        parent[0] = -1;         // root has no parent

        int[] order = new int[n];
        int orderCount = 0;

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

        const long INF = long.MaxValue / 4;

        long[] dp0 = new long[n];
        long[] dp1 = new long[n];
        long[] dp2 = new long[n];

        // Process in reverse order so every child is computed before its parent.
        for (int idx = n - 1; idx >= 0; idx--)
        {
            int u = order[idx];

            // ------------------------------------------------------------
            // State dp0[u]: u HAS a gate
            // ------------------------------------------------------------
            //
            // If u has a gate:
            // - u is protected
            // - every child of u is protected by parent
            //
            // Therefore each child v can choose either:
            // - dp0[v]: child has its own gate
            // - dp1[v]: child does not have gate, but is protected by parent (which is u)
            //
            // Child cannot use dp2[v] here because dp2[v] means child expects one of its own children
            // to protect it, but since v is already protected by u, that state is not the right external condition.
            long costHasGate = risk[u];

            // ------------------------------------------------------------
            // State dp1[u]: u has NO gate, but u IS protected by parent
            // ------------------------------------------------------------
            //
            // Since u has no gate, u does not protect its children.
            // So each child v must be solved in a way that does not rely on u protecting it.
            //
            // For each child, valid choices are:
            // - dp0[v]: child has a gate
            // - dp2[v]: child has no gate and is not protected by parent, so it must be protected by its own child
            //
            // dp1[v] is invalid here because that would mean child v is protected by parent u,
            // but u has no gate.
            long costProtectedByParent = 0;

            // ------------------------------------------------------------
            // State dp2[u]: u has NO gate, u is NOT protected by parent,
            // so at least one child must have a gate to protect u.
            // ------------------------------------------------------------
            //
            // For each child v, the same local valid choices as in dp1[u]:
            // - dp0[v]
            // - dp2[v]
            //
            // But in addition, among all children, at least one must choose dp0[v]
            // so that u gets protected by a child gate.
            //
            // We compute:
            // 1) base = sum(min(dp0[v], dp2[v])) over children
            // 2) if base already includes some child using dp0 naturally, great
            // 3) otherwise we force one child to switch from dp2[v] to dp0[v] with minimum extra cost
            long baseForNeedChildProtection = 0;
            long minExtraToForceChildGate = INF;
            bool naturallyHasChildGate = false;

            bool isLeaf = true;

            foreach (int v in graph[u])
            {
                if (v == parent[u]) continue;
                isLeaf = false;

                // Transition for dp0[u]
                costHasGate += Math.Min(dp0[v], dp1[v]);

                // Transition for dp1[u]
                long bestWhenUHasNoGate = Math.Min(dp0[v], dp2[v]);
                costProtectedByParent += bestWhenUHasNoGate;

                // Transition preparation for dp2[u]
                baseForNeedChildProtection += bestWhenUHasNoGate;

                if (dp0[v] <= dp2[v])
                {
                    // This child can naturally be chosen with a gate without increasing the base.
                    // That means the "at least one child has a gate" requirement can already be satisfied.
                    naturallyHasChildGate = true;
                    minExtraToForceChildGate = 0;
                }
                else
                {
                    // If we need to force this child to have a gate, the extra cost is dp0[v] - dp2[v].
                    minExtraToForceChildGate = Math.Min(minExtraToForceChildGate, dp0[v] - dp2[v]);
                }
            }

            dp0[u] = costHasGate;
            dp1[u] = costProtectedByParent;

            if (isLeaf)
            {
                // Leaf handling is very important:
                //
                // dp0[leaf] = risk[leaf]
                //   Put a gate on the leaf.
                //
                // dp1[leaf] = 0
                //   Leaf has no gate, but parent protects it. No subtree cost below it.
                //
                // dp2[leaf] = INF
                //   Leaf has no gate, is not protected by parent, and must be protected by a child.
                //   Impossible, because a leaf has no children.
                dp2[u] = INF;
            }
            else
            {
                if (naturallyHasChildGate)
                {
                    // The base solution already includes at least one child gate.
                    dp2[u] = baseForNeedChildProtection;
                }
                else
                {
                    // No child gate was chosen in the base.
                    // We must force one child to switch to dp0.
                    dp2[u] = baseForNeedChildProtection + minExtraToForceChildGate;
                }
            }
        }

        // Root node 0 has no parent, so it cannot be in state dp1[0].
        // Therefore the final answer is:
        // - dp0[0]: root has a gate
        // - dp2[0]: root has no gate and must be protected by one of its children
        //
        // We take the minimum of these two valid root states.
        return Math.Min(dp0[0], dp2[0]);
    }
}

// Demo code

var solution = new Solution();

int[] risk1 = { 5, 2, 4, 6 };
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 1, 3 }
};
long result1 = solution.MinimumCost(risk1, edges1);
Console.WriteLine(result1); // Expected: 2

int[] risk2 = { 7, 3, 8, 2, 5, 1 };
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 },
    new[] { 2, 5 }
};
long result2 = solution.MinimumCost(risk2, edges2);
Console.WriteLine(result2); // Expected: 4