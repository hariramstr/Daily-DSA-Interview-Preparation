```python
"""
Burning Tree Spread Timer
=========================

Problem Description:
You are given the root of a binary tree where each node has a unique integer value.
A fire starts at a specific node (identified by its value) at time 0. Every second,
the fire spreads to all adjacent nodes — the left child, right child, and parent of
any currently burning node.

Return the minimum number of seconds it takes for the entire tree to be on fire.

Problem Details:
- The fire starts at the node with value `start`.
- At each second, fire spreads simultaneously to all unburned neighbors (parent, left child, right child).
- All nodes must be burning for the process to complete.

Constraints:
- The number of nodes in the tree is in the range [1, 10^4].
- 1 <= Node.val <= 10^4
- All node values are unique.
- The `start` node is guaranteed to exist in the tree.
- 1 <= start <= 10^4

Example 1:
    Input: root = [1,2,3,4,5,null,6], start = 2
    Tree structure:
            1
           / \
          2   3
         / \    \
        4   5    6
    Output: 3
    Explanation: At t=0: node 2 burns. At t=1: nodes 1, 4, 5 burn.
                 At t=2: node 3 burns. At t=3: node 6 burns.

Example 2:
    Input: root = [1,2,3], start = 1
    Tree structure:
        1
       / \
      2   3
    Output: 1
    Explanation: At t=0: node 1 burns. At t=1: nodes 2 and 3 burn. All nodes burned.
"""

from collections import defaultdict, deque
from typing import Optional, Dict, List


# ─────────────────────────────────────────────
# TreeNode definition (standard LeetCode style)
# ─────────────────────────────────────────────
class TreeNode:
    """Standard binary tree node."""

    def __init__(self, val: int = 0,
                 left: "Optional[TreeNode]" = None,
                 right: "Optional[TreeNode]" = None):
        self.val = val
        self.left = left
        self.right = right


# ─────────────────────────────────────────────
# Solution
# ─────────────────────────────────────────────
class Solution:
    """
    Strategy
    --------
    The key insight is that the tree is *undirected* from the fire's perspective:
    fire can travel upward (to a parent) as well as downward (to children).

    Step 1 – Convert the binary tree into an undirected adjacency list.
             This lets us treat the problem as a plain graph BFS, which
             naturally handles the "spread to all neighbours simultaneously"
             requirement.

    Step 2 – Run BFS (level-by-level) starting from the `start` node.
             Each BFS level corresponds to one second of burning.
             The answer is the number of levels we process minus 1
             (because the first level is time 0, not time 1).
    """

    # ------------------------------------------------------------------
    # Helper: build adjacency list from the binary tree
    # ------------------------------------------------------------------
    def _build_graph(
        self,
        root: Optional[TreeNode],
        graph: Dict[int, List[int]]
    ) -> None:
        """
        Recursively traverse the tree and record bidirectional edges.

        Args:
            root  : Current tree node being processed.
            graph : Adjacency list being built (node_val -> [neighbour_vals]).

        Returns:
            None  (mutates `graph` in place)

        Time complexity : O(N) – visits every node exactly once.
        Space complexity: O(N) – recursion stack depth up to O(N) for a
                          skewed tree; O(log N) for a balanced tree.
        """
        # Base case: nothing to do for a null node
        if root is None:
            return

        # If the current node has a left child, add edges in both directions.
        # We need the edge root→left AND left→root so fire can travel either way.
        if root.left:
            graph[root.val].append(root.left.val)   # parent → left child
            graph[root.left.val].append(root.val)   # left child → parent
            # Recurse into the left subtree
            self._build_graph(root.left, graph)

        # Same logic for the right child
        if root.right:
            graph[root.val].append(root.right.val)  # parent → right child
            graph[root.right.val].append(root.val)  # right child → parent
            # Recurse into the right subtree
            self._build_graph(root.right, graph)

    # ------------------------------------------------------------------
    # Main method
    # ------------------------------------------------------------------
    def amountOfTime(self, root: Optional[TreeNode], start: int) -> int:
        """
        Return the minimum number of seconds for the entire tree to burn.

        Args:
            root  : Root of the binary tree.
            start : Value of the node where fire begins at time 0.

        Returns:
            Integer representing the number of seconds until every node burns.

        Time complexity : O(N) – we visit every node once during graph
                          construction and once during BFS.
        Space complexity: O(N) – adjacency list + BFS queue + visited set,
                          each of size O(N).
        """

        # ── Step 1: Build the undirected graph ──────────────────────────
        # defaultdict(list) automatically creates an empty list for any new key,
        # so we never have to check "does this key exist?" before appending.
        graph: Dict[int, List[int]] = defaultdict(list)
        self._build_graph(root, graph)

        # ── Step 2: BFS from the `start` node ───────────────────────────
        # We use a deque (double-ended queue) for O(1) popleft operations.
        # A regular list would give O(N) popleft, making BFS O(N²) overall.
        queue: deque[int] = deque()
        queue.append(start)

        # `visited` tracks nodes that are already burning (or have been added
        # to the queue) so we never process the same node twice.
        visited: set[int] = set()
        visited.add(start)

        # `time` counts how many full BFS levels (seconds) have elapsed.
        # We start at -1 so that after processing the first level (the start
        # node itself at t=0) we correctly return 0 if the tree has only one node.
        time: int = -1

        # Process the BFS level by level.
        # Each iteration of the outer while-loop represents ONE second passing.
        while queue:
            # Increment time at the start of each level.
            # After the first level (just the start node), time becomes 0.
            time += 1

            # We only want to process nodes that are *currently* in the queue
            # (i.e., nodes that caught fire in the previous second).
            # `level_size` freezes the count before we start adding new nodes.
            level_size: int = len(queue)

            for _ in range(level_size):
                # Dequeue the next burning node
                current: int = queue.popleft()

                # Spread fire to every unburned neighbour of `current`
                for neighbour in graph[current]:
                    if neighbour not in visited:
                        # Mark as burning so we don't revisit
                        visited.add(neighbour)
                        # Schedule for processing in the next BFS level (next second)
                        queue.append(neighbour)

        # After BFS completes, `time` equals the index of the last level processed,
        # which is exactly the number of seconds needed.
        return time


# ─────────────────────────────────────────────
# Helper: build a tree from a level-order list
# (None represents missing nodes)
# ─────────────────────────────────────────────
def build_tree(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Construct a binary tree from a level-order (BFS) list of values.

    Args:
        values: Level-order list where None means no node at that position.

    Returns:
        Root TreeNode of the constructed tree, or None if the list is empty.
    """
    if not values or values[0] is None:
        return None

    root = TreeNode(values[0])
    queue: deque[TreeNode] = deque([root])
    i = 1  # index into `values`

    while queue and i < len(values):
        node = queue.popleft()

        # Assign left child
        if i < len(values) and values[i] is not None:
            node.left = TreeNode(values[i])
            queue.append(node.left)
        i += 1

        # Assign right child
        if i < len(values) and values[i] is not None:
            node.right = TreeNode(values[i])
            queue.append(node.right)
        i += 1

    return root


# ─────────────────────────────────────────────
# Main: trace through both examples
# ─────────────────────────────────────────────
if __name__ == "__main__":
    solution = Solution()

    # ── Example 1 ──────────────────────────────────────────────────────
    # Tree:
    #         1
    #        / \
    #       2   3
    #      / \    \
    #     4   5    6
    # start = 2
    # Expected output: 3
    #
    # Trace:
    #   t=0: {2} burns
    #   t=1: neighbours of 2 → {1, 4, 5} burn
    #   t=2: neighbours of 1 not yet burned → {3} burns
    #   t=3: neighbours of 3 not yet burned → {6} burns
    #   All 6 nodes burned → answer = 3 ✓

    root1 = build_tree([1, 2, 3, 4, 5, None, 6])
    result1 = solution.amountOfTime(root1, start=2)
    print(f"Example 1 → Expected: 3  |  Got: {result1}")
    assert result1 == 3, f"Example 1 FAILED: expected 3, got {result1}"

    # ── Example 2 ──────────────────────────────────────────────────────
    # Tree:
    #     1
    #    / \
    #   2   3
    # start = 1
    # Expected output: 1
    #
    # Trace:
    #   t=0: {1} burns
    #   t=1: neighbours of 1 → {2, 3} burn
    #   All 3 nodes burned → answer = 1 ✓

    root2 = build_tree([1, 2, 3])
    result2 = solution.amountOfTime(root2, start=1)
    print(f"Example 2 → Expected: 1  |  Got: {result2}")
    assert result2 == 1, f"Example 2 FAILED: expected 1, got {result2}"

    # ── Edge case: single node ──────────────────────────────────────────
    # Tree: just node 1, start = 1
    # Expected output: 0 (it's already fully burned at t=0)

    root3 = build_tree([1])
    result3 = solution.amountOfTime(root3, start=1)
    print(f"Edge case (single node) → Expected: 0  |  Got: {result3}")
    assert result3 == 0, f"Edge case FAILED: expected 0, got {result3}"

    # ── Edge case: linear (skewed) tree ────────────────────────────────
    # Tree: 1 → 2 → 3 → 4 → 5  (all left children), start = 1
    # Expected output: 4 (fire travels down the chain)

    root4 = build_tree([1, 2, None, 3, None, None, None, 4, None, None, None,
                        None, None, None, None, 5])
    # Build manually for clarity
    root4 = TreeNode(1)
    root4.left = TreeNode(2)
    root4.left.left = TreeNode(3)
    root4.left.left.left = TreeNode(4)
    root4.left.left.left.left = TreeNode(5)

    result4 = solution.amountOfTime(root4, start=1)
    print(f"Edge case (skewed tree, start=1) → Expected: 4  |  Got: {result4}")
    assert result4 == 4, f"Edge case FAILED: expected 4, got {result4}"

    # Fire starting from the leaf of the skewed tree
    result5 = solution.amountOfTime(root4, start=5)
    print(f"Edge case (skewed tree, start=5) → Expected: 4  |  Got: {result5}")
    assert result5 == 4, f"Edge case FAILED: expected 4, got {result5}"

    print("\nAll test cases passed! ✓")
```