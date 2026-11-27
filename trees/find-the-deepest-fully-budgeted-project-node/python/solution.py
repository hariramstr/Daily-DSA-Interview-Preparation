"""
Title: Find the Deepest Fully Budgeted Project Node

Problem Description:
You are given the root of a binary tree representing a hierarchy of project tasks.
Each node stores an integer budget value. A project node is called fully budgeted
if the sum of all values in its left subtree is exactly equal to the sum of all
values in its right subtree. An empty subtree has sum 0.

Your task is to return the value of the deepest fully budgeted node in the tree.
If multiple fully budgeted nodes exist at the same deepest depth, return the one
that appears leftmost in the tree. If no such node exists, return -1.

Depth is measured from the root, where the root has depth 0. A leaf node is fully
budgeted because both its left and right subtree sums are 0.

You should design an algorithm that works efficiently for trees with up to 100,000
nodes, so repeated subtree-sum recomputation from every node will be too slow.
A postorder traversal is useful because it allows us to compute subtree sums while
also tracking candidate answers by depth.

Constraints:
- The number of nodes in the tree is in the range [1, 100000]
- -100000 <= Node.val <= 100000
- The tree is binary and may be unbalanced
- Return the node value, not the node reference
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class TreeNode:
    """Binary tree node."""

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
    def deepestFullyBudgetedNode(self, root: Optional[TreeNode]) -> int:
        """
        Return the value of the deepest fully budgeted node.

        A node is fully budgeted when the sum of values in its left subtree
        equals the sum of values in its right subtree. If multiple such nodes
        exist at the same deepest depth, the leftmost one is returned.

        Args:
            root: Root of the binary tree.

        Returns:
            The value of the deepest fully budgeted node, or -1 if no such node exists.

        Time complexity:
            O(n), where n is the number of nodes in the tree, because each node
            is processed a constant number of times.

        Space complexity:
            O(n), due to the explicit traversal stack used for iterative postorder
            traversal. This avoids recursion depth issues on very deep trees.
        """
        if root is None:
            return -1

        # We must support trees with up to 100,000 nodes.
        # A recursive DFS could hit Python's recursion limit on a highly unbalanced tree,
        # so we use an iterative postorder traversal instead.
        #
        # Why postorder?
        # Because to decide whether a node is fully budgeted, we need:
        #   1. the sum of its left subtree
        #   2. the sum of its right subtree
        # Those values are only known after both children have already been processed.
        #
        # A standard iterative postorder pattern is:
        #   stack entries = (node, depth, visited_flag)
        #   - first time we see a node: push it back as visited, then push right child,
        #     then left child
        #   - second time we see it (visited=True): both children are already processed,
        #     so we can compute this node's subtree sum
        #
        # We also track the best answer found so far:
        #   best_depth = deepest depth of any fully budgeted node seen so far
        #   best_value = node value for that best candidate
        #
        # Leftmost tie-breaking:
        # Since we push right child before left child, the left subtree is processed first.
        # Therefore, among nodes at the same depth, the leftmost fully budgeted node will
        # be encountered first. To preserve leftmost behavior, we only update the answer
        # when we find a strictly deeper node, not when depth is equal.
        best_depth: int = -1
        best_value: int = -1

        # This dictionary stores the total subtree sum for each processed node.
        # subtree_sum[node] = sum of all values in the subtree rooted at node
        #
        # We use the node object itself as the key. Python objects are hashable by identity
        # unless custom equality/hash behavior is defined, which is not the case here.
        subtree_sum: dict[TreeNode, int] = {}

        # Iterative postorder traversal stack.
        stack: List[Tuple[Optional[TreeNode], int, bool]] = [(root, 0, False)]

        while stack:
            node, depth, visited = stack.pop()

            # Ignore null children. Treating them here keeps the main logic simple.
            if node is None:
                continue

            if not visited:
                # First time visiting this node.
                #
                # We are not ready to compute its subtree sum yet, because its children
                # have not been processed. So:
                #   1. push this node back with visited=True
                #   2. push right child
                #   3. push left child
                #
                # Because the stack is LIFO, left child will be processed before right child.
                # That helps preserve the "leftmost" tie-breaking naturally.
                stack.append((node, depth, True))
                stack.append((node.right, depth + 1, False))
                stack.append((node.left, depth + 1, False))
            else:
                # Second time visiting this node.
                #
                # At this point, both children have already been processed, so their subtree
                # sums are available in the dictionary. If a child is missing, its subtree
                # sum is 0 because an empty subtree has sum 0.
                left_sum: int = subtree_sum.get(node.left, 0)
                right_sum: int = subtree_sum.get(node.right, 0)

                # A node is fully budgeted exactly when the left and right subtree sums match.
                if left_sum == right_sum:
                    # We only replace the answer if this node is deeper than the current best.
                    #
                    # Important for correctness:
                    # - If depth is greater, this is definitely a better answer.
                    # - If depth is equal, we do NOT replace, because the first node found
                    #   at that depth is the leftmost one due to traversal order.
                    if depth > best_depth:
                        best_depth = depth
                        best_value = node.val

                # Compute and store the total subtree sum rooted at this node.
                # This value will be used by this node's parent later.
                subtree_sum[node] = node.val + left_sum + right_sum

        return best_value


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common convention:
    - Each list element represents a node value in level order.
    - None means the node is missing.

    Args:
        level_order: List of node values in level-order form.

    Returns:
        The root of the constructed binary tree.

    Time complexity:
        O(n), where n is the number of list elements.

    Space complexity:
        O(n), for the queue used during construction.
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

        if index < len(level_order):
            left_value = level_order[index]
            index += 1
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        if index < len(level_order):
            right_value = level_order[index]
            index += 1
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Tree: [8,4,6,2,2,3,3]
    #
    # Structure:
    #         8
    #       /   \
    #      4     6
    #     / \   / \
    #    2   2 3   3
    #
    # Fully budgeted nodes:
    # - All leaves are fully budgeted because 0 == 0
    # - Node 4 is fully budgeted because left subtree sum = 2, right subtree sum = 2
    # - Node 6 is fully budgeted because left subtree sum = 3, right subtree sum = 3
    # - Root 8 is NOT fully budgeted because left subtree sum = 4+2+2 = 8,
    #   right subtree sum = 6+3+3 = 12
    #
    # Deepest fully budgeted depth is 2 (the leaves), and the leftmost leaf there has value 2.
    root1 = build_tree([8, 4, 6, 2, 2, 3, 3])
    print(solution.deepestFullyBudgetedNode(root1))  # Expected: 2

    # Example 2
    # Tree: [5,1,7,None,None,4,3]
    #
    # Structure:
    #         5
    #       /   \
    #      1     7
    #           / \
    #          4   3
    #
    # Fully budgeted nodes:
    # - Leaves 1, 4, 3 are fully budgeted
    # - Node 7 is NOT fully budgeted because 4 != 3
    # - Root 5 is NOT fully budgeted because left subtree sum = 1, right subtree sum = 14
    #
    # Deepest fully budgeted nodes are 4 and 3 at depth 2, and the leftmost is 4.
    root2 = build_tree([5, 1, 7, None, None, 4, 3])
    print(solution.deepestFullyBudgetedNode(root2))  # Expected: 4

    # Additional quick sanity checks
    root3 = build_tree([1])
    print(solution.deepestFullyBudgetedNode(root3))  # Expected: 1

    root4 = build_tree([0, 0, 0])
    print(solution.deepestFullyBudgetedNode(root4))  # Expected: 0