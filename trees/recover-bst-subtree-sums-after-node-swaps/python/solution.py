```python
"""
Title: Recover Binary Search Tree Subtree Sums After Node Swaps
Difficulty: Hard
Topic: Trees

Problem Description:
You are given the root of a Binary Search Tree (BST) where exactly two nodes have been
swapped, violating the BST property. Your task is to restore the BST to its original
valid state and then return an array of subtree sums for every node in the tree, listed
in level-order (BFS) traversal.

The subtree sum of a node is defined as the sum of all node values in the subtree rooted
at that node (including the node itself).

You must first identify and swap back the two incorrectly placed nodes to restore the BST,
then compute the subtree sum for each node.

Constraints:
- The number of nodes in the tree is in the range [2, 1000].
- Each node value is a unique integer in the range [-10^6, 10^6].
- It is guaranteed that exactly two nodes have been swapped.
- The tree is not necessarily balanced.
"""

from __future__ import annotations
from collections import deque
from typing import List, Optional


# ---------------------------------------------------------------------------
# TreeNode definition
# ---------------------------------------------------------------------------
class TreeNode:
    """Standard binary tree node."""

    def __init__(self, val: int = 0,
                 left: Optional[TreeNode] = None,
                 right: Optional[TreeNode] = None):
        self.val = val
        self.left = left
        self.right = right


# ---------------------------------------------------------------------------
# Helper: build a tree from a level-order list (LeetCode style)
# ---------------------------------------------------------------------------
def build_tree(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list where None means no node.

    Args:
        values: Level-order list of node values (None = absent node).

    Returns:
        Root of the constructed binary tree.
    """
    if not values or values[0] is None:
        return None

    root = TreeNode(values[0])
    queue: deque[TreeNode] = deque([root])
    i = 1

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
# Helper: collect level-order values for display
# ---------------------------------------------------------------------------
def level_order_values(root: Optional[TreeNode]) -> List[Optional[int]]:
    """Return level-order values (None for missing nodes) for display."""
    if root is None:
        return []
    result: List[Optional[int]] = []
    queue: deque[Optional[TreeNode]] = deque([root])
    while queue:
        node = queue.popleft()
        if node is None:
            result.append(None)
        else:
            result.append(node.val)
            queue.append(node.left)
            queue.append(node.right)
    # Strip trailing Nones for cleaner output
    while result and result[-1] is None:
        result.pop()
    return result


# ---------------------------------------------------------------------------
# Main Solution
# ---------------------------------------------------------------------------
class Solution:
    """
    Recovers a BST where exactly two nodes were swapped, then computes
    the subtree sum for every node in level-order (BFS) order.
    """

    def recoverAndComputeSubtreeSums(self, root: Optional[TreeNode]) -> List[int]:
        """
        Main entry point: recover the BST then return level-order subtree sums.

        Args:
            root: Root of the corrupted BST (two nodes swapped).

        Returns:
            List of subtree sums in level-order (BFS) traversal order.

        Time Complexity:  O(n) — each node is visited a constant number of times.
        Space Complexity: O(n) — for the recursion stack and auxiliary storage.
        """

        # ----------------------------------------------------------------
        # STEP 1: Find the two swapped nodes using in-order traversal.
        #
        # Key insight: In a valid BST, an in-order traversal produces a
        # strictly increasing sequence.  When exactly two nodes are swapped,
        # the in-order sequence will have either:
        #   (a) Two "inversions" (adjacent pairs where left > right) — the
        #       first node of the 1st inversion and the second node of the
        #       2nd inversion are the culprits.
        #   (b) One inversion — the two nodes are adjacent in in-order, so
        #       both nodes of that single inversion are the culprits.
        #
        # We track:
        #   - `prev`   : the previously visited node during in-order walk
        #   - `first`  : the first offending node (larger value out of place)
        #   - `second` : the second offending node (smaller value out of place)
        # ----------------------------------------------------------------

        self._first: Optional[TreeNode] = None   # first swapped node
        self._second: Optional[TreeNode] = None  # second swapped node
        self._prev: Optional[TreeNode] = None    # previous node in in-order

        # Perform in-order traversal to locate the two swapped nodes
        self._find_swapped(root)

        # ----------------------------------------------------------------
        # STEP 2: Swap the values back to restore the BST.
        #
        # We only swap the *values* stored in the nodes, not the nodes
        # themselves (pointers).  This is the classic "Recover BST" trick —
        # it is O(1) extra work and keeps all parent/child pointers intact.
        # ----------------------------------------------------------------
        if self._first is not None and self._second is not None:
            # Exchange the values of the two misplaced nodes
            self._first.val, self._second.val = self._second.val, self._first.val

        # ----------------------------------------------------------------
        # STEP 3: Compute subtree sums for every node.
        #
        # We use a post-order DFS: process left subtree, then right subtree,
        # then the current node.  This way, when we visit a node we already
        # know the sums of both its children's subtrees.
        #
        # subtree_sum(node) = node.val
        #                   + subtree_sum(node.left)   [0 if no left child]
        #                   + subtree_sum(node.right)  [0 if no right child]
        #
        # We store the computed sum in a dictionary keyed by node id so we
        # can look it up later during the BFS level-order collection step.
        # ----------------------------------------------------------------
        self._subtree_sums: dict[int, int] = {}  # node id -> subtree sum
        self._compute_subtree_sum(root)

        # ----------------------------------------------------------------
        # STEP 4: Collect subtree sums in level-order (BFS).
        #
        # A standard BFS with a queue visits nodes level by level, left to
        # right — exactly the order required by the problem.
        # ----------------------------------------------------------------
        result: List[int] = []
        if root is None:
            return result

        bfs_queue: deque[TreeNode] = deque([root])
        while bfs_queue:
            node = bfs_queue.popleft()
            # Look up the pre-computed subtree sum for this node
            result.append(self._subtree_sums[id(node)])

            # Enqueue children (left before right for correct level-order)
            if node.left:
                bfs_queue.append(node.left)
            if node.right:
                bfs_queue.append(node.right)

        return result

    # ------------------------------------------------------------------
    # Private helper: in-order traversal to find the two swapped nodes
    # ------------------------------------------------------------------
    def _find_swapped(self, node: Optional[TreeNode]) -> None:
        """
        Perform an in-order traversal of the BST to identify the two nodes
        whose values were swapped, violating the BST property.

        The algorithm exploits the fact that a valid BST's in-order sequence
        is strictly increasing.  We look for "descents" — places where the
        current node's value is less than the previous node's value.

        Args:
            node: Current node being visited.

        Returns:
            None (results stored in self._first and self._second).

        Time Complexity:  O(n)
        Space Complexity: O(h) where h is the tree height (recursion stack).
        """
        if node is None:
            return

        # --- Recurse into left subtree first (in-order: left → root → right)
        self._find_swapped(node.left)

        # --- Check for an inversion with the previously visited node
        if self._prev is not None and self._prev.val > node.val:
            # We found a descent: prev.val should be LESS than node.val in a
            # valid BST, but it is not.

            if self._first is None:
                # This is the FIRST inversion we encounter.
                # The larger value (self._prev) is out of place — mark it.
                self._first = self._prev

            # Whether this is the first or second inversion, the smaller
            # value (node) is always a candidate for the second swapped node.
            # If there is only one inversion (adjacent swap), this assignment
            # on the first (and only) inversion gives us the correct pair.
            self._second = node

        # Advance the "previous" pointer to the current node
        self._prev = node

        # --- Recurse into right subtree
        self._find_swapped(node.right)

    # ------------------------------------------------------------------
    # Private helper: post-order DFS to compute subtree sums
    # ------------------------------------------------------------------
    def _compute_subtree_sum(self, node: Optional[TreeNode]) -> int:
        """
        Recursively compute the subtree sum for every node using post-order
        DFS and store the results in self._subtree_sums.

        Args:
            node: Current node being processed.

        Returns:
            The subtree sum rooted at `node` (0 if node is None).

        Time Complexity:  O(n)
        Space Complexity: O(h) where h is the tree height (recursion stack).
        """
        if node is None:
            # Base case: an empty subtree contributes 0 to the sum
            return 0

        # Post-order: compute left and right subtree sums first
        left_sum = self._compute_subtree_sum(node.left)
        right_sum = self._compute_subtree_sum(node.right)

        # The subtree sum of this node = its own value + both children's sums
        total = node.val + left_sum + right_sum

        # Store in dictionary using the node's memory id as a unique key
        # (node values could theoretically collide, but id() is always unique)
        self._subtree_sums[id(node)] = total

        return total


# ---------------------------------------------------------------------------
# Main block: demonstrate with the examples from the problem description
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # Input tree (level-order): [3, 1, 4, null, null, 2, null]
    #
    #        3          ← should be 2 (swapped with 2)
    #       / \
    #      1   4
    #         /
    #        2          ← should be 3 (swapped with 3)
    #
    # After recovery:
    #        2
    #       / \
    #      1   4
    #         /
    #        3
    #
    # Subtree sums (level-order):
    #   node(2) = 2+1+4+3 = 10
    #   node(1) = 1
    #   node(4) = 4+3 = 7
    #   node(3) = 3
    # Expected output: [10, 1, 7, 3]
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Example 1")
    print("=" * 60)
    root1 = build_tree([3, 1, 4, None, None, 2, None])
    print("Input tree (level-order):", level_order_values(root1))

    result1 = solution.recoverAndComputeSubtreeSums(root1)
    print("Recovered tree (level-order):", level_order_values(root1))
    print("Subtree sums (level-order):", result1)
    print("Expected:                   [10, 1, 7, 3]")
    assert result1 == [10, 1, 7, 3], f"Example 1 FAILED: got {result1}"
    print("✓ Example 1 PASSED\n")

    # -----------------------------------------------------------------------
    # Example 2
    # Input tree (level-order): [5, 3, 8, 1, 7, 6, 9]
    #
    #          5
    #         / \
    #        3   8
    #       / \ / \
    #      1  7 6  9
    #
    # Nodes 7 and 6 are swapped (7 is in left subtree of 3, 6 is in left
    # subtree of 8 — they should be exchanged).
    #
    # After recovery:
    #          5
    #         / \
    #        3   8
    #       / \ / \
    #      1  6 7  9
    #
    # Subtree sums (level-order):
    #   node(5) = 5+3+8+1+6+7+9 = 39
    #   node(3) = 3+1+6 = 10
    #   node(8) = 8+7+9 = 24
    #   node(1) = 1
    #   node(6) = 6
    #   node(7) = 7
    #   node(9) = 9
    # Expected output: [39, 10, 24, 1, 6, 7, 9]
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Example 2")
    print("=" * 60)
    root2 = build_tree([5, 3, 8, 1, 7, 6, 9])
    print("Input tree (level-order):", level_order_values(root2))

    result2 = solution.recoverAndComputeSubtreeSums(root2)
    print("Recovered tree (level-order):", level_order_values(root2))
    print("Subtree sums (level-order):", result2)
    print("Expected:                   [39, 10, 24, 1, 6, 7, 9]")
    assert result2 == [39, 10, 24, 1, 6, 7, 9], f"Example 2 FAILED: got {result2}"
    print("✓ Example 2 PASSED\n")

    # -----------------------------------------------------------------------
    # Extra test: adjacent swap (nodes next to each other in in-order)
    #
    # Valid BST:
    #      2
    #     / \
    #    1   3
    #
    # Swap 1 and 2 → corrupted:
    #      1
    #     / \
    #    2   3
    #
    # After recovery:
    #      2
    #     / \
    #    1   3
    #
    # Subtree sums: node(2)=6, node(1)=1, node(3)=3
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Extra test: adjacent swap")
    print("=" * 60)
    root3 = build_tree([1, 2, 3])
    print("Input tree (level-order):", level_order_values(root3))

    result3 = solution.recoverAndComputeSubtreeSums(root3)
    