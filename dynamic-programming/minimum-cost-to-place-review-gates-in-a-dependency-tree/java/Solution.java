import java.util.*;

/*
 * Title: Minimum Cost to Place Review Gates in a Dependency Tree
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a software dependency tree with n modules numbered from 0 to n - 1, rooted at module 0.
 * Each module has a non-negative installation risk cost risk[i]. A review gate can be placed on any module i,
 * paying risk[i]. A gate on module i protects module i itself, its parent, and all of its direct children.
 * Your task is to place review gates so that every module in the tree is protected by at least one gate,
 * while minimizing the total cost.
 *
 * Formally, you are given an array risk of length n and an edge list edges of length n - 1 describing an
 * undirected tree rooted at 0. If a gate is placed at node i, then node i, every neighbor of i at distance 1
 * upward or downward along the rooted tree, are considered protected. Every node must end up protected.
 *
 * Return the minimum possible total cost.
 *
 * This is not a simple greedy problem: choosing a cheap gate deep in the tree may fail to protect its ancestors
 * correctly, while placing a gate high in the tree may save multiple expensive descendants. An efficient solution
 * should use tree dynamic programming with carefully designed states describing whether a node is protected by
 * itself, by its parent, or must be protected by one of its children.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 0 <= risk[i] <= 1000000000
 * - edges.length == n - 1
 * - edges represents a valid tree
 * - The answer fits in a 64-bit signed integer
 *
 * Example 1:
 * Input: risk = [5,2,4,6], edges = [[0,1],[1,2],[1,3]]
 * Output: 2
 * Explanation: Place one gate at module 1. It protects modules 0, 1, 2, and 3, so all modules are covered
 * with total cost 2.
 *
 * Example 2:
 * Input: risk = [7,3,8,2,5,1], edges = [[0,1],[0,2],[1,3],[1,4],[2,5]]
 * Output: 4
 * Explanation: Place gates at modules 1 and 5. Gate 1 protects 0, 1, 3, and 4. Gate 5 protects 2 and 5.
 * Every module is protected, and the total cost is 3 + 1 = 4.
 */

public class Solution {

    /**
     * A very large value used as "infinity" for impossible DP states.
     * We keep it safely below Long.MAX_VALUE so additions do not overflow.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum total risk cost needed so that every node in the rooted tree is protected.
     *
     * Core DP idea:
     * For each node u, we compute three states:
     *
     * 1) dp[u][0] = minimum cost in subtree(u) if we place a gate at u.
     *    - Then u is protected by itself.
     *    - Its children are also protected by u from above.
     *
     * 2) dp[u][1] = minimum cost in subtree(u) if u has NO gate, but u is protected by its parent.
     *    - Since parent protects u, u itself is already covered.
     *    - However, children are NOT protected by u (because u has no gate), so each child must be handled
     *      either by placing a gate on that child or by having that child be protected by one of its own children.
     *
     * 3) dp[u][2] = minimum cost in subtree(u) if u has NO gate and is NOT protected by parent,
     *    so u must be protected by at least one of its children.
     *    - Therefore, at least one child must have a gate.
     *    - Other children can be handled similarly to state 1 for their own root relation.
     *
     * Transition details:
     *
     * If u has a gate:
     *   Every child v sees its parent protected by a gate at u, so for child v we may choose:
     *   - v has a gate: dp[v][0]
     *   - v has no gate and is protected by parent u: dp[v][1]
     *   We take min(dp[v][0], dp[v][1]) for each child.
     *
     * If u is protected by parent and has no gate:
     *   For each child v:
     *   - v may have a gate: dp[v][0]
     *   - or v may have no gate but then must be protected by one of its own children: dp[v][2]
     *   We take min(dp[v][0], dp[v][2]) for each child.
     *
     * If u must be protected by a child:
     *   Similar to previous case, except at least one child must choose dp[v][0].
     *   So we first sum min(dp[v][0], dp[v][2]) for all children, then force at least one child into state 0
     *   by paying the smallest extra cost among children where needed.
     *
     * Root handling:
     * The root has no parent, so it cannot be in state 1.
     * Final answer = min(dp[root][0], dp[root][2]).
     *
     * This is the classic minimum weighted dominating set on a tree.
     *
     * @param risk the non-negative gate placement cost for each node
     * @param edges the undirected edges of the tree
     * @return the minimum total cost to protect every node
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long minimumCost(int[] risk, int[][] edges) {
        int n = risk.length;
        if (n == 1) {
            return risk[0];
        }

        List<Integer>[] graph = buildGraph(n, edges);
        int[] parent = new int[n];
        int[] order = buildParentAndOrder(graph, parent);

        long[][] dp = new long[n][3];

        /*
         * We process nodes in reverse DFS/BFS order so that children are processed before their parent.
         * This is a standard bottom-up tree DP technique without recursion, which is important because
         * n can be as large as 100000 and recursive DFS could overflow the Java stack.
         */
        for (int idx = n - 1; idx >= 0; idx--) {
            int u = order[idx];

            long withGate = risk[u];   // dp[u][0]
            long coveredByParent = 0;  // dp[u][1]
            long coveredByChild = 0;   // base for dp[u][2]

            boolean hasChild = false;
            long minExtraToForceChildGate = INF;

            for (int v : graph[u]) {
                if (v == parent[u]) {
                    continue;
                }
                hasChild = true;

                /*
                 * State 0: place gate at u.
                 * Child v can either:
                 * - place gate at v, or
                 * - skip gate at v because parent u already protects v.
                 */
                withGate += Math.min(dp[v][0], dp[v][1]);

                /*
                 * State 1: u has no gate, but parent protects u.
                 * Child v is NOT protected by u, so child v must either:
                 * - place gate at v, or
                 * - be protected by one of its own children.
                 */
                long bestWithoutParentHelp = Math.min(dp[v][0], dp[v][2]);
                coveredByParent += bestWithoutParentHelp;

                /*
                 * State 2: same local options for each child as state 1,
                 * but globally we must ensure at least one child actually places a gate
                 * so that u becomes protected from below.
                 */
                coveredByChild += bestWithoutParentHelp;

                /*
                 * If bestWithoutParentHelp already chooses dp[v][0], extra cost may be 0.
                 * Otherwise, this tells us how much more we must pay to force child v to have a gate.
                 */
                long extra = dp[v][0] - bestWithoutParentHelp;
                if (extra < minExtraToForceChildGate) {
                    minExtraToForceChildGate = extra;
                }
            }

            dp[u][0] = withGate;
            dp[u][1] = coveredByParent;

            /*
             * For dp[u][2], u must be protected by at least one child.
             * If u is a leaf, this is impossible because it has no child that can protect it.
             */
            if (!hasChild) {
                dp[u][2] = INF;
            } else {
                dp[u][2] = coveredByChild + minExtraToForceChildGate;
            }
        }

        return Math.min(dp[0][0], dp[0][2]);
    }

    /**
     * Builds an undirected adjacency list for the tree.
     *
     * @param n the number of nodes
     * @param edges the tree edges
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
     * Builds the parent array and a traversal order starting from root 0.
     * The returned order can be processed in reverse to obtain a bottom-up order.
     *
     * We use an iterative traversal to avoid recursion depth issues on large trees.
     *
     * @param graph the adjacency list of the tree
     * @param parent output array where parent[u] will be filled
     * @return an order of nodes reachable from root 0
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildParentAndOrder(List<Integer>[] graph, int[] parent) {
        int n = graph.length;
        int[] order = new int[n];
        Arrays.fill(parent, -1);

        int[] stack = new int[n];
        int top = 0;
        stack[top++] = 0;
        parent[0] = -2; // special marker for root during traversal

        int size = 0;
        while (top > 0) {
            int u = stack[--top];
            order[size++] = u;

            for (int v : graph[u]) {
                if (parent[v] != -1) {
                    continue;
                }
                parent[v] = u;
                stack[top++] = v;
            }
        }

        parent[0] = -1;
        return order;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n) for each demonstration call
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] risk1 = {5, 2, 4, 6};
        int[][] edges1 = {
            {0, 1},
            {1, 2},
            {1, 3}
        };
        long result1 = solution.minimumCost(risk1, edges1);
        System.out.println(result1); // Expected: 2

        int[] risk2 = {7, 3, 8, 2, 5, 1};
        int[][] edges2 = {
            {0, 1},
            {0, 2},
            {1, 3},
            {1, 4},
            {2, 5}
        };
        long result2 = solution.minimumCost(risk2, edges2);
        System.out.println(result2); // Expected: 4

        int[] risk3 = {10};
        int[][] edges3 = {};
        long result3 = solution.minimumCost(risk3, edges3);
        System.out.println(result3); // Expected: 10
    }
}