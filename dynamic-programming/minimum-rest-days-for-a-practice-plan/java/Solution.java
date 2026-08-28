import java.util.*;

/*
 * Title: Minimum Rest Days for a Practice Plan
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are planning a sequence of daily activities for a student preparing for a competition.
 * For each day, the student may have access to coding practice, reading practice, both, or neither.
 * The student wants to stay productive, but cannot do the same type of practice on two consecutive
 * days because it becomes ineffective. On any day, the student may also choose to rest.
 *
 * You are given an integer array activities where activities[i] describes what is available on day i:
 * - 0: neither coding nor reading is available, so the student must rest
 * - 1: only coding is available
 * - 2: only reading is available
 * - 3: both coding and reading are available
 *
 * Return the minimum number of rest days needed over the entire schedule.
 *
 * A valid plan must follow these rules:
 * - The student can do coding only if coding is available that day.
 * - The student can do reading only if reading is available that day.
 * - The student cannot do coding on two consecutive days.
 * - The student cannot do reading on two consecutive days.
 * - Resting is always allowed.
 *
 * Constraints:
 * - 1 <= activities.length <= 100
 * - 0 <= activities[i] <= 3
 *
 * Example 1:
 * Input: activities = [1,3,2,0,3]
 * Output: 2
 * Explanation: One optimal plan is coding, reading, rest, rest, coding.
 * There are 2 rest days, and no activity type is repeated on consecutive days.
 *
 * Example 2:
 * Input: activities = [3,3,3]
 * Output: 1
 * Explanation: The student can do coding on day 1, reading on day 2, and must rest or choose
 * a non-repeating valid option on day 3. The best possible answer is 1 rest day.
 *
 * Dynamic Programming Idea:
 * For each day, we track the minimum rest days so far under three possible states:
 * - state 0: rested today
 * - state 1: did coding today
 * - state 2: did reading today
 *
 * The transition depends only on:
 * - what is available today
 * - what was done yesterday
 *
 * This works because the only restriction involving previous days is:
 * "do not repeat the same activity on consecutive days."
 */
public class Solution {

    /**
     * Computes the minimum number of rest days needed for the entire schedule.
     *
     * We use dynamic programming where:
     * dp[i][0] = minimum rest days after day i if day i is a rest day
     * dp[i][1] = minimum rest days after day i if day i is a coding day
     * dp[i][2] = minimum rest days after day i if day i is a reading day
     *
     * Transition rules:
     * 1. Rest is always allowed, so:
     *    dp[i][0] = 1 + min(dp[i-1][0], dp[i-1][1], dp[i-1][2])
     *
     * 2. Coding is allowed only if coding is available today, and yesterday was not coding:
     *    dp[i][1] = min(dp[i-1][0], dp[i-1][2])
     *
     * 3. Reading is allowed only if reading is available today, and yesterday was not reading:
     *    dp[i][2] = min(dp[i-1][0], dp[i-1][1])
     *
     * The answer is the minimum among the three states on the last day.
     *
     * @param activities an array where each value describes what activities are available on that day:
     *                   0 = neither, 1 = coding only, 2 = reading only, 3 = both
     * @return the minimum number of rest days needed to complete the schedule validly
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(n), due to the DP table of size n x 3
     */
    public int minRestDays(int[] activities) {
        int n = activities.length;

        // A large number used as "infinity" for impossible states.
        // Since n <= 100, using 1_000_000 is more than enough.
        int INF = 1_000_000;

        // dp[i][state]:
        // state 0 -> rested on day i
        // state 1 -> did coding on day i
        // state 2 -> did reading on day i
        int[][] dp = new int[n][3];

        // Initialize all states as impossible first.
        for (int i = 0; i < n; i++) {
            Arrays.fill(dp[i], INF);
        }

        // -----------------------------
        // Base case: day 0
        // -----------------------------

        // Resting is always possible on day 0, costing 1 rest day.
        dp[0][0] = 1;

        // If coding is available on day 0, we can do coding with 0 rest days.
        // activities[0] == 1 means coding only
        // activities[0] == 3 means both are available
        if (activities[0] == 1 || activities[0] == 3) {
            dp[0][1] = 0;
        }

        // If reading is available on day 0, we can do reading with 0 rest days.
        // activities[0] == 2 means reading only
        // activities[0] == 3 means both are available
        if (activities[0] == 2 || activities[0] == 3) {
            dp[0][2] = 0;
        }

        // -----------------------------
        // Fill DP table for days 1..n-1
        // -----------------------------
        for (int day = 1; day < n; day++) {

            // ---------------------------------------------------------
            // Option 1: Rest today
            // ---------------------------------------------------------
            // Resting is always allowed.
            // If we rest today, then we add 1 rest day to the best plan
            // from yesterday, regardless of what we did yesterday.
            dp[day][0] = 1 + Math.min(dp[day - 1][0],
                    Math.min(dp[day - 1][1], dp[day - 1][2]));

            // ---------------------------------------------------------
            // Option 2: Do coding today
            // ---------------------------------------------------------
            // Coding is possible today only if today's availability includes coding.
            // That means activities[day] is either:
            // - 1 (coding only)
            // - 3 (both coding and reading)
            //
            // Also, we cannot do coding if yesterday was also coding.
            // So the previous day must be either:
            // - rest
            // - reading
            if (activities[day] == 1 || activities[day] == 3) {
                dp[day][1] = Math.min(dp[day - 1][0], dp[day - 1][2]);
            }

            // ---------------------------------------------------------
            // Option 3: Do reading today
            // ---------------------------------------------------------
            // Reading is possible today only if today's availability includes reading.
            // That means activities[day] is either:
            // - 2 (reading only)
            // - 3 (both coding and reading)
            //
            // Also, we cannot do reading if yesterday was also reading.
            // So the previous day must be either:
            // - rest
            // - coding
            if (activities[day] == 2 || activities[day] == 3) {
                dp[day][2] = Math.min(dp[day - 1][0], dp[day - 1][1]);
            }
        }

        // The final answer is the best among all valid states on the last day.
        return Math.min(dp[n - 1][0], Math.min(dp[n - 1][1], dp[n - 1][2]));
    }

    /**
     * Computes the minimum number of rest days using a space-optimized dynamic programming approach.
     *
     * This method stores only the previous day's three states instead of the full DP table.
     * It is logically equivalent to minRestDays(int[]), but uses constant extra space.
     *
     * States:
     * - rest: minimum rest days so far if today is a rest day
     * - coding: minimum rest days so far if today is a coding day
     * - reading: minimum rest days so far if today is a reading day
     *
     * @param activities an array where each value describes what activities are available on that day:
     *                   0 = neither, 1 = coding only, 2 = reading only, 3 = both
     * @return the minimum number of rest days needed to complete the schedule validly
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(1), because only a constant number of variables are used
     */
    public int minRestDaysOptimized(int[] activities) {
        int INF = 1_000_000;

        // Initialize states for day 0.
        int rest = 1;
        int coding = (activities[0] == 1 || activities[0] == 3) ? 0 : INF;
        int reading = (activities[0] == 2 || activities[0] == 3) ? 0 : INF;

        for (int day = 1; day < activities.length; day++) {
            // Save previous day's states before computing current day's states.
            int prevRest = rest;
            int prevCoding = coding;
            int prevReading = reading;

            // Rest today: always possible.
            int newRest = 1 + Math.min(prevRest, Math.min(prevCoding, prevReading));

            // Coding today: only if coding is available and yesterday was not coding.
            int newCoding = INF;
            if (activities[day] == 1 || activities[day] == 3) {
                newCoding = Math.min(prevRest, prevReading);
            }

            // Reading today: only if reading is available and yesterday was not reading.
            int newReading = INF;
            if (activities[day] == 2 || activities[day] == 3) {
                newReading = Math.min(prevRest, prevCoding);
            }

            // Move current values into the rolling variables.
            rest = newRest;
            coding = newCoding;
            reading = newReading;
        }

        return Math.min(rest, Math.min(coding, reading));
    }

    /**
     * Utility method to print an integer array in a beginner-friendly format.
     *
     * @param activities the input activities array to display
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to the string construction
     */
    public String arrayToString(int[] activities) {
        return Arrays.toString(activities);
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n), where k is the number of demonstrated test cases
     * Space complexity: O(1) extra, excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] activities1 = {1, 3, 2, 0, 3};
        int[] activities2 = {3, 3, 3};
        int[] activities3 = {0};
        int[] activities4 = {1, 1, 1, 1};
        int[] activities5 = {2, 3, 1, 3, 2, 0, 3};

        System.out.println("Sample Input 1: " + solution.arrayToString(activities1));
        System.out.println("Minimum rest days: " + solution.minRestDays(activities1));
        System.out.println("Expected: 2");
        System.out.println();

        System.out.println("Sample Input 2: " + solution.arrayToString(activities2));
        System.out.println("Minimum rest days: " + solution.minRestDays(activities2));
        System.out.println("Expected: 1");
        System.out.println();

        System.out.println("Additional Input 3: " + solution.arrayToString(activities3));
        System.out.println("Minimum rest days: " + solution.minRestDays(activities3));
        System.out.println();

        System.out.println("Additional Input 4: " + solution.arrayToString(activities4));
        System.out.println("Minimum rest days: " + solution.minRestDays(activities4));
        System.out.println();

        System.out.println("Additional Input 5: " + solution.arrayToString(activities5));
        System.out.println("Minimum rest days: " + solution.minRestDays(activities5));
        System.out.println();

        System.out.println("Verifying optimized method gives the same results:");
        System.out.println(solution.minRestDaysOptimized(activities1));
        System.out.println(solution.minRestDaysOptimized(activities2));
        System.out.println(solution.minRestDaysOptimized(activities3));
        System.out.println(solution.minRestDaysOptimized(activities4));
        System.out.println(solution.minRestDaysOptimized(activities5));
    }
}