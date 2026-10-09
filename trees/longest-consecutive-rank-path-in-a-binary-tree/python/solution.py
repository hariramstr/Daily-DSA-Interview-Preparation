"""
Title: Longest Consecutive Rank Path in a Binary Tree

Problem Description:
You are given the root of a binary tree where each node stores an integer rank.
A valid rank chain is any path that moves only through parent-child edges, and at
every step the rank must change by exactly 1. The path may be strictly increasing,
strictly decreasing, or switch direction once at a middle node by combining a
decreasing side and an increasing side.

The path:
- does not need to start at the root
- does not need to end at a leaf
- may go from one descendant up through a node and down to another descendant
- is valid as long as every adjacent pair differs by exactly 1

Return the length of the longest valid rank chain in the tree, measured as the
number of nodes in the path.

Examples:
1) root = [4,3,5,2,null,null,6,1]
   Output: 6
   Explanation: The longest valid path is 1 -> 2 -> 3 -> 4 -> 5 -> 6

2) root = [10,9,11,8,10,null,12]
   Output: 5
   Explanation: One longest valid path is 8 -> 9 -> 10 -> 11 -> 12

Constraints:
- The number of nodes is in the range [1, 100000]
- -1000000000 <= Node.val <= 1000000000
- The tree is a binary tree
- The solution should run in O(n) time
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class TreeNode:
    """Binary tree node.

    Args:
        val: Integer value stored in the node.
        left: Left child.
        right: Right child.
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
    def longestConsecutive(self, root: Optional[TreeNode]) -> int:
        """Return the length of the longest valid consecutive path in the tree.

        The path may:
        - strictly increase by 1 at each step
        - strictly decrease by 1 at each step
        - combine one decreasing side and one increasing side through a middle node

        The key idea:
        For every node, compute two values:
        1. inc: longest downward path starting at this node where child = node.val + 1
        2. dec: longest downward path starting at this node where child = node.val - 1

        Then, at each node, combine:
        - dec from one side
        - the current node
        - inc from the other side

        This gives a path shaped like:
            larger ... -> node -> ... smaller
        or the reverse direction when read from the other end.

        Args:
            root: Root of the binary tree.

        Returns:
            Length of the longest valid rank chain.

        Time complexity:
            O(n), because each node is processed exactly once.

        Space complexity:
            O(h), where h is the tree height due to recursion stack.
            In the worst case of a skewed tree, this can be O(n).
        """
        if root is None:
            return 0

        # This variable stores the best answer found anywhere in the tree.
        # We update it while processing each node.
        self.best: int = 0

        def dfs(node: Optional[TreeNode]) -> Tuple[int, int]:
            """Depth-first search that returns increasing and decreasing lengths.

            For the current node, return:
            - inc: longest downward chain starting at node where values increase by 1
            - dec: longest downward chain starting at node where values decrease by 1

            Example:
                If node.val = 5 and a child has value 6, that child can extend inc.
                If node.val = 5 and a child has value 4, that child can extend dec.

            We process children first, because the current node's answer depends on
            the best chains already computed for its left and right subtrees.

            Args:
                node: Current tree node.

            Returns:
                A tuple (inc, dec).

            Time complexity:
                O(size of subtree rooted at node) overall across all calls is O(n).

            Space complexity:
                O(h) recursion stack.
            """
            # Base case:
            # An empty node contributes no path length.
            # Returning (0, 0) makes parent logic simple and safe.
            if node is None:
                return 0, 0

            # Recursively compute results for left and right children first.
            # This is post-order traversal: left, right, node.
            left_inc, left_dec = dfs(node.left)
            right_inc, right_dec = dfs(node.right)

            # Every single node by itself forms a valid path of length 1.
            # So both increasing and decreasing chains start at 1.
            inc: int = 1
            dec: int = 1

            # -----------------------------
            # Process the left child
            # -----------------------------
            if node.left is not None:
                # If left child is exactly one greater than current node,
                # then current node can start an increasing chain through left child.
                #
                # Example:
                #   node = 4, left = 5
                #   Then 4 -> 5 is increasing by 1.
                #
                # We extend using left_inc because that child already knows the best
                # increasing chain continuing downward from itself.
                if node.left.val == node.val + 1:
                    inc = max(inc, 1 + left_inc)

                # If left child is exactly one smaller than current node,
                # then current node can start a decreasing chain through left child.
                #
                # Example:
                #   node = 4, left = 3
                #   Then 4 -> 3 is decreasing by 1.
                #
                # We extend using left_dec because that child already knows the best
                # decreasing chain continuing downward from itself.
                elif node.left.val == node.val - 1:
                    dec = max(dec, 1 + left_dec)

            # -----------------------------
            # Process the right child
            # -----------------------------
            if node.right is not None:
                # Same logic as the left child:
                # if right child is one greater, it can extend an increasing chain.
                if node.right.val == node.val + 1:
                    inc = max(inc, 1 + right_inc)

                # if right child is one smaller, it can extend a decreasing chain.
                elif node.right.val == node.val - 1:
                    dec = max(dec, 1 + right_dec)

            # ---------------------------------------------------------
            # Combine decreasing and increasing chains through this node
            # ---------------------------------------------------------
            # Why combine dec + inc - 1?
            #
            # - dec counts a chain starting at this node and going downward where
            #   values decrease by 1 each step.
            # - inc counts a chain starting at this node and going downward where
            #   values increase by 1 each step.
            #
            # If we connect one side of dec and one side of inc through this node,
            # the current node gets counted in both chains, so subtract 1 once.
            #
            # Example:
            #   1 -> 2 -> 3 -> 4 -> 5 -> 6
            # At node 4:
            #   dec might represent 4 -> 3 -> 2 -> 1  (length 4)
            #   inc might represent 4 -> 5 -> 6       (length 3)
            # Combined length = 4 + 3 - 1 = 6
            #
            # This exactly matches the problem's "switch direction once" rule.
            self.best = max(self.best, inc + dec - 1)

            # Return the best increasing and decreasing chains starting at this node
            # so the parent can potentially extend them.
            return inc, dec

        dfs(root)
        return self.best


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """Build a binary tree from a level-order list representation.

    The input format uses None for missing children.

    Example:
        [4, 3, 5, 2, None, None, 6, 1]

    Builds:
              4
             / \
            3   5
           /     \
          2       6
         /
        1

    Args:
        level_order: Tree values in level-order.

    Returns:
        Root of the constructed binary tree.

    Time complexity:
        O(n), where n is the number of items in the list.

    Space complexity:
        O(n), for the queue and created nodes.
    """
    if not level_order:
        return None

    if level_order[0] is None:
        return None

    root = TreeNode(level_order[0])
    queue: Deque[TreeNode] = deque([root])
    index = 1

    while queue and index < len(level_order):
        current = queue.popleft()

        # Read and attach the left child if present.
        if index < len(level_order):
            left_value = level_order[index]
            index += 1
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Read and attach the right child if present.
        if index < len(level_order):
            right_value = level_order[index]
            index += 1
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create a Solution instance.
    solver = Solution()

    # Example 1:
    # Tree: [4,3,5,2,None,None,6,1]
    # Longest path: 1 -> 2 -> 3 -> 4 -> 5 -> 6
    # Expected output: 6
    example_1 = build_tree([4, 3, 5, 2, None, None, 6, 1])
    result_1 = solver.longestConsecutive(example_1)
    print("Example 1 Result:", result_1)

    # Example 2:
    # Tree: [10,9,11,8,10,None,12]
    # One longest path: 8 -> 9 -> 10 -> 11 -> 12
    # Expected output: 5
    example_2 = build_tree([10, 9, 11, 8, 10, None, 12])
    result_2 = solver.longestConsecutive(example_2)
    print("Example 2 Result:", result_2)

    # Additional simple test:
    # Single node tree
    # Expected output: 1
    example_3 = build_tree([42])
    result_3 = solver.longestConsecutive(example_3)
    print("Single Node Result:", result_3)