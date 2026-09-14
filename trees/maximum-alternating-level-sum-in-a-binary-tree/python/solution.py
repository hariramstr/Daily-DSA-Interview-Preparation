"""
Title: Maximum Alternating Level Sum in a Binary Tree

Problem Description:
You are given the root of a binary tree where each node contains an integer value,
which may be positive, zero, or negative.

For any node, define its alternating level sum as the sum of values in its subtree
where nodes at even distance from that node are added and nodes at odd distance
from that node are subtracted.

In other words, for a chosen node x:
- include x.val
- subtract the values of its children
- add the values of its grandchildren
- subtract the values of its great-grandchildren
- and so on for the entire subtree of x

Your task is to return the maximum alternating level sum among all nodes in the tree.

Important:
This is not a path problem. For every candidate node, we evaluate its entire subtree
with alternating signs by depth relative to that node, then take the largest result
over all nodes.

Constraints:
- Number of nodes: [1, 100000]
- -100000 <= Node.val <= 100000
- The tree is a valid binary tree

Goal:
Design an O(n) solution using tree traversal and dynamic programming on trees.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class TreeNode:
    """
    Basic binary tree node.

    Attributes:
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
        self.val = val
        self.left = left
        self.right = right


class Solution:
    def maxAlternatingLevelSum(self, root: Optional[TreeNode]) -> int:
        """
        Compute the maximum alternating level sum among all nodes in the tree.

        Core idea:
        Let dp(node) be the alternating level sum of the subtree rooted at node:
            dp(node) = node.val - dp_children_level_1 + dp_grandchildren_level_2 - ...
        A very useful recurrence appears immediately:
            dp(node) = node.val - dp(node.left) - dp(node.right)
        Why?
        - dp(node.left) already represents:
              left.val - left.children + left.grandchildren - ...
          But from node's perspective, every node inside the left subtree should have
          the opposite sign compared to left's own perspective, so we subtract dp(left).
        - Same logic for the right subtree.

        Therefore, if we compute dp for every node in postorder (children first, then parent),
        we can get every node's alternating level sum in O(1) after its children are known.

        We then simply track the maximum dp value over all nodes.

        Args:
            root: Root of the binary tree.

        Returns:
            The maximum alternating level sum over all nodes.

        Time Complexity:
            O(n), because each node is processed a constant number of times.

        Space Complexity:
            O(n), due to the explicit traversal stack used for iterative postorder.
            In the worst case of a skewed tree, the stack can hold O(n) nodes.
        """
        # The problem guarantees at least one node, but we still guard against None
        # to keep the method safe and complete.
        if root is None:
            return 0

        # We need postorder traversal:
        # - process left subtree
        # - process right subtree
        # - process current node
        #
        # Why postorder?
        # Because dp(node) depends on dp(left) and dp(right), so children must be
        # computed before the parent.
        #
        # We use an iterative approach instead of recursion because the tree can have
        # up to 100000 nodes. A recursive DFS could exceed Python's recursion depth
        # on a very deep/skewed tree.
        #
        # The stack stores pairs:
        #   (node, visited_flag)
        #
        # Meaning:
        # - visited_flag == False:
        #     This is the first time we see the node.
        #     We will push it back as visited=True, then push its children.
        # - visited_flag == True:
        #     Both children have already been processed, so now we can compute dp(node).
        stack: List[Tuple[TreeNode, bool]] = [(root, False)]

        # We attach the computed alternating sum directly onto each node as a dynamic
        # attribute named "_alt_sum".
        #
        # This avoids using a separate dictionary keyed by node object and keeps the
        # implementation simple and efficient.
        #
        # For each node:
        #   node._alt_sum = node.val - left_alt_sum - right_alt_sum
        #
        # We also maintain the global best answer while computing these values.
        best: int = -10**30

        while stack:
            node, visited = stack.pop()

            if not visited:
                # First encounter of this node.
                # Push it back marked as visited so that after its children are processed,
                # we can compute its dp value.
                stack.append((node, True))

                # Push right child first, then left child.
                # Because stack is LIFO, left child will be processed before right child.
                # The exact left/right order does not matter for correctness here,
                # but this mirrors standard DFS ordering.
                if node.right is not None:
                    stack.append((node.right, False))
                if node.left is not None:
                    stack.append((node.left, False))
            else:
                # At this point, both children (if they exist) have already had their
                # alternating sums computed and stored.

                # If a child does not exist, its contribution is simply 0 because there
                # is no subtree there.
                left_alt_sum: int = node.left._alt_sum if node.left is not None else 0
                right_alt_sum: int = node.right._alt_sum if node.right is not None else 0

                # This is the key recurrence:
                #
                # alternating_sum(node)
                #   = node.val
                #     - alternating_sum(node.left)
                #     - alternating_sum(node.right)
                #
                # Intuition:
                # - The root node itself is at distance 0 from itself, so it is added.
                # - Every node in the left subtree is one level deeper relative to node
                #   than it is relative to node.left, so all signs flip -> subtract dp(left).
                # - Same for the right subtree.
                node._alt_sum = node.val - left_alt_sum - right_alt_sum

                # Update the global maximum answer.
                if node._alt_sum > best:
                    best = node._alt_sum

        return best


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common convention:
    - Each value represents a node.
    - None means the node is missing.
    - Children are assigned left-to-right level by level.

    Example:
        [5, 2, 4, 1, 3, None, 6]

    builds:
              5
            /   \
           2     4
          / \     \
         1   3     6

    Args:
        level_order: List of node values in level-order, using None for missing nodes.

    Returns:
        The root of the constructed binary tree, or None if the list is empty.

    Time Complexity:
        O(n), where n is the number of entries in the input list.

    Space Complexity:
        O(n), for the queue used during construction.
    """
    if not level_order:
        return None

    if level_order[0] is None:
        return None

    root = TreeNode(level_order[0])
    queue: Deque[TreeNode] = deque([root])
    index = 1

    # We process nodes in BFS order.
    # For each node popped from the queue, we try to assign:
    # - one left child from level_order[index]
    # - one right child from level_order[index + 1]
    while queue and index < len(level_order):
        current = queue.popleft()

        # Assign left child if available.
        if index < len(level_order):
            left_value = level_order[index]
            index += 1
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if available.
        if index < len(level_order):
            right_value = level_order[index]
            index += 1
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create the solution instance.
    solution = Solution()

    # Example 1 from the prompt:
    # Tree: [5,2,4,1,3,null,6]
    #
    # Structure:
    #       5
    #      / \
    #     2   4
    #    / \   \
    #   1   3   6
    #
    # Alternating sums:
    # - Node 1: 1
    # - Node 3: 3
    # - Node 6: 6
    # - Node 2: 2 - 1 - 3 = -2
    # - Node 4: 4 - 6 = -2
    # - Node 5: 5 - (-2) - (-2) = 9
    # Maximum = 9
    root1 = build_tree([5, 2, 4, 1, 3, None, 6])
    result1 = solution.maxAlternatingLevelSum(root1)
    print("Example 1 Output:", result1)  # Expected: 9

    # Example 2 from the prompt:
    # Tree: [-3,7,2,-5,1]
    #
    # Structure:
    #       -3
    #      /  \
    #     7    2
    #    / \
    #  -5   1
    #
    # Alternating sums:
    # - Node -5: -5
    # - Node 1: 1
    # - Node 2: 2
    # - Node 7: 7 - (-5) - 1 = 11
    # - Node -3: -3 - 11 - 2 = -16
    # Maximum = 11
    root2 = build_tree([-3, 7, 2, -5, 1])
    result2 = solution.maxAlternatingLevelSum(root2)
    print("Example 2 Output:", result2)  # Expected: 11

    # Additional small sanity checks.

    # Single node tree:
    # Only one candidate, so answer is the node value itself.
    root3 = build_tree([42])
    result3 = solution.maxAlternatingLevelSum(root3)
    print("Single Node Output:", result3)  # Expected: 42

    # Tree with negatives:
    #      -1
    #      / \
    #    -2  -3
    #
    # Alternating sums:
    # -2, -3, and for root: -1 - (-2) - (-3) = 4
    # Maximum = 4
    root4 = build_tree([-1, -2, -3])
    result4 = solution.maxAlternatingLevelSum(root4)
    print("Negative Tree Output:", result4)  # Expected: 4