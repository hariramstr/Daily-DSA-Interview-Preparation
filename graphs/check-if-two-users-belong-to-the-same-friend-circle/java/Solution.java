import java.util.*;

/*
Problem Title: Check if Two Users Belong to the Same Friend Circle

Problem Description:
You are given an undirected social graph with n users labeled from 0 to n - 1.
Each friendship is represented by a pair [u, v], meaning user u and user v know
each other directly. Friendships are mutual, so the graph is undirected.

A friend circle is a connected group of users where every user can reach every
other user through one or more friendship links.

Your task is to determine, for a single query, whether two given users source and
target belong to the same friend circle. Return true if there is a path between
them, otherwise return false.

This problem is intended to test basic graph traversal using either Breadth-First
Search (BFS) or Depth-First Search (DFS). You may build an adjacency list from
the friendship pairs and explore from source until you either reach target or
finish visiting the connected component.

Constraints:
- 1 <= n <= 10^4
- 0 <= friendships.length <= 2 * 10^4
- friendships[i].length == 2
- 0 <= u, v < n
- u != v
- There are no duplicate friendship pairs.
- 0 <= source, target < n

Example 1:
Input: n = 5, friendships = [[0,1],[1,2],[3,4]], source = 0, target = 2
Output: true
Explanation: User 0 can reach user 2 through 1, so they are in the same friend circle.

Example 2:
Input: n = 6, friendships = [[0,1],[2,3],[3,4]], source = 1, target = 5
Output: false
Explanation: User 5 has no connection to user 1, so they are not in the same connected component.
*/

public class Solution {

    /**
     * Determines whether two users belong to the same friend circle by performing
     * a Breadth-First Search (BFS) from the source user.
     *
     * The idea is:
     * 1. Build an adjacency list for the undirected graph.
     * 2. Start BFS from source.
     * 3. Visit all reachable users.
     * 4. If target is reached at any point, return true.
     * 5. If BFS finishes without reaching target, return false.
     *
     * @param n the total number of users labeled from 0 to n - 1
     * @param friendships the list of undirected friendship pairs where each pair is [u, v]
     * @param source the starting user for the connectivity check
     * @param target the destination user to test reachability
     * @return true if source and target are in the same connected component; false otherwise
     *
     * Time complexity: O(n + m), where n is the number of users and m is the number of friendships
     * Space complexity: O(n + m) for the adjacency list, visited array, and BFS queue
     */
    public boolean areInSameFriendCircle(int n, int[][] friendships, int source, int target) {
        // If both user IDs are the same, then they are trivially in the same friend circle.
        // A user is always reachable from themselves using a path of length 0.
        if (source == target) {
            return true;
        }

        // Build the graph as an adjacency list.
        // Each index represents a user.
        // The list at that index contains all directly connected friends.
        List<List<Integer>> graph = buildGraph(n, friendships);

        // visited[i] tells us whether user i has already been explored.
        // This prevents:
        // 1. Visiting the same node multiple times
        // 2. Infinite loops in cyclic graphs
        boolean[] visited = new boolean[n];

        // Standard BFS queue.
        // We begin by placing the source user into the queue.
        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(source);
        visited[source] = true;

        // Continue BFS while there are still users to process.
        while (!queue.isEmpty()) {
            // Remove the next user from the front of the queue.
            int currentUser = queue.poll();

            // Explore all direct friends of the current user.
            for (int neighbor : graph.get(currentUser)) {
                // If this neighbor is exactly the target user, then we found a path.
                // That means source and target are in the same connected component.
                if (neighbor == target) {
                    return true;
                }

                // If this neighbor has not been visited yet, we should explore it later.
                if (!visited[neighbor]) {
                    // Mark first, then enqueue.
                    // Marking before enqueueing avoids adding the same node many times.
                    visited[neighbor] = true;
                    queue.offer(neighbor);
                }
            }
        }

        // If BFS completes and target was never found, then no path exists.
        return false;
    }

    /**
     * Builds an adjacency list representation of the undirected friendship graph.
     *
     * Because friendships are mutual:
     * - if [u, v] exists, then u is connected to v
     * - and v is also connected to u
     *
     * @param n the total number of users
     * @param friendships the list of friendship pairs
     * @return an adjacency list where graph.get(i) contains all neighbors of user i
     *
     * Time complexity: O(n + m), where m is the number of friendships
     * Space complexity: O(n + m)
     */
    public List<List<Integer>> buildGraph(int n, int[][] friendships) {
        // Create an empty adjacency list with one list for each user.
        List<List<Integer>> graph = new ArrayList<>(n);
        for (int user = 0; user < n; user++) {
            graph.add(new ArrayList<>());
        }

        // Add each undirected edge in both directions.
        for (int[] friendship : friendships) {
            int u = friendship[0];
            int v = friendship[1];

            graph.get(u).add(v);
            graph.get(v).add(u);
        }

        return graph;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments; not used in this program
     *
     * @return nothing
     *
     * Time complexity: O(n + m) per demonstration call to the algorithm
     * Space complexity: O(n + m) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // n = 5
        // friendships = [[0,1],[1,2],[3,4]]
        // source = 0
        // target = 2
        //
        // Graph structure:
        // 0 -- 1 -- 2
        // 3 -- 4
        //
        // There is a path from 0 to 2: 0 -> 1 -> 2
        // Expected output: true
        int n1 = 5;
        int[][] friendships1 = {
            {0, 1},
            {1, 2},
            {3, 4}
        };
        int source1 = 0;
        int target1 = 2;
        boolean result1 = solution.areInSameFriendCircle(n1, friendships1, source1, target1);
        System.out.println("Example 1 Output: " + result1);

        // Example 2:
        // n = 6
        // friendships = [[0,1],[2,3],[3,4]]
        // source = 1
        // target = 5
        //
        // Graph structure:
        // 0 -- 1
        // 2 -- 3 -- 4
        // 5 is isolated
        //
        // There is no path from 1 to 5
        // Expected output: false
        int n2 = 6;
        int[][] friendships2 = {
            {0, 1},
            {2, 3},
            {3, 4}
        };
        int source2 = 1;
        int target2 = 5;
        boolean result2 = solution.areInSameFriendCircle(n2, friendships2, source2, target2);
        System.out.println("Example 2 Output: " + result2);

        // Additional quick check:
        // Same source and target should always be true.
        int n3 = 3;
        int[][] friendships3 = {
            {0, 1}
        };
        int source3 = 2;
        int target3 = 2;
        boolean result3 = solution.areInSameFriendCircle(n3, friendships3, source3, target3);
        System.out.println("Additional Example Output: " + result3);
    }
}