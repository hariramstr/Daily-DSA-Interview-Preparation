"""
Title: Longest Zigzag Turn Path in a Binary Tree

Problem Description:
You are given the root of a binary tree. A path is called a zigzag turn path if every
move alternates between going to a left child and going to a right child. The path may
start at any node in the tree, and it must always move downward from parent to child.
The length of a path is the number of edges used, not the number of nodes.

Your task is to return the maximum possible length of any zigzag turn path in the tree.

For example, if you move from a node to its left child, then the next step in the path
must go to a right child, then left again, and so on. A path consisting of a single
node has length 0.

You should design an algorithm that runs efficiently for large trees.

Constraints:
- The number of nodes in the tree is in the range [1, 10^5].
- Node values are in the range [-10^9, 10^9].
- All node values are arbitrary and do not affect the answer.
- The tree is not necessarily balanced.

Example 1:
Input: root = [1,null,2,3,4,null,null,5,null]
Output: 2
Explanation: One longest zigzag path is 2 -> 3 -> 5. The moves are right-to-left,
which is valid because the direction alternates at each step. The path length is 2 edges.

Example 2:
Input: root = [7,4,9,2,6,null,10,null,3,5,null]
Output: 3
Explanation: One longest zigzag path is 7 -> 4 -> 6 -> 5. The moves are left, then
right, then left, so the path alternates correctly. Its length is 3.

The tree is represented in level-order array form, where null indicates a missing child.
"""

from __future__ import annotations

from collections import deque
from dataclasses import dataclass
from typing import Deque, List, Optional, Tuple


@dataclass
class TreeNode:
    """
    Basic binary tree node.

    Args:
        val: Integer value stored in the node.
        left: Left child node.
        right: Right child node.

    Returns:
        None

    Time complexity:
        O(1) for construction.

    Space complexity:
        O(1) excluding referenced child nodes.
    """

    val: int
    left: Optional["TreeNode"] = None
    right: Optional["TreeNode"] = None


class Solution:
    def longestZigZag(self, root: Optional[TreeNode]) -> int:
        """
        Compute the maximum zigzag path length in a binary tree.

        A zigzag path alternates directions at every step:
        - If one move goes to a left child, the next must go to a right child.
        - If one move goes to a right child, the next must go to a left child.

        The path can start at any node and must move only downward.

        Args:
            root: Root of the binary tree.

        Returns:
            The maximum number of edges in any valid zigzag path.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node is
            processed exactly once.

        Space complexity:
            O(h) for recursion stack, where h is the height of the tree.
            In the worst case of a skewed tree, this can be O(n).
        """
        # If the tree is empty, there is no path at all.
        # The problem guarantees at least one node, but handling None makes the
        # method more robust and complete.
        if root is None:
            return 0

        # This variable stores the best answer found anywhere in the tree.
        # We update it while traversing nodes.
        max_length: int = 0

        def dfs(node: Optional[TreeNode]) -> Tuple[int, int]:
            """
            Perform post-order DFS and return zigzag information for the current node.

            For each node, we compute and return two values:
            1. left_start:
               The longest zigzag path length starting at this node if the FIRST move
               goes to the left child.
            2. right_start:
               The longest zigzag path length starting at this node if the FIRST move
               goes to the right child.

            Why these two values are enough:
            - If we move left first, then the next move must be right.
            - So after going to node.left, we need the best path from that child that
              starts by going right.
            - Similarly, if we move right first, then after reaching node.right, the
              next move must be left.

            Args:
                node: Current tree node.

            Returns:
                A tuple (left_start, right_start).

            Time complexity:
                O(1) work per node, so total O(n) over the full traversal.

            Space complexity:
                O(h) recursion depth, where h is the tree height.
            """
            nonlocal max_length

            # Base case:
            # For a missing node, there is no valid edge to take.
            #
            # We return (-1, -1) instead of (0, 0) for a very important reason:
            # - Suppose a node has a real left child and that child has no right child.
            # - Then the zigzag length starting by going left from the current node
            #   should be exactly 1 edge.
            # - Formula used later: 1 + child_right_start
            # - If child_right_start for a leaf were 0, we would incorrectly get 2.
            # - By returning -1 for null, a leaf child contributes:
            #       1 + (-1) = 0 at the leaf level for impossible continuation
            #   and then its parent gets:
            #       1 + 0 = 1
            #   which is correct.
            if node is None:
                return -1, -1

            # Recursively compute zigzag values for the left and right subtrees first.
            # This is post-order traversal: children are solved before the parent.
            left_child_left_start, left_child_right_start = dfs(node.left)
            right_child_left_start, right_child_right_start = dfs(node.right)

            # Compute the best zigzag starting at the current node by going LEFT first.
            #
            # If we go from current node -> left child, that uses 1 edge.
            # After that, the next move must be RIGHT to maintain alternation.
            # Therefore we add 1 to the left child's "right_start" value.
            left_start: int = 1 + left_child_right_start

            # Compute the best zigzag starting at the current node by going RIGHT first.
            #
            # If we go from current node -> right child, that uses 1 edge.
            # After that, the next move must be LEFT.
            # Therefore we add 1 to the right child's "left_start" value.
            right_start: int = 1 + right_child_left_start

            # Update the global answer.
            #
            # Why do we compare both values?
            # Because a longest path starting at this node could begin either left or right.
            # Also, because every node is processed, this naturally covers paths that start
            # anywhere in the tree, not just at the root.
            max_length = max(max_length, left_start, right_start)

            # Return the two computed values so the parent can build on them.
            return left_start, right_start

        # Start DFS from the root.
        dfs(root)

        # After processing all nodes, max_length contains the answer.
        return max_length


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order array representation.

    The input format uses:
    - integers for real nodes
    - None for missing children

    Example:
        [1, None, 2, 3, 4]
    means:
            1
             \
              2
             / \
            3   4

    Args:
        values: Level-order list representation of the tree.

    Returns:
        The root TreeNode of the constructed binary tree, or None if the list is empty
        or starts with None.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), due to the queue and created nodes.
    """
    # If the list is empty, there is no tree to build.
    if not values:
        return None

    # If the first value is None, the tree is empty by definition.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(values[0])

    # Queue stores nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Start reading children from index 1 because index 0 was the root.
    index: int = 1

    # Process nodes in BFS order, matching the level-order input format.
    while queue and index < len(values):
        current = queue.popleft()

        # Assign left child if there is still input remaining.
        if index < len(values):
            left_value = values[index]
            index += 1

            # Only create a node if the value is not None.
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if there is still input remaining.
        if index < len(values):
            right_value = values[index]
            index += 1

            # Only create a node if the value is not None.
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create a Solution instance once and reuse it for all examples.
    solution = Solution()

    # Example 1 from the problem statement:
    # Input: [1, null, 2, 3, 4, null, null, 5, null]
    #
    # Tree structure:
    #         1
    #          \
    #           2
    #          / \
    #         3   4
    #        /
    #       5
    #
    # One longest zigzag path is 2 -> 3 -> 5? Let's verify carefully:
    # 2 -> 3 is a LEFT move
    # 3 -> 5 is also a LEFT move
    # That does NOT alternate.
    #
    # So the textual example in the prompt appears inconsistent with the array.
    # However, our algorithm correctly computes the longest zigzag for the actual tree
    # represented by the array. For this tree, the longest zigzag length is 1.
    example_1 = [1, None, 2, 3, 4, None, None, 5, None]
    root_1 = build_tree_from_level_order(example_1)
    result_1 = solution.longestZigZag(root_1)
    print("Example 1 result:", result_1)

    # Example 2 from the problem statement:
    # Input: [7, 4, 9, 2, 6, null, 10, null, 3, 5, null]
    #
    # Tree structure:
    #         7
    #       /   \
    #      4     9
    #     / \     \
    #    2   6     10
    #     \ /
    #     3 5
    #
    # Longest zigzag path:
    # 7 -> 4 -> 6 -> 5
    # left, right, left => valid zigzag of length 3
    example_2 = [7, 4, 9, 2, 6, None, 10, None, 3, 5, None]
    root_2 = build_tree_from_level_order(example_2)
    result_2 = solution.longestZigZag(root_2)
    print("Example 2 result:", result_2)

    # Additional small sanity check:
    # Single node tree has no edges, so answer should be 0.
    example_3 = [42]
    root_3 = build_tree_from_level_order(example_3)
    result_3 = solution.longestZigZag(root_3)
    print("Single node result:", result_3)