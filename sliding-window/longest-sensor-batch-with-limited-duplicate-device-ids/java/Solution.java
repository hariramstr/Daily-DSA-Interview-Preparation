import java.util.*;

/*
 * Title: Longest Sensor Batch With Limited Duplicate Device IDs
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A monitoring system receives a stream of sensor readings, where each reading is labeled
 * with the integer device ID that produced it. Engineers want to analyze the longest
 * contiguous batch of readings that is still considered "diverse enough." A batch is valid
 * if no single device ID appears more than k times inside that contiguous segment.
 *
 * Given an integer array deviceIds and an integer k, return the length of the longest
 * contiguous subarray such that every distinct device ID appears at most k times within
 * that subarray.
 *
 * This is a contiguous-window problem: you may only choose a single continuous segment
 * from the input array. The goal is to maximize its length while respecting the per-device
 * frequency limit.
 *
 * You should design an efficient solution suitable for large input sizes.
 *
 * Constraints:
 * - 1 <= deviceIds.length <= 200000
 * - 1 <= deviceIds[i] <= 1000000000
 * - 1 <= k <= deviceIds.length
 * - The answer must be computed in O(n) or O(n log n) time.
 *
 * Example 1:
 * Input: deviceIds = [4, 1, 4, 2, 4, 1, 2, 2], k = 2
 * Output: 5
 * Explanation: One longest valid batch is [1, 4, 2, 4, 1]. In this batch, device 1 appears
 * 2 times, device 4 appears 2 times, and device 2 appears 1 time. Any longer contiguous
 * batch would cause some device to appear more than 2 times.
 *
 * Example 2:
 * Input: deviceIds = [7, 7, 3, 7, 3, 3, 8], k = 1
 * Output: 2
 * Explanation: Since each device ID may appear at most once, every valid batch must contain
 * only unique IDs. The longest valid batches include [7, 3] and [3, 8], both with length 2.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray in which every distinct
     * device ID appears at most k times.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right boundary one element at a time.
     * - Track frequencies of values inside the current window.
     * - If adding the new value makes its frequency exceed k, move the left boundary
     *   rightward until the window becomes valid again.
     * - Record the maximum valid window length seen so far.
     *
     * @param deviceIds the array of device IDs representing the sensor reading stream
     * @param k the maximum allowed frequency for any single device ID inside a valid window
     * @return the length of the longest valid contiguous subarray
     *
     * Time complexity: O(n), because each element is added to the window once and removed at most once.
     * Space complexity: O(m), where m is the number of distinct device IDs currently tracked in the map
     * (at most O(n) in the worst case).
     */
    public int longestValidBatch(int[] deviceIds, int k) {
        // Frequency map:
        // key   -> device ID
        // value -> how many times that device ID appears in the current window [left, right]
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < deviceIds.length; right++) {
            int currentDevice = deviceIds[right];

            // Step 1: Include deviceIds[right] into the current window.
            // We increase its count in the frequency map.
            frequency.put(currentDevice, frequency.getOrDefault(currentDevice, 0) + 1);

            // Step 2: If this addition caused the current device to appear more than k times,
            // the window is invalid. We must shrink from the left until it becomes valid again.
            //
            // Important observation:
            // Before adding currentDevice, the window was valid.
            // Therefore, after adding one element, the only possible violation is that
            // currentDevice itself now has count k + 1.
            //
            // So we keep moving left forward until currentDevice's count is back to <= k.
            while (frequency.get(currentDevice) > k) {
                int leftDevice = deviceIds[left];

                // Remove one occurrence of the leftmost device from the window.
                frequency.put(leftDevice, frequency.get(leftDevice) - 1);

                // Optional cleanup:
                // If a device count becomes zero, remove it from the map to keep the map tidy.
                if (frequency.get(leftDevice) == 0) {
                    frequency.remove(leftDevice);
                }

                // Move the left boundary rightward, effectively shrinking the window.
                left++;
            }

            // Step 3: At this point, the window [left, right] is valid:
            // every device ID appears at most k times.
            int currentWindowLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is the largest so far.
            maxLength = Math.max(maxLength, currentWindowLength);
        }

        return maxLength;
    }

    /**
     * A secondary method name that may be convenient for interview-style usage.
     * It delegates to the main algorithm implementation.
     *
     * @param deviceIds the array of device IDs
     * @param k the maximum allowed frequency for any device ID in the window
     * @return the maximum length of a valid contiguous subarray
     *
     * Time complexity: O(n)
     * Space complexity: O(m), where m is the number of distinct values tracked
     */
    public int maxSubarrayLength(int[] deviceIds, int k) {
        return longestValidBatch(deviceIds, k);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(m) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] deviceIds1 = {4, 1, 4, 2, 4, 1, 2, 2};
        int k1 = 2;
        int result1 = solution.longestValidBatch(deviceIds1, k1);
        System.out.println("Example 1 Result: " + result1);
        // Expected: 5

        // Example 2
        int[] deviceIds2 = {7, 7, 3, 7, 3, 3, 8};
        int k2 = 1;
        int result2 = solution.longestValidBatch(deviceIds2, k2);
        System.out.println("Example 2 Result: " + result2);
        // Expected: 2

        // Additional quick checks
        int[] deviceIds3 = {1, 2, 3, 4, 5};
        int k3 = 1;
        System.out.println("Additional Example 3 Result: " + solution.longestValidBatch(deviceIds3, k3));
        // Expected: 5

        int[] deviceIds4 = {9, 9, 9, 9};
        int k4 = 2;
        System.out.println("Additional Example 4 Result: " + solution.longestValidBatch(deviceIds4, k4));
        // Expected: 2
    }
}