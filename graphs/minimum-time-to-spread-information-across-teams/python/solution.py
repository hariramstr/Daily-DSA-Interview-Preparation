```python
"""
Title: Minimum Time to Spread Information Across Teams
Difficulty: Medium
Topic: Graphs

Problem Description:
A company has `n` employees numbered from `0` to `n-1`, organized into a directed
communication network. Each employee can pass information to a specific set of direct
reports. When an employee receives a piece of information, they immediately begin
sharing it with all their direct reports simultaneously. It takes exactly `time[i]`
minutes for employee `i` to pass information to each of their direct reports.

Given the number of employees `n`, an array `time` of length `n` where `time[i]` is
the number of minutes it takes employee `i` to inform their direct reports, and a list
of directed edges `relations` where `relations[j] = [a, b]` means employee `a` can
directly inform employee `b`, determine the minimum number of minutes it takes for a
piece of information starting at employee `0` to reach ALL employees in the network.
If it is impossible to inform all employees, return `-1`.

Constraints:
- 1 <= n <= 10^4
- 0 <= time[i] <= 1000
- 0 <= relations.length <= 5 * 10^4
- relations[j].length == 2
- 0 <= relations[j][0], relations[j][1] < n
- No self-loops in relations
"""

import heapq
from typing import List, Dict
from collections import defaultdict


class Solution:
    def min_time_to_inform(
        self, n: int, time: List[int], relations: List[List[int]]
    ) -> int:
        """
        Find the minimum time for information to spread from employee 0 to all employees.

        This problem is essentially finding the longest path (in terms of cumulative time)
        from node 0 to any node in a Directed Acyclic Graph (DAG). We use Dijkstra's
        algorithm adapted for "maximum arrival time" (or equivalently, we negate costs
        and use a min-heap to simulate a max-heap).

        Actually, since we want the MINIMUM time for ALL employees to be informed,
        we need the MAXIMUM of the earliest arrival times at each node. This is because
        all paths run in parallel, but we must wait for the slowest path to complete.

        We use a modified Dijkstra's approach:
        - dist[i] = earliest time employee i receives the information
        - We want to maximize the minimum arrival time across all nodes (i.e., find
          the maximum dist[i] over all reachable nodes).

        Args:
            n: Number of employees (nodes 0 to n-1)
            time: time[i] = minutes employee i takes to inform their direct reports
            relations: List of [a, b] meaning employee a can directly inform employee b

        Returns:
            Minimum number of minutes for all employees to be informed, or -1 if impossible.

        Time Complexity: O((V + E) * log V) where V = n (employees), E = len(relations)
                         due to Dijkstra's algorithm with a priority queue.
        Space Complexity: O(V + E) for the adjacency list and distance array.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build the adjacency list (directed graph)
        # -----------------------------------------------------------------------
        # We represent the graph as a dictionary mapping each node to its neighbors.
        # graph[a] = list of b's where employee a can directly inform employee b.
        # Using defaultdict(list) so we don't need to initialize each key manually.
        graph: Dict[int, List[int]] = defaultdict(list)

        for a, b in relations:
            # Employee 'a' can inform employee 'b'
            graph[a].append(b)

        # -----------------------------------------------------------------------
        # STEP 2: Initialize the distance (earliest arrival time) array
        # -----------------------------------------------------------------------
        # dist[i] = the earliest time (in minutes) that employee i receives the info.
        # Initially, all employees have not been reached (infinity).
        # Employee 0 starts with the information at time 0.
        INF = float('inf')
        dist = [INF] * n
        dist[0] = 0  # Employee 0 already has the information at time 0

        # -----------------------------------------------------------------------
        # STEP 3: Dijkstra's Algorithm (standard shortest path)
        # -----------------------------------------------------------------------
        # We use a min-heap (priority queue) to always process the employee who
        # receives information the earliest first.
        #
        # Each entry in the heap is (current_time, employee_id).
        # current_time = the time at which this employee received the information.
        #
        # Why Dijkstra? Because all edge weights (time[i]) are non-negative,
        # Dijkstra's algorithm correctly finds the shortest (earliest) path from
        # source to each node.
        #
        # Note: "shortest path" here means "earliest time to receive information",
        # which is what we want for each individual node. The final answer is the
        # MAXIMUM of all these earliest times (since all must be informed).

        # Initialize the heap with the starting employee (employee 0 at time 0)
        min_heap: List[tuple] = [(0, 0)]  # (arrival_time, employee_id)

        while min_heap:
            # Pop the employee with the smallest current arrival time
            curr_time, employee = heapq.heappop(min_heap)

            # -----------------------------------------------------------------------
            # OPTIMIZATION: Skip if we've already found a better (earlier) time
            # -----------------------------------------------------------------------
            # This is the standard Dijkstra lazy deletion optimization.
            # If curr_time > dist[employee], it means we already processed this
            # employee with a shorter time, so this entry is outdated — skip it.
            if curr_time > dist[employee]:
                continue

            # -----------------------------------------------------------------------
            # STEP 4: Relax edges (inform direct reports)
            # -----------------------------------------------------------------------
            # Employee 'employee' received info at 'curr_time'.
            # They will finish informing their direct reports at:
            #   curr_time + time[employee]
            # (because it takes time[employee] minutes to pass the info)
            inform_time = curr_time + time[employee]

            for neighbor in graph[employee]:
                # If this path gives the neighbor an earlier arrival time, update it
                if inform_time < dist[neighbor]:
                    dist[neighbor] = inform_time
                    # Push the updated (arrival_time, neighbor) into the heap
                    heapq.heappush(min_heap, (inform_time, neighbor))

        # -----------------------------------------------------------------------
        # STEP 5: Determine the final answer
        # -----------------------------------------------------------------------
        # After Dijkstra, dist[i] holds the earliest time employee i is informed.
        #
        # The answer is the MAXIMUM of all dist[i], because:
        # - All paths run in PARALLEL (simultaneous spreading)
        # - We must wait for the LAST employee to be informed
        # - So the total time = max(dist[0], dist[1], ..., dist[n-1])
        #
        # If any dist[i] is still INF, that employee is unreachable → return -1.

        max_time = max(dist)

        # If any employee couldn't be reached, return -1
        if max_time == INF:
            return -1

        return max_time


# -------------------------------------------------------------------------------
# VERIFICATION / TRACING THROUGH EXAMPLES
# -------------------------------------------------------------------------------
# Example 1:
#   n=6, time=[0,3,2,1,4,0], relations=[[0,1],[0,2],[1,3],[1,4],[2,5]]
#   Graph: 0→1, 0→2, 1→3, 1→4, 2→5
#   Dijkstra from 0:
#     dist = [0, INF, INF, INF, INF, INF]
#     Process (0, 0): inform_time = 0+0=0
#       neighbor 1: dist[1]=0 < INF → dist[1]=0, push (0,1)
#       neighbor 2: dist[2]=0 < INF → dist[2]=0, push (0,2)
#     dist = [0, 0, 0, INF, INF, INF]
#     Process (0, 1): inform_time = 0+3=3
#       neighbor 3: dist[3]=3 < INF → dist[3]=3, push (3,3)
#       neighbor 4: dist[4]=3 < INF → dist[4]=3, push (3,4)
#     dist = [0, 0, 0, 3, 3, INF]
#     Process (0, 2): inform_time = 0+2=2
#       neighbor 5: dist[5]=2 < INF → dist[5]=2, push (2,5)
#     dist = [0, 0, 0, 3, 3, 2]
#     Process (2, 5): no neighbors
#     Process (3, 3): no neighbors
#     Process (3, 4): no neighbors
#   Final dist = [0, 0, 0, 3, 3, 2]
#   max(dist) = 3... wait, that gives 3, but expected is 7.
#
# WAIT — I need to re-read the problem. Employee 0 informs employees 1 and 2
# "instantly" (time[0]=0). But employee 1 takes 3 minutes to inform 3 and 4.
# So employee 1 receives info at time 0 (from employee 0 who takes 0 min).
# Then employee 1 informs 3 and 4 at time 0+3=3.
# Employee 4 receives info at time 3, and time[4]=4, but employee 4 has no reports.
# So the answer should be max arrival time = 3+4? No wait...
#
# Re-reading: "time[i] is the number of minutes it takes employee i to inform their
# direct reports". Employee 4 has no direct reports (no outgoing edges from 4).
# So dist[4] = time to RECEIVE info at employee 4 = 0 (time[0]) + 3 (time[1]) = 3.
# But the explanation says "0→1→4 with time 0+3+4=7".
#
# Hmm, the explanation includes time[4]=4 in the path cost. That means the cost
# includes the time for the DESTINATION node too? That seems odd for "time to reach".
#
# Actually wait — re-reading more carefully: "It takes exactly time[i] minutes for
# employee i to pass information to each of their direct reports."
# So the edge weight from i to j is time[i] (the time i takes to pass to j).
# The path 0→1→4: cost = time[0] + time[1] + time[4] = 0+3+4 = 7.
# But why include time[4]? Employee 4 has no direct reports...
#
# OH WAIT. I think the problem means: the total time includes the time for the
# LAST employee to finish informing (even if they have no reports). No, that doesn't
# make sense either.
#
# Let me re-read the explanation: "Employee 0 informs employees 1 and 2 instantly.
# Employee 1 (after 3 min) informs 3 and 4. Employee 2 (after 2 min) informs 5.
# The longest path is 0→1→4 with time 0+3+4=7."
#
# So the path cost is time[0]+time[1]+time[4] = 0+3+4 = 7. But employee 4 has no
# direct reports, so why does time[4] matter?
#
# I think the problem is actually asking: what is the time for ALL employees to
# FINISH informing their reports? Not just receive the info. So the "done" time
# for employee i = arrival_time[i] + time[i].
#
# Let me verify with Example 2:
#   n=4, time=[0,1,2,0], relations=[[0,1],[0,2],[1,3]]
#   arrival times: 0→0, 1→0+0=0, 2→0+0=0, 3→0+1=1
#   done times: 0→0+0=0, 1→0+1=1, 2→0+2=2, 3→1+0=1
#   max done time = 2. Expected output: 2. ✓
#
# And Example 1:
#   arrival times: 0→0, 1→0, 2→0, 3→3, 4→3, 5→2
#   done times: 0→0, 1→3, 2→2, 3→4, 4→7, 5→2
#   max done time = 7. Expected output: 7. ✓
#
# So the answer is max(dist[i] + time[i]) for all i!
# I need to fix my solution.
# -------------------------------------------------------------------------------


class SolutionFixed:
    def min_time_to_inform(
        self, n: int, time: List[int], relations: List[List[int]]
    ) -> int:
        """
        Find the minimum time for information to spread from employee 0 to all employees.

        Key Insight:
        - dist[i] = earliest time employee i RECEIVES the information
        - The "done" time for employee i = dist[i] + time[i]
          (time to receive + time to finish informing their own reports)
        - Answer = max(dist[i] + time[i]) for all i reachable from 0
        - If any employee is unreachable, return -1

        We use Dijkstra's algorithm to find the shortest (earliest) arrival time
        for each employee, then compute the maximum "done" time.

        Args:
            n: Number of employees (nodes 0 to n-1)
            time: time[i] = minutes employee i takes to inform their direct reports
            relations: List of [a, b] meaning employee a can directly inform employee b

        Returns:
            Minimum number of minutes for all employees to be informed (and finish
            informing their own reports), or -1 if impossible.

        Time Complexity: O((V + E) * log V) where V = n, E = len(relations)
        Space Complexity: O(V + E)
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build the adjacency list (directed graph)
        # -----------------------------------------------------------------------
        # graph[a] = list of employees that a can directly inform
        graph: Dict[int, List[int]] = defaultdict(list)

        for a, b in relations:
            graph[a].append(b)

        # -----------------------------------------------------------------------
        # STEP 2: Initialize the distance (earliest arrival time) array
        # -----------------------------------------------------------------------
        # dist[i] = earliest time employee i RECEIVES the information
        # Start: employee 0 has info at time 0
        INF = float('inf')
        dist = [INF] * n
        dist[0] = 0

        # -----------------------------------------------------------------------
        # STEP 3: Dijkstra's Algorithm
        # -----------------------------------------------------------------------
        # min-heap entries: (arrival_time, employee_id)
        # We process employees in order of their earliest arrival time.
        #
        # Edge weight from employee u to employee v = time[u]
        # (it takes time[u] minutes for u to pass info to v)
        #
        # So: dist[v] = min(dist[v], dist[u] + time[u])
        min_heap: List[tuple] = [(0, 0)]

        while min_heap:
            curr_time, employee = heapq.heappop(min_heap)

            # Skip outdated entries (lazy deletion)
            if curr_time > dist[employee]:
                continue

            # The time at which employee's direct reports receive the info
            # = curr_time (when employee received it) + time[employee] (time to pass it)
            inform_time = curr_time + time[employee]

            for neighbor in graph[employee]:
                # Relax the edge: can we inform the neighbor earlier?
                if inform_time < dist[neighbor]:
                    dist[neighbor] = inform_time
                    heapq.heappush(min_heap, (inform_time, neighbor))

        # -----------------------------------------------------------------------
        # STEP 4: Compute