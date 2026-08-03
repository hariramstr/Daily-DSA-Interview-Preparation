import java.util.*;

/*
 * Title: Longest Run of Pairwise Disjoint Feature Masks
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an array masks where masks[i] is a non-negative integer representing
 * the enabled feature bits of the i-th software build in chronological order.
 * A contiguous run of builds is called compatible if no bit position is enabled in
 * more than one build inside that run. In other words, for every pair of different
 * indices a and b within the same run, masks[a] & masks[b] == 0.
 *
 * Your task is to return the length of the longest compatible contiguous run.
 *
 * This is not the same as checking whether the bitwise AND of the whole window is zero.
 * A run is valid only when every bit appears at most once across the entire window.
 * For example, [1, 2, 4] is compatible, but [3, 4, 1] is not, because bit 0 appears
 * in both 3 and 1.
 *
 * Design an efficient algorithm that works for large inputs.
 *
 * Constraints:
 * - 1 <= masks.length <= 100000
 * - 0 <= masks[i] <= 10^9
 * - masks[i] fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: masks = [1, 2, 4, 3, 8]
 * Output: 3
 * Explanation: The longest compatible run is [1, 2, 4]. When 3 is added, it overlaps
 * with both 1 and 2, so the window must shrink.
 *
 * Example 2:
 * Input: masks = [5, 1, 2, 8, 4]
 * Output: 4
 * Explanation: The run [1, 2, 8, 4] is compatible because no bit is shared between
 * any two numbers. The first value 5 overlaps with 1 and 4 through its set bits,
 * so the full array is not valid.
 */

public class Solution {

    /**
     * Returns the length of the longest contiguous run in which every pair of values
     * has bitwise AND equal to zero.
     *
     * Core idea:
     * We maintain a sliding window [left, right] that is always valid.
     * For a valid window, no bit can appear in more than one number.
     * Because of that property, the bitwise OR of all numbers in the window can be stored
     * in a single integer called windowBits.
     *
     * Why OR works here:
     * - If the window is valid, each set bit belongs to exactly one element in the window.
     * - Therefore, when removing an element from the left, we can safely "toggle off"
     *   its bits using XOR, because no other element in the window shares those bits.
     *
     * Process:
     * 1. Expand the window by moving right.
     * 2. If the new value overlaps with any bit already in the window
     *    ((windowBits & masks[right]) != 0), the window becomes invalid.
     * 3. Shrink from the left until the overlap disappears.
     * 4. Add the new value to the window using OR.
     * 5. Update the best length.
     *
     * @param masks the array of non-negative feature masks in chronological order
     * @return the maximum length of a contiguous compatible run
     * Time complexity: O(n), because each element enters and leaves the sliding window at most once
     * Space complexity: O(1), because only a few integer variables are used
     */
    public int longestCompatibleRun(int[] masks) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This integer stores the union of all bits currently present in the window.
        // Because the window is always kept valid, each bit appears at most once.
        int windowBits = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary one step at a time.
        for (int right = 0; right < masks.length; right++) {
            int current = masks[right];

            // If current shares any bit with the existing window, the window is invalid.
            // We must remove elements from the left until there is no overlap.
            //
            // Condition meaning:
            // (windowBits & current) != 0
            // => at least one bit is already used by some element in the window
            // => adding current would violate pairwise disjointness
            while ((windowBits & current) != 0) {
                // Remove masks[left] from the window.
                //
                // Since the window was valid before removal, every bit in masks[left]
                // appears exactly once in the window. Therefore XOR cleanly removes
                // those bits from windowBits.
                windowBits ^= masks[left];

                // Move left boundary forward.
                left++;
            }

            // Now there is no overlap, so we can safely include current.
            // OR adds its bits into the window summary.
            windowBits |= current;

            // Current valid window length is right - left + 1.
            int currentLength = right - left + 1;

            // Update the best answer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * A helper method that prints a detailed demonstration of how the algorithm behaves
     * on a given input array. This is useful for learning and for manually verifying
     * the sliding window process.
     *
     * @param masks the input array to trace
     * @return the computed longest compatible run length for the provided array
     * Time complexity: O(n), because it follows the same sliding window process
     * Space complexity: O(1), excluding output generated by printing
     */
    public int traceLongestCompatibleRun(int[] masks) {
        int left = 0;
        int windowBits = 0;
        int maxLength = 0;

        System.out.println("Tracing input: " + Arrays.toString(masks));

        for (int right = 0; right < masks.length; right++) {
            int current = masks[right];
            System.out.println();
            System.out.println("Step: try to include masks[" + right + "] = " + current);
            System.out.println("Current window before adjustment: left = " + left + ", right = " + (right - 1));
            System.out.println("windowBits before adjustment = " + windowBits);

            while ((windowBits & current) != 0) {
                System.out.println("Overlap detected because (windowBits & current) = " + (windowBits & current));
                System.out.println("Removing masks[" + left + "] = " + masks[left] + " from the left side");
                windowBits ^= masks[left];
                left++;
                System.out.println("After removal: left = " + left + ", windowBits = " + windowBits);
            }

            windowBits |= current;
            int currentLength = right - left + 1;
            maxLength = Math.max(maxLength, currentLength);

            System.out.println("Included " + current + ", new window is [" + left + ", " + right + "]");
            System.out.println("windowBits after inclusion = " + windowBits);
            System.out.println("Current valid length = " + currentLength);
            System.out.println("Best length so far = " + maxLength);
        }

        System.out.println();
        System.out.println("Final answer = " + maxLength);
        return maxLength;
    }

    /**
     * Utility method to run one test case and print the result.
     *
     * @param masks the test input array
     * @return the computed answer for the test case
     * Time complexity: O(n), delegated to longestCompatibleRun
     * Space complexity: O(1)
     */
    public int runAndPrint(int[] masks) {
        int result = longestCompatibleRun(masks);
        System.out.println("Input:  " + Arrays.toString(masks));
        System.out.println("Output: " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a few additional cases.
     *
     * Verified sample outputs:
     * - [1, 2, 4, 3, 8] -> 3
     * - [5, 1, 2, 8, 4] -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of elements across demonstrated test cases)
     * Space complexity: O(1), excluding printing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sample1 = {1, 2, 4, 3, 8};
        int[] sample2 = {5, 1, 2, 8, 4};

        System.out.println("Sample demonstrations:");
        solution.runAndPrint(sample1); // Expected: 3
        solution.runAndPrint(sample2); // Expected: 4

        System.out.println("Detailed trace for Example 1:");
        solution.traceLongestCompatibleRun(sample1);
        System.out.println();

        System.out.println("Detailed trace for Example 2:");
        solution.traceLongestCompatibleRun(sample2);
        System.out.println();

        System.out.println("Additional tests:");
        solution.runAndPrint(new int[]{0});              // Expected: 1
        solution.runAndPrint(new int[]{1, 1, 1});       // Expected: 1
        solution.runAndPrint(new int[]{1, 2, 4, 8});    // Expected: 4
        solution.runAndPrint(new int[]{3, 4, 1});       // Expected: 2
        solution.runAndPrint(new int[]{0, 0, 0, 0});    // Expected: 4
    }
}