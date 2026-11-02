"""
Title: Longest Alternating Turn Path in a Binary Tree

Problem Description:
You are given the root of a binary tree. A path may start at any node and must move
only downward, following parent-to-child edges. For every step in the path, you choose
either the left child or the right child of the current node. A path is called an
alternating turn path if the direction of every move alternates: left, right, left,
right, ... or right, left, right, left, ... . The path does not need to end at a leaf,
and it may contain just one node.

Return the length of the longest alternating turn path in the tree, measured in number
of edges.

For example, if a path visits nodes [8 -> 3 -> 6 -> 4], then its length is 3 because
it uses 3 edges. A single node has length 0.

Your task is to compute the maximum possible length over all valid starting nodes and
both possible first directions.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Node values are in the range [-1000000000, 1000000000].
- All node values are arbitrary and are not required to be unique.
- The expected solution should run in O(n) time.
- Use O(h) auxiliary space for recursion or an equivalent iterative traversal, where h
  is the tree height.

Approach Summary:
We perform one depth-first traversal. For every node, we compute two values:

1. left_start:
   The longest alternating path length starting at this node if the first move is to
   the left child.

2. right_start:
   The longest alternating path length starting at this node if the first move is to
   the right child.

These values can be computed from children:
- If we move left first, then the next move must be right.
  So:
      left_start = 1 + (right_start value from left child)
- If we move right first, then the next move must be left.
  So:
      right_start = 1 + (left_start value from right child)

If a required child does not exist, that path length is 0 because no edge can be taken.

The answer is the maximum of all left_start and right_start values across all nodes.
This is an O(n) solution because each node is processed exactly once.
"""

from __future__ import annotations

from collections import deque
from dataclasses import dataclass
from typing import Deque, List, Optional, Tuple


@dataclass
class TreeNode:
    """Binary tree node used for building and testing the solution."""

    val: int
    left: Optional["TreeNode"] = None
    right: Optional["TreeNode"] = None


class Solution:
    def longest_alternating_turn_path(self, root: Optional[TreeNode]) -> int:
        """
        Compute the length of the longest downward path whose move directions
        strictly alternate between left and right.

        Args:
            root: The root of the binary tree.

        Returns:
            The maximum number of edges in any valid alternating turn path.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is visited exactly once.

        Space complexity:
            O(h), where h is the height of the tree, due to recursion stack.
        """
        # This variable stores the best answer found anywhere in the tree.
        # We update it while processing each node.
        self.best: int = 0

        def dfs(node: Optional[TreeNode]) -> Tuple[int, int]:
            """
            Perform a post-order DFS and return two DP values for the current node.

            Returned tuple meaning:
            - index 0: longest alternating path starting at this node if the first
              move is to the left child
            - index 1: longest alternating path starting at this node if the first
              move is to the right child

            Args:
                node: Current tree node being processed.

            Returns:
                A tuple (left_start, right_start).

            Time complexity:
                O(size of subtree rooted at node) across all calls totals O(n).

            Space complexity:
                O(h) recursion depth.
            """
            # Base case:
            # If the node does not exist, there is no path starting here.
            # We return (0, 0). These values are safe because a missing child
            # contributes no additional edges.
            if node is None:
                return 0, 0

            # Recursively compute DP values for the left subtree.
            # left_left_start  = longest path from node.left if first move is left
            # left_right_start = longest path from node.left if first move is right
            left_left_start, left_right_start = dfs(node.left)

            # Recursively compute DP values for the right subtree.
            # right_left_start  = longest path from node.right if first move is left
            # right_right_start = longest path from node.right if first move is right
            right_left_start, right_right_start = dfs(node.right)

            # Compute the best path starting at the current node with first move LEFT.
            #
            # Why this formula?
            # - To start with a left move, the current node must have a left child.
            # - Taking that edge contributes 1 edge.
            # - After moving left, the next move must be RIGHT to preserve alternation.
            # - Therefore we add the "start with right" value from the left child.
            #
            # If there is no left child, we cannot take a left edge at all, so the
            # path length for "start left" is 0.
            if node.left is not None:
                left_start: int = 1 + left_right_start
            else:
                left_start = 0

            # Compute the best path starting at the current node with first move RIGHT.
            #
            # Symmetric reasoning:
            # - Need a right child to take the first edge.
            # - That first edge contributes 1.
            # - After moving right, the next move must be LEFT.
            # - So we add the "start with left" value from the right child.
            #
            # If there is no right child, this option is impossible and equals 0.
            if node.right is not None:
                right_start = 1 + right_left_start
            else:
                right_start = 0

            # Update the global best answer.
            #
            # Why do we do this at every node?
            # The problem allows the path to start at ANY node, not just the root.
            # Therefore every node is a candidate starting point.
            self.best = max(self.best, left_start, right_start)

            # Return the two DP values so the parent can build its own answer.
            return left_start, right_start

        # Run DFS from the root to process the entire tree.
        dfs(root)

        # self.best now contains the maximum alternating path length in edges.
        return self.best


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a standard level-order list representation.

    Example:
        [1, 2, 3, None, 4] means:
              1
             / \
            2   3
             \
              4

    Args:
        level_order: List of node values in level-order, using None for missing nodes.

    Returns:
        The root of the constructed binary tree.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), due to the queue used during construction.
    """
    # An empty list means there is no tree.
    if not level_order:
        return None

    # If the first value is None, the tree is empty.
    if level_order[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(level_order[0])

    # Queue stores nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list to consume.
    index = 1

    # Process nodes level by level.
    while queue and index < len(level_order):
        current = queue.popleft()

        # Assign left child if there is a remaining value.
        if index < len(level_order):
            left_value = level_order[index]
            index += 1

            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if there is a remaining value.
        if index < len(level_order):
            right_value = level_order[index]
            index += 1

            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create a Solution instance.
    solution = Solution()

    # Example 1 from the prompt:
    # root = [1,2,3,null,4,5,null,null,null,6]
    #
    # Tree structure:
    #         1
    #       /   \
    #      2     3
    #       \   /
    #        4 5
    #          /
    #         6
    #
    # One valid longest alternating path is:
    # 1 -> 2 -> 4
    # directions: left, right
    # length = 2
    example_1 = build_tree([1, 2, 3, None, 4, 5, None, None, None, 6])
    result_1 = solution.longest_alternating_turn_path(example_1)
    print("Example 1 Output:", result_1)

    # Example 2 from the prompt:
    # root = [7,9,8,4,null,null,10,null,5]
    #
    # Tree structure:
    #         7
    #       /   \
    #      9     8
    #     /       \
    #    4         10
    #     \
    #      5
    #
    # The path 9 -> 4 -> 5 is left then right, length 2.
    # Depending on the exact intended interpretation in the prompt text,
    # the algorithm correctly computes the true longest alternating path
    # in the given tree structure.
    example_2 = build_tree([7, 9, 8, 4, None, None, 10, None, 5])
    result_2 = solution.longest_alternating_turn_path(example_2)
    print("Example 2 Output:", result_2)

    # Additional sanity checks for beginners:
    #
    # Single node tree:
    # No edges exist, so answer must be 0.
    single_node = build_tree([42])
    print("Single Node Output:", solution.longest_alternating_turn_path(single_node))

    # Perfect alternating chain:
    #      1
    #     /
    #    2
    #     \
    #      3
    #     /
    #    4
    #
    # Path 1 -> 2 -> 3 -> 4 has directions L, R, L => length 3.
    alternating_chain = build_tree([1, 2, None, None, 3, 4])
    print("Alternating Chain Output:", solution.longest_alternating_turn_path(alternating_chain))