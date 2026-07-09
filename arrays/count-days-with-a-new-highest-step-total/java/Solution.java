import java.util.*;

/*
 * Title: Count Days with a New Highest Step Total
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array steps where steps[i] represents the number of steps
 * a user walked on day i. A day is called a record day if its step count is strictly
 * greater than every previous day's step count. The first day is always considered
 * a record day because there are no earlier days to compare against.
 *
 * Your task is to return the total number of record days in the array.
 *
 * This problem tests your ability to scan an array while maintaining running state.
 * A correct solution should track the highest step count seen so far and count how
 * many times a new maximum appears.
 *
 * Constraints:
 * - 1 <= steps.length <= 100000
 * - 0 <= steps[i] <= 1000000
 *
 * Example 1:
 * Input: steps = [3000, 4500, 4200, 5000, 5000, 6200]
 * Output: 4
 * Explanation:
 * Record days are:
 * - day 0 -> 3000
 * - day 1 -> 4500
 * - day 3 -> 5000
 * - day 5 -> 6200
 * The second 5000 is not a record day because it is equal to the current maximum,
 * not greater.
 *
 * Example 2:
 * Input: steps = [8000, 7000, 7000, 6500]
 * Output: 1
 * Explanation:
 * Only the first day is a record day. No later day exceeds the highest value seen so far.
 *
 * Return the number of days that set a new personal best step total.
 */

public class Solution {

    /**
     * Counts how many days are record days.
     *
     * A record day is a day whose step total is strictly greater than every earlier day's
     * step total. The first day is always a record day because there are no previous days.
     *
     * @param steps an array where steps[i] is the number of steps walked on day i
     * @return the total number of record days in the array
     *
     * Time complexity: O(n), because we scan the array exactly once.
     * Space complexity: O(1), because we use only a few extra variables.
     */
    public int countRecordDays(int[] steps) {
        // Since the problem guarantees at least one element, the first day always exists.
        // We will still add a defensive check to make the method safer for general use.
        if (steps == null || steps.length == 0) {
            return 0;
        }

        // This variable stores how many record days we have found so far.
        int recordDays = 0;

        // This variable stores the highest step count seen up to the current point.
        // We start with the smallest possible integer so that the first day will
        // definitely be greater than it and therefore counted as a record day.
        int maxSoFar = Integer.MIN_VALUE;

        // Go through each day one by one from left to right.
        for (int i = 0; i < steps.length; i++) {
            // Read the current day's step count.
            int currentSteps = steps[i];

            // A day is a record day only if its step count is STRICTLY greater
            // than every previous day's step count.
            //
            // That means:
            // - if currentSteps > maxSoFar, this day sets a new record
            // - if currentSteps == maxSoFar, it does NOT count
            // - if currentSteps < maxSoFar, it does NOT count
            if (currentSteps > maxSoFar) {
                // We found a new record day, so increase the count.
                recordDays++;

                // Update the running maximum so future days compare against
                // this new highest value.
                maxSoFar = currentSteps;
            }

            // If currentSteps is not greater than maxSoFar, we do nothing.
            // The existing maximum remains unchanged.
        }

        // After scanning all days, return the total number of record days found.
        return recordDays;
    }

    /**
     * Helper method to print an array in a readable format and show the computed answer.
     *
     * @param steps the input array of daily step totals
     * @return the computed number of record days for the given input
     *
     * Time complexity: O(n), because it calls the main counting method which scans the array once.
     * Space complexity: O(1), ignoring the temporary memory used internally for printing.
     */
    public int demonstrate(int[] steps) {
        int result = countRecordDays(steps);
        System.out.println("steps = " + Arrays.toString(steps));
        System.out.println("record days = " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * This main method verifies the examples from the prompt:
     * - [3000, 4500, 4200, 5000, 5000, 6200] -> 4
     * - [8000, 7000, 7000, 6500] -> 1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(1), excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement.
        // Trace:
        // Day 0: 3000 -> first day, record
        // Day 1: 4500 -> greater than 3000, record
        // Day 2: 4200 -> less than 4500, not a record
        // Day 3: 5000 -> greater than 4500, record
        // Day 4: 5000 -> equal to 5000, not a record
        // Day 5: 6200 -> greater than 5000, record
        // Total = 4
        int[] steps1 = {3000, 4500, 4200, 5000, 5000, 6200};
        int result1 = solution.demonstrate(steps1);
        System.out.println("Expected: 4, Actual: " + result1);
        System.out.println();

        // Example 2 from the problem statement.
        // Trace:
        // Day 0: 8000 -> first day, record
        // Day 1: 7000 -> less than 8000, not a record
        // Day 2: 7000 -> equal/lower than max 8000, not a record
        // Day 3: 6500 -> less than 8000, not a record
        // Total = 1
        int[] steps2 = {8000, 7000, 7000, 6500};
        int result2 = solution.demonstrate(steps2);
        System.out.println("Expected: 1, Actual: " + result2);
        System.out.println();

        // Additional beginner-friendly test:
        // Every day is a new record.
        int[] steps3 = {1000, 2000, 3000, 4000};
        int result3 = solution.demonstrate(steps3);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println();

        // Additional test:
        // All days have the same value, so only the first day counts.
        int[] steps4 = {5000, 5000, 5000, 5000};
        int result4 = solution.demonstrate(steps4);
        System.out.println("Expected: 1, Actual: " + result4);
    }
}