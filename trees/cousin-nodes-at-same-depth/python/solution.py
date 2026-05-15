```python
"""
Title: Cousin Nodes at Same Depth
Difficulty: Easy
Topic: Trees

Problem Description:
Given the root of a binary tree and two node values x and y, determine whether
the nodes with these values are cousins. Two nodes are considered cousins if they
are at the same depth in the tree but have different parent nodes.

You may assume that both values x and y exist exactly once in the tree, and the
tree contains at least 2 nodes.

Constraints:
- The number of nodes in the tree is in the range [2, 100].
- Node values are unique integers in the range [1, 100].
- Both x and y exist in the tree.

Example 1:
    Input: root = [1, 2, 3, 4, 5, null, null], x = 4, y = 5
    Output: false
    Explanation: Nodes 4 and 5 are both at depth 2, but they share the same
    parent (node 2). So they are siblings, not cousins.

Example 2:
    Input: root = [1, 2, 3, null, 4, null, 5], x = 4, y = 5
    Output: true
    Explanation: Node 4 is a child of node 2 (depth 2), and node 5 is a child
    of node 3 (depth 2). They are at the same depth and have different parents,
    so they are cousins.
"""

from collections import deque
from typing import Optional, Tuple


# ---------------------------------------------------------------------------
# Definition for a binary tree node.
# ---------------------------------------------------------------------------
class TreeNode:
    """Represents a single node in a binary tree."""

    def __init__(self, val: int = 0,
                 left: "Optional[TreeNode]" = None,
                 right: "Optional[TreeNode]" = None):
        self.val = val
        self.left = left
        self.right = right


# ---------------------------------------------------------------------------
# Solution class
# ---------------------------------------------------------------------------
class Solution:
    """Contains the algorithm to check whether two nodes are cousins."""

    def isCousins(self, root: Optional[TreeNode], x: int, y: int) -> bool:
        """
        Determine whether nodes with values x and y are cousins in the tree.

        Two nodes are cousins if:
          1. They are at the same depth (distance from root).
          2. They have DIFFERENT parent nodes.

        Strategy — BFS (Level-Order Traversal):
        We perform a Breadth-First Search so we naturally process the tree
        level by level. For each node we visit, we record:
          - Its depth
          - Its parent node value
        Once we have found both x and y, we compare their depths and parents.

        Args:
            root: The root of the binary tree.
            x:    The value of the first target node.
            y:    The value of the second target node.

        Returns:
            True if nodes x and y are cousins, False otherwise.

        Time Complexity:  O(N) — we may visit every node in the worst case.
        Space Complexity: O(N) — the BFS queue can hold up to N nodes (last
                          level of a complete binary tree can have N/2 nodes).
        """

        # ---------------------------------------------------------------
        # Step 1: Handle the trivial edge case where root is None.
        # (The problem guarantees at least 2 nodes, but defensive coding
        # is a good habit.)
        # ---------------------------------------------------------------
        if root is None:
            return False

        # ---------------------------------------------------------------
        # Step 2: Initialise BFS data structures.
        #
        # We use a deque (double-ended queue) as our BFS queue because
        # popleft() is O(1) for deque vs O(N) for a plain list.
        #
        # Each element in the queue is a tuple:
        #   (node, parent_value, depth)
        #
        # The root has no parent, so we use -1 as a sentinel value.
        # The root is at depth 0.
        # ---------------------------------------------------------------
        queue: deque[Tuple[TreeNode, int, int]] = deque()
        queue.append((root, -1, 0))  # (node, parent_val, depth)

        # ---------------------------------------------------------------
        # Step 3: Variables to store the depth and parent of x and y
        # once we find them during BFS.
        #
        # We initialise them to None so we can check whether we have
        # found each node yet.
        # ---------------------------------------------------------------
        x_depth: Optional[int] = None
        x_parent: Optional[int] = None
        y_depth: Optional[int] = None
        y_parent: Optional[int] = None

        # ---------------------------------------------------------------
        # Step 4: BFS loop — process nodes one by one.
        # ---------------------------------------------------------------
        while queue:
            # Dequeue the front element
            node, parent_val, depth = queue.popleft()

            # -----------------------------------------------------------
            # Step 4a: Check whether the current node is x or y.
            # If it is, record its depth and parent.
            # -----------------------------------------------------------
            if node.val == x:
                x_depth = depth
                x_parent = parent_val

            if node.val == y:
                y_depth = depth
                y_parent = parent_val

            # -----------------------------------------------------------
            # Step 4b: Early exit optimisation.
            # If we have already found BOTH nodes, there is no need to
            # continue traversing the rest of the tree.
            # -----------------------------------------------------------
            if x_depth is not None and y_depth is not None:
                break

            # -----------------------------------------------------------
            # Step 4c: Enqueue the children of the current node.
            # We pass the current node's value as the parent_val for its
            # children, and increment the depth by 1.
            # -----------------------------------------------------------
            if node.left:
                queue.append((node.left, node.val, depth + 1))
            if node.right:
                queue.append((node.right, node.val, depth + 1))

        # ---------------------------------------------------------------
        # Step 5: Evaluate the cousin condition.
        #
        # Cousins must satisfy BOTH:
        #   (a) Same depth:          x_depth == y_depth
        #   (b) Different parents:   x_parent != y_parent
        #
        # We use Python's short-circuit evaluation; if depths differ we
        # never even compare parents.
        # ---------------------------------------------------------------
        return x_depth == y_depth and x_parent != y_parent


# ---------------------------------------------------------------------------
# Helper function to build a binary tree from a level-order list.
# None values represent missing nodes (like LeetCode's input format).
# ---------------------------------------------------------------------------
def build_tree(values: list) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order (BFS) list representation.

    Args:
        values: A list of integers and None values representing the tree
                in level-order. None means no node at that position.

    Returns:
        The root TreeNode of the constructed binary tree, or None if the
        list is empty or the first element is None.
    """
    if not values or values[0] is None:
        return None

    # Create the root node from the first element
    root = TreeNode(values[0])

    # Use a queue to keep track of nodes whose children we still need to assign
    queue: deque[TreeNode] = deque([root])

    # Index into the values list; start at 1 (index 0 is the root)
    i = 1

    while queue and i < len(values):
        node = queue.popleft()

        # Assign left child if the value exists and is not None
        if i < len(values) and values[i] is not None:
            node.left = TreeNode(values[i])
            queue.append(node.left)
        i += 1

        # Assign right child if the value exists and is not None
        if i < len(values) and values[i] is not None:
            node.right = TreeNode(values[i])
            queue.append(node.right)
        i += 1

    return root


# ---------------------------------------------------------------------------
# Main block — demonstrate the solution with the provided examples.
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solver = Solution()

    # -----------------------------------------------------------------------
    # Example 1:
    # Tree structure:
    #         1
    #        / \
    #       2   3
    #      / \
    #     4   5
    #
    # x = 4, y = 5
    # Node 4: depth=2, parent=2
    # Node 5: depth=2, parent=2
    # Same depth (2 == 2) BUT same parent (2 == 2) → NOT cousins → False
    # -----------------------------------------------------------------------
    print("=" * 50)
    print("Example 1")
    print("Tree: [1, 2, 3, 4, 5, null, null]")
    print("x = 4, y = 5")

    tree1 = build_tree([1, 2, 3, 4, 5, None, None])
    result1 = solver.isCousins(tree1, 4, 5)
    print(f"Output: {result1}")          # Expected: False
    print(f"Expected: False")
    assert result1 == False, "Example 1 failed!"
    print("✓ Correct!\n")

    # -----------------------------------------------------------------------
    # Example 2:
    # Tree structure:
    #         1
    #        / \
    #       2   3
    #        \    \
    #         4    5
    #
    # x = 4, y = 5
    # Node 4: depth=2, parent=2
    # Node 5: depth=2, parent=3
    # Same depth (2 == 2) AND different parents (2 != 3) → cousins → True
    # -----------------------------------------------------------------------
    print("=" * 50)
    print("Example 2")
    print("Tree: [1, 2, 3, null, 4, null, 5]")
    print("x = 4, y = 5")

    tree2 = build_tree([1, 2, 3, None, 4, None, 5])
    result2 = solver.isCousins(tree2, 4, 5)
    print(f"Output: {result2}")          # Expected: True
    print(f"Expected: True")
    assert result2 == True, "Example 2 failed!"
    print("✓ Correct!\n")

    # -----------------------------------------------------------------------
    # Additional Example 3:
    # Tree structure:
    #         1
    #        / \
    #       2   3
    #      /
    #     4
    #
    # x = 2, y = 3
    # Node 2: depth=1, parent=1
    # Node 3: depth=1, parent=1
    # Same depth (1 == 1) BUT same parent (1 == 1) → NOT cousins → False
    # (They are siblings, not cousins.)
    # -----------------------------------------------------------------------
    print("=" * 50)
    print("Additional Example 3 (siblings at depth 1)")
    print("Tree: [1, 2, 3, 4]")
    print("x = 2, y = 3")

    tree3 = build_tree([1, 2, 3, 4])
    result3 = solver.isCousins(tree3, 2, 3)
    print(f"Output: {result3}")          # Expected: False
    print(f"Expected: False")
    assert result3 == False, "Example 3 failed!"
    print("✓ Correct!\n")

    # -----------------------------------------------------------------------
    # Additional Example 4:
    # Tree structure:
    #         1
    #        / \
    #       2   3
    #      /     \
    #     4       5
    #    /
    #   6
    #
    # x = 6, y = 5
    # Node 6: depth=3, parent=4
    # Node 5: depth=2, parent=3
    # Different depths (3 != 2) → NOT cousins → False
    # -----------------------------------------------------------------------
    print("=" * 50)
    print("Additional Example 4 (different depths)")
    print("Tree: [1, 2, 3, 4, null, null, 5, 6]")
    print("x = 6, y = 5")

    tree4 = build_tree([1, 2, 3, 4, None, None, 5, 6])
    result4 = solver.isCousins(tree4, 6, 5)
    print(f"Output: {result4}")          # Expected: False
    print(f"Expected: False")
    assert result4 == False, "Example 4 failed!"
    print("✓ Correct!\n")

    print("=" * 50)
    print("All test cases passed!")
```