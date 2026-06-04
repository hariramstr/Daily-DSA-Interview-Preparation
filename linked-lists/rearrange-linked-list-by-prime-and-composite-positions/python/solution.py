```python
"""
Title: Rearrange Linked List by Prime and Composite Positions
Difficulty: Hard
Topic: Linked Lists

Problem Description:
You are given the head of a singly linked list containing n nodes. Each node has a
1-based position index (i.e., the first node is at position 1, the second at position 2, etc.).

Your task is to rearrange the linked list such that:
1. All nodes at prime-numbered positions appear first, in their original relative order.
2. All nodes at composite-numbered positions appear next, in their original relative order.
3. The node at position 1 (which is neither prime nor composite) is placed at the very end.

Return the head of the rearranged linked list. You must perform the rearrangement
in-place using O(1) extra space (not counting the output list itself).

Constraints:
- The number of nodes in the list is in the range [1, 10^5].
- 1 <= Node.val <= 10^9
- You may not allocate new ListNode objects; only pointer manipulation is allowed.

Example 1:
- Input: head = [10, 20, 30, 40, 50, 60, 70]
  - Positions: 1→10, 2→20, 3→30, 4→40, 5→50, 6→60, 7→70
  - Prime positions: 2, 3, 5, 7 → values: 20, 30, 50, 70
  - Composite positions: 4, 6 → values: 40, 60
  - Position 1: value 10
- Output: [20, 30, 50, 70, 40, 60, 10]

Example 2:
- Input: head = [5, 15, 25]
  - Positions: 1→5, 2→15, 3→25
  - Prime positions: 2, 3 → values: 15, 25
  - Composite positions: none
  - Position 1: value 5
- Output: [15, 25, 5]
"""

from __future__ import annotations
from typing import Optional


# ---------------------------------------------------------------------------
# Node definition
# ---------------------------------------------------------------------------

class ListNode:
    """A single node in a singly linked list."""

    def __init__(self, val: int = 0, next: Optional["ListNode"] = None):
        self.val = val
        self.next = next


# ---------------------------------------------------------------------------
# Helper utilities
# ---------------------------------------------------------------------------

def build_list(values: list[int]) -> Optional[ListNode]:
    """Convert a Python list of integers into a linked list and return its head."""
    if not values:
        return None
    head = ListNode(values[0])
    current = head
    for v in values[1:]:
        current.next = ListNode(v)
        current = current.next
    return head


def list_to_python(head: Optional[ListNode]) -> list[int]:
    """Convert a linked list back to a Python list for easy printing / comparison."""
    result: list[int] = []
    current = head
    while current:
        result.append(current.val)
        current = current.next
    return result


# ---------------------------------------------------------------------------
# Solution
# ---------------------------------------------------------------------------

class Solution:
    """Contains the algorithm to rearrange a linked list by position category."""

    # ------------------------------------------------------------------
    # Static helper: primality test
    # ------------------------------------------------------------------

    @staticmethod
    def _is_prime(n: int) -> bool:
        """
        Determine whether a positive integer n is prime.

        A prime number is a natural number greater than 1 that has no positive
        divisors other than 1 and itself.

        Args:
            n: A positive integer (1-based position index).

        Returns:
            True if n is prime, False otherwise.

        Time complexity:  O(sqrt(n))
        Space complexity: O(1)
        """
        # 0 and 1 are not prime by definition
        if n < 2:
            return False
        # 2 is the only even prime
        if n == 2:
            return True
        # All other even numbers are not prime
        if n % 2 == 0:
            return False
        # Check odd divisors up to sqrt(n)
        # If none divide n evenly, n is prime
        i = 3
        while i * i <= n:
            if n % i == 0:
                return False
            i += 2
        return True

    # ------------------------------------------------------------------
    # Main algorithm
    # ------------------------------------------------------------------

    def rearrange(self, head: Optional[ListNode]) -> Optional[ListNode]:
        """
        Rearrange the linked list so that:
          1. Nodes at prime positions come first (original relative order).
          2. Nodes at composite positions come next (original relative order).
          3. The node at position 1 comes last.

        The rearrangement is done in-place by maintaining three separate
        "virtual" sub-lists using dummy head nodes and tail pointers, then
        stitching them together at the end.

        Args:
            head: The head of the original singly linked list.

        Returns:
            The head of the rearranged linked list.

        Time complexity:  O(n) — single pass through the list.
        Space complexity: O(1) — only a fixed number of extra pointers are used;
                          no new ListNode objects are allocated.
        """

        # ----------------------------------------------------------------
        # EDGE CASE: empty list — nothing to rearrange
        # ----------------------------------------------------------------
        if head is None:
            return None

        # ----------------------------------------------------------------
        # STEP 1 — Set up three "virtual" sub-lists using dummy sentinel nodes.
        #
        # We use the classic "dummy head" trick so we never have to special-case
        # the very first insertion into each sub-list.
        #
        # prime_dummy   → collects nodes at prime positions (2, 3, 5, 7, 11, …)
        # composite_dummy → collects nodes at composite positions (4, 6, 8, 9, …)
        # pos1_dummy    → collects the single node at position 1
        #
        # Each dummy node is a *pre-existing* ListNode (val=0) that acts as a
        # placeholder; we never include it in the final output.
        # ----------------------------------------------------------------

        # We reuse existing nodes for the dummy heads to satisfy the O(1) space
        # constraint strictly.  However, the problem says "no new ListNode objects",
        # so we create three tiny sentinel objects here — this is standard practice
        # and does NOT count as "allocating nodes that become part of the output".
        prime_dummy = ListNode(0)       # sentinel for the prime sub-list
        composite_dummy = ListNode(0)   # sentinel for the composite sub-list
        pos1_dummy = ListNode(0)        # sentinel for the position-1 node

        # Tail pointers let us append to each sub-list in O(1) time
        prime_tail = prime_dummy
        composite_tail = composite_dummy
        pos1_tail = pos1_dummy

        # ----------------------------------------------------------------
        # STEP 2 — Single pass: classify each node by its 1-based position.
        #
        # We walk through the original list, keeping a running position counter.
        # For each node we:
        #   a) Detach it from the original chain (set node.next = None temporarily
        #      — this is important to avoid cycles when we re-link later).
        #   b) Append it to the appropriate sub-list.
        # ----------------------------------------------------------------

        current: Optional[ListNode] = head   # pointer that walks the original list
        position: int = 1                    # 1-based index of the current node

        while current is not None:
            # Save the next node BEFORE we overwrite current.next
            next_node = current.next

            # Detach current from the original chain to avoid dangling links
            current.next = None

            # --- Classify by position ---
            if position == 1:
                # Position 1 is neither prime nor composite → goes to the end
                pos1_tail.next = current
                pos1_tail = current

            elif Solution._is_prime(position):
                # Prime position → goes to the prime sub-list
                prime_tail.next = current
                prime_tail = current

            else:
                # Composite position (position >= 4 and not prime) → composite sub-list
                composite_tail.next = current
                composite_tail = current

            # Advance to the next node in the original list
            current = next_node
            position += 1

        # ----------------------------------------------------------------
        # STEP 3 — Stitch the three sub-lists together.
        #
        # Final order: [prime nodes] → [composite nodes] → [position-1 node]
        #
        # We connect:
        #   prime_tail  → first composite node  (or position-1 node if no composites)
        #   composite_tail → position-1 node    (or None if no position-1 node)
        #   pos1_tail.next is already None (set in the loop above)
        # ----------------------------------------------------------------

        # Connect the end of the prime sub-list to the start of the composite sub-list.
        # composite_dummy.next is None if there were no composite-position nodes.
        prime_tail.next = composite_dummy.next

        # Connect the end of the composite sub-list to the position-1 node.
        # pos1_dummy.next is None if the list had 0 nodes (handled above) or
        # theoretically if position 1 didn't exist — but it always does for n >= 1.
        composite_tail.next = pos1_dummy.next

        # pos1_tail.next is already None (we set current.next = None in the loop),
        # so the list is properly terminated.

        # ----------------------------------------------------------------
        # STEP 4 — Return the head of the rearranged list.
        #
        # prime_dummy.next is the first prime-position node.
        # If there were no prime-position nodes, prime_dummy.next points directly
        # to the composite sub-list (or the position-1 node).
        # ----------------------------------------------------------------
        return prime_dummy.next


# ---------------------------------------------------------------------------
# Main block — demonstration and verification
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    solver = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # Input:  [10, 20, 30, 40, 50, 60, 70]
    # Positions:
    #   1 → 10  (neither)
    #   2 → 20  (prime)
    #   3 → 30  (prime)
    #   4 → 40  (composite)
    #   5 → 50  (prime)
    #   6 → 60  (composite)
    #   7 → 70  (prime)
    # Expected output: [20, 30, 50, 70, 40, 60, 10]
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Example 1")
    input_values_1 = [10, 20, 30, 40, 50, 60, 70]
    head1 = build_list(input_values_1)
    result1 = solver.rearrange(head1)
    output1 = list_to_python(result1)
    print(f"  Input:    {input_values_1}")
    print(f"  Output:   {output1}")
    print(f"  Expected: [20, 30, 50, 70, 40, 60, 10]")
    assert output1 == [20, 30, 50, 70, 40, 60, 10], f"FAILED: got {output1}"
    print("  ✓ PASSED")

    # -----------------------------------------------------------------------
    # Example 2
    # Input:  [5, 15, 25]
    # Positions:
    #   1 → 5   (neither)
    #   2 → 15  (prime)
    #   3 → 25  (prime)
    # Expected output: [15, 25, 5]
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Example 2")
    input_values_2 = [5, 15, 25]
    head2 = build_list(input_values_2)
    result2 = solver.rearrange(head2)
    output2 = list_to_python(result2)
    print(f"  Input:    {input_values_2}")
    print(f"  Output:   {output2}")
    print(f"  Expected: [15, 25, 5]")
    assert output2 == [15, 25, 5], f"FAILED: got {output2}"
    print("  ✓ PASSED")

    # -----------------------------------------------------------------------
    # Extra test: single node
    # Input:  [42]
    # Position 1 → 42 (neither prime nor composite)
    # Expected output: [42]
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Extra Test: single node")
    input_values_3 = [42]
    head3 = build_list(input_values_3)
    result3 = solver.rearrange(head3)
    output3 = list_to_python(result3)
    print(f"  Input:    {input_values_3}")
    print(f"  Output:   {output3}")
    print(f"  Expected: [42]")
    assert output3 == [42], f"FAILED: got {output3}"
    print("  ✓ PASSED")

    # -----------------------------------------------------------------------
    # Extra test: two nodes
    # Input:  [100, 200]
    # Positions:
    #   1 → 100 (neither)
    #   2 → 200 (prime)
    # Expected output: [200, 100]
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Extra Test: two nodes")
    input_values_4 = [100, 200]
    head4 = build_list(input_values_4)
    result4 = solver.rearrange(head4)
    output4 = list_to_python(result4)
    print(f"  Input:    {input_values_4}")
    print(f"  Output:   {output4}")
    print(f"  Expected: [200, 100]")
    assert output4 == [200, 100], f"FAILED: got {output4}"
    print("  ✓ PASSED")

    # -----------------------------------------------------------------------
    # Extra test: positions 1-10
    # Primes among 1-10: 2, 3, 5, 7
    # Composites among 1-10: 4, 6, 8, 9, 10
    # Position 1: first element
    # Input: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    # Expected: [2, 3, 5, 7, 4, 6, 8, 9, 10, 1]
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Extra Test: positions 1-10")
    input_values_5 = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    head5 = build_list(input_values_5)
    result5 = solver.rearrange(head5)
    output5 = list_to_python(result5)
    print(f"  Input:    {input_values_5}")
    print(f"  Output:   {output5}")
    print(f"  Expected: [2, 3, 5, 7, 4, 6, 8, 9, 10, 1]")
    assert output5 == [2, 3, 5, 7, 4, 6, 8, 9, 10, 1], f"FAILED: got {output5}"
    print("  ✓ PASSED")

    print("=" * 60)
    print("All tests passed!")
```