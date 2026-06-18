import java.util.*;

/*
 * Title: Fewest Route Transfers to Reach Destination Hub
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * A city transit system publishes bus routes as lists of stop IDs. Each route is circular
 * and can be boarded at any stop that belongs to that route. You are given an array routes
 * where routes[i] contains all stop IDs served by the i-th bus route, along with two stop IDs:
 * source and target.
 *
 * Starting at source, you may board any route that includes source. If two different routes
 * share at least one common stop, you may transfer between them at that stop. Your goal is to
 * determine the minimum number of route boardings needed to travel from source to target.
 * Boarding the first route counts as 1. If source == target, return 0. If it is impossible
 * to reach target, return -1.
 *
 * This problem should be solved by modeling the transit system as a graph. One natural approach
 * is to treat each route as a node and connect two route-nodes if the corresponding routes share
 * at least one stop. Then perform a breadth-first search from all routes containing source until
 * reaching any route containing target. Efficient preprocessing is important because the total
 * number of stops across all routes can be large.
 *
 * Constraints:
 * - 1 <= routes.length <= 500
 * - 1 <= routes[i].length <= 10^4
 * - Sum of routes[i].length over all i does not exceed 10^5
 * - 0 <= routes[i][j] <= 10^6
 * - All stop IDs inside a single route are distinct
 * - 0 <= source, target <= 10^6
 *
 * Example 1:
 * Input: routes = [[1,5,7],[3,7,9,10],[10,11],[2,4,8]], source = 1, target = 11
 * Output: 3
 * Explanation: Board route 0 at stop 1, transfer to route 1 at stop 7, then transfer to route 2
 * at stop 10. The minimum number of boarded routes is 3.
 *
 * Example 2:
 * Input: routes = [[2,6],[1,3,5],[7,8,9]], source = 2, target = 5
 * Output: -1
 * Explanation: The route containing stop 2 shares no stop with the route containing stop 5,
 * and there is no chain of transfers connecting them, so the destination cannot be reached.
 */

public class Solution {

    /**
     * Computes the minimum number of route boardings needed to travel from source stop to target stop.
     *
     * The core idea is:
     * 1. Treat each bus route as a graph node.
     * 2. Two route-nodes are connected if the corresponding routes share at least one stop.
     * 3. Start BFS from every route that contains the source stop.
     * 4. The first time BFS reaches any route that contains the target stop, the BFS level
     *    equals the minimum number of boarded routes.
     *
     * Important implementation detail:
     * Instead of comparing every pair of routes directly (which can be expensive),
     * we first build a map from stop -> list of routes containing that stop.
     * This lets us efficiently discover neighboring routes through shared stops.
     *
     * @param routes the transit routes, where routes[i] is the list of stops served by route i
     * @param source the starting stop ID
     * @param target the destination stop ID
     * @return the minimum number of route boardings required, or -1 if the destination is unreachable
     *
     * Time complexity:
     * O(S + E) in practice using stop-to-routes mapping and BFS, where S is the total number of stops
     * across all routes and E is the total number of route-to-route discoveries through shared stops.
     * Because each route is visited at most once and each stop list is processed carefully, this is
     * efficient for the given constraints.
     *
     * Space complexity:
     * O(S + R), where S is the total number of stops across all routes and R is the number of routes,
     * due to the stop-to-routes map, visited arrays/sets, and BFS queue.
     */
    public int numBusesToDestination(int[][] routes, int source, int target) {
        // If source and target are the same stop, we do not need to board any route.
        if (source == target) {
            return 0;
        }

        // Step 1:
        // Build a mapping from each stop ID to all route indices that contain that stop.
        //
        // Example:
        // routes = [[1,5,7], [3,7,9,10], [10,11]]
        //
        // stopToRoutes might become:
        // 1  -> [0]
        // 5  -> [0]
        // 7  -> [0, 1]
        // 3  -> [1]
        // 9  -> [1]
        // 10 -> [1, 2]
        // 11 -> [2]
        //
        // This map is the key to efficiently finding transfers.
        Map<Integer, List<Integer>> stopToRoutes = buildStopToRoutesMap(routes);

        // If the source stop does not appear in any route, we cannot even start.
        if (!stopToRoutes.containsKey(source)) {
            return -1;
        }

        // If the target stop does not appear in any route, it is impossible to reach.
        if (!stopToRoutes.containsKey(target)) {
            return -1;
        }

        // Step 2:
        // Identify all routes that contain the target stop.
        // Reaching any one of these routes means we can get off at target.
        Set<Integer> targetRoutes = new HashSet<>(stopToRoutes.get(target));

        // Step 3:
        // Prepare BFS.
        //
        // Each queue entry is a route index.
        // The BFS "distance" in terms of levels corresponds to the number of boarded routes.
        Queue<Integer> queue = new ArrayDeque<>();

        // visitedRoutes[i] = true means route i has already been added to BFS and processed or scheduled.
        boolean[] visitedRoutes = new boolean[routes.length];

        // visitedStops keeps track of stops whose route lists have already been expanded.
        //
        // Why do we need this?
        // Suppose many routes share the same stop. Without this set, every time we visit another route
        // containing that stop, we would repeatedly scan the same stop's route list again and again.
        // Marking a stop as processed ensures each stop's adjacency expansion happens only once.
        Set<Integer> visitedStops = new HashSet<>();

        // Step 4:
        // Initialize BFS with every route that contains the source stop.
        //
        // Boarding any of these routes counts as 1 bus.
        for (int routeIndex : stopToRoutes.get(source)) {
            queue.offer(routeIndex);
            visitedRoutes[routeIndex] = true;
        }

        int busesTaken = 1;

        // Step 5:
        // Standard BFS over route-nodes.
        //
        // At each BFS level:
        // - all routes currently in the queue are reachable using exactly busesTaken boardings
        // - if any of them is a target route, we return busesTaken immediately
        while (!queue.isEmpty()) {
            int levelSize = queue.size();

            // Process one full BFS layer.
            for (int i = 0; i < levelSize; i++) {
                int currentRoute = queue.poll();

                // If this route contains the target stop, we are done.
                if (targetRoutes.contains(currentRoute)) {
                    return busesTaken;
                }

                // Explore all stops on the current route.
                //
                // For each stop:
                // - find all other routes that also contain this stop
                // - those routes are directly reachable by one transfer
                for (int stop : routes[currentRoute]) {
                    // If we have already expanded this stop before, skip it.
                    // This avoids redundant work.
                    if (!visitedStops.add(stop)) {
                        continue;
                    }

                    // All routes sharing this stop are neighbors in the route graph.
                    List<Integer> nextRoutes = stopToRoutes.get(stop);
                    if (nextRoutes == null) {
                        continue;
                    }

                    for (int nextRoute : nextRoutes) {
                        if (!visitedRoutes[nextRoute]) {
                            visitedRoutes[nextRoute] = true;
                            queue.offer(nextRoute);
                        }
                    }
                }
            }

            // After finishing one BFS layer, moving to the next layer means
            // we would board one more route.
            busesTaken++;
        }

        // If BFS finishes without reaching any target route, target is unreachable.
        return -1;
    }

    /**
     * Builds a mapping from stop ID to the list of route indices that contain that stop.
     *
     * This preprocessing step is essential because it allows us to quickly discover
     * which routes are connected through a shared stop.
     *
     * @param routes the transit routes
     * @return a map where each key is a stop ID and the value is the list of route indices serving that stop
     *
     * Time complexity:
     * O(S), where S is the total number of stops across all routes.
     *
     * Space complexity:
     * O(S), for storing the stop-to-routes relationships.
     */
    public Map<Integer, List<Integer>> buildStopToRoutesMap(int[][] routes) {
        Map<Integer, List<Integer>> stopToRoutes = new HashMap<>();

        // Go through every route and every stop in that route.
        for (int routeIndex = 0; routeIndex < routes.length; routeIndex++) {
            for (int stop : routes[routeIndex]) {
                // If this stop has not been seen before, create a new list for it.
                stopToRoutes.computeIfAbsent(stop, key -> new ArrayList<>()).add(routeIndex);
            }
        }

        return stopToRoutes;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity:
     * O(1) for the demonstration setup itself, excluding the called algorithm.
     *
     * Space complexity:
     * O(1) for the demonstration setup itself, excluding the called algorithm.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] routes1 = {
            {1, 5, 7},
            {3, 7, 9, 10},
            {10, 11},
            {2, 4, 8}
        };
        int source1 = 1;
        int target1 = 11;
        int result1 = solution.numBusesToDestination(routes1, source1, target1);
        System.out.println("Example 1 Output: " + result1);

        int[][] routes2 = {
            {2, 6},
            {1, 3, 5},
            {7, 8, 9}
        };
        int source2 = 2;
        int target2 = 5;
        int result2 = solution.numBusesToDestination(routes2, source2, target2);
        System.out.println("Example 2 Output: " + result2);

        // Additional quick sanity checks for beginner-friendly verification.
        int[][] routes3 = {
            {1, 2, 3}
        };
        int result3 = solution.numBusesToDestination(routes3, 2, 2);
        System.out.println("Same source and target Output: " + result3);

        int[][] routes4 = {
            {1, 2, 7},
            {3, 6, 7}
        };
        int result4 = solution.numBusesToDestination(routes4, 1, 6);
        System.out.println("Transfer once Output: " + result4);
    }
}