```java
/*
 * Title: Tile a Board with Dominoes and Trominoes
 *
 * Problem Description:
 * You are given an m x n grid board. You have an unlimited supply of two types of tiles:
 * - Domino: covers exactly 2 adjacent cells (placed horizontally or vertically)
 * - Tromino (L-shape): covers exactly 3 cells in an L-shaped pattern (any of the 4 rotations)
 *
 * Your task is to find all distinct ways to completely tile the board such that every cell
 * is covered by exactly one tile. Two tilings are considered different if at least one tile
 * occupies a different set of cells.
 *
 * Return the list of all valid tilings, where each tiling is represented as a 2D grid of integers.
 * Cells covered by the same tile share the same positive integer label, and labels are assigned
 * in the order the tiles are placed (left-to-right, top-to-bottom by the top-left cell of each tile).
 *
 * Constraints:
 * - 1 <= m <= 4
 * - 1 <= n <= 4
 * - The board dimensions are small enough to enumerate all solutions via backtracking.
 *
 * Example 1:
 * Input: m = 2, n = 2
 * Output: 2 (two distinct tilings exist)
 *
 * Example 2:
 * Input: m = 2, n = 3
 * Output: 3 (three distinct tilings exist)
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Solution class for tiling a board with dominoes and trominoes using backtracking.
 *
 * <p>The approach:
 * 1. Scan the board left-to-right, top-to-bottom to find the first uncovered cell.
 * 2. Try placing each possible tile (domino horizontal, domino vertical, 4 tromino rotations)
 *    that covers that cell.
 * 3. Recursively fill the rest of the board.
 * 4. If the board is fully covered, record the tiling.
 * 5. Backtrack by removing the tile and trying the next option.
 */
public class Solution {

    /** The number of rows in the board. */
    private int m;

    /** The number of columns in the board. */
    private int n;

    /** The current state of the board (0 = empty, positive integer = tile label). */
    private int[][] board;

    /** Counter for assigning unique labels to tiles. */
    private int tileLabel;

    /** List to store all valid tilings found. */
    private List<int[][]> results;

    /**
     * Finds all distinct ways to tile an m x n board with dominoes and trominoes.
     *
     * <p>A domino covers 2 adjacent cells (horizontal or vertical).
     * A tromino covers 3 cells in an L-shape (4 possible rotations).
     *
     * @param m the number of rows (1 <= m <= 4)
     * @param n the number of columns (1 <= n <= 4)
     * @return the count of all valid tilings
     *
     * Time complexity: O(k^(m*n)) where k is the number of tile shapes (bounded by small constants
     *                  since m,n <= 4, this is effectively O(1) for the given constraints)
     * Space complexity: O(m*n) for the board and recursion stack depth
     */
    public int countTilings(int m, int n) {
        // Initialize instance variables
        this.m = m;
        this.n = n;
        this.board = new int[m][n];
        this.tileLabel = 1;
        this.results = new ArrayList<>();

        // Start the backtracking process from the beginning
        backtrack();

        // Return the total number of valid tilings found
        return results.size();
    }

    /**
     * Finds all distinct tilings and returns them as a list of 2D grids.
     *
     * @param m the number of rows (1 <= m <= 4)
     * @param n the number of columns (1 <= n <= 4)
     * @return list of all valid tilings, each represented as a 2D integer array
     *
     * Time complexity: O(k^(m*n)) where k is the number of tile shapes
     * Space complexity: O(m*n * T) where T is the number of valid tilings
     */
    public List<int[][]> findAllTilings(int m, int n) {
        // Initialize instance variables
        this.m = m;
        this.n = n;
        this.board = new int[m][n];
        this.tileLabel = 1;
        this.results = new ArrayList<>();

        // Start the backtracking process
        backtrack();

        // Return all found tilings
        return results;
    }

    /**
     * Core backtracking method that tries to fill the board completely.
     *
     * <p>Strategy:
     * - Find the first empty cell (scanning left-to-right, top-to-bottom).
     * - Try all possible tiles that can cover that cell.
     * - Recurse to fill the remaining empty cells.
     * - If no empty cell exists, the board is fully tiled — record it.
     * - Backtrack by undoing the last tile placement.
     *
     * Time complexity: Exponential in the number of cells, but bounded by small board size
     * Space complexity: O(m*n) for the recursion stack
     */
    private void backtrack() {
        // Step 1: Find the first empty cell by scanning left-to-right, top-to-bottom
        int[] firstEmpty = findFirstEmpty();

        // Step 2: If no empty cell found, the board is completely tiled — record this solution
        if (firstEmpty == null) {
            // Deep copy the current board state and add to results
            int[][] copy = deepCopy(board);
            results.add(copy);
            return; // Base case: successful tiling found
        }

        // Extract the row and column of the first empty cell
        int row = firstEmpty[0];
        int col = firstEmpty[1];

        // Step 3: Get the current tile label (will be incremented for each new tile)
        int label = tileLabel;

        // Step 4: Try all possible tile placements that cover (row, col)
        // We define all tile shapes as arrays of [deltaRow, deltaCol] offsets from (row, col)
        // Each tile shape is a list of cells it covers (including the anchor cell at offset [0,0])

        List<int[][]> tileShapes = getTileShapes();

        // Step 5: Iterate over each possible tile shape
        for (int[][] shape : tileShapes) {
            // Check if this shape can be placed starting at (row, col)
            // The shape must:
            // (a) fit within the board boundaries
            // (b) cover only currently empty cells
            // (c) include the first empty cell (row, col) — guaranteed by construction

            if (canPlace(row, col, shape)) {
                // Place the tile: mark all cells covered by this shape with the current label
                placeTile(row, col, shape, label);
                tileLabel++; // Increment label for the next tile

                // Recurse to fill the rest of the board
                backtrack();

                // Backtrack: remove the tile (reset cells to 0) and restore the label counter
                tileLabel--;
                removeTile(row, col, shape);
            }
        }
        // If no tile can be placed at (row, col), this branch is a dead end — backtrack implicitly
    }

    /**
     * Returns all possible tile shapes as lists of cell offsets from an anchor cell.
     *
     * <p>Each shape is represented as a 2D array of [deltaRow, deltaCol] pairs.
     * The anchor cell is always included (offset [0, 0] is implicit or explicit).
     *
     * <p>Tile types:
     * - Domino horizontal: covers (r,c) and (r,c+1)
     * - Domino vertical:   covers (r,c) and (r+1,c)
     * - Tromino rotation 1 (L): covers (r,c), (r+1,c), (r+1,c+1)  — bottom-right L
     * - Tromino rotation 2 (L): covers (r,c), (r,c+1), (r+1,c)    — bottom-left L
     * - Tromino rotation 3 (L): covers (r,c), (r,c+1), (r+1,c+1)  — top-left L
     * - Tromino rotation 4 (L): covers (r,c), (r+1,c), (r,c+1) with different anchor
     *
     * <p>We enumerate all 6 distinct shapes (2 dominoes + 4 tromino rotations).
     *
     * @return list of tile shapes, each shape is an array of [deltaRow, deltaCol] offsets
     *
     * Time complexity: O(1) — fixed number of shapes
     * Space complexity: O(1) — fixed size list
     */
    private List<int[][]> getTileShapes() {
        List<int[][]> shapes = new ArrayList<>();

        // ---- DOMINO SHAPES ----

        // Domino 1: Horizontal domino — covers (r,c) and (r,c+1)
        // [ ][ ]
        shapes.add(new int[][] {
            {0, 0}, {0, 1}
        });

        // Domino 2: Vertical domino — covers (r,c) and (r+1,c)
        // [ ]
        // [ ]
        shapes.add(new int[][] {
            {0, 0}, {1, 0}
        });

        // ---- TROMINO SHAPES (L-shaped, 4 rotations) ----

        // Tromino Rotation 1:
        // [ ]
        // [ ][ ]
        // Covers (r,c), (r+1,c), (r+1,c+1)
        shapes.add(new int[][] {
            {0, 0}, {1, 0}, {1, 1}
        });

        // Tromino Rotation 2:
        // [ ][ ]
        // [ ]
        // Covers (r,c), (r,c+1), (r+1,c)
        shapes.add(new int[][] {
            {0, 0}, {0, 1}, {1, 0}
        });

        // Tromino Rotation 3:
        // [ ][ ]
        //    [ ]
        // Covers (r,c), (r,c+1), (r+1,c+1)
        shapes.add(new int[][] {
            {0, 0}, {0, 1}, {1, 1}
        });

        // Tromino Rotation 4:
        //    [ ]
        // [ ][ ]
        // Covers (r,c), (r+1,c), (r+1,c-1)
        // Note: c-1 offset means we go left, so deltaCol = -1
        shapes.add(new int[][] {
            {0, 0}, {1, 0}, {1, -1}
        });

        return shapes;
    }

    /**
     * Checks whether a tile shape can be placed with its anchor at (row, col).
     *
     * <p>A placement is valid if:
     * 1. All cells covered by the shape are within the board boundaries.
     * 2. All cells covered by the shape are currently empty (value == 0).
     *
     * @param row    the row of the anchor cell
     * @param col    the column of the anchor cell
     * @param shape  the tile shape as an array of [deltaRow, deltaCol] offsets
     * @return true if the tile can be placed, false otherwise
     *
     * Time complexity: O(k) where k is the number of cells in the shape (2 or 3)
     * Space complexity: O(1)
     */
    private boolean canPlace(int row, int col, int[][] shape) {
        // Check each cell that the shape would cover
        for (int[] offset : shape) {
            int r = row + offset[0]; // Actual row of this cell
            int c = col + offset[1]; // Actual column of this cell

            // Check boundary: row must be in [0, m-1]
            if (r < 0 || r >= m) return false;

            // Check boundary: column must be in [0, n-1]
            if (c < 0 || c >= n) return false;

            // Check that the cell is currently empty
            if (board[r][c] != 0) return false;
        }
        // All cells are valid and empty — placement is possible
        return true;
    }

    /**
     * Places a tile on the board by marking all covered cells with the given label.
     *
     * @param row    the row of the anchor cell
     * @param col    the column of the anchor cell
     * @param shape  the tile shape as an array of [deltaRow, deltaCol] offsets
     * @param label  the positive integer label to assign to all cells of this tile
     *
     * Time complexity: O(k) where k is the number of cells in the shape
     * Space complexity: O(1)
     */
    private void placeTile(int row, int col, int[][] shape, int label) {
        // Mark each cell covered by the shape with the tile label
        for (int[] offset : shape) {
            int r = row + offset[0];
            int c = col + offset[1];
            board[r][c] = label; // Assign the tile's unique label
        }
    }

    /**
     * Removes a tile from the board by resetting all covered cells to 0 (empty).
     *
     * <p>This is the "undo" step in backtracking.
     *
     * @param row    the row of the anchor cell
     * @param col    the column of the anchor cell
     * @param shape  the tile shape as an array of [deltaRow, deltaCol] offsets
     *
     * Time complexity: O(k) where k is the number of cells in the shape
     * Space complexity: O(1)
     */
    private void removeTile(int row, int col, int[][] shape) {
        // Reset each cell covered by the shape back to 0 (empty)
        for (int[] offset : shape) {
            int r = row + offset[0];
            int c = col + offset[1];
            board[r][c] = 0; // Mark as empty again
        }
    }

    /**
     * Finds the first empty cell in the board by scanning left-to-right, top-to-bottom.
     *
     * <p>This ensures we always try to fill the topmost-leftmost empty cell first,
     * which is a key property for avoiding duplicate solutions in backtracking.
     *
     * @return an array [row, col] of the first empty cell, or null if no empty cell exists
     *
     * Time complexity: O(m*n)
     * Space complexity: O(1)
     */
    private int[] findFirstEmpty() {
        // Scan row by row, left to right within each row
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == 0) {
                    // Found the first empty cell
                    return new int[] {r, c};
                }
            }
        }
        // No empty cell found — board is completely filled
        return null;
    }

    /**
     * Creates a deep copy of a 2D integer array.
     *
     * @param original the 2D array to copy
     * @return a new 2D array with the same values as the original
     *
     * Time complexity: O(m*n)
     * Space complexity: O(m*n)
     */
    private int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            // Arrays.copy