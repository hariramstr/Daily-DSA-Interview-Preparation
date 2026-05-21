```python
"""
Title: Inbox Message Thread Flattener
Difficulty: Medium
Topic: Stacks and Queues

Problem Description:
You are building an email client. Messages arrive as a stream of events. Each event is either:
- `SEND id parent_id`: A new message with a unique integer `id` is sent as a reply to
  message `parent_id`. If `parent_id` is 0, it starts a new top-level thread.
- `READ depth`: Return all message IDs at exactly `depth` levels deep in the current
  thread tree, in the order they were first received (left to right, BFS order).
  Depth 0 means only top-level messages.

Process all events in order and return a list of results — one list of IDs per READ event
(in the order the READ events appear).

A message's depth is defined as the number of ancestors it has. Top-level messages
(parent_id = 0) have depth 0.

Constraints:
- 1 <= number of events <= 10^4
- 1 <= id <= 10^5, all IDs are unique
- parent_id is either 0 or the ID of a previously sent message
- 0 <= depth <= 500
- A READ event always comes after at least one SEND event
"""

from collections import deque, defaultdict
from typing import List, Dict, Tuple


class Solution:
    """
    Solves the Inbox Message Thread Flattener problem.

    Core idea:
    - Maintain a tree of messages where each node stores its children (in insertion order).
    - For each READ event, perform a BFS from the root(s) and collect all nodes at the
      requested depth.
    - We use a virtual root (id=0) so that all top-level messages are children of node 0,
      which simplifies the BFS traversal.
    """

    def process_events(self, events: List[str]) -> List[List[int]]:
        """
        Process a list of SEND/READ events and return results for each READ event.

        Args:
            events: A list of strings. Each string is either:
                    "SEND id parent_id" or "READ depth"

        Returns:
            A list of lists. Each inner list contains the message IDs at the requested
            depth for the corresponding READ event, in BFS (insertion) order.

        Time Complexity:
            - SEND: O(1) per event (dictionary insertion and list append).
            - READ: O(N) per event where N is the total number of messages sent so far,
              because BFS visits every node in the worst case.
            - Overall: O(E * N) where E is the number of READ events and N is the number
              of messages.

        Space Complexity:
            O(N) for storing the tree (children dictionary and depth dictionary),
            where N is the total number of messages.
        """

        # -----------------------------------------------------------------------
        # DATA STRUCTURES SETUP
        # -----------------------------------------------------------------------
        # children[node_id] = list of child IDs in the order they were received.
        # We use defaultdict(list) so we never need to manually initialise a key.
        # Node 0 is our virtual root; all top-level messages are its children.
        children: Dict[int, List[int]] = defaultdict(list)

        # depth_map[node_id] = depth of that node in the tree.
        # The virtual root (0) has depth -1 so that its children (top-level messages)
        # end up at depth 0.
        depth_map: Dict[int, int] = {}
        depth_map[0] = -1  # virtual root sentinel

        # results accumulates one list per READ event.
        results: List[List[int]] = []

        # -----------------------------------------------------------------------
        # EVENT PROCESSING LOOP
        # -----------------------------------------------------------------------
        for event in events:
            # Split the event string into tokens.
            tokens = event.split()
            event_type = tokens[0]

            if event_type == "SEND":
                # -----------------------------------------------------------
                # SEND EVENT
                # -----------------------------------------------------------
                # Parse the message id and its parent id from the tokens.
                msg_id = int(tokens[1])
                parent_id = int(tokens[2])

                # Register this message as a child of its parent.
                # Because we use a list, insertion order is preserved automatically.
                children[parent_id].append(msg_id)

                # Calculate and store the depth of this new message.
                # depth = parent's depth + 1.
                # Since depth_map[0] = -1, a top-level message (parent=0) gets depth 0.
                depth_map[msg_id] = depth_map[parent_id] + 1

            elif event_type == "READ":
                # -----------------------------------------------------------
                # READ EVENT
                # -----------------------------------------------------------
                # Parse the target depth we need to collect messages from.
                target_depth = int(tokens[1])

                # We perform a BFS starting from the virtual root (node 0).
                # BFS naturally visits nodes level by level, which matches the
                # "left to right, BFS order" requirement.

                # The BFS queue holds (node_id, current_depth) tuples.
                # We start from the virtual root at depth -1.
                bfs_queue: deque = deque()
                bfs_queue.append((0, -1))  # (node_id, node_depth)

                # This list will collect all message IDs found at target_depth.
                found_at_depth: List[int] = []

                while bfs_queue:
                    node_id, node_depth = bfs_queue.popleft()

                    # If we have already reached the target depth, we only need
                    # to collect this node (if it's not the virtual root) and
                    # we do NOT need to go deeper — so we skip adding children.
                    if node_depth == target_depth:
                        # node 0 is the virtual root, never a real message.
                        if node_id != 0:
                            found_at_depth.append(node_id)
                        # Do not enqueue children; we've found what we need at
                        # this level and going deeper would exceed target_depth.
                        continue

                    # If we haven't reached target_depth yet, enqueue all children
                    # of the current node so BFS can explore the next level.
                    # Children are already stored in insertion order, so BFS order
                    # is preserved automatically.
                    for child_id in children[node_id]:
                        bfs_queue.append((child_id, node_depth + 1))

                # Append the collected IDs for this READ event to results.
                results.append(found_at_depth)

        return results


# ---------------------------------------------------------------------------
# MAIN BLOCK — demonstrates the solution with the provided examples
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # -----------------------------------------------------------------------
    # Tree structure after all SENDs:
    #
    #        (virtual root 0)
    #        /              \
    #       1               2          <- depth 0
    #      / \
    #     3   4                        <- depth 1
    #     |
    #     5                            <- depth 2
    #
    # READ 0 -> [1, 2]
    # READ 1 -> [3, 4]
    # READ 2 -> [5]

    events_1 = [
        "SEND 1 0",
        "SEND 2 0",
        "SEND 3 1",
        "SEND 4 1",
        "SEND 5 3",
        "READ 0",
        "READ 1",
        "READ 2",
    ]

    result_1 = solution.process_events(events_1)
    print("Example 1:")
    print(f"  Events : {events_1}")
    print(f"  Output : {result_1}")
    print(f"  Expected: [[1, 2], [3, 4], [5]]")
    assert result_1 == [[1, 2], [3, 4], [5]], f"Example 1 FAILED: got {result_1}"
    print("  PASSED\n")

    # -----------------------------------------------------------------------
    # Example 2
    # -----------------------------------------------------------------------
    # After SEND 10 0 and SEND 20 10:
    #   (root) -> 10 -> 20
    # READ 1 -> [20]   (only 20 is at depth 1)
    #
    # After SEND 30 10:
    #   (root) -> 10 -> 20
    #                -> 30
    # READ 1 -> [20, 30]  (both 20 and 30 are at depth 1)

    events_2 = [
        "SEND 10 0",
        "SEND 20 10",
        "READ 1",
        "SEND 30 10",
        "READ 1",
    ]

    result_2 = solution.process_events(events_2)
    print("Example 2:")
    print(f"  Events : {events_2}")
    print(f"  Output : {result_2}")
    print(f"  Expected: [[20], [20, 30]]")
    assert result_2 == [[20], [20, 30]], f"Example 2 FAILED: got {result_2}"
    print("  PASSED\n")

    # -----------------------------------------------------------------------
    # Additional edge-case: READ at a depth with no messages
    # -----------------------------------------------------------------------
    events_3 = [
        "SEND 1 0",
        "READ 5",   # No messages at depth 5 -> should return []
    ]

    result_3 = solution.process_events(events_3)
    print("Edge Case — READ at non-existent depth:")
    print(f"  Events : {events_3}")
    print(f"  Output : {result_3}")
    print(f"  Expected: [[]]")
    assert result_3 == [[]], f"Edge case FAILED: got {result_3}"
    print("  PASSED\n")

    print("All tests passed!")
```