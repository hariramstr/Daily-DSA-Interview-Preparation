```python
"""
Title: Meeting Rooms with Earliest Availability
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
You are managing a building with `n` meeting rooms. Each room becomes available at time 0 initially.
You are given a list of meeting requests, where each request is represented as `[duration, priority]`.
Meetings must be assigned to the room that becomes available the earliest. If two rooms become
available at the same time, assign the meeting to the room with the smallest index.

After each meeting ends, the room becomes available again at `start_time + duration`. Your goal is
to process all meetings in the order they are given and return the index of the room that hosted
the most meetings. If there is a tie, return the room with the smallest index.

Constraints:
- 1 <= n <= 100
- 1 <= meetings.length <= 10^5
- meetings[i] = [duration] where 1 <= duration <= 10^4
"""

import heapq
from typing import List


class Solution:
    def most_booked(self, n: int, meetings: List[List[int]]) -> int:
        """
        Find the room that hosts the most meetings using a greedy + heap approach.

        Each meeting is assigned to the room that becomes available the earliest.
        If multiple rooms are available at the same time, the one with the smallest
        index is chosen.

        Args:
            n (int): Number of meeting rooms (indexed 0 to n-1).
            meetings (List[List[int]]): List of meetings, each as [duration].

        Returns:
            int: Index of the room that hosted the most meetings.
                 In case of a tie, returns the smallest room index.

        Time Complexity:
            O(M * log N) where M = number of meetings, N = number of rooms.
            Each meeting requires at most one heap push and one heap pop,
            both of which are O(log N).

        Space Complexity:
            O(N) for the two heaps and the count array.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Initialize data structures
        # -----------------------------------------------------------------------

        # `count[i]` tracks how many meetings room i has hosted.
        # We'll use this at the end to find the winner.
        count: List[int] = [0] * n

        # `available` is a min-heap of room indices that are currently free.
        # We use a min-heap so that the room with the smallest index is always
        # at the top when multiple rooms are free at the same time.
        # Initially, ALL rooms are free (available at time 0).
        # Each element: room_index (int)
        available: List[int] = list(range(n))
        heapq.heapify(available)  # Turn the list into a valid min-heap in O(N)

        # `busy` is a min-heap of rooms that are currently occupied.
        # Each element is a tuple: (end_time, room_index)
        # The room with the earliest end_time is at the top.
        # If two rooms have the same end_time, the one with the smaller index
        # comes first (Python tuples compare element by element).
        busy: List[tuple] = []

        # -----------------------------------------------------------------------
        # STEP 2: Process each meeting in order
        # -----------------------------------------------------------------------

        # We process meetings one by one. For each meeting, we need to:
        #   a) Free up any rooms whose meetings have ended.
        #   b) Assign the meeting to the earliest-available room.

        # We use a virtual "current time" concept:
        # Since meetings are processed sequentially (not by start time),
        # we need to track when each meeting actually starts.
        # The start time of a meeting is the time the assigned room becomes free.

        # We'll track the current "scheduling time" as we go.
        # For the first n meetings, they all start at time 0.
        # After that, we must wait for the earliest room to free up.

        # We use a simple counter to know which "slot" we're filling.
        # The key insight: meetings are assigned in order, so meeting i
        # is assigned as soon as a room is free (possibly waiting for one).

        # We don't have explicit start times in the input, so we derive them:
        # - If a room is available now (in `available` heap), start_time = 0
        #   for the first batch, or we just pick the room immediately.
        # - If no room is available, we must wait until the earliest busy room
        #   finishes, and that finish time becomes the new start time.

        # Let's track the "current time" for scheduling purposes.
        # This represents when the current meeting can start at the earliest.
        # We process meetings sequentially, so we don't advance time arbitrarily;
        # instead, each meeting starts as soon as a room is free.

        # We'll use a simple approach:
        # - Maintain a "current_time" that advances as needed.
        # - For each meeting, check if any room in `busy` has freed up
        #   by the time we need to schedule this meeting.

        # Since meetings are processed in order and each one needs a room ASAP,
        # the start time of meeting i is:
        #   max(end_time_of_earliest_free_room, 0) if no room is currently free.
        # If a room IS currently free, start_time is effectively "now" (or 0
        # for the very first meetings).

        # We'll track the "current scheduling time" as the time at which
        # we're trying to assign the current meeting.
        # For the first meeting, it's time 0.
        # For subsequent meetings, it's the time the previous meeting was assigned
        # (since we assign meetings one by one, back-to-back in scheduling order).

        # Actually, the simplest model:
        # We assign meetings one by one. The "current time" for scheduling
        # is 0 for the first meeting, and for each subsequent meeting,
        # we try to assign it immediately. If a room is free, we assign it now.
        # If not, we wait until the earliest room frees up.

        # Let's use `current_time` to track when we're scheduling.
        # We start at time 0 and never go backward.
        current_time: int = 0

        for i, meeting in enumerate(meetings):
            # Extract the duration of this meeting.
            duration = meeting[0]

            # -------------------------------------------------------------------
            # STEP 2a: Release rooms that have finished by current_time
            # -------------------------------------------------------------------
            # Check the `busy` heap: any room with end_time <= current_time
            # is now free and should be moved to `available`.
            # We keep popping from `busy` as long as the earliest end_time
            # is <= current_time.
            while busy and busy[0][0] <= current_time:
                end_time, room_idx = heapq.heappop(busy)
                # This room is now free — add it back to the available heap.
                heapq.heappush(available, room_idx)

            # -------------------------------------------------------------------
            # STEP 2b: Assign the meeting to a room
            # -------------------------------------------------------------------
            if available:
                # There is at least one free room right now.
                # Pick the one with the smallest index (min-heap gives us this).
                room_idx = heapq.heappop(available)

                # This meeting starts at current_time and ends at:
                end_time = current_time + duration

                # Mark this room as busy until end_time.
                heapq.heappush(busy, (end_time, room_idx))

                # Record that this room hosted one more meeting.
                count[room_idx] += 1

                # Note: current_time does NOT advance here because the next
                # meeting can also start at current_time if another room is free.
                # We only advance current_time when we MUST wait for a room.

            else:
                # No room is currently free. We must wait for the earliest
                # room to finish. The earliest finish time is at the top of
                # the `busy` heap.
                end_time_of_earliest, room_idx = heapq.heappop(busy)

                # Advance current_time to when this room becomes free.
                current_time = end_time_of_earliest

                # This meeting starts at current_time (the room just freed up).
                new_end_time = current_time + duration

                # Push the room back as busy with the new end time.
                heapq.heappush(busy, (new_end_time, room_idx))

                # Record the meeting for this room.
                count[room_idx] += 1

                # IMPORTANT: After waiting, other rooms might also have freed up
                # by current_time. We should release them for future meetings.
                # We'll handle this at the start of the next iteration's Step 2a.

        # -----------------------------------------------------------------------
        # STEP 3: Find the room with the most meetings
        # -----------------------------------------------------------------------
        # We want the room with the maximum count.
        # If there's a tie, we want the smallest index.
        # Python's max() with a key works here, but we need to handle ties.
        # We can iterate and track manually for clarity.

        best_room: int = 0
        best_count: int = count[0]

        for room_idx in range(1, n):
            # Update best if this room has strictly more meetings.
            # (We don't update on equal count because we want the smallest index,
            # and we're iterating in increasing order of index.)
            if count[room_idx] > best_count:
                best_count = count[room_idx]
                best_room = room_idx

        return best_room


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
#
# Example 1: n=2, meetings=[[1],[2],[3],[1],[2]]
# Rooms: 0, 1
# available = [0, 1] (min-heap), busy = [], count = [0, 0], current_time = 0
#
# Meeting 0: duration=1
#   Step 2a: busy is empty, nothing to release.
#   Step 2b: available=[0,1], pop room 0.
#     end_time = 0 + 1 = 1. busy=[(1,0)]. count=[1,0].
#
# Meeting 1: duration=2
#   Step 2a: busy[0]=(1,0), 1 > 0 (current_time=0), don't release.
#   Step 2b: available=[1], pop room 1.
#     end_time = 0 + 2 = 2. busy=[(1,0),(2,1)]. count=[1,1].
#
# Meeting 2: duration=3
#   Step 2a: busy[0]=(1,0), 1 > 0, don't release.
#   Step 2b: available is empty. Pop (1,0) from busy.
#     current_time = 1. new_end_time = 1+3=4. busy=[(2,1),(4,0)]. count=[2,1].
#
# Meeting 3: duration=1
#   Step 2a: busy[0]=(2,1), 2 > 1 (current_time=1), don't release.
#   Step 2b: available is empty. Pop (2,1) from busy.
#     current_time = 2. new_end_time = 2+1=3. busy=[(3,1),(4,0)]. count=[2,2].
#
# Meeting 4: duration=2
#   Step 2a: busy[0]=(3,1), 3 > 2 (current_time=2), don't release.
#   Step 2b: available is empty. Pop (3,1) from busy.
#     current_time = 3. new_end_time = 3+2=5. busy=[(4,0),(5,1)]. count=[2,3].
#
# Wait, that gives count=[2,3], so room 1 wins. But expected output is 0!
#
# Let me re-read the problem...
# "Room 0 hosts meetings at times 0, 1, 4 (3 meetings). Room 1 hosts meetings
#  at times 0, 2 (2 meetings). Room 0 wins."
#
# The explanation says meetings start at specific times (0, 1, 4 for room 0).
# This suggests meetings have fixed start times (0, 1, 2, 3, 4, ...) or
# they start at time 0, 1, 2, 3, 4 respectively (one per time unit)?
#
# Wait — re-reading: "meetings = [[1],[2],[3],[1],[2]]"
# Maybe the meetings arrive at times 0, 1, 2, 3, 4 (one per time step)?
# Or maybe they all arrive at time 0 and we just assign them in order?
#
# From the explanation:
# - Meeting 0 (dur=1): Room 0 at time 0, ends at 1.
# - Meeting 1 (dur=2): Room 1 at time 0, ends at 2.
# - Meeting 2 (dur=3): Room 0 at time 1 (earliest available), ends at 4.
# - Meeting 3 (dur=1): Room 1 at time 2 (next available), ends at 3.
# - Meeting 4 (dur=2): Room 0 at time 4 (next available), ends at 6.
#
# So Room 0: meetings 0, 2, 4 → 3 meetings. Room 1: meetings 1, 3 → 2 meetings.
# Output: 0. ✓
#
# The issue with my approach: when no room is free, I advance current_time
# to the earliest free room. But I should NOT advance current_time globally —
# each meeting should be assigned to the earliest available room, but
# current_time should only advance when needed for the NEXT meeting.
#
# Actually, looking more carefully: the meetings don't have fixed arrival times.
# They're just processed in order. Each meeting is assigned to the room that
# becomes free the soonest. The "time" is just the room's availability time.
#
# The key insight I was missing: current_time should NOT persist between meetings
# in the way I implemented. Let me reconsider.
#
# Actually, the issue is: after assigning meeting 2 to room 0 (which freed at t=1),
# current_time becomes 1. Then for meeting 3, room 1 frees at t=2 > current_time=1,
# so we wait and current_time becomes 2. That's correct.
# Then for meeting 4, current_time=2, room 0 frees at t=4 > 2, room 1 frees at t=3 > 2.
# So we wait for room 1 (t=3). But the explanation says meeting 4 goes to room 0 at t=4!
#
# Hmm. Let me re-read the explanation again.
# "Room 0 hosts meetings at times 0, 1, 4"
# "Room 1 hosts meetings at times 0, 2"
#
# Meeting assignments:
# Meeting 0 (dur=1) → Room 0 at t=0, ends t=1
# Meeting 1 (dur=2) → Room 1 at t=0, ends t=2
# Meeting 2 (dur=3) → Room 0 at t=1 (earliest free), ends t=4
# Meeting 3 (dur=1) → Room 1 at t=2 (next earliest free), ends t=3
# Meeting 4 (dur=2) → Room 0 at t=4 (next earliest free), ends t=6
#
# After meeting 3, busy = [(3,1), (4,0)]. current_time should be 2.
# For meeting 4: current_time=2, busy[0]=(3,1). No room available at t=2.
# Wait for t=3, room 1 frees. Assign meeting 4 to room 1 at t=3.
# But explanation says meeting 4 goes to room 0 at t=4!
#
# There's a contradiction. Let me re-examine.
#
# Oh wait — maybe the problem means meetings are processed one at a time,
# and the "current time" for each meeting is determined by when the previous
# meeting was ASSIGNED (not when the previous meeting ENDED).
#
# Alternative