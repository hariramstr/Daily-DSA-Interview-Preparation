import java.util.*;

/*
 * Title: Earliest Room Free for Delayed Bookings
 * Difficulty: Medium
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are managing a set of meeting rooms in an office. There are n identical rooms
 * numbered from 0 to n - 1. You are given a list of meeting requests, where each
 * request is represented as [start, duration]. A request wants to begin at time start
 * and would occupy a room for exactly duration minutes.
 *
 * If at least one room is free at time start, the meeting is assigned immediately to
 * the free room with the smallest room number. If all rooms are busy, the meeting must
 * wait until some room becomes available. In that case, the meeting starts at the
 * earliest time any room becomes free, and among all rooms that become free at that
 * earliest time, it uses the one with the smallest room number. The meeting still lasts
 * for its full duration after it actually starts.
 *
 * Return an array answer of length equal to the number of requests, where answer[i] is
 * the actual start time of the i-th meeting request after applying the scheduling rules above.
 *
 * The requests should be processed in increasing order of their original start times.
 * If multiple requests have the same start, process them in their original input order.
 *
 * Constraints:
 * - 1 <= n <= 10^5
 * - 1 <= meetings.length <= 2 * 10^5
 * - meetings[i].length == 2
 * - 0 <= start <= 10^9
 * - 1 <= duration <= 10^9
 * - The answer values may exceed 10^9, so use 64-bit integers.
 *
 * Example 1:
 * Input: n = 2, meetings = [[1,4],[2,3],[3,2],[10,1]]
 * Output: [1,2,5,10]
 *
 * Example 2:
 * Input: n = 1, meetings = [[0,5],[1,2],[1,4],[8,3]]
 * Output: [0,5,7,11]
 */

public class Solution {

    /**
     * Small helper class representing one meeting request after we enrich it with:
     * - original start time
     * - duration
     * - original input index
     *
     * We keep the original index so that after processing meetings in sorted order,
     * we can place each computed actual start time back into the correct answer position.
     */
    private static class MeetingRequest {
        long start;
        long duration;
        int index;

        MeetingRequest(long start, long duration, int index) {
            this.start = start;
            this.duration = duration;
            this.index = index;
        }
    }

    /**
     * Small helper class representing a room that is currently busy.
     *
     * Fields:
     * - endTime: when the room becomes free
     * - roomId: which room this is
     *
     * This is stored inside a min-heap ordered by:
     * 1) earliest end time
     * 2) smallest room id if end times tie
     *
     * That ordering exactly matches the problem rule for delayed meetings:
     * if all rooms are busy, choose the room that becomes free earliest,
     * and among those, choose the smallest room number.
     */
    private static class BusyRoom {
        long endTime;
        int roomId;

        BusyRoom(long endTime, int roomId) {
            this.endTime = endTime;
            this.roomId = roomId;
        }
    }

    /**
     * Computes the actual start time for every meeting request after applying the room
     * assignment and delay rules.
     *
     * Core idea:
     * - Process meetings in increasing requested start time.
     * - Maintain a min-heap of free room ids so we can always choose the smallest free room.
     * - Maintain a min-heap of busy rooms ordered by earliest finish time, then smallest room id.
     * - Before handling a meeting at time s, release every room whose meeting ended at or before s.
     * - If any room is free, start immediately at s.
     * - Otherwise, delay the meeting until the earliest busy room becomes free.
     *
     * @param n the number of rooms, numbered from 0 to n - 1
     * @param meetings the meeting requests where each request is [start, duration]
     * @return an array where the i-th value is the actual start time of the i-th meeting
     *
     * Time complexity: O((m + n) log n + m log m), where m = meetings.length
     * Sorting the meetings costs O(m log m), and each heap operation costs O(log n).
     *
     * Space complexity: O(m + n)
     * We store the sorted meeting list, answer array, and the two priority queues.
     */
    public long[] earliestRoomFree(int n, int[][] meetings) {
        int m = meetings.length;

        // This array will hold the final actual start time for each original meeting index.
        long[] answer = new long[m];

        // Step 1:
        // Convert the raw input into MeetingRequest objects so we can sort by:
        // - requested start time ascending
        // - original index ascending when start times tie
        //
        // The second rule is important because the problem explicitly says:
        // if multiple requests have the same start, process them in original input order.
        MeetingRequest[] requests = new MeetingRequest[m];
        for (int i = 0; i < m; i++) {
            requests[i] = new MeetingRequest(meetings[i][0], meetings[i][1], i);
        }

        Arrays.sort(requests, (a, b) -> {
            if (a.start != b.start) {
                return Long.compare(a.start, b.start);
            }
            return Integer.compare(a.index, b.index);
        });

        // Step 2:
        // Min-heap of currently free room ids.
        // Because it is a min-heap, polling gives the smallest available room number.
        PriorityQueue<Integer> freeRooms = new PriorityQueue<>();

        // Initially, all rooms are free.
        for (int room = 0; room < n; room++) {
            freeRooms.offer(room);
        }

        // Step 3:
        // Min-heap of busy rooms.
        // Ordered by:
        //   1) earliest end time
        //   2) smallest room id
        //
        // This ordering is exactly what we need when all rooms are busy.
        PriorityQueue<BusyRoom> busyRooms = new PriorityQueue<>((a, b) -> {
            if (a.endTime != b.endTime) {
                return Long.compare(a.endTime, b.endTime);
            }
            return Integer.compare(a.roomId, b.roomId);
        });

        // Step 4:
        // Process each meeting request in the required order.
        for (MeetingRequest request : requests) {
            long requestedStart = request.start;
            long duration = request.duration;

            // Before scheduling the current meeting, release every room that has already
            // finished by the requested start time.
            //
            // Why "while" and not "if"?
            // Because multiple rooms may have become free before or exactly at this time,
            // and all of them should be moved back into the free-room heap.
            while (!busyRooms.isEmpty() && busyRooms.peek().endTime <= requestedStart) {
                BusyRoom finished = busyRooms.poll();
                freeRooms.offer(finished.roomId);
            }

            // Case 1: At least one room is free right now.
            if (!freeRooms.isEmpty()) {
                // Choose the smallest room number among free rooms.
                int roomId = freeRooms.poll();

                // The meeting starts immediately at its requested start time.
                long actualStart = requestedStart;
                long endTime = actualStart + duration;

                // Record the answer in the original meeting position.
                answer[request.index] = actualStart;

                // Mark this room as busy until endTime.
                busyRooms.offer(new BusyRoom(endTime, roomId));
            } else {
                // Case 2: All rooms are busy.
                //
                // We must delay the meeting until the earliest room becomes free.
                // Because busyRooms is ordered by endTime then roomId, polling gives:
                // - the earliest available time
                // - and among ties, the smallest room id
                BusyRoom earliest = busyRooms.poll();

                long actualStart = earliest.endTime;
                long endTime = actualStart + duration;

                // Record the delayed actual start time.
                answer[request.index] = actualStart;

                // The same room is immediately reused by this delayed meeting,
                // so it becomes busy again until the new end time.
                busyRooms.offer(new BusyRoom(endTime, earliest.roomId));
            }
        }

        return answer;
    }

    /**
     * Utility method to print a long array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation such as [1, 2, 5, 10]
     *
     * Time complexity: O(k), where k is the array length
     * Space complexity: O(k) for the StringBuilder content
     */
    public String arrayToString(long[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arr[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     *
     * Time complexity: O(total input size processed in the demonstrations)
     * Space complexity: O(total output arrays created in the demonstrations)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 2;
        int[][] meetings1 = {
            {1, 4},
            {2, 3},
            {3, 2},
            {10, 1}
        };
        long[] result1 = solution.earliestRoomFree(n1, meetings1);
        System.out.println("Example 1 Output: " + solution.arrayToString(result1));
        System.out.println("Expected: [1, 2, 5, 10]");

        int n2 = 1;
        int[][] meetings2 = {
            {0, 5},
            {1, 2},
            {1, 4},
            {8, 3}
        };
        long[] result2 = solution.earliestRoomFree(n2, meetings2);
        System.out.println("Example 2 Output: " + solution.arrayToString(result2));
        System.out.println("Expected: [0, 5, 7, 11]");
    }
}