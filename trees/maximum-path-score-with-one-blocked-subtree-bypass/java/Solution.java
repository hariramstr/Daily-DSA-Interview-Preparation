import java.util.*;

/*
 * Maximum Path Score With One Blocked Subtree Bypass
 *
 * Problem Description:
 * You are given a rooted tree with n nodes numbered from 0 to n - 1, rooted at node 0.
 * Each node i has an integer score value[i], which may be positive, zero, or negative.
 * You are also given a list blocked containing some nodes that are considered unavailable.
 * A valid root-to-leaf route normally cannot pass through any blocked node.
 *
 * However, the system allows exactly one special bypass operation on a route: at most once,
 * when your route reaches a blocked node b, you may ignore node b and jump directly to one
 * of its children c, continuing the route from c. This bypass skips only node b itself.
 * It does not remove the blockage of any other blocked nodes, and it cannot be used more
 * than once. If a blocked node has no children, the bypass cannot help there.
 * The score of a route is the sum of value[x] over all visited nodes; a skipped blocked node
 * contributes nothing because it is not visited.
 *
 * Return the maximum possible score of any root-to-leaf route using at most one bypass.
 * If no valid root-to-leaf route exists, return null.
 *
 * A leaf is a node with no children. The route must start at the root. If the root is blocked,
 * you may use the bypass immediately to jump to one of its children.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - -10^9 <= value[i] <= 10^9
 * - edges.length == n - 1
 * - edges describes a valid tree
 * - 0 <= blocked.length <= n
 * - All nodes in blocked are distinct
 *
 * A correct solution uses tree DP with states that distinguish whether the bypass has already
 * been used before reaching the current node.
 */
public class Solution {

    /**
     * A very negative sentinel used to represent "impossible".
     * We keep it far away from any real answer to avoid accidental collisions.
     */
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    /**
     * Computes the maximum root-to-leaf score using at most one bypass.
     *
     * Core DP idea:
     * For each node u, compute two values:
     *
     * 1) dp0[u]:
     *    Best score of a valid route that starts at node u, must VISIT u,
     *    and reaches some leaf in u's subtree, assuming the bypass has NOT been used yet
     *    before arriving at u.
     *
     * 2) dp1[u]:
     *    Best score of a valid route that starts at node u, must VISIT u,
     *    and reaches some leaf in u's subtree, assuming the bypass HAS ALREADY been used
     *    before arriving at u.
     *
     * Transition details:
     *
     * - If u is blocked:
     *   - dp1[u] is impossible, because if bypass was already used earlier, we cannot visit u.
     *   - dp0[u] may still be possible only by using the bypass exactly here:
     *       skip u itself and jump directly to one child v.
     *       Therefore dp0[u] = max(dp1[v]) over children v.
     *       We use dp1[v] because after bypassing u, the bypass is now considered used.
     *       Note carefully: value[u] is NOT added, because u is skipped.
     *   - If blocked u is a leaf, bypass cannot help, so dp0[u] is impossible.
     *
     * - If u is not blocked:
     *   - We must visit u, so value[u] is included.
     *   - If u is a leaf:
     *       dp0[u] = value[u]
     *       dp1[u] = value[u]
     *   - Otherwise:
     *       dp0[u] = value[u] + max(dp0[child])
     *       dp1[u] = value[u] + max(dp1[child])
     *     because the route must continue to exactly one child on the way to a leaf.
     *
     * The answer is dp0[root], because initially we start at the root with bypass unused.
     * If dp0[root] is impossible, return null.
     *
     * We process nodes in reverse topological/tree order using an iterative DFS to avoid
     * recursion depth issues for n up to 200000.
     *
     * @param value score of each node
     * @param edges undirected edges of the tree
     * @param blocked list of blocked nodes
     * @return maximum possible score as a Long, or null if no valid route exists
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public Long maximumPathScore(int[] value, int[][] edges, int[] blocked) {
        int n = value.length;

        // Build adjacency list for the undirected tree.
        List<Integer>[] graph = buildGraph(n, edges);

        // Mark blocked nodes for O(1) checks during DP.
        boolean[] isBlocked = new boolean[n];
        for (int node : blocked) {
            isBlocked[node] = true;
        }

        // We root the tree at node 0.
        // parent[u] stores the parent of u in the rooted tree.
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        // order[] will store nodes in DFS visitation order.
        // Later, we process it backwards so children are handled before parents.
        int[] order = new int[n];
        int orderSize = buildParentAndOrder(graph, parent, order);

        // DP arrays explained in the method Javadoc above.
        long[] dp0 = new long[n];
        long[] dp1 = new long[n];
        Arrays.fill(dp0, NEG_INF);
        Arrays.fill(dp1, NEG_INF);

        // Process nodes bottom-up: reverse of DFS order.
        for (int idx = orderSize - 1; idx >= 0; idx--) {
            int u = order[idx];

            // We need to know whether u is a leaf in the rooted tree.
            // In a rooted tree, u is a leaf if it has no child other than its parent.
            boolean isLeaf = true;

            // These variables track the best child contribution for each DP state.
            long bestChildDp0 = NEG_INF;
            long bestChildDp1 = NEG_INF;

            for (int v : graph[u]) {
                if (v == parent[u]) {
                    continue;
                }
                isLeaf = false;
                bestChildDp0 = Math.max(bestChildDp0, dp0[v]);
                bestChildDp1 = Math.max(bestChildDp1, dp1[v]);
            }

            if (isBlocked[u]) {
                // If u is blocked and bypass already used earlier, we cannot pass through u.
                dp1[u] = NEG_INF;

                // If bypass is still available when we reach blocked u, we may use it here.
                // That means:
                // - skip u itself (do not add value[u])
                // - jump directly to one child
                // - from that child onward, bypass is considered used
                //
                // Therefore we need the best dp1 among children.
                // If u is a blocked leaf, there is no child to jump to, so impossible.
                dp0[u] = isLeaf ? NEG_INF : bestChildDp1;
            } else {
                if (isLeaf) {
                    // A non-blocked leaf is a valid route endpoint.
                    // Whether bypass was used earlier or not does not matter here:
                    // we simply visit this leaf and stop.
                    dp0[u] = value[u];
                    dp1[u] = value[u];
                } else {
                    // For a non-blocked internal node, we must visit u and then choose
                    // exactly one child that leads to the best valid root-to-leaf route.
                    //
                    // If bypass is still unused at u, child route also starts with bypass unused.
                    if (bestChildDp0 != NEG_INF) {
                        dp0[u] = value[u] + bestChildDp0;
                    }

                    // If bypass was already used before u, child route also starts with bypass used.
                    if (bestChildDp1 != NEG_INF) {
                        dp1[u] = value[u] + bestChildDp1;
                    }
                }
            }
        }

        return dp0[0] == NEG_INF ? null : dp0[0];
    }

    /**
     * Builds an adjacency list for the undirected tree.
     *
     * @param n number of nodes
     * @param edges undirected edges
     * @return adjacency list representation of the tree
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public List<Integer>[] buildGraph(int n, int[][] edges) {
        @SuppressWarnings("unchecked")
        List<Integer>[] graph = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        for (int[] edge : edges) {
            int a = edge[0];
            int b = edge[1];
            graph[a].add(b);
            graph[b].add(a);
        }
        return graph;
    }

    /**
     * Performs an iterative DFS from the root (node 0) to:
     * - assign each node's parent in the rooted tree
     * - record a traversal order that can later be reversed for bottom-up DP
     *
     * We use an explicit stack instead of recursion to safely handle very deep trees.
     *
     * @param graph adjacency list of the tree
     * @param parent output array where parent[u] will be stored
     * @param order output array where DFS order will be stored
     * @return number of nodes written into order
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int buildParentAndOrder(List<Integer>[] graph, int[] parent, int[] order) {
        int n = graph.length;
        int[] stack = new int[n];
        int top = 0;
        int size = 0;

        stack[top++] = 0;
        parent[0] = -2; // temporary special marker for root while traversing

        while (top > 0) {
            int u = stack[--top];
            order[size++] = u;

            for (int v : graph[u]) {
                if (v == parent[u]) {
                    continue;
                }
                if (parent[v] != -1) {
                    continue;
                }
                parent[v] = u;
                stack[top++] = v;
            }
        }

        parent[0] = -1; // restore conventional root parent
        return size;
    }

    /**
     * Convenience helper for running and printing one test case.
     *
     * @param value node values
     * @param edges tree edges
     * @param blocked blocked nodes
     * @return computed answer
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public Long runExample(int[] value, int[][] edges, int[] blocked) {
        return maximumPathScore(value, edges, blocked);
    }

    /**
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and prints the results.
     *
     * Expected outputs:
     * Example 1 -> 12
     * Example 2 -> null
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total n across examples)
     * Space complexity: O(total n across examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] value1 = {5, 4, -2, 7, 3, 6};
        int[][] edges1 = {
                {0, 1},
                {0, 2},
                {1, 3},
                {1, 4},
                {2, 5}
        };
        int[] blocked1 = {1};
        Long result1 = solution.runExample(value1, edges1, blocked1);
        System.out.println(result1); // Expected: 12

        int[] value2 = {2, -5, 10, 1, 4};
        int[][] edges2 = {
                {0, 1},
                {1, 2},
                {2, 3},
                {2, 4}
        };
        int[] blocked2 = {1, 2};
        Long result2 = solution.runExample(value2, edges2, blocked2);
        System.out.println(result2); // Expected: null

        // Additional quick sanity checks.

        // Root blocked, bypass immediately to a child.
        int[] value3 = {10, 5, 7};
        int[][] edges3 = {
                {0, 1},
                {0, 2}
        };
        int[] blocked3 = {0};
        Long result3 = solution.runExample(value3, edges3, blocked3);
        System.out.println(result3); // Expected: 7

        // No blocked nodes: ordinary maximum root-to-leaf path sum.
        int[] value4 = {1, 2, 3, 4, -10};
        int[][] edges4 = {
                {0, 1},
                {0, 2},
                {1, 3},
                {2, 4}
        };
        int[] blocked4 = {};
        Long result4 = solution.runExample(value4, edges4, blocked4);
        System.out.println(result4); // Expected: 7
    }
}