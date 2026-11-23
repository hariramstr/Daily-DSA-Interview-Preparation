"""
Title: Count Hallways With Even Door Endpoints

Problem Description:
You are given the root of a binary tree representing a building map. Each node stores
an integer room number. A hallway is defined as any parent node together with its
existing direct child connections. A hallway is considered valid if the parent room
number is even and every existing child of that parent also has an even room number.
Leaf nodes do not count as hallways because they have no child connections.

Return the number of valid hallways in the tree.

In other words, for every non-leaf node, check whether its value is even and whether
all of its present children (left child, right child, or both) also have even values.
Count how many nodes satisfy this rule.

This is an easy tree traversal problem. A simple DFS or BFS is enough because each
node only needs to be checked once.

Constraints:
- The number of nodes in the tree is in the range [1, 1000].
- -10^4 <= Node.val <= 10^4
- The tree is a binary tree.

Example 1:
Input: root = [8,4,6,2,7,null,10]
Output: 2
Explanation:
- Node 8 forms a valid hallway because 8, 4, and 6 are all even.
- Node 6 also forms a valid hallway because its existing child 10 is even.
- Node 4 does not qualify because one of its children is 7, which is odd.

Example 2:
Input: root = [5,2,8,4,6]
Output: 1
Explanation:
- Node 5 is odd, so it cannot form a valid hallway.
- Node 2 forms a valid hallway because both of its children, 4 and 6, are even.
- The other nodes are leaves.
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

    Time complexity:
        O(1)

    Space complexity:
        O(1)
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
    def count_even_hallways(self, root: Optional[TreeNode]) -> int:
        """Count non-leaf nodes whose value and all existing children values are even.

        We traverse the tree once using depth-first search. For each node, we check:
        1. Whether it is a non-leaf node (has at least one child)
        2. Whether the node's value is even
        3. Whether every existing child also has an even value

        If all conditions are true, we add 1 to the answer.

        Args:
            root: Root of the binary tree.

        Returns:
            The number of valid hallways in the tree.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node is visited once.

        Space complexity:
            O(h), where h is the height of the tree due to recursion stack.
            In the worst case of a skewed tree, this is O(n).
        """

        def dfs(node: Optional[TreeNode]) -> int:
            """Recursively count valid hallways in the subtree rooted at node.

            Args:
                node: Current tree node being processed.

            Returns:
                Number of valid hallways in this subtree.

            Time complexity:
                O(k), where k is the number of nodes in this subtree.

            Space complexity:
                O(h), where h is the subtree height due to recursion.
            """
            # Base case:
            # If the current node does not exist, there is nothing to count.
            if node is None:
                return 0

            # Recursively count valid hallways in the left subtree.
            # This ensures every node on the left side is processed exactly once.
            left_count: int = dfs(node.left)

            # Recursively count valid hallways in the right subtree.
            # This ensures every node on the right side is processed exactly once.
            right_count: int = dfs(node.right)

            # Start with the total count gathered from both subtrees.
            total: int = left_count + right_count

            # A hallway must be a parent with at least one existing child.
            # Therefore, leaf nodes are explicitly excluded.
            has_child: bool = node.left is not None or node.right is not None

            if has_child:
                # First requirement:
                # The parent node itself must contain an even value.
                parent_is_even: bool = node.val % 2 == 0

                # Second requirement:
                # Every existing child must also contain an even value.
                #
                # We only check children that actually exist.
                # Missing children do not hurt validity because the problem says
                # "every existing child" must be even.
                left_is_valid: bool = True
                right_is_valid: bool = True

                if node.left is not None:
                    left_is_valid = node.left.val % 2 == 0

                if node.right is not None:
                    right_is_valid = node.right.val % 2 == 0

                # If the parent is even and all present children are even,
                # then this node forms a valid hallway.
                if parent_is_even and left_is_valid and right_is_valid:
                    total += 1

            # Return the total number of valid hallways found in this subtree.
            return total

        return dfs(root)


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """Build a binary tree from a level-order list representation.

    The input format follows the common array-style binary tree representation:
    - Each value corresponds to a node in level order
    - None means that position has no node

    Example:
        [8, 4, 6, 2, 7, None, 10]

    Args:
        level_order: List of node values in level-order form.

    Returns:
        The root of the constructed binary tree.

    Time complexity:
        O(n), where n is the number of items in the input list.

    Space complexity:
        O(n), due to the queue used during construction.
    """
    # If the input list is empty, there is no tree to build.
    if not level_order:
        return None

    # If the first value is None, the tree is empty.
    if level_order[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(level_order[0])

    # We use a queue to attach children level by level.
    # This matches the level-order input format naturally.
    queue: Deque[TreeNode] = deque([root])

    # Index points to the next value in the level_order list that has not been used yet.
    index: int = 1

    # Continue until we either run out of parent nodes to process
    # or consume all input values.
    while queue and index < len(level_order):
        # Take the next parent node from the queue.
        current: TreeNode = queue.popleft()

        # Try to assign the left child if there is still input remaining.
        if index < len(level_order):
            left_value: Optional[int] = level_order[index]
            index += 1

            # Only create a child node when the value is not None.
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Try to assign the right child if there is still input remaining.
        if index < len(level_order):
            right_value: Optional[int] = level_order[index]
            index += 1

            # Only create a child node when the value is not None.
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Tree: [8,4,6,2,7,None,10]
    #
    # Structure:
    #         8
    #       /   \
    #      4     6
    #     / \     \
    #    2   7     10
    #
    # Valid hallways:
    # - Node 8: even, children 4 and 6 are even -> valid
    # - Node 4: child 7 is odd -> invalid
    # - Node 6: existing child 10 is even -> valid
    #
    # Expected answer: 2
    example_1: List[Optional[int]] = [8, 4, 6, 2, 7, None, 10]
    root_1: Optional[TreeNode] = build_tree(example_1)
    result_1: int = solution.count_even_hallways(root_1)
    print("Example 1 Output:", result_1)

    # Example 2:
    # Tree: [5,2,8,4,6]
    #
    # Structure:
    #         5
    #       /   \
    #      2     8
    #     / \
    #    4   6
    #
    # Valid hallways:
    # - Node 5: odd -> invalid
    # - Node 2: even, children 4 and 6 are even -> valid
    #
    # Expected answer: 1
    example_2: List[Optional[int]] = [5, 2, 8, 4, 6]
    root_2: Optional[TreeNode] = build_tree(example_2)
    result_2: int = solution.count_even_hallways(root_2)
    print("Example 2 Output:", result_2)

    # Additional small sanity check:
    # Single-node tree has no hallway because the root is a leaf.
    example_3: List[Optional[int]] = [2]
    root_3: Optional[TreeNode] = build_tree(example_3)
    result_3: int = solution.count_even_hallways(root_3)
    print("Single Node Output:", result_3)