```csharp
/*
 * Title: Tile a Board with Dominoes and Trominoes
 *
 * Problem Description:
 * You are given an m x n grid board. You have an unlimited supply of two types of tiles:
 *   - Domino: covers exactly 2 adjacent cells (placed horizontally or vertically)
 *   - Tromino (L-shape): covers exactly 3 cells in an L-shaped pattern (any of the 4 rotations)
 *
 * Your task is to find ALL distinct ways to completely tile the board such that every cell
 * is covered by exactly one tile. Two tilings are considered different if at least one tile
 * occupies a different set of cells.
 *
 * Return the COUNT of all valid tilings.
 *
 * Constraints:
 *   - 1 <= m <= 4
 *   - 1 <= n <= 4
 *
 * Examples:
 *   m=2, n=2 => 2 tilings
 *   m=2, n=3 => 3 tilings
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class encapsulating the backtracking algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    // -------------------------------------------------------------------------
    // CountTilings(m, n)
    //
    // Time Complexity:  O(T * P) where T is the number of valid tilings and P is
    //                   the number of cells (m*n). In practice, for m,n <= 4 this
    //                   is extremely fast.
    // Space Complexity: O(m * n) for the board grid plus O(m*n) recursion depth.
    //
    // High-level approach:
    //   We maintain a 2D integer grid (board) where 0 means "empty" and a positive
    //   integer means "occupied by tile #k".
    //
    //   At each recursive step we:
    //     1. Find the FIRST empty cell (scanning left-to-right, top-to-bottom).
    //     2. Try placing every possible tile shape that COVERS that cell.
    //     3. Recurse. If the board is fully covered, count it as a valid tiling.
    //     4. Undo the placement (backtrack) and try the next shape.
    //
    //   Why "first empty cell"?
    //     Fixing the first empty cell as the "anchor" of the next tile guarantees
    //     we never generate the same tiling twice in a different order. Every tile
    //     placement is uniquely determined by which cells it covers, and we always
    //     attach the new tile to the earliest uncovered cell.
    // -------------------------------------------------------------------------
    public int CountTilings(int m, int n)
    {
        // Create the board: board[r][c] == 0 means the cell is empty.
        int[,] board = new int[m, n];

        // tileCount tracks how many tiles we have placed so far.
        // Each new tile gets label (tileCount + 1).
        int tileCount = 0;

        // solutionCount accumulates the number of complete tilings found.
        int solutionCount = 0;

        // ── Define all tile shapes ────────────────────────────────────────────
        //
        // Each shape is a list of (rowOffset, colOffset) pairs relative to the
        // ANCHOR cell (the first empty cell we find). The anchor itself is always
        // included as (0, 0).
        //
        // Domino shapes (2 cells):
        //   H: (0,0),(0,1)   – horizontal
        //   V: (0,0),(1,0)   – vertical
        //
        // Tromino L-shapes (3 cells) – all 4 rotations:
        //   We enumerate every L-tromino that INCLUDES the anchor cell.
        //   An L-tromino is 3 cells forming an L. There are many ways to pick
        //   3 cells in an L from the anchor, but we only need shapes where the
        //   anchor is the TOP-LEFT-MOST cell (to avoid duplicates). Actually,
        //   since we always anchor to the FIRST empty cell, the anchor is
        //   guaranteed to be the top-left-most cell of the tile — so we must
        //   include ALL rotations where the anchor is the minimum cell.
        //
        //   Let's enumerate carefully. An L-tromino covers 3 of the 4 cells of
        //   a 2×2 square. Given anchor (0,0), the 4 possible L-trominoes that
        //   include (0,0) are:
        //
        //   Shape A (missing bottom-right):
        //     (0,0),(0,1),(1,0)
        //
        //   Shape B (missing bottom-left):
        //     (0,0),(0,1),(1,1)
        //
        //   Shape C (missing top-right):
        //     (0,0),(1,0),(1,1)
        //
        //   Shape D (missing top-left — anchor is top-left but the L goes right+down):
        //     (0,0),(1,0),(1,-1)  ← this has a negative column offset!
        //     Wait — if anchor is the FIRST empty cell (leftmost in the row),
        //     col-1 might be occupied already, so this shape would be invalid
        //     (the cell to the left is already filled). We still try it; the
        //     bounds/occupancy check will reject it naturally.
        //
        //   Actually let's think more carefully. The anchor is the first EMPTY
        //   cell. Cells to its left (same row) are already filled. So any shape
        //   that requires a cell to the LEFT of the anchor will fail the
        //   "cell is empty" check and be skipped. We can safely include such
        //   shapes in our list — they'll just never succeed.
        //
        //   Full list of L-tromino offsets relative to anchor (0,0):
        //     We want every set of 3 cells {(0,0), A, B} that form an L.
        //     An L means the 3 cells fit in a 2×2 bounding box (i.e., they are
        //     3 of the 4 corners of some 2×2 square).
        //
        //   All 2×2 squares that contain (0,0) as one of their 4 corners:
        //     Square with top-left at (0,0):  corners (0,0),(0,1),(1,0),(1,1)
        //     Square with top-right at (0,0): corners (0,-1),(0,0),(1,-1),(1,0)
        //     Square with bot-left at (0,0):  corners (-1,0),(-1,1),(0,0),(0,1)
        //     Square with bot-right at (0,0): corners (-1,-1),(-1,0),(0,-1),(0,0)
        //
        //   For each square, we pick 3 of the 4 corners (the ones that include (0,0)):
        //
        //   Square TL=(0,0): missing (0,1) → {(0,0),(1,0),(1,1)}
        //                    missing (1,0) → {(0,0),(0,1),(1,1)}
        //                    missing (1,1) → {(0,0),(0,1),(1,0)}
        //                    missing (0,0) → doesn't include anchor, skip
        //
        //   Square TR=(0,0): missing (0,-1)→ {(0,0),(1,-1),(1,0)}
        //                    missing (1,-1)→ {(0,0),(0,-1),(1,0)}
        //                    missing (1,0) → {(0,0),(0,-1),(1,-1)}
        //                    missing (0,0) → skip
        //
        //   Square BL=(0,0): missing (-1,0)→ {(0,0),(-1,1),(0,1)}
        //                    missing (-1,1)→ {(0,0),(-1,0),(0,1)}
        //                    missing (0,1) → {(0,0),(-1,0),(-1,1)}
        //                    missing (0,0) → skip
        //
        //   Square BR=(0,0): missing (-1,-1)→{(0,0),(-1,0),(0,-1)}
        //                    missing (-1,0) →{(0,0),(-1,-1),(0,-1)}
        //                    missing (0,-1) →{(0,0),(-1,-1),(-1,0)}
        //                    missing (0,0) → skip
        //
        //   That gives 12 L-tromino shapes. Many will be rejected by bounds/
        //   occupancy checks (negative offsets, out-of-bounds, already filled).
        //   We include them all for completeness and let the checks filter them.

        // ── Build the shapes list ─────────────────────────────────────────────
        var shapes = new List<(int dr, int dc)[]>();

        // --- Domino shapes ---
        // Horizontal domino
        shapes.Add(new[] { (0, 0), (0, 1) });
        // Vertical domino
        shapes.Add(new[] { (0, 0), (1, 0) });

        // --- L-Tromino shapes (all 12 combinations from the analysis above) ---
        // From square with top-left at anchor (0,0):
        shapes.Add(new[] { (0, 0), (1, 0), (1, 1) });   // missing top-right
        shapes.Add(new[] { (0, 0), (0, 1), (1, 1) });   // missing bottom-left
        shapes.Add(new[] { (0, 0), (0, 1), (1, 0) });   // missing bottom-right

        // From square with top-right at anchor (0,0):
        shapes.Add(new[] { (0, 0), (1, -1), (1, 0) });  // missing top-left of that square
        shapes.Add(new[] { (0, 0), (0, -1), (1, 0) });  // missing bottom-left of that square
        shapes.Add(new[] { (0, 0), (0, -1), (1, -1) }); // missing top-right of that square (anchor)

        // From square with bottom-left at anchor (0,0):
        shapes.Add(new[] { (0, 0), (-1, 1), (0, 1) });  // missing top-left of that square
        shapes.Add(new[] { (0, 0), (-1, 0), (0, 1) });  // missing top-right of that square
        shapes.Add(new[] { (0, 0), (-1, 0), (-1, 1) }); // missing bottom-right of that square (anchor)

        // From square with bottom-right at anchor (0,0):
        shapes.Add(new[] { (0, 0), (-1, 0), (0, -1) }); // missing top-left of that square
        shapes.Add(new[] { (0, 0), (-1, -1), (0, -1) });// missing top-right of that square
        shapes.Add(new[] { (0, 0), (-1, -1), (-1, 0) });// missing bottom-right of that square (anchor)

        // ── Backtracking helper (local function) ──────────────────────────────
        void Backtrack()
        {
            // Step 1: Find the first empty cell (scan row by row, left to right).
            // This is our "anchor" — the cell that MUST be covered by the next tile.
            int anchorRow = -1, anchorCol = -1;
            bool found = false;
            for (int r = 0; r < m && !found; r++)
            {
                for (int c = 0; c < n && !found; c++)
                {
                    if (board[r, c] == 0)
                    {
                        anchorRow = r;
                        anchorCol = c;
                        found = true;
                    }
                }
            }

            // Step 2: If no empty cell was found, the board is completely tiled!
            // Count this as a valid solution.
            if (!found)
            {
                solutionCount++;
                return;
            }

            // Step 3: Try every tile shape that covers the anchor cell.
            // The anchor is always the first cell (offset 0,0) in each shape.
            tileCount++;           // assign the next tile label
            int label = tileCount; // this tile's label

            foreach (var shape in shapes)
            {
                // Step 3a: Check if this shape can be placed with its anchor at
                //          (anchorRow, anchorCol). All cells in the shape must be:
                //            - Within the board bounds
                //            - Currently empty (== 0)
                bool canPlace = true;
                foreach (var (dr, dc) in shape)
                {
                    int r = anchorRow + dr;
                    int c = anchorCol + dc;
                    // Bounds check
                    if (r < 0 || r >= m || c < 0 || c >= n)
                    {
                        canPlace = false;
                        break;
                    }
                    // Occupancy check
                    if (board[r, c] != 0)
                    {
                        canPlace = false;
                        break;
                    }
                }

                if (!canPlace)
                    continue; // This shape doesn't fit here; try the next one.

                // Step 3b: Place the tile — mark all its cells with the label.
                foreach (var (dr, dc) in shape)
                    board[anchorRow + dr, anchorCol + dc] = label;

                // Step 3c: Recurse to fill the rest of the board.
                Backtrack();

                // Step 3d: Undo the placement (backtrack) so we can try the next shape.
                foreach (var (dr, dc) in shape)
                    board[anchorRow + dr, anchorCol + dc] = 0;
            }

            // Restore tileCount so the label can be reused by sibling branches.
            tileCount--;
        }

        // ── Kick off the backtracking ─────────────────────────────────────────
        Backtrack();

        return solutionCount;
    }

    // -------------------------------------------------------------------------
    // Helper: Print a tiling board nicely (for debugging / demo purposes)
    // -------------------------------------------------------------------------
    public void PrintBoard(int[,] board, int m, int n)
    {
        for (int r = 0; r < m; r++)
        {
            for (int c = 0; c < n; c++)
            {
                Console.Write($"{board[r, c],3}");
            }
            Console.WriteLine();
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var sol = new Solution();

Console.WriteLine("=== Tile a Board with Dominoes and Trominoes ===");
Console.WriteLine();

// ── Example 1: m=2, n=2 ──────────────────────────────────────────────────────
// Expected: 2 tilings
//   Tiling 1: Two horizontal dominoes stacked vertically
//     1 1
//     2 2
//   Tiling 2: Two vertical dominoes side by side
//     1 2
//     1 2
int result1 = sol.CountTilings(2, 2);
Console.WriteLine($"m=2, n=2 => {result1} tilings  (expected: 2)");
Console.WriteLine();

// ── Example 2: m=2, n=3 ──────────────────────────────