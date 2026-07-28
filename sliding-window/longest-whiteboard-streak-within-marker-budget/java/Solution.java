import java.util.*;

/*
 * Title: Longest Whiteboard Streak Within Marker Budget
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A teacher is writing a sequence of lesson sections on a whiteboard. The array
 * inkUse represents how many units of marker ink are needed for each section, in order.
 * Because the teacher only has a limited amount of ink for one uninterrupted writing
 * session, you need to find the longest contiguous group of sections that can be written
 * without exceeding the available ink budget maxInk.
 *
 * Return the length of the longest contiguous subarray whose sum is less than or equal
 * to maxInk.
 *
 * This is a classic interview setting for a sliding window because all values in inkUse
 * are non-negative. As the right end of the window expands, the total ink usage increases.
 * If the total becomes too large, move the left end forward until the window becomes valid again.
 *
 * Constraints:
 * - 1 <= inkUse.length <= 100000
 * - 0 <= inkUse[i] <= 10000
 * - 0 <= maxInk <= 1000000000
 * - All section ink costs are non-negative integers.
 *
 * Example 1:
 * Input: inkUse = [2, 1, 3, 2, 1], maxInk = 5
 * Output: 2
 * Explanation: Valid contiguous groups include [2,1], [3,2], and [2,1].
 * Any length-3 group uses more than 5 units of ink, so the answer is 2.
 *
 * Example 2:
 * Input: inkUse = [1, 0, 2, 1, 1, 0, 1], maxInk = 4
 * Output: 5
 * Explanation: The subarray [2,1,1,0,1] has total ink usage 5, so it is too large.
 * But [0,2,1,1,0] has total 4 and length 5, which is the longest valid writing streak.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than
     * or equal to the given ink budget.
     *
     * This method uses the sliding window technique:
     * - Expand the right pointer to include more sections.
     * - Keep track of the current window sum.
     * - If the sum becomes larger than maxInk, move the left pointer forward
     *   until the window becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * Because all values are non-negative, once the sum exceeds the budget,
     * moving the left pointer forward is the correct way to reduce the sum.
     *
     * @param inkUse the array where each element represents the ink needed for one section
     * @param maxInk the maximum total ink allowed for one contiguous writing session
     * @return the length of the longest contiguous subarray with sum less than or equal to maxInk
     *
     * Time complexity: O(n), where n is the length of inkUse, because each element
     * is added to the window once and removed from the window at most once.
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestWhiteboardStreak(int[] inkUse, int maxInk) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Use long for safety, even though int would still fit under given constraints.
        // This is a good habit when dealing with cumulative sums.
        long currentSum = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < inkUse.length; right++) {
            // Step 1:
            // Include the current right element in the window.
            currentSum += inkUse[right];

            // Step 2:
            // If the window sum is too large, shrink the window from the left
            // until the sum becomes valid again.
            //
            // Because all numbers are non-negative:
            // - expanding right can only keep the sum the same or increase it
            // - shrinking left can only keep the sum the same or decrease it
            //
            // This property is exactly why sliding window works here.
            while (currentSum > maxInk && left <= right) {
                currentSum -= inkUse[left];
                left++;
            }

            // Step 3:
            // At this point, the window [left...right] is valid:
            // currentSum <= maxInk
            //
            // So we compute its length and update the answer if this window
            // is longer than any valid window we have seen before.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // Return the longest valid contiguous subarray length found.
        return maxLength;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param arr the array to print
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
     * - the ink budget
     * - the computed result
     * - the expected result
     *
     * This allows quick manual verification that the implementation matches
     * the examples exactly.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) overall for each demonstrated test case.
     * Space complexity: O(1) extra, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] inkUse1 = {2, 1, 3, 2, 1};
        int maxInk1 = 5;
        int result1 = solution.longestWhiteboardStreak(inkUse1, maxInk1);

        System.out.println("Example 1");
        System.out.println("inkUse = " + solution.arrayToString(inkUse1));
        System.out.println("maxInk = " + maxInk1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Example 2
        int[] inkUse2 = {1, 0, 2, 1, 1, 0, 1};
        int maxInk2 = 4;
        int result2 = solution.longestWhiteboardStreak(inkUse2, maxInk2);

        System.out.println("Example 2");
        System.out.println("inkUse = " + solution.arrayToString(inkUse2));
        System.out.println("maxInk = " + maxInk2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 5");
        System.out.println();

        // Additional quick sanity checks for beginners.

        int[] inkUse3 = {0, 0, 0, 0};
        int maxInk3 = 0;
        int result3 = solution.longestWhiteboardStreak(inkUse3, maxInk3);

        System.out.println("Additional Test 1");
        System.out.println("inkUse = " + solution.arrayToString(inkUse3));
        System.out.println("maxInk = " + maxInk3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 4");
        System.out.println();

        int[] inkUse4 = {5, 1, 2};
        int maxInk4 = 0;
        int result4 = solution.longestWhiteboardStreak(inkUse4, maxInk4);

        System.out.println("Additional Test 2");
        System.out.println("inkUse = " + solution.arrayToString(inkUse4));
        System.out.println("maxInk = " + maxInk4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 0");
    }
}