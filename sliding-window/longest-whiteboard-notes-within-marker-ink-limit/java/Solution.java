import java.util.*;

/*
 * Title: Longest Whiteboard Notes Within Marker Ink Limit
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A teacher writes a sequence of note segments on a digital whiteboard. The i-th segment uses
 * ink[i] units of marker ink. You are given an integer array ink where each value is non-negative,
 * and an integer maxInk representing the maximum total ink that can be used before the marker
 * must be replaced.
 *
 * Your task is to find the length of the longest contiguous group of note segments whose total
 * ink usage is less than or equal to maxInk.
 *
 * In other words, choose a subarray ink[l..r] such that the sum of its elements does not exceed
 * maxInk, and return the maximum possible number of segments in such a subarray.
 *
 * This problem is designed to be solved efficiently using the sliding window technique. Since all
 * ink values are non-negative, once a window exceeds the limit, moving the left pointer forward
 * can only decrease the total.
 *
 * Constraints:
 * - 1 <= ink.length <= 100000
 * - 0 <= ink[i] <= 10000
 * - 0 <= maxInk <= 1000000000
 *
 * Example 1:
 * Input: ink = [2, 1, 3, 2, 1], maxInk = 5
 * Output: 2
 * Explanation: Valid windows include [2,1], [3,2], and [2,1]. No contiguous window of length 3
 * has total ink at most 5.
 *
 * Example 2:
 * Input: ink = [0, 2, 1, 0, 1, 1], maxInk = 3
 * Output: 4
 * Explanation:
 * The correct longest valid contiguous subarrays include:
 * - [0,2,1,0] with sum 3 and length 4
 * - [1,0,1,1] with sum 3 and length 4
 * No valid contiguous subarray of length 5 exists in the given array under the limit.
 * Therefore, the correct answer is 4.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to maxInk.
     *
     * This method uses the classic sliding window technique:
     * 1. Expand the window by moving the right pointer.
     * 2. Add the new element to the running sum.
     * 3. If the sum becomes larger than maxInk, shrink the window from the left
     *    until the sum is valid again.
     * 4. Track the largest valid window length seen so far.
     *
     * This works because all values in the array are non-negative. That property guarantees that:
     * - Expanding the window can only increase or keep the sum the same.
     * - Shrinking the window can only decrease or keep the sum the same.
     *
     * @param ink the array where each element represents ink usage of a note segment
     * @param maxInk the maximum allowed total ink usage for a valid contiguous group
     * @return the length of the longest contiguous subarray with sum less than or equal to maxInk
     * Time complexity: O(n), because each element is added to and removed from the window at most once
     * Space complexity: O(1), because only a few variables are used
     */
    public int longestNotesWithinInkLimit(int[] ink, int maxInk) {
        int left = 0;
        int bestLength = 0;

        long currentSum = 0L;

        for (int right = 0; right < ink.length; right++) {
            currentSum += ink[right];

            while (currentSum > maxInk && left <= right) {
                currentSum -= ink[left];
                left++;
            }

            int currentWindowLength = right - left + 1;
            if (currentWindowLength > bestLength) {
                bestLength = currentWindowLength;
            }
        }

        return bestLength;
    }

    /**
     * Helper method to print an example input and the computed answer.
     *
     * @param ink the input array of ink usage values
     * @param maxInk the maximum allowed sum
     * @return the computed maximum valid window length
     * Time complexity: O(n), because it delegates to the sliding window method
     * Space complexity: O(1), excluding the space used by printing the array representation
     */
    public int demonstrateExample(int[] ink, int maxInk) {
        int answer = longestNotesWithinInkLimit(ink, maxInk);
        System.out.println("ink = " + Arrays.toString(ink));
        System.out.println("maxInk = " + maxInk);
        System.out.println("Longest valid contiguous length = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Main method to demonstrate the solution on sample inputs.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] ink1 = {2, 1, 3, 2, 1};
        int maxInk1 = 5;
        int result1 = solution.demonstrateExample(ink1, maxInk1);
        System.out.println("Expected: 2");
        System.out.println("Actual:   " + result1);
        System.out.println();

        int[] ink2 = {0, 2, 1, 0, 1, 1};
        int maxInk2 = 3;
        int result2 = solution.demonstrateExample(ink2, maxInk2);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result2);
        System.out.println();

        int[] ink3 = {1, 2, 3, 4};
        int maxInk3 = 6;
        solution.demonstrateExample(ink3, maxInk3);

        int[] ink4 = {0, 0, 0, 0};
        int maxInk4 = 0;
        solution.demonstrateExample(ink4, maxInk4);
    }
}