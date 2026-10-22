/*
Problem Title: Find the Last Passed Practice Exam

Problem Description:
You are given a sorted array `scores` where `scores[i]` is the score of the `i`-th practice exam taken by a student.
The array is sorted in non-decreasing order, so later values may be equal to earlier ones.
You are also given an integer `passMark`, representing the minimum score required to pass.

Your task is to return the index of the last exam whose score is less than or equal to `passMark`
if the goal is to find the final exam result that still counts as passing under a given cutoff.
If no exam score is less than or equal to `passMark`, return `-1`.

Because the scores are already sorted, the expected solution should use binary search instead of
scanning the entire array. This makes the solution efficient even when the number of exams is large.

Constraints:
- 1 <= scores.length <= 100000
- 0 <= scores[i] <= 10^9
- scores is sorted in non-decreasing order
- 0 <= passMark <= 10^9

Example 1:
Input: scores = [35, 42, 42, 58, 71, 88], passMark = 58
Output: 3
Explanation: The last score that is less than or equal to 58 is 58, which appears at index 3.

Example 2:
Input: scores = [60, 75, 80], passMark = 50
Output: -1
Explanation: Every score is greater than 50, so there is no valid passing exam under this cutoff.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the index of the last element in the sorted array whose value is less than or equal to passMark.
     *
     * This method uses binary search to efficiently locate the rightmost valid index.
     * Because the array is sorted in non-decreasing order, once we find a value that is
     * less than or equal to passMark, we know that value could be a valid answer, but there
     * might still be another valid value farther to the right. So we continue searching on
     * the right side to find the last such occurrence.
     *
     * @param scores the sorted array of exam scores in non-decreasing order
     * @param passMark the cutoff score; we want the last index where scores[index] <= passMark
     * @return the index of the last score less than or equal to passMark, or -1 if no such score exists
     *
     * Time complexity: O(log n), where n is the length of the scores array
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int findLastPassedPracticeExam(int[] scores, int passMark) {
        // 'left' points to the beginning of the current search range.
        int left = 0;

        // 'right' points to the end of the current search range.
        int right = scores.length - 1;

        // 'answer' stores the best valid index found so far.
        // We start with -1 because if no score is <= passMark, the required result is -1.
        int answer = -1;

        // Continue searching while the current range is valid.
        // A valid range means left has not crossed right.
        while (left <= right) {
            // Compute the middle index safely.
            // This avoids potential integer overflow that could happen with (left + right) / 2.
            int mid = left + (right - left) / 2;

            // Check the value at the middle.
            if (scores[mid] <= passMark) {
                // This middle score is valid because it is <= passMark.
                // So mid is a candidate answer.
                answer = mid;

                // But we are specifically looking for the LAST such index.
                // Therefore, we must continue searching to the RIGHT side
                // to see if there is another valid score later in the array.
                left = mid + 1;
            } else {
                // This middle score is too large.
                // Since the array is sorted, every element to the right of mid
                // will also be >= scores[mid], and therefore also too large.
                // So we discard the right half and search only on the LEFT side.
                right = mid - 1;
            }
        }

        // After the loop ends, 'answer' contains the rightmost valid index found,
        // or -1 if no valid score existed.
        return answer;
    }

    /**
     * A helper method that prints the input array, the passMark, and the computed result.
     * This is useful for demonstration in main.
     *
     * @param scores the sorted array of exam scores
     * @param passMark the cutoff score
     * @return the computed index of the last score less than or equal to passMark
     *
     * Time complexity: O(log n) for the search itself, where n is the length of the scores array
     * Space complexity: O(1) extra space, excluding output formatting performed by the library
     */
    public int demonstrateAndSolve(int[] scores, int passMark) {
        int result = findLastPassedPracticeExam(scores, passMark);
        System.out.println("scores = " + Arrays.toString(scores));
        System.out.println("passMark = " + passMark);
        System.out.println("Output: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution using the sample inputs from the problem statement
     * and a few additional examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(log n) per demonstration call
     * Space complexity: O(1) extra space, excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // scores = [35, 42, 42, 58, 71, 88], passMark = 58
        // The last score <= 58 is 58 at index 3.
        solution.demonstrateAndSolve(new int[]{35, 42, 42, 58, 71, 88}, 58);

        // Example 2:
        // scores = [60, 75, 80], passMark = 50
        // Every score is greater than 50, so the answer is -1.
        solution.demonstrateAndSolve(new int[]{60, 75, 80}, 50);

        // Additional beginner-friendly checks:

        // All values are <= passMark, so the answer should be the last index.
        solution.demonstrateAndSolve(new int[]{10, 20, 30, 40}, 100);

        // Multiple equal values at the end that are still valid.
        solution.demonstrateAndSolve(new int[]{5, 5, 5, 5}, 5);

        // passMark matches a repeated middle value; answer should be the last occurrence of that value.
        solution.demonstrateAndSolve(new int[]{1, 2, 2, 2, 3, 4}, 2);
    }
}