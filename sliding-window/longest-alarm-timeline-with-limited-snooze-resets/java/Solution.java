import java.util.*;

/*
 * Title: Longest Alarm Timeline With Limited Snooze Resets
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A productivity app records a user's wake-up behavior over several days. For each day, the app stores
 * an integer in an array alarms, where alarms[i] is the alarm label used on day i. Equal values mean
 * the same exact alarm sound was used again.
 *
 * The app considers a contiguous block of days to be a valid timeline if no alarm label appears more
 * than limit times inside that block. However, the app is allowed to apply up to k snooze resets inside
 * the chosen block. A snooze reset can be assigned to any single day in the block and makes that day
 * exempt from the frequency rule, meaning its alarm label does not count toward the limit for that label.
 * Each day can use at most one reset.
 *
 * Return the length of the longest contiguous subarray that can be made valid using at most k snooze resets.
 *
 * In other words, for a chosen window, if an alarm label appears f times, then at least max(0, f - limit)
 * of those occurrences must be covered by snooze resets. The total number of required resets across all
 * labels in the window must be at most k.
 *
 * Design an algorithm efficient enough for large inputs.
 *
 * Constraints:
 * - 1 <= alarms.length <= 200000
 * - 1 <= alarms[i] <= 10^9
 * - 0 <= k <= alarms.length
 * - 1 <= limit <= alarms.length
 *
 * Example 1:
 * Input: alarms = [5, 1, 5, 2, 5, 1, 1], limit = 2, k = 1
 * Output: 5
 * Explanation: The subarray [5, 1, 5, 2, 5] has counts {5: 3, 1: 1, 2: 1}. Since label 5 exceeds the
 * limit by 1, one snooze reset is enough, so this window is valid. No longer valid window exists.
 *
 * Example 2:
 * Input: alarms = [4, 4, 4, 3, 3, 4, 3, 3], limit = 1, k = 3
 * Output: 5
 * Explanation: Consider [4, 4, 3, 3, 4]. The counts are {4: 3, 3: 2}. To make every label appear at
 * most once, we need (3 - 1) + (2 - 1) = 3 snooze resets, which is allowed. A length-6 window would
 * require at least 4 resets, so the answer is 5.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray that can be made valid
     * using at most k snooze resets.
     *
     * Core idea:
     * For any current sliding window, if a value appears f times, then it contributes
     * max(0, f - limit) required resets.
     *
     * Therefore, the total resets needed for the whole window is:
     * sum over all distinct values of max(0, frequency[value] - limit)
     *
     * A window is valid if this total is <= k.
     *
     * We maintain:
     * - a frequency map for the current window
     * - a running total "requiredResets"
     *
     * When we add one element x:
     * - let oldCount = frequency[x]
     * - let newCount = oldCount + 1
     * - if oldCount >= limit, then this new occurrence increases the excess by 1,
     *   so requiredResets++
     *
     * When we remove one element y from the left:
     * - let oldCount = frequency[y]
     * - if oldCount > limit, then removing one occurrence decreases the excess by 1,
     *   so requiredResets--
     * - then decrement the stored frequency
     *
     * This lets us update the validity of the window in O(1) average time per move.
     *
     * @param alarms the array of alarm labels for each day
     * @param limit the maximum allowed counted occurrences of any label inside a valid window
     * @param k the maximum number of snooze resets allowed in the chosen window
     * @return the maximum length of a contiguous subarray that can be made valid
     * @implNote Time complexity: O(n) average, where n is alarms.length
     * @implNote Space complexity: O(m), where m is the number of distinct values in the current array/window
     */
    public int longestAlarmTimeline(int[] alarms, int limit, int k) {
        // Frequency map for values inside the current sliding window [left, right].
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // Total number of snooze resets required to make the current window valid.
        // This equals:
        // sum(max(0, frequency[value] - limit)) over all values in the window.
        int requiredResets = 0;

        // Expand the window one element at a time using "right".
        for (int right = 0; right < alarms.length; right++) {
            int valueToAdd = alarms[right];

            // Get the old frequency before adding this new element.
            int oldCount = frequency.getOrDefault(valueToAdd, 0);

            // If oldCount is already at least "limit", then adding one more occurrence
            // creates one additional excess occurrence that must be covered by a reset.
            //
            // Example with limit = 2:
            // oldCount = 0 -> newCount = 1 : excess stays 0
            // oldCount = 1 -> newCount = 2 : excess stays 0
            // oldCount = 2 -> newCount = 3 : excess becomes 1  => requiredResets++
            // oldCount = 3 -> newCount = 4 : excess becomes 2  => requiredResets++
            if (oldCount >= limit) {
                requiredResets++;
            }

            // Store the incremented frequency.
            frequency.put(valueToAdd, oldCount + 1);

            // If the current window needs too many resets, it is invalid.
            // We must shrink it from the left until it becomes valid again.
            while (requiredResets > k) {
                int valueToRemove = alarms[left];
                int countBeforeRemoval = frequency.get(valueToRemove);

                // If countBeforeRemoval is strictly greater than limit, then this occurrence
                // is part of the excess. Removing it reduces requiredResets by 1.
                //
                // Example with limit = 2:
                // countBeforeRemoval = 4 -> after removal 3 : excess drops from 2 to 1
                // countBeforeRemoval = 3 -> after removal 2 : excess drops from 1 to 0
                // countBeforeRemoval = 2 -> after removal 1 : excess stays 0
                if (countBeforeRemoval > limit) {
                    requiredResets--;
                }

                // Decrease the frequency in the map.
                if (countBeforeRemoval == 1) {
                    frequency.remove(valueToRemove);
                } else {
                    frequency.put(valueToRemove, countBeforeRemoval - 1);
                }

                // Move the left boundary rightward.
                left++;
            }

            // At this point, the window [left, right] is valid:
            // requiredResets <= k
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A helper method that runs one demonstration case and prints the result.
     *
     * @param alarms the input alarm array
     * @param limit the per-label frequency limit before resets are needed
     * @param k the maximum allowed number of resets
     * @return the computed longest valid window length
     * @implNote Time complexity: O(n) average due to the call to longestAlarmTimeline
     * @implNote Space complexity: O(m) due to the call to longestAlarmTimeline
     */
    public int runDemo(int[] alarms, int limit, int k) {
        int result = longestAlarmTimeline(alarms, limit, k);
        System.out.println("alarms = " + Arrays.toString(alarms));
        System.out.println("limit = " + limit + ", k = " + k);
        System.out.println("Longest valid timeline length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and prints the outputs.
     *
     * Expected sample outputs:
     * - Example 1 -> 5
     * - Example 2 -> 5
     *
     * @param args command-line arguments (not used)
     * @implNote Time complexity: O(total input size of demonstrated examples)
     * @implNote Space complexity: O(distinct values in each demonstrated example)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] alarms1 = {5, 1, 5, 2, 5, 1, 1};
        int result1 = solution.runDemo(alarms1, 2, 1);
        System.out.println("Expected: 5, Actual: " + result1);
        System.out.println();

        // Example 2
        int[] alarms2 = {4, 4, 4, 3, 3, 4, 3, 3};
        int result2 = solution.runDemo(alarms2, 1, 3);
        System.out.println("Expected: 5, Actual: " + result2);
        System.out.println();

        // A few extra beginner-friendly sanity checks.

        // No resets needed because all frequencies already fit.
        int[] alarms3 = {1, 2, 3, 4};
        int result3 = solution.runDemo(alarms3, 1, 0);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println();

        // All same value, some resets available.
        int[] alarms4 = {7, 7, 7, 7, 7};
        int result4 = solution.runDemo(alarms4, 2, 2);
        System.out.println("Expected: 4, Actual: " + result4);
        System.out.println();

        // If limit is large enough, the whole array is valid.
        int[] alarms5 = {9, 9, 8, 8, 7};
        int result5 = solution.runDemo(alarms5, 10, 0);
        System.out.println("Expected: 5, Actual: " + result5);
    }
}