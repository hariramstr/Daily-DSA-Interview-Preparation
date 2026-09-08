"""
Title: Earliest Room Free for Delayed Bookings

Problem Description:
You are managing a set of meeting rooms in an office. There are n identical rooms
numbered from 0 to n - 1. You are given a list of meeting requests, where each
request is represented as [start, duration]. A request wants to begin at time start
and would occupy a room for exactly duration minutes.

If at least one room is free at time start, the meeting is assigned immediately to
the free room with the smallest room number. If all rooms are busy, the meeting must
wait until some room becomes available. In that case, the meeting starts at the
earliest time any room becomes free, and among all rooms that become free at that
earliest time, it uses the one with the smallest room number. The meeting still lasts
for its full duration after it actually starts.

Return an array answer of length equal to the number of requests, where answer[i] is
the actual start time of the i-th meeting request after applying the scheduling rules
above.

The requests should be processed in increasing order of their original start times.
If multiple requests have the same start, process them in their original input order.
"""

from heapq import heapify, heappop, heappush
from typing import List, Tuple


class Solution:
    def earliestRoomFree(self, n: int, meetings: List[List[int]]) -> List[int]:
        """
        Compute the actual start time for each meeting request after scheduling.

        The algorithm processes meetings in the required order:
        1. Increasing requested start time
        2. Original input order for ties

        It uses two heaps:
        - A min-heap of currently free room numbers
        - A min-heap of busy rooms storing (end_time, room_number)

        For each meeting:
        - First, release every room whose meeting has already finished by the
          requested start time.
        - If any room is free, assign the smallest-numbered free room immediately.
        - Otherwise, delay the meeting until the earliest busy room becomes free.
          If multiple rooms become free at that same earliest time, the heap order
          ensures the smallest room number is chosen.

        Args:
            n: Number of rooms.
            meetings: List of [start, duration] meeting requests.

        Returns:
            A list where the i-th value is the actual start time of the i-th request.

        Time complexity:
            O((m + n) log n), where m is the number of meetings.

        Space complexity:
            O(n + m) due to heaps and the indexed/sorted meeting list.
        """
        # We must return answers in original input order, but meetings are processed
        # in sorted order by requested start time, with original order used to break ties.
        # To preserve that information, we build a new list containing:
        # (requested_start, original_index, duration)
        indexed_meetings: List[Tuple[int, int, int]] = [
            (start, index, duration)
            for index, (start, duration) in enumerate(meetings)
        ]

        # Python tuple sorting naturally sorts by first element, then second, etc.
        # That exactly matches the problem requirement:
        # - sort by start time
        # - if equal start time, keep original input order
        # Since original_index increases with input order, sorting by it breaks ties correctly.
        indexed_meetings.sort()

        # This will store the final actual start time for each original meeting index.
        answer: List[int] = [0] * len(meetings)

        # free_rooms:
        # A min-heap containing room numbers that are currently available.
        # We initialize it with all room numbers 0..n-1.
        # Because it is a min-heap, popping gives the smallest available room number,
        # which matches the scheduling rule.
        free_rooms: List[int] = list(range(n))
        heapify(free_rooms)

        # busy_rooms:
        # A min-heap of tuples (end_time, room_number).
        #
        # Why this structure?
        # - We need to know which room becomes available the earliest.
        # - If multiple rooms become free at the same earliest time, we need the
        #   smallest room number among them.
        #
        # Python compares tuples lexicographically, so (end_time, room_number)
        # automatically gives:
        # 1. smallest end_time first
        # 2. for ties, smallest room_number first
        #
        # That is exactly what the problem asks for.
        busy_rooms: List[Tuple[int, int]] = []

        # Process each meeting request in the required order.
        for requested_start, original_index, duration in indexed_meetings:
            # Before scheduling the current meeting, we should release every room
            # whose current meeting has already ended by requested_start.
            #
            # Why "while" and not "if"?
            # Because multiple rooms may have become free before this meeting arrives,
            # and all of them should be moved back into the free room heap.
            while busy_rooms and busy_rooms[0][0] <= requested_start:
                finished_time, room_number = heappop(busy_rooms)
                heappush(free_rooms, room_number)

            # Case 1:
            # At least one room is free at the requested start time.
            if free_rooms:
                # Choose the smallest-numbered free room.
                room_number = heappop(free_rooms)

                # The meeting starts immediately at its requested start time.
                actual_start = requested_start
                actual_end = actual_start + duration

                # Record the answer for this meeting in original input order.
                answer[original_index] = actual_start

                # Mark this room as busy until actual_end.
                heappush(busy_rooms, (actual_end, room_number))

            else:
                # Case 2:
                # No room is free at requested_start, so the meeting must wait.
                #
                # We take the room that becomes free the earliest.
                # Because busy_rooms stores (end_time, room_number), this also
                # automatically chooses the smallest room number if several rooms
                # become free at the same earliest time.
                earliest_end, room_number = heappop(busy_rooms)

                # The meeting starts exactly when that room becomes free.
                actual_start = earliest_end
                actual_end = actual_start + duration

                # Store the delayed actual start time.
                answer[original_index] = actual_start

                # Put the room back into the busy heap with its new end time.
                heappush(busy_rooms, (actual_end, room_number))

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 2
    meetings1 = [[1, 4], [2, 3], [3, 2], [10, 1]]
    result1 = solution.earliestRoomFree(n1, meetings1)
    print("Example 1 result:", result1)
    print("Expected:         [1, 2, 5, 10]")
    print()

    # Example 2
    n2 = 1
    meetings2 = [[0, 5], [1, 2], [1, 4], [8, 3]]
    result2 = solution.earliestRoomFree(n2, meetings2)
    print("Example 2 result:", result2)
    print("Expected:         [0, 5, 7, 11]")
    print()

    # Additional quick sanity check
    n3 = 3
    meetings3 = [[0, 2], [0, 1], [0, 3], [1, 2], [2, 1]]
    result3 = solution.earliestRoomFree(n3, meetings3)
    print("Additional test result:", result3)