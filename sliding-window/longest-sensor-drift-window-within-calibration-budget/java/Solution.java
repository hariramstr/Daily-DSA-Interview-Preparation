import java.util.*;

/*
 * Title: Longest Sensor Drift Window Within Calibration Budget
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A factory records a sequence of integer sensor readings over time. Engineers want to analyze
 * the longest contiguous time window that can be considered stable enough to recalibrate together.
 * A window is valid if the difference between its highest reading and lowest reading is at most budget.
 *
 * Given an integer array readings and an integer budget, return the length of the longest contiguous
 * subarray such that max(readings[l..r]) - min(readings[l..r]) <= budget.
 *
 * This models a real monitoring system where readings may fluctuate, but only within a limited
 * tolerance before a recalibration job must be split into smaller batches.
 *
 * Constraints:
 * - 1 <= readings.length <= 200000
 * - -10^9 <= readings[i] <= 10^9
 * - 0 <= budget <= 10^9
 *
 * Example 1:
 * Input: readings = [8, 10, 9, 12, 7, 8], budget = 3
 * Output: 3
 * Explanation: One longest valid window is [8, 10, 9]. Its maximum is 10 and minimum is 8,
 * so the difference is 2, which is within budget. Any longer window exceeds the allowed drift.
 *
 * Example 2:
 * Input: readings = [4, 4, 5, 6, 6, 3, 4], budget = 2
 * Output: 5
 * Explanation: The window [4, 4, 5, 6, 6] is valid because max - min = 6 - 4 = 2.
 * Extending it to include 3 would make the difference 3, which is too large.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray where the difference between
     * the maximum and minimum values is at most the given budget.
     *
     * This method uses a sliding window together with two monotonic deques:
     * 1. A decreasing deque to keep track of the current window's maximum value.
     * 2. An increasing deque to keep track of the current window's minimum value.
     *
     * The key idea:
     * - Expand the right side of the window one element at a time.
     * - Maintain the max and min of the current window efficiently.
     * - If the window becomes invalid (max - min > budget), move the left side forward
     *   until the window becomes valid again.
     * - Track the largest valid window length seen during the process.
     *
     * @param readings the array of sensor readings over time
     * @param budget the maximum allowed difference between the highest and lowest reading in a valid window
     * @return the length of the longest contiguous valid window
     *
     * Time complexity: O(n), because each element is added to and removed from each deque at most once.
     * Space complexity: O(n), in the worst case for the deques.
     */
    public int longestStableWindow(int[] readings, int budget) {
        // Defensive handling for completeness.
        // According to constraints, readings.length >= 1, but this makes the method safer.
        if (readings == null || readings.length == 0) {
            return 0;
        }

        // maxDeque will store values in decreasing order.
        // The front always contains the maximum value in the current window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // minDeque will store values in increasing order.
        // The front always contains the minimum value in the current window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // left is the starting index of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right from left to right across the array, expanding the window.
        for (int right = 0; right < readings.length; right++) {
            int currentValue = readings[right];

            // ------------------------------------------------------------
            // Step 1: Insert the new value into the max deque.
            // ------------------------------------------------------------
            // We want maxDeque to remain in decreasing order.
            // So while the last element is smaller than the current value,
            // it can never become the maximum for any future window that includes currentValue.
            while (!maxDeque.isEmpty() && maxDeque.peekLast() < currentValue) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(currentValue);

            // ------------------------------------------------------------
            // Step 2: Insert the new value into the min deque.
            // ------------------------------------------------------------
            // We want minDeque to remain in increasing order.
            // So while the last element is larger than the current value,
            // it can never become the minimum for any future window that includes currentValue.
            while (!minDeque.isEmpty() && minDeque.peekLast() > currentValue) {
                minDeque.pollLast();
            }
            minDeque.offerLast(currentValue);

            // ------------------------------------------------------------
            // Step 3: Shrink the window from the left while it is invalid.
            // ------------------------------------------------------------
            // The current window is [left, right].
            // It is invalid if current max - current min > budget.
            //
            // maxDeque.peekFirst() gives the maximum in the window.
            // minDeque.peekFirst() gives the minimum in the window.
            while ((long) maxDeque.peekFirst() - (long) minDeque.peekFirst() > budget) {
                int outgoingValue = readings[left];

                // If the value leaving the window is exactly the current maximum,
                // remove it from the front of maxDeque.
                if (outgoingValue == maxDeque.peekFirst()) {
                    maxDeque.pollFirst();
                }

                // If the value leaving the window is exactly the current minimum,
                // remove it from the front of minDeque.
                if (outgoingValue == minDeque.peekFirst()) {
                    minDeque.pollFirst();
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // Step 4: At this point, the window [left, right] is valid.
            // Update the best answer.
            // ------------------------------------------------------------
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A helper method that runs the algorithm on a sample input and prints the result.
     *
     * @param readings the array of sensor readings
     * @param budget the allowed calibration budget
     * @return the computed maximum valid window length
     *
     * Time complexity: O(n), where n is the length of readings.
     * Space complexity: O(n), in the worst case due to deque storage.
     */
    public int demonstrate(int[] readings, int budget) {
        int result = longestStableWindow(readings, budget);
        System.out.println("Readings: " + Arrays.toString(readings));
        System.out.println("Budget: " + budget);
        System.out.println("Longest stable window length: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution using the sample inputs from the problem statement.
     *
     * It prints the results and also includes the expected outputs for easy verification.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call.
     * Space complexity: O(n) per demonstration call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] readings1 = {8, 10, 9, 12, 7, 8};
        int budget1 = 3;
        int result1 = solution.demonstrate(readings1, budget1);
        System.out.println("Expected: 3");
        System.out.println("Actual:   " + result1);
        System.out.println();

        // Sample 2
        int[] readings2 = {4, 4, 5, 6, 6, 3, 4};
        int budget2 = 2;
        int result2 = solution.demonstrate(readings2, budget2);
        System.out.println("Expected: 5");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // Additional quick checks
        int[] readings3 = {5};
        int budget3 = 0;
        int result3 = solution.demonstrate(readings3, budget3);
        System.out.println("Expected: 1");
        System.out.println("Actual:   " + result3);
        System.out.println();

        int[] readings4 = {1, 2, 3, 4, 5};
        int budget4 = 4;
        int result4 = solution.demonstrate(readings4, budget4);
        System.out.println("Expected: 5");
        System.out.println("Actual:   " + result4);
    }
}