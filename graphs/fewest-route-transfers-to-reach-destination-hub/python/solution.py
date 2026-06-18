"""
Title: Fewest Route Transfers to Reach Destination Hub

Problem Description:
A city transit system publishes bus routes as lists of stop IDs. Each route is circular
and can be boarded at any stop that belongs to that route. You are given an array
routes where routes[i] contains all stop IDs served by the i-th bus route, along with
two stop IDs: source and target.

Starting at source, you may board any route that includes source. If two different
routes share at least one common stop, you may transfer between them at that stop.
Your goal is to determine the minimum number of route boardings needed to travel from
source to target. Boarding the first route counts as 1. If source == target, return 0.
If it is impossible to reach target, return -1.

This problem can be modeled as a graph:
- Each bus route is a graph node.
- Two route-nodes are connected if the corresponding routes share at least one stop.
- We start BFS from every route containing the source stop.
- We want to reach any route containing the target stop.

Efficient preprocessing is important because the total number of stops across all routes
can be large.
"""

from collections import defaultdict, deque
from typing import Deque, DefaultDict, Dict, List, Set, Tuple


class Solution:
    def _build_stop_to_routes(self, routes: List[List[int]]) -> DefaultDict[int, List[int]]:
        """
        Build a mapping from each stop ID to the list of route indices that contain it.

        This preprocessing lets us quickly answer:
        "Given a stop, which routes can I board or transfer to here?"

        Args:
            routes: List of bus routes, where routes[i] is the list of stops on route i.

        Returns:
            A dictionary-like mapping:
            stop_id -> list of route indices containing that stop.

        Time complexity:
            O(S), where S is the total number of stops across all routes.

        Space complexity:
            O(S), for storing the reverse mapping from stops to routes.
        """
        stop_to_routes: DefaultDict[int, List[int]] = defaultdict(list)

        # We scan every route and every stop exactly once.
        # For each stop, we record which route index contains it.
        for route_index, route in enumerate(routes):
            for stop in route:
                stop_to_routes[stop].append(route_index)

        return stop_to_routes

    def numBusesToDestination(self, routes: List[List[int]], source: int, target: int) -> int:
        """
        Compute the minimum number of route boardings needed to travel from source to target.

        The algorithm performs a breadth-first search (BFS) over routes rather than stops.
        Each route is treated as a graph node. Two routes are considered connected if they
        share at least one stop. BFS guarantees that the first time we reach a route
        containing the target, we have used the fewest possible route boardings.

        Args:
            routes: List of bus routes, where routes[i] contains distinct stop IDs.
            source: Starting stop ID.
            target: Destination stop ID.

        Returns:
            The minimum number of boarded routes needed to reach target from source.
            Returns 0 if source == target.
            Returns -1 if target is unreachable.

        Time complexity:
            O(S), where S is the total number of stops across all routes.
            Explanation:
            - Building stop -> routes mapping takes O(S).
            - During BFS, each route is processed at most once.
            - Each stop on a visited route is examined once.
            - Each stop's route list is expanded once effectively due to route visitation.

        Space complexity:
            O(S + R), where:
            - S is the total number of stops across all routes,
            - R is the number of routes.
        """
        # Special case:
        # If source and target are the same stop, we are already at the destination.
        # No bus needs to be boarded, so the answer is 0.
        if source == target:
            return 0

        # Build a reverse lookup table:
        # stop_to_routes[stop] = all route indices that contain this stop.
        #
        # Why do we need this?
        # Because transfers happen at shared stops. If we are currently exploring one route
        # and we look at one of its stops, this mapping instantly tells us all other routes
        # reachable via transfer at that stop.
        stop_to_routes: DefaultDict[int, List[int]] = self._build_stop_to_routes(routes)

        # If source is not present in any route, we cannot even start.
        if source not in stop_to_routes:
            return -1

        # If target is not present in any route, there is no route that can ever deliver us there.
        if target not in stop_to_routes:
            return -1

        # Identify all routes that contain the target stop.
        # Reaching any one of these routes means success, because once we board such a route,
        # we can ride it to the target stop.
        target_routes: Set[int] = set(stop_to_routes[target])

        # BFS queue entries are:
        # (route_index, buses_taken_so_far)
        #
        # buses_taken_so_far means:
        # - If we start on a route containing source, that counts as 1 boarding.
        queue: Deque[Tuple[int, int]] = deque()

        # visited_routes prevents processing the same route multiple times.
        # This is essential for both correctness and efficiency:
        # - Correctness: avoids infinite loops in cyclic route connections.
        # - Efficiency: each route should be expanded only once.
        visited_routes: Set[int] = set()

        # Initialize BFS with every route that contains the source stop.
        # Why all of them?
        # Because at the starting stop, we may choose to board any available route.
        # Each such choice costs exactly 1 boarding.
        for route_index in stop_to_routes[source]:
            queue.append((route_index, 1))
            visited_routes.add(route_index)

        # Standard BFS:
        # We process routes in increasing order of number of boardings.
        # Therefore, the first time we encounter a target route, it is guaranteed to be optimal.
        while queue:
            current_route, buses_taken = queue.popleft()

            # If the current route contains the target stop, we are done.
            # Because BFS explores by minimum number of boardings, this is the best answer.
            if current_route in target_routes:
                return buses_taken

            # Explore all stops on the current route.
            # At each stop, we can transfer to any other route that also contains that stop.
            for stop in routes[current_route]:
                # Look up all routes sharing this stop.
                for next_route in stop_to_routes[stop]:
                    # If we have not visited this route before, it is a new state to explore.
                    if next_route not in visited_routes:
                        visited_routes.add(next_route)
                        # Boarding a new route via transfer increases the route count by 1.
                        queue.append((next_route, buses_taken + 1))

        # If BFS finishes without reaching any route containing target,
        # then the destination is unreachable.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # routes = [[1,5,7],[3,7,9,10],[10,11],[2,4,8]]
    # source = 1, target = 11
    #
    # Valid shortest path:
    # - Board route 0 at stop 1
    # - Transfer to route 1 at stop 7
    # - Transfer to route 2 at stop 10
    # Reach stop 11
    #
    # Total boarded routes = 3
    routes1: List[List[int]] = [[1, 5, 7], [3, 7, 9, 10], [10, 11], [2, 4, 8]]
    source1: int = 1
    target1: int = 11
    result1: int = solution.numBusesToDestination(routes1, source1, target1)
    print("Example 1 Output:", result1)  # Expected: 3

    # Example 2:
    # routes = [[2,6],[1,3,5],[7,8,9]]
    # source = 2, target = 5
    #
    # Route containing 2 is disconnected from route containing 5.
    # Therefore answer is -1.
    routes2: List[List[int]] = [[2, 6], [1, 3, 5], [7, 8, 9]]
    source2: int = 2
    target2: int = 5
    result2: int = solution.numBusesToDestination(routes2, source2, target2)
    print("Example 2 Output:", result2)  # Expected: -1

    # Additional quick sanity checks:
    routes3: List[List[int]] = [[1, 2, 3]]
    source3: int = 2
    target3: int = 2
    result3: int = solution.numBusesToDestination(routes3, source3, target3)
    print("Same source/target Output:", result3)  # Expected: 0

    routes4: List[List[int]] = [[1, 2, 7], [3, 6, 7]]
    source4: int = 1
    target4: int = 6
    result4: int = solution.numBusesToDestination(routes4, source4, target4)
    print("Transfer needed Output:", result4)  # Expected: 2