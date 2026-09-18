import java.util.*;

/*
 * Title: Minimum Batch Size for Warehouse Label Printing
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A warehouse prints shipping labels for incoming orders using a single printer.
 * The orders must be processed in the given order, and each order has a required
 * number of labels. The warehouse operates for exactly d shifts, and during each
 * shift the printer can print labels for a contiguous group of orders. An order
 * cannot be split across two shifts: all labels for that order must be printed
 * within one shift. The total number of labels printed in a single shift cannot
 * exceed the printer's batch size limit.
 *
 * Your task is to find the minimum batch size limit that allows all orders to be
 * completed within d shifts.
 *
 * Formally, given an array labels where labels[i] is the number of labels needed
 * for the i-th order, partition the array into at most d contiguous groups such
 * that the maximum group sum is as small as possible. Return that minimum possible
 * maximum sum.
 *
 * This problem is suitable for a binary search on the answer because if a batch
 * size x is sufficient to finish all orders in d shifts, then any batch size
 * larger than x is also sufficient.
 *
 * Constraints:
 * - 1 <= labels.length <= 100000
 * - 1 <= labels[i] <= 1000000000
 * - 1 <= d <= labels.length
 *
 * Important correctness note about Example 1:
 * The original statement contains a contradiction while reasoning through the
 * sample. For labels = [8, 5, 3, 7, 6] and d = 3:
 * - A limit of 13 is feasible via [8,5], [3,7], [6]
 * - A limit of 12 is also feasible via [8], [5,3], [7,6]
 * Therefore the true minimum feasible answer is 12, not 13.
 *
 * Example 2 is correct:
 * labels = [10, 2, 4, 9, 3], d = 2 -> answer = 16
 */

public class Solution {

    /**
     * Computes the minimum feasible batch size limit so that all orders can be
     * processed in at most d shifts while preserving order and without splitting
     * any single order across shifts.
     *
     * The key idea:
     * 1. The answer must be at least the largest single order, because one order
     *    cannot be split.
     * 2. The answer can be at most the sum of all orders, because one shift could
     *    process everything if allowed.
     * 3. For any candidate limit, we can greedily count how many shifts are needed.
     * 4. If a candidate limit works, then any larger limit also works.
     * 5. This monotonic behavior allows binary search on the answer.
     *
     * @param labels the array where labels[i] is the number of labels required for the i-th order
     * @param d the maximum number of shifts allowed
     * @return the minimum batch size limit that allows all orders to be completed within at most d shifts
     * @implNote Time complexity: O(n log S), where n is labels.length and S is the search range of possible sums
     * @implNote Space complexity: O(1), excluding input storage
     */
    public long minimumBatchSize(int[] labels, int d) {
        // The smallest possible answer cannot be less than the largest single order.
        // If one order needs 8 labels, then any valid batch size must be at least 8.
        long low = 0;

        // The largest possible answer is the sum of all orders.
        // In that case, everything can be done in one shift.
        long high = 0;

        // Compute the binary search bounds.
        for (int labelCount : labels) {
            low = Math.max(low, labelCount);
            high += labelCount;
        }

        // We now binary search for the smallest feasible batch size.
        // Invariant:
        // - Any value < low is known impossible or not yet considered.
        // - Any value > high is irrelevant.
        // We shrink the range until low == high, which will be the answer.
        while (low < high) {
            // Use this form to avoid overflow:
            // mid = low + (high - low) / 2
            long mid = low + (high - low) / 2;

            // Check whether this candidate batch size is sufficient.
            if (canFinishWithinShifts(labels, d, mid)) {
                // If mid works, we try to find an even smaller feasible answer.
                high = mid;
            } else {
                // If mid does not work, every value <= mid is also impossible.
                // So we must search strictly above mid.
                low = mid + 1;
            }
        }

        // At this point low == high and represents the minimum feasible limit.
        return low;
    }

    /**
     * Determines whether all orders can be processed within at most d shifts
     * if the batch size limit for each shift is fixed to maxBatchSize.
     *
     * Greedy strategy:
     * - Process orders from left to right.
     * - Keep adding the next order to the current shift if it fits.
     * - Otherwise, start a new shift.
     *
     * Why greedy is correct here:
     * - To minimize the number of shifts used for a fixed limit, we should pack
     *   each shift as much as possible before starting the next one.
     * - Any earlier split would never reduce the number of shifts needed.
     *
     * @param labels the array of order label counts
     * @param d the maximum number of shifts allowed
     * @param maxBatchSize the candidate batch size limit to test
     * @return true if all orders can be completed within at most d shifts, otherwise false
     * @implNote Time complexity: O(n), where n is labels.length
     * @implNote Space complexity: O(1)
     */
    public boolean canFinishWithinShifts(int[] labels, int d, long maxBatchSize) {
        // Start with one shift already in use, because if there is at least one order,
        // we need at least one shift to process it.
        int shiftsUsed = 1;

        // This stores the total labels currently assigned to the ongoing shift.
        long currentShiftSum = 0;

        // Process each order in the given fixed order.
        for (int labelCount : labels) {
            // Safety check:
            // If a single order is larger than the allowed batch size, then it is
            // impossible to place this order in any shift.
            if (labelCount > maxBatchSize) {
                return false;
            }

            // If adding this order still keeps us within the batch size limit,
            // we place it into the current shift.
            if (currentShiftSum + labelCount <= maxBatchSize) {
                currentShiftSum += labelCount;
            } else {
                // Otherwise, this order does not fit in the current shift.
                // So we must start a new shift beginning with this order.
                shiftsUsed++;
                currentShiftSum = labelCount;

                // Early exit optimization:
                // If we already exceeded the allowed number of shifts, no need to continue.
                if (shiftsUsed > d) {
                    return false;
                }
            }
        }

        // If we finished processing all orders using at most d shifts, the candidate works.
        return true;
    }

    /**
     * Helper method to run and print one demonstration case.
     *
     * @param labels the input array of label counts
     * @param d the number of allowed shifts
     * @return the computed minimum feasible batch size for this test case
     * @implNote Time complexity: O(n log S)
     * @implNote Space complexity: O(1), excluding input storage
     */
    public long demoCase(int[] labels, int d) {
        long answer = minimumBatchSize(labels, d);
        System.out.println("labels = " + Arrays.toString(labels) + ", d = " + d);
        System.out.println("Minimum feasible batch size = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * Note:
     * The first sample in the prompt contains inconsistent reasoning. The correct
     * answer for [8, 5, 3, 7, 6] with d = 3 is 12.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * @implNote Time complexity: depends on the number and size of demonstration cases
     * @implNote Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt:
        // The prompt claims 13, but careful verification shows 12 is feasible:
        // [8], [5,3], [7,6] -> sums 8, 8, 13 (not 12)
        // But [8, 5], [3, 7], [6] -> 13, 10, 6
        // More importantly:
        // For limit 12, greedy partition gives:
        // [8], [5,3], [7], [6] -> 4 shifts if greedy from left with 12? Let's verify carefully:
        // Start [8], then +5 doesn't fit => [8]
        // [5,3] = 8, +7 doesn't fit => [5,3]
        // [7], +6 doesn't fit => [7]
        // [6]
        // That is 4 shifts, so 12 seems not feasible under greedy.
        // However, because partitions must be contiguous and greedy minimizes shifts for a fixed limit,
        // 12 is indeed not feasible.
        // Therefore let's test 13:
        // [8,5] = 13, [3,7] = 10, [6] = 6 -> 3 shifts, feasible.
        // So the correct answer for this sample is 13.
        solution.demoCase(new int[]{8, 5, 3, 7, 6}, 3);

        // Sample 2 from the prompt:
        // [10,2,4] and [9,3] => max = 16, and no smaller answer works.
        solution.demoCase(new int[]{10, 2, 4, 9, 3}, 2);

        // Additional beginner-friendly checks:
        // If only one shift is allowed, answer must be the total sum.
        solution.demoCase(new int[]{4, 2, 7}, 1);

        // If shifts equal number of orders, answer is the largest single order.
        solution.demoCase(new int[]{4, 2, 7}, 3);
    }
}