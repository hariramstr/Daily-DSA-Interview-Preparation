import java.util.*;

/*
 * Title: Earliest Day to Activate K Sensor Corridors
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A research facility has deployed sensors along a long hallway. There are n sensors in a fixed
 * left-to-right order, and sensor i becomes operational on day activationDay[i]. A corridor is
 * defined as a contiguous block of sensors in this order. A corridor is considered valid on day D
 * if every sensor inside that block is operational by day D, and the length of the block is at
 * least minLen and at most maxLen.
 *
 * The facility wants to activate at least k non-overlapping valid corridors as early as possible.
 * Two corridors are non-overlapping if they do not share any sensor index. You may choose any
 * corridor lengths within the allowed range [minLen, maxLen], and you do not need to use all
 * operational sensors.
 *
 * Return the earliest day D such that it is possible to select at least k non-overlapping valid
 * corridors. If it is impossible even after all sensors have become operational, return -1.
 *
 * This problem is designed to reward a binary search on the answer. For a fixed day D, each sensor
 * can be treated as either active or inactive, and the challenge is to determine whether at least
 * k disjoint valid segments can be formed from the active runs. A correct solution must handle
 * large inputs efficiently.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= activationDay[i] <= 1000000000
 * - 1 <= k <= n
 * - 1 <= minLen <= maxLen <= n
 * - activationDay.length == n
 *
 * Key Observation:
 * For a fixed day D, each sensor is either active (activationDay[i] <= D) or inactive.
 * The active sensors form runs of consecutive active positions.
 *
 * Inside one active run of length L, the maximum number of non-overlapping valid corridors is:
 *     floor(L / minLen)
 * whenever L >= minLen.
 *
 * Why is maxLen irrelevant for maximizing the count inside a run?
 * Because every chosen corridor must have length at least minLen. To maximize the number of
 * corridors, we should use the shortest allowed length, namely minLen. Since minLen is within
 * [minLen, maxLen], using length minLen is always valid. Therefore, from a run of length L,
 * we can always carve out floor(L / minLen) disjoint corridors of length exactly minLen.
 *
 * So for a given day D:
 * 1. Scan the array and compute lengths of active runs.
 * 2. Sum floor(runLength / minLen) over all runs.
 * 3. If the total is at least k, then day D is feasible.
 *
 * Since feasibility is monotonic in D (if a day works, every later day also works),
 * we can binary search the minimum feasible day.
 */

public class Solution {

    /**
     * Returns the earliest day on which at least k non-overlapping valid corridors can be formed.
     *
     * The algorithm uses binary search on the answer (the day).
     *
     * Step-by-step idea:
     * 1. If even after all sensors are active we still cannot form k corridors, return -1.
     * 2. Otherwise, binary search the minimum day D such that feasibility(D) is true.
     * 3. Feasibility(D) is checked in O(n) by scanning active runs and summing floor(runLength / minLen).
     *
     * @param activationDay activationDay[i] is the day sensor i becomes operational
     * @param k the minimum number of non-overlapping valid corridors required
     * @param minLen the minimum allowed corridor length
     * @param maxLen the maximum allowed corridor length
     * @return the earliest feasible day, or -1 if impossible
     *
     * Time complexity: O(n log M), where M is the range of activation days
     * Space complexity: O(1) extra space
     */
    public int earliestDay(int[] activationDay, int k, int minLen, int maxLen) {
        int n = activationDay.length;

        // Quick impossible check based on total number of sensors.
        // Since every corridor must use at least minLen sensors, k corridors need at least k * minLen sensors.
        // Use long to avoid overflow in multiplication.
        if ((long) k * minLen > n) {
            return -1;
        }

        int minDay = Integer.MAX_VALUE;
        int maxDay = Integer.MIN_VALUE;

        // Determine the binary search range from the input days.
        for (int day : activationDay) {
            minDay = Math.min(minDay, day);
            maxDay = Math.max(maxDay, day);
        }

        // If even on the latest day (when all sensors that will ever activate are active)
        // we cannot form k corridors, then the answer is impossible.
        if (!canFormAtLeastK(activationDay, k, minLen, maxLen, maxDay)) {
            return -1;
        }

        int left = minDay;
        int right = maxDay;
        int answer = maxDay;

        // Standard binary search for the first true value in a monotonic predicate.
        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (canFormAtLeastK(activationDay, k, minLen, maxLen, mid)) {
                // mid works, so record it and try to find an even earlier feasible day.
                answer = mid;
                right = mid - 1;
            } else {
                // mid does not work, so we must search later days.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether by the given day we can form at least k non-overlapping valid corridors.
     *
     * Detailed reasoning:
     * - A sensor is active on 'day' if activationDay[i] <= day.
     * - Consecutive active sensors form an active run.
     * - From a run of length L, the maximum number of non-overlapping valid corridors is floor(L / minLen),
     *   because we can simply take corridors of length exactly minLen.
     * - This is valid because minLen is allowed, and using the shortest allowed length maximizes the count.
     *
     * Example:
     * If minLen = 2 and a run has length 7, then we can take:
     * [2 sensors] + [2 sensors] + [2 sensors], leaving 1 unused sensor.
     * So the contribution is floor(7 / 2) = 3.
     *
     * @param activationDay activation days of sensors
     * @param k required number of corridors
     * @param minLen minimum allowed corridor length
     * @param maxLen maximum allowed corridor length
     * @param day the day being tested
     * @return true if at least k corridors can be formed by this day, otherwise false
     *
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean canFormAtLeastK(int[] activationDay, int k, int minLen, int maxLen, int day) {
        long corridors = 0L;
        int currentRunLength = 0;

        // Scan from left to right and build lengths of active runs.
        for (int value : activationDay) {
            if (value <= day) {
                // This sensor is active on the tested day, so it extends the current active run.
                currentRunLength++;
            } else {
                // This sensor is inactive, so the current active run ends here.
                // Convert the completed run into as many corridors as possible.
                corridors += currentRunLength / minLen;

                // Early exit: once we already have enough corridors, no need to continue scanning.
                if (corridors >= k) {
                    return true;
                }

                // Reset for the next run.
                currentRunLength = 0;
            }
        }

        // The array may end while we are still inside an active run, so process the final run too.
        corridors += currentRunLength / minLen;

        return corridors >= k;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) extra space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] activationDay1 = {4, 2, 5, 3, 3, 6, 1};
        int k1 = 2;
        int minLen1 = 2;
        int maxLen1 = 3;
        System.out.println(solution.earliestDay(activationDay1, k1, minLen1, maxLen1)); // Expected: 4

        int[] activationDay2 = {7, 7, 7};
        int k2 = 2;
        int minLen2 = 2;
        int maxLen2 = 2;
        System.out.println(solution.earliestDay(activationDay2, k2, minLen2, maxLen2)); // Expected: -1

        int[] activationDay3 = {7, 7, 7, 7, 7};
        int k3 = 2;
        int minLen3 = 2;
        int maxLen3 = 2;
        System.out.println(solution.earliestDay(activationDay3, k3, minLen3, maxLen3)); // Expected: 7
    }
}