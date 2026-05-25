```python
"""
Title: Interleave Linked Lists by Prime and Composite Positions
Difficulty: Hard
Topic: Linked Lists

Problem Description:
You are given a singly linked list of integers. Your task is to restructure the list
in-place such that nodes originally at prime-indexed positions (1-indexed) form the
first half of the new list, and nodes at composite (non-prime, non-one) positions form
the second half — but the two groups must be interleaved by alternating one node from
each group, starting with the prime-indexed group. If one group is exhausted before the
other, append the remaining nodes of the longer group to the end.

Position 1 is considered neither prime nor composite. Nodes at position 1 should be
placed at the very beginning of the resulting list before any interleaving begins.

For example, in a list of 7 nodes: position 1 is neither, positions 2, 3, 5, 7 are
prime, and positions 4, 6 are composite.

The resulting list should be:
[node1] → [node2] → [node4] → [node3] → [node6] → [node5] → [node7]

Constraints:
- The number of nodes in the list is in the range [1, 10^5]
- Node values are in the range [-10^9, 10^9]
- You must perform the restructuring in-place (O(1) extra space, not counting output)
- Time complexity should be O(n)
"""

from typing import Optional, List


# ---------------------------------------------------------------------------
# Node definition for a singly linked list
# ---------------------------------------------------------------------------
class ListNode:
    """A single node in a singly linked list."""

    def __init__(self, val: int = 0, next: "Optional[ListNode]" = None):
        self.val = val
        self.next = next


# ---------------------------------------------------------------------------
# Helper utilities
# ---------------------------------------------------------------------------

def is_prime(n: int) -> bool:
    """
    Return True if n is a prime number, False otherwise.

    We only need this for positions (1-indexed), so n >= 1.
    Position 1 → neither prime nor composite.
    Position 2 → prime.
    etc.

    Args:
        n: A positive integer representing a 1-based position.

    Returns:
        True if n is prime, False otherwise.

    Time complexity:  O(sqrt(n))
    Space complexity: O(1)
    """
    if n < 2:
        return False
    if n == 2:
        return True
    if n % 2 == 0:
        return False
    i = 3
    while i * i <= n:
        if n % i == 0:
            return False
        i += 2
    return True


def build_list(values: List[int]) -> Optional[ListNode]:
    """
    Build a singly linked list from a Python list of integers.

    Args:
        values: List of integer values.

    Returns:
        Head node of the constructed linked list, or None if values is empty.

    Time complexity:  O(n)
    Space complexity: O(n)  — n new nodes are created
    """
    if not values:
        return None
    head = ListNode(values[0])
    current = head
    for v in values[1:]:
        current.next = ListNode(v)
        current = current.next
    return head


def list_to_python(head: Optional[ListNode]) -> List[int]:
    """
    Convert a linked list back to a Python list for easy printing / testing.

    Args:
        head: Head node of the linked list.

    Returns:
        A Python list of integer values in linked-list order.

    Time complexity:  O(n)
    Space complexity: O(n)
    """
    result: List[int] = []
    current = head
    while current:
        result.append(current.val)
        current = current.next
    return result


# ---------------------------------------------------------------------------
# Main Solution
# ---------------------------------------------------------------------------

class Solution:
    """Encapsulates the algorithm for interleaving prime/composite position nodes."""

    def interleave_by_position(self, head: Optional[ListNode]) -> Optional[ListNode]:
        """
        Restructure the linked list so that:
          1. The node at position 1 (neither prime nor composite) comes first.
          2. Then prime-position and composite-position nodes are interleaved
             (one prime, one composite, one prime, one composite, …).
          3. Any leftover nodes from the longer group are appended at the end.

        The restructuring is done in-place by re-wiring the `next` pointers of
        existing nodes — no new nodes are created.

        Args:
            head: Head of the original singly linked list.

        Returns:
            Head of the restructured linked list.

        Time complexity:  O(n)  — two linear passes (one to separate, one to merge)
        Space complexity: O(1)  — only a constant number of pointer variables used
        """

        # ------------------------------------------------------------------ #
        # EDGE CASE: empty list or single node — nothing to restructure.
        # ------------------------------------------------------------------ #
        if head is None or head.next is None:
            return head

        # ------------------------------------------------------------------ #
        # STEP 1 — Separate nodes into three sub-lists using dummy head nodes.
        #
        # We walk through the original list once, keeping track of the current
        # 1-based position.  For each node we decide which bucket it belongs to:
        #   • position == 1  → "neither" bucket  (at most one node)
        #   • is_prime(pos)  → "prime" bucket
        #   • else           → "composite" bucket  (pos >= 4, not prime)
        #
        # Using dummy (sentinel) nodes avoids special-casing an empty bucket.
        # ------------------------------------------------------------------ #

        # Dummy heads — their `.next` will point to the first real node in each bucket.
        neither_dummy = ListNode(0)   # bucket for position-1 node
        prime_dummy   = ListNode(0)   # bucket for prime-position nodes
        comp_dummy    = ListNode(0)   # bucket for composite-position nodes

        # Tail pointers let us append in O(1) without traversing each bucket.
        neither_tail = neither_dummy
        prime_tail   = prime_dummy
        comp_tail    = comp_dummy

        current = head   # pointer that walks the original list
        pos = 1          # 1-based position counter

        while current is not None:
            # Save the next node before we overwrite current.next
            next_node = current.next
            # Detach current from the original chain (clean separation)
            current.next = None

            if pos == 1:
                # Position 1 is neither prime nor composite.
                neither_tail.next = current
                neither_tail = current
            elif is_prime(pos):
                # Prime position → goes into the prime bucket.
                prime_tail.next = current
                prime_tail = current
            else:
                # Composite position (pos >= 4 and not prime).
                comp_tail.next = current
                comp_tail = current

            current = next_node   # advance to the next original node
            pos += 1              # increment position counter

        # After the loop:
        #   neither_dummy.next → node at position 1 (or None)
        #   prime_dummy.next   → first prime-position node (or None)
        #   comp_dummy.next    → first composite-position node (or None)

        # ------------------------------------------------------------------ #
        # STEP 2 — Interleave the prime and composite sub-lists.
        #
        # We alternate: one node from prime, one from composite, repeat.
        # If one list runs out first, we append the rest of the other list.
        #
        # We build the merged interleaved list using a dummy head + tail pointer.
        # ------------------------------------------------------------------ #

        merge_dummy = ListNode(0)   # dummy head for the interleaved result
        merge_tail  = merge_dummy

        p = prime_dummy.next   # current node in the prime sub-list
        c = comp_dummy.next    # current node in the composite sub-list

        # Alternate: prime → composite → prime → composite → …
        while p is not None and c is not None:
            # Append one prime-position node
            merge_tail.next = p
            merge_tail = p
            p = p.next
            merge_tail.next = None   # detach from old chain

            # Append one composite-position node
            merge_tail.next = c
            merge_tail = c
            c = c.next
            merge_tail.next = None   # detach from old chain

        # One of the lists may still have remaining nodes — append them.
        if p is not None:
            merge_tail.next = p
        elif c is not None:
            merge_tail.next = c

        # merge_dummy.next now points to the fully interleaved prime+composite list.

        # ------------------------------------------------------------------ #
        # STEP 3 — Prepend the "neither" node (position 1) to the front.
        #
        # The problem states: "Nodes at position 1 should be placed at the very
        # beginning of the resulting list before any interleaving begins."
        # ------------------------------------------------------------------ #

        neither_node = neither_dummy.next   # the position-1 node (or None)

        if neither_node is not None:
            # Attach the interleaved list right after the position-1 node.
            neither_node.next = merge_dummy.next
            return neither_node
        else:
            # No position-1 node (shouldn't happen for n >= 1, but be safe).
            return merge_dummy.next


# ---------------------------------------------------------------------------
# Main block — demonstration and verification
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------ #
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
    #
    # prime bucket:     20 → 30 → 50 → 70
    # composite bucket: 40 → 60
    #
    # Interleave prime & composite:
    #   20, 40, 30, 60, 50, 70   (50 and 70 are leftover primes)
    #
    # Prepend position-1 node (10):
    #   10 → 20 → 40 → 30 → 60 → 50 → 70
    #
    # Expected output: [10, 20, 40, 30, 60, 50, 70]
    # ------------------------------------------------------------------ #
    print("=== Example 1 ===")
    head1 = build_list([10, 20, 30, 40, 50, 60, 70])
    result1 = solution.interleave_by_position(head1)
    output1 = list_to_python(result1)
    print(f"Input:    [10, 20, 30, 40, 50, 60, 70]")
    print(f"Output:   {output1}")
    print(f"Expected: [10, 20, 40, 30, 60, 50, 70]")
    assert output1 == [10, 20, 40, 30, 60, 50, 70], f"FAILED: got {output1}"
    print("PASSED\n")

    # ------------------------------------------------------------------ #
    # Example 2
    # Input:  [5, 3, 8, 1, 9]
    # Positions:
    #   1 → 5   (neither)
    #   2 → 3   (prime)
    #   3 → 8   (prime)
    #   4 → 1   (composite)
    #   5 → 9   (prime)
    #
    # prime bucket:     3 → 8 → 9
    # composite bucket: 1
    #
    # Interleave prime & composite:
    #   3, 1, 8, 9   (8 and 9 are leftover primes after composite exhausted)
    #
    # Prepend position-1 node (5):
    #   5 → 3 → 1 → 8 → 9
    #
    # Expected output: [5, 3, 1, 8, 9]
    # ------------------------------------------------------------------ #
    print("=== Example 2 ===")
    head2 = build_list([5, 3, 8, 1, 9])
    result2 = solution.interleave_by_position(head2)
    output2 = list_to_python(result2)
    print(f"Input:    [5, 3, 8, 1, 9]")
    print(f"Output:   {output2}")
    print(f"Expected: [5, 3, 1, 8, 9]")
    assert output2 == [5, 3, 1, 8, 9], f"FAILED: got {output2}"
    print("PASSED\n")

    # ------------------------------------------------------------------ #
    # Edge case: single node
    # ------------------------------------------------------------------ #
    print("=== Edge Case: Single Node ===")
    head3 = build_list([42])
    result3 = solution.interleave_by_position(head3)
    output3 = list_to_python(result3)
    print(f"Input:    [42]")
    print(f"Output:   {output3}")
    print(f"Expected: [42]")
    assert output3 == [42], f"FAILED: got {output3}"
    print("PASSED\n")

    # ------------------------------------------------------------------ #
    # Edge case: two nodes
    # Position 1 → val1 (neither), Position 2 → val2 (prime)
    # No composite nodes.
    # Result: val1 → val2
    # ------------------------------------------------------------------ #
    print("=== Edge Case: Two Nodes ===")
    head4 = build_list([7, 14])
    result4 = solution.interleave_by_position(head4)
    output4 = list_to_python(result4)
    print(f"Input:    [7, 14]")
    print(f"Output:   {output4}")
    print(f"Expected: [7, 14]")
    assert output4 == [7, 14], f"FAILED: got {output4}"
    print("PASSED\n")

    # ------------------------------------------------------------------ #
    # Edge case: four nodes
    # Position 1 → a (neither)
    # Position 2 → b (prime)
    # Position 3 → c (prime)
    # Position 4 → d (composite)
    # prime bucket:     b → c
    # composite bucket: d
    # Interleave: b, d, c
    # Prepend: a → b → d → c
    # ------------------------------------------------------------------ #
    print("=== Edge Case: Four Nodes ===")
    head5 = build_list([1, 2, 3, 4])
    result5 = solution.interleave_by_position(head5)
    output5 = list_to_python(result5)
    print(f"Input:    [1, 2, 3, 4]")
    print(f"Output:   {output5}")
    print(f"Expected: [1, 2, 4, 3]")
    assert output5 == [1, 2, 4, 3], f"FAILED: got {output5}"
    print("PASSED\n")

    print("All test cases passed!")
```