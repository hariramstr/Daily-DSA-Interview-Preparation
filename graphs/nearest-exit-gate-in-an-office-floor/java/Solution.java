import java.util.*;

/*
 * Title: Nearest Exit Gate in an Office Floor
 * Difficulty: Easy
 * Topic: Graphs
 *
 * Problem Description:
 * You are given an office floor represented as an undirected graph. Each room is numbered
 * from 0 to n - 1, and hallways connect pairs of rooms. Some rooms contain emergency exit gates.
 * Starting from a given room, you must determine the minimum number of hallways an employee
 * needs to walk through to reach any exit gate.
 *
 * Return the smallest number of edges from the start room to any exit room. If the starting
 * room is itself an exit room, return 0. If no exit gate is reachable, return -1.
 *
 * The graph may be disconnected, and there may be multiple possible exit rooms. All hallways
 * can be used in both directions, and every hallway has the same cost of 1 step. This makes
 * the problem a shortest-path search in an unweighted graph.
 *
 * Your task is to write a function that takes the number of rooms n, a list of hallways edges
 * where each edge is [u, v], a list exits containing all rooms with exit gates, and an integer
 * start representing the employee's current room.
 *
 * Constraints:
 * - 1 <= n <= 10^5
 * - 0 <= edges.length <= 2 * 10^5
 * - edges[i].length == 2
 * - 0 <= u, v < n
 * - u != v
 * - 1 <= exits.length <= n
 * - 0 <= exits[i] < n
 * - 0 <= start < n
 * - There are no duplicate hallways.
 *
 * Example 1:
 * Input: n = 7, edges = [[0,1],[1,2],[2,3],[1,4],[4,5],[5,6]], exits = [3,6], start = 0
 * Output: 3
 * Explanation: The shortest path from room 0 to an exit is 0 -> 1 -> 2 -> 3, which uses
 * 3 hallways. The other exit at room 6 takes 4 hallways.
 *
 * Example 2:
 * Input: n = 5, edges = [[0,1],[1,2],[3,4]], exits = [4], start = 0
 * Output: -1
 * Explanation: Room 4 is in a different connected component, so no exit is reachable from room 0.
 */

public class Solution {

    /**
     * Finds the minimum number of hallways needed to reach any exit room from the start room.
     *
     * This method uses Breadth-First Search (BFS), which is the correct algorithm for finding
     * the shortest path in an unweighted graph. Since every hallway has equal cost (1 step),
     * BFS guarantees that the first time we reach an exit room, we have found the minimum number
     * of edges needed.
     *
     * @param n the total number of rooms, labeled from 0 to n - 1
     * @param edges the list of undirected hallways, where each element is [u, v]
     * @param exits the list of rooms that contain emergency exit gates
     * @param start the room where the employee starts
     * @return the minimum number of hallways from start to any exit room; returns 0 if start
     *         is already an exit room; returns -1 if no exit room is reachable
     *
     * Time complexity: O(n + m), where n is the number of rooms and m is the number of edges.
     * Space complexity: O(n + m), for the adjacency list, visited tracking, and BFS queue.
     */
    public int nearestExit(int n, int[][] edges, int[] exits, int start) {
        // Step 1:
        // Convert the exits array into a boolean lookup table.
        //
        // Why do this?
        // Because during BFS, we will repeatedly ask:
        // "Is this room an exit?"
        //
        // A boolean array lets us answer that in O(1) time.
        boolean[] isExit = new boolean[n];
        for (int exitRoom : exits) {
            isExit[exitRoom] = true;
        }

        // Step 2:
        // If the starting room is already an exit room, the answer is immediately 0.
        //
        // No movement is needed because the employee is already at an exit.
        if (isExit[start]) {
            return 0;
        }

        // Step 3:
        // Build the graph as an adjacency list.
        //
        // Since the graph is undirected:
        // if there is a hallway [u, v], then:
        // - u connects to v
        // - v connects to u
        //
        // We create a list for every room, then fill in neighbors.
        List<List<Integer>> graph = buildGraph(n, edges);

        // Step 4:
        // Prepare BFS data structures.
        //
        // visited[room] tells us whether we have already processed or enqueued that room.
        // This prevents infinite loops and repeated work in cyclic graphs.
        boolean[] visited = new boolean[n];

        // Queue for BFS.
        // BFS explores rooms level by level:
        // - first all rooms at distance 0
        // - then all rooms at distance 1
        // - then all rooms at distance 2
        // and so on.
        Queue<Integer> queue = new ArrayDeque<>();

        // Start BFS from the starting room.
        queue.offer(start);
        visited[start] = true;

        // Step 5:
        // Track the current distance from the start room.
        //
        // At the beginning, we are at distance 0 from the start.
        int distance = 0;

        // Step 6:
        // Standard BFS loop.
        //
        // Each iteration of the outer loop processes one "layer" of BFS.
        // All rooms currently in the queue belong to the same distance from the start.
        while (!queue.isEmpty()) {
            // Number of rooms in the current BFS layer.
            int levelSize = queue.size();

            // Process every room at the current distance.
            for (int i = 0; i < levelSize; i++) {
                int currentRoom = queue.poll();

                // Explore all neighboring rooms connected by one hallway.
                for (int neighbor : graph.get(currentRoom)) {
                    // If we have already visited this neighbor, skip it.
                    //
                    // This avoids:
                    // - revisiting the same room
                    // - processing longer paths to a room we already reached earlier
                    if (visited[neighbor]) {
                        continue;
                    }

                    // Mark the neighbor as visited as soon as we enqueue it.
                    //
                    // This is important:
                    // if we delay marking until dequeue time, the same room could be added
                    // to the queue multiple times by different parents.
                    visited[neighbor] = true;

                    // Since neighbor is one hallway away from currentRoom,
                    // and currentRoom is at "distance",
                    // neighbor is at "distance + 1".
                    //
                    // If this neighbor is an exit, then because BFS explores in increasing
                    // order of distance, this is guaranteed to be the nearest reachable exit.
                    if (isExit[neighbor]) {
                        return distance + 1;
                    }

                    // Otherwise, add the neighbor to the queue so it can be explored
                    // in the next BFS layer.
                    queue.offer(neighbor);
                }
            }

            // After processing the entire current layer, we move one step farther away
            // from the start room.
            distance++;
        }

        // Step 7:
        // If BFS finishes without finding any exit room, then no exit is reachable
        // from the starting room.
        return -1;
    }

    /**
     * Builds an adjacency list representation of the undirected graph.
     *
     * @param n the total number of rooms
     * @param edges the list of undirected hallways, where each hallway is [u, v]
     * @return an adjacency list where graph.get(room) contains all neighboring rooms
     *
     * Time complexity: O(n + m), where m is the number of edges.
     * Space complexity: O(n + m), for storing the adjacency list.
     */
    public List<List<Integer>> buildGraph(int n, int[][] edges) {
        // Create an empty neighbor list for every room.
        List<List<Integer>> graph = new ArrayList<>(n);
        for (int room = 0; room < n; room++) {
            graph.add(new ArrayList<>());
        }

        // Add each undirected edge in both directions.
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];

            graph.get(u).add(v);
            graph.get(v).add(u);
        }

        return graph;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration setup, excluding the algorithm calls.
     * Space complexity: O(1) auxiliary space for the demonstration setup, excluding input storage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 7;
        int[][] edges1 = {
            {0, 1},
            {1, 2},
            {2, 3},
            {1, 4},
            {4, 5},
            {5, 6}
        };
        int[] exits1 = {3, 6};
        int start1 = 0;

        int result1 = solution.nearestExit(n1, edges1, exits1, start1);
        System.out.println("Example 1 Output: " + result1);
        // Expected: 3

        // Example 2
        int n2 = 5;
        int[][] edges2 = {
            {0, 1},
            {1, 2},
            {3, 4}
        };
        int[] exits2 = {4};
        int start2 = 0;

        int result2 = solution.nearestExit(n2, edges2, exits2, start2);
        System.out.println("Example 2 Output: " + result2);
        // Expected: -1

        // Additional quick demonstration:
        // Start room is itself an exit.
        int n3 = 4;
        int[][] edges3 = {
            {0, 1},
            {1, 2},
            {2, 3}
        };
        int[] exits3 = {2};
        int start3 = 2;

        int result3 = solution.nearestExit(n3, edges3, exits3, start3);
        System.out.println("Example 3 Output: " + result3);
        // Expected: 0
    }
}