```python
"""
Title: Reorder Linked List by Frequency Buckets
Difficulty: Medium
Topic: Linked Lists

Problem Description:
You are given the head of a singly linked list where each node contains a positive
integer value. Your task is to reorder the linked list such that nodes are grouped
by the frequency of their values in ascending order of frequency. Within the same
frequency group, nodes should appear in the order their value was first encountered
in the original list.

Specifically:
- Count the frequency of each unique value in the linked list.
- Reconstruct the linked list by placing nodes with the lowest frequency first,
  and nodes with the highest frequency last.
- If two values have the same frequency, the value that appeared first in the
  original list should come first in the reordered list.
- The relative order of nodes sharing the same value must be preserved
  (all nodes of the same value are grouped together).

Example 1:
  Input:  [4, 2, 4, 1, 2, 4]
  Output: [1, 2, 2, 4, 4, 4]
  Explanation: freq(1)=1, freq(2)=2, freq(4)=3 → sorted ascending by freq.

Example 2:
  Input:  [3, 1, 3, 2, 1, 3, 2]
  Output: [1, 1, 2, 2, 3, 3, 3]
  Explanation: freq(3)=3, freq(1)=2 (first seen idx 1), freq(2)=2 (first seen idx 3).
               Same frequency tie broken by first-seen order: 1 before 2.
"""

from __future__ import annotations
from typing import Optional, Dict, List
from collections import defaultdict


# ---------------------------------------------------------------------------
# Node definition for a singly linked list
# ---------------------------------------------------------------------------
class ListNode:
    """A single node in a singly linked list."""

    def __init__(self, val: int = 0, next: Optional["ListNode"] = None):
        self.val = val
        self.next = next


# ---------------------------------------------------------------------------
# Helper utilities
# ---------------------------------------------------------------------------

def build_linked_list(values: List[int]) -> Optional[ListNode]:
    """
    Convert a Python list of integers into a singly linked list.

    Args:
        values: List of integer values.

    Returns:
        Head node of the constructed linked list, or None if values is empty.
    """
    if not values:
        return None
    head = ListNode(values[0])
    current = head
    for v in values[1:]:
        current.next = ListNode(v)
        current = current.next
    return head


def linked_list_to_list(head: Optional[ListNode]) -> List[int]:
    """
    Convert a singly linked list back into a Python list for easy printing/testing.

    Args:
        head: Head node of the linked list.

    Returns:
        Python list of integer values in the order they appear in the list.
    """
    result: List[int] = []
    current = head
    while current:
        result.append(current.val)
        current = current.next
    return result


# ---------------------------------------------------------------------------
# Solution
# ---------------------------------------------------------------------------

class Solution:
    """Encapsulates the algorithm to reorder a linked list by value frequency."""

    def reorder_by_frequency(self, head: Optional[ListNode]) -> Optional[ListNode]:
        """
        Reorder the linked list so that nodes are grouped by ascending frequency
        of their values. Ties in frequency are broken by the order in which each
        value was first encountered in the original list.

        The reordering is done in-place by relinking existing nodes — no new
        nodes are created.

        Args:
            head: The head node of the original singly linked list.

        Returns:
            The head node of the reordered linked list.

        Time Complexity:  O(N log N) — dominated by sorting the unique values.
                          N = number of nodes in the list.
        Space Complexity: O(N) — we store references to all nodes grouped by value,
                          plus auxiliary dictionaries of size O(U) where U ≤ N is
                          the number of unique values.
        """

        # ------------------------------------------------------------------ #
        # EDGE CASE: empty list or single node — nothing to reorder.
        # ------------------------------------------------------------------ #
        if head is None or head.next is None:
            return head

        # ------------------------------------------------------------------ #
        # STEP 1 — Single pass: collect information about each unique value.
        #
        # We need three things per unique value:
        #   a) frequency  : how many times it appears (for sorting key #1)
        #   b) first_seen : the index at which it first appeared (sort key #2)
        #   c) node_chain : a list of the actual ListNode objects with that value,
        #                   in the order they appear in the original list.
        #                   We will relink these nodes later.
        #
        # Why store nodes instead of just counts?
        # The problem says "reorder in-place by relinking nodes", so we must
        # reuse the original node objects.
        # ------------------------------------------------------------------ #

        # Maps value → list of ListNode objects (in original order)
        nodes_by_value: Dict[int, List[ListNode]] = defaultdict(list)

        # Maps value → index at which it was first seen (0-based)
        first_seen: Dict[int, int] = {}

        index = 0          # position counter as we traverse the list
        current = head
        while current is not None:
            val = current.val

            # Record first-seen index only on the first encounter
            if val not in first_seen:
                first_seen[val] = index

            # Append this node to the chain for its value
            nodes_by_value[val].append(current)

            current = current.next
            index += 1

        # ------------------------------------------------------------------ #
        # STEP 2 — Determine the sorted order of unique values.
        #
        # Primary sort key   : frequency (ascending) — lower freq comes first.
        # Secondary sort key : first_seen index (ascending) — if two values
        #                      share the same frequency, the one encountered
        #                      earlier in the original list comes first.
        #
        # Python's sort is stable and accepts a tuple key, so this is clean.
        # ------------------------------------------------------------------ #

        # Get all unique values present in the list
        unique_values: List[int] = list(nodes_by_value.keys())

        # Sort by (frequency, first_seen_index)
        unique_values.sort(
            key=lambda v: (len(nodes_by_value[v]), first_seen[v])
        )

        # ------------------------------------------------------------------ #
        # STEP 3 — Relink nodes in the new order.
        #
        # We iterate over unique_values in sorted order.
        # For each value, we iterate over its node chain (already in original
        # relative order) and link them one after another.
        #
        # We use a "dummy" sentinel node so we never have to special-case the
        # very first node — the final head will be dummy.next.
        # ------------------------------------------------------------------ #

        dummy = ListNode(0)   # sentinel; its .next will be the new head
        tail = dummy          # 'tail' always points to the last linked node

        for val in unique_values:
            # Retrieve all nodes that carry this value (in original order)
            chain: List[ListNode] = nodes_by_value[val]

            for node in chain:
                # Attach this node right after the current tail
                tail.next = node
                tail = node   # advance tail to the newly attached node

        # ------------------------------------------------------------------ #
        # STEP 4 — Terminate the list.
        #
        # After relinking, 'tail' is the very last node in the new list.
        # Its .next might still point to some old node from the original list,
        # which would create a cycle or a dangling chain.  Set it to None.
        # ------------------------------------------------------------------ #
        tail.next = None

        # The new head is whatever comes after our dummy sentinel
        return dummy.next


# ---------------------------------------------------------------------------
# Main — demonstration and verification
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    solver = Solution()

    # ------------------------------------------------------------------ #
    # Example 1
    # Input:    [4, 2, 4, 1, 2, 4]
    # Expected: [1, 2, 2, 4, 4, 4]
    #
    # Trace:
    #   freq(4) = 3, first_seen(4) = 0
    #   freq(2) = 2, first_seen(2) = 1
    #   freq(1) = 1, first_seen(1) = 3
    #
    #   Sorted by (freq, first_seen):
    #     1 → (1, 3)
    #     2 → (2, 1)
    #     4 → (3, 0)
    #
    #   Relinked: 1 → 2 → 2 → 4 → 4 → 4  ✓
    # ------------------------------------------------------------------ #
    values1 = [4, 2, 4, 1, 2, 4]
    head1 = build_linked_list(values1)
    result1 = solver.reorder_by_frequency(head1)
    output1 = linked_list_to_list(result1)
    print("Example 1:")
    print(f"  Input:    {values1}")
    print(f"  Output:   {output1}")
    print(f"  Expected: [1, 2, 2, 4, 4, 4]")
    print(f"  PASS: {output1 == [1, 2, 2, 4, 4, 4]}")
    print()

    # ------------------------------------------------------------------ #
    # Example 2
    # Input:    [3, 1, 3, 2, 1, 3, 2]
    # Expected: [1, 1, 2, 2, 3, 3, 3]
    #
    # Trace:
    #   freq(3) = 3, first_seen(3) = 0
    #   freq(1) = 2, first_seen(1) = 1
    #   freq(2) = 2, first_seen(2) = 3
    #
    #   Sorted by (freq, first_seen):
    #     1 → (2, 1)   ← same freq as 2 but seen earlier
    #     2 → (2, 3)
    #     3 → (3, 0)
    #
    #   Relinked: 1 → 1 → 2 → 2 → 3 → 3 → 3  ✓
    # ------------------------------------------------------------------ #
    values2 = [3, 1, 3, 2, 1, 3, 2]
    head2 = build_linked_list(values2)
    result2 = solver.reorder_by_frequency(head2)
    output2 = linked_list_to_list(result2)
    print("Example 2:")
    print(f"  Input:    {values2}")
    print(f"  Output:   {output2}")
    print(f"  Expected: [1, 1, 2, 2, 3, 3, 3]")
    print(f"  PASS: {output2 == [1, 1, 2, 2, 3, 3, 3]}")
    print()

    # ------------------------------------------------------------------ #
    # Edge case: single node
    # ------------------------------------------------------------------ #
    values3 = [7]
    head3 = build_linked_list(values3)
    result3 = solver.reorder_by_frequency(head3)
    output3 = linked_list_to_list(result3)
    print("Edge Case — Single Node:")
    print(f"  Input:    {values3}")
    print(f"  Output:   {output3}")
    print(f"  Expected: [7]")
    print(f"  PASS: {output3 == [7]}")
    print()

    # ------------------------------------------------------------------ #
    # Edge case: all same value
    # ------------------------------------------------------------------ #
    values4 = [5, 5, 5, 5]
    head4 = build_linked_list(values4)
    result4 = solver.reorder_by_frequency(head4)
    output4 = linked_list_to_list(result4)
    print("Edge Case — All Same Value:")
    print(f"  Input:    {values4}")
    print(f"  Output:   {output4}")
    print(f"  Expected: [5, 5, 5, 5]")
    print(f"  PASS: {output4 == [5, 5, 5, 5]}")
    print()

    # ------------------------------------------------------------------ #
    # Edge case: all distinct values (each freq = 1, order by first_seen)
    # ------------------------------------------------------------------ #
    values5 = [9, 3, 7, 1]
    head5 = build_linked_list(values5)
    result5 = solver.reorder_by_frequency(head5)
    output5 = linked_list_to_list(result5)
    print("Edge Case — All Distinct Values:")
    print(f"  Input:    {values5}")
    print(f"  Output:   {output5}")
    print(f"  Expected: [9, 3, 7, 1]  (all freq=1, preserve first-seen order)")
    print(f"  PASS: {output5 == [9, 3, 7, 1]}")
```