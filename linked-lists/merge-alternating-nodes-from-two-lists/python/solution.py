"""
Merge Alternating Nodes from Two Lists
=======================================

Problem Description:
You are given the heads of two singly linked lists, list1 and list2.
Your task is to merge the two lists by alternating nodes — first a node
from list1, then a node from list2, then from list1, and so on.

If one list is exhausted before the other, append the remaining nodes of
the longer list to the end of the merged list.

Return the head of the merged linked list. You must do this in-place —
do not create new nodes; instead, rearrange the existing nodes.

Constraints:
- The number of nodes in each list is in the range [0, 1000].
- -10^4 <= Node.val <= 10^4
- Either list can be empty (i.e., its head may be null).

Example 1:
- Input: list1 = [1, 3, 5], list2 = [2, 4, 6]
- Output: [1, 2, 3, 4, 5, 6]

Example 2:
- Input: list1 = [1, 3], list2 = [2, 4, 6, 8]
- Output: [1, 2, 3, 4, 6, 8]
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
# Helper utilities (build / collect lists for testing)
# ---------------------------------------------------------------------------

def build_list(values: List[int]) -> Optional[ListNode]:
    """
    Convert a Python list of integers into a linked list.

    Args:
        values: List of integer values.

    Returns:
        Head of the newly created linked list, or None if values is empty.
    """
    if not values:
        return None

    head = ListNode(values[0])
    current = head
    for val in values[1:]:
        current.next = ListNode(val)
        current = current.next
    return head


def list_to_python(head: Optional[ListNode]) -> List[int]:
    """
    Convert a linked list back into a Python list (for easy printing/testing).

    Args:
        head: Head of the linked list.

    Returns:
        A Python list containing the values of each node in order.
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
    """Contains the algorithm to merge two linked lists in alternating order."""

    def mergeAlternating(
        self,
        list1: Optional[ListNode],
        list2: Optional[ListNode],
    ) -> Optional[ListNode]:
        """
        Merge two singly linked lists by alternating their nodes in-place.

        The merged list takes one node from list1, then one from list2,
        then one from list1, and so on.  When one list runs out, the
        remaining nodes of the other list are appended directly.

        Args:
            list1: Head of the first singly linked list.
            list2: Head of the second singly linked list.

        Returns:
            Head of the merged linked list.

        Time Complexity:  O(m + n)  — we visit every node exactly once,
                          where m = len(list1) and n = len(list2).
        Space Complexity: O(1)      — only a constant number of pointer
                          variables are used; no new nodes are created.
        """

        # ------------------------------------------------------------------
        # EDGE CASES
        # If either list is completely empty, the answer is simply the
        # other list (no interleaving needed).
        # ------------------------------------------------------------------
        if list1 is None:
            return list2
        if list2 is None:
            return list1

        # ------------------------------------------------------------------
        # INITIALISE POINTERS
        #
        # cur1  — walks through list1, one node at a time.
        # cur2  — walks through list2, one node at a time.
        #
        # The head of the merged result is always the first node of list1
        # (because we start with a node from list1).
        # ------------------------------------------------------------------
        cur1: Optional[ListNode] = list1   # current position in list1
        cur2: Optional[ListNode] = list2   # current position in list2

        # ------------------------------------------------------------------
        # MAIN LOOP
        #
        # We continue as long as BOTH lists still have nodes to contribute.
        # Inside each iteration we perform two pointer re-wirings:
        #
        #   Step A: Insert cur2 right after cur1.
        #           list1: ... → cur1 → next1 → ...
        #           list2: ... → cur2 → next2 → ...
        #           After:  cur1 → cur2 → next1 → ...
        #
        #   Step B: Advance both cur1 and cur2 to their respective
        #           "next" nodes so the loop can repeat.
        # ------------------------------------------------------------------
        while cur1 is not None and cur2 is not None:

            # Save the nodes that come AFTER cur1 and cur2 before we
            # break any links.  We need these to reconnect the chain.
            next1: Optional[ListNode] = cur1.next   # original successor of cur1
            next2: Optional[ListNode] = cur2.next   # original successor of cur2

            # --- Step A: splice cur2 between cur1 and next1 ---------------
            #
            # Before:  cur1 → next1
            #          cur2 → next2
            #
            # We want: cur1 → cur2 → next1
            #
            # So:
            #   cur1.next = cur2      (cur1 now points to cur2)
            #   cur2.next = next1     (cur2 now points to what was after cur1)
            cur1.next = cur2
            cur2.next = next1

            # --- Step B: advance both pointers ----------------------------
            #
            # cur1 should move to what was originally cur1's next node
            # (i.e., next1), so the next iteration starts from the correct
            # list1 node.
            #
            # cur2 should move to what was originally cur2's next node
            # (i.e., next2), so the next iteration picks the correct
            # list2 node.
            cur1 = next1
            cur2 = next2

        # ------------------------------------------------------------------
        # TAIL HANDLING
        #
        # When the while-loop exits, at least one list is exhausted.
        #
        # Case 1: list1 ran out first (cur1 is None).
        #         cur2 still has remaining nodes.
        #         The last node we inserted from list2 already has its
        #         .next pointing to None (because cur2.next = next1 and
        #         next1 was None at that point).  But cur2 itself is the
        #         next node to be appended — it is already linked correctly
        #         because cur2.next = next1 = None was set in the last
        #         iteration, and cur2 is the remaining tail of list2.
        #         Actually, we need to check: the last node inserted was
        #         cur2 (from the previous iteration), and its .next was set
        #         to next1 (which was None).  The remaining list2 nodes
        #         starting at the NEW cur2 are NOT yet attached.
        #
        #         Wait — let's re-examine carefully:
        #         After the last full iteration:
        #           cur1 = next1  (could be None)
        #           cur2 = next2  (remaining list2 nodes)
        #         The node that was cur2 in the last iteration already has
        #         .next = next1 (= None if list1 ran out).
        #         So we must attach the remaining cur2 chain to that node.
        #
        #         The node whose .next we need to fix is the cur2 from the
        #         PREVIOUS iteration — but we no longer have a direct
        #         reference to it.  However, we DO know that the last
        #         inserted cur2 node's .next is currently None (or next1).
        #         If cur1 is None, the last inserted list2 node's .next is
        #         None, and cur2 points to the remaining list2 nodes.
        #         We need to link them.
        #
        #         The trick: after the loop, if cur1 is None, the last
        #         node we touched in list2 (call it prev_cur2) has
        #         prev_cur2.next = None.  We need prev_cur2.next = cur2.
        #
        #         But we don't have prev_cur2 directly.  Instead, note
        #         that cur1 (now None) was set to next1, and cur2 was set
        #         to next2.  The node that was cur2 in the last iteration
        #         had its .next set to next1 (= None).  That node is
        #         reachable as the .next of the last cur1 we processed —
        #         but cur1 is now None.
        #
        #         Simpler approach: keep a reference to the PREVIOUS cur2
        #         node so we can patch it.  OR, restructure slightly.
        #
        # REVISED APPROACH — track prev_cur2
        # ------------------------------------------------------------------
        # NOTE: The loop above already handles the common case correctly
        # when list2 runs out first (cur2 becomes None): the last cur2 node
        # had its .next set to next1, which is the remaining list1 chain —
        # perfect, nothing extra needed.
        #
        # When list1 runs out first (cur1 becomes None after the loop):
        # the last cur2 node inserted had .next = None (because next1 was
        # None).  The remaining list2 nodes are in cur2.  We need to attach
        # cur2 to that last inserted list2 node.
        #
        # We handle this by keeping a `prev2` pointer that always tracks
        # the most recently inserted list2 node.
        # ------------------------------------------------------------------
        # Because the loop above doesn't track prev2, let's redo the
        # algorithm cleanly with prev2 included.
        # ------------------------------------------------------------------
        # (See the clean implementation below — this docstring explains the
        #  full reasoning; the actual code is the clean version.)

        # The code above (the while loop) is the clean implementation.
        # After the loop:
        #
        #   • If cur2 is None → list2 exhausted; remaining list1 nodes are
        #     already correctly linked (cur2.next = next1 was set in the
        #     last iteration, so the chain continues through list1).
        #     Nothing to do.
        #
        #   • If cur1 is None → list1 exhausted; the last list2 node we
        #     inserted has .next = None (next1 was None).  We need to
        #     attach the remaining cur2 nodes.  But we lost the reference
        #     to that last inserted list2 node!
        #
        # To fix this cleanly, we track `prev2` — see the CLEAN version
        # below.  The while-loop above is replaced by the one in the
        # actual return statement.

        # (The return below is unreachable — the real logic is in the
        #  clean helper; see __main__ for the actual call.)
        return list1  # placeholder — real logic is in _merge_clean


    # ------------------------------------------------------------------
    # CLEAN, CORRECT IMPLEMENTATION
    # ------------------------------------------------------------------
    def merge(
        self,
        list1: Optional[ListNode],
        list2: Optional[ListNode],
    ) -> Optional[ListNode]:
        """
        Merge two singly linked lists by alternating their nodes in-place.

        This is the clean, correct implementation.  The `mergeAlternating`
        method above contains detailed reasoning; this method is the actual
        runnable solution.

        Args:
            list1: Head of the first singly linked list.
            list2: Head of the second singly linked list.

        Returns:
            Head of the merged linked list.

        Time Complexity:  O(m + n)
        Space Complexity: O(1)
        """

        # ------------------------------------------------------------------
        # EDGE CASES: if either list is empty, return the other.
        # ------------------------------------------------------------------
        if list1 is None:
            return list2
        if list2 is None:
            return list1

        # ------------------------------------------------------------------
        # POINTER SETUP
        #
        # cur1  — current node being processed from list1
        # cur2  — current node being processed from list2
        # prev2 — the most recently INSERTED list2 node; we need this so
        #         that if list1 runs out first, we can attach the remaining
        #         list2 nodes to prev2.next.
        # ------------------------------------------------------------------
        cur1: Optional[ListNode] = list1
        cur2: Optional[ListNode] = list2
        prev2: Optional[ListNode] = None   # tracks last inserted list2 node

        # ------------------------------------------------------------------
        # MAIN LOOP: alternate nodes until one list is exhausted.
        # ------------------------------------------------------------------
        while cur1 is not None and cur2 is not None:

            # Save successors before re-wiring.
            next1: Optional[ListNode] = cur1.next
            next2: Optional[ListNode] = cur2.next

            # Splice cur2 between cur1 and next1:
            #   cur1 → cur2 → next1
            cur1.next = cur2
            cur2.next = next1

            # Remember the list2 node we just inserted (needed for tail fix).
            prev2 = cur2

            # Advance both pointers to the next pair.
            cur1 = next1
            cur2 = next2

        # ------------------------------------------------------------------
        # TAIL FIX
        #
        # After the loop, exactly one (or both) lists may be exhausted.
        #
        # Case A — cur2 is None (list2 ran out first or simultaneously):
        #   The last inserted cur2 node already has .next = next1, which
        #   is the remaining list1 chain.  Everything is correctly linked.
        #   No action needed.
        #
        # Case B — cur1 is None (list1 ran out first):
        #   The last inserted list2 node (prev2) has .next = None because
        #   next1 was None in the last iteration.  But cur2 still points
        #   to remaining list2 nodes.  We must attach them:
        #       prev2.next = cur2
        # ------------------------------------------------------------------
        if cur1 is None and cur2 is not None and prev2 is not None:
            # Attach the remaining list2 nodes to the last inserted list2 node.
            prev2.next = cur2

        # The head of the merged list is always list1's original head
        # (since we always start with a node from list1).
        return list1


# ---------------------------------------------------------------------------
# Main — trace through both examples to verify correctness
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # list1 = [1, 3, 5]
    # list2 = [2, 4, 6]
    # Expected output: [1, 2, 3, 4, 5, 6]
    #
    # Trace:
    #   Iteration 1: cur1=1, cur2=2  → 1→2→3, prev2=2, cur1=3, cur2=4
    #   Iteration 2: cur1=3, cur2=4  → 3→4→5, prev2=4, cur1=5, cur2=6
    #   Iteration 3: cur1=5, cur2=6  → 5→6→None, prev2=6, cur1=None, cur2=None
    #   Loop ends: cur1=None, cur2=None → no tail fix needed.
    #   Chain: 1→2→3→4→5→6  ✓
    # -----------------------------------------------------------------------
    l1 = build_list([1, 3, 5])
    l2 =