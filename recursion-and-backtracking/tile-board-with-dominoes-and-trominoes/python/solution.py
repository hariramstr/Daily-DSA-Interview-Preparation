```python
"""
Title: Tile a Board with Dominoes and Trominoes
Difficulty: Medium
Topic: Recursion and Backtracking

Problem Description:
You are given an m x n grid board. You have an unlimited supply of two types of tiles:
- Domino: covers exactly 2 adjacent cells (placed horizontally or vertically)
- Tromino (L-shape): covers exactly 3 cells in an L-shaped pattern (any of the 4 rotations)

Your task is to find all distinct ways to completely tile the board such that every cell
is covered by exactly one tile. Two tilings are considered different if at least one tile
occupies a different set of cells.

Return the count of all valid tilings.

Constraints:
- 1 <= m <= 4
- 1 <= n <= 4
"""

from typing import List, Tuple, Optional


class Solution:
    def tile_board(self, m: int, n: int) -> int:
        """
        Count all distinct ways to tile an m x n board using dominoes and trominoes.

        Strategy: Use backtracking. At each step, find the first uncovered cell
        (scanning left-to-right, top-to-bottom), and try placing every possible
        tile that covers that cell. Since we always fill the first empty cell,
        we avoid counting duplicate arrangements.

        Args:
            m: Number of rows in the board.
            n: Number of columns in the board.

        Returns:
            The count of all valid tilings.

        Time Complexity: O(T^(m*n)) where T is the number of tile shapes tried
                         at each step. In practice much smaller due to pruning.
        Space Complexity: O(m*n) for the board and recursion stack depth.
        """

        # -----------------------------------------------------------------------
        # Step 1: Initialize the board.
        # We use a 2D list where 0 means "uncovered" and a positive integer
        # means "covered by tile with that label".
        # -----------------------------------------------------------------------
        board: List[List[int]] = [[0] * n for _ in range(m)]

        # We'll use a mutable counter (list of one int) so the nested function
        # can modify it without needing 'nonlocal' repeatedly.
        count: List[int] = [0]

        # tile_label tracks the next label to assign to a new tile.
        # Using a list so the inner function can mutate it.
        tile_label: List[int] = [1]

        def find_first_empty() -> Optional[Tuple[int, int]]:
            """
            Scan the board left-to-right, top-to-bottom and return the
            (row, col) of the first uncovered cell, or None if all cells
            are covered.

            Returns:
                Tuple (row, col) of first empty cell, or None.
            """
            for r in range(m):
                for c in range(n):
                    if board[r][c] == 0:
                        return (r, c)
            return None  # Board is fully covered

        def place_tile(cells: List[Tuple[int, int]], label: int) -> None:
            """
            Mark the given cells on the board with the given label.

            Args:
                cells: List of (row, col) positions to mark.
                label: The integer label to assign.
            """
            for (r, c) in cells:
                board[r][c] = label

        def remove_tile(cells: List[Tuple[int, int]]) -> None:
            """
            Unmark the given cells on the board (set back to 0).

            Args:
                cells: List of (row, col) positions to clear.
            """
            for (r, c) in cells:
                board[r][c] = 0

        def in_bounds(r: int, c: int) -> bool:
            """
            Check whether (r, c) is within the board boundaries.

            Args:
                r: Row index.
                c: Column index.

            Returns:
                True if within bounds, False otherwise.
            """
            return 0 <= r < m and 0 <= c < n

        def all_empty(cells: List[Tuple[int, int]]) -> bool:
            """
            Check that all given cells are currently uncovered (value == 0).

            Args:
                cells: List of (row, col) positions to check.

            Returns:
                True if all cells are empty, False otherwise.
            """
            return all(board[r][c] == 0 for (r, c) in cells)

        def get_tile_shapes(r: int, c: int) -> List[List[Tuple[int, int]]]:
            """
            Generate all possible tile placements that include cell (r, c)
            as their "anchor" — i.e., (r, c) must be the first empty cell
            covered by the tile.

            We enumerate:
            - 2 domino orientations (horizontal, vertical)
            - 4 tromino (L-shape) orientations

            For each shape, we only include it if:
            1. All cells are in bounds.
            2. All cells are currently empty.
            3. The cell (r, c) is the topmost-leftmost cell of the shape
               (so we don't re-discover the same shape from a different anchor).

            Args:
                r: Row of the first empty cell.
                c: Column of the first empty cell.

            Returns:
                List of valid tile placements, each a list of (row, col) tuples.
            """
            shapes: List[List[Tuple[int, int]]] = []

            # -------------------------------------------------------------------
            # Domino shapes:
            # A domino covers 2 cells. The anchor (r, c) is always included.
            # We only place the domino such that (r, c) is the "first" cell
            # in reading order (top-left). So we extend to the right or down.
            # -------------------------------------------------------------------

            # Horizontal domino: (r,c) and (r, c+1)
            candidate = [(r, c), (r, c + 1)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Vertical domino: (r,c) and (r+1, c)
            candidate = [(r, c), (r + 1, c)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # -------------------------------------------------------------------
            # Tromino (L-shape) shapes:
            # An L-tromino covers 3 cells. There are 4 rotations.
            # We define each rotation by its offsets from the anchor (r, c).
            # We must ensure (r, c) is the topmost-then-leftmost cell of the shape.
            #
            # The 4 L-tromino rotations (using offsets from anchor):
            #
            # Rotation 1 (┘ shape, anchor top-left):
            #   X X
            #   X .
            #   Cells: (r,c), (r,c+1), (r+1,c)
            #
            # Rotation 2 (└ shape, anchor top-right):
            #   X X
            #   . X
            #   Cells: (r,c), (r,c+1), (r+1,c+1)
            #
            # Rotation 3 (┐ shape, anchor top-left of bottom row):
            #   X .
            #   X X
            #   Cells: (r,c), (r+1,c), (r+1,c+1)
            #
            # Rotation 4 (┌ shape, anchor top-right of bottom row):
            #   . X
            #   X X
            #   Cells: (r,c), (r+1,c-1), (r+1,c)
            #   BUT: we need (r,c) to be the first empty cell. Since (r+1,c-1)
            #   comes after (r,c) in reading order only if c-1 >= c which is false.
            #   Actually (r,c) IS before (r+1,c-1) and (r+1,c) in reading order,
            #   so this is valid as long as (r+1,c-1) is in bounds and empty.
            # -------------------------------------------------------------------

            # Tromino Rotation 1: top-left 2x2 minus bottom-right
            #   X X
            #   X .
            candidate = [(r, c), (r, c + 1), (r + 1, c)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Tromino Rotation 2: top-left 2x2 minus bottom-left
            #   X X
            #   . X
            candidate = [(r, c), (r, c + 1), (r + 1, c + 1)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Tromino Rotation 3: top-left 2x2 minus top-right
            #   X .
            #   X X
            candidate = [(r, c), (r + 1, c), (r + 1, c + 1)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Tromino Rotation 4: top-left 2x2 minus top-left
            #   . X
            #   X X
            # Anchor is (r, c) which maps to the top-right cell.
            # The other cells are (r+1, c-1) and (r+1, c).
            candidate = [(r, c), (r + 1, c - 1), (r + 1, c)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            return shapes

        def backtrack() -> None:
            """
            Core backtracking function.

            At each call:
            1. Find the first uncovered cell.
            2. If none exists, the board is fully tiled — increment count.
            3. Otherwise, try every valid tile placement that covers that cell.
               For each placement, mark the cells, recurse, then unmark (backtrack).
            """
            # -----------------------------------------------------------------------
            # Base case: if there's no empty cell, we have a complete tiling.
            # -----------------------------------------------------------------------
            pos = find_first_empty()
            if pos is None:
                count[0] += 1
                return

            r, c = pos

            # -----------------------------------------------------------------------
            # Recursive case: try all tile shapes anchored at (r, c).
            # Because (r, c) is the FIRST empty cell, any valid tile must cover it.
            # This ensures we don't skip cells and don't double-count arrangements.
            # -----------------------------------------------------------------------
            shapes = get_tile_shapes(r, c)

            for shape in shapes:
                # Assign the current label to this tile
                label = tile_label[0]
                tile_label[0] += 1  # Increment for the next tile

                # Place the tile on the board
                place_tile(shape, label)

                # Recurse to fill the rest of the board
                backtrack()

                # Backtrack: remove the tile and restore the label counter
                remove_tile(shape)
                tile_label[0] -= 1  # Restore label counter

        # -----------------------------------------------------------------------
        # Start the backtracking from an empty board.
        # -----------------------------------------------------------------------
        backtrack()

        return count[0]

    def tile_board_with_tilings(self, m: int, n: int) -> List[List[List[int]]]:
        """
        Return all distinct tilings of an m x n board using dominoes and trominoes.

        Each tiling is a 2D grid where cells covered by the same tile share
        the same positive integer label.

        Args:
            m: Number of rows in the board.
            n: Number of columns in the board.

        Returns:
            List of all valid tilings, each represented as a 2D list of ints.

        Time Complexity: O(T^(m*n)) in the worst case.
        Space Complexity: O(m*n * S) where S is the number of solutions stored.
        """
        import copy

        board: List[List[int]] = [[0] * n for _ in range(m)]
        results: List[List[List[int]]] = []
        tile_label: List[int] = [1]

        def find_first_empty() -> Optional[Tuple[int, int]]:
            for r in range(m):
                for c in range(n):
                    if board[r][c] == 0:
                        return (r, c)
            return None

        def in_bounds(r: int, c: int) -> bool:
            return 0 <= r < m and 0 <= c < n

        def all_empty(cells: List[Tuple[int, int]]) -> bool:
            return all(board[r][c] == 0 for (r, c) in cells)

        def get_tile_shapes(r: int, c: int) -> List[List[Tuple[int, int]]]:
            shapes: List[List[Tuple[int, int]]] = []

            # Horizontal domino
            candidate = [(r, c), (r, c + 1)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Vertical domino
            candidate = [(r, c), (r + 1, c)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Tromino Rotation 1: X X / X .
            candidate = [(r, c), (r, c + 1), (r + 1, c)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Tromino Rotation 2: X X / . X
            candidate = [(r, c), (r, c + 1), (r + 1, c + 1)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Tromino Rotation 3: X . / X X
            candidate = [(r, c), (r + 1, c), (r + 1, c + 1)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            # Tromino Rotation 4: . X / X X
            candidate = [(r, c), (r + 1, c - 1), (r + 1, c)]
            if all(in_bounds(row, col) for row, col in candidate) and all_empty(candidate):
                shapes.append(candidate)

            return shapes

        def backtrack() -> None:
            pos = find_first_empty()
            if pos is None:
                results.append(copy.deepcopy(board))
                return

            r, c = pos
            shapes = get_tile_shapes(r, c)

            for shape in shapes:
                label = tile_label[0]
                tile_label[0] += 1

                for (row, col) in shape:
                    board[row][col] = label

                backtrack()

                for (row, col) in shape:
                    board[row][col] = 0
                tile_label[0] -= 1

        backtrack()
        return results


def print_board(board: List[List[int]]) -> None:
    """
    Pretty-print a tiling board.

    Args:
        board: 2D list of integers representing a tiling.
    """
    for row in board:
        print(" ".join(f"{cell:2d}" for cell in row))
    print()


if __name__ == "__main__":
    sol = Solution()

    # -----------------------------------------------------------------------
    # Example 1: m=2, n=2
    # Expected output: 2