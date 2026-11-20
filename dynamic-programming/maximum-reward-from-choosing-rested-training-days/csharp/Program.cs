/*
Title: Maximum Reward from Choosing Rested Training Days
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A sports analytics team is building a planner for an athlete preparing for a tournament.
You are given an integer array rewards where rewards[i] is the performance benefit of
training on day i. Some days may even have negative values, meaning training that day
would hurt recovery more than it helps. The athlete may choose any subset of days to
train, but cannot train on two consecutive days because each training day requires a
full recovery day afterward.

Your task is to return the maximum total reward the athlete can earn.

Unlike a simple greedy choice, the best answer may require skipping a large positive day
in order to combine several other days more effectively. If all values are negative, the
athlete is allowed to train on no days at all, so the answer can be 0.

Write a function that computes this maximum reward.

Constraints:
- 1 <= rewards.length <= 100000
- -10000 <= rewards[i] <= 10000
- Your solution should run in O(n) time
- Aim for O(1) extra space if possible

Example 1:
Input: rewards = [5, 1, 1, 5]
Output: 10
Explanation: Train on days 0 and 3 for a total reward of 5 + 5 = 10. Training on adjacent days is not allowed.

Example 2:
Input: rewards = [4, -2, 7, 3, -1, 8]
Output: 19
Explanation: One optimal plan is to train on days 0, 2, and 5. The total reward is 4 + 7 + 8 = 19.

Core Dynamic Programming Idea:
For each day, we decide between two choices:
1. Skip the current day:
   - Then the best total reward stays the same as the best answer up to the previous day.
2. Train on the current day:
   - Then we are not allowed to train on the previous day,
     so we add the current reward to the best answer up to day i - 2.

The recurrence is:
dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])

Because dp[i] only depends on the previous two states, we can optimize space
from O(n) down to O(1) by storing only two variables.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - Each day performs only constant-time work.

    Space Complexity: O(1)
    - We do not build a full DP array.
    - We only keep track of the best answers for the previous two positions.

    Beginner-friendly explanation:
    We maintain:
    - prevTwo = best answer up to day i - 2
    - prevOne = best answer up to day i - 1

    For each current day:
    - If we skip it, total reward is prevOne
    - If we take it, total reward is prevTwo + rewards[i]

    The best answer for this day is the larger of those two values.
    Then we slide the window forward:
    - prevTwo becomes old prevOne
    - prevOne becomes current best
    */
    public int MaxReward(int[] rewards)
    {
        // Defensive handling:
        // The problem guarantees at least one element, but checking for null or empty
        // makes the method safer and easier to reuse in real-world code.
        // If there are no days, the athlete cannot train, so the reward is 0.
        if (rewards == null || rewards.Length == 0)
        {
            return 0;
        }

        // prevTwo represents the best total reward we can achieve considering days
        // up to index i - 2.
        //
        // At the very beginning, before processing any day, the best reward is 0
        // because the athlete is allowed to choose no days at all.
        int prevTwo = 0;

        // prevOne represents the best total reward we can achieve considering days
        // up to index i - 1.
        //
        // Initially, before processing the first day, this is also 0.
        int prevOne = 0;

        // We now process each day from left to right.
        // This order is important because the decision for the current day depends
        // on already-computed best answers from earlier days.
        for (int i = 0; i < rewards.Length; i++)
        {
            // Option 1: Skip the current day.
            //
            // If we skip day i, then the best total reward does not change from the
            // best answer we already had up to day i - 1.
            int skipCurrentDay = prevOne;

            // Option 2: Train on the current day.
            //
            // If we train on day i, we are forbidden from training on day i - 1.
            // Therefore, the best compatible previous total is the answer up to day i - 2,
            // which is stored in prevTwo.
            //
            // We add rewards[i] to prevTwo to get the total reward for taking this day.
            int takeCurrentDay = prevTwo + rewards[i];

            // Choose the better of the two options.
            //
            // This is the heart of the dynamic programming transition:
            // best up to current day = max(skip current day, take current day)
            //
            // This also naturally handles negative values:
            // - If rewards[i] is negative, takeCurrentDay may be worse than skipping.
            // - Because skipping is always allowed, the algorithm will never be forced
            //   into a negative total.
            int currentBest = Math.Max(skipCurrentDay, takeCurrentDay);

            // Move the rolling DP window forward.
            //
            // Before updating:
            // - prevTwo = best up to i - 2
            // - prevOne = best up to i - 1
            //
            // After processing day i:
            // - prevTwo should become the old prevOne
            // - prevOne should become currentBest
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After the loop finishes, prevOne stores the best answer considering all days.
        return prevOne;
    }
}

// Demo code:
// We create the sample inputs from the problem statement,
// call the solution method, and print the results.

var solution = new Solution();

// Example 1:
// rewards = [5, 1, 1, 5]
// Best choice: day 0 and day 3 => 5 + 5 = 10
int[] rewards1 = { 5, 1, 1, 5 };
int result1 = solution.MaxReward(rewards1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 10

// Example 2:
// rewards = [4, -2, 7, 3, -1, 8]
// One optimal choice: day 0, day 2, day 5 => 4 + 7 + 8 = 19
int[] rewards2 = { 4, -2, 7, 3, -1, 8 };
int result2 = solution.MaxReward(rewards2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 19

// Additional demo: all negative values
// Since the athlete may choose no days at all, the answer should be 0.
int[] rewards3 = { -5, -1, -9 };
int result3 = solution.MaxReward(rewards3);
Console.WriteLine("All Negative Example Result: " + result3); // Expected: 0

// Additional demo: single day
int[] rewards4 = { 12 };
int result4 = solution.MaxReward(rewards4);
Console.WriteLine("Single Day Example Result: " + result4); // Expected: 12

// Additional demo: mixed values
int[] rewards5 = { 2, 1, 4, 9 };
int result5 = solution.MaxReward(rewards5);
Console.WriteLine("Mixed Example Result: " + result5); // Expected: 11 (2 + 9)