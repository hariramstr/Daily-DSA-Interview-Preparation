"""
Title: Count Branches With Missing Siblings

Problem Description:
You are given the root of a binary tree that represents a simple organizational chart.
Each node stores an integer ID, but the ID values do not affect the answer.

Your task is to count how many nodes in the tree have exactly one child.
That means we count a node if:
- it has a left child but no right child, or
- it has a right child but no left child

Important notes:
- Leaf nodes do not count because they have zero children.
- Nodes with two children do not count.
- Only nodes with exactly one child should be included.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- -10^4 <= Node.val <= 10^4
- The tree is a binary tree.

Examples:
1)
Input: root = [5,3,8,1,null,null,9]
Output: 2
Explanation:
- Node 3 has only one child (1)
- Node 8 has only one child (9)
So the answer is 2.

2)
Input: root = [7,4,10,2,6,null,null]
Output: 0
Explanation:
- Node 4 has two children
- Node 10 has no children
- Nodes 2 and 6 are leaves
So no node has exactly one child.
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
    def countNodesWithOneChild(self, root: Optional[TreeNode]) -> int:
        """Count how many nodes in a binary tree have exactly one child.

        This method performs a depth-first traversal of the tree.
        For each node, it checks whether exactly one of its two child pointers
        is present. If so, that node contributes 1 to the answer.

        Args:
            root: The root node of the binary tree.

        Returns:
            The number of nodes that have exactly one child.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is visited exactly once.

        Space complexity:
            O(h), where h is the height of the tree due to recursion stack.
            In the worst case of a skewed tree, this can be O(n).
        """

        def dfs(node: Optional[TreeNode]) -> int:
            """Recursively count nodes with exactly one child in a subtree.

            Args:
                node: Current subtree root.

            Returns:
                Count of nodes with exactly one child in this subtree.

            Time complexity:
                O(k), where k is the number of nodes in this subtree.

            Space complexity:
                O(h), where h is the height of this subtree.
            """
            # Base case:
            # If the current node is None, there is no actual node here,
            # so there is nothing to count.
            if node is None:
                return 0

            # We want to know whether this node has exactly one child.
            #
            # A node has exactly one child if:
            # - left exists and right does not, OR
            # - right exists and left does not
            #
            # In Python, the XOR-like behavior for booleans can be expressed
            # with "!=" because:
            # - True != False  -> True
            # - False != True  -> True
            # - True != True   -> False
            # - False != False -> False
            #
            # So this is a clean and readable way to test "exactly one exists".
            has_exactly_one_child = (node.left is not None) != (node.right is not None)

            # If the current node satisfies the condition, count it as 1.
            # Otherwise, count it as 0.
            current_count = 1 if has_exactly_one_child else 0

            # Recursively count matching nodes in the left subtree.
            left_count = dfs(node.left)

            # Recursively count matching nodes in the right subtree.
            right_count = dfs(node.right)

            # The total for this subtree is:
            # count from current node + count from left subtree + count from right subtree
            return current_count + left_count + right_count

        # Start the recursive traversal from the root and return the final count.
        return dfs(root)


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """Build a binary tree from a level-order list representation.

    The input format follows the common array representation of binary trees:
    - values[0] is the root
    - For each node, the next two values represent its left and right children
    - None means that child is missing

    Example:
        [5, 3, 8, 1, None, None, 9]

    Builds the tree:
            5
           / \
          3   8
         /     \
        1       9

    Args:
        values: Level-order list of node values, where None indicates no node.

    Returns:
        The root of the constructed binary tree, or None if the list is empty
        or starts with None.

    Time complexity:
        O(n), where n is the number of items in the input list.

    Space complexity:
        O(n), because we may store many nodes in the queue.
    """
    # If the input list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the root does not exist.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(values[0])

    # We use a queue to process nodes in level-order.
    # This matches the structure of the input list.
    queue: Deque[TreeNode] = deque([root])

    # This index points to the next value in the list that has not yet been used.
    index = 1

    # Continue while there are parent nodes waiting in the queue
    # and there are still values left to process.
    while queue and index < len(values):
        # Take the next parent node from the front of the queue.
        current = queue.popleft()

        # Try to assign the left child if there is still input remaining.
        if index < len(values):
            left_value = values[index]
            index += 1

            # If the value is not None, create an actual left child node.
            if left_value is not None:
                current.left = TreeNode(left_value)

                # Add the new child to the queue so its children can be assigned later.
                queue.append(current.left)

        # Try to assign the right child if there is still input remaining.
        if index < len(values):
            right_value = values[index]
            index += 1

            # If the value is not None, create an actual right child node.
            if right_value is not None:
                current.right = TreeNode(right_value)

                # Add the new child to the queue so its children can be assigned later.
                queue.append(current.right)

    # Return the fully constructed tree root.
    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement:
    # Tree from level-order: [5, 3, 8, 1, None, None, 9]
    #
    # Structure:
    #         5
    #        / \
    #       3   8
    #      /     \
    #     1       9
    #
    # Node 3 has exactly one child.
    # Node 8 has exactly one child.
    # Answer should be 2.
    example_1 = [5, 3, 8, 1, None, None, 9]
    root_1 = build_tree_from_level_order(example_1)
    result_1 = solution.countNodesWithOneChild(root_1)
    print("Example 1 Output:", result_1)  # Expected: 2

    # Example 2 from the problem statement:
    # Tree from level-order: [7, 4, 10, 2, 6, None, None]
    #
    # Structure:
    #         7
    #        / \
    #       4   10
    #      / \
    #     2   6
    #
    # Node 4 has two children -> does not count
    # Node 10 has zero children -> does not count
    # Nodes 2 and 6 are leaves -> do not count
    # Node 7 has two children -> does not count
    # Answer should be 0.
    example_2 = [7, 4, 10, 2, 6, None, None]
    root_2 = build_tree_from_level_order(example_2)
    result_2 = solution.countNodesWithOneChild(root_2)
    print("Example 2 Output:", result_2)  # Expected: 0

    # Additional beginner-friendly test:
    # A single-node tree has no children, so the answer is 0.
    example_3 = [42]
    root_3 = build_tree_from_level_order(example_3)
    result_3 = solution.countNodesWithOneChild(root_3)
    print("Example 3 Output:", result_3)  # Expected: 0

    # Additional test:
    # Tree: [1, 2, None, 3]
    #
    # Structure:
    #     1
    #    /
    #   2
    #  /
    # 3
    #
    # Node 1 has exactly one child.
    # Node 2 has exactly one child.
    # Node 3 has zero children.
    # Answer should be 2.
    example_4 = [1, 2, None, 3]
    root_4 = build_tree_from_level_order(example_4)
    result_4 = solution.countNodesWithOneChild(root_4)
    print("Example 4 Output:", result_4)  # Expected: 2)