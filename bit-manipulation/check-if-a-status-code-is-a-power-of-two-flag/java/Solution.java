/*
Problem Title: Check if a Status Code Is a Power-of-Two Flag

Problem Description:
In a monitoring system, each valid standalone status flag is encoded as a positive integer
with exactly one bit set in its binary representation. For example, 1 (binary 1), 2 (binary 10),
4 (binary 100), and 8 (binary 1000) are valid standalone flags. A number like 10 (binary 1010)
is not, because it has more than one set bit. Given an integer code, determine whether it
represents a valid standalone status flag.

Return true if the code is a positive power of two, and false otherwise.

A simple and efficient bit manipulation solution is expected. Try to solve it in O(1) time
using bitwise operators rather than loops over all bits.

Constraints:
- -2^31 <= code <= 2^31 - 1
- The input is a single integer.
- 0 and all negative numbers are not valid standalone flags.

Example 1:
Input: code = 16
Output: true
Explanation: 16 in binary is 10000, which contains exactly one set bit, so it is a valid standalone flag.

Example 2:
Input: code = 18
Output: false
Explanation: 18 in binary is 10010, which contains two set bits, so it is not a power of two.

Task:
Decide whether the given code has exactly one set bit and is positive.
*/

import java.util.*;

public class Solution {

    /**
     * Determines whether the given integer is a positive power of two.
     *
     * A number is a power of two if:
     * 1. It is greater than 0
     * 2. It has exactly one bit set in its binary representation
     *
     * Key bit trick:
     * For any positive power of two n, the expression (n & (n - 1)) equals 0.
     * This works because:
     * - A power of two looks like: 1000...000
     * - Subtracting 1 makes it:   0111...111
     * - ANDing them gives:        0000...000
     *
     * @param code the integer status code to check
     * @return true if code is positive and has exactly one set bit; false otherwise
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isPowerOfTwoFlag(int code) {
        // Step 1:
        // Reject all non-positive numbers immediately.
        // Why?
        // - 0 is not a power of two.
        // - Negative numbers are not valid standalone flags in this problem.
        if (code <= 0) {
            return false;
        }

        // Step 2:
        // Use the classic bit manipulation rule:
        // A positive number is a power of two if and only if:
        // (code & (code - 1)) == 0
        //
        // Let's understand this very carefully:
        //
        // Example: code = 16
        // Binary of 16     = 10000
        // Binary of 15     = 01111
        // 10000 & 01111    = 00000
        // Result is 0, so 16 is a power of two.
        //
        // Example: code = 18
        // Binary of 18     = 10010
        // Binary of 17     = 10001
        // 10010 & 10001    = 10000
        // Result is not 0, so 18 is NOT a power of two.
        //
        // Why this works:
        // - A power of two has exactly one 1 bit.
        // - Subtracting 1 flips that 1 bit to 0 and turns all lower bits into 1.
        // - Therefore, the original number and one-less-than-it share no 1 bits.
        return (code & (code - 1)) == 0;
    }

    /**
     * Demonstrates the solution on several sample and edge-case inputs.
     *
     * This method prints the input value and whether it is a valid standalone
     * status flag (that is, a positive power of two).
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     * Time complexity: O(k), where k is the number of demonstration test cases
     * Space complexity: O(1), excluding output
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Demonstration inputs:
        // Includes the examples from the problem statement and a few extra cases
        // to help beginners understand the behavior.
        int[] testCodes = {
            16,   // expected true
            18,   // expected false
            1,    // expected true
            2,    // expected true
            3,    // expected false
            4,    // expected true
            8,    // expected true
            10,   // expected false
            0,    // expected false
            -2    // expected false
        };

        // Print a simple header for readability.
        System.out.println("Check if a Status Code Is a Power-of-Two Flag");
        System.out.println();

        // Go through each test code and print the result.
        for (int code : testCodes) {
            boolean result = solution.isPowerOfTwoFlag(code);
            System.out.println("code = " + code + " -> " + result);
        }

        // Explicit verification of the problem examples:
        //
        // Example 1:
        // code = 16
        // 16 > 0, and (16 & 15) = 0
        // Therefore output is true.
        //
        // Example 2:
        // code = 18
        // 18 > 0, but (18 & 17) = 16, not 0
        // Therefore output is false.
        //
        // These match the required outputs in the problem statement.
    }
}