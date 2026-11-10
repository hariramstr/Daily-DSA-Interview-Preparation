"""
Title: Find the Nearest Open Seat in a Venue Tree

Problem Description:
A concert venue stores its seating layout as a binary tree. Each node represents a
seat section and contains a unique integer id. The root is the main entrance section.
Some sections are temporarily closed for cleaning, while the rest are open.

You are given the root of the binary tree, an integer target representing the id of a
section where a guest is currently standing, and a set closedSections containing the
ids of closed sections. Return the id of the open section that is closest to the target
section by number of edges traveled.

Distance is measured along tree edges, and movement is allowed both downward to children
and upward to parents. The target section itself may be open or closed. If the target
section is open, then it is considered a valid answer with distance 0. If multiple open
sections are tied for the minimum distance, return the one with the smallest section id.
If the target id does not exist in the tree, return -1. If every section is closed,
return -1.

This problem asks you to search a tree as if it were an undirected graph. A common
approach is to first record parent pointers, then run a breadth-first search starting
from the target node while skipping visited nodes.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Node ids are unique integers in the range [1, 10^9].
- closedSections contains between 0 and n ids.
- target is an integer in the range [1, 10^9].
- Your solution should run in O(n) time and use O(n) extra space.
"""

from __future__ import annotations

from collections import deque
from typing import Deque, Dict, List, Optional, Set, Tuple


class TreeNode:
    """Binary tree node used to represent a seat section in the venue tree."""

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        self.val: int = val
        self.left: Optional["TreeNode"] = left
        self.right: Optional["TreeNode"] = right


class Solution:
    def build_parent_map_and_find_target(
        self, root: Optional[TreeNode], target: int
    ) -> Tuple[Dict[TreeNode, Optional[TreeNode]], Optional[TreeNode]]:
        """
        Build parent pointers for every node and locate the target node.

        Args:
            root: Root of the binary tree.
            target: The section id we want to start searching from.

        Returns:
            A tuple containing:
            - A dictionary mapping each node to its parent node
            - The TreeNode whose value equals target, or None if not found

        Time complexity:
            O(n), where n is the number of nodes in the tree.

        Space complexity:
            O(n), for the parent map and traversal queue.
        """
        # This dictionary lets us move "upward" in the tree during BFS later.
        # Trees normally only give direct access from parent to child.
        # Since the problem allows movement both downward and upward,
        # we explicitly record each node's parent.
        parent: Dict[TreeNode, Optional[TreeNode]] = {}

        # If the tree is empty, there is nothing to process.
        if root is None:
            return parent, None

        # Standard BFS queue for traversing the tree level by level.
        queue: Deque[TreeNode] = deque([root])

        # The root has no parent.
        parent[root] = None

        # We will store the actual node object once we find the target value.
        target_node: Optional[TreeNode] = None

        # Traverse every node exactly once.
        while queue:
            current: TreeNode = queue.popleft()

            # If this node matches the target id, remember it.
            if current.val == target:
                target_node = current

            # Record parent for the left child, then enqueue it.
            if current.left is not None:
                parent[current.left] = current
                queue.append(current.left)

            # Record parent for the right child, then enqueue it.
            if current.right is not None:
                parent[current.right] = current
                queue.append(current.right)

        return parent, target_node

    def findNearestOpenSeat(
        self, root: Optional[TreeNode], target: int, closedSections: Set[int]
    ) -> int:
        """
        Find the closest open section id to the target section in a binary tree.

        The tree is treated like an undirected graph by allowing movement to
        left child, right child, and parent. Breadth-first search is used
        because BFS explores nodes in increasing order of distance.

        Args:
            root: Root of the binary tree.
            target: The id of the section where the guest is currently standing.
            closedSections: Set of ids representing closed sections.

        Returns:
            The id of the nearest open section. If multiple open sections are tied
            at the same minimum distance, returns the smallest id among them.
            Returns -1 if target does not exist or if every section is closed.

        Time complexity:
            O(n), where n is the number of nodes in the tree.

        Space complexity:
            O(n), for the parent map, visited set, and BFS queue.
        """
        # Step 1:
        # Build parent pointers and locate the target node.
        # This transforms the tree into something we can traverse like an
        # undirected graph: from any node, we can go left, right, or up.
        parent, target_node = self.build_parent_map_and_find_target(root, target)

        # If the target id does not exist anywhere in the tree, the problem
        # requires us to return -1 immediately.
        if target_node is None:
            return -1

        # Step 2:
        # Run BFS starting from the target node.
        #
        # Why BFS?
        # Because BFS explores all nodes at distance 0, then all nodes at
        # distance 1, then all nodes at distance 2, and so on.
        # Therefore, the first distance level containing an open section
        # must be the minimum possible distance.
        queue: Deque[TreeNode] = deque([target_node])

        # We must avoid revisiting nodes, because once parent links are added,
        # the structure behaves like an undirected graph and cycles can occur:
        # child -> parent -> child.
        visited: Set[TreeNode] = {target_node}

        # Process the graph one distance layer at a time.
        while queue:
            # All nodes currently in the queue are at the same distance
            # from the target. We will inspect this entire layer first.
            level_size: int = len(queue)

            # We collect all open section ids found at this exact distance.
            # If this list becomes non-empty, we return the smallest id from it,
            # because the problem asks for the smallest id among ties.
            open_ids_at_this_distance: List[int] = []

            # Examine every node in the current BFS layer.
            for _ in range(level_size):
                current: TreeNode = queue.popleft()

                # If the current section is not closed, it is open and is a
                # valid candidate answer at the current minimum distance.
                if current.val not in closedSections:
                    open_ids_at_this_distance.append(current.val)

                # Explore the three possible neighbors:
                # 1) left child
                # 2) right child
                # 3) parent
                #
                # We gather them into a list to keep the logic compact and clear.
                neighbors: List[Optional[TreeNode]] = [
                    current.left,
                    current.right,
                    parent.get(current),
                ]

                # Add each unvisited neighbor to the queue for the next distance.
                for neighbor in neighbors:
                    if neighbor is not None and neighbor not in visited:
                        visited.add(neighbor)
                        queue.append(neighbor)

            # After checking the full layer, if we found any open sections,
            # this is guaranteed to be the minimum distance.
            # We return the smallest id among them to satisfy the tie-break rule.
            if open_ids_at_this_distance:
                return min(open_ids_at_this_distance)

        # If BFS finishes without finding any open section, then every section
        # in the tree must be closed.
        return -1


def build_tree_from_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    Args:
        values: List where each element is a node value or None.

    Returns:
        The root of the constructed binary tree.

    Time complexity:
        O(n), where n is the number of list entries.

    Space complexity:
        O(n), for the queue used during construction.
    """
    # An empty list means there is no tree.
    if not values:
        return None

    # If the first value is None, the tree is empty.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root: TreeNode = TreeNode(values[0])

    # Queue of nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index into the input list for reading child values.
    index: int = 1

    # Process nodes in level order.
    while queue and index < len(values):
        current: TreeNode = queue.popleft()

        # Assign left child if there is a remaining value.
        if index < len(values):
            left_value: Optional[int] = values[index]
            index += 1
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if there is a remaining value.
        if index < len(values):
            right_value: Optional[int] = values[index]
            index += 1
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Tree: [8,4,12,2,6,10,14]
    # Target: 4
    # Closed: [4,2]
    #
    # Distances from 4:
    # - 4 is distance 0 but closed
    # - neighbors 2, 6, 8 are distance 1
    #   2 is closed, 6 is open, 8 is open
    # Minimum distance with open sections is 1, and among {6, 8}, smallest is 6
    root1: Optional[TreeNode] = build_tree_from_level_order([8, 4, 12, 2, 6, 10, 14])
    target1: int = 4
    closed1: Set[int] = {4, 2}
    result1: int = solution.findNearestOpenSeat(root1, target1, closed1)
    print(result1)  # Expected: 6

    # Example 2
    # Tree: [5,3,9,1,None,7,11]
    # Target: 3
    # Closed: [1,3,5]
    #
    # Distances from 3:
    # - 3 is distance 0 but closed
    # - neighbors: 1 and 5 at distance 1, both closed
    # - from 5 we can reach 7 and 9 at distance 2, both open
    # Tie at distance 2 => choose smaller id => 7
    root2: Optional[TreeNode] = build_tree_from_level_order([5, 3, 9, 1, None, 7, 11])
    target2: int = 3
    closed2: Set[int] = {1, 3, 5}
    result2: int = solution.findNearestOpenSeat(root2, target2, closed2)
    print(result2)  # Expected: 7

    # Additional quick checks

    # Target itself is open, so answer should be target with distance 0.
    root3: Optional[TreeNode] = build_tree_from_level_order([10, 5, 15])
    print(solution.findNearestOpenSeat(root3, 5, {15}))  # Expected: 5

    # Target does not exist.
    print(solution.findNearestOpenSeat(root3, 99, set()))  # Expected: -1

    # Every section is closed.
    print(solution.findNearestOpenSeat(root3, 10, {10, 5, 15}))  # Expected: -1