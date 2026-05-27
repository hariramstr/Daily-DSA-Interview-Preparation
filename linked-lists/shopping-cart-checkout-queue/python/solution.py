```python
"""
Title: Shopping Cart Checkout Queue
Difficulty: Easy
Topic: Linked Lists

Problem Description:
You are building a shopping cart system for an online store. Each customer's cart is
represented as a singly linked list where each node contains the price of one item.
When a customer proceeds to checkout, the system needs to determine if their cart is
"balanced" — meaning the total price of items in the first half of the cart equals
the total price of items in the second half of the cart.

Given the head of a singly linked list representing a customer's cart, return `true`
if the sum of node values in the first half equals the sum of node values in the
second half, and `false` otherwise.

If the list has an odd number of nodes, the middle node is excluded from both halves.

Constraints:
- The number of nodes in the list is in the range [1, 10^4]
- 1 <= Node.val <= 1000
- You must solve it in O(n) time and O(1) extra space (excluding the output).

Example 1:
Input: head = [3, 7, 2, 7, 3]
Output: true
Explanation: First half = [3, 7], sum = 10. Middle node = [2] (excluded).
             Second half = [7, 3], sum = 10. Sums are equal.

Example 2:
Input: head = [1, 4, 9, 2]
Output: false
Explanation: First half = [1, 4], sum = 5. Second half = [9, 2], sum = 11.
             Sums are not equal.
"""

from typing import Optional


# ─────────────────────────────────────────────
# Definition of the singly linked list node
# ─────────────────────────────────────────────
class ListNode:
    """A single node in a singly linked list."""

    def __init__(self, val: int = 0, next: "Optional[ListNode]" = None):
        self.val = val
        self.next = next


class Solution:
    """Contains the algorithm to check if a linked list cart is 'balanced'."""

    def isBalanced(self, head: Optional[ListNode]) -> bool:
        """
        Determine whether the sum of the first half of the linked list equals
        the sum of the second half. The middle node (for odd-length lists) is
        excluded from both halves.

        High-level strategy
        -------------------
        We use the classic "slow & fast pointer" (Floyd's tortoise-and-hare)
        technique to find the middle of the list in a single pass, while
        simultaneously accumulating the sum of the first half.  Then we walk
        the second half and accumulate its sum.  Finally we compare the two
        sums.

        This keeps time complexity O(n) and extra space O(1) — we only use a
        handful of pointer/integer variables regardless of list length.

        Args:
            head: The head node of the singly linked list.

        Returns:
            True  if first-half sum == second-half sum, False otherwise.

        Time Complexity:  O(n) — we traverse the list at most twice.
        Space Complexity: O(1) — only a fixed number of extra variables used.
        """

        # ── Edge case: an empty list or a single-node list ──────────────────
        # A list with 0 or 1 nodes has no meaningful "halves", so we treat it
        # as balanced (both halves are empty / zero).
        if head is None or head.next is None:
            return True

        # ── Step 1: Use slow/fast pointers to find the middle ───────────────
        #
        # • `slow` advances ONE step at a time.
        # • `fast` advances TWO steps at a time.
        #
        # When `fast` reaches the end of the list, `slow` will be sitting at
        # (or just past) the middle node.
        #
        # We also accumulate `first_half_sum` as `slow` moves forward, so we
        # capture the sum of the first half in the same pass.
        #
        # Why slow/fast pointers?
        #   They let us find the midpoint in O(n) time without knowing the
        #   length of the list in advance, and without any extra storage.

        slow: Optional[ListNode] = head   # tortoise — moves 1 step
        fast: Optional[ListNode] = head   # hare     — moves 2 steps
        first_half_sum: int = 0

        # Walk until `fast` can no longer take two steps.
        # Loop invariant: before each iteration, `slow` points to the next
        # node whose value should be added to the first-half sum.
        while fast is not None and fast.next is not None:
            # Add the current slow-pointer node's value to the first-half sum.
            first_half_sum += slow.val  # type: ignore[union-attr]

            # Advance slow by 1 and fast by 2.
            slow = slow.next            # type: ignore[union-attr]
            fast = fast.next.next       # type: ignore[union-attr]

        # ── Step 2: Determine where the second half starts ──────────────────
        #
        # After the loop:
        #   • If the list length is EVEN  → `fast` is None.
        #     `slow` is now at the first node of the second half.
        #     Example (even, n=4): nodes 0-1-2-3
        #       After loop: slow → node 2, fast → None
        #       first_half_sum = node0 + node1
        #       second half starts at node 2
        #
        #   • If the list length is ODD   → `fast.next` is None (fast is the
        #     last node).
        #     `slow` is at the middle node, which must be EXCLUDED.
        #     So the second half starts at slow.next.
        #     Example (odd, n=5): nodes 0-1-2-3-4
        #       After loop: slow → node 2 (middle), fast → node 4
        #       first_half_sum = node0 + node1
        #       second half starts at node 3

        if fast is not None:
            # Odd-length list: skip the middle node (slow is the middle).
            second_half_start: Optional[ListNode] = slow.next  # type: ignore[union-attr]
        else:
            # Even-length list: slow is already the first node of the 2nd half.
            second_half_start = slow

        # ── Step 3: Sum the second half ──────────────────────────────────────
        # Walk from `second_half_start` to the end of the list.
        second_half_sum: int = 0
        current: Optional[ListNode] = second_half_start

        while current is not None:
            second_half_sum += current.val
            current = current.next

        # ── Step 4: Compare and return ───────────────────────────────────────
        # The cart is "balanced" if and only if both sums are equal.
        return first_half_sum == second_half_sum


# ─────────────────────────────────────────────────────────────────────────────
# Helper utilities (not part of the algorithm)
# ─────────────────────────────────────────────────────────────────────────────

def build_linked_list(values: list[int]) -> Optional[ListNode]:
    """
    Convert a plain Python list of integers into a singly linked list.

    Args:
        values: List of integer node values.

    Returns:
        The head node of the constructed linked list, or None if values is empty.
    """
    if not values:
        return None

    # Create the head node from the first value.
    head = ListNode(values[0])
    current = head

    # Append each subsequent value as a new node.
    for val in values[1:]:
        current.next = ListNode(val)
        current = current.next

    return head


def linked_list_to_str(head: Optional[ListNode]) -> str:
    """
    Convert a linked list back to a readable string for display purposes.

    Args:
        head: The head node of the linked list.

    Returns:
        A string representation like "3 -> 7 -> 2 -> 7 -> 3".
    """
    nodes: list[str] = []
    current = head
    while current is not None:
        nodes.append(str(current.val))
        current = current.next
    return " -> ".join(nodes) if nodes else "(empty)"


# ─────────────────────────────────────────────────────────────────────────────
# Main block — trace through the provided examples and additional edge cases
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    solver = Solution()

    # ── Example 1 ────────────────────────────────────────────────────────────
    # Input : [3, 7, 2, 7, 3]  (odd length = 5)
    # First half  = [3, 7]  → sum = 10
    # Middle node = [2]     → excluded
    # Second half = [7, 3]  → sum = 10
    # Expected output: True
    values1 = [3, 7, 2, 7, 3]
    head1 = build_linked_list(values1)
    result1 = solver.isBalanced(head1)
    print("─" * 50)
    print(f"Example 1")
    print(f"  List   : {linked_list_to_str(head1)}")
    print(f"  Result : {result1}")
    print(f"  Expected: True")
    assert result1 is True, "Example 1 FAILED"
    print("  ✓ PASSED")

    # ── Example 2 ────────────────────────────────────────────────────────────
    # Input : [1, 4, 9, 2]  (even length = 4)
    # First half  = [1, 4]  → sum = 5
    # Second half = [9, 2]  → sum = 11
    # Expected output: False
    values2 = [1, 4, 9, 2]
    head2 = build_linked_list(values2)
    result2 = solver.isBalanced(head2)
    print("─" * 50)
    print(f"Example 2")
    print(f"  List   : {linked_list_to_str(head2)}")
    print(f"  Result : {result2}")
    print(f"  Expected: False")
    assert result2 is False, "Example 2 FAILED"
    print("  ✓ PASSED")

    # ── Edge case: single node ────────────────────────────────────────────────
    # A single node has no halves → considered balanced.
    values3 = [42]
    head3 = build_linked_list(values3)
    result3 = solver.isBalanced(head3)
    print("─" * 50)
    print(f"Edge Case: Single Node")
    print(f"  List   : {linked_list_to_str(head3)}")
    print(f"  Result : {result3}")
    print(f"  Expected: True")
    assert result3 is True, "Single-node edge case FAILED"
    print("  ✓ PASSED")

    # ── Edge case: two nodes, equal ───────────────────────────────────────────
    # [5, 5] → first half = [5], second half = [5] → balanced
    values4 = [5, 5]
    head4 = build_linked_list(values4)
    result4 = solver.isBalanced(head4)
    print("─" * 50)
    print(f"Edge Case: Two Equal Nodes")
    print(f"  List   : {linked_list_to_str(head4)}")
    print(f"  Result : {result4}")
    print(f"  Expected: True")
    assert result4 is True, "Two-equal-nodes edge case FAILED"
    print("  ✓ PASSED")

    # ── Edge case: two nodes, unequal ─────────────────────────────────────────
    # [3, 7] → first half = [3], second half = [7] → not balanced
    values5 = [3, 7]
    head5 = build_linked_list(values5)
    result5 = solver.isBalanced(head5)
    print("─" * 50)
    print(f"Edge Case: Two Unequal Nodes")
    print(f"  List   : {linked_list_to_str(head5)}")
    print(f"  Result : {result5}")
    print(f"  Expected: False")
    assert result5 is False, "Two-unequal-nodes edge case FAILED"
    print("  ✓ PASSED")

    # ── Edge case: odd length, balanced ───────────────────────────────────────
    # [1, 2, 99, 2, 1] → first=[1,2] sum=3, mid=99, second=[2,1] sum=3
    values6 = [1, 2, 99, 2, 1]
    head6 = build_linked_list(values6)
    result6 = solver.isBalanced(head6)
    print("─" * 50)
    print(f"Edge Case: Odd Length Balanced")
    print(f"  List   : {linked_list_to_str(head6)}")
    print(f"  Result : {result6}")
    print(f"  Expected: True")
    assert result6 is True, "Odd-length balanced edge case FAILED"
    print("  ✓ PASSED")

    print("─" * 50)
    print("All test cases passed! ✓")
```