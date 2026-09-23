import java.util.*;

/*
 * Title: Longest Shipping Lane With Limited Hazard Labels
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A logistics company records the hazard label attached to each package loaded onto a conveyor belt.
 * The labels are given as an array of strings labels, where labels[i] is the hazard category of the
 * i-th package in loading order. For safety, a supervisor wants to inspect the longest contiguous
 * block of packages such that the block contains at most k distinct hazard categories.
 *
 * Your task is to return the length of the longest contiguous subarray of labels that contains
 * no more than k distinct values.
 *
 * This models a real monitoring problem: when too many hazard categories appear together,
 * the inspection procedure becomes too complex, so the company wants the largest continuous
 * stretch that still stays within the allowed variety.
 *
 * A contiguous block means you may only choose packages that appear next to each other in the original order.
 *
 * Constraints:
 * - 1 <= labels.length <= 100000
 * - 1 <= labels[i].length <= 20
 * - labels[i] consists of uppercase English letters, digits, or underscores
 * - 1 <= k <= labels.length
 *
 * Example 1:
 * Input: labels = ["FLAMMABLE", "CORROSIVE", "FLAMMABLE", "TOXIC", "CORROSIVE", "CORROSIVE"], k = 2
 * Output: 3
 * Explanation: The longest valid block is ["FLAMMABLE", "CORROSIVE", "FLAMMABLE"].
 * Any valid block with at most 2 distinct labels has maximum length 3.
 *
 * Example 2:
 * Input: labels = ["A", "A", "B", "B", "C", "B", "B", "A"], k = 2
 * Output: 5
 * Explanation: One longest valid block is ["B", "B", "C", "B", "B"], which contains only
 * the distinct labels "B" and "C".
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most k distinct labels.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Track how many times each label appears inside the current window.
     * - If the window becomes invalid (more than k distinct labels), move the left side forward
     *   until the window becomes valid again.
     * - Record the maximum valid window length seen during the process.
     *
     * @param labels the array of package hazard labels in loading order
     * @param k the maximum number of distinct hazard categories allowed in the window
     * @return the length of the longest contiguous block containing at most k distinct labels
     *
     * Time complexity: O(n), where n is labels.length, because each index is visited at most twice
     * (once by the right pointer and once by the left pointer).
     * Space complexity: O(k) in the typical sliding-window sense, or more precisely O(m),
     * where m is the number of distinct labels that appear in the current window / input.
     */
    public int longestShippingLaneWithLimitedHazardLabels(String[] labels, int k) {
        // This map stores the frequency of each label currently inside the sliding window.
        // Key   -> hazard label
        // Value -> how many times that label appears between left and right (inclusive)
        Map<String, Integer> frequencyMap = new HashMap<>();

        // left marks the beginning of the current window.
        int left = 0;

        // bestLength stores the maximum valid window size found so far.
        int bestLength = 0;

        // We expand the window by moving right from 0 to labels.length - 1.
        for (int right = 0; right < labels.length; right++) {
            // Step 1: include labels[right] into the current window.
            String currentLabel = labels[right];
            frequencyMap.put(currentLabel, frequencyMap.getOrDefault(currentLabel, 0) + 1);

            // Step 2: if the window now contains too many distinct labels,
            // shrink it from the left until it becomes valid again.
            while (frequencyMap.size() > k) {
                // Identify the label that is leaving the window.
                String leftLabel = labels[left];

                // Decrease its frequency because we are moving left forward.
                int updatedCount = frequencyMap.get(leftLabel) - 1;

                // If the count becomes zero, that label is no longer inside the window,
                // so we remove it completely from the map.
                if (updatedCount == 0) {
                    frequencyMap.remove(leftLabel);
                } else {
                    frequencyMap.put(leftLabel, updatedCount);
                }

                // Finally move the left boundary one step to the right.
                left++;
            }

            // Step 3: at this point, the window [left..right] is guaranteed to be valid
            // because it contains at most k distinct labels.
            int currentWindowLength = right - left + 1;

            // Step 4: update the best answer if this valid window is longer than any previous one.
            if (currentWindowLength > bestLength) {
                bestLength = currentWindowLength;
            }
        }

        // After processing all positions, bestLength is the answer.
        return bestLength;
    }

    /**
     * A second public method with a shorter name, useful if a caller wants a concise API.
     * It delegates directly to the main algorithm method.
     *
     * @param labels the array of package hazard labels
     * @param k the maximum number of distinct labels allowed
     * @return the maximum length of a contiguous subarray with at most k distinct labels
     *
     * Time complexity: O(n), where n is labels.length
     * Space complexity: O(m), where m is the number of distinct labels tracked in the map
     */
    public int longestSubarrayAtMostKDistinct(String[] labels, int k) {
        return longestShippingLaneWithLimitedHazardLabels(labels, k);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(m) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] labels1 = {"FLAMMABLE", "CORROSIVE", "FLAMMABLE", "TOXIC", "CORROSIVE", "CORROSIVE"};
        int k1 = 2;
        int result1 = solution.longestShippingLaneWithLimitedHazardLabels(labels1, k1);
        System.out.println("Example 1 Result: " + result1);

        String[] labels2 = {"A", "A", "B", "B", "C", "B", "B", "A"};
        int k2 = 2;
        int result2 = solution.longestShippingLaneWithLimitedHazardLabels(labels2, k2);
        System.out.println("Example 2 Result: " + result2);

        // Additional small demonstration:
        String[] labels3 = {"X"};
        int k3 = 1;
        int result3 = solution.longestShippingLaneWithLimitedHazardLabels(labels3, k3);
        System.out.println("Single Element Example Result: " + result3);
    }
}