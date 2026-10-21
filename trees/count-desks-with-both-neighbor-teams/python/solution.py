"""
Title: Count Desks with Both Neighbor Teams

Problem Description:
A company stores its office layout as a binary tree. Each node represents a desk and
contains an integer team ID. The left and right child pointers represent the two
neighboring desks directly connected to that desk in the layout map.

A desk is called a mixed-neighbor desk if:
1. It has both a left child and a right child, and
2. The team ID of the current desk is different from both children.

In other words, for a desk with value x, left child value a, and right child value b,
the desk should be counted only if:
- x != a
- x != b

Return the number of mixed-neighbor desks in the tree.

This is a simple tree traversal problem. If the tree is empty, the answer is 0.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are integers in the range [-10^4, 10^4].
- The tree is a binary tree and is not necessarily complete or balanced.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node used to represent each desk in the office layout."""

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        """
        Initialize a tree node.

        Args:
            val: Integer value stored in the node.
            left: Left child node.
            right: Right child node.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.val = val
        self.left = left
        self.right = right


class Solution:
    def count_mixed_neighbor_desks(self, root: Optional[TreeNode]) -> int:
        """
        Count how many nodes have exactly two children and have a value different
        from both child values.

        This method uses depth-first search (DFS) recursion to visit every node
        exactly once and check whether it satisfies the required condition.

        Args:
            root: The root of the binary tree.

        Returns:
            The number of mixed-neighbor desks in the tree.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is visited once.

        Space complexity:
            O(h), where h is the height of the tree due to recursion stack usage.
            In the worst case of a skewed tree, this can be O(n).
        """

        # If the tree is empty, there are no desks at all, so the answer must be 0.
        if root is None:
            return 0

        def dfs(node: Optional[TreeNode]) -> int:
            """
            Recursively traverse the tree and count qualifying nodes.

            Args:
                node: The current node being processed.

            Returns:
                The count of mixed-neighbor desks in the subtree rooted at `node`.

            Time complexity:
                O(k), where k is the number of nodes in this subtree.

            Space complexity:
                O(h), where h is the height of this subtree due to recursion.
            """

            # Base case:
            # If the current node does not exist, there is nothing to count.
            if node is None:
                return 0

            # Start with 0 for the current node.
            # We will change this to 1 only if the current node satisfies
            # the exact condition from the problem.
            current_count: int = 0

            # A node can only qualify if it has BOTH left and right children.
            # If one child is missing, it should not be counted.
            if node.left is not None and node.right is not None:
                # Check the value condition:
                # - current node's value must be different from left child's value
                # - current node's value must be different from right child's value
                #
                # Note:
                # The problem does NOT require the two children to be different
                # from each other. It only requires the parent to be different
                # from each child.
                if node.val != node.left.val and node.val != node.right.val:
                    current_count = 1

            # Recursively count qualifying nodes in the left subtree.
            left_count: int = dfs(node.left)

            # Recursively count qualifying nodes in the right subtree.
            right_count: int = dfs(node.right)

            # The total for this subtree is:
            # count from current node + count from left subtree + count from right subtree
            return current_count + left_count + right_count

        # Start DFS from the root and return the final total.
        return dfs(root)


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common convention:
    - Each element represents a node value in breadth-first order.
    - None means that position has no node.

    Example:
        [7, 2, 9, 1, 2, None, 10]

    Args:
        level_order: List of node values in level-order form.

    Returns:
        The root node of the constructed binary tree, or None if the list is empty
        or starts with None.

    Time complexity:
        O(n), where n is the number of elements in the input list.

    Space complexity:
        O(n), due to the queue used while building the tree.
    """

    # If the input list is empty, there is no tree to build.
    if not level_order:
        return None

    # If the first value is None, the root does not exist, so the tree is empty.
    if level_order[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(level_order[0])

    # Use a queue to attach children level by level.
    # This matches the level-order structure of the input list.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level_order list that has not yet
    # been processed as a child.
    index: int = 1

    # Continue until we either run out of nodes to expand or run out of input values.
    while queue and index < len(level_order):
        # Take the next parent node from the queue.
        current = queue.popleft()

        # Process the left child if there is still an input value available.
        if index < len(level_order):
            left_value = level_order[index]
            index += 1

            # Only create a child node if the value is not None.
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Process the right child if there is still an input value available.
        if index < len(level_order):
            right_value = level_order[index]
            index += 1

            # Only create a child node if the value is not None.
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # Input: [5, 3, 8, 3, 4, 8, 9]
    #
    # Manual verification:
    # - Node 5 has children 3 and 8 -> 5 != 3 and 5 != 8 -> counted
    # - Node 3 has children 3 and 4 -> left child matches parent -> not counted
    # - Node 8 has children 8 and 9 -> left child matches parent -> not counted
    # - Leaves are not counted because they do not have two children
    # Correct output: 1
    tree1 = build_tree([5, 3, 8, 3, 4, 8, 9])
    result1 = solution.count_mixed_neighbor_desks(tree1)
    print("Example 1 Output:", result1)  # Expected: 1

    # Example 2 from the prompt:
    # Input: [7, 2, 9, 1, 2, None, 10]
    #
    # Manual verification:
    # - Node 7 has children 2 and 9 -> both different from 7 -> counted
    # - Node 2 has children 1 and 2 -> right child matches parent -> not counted
    # - Node 9 has only one child -> not counted
    # Correct output: 1
    tree2 = build_tree([7, 2, 9, 1, 2, None, 10])
    result2 = solution.count_mixed_neighbor_desks(tree2)
    print("Example 2 Output:", result2)  # Expected: 1

    # Additional simple test: empty tree
    tree3 = build_tree([])
    result3 = solution.count_mixed_neighbor_desks(tree3)
    print("Empty Tree Output:", result3)  # Expected: 0

    # Additional test: one node only
    tree4 = build_tree([42])
    result4 = solution.count_mixed_neighbor_desks(tree4)
    print("Single Node Output:", result4)  # Expected: 0

    # Additional test: root qualifies, children are leaves
    tree5 = build_tree([1, 2, 3])
    result5 = solution.count_mixed_neighbor_desks(tree5)
    print("Simple Qualifying Root Output:", result5)  # Expected: 1