import java.util.*;

/*
 * Title: Longest Work Block With Limited App Switching
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array apps where apps[i] is the name of the application a user
 * was focused on during the i-th minute of a work session. A contiguous block of
 * minutes is called efficient if the user switched between at most k distinct
 * applications inside that block. Your task is to return the length of the longest
 * efficient block.
 *
 * More formally, find the maximum length of a contiguous subarray of apps that
 * contains no more than k distinct strings.
 *
 * This problem models productivity analysis in desktop telemetry systems, where
 * frequent switching across too many tools in a short period may indicate fragmented
 * work. You must process the session efficiently because the log can be large.
 *
 * Return 0 if the input array is empty. You may assume k is non-negative.
 * If k = 0, no non-empty block is valid, so the answer is 0.
 *
 * Constraints:
 * - 0 <= apps.length <= 200000
 * - 0 <= k <= apps.length
 * - 1 <= apps[i].length <= 20
 * - apps[i] consists of lowercase English letters, digits, underscores, or hyphens
 *
 * Example 1:
 * Input: apps = ["mail","docs","mail","chat","docs","docs"], k = 2
 * Output: 3
 * Explanation: One longest valid block is ["docs","mail","chat"]? No, that has
 * 3 distinct apps, so it is invalid. The longest valid blocks include
 * ["mail","docs","mail"] and ["chat","docs","docs"], both of length 3.
 *
 * Example 2:
 * Input: apps = ["ide","ide","browser","terminal","browser","terminal","music"], k = 3
 * Output: 6
 * Explanation: The subarray
 * ["ide","browser","terminal","browser","terminal","music"] has 4 distinct apps,
 * so it is invalid. A longest valid block is
 * ["ide","ide","browser","terminal","browser","terminal"], which contains exactly
 * 3 distinct apps and has length 6.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most k distinct app names.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Track how many times each app appears inside the current window.
     * - If the window becomes invalid (more than k distinct apps), move the left side
     *   forward until the window becomes valid again.
     * - Record the maximum valid window length seen during the process.
     *
     * @param apps the array of app names, where apps[i] is the app focused during minute i
     * @param k the maximum number of distinct app names allowed in a valid block
     * @return the length of the longest contiguous block containing at most k distinct apps;
     *         returns 0 if the array is empty or if k is 0
     *
     * Time complexity: O(n), where n is apps.length, because each element enters and leaves
     * the sliding window at most once.
     * Space complexity: O(k) on average for the frequency map of apps in the current window,
     * and in the worst case O(n) if many distinct apps appear while processing.
     */
    public int longestEfficientBlock(String[] apps, int k) {
        // If the input array is empty, there is no block at all.
        if (apps == null || apps.length == 0) {
            return 0;
        }

        // If k is 0, then no non-empty subarray can be valid because even one app
        // would already create 1 distinct app, which exceeds the allowed limit.
        if (k == 0) {
            return 0;
        }

        // This map stores the frequency of each app name inside the current window.
        // Example:
        // window = ["mail", "docs", "mail"]
        // map = {mail=2, docs=1}
        Map<String, Integer> frequency = new HashMap<>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to apps.length - 1.
        for (int right = 0; right < apps.length; right++) {
            String currentApp = apps[right];

            // Add the current app to the frequency map.
            // If it is not already present, start its count at 0 and then add 1.
            frequency.put(currentApp, frequency.getOrDefault(currentApp, 0) + 1);

            // At this point, the window is apps[left...right].
            // It may or may not be valid.
            //
            // If the number of distinct apps is greater than k, we must shrink
            // the window from the left until it becomes valid again.
            while (frequency.size() > k) {
                String leftApp = apps[left];

                // Decrease the count of the app that is leaving the window.
                frequency.put(leftApp, frequency.get(leftApp) - 1);

                // If its count becomes 0, remove it completely from the map.
                // This is important because the number of distinct apps is exactly
                // the number of keys currently in the map.
                if (frequency.get(leftApp) == 0) {
                    frequency.remove(leftApp);
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Now the window apps[left...right] is guaranteed to have at most k distinct apps.
            // So it is valid, and we can compute its length.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer than any previous one.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A convenience wrapper with the same behavior as longestEfficientBlock.
     * This method is included to make the solution interface more descriptive.
     *
     * @param apps the array of app names observed over time
     * @param k the maximum number of distinct apps allowed in the block
     * @return the maximum length of a contiguous valid block
     *
     * Time complexity: O(n), where n is apps.length.
     * Space complexity: O(k) on average for the sliding window frequency map,
     * worst-case O(n).
     */
    public int lengthOfLongestSubarrayAtMostKDistinct(String[] apps, int k) {
        return longestEfficientBlock(apps, k);
    }

    /**
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(total number of elements across demonstrated test cases).
     * Space complexity: O(k) on average per test case for the frequency map.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        String[] apps1 = {"mail", "docs", "mail", "chat", "docs", "docs"};
        int k1 = 2;
        int result1 = solution.longestEfficientBlock(apps1, k1);
        System.out.println("Sample 1 Result: " + result1);
        // Expected: 3

        // Sample 2
        String[] apps2 = {"ide", "ide", "browser", "terminal", "browser", "terminal", "music"};
        int k2 = 3;
        int result2 = solution.longestEfficientBlock(apps2, k2);
        System.out.println("Sample 2 Result: " + result2);
        // Expected: 6

        // Edge case: empty array
        String[] apps3 = {};
        int k3 = 2;
        int result3 = solution.longestEfficientBlock(apps3, k3);
        System.out.println("Empty Array Result: " + result3);
        // Expected: 0

        // Edge case: k = 0
        String[] apps4 = {"mail", "mail", "docs"};
        int k4 = 0;
        int result4 = solution.longestEfficientBlock(apps4, k4);
        System.out.println("k = 0 Result: " + result4);
        // Expected: 0

        // Additional test: all same app
        String[] apps5 = {"editor", "editor", "editor", "editor"};
        int k5 = 1;
        int result5 = solution.longestEfficientBlock(apps5, k5);
        System.out.println("All Same App Result: " + result5);
        // Expected: 4

        // Additional test: every app different, k = 2
        String[] apps6 = {"a", "b", "c", "d", "e"};
        int k6 = 2;
        int result6 = solution.longestEfficientBlock(apps6, k6);
        System.out.println("All Different Apps Result: " + result6);
        // Expected: 2
    }
}