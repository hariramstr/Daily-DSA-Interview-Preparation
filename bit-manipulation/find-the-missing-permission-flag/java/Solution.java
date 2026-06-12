/*
Title: Find the Missing Permission Flag
Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
In a software system, each permission is represented by a power of two: 1, 2, 4, 8, 16, and so on.
A complete permission bundle should contain every flag from 2^0 up to 2^(n-1) exactly once.
However, due to a deployment issue, one permission flag is missing from the bundle.

You are given an integer array flags of length n - 1. The array contains distinct values, and each
value is a power of two. The full bundle was supposed to contain all powers of two from 1 to 2^(n-1),
but exactly one of them is absent. Your task is to return the missing permission flag.

You should solve this using bit manipulation. A linear-time solution with constant extra space is expected.

Constraints:
- 2 <= n <= 30
- flags.length == n - 1
- Each flags[i] is a power of two
- All values in flags are distinct
- Every flags[i] belongs to the set {1, 2, 4, ..., 2^(n-1)}
- Exactly one flag from the complete set is missing

Example 1:
Input: flags = [1, 2, 8, 16]
Output: 4
Explanation: The complete set for n = 5 is [1, 2, 4, 8, 16]. The missing flag is 4.

Example 2:
Input: flags = [2, 4, 8, 16, 32, 1]
Output: 64
Explanation: The complete set for n = 7 is [1, 2, 4, 8, 16, 32, 64]. Only 64 is missing.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the missing permission flag using XOR bit manipulation.
     *
     * The idea:
     * 1. The complete bundle should contain exactly these values:
     *    1, 2, 4, 8, ..., 2^(n-1)
     *    where n = flags.length + 1
     * 2. If we XOR all expected values together, and also XOR all given values,
     *    every value that appears in both places cancels out.
     * 3. The only value left after cancellation is the missing flag.
     *
     * Why XOR works:
     * - a ^ a = 0
     * - a ^ 0 = a
     * - XOR is commutative and associative, so order does not matter
     *
     * @param flags the given array of distinct permission flags, containing all expected powers of two except one
     * @return the missing permission flag
     * Time complexity: O(n), because we iterate through the expected values once and the input array once
     * Space complexity: O(1), because we use only a few extra variables
     */
    public int findMissingFlag(int[] flags) {
        // The full bundle size is one more than the given array length,
        // because exactly one flag is missing.
        int n = flags.length + 1;

        // This variable will store the running XOR result.
        // We start from 0 because 0 is the identity value for XOR:
        // x ^ 0 = x
        int xor = 0;

        // Step 1:
        // XOR all expected flags from the complete bundle.
        //
        // The expected flags are:
        // 2^0, 2^1, 2^2, ..., 2^(n-1)
        // which are:
        // 1, 2, 4, 8, ..., (1 << (n - 1))
        //
        // We generate them one by one using left shift.
        for (int i = 0; i < n; i++) {
            // (1 << i) means 2^i
            // Example:
            // i = 0 -> 1
            // i = 1 -> 2
            // i = 2 -> 4
            // i = 3 -> 8
            int expectedFlag = 1 << i;

            // Add this expected flag into the XOR result.
            xor ^= expectedFlag;
        }

        // Step 2:
        // XOR all flags that are actually present in the input array.
        //
        // Every flag that exists in both:
        // - the expected full set
        // - the given input
        // will cancel out.
        //
        // The only flag that does not get canceled is the missing one.
        for (int flag : flags) {
            xor ^= flag;
        }

        // After all cancellations, xor now holds the missing flag.
        return xor;
    }

    /**
     * A second beginner-friendly method that does the same work,
     * but separates the XOR of expected flags and actual flags into two variables.
     *
     * This is useful for understanding the logic more clearly.
     *
     * @param flags the given array of distinct permission flags, containing all expected powers of two except one
     * @return the missing permission flag
     * Time complexity: O(n), because we scan through the expected values and the input array once
     * Space complexity: O(1), because only constant extra variables are used
     */
    public int findMissingFlagVerbose(int[] flags) {
        // Total number of flags that should exist in the complete bundle.
        int n = flags.length + 1;

        // XOR of all expected flags in the complete bundle.
        int expectedXor = 0;

        // XOR of all flags actually present in the input.
        int actualXor = 0;

        // Build the XOR of the complete expected set:
        // 1, 2, 4, 8, ..., 2^(n-1)
        for (int i = 0; i < n; i++) {
            expectedXor ^= (1 << i);
        }

        // Build the XOR of the given flags.
        for (int flag : flags) {
            actualXor ^= flag;
        }

        // XORing these two results removes all common values,
        // leaving only the missing flag.
        return expectedXor ^ actualXor;
    }

    /**
     * Converts an integer array into a readable string representation.
     *
     * This helper method is used only for printing example inputs in main.
     *
     * @param arr the array to convert to a string
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to the created string content
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input array
     * - the computed missing flag
     * - the expected answer for verification
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for each demonstrated example
     * Space complexity: O(1) extra, excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // Complete set for n = 5 should be [1, 2, 4, 8, 16]
        // Given flags are [1, 2, 8, 16]
        // Therefore, the missing flag is 4.
        int[] flags1 = {1, 2, 8, 16};
        int result1 = solution.findMissingFlag(flags1);
        System.out.println("Example 1:");
        System.out.println("Input: flags = " + solution.arrayToString(flags1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 4");
        System.out.println();

        // Example 2 from the problem statement:
        // Complete set for n = 7 should be [1, 2, 4, 8, 16, 32, 64]
        // Given flags are [2, 4, 8, 16, 32, 1]
        // Therefore, the missing flag is 64.
        int[] flags2 = {2, 4, 8, 16, 32, 1};
        int result2 = solution.findMissingFlag(flags2);
        System.out.println("Example 2:");
        System.out.println("Input: flags = " + solution.arrayToString(flags2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 64");
        System.out.println();

        // Additional small example:
        // Complete set for n = 2 should be [1, 2]
        // If input is [2], then missing is 1.
        int[] flags3 = {2};
        int result3 = solution.findMissingFlag(flags3);
        System.out.println("Additional Example 3:");
        System.out.println("Input: flags = " + solution.arrayToString(flags3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional example:
        // Complete set for n = 4 should be [1, 2, 4, 8]
        // If input is [1, 4, 8], then missing is 2.
        int[] flags4 = {1, 4, 8};
        int result4 = solution.findMissingFlagVerbose(flags4);
        System.out.println("Additional Example 4:");
        System.out.println("Input: flags = " + solution.arrayToString(flags4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 2");
    }
}