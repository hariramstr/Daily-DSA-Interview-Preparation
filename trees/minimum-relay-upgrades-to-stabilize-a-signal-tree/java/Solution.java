import java.util.*;

/*
Problem Title: Minimum Relay Upgrades to Stabilize a Signal Tree

Problem Description:
You are given an undirected tree with n relay stations numbered from 0 to n - 1, rooted at node 0.
Each station i has a non-negative signal noise value noise[i]. A communication path is considered
stable if the greatest common divisor (GCD) of all noise values on that root-to-node path is exactly 1.

In one upgrade operation, you may choose any station and replace its noise value with any positive integer you want.
Your goal is to make every root-to-node path in the tree stable using the minimum number of upgrade operations.

Return the minimum number of stations that must be upgraded.

Notes:
- After upgrading a station, the new value is used in every path passing through that station.
- You may assign different upgraded stations different new values.
- The tree is connected and contains exactly n - 1 edges.
- A path from the root to the root itself is also considered a root-to-node path, so node 0 must also lie on a stable path.

Key observation:
A root-to-node path has GCD 1 if and only if at least one node on that path has value 1 after upgrades.
Why?
- If some node on the path is changed to 1, then the GCD of the whole path becomes 1 immediately.
- If no node on the path is 1, then changing values to arbitrary positive integers still cannot guarantee
  GCD 1 for every path more cheaply than simply placing a 1 somewhere on each root-to-node path.
- Therefore the problem becomes:
  "Choose the minimum number of nodes to upgrade to value 1 so that every root-to-node path contains at least one chosen node."

This is exactly a minimum vertex cut on root-to-leaf paths in a rooted tree.
Because choosing an ancestor covers all descendants, the optimal tree DP is:

For each node u:
- If some ancestor is already chosen as 1, then the whole subtree of u is already covered, cost = 0.
- Otherwise, to cover the path to u itself:
  * if noise[u] == 1 already, then coverage starts here for free, and all descendants are covered too.
  * else we must either:
      1) upgrade u itself (cost 1), which covers the whole subtree, or
      2) if u is not a leaf, do not upgrade u and instead ensure every child subtree gets covered independently.
         This is only valid for internal nodes, because for a leaf the path to the leaf would remain uncovered.

This yields a simple and correct DFS DP.
*/
public class Solution {

    /**
     * Computes the minimum number of stations that must be upgraded so that every root-to-node path
     * has GCD exactly 1.
     *
     * Core reduction:
     * A path has GCD 1 if at least one node on that path has value 1.
     * Since an upgrade may assign any positive integer, assigning upgraded nodes to 1 is always optimal.
     * Therefore we need the minimum number of nodes to set to 1 so that every root-to-node path
     * contains at least one node whose final value is 1 (either originally 1 or upgraded to 1).
     *
     * @param n the number of nodes in the tree
     * @param edges the undirected edges of the tree, where each edge is [u, v]
     * @param noise the original noise values of the nodes
     * @return the minimum number of upgrades required
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minimumRelayUpgrades(int n, int[][] edges, int[] noise) {
        List<Integer>[] graph = buildGraph(n, edges);
        int[] parent = new int[n];
        int[] order = buildParentAndOrder(graph, parent);

        // dp[u] = minimum upgrades needed in subtree rooted at u,
        // assuming NO ancestor of u has already provided a value 1 on the path.
        //
        // If an ancestor already had value 1, then the answer for the whole subtree is 0,
        // so we do not need a second DP state.
        long[] dp = new long[n];

        // Process nodes in reverse DFS/BFS order so children are computed before parent.
        for (int i = n - 1; i >= 0; i--) {
            int u = order[i];

            // If this node already has value 1, then every path from root to any node in this subtree
            // passes through u and therefore already has a 1 on it.
            // So no upgrades are needed below.
            if (noise[u] == 1) {
                dp[u] = 0;
                continue;
            }

            boolean isLeaf = true;
            long sumChildren = 0;

            for (int v : graph[u]) {
                if (v == parent[u]) {
                    continue;
                }
                isLeaf = false;
                sumChildren += dp[v];
            }

            // Option 1: upgrade this node itself to 1.
            long upgradeHere = 1;

            // Option 2: do not upgrade this node.
            // Then every child subtree must independently place a 1 somewhere,
            // because the path to any descendant in that child subtree still needs coverage.
            //
            // This option is invalid for a leaf, because the path ending at this leaf would remain uncovered.
            long dontUpgradeHere = isLeaf ? Long.MAX_VALUE / 4 : sumChildren;

            dp[u] = Math.min(upgradeHere, dontUpgradeHere);
        }

        return (int) dp[0];
    }

    /**
     * Builds an adjacency list for the tree.
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
        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            graph[u].add(v);
            graph[v].add(u);
        }
        return graph;
    }

    /**
     * Builds parent information and a traversal order starting from root 0.
     * The returned order can be processed in reverse to obtain a bottom-up DP order.
     *
     * @param graph adjacency list of the tree
     * @param parent output array where parent[u] is filled
     * @return traversal order from root outward
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildParentAndOrder(List<Integer>[] graph, int[] parent) {
        int n = graph.length;
        Arrays.fill(parent, -1);

        int[] order = new int[n];
        int idx = 0;

        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        parent[0] = -2; // special marker for root during traversal

        while (!stack.isEmpty()) {
            int u = stack.pop();
            order[idx++] = u;

            for (int v : graph[u]) {
                if (v == parent[u]) {
                    continue;
                }
                parent[v] = u;
                stack.push(v);
            }
        }

        parent[0] = -1;
        return order;
    }

    /**
     * Computes the greatest common divisor of two integers.
     * This helper is included for completeness and educational value,
     * although the final optimized solution does not need to explicitly use GCD.
     *
     * @param a first integer
     * @param b second integer
     * @return gcd(a, b)
     * Time complexity: O(log(min(a, b)))
     * Space complexity: O(1)
     */
    public int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return Math.abs(a);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total n across demonstrated tests)
     * Space complexity: O(total n across demonstrated tests)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int n1 = 5;
        int[][] edges1 = {
                {0, 1},
                {0, 2},
                {1, 3},
                {1, 4}
        };
        int[] noise1 = {6, 10, 15, 9, 25};
        System.out.println(sol.minimumRelayUpgrades(n1, edges1, noise1)); // Expected: 1

        int n2 = 4;
        int[][] edges2 = {
                {0, 1},
                {1, 2},
                {1, 3}
        };
        int[] noise2 = {6, 10, 7, 15};
        System.out.println(sol.minimumRelayUpgrades(n2, edges2, noise2)); // Expected: 1

        int n3 = 1;
        int[][] edges3 = {};
        int[] noise3 = {1};
        System.out.println(sol.minimumRelayUpgrades(n3, edges3, noise3)); // Expected: 0

        int n4 = 1;
        int[][] edges4 = {};
        int[] noise4 = {8};
        System.out.println(sol.minimumRelayUpgrades(n4, edges4, noise4)); // Expected: 1

        int n5 = 6;
        int[][] edges5 = {
                {0, 1},
                {0, 2},
                {1, 3},
                {1, 4},
                {2, 5}
        };
        int[] noise5 = {2, 3, 1, 4, 5, 6};
        System.out.println(sol.minimumRelayUpgrades(n5, edges5, noise5)); // Expected: 1
    }
}