"""
Title: Count Leaves at Each Level
Difficulty: Easy
Topic: Trees

Problem Description:
Given the root of a binary tree, return a list where each element represents
the number of leaf nodes at that level of the tree. Levels are 0-indexed,
starting from the root at level 0.

A leaf node is defined as a node with no left or right children.

For example, if the root itself has no children, it is a leaf at level 0,
so the result would be [1].

Constraints:
- The number of nodes in the tree is in the range [1, 1000].
- -1000 <= Node.val <= 1000
- The tree may be skewed (i.e., all nodes on one side).
"""

from collections import deque
from typing import List, Optional


# ---------------------------------------------------------------------------
# TreeNode definition
# ---------------------------------------------------------------------------
class TreeNode:
    """Standard binary tree node."""

    def __init__(self, val: int = 0,
                 left: "Optional[TreeNode]" = None,
                 right: "Optional[TreeNode]" = None):
        self.val = val
        self.left = left
        self.right = right


# ---------------------------------------------------------------------------
# Helper: build a tree from a level-order list (LeetCode-style)
# ---------------------------------------------------------------------------
def build_tree(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order (BFS) list.
    'None' entries represent missing nodes.

    Args:
        values: Level-order list of node values (None = absent node).

    Returns:
        Root TreeNode of the constructed tree, or None if list is empty.
    """
    if not values or values[0] is None:
        return None

    root = TreeNode(values[0])
    queue: deque[TreeNode] = deque([root])
    i = 1  # index into the values list

    while queue and i < len(values):
        node = queue.popleft()

        # Assign left child
        if i < len(values):
            if values[i] is not None:
                node.left = TreeNode(values[i])
                queue.append(node.left)
            i += 1

        # Assign right child
        if i < len(values):
            if values[i] is not None:
                node.right = TreeNode(values[i])
                queue.append(node.right)
            i += 1

    return root


# ---------------------------------------------------------------------------
# Solution
# ---------------------------------------------------------------------------
class Solution:
    """Contains the algorithm to count leaf nodes at each level."""

    def count_leaves_per_level(self, root: Optional[TreeNode]) -> List[int]:
        """
        Return a list where index i holds the number of leaf nodes at level i.

        Approach — BFS (level-order traversal):
        We process the tree level by level using a queue.  For every level we
        count how many of the nodes in that level are leaves (no children).
        This naturally gives us the answer without any extra bookkeeping.

        Args:
            root: Root of the binary tree.

        Returns:
            List[int] where result[i] = number of leaf nodes at level i.

        Time Complexity:  O(N) — every node is visited exactly once.
        Space Complexity: O(W) — W is the maximum width of the tree
                          (at most N/2 nodes on the last level for a full tree),
                          so O(N) in the worst case.
        """

        # ----------------------------------------------------------------
        # Edge case: empty tree → return empty list
        # ----------------------------------------------------------------
        if root is None:
            return []

        # ----------------------------------------------------------------
        # result  : accumulates the leaf count for each level.
        # queue   : standard BFS frontier; starts with just the root.
        #
        # We use collections.deque because popleft() is O(1) for a deque
        # but O(N) for a plain Python list — important for large trees.
        # ----------------------------------------------------------------
        result: List[int] = []
        queue: deque[TreeNode] = deque([root])

        # ----------------------------------------------------------------
        # BFS loop — each iteration of the OUTER while-loop processes
        # exactly ONE complete level of the tree.
        # ----------------------------------------------------------------
        while queue:
            # How many nodes are currently in the queue?
            # All of them belong to the CURRENT level.
            level_size: int = len(queue)

            # Count of leaf nodes found at this level.
            leaf_count: int = 0

            # ------------------------------------------------------------
            # Inner loop — process every node that belongs to this level.
            # ------------------------------------------------------------
            for _ in range(level_size):
                # Dequeue the front node (O(1) with deque).
                node: TreeNode = queue.popleft()

                # Check whether this node is a leaf:
                # A leaf has NO left child AND NO right child.
                if node.left is None and node.right is None:
                    leaf_count += 1
                else:
                    # Not a leaf → enqueue its existing children so they
                    # will be processed in the NEXT level iteration.
                    if node.left is not None:
                        queue.append(node.left)
                    if node.right is not None:
                        queue.append(node.right)

            # After processing all nodes at this level, record the count.
            result.append(leaf_count)

        # ----------------------------------------------------------------
        # Return the completed list.
        # Example 1 trace:
        #   Level 0: queue=[3]        → 3 has children → leaf_count=0
        #   Level 1: queue=[9,20]     → 9 is leaf, 20 has children → leaf_count=1
        #   Level 2: queue=[15,7]     → both leaves → leaf_count=2
        #   result = [0, 1, 2]  ✓
        #
        # Example 2 trace:
        #   Level 0: queue=[1]        → 1 has children → leaf_count=0
        #   Level 1: queue=[2,3]      → 2 has child(4), 3 is leaf → leaf_count=1
        #   Level 2: queue=[4]        → 4 is leaf → leaf_count=1
        #   result = [0, 1, 1]  ✓
        # ----------------------------------------------------------------
        return result


# ---------------------------------------------------------------------------
# Main — demonstrate with the examples from the problem description
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solver = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # Tree structure:
    #        3
    #       / \
    #      9  20
    #         / \
    #        15   7
    # Expected output: [0, 1, 2]
    # ------------------------------------------------------------------
    print("=== Example 1 ===")
    root1 = build_tree([3, 9, 20, None, None, 15, 7])
    output1 = solver.count_leaves_per_level(root1)
    print(f"Input  : [3, 9, 20, null, null, 15, 7]")
    print(f"Output : {output1}")          # [0, 1, 2]
    print(f"Expected: [0, 1, 2]")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # Tree structure:
    #        1
    #       / \
    #      2   3
    #     /
    #    4
    # Expected output: [0, 1, 1]
    # ------------------------------------------------------------------
    print("=== Example 2 ===")
    root2 = build_tree([1, 2, 3, 4, None, None, None])
    output2 = solver.count_leaves_per_level(root2)
    print(f"Input  : [1, 2, 3, 4, null, null, null]")
    print(f"Output : {output2}")          # [0, 1, 1]
    print(f"Expected: [0, 1, 1]")
    print()

    # ------------------------------------------------------------------
    # Extra: single-node tree (root is itself a leaf)
    # Expected output: [1]
    # ------------------------------------------------------------------
    print("=== Extra: single node ===")
    root3 = build_tree([42])
    output3 = solver.count_leaves_per_level(root3)
    print(f"Input  : [42]")
    print(f"Output : {output3}")          # [1]
    print(f"Expected: [1]")
    print()

    # ------------------------------------------------------------------
    # Extra: skewed (right-only) tree
    # Tree:  1 -> 2 -> 3
    # Expected output: [0, 0, 1]
    # ------------------------------------------------------------------
    print("=== Extra: right-skewed tree [1, null, 2, null, 3] ===")
    root4 = build_tree([1, None, 2, None, 3])
    output4 = solver.count_leaves_per_level(root4)
    print(f"Input  : [1, null, 2, null, 3]")
    print(f"Output : {output4}")          # [0, 0, 1]
    print(f"Expected: [0, 0, 1]")