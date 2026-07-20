import java.util.*;

/*
 * Title: Minimum Warmup Time for Shared Conference Rooms
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A company has n meetings that must be held in the given order during a single day.
 * The i-th meeting requires rooms[i] identical conference rooms at the same time and
 * lasts for durations[i] minutes. Before a room can host a meeting, it must be warmed
 * up for W minutes. Once warmed, a room stays available for the rest of the day and
 * can be reused by later meetings without additional warmup.
 *
 * All room warmups for a meeting must finish before that meeting starts. Meetings cannot
 * overlap, but you are allowed to insert idle time between consecutive meetings. The company
 * wants to know the minimum integer warmup time W such that all meetings can be completed
 * within totalTime minutes.
 *
 * More formally, if a meeting needs r rooms and only x rooms have already been warmed before
 * it starts, then max(0, r - x) new rooms must be warmed, costing W minutes regardless of how
 * many rooms are warmed in parallel, because the building system warms all newly opened rooms
 * together in one batch. After that meeting, at least r rooms remain warmed for the rest of the day.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 1 <= rooms[i] <= 10^9
 * - 1 <= durations[i] <= 10^9
 * - 1 <= totalTime <= 10^18
 * - The answer is guaranteed to fit in a 64-bit signed integer.
 *
 * Important note about the examples:
 * The natural feasibility condition is:
 *     totalDuration + batches * W <= totalTime
 * where "batches" is the number of times the running maximum of rooms increases.
 *
 * Under that condition, larger W makes scheduling harder, not easier. Therefore, the mathematically
 * correct optimization target is the maximum feasible W, not the minimum feasible W, because W = 0
 * would always be feasible whenever the durations alone fit in totalTime.
 *
 * The provided examples and the statement's "binary search on the answer" hint clearly align with
 * finding the largest feasible W:
 * - Example 1: answer is 3 because 12 + 2 * 3 = 18, and any larger W fails.
 * - Example 2: answer should be 4 because 15 + 2 * 4 = 23 <= 24, while 5 fails.
 *
 * To keep the implementation correct and consistent with the actual feasibility rule, this solution
 * computes the maximum feasible integer warmup time W.
 */

public class Solution {

    /**
     * Computes the maximum feasible integer warmup time W such that all meetings can be completed
     * within totalTime.
     *
     * The key observation is:
     * 1. Meeting durations always contribute their full sum.
     * 2. A warmup batch is needed exactly when the running maximum of required rooms increases.
     * 3. If there are B such increases, total schedule time is:
     *        sum(durations) + B * W
     * 4. We need:
     *        sum(durations) + B * W <= totalTime
     *
     * Because feasibility becomes harder as W increases, we can binary search for the largest W
     * that still satisfies the inequality.
     *
     * @param rooms the number of rooms required by each meeting, in order
     * @param durations the duration of each meeting, in order
     * @param totalTime the total available time for the whole day
     * @return the largest feasible integer warmup time W
     * Time complexity: O(n + log answer)
     * Space complexity: O(1)
     */
    public long minimumWarmupTime(int[] rooms, int[] durations, long totalTime) {
        return maximumFeasibleWarmupTime(rooms, durations, totalTime);
    }

    /**
     * Computes the largest feasible integer warmup time W.
     *
     * This method is the mathematically correct interpretation of the scheduling rule.
     *
     * @param rooms the number of rooms required by each meeting, in order
     * @param durations the duration of each meeting, in order
     * @param totalTime the total available time for the whole day
     * @return the largest feasible integer warmup time W
     * Time complexity: O(n + log answer)
     * Space complexity: O(1)
     */
    public long maximumFeasibleWarmupTime(int[] rooms, int[] durations, long totalTime) {
        validateInput(rooms, durations);

        long durationSum = sumDurations(durations);
        long batches = countWarmupBatches(rooms);

        if (durationSum > totalTime) {
            return -1L;
        }

        if (batches == 0) {
            return Long.MAX_VALUE;
        }

        long low = 0L;
        long high = (totalTime - durationSum) / batches;
        long answer = 0L;

        while (low <= high) {
            long mid = low + ((high - low) >>> 1);

            if (canFinishWithinTime(rooms, durations, totalTime, mid)) {
                answer = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a candidate warmup time W is feasible.
     *
     * Step-by-step logic:
     * 1. Add all meeting durations.
     * 2. Scan meetings from left to right.
     * 3. Track the largest room requirement seen so far.
     * 4. Every time the current meeting requires more rooms than ever before,
     *    we must perform one new warmup batch before that meeting.
     * 5. Each such batch costs exactly W minutes.
     * 6. The candidate is feasible if total duration + batchCount * W <= totalTime.
     *
     * @param rooms the number of rooms required by each meeting
     * @param durations the duration of each meeting
     * @param totalTime the total available time
     * @param warmupTime the candidate W to test
     * @return true if the candidate W is feasible, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canFinishWithinTime(int[] rooms, int[] durations, long totalTime, long warmupTime) {
        long elapsed = 0L;

        for (int duration : durations) {
            elapsed += duration;
            if (elapsed > totalTime) {
                return false;
            }
        }

        long warmedRoomsSoFar = 0L;

        for (int requiredRooms : rooms) {
            if (requiredRooms > warmedRoomsSoFar) {
                elapsed += warmupTime;
                if (elapsed > totalTime) {
                    return false;
                }
                warmedRoomsSoFar = requiredRooms;
            }
        }

        return elapsed <= totalTime;
    }

    /**
     * Counts how many warmup batches are required.
     *
     * A batch is required exactly when the running maximum of room requirements increases.
     *
     * Example:
     * rooms = [2, 5, 3]
     * running maxima = 2, 5, 5
     * increases happen at 2 and 5, so the answer is 2 batches.
     *
     * @param rooms the room requirements in meeting order
     * @return the number of warmup batches
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long countWarmupBatches(int[] rooms) {
        long batches = 0L;
        long warmedRoomsSoFar = 0L;

        for (int requiredRooms : rooms) {
            if (requiredRooms > warmedRoomsSoFar) {
                batches++;
                warmedRoomsSoFar = requiredRooms;
            }
        }

        return batches;
    }

    /**
     * Sums all meeting durations using long arithmetic to avoid overflow.
     *
     * @param durations the meeting durations
     * @return the total duration
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long sumDurations(int[] durations) {
        long sum = 0L;
        for (int duration : durations) {
            sum += duration;
        }
        return sum;
    }

    /**
     * Validates that the two input arrays are non-null and have the same length.
     *
     * @param rooms the room requirements array
     * @param durations the durations array
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] rooms, int[] durations) {
        if (rooms == null || durations == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (rooms.length != durations.length) {
            throw new IllegalArgumentException("rooms and durations must have the same length.");
        }
        if (rooms.length == 0) {
            throw new IllegalArgumentException("Input arrays must not be empty.");
        }
    }

    /**
     * Demonstrates the solution on sample inputs.
     *
     * Note:
     * - For Example 1, the correct result under the stated feasibility rule is 3.
     * - For Example 2, the correct result under the stated feasibility rule is 4.
     *   The problem statement says 3, but its own arithmetic shows 4 is still feasible.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n) for the demonstrated examples
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] rooms1 = {2, 5, 3};
        int[] durations1 = {4, 6, 2};
        long totalTime1 = 18L;
        System.out.println(solution.minimumWarmupTime(rooms1, durations1, totalTime1)); // 3

        int[] rooms2 = {4, 1, 4, 7};
        int[] durations2 = {5, 2, 5, 3};
        long totalTime2 = 24L;
        System.out.println(solution.minimumWarmupTime(rooms2, durations2, totalTime2)); // 4

        System.out.println(solution.canFinishWithinTime(rooms1, durations1, totalTime1, 3L)); // true
        System.out.println(solution.canFinishWithinTime(rooms1, durations1, totalTime1, 4L)); // false
    }
}