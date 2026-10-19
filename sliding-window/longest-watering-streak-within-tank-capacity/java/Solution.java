import java.util.*;

/*
 * Title: Longest Watering Streak Within Tank Capacity
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A gardener records how many liters of water are used on each consecutive day in an array
 * usage, where usage[i] is the water needed on day i. The watering system has a tank that
 * can supply at most capacity liters before it must be refilled. We want to find the longest
 * consecutive stretch of days that can be fully covered using a single tank fill.
 *
 * Return the length of the longest contiguous subarray whose total water usage is less than
 * or equal to capacity.
 *
 * This is a classic contiguous-range optimization problem: we are not allowed to skip days,
 * reorder them, or split the interval. If no day can be covered because every single day's
 * usage is greater than capacity, return 0.
 *
 * Constraints:
 * - 1 <= usage.length <= 100000
 * - 0 <= usage[i] <= 10000
 * - 0 <= capacity <= 10^9
 *
 * Example 1:
 * Input: usage = [2, 1, 3, 2, 1], capacity = 5
 * Output: 2
 *
 * Example 2:
 * Input: usage = [1, 1, 1, 2, 1], capacity = 4
 * Correct Output: 3
 *
 * Note:
 * The second example's narrative in the prompt contains a correction inside the explanation.
 * The correct longest valid contiguous subarray length is 3.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to
     * the given tank capacity.
     *
     * This method uses the sliding window technique, which works efficiently here because all
     * values in the usage array are non-negative. That non-negative property is very important:
     * when the current window sum becomes too large, moving the left boundary to the right can
     * only decrease (or keep) the sum, so we can safely shrink the window until it becomes valid.
     *
     * @param usage the array where usage[i] represents liters of water needed on day i
     * @param capacity the maximum liters the tank can provide in one fill
     * @return the maximum length of a contiguous subarray with total sum <= capacity
     *
     * Time complexity: O(n), because each element is added to the window once and removed at most once.
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestWateringStreak(int[] usage, int capacity) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This variable stores the sum of the current window usage[left...right].
        long currentSum = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Expand the window one day at a time by moving the right boundary.
        for (int right = 0; right < usage.length; right++) {
            // Include the current day's water usage into the running window sum.
            currentSum += usage[right];

            // If the window sum is too large, we must shrink the window from the left
            // until the sum becomes valid again (<= capacity).
            //
            // Because all usage values are non-negative:
            // - expanding the window can only increase or keep the sum
            // - shrinking the window can only decrease or keep the sum
            //
            // That is exactly why the sliding window technique is correct here.
            while (currentSum > capacity && left <= right) {
                // Remove the leftmost day's usage from the current sum.
                currentSum -= usage[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // At this point, the window usage[left...right] is valid:
            // its sum is <= capacity.
            //
            // So we compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If every single day had usage > capacity, then no valid window of positive length
        // would ever be formed, and maxLength would remain 0, which is exactly what we want.
        return maxLength;
    }

    /**
     * Helper method to print an example in a beginner-friendly format.
     *
     * @param usage the input water usage array
     * @param capacity the tank capacity
     * @return the computed longest valid streak length
     *
     * Time complexity: O(n), because it calls the main sliding window method once.
     * Space complexity: O(1), ignoring the space used by output formatting.
     */
    public int demonstrateExample(int[] usage, int capacity) {
        int result = longestWateringStreak(usage, capacity);
        System.out.println("usage = " + Arrays.toString(usage));
        System.out.println("capacity = " + capacity);
        System.out.println("Longest watering streak = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total number of elements across demonstrated test cases).
     * Space complexity: O(1), excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt:
        // usage = [2, 1, 3, 2, 1], capacity = 5
        // Valid longest windows have length 2, so expected output is 2.
        int[] usage1 = {2, 1, 3, 2, 1};
        int capacity1 = 5;
        int result1 = solution.demonstrateExample(usage1, capacity1);
        System.out.println("Expected: 2, Actual: " + result1);
        System.out.println("Match: " + (result1 == 2));
        System.out.println();

        // Example 2 from the prompt:
        // usage = [1, 1, 1, 2, 1], capacity = 4
        // Correct longest valid contiguous subarray length is 3.
        int[] usage2 = {1, 1, 1, 2, 1};
        int capacity2 = 4;
        int result2 = solution.demonstrateExample(usage2, capacity2);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println("Match: " + (result2 == 3));
        System.out.println();

        // Additional edge case:
        // Every day individually exceeds capacity, so answer should be 0.
        int[] usage3 = {6, 7, 8};
        int capacity3 = 5;
        int result3 = solution.demonstrateExample(usage3, capacity3);
        System.out.println("Expected: 0, Actual: " + result3);
        System.out.println("Match: " + (result3 == 0));
        System.out.println();

        // Additional edge case:
        // Capacity is 0 and all values are 0, so the whole array is valid.
        int[] usage4 = {0, 0, 0, 0};
        int capacity4 = 0;
        int result4 = solution.demonstrateExample(usage4, capacity4);
        System.out.println("Expected: 4, Actual: " + result4);
        System.out.println("Match: " + (result4 == 4));
        System.out.println();

        // Additional edge case:
        // Mixed values with zeros.
        int[] usage5 = {0, 2, 0, 1, 0};
        int capacity5 = 3;
        int result5 = solution.demonstrateExample(usage5, capacity5);
        System.out.println("Expected: 5, Actual: " + result5);
        System.out.println("Match: " + (result5 == 5));
    }
}