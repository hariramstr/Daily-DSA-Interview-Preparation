/*
Title: Minimum Energy to Paint Fence Posts

Problem Description:
You are repainting a straight fence with n posts. Each post must be painted either red, blue, or green.
The cost of painting post i with a given color is provided in a 2D array costs, where:
- costs[i][0] is the cost for red
- costs[i][1] is the cost for blue
- costs[i][2] is the cost for green

For appearance reasons, no two adjacent fence posts are allowed to have the same color.

Your task is to return the minimum total energy cost needed to paint all posts while following this rule.

This is a classic dynamic programming problem:
For each post and each color, we only need to know the minimum total cost of painting the previous post
with one of the other two colors.

Constraints:
- 1 <= n <= 1000
- costs.length == n
- costs[i].length == 3
- 1 <= costs[i][j] <= 10^4

Example 1:
Input: costs = [[1,5,3],[2,9,4]]
Output: 5
Explanation:
- Paint post 0 red for 1
- Paint post 1 green for 4
Total = 5

Example 2:
Input: costs = [[7,6,2],[5,8,4],[3,9,1],[6,2,7]]
Output: 10
Explanation:
One optimal painting is:
- post 0 -> green = 2
- post 1 -> red   = 5
- post 2 -> green = 1
- post 3 -> blue  = 2
Total = 10
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each post exactly once.
    - For each post, we compute 3 values (red, blue, green), each in constant time.

    Space Complexity: O(1)
    - We do not need a full DP table.
    - We only keep track of the previous post's best costs for the 3 colors.
    */
    public int MinCost(int[][] costs)
    {
        // Step 1:
        // Handle edge cases defensively.
        // The problem guarantees at least 1 post, but checking for null or empty input
        // makes the method safer and easier to understand in real-world code.
        if (costs == null || costs.Length == 0)
        {
            return 0;
        }

        // Step 2:
        // Initialize the dynamic programming state using the first post.
        //
        // Meaning of these variables:
        // - prevRed   = minimum total cost to paint all posts up to the current point,
        //               where the most recent post is painted red
        // - prevBlue  = same idea, but the most recent post is blue
        // - prevGreen = same idea, but the most recent post is green
        //
        // For the very first post, the minimum cost for each color is simply the direct
        // painting cost of that first post, because there is no previous post to worry about.
        int prevRed = costs[0][0];
        int prevBlue = costs[0][1];
        int prevGreen = costs[0][2];

        // Step 3:
        // Process each remaining post from left to right.
        //
        // Why left to right?
        // Because the rule only depends on the previous adjacent post.
        // That means when deciding the best way to paint post i, we only need information
        // from post i - 1. This is exactly what makes dynamic programming efficient here.
        for (int i = 1; i < costs.Length; i++)
        {
            // Step 3a:
            // Compute the minimum total cost if the current post i is painted red.
            //
            // Since adjacent posts cannot have the same color, if current is red,
            // previous must be either blue or green.
            //
            // So:
            // currentRed = cost to paint current post red
            //            + minimum of:
            //              - previous total cost ending in blue
            //              - previous total cost ending in green
            int currentRed = costs[i][0] + Math.Min(prevBlue, prevGreen);

            // Step 3b:
            // Compute the minimum total cost if the current post i is painted blue.
            //
            // If current is blue, previous must be red or green.
            int currentBlue = costs[i][1] + Math.Min(prevRed, prevGreen);

            // Step 3c:
            // Compute the minimum total cost if the current post i is painted green.
            //
            // If current is green, previous must be red or blue.
            int currentGreen = costs[i][2] + Math.Min(prevRed, prevBlue);

            // Step 3d:
            // Move the "current" results into the "previous" variables.
            //
            // Why do this?
            // Because on the next iteration, today's current post becomes tomorrow's previous post.
            //
            // This is the key reason we only need O(1) extra space:
            // we do not store all rows of a DP table, only the last row.
            prevRed = currentRed;
            prevBlue = currentBlue;
            prevGreen = currentGreen;
        }

        // Step 4:
        // After processing all posts, the answer is the minimum among the three possibilities
        // for the last post's color.
        //
        // Why?
        // Because the fence is fully painted, and we do not care what color the final post is,
        // only that the total cost is as small as possible.
        return Math.Min(prevRed, Math.Min(prevBlue, prevGreen));
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] costs1 =
{
    new[] { 1, 5, 3 },
    new[] { 2, 9, 4 }
};

int result1 = solution.MinCost(costs1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[][] costs2 =
{
    new[] { 7, 6, 2 },
    new[] { 5, 8, 4 },
    new[] { 3, 9, 1 },
    new[] { 6, 2, 7 }
};

int result2 = solution.MinCost(costs2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 10

// Additional demo: single post
int[][] costs3 =
{
    new[] { 8, 3, 6 }
};

int result3 = solution.MinCost(costs3);
Console.WriteLine("Single Post Result: " + result3); // Expected: 3