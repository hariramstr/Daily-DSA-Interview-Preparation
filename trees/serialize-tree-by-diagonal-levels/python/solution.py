```python
"""
Title: Serialize Tree by Diagonal Levels
Difficulty: Hard
Topic: Trees

Problem Description:
Given the root of a binary tree, serialize the tree by grouping nodes along their diagonals.
A diagonal is defined by the slope going from top-right to bottom-left: nodes reachable only
by going right from their parent share the same diagonal as the parent, while going left
increases the diagonal index by 1.

Return a list of lists where each inner list contains the node values along a diagonal,
ordered from top to bottom within that diagonal, and diagonals are ordered from the leftmost
(highest index) to the rightmost (index 0).

After serializing, you must also return the minimum number of diagonals needed such that
no two nodes in the same diagonal have a value difference less than or equal to K.
"""

from collections import defaultdict, deque
from typing import List, Optional, Dict


# Definition for a binary tree node.
class TreeNode:
    def __init__(self, val: int = 0, left: 'Optional[TreeNode]' = None,
                 right: 'Optional[TreeNode]' = None):
        self.val = val
        self.left = left
        self.right = right


class Solution:
    def serialize_by_diagonal(self, root: Optional[TreeNode], K: int):
        """
        Serialize a binary tree by diagonal levels and compute minimum splits.

        A diagonal is defined such that:
        - Moving right from a node keeps the same diagonal index.
        - Moving left from a node increases the diagonal index by 1.

        After collecting diagonals, we compute the minimum number of groups
        (splits) needed so that no two nodes in the same group have a value
        difference <= K. This is essentially a graph coloring / interval
        scheduling problem per diagonal.

        Args:
            root: The root of the binary tree.
            K: The threshold value difference.

        Returns:
            A tuple (diagonals, min_splits) where:
            - diagonals: list of lists, each inner list is the values on one diagonal,
              ordered from diagonal with highest index (leftmost) to index 0 (rightmost).
            - min_splits: minimum number of groups needed across all diagonals such that
              no two nodes in the same group have |val_i - val_j| <= K.

        Time Complexity: O(N log N) where N is the number of nodes (due to sorting for
                         the greedy interval coloring step).
        Space Complexity: O(N) for storing diagonal groups and auxiliary structures.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Handle edge case — empty tree
        # -----------------------------------------------------------------------
        if root is None:
            return [], 0

        # -----------------------------------------------------------------------
        # STEP 2: BFS traversal to collect nodes by diagonal index.
        #
        # We use a queue that stores (node, diagonal_index).
        # - Root starts at diagonal index 0.
        # - Going RIGHT keeps the same diagonal index (right child gets same index).
        # - Going LEFT increases the diagonal index by 1 (left child gets index + 1).
        #
        # We use a defaultdict(list) to map diagonal_index -> list of node values.
        # BFS ensures nodes are visited top-to-bottom, left-to-right within each level,
        # which gives us the correct top-to-bottom ordering within each diagonal.
        # -----------------------------------------------------------------------

        # diagonal_map maps diagonal_index -> list of node values in that diagonal
        diagonal_map: Dict[int, List[int]] = defaultdict(list)

        # BFS queue: each entry is (TreeNode, diagonal_index)
        queue: deque = deque()
        queue.append((root, 0))

        # Track the maximum diagonal index seen (to know how many diagonals exist)
        max_diagonal = 0

        while queue:
            # Dequeue the front element
            node, diag = queue.popleft()

            # Add this node's value to its diagonal group
            diagonal_map[diag].append(node.val)

            # Update the maximum diagonal index seen
            max_diagonal = max(max_diagonal, diag)

            # RIGHT child: same diagonal index (going right doesn't change diagonal)
            if node.right:
                queue.append((node.right, diag))

            # LEFT child: diagonal index increases by 1 (going left increases diagonal)
            if node.left:
                queue.append((node.left, diag + 1))

        # -----------------------------------------------------------------------
        # STEP 3: Build the result list of diagonals.
        #
        # The problem asks for diagonals ordered from leftmost (highest index)
        # to rightmost (index 0). So we iterate from max_diagonal down to 0.
        # -----------------------------------------------------------------------

        diagonals: List[List[int]] = []
        for diag_idx in range(max_diagonal, -1, -1):
            # Only include diagonals that actually have nodes
            if diag_idx in diagonal_map:
                diagonals.append(diagonal_map[diag_idx])

        # -----------------------------------------------------------------------
        # STEP 4: Compute the minimum number of "splits" (groups) needed.
        #
        # The problem asks: what is the minimum number of diagonals (groups) needed
        # such that no two nodes in the same group have |val_i - val_j| <= K?
        #
        # Equivalently, two nodes CONFLICT if |val_i - val_j| <= K.
        # We need to color all nodes across all diagonals with the minimum number
        # of colors such that no two conflicting nodes share a color.
        #
        # This is equivalent to interval graph coloring:
        # - Each node with value v "occupies" the interval [v - K, v + K].
        # - Two nodes conflict if their intervals overlap (i.e., |v1 - v2| <= K).
        # - The minimum number of colors = maximum clique size = maximum number of
        #   nodes whose intervals all mutually overlap at some point.
        #
        # Algorithm (greedy interval scheduling / sweep line):
        # 1. Collect ALL node values from all diagonals.
        # 2. Sort them.
        # 3. Use a sliding window / two-pointer approach to find the maximum number
        #    of values that all fall within a window of size K (i.e., max_val - min_val <= K).
        #    This maximum window size is the answer.
        #
        # Wait — let me reconsider. Two nodes conflict if |v1 - v2| <= K.
        # The minimum number of groups = chromatic number of the conflict graph.
        # For interval graphs, chromatic number = clique number.
        # The maximum clique is the maximum set of values where all pairwise differences <= K,
        # which means max_val - min_val <= K (since if all pairs satisfy this, the range is <= K).
        #
        # So we need: maximum number of values in any window [x, x+K].
        # -----------------------------------------------------------------------

        # Collect all node values from all diagonals
        all_values: List[int] = []
        for diag_vals in diagonal_map.values():
            all_values.extend(diag_vals)

        # Sort all values to use sliding window
        all_values.sort()

        n = len(all_values)
        min_splits = 1  # At minimum, we need 1 group

        # Sliding window: find maximum number of values in window [all_values[left], all_values[left] + K]
        # Two pointers: left and right
        left = 0
        for right in range(n):
            # Shrink window from left while the range exceeds K
            while all_values[right] - all_values[left] > K:
                left += 1
            # Window size is (right - left + 1)
            window_size = right - left + 1
            min_splits = max(min_splits, window_size)

        # -----------------------------------------------------------------------
        # STEP 5: Return the result
        # -----------------------------------------------------------------------
        return diagonals, min_splits

    def build_tree(self, values: List[Optional[int]]) -> Optional[TreeNode]:
        """
        Build a binary tree from a level-order list representation.

        None values in the list represent missing nodes (null children).

        Args:
            values: List of integers and None values in level-order.

        Returns:
            The root TreeNode of the constructed binary tree.

        Time Complexity: O(N) where N is the number of elements in values.
        Space Complexity: O(N) for the queue used during construction.
        """
        if not values or values[0] is None:
            return None

        # Create the root node
        root = TreeNode(values[0])
        queue: deque = deque([root])
        i = 1  # Index into the values list

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


# -------------------------------------------------------------------------------
# MAIN: Test the solution with the provided examples
# -------------------------------------------------------------------------------

if __name__ == "__main__":
    solution = Solution()

    # ===========================================================================
    # Example 1:
    # Input: root = [8, 3, 10, 1, 6, null, 14, null, null, 4, 7, 13], K = 2
    # Tree structure:
    #         8
    #        / \
    #       3   10
    #      / \    \
    #     1   6   14
    #        / \ /
    #       4  7 13
    #
    # Diagonal 0 (rightmost): 8 -> right to 10 -> right to 14 => [8, 10, 14]
    # Diagonal 1: 3 (left of 8, diag=1), 6 (right of 3, same diag=1), 7 (right of 6, diag=1)
    #             => [3, 6, 7]
    # Diagonal 2: 1 (left of 3, diag=2), 4 (left of 6, diag=2), 13 (left of 14, diag=1... wait)
    #
    # Let me re-trace:
    # - 8: diag=0
    # - 8's right=10: diag=0; 8's left=3: diag=1
    # - 10's right=14: diag=0; 3's right=6: diag=1; 3's left=1: diag=2
    # - 14's left=13: diag=1; 6's right=7: diag=1; 6's left=4: diag=2
    #
    # So:
    # diag 0: [8, 10, 14]
    # diag 1: [3, 6, 13, 7]  <- BFS order: 3 visited before 6, then 14's left=13, then 6's right=7
    # diag 2: [1, 4]
    #
    # Hmm, but expected output says diagonal 1 = [3, 6, 7] and diagonal 2 = [1, 4, 13].
    # Let me re-read: "14's left child is 13" — so 13 is left child of 14.
    # 14 is at diag=0. 14's left child 13 gets diag=0+1=1.
    # 6 is at diag=1. 6's left child 4 gets diag=2. 6's right child 7 gets diag=1.
    #
    # BFS order of visiting:
    # Level 0: (8, diag=0)
    # Level 1: (3, diag=1), (10, diag=0)  [left of 8, right of 8]
    # Level 2: (1, diag=2), (6, diag=1), (14, diag=0)  [left of 3, right of 3, right of 10]
    # Level 3: (4, diag=2), (7, diag=1), (13, diag=1)  [left of 6, right of 6, left of 14]
    #
    # So:
    # diag 0: [8, 10, 14]
    # diag 1: [3, 6, 7, 13]
    # diag 2: [1, 4]
    #
    # But expected says diag 1 = [3, 6, 7] and diag 2 = [1, 4, 13].
    # The expected output places 13 in diagonal 2. Let me re-check.
    # 14 is at diag=0. 14's left child 13: diag = 0 + 1 = 1. So 13 should be in diag 1.
    # But expected says diag 2 = [1, 4, 13].
    #
    # Hmm, maybe the expected output in the problem has 13 in diagonal 2 because
    # the problem counts differently. Let me re-read the problem example.
    # "Diagonal 2 contains [1, 4, 13]"
    # 
    # Looking at the tree again: 14's left child is 13.
    # If 14 is at diagonal 0, then 13 (left of 14) should be at diagonal 1.
    # But the expected says diagonal 2 = [1, 4, 13].
    #
    # Wait — maybe I'm misreading the tree. Let me re-read:
    # "8 is root; left child 3, right child 10; 3's children are 1 and 6; 
    #  10's right child is 14; 6's children are 4 and 7; 14's left child is 13."
    #
    # So the tree is:
    #         8
    #        / \
    #       3   10
    #      / \    \
    #     1   6   14
    #        / \ /
    #       4  7 13
    #
    # 14 is right child of 10. 10 is right child of 8. So:
    # 8: diag=0
    # 10 (right of 8): diag=0
    # 14 (right of 10): diag=0
    # 13 (left of 14): diag=0+1=1
    #
    # 3 (left of 8): diag=1
    # 6 (right of 3): diag=1
    # 7 (right of 6): diag=1
    # 4 (left of 6): diag=2
    # 1 (left of 3): diag=2
    #
    # So diag 0: [8, 10, 14], diag 1: [3, 6, 7, 13], diag 2: [1, 4]
    # But expected: diag 0: [8,10,14], diag 1: [3,6,7], diag 2: [1,4,13]
    #
    # The expected output places 13 in diagonal 2. This would only happen if
    # 13 is considered to be at diagonal 2. That would mean 14 is at diagonal 1,
    # which would mean 10 is at diagonal 1... but 10 is right child of 8 (diag=0),
    # so 10 should be diag=0.
    #
    # OR maybe the problem uses a different convention: the level-order input
    # [8, 3, 10, 1, 6, null, 14, null, null, 4, 7, 13] places 13 as the last node.
    # In level-order: 8(root), 3(left of 8), 10(right of 8), 1(left of 3),
    # 6(right of 3), null(left of 10), 14(right of