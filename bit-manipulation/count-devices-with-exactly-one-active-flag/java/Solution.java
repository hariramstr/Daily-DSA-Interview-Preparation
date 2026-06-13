import java.util.*;

/*
 * Title: Count Devices with Exactly One Active Flag
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an array states where each integer represents the status flags of a device.
 * In the binary representation of states[i], each bit indicates whether a particular feature
 * is enabled (1) or disabled (0). A device is considered "simple-active" if it has exactly
 * one enabled feature, meaning its binary form contains exactly one set bit.
 *
 * Your task is to return how many devices in the array are simple-active.
 *
 * For example, the values 1 (0001), 2 (0010), 4 (0100), and 8 (1000) are simple-active
 * because each has exactly one bit set. The values 0, 3, 6, and 10 are not, because they
 * have zero or more than one set bit.
 *
 * This problem is expected to be solved efficiently using bit manipulation rather than
 * converting numbers to strings. A common observation is that a positive number has exactly
 * one set bit if and only if x & (x - 1) == 0.
 *
 * Constraints:
 * - 1 <= states.length <= 100000
 * - 0 <= states[i] <= 10^9
 * - Return the total count of values that have exactly one set bit.
 *
 * Example 1:
 * Input: states = [1, 2, 3, 4, 6]
 * Output: 3
 * Explanation: 1, 2, and 4 each contain exactly one set bit. 3 and 6 do not.
 *
 * Example 2:
 * Input: states = [0, 7, 8, 16, 18]
 * Output: 2
 * Explanation: 8 and 16 are simple-active. 0 has no set bits, while 7 and 18 have more than one.
 */

public class Solution {

    /**
     * Counts how many numbers in the given array have exactly one set bit in binary.
     *
     * The key bit manipulation rule used here is:
     * A positive integer x has exactly one set bit if and only if:
     * (x & (x - 1)) == 0
     *
     * Why this works:
     * - If x is a power of two, its binary form looks like 1000...000
     * - Then x - 1 becomes 0111...111
     * - Performing AND between them gives 0
     *
     * Examples:
     * - 1  -> 0001, one set bit, valid
     * - 2  -> 0010, one set bit, valid
     * - 4  -> 0100, one set bit, valid
     * - 3  -> 0011, two set bits, invalid
     * - 0  -> 0000, zero set bits, invalid
     *
     * @param states the array of device states where each integer represents enabled/disabled flags
     * @return the number of values in the array that contain exactly one set bit
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), excluding input storage
     */
    public int countSimpleActiveDevices(int[] states) {
        // This variable will store the final answer:
        // how many values in the array are "simple-active".
        int count = 0;

        // We examine every number one by one.
        for (int state : states) {
            // For each number, we check whether it has exactly one set bit.
            // If yes, we increase our answer.
            if (hasExactlyOneSetBit(state)) {
                count++;
            }
        }

        // After processing the full array, return the total count.
        return count;
    }

    /**
     * Checks whether a given non-negative integer has exactly one set bit.
     *
     * Detailed reasoning:
     * 1. The number must be positive.
     *    - 0 is not valid because it has no set bits.
     * 2. For a positive number with exactly one set bit:
     *    - Example: 8 = 1000
     *    - 8 - 1 = 7 = 0111
     *    - 1000 & 0111 = 0000
     * 3. If a number has more than one set bit:
     *    - Example: 10 = 1010
     *    - 10 - 1 = 9 = 1001
     *    - 1010 & 1001 = 1000, which is not 0
     *
     * @param x the number to test
     * @return true if x has exactly one set bit; otherwise false
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean hasExactlyOneSetBit(int x) {
        // Step 1:
        // Reject 0 immediately.
        // 0 in binary is 0000, which contains zero set bits, not one.
        if (x <= 0) {
            return false;
        }

        // Step 2:
        // Use the classic bit trick:
        // If x has exactly one set bit, then x & (x - 1) becomes 0.
        //
        // Example with x = 4:
        // x     = 0100
        // x - 1 = 0011
        // AND   = 0000  -> valid
        //
        // Example with x = 6:
        // x     = 0110
        // x - 1 = 0101
        // AND   = 0100  -> not valid
        return (x & (x - 1)) == 0;
    }

    /**
     * Runs a demonstration of the solution using the sample inputs from the problem statement.
     *
     * This method prints:
     * - The input arrays
     * - The computed result
     * - The expected result
     *
     * It also includes a few extra checks to help beginners understand the behavior.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) across the demonstrated examples
     * Space complexity: O(1), excluding the sample arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement
        int[] states1 = {1, 2, 3, 4, 6};
        int result1 = solution.countSimpleActiveDevices(states1);
        System.out.println("Example 1 Input:  " + Arrays.toString(states1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected Output:  3");
        System.out.println();

        // Example 2 from the problem statement
        int[] states2 = {0, 7, 8, 16, 18};
        int result2 = solution.countSimpleActiveDevices(states2);
        System.out.println("Example 2 Input:  " + Arrays.toString(states2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected Output:  2");
        System.out.println();

        // Additional demonstration:
        // Values that should be true: 1, 2, 4, 8, 16
        // Values that should be false: 0, 3, 6, 10
        int[] extra = {1, 2, 4, 8, 16, 0, 3, 6, 10};
        System.out.println("Extra Input:      " + Arrays.toString(extra));
        System.out.println("Extra Output:     " + solution.countSimpleActiveDevices(extra));
        System.out.println("Expected Output:  5");
        System.out.println();

        // Individual checks for beginner-friendly understanding
        int[] checks = {0, 1, 2, 3, 4, 6, 8, 10, 16, 18};
        System.out.println("Individual checks:");
        for (int value : checks) {
            System.out.println("Value " + value + " -> hasExactlyOneSetBit = " + solution.hasExactlyOneSetBit(value));
        }
    }
}