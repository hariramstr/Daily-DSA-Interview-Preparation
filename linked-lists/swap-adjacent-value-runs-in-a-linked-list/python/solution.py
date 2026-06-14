"""
Title: Swap Adjacent Value Runs in a Linked List

Problem Description:
You are given the head of a singly linked list representing a stream of event codes.
Consecutive nodes with the same value form a run. Your task is to rearrange the list
by swapping every two adjacent runs, while preserving the internal order of nodes
inside each run.

A run is a maximal contiguous block of nodes with equal values. For example, in the
list 4 -> 4 -> 1 -> 1 -> 1 -> 3 -> 2 -> 2, the runs are [4,4], [1,1,1], [3], and [2,2].
After swapping adjacent runs, the result becomes [1,1,1] -> [4,4] -> [2,2] -> [3].

If the list contains an odd number of runs, the final run stays in its original
position. You must relink existing nodes and should not create a new list of copied
values. The goal is to return the head of the modified list.

Constraints:
- The number of nodes is in the range [0, 2 * 10^5].
- Node values are in the range [-10^9, 10^9].
- The list is singly linked.
- The solution should run in O(n) time.
- Extra space should be O(1), excluding recursion stack and input storage.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import List, Optional, Tuple


@dataclass
class ListNode:
    """Node for a singly linked list."""
    val: int
    next: Optional["ListNode"] = None


class Solution:
    def _get_run(self, start: ListNode) -> Tuple[ListNode, ListNode, Optional[ListNode]]:
        """
        Find the maximal run of equal values starting at `start`.

        Args:
            start: The first node of the run.

        Returns:
            A tuple containing:
            - run_head: First node of the run
            - run_tail: Last node of the run
            - next_run_head: First node after the run, or None if no more nodes

        Time complexity:
            O(k), where k is the length of this run.

        Space complexity:
            O(1)
        """
        run_head: ListNode = start
        run_tail: ListNode = start

        # Move forward while the next node exists and still belongs to the same run.
        while run_tail.next is not None and run_tail.next.val == run_head.val:
            run_tail = run_tail.next

        next_run_head: Optional[ListNode] = run_tail.next
        return run_head, run_tail, next_run_head

    def swap_adjacent_value_runs(self, head: Optional[ListNode]) -> Optional[ListNode]:
        """
        Swap every two adjacent value-runs in a singly linked list.

        A run is a maximal contiguous block of nodes with the same value.
        The internal order of nodes inside each run is preserved.

        Args:
            head: Head of the input singly linked list.

        Returns:
            Head of the modified linked list after swapping adjacent runs.

        Time complexity:
            O(n), because each node is visited a constant number of times.

        Space complexity:
            O(1), because only a few pointers are used.
        """
        # If the list is empty or has only one node, there is nothing to rearrange.
        if head is None or head.next is None:
            return head

        # A dummy node is a standard linked-list technique that makes pointer updates
        # easier, especially when the head itself may change after the first swap.
        dummy: ListNode = ListNode(0, head)

        # `prev_tail` always points to the node that should connect to the next
        # processed portion of the list.
        prev_tail: ListNode = dummy

        # `current` points to the first node of the next unprocessed run.
        current: Optional[ListNode] = head

        # Process the list run by run.
        while current is not None:
            # Identify the first run in the current pair.
            first_head, first_tail, second_start = self._get_run(current)

            # If there is no second run, then the number of runs is odd and this final
            # run must remain in place. We simply connect it and stop.
            if second_start is None:
                prev_tail.next = first_head
                break

            # Identify the second run in the current pair.
            second_head, second_tail, next_pair_start = self._get_run(second_start)

            # We now have:
            #   first run  = first_head ... first_tail
            #   second run = second_head ... second_tail
            #   remainder  = next_pair_start ...
            #
            # We want to transform:
            #   prev_tail -> first run -> second run -> remainder
            # into:
            #   prev_tail -> second run -> first run -> remainder

            # Connect the previous processed part to the second run,
            # because after swapping, the second run comes first.
            prev_tail.next = second_head

            # Connect the end of the second run to the start of the first run.
            second_tail.next = first_head

            # Connect the end of the first run to the remainder of the list.
            first_tail.next = next_pair_start

            # After the swap, the first run is now the second run in the pair,
            # so its tail becomes the new `prev_tail`.
            prev_tail = first_tail

            # Continue processing from the next unprocessed run.
            current = next_pair_start

        return dummy.next


def build_linked_list(values: List[int]) -> Optional[ListNode]:
    """
    Build a singly linked list from a Python list.

    Args:
        values: List of integer values.

    Returns:
        Head of the created linked list, or None for an empty list.

    Time complexity:
        O(n)

    Space complexity:
        O(n) for the created nodes
    """
    if not values:
        return None

    head: ListNode = ListNode(values[0])
    tail: ListNode = head

    for value in values[1:]:
        tail.next = ListNode(value)
        tail = tail.next

    return head


def linked_list_to_list(head: Optional[ListNode]) -> List[int]:
    """
    Convert a singly linked list to a Python list.

    Args:
        head: Head of the linked list.

    Returns:
        List of node values in order.

    Time complexity:
        O(n)

    Space complexity:
        O(n) for the output list
    """
    result: List[int] = []
    current: Optional[ListNode] = head

    while current is not None:
        result.append(current.val)
        current = current.next

    return result


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Input:  [4,4,1,1,1,3,2,2]
    # Runs:   [4,4], [1,1,1], [3], [2,2]
    # Output: [1,1,1,4,4,2,2,3]
    example1: List[int] = [4, 4, 1, 1, 1, 3, 2, 2]
    head1: Optional[ListNode] = build_linked_list(example1)
    result1: Optional[ListNode] = solution.swap_adjacent_value_runs(head1)
    print(linked_list_to_list(result1))

    # Example 2:
    # Input:  [7,7,5,6,6,6,9]
    # Runs:   [7,7], [5], [6,6,6], [9]
    # Output: [5,7,7,9,6,6,6]
    example2: List[int] = [7, 7, 5, 6, 6, 6, 9]
    head2: Optional[ListNode] = build_linked_list(example2)
    result2: Optional[ListNode] = solution.swap_adjacent_value_runs(head2)
    print(linked_list_to_list(result2))

    # Additional quick checks for beginner-friendly demonstration.
    extra_tests: List[List[int]] = [
        [],
        [1],
        [2, 2, 2],
        [1, 2],
        [1, 1, 2, 2, 3, 3],
        [5, 5, 4, 4, 4, 3],
    ]

    for values in extra_tests:
        head: Optional[ListNode] = build_linked_list(values)
        result: Optional[ListNode] = solution.swap_adjacent_value_runs(head)
        print(f"{values} -> {linked_list_to_list(result)}")