import java.util.*;

/*
 * Title: Longest Caption Segment Within Reading Speed
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A video platform stores the number of words shown in each consecutive subtitle frame
 * in an integer array words, where words[i] is the word count displayed during the i-th
 * frame interval. A viewer has a reading capacity limit maxWords, meaning they can
 * comfortably follow any contiguous segment of subtitle frames only if the total number
 * of words shown in that segment does not exceed maxWords.
 *
 * Your task is to return the length of the longest contiguous segment of frames the viewer
 * can watch comfortably. In other words, find the maximum size of a subarray whose sum is
 * less than or equal to maxWords.
 *
 * This problem should be solved efficiently for large inputs. A brute-force solution that
 * checks every possible segment will be too slow. Since all values in words are non-negative,
 * a sliding window approach is expected.
 *
 * Constraints:
 * - 1 <= words.length <= 100000
 * - 0 <= words[i] <= 10000
 * - 0 <= maxWords <= 10^9
 *
 * Example 1:
 * Input: words = [3, 1, 2, 1, 1, 4], maxWords = 5
 * Output: 4
 * Explanation: The longest valid segment is [1, 2, 1, 1], whose total is 5, so the answer is 4.
 *
 * Example 2:
 * Input: words = [5, 2, 2, 6, 1], maxWords = 7
 * Output: 2
 * Explanation: Valid longest segments include [5, 2] and [6, 1], each with total 7.
 * No contiguous segment of length 3 or more stays within the reading limit.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to maxWords.
     *
     * This method uses the classic sliding window / two-pointer technique.
     * The key reason this works is that every value in the input array is non-negative.
     * Because of that:
     * - Expanding the window to the right can only keep the sum the same or increase it.
     * - Shrinking the window from the left can only keep the sum the same or decrease it.
     *
     * That monotonic behavior allows us to process the array in linear time.
     *
     * @param words the array where words[i] is the number of words shown in the i-th frame interval
     * @param maxWords the maximum total number of words allowed in a comfortable contiguous segment
     * @return the length of the longest contiguous segment whose sum is less than or equal to maxWords
     * Time complexity: O(n), where n is words.length, because each index is visited at most twice
     * Space complexity: O(1), because only a few extra variables are used
     */
    public int longestCaptionSegment(int[] words, int maxWords) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This will store the running sum of the current window [left..right].
        long currentSum = 0;

        // This keeps track of the best (maximum) valid window length seen so far.
        int maxLength = 0;

        // Move the right boundary one step at a time across the array.
        for (int right = 0; right < words.length; right++) {
            // Step 1: Include the new element at index 'right' into the current window.
            currentSum += words[right];

            // Step 2: If the window sum is now too large, shrink from the left
            // until the window becomes valid again.
            //
            // We use a while loop, not an if statement, because one removal may not be enough.
            // Example:
            // currentSum = 20, maxWords = 7
            // We may need to remove several elements from the left.
            while (currentSum > maxWords && left <= right) {
                currentSum -= words[left];
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid
            // (sum <= maxWords), so we can compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength contains the answer.
        return maxLength;
    }

    /**
     * A helper method that prints the input and result in a beginner-friendly format.
     *
     * @param words the subtitle word counts per frame interval
     * @param maxWords the maximum allowed sum for a valid contiguous segment
     * @return the computed longest valid segment length
     * Time complexity: O(n), because it calls the linear-time sliding window method
     * Space complexity: O(1), excluding the space used by output formatting
     */
    public int demonstrateCase(int[] words, int maxWords) {
        int result = longestCaptionSegment(words, maxWords);
        System.out.println("words = " + Arrays.toString(words));
        System.out.println("maxWords = " + maxWords);
        System.out.println("Longest valid segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified sample outputs:
     * - Example 1: [3, 1, 2, 1, 1, 4], maxWords = 5 -> 4
     * - Example 2: [5, 2, 2, 6, 1], maxWords = 7 -> 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] words1 = {3, 1, 2, 1, 1, 4};
        int maxWords1 = 5;
        int result1 = solution.demonstrateCase(words1, maxWords1);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result1);
        System.out.println();

        // Example 2
        int[] words2 = {5, 2, 2, 6, 1};
        int maxWords2 = 7;
        int result2 = solution.demonstrateCase(words2, maxWords2);
        System.out.println("Expected: 2");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // Additional quick checks
        int[] words3 = {0, 0, 0, 0};
        int maxWords3 = 0;
        int result3 = solution.demonstrateCase(words3, maxWords3);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result3);
        System.out.println();

        int[] words4 = {10, 20, 30};
        int maxWords4 = 5;
        int result4 = solution.demonstrateCase(words4, maxWords4);
        System.out.println("Expected: 0");
        System.out.println("Actual:   " + result4);
    }
}