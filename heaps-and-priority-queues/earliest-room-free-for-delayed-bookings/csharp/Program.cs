/*
Title: Earliest Room Free for Delayed Bookings

Problem Description:
You are managing a set of meeting rooms in an office. There are n identical rooms numbered from 0 to n - 1.
You are given a list of meeting requests, where each request is represented as [start, duration].
A request wants to begin at time start and would occupy a room for exactly duration minutes.

If at least one room is free at time start, the meeting is assigned immediately to the free room with the smallest room number.
If all rooms are busy, the meeting must wait until some room becomes available.
In that case, the meeting starts at the earliest time any room becomes free, and among all rooms that become free at that earliest time,
it uses the one with the smallest room number. The meeting still lasts for its full duration after it actually starts.

Return an array answer of length equal to the number of requests, where answer[i] is the actual start time of the i-th meeting request
after applying the scheduling rules above.

The requests should be processed in increasing order of their original start times.
If multiple requests have the same start, process them in their original input order.

Constraints:
- 1 <= n <= 10^5
- 1 <= meetings.length <= 2 * 10^5
- meetings[i].length == 2
- 0 <= start <= 10^9
- 1 <= duration <= 10^9
- The answer values may exceed 10^9, so use 64-bit integers.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the meetings by (start time, original index for tie-breaking): O(m log m)
    - Each meeting is inserted/removed from heaps a constant number of times: O(m log n)
    - Total: O(m log m + m log n)

    Space Complexity:
    - O(m) for the sorted meeting metadata and answer array
    - O(n) for the heaps
    - Total: O(m + n)

    Where:
    - n = number of rooms
    - m = number of meetings
    */
    public long[] EarliestRoomFreeForDelayedBookings(int n, int[][] meetings)
    {
        int m = meetings.Length;

        // This array will store the final actual start time for each original meeting.
        // We must return answers in original input order, not sorted order.
        long[] answer = new long[m];

        // We create a separate array of meeting metadata so we can sort safely while still
        // remembering each meeting's original position.
        //
        // Each item stores:
        // - Start: requested start time
        // - Duration: how long the meeting lasts
        // - Index: original input index
        var orderedMeetings = new MeetingInfo[m];
        for (int i = 0; i < m; i++)
        {
            orderedMeetings[i] = new MeetingInfo(
                start: meetings[i][0],
                duration: meetings[i][1],
                index: i
            );
        }

        // The problem says:
        // 1) Process meetings in increasing order of original start time.
        // 2) If multiple meetings have the same start time, process them in original input order.
        //
        // Sorting by (start, index) achieves exactly that.
        Array.Sort(orderedMeetings, (a, b) =>
        {
            int byStart = a.Start.CompareTo(b.Start);
            if (byStart != 0) return byStart;
            return a.Index.CompareTo(b.Index);
        });

        // -----------------------------
        // Data structure #1: freeRooms
        // -----------------------------
        // This min-heap stores currently available room numbers.
        // Why a min-heap?
        // Because whenever multiple rooms are free, we must choose the smallest room number.
        //
        // Priority = room number
        // Element  = room number
        var freeRooms = new PriorityQueue<int, int>();

        // Initially, all rooms are free, so we add every room number.
        for (int room = 0; room < n; room++)
        {
            freeRooms.Enqueue(room, room);
        }

        // -----------------------------
        // Data structure #2: busyRooms
        // -----------------------------
        // This min-heap stores rooms that are currently occupied.
        //
        // We need to know:
        // - which room becomes free the earliest
        // - if multiple rooms become free at the same earliest time, choose the smallest room number
        //
        // So the heap priority is:
        //   (endTime, roomNumber)
        //
        // The element stores the same information as a BusyRoom record.
        var busyRooms = new PriorityQueue<BusyRoom, BusyPriority>();

        // Process each meeting in the required order.
        foreach (var meeting in orderedMeetings)
        {
            long requestedStart = meeting.Start;
            long duration = meeting.Duration;

            // ------------------------------------------------------------
            // Step 1: Release every room that has already finished by the
            //         time this meeting wants to start.
            // ------------------------------------------------------------
            //
            // Why is this necessary?
            // Because if a room's meeting ended at or before requestedStart,
            // that room is now free and should be moved from busyRooms to freeRooms.
            //
            // We keep doing this while the earliest finishing busy room has
            // endTime <= requestedStart.
            while (busyRooms.Count > 0 && busyRooms.Peek().EndTime <= requestedStart)
            {
                BusyRoom finished = busyRooms.Dequeue();

                // That room is now available again, so put it into the free-room heap.
                freeRooms.Enqueue(finished.RoomNumber, finished.RoomNumber);
            }

            // ------------------------------------------------------------
            // Step 2: If at least one room is free right now, assign the
            //         meeting immediately to the smallest-numbered free room.
            // ------------------------------------------------------------
            if (freeRooms.Count > 0)
            {
                // Smallest room number among all currently free rooms.
                int chosenRoom = freeRooms.Dequeue();

                // Since the room is free at requestedStart, the meeting starts immediately.
                long actualStart = requestedStart;
                long endTime = actualStart + duration;

                // Save the answer in the original meeting position.
                answer[meeting.Index] = actualStart;

                // Mark the room as busy until endTime.
                busyRooms.Enqueue(
                    new BusyRoom(endTime, chosenRoom),
                    new BusyPriority(endTime, chosenRoom)
                );
            }
            else
            {
                // ------------------------------------------------------------
                // Step 3: No room is free at requestedStart.
                //         The meeting must wait.
                // ------------------------------------------------------------
                //
                // According to the rules:
                // - It starts at the earliest time any room becomes free.
                // - If multiple rooms become free at that same earliest time,
                //   choose the smallest room number among them.
                //
                // Because busyRooms is ordered by (endTime, roomNumber),
                // the top of the heap is exactly the room we need.
                BusyRoom earliest = busyRooms.Dequeue();

                long actualStart = earliest.EndTime;
                long endTime = actualStart + duration;

                // Record the delayed actual start time.
                answer[meeting.Index] = actualStart;

                // The same room becomes busy again immediately after this delayed meeting starts.
                busyRooms.Enqueue(
                    new BusyRoom(endTime, earliest.RoomNumber),
                    new BusyPriority(endTime, earliest.RoomNumber)
                );
            }
        }

        return answer;
    }

    private readonly record struct MeetingInfo(long Start, long Duration, int Index);

    private readonly record struct BusyRoom(long EndTime, int RoomNumber);

    private readonly record struct BusyPriority(long EndTime, int RoomNumber) : IComparable<BusyPriority>
    {
        public int CompareTo(BusyPriority other)
        {
            int byEnd = EndTime.CompareTo(other.EndTime);
            if (byEnd != 0) return byEnd;
            return RoomNumber.CompareTo(other.RoomNumber);
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int n1 = 2;
int[][] meetings1 =
{
    new[] { 1, 4 },
    new[] { 2, 3 },
    new[] { 3, 2 },
    new[] { 10, 1 }
};

long[] result1 = solution.EarliestRoomFreeForDelayedBookings(n1, meetings1);
Console.WriteLine("Example 1 Output: [" + string.Join(",", result1) + "]");
// Expected: [1,2,5,10]

// Example 2
int n2 = 1;
int[][] meetings2 =
{
    new[] { 0, 5 },
    new[] { 1, 2 },
    new[] { 1, 4 },
    new[] { 8, 3 }
};

long[] result2 = solution.EarliestRoomFreeForDelayedBookings(n2, meetings2);
Console.WriteLine("Example 2 Output: [" + string.Join(",", result2) + "]");
// Expected: [0,5,7,11]

// Additional small demo
int n3 = 3;
int[][] meetings3 =
{
    new[] { 0, 10 },
    new[] { 0, 5 },
    new[] { 0, 7 },
    new[] { 1, 2 },
    new[] { 5, 3 }
};

long[] result3 = solution.EarliestRoomFreeForDelayedBookings(n3, meetings3);
Console.WriteLine("Example 3 Output: [" + string.Join(",", result3) + "]");