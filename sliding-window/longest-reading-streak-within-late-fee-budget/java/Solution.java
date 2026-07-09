import java.util.*;

/*
 * Title: Longest Reading Streak Within Late Fee Budget
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array lateFees where lateFees[i] is the late fee charged on day i
 * for a borrowed library item. A student wants to study over a consecutive block of days,
 * but the total late fees during that block must not exceed a given budget B.
 *
 * Return the length of the longest contiguous subarray whose sum is less than or equal to B.
 *
 * In other words, find the maximum number of consecutive days the student can keep the item
 * while ensuring the total accumulated late fees in that chosen window stay within budget.
 *
 * This problem is designed to be solved efficiently using a sliding window technique.
 * Since all late fees are non-negative, once the current window exceeds the budget,
 * moving the left boundary to the right is the correct way to restore validity.
 *
 * Constraints:
 * - 1 <= lateFees.length <= 100000
 * - 0 <= lateFees[i] <= 10000
 * - 0 <= B <= 1000000000
 * - lateFees contains only non-negative integers
 *
 * Example 1:
 * Input: lateFees = [2, 1, 3, 2, 1], B = 5
 * Output: 2
 * Explanation: Valid windows include [2,1], [3,2], and [2,1]. Any window of length 3
 * has total fee greater than 5, so the answer is 2.
 *
 * Example 2:
 * Input: lateFees = [0, 1, 1, 0, 2, 1], B = 3
 * Output: 4
 * Explanation: One optimal window is [1,1,0,1] formed by days with fees [1,1,0,1],
 * which sums to 3. No longer contiguous window stays within the budget.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to the given budget.
     *
     * This method uses the sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep track of the current sum of the window.
     * - If the sum becomes larger than the budget, shrink the window from the left
     *   until the sum becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * This works correctly because all values in lateFees are non-negative.
     * That property guarantees that:
     * - Expanding the window can only keep or increase the sum.
     * - Shrinking the window can only keep or decrease the sum.
     * Therefore, once a window exceeds the budget, the only way to make it valid again
     * is to move the left boundary to the right.
     *
     * @param lateFees the array of non-negative late fees for each day
     * @param budget the maximum allowed total late fee for the chosen consecutive block
     * @return the length of the longest contiguous subarray with sum less than or equal to budget
     * Time complexity: O(n), because each element is added to the window once and removed at most once.
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestReadingStreak(int[] lateFees, int budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This will store the sum of the current window [left..right].
        long currentSum = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move the right boundary from the beginning to the end of the array.
        for (int right = 0; right < lateFees.length; right++) {
            // Step 1:
            // Include the new element at index 'right' into the current window.
            currentSum += lateFees[right];

            // Step 2:
            // If the current window sum is too large, we must shrink the window
            // from the left side until the sum becomes <= budget again.
            //
            // Because all numbers are non-negative, removing elements from the left
            // is the correct and sufficient way to restore validity.
            while (currentSum > budget && left <= right) {
                currentSum -= lateFees[left];
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is valid:
            // currentSum <= budget
            //
            // So we compute its length and update the answer if this window is longer
            // than any valid window seen before.
            int currentLength = right - left + 1;
            maxLength = Math.max(maxLength, currentLength);
        }

        // After processing all possible right boundaries, maxLength contains
        // the length of the longest valid contiguous subarray.
        return maxLength;
    }

    /**
     * Utility method to print an integer array in a beginner-friendly format.
     *
     * @param array the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the StringBuilder used to build the output string.
     */
    public String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input array
     * - the budget
     * - the computed result
     * - the expected result
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for the demonstrated test cases.
     * Space complexity: O(1) extra space excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        int[] lateFees1 = {2, 1, 3, 2, 1};
        int budget1 = 5;
        int result1 = solution.longestReadingStreak(lateFees1, budget1);

        System.out.println("Example 1");
        System.out.println("lateFees = " + solution.arrayToString(lateFees1));
        System.out.println("B = " + budget1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Sample Example 2
        int[] lateFees2 = {0, 1, 1, 0, 2, 1};
        int budget2 = 3;
        int result2 = solution.longestReadingStreak(lateFees2, budget2);

        System.out.println("Example 2");
        System.out.println("lateFees = " + solution.arrayToString(lateFees2));
        System.out.println("B = " + budget2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional quick checks for beginners
        int[] lateFees3 = {5};
        int budget3 = 5;
        System.out.println("Additional Test 1");
        System.out.println("lateFees = " + solution.arrayToString(lateFees3));
        System.out.println("B = " + budget3);
        System.out.println("Output = " + solution.longestReadingStreak(lateFees3, budget3));
        System.out.println("Expected = 1");
        System.out.println();

        int[] lateFees4 = {6};
        int budget4 = 5;
        System.out.println("Additional Test 2");
        System.out.println("lateFees = " + solution.arrayToString(lateFees4));
        System.out.println("B = " + budget4);
        System.out.println("Output = " + solution.longestReadingStreak(lateFees4, budget4));
        System.out.println("Expected = 0");
        System.out.println();

        int[] lateFees5 = {0, 0, 0, 0};
        int budget5 = 0;
        System.out.println("Additional Test 3");
        System.out.println("lateFees = " + solution.arrayToString(lateFees5));
        System.out.println("B = " + budget5);
        System.out.println("Output = " + solution.longestReadingStreak(lateFees5, budget5));
        System.out.println("Expected = 4");
    }
}