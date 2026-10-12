/*
Title: Maximum Stability Score from Choosing Daily Work Modes

Problem Description:
A product team plans work over n days. On each day i, the team may operate in exactly one of three modes:
Focus, Meeting, or Recovery.

If the team chooses mode m on day i, it earns score[i][m] stability points.

Rule:
The same mode cannot be chosen on two consecutive days.

Goal:
Given a 2D integer array score of size n x 3, compute the maximum total stability score
that can be earned over all n days while respecting the adjacency rule.

Dynamic Programming Insight:
A brute-force approach would try all possible schedules, which grows exponentially.
However, the choice for the current day only depends on what was chosen on the previous day.
That makes this a classic dynamic programming problem.

State idea:
Let dp[i][m] represent the maximum score we can achieve up to day i
if we choose mode m on day i.

Transition:
dp[i][0] = score[i][0] + max(dp[i-1][1], dp[i-1][2])
dp[i][1] = score[i][1] + max(dp[i-1][0], dp[i-1][2])
dp[i][2] = score[i][2] + max(dp[i-1][0], dp[i-1][1])

Because each day only depends on the previous day, we can optimize space
and keep only the previous day's 3 values.

Example 1:
score = [[8,5,6],[7,9,4],[3,8,10],[9,2,7]]

Day 0:
Focus=8, Meeting=5, Recovery=6

Day 1:
Focus = 7 + max(5,6) = 13
Meeting = 9 + max(8,6) = 17
Recovery = 4 + max(8,5) = 12

Day 2:
Focus = 3 + max(17,12) = 20
Meeting = 8 + max(13,12) = 21
Recovery = 10 + max(13,17) = 27

Day 3:
Focus = 9 + max(21,27) = 36
Meeting = 2 + max(20,27) = 29
Recovery = 7 + max(20,21) = 28

Answer = max(36,29,28) = 36

Example 2:
score = [[4,4,7],[6,1,5],[2,9,3]]

Day 0:
Focus=4, Meeting=4, Recovery=7

Day 1:
Focus = 6 + max(4,7) = 13
Meeting = 1 + max(4,7) = 8
Recovery = 5 + max(4,4) = 9

Day 2:
Focus = 2 + max(8,9) = 11
Meeting = 9 + max(13,9) = 22
Recovery = 3 + max(13,8) = 16

Answer = max(11,22,16) = 22
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n)
    We process each day exactly once, and for each day we compute 3 values.

    Space Complexity:
    O(1)
    We only store the previous day's 3 DP values and the current day's 3 DP values.
    */
    public int MaximumStabilityScore(int[][] score)
    {
        // The input guarantees at least one day, but this guard makes the method safer
        // and easier to understand for beginners.
        if (score == null || score.Length == 0)
        {
            return 0;
        }

        // -----------------------------
        // STEP 1: Initialize the DP state for day 0
        // -----------------------------
        // Why?
        // On the first day, there is no previous day, so there is no adjacency restriction yet.
        // That means:
        // - If we choose Focus on day 0, the best total is simply score[0][0]
        // - If we choose Meeting on day 0, the best total is simply score[0][1]
        // - If we choose Recovery on day 0, the best total is simply score[0][2]
        //
        // We store these in three variables instead of a full 2D DP table because
        // each future day only depends on the previous day.
        int prevFocus = score[0][0];
        int prevMeeting = score[0][1];
        int prevRecovery = score[0][2];

        // -----------------------------
        // STEP 2: Process each remaining day from left to right
        // -----------------------------
        // For each day, we compute the best total if we end that day in each of the 3 modes.
        // The key rule is:
        // We cannot choose the same mode as the previous day.
        for (int day = 1; day < score.Length; day++)
        {
            // ---------------------------------------------------------
            // Compute best total if we choose Focus on the current day
            // ---------------------------------------------------------
            // If today is Focus, yesterday cannot also be Focus.
            // So the previous day must have been either Meeting or Recovery.
            //
            // We take the better of those two possibilities and add today's Focus score.
            int currentFocus = score[day][0] + Math.Max(prevMeeting, prevRecovery);

            // -----------------------------------------------------------
            // Compute best total if we choose Meeting on the current day
            // -----------------------------------------------------------
            // If today is Meeting, yesterday must have been Focus or Recovery.
            int currentMeeting = score[day][1] + Math.Max(prevFocus, prevRecovery);

            // ------------------------------------------------------------
            // Compute best total if we choose Recovery on the current day
            // ------------------------------------------------------------
            // If today is Recovery, yesterday must have been Focus or Meeting.
            int currentRecovery = score[day][2] + Math.Max(prevFocus, prevMeeting);

            // ------------------------------------------------------------
            // STEP 3: Move the "current day" values into "previous day"
            // ------------------------------------------------------------
            // Why?
            // On the next loop iteration, today's results become yesterday's results.
            // This is the standard rolling-DP optimization.
            prevFocus = currentFocus;
            prevMeeting = currentMeeting;
            prevRecovery = currentRecovery;
        }

        // -----------------------------
        // STEP 4: Return the best final answer
        // -----------------------------
        // After processing all days, the schedule may end in any of the 3 modes.
        // So the overall best answer is the maximum of those 3 possibilities.
        return Math.Max(prevFocus, Math.Max(prevMeeting, prevRecovery));
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[][] score1 =
{
    new[] { 8, 5, 6 },
    new[] { 7, 9, 4 },
    new[] { 3, 8, 10 },
    new[] { 9, 2, 7 }
};

int result1 = solution.MaximumStabilityScore(score1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 36

// Example 2
int[][] score2 =
{
    new[] { 4, 4, 7 },
    new[] { 6, 1, 5 },
    new[] { 2, 9, 3 }
};

int result2 = solution.MaximumStabilityScore(score2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 22

// Additional small test: single day
int[][] score3 =
{
    new[] { 10, 3, 8 }
};

int result3 = solution.MaximumStabilityScore(score3);
Console.WriteLine("Single Day Result: " + result3); // Expected: 10