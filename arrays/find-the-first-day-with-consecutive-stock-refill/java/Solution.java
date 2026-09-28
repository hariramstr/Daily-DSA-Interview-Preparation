import java.util.*;

/*
 * Title: Find the First Day With Consecutive Stock Refill
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * A store tracks the number of units added to inventory each day in an integer array refills,
 * where refills[i] is the number of items restocked on day i. A manager wants to know the
 * earliest day when the store begins a streak of at least k consecutive days with a positive refill.
 * In other words, find the smallest index i such that refills[i], refills[i + 1], ...,
 * refills[i + k - 1] are all greater than 0. If no such streak exists, return -1.
 *
 * Your task is to write a function that returns the starting index of the first valid streak.
 * This is a straightforward array scanning problem, but careful handling of edge cases is important.
 * For example, if k is 1, any day with a positive refill is already a valid streak.
 * If the array is shorter than k, the answer must be -1.
 *
 * You may assume the input array contains non-negative integers. A refill value of 0 means
 * no stock was added on that day, which breaks any consecutive streak.
 *
 * Constraints:
 * - 1 <= refills.length <= 100000
 * - 0 <= refills[i] <= 1000000
 * - 1 <= k <= 100000
 *
 * Example 1:
 * Input: refills = [0, 3, 2, 5, 0, 4], k = 3
 * Output: 1
 * Explanation: The first streak of 3 consecutive positive refill days starts at index 1
 * because values [3, 2, 5] are all greater than 0.
 *
 * Example 2:
 * Input: refills = [1, 0, 2, 3, 0, 1], k = 2
 * Output: 2
 * Explanation: The first valid streak of length 2 is [2, 3], which starts at index 2.
 */

public class Solution {

    /**
     * Finds the earliest index where a streak of at least k consecutive positive refill days begins.
     *
     * The idea is simple:
     * 1. Walk through the array from left to right.
     * 2. Keep track of how many consecutive days so far have a positive refill.
     * 3. If the current value is greater than 0, extend the current streak.
     * 4. If the current value is 0, the streak is broken, so reset the counter to 0.
     * 5. As soon as the streak length becomes k, we know the first valid starting index is:
     *    currentIndex - k + 1
     * 6. Return that index immediately because we want the earliest such streak.
     *
     * @param refills the array where refills[i] represents the number of items restocked on day i
     * @param k the required number of consecutive days with positive refill
     * @return the starting index of the first valid streak of length at least k; returns -1 if no such streak exists
     * Time complexity: O(n), where n is the length of the refills array
     * Space complexity: O(1), because only a few extra variables are used
     */
    public int firstDayWithConsecutiveRefill(int[] refills, int k) {
        // Defensive check:
        // If the array reference itself is null, there is no valid answer.
        if (refills == null) {
            return -1;
        }

        // If k is larger than the number of available days,
        // it is impossible to have a streak of length k.
        if (refills.length < k) {
            return -1;
        }

        // This variable stores the length of the current consecutive streak
        // of days where refill > 0.
        int consecutivePositiveDays = 0;

        // Scan every day from left to right.
        for (int i = 0; i < refills.length; i++) {

            // If today's refill is positive, then today's day can be part of a valid streak.
            if (refills[i] > 0) {
                // Extend the current streak by 1.
                consecutivePositiveDays++;

                // The moment the streak length reaches k,
                // we have found the earliest valid starting index.
                if (consecutivePositiveDays == k) {
                    // If the streak ends at index i and has length k,
                    // then it starts at i - k + 1.
                    return i - k + 1;
                }
            } else {
                // A refill of 0 breaks the streak completely.
                // So we reset the count and start fresh from the next day.
                consecutivePositiveDays = 0;
            }
        }

        // If we finish scanning the entire array and never reach a streak of length k,
        // then no valid answer exists.
        return -1;
    }

    /**
     * Converts an integer array into a readable string representation.
     * This helper method is used only for clean demonstration output in main.
     *
     * @param arr the input integer array
     * @return a string like [1, 2, 3]
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n), due to the StringBuilder content
     */
    public String arrayToString(int[] arr) {
        if (arr == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arr[i]);
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(m), where m is the total number of elements across demonstrated test arrays
     * Space complexity: O(1) extra space excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // refills = [0, 3, 2, 5, 0, 4], k = 3
        // The first streak of 3 consecutive positive values is [3, 2, 5],
        // which starts at index 1.
        int[] refills1 = {0, 3, 2, 5, 0, 4};
        int k1 = 3;
        int result1 = solution.firstDayWithConsecutiveRefill(refills1, k1);
        System.out.println("Example 1:");
        System.out.println("refills = " + solution.arrayToString(refills1) + ", k = " + k1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 1");
        System.out.println();

        // Example 2 from the problem statement:
        // refills = [1, 0, 2, 3, 0, 1], k = 2
        // The first valid streak of length 2 is [2, 3], starting at index 2.
        int[] refills2 = {1, 0, 2, 3, 0, 1};
        int k2 = 2;
        int result2 = solution.firstDayWithConsecutiveRefill(refills2, k2);
        System.out.println("Example 2:");
        System.out.println("refills = " + solution.arrayToString(refills2) + ", k = " + k2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 2");
        System.out.println();

        // Additional edge case:
        // k = 1 means any positive refill day is immediately valid.
        int[] refills3 = {0, 0, 7, 0};
        int k3 = 1;
        int result3 = solution.firstDayWithConsecutiveRefill(refills3, k3);
        System.out.println("Edge Case 1:");
        System.out.println("refills = " + solution.arrayToString(refills3) + ", k = " + k3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 2");
        System.out.println();

        // Additional edge case:
        // Array shorter than k, so answer must be -1.
        int[] refills4 = {5, 6};
        int k4 = 3;
        int result4 = solution.firstDayWithConsecutiveRefill(refills4, k4);
        System.out.println("Edge Case 2:");
        System.out.println("refills = " + solution.arrayToString(refills4) + ", k = " + k4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: -1");
        System.out.println();

        // Additional edge case:
        // No streak exists because zeros keep breaking the sequence.
        int[] refills5 = {1, 0, 1, 0, 1, 0};
        int k5 = 2;
        int result5 = solution.firstDayWithConsecutiveRefill(refills5, k5);
        System.out.println("Edge Case 3:");
        System.out.println("refills = " + solution.arrayToString(refills5) + ", k = " + k5);
        System.out.println("Output: " + result5);
        System.out.println("Expected: -1");
    }
}