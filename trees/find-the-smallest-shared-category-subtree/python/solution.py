"""
Title: Find the Smallest Shared Category Subtree

Problem Description:
You are given the root of a binary tree representing a product category hierarchy.
Each node contains an integer category ID, and IDs are unique within the tree.
You are also given a list of distinct target category IDs that correspond to
categories selected by different teams. Your task is to return the category ID
of the smallest subtree that contains all target categories. A subtree is
considered rooted at some node and includes that node and all of its descendants.

If all target categories appear in the tree, return the ID of the lowest node
whose subtree contains every target ID. If one or more target IDs do not exist
in the tree, return -1.

This problem is not asking for the path between nodes. Instead, you must identify
the single subtree root that covers all requested categories as tightly as possible.
The selected node may itself be one of the target categories.

You should design an algorithm that works efficiently for large trees.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, Dict, List, Optional, Set, Tuple


class TreeNode:
    """Binary tree node used for the category hierarchy."""

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
            left: Left child.
            right: Right child.

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
    def smallest_shared_category_subtree(
        self, root: Optional[TreeNode], targets: List[int]
    ) -> int:
        """
        Return the value of the smallest subtree root that contains all target values.

        The algorithm performs a post-order traversal. For each node, it computes
        how many target values exist inside that node's subtree. The first node
        encountered from the bottom whose subtree count equals the total number
        of targets is the lowest node containing all targets.

        If one or more target values do not appear anywhere in the tree, return -1.

        Args:
            root: Root of the binary tree.
            targets: Distinct target category IDs to be covered by one subtree.

        Returns:
            The category ID of the smallest subtree root containing all targets,
            or -1 if any target is missing from the tree.

        Time complexity:
            O(n), where n is the number of nodes in the tree.

        Space complexity:
            O(n), due to the explicit traversal stack in the iterative post-order
            traversal and the set of target values.
        """
        # If the tree is empty, there is no subtree that can contain anything.
        if root is None:
            return -1

        # Convert the list of targets into a set.
        #
        # Why use a set?
        # - Membership checks like "is this node value a target?" become O(1) average time.
        # - The problem guarantees targets are distinct, so a set is a natural fit.
        target_set: Set[int] = set(targets)

        # Store the total number of target values we must find in the tree.
        total_targets: int = len(target_set)

        # This variable will hold the answer node once we discover the lowest subtree
        # that contains all target values.
        #
        # Important detail:
        # We use a post-order traversal (left, right, node). That means children are
        # processed before their parent. Therefore, the first node we find whose subtree
        # contains all targets is automatically the LOWEST such node.
        answer_node: Optional[TreeNode] = None

        # We will do an iterative post-order traversal instead of recursion.
        #
        # Why iterative?
        # - The tree can contain up to 100000 nodes.
        # - A recursive DFS on a very deep or highly unbalanced tree may exceed Python's
        #   recursion limit and crash with RecursionError.
        #
        # Stack entries are tuples:
        #   (node, visited_flag)
        #
        # Meaning:
        # - visited_flag == False:
        #     We are seeing this node for the first time. We still need to process
        #     its children before computing its subtree information.
        # - visited_flag == True:
        #     Both children have already been handled, so now we can compute how many
        #     target values are in this node's subtree.
        stack: List[Tuple[Optional[TreeNode], bool]] = [(root, False)]

        # This dictionary maps a node object to the number of target values found
        # in that node's subtree.
        #
        # Example:
        # subtree_target_count[some_node] = 3
        # means that exactly 3 target IDs appear somewhere under that node,
        # including the node itself if applicable.
        subtree_target_count: Dict[TreeNode, int] = {}

        # This counter tracks how many target values were found anywhere in the tree.
        #
        # At the end:
        # - if found_targets_in_tree == total_targets, then every target exists
        # - otherwise, at least one target is missing, so answer must be -1
        found_targets_in_tree: int = 0

        # Standard iterative post-order traversal loop.
        while stack:
            node, visited = stack.pop()

            # Ignore null children. This keeps the rest of the logic simpler.
            if node is None:
                continue

            if not visited:
                # First time we see this node.
                #
                # Because we want post-order, we push:
                # 1) the current node back with visited=True
                # 2) right child
                # 3) left child
                #
                # Since the stack is LIFO, left and right children will be processed
                # before the node itself.
                stack.append((node, True))
                stack.append((node.right, False))
                stack.append((node.left, False))
            else:
                # Now both children have already been processed, so their subtree counts
                # are available in the dictionary.
                left_count: int = subtree_target_count.get(node.left, 0)
                right_count: int = subtree_target_count.get(node.right, 0)

                # Start with the total targets found in the left and right subtrees.
                current_count: int = left_count + right_count

                # If the current node's value itself is one of the targets, add 1.
                if node.val in target_set:
                    current_count += 1

                    # This increments the global count of how many target values
                    # actually appear somewhere in the tree.
                    #
                    # Because node values are unique in the tree and targets are distinct,
                    # each target can be counted at most once.
                    found_targets_in_tree += 1

                # Save the computed subtree target count for this node.
                subtree_target_count[node] = current_count

                # If this subtree contains all target values, and we have not already
                # chosen an answer, then this node is the lowest valid answer.
                #
                # Why is the first such node the correct one?
                # - We are processing in post-order.
                # - That means deeper nodes are evaluated before higher nodes.
                # - So the first node whose subtree contains all targets must be the
                #   smallest / lowest subtree root that works.
                if current_count == total_targets and answer_node is None:
                    answer_node = node

        # After traversing the entire tree, verify that every target was actually found.
        #
        # If not, the problem requires returning -1.
        if found_targets_in_tree != total_targets:
            return -1

        # If all targets were found, answer_node must be the correct lowest subtree root.
        # The tree is non-empty and targets is non-empty, so answer_node should exist here.
        return answer_node.val if answer_node is not None else -1

    def build_tree_from_level_order(self, values: List[Optional[int]]) -> Optional[TreeNode]:
        """
        Build a binary tree from a level-order list representation.

        The input format uses None to represent missing children, similar to many
        coding platform examples.

        Args:
            values: Level-order list of node values, where None means no node.

        Returns:
            The root of the constructed binary tree, or None if the list is empty
            or starts with None.

        Time complexity:
            O(n), where n is the number of entries in the input list.

        Space complexity:
            O(n), due to the queue used during tree construction.
        """
        # If the input list is empty, there is no tree to build.
        if not values:
            return None

        # If the first value is None, the root does not exist.
        if values[0] is None:
            return None

        # Create the root node from the first value.
        root: TreeNode = TreeNode(values[0])

        # Queue for level-order construction.
        queue: Deque[TreeNode] = deque([root])

        # Index points to the next value in the list that has not yet been used.
        index: int = 1

        # Process nodes level by level, attaching left and right children.
        while queue and index < len(values):
            current: TreeNode = queue.popleft()

            # Try to attach the left child if there is still input remaining.
            if index < len(values):
                left_value: Optional[int] = values[index]
                index += 1

                if left_value is not None:
                    current.left = TreeNode(left_value)
                    queue.append(current.left)

            # Try to attach the right child if there is still input remaining.
            if index < len(values):
                right_value: Optional[int] = values[index]
                index += 1

                if right_value is not None:
                    current.right = TreeNode(right_value)
                    queue.append(current.right)

        return root


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Tree: [10,5,15,3,7,12,18,null,4]
    # Targets: [4,7]
    #
    # Structure:
    #         10
    #        /  \
    #       5    15
    #      / \   / \
    #     3   7 12 18
    #      \
    #       4
    #
    # Smallest subtree containing 4 and 7 is rooted at 5.
    root1 = solution.build_tree_from_level_order([10, 5, 15, 3, 7, 12, 18, None, 4])
    targets1 = [4, 7]
    result1 = solution.smallest_shared_category_subtree(root1, targets1)
    print(result1)  # Expected: 5

    # Example 2:
    # Tree: [8,4,11,2,6,9,13,1,3,5,7]
    # Targets: [1,7,9]
    #
    # Structure:
    #           8
    #         /   \
    #        4     11
    #       / \    / \
    #      2   6  9  13
    #     / \ / \
    #    1  3 5  7
    #
    # Node 1 and 7 are in the left subtree of 8, while 9 is in the right subtree.
    # Therefore, the smallest subtree containing all three is rooted at 8.
    root2 = solution.build_tree_from_level_order([8, 4, 11, 2, 6, 9, 13, 1, 3, 5, 7])
    targets2 = [1, 7, 9]
    result2 = solution.smallest_shared_category_subtree(root2, targets2)
    print(result2)  # Expected: 8

    # Additional check: missing target should return -1.
    root3 = solution.build_tree_from_level_order([1, 2, 3])
    targets3 = [2, 4]
    result3 = solution.smallest_shared_category_subtree(root3, targets3)
    print(result3)  # Expected: -1