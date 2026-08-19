import java.util.*;

/*
 * Title: Longest Restock Streak with One Overstock Removal
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A warehouse records the number of units restocked each day in an integer array restocks,
 * where restocks[i] is the number of units added on day i. Management wants to identify
 * the longest streak of days that looks steadily improving. A streak is considered steadily
 * improving if, after optionally removing at most one day from that streak, the remaining
 * days form a strictly increasing sequence.
 *
 * Your task is to return the maximum possible length of a contiguous streak satisfying this rule.
 * The removed day, if any, must come from inside the chosen streak, and removing it should connect
 * the left and right parts into one strictly increasing sequence. You are not allowed to reorder days;
 * only one deletion is permitted.
 *
 * For example, in [3, 5, 4, 6, 7], the whole array qualifies because removing 4 leaves
 * [3, 5, 6, 7], which is strictly increasing. However, [2, 2, 3] does not become strictly
 * increasing unless one of the equal 2s is removed.
 *
 * Constraints:
 * - 1 <= restocks.length <= 200000
 * - -10^9 <= restocks[i] <= 10^9
 *
 * Example 1:
 * Input: restocks = [3, 5, 4, 6, 7]
 * Output: 5
 * Explanation: Choose the full streak [3, 5, 4, 6, 7] and remove 4. The remaining sequence is
 * strictly increasing.
 *
 * Example 2:
 * Input: restocks = [1, 2, 3, 2, 3, 4]
 * Output: 4
 * Explanation: One valid answer is the streak [1, 2, 3, 2]; removing the last 2 leaves [1, 2, 3].
 * Another valid answer is [2, 3, 4] with no removal. No contiguous streak of length 5 or 6 can be
 * made strictly increasing using only one deletion.
 */

public class Solution {

    /**
     * Returns the maximum length of a contiguous subarray that can become strictly increasing
     * after removing at most one element from that chosen subarray.
     *
     * Core idea:
     * 1. Precompute incLeft[i]:
     *    the length of the strictly increasing contiguous segment ending at index i.
     * 2. Precompute incRight[i]:
     *    the length of the strictly increasing contiguous segment starting at index i.
     * 3. Consider every index i as the one element we delete.
     *    - The left part is the increasing segment ending at i - 1.
     *    - The right part is the increasing segment starting at i + 1.
     *    - We can connect them only if restocks[i - 1] < restocks[i + 1].
     * 4. Also consider the case of no deletion at all, which is simply the maximum increasing run.
     *
     * Important subtlety:
     * The answer is the length of the original chosen streak, not the remaining length after deletion.
     * So if deleting index i allows us to connect a left increasing part of length L and a right
     * increasing part of length R, then the chosen streak length is L + 1 + R.
     *
     * @param restocks the array of daily restock counts
     * @return the maximum possible length of a contiguous streak that can be made strictly increasing
     *         after removing at most one element
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int longestRestockStreak(int[] restocks) {
        int n = restocks.length;

        // With only one day, the answer is trivially 1:
        // the single-element streak is already strictly increasing.
        if (n == 1) {
            return 1;
        }

        // incLeft[i] = length of the strictly increasing contiguous segment ending at i.
        int[] incLeft = new int[n];

        // incRight[i] = length of the strictly increasing contiguous segment starting at i.
        int[] incRight = new int[n];

        // Every single element by itself forms a strictly increasing segment of length 1.
        incLeft[0] = 1;

        // Build incLeft from left to right.
        for (int i = 1; i < n; i++) {
            // If current value is greater than previous value,
            // then the increasing segment can be extended.
            if (restocks[i] > restocks[i - 1]) {
                incLeft[i] = incLeft[i - 1] + 1;
            } else {
                // Otherwise, a new increasing segment starts here.
                incLeft[i] = 1;
            }
        }

        incRight[n - 1] = 1;

        // Build incRight from right to left.
        for (int i = n - 2; i >= 0; i--) {
            // If current value is smaller than next value,
            // then the increasing segment can continue to the right.
            if (restocks[i] < restocks[i + 1]) {
                incRight[i] = incRight[i + 1] + 1;
            } else {
                // Otherwise, a new increasing segment starts here.
                incRight[i] = 1;
            }
        }

        // Start with the best answer when we do NOT delete anything.
        // That is simply the longest strictly increasing contiguous segment.
        int answer = 1;
        for (int len : incLeft) {
            answer = Math.max(answer, len);
        }

        // Now try deleting each index i.
        for (int i = 0; i < n; i++) {
            int leftLen = 0;
            int rightLen = 0;

            // If there is a left side, take the increasing segment ending at i - 1.
            if (i > 0) {
                leftLen = incLeft[i - 1];
            }

            // If there is a right side, take the increasing segment starting at i + 1.
            if (i + 1 < n) {
                rightLen = incRight[i + 1];
            }

            int candidate;

            // Case 1: deleting an endpoint of the chosen streak.
            // If i is at the array boundary, there is only one side to keep.
            if (i == 0 || i == n - 1) {
                candidate = leftLen + 1 + rightLen;
            } else {
                // Case 2: deleting a middle element.
                // We can connect the left and right increasing parts only if
                // the last value on the left is strictly smaller than the first value on the right.
                if (restocks[i - 1] < restocks[i + 1]) {
                    candidate = leftLen + 1 + rightLen;
                } else {
                    // If they cannot be connected, then the chosen streak can include
                    // only one side plus the deleted element.
                    candidate = Math.max(leftLen, rightLen) + 1;
                }
            }

            answer = Math.max(answer, candidate);
        }

        return answer;
    }

    /**
     * Helper method to format an array as a readable string.
     *
     * @param arr the input array
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Runs a single demonstration test case and prints the result.
     *
     * @param restocks the input array
     * @return the computed answer for the given input
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int runDemo(int[] restocks) {
        int result = longestRestockStreak(restocks);
        System.out.println("restocks = " + arrayToString(restocks));
        System.out.println("Longest restock streak with at most one removal = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size across demo cases)
     * Space complexity: O(n) for the largest demo case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt:
        // [3, 5, 4, 6, 7]
        // Remove 4 to get [3, 5, 6, 7], so the whole streak length is 5.
        int[] sample1 = {3, 5, 4, 6, 7};
        int result1 = solution.runDemo(sample1);
        System.out.println("Expected: 5, Actual: " + result1);
        System.out.println();

        // Sample 2 from the prompt:
        // [1, 2, 3, 2, 3, 4]
        // The best valid streak length is 4.
        int[] sample2 = {1, 2, 3, 2, 3, 4};
        int result2 = solution.runDemo(sample2);
        System.out.println("Expected: 4, Actual: " + result2);
        System.out.println();

        // Additional checks:

        // Already strictly increasing: whole array qualifies with no deletion.
        int[] test1 = {1, 2, 3, 4, 5};
        solution.runDemo(test1);

        // Equal values: removing one can help.
        // [2, 2, 3] -> remove one 2, whole streak length is 3.
        int[] test2 = {2, 2, 3};
        solution.runDemo(test2);

        // Single element.
        int[] test3 = {42};
        solution.runDemo(test3);

        // A case where deleting one middle element connects both sides.
        int[] test4 = {10, 20, 15, 30, 40};
        solution.runDemo(test4);

        // A case where connection is impossible, so only one side plus deleted element counts.
        int[] test5 = {1, 5, 3, 4};
        solution.runDemo(test5);
    }
}