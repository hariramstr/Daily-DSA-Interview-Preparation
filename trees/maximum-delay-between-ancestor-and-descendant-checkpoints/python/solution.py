"""
Title: Maximum Delay Between Ancestor and Descendant Checkpoints

Problem Description:
You are given the root of a tree representing checkpoint events in a monitoring system.
Each node stores an integer timestamp. A checkpoint A is considered an ancestor of
checkpoint B if A lies on the path from the root to B, and A and B may be the same node.
The delay between an ancestor and a descendant is defined as:

    descendant.timestamp - ancestor.timestamp

Your task is to return the maximum possible delay over all valid ancestor-descendant
pairs in the tree.

This is not the same as finding the maximum difference between any two nodes anywhere
in the tree. The earlier checkpoint must be an ancestor of the later checkpoint.
Because timestamps may decrease or increase as you move down the tree, the best answer
may come from tracking the smallest timestamp seen so far on each root-to-node path.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Each node value is an integer timestamp in the range [-1000000000, 1000000000].
- The tree is not necessarily balanced.
- The answer is never negative because a node can be paired with itself, producing 0.

Examples:
1)
Input: root = [8,3,10,1,6,null,14,null,null,4,7,13]
Output: 11
Explanation:
The best pair is ancestor 3 and descendant 14, giving 14 - 3 = 11.

2)
Input: root = [5,9,2,12,null,-1,8]
Output: 7
Explanation:
One optimal pair is ancestor 5 and descendant 12, so the maximum delay is 12 - 5 = 7.

Approach:
Traverse the tree once. For every node, carry the minimum timestamp seen so far on the
current root-to-node path. The best delay ending at the current node is:

    current_node.val - minimum_value_seen_on_path

Update the global answer with that value, then continue to children with the updated
path minimum.

This gives an O(n) solution.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class TreeNode:
    """Binary tree node used for the checkpoint tree."""

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        """
        Initialize a tree node.

        Args:
            val: Integer timestamp stored in the node.
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
    def max_ancestor_delay(self, root: Optional[TreeNode]) -> int:
        """
        Return the maximum delay between an ancestor and a descendant.

        The delay is defined as:
            descendant.timestamp - ancestor.timestamp

        A node may be paired with itself, so the answer is always at least 0.

        Args:
            root: Root of the binary tree.

        Returns:
            The maximum valid ancestor-descendant delay.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is processed exactly once.

        Space complexity:
            O(h) for the recursion stack, where h is the height of the tree.
            In the worst case of a skewed tree, h can be O(n).
        """
        # Defensive handling for an empty tree.
        # The problem guarantees at least one node, but returning 0 here makes
        # the function safer and easier to reuse.
        if root is None:
            return 0

        # This variable stores the best answer found anywhere in the tree.
        # We start at 0 because pairing a node with itself always gives delay 0,
        # so the answer can never be smaller than 0.
        best_delay: int = 0

        def dfs(node: Optional[TreeNode], min_on_path: int) -> None:
            """
            Depth-first traversal that carries the minimum value seen so far
            on the current root-to-node path.

            Args:
                node: Current tree node being processed.
                min_on_path: Smallest timestamp seen from the root down to the
                    parent of this node (or including current node depending on
                    the caller's state).

            Returns:
                None

            Time complexity:
                O(size of subtree rooted at node), and overall O(n) across the
                full traversal.

            Space complexity:
                O(h) recursion depth for the current call stack.
            """
            nonlocal best_delay

            # Base case:
            # If there is no node, there is nothing to process.
            if node is None:
                return

            # At this exact node, the best ancestor to pair with it is the
            # smallest timestamp that has appeared on the path from the root
            # to this node.
            #
            # Why?
            # Because for a fixed descendant value node.val, the expression
            #     node.val - ancestor_value
            # becomes as large as possible when ancestor_value is as small
            # as possible.
            current_delay: int = node.val - min_on_path

            # Update the global best answer if this node creates a better delay.
            if current_delay > best_delay:
                best_delay = current_delay

            # Update the minimum value seen on the path so far.
            #
            # This updated minimum is what children should use, because for any
            # descendant below this node, valid ancestors include all previous
            # ancestors plus the current node itself.
            new_min_on_path: int = min(min_on_path, node.val)

            # Recurse into the left subtree carrying the updated path minimum.
            dfs(node.left, new_min_on_path)

            # Recurse into the right subtree carrying the updated path minimum.
            dfs(node.right, new_min_on_path)

        # Start DFS from the root.
        #
        # For the root, the minimum value on its path is simply its own value.
        # The root paired with itself gives delay 0, which is valid.
        dfs(root, root.val)

        return best_delay


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    Example:
        [8, 3, 10, 1, 6, None, 14, None, None, 4, 7, 13]

    This helper is included so the file is fully runnable and easy to test.

    Args:
        values: Level-order representation of a binary tree, where None means
            a missing child.

    Returns:
        The root of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the length of the input list.

    Space complexity:
        O(n), due to the queue and created nodes.
    """
    # If the input list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the tree is empty.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(values[0])

    # Queue used for standard level-order tree construction.
    # Each node pulled from the queue receives up to two children from the list.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list that has not yet
    # been used to create a child node.
    index: int = 1

    # Continue until we either run out of parent nodes or run out of input values.
    while queue and index < len(values):
        current: TreeNode = queue.popleft()

        # Assign left child if there is a remaining value.
        if index < len(values):
            left_value: Optional[int] = values[index]
            index += 1

            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if there is a remaining value.
        if index < len(values):
            right_value: Optional[int] = values[index]
            index += 1

            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


def print_example_result(values: List[Optional[int]], expected: int) -> None:
    """
    Build a tree from the given values, run the solution, and print the result.

    Args:
        values: Level-order tree representation.
        expected: Expected answer for comparison.

    Returns:
        None

    Time complexity:
        O(n), dominated by tree construction and traversal.

    Space complexity:
        O(n), due to the constructed tree and helper data structures.
    """
    root: Optional[TreeNode] = build_tree_from_level_order(values)
    solver: Solution = Solution()
    result: int = solver.max_ancestor_delay(root)
    print(f"Input: {values}")
    print(f"Output: {result}")
    print(f"Expected: {expected}")
    print(f"Match: {result == expected}")
    print("-" * 50)


if __name__ == "__main__":
    # Example 1 from the problem statement.
    #
    # Tree structure:
    #         8
    #       /   \
    #      3     10
    #     / \      \
    #    1   6      14
    #       / \     /
    #      4   7   13
    #
    # Best valid ancestor-descendant pair:
    # 3 -> 14 is NOT a direct path in a binary tree rooted at 8? Let's verify carefully.
    # Path to 14 is 8 -> 10 -> 14, so 3 is NOT an ancestor of 14 in this exact tree.
    #
    # Therefore, the originally stated explanation in the prompt is inconsistent with
    # the provided tree structure. We must compute the correct answer based on the
    # actual ancestor relation in the tree.
    #
    # Valid path-based differences include:
    # 8 -> 14 = 6
    # 10 -> 14 = 4
    # 8 -> 13 = 5
    # 3 -> 7 = 4
    # 3 -> 6 = 3
    # 1 -> 1 = 0, etc.
    #
    # So for this exact binary tree, the correct maximum delay is 6.
    example_1: List[Optional[int]] = [8, 3, 10, 1, 6, None, 14, None, None, 4, 7, 13]
    print_example_result(example_1, 6)

    # Example 2 from the problem statement.
    #
    # Tree structure:
    #         5
    #       /   \
    #      9     2
    #     /     / \
    #    12   -1   8
    #
    # Valid ancestor-descendant differences:
    # 5 -> 12 = 7
    # 5 -> 9 = 4
    # 5 -> 2 = -3
    # 2 -> 8 = 6
    # 2 -> -1 = -3
    # self-pairs = 0
    #
    # Maximum is 7.
    example_2: List[Optional[int]] = [5, 9, 2, 12, None, -1, 8]
    print_example_result(example_2, 7)

    # Additional small sanity checks for beginners.

    # Single-node tree:
    # Only valid pair is the node with itself, so answer is 0.
    single_node: List[Optional[int]] = [42]
    print_example_result(single_node, 0)

    # Decreasing chain:
    # 10 -> 7 -> 3 -> 1
    # Every descendant is smaller than its ancestors, so the best answer is still 0
    # by choosing a node with itself.
    decreasing_tree: List[Optional[int]] = [10, 7, None, 3, None, 1]
    print_example_result(decreasing_tree, 0)

    # Tree with negative values:
    #        -5
    #       /  \
    #    -10   -2
    #          /
    #        20
    #
    # Best pair is -5 -> 20 = 25, or -2 -> 20 = 22, so answer is 25.
    negative_mix: List[Optional[int]] = [-5, -10, -2, None, None, 20]
    print_example_result(negative_mix, 25)