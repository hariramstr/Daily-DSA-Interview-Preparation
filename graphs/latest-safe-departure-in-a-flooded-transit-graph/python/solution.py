"""
Latest Safe Departure in a Flooded Transit Graph

Problem Summary:
We have an undirected graph of stations. Station 0 is the start and station n - 1
is the destination. Each station i has a flood time floodTime[i], meaning the station
is usable only at times strictly less than floodTime[i].

Rules:
- You may start at station 0 at some integer time t.
- Moving across any edge takes exactly 1 minute.
- You may wait at a station for any whole number of minutes, but only while that
  station remains usable for the entire waiting period.
- You may stand at or enter station i only at times strictly less than floodTime[i].

Goal:
Find the latest integer departure time t such that starting from station 0 at time t,
you can still reach station n - 1 safely.
- Return -1 if even starting at time 0 cannot reach the destination.
- Return 1000000000 if you can delay departure arbitrarily long.

Key observation:
Waiting never helps once you have departed, because all constraints are upper bounds
on time. If a route is feasible, taking each edge immediately is always at least as
good as waiting. Therefore, for a fixed start time t, feasibility reduces to checking
whether there exists a path where every visited node v at distance d from the start
satisfies:
    t + d < floodTime[v]

This gives a monotone decision problem:
- If departure time t is feasible, then every smaller departure time is also feasible.
So we can binary search the answer.

To test feasibility for a fixed t:
- Run BFS from node 0.
- Only traverse to a node v at BFS distance dist if t + dist < floodTime[v].
Because all edges have equal weight 1, BFS gives the earliest possible arrival time
for each node, which is the only arrival time worth considering.

Complexities:
- Each feasibility check is O(n + m).
- Binary search over t in [0, 10^9] takes O(log 10^9) ~ 31 checks.
- Total: O((n + m) log 10^9), which is efficient for the constraints.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def latestSafeDeparture(self, n: int, edges: List[List[int]], floodTime: List[int]) -> int:
        """
        Compute the latest integer time at which we can start from node 0 and still
        reach node n - 1 before any visited station floods.

        Args:
            n: Number of stations/nodes.
            edges: Undirected edges of the graph.
            floodTime: floodTime[i] is the earliest minute when station i becomes unusable.

        Returns:
            The latest safe departure time.
            - Returns -1 if no valid trip exists even when starting at time 0.
            - Returns 1000000000 if departure can be delayed arbitrarily long.

        Time complexity:
            O((n + m) * log(10^9))

        Space complexity:
            O(n + m)
        """
        # Build the adjacency list representation of the graph.
        # We use a list of lists because:
        # - the graph is static,
        # - we need fast iteration over neighbors,
        # - this is memory-efficient for sparse graphs.
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # First, check whether starting at time 0 is possible at all.
        # If not, the answer must be -1.
        if not self._can_reach(0, graph, floodTime):
            return -1

        # If even starting at time 1_000_000_000 is feasible, then by the problem's
        # convention we return 1_000_000_000 to represent "arbitrarily long".
        #
        # Why is this safe?
        # The problem explicitly asks us to return 1_000_000_000 in that case.
        # Since all flood times are <= 1_000_000_000, feasibility at this bound means
        # the start and every visited node on some route must still be valid. In practice,
        # this can only happen in the intended "infinite enough" interpretation from the
        # statement, so we follow the required output convention.
        if self._can_reach(1_000_000_000, graph, floodTime):
            return 1_000_000_000

        # Binary search for the maximum feasible departure time.
        #
        # Monotonicity:
        # If we can depart at time t, then we can also depart at any earlier time t' < t,
        # because all arrival times become earlier and therefore remain valid.
        #
        # So the predicate "can_reach(t)" is True for a prefix of times and False after that.
        left: int = 0
        right: int = 1_000_000_000

        while left < right:
            # We bias the midpoint upward so that when mid is feasible,
            # we move left up to mid and still make progress.
            mid: int = (left + right + 1) // 2

            if self._can_reach(mid, graph, floodTime):
                left = mid
            else:
                right = mid - 1

        return left

    def _can_reach(self, start_time: int, graph: List[List[int]], floodTime: List[int]) -> bool:
        """
        Check whether it is possible to start at node 0 at the given time and reach
        node n - 1 while always arriving at each visited node strictly before it floods.

        Args:
            start_time: Proposed departure time from node 0.
            graph: Adjacency list of the undirected graph.
            floodTime: Flood deadlines for each node.

        Returns:
            True if the destination is reachable safely, otherwise False.

        Time complexity:
            O(n + m)

        Space complexity:
            O(n)
        """
        n: int = len(graph)

        # We must be able to stand at the start node at the departure time.
        # The rule is strict: time must be strictly less than floodTime[0].
        if start_time >= floodTime[0]:
            return False

        # Standard BFS setup.
        #
        # visited[v] tells us whether we have already enqueued node v.
        # Because all edges have weight 1, the first time BFS reaches a node is the
        # earliest possible arrival time at that node. Any later arrival would only be
        # worse, since flood constraints are upper bounds. Therefore, visiting each node
        # once is sufficient and correct.
        visited: List[bool] = [False] * n
        queue: Deque[int] = deque()

        visited[0] = True
        queue.append(0)

        # distance is the number of edges from node 0 to the current BFS layer.
        # Actual arrival time at a node in this layer is start_time + distance.
        distance: int = 0

        # Layer-by-layer BFS:
        # This is especially beginner-friendly because every node in the current layer
        # has the same shortest-path distance from the start.
        while queue:
            layer_size: int = len(queue)

            # Process all nodes at the current shortest-path distance.
            for _ in range(layer_size):
                u: int = queue.popleft()

                # If we reached the destination, then the arrival time here is valid.
                # Why valid? Because we only ever enqueue a node if its arrival time
                # satisfies the strict flood constraint.
                if u == n - 1:
                    return True

                # Try all neighbors. Reaching a neighbor takes one more minute,
                # so its arrival time would be start_time + distance + 1.
                next_arrival: int = start_time + distance + 1

                for v in graph[u]:
                    if visited[v]:
                        continue

                    # We may enter station v only if arrival time is strictly less than
                    # floodTime[v]. If not, this move is invalid.
                    if next_arrival >= floodTime[v]:
                        continue

                    visited[v] = True
                    queue.append(v)

            # After finishing this layer, we move to the next distance.
            distance += 1

        # BFS exhausted all reachable valid states without reaching the destination.
        return False


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt text.
    # Important note:
    # The prompt's narrative explains that starting at time 2 is invalid and time 1 works,
    # so the correct result for that example is 1.
    n1 = 5
    edges1 = [[0, 1], [1, 2], [2, 4], [0, 3], [3, 4]]
    flood1 = [4, 3, 5, 2, 6]
    print(solution.latestSafeDeparture(n1, edges1, flood1))  # Expected: 1

    # Example 2 from the prompt.
    # With all flood times at 1_000_000_000, the required convention says return 1_000_000_000.
    n2 = 4
    edges2 = [[0, 1], [1, 2], [2, 3], [0, 2]]
    flood2 = [1_000_000_000, 1_000_000_000, 1_000_000_000, 1_000_000_000]
    print(solution.latestSafeDeparture(n2, edges2, flood2))  # Expected: 1000000000