/*
Title: Minimum Relays to Seal a Firebreak Tree

Problem Description:
You are given an undirected tree with n zones numbered from 0 to n - 1. A wildfire can start at any leaf zone and spreads one edge per minute toward the interior of the tree. You may install emergency relay beacons on some zones. A beacon protects its own zone, its parent, and all of its direct children. If the fire reaches a protected zone, it is stopped immediately and cannot pass through that zone.

Your task is to compute the minimum number of beacons needed so that, regardless of which leaf the fire starts from, the fire can never reach the designated command center root r.

The tree is rooted at r only for defining parent-child relationships used by beacon coverage. A beacon on node u covers: u, parent(u) if it exists, and every child of u. You may place a beacon on any node. The fire may start from any leaf, including leaves very deep in the tree. You must guarantee that every path from any leaf to the root contains at least one protected node.

Return the minimum number of beacons required.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= r < n
- edges.length == n - 1
- edges[i] = [ui, vi]
- The input graph is a valid tree.
- An O(n) or O(n log n) solution is expected.

Key idea:
We only need to ensure that every leaf-to-root path contains at least one protected node.
A node becomes protected if there is a beacon on:
- itself
- its parent
- one of its children

So for every subtree, we do dynamic programming with carefully chosen states that describe
whether the current node is already protected from below, whether we place a beacon here,
or whether we still require the parent to protect this node.

We root the tree at r, process children first (postorder), and compute three DP values per node:

State 0: dpNeedParent[u]
    All leaf-to-u paths inside u's subtree are already blocked below u,
    but node u itself is NOT protected by anything inside its subtree.
    Therefore, if a path from a leaf reaches u, it can only be stopped above u if parent(u)
    has a beacon. So this state means: "u still needs parent protection."

State 1: dpCoveredNoBeacon[u]
    All leaf-to-u paths inside u's subtree are blocked, and u is already protected from below
    (specifically by at least one child beacon), while u itself does NOT have a beacon.

State 2: dpHasBeacon[u]
    We place a beacon on u. Then u is protected, its parent is protected, and all children are protected.
    Also every leaf-to-u path is blocked because fire stops at u if it reaches u.

Transitions:
- If u is a leaf:
    needParent = 0
        A fire starting at u reaches u immediately. If parent has a beacon, u is protected and fire stops.
        No beacon is needed inside the leaf subtree itself.
    coveredNoBeacon = INF
        A leaf has no child, so it cannot be protected from below without placing a beacon on itself.
    hasBeacon = 1

- For an internal node u:
    hasBeacon:
        Since beacon on u protects every child, each child subtree only needs to ensure that
        every leaf-to-child path is blocked by the time fire reaches that child.
        A child may be in any of the three states because:
          * child needParent: satisfied because u has beacon
          * child coveredNoBeacon: already fine
          * child hasBeacon: also fine
        So:
            hasBeacon[u] = 1 + sum(min(all three child states))

    needParent:
        Since u is not protected inside its subtree, no fire may be allowed to stop at u or below u
        unless it is already blocked before reaching u.
        Therefore each child subtree must already block every leaf-to-child path strictly inside itself.
        Child cannot rely on u's beacon because u has no beacon in this state.
        Also child cannot have a beacon, because then child's beacon would protect u, contradicting
        that u is unprotected in this state.
        So every child must be in coveredNoBeacon state.
            needParent[u] = sum(coveredNoBeacon[child])
        If any child cannot do that, this state is impossible.

    coveredNoBeacon:
        u is protected from below, so at least one child must have a beacon.
        For every child:
          * child needParent is allowed, because a beacon on that child protects the child itself,
            and leaf-to-child paths are blocked there.
          * child coveredNoBeacon is allowed.
          * child hasBeacon is allowed.
        But to ensure u is protected from below, at least one child must be in hasBeacon state.
        So:
            coveredNoBeacon[u] = minimum total over children, with at least one child chosen as hasBeacon.
        We compute this efficiently by:
          1) taking for each child the cheaper of needParent / coveredNoBeacon / hasBeacon
          2) if at least one child already naturally chooses hasBeacon, done
          3) otherwise force one child to switch to hasBeacon with minimum extra cost

Final answer:
At the root, there is no parent, so root cannot be in needParent state.
Thus answer = min(coveredNoBeacon[root], hasBeacon[root])

This matches the examples:
- Example 1 => 2
- Example 2 => 2
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private const long INF = (long)4e18;

    // Time Complexity: O(n)
    // Space Complexity: O(n)
    public int MinimumBeacons(int n, int[][] edges, int r)
    {
        // -----------------------------
        // Step 1: Build the undirected adjacency list.
        // -----------------------------
        // Why:
        // The input tree is undirected, but our DP needs a rooted tree.
        // An adjacency list is the standard efficient structure for trees:
        // - O(n) total memory
        // - O(degree) iteration for each node
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        foreach (var e in edges)
        {
            int a = e[0];
            int b = e[1];
            graph[a].Add(b);
            graph[b].Add(a);
        }

        // -----------------------------
        // Step 2: Root the tree at r.
        // -----------------------------
        // We need parent/child relationships because beacon coverage depends on them.
        // We also need a postorder traversal so that children are processed before parent.
        //
        // We avoid recursive DFS because n can be as large as 200,000,
        // and recursion depth could overflow the call stack.
        //
        // So we do an iterative DFS:
        // - parent[u] stores the parent of u in the rooted tree
        // - order[] stores traversal order
        // Later we process order in reverse to simulate postorder.
        int[] parent = new int[n];
        Array.Fill(parent, -2);
        parent[r] = -1;

        int[] order = new int[n];
        int orderCount = 0;

        var stack = new Stack<int>();
        stack.Push(r);

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

        // -----------------------------
        // Step 3: Prepare DP arrays.
        // -----------------------------
        // needParent[u]      = state 0
        // coveredNoBeacon[u] = state 1
        // hasBeacon[u]       = state 2
        //
        // We use long internally to be extra safe with INF arithmetic,
        // though the final answer is at most n.
        long[] needParent = new long[n];
        long[] coveredNoBeacon = new long[n];
        long[] hasBeacon = new long[n];

        // -----------------------------
        // Step 4: Process nodes in reverse order (postorder).
        // -----------------------------
        // This guarantees that when we compute DP for u,
        // all child DP values are already known.
        for (int idx = n - 1; idx >= 0; idx--)
        {
            int u = order[idx];

            // Collect children of u in the rooted tree.
            // In a rooted tree, children are exactly neighbors except the parent.
            int childCount = 0;
            foreach (int v in graph[u])
            {
                if (v != parent[u]) childCount++;
            }

            // -----------------------------
            // Base case: leaf
            // -----------------------------
            // A leaf has no children in the rooted tree.
            if (childCount == 0)
            {
                // needParent = 0
                // Explanation:
                // If fire starts at this leaf, the only way to stop it without placing a beacon here
                // is for the parent to have a beacon, because parent beacon protects this leaf.
                // So this subtree can "delegate" responsibility upward at zero cost.
                needParent[u] = 0;

                // coveredNoBeacon = impossible
                // Explanation:
                // To be protected from below without placing a beacon on itself,
                // the leaf would need a child beacon, but it has no children.
                coveredNoBeacon[u] = INF;

                // hasBeacon = 1
                // Explanation:
                // Place a beacon on the leaf itself.
                hasBeacon[u] = 1;

                continue;
            }

            // -----------------------------
            // Transition for hasBeacon[u]
            // -----------------------------
            // If we place a beacon on u:
            // - u is protected
            // - every child is protected by u's beacon
            // Therefore each child can be in any valid state, including needParent,
            // because the child's "need parent beacon" requirement is satisfied by u.
            long costHasBeacon = 1;

            // -----------------------------
            // Transition for needParent[u]
            // -----------------------------
            // In this state:
            // - u has no beacon
            // - u is not protected from below
            // - so u must remain unprotected inside its subtree
            //
            // That means:
            // - no child may have a beacon, because a child beacon would protect u
            // - no child may be in needParent, because that would require u to have a beacon
            // So every child must be coveredNoBeacon.
            long costNeedParent = 0;
            bool needParentPossible = true;

            // -----------------------------
            // Transition for coveredNoBeacon[u]
            // -----------------------------
            // In this state:
            // - u has no beacon
            // - u is protected from below
            // Therefore at least one child must have a beacon.
            //
            // We compute:
            // 1) baseline = sum(min(child states))
            // 2) ensure at least one child is chosen in hasBeacon state
            long baselineCovered = 0;
            bool alreadyHasChildBeacon = false;
            long bestExtraToForceBeacon = INF;

            foreach (int v in graph[u])
            {
                if (v == parent[u]) continue;

                long a = needParent[v];
                long b = coveredNoBeacon[v];
                long c = hasBeacon[v];

                // ---- hasBeacon[u] contribution ----
                long bestForChildWhenUHasBeacon = Math.Min(a, Math.Min(b, c));
                costHasBeacon += bestForChildWhenUHasBeacon;

                // ---- needParent[u] contribution ----
                if (b >= INF / 2)
                {
                    needParentPossible = false;
                }
                else
                {
                    costNeedParent += b;
                }

                // ---- coveredNoBeacon[u] contribution ----
                long best = Math.Min(a, Math.Min(b, c));
                baselineCovered += best;

                if (best == c)
                {
                    // At least one optimal child choice already uses a beacon.
                    // Then u is protected from below automatically.
                    alreadyHasChildBeacon = true;
                    bestExtraToForceBeacon = 0;
                }
                else
                {
                    // If current best child state is not "hasBeacon",
                    // compute extra cost needed to force this child into hasBeacon.
                    long extra = c - best;
                    if (extra < bestExtraToForceBeacon)
                    {
                        bestExtraToForceBeacon = extra;
                    }
                }
            }

            hasBeacon[u] = costHasBeacon;
            needParent[u] = needParentPossible ? costNeedParent : INF;

            if (baselineCovered >= INF / 2 || bestExtraToForceBeacon >= INF / 2)
            {
                coveredNoBeacon[u] = INF;
            }
            else
            {
                coveredNoBeacon[u] = baselineCovered + bestExtraToForceBeacon;
            }
        }

        // -----------------------------
        // Step 5: Final answer at the root.
        // -----------------------------
        // The root has no parent, so it cannot be in needParent state.
        // Therefore the answer is the cheaper of:
        // - root is protected from below without beacon
        // - root has a beacon
        long answer = Math.Min(coveredNoBeacon[r], hasBeacon[r]);
        return (int)answer;
    }
}

// Demo code

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
int r1 = 0;
int result1 = solution.MinimumBeacons(n1, edges1, r1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int n2 = 8;
int[][] edges2 =
{
    new[] { 3, 0 },
    new[] { 3, 1 },
    new[] { 3, 4 },
    new[] { 4, 2 },
    new[] { 4, 5 },
    new[] { 5, 6 },
    new[] { 5, 7 }
};
int r2 = 3;
int result2 = solution.MinimumBeacons(n2, edges2, r2);
Console.WriteLine(result2); // Expected: 2

// Additional small sanity checks

// Single node tree: root is also a leaf.
// Fire can start at root itself, so root must be protected.
// One beacon on root is enough.
int n3 = 1;
int[][] edges3 = Array.Empty<int[]>();
int r3 = 0;
int result3 = solution.MinimumBeacons(n3, edges3, r3);
Console.WriteLine(result3); // Expected: 1

// Chain: 0 - 1 - 2, root = 0
// One beacon at 1 protects 0,1,2 and blocks the only leaf-to-root path.
int n4 = 3;
int[][] edges4 =
{
    new[] { 0, 1 },
    new[] { 1, 2 }
};
int r4 = 0;
int result4 = solution.MinimumBeacons(n4, edges4, r4);
Console.WriteLine(result4); // Expected: 1