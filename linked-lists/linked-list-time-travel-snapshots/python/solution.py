"""
Linked List Time Travel Snapshots
==================================

Problem Description:
You are given a singly linked list where each node contains an integer value and a timestamp.
The list is sorted in ascending order by timestamp. You are also given a list of query operations,
each of which is one of the following:

- INSERT t v: Insert a new node with timestamp t and value v into the correct sorted position.
- SNAPSHOT t: Return the sum of values of all nodes with timestamps <= t at the time of this query.
- ROLLBACK t: Remove all nodes with timestamps strictly greater than t from the list.

After processing all operations, return an array of integers — one result for each SNAPSHOT query,
in the order they were encountered.

Constraints:
- 1 <= number of operations <= 10^5
- 1 <= t <= 10^9
- -10^4 <= v <= 10^4
- Timestamps in INSERT operations are not necessarily unique
- The initial linked list may be empty
- ROLLBACK does not affect nodes with timestamps equal to t
- The answer for each SNAPSHOT query fits in a 32-bit signed integer
"""

from typing import List, Optional, Tuple


class Node:
    """
    Represents a single node in the singly linked list.
    Each node holds a timestamp, a value, and a pointer to the next node.
    """
    def __init__(self, timestamp: int, value: int):
        self.timestamp = timestamp
        self.value = value
        self.next: Optional['Node'] = None


class LinkedList:
    """
    A singly linked list sorted by timestamp in ascending order.
    Supports insert (maintaining sort order), snapshot (prefix sum up to timestamp),
    and rollback (truncate nodes with timestamp > t).
    """

    def __init__(self):
        # Sentinel/dummy head node makes insert and rollback logic uniform
        # (no special-casing for the head of the list)
        self.head: Node = Node(-1, 0)  # dummy head with sentinel timestamp -1

    def insert(self, t: int, v: int) -> None:
        """
        Insert a node with timestamp t and value v in sorted order.

        Args:
            t: The timestamp of the new node.
            v: The value of the new node.

        Time complexity: O(n) where n is the number of nodes.
        Space complexity: O(1) extra space.
        """
        # Walk the list until we find the correct insertion point.
        # We insert AFTER 'current' when current.next is None OR
        # current.next.timestamp > t  (stable: equal timestamps go after existing ones).
        current = self.head
        while current.next is not None and current.next.timestamp <= t:
            current = current.next

        # Create the new node and splice it in between current and current.next
        new_node = Node(t, v)
        new_node.next = current.next
        current.next = new_node

    def snapshot(self, t: int) -> int:
        """
        Return the sum of values of all nodes with timestamp <= t.

        Args:
            t: The upper bound timestamp (inclusive).

        Returns:
            Integer sum of values for nodes with timestamp <= t.

        Time complexity: O(n)
        Space complexity: O(1)
        """
        total = 0
        current = self.head.next  # skip dummy head
        while current is not None and current.timestamp <= t:
            total += current.value
            current = current.next
        return total

    def rollback(self, t: int) -> None:
        """
        Remove all nodes with timestamp strictly greater than t.

        Args:
            t: Nodes with timestamp > t are removed.

        Time complexity: O(n)
        Space complexity: O(1)
        """
        # Walk until we find the last node whose timestamp <= t
        current = self.head
        while current.next is not None and current.next.timestamp <= t:
            current = current.next
        # Sever the list at this point — everything after is dropped
        current.next = None


class Solution:
    """
    Processes a series of INSERT / SNAPSHOT / ROLLBACK operations on a
    timestamp-sorted linked list and returns the results of all SNAPSHOT queries.
    """

    def process_operations(
        self,
        initial: List[Tuple[int, int]],
        operations: List[str]
    ) -> List[int]:
        """
        Process all operations and collect SNAPSHOT results.

        Args:
            initial:    List of (timestamp, value) tuples representing the
                        initial state of the linked list (already sorted by
                        timestamp, but we insert them one by one to be safe).
            operations: List of operation strings, each being one of:
                        "INSERT t v", "SNAPSHOT t", "ROLLBACK t".

        Returns:
            A list of integers, one per SNAPSHOT query, in encounter order.

        Time complexity:  O(Q * N) where Q = number of operations, N = list length.
                          Each operation walks at most the full list.
        Space complexity: O(N) for the linked list itself.
        """

        # ------------------------------------------------------------------ #
        # Step 1: Build the linked list from the initial data.
        # We use our LinkedList class which maintains sorted order on insert.
        # ------------------------------------------------------------------ #
        ll = LinkedList()
        for ts, val in initial:
            # Insert each initial node; since initial is already sorted this
            # is O(n) per insert but keeps the code simple and correct.
            ll.insert(ts, val)

        # ------------------------------------------------------------------ #
        # Step 2: Prepare the results container.
        # We only append to this list when we encounter a SNAPSHOT operation.
        # ------------------------------------------------------------------ #
        results: List[int] = []

        # ------------------------------------------------------------------ #
        # Step 3: Process each operation in order.
        # ------------------------------------------------------------------ #
        for op in operations:
            # Split the operation string into its components.
            # "INSERT 4 15"  -> parts = ["INSERT", "4", "15"]
            # "SNAPSHOT 4"   -> parts = ["SNAPSHOT", "4"]
            # "ROLLBACK 3"   -> parts = ["ROLLBACK", "3"]
            parts = op.split()
            command = parts[0]

            if command == "INSERT":
                # ---------------------------------------------------------- #
                # INSERT t v
                # Parse timestamp and value, then insert into sorted position.
                # ---------------------------------------------------------- #
                t = int(parts[1])
                v = int(parts[2])
                # Delegate to the linked list's insert method which walks the
                # list to find the correct sorted position.
                ll.insert(t, v)

            elif command == "SNAPSHOT":
                # ---------------------------------------------------------- #
                # SNAPSHOT t
                # Sum all node values with timestamp <= t and record the result.
                # ---------------------------------------------------------- #
                t = int(parts[1])
                # Walk the list and accumulate values up to timestamp t.
                result = ll.snapshot(t)
                # Append to results — this is the output for this SNAPSHOT.
                results.append(result)

            elif command == "ROLLBACK":
                # ---------------------------------------------------------- #
                # ROLLBACK t
                # Remove all nodes with timestamp strictly greater than t.
                # Nodes with timestamp == t are KEPT (per problem statement).
                # ---------------------------------------------------------- #
                t = int(parts[1])
                ll.rollback(t)

            # Any unrecognised command is silently ignored (defensive coding).

        # ------------------------------------------------------------------ #
        # Step 4: Return the collected SNAPSHOT results.
        # ------------------------------------------------------------------ #
        return results


# --------------------------------------------------------------------------- #
# Verification helpers — trace through the examples manually
# --------------------------------------------------------------------------- #

def _list_contents(ll: LinkedList) -> List[Tuple[int, int]]:
    """Helper: return list contents as [(ts, val), ...] for debugging."""
    result = []
    cur = ll.head.next
    while cur:
        result.append((cur.timestamp, cur.value))
        cur = cur.next
    return result


# --------------------------------------------------------------------------- #
# Main entry point — demonstrates both examples from the problem description
# --------------------------------------------------------------------------- #

if __name__ == "__main__":
    sol = Solution()

    # ------------------------------------------------------------------ #
    # Example 1
    # Initial list: [(1, 10), (3, 20), (5, 30)]
    # Operations:   ["INSERT 4 15", "SNAPSHOT 4", "ROLLBACK 3", "SNAPSHOT 4"]
    # Expected:     [75, 30]
    #
    # Trace:
    #   Start:        [(1,10),(3,20),(5,30)]
    #   INSERT 4 15:  [(1,10),(3,20),(4,15),(5,30)]
    #   SNAPSHOT 4:   10+20+15 = 75  ✓
    #   ROLLBACK 3:   [(1,10),(3,20)]
    #   SNAPSHOT 4:   10+20 = 30     ✓
    # ------------------------------------------------------------------ #
    initial1 = [(1, 10), (3, 20), (5, 30)]
    ops1 = ["INSERT 4 15", "SNAPSHOT 4", "ROLLBACK 3", "SNAPSHOT 4"]
    output1 = sol.process_operations(initial1, ops1)
    print("Example 1:")
    print(f"  Operations : {ops1}")
    print(f"  Output     : {output1}")
    print(f"  Expected   : [75, 30]")
    assert output1 == [75, 30], f"Example 1 FAILED: got {output1}"
    print("  PASSED ✓\n")

    # ------------------------------------------------------------------ #
    # Example 2
    # Initial list: []
    # Operations:   ["INSERT 2 5", "INSERT 2 3", "SNAPSHOT 2",
    #                "INSERT 4 10", "ROLLBACK 2", "SNAPSHOT 5"]
    # Expected:     [8, 8]
    #
    # Trace:
    #   Start:          []
    #   INSERT 2 5:     [(2,5)]
    #   INSERT 2 3:     [(2,5),(2,3)]   (stable: 3 goes after 5 since ts equal)
    #   SNAPSHOT 2:     5+3 = 8        ✓
    #   INSERT 4 10:    [(2,5),(2,3),(4,10)]
    #   ROLLBACK 2:     removes t>2 → [(2,5),(2,3)]
    #   SNAPSHOT 5:     5+3 = 8        ✓
    # ------------------------------------------------------------------ #
    initial2: List[Tuple[int, int]] = []
    ops2 = ["INSERT 2 5", "INSERT 2 3", "SNAPSHOT 2",
            "INSERT 4 10", "ROLLBACK 2", "SNAPSHOT 5"]
    output2 = sol.process_operations(initial2, ops2)
    print("Example 2:")
    print(f"  Operations : {ops2}")
    print(f"  Output     : {output2}")
    print(f"  Expected   : [8, 8]")
    assert output2 == [8, 8], f"Example 2 FAILED: got {output2}"
    print("  PASSED ✓\n")

    # ------------------------------------------------------------------ #
    # Extra edge-case: empty list, only a SNAPSHOT
    # ------------------------------------------------------------------ #
    initial3: List[Tuple[int, int]] = []
    ops3 = ["SNAPSHOT 100"]
    output3 = sol.process_operations(initial3, ops3)
    print("Edge case — empty list SNAPSHOT:")
    print(f"  Output  : {output3}")
    print(f"  Expected: [0]")
    assert output3 == [0], f"Edge case FAILED: got {output3}"
    print("  PASSED ✓\n")

    # ------------------------------------------------------------------ #
    # Extra edge-case: ROLLBACK to 0 clears everything
    # ------------------------------------------------------------------ #
    initial4 = [(1, 5), (2, 10)]
    ops4 = ["ROLLBACK 0", "SNAPSHOT 999"]
    output4 = sol.process_operations(initial4, ops4)
    print("Edge case — ROLLBACK 0 clears all nodes:")
    print(f"  Output  : {output4}")
    print(f"  Expected: [0]")
    assert output4 == [0], f"Edge case FAILED: got {output4}"
    print("  PASSED ✓\n")

    print("All tests passed!")