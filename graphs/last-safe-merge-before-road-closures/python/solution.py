"""
Title: Last Safe Merge Before Road Closures

Problem Description:
A delivery company operates in a city with one-way roads. There are n intersections
labeled from 0 to n - 1 and m directed roads. Every road is described by three integers
[u, v, closeTime], meaning a driver can travel from intersection u to intersection v
only if they start using that road at a time strictly less than closeTime. Traveling
along any road always takes exactly 1 minute.

Two drivers start at the same time 0 from different intersections s1 and s2. They want
to meet as early as possible at some intersection x such that both drivers can reach x
while respecting all road closing times. If multiple intersections can be used for the
earliest possible meeting time, return the smallest intersection index. If no meeting is
possible, return -1.

Important details:
- A driver may wait at any intersection for any amount of time.
- A road with closeTime = t can be entered only at times 0, 1, ..., t - 1.
- Since each road takes 1 minute, arriving later can make some future roads unusable.
- The meeting happens when both drivers have arrived at the same intersection; one driver
  may arrive earlier and wait.

Key observation:
Because waiting is allowed but never helps to catch a road with a strict deadline
(waiting only makes departure times later), the best strategy for reachability is always
to move as early as possible. Therefore, for each start node, we compute the earliest
arrival time to every node under the rule:
    from node u reached at time t, we may use edge (u -> v, closeTime = c)
    only if t < c, and then we reach v at time t + 1.

After computing earliest arrival arrays from s1 and s2, any node reachable from both is
a valid meeting point. If driver A arrives at time a and driver B arrives at time b,
they can meet there at time max(a, b) because the earlier driver can wait. So we choose
the node minimizing:
    max(dist1[x], dist2[x])
and break ties by smaller node index.
"""

from heapq import heappop, heappush
from typing import List, Tuple


class Solution:
    def _earliest_arrivals(
        self,
        n: int,
        graph: List[List[Tuple[int, int]]],
        start: int,
    ) -> List[int]:
        """
        Compute the earliest feasible arrival time from one start node to every node.

        We use Dijkstra's algorithm style processing with a min-heap because:
        - Every state is "being at node u at earliest known time t".
        - From that state, an outgoing edge (u -> v, closeTime) is usable only if t < closeTime.
        - If usable, it leads to time t + 1 at node v.
        - All travel times are non-negative (in fact always 1), so processing nodes in
          increasing time order is correct.

        Even though all edge travel times are 1, a plain BFS is not enough by itself unless
        we are careful, because edge usability depends on the current time. However, since
        the transition cost is still uniform and monotonic, Dijkstra with a heap is simple,
        safe, and efficient for the given constraints.

        Args:
            n: Number of intersections.
            graph: Adjacency list where graph[u] contains (v, close_time).
            start: Starting intersection.

        Returns:
            A list dist where dist[x] is the earliest time we can reach x from start,
            or a very large number if x is unreachable.

        Time complexity:
            O((n + m) log n)

        Space complexity:
            O(n + m)
        """
        inf: int = 10**30

        # dist[node] = earliest known time to arrive at this node.
        # Initialize everything as unreachable, except the start node at time 0.
        dist: List[int] = [inf] * n
        dist[start] = 0

        # Min-heap of (arrival_time, node).
        # We always expand the currently earliest reachable state first.
        heap: List[Tuple[int, int]] = [(0, start)]

        while heap:
            current_time, u = heappop(heap)

            # If this heap entry is stale, skip it.
            # This happens when we already found a better way to reach u.
            if current_time != dist[u]:
                continue

            # Explore all outgoing roads from u.
            for v, close_time in graph[u]:
                # Very important rule:
                # We may start using this road only if the departure time is strictly less
                # than close_time. Since we are at u at current_time, the road is usable
                # exactly when current_time < close_time.
                if current_time < close_time:
                    next_time: int = current_time + 1

                    # Standard relaxation step:
                    # If this route reaches v earlier than any previously known route,
                    # update and push into the heap.
                    if next_time < dist[v]:
                        dist[v] = next_time
                        heappush(heap, (next_time, v))

        return dist

    def lastSafeMergeBeforeRoadClosures(
        self,
        n: int,
        roads: List[List[int]],
        s1: int,
        s2: int,
    ) -> int:
        """
        Find the meeting intersection that allows the earliest possible meeting time.

        Strategy:
        1. Build the directed graph.
        2. Compute earliest arrival times from s1 to all nodes.
        3. Compute earliest arrival times from s2 to all nodes.
        4. For every node reachable from both starts:
           - They can meet there at time max(dist1[node], dist2[node]),
             because the earlier driver can wait.
        5. Choose the node with the smallest meeting time.
           If multiple nodes have the same earliest meeting time, choose the smallest index.
        6. If no node is reachable from both, return -1.

        Args:
            n: Number of intersections.
            roads: Directed roads [u, v, closeTime].
            s1: Start node of driver 1.
            s2: Start node of driver 2.

        Returns:
            The best meeting intersection index, or -1 if no meeting is possible.

        Time complexity:
            O((n + m) log n)

        Space complexity:
            O(n + m)
        """
        # Build adjacency list for the directed graph.
        # graph[u] will store all outgoing roads from u as (v, close_time).
        graph: List[List[Tuple[int, int]]] = [[] for _ in range(n)]
        for u, v, close_time in roads:
            graph[u].append((v, close_time))

        # Compute earliest feasible arrival times from both starting points.
        dist1: List[int] = self._earliest_arrivals(n, graph, s1)
        dist2: List[int] = self._earliest_arrivals(n, graph, s2)

        inf: int = 10**30

        # Track the best answer found so far.
        best_node: int = -1
        best_meeting_time: int = inf

        # Check every intersection as a possible meeting point.
        for node in range(n):
            # Both drivers must be able to reach this node.
            if dist1[node] == inf or dist2[node] == inf:
                continue

            # If one arrives earlier, they can wait.
            # Therefore the actual meeting time at this node is the later arrival time.
            meeting_time: int = max(dist1[node], dist2[node])

            # We want the earliest possible meeting time.
            # If tied, choose the smaller node index.
            if meeting_time < best_meeting_time:
                best_meeting_time = meeting_time
                best_node = node
            elif meeting_time == best_meeting_time and node < best_node:
                best_node = node

        return best_node


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 5
    roads1 = [
        [0, 2, 3],
        [1, 2, 2],
        [2, 3, 5],
        [1, 4, 10],
        [4, 3, 10],
    ]
    s1_1 = 0
    s2_1 = 1
    result1 = solution.lastSafeMergeBeforeRoadClosures(n1, roads1, s1_1, s2_1)
    print(result1)  # Expected: 2

    # Example 2
    # Careful reasoning:
    # Driver 1: 0 -> 2 at time 0, arrives 1; then 2 -> 3 at time 1, arrives 2.
    # Driver 2: 1 -> 3 at time 0, arrives 1.
    # Since waiting is allowed, they can meet at node 3 at time 2.
    # Therefore the correct answer under the stated rules is 3.
    n2 = 4
    roads2 = [
        [0, 2, 1],
        [2, 3, 2],
        [1, 3, 1],
    ]
    s1_2 = 0
    s2_2 = 1
    result2 = solution.lastSafeMergeBeforeRoadClosures(n2, roads2, s1_2, s2_2)
    print(result2)  # Correct under the problem statement: 3

    # Additional custom example where no meeting is possible.
    n3 = 4
    roads3 = [
        [0, 2, 1],
        [1, 3, 1],
    ]
    s1_3 = 0
    s2_3 = 1
    result3 = solution.lastSafeMergeBeforeRoadClosures(n3, roads3, s1_3, s2_3)
    print(result3)  # Expected: -1