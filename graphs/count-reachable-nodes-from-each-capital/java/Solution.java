/*
 * Title: Count Reachable Nodes from Each Capital
 *
 * Problem Description:
 * You are given a country represented as an undirected tree with n cities numbered
 * from 0 to n-1. City 0 is the national capital. Each city may also have a designated
 * 'regional capital' — specifically, some cities are marked as regional capitals in an
 * array capitals, where capitals[i] = 1 means city i is a regional capital, and
 * capitals[i] = 0 means it is not.
 *
 * For each regional capital, determine how many cities (including itself) are reachable
 * from it without passing through any other regional capital or the national capital (city 0).
 *
 * You are given:
 * - An integer n — the number of cities.
 * - A 2D integer array edges of size n-1, where edges[i] = [u, v] represents a
 *   bidirectional road between cities u and v.
 * - An integer array capitals of size n.
 *
 * Return an array result where result[i] is the count of reachable cities for the
 * i-th regional capital (in the order they appear, i.e., cities where capitals[i] = 1).
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - edges.length == n - 1
 * - 0 <= u, v < n
 * - The input forms a valid tree.
 * - capitals.length == n
 * - capitals[i] is 0 or 1
 * - capitals[0] is always 0 (city 0 is the national capital, not a regional one)
 */

import java.util.*;

/**
 * Solution class for counting reachable nodes from each regional capital
 * in an undirected tree, without crossing other capitals or the national capital.
 */
public class Solution {

    /**
     * Counts the number of cities reachable from each regional capital without
     * passing through any other regional capital or the national capital (city 0).
     *
     * <p>Algorithm Overview:
     * 1. Build an adjacency list representation of the tree from the edges array.
     * 2. Identify all "blocked" nodes: city 0 (national capital) and all regional capitals.
     * 3. For each regional capital, perform a BFS/DFS starting from that capital,
     *    but do NOT traverse into any blocked node (except the starting capital itself).
     * 4. Count all visited nodes (including the starting capital) and add to result.
     *
     * @param n        the number of cities (nodes in the tree)
     * @param edges    2D array of size n-1 representing bidirectional roads
     * @param capitals array of size n where capitals[i]=1 means city i is a regional capital
     * @return an int array where result[i] is the reachable city count for the i-th regional capital
     *
     * Time Complexity:  O(n * n) in the worst case — for each of the O(n) regional capitals,
     *                   we may traverse up to O(n) nodes in the BFS.
     * Space Complexity: O(n) for the adjacency list and BFS visited/queue structures.
     */
    public int[] countReachableNodes(int n, int[][] edges, int[] capitals) {

        // -----------------------------------------------------------------------
        // STEP 1: Build the adjacency list for the undirected tree.
        // An adjacency list maps each node to its list of neighboring nodes.
        // -----------------------------------------------------------------------
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        // For each edge [u, v], add v to u's neighbor list and u to v's neighbor list
        // because the tree is undirected (roads go both ways).
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            adj.get(u).add(v);
            adj.get(v).add(u);
        }

        // -----------------------------------------------------------------------
        // STEP 2: Create a boolean array "isBlocked" to mark nodes that act as
        // barriers during traversal. A node is blocked if:
        //   - It is city 0 (the national capital), OR
        //   - It is a regional capital (capitals[i] == 1)
        //
        // When doing BFS from a regional capital X, we start AT X (so X itself
        // is visited), but we do NOT enter any other blocked node.
        // -----------------------------------------------------------------------
        boolean[] isBlocked = new boolean[n];

        // City 0 is always the national capital — mark it blocked
        isBlocked[0] = true;

        // Mark all regional capitals as blocked
        for (int i = 0; i < n; i++) {
            if (capitals[i] == 1) {
                isBlocked[i] = true;
            }
        }

        // -----------------------------------------------------------------------
        // STEP 3: Collect the list of regional capitals in order.
        // We'll iterate through the capitals array and record indices where
        // capitals[i] == 1.
        // -----------------------------------------------------------------------
        List<Integer> regionalCapitals = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (capitals[i] == 1) {
                regionalCapitals.add(i);
            }
        }

        // -----------------------------------------------------------------------
        // STEP 4: For each regional capital, perform a BFS to count all reachable
        // cities (including the capital itself) without crossing blocked nodes.
        // -----------------------------------------------------------------------
        int[] result = new int[regionalCapitals.size()];

        for (int idx = 0; idx < regionalCapitals.size(); idx++) {
            int startCity = regionalCapitals.get(idx);

            // Count of cities reachable from this regional capital
            int count = bfsCount(startCity, isBlocked, adj, n);

            result[idx] = count;
        }

        return result;
    }

    /**
     * Performs a Breadth-First Search (BFS) starting from the given city,
     * counting all reachable cities without entering any blocked node
     * (except the start city itself, which is always counted).
     *
     * <p>Key insight: The start city IS a blocked node (it's a regional capital),
     * but we still begin our traversal there. We simply don't enter OTHER blocked
     * nodes when expanding neighbors.
     *
     * @param startCity  the regional capital from which BFS begins
     * @param isBlocked  boolean array marking national capital and all regional capitals
     * @param adj        adjacency list of the tree
     * @param n          total number of cities (used for visited array size)
     * @return the count of cities reachable from startCity without crossing blocked nodes
     *
     * Time Complexity:  O(n) — each node is visited at most once in BFS
     * Space Complexity: O(n) — for the visited array and BFS queue
     */
    private int bfsCount(int startCity, boolean[] isBlocked, List<List<Integer>> adj, int n) {

        // Track which nodes we've already visited to avoid revisiting
        boolean[] visited = new boolean[n];

        // BFS queue — stores nodes to process
        Queue<Integer> queue = new LinkedList<>();

        // Start BFS from the regional capital itself
        queue.offer(startCity);
        visited[startCity] = true;

        int count = 0; // Will count all visited nodes

        // Standard BFS loop
        while (!queue.isEmpty()) {
            // Dequeue the front node
            int current = queue.poll();

            // Count this node as reachable
            count++;

            // Explore all neighbors of the current node
            for (int neighbor : adj.get(current)) {

                // Skip if already visited
                if (visited[neighbor]) {
                    continue;
                }

                // KEY RULE: Do NOT traverse into a blocked node.
                // Blocked nodes are: city 0 (national capital) and all regional capitals.
                // Since the start city itself is blocked but we began there manually,
                // this check correctly prevents entering OTHER blocked nodes.
                if (isBlocked[neighbor]) {
                    continue;
                }

                // Mark as visited and enqueue for further exploration
                visited[neighbor] = true;
                queue.offer(neighbor);
            }
        }

        return count;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // n = 6
        // edges = [[0,1],[1,2],[1,3],[3,4],[3,5]]
        // capitals = [0,1,0,1,0,0]
        //
        // Tree structure:
        //       0
        //       |
        //       1  <-- regional capital
        //      / \
        //     2   3  <-- regional capital
        //        / \
        //       4   5
        //
        // Regional capitals: city 1 and city 3
        //
        // From city 1: neighbors are 0 (blocked=national capital) and 2 and 3 (blocked=regional).
        //   - Start at 1 (count=1)
        //   - Neighbor 0: blocked, skip
        //   - Neighbor 2: not blocked, visit (count=2)
        //   - Neighbor 3: blocked, skip
        //   → count = 2
        //
        // From city 3: neighbors are 1 (blocked=regional capital), 4, 5.
        //   - Start at 3 (count=1)
        //   - Neighbor 1: blocked, skip
        //   - Neighbor 4: not blocked, visit (count=2)
        //   - Neighbor 5: not blocked, visit (count=3)
        //   → count = 3
        //
        // Expected output: [2, 3]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int n1 = 6;
        int[][] edges1 = {{0, 1}, {1, 2}, {1, 3}, {3, 4}, {3, 5}};
        int[] capitals1 = {0, 1, 0, 1, 0, 0};

        int[] result1 = solution.countReachableNodes(n1, edges1, capitals1);
        System.out.print("Output: [");
        for (int i = 0; i < result1.length; i++) {
            System.out.print(result1[i]);
            if (i < result1.length - 1) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println("Expected: [2, 3]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // n = 4
        // edges = [[0,1],[1,2],[2,3]]
        // capitals = [0,1,0,0]
        //
        // Tree structure:
        //   0 -- 1 -- 2 -- 3
        //        ^
        //        regional capital
        //
        // Regional capitals: only city 1
        //
        // From city 1: neighbors are 0 (blocked=national capital) and 2.
        //   - Start at 1 (count=1)
        //   - Neighbor 0: blocked, skip
        //   - Neighbor 2: not blocked, visit (count=2)
        //     - From 2: neighbor 1 (visited), neighbor 3 (not blocked, visit, count=3)
        //   → count = 3
        //
        // Expected output: [3]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int n2 = 4;
        int[][] edges2 = {{0, 1}, {1, 2}, {2, 3}};
        int[] capitals2 = {0, 1, 0, 0};

        int[] result2 = solution.countReachableNodes(n2, edges2, capitals2);
        System.out.print("Output: [");
        for (int i = 0; i < result2.length; i++) {
            System.out.print(result2[i]);
            if (i < result2.length - 1) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println("Expected: [3]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3: No regional capitals
        // n = 3, edges = [[0,1],[1,2]], capitals = [0,0,0]
        // Expected output: [] (empty array)
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3 (No regional capitals) ===");
        int n3 = 3;
        int[][] edges3 = {{0, 1}, {1, 2}};
        int[] capitals3 = {0, 0, 0};

        int[] result3 = solution.countReachableNodes(n3, edges3, capitals3);
        System.out.print("Output: [");
        for (int i = 0; i < result3.length; i++) {
            System.out.print(result3[i]);
            if (i < result3.length - 1) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println("Expected: []");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4: Single city (n=1), no edges, no regional capitals
        // n = 1, edges = [], capitals = [0]
        // Expected output: []
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4 (Single city) ===");
        int n4 = 1;
        int[][] edges4 = {};
        int[] capitals4 = {0};

        int[] result4 = solution.countReachableNodes(n4, edges4, capitals4);
        System.out.print("Output: [");
        for (int i = 0; i < result4.length; i++) {
            System.out.print(result4[i]);
            if (i < result4.length - 1) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println("Expected: []");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 5: Multiple regional capitals in a line
        // n = 5, edges = [[0,1],[1,2],[2,3],[3,4]]
        // capitals = [0,1,0,1,0]
        //
        // Tree: 0 -- 1 -- 2 -- 3 -- 4
        //            ^         ^
        //         reg cap   reg cap
        //
        // From city 1: neighbors 0 (blocked), 2 (not blocked)
        //   - 1 (count=1), 2 (count=2), from 2: neighbor 1(visited), 3(blocked,skip)
        //   → count = 2
        //
        // From city 3: neighbors 2 (not blocked), 4 (not blocked)
        //   - 3 (count=1), 2 (count=2), 4 (count=3)
        //   - from 2: neighbor 1(blocked,skip), 3(visited)
        //   → count = 3
        //
        // Expected output: [2, 3]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 5 (Line graph with two regional capitals) ===");
        int n5 = 5;
        int[][] edges5 = {{0, 1}, {1, 2}, {2, 3}, {3, 4}};
        int[] capitals5 = {0, 1, 0, 1, 0};

        int[] result5 = solution.countReachableNodes(n5, edges5, capitals5);
        System.out.print("Output: [");
        for (int i = 0; i < result