"""
Title: Count Managers Above Team Budget

Problem Description:
You are given the root of a binary tree representing a company budget hierarchy.
Each node stores an integer `val`, which is the budget assigned to that manager
or employee.

For every non-leaf node, define its immediate team budget as the sum of the
values of its existing left and right children.

A manager is considered over budget if their own value is strictly greater than
the immediate team budget of their direct children.

Your task is to return the number of nodes in the tree that are over budget.
Leaf nodes are not counted, because they do not manage any direct reports.

A missing child contributes nothing to the team budget. For example, if a node
has only a left child with value 4, then its immediate team budget is 4.

Constraints:
- The number of nodes in the tree is in the range [1, 1000]
- -1000 <= Node.val <= 1000
- The tree is a binary tree

Examples:
1) root = [10, 4, 3, 1, 2]
   Output: 2

2) root = [5, 7, 1, null, 2]
   Output: 1
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node used for the budget hierarchy."""

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
    def count_over_budget_managers(self, root: Optional[TreeNode]) -> int:
        """
        Count how many non-leaf nodes have a value strictly greater than the
        sum of their direct children's values.

        Args:
            root: The root node of the binary tree.

        Returns:
            The number of non-leaf nodes that are over budget.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is visited exactly once.

        Space complexity:
            O(h) for the recursion stack, where h is the height of the tree.
            In the worst case of a skewed tree, this becomes O(n).
        """

        # If the tree is empty, there are no managers to evaluate.
        # The problem constraints say there is at least one node, but handling
        # None makes the method more robust and reusable.
        if root is None:
            return 0

        def dfs(node: Optional[TreeNode]) -> int:
            """
            Perform a depth-first traversal and count qualifying managers.

            Args:
                node: The current node being processed.

            Returns:
                The number of over-budget managers in the subtree rooted at node.

            Time complexity:
                O(n) across the full traversal.

            Space complexity:
                O(h) due to recursion depth.
            """

            # Base case:
            # If there is no node, there is nothing to count.
            if node is None:
                return 0

            # Recursively count qualifying nodes in the left subtree.
            # We do this first so that by the time we finish this function,
            # the total includes every relevant node below the current one.
            left_count: int = dfs(node.left)

            # Recursively count qualifying nodes in the right subtree.
            right_count: int = dfs(node.right)

            # A leaf node has no children, so it is not considered a manager.
            # The problem explicitly says leaf nodes must not be counted.
            if node.left is None and node.right is None:
                return left_count + right_count

            # Compute the immediate team budget.
            # A missing child contributes 0, exactly as the problem states.
            left_value: int = node.left.val if node.left is not None else 0
            right_value: int = node.right.val if node.right is not None else 0
            child_sum: int = left_value + right_value

            # Check whether the current non-leaf node is over budget.
            # The condition is STRICTLY greater than the sum of direct children.
            current_node_counts: int = 1 if node.val > child_sum else 0

            # Combine:
            # - count from left subtree
            # - count from right subtree
            # - possibly 1 for the current node
            return left_count + right_count + current_node_counts

        return dfs(root)


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    This helper is useful for testing because many coding platforms describe
    trees using arrays like [10, 4, 3, 1, 2].

    Args:
        values: A list of node values in level-order, where None means
            a missing node.

    Returns:
        The root of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), due to the queue used during construction.
    """

    # If the input list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the tree is empty.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(values[0])

    # We use a queue to process nodes level by level.
    # This matches the level-order structure of the input list.
    queue: Deque[TreeNode] = deque([root])

    # Start reading children from index 1 because index 0 was used for the root.
    index: int = 1

    # Continue until we either run out of nodes to attach children to
    # or run out of values in the input list.
    while queue and index < len(values):
        current: TreeNode = queue.popleft()

        # Try to assign the left child if there is still input remaining.
        if index < len(values):
            left_value: Optional[int] = values[index]
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)
            index += 1

        # Try to assign the right child if there is still input remaining.
        if index < len(values):
            right_value: Optional[int] = values[index]
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)
            index += 1

    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Tree: [10, 4, 3, 1, 2]
    #
    # Structure:
    #         10
    #        /  \
    #       4    3
    #      / \
    #     1   2
    #
    # Check each non-leaf:
    # - Node 10: child sum = 4 + 3 = 7, and 10 > 7 -> counts
    # - Node 4: child sum = 1 + 2 = 3, and 4 > 3 -> counts
    # - Node 3 is a leaf -> does not count
    # - Node 1 is a leaf -> does not count
    # - Node 2 is a leaf -> does not count
    #
    # Expected answer: 2
    example_1: List[Optional[int]] = [10, 4, 3, 1, 2]
    root_1: Optional[TreeNode] = build_tree_from_level_order(example_1)
    result_1: int = solution.count_over_budget_managers(root_1)
    print("Example 1 Output:", result_1)

    # Example 2:
    # Tree: [5, 7, 1, None, 2]
    #
    # Structure:
    #         5
    #        / \
    #       7   1
    #        \
    #         2
    #
    # Check each non-leaf:
    # - Node 5: child sum = 7 + 1 = 8, and 5 > 8 -> no
    # - Node 7: child sum = 0 + 2 = 2, and 7 > 2 -> counts
    # - Node 1 is a leaf -> does not count
    # - Node 2 is a leaf -> does not count
    #
    # Expected answer: 1
    example_2: List[Optional[int]] = [5, 7, 1, None, 2]
    root_2: Optional[TreeNode] = build_tree_from_level_order(example_2)
    result_2: int = solution.count_over_budget_managers(root_2)
    print("Example 2 Output:", result_2)

    # Additional simple test:
    # Single-node tree. The root is a leaf, so the answer must be 0.
    example_3: List[Optional[int]] = [42]
    root_3: Optional[TreeNode] = build_tree_from_level_order(example_3)
    result_3: int = solution.count_over_budget_managers(root_3)
    print("Example 3 Output:", result_3)