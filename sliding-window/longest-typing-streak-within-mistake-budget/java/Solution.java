import java.util.*;

/*
Problem Title: Longest Typing Streak Within Mistake Budget

Problem Description:
A typing practice platform stores a student's results as a binary array mistakes,
where mistakes[i] = 1 means the i-th typed word contained a mistake and
mistakes[i] = 0 means it was typed correctly.

The platform wants to highlight the longest continuous streak that can be considered
"acceptable" if the student is allowed to ignore at most k mistaken words inside that streak.

Given an integer array mistakes and an integer k, return the length of the longest
contiguous subarray containing at most k values equal to 1.

In other words, you must find the maximum size of a consecutive block of words where
the number of mistakes does not exceed the allowed budget k.

This problem should be solved efficiently for large inputs, so solutions that check
every possible subarray will be too slow.

Constraints:
- 1 <= mistakes.length <= 200000
- mistakes[i] is either 0 or 1
- 0 <= k <= mistakes.length

Example 1:
Input: mistakes = [0,1,0,0,1,0,0,0], k = 1
Output: 5

Example 2:
Input: mistakes = [1,1,0,0,0,1,0,0,1,0], k = 2
Output: 8
*/

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray that contains
     * at most k values equal to 1.
     *
     * This method uses the sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Count how many mistakes (1s) are currently inside the window.
     * - If the number of mistakes becomes greater than k, move the left side
     *   of the window forward until the window becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * @param mistakes the binary array where 1 represents a mistake and 0 represents a correct word
     * @param k the maximum number of mistakes allowed inside the chosen contiguous subarray
     * @return the maximum length of a contiguous subarray containing at most k ones
     *
     * Time complexity: O(n), where n is the length of the array, because each pointer
     * moves from left to right at most once.
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int longestTypingStreakWithinBudget(int[] mistakes, int k) {
        // The left boundary of our sliding window.
        int left = 0;

        // This variable stores how many 1s (mistakes) are currently inside the window.
        int onesInWindow = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We move the right boundary from the start of the array to the end.
        for (int right = 0; right < mistakes.length; right++) {

            // Step 1:
            // Include mistakes[right] into the current window.
            // If it is a mistake (1), increase the count of mistakes in the window.
            if (mistakes[right] == 1) {
                onesInWindow++;
            }

            // Step 2:
            // If the window has too many mistakes, it is invalid.
            // We must shrink it from the left until the number of mistakes is at most k again.
            while (onesInWindow > k) {

                // Before moving left forward, check whether the element leaving the window
                // is a mistake. If yes, reduce the mistake count.
                if (mistakes[left] == 1) {
                    onesInWindow--;
                }

                // Move the left boundary one step to the right,
                // effectively removing mistakes[left] from the window.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid,
            // meaning it contains at most k mistakes.
            // So we compute its length.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is longer than any previous one.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After processing all possible right boundaries, maxLength is the answer.
        return maxLength;
    }

    /**
     * A small helper method to print an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input array
     * - the value of k
     * - the computed result
     * - the expected result for comparison
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) overall for each demonstrated test case.
     * Space complexity: O(1) extra, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample test case 1
        int[] mistakes1 = {0, 1, 0, 0, 1, 0, 0, 0};
        int k1 = 1;
        int result1 = solution.longestTypingStreakWithinBudget(mistakes1, k1);

        System.out.println("Example 1:");
        System.out.println("mistakes = " + solution.arrayToString(mistakes1));
        System.out.println("k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 5");
        System.out.println();

        // Sample test case 2
        int[] mistakes2 = {1, 1, 0, 0, 0, 1, 0, 0, 1, 0};
        int k2 = 2;
        int result2 = solution.longestTypingStreakWithinBudget(mistakes2, k2);

        System.out.println("Example 2:");
        System.out.println("mistakes = " + solution.arrayToString(mistakes2));
        System.out.println("k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 8");
        System.out.println();

        // Additional quick checks for edge cases
        int[] mistakes3 = {0, 0, 0, 0};
        int k3 = 0;
        int result3 = solution.longestTypingStreakWithinBudget(mistakes3, k3);

        System.out.println("Additional Test 1:");
        System.out.println("mistakes = " + solution.arrayToString(mistakes3));
        System.out.println("k = " + k3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 4");
        System.out.println();

        int[] mistakes4 = {1, 1, 1, 1};
        int k4 = 2;
        int result4 = solution.longestTypingStreakWithinBudget(mistakes4, k4);

        System.out.println("Additional Test 2:");
        System.out.println("mistakes = " + solution.arrayToString(mistakes4));
        System.out.println("k = " + k4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 2");
    }
}