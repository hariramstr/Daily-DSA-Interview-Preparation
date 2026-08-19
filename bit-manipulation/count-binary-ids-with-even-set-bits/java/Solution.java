/*
Title: Count Binary IDs With Even Set Bits

Problem Description:
A warehouse system stores item IDs as non-negative integers. For a quick integrity check,
an ID is called valid if its binary representation contains an even number of 1 bits.
Given an integer array ids, return how many IDs are valid.

For example, the number 10 is binary 1010, which contains two 1 bits, so it is valid.
The number 7 is binary 111, which contains three 1 bits, so it is not valid.

Your task is to scan the array and count how many values have even bit parity.
A straightforward solution can examine each number independently and count its set bits
using bit manipulation operations such as shifting or repeatedly clearing the lowest set bit.

Constraints:
- 1 <= ids.length <= 100000
- 0 <= ids[i] <= 10^9
- The answer fits in a 32-bit integer

Example 1:
Input: ids = [0, 1, 2, 3, 4]
Output: 2
Explanation: 0 has 0 set bits (even), 1 has 1, 2 has 1, 3 has 2 (even), and 4 has 1.
So only 0 and 3 are valid.

Example 2:
Input: ids = [5, 6, 7, 8, 15]
Output: 3
Explanation: 5 is 101 (2 set bits), 6 is 110 (2 set bits), 7 is 111 (3 set bits),
8 is 1000 (1 set bit), and 15 is 1111 (4 set bits). The valid IDs are 5, 6, and 15.
*/

import java.util.*;

public class Solution {

    /**
     * Counts how many IDs in the array have an even number of set bits (1s) in binary.
     *
     * We process each number independently:
     * 1. Count how many 1 bits it contains.
     * 2. Check whether that count is even.
     * 3. If yes, include it in the final answer.
     *
     * @param ids the array of non-negative integer IDs to inspect
     * @return the number of IDs whose binary representation contains an even number of 1 bits
     * Time complexity: O(n * b), where n is the number of IDs and b is the number of set bits processed per number;
     * using the lowest-set-bit removal technique, this is O(total number of set bits across all values)
     * Space complexity: O(1), ignoring input storage
     */
    public int countValidIds(int[] ids) {
        // This variable will store the total number of valid IDs found so far.
        int validCount = 0;

        // We examine every ID one by one.
        for (int id : ids) {
            // For each ID, determine whether it has an even number of set bits.
            if (hasEvenSetBits(id)) {
                // If yes, increase the answer.
                validCount++;
            }
        }

        // After scanning the full array, return the final count.
        return validCount;
    }

    /**
     * Determines whether a number contains an even number of set bits (1s) in binary.
     *
     * This method counts set bits using a classic bit manipulation trick:
     * repeatedly apply n = n & (n - 1), which removes the lowest set bit each time.
     *
     * Example:
     * n = 10 (1010)
     * first removal: 1010 & 1001 = 1000
     * second removal: 1000 & 0111 = 0000
     * Two removals means two set bits.
     *
     * @param value the non-negative integer to inspect
     * @return true if the number of set bits is even, false otherwise
     * Time complexity: O(k), where k is the number of set bits in value
     * Space complexity: O(1)
     */
    public boolean hasEvenSetBits(int value) {
        // This variable tracks how many 1 bits we have seen.
        int setBitCount = 0;

        // We make a working copy because we will modify it during the counting process.
        int current = value;

        // Continue until all set bits have been removed.
        while (current != 0) {
            // The expression current & (current - 1) removes the lowest set bit.
            // Every time we do this, we know exactly one 1 bit has been removed.
            current = current & (current - 1);

            // Since one set bit was removed, increment the count.
            setBitCount++;
        }

        // A number is valid if the count of set bits is even.
        return setBitCount % 2 == 0;
    }

    /**
     * Counts the number of set bits (1s) in the binary representation of a non-negative integer.
     *
     * This helper is useful for demonstration and tracing example outputs.
     *
     * @param value the non-negative integer whose set bits should be counted
     * @return the number of 1 bits in the binary representation of value
     * Time complexity: O(k), where k is the number of set bits in value
     * Space complexity: O(1)
     */
    public int countSetBits(int value) {
        // Start with zero set bits counted.
        int count = 0;

        // Use a working variable so the original input remains unchanged.
        int current = value;

        // Remove one set bit at a time until the number becomes zero.
        while (current != 0) {
            current = current & (current - 1);
            count++;
        }

        return count;
    }

    /**
     * Converts an integer array into a readable string form.
     *
     * This is a small utility method used by main to print sample inputs clearly.
     *
     * @param array the integer array to convert
     * @return a string representation such as [1, 2, 3]
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n) for the created string content
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints detailed results.
     *
     * It also traces each value so a beginner can verify correctness manually.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(m), proportional to the total work of the demonstrations
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] ids1 = {0, 1, 2, 3, 4};

        // Sample input 2 from the problem statement.
        int[] ids2 = {5, 6, 7, 8, 15};

        System.out.println("Sample 1 Input: " + solution.arrayToString(ids1));
        for (int id : ids1) {
            int bitCount = solution.countSetBits(id);
            boolean valid = solution.hasEvenSetBits(id);
            System.out.println(
                "ID = " + id +
                ", set bits = " + bitCount +
                ", valid = " + valid
            );
        }
        int result1 = solution.countValidIds(ids1);
        System.out.println("Sample 1 Output: " + result1);
        System.out.println("Expected Output: 2");
        System.out.println();

        System.out.println("Sample 2 Input: " + solution.arrayToString(ids2));
        for (int id : ids2) {
            int bitCount = solution.countSetBits(id);
            boolean valid = solution.hasEvenSetBits(id);
            System.out.println(
                "ID = " + id +
                ", set bits = " + bitCount +
                ", valid = " + valid
            );
        }
        int result2 = solution.countValidIds(ids2);
        System.out.println("Sample 2 Output: " + result2);
        System.out.println("Expected Output: 3");
        System.out.println();

        // Additional quick check to show the method can be reused.
        int[] extra = {10, 7};
        System.out.println("Extra Demo Input: " + solution.arrayToString(extra));
        for (int id : extra) {
            int bitCount = solution.countSetBits(id);
            boolean valid = solution.hasEvenSetBits(id);
            System.out.println(
                "ID = " + id +
                ", set bits = " + bitCount +
                ", valid = " + valid
            );
        }
        System.out.println("Extra Demo Output: " + solution.countValidIds(extra));
    }
}