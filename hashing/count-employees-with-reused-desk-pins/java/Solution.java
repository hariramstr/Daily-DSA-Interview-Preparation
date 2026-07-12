import java.util.*;

/*
 * Title: Count Employees With Reused Desk PINs
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * A company assigns each employee a temporary desk PIN for one day. You are given
 * an array pins where pins[i] is the PIN used by the i-th employee in the check-in list.
 * Some PINs may appear more than once because employees accidentally reused an existing
 * temporary PIN instead of generating a new one.
 *
 * Your task is to return the number of employees whose PIN is not unique in the list.
 * In other words, count how many positions belong to a PIN value that appears at least twice.
 *
 * For example, if the PIN list is [4312, 9981, 4312, 7777, 9981], then PIN 4312 is used
 * by 2 employees and PIN 9981 is used by 2 employees, so the answer is 4. The employee
 * with PIN 7777 is not counted because that PIN appears only once.
 *
 * This problem should be solved efficiently using hashing. A straightforward approach is
 * to count the frequency of each PIN, then sum the frequencies of all PINs that occur
 * more than once.
 *
 * Constraints:
 * - 1 <= pins.length <= 100000
 * - 0 <= pins[i] <= 1000000000
 * - The answer fits in a 32-bit integer.
 *
 * Example 1:
 * Input: pins = [4312, 9981, 4312, 7777, 9981]
 * Output: 4
 * Explanation: Two employees used 4312 and two employees used 9981, so 4 employees are counted.
 *
 * Example 2:
 * Input: pins = [12, 34, 56, 78]
 * Output: 0
 * Explanation: Every PIN appears exactly once, so no employee reused a PIN.
 *
 * Return the total number of employees whose desk PIN appears more than once anywhere in the array.
 */

public class Solution {

    /**
     * Counts how many employees have a PIN value that appears more than once in the array.
     *
     * The idea is:
     * 1. Count how many times each PIN appears using a HashMap.
     * 2. Go through those frequencies.
     * 3. If a frequency is greater than 1, add that full frequency to the answer,
     *    because every employee using that repeated PIN must be counted.
     *
     * Example:
     * pins = [4312, 9981, 4312, 7777, 9981]
     * Frequency map becomes:
     * 4312 -> 2
     * 9981 -> 2
     * 7777 -> 1
     * We add 2 + 2, and ignore 1, so answer = 4.
     *
     * @param pins the array of employee desk PINs, where each element represents one employee's PIN
     * @return the total number of employees whose PIN appears at least twice in the array
     * Time complexity: O(n), where n is the number of employees/PINs in the array
     * Space complexity: O(n), in the worst case when all PINs are distinct
     */
    public int countEmployeesWithReusedPins(int[] pins) {
        // Create a HashMap to store frequency counts.
        // Key   = PIN value
        // Value = number of times that PIN appears in the array
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        // Step 1: Build the frequency map.
        // We examine each PIN one by one.
        for (int pin : pins) {
            // If the PIN is already in the map, increase its count by 1.
            // If it is not in the map yet, start its count at 0 and then add 1.
            frequencyMap.put(pin, frequencyMap.getOrDefault(pin, 0) + 1);
        }

        // Step 2: Sum the frequencies of only those PINs that appear more than once.
        int reusedEmployeeCount = 0;

        // We only need the frequency values here.
        for (int count : frequencyMap.values()) {
            // If a PIN appears at least twice, then every employee using that PIN
            // should be counted in the final answer.
            if (count > 1) {
                reusedEmployeeCount += count;
            }
        }

        // Step 3: Return the computed total.
        return reusedEmployeeCount;
    }

    /**
     * A second beginner-friendly implementation that performs the same task.
     * This version uses two passes over the data:
     * 1. First pass builds the frequency map.
     * 2. Second pass checks each original array position and counts it if its PIN frequency is > 1.
     *
     * This method is also correct and easy to understand because it directly answers:
     * "For this employee, is their PIN repeated somewhere else?"
     *
     * @param pins the array of employee desk PINs
     * @return the number of employees whose PIN is not unique
     * Time complexity: O(n), where n is the number of elements in pins
     * Space complexity: O(n), due to the HashMap storing frequencies
     */
    public int countEmployeesWithReusedPinsByCheckingEachEmployee(int[] pins) {
        // Frequency map for all PINs.
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        // First pass: count occurrences of each PIN.
        for (int pin : pins) {
            frequencyMap.put(pin, frequencyMap.getOrDefault(pin, 0) + 1);
        }

        // Second pass: for each employee's PIN, check whether it appears more than once.
        int result = 0;
        for (int pin : pins) {
            if (frequencyMap.get(pin) > 1) {
                result++;
            }
        }

        return result;
    }

    /**
     * Converts an int array to a readable string for printing.
     *
     * @param array the input integer array
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to the generated string content
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases shown here
     * Space complexity: O(1) excluding internal method calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1 from the problem statement
        int[] pins1 = {4312, 9981, 4312, 7777, 9981};
        int result1 = solution.countEmployeesWithReusedPins(pins1);
        System.out.println("Input:  " + solution.arrayToString(pins1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 4");
        System.out.println();

        // Sample Input 2 from the problem statement
        int[] pins2 = {12, 34, 56, 78};
        int result2 = solution.countEmployeesWithReusedPins(pins2);
        System.out.println("Input:  " + solution.arrayToString(pins2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional example: all employees use the same PIN
        int[] pins3 = {1111, 1111, 1111};
        int result3 = solution.countEmployeesWithReusedPins(pins3);
        System.out.println("Input:  " + solution.arrayToString(pins3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional example: one repeated group and one unique PIN
        int[] pins4 = {5, 9, 5, 10};
        int result4 = solution.countEmployeesWithReusedPins(pins4);
        System.out.println("Input:  " + solution.arrayToString(pins4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 2");
        System.out.println();

        // Verify the alternate implementation gives the same result for the first sample.
        int alternateResult1 = solution.countEmployeesWithReusedPinsByCheckingEachEmployee(pins1);
        System.out.println("Alternate method output for first sample: " + alternateResult1);
        System.out.println("Expected: 4");
    }
}