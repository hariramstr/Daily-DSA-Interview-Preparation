import java.util.*;

/*
Problem Title: Find the Last Discount Tier Not Exceeding Budget

Problem Description:
An online store offers bulk discounts based on purchase quantity. You are given a sorted integer array
`tiers`, where `tiers[i]` represents a quantity threshold at which a new discount tier begins.
The array is sorted in strictly increasing order. You are also given an integer `budgetQty`,
representing the maximum number of items a customer is willing or able to buy.

Your task is to return the index of the last discount tier whose threshold is less than or equal to
`budgetQty`. In other words, find the greatest index `i` such that `tiers[i] <= budgetQty`.

If every threshold in `tiers` is greater than `budgetQty`, return `-1` because the customer does not
qualify for any tier. If `budgetQty` is greater than or equal to the largest threshold, return the
last index.

This problem should be solved efficiently using binary search rather than scanning the entire array.
A linear scan works for small inputs, but interviewers expect an `O(log n)` solution because the
thresholds are already sorted.

Constraints:
- 1 <= tiers.length <= 100000
- 1 <= tiers[i] <= 1000000000
- tiers is strictly increasing
- 1 <= budgetQty <= 1000000000

Example 1:
Input: tiers = [5, 10, 20, 50], budgetQty = 23
Output: 2
Explanation: The thresholds less than or equal to 23 are 5, 10, and 20. The last such threshold is 20 at index 2.

Example 2:
Input: tiers = [4, 8, 15], budgetQty = 2
Output: -1
Explanation: All thresholds are greater than 2, so no discount tier is available.
*/

public class Solution {

    /**
     * Finds the index of the last discount tier whose threshold is less than or equal to the given budget quantity.
     *
     * This method uses binary search because the input array is already sorted in strictly increasing order.
     * The goal is to efficiently locate the rightmost element that satisfies:
     * tiers[i] <= budgetQty
     *
     * @param tiers the sorted array of discount tier thresholds in strictly increasing order
     * @param budgetQty the maximum quantity the customer can buy
     * @return the greatest index i such that tiers[i] <= budgetQty; returns -1 if no such index exists
     *
     * Time Complexity: O(log n), where n is the length of the tiers array
     * Space Complexity: O(1), because only a constant amount of extra space is used
     */
    public int findLastDiscountTier(int[] tiers, int budgetQty) {
        // 'left' marks the beginning of the current search range.
        int left = 0;

        // 'right' marks the end of the current search range.
        int right = tiers.length - 1;

        // This variable stores the best valid answer found so far.
        // We start with -1 because if no threshold is <= budgetQty,
        // then the correct answer must be -1.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        while (left <= right) {
            // Compute the middle index safely.
            // We use this form instead of (left + right) / 2 to avoid integer overflow,
            // even though overflow is not likely here under the given constraints.
            int mid = left + (right - left) / 2;

            // If the middle threshold is less than or equal to the budget,
            // then this index is a valid candidate.
            if (tiers[mid] <= budgetQty) {
                // Record this index as the current best answer.
                answer = mid;

                // Since we want the LAST such index, we must continue searching
                // to the RIGHT to see if there is another valid threshold later in the array.
                left = mid + 1;
            } else {
                // If tiers[mid] is greater than the budget,
                // then mid cannot be part of the answer, and neither can anything to its right,
                // because the array is sorted in increasing order.
                // So we move the search to the LEFT half.
                right = mid - 1;
            }
        }

        // After the loop ends, 'answer' contains the rightmost valid index found,
        // or -1 if no threshold was <= budgetQty.
        return answer;
    }

    /**
     * A small helper method to print a test case in a beginner-friendly format.
     *
     * @param tiers the sorted array of discount tier thresholds
     * @param budgetQty the customer's maximum quantity
     * @return the computed index of the last threshold not exceeding the budget quantity
     *
     * Time Complexity: O(log n) for the search itself, where n is the length of tiers
     * Space Complexity: O(1), excluding the space used by output formatting
     */
    public int demonstrateCase(int[] tiers, int budgetQty) {
        int result = findLastDiscountTier(tiers, budgetQty);
        System.out.println("tiers = " + Arrays.toString(tiers) + ", budgetQty = " + budgetQty);
        System.out.println("Result: " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations of the binary search solution.
     *
     * This main method verifies the examples from the problem statement and also shows
     * a few extra cases to help beginners understand edge behavior.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time Complexity: O(log n) per demonstrated test case
     * Space Complexity: O(1), excluding output-related internal costs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // tiers = [5, 10, 20, 50], budgetQty = 23
        // Thresholds <= 23 are 5, 10, 20
        // The last such threshold is 20 at index 2
        solution.demonstrateCase(new int[]{5, 10, 20, 50}, 23);

        // Example 2 from the problem statement:
        // tiers = [4, 8, 15], budgetQty = 2
        // No threshold is <= 2
        // So the correct answer is -1
        solution.demonstrateCase(new int[]{4, 8, 15}, 2);

        // Additional example:
        // budgetQty exactly matches a threshold
        solution.demonstrateCase(new int[]{3, 6, 9, 12}, 9);

        // Additional example:
        // budgetQty is larger than all thresholds, so return the last index
        solution.demonstrateCase(new int[]{2, 5, 7, 11}, 20);

        // Additional example:
        // budgetQty is smaller than the first threshold, so return -1
        solution.demonstrateCase(new int[]{10, 20, 30}, 1);
    }
}