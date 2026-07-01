"""
Title: Nearest Exit Gate in an Office Floor

Problem Description:
You are given an office floor represented as an undirected graph. Each room is numbered
from 0 to n - 1, and hallways connect pairs of rooms. Some rooms contain emergency exit
gates. Starting from a given room, you must determine the minimum number of hallways an
employee needs to walk through to reach any exit gate.

Return the smallest number of edges from the start room to any exit room. If the starting
room is itself an exit room, return 0. If no exit gate is reachable, return -1.

The graph may be disconnected, and there may be multiple possible exit rooms. All hallways
can be used in both directions, and every hallway has the same cost of 1 step. This makes
the problem a shortest-path search in an unweighted graph.

Your task is to write a function that takes the number of rooms n, a list of hallways
edges where each edge is [u, v], a list exits containing all rooms with exit gates, and
an integer start representing the employee's current room.

Constraints:
- 1 <= n <= 10^5
- 0 <= edges.length <= 2 * 10^5
- edges[i].length == 2
- 0 <= u, v < n
- u != v
- 1 <= exits.length <= n
- 0 <= exits[i] < n
- 0 <= start < n
- There are no duplicate hallways.
"""

from collections import deque
from typing import Deque, List, Set


class Solution:
    def nearest_exit_distance(
        self,
        n: int,
        edges: List[List[int]],
        exits: List[int],
        start: int,
    ) -> int:
        """
        Find the minimum number of hallways needed to reach any exit room.

        This method models the office floor as an undirected, unweighted graph and uses
        Breadth-First Search (BFS) starting from the given room. BFS is the correct choice
        because in an unweighted graph, the first time we reach a node, we have found the
        shortest path to it in terms of number of edges.

        Args:
            n: Total number of rooms labeled from 0 to n - 1.
            edges: List of undirected hallways, where each hallway is represented as [u, v].
            exits: List of rooms that contain emergency exit gates.
            start: The room where the employee starts.

        Returns:
            The minimum number of hallways from start to any exit room.
            Returns 0 if start is already an exit.
            Returns -1 if no exit is reachable.

        Time complexity:
            O(n + m), where m is the number of edges.
            We build the adjacency list in O(m), and BFS visits each node and edge at most once.

        Space complexity:
            O(n + m) for the adjacency list, visited structure, and BFS queue.
        """
        # Convert the list of exit rooms into a set.
        #
        # Why do this?
        # A set allows O(1) average-time membership checks.
        # During BFS, we repeatedly ask:
        # "Is this room an exit room?"
        # Using a set makes that check very fast.
        exit_set: Set[int] = set(exits)

        # Important early check:
        # If the starting room is already an exit room, then the employee does not need
        # to walk through any hallway at all.
        # The answer is immediately 0.
        if start in exit_set:
            return 0

        # Build the adjacency list representation of the graph.
        #
        # adjacency[room] will contain a list of all neighboring rooms directly connected
        # by one hallway.
        #
        # We use a list of lists because:
        # - Room numbers are from 0 to n - 1
        # - This makes indexing direct and efficient
        # - It is memory-efficient and fast for graph traversal
        adjacency: List[List[int]] = [[] for _ in range(n)]

        # Since the graph is undirected, every hallway [u, v] means:
        # - u connects to v
        # - v connects to u
        #
        # So we add both directions to the adjacency list.
        for u, v in edges:
            adjacency[u].append(v)
            adjacency[v].append(u)

        # Prepare BFS.
        #
        # We use a queue because BFS explores rooms level by level:
        # - First all rooms at distance 1
        # - Then all rooms at distance 2
        # - Then all rooms at distance 3
        # and so on.
        #
        # This guarantees that the first exit room we encounter is the nearest one.
        queue: Deque[int] = deque([start])

        # visited keeps track of rooms we have already added to the BFS process.
        #
        # Why is this necessary?
        # - It prevents revisiting the same room again and again
        # - It avoids infinite loops in graphs with cycles
        # - It ensures each room is processed at most once
        visited: List[bool] = [False] * n
        visited[start] = True

        # distance stores how many hallways away the current BFS layer is from the start.
        #
        # At the beginning:
        # - queue contains only the start room
        # - start is distance 0
        #
        # After processing one full layer, we increase distance by 1.
        distance: int = 0

        # Standard BFS loop:
        # Continue until there are no more reachable rooms to explore.
        while queue:
            # The current queue contains all rooms at the same distance from start.
            #
            # We process exactly this many rooms before increasing the distance.
            # This is the key idea that lets us track shortest path length without
            # storing a separate distance for every queued node.
            level_size: int = len(queue)

            # Process one full BFS layer.
            for _ in range(level_size):
                current_room: int = queue.popleft()

                # Explore all directly connected neighboring rooms.
                for neighbor in adjacency[current_room]:
                    # If we have already visited this neighbor before, skip it.
                    #
                    # Why skip?
                    # Because the first time we reach a room in BFS is always via the
                    # shortest possible path. Any later visit would be equal or longer,
                    # so it is not useful.
                    if visited[neighbor]:
                        continue

                    # Mark the neighbor as visited as soon as we enqueue it.
                    #
                    # Why mark here instead of later?
                    # This prevents the same room from being added to the queue multiple
                    # times by different parents in the same BFS layer.
                    visited[neighbor] = True

                    # The neighbor is one hallway farther than current_room.
                    #
                    # Since current_room belongs to the current layer at distance `distance`,
                    # every unvisited neighbor belongs to the next layer at distance
                    # `distance + 1`.
                    #
                    # If this neighbor is an exit room, then we have found the nearest
                    # reachable exit, and we can return immediately.
                    if neighbor in exit_set:
                        return distance + 1

                    # Otherwise, add the neighbor to the queue so it can be explored
                    # in the next BFS layer.
                    queue.append(neighbor)

            # After finishing the entire current layer, we move one step farther away
            # from the start room.
            distance += 1

        # If BFS finishes and we never reached an exit room, then no exit is reachable
        # from the starting room.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Graph:
    # 0 - 1 - 2 - 3
    #     |
    #     4 - 5 - 6
    #
    # Exits are rooms 3 and 6, start is 0.
    # Shortest path to an exit:
    # 0 -> 1 -> 2 -> 3
    # Number of hallways = 3
    n1: int = 7
    edges1: List[List[int]] = [[0, 1], [1, 2], [2, 3], [1, 4], [4, 5], [5, 6]]
    exits1: List[int] = [3, 6]
    start1: int = 0
    result1: int = solution.nearest_exit_distance(n1, edges1, exits1, start1)
    print(result1)  # Expected: 3

    # Example 2
    # Graph has two disconnected components:
    # 0 - 1 - 2      3 - 4
    #
    # Exit is room 4, start is 0.
    # Since room 4 is in a different connected component, it cannot be reached.
    n2: int = 5
    edges2: List[List[int]] = [[0, 1], [1, 2], [3, 4]]
    exits2: List[int] = [4]
    start2: int = 0
    result2: int = solution.nearest_exit_distance(n2, edges2, exits2, start2)
    print(result2)  # Expected: -1

    # Additional quick check:
    # If the start room is already an exit, answer should be 0.
    n3: int = 3
    edges3: List[List[int]] = [[0, 1], [1, 2]]
    exits3: List[int] = [1]
    start3: int = 1
    result3: int = solution.nearest_exit_distance(n3, edges3, exits3, start3)
    print(result3)  # Expected: 0