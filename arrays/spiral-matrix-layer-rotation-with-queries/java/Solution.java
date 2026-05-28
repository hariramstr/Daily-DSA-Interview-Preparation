```java
/*
 * Title: Spiral Matrix Layer Rotation with Queries
 *
 * Problem Description:
 * You are given an n x m matrix of integers. The matrix is composed of concentric
 * rectangular layers, where layer 0 is the outermost ring, layer 1 is the next ring
 * inward, and so on. You are also given a list of queries, where each query is of the
 * form [layer, k, direction], meaning: rotate all elements in the specified layer by k
 * positions in the given direction ('L' for left/clockwise, 'R' for right/counter-clockwise).
 *
 * After applying all queries in order, return the resulting matrix.
 *
 * A layer's elements are traversed in clockwise order starting from the top-left corner
 * of that layer. Rotating left by k means each element moves k positions forward in the
 * clockwise traversal order. Rotating right by k means each element moves k positions backward.
 *
 * Note: Multiple queries may target the same layer, and k can be larger than the number
 * of elements in the layer.
 *
 * Constraints:
 * - 1 <= n, m <= 300
 * - 1 <= queries.length <= 10^4
 * - 0 <= layer < min(n, m) / 2
 * - 1 <= k <= 10^9
 * - direction is either 'L' or 'R'
 */

import java.util.*;

/**
 * Solution class for Spiral Matrix Layer Rotation with Queries.
 * <p>
 * This solution extracts each layer's elements in clockwise order,
 * applies the rotation, and writes them back to the matrix.
 */
public class Solution {

    /**
     * Applies a series of rotation queries to the matrix layers and returns the result.
     *
     * <p>Algorithm Overview:
     * <ol>
     *   <li>For each query, extract the elements of the specified layer in clockwise order.</li>
     *   <li>Compute the effective rotation amount using modulo.</li>
     *   <li>Perform the rotation on the extracted list.</li>
     *   <li>Write the rotated elements back into the matrix layer positions.</li>
     * </ol>
     *
     * @param matrix  the n x m integer matrix to operate on
     * @param queries a list of queries, each as an Object array: [layer (int), k (int), direction (char)]
     * @return the matrix after all queries have been applied
     *
     * Time Complexity:  O(Q * (n + m)) where Q is the number of queries, since each query
     *                   processes at most O(n + m) elements in a layer.
     * Space Complexity: O(n + m) for the temporary list used to hold layer elements.
     */
    public int[][] solve(int[][] matrix, Object[][] queries) {
        int n = matrix.length;
        int m = matrix[0].length;

        // Process each query one by one in order
        for (Object[] query : queries) {
            // Extract query parameters
            int layer = (int) query[0];
            int k = (int) query[1];
            char direction = (char) query[2];

            // Step 1: Extract all elements of this layer in clockwise order.
            // The layer boundary is defined by the offset from the matrix edges.
            List<Integer> elements = extractLayer(matrix, n, m, layer);

            // Step 2: Determine the size of the layer (number of elements in the ring).
            int size = elements.size();

            // If the layer has 0 or 1 elements, rotation has no effect.
            if (size <= 1) continue;

            // Step 3: Compute effective rotation amount using modulo to handle k > size.
            // For 'L' (left/clockwise): elements shift forward, so index i goes to index (i - k + size) % size
            // For 'R' (right/counter-clockwise): elements shift backward, so index i goes to index (i + k) % size
            int effectiveK = k % size;

            // Step 4: Perform the rotation.
            // We create a new list to hold the rotated elements.
            List<Integer> rotated = new ArrayList<>(size);

            if (direction == 'L') {
                // Left rotation by effectiveK:
                // The new list starts at index effectiveK of the original list.
                // Example: [1,2,3,4,5] rotated left by 2 => [3,4,5,1,2]
                for (int i = effectiveK; i < size; i++) {
                    rotated.add(elements.get(i));
                }
                for (int i = 0; i < effectiveK; i++) {
                    rotated.add(elements.get(i));
                }
            } else {
                // Right rotation by effectiveK:
                // The new list starts at index (size - effectiveK) of the original list.
                // Example: [1,2,3,4,5] rotated right by 2 => [4,5,1,2,3]
                int start = size - effectiveK;
                for (int i = start; i < size; i++) {
                    rotated.add(elements.get(i));
                }
                for (int i = 0; i < start; i++) {
                    rotated.add(elements.get(i));
                }
            }

            // Step 5: Write the rotated elements back into the matrix at the same layer positions.
            writeLayer(matrix, n, m, layer, rotated);
        }

        return matrix;
    }

    /**
     * Extracts all elements of a given layer from the matrix in clockwise order,
     * starting from the top-left corner of that layer.
     *
     * <p>Clockwise traversal order for a layer:
     * <ol>
     *   <li>Top row: left to right</li>
     *   <li>Right column: top+1 to bottom</li>
     *   <li>Bottom row: right-1 to left (only if there are multiple rows)</li>
     *   <li>Left column: bottom-1 to top+1 (only if there are multiple columns)</li>
     * </ol>
     *
     * @param matrix the matrix to extract from
     * @param n      number of rows in the matrix
     * @param m      number of columns in the matrix
     * @param layer  the layer index (0 = outermost)
     * @return a list of integers representing the layer elements in clockwise order
     *
     * Time Complexity:  O(n + m) — traverses the perimeter of the layer once.
     * Space Complexity: O(n + m) — stores all elements of the layer.
     */
    private List<Integer> extractLayer(int[][] matrix, int n, int m, int layer) {
        List<Integer> elements = new ArrayList<>();

        // Define the boundaries of this layer.
        // 'layer' acts as an offset from each edge.
        int top = layer;
        int bottom = n - 1 - layer;
        int left = layer;
        int right = m - 1 - layer;

        // If the layer boundaries are invalid (layer too deep), return empty list.
        if (top > bottom || left > right) {
            return elements;
        }

        // Traverse the top row from left to right.
        for (int col = left; col <= right; col++) {
            elements.add(matrix[top][col]);
        }

        // Traverse the right column from top+1 to bottom.
        for (int row = top + 1; row <= bottom; row++) {
            elements.add(matrix[row][right]);
        }

        // Traverse the bottom row from right-1 to left (only if top != bottom, i.e., more than one row).
        if (top < bottom) {
            for (int col = right - 1; col >= left; col--) {
                elements.add(matrix[bottom][col]);
            }
        }

        // Traverse the left column from bottom-1 to top+1 (only if left != right, i.e., more than one column).
        if (left < right) {
            for (int row = bottom - 1; row >= top + 1; row--) {
                elements.add(matrix[row][left]);
            }
        }

        return elements;
    }

    /**
     * Writes a list of elements back into the specified layer of the matrix,
     * following the same clockwise traversal order used during extraction.
     *
     * @param matrix   the matrix to write into
     * @param n        number of rows in the matrix
     * @param m        number of columns in the matrix
     * @param layer    the layer index (0 = outermost)
     * @param elements the list of elements to write back (in clockwise order)
     *
     * Time Complexity:  O(n + m) — writes to each position of the layer once.
     * Space Complexity: O(1) — no extra space beyond the index variable.
     */
    private void writeLayer(int[][] matrix, int n, int m, int layer, List<Integer> elements) {
        // Define the boundaries of this layer (same as in extractLayer).
        int top = layer;
        int bottom = n - 1 - layer;
        int left = layer;
        int right = m - 1 - layer;

        // Index to track our position in the elements list.
        int idx = 0;

        // Write the top row from left to right.
        for (int col = left; col <= right; col++) {
            matrix[top][col] = elements.get(idx++);
        }

        // Write the right column from top+1 to bottom.
        for (int row = top + 1; row <= bottom; row++) {
            matrix[row][right] = elements.get(idx++);
        }

        // Write the bottom row from right-1 to left (only if top != bottom).
        if (top < bottom) {
            for (int col = right - 1; col >= left; col--) {
                matrix[bottom][col] = elements.get(idx++);
            }
        }

        // Write the left column from bottom-1 to top+1 (only if left != right).
        if (left < right) {
            for (int row = bottom - 1; row >= top + 1; row--) {
                matrix[row][left] = elements.get(idx++);
            }
        }
    }

    /**
     * Helper method to print a 2D matrix in a readable format.
     *
     * @param matrix the matrix to print
     *
     * Time Complexity:  O(n * m)
     * Space Complexity: O(1)
     */
    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    /**
     * Main method demonstrating the solution with the provided examples.
     *
     * <p>Example 1:
     * Input:  matrix = [[1,2,3],[4,5,6],[7,8,9]], queries = [[0,1,'L']]
     * Expected Output: [[2,3,6],[1,5,9],[4,7,8]]
     *
     * <p>Example 2:
     * Input:  matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12],[13,14,15,16]], queries = [[0,2,'R'],[1,1,'L']]
     * Expected Output: [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -------------------------------------------------------
        // Example 1:
        // Matrix:
        //   1 2 3
        //   4 5 6
        //   7 8 9
        //
        // Query: [0, 1, 'L'] — rotate layer 0 left by 1
        //
        // Layer 0 clockwise: [1, 2, 3, 6, 9, 8, 7, 4]
        // After left rotation by 1: [2, 3, 6, 9, 8, 7, 4, 1]
        //
        // Placing back:
        //   Top row (left to right):    2, 3, 6
        //   Right col (top+1 to bot):   9
        //   Bottom row (right-1 to left): 8, 7, 4
        //   Left col (bot-1 to top+1):  1
        //
        // Result:
        //   2 3 6
        //   1 5 9
        //   4 7 8
        // -------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[][] matrix1 = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        Object[][] queries1 = {
            {0, 1, 'L'}
        };
        int[][] result1 = solution.solve(matrix1, queries1);
        System.out.println("Result:");
        printMatrix(result1);
        System.out.println("Expected:");
        System.out.println("[2, 3, 6]");
        System.out.println("[1, 5, 9]");
        System.out.println("[4, 7, 8]");
        System.out.println();

        // -------------------------------------------------------
        // Example 2:
        // Matrix:
        //    1  2  3  4
        //    5  6  7  8
        //    9 10 11 12
        //   13 14 15 16
        //
        // Query 1: [0, 2, 'R'] — rotate layer 0 right by 2
        //
        // Layer 0 clockwise: [1, 2, 3, 4, 8, 12, 16, 15, 14, 13, 9, 5]
        // Size = 12, effectiveK = 2 % 12 = 2
        // Right rotation by 2: last 2 elements come to front
        //   => [9, 5, 1, 2, 3, 4, 8, 12, 16, 15, 14, 13]
        //
        // Placing back into layer 0:
        //   Top row (4 elements):    9,  5,  1,  2
        //   Right col (rows 1-3):    3,  4,  8
        //   Bottom row (cols 2-0):  12, 16, 15
        //   Left col (rows 2-1):    14, 13
        //
        // Wait, let me re-trace carefully.
        // Layer 0: top=0, bottom=3, left=0, right=3
        //   Top row (col 0..3):     1, 2, 3, 4
        //   Right col (row 1..3):   8, 12, 16
        //   Bottom row (col 2..0):  15, 14, 13
        //   Left col (row 2..1):    9, 5
        // So layer 0 = [1, 2, 3, 4, 8, 12, 16, 15, 14, 13, 9, 5]  (size=12)
        //
        // Right rotation by 2: start = 12 - 2 = 10
        //   elements[10..11] = [9, 5], then elements[0..9] = [1,2,3,4,8,12,16,15,14,13]
        //   rotated = [9, 5, 1, 2, 3, 4, 8, 12, 16, 15, 14, 13]
        //
        // Write back:
        //   Top row (col 0..3):     9, 5, 1, 2
        //   Right col (row 1..3):   3, 4, 8
        //   Bottom row (col 2..0): 12, 16, 15
        //