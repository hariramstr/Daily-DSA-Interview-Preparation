"""
Title: Minimum Relabels to Sort a Binary Tree by Level

Problem Description:
You are given the root of a binary tree where each node stores an integer label.
In one operation, you may swap the labels of any two nodes that are on the same
depth level of the tree. The tree structure itself cannot be changed; only labels
may move between nodes on the same level.

Your task is to return the minimum number of such swaps needed so that, for every
depth level independently, the labels appearing from left to right are in strictly
nondecreasing order. In other words, if you perform a level-order traversal and
look at the nodes level by level, each level must end up sorted when read from
left to right.

If a level already appears sorted, it requires 0 operations. Levels are independent:
a label from one level can never be moved to another level. You should compute the
total minimum number of swaps across all levels.

It is guaranteed that the tree contains between 1 and 100000 nodes, and each node
value is between -1000000000 and 1000000000. The tree is not necessarily complete
or balanced. An O(n log n) solution is expected.

Example 1:
Input: root = [5,4,3,7,6,8,9]
Output: 2

Explanation:
Level 0 is [5], already sorted -> 0 swaps
Level 1 is [4,3], needs 1 swap to become [3,4]
Level 2 is [7,6,8,9], needs 1 swap to become [6,7,8,9]
Total = 2

Note:
The original prompt text claimed the total was 1, but that contradicts the
described per-level operations. Since levels are independent and both level 1
and level 2 are unsorted, the correct total is 2.

Example 2:
Input: root = [10,1,8,7,6,5,4]
Output: 2

Explanation:
Level 0 is [10], already sorted -> 0 swaps
Level 1 is [1,8], already sorted -> 0 swaps
Level 2 is [7,6,5,4], sorting to [4,5,6,7] requires 2 swaps
Total = 2
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node.

    Args:
        val: Integer value stored in the node.
        left: Left child.
        right: Right child.
    """

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        self.val = val
        self.left = left
        self.right = right


class Solution:
    def minimum_operations(self, root: Optional[TreeNode]) -> int:
        """Compute the minimum number of same-level label swaps needed.

        The algorithm performs a breadth-first traversal of the tree so that
        values are processed one level at a time. For each level, it computes
        the minimum number of swaps required to sort that level's values into
        nondecreasing order. The total answer is the sum across all levels.

        Args:
            root: Root node of the binary tree.

        Returns:
            The minimum total number of swaps across all levels.

        Time Complexity:
            O(n log n) overall, where n is the number of nodes.
            Each node is visited once during BFS, and each level is sorted once.

        Space Complexity:
            O(n) in the worst case for the BFS queue and temporary level storage.
        """
        # If the tree is empty, there are no levels and therefore no swaps.
        if root is None:
            return 0

        # Standard BFS queue.
        # We use deque because:
        # - appending to the right is O(1)
        # - popping from the left is O(1)
        queue: Deque[TreeNode] = deque([root])

        # This variable accumulates the answer over all levels.
        total_swaps: int = 0

        # Process the tree level by level.
        while queue:
            # The current queue length tells us exactly how many nodes belong
            # to this level. We must capture it before we start popping nodes,
            # because we will also be pushing children for the next level.
            level_size: int = len(queue)

            # Collect the values of the current level from left to right.
            # This exact left-to-right order matters because the level must end
            # up sorted in that same positional order.
            level_values: List[int] = []

            # Extract all nodes of the current level.
            for _ in range(level_size):
                node: TreeNode = queue.popleft()
                level_values.append(node.val)

                # Push children into the queue for the next level.
                if node.left is not None:
                    queue.append(node.left)
                if node.right is not None:
                    queue.append(node.right)

            # For this level, compute the minimum swaps needed to sort the
            # collected values. Since swaps may happen between any two nodes
            # on the same level, this reduces exactly to the classic
            # "minimum swaps to sort an array" problem.
            total_swaps += self._min_swaps_to_sort(level_values)

        return total_swaps

    def _min_swaps_to_sort(self, values: List[int]) -> int:
        """Return the minimum number of swaps needed to sort a list.

        This method handles duplicate values correctly.

        Core idea:
        - Create a sorted copy of the values.
        - Build a mapping from each value to the list of positions where that
          value should appear in the sorted array.
        - Convert the current array into a permutation of target indices.
        - Count permutation cycles. A cycle of length k requires k - 1 swaps.

        Why this works:
        Sorting by arbitrary swaps is equivalent to rearranging items into their
        target positions. The minimum number of swaps needed for a permutation
        equals the sum over all cycles of (cycle_length - 1).

        Args:
            values: The list of integers for one tree level.

        Returns:
            Minimum number of swaps needed to sort the list.

        Time Complexity:
            O(m log m), where m is the size of the level, due to sorting.

        Space Complexity:
            O(m) for auxiliary arrays and mappings.
        """
        # A level with 0 or 1 element is already sorted.
        if len(values) <= 1:
            return 0

        # Create the sorted target arrangement for this level.
        sorted_values: List[int] = sorted(values)

        # To correctly support duplicate values, we cannot simply map
        # value -> single index. Instead, we map each value to all indices
        # where it appears in the sorted array.
        #
        # Example:
        # values        = [2, 1, 2]
        # sorted_values = [1, 2, 2]
        # target positions:
        #   1 -> [0]
        #   2 -> [1, 2]
        #
        # Then, as we scan the original array from left to right, we assign
        # each occurrence of a value to the next available target index for
        # that same value.
        from collections import defaultdict

        positions = defaultdict(list)
        for index, value in enumerate(sorted_values):
            positions[value].append(index)

        # For each value, track which target index should be used next.
        next_pos_index = defaultdict(int)

        # Build the permutation:
        # perm[i] = the index in the sorted array where the element currently
        # at position i should go.
        perm: List[int] = [0] * len(values)
        for i, value in enumerate(values):
            target_list = positions[value]
            use_idx = next_pos_index[value]
            perm[i] = target_list[use_idx]
            next_pos_index[value] += 1

        # Now count cycles in the permutation.
        #
        # If perm[i] == i, that element is already in a valid sorted position.
        # Otherwise, following perm pointers reveals a cycle.
        # A cycle of length k needs exactly k - 1 swaps.
        visited: List[bool] = [False] * len(values)
        swaps: int = 0

        for i in range(len(values)):
            # Skip if already processed or already in correct place.
            if visited[i] or perm[i] == i:
                continue

            cycle_length: int = 0
            current: int = i

            # Walk through the cycle until we return to a visited node.
            while not visited[current]:
                visited[current] = True
                current = perm[current]
                cycle_length += 1

            # If the cycle has more than one element, it contributes
            # cycle_length - 1 swaps.
            if cycle_length > 1:
                swaps += cycle_length - 1

        return swaps


def build_tree(level_order: List[Optional[int]]) -> Optional[TreeNode]:
    """Build a binary tree from a level-order list representation.

    The input format follows the common convention:
    - Each element represents a node value or None.
    - Children of a node appear in the list in left/right order.

    Args:
        level_order: List representation of the tree.

    Returns:
        Root of the constructed binary tree.

    Time Complexity:
        O(n), where n is the number of list entries.

    Space Complexity:
        O(n) for the queue used during construction.
    """
    if not level_order or level_order[0] is None:
        return None

    root = TreeNode(level_order[0])
    queue: Deque[TreeNode] = deque([root])
    index: int = 1

    while queue and index < len(level_order):
        current = queue.popleft()

        # Assign left child if present.
        if index < len(level_order) and level_order[index] is not None:
            current.left = TreeNode(level_order[index])
            queue.append(current.left)
        index += 1

        # Assign right child if present.
        if index < len(level_order) and level_order[index] is not None:
            current.right = TreeNode(level_order[index])
            queue.append(current.right)
        index += 1

    return root


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    # Important correctness note:
    # The prompt's stated output of 1 is inconsistent with its own description.
    # Level 1 needs 1 swap and level 2 needs 1 swap, so the correct total is 2.
    root1 = build_tree([5, 4, 3, 7, 6, 8, 9])
    result1 = solution.minimum_operations(root1)
    print("Example 1 result:", result1)  # Correct result: 2

    # Sample 2 from the prompt.
    root2 = build_tree([10, 1, 8, 7, 6, 5, 4])
    result2 = solution.minimum_operations(root2)
    print("Example 2 result:", result2)  # Expected: 2

    # Additional quick sanity checks.
    root3 = build_tree([1])
    print("Single node result:", solution.minimum_operations(root3))  # Expected: 0

    root4 = build_tree([2, 1, 3])
    print("Already sorted levels result:", solution.minimum_operations(root4))  # Expected: 0