/*
 * Title: Shortest Path Through Restricted Zones
 *
 * Problem Description:
 * You are given a city map represented as an n x n grid. Each cell in the grid is one of:
 *   - 0 — an open road
 *   - 1 — a wall (impassable)
 *   - 2 — a restricted zone (can be entered, but adds an extra penalty of p to the travel cost)
 *
 * You start at the top-left cell (0, 0) and want to reach the bottom-right cell (n-1, n-1).
 * You can move in 4 directions (up, down, left, right). Each normal move costs 1.
 * Moving into a restricted zone costs 1 + p.
 *
 * Return the minimum total cost to travel from (0,0) to (n-1,n-1).
 * If there is no valid path, return -1.
 *
 * Constraints:
 *   - 2 <= n <= 200
 *   - grid[i][j] is 0, 1, or 2
 *   - 1 <= p <= 100
 *   - grid[0][0] and grid[n-1][n-1] are always 0
 *
 * Example 1:
 *   Input: grid = [[0,0,1],[2,0,1],[1,0,0]], p = 3
 *   Output: 4
 *   Explanation: Path (0,0)->(0,1)->(1,1)->(2,1)->(2,2) costs 4 (all open roads).
 *
 * Example 2:
 *   Input: grid = [[0,2,0],[1,1,0],[0,0,0]], p = 5
 *   Output: 8
 *   Explanation: Only viable path goes through restricted zone at (0,1) costing 1+5=6,
 *                then 2 more steps, totaling 8.
 */

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Solution class for the Shortest Path Through Restricted Zones problem.
 *
 * <p>Approach: We use Dijkstra's algorithm because edge weights are variable
 * (normal cells cost 1, restricted zones cost 1+p). Dijkstra's is ideal for
 * weighted shortest-path problems on non-negative weight graphs.
 *
 * <p>Key insight: The cost to ENTER a cell determines the edge weight:
 *   - Entering a cell with value 0 costs 1
 *   - Entering a cell with value 2 costs 1 + p
 *   - Cells with value 1 are walls and cannot be entered
 */
public class Solution {

    // The four possible movement directions: up, down, left, right
    // Each pair represents [row_delta, col_delta]
    private static final int[][] DIRECTIONS = {
        {-1, 0},  // up
        { 1, 0},  // down
        { 0, -1}, // left
        { 0,  1}  // right
    };

    /**
     * Finds the minimum cost path from (0,0) to (n-1,n-1) in a grid with walls
     * and restricted zones using Dijkstra's shortest path algorithm.
     *
     * @param grid The n x n grid where 0=open road, 1=wall, 2=restricted zone
     * @param p    The extra penalty cost for entering a restricted zone (cell value 2)
     * @return The minimum total travel cost, or -1 if no valid path exists
     *
     * Time Complexity:  O(n^2 * log(n^2)) = O(n^2 * log n) — each of the n^2 cells
     *                   is processed at most once, and each priority queue operation
     *                   costs O(log(n^2)) = O(2 log n) = O(log n).
     * Space Complexity: O(n^2) — for the distance array and priority queue storage.
     */
    public int minimumCost(int[][] grid, int p) {
        // Step 1: Determine the grid size
        int n = grid.length;

        // Step 2: Initialize a distance array with "infinity" for all cells.
        // dist[i][j] represents the minimum cost found so far to reach cell (i, j).
        // We use Integer.MAX_VALUE as a stand-in for infinity (unreachable).
        int[][] dist = new int[n][n];
        for (int[] row : dist) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }

        // Step 3: The starting cell (0,0) has cost 0 — we're already there.
        dist[0][0] = 0;

        // Step 4: Set up a min-heap (priority queue) ordered by cost.
        // Each entry in the queue is an int array: [cost, row, col]
        // The queue always gives us the cell with the smallest known cost first.
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);

        // Step 5: Add the starting cell to the priority queue with cost 0.
        pq.offer(new int[]{0, 0, 0}); // [cost=0, row=0, col=0]

        // Step 6: Main Dijkstra loop — process cells in order of increasing cost.
        while (!pq.isEmpty()) {

            // Step 6a: Extract the cell with the minimum current cost.
            int[] current = pq.poll();
            int currentCost = current[0];
            int row         = current[1];
            int col         = current[2];

            // Step 6b: If we've reached the destination, return the cost immediately.
            // Since Dijkstra processes nodes in non-decreasing cost order, the first
            // time we reach (n-1, n-1) is guaranteed to be the minimum cost.
            if (row == n - 1 && col == n - 1) {
                return currentCost;
            }

            // Step 6c: Skip this entry if we've already found a cheaper path to this cell.
            // This handles the case where a cell was added to the queue multiple times
            // with different costs (we only care about the cheapest one).
            if (currentCost > dist[row][col]) {
                continue; // This is a stale entry; skip it
            }

            // Step 6d: Explore all 4 neighboring cells.
            for (int[] direction : DIRECTIONS) {
                int newRow = row + direction[0]; // Calculate neighbor's row
                int newCol = col + direction[1]; // Calculate neighbor's column

                // Step 6e: Check bounds — skip if the neighbor is outside the grid.
                if (newRow < 0 || newRow >= n || newCol < 0 || newCol >= n) {
                    continue;
                }

                // Step 6f: Check if the neighbor is a wall — walls are impassable.
                if (grid[newRow][newCol] == 1) {
                    continue;
                }

                // Step 6g: Calculate the cost to move into the neighbor cell.
                // - Open road (0): costs 1
                // - Restricted zone (2): costs 1 + p
                int moveCost;
                if (grid[newRow][newCol] == 2) {
                    moveCost = 1 + p; // Extra penalty for restricted zone
                } else {
                    moveCost = 1;     // Normal movement cost
                }

                // Step 6h: Calculate the total cost to reach the neighbor via this path.
                int newCost = currentCost + moveCost;

                // Step 6i: If this new cost is cheaper than the previously known cost
                // for the neighbor, update it and add the neighbor to the queue.
                if (newCost < dist[newRow][newCol]) {
                    dist[newRow][newCol] = newCost; // Update best known cost
                    pq.offer(new int[]{newCost, newRow, newCol}); // Enqueue for exploration
                }
            }
        }

        // Step 7: If we exhaust the queue without reaching (n-1, n-1), no path exists.
        // Return -1 to indicate this.
        return -1;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // Grid:
        //   [0, 0, 1]
        //   [2, 0, 1]
        //   [1, 0, 0]
        // p = 3
        //
        // Optimal path: (0,0) -> (0,1) -> (1,1) -> (2,1) -> (2,2)
        // All cells on this path are open roads (value 0), so cost = 4 * 1 = 4
        // Expected output: 4
        // -----------------------------------------------------------------------
        int[][] grid1 = {
            {0, 0, 1},
            {2, 0, 1},
            {1, 0, 0}
        };
        int p1 = 3;
        int result1 = solution.minimumCost(grid1, p1);
        System.out.println("Example 1:");
        System.out.println("Grid: [[0,0,1],[2,0,1],[1,0,0]], p = " + p1);
        System.out.println("Expected: 4");
        System.out.println("Got:      " + result1);
        System.out.println("Correct:  " + (result1 == 4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // Grid:
        //   [0, 2, 0]
        //   [1, 1, 0]
        //   [0, 0, 0]
        // p = 5
        //
        // The only viable path must go through (0,1) which is a restricted zone.
        // Path: (0,0) -> (0,1) -> (0,2) -> (1,2) -> (2,2)
        // Cost: 0 + (1+5) + 1 + 1 + 1 = 0 + 6 + 1 + 1 + 1 = ... wait, let me retrace.
        //
        // Starting at (0,0) with cost 0.
        // Move to (0,1) [restricted, value=2]: cost = 0 + (1+5) = 6
        // Move to (0,2) [open, value=0]:       cost = 6 + 1 = 7
        // Move to (1,2) [open, value=0]:       cost = 7 + 1 = 8
        // Move to (2,2) [open, value=0]:       cost = 8 + 1 = 9... hmm
        //
        // Wait, let me re-read: path is (0,0)->(0,1)->(0,2)->(1,2)->(2,2)
        // That's 4 moves. Cost = (1+5) + 1 + 1 + 1 = 9? But expected is 8.
        //
        // Let me re-check: maybe the path is (0,0)->(0,1)->(0,2)->(1,2)->(2,2)
        // = 4 steps. Or maybe (0,0)->(0,1) then down? (1,1) is a wall.
        //
        // Actually: (0,0) -> (0,1) [cost 6] -> (0,2) [cost 7] -> (1,2) [cost 8] -> (2,2) [cost 9]
        // That's 9, not 8. Let me re-examine the problem statement.
        //
        // The problem says output is 8 with "cost 1+5=6, then 2 more steps, totaling 8".
        // 6 + 2 = 8. So the path has only 3 moves total? 
        // (0,0) -> (0,1) [cost 6] -> ... -> (n-1,n-1) in 2 more steps?
        // From (0,1) to (2,2) in 2 steps is impossible (Manhattan distance = 3).
        //
        // Hmm, let me re-read. "cost 1+5=6, then 2 more steps" — maybe the explanation
        // means 2 more moves after the restricted zone, but the path is longer.
        // Actually 6 + 1 + 1 = 8 means only 2 more moves. But (0,1) to (2,2) needs 3 moves.
        //
        // Let me reconsider: maybe the grid indexing or path is different.
        // Grid: row 0: [0,2,0], row 1: [1,1,0], row 2: [0,0,0]
        // (0,0)->(0,1)[cost 1+5=6]->(0,2)[cost 7]->(1,2)[cost 8]->(2,2)[cost 9]
        // That's 9. But expected is 8.
        //
        // OR maybe the problem counts the starting cell cost too? No, grid[0][0]=0.
        //
        // Let me try another path: is there a 3-step path?
        // (0,0) to (2,2): minimum Manhattan distance = 4 steps.
        // So minimum 4 moves, minimum cost 4 (if all open). With one restricted zone = 4+5=9? 
        // No wait: cost of entering restricted = 1+p, not extra p on top of 1.
        // So 4 moves, one restricted: 3*1 + (1+5) = 3 + 6 = 9.
        //
        // Hmm, but expected is 8. Let me re-examine.
        // Maybe the problem means: moving into restricted zone costs p extra (total = 1+p),
        // but the base cost of 1 is already included? So 4 moves = 4 base + 5 penalty = 9?
        // Or maybe it's: 4 moves = 3*1 + 1*(1+5) = 9.
        //
        // I'm getting 9 for example 2. Let me re-read the problem statement carefully.
        // "Each normal move costs 1. Moving into a restricted zone costs 1 + p."
        // So entering restricted zone: total cost for that move = 1+p = 6.
        // Path (0,0)->(0,1)->(0,2)->(1,2)->(2,2): costs 6+1+1+1=9.
        //
        // But expected is 8. There must be a shorter path I'm missing.
        // Wait: (0,0)->(0,1)->(0,2)->(1,2)->(2,2) = 4 moves.
        // Is there a 3-move path? No, Manhattan distance from (0,0) to (2,2) is 4.
        //
        // Unless... the path doesn't need to be the shortest in steps?
        // Or maybe I'm misreading the grid. Let me re-examine example 2.
        // grid = [[0,2,0],[1,1,0],[0,0,0]]
        // (0,0)=0, (0,1)=2, (0,2)=0
        // (1,0)=1, (1,1)=1, (1,2)=0
        // (2,0)=0, (2,1)=0, (2,2)=0
        //
        // Paths from (0,0) to (2,2):
        // Must avoid (1,0) and (1,1) walls.
        // Only way through: go right to (0,1)[restricted], right to (0,2), down to (1,2), down to (2,2).
        // Cost: (1+5)+1+1+