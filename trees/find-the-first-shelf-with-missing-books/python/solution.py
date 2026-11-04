"""
Title: Find the First Shelf With Missing Books

Problem Description:
You are given the root of a binary tree representing shelves in a library.
Each node stores an integer shelf ID.

A shelf is considered complete if it has either:
- 0 children, or
- exactly 2 children

A shelf is considered missing books if it has exactly one child, meaning one side
of the shelf structure exists and the other side is empty.

Return the shelf ID of the first shelf with missing books when the tree is scanned
level by level from top to bottom, and from left to right within each level.

If no such shelf exists, return -1.

In other words, perform a breadth-first search (BFS) and find the earliest node
that has only one child.

Constraints:
- The number of nodes in the tree is in the range [0, 1000]
- Node values are in the range [1, 100000]
- All shelf IDs are unique
- The input tree is a binary tree and may be empty

Example 1:
Input: root = [10,5,20,3,null,15,30]
Output: 5

Explanation:
Shelf 5 has a left child (3) but no right child. During level-order traversal,
it is the first node with exactly one child.

Example 2:
Input: root = [8,4,12,2,6,10,14]
Output: -1

Explanation:
Every non-leaf shelf has either two children or none, so there is no shelf with
exactly one child.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node used to represent a shelf in the library."""

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
        self.val = val
        self.left = left
        self.right = right


class Solution:
    def first_shelf_with_missing_books(self, root: Optional[TreeNode]) -> int:
        """
        Find the first node in level-order traversal that has exactly one child.

        This method performs a breadth-first search (BFS), which naturally visits
        nodes level by level from left to right. That traversal order exactly matches
        the problem statement, so the first node we encounter with only one child is
        the correct answer.

        Args:
            root: The root of the binary tree.

        Returns:
            The value of the first node that has exactly one child, or -1 if no such
            node exists.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node is
            visited at most once.

        Space complexity:
            O(w), where w is the maximum width of the tree, due to the BFS queue.
        """
        # If the tree is empty, there are no shelves at all.
        # Therefore, there cannot be any shelf with exactly one child.
        if root is None:
            return -1

        # We use a queue for breadth-first search.
        # Why a queue?
        # - BFS requires processing nodes in the exact order they are discovered.
        # - A queue follows First-In-First-Out (FIFO), which is perfect for level-order traversal.
        # - This guarantees we scan top to bottom and left to right.
        queue: Deque[TreeNode] = deque([root])

        # Continue processing until there are no more nodes left to inspect.
        while queue:
            # Remove the node at the front of the queue.
            # This is the next node in level-order traversal.
            current: TreeNode = queue.popleft()

            # Determine whether left and right children exist.
            # Storing these checks in variables makes the logic easier to read,
            # especially for beginners.
            has_left: bool = current.left is not None
            has_right: bool = current.right is not None

            # A node has exactly one child if:
            # - it has a left child but no right child, OR
            # - it has a right child but no left child
            #
            # The XOR idea ("one or the other, but not both") can be expressed clearly as:
            # has_left != has_right
            #
            # Why this works:
            # - True != False  -> True  (exactly one child)
            # - False != True  -> True  (exactly one child)
            # - True != True   -> False (two children)
            # - False != False -> False (no children)
            if has_left != has_right:
                # Because BFS visits nodes in the required order, the first such node
                # is immediately the correct answer.
                return current.val

            # If the left child exists, add it to the queue.
            # It will be processed later in proper BFS order.
            if current.left is not None:
                queue.append(current.left)

            # If the right child exists, add it to the queue.
            # We add left before right so that within the same level,
            # nodes are processed from left to right.
            if current.right is not None:
                queue.append(current.right)

        # If we finish the entire BFS without finding any node with exactly one child,
        # then every node had either 0 or 2 children.
        return -1


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format uses:
    - integers for real nodes
    - None for missing children

    Example:
        [10, 5, 20, 3, None, 15, 30]

    builds the tree:
            10
           /  \
          5    20
         /    /  \
        3    15  30

    Args:
        level_order: List representing the tree in level-order.

    Returns:
        The root of the constructed binary tree, or None if the list is empty
        or starts with None.

    Time complexity:
        O(n), where n is the number of entries in the input list.

    Space complexity:
        O(n), due to the queue and created nodes.
    """
    # If the input list is empty, there is no tree to build.
    if not level_order:
        return None

    # If the first value is None, the root does not exist.
    if level_order[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(level_order[0])

    # Queue used to assign children level by level.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list that we have not used yet.
    index: int = 1

    # Process parent nodes one by one until we run out of input values.
    while queue and index < len(level_order):
        parent = queue.popleft()

        # Try to assign the left child if there is still input remaining.
        if index < len(level_order):
            left_value = level_order[index]
            if left_value is not None:
                parent.left = TreeNode(left_value)
                queue.append(parent.left)
            index += 1

        # Try to assign the right child if there is still input remaining.
        if index < len(level_order):
            right_value = level_order[index]
            if right_value is not None:
                parent.right = TreeNode(right_value)
                queue.append(parent.right)
            index += 1

    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Tree: [10, 5, 20, 3, None, 15, 30]
    #
    # Level-order traversal:
    # - Visit 10: has two children (5 and 20) -> not the answer
    # - Visit 5: has left child 3, but no right child -> exactly one child
    # Therefore, the correct answer is 5.
    example_1 = [10, 5, 20, 3, None, 15, 30]
    root_1 = build_tree(example_1)
    result_1 = solution.first_shelf_with_missing_books(root_1)
    print("Example 1 Output:", result_1)  # Expected: 5

    # Example 2:
    # Tree: [8, 4, 12, 2, 6, 10, 14]
    #
    # Every non-leaf node has exactly two children:
    # - 8 has 4 and 12
    # - 4 has 2 and 6
    # - 12 has 10 and 14
    # Leaves have zero children.
    # So there is no node with exactly one child.
    # Therefore, the correct answer is -1.
    example_2 = [8, 4, 12, 2, 6, 10, 14]
    root_2 = build_tree(example_2)
    result_2 = solution.first_shelf_with_missing_books(root_2)
    print("Example 2 Output:", result_2)  # Expected: -1

    # Additional sample: empty tree
    example_3: List[Optional[int]] = []
    root_3 = build_tree(example_3)
    result_3 = solution.first_shelf_with_missing_books(root_3)
    print("Example 3 Output:", result_3)  # Expected: -1

    # Additional sample: root with only one child
    example_4 = [1, 2, None]
    root_4 = build_tree(example_4)
    result_4 = solution.first_shelf_with_missing_books(root_4)
    print("Example 4 Output:", result_4)  # Expected: 1