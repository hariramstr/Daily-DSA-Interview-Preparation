"""
Title: Find the Deepest Complete Team Subtree

Problem Description:
A company stores its reporting structure as a binary tree where each node represents a team lead.
A node may have a left direct report, a right direct report, both, or neither.

A subtree rooted at some node is called complete if every node in that subtree has either exactly
0 children or exactly 2 children. In other words, no node inside that subtree is allowed to have
only one child.

Given the root of a binary tree, return the value of the root node of the deepest complete subtree.
If multiple complete subtrees have the same maximum depth, return the one with the smallest node
value. The depth of a subtree is defined by the depth of its root in the original tree, where the
overall root has depth 0.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Node values are integers in the range [-1000000000, 1000000000].
- All node values are unique.
- The tree is not necessarily balanced.

Important correctness note:
A subtree is valid if every node inside that subtree has either 0 or 2 children.
That means:
- Every leaf is a valid complete subtree.
- Any internal node is a valid complete subtree root only if:
  1. It has both left and right children, and
  2. Both child subtrees are themselves complete.

Therefore, the task is:
- Find all roots of such valid subtrees.
- Choose the one with the greatest depth.
- If several are tied at that same depth, choose the smallest node value.

This interpretation makes Example 2 return 3, because the deepest valid subtree roots are
the leaves 3, 15, and 25 at depth 2, and the smallest value among them is 3.
"""

from __future__ import annotations

from collections import deque
from dataclasses import dataclass
from typing import Deque, List, Optional, Tuple
import sys


class TreeNode:
    """Binary tree node."""

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
            left: Left child.
            right: Right child.

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


@dataclass
class SubtreeInfo:
    """Stores whether a subtree is complete under the problem definition."""
    is_complete: bool


class Solution:
    def deepest_complete_subtree(self, root: Optional[TreeNode]) -> int:
        """
        Return the value of the root node of the deepest complete subtree.

        A subtree is complete here if every node in that subtree has either:
        - exactly 0 children, or
        - exactly 2 children.

        If multiple complete subtree roots appear at the same maximum depth,
        the smallest node value is returned.

        Args:
            root: Root of the binary tree.

        Returns:
            The integer value of the deepest complete subtree root.

        Time complexity:
            O(n), because each node is processed once.

        Space complexity:
            O(h) for recursion stack in a balanced tree on average,
            O(n) in the worst case for a highly skewed tree.
        """
        # The problem guarantees at least one node, but we still guard against None
        # to make the method robust and beginner-friendly.
        if root is None:
            raise ValueError("The tree must contain at least one node.")

        # Because the tree can contain up to 100000 nodes, Python's default recursion
        # limit may be too small for a very skewed tree. We increase it so that a deep
        # recursive DFS does not fail with RecursionError.
        sys.setrecursionlimit(300000)

        # These two variables track the best answer found so far while traversing.
        #
        # best_depth:
        #   The deepest depth at which we have found a valid complete subtree root.
        #
        # best_value:
        #   Among all valid roots at best_depth, we keep the smallest node value.
        #
        # We initialize best_depth to -1 so that even the actual root at depth 0
        # can replace it immediately if valid.
        best_depth: int = -1
        best_value: int = 0

        def dfs(node: Optional[TreeNode], depth: int) -> SubtreeInfo:
            """
            Perform a post-order traversal and determine whether each subtree is complete.

            We use post-order traversal because the validity of the current node's subtree
            depends on the validity of its left and right subtrees. So we must compute
            child information first, then decide for the current node.

            Args:
                node: Current tree node being processed.
                depth: Depth of the current node in the original tree.

            Returns:
                A SubtreeInfo object indicating whether the subtree rooted at this node
                is complete under the problem's definition.

            Time complexity:
                O(size of subtree), and across the whole tree this totals O(n).

            Space complexity:
                O(h) recursion stack, where h is the tree height.
            """
            nonlocal best_depth, best_value

            # Base case:
            # A missing node is not itself a subtree root we can choose, but for the purpose
            # of validating a parent with two children, we treat recursion on None as complete.
            #
            # Why?
            # Because when we are at a real node, we explicitly check whether it has:
            # - no children, or
            # - both children.
            #
            # So None is only a helper state in recursion, not a candidate answer.
            if node is None:
                return SubtreeInfo(is_complete=True)

            # Recursively process the left subtree first.
            left_info: SubtreeInfo = dfs(node.left, depth + 1)

            # Recursively process the right subtree next.
            right_info: SubtreeInfo = dfs(node.right, depth + 1)

            # Determine how many children the current node has.
            has_left: bool = node.left is not None
            has_right: bool = node.right is not None

            # A subtree rooted at the current node is complete if and only if:
            #
            # Case 1: The node is a leaf.
            #   - No children at all.
            #
            # Case 2: The node has exactly two children, and both child subtrees are complete.
            #
            # Any node with exactly one child immediately makes the subtree invalid.
            is_current_complete: bool = False

            if not has_left and not has_right:
                # Leaf nodes are valid complete subtrees by definition.
                is_current_complete = True
            elif has_left and has_right and left_info.is_complete and right_info.is_complete:
                # Internal node with exactly two children is valid only if both child
                # subtrees are also valid complete subtrees.
                is_current_complete = True
            else:
                # This covers the "exactly one child" case, or the case where one/both
                # child subtrees are not complete.
                is_current_complete = False

            # If the current subtree is complete, then the current node is a candidate answer.
            #
            # We update the global best answer using the problem's tie-breaking rules:
            # 1. Prefer greater depth.
            # 2. If depth ties, prefer smaller node value.
            if is_current_complete:
                if depth > best_depth:
                    best_depth = depth
                    best_value = node.val
                elif depth == best_depth:
                    best_value = min(best_value, node.val)

            # Return the computed status upward so the parent can decide whether its own
            # subtree is complete.
            return SubtreeInfo(is_complete=is_current_complete)

        # Start DFS from the root at depth 0.
        dfs(root, 0)

        # After the full traversal, best_value stores the correct answer.
        return best_value


def build_tree_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common interview style:
    - values[i] is the node value or None
    - children are assigned level by level from left to right

    Example:
        [8, 4, 10, 2, 6, 9, None, None, None, 5, 7]

    Args:
        values: Level-order list with integers or None.

    Returns:
        Root of the constructed binary tree.

    Time complexity:
        O(n), where n is the number of list entries.

    Space complexity:
        O(n), due to the queue and created nodes.
    """
    if not values:
        return None

    # If the first value is None, the tree is empty.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(values[0])

    # Queue stores nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list.
    index: int = 1

    # Process nodes in BFS order until we run out of input values.
    while queue and index < len(values):
        current: TreeNode = queue.popleft()

        # Assign left child if available.
        if index < len(values):
            left_value: Optional[int] = values[index]
            index += 1
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if available.
        if index < len(values):
            right_value: Optional[int] = values[index]
            index += 1
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create a Solution instance.
    solution = Solution()

    # Example 1 from the prompt:
    # Tree:
    #         8
    #       /   \
    #      4     10
    #     / \    /
    #    2   6  9
    #       / \
    #      5   7
    #
    # Valid complete subtree roots:
    # - Leaves: 2, 5, 7, 9
    # - Node 6 is also valid because it has exactly two children (5 and 7),
    #   and both are leaves.
    #
    # Depths:
    # - 2 is at depth 2
    # - 9 is at depth 2
    # - 6 is at depth 2
    # - 5 is at depth 3
    # - 7 is at depth 3
    #
    # The deepest valid roots are 5 and 7 at depth 3, so the smallest value is 5.
    #
    # This is the logically correct answer under the stated rules.
    example_1: List[Optional[int]] = [8, 4, 10, 2, 6, 9, None, None, None, 5, 7]
    root_1: Optional[TreeNode] = build_tree_level_order(example_1)
    result_1: int = solution.deepest_complete_subtree(root_1)
    print("Example 1 result:", result_1)

    # Example 2 from the prompt:
    # Tree:
    #        12
    #       /  \
    #      7    20
    #     /    /  \
    #    3    15  25
    #
    # Valid complete subtree roots:
    # - Leaves: 3, 15, 25
    # - Node 20 is valid because it has two leaf children.
    # - Node 7 is invalid because it has only one child.
    # - Node 12 is invalid because left subtree rooted at 7 is invalid.
    #
    # Deepest valid roots are 3, 15, and 25 at depth 2.
    # Smallest value among them is 3.
    example_2: List[Optional[int]] = [12, 7, 20, 3, None, 15, 25]
    root_2: Optional[TreeNode] = build_tree_level_order(example_2)
    result_2: int = solution.deepest_complete_subtree(root_2)
    print("Example 2 result:", result_2)

    # Additional quick sanity checks for beginners:
    #
    # Single-node tree:
    # The only node is a leaf, so it is a valid complete subtree root.
    single_node: List[Optional[int]] = [42]
    root_3: Optional[TreeNode] = build_tree_level_order(single_node)
    result_3: int = solution.deepest_complete_subtree(root_3)
    print("Single node result:", result_3)

    # Perfect small tree:
    #        1
    #       / \
    #      2   3
    #
    # Valid roots:
    # - 2 and 3 at depth 1
    # - 1 at depth 0
    #
    # Deepest valid roots are 2 and 3, smallest is 2.
    perfect_small: List[Optional[int]] = [1, 2, 3]
    root_4: Optional[TreeNode] = build_tree_level_order(perfect_small)
    result_4: int = solution.deepest_complete_subtree(root_4)
    print("Perfect small tree result:", result_4)