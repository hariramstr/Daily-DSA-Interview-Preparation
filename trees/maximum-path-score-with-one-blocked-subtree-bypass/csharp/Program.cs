/*
Title: Maximum Path Score With One Blocked Subtree Bypass

Problem Description:
You are given a rooted tree with n nodes numbered from 0 to n - 1, rooted at node 0.
Each node i has an integer score value[i], which may be positive, zero, or negative.
You are also given a list blocked containing some nodes that are considered unavailable.
A valid root-to-leaf route normally cannot pass through any blocked node.

However, the system allows exactly one special bypass operation on a route: at most once,
when your route reaches a blocked node b, you may ignore node b and jump directly to one
of its children c, continuing the route from c. This bypass skips only node b itself.
It does not remove the blockage of any other blocked nodes, and it cannot be used more than once.
If a blocked node has no children, the bypass cannot help there. The score of a route is the sum
of value[x] over all visited nodes; a skipped blocked node contributes nothing because it is not visited.

Return the maximum possible score of any root-to-leaf route using at most one bypass.
If no valid root-to-leaf route exists, return null.

A leaf is a node with no children. The route must start at the root.
If the root is blocked, you may use the bypass immediately to jump to one of its children.

Constraints:
- 1 <= n <= 200000
- -10^9 <= value[i] <= 10^9
- edges.length == n - 1
- edges describes a valid tree
- 0 <= blocked.length <= n
- All nodes in blocked are distinct
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Idea:
    We do tree DP with two states for every node u:

    1) dpUnused[u]
       Maximum score of a valid route that starts by VISITING node u and ends at a leaf
       in u's subtree, assuming the bypass has NOT been used before reaching u.

    2) dpUsed[u]
       Maximum score of a valid route that starts by VISITING node u and ends at a leaf
       in u's subtree, assuming the bypass HAS ALREADY been used before reaching u.

    Important meaning of "starts by VISITING node u":
    - If u is blocked, then dpUnused[u] and dpUsed[u] are both impossible, because a blocked
      node cannot be visited.
    - The bypass is handled by the parent when the parent tries to move into a blocked child.
      In that case, the parent may skip that blocked child and jump directly to one of its children.

    Transition details:
    - If u is a leaf:
        * If u is blocked => impossible in both states.
        * Otherwise dpUnused[u] = dpUsed[u] = value[u].

    - If u is not blocked:
        * dpUsed[u]:
            Since bypass is already consumed, we can only move to children by normal traversal.
            So from u we choose the best child v such that dpUsed[v] is possible.
            Result = value[u] + max(dpUsed[v]).
            If no child works and u is not a leaf, then impossible.

        * dpUnused[u]:
            We have two kinds of moves from u to continue downward:
            A) Normal move into an unblocked child v:
               score candidate = value[u] + dpUnused[v]
            B) Use the bypass on a blocked child b:
               We skip b itself and jump to one of b's children c.
               After using the bypass, it is now consumed, so the remainder must be dpUsed[c].
               score candidate = value[u] + dpUsed[c]
               for any child c of blocked child b.
            We take the maximum among all valid candidates.

    Why this is correct:
    - Every valid root-to-leaf route either never uses bypass, or uses it exactly once at the
      first blocked node encountered on the route.
    - Our DP states exactly encode whether bypass is still available.
    - Because the tree is rooted, every route from a node to a leaf is fully contained in that
      node's subtree, so bottom-up DP is natural and complete.
    */
    public long? MaximumPathScoreWithOneBlockedSubtreeBypass(int[] value, int[][] edges, int[] blocked)
    {
        int n = value.Length;

        // Step 1:
        // Build the rooted tree as a children adjacency list.
        //
        // The input edges are undirected tree edges.
        // Since the tree is rooted at node 0, we first build an undirected graph,
        // then perform an iterative DFS/BFS from the root to determine parent/children.
        //
        // We use List<int>[] because:
        // - it is simple and efficient for adjacency lists,
        // - total number of stored edges is O(n),
        // - iteration over neighbors/children is fast.
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

        // Step 2:
        // Mark blocked nodes for O(1) lookup.
        //
        // This lets us quickly answer:
        // "Is node u blocked?"
        // during DP transitions.
        var isBlocked = new bool[n];
        foreach (int b in blocked)
        {
            isBlocked[b] = true;
        }

        // Step 3:
        // Root the tree at 0 and also produce a traversal order.
        //
        // We need a postorder-like processing order so that children are processed
        // before their parent. A common iterative trick is:
        // - do a DFS/BFS to record an order,
        // - then process that order in reverse.
        //
        // parent[u] = parent of u in rooted tree, or -1 for root.
        // children[u] = list of children of u in rooted tree.
        var parent = new int[n];
        Array.Fill(parent, -2);

        var children = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            children[i] = new List<int>();
        }

        var order = new int[n];
        int orderCount = 0;

        var stack = new Stack<int>();
        stack.Push(0);
        parent[0] = -1;

        while (stack.Count > 0)
        {
            int u = stack.Pop();
            order[orderCount++] = u;

            foreach (int v in graph[u])
            {
                if (v == parent[u])
                {
                    continue;
                }

                parent[v] = u;
                children[u].Add(v);
                stack.Push(v);
            }
        }

        // Step 4:
        // Prepare DP arrays.
        //
        // We need to represent "impossible".
        // Since scores can be negative, we cannot use 0 as a sentinel.
        // We use a very small number NEG_INF.
        //
        // Because values can be as low as -1e9 and n can be 2e5,
        // the minimum possible valid path sum is around -2e14,
        // so long is required and NEG_INF can safely be much smaller.
        const long NEG_INF = long.MinValue / 4;

        var dpUnused = new long[n];
        var dpUsed = new long[n];

        for (int i = 0; i < n; i++)
        {
            dpUnused[i] = NEG_INF;
            dpUsed[i] = NEG_INF;
        }

        // Step 5:
        // Process nodes bottom-up.
        //
        // Reversing the DFS order ensures every child is processed before its parent.
        // That is exactly what tree DP needs.
        for (int idx = orderCount - 1; idx >= 0; idx--)
        {
            int u = order[idx];
            bool leaf = children[u].Count == 0;

            // If u is blocked, then a route cannot "visit" u.
            // Therefore both states are impossible here.
            //
            // Important subtlety:
            // The bypass does NOT make dpUnused[u] or dpUsed[u] valid for a blocked u,
            // because these DP states are defined as routes that START BY VISITING u.
            // If u is blocked and we want to bypass it, that decision is made by u's parent,
            // which skips u entirely and jumps to one of u's children.
            if (isBlocked[u])
            {
                dpUnused[u] = NEG_INF;
                dpUsed[u] = NEG_INF;
                continue;
            }

            // If u is an unblocked leaf, then the only route is the node itself.
            // This is valid whether bypass is still available or already used.
            if (leaf)
            {
                dpUnused[u] = value[u];
                dpUsed[u] = value[u];
                continue;
            }

            // Compute dpUsed[u]:
            //
            // Bypass has already been used before reaching u.
            // So from u onward, we are not allowed to skip any blocked node.
            // Since blocked children are not visitable, only children v with dpUsed[v] possible
            // can be used.
            long bestUsedChild = NEG_INF;

            foreach (int v in children[u])
            {
                if (dpUsed[v] > bestUsedChild)
                {
                    bestUsedChild = dpUsed[v];
                }
            }

            if (bestUsedChild != NEG_INF)
            {
                dpUsed[u] = (long)value[u] + bestUsedChild;
            }

            // Compute dpUnused[u]:
            //
            // Bypass is still available before reaching u.
            // We consider every possible legal next step.
            long bestUnusedContinuation = NEG_INF;

            foreach (int v in children[u])
            {
                if (!isBlocked[v])
                {
                    // Normal move into an unblocked child.
                    //
                    // Since we did not use the bypass yet, it remains available
                    // when entering child v, so we use dpUnused[v].
                    if (dpUnused[v] > bestUnusedContinuation)
                    {
                        bestUnusedContinuation = dpUnused[v];
                    }
                }
                else
                {
                    // Child v is blocked.
                    //
                    // We cannot visit v normally.
                    // The only way to continue through this branch is to use the bypass NOW:
                    // skip v itself and jump directly to one of v's children c.
                    //
                    // After that jump, the bypass has been consumed, so the remainder
                    // must be a route in state "used", i.e. dpUsed[c].
                    //
                    // If blocked child v has no children, or none of its children can lead
                    // to a valid leaf route under dpUsed, then this bypass attempt fails.
                    foreach (int c in children[v])
                    {
                        if (dpUsed[c] > bestUnusedContinuation)
                        {
                            bestUnusedContinuation = dpUsed[c];
                        }
                    }
                }
            }

            if (bestUnusedContinuation != NEG_INF)
            {
                dpUnused[u] = (long)value[u] + bestUnusedContinuation;
            }
        }

        // Step 6:
        // The answer starts from the root.
        //
        // Case A: root is unblocked
        //   Then we must visit root, and bypass is initially unused.
        //   So answer is dpUnused[0].
        //
        // Case B: root is blocked
        //   We cannot visit root.
        //   The only possible start is to use the bypass immediately and jump to one of root's children.
        //   Since bypass becomes used after that jump, the best answer is max(dpUsed[c]) over children c of root.
        //
        // If no valid route exists, return null.
        long answer = NEG_INF;

        if (!isBlocked[0])
        {
            answer = dpUnused[0];
        }
        else
        {
            foreach (int c in children[0])
            {
                if (dpUsed[c] > answer)
                {
                    answer = dpUsed[c];
                }
            }
        }

        return answer == NEG_INF ? null : answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] value1 = { 5, 4, -2, 7, 3, 6 };
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 },
    new[] { 2, 5 }
};
int[] blocked1 = { 1 };

long? result1 = solution.MaximumPathScoreWithOneBlockedSubtreeBypass(value1, edges1, blocked1);
Console.WriteLine(result1.HasValue ? result1.Value.ToString() : "null");

// Example 2
int[] value2 = { 2, -5, 10, 1, 4 };
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 2, 4 }
};
int[] blocked2 = { 1, 2 };

long? result2 = solution.MaximumPathScoreWithOneBlockedSubtreeBypass(value2, edges2, blocked2);
Console.WriteLine(result2.HasValue ? result2.Value.ToString() : "null");