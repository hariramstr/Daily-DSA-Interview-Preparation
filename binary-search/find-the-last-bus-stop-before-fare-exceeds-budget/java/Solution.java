import java.util.*;

/*
Problem Title: Find the Last Bus Stop Before Fare Exceeds Budget

Problem Description:
You are given a non-decreasing array fares where fares[i] is the total bus fare needed
to travel from the starting terminal to stop i. Because the route gets longer, the total
fare never decreases as the stop index increases. You are also given an integer budget
representing the maximum amount a passenger can spend.

Return the largest index i such that fares[i] <= budget. If even the first stop is too
expensive, return -1.

This problem models a common interview pattern: finding the rightmost value in a sorted
array that satisfies a condition. Be careful with duplicate fares. If several stops have
the same fare and that fare is within the budget, you should return the last such stop.

Constraints:
- 1 <= fares.length <= 100000
- 0 <= fares[i] <= 1000000000
- fares is sorted in non-decreasing order
- 0 <= budget <= 1000000000

Example 1:
Input: fares = [2, 4, 4, 7, 9], budget = 4
Output: 2
Explanation: Stops 1 and 2 both cost 4, which is within budget. The last reachable stop is index 2.

Example 2:
Input: fares = [5, 8, 12], budget = 3
Output: -1
Explanation: The passenger cannot afford even the first stop, so the answer is -1.
*/

public class Solution {

    /**
     * Finds the largest index i such that fares[i] <= budget using binary search.
     *
     * This method solves the "rightmost valid position" version of binary search:
     * we want the last index whose fare is still affordable.
     *
     * @param fares a non-decreasing array where fares[i] is the total fare to reach stop i
     * @param budget the maximum fare the passenger can afford
     * @return the largest index i such that fares[i] <= budget; returns -1 if no stop is affordable
     *
     * Time complexity: O(log n), where n is fares.length
     * Space complexity: O(1)
     */
    public int findLastAffordableStop(int[] fares, int budget) {
        // 'left' marks the beginning of the current search range.
        int left = 0;

        // 'right' marks the end of the current search range.
        int right = fares.length - 1;

        // 'answer' stores the best valid index found so far.
        // We start with -1 because if no fare is within budget,
        // the required result is -1.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        while (left <= right) {
            // Compute the middle index carefully.
            // We use this form instead of (left + right) / 2
            // to avoid potential integer overflow in general.
            int mid = left + (right - left) / 2;

            // If the fare at 'mid' is affordable, then 'mid' is a valid candidate.
            if (fares[mid] <= budget) {
                // Record this index as the current best answer.
                answer = mid;

                // Since we want the LAST index that is still affordable,
                // we do NOT stop here.
                //
                // Instead, we move to the RIGHT half to see whether there is
                // another affordable stop at a larger index.
                left = mid + 1;
            } else {
                // If fares[mid] is too expensive, then 'mid' cannot be the answer,
                // and neither can any index to the right of 'mid',
                // because the array is sorted in non-decreasing order.
                //
                // Therefore, we must search only in the LEFT half.
                right = mid - 1;
            }
        }

        // After the loop ends, 'answer' contains the rightmost affordable index,
        // or -1 if none was found.
        return answer;
    }

    /**
     * A simple helper method to print an array in a beginner-friendly format.
     *
     * @param fares the array of fares to display
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is fares.length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] fares) {
        return Arrays.toString(fares);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(log n) per test case for the search itself
     * Space complexity: O(1) extra space for the search itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        int[] fares1 = {2, 4, 4, 7, 9};
        int budget1 = 4;
        int result1 = solution.findLastAffordableStop(fares1, budget1);
        System.out.println("Example 1:");
        System.out.println("fares = " + solution.arrayToString(fares1) + ", budget = " + budget1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        // Sample Example 2
        int[] fares2 = {5, 8, 12};
        int budget2 = 3;
        int result2 = solution.findLastAffordableStop(fares2, budget2);
        System.out.println("Example 2:");
        System.out.println("fares = " + solution.arrayToString(fares2) + ", budget = " + budget2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: -1");
        System.out.println();

        // Additional test: budget reaches the final stop
        int[] fares3 = {1, 3, 5, 7};
        int budget3 = 10;
        int result3 = solution.findLastAffordableStop(fares3, budget3);
        System.out.println("Additional Test 1:");
        System.out.println("fares = " + solution.arrayToString(fares3) + ", budget = " + budget3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional test: duplicates and exact match
        int[] fares4 = {2, 2, 2, 2};
        int budget4 = 2;
        int result4 = solution.findLastAffordableStop(fares4, budget4);
        System.out.println("Additional Test 2:");
        System.out.println("fares = " + solution.arrayToString(fares4) + ", budget = " + budget4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional test: only first stop is affordable
        int[] fares5 = {4, 6, 8, 10};
        int budget5 = 4;
        int result5 = solution.findLastAffordableStop(fares5, budget5);
        System.out.println("Additional Test 3:");
        System.out.println("fares = " + solution.arrayToString(fares5) + ", budget = " + budget5);
        System.out.println("Output: " + result5);
        System.out.println("Expected: 0");
    }
}