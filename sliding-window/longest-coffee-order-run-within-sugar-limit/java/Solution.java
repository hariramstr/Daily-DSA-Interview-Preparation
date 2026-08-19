import java.util.*;

/*
 * Title: Longest Coffee Order Run Within Sugar Limit
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * A coffee shop records the sugar added to each drink in the order it was prepared.
 * You are given an integer array sugars where sugars[i] is the number of sugar packets
 * used in the i-th order, and an integer limit representing the maximum total sugar allowed.
 *
 * Find the length of the longest contiguous sequence of orders whose total sugar does not
 * exceed limit.
 *
 * In other words, you must choose a subarray sugars[left...right] such that the sum of its
 * values is less than or equal to limit, and the number of elements in that subarray is as
 * large as possible.
 *
 * This problem models a common interview pattern: maintaining a valid moving window while
 * scanning the array from left to right. Since all sugar counts are non-negative, once a
 * window exceeds the limit, you can safely move the left pointer forward until the window
 * becomes valid again.
 *
 * Return the maximum number of consecutive orders that can fit within the sugar limit.
 *
 * Constraints:
 * - 1 <= sugars.length <= 100000
 * - 0 <= sugars[i] <= 10000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: sugars = [1, 2, 1, 1, 3], limit = 4
 * Output: 3
 * Explanation: The longest valid contiguous run is [1, 2, 1] or [2, 1, 1], both with total sugar 4 and length 3.
 *
 * Example 2:
 * Input: sugars = [4, 1, 1, 1, 2], limit = 3
 * Output: 3
 * Explanation: The first order alone exceeds the limit, so it cannot be part of any valid window.
 * The longest valid run is [1, 1, 1], which has total sugar 3.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to the given limit.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer one step at a time.
     * - Add the new value into the running sum.
     * - If the sum becomes too large, shrink the window from the left until the sum is valid again.
     * - Track the largest valid window length seen during the scan.
     *
     * Why this works:
     * Because all values in the array are non-negative, adding more elements can only keep the sum the same
     * or increase it. Therefore, when the sum exceeds the limit, the only way to make it valid again is to
     * move the left boundary forward.
     *
     * @param sugars the array where sugars[i] represents the sugar packets used in the i-th coffee order
     * @param limit the maximum allowed total sugar for any chosen contiguous run of orders
     * @return the length of the longest contiguous sequence whose total sugar is at most limit
     *
     * Time complexity: O(n), because each element is added to the window once and removed from the window at most once.
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int longestCoffeeOrderRunWithinLimit(int[] sugars, int limit) {
        // The left boundary of our current sliding window.
        int left = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We use long for safety, even though int would still fit under the given constraints.
        // Using long is a good habit when maintaining running sums.
        long currentSum = 0;

        // Move the right boundary from left to right across the entire array.
        for (int right = 0; right < sugars.length; right++) {
            // Step 1: Expand the window by including sugars[right].
            currentSum += sugars[right];

            // Step 2: If the window is invalid (sum > limit), shrink it from the left.
            // Because all numbers are non-negative, removing elements from the left is the correct way
            // to reduce the sum until the window becomes valid again.
            while (currentSum > limit && left <= right) {
                currentSum -= sugars[left];
                left++;
            }

            // Step 3: At this point, the window [left...right] is guaranteed to be valid.
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the full array, maxLength holds the answer.
        return maxLength;
    }

    /**
     * A helper method that prints an input array in a readable format.
     *
     * This is used only for demonstration in main.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to the produced string representation.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input array
     * - the sugar limit
     * - the computed result
     * - the expected result for easy verification
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) overall for the demonstrated test cases.
     * Space complexity: O(1) extra, excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        int[] sugars1 = {1, 2, 1, 1, 3};
        int limit1 = 4;
        int result1 = solution.longestCoffeeOrderRunWithinLimit(sugars1, limit1);

        System.out.println("Example 1");
        System.out.println("sugars = " + solution.arrayToString(sugars1));
        System.out.println("limit = " + limit1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Sample Example 2
        int[] sugars2 = {4, 1, 1, 1, 2};
        int limit2 = 3;
        int result2 = solution.longestCoffeeOrderRunWithinLimit(sugars2, limit2);

        System.out.println("Example 2");
        System.out.println("sugars = " + solution.arrayToString(sugars2));
        System.out.println("limit = " + limit2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional beginner-friendly checks
        int[] sugars3 = {0, 0, 0, 0};
        int limit3 = 0;
        int result3 = solution.longestCoffeeOrderRunWithinLimit(sugars3, limit3);

        System.out.println("Additional Example 3");
        System.out.println("sugars = " + solution.arrayToString(sugars3));
        System.out.println("limit = " + limit3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 4");
        System.out.println();

        int[] sugars4 = {5, 6, 7};
        int limit4 = 4;
        int result4 = solution.longestCoffeeOrderRunWithinLimit(sugars4, limit4);

        System.out.println("Additional Example 4");
        System.out.println("sugars = " + solution.arrayToString(sugars4));
        System.out.println("limit = " + limit4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 0");
    }
}