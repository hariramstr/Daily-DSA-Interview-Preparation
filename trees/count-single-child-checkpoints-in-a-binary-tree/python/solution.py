"""
Title: Count Single-Child Checkpoints in a Binary Tree

Problem Description:
You are given the root of a binary tree representing checkpoints in a monitoring system.
Each node stores an integer checkpoint ID. A checkpoint is called a single-child
checkpoint if it has exactly one direct child: either a left child or a right child,
but not both.

Your task is to return the number of single-child checkpoints in the tree.

This is a structural tree problem: the checkpoint values themselves do not affect the
answer. You only need to examine whether each node has zero, one, or two children.
A leaf node has no children, so it does not count. A node with both left and right
children also does not count.

The tree can be empty. In that case, the answer is 0.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are in the range [-10^4, 10^4].
- The tree is a standard binary tree and is not necessarily balanced or complete.

Example 1:
Input: root = [5,3,8,1,null,null,9]
Output: 2
Explanation:
- Node 3 has only a left child (1), so it counts.
- Node 8 has only a right child (9), so it counts.
- Node 5 has two children, so it does not count.
- Nodes 1 and 9 are leaves, so they do not count.
Total = 2

Example 2:
Input: root = [10,4,12,2,6,null,null]
Output: 0
Explanation:
- Node 10 has two children, so it does not count.
- Node 4 has two children, so it does not count.
- Nodes 2, 6, and 12 are leaves, so they do not count.
Total = 0
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node used for the checkpoint tree."""

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        """
        Initialize a binary tree node.

        Args:
            val: Integer value stored in the node.
            left: Reference to the left child.
            right: Reference to the right child.

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
    def count_single_child_checkpoints(self, root: Optional[TreeNode]) -> int:
        """
        Count how many nodes in the binary tree have exactly one child.

        This method performs a depth-first traversal of the tree. For every node,
        it checks whether exactly one of its child references is present. If yes,
        that node is counted as a single-child checkpoint.

        Args:
            root: The root node of the binary tree.

        Returns:
            The number of nodes that have exactly one direct child.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is visited exactly once.

        Space complexity:
            O(h), where h is the height of the tree due to recursion stack usage.
            In the worst case of a skewed tree, this becomes O(n).
        """

        def dfs(node: Optional[TreeNode]) -> int:
            """
            Recursively count single-child nodes in the subtree rooted at `node`.

            Args:
                node: Current subtree root.

            Returns:
                Number of single-child nodes in this subtree.

            Time complexity:
                O(k), where k is the number of nodes in this subtree.

            Space complexity:
                O(h), where h is the subtree height because of recursion.
            """
            # Base case:
            # If the current node is None, there is no actual tree node here.
            # That means there is nothing to count, so we return 0.
            if node is None:
                return 0

            # Determine whether the current node has exactly one child.
            #
            # We use the XOR-style logic:
            # - If left exists and right does not, count this node.
            # - If right exists and left does not, count this node.
            # - If both exist, do not count.
            # - If neither exists (leaf), do not count.
            #
            # In Python, "is not None" makes the intent very clear and beginner-friendly.
            has_left: bool = node.left is not None
            has_right: bool = node.right is not None

            # Exactly one child means one side is True and the other is False.
            current_count: int = 1 if has_left != has_right else 0

            # Recursively count in the left subtree.
            # This explores every node reachable through the left child.
            left_count: int = dfs(node.left)

            # Recursively count in the right subtree.
            # This explores every node reachable through the right child.
            right_count: int = dfs(node.right)

            # The total for this subtree is:
            # count from current node + count from left subtree + count from right subtree
            return current_count + left_count + right_count

        # Start the recursive depth-first search from the root.
        return dfs(root)

    def build_tree_from_level_order(self, values: List[Optional[int]]) -> Optional[TreeNode]:
        """
        Build a binary tree from a level-order list representation.

        The input format follows the common interview/LeetCode style:
        - values[i] is the node value or None
        - children are assigned level by level from left to right

        Args:
            values: List of node values in level-order, using None for missing nodes.

        Returns:
            The root of the constructed binary tree, or None if the list is empty
            or starts with None.

        Time complexity:
            O(n), where n is the number of entries in the input list.

        Space complexity:
            O(n), due to the queue used during tree construction.
        """
        # If the input list is empty, there is no tree to build.
        if not values:
            return None

        # If the first value is None, the tree is empty by definition.
        if values[0] is None:
            return None

        # Create the root node from the first value.
        root = TreeNode(values[0])

        # We use a queue to process parent nodes in level-order.
        # This matches the structure of the input list.
        queue: Deque[TreeNode] = deque([root])

        # Index points to the next value in the list that has not yet been used.
        index: int = 1

        # Continue while there are parent nodes waiting in the queue
        # and there are still values left to process.
        while queue and index < len(values):
            # Take the next parent node from the front of the queue.
            current = queue.popleft()

            # Try to assign the left child if there is still input available.
            if index < len(values):
                left_value = values[index]
                index += 1

                # Only create a node when the value is not None.
                # None means that child is missing.
                if left_value is not None:
                    current.left = TreeNode(left_value)
                    queue.append(current.left)

            # Try to assign the right child if there is still input available.
            if index < len(values):
                right_value = values[index]
                index += 1

                # Again, only create a node for a real value.
                if right_value is not None:
                    current.right = TreeNode(right_value)
                    queue.append(current.right)

        return root


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Tree from level-order: [5, 3, 8, 1, None, None, 9]
    #
    # Structure:
    #         5
    #       /   \
    #      3     8
    #     /       \
    #    1         9
    #
    # Node 3 has exactly one child -> count 1
    # Node 8 has exactly one child -> count 1
    # Total expected = 2
    example_1_values: List[Optional[int]] = [5, 3, 8, 1, None, None, 9]
    example_1_root = solution.build_tree_from_level_order(example_1_values)
    example_1_result = solution.count_single_child_checkpoints(example_1_root)
    print("Example 1 Output:", example_1_result)  # Expected: 2

    # Example 2:
    # Tree from level-order: [10, 4, 12, 2, 6, None, None]
    #
    # Structure:
    #         10
    #        /  \
    #       4    12
    #      / \
    #     2   6
    #
    # Node 10 has two children -> does not count
    # Node 4 has two children -> does not count
    # Nodes 2, 6, 12 are leaves -> do not count
    # Total expected = 0
    example_2_values: List[Optional[int]] = [10, 4, 12, 2, 6, None, None]
    example_2_root = solution.build_tree_from_level_order(example_2_values)
    example_2_result = solution.count_single_child_checkpoints(example_2_root)
    print("Example 2 Output:", example_2_result)  # Expected: 0

    # Additional simple check: empty tree
    empty_values: List[Optional[int]] = []
    empty_root = solution.build_tree_from_level_order(empty_values)
    empty_result = solution.count_single_child_checkpoints(empty_root)
    print("Empty Tree Output:", empty_result)  # Expected: 0