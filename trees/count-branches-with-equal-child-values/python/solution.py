"""
Title: Count Branches With Equal Child Values

Problem Description:
You are given the root of a binary tree representing a simple rule hierarchy.
Each node stores an integer value. A node is called a balanced branch if it has
both a left child and a right child, and the values of those two children are
exactly the same. The value of the current node does not matter for this check.

Your task is to return the number of balanced branch nodes in the tree.

This is a structural tree traversal problem. You should inspect every node once
and count how many nodes satisfy the condition. Nodes with only one child or no
children are not counted.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are in the range [-1000, 1000].
- The tree is a binary tree.

Examples:
1) root = [8,4,4,3,3,null,3]
   Output: 2

2) root = [5,2,7,2,null,7,7]
   Output: 1
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node.

    Args:
        val: Integer value stored in the node.
        left: Left child node.
        right: Right child node.
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
    def countBalancedBranches(self, root: Optional[TreeNode]) -> int:
        """Count nodes whose left and right child both exist and have equal values.

        This method performs a depth-first traversal of the binary tree.
        At each node, it checks whether:
        1. A left child exists
        2. A right child exists
        3. The two child values are equal

        If all three conditions are true, that node is counted as a balanced branch.

        Args:
            root: The root node of the binary tree.

        Returns:
            The number of balanced branch nodes in the tree.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is visited exactly once.

        Space complexity:
            O(h), where h is the height of the tree due to recursion stack usage.
            In the worst case of a skewed tree, this can be O(n).
        """

        def dfs(node: Optional[TreeNode]) -> int:
            """Recursively traverse the tree and count balanced branches.

            Args:
                node: The current node being processed.

            Returns:
                The number of balanced branch nodes in the subtree rooted at node.

            Time complexity:
                O(n) across the full traversal, since each node is processed once.

            Space complexity:
                O(h) for recursion stack, where h is the tree height.
            """
            # Base case:
            # If the current node is None, there is no tree/subtree here,
            # so there are zero balanced branches to count.
            if node is None:
                return 0

            # Start with zero for the current node.
            # We will add 1 if this node itself satisfies the condition.
            count_for_current_node = 0

            # A node can only be a balanced branch if it has BOTH children.
            # Nodes with only one child or no children are explicitly excluded.
            if node.left is not None and node.right is not None:
                # Compare the values stored in the left and right child nodes.
                # The current node's own value does not matter.
                if node.left.val == node.right.val:
                    count_for_current_node = 1

            # Recursively count valid nodes in the left subtree.
            left_count = dfs(node.left)

            # Recursively count valid nodes in the right subtree.
            right_count = dfs(node.right)

            # Total for this subtree is:
            # - 1 or 0 from the current node
            # - plus everything found in the left subtree
            # - plus everything found in the right subtree
            return count_for_current_node + left_count + right_count

        # Start DFS from the root and return the total count.
        return dfs(root)


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """Build a binary tree from a level-order list representation.

    This helper interprets the input list the same way many coding platforms do:
    - values[i] is the node value at that position in level order
    - None means there is no node at that position

    Example:
        [8, 4, 4, 3, 3, None, 3]

    Args:
        values: Level-order representation of the binary tree.

    Returns:
        The root of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), due to the queue used while constructing the tree.
    """
    # If the list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the tree is empty by definition.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(values[0])

    # Use a queue to assign children level by level.
    # This matches the level-order structure of the input list.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the list that has not yet been used.
    index = 1

    # Continue while there are parent nodes waiting for children
    # and there are still values left in the input list.
    while queue and index < len(values):
        # Take the next parent node from the queue.
        current = queue.popleft()

        # Try to assign the left child if a value is available.
        if index < len(values):
            left_value = values[index]
            index += 1

            # Only create a node if the value is not None.
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Try to assign the right child if a value is available.
        if index < len(values):
            right_value = values[index]
            index += 1

            # Only create a node if the value is not None.
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Tree from level order: [8,4,4,3,3,None,3]
    #
    # Structure:
    #         8
    #       /   \
    #      4     4
    #     / \     \
    #    3   3     3
    #
    # Check each node:
    # - Node 8: left child = 4, right child = 4 -> equal -> count it
    # - Left node 4: left child = 3, right child = 3 -> equal -> count it
    # - Right node 4: only one child -> do not count
    # - Leaf nodes: no children -> do not count
    #
    # Expected answer: 2
    example_1 = [8, 4, 4, 3, 3, None, 3]
    root_1 = build_tree_from_level_order(example_1)
    result_1 = solution.countBalancedBranches(root_1)
    print("Example 1 Output:", result_1)

    # Example 2:
    # Tree from level order: [5,2,7,2,None,7,7]
    #
    # Structure:
    #         5
    #       /   \
    #      2     7
    #     /     / \
    #    2     7   7
    #
    # Check each node:
    # - Node 5: children are 2 and 7 -> not equal -> do not count
    # - Node 2: only one child -> do not count
    # - Node 7: children are 7 and 7 -> equal -> count it
    # - Leaf nodes: no children -> do not count
    #
    # Expected answer: 1
    example_2 = [5, 2, 7, 2, None, 7, 7]
    root_2 = build_tree_from_level_order(example_2)
    result_2 = solution.countBalancedBranches(root_2)
    print("Example 2 Output:", result_2)

    # Additional simple checks for beginners:
    # Empty tree -> no nodes -> answer should be 0
    example_3: List[Optional[int]] = []
    root_3 = build_tree_from_level_order(example_3)
    result_3 = solution.countBalancedBranches(root_3)
    print("Empty Tree Output:", result_3)

    # Single node tree -> no children -> answer should be 0
    example_4 = [10]
    root_4 = build_tree_from_level_order(example_4)
    result_4 = solution.countBalancedBranches(root_4)
    print("Single Node Output:", result_4)