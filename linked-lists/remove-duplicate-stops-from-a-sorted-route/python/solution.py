"""
Title: Remove Duplicate Stops from a Sorted Route

Problem Description:
A city transit system stores the stops of a bus route in a singly linked list.
The stop IDs are sorted in non-decreasing order because nearby route planning
software groups identical stop IDs together. Sometimes duplicate stop entries
appear due to data import issues, and you need to clean the route.

Given the head of a singly linked list where each node contains an integer stop ID,
remove duplicate nodes so that each stop ID appears only once in the final list.
Since the list is already sorted, all duplicates of the same value will appear
next to each other. You must modify the linked list in place by updating next
pointers and return the head of the cleaned list.

If the list is empty, return null. If the list has only one node, it should be
returned unchanged.

Your goal is to keep the first occurrence of each stop ID and remove any
immediately repeated copies that follow it.

Constraints:
- The number of nodes in the list is in the range [0, 300].
- -1000 <= Node.val <= 1000
- The linked list is sorted in non-decreasing order.
- Use O(1) extra space, excluding the input list.

Example 1:
Input: head = [4,4,7,7,7,9,12,12]
Output: [4,7,9,12]

Example 2:
Input: head = [1,2,2,3,5,5,8]
Output: [1,2,3,5,8]
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
        self.val: int = val
        self.next: Optional[ListNode] = next


class Solution:
    def deleteDuplicates(self, head: Optional[ListNode]) -> Optional[ListNode]:
        """
        Remove duplicates from a sorted singly linked list in place.

        Because the list is sorted, any duplicate values must appear directly
        next to each other. This allows us to scan the list once and compare
        each node with its next node. When two adjacent nodes have the same
        value, we skip the duplicate by changing pointers.

        Args:
            head: The head of the sorted singly linked list.

        Returns:
            The head of the modified linked list with duplicates removed.

        Time complexity:
            O(n), where n is the number of nodes in the list.

        Space complexity:
            O(1), because we only use a constant amount of extra space.
        """
        # If the list is empty, there is nothing to clean.
        # We simply return None.
        if head is None:
            return None

        # We use a pointer named "current" to walk through the linked list.
        # This pointer will always represent the node we are currently checking.
        current: Optional[ListNode] = head

        # We continue as long as:
        # 1. "current" is a valid node, and
        # 2. "current.next" exists, because we need to compare current with the next node.
        while current is not None and current.next is not None:
            # Since the list is sorted, duplicates can only appear immediately after
            # the first occurrence. So comparing current.val with current.next.val
            # is enough to detect duplicates.
            if current.val == current.next.val:
                # Duplicate found.
                #
                # Example:
                # current -> [7] -> [7] -> [7] -> [9]
                #
                # We want to keep the first [7] and remove the next duplicate.
                # To do that, we change current.next so it skips over the duplicate node.
                #
                # Before:
                # current.next = duplicate node
                #
                # After:
                # current.next = duplicate node's next
                #
                # This removes one duplicate from the chain.
                current.next = current.next.next
            else:
                # No duplicate at this position.
                #
                # That means current and current.next have different values,
                # so current is already correct and we can safely move forward.
                current = current.next

        # The original head still points to the cleaned list,
        # so we return it.
        return head


def build_linked_list(values: List[int]) -> Optional[ListNode]:
    """
    Build a singly linked list from a Python list of integers.

    Args:
        values: List of integer values to place into the linked list.

    Returns:
        The head of the newly created linked list, or None if the input is empty.

    Time complexity:
        O(n), where n is the number of values.

    Space complexity:
        O(n), for the created linked list nodes.
    """
    # If the input list is empty, the linked list should also be empty.
    if not values:
        return None

    # Create the first node, which becomes the head.
    head: ListNode = ListNode(values[0])

    # "tail" keeps track of the last node so we can append efficiently.
    tail: ListNode = head

    # Create the remaining nodes and link them one by one.
    for value in values[1:]:
        tail.next = ListNode(value)
        tail = tail.next

    return head


def linked_list_to_list(head: Optional[ListNode]) -> List[int]:
    """
    Convert a singly linked list back into a Python list.

    Args:
        head: The head of the linked list.

    Returns:
        A list containing the node values in order.

    Time complexity:
        O(n), where n is the number of nodes.

    Space complexity:
        O(n), for the output Python list.
    """
    result: List[int] = []
    current: Optional[ListNode] = head

    # Traverse the list from head to end, collecting values.
    while current is not None:
        result.append(current.val)
        current = current.next

    return result


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement:
    # Input: [4,4,7,7,7,9,12,12]
    # Expected output: [4,7,9,12]
    example_1: List[int] = [4, 4, 7, 7, 7, 9, 12, 12]
    head_1: Optional[ListNode] = build_linked_list(example_1)
    cleaned_1: Optional[ListNode] = solution.deleteDuplicates(head_1)
    print("Input:", example_1)
    print("Output:", linked_list_to_list(cleaned_1))
    print("Expected:", [4, 7, 9, 12])
    print()

    # Example 2 from the problem statement:
    # Input: [1,2,2,3,5,5,8]
    # Expected output: [1,2,3,5,8]
    example_2: List[int] = [1, 2, 2, 3, 5, 5, 8]
    head_2: Optional[ListNode] = build_linked_list(example_2)
    cleaned_2: Optional[ListNode] = solution.deleteDuplicates(head_2)
    print("Input:", example_2)
    print("Output:", linked_list_to_list(cleaned_2))
    print("Expected:", [1, 2, 3, 5, 8])
    print()

    # Additional beginner-friendly checks:
    # Empty list
    example_3: List[int] = []
    head_3: Optional[ListNode] = build_linked_list(example_3)
    cleaned_3: Optional[ListNode] = solution.deleteDuplicates(head_3)
    print("Input:", example_3)
    print("Output:", linked_list_to_list(cleaned_3))
    print("Expected:", [])
    print()

    # Single node list
    example_4: List[int] = [42]
    head_4: Optional[ListNode] = build_linked_list(example_4)
    cleaned_4: Optional[ListNode] = solution.deleteDuplicates(head_4)
    print("Input:", example_4)
    print("Output:", linked_list_to_list(cleaned_4))
    print("Expected:", [42])