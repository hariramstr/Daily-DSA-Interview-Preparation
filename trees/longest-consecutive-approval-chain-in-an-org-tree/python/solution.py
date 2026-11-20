"""
Title: Longest Consecutive Approval Chain in an Org Tree

Problem Description:
A company stores its approval hierarchy as a binary tree. Each node represents a manager
and contains an integer approval level. A valid approval chain is any path that moves only
between parent and child nodes, where every next node in the path has an approval level
exactly 1 greater than the previous node. The path must follow tree edges, but it does not
need to start at the root, and it may end at any node. You may only move downward from
parent to child while building the chain.

Your task is to return the length of the longest valid approval chain in the tree.

For example, if a manager with level 4 has a child with level 5, and that child has a child
with level 6, then these three nodes form a valid chain of length 3. If a child differs by
more than 1, less than 1, or is equal, the chain is broken and a new chain may start from
that child.

Implement a function that, given the root of the binary tree, computes the maximum chain length.

Constraints:
- The number of nodes in the tree is in the range [0, 100000].
- Node values are in the range [-1000000000, 1000000000].
- The tree is not necessarily balanced.
- An empty tree should return 0.

Example 1:
Input: root = [3,4,2,5,null,null,3,6]
Output: 4
Explanation: The longest downward consecutive chain is 3 -> 4 -> 5 -> 6, so the answer is 4.

Example 2:
Input: root = [10,11,9,12,10,null,null,13]
Output: 4
Explanation: The longest valid chain is 10 -> 11 -> 12 -> 13. The node with value 10 under 11
breaks the chain because it is not 12.

Expected:
- A solution that runs in O(n) time, where n is the number of nodes in the tree.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class TreeNode:
    """
    Basic binary tree node.

    Args:
        val: Integer value stored in the node.
        left: Left child.
        right: Right child.
    """

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
    def longestConsecutive(self, root: Optional[TreeNode]) -> int:
        """
        Compute the length of the longest downward parent-to-child consecutive chain.

        A chain is valid when each next child has value exactly parent_value + 1.
        The chain may start at any node and end at any node, but it must move only
        downward along tree edges.

        Args:
            root: Root of the binary tree.

        Returns:
            The maximum length of any valid consecutive downward chain.
            Returns 0 for an empty tree.

        Time complexity:
            O(n), because every node is processed exactly once.

        Space complexity:
            O(h) for the explicit stack in the average recursive-depth sense, but here
            implemented iteratively, so worst-case O(n) in an unbalanced tree due to
            the stack. This avoids Python recursion depth issues for large trees.
        """
        # If the tree is empty, there are no nodes and therefore no valid chain.
        # The problem explicitly states that an empty tree should return 0.
        if root is None:
            return 0

        # We will use an iterative depth-first traversal instead of recursion.
        #
        # Why iterative?
        # - The tree can contain up to 100000 nodes.
        # - A recursive DFS on a highly unbalanced tree could exceed Python's recursion limit.
        # - An explicit stack is safer and still gives us O(n) time.
        #
        # What do we store in the stack?
        # For each node, we need:
        # 1. The node itself
        # 2. The length of the current valid consecutive chain ending at this node
        #
        # Example:
        # If we are at node value 12 and arrived from parent value 11, then if 12 == 11 + 1,
        # the chain length becomes parent_chain_length + 1.
        # Otherwise, the chain is broken and a new chain starts at this node with length 1.
        stack: List[Tuple[TreeNode, int]] = [(root, 1)]

        # This variable tracks the best answer seen anywhere in the tree.
        # We initialize it to 1 because root exists, so at minimum there is a chain
        # consisting of the root alone.
        longest_chain: int = 1

        # Standard iterative DFS loop:
        # Repeatedly pop one node, process it, and push its children with the correct
        # chain lengths.
        while stack:
            node, current_length = stack.pop()

            # Update the global best answer using the chain length that ends at this node.
            # This is important because the longest chain may end at any node, not necessarily
            # at a leaf.
            if current_length > longest_chain:
                longest_chain = current_length

            # Process the left child, if it exists.
            if node.left is not None:
                # If the left child continues the consecutive pattern exactly by +1,
                # then we extend the current chain.
                if node.left.val == node.val + 1:
                    left_length: int = current_length + 1
                else:
                    # Otherwise the chain is broken.
                    # A new valid chain can always start from the child itself,
                    # so its chain length becomes 1.
                    left_length = 1

                # Push the child and its computed chain length onto the stack
                # so it can be processed later.
                stack.append((node.left, left_length))

            # Process the right child using the exact same logic.
            if node.right is not None:
                if node.right.val == node.val + 1:
                    right_length: int = current_length + 1
                else:
                    right_length = 1

                stack.append((node.right, right_length))

        # After visiting all nodes, longest_chain contains the maximum valid chain length.
        return longest_chain


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common convention:
    - values[i] is the node value or None
    - children are assigned left-to-right level by level

    Example:
        [3, 4, 2, 5, None, None, 3, 6]

    Args:
        values: Level-order list with integers or None.

    Returns:
        The root of the constructed binary tree, or None if the list is empty
        or starts with None.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), due to the queue and created nodes.
    """
    # If the input list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the root does not exist, so the tree is empty.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(values[0])

    # We use a queue to assign children level by level.
    # This mirrors the level-order representation naturally.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the list that has not yet been used.
    index: int = 1

    # Continue until we either run out of nodes to attach children to
    # or run out of input values.
    while queue and index < len(values):
        current: TreeNode = queue.popleft()

        # Attempt to assign the left child.
        if index < len(values):
            left_value: Optional[int] = values[index]
            index += 1

            # Only create a node if the value is not None.
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Attempt to assign the right child.
        if index < len(values):
            right_value: Optional[int] = values[index]
            index += 1

            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Tree from level order: [3,4,2,5,None,None,3,6]
    #
    # Structure:
    #         3
    #        / \
    #       4   2
    #      /     \
    #     5       3
    #    /
    #   6
    #
    # Longest valid downward consecutive chain:
    # 3 -> 4 -> 5 -> 6
    # Length = 4
    example_1: List[Optional[int]] = [3, 4, 2, 5, None, None, 3, 6]
    root_1: Optional[TreeNode] = build_tree_from_level_order(example_1)
    result_1: int = solution.longestConsecutive(root_1)
    print("Example 1 result:", result_1)  # Expected: 4

    # Example 2:
    # Tree from level order: [10,11,9,12,10,None,None,13]
    #
    # Structure:
    #         10
    #        /  \
    #      11    9
    #     /  \
    #   12   10
    #  /
    # 13
    #
    # Longest valid downward consecutive chain:
    # 10 -> 11 -> 12 -> 13
    # Length = 4
    #
    # The node 10 under 11 breaks the chain because 10 != 12.
    example_2: List[Optional[int]] = [10, 11, 9, 12, 10, None, None, 13]
    root_2: Optional[TreeNode] = build_tree_from_level_order(example_2)
    result_2: int = solution.longestConsecutive(root_2)
    print("Example 2 result:", result_2)  # Expected: 4

    # Additional quick sanity checks for beginners:
    # Empty tree -> 0
    empty_root: Optional[TreeNode] = build_tree_from_level_order([])
    print("Empty tree result:", solution.longestConsecutive(empty_root))  # Expected: 0

    # Single node tree -> 1
    single_root: Optional[TreeNode] = build_tree_from_level_order([42])
    print("Single node result:", solution.longestConsecutive(single_root))  # Expected: 1