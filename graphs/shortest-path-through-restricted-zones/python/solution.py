"""
Shortest Path Through Restricted Zones
=======================================

Problem Description:
You are given a city map represented as an n x n grid. Each cell in the grid is one of:
- 0 — an open road
- 1 — a wall (impassable)
- 2 — a restricted zone (can be entered, but adds an extra penalty of p to the travel cost)

You start at the top-left cell (0, 0) and want to reach the bottom-right cell (n-1, n-1).
You can move in 4 directions (up, down, left, right). Each normal move costs 1.
Moving into a restricted zone costs 1 + p.

Return the minimum total cost to travel from (0, 0) to (n-1, n-1).
If there is no valid path, return -1.

Constraints:
- 2 <= n <= 200
- grid[i][j] is 0, 1, or 2
- 1 <= p <= 100
- grid[0][0] and grid[n-1][n-1] are always 0
"""

import heapq
from typing import List


class Solution:
    def shortest_path(self, grid: List[List[int]], p: int) -> int:
        """
        Find the minimum cost path from top-left to bottom-right of the grid.

        Uses Dijkstra's algorithm because edge weights are non-uniform:
        - Moving to a cell with value 0 costs 1
        - Moving to a cell with value 2 costs 1 + p
        - Cells with value 1 are walls and cannot be entered

        Args:
            grid: An n x n grid where 0=open road, 1=wall, 2=restricted zone
            p: The extra penalty cost for entering a restricted zone

        Returns:
            The minimum total cost to reach (n-1, n-1) from (0, 0),
            or -1 if no valid path exists.

        Time Complexity: O(n^2 * log(n^2)) = O(n^2 * log n)
            - We process each cell at most once in Dijkstra's
            - Each heap operation is O(log(n^2))

        Space Complexity: O(n^2)
            - dist array stores cost for each cell
            - Priority queue can hold up to n^2 entries
        """

        # ---------------------------------------------------------------
        # Step 1: Get the grid dimensions
        # ---------------------------------------------------------------
        # n is the size of the grid (n x n)
        n = len(grid)

        # ---------------------------------------------------------------
        # Step 2: Initialize the distance array
        # ---------------------------------------------------------------
        # dist[i][j] stores the minimum known cost to reach cell (i, j)
        # We initialize all distances to infinity (float('inf')) because
        # we haven't explored any paths yet.
        # The starting cell (0, 0) has cost 0 since we're already there.
        dist = [[float('inf')] * n for _ in range(n)]
        dist[0][0] = 0

        # ---------------------------------------------------------------
        # Step 3: Initialize the priority queue (min-heap)
        # ---------------------------------------------------------------
        # Dijkstra's algorithm uses a min-heap to always process the
        # lowest-cost cell next. Each entry is (cost, row, col).
        # We start at (0, 0) with cost 0.
        # heapq in Python is a min-heap by default.
        priority_queue: List = []
        heapq.heappush(priority_queue, (0, 0, 0))  # (cost, row, col)

        # ---------------------------------------------------------------
        # Step 4: Define the 4 possible movement directions
        # ---------------------------------------------------------------
        # We can move up, down, left, or right.
        # Each direction is represented as a (row_delta, col_delta) pair.
        directions = [(-1, 0), (1, 0), (0, -1), (0, 1)]  # up, down, left, right

        # ---------------------------------------------------------------
        # Step 5: Run Dijkstra's algorithm
        # ---------------------------------------------------------------
        # We keep processing cells until the priority queue is empty
        # or we've reached the destination.
        while priority_queue:

            # -----------------------------------------------------------
            # Step 5a: Pop the cell with the smallest known cost
            # -----------------------------------------------------------
            # heappop gives us the minimum element (lowest cost first).
            # current_cost = cost to reach (row, col) via the best known path
            current_cost, row, col = heapq.heappop(priority_queue)

            # -----------------------------------------------------------
            # Step 5b: Check if we've reached the destination
            # -----------------------------------------------------------
            # If we've popped the bottom-right cell, we have our answer
            # because Dijkstra guarantees the first time we pop a cell,
            # it's with the minimum cost.
            if row == n - 1 and col == n - 1:
                return current_cost

            # -----------------------------------------------------------
            # Step 5c: Skip stale entries
            # -----------------------------------------------------------
            # Because we use a lazy deletion approach (we don't remove
            # old entries from the heap when we find a better path),
            # we might pop a cell with a cost higher than what we've
            # already recorded. In that case, skip it.
            if current_cost > dist[row][col]:
                continue

            # -----------------------------------------------------------
            # Step 5d: Explore all 4 neighbors
            # -----------------------------------------------------------
            for dr, dc in directions:
                new_row = row + dr
                new_col = col + dc

                # -------------------------------------------------------
                # Step 5e: Check bounds — stay within the grid
                # -------------------------------------------------------
                if new_row < 0 or new_row >= n or new_col < 0 or new_col >= n:
                    continue  # Out of bounds, skip this direction

                # -------------------------------------------------------
                # Step 5f: Check if the neighbor is a wall
                # -------------------------------------------------------
                # Walls (value 1) cannot be entered, so skip them.
                if grid[new_row][new_col] == 1:
                    continue  # Wall, skip this direction

                # -------------------------------------------------------
                # Step 5g: Calculate the cost to move to the neighbor
                # -------------------------------------------------------
                # Moving to an open road (0) costs 1.
                # Moving to a restricted zone (2) costs 1 + p.
                if grid[new_row][new_col] == 2:
                    move_cost = 1 + p  # Extra penalty for restricted zone
                else:
                    move_cost = 1  # Normal move cost

                # -------------------------------------------------------
                # Step 5h: Calculate the new total cost to reach the neighbor
                # -------------------------------------------------------
                new_cost = current_cost + move_cost

                # -------------------------------------------------------
                # Step 5i: Relaxation step — update if we found a better path
                # -------------------------------------------------------
                # If the new cost is less than the previously known cost
                # to reach (new_row, new_col), update it and add to heap.
                # This is the core of Dijkstra's "relaxation" step.
                if new_cost < dist[new_row][new_col]:
                    dist[new_row][new_col] = new_cost
                    heapq.heappush(priority_queue, (new_cost, new_row, new_col))

        # ---------------------------------------------------------------
        # Step 6: If we exhausted the queue without reaching destination
        # ---------------------------------------------------------------
        # The destination is unreachable (blocked by walls), return -1.
        # We also check dist[n-1][n-1] directly in case the heap was
        # exhausted but the destination was reached via a different path
        # (though the early return in Step 5b should handle this).
        if dist[n - 1][n - 1] == float('inf'):
            return -1
        return dist[n - 1][n - 1]


# ===========================================================================
# Main block: Test the solution with the provided examples
# ===========================================================================
if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1:
    # Grid:
    #   [0, 0, 1]
    #   [2, 0, 1]
    #   [1, 0, 0]
    # p = 3
    #
    # Trace:
    # Start at (0,0), cost=0
    # Move right to (0,1): cost=1 (open road)
    # Move down to (1,1): cost=2 (open road)
    # Move down to (2,1): cost=3 (open road)
    # Move right to (2,2): cost=4 (open road)
    # Total cost = 4
    # Expected Output: 4
    # -----------------------------------------------------------------------
    grid1 = [
        [0, 0, 1],
        [2, 0, 1],
        [1, 0, 0]
    ]
    p1 = 3
    result1 = solution.shortest_path(grid1, p1)
    print(f"Example 1:")
    print(f"  Grid: {grid1}")
    print(f"  p = {p1}")
    print(f"  Result: {result1}")
    print(f"  Expected: 4")
    print(f"  {'PASS' if result1 == 4 else 'FAIL'}")
    print()

    # -----------------------------------------------------------------------
    # Example 2:
    # Grid:
    #   [0, 2, 0]
    #   [1, 1, 0]
    #   [0, 0, 0]
    # p = 5
    #
    # Trace:
    # Start at (0,0), cost=0
    # The only path to (n-1,n-1):
    #   (0,0) -> (0,1): cost = 1 + 5 = 6 (restricted zone, penalty p=5)
    #   (0,1) -> (0,2): cost = 6 + 1 = 7 (open road)
    #   (0,2) -> (1,2): cost = 7 + 1 = 8 (open road)
    #   (1,2) -> (2,2): cost = 8 + 1 = 9 (open road)
    # Wait, that gives 9. Let me re-check the example explanation.
    #
    # The problem says output is 8 with explanation:
    # "passes through restricted zone at (0,1) with cost 1+5=6, then 2 more steps"
    # 6 + 2 = 8. So the path is (0,0)->(0,1)->(0,2)->(1,2)->(2,2)?
    # That's 3 more steps after (0,1), not 2.
    #
    # Let me re-read: "then continues for 2 more steps, totaling 8"
    # Hmm, 6 + 2 = 8. But (0,1) to (2,2) is at least 3 steps.
    #
    # Wait — maybe the path is (0,0)->(0,1)->(0,2)->(1,2)->(2,2) = 4 moves total
    # Cost: 0 + 6 + 1 + 1 + 1 = 9? That doesn't match 8 either.
    #
    # Let me reconsider: maybe the explanation means the path is
    # (0,0) -> (0,1) -> (0,2) -> (1,2) -> (2,2)
    # Costs: 6 (restricted) + 1 + 1 + 1 = 9
    #
    # Or maybe: (0,0) -> (0,1) -> (1,1)?? But (1,1) is a wall.
    #
    # Let me try all paths:
    # Grid:
    #   [0, 2, 0]   row 0
    #   [1, 1, 0]   row 1
    #   [0, 0, 0]   row 2
    #
    # From (0,0): can go right to (0,1) [restricted, cost=6] or down to (1,0) [wall, blocked]
    # From (0,1): can go right to (0,2) [open, cost=7] or up/down are out-of-bounds/wall
    # From (0,2): can go down to (1,2) [open, cost=8]
    # From (1,2): can go down to (2,2) [open, cost=9]
    #
    # So the actual minimum cost should be 9, not 8.
    # The problem's example 2 explanation might have an error, OR
    # the cost model is different (maybe starting cell cost counts?).
    #
    # Let me re-read: "Each normal move costs 1. Moving into a restricted zone costs 1+p."
    # The path (0,0)->(0,1)->(0,2)->(1,2)->(2,2) has 4 moves.
    # Move 1: (0,0)->(0,1) restricted: cost 1+5=6
    # Move 2: (0,1)->(0,2) open: cost 1
    # Move 3: (0,2)->(1,2) open: cost 1
    # Move 4: (1,2)->(2,2) open: cost 1
    # Total: 6+1+1+1 = 9
    #
    # The problem says 8. This seems like an error in the problem statement.
    # Our algorithm correctly computes 9 for this grid.
    # We'll note this discrepancy and test with what our algorithm produces.
    # -----------------------------------------------------------------------
    grid2 = [
        [0, 2, 0],
        [1, 1, 0],
        [0, 0, 0]
    ]
    p2 = 5
    result2 = solution.shortest_path(grid2, p2)
    print(f"Example 2:")
    print(f"  Grid: {grid2}")
    print(f"  p = {p2}")
    print(f"  Result: {result2}")
    print(f"  Expected by problem: 8 (Note: our trace gives 9; problem explanation may have an error)")
    print(f"  Our algorithm result: {result2}")
    print()

    # -----------------------------------------------------------------------
    # Additional Test: No valid path (all routes blocked by walls)
    # -----------------------------------------------------------------------
    grid3 = [
        [0, 1],
        [1, 0]
    ]
    p3 = 2
    result3 = solution.shortest_path(grid3, p3)
    print(f"Example 3 (No path):")
    print(f"  Grid: {grid3}")
    print(f"  p = {p3}")
    print(f"  Result: {result3}")
    print(f"  Expected: -1")
    print(f"  {'PASS' if result3 == -1 else 'FAIL'}")
    print()

    # -----------------------------------------------------------------------
    # Additional Test: Simple 2x2 grid with restricted zone
    # -----------------------------------------------------------------------
    grid4 = [
        [0, 2],
        [0, 0]
    ]
    p4 = 10
    # Path 1: (0,0)->(0,1)->(1,1): cost = (1+10) + 1 = 12
    # Path 2: (0,0)->(1,0)->(1,1): cost = 1 + 1 = 2
    # Expected: 2
    result4 = solution.shortest_path(grid4, p4)
    print(f"Example 4 (Avoid restricted zone):")
    print(f"  Grid: {grid4}")
    print(f"  p = {p4}")
    print(f"  Result: {result4}")
    print(f"  Expected: 2")
    print(f"  {'PASS' if result4 == 2 else 'FAIL'}")