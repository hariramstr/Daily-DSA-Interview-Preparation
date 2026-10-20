import java.util.*;

/*
 * Title: Maximum Recovered Signal by Cutting One Corrupted Branch
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * You are given a rooted tree with nodes numbered from 0 to n - 1, where node 0 is the root.
 * Each node i has an integer signal value value[i], which may be positive, zero, or negative.
 * A monitoring system may disconnect exactly one subtree by cutting the edge between some node x
 * and its parent. If you cut at node x, then the entire subtree rooted at x is removed from the tree.
 * The root cannot be cut because it has no parent.
 *
 * After at most one cut, the recovered signal of the system is defined as the maximum root-to-leaf
 * path sum in the remaining tree. If no cut is performed, the tree stays unchanged. Your task is to
 * return the largest possible recovered signal.
 *
 * Note that after removing a subtree, some internal node may become a leaf if all of its children
 * were removed. The remaining structure is still a rooted tree containing node 0.
 *
 * Constraints:
 * - 2 <= n <= 2 * 10^5
 * - edges.length == n - 1
 * - 0 <= parent of each non-root node < n
 * - -10^9 <= value[i] <= 10^9
 * - The answer fits in a signed 64-bit integer
 *
 * Key Insight:
 * We want the maximum root-to-leaf path sum after removing at most one subtree.
 *
 * A root-to-leaf path in the remaining tree can end in two ways:
 * 1) It ends at an original leaf and we do not cut on that path.
 * 2) It ends at some internal node u because we cut exactly one child subtree of u,
 *    and after that cut u has no remaining children. This means u originally had exactly one child,
 *    and we cut that only child.
 *
 * Therefore, the only way a cut can improve the answer is by deleting the unique child of some node,
 * turning that node into a new leaf. If a node has 2 or more children, cutting only one subtree
 * cannot make it a leaf. If a node is already a leaf, no cut is needed.
 *
 * So the answer is simply:
 * maximum over all nodes u such that (u is an original leaf) OR (u has exactly one child and u != root? actually root also allowed to remain)
 * of the root-to-u path sum.
 *
 * More precisely:
 * - Every original leaf contributes its root-to-leaf sum.
 * - Every node with exactly one child contributes its root-to-node sum, because we may cut that only child.
 *   This is valid for root as well: cutting root's only child is allowed because we cut the edge between
 *   that child and root, not the root itself.
 *
 * We compute parent/children structure, then do one iterative DFS/BFS from root to get root-to-node sums,
 * and evaluate the above condition for each node.
 */
public class Solution {

    /**
     * Computes the largest possible recovered signal after removing at most one subtree.
     *
     * The algorithm builds the rooted tree from the undirected edges, computes the number of children
     * of each node and the root-to-node path sum for every node, then checks which nodes can be the
     * endpoint of the best remaining root-to-leaf path:
     *
     * - Original leaves are always valid endpoints without any cut.
     * - Nodes with exactly one child are also valid endpoints, because cutting that only child subtree
     *   makes the node become a leaf.
     *
     * The answer is the maximum root-to-node sum among those valid endpoints.
     *
     * @param value the signal value at each node
     * @param edges undirected edges of the tree, where node 0 is the root
     * @return the maximum recovered signal after at most one cut
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumRecoveredSignal(int[] value, int[][] edges) {
        int n = value.length;

        // Build an undirected adjacency list first.
        // Since the input is a tree, this is enough to later root it at node 0.
        List<Integer>[] graph = buildGraph(n, edges);

        // parent[i] = parent of node i in the rooted tree.
        // We initialize with -2 to mean "unvisited".
        int[] parent = new int[n];
        Arrays.fill(parent, -2);
        parent[0] = -1;

        // childrenCount[i] = number of children of node i in the rooted tree.
        int[] childrenCount = new int[n];

        // pathSum[i] = sum of values along the path from root 0 to node i.
        long[] pathSum = new long[n];
        pathSum[0] = value[0];

        // We use an iterative DFS to avoid recursion depth issues for n up to 2 * 10^5.
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);

        while (!stack.isEmpty()) {
            int u = stack.pop();

            // Explore all neighbors. Any unvisited neighbor becomes a child of u.
            for (int v : graph[u]) {
                if (parent[v] != -2) {
                    continue;
                }

                parent[v] = u;
                childrenCount[u]++;
                pathSum[v] = pathSum[u] + value[v];
                stack.push(v);
            }
        }

        long answer = Long.MIN_VALUE;

        // Now evaluate every node as a possible endpoint of the best remaining root-to-leaf path.
        for (int u = 0; u < n; u++) {
            boolean isOriginalLeaf = childrenCount[u] == 0;
            boolean canBecomeLeafByOneCut = childrenCount[u] == 1;

            // If u is already a leaf, we can keep the tree unchanged and end at u.
            // If u has exactly one child, we can cut that only child subtree and then u becomes a leaf.
            if (isOriginalLeaf || canBecomeLeafByOneCut) {
                answer = Math.max(answer, pathSum[u]);
            }
        }

        return answer;
    }

    /**
     * Builds an undirected adjacency list for the tree.
     *
     * @param n the number of nodes
     * @param edges the undirected edges
     * @return adjacency list representation of the tree
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public List<Integer>[] buildGraph(int n, int[][] edges) {
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
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding the solver calls
     * Space complexity: O(1) for the fixed demo inputs, excluding the solver calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] value1 = {5, 4, -10, 3, 2, 7};
        int[][] edges1 = {
                {0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 5}
        };
        long result1 = solution.maximumRecoveredSignal(value1, edges1);
        System.out.println(result1); // Expected: 12

        int[] value2 = {8, -5, 6, -2, 4, -1, 3};
        int[][] edges2 = {
                {0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 5}, {5, 6}
        };
        long result2 = solution.maximumRecoveredSignal(value2, edges2);
        System.out.println(result2); // Expected: 16
    }
}