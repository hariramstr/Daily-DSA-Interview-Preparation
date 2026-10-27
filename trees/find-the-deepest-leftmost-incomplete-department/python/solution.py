"""
Title: Find the Deepest Leftmost Incomplete Department

Problem Description:
A company models its department structure as a binary tree. Each node represents a
department and stores a unique integer department ID. The left child is the first
sub-department and the right child is the second sub-department. A department is
considered incomplete if it has exactly one direct sub-department (either left or
right, but not both).

Your task is to return the department ID of the incomplete department that appears
at the greatest depth in the tree. If multiple incomplete departments exist at the
same deepest depth, return the leftmost one among them. The root is at depth 0,
its children are at depth 1, and so on.

If there is no incomplete department in the tree, return -1.

Constraints:
- The number of nodes in the tree is in the range [0, 100000].
- Node values are unique integers in the range [-1000000000, 1000000000].
- The input tree is a binary tree and may be empty.

Examples:
1) root = [10,5,20,3,null,15,30,null,4]
   Output: 3

2) root = [8,4,12,2,6,null,14,1]
   Output: 2
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class TreeNode:
    """Binary tree node.

    Attributes:
        val: Integer value stored in the node.
        left: Reference to the left child.
        right: Reference to the right child.
    """

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        self.val = val
        self.left = left
        self.right = right


class Solution:
    def deepest_leftmost_incomplete_department(self, root: Optional[TreeNode]) -> int:
        """Return the deepest leftmost incomplete node's value.

        An incomplete node is a node with exactly one child.
        Among all incomplete nodes, we choose:
        1. The one with the greatest depth.
        2. If there is a tie in depth, the leftmost one.

        We use a level-order traversal (BFS), because BFS naturally processes nodes
        from left to right within each depth level. That means when we scan a level,
        the first incomplete node we encounter at that level is automatically the
        leftmost incomplete node for that depth.

        Args:
            root: Root of the binary tree.

        Returns:
            The value of the deepest leftmost incomplete node, or -1 if none exists.

        Time complexity:
            O(n), where n is the number of nodes in the tree.

        Space complexity:
            O(w), where w is the maximum width of the tree. In the worst case, O(n).
        """
        # If the tree is empty, there are no nodes at all, so there cannot be any
        # incomplete node. The required answer in that case is -1.
        if root is None:
            return -1

        # We perform a standard breadth-first search using a queue.
        # Each queue entry stores:
        #   - the current node
        #   - the depth of that node
        #
        # Why BFS?
        # - We must prioritize deeper nodes over shallower ones.
        # - For nodes at the same depth, we must choose the leftmost one.
        # - BFS visits nodes level by level, and within each level from left to right
        #   if we enqueue left child before right child.
        queue: Deque[Tuple[TreeNode, int]] = deque([(root, 0)])

        # These variables track the best answer found so far.
        #
        # best_depth:
        #   The deepest depth at which we have seen an incomplete node.
        #
        # best_value:
        #   The node value corresponding to the best candidate.
        #
        # We initialize best_depth to -1 so that any real node depth (0 or more)
        # will be deeper than this initial value.
        best_depth: int = -1
        best_value: int = -1

        # Process nodes in BFS order.
        while queue:
            node, depth = queue.popleft()

            # A node is incomplete if exactly one child exists.
            #
            # We use XOR-like logic:
            # - (node.left is None) != (node.right is None)
            # This is True exactly when one side is missing and the other exists.
            is_incomplete: bool = (node.left is None) != (node.right is None)

            # If this node is incomplete, it is a candidate answer.
            if is_incomplete:
                # If this node is deeper than the best incomplete node seen so far,
                # it becomes the new best answer immediately.
                if depth > best_depth:
                    best_depth = depth
                    best_value = node.val

                # If depth == best_depth, we do NOT update.
                #
                # Why?
                # Because BFS processes nodes from left to right within the same level.
                # Therefore, the first incomplete node encountered at a given depth is
                # the leftmost incomplete node at that depth. If we updated on ties,
                # we would incorrectly replace the leftmost candidate with a later one.
                #
                # So for equal depth, we intentionally keep the existing best_value.

            # Enqueue children for the next level.
            #
            # Important detail:
            # We enqueue left child before right child.
            # This preserves left-to-right order within each level during BFS,
            # which is exactly what we need for the "leftmost" tie-break rule.
            if node.left is not None:
                queue.append((node.left, depth + 1))
            if node.right is not None:
                queue.append((node.right, depth + 1))

        # If no incomplete node was ever found, best_value remains -1.
        return best_value


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """Build a binary tree from a level-order list representation.

    The input uses None to represent missing children.

    Example:
        [10, 5, 20, 3, None, 15, 30, None, 4]

    Args:
        level_order: List of node values in level-order, with None for missing nodes.

    Returns:
        The root of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the length of the input list.

    Space complexity:
        O(n), due to the queue and created nodes.
    """
    # An empty list means there is no tree.
    if not level_order:
        return None

    # If the first value is None, the root itself is missing, so the tree is empty.
    if level_order[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(level_order[0])

    # Queue used to assign children level by level.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list that has not yet been used.
    index: int = 1

    # Continue until we either run out of parent nodes or run out of input values.
    while queue and index < len(level_order):
        current = queue.popleft()

        # Assign left child if a value is available.
        if index < len(level_order):
            left_value = level_order[index]
            index += 1

            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if a value is available.
        if index < len(level_order):
            right_value = level_order[index]
            index += 1

            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Tree from level-order:
    # [10, 5, 20, 3, None, 15, 30, None, 4]
    #
    # Structure:
    #         10
    #        /  \
    #       5    20
    #      /    /  \
    #     3    15  30
    #      \
    #       4
    #
    # Incomplete nodes:
    # - 5 has only left child -> depth 1
    # - 3 has only right child -> depth 2
    #
    # Deepest incomplete node is 3.
    example_1 = [10, 5, 20, 3, None, 15, 30, None, 4]
    root_1 = build_tree(example_1)
    print(solution.deepest_leftmost_incomplete_department(root_1))  # Expected: 3

    # Example 2:
    # Tree from level-order:
    # [8, 4, 12, 2, 6, None, 14, 1]
    #
    # Structure:
    #         8
    #       /   \
    #      4     12
    #     / \      \
    #    2   6      14
    #   /
    #  1
    #
    # Incomplete nodes:
    # - 12 has only right child -> depth 1
    # - 2 has only left child -> depth 2
    #
    # Deepest incomplete node is 2.
    example_2 = [8, 4, 12, None, None, 2, 6, None, 14, 1]
    # The problem statement's textual explanation corresponds to the intended tree:
    # [8, 4, 12, 2, 6, None, 14, 1]
    # We build exactly that intended tree below.
    example_2 = [8, 4, 12, 2, 6, None, 14, 1]
    root_2 = build_tree(example_2)
    print(solution.deepest_leftmost_incomplete_department(root_2))  # Expected: 2

    # Additional quick checks:
    # Empty tree -> no incomplete node.
    root_3 = build_tree([])
    print(solution.deepest_leftmost_incomplete_department(root_3))  # Expected: -1

    # Perfect small tree -> no incomplete node.
    root_4 = build_tree([1, 2, 3])
    print(solution.deepest_leftmost_incomplete_department(root_4))  # Expected: -1

    # Tie at same deepest depth:
    #         1
    #       /   \
    #      2     3
    #     /       \
    #    4         5
    #
    # Incomplete nodes:
    # - 2 at depth 1
    # - 3 at depth 1
    # Leftmost at deepest depth 1 is 2.
    root_5 = build_tree([1, 2, 3, 4, None, None, 5])
    print(solution.deepest_leftmost_incomplete_department(root_5))  # Expected: 2