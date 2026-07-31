import java.util.*;

/*
 * Title: Longest Reading List Within Page Limit
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array pages where pages[i] is the number of pages in the i-th article
 * of an online reading list. A user wants to read a consecutive group of articles in one
 * session, but they can read at most maxPages pages total.
 *
 * Your task is to return the maximum number of consecutive articles the user can read
 * without the sum of pages in that group exceeding maxPages.
 *
 * In other words, find the length of the longest contiguous subarray whose sum is less
 * than or equal to maxPages.
 *
 * This problem models a common interview pattern where all values are non-negative,
 * which makes a sliding window approach efficient. As you expand the right end of the
 * window, keep track of the total pages. If the total becomes too large, move the left
 * end forward until the window is valid again. The answer is the largest valid window
 * size seen during the scan.
 *
 * Constraints:
 * - 1 <= pages.length <= 100000
 * - 0 <= pages[i] <= 10000
 * - 0 <= maxPages <= 1000000000
 * - All page counts are non-negative integers.
 *
 * Example 1:
 * Input: pages = [4, 2, 1, 7, 3, 2], maxPages = 8
 * Output: 3
 * Explanation: The longest valid consecutive group is [4, 2, 1] with total 7.
 * Any window of length 4 exceeds 8.
 *
 * Example 2:
 * Input: pages = [1, 1, 1, 1, 1], maxPages = 3
 * Output: 3
 * Explanation: Any 3 consecutive articles fit within the limit, but 4 articles
 * would total 4, which is too large.
 */

public class Solution {

    /**
     * Finds the maximum number of consecutive articles that can be read without
     * the total page count exceeding the given limit.
     *
     * This method uses the sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Add the new article's pages to the running sum.
     * - If the sum becomes too large, shrink the window from the left until the
     *   sum is valid again.
     * - Track the largest valid window length seen during the process.
     *
     * Because all page counts are non-negative, once the sum exceeds the limit,
     * moving the left pointer forward is the correct and efficient way to restore validity.
     *
     * @param pages the array where pages[i] is the number of pages in the i-th article
     * @param maxPages the maximum total number of pages the user can read in one session
     * @return the length of the longest contiguous subarray whose sum is less than or equal to maxPages
     * Time complexity: O(n), where n is the length of the pages array, because each index is visited at most twice
     * Space complexity: O(1), because only a few extra variables are used
     */
    public int longestReadingList(int[] pages, int maxPages) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Running sum of pages inside the current window [left, right].
        // We use long for extra safety, even though int would also work within given constraints.
        long currentSum = 0;

        // Move the right boundary of the window from left to right across the array.
        for (int right = 0; right < pages.length; right++) {
            // Step 1:
            // Include the article at index 'right' into the current window.
            currentSum += pages[right];

            // Step 2:
            // If the window is invalid (sum too large), shrink it from the left.
            // Since all values are non-negative, removing elements from the left
            // can only decrease the sum, which helps restore validity.
            while (currentSum > maxPages && left <= right) {
                currentSum -= pages[left];
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is valid:
            // currentSum <= maxPages
            //
            // So we compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            maxLength = Math.max(maxLength, currentLength);
        }

        // After scanning the entire array, maxLength holds the answer.
        return maxLength;
    }

    /**
     * A helper method that prints the input and the computed result in a beginner-friendly format.
     *
     * @param pages the array of article page counts
     * @param maxPages the maximum allowed total pages
     * @return the computed maximum number of consecutive readable articles
     * Time complexity: O(n), because it calls the main sliding window method and also formats the array for printing
     * Space complexity: O(1) auxiliary space, excluding the output string created for display
     */
    public int demonstrateCase(int[] pages, int maxPages) {
        int result = longestReadingList(pages, maxPages);
        System.out.println("pages = " + Arrays.toString(pages));
        System.out.println("maxPages = " + maxPages);
        System.out.println("Longest valid consecutive reading list length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample inputs from the problem statement
     * and a few additional edge-style examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(1) auxiliary space, excluding printing-related memory
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        // Expected output: 3
        int[] pages1 = {4, 2, 1, 7, 3, 2};
        int result1 = solution.demonstrateCase(pages1, 8);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println("Matches expected? " + (result1 == 3));
        System.out.println();

        // Sample Example 2
        // Expected output: 3
        int[] pages2 = {1, 1, 1, 1, 1};
        int result2 = solution.demonstrateCase(pages2, 3);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println("Matches expected? " + (result2 == 3));
        System.out.println();

        // Additional example: no article can be read if each one exceeds maxPages
        int[] pages3 = {5, 6, 7};
        int result3 = solution.demonstrateCase(pages3, 4);
        System.out.println("Expected: 0, Actual: " + result3);
        System.out.println("Matches expected? " + (result3 == 0));
        System.out.println();

        // Additional example: all articles fit
        int[] pages4 = {2, 0, 3, 1};
        int result4 = solution.demonstrateCase(pages4, 10);
        System.out.println("Expected: 4, Actual: " + result4);
        System.out.println("Matches expected? " + (result4 == 4));
        System.out.println();

        // Additional example: includes zeros and exact fits
        int[] pages5 = {0, 0, 0, 2, 1};
        int result5 = solution.demonstrateCase(pages5, 2);
        System.out.println("Expected: 4, Actual: " + result5);
        System.out.println("Matches expected? " + (result5 == 4));
    }
}