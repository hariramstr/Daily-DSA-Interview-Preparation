import java.util.*;

/*
 * Title: Longest Store Queue Under Customer Limit
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A supermarket records the number of customers joining a checkout line each minute.
 * You are given an integer array customers where customers[i] is the number of new
 * customers who joined during the i-th minute, and an integer limit representing the
 * maximum total number of customers the manager is willing to handle in one continuous
 * observation period.
 *
 * Your task is to find the length of the longest contiguous block of minutes such that
 * the sum of customers in that block is less than or equal to limit.
 *
 * In other words, among all subarrays of customers whose total sum does not exceed limit,
 * return the maximum possible subarray length.
 *
 * This problem models a common real-world monitoring task where a team wants to identify
 * the longest time span during which demand stayed within a manageable threshold. Since
 * all customer counts are non-negative, an efficient sliding window solution can expand
 * and shrink a window while maintaining the current sum.
 *
 * Constraints:
 * - 1 <= customers.length <= 100000
 * - 0 <= customers[i] <= 10000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: customers = [2, 1, 3, 2, 1], limit = 5
 * Output: 2
 * Explanation: Valid windows include [2,1], [3,2], and [2,1]. Any window of length 3
 * has sum greater than 5, so the answer is 2.
 *
 * Example 2:
 * Input: customers = [1, 0, 1, 1, 0, 1], limit = 3
 * Output: 5
 * Explanation: The window [1,0,1,1,0] has sum 3 and length 5, which is the longest
 * valid contiguous block.
 *
 * Return only the maximum length. If no minute can be included because every single
 * value is greater than limit, return 0.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to
     * the given limit using the sliding window technique.
     *
     * Because all values in the array are non-negative, once the current window sum becomes
     * too large, moving the left boundary to the right can only decrease or keep the sum.
     * This property makes sliding window the ideal linear-time approach.
     *
     * @param customers an array where customers[i] is the number of customers joining during minute i
     * @param limit the maximum allowed total number of customers in a valid contiguous block
     * @return the length of the longest contiguous subarray whose sum is less than or equal to limit
     * Time complexity: O(n), where n is the length of the customers array, because each index
     * is visited at most twice (once by the right pointer and once by the left pointer)
     * Space complexity: O(1), because only a few extra variables are used
     */
    public int longestQueueUnderLimit(int[] customers, int limit) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the running sum of the current window customers[left...right].
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Expand the window one element at a time by moving the right boundary.
        for (int right = 0; right < customers.length; right++) {
            // Include the new rightmost element in the current window sum.
            currentSum += customers[right];

            // If the window sum exceeds the allowed limit, the window is invalid.
            // Since all numbers are non-negative, the only way to make it valid again
            // is to remove elements from the left side of the window.
            while (currentSum > limit && left <= right) {
                // Remove the leftmost element from the sum.
                currentSum -= customers[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // At this point, the window customers[left...right] is guaranteed to be valid
            // (sum <= limit), so we can compute its length.
            int currentLength = right - left + 1;

            // Update the answer if this valid window is the longest one seen so far.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If no valid single element exists, maxLength remains 0, which is correct.
        return maxLength;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input array
     * - the limit
     * - the computed result
     * - the expected result
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for the demonstrated test cases
     * Space complexity: O(1) auxiliary space excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample test case 1
        int[] customers1 = {2, 1, 3, 2, 1};
        int limit1 = 5;
        int result1 = solution.longestQueueUnderLimit(customers1, limit1);

        System.out.println("Example 1");
        System.out.println("customers = " + solution.arrayToString(customers1));
        System.out.println("limit = " + limit1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Sample test case 2
        int[] customers2 = {1, 0, 1, 1, 0, 1};
        int limit2 = 3;
        int result2 = solution.longestQueueUnderLimit(customers2, limit2);

        System.out.println("Example 2");
        System.out.println("customers = " + solution.arrayToString(customers2));
        System.out.println("limit = " + limit2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 5");
        System.out.println();

        // Additional edge case: every single value is greater than the limit
        int[] customers3 = {6, 7, 8};
        int limit3 = 5;
        int result3 = solution.longestQueueUnderLimit(customers3, limit3);

        System.out.println("Edge Case");
        System.out.println("customers = " + solution.arrayToString(customers3));
        System.out.println("limit = " + limit3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
    }
}