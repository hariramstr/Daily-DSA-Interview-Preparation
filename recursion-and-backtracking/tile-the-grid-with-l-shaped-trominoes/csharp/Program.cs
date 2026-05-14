/*
 * Title: Tile the Grid with L-Shaped Trominoes
 * Difficulty: Medium
 * Topic: Recursion and Backtracking
 *
 * Problem Description:
 * You are given a grid of size rows x cols and a list of blocked cells that cannot be covered.
 * Your task is to determine all distinct ways to tile the remaining empty cells using L-shaped
 * trominoes (each tromino covers exactly 3 cells in an L-shape). A valid tiling must cover every
 * non-blocked cell exactly once, with no tromino extending outside the grid boundaries or
 * overlapping a blocked cell.
 *
 * An L-shaped tromino can be placed in 4 rotations:
 * - Rotation 0: covers (r,c), (r+1,c), (r+1,c+1)
 * - Rotation 1: covers (r,c), (r,c+1), (r+1,c+1)
 * - Rotation 2: covers (r,c), (r,c+1), (r+1,c)
 * - Rotation 3: covers (r,c+1), (r+1,c), (r+1,c+1)
 *
 * Return the total number of distinct valid tilings modulo 10^9 + 7.
 * If it is impossible to tile all non-blocked cells, return 0.
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class encapsulating the backtracking algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // -------------------------------------------------------------------------
    // Time Complexity:  O(4^(rows*cols / 3)) in the worst case — at each step we
    //                   try up to 4 tromino placements anchored at the first free
    //                   cell, and the recursion depth is at most (rows*cols)/3.
    //                   In practice the grid is tiny (≤6×6=36 cells) so this is
    //                   extremely fast.
    // Space Complexity: O(rows * cols) for the grid state array plus O(rows*cols/3)
    //                   recursion stack depth.
    // -------------------------------------------------------------------------

    // Modulus constant as required by the problem
    private const int MOD = 1_000_000_007;

    // The four L-tromino shapes, each expressed as three (rowOffset, colOffset)
    // pairs relative to an "anchor" cell (r, c).
    //
    // Rotation 0:  ##    cells: (r,c)  (r+1,c)  (r+1,c+1)
    //               ##
    //
    // Rotation 1:  ##    cells: (r,c)  (r,c+1)  (r+1,c+1)
    //               #
    //
    // Rotation 2:  ##    cells: (r,c)  (r,c+1)  (r+1,c)
    //              #
    //
    // Rotation 3:   #    cells: (r,c+1) (r+1,c)  (r+1,c+1)
    //              ##
    //
    // Each entry is { (dr0,dc0), (dr1,dc1), (dr2,dc2) }
    private static readonly int[,,] Shapes = new int[4, 3, 2]
    {
        // Rotation 0: (r,c), (r+1,c), (r+1,c+1)
        { { 0, 0 }, { 1, 0 }, { 1, 1 } },
        // Rotation 1: (r,c), (r,c+1), (r+1,c+1)
        { { 0, 0 }, { 0, 1 }, { 1, 1 } },
        // Rotation 2: (r,c), (r,c+1), (r+1,c)
        { { 0, 0 }, { 0, 1 }, { 1, 0 } },
        // Rotation 3: (r,c+1), (r+1,c), (r+1,c+1)
        { { 0, 1 }, { 1, 0 }, { 1, 1 } }
    };

    /// <summary>
    /// Main entry point: counts all distinct valid tilings of the grid.
    /// </summary>
    public int CountTilings(int rows, int cols, int[][] blocked)
    {
        // ── Step 1: Build the grid ────────────────────────────────────────────
        // We represent the grid as a 2-D boolean array.
        //   true  = cell is FREE (can be covered by a tromino)
        //   false = cell is OCCUPIED (either blocked or already covered)
        //
        // Why a boolean grid? It gives O(1) read/write per cell and is easy to
        // undo during backtracking.
        bool[,] grid = new bool[rows, cols];

        // Mark every cell as free initially
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r, c] = true;

        // Mark blocked cells as occupied so we never try to cover them
        foreach (int[] cell in blocked)
            grid[cell[0], cell[1]] = false;

        // ── Step 2: Quick feasibility check ──────────────────────────────────
        // Count the number of free cells. Each tromino covers exactly 3 cells,
        // so the number of free cells must be divisible by 3. If not, no valid
        // tiling exists and we can return 0 immediately without any recursion.
        int freeCells = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (grid[r, c]) freeCells++;

        if (freeCells % 3 != 0)
            return 0; // Impossible — early exit

        // ── Step 3: Start backtracking ────────────────────────────────────────
        // We call the recursive helper which will explore all valid placements
        // and accumulate the count of complete tilings.
        return Backtrack(grid, rows, cols);
    }

    /// <summary>
    /// Recursive backtracking function.
    ///
    /// Strategy — "first free cell" anchor:
    ///   At each recursive call we scan the grid left-to-right, top-to-bottom
    ///   and find the FIRST free cell. We then try to place each of the 4
    ///   tromino rotations that INCLUDE that cell. Because we always pick the
    ///   topmost-leftmost free cell, every valid tiling will be discovered
    ///   exactly once (no duplicates), and we never miss any tiling.
    ///
    /// Why does this avoid duplicates?
    ///   The first free cell MUST be covered by some tromino. By anchoring on
    ///   that specific cell we fix which tromino covers it, so the same tiling
    ///   cannot be reached via two different orderings.
    /// </summary>
    private int Backtrack(bool[,] grid, int rows, int cols)
    {
        // ── Step A: Find the first free cell ─────────────────────────────────
        // Scan row by row, column by column. The first cell that is still free
        // becomes our "anchor" — we MUST place a tromino covering it right now.
        int anchorRow = -1, anchorCol = -1;

        outerLoop:
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (grid[r, c])
                {
                    anchorRow = r;
                    anchorCol = c;
                    goto outerLoop_done; // break out of both loops
                }
            }
        }
        outerLoop_done:;

        // ── Step B: Base case — no free cell found ────────────────────────────
        // If anchorRow is still -1, every cell has been covered (or was blocked).
        // This means we have found one complete valid tiling — return 1.
        if (anchorRow == -1)
            return 1;

        // ── Step C: Try all 4 tromino rotations ──────────────────────────────
        // For each rotation we check whether all 3 cells of that shape:
        //   (a) lie within the grid boundaries, AND
        //   (b) are currently free.
        // If both conditions hold, we place the tromino (mark cells occupied),
        // recurse, then undo the placement (backtrack).
        int totalCount = 0;

        for (int rotation = 0; rotation < 4; rotation++)
        {
            // Compute the absolute grid coordinates of the 3 cells for this
            // rotation, using the anchor (anchorRow, anchorCol) as the base.
            int r0 = anchorRow + Shapes[rotation, 0, 0];
            int c0 = anchorCol + Shapes[rotation, 0, 1];

            int r1 = anchorRow + Shapes[rotation, 1, 0];
            int c1 = anchorCol + Shapes[rotation, 1, 1];

            int r2 = anchorRow + Shapes[rotation, 2, 0];
            int c2 = anchorCol + Shapes[rotation, 2, 1];

            // ── Step C1: Boundary check ───────────────────────────────────────
            // All three cells must be inside the grid. If any cell is out of
            // bounds, this rotation is invalid — skip it.
            if (!InBounds(r0, c0, rows, cols) ||
                !InBounds(r1, c1, rows, cols) ||
                !InBounds(r2, c2, rows, cols))
                continue;

            // ── Step C2: Availability check ───────────────────────────────────
            // All three cells must currently be free (not blocked, not already
            // covered by a previously placed tromino).
            if (!grid[r0, c0] || !grid[r1, c1] || !grid[r2, c2])
                continue;

            // ── Step C3: Place the tromino (mark cells as occupied) ───────────
            // We set the three cells to false so future recursive calls know
            // they are no longer available.
            grid[r0, c0] = false;
            grid[r1, c1] = false;
            grid[r2, c2] = false;

            // ── Step C4: Recurse ──────────────────────────────────────────────
            // With these three cells covered, continue placing trominoes for
            // the remaining free cells. Add the result to our running total,
            // taking modulo to prevent integer overflow.
            totalCount = (totalCount + Backtrack(grid, rows, cols)) % MOD;

            // ── Step C5: Undo the placement (backtrack) ───────────────────────
            // Restore the three cells to free so we can try the next rotation.
            // This is the "undo" step that makes backtracking work — we explore
            // one branch fully, then revert and explore the next branch.
            grid[r0, c0] = true;
            grid[r1, c1] = true;
            grid[r2, c2] = true;
        }

        // Return the total number of valid tilings found from this state
        return totalCount;
    }

    /// <summary>
    /// Helper: returns true if (r, c) is within the grid boundaries.
    /// Extracted into its own method for clarity and reuse.
    /// </summary>
    private static bool InBounds(int r, int c, int rows, int cols)
        => r >= 0 && r < rows && c >= 0 && c < cols;
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / test code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

Console.WriteLine("=== Tile the Grid with L-Shaped Trominoes ===");
Console.WriteLine();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Input : rows=2, cols=3, blocked=[]
// Expected output: 2
// Explanation: The 2×3 grid has 6 free cells = 2 trominoes.
//   Tiling A:  [##]   Tiling B:  [##]
//              [##]              [##]
//   (Two distinct L-tromino arrangements cover the 2×3 grid.)
int result1 = solution.CountTilings(2, 3, Array.Empty<int[]>());
Console.WriteLine($"Example 1: rows=2, cols=3, blocked=[]");
Console.WriteLine($"  Expected: 2");
Console.WriteLine($"  Got:      {result1}");
Console.WriteLine($"  Pass:     {result1 == 2}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Input : rows=2, cols=3, blocked=[[0,2]]
// Expected output: 0
// Explanation: 6 - 1 = 5 free cells; 5 % 3 != 0, so impossible.
int result2 = solution.CountTilings(2, 3, new int[][] { new int[] { 0, 2 } });
Console.WriteLine($"Example 2: rows=2, cols=3, blocked=[[0,2]]");
Console.WriteLine($"  Expected: 0");
Console.WriteLine($"  Got:      {result2}");
Console.WriteLine($"  Pass:     {result2 == 0}");
Console.WriteLine();

// ── Additional test: 3×3 grid with center blocked ────────────────────────────
// 9 - 1 = 8 free cells; 8 % 3 != 0 → expected 0
int result3 = solution.CountTilings(3, 3, new int[][] { new int[] { 1, 1 } });
Console.WriteLine($"Extra 1: rows=3, cols=3, blocked=[[1,1]]");
Console.WriteLine($"  Free cells = 8, 8 % 3 != 0 → Expected: 0");
Console.WriteLine($"  Got:      {result3}");
Console.WriteLine($"  Pass:     {result3 == 0}");
Console.WriteLine();

// ── Additional test: 3×3 grid, no blocked cells ──────────────────────────────
// 9 free cells = 3 trominoes; let's see how many tilings exist
int result4 = solution.CountTilings(3, 3, Array.Empty<int[]>());
Console.WriteLine($"Extra 2: rows=3, cols=3, blocked=[]");
Console.WriteLine($"  Free cells = 9 = 3 trominoes");
Console.WriteLine($"  Got:      {result4}  (computed by backtracking)");
Console.WriteLine();

// ── Additional test: 1×3 grid, no blocked cells ──────────────────────────────
// 3 free cells = 1 tromino, but no L-shape fits in a 1-row grid → expected 0
int result5 = solution.CountTilings(1, 3, Array.Empty<int[]>());
Console.WriteLine($"Extra 3: rows=1, cols=3, blocked=[]");
Console.WriteLine($"  A 1×3 strip cannot fit any L-tromino → Expected: 0");
Console.WriteLine($"  Got:      {result5}");
Console.WriteLine($"  Pass:     {result5 == 0}");
Console.WriteLine();

// ── Additional test: 2×6 grid, no blocked cells ──────────────────────────────
// 12 free cells =