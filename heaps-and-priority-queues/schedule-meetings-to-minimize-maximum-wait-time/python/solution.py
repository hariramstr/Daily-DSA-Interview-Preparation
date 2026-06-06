```python
"""
Title: Schedule Meetings to Minimize Maximum Wait Time
Difficulty: Hard
Topic: Heaps and Priority Queues

Problem Description:
You are managing a conference center with k meeting rooms. You are given a list of n
meeting requests, where requests[i] = [arrivalTime, duration] represents a meeting that
arrives at arrivalTime and requires duration units of time to complete.

Meetings are assigned to rooms greedily in the order they arrive. When a meeting arrives,
it is assigned to the room that will become available the earliest (i.e., the room whose
current meeting ends the soonest). If multiple rooms are available at or before the
meeting's arrival time, assign the meeting to the room with the smallest index.

Each meeting's wait time is defined as max(0, roomAvailableTime - arrivalTime), meaning
how long a meeting must wait before it can start.

Return the maximum wait time experienced by any single meeting across all k rooms.

Constraints:
- 1 <= k <= 100
- 1 <= n <= 10^5
- 0 <= arrivalTime <= 10^9
- 1 <= duration <= 10^4
- Meetings are given in non-decreasing order of arrivalTime.
"""

import heapq
from typing import List


class Solution:
    def maxWaitTime(self, k: int, requests: List[List[int]]) -> int:
        """
        Find the maximum wait time experienced by any meeting across all k rooms.

        The algorithm uses two heaps:
        1. A min-heap of available rooms (sorted by room index) to quickly find
           the smallest-indexed free room.
        2. A min-heap of busy rooms (sorted by end time, then room index) to
           quickly find which rooms become free and when.

        Args:
            k: Number of meeting rooms available.
            requests: List of [arrivalTime, duration] pairs in non-decreasing
                      order of arrivalTime.

        Returns:
            The maximum wait time experienced by any single meeting.

        Time Complexity: O(n log k) where n is the number of meetings and k is
                         the number of rooms. Each meeting involves at most one
                         heap push and one heap pop, each O(log k).
        Space Complexity: O(k) for the two heaps storing room information.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Initialize the two heaps
        # -----------------------------------------------------------------------
        # available_rooms: a min-heap of room indices that are currently free.
        # We store just the room index (an integer). Python's heapq is a min-heap,
        # so the smallest index will always be at the top — exactly what we need
        # when multiple rooms are free at the same time.
        available_rooms: List[int] = list(range(k))
        heapq.heapify(available_rooms)  # O(k) to build the heap

        # busy_rooms: a min-heap of (endTime, roomIndex) tuples for rooms that
        # are currently occupied. The room with the earliest end time sits at
        # the top, letting us efficiently find which room frees up next.
        busy_rooms: List[tuple] = []

        # Track the maximum wait time seen so far across all meetings.
        max_wait: int = 0

        # -----------------------------------------------------------------------
        # STEP 2: Process each meeting request in arrival order
        # -----------------------------------------------------------------------
        for arrival, duration in requests:
            # -------------------------------------------------------------------
            # STEP 2a: Free up any rooms that have finished by the time this
            #          meeting arrives.
            # -------------------------------------------------------------------
            # A room is "free" if its end time <= arrival time of the current
            # meeting (the meeting can start immediately without waiting).
            # We keep popping from busy_rooms as long as the earliest-ending
            # busy room finishes at or before the current arrival time.
            while busy_rooms and busy_rooms[0][0] <= arrival:
                end_time, room_idx = heapq.heappop(busy_rooms)
                # This room is now free; add it back to available_rooms.
                heapq.heappush(available_rooms, room_idx)

            # -------------------------------------------------------------------
            # STEP 2b: Decide which room to assign this meeting to.
            # -------------------------------------------------------------------
            if available_rooms:
                # Case A: At least one room is free right now (end time <= arrival).
                # Per the problem rules, we pick the room with the SMALLEST index
                # among all currently free rooms. Since available_rooms is a
                # min-heap of room indices, heappop gives us the smallest index.
                room_idx = heapq.heappop(available_rooms)

                # The meeting starts immediately — no waiting.
                wait = 0
                start_time = arrival
                end_time = start_time + duration

            else:
                # Case B: All rooms are busy. We must wait for the room that
                # becomes free the EARLIEST. The busy_rooms min-heap (sorted by
                # end time) gives us that room at index 0.
                # If two rooms end at the same time, we want the smaller index —
                # the heap tuple (endTime, roomIndex) handles this automatically
                # because Python compares tuples lexicographically.
                end_time_of_room, room_idx = heapq.heappop(busy_rooms)

                # The meeting must wait until the room is free.
                wait = end_time_of_room - arrival  # always > 0 in this branch
                start_time = end_time_of_room       # meeting starts when room frees
                end_time = start_time + duration

            # -------------------------------------------------------------------
            # STEP 2c: Update the maximum wait time.
            # -------------------------------------------------------------------
            max_wait = max(max_wait, wait)

            # -------------------------------------------------------------------
            # STEP 2d: Mark the chosen room as busy until end_time.
            # -------------------------------------------------------------------
            # Push (end_time, room_idx) onto busy_rooms so future meetings know
            # when this room becomes available again.
            heapq.heappush(busy_rooms, (end_time, room_idx))

        # -----------------------------------------------------------------------
        # STEP 3: Return the overall maximum wait time.
        # -----------------------------------------------------------------------
        return max_wait


# ---------------------------------------------------------------------------
# Trace-through verification
# ---------------------------------------------------------------------------
# Example 1: k=2, requests=[[0,5],[1,3],[2,4],[6,2]]
#
# Initial state:
#   available_rooms = [0, 1]  (min-heap)
#   busy_rooms      = []
#
# Meeting [0, 5] (arrival=0, duration=5):
#   - No busy rooms to free (busy_rooms empty).
#   - available_rooms not empty → pick room 0 (smallest index).
#   - wait=0, start=0, end=5.
#   - busy_rooms = [(5, 0)]
#   - available_rooms = [1]
#   - max_wait = 0
#
# Meeting [1, 3] (arrival=1, duration=3):
#   - busy_rooms[0] = (5,0); 5 > 1, so no rooms freed.
#   - available_rooms not empty → pick room 1.
#   - wait=0, start=1, end=4.
#   - busy_rooms = [(4,1),(5,0)]
#   - available_rooms = []
#   - max_wait = 0
#
# Meeting [2, 4] (arrival=2, duration=4):
#   - busy_rooms[0] = (4,1); 4 > 2, so no rooms freed.
#   - available_rooms empty → pick earliest busy room: (4,1), room 1.
#   - wait = 4-2 = 2, start=4, end=8.
#   - busy_rooms = [(5,0),(8,1)]
#   - max_wait = 2
#
# Meeting [6, 2] (arrival=6, duration=2):
#   - busy_rooms[0] = (5,0); 5 <= 6 → free room 0. available_rooms=[0]
#   - busy_rooms[0] = (8,1); 8 > 6 → stop.
#   - available_rooms not empty → pick room 0.
#   - wait=0, start=6, end=8.
#   - max_wait = 2
#
# Output: 2  ✓ (matches expected output)
#
# ---------------------------------------------------------------------------
# Example 2: k=1, requests=[[0,10],[2,5],[3,2]]
#
# Initial state:
#   available_rooms = [0]
#   busy_rooms      = []
#
# Meeting [0, 10] (arrival=0, duration=10):
#   - No busy rooms to free.
#   - available_rooms not empty → pick room 0.
#   - wait=0, start=0, end=10.
#   - busy_rooms = [(10,0)]
#   - max_wait = 0
#
# Meeting [2, 5] (arrival=2, duration=5):
#   - busy_rooms[0] = (10,0); 10 > 2 → no rooms freed.
#   - available_rooms empty → pick (10,0), room 0.
#   - wait = 10-2 = 8, start=10, end=15.
#   - busy_rooms = [(15,0)]
#   - max_wait = 8
#
# Meeting [3, 2] (arrival=3, duration=2):
#   - busy_rooms[0] = (15,0); 15 > 3 → no rooms freed.
#   - available_rooms empty → pick (15,0), room 0.
#   - wait = 15-3 = 12, start=15, end=17.
#   - busy_rooms = [(17,0)]
#   - max_wait = 12
#
# Output: 12  ✓ (matches expected output)
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    k1 = 2
    requests1 = [[0, 5], [1, 3], [2, 4], [6, 2]]
    result1 = solution.maxWaitTime(k1, requests1)
    print(f"Example 1:")
    print(f"  k = {k1}")
    print(f"  requests = {requests1}")
    print(f"  Output   = {result1}")   # Expected: 2
    print(f"  Expected = 2")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    k2 = 1
    requests2 = [[0, 10], [2, 5], [3, 2]]
    result2 = solution.maxWaitTime(k2, requests2)
    print(f"Example 2:")
    print(f"  k = {k2}")
    print(f"  requests = {requests2}")
    print(f"  Output   = {result2}")   # Expected: 12
    print(f"  Expected = 12")
    print()

    # ------------------------------------------------------------------
    # Additional edge case: single meeting, single room — no wait
    # ------------------------------------------------------------------
    k3 = 3
    requests3 = [[5, 2]]
    result3 = solution.maxWaitTime(k3, requests3)
    print(f"Edge Case (single meeting, 3 rooms):")
    print(f"  k = {k3}")
    print(f"  requests = {requests3}")
    print(f"  Output   = {result3}")   # Expected: 0
    print(f"  Expected = 0")
    print()

    # ------------------------------------------------------------------
    # Additional edge case: all meetings arrive at the same time
    # k=2, requests=[[0,3],[0,5],[0,2]]
    # Meeting 0 → Room 0, wait=0, ends at 3
    # Meeting 1 → Room 1, wait=0, ends at 5
    # Meeting 2 → Both rooms busy; earliest is Room 0 (ends at 3).
    #             wait = 3-0 = 3, ends at 5.
    # max_wait = 3
    # ------------------------------------------------------------------
    k4 = 2
    requests4 = [[0, 3], [0, 5], [0, 2]]
    result4 = solution.maxWaitTime(k4, requests4)
    print(f"Edge Case (all same arrival time):")
    print(f"  k = {k4}")
    print(f"  requests = {requests4}")
    print(f"  Output   = {result4}")   # Expected: 3
    print(f"  Expected = 3")
```