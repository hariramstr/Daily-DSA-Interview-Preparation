import java.util.*;

/*
 * Title: Closest Badge Pair Across Two Sorted Gates
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A company campus has two security gates, Gate A and Gate B. Each gate logs badge IDs
 * in non-decreasing order as employees pass through. You are given two sorted integer
 * arrays gateA and gateB, where each value represents a badge ID seen at that gate.
 * The same badge ID may appear multiple times in either array.
 *
 * Your task is to find one pair of badge IDs (a, b) such that a comes from gateA,
 * b comes from gateB, and the absolute difference |a - b| is as small as possible.
 * Return that minimum absolute difference.
 *
 * If either array is empty, return -1 because no cross-gate pair can be formed.
 *
 * This problem should be solved efficiently by taking advantage of the fact that both
 * arrays are already sorted. A brute-force solution that compares every pair may be
 * too slow for large inputs.
 *
 * Constraints:
 * - 0 <= gateA.length, gateB.length <= 2 * 10^5
 * - -10^9 <= gateA[i], gateB[i] <= 10^9
 * - gateA and gateB are each sorted in non-decreasing order
 * - Duplicate values are allowed
 *
 * Example 1:
 * Input: gateA = [3, 8, 15, 21], gateB = [1, 9, 18, 30]
 * Output: 1
 * Explanation: The closest cross-gate pair is (8, 9), so the minimum absolute difference is 1.
 *
 * Example 2:
 * Input: gateA = [-10, -4, 0, 7], gateB = [-6, -3, 2, 11]
 * Output: 1
 * Explanation: There are several close pairs. The best valid pair is (-4, -3), giving a difference of 1.
 */

public class Solution {

    /**
     * Finds the minimum absolute difference between one value from gateA and one value from gateB.
     *
     * This method uses the classic two-pointer technique:
     * - Start one pointer at the beginning of gateA
     * - Start another pointer at the beginning of gateB
     * - Compare the current values
     * - Update the best answer seen so far
     * - Move the pointer that points to the smaller value
     *
     * Why does moving the smaller value work?
     * Because the arrays are sorted. If gateA[i] < gateB[j], then keeping gateB[j] fixed and moving
     * to a larger value in gateA is the only way we might reduce the difference. Moving gateB[j]
     * forward would only make gateB[j] the same or larger, which would not help compare with the
     * smaller gateA[i] as effectively.
     *
     * @param gateA the sorted badge IDs recorded at Gate A
     * @param gateB the sorted badge IDs recorded at Gate B
     * @return the minimum absolute difference between any cross-gate pair, or -1 if either array is empty
     * Time complexity: O(n + m), where n is gateA.length and m is gateB.length
     * Space complexity: O(1), ignoring input storage
     */
    public int minAbsoluteDifference(int[] gateA, int[] gateB) {
        // If either array is null, we cannot form a valid pair.
        if (gateA == null || gateB == null) {
            return -1;
        }

        // If either array is empty, no cross-gate pair exists.
        if (gateA.length == 0 || gateB.length == 0) {
            return -1;
        }

        // Pointer for gateA.
        int i = 0;

        // Pointer for gateB.
        int j = 0;

        // We store the best answer in a long first, even though the final answer fits in int.
        // This is a safe habit when subtracting integers, because subtraction can overflow
        // if done carelessly with int in some problems. Here values are within +/-1e9, so int
        // would still be safe, but long is clearer and more robust.
        long best = Long.MAX_VALUE;

        // Continue while both pointers are still inside their arrays.
        while (i < gateA.length && j < gateB.length) {
            // Read the current values from both arrays.
            long a = gateA[i];
            long b = gateB[j];

            // Compute the absolute difference for the current pair.
            long diff = Math.abs(a - b);

            // If this pair is better than anything seen before, update the answer.
            if (diff < best) {
                best = diff;
            }

            // If the difference is 0, we have found the smallest possible answer.
            // No absolute difference can be smaller than 0, so we can stop immediately.
            if (best == 0) {
                return 0;
            }

            // Key two-pointer decision:
            //
            // If a is smaller than b, then to possibly get closer to b,
            // we should move i forward so that gateA provides a larger value.
            if (a < b) {
                i++;
            }
            // If b is smaller than a, then to possibly get closer to a,
            // we should move j forward so that gateB provides a larger value.
            else {
                j++;
            }
        }

        // Convert the final answer back to int.
        return (int) best;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param array the array to print
     * @return a string representation of the array
     * Time complexity: O(k), where k is the array length
     * Space complexity: O(k) for the produced string
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * Verified examples:
     * - gateA = [3, 8, 15, 21], gateB = [1, 9, 18, 30] => 1
     *   Closest pair includes (8, 9), difference = 1
     *
     * - gateA = [-10, -4, 0, 7], gateB = [-6, -3, 2, 11] => 1
     *   Closest pair includes (-4, -3), difference = 1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total size of demonstrated arrays)
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] gateA1 = {3, 8, 15, 21};
        int[] gateB1 = {1, 9, 18, 30};
        int result1 = solution.minAbsoluteDifference(gateA1, gateB1);
        System.out.println("Example 1");
        System.out.println("gateA = " + solution.arrayToString(gateA1));
        System.out.println("gateB = " + solution.arrayToString(gateB1));
        System.out.println("Minimum absolute difference = " + result1);
        System.out.println();

        int[] gateA2 = {-10, -4, 0, 7};
        int[] gateB2 = {-6, -3, 2, 11};
        int result2 = solution.minAbsoluteDifference(gateA2, gateB2);
        System.out.println("Example 2");
        System.out.println("gateA = " + solution.arrayToString(gateA2));
        System.out.println("gateB = " + solution.arrayToString(gateB2));
        System.out.println("Minimum absolute difference = " + result2);
        System.out.println();

        int[] gateA3 = {};
        int[] gateB3 = {5, 10, 15};
        int result3 = solution.minAbsoluteDifference(gateA3, gateB3);
        System.out.println("Edge Case: One array is empty");
        System.out.println("gateA = " + solution.arrayToString(gateA3));
        System.out.println("gateB = " + solution.arrayToString(gateB3));
        System.out.println("Minimum absolute difference = " + result3);
        System.out.println();

        int[] gateA4 = {1, 2, 2, 2, 10};
        int[] gateB4 = {2, 2, 3};
        int result4 = solution.minAbsoluteDifference(gateA4, gateB4);
        System.out.println("Edge Case: Duplicate values with exact match");
        System.out.println("gateA = " + solution.arrayToString(gateA4));
        System.out.println("gateB = " + solution.arrayToString(gateB4));
        System.out.println("Minimum absolute difference = " + result4);
    }
}