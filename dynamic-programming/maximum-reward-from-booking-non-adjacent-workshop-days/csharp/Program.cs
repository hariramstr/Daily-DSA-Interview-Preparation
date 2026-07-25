/*
Title: Maximum Reward from Booking Non-Adjacent Workshop Days
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A training company offers a sequence of one-day workshops over the next n days. If you book the workshop on day i, you earn rewards[i] points. However, preparing for a workshop uses the entire following day, so you are not allowed to book workshops on two adjacent days.

Your task is to return the maximum total reward points you can earn by choosing a subset of workshop days under this rule.

Formally, given an integer array rewards where rewards[i] is the reward for booking the workshop on day i, choose a set of indices such that no two chosen indices differ by 1, and the sum of their rewards is as large as possible.

This is not just about greedily taking the largest reward. A smaller reward today may allow a better combination later, so you must consider overlapping subproblems efficiently.

Constraints:
- 1 <= rewards.length <= 100000
- 0 <= rewards[i] <= 1000000000
- The answer fits in a 64-bit signed integer

Example 1:
Input: rewards = [4, 10, 3, 1, 5]
Output: 15
Explanation: Book days 1 and 4 for a total of 10 + 5 = 15. Booking day 0, 2, and 4 gives 12, which is smaller.

Example 2:
Input: rewards = [2, 7, 9, 3, 1]
Output: 12
Explanation: The best choice is day 0, day 2, and day 4 for 2 + 9 + 1 = 12.

We solve this with dynamic programming:
For each day, we decide between:
1. Skip the current day -> keep the best reward from previous day
2. Book the current day -> add current reward to the best reward from two days ago

The recurrence is:
dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])

To reduce memory usage from O(n) to O(1), we only keep the last two DP values.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the rewards array exactly once.
    - Each day performs only constant-time work.

    Space Complexity: O(1)
    - We do not build a full DP array.
    - We only store two previous dynamic programming states.
    */
    public long MaxReward(int[] rewards)
    {
        // The input constraints guarantee at least one element,
        // but this guard makes the method safer and easier to reuse.
        // If there are no workshop days, the maximum reward is 0.
        if (rewards == null || rewards.Length == 0)
        {
            return 0;
        }

        // "prevTwo" will represent the best answer up to day i - 2.
        // At the beginning, before processing any days, that value is 0
        // because choosing from no days gives reward 0.
        long prevTwo = 0;

        // "prevOne" will represent the best answer up to day i - 1.
        // Before we process any day, this is also 0 for the same reason.
        long prevOne = 0;

        // We now process each day from left to right.
        // This order is important because the best answer for today depends
        // on answers from earlier days only.
        for (int i = 0; i < rewards.Length; i++)
        {
            // Option 1: Skip the current day.
            // If we do not book today's workshop, then the best total reward
            // remains exactly the same as the best answer up to yesterday.
            long skipCurrentDay = prevOne;

            // Option 2: Book the current day.
            // If we book today, we are forbidden from booking yesterday.
            // Therefore, the best compatible previous answer is the one from
            // two days ago, stored in "prevTwo".
            // We add today's reward to that value.
            long takeCurrentDay = prevTwo + rewards[i];

            // The best answer up to the current day is whichever choice
            // gives a larger total reward:
            // - skipping today
            // - taking today
            long current = Math.Max(skipCurrentDay, takeCurrentDay);

            // Now we shift our rolling DP window forward by one day.
            //
            // Before moving on:
            // - prevOne was dp[i - 1]
            // - prevTwo was dp[i - 2]
            //
            // After moving on:
            // - prevTwo should become old prevOne
            // - prevOne should become current dp[i]
            prevTwo = prevOne;
            prevOne = current;
        }

        // After processing all days, "prevOne" stores the best possible
        // total reward for the entire array.
        return prevOne;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// rewards = [4, 10, 3, 1, 5]
// Best choice: day 1 and day 4 => 10 + 5 = 15
int[] rewards1 = { 4, 10, 3, 1, 5 };
long result1 = solution.MaxReward(rewards1);
Console.WriteLine(result1); // Expected: 15

// Example 2:
// rewards = [2, 7, 9, 3, 1]
// Best choice: day 0, day 2, day 4 => 2 + 9 + 1 = 12
int[] rewards2 = { 2, 7, 9, 3, 1 };
long result2 = solution.MaxReward(rewards2);
Console.WriteLine(result2); // Expected: 12

// Additional small sanity check:
// Single day: must take it if positive
int[] rewards3 = { 8 };
long result3 = solution.MaxReward(rewards3);
Console.WriteLine(result3); // Expected: 8

// Additional sanity check:
// All zeros => answer is 0
int[] rewards4 = { 0, 0, 0, 0 };
long result4 = solution.MaxReward(rewards4);
Console.WriteLine(result4); // Expected: 0