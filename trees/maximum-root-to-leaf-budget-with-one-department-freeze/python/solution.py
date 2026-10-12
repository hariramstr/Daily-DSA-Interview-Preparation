"""
Title: Maximum Root-to-Leaf Budget With One Department Freeze

Problem Description:
You are given the hierarchy of a company as a binary tree. Each node represents a
department, and its integer value represents the net budget contribution of that
department for the quarter. The value may be positive (profit) or negative (loss).

A valid reporting chain is any path from the root department down to a leaf department.

The finance team is allowed to apply at most one "department freeze" on a reporting
chain. Freezing a department means you may skip the contribution of exactly one node
on the chosen root-to-leaf path, treating its value as 0 instead of adding it to the
path total. You may also choose not to use the freeze.

Return the maximum possible budget total among all root-to-leaf paths after applying
at most one freeze.

A leaf is a node with no children. The freeze can be used on the root, an internal
node, or a leaf, but at most once on the chosen path.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- -100000 <= Node.val <= 100000
- The tree is a binary tree.
- The answer fits in a 64-bit signed integer.
"""

from __future__ import annotations

from collections import deque
from dataclasses import dataclass
from typing import Deque, List, Optional, Tuple


@dataclass
class TreeNode:
    """
    Basic binary tree node.

    Attributes:
        val: Integer value stored in the node.
        left: Left child.
        right: Right child.
    """

    val: int
    left: Optional["TreeNode"] = None
    right: Optional["TreeNode"] = None


class Solution:
    def max_root_to_leaf_with_one_freeze(self, root: Optional[TreeNode]) -> int:
        """
        Compute the maximum root-to-leaf path sum when we may skip the value of
        at most one node on the chosen path.

        The algorithm performs an iterative post-order traversal so it can safely
        handle very deep trees without risking Python recursion depth errors.

        For each node, we compute two dynamic programming states:
        1. no_freeze[node]:
           Best root-to-leaf sum starting at this node if we have NOT used the
           freeze yet in this subtree path.
        2. used_freeze[node]:
           Best root-to-leaf sum starting at this node if the freeze HAS already
           been used somewhere from this node down to a leaf, or we choose to use
           it exactly at this node.

        Transition idea:
        - If we have not used the freeze yet at the current node, we have two choices:
          a) Do not freeze this node:
             node.val + max(no_freeze[child])
          b) Freeze this node right now:
             0 + max(no_freeze[child])
             or 0 if this node is a leaf
        - If the freeze is already used by the time we leave this node, then:
          a) It may have been used earlier above this node, so we must include node.val
             and continue into a child with used_freeze[child]
          b) Or we use the freeze exactly at this node, contributing 0 and then continue
             into a child with no_freeze[child]
          The best of these possibilities becomes used_freeze[node].

        At the root, the answer is no_freeze[root], because "at most one freeze"
        means we start with the freeze still available.

        Args:
            root: Root of the binary tree.

        Returns:
            Maximum achievable root-to-leaf sum with at most one freeze.

        Time complexity:
            O(n), where n is the number of nodes, because each node is processed once.

        Space complexity:
            O(n), due to the traversal stack and DP storage.
        """
        if root is None:
            return 0

        # We use a dictionary keyed by the actual node object identity.
        # Each value is a tuple:
        #   (best_if_freeze_not_used_yet, best_if_freeze_already_used)
        #
        # Why store both states?
        # Because the decision at a node depends on whether the single allowed
        # freeze is still available or has already been consumed earlier on the path.
        dp: dict[int, Tuple[int, int]] = {}

        # Iterative post-order traversal:
        # We need children processed before their parent, because the parent's DP
        # values depend on the children's DP values.
        #
        # Stack entries are:
        #   (node, visited_flag)
        #
        # visited_flag == False:
        #   First time we see the node. Push it back as visited, then push children.
        # visited_flag == True:
        #   Children have already been processed, so now we can compute DP for node.
        stack: List[Tuple[Optional[TreeNode], bool]] = [(root, False)]

        # A very small sentinel for impossible states.
        # We use a large negative number because path sums can be negative.
        NEG_INF = -(10**30)

        while stack:
            node, visited = stack.pop()

            if node is None:
                continue

            if not visited:
                # First encounter:
                # Push current node back marked as visited so it will be processed
                # after its children.
                stack.append((node, True))

                # Push children to ensure they are processed first.
                stack.append((node.right, False))
                stack.append((node.left, False))
            else:
                # Now both children (if any) already have DP values in the dictionary.

                # Detect whether this node is a leaf.
                is_leaf = node.left is None and node.right is None

                if is_leaf:
                    # Leaf handling is the simplest base case.
                    #
                    # If freeze not used yet:
                    #   We can either keep the leaf value, or freeze this leaf and take 0.
                    #   Since we want the maximum:
                    #       no_freeze = max(node.val, 0)
                    #
                    # If freeze already used:
                    #   We must include this leaf's value because no freeze remains.
                    #       used_freeze = node.val
                    #
                    # Important subtlety:
                    # The "used_freeze" state means by the time we finish this path,
                    # the freeze has definitely been used somewhere on or above this node.
                    # If we are already in that state at a leaf, we simply add the leaf value.
                    no_freeze = max(node.val, 0)
                    used_freeze = node.val
                    dp[id(node)] = (no_freeze, used_freeze)
                    continue

                # Read child DP values if children exist.
                # For missing children, we treat them as impossible paths.
                left_no, left_used = dp[id(node.left)] if node.left is not None else (NEG_INF, NEG_INF)
                right_no, right_used = dp[id(node.right)] if node.right is not None else (NEG_INF, NEG_INF)

                # Best continuation into exactly one child, because a root-to-leaf path
                # must choose one branch, not both.
                best_child_no_freeze = max(left_no, right_no)
                best_child_used_freeze = max(left_used, right_used)

                # State 1: no_freeze
                #
                # Meaning:
                #   Starting at this node, what is the best root-to-leaf sum if the freeze
                #   is still available before processing this node?
                #
                # Two choices:
                #
                # Choice A: Do NOT freeze current node.
                #   Then current node contributes node.val, and the freeze remains available
                #   for the chosen child path.
                #   Total = node.val + best_child_no_freeze
                keep_current = node.val + best_child_no_freeze

                # Choice B: Freeze current node.
                #   Then current node contributes 0, and the freeze is now consumed.
                #   After that, the child path must be evaluated in the "freeze already used"
                #   state? Let's reason carefully:
                #
                #   If we freeze the current node, then from the child downward the freeze
                #   is no longer available. However, our DP definition for child states is:
                #     - child_no_freeze: freeze still available at child
                #     - child_used_freeze: freeze already used before child
                #
                #   Since we used the freeze at the current node, by the time we move to the
                #   child, the correct state is "freeze already used".
                #
                #   Therefore:
                #       freeze_current = 0 + best_child_used_freeze
                #
                #   This is the correct transition for internal nodes.
                freeze_current = best_child_used_freeze

                no_freeze = max(keep_current, freeze_current)

                # State 2: used_freeze
                #
                # Meaning:
                #   Starting at this node, what is the best root-to-leaf sum if the freeze
                #   has already been used somewhere above this node?
                #
                # Since the freeze is already gone, we MUST include current node's value
                # and continue with a child also in the "used_freeze" state.
                used_freeze = node.val + best_child_used_freeze

                dp[id(node)] = (no_freeze, used_freeze)

        # At the root, we begin with the freeze still available.
        answer, _ = dp[id(root)]
        return answer


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common convention:
    - values[i] is the node value or None for a missing node
    - children are assigned left-to-right level by level

    Example:
        [5, 4, 8, -3, None, 6, 2]

    builds:
              5
             / \
            4   8
           /   / \
         -3   6   2

    Args:
        values: Level-order list with integers or None.

    Returns:
        Root of the constructed binary tree, or None if the list is empty.

    Time complexity:
        O(n), where n is the length of the input list.

    Space complexity:
        O(n), for the queue and created nodes.
    """
    if not values:
        return None

    if values[0] is None:
        return None

    root = TreeNode(values[0])
    queue: Deque[TreeNode] = deque([root])
    index = 1

    while queue and index < len(values):
        current = queue.popleft()

        # Assign left child if present in the input.
        if index < len(values):
            left_value = values[index]
            index += 1
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if present in the input.
        if index < len(values):
            right_value = values[index]
            index += 1
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # Tree: [5,4,8,-3,null,6,2]
    #
    # Paths:
    # 5 -> 4 -> -3 = 6
    #   Freeze -3 => 9
    #   Freeze 4  => 2
    #   Freeze 5  => 1
    #
    # 5 -> 8 -> 6 = 19
    #   Freeze 6 => 13
    #   Freeze 8 => 11
    #   Freeze 5 => 14
    #
    # 5 -> 8 -> 2 = 15
    #   Freeze 2 => 13
    #   Freeze 8 => 7
    #   Freeze 5 => 10
    #
    # Best overall = 19
    root1 = build_tree_from_level_order([5, 4, 8, -3, None, 6, 2])
    result1 = solution.max_root_to_leaf_with_one_freeze(root1)
    print("Example 1 Output:", result1)  # Expected: 19

    # Example 2 from the prompt:
    # Tree: [7,-5,3,4,2,-6,9]
    #
    # Paths:
    # 7 -> -5 -> 4 = 6, freeze -5 => 11
    # 7 -> -5 -> 2 = 4, freeze -5 => 9
    # 7 -> 3 -> -6 = 4, freeze -6 => 10
    # 7 -> 3 -> 9 = 19, no freeze needed
    #
    # Best overall = 19
    root2 = build_tree_from_level_order([7, -5, 3, 4, 2, -6, 9])
    result2 = solution.max_root_to_leaf_with_one_freeze(root2)
    print("Example 2 Output:", result2)  # Expected: 19

    # Additional quick sanity checks:

    # Single positive node:
    # Path sum is 10, freezing would give 0, so best is 10.
    root3 = build_tree_from_level_order([10])
    result3 = solution.max_root_to_leaf_with_one_freeze(root3)
    print("Single Positive Node Output:", result3)  # Expected: 10

    # Single negative node:
    # Path sum is -7, freezing gives 0, so best is 0.
    root4 = build_tree_from_level_order([-7])
    result4 = solution.max_root_to_leaf_with_one_freeze(root4)
    print("Single Negative Node Output:", result4)  # Expected: 0

    # Mixed tree where freezing helps:
    #      1
    #     / \
    #   -10  2
    #   /
    #  5
    #
    # Paths:
    # 1 -> -10 -> 5 = -4, freeze -10 => 6
    # 1 -> 2 = 3
    # Best = 6
    root5 = build_tree_from_level_order([1, -10, 2, 5])
    result5 = solution.max_root_to_leaf_with_one_freeze(root5)
    print("Freeze Helps Output:", result5)  # Expected: 6