```java
/*
 * Title: Tile the Grid with L-Shaped Trominoes
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
 *
 * Constraints:
 * - 1 <= rows, cols <= 6
 * - 0 <= blocked.length <= rows * cols
 * - Each element in blocked is a pair [r, c] where 0 <= r < rows, 0 <= c < cols
 * - No two blocked cells are the same
 */

import java.util.Arrays;
import java.util.List;

/**
 * Solution class for tiling a grid with L-shaped trominoes using backtracking.
 *
 * <p>The approach:
 * 1. Mark blocked cells on the grid.
 * 2. Scan the grid from top-left to bottom-right to find the first uncovered cell.
 * 3. Try all 4 rotations of the L-tromino anchored at or near that cell.
 * 4. Recursively attempt to fill the rest of the grid.
 * 5. Count all valid complete tilings.
 */
public class Solution {

    /** Modulo constant as required by the problem. */
    private static final int MOD = 1_000_000_007;

    /**
     * The grid state:
     * - 0 means the cell is empty (not yet covered).
     * - 1 means the cell is blocked or already covered by a tromino.
     */
    private int[][] grid;

    /** Number of rows in the grid. */
    private int rows;

    /** Number of columns in the grid. */
    private int cols;

    /**
     * The 4 L-tromino rotations, each defined as 3 (row-offset, col-offset) pairs
     * relative to an anchor cell (r, c).
     *
     * Rotation 0: (r,c), (r+1,c), (r+1,c+1)  — bottom-left L
     * Rotation 1: (r,c), (r,c+1), (r+1,c+1)  — bottom-right L
     * Rotation 2: (r,c), (r,c+1), (r+1,c)    — top-right L (mirror)
     * Rotation 3: (r,c+1), (r+1,c), (r+1,c+1)— top-left L (mirror)
     *
     * Each rotation is an array of 3 cells, each cell is {rowOffset, colOffset}.
     */
    private static final int[][][] ROTATIONS = {
        // Rotation 0: top-left corner, extends down and right-down
        {{0, 0}, {1, 0}, {1, 1}},
        // Rotation 1: top-left corner, extends right and right-down
        {{0, 0}, {0, 1}, {1, 1}},
        // Rotation 2: top-left corner, extends right and down
        {{0, 0}, {0, 1}, {1, 0}},
        // Rotation 3: top-right corner, extends down-left and down
        {{0, 1}, {1, 0}, {1, 1}}
    };

    /**
     * Main entry point — demonstrates the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // -------------------------------------------------------
        // Example 1: rows=2, cols=3, no blocked cells → expected 2
        // -------------------------------------------------------
        System.out.println("=== Example 1 ===");
        System.out.println("rows=2, cols=3, blocked=[]");
        int[][] blocked1 = {};
        int result1 = sol.countTilings(2, 3, blocked1);
        System.out.println("Result: " + result1);   // Expected: 2
        System.out.println();

        // -------------------------------------------------------
        // Example 2: rows=2, cols=3, blocked=[[0,2]] → expected 0
        // -------------------------------------------------------
        System.out.println("=== Example 2 ===");
        System.out.println("rows=2, cols=3, blocked=[[0,2]]");
        int[][] blocked2 = {{0, 2}};
        int result2 = sol.countTilings(2, 3, blocked2);
        System.out.println("Result: " + result2);   // Expected: 0
        System.out.println();

        // -------------------------------------------------------
        // Additional test: 3x3 grid, no blocked cells
        // 9 cells → 3 trominoes needed; let's see how many ways
        // -------------------------------------------------------
        System.out.println("=== Extra Test: 3x3, no blocked ===");
        int[][] blocked3 = {};
        int result3 = sol.countTilings(3, 3, blocked3);
        System.out.println("rows=3, cols=3, blocked=[] → Result: " + result3);
        System.out.println();

        // -------------------------------------------------------
        // Additional test: 2x2 grid, no blocked cells
        // 4 cells → cannot be divided into groups of 3 → 0
        // -------------------------------------------------------
        System.out.println("=== Extra Test: 2x2, no blocked ===");
        int[][] blocked4 = {};
        int result4 = sol.countTilings(2, 2, blocked4);
        System.out.println("rows=2, cols=2, blocked=[] → Result: " + result4);  // Expected: 0
        System.out.println();

        // -------------------------------------------------------
        // Additional test: 1x3 grid, no blocked cells
        // 3 cells in a row — no L-tromino fits → 0
        // -------------------------------------------------------
        System.out.println("=== Extra Test: 1x3, no blocked ===");
        int[][] blocked5 = {};
        int result5 = sol.countTilings(1, 3, blocked5);
        System.out.println("rows=1, cols=3, blocked=[] → Result: " + result5);  // Expected: 0
        System.out.println();

        // -------------------------------------------------------
        // Additional test: 2x3 grid with one blocked cell that
        // leaves exactly 5 cells → impossible → 0
        // -------------------------------------------------------
        System.out.println("=== Extra Test: 2x3, blocked=[[1,1]] ===");
        int[][] blocked6 = {{1, 1}};
        int result6 = sol.countTilings(2, 3, blocked6);
        System.out.println("rows=2, cols=3, blocked=[[1,1]] → Result: " + result6);
        System.out.println();

        // -------------------------------------------------------
        // Additional test: 3x4 grid, no blocked cells
        // 12 cells → 4 trominoes
        // -------------------------------------------------------
        System.out.println("=== Extra Test: 3x4, no blocked ===");
        int[][] blocked7 = {};
        int result7 = sol.countTilings(3, 4, blocked7);
        System.out.println("rows=3, cols=4, blocked=[] → Result: " + result7);
        System.out.println();
    }

    /**
     * Counts the number of distinct ways to tile all non-blocked cells of a
     * {@code rows x cols} grid using L-shaped trominoes.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>Quick feasibility check: the number of empty cells must be divisible by 3.</li>
     *   <li>Initialize the grid, marking blocked cells.</li>
     *   <li>Use recursive backtracking to place trominoes one by one.</li>
     *   <li>At each step, find the first empty cell (top-to-bottom, left-to-right)
     *       and try all 4 rotations anchored so that the tromino covers that cell.</li>
     *   <li>Count all complete tilings modulo 10^9+7.</li>
     * </ol>
     *
     * @param rows    number of rows (1 ≤ rows ≤ 6)
     * @param cols    number of columns (1 ≤ cols ≤ 6)
     * @param blocked array of [r, c] pairs representing blocked cells
     * @return total number of distinct valid tilings modulo 10^9+7
     *
     * Time complexity:  O(4^(rows*cols/3)) in the worst case — each recursive call
     *                   tries at most 4 rotations, and there are at most rows*cols/3
     *                   trominoes to place. In practice much faster due to pruning.
     * Space complexity: O(rows * cols) for the grid plus O(rows*cols/3) recursion depth.
     */
    public int countTilings(int rows, int cols, int[][] blocked) {
        this.rows = rows;
        this.cols = cols;

        // Step 1: Count total cells and blocked cells to check divisibility.
        int totalCells = rows * cols;
        int blockedCount = blocked.length;
        int emptyCells = totalCells - blockedCount;

        // If the number of empty cells is not divisible by 3, tiling is impossible.
        if (emptyCells % 3 != 0) {
            return 0;
        }

        // Step 2: Initialize the grid.
        // grid[r][c] = 0 → empty; grid[r][c] = 1 → blocked or covered.
        grid = new int[rows][cols];

        // Mark all blocked cells as occupied (value = 1).
        for (int[] cell : blocked) {
            grid[cell[0]][cell[1]] = 1;
        }

        // Step 3: Start the backtracking search and return the result.
        return backtrack();
    }

    /**
     * Recursive backtracking method that counts all valid tilings of the current
     * grid state.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Find the first empty cell scanning row by row, column by column.</li>
     *   <li>If no empty cell exists, we have a complete tiling — return 1.</li>
     *   <li>Otherwise, try all 4 L-tromino rotations that include the found cell.</li>
     *   <li>For each valid placement, mark the cells, recurse, then unmark (backtrack).</li>
     *   <li>Sum up all successful placements modulo MOD.</li>
     * </ol>
     *
     * @return number of valid complete tilings reachable from the current grid state
     *
     * Time complexity:  Exponential in the number of empty cells divided by 3.
     * Space complexity: O(rows * cols / 3) for the recursion call stack.
     */
    private int backtrack() {
        // Step A: Find the first empty cell (top-left to bottom-right scan).
        int firstRow = -1, firstCol = -1;
        outerLoop:
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 0) {
                    // Found the first empty cell.
                    firstRow = r;
                    firstCol = c;
                    break outerLoop;
                }
            }
        }

        // Step B: If no empty cell was found, all cells are covered — valid tiling!
        if (firstRow == -1) {
            return 1;
        }

        // Step C: Try all 4 rotations of the L-tromino.
        // We need to find placements where the tromino covers (firstRow, firstCol).
        // Each rotation is defined by 3 offsets from an "anchor" point.
        // We iterate over all possible anchor positions such that one of the 3 cells
        // in the rotation lands exactly on (firstRow, firstCol).

        int count = 0;

        // Iterate over each of the 4 rotations.
        for (int rot = 0; rot < 4; rot++) {
            int[][] offsets = ROTATIONS[rot];

            // For each of the 3 cells in this rotation, try making it the "first cell".
            // That means we shift the anchor so that offsets[k] maps to (firstRow, firstCol).
            for (int k = 0; k < 3; k++) {
                // Compute the anchor position such that cell k of the rotation
                // lands on (firstRow, firstCol).
                int anchorRow = firstRow - offsets[k][0];
                int anchorCol = firstCol - offsets[k][1];

                // Compute the actual 3 cells this placement would cover.
                int[] cellRows = new int[3];
                int[] cellCols = new int[3];
                boolean valid = true;

                for (int i = 0; i < 3; i++) {
                    cellRows[i] = anchorRow + offsets[i][0];
                    cellCols[i] = anchorCol + offsets[i][1];

                    // Check bounds.
                    if (cellRows[i] < 0 || cellRows[i] >= rows ||
                        cellCols[i] < 0 || cellCols[i] >= cols) {
                        valid = false;
                        break;
                    }

                    // Check that the cell is empty (not blocked or already covered).
                    if (grid[cellRows[i]][cellCols[i]] != 0) {
                        valid = false;
                        break;
                    }
                }

                // If the placement is invalid, skip it.
                if (!valid) {
                    continue;
                }

                // Important optimization: ensure this placement actually covers
                // (firstRow, firstCol). Since we derived the anchor from cell k,
                // cell k should always be (firstRow, firstCol). But we also need
                // to ensure that none of the other cells in this tromino come
                // BEFORE (firstRow, firstCol) in our scan order (row-major).
                // If any cell of the tromino is "earlier" than firstRow/firstCol,
                // that cell would have been found first — contradiction, since
                // we said (firstRow, firstCol) is the FIRST empty cell.
                // So we must verify all 3 cells are at or after (firstRow, firstCol)
                // in row-major order.
                boolean coversFirst = false;
                boolean allAfterOrEqual = true;
                for (int i = 0; i < 3; i++) {
                    int r = cellRows[i];
                    int c = cellCols[i];
                    // Check if this cell is (firstRow, firstCol)
                    if (r == firstRow && c == firstCol) {
                        coversFirst = true;
                    }
                    // Check if this cell comes before (firstRow, firstCol) in row-major order
                    if (r < firstRow || (r == firstRow && c < firstCol)) {