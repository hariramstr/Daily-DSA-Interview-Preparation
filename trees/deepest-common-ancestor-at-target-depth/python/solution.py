```python
"""
Title: Deepest Common Ancestor at Target Depth
Difficulty: Medium
Topic: Trees

Problem Description:
Given the root of a binary tree and an integer `depth`, find the lowest common ancestor (LCA)
of all nodes that exist exactly at the given `depth` in the tree. The depth of the root node is 1.

If there is only one node at the given depth, return that node itself. If no nodes exist at
the given depth, return null.

The 'deepest common ancestor at target depth' is defined as the deepest node in the tree
that is an ancestor of every node found at exactly `depth` levels from the root.

Constraints:
- The number of nodes in the tree is in the range [1, 1000].
- -10^5 <= Node.val <= 10^5
- All node values are unique.
- 1 <= depth <= 1000

Example 1:
    Input: root = [3,5,1,6,2,0,8,null,null,7,4], depth = 4
    Output: 2
    Explanation: Nodes at depth 4 are [7, 4]. Their lowest common ancestor is node 2.

Example 2:
    Input: root = [1,2,3,4,5], depth = 3
    Output: 1 (wait — actually node 2, see explanation below)
    Explanation: Nodes at depth 3 are [4, 5]. LCA is node 2.
    Note: The problem statement says output is 1 but the explanation says LCA is node 2.
    Based on the tree structure [1,2,3,4,5]:
        depth 1: [1]
        depth 2: [2, 3]
        depth 3: [4, 5]  (children of node 2; node 3 has no children)
    LCA of [4, 5] is node 2.

Example 3:
    Input: root = [1,2,3], depth = 1
    Output: 1
    Explanation: Only the root exists at depth 1, so return the root.
"""

from __future__ import annotations
from typing import Optional, List, Tuple
from collections import deque


# ─────────────────────────────────────────────
# TreeNode definition
# ─────────────────────────────────────────────
class TreeNode:
    """Standard binary tree node."""

    def __init__(self, val: int = 0,
                 left: Optional['TreeNode'] = None,
                 right: Optional['TreeNode'] = None):
        self.val = val
        self.left = left
        self.right = right

    def __repr__(self) -> str:
        return f"TreeNode({self.val})"


# ─────────────────────────────────────────────
# Helper: build tree from level-order list
# ─────────────────────────────────────────────
def build_tree(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order (BFS) list representation.
    None values represent missing nodes.

    Args:
        values: Level-order list of node values (None = absent node).

    Returns:
        Root TreeNode of the constructed tree, or None if list is empty.

    Time complexity:  O(n)
    Space complexity: O(n)
    """
    if not values or values[0] is None:
        return None

    root = TreeNode(values[0])
    queue: deque[TreeNode] = deque([root])
    i = 1  # index into values list

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


# ─────────────────────────────────────────────
# Solution
# ─────────────────────────────────────────────
class Solution:
    """
    Approach:
    ---------
    We use a single post-order DFS traversal.  For each node we return a
    tuple (lca_candidate, max_depth_reached) where:

      • max_depth_reached  – the deepest level (relative to root, 1-indexed)
                             that exists in the subtree rooted at this node.
      • lca_candidate      – the LCA of all nodes at `depth` that live inside
                             this subtree, or None if no such nodes exist.

    Key insight
    -----------
    When we combine the results from the left and right children of a node N
    (currently at level `cur_depth`):

    Case A – Neither child subtree reaches `depth`:
        → No target nodes in this subtree.  Return (None, max_depth_reached).

    Case B – Only one child subtree reaches `depth`:
        → All target nodes are in that one subtree.
          The LCA is whatever that subtree reported.

    Case C – Both child subtrees reach `depth`:
        → Target nodes exist on both sides.
          The current node N is the LCA of all of them.
          Return (N, depth).

    Special case – N itself is at `depth`:
        → N is a target node.  Return (N, cur_depth).

    This single pass correctly handles every scenario without needing a
    separate "collect all nodes at depth" step.
    """

    def lcaAtDepth(self,
                   root: Optional[TreeNode],
                   depth: int) -> Optional[TreeNode]:
        """
        Find the lowest common ancestor of all nodes at exactly `depth`.

        Args:
            root:  Root of the binary tree.
            depth: Target depth (root is depth 1).

        Returns:
            The LCA node, or None if no nodes exist at `depth`.

        Time complexity:  O(n)  – every node is visited exactly once.
        Space complexity: O(h)  – recursion stack, where h is tree height.
                                  Worst case O(n) for a skewed tree.
        """

        def dfs(node: Optional[TreeNode],
                cur_depth: int) -> Tuple[Optional[TreeNode], int]:
            """
            Post-order DFS helper.

            Args:
                node:      Current tree node (may be None).
                cur_depth: Depth of `node` from the root (root = 1).

            Returns:
                A tuple (lca_candidate, max_depth_reached):
                  - lca_candidate:    LCA of all target-depth nodes in this
                                      subtree, or None if none exist.
                  - max_depth_reached: Deepest level found in this subtree.
            """

            # ── Base case: null node ──────────────────────────────────────
            # A null node contributes no depth and no LCA candidate.
            if node is None:
                # We return cur_depth - 1 because this "slot" doesn't exist;
                # the parent's depth is cur_depth - 1.
                return (None, cur_depth - 1)

            # ── Base case: we've reached the target depth ─────────────────
            # This node IS one of the target nodes.
            # It is trivially the LCA of itself.
            if cur_depth == depth:
                return (node, cur_depth)

            # ── Recursive step: go deeper ─────────────────────────────────
            # We haven't reached the target depth yet; recurse into children.
            left_lca,  left_max  = dfs(node.left,  cur_depth + 1)
            right_lca, right_max = dfs(node.right, cur_depth + 1)

            # Determine the deepest level reachable from this node's subtree.
            overall_max = max(left_max, right_max)

            # ── Case A: target depth not reachable from here ──────────────
            if overall_max < depth:
                # Neither subtree contains any target-depth node.
                return (None, overall_max)

            # ── Case B: only the left subtree reaches target depth ─────────
            if left_max == depth and right_max < depth:
                # All target nodes are in the left subtree.
                # Propagate the left LCA upward unchanged.
                return (left_lca, depth)

            # ── Case B (mirror): only the right subtree reaches target depth
            if right_max == depth and left_max < depth:
                # All target nodes are in the right subtree.
                return (right_lca, depth)

            # ── Case C: both subtrees reach target depth ───────────────────
            # Target nodes exist on BOTH sides of the current node.
            # Therefore the current node is the LCA of all of them.
            # (Any ancestor higher up would also be an ancestor, but we want
            #  the DEEPEST such ancestor, which is the current node.)
            return (node, depth)

        # ── Kick off the recursion from the root at depth 1 ──────────────
        lca_node, _ = dfs(root, 1)
        return lca_node


# ─────────────────────────────────────────────
# Main: trace through all examples
# ─────────────────────────────────────────────
if __name__ == "__main__":
    solution = Solution()

    # ──────────────────────────────────────────
    # Example 1
    # Tree:
    #            3
    #          /   \
    #         5     1
    #        / \   / \
    #       6   2 0   8
    #          / \
    #         7   4
    #
    # depth = 4  →  nodes at depth 4: [7, 4]
    # LCA of 7 and 4 is node 2  (expected output: 2)
    # ──────────────────────────────────────────
    values1 = [3, 5, 1, 6, 2, 0, 8, None, None, 7, 4]
    root1 = build_tree(values1)
    result1 = solution.lcaAtDepth(root1, 4)
    print(f"Example 1 → LCA node value: {result1.val if result1 else None}")
    # Expected: 2
    assert result1 is not None and result1.val == 2, \
        f"Example 1 FAILED: got {result1}"

    # ──────────────────────────────────────────
    # Example 2
    # Tree:
    #        1
    #       / \
    #      2   3
    #     / \
    #    4   5
    #
    # depth = 3  →  nodes at depth 3: [4, 5]
    # LCA of 4 and 5 is node 2  (expected output: 2)
    # Note: The problem statement says "Output: 1" but the explanation
    # clearly states LCA is node 2.  We follow the explanation.
    # ──────────────────────────────────────────
    values2 = [1, 2, 3, 4, 5]
    root2 = build_tree(values2)
    result2 = solution.lcaAtDepth(root2, 3)
    print(f"Example 2 → LCA node value: {result2.val if result2 else None}")
    # Expected: 2  (per the explanation)
    assert result2 is not None and result2.val == 2, \
        f"Example 2 FAILED: got {result2}"

    # ──────────────────────────────────────────
    # Example 3
    # Tree:
    #      1
    #     / \
    #    2   3
    #
    # depth = 1  →  only node [1] at depth 1
    # Return the node itself  (expected output: 1)
    # ──────────────────────────────────────────
    values3 = [1, 2, 3]
    root3 = build_tree(values3)
    result3 = solution.lcaAtDepth(root3, 1)
    print(f"Example 3 → LCA node value: {result3.val if result3 else None}")
    # Expected: 1
    assert result3 is not None and result3.val == 1, \
        f"Example 3 FAILED: got {result3}"

    # ──────────────────────────────────────────
    # Extra test: depth deeper than the tree
    # Tree: [1, 2, 3], depth = 5  →  no nodes  →  None
    # ──────────────────────────────────────────
    root4 = build_tree([1, 2, 3])
    result4 = solution.lcaAtDepth(root4, 5)
    print(f"Extra test (depth > tree height) → LCA node: {result4}")
    assert result4 is None, f"Extra test FAILED: got {result4}"

    # ──────────────────────────────────────────
    # Extra test: single node tree, depth = 1
    # ──────────────────────────────────────────
    root5 = build_tree([42])
    result5 = solution.lcaAtDepth(root5, 1)
    print(f"Single node tree → LCA node value: {result5.val if result5 else None}")
    assert result5 is not None and result5.val == 42, \
        f"Single node test FAILED: got {result5}"

    # ──────────────────────────────────────────
    # Extra test: only one node at target depth (right-skewed)
    # Tree:
    #   1
    #    \
    #     2
    #      \
    #       3
    # depth = 3  →  only node [3]  →  return node 3
    # ──────────────────────────────────────────
    root6 = build_tree([1, None, 2, None, 3])
    result6 = solution.lcaAtDepth(root6, 3)
    print(f"Right-skewed tree, depth=3 → LCA node value: {result6.val if result6 else None}")
    assert result6 is not None and result6.val == 3, \
        f"Right-skewed test FAILED: got {result6}"

    print("\nAll tests passed ✓")
```