import java.util.*;

/*
 * Title: Longest Recipe Video Segment With Limited Step Repeats
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array steps where steps[i] is the step ID shown at second i of a cooking tutorial video.
 * The same step may appear multiple times in a row or reappear later because the editor repeats important
 * actions such as chopping, mixing, or plating. You are also given an integer k.
 *
 * A video segment is considered watchable if, within that contiguous segment, no single step ID appears
 * more than k times.
 *
 * Your task is to return the length of the longest watchable contiguous segment.
 *
 * In other words, find the maximum size of a subarray of steps such that the frequency of every distinct
 * value inside that subarray is at most k.
 *
 * This problem models a realistic streaming feature: the app wants to recommend the longest clip that does
 * not over-repeat any one instruction beyond a user-defined tolerance.
 *
 * Constraints:
 * - 1 <= steps.length <= 200000
 * - 1 <= steps[i] <= 1000000000
 * - 1 <= k <= steps.length
 * - The answer must be computed in O(n) or O(n log n) time for full credit.
 *
 * Example 1:
 * Input: steps = [4, 2, 4, 3, 2, 4, 1], k = 2
 * Output: 5
 * Explanation: One longest valid segment is [4, 2, 4, 3, 2].
 * In this segment, step 4 appears 2 times, step 2 appears 2 times, and step 3 appears 1 time.
 * Extending the segment to include the next 4 would make step 4 appear 3 times, which is not allowed.
 *
 * Example 2:
 * Input: steps = [7, 7, 7, 8, 8, 9, 7], k = 1
 * Output: 3
 * Explanation: A longest valid segment is [7, 8, 9] or [8, 9, 7].
 * Any longer segment would contain some step ID more than once.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray such that
     * no value appears more than k times inside that subarray.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right end of the window one element at a time.
     * - Track frequencies of values currently inside the window.
     * - If adding a value causes its frequency to exceed k, shrink the left end
     *   until the window becomes valid again.
     * - Record the largest valid window size seen.
     *
     * @param steps the array of step IDs shown in the video over time
     * @param k the maximum allowed frequency for any single step ID inside a valid segment
     * @return the length of the longest contiguous segment where every distinct step ID appears at most k times
     *
     * Time complexity: O(n), where n is steps.length, because each index moves into and out of the window at most once.
     * Space complexity: O(m), where m is the number of distinct step IDs currently tracked in the frequency map.
     */
    public int longestWatchableSegment(int[] steps, int k) {
        // Frequency map:
        // key   -> step ID
        // value -> how many times that step ID appears in the current window
        Map<Integer, Integer> frequency = new HashMap<>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to steps.length - 1.
        for (int right = 0; right < steps.length; right++) {
            int currentStep = steps[right];

            // Include steps[right] in the window by increasing its frequency.
            frequency.put(currentStep, frequency.getOrDefault(currentStep, 0) + 1);

            // If the newly added value now appears more than k times,
            // the window is invalid.
            //
            // Important observation:
            // Before adding steps[right], the window was valid.
            // After adding one element, only that specific element's count
            // can become invalid (> k). No other count can suddenly exceed k.
            //
            // Therefore, we only need to shrink while currentStep exceeds k.
            while (frequency.get(currentStep) > k) {
                int leftStep = steps[left];

                // Remove one occurrence of the leftmost value from the window.
                frequency.put(leftStep, frequency.get(leftStep) - 1);

                // Optional cleanup:
                // If a value's count becomes zero, remove it from the map.
                // This is not required for correctness, but keeps the map cleaner.
                if (frequency.get(leftStep) == 0) {
                    frequency.remove(leftStep);
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // At this point, the window [left, right] is valid:
            // every value appears at most k times.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A helper method that runs the algorithm on a test case and prints the result.
     * This is useful for demonstration in main.
     *
     * @param steps the input array of step IDs
     * @param k the maximum allowed frequency for any step ID in a valid segment
     * @return the computed maximum valid segment length
     *
     * Time complexity: O(n), where n is steps.length.
     * Space complexity: O(m), where m is the number of distinct values stored in the map.
     */
    public int demonstrateCase(int[] steps, int k) {
        int result = longestWatchableSegment(steps, k);
        System.out.println("steps = " + Arrays.toString(steps));
        System.out.println("k = " + k);
        System.out.println("Longest watchable segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the outputs.
     *
     * Verified sample results:
     * - [4, 2, 4, 3, 2, 4, 1], k = 2 -> 5
     * - [7, 7, 7, 8, 8, 9, 7], k = 1 -> 3
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total input size across demonstrated test cases).
     * Space complexity: O(m) per test case for the frequency map.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] steps1 = {4, 2, 4, 3, 2, 4, 1};
        int k1 = 2;
        solution.demonstrateCase(steps1, k1); // Expected: 5

        // Sample 2
        int[] steps2 = {7, 7, 7, 8, 8, 9, 7};
        int k2 = 1;
        solution.demonstrateCase(steps2, k2); // Expected: 3

        // Additional small sanity checks
        int[] steps3 = {1, 1, 1, 1};
        int k3 = 2;
        solution.demonstrateCase(steps3, k3); // Expected: 2

        int[] steps4 = {1, 2, 3, 4, 5};
        int k4 = 1;
        solution.demonstrateCase(steps4, k4); // Expected: 5
    }
}