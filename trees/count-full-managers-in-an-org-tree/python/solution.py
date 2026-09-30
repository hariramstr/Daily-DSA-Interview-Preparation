"""
Title: Count Full Managers in an Org Tree
Difficulty: Easy
Topic: Trees

Problem Description:
A company stores its reporting structure as a binary tree. Each node represents one employee,
and the left and right child pointers represent up to two direct reports. A manager is called
full if they have exactly two direct reports. Employees with zero or one direct report are not
considered full managers.

Given the root of the org tree, return the number of full managers in the company.

This is a basic tree traversal problem. You may solve it using either depth-first search
(recursive or iterative) or breadth-first search. The tree can be empty, in which case the
answer is 0.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are integers in the range [-10^4, 10^4].
- All node values are irrelevant to the counting logic; only the tree structure matters.

Example 1:
Input: root = [10,5,20,3,7,null,30]
Output: 2
Explanation: Employee 10 has two direct reports (5 and 20), and employee 5 has two direct
reports (3 and 7). Employee 20 has only one direct report (30). So the total number of full
managers is 2.

Example 2:
Input: root = [1,2,3,4,null,null,null]
Output: 1
Explanation: Employee 1 has exactly two direct reports, so they count as a full manager.
Employee 2 has only one direct report, and all other nodes have none. Therefore the answer is 1.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node used to represent an employee in the org tree."""

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
    def count_full_managers(self, root: Optional[TreeNode]) -> int:
        """
        Count how many nodes in the binary tree have exactly two children.

        This method performs a breadth-first traversal of the tree and checks
        each node to see whether both its left and right child exist.

        Args:
            root: The root node of the binary tree.

        Returns:
            The number of full managers (nodes with exactly two children).

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is visited exactly once.

        Space complexity:
            O(w), where w is the maximum number of nodes stored in the queue at
            one time (the maximum width of the tree).
        """
        # If the tree is empty, there are no employees and therefore no full managers.
        # Returning 0 immediately avoids unnecessary work and also safely handles
        # the edge case described in the problem statement.
        if root is None:
            return 0

        # We use a queue for breadth-first search (BFS).
        # Why BFS?
        # - It is simple and beginner-friendly.
        # - It visits nodes level by level.
        # - For this problem, any traversal works, but BFS makes the process easy to follow.
        #
        # deque from collections is chosen because:
        # - appending to the right is O(1)
        # - popping from the left is also O(1)
        # A normal Python list would be inefficient for popping from the front.
        queue: Deque[TreeNode] = deque([root])

        # This variable will store the final count of full managers.
        # A "full manager" means a node that has BOTH a left child and a right child.
        full_manager_count: int = 0

        # Continue processing until there are no more nodes left in the queue.
        while queue:
            # Remove the next node from the front of the queue.
            # This is the current employee/manager we are examining.
            current: TreeNode = queue.popleft()

            # Check whether the current node has exactly two direct reports.
            # In a binary tree, that means:
            # - current.left is not None
            # - current.right is not None
            #
            # If both exist, this node is a full manager, so we increase the count.
            if current.left is not None and current.right is not None:
                full_manager_count += 1

            # If the left child exists, add it to the queue so we can examine it later.
            # This ensures every node in the tree is eventually visited.
            if current.left is not None:
                queue.append(current.left)

            # If the right child exists, add it to the queue as well.
            if current.right is not None:
                queue.append(current.right)

        # After the traversal finishes, full_manager_count contains the total number
        # of nodes that had exactly two children.
        return full_manager_count


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common array-style tree representation:
    - values[0] is the root
    - For each node, the next two values represent its left and right children
    - None means that child does not exist

    Args:
        values: A list of integers and/or None values representing the tree.

    Returns:
        The root of the constructed binary tree, or None if the list is empty
        or starts with None.

    Time complexity:
        O(n), where n is the number of items in the input list.

    Space complexity:
        O(n), due to the queue and the created tree nodes.
    """
    # If the input list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the root does not exist, so the tree is empty.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(values[0])

    # Queue used to keep track of nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level-order list that has not yet been used.
    index: int = 1

    # Process nodes in the queue one by one, assigning left and right children.
    while queue and index < len(values):
        current: TreeNode = queue.popleft()

        # Assign the left child if there is still a value available.
        if index < len(values):
            left_value: Optional[int] = values[index]
            index += 1

            # Only create a node if the value is not None.
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign the right child if there is still a value available.
        if index < len(values):
            right_value: Optional[int] = values[index]
            index += 1

            # Only create a node if the value is not None.
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Tree from level order: [10, 5, 20, 3, 7, None, 30]
    #
    # Structure:
    #         10
    #        /  \
    #       5    20
    #      / \     \
    #     3   7     30
    #
    # Full managers:
    # - 10 has two children: 5 and 20
    # - 5 has two children: 3 and 7
    # - 20 has only one child: 30
    # Expected answer: 2
    example_1_values: List[Optional[int]] = [10, 5, 20, 3, 7, None, 30]
    example_1_root: Optional[TreeNode] = build_tree_from_level_order(example_1_values)
    example_1_result: int = solution.count_full_managers(example_1_root)
    print("Example 1 Output:", example_1_result)  # Expected: 2

    # Example 2:
    # Tree from level order: [1, 2, 3, 4, None, None, None]
    #
    # Structure:
    #         1
    #        / \
    #       2   3
    #      /
    #     4
    #
    # Full managers:
    # - 1 has two children: 2 and 3
    # - 2 has only one child: 4
    # - 3 has no children
    # Expected answer: 1
    example_2_values: List[Optional[int]] = [1, 2, 3, 4, None, None, None]
    example_2_root: Optional[TreeNode] = build_tree_from_level_order(example_2_values)
    example_2_result: int = solution.count_full_managers(example_2_root)
    print("Example 2 Output:", example_2_result)  # Expected: 1

    # Additional edge case:
    # Empty tree should return 0.
    empty_tree_values: List[Optional[int]] = []
    empty_tree_root: Optional[TreeNode] = build_tree_from_level_order(empty_tree_values)
    empty_tree_result: int = solution.count_full_managers(empty_tree_root)
    print("Empty Tree Output:", empty_tree_result)  # Expected: 0