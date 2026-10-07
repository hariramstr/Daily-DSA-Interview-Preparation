/*
Title: Minimum Cost to Plan Exam Study Days

Problem Description:
You are given a strictly increasing array studyDays where each value represents a calendar day on which a student must attend a required study session before an exam. The student can buy study passes that cover consecutive calendar days. A 1-day pass costs costs[0], a 3-day pass costs costs[1], and a 7-day pass costs costs[2]. A pass bought on day d covers day d and the next consecutive days based on its duration. For example, a 3-day pass bought on day 10 covers days 10, 11, and 12.

Your task is to return the minimum total cost needed to cover every day in studyDays.

A pass may cover days that are not in studyDays, and buying multiple overlapping passes is allowed, although it may not be optimal. The student may buy any number of passes in any order as long as every required study day is covered by at least one active pass.

Design an efficient algorithm. A brute-force search over all pass combinations will be too slow for the largest inputs.

Constraints:
- 1 <= studyDays.length <= 365
- 1 <= studyDays[i] <= 365
- studyDays is strictly increasing
- costs.length == 3
- 1 <= costs[i] <= 1000
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - n is the number of required study days.
    - For each study day, we advance two pointers at most n times total across the whole algorithm.
    - Because each pointer only moves forward and never backward, the total work is linear.

    Space Complexity: O(n)
    - We use a dp array of size n + 1.
    - No other large data structures are needed.
    */
    public int MinCostToCoverStudyDays(int[] studyDays, int[] costs)
    {
        int n = studyDays.Length;

        // dp[i] will store the minimum cost needed to cover the first i required study days.
        //
        // Important meaning:
        // - dp[0] = 0 means covering zero study days costs nothing.
        // - dp[1] means minimum cost to cover studyDays[0]
        // - dp[2] means minimum cost to cover studyDays[0..1]
        // - ...
        // - dp[n] means minimum cost to cover all required study days
        //
        // This is a classic dynamic programming setup:
        // we build the answer for larger prefixes using answers for smaller prefixes.
        int[] dp = new int[n + 1];

        // These two pointers help us efficiently find:
        // - the earliest study day NOT covered by a 3-day pass ending at the current decision point
        // - the earliest study day NOT covered by a 7-day pass ending at the current decision point
        //
        // More concretely:
        // when we are deciding how to cover studyDays[i - 1], we imagine buying a pass that starts
        // on that day. Then we need to know how many earlier study days are still uncovered.
        //
        // Instead of scanning backward every time, we move these pointers forward only once overall.
        int j3 = 0;
        int j7 = 0;

        // We process required study days one by one.
        for (int i = 1; i <= n; i++)
        {
            // This is the actual calendar day we are currently trying to ensure is covered.
            int currentDay = studyDays[i - 1];

            // ------------------------------------------------------------
            // Option 1: Buy a 1-day pass for currentDay
            // ------------------------------------------------------------
            //
            // A 1-day pass only covers the current required day itself.
            // So the cost is:
            // - minimum cost to cover the previous i - 1 study days
            // - plus the cost of one 1-day pass
            int costWith1DayPass = dp[i - 1] + costs[0];

            // ------------------------------------------------------------
            // Option 2: Buy a 3-day pass starting on currentDay
            // ------------------------------------------------------------
            //
            // A 3-day pass bought on currentDay covers:
            // currentDay, currentDay + 1, currentDay + 2
            //
            // Since we are processing required days in increasing order, we want to know:
            // "What is the first required study day that is covered by this pass?"
            //
            // Another equivalent and easier way to think about it:
            // We want j3 to be the first index such that studyDays[j3] >= currentDay - 2
            //
            // Why currentDay - 2?
            // Because any required day earlier than currentDay - 2 cannot be covered by a 3-day pass
            // that includes currentDay.
            //
            // Example:
            // If currentDay = 10, a 3-day pass can cover days 8, 9, 10 if we think backward in terms
            // of "which previous required days could be grouped with currentDay under one 3-day window".
            //
            // Since the pass duration is 3 consecutive days, the covered window relevant to currentDay is:
            // [currentDay - 2, currentDay]
            //
            // We move j3 forward until it points to the first study day inside that window.
            while (j3 < n && studyDays[j3] < currentDay - 2)
            {
                j3++;
            }

            // Now:
            // - studyDays[0..j3-1] are NOT covered by this 3-day pass
            // - studyDays[j3..i-1] ARE covered by this 3-day pass
            //
            // So total cost is:
            // - dp[j3] to cover all required days before index j3
            // - plus the cost of one 3-day pass
            int costWith3DayPass = dp[j3] + costs[1];

            // ------------------------------------------------------------
            // Option 3: Buy a 7-day pass covering currentDay
            // ------------------------------------------------------------
            //
            // Same idea as above.
            // A 7-day pass can cover a 7-day window ending at currentDay:
            // [currentDay - 6, currentDay]
            //
            // Any required study day earlier than currentDay - 6 cannot be covered by that pass.
            while (j7 < n && studyDays[j7] < currentDay - 6)
            {
                j7++;
            }

            // After moving j7:
            // - studyDays[0..j7-1] are outside the 7-day window and must already be covered
            // - studyDays[j7..i-1] are covered by this 7-day pass
            int costWith7DayPass = dp[j7] + costs[2];

            // ------------------------------------------------------------
            // Choose the cheapest of the three valid choices
            // ------------------------------------------------------------
            //
            // This is the heart of dynamic programming:
            // for the current subproblem, we try all reasonable actions and keep the best one.
            dp[i] = Math.Min(costWith1DayPass, Math.Min(costWith3DayPass, costWith7DayPass));
        }

        // The final answer is the minimum cost to cover all n required study days.
        return dp[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] studyDays1 = { 1, 4, 6, 7, 8, 20 };
int[] costs1 = { 2, 7, 15 };
int result1 = solution.MinCostToCoverStudyDays(studyDays1, costs1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
//
// Important note:
// The problem statement's explanation claims the answer is 17.
// However, that is not actually optimal.
//
// A cheaper valid plan is:
// - 3-day pass starting on day 2 covers 2,3,4   => cost 8
// - 3-day pass starting on day 5 covers 5,6,7   => cost 8 (covers required day 5)
// - 3-day pass starting on day 9 covers 9,10,11 => cost 8
// - 1-day pass for day 30                        => cost 3
// Total = 27? No, that is worse.
//
// Better:
// - 7-day pass covering days 2 through 8        => cost 14
// - 3-day pass covering days 9 through 11       => cost 8
// - 1-day pass for day 30                        => cost 3
// Total = 25? No, because costs are [3,8,14], so 7-day pass costs 14.
//
// But even better:
// - 3-day pass for days 2,3,4                    => 8
// - 1-day pass for day 5                         => 3
// - 3-day pass for days 9,10,11                  => 8
// - 1-day pass for day 30                        => 3
// Total = 22
//
// Best found by DP is 22.
//
// Therefore, the statement's sample output of 17 is inconsistent with the given costs.
// The algorithm below computes the true minimum for the provided input.
int[] studyDays2 = { 2, 3, 4, 5, 9, 10, 11, 30 };
int[] costs2 = { 3, 8, 14 };
int result2 = solution.MinCostToCoverStudyDays(studyDays2, costs2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick custom demo
int[] studyDays3 = { 1, 2, 3, 10 };
int[] costs3 = { 4, 5, 20 };
int result3 = solution.MinCostToCoverStudyDays(studyDays3, costs3);
Console.WriteLine($"Custom Example Result: {result3}");