import java.util.*;

/*
Title: Redundant Approval Link in a Workflow Graph

Problem Description:
A company models an approval workflow as a directed graph with nodes labeled from 1 to n.
Each directed edge [u, v] means task u must be completed immediately before task v can proceed.
The workflow was originally intended to form a valid rooted structure: exactly one task is the root,
every other task has exactly one direct prerequisite, and all tasks are reachable from the root by
following directed edges. However, due to a configuration mistake, one extra directed edge was added.

Your task is to return the single edge that should be removed so the graph becomes a valid rooted
workflow again. If multiple edges could be removed, return the one that appears last in the input
order among the valid choices.

A valid rooted workflow must satisfy all of the following:
1. Exactly one node has indegree 0.
2. Every other node has indegree 1.
3. The graph contains no directed cycle.
4. All nodes belong to one connected workflow when viewed from the root through edge directions.

Input is given as an array edges of length n, where each element is a pair [u, v]. You may assume
the final correct graph uses the same n nodes and exactly n - 1 edges.

Constraints:
- 2 <= n <= 100000
- edges.length == n
- 1 <= u, v <= n
- u != v
- It is guaranteed that removing exactly one edge can restore a valid rooted workflow.

Examples:
1) Input: edges = [[1,2],[1,3],[2,3]]
   Output: [2,3]

2) Input: edges = [[1,2],[2,3],[3,4],[4,1],[1,5]]
   Output: [4,1]
*/

/**
 * Solution for finding the redundant directed edge that prevents a directed graph
 * from being a valid rooted tree.
 *
 * This is the classic "redundant connection in a directed graph" problem.
 * The key observation is that exactly one extra edge was added, so only two kinds
 * of structural problems can happen:
 *
 * 1. A node gets two parents.
 * 2. A directed cycle exists.
 * 3. Both can happen at the same time.
 *
 * We handle all cases efficiently using:
 * - A parent tracking array to detect whether some node has indegree 2.
 * - A Disjoint Set Union (Union-Find) structure to detect cycles.
 */
public class Solution {

    /**
     * Finds the single redundant directed edge whose removal restores the graph
     * into a valid rooted tree.
     *
     * Detailed idea:
     * 1. Scan all edges and detect whether some node has two incoming edges.
     *    - If yes, remember both conflicting edges:
     *      candidate1 = earlier edge pointing to that node
     *      candidate2 = later edge pointing to that node
     *
     * 2. Run Union-Find over the edges, but if candidate2 exists, temporarily skip it.
     *    This lets us test whether the remaining graph is already a valid tree.
     *
     * 3. There are then two possibilities:
     *    - If a cycle is still found even after skipping candidate2, then candidate1
     *      must be removed.
     *    - If no cycle is found after skipping candidate2, then candidate2 is the
     *      redundant edge and should be removed.
     *
     * 4. If no node had two parents, then the problem is purely a cycle problem.
     *    In that case, the edge that closes the cycle is the answer.
     *
     * @param edges the directed edges of the graph, where edges.length == n and nodes are labeled 1..n
     * @return the redundant edge that should be removed
     * Time complexity: O(n * alpha(n)), effectively near O(n)
     * Space complexity: O(n)
     */
    public int[] findRedundantDirectedConnection(int[][] edges) {
        int n = edges.length;

        // parentOf[v] stores the parent currently assigned to node v while scanning edges.
        // If we encounter another edge also pointing to v, then v has two parents.
        int[] parentOf = new int[n + 1];

        // These two variables store the two conflicting edges when a node has indegree 2.
        // candidate1 = the earlier edge that first assigned the parent
        // candidate2 = the later edge that causes the conflict
        int[] candidate1 = null;
        int[] candidate2 = null;

        // ------------------------------------------------------------
        // STEP 1: Detect whether any node has two parents.
        // ------------------------------------------------------------
        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];

            // If this node has not yet been assigned a parent, record it.
            if (parentOf[to] == 0) {
                parentOf[to] = from;
            } else {
                // We found a node with two incoming edges.
                // The earlier edge is [parentOf[to], to]
                // The later edge is [from, to]
                candidate1 = new int[] { parentOf[to], to };
                candidate2 = new int[] { from, to };

                // Important:
                // We do NOT overwrite parentOf[to] here because candidate1 should remain
                // the original parent edge. We simply remember both candidates.
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Run Union-Find to detect cycles.
        //
        // If candidate2 exists, we skip it temporarily. This is the standard trick:
        // - If skipping candidate2 removes all problems, then candidate2 is the answer.
        // - If a cycle still exists, then candidate1 is the answer.
        // ------------------------------------------------------------
        UnionFind uf = new UnionFind(n);

        for (int[] edge : edges) {
            // If there was a two-parent conflict, skip the later conflicting edge
            // during this pass.
            if (candidate2 != null && edge[0] == candidate2[0] && edge[1] == candidate2[1]) {
                continue;
            }

            int from = edge[0];
            int to = edge[1];

            // In a rooted tree, adding an edge between two nodes already connected
            // in Union-Find indicates a cycle.
            if (!uf.union(from, to)) {
                // Case A: No two-parent conflict existed.
                // Then this edge is simply the one creating the cycle.
                if (candidate1 == null) {
                    return edge;
                }

                // Case B: A two-parent conflict existed, and even after skipping candidate2,
                // we still found a cycle. That means candidate1 is the real bad edge.
                return candidate1;
            }
        }

        // ------------------------------------------------------------
        // STEP 3: If we reach here, no cycle was found in the graph after skipping candidate2.
        // Therefore candidate2 is the redundant edge.
        // ------------------------------------------------------------
        return candidate2;
    }

    /**
     * Converts an edge array like [u, v] into a readable string.
     *
     * @param edge the edge to format
     * @return a string representation such as "[u, v]"
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public String edgeToString(int[] edge) {
        return "[" + edge[0] + ", " + edge[1] + "]";
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * This method also prints the expected outputs so it is easy to compare.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding the algorithm calls
     * Space complexity: O(1), excluding the input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] edges1 = {
            {1, 2},
            {1, 3},
            {2, 3}
        };

        int[][] edges2 = {
            {1, 2},
            {2, 3},
            {3, 4},
            {4, 1},
            {1, 5}
        };

        int[] result1 = solution.findRedundantDirectedConnection(edges1);
        int[] result2 = solution.findRedundantDirectedConnection(edges2);

        System.out.println("Example 1 Input: [[1,2],[1,3],[2,3]]");
        System.out.println("Expected Output: [2, 3]");
        System.out.println("Actual Output:   " + solution.edgeToString(result1));
        System.out.println();

        System.out.println("Example 2 Input: [[1,2],[2,3],[3,4],[4,1],[1,5]]");
        System.out.println("Expected Output: [4, 1]");
        System.out.println("Actual Output:   " + solution.edgeToString(result2));
    }

    /**
     * A standard Disjoint Set Union (Union-Find) data structure with:
     * - Path compression in find()
     * - Union by rank
     *
     * Even though the original graph is directed, Union-Find is still useful here
     * for detecting whether adding an edge would create a cycle in the underlying
     * structure we are validating.
     */
    static class UnionFind {
        private final int[] parent;
        private final int[] rank;

        /**
         * Creates a Union-Find structure for nodes 1..size.
         *
         * @param size the maximum node label
         * @return nothing
         * Time complexity: O(size)
         * Space complexity: O(size)
         */
        UnionFind(int size) {
            parent = new int[size + 1];
            rank = new int[size + 1];

            for (int i = 0; i <= size; i++) {
                parent[i] = i;
            }
        }

        /**
         * Finds the representative (root) of the set containing x.
         * Uses path compression to flatten the structure for efficiency.
         *
         * @param x the node whose set representative is requested
         * @return the representative of x
         * Time complexity: Amortized O(alpha(n))
         * Space complexity: O(1) auxiliary, ignoring recursion stack because this implementation is iterative via recursion depth effectively tiny; practical amortized near constant
         */
        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        /**
         * Unions the sets containing a and b.
         *
         * If a and b are already in the same set, then adding the corresponding edge
         * would create a cycle in the structure being checked, so this method returns false.
         *
         * Otherwise, it merges the two sets and returns true.
         *
         * @param a first node
         * @param b second node
         * @return true if the union succeeded, false if a and b were already connected
         * Time complexity: Amortized O(alpha(n))
         * Space complexity: O(1)
         */
        boolean union(int a, int b) {
            int rootA = find(a);
            int rootB = find(b);

            if (rootA == rootB) {
                return false;
            }

            if (rank[rootA] < rank[rootB]) {
                parent[rootA] = rootB;
            } else if (rank[rootA] > rank[rootB]) {
                parent[rootB] = rootA;
            } else {
                parent[rootB] = rootA;
                rank[rootA]++;
            }

            return true;
        }
    }
}