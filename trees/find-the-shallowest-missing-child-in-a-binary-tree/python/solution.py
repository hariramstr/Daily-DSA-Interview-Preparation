"""
Title: Find the Shallowest Missing Child in a Binary Tree

Problem Description:
You are given the root of a binary tree representing a hierarchy of checkpoints.
A checkpoint is considered incomplete if it is missing at least one direct child:
its left child, its right child, or both.

Your task is to return the value of the incomplete checkpoint that appears at the
smallest depth from the root. If multiple incomplete checkpoints exist at the same
depth, return the one that appears first from left to right.

In other words, scan the tree level by level. The first node you encounter that
does not have two children is the answer. If the tree is empty, return -1.

This problem is designed to test basic tree traversal skills and understanding of
level-order processing. A breadth-first search works naturally because it visits
nodes in increasing depth order and preserves left-to-right order within each level.

Constraints:
- The number of nodes is in the range [0, 1000].
- Node values are integers in the range [-10000, 10000].
- All node values are unique.

Examples:
1) Input: root = [7,4,9,2,6,null,11]
   Output: 9
   Explanation:
   - Node 7 has both children.
   - Node 4 has both children.
   - Node 9 is missing a left child, so it is the first incomplete checkpoint
     found in level-order traversal.

2) Input: root = [5,3,8,1,null,7,10]
   Output: 3
   Explanation:
   - At depth 1, node 3 is missing its right child.
   - Node 8 has two children, but node 3 appears first from left to right,
     so the answer is 3.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node.

    Args:
        val: Integer value stored in the node.
        left: Left child node.
        right: Right child node.

    Attributes:
        val: The node's integer value.
        left: Reference to the left child.
        right: Reference to the right child.
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
    def find_shallowest_missing_child(self, root: Optional[TreeNode]) -> int:
        """Return the first node in level-order traversal that is missing a child.

        This method performs a breadth-first search (BFS), which is the natural
        choice for this problem because BFS visits nodes:
        1. By increasing depth (top to bottom)
        2. From left to right within the same depth

        Therefore, the first node encountered that does not have both a left and
        a right child is exactly the required answer.

        Args:
            root: The root of the binary tree.

        Returns:
            The value of the shallowest incomplete node, or -1 if the tree is empty.

        Time Complexity:
            O(n), where n is the number of nodes in the tree, because in the worst
            case we may visit every node once.

        Space Complexity:
            O(w), where w is the maximum width of the tree, due to the BFS queue.
            In the worst case, this can be O(n).
        """
        # If the tree is empty, there is no node to inspect.
        # The problem explicitly says to return -1 in this case.
        if root is None:
            return -1

        # We use a queue for breadth-first search.
        # Why a queue?
        # - BFS requires processing nodes in the exact order they are discovered.
        # - A queue is First-In-First-Out (FIFO), which matches level-order traversal.
        # - This guarantees we inspect all nodes at depth d before any node at depth d+1.
        queue: Deque[TreeNode] = deque([root])

        # Continue processing until we find the answer.
        # Because the problem guarantees that the first incomplete node in BFS order
        # is the correct answer, we can return immediately when we find it.
        while queue:
            # Remove the node at the front of the queue.
            # This is the next node in level-order traversal.
            current: TreeNode = queue.popleft()

            # A node is considered incomplete if:
            # - left child is missing, OR
            # - right child is missing, OR
            # - both are missing
            #
            # So the only "complete" case is when both children exist.
            # If either child is absent, this is the first incomplete node
            # encountered in BFS order, so we return its value immediately.
            if current.left is None or current.right is None:
                return current.val

            # If we reach this point, the current node has BOTH children.
            # We add them to the queue in left-to-right order.
            #
            # Why left first, then right?
            # - The problem requires left-to-right order among nodes at the same depth.
            # - Enqueuing left before right preserves that order naturally.
            queue.append(current.left)
            queue.append(current.right)

        # In a non-empty tree, BFS will always eventually find a node with a missing
        # child, because leaf nodes have no children and are therefore incomplete.
        # This return is included only as a safe fallback.
        return -1


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """Build a binary tree from a level-order list representation.

    The input format follows the common array-style tree representation:
    - Each value corresponds to a node in level-order.
    - None means that position has no node.

    Example:
        [7, 4, 9, 2, 6, None, 11]

    Produces:
            7
           / \
          4   9
         / \   \
        2   6   11

    Args:
        level_order: List of node values in level-order, using None for missing nodes.

    Returns:
        The root of the constructed binary tree, or None if the list is empty.

    Time Complexity:
        O(n), where n is the number of elements in the input list.

    Space Complexity:
        O(n), due to the queue used during construction and the created nodes.
    """
    # If the input list is empty, there is no tree to build.
    if not level_order:
        return None

    # If the first value is None, the root itself is missing,
    # so the tree is empty.
    if level_order[0] is None:
        return None

    # Create the root node from the first element.
    root: TreeNode = TreeNode(level_order[0])

    # Queue of nodes whose children we still need to assign.
    # This mirrors the level-order structure of the input.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the list that has not yet been used.
    index: int = 1

    # Process nodes in queue order, assigning left and right children
    # from the level-order list.
    while queue and index < len(level_order):
        current: TreeNode = queue.popleft()

        # Assign left child if there is still input remaining.
        if index < len(level_order):
            left_value: Optional[int] = level_order[index]
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)
            index += 1

        # Assign right child if there is still input remaining.
        if index < len(level_order):
            right_value: Optional[int] = level_order[index]
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)
            index += 1

    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement:
    # Tree: [7,4,9,2,6,None,11]
    #
    # Level-order traversal inspection:
    # - 7 has both children -> continue
    # - 4 has both children -> continue
    # - 9 is missing left child -> answer is 9
    example_1: List[Optional[int]] = [7, 4, 9, 2, 6, None, 11]
    root_1: Optional[TreeNode] = build_tree(example_1)
    result_1: int = solution.find_shallowest_missing_child(root_1)
    print("Example 1 result:", result_1)  # Expected: 9

    # Example 2 from the problem statement:
    # Tree: [5,3,8,1,None,7,10]
    #
    # Level-order traversal inspection:
    # - 5 has both children -> continue
    # - 3 is missing right child -> answer is 3
    # - Even though 8 has both children, 3 appears first from left to right
    example_2: List[Optional[int]] = [5, 3, 8, 1, None, 7, 10]
    root_2: Optional[TreeNode] = build_tree(example_2)
    result_2: int = solution.find_shallowest_missing_child(root_2)
    print("Example 2 result:", result_2)  # Expected: 3

    # Additional simple test: empty tree
    example_3: List[Optional[int]] = []
    root_3: Optional[TreeNode] = build_tree(example_3)
    result_3: int = solution.find_shallowest_missing_child(root_3)
    print("Example 3 result:", result_3)  # Expected: -1

    # Additional simple test: single-node tree
    # The root has no children, so it is incomplete and should be returned.
    example_4: List[Optional[int]] = [42]
    root_4: Optional[TreeNode] = build_tree(example_4)
    result_4: int = solution.find_shallowest_missing_child(root_4)
    print("Example 4 result:", result_4)  # Expected: 42