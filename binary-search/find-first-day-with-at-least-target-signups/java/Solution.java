/*
 * Title: Find First Day With At Least Target Signups
 * Difficulty: Easy
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given a non-decreasing integer array `signups`, where `signups[i]`
 * represents the total number of users who have signed up for a product by the
 * end of day `i`. Because this is a cumulative total, the array is sorted in
 * non-decreasing order. You are also given an integer `target` representing a
 * milestone number of total signups.
 *
 * Your task is to return the earliest day index at which the total number of
 * signups is greater than or equal to `target`. If the milestone is never
 * reached, return `-1`.
 *
 * This problem models a common analytics task: product teams often store
 * cumulative counts over time and need to quickly answer milestone queries.
 * A linear scan works, but interviewers expect a faster solution by using
 * binary search on the sorted cumulative totals.
 *
 * Return the smallest index `i` such that `signups[i] >= target`.
 *
 * Constraints:
 * - 1 <= signups.length <= 100000
 * - 0 <= signups[i] <= 1000000000
 * - signups is sorted in non-decreasing order
 * - 0 <= target <= 1000000000
 *
 * Example 1:
 * Input: signups = [3, 5, 5, 9, 14], target = 6
 * Output: 3
 * Explanation: The first day where total signups are at least 6 is day index 3,
 * where the value is 9.
 *
 * Example 2:
 * Input: signups = [1, 2, 4, 4, 7], target = 4
 * Output: 2
 * Explanation: Although 4 appears more than once, the earliest day index with
 * at least 4 signups is 2.
 *
 * If no such day exists, return -1. Your solution should run in O(log n) time.
 */

import java.util.*;

public class Solution {

    /**
     * Finds the earliest index where the cumulative signup count is greater than
     * or equal to the given target.
     *
     * This method uses binary search because the input array is sorted in
     * non-decreasing order. We are specifically looking for the "first position"
     * where signups[i] >= target, which is a classic lower-bound search.
     *
     * @param signups the non-decreasing array of cumulative signup totals by day
     * @param target the milestone signup count we want to reach or exceed
     * @return the smallest index i such that signups[i] >= target; returns -1 if
     *         no such index exists
     *
     * Time complexity: O(log n), where n is the length of the signups array
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int firstDayAtLeastTarget(int[] signups, int target) {
        // We maintain a search window [left, right].
        // Initially, the entire array is a candidate search space.
        int left = 0;
        int right = signups.length - 1;

        // This variable will store the best answer found so far.
        // We start with -1, meaning "not found yet".
        int answer = -1;

        // Continue searching while the window is valid.
        while (left <= right) {
            // Compute the middle index safely.
            // Using left + (right - left) / 2 avoids potential integer overflow.
            int mid = left + (right - left) / 2;

            // If the value at mid is large enough, then mid is a valid candidate.
            if (signups[mid] >= target) {
                // Record mid as a possible answer.
                answer = mid;

                // But we are not done yet:
                // since we need the FIRST such index, we continue searching
                // on the LEFT side to see if an earlier valid index exists.
                right = mid - 1;
            } else {
                // If signups[mid] < target, then mid cannot be the answer,
                // and neither can anything to the left of mid, because the array
                // is sorted in non-decreasing order.
                //
                // Therefore, we move to the RIGHT half.
                left = mid + 1;
            }
        }

        // If we found at least one valid index, answer holds the earliest one.
        // Otherwise, it remains -1.
        return answer;
    }

    /**
     * A helper method that prints an array in a readable format and shows the
     * result of the binary search query.
     *
     * @param signups the non-decreasing array of cumulative signup totals
     * @param target the milestone signup count to search for
     * @return the computed earliest day index where signups[i] >= target
     *
     * Time complexity: O(log n) for the search itself, plus O(n) only for
     * printing the array contents
     * Space complexity: O(1), excluding the internal memory used by output handling
     */
    public int demonstrateCase(int[] signups, int target) {
        int result = firstDayAtLeastTarget(signups, target);
        System.out.println("signups = " + Arrays.toString(signups) + ", target = " + target);
        System.out.println("Earliest day index with at least target signups: " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * This main method verifies the examples from the prompt and also shows a few
     * additional edge cases so beginners can better understand the behavior.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(log n) per test case for the search, plus printing cost
     * Space complexity: O(1), excluding output-related internals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // signups = [3, 5, 5, 9, 14], target = 6
        // The first value >= 6 is 9 at index 3.
        solution.demonstrateCase(new int[]{3, 5, 5, 9, 14}, 6);

        // Example 2 from the problem statement:
        // signups = [1, 2, 4, 4, 7], target = 4
        // The first value >= 4 is 4 at index 2.
        solution.demonstrateCase(new int[]{1, 2, 4, 4, 7}, 4);

        // Additional example:
        // Target is smaller than or equal to the first element,
        // so the answer should be 0.
        solution.demonstrateCase(new int[]{2, 3, 5, 8}, 1);

        // Additional example:
        // Target is larger than every element, so the answer should be -1.
        solution.demonstrateCase(new int[]{1, 3, 6, 10}, 15);

        // Additional example:
        // Repeated values and target exactly matches them.
        solution.demonstrateCase(new int[]{0, 0, 0, 5, 5, 9}, 5);
    }
}