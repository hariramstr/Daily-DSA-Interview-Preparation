import java.util.*;

/*
Title: Maximum Stability Score from Choosing Daily Work Modes
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A product team plans work over n days. On each day i, the team may operate in exactly one of three modes:
Focus, Meeting, or Recovery. If the team chooses mode m on day i, it earns score[i][m] stability points.
However, using the same mode on consecutive days causes burnout, so the same mode cannot be chosen on
two adjacent days.

Your task is to compute the maximum total stability score the team can earn over all n days while
respecting this rule.

Formally, you are given a 2D integer array score of size n x 3, where score[i][0], score[i][1], and
score[i][2] represent the points earned on day i for choosing Focus, Meeting, and Recovery respectively.
Choose exactly one mode per day such that for every i > 0, the chosen mode on day i is different from
the chosen mode on day i - 1. Return the maximum possible sum of points.

This problem is intended to test state-based dynamic programming. A brute-force search over all possible
schedules grows exponentially and will not finish for large inputs, but the transition only depends on
the previous day.

Constraints:
- 1 <= n <= 100000
- score.length == n
- score[i].length == 3
- 0 <= score[i][m] <= 10000

Example 1:
Input: score = [[8,5,6],[7,9,4],[3,8,10],[9,2,7]]
Output: 36
Explanation: One optimal schedule is Focus on day 0 (8), Meeting on day 1 (9), Recovery on day 2 (10),
Focus on day 3 (9). The total is 8 + 9 + 10 + 9 = 36.

Example 2:
Input: score = [[4,4,7],[6,1,5],[2,9,3]]
Output: 22
Explanation: Choose Recovery on day 0 (7), Focus on day 1 (6), and Meeting on day 2 (9). This satisfies
the adjacency rule and gives 7 + 6 + 9 = 22.
*/

public class Solution {

    /**
     * Computes the maximum total stability score using dynamic programming with constant extra space.
     *
     * The key idea:
     * - For each day, we track the best total score if we end that day in:
     *   1) Focus
     *   2) Meeting
     *   3) Recovery
     * - Because the same mode cannot be chosen on consecutive days, the value for a mode today
     *   must come from one of the other two modes from yesterday.
     *
     * Example transition for day i:
     * - bestFocusToday = score[i][0] + max(bestMeetingYesterday, bestRecoveryYesterday)
     * - bestMeetingToday = score[i][1] + max(bestFocusYesterday, bestRecoveryYesterday)
     * - bestRecoveryToday = score[i][2] + max(bestFocusYesterday, bestMeetingYesterday)
     *
     * We only need the previous day's three values, so we use O(1) extra space.
     *
     * @param score a 2D array where score[i][0], score[i][1], and score[i][2] are the points for
     *              Focus, Meeting, and Recovery on day i
     * @return the maximum total stability score achievable while never choosing the same mode on
     *         two consecutive days
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(1), excluding the input array
     */
    public long maximumStabilityScore(int[][] score) {
        validateInput(score);

        int n = score.length;

        // Base case:
        // On day 0, if we end in a specific mode, the best score is simply the score of that mode
        // on day 0, because there is no previous day to conflict with.
        long prevFocus = score[0][0];
        long prevMeeting = score[0][1];
        long prevRecovery = score[0][2];

        // Process each remaining day one by one.
        for (int day = 1; day < n; day++) {
            // If we choose Focus today, yesterday cannot also be Focus.
            // So we take today's Focus score plus the better of:
            // - ending yesterday with Meeting
            // - ending yesterday with Recovery
            long currentFocus = score[day][0] + Math.max(prevMeeting, prevRecovery);

            // If we choose Meeting today, yesterday cannot be Meeting.
            // So we take today's Meeting score plus the better of:
            // - ending yesterday with Focus
            // - ending yesterday with Recovery
            long currentMeeting = score[day][1] + Math.max(prevFocus, prevRecovery);

            // If we choose Recovery today, yesterday cannot be Recovery.
            // So we take today's Recovery score plus the better of:
            // - ending yesterday with Focus
            // - ending yesterday with Meeting
            long currentRecovery = score[day][2] + Math.max(prevFocus, prevMeeting);

            // Move the "current day" values into the "previous day" variables
            // so the next loop iteration can build on them.
            prevFocus = currentFocus;
            prevMeeting = currentMeeting;
            prevRecovery = currentRecovery;
        }

        // After processing all days, the answer is the best total among the three possible
        // ending modes on the final day.
        return Math.max(prevFocus, Math.max(prevMeeting, prevRecovery));
    }

    /**
     * Computes the maximum total stability score using a full DP table.
     *
     * This version is more explicit and beginner-friendly for learning:
     * - dp[i][0] = best total score up to day i if day i uses Focus
     * - dp[i][1] = best total score up to day i if day i uses Meeting
     * - dp[i][2] = best total score up to day i if day i uses Recovery
     *
     * Transition:
     * - dp[i][0] = score[i][0] + max(dp[i - 1][1], dp[i - 1][2])
     * - dp[i][1] = score[i][1] + max(dp[i - 1][0], dp[i - 1][2])
     * - dp[i][2] = score[i][2] + max(dp[i - 1][0], dp[i - 1][1])
     *
     * @param score a 2D array where score[i][0], score[i][1], and score[i][2] are the points for
     *              Focus, Meeting, and Recovery on day i
     * @return the maximum total stability score achievable while never choosing the same mode on
     *         two consecutive days
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(n), for the DP table
     */
    public long maximumStabilityScoreWithTable(int[][] score) {
        validateInput(score);

        int n = score.length;
        long[][] dp = new long[n][3];

        // Initialize day 0 directly from the input.
        dp[0][0] = score[0][0];
        dp[0][1] = score[0][1];
        dp[0][2] = score[0][2];

        // Fill the DP table from day 1 to day n - 1.
        for (int day = 1; day < n; day++) {
            dp[day][0] = score[day][0] + Math.max(dp[day - 1][1], dp[day - 1][2]);
            dp[day][1] = score[day][1] + Math.max(dp[day - 1][0], dp[day - 1][2]);
            dp[day][2] = score[day][2] + Math.max(dp[day - 1][0], dp[day - 1][1]);
        }

        return Math.max(dp[n - 1][0], Math.max(dp[n - 1][1], dp[n - 1][2]));
    }

    /**
     * Validates the input according to the problem structure.
     *
     * This method checks:
     * - the array is not null
     * - there is at least one day
     * - each day has exactly three mode scores
     *
     * @param score the input score matrix to validate
     * @return nothing; this method throws an exception if the input is invalid
     * Time complexity: O(n), because each row is checked once
     * Space complexity: O(1)
     */
    public void validateInput(int[][] score) {
        if (score == null || score.length == 0) {
            throw new IllegalArgumentException("Input score array must contain at least one day.");
        }

        for (int i = 0; i < score.length; i++) {
            if (score[i] == null || score[i].length != 3) {
                throw new IllegalArgumentException("Each day must contain exactly 3 mode scores.");
            }
        }
    }

    /**
     * Converts a 2D int array into a readable string for printing.
     *
     * @param matrix the 2D array to convert
     * @return a readable string representation of the matrix
     * Time complexity: O(n), where n is the number of rows
     * Space complexity: O(n), for the produced string content
     */
    public String array2DToString(int[][] matrix) {
        return Arrays.deepToString(matrix);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It prints:
     * - the input matrix
     * - the computed answer
     * - the expected answer
     *
     * Verified manually:
     * Example 1:
     * - Day 0: [8, 5, 6]
     * - Day 1: Focus=13, Meeting=17, Recovery=12
     * - Day 2: Focus=20, Meeting=21, Recovery=27
     * - Day 3: Focus=36, Meeting=29, Recovery=28
     *   Answer = 36
     *
     * Example 2:
     * - Day 0: [4, 4, 7]
     * - Day 1: Focus=13, Meeting=8, Recovery=9
     * - Day 2: Focus=11, Meeting=22, Recovery=16
     *   Answer = 22
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) for each demonstrated test case
     * Space complexity: O(1) extra for the optimized method
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] score1 = {
            {8, 5, 6},
            {7, 9, 4},
            {3, 8, 10},
            {9, 2, 7}
        };

        int[][] score2 = {
            {4, 4, 7},
            {6, 1, 5},
            {2, 9, 3}
        };

        long result1 = solution.maximumStabilityScore(score1);
        long result2 = solution.maximumStabilityScore(score2);

        System.out.println("Example 1 Input: " + solution.array2DToString(score1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Example 1 Expected: 36");
        System.out.println();

        System.out.println("Example 2 Input: " + solution.array2DToString(score2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Example 2 Expected: 22");
        System.out.println();

        // Optional extra demonstration showing the table-based version matches.
        System.out.println("Example 1 Output using DP table: " + solution.maximumStabilityScoreWithTable(score1));
        System.out.println("Example 2 Output using DP table: " + solution.maximumStabilityScoreWithTable(score2));
    }
}