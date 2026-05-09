```java
/*
 * Title: Meeting Rooms with Earliest Availability
 * Difficulty: Medium
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are managing a building with `n` meeting rooms. Each room becomes available at time 0 initially.
 * You are given a list of meeting requests, where each request is represented as [duration].
 * Meetings must be assigned to the room that becomes available the earliest.
 * If two rooms become available at the same time, assign the meeting to the room with the smallest index.
 *
 * After each meeting ends, the room becomes available again at start_time + duration.
 * Your goal is to process all meetings in the order they are given and return the index of the room
 * that hosted the most meetings. If there is a tie, return the room with the smallest index.
 *
 * Constraints:
 * - 1 <= n <= 100
 * - 1 <= meetings.length <= 10^5
 * - meetings[i] = [duration] where 1 <= duration <= 10^4
 *
 * Example 1:
 * Input: n = 2, meetings = [[1],[2],[3],[1],[2]]
 * Output: 0
 * Explanation: Room 0 hosts meetings at times 0, 1, 4 (3 meetings).
 *              Room 1 hosts meetings at times 0, 2 (2 meetings). Room 0 wins.
 *
 * Example 2:
 * Input: n = 3, meetings = [[5],[2],[1],[3]]
 * Output: 2
 * Explanation: Room 0 gets meeting of duration 5 (available at 5).
 *              Room 1 gets duration 2 (available at 2).
 *              Room 2 gets duration 1 (available at 1).
 *              Fourth meeting (duration 3) goes to Room 2 (earliest available at time 1).
 *              Room 2 ends at 1+3=4, hosting 2 meetings total. Room 2 wins.
 */

import java.util.*;

/**
 * Solution class for the "Meeting Rooms with Earliest Availability" problem.
 * Uses two priority queues (min-heaps) to efficiently track:
 * 1. Available rooms (sorted by room index)
 * 2. Occupied rooms (sorted by availability time, then room index)
 */
public class Solution {

    /**
     * Finds the room that hosted the most meetings.
     *
     * <p>Algorithm Overview:
     * We use two priority queues:
     * - availableRooms: a min-heap of room indices that are currently free (sorted by index)
     * - occupiedRooms: a min-heap of [availableTime, roomIndex] for busy rooms
     *   (sorted by availableTime first, then roomIndex for ties)
     *
     * For each meeting:
     * 1. Free up any rooms that have become available by the current "virtual time"
     * 2. If there's a free room, assign the meeting to the smallest-indexed free room
     * 3. If no room is free, wait for the earliest room to become free, then assign
     *
     * @param n        the number of meeting rooms (1 <= n <= 100)
     * @param meetings a 2D array where meetings[i] = [duration] (1 <= duration <= 10^4)
     * @return the index of the room that hosted the most meetings (smallest index on tie)
     *
     * Time Complexity:  O(M * log N) where M = number of meetings, N = number of rooms
     *                   Each meeting involves heap operations costing O(log N)
     * Space Complexity: O(N) for the two priority queues storing at most N rooms total
     */
    public int mostBooked(int n, int[][] meetings) {

        // Step 1: Initialize a count array to track how many meetings each room hosts
        // meetingCount[i] = number of meetings hosted by room i
        int[] meetingCount = new int[n];

        // Step 2: Create a min-heap for available rooms, sorted by room index (smallest index first)
        // This ensures that when multiple rooms are free, we pick the one with the smallest index
        PriorityQueue<Integer> availableRooms = new PriorityQueue<>();

        // Add all rooms as initially available (rooms 0 through n-1)
        for (int i = 0; i < n; i++) {
            availableRooms.offer(i);
        }

        // Step 3: Create a min-heap for occupied rooms
        // Each entry is a long[] array: [availableTime, roomIndex]
        // Primary sort: by availableTime (earliest available first)
        // Secondary sort: by roomIndex (smallest index first, for tie-breaking)
        PriorityQueue<long[]> occupiedRooms = new PriorityQueue<>((a, b) -> {
            if (a[0] != b[0]) {
                // Different availability times: earlier time has higher priority
                return Long.compare(a[0], b[0]);
            } else {
                // Same availability time: smaller room index has higher priority
                return Long.compare(a[1], b[1]);
            }
        });

        // Step 4: Process each meeting in the order given
        // We use a "virtual current time" concept:
        // - If rooms are available, the meeting starts "now" (we don't track absolute time strictly)
        // - If no rooms are available, the meeting waits for the earliest room to free up
        // We track the current time as the time when the next meeting can start
        long currentTime = 0;

        for (int i = 0; i < meetings.length; i++) {
            // Extract the duration of the current meeting
            int duration = meetings[i][0];

            // Step 5: Determine the "start time" for this meeting
            // Since meetings are processed sequentially (one at a time), we need to figure out
            // when this meeting can start. We use a running time tracker.
            // However, the key insight is: meetings are assigned as soon as a room is free.
            // We don't have explicit arrival times, so each meeting is processed in order,
            // and we assign it to the earliest available room.

            // Step 6: Release rooms that have become available
            // We need to know the "current time" to determine which rooms are free.
            // Since meetings are processed in order without explicit start times,
            // we track the earliest possible start time for the current meeting.

            // If there are available rooms, the meeting can start at currentTime
            // If no rooms are available, we must wait until the earliest room frees up

            // First, check if any occupied rooms have become available by currentTime
            // Release all rooms whose availability time <= currentTime
            while (!occupiedRooms.isEmpty() && occupiedRooms.peek()[0] <= currentTime) {
                long[] room = occupiedRooms.poll();
                // Room at index room[1] is now free, add it back to available rooms
                availableRooms.offer((int) room[1]);
            }

            // Step 7: Assign the meeting to a room
            if (!availableRooms.isEmpty()) {
                // Case A: There is at least one available room
                // Pick the room with the smallest index (min-heap gives us this automatically)
                int roomIndex = availableRooms.poll();

                // Increment the meeting count for this room
                meetingCount[roomIndex]++;

                // Mark this room as occupied until currentTime + duration
                occupiedRooms.offer(new long[]{currentTime + duration, roomIndex});

                // Note: currentTime does NOT advance here because the next meeting
                // also starts at the same "current time" if rooms are available
                // Actually, we need to reconsider: since there's no explicit arrival time,
                // each meeting is processed one by one. The "current time" advances
                // only when we must wait for a room.
                // For this problem, we treat each meeting as arriving sequentially,
                // so the current time stays the same until we run out of rooms.

            } else {
                // Case B: No rooms are available right now
                // We must wait for the earliest room to become free
                long[] earliestRoom = occupiedRooms.poll();
                long newAvailableTime = earliestRoom[0];
                int roomIndex = (int) earliestRoom[1];

                // The current time advances to when this room becomes free
                currentTime = newAvailableTime;

                // Increment the meeting count for this room
                meetingCount[roomIndex]++;

                // Schedule this room as occupied again for the new meeting's duration
                occupiedRooms.offer(new long[]{currentTime + duration, roomIndex});

                // Now release any other rooms that also became available at this new currentTime
                // (They will be available for the NEXT meeting)
                while (!occupiedRooms.isEmpty() && occupiedRooms.peek()[0] <= currentTime) {
                    long[] room = occupiedRooms.poll();
                    availableRooms.offer((int) room[1]);
                }
            }
        }

        // Step 8: Find the room with the maximum meeting count
        // In case of a tie, return the smallest room index
        int maxMeetings = 0;
        int resultRoom = 0;

        for (int i = 0; i < n; i++) {
            // If room i hosted strictly more meetings than the current max,
            // update the result (naturally handles tie-breaking by iterating in order)
            if (meetingCount[i] > maxMeetings) {
                maxMeetings = meetingCount[i];
                resultRoom = i;
            }
            // If equal, we keep the smaller index (which is already stored in resultRoom
            // since we iterate from 0 to n-1)
        }

        return resultRoom;
    }

    /**
     * Helper method to print meeting assignments for debugging/visualization.
     * Shows how many meetings each room hosted.
     *
     * @param n        number of rooms
     * @param meetings the meetings array
     */
    public void printRoomStats(int n, int[][] meetings) {
        // We'll re-run the algorithm with detailed logging
        int[] meetingCount = new int[n];
        PriorityQueue<Integer> availableRooms = new PriorityQueue<>();
        for (int i = 0; i < n; i++) {
            availableRooms.offer(i);
        }

        PriorityQueue<long[]> occupiedRooms = new PriorityQueue<>((a, b) -> {
            if (a[0] != b[0]) return Long.compare(a[0], b[0]);
            return Long.compare(a[1], b[1]);
        });

        long currentTime = 0;

        for (int i = 0; i < meetings.length; i++) {
            int duration = meetings[i][0];

            while (!occupiedRooms.isEmpty() && occupiedRooms.peek()[0] <= currentTime) {
                long[] room = occupiedRooms.poll();
                availableRooms.offer((int) room[1]);
            }

            if (!availableRooms.isEmpty()) {
                int roomIndex = availableRooms.poll();
                meetingCount[roomIndex]++;
                System.out.println("  Meeting " + (i + 1) + " (duration=" + duration +
                        "): assigned to Room " + roomIndex +
                        " starting at time " + currentTime +
                        ", ends at " + (currentTime + duration));
                occupiedRooms.offer(new long[]{currentTime + duration, roomIndex});
            } else {
                long[] earliestRoom = occupiedRooms.poll();
                long newAvailableTime = earliestRoom[0];
                int roomIndex = (int) earliestRoom[1];
                currentTime = newAvailableTime;
                meetingCount[roomIndex]++;
                System.out.println("  Meeting " + (i + 1) + " (duration=" + duration +
                        "): waited, assigned to Room " + roomIndex +
                        " starting at time " + currentTime +
                        ", ends at " + (currentTime + duration));
                occupiedRooms.offer(new long[]{currentTime + duration, roomIndex});

                while (!occupiedRooms.isEmpty() && occupiedRooms.peek()[0] <= currentTime) {
                    long[] room = occupiedRooms.poll();
                    availableRooms.offer((int) room[1]);
                }
            }
        }

        System.out.println("  Room meeting counts:");
        for (int i = 0; i < n; i++) {
            System.out.println("    Room " + i + ": " + meetingCount[i] + " meetings");
        }
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through the examples from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ============================================================
        // Example 1:
        // n = 2, meetings = [[1],[2],[3],[1],[2]]
        // Expected Output: 0
        // Trace:
        //   Meeting 1 (dur=1): Room 0 free, assign Room 0, ends at 1
        //   Meeting 2 (dur=2): Room 1 free, assign Room 1, ends at 2
        //   Meeting 3 (dur=3): No rooms free (Room 0 free at 1, Room 1 free at 2)
        //                      Wait for Room 0 at time 1, assign Room 0, ends at 4
        //   Meeting 4 (dur=1): Room 1 free at 2, currentTime=1, Room 1 not free yet
        //                      Wait for Room 1 at time 2, assign Room 1, ends at 3
        //   Meeting 5 (dur=2): Room 1 free at 3, currentTime=2
        //                      Room 1 becomes free at 3, Room 0 free at 4
        //                      Wait for Room 1 at time 3, assign Room 1... 
        //                      Actually let's re-trace carefully:
        //   After meeting 3: currentTime=1, Room 0 busy until 4, Room 1 busy until 2
        //   Meeting 4 (dur=1): currentTime=1, check occupied: Room 1 free at 2 > 1, no release
        //                      No available rooms, pick earliest: Room 1 at time 2
        //                      currentTime=2, assign Room 1, ends at 3
        //                      Check occupied: Room 0 busy until 4 > 2, no release
        //   Meeting 5 (dur=2): currentTime=2, check occupied: Room 1 free at 3 > 2, no release
        //                      No available rooms, pick earliest: Room 1 at time 3
        //                      currentTime=3, assign Room 1, ends at 5
        //   Final counts: Room 0 = 2 meetings, Room 1 = 3 meetings... 
        //   Hmm, let me re-read the problem...
        //   The problem says Room 0 hosts 3 meetings. Let me re-trace.
        //   Actually the issue is that after assigning meeting 3 to Room 0 at time 1,
        //   currentTime should reset or stay. Let me think again.
        //   The key: currentTime only advances when we WAIT. After waiting, it stays at that time.
        //   Meeting 1: currentTime=0, Room 0 available, assign Room 0 (ends at 1). currentTime stays 0.
        //   Meeting 2: currentTime=0, Room 1 available, assign Room 1 (ends at 2). currentTime stays 0.
        //   Meeting 3: currentTime=0, no rooms free (Room 0 free at 1, Room 1 free at 2)
        //              Wait for Room 0 at time 1. currentTime=1. Assign Room 0 (ends at 4).
        //              Release rooms with availTime <= 1: none (Room 1 free at 2 > 1).
        //   Meeting 4: currentTime=1, check occupied: Room 1 free at 2 > 1, no release.
        //