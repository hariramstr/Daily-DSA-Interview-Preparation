import java.util.*;

/*
 * Title: Longest Camera Feed With Limited Motion Zones
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A security team monitors a hallway using a camera that reports one motion zone ID for each second.
 * The array zones represents the detected zone at every second, where zones[i] is the zone that had
 * motion during second i. To reduce operator fatigue, the team wants to review the longest continuous
 * time interval that contains motion from at most k distinct zones. If more than k different zone IDs
 * appear in the interval, the segment is considered too noisy to review efficiently.
 *
 * Your task is to return the length of the longest contiguous subarray of zones that contains at most
 * k distinct values.
 *
 * This is a realistic streaming problem: the answer must be based on a continuous interval, not a
 * subsequence. An efficient solution is expected because the feed may be very large.
 *
 * Constraints:
 * - 1 <= zones.length <= 2 * 10^5
 * - 1 <= zones[i] <= 10^9
 * - 1 <= k <= zones.length
 * - The solution should run in linear time or close to it.
 *
 * Example 1:
 * Input: zones = [4, 2, 4, 3, 2, 2, 4], k = 2
 * Output: 3
 *
 * Example 2:
 * Input: zones = [7, 7, 8, 9, 8, 8, 7, 7], k = 2
 * Output: 4
 *
 * Note:
 * You only need to return the maximum length, not the interval itself.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray that contains
     * at most k distinct zone IDs.
     *
     * This method uses the classic sliding window technique:
     * 1. Expand the right side of the window one element at a time.
     * 2. Track frequencies of values currently inside the window.
     * 3. If the window becomes invalid (more than k distinct values),
     *    move the left side forward until the window becomes valid again.
     * 4. After each valid expansion, update the best answer.
     *
     * @param zones the array of motion zone IDs, where zones[i] is the detected zone at second i
     * @param k the maximum number of distinct zone IDs allowed in the reviewed interval
     * @return the maximum length of a contiguous subarray containing at most k distinct values
     *
     * Time complexity: O(n), where n is zones.length, because each index is visited
     * at most twice (once by the right pointer and once by the left pointer).
     * Space complexity: O(k) on average for the frequency map of values inside the window,
     * and O(min(n, number of distinct values))) in the most general sense.
     */
    public int longestCameraFeedWithLimitedMotionZones(int[] zones, int k) {
        // Frequency map:
        // key   -> zone ID
        // value -> how many times that zone ID appears in the current window
        Map<Integer, Integer> frequency = new HashMap<>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We move right from 0 to zones.length - 1, expanding the window.
        for (int right = 0; right < zones.length; right++) {
            int currentZone = zones[right];

            // Add the new rightmost element into the window.
            // If it is already present, increase its count.
            // Otherwise, insert it with count 1.
            frequency.put(currentZone, frequency.getOrDefault(currentZone, 0) + 1);

            // If the number of distinct zone IDs exceeds k,
            // the current window is invalid and must be shrunk from the left.
            //
            // We keep shrinking until the window again contains at most k distinct values.
            while (frequency.size() > k) {
                int leftZone = zones[left];

                // Decrease the count of the element that is leaving the window.
                frequency.put(leftZone, frequency.get(leftZone) - 1);

                // If its count becomes zero, it no longer exists in the window,
                // so we remove it from the map completely.
                if (frequency.get(leftZone) == 0) {
                    frequency.remove(leftZone);
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // At this point, the window [left, right] is guaranteed to be valid:
            // it contains at most k distinct zone IDs.
            //
            // Compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            best = Math.max(best, currentLength);
        }

        return best;
    }

    /**
     * A small helper method that prints an input array in a readable format
     * and displays the computed answer.
     *
     * @param zones the array of motion zone IDs
     * @param k the maximum number of distinct zone IDs allowed
     * @return the computed maximum valid interval length
     *
     * Time complexity: O(n), where n is zones.length, due to the main algorithm.
     * Space complexity: O(k) on average for the sliding window frequency map.
     */
    public int demonstrateCase(int[] zones, int k) {
        int result = longestCameraFeedWithLimitedMotionZones(zones, k);
        System.out.println("zones = " + Arrays.toString(zones));
        System.out.println("k = " + k);
        System.out.println("Longest valid interval length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * The examples are verified carefully:
     *
     * Example 1:
     * zones = [4, 2, 4, 3, 2, 2, 4], k = 2
     * Valid longest intervals include [4, 2, 4] and [3, 2, 2] and [2, 2, 4],
     * each of length 3. No valid interval of length 4 exists with at most 2 distinct values.
     * Therefore, the correct answer is 3.
     *
     * Example 2:
     * zones = [7, 7, 8, 9, 8, 8, 7, 7], k = 2
     * Longest valid intervals include [7, 7, 8] length 3, [8, 9, 8, 8] length 4,
     * and [8, 8, 7, 7] length 4. Any length 5 candidate contains 3 distinct values.
     * Therefore, the correct answer is 4.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size across demonstrated test cases).
     * Space complexity: O(k) on average per test case for the frequency map.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] zones1 = {4, 2, 4, 3, 2, 2, 4};
        int k1 = 2;
        solution.demonstrateCase(zones1, k1); // Expected: 3

        int[] zones2 = {7, 7, 8, 9, 8, 8, 7, 7};
        int k2 = 2;
        solution.demonstrateCase(zones2, k2); // Expected: 4

        int[] zones3 = {1};
        int k3 = 1;
        solution.demonstrateCase(zones3, k3); // Expected: 1

        int[] zones4 = {5, 5, 5, 5};
        int k4 = 1;
        solution.demonstrateCase(zones4, k4); // Expected: 4

        int[] zones5 = {1, 2, 3, 4, 5};
        int k5 = 2;
        solution.demonstrateCase(zones5, k5); // Expected: 2
    }
}