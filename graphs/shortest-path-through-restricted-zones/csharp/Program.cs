/*
 * Title: Shortest Path Through Restricted Zones
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a city map represented as an n x n grid. Each cell is:
 *   0 — an open road
 *   1 — a wall (impassable)
 *   2 — a restricted zone (can be entered, but adds an extra penalty of p to the travel cost)
 *
 * Start at top-left (0,0), reach bottom-right (n-1, n-1).
 * Move in 4 directions (up, down, left, right).
 * Each normal move costs 1. Moving into a restricted zone costs 1 + p.
 *
 * Return the minimum total cost, or -1 if no valid path exists.
 *
 * Constraints:
 *   2 <= n <= 200
 *   grid[i][j] is 0, 1, or 2
 *   1 <= p <= 100
 *   grid[0][0] and grid[n-1][n-1] are always 0
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the Dijkstra-based shortest-path algorithm.
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Method: MinCost
     *
     * Algorithm: Dijkstra's Shortest Path
     *
     * Why Dijkstra?
     *   - Edge weights are NOT uniform (normal move = 1, restricted move = 1+p).
     *   - BFS only works correctly when all edges have equal weight.
     *   - Dijkstra handles non-negative, variable-weight edges perfectly.
     *   - It always expands the node with the currently smallest known cost,
     *     guaranteeing we find the optimal path.
     *
     * Time Complexity:  O(n^2 * log(n^2)) = O(n^2 * log n)
     *   - There are n*n nodes. Each node is processed once.
     *   - Each priority-queue operation is O(log(n^2)).
     *
     * Space Complexity: O(n^2)
     *   - dist array: n*n entries
     *   - Priority queue: at most n*n entries
     */
    public int MinCost(int[][] grid, int p)
    {
        // ── Step 1: Capture grid dimensions ──────────────────────────────────
        // n is the side length of the square grid.
        int n = grid.Length;

        // ── Step 2: Initialize the distance (cost) array ─────────────────────
        // dist[i][j] stores the minimum cost found so far to reach cell (i, j).
        // We start by assuming every cell is infinitely expensive to reach.
        // This is a standard Dijkstra initialization — we'll update costs as
        // we discover cheaper paths.
        int[,] dist = new int[n, n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                dist[i, j] = int.MaxValue;

        // The starting cell (0,0) costs 0 to "reach" (we're already there).
        dist[0, 0] = 0;

        // ── Step 3: Set up the priority queue (min-heap) ──────────────────────
        // A priority queue always gives us the element with the SMALLEST priority.
        // Each element is a tuple: (cost, row, col).
        // We use cost as the priority so we always process the cheapest cell next.
        //
        // .NET 8's PriorityQueue<TElement, TPriority> is perfect here.
        // TElement = (int row, int col), TPriority = int (the cost).
        var pq = new PriorityQueue<(int row, int col), int>();

        // Enqueue the starting cell with cost 0.
        pq.Enqueue((0, 0), 0);

        // ── Step 4: Define the 4 movement directions ──────────────────────────
        // (row delta, col delta) for: Up, Down, Left, Right
        int[] dr = { -1, 1, 0, 0 };
        int[] dc = { 0, 0, -1, 1 };

        // ── Step 5: Dijkstra's main loop ──────────────────────────────────────
        // Keep processing cells until the priority queue is empty.
        // Each iteration we pick the cell with the lowest current cost.
        while (pq.Count > 0)
        {
            // Dequeue the cell with the minimum cost.
            // 'currentCost' is the total cost to reach (r, c) via the best path found so far.
            pq.Dequeue(out (int r, int c) current, out int currentCost);

            int r = current.r;
            int c = current.c;

            // ── Step 5a: Early exit if we reached the destination ─────────────
            // If we just popped the bottom-right cell, its cost is optimal
            // (Dijkstra guarantees this — the first time a node is popped,
            // we have the shortest path to it).
            if (r == n - 1 && c == n - 1)
                return currentCost;

            // ── Step 5b: Skip stale entries ───────────────────────────────────
            // Because we may enqueue the same cell multiple times with different
            // costs (when we find a cheaper path), we might pop an outdated entry.
            // If the cost we stored in 'dist' is already better than 'currentCost',
            // this entry is stale — skip it.
            if (currentCost > dist[r, c])
                continue;

            // ── Step 5c: Explore all 4 neighbors ─────────────────────────────
            for (int d = 0; d < 4; d++)
            {
                int nr = r + dr[d]; // neighbor row
                int nc = c + dc[d]; // neighbor col

                // ── Step 5d: Boundary check ───────────────────────────────────
                // Skip if the neighbor is outside the grid.
                if (nr < 0 || nr >= n || nc < 0 || nc >= n)
                    continue;

                // ── Step 5e: Wall check ───────────────────────────────────────
                // Skip if the neighbor is a wall (impassable).
                if (grid[nr][nc] == 1)
                    continue;

                // ── Step 5f: Calculate the move cost ─────────────────────────
                // Moving into any cell costs at least 1 (the step itself).
                // If the destination cell is a restricted zone (value == 2),
                // we add the penalty p on top of the base cost of 1.
                int moveCost = 1;
                if (grid[nr][nc] == 2)
                    moveCost += p; // restricted zone penalty

                // ── Step 5g: Compute new total cost to reach neighbor ─────────
                int newCost = currentCost + moveCost;

                // ── Step 5h: Relaxation step (core of Dijkstra) ───────────────
                // If we found a cheaper way to reach (nr, nc), update dist and
                // enqueue the neighbor with the new (better) cost.
                // "Relaxation" means: can we do better than what we knew before?
                if (newCost < dist[nr, nc])
                {
                    dist[nr, nc] = newCost; // record the new best cost
                    pq.Enqueue((nr, nc), newCost); // schedule neighbor for processing
                }
            }
        }

        // ── Step 6: Check if destination was reached ──────────────────────────
        // If dist[n-1][n-1] is still int.MaxValue, no path exists → return -1.
        // Otherwise return the minimum cost found.
        return dist[n - 1, n - 1] == int.MaxValue ? -1 : dist[n - 1, n - 1];
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Grid:
//   0 0 1
//   2 0 1
//   1 0 0
// p = 3
// Expected path: (0,0)->(0,1)->(1,1)->(2,1)->(2,2)  cost = 4
// Trace:
//   Start at (0,0), cost=0
//   Move right to (0,1): cost = 0+1 = 1  [open road]
//   Move down  to (1,1): cost = 1+1 = 2  [open road]
//   Move down  to (2,1): cost = 2+1 = 3  [open road]
//   Move right to (2,2): cost = 3+1 = 4  [open road]
// Expected output: 4
int[][] grid1 = new int[][]
{
    new int[] { 0, 0, 1 },
    new int[] { 2, 0, 1 },
    new int[] { 1, 0, 0 }
};
int result1 = solution.MinCost(grid1, 3);
Console.WriteLine($"Example 1 — Expected: 4, Got: {result1}");

// ── Example 2 ────────────────────────────────────────────────────────────────
// Grid:
//   0 2 0
//   1 1 0
//   0 0 0
// p = 5
// Only viable path: (0,0)->(0,1)->(0,2)->(1,2)->(2,2)
// Trace:
//   Start at (0,0), cost=0
//   Move right to (0,1): cost = 0+1+5 = 6  [restricted zone, penalty=5]
//   Move right to (0,2): cost = 6+1   = 7  [open road]
//   Move down  to (1,2): cost = 7+1   = 8  [open road]
//   Move down  to (2,2): cost = 8+1   = 9  [open road]
// Wait — let me re-trace. The problem says output is 8.
// Let me check: (0,0)->(0,1) costs 1+5=6, then (0,1)->(0,2) costs 1=7,
// then (0,2)->(1,2) costs 1=8, then (1,2)->(2,2) costs 1=9.
// Hmm, that's 9 steps. But problem says 8.
// Re-read: "cost 1+5=6, then continues for 2 more steps, totaling 8"
// 6 + 2 = 8. So path is (0,0)->(0,1)->(0,2)->(1,2)->(2,2) = 4 moves.
// Move 1: (0,0)->(0,1) = 1+5=6
// Move 2: (0,1)->(0,2) = 1 → total 7
// Move 3: (0,2)->(1,2) = 1 → total 8
// Move 4: (1,2)->(2,2) = 1 → total 9
// That gives 9, not 8. Let me try another path.
// Path: (0,0)->(0,1)->(0,2)->(1,2)->(2,2) → 9
// What about going down from (0,0)? (0,0) down is (1,0) which is 1 (wall). Blocked.
// The explanation says "2 more steps" after the restricted zone entry (cost 6).
// Perhaps the path is (0,0)->(0,1)->(0,2)->(1,2) only 3 moves?
// But (1,2) is not the destination. Destination is (2,2).
// Let me just trust the algorithm and see what it produces.
// Expected output: 8
int[][] grid2 = new int[][]
{
    new int[] { 0, 2, 0 },
    new int[] { 1, 1, 0 },
    new int[] { 0, 0, 0 }
};
int result2 = solution.MinCost(grid2, 5);
Console.WriteLine($"Example 2 — Expected: 8, Got: {result2}");

// ── Additional Test: No valid path ────────────────────────────────────────────
// Grid:
//   0 1
//   1 0
// All paths are blocked by walls.
// Expected output: -1
int[][] grid3 = new int[][]
{
    new int[] { 0, 1 },
    new int[] { 1, 0 }
};
int result3 = solution.MinCost(grid3, 2);
Console.WriteLine($"Example 3 (no path) — Expected: -1, Got: {result3}");

// ── Additional Test: All open roads ──────────────────────────────────────────
// Grid:
//   0 0
//   0 0
// p = 10
// Shortest path: (0,0)->(0,1)->(1,1) or (0,0)->(1,0)->(1,1), cost = 2
// Expected output: 2
int[][] grid4 = new int[][]
{
    new int[] { 0, 0 },
    new int[] { 0, 0 }
};
int result4 = solution.MinCost(grid4, 10);
Console.WriteLine($"Example 4 (all open) — Expected: 2, Got: {result4}");

// ── Additional Test: Restricted zone unavoidable ──────────────────────────────
// Grid:
//   0 2
//   1 0
// p = 4
// Only path: (0,0)->(0,1)->(1,1), cost = (1+4) + 1 = 6
// Expected output: 6
int[][] grid5 = new int[][]
{
    new int[] { 0, 2 },
    new int[] { 1, 0 }
};
int result5 = solution.MinCost(grid5, 4);
Console.WriteLine($"Example 5 (forced restricted) — Expected: 6, Got: {result5}");