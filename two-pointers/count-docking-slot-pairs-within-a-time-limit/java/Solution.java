import java.util.*;

/*
 * Title: Count Docking Slot Pairs Within a Time Limit
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A shipping terminal records the available docking duration of each open slot during the next hour.
 * You are given an integer array durations where durations[i] is the number of minutes that slot i
 * will remain available, and an integer limit. Two different slots can be assigned to a dual-berth
 * vessel only if their combined available time is less than or equal to limit.
 *
 * Your task is to return the number of distinct pairs of slots (i, j) such that i < j and
 * durations[i] + durations[j] <= limit.
 *
 * The input array is not guaranteed to be sorted. A brute-force O(n^2) approach may be too slow
 * for large terminals, so you should design an efficient solution using sorting and a two-pointer strategy.
 *
 * A pair is counted by indices, not by values. This means if the same duration appears multiple times,
 * different index combinations are considered different valid pairs.
 *
 * Constraints:
 * - 1 <= durations.length <= 200000
 * - 0 <= durations[i] <= 1000000000
 * - 0 <= limit <= 2000000000
 * - The answer can be large, so use a 64-bit integer type if needed.
 *
 * Example 1:
 * Input: durations = [4, 1, 3, 2], limit = 5
 * Output: 4
 * Explanation: After sorting, durations become [1, 2, 3, 4]. Valid pairs are (1,2), (1,3), (1,4),
 * and (2,3), whose sums are 3, 4, 5, and 5.
 *
 * Example 2:
 * Input: durations = [6, 2, 2, 5, 1], limit = 7
 * Output: 6
 * Explanation: Valid index pairs correspond to value pairs (1,2), (1,2), (1,5), (1,6), (2,2), and (2,5).
 * Pairs involving 6 with 2 or 5 exceed the limit, and 5 with 2 also exceeds it.
 */

public class Solution {

    /**
     * Counts the number of distinct index pairs (i, j) such that i < j and
     * durations[i] + durations[j] <= limit.
     *
     * The method uses the standard efficient approach:
     * 1. Copy and sort the array.
     * 2. Use two pointers:
     *    - left starts at the smallest value
     *    - right starts at the largest value
     * 3. If sorted[left] + sorted[right] <= limit, then every element from
     *    left + 1 through right can pair with sorted[left], because the array is sorted.
     *    So we add (right - left) pairs at once and move left forward.
     * 4. Otherwise, the sum is too large, so we move right backward to try a smaller value.
     *
     * @param durations the array of slot availability durations in minutes
     * @param limit the maximum allowed combined duration for a valid pair
     * @return the number of valid distinct pairs as a long
     * Time complexity: O(n log n) due to sorting, where n is durations.length
     * Space complexity: O(n) for the copied array used for sorting
     */
    public long countPairsWithinLimit(int[] durations, int limit) {
        // Defensive handling:
        // If the array has fewer than 2 elements, no pair can exist.
        if (durations == null || durations.length < 2) {
            return 0L;
        }

        // We copy the input array so that the original input remains unchanged.
        // This is often a good practice in interview-style problems unless mutation is explicitly allowed.
        int[] sorted = Arrays.copyOf(durations, durations.length);

        // Sort the copied array in non-decreasing order.
        // After sorting, smaller values are on the left and larger values are on the right.
        Arrays.sort(sorted);

        // Two pointers:
        // left points to the current smallest candidate.
        // right points to the current largest candidate.
        int left = 0;
        int right = sorted.length - 1;

        // Use long because the number of valid pairs can be very large.
        // For n = 200000, the maximum number of pairs is n * (n - 1) / 2,
        // which does not fit safely in int.
        long count = 0L;

        // Continue until the pointers cross.
        // We only consider pairs where left < right.
        while (left < right) {
            // Use long for the sum to avoid any risk of integer overflow,
            // even though the given constraints are still within int range for addition.
            long sum = (long) sorted[left] + sorted[right];

            // Case 1:
            // If the smallest remaining value plus the largest remaining value is valid,
            // then the smallest remaining value can pair with EVERY value between left+1 and right.
            //
            // Why?
            // Because the array is sorted:
            // sorted[left] + sorted[right] <= limit
            // implies
            // sorted[left] + sorted[k] <= limit for every k in [left+1, right]
            // since sorted[k] <= sorted[right].
            if (sum <= limit) {
                // Number of valid pairs formed with sorted[left]:
                // (left, left+1), (left, left+2), ..., (left, right)
                // That is exactly (right - left) pairs.
                count += (right - left);

                // Move left forward to count pairs for the next smallest value.
                left++;
            } else {
                // Case 2:
                // If the sum is too large, then sorted[right] is too large to pair with sorted[left].
                // Since sorted[left] is already the smallest available value,
                // sorted[right] also cannot pair with any value to the right of left and still become smaller.
                //
                // Therefore, to reduce the sum, we must move right backward.
                right--;
            }
        }

        return count;
    }

    /**
     * A simple brute-force method for verification and educational comparison.
     * This checks every pair and counts those whose sum is within the limit.
     *
     * This method is not efficient for large inputs, but it is useful for:
     * - understanding the problem
     * - testing the optimized solution on small examples
     *
     * @param durations the array of slot availability durations in minutes
     * @param limit the maximum allowed combined duration for a valid pair
     * @return the number of valid distinct pairs as a long
     * Time complexity: O(n^2)
     * Space complexity: O(1) extra space
     */
    public long countPairsWithinLimitBruteForce(int[] durations, int limit) {
        if (durations == null || durations.length < 2) {
            return 0L;
        }

        long count = 0L;

        // Check every pair of indices (i, j) with i < j.
        for (int i = 0; i < durations.length; i++) {
            for (int j = i + 1; j < durations.length; j++) {
                long sum = (long) durations[i] + durations[j];
                if (sum <= limit) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Prints an array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string creation
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * It also compares the optimized solution with the brute-force solution on
     * the same small examples to help confirm correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demo inputs; for the shown examples it is small
     * Space complexity: Depends on the demo inputs; for the shown examples it is small
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] durations1 = {4, 1, 3, 2};
        int limit1 = 5;

        long result1 = solution.countPairsWithinLimit(durations1, limit1);
        long brute1 = solution.countPairsWithinLimitBruteForce(durations1, limit1);

        System.out.println("Example 1");
        System.out.println("durations = " + solution.arrayToString(durations1));
        System.out.println("limit = " + limit1);
        System.out.println("Optimized result = " + result1);
        System.out.println("Brute-force result = " + brute1);
        System.out.println("Expected = 4");
        System.out.println();

        // Example 2
        int[] durations2 = {6, 2, 2, 5, 1};
        int limit2 = 7;

        long result2 = solution.countPairsWithinLimit(durations2, limit2);
        long brute2 = solution.countPairsWithinLimitBruteForce(durations2, limit2);

        System.out.println("Example 2");
        System.out.println("durations = " + solution.arrayToString(durations2));
        System.out.println("limit = " + limit2);
        System.out.println("Optimized result = " + result2);
        System.out.println("Brute-force result = " + brute2);
        System.out.println("Expected = 6");
        System.out.println();

        // Additional quick demonstration
        int[] durations3 = {0, 0, 0, 0};
        int limit3 = 0;

        long result3 = solution.countPairsWithinLimit(durations3, limit3);
        long brute3 = solution.countPairsWithinLimitBruteForce(durations3, limit3);

        System.out.println("Additional Example");
        System.out.println("durations = " + solution.arrayToString(durations3));
        System.out.println("limit = " + limit3);
        System.out.println("Optimized result = " + result3);
        System.out.println("Brute-force result = " + brute3);
        System.out.println("Expected = 6");
    }
}