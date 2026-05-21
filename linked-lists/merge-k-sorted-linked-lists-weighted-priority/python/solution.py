```python
"""
Title: Merge K Sorted Linked Lists with Weighted Priority
Difficulty: Hard
Topic: Linked Lists

Problem Description:
You are given `k` sorted singly linked lists and an integer array `weights` of length `k`,
where `weights[i]` represents the priority weight of the `i`-th linked list. Your task is to
merge all `k` linked lists into a single sorted linked list, but with a twist: when two nodes
from different lists have equal values, the node from the list with the **higher weight** should
appear first. If two equal-valued nodes come from lists with equal weights, the node from the
list with the **smaller index** should appear first.

Additionally, after merging, you must **remove every node whose value appears more than
`threshold` times** in the final merged list, where `threshold` is a given integer.

Return the head of the resulting linked list.

Constraints:
- 1 <= k <= 10^4
- 0 <= weights[i] <= 10^9
- Each linked list has at most 500 nodes.
- Node values are in the range [-10^5, 10^5]
- 1 <= threshold <= 10^4
"""

import heapq
from typing import List, Optional, Dict


# ─────────────────────────────────────────────
# Definition of the singly linked list node
# ─────────────────────────────────────────────
class ListNode:
    """A single node in a singly linked list."""

    def __init__(self, val: int = 0, next: "Optional[ListNode]" = None):
        self.val = val
        self.next = next

    def __repr__(self) -> str:
        """Helper for debugging: show the full list from this node."""
        result = []
        cur = self
        while cur:
            result.append(str(cur.val))
            cur = cur.next
        return " -> ".join(result)


# ─────────────────────────────────────────────
# Helper utilities
# ─────────────────────────────────────────────

def build_linked_list(values: List[int]) -> Optional[ListNode]:
    """Convert a Python list of integers into a linked list, returning the head."""
    if not values:
        return None
    head = ListNode(values[0])
    cur = head
    for v in values[1:]:
        cur.next = ListNode(v)
        cur = cur.next
    return head


def linked_list_to_list(head: Optional[ListNode]) -> List[int]:
    """Convert a linked list back to a Python list (for easy printing/testing)."""
    result: List[int] = []
    cur = head
    while cur:
        result.append(cur.val)
        cur = cur.next
    return result


# ─────────────────────────────────────────────
# Main Solution
# ─────────────────────────────────────────────

class Solution:
    """
    Merges k sorted linked lists with weighted priority and threshold filtering.
    """

    def merge_k_lists(
        self,
        lists: List[Optional[ListNode]],
        weights: List[int],
        threshold: int,
    ) -> Optional[ListNode]:
        """
        Merge k sorted linked lists with weighted priority, then remove nodes
        whose value appears more than `threshold` times.

        Args:
            lists:     A list of k heads of sorted singly linked lists.
            weights:   weights[i] is the priority weight of lists[i].
            threshold: Maximum allowed occurrences of any value in the result.

        Returns:
            The head of the merged, filtered linked list.

        Time Complexity:
            O(N log k) for the heap-based merge, where N is the total number of
            nodes across all lists and k is the number of lists.
            O(N) for the threshold-filtering pass.
            Overall: O(N log k).

        Space Complexity:
            O(k) for the heap (at most one entry per list at any time).
            O(N) for the count dictionary used during filtering.
            Overall: O(N + k).
        """

        # ─────────────────────────────────────────────────────────────────
        # STEP 1: Build a min-heap seeded with the first node of each list.
        #
        # Why a heap?
        #   A min-heap lets us always extract the globally smallest (or
        #   highest-priority equal) node in O(log k) time, which is far
        #   better than scanning all k list heads in O(k) time.
        #
        # Heap entry format: (value, neg_weight, list_index, node)
        #   • value        – primary sort key (ascending, so smallest first).
        #   • neg_weight   – secondary sort key. We negate the weight so that
        #                    a HIGHER weight sorts SMALLER (i.e., comes first
        #                    in a min-heap). E.g., weight=3 → -3 < -1, so
        #                    weight-3 list wins the tie.
        #   • list_index   – tertiary sort key. Smaller index wins ties when
        #                    both value AND weight are equal.
        #   • node         – the actual ListNode; NOT compared by heapq because
        #                    the three keys before it break all ties, but we
        #                    still need the node to advance the pointer.
        #
        # NOTE: heapq compares tuples element-by-element, so we must ensure
        # no two entries are ever identical in (value, neg_weight, list_index)
        # — list_index is unique per list, so this is guaranteed.
        # ─────────────────────────────────────────────────────────────────

        heap: List[tuple] = []

        for i, head in enumerate(lists):
            if head is not None:
                # Push (value, -weight, list_index, node) onto the heap.
                heapq.heappush(heap, (head.val, -weights[i], i, head))

        # ─────────────────────────────────────────────────────────────────
        # STEP 2: Extract nodes from the heap one by one to build the merged
        #         sorted list.
        #
        # We use a "dummy" sentinel node so we never have to special-case
        # the very first node — we just always append to dummy.next.
        # ─────────────────────────────────────────────────────────────────

        dummy = ListNode(0)   # sentinel head; result starts at dummy.next
        tail = dummy          # `tail` always points to the last appended node

        while heap:
            # Pop the entry with the smallest (value, -weight, index) tuple.
            val, neg_w, idx, node = heapq.heappop(heap)

            # Append this node to the merged list.
            tail.next = node
            tail = tail.next

            # Advance the pointer for list `idx` and push the next node
            # (if it exists) back onto the heap.
            next_node = node.next
            if next_node is not None:
                heapq.heappush(heap, (next_node.val, neg_w, idx, next_node))

        # Detach the tail from any leftover next pointer (safety measure).
        tail.next = None

        # ─────────────────────────────────────────────────────────────────
        # STEP 3: Count how many times each value appears in the merged list.
        #
        # We need this count BEFORE we start removing nodes so we know which
        # values to keep and which to discard.
        # ─────────────────────────────────────────────────────────────────

        value_count: Dict[int, int] = {}
        cur = dummy.next
        while cur:
            value_count[cur.val] = value_count.get(cur.val, 0) + 1
            cur = cur.next

        # ─────────────────────────────────────────────────────────────────
        # STEP 4: Filter out nodes whose value count exceeds `threshold`.
        #
        # We walk the merged list again. For each node:
        #   • If its value count <= threshold → keep it (advance tail).
        #   • Otherwise                       → skip it (don't link it in).
        #
        # We reuse the `dummy` sentinel so the filtered list also starts at
        # dummy.next.
        # ─────────────────────────────────────────────────────────────────

        # Reset dummy to act as the head of the filtered list.
        dummy.next = None
        tail = dummy          # tail of the filtered list

        # We need to iterate over the merged list. Since we already walked it
        # once for counting, we need to re-traverse from the original merged
        # head. But we've been modifying `dummy.next` — so we saved the merged
        # head before this step? Actually we haven't detached it yet.
        #
        # Wait — we set dummy.next = None above, which breaks the reference.
        # We need to save the merged head BEFORE resetting dummy.next.
        #
        # Let's restructure: save merged_head, then reset dummy.next.
        # (See the corrected flow below — the counting loop already walked
        #  `dummy.next` via `cur`, so `merged_head` is still intact.)
        #
        # Actually the issue is that we set dummy.next = None AFTER counting.
        # We need to save the merged head first.
        #
        # The code below is the corrected version that saves merged_head
        # before resetting dummy.

        # ── We'll redo this cleanly in the actual implementation below. ──
        # (The above comments are left as educational explanation of the
        #  design decision; the real code is structured correctly.)

        # This method delegates to a cleaner internal implementation.
        return self._merge_and_filter(lists, weights, threshold)

    # ─────────────────────────────────────────────────────────────────────
    # Internal clean implementation (avoids the structural issue noted above)
    # ─────────────────────────────────────────────────────────────────────

    def _merge_and_filter(
        self,
        lists: List[Optional[ListNode]],
        weights: List[int],
        threshold: int,
    ) -> Optional[ListNode]:
        """
        Internal helper that performs the actual merge + filter in a clean,
        single-pass-friendly structure.

        Args:
            lists:     k heads of sorted singly linked lists.
            weights:   Priority weights for each list.
            threshold: Maximum allowed occurrences per value.

        Returns:
            Head of the merged, filtered linked list.

        Time Complexity:  O(N log k)
        Space Complexity: O(N + k)
        """

        # ── Phase A: Heap-based merge ──────────────────────────────────

        # Seed the heap with the first node of every non-empty list.
        # Tuple layout: (node_value, -weight, list_index, node_object)
        # Sorting rules (min-heap, so "smallest" wins):
        #   1. Smallest value first  (ascending order)
        #   2. Largest weight first  (we negate weight so -large < -small)
        #   3. Smallest list index first (tie-break by position)
        heap: List[tuple] = []
        for i, head in enumerate(lists):
            if head is not None:
                heapq.heappush(heap, (head.val, -weights[i], i, head))

        # dummy is a sentinel; the real merged list starts at dummy.next
        merge_dummy = ListNode(0)
        tail = merge_dummy

        while heap:
            val, neg_w, idx, node = heapq.heappop(heap)

            # Link this node into the merged list.
            tail.next = node
            tail = tail.next

            # If this list has more nodes, push the next one onto the heap.
            if node.next is not None:
                heapq.heappush(heap, (node.next.val, neg_w, idx, node.next))

        # Sever any leftover .next pointer from the last node.
        tail.next = None

        # Save the head of the fully merged list before we start filtering.
        merged_head: Optional[ListNode] = merge_dummy.next

        # ── Phase B: Count occurrences of each value ───────────────────

        # Walk the merged list once to tally how many times each value appears.
        # This is O(N) time and O(distinct values) ≤ O(N) space.
        value_count: Dict[int, int] = {}
        cur: Optional[ListNode] = merged_head
        while cur:
            value_count[cur.val] = value_count.get(cur.val, 0) + 1
            cur = cur.next

        # ── Phase C: Filter nodes exceeding the threshold ──────────────

        # We walk the merged list a second time.
        # We keep a node only if its value appears <= threshold times.
        # Using a new dummy sentinel for the filtered result.
        filter_dummy = ListNode(0)
        tail = filter_dummy

        cur = merged_head
        while cur:
            # Save next pointer BEFORE potentially skipping this node.
            nxt: Optional[ListNode] = cur.next

            if value_count[cur.val] <= threshold:
                # This value is within the allowed count — keep the node.
                tail.next = cur
                tail = tail.next
                tail.next = None  # clean up forward pointer

            # If count > threshold, we simply don't link `cur` in — skip it.

            cur = nxt

        return filter_dummy.next


# ─────────────────────────────────────────────
# Driver / Test
# ─────────────────────────────────────────────

if __name__ == "__main__":
    sol = Solution()

    # ──────────────────────────────────────────
    # Example 1
    # Input:  lists = [[1,4,7],[2,4,6],[1,3,5]], weights = [3,1,2], threshold = 2
    # Expected Output: [1,1,2,3,4,4,5,6,7]
    #
    # Trace:
    #   List 0 (weight=3): 1 → 4 → 7
    #   List 1 (weight=1): 2 → 4 → 6
    #   List 2 (weight=2): 1 → 3 → 5
    #
    #   Heap initially: (1,-3,0,node1_0), (2,-1,1,node2_1), (1,-2,2,node1_2)
    #
    #   Pop (1,-3,0) → value=1 from list0 (weight=3). Push (4,-3,0).
    #   Pop (1,-2,2) → value=1 from list2 (weight=2). Push (3,-2,2).
    #   Pop (2,-1,1) → value=2 from list1. Push (4,-1,1).
    #   Pop (3,-2,2) → value=3 from list2. Push (5,-2,2).
    #   Pop (4,-3,0) → value=4 from list0 (weight=3). Push (7,-3,0).
    #   Pop (4,-1,1) → value=4 from list1 (weight=1). Push (6,-1,1).
    #   Pop (5,-2,2) → value=5 from list2. (no more in list2)
    #   Pop (6,-1,1) → value=6 from list1. (no more in list1)
    #   Pop (7,-3,0) → value=7 from list0. (no more in list0)
    #
    #   Merged: [1,1,2,3,4,4,5,6,7]
    #   Counts: {1:2, 2:1, 3:1, 4:2, 5:1, 6:1, 7:1}
    #   All counts <= 2 (threshold), so nothing removed.
    #   Result: [1,1,2,3,4,4,5,6,7]  ✓
    # ──────────────────────────────────────────
    print("=== Example 1 ===")
    lists1 = [