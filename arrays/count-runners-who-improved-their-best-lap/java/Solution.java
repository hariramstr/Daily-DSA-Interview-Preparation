import java.util.*;

/*
Problem Title: Count Runners Who Improved Their Best Lap

Problem Description:
You are given an integer array laps where laps[i] represents the lap time recorded by a runner on day i.
A smaller lap time is better. A runner is said to have improved their best lap on day i if laps[i] is
strictly smaller than every lap time that appeared before it. The first day does not count as an
improvement, because there is no earlier lap to compare against.

Return the number of days on which the runner improved their best lap.

This problem is about scanning an array from left to right while tracking the smallest value seen so far.
Each time you encounter a new value that is smaller than that running minimum, it counts as a new
improvement. Equal values do not count, because the lap must be strictly better than all previous laps.

Constraints:
- 1 <= laps.length <= 100000
- 1 <= laps[i] <= 1000000000

Example 1:
Input: laps = [72, 70, 71, 69, 69, 68]
Output: 3
Explanation: Improvements happen on day 1 with 70, day 3 with 69, and day 5 with 68.
The first value 72 is the initial best but does not count.

Example 2:
Input: laps = [55, 55, 55, 54, 53]
Output: 2
Explanation: Day 3 with 54 improves over all previous values, and day 4 with 53 improves again.
The repeated 55 values do not count as improvements.
*/

public class Solution {

    /**
     * Counts how many days the runner recorded a lap time that is strictly better
     * than every lap time seen on earlier days.
     *
     * The first day never counts as an improvement because there is no previous day
     * to compare against.
     *
     * @param laps the array of lap times, where a smaller value means a better lap
     * @return the number of days on which the runner improved their best lap
     *
     * Time complexity: O(n), where n is the length of the laps array, because we scan the array once.
     * Space complexity: O(1), because we use only a few extra variables.
     */
    public int countImprovedBestLaps(int[] laps) {
        // Defensive handling:
        // The problem guarantees at least one element, but this check makes the method
        // safer and easier for beginners to reuse in other contexts.
        if (laps == null || laps.length == 0) {
            return 0;
        }

        // This variable stores the smallest lap time we have seen so far.
        // We start with the first day's lap time because that is the only value
        // known at the beginning of the scan.
        int bestSoFar = laps[0];

        // This variable counts how many times we find a strictly smaller lap time
        // than every previous lap time.
        int improvements = 0;

        // Start from index 1, not index 0.
        // Why?
        // - Day 0 is the first recorded lap.
        // - The problem explicitly says the first day does NOT count as an improvement.
        for (int i = 1; i < laps.length; i++) {
            // Read the current day's lap time.
            int currentLap = laps[i];

            // Check whether today's lap is strictly smaller than the best lap seen before today.
            // "Strictly smaller" is important:
            // - If currentLap == bestSoFar, it is NOT an improvement.
            // - If currentLap > bestSoFar, it is also NOT an improvement.
            // - Only currentLap < bestSoFar counts.
            if (currentLap < bestSoFar) {
                // We found a new best lap time, so this day counts as an improvement.
                improvements++;

                // Update the running minimum so future days compare against this new best value.
                bestSoFar = currentLap;
            }

            // If currentLap is not smaller, we do nothing:
            // - the count does not change
            // - bestSoFar stays the same
        }

        // After scanning all days, return the total number of improvements found.
        return improvements;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param laps the array of lap times to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the string created for display.
     */
    public String arrayToString(int[] laps) {
        return Arrays.toString(laps);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call, based on the counting method.
     * Space complexity: O(1) extra space for the algorithm itself.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1
        int[] laps1 = {72, 70, 71, 69, 69, 68};
        int result1 = solution.countImprovedBestLaps(laps1);
        System.out.println("Input: laps = " + solution.arrayToString(laps1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 3");
        System.out.println();

        // Sample input 2
        int[] laps2 = {55, 55, 55, 54, 53};
        int result2 = solution.countImprovedBestLaps(laps2);
        System.out.println("Input: laps = " + solution.arrayToString(laps2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 2");
        System.out.println();

        // Additional quick checks for clarity

        // No improvements after the first day
        int[] laps3 = {60, 61, 62, 63};
        int result3 = solution.countImprovedBestLaps(laps3);
        System.out.println("Input: laps = " + solution.arrayToString(laps3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Every later day improves
        int[] laps4 = {100, 90, 80, 70, 60};
        int result4 = solution.countImprovedBestLaps(laps4);
        System.out.println("Input: laps = " + solution.arrayToString(laps4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 4");
    }
}