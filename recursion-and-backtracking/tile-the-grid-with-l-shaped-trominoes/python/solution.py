```python
"""
Title: Tile the Grid with L-Shaped Trominoes
Difficulty: Medium
Topic: Recursion and Backtracking

Problem Description:
You are given a grid of size `rows x cols` and a list of `blocked` cells that cannot be covered.
Your task is to determine all distinct ways to tile the remaining empty cells using L-shaped
trominoes (each tromino covers exactly 3 cells in an L-shape). A valid tiling must cover every
non-blocked cell exactly once, with no tromino extending outside the grid boundaries or
overlapping a blocked cell.

An L-shaped tromino can be placed in 4 rotations:
- Rotation 0: covers (r,c), (r+1,c), (r+1,c+1)
- Rotation 1: covers (r,c), (r,c+1), (r+1,c+1)
- Rotation 2: covers (r,c), (r,c+1), (r+1,c)
- Rotation 3: covers (r,c+1), (r+1,c), (r+1,c+1)

Return the total number of distinct valid tilings modulo 10^9 + 7.
If it is impossible to tile all non-blocked cells, return 0.
"""

from typing import List, Tuple


class Solution:
    """
    Solution class for the L-shaped tromino tiling problem.
    Uses recursive backtracking to explore all valid placements.
    """

    def numTilings(self, rows: int, cols: int, blocked: List[List[int]]) -> int:
        """
        Count all distinct ways to tile the non-blocked cells with L-shaped trominoes.

        The approach:
        1. First check if the number of empty cells is divisible by 3 (necessary condition).
        2. Build a grid marking blocked cells.
        3. Use backtracking: always find the first uncovered empty cell (scanning top-left
           to bottom-right), then try all 4 rotations of an L-tromino that cover that cell
           as their top-left anchor or first cell.
        4. For each rotation, check if all 3 cells are within bounds and unoccupied.
        5. Place the tromino, recurse, then undo (backtrack).
        6. Count all complete tilings.

        Args:
            rows: Number of rows in the grid.
            cols: Number of columns in the grid.
            blocked: List of [r, c] pairs representing blocked cells.

        Returns:
            Total number of distinct valid tilings modulo 10^9 + 7.

        Time Complexity: O(4^(rows*cols/3)) in the worst case — exponential, but
                         the grid is at most 6x6=36 cells, so at most 12 trominoes,
                         making it very manageable in practice.
        Space Complexity: O(rows * cols) for the grid and recursion stack depth
                          O(rows * cols / 3) deep.
        """

        MOD = 10 ** 9 + 7

        # -----------------------------------------------------------------------
        # Step 1: Build the grid.
        # We use a 2D list where:
        #   0 = empty (needs to be covered)
        #   1 = blocked (must not be covered)
        #   2 = already covered by a tromino placed during backtracking
        # -----------------------------------------------------------------------
        grid = [[0] * cols for _ in range(rows)]

        # Mark all blocked cells as 1
        blocked_set = set()
        for r, c in blocked:
            grid[r][c] = 1
            blocked_set.add((r, c))

        # -----------------------------------------------------------------------
        # Step 2: Count empty cells and check divisibility by 3.
        # If the number of empty cells is not divisible by 3, it's impossible
        # to cover them all with trominoes (each covers exactly 3 cells).
        # -----------------------------------------------------------------------
        total_cells = rows * cols
        blocked_count = len(blocked)
        empty_cells = total_cells - blocked_count

        if empty_cells % 3 != 0:
            # Cannot partition into groups of 3 — immediately return 0
            return 0

        # -----------------------------------------------------------------------
        # Step 3: Define the 4 L-tromino rotations.
        # Each rotation is defined as a list of 3 (dr, dc) offsets from an
        # "anchor" cell (r, c). The anchor is always the top-leftmost cell
        # that the tromino touches, which ensures we always try to cover the
        # first uncovered cell we find (scanning row by row, left to right).
        #
        # Rotation 0: (r,c), (r+1,c), (r+1,c+1)   — L pointing right-down
        #   X .
        #   X X
        #
        # Rotation 1: (r,c), (r,c+1), (r+1,c+1)   — L pointing left-down
        #   X X
        #   . X
        #
        # Rotation 2: (r,c), (r,c+1), (r+1,c)     — L pointing right-up
        #   X X
        #   X .
        #
        # Rotation 3: (r,c+1), (r+1,c), (r+1,c+1) — L pointing left-up
        #   . X
        #   X X
        #
        # IMPORTANT: For the backtracking to work correctly, the anchor (r, c)
        # must be the first cell encountered in row-major order among the 3 cells
        # of the tromino. This way, when we find the first uncovered cell at (r,c),
        # we only try rotations where (r,c) is the "earliest" cell.
        #
        # Let's verify each rotation:
        # Rot 0: cells are (r,c), (r+1,c), (r+1,c+1). First in row-major = (r,c). ✓
        # Rot 1: cells are (r,c), (r,c+1), (r+1,c+1). First in row-major = (r,c). ✓
        # Rot 2: cells are (r,c), (r,c+1), (r+1,c). First in row-major = (r,c). ✓
        # Rot 3: cells are (r,c+1), (r+1,c), (r+1,c+1). First in row-major = (r,c+1).
        #        So the anchor here is (r, c+1), not (r, c).
        #        When we find the first uncovered cell at position (r, c), we should
        #        NOT try rotation 3 with anchor (r, c) because (r, c) itself is not
        #        covered by rotation 3.
        #
        # To handle this properly, we define rotations as offsets from the
        # first uncovered cell. For rotation 3, the first uncovered cell is
        # at offset (0, 0) relative to itself, but the tromino cells are at
        # (0,0)=anchor, (0,1), (1,-1+1)=(1,0), (1,1).
        # Wait, let me redefine more carefully.
        #
        # We want: given that (r, c) is the first uncovered cell, what trominoes
        # can cover (r, c)?
        #
        # Rot 0: (r,c), (r+1,c), (r+1,c+1)  → offsets from (r,c): (0,0),(1,0),(1,1)
        # Rot 1: (r,c), (r,c+1), (r+1,c+1)  → offsets from (r,c): (0,0),(0,1),(1,1)
        # Rot 2: (r,c), (r,c+1), (r+1,c)    → offsets from (r,c): (0,0),(0,1),(1,0)
        # Rot 3: (r,c), (r+1,c-1),(r+1,c)   → offsets from (r,c): (0,0),(1,-1),(1,0)
        #        This covers (r,c), (r+1,c-1), (r+1,c)
        #        In the original problem's rotation 3: (r,c+1),(r+1,c),(r+1,c+1)
        #        If we shift so (r,c) is the first cell: the "anchor" in original
        #        rotation 3 is (r, c+1). So if our first uncovered cell is at (r,c),
        #        we need the rotation where (r,c) plays the role of (r, c+1) in rot3.
        #        That means: (r,c), (r+1,c-1), (r+1,c) — offsets: (0,0),(1,-1),(1,0)
        #
        # So the 4 rotations, all anchored at the first uncovered cell (r,c):
        # -----------------------------------------------------------------------

        # Each rotation: list of (dr, dc) offsets from the first uncovered cell
        rotations: List[List[Tuple[int, int]]] = [
            [(0, 0), (1, 0), (1, 1)],   # Rotation 0
            [(0, 0), (0, 1), (1, 1)],   # Rotation 1
            [(0, 0), (0, 1), (1, 0)],   # Rotation 2
            [(0, 0), (1, -1), (1, 0)],  # Rotation 3 (anchor is top-right of the L)
        ]

        # -----------------------------------------------------------------------
        # Step 4: Define the backtracking function.
        # We scan the grid in row-major order (row 0 left-to-right, then row 1, etc.)
        # to find the first uncovered empty cell. We then try all 4 rotations.
        # For each valid placement, we mark the 3 cells as covered (value=2),
        # recurse, then unmark them (backtrack).
        # -----------------------------------------------------------------------

        # We'll use a mutable counter to accumulate results across recursive calls
        count = [0]

        def find_first_empty() -> Tuple[int, int]:
            """
            Scan the grid in row-major order and return the (row, col) of the
            first cell that is empty (value == 0). Returns (-1, -1) if none found.
            """
            for r in range(rows):
                for c in range(cols):
                    if grid[r][c] == 0:
                        return (r, c)
            return (-1, -1)

        def backtrack() -> None:
            """
            Recursive backtracking function.
            Finds the first uncovered empty cell, tries all 4 tromino rotations
            anchored at that cell, and recurses. If no empty cell remains,
            we have a complete valid tiling — increment the count.
            """
            # Find the first uncovered empty cell
            r, c = find_first_empty()

            # Base case: no empty cells remain → valid complete tiling found
            if r == -1:
                count[0] = (count[0] + 1) % MOD
                return

            # Try each of the 4 rotations anchored at (r, c)
            for rotation in rotations:
                # Compute the actual grid coordinates for this rotation's 3 cells
                cells = [(r + dr, c + dc) for dr, dc in rotation]

                # Check if all 3 cells are valid (in bounds and empty)
                valid = True
                for nr, nc in cells:
                    if nr < 0 or nr >= rows or nc < 0 or nc >= cols:
                        # Out of bounds
                        valid = False
                        break
                    if grid[nr][nc] != 0:
                        # Cell is either blocked (1) or already covered (2)
                        valid = False
                        break

                if not valid:
                    continue  # Try next rotation

                # Place the tromino: mark all 3 cells as covered
                for nr, nc in cells:
                    grid[nr][nc] = 2

                # Recurse to place the next tromino
                backtrack()

                # Backtrack: unmark all 3 cells (restore to empty)
                for nr, nc in cells:
                    grid[nr][nc] = 0

        # -----------------------------------------------------------------------
        # Step 5: Start the backtracking and return the result.
        # -----------------------------------------------------------------------
        backtrack()
        return count[0]


# -------------------------------------------------------------------------------
# Main block: test with the provided examples and additional cases
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    sol = Solution()

    # -------------------------------------------------------------------
    # Example 1: rows=2, cols=3, blocked=[]
    # Expected output: 2
    # The 2x3 grid has 6 empty cells = 2 trominoes.
    # There are exactly 2 ways to tile it.
    # -------------------------------------------------------------------
    result1 = sol.numTilings(2, 3, [])
    print(f"Example 1: rows=2, cols=3, blocked=[]")
    print(f"  Expected: 2")
    print(f"  Got:      {result1}")
    print(f"  {'PASS' if result1 == 2 else 'FAIL'}")
    print()

    # -------------------------------------------------------------------
    # Example 2: rows=2, cols=3, blocked=[[0,2]]
    # Expected output: 0
    # 6 - 1 = 5 empty cells, 5 % 3 != 0 → impossible
    # -------------------------------------------------------------------
    result2 = sol.numTilings(2, 3, [[0, 2]])
    print(f"Example 2: rows=2, cols=3, blocked=[[0,2]]")
    print(f"  Expected: 0")
    print(f"  Got:      {result2}")
    print(f"  {'PASS' if result2 == 0 else 'FAIL'}")
    print()

    # -------------------------------------------------------------------
    # Additional test: 1x1 grid, no blocked cells
    # 1 empty cell, 1 % 3 != 0 → 0
    # -------------------------------------------------------------------
    result3 = sol.numTilings(1, 1, [])
    print(f"Additional 1: rows=1, cols=1, blocked=[]")
    print(f"  Expected: 0")
    print(f"  Got:      {result3}")
    print(f"  {'PASS' if result3 == 0 else 'FAIL'}")
    print()

    # -------------------------------------------------------------------
    # Additional test: 1x3 grid, no blocked cells
    # 3 empty cells but no L-tromino fits in a 1-row grid → 0
    # (All rotations require at least 2 rows)
    # -------------------------------------------------------------------
    result4 = sol.numTilings(1, 3, [])
    print(f"Additional 2: rows=1, cols=3, blocked=[]")
    print(f"  Expected: 0")
    print(f"  Got:      {result4}")
    print(f"  {'PASS' if result4 == 0 else 'FAIL'}")
    print()

    # -------------------------------------------------------------------
    # Additional test: 2x2 grid with 1 blocked cell
    # 4 - 1 = 3 empty cells → exactly 1 tromino needed
    # Blocked cell at (0,0): remaining cells (0,1),(1,0),(1,1)
    # This matches rotation 3 (original): (r,c+1),(r+1,c),(r+1,c+1) with r=0,c=0
    # So there should be 1 way.
    # -------------------------------------------------------------------
    result5 = sol.numTilings(2, 2, [[0, 0]])
    print(f"Additional 3: rows=2, cols=2, blocked=[[0,0]]")
    print(f"  Expected: 1")
    print(f"  Got:      {result5}")
    print(f"  {'PASS