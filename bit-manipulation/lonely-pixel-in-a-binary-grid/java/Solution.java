```java
/*
 * Title: Lonely Pixel in a Binary Grid
 *
 * Problem Description:
 * You are given a binary grid represented as an array of integers, where each integer
 * encodes one row of the grid using its binary representation. A pixel at position
 * (row, col) is considered **lonely** if it is set to `1` and no other `1` exists
 * in the same column across all rows.
 *
 * Your task is to count the total number of lonely pixels in the grid.
 *
 * Each row is represented as a 32-bit unsigned integer. The grid has exactly `n` rows
 * and `w` columns, where `w` is the number of bits considered (given as a parameter).
 * Bit positions are counted from the **most significant bit** (leftmost) starting at
 * column index `0`.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - 1 <= w <= 30
 * - Each element in `rows` is a non-negative integer fitting within `w` bits.
 *
 * Example 1:
 * Input: rows = [5, 3, 4], w = 3
 *   Row 0: 5 -> 101
 *   Row 1: 3 -> 011
 *   Row 2: 4 -> 100
 * Column sums: col0=2, col1=1, col2=2
 * Lonely pixels: only col1 has exactly one 1 (row 1).
 * Output: 1
 *
 * Example 2:
 * Input: rows = [8, 4, 2, 1], w = 4
 *   Row 0: 1000
 *   Row 1: 0100
 *   Row 2: 0010
 *   Row 3: 0001
 * Column sums: each column has exactly one 1.
 * Output: 4
 */

import java.util.Arrays;

/**
 * Solution class for the "Lonely Pixel in a Binary Grid" problem.
 *
 * <p>Approach:
 * 1. First, compute the column sums — how many rows have a '1' in each column.
 * 2. Then, for each cell (row, col), check if the bit is set AND the column sum is exactly 1.
 * 3. Count all such lonely pixels.
 *
 * <p>Key insight: We use bit manipulation to extract individual bits from each row integer.
 * Since column 0 is the MSB (most significant bit) within the w-bit window, we shift
 * accordingly: for column `col`, the bit mask is `1 << (w - 1 - col)`.
 */
public class Solution {

    /**
     * Counts the number of lonely pixels in the binary grid.
     *
     * <p>A pixel at (row, col) is lonely if:
     * - It is set to 1, AND
     * - No other row has a 1 in the same column (i.e., column sum == 1).
     *
     * @param rows an array of integers, each representing one row of the grid in binary
     * @param w    the number of columns (bits) to consider, starting from the MSB side
     * @return the total count of lonely pixels
     *
     * Time Complexity:  O(n * w) — we iterate over all rows and all columns twice
     * Space Complexity: O(w)     — we store a column sum array of size w
     */
    public int countLonelyPixels(int[] rows, int w) {
        int n = rows.length;

        // -----------------------------------------------------------------------
        // Step 1: Build the column sum array.
        // columnSum[col] = number of rows that have a '1' at column index `col`.
        // Column index 0 corresponds to the leftmost (most significant) bit
        // within the w-bit representation.
        // -----------------------------------------------------------------------
        int[] columnSum = new int[w];

        for (int row = 0; row < n; row++) {
            // For each column, check if the bit at that position is set in rows[row].
            for (int col = 0; col < w; col++) {
                // The bit mask for column `col` (MSB = col 0):
                // If w=3 and col=0, mask = 1 << (3-1-0) = 1 << 2 = 4 (binary 100)
                // If w=3 and col=1, mask = 1 << (3-1-1) = 1 << 1 = 2 (binary 010)
                // If w=3 and col=2, mask = 1 << (3-1-2) = 1 << 0 = 1 (binary 001)
                int mask = 1 << (w - 1 - col);

                // Check if the bit at position `col` is set in the current row's value.
                if ((rows[row] & mask) != 0) {
                    // Increment the count for this column.
                    columnSum[col]++;
                }
            }
        }

        // -----------------------------------------------------------------------
        // Step 2: Count lonely pixels.
        // A pixel (row, col) is lonely if:
        //   - The bit at column `col` in rows[row] is set (value is 1), AND
        //   - columnSum[col] == 1 (no other row has a 1 in this column).
        // -----------------------------------------------------------------------
        int lonelyCount = 0;

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < w; col++) {
                // Compute the bit mask for this column (same formula as above).
                int mask = 1 << (w - 1 - col);

                // Check if this pixel is set to 1.
                boolean pixelIsSet = (rows[row] & mask) != 0;

                // Check if this column has exactly one '1' across all rows.
                boolean columnHasOnlyOne = (columnSum[col] == 1);

                // If both conditions are true, this pixel is lonely.
                if (pixelIsSet && columnHasOnlyOne) {
                    lonelyCount++;
                }
            }
        }

        // -----------------------------------------------------------------------
        // Step 3: Return the total count of lonely pixels.
        // -----------------------------------------------------------------------
        return lonelyCount;
    }

    /**
     * Helper method to display the binary grid for debugging/visualization purposes.
     *
     * @param rows the array of row integers
     * @param w    the number of columns (bits) to display
     *
     * Time Complexity:  O(n * w)
     * Space Complexity: O(w) for the StringBuilder
     */
    public void printGrid(int[] rows, int w) {
        System.out.println("Grid visualization (w=" + w + " bits, MSB=col0):");
        for (int row = 0; row < rows.length; row++) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Row ").append(row).append(": ").append(rows[row]).append(" -> ");
            for (int col = 0; col < w; col++) {
                int mask = 1 << (w - 1 - col);
                sb.append((rows[row] & mask) != 0 ? "1" : "0");
            }
            System.out.println(sb.toString());
        }
    }

    /**
     * Main method demonstrating the solution with sample inputs from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // rows = [5, 3, 4], w = 3
        //   Row 0: 5 -> 101
        //   Row 1: 3 -> 011
        //   Row 2: 4 -> 100
        //
        // Column sums:
        //   col0: rows 0 and 2 have bit set -> sum = 2
        //   col1: only row 1 has bit set    -> sum = 1
        //   col2: rows 0 and 2 have bit set -> sum = 2
        //
        // Wait, let's trace carefully:
        //   5 in binary (3 bits) = 101 -> col0=1, col1=0, col2=1
        //   3 in binary (3 bits) = 011 -> col0=0, col1=1, col2=1
        //   4 in binary (3 bits) = 100 -> col0=1, col1=0, col2=0
        //
        // Column sums:
        //   col0: 1+0+1 = 2
        //   col1: 0+1+0 = 1
        //   col2: 1+1+0 = 2
        //
        // Lonely pixels: only (row=1, col=1) where col1 sum=1 and bit is set.
        // Expected output: 1
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[] rows1 = {5, 3, 4};
        int w1 = 3;
        solution.printGrid(rows1, w1);

        // Manually trace column sums for verification:
        // 5 = 101: col0=1, col1=0, col2=1
        // 3 = 011: col0=0, col1=1, col2=1
        // 4 = 100: col0=1, col1=0, col2=0
        // columnSum = [2, 1, 2]
        System.out.println("Column sums (expected [2, 1, 2]):");
        int[] colSums1 = new int[w1];
        for (int r = 0; r < rows1.length; r++) {
            for (int c = 0; c < w1; c++) {
                int mask = 1 << (w1 - 1 - c);
                if ((rows1[r] & mask) != 0) colSums1[c]++;
            }
        }
        System.out.println("  " + Arrays.toString(colSums1));

        int result1 = solution.countLonelyPixels(rows1, w1);
        System.out.println("Lonely pixel count: " + result1);
        System.out.println("Expected: 1");
        System.out.println("PASS: " + (result1 == 1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // rows = [8, 4, 2, 1], w = 4
        //   Row 0: 8  -> 1000
        //   Row 1: 4  -> 0100
        //   Row 2: 2  -> 0010
        //   Row 3: 1  -> 0001
        //
        // Column sums:
        //   col0: only row 0 -> sum = 1
        //   col1: only row 1 -> sum = 1
        //   col2: only row 2 -> sum = 1
        //   col3: only row 3 -> sum = 1
        //
        // All 4 pixels are lonely.
        // Expected output: 4
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int[] rows2 = {8, 4, 2, 1};
        int w2 = 4;
        solution.printGrid(rows2, w2);

        System.out.println("Column sums (expected [1, 1, 1, 1]):");
        int[] colSums2 = new int[w2];
        for (int r = 0; r < rows2.length; r++) {
            for (int c = 0; c < w2; c++) {
                int mask = 1 << (w2 - 1 - c);
                if ((rows2[r] & mask) != 0) colSums2[c]++;
            }
        }
        System.out.println("  " + Arrays.toString(colSums2));

        int result2 = solution.countLonelyPixels(rows2, w2);
        System.out.println("Lonely pixel count: " + result2);
        System.out.println("Expected: 4");
        System.out.println("PASS: " + (result2 == 4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3: All zeros — no lonely pixels.
        // rows = [0, 0, 0], w = 3
        // Expected output: 0
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3 (All zeros) ===");
        int[] rows3 = {0, 0, 0};
        int w3 = 3;
        solution.printGrid(rows3, w3);
        int result3 = solution.countLonelyPixels(rows3, w3);
        System.out.println("Lonely pixel count: " + result3);
        System.out.println("Expected: 0");
        System.out.println("PASS: " + (result3 == 0));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4: Single row — all set bits are lonely.
        // rows = [7], w = 3  (7 = 111)
        // Column sums: col0=1, col1=1, col2=1
        // All 3 pixels are lonely.
        // Expected output: 3
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4 (Single row, all bits set) ===");
        int[] rows4 = {7};
        int w4 = 3;
        solution.printGrid(rows4, w4);
        int result4 = solution.countLonelyPixels(rows4, w4);
        System.out.println("Lonely pixel count: " + result4);
        System.out.println("Expected: 3");
        System.out.println("PASS: " + (result4 == 3));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 5: Two identical rows — no lonely pixels.
        // rows = [5, 5], w = 3  (5 = 101)
        //   Row 0: 101
        //   Row 1: 101
        // Column sums: col0=2, col1=0, col2=2
        // No column has sum == 1, so no lonely pixels.
        // Expected output: 0
        // -----------------------------------------------------------------------
        System.out.println("=== Example 5 (Two identical rows) ===");
        int[] rows5 = {5, 5};
        int w5 = 3;
        solution.printGrid(rows5, w5);
        int result5 = solution.countLonelyPixels(rows5, w5);
        System.out.println("Lonely pixel count: " + result5);
        System.out.println("Expected: 0");
        System.out.println("PASS: " + (result5 == 0));
    }
}
```