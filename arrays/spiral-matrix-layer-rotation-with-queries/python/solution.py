"""
Spiral Matrix Layer Rotation with Queries

Problem Description:
You are given an n x m matrix of integers. The matrix is composed of concentric
rectangular layers, where layer 0 is the outermost ring, layer 1 is the next ring
inward, and so on. You are also given a list of queries, where each query is of the
form [layer, k, direction], meaning: rotate all elements in the specified layer by k
positions in the given direction ('L' for left/clockwise, 'R' for right/counter-clockwise).

After applying all queries in order, return the resulting matrix.

A layer's elements are traversed in clockwise order starting from the top-left corner
of that layer. Rotating left by k means each element moves k positions forward in the
clockwise traversal order. Rotating right by k means each element moves k positions backward.

Note: Multiple queries may target the same layer, and k can be larger than the number
of elements in the layer.

Constraints:
- 1 <= n, m <= 300
- 1 <= queries.length <= 10^4
- 0 <= layer < min(n, m) / 2
- 1 <= k <= 10^9
- direction is either 'L' or 'R'
"""

from typing import List


class Solution:
    def extract_layer(self, matrix: List[List[int]], layer: int) -> List[int]:
        """
        Extract all elements of a given layer in clockwise order.

        Traversal order:
          1. Top row: left to right
          2. Right column: top+1 to bottom
          3. Bottom row: right-1 to left (if bottom != top)
          4. Left column: bottom-1 to top+1 (if left != right)

        Args:
            matrix: The n x m integer matrix.
            layer: The layer index (0 = outermost).

        Returns:
            A list of integers representing the layer elements in clockwise order.

        Time complexity: O(n + m) for a single layer traversal.
        Space complexity: O(n + m) for the returned list.
        """
        n = len(matrix)
        m = len(matrix[0])

        # Define the boundaries of this layer
        top = layer
        bottom = n - 1 - layer
        left = layer
        right = m - 1 - layer

        elements = []

        # --- Step 1: Traverse the top row from left to right ---
        for col in range(left, right + 1):
            elements.append(matrix[top][col])

        # --- Step 2: Traverse the right column from top+1 to bottom ---
        for row in range(top + 1, bottom + 1):
            elements.append(matrix[row][right])

        # --- Step 3: Traverse the bottom row from right-1 to left ---
        # Only if bottom != top (i.e., the layer has more than one row)
        if bottom != top:
            for col in range(right - 1, left - 1, -1):
                elements.append(matrix[bottom][col])

        # --- Step 4: Traverse the left column from bottom-1 to top+1 ---
        # Only if left != right (i.e., the layer has more than one column)
        if left != right:
            for row in range(bottom - 1, top, -1):
                elements.append(matrix[row][left])

        return elements

    def place_layer(self, matrix: List[List[int]], layer: int, elements: List[int]) -> None:
        """
        Place a list of elements back into the matrix at the specified layer,
        following the same clockwise traversal order used in extract_layer.

        Args:
            matrix: The n x m integer matrix (modified in place).
            layer: The layer index (0 = outermost).
            elements: The list of integers to place back into the layer.

        Returns:
            None (modifies matrix in place).

        Time complexity: O(n + m) for a single layer placement.
        Space complexity: O(1) extra space (modifies in place).
        """
        n = len(matrix)
        m = len(matrix[0])

        # Define the boundaries of this layer (same as extract_layer)
        top = layer
        bottom = n - 1 - layer
        left = layer
        right = m - 1 - layer

        # Use an index to track our position in the elements list
        idx = 0

        # --- Step 1: Place elements along the top row ---
        for col in range(left, right + 1):
            matrix[top][col] = elements[idx]
            idx += 1

        # --- Step 2: Place elements along the right column ---
        for row in range(top + 1, bottom + 1):
            matrix[row][right] = elements[idx]
            idx += 1

        # --- Step 3: Place elements along the bottom row (right to left) ---
        if bottom != top:
            for col in range(right - 1, left - 1, -1):
                matrix[bottom][col] = elements[idx]
                idx += 1

        # --- Step 4: Place elements along the left column (bottom to top) ---
        if left != right:
            for row in range(bottom - 1, top, -1):
                matrix[row][left] = elements[idx]
                idx += 1

    def rotate_layer(self, elements: List[int], k: int, direction: str) -> List[int]:
        """
        Rotate a list of elements by k positions in the given direction.

        'L' (left/clockwise): elements shift forward in the list.
          e.g., [1,2,3,4,5] rotated left by 2 -> [3,4,5,1,2]
          This is equivalent to a standard left rotation.

        'R' (right/counter-clockwise): elements shift backward in the list.
          e.g., [1,2,3,4,5] rotated right by 2 -> [4,5,1,2,3]
          This is equivalent to a standard right rotation.

        Args:
            elements: The list of layer elements in clockwise order.
            k: Number of positions to rotate.
            direction: 'L' for left rotation, 'R' for right rotation.

        Returns:
            A new list with elements rotated appropriately.

        Time complexity: O(L) where L is the length of elements.
        Space complexity: O(L) for the new list.
        """
        length = len(elements)

        # Edge case: if the layer has 0 or 1 elements, no rotation needed
        if length <= 1:
            return elements

        # Normalize k to avoid unnecessary full rotations
        # k % length gives the effective rotation amount
        k = k % length

        if direction == 'L':
            # Left rotation by k: take elements[k:] + elements[:k]
            # Example: [1,2,3,4,5], k=2 -> [3,4,5,1,2]
            return elements[k:] + elements[:k]
        else:
            # Right rotation by k: take elements[length-k:] + elements[:length-k]
            # Example: [1,2,3,4,5], k=2 -> [4,5,1,2,3]
            return elements[length - k:] + elements[:length - k]

    def spiralRotate(
        self,
        matrix: List[List[int]],
        queries: List[List]
    ) -> List[List[int]]:
        """
        Apply a series of layer rotation queries to the matrix and return the result.

        Algorithm Overview:
          For each query [layer, k, direction]:
            1. Extract the elements of the specified layer in clockwise order.
            2. Rotate those elements by k positions in the given direction.
            3. Place the rotated elements back into the matrix.

        Args:
            matrix: An n x m matrix of integers.
            queries: A list of queries, each of the form [layer, k, direction].

        Returns:
            The matrix after all queries have been applied.

        Time complexity: O(Q * (n + m)) where Q is the number of queries,
                         n is the number of rows, m is the number of columns.
        Space complexity: O(n + m) for storing layer elements during each query.
        """
        # --- Process each query one by one ---
        for query in queries:
            # Unpack the query components
            layer = query[0]
            k = query[1]
            direction = query[2]

            # --- Step A: Extract the layer elements in clockwise order ---
            # This gives us a flat list we can easily rotate
            elements = self.extract_layer(matrix, layer)

            # --- Step B: Rotate the elements ---
            # 'L' = left (clockwise) rotation
            # 'R' = right (counter-clockwise) rotation
            rotated = self.rotate_layer(elements, k, direction)

            # --- Step C: Place the rotated elements back into the matrix ---
            # This updates the matrix in place
            self.place_layer(matrix, layer, rotated)

        # Return the modified matrix
        return matrix


# ─────────────────────────────────────────────
# Verification / Tracing
# ─────────────────────────────────────────────
# Example 1:
#   matrix = [[1,2,3],[4,5,6],[7,8,9]], queries = [[0,1,'L']]
#   Layer 0 clockwise: [1,2,3,6,9,8,7,4]
#   Rotate left by 1:  [2,3,6,9,8,7,4,1]
#   Place back:
#     Top row (left->right):   matrix[0][0]=2, matrix[0][1]=3, matrix[0][2]=6
#     Right col (top+1->bot):  matrix[1][2]=9
#     Bot row (right-1->left): matrix[2][2]=8, matrix[2][1]=7, matrix[2][0]=4
#     Left col (bot-1->top+1): matrix[1][0]=1
#   Result: [[2,3,6],[1,5,9],[4,7,8]]  ✓
#
# Example 2:
#   matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12],[13,14,15,16]]
#   Query 1: layer=0, k=2, direction='R'
#     Layer 0 clockwise: [1,2,3,4,8,12,16,15,14,13,9,5]  (12 elements)
#     Rotate right by 2: elements[10:] + elements[:10] = [9,5,1,2,3,4,8,12,16,15,14,13]
#     Place back:
#       Top row:    matrix[0] = [9,5,1,2]
#       Right col:  matrix[1][3]=3, matrix[2][3]=4, matrix[3][3]=8  -- wait let me redo
#
#   Let me carefully re-extract layer 0 for a 4x4 matrix:
#     top=0, bottom=3, left=0, right=3
#     Top row (col 0..3):       1, 2, 3, 4
#     Right col (row 1..3):     8, 12, 16
#     Bottom row (col 2..0):    15, 14, 13
#     Left col (row 2..1):      9, 5
#     Full: [1,2,3,4,8,12,16,15,14,13,9,5]  (12 elements)
#
#   Rotate right by 2: length=12, k=2
#     elements[10:] + elements[:10] = [9,5] + [1,2,3,4,8,12,16,15,14,13]
#     = [9,5,1,2,3,4,8,12,16,15,14,13]
#
#   Place back:
#     Top row (col 0..3):       matrix[0] = [9,5,1,2]
#     Right col (row 1..3):     matrix[1][3]=3, matrix[2][3]=4, matrix[3][3]=8
#     Bottom row (col 2..0):    matrix[3][2]=12, matrix[3][1]=16, matrix[3][0]=15
#     Left col (row 2..1):      matrix[2][0]=14, matrix[1][0]=13
#
#   Matrix after query 1:
#     [[ 9,  5,  1,  2],
#      [13,  6,  7,  3],
#      [14, 10, 11,  4],
#      [15, 16, 12,  8]]
#
#   Query 2: layer=1, k=1, direction='L'
#     top=1, bottom=2, left=1, right=2
#     Top row (col 1..2):       6, 7
#     Right col (row 2..2):     11
#     Bottom row (col 1..1):    10
#     Left col: (bottom-1=1 > top=1 is False, so nothing)
#     Full: [6,7,11,10]  (4 elements)
#
#   Rotate left by 1: [7,11,10,6]
#
#   Place back:
#     Top row (col 1..2):       matrix[1][1]=7, matrix[1][2]=11
#     Right col (row 2..2):     matrix[2][2]=10
#     Bottom row (col 1..1):    matrix[2][1]=6
#
#   Matrix after query 2:
#     [[ 9,  5,  1,  2],
#      [13,  7, 11,  3],
#      [14,  6, 10,  4],
#      [15, 16, 12,  8]]
#
#   Expected: [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
#
#   Hmm, that doesn't match. Let me re-read the problem.
#   Expected output: [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
#
#   Let me re-check query 1: layer=0, k=2, direction='R'
#   The expected output after both queries is [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
#
#   Let me work backwards from the expected final answer to verify.
#   After query 2 (layer=1, k=1, 'L'), the inner layer [1][1],[1][2],[2][2],[2][1]
#   should be [11,6,10,7] -> rotated left by 1 -> [6,10,7,11]... hmm.
#
#   Wait, let me re-read: the expected output is [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
#   Inner layer positions: [1][1]=11, [1][2]=6, [2][2]=10, [2][1]=7
#   Clockwise order: top-row=[11,6], right-col=[10], bottom-row=[7], left-col=[]
#   So inner layer before query 2 = [11,6,10,7]
#   After rotating left by 1: [6,10,7,11]
#   Place back: [1][1]=6, [1][2]=10, [2][2]=7, [2][1]=11  -- doesn't match expected
#
#   Hmm. Let me look at the expected output more carefully.
#   Expected: [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
#   Inner 2x2 (positions [1][1],[1][2],[2][1],[2][2]): 11,6,7,10
#
#   If before query 2 the inner layer was [6,7,11,10] (my calculation above),
#   rotating left by 1 gives [7,11,