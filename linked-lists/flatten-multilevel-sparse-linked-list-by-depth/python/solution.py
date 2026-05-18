```python
"""
Title: Flatten a Multilevel Sparse Linked List by Depth
Difficulty: Hard
Topic: Linked Lists

Problem Description:
You are given the head of a multilevel linked list. Each node contains an integer value,
a `next` pointer to the next node at the same level, and a `child` pointer that may point
to the head of another linked list (creating a new sub-level). Your task is to flatten the
multilevel linked list into a single-level linked list such that nodes are ordered by their
depth first, then by their original left-to-right order within each depth level.

In other words, collect all nodes at depth 1 (the top level) in order, then all nodes at
depth 2 in order (left-to-right across all sub-lists at that depth), then depth 3, and so on.
Return the head of the resulting flattened list. The `child` and `next` pointers of the
returned list should all be set to null except for `next` which chains the flattened result.

Constraints:
- The number of nodes in the list is in the range [1, 10^4].
- Node values are in the range [-10^5, 10^5].
- The depth of nesting is at most 1000.
- No cycles exist in the list.

Example 1:
Input: 1 - 2 - 3, where node 2 has a child list 4 - 5, and node 5 has a child list 6 - 7.
Depth 1: [1, 2, 3]
Depth 2: [4, 5]
Depth 3: [6, 7]
Output: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7

Example 2:
Input: 10 - 20 - 30, where node 10 has child 11 - 12 and node 20 has child 21 - 22.
Node 12 has child 100.
Depth 1: [10, 20, 30]
Depth 2: [11, 12, 21, 22]
Depth 3: [100]
Output: 10 -> 20 -> 30 -> 11 -> 12 -> 21 -> 22 -> 100
"""

from collections import deque
from typing import Optional, List, Dict


class Node:
    """Represents a node in a multilevel linked list."""
    
    def __init__(self, val: int = 0, next: 'Optional[Node]' = None, child: 'Optional[Node]' = None):
        self.val = val
        self.next = next
        self.child = child


class Solution:
    """Solution class for flattening a multilevel linked list by depth (BFS order)."""
    
    def flatten(self, head: Optional[Node]) -> Optional[Node]:
        """
        Flatten a multilevel linked list by depth (BFS level-order traversal).
        
        The key insight: we want BFS order — process all nodes at depth 1 first,
        then all nodes at depth 2, then depth 3, etc. Within each depth level,
        nodes appear left-to-right as they appear in the original structure.
        
        Strategy:
        - Use a queue (BFS) where we enqueue the heads of child lists as we
          encounter them during traversal of each level.
        - We process the top-level list first (depth 1), and whenever we see
          a node with a child, we enqueue that child's head for later processing.
        - After finishing depth 1, we process all the child heads we enqueued
          (depth 2), and so on.
        
        This is essentially a BFS traversal where:
        - Each "level" in BFS corresponds to a depth level in the multilevel list.
        - The queue holds the starting nodes of each sub-list at the next depth.
        
        Args:
            head: The head node of the multilevel linked list.
        
        Returns:
            The head of the flattened single-level linked list ordered by depth.
        
        Time Complexity: O(N) where N is the total number of nodes.
            - Each node is visited exactly once.
        
        Space Complexity: O(W) where W is the maximum number of child lists
            at any single depth level (width of the BFS queue).
            In the worst case this could be O(N).
        """
        
        # -----------------------------------------------------------------------
        # EDGE CASE: If the list is empty, return None immediately.
        # -----------------------------------------------------------------------
        if not head:
            return None
        
        # -----------------------------------------------------------------------
        # SETUP: We'll collect all nodes in BFS (depth-level) order into a list,
        # then re-link them into a single flat linked list.
        #
        # Why collect into a list first?
        # It makes it easy to re-link nodes without worrying about pointer
        # manipulation during traversal.
        # -----------------------------------------------------------------------
        result_nodes: List[Node] = []
        
        # -----------------------------------------------------------------------
        # BFS QUEUE: We use a deque for efficient O(1) append and popleft.
        #
        # The queue stores the HEAD of each sub-list (at any depth level)
        # that we need to process. We start by enqueuing the top-level head.
        #
        # Key idea: When we process a sub-list, we traverse it node by node.
        # If any node has a child, we enqueue that child's head to be processed
        # AFTER all nodes at the current depth are done.
        #
        # This naturally gives us BFS (breadth-first) ordering by depth.
        # -----------------------------------------------------------------------
        queue: deque = deque()
        queue.append(head)
        
        # -----------------------------------------------------------------------
        # BFS TRAVERSAL: Process each sub-list head from the queue.
        #
        # Outer loop: Each iteration processes one "sub-list" (a chain of nodes
        # connected by `next` pointers at the same depth level).
        # -----------------------------------------------------------------------
        while queue:
            # Dequeue the head of the next sub-list to process.
            # This sub-list is at the current depth level being processed.
            current: Optional[Node] = queue.popleft()
            
            # -------------------------------------------------------------------
            # INNER TRAVERSAL: Walk through all nodes in this sub-list
            # (following `next` pointers until we reach the end of this chain).
            # -------------------------------------------------------------------
            while current is not None:
                # Add this node to our result collection.
                # We'll use it to build the flat linked list later.
                result_nodes.append(current)
                
                # ---------------------------------------------------------------
                # CHILD DETECTION: If this node has a child, it means there's
                # a sub-list at the next depth level starting at current.child.
                #
                # We enqueue the child's head so it will be processed AFTER
                # all nodes at the current depth level are done.
                #
                # Why enqueue instead of processing immediately?
                # Because we want BFS order: finish the current depth level
                # before going deeper. If we processed children immediately,
                # we'd get DFS (depth-first) order instead.
                # ---------------------------------------------------------------
                if current.child is not None:
                    queue.append(current.child)
                
                # Move to the next node in the current sub-list.
                current = current.next
        
        # -----------------------------------------------------------------------
        # RE-LINKING: Now that we have all nodes in the correct BFS order,
        # we re-link them into a single flat linked list.
        #
        # For each node (except the last), set its `next` to the following node.
        # Clear the `child` pointer for all nodes (as required by the problem).
        # -----------------------------------------------------------------------
        n = len(result_nodes)
        
        for i in range(n):
            node = result_nodes[i]
            
            # Clear the child pointer — the flattened list has no sub-levels.
            node.child = None
            
            if i < n - 1:
                # Link this node to the next node in BFS order.
                node.next = result_nodes[i + 1]
            else:
                # Last node: its next should be None (end of list).
                node.next = None
        
        # -----------------------------------------------------------------------
        # RETURN: The first node in our collected list is the head of the
        # flattened result.
        # -----------------------------------------------------------------------
        return result_nodes[0]


# =============================================================================
# HELPER FUNCTIONS for building and displaying linked lists
# =============================================================================

def build_list(values: List[int]) -> Optional[Node]:
    """
    Build a simple single-level linked list from a list of integers.
    
    Args:
        values: List of integer values for the nodes.
    
    Returns:
        Head of the newly created linked list, or None if values is empty.
    """
    if not values:
        return None
    
    # Create the head node with the first value.
    head = Node(values[0])
    current = head
    
    # Create and link subsequent nodes.
    for val in values[1:]:
        current.next = Node(val)
        current = current.next
    
    return head


def list_to_values(head: Optional[Node]) -> List[int]:
    """
    Convert a linked list to a Python list of values for easy display.
    
    Args:
        head: Head of the linked list.
    
    Returns:
        List of integer values in order.
    """
    values = []
    current = head
    while current is not None:
        values.append(current.val)
        current = current.next
    return values


def print_list(head: Optional[Node], label: str = "") -> None:
    """
    Print the linked list values in a readable format.
    
    Args:
        head: Head of the linked list.
        label: Optional label to print before the list.
    """
    values = list_to_values(head)
    arrow_str = " -> ".join(str(v) for v in values)
    if label:
        print(f"{label}: {arrow_str}")
    else:
        print(arrow_str)


# =============================================================================
# MAIN: Test with the examples from the problem description
# =============================================================================

if __name__ == "__main__":
    solution = Solution()
    
    print("=" * 60)
    print("EXAMPLE 1")
    print("=" * 60)
    print("Structure:")
    print("  Level 1: 1 - 2 - 3")
    print("  Node 2's child: 4 - 5")
    print("  Node 5's child: 6 - 7")
    print()
    print("Expected depth ordering:")
    print("  Depth 1: [1, 2, 3]")
    print("  Depth 2: [4, 5]")
    print("  Depth 3: [6, 7]")
    print("Expected output: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7")
    print()
    
    # Build Example 1:
    # Level 1: 1 - 2 - 3
    node1 = Node(1)
    node2 = Node(2)
    node3 = Node(3)
    node1.next = node2
    node2.next = node3
    
    # Level 2 (child of node2): 4 - 5
    node4 = Node(4)
    node5 = Node(5)
    node4.next = node5
    node2.child = node4
    
    # Level 3 (child of node5): 6 - 7
    node6 = Node(6)
    node7 = Node(7)
    node6.next = node7
    node5.child = node6
    
    result1 = solution.flatten(node1)
    print_list(result1, "Actual output")
    
    # Verify
    actual1 = list_to_values(result1)
    expected1 = [1, 2, 3, 4, 5, 6, 7]
    print(f"Correct: {actual1 == expected1}")
    
    print()
    print("=" * 60)
    print("EXAMPLE 2")
    print("=" * 60)
    print("Structure:")
    print("  Level 1: 10 - 20 - 30")
    print("  Node 10's child: 11 - 12")
    print("  Node 20's child: 21 - 22")
    print("  Node 12's child: 100")
    print()
    print("Expected depth ordering:")
    print("  Depth 1: [10, 20, 30]")
    print("  Depth 2: [11, 12, 21, 22]")
    print("  Depth 3: [100]")
    print("Expected output: 10 -> 20 -> 30 -> 11 -> 12 -> 21 -> 22 -> 100")
    print()
    
    # Build Example 2:
    # Level 1: 10 - 20 - 30
    n10 = Node(10)
    n20 = Node(20)
    n30 = Node(30)
    n10.next = n20
    n20.next = n30
    
    # Child of node 10: 11 - 12
    n11 = Node(11)
    n12 = Node(12)
    n11.next = n12
    n10.child = n11
    
    # Child of node 20: 21 - 22
    n21 = Node(21)
    n22 = Node(22)
    n21.next = n22
    n20.child = n21
    
    # Child of node 12: 100
    n100 = Node(100)
    n12.child = n100
    
    result2 = solution.flatten(n10)
    print_list(result2, "Actual output")
    
    # Verify
    actual2 = list_to_values(result2)
    expected2 = [10, 20, 30, 11, 12, 21, 22, 100]
    print(f"Correct: {actual2 == expected2}")
    
    print()
    print("=" * 60)
    print("EXAMPLE 3: Single node (edge case)")
    print("=" * 60)
    single = Node(42)
    result3 = solution.flatten(single)
    print_list(result3, "Actual output")
    actual3 = list_to_values(result3)
    expected3 = [42]
    print(f"Correct: {actual3 == expected3}")
    
    print()
    print("=" * 60)
    print("EXAMPLE 4: No children (flat list)")
    print("=" * 60)
    flat_head = build_list([1, 2, 3, 4, 5])
    result4 = solution.flatten(flat_head)
    print_list(result4, "Actual output")
    actual4 = list_to_values(result4)
    expected4 = [1, 2, 3, 4, 5]
    print(f"Correct: {actual4 == expected4}")
    
    print()
    print("=" * 60)
    print("EXAMPLE 5: Deep nesting (chain of children)")
    print("=" * 60)
    print("Structure: 1 -> (child: 2 -> (child: 3 -> (child: 4)))")
    print("Expected: 1 -> 2 -> 3 -> 4")
    
    # Build a chain: each node has one child
    d1 = Node(1)
    d2 = Node(2)
    d3 = Node(3)
    d4 = Node(4)
    d1.child = d2
    d2.child = d3
    d3.child = d4
    
    result5 = solution.flatten(d1)
    print_list(result5, "Actual output")
    actual5 = list_to_values(result5)
    expected5 = [1, 2, 3, 4]
    print(f"Correct: {actual5 == expected5}")
    
    print()
    print("=" * 60)
    print("EXAMPLE 6: Multiple children at