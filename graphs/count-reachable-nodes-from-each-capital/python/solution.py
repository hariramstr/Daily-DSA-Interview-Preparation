```python
"""
Title: Count Reachable Nodes from Each Capital

Problem Description:
You are given a country represented as an undirected tree with n cities numbered
from 0 to n - 1. City 0 is the national capital. Each city may also have a
designated 'regional capital' — specifically, some cities are marked as regional
capitals in an array capitals, where capitals[i] = 1 means city i is a regional
capital, and capitals[i] = 0 means it is not.

For each regional capital, determine how many cities (including itself) are
reachable from it without passing through any other regional capital or the
national capital (city 0).

You are given:
- An integer n — the number of cities.
- A 2D integer array edges of size n - 1, where edges[i] = [u, v] represents
  a bidirectional road between cities u and v.
- An integer array capitals of size n.

Return an array result where result[i] is the count of reachable cities for the
i-th regional capital (in the order they appear, i.e., cities where capitals[i] = 1).
"""

from typing import List, Dict
from collections import defaultdict, deque


class Solution:
    def countReachableNodes(
        self, n: int, edges: List[List[int]], capitals: List[int]
    ) -> List[int]:
        """
        For each regional capital, count how many cities are reachable from it
        without passing through any other regional capital or city 0.

        Args:
            n (int): Number of cities (nodes in the tree).
            edges (List[List[int]]): Undirected edges of the tree.
            capitals (List[int]): capitals[i] = 1 if city i is a regional capital.

        Returns:
            List[int]: A list where each entry is the reachable city count for
                       each regional capital (in order of city index).

        Time Complexity:  O(k * n) where k is the number of regional capitals
                          and n is the number of cities. In the worst case each
                          BFS/DFS visits all n nodes.
        Space Complexity: O(n) for the adjacency list and the visited set used
                          during each BFS traversal.
        """

        # -----------------------------------------------------------------------
        # Step 1: Build an adjacency list representation of the undirected tree.
        #
        # An adjacency list maps each city to its list of directly connected
        # neighbors. We use defaultdict(list) so we never get a KeyError when
        # accessing a city that hasn't been explicitly added yet.
        # -----------------------------------------------------------------------
        graph: Dict[int, List[int]] = defaultdict(list)

        for u, v in edges:
            # Because the tree is undirected, add both directions.
            graph[u].append(v)
            graph[v].append(u)

        # -----------------------------------------------------------------------
        # Step 2: Identify the set of "barrier" nodes.
        #
        # A barrier node is any node that acts as a boundary during traversal.
        # These are:
        #   - City 0 (the national capital) — always a barrier.
        #   - Any city marked as a regional capital (capitals[i] == 1).
        #
        # When we do BFS from a regional capital, we must NOT cross through
        # any barrier node OTHER than the starting capital itself.
        # -----------------------------------------------------------------------
        # Collect all regional capital city indices (where capitals[i] == 1).
        regional_capitals: List[int] = [
            city for city, is_capital in enumerate(capitals) if is_capital == 1
        ]

        # Build a set of ALL barrier nodes for O(1) lookup during BFS.
        # City 0 is always a barrier (national capital).
        # Every regional capital is also a barrier.
        all_barriers: set = set(regional_capitals)
        all_barriers.add(0)  # City 0 is the national capital — always a barrier.

        # -----------------------------------------------------------------------
        # Step 3: For each regional capital, perform a BFS to count reachable
        #         cities.
        #
        # BFS (Breadth-First Search) is a natural fit here because we want to
        # explore all cities reachable from a starting node without crossing
        # any barrier.
        #
        # Rules for BFS from a regional capital `start`:
        #   - We CAN visit `start` itself (it's the origin).
        #   - We CANNOT enter any other barrier node (other regional capitals
        #     or city 0).
        #   - We count every node we successfully visit (including `start`).
        # -----------------------------------------------------------------------
        result: List[int] = []

        for start in regional_capitals:
            # --- BFS initialization ---
            # `visited` tracks nodes we have already counted to avoid revisiting.
            visited: set = {start}

            # `queue` holds nodes to explore next (standard BFS queue).
            queue: deque = deque([start])

            # `count` accumulates the number of reachable cities.
            count: int = 0

            # --- BFS traversal ---
            while queue:
                current = queue.popleft()

                # Count this city as reachable.
                count += 1

                # Explore all neighbors of the current city.
                for neighbor in graph[current]:
                    # Skip if already visited (prevents infinite loops in the
                    # undirected graph).
                    if neighbor in visited:
                        continue

                    # Skip if the neighbor is a barrier node (another regional
                    # capital or city 0). We must NOT pass through barriers.
                    # Note: `start` itself is in `all_barriers` but we already
                    # added it to `visited` before the loop, so it will be
                    # caught by the `visited` check above — no double-counting.
                    if neighbor in all_barriers:
                        continue

                    # The neighbor is a valid, unvisited, non-barrier city.
                    # Mark it visited and add to the queue for further exploration.
                    visited.add(neighbor)
                    queue.append(neighbor)

            # After BFS completes, `count` holds the total reachable cities
            # from this regional capital. Append to results.
            result.append(count)

        # -----------------------------------------------------------------------
        # Step 4: Return the result list.
        #
        # The list is already in the correct order because we iterated over
        # `regional_capitals` which was built by scanning cities 0..n-1 in order.
        # -----------------------------------------------------------------------
        return result


# ---------------------------------------------------------------------------
# Manual trace verification
# ---------------------------------------------------------------------------
# Example 1:
#   n=6, edges=[[0,1],[1,2],[1,3],[3,4],[3,5]], capitals=[0,1,0,1,0,0]
#   Graph: 0-1, 1-2, 1-3, 3-4, 3-5
#   Regional capitals: [1, 3]
#   all_barriers: {0, 1, 3}
#
#   BFS from city 1:
#     Start: visited={1}, queue=[1], count=0
#     Pop 1 → count=1, neighbors: [0,2,3]
#       0 → in all_barriers → skip
#       2 → not visited, not barrier → add; visited={1,2}, queue=[2]
#       3 → in all_barriers → skip
#     Pop 2 → count=2, neighbors: [1]
#       1 → in visited → skip
#     Queue empty. count=2 ✓
#
#   BFS from city 3:
#     Start: visited={3}, queue=[3], count=0
#     Pop 3 → count=1, neighbors: [1,4,5]
#       1 → in all_barriers → skip
#       4 → not visited, not barrier → add; visited={3,4}, queue=[4]
#       5 → not visited, not barrier → add; visited={3,4,5}, queue=[4,5]
#     Pop 4 → count=2, neighbors: [3]
#       3 → in visited → skip
#     Pop 5 → count=3, neighbors: [3]
#       3 → in visited → skip
#     Queue empty. count=3 ✓
#
#   Result: [2, 3] ✓
#
# Example 2:
#   n=4, edges=[[0,1],[1,2],[2,3]], capitals=[0,1,0,0]
#   Graph: 0-1, 1-2, 2-3
#   Regional capitals: [1]
#   all_barriers: {0, 1}
#
#   BFS from city 1:
#     Start: visited={1}, queue=[1], count=0
#     Pop 1 → count=1, neighbors: [0,2]
#       0 → in all_barriers → skip
#       2 → not visited, not barrier → add; visited={1,2}, queue=[2]
#     Pop 2 → count=2, neighbors: [1,3]
#       1 → in visited → skip
#       3 → not visited, not barrier → add; visited={1,2,3}, queue=[3]
#     Pop 3 → count=3, neighbors: [2]
#       2 → in visited → skip
#     Queue empty. count=3 ✓
#
#   Result: [3] ✓
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    n1 = 6
    edges1 = [[0, 1], [1, 2], [1, 3], [3, 4], [3, 5]]
    capitals1 = [0, 1, 0, 1, 0, 0]
    output1 = solution.countReachableNodes(n1, edges1, capitals1)
    print("Example 1:")
    print(f"  n        = {n1}")
    print(f"  edges    = {edges1}")
    print(f"  capitals = {capitals1}")
    print(f"  Output   = {output1}")   # Expected: [2, 3]
    print()

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    n2 = 4
    edges2 = [[0, 1], [1, 2], [2, 3]]
    capitals2 = [0, 1, 0, 0]
    output2 = solution.countReachableNodes(n2, edges2, capitals2)
    print("Example 2:")
    print(f"  n        = {n2}")
    print(f"  edges    = {edges2}")
    print(f"  capitals = {capitals2}")
    print(f"  Output   = {output2}")   # Expected: [3]
    print()

    # ------------------------------------------------------------------
    # Edge case: only the national capital exists, no regional capitals
    # ------------------------------------------------------------------
    n3 = 1
    edges3: List[List[int]] = []
    capitals3 = [0]
    output3 = solution.countReachableNodes(n3, edges3, capitals3)
    print("Edge case (no regional capitals):")
    print(f"  n        = {n3}")
    print(f"  edges    = {edges3}")
    print(f"  capitals = {capitals3}")
    print(f"  Output   = {output3}")   # Expected: []
    print()

    # ------------------------------------------------------------------
    # Edge case: two adjacent regional capitals
    # ------------------------------------------------------------------
    n4 = 4
    edges4 = [[0, 1], [1, 2], [2, 3]]
    capitals4 = [0, 1, 1, 0]
    output4 = solution.countReachableNodes(n4, edges4, capitals4)
    print("Edge case (two adjacent regional capitals):")
    print(f"  n        = {n4}")
    print(f"  edges    = {edges4}")
    print(f"  capitals = {capitals4}")
    print(f"  Output   = {output4}")
    # City 1: neighbors are 0 (barrier) and 2 (barrier) → count = 1
    # City 2: neighbors are 1 (barrier) and 3 (not barrier) → count = 2
    # Expected: [1, 2]
    print()
```