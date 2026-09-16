import java.util.*;

/*
Title: Count Pairs of Photos Within Brightness Budget
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given an integer array brightness where brightness[i] is the brightness score of the i-th photo in a gallery, and an integer budget. Two photos can be edited together if the absolute difference between their brightness scores is less than or equal to budget.

Your task is to return the number of distinct pairs of indices (i, j) such that 0 <= i < j < n and |brightness[i] - brightness[j]| <= budget.

A brute-force solution that checks every pair takes O(n^2) time and is too slow for large galleries. You should design an algorithm efficient enough for up to 200,000 photos. A common approach is to sort the brightness values and use two pointers to count how many valid partners each position can form.

Note that pairs are based on indices, but after sorting, each original photo still contributes exactly once to the count through its value. Duplicate brightness values are allowed, and they may create many valid pairs.

Constraints:
- 1 <= brightness.length <= 200000
- 0 <= brightness[i] <= 10^9
- 0 <= budget <= 10^9

Example 1:
Input: brightness = [4, 1, 7, 5], budget = 3
Output: 4
Explanation: Valid pairs are (4,1), (4,7), (4,5), and (7,5). Their brightness differences are 3, 3, 1, and 2.

Example 2:
Input: brightness = [2, 2, 2, 8, 9], budget = 1
Output: 4
Explanation: Among the three photos with brightness 2, there are 3 valid pairs. Also, the pair (8,9) is valid. No pair involving a 2 and either 8 or 9 satisfies the budget.
*/

public class Solution {

    /**
     * Counts how many distinct index pairs (i, j) satisfy:
     * 0 <= i < j < brightness.length and |brightness[i] - brightness[j]| <= budget.
     *
     * The method uses the standard efficient approach:
     * 1. Copy and sort the array.
     * 2. Use a sliding window / two-pointer technique.
     * 3. For each left boundary, expand the right boundary as far as possible while
     *    the brightness difference stays within the budget.
     * 4. Count how many elements to the right of the current left index are valid partners.
     *
     * Why sorting helps:
     * After sorting, for any i < j, we know sorted[j] >= sorted[i], so:
     * |sorted[j] - sorted[i]| = sorted[j] - sorted[i]
     * This removes the need to consider absolute value in two directions.
     *
     * @param brightness the array of photo brightness values
     * @param budget the maximum allowed absolute difference between two photos
     * @return the number of valid distinct pairs; returned as long because the count
     *         can be as large as n * (n - 1) / 2, which exceeds int for large n
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scan
     * Space complexity: O(n) for the copied sorted array
     */
    public long countPairsWithinBudget(int[] brightness, int budget) {
        // Defensive handling for very small arrays:
        // if there are fewer than 2 photos, no pair can exist.
        if (brightness == null || brightness.length < 2) {
            return 0L;
        }

        // We copy the input array so the original data remains unchanged.
        // This is often a good practice in interview and production settings unless
        // in-place modification is explicitly allowed and desired.
        int[] sorted = Arrays.copyOf(brightness, brightness.length);

        // Sorting is the key step that enables the linear two-pointer scan.
        Arrays.sort(sorted);

        long pairCount = 0L;

        // "right" will always move forward, never backward.
        // This is what makes the scan O(n) after sorting.
        int right = 0;

        // We treat each index "left" as the first element of a pair.
        for (int left = 0; left < sorted.length; left++) {
            // Ensure right is at least left.
            // This keeps the window valid and avoids right lagging behind.
            if (right < left) {
                right = left;
            }

            // Expand "right" as far as possible while the difference remains within budget.
            //
            // Important detail:
            // We use (long) when subtracting to be extra safe and explicit,
            // even though the given constraints fit in int.
            //
            // The loop condition means:
            // while the next element still forms a valid pair with sorted[left],
            // include it in the current window.
            while (right + 1 < sorted.length
                    && (long) sorted[right + 1] - sorted[left] <= budget) {
                right++;
            }

            // At this point, every index from left+1 through right is a valid partner
            // for index left.
            //
            // Number of such partners:
            // right - left
            //
            // Example:
            // if left = 2 and right = 5, then valid partners are 3, 4, 5 => 3 partners.
            pairCount += (right - left);
        }

        return pairCount;
    }

    /**
     * A helper method that runs the algorithm and prints a friendly summary.
     *
     * @param brightness the array of photo brightness values
     * @param budget the maximum allowed absolute difference
     * @return the computed number of valid pairs
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long demonstrate(int[] brightness, int budget) {
        long result = countPairsWithinBudget(brightness, budget);
        System.out.println("brightness = " + Arrays.toString(brightness));
        System.out.println("budget = " + budget);
        System.out.println("valid pair count = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified expected outputs:
     * Example 1 -> 4
     * Example 2 -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: O(n) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // brightness = [4, 1, 7, 5], budget = 3
        // Valid pairs:
        // (4,1) diff 3
        // (4,7) diff 3
        // (4,5) diff 1
        // (7,5) diff 2
        // Total = 4
        int[] brightness1 = {4, 1, 7, 5};
        int budget1 = 3;
        long result1 = solution.demonstrate(brightness1, budget1);
        System.out.println("Expected: 4, Actual: " + result1);
        System.out.println();

        // Example 2:
        // brightness = [2, 2, 2, 8, 9], budget = 1
        // Among the three 2s: C(3,2) = 3 valid pairs
        // (8,9) is also valid
        // Total = 4
        int[] brightness2 = {2, 2, 2, 8, 9};
        int budget2 = 1;
        long result2 = solution.demonstrate(brightness2, budget2);
        System.out.println("Expected: 4, Actual: " + result2);
        System.out.println();

        // Additional quick sanity checks for beginners:

        // No valid pairs because the difference is too large.
        int[] brightness3 = {1, 10, 20};
        int budget3 = 2;
        long result3 = solution.demonstrate(brightness3, budget3);
        System.out.println("Expected: 0, Actual: " + result3);
        System.out.println();

        // All pairs are valid when budget is large enough.
        // For 4 elements, total pairs = 4 * 3 / 2 = 6
        int[] brightness4 = {3, 6, 9, 12};
        int budget4 = 100;
        long result4 = solution.demonstrate(brightness4, budget4);
        System.out.println("Expected: 6, Actual: " + result4);
    }
}