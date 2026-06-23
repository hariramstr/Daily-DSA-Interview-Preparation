import java.util.*;

/*
 * Title: Minimum Access Revocations to Isolate Sensitive Databases
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * A company models internal service-to-service access as an undirected graph with n systems
 * labeled from 0 to n - 1. Each edge [u, v] means system u can directly communicate with system v.
 * Some systems host sensitive databases and are listed in an array sensitive.
 *
 * For compliance, no two sensitive systems are allowed to remain in the same connected component
 * after access revocations are applied.
 *
 * In one operation, you may revoke exactly one existing access link (remove one edge from the graph).
 * Return the minimum number of access links that must be revoked so that every connected component
 * contains at most one sensitive system.
 *
 * If a component already contains zero or one sensitive system, it is valid and requires no changes.
 * If a component contains multiple sensitive systems, you may remove any edges inside that component
 * to split it into smaller valid components.
 *
 * Your task is to compute the minimum number of revoked edges needed over the entire graph.
 *
 * This problem is guaranteed to have a graph with no self-loops and no duplicate edges.
 * The graph may be disconnected initially.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 0 <= edges.length <= 200000
 * - edges[i].length == 2
 * - 0 <= u, v < n
 * - 1 <= sensitive.length <= n
 * - All values in sensitive are distinct
 *
 * Key Insight:
 * For any connected component:
 * - Let s = number of nodes in the component
 * - Let e = number of edges in the component
 * - Let k = number of sensitive nodes in the component
 *
 * If k <= 1, no edge must be removed.
 *
 * If k >= 2, we need to split this connected component into at least k connected pieces,
 * because each final piece may contain at most one sensitive node.
 *
 * Starting from one connected component, every removed edge can increase the number of connected
 * components by at most 1. Therefore, to go from 1 component to at least k components,
 * we need at least k - 1 removed edges.
 *
 * This lower bound is always achievable:
 * - Take any spanning tree of the component.
 * - In that tree, repeatedly cut an edge that separates at least one sensitive node from the rest.
 * - A tree with k marked nodes can always be split into k components each containing exactly one
 *   marked node using exactly k - 1 cuts.
 * - Extra cycle edges never force additional removals, because they can simply remain inside the
 *   resulting valid components if they do not reconnect different pieces, and the spanning-tree
 *   argument shows k - 1 cuts are sufficient in the original graph.
 *
 * Therefore, the minimum number of removed edges for a connected component is:
 * - 0, if k <= 1
 * - k - 1, if k >= 2
 *
 * So the total answer is simply:
 * Sum over all connected components of max(0, sensitiveCountInComponent - 1)
 *
 * We compute connected components with an iterative BFS/DFS to avoid recursion depth issues.
 */
public class Solution {

    /**
     * Computes the minimum number of access links (edges) that must be revoked so that
     * every connected component contains at most one sensitive node.
     *
     * Algorithm:
     * 1. Build an adjacency list for the undirected graph.
     * 2. Mark which nodes are sensitive.
     * 3. Traverse each connected component using iterative BFS.
     * 4. Count how many sensitive nodes are inside that component.
     * 5. If a component contains k sensitive nodes, it contributes max(0, k - 1) to the answer.
     * 6. Sum this over all components.
     *
     * Why this is correct:
     * - A connected component with k sensitive nodes must be split into at least k components,
     *   so at least k - 1 edge removals are necessary.
     * - This is also sufficient, so the minimum is exactly k - 1.
     *
     * @param n the number of nodes labeled from 0 to n - 1
     * @param edges the undirected edges of the graph, where each edge is [u, v]
     * @param sensitive the list of sensitive nodes
     * @return the minimum number of edges that must be removed
     *
     * Time complexity: O(n + m), where m = edges.length
     * Space complexity: O(n + m)
     */
    public int minimumAccessRevocations(int n, int[][] edges, int[] sensitive) {
        // Step 1:
        // Build a fast lookup table so we can instantly check whether a node is sensitive.
        boolean[] isSensitive = new boolean[n];
        for (int node : sensitive) {
            isSensitive[node] = true;
        }

        // Step 2:
        // Build the adjacency list representation of the graph.
        //
        // Because the graph is undirected:
        // - if there is an edge [u, v]
        // - then u is a neighbor of v
        // - and v is a neighbor of u
        //
        // We use ArrayList<Integer>[] because it is simple and efficient enough
        // for the given constraints.
        @SuppressWarnings("unchecked")
        List<Integer>[] graph = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }

        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            graph[u].add(v);
            graph[v].add(u);
        }

        // Step 3:
        // Track which nodes have already been visited during component traversal.
        boolean[] visited = new boolean[n];

        // This variable accumulates the final answer.
        long answer = 0L;

        // We use an iterative queue-based BFS to avoid recursion depth problems
        // on very large graphs.
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        // Step 4:
        // Explore every node. If it has not been visited yet, it starts a new connected component.
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            // Begin BFS for this connected component.
            visited[start] = true;
            queue.offer(start);

            // Count how many sensitive nodes are in this component.
            int sensitiveCount = 0;

            while (!queue.isEmpty()) {
                int current = queue.poll();

                // If the current node is sensitive, count it.
                if (isSensitive[current]) {
                    sensitiveCount++;
                }

                // Visit all neighbors.
                for (int neighbor : graph[current]) {
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        queue.offer(neighbor);
                    }
                }
            }

            // Step 5:
            // If this component has k sensitive nodes, it contributes k - 1 removals.
            // If k is 0 or 1, it contributes 0.
            if (sensitiveCount > 1) {
                answer += (sensitiveCount - 1L);
            }
        }

        // The constraints guarantee the result fits comfortably in int,
        // but we used long during accumulation for extra safety.
        return (int) answer;
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total n + total m) across the demonstrated examples
     * Space complexity: O(total n + total m) across the demonstrated examples
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 7;
        int[][] edges1 = {
            {0, 1},
            {1, 2},
            {1, 3},
            {3, 4},
            {3, 5},
            {5, 6}
        };
        int[] sensitive1 = {2, 4, 6};
        int result1 = solution.minimumAccessRevocations(n1, edges1, sensitive1);
        System.out.println("Example 1 Output: " + result1); // Expected: 2

        // Example 2
        int n2 = 8;
        int[][] edges2 = {
            {0, 1},
            {1, 2},
            {2, 0},
            {2, 3},
            {4, 5},
            {5, 6},
            {6, 7}
        };
        int[] sensitive2 = {0, 3, 7};
        int result2 = solution.minimumAccessRevocations(n2, edges2, sensitive2);
        System.out.println("Example 2 Output: " + result2); // Expected: 1

        // Additional quick sanity checks

        // Single component, one sensitive node -> already valid
        int n3 = 4;
        int[][] edges3 = {
            {0, 1},
            {1, 2},
            {2, 3}
        };
        int[] sensitive3 = {2};
        int result3 = solution.minimumAccessRevocations(n3, edges3, sensitive3);
        System.out.println("Sanity Check 1 Output: " + result3); // Expected: 0

        // One component with four sensitive nodes -> need 3 removals
        int n4 = 5;
        int[][] edges4 = {
            {0, 1},
            {1, 2},
            {2, 3},
            {3, 4}
        };
        int[] sensitive4 = {0, 1, 3, 4};
        int result4 = solution.minimumAccessRevocations(n4, edges4, sensitive4);
        System.out.println("Sanity Check 2 Output: " + result4); // Expected: 3
    }
}