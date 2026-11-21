import java.util.*;

/*
Problem Title: Longest Reading Lamp Stretch Within Battery Budget

Problem Description:
A student uses a rechargeable reading lamp while studying late at night. You are given an integer array
batteryUse where batteryUse[i] is the number of battery units consumed during minute i, and an integer
budget representing the maximum total battery units the lamp can spend before it must be recharged.

Your task is to find the length of the longest contiguous block of minutes during which the total battery
consumption is less than or equal to budget.

In other words, return the maximum size of a subarray whose sum does not exceed budget.

This problem is meant to be solved efficiently using a sliding window. Since all battery usage values are
non-negative, once a window exceeds the budget, moving the left side forward is enough to restore validity.

Constraints:
- 1 <= batteryUse.length <= 100000
- 0 <= batteryUse[i] <= 10000
- 0 <= budget <= 1000000000

Example 1:
Input: batteryUse = [2, 1, 3, 2, 1], budget = 5
Output: 2
Explanation: Valid windows include [2,1], [3,2], and [2,1]. Any window of length 3 has sum greater than 5,
so the answer is 2.

Example 2:
Input: batteryUse = [0, 1, 1, 0, 2, 1], budget = 3
Output: 4
Explanation: The subarray [0,1,1,0] has total consumption 2, which is within the budget. No contiguous
subarray of length 5 or more stays within 3, so the answer is 4.
*/

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to the given budget.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep track of the current window sum.
     * - If the sum becomes larger than the budget, shrink the window from the left
     *   until the sum becomes valid again.
     * - Track the largest valid window length seen so far.
     *
     * This works because all values in the array are non-negative. That property guarantees that:
     * - Expanding the window can only increase or keep the same sum.
     * - Shrinking the window can only decrease or keep the same sum.
     *
     * @param batteryUse the array where batteryUse[i] is the battery consumed during minute i
     * @param budget the maximum allowed total battery usage for any valid contiguous block
     * @return the length of the longest contiguous subarray with sum less than or equal to budget
     *
     * Time complexity: O(n), because each element is added to the window once and removed at most once.
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestReadingLampStretch(int[] batteryUse, int budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Current sum of the elements inside the window [left, right].
        // We use long for extra safety, even though int would also be enough under given constraints.
        long currentSum = 0;

        // Move the right boundary one step at a time across the array.
        for (int right = 0; right < batteryUse.length; right++) {
            // Step 1:
            // Include batteryUse[right] in the current window.
            currentSum += batteryUse[right];

            // Step 2:
            // If the window sum is too large, it is invalid.
            // Since all numbers are non-negative, the only way to make it valid again
            // is to move the left boundary to the right and remove elements from the sum.
            while (currentSum > budget && left <= right) {
                currentSum -= batteryUse[left];
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid
            // because currentSum <= budget.
            // So we compute its length and update the answer if this window is larger.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // Return the largest valid window length found.
        return maxLength;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
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
     * - the budget
     * - the computed result
     * - the expected result
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call, depending on the input size.
     * Space complexity: O(1) extra space excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] batteryUse1 = {2, 1, 3, 2, 1};
        int budget1 = 5;
        int result1 = solution.longestReadingLampStretch(batteryUse1, budget1);

        System.out.println("Sample 1");
        System.out.println("batteryUse = " + solution.arrayToString(batteryUse1));
        System.out.println("budget = " + budget1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Sample 2
        int[] batteryUse2 = {0, 1, 1, 0, 2, 1};
        int budget2 = 3;
        int result2 = solution.longestReadingLampStretch(batteryUse2, budget2);

        System.out.println("Sample 2");
        System.out.println("batteryUse = " + solution.arrayToString(batteryUse2));
        System.out.println("budget = " + budget2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional quick checks for clarity
        int[] batteryUse3 = {5};
        int budget3 = 5;
        System.out.println("Additional Check 1");
        System.out.println("batteryUse = " + solution.arrayToString(batteryUse3));
        System.out.println("budget = " + budget3);
        System.out.println("Output = " + solution.longestReadingLampStretch(batteryUse3, budget3));
        System.out.println("Expected = 1");
        System.out.println();

        int[] batteryUse4 = {6};
        int budget4 = 5;
        System.out.println("Additional Check 2");
        System.out.println("batteryUse = " + solution.arrayToString(batteryUse4));
        System.out.println("budget = " + budget4);
        System.out.println("Output = " + solution.longestReadingLampStretch(batteryUse4, budget4));
        System.out.println("Expected = 0");
        System.out.println();

        int[] batteryUse5 = {0, 0, 0, 0};
        int budget5 = 0;
        System.out.println("Additional Check 3");
        System.out.println("batteryUse = " + solution.arrayToString(batteryUse5));
        System.out.println("budget = " + budget5);
        System.out.println("Output = " + solution.longestReadingLampStretch(batteryUse5, budget5));
        System.out.println("Expected = 4");
    }
}