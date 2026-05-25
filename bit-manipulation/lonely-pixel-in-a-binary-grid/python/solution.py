```python
"""
Title: Lonely Pixel in a Binary Grid
Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
You are given a binary grid represented as an array of integers, where each integer
encodes one row of the grid using its binary representation. A pixel at position
(row, col) is considered **lonely** if it is set to `1` and no other `1` exists in
the same column across all rows.

Your task is to count the total number of lonely pixels in the grid.

Each row is represented as a 32-bit unsigned integer. The grid has exactly `n` rows
and `w` columns, where `w` is the number of bits considered (given as a parameter).
Bit positions are counted from the **most significant bit** (leftmost) starting at
column index `0`.

Constraints:
- 1 <= n <= 1000
- 1 <= w <= 30
- Each element in `rows` is a non-negative integer fitting within `w` bits.

Example 1:
    Input: rows = [5, 3, 4], w = 3
    Row 0: 5 -> 101
    Row 1: 3 -> 011
    Row 2: 4 -> 100
    Column sums: col0=2, col1=1, col2=1
    But col2 has rows 0 and 2 set (5=101 has bit2 set, 4=100 has bit2 set? Let's check)
    Actually with w=3:
      col0 is the MSB (bit index w-1 = 2 from right, i.e., value 4)
      col1 is the middle bit (bit index 1 from right, value 2)
      col2 is the LSB (bit index 0 from right, value 1)
    Row 0: 5 = 101 -> col0=1, col1=0, col2=1
    Row 1: 3 = 011 -> col0=0, col1=1, col2=1
    Row 2: 4 = 100 -> col0=1, col1=0, col2=0
    Column sums: col0=2, col1=1, col2=2
    Lonely pixels: only col1 has exactly one 1 (row 1, col1)
    Output: 1

Example 2:
    Input: rows = [8, 4, 2, 1], w = 4
    Row 0: 8 = 1000 -> col0=1, col1=0, col2=0, col3=0
    Row 1: 4 = 0100 -> col0=0, col1=1, col2=0, col3=0
    Row 2: 2 = 0010 -> col0=0, col1=0, col2=1, col3=0
    Row 3: 1 = 0001 -> col0=0, col1=0, col2=0, col3=1
    Column sums: each column has exactly one 1.
    Output: 4
"""

from typing import List


class Solution:
    def count_lonely_pixels(self, rows: List[int], w: int) -> int:
        """
        Count the number of lonely pixels in a binary grid.

        A pixel is lonely if it is 1 and no other 1 exists in the same column.

        Args:
            rows: List of integers where each integer encodes one row of the grid
                  using its binary representation (MSB = column 0).
            w:    The number of bit columns to consider (width of the grid).

        Returns:
            The total count of lonely pixels.

        Time Complexity:  O(n * w) where n = number of rows, w = number of columns.
                          We iterate over all rows for each column (or equivalently,
                          over all columns for each row).
        Space Complexity: O(w) for the column-sum array.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build a column-sum array.
        #
        # We need to know, for each column c (0-indexed from the MSB/left),
        # how many rows have a '1' in that column.
        #
        # To extract bit at column c (where c=0 is the MSB):
        #   - The bit position from the right (LSB side) is: (w - 1 - c)
        #   - We check: (row_value >> (w - 1 - c)) & 1
        #
        # We create a list `col_count` of length w, initialized to 0.
        # -----------------------------------------------------------------------
        col_count: List[int] = [0] * w  # col_count[c] = number of 1s in column c

        for row_val in rows:
            # For each column index c from 0 to w-1:
            for c in range(w):
                # Compute the bit position from the right for column c
                bit_pos = w - 1 - c

                # Extract the bit at this position
                bit = (row_val >> bit_pos) & 1

                # Accumulate into the column count
                col_count[c] += bit

        # -----------------------------------------------------------------------
        # STEP 2: Count lonely pixels.
        #
        # A pixel at (row_index, col) is lonely if:
        #   1. The bit at column col in that row is 1.
        #   2. col_count[col] == 1  (no other row has a 1 in that column).
        #
        # We iterate over every (row, col) pair and check both conditions.
        # -----------------------------------------------------------------------
        lonely_count: int = 0

        for row_val in rows:
            for c in range(w):
                # Compute the bit position from the right for column c
                bit_pos = w - 1 - c

                # Extract the bit at this position for the current row
                bit = (row_val >> bit_pos) & 1

                # Check if this pixel is 1 AND it is the only 1 in its column
                if bit == 1 and col_count[c] == 1:
                    lonely_count += 1

        return lonely_count


# ---------------------------------------------------------------------------
# Verification / Trace-through
# ---------------------------------------------------------------------------
# Example 1: rows = [5, 3, 4], w = 3
#   Encoding (MSB = col 0):
#     5 = 101 -> col0=1, col1=0, col2=1
#     3 = 011 -> col0=0, col1=1, col2=1
#     4 = 100 -> col0=1, col1=0, col2=0
#   col_count = [2, 1, 2]
#   Lonely check:
#     Row 5: col0 bit=1, col_count[0]=2 -> NOT lonely
#             col1 bit=0 -> skip
#             col2 bit=1, col_count[2]=2 -> NOT lonely
#     Row 3: col0 bit=0 -> skip
#             col1 bit=1, col_count[1]=1 -> LONELY  (+1)
#             col2 bit=1, col_count[2]=2 -> NOT lonely
#     Row 4: col0 bit=1, col_count[0]=2 -> NOT lonely
#             col1 bit=0 -> skip
#             col2 bit=0 -> skip
#   Total = 1  ✓
#
# Example 2: rows = [8, 4, 2, 1], w = 4
#   8=1000, 4=0100, 2=0010, 1=0001
#   col_count = [1, 1, 1, 1]
#   Every set bit is the only 1 in its column -> 4 lonely pixels  ✓
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    rows1 = [5, 3, 4]
    w1 = 3
    result1 = solution.count_lonely_pixels(rows1, w1)
    print("Example 1:")
    print(f"  Input : rows={rows1}, w={w1}")
    # Display the grid for clarity
    for i, r in enumerate(rows1):
        bits = format(r, f'0{w1}b')
        print(f"  Row {i}: {r} -> {bits}")
    print(f"  Output: {result1}")   # Expected: 1
    print()

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    rows2 = [8, 4, 2, 1]
    w2 = 4
    result2 = solution.count_lonely_pixels(rows2, w2)
    print("Example 2:")
    print(f"  Input : rows={rows2}, w={w2}")
    for i, r in enumerate(rows2):
        bits = format(r, f'0{w2}b')
        print(f"  Row {i}: {r} -> {bits}")
    print(f"  Output: {result2}")   # Expected: 4
    print()

    # ------------------------------------------------------------------
    # Additional edge case: single row, single column
    # ------------------------------------------------------------------
    rows3 = [1]
    w3 = 1
    result3 = solution.count_lonely_pixels(rows3, w3)
    print("Example 3 (edge case - single cell with 1):")
    print(f"  Input : rows={rows3}, w={w3}")
    print(f"  Output: {result3}")   # Expected: 1
    print()

    # ------------------------------------------------------------------
    # Additional edge case: all zeros
    # ------------------------------------------------------------------
    rows4 = [0, 0, 0]
    w4 = 3
    result4 = solution.count_lonely_pixels(rows4, w4)
    print("Example 4 (edge case - all zeros):")
    print(f"  Input : rows={rows4}, w={w4}")
    print(f"  Output: {result4}")   # Expected: 0
    print()

    # ------------------------------------------------------------------
    # Additional edge case: one column has multiple 1s, others are lonely
    # ------------------------------------------------------------------
    rows5 = [7, 7, 4]   # w=3
    # 7=111, 7=111, 4=100
    # col0: 1+1+1=3, col1: 1+1+0=2, col2: 1+1+0=2
    # No lonely pixels
    w5 = 3
    result5 = solution.count_lonely_pixels(rows5, w5)
    print("Example 5 (no lonely pixels):")
    print(f"  Input : rows={rows5}, w={w5}")
    for i, r in enumerate(rows5):
        bits = format(r, f'0{w5}b')
        print(f"  Row {i}: {r} -> {bits}")
    print(f"  Output: {result5}")   # Expected: 0
```