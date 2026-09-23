"""
Title: Find the First Manager Whose Team Size Matches Their Depth

Problem Description:
You are given the root of an organizational hierarchy represented as a rooted tree.
Each node stores a unique employee ID, and each employee may have zero or more direct reports.
The root is the CEO at depth 0.

For any employee, define their team size as the total number of employees in that employee's
subtree, including the employee themself.

Your task is to find the employee ID of the shallowest employee whose team size is exactly equal
to their depth. If multiple employees at the same minimum depth satisfy the condition, return the
one that appears first in a left-to-right traversal of the tree based on the given order of children.
If no employee satisfies the condition, return -1.

This problem requires combining depth information from a top-down traversal with subtree size
information from a bottom-up traversal. A correct solution should work efficiently for large
hierarchies and should not recompute subtree sizes repeatedly.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- 0 <= employee ID <= 10^9, and all IDs are unique.
- Each node may have between 0 and 10 children.
- Depth of the root is 0.
- The input tree is guaranteed to be valid and connected.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional, Tuple


class EmployeeNode:
    """
    Represents one employee in the organizational tree.

    Attributes:
        employee_id: Unique ID for the employee.
        children: Direct reports of this employee.
    """

    def __init__(self, employee_id: int, children: Optional[List["EmployeeNode"]] = None) -> None:
        self.employee_id: int = employee_id
        self.children: List["EmployeeNode"] = children if children is not None else []


class Solution:
    def find_first_manager_matching_depth(self, root: Optional[EmployeeNode]) -> int:
        """
        Find the shallowest employee whose subtree size equals their depth.

        The method performs two coordinated traversals:
        1. A top-down traversal to record each node's depth and left-to-right order.
        2. A bottom-up computation of subtree sizes using postorder processing.

        We then choose the valid employee with:
        - minimum depth
        - and among those, earliest left-to-right appearance

        Args:
            root: The root of the organizational tree.

        Returns:
            The employee ID of the first qualifying employee, or -1 if none exists.

        Time complexity:
            O(n), where n is the number of nodes in the tree.

        Space complexity:
            O(n), for traversal storage and metadata.
        """
        # If the tree is empty, there is no employee to inspect.
        # The problem guarantees at least one node in valid inputs, but handling
        # None makes the method safer and more reusable.
        if root is None:
            return -1

        # We need both depth information and subtree sizes.
        #
        # A recursive DFS could compute subtree sizes naturally, but the constraint
        # allows up to 100000 nodes. Deep recursion in Python can hit recursion limits.
        #
        # To avoid recursion issues, we use an iterative traversal strategy:
        # - First, perform a DFS-like traversal with an explicit stack.
        # - Record:
        #   * depth of each node
        #   * left-to-right visitation order
        #   * a postorder list foundation by storing nodes in traversal order
        #
        # Later, we process the nodes in reverse traversal order so that children
        # are handled before parents, which lets us compute subtree sizes bottom-up.
        stack: List[Tuple[EmployeeNode, int]] = [(root, 0)]

        # traversal_order stores nodes in preorder (root before children).
        # Reversing this list gives a valid bottom-up processing order for subtree sizes.
        traversal_order: List[EmployeeNode] = []

        # depths[i] conceptually belongs to traversal_order[i], but for easier direct access
        # we store depth on the node object externally via parallel metadata lists/maps.
        #
        # Since custom objects are hashable by identity by default, we can safely use them
        # as dictionary keys.
        depth_map: dict[EmployeeNode, int] = {}

        # left_to_right_index records the exact order in which nodes are first seen
        # in a left-to-right preorder traversal.
        #
        # This is important because if multiple valid employees exist at the same minimum
        # depth, we must return the one that appears first from left to right.
        left_to_right_index: dict[EmployeeNode, int] = {}

        visit_counter: int = 0

        while stack:
            node, depth = stack.pop()

            # Record this node's depth.
            depth_map[node] = depth

            # Record its left-to-right appearance index.
            left_to_right_index[node] = visit_counter
            visit_counter += 1

            # Save node into traversal order.
            traversal_order.append(node)

            # Important detail:
            # We want a left-to-right traversal based on the given child order.
            # Because a stack is LIFO, we must push children in reverse order.
            # That way, the leftmost child is processed first when popped.
            for child in reversed(node.children):
                stack.append((child, depth + 1))

        # subtree_size[node] will store the total number of employees in node's subtree,
        # including the node itself.
        subtree_size: dict[EmployeeNode, int] = {}

        # Process nodes in reverse preorder.
        #
        # Why this works:
        # In preorder, a parent is visited before its descendants.
        # Therefore, reversing that order ensures descendants are processed before parents.
        # By the time we compute a parent's subtree size, all child subtree sizes are ready.
        for node in reversed(traversal_order):
            # Every node contributes 1 for itself.
            total_size: int = 1

            # Add the subtree sizes of all direct reports.
            for child in node.children:
                total_size += subtree_size[child]

            subtree_size[node] = total_size

        # Now scan nodes in left-to-right preorder order.
        #
        # Because traversal_order is already in preorder left-to-right order:
        # - the first time we encounter a valid node at the minimum depth,
        #   it is automatically the correct answer.
        #
        # To guarantee "shallowest employee", we track the best depth found so far.
        best_depth: Optional[int] = None
        best_employee_id: int = -1

        for node in traversal_order:
            current_depth: int = depth_map[node]
            current_subtree_size: int = subtree_size[node]

            # Check the required condition:
            # team size == depth
            if current_subtree_size == current_depth:
                # If this is the first valid node, it becomes the current best.
                if best_depth is None:
                    best_depth = current_depth
                    best_employee_id = node.employee_id
                # If we somehow find another valid node at a smaller depth,
                # it should replace the current answer.
                #
                # In practice, because traversal_order is preorder and depth can go up/down,
                # this check is still necessary for correctness.
                elif current_depth < best_depth:
                    best_depth = current_depth
                    best_employee_id = node.employee_id
                # If current_depth == best_depth, we do NOT replace the answer,
                # because traversal_order is already left-to-right and the earlier one
                # must be kept.

        return best_employee_id


def build_tree_from_nested_list(data: List) -> Optional[EmployeeNode]:
    """
    Build an EmployeeNode tree from a nested list representation.

    Expected format:
        [employee_id, child1, child2, child3, ...]
    where each child is itself either:
        - [] meaning no child / empty placeholder
        - another nested list in the same format

    This helper is designed to support the sample-style inputs shown in the prompt.
    It is intentionally beginner-friendly rather than overly compact.

    Args:
        data: Nested list representation of the tree.

    Returns:
        The root EmployeeNode, or None if the input is empty.

    Time complexity:
        O(n), where n is the number of actual nodes represented.

    Space complexity:
        O(n), for the constructed tree and recursion stack proportional to height.
    """
    # Empty input means no node.
    if not data:
        return None

    # The first element is the employee ID for the current node.
    employee_id: int = data[0]
    node = EmployeeNode(employee_id)

    # Every remaining element is interpreted as one child description.
    #
    # The prompt examples include [] entries. We treat [] as "no node here" and skip them.
    # This makes the helper flexible enough to parse the provided examples.
    for child_data in data[1:]:
        if child_data == []:
            continue

        # If the child is given as a plain integer, create a leaf node.
        # This is not the main expected format, but supporting it makes the helper
        # more robust for manual testing.
        if isinstance(child_data, int):
            node.children.append(EmployeeNode(child_data))
        else:
            child_node = build_tree_from_nested_list(child_data)
            if child_node is not None:
                node.children.append(child_node)

    return node


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt is written in a compact custom notation:
    # root = [10, [20, 30], [40], [], [50, 60], [], []]
    #
    # That notation is ambiguous if interpreted literally as a standard nested tree,
    # so below we construct a concrete tree that matches the explanation:
    #
    #         10
    #       /    \
    #     20      40
    #    /       /
    #   30      50
    #             \
    #              60
    #
    # Depths:
    # 10 -> 0
    # 20, 40 -> 1
    # 30, 50 -> 2
    # 60 -> 3
    #
    # Subtree sizes:
    # 30 -> 1
    # 20 -> 2
    # 60 -> 1
    # 50 -> 2   <-- matches depth 2
    # 40 -> 3
    # 10 -> 6
    #
    # Therefore the correct answer is 50.
    example1_root = EmployeeNode(
        10,
        [
            EmployeeNode(20, [EmployeeNode(30)]),
            EmployeeNode(40, [EmployeeNode(50, [EmployeeNode(60)])]),
        ],
    )

    result1 = solution.find_first_manager_matching_depth(example1_root)
    print(result1)  # Expected: 50

    # Example 2:
    #       1
    #    /  |  \
    #   2   3   4
    #
    # Root depth = 0, subtree size = 4 -> not equal
    # Each child depth = 1, subtree size = 1 -> equal
    #
    # However, the prompt says expected output is -1.
    # That statement conflicts with the stated rule "team size is exactly equal to depth".
    #
    # Under the problem's written rule, each leaf at depth 1 with subtree size 1 DOES satisfy.
    # The first such node in left-to-right order is 2.
    #
    # To remain faithful to the formal problem statement, this implementation returns 2 here.
    example2_root = EmployeeNode(
        1,
        [
            EmployeeNode(2),
            EmployeeNode(3),
            EmployeeNode(4),
        ],
    )

    result2 = solution.find_first_manager_matching_depth(example2_root)
    print(result2)  # According to the written rule, expected: 2

    # Additional sample where no node satisfies the condition:
    #       7
    #      /
    #     8
    #
    # Depths: 7->0, 8->1
    # Subtree sizes: 7->2, 8->1
    # Node 8 satisfies 1 == 1, so this still has a match.
    #
    # Let's build a true no-match example:
    #       100
    #
    # Depth 0, subtree size 1 -> not equal
    no_match_root = EmployeeNode(100)
    result3 = solution.find_first_manager_matching_depth(no_match_root)
    print(result3)  # Expected: -1