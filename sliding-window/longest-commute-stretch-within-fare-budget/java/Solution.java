import java.util.*;

/*
 * Title: Longest Commute Stretch Within Fare Budget
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array fares where fares[i] is the transit fare paid on the i-th ride
 * of a commuter's travel history, and an integer budget. A commute stretch is any
 * contiguous group of rides. Your task is to find the maximum number of consecutive rides
 * whose total fare is less than or equal to budget.
 *
 * In other words, among all subarrays of fares, return the length of the longest one
 * whose sum does not exceed budget.
 *
 * This models a common analytics problem: given a daily or weekly ride log, determine
 * the longest uninterrupted sequence of rides that could have been covered by a fixed
 * reimbursement limit.
 *
 * Return 0 if no single ride can fit within the budget.
 *
 * Constraints:
 * - 1 <= fares.length <= 100000
 * - 1 <= fares[i] <= 10000
 * - 1 <= budget <= 1000000000
 * - All fares are positive integers
 *
 * Because all fare values are positive, a sliding window solution can efficiently
 * expand and shrink a window while tracking the running sum.
 *
 * Example 1:
 * Input: fares = [2, 1, 3, 2, 1], budget = 5
 * Output: 2
 * Explanation: Valid stretches include [2,1], [3,2], and [2,1]. Any stretch of length 3
 * has total fare greater than 5, so the answer is 2.
 *
 * Example 2:
 * Input: fares = [4, 2, 1, 1, 3], budget = 6
 * Output: 3
 * Explanation: The stretch [2,1,1] has total fare 4 and length 3. Another valid stretch
 * is [1,1,3] with total fare 5 and length 3. No valid stretch of length 4 fits within
 * the budget.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to
     * the given budget.
     *
     * This method uses the sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep adding fares[right] to the running sum.
     * - If the sum becomes larger than budget, shrink the window from the left
     *   until the sum is valid again.
     * - Track the largest valid window length seen so far.
     *
     * This works because all fare values are positive:
     * - Adding a new element can only increase the sum.
     * - Removing elements from the left can only decrease the sum.
     * Therefore, once a window exceeds the budget, the only way to make it valid again
     * is to move the left pointer forward.
     *
     * @param fares the array of positive transit fares, where fares[i] is the fare of the i-th ride
     * @param budget the maximum allowed total fare for a valid commute stretch
     * @return the length of the longest contiguous stretch whose total fare is at most budget;
     *         returns 0 if no single ride fits within the budget
     *
     * Time complexity: O(n), because each element is added to the window once and removed at most once
     * Space complexity: O(1), because only a few extra variables are used
     */
    public int longestCommuteStretch(int[] fares, int budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the sum of all elements currently inside the window [left..right].
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move the right boundary one step at a time through the array.
        for (int right = 0; right < fares.length; right++) {
            // Step 1: Expand the window by including fares[right].
            currentSum += fares[right];

            // Step 2: If the window sum is too large, shrink from the left.
            // Because all numbers are positive, shrinking the window is guaranteed
            // to reduce the sum and eventually restore validity.
            while (currentSum > budget && left <= right) {
                currentSum -= fares[left];
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid
            // (its sum is <= budget), so compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the answer if this valid window is the largest so far.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If no valid ride existed, maxLength remains 0, which is exactly what we should return.
        return maxLength;
    }

    /**
     * Helper method to convert an int array into a readable string representation.
     *
     * @param array the input integer array
     * @return a string representation such as [1, 2, 3]
     *
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n), due to building the output string
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * It also includes the expected outputs so a beginner can easily compare
     * the actual result with the intended answer.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per test case, where n is the size of the fares array
     * Space complexity: O(1) extra space for the algorithm itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] fares1 = {2, 1, 3, 2, 1};
        int budget1 = 5;
        int result1 = solution.longestCommuteStretch(fares1, budget1);

        System.out.println("Example 1");
        System.out.println("Fares: " + solution.arrayToString(fares1));
        System.out.println("Budget: " + budget1);
        System.out.println("Expected Output: 2");
        System.out.println("Actual Output: " + result1);
        System.out.println();

        // Example 2
        int[] fares2 = {4, 2, 1, 1, 3};
        int budget2 = 6;
        int result2 = solution.longestCommuteStretch(fares2, budget2);

        System.out.println("Example 2");
        System.out.println("Fares: " + solution.arrayToString(fares2));
        System.out.println("Budget: " + budget2);
        System.out.println("Expected Output: 3");
        System.out.println("Actual Output: " + result2);
        System.out.println();

        // Additional demonstration: no single ride fits within the budget
        int[] fares3 = {7, 8, 9};
        int budget3 = 5;
        int result3 = solution.longestCommuteStretch(fares3, budget3);

        System.out.println("Additional Example");
        System.out.println("Fares: " + solution.arrayToString(fares3));
        System.out.println("Budget: " + budget3);
        System.out.println("Expected Output: 0");
        System.out.println("Actual Output: " + result3);
    }
}