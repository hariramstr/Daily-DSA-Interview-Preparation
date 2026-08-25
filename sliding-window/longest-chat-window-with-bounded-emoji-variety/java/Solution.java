import java.util.*;

/*
 * Title: Longest Chat Window With Bounded Emoji Variety
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A messaging platform stores the sequence of emoji reactions added to a live chat as an array of strings,
 * where each string is a single emoji code such as ":smile:" or ":fire:". Product analysts want to identify
 * the longest contiguous time window in which the conversation stayed focused enough that no more than k
 * distinct emoji types were used.
 *
 * Given an array reactions and an integer k, return the length of the longest contiguous subarray that
 * contains at most k distinct emoji strings.
 *
 * A window is contiguous, so you may only choose reactions that appear next to each other in the original
 * array. If k is 0, the answer is 0 because no emoji types are allowed. If the array is empty, return 0.
 *
 * Your solution should be efficient enough for large chat logs, so an approach that checks every possible
 * subarray will be too slow.
 *
 * Constraints:
 * - 0 <= reactions.length <= 200000
 * - 0 <= k <= reactions.length
 * - Each reactions[i] is a non-empty string of length 1 to 20
 * - reactions[i] consists of visible ASCII characters
 *
 * Example 1:
 * Input: reactions = [":smile:",":fire:",":smile:",":heart:",":fire:",":fire:"], k = 2
 * Output: 3
 * Explanation: The longest valid window is [":smile:",":fire:",":smile:"] or [":fire:",":fire:"]
 * extended with one neighboring valid emoji, but any length-4 window contains 3 distinct emoji types.
 *
 * Example 2:
 * Input: reactions = [":ok:",":ok:",":wave:",":wave:",":wave:",":star:"], k = 1
 * Output: 3
 * Explanation: The longest contiguous window with at most 1 distinct emoji type is
 * [":wave:",":wave:",":wave:"], which has length 3.
 */

public class Solution {

    /**
     * Returns the length of the longest contiguous subarray that contains at most k distinct emoji strings.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Track how many times each emoji appears inside the current window.
     * - If the window becomes invalid (more than k distinct emoji types), move the left side forward
     *   until the window becomes valid again.
     * - Record the maximum valid window length seen during the process.
     *
     * @param reactions the array of emoji reaction strings representing the chat log
     * @param k the maximum number of distinct emoji types allowed in a valid window
     * @return the length of the longest contiguous subarray with at most k distinct emoji strings
     * Time complexity: O(n), where n is reactions.length, because each index is moved at most once by
     * the left pointer and once by the right pointer
     * Space complexity: O(k) on average for the frequency map of emojis in the current window,
     * and in the worst case O(n) if many distinct strings appear while processing
     */
    public int longestChatWindowWithBoundedEmojiVariety(String[] reactions, int k) {
        // Edge case 1:
        // If the input array is null, there is no data to process.
        // Returning 0 is safe and beginner-friendly.
        if (reactions == null) {
            return 0;
        }

        // Edge case 2:
        // If the array is empty, there is no subarray at all.
        if (reactions.length == 0) {
            return 0;
        }

        // Edge case 3:
        // If k is 0, we are not allowed to include any emoji type.
        // Therefore, no non-empty window can be valid.
        if (k == 0) {
            return 0;
        }

        // This map stores the frequency of each emoji string currently inside the sliding window.
        // Key   -> emoji string
        // Value -> how many times that emoji appears in the current window
        Map<String, Integer> frequencyMap = new HashMap<>();

        // left marks the beginning of the current window.
        int left = 0;

        // maxLength stores the best answer found so far.
        int maxLength = 0;

        // We expand the window by moving right from 0 to reactions.length - 1.
        for (int right = 0; right < reactions.length; right++) {
            // Step 1:
            // Include reactions[right] into the current window.
            String currentEmoji = reactions[right];
            frequencyMap.put(currentEmoji, frequencyMap.getOrDefault(currentEmoji, 0) + 1);

            // Step 2:
            // If the number of distinct emoji types is now greater than k,
            // the window is invalid and must be shrunk from the left.
            //
            // We keep moving left forward until the window has at most k distinct types again.
            while (frequencyMap.size() > k) {
                // Identify the emoji that is leaving the window.
                String leftEmoji = reactions[left];

                // Decrease its count because it is no longer fully inside the window.
                int updatedCount = frequencyMap.get(leftEmoji) - 1;

                // If its count becomes zero, remove it from the map completely.
                // This is very important because the number of distinct emoji types
                // is exactly the number of keys in the map.
                if (updatedCount == 0) {
                    frequencyMap.remove(leftEmoji);
                } else {
                    frequencyMap.put(leftEmoji, updatedCount);
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid:
            // it contains at most k distinct emoji types.
            //
            // So we compute its length and update the best answer if needed.
            int currentWindowLength = right - left + 1;
            maxLength = Math.max(maxLength, currentWindowLength);
        }

        // After processing all possible right endpoints, maxLength is the answer.
        return maxLength;
    }

    /**
     * A convenience wrapper method with a shorter name.
     * This simply calls the main algorithm method.
     *
     * @param reactions the array of emoji reaction strings
     * @param k the maximum number of distinct emoji types allowed
     * @return the length of the longest valid contiguous subarray
     * Time complexity: O(n), where n is reactions.length
     * Space complexity: O(k) on average, worst-case O(n)
     */
    public int lengthOfLongestWindow(String[] reactions, int k) {
        return longestChatWindowWithBoundedEmojiVariety(reactions, k);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(k) on average per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] reactions1 = {":smile:", ":fire:", ":smile:", ":heart:", ":fire:", ":fire:"};
        int k1 = 2;
        int result1 = solution.longestChatWindowWithBoundedEmojiVariety(reactions1, k1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 3");

        String[] reactions2 = {":ok:", ":ok:", ":wave:", ":wave:", ":wave:", ":star:"};
        int k2 = 1;
        int result2 = solution.longestChatWindowWithBoundedEmojiVariety(reactions2, k2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 3");

        String[] reactions3 = {};
        int k3 = 2;
        int result3 = solution.longestChatWindowWithBoundedEmojiVariety(reactions3, k3);
        System.out.println("Empty Array Result: " + result3);
        System.out.println("Expected: 0");

        String[] reactions4 = {":a:", ":b:", ":c:"};
        int k4 = 0;
        int result4 = solution.longestChatWindowWithBoundedEmojiVariety(reactions4, k4);
        System.out.println("k = 0 Result: " + result4);
        System.out.println("Expected: 0");

        String[] reactions5 = {":x:", ":x:", ":x:", ":x:"};
        int k5 = 1;
        int result5 = solution.longestChatWindowWithBoundedEmojiVariety(reactions5, k5);
        System.out.println("Single Distinct Emoji Result: " + result5);
        System.out.println("Expected: 4");
    }
}