import java.util.*;

/*
Problem Title: Maximum Launch Delay Before Missing Any Ferry Window

Problem Description:
A research team must transport equipment through a sequence of islands. There are n ferry crossings
that must be taken in order. For crossing i, ferries depart every interval[i] minutes starting at
time 0, and the team needs travel[i] minutes to complete that crossing once they board.

The team may wait at any island for the next ferry, but they are allowed to delay their initial
launch from the mainland by some non-negative integer number of minutes D. After that, all movement
is deterministic: for each crossing, if they arrive at time t, they board the earliest ferry
departing at or after t, then spend travel[i] minutes on that crossing.

You are also given a hard deadline T: the team must finish the final crossing no later than T.
Return the maximum integer launch delay D such that the team can still complete all crossings by
time T. If even launching at time 0 misses the deadline, return -1.

This problem is designed for a binary-search-on-answer solution. The key observation is that
feasibility is monotonic: if a delay D works, then any smaller delay also works. For a fixed D,
simulate the route by repeatedly rounding the current time up to the next multiple of interval[i],
then adding travel[i].

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= interval[i] <= 10^9
- 1 <= travel[i] <= 10^9
- 1 <= T <= 10^18
- All answers and intermediate times may require 64-bit integers.

Important correctness note:
The sample explanation in the prompt contains arithmetic inconsistencies. The correct simulation rule
is exactly the one stated in the problem: at each crossing, round the current time up to the next
multiple of interval[i], then add travel[i]. This implementation follows that rule precisely.
*/

public class Solution {

    /**
     * Computes the maximum non-negative integer launch delay D such that the team can still finish
     * all ferry crossings by time T.
     *
     * The method first checks whether delay 0 is feasible. If not, the answer is -1.
     * Otherwise, it performs binary search on D because feasibility is monotonic:
     * if some delay D works, then every smaller delay also works.
     *
     * @param interval interval[i] is the ferry departure period for crossing i
     * @param travel travel[i] is the crossing duration for crossing i after boarding
     * @param T the hard deadline by which the final crossing must be completed
     * @return the maximum feasible integer launch delay, or -1 if even delay 0 is infeasible
     * Time complexity: O(n log T)
     * Space complexity: O(1) extra space
     */
    public long maximumLaunchDelay(int[] interval, int[] travel, long T) {
        int n = interval.length;

        // Basic sanity check for beginner-friendliness.
        if (n != travel.length) {
            throw new IllegalArgumentException("interval and travel must have the same length.");
        }

        // If starting immediately already misses the deadline, no answer exists.
        if (!canFinish(interval, travel, T, 0L)) {
            return -1L;
        }

        // The delay can never need to exceed T:
        // if D > T, then the team starts after the deadline itself, and since travel times are positive,
        // finishing by T is impossible.
        long left = 0L;
        long right = T;
        long answer = 0L;

        // Standard "find maximum feasible value" binary search.
        while (left <= right) {
            long mid = left + (right - left) / 2;

            if (canFinish(interval, travel, T, mid)) {
                // mid works, so record it and try to push to a larger delay.
                answer = mid;
                left = mid + 1;
            } else {
                // mid does not work, so all larger values also fail.
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether launching with delay D allows the team to finish all crossings by time T.
     *
     * Simulation details:
     * 1. Current time starts at D.
     * 2. For each crossing:
     *    - If current time is already a multiple of interval[i], they can board immediately.
     *    - Otherwise, they wait until the next multiple of interval[i].
     *    - Then they spend travel[i] minutes crossing.
     * 3. If the final time is <= T, the delay is feasible.
     *
     * This method also performs careful early stopping:
     * if the current time already exceeds T at any point, we can immediately return false.
     *
     * @param interval interval[i] is the ferry departure period for crossing i
     * @param travel travel[i] is the crossing duration for crossing i after boarding
     * @param T the hard deadline
     * @param D the proposed initial launch delay
     * @return true if delay D is feasible, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean canFinish(int[] interval, int[] travel, long T, long D) {
        long currentTime = D;

        // Process each crossing in order.
        for (int i = 0; i < interval.length; i++) {
            long period = interval[i];
            long crossingTime = travel[i];

            // Step 1: round currentTime up to the next multiple of period.
            //
            // We use the safe arithmetic form:
            // nextDeparture = ((currentTime + period - 1) / period) * period
            //
            // This computes the smallest multiple of period that is >= currentTime.
            long nextDeparture = roundUpToNextMultiple(currentTime, period);

            // Step 2: board that ferry and add the crossing duration.
            currentTime = nextDeparture + crossingTime;

            // Early exit:
            // If we already exceeded the deadline, no need to continue simulating.
            if (currentTime > T) {
                return false;
            }
        }

        // If we finished all crossings and never exceeded T, the delay is feasible.
        return currentTime <= T;
    }

    /**
     * Rounds a non-negative time value up to the smallest multiple of period that is at least time.
     *
     * Examples:
     * - time = 0, period = 5  => 0
     * - time = 4, period = 5  => 5
     * - time = 10, period = 5 => 10
     * - time = 11, period = 5 => 15
     *
     * @param time the current time
     * @param period the ferry departure interval
     * @return the earliest departure time that is a multiple of period and >= time
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long roundUpToNextMultiple(long time, long period) {
        return ((time + period - 1) / period) * period;
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The prompt's sample arithmetic is inconsistent with the stated simulation rule.
     * This main method prints the results produced by the correct rule:
     * repeatedly round up to the next multiple of interval[i], then add travel[i].
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n log T) across the demonstrated calls
     * Space complexity: O(1) extra space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] interval1 = {5, 7, 3};
        int[] travel1 = {4, 2, 5};
        long T1 = 25L;
        long result1 = solution.maximumLaunchDelay(interval1, travel1, T1);
        System.out.println("Example 1 result: " + result1);

        int[] interval2 = {6, 10};
        int[] travel2 = {7, 8};
        long T2 = 14L;
        long result2 = solution.maximumLaunchDelay(interval2, travel2, T2);
        System.out.println("Example 2 result: " + result2);

        // Additional quick checks for clarity.
        int[] interval3 = {2, 3};
        int[] travel3 = {1, 1};
        long T3 = 10L;
        long result3 = solution.maximumLaunchDelay(interval3, travel3, T3);
        System.out.println("Additional example result: " + result3);
    }
}