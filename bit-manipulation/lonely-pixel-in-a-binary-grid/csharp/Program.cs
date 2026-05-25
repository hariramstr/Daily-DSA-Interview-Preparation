/*
 * Lonely Pixel in a Binary Grid
 * ==============================
 * You are given a binary grid represented as an array of integers, where each integer
 * encodes one row of the grid using its binary representation. A pixel at position
 * (row, col) is considered LONELY if it is set to 1 and no other 1 exists in the
 * same column across all rows.
 *
 * Your task is to count the total number of lonely pixels in the grid.
 *
 * Each row is represented as a 32-bit unsigned integer. The grid has exactly n rows
 * and w columns, where w is the number of bits considered (given as a parameter).
 * Bit positions are counted from the MOST SIGNIFICANT BIT (leftmost) starting at
 * column index 0.
 *
 * Constraints:
 *   - 1 <= n <= 1000
 *   - 1 <= w <= 30
 *   - Each element in rows is a non-negative integer fitting within w bits.
 *
 * Example 1:
 *   Input: rows = [5, 3, 4], w = 3
 *   Row 0: 5 -> 101
 *   Row 1: 3 -> 011
 *   Row 2: 4 -> 100
 *   Column sums: col0=2, col1=1, col2=1 -- wait, col2 has rows 0 and 2 set.
 *   Actual lonely: only col1 has exactly one 1 (row 1). col2 has rows 0 and 2.
 *   Output: 1
 *
 * Example 2:
 *   Input: rows = [8, 4, 2, 1], w = 4
 *   Row 0: 1000
 *   Row 1: 0100
 *   Row 2: 0010
 *   Row 3: 0001
 *   Column sums: each column has exactly one 1.
 *   Output: 4
 */

using System;

// ============================================================
// Solution Class
// ============================================================
public class Solution
{
    /// <summary>
    /// Counts the number of lonely pixels in a binary grid.
    ///
    /// Time Complexity:  O(n * w)
    ///   - We iterate over all n rows and w columns twice:
    ///     once to build column sums, once to count lonely pixels.
    ///
    /// Space Complexity: O(w)
    ///   - We store one integer per column in the columnCount array.
    /// </summary>
    /// <param name="rows">Array of integers, each encoding one row via binary bits.</param>
    /// <param name="w">Number of columns (bits) to consider, counting from the MSB.</param>
    /// <returns>Total count of lonely pixels.</returns>
    public int CountLonelyPixels(int[] rows, int w)
    {
        // -------------------------------------------------------
        // STEP 1: Create a column-count array.
        // -------------------------------------------------------
        // We need to know, for each column, how many rows have a '1' in that column.
        // A pixel is lonely only if its column sum equals exactly 1.
        //
        // We allocate an array of size w (one slot per column).
        // All values start at 0 by default in C#.
        int[] columnCount = new int[w];

        // -------------------------------------------------------
        // STEP 2: Populate the column-count array.
        // -------------------------------------------------------
        // For each row, we examine each of the w bit positions.
        // Bit positions are counted from the MOST SIGNIFICANT BIT (MSB) as column 0.
        //
        // To extract the bit at column index col (0-based from MSB):
        //   - The MSB of the w-bit number is at bit position (w - 1) in standard
        //     binary notation (where bit 0 is the least significant bit).
        //   - So column col corresponds to bit position (w - 1 - col).
        //   - We shift the row value RIGHT by (w - 1 - col) and AND with 1.
        //
        // Example: w=3, row value=5 (binary 101)
        //   col=0 -> bit position 2 -> (5 >> 2) & 1 = 1  ✓ (MSB is 1)
        //   col=1 -> bit position 1 -> (5 >> 1) & 1 = 0  ✓ (middle bit is 0)
        //   col=2 -> bit position 0 -> (5 >> 0) & 1 = 1  ✓ (LSB is 1)

        foreach (int rowValue in rows)
        {
            // Iterate over each column index from 0 to w-1
            for (int col = 0; col < w; col++)
            {
                // Calculate which bit position corresponds to this column.
                // Column 0 is the leftmost (most significant) bit of the w-bit window.
                int bitPosition = w - 1 - col;

                // Extract the bit at this position using a right-shift and bitwise AND.
                // (rowValue >> bitPosition) moves the desired bit to position 0.
                // & 1 isolates just that bit (result is 0 or 1).
                int bitValue = (rowValue >> bitPosition) & 1;

                // Accumulate: if this bit is 1, increment the count for this column.
                columnCount[col] += bitValue;
            }
        }

        // -------------------------------------------------------
        // STEP 3: Count lonely pixels.
        // -------------------------------------------------------
        // A pixel at (row, col) is lonely if:
        //   1. The pixel itself is 1 (the bit is set in that row).
        //   2. The column sum for that column equals exactly 1
        //      (meaning no other row has a 1 in the same column).
        //
        // We iterate over every row and every column again.
        // For each position, we check both conditions.

        int lonelyCount = 0;

        for (int r = 0; r < rows.Length; r++)
        {
            for (int col = 0; col < w; col++)
            {
                // Recalculate the bit position for this column (same formula as above).
                int bitPosition = w - 1 - col;

                // Extract the bit value at this (row, col) position.
                int bitValue = (rows[r] >> bitPosition) & 1;

                // Condition 1: The pixel must be 1 (bit is set).
                // Condition 2: The column must have exactly one 1 across all rows.
                //
                // If both conditions are true, this pixel is lonely.
                if (bitValue == 1 && columnCount[col] == 1)
                {
                    // This pixel is lonely — increment our counter.
                    lonelyCount++;
                }
            }
        }

        // -------------------------------------------------------
        // STEP 4: Return the total count of lonely pixels.
        // -------------------------------------------------------
        return lonelyCount;
    }
}

// ============================================================
// Demo / Test Code (top-level statements)
// ============================================================

var solution = new Solution();

// -----------------------------------------------------------
// Example 1 Trace:
//   rows = [5, 3, 4], w = 3
//
//   Binary representations (3 bits each):
//     Row 0: 5  -> 101
//     Row 1: 3  -> 011
//     Row 2: 4  -> 100
//
//   Building columnCount:
//     col=0 (bit pos 2): row0=(5>>2)&1=1, row1=(3>>2)&1=0, row2=(4>>2)&1=1 -> sum=2
//     col=1 (bit pos 1): row0=(5>>1)&1=0, row1=(3>>1)&1=1, row2=(4>>1)&1=0 -> sum=1
//     col=2 (bit pos 0): row0=(5>>0)&1=1, row1=(3>>0)&1=1, row2=(4>>0)&1=0 -> sum=2
//
//   columnCount = [2, 1, 2]
//
//   Checking lonely pixels:
//     (0,0): bit=1, colCount=2 -> NOT lonely
//     (0,1): bit=0             -> NOT lonely (bit is 0)
//     (0,2): bit=1, colCount=2 -> NOT lonely
//     (1,0): bit=0             -> NOT lonely
//     (1,1): bit=1, colCount=1 -> LONELY ✓
//     (1,2): bit=1, colCount=2 -> NOT lonely
//     (2,0): bit=1, colCount=2 -> NOT lonely
//     (2,1): bit=0             -> NOT lonely
//     (2,2): bit=0             -> NOT lonely
//
//   Total lonely = 1  ✓ matches expected output
// -----------------------------------------------------------

int[] rows1 = { 5, 3, 4 };
int w1 = 3;
int result1 = solution.CountLonelyPixels(rows1, w1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input:    rows = [5, 3, 4], w = 3");
Console.WriteLine($"  Expected: 1");
Console.WriteLine($"  Got:      {result1}");
Console.WriteLine($"  Pass:     {result1 == 1}");
Console.WriteLine();

// -----------------------------------------------------------
// Example 2 Trace:
//   rows = [8, 4, 2, 1], w = 4
//
//   Binary representations (4 bits each):
//     Row 0: 8  -> 1000
//     Row 1: 4  -> 0100
//     Row 2: 2  -> 0010
//     Row 3: 1  -> 0001
//
//   Building columnCount:
//     col=0 (bit pos 3): 1,0,0,0 -> sum=1
//     col=1 (bit pos 2): 0,1,0,0 -> sum=1
//     col=2 (bit pos 1): 0,0,1,0 -> sum=1
//     col=3 (bit pos 0): 0,0,0,1 -> sum=1
//
//   columnCount = [1, 1, 1, 1]
//
//   Every set bit is in a column with sum=1, so all 4 set bits are lonely.
//   Total lonely = 4  ✓ matches expected output
// -----------------------------------------------------------

int[] rows2 = { 8, 4, 2, 1 };
int w2 = 4;
int result2 = solution.CountLonelyPixels(rows2, w2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input:    rows = [8, 4, 2, 1], w = 4");
Console.WriteLine($"  Expected: 4");
Console.WriteLine($"  Got:      {result2}");
Console.WriteLine($"  Pass:     {result2 == 4}");
Console.WriteLine();

// -----------------------------------------------------------
// Additional Edge Case: Single row, single column
//   rows = [1], w = 1
//   Row 0: 1 -> "1"
//   columnCount = [1]
//   Pixel (0,0): bit=1, colCount=1 -> LONELY
//   Expected: 1
// -----------------------------------------------------------

int[] rows3 = { 1 };
int w3 = 1;
int result3 = solution.CountLonelyPixels(rows3, w3);
Console.WriteLine("Edge Case - Single row, single column:");
Console.WriteLine($"  Input:    rows = [1], w = 1");
Console.WriteLine($"  Expected: 1");
Console.WriteLine($"  Got:      {result3}");
Console.WriteLine($"  Pass:     {result3 == 1}");
Console.WriteLine();

// -----------------------------------------------------------
// Additional Edge Case: All zeros
//   rows = [0, 0, 0], w = 3
//   No bits set anywhere -> no lonely pixels.
//   Expected: 0
// -----------------------------------------------------------

int[] rows4 = { 0, 0, 0 };
int w4 = 3;
int result4 = solution.CountLonelyPixels(rows4, w4);
Console.WriteLine("Edge Case - All zeros:");
Console.WriteLine($"  Input:    rows = [0, 0, 0], w = 3");
Console.WriteLine($"  Expected: 0");
Console.WriteLine($"  Got:      {result4}");
Console.WriteLine($"  Pass:     {result4 == 0}");