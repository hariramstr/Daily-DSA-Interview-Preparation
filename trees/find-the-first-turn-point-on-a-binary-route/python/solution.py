"""
Title: Find the First Turn Point on a Binary Route

Problem Description:
You are given the root of a binary tree and a target value that is guaranteed to exist
in the tree. Imagine walking from the root down to the node containing the target value.
Along this route, each move is either to the left child or to the right child.

A node is called a turn point if the direction you take from that node is different
from the direction taken from its parent. In other words:
- if you moved left from one node and then right from the next node, that next node
  is where the direction changed,
- similarly for right followed by left.

Your task is to return the value of the first turn point encountered on the path from
the root to the target. If the path never changes direction, return -1.

Important clarification used by this solution:
If the path is:
    root -> A -> B
and the move root -> A is "left" while the move A -> B is "right",
then the first turn point is A, because A is the node from which the new direction
is taken. This matches the examples in the prompt.

Notes:
- The root can never be considered a turn point because it has no parent.
- If the target is the root, the answer is -1.
- Node values are unique.
- A simple DFS is sufficient.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class TreeNode:
    """Binary tree node.

    Attributes:
        val: Integer value stored in the node.
        left: Reference to the left child.
        right: Reference to the right child.
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
    def first_turn_point(self, root: Optional[TreeNode], target: int) -> int:
        """Return the value of the first turn point on the path from root to target.

        The algorithm performs a depth-first search to locate the target while
        simultaneously building the exact path from the root to that target.
        Once the path is known, it scans the sequence of moves:
        - "L" for moving to a left child
        - "R" for moving to a right child

        The first time two consecutive moves differ, the node where the second
        move starts is the first turn point.

        Args:
            root: The root of the binary tree.
            target: The target value guaranteed to exist in the tree.

        Returns:
            The value of the first turn point, or -1 if the path never changes
            direction.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because in the
            worst case we may visit each node once during DFS.

        Space complexity:
            O(h), where h is the height of the tree, for recursion stack and
            path storage. In the worst case of a skewed tree, this becomes O(n).
        """
        # If the tree is empty, there is no valid path.
        # The problem guarantees a valid tree with an existing target, but this
        # guard makes the method safer and more complete.
        if root is None:
            return -1

        # This list will store the nodes on the path from the root to the target.
        # Example:
        #   path_nodes = [8, 4, 6, 7]
        # if target is 7 in the sample tree.
        path_nodes: List[TreeNode] = []

        # We now run DFS to fill path_nodes with the exact root-to-target path.
        self._find_path(root, target, path_nodes)

        # If the target itself is the root, then the path length is 1.
        # There are no moves at all, so there cannot be a turn point.
        if len(path_nodes) <= 1:
            return -1

        # Build the list of directions taken along the path.
        # For each consecutive pair of nodes in the path:
        #   parent -> child
        # we record whether the child is the left or right child of the parent.
        #
        # Example:
        #   path: 8 -> 4 -> 6 -> 7
        #   directions: ["L", "R", "R"]
        directions: List[str] = []

        for index in range(len(path_nodes) - 1):
            current_node = path_nodes[index]
            next_node = path_nodes[index + 1]

            # Since next_node must be either the left or right child of current_node
            # on a valid root-to-target path, we can determine the direction directly.
            if current_node.left is next_node:
                directions.append("L")
            else:
                directions.append("R")

        # We now scan the directions to find the first change.
        #
        # Suppose directions are:
        #   ["L", "R", "R"]
        #
        # Compare:
        #   directions[0] with directions[1]
        # If they differ, then the turn happens at path_nodes[1].
        #
        # Why path_nodes[1]?
        # Because:
        #   path_nodes[0] -> path_nodes[1] used directions[0]
        #   path_nodes[1] -> path_nodes[2] used directions[1]
        # The new direction is taken FROM path_nodes[1], so that node is the
        # first turn point.
        for index in range(1, len(directions)):
            previous_direction = directions[index - 1]
            current_direction = directions[index]

            # A direction change means we found the first turn point.
            if previous_direction != current_direction:
                return path_nodes[index].val

        # If we finish scanning and never see a change, the path is entirely
        # left-only or right-only (or has just one move), so there is no turn point.
        return -1

    def _find_path(
        self,
        node: Optional[TreeNode],
        target: int,
        path_nodes: List[TreeNode],
    ) -> bool:
        """Find and store the path from the current node to the target using DFS.

        This helper performs a standard recursive depth-first search.
        It uses backtracking:
        1. Add the current node to the path.
        2. If it is the target, stop successfully.
        3. Otherwise, search left and right subtrees.
        4. If neither subtree contains the target, remove the current node
           from the path before returning.

        Args:
            node: The current node being explored.
            target: The target value to find.
            path_nodes: Mutable list storing the current root-to-node path.

        Returns:
            True if the target is found in this subtree, otherwise False.

        Time complexity:
            O(n), because each node is visited at most once.

        Space complexity:
            O(h), where h is the tree height, due to recursion and path storage.
        """
        # Base case:
        # If the current node is None, this path does not lead to the target.
        if node is None:
            return False

        # Add the current node to the path because we are now exploring through it.
        path_nodes.append(node)

        # If this node is the target, we are done.
        if node.val == target:
            return True

        # Recursively search the left subtree.
        # If found there, immediately return True and keep the path as-is.
        if self._find_path(node.left, target, path_nodes):
            return True

        # If not found on the left, search the right subtree.
        if self._find_path(node.right, target, path_nodes):
            return True

        # If neither subtree contains the target, this node is not part of the
        # final root-to-target path. Remove it before backtracking.
        path_nodes.pop()
        return False


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """Build a binary tree from a level-order list representation.

    This helper is included to make the example code in main easy to read and run.
    The input format matches common coding platform representations:
    - integers represent node values
    - None represents a missing child

    Example:
        [8, 4, 12, 2, 6, None, 14, None, None, 5, 7]

    Args:
        values: Level-order list representation of the tree.

    Returns:
        The root of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), for the queue used during construction.
    """
    # An empty list means there is no tree.
    if not values:
        return None

    # If the first value is None, the tree is invalid/empty in practice.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(values[0])

    # Queue of nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Start reading child values from index 1 onward.
    index = 1

    # Process nodes in BFS order, assigning left and right children.
    while queue and index < len(values):
        current = queue.popleft()

        # Assign left child if a value is available.
        if index < len(values):
            left_value = values[index]
            index += 1

            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if a value is available.
        if index < len(values):
            right_value = values[index]
            index += 1

            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # Tree: [8,4,12,2,6,None,14,None,None,5,7]
    # Target: 7
    #
    # Path to 7:
    #   8 -> 4 -> 6 -> 7
    # Directions:
    #   L, R, R
    # First change:
    #   L -> R
    # This change happens at node 4, because 4 is the node from which the new
    # direction ("R") is taken.
    #
    # Expected output: 4
    root1 = build_tree_from_level_order([8, 4, 12, 2, 6, None, 14, None, None, 5, 7])
    target1 = 7
    result1 = solution.first_turn_point(root1, target1)
    print(result1)

    # Example 2 from the prompt:
    # Tree: [10,5,15,3,7]
    # Target: 3
    #
    # Path to 3:
    #   10 -> 5 -> 3
    # Directions:
    #   L, L
    # No change in direction.
    #
    # Expected output: -1
    root2 = build_tree_from_level_order([10, 5, 15, 3, 7])
    target2 = 3
    result2 = solution.first_turn_point(root2, target2)
    print(result2)

    # Additional quick check:
    # If target is the root, answer must be -1.
    root3 = build_tree_from_level_order([1, 2, 3])
    target3 = 1
    result3 = solution.first_turn_point(root3, target3)
    print(result3)