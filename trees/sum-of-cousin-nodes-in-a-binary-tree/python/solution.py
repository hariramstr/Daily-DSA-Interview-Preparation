"""
Title: Sum of Cousin Nodes in a Binary Tree

Problem Description:
You are given the root of a binary tree and a target value x that is guaranteed to
appear exactly once in the tree. Two nodes are considered cousins if they are on
the same depth level but have different parents. Your task is to return the sum
of all cousin node values of the node whose value is x.

If the target node has no cousins, return 0.

The tree is not necessarily complete or balanced. Node values may be positive,
negative, or zero, but all values in the tree are unique so the target node can
be identified by value alone.

A straightforward way to solve this is to traverse the tree level by level.
Once you find the level containing the target node, you should sum all nodes on
that same level except the target node itself and any siblings that share the
same parent as the target node.

Constraints:
- The number of nodes in the tree is in the range [1, 1000].
- -10^4 <= Node.val <= 10^4
- All node values are unique.
- x is the value of exactly one node in the tree.

Example 1:
Input: root = [5,3,8,1,4,7,9], x = 4
Output: 16

Example 2:
Input: root = [10,6,15,3,null,12,18], x = 12
Output: 3
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class TreeNode:
    """Binary tree node."""

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        """
        Initialize a binary tree node.

        Args:
            val: Value stored in the node.
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
    def sum_of_cousins(self, root: Optional[TreeNode], x: int) -> int:
        """
        Return the sum of cousin node values for the node whose value is x.

        The algorithm performs a level-order traversal (Breadth-First Search).
        For each level, it checks whether the target node x appears among the
        children of any node in the current level. Once the target level is found,
        it sums all children on that level except:
        1. The target node itself
        2. Any sibling of the target node (because siblings are not cousins)

        Args:
            root: Root of the binary tree.
            x: Target node value that appears exactly once in the tree.

        Returns:
            The sum of all cousin node values of the target node. If no cousins
            exist, returns 0.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is visited at most once during BFS.

        Space complexity:
            O(w), where w is the maximum width of the tree, due to the queue used
            for level-order traversal.
        """
        # If the tree is empty, there cannot be any cousins.
        # The problem guarantees a valid target in the tree, but this guard keeps
        # the method safe and complete for general use.
        if root is None:
            return 0

        # Special case:
        # If the root itself is the target, then it has no parent and therefore
        # cannot have cousins. Cousins must be on the same depth with different
        # parents, and there are no other nodes at depth 0 besides the root.
        if root.val == x:
            return 0

        # We use a queue for Breadth-First Search (BFS), also called level-order
        # traversal. This is the most natural choice here because cousin
        # relationships depend on depth/level.
        #
        # Each iteration of the outer loop processes exactly one tree level.
        queue: Deque[TreeNode] = deque([root])

        # Continue processing until all reachable levels are explored.
        while queue:
            # The number of nodes currently in the queue equals the number of
            # nodes in the current level.
            level_size: int = len(queue)

            # This variable will store the parent of the target node once we find
            # that the target exists in the next level (among children of the
            # current level's nodes).
            target_parent: Optional[TreeNode] = None

            # This list will store all child nodes that belong to the next level.
            # We collect them level by level so that if the target is found in
            # this next level, we can compute the cousin sum immediately.
            next_level_nodes: List[Tuple[TreeNode, TreeNode]] = []

            # Process every node in the current level.
            for _ in range(level_size):
                current: TreeNode = queue.popleft()

                # Check the left child, if it exists.
                if current.left is not None:
                    # Store both the child and its parent.
                    # We need the parent information later to exclude siblings.
                    next_level_nodes.append((current.left, current))

                    # If this child is the target, remember its parent.
                    if current.left.val == x:
                        target_parent = current

                # Check the right child, if it exists.
                if current.right is not None:
                    # Again, store child-parent pair for later processing.
                    next_level_nodes.append((current.right, current))

                    # If this child is the target, remember its parent.
                    if current.right.val == x:
                        target_parent = current

            # If target_parent is not None, then the target node x has been found
            # in the next level. That means all cousin candidates are exactly the
            # other nodes in next_level_nodes whose parent is different from
            # target_parent.
            if target_parent is not None:
                cousin_sum: int = 0

                # Go through every node in the target's level.
                for node, parent in next_level_nodes:
                    # A cousin must have a different parent from the target node.
                    # This condition automatically excludes:
                    # - the target node itself
                    # - the target's sibling (if it exists)
                    #
                    # Why? Because both the target and its sibling share the same
                    # parent, so parent != target_parent will be False for them.
                    if parent != target_parent:
                        cousin_sum += node.val

                return cousin_sum

            # If the target was not found in the next level, we must continue BFS.
            # So we enqueue all nodes from next_level_nodes to become the current
            # level in the next iteration.
            for node, _ in next_level_nodes:
                queue.append(node)

        # The problem guarantees that x exists exactly once in the tree, so in
        # valid inputs we should never reach this line. Returning 0 keeps the
        # function safe for unexpected inputs.
        return 0


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format uses None to represent missing children, similar to common
    coding platform tree serialization.

    Args:
        values: Level-order list of node values where None means no node.

    Returns:
        The root of the constructed binary tree, or None if the input is empty
        or starts with None.

    Time complexity:
        O(n), where n is the number of elements in the input list.

    Space complexity:
        O(n), due to the queue used while constructing the tree.
    """
    # If the list is empty or the first value is None, there is no tree to build.
    if not values or values[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(values[0])

    # Queue of nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list.
    index: int = 1

    # Continue until we either use all values or there are no more parent nodes
    # waiting in the queue.
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
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Tree: [5,3,8,1,4,7,9]
    # Target: 4
    #
    # Structure:
    #         5
    #       /   \
    #      3     8
    #     / \   / \
    #    1   4 7   9
    #
    # Node 4 is at depth 2 with parent 3.
    # Same-level nodes are 1, 4, 7, 9.
    # Exclude 4 itself and sibling 1.
    # Cousins are 7 and 9 => sum = 16
    root1 = build_tree_from_level_order([5, 3, 8, 1, 4, 7, 9])
    x1 = 4
    result1 = solution.sum_of_cousins(root1, x1)
    print(f"Example 1 Output: {result1}")  # Expected: 16

    # Example 2:
    # Tree: [10,6,15,3,None,12,18]
    # Target: 12
    #
    # Structure:
    #         10
    #       /    \
    #      6      15
    #     /      /  \
    #    3      12  18
    #
    # Node 12 is at depth 2 with parent 15.
    # Same-level nodes are 3, 12, 18.
    # Exclude 12 itself and sibling 18.
    # Cousin is 3 => sum = 3
    root2 = build_tree_from_level_order([10, 6, 15, 3, None, 12, 18])
    x2 = 12
    result2 = solution.sum_of_cousins(root2, x2)
    print(f"Example 2 Output: {result2}")  # Expected: 3

    # Additional quick check:
    # Single-node tree. Root has no cousins.
    root3 = build_tree_from_level_order([1])
    x3 = 1
    result3 = solution.sum_of_cousins(root3, x3)
    print(f"Additional Example Output: {result3}")  # Expected: 0