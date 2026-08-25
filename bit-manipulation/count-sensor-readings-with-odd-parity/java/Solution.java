import java.util.*;

/*
 * Title: Count Sensor Readings With Odd Parity
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an integer array readings, where each value represents a compact binary
 * reading produced by a sensor. A reading is considered odd-parity if its binary
 * representation contains an odd number of 1 bits. Your task is to return how many
 * readings in the array are odd-parity.
 *
 * For example, the number 5 has binary form 101, which contains two 1 bits, so it is
 * not odd-parity. The number 7 has binary form 111, which contains three 1 bits, so it
 * is odd-parity.
 *
 * Write a function that counts how many numbers in the array satisfy this condition.
 * The expected solution should use bit manipulation rather than converting numbers to
 * strings.
 *
 * Constraints:
 * - 1 <= readings.length <= 100000
 * - 0 <= readings[i] <= 10^9
 * - An O(n * number_of_bits) solution is acceptable
 *
 * Example 1:
 * Input: readings = [1, 2, 3, 4]
 * Output: 3
 * Explanation:
 * - 1 -> 1 has 1 set bit, odd
 * - 2 -> 10 has 1 set bit, odd
 * - 3 -> 11 has 2 set bits, even
 * - 4 -> 100 has 1 set bit, odd
 * So the answer is 3.
 *
 * Example 2:
 * Input: readings = [0, 5, 7, 8, 10]
 * Output: 2
 * Explanation:
 * - 0 -> 0 set bits, even
 * - 5 -> 101 has 2 set bits, even
 * - 7 -> 111 has 3 set bits, odd
 * - 8 -> 1000 has 1 set bit, odd
 * - 10 -> 1010 has 2 set bits, even
 * There are 2 odd-parity readings in total.
 */

public class Solution {

    /**
     * Counts how many sensor readings have odd parity, meaning their binary representation
     * contains an odd number of set bits (1s).
     *
     * We process each number independently and use bit manipulation to determine whether
     * the count of 1 bits is odd.
     *
     * @param readings the array of non-negative sensor readings
     * @return the number of readings whose binary representation contains an odd number of 1 bits
     * Time complexity: O(n * b), where n is the number of readings and b is the number of bits processed
     * Space complexity: O(1), excluding input storage
     */
    public int countOddParityReadings(int[] readings) {
        // This variable will store the final answer:
        // how many numbers in the array have an odd number of set bits.
        int oddParityCount = 0;

        // We examine every reading one by one.
        for (int reading : readings) {
            // For each reading, determine whether it has odd parity.
            // If yes, increase the answer.
            if (hasOddParity(reading)) {
                oddParityCount++;
            }
        }

        // After checking all readings, return the total count.
        return oddParityCount;
    }

    /**
     * Determines whether a single integer has odd parity.
     *
     * This method counts the number of set bits using bit manipulation.
     * Instead of converting the number to a binary string, we repeatedly inspect
     * the least significant bit and then shift the number to the right.
     *
     * Example:
     * 7 in binary is 111
     * - least significant bit = 1
     * - shift right -> 11
     * - least significant bit = 1
     * - shift right -> 1
     * - least significant bit = 1
     * Total set bits = 3, which is odd
     *
     * @param number the non-negative integer to inspect
     * @return true if the number contains an odd number of 1 bits, otherwise false
     * Time complexity: O(b), where b is the number of bits in the number
     * Space complexity: O(1)
     */
    public boolean hasOddParity(int number) {
        // This variable stores how many 1 bits we have seen so far.
        int setBitCount = 0;

        // We use a copy so the original input value remains unchanged conceptually.
        int current = number;

        // Continue until all bits have been processed.
        // When current becomes 0, there are no more 1 bits left to inspect.
        while (current > 0) {
            // current & 1 extracts the least significant bit.
            //
            // If current ends with binary ...1, then (current & 1) is 1.
            // If current ends with binary ...0, then (current & 1) is 0.
            //
            // We add that result directly to setBitCount.
            setBitCount += (current & 1);

            // Shift all bits one position to the right.
            // This discards the bit we just processed and moves the next bit
            // into the least significant position for the next loop iteration.
            current >>= 1;
        }

        // A number has odd parity if the count of set bits is odd.
        // We can test odd/even using modulo 2.
        return setBitCount % 2 == 1;
    }

    /**
     * Alternative helper method that counts set bits using Brian Kernighan's algorithm.
     *
     * This is a classic bit manipulation technique:
     * number & (number - 1) removes the lowest set bit from the number.
     *
     * Example for 10:
     * 10 = 1010
     *  9 = 1001
     * 10 & 9 = 1000  (removed one set bit)
     *
     * Repeating this process counts only the set bits, which can be efficient.
     *
     * @param number the non-negative integer whose set bits should be counted
     * @return the number of 1 bits in the binary representation of the number
     * Time complexity: O(k), where k is the number of set bits
     * Space complexity: O(1)
     */
    public int countSetBits(int number) {
        int count = 0;
        int current = number;

        while (current > 0) {
            // Remove the lowest set bit.
            current = current & (current - 1);

            // Since one set bit was removed, increment the count.
            count++;
        }

        return count;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * This main method also verifies the expected outputs described in the examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations shown here
     * Space complexity: O(1), excluding the sample arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] readings1 = {1, 2, 3, 4};
        int result1 = solution.countOddParityReadings(readings1);

        System.out.println("Sample 1 readings: " + Arrays.toString(readings1));
        System.out.println("Expected: 3");
        System.out.println("Actual: " + result1);
        System.out.println();

        // Sample input 2 from the problem statement.
        // Important correctness check:
        // 0  -> 0 set bits -> even
        // 5  -> 2 set bits -> even
        // 7  -> 3 set bits -> odd
        // 8  -> 1 set bit  -> odd
        // 10 -> 2 set bits -> even
        // Total odd-parity readings = 2
        int[] readings2 = {0, 5, 7, 8, 10};
        int result2 = solution.countOddParityReadings(readings2);

        System.out.println("Sample 2 readings: " + Arrays.toString(readings2));
        System.out.println("Expected: 2");
        System.out.println("Actual: " + result2);
        System.out.println();

        // Additional small demonstration to help beginners understand behavior.
        int[] readings3 = {0, 1, 5, 6, 7};
        int result3 = solution.countOddParityReadings(readings3);

        System.out.println("Additional demo readings: " + Arrays.toString(readings3));
        System.out.println("Actual: " + result3);
        System.out.println();

        // Step-by-step parity checks for a few individual numbers.
        int[] singleChecks = {0, 1, 5, 7, 8, 10};
        for (int value : singleChecks) {
            System.out.println(
                "Value: " + value +
                ", set bits: " + solution.countSetBits(value) +
                ", odd parity: " + solution.hasOddParity(value)
            );
        }
    }
}