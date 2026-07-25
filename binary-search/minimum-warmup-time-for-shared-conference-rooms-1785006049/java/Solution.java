import java.util.*;

/*
 * Title: Minimum Warmup Time for Shared Conference Rooms
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A company has n meetings that must be held in the given order. The i-th meeting starts at time start[i]
 * and ends at time end[i], where start and end are strictly increasing arrays and start[i] < end[i].
 * Before any meeting begins, a room assigned to that meeting must be warmed up for w minutes immediately
 * before the meeting starts, meaning the room is occupied during the interval [start[i] - w, end[i]].
 *
 * The company has exactly k identical conference rooms. Meetings cannot be reordered, split, or moved.
 * Two meetings may use the same room only if their occupied intervals do not overlap. Your task is to find
 * the maximum integer warmup time w such that all meetings can still be scheduled using at most k rooms.
 *
 * Because larger warmup times make scheduling harder, feasibility is monotonic:
 * - if a warmup time w is feasible, then any smaller warmup time is also feasible
 * - if a warmup time w is not feasible, then any larger warmup time is also not feasible
 *
 * Therefore, we can binary search for the largest feasible w.
 *
 * Efficient idea:
 * For a fixed warmup time w, each meeting occupies [start[i] - w, end[i]].
 * Since meetings are processed in increasing order of start[i], we can sweep from left to right and keep
 * track of currently occupied rooms using a min-heap of ending times.
 *
 * For each meeting:
 * 1. Its occupied interval begins at start[i] - w.
 * 2. Any room whose previous meeting ended strictly before this begin time can be reused.
 *    Since intervals are closed in the statement, touching at a point still overlaps.
 *    So a room is reusable only when previousEnd < currentBegin.
 * 3. Push the current meeting's end time into the heap.
 * 4. If heap size ever exceeds k, then w is not feasible.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 1 <= k <= n
 * - 1 <= start[i] < end[i] <= 10^9
 * - start is strictly increasing
 * - end is strictly increasing
 *
 * Example 1:
 * start = [10, 20, 35], end = [15, 30, 40], k = 2
 * Answer = 10
 *
 * Example 2:
 * start = [5, 8, 14, 20], end = [6, 12, 18, 22], k = 2
 * Answer = 6
 */

public class Solution {

    /**
     * Computes the maximum integer warmup time w such that all meetings can still be scheduled
     * using at most k rooms.
     *
     * The key observation is monotonicity:
     * - Smaller warmup times are easier to schedule.
     * - Larger warmup times are harder to schedule.
     *
     * So we binary search on w.
     *
     * Upper bound:
     * A safe and tight enough upper bound is start[n - 1] - start[0].
     * If w exceeds this value, then even the earliest and latest starts are shifted enough that
     * overlap only increases further; this bound is sufficient for binary search because the answer
     * in all meaningful cases cannot exceed the spread of start times needed to create new overlap.
     *
     * More generally, using 1e9 is also safe, but this tighter bound reduces iterations slightly.
     *
     * @param start strictly increasing meeting start times
     * @param end strictly increasing meeting end times
     * @param k maximum number of available rooms
     * @return the largest feasible integer warmup time
     * Time complexity: O(n log n + n log U), which simplifies to O(n log U) because each feasibility
     *                  check is O(n log n) with a heap whose size is at most n, and U is the search range.
     *                  More precisely, each check is O(n log k') where k' is the active overlap count.
     * Space complexity: O(n) in the worst case for the priority queue during a feasibility check.
     */
    public int maximumWarmupTime(int[] start, int[] end, int k) {
        validateInput(start, end, k);

        int n = start.length;

        if (n == 0) {
            return 0;
        }

        /*
         * Binary search range:
         *
         * low  = definitely feasible candidate lower bound
         * high = search upper bound
         *
         * We search for the largest feasible w.
         */
        long low = 0L;
        long high = (long) start[n - 1] - start[0];

        /*
         * Standard "largest true" binary search:
         * - if mid is feasible, move right
         * - otherwise move left
         */
        while (low < high) {
            long mid = low + (high - low + 1) / 2;

            if (isFeasible(start, end, k, mid)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return (int) low;
    }

    /**
     * Checks whether a given warmup time w is feasible using at most k rooms.
     *
     * Each meeting occupies the closed interval [start[i] - w, end[i]].
     * Because intervals are closed, two meetings overlap if one ends exactly when another begins.
     * Therefore, a room becomes reusable only if:
     * previousEnd < currentBegin
     *
     * We process meetings in increasing order and maintain a min-heap of end times for rooms that
     * are currently occupied.
     *
     * Detailed sweep logic:
     * 1. Compute currentBegin = start[i] - w.
     * 2. Remove all meetings from the heap whose end time is strictly less than currentBegin.
     *    Those rooms are now free before this meeting starts occupying its room.
     * 3. Add current meeting's end time to the heap.
     * 4. If heap size exceeds k, then more than k rooms are simultaneously needed.
     *
     * @param start strictly increasing meeting start times
     * @param end strictly increasing meeting end times
     * @param k maximum number of available rooms
     * @param w candidate warmup time to test
     * @return true if all meetings can be scheduled with at most k rooms, false otherwise
     * Time complexity: O(n log n) in the worst case due to heap operations across n meetings
     * Space complexity: O(n) in the worst case for the heap
     */
    public boolean isFeasible(int[] start, int[] end, int k, long w) {
        /*
         * Min-heap storing end times of meetings currently occupying rooms.
         * The smallest end time is on top, which lets us quickly free rooms that finish earliest.
         */
        PriorityQueue<Integer> activeRoomEndTimes = new PriorityQueue<>();

        for (int i = 0; i < start.length; i++) {
            long currentBegin = (long) start[i] - w;

            /*
             * Free every room whose current meeting ends strictly before currentBegin.
             *
             * Why strictly before?
             * The occupied interval is closed: [start[i] - w, end[i]].
             * If previousEnd == currentBegin, then the two intervals share that exact point,
             * so they still overlap and cannot use the same room.
             */
            while (!activeRoomEndTimes.isEmpty() && activeRoomEndTimes.peek() < currentBegin) {
                activeRoomEndTimes.poll();
            }

            /*
             * Assign a room to the current meeting by adding its end time to the active set.
             * Conceptually, this means one room is occupied until end[i].
             */
            activeRoomEndTimes.offer(end[i]);

            /*
             * If the number of simultaneously occupied rooms exceeds k,
             * then this warmup time is not feasible.
             */
            if (activeRoomEndTimes.size() > k) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the input according to the problem constraints.
     * This method is mainly for safety and beginner-friendliness in a runnable demo program.
     *
     * @param start strictly increasing meeting start times
     * @param end strictly increasing meeting end times
     * @param k maximum number of available rooms
     * @return nothing
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public void validateInput(int[] start, int[] end, int k) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end arrays must not be null.");
        }

        if (start.length != end.length) {
            throw new IllegalArgumentException("start and end arrays must have the same length.");
        }

        if (start.length == 0) {
            throw new IllegalArgumentException("There must be at least one meeting.");
        }

        if (k < 1 || k > start.length) {
            throw new IllegalArgumentException("k must satisfy 1 <= k <= n.");
        }

        for (int i = 0; i < start.length; i++) {
            if (start[i] >= end[i]) {
                throw new IllegalArgumentException("Each meeting must satisfy start[i] < end[i].");
            }

            if (i > 0) {
                if (start[i] <= start[i - 1]) {
                    throw new IllegalArgumentException("start array must be strictly increasing.");
                }
                if (end[i] <= end[i - 1]) {
                    throw new IllegalArgumentException("end array must be strictly increasing.");
                }
            }
        }
    }

    /**
     * Helper method to print one test case and its computed answer.
     *
     * @param start meeting start times
     * @param end meeting end times
     * @param k number of rooms
     * @return nothing
     * Time complexity: O(n log U) because it calls the main algorithm
     * Space complexity: O(n) in the worst case
     */
    public void runDemo(int[] start, int[] end, int k) {
        System.out.println("start = " + Arrays.toString(start));
        System.out.println("end   = " + Arrays.toString(end));
        System.out.println("k     = " + k);
        System.out.println("maximum warmup time = " + maximumWarmupTime(start, end, k));
        System.out.println();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1 -> 10
     * - Example 2 -> 6
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size * log U) for the demo cases
     * Space complexity: O(n) per case in the worst case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] start1 = {10, 20, 35};
        int[] end1 = {15, 30, 40};
        int k1 = 2;
        solution.runDemo(start1, end1, k1);

        int[] start2 = {5, 8, 14, 20};
        int[] end2 = {6, 12, 18, 22};
        int k2 = 2;
        solution.runDemo(start2, end2, k2);

        /*
         * Quick internal verification of the examples:
         *
         * Example 1:
         * w = 10 -> feasible
         * w = 11 -> not feasible
         * so answer is 10
         */
        System.out.println("Example 1 verification:");
        System.out.println("w = 10 feasible? " + solution.isFeasible(start1, end1, k1, 10));
        System.out.println("w = 11 feasible? " + solution.isFeasible(start1, end1, k1, 11));
        System.out.println();

        /*
         * Example 2:
         * w = 6 -> feasible
         * w = 7 -> not feasible
         * so answer is 6
         */
        System.out.println("Example 2 verification:");
        System.out.println("w = 6 feasible? " + solution.isFeasible(start2, end2, k2, 6));
        System.out.println("w = 7 feasible? " + solution.isFeasible(start2, end2, k2, 7));
    }
}