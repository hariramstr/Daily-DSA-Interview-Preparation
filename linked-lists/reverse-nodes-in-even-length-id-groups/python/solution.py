"""
Title: Reverse Nodes in Even-Length ID Groups

Problem Description:
You are given the head of a singly linked list representing a stream of record IDs.
Starting from the head, split the list into consecutive groups whose intended sizes
are 1, 2, 3, 4, and so on. The last group may contain fewer nodes than its intended
size if the list runs out.

Your task is to reverse the nodes inside every group whose actual length is even,
while leaving odd-length groups unchanged. The groups must remain in the same overall
order, and only the node links should be modified. You may not create a second list
of all values and rebuild the answer from scratch.

For example, if the list is grouped as [a], [b, c], [d, e, f], [g, h, i, j], then
the second and fourth groups should be reversed because their lengths are 2 and 4,
both even. If the final group has fewer nodes than expected, use its actual size
when deciding whether to reverse it.

Return the head of the modified linked list.

Constraints:
- The number of nodes in the list is in the range [1, 100000].
- Node values are integers in the range [-1000000000, 1000000000].
- The list is singly linked.
- Aim for O(n) time complexity.
- Extra space should be O(1), excluding recursion stack and input storage.
"""

from __future__ import annotations

from typing import List, Optional


class ListNode:
    """Node for a singly linked list."""

    def __init__(self, val: int = 0, next: Optional["ListNode"] = None) -> None:
        """
        Initialize a linked list node.

        Args:
            val: Integer value stored in the node.
            next: Reference to the next node.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.val = val
        self.next = next


class Solution:
    def reverseEvenLengthGroups(self, head: Optional[ListNode]) -> Optional[ListNode]:
        """
        Reverse nodes in every group whose actual length is even.

        The list is processed in groups of intended sizes 1, 2, 3, 4, ...
        The final group may be shorter than its intended size. For each group,
        if the group's actual size is even, the nodes in that group are reversed
        in place. If the size is odd, the group is left unchanged.

        Args:
            head: Head of the singly linked list.

        Returns:
            The head of the modified linked list.

        Time complexity:
            O(n), because each node is visited a constant number of times.

        Space complexity:
            O(1), because the algorithm uses only a few pointer variables.
        """
        # If the list is empty or has only one node, there is nothing meaningful
        # to reverse. Returning early keeps the code simple and avoids unnecessary work.
        if head is None or head.next is None:
            return head

        # A dummy node is a very common linked-list technique.
        # Why use it?
        # - It gives us a stable node before the real head.
        # - This makes pointer updates easier, especially if a group starting at
        #   the head ever needs to be reconnected differently.
        # In this problem, the first group has size 1 and will never be reversed,
        # but using a dummy still keeps the logic uniform and beginner-friendly.
        dummy = ListNode(0, head)

        # prev_group_tail will always point to the node immediately BEFORE
        # the current group we are about to process.
        #
        # At the beginning:
        # dummy -> head -> ...
        # so the node before the first group is dummy.
        prev_group_tail: ListNode = dummy

        # group_size is the intended size of the current group:
        # first group wants 1 node, second wants 2, third wants 3, etc.
        group_size = 1

        # Continue processing as long as there are nodes left after prev_group_tail.
        while prev_group_tail.next is not None:
            # group_head is the first node of the current group.
            group_head = prev_group_tail.next

            # We now need to determine the ACTUAL length of this group.
            # This is important because the final group may have fewer nodes than
            # its intended size.
            #
            # Example:
            # If group_size is 4 but only 3 nodes remain, actual_length = 3.
            actual_length = 0
            current = group_head

            # Move through at most group_size nodes, counting how many actually exist.
            while current is not None and actual_length < group_size:
                actual_length += 1
                current = current.next

            # After the loop:
            # - actual_length is the number of nodes in this group
            # - current points to the first node AFTER this group
            #
            # We will call that node next_group_head because that is exactly what it is.
            next_group_head = current

            # If the actual group length is even, we must reverse this group in place.
            if actual_length % 2 == 0:
                # We reverse exactly actual_length nodes starting from group_head.
                #
                # Standard linked-list reversal idea:
                # - prev starts as the node that should come AFTER the reversed group
                #   so that when reversal is complete, the group's tail already points
                #   to the correct next node.
                # - curr walks through the group.
                #
                # Example:
                # group: A -> B -> C -> D
                # next_group_head: X
                #
                # Start:
                # prev = X
                # curr = A
                #
                # After reversal:
                # D -> C -> B -> A -> X
                prev: Optional[ListNode] = next_group_head
                curr: Optional[ListNode] = group_head

                # Reverse exactly actual_length nodes.
                for _ in range(actual_length):
                    # Save the next node before changing curr.next.
                    temp_next = curr.next if curr is not None else None

                    # Reverse the pointer:
                    # current node now points backward to prev.
                    if curr is not None:
                        curr.next = prev

                    # Advance prev and curr one step forward in the original list.
                    prev = curr
                    curr = temp_next

                # After reversal:
                # - prev points to the NEW head of the reversed group
                # - group_head is now the TAIL of the reversed group
                #
                # We must connect the previous part of the list to the new group head.
                prev_group_tail.next = prev

                # Since group_head became the tail after reversal,
                # it is now the "previous group tail" for the next iteration.
                prev_group_tail = group_head
            else:
                # If the group length is odd, we do NOT reverse it.
                # But we still need to move prev_group_tail to the end of this group
                # so that the next iteration starts correctly.
                #
                # We know the group starts at group_head and has actual_length nodes.
                # So we advance prev_group_tail actual_length steps.
                prev_group_tail = group_head
                for _ in range(actual_length - 1):
                    prev_group_tail = prev_group_tail.next  # type: ignore[assignment]

            # Move to the next intended group size.
            group_size += 1

        # The real head is after the dummy node.
        return dummy.next


def build_linked_list(values: List[int]) -> Optional[ListNode]:
    """
    Build a singly linked list from a Python list.

    Args:
        values: List of integer values.

    Returns:
        Head of the created linked list, or None if the input list is empty.

    Time complexity:
        O(n), where n is the number of values.

    Space complexity:
        O(n), for the created linked list nodes.
    """
    dummy = ListNode()
    tail = dummy

    for value in values:
        tail.next = ListNode(value)
        tail = tail.next

    return dummy.next


def linked_list_to_list(head: Optional[ListNode]) -> List[int]:
    """
    Convert a singly linked list to a Python list.

    Args:
        head: Head of the linked list.

    Returns:
        A list containing the node values in order.

    Time complexity:
        O(n), where n is the number of nodes.

    Space complexity:
        O(n), for the output list.
    """
    result: List[int] = []
    current = head

    while current is not None:
        result.append(current.val)
        current = current.next

    return result


if __name__ == "__main__":
    # Create a Solution instance once and reuse it for all examples.
    solution = Solution()

    # Example 1 from the problem statement:
    # Input: [5,8,3,9,1,4]
    # Groups:
    #   [5]       -> length 1 (odd)  -> unchanged
    #   [8,3]     -> length 2 (even) -> reversed to [3,8]
    #   [9,1,4]   -> length 3 (odd)  -> unchanged
    # Expected output: [5,3,8,9,1,4]
    example_1 = [5, 8, 3, 9, 1, 4]
    head_1 = build_linked_list(example_1)
    result_1 = solution.reverseEvenLengthGroups(head_1)
    print("Example 1 input:   ", example_1)
    print("Example 1 output:  ", linked_list_to_list(result_1))
    print("Example 1 expected:", [5, 3, 8, 9, 1, 4])
    print()

    # Example 2 from the problem statement:
    # Input: [1,2,3,4,5,6,7,8,9]
    # Groups:
    #   [1]         -> length 1 (odd)  -> unchanged
    #   [2,3]       -> length 2 (even) -> reversed to [3,2]
    #   [4,5,6]     -> length 3 (odd)  -> unchanged
    #   [7,8,9]     -> intended size 4, actual size 3 (odd) -> unchanged
    # Expected output: [1,3,2,4,5,6,7,8,9]
    #
    # Note:
    # The problem statement's shown output [1,3,2,4,5,6,9,8,7] would reverse the
    # final group of actual length 3, which contradicts the rule that only even-
    # length groups are reversed. The correct output under the stated rules is
    # [1,3,2,4,5,6,7,8,9].
    example_2 = [1, 2, 3, 4, 5, 6, 7, 8, 9]
    head_2 = build_linked_list(example_2)
    result_2 = solution.reverseEvenLengthGroups(head_2)
    print("Example 2 input:   ", example_2)
    print("Example 2 output:  ", linked_list_to_list(result_2))
    print("Example 2 expected:", [1, 3, 2, 4, 5, 6, 7, 8, 9])
    print()

    # Additional quick sanity check:
    # Input: [1,2,3,4,5]
    # Groups:
    #   [1]       -> odd -> unchanged
    #   [2,3]     -> even -> reversed to [3,2]
    #   [4,5]     -> intended size 3, actual size 2 -> even -> reversed to [5,4]
    # Expected: [1,3,2,5,4]
    example_3 = [1, 2, 3, 4, 5]
    head_3 = build_linked_list(example_3)
    result_3 = solution.reverseEvenLengthGroups(head_3)
    print("Example 3 input:   ", example_3)
    print("Example 3 output:  ", linked_list_to_list(result_3))
    print("Example 3 expected:", [1, 3, 2, 5, 4])