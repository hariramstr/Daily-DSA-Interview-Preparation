/*
Title: Fewest Route Transfers to Reach Destination Hub

Problem Description:
A city transit system publishes bus routes as lists of stop IDs. Each route is circular and can be boarded at any stop that belongs to that route. You are given an array routes where routes[i] contains all stop IDs served by the i-th bus route, along with two stop IDs: source and target.

Starting at source, you may board any route that includes source. If two different routes share at least one common stop, you may transfer between them at that stop. Your goal is to determine the minimum number of route boardings needed to travel from source to target. Boarding the first route counts as 1. If source == target, return 0. If it is impossible to reach target, return -1.

This problem should be solved by modeling the transit system as a graph. One natural approach is to treat each route as a node and connect two route-nodes if the corresponding routes share at least one stop. Then perform a breadth-first search from all routes containing source until reaching any route containing target. Efficient preprocessing is important because the total number of stops across all routes can be large.

Constraints:
- 1 <= routes.length <= 500
- 1 <= routes[i].length <= 10^4
- Sum of routes[i].length over all i does not exceed 10^5
- 0 <= routes[i][j] <= 10^6
- All stop IDs inside a single route are distinct
- 0 <= source, target <= 10^6
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    Let N be the number of routes, and let M be the total number of stop entries across all routes.
    - Building the stop -> routes map takes O(M)
    - The BFS processes each route at most once, and each stop list of a visited route is scanned once
    - Across the whole BFS, the total scanning work over route stop lists is O(M)
    Therefore, total time complexity is O(M)

    Space Complexity:
    - stop -> routes map stores every stop occurrence once: O(M)
    - visited routes, visited stops, and BFS queue together use O(M + N)
    Therefore, total space complexity is O(M)
    */
    public int NumBusesToDestination(int[][] routes, int source, int target)
    {
        // If the starting stop and destination stop are the same,
        // we do not need to board any route at all.
        // The problem explicitly says the answer should be 0 in this case.
        if (source == target)
        {
            return 0;
        }

        // This dictionary maps:
        //   stop ID -> list of route indices that include this stop
        //
        // Why do we build this?
        // Because when we are standing at a stop, we need to quickly know
        // which routes can be boarded from that stop.
        //
        // This is much more efficient than, for every stop, scanning all routes
        // to see which ones contain it.
        var stopToRoutes = new Dictionary<int, List<int>>();

        // Build the stop -> routes mapping.
        for (int routeIndex = 0; routeIndex < routes.Length; routeIndex++)
        {
            foreach (int stop in routes[routeIndex])
            {
                if (!stopToRoutes.ContainsKey(stop))
                {
                    stopToRoutes[stop] = new List<int>();
                }

                stopToRoutes[stop].Add(routeIndex);
            }
        }

        // If the source stop does not appear in any route,
        // then we cannot even begin the trip.
        if (!stopToRoutes.ContainsKey(source))
        {
            return -1;
        }

        // If the target stop does not appear in any route,
        // then no route can ever deliver us there.
        if (!stopToRoutes.ContainsKey(target))
        {
            return -1;
        }

        // We will perform BFS over ROUTES, not over stops.
        //
        // Each queue entry stores:
        //   (routeIndex, busesTakenSoFar)
        //
        // Why BFS?
        // Because every time we move from one route to another route,
        // that represents boarding one more bus.
        // BFS naturally finds the minimum number of such steps.
        var queue = new Queue<(int RouteIndex, int BusesTaken)>();

        // Tracks which routes have already been visited in BFS.
        //
        // Why necessary?
        // Without this, the same route could be added to the queue many times,
        // causing repeated work and possibly cycles.
        var visitedRoutes = new bool[routes.Length];

        // Tracks which stops we have already expanded.
        //
        // Why necessary?
        // Suppose many routes share the same stop. If we repeatedly process that stop,
        // we would repeatedly look up the same neighboring routes again and again.
        // Marking a stop as visited ensures each stop's route list is expanded only once.
        var visitedStops = new HashSet<int>();

        // All routes containing the source stop are valid starting points.
        // Boarding any one of them counts as taking 1 bus.
        foreach (int routeIndex in stopToRoutes[source])
        {
            queue.Enqueue((routeIndex, 1));
            visitedRoutes[routeIndex] = true;
        }

        // Standard BFS loop.
        while (queue.Count > 0)
        {
            var (currentRoute, busesTaken) = queue.Dequeue();

            // If the current route directly contains the target stop,
            // then we can reach the destination using the current number of boarded buses.
            //
            // We check this by scanning the stops of the current route.
            foreach (int stop in routes[currentRoute])
            {
                if (stop == target)
                {
                    return busesTaken;
                }
            }

            // Now we explore neighboring routes.
            //
            // Two routes are neighbors if they share at least one stop.
            // Instead of explicitly building a route-to-route graph ahead of time,
            // we discover neighbors on the fly:
            //   current route -> its stops -> all routes containing those stops
            //
            // This is memory-efficient and avoids unnecessary pairwise route comparisons.
            foreach (int stop in routes[currentRoute])
            {
                // If we have already expanded this stop before,
                // then we have already used it to discover all routes connected through it.
                // So we skip it to avoid duplicate work.
                if (visitedStops.Contains(stop))
                {
                    continue;
                }

                visitedStops.Add(stop);

                // Every route that contains this stop is reachable from the current route
                // by transferring at this stop.
                foreach (int nextRoute in stopToRoutes[stop])
                {
                    // If we have not visited that route yet,
                    // we can board it as one more bus ride.
                    if (!visitedRoutes[nextRoute])
                    {
                        visitedRoutes[nextRoute] = true;
                        queue.Enqueue((nextRoute, busesTaken + 1));
                    }
                }
            }
        }

        // If BFS finishes without finding any route that reaches target,
        // then the destination is impossible to reach.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// routes = [[1,5,7],[3,7,9,10],[10,11],[2,4,8]], source = 1, target = 11
// Expected output: 3
int[][] routes1 =
{
    new[] { 1, 5, 7 },
    new[] { 3, 7, 9, 10 },
    new[] { 10, 11 },
    new[] { 2, 4, 8 }
};
int source1 = 1;
int target1 = 11;
int result1 = solution.NumBusesToDestination(routes1, source1, target1);
Console.WriteLine(result1);

// Example 2:
// routes = [[2,6],[1,3,5],[7,8,9]], source = 2, target = 5
// Expected output: -1
int[][] routes2 =
{
    new[] { 2, 6 },
    new[] { 1, 3, 5 },
    new[] { 7, 8, 9 }
};
int source2 = 2;
int target2 = 5;
int result2 = solution.NumBusesToDestination(routes2, source2, target2);
Console.WriteLine(result2);

// Additional quick check:
// source == target, expected output: 0
int[][] routes3 =
{
    new[] { 4, 6, 8 },
    new[] { 1, 2, 3 }
};
int source3 = 6;
int target3 = 6;
int result3 = solution.NumBusesToDestination(routes3, source3, target3);
Console.WriteLine(result3);