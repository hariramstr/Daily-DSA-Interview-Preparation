"""
Title: Find the Next Right Record in an Incomplete Binary Archive

Problem Description:
You are given the root of a binary tree representing a partially filled archive index.
Each node stores a unique integer recordId. The tree is not necessarily perfect or
complete, and some children may be missing. You are also given an integer target
representing the recordId of an existing node in the tree.

For the node whose value is target, return the recordId of the nearest node to its
right on the same depth level. If no such node exists, return -1.

Two nodes are on the same depth level if they have the same distance from the root.
The "next right" node is the first node that would appear after the target when
scanning that level from left to right.

Your solution should work efficiently for large trees. A common interview follow-up
is whether you can solve this without storing every level explicitly, although any
solution with linear time complexity is acceptable.

Constraints:
- The number of nodes in the tree is in the range [1, 100000]
- 1 <= recordId <= 1000000000
- All recordId values are unique
- target is guaranteed to exist in the tree
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node used for the archive index."""

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
            left: Left child node.
            right: Right child node.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.val: int = val
        self.left: Optional["TreeNode"] = left
        self.right: Optional["TreeNode"] = right


class Solution:
    def findNearestRightNodeValue(self, root: Optional[TreeNode], target: int) -> int:
        """
        Return the value of the nearest node to the right of target on the same level.

        This method performs a level-order traversal (Breadth-First Search / BFS).
        BFS is ideal here because it naturally processes the tree one depth level at
        a time from left to right. That means when we find the target node inside a
        level, the very next node in that same level order is exactly the answer.

        Args:
            root: Root node of the binary tree.
            target: Value of the existing target node.

        Returns:
            The value of the nearest node to the right on the same level, or -1 if
            no such node exists.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node is
            visited at most once.

        Space complexity:
            O(w), where w is the maximum width of the tree, due to the BFS queue.
        """
        # If the tree is empty, there is no answer.
        # The problem guarantees at least one node, but this guard makes the method
        # safer and more reusable.
        if root is None:
            return -1

        # We use a queue for Breadth-First Search.
        # Why a queue?
        # - BFS explores nodes level by level.
        # - The problem asks for the next node on the same depth level.
        # - A queue preserves the exact left-to-right order in which nodes appear
        #   within each level.
        queue: Deque[TreeNode] = deque([root])

        # Continue until all reachable nodes have been processed.
        while queue:
            # At the start of each loop, the queue contains exactly the nodes for
            # the current level, in left-to-right order.
            #
            # We store the level size so we know where this level ends.
            # This is extremely important because:
            # - We only want the "next right" node on the SAME level.
            # - If target is the last node in this level, the answer must be -1,
            #   even though the queue may later contain nodes from the next level.
            level_size: int = len(queue)

            # Process every node in the current level from left to right.
            for index_in_level in range(level_size):
                # Remove the next node from the front of the queue.
                current: TreeNode = queue.popleft()

                # If this node is the target, then:
                # - If it is NOT the last node in this level, the next node to be
                #   processed in this same level is exactly the nearest right node.
                # - If it IS the last node in this level, there is no node to its
                #   right on the same level, so return -1.
                if current.val == target:
                    if index_in_level < level_size - 1:
                        # The next node currently at the front of the queue belongs
                        # to the same level because we have not yet finished this
                        # level's loop.
                        return queue[0].val
                    return -1

                # Add children for the next level.
                # We always enqueue left child before right child so that the queue
                # preserves the natural left-to-right order required by the problem.
                if current.left is not None:
                    queue.append(current.left)
                if current.right is not None:
                    queue.append(current.right)

        # The problem guarantees target exists, so normally execution never reaches
        # this line. We return -1 as a defensive fallback.
        return -1


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common interview/LeetCode style:
    - values[i] is the node value at that position in level order
    - None means that position has no node

    Example:
        [10, 5, 15, 2, 7, None, 20]

    Args:
        values: Level-order representation of the tree.

    Returns:
        The root of the constructed binary tree, or None if the list is empty or
        starts with None.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), for the queue and created nodes.
    """
    # If the input list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the root does not exist.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(values[0])

    # Queue of parent nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list that has not yet been used.
    index: int = 1

    # Process parents one by one, assigning left and right children.
    while queue and index < len(values):
        parent: TreeNode = queue.popleft()

        # Assign left child if there is still input remaining.
        if index < len(values):
            left_value: Optional[int] = values[index]
            index += 1

            if left_value is not None:
                parent.left = TreeNode(left_value)
                queue.append(parent.left)

        # Assign right child if there is still input remaining.
        if index < len(values):
            right_value: Optional[int] = values[index]
            index += 1

            if right_value is not None:
                parent.right = TreeNode(right_value)
                queue.append(parent.right)

    return root


if __name__ == "__main__":
    # Create a Solution instance.
    solution = Solution()

    # Example 1:
    # Tree: [10, 5, 15, 2, 7, None, 20]
    # Levels:
    #   depth 0 -> [10]
    #   depth 1 -> [5, 15]
    #   depth 2 -> [2, 7, 20]
    #
    # target = 5
    # The next node to the right of 5 on the same level is 15.
    example1_root: Optional[TreeNode] = build_tree_from_level_order(
        [10, 5, 15, 2, 7, None, 20]
    )
    example1_target: int = 5
    example1_result: int = solution.findNearestRightNodeValue(
        example1_root, example1_target
    )
    print("Example 1 Output:", example1_result)  # Expected: 15

    # Example 2:
    # Tree: [8, 3, 11, 1, 6, 9, None, None, None, 4, 7]
    # Levels:
    #   depth 0 -> [8]
    #   depth 1 -> [3, 11]
    #   depth 2 -> [1, 6, 9]
    #   depth 3 -> [4, 7]
    #
    # target = 6
    # The next node to the right of 6 on the same level is 9.
    example2_root: Optional[TreeNode] = build_tree_from_level_order(
        [8, 3, 11, 1, 6, 9, None, None, None, 4, 7]
    )
    example2_target: int = 6
    example2_result: int = solution.findNearestRightNodeValue(
        example2_root, example2_target
    )
    print("Example 2 Output:", example2_result)  # Expected: 9

    # Additional verification from the problem statement's explanation:
    # If target were 9 in Example 2, the answer should be -1 because 9 is the
    # rightmost node on its level.
    example2_target_rightmost: int = 9
    example2_rightmost_result: int = solution.findNearestRightNodeValue(
        example2_root, example2_target_rightmost
    )
    print("Example 2 (target=9) Output:", example2_rightmost_result)  # Expected: -1