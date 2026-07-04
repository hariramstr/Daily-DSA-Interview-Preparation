import java.util.*;

/*
 * Title: Minimum Processing Rate for Deadline Batches
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A data platform receives analytics batches, where batch i contains batches[i] records
 * that must be processed by a single worker. The worker uses one fixed processing rate r
 * records per hour for the entire day. In one hour, the worker may process records from
 * only one batch, and if a batch has fewer than r remaining records, the worker still
 * spends the full hour finishing that batch. The batches can be processed in any order.
 *
 * Given an integer array batches and an integer h, return the minimum integer processing
 * rate r such that all batches can be completed within h hours.
 *
 * This is a realistic capacity-planning problem: choosing a rate that is too low misses
 * the deadline, while choosing a rate that is too high may waste resources. Your task is
 * to find the smallest feasible rate.
 *
 * Constraints:
 * - 1 <= batches.length <= 100000
 * - 1 <= batches[i] <= 1000000000
 * - batches.length <= h <= 1000000000
 * - The answer always exists.
 *
 * Notes:
 * - The time needed for one batch of size x at rate r is ceil(x / r) hours.
 * - Since the order of processing does not change the total hours, you only need to
 *   determine whether a candidate rate is feasible.
 * - An O(n log M) solution is expected, where M is the maximum batch size.
 *
 * Example 1:
 * Input: batches = [12, 7, 25, 9], h = 10
 * Output: 7
 * Explanation:
 * At rate 7, the required hours are ceil(12/7) + ceil(7/7) + ceil(25/7) + ceil(9/7)
 * = 2 + 1 + 4 + 2 = 9, which fits within 10 hours.
 * At rate 6, the required hours are 2 + 2 + 5 + 2 = 11, which is too slow.
 * So the minimum valid rate is 7.
 *
 * Example 2:
 * Input: batches = [30, 11, 23, 4, 20], h = 6
 * Output: 23
 * Explanation:
 * At rate 23, the total time is 2 + 1 + 1 + 1 + 1 = 6 hours.
 * At rate 22, the total time becomes 2 + 1 + 2 + 1 + 1 = 7 hours, which exceeds the limit.
 */

public class Solution {

    /**
     * Finds the minimum integer processing rate needed to finish all batches within h hours.
     *
     * The key idea is binary search on the answer:
     * - If a rate r is sufficient, then any rate larger than r is also sufficient.
     * - If a rate r is insufficient, then any rate smaller than r is also insufficient.
     * This monotonic behavior makes binary search the ideal approach.
     *
     * @param batches an array where batches[i] is the number of records in the i-th batch
     * @param h the maximum total number of hours allowed to finish all batches
     * @return the smallest integer processing rate that allows all batches to be completed within h hours
     *
     * Time complexity: O(n log M), where n is batches.length and M is the maximum batch size.
     * Space complexity: O(1), ignoring the input array.
     */
    public int minProcessingRate(int[] batches, int h) {
        // The minimum possible rate is 1 record per hour.
        int left = 1;

        // The maximum necessary rate is the size of the largest batch.
        // Why? Because processing faster than the largest batch size in one hour
        // does not reduce any single batch below 1 hour.
        int right = getMaxBatchSize(batches);

        // We will binary search for the smallest feasible rate.
        while (left < right) {
            // Use this form to avoid integer overflow:
            // mid = left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Check whether this candidate rate is enough.
            if (canFinishWithinHours(batches, h, mid)) {
                // If mid works, it might be the answer,
                // but there could still be a smaller valid rate.
                // So we keep searching on the left half, including mid.
                right = mid;
            } else {
                // If mid does not work, every smaller rate also fails.
                // So we must search strictly to the right of mid.
                left = mid + 1;
            }
        }

        // When left == right, we have found the smallest feasible rate.
        return left;
    }

    /**
     * Determines whether all batches can be processed within h hours at a given rate.
     *
     * For each batch of size x, the required hours are ceil(x / rate).
     * To compute this using integer arithmetic safely and efficiently:
     * ceil(x / rate) = (x + rate - 1) / rate
     *
     * We use a long for the running total because the sum of hours can exceed
     * the range of int in intermediate calculations.
     *
     * @param batches an array of batch sizes
     * @param h the maximum allowed total hours
     * @param rate the candidate processing rate to test
     * @return true if all batches can be completed within h hours at the given rate; false otherwise
     *
     * Time complexity: O(n), where n is batches.length.
     * Space complexity: O(1).
     */
    public boolean canFinishWithinHours(int[] batches, int h, int rate) {
        long totalHours = 0L;

        // Process each batch independently.
        for (int batch : batches) {
            // Compute ceil(batch / rate) using integer math.
            totalHours += (batch + (long) rate - 1) / rate;

            // Important optimization:
            // If we already exceed h, we can stop early.
            // There is no need to continue summing.
            if (totalHours > h) {
                return false;
            }
        }

        return totalHours <= h;
    }

    /**
     * Finds the maximum value in the batches array.
     *
     * This value is used as the upper bound in binary search because no rate larger
     * than the maximum batch size is necessary.
     *
     * @param batches an array of batch sizes
     * @return the maximum batch size in the array
     *
     * Time complexity: O(n), where n is batches.length.
     * Space complexity: O(1).
     */
    public int getMaxBatchSize(int[] batches) {
        int max = 0;

        for (int batch : batches) {
            if (batch > max) {
                max = batch;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * This main method:
     * - creates sample test cases
     * - calls the algorithm
     * - prints the results
     * - includes expected outputs for easy verification
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n log M) per demonstrated test case.
     * Space complexity: O(1), ignoring input storage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] batches1 = {12, 7, 25, 9};
        int h1 = 10;
        int result1 = solution.minProcessingRate(batches1, h1);
        System.out.println("Example 1:");
        System.out.println("batches = " + Arrays.toString(batches1) + ", h = " + h1);
        System.out.println("Minimum processing rate = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        int[] batches2 = {30, 11, 23, 4, 20};
        int h2 = 6;
        int result2 = solution.minProcessingRate(batches2, h2);
        System.out.println("Example 2:");
        System.out.println("batches = " + Arrays.toString(batches2) + ", h = " + h2);
        System.out.println("Minimum processing rate = " + result2);
        System.out.println("Expected = 23");
        System.out.println();

        // Additional quick sanity check.
        int[] batches3 = {3, 6, 7, 11};
        int h3 = 8;
        int result3 = solution.minProcessingRate(batches3, h3);
        System.out.println("Additional Example:");
        System.out.println("batches = " + Arrays.toString(batches3) + ", h = " + h3);
        System.out.println("Minimum processing rate = " + result3);
        System.out.println("Expected = 4");
    }
}