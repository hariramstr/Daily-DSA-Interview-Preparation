"""
Sum of Left Boundary Nodes in Binary Tree

Problem Description:
Given the root of a binary tree, return the sum of all node values that lie on the left boundary
of the tree.

The left boundary is defined as the path from the root down to the leftmost leaf node,
following only left children when available, and right children only when no left child exists.
The root itself is always included, but leaf nodes should NOT be included in the sum
(only the non-leaf nodes along the path).

A leaf node is a node with no children.

Constraints:
- The number of nodes in the tree is in the range [1, 100].
- -100 <= Node.val <= 100
"""

from typing import Optional


# Definition for a binary tree node
class TreeNode:
    def __init__(self, val: int = 0, left: 'Optional[TreeNode]' = None,
                 right: 'Optional[TreeNode]' = None):
        self.val = val
        self.left = left
        self.right = right


class Solution:
    def sumOfLeftBoundary(self, root: Optional[TreeNode]) -> int:
        """
        Calculate the sum of all non-leaf nodes on the left boundary of a binary tree.

        The left boundary starts at the root and follows left children preferentially,
        falling back to right children when no left child exists. Leaf nodes are excluded.

        Args:
            root: The root node of the binary tree.

        Returns:
            int: The sum of all non-leaf node values along the left boundary.

        Time Complexity: O(H) where H is the height of the tree, since we traverse
                         only the left boundary path from root to the leftmost leaf.
        Space Complexity: O(1) extra space (not counting the call stack), since we
                          use an iterative approach with just a pointer variable.
        """

        # -----------------------------------------------------------------------
        # EDGE CASE: If the tree is empty, there are no nodes to sum.
        # Return 0 immediately.
        # -----------------------------------------------------------------------
        if root is None:
            return 0

        # -----------------------------------------------------------------------
        # HELPER FUNCTION: Determine if a node is a leaf node.
        # A leaf node has no left child AND no right child.
        # We need this check because the problem says leaf nodes should NOT
        # be included in the sum.
        # -----------------------------------------------------------------------
        def is_leaf(node: TreeNode) -> bool:
            """Return True if node has no children (is a leaf)."""
            return node.left is None and node.right is None

        # -----------------------------------------------------------------------
        # INITIALIZE the sum and the current pointer.
        # We start traversal from the root node.
        # The root is always included (unless it's also a leaf, in which case
        # it's excluded per the problem rules).
        # -----------------------------------------------------------------------
        total_sum = 0
        current = root  # Start at the root of the tree

        # -----------------------------------------------------------------------
        # TRAVERSE the left boundary from root down to the leftmost leaf.
        #
        # At each step:
        #   1. Check if the current node is a leaf — if so, STOP (don't add it).
        #   2. If it's NOT a leaf, add its value to the sum.
        #   3. Move to the next node:
        #      - Prefer the LEFT child (go left if available)
        #      - Fall back to the RIGHT child if no left child exists
        #
        # This loop naturally handles the "left boundary" definition:
        # we always try to go left first, and only go right when forced.
        # -----------------------------------------------------------------------
        while current is not None:
            # Check if the current node is a leaf node
            if is_leaf(current):
                # Leaf nodes are EXCLUDED from the sum — stop traversal here
                break

            # Current node is NOT a leaf, so include its value in the sum
            total_sum += current.val

            # Decide which child to visit next:
            # Priority 1: Go LEFT if a left child exists
            if current.left is not None:
                current = current.left
            # Priority 2: Go RIGHT only if there is NO left child
            else:
                current = current.right

        # -----------------------------------------------------------------------
        # RETURN the accumulated sum of all non-leaf left boundary nodes.
        # -----------------------------------------------------------------------
        return total_sum


# -------------------------------------------------------------------------------
# HELPER FUNCTION: Build a binary tree from a level-order list representation.
# None values in the list represent missing nodes.
# This makes it easy to construct test cases from the problem examples.
# -------------------------------------------------------------------------------
def build_tree(values: list) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order (BFS) list of values.

    Args:
        values: List of node values in level-order. None means no node at that position.

    Returns:
        The root TreeNode of the constructed tree, or None if the list is empty.
    """
    if not values or values[0] is None:
        return None

    # Create the root node from the first element
    root = TreeNode(values[0])

    # Use a queue to keep track of nodes that still need children assigned
    from collections import deque
    queue = deque([root])

    # Index into the values list — start at index 1 (after the root)
    i = 1

    while queue and i < len(values):
        # Take the next node from the queue that needs children
        node = queue.popleft()

        # Assign LEFT child if the next value exists and is not None
        if i < len(values):
            if values[i] is not None:
                node.left = TreeNode(values[i])
                queue.append(node.left)  # This node may also need children
            i += 1  # Move to the next value regardless

        # Assign RIGHT child if the next value exists and is not None
        if i < len(values):
            if values[i] is not None:
                node.right = TreeNode(values[i])
                queue.append(node.right)  # This node may also need children
            i += 1  # Move to the next value regardless

    return root


# -------------------------------------------------------------------------------
# MAIN BLOCK: Demonstrate the solution with the provided examples.
# We trace through each example to verify correctness.
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ===========================================================================
    # EXAMPLE 1:
    # Input: root = [1, 2, 3, 4, 5, null, null, 7]
    # Tree structure:
    #         1
    #        / \
    #       2   3
    #      / \
    #     4   5
    #    /
    #   7
    #
    # Left boundary path: 1 -> 2 -> 4 -> 7
    # Leaf node 7 is excluded.
    # Expected sum: 1 + 2 + 4 = 7
    # ===========================================================================
    print("=" * 60)
    print("EXAMPLE 1")
    print("=" * 60)

    # Build the tree: [1, 2, 3, 4, 5, null, null, 7]
    # Level 0: 1
    # Level 1: 2 (left of 1), 3 (right of 1)
    # Level 2: 4 (left of 2), 5 (right of 2), null (left of 3), null (right of 3)
    # Level 3: 7 (left of 4)
    values1 = [1, 2, 3, 4, 5, None, None, 7]
    root1 = build_tree(values1)

    result1 = solution.sumOfLeftBoundary(root1)
    print(f"Input tree (level-order): {values1}")
    print(f"Left boundary path: 1 -> 2 -> 4 -> 7 (leaf 7 excluded)")
    print(f"Expected output: 7")
    print(f"Actual output:   {result1}")
    print(f"PASS" if result1 == 7 else f"FAIL")
    print()

    # ===========================================================================
    # EXAMPLE 2:
    # Input: root = [10, 20, 30, 40, null, null, null]
    # Tree structure:
    #         10
    #        /  \
    #       20   30
    #      /
    #     40
    #
    # Left boundary path: 10 -> 20 -> 40
    # Leaf node 40 is excluded.
    # Expected sum: 10 + 20 = 30
    # ===========================================================================
    print("=" * 60)
    print("EXAMPLE 2")
    print("=" * 60)

    values2 = [10, 20, 30, 40, None, None, None]
    root2 = build_tree(values2)

    result2 = solution.sumOfLeftBoundary(root2)
    print(f"Input tree (level-order): {values2}")
    print(f"Left boundary path: 10 -> 20 -> 40 (leaf 40 excluded)")
    print(f"Expected output: 30")
    print(f"Actual output:   {result2}")
    print(f"PASS" if result2 == 30 else f"FAIL")
    print()

    # ===========================================================================
    # EDGE CASE 1: Single node tree (root is also a leaf)
    # The root is a leaf, so it should NOT be included.
    # Expected sum: 0
    # ===========================================================================
    print("=" * 60)
    print("EDGE CASE 1: Single node (root is a leaf)")
    print("=" * 60)

    root_single = TreeNode(5)
    result_single = solution.sumOfLeftBoundary(root_single)
    print(f"Input: single node with value 5")
    print(f"Root is a leaf, so it's excluded.")
    print(f"Expected output: 0")
    print(f"Actual output:   {result_single}")
    print(f"PASS" if result_single == 0 else f"FAIL")
    print()

    # ===========================================================================
    # EDGE CASE 2: Tree where left boundary must use right child
    # Tree:
    #     1
    #      \
    #       2
    #        \
    #         3  (leaf)
    #
    # Left boundary: 1 -> 2 -> 3 (leaf 3 excluded)
    # Expected sum: 1 + 2 = 3
    # ===========================================================================
    print("=" * 60)
    print("EDGE CASE 2: Left boundary uses right children")
    print("=" * 60)

    root_right = TreeNode(1)
    root_right.right = TreeNode(2)
    root_right.right.right = TreeNode(3)

    result_right = solution.sumOfLeftBoundary(root_right)
    print(f"Tree: 1 -> (right) 2 -> (right) 3 (leaf)")
    print(f"Left boundary: 1 -> 2 -> 3 (leaf 3 excluded)")
    print(f"Expected output: 3")
    print(f"Actual output:   {result_right}")
    print(f"PASS" if result_right == 3 else f"FAIL")
    print()

    # ===========================================================================
    # EDGE CASE 3: Empty tree
    # Expected sum: 0
    # ===========================================================================
    print("=" * 60)
    print("EDGE CASE 3: Empty tree (None root)")
    print("=" * 60)

    result_empty = solution.sumOfLeftBoundary(None)
    print(f"Input: None (empty tree)")
    print(f"Expected output: 0")
    print(f"Actual output:   {result_empty}")
    print(f"PASS" if result_empty == 0 else f"FAIL")