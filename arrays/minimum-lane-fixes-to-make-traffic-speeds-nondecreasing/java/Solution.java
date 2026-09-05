import java.util.*;

/*
 * Title: Minimum Lane Fixes to Make Traffic Speeds Nondecreasing
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A city records the average vehicle speed for each lane segment along a highway during a short time window.
 * The speeds are stored in an integer array speeds, where speeds[i] is the measured speed at segment i
 * from west to east.
 *
 * Because of sensor noise, the recorded speeds may go down and up unpredictably. Traffic engineers want
 * the final reported sequence to be nondecreasing, meaning final[i] <= final[i + 1] for every valid i.
 * To correct the data, they are allowed to apply lane fixes. In one lane fix, they may choose a single
 * segment and increase its speed by any positive amount. Decreasing values is not allowed.
 *
 * Return the minimum total added speed needed to make the entire array nondecreasing.
 *
 * Your task is only to compute the minimum total increase, not the resulting array.
 *
 * Constraints:
 * - 1 <= speeds.length <= 100000
 * - 0 <= speeds[i] <= 1000000000
 * - The answer can be larger than 32-bit integer range, so use 64-bit arithmetic.
 *
 * Example 1:
 * Input: speeds = [5, 3, 3, 7, 2]
 * Output: 9
 * Explanation:
 * Increase the second value from 3 to 5 (+2),
 * increase the third value from 3 to 5 (+2),
 * and increase the last value from 2 to 7 (+5).
 * Total added speed = 2 + 2 + 5 = 9.
 *
 * Example 2:
 * Input: speeds = [1, 2, 4, 4, 6]
 * Output: 0
 * Explanation:
 * The array is already nondecreasing, so no fixes are needed.
 */

public class Solution {

    /**
     * Computes the minimum total increase required to make the given array nondecreasing.
     *
     * The key greedy idea:
     * - Scan from left to right.
     * - Keep track of the minimum value that the current element must be at least equal to,
     *   which is simply the previous final value in the nondecreasing sequence.
     * - If the current value is already large enough, no increase is needed.
     * - If the current value is smaller, we must increase it exactly up to that previous value.
     *   Increasing it any more would only add unnecessary cost and can never help reduce future cost.
     *
     * @param speeds the recorded speeds for highway segments
     * @return the minimum total added speed needed to make the array nondecreasing
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), ignoring input storage
     */
    public long minimumTotalIncrease(int[] speeds) {
        // This variable stores the total amount of speed we add across all segments.
        // We use long because the total can exceed the 32-bit integer range.
        long totalIncrease = 0L;

        // "previousFinalValue" represents the value that the previous segment ends up having
        // in the corrected nondecreasing sequence.
        //
        // Initially, the first element does not need any previous comparison,
        // so we can start with its original value.
        long previousFinalValue = speeds[0];

        // Process every segment from left to right, starting from index 1
        // because index 0 is already our starting point.
        for (int i = 1; i < speeds.length; i++) {
            // Read the current original speed.
            long currentValue = speeds[i];

            // If currentValue is smaller than previousFinalValue, then the sequence would decrease here.
            // Since decreasing is not allowed, the ONLY way to fix this is to increase currentValue.
            if (currentValue < previousFinalValue) {
                // We must raise currentValue exactly to previousFinalValue.
                // Why exactly?
                // - Raising it less would still violate nondecreasing order.
                // - Raising it more would cost extra and provide no benefit for minimizing total increase.
                long neededIncrease = previousFinalValue - currentValue;

                // Add this required increase to the answer.
                totalIncrease += neededIncrease;

                // After correction, this position's final value becomes previousFinalValue.
                // So previousFinalValue stays unchanged.
            } else {
                // If currentValue is already >= previousFinalValue,
                // then no increase is needed at this position.
                //
                // This position now becomes the new "previous final value" for the next step,
                // because the corrected sequence up to this point ends with currentValue.
                previousFinalValue = currentValue;
            }
        }

        return totalIncrease;
    }

    /**
     * An alternative helper method that also computes the minimum total increase.
     *
     * This version conceptually builds the corrected sequence value-by-value without modifying
     * the original array. It is functionally equivalent to minimumTotalIncrease.
     *
     * @param speeds the recorded speeds for highway segments
     * @return the minimum total added speed needed to make the array nondecreasing
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), ignoring input storage
     */
    public long minimumTotalIncreaseVerbose(int[] speeds) {
        long totalIncrease = 0L;

        // The first element remains as-is because there is nothing before it.
        long finalValueAtPreviousIndex = speeds[0];

        for (int i = 1; i < speeds.length; i++) {
            long original = speeds[i];

            // The corrected value at this index must be at least the corrected value
            // from the previous index to maintain nondecreasing order.
            long corrected = Math.max(original, finalValueAtPreviousIndex);

            // Any difference between corrected and original is the increase we pay.
            totalIncrease += corrected - original;

            // Move forward: this corrected value becomes the previous value for the next iteration.
            finalValueAtPreviousIndex = corrected;
        }

        return totalIncrease;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and a few additional examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) across all demonstrations, where k is the number of test arrays
     * Space complexity: O(1), excluding the sample arrays themselves
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] speeds1 = {5, 3, 3, 7, 2};
        int[] speeds2 = {1, 2, 4, 4, 6};
        int[] speeds3 = {10};
        int[] speeds4 = {4, 1, 2, 1, 3};
        int[] speeds5 = {0, 0, 0, 0};

        System.out.println("Sample 1:");
        System.out.println("Input: " + Arrays.toString(speeds1));
        System.out.println("Output: " + solution.minimumTotalIncrease(speeds1));
        System.out.println("Expected: 9");
        System.out.println();

        System.out.println("Sample 2:");
        System.out.println("Input: " + Arrays.toString(speeds2));
        System.out.println("Output: " + solution.minimumTotalIncrease(speeds2));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println("Additional Test 1:");
        System.out.println("Input: " + Arrays.toString(speeds3));
        System.out.println("Output: " + solution.minimumTotalIncrease(speeds3));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println("Additional Test 2:");
        System.out.println("Input: " + Arrays.toString(speeds4));
        System.out.println("Output: " + solution.minimumTotalIncrease(speeds4));
        System.out.println("Expected: 9");
        System.out.println();

        System.out.println("Additional Test 3:");
        System.out.println("Input: " + Arrays.toString(speeds5));
        System.out.println("Output: " + solution.minimumTotalIncrease(speeds5));
        System.out.println("Expected: 0");
    }
}