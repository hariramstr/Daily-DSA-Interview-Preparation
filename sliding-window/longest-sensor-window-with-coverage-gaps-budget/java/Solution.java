import java.util.*;

/*
 * Title: Longest Sensor Window With Coverage Gaps Budget
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a strictly increasing array times of length n, where times[i]
 * is the timestamp (in seconds) of the i-th sensor report from a remote device.
 * A monitoring dashboard wants to display the longest contiguous block of reports
 * that can be considered a "continuous observation window". However, the device
 * may occasionally go silent between two consecutive reports.
 *
 * For any contiguous subarray times[l..r], define its coverage gap cost as the
 * sum of all missing seconds between adjacent reports inside the window:
 *
 * cost(l, r) = sum(max(0, times[i] - times[i-1] - 1)) for all i from l+1 to r.
 *
 * In other words, if two adjacent reports happen at seconds 10 and 13, then
 * seconds 11 and 12 are missing, contributing 2 to the cost. You are given an
 * integer budget, representing the maximum total number of missing seconds that
 * may be tolerated inside the chosen window.
 *
 * Return the maximum number of reports in any contiguous window whose coverage
 * gap cost is at most budget.
 *
 * A window containing only one report has cost 0.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 0 <= times[i] <= 10^18
 * - times is strictly increasing
 * - 0 <= budget <= 10^18
 *
 * Example 1:
 * Input: times = [2, 3, 7, 8, 10], budget = 3
 * Output: 4
 * Explanation:
 * The window [2, 3, 7, 8] has gaps (0 + 3 + 0) = 3, so its cost fits the budget
 * and its length is 4. The full window [2, 3, 7, 8, 10] has cost
 * 0 + 3 + 0 + 1 = 4, which exceeds the budget.
 *
 * Example 2:
 * Input: times = [1, 5, 6, 9, 15, 16], budget = 5
 * Output: 3
 * Explanation:
 * Window [1, 5, 6] has cost 3 + 0 = 3, and window [5, 6, 9] has cost 0 + 2 = 2.
 * Any valid window of length 4 or more exceeds the allowed total missing seconds.
 */

public class Solution {

    /**
     * Computes the maximum number of reports in any contiguous window whose total
     * coverage gap cost is at most the given budget.
     *
     * The key observation is:
     * - For each adjacent pair times[i - 1], times[i], the contribution to the cost is:
     *   times[i] - times[i - 1] - 1
     * - Because the array is strictly increasing, this value is always >= 0.
     * - Therefore, the window cost is simply the sum of these adjacent gap values
     *   inside the window.
     *
     * This allows a classic sliding window:
     * - Expand the right end one report at a time.
     * - Add the newly introduced adjacent gap to the running cost.
     * - If the cost exceeds budget, move the left end rightward until the window
     *   becomes valid again.
     * - Track the maximum valid window length seen.
     *
     * @param times the strictly increasing timestamps of sensor reports
     * @param budget the maximum allowed total number of missing seconds inside the window
     * @return the maximum size of a contiguous valid window
     * Time complexity: O(n), because each pointer moves at most n times
     * Space complexity: O(1), ignoring input storage
     */
    public int longestSensorWindow(long[] times, long budget) {
        int n = times.length;

        // A single report always forms a valid window with cost 0.
        if (n == 0) {
            return 0;
        }

        // left = left boundary of the current sliding window
        int left = 0;

        // currentCost = total gap cost inside the current window [left..right]
        long currentCost = 0L;

        // best = maximum valid window length found so far
        int best = 1;

        // We expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < n; right++) {

            // When right > 0, adding times[right] to the window introduces exactly one
            // new adjacent pair: (times[right - 1], times[right]).
            //
            // Its missing-seconds contribution is:
            // times[right] - times[right - 1] - 1
            //
            // Example:
            // if timestamps are 7 and 10, then missing seconds are 8 and 9 => contribution 2.
            if (right > 0) {
                long addedGap = times[right] - times[right - 1] - 1;
                currentCost += addedGap;
            }

            // If the current window cost exceeds the budget, we must shrink from the left.
            //
            // Important detail:
            // When we remove times[left] from the window, the only adjacent gap that disappears
            // is the one between times[left] and times[left + 1].
            //
            // So we subtract:
            // times[left + 1] - times[left] - 1
            //
            // We keep shrinking until the window becomes valid again.
            while (currentCost > budget && left < right) {
                long removedGap = times[left + 1] - times[left] - 1;
                currentCost -= removedGap;
                left++;
            }

            // Now [left..right] is guaranteed to be valid (cost <= budget),
            // so we can update the best answer.
            int windowLength = right - left + 1;
            if (windowLength > best) {
                best = windowLength;
            }
        }

        return best;
    }

    /**
     * Convenience overload that accepts an int array and internally converts it to long[].
     * This is useful for small demonstrations and beginner-friendly testing.
     *
     * @param times the strictly increasing timestamps of sensor reports as int values
     * @param budget the maximum allowed total number of missing seconds inside the window
     * @return the maximum size of a contiguous valid window
     * Time complexity: O(n), because conversion is O(n) and sliding window is O(n)
     * Space complexity: O(n), due to the temporary long[] conversion
     */
    public int longestSensorWindow(int[] times, long budget) {
        long[] converted = new long[times.length];
        for (int i = 0; i < times.length; i++) {
            converted[i] = times[i];
        }
        return longestSensorWindow(converted, budget);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Verified manually:
     *
     * Example 1:
     * times = [2, 3, 7, 8, 10], budget = 3
     * Adjacent gaps:
     * 3-2-1 = 0
     * 7-3-1 = 3
     * 8-7-1 = 0
     * 10-8-1 = 1
     * Best valid window length = 4
     *
     * Example 2:
     * times = [1, 5, 6, 9, 15, 16], budget = 5
     * Adjacent gaps:
     * 5-1-1 = 3
     * 6-5-1 = 0
     * 9-6-1 = 2
     * 15-9-1 = 5
     * 16-15-1 = 0
     * Best valid window length = 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo calls, excluding the called algorithm
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] times1 = {2, 3, 7, 8, 10};
        long budget1 = 3;
        int result1 = solution.longestSensorWindow(times1, budget1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 4");

        int[] times2 = {1, 5, 6, 9, 15, 16};
        long budget2 = 5;
        int result2 = solution.longestSensorWindow(times2, budget2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 3");

        long[] times3 = {10L};
        long budget3 = 0L;
        int result3 = solution.longestSensorWindow(times3, budget3);
        System.out.println("Single report example result: " + result3);
        System.out.println("Expected: 1");

        long[] times4 = {0L, 1L, 2L, 3L, 4L};
        long budget4 = 0L;
        int result4 = solution.longestSensorWindow(times4, budget4);
        System.out.println("No gaps example result: " + result4);
        System.out.println("Expected: 5");
    }
}