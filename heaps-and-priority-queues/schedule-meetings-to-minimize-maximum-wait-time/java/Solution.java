```java
/*
 * Title: Schedule Meetings to Minimize Maximum Wait Time
 * Difficulty: Hard
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are managing a conference center with `k` meeting rooms. You are given a list of `n` meeting
 * requests, where `requests[i] = [arrivalTime, duration]` represents a meeting that arrives at
 * `arrivalTime` and requires `duration` units of time to complete.
 *
 * Meetings are assigned to rooms greedily in the order they arrive. When a meeting arrives, it is
 * assigned to the room that will become available the earliest (i.e., the room whose current meeting
 * ends the soonest). If multiple rooms are available at or before the meeting's arrival time, assign
 * the meeting to the room with the smallest index.
 *
 * Each meeting's wait time is defined as max(0, roomAvailableTime - arrivalTime), meaning how long
 * a meeting must wait before it can start.
 *
 * Return the maximum wait time experienced by any single meeting across all k rooms.
 *
 * Constraints:
 * - 1 <= k <= 100
 * - 1 <= n <= 10^5
 * - 0 <= arrivalTime <= 10^9
 * - 1 <= duration <= 10^4
 * - Meetings are given in non-decreasing order of arrivalTime.
 */

import java.util.*;

/**
 * Solution class for the "Schedule Meetings to Minimize Maximum Wait Time" problem.
 *
 * <p>Core Idea:
 * We simulate the meeting room assignment process using two priority queues (min-heaps):
 * 1. A "free rooms" heap that tracks available rooms, ordered by room index (smallest index first).
 * 2. A "busy rooms" heap that tracks occupied rooms, ordered by the time they become free
 *    (earliest finish time first), with room index as a tiebreaker.
 *
 * For each meeting, we first check if any busy rooms have become free by the meeting's arrival time.
 * If so, we move them to the free rooms heap. Then we pick the best available room:
 * - If there are free rooms, pick the one with the smallest index (wait = 0).
 * - If all rooms are busy, pick the one that finishes earliest (wait = finishTime - arrivalTime).
 */
public class Solution {

    /**
     * Computes the maximum wait time experienced by any meeting when scheduled across k rooms.
     *
     * <p>Algorithm Overview:
     * 1. Initialize a min-heap of free rooms (ordered by room index).
     * 2. Initialize a min-heap of busy rooms (ordered by finish time, then room index).
     * 3. For each meeting (in arrival order):
     *    a. Release all rooms that have finished by the current arrival time into the free heap.
     *    b. If there's a free room, assign the meeting there (wait = 0), pick smallest index.
     *    c. If no free room, assign to the room finishing earliest (wait = finishTime - arrivalTime).
     *    d. Update the maximum wait time.
     * 4. Return the maximum wait time.
     *
     * @param k        the number of meeting rooms available
     * @param requests a 2D array where requests[i] = [arrivalTime, duration]
     * @return the maximum wait time experienced by any single meeting
     *
     * Time Complexity:  O(n log k) — each meeting involves at most one heap insertion and one
     *                   heap extraction from heaps of size at most k.
     * Space Complexity: O(k) — we store at most k rooms across both heaps at any time.
     */
    public int maxWaitTime(int k, int[][] requests) {

        // -----------------------------------------------------------------------
        // Step 1: Initialize the "free rooms" min-heap.
        // We order free rooms by their room index (smallest index = highest priority).
        // Initially, all k rooms are free.
        // -----------------------------------------------------------------------
        // PriorityQueue with natural ordering on Integer (smallest index first)
        PriorityQueue<Integer> freeRooms = new PriorityQueue<>();
        for (int i = 0; i < k; i++) {
            freeRooms.offer(i); // Add room 0, 1, 2, ..., k-1
        }

        // -----------------------------------------------------------------------
        // Step 2: Initialize the "busy rooms" min-heap.
        // Each entry is a long[] array: [finishTime, roomIndex].
        // We order by finishTime first (earliest finish = highest priority),
        // then by roomIndex (smallest index = higher priority) as a tiebreaker.
        // -----------------------------------------------------------------------
        PriorityQueue<long[]> busyRooms = new PriorityQueue<>((a, b) -> {
            if (a[0] != b[0]) {
                // Primary sort: by finish time (ascending)
                return Long.compare(a[0], b[0]);
            }
            // Secondary sort: by room index (ascending) — smaller index preferred
            return Long.compare(a[1], b[1]);
        });

        // -----------------------------------------------------------------------
        // Step 3: Track the maximum wait time seen so far.
        // -----------------------------------------------------------------------
        long maxWait = 0;

        // -----------------------------------------------------------------------
        // Step 4: Process each meeting request in order of arrival.
        // -----------------------------------------------------------------------
        for (int[] request : requests) {
            long arrivalTime = request[0]; // When this meeting arrives
            long duration    = request[1]; // How long this meeting takes

            // -------------------------------------------------------------------
            // Step 4a: Release rooms that have finished by the current arrival time.
            // A room is "free" if its finishTime <= arrivalTime (the meeting has ended
            // before or exactly when the new meeting arrives, so no waiting needed).
            // -------------------------------------------------------------------
            while (!busyRooms.isEmpty() && busyRooms.peek()[0] <= arrivalTime) {
                // This room's meeting has ended; move it to the free rooms heap
                long[] finishedRoom = busyRooms.poll();
                int roomIndex = (int) finishedRoom[1];
                freeRooms.offer(roomIndex);
            }

            // -------------------------------------------------------------------
            // Step 4b: Assign the current meeting to a room.
            // -------------------------------------------------------------------
            long waitTime;   // Wait time for this particular meeting
            long startTime;  // When this meeting actually starts
            int assignedRoom; // Which room gets this meeting

            if (!freeRooms.isEmpty()) {
                // ---------------------------------------------------------------
                // Case A: There is at least one free room available.
                // Pick the free room with the smallest index (freeRooms is a min-heap
                // ordered by room index, so peek/poll gives the smallest index).
                // The meeting starts immediately upon arrival → wait = 0.
                // ---------------------------------------------------------------
                assignedRoom = freeRooms.poll(); // Smallest-index free room
                startTime    = arrivalTime;       // No waiting needed
                waitTime     = 0;                 // Wait time is 0
            } else {
                // ---------------------------------------------------------------
                // Case B: All rooms are busy.
                // Pick the room that finishes earliest (busyRooms is a min-heap
                // ordered by finish time, then room index).
                // The meeting must wait until that room is free.
                // ---------------------------------------------------------------
                long[] earliestRoom = busyRooms.poll(); // Room finishing soonest
                long roomFinishTime = earliestRoom[0];
                assignedRoom        = (int) earliestRoom[1];

                // The meeting starts when the room becomes free
                startTime = roomFinishTime;

                // Wait time = how long the meeting had to wait after arriving
                waitTime = roomFinishTime - arrivalTime;
            }

            // -------------------------------------------------------------------
            // Step 4c: Update the global maximum wait time.
            // -------------------------------------------------------------------
            maxWait = Math.max(maxWait, waitTime);

            // -------------------------------------------------------------------
            // Step 4d: Mark the assigned room as busy until startTime + duration.
            // Add it to the busyRooms heap with its new finish time.
            // -------------------------------------------------------------------
            long finishTime = startTime + duration;
            busyRooms.offer(new long[]{finishTime, assignedRoom});
        }

        // -----------------------------------------------------------------------
        // Step 5: Return the maximum wait time observed across all meetings.
        // Cast to int since wait times fit within int range given the constraints.
        // -----------------------------------------------------------------------
        return (int) maxWait;
    }

    // ===========================================================================
    // Main method: Demonstrates the solution with sample inputs from the problem.
    // ===========================================================================

    /**
     * Entry point for demonstration purposes.
     * Traces through the examples provided in the problem description and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // k = 2, requests = [[0,5],[1,3],[2,4],[6,2]]
        // Expected Output: 2
        //
        // Trace:
        // - Meeting [0,5]: freeRooms = {0,1}. Pick room 0. Start=0, End=5. Wait=0.
        //   busyRooms = {[5,0]}. freeRooms = {1}.
        // - Meeting [1,3]: freeRooms = {1}. Pick room 1. Start=1, End=4. Wait=0.
        //   busyRooms = {[4,1],[5,0]}. freeRooms = {}.
        // - Meeting [2,4]: No rooms free (room 1 ends at 4 > 2, room 0 ends at 5 > 2).
        //   Pick earliest: room 1 (ends at 4). Start=4, End=8. Wait=4-2=2.
        //   busyRooms = {[5,0],[8,1]}. freeRooms = {}.
        // - Meeting [6,2]: Release rooms finishing <= 6: room 0 (ends at 5 <= 6).
        //   freeRooms = {0}. Pick room 0. Start=6, End=8. Wait=0.
        //   busyRooms = {[8,0],[8,1]}.
        // Max wait = 2. ✓
        // -----------------------------------------------------------------------
        int k1 = 2;
        int[][] requests1 = {{0, 5}, {1, 3}, {2, 4}, {6, 2}};
        int result1 = solution.maxWaitTime(k1, requests1);
        System.out.println("Example 1:");
        System.out.println("  k = " + k1);
        System.out.println("  requests = [[0,5],[1,3],[2,4],[6,2]]");
        System.out.println("  Expected Output: 2");
        System.out.println("  Actual Output:   " + result1);
        System.out.println("  " + (result1 == 2 ? "✓ PASS" : "✗ FAIL"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // k = 1, requests = [[0,10],[2,5],[3,2]]
        // Expected Output: 12
        //
        // Trace:
        // - Meeting [0,10]: freeRooms = {0}. Pick room 0. Start=0, End=10. Wait=0.
        //   busyRooms = {[10,0]}. freeRooms = {}.
        // - Meeting [2,5]: No rooms free (room 0 ends at 10 > 2).
        //   Pick earliest: room 0 (ends at 10). Start=10, End=15. Wait=10-2=8.
        //   busyRooms = {[15,0]}. freeRooms = {}.
        // - Meeting [3,2]: No rooms free (room 0 ends at 15 > 3).
        //   Pick earliest: room 0 (ends at 15). Start=15, End=17. Wait=15-3=12.
        //   busyRooms = {[17,0]}. freeRooms = {}.
        // Max wait = 12. ✓
        // -----------------------------------------------------------------------
        int k2 = 1;
        int[][] requests2 = {{0, 10}, {2, 5}, {3, 2}};
        int result2 = solution.maxWaitTime(k2, requests2);
        System.out.println("Example 2:");
        System.out.println("  k = " + k2);
        System.out.println("  requests = [[0,10],[2,5],[3,2]]");
        System.out.println("  Expected Output: 12");
        System.out.println("  Actual Output:   " + result2);
        System.out.println("  " + (result2 == 12 ? "✓ PASS" : "✗ FAIL"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case: Single meeting, single room — no wait.
        // k = 1, requests = [[5, 3]]
        // Expected Output: 0 (meeting arrives at 5, room is free, starts immediately)
        // -----------------------------------------------------------------------
        int k3 = 1;
        int[][] requests3 = {{5, 3}};
        int result3 = solution.maxWaitTime(k3, requests3);
        System.out.println("Edge Case (single meeting):");
        System.out.println("  k = " + k3);
        System.out.println("  requests = [[5,3]]");
        System.out.println("  Expected Output: 0");
        System.out.println("  Actual Output:   " + result3);
        System.out.println("  " + (result3 == 0 ? "✓ PASS" : "✗ FAIL"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case: All meetings arrive at the same time.
        // k = 2, requests = [[0,5],[0,3],[0,4]]
        // Trace:
        // - Meeting [0,5]: freeRooms={0,1}. Pick room 0. Start=0, End=5. Wait=0.
        // - Meeting [0,3]: freeRooms={1}. Pick room 1. Start=0, End=3. Wait=0.
        // - Meeting [0,4]: No free rooms. busyRooms={[3,1],[5,0]}. Pick room 1 (ends at 3).
        //   Start=3, End=7. Wait=3-0=3.
        // Max wait = 3.
        // -----------------------------------------------------------------------
        int k4 = 2;
        int[][] requests4 = {{0, 5}, {0, 3}, {0, 4}};
        int result4 = solution.maxWaitTime(k4, requests4);
        System.out.println("Edge Case (same arrival time):");
        System.out.println("  k = " + k4);
        System.out.println("  requests = [[0,5],[0,3],[0,4]]");
        System.out.println("  Expected Output: 3");
        System.out.println("  Actual Output:   " + result4);
        System.out.println("  " + (result4 == 3 ? "✓ PASS" : "✗ FAIL"));
    }
}
```