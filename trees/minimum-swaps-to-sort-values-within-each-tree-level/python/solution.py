"""
Title: Minimum Swaps to Sort Values Within Each Tree Level

Problem Description:
You are given the root of a binary tree where each node contains an integer value.
In one operation, you may choose any two nodes that are on the same depth level and
swap their values. The structure of the tree cannot be changed; only the values stored
in nodes may be swapped.

Your task is to return the minimum total number of such swaps required so that, for
every depth level independently, the values of the nodes on that level appear in
strictly increasing order from left to right.

If a level already appears in strictly increasing order, it contributes 0 to the
answer. Each level is processed independently, but the final answer is the sum of the
minimum swaps needed across all levels. You may assume that for every level, all values
are distinct, so a strictly increasing order is well-defined.

A common approach is to perform a breadth-first traversal to collect values level by
level, then compute the minimum number of swaps needed to sort each level. This turns
the tree problem into a repeated array-reordering problem.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- -1000000000 <= Node.val <= 1000000000
- All node values on the same level are distinct.
- The tree is a binary tree.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, List, Optional


class TreeNode:
    """Binary tree node."""

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
            left: Left child node.
            right: Right child node.

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
    def minimumOperations(self, root: Optional[TreeNode]) -> int:
        """
        Compute the minimum total number of swaps needed so that values on every
        tree level are strictly increasing from left to right.

        The algorithm performs a breadth-first traversal (level-order traversal)
        to process the tree one level at a time. For each level, it extracts the
        node values into an array, then computes the minimum number of swaps
        required to sort that array.

        Args:
            root: Root node of the binary tree.

        Returns:
            The minimum total number of swaps across all levels.

        Time complexity:
            O(n log n), where n is the number of nodes.
            Each node is visited once during BFS, and sorting all level arrays
            together costs O(n log n) in the worst case.

        Space complexity:
            O(n), due to the BFS queue and temporary arrays for level values.
        """
        # If the tree is empty, there are no levels to process and therefore
        # no swaps are needed.
        if root is None:
            return 0

        # We use a queue for breadth-first traversal.
        # BFS is the natural choice here because the problem is explicitly about
        # processing nodes level by level.
        queue: Deque[TreeNode] = deque([root])

        # This variable accumulates the answer across all levels.
        total_swaps: int = 0

        # Continue until every node has been processed.
        while queue:
            # The current size of the queue tells us exactly how many nodes
            # belong to the current depth level.
            level_size: int = len(queue)

            # We collect the values of the current level in left-to-right order.
            # This order matters because the target is "strictly increasing from
            # left to right" within the level.
            level_values: List[int] = []

            # Process exactly one level.
            for _ in range(level_size):
                node: TreeNode = queue.popleft()
                level_values.append(node.val)

                # Add children for the next level.
                if node.left is not None:
                    queue.append(node.left)
                if node.right is not None:
                    queue.append(node.right)

            # For the collected values of this level, compute the minimum number
            # of swaps needed to sort them.
            total_swaps += self._min_swaps_to_sort(level_values)

        return total_swaps

    def _min_swaps_to_sort(self, values: List[int]) -> int:
        """
        Compute the minimum number of swaps required to sort an array of distinct integers.

        This is done by analyzing the permutation from current positions to sorted
        positions. The minimum swaps needed equals the sum over all permutation
        cycles of (cycle_length - 1).

        Args:
            values: A list of distinct integers.

        Returns:
            The minimum number of swaps needed to sort the list in ascending order.

        Time complexity:
            O(m log m), where m is the length of the list, due to sorting.

        Space complexity:
            O(m), for the sorted-position mapping and visited tracking.
        """
        # Arrays of size 0 or 1 are already sorted.
        if len(values) <= 1:
            return 0

        # Create a sorted copy to know the target order.
        sorted_values: List[int] = sorted(values)

        # Map each value to the index where it should appear in the sorted array.
        # This works because the problem guarantees that values on the same level
        # are distinct.
        value_to_sorted_index = {value: index for index, value in enumerate(sorted_values)}

        # Build a permutation array:
        # target_index[i] tells us where the element currently at index i should go
        # in the sorted arrangement.
        target_index: List[int] = [value_to_sorted_index[value] for value in values]

        # Track which indices have already been processed as part of some cycle.
        visited: List[bool] = [False] * len(values)

        swaps: int = 0

        # Walk through each index and discover permutation cycles.
        for i in range(len(values)):
            # If already visited, this index is already part of a processed cycle.
            # If target_index[i] == i, the element is already in the correct place,
            # so it forms a cycle of length 1 and contributes 0 swaps.
            if visited[i] or target_index[i] == i:
                continue

            # Explore the cycle starting from index i.
            cycle_length: int = 0
            current: int = i

            while not visited[current]:
                visited[current] = True
                current = target_index[current]
                cycle_length += 1

            # A cycle of length k requires exactly (k - 1) swaps.
            swaps += cycle_length - 1

        return swaps


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input uses None to represent missing children.

    Args:
        values: Level-order list of node values.

    Returns:
        Root of the constructed binary tree.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), due to the queue used during construction.
    """
    if not values:
        return None

    if values[0] is None:
        return None

    root = TreeNode(values[0])
    queue: Deque[TreeNode] = deque([root])
    index: int = 1

    while queue and index < len(values):
        current = queue.popleft()

        if index < len(values) and values[index] is not None:
            current.left = TreeNode(values[index])
            queue.append(current.left)
        index += 1

        if index < len(values) and values[index] is not None:
            current.right = TreeNode(values[index])
            queue.append(current.right)
        index += 1

    return root


if __name__ == "__main__":
    solution = Solution()

    # Important note about Example 1:
    # The written explanation in the prompt says to assume the intended level order
    # is [5,3,4,7,6,8,9] so that the output is 1.
    #
    # Let's verify:
    # Level 0: [5] -> already sorted -> 0
    # Level 1: [3,4] -> already sorted -> 0
    # Level 2: [7,6,8,9] -> one swap (7 and 6) -> 1
    # Total = 1
    example1 = build_tree_from_level_order([5, 3, 4, 7, 6, 8, 9])
    print("Example 1 Output:", solution.minimumOperations(example1))  # Expected: 1

    # Example 2 from the prompt:
    # Level 0: [1] -> 0
    # Level 1: [7,5] -> 1 swap
    # Level 2: [4,6,8,9] -> already sorted -> 0
    # Total = 1
    example2 = build_tree_from_level_order([1, 7, 5, 4, 6, 8, 9])
    print("Example 2 Output:", solution.minimumOperations(example2))  # Expected: 1

    # Additional sanity check:
    # A tree where every level is already strictly increasing.
    example3 = build_tree_from_level_order([2, 1, 3, 4, 5, 6, 7])
    print("Example 3 Output:", solution.minimumOperations(example3))  # Expected: 0