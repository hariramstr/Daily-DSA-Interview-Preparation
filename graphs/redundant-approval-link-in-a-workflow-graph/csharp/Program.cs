/*
Title: Redundant Approval Link in a Workflow Graph

Problem Description:
A company models an approval workflow as a directed graph with nodes labeled from 1 to n.
Each directed edge [u, v] means task u must be completed immediately before task v can proceed.

The workflow was originally intended to form a valid rooted structure:
- Exactly one task is the root
- Every other task has exactly one direct prerequisite
- All tasks are reachable from the root by following directed edges

However, due to a configuration mistake, one extra directed edge was added.

Your task is to return the single edge that should be removed so the graph becomes a valid rooted workflow again.
If multiple edges could be removed, return the one that appears last in the input order among the valid choices.

A valid rooted workflow must satisfy all of the following:
1. Exactly one node has indegree 0.
2. Every other node has indegree 1.
3. The graph contains no directed cycle.
4. All nodes belong to one connected workflow when viewed from the root through edge directions.

Input is given as an array edges of length n, where each element is a pair [u, v].
You may assume the final correct graph uses the same n nodes and exactly n - 1 edges.

Constraints:
- 2 <= n <= 100000
- edges.length == n
- 1 <= u, v <= n
- u != v
- It is guaranteed that removing exactly one edge can restore a valid rooted workflow.

Examples:
1) edges = [[1,2],[1,3],[2,3]]
   Output: [2,3]

2) edges = [[1,2],[2,3],[3,4],[4,1],[1,5]]
   Output: [4,1]
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n * α(n))
    - We scan the edges a constant number of times.
    - Union-Find operations are almost constant time due to path compression.

    Space Complexity: O(n)
    - We store parent information for indegree checking.
    - We store Union-Find parent array.
    */
    public int[] FindRedundantDirectedConnection(int[][] edges)
    {
        int n = edges.Length;

        // parentOfNode[v] stores the parent that currently points to node v.
        // In a valid rooted tree, every node except the root must have exactly one parent.
        // So if we ever see a node v that already has a parent, then v has indegree 2,
        // which is one of the two important failure cases in this problem.
        int[] parentOfNode = new int[n + 1];

        // These two variables will store the two competing edges if we discover
        // a node with two parents.
        //
        // candidate1 = the earlier edge that first assigned a parent to the node
        // candidate2 = the later edge that caused the indegree-2 conflict
        //
        // Example:
        // edges: [1,3], [2,3]
        // candidate1 = [1,3]
        // candidate2 = [2,3]
        int[]? candidate1 = null;
        int[]? candidate2 = null;

        // ------------------------------------------------------------
        // STEP 1: Detect whether any node has two parents.
        // ------------------------------------------------------------
        //
        // Why this matters:
        // In this problem, the graph differs from a rooted tree by exactly one extra edge.
        // That means the invalidity must come from one of these patterns:
        //
        // A) A node has two parents
        // B) There is a directed cycle
        // C) Both happen at the same time
        //
        // We first detect case A because it strongly narrows down which edge(s)
        // could possibly be removed.
        for (int i = 0; i < n; i++)
        {
            int u = edges[i][0];
            int v = edges[i][1];

            if (parentOfNode[v] == 0)
            {
                // This is the first incoming edge we have seen for node v.
                // Record u as v's parent.
                parentOfNode[v] = u;
            }
            else
            {
                // We found a node v that already had a parent.
                // So v has indegree 2.
                //
                // The earlier edge is [parentOfNode[v], v]
                // The later edge is [u, v]
                candidate1 = new int[] { parentOfNode[v], v };
                candidate2 = new int[] { u, v };

                // Important:
                // We do NOT overwrite parentOfNode[v] here.
                // We only record the conflict.
                //
                // Later, when we run Union-Find, we will intentionally skip candidate2
                // and test whether the remaining edges form a valid rooted tree.
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Run Union-Find to detect cycles.
        // ------------------------------------------------------------
        //
        // Core idea:
        // - If there was NO indegree-2 conflict, then the answer is simply the edge
        //   that closes the cycle.
        // - If there WAS an indegree-2 conflict, then:
        //      * Try ignoring candidate2 (the later conflicting edge).
        //      * If the graph is valid without candidate2, then candidate2 is the answer.
        //      * Otherwise, candidate1 must be the answer.
        //
        // Why Union-Find works here:
        // Although the graph is directed, the standard known solution uses Union-Find
        // to detect whether adding an edge connects two nodes already in the same set.
        // In this specific problem structure (tree + one extra edge), that is enough
        // to identify the problematic edge when combined with the indegree-2 logic above.
        UnionFind uf = new UnionFind(n);

        for (int i = 0; i < n; i++)
        {
            int u = edges[i][0];
            int v = edges[i][1];

            // If we found a node with two parents, we temporarily skip candidate2.
            // This lets us test whether removing candidate2 alone fixes the graph.
            if (candidate2 != null && u == candidate2[0] && v == candidate2[1])
            {
                continue;
            }

            // If u and v are already connected in the Union-Find structure,
            // then adding this edge creates a cycle.
            //
            // There are two sub-cases:
            //
            // 1) No indegree-2 conflict exists:
            //    Then this edge is the redundant one, and we return it immediately.
            //
            // 2) An indegree-2 conflict exists:
            //    Since we skipped candidate2 and still found a cycle,
            //    that means candidate1 is part of the cycle and must be removed.
            if (!uf.Union(u, v))
            {
                if (candidate1 == null)
                {
                    // Pure cycle case.
                    return new int[] { u, v };
                }

                // Both indegree-2 and cycle case.
                return candidate1;
            }
        }

        // If we get here, then after skipping candidate2, no cycle was found.
        // That means candidate2 is the edge whose removal restores a valid rooted tree.
        //
        // This exactly matches Example 1:
        // edges = [[1,2],[1,3],[2,3]]
        // Node 3 has two parents: [1,3] and [2,3]
        // Skip [2,3], remaining edges form a valid rooted tree, so answer is [2,3].
        if (candidate2 != null)
        {
            return candidate2;
        }

        // The problem guarantees there is always exactly one removable edge,
        // so this line should never be reached for valid inputs.
        return Array.Empty<int>();
    }

    private class UnionFind
    {
        private readonly int[] parent;

        public UnionFind(int size)
        {
            parent = new int[size + 1];

            // Initially, every node is its own parent.
            // This means each node starts in its own separate set.
            for (int i = 1; i <= size; i++)
            {
                parent[i] = i;
            }
        }

        public int Find(int x)
        {
            // Path compression:
            // We recursively find the root representative of x's set,
            // then directly attach x to that root.
            //
            // Why this helps:
            // It makes future Find operations much faster.
            if (parent[x] != x)
            {
                parent[x] = Find(parent[x]);
            }

            return parent[x];
        }

        public bool Union(int a, int b)
        {
            int rootA = Find(a);
            int rootB = Find(b);

            // If both nodes already have the same root, then they are already connected.
            // Adding this edge would create a cycle in the underlying structure we are building.
            if (rootA == rootB)
            {
                return false;
            }

            // Merge the two sets.
            parent[rootB] = rootA;
            return true;
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[][] edges1 =
{
    new[] { 1, 2 },
    new[] { 1, 3 },
    new[] { 2, 3 }
};

int[] result1 = solution.FindRedundantDirectedConnection(edges1);
Console.WriteLine($"Example 1 Result: [{result1[0]},{result1[1]}]");

// Example 2
int[][] edges2 =
{
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 3, 4 },
    new[] { 4, 1 },
    new[] { 1, 5 }
};

int[] result2 = solution.FindRedundantDirectedConnection(edges2);
Console.WriteLine($"Example 2 Result: [{result2[0]},{result2[1]}]");

// Additional demo: both indegree-2 and cycle case
int[][] edges3 =
{
    new[] { 2, 1 },
    new[] { 3, 1 },
    new[] { 4, 2 },
    new[] { 1, 4 }
};

int[] result3 = solution.FindRedundantDirectedConnection(edges3);
Console.WriteLine($"Example 3 Result: [{result3[0]},{result3[1]}]");