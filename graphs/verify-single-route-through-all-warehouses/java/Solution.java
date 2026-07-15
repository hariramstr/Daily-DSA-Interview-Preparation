import java.util.*;

/*
Problem Title: Verify Single Route Through All Warehouses

Problem Description:
A logistics company stores package transfers as an undirected graph. There are n warehouses labeled from 0 to n - 1,
and each road in roads[i] = [a, b] means warehouse a is directly connected to warehouse b.

The company wants to know whether its road map forms exactly one simple connected route through all warehouses.
In other words, starting from one end, you should be able to visit every warehouse exactly once by following the roads,
without encountering any branch and without leaving any warehouse disconnected.

Return true if the graph forms a single path that uses all n warehouses. Otherwise, return false.

A graph is considered a valid single route if:
1. Every warehouse belongs to the same connected component.
2. Exactly two warehouses have degree 1 when n > 1 (the two ends of the route).
3. Every other warehouse has degree 2.
4. If n == 1, the graph with no roads is also valid.

Constraints:
- 1 <= n <= 1000
- 0 <= roads.length <= 1000
- roads[i].length == 2
- 0 <= a, b < n
- a != b
- There are no duplicate roads.

Example 1:
Input: n = 4, roads = [[0,1],[1,2],[2,3]]
Output: true
Explanation: All 4 warehouses are connected in a straight chain. The end warehouses 0 and 3 have degree 1,
and the middle warehouses 1 and 2 have degree 2.

Example 2:
Input: n = 4, roads = [[0,1],[1,2],[1,3]]
Output: false
Explanation: The graph is connected, but warehouse 1 has degree 3, which creates a branch.
Therefore it is not a single simple route through all warehouses.
*/

public class Solution {

    /**
     * Determines whether the given undirected graph forms exactly one simple path
     * that includes all warehouses.
     *
     * A valid graph must satisfy:
     * 1. If n == 1, there must be no roads.
     * 2. If n > 1, exactly two nodes must have degree 1.
     * 3. All other nodes must have degree 2.
     * 4. The graph must be connected.
     *
     * @param n the number of warehouses labeled from 0 to n - 1
     * @param roads the undirected roads where each element is [a, b]
     * @return true if the graph is a single simple path through all warehouses; false otherwise
     * Time complexity: O(n + m), where m is the number of roads
     * Space complexity: O(n + m)
     */
    public boolean isSingleRoute(int n, int[][] roads) {
        // Special case:
        // If there is only one warehouse, the only valid "single path" is a graph with no roads.
        // Example:
        // n = 1, roads = []
        // This is valid because the single warehouse itself forms the whole path.
        if (n == 1) {
            return roads.length == 0;
        }

        // For a simple path with n > 1, the number of edges must be exactly n - 1.
        // Why?
        // A path visiting all n nodes exactly once always has exactly n - 1 edges.
        //
        // This check is not strictly required if we already verify degree rules + connectivity,
        // but it is a very useful early validation and makes the logic clearer.
        if (roads.length != n - 1) {
            return false;
        }

        // Build an adjacency list to represent the undirected graph.
        // adjacency.get(i) will contain all neighbors directly connected to warehouse i.
        List<List<Integer>> adjacency = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjacency.add(new ArrayList<>());
        }

        // degree[i] stores how many roads touch warehouse i.
        int[] degree = new int[n];

        // Fill the adjacency list and degree array.
        for (int[] road : roads) {
            int a = road[0];
            int b = road[1];

            adjacency.get(a).add(b);
            adjacency.get(b).add(a);

            degree[a]++;
            degree[b]++;
        }

        // Count how many warehouses have degree 1.
        // In a valid path with n > 1:
        // - exactly 2 nodes are endpoints => degree 1
        // - all other nodes are internal => degree 2
        int degreeOneCount = 0;

        for (int i = 0; i < n; i++) {
            // If a node has degree 1, it may be one of the two endpoints.
            if (degree[i] == 1) {
                degreeOneCount++;
            }
            // If a node has degree 2, that is fine for an internal node.
            else if (degree[i] == 2) {
                // Valid internal node, nothing else to do.
            }
            // Any other degree makes the graph invalid as a single simple path.
            // Examples:
            // - degree 0 => disconnected node
            // - degree 3 or more => branching
            else {
                return false;
            }
        }

        // For n > 1, a valid path must have exactly two endpoints.
        if (degreeOneCount != 2) {
            return false;
        }

        // Now verify connectivity.
        // Even if degree rules look correct, we still must ensure all nodes belong to one connected component.
        //
        // We start BFS from one endpoint (a node with degree 1).
        // In a valid path, traversing from one endpoint should eventually reach every node.
        int startNode = findEndpoint(degree);

        boolean[] visited = new boolean[n];
        int visitedCount = bfsCountVisited(startNode, adjacency, visited);

        // The graph is valid only if every warehouse was reached.
        return visitedCount == n;
    }

    /**
     * Finds one endpoint of the path, meaning a node with degree 1.
     * This method assumes such a node exists when called.
     *
     * @param degree the degree array where degree[i] is the number of neighbors of node i
     * @return the index of a node with degree 1
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int findEndpoint(int[] degree) {
        // Scan all nodes and return the first node whose degree is 1.
        // In a valid path with n > 1, there are exactly two such nodes.
        for (int i = 0; i < degree.length; i++) {
            if (degree[i] == 1) {
                return i;
            }
        }

        // This should never happen if the caller has already validated the degree conditions.
        // Returning -1 is a safe fallback.
        return -1;
    }

    /**
     * Performs a breadth-first search (BFS) starting from the given node
     * and counts how many nodes are reachable.
     *
     * @param startNode the node from which BFS begins
     * @param adjacency the adjacency list of the graph
     * @param visited a boolean array used to mark visited nodes
     * @return the number of nodes visited during BFS
     * Time complexity: O(n + m), where m is the number of edges
     * Space complexity: O(n)
     */
    public int bfsCountVisited(int startNode, List<List<Integer>> adjacency, boolean[] visited) {
        // Standard BFS queue.
        Queue<Integer> queue = new LinkedList<>();

        // Mark the start node as visited and add it to the queue.
        visited[startNode] = true;
        queue.offer(startNode);

        int count = 0;

        // Process nodes level by level until the queue becomes empty.
        while (!queue.isEmpty()) {
            int current = queue.poll();
            count++;

            // Explore every neighbor of the current node.
            for (int neighbor : adjacency.get(current)) {
                // If this neighbor has not been visited yet,
                // mark it visited immediately and add it to the queue.
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.offer(neighbor);
                }
            }
        }

        return count;
    }

    /**
     * Utility method to print a test case result in a beginner-friendly format.
     *
     * @param n the number of warehouses
     * @param roads the roads array
     * @param solver the Solution instance used to evaluate the test case
     * @return no return value
     * Time complexity: O(n + m), because it calls the main algorithm
     * Space complexity: O(n + m), because it depends on the main algorithm
     */
    public static void runTest(int n, int[][] roads, Solution solver) {
        System.out.println("n = " + n);
        System.out.println("roads = " + Arrays.deepToString(roads));
        System.out.println("Is single route? " + solver.isSingleRoute(n, roads));
        System.out.println();
    }

    /**
     * Demonstrates the solution using sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return no return value
     * Time complexity: O(total size of all demonstrated test cases)
     * Space complexity: O(max size of a demonstrated test case)
     */
    public static void main(String[] args) {
        Solution solver = new Solution();

        // Example 1 from the problem:
        // 0 - 1 - 2 - 3
        // Degrees are [1, 2, 2, 1], connected, so expected result is true.
        int[][] roads1 = {
            {0, 1},
            {1, 2},
            {2, 3}
        };
        runTest(4, roads1, solver);

        // Example 2 from the problem:
        //     2
        //     |
        // 0 - 1 - 3
        // Node 1 has degree 3, so this is a branch, not a simple path.
        // Expected result is false.
        int[][] roads2 = {
            {0, 1},
            {1, 2},
            {1, 3}
        };
        runTest(4, roads2, solver);

        // Additional test:
        // Single warehouse with no roads is valid.
        // Expected result is true.
        int[][] roads3 = {};
        runTest(1, roads3, solver);

        // Additional test:
        // Cycle: 0 - 1 - 2 - 3 - 0
        // Every node has degree 2, but there are no endpoints.
        // A cycle is not a simple path.
        // Expected result is false.
        int[][] roads4 = {
            {0, 1},
            {1, 2},
            {2, 3},
            {3, 0}
        };
        runTest(4, roads4, solver);

        // Additional test:
        // Disconnected graph:
        // 0 - 1   2 - 3
        // Degrees alone are not enough; connectivity fails.
        // Expected result is false.
        int[][] roads5 = {
            {0, 1},
            {2, 3}
        };
        runTest(4, roads5, solver);
    }
}