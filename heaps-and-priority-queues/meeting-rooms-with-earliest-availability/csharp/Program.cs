/*
 * Meeting Rooms with Earliest Availability
 * =========================================
 * Difficulty: Medium
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are managing a building with `n` meeting rooms. Each room becomes available
 * at time 0 initially. You are given a list of meeting requests, where each request
 * is represented as [duration]. Meetings must be assigned to the room that becomes
 * available the earliest. If two rooms become available at the same time, assign
 * the meeting to the room with the smallest index.
 *
 * After each meeting ends, the room becomes available again at start_time + duration.
 * Your goal is to process all meetings in the order they are given and return the
 * index of the room that hosted the most meetings. If there is a tie, return the
 * room with the smallest index.
 *
 * Example 1:
 *   Input:  n = 2, meetings = [[1],[2],[3],[1],[2]]
 *   Output: 0
 *   Explanation: Room 0 hosts meetings at times 0, 1, 4 (3 meetings).
 *                Room 1 hosts meetings at times 0, 2 (2 meetings). Room 0 wins.
 *
 * Example 2:
 *   Input:  n = 3, meetings = [[5],[2],[1],[3]]
 *   Output: 2
 *   Explanation: Room 0 gets duration 5 (available at 5).
 *                Room 1 gets duration 2 (available at 2).
 *                Room 2 gets duration 1 (available at 1).
 *                Fourth meeting (duration 3) goes to Room 2 (earliest available at 1).
 *                Room 2 ends at 1+3=4. Room 2 hosted 2 meetings total. Room 2 wins.
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class MeetingRoomsSolution
{
    // -------------------------------------------------------------------------
    // FindBusiestRoom
    //
    // Time Complexity:  O(M log N) where M = number of meetings, N = number of rooms
    //   - Each meeting triggers at most one heap push and one heap pop, each O(log N).
    //
    // Space Complexity: O(N) for the two priority queues (each holds at most N entries)
    //   plus O(N) for the meeting-count array.
    // -------------------------------------------------------------------------
    public int FindBusiestRoom(int n, int[][] meetings)
    {
        // ── Step 1: Create a count array to track how many meetings each room hosts.
        //    Index i corresponds to room i.  All start at 0.
        int[] meetingCount = new int[n];

        // ── Step 2: "Available rooms" min-heap, keyed by room index.
        //    We want the room with the SMALLEST index first when multiple rooms
        //    are free at the same time, so we compare by room index directly.
        //
        //    PriorityQueue<TElement, TPriority> is the .NET 6+ built-in min-heap.
        //    We store (roomIndex) as both element and priority so the smallest
        //    room index is always dequeued first.
        var availableRooms = new PriorityQueue<int, int>();

        // Populate with all rooms (0 … n-1).  All are free at time 0.
        for (int i = 0; i < n; i++)
        {
            availableRooms.Enqueue(i, i); // priority = room index (smaller = higher priority)
        }

        // ── Step 3: "Busy rooms" min-heap, keyed by the time the room becomes free.
        //    When a room finishes its current meeting, we move it back to availableRooms.
        //    We need the room that finishes EARLIEST to be dequeued first.
        //    Tie-break: if two rooms finish at the same time, the one with the smaller
        //    index should be freed first (so it can be re-assigned first).
        //
        //    We store (availableTime, roomIndex) as a tuple.
        //    Priority is a tuple (availableTime, roomIndex) — C# compares tuples
        //    lexicographically, so this gives us the correct tie-breaking for free.
        var busyRooms = new PriorityQueue<(long availableTime, int roomIndex), (long, int)>();

        // ── Step 4: Process each meeting in the order given.
        foreach (int[] meeting in meetings)
        {
            long duration = meeting[0]; // duration of this meeting

            // ── Step 4a: Before assigning this meeting, release every busy room
            //    whose availability time is ≤ the current "virtual" time.
            //
            //    IMPORTANT: In this problem meetings are NOT sorted by start time —
            //    they are processed in the order given, and each meeting starts as
            //    soon as a room is available (not at a fixed wall-clock start time).
            //    So the "current time" for this meeting is the earliest time any
            //    room is free.
            //
            //    Algorithm:
            //      • If there is at least one available room, the meeting starts NOW
            //        (conceptually at the moment the previous meeting was assigned,
            //         but we don't need an explicit global clock — we just pick the
            //         earliest free room).
            //      • If NO room is available, the meeting must wait until the
            //        earliest busy room finishes.  We release that room and use it.

            if (availableRooms.Count == 0)
            {
                // No room is free right now.
                // Dequeue the room that becomes free the soonest.
                // Its (availableTime, roomIndex) is the minimum in busyRooms.
                var (freeTime, roomIdx) = busyRooms.Dequeue();

                // This room is now free.  Put it back in the available pool.
                availableRooms.Enqueue(roomIdx, roomIdx);

                // Also release any OTHER rooms that become free at the SAME time,
                // because they should also be considered available for future meetings.
                // (We peek and drain all rooms with the same freeTime.)
                while (busyRooms.Count > 0 && busyRooms.Peek().availableTime == freeTime)
                {
                    var (_, otherRoom) = busyRooms.Dequeue();
                    availableRooms.Enqueue(otherRoom, otherRoom);
                }

                // Now assign the meeting to the room with the smallest index
                // among those that just became free (or were already free).
                int assignedRoom = availableRooms.Dequeue();
                meetingCount[assignedRoom]++;

                // The assigned room is now busy until freeTime + duration.
                busyRooms.Enqueue(
                    (freeTime + duration, assignedRoom),
                    (freeTime + duration, assignedRoom));
            }
            else
            {
                // At least one room is available right now.
                // Pick the one with the smallest index (min-heap gives us this).
                int assignedRoom = availableRooms.Dequeue();
                meetingCount[assignedRoom]++;

                // We need to know WHEN this room becomes free.
                // Since multiple rooms may be free simultaneously, we need a
                // "current time" reference.  The current time is the maximum
                // of 0 and the latest time we've seen any room become free
                // that is still in the available pool.
                //
                // Simpler approach: track a global "current time" that advances
                // to the earliest busy-room release time whenever we drain busy rooms.
                // But here, since the room was already available (freeTime ≤ currentTime),
                // we just need to know what "now" is.
                //
                // We'll use a separate variable to track the current scheduling time.
                // However, to keep this loop clean, let's refactor to use an explicit
                // currentTime variable.  See the note below — we'll restructure.

                // For now, place a placeholder; we'll fix this in the refactored version.
                busyRooms.Enqueue(
                    (duration, assignedRoom),   // This is WRONG without currentTime!
                    (duration, assignedRoom));
            }
        }

        // ── Step 5: Find the room with the most meetings.
        //    If there's a tie, return the smallest index (iterate 0..n-1 in order).
        int busiestRoom = 0;
        for (int i = 1; i < n; i++)
        {
            if (meetingCount[i] > meetingCount[busiestRoom])
                busiestRoom = i;
        }
        return busiestRoom;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REVISED, CORRECT Solution Class
// The first attempt above has a bug: when a room is "available" we don't know
// the current time to compute its next free time.  We need to track currentTime
// explicitly.  The correct approach is:
//
//   • Keep a global `currentTime` that represents the time at which the NEXT
//     meeting will start.
//   • Before assigning each meeting:
//       – If any room is free, currentTime stays as-is (or advances to the
//         earliest busy-room release if no room is free).
//       – Release all busy rooms whose freeTime ≤ currentTime back to available.
//   • Assign to the smallest-index available room.
//   • Mark that room busy until currentTime + duration.
//   • Advance currentTime for the next iteration only if no room was free
//     (i.e., we had to wait).
//
// Actually the cleanest model is:
//   currentTime = 0 initially.
//   For each meeting:
//     1. Release all busy rooms with freeTime ≤ currentTime → available.
//     2. If available is non-empty → assign immediately at currentTime.
//     3. Else → pop earliest busy room, set currentTime = that freeTime,
//               release all busy rooms with freeTime == currentTime → available,
//               assign at currentTime.
//     4. Busy until currentTime + duration.
//   NOTE: currentTime never decreases.
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // -------------------------------------------------------------------------
    // FindBusiestRoom  (correct, final version)
    //
    // Time Complexity:  O(M log N)
    //   M = number of meetings, N = number of rooms.
    //   Each meeting does O(log N) work on the heaps.
    //
    // Space Complexity: O(N)
    //   Both heaps hold at most N entries at any time.
    // -------------------------------------------------------------------------
    public int FindBusiestRoom(int n, int[][] meetings)
    {
        // ── Data structures ──────────────────────────────────────────────────

        // meetingCount[i] = number of meetings room i has hosted.
        int[] meetingCount = new int[n];

        // availableRooms: min-heap of room indices that are currently free.
        // Priority = room index, so the smallest-index room is always at the top.
        // This satisfies the tie-breaking rule: "if two rooms are free at the
        // same time, assign to the one with the smallest index."
        var availableRooms = new PriorityQueue<int, int>();

        // busyRooms: min-heap of (freeTime, roomIndex) for rooms currently in use.
        // Priority = (freeTime, roomIndex) — lexicographic comparison means:
        //   • First sort by freeTime ascending (earliest-finishing room first).
        //   • Then sort by roomIndex ascending (tie-break by room index).
        // This is exactly what we need when draining rooms that finish simultaneously.
        var busyRooms = new PriorityQueue<(long freeTime, int roomIndex), (long, int)>();

        // ── Initialise: all rooms are free at time 0 ─────────────────────────
        for (int i = 0; i < n; i++)
        {
            // Enqueue room i with priority i (smaller index = higher priority).
            availableRooms.Enqueue(i, i);
        }

        // currentTime tracks the "wall clock" of our scheduling simulation.
        // Meetings are processed in the order given; each one starts as soon
        // as a room is available (which may require waiting if all rooms are busy).
        long currentTime = 0;

        // ── Process each meeting ──────────────────────────────────────────────
        foreach (int[] meeting in meetings)
        {
            long duration = meeting[0];

            // ── Step A: Release rooms that have finished by currentTime ───────
            // Any room whose freeTime ≤ currentTime is no longer busy.
            // We move it back to the availableRooms heap.
            //
            // Why peek before dequeue?  We only want to release rooms that are
            // actually done; rooms finishing AFTER currentTime stay busy.
            while (busyRooms.Count > 0 && busyRooms.Peek().freeTime <= currentTime)
            {
                var (_, roomIdx) = busyRooms.Dequeue();
                // Room roomIdx is now free — add it back to the available pool.
                availableRooms.Enqueue(roomIdx, roomIdx);
            }

            // ── Step B: If still no room is free, advance time ────────────────
            // All rooms are busy past currentTime.  We must wait until the
            // earliest-finishing room is done.
            if (availableRooms.Count == 0)
            {
                // The next room to become free defines the new currentTime.
                var (earliestFreeTime, _) = busyRooms.Peek();
                currentTime = earliestFreeTime;

                // Now release ALL rooms that finish at exactly currentTime.
                // (There could be multiple rooms finishing simultaneously.)
                while (busyRooms.Count > 0 && busyRooms.Peek().freeTime == currentTime)
                {
                    var (_, roomIdx) = busyRooms.Dequeue();
                    availableRooms.Enqueue(roomIdx, roomIdx);
                }
            }

            // ── Step C: Assign the meeting to the best available room ─────────
            // "Best" = smallest index among all currently free rooms.
            // Our min-heap (keyed by room index) gives us this in O(log N).
            int assignedRoom = availableRooms.Dequeue();

            // Record that this room hosted one more meeting.
            meetingCount[assignedRoom]++;

            // ── Step D: Mark the room as busy until currentTime + duration ────
            long newFreeTime = currentTime + duration;
            busyRooms.Enqueue(
                (newFreeTime, assignedRoom),   // element stored in the heap
                (newFreeTime, assignedRoom));  // priority used for ordering

            // Note: we do NOT advance currentTime here.
            // The next meeting will start at whatever currentTime is when we
            // process it (after releasing any rooms that have finished).
            // currentTime only advances when we are forced to wait (Step B).
        }

        // ── Step 5: Find the busiest room ─────────────────────────────────────
        // Scan meetingCount[0..n-1] in order.
        // Because we scan left-to-right, the first room we find with the maximum
        // count is automatically the one with the smallest index — satisfying the
        // tie-breaking rule.
        int busiestRoom = 0;
        for (int i = 1; i < n; i++)
        {
            if (meetingCount[i] > meetingCount[busiestRoom])
            {
                busiestRoom = i;
            }
        }

        return busiestRoom;
    }
}

// ─────────────────────────────────────────────────────