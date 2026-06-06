/*
 * Title: Schedule Meetings to Minimize Maximum Wait Time
 * Difficulty: Hard
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are managing a conference center with k meeting rooms. You are given a list of n meeting
 * requests, where requests[i] = [arrivalTime, duration] represents a meeting that arrives at
 * arrivalTime and requires duration units of time to complete.
 *
 * Meetings are assigned to rooms greedily in the order they arrive. When a meeting arrives,
 * it is assigned to the room that will become available the earliest (i.e., the room whose
 * current meeting ends the soonest). If multiple rooms are available at or before the meeting's
 * arrival time, assign the meeting to the room with the smallest index.
 *
 * Each meeting's wait time is defined as max(0, roomAvailableTime - arrivalTime), meaning how
 * long a meeting must wait before it can start.
 *
 * Return the maximum wait time experienced by any single meeting across all k rooms.
 *
 * Constraints:
 * - 1 <= k <= 100
 * - 1 <= n <= 10^5
 * - 0 <= arrivalTime <= 10^9
 * - 1 <= duration <= 10^4
 * - Meetings are given in non-decreasing order of arrivalTime
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Time Complexity:  O(n log k)
     *   - We process each of the n meetings once.
     *   - Each heap operation (push/pop) on a heap of size k costs O(log k).
     *
     * Space Complexity: O(k)
     *   - We maintain two priority queues whose combined size is always k.
     *   - The "available" min-heap holds rooms free right now (up to k entries).
     *   - The "busy"    min-heap holds rooms currently occupied (up to k entries).
     */
    public int MaxWaitTime(int k, int[][] requests)
    {
        // ── Step 1: Create a min-heap for AVAILABLE rooms ─────────────────────
        // We want to always pick the room with the SMALLEST index when multiple
        // rooms are free at the time a meeting arrives.
        // A min-heap ordered by room index gives us the smallest index at the top.
        //
        // Each element is simply the room index (0-based).
        var availableRooms = new PriorityQueue<int, int>(); // (roomIndex, roomIndex)

        // Initialise: all k rooms start free at time 0.
        for (int roomIndex = 0; roomIndex < k; roomIndex++)
        {
            // Priority = roomIndex so that room 0 is always dequeued first
            // when several rooms are free simultaneously.
            availableRooms.Enqueue(roomIndex, roomIndex);
        }

        // ── Step 2: Create a min-heap for BUSY rooms ──────────────────────────
        // When no room is free we must pick the room that becomes free the
        // SOONEST (smallest endTime).  A min-heap ordered by endTime achieves this.
        //
        // Each element stores (roomIndex, endTime) so we can recover the room
        // after it finishes.  The priority key is endTime.
        var busyRooms = new PriorityQueue<(int roomIndex, long endTime), long>();

        // ── Step 3: Track the global maximum wait time ────────────────────────
        int maxWait = 0;

        // ── Step 4: Process every meeting in arrival order ────────────────────
        // The problem guarantees requests are sorted by arrivalTime, so we can
        // iterate straight through without sorting.
        foreach (var request in requests)
        {
            long arrivalTime = request[0];
            long duration    = request[1];

            // ── Step 4a: Release rooms that have finished by arrivalTime ───────
            // Before assigning this meeting we check whether any busy room has
            // become free (its endTime <= arrivalTime).  If so, move it back to
            // the availableRooms heap so it can be selected with the correct
            // smallest-index priority.
            //
            // Why a loop?  Multiple rooms could finish before this meeting arrives.
            while (busyRooms.Count > 0 &&
                   busyRooms.TryPeek(out var topBusy, out long topEndTime) &&
                   topEndTime <= arrivalTime)
            {
                // Remove the room from the busy heap …
                busyRooms.Dequeue();
                // … and put it back into the available heap, keyed by room index
                // so the smallest-index room surfaces first.
                availableRooms.Enqueue(topBusy.roomIndex, topBusy.roomIndex);
            }

            // ── Step 4b: Assign the meeting to a room ─────────────────────────
            if (availableRooms.Count > 0)
            {
                // ── Case A: At least one room is free right now ────────────────
                // Pick the free room with the smallest index (top of min-heap).
                int chosenRoom = availableRooms.Dequeue();

                // The meeting starts immediately — no waiting.
                long waitTime = 0; // max(0, roomAvailableTime - arrivalTime) = max(0, 0) = 0

                // Update the global maximum.
                maxWait = Math.Max(maxWait, (int)waitTime);

                // The room will be busy until arrivalTime + duration.
                long endTime = arrivalTime + duration;

                // Push the room onto the busy heap so future meetings know when
                // it will be free again.
                busyRooms.Enqueue((chosenRoom, endTime), endTime);
            }
            else
            {
                // ── Case B: All rooms are busy — pick the one finishing soonest ─
                // The meeting must wait until that room is free.
                busyRooms.TryPeek(out var earliestBusy, out long earliestEnd);
                busyRooms.Dequeue(); // remove it from the busy heap

                // Wait time = how long after arrival the meeting must wait.
                long waitTime = earliestEnd - arrivalTime; // always > 0 here

                // Update the global maximum.
                maxWait = Math.Max(maxWait, (int)waitTime);

                // The meeting starts at earliestEnd and runs for duration.
                long newEndTime = earliestEnd + duration;

                // Put the room back on the busy heap with its new end time.
                busyRooms.Enqueue((earliestBusy.roomIndex, newEndTime), newEndTime);
            }
        }

        // ── Step 5: Return the answer ─────────────────────────────────────────
        // maxWait now holds the largest wait time any single meeting experienced.
        return maxWait;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solver = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// k = 2, requests = [[0,5],[1,3],[2,4],[6,2]]
// Expected output: 2
//
// Trace:
//   Meeting [0,5]  → Room 0 free (smallest index). Wait=0. Room 0 busy until 5.
//   Meeting [1,3]  → Room 1 free (smallest index). Wait=0. Room 1 busy until 4.
//   Meeting [2,4]  → No room free at t=2. Earliest busy room: Room 1 ends at 4.
//                    Wait = 4-2 = 2. Room 1 busy until 4+4=8.
//   Meeting [6,2]  → Room 0 ends at 5 <= 6 → moves to available.
//                    Room 1 ends at 8 > 6 → still busy.
//                    Available: {Room 0}. Pick Room 0. Wait=0. Room 0 busy until 8.
// Max wait = 2  ✓
int[][] requests1 = [[0, 5], [1, 3], [2, 4], [6, 2]];
int result1 = solver.MaxWaitTime(2, requests1);
Console.WriteLine($"Example 1 → Expected: 2,  Got: {result1}");

// ── Example 2 ────────────────────────────────────────────────────────────────
// k = 1, requests = [[0,10],[2,5],[3,2]]
// Expected output: 12
//
// Trace:
//   Meeting [0,10] → Room 0 free. Wait=0. Room 0 busy until 10.
//   Meeting [2,5]  → No room free at t=2. Room 0 ends at 10. Wait=10-2=8. Busy until 15.
//   Meeting [3,2]  → No room free at t=3. Room 0 ends at 15. Wait=15-3=12. Busy until 17.
// Max wait = 12  ✓
int[][] requests2 = [[0, 10], [2, 5], [3, 2]];
int result2 = solver.MaxWaitTime(1, requests2);
Console.WriteLine($"Example 2 → Expected: 12, Got: {result2}");

// ── Extra Example: All meetings fit without waiting ───────────────────────────
// k = 3, requests = [[0,1],[5,1],[10,1]]
// Each meeting arrives after the previous one finishes → all waits = 0.
int[][] requests3 = [[0, 1], [5, 1], [10, 1]];
int result3 = solver.MaxWaitTime(3, requests3);
Console.WriteLine($"Extra 1  → Expected: 0,  Got: {result3}");

// ── Extra Example: Single room, back-to-back meetings ────────────────────────
// k = 1, requests = [[0,3],[0,3],[0,3]]
// Meeting 0 → Room 0, wait=0, ends at 3.
// Meeting 1 → Room 0, wait=3-0=3, ends at 6.
// Meeting 2 → Room 0, wait=6-0=6, ends at 9.
// Max wait = 6
int[][] requests4 = [[0, 3], [0, 3], [0, 3]];
int result4 = solver.MaxWaitTime(1, requests4);
Console.WriteLine($"Extra 2  → Expected: 6,  Got: {result4}");

// ── Extra Example: Smallest-index tie-breaking ────────────────────────────────
// k = 3, requests = [[0,5],[0,5],[0,5],[6,1]]
// Meetings 0,1,2 fill rooms 0,1,2 (all end at 5).
// Meeting 3 arrives at t=6; all rooms free. Smallest index = Room 0. Wait=0.
// Max wait = 0
int[][] requests5 = [[0, 5], [0, 5], [0, 5], [6, 1]];
int result5 = solver.MaxWaitTime(3, requests5);
Console.WriteLine($"Extra 3  → Expected: 0,  Got: {result5}");