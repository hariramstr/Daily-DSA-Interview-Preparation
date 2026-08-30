/*
Title: Minimum Cost to Reach the Office With 1-Step or 2-Step Elevators
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are in a building lobby and want to reach the office on floor n. The building has a special elevator system:
from floor i, you may move either 1 floor up or 2 floors up. However, each floor has an entry fee charged when
you land on it. You are given an integer array cost where cost[i] is the fee to land on floor i + 1.

Your goal is to reach exactly floor n while paying the minimum total fee.

You start before floor 1, so no fee is paid at the beginning. If you jump directly to a floor, you pay only for
the floor where you land. For example, from the lobby you may go to floor 1 and pay cost[0], or go directly to
floor 2 and pay cost[1].

Return the minimum total fee needed to reach floor n.

Dynamic Programming Idea:
Let dp[i] represent the minimum cost to reach floor i (using 1-based floor numbering).
Then:
- To reach floor i, you must come either from floor i - 1 or floor i - 2.
- So the cheapest way to reach floor i is:
      dp[i] = min(dp[i - 1], dp[i - 2]) + fee of floor i

Because arrays in C# are 0-based:
- floor 1 has fee cost[0]
- floor 2 has fee cost[1]
- ...
- floor n has fee cost[n - 1]

Examples:
1) cost = [4, 2, 7, 3]
   Best path: lobby -> floor 2 -> floor 4
   Total = 2 + 3 = 5

2) cost = [1, 100, 1, 1, 100, 1]
   Valid optimal path:
   lobby -> floor 1 -> floor 3 -> floor 4 -> floor 6
   Total = 1 + 1 + 1 + 1 = 4

Constraints:
- 1 <= cost.length <= 1000
- 1 <= cost[i] <= 1000
- n = cost.length
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each floor exactly once.
    - Each floor requires only constant-time work.

    Space Complexity: O(n)
    - We use a dp array of size n + 1 to store the minimum cost for each floor.
    - This is helpful for learning because it makes the state definition very clear.
    */
    public int MinCostToReachOffice(int[] cost)
    {
        // Step 1:
        // Read the total number of floors.
        // The problem states that n is exactly cost.Length.
        // If cost has length 4, that means the office is on floor 4.
        int n = cost.Length;

        // Step 2:
        // Handle the smallest possible case directly.
        // If there is only one floor, the only way to reach it is to land on floor 1,
        // so we must pay cost[0].
        if (n == 1)
        {
            return cost[0];
        }

        // Step 3:
        // Create a DP array where:
        // dp[i] = minimum total fee needed to reach floor i
        //
        // Important detail:
        // We will use 1-based floor numbering in dp for clarity:
        // dp[1] = minimum cost to reach floor 1
        // dp[2] = minimum cost to reach floor 2
        // ...
        // dp[n] = answer
        //
        // We allocate n + 1 so that indices 1 through n are valid.
        int[] dp = new int[n + 1];

        // Step 4:
        // Initialize the base cases.
        //
        // Base case for floor 1:
        // From the lobby, we can jump directly to floor 1 and pay its fee.
        // So the minimum cost to reach floor 1 is simply cost[0].
        dp[1] = cost[0];

        // Base case for floor 2:
        // From the lobby, we can also jump directly to floor 2 and pay only cost[1].
        // Since the problem allows starting with either a 1-floor or 2-floor move,
        // the cheapest way to reach floor 2 is NOT necessarily via floor 1.
        // It is simply the direct landing cost on floor 2.
        dp[2] = cost[1];

        // Step 5:
        // Fill the DP table from floor 3 up to floor n.
        //
        // Why start at 3?
        // Because floors 1 and 2 are already known from the base cases.
        //
        // For each floor i:
        // - We can arrive from floor i - 1 by taking a 1-floor move.
        // - Or we can arrive from floor i - 2 by taking a 2-floor move.
        //
        // Since we want the minimum total fee, we choose the cheaper of those two previous states,
        // then add the fee for landing on the current floor.
        for (int i = 3; i <= n; i++)
        {
            // The fee for floor i is stored at cost[i - 1]
            // because cost uses 0-based indexing while floors are 1-based.
            int currentFloorFee = cost[i - 1];

            // Option 1: come from the previous floor (i - 1)
            int costIfComingFromOneFloorBelow = dp[i - 1] + currentFloorFee;

            // Option 2: come from two floors below (i - 2)
            int costIfComingFromTwoFloorsBelow = dp[i - 2] + currentFloorFee;

            // Choose the cheaper of the two valid ways to reach this floor.
            dp[i] = Math.Min(costIfComingFromOneFloorBelow, costIfComingFromTwoFloorsBelow]);
        }

        // Step 6:
        // The answer is the minimum cost to reach exactly floor n.
        return dp[n];
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1
int[] cost1 = { 4, 2, 7, 3 };
int result1 = solution.MinCostToReachOffice(cost1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] cost2 = { 1, 100, 1, 1, 100, 1 };
int result2 = solution.MinCostToReachOffice(cost2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional small test: single floor
int[] cost3 = { 9 };
int result3 = solution.MinCostToReachOffice(cost3);
Console.WriteLine("Single Floor Result: " + result3); // Expected: 9

// Additional test: two floors
int[] cost4 = { 8, 3 };
int result4 = solution.MinCostToReachOffice(cost4);
Console.WriteLine("Two Floors Result: " + result4); // Expected: 3