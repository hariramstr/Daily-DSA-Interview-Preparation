/*
Title: Longest Snack Stall Run Within Spending Limit
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are visiting a street market with a row of snack stalls. The i-th stall has a snack that costs costs[i] dollars.
You want to buy snacks from a single contiguous block of stalls, because walking back and forth is inconvenient.
However, you have a fixed budget and cannot spend more than budget dollars in total.

Return the length of the longest contiguous segment of stalls such that the sum of costs in that segment
is less than or equal to budget.

If no single stall can be afforded, return 0.

This problem is a natural fit for a sliding window approach: as you expand the right end of the window,
keep track of the running total. If the total becomes greater than budget, move the left end forward
until the window becomes valid again. Track the maximum valid window length seen during the scan.

Constraints:
- 1 <= costs.length <= 100000
- 1 <= costs[i] <= 10000
- 1 <= budget <= 1000000000
- costs contains only positive integers

Example 1:
Input: costs = [2, 1, 3, 2, 1], budget = 5
Output: 2
Explanation: Valid segments with total cost at most 5 include [2,1], [3,2], and [2,1].
The longest valid length is 2. The segment [1,3,2] costs 6, so it is not allowed.

Example 2:
Input: costs = [4, 2, 1, 1, 3], budget = 6
Output: 3
Explanation: One longest valid segment is [2,1,1], which costs 4.
The segment [4,2,1] costs 7, so it exceeds the budget. No valid segment of length 4 exists.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to the given budget.
     *
     * This method uses the sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Add the new element to the running sum.
     * - If the sum becomes too large, shrink the window from the left until it becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * @param costs the array of positive snack costs, where costs[i] is the cost at stall i
     * @param budget the maximum total amount that can be spent
     * @return the maximum length of a contiguous segment whose total cost is at most budget
     *
     * Time complexity: O(n), because each element enters and leaves the window at most once.
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int longestSnackStallRun(int[] costs, int budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Running sum of the current window [left..right].
        // We use long for extra safety, even though int would also fit within the given constraints.
        long currentSum = 0;

        // Move the right boundary from the start of the array to the end.
        for (int right = 0; right < costs.length; right++) {
            // Step 1: Expand the window by including costs[right].
            currentSum += costs[right];

            // Step 2: If the window is invalid (sum > budget),
            // keep shrinking it from the left until it becomes valid again.
            //
            // This works because all costs are positive:
            // removing elements from the left always decreases the sum.
            while (currentSum > budget && left <= right) {
                currentSum -= costs[left];
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid
            // (or empty in extreme cases, though with positive numbers it will simply become valid).
            //
            // So we compute its length and update the answer if this window is longer.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // If no single stall was affordable, maxLength remains 0.
        return maxLength;
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call to the algorithm.
     * Space complexity: O(1) extra for the algorithm itself.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        int[] costs1 = {2, 1, 3, 2, 1};
        int budget1 = 5;
        int result1 = solution.longestSnackStallRun(costs1, budget1);
        System.out.println("Example 1:");
        System.out.println("costs = " + solution.arrayToString(costs1) + ", budget = " + budget1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        // Sample Example 2
        int[] costs2 = {4, 2, 1, 1, 3};
        int budget2 = 6;
        int result2 = solution.longestSnackStallRun(costs2, budget2);
        System.out.println("Example 2:");
        System.out.println("costs = " + solution.arrayToString(costs2) + ", budget = " + budget2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional Example: no single stall can be afforded
        int[] costs3 = {7, 8, 9};
        int budget3 = 5;
        int result3 = solution.longestSnackStallRun(costs3, budget3);
        System.out.println("Additional Example 1:");
        System.out.println("costs = " + solution.arrayToString(costs3) + ", budget = " + budget3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional Example: entire array is affordable
        int[] costs4 = {1, 1, 1, 1};
        int budget4 = 10;
        int result4 = solution.longestSnackStallRun(costs4, budget4);
        System.out.println("Additional Example 2:");
        System.out.println("costs = " + solution.arrayToString(costs4) + ", budget = " + budget4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 4");
    }
}