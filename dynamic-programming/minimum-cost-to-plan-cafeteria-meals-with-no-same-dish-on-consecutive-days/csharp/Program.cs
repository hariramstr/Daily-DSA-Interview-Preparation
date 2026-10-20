/*
Title: Minimum Cost to Plan Cafeteria Meals with No Same Dish on Consecutive Days

Problem Description:
A company cafeteria offers exactly three meal options each workday: Pasta, Salad, and Soup.
You are given a 2D array costs where costs[i][j] is the cost of choosing meal j on day i,
with j = 0 for Pasta, j = 1 for Salad, and j = 2 for Soup.

You must choose exactly one meal for each day.
However, you are not allowed to choose the same meal on two consecutive days.

Your task is to compute the minimum total cost needed to plan meals for all days while
satisfying the no-consecutive-repeat rule.

Return the minimum possible total cost.

Dynamic Programming Idea:
For each day, if we decide today's meal is:
- Pasta, then yesterday must have been Salad or Soup
- Salad, then yesterday must have been Pasta or Soup
- Soup, then yesterday must have been Pasta or Salad

So for each day, we keep track of the minimum total cost ending with each of the 3 meals.
Because each day's state depends only on the previous day, we can optimize space to O(1).
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each day exactly once.
    - For each day, we do a constant amount of work because there are only 3 meal choices.

    Space Complexity: O(1)
    - We only store the previous day's 3 dynamic programming values.
    - We do not create an extra DP table proportional to the number of days.
    */
    public int MinMealPlanCost(int[][] costs)
    {
        // Defensive handling:
        // The problem guarantees at least one day, but checking for null or empty input
        // makes the method safer and easier to understand in real-world code.
        if (costs == null || costs.Length == 0)
        {
            return 0;
        }

        // Base case:
        // On day 0, the minimum total cost of ending with each meal is simply the direct cost
        // of choosing that meal on day 0, because there is no previous day to worry about.
        //
        // prevPasta  = minimum total cost up to the previous day if previous day ends with Pasta
        // prevSalad  = minimum total cost up to the previous day if previous day ends with Salad
        // prevSoup   = minimum total cost up to the previous day if previous day ends with Soup
        int prevPasta = costs[0][0];
        int prevSalad = costs[0][1];
        int prevSoup = costs[0][2];

        // We now process days 1 through the last day.
        // For each day, we compute the best possible total cost for ending that day
        // with each meal, while respecting the "no same meal on consecutive days" rule.
        for (int day = 1; day < costs.Length; day++)
        {
            // If we choose Pasta today:
            // Yesterday cannot also be Pasta.
            // Therefore, the previous day must have ended with either Salad or Soup.
            // We take the cheaper of those two valid previous states, then add today's Pasta cost.
            int currentPasta = costs[day][0] + Math.Min(prevSalad, prevSoup);

            // If we choose Salad today:
            // Yesterday cannot be Salad.
            // So yesterday must have ended with Pasta or Soup.
            // Again, choose the cheaper valid previous state and add today's Salad cost.
            int currentSalad = costs[day][1] + Math.Min(prevPasta, prevSoup);

            // If we choose Soup today:
            // Yesterday cannot be Soup.
            // So yesterday must have ended with Pasta or Salad.
            // Choose the cheaper valid previous state and add today's Soup cost.
            int currentSoup = costs[day][2] + Math.Min(prevPasta, prevSalad);

            // After computing today's three DP values, we move the window forward:
            // today's values become "previous day" values for the next iteration.
            //
            // This is why O(1) extra space works:
            // each day depends only on the immediately previous day's 3 values.
            prevPasta = currentPasta;
            prevSalad = currentSalad;
            prevSoup = currentSoup;
        }

        // At the end, the final day could end with any of the three meals.
        // So the answer is the minimum among the three possible ending states.
        return Math.Min(prevPasta, Math.Min(prevSalad, prevSoup));
    }
}

// Demo code

var solution = new Solution();

// Example 1
// costs = [[5,9,4],[8,7,6],[3,5,10]]
// Day 0: Pasta=5, Salad=9, Soup=4
// Day 1:
//   Pasta = 8 + min(9,4) = 12
//   Salad = 7 + min(5,4) = 11
//   Soup  = 6 + min(5,9) = 11
// Day 2:
//   Pasta = 3 + min(11,11) = 14
//   Salad = 5 + min(12,11) = 16
//   Soup  = 10 + min(12,11) = 21
// Answer = min(14,16,21) = 14
int[][] costs1 =
{
    new[] { 5, 9, 4 },
    new[] { 8, 7, 6 },
    new[] { 3, 5, 10 }
};

int result1 = solution.MinMealPlanCost(costs1);
Console.WriteLine(result1); // Expected: 14

// Example 2
// costs = [[7,2,8],[6,4,3],[5,9,1],[2,7,6]]
//
// Day 0:
//   Pasta=7, Salad=2, Soup=8
//
// Day 1:
//   Pasta = 6 + min(2,8) = 8
//   Salad = 4 + min(7,8) = 11
//   Soup  = 3 + min(7,2) = 5
//
// Day 2:
//   Pasta = 5 + min(11,5) = 10
//   Salad = 9 + min(8,5) = 14
//   Soup  = 1 + min(8,11) = 9
//
// Day 3:
//   Pasta = 2 + min(14,9) = 11
//   Salad = 7 + min(10,9) = 16
//   Soup  = 6 + min(10,14) = 16
//
// Answer = min(11,16,16) = 11
//
// Note:
// The problem statement's listed "Output: 8" is inconsistent with its own explanation.
// The explanation correctly concludes the minimum is 11.
// Our algorithm correctly computes 11.
int[][] costs2 =
{
    new[] { 7, 2, 8 },
    new[] { 6, 4, 3 },
    new[] { 5, 9, 1 },
    new[] { 2, 7, 6 }
};

int result2 = solution.MinMealPlanCost(costs2);
Console.WriteLine(result2); // Expected: 11

// Additional small sanity check
int[][] costs3 =
{
    new[] { 1, 100, 100 }
};

int result3 = solution.MinMealPlanCost(costs3);
Console.WriteLine(result3); // Expected: 1