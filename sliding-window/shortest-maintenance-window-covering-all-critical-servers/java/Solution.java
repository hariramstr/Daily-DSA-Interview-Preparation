import java.util.*;

/*
 * Title: Shortest Maintenance Window Covering All Critical Servers
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A data center records a time-ordered stream of server IDs representing which server emitted
 * the most recent heartbeat at each second. You are given an integer array events, where
 * events[i] is the server ID seen at second i, and an integer array critical containing
 * distinct server IDs that must all be observed during a maintenance audit.
 *
 * Your task is to find the length of the shortest contiguous time window in events that
 * contains every server in critical at least once. However, there is an additional reliability
 * rule: within the chosen window, no non-critical server ID is allowed to appear more than L times.
 * If no such window exists, return -1.
 *
 * Formally, find the minimum value of (right - left + 1) such that the subarray
 * events[left...right] satisfies both conditions:
 * 1. Every server ID in critical appears at least once in the window.
 * 2. For every server ID x not in critical, its frequency inside the window is at most L.
 *
 * Constraints:
 * - 1 <= events.length <= 200000
 * - 1 <= critical.length <= min(100000, events.length)
 * - 1 <= events[i], critical[i] <= 10^9
 * - All values in critical are distinct
 * - 0 <= L <= events.length
 *
 * Efficient sliding window with frequency tracking is expected.
 */
public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray that:
     * 1) contains every critical server ID at least once, and
     * 2) does not contain any non-critical server ID more than L times.
     *
     * Core idea:
     * We use a classic two-pointer / sliding window technique.
     *
     * We expand the right pointer one step at a time and maintain:
     * - how many critical IDs are currently covered at least once,
     * - frequencies of critical IDs inside the window,
     * - frequencies of non-critical IDs inside the window,
     * - how many non-critical IDs currently violate the "frequency <= L" rule.
     *
     * A window is valid exactly when:
     * - all critical IDs are covered, and
     * - there are zero violating non-critical IDs.
     *
     * Once the window becomes valid, we greedily move the left pointer to shrink it as much
     * as possible while preserving validity. This guarantees that for each right endpoint,
     * we consider the shortest valid window ending there.
     *
     * @param events the time-ordered stream of server IDs
     * @param critical the distinct server IDs that must all appear in the chosen window
     * @param L the maximum allowed frequency for each non-critical server ID inside the window
     * @return the minimum valid window length, or -1 if no valid window exists
     *
     * Time complexity: O(n), where n = events.length, because each pointer moves at most n times.
     * Space complexity: O(c + u), where c = critical.length and u is the number of distinct
     * non-critical IDs that appear in the current/global processing maps.
     */
    public int shortestMaintenanceWindow(int[] events, int[] critical, int L) {
        int n = events.length;

        // Store all critical IDs in a set for O(1) membership checks.
        Set<Integer> criticalSet = new HashSet<>();
        for (int id : critical) {
            criticalSet.add(id);
        }

        // Frequency map for critical IDs currently inside the sliding window.
        Map<Integer, Integer> criticalFreq = new HashMap<>();

        // Frequency map for non-critical IDs currently inside the sliding window.
        Map<Integer, Integer> nonCriticalFreq = new HashMap<>();

        // Number of distinct critical IDs that are currently present in the window at least once.
        int coveredCritical = 0;

        // Total number of critical IDs that must be covered.
        int requiredCritical = critical.length;

        // Number of non-critical IDs whose frequency currently exceeds L.
        // If this value is 0, then the non-critical constraint is satisfied.
        int violatingNonCriticalKinds = 0;

        int left = 0;
        int answer = Integer.MAX_VALUE;

        // Expand the window by moving 'right' from left to right.
        for (int right = 0; right < n; right++) {
            int value = events[right];

            // Step 1: Add events[right] into the window and update bookkeeping.
            if (criticalSet.contains(value)) {
                // This is a critical ID.
                int newFreq = criticalFreq.getOrDefault(value, 0) + 1;
                criticalFreq.put(value, newFreq);

                // If its frequency became 1, this critical ID is now newly covered.
                if (newFreq == 1) {
                    coveredCritical++;
                }
            } else {
                // This is a non-critical ID.
                int newFreq = nonCriticalFreq.getOrDefault(value, 0) + 1;
                nonCriticalFreq.put(value, newFreq);

                // If frequency just crossed from L to L+1, this ID now violates the rule.
                if (newFreq == L + 1) {
                    violatingNonCriticalKinds++;
                }
            }

            // Step 2: While the current window is fully valid, try to shrink it from the left.
            //
            // A valid window must:
            // - cover all critical IDs
            // - have no non-critical ID appearing more than L times
            while (coveredCritical == requiredCritical && violatingNonCriticalKinds == 0) {
                // Update the best answer using the current valid window [left, right].
                answer = Math.min(answer, right - left + 1);

                // Attempt to remove events[left] and see whether validity can still hold.
                int leftValue = events[left];

                if (criticalSet.contains(leftValue)) {
                    // Removing a critical ID may cause us to lose coverage.
                    int oldFreq = criticalFreq.get(leftValue);
                    int newFreq = oldFreq - 1;

                    if (newFreq == 0) {
                        criticalFreq.remove(leftValue);
                        coveredCritical--;
                    } else {
                        criticalFreq.put(leftValue, newFreq);
                    }
                } else {
                    // Removing a non-critical ID may reduce a violation.
                    int oldFreq = nonCriticalFreq.get(leftValue);
                    int newFreq = oldFreq - 1;

                    // If oldFreq was L+1, then after decrement it becomes L,
                    // so this non-critical ID stops violating the rule.
                    if (oldFreq == L + 1) {
                        violatingNonCriticalKinds--;
                    }

                    if (newFreq == 0) {
                        nonCriticalFreq.remove(leftValue);
                    } else {
                        nonCriticalFreq.put(leftValue, newFreq);
                    }
                }

                // Move left boundary rightward to continue shrinking.
                left++;
            }
        }

        return answer == Integer.MAX_VALUE ? -1 : answer;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation such as [1, 2, 3]
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n) for the produced string.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Runs a single demonstration test case and prints the input and output.
     *
     * @param events the event stream
     * @param critical the required critical IDs
     * @param L the non-critical frequency limit
     * @return the computed shortest valid window length
     *
     * Time complexity: O(n), delegated to shortestMaintenanceWindow.
     * Space complexity: O(c + u), delegated to shortestMaintenanceWindow.
     */
    public int runDemo(int[] events, int[] critical, int L) {
        System.out.println("events   = " + arrayToString(events));
        System.out.println("critical = " + arrayToString(critical));
        System.out.println("L        = " + L);
        int result = shortestMaintenanceWindow(events, critical, L);
        System.out.println("result   = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra sanity checks.
     *
     * Verified sample traces:
     * Example 1:
     * events = [7,2,9,2,5,7,3,9,5], critical = [2,5,9], L = 1
     * Shortest valid window is [9,2,5] => length 3.
     *
     * Example 2:
     * events = [4,8,1,8,6,4,2,6,1], critical = [1,2,6], L = 0
     * Any non-critical ID is forbidden in a valid window.
     * Window [2,6,1] at indices 6..8 contains all critical IDs and no non-critical IDs => length 3.
     *
     * @param args command-line arguments (unused)
     *
     * Time complexity: O(total input size of demo cases).
     * Space complexity: proportional to the largest demo case processed.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] events1 = {7, 2, 9, 2, 5, 7, 3, 9, 5};
        int[] critical1 = {2, 5, 9};
        int L1 = 1;
        solution.runDemo(events1, critical1, L1); // Expected: 3

        // Sample 2
        int[] events2 = {4, 8, 1, 8, 6, 4, 2, 6, 1};
        int[] critical2 = {1, 2, 6};
        int L2 = 0;
        solution.runDemo(events2, critical2, L2); // Expected: 3

        // Extra sanity check: impossible because one critical ID never appears.
        int[] events3 = {1, 2, 3, 4};
        int[] critical3 = {2, 5};
        int L3 = 10;
        solution.runDemo(events3, critical3, L3); // Expected: -1

        // Extra sanity check: non-critical repetition can invalidate larger windows,
        // but a smaller valid one may still exist.
        int[] events4 = {10, 1, 99, 2, 99, 3, 4};
        int[] critical4 = {1, 2, 3};
        int L4 = 1;
        solution.runDemo(events4, critical4, L4); // Expected: 5 ([1,99,2,99,3] invalid because 99 appears twice > 1, so answer is [1,99,2,99,3]? invalid. Valid shortest is [1,99,2,99,3,4]? still invalid. Actually valid is [1,99,2,99,3] invalid, so no valid window containing all 1,2,3 with L=1 => -1.
    }
}