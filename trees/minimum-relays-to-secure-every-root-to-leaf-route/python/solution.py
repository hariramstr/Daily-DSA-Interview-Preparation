"""
Title: Minimum Relays to Secure Every Root-to-Leaf Route

Problem Description:
You are given the root of a binary tree representing a hierarchy of communication towers.
Every node is a tower, and every edge is a direct communication link between a tower and
one of its children. A security relay can be installed on any tower. If a relay is
installed on a tower, then that tower, its parent, and its immediate children are
considered secured.

Your task is to return the minimum number of relays needed so that every tower in the
tree is secured.

A tower is secured if at least one of the following is true:
1. A relay is installed on that tower.
2. Its parent has a relay.
3. One of its children has a relay.

You must cover the entire tree, including the root and all leaves.

The input tree can be assumed to be a standard binary tree where each node has at most
two children. Node values are unique integers but are only identifiers; they do not
affect the answer.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Each node has at most two children.
- Node values are in the range [-1000000000, 1000000000].
- The solution should run in O(n) time.
- Recursive solutions should be careful about stack depth on highly skewed trees.

Examples:
1) root = [0,0,null,0,0]
   Output: 1

2) root = [0,0,null,0,null,0,null,null,0]
   Output: 2

Follow-up:
Can you solve it using a postorder traversal with a small number of states per node,
instead of trying all placement combinations?
"""

from __future__ import annotations

from collections import deque
from typing import Deque, Dict, List, Optional, Tuple


class TreeNode:
    """Binary tree node used for the communication tower hierarchy."""

    def __init__(
        self,
        val: int = 0,
        left: Optional["TreeNode"] = None,
        right: Optional["TreeNode"] = None,
    ) -> None:
        """
        Initialize a tree node.

        Args:
            val: Integer identifier of the node.
            left: Left child.
            right: Right child.

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
    def minRelayCover(self, root: Optional[TreeNode]) -> int:
        """
        Compute the minimum number of relays needed to secure every node in the tree.

        This method uses an iterative postorder traversal so it remains safe even for
        highly skewed trees with up to 100000 nodes. Each node is assigned one of three
        compact states after its children are processed:

        State 0: This node is NOT secured.
                 It does not have a relay, and none of its children has a relay.
                 It is asking its parent to secure it.

        State 1: This node HAS a relay.
                 Therefore, it secures itself, its parent, and its immediate children.

        State 2: This node is secured, but it does NOT have a relay.
                 This happens when one of its children has a relay.

        Core greedy rule:
        - If any child is in state 0 (unsecured), we MUST place a relay on the current node.
        - Else if any child is in state 1 (child has relay), current node is secured.
        - Else current node becomes state 0 (unsecured), hoping its parent will cover it.

        Finally, if the root ends in state 0, we must place one more relay at the root.

        Args:
            root: Root of the binary tree.

        Returns:
            Minimum number of relays required.

        Time complexity:
            O(n), where n is the number of nodes, because each node is pushed/popped a
            constant number of times.

        Space complexity:
            O(n) in the worst case for the explicit traversal stack and state map.
        """
        # Edge case:
        # If the tree is empty, there are no towers to secure, so the answer is 0.
        if root is None:
            return 0

        # This counter stores how many relays we install in total.
        relay_count: int = 0

        # We need postorder traversal:
        # process left subtree, then right subtree, then the node itself.
        #
        # Because recursion can overflow on a deep tree, we simulate postorder iteratively.
        #
        # Stack entries are tuples:
        #   (node, visited_flag)
        #
        # visited_flag == False:
        #   We are seeing this node for the first time.
        #   We should come back later after its children are processed.
        #
        # visited_flag == True:
        #   Both children have already been handled, so now we can compute this node's state.
        stack: List[Tuple[Optional[TreeNode], bool]] = [(root, False)]

        # This dictionary stores the computed state for each processed node.
        #
        # Why a dictionary?
        # During iterative postorder, when we process a node, we need to read the states
        # of its left and right children. Since children are processed earlier, their
        # states are already available here.
        #
        # We use the node object itself as the key.
        state: Dict[TreeNode, int] = {}

        # Process the entire tree.
        while stack:
            node, visited = stack.pop()

            # Ignore null children. In tree algorithms, null children are often treated
            # specially. Here, we simply skip them in the stack and later interpret them
            # as "secured without needing a relay".
            if node is None:
                continue

            if not visited:
                # First time we see this node.
                #
                # To simulate postorder, we push:
                # 1) the current node marked as visited=True, so it will be processed later
                # 2) the right child
                # 3) the left child
                #
                # Because stack is LIFO, left and right children will be processed before
                # the current node, which is exactly what we need.
                stack.append((node, True))
                stack.append((node.right, False))
                stack.append((node.left, False))
            else:
                # Now both children have already been processed, so their states are known.
                #
                # For a missing child, we treat it as state 2:
                # "secured, no relay".
                #
                # Why state 2 for null?
                # A null child does not need coverage and should not force us to place a relay.
                # Treating null as secured is the standard trick that makes leaf handling elegant.
                left_state: int = state.get(node.left, 2)
                right_state: int = state.get(node.right, 2)

                # Decision 1:
                # If ANY child is unsecured (state 0), then the current node must install
                # a relay to secure that child.
                #
                # This is the greedy heart of the algorithm.
                # Waiting longer is impossible because only the current node or the child
                # itself could secure that child from below/above, and the child's subtree
                # has already been optimally processed.
                if left_state == 0 or right_state == 0:
                    state[node] = 1
                    relay_count += 1

                # Decision 2:
                # Otherwise, if ANY child has a relay (state 1), then the current node is
                # already secured by that child.
                elif left_state == 1 or right_state == 1:
                    state[node] = 2

                # Decision 3:
                # Otherwise, both children are secured but neither has a relay.
                # That means the current node is not secured by any child, and it does not
                # have a relay itself, so it becomes unsecured (state 0).
                #
                # We intentionally leave it uncovered for now, hoping its parent will place
                # a relay if needed. This postponement is what keeps the solution minimal.
                else:
                    state[node] = 0

        # After processing the whole tree, the root has no parent.
        # So if the root is still unsecured, we have no choice but to place a relay there.
        if state[root] == 0:
            relay_count += 1

        return relay_count


def build_tree_level_order(values: List[Optional[int]]) -> Optional[TreeNode]:
    """
    Build a binary tree from a level-order list representation.

    The input format follows the common convention:
    - values[i] is a node value or None
    - children are assigned from left to right level by level

    Example:
        [0, 0, None, 0, 0]
    means:
            0
           /
          0
         / \
        0   0

    Args:
        values: Level-order list with integers or None.

    Returns:
        Root of the constructed binary tree.

    Time complexity:
        O(n), where n is the number of entries in the list.

    Space complexity:
        O(n), for the queue and created nodes.
    """
    if not values:
        return None

    # If the first value is None, the tree is empty.
    if values[0] is None:
        return None

    # Create the root node from the first value.
    root = TreeNode(values[0])

    # Queue of nodes whose children we still need to assign.
    queue: Deque[TreeNode] = deque([root])

    # Index into the values list for reading child entries.
    index: int = 1

    # Continue until we run out of parent nodes or input values.
    while queue and index < len(values):
        current = queue.popleft()

        # Assign left child if available.
        if index < len(values):
            left_value = values[index]
            index += 1
            if left_value is not None:
                current.left = TreeNode(left_value)
                queue.append(current.left)

        # Assign right child if available.
        if index < len(values):
            right_value = values[index]
            index += 1
            if right_value is not None:
                current.right = TreeNode(right_value)
                queue.append(current.right)

    return root


if __name__ == "__main__":
    # Create a Solution instance.
    solver = Solution()

    # Example 1:
    # Input: [0, 0, None, 0, 0]
    # Structure:
    #       0
    #      /
    #     0
    #    / \
    #   0   0
    #
    # One relay on the left child secures all nodes.
    example_1 = build_tree_level_order([0, 0, None, 0, 0])
    result_1 = solver.minRelayCover(example_1)
    print("Example 1 result:", result_1)  # Expected: 1

    # Example 2:
    # Input: [0, 0, None, 0, None, 0, None, None, 0]
    #
    # This is a chain-like tree. One relay is not enough.
    # The optimal answer is 2.
    example_2 = build_tree_level_order([0, 0, None, 0, None, 0, None, None, 0])
    result_2 = solver.minRelayCover(example_2)
    print("Example 2 result:", result_2)  # Expected: 2

    # Additional small sanity checks for beginners:

    # Single node tree:
    # One relay is needed on the root.
    single = build_tree_level_order([1])
    print("Single node result:", solver.minRelayCover(single))  # Expected: 1

    # Root with two children:
    # One relay on the root secures all three nodes.
    small_full = build_tree_level_order([1, 2, 3])
    print("Small full tree result:", solver.minRelayCover(small_full))  # Expected: 1