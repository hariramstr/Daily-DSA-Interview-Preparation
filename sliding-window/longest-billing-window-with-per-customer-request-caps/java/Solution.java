import java.util.*;

/*
 * Title: Longest Billing Window With Per-Customer Request Caps
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A cloud platform records API requests in chronological order. Each request is labeled
 * with the customer ID that generated it. For billing analysis, you are given an array
 * requests where requests[i] is the customer ID of the i-th request, and an integer limit.
 * A contiguous time window is considered billable if no customer appears more than limit
 * times inside that window.
 *
 * Your task is to return the length of the longest billable contiguous window.
 *
 * This is harder than a standard frequency-limited window because the input size is large,
 * customer IDs may be large integers, and the optimal solution is expected to run in linear
 * time using a sliding window with dynamic frequency tracking. Any solution that repeatedly
 * recomputes frequencies for candidate windows will be too slow.
 *
 * Formally, find the maximum value of (right - left + 1) over all pairs
 * 0 <= left <= right < n such that for every customer ID x, the number of indices i in
 * [left, right] with requests[i] = x is at most limit.
 *
 * Constraints:
 * - 1 <= requests.length <= 200000
 * - 1 <= requests[i] <= 1000000000
 * - 1 <= limit <= requests.length
 * - requests is already ordered by time
 *
 * Example 1:
 * Input: requests = [4, 7, 4, 2, 7, 4, 7], limit = 2
 * Output: 5
 * Explanation: The longest valid window is [4, 2, 7, 4, 7], where customer 4 appears
 * 2 times and customer 7 appears 2 times.
 *
 * Example 2:
 * Input: requests = [9, 9, 9, 3, 3, 8, 8, 8, 8], limit = 2
 * Output: 4
 * Explanation: One optimal window is [9, 3, 3, 8]. Any longer window would cause either
 * customer 9 or customer 8 to appear more than 2 times.
 *
 * Return only the maximum valid window length.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous window such that no customer ID
     * appears more than {@code limit} times inside the window.
     *
     * The method uses the classic sliding window technique:
     * - Expand the right boundary one request at a time.
     * - Track frequencies of customer IDs inside the current window.
     * - If adding the new request makes some customer's frequency exceed {@code limit},
     *   move the left boundary rightward until the window becomes valid again.
     * - Record the maximum valid window length seen so far.
     *
     * Why this works:
     * - The window always remains contiguous.
     * - Each element enters the window once (when right moves).
     * - Each element leaves the window at most once (when left moves).
     * - Therefore, the total work is linear.
     *
     * @param requests the chronological list of customer IDs for API requests
     * @param limit the maximum allowed number of occurrences of any single customer ID
     *              inside a valid billing window
     * @return the maximum length of any contiguous valid billing window
     *
     * Time complexity: O(n), where n is requests.length, because each index is processed
     * at most twice: once by the right pointer and once by the left pointer.
     * Space complexity: O(k), where k is the number of distinct customer IDs currently
     * tracked in the frequency map, up to O(n) in the worst case.
     */
    public int longestBillableWindow(int[] requests, int limit) {
        // Frequency map:
        // key   -> customer ID
        // value -> how many times that customer appears in the current window [left, right]
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < requests.length; right++) {
            int currentCustomer = requests[right];

            // Include the current request in the window.
            frequency.put(currentCustomer, frequency.getOrDefault(currentCustomer, 0) + 1);

            // If the newly added customer now appears too many times,
            // the window is invalid and we must shrink it from the left.
            //
            // Important observation:
            // Before adding requests[right], the window was valid.
            // Therefore, the only possible violation after adding requests[right]
            // is that currentCustomer exceeded the limit.
            while (frequency.get(currentCustomer) > limit) {
                int leftCustomer = requests[left];

                // Remove the leftmost request from the window.
                frequency.put(leftCustomer, frequency.get(leftCustomer) - 1);

                // Optional cleanup:
                // If a frequency becomes zero, remove it from the map to keep the map tidy.
                if (frequency.get(leftCustomer) == 0) {
                    frequency.remove(leftCustomer);
                }

                // Move the left boundary rightward.
                left++;
            }

            // At this point, the window [left, right] is valid.
            int currentWindowLength = right - left + 1;

            // Update the best answer if this valid window is longer.
            if (currentWindowLength > maxLength) {
                maxLength = currentWindowLength;
            }
        }

        return maxLength;
    }

    /**
     * A small helper method that runs the algorithm on one test case and prints
     * the input and output in a readable format.
     *
     * @param requests the request array to test
     * @param limit the maximum allowed frequency per customer inside a valid window
     * @return the computed longest valid window length
     *
     * Time complexity: O(n), delegated to {@link #longestBillableWindow(int[], int)}.
     * Space complexity: O(k), delegated to {@link #longestBillableWindow(int[], int)}.
     */
    public int demonstrateCase(int[] requests, int limit) {
        int result = longestBillableWindow(requests, limit);
        System.out.println("requests = " + Arrays.toString(requests));
        System.out.println("limit = " + limit);
        System.out.println("Longest billable window length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Verified sample results:
     * - [4, 7, 4, 2, 7, 4, 7], limit = 2 -> 5
     * - [9, 9, 9, 3, 3, 8, 8, 8, 8], limit = 2 -> 4
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total input size of demonstrated test cases).
     * Space complexity: O(k) for the frequency map used during each demonstration.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] requests1 = {4, 7, 4, 2, 7, 4, 7};
        int limit1 = 2;
        int result1 = solution.demonstrateCase(requests1, limit1);
        System.out.println("Expected: 5");
        System.out.println("Actual:   " + result1);
        System.out.println();

        int[] requests2 = {9, 9, 9, 3, 3, 8, 8, 8, 8};
        int limit2 = 2;
        int result2 = solution.demonstrateCase(requests2, limit2);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // Additional quick sanity check.
        int[] requests3 = {1, 2, 3, 4, 5};
        int limit3 = 1;
        int result3 = solution.demonstrateCase(requests3, limit3);
        System.out.println("Expected: 5");
        System.out.println("Actual:   " + result3);
    }
}