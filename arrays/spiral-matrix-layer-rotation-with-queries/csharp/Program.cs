/*
 * Title: Spiral Matrix Layer Rotation with Queries
 * 
 * Problem Description:
 * You are given an n x m matrix of integers. The matrix is composed of concentric
 * rectangular layers, where layer 0 is the outermost ring, layer 1 is the next ring
 * inward, and so on. You are also given a list of queries, where each query is of
 * the form [layer, k, direction], meaning: rotate all elements in the specified layer
 * by k positions in the given direction ('L' for left/clockwise, 'R' for right/counter-clockwise).
 *
 * After applying all queries in order, return the resulting matrix.
 *
 * A layer's elements are traversed in clockwise order starting from the top-left corner
 * of that layer. Rotating left by k means each element moves k positions forward in the
 * clockwise traversal order. Rotating right by k means each element moves k positions backward.
 *
 * Note: Multiple queries may target the same layer, and k can be larger than the number
 * of elements in the layer.
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // Time Complexity:  O(Q * (n + m) + n * m)
    //   - For each query we extract and re-place one layer: O(n + m) per query
    //   - Q queries total: O(Q * (n + m))
    //   - Final matrix construction: O(n * m)
    //
    // Space Complexity: O(n * m)
    //   - We store the matrix plus temporary layer lists of total size O(n + m)

    /// <summary>
    /// Applies a sequence of rotation queries to the concentric layers of a matrix
    /// and returns the modified matrix.
    /// </summary>
    /// <param name="matrix">The input n×m integer matrix.</param>
    /// <param name="queries">
    ///   Each query is an object[] with three elements:
    ///     [0] int  layer     – which concentric ring (0 = outermost)
    ///     [1] int  k         – how many positions to rotate
    ///     [2] char direction – 'L' (clockwise) or 'R' (counter-clockwise)
    /// </param>
    /// <returns>The matrix after all queries have been applied.</returns>
    public int[][] SpiralRotate(int[][] matrix, object[][] queries)
    {
        // ── Step 1: Capture matrix dimensions ────────────────────────────────
        // We need n (rows) and m (columns) throughout the algorithm.
        int n = matrix.Length;
        int m = matrix[0].Length;

        // ── Step 2: Process each query one by one ────────────────────────────
        // Queries must be applied sequentially because a later query may operate
        // on a layer that was already modified by an earlier query.
        foreach (object[] query in queries)
        {
            // ── Step 2a: Parse the query fields ──────────────────────────────
            // The query array holds mixed types, so we cast explicitly.
            int  layer     = (int)  query[0];
            int  k         = (int)  query[1];
            char direction = (char) query[2];

            // ── Step 2b: Extract the layer elements in clockwise order ────────
            // We walk the four sides of the rectangular ring defined by 'layer':
            //   Top row    : left  → right
            //   Right col  : top+1 → bottom
            //   Bottom row : right-1 → left   (only if there is more than one row)
            //   Left col   : bottom-1 → top+1 (only if there is more than one col)
            //
            // The ring boundaries are:
            //   top    = layer
            //   left   = layer
            //   bottom = n - 1 - layer
            //   right  = m - 1 - layer
            int top    = layer;
            int left   = layer;
            int bottom = n - 1 - layer;
            int right  = m - 1 - layer;

            // Collect all elements of this layer into a flat list.
            // Using a List<int> because we don't know the exact count upfront
            // (though we could compute it as 2*(rows+cols)-4).
            List<int> layerElements = new List<int>();

            // Top row: move right across the top edge
            for (int col = left; col <= right; col++)
                layerElements.Add(matrix[top][col]);

            // Right column: move down the right edge (skip the top-right corner already added)
            for (int row = top + 1; row <= bottom; row++)
                layerElements.Add(matrix[row][right]);

            // Bottom row: move left across the bottom edge
            // Guard: only traverse if bottom != top (i.e., the ring has more than one row)
            if (bottom > top)
                for (int col = right - 1; col >= left; col--)
                    layerElements.Add(matrix[bottom][col]);

            // Left column: move up the left edge
            // Guard: only traverse if left != right (i.e., the ring has more than one column)
            // Also skip the bottom-left corner if we already added it, and the top-left
            // corner which was the very first element.
            if (left < right)
                for (int row = bottom - 1; row > top; row--)
                    layerElements.Add(matrix[row][left]);

            // ── Step 2c: Compute the effective rotation amount ────────────────
            // The layer has 'count' elements. Rotating by 'count' positions is a
            // no-op, so we take k modulo count to get the minimal equivalent shift.
            int count = layerElements.Count;

            // If the layer has 0 or 1 element, rotation has no effect.
            if (count <= 1) continue;

            int shift = k % count;

            // ── Step 2d: Determine actual shift direction ─────────────────────
            // 'L' (left / clockwise): element at index i moves to index i - shift
            //   → equivalent to rotating the array left by 'shift'
            //   → new array = layerElements[shift..] + layerElements[..shift]
            //
            // 'R' (right / counter-clockwise): element at index i moves to index i + shift
            //   → equivalent to rotating the array right by 'shift'
            //   → rotating right by shift == rotating left by (count - shift)
            if (direction == 'R')
            {
                // Convert a right-rotation into an equivalent left-rotation.
                // Example: rotating [1,2,3,4] right by 1 → [4,1,2,3]
                //          same as rotating left by 3 (count - 1)
                shift = count - shift;
            }

            // ── Step 2e: Build the rotated layer list ─────────────────────────
            // After a left-rotation by 'shift', the new element at position i is
            // the old element at position (i + shift) % count.
            // We build a new list to avoid overwriting values we still need.
            List<int> rotated = new List<int>(count);
            for (int i = 0; i < count; i++)
                rotated.Add(layerElements[(i + shift) % count]);

            // ── Step 2f: Write the rotated elements back into the matrix ──────
            // We traverse the same clockwise path and place elements from 'rotated'
            // back into the corresponding matrix cells.
            int idx = 0; // index into the rotated list

            // Top row
            for (int col = left; col <= right; col++)
                matrix[top][col] = rotated[idx++];

            // Right column (skip top-right corner)
            for (int row = top + 1; row <= bottom; row++)
                matrix[row][right] = rotated[idx++];

            // Bottom row (skip bottom-right corner, guard for single-row rings)
            if (bottom > top)
                for (int col = right - 1; col >= left; col--)
                    matrix[bottom][col] = rotated[idx++];

            // Left column (skip corners, guard for single-column rings)
            if (left < right)
                for (int row = bottom - 1; row > top; row--)
                    matrix[row][left] = rotated[idx++];
        }

        // ── Step 3: Return the modified matrix ───────────────────────────────
        // All queries have been applied in-place, so we simply return the matrix.
        return matrix;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper to print a 2-D matrix to the console
// ─────────────────────────────────────────────────────────────────────────────
static void PrintMatrix(int[][] mat)
{
    Console.WriteLine("[");
    foreach (int[] row in mat)
        Console.WriteLine("  [" + string.Join(", ", row) + "]");
    Console.WriteLine("]");
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / test code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────
Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Input matrix:
//   1 2 3
//   4 5 6
//   7 8 9
//
// Layer 0 clockwise: [1, 2, 3, 6, 9, 8, 7, 4]
// Rotate left by 1 → [2, 3, 6, 9, 8, 7, 4, 1]
//
// Expected output:
//   2 3 6
//   1 5 9
//   4 7 8
Console.WriteLine("=== Example 1 ===");
int[][] matrix1 = new int[][]
{
    new int[] { 1, 2, 3 },
    new int[] { 4, 5, 6 },
    new int[] { 7, 8, 9 }
};
object[][] queries1 = new object[][]
{
    new object[] { 0, 1, 'L' }
};
int[][] result1 = sol.SpiralRotate(matrix1, queries1);
PrintMatrix(result1);
// Expected:
// [ [2, 3, 6],
//   [1, 5, 9],
//   [4, 7, 8] ]

Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Input matrix:
//    1  2  3  4
//    5  6  7  8
//    9 10 11 12
//   13 14 15 16
//
// Query 1: layer 0, rotate right by 2
//   Layer 0 clockwise: [1,2,3,4,8,12,16,15,14,13,9,5]  (12 elements)
//   Rotate right by 2 → shift left by 10
//   rotated: [9,5,1,2,3,4,8,12,16,15,14,13]
//   Place back:
//     Top row (cols 0-3):   9  5  1  2
//     Right col (rows 1-3): 3  4  8
//     Bottom row (cols 2-0):12 16 15 14 13  → wait let me retrace carefully
//
//   Actually let me retrace the clockwise extraction for a 4×4 matrix, layer 0:
//     top=0, left=0, bottom=3, right=3
//     Top row    cols 0→3: matrix[0][0..3] = 1,2,3,4
//     Right col  rows 1→3: matrix[1..3][3] = 8,12,16
//     Bottom row cols 2→0: matrix[3][2..0] = 15,14,13
//     Left col   rows 2→1: matrix[2..1][0] = 9,5
//   So layer 0 = [1,2,3,4,8,12,16,15,14,13,9,5]  ✓
//
//   Rotate right by 2: shift = 12 - 2 = 10 (left rotation by 10)
//   rotated[i] = layer[(i+10) % 12]
//   i=0: layer[10]=9   i=1: layer[11]=5   i=2: layer[0]=1   i=3: layer[1]=2
//   i=4: layer[2]=3    i=5: layer[3]=4    i=6: layer[4]=8   i=7: layer[5]=12
//   i=8: layer[6]=16   i=9: layer[7]=15   i=10:layer[8]=14  i=11:layer[9]=13
//   rotated = [9,5,1,2,3,4,8,12,16,15,14,13]
//
//   Place back:
//     Top row    [0][0..3]  ← 9,5,1,2
//     Right col  [1..3][3]  ← 3,4,8
//     Bottom row [3][2..0]  ← 12,16,15,14   wait: cols right-1..left = 2,1,0 → 3 values: 12,16,15  then left col
//     Left col   [2..1][0]  ← 14,13
//
//   Matrix after query 1:
//     row 0:  9  5  1  2
//     row 1: 13  6  7  3
//     row 2: 14 10 11  4
//     row 3: 15 16 12  8
//
// Query 2: layer 1, rotate left by 1
//   top=1, left=1, bottom=2, right=2
//   Top row    cols 1→2: matrix[1][1..2] = 6,7
//   Right col  rows 2→2: matrix[2][2]    = 11
//   Bottom row cols 1→1: matrix[2][1]    = 10
//   Left col   rows 1→1: (bottom-1=1, not > top=1, so skipped)
//   layer 1 = [6,7,11,10]
//   Rotate left by 1: shift=1
//   rotated = [7,11,10,6]
//   Place back:
//     [1][1]=7, [1][2]=11, [2][2]=10, [2][1]=6
//
//   Matrix after query 2:
//     row 0:  9  5  1  2
//     row 1: 13  7 11  3
//     row 2: 14  6 10  4
//     row 3: 15 16 12  8
//
// Expected output from problem:
//   [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
//
// Hmm, my trace gives a different answer. Let me re-examine the problem's expected output.
// The problem says after query1 (layer0, R, 2) then query2 (layer1, L, 1).
// Expected: [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
//
// Let me re-check my query1 result. Rotating right by 2 means elements shift 2 positions
// in the counter-clockwise direction. In terms of the clockwise list, each element
// moves 2 positions backward (to a lower index), so the list shifts right by 2.
//
// Right-