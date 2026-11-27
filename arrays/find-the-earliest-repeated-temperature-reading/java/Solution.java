import java.util.*;

/*
 * Title: Find the Earliest Repeated Temperature Reading
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * A weather station stores hourly temperature readings in an integer array temps,
 * where temps[i] is the recorded temperature at hour i. Your task is to find the
 * first temperature value that appears more than once when scanning the array from
 * left to right. Return the temperature whose second occurrence happens earliest.
 * If no temperature is repeated, return -1.
 *
 * This problem is about identifying the earliest duplicate by arrival order, not
 * the smallest repeated value and not the value with the highest frequency. In
 * other words, as you read the array from index 0 onward, the moment you encounter
 * a value that has already appeared before, that value is the answer.
 *
 * Example:
 * In [18, 21, 19, 21, 18], the answer is 21 because its second appearance happens
 * at index 3, before 18 repeats at index 4.
 *
 * Constraints:
 * - 1 <= temps.length <= 100000
 * - -1000000 <= temps[i] <= 1000000
 * - The solution should run efficiently for large arrays.
 */

public class Solution {

    /**
     * Finds the earliest repeated temperature reading.
     *
     * The idea is simple:
     * 1. Scan the array from left to right.
     * 2. Keep track of every temperature already seen.
     * 3. The first time we encounter a temperature that is already in the set,
     *    that temperature is the answer because its second occurrence happened
     *    earlier than any later duplicate we may find.
     *
     * Example:
     * temps = [18, 21, 19, 21, 18]
     * - See 18 -> not seen before, store it
     * - See 21 -> not seen before, store it
     * - See 19 -> not seen before, store it
     * - See 21 -> already seen, return 21 immediately
     *
     * @param temps the array of hourly temperature readings
     * @return the first temperature whose second occurrence appears earliest while scanning
     *         from left to right; returns -1 if no temperature is repeated
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n) in the worst case for the hash set
     */
    public int findEarliestRepeatedTemperature(int[] temps) {
        // A HashSet stores temperatures we have already encountered.
        // Why a HashSet?
        // - add(...) is fast on average
        // - contains(...) is fast on average
        // This allows us to process large arrays efficiently.
        Set<Integer> seenTemperatures = new HashSet<>();

        // We scan from left to right because the problem asks for the duplicate
        // whose second appearance happens earliest in arrival order.
        for (int i = 0; i < temps.length; i++) {
            // Read the current temperature at index i.
            int currentTemperature = temps[i];

            // Step 1:
            // Check whether this temperature has already appeared earlier.
            // If yes, then this is the first duplicate we have reached while
            // scanning from left to right, so it must be the correct answer.
            if (seenTemperatures.contains(currentTemperature)) {
                return currentTemperature;
            }

            // Step 2:
            // If it has not appeared before, record it as seen so that if we
            // encounter it again later, we can detect the repetition.
            seenTemperatures.add(currentTemperature);
        }

        // If we finish the entire scan without finding any repeated value,
        // then no temperature appears more than once.
        return -1;
    }

    /**
     * Helper method to convert an int array into a readable string.
     *
     * This is used only for demonstration in main so the printed output is
     * beginner-friendly and easy to understand.
     *
     * @param array the integer array to convert into a string
     * @return a string representation of the array, such as [1, 2, 3]
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n) for the string construction
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * This method also includes a few extra test cases to show the behavior clearly.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k + total input size across demo arrays)
     * Space complexity: O(n) for the largest demo array processed by the algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1:
        // temps = [18, 21, 19, 21, 18]
        // Expected output: 21
        // Trace:
        // - 18 seen first time
        // - 21 seen first time
        // - 19 seen first time
        // - 21 seen again -> first duplicate encountered while scanning
        int[] temps1 = {18, 21, 19, 21, 18};
        System.out.println("Input: temps = " + solution.arrayToString(temps1));
        System.out.println("Output: " + solution.findEarliestRepeatedTemperature(temps1));
        System.out.println("Expected: 21");
        System.out.println();

        // Sample Input 2:
        // temps = [5, 7, 8, 9]
        // Expected output: -1
        // Trace:
        // - 5 first time
        // - 7 first time
        // - 8 first time
        // - 9 first time
        // No duplicates found.
        int[] temps2 = {5, 7, 8, 9};
        System.out.println("Input: temps = " + solution.arrayToString(temps2));
        System.out.println("Output: " + solution.findEarliestRepeatedTemperature(temps2));
        System.out.println("Expected: -1");
        System.out.println();

        // Extra test case:
        // The earliest repeated value is determined by the earliest second occurrence,
        // not by the smallest value.
        // temps = [4, 2, 4, 2]
        // 4 repeats at index 2, 2 repeats at index 3 -> answer is 4
        int[] temps3 = {4, 2, 4, 2};
        System.out.println("Input: temps = " + solution.arrayToString(temps3));
        System.out.println("Output: " + solution.findEarliestRepeatedTemperature(temps3));
        System.out.println("Expected: 4");
        System.out.println();

        // Extra test case with negative values:
        int[] temps4 = {-3, -1, -2, -3};
        System.out.println("Input: temps = " + solution.arrayToString(temps4));
        System.out.println("Output: " + solution.findEarliestRepeatedTemperature(temps4));
        System.out.println("Expected: -3");
        System.out.println();

        // Extra test case with immediate repetition:
        int[] temps5 = {10, 10, 12};
        System.out.println("Input: temps = " + solution.arrayToString(temps5));
        System.out.println("Output: " + solution.findEarliestRepeatedTemperature(temps5));
        System.out.println("Expected: 10");
    }
}