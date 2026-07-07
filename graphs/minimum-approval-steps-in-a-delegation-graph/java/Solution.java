import java.util.*;

/*
 * Title: Minimum Approval Steps in a Delegation Graph
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * In a company workflow system, each employee may delegate approval authority to several other employees.
 * You are given a directed graph with n employees labeled from 0 to n - 1, where each directed edge [u, v]
 * means employee u can forward an approval request directly to employee v in one step.
 *
 * You are also given a list of employees who are initially authorized to start the request, and a list of
 * target employees whose signatures are considered acceptable final approvals.
 *
 * Your task is to compute the minimum number of delegation steps needed for any starting employee to reach
 * any target employee. If no target can be reached from any starting employee, return -1.
 *
 * This is not the same as finding a path from one fixed source to one fixed destination. Multiple starting
 * points and multiple acceptable ending points are allowed, and the optimal answer may begin from any start
 * node and end at any target node.
 *
 * Return the fewest number of edges in such a path.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 0 <= edges.length <= 3 * 10^5
 * - edges[i] = [u, v]
 * - 0 <= u, v < n
 * - 1 <= starts.length <= n
 * - 1 <= targets.length <= n
 * - Values in starts are distinct
 * - Values in targets are distinct
 *
 * Example 1:
 * Input: n = 7, edges = [[0,1],[1,3],[2,3],[3,4],[4,6],[2,5]], starts = [0,2], targets = [6,5]
 * Output: 1
 * Explanation: Start employee 2 can delegate directly to employee 5, which is already an acceptable
 * final approver. So the minimum number of steps is 1.
 *
 * Example 2:
 * Input: n = 6, edges = [[0,1],[1,2],[2,3],[4,5]], starts = [0,4], targets = [3]
 * Output: 3
 * Explanation: The shortest valid chain is 0 -> 1 -> 2 -> 3, which takes 3 delegation steps.
 * The other start employee 4 cannot reach the target.
 */

public class Solution {

    /**
     * Computes the minimum number of delegation steps needed to reach any target employee
     * from any starting employee in a directed graph.
     *
     * The key idea is to run a multi-source BFS:
     * - Put all start nodes into the queue initially with distance 0.
     * - Expand level by level.
     * - The first time we reach any target node, that distance is guaranteed to be the minimum.
     *
     * Why this works:
     * - Every edge has equal cost: 1 step.
     * - BFS always explores nodes in increasing order of distance.
     * - Starting from all sources at once means we effectively search from the best possible start
     *   automatically, without needing to run BFS separately for each start node.
     *
     * @param n the number of employees/nodes in the graph
     * @param edges directed edges where each edge [u, v] means u can delegate directly to v
     * @param starts the list of valid starting employees
     * @param targets the list of acceptable final approval employees
     * @return the minimum number of edges from any start to any target, or -1 if unreachable
     * Time complexity: O(n + m), where m = edges.length
     * Space complexity: O(n + m)
     */
    public int minimumApprovalSteps(int n, int[][] edges, int[] starts, int[] targets) {
        // Build an adjacency list for the directed graph.
        // graph[u] will contain all nodes v such that there is an edge u -> v.
        List<List<Integer>> graph = buildGraph(n, edges);

        // Convert targets into a boolean lookup array for O(1) membership checking.
        // targetSet[x] == true means node x is an acceptable final node.
        boolean[] targetSet = new boolean[n];
        for (int target : targets) {
            targetSet[target] = true;
        }

        // Important edge case:
        // If any start node is already a target node, then the minimum number of steps is 0,
        // because we do not need to traverse any edge.
        for (int start : starts) {
            if (targetSet[start]) {
                return 0;
            }
        }

        // Distance array:
        // dist[node] = shortest number of edges from any start node to this node.
        // Initialize all distances to -1 to mean "unvisited".
        int[] dist = new int[n];
        Arrays.fill(dist, -1);

        // Standard BFS queue.
        Queue<Integer> queue = new ArrayDeque<>();

        // Multi-source BFS initialization:
        // Put every start node into the queue with distance 0.
        // This simulates running BFS from all starts simultaneously.
        for (int start : starts) {
            dist[start] = 0;
            queue.offer(start);
        }

        // Perform BFS.
        while (!queue.isEmpty()) {
            // Remove the next node to process.
            int current = queue.poll();

            // Explore all outgoing edges current -> neighbor.
            for (int neighbor : graph.get(current)) {
                // If neighbor has already been visited, we skip it.
                // The first time BFS reaches a node is always via the shortest path.
                if (dist[neighbor] != -1) {
                    continue;
                }

                // Since neighbor is reached from current by one more edge,
                // its distance is dist[current] + 1.
                dist[neighbor] = dist[current] + 1;

                // If this neighbor is a target, we can return immediately.
                // Because BFS explores in increasing distance order, this is the
                // minimum possible number of steps to any target.
                if (targetSet[neighbor]) {
                    return dist[neighbor];
                }

                // Otherwise, continue BFS from this newly discovered node.
                queue.offer(neighbor);
            }
        }

        // If BFS finishes without reaching any target, then no valid path exists.
        return -1;
    }

    /**
     * Builds the adjacency list representation of the directed graph.
     *
     * @param n the number of nodes
     * @param edges the directed edges
     * @return adjacency list where result.get(u) contains all v such that u -> v exists
     * Time complexity: O(n + m), where m = edges.length
     * Space complexity: O(n + m)
     */
    public List<List<Integer>> buildGraph(int n, int[][] edges) {
        // Create an empty adjacency list for each node.
        List<List<Integer>> graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        // Add each directed edge u -> v to the adjacency list.
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            graph.get(u).add(v);
        }

        return graph;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration inputs, excluding the called algorithm
     * Space complexity: O(1) for the fixed demonstration inputs, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 7;
        int[][] edges1 = {
            {0, 1},
            {1, 3},
            {2, 3},
            {3, 4},
            {4, 6},
            {2, 5}
        };
        int[] starts1 = {0, 2};
        int[] targets1 = {6, 5};

        int result1 = solution.minimumApprovalSteps(n1, edges1, starts1, targets1);
        System.out.println("Example 1 Output: " + result1);
        // Expected: 1
        // Reason:
        // Start 2 can go directly to 5 in one step, and 5 is a target.

        // Example 2
        int n2 = 6;
        int[][] edges2 = {
            {0, 1},
            {1, 2},
            {2, 3},
            {4, 5}
        };
        int[] starts2 = {0, 4};
        int[] targets2 = {3};

        int result2 = solution.minimumApprovalSteps(n2, edges2, starts2, targets2);
        System.out.println("Example 2 Output: " + result2);
        // Expected: 3
        // Reason:
        // 0 -> 1 -> 2 -> 3 takes 3 steps.
        // Start 4 cannot reach 3.

        // Additional quick check: start already equals target
        int n3 = 3;
        int[][] edges3 = {
            {0, 1},
            {1, 2}
        };
        int[] starts3 = {2};
        int[] targets3 = {2};

        int result3 = solution.minimumApprovalSteps(n3, edges3, starts3, targets3);
        System.out.println("Additional Example Output: " + result3);
        // Expected: 0
    }
}