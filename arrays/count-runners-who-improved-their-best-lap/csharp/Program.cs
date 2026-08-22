/*
Title: Count Runners Who Improved Their Best Lap

Problem Description:
You are given an integer array laps where laps[i] represents the lap time recorded by a runner on day i.
A smaller lap time is better.

A runner is said to have improved their best lap on day i if laps[i] is strictly smaller than every lap
time that appeared before it.

The first day does not count as an improvement, because there is no earlier lap to compare against.

Return the number of days on which the runner improved their best lap.

Key idea:
- Scan the array from left to right.
- Keep track of the smallest lap time seen so far.
- Whenever the current lap is strictly smaller than that smallest value, it is a new improvement.
- Equal values do NOT count, because the lap must be strictly better.

Example 1:
Input:  [72, 70, 71, 69, 69, 68]
Output: 3
Explanation:
- Day 0: 72 -> first day, does not count
- Day 1: 70 -> smaller than 72, improvement #1
- Day 2: 71 -> not smaller than best-so-far 70
- Day 3: 69 -> smaller than 70, improvement #2
- Day 4: 69 -> equal to best-so-far 69, does not count
- Day 5: 68 -> smaller than 69, improvement #3

Example 2:
Input:  [55, 55, 55, 54, 53]
Output: 2
Explanation:
- Day 0: 55 -> first day, does not count
- Day 1: 55 -> equal, does not count
- Day 2: 55 -> equal, does not count
- Day 3: 54 -> smaller than 55, improvement #1
- Day 4: 53 -> smaller than 54, improvement #2

Constraints:
- 1 <= laps.length <= 100000
- 1 <= laps[i] <= 1000000000
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each element exactly once.

    Space Complexity: O(1)
    - We only use a few extra variables regardless of input size.
    */
    public int CountImprovedBestLaps(int[] laps)
    {
        // The problem guarantees at least one element, but this guard makes the method safer
        // and easier for beginners to understand in case it is reused elsewhere.
        if (laps == null || laps.Length == 0)
        {
            return 0;
        }

        // This variable stores the smallest lap time we have seen so far while scanning
        // from left to right.
        //
        // Why do we need it?
        // A day counts as an improvement only if its lap time is strictly smaller than
        // every earlier lap time. Instead of checking all previous values every time
        // (which would be slow), we only need to remember the best previous lap.
        //
        // Since smaller lap times are better, "best so far" means "minimum so far".
        int bestSoFar = laps[0];

        // This variable counts how many improvement days we find.
        int improvements = 0;

        // We start from index 1, not index 0.
        //
        // Why?
        // The first day never counts as an improvement because there is no earlier day
        // to compare against. It simply establishes the initial best lap.
        for (int i = 1; i < laps.Length; i++)
        {
            // Read the current day's lap time into a local variable.
            //
            // This is not required for correctness, but it makes the code easier to read
            // because we can talk about "currentLap" clearly in the logic below.
            int currentLap = laps[i];

            // Check whether today's lap is strictly better than every previous lap.
            //
            // Because bestSoFar stores the smallest previous lap time, the condition
            // "currentLap < bestSoFar" means:
            // - currentLap is smaller than the best previous lap
            // - therefore currentLap is smaller than all previous laps
            // - therefore today is an improvement day
            //
            // Important:
            // We use < and NOT <= because equal lap times do not count as improvements.
            if (currentLap < bestSoFar)
            {
                // We found a new improvement day, so increase the answer.
                improvements++;

                // Since currentLap is now the smallest value seen so far, we must update
                // bestSoFar.
                //
                // Why is this necessary?
                // Future days must be compared against the newest best lap, not the old one.
                bestSoFar = currentLap;
            }
            else
            {
                // If currentLap is greater than or equal to bestSoFar, then it is not
                // strictly better than all previous laps.
                //
                // In that case:
                // - it does not count as an improvement
                // - bestSoFar stays unchanged
            }
        }

        // After scanning all days, improvements contains the total number of days
        // where the runner achieved a strictly better lap than on all earlier days.
        return improvements;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] laps1 = { 72, 70, 71, 69, 69, 68 };
int result1 = solution.CountImprovedBestLaps(laps1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] laps2 = { 55, 55, 55, 54, 53 };
int result2 = solution.CountImprovedBestLaps(laps2);
Console.WriteLine(result2); // Expected: 2

// Additional demo
int[] laps3 = { 100 };
int result3 = solution.CountImprovedBestLaps(laps3);
Console.WriteLine(result3); // Expected: 0

int[] laps4 = { 90, 89, 88, 87 };
int result4 = solution.CountImprovedBestLaps(laps4);
Console.WriteLine(result4); // Expected: 3