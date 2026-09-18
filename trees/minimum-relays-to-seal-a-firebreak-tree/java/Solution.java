import java.util.*;

/*
 * Title: Minimum Relays to Seal a Firebreak Tree
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * You are given an undirected tree with n zones numbered from 0 to n - 1.
 * A wildfire can start at any leaf zone and spreads one edge per minute toward the interior of the tree.
 * You may install emergency relay beacons on some zones. A beacon protects its own zone,
 * its parent, and all of its direct children. If the fire reaches a protected zone,
 * it is stopped immediately and cannot pass through that zone.
 *
 * Your task is to compute the minimum number of beacons needed so that, regardless of which leaf
 * the fire starts from, the fire can never reach the designated command center root r.
 *
 * The tree is rooted at r only for defining parent-child relationships used by beacon coverage.
 * A beacon on node u covers: u, parent(u) if it exists, and every child of u.
 * You may place a beacon on any node. The fire may start from any leaf, including leaves very deep in the tree.
 * You must guarantee that every path from any leaf to the root contains at least one protected node.
 *
 * Return the minimum number of beacons required.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 0 <= r < n
 * - edges.length == n - 1
 * - edges[i] = [ui, vi]
 * - The input graph is a valid tree.
 * - An O(n) or O(n log n) solution is expected.
 *
 * Example 1:
 * Input: n = 7, edges = [[0,1],[0,2],[1,3],[1,4],[2,5],[2,6]], r = 0
 * Output: 2
 * Explanation: Place beacons at nodes 1 and 2. Then every root-to-leaf path contains a protected node.
 * No single beacon can block all four leaves from reaching the root.
 *
 * Example 2:
 * Input: n = 8, edges = [[3,0],[3,1],[3,4],[4,2],[4,5],[5,6],[5,7]], r = 3
 * Output: 2
 * Explanation: One optimal placement is at nodes 4 and 5. Beacon 4 protects nodes 3, 4, 2, and 5.
 * Beacon 5 protects nodes 4, 5, 6, and 7. Every path from any leaf to root 3 intersects at least one protected node,
 * so the fire cannot reach the command center.
 *
 * Key Insight:
 * We do NOT need to protect every node. We only need every leaf-to-root path to contain at least one protected node.
 *
 * A beacon placed at node u protects:
 * - u itself
 * - parent(u)
 * - all direct children of u
 *
 * Therefore, for any node x, x becomes protected if there is a beacon on:
 * - x
 * - parent(x)
 * - one of x's children
 *
 * This is a tree DP problem with local interactions between parent/child.
 *
 * We root the tree at r and compute a dynamic programming table bottom-up.
 *
 * DP state:
 * For each node u, we compute dp[u][pb][ub], where:
 * - pb = 0/1 : whether parent(u) has a beacon
 * - ub = 0/1 : whether u itself has a beacon
 *
 * dp[u][pb][ub] = minimum number of beacons needed in the subtree of u
 *                 (including possibly u itself if ub=1),
 *                 so that every leaf-to-root path entirely inside this subtree is blocked
 *                 before it can go above u.
 *
 * Important interpretation:
 * - If u is already protected by parent beacon, or by its own beacon, or by some child beacon,
 *   then any fire coming from below cannot pass through u upward.
 * - If u is not protected, then every child-subtree path must already be blocked strictly below u,
 *   because fire would otherwise pass through u to its parent.
 *
 * Transition idea:
 * For each child v of u, we choose whether v has a beacon or not.
 * The child sees parentBeacon = ub.
 *
 * If u is already protected by parent or self, then child paths may either:
 * - be blocked inside v-subtree, or
 * - reach v and then u, because u itself will stop them.
 *
 * If u is not yet protected by parent/self, then after processing children:
 * - either at least one child has a beacon, which protects u, or
 * - every child-subtree must be blocked internally before reaching u.
 *
 * This leads to a small knapsack-like merge with two aggregate states:
 * - whether some child beacon has been chosen so far
 * - whether all processed children are internally blocked if needed
 *
 * Because each node has only 4 DP values and each edge is processed O(1) times,
 * the total complexity is O(n).
 */
public class Solution {

    /**
     * A large value used as "infinity" for impossible DP states.
     */
    private static final long INF = (long) 4e18;

    /**
     * Computes the minimum number of beacons needed so that every leaf-to-root path
     * contains at least one protected node.
     *
     * Algorithm overview:
     * 1. Build the undirected adjacency list.
     * 2. Root the tree at r using an iterative DFS/BFS to avoid recursion depth issues.
     * 3. Process nodes in postorder so children are solved before parents.
     * 4. For each node u, compute dp[u][parentBeacon][uBeacon].
     * 5. The final answer is min(dp[root][0][0], dp[root][0][1]) because the root has no parent.
     *
     * @param n the number of nodes in the tree
     * @param edges the undirected edges of the tree, where each edge is [u, v]
     * @param r the chosen root of the tree
     * @return the minimum number of beacons required
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minimumBeacons(int n, int[][] edges, int r) {
        if (n == 1) {
            // Single-node tree:
            // The root is also a leaf. A fire can start there and immediately reaches the root.
            // So the root itself must be protected, which requires one beacon on the root.
            return 1;
        }

        List<Integer>[] graph = buildGraph(n, edges);
        int[] parent = new int[n];
        int[] order = buildRootedOrder(graph, r, parent);

        // children[u] will be iterated by scanning graph[u] and skipping parent[u],
        // so we do not need a separate children list.

        // dp[u][pb][ub]
        long[][][] dp = new long[n][2][2];

        // Process in reverse order => postorder.
        for (int idx = n - 1; idx >= 0; idx--) {
            int u = order[idx];

            // Determine whether u is a leaf in the rooted tree.
            boolean isLeaf = true;
            for (int v : graph[u]) {
                if (v != parent[u]) {
                    isLeaf = false;
                    break;
                }
            }

            for (int pb = 0; pb <= 1; pb++) {
                for (int ub = 0; ub <= 1; ub++) {
                    long result;

                    if (isLeaf) {
                        result = solveLeafState(pb, ub);
                    } else {
                        result = solveInternalState(u, pb, ub, graph, parent, dp);
                    }

                    // If u itself has a beacon, count it here.
                    if (result >= INF / 2) {
                        dp[u][pb][ub] = INF;
                    } else {
                        dp[u][pb][ub] = result + ub;
                    }
                }
            }
        }

        long ans = Math.min(dp[r][0][0], dp[r][0][1]);
        return (int) ans;
    }

    /**
     * Builds the adjacency list of the undirected tree.
     *
     * @param n the number of nodes
     * @param edges the tree edges
     * @return adjacency list representation of the tree
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public List<Integer>[] buildGraph(int n, int[][] edges) {
        List<Integer>[] graph = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        for (int[] e : edges) {
            int a = e[0];
            int b = e[1];
            graph[a].add(b);
            graph[b].add(a);
        }
        return graph;
    }

    /**
     * Roots the tree at r and returns a traversal order such that reversing it gives postorder.
     *
     * We use an iterative DFS to avoid recursion stack overflow for large trees.
     *
     * @param graph adjacency list of the tree
     * @param r the root
     * @param parent output array where parent[u] is the parent of u in the rooted tree
     * @return an order of nodes discovered from the root
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildRootedOrder(List<Integer>[] graph, int r, int[] parent) {
        int n = graph.length;
        Arrays.fill(parent, -2);

        int[] order = new int[n];
        int[] stack = new int[n];
        int top = 0;
        int size = 0;

        stack[top++] = r;
        parent[r] = -1;

        while (top > 0) {
            int u = stack[--top];
            order[size++] = u;

            for (int v : graph[u]) {
                if (parent[v] != -2) {
                    continue;
                }
                parent[v] = u;
                stack[top++] = v;
            }
        }

        return order;
    }

    /**
     * Solves the DP value for a leaf node.
     *
     * For a leaf u:
     * - If u is protected by parent beacon (pb=1) or by its own beacon (ub=1),
     *   then a fire starting at u is stopped immediately at u.
     * - Otherwise, there is no child that could protect u, and no lower node exists to block the path.
     *   So this state is impossible.
     *
     * @param parentBeacon whether the parent of the leaf has a beacon
     * @param selfBeacon whether the leaf itself has a beacon
     * @return minimum additional beacons needed inside the leaf subtree excluding the leaf's own beacon cost
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long solveLeafState(int parentBeacon, int selfBeacon) {
        if (parentBeacon == 1 || selfBeacon == 1) {
            return 0L;
        }
        return INF;
    }

    /**
     * Solves the DP value for an internal node u with fixed values of:
     * - whether parent(u) has a beacon
     * - whether u itself has a beacon
     *
     * Detailed transition logic:
     *
     * Let "uProtectedByParentOrSelf" = (pb == 1 || ub == 1).
     *
     * For each child v, we have two possible choices:
     * 1. Put a beacon on v:
     *    cost = dp[v][ub][1]
     *    This also protects u.
     *
     * 2. Do not put a beacon on v:
     *    cost = dp[v][ub][0]
     *    Then whether paths from v can still reach u depends on the internal arrangement in v-subtree.
     *
     * We need to combine children carefully.
     *
     * Aggregate DP over children:
     * bestNoChildBeacon:
     *   minimum cost after processing some children with NO child beacon chosen yet,
     *   and every processed child-subtree already blocked before reaching u.
     *
     * bestHasChildBeacon:
     *   minimum cost after processing some children with AT LEAST ONE child beacon chosen,
     *   so u is protected by a child beacon.
     *   Once u is protected, other child-subtrees may either be blocked internally or simply reach u,
     *   because u will stop the fire.
     *
     * If u is already protected by parent/self:
     * - We can allow all children to choose their individually cheapest valid state,
     *   because any path that reaches u is stopped at u.
     *
     * If u is not protected by parent/self:
     * - Then in the final result, either:
     *   A) some child beacon exists, protecting u, or
     *   B) no child beacon exists, but then every child-subtree must already be blocked below u.
     *
     * The merge below handles both cases uniformly.
     *
     * @param u the current node
     * @param pb whether parent(u) has a beacon
     * @param ub whether u has a beacon
     * @param graph adjacency list
     * @param parent parent array of rooted tree
     * @param dp already-computed DP table for descendants
     * @return minimum additional beacons needed in u's subtree excluding u's own beacon cost
     * Time complexity: O(number of children of u)
     * Space complexity: O(1) extra
     */
    public long solveInternalState(int u, int pb, int ub, List<Integer>[] graph, int[] parent, long[][][] dp) {
        boolean protectedByParentOrSelf = (pb == 1 || ub == 1);

        long bestNoChildBeacon = 0L;
        long bestHasChildBeacon = INF;

        for (int v : graph[u]) {
            if (v == parent[u]) {
                continue;
            }

            // Child v sees whether its parent has a beacon: that is exactly ub.
            long childWithoutBeacon = dp[v][ub][0];
            long childWithBeacon = dp[v][ub][1];

            long nextNoChildBeacon = INF;
            long nextHasChildBeacon = INF;

            if (protectedByParentOrSelf) {
                // Since u is already protected, a path from v may safely reach u and stop there.
                // Therefore for this child we may choose either:
                // - v has beacon
                // - v has no beacon
                // whichever is cheaper and valid.
                long freeChoice = Math.min(childWithoutBeacon, childWithBeacon);

                if (bestNoChildBeacon < INF / 2 && freeChoice < INF / 2) {
                    nextNoChildBeacon = Math.min(nextNoChildBeacon, bestNoChildBeacon + freeChoice);
                }
                if (bestHasChildBeacon < INF / 2 && freeChoice < INF / 2) {
                    nextHasChildBeacon = Math.min(nextHasChildBeacon, bestHasChildBeacon + freeChoice);
                }

                // We may also explicitly choose a child beacon and move into "has child beacon" state.
                if (bestNoChildBeacon < INF / 2 && childWithBeacon < INF / 2) {
                    nextHasChildBeacon = Math.min(nextHasChildBeacon, bestNoChildBeacon + childWithBeacon);
                }
                if (bestHasChildBeacon < INF / 2 && childWithBeacon < INF / 2) {
                    nextHasChildBeacon = Math.min(nextHasChildBeacon, bestHasChildBeacon + childWithBeacon);
                }
            } else {
                // u is NOT protected by parent or self.
                //
                // If we still have no child beacon after processing this child,
                // then this child-subtree must be blocked internally before reaching u.
                // That means we can only use childWithoutBeacon in a way that is valid for v-subtree.
                if (bestNoChildBeacon < INF / 2 && childWithoutBeacon < INF / 2) {
                    nextNoChildBeacon = Math.min(nextNoChildBeacon, bestNoChildBeacon + childWithoutBeacon);
                }

                // If we choose a beacon on this child, then u becomes protected.
                if (bestNoChildBeacon < INF / 2 && childWithBeacon < INF / 2) {
                    nextHasChildBeacon = Math.min(nextHasChildBeacon, bestNoChildBeacon + childWithBeacon);
                }

                // Once some child beacon has already been chosen, u is protected.
                // So for later children we can freely choose min(valid no-beacon, valid beacon).
                long freeChoice = Math.min(childWithoutBeacon, childWithBeacon);
                if (bestHasChildBeacon < INF / 2 && freeChoice < INF / 2) {
                    nextHasChildBeacon = Math.min(nextHasChildBeacon, bestHasChildBeacon + freeChoice);
                }
            }

            bestNoChildBeacon = nextNoChildBeacon;
            bestHasChildBeacon = nextHasChildBeacon;
        }

        if (protectedByParentOrSelf) {
            // u is already protected, so we do not require any child beacon.
            return Math.min(bestNoChildBeacon, bestHasChildBeacon);
        } else {
            // u is not protected by parent/self.
            // Valid outcomes:
            // - some child beacon protects u, OR
            // - no child beacon, but all child-subtrees are blocked below u.
            //
            // Both are represented by these two aggregate states.
            return Math.min(bestNoChildBeacon, bestHasChildBeacon);
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total nodes in demonstrated examples)
     * Space complexity: O(total nodes in demonstrated examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 7;
        int[][] edges1 = {
                {0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 5}, {2, 6}
        };
        int r1 = 0;
        int ans1 = solution.minimumBeacons(n1, edges1, r1);
        System.out.println(ans1); // Expected: 2

        int n2 = 8;
        int[][] edges2 = {
                {3, 0}, {3, 1}, {3, 4}, {4, 2}, {4, 5}, {5, 6}, {5, 7}
        };
        int r2 = 3;
        int ans2 = solution.minimumBeacons(n2, edges2, r2);
        System.out.println(ans2); // Expected: 2

        int n3 = 1;
        int[][] edges3 = {};
        int r3 = 0;
        int ans3 = solution.minimumBeacons(n3