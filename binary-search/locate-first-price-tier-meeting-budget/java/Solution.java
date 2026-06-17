import java.util.*;

/*
Problem Title: Locate First Price Tier Meeting Budget

Problem Description:
You are given a sorted array `tiers` where `tiers[i]` represents the minimum order amount required
to unlock the `i`-th discount tier in an online store. The array is sorted in non-decreasing order,
and duplicate values may appear because multiple tier labels can start at the same minimum amount.
You are also given an integer `budget`.

Your task is to return the smallest index `i` such that `tiers[i] >= budget`. In other words,
find the first discount tier whose required minimum order amount is at least the shopper's budget target.
If no such index exists, return `-1`.

This problem should be solved efficiently. A linear scan works, but the intended solution uses binary
search because the array is already sorted. Be careful with edge cases such as an empty array, all values
being smaller than `budget`, or the answer appearing multiple times due to duplicates.

Constraints:
- 0 <= tiers.length <= 100000
- 0 <= tiers[i] <= 1000000000
- tiers is sorted in non-decreasing order
- 0 <= budget <= 1000000000

Example 1:
Input: tiers = [20, 35, 50, 50, 80], budget = 50
Output: 2
Explanation: The first index with value at least 50 is index 2.

Example 2:
Input: tiers = [10, 15, 15, 30], budget = 16
Output: 3
Explanation: Values at indices 0, 1, and 2 are smaller than 16. The first valid tier is 30 at index 3.
*/

public class Solution {

    /**
     * Finds the smallest index i such that tiers[i] >= budget using binary search.
     *
     * @param tiers the sorted array of minimum order amounts for discount tiers
     * @param budget the target budget that we want a tier to meet or exceed
     * @return the first index whose value is at least budget, or -1 if no such index exists
     *
     * Time complexity: O(log n), where n is the length of the array
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int findFirstTierMeetingBudget(int[] tiers, int budget) {
        // If the array is null or empty, there is no valid index to return.
        if (tiers == null || tiers.length == 0) {
            return -1;
        }

        // We will search within the inclusive range [left, right].
        int left = 0;
        int right = tiers.length - 1;

        // This variable will store the best answer found so far.
        // We initialize it to -1, meaning "not found yet".
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        while (left <= right) {
            // Compute the middle index carefully.
            // Using left + (right - left) / 2 avoids potential overflow.
            int mid = left + (right - left) / 2;

            // Case 1:
            // If tiers[mid] is large enough, then mid is a valid candidate.
            // But it may not be the FIRST such index, because there could be another
            // valid index further to the left.
            if (tiers[mid] >= budget) {
                // Record mid as a possible answer.
                answer = mid;

                // Move left to search the earlier part of the array
                // for an even smaller valid index.
                right = mid - 1;
            } else {
                // Case 2:
                // If tiers[mid] < budget, then mid cannot be the answer,
                // and neither can any index to the left of mid, because the array is sorted.
                // So we must search only the right half.
                left = mid + 1;
            }
        }

        // If we found at least one valid index, answer holds the first one.
        // Otherwise, it remains -1.
        return answer;
    }

    /**
     * A simple linear scan version for learning and verification purposes.
     * This is not the intended optimal solution, but it is easy to understand.
     *
     * @param tiers the sorted array of minimum order amounts for discount tiers
     * @param budget the target budget that we want a tier to meet or exceed
     * @return the first index whose value is at least budget, or -1 if no such index exists
     *
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int findFirstTierMeetingBudgetLinear(int[] tiers, int budget) {
        if (tiers == null || tiers.length == 0) {
            return -1;
        }

        for (int i = 0; i < tiers.length; i++) {
            if (tiers[i] >= budget) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Converts an integer array to a readable string representation.
     *
     * @param arr the array to convert into a string
     * @return a string showing the array contents, such as [1, 2, 3]
     *
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n), due to the created string content
     */
    public static String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Runs demonstration test cases and prints the results.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     *
     * Time complexity: O(k log n) for k test cases using the binary search method
     * Space complexity: O(1) extra space excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement
        int[] tiers1 = {20, 35, 50, 50, 80};
        int budget1 = 50;
        int result1 = solution.findFirstTierMeetingBudget(tiers1, budget1);
        System.out.println("Example 1:");
        System.out.println("tiers = " + arrayToString(tiers1) + ", budget = " + budget1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        // Example 2 from the problem statement
        int[] tiers2 = {10, 15, 15, 30};
        int budget2 = 16;
        int result2 = solution.findFirstTierMeetingBudget(tiers2, budget2);
        System.out.println("Example 2:");
        System.out.println("tiers = " + arrayToString(tiers2) + ", budget = " + budget2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional edge case: empty array
        int[] tiers3 = {};
        int budget3 = 25;
        int result3 = solution.findFirstTierMeetingBudget(tiers3, budget3);
        System.out.println("Edge Case 1 (empty array):");
        System.out.println("tiers = " + arrayToString(tiers3) + ", budget = " + budget3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: -1");
        System.out.println();

        // Additional edge case: all values smaller than budget
        int[] tiers4 = {5, 10, 15};
        int budget4 = 20;
        int result4 = solution.findFirstTierMeetingBudget(tiers4, budget4);
        System.out.println("Edge Case 2 (all values smaller):");
        System.out.println("tiers = " + arrayToString(tiers4) + ", budget = " + budget4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: -1");
        System.out.println();

        // Additional edge case: answer is at index 0
        int[] tiers5 = {7, 12, 18};
        int budget5 = 5;
        int result5 = solution.findFirstTierMeetingBudget(tiers5, budget5);
        System.out.println("Edge Case 3 (answer at index 0):");
        System.out.println("tiers = " + arrayToString(tiers5) + ", budget = " + budget5);
        System.out.println("Output: " + result5);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional edge case: duplicates where the first duplicate must be returned
        int[] tiers6 = {10, 20, 20, 20, 40};
        int budget6 = 20;
        int result6 = solution.findFirstTierMeetingBudget(tiers6, budget6);
        System.out.println("Edge Case 4 (duplicates):");
        System.out.println("tiers = " + arrayToString(tiers6) + ", budget = " + budget6);
        System.out.println("Output: " + result6);
        System.out.println("Expected: 1");
        System.out.println();

        // Optional verification: compare binary search result with linear scan
        System.out.println("Verification with linear scan:");
        System.out.println("Example 1 binary = " + solution.findFirstTierMeetingBudget(tiers1, budget1)
                + ", linear = " + solution.findFirstTierMeetingBudgetLinear(tiers1, budget1));
        System.out.println("Example 2 binary = " + solution.findFirstTierMeetingBudget(tiers2, budget2)
                + ", linear = " + solution.findFirstTierMeetingBudgetLinear(tiers2, budget2));
    }
}