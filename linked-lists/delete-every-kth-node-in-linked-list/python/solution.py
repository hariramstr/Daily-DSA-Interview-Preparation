```python
"""
Title: Delete Every K-th Node in a Linked List
Difficulty: Easy
Topic: Linked Lists

Problem Description:
You are given the head of a singly linked list and a positive integer k.
Your task is to delete every k-th node from the list and return the head
of the modified list.

Specifically, keep the first k-1 nodes, delete the k-th node, keep the
next k-1 nodes, delete the next k-th node, and so on until the end of
the list.

If the total number of nodes is not a multiple of k, the remaining nodes
at the end (fewer than k nodes) should all be kept.

Constraints:
- The number of nodes in the list is in the range [1, 10^4].
- 1 <= Node.val <= 1000
- 1 <= k <= 10^4

Example 1:
- Input: head = [1, 2, 3, 4, 5, 6, 7, 8], k = 3
- Output: [1, 2, 4, 5, 7, 8]
- Explanation: The 3rd node (value 3) and the 6th node (value 6) are deleted.
  Nodes at positions 7 and 8 are kept since there are fewer than k=3 remaining
  after position 6.

Example 2:
- Input: head = [10, 20, 30, 40, 50], k = 2
- Output: [10, 30, 50]
- Explanation: Nodes at positions 2 (value 20) and 4 (value 40) are deleted.
  The node at position 5 (value 50) is kept since it is the only remaining
  node after the last deletion.
"""

from typing import Optional, List


# ─────────────────────────────────────────────
# Node definition for a singly linked list
# ─────────────────────────────────────────────
class ListNode:
    """A single node in a singly linked list."""

    def __init__(self, val: int = 0, next: "Optional[ListNode]" = None):
        self.val = val
        self.next = next


# ─────────────────────────────────────────────
# Helper utilities (build / convert list)
# ─────────────────────────────────────────────
def build_linked_list(values: List[int]) -> Optional[ListNode]:
    """Convert a Python list of integers into a linked list and return its head."""
    if not values:
        return None
    head = ListNode(values[0])
    current = head
    for val in values[1:]:
        current.next = ListNode(val)
        current = current.next
    return head


def linked_list_to_python_list(head: Optional[ListNode]) -> List[int]:
    """Convert a linked list back to a Python list (for easy printing/testing)."""
    result: List[int] = []
    current = head
    while current:
        result.append(current.val)
        current = current.next
    return result


# ─────────────────────────────────────────────
# Solution
# ─────────────────────────────────────────────
class Solution:
    """Contains the algorithm to delete every k-th node from a linked list."""

    def delete_every_kth_node(
        self, head: Optional[ListNode], k: int
    ) -> Optional[ListNode]:
        """
        Delete every k-th node from the linked list and return the modified head.

        Strategy:
        ---------
        We use a **dummy (sentinel) node** placed before the real head.
        This simplifies edge cases where the head itself might need to be
        deleted (e.g., k == 1).

        We maintain two pointers:
          • `prev`  – the node just BEFORE the one we might delete.
                      When we need to delete the k-th node, we set
                      prev.next = prev.next.next, effectively unlinking it.
          • `current` – the node we are currently examining.

        We also keep a counter that resets to 1 after each deletion.

        Args:
            head: The head node of the singly linked list.
            k:    Delete every k-th node (1-indexed counting).

        Returns:
            The head of the modified linked list.

        Time Complexity:  O(n)  – we visit each node exactly once.
        Space Complexity: O(1)  – only a fixed number of pointer variables used.
        """

        # ── Step 1: Handle the trivial edge case ──────────────────────────────
        # If k == 1, every single node must be deleted, leaving an empty list.
        if k == 1:
            return None  # All nodes are deleted.

        # ── Step 2: Create a dummy node ────────────────────────────────────────
        # A dummy (sentinel) node with an arbitrary value is placed before the
        # real head.  This means `prev` always has a valid node to work with,
        # even when we need to delete the very first real node.
        #
        #   dummy → node1 → node2 → node3 → ...
        #
        dummy = ListNode(0)
        dummy.next = head  # Link dummy to the real list.

        # ── Step 3: Initialise traversal pointers and counter ─────────────────
        # `prev`    starts at the dummy node (the node BEFORE the first real node).
        # `current` starts at the first real node.
        # `count`   tracks how many nodes we have seen since the last deletion
        #           (or since the start).  When count reaches k, we delete.
        prev: ListNode = dummy
        current: Optional[ListNode] = head
        count: int = 1  # We are about to examine the 1st node.

        # ── Step 4: Traverse the entire list ──────────────────────────────────
        while current is not None:
            # Check whether the current node is the k-th node in this group.
            if count == k:
                # ── Deletion step ──────────────────────────────────────────
                # We bypass `current` by linking `prev` directly to
                # `current.next`.
                #
                #   Before: prev → current → current.next
                #   After:  prev → current.next
                #
                # `prev` does NOT advance here because the node that is now
                # at prev.next is the first node of the NEXT group (count = 1).
                prev.next = current.next

                # Reset the counter: the next node we look at will be count = 1
                # in the new group.
                count = 1

                # Advance `current` to the node that is now prev.next
                # (the first node of the next group).
                current = prev.next

            else:
                # ── Normal (keep) step ─────────────────────────────────────
                # This node is NOT the k-th node, so we keep it.
                # Advance both `prev` and `current` by one position.
                prev = current
                current = current.next

                # Increment the counter to track our position within the group.
                count += 1

        # ── Step 5: Return the modified list ──────────────────────────────────
        # `dummy.next` is the real head of the modified list.
        # (The original head may or may not have been deleted, but dummy.next
        #  always points to whatever the first surviving node is.)
        return dummy.next


# ─────────────────────────────────────────────
# Manual trace verification
# ─────────────────────────────────────────────
# Example 1: [1, 2, 3, 4, 5, 6, 7, 8], k = 3
#   count=1 → keep 1  (prev=dummy, cur=1)
#   count=2 → keep 2  (prev=1,     cur=2)
#   count=3 → DELETE 3 (prev=2, cur=4, reset count=1)
#   count=1 → keep 4  (prev=2,     cur=4)  ← prev advances to 4
#   count=2 → keep 5  (prev=4,     cur=5)
#   count=3 → DELETE 6 (prev=5, cur=7, reset count=1)
#   count=1 → keep 7  (prev=5,     cur=7)
#   count=2 → keep 8  (prev=7,     cur=8)
#   current becomes None → stop
#   Result: [1, 2, 4, 5, 7, 8]  ✓
#
# Example 2: [10, 20, 30, 40, 50], k = 2
#   count=1 → keep 10  (prev=dummy, cur=10)
#   count=2 → DELETE 20 (prev=10, cur=30, reset count=1)
#   count=1 → keep 30  (prev=10,   cur=30)
#   count=2 → DELETE 40 (prev=30, cur=50, reset count=1)
#   count=1 → keep 50  (prev=30,   cur=50)
#   current becomes None → stop
#   Result: [10, 30, 50]  ✓


# ─────────────────────────────────────────────
# Main block – demonstrate with sample inputs
# ─────────────────────────────────────────────
if __name__ == "__main__":
    solution = Solution()

    # ── Test Case 1 ───────────────────────────────────────────────────────────
    print("=" * 50)
    print("Test Case 1")
    values1 = [1, 2, 3, 4, 5, 6, 7, 8]
    k1 = 3
    head1 = build_linked_list(values1)
    print(f"  Input list : {values1}")
    print(f"  k          : {k1}")

    result_head1 = solution.delete_every_kth_node(head1, k1)
    result1 = linked_list_to_python_list(result_head1)

    print(f"  Output list: {result1}")
    print(f"  Expected   : [1, 2, 4, 5, 7, 8]")
    print(f"  PASS       : {result1 == [1, 2, 4, 5, 7, 8]}")

    # ── Test Case 2 ───────────────────────────────────────────────────────────
    print("=" * 50)
    print("Test Case 2")
    values2 = [10, 20, 30, 40, 50]
    k2 = 2
    head2 = build_linked_list(values2)
    print(f"  Input list : {values2}")
    print(f"  k          : {k2}")

    result_head2 = solution.delete_every_kth_node(head2, k2)
    result2 = linked_list_to_python_list(result_head2)

    print(f"  Output list: {result2}")
    print(f"  Expected   : [10, 30, 50]")
    print(f"  PASS       : {result2 == [10, 30, 50]}")

    # ── Edge Case: k == 1 (delete every node) ─────────────────────────────────
    print("=" * 50)
    print("Edge Case: k = 1 (delete every node)")
    values3 = [1, 2, 3, 4, 5]
    k3 = 1
    head3 = build_linked_list(values3)
    print(f"  Input list : {values3}")
    print(f"  k          : {k3}")

    result_head3 = solution.delete_every_kth_node(head3, k3)
    result3 = linked_list_to_python_list(result_head3)

    print(f"  Output list: {result3}")
    print(f"  Expected   : []")
    print(f"  PASS       : {result3 == []}")

    # ── Edge Case: k larger than list length (delete nothing) ─────────────────
    print("=" * 50)
    print("Edge Case: k > len(list) (delete nothing)")
    values4 = [1, 2, 3]
    k4 = 10
    head4 = build_linked_list(values4)
    print(f"  Input list : {values4}")
    print(f"  k          : {k4}")

    result_head4 = solution.delete_every_kth_node(head4, k4)
    result4 = linked_list_to_python_list(result_head4)

    print(f"  Output list: {result4}")
    print(f"  Expected   : [1, 2, 3]")
    print(f"  PASS       : {result4 == [1, 2, 3]}")

    # ── Edge Case: single node, k == 1 ────────────────────────────────────────
    print("=" * 50)
    print("Edge Case: single node, k = 1")
    values5 = [42]
    k5 = 1
    head5 = build_linked_list(values5)
    print(f"  Input list : {values5}")
    print(f"  k          : {k5}")

    result_head5 = solution.delete_every_kth_node(head5, k5)
    result5 = linked_list_to_python_list(result_head5)

    print(f"  Output list: {result5}")
    print(f"  Expected   : []")
    print(f"  PASS       : {result5 == []}")

    # ── Edge Case: single node, k == 2 (keep it) ──────────────────────────────
    print("=" * 50)
    print("Edge Case: single node, k = 2 (keep it)")
    values6 = [42]
    k6 = 2
    head6 = build_linked_list(values6)
    print(f"  Input list : {values6}")
    print(f"  k          : {k6}")

    result_head6 = solution.delete_every_kth_node(head6, k6)
    result6 = linked_list_to_python_list(result_head6)

    print(f"  Output list: {result6}")
    print(f"  Expected   : [42]")
    print(f"  PASS       : {result6 == [42]}")
    print("=" * 50)
```