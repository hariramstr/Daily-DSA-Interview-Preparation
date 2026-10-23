"""
Title: Find the First Unbalanced Review Folder

Problem Description:
You are given the root of a binary tree representing a document review folder structure.
Each node stores an integer number of pending files in that folder.

A folder is considered balanced if the total number of pending files in its left subtree
is exactly equal to the total number of pending files in its right subtree.
If a child does not exist, its subtree sum is 0.

Your task is to return the value stored in the first unbalanced folder found during a
level-order traversal (top to bottom, left to right). If every folder is balanced,
return -1.

The value of a folder is not guaranteed to be unique, so you should return the node's
stored integer value exactly as it appears when the first unbalanced node is encountered
in level order.

Constraints:
- The number of nodes in the tree is in the range [1, 1000].
- Node values are in the range [0, 10^4].
- The tree is a binary tree and may be incomplete.

Examples:
1) root = [8,4,4,2,2,2,2]
   Output: -1

2) root = [10,3,5,1,null,2,1]
   Output: 10
"""

from __future__ import annotations

from collections import deque
from typing import Deque, Dict, List, Optional


class TreeNode:
    """Binary tree node used for the folder structure."""

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        self.val: int = val
        self.left: Optional["TreeNode"] = left
        self.right: Optional["TreeNode"] = right


class Solution:
    def _compute_subtree_sums(
        self,
        node: Optional[TreeNode],
        subtree_sum: Dict[TreeNode, int],
    ) -> int:
        """
        Recursively compute and store the total sum of each node's entire subtree.

        The subtree sum of a node means:
        node.val + sum(left subtree) + sum(right subtree)

        We store the result in a dictionary so that later, during level-order traversal,
        we can instantly look up the sum of the left subtree and right subtree for any node.

        Args:
            node: Current tree node being processed.
            subtree_sum: Dictionary mapping each node object to its full subtree sum.

        Returns:
            The full subtree sum for the current node. If node is None, returns 0.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node is visited once.

        Space complexity:
            O(n), for the dictionary plus recursion stack in the worst case.
        """
        # Base case:
        # If the current node does not exist, its subtree contributes 0 to the sum.
        if node is None:
            return 0

        # Recursively compute the total sum of the left subtree.
        left_total: int = self._compute_subtree_sums(node.left, subtree_sum)

        # Recursively compute the total sum of the right subtree.
        right_total: int = self._compute_subtree_sums(node.right, subtree_sum)

        # The full subtree sum rooted at this node includes:
        # - the current node's own value
        # - everything in the left subtree
        # - everything in the right subtree
        total: int = node.val + left_total + right_total

        # Store the computed total so we can reuse it later in O(1) time.
        subtree_sum[node] = total

        return total

    def first_unbalanced_folder(self, root: Optional[TreeNode]) -> int:
        """
        Return the value of the first unbalanced node found in level-order traversal.

        A node is unbalanced when:
        sum(values in left subtree) != sum(values in right subtree)

        The algorithm works in two phases:
        1. Compute subtree sums for every node using post-order DFS.
        2. Traverse the tree in level order (BFS) and return the first node whose
           left subtree sum and right subtree sum are different.

        Args:
            root: Root of the binary tree.

        Returns:
            The integer value stored in the first unbalanced node in level-order traversal.
            Returns -1 if all nodes are balanced.

        Time complexity:
            O(n), because we do one DFS and one BFS over the tree.

        Space complexity:
            O(n), for the subtree sum dictionary and BFS queue.
        """
        # Defensive check:
        # The constraints say there is at least 1 node, but handling None makes the
        # method more robust and easier to reuse.
        if root is None:
            return -1

        # This dictionary will map:
        #   node object -> total sum of the subtree rooted at that node
        #
        # Why use a dictionary?
        # Because after we compute subtree sums once, we want to answer:
        #   "What is the sum of this node's left subtree?"
        #   "What is the sum of this node's right subtree?"
        # instantly during BFS.
        subtree_sum: Dict[TreeNode, int] = {}

        # Phase 1: Compute subtree sums for all nodes.
        self._compute_subtree_sums(root, subtree_sum)

        # Phase 2: Perform a standard breadth-first search (level-order traversal).
        #
        # We use a queue because BFS processes nodes in the exact order required:
        # top to bottom, left to right.
        queue: Deque[TreeNode] = deque([root])

        while queue:
            # Remove the next node in level-order.
            current: TreeNode = queue.popleft()

            # If a child is missing, its subtree sum is defined as 0.
            # Otherwise, look up the already-computed subtree sum from the dictionary.
            left_sum: int = subtree_sum[current.left] if current.left is not None else 0
            right_sum: int = subtree_sum[current.right] if current.right is not None else 0

            # This is the exact balance condition from the problem statement.
            # If the sums differ, this is the first unbalanced node encountered in BFS,
            # so we must return its stored value immediately.
            if left_sum != right_sum:
                return current.val

            # Continue BFS in left-to-right order.
            # This preserves the required traversal order.
            if current.left is not None:
                queue.append(current.left)
            if current.right is not None:
                queue.append(current.right)

        # If we finish BFS without finding any unbalanced node,
        # then every node in the tree is balanced.
        return -1


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from the usual level-order array representation.

    Example:
        [10, 3, 5, 1, None, 2, 1]

    Means:
              10
             /  \
            3    5
           /    / \
          1    2   1

    Args:
        values: List of node values in level-order, where None means missing child.

    Returns:
        The root TreeNode of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the number of entries in the input list.

    Space complexity:
        O(n), for the queue and created nodes.
    """
    # If the input list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the tree is empty.
    if values[0] is None:
        return None

    # Create the root node from the first element.
    root: TreeNode = TreeNode(values[0])

    # Queue of nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list to consume.
    index: int = 1

    # Process nodes in BFS order while there are still values left to assign.
    while queue and index < len(values):
        current: TreeNode = queue.popleft()

        # Assign left child if there is a value available.
        if index < len(values):
            left_value: Optional[int] = values[index]
            index += 1

            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if there is a value available.
        if index < len(values):
            right_value: Optional[int] = values[index]
            index += 1

            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Tree: [8,4,4,2,2,2,2]
    #
    # Structure:
    #         8
    #       /   \
    #      4     4
    #     / \   / \
    #    2  2  2  2
    #
    # Left subtree sum of root = 4 + 2 + 2 = 8
    # Right subtree sum of root = 4 + 2 + 2 = 8
    # Every internal node also has equal left/right subtree sums.
    # Expected output: -1
    example_1: List[Optional[int]] = [8, 4, 4, 2, 2, 2, 2]
    root_1: Optional[TreeNode] = build_tree_from_level_order(example_1)
    result_1: int = solution.first_unbalanced_folder(root_1)
    print(result_1)  # Expected: -1

    # Example 2:
    # Tree: [10,3,5,1,None,2,1]
    #
    # Structure:
    #         10
    #        /  \
    #       3    5
    #      /    / \
    #     1    2   1
    #
    # Root left subtree sum = 3 + 1 = 4
    # Root right subtree sum = 5 + 2 + 1 = 8
    # Since 4 != 8, root is unbalanced and is visited first in level order.
    # Expected output: 10
    example_2: List[Optional[int]] = [10, 3, 5, 1, None, 2, 1]
    root_2: Optional[TreeNode] = build_tree_from_level_order(example_2)
    result_2: int = solution.first_unbalanced_folder(root_2)
    print(result_2)  # Expected: 10

    # Additional quick sanity check:
    # Single node tree:
    # A leaf has left subtree sum = 0 and right subtree sum = 0, so it is balanced.
    # Expected output: -1
    example_3: List[Optional[int]] = [7]
    root_3: Optional[TreeNode] = build_tree_from_level_order(example_3)
    result_3: int = solution.first_unbalanced_folder(root_3)
    print(result_3)  # Expected: -1