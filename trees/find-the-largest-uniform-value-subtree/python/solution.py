"""
Title: Find the Largest Uniform-Value Subtree

Problem Description:
You are given the root of a binary tree where each node stores an integer category code.
A subtree is called uniform if every node inside that subtree has the same value.
A subtree may consist of a single node, which is always considered uniform.

Return the size (number of nodes) of the largest uniform subtree in the tree.

Your task is to examine every possible subtree rooted at each node and determine whether
all nodes below that root, including the root itself, share the same value. If they do,
that subtree is uniform and contributes its total node count as a candidate answer.
If not, you must continue checking smaller subtrees.

This problem is intended to test recursive tree reasoning and bottom-up aggregation of
information from child subtrees. An efficient solution should avoid re-checking the same
nodes many times.

Constraints:
- The number of nodes in the tree is in the range [1, 10^5].
- Node values are in the range [-10^9, 10^9].
- The tree is binary, and may be unbalanced.
- Your solution should run in O(n) time.

Example 1:
Input: root = [5,5,5,5,5,null,5]
Output: 6
Explanation: Every existing node has value 5, so the entire tree is a uniform subtree with 6 nodes.

Example 2:
Input: root = [3,3,4,3,null,4,4]
Output: 3
Explanation: The subtree rooted at the right child of the root contains three nodes, all with value 4,
so its size is 3. The whole tree is not uniform because it contains both 3 and 4.
"""

from __future__ import annotations

from collections import deque
from dataclasses import dataclass
from typing import Deque, List, Optional, Tuple
import sys


@dataclass
class TreeNode:
    """Binary tree node."""

    val: int
    left: Optional["TreeNode"] = None
    right: Optional["TreeNode"] = None


class Solution:
    def largest_uniform_subtree(self, root: Optional[TreeNode]) -> int:
        """
        Compute the size of the largest subtree in which every node has the same value.

        This method performs a single bottom-up traversal. For each node, it determines:
        1. Whether the subtree rooted at that node is uniform.
        2. The total number of nodes in that subtree.

        If both children are uniform and, when present, both child values match the current
        node's value, then the current subtree is also uniform.

        Args:
            root: The root of the binary tree.

        Returns:
            The number of nodes in the largest uniform-value subtree.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node is processed once.

        Space complexity:
            O(h) for recursion stack, where h is the height of the tree.
            In the worst case of a skewed tree, this can be O(n).
        """
        # If the tree is empty, there is no subtree to count.
        # The problem constraints say at least 1 node exists, but handling this case
        # makes the method more robust and reusable.
        if root is None:
            return 0

        # This variable stores the best answer found anywhere in the tree.
        # We update it whenever we discover a uniform subtree larger than all previous ones.
        self.best_size: int = 0

        def dfs(node: Optional[TreeNode]) -> Tuple[bool, int]:
            """
            Recursively analyze a subtree and return whether it is uniform and its size.

            The return value is a tuple:
            - is_uniform: True if every node in this subtree has the same value
            - size: total number of nodes in this subtree

            The key idea is bottom-up aggregation:
            - First solve the left subtree
            - Then solve the right subtree
            - Then use those answers to determine the status of the current node's subtree

            Args:
                node: Current subtree root.

            Returns:
                A tuple (is_uniform, size).

            Time complexity:
                O(1) work per node besides recursive calls, so O(n) overall across the tree.

            Space complexity:
                O(h) recursion stack, where h is tree height.
            """
            # An empty subtree is treated as uniform with size 0.
            # Why?
            # Because it should not block a parent from being uniform.
            # For example, a leaf node with no children should count as a uniform subtree of size 1.
            if node is None:
                return True, 0

            # Recursively gather information from the left child.
            # After this call:
            # - left_uniform tells us whether the left subtree is uniform
            # - left_size tells us how many nodes are in the left subtree
            left_uniform, left_size = dfs(node.left)

            # Recursively gather information from the right child.
            right_uniform, right_size = dfs(node.right)

            # Total size of the current subtree is:
            # current node itself + all nodes in left subtree + all nodes in right subtree
            subtree_size: int = 1 + left_size + right_size

            # Start by assuming the current subtree can be uniform.
            # We will invalidate this assumption if any required condition fails.
            is_uniform_subtree: bool = True

            # Condition 1:
            # The left subtree itself must already be uniform.
            # If it contains mixed values, then the current subtree cannot possibly be uniform.
            if not left_uniform:
                is_uniform_subtree = False

            # Condition 2:
            # The right subtree itself must already be uniform.
            if not right_uniform:
                is_uniform_subtree = False

            # Condition 3:
            # If a left child exists, its value must match the current node's value.
            # Because if the left subtree is uniform but all its nodes are, say, 7,
            # and the current node is 5, then the combined subtree is not uniform.
            if node.left is not None and node.left.val != node.val:
                is_uniform_subtree = False

            # Condition 4:
            # If a right child exists, its value must also match the current node's value.
            if node.right is not None and node.right.val != node.val:
                is_uniform_subtree = False

            # If all checks passed, then the subtree rooted at this node is uniform.
            # Its size becomes a candidate answer.
            if is_uniform_subtree:
                # Update the global best answer if this uniform subtree is larger.
                if subtree_size > self.best_size:
                    self.best_size = subtree_size

            # Return the computed information to the parent.
            return is_uniform_subtree, subtree_size

        # Run the depth-first search starting from the root.
        dfs(root)

        # After processing the whole tree, best_size contains the largest uniform subtree size.
        return self.best_size


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common interview/LeetCode style:
    - Each list element represents a node value in breadth-first order.
    - None means that position has no node.

    Example:
        [5, 5, 5, 5, 5, None, 5]

    Args:
        level_order: List of node values in level-order, using None for missing nodes.

    Returns:
        The root of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), due to the queue used during construction.
    """
    # If the input list is empty, there is no tree to build.
    if not level_order:
        return None

    # If the first value is None, the tree is empty.
    if level_order[0] is None:
        return None

    # Create the root node from the first element.
    root = TreeNode(level_order[0])

    # Queue for breadth-first construction.
    # Each node pulled from the queue will receive up to two children
    # from the remaining values in the list.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list that has not yet been used.
    index = 1

    # Continue until we either run out of nodes to attach children to
    # or run out of input values.
    while queue and index < len(level_order):
        current = queue.popleft()

        # Try to assign the left child if there is still input remaining.
        if index < len(level_order):
            left_value = level_order[index]
            index += 1

            # Only create a node when the value is not None.
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Try to assign the right child if there is still input remaining.
        if index < len(level_order):
            right_value = level_order[index]
            index += 1

            # Only create a node when the value is not None.
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Increase recursion limit because the tree can be very deep (up to 10^5 nodes in a skewed tree).
    # Python's default recursion limit is often around 1000, which may not be enough.
    sys.setrecursionlimit(300000)

    solution = Solution()

    # Example 1:
    # Tree: [5,5,5,5,5,None,5]
    # All existing nodes have value 5.
    # Therefore, the entire tree is uniform and contains 6 nodes.
    example_1 = [5, 5, 5, 5, 5, None, 5]
    root_1 = build_tree(example_1)
    result_1 = solution.largest_uniform_subtree(root_1)
    print("Example 1 Result:", result_1)  # Expected: 6

    # Example 2:
    # Tree: [3,3,4,3,None,4,4]
    # The largest uniform subtree is the right subtree rooted at value 4,
    # containing three nodes all equal to 4.
    example_2 = [3, 3, 4, 3, None, 4, 4]
    root_2 = build_tree(example_2)
    result_2 = solution.largest_uniform_subtree(root_2)
    print("Example 2 Result:", result_2)  # Expected: 3

    # Additional small sanity check:
    # Single node tree is always a uniform subtree of size 1.
    example_3 = [42]
    root_3 = build_tree(example_3)
    result_3 = solution.largest_uniform_subtree(root_3)
    print("Example 3 Result:", result_3)  # Expected: 1