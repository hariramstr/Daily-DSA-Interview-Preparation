import java.util.*;

/*
 * Title: Longest Workout Segment With Limited Heart Rate Zones
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array `zones` where `zones[i]` represents the heart rate zone
 * recorded during the `i`-th minute of a workout. A coach wants to analyze the
 * longest continuous workout segment that stayed focused enough to use at most `k`
 * distinct heart rate zones.
 *
 * Return the length of the longest contiguous subarray of `zones` that contains
 * no more than `k` distinct values.
 *
 * This problem models a realistic fitness analytics task: short fluctuations are
 * acceptable, but if too many different zones appear in one continuous segment,
 * the workout is considered inconsistent. Your goal is to find the maximum duration
 * of a consistent segment.
 *
 * A subarray is a contiguous portion of the array.
 *
 * Constraints:
 * - 1 <= zones.length <= 2 * 10^5
 * - 1 <= zones[i] <= 10^9
 * - 1 <= k <= zones.length
 *
 * Example 1:
 * Input: zones = [2, 2, 1, 3, 2, 2, 1], k = 2
 * Output: 3
 * Explanation: One valid longest segment is [2, 2, 1], which uses only zones {1, 2}.
 * Any longer contiguous segment contains at least 3 distinct zones.
 *
 * Example 2:
 * Input: zones = [5, 5, 5, 4, 4, 6, 6, 4], k = 2
 * Output: 5
 * Explanation: The segment [4, 4, 6, 6, 4] contains only zones {4, 6} and has
 * length 5, which is the maximum possible.
 *
 * You only need to return the maximum length, not the segment itself.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains
     * at most k distinct heart rate zones.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right boundary one step at a time.
     * - Track frequencies of values inside the current window.
     * - If the window becomes invalid (more than k distinct values),
     *   move the left boundary rightward until the window is valid again.
     * - Record the maximum valid window length seen so far.
     *
     * @param zones the array of heart rate zones recorded minute by minute
     * @param k the maximum number of distinct zones allowed in a valid segment
     * @return the maximum length of a contiguous subarray containing at most k distinct values
     *
     * Time complexity: O(n), where n is the length of zones, because each index
     * is moved at most once by the left pointer and once by the right pointer.
     * Space complexity: O(k) on average for the frequency map of values currently
     * in the window, and O(n) in the worst case if many distinct values are encountered.
     */
    public int longestWorkoutSegment(int[] zones, int k) {
        // Frequency map:
        // key   -> zone value
        // value -> how many times that zone currently appears in the sliding window
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < zones.length; right++) {
            int currentZone = zones[right];

            // Include zones[right] in the current window by increasing its count.
            frequency.put(currentZone, frequency.getOrDefault(currentZone, 0) + 1);

            // If we now have more than k distinct values, the window is invalid.
            // We must shrink it from the left until it becomes valid again.
            while (frequency.size() > k) {
                int leftZone = zones[left];

                // Decrease the count of the value leaving the window.
                frequency.put(leftZone, frequency.get(leftZone) - 1);

                // If its count becomes zero, remove it completely from the map.
                // This is important because the number of distinct values is exactly
                // the number of keys in the map.
                if (frequency.get(leftZone) == 0) {
                    frequency.remove(leftZone);
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // At this point, the window [left, right] is guaranteed to contain
            // at most k distinct values, so it is valid.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * A helper method that runs a single demonstration test case and prints the result.
     *
     * @param zones the input array of heart rate zones
     * @param k the maximum number of distinct zones allowed
     * @return the computed maximum valid segment length
     *
     * Time complexity: O(n), where n is the length of zones.
     * Space complexity: O(k) on average for the sliding window frequency map,
     * and O(n) in the worst case.
     */
    public int runAndPrintExample(int[] zones, int k) {
        int result = longestWorkoutSegment(zones, k);
        System.out.println("zones = " + Arrays.toString(zones));
        System.out.println("k = " + k);
        System.out.println("Longest valid segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution using the sample inputs
     * from the problem statement, plus one additional example.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated examples).
     * Space complexity: Depends on the largest example processed, following
     * the same bounds as longestWorkoutSegment.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // zones = [2, 2, 1, 3, 2, 2, 1], k = 2
        // Expected output: 3
        int[] zones1 = {2, 2, 1, 3, 2, 2, 1};
        int k1 = 2;
        int result1 = solution.runAndPrintExample(zones1, k1);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println("Matches expected: " + (result1 == 3));
        System.out.println();

        // Example 2 from the problem statement:
        // zones = [5, 5, 5, 4, 4, 6, 6, 4], k = 2
        // Expected output: 5
        int[] zones2 = {5, 5, 5, 4, 4, 6, 6, 4};
        int k2 = 2;
        int result2 = solution.runAndPrintExample(zones2, k2);
        System.out.println("Expected: 5, Actual: " + result2);
        System.out.println("Matches expected: " + (result2 == 5));
        System.out.println();

        // Additional beginner-friendly demonstration:
        // Entire array is valid because it uses only 1 distinct value.
        int[] zones3 = {7, 7, 7, 7};
        int k3 = 1;
        int result3 = solution.runAndPrintExample(zones3, k3);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println("Matches expected: " + (result3 == 4));
    }
}