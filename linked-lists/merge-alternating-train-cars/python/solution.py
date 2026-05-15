"""
Title: Merge Alternating Train Cars
Difficulty: Easy
Topic: Linked Lists

Problem Description:
You are given the heads of two singly linked lists, `trainA` and `trainB`, each
representing a sequence of train cars identified by their car numbers. Your task
is to merge the two trains into one by alternating cars from each train, starting
with a car from `trainA`. If one train runs out of cars before the other, append
all remaining cars from the longer train to the end of the merged result.

Return the head of the merged linked list. You must do this **in-place** without
allocating extra nodes — simply re-link the existing nodes.

Constraints:
- The number of nodes in each list is in the range [0, 1000].
- 0 <= Node.val <= 10000
- Either list can be empty, in which case return the other list as-is.

Example 1:
- Input: trainA = [1, 3, 5], trainB = [2, 4, 6]
- Output: [1, 2, 3, 4, 5, 6]

Example 2:
- Input: trainA = [10, 20], trainB = [1, 2, 3, 4]
- Output: [10, 1, 20, 2, 3, 4]
"""

from __future__ import annotations
from typing import Optional, List


# ---------------------------------------------------------------------------
# Node definition
# ---------------------------------------------------------------------------

class ListNode:
    """A single node in a singly linked list."""

    def __init__(self, val: int = 0, next: Optional["ListNode"] = None):
        self.val = val
        self.next = next


# ---------------------------------------------------------------------------
# Helper utilities (build / collect) — used only in __main__ for testing
# ---------------------------------------------------------------------------

def build_list(values: List[int]) -> Optional[ListNode]:
    """
    Convert a plain Python list of integers into a linked list.

    Args:
        values: List of integer values.

    Returns:
        Head node of the newly created linked list, or None if values is empty.
    """
    if not values:
        return None
    head = ListNode(values[0])
    current = head
    for v in values[1:]:
        current.next = ListNode(v)
        current = current.next
    return head


def collect_list(head: Optional[ListNode]) -> List[int]:
    """
    Walk a linked list and return its values as a Python list.

    Args:
        head: Head node of the linked list.

    Returns:
        List of integer values in order.
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
    """Contains the merge-alternating algorithm."""

    def mergeAlternating(
        self,
        trainA: Optional[ListNode],
        trainB: Optional[ListNode],
    ) -> Optional[ListNode]:
        """
        Merge two linked lists by alternating nodes, starting with trainA.

        The merge is performed **in-place**: no new nodes are created; only the
        `.next` pointers of existing nodes are re-wired.

        Args:
            trainA: Head of the first linked list (or None if empty).
            trainB: Head of the second linked list (or None if empty).

        Returns:
            Head of the merged linked list.

        Time Complexity:  O(min(m, n)) for the interleaving loop, then O(1) to
                          attach the tail — overall O(m + n) where m = len(trainA)
                          and n = len(trainB).
        Space Complexity: O(1) — only a constant number of pointer variables are
                          used; no auxiliary data structures are allocated.
        """

        # ------------------------------------------------------------------
        # EDGE CASES
        # If either list is completely empty we have nothing to interleave.
        # Simply return whichever list is non-empty (or None if both are empty).
        # This satisfies the constraint: "Either list can be empty, in which
        # case return the other list as-is."
        # ------------------------------------------------------------------
        if trainA is None:
            return trainB          # trainA empty → return trainB unchanged
        if trainB is None:
            return trainA          # trainB empty → return trainA unchanged

        # ------------------------------------------------------------------
        # SAVE THE HEAD OF THE RESULT
        # The merged list always starts with the first node of trainA (per the
        # problem statement: "starting with a car from trainA").
        # We keep `head` pointing here so we can return it at the end.
        # ------------------------------------------------------------------
        head: ListNode = trainA    # the very first node of the merged result

        # ------------------------------------------------------------------
        # WORKING POINTERS
        # `cur_a` walks through trainA one node at a time.
        # `cur_b` walks through trainB one node at a time.
        # At each iteration we will:
        #   1. Save cur_a.next  (the next A node we still need to visit)
        #   2. Save cur_b.next  (the next B node we still need to visit)
        #   3. Wire:  cur_a → cur_b → next_a
        #   4. Advance both pointers to their saved "next" values.
        # ------------------------------------------------------------------
        cur_a: ListNode = trainA
        cur_b: ListNode = trainB

        # ------------------------------------------------------------------
        # MAIN INTERLEAVING LOOP
        # We continue as long as BOTH lists still have nodes to contribute.
        # The moment either pointer becomes None we exit and handle the tail.
        # ------------------------------------------------------------------
        while cur_a is not None and cur_b is not None:

            # Step 1 — Remember where each list continues BEFORE we break links.
            #          If we don't save these now, we'll lose the rest of the list
            #          once we overwrite the .next pointers below.
            next_a: Optional[ListNode] = cur_a.next   # next node in trainA
            next_b: Optional[ListNode] = cur_b.next   # next node in trainB

            # Step 2 — Insert cur_b immediately after cur_a.
            #          Before:  cur_a → next_a  …  cur_b → next_b
            #          After:   cur_a → cur_b → next_a  …  (next_b still intact)
            cur_a.next = cur_b      # A-node now points to the B-node
            cur_b.next = next_a     # B-node now points to the old next A-node

            # Step 3 — Advance both pointers for the next iteration.
            #          `cur_a` moves to what was next_a (the node we just
            #           re-linked cur_b to point at).
            #          `cur_b` moves to what was next_b (the next B-node).
            cur_a = next_a          # advance A pointer
            cur_b = next_b          # advance B pointer

            # At this point the partial merged list looks like:
            #   head → … → (old cur_a) → (old cur_b) → next_a → …
            # and we're ready to process the next pair.

        # ------------------------------------------------------------------
        # TAIL ATTACHMENT
        # When the loop ends, at least one of cur_a or cur_b is None.
        #
        # Case A: cur_a is None but cur_b is not None
        #   trainA was exhausted first.  The last node we processed from
        #   trainA already has its .next pointing to None (set in the loop).
        #   We need to find that last node and attach the remaining trainB.
        #
        # Case B: cur_b is None (trainB exhausted first or simultaneously)
        #   trainA's remaining nodes are already correctly linked (they were
        #   never touched), so nothing extra needs to be done.
        #
        # To handle Case A efficiently we walk from `head` to the last node
        # of the current merged list and attach cur_b there.
        # ------------------------------------------------------------------
        if cur_b is not None:
            # trainA ran out before trainB.
            # Find the current tail of the merged list so we can append cur_b.
            tail: ListNode = head
            while tail.next is not None:
                tail = tail.next
            # `tail` is now the last node in the merged list so far.
            # Attach the remaining trainB nodes.
            tail.next = cur_b

        # ------------------------------------------------------------------
        # RETURN
        # `head` still points to the very first node (trainA's original head),
        # which is the start of the fully merged list.
        # ------------------------------------------------------------------
        return head


# ---------------------------------------------------------------------------
# Main — demonstration and verification
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    solver = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # trainA = [1, 3, 5]
    # trainB = [2, 4, 6]
    # Expected output: [1, 2, 3, 4, 5, 6]
    # Trace:
    #   Iteration 1: cur_a=1, cur_b=2  → 1→2→3, next_a=3, next_b=4
    #   Iteration 2: cur_a=3, cur_b=4  → 3→4→5, next_a=5, next_b=6
    #   Iteration 3: cur_a=5, cur_b=6  → 5→6→None, next_a=None, next_b=None
    #   Loop ends (both None).  No tail to attach.
    #   Result: 1→2→3→4→5→6  ✓
    # -----------------------------------------------------------------------
    trainA1 = build_list([1, 3, 5])
    trainB1 = build_list([2, 4, 6])
    merged1 = solver.mergeAlternating(trainA1, trainB1)
    print("Example 1:")
    print(f"  Input  trainA : [1, 3, 5]")
    print(f"  Input  trainB : [2, 4, 6]")
    print(f"  Output        : {collect_list(merged1)}")
    print(f"  Expected      : [1, 2, 3, 4, 5, 6]")
    print()

    # -----------------------------------------------------------------------
    # Example 2
    # trainA = [10, 20]
    # trainB = [1, 2, 3, 4]
    # Expected output: [10, 1, 20, 2, 3, 4]
    # Trace:
    #   Iteration 1: cur_a=10, cur_b=1  → 10→1→20, next_a=20, next_b=2
    #   Iteration 2: cur_a=20, cur_b=2  → 20→2→None, next_a=None, next_b=3
    #   Loop ends (cur_a is None).
    #   cur_b=3 is not None → find tail of merged list:
    #     head=10 → 1 → 20 → 2 → None  (tail = node 2)
    #   Attach: 2→3→4
    #   Result: 10→1→20→2→3→4  ✓
    # -----------------------------------------------------------------------
    trainA2 = build_list([10, 20])
    trainB2 = build_list([1, 2, 3, 4])
    merged2 = solver.mergeAlternating(trainA2, trainB2)
    print("Example 2:")
    print(f"  Input  trainA : [10, 20]")
    print(f"  Input  trainB : [1, 2, 3, 4]")
    print(f"  Output        : {collect_list(merged2)}")
    print(f"  Expected      : [10, 1, 20, 2, 3, 4]")
    print()

    # -----------------------------------------------------------------------
    # Edge case: trainA is empty
    # Expected output: [7, 8, 9]
    # -----------------------------------------------------------------------
    trainA3 = build_list([])
    trainB3 = build_list([7, 8, 9])
    merged3 = solver.mergeAlternating(trainA3, trainB3)
    print("Edge case — trainA empty:")
    print(f"  Input  trainA : []")
    print(f"  Input  trainB : [7, 8, 9]")
    print(f"  Output        : {collect_list(merged3)}")
    print(f"  Expected      : [7, 8, 9]")
    print()

    # -----------------------------------------------------------------------
    # Edge case: trainB is empty
    # Expected output: [1, 2, 3]
    # -----------------------------------------------------------------------
    trainA4 = build_list([1, 2, 3])
    trainB4 = build_list([])
    merged4 = solver.mergeAlternating(trainA4, trainB4)
    print("Edge case — trainB empty:")
    print(f"  Input  trainA : [1, 2, 3]")
    print(f"  Input  trainB : []")
    print(f"  Output        : {collect_list(merged4)}")
    print(f"  Expected      : [1, 2, 3]")
    print()

    # -----------------------------------------------------------------------
    # Edge case: both lists empty
    # Expected output: []
    # -----------------------------------------------------------------------
    trainA5 = build_list([])
    trainB5 = build_list([])
    merged5 = solver.mergeAlternating(trainA5, trainB5)
    print("Edge case — both empty:")
    print(f"  Input  trainA : []")
    print(f"  Input  trainB : []")
    print(f"  Output        : {collect_list(merged5)}")
    print(f"  Expected      : []")