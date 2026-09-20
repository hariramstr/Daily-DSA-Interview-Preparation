"""
Title: Minimum Cameras to Monitor a Facility Tree

Problem Description:
A company models its facility layout as a binary tree. Each tree node represents a room,
and each room may have up to two directly connected child rooms. You want to install
security cameras so that every room is monitored.

A camera placed in a room monitors exactly three types of rooms: the room where it is
installed, its parent room, and its immediate child rooms. A room is considered secure
if it is monitored by at least one camera.

Given the root of the binary tree, return the minimum number of cameras needed to monitor
all rooms.

The number of nodes in the tree is in the range [1, 10^5].
Node values are integers in the range [0, 10^9], but values are only identifiers and do
not affect the answer.
The input tree is a valid binary tree.

Example 1:
Input: root = [0,0,null,0,0]
Output: 1

Example 2:
Input: root = [0,0,null,0,null,0,null,null,0]
Output: 2
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node used for the facility layout."""

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
    # We use three states during postorder traversal.
    # These integer constants make the code easier to read.
    NEEDS_CAMERA = 0
    HAS_CAMERA = 1
    COVERED = 2

    def minCameraCover(self, root: Optional[TreeNode]) -> int:
        """
        Compute the minimum number of cameras needed to monitor every node in the tree.

        The algorithm performs a postorder traversal (left, right, node) and assigns
        one of three states to each node:
        1. NEEDS_CAMERA: This node is not covered by any camera yet.
        2. HAS_CAMERA: A camera is installed on this node.
        3. COVERED: This node does not have a camera, but it is already monitored.

        The key greedy idea:
        - If any child needs a camera, place a camera at the current node.
        - If any child has a camera, the current node is covered.
        - Otherwise, the current node needs a camera.

        This works because cameras are most valuable when placed on the parent of an
        uncovered child: that single camera can cover the parent, the child, and the
        child's sibling if present.

        Args:
            root: Root of the binary tree.

        Returns:
            Minimum number of cameras required to cover the entire tree.

        Time complexity:
            O(n), where n is the number of nodes, because each node is processed once.

        Space complexity:
            O(h) for recursion stack, where h is the height of the tree.
            In the worst case of a skewed tree, this can be O(n).
        """
        self.camera_count: int = 0

        def dfs(node: Optional[TreeNode]) -> int:
            """
            Postorder traversal that returns the monitoring state of the current node.

            Args:
                node: Current tree node being processed.

            Returns:
                One of:
                - Solution.NEEDS_CAMERA
                - Solution.HAS_CAMERA
                - Solution.COVERED

            Time complexity:
                O(1) work per node, so O(n) total across the traversal.

            Space complexity:
                O(h) recursion stack, where h is the tree height.
            """
            # Base case:
            # A null child does not need a camera.
            # We treat it as already covered so that leaf nodes can be handled cleanly.
            # If we returned NEEDS_CAMERA for null, every leaf would force a camera
            # unnecessarily. Returning COVERED is the standard and correct choice.
            if node is None:
                return self.COVERED

            # Process left subtree first.
            # Because this is postorder traversal, we fully understand the state of the
            # left child before deciding what to do at the current node.
            left_state: int = dfs(node.left)

            # Process right subtree next.
            # Again, we want both child states before making the greedy decision here.
            right_state: int = dfs(node.right)

            # Case 1:
            # If either child still needs coverage, then the best place to install a
            # camera is the current node.
            #
            # Why?
            # - A camera on the current node covers:
            #   * the current node itself
            #   * its parent
            #   * its immediate children
            # - So one camera here can solve the uncovered child and also help upward.
            #
            # This is the greedy heart of the solution.
            if left_state == self.NEEDS_CAMERA or right_state == self.NEEDS_CAMERA:
                self.camera_count += 1
                return self.HAS_CAMERA

            # Case 2:
            # If any child has a camera, then the current node is already covered.
            # We do not need to place another camera here.
            #
            # This avoids wasting cameras.
            if left_state == self.HAS_CAMERA or right_state == self.HAS_CAMERA:
                return self.COVERED

            # Case 3:
            # If both children are covered, but neither child has a camera, then the
            # current node is currently not monitored by any child camera.
            #
            # We do NOT place a camera immediately. Instead, we mark this node as
            # needing a camera and let its parent decide. This is better because a
            # camera on the parent can cover more nodes than a camera on this node.
            return self.NEEDS_CAMERA

        # Run the postorder traversal from the root.
        root_state: int = dfs(root)

        # After traversal, the root may still be uncovered.
        # Since the root has no parent to cover it, we must place a camera at the root
        # if it still needs one.
        if root_state == self.NEEDS_CAMERA:
            self.camera_count += 1

        return self.camera_count


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common interview / online judge style:
    - Each list element represents a node value or None.
    - Children are assigned from left to right level by level.

    Example:
        [0, 0, None, 0, 0]

    Args:
        level_order: List of node values in level-order, using None for missing nodes.

    Returns:
        Root of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the number of list elements.

    Space complexity:
        O(n), due to the queue used during construction.
    """
    if not level_order:
        return None

    # If the first value is None, the tree is empty.
    if level_order[0] is None:
        return None

    # Create the root node from the first element.
    root: TreeNode = TreeNode(level_order[0])

    # Queue stores nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list to consume.
    index: int = 1

    # Continue until we either run out of nodes to expand or run out of input values.
    while queue and index < len(level_order):
        current: TreeNode = queue.popleft()

        # Assign left child if there is a value available.
        if index < len(level_order):
            left_value: Optional[int] = level_order[index]
            index += 1

            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if there is a value available.
        if index < len(level_order):
            right_value: Optional[int] = level_order[index]
            index += 1

            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Example 1 from the prompt:
    # Tree from level order: [0, 0, null, 0, 0]
    #
    # Structure:
    #       0
    #      /
    #     0
    #    / \
    #   0   0
    #
    # One camera on the second node from the top covers all nodes.
    example_1: List[Optional[int]] = [0, 0, None, 0, 0]
    root_1: Optional[TreeNode] = build_tree(example_1)
    result_1: int = Solution().minCameraCover(root_1)
    print(result_1)  # Expected: 1

    # Example 2 from the prompt:
    # Tree from level order: [0, 0, null, 0, null, 0, null, null, 0]
    #
    # This forms a deeper structure where two cameras are needed.
    example_2: List[Optional[int]] = [0, 0, None, 0, None, 0, None, None, 0]
    root_2: Optional[TreeNode] = build_tree(example_2)
    result_2: int = Solution().minCameraCover(root_2)
    print(result_2)  # Expected: 2

    # Additional small sanity check:
    # Single node tree needs exactly one camera.
    example_3: List[Optional[int]] = [5]
    root_3: Optional[TreeNode] = build_tree(example_3)
    result_3: int = Solution().minCameraCover(root_3)
    print(result_3)  # Expected: 1