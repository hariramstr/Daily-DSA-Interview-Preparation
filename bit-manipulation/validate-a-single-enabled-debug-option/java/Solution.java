/*
 * Title: Validate a Single Enabled Debug Option
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A monitoring tool stores debug settings for a service in one non-negative integer called mask.
 * Each bit in mask represents whether a specific debug option is enabled (1) or disabled (0).
 * For safety reasons, the service is considered valid only when exactly one debug option is
 * enabled at a time.
 *
 * Given an integer mask, return true if it contains exactly one set bit in its binary
 * representation. Otherwise, return false.
 *
 * This is a bit manipulation problem. A direct loop over all bits works, but there is also a
 * simple constant-time trick using bitwise operators. Your solution should correctly handle 0,
 * since a value of 0 means no options are enabled and therefore is not valid.
 *
 * Constraints:
 * - 0 <= mask <= 2^31 - 1
 * - The expected solution should use O(1) extra space.
 * - Any solution running in O(number of bits) or better is acceptable.
 *
 * Example 1:
 * Input: mask = 8
 * Output: true
 * Explanation: 8 in binary is 1000, which has exactly one set bit.
 *
 * Example 2:
 * Input: mask = 10
 * Output: false
 * Explanation: 10 in binary is 1010, which has two set bits, so more than one debug option
 * is enabled.
 */

import java.util.*;

public class Solution {

    /**
     * Determines whether the given mask has exactly one enabled debug option.
     *
     * This method uses the classic bit trick:
     * - A positive power of two has exactly one set bit.
     * - For such a number n, the expression (n & (n - 1)) becomes 0.
     *
     * Why this works:
     * - Suppose n = 8, which is binary 1000.
     * - Then n - 1 = 7, which is binary 0111.
     * - 1000 & 0111 = 0000
     *
     * If a number has more than one set bit:
     * - Suppose n = 10, which is binary 1010.
     * - Then n - 1 = 9, which is binary 1001.
     * - 1010 & 1001 = 1000, which is not 0
     *
     * Important special case:
     * - 0 must return false, because it has no set bits.
     *
     * @param mask the non-negative integer representing enabled/disabled debug options
     * @return true if and only if mask contains exactly one set bit; otherwise false
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isValidDebugMask(int mask) {
        // Step 1:
        // If mask is 0, then no debug options are enabled.
        // The problem requires exactly one enabled option, so 0 is invalid.
        if (mask == 0) {
            return false;
        }

        // Step 2:
        // Compute (mask & (mask - 1)).
        //
        // Detailed intuition:
        // - Subtracting 1 from a number flips:
        //   - the rightmost set bit from 1 to 0
        //   - all bits to the right of it from 0 to 1
        //
        // If mask has exactly one set bit:
        // - Example: 1000
        // - mask - 1: 0111
        // - AND result: 0000
        //
        // If mask has more than one set bit:
        // - At least one set bit remains after the AND, so result is not 0.
        //
        // Therefore:
        // - result == 0  => exactly one set bit
        // - result != 0  => zero or multiple set bits
        //
        // Since we already handled mask == 0 above, result == 0 now means exactly one set bit.
        return (mask & (mask - 1)) == 0;
    }

    /**
     * Determines whether the given mask has exactly one set bit by explicitly counting set bits.
     *
     * This method is included for learning purposes. It is more direct and beginner-friendly:
     * - Repeatedly inspect the least significant bit
     * - Count how many 1s appear
     * - Return true only if the count is exactly 1
     *
     * @param mask the non-negative integer representing enabled/disabled debug options
     * @return true if mask contains exactly one set bit; otherwise false
     * Time complexity: O(number of bits), which is O(31) for int in this problem
     * Space complexity: O(1)
     */
    public boolean isValidDebugMaskByCounting(int mask) {
        // Step 1:
        // Start a counter at 0. This will track how many bits are set to 1.
        int setBitCount = 0;

        // Step 2:
        // Continue until all bits have been processed.
        // Each iteration shifts the number one position to the right.
        while (mask > 0) {
            // Step 2a:
            // Check the least significant bit using (mask & 1).
            //
            // - If the last bit is 1, then (mask & 1) == 1
            // - If the last bit is 0, then (mask & 1) == 0
            if ((mask & 1) == 1) {
                setBitCount++;

                // Step 2b:
                // If we already found more than one set bit, we can stop early.
                // The answer must be false in that case.
                if (setBitCount > 1) {
                    return false;
                }
            }

            // Step 2c:
            // Shift right by one bit to examine the next bit in the next iteration.
            mask >>= 1;
        }

        // Step 3:
        // The mask is valid only if exactly one set bit was found.
        return setBitCount == 1;
    }

    /**
     * Runs sample demonstrations of the solution and prints the results.
     *
     * This main method verifies the examples from the problem statement and also shows a few
     * additional test cases for clarity.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration set
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Problem example 1:
        // mask = 8
        // Binary: 1000
        // Exactly one set bit -> expected true
        int mask1 = 8;
        System.out.println("mask = " + mask1 + " -> " + solution.isValidDebugMask(mask1));

        // Problem example 2:
        // mask = 10
        // Binary: 1010
        // Two set bits -> expected false
        int mask2 = 10;
        System.out.println("mask = " + mask2 + " -> " + solution.isValidDebugMask(mask2));

        // Additional beginner-friendly checks:

        // mask = 0
        // Binary: 0
        // No set bits -> false
        int mask3 = 0;
        System.out.println("mask = " + mask3 + " -> " + solution.isValidDebugMask(mask3));

        // mask = 1
        // Binary: 1
        // Exactly one set bit -> true
        int mask4 = 1;
        System.out.println("mask = " + mask4 + " -> " + solution.isValidDebugMask(mask4));

        // mask = 16
        // Binary: 10000
        // Exactly one set bit -> true
        int mask5 = 16;
        System.out.println("mask = " + mask5 + " -> " + solution.isValidDebugMask(mask5));

        // mask = 18
        // Binary: 10010
        // Two set bits -> false
        int mask6 = 18;
        System.out.println("mask = " + mask6 + " -> " + solution.isValidDebugMask(mask6));

        // Optional comparison with the counting-based method to show both methods agree.
        System.out.println("Using counting method:");
        System.out.println("mask = " + mask1 + " -> " + solution.isValidDebugMaskByCounting(mask1));
        System.out.println("mask = " + mask2 + " -> " + solution.isValidDebugMaskByCounting(mask2));
    }
}