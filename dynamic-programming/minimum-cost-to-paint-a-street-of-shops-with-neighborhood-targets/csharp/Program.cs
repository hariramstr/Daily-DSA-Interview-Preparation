/*
Title: Minimum Cost to Paint a Street of Shops with Neighborhood Targets

Problem Description:
A city planning team is repainting a straight street of shops. There are n shops in order from left to right,
and each shop must end up painted in exactly one of m colors. Some shops are already painted and cannot be changed,
while others are unpainted and may be painted at a given cost.

A neighborhood is defined as a maximal contiguous group of shops painted the same color.
For example:
- colors [2, 2, 3, 3, 1] form 3 neighborhoods
- colors [1, 2, 1] form 3 neighborhoods because adjacent colors differ at every position

You are given:
- an integer array shops of length n, where shops[i] = 0 means the i-th shop is unpainted,
  and shops[i] in [1, m] means it is already painted with that color
- a 2D integer array cost where cost[i][c - 1] is the cost to paint shop i with color c if shops[i] is unpainted
- an integer target representing the exact number of neighborhoods required after all shops are painted

Return the minimum total painting cost to achieve exactly target neighborhoods.
If it is impossible, return -1.

This is a dynamic programming problem because the best choice for each shop depends on:
- the color chosen for the previous shop
- how many neighborhoods have already been formed
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * target * m * m)

    Explanation:
    - We process each of the n shops.
    - For each shop, we consider every possible neighborhood count from 1 to target.
    - For each state, we consider every current color (m choices).
    - To transition, we may compare against every previous color (m choices).

    Since n <= 100 and m <= 20, this is acceptable.

    Space Complexity:
    O(target * m)

    Explanation:
    - We only keep the previous row of DP and the current row of DP.
    - Each row stores states for:
      neighborhood count from 0..target
      and color from 1..m
    */
    public int MinCost(int[] shops, int[][] cost, int m, int target)
    {
        int n = shops.Length;

        // We use a very large number to represent an impossible state.
        // We do not use int.MaxValue directly because later we may add painting cost,
        // and adding to int.MaxValue could overflow.
        const int INF = 1_000_000_000;

        // prev[k, c] means:
        // After processing the previous shop,
        // the minimum total cost to form exactly k neighborhoods
        // and end with color c.
        //
        // Color c is stored in the range 1..m.
        // We keep index 0 unused for color to make the code easier to read.
        int[,] prev = new int[target + 1, m + 1];
        int[,] curr = new int[target + 1, m + 1];

        // Initialize all previous states as impossible.
        for (int k = 0; k <= target; k++)
        {
            for (int c = 0; c <= m; c++)
            {
                prev[k, c] = INF;
                curr[k, c] = INF;
            }
        }

        // Before processing any shop:
        // We have formed 0 neighborhoods and there is no ending color yet.
        //
        // We do not store "no color" as a normal DP color state.
        // Instead, we handle the first shop separately inside the loop by checking i == 0.
        // So no explicit prev[0, something] base color is needed here.

        // Process shops from left to right.
        for (int i = 0; i < n; i++)
        {
            // Reset current DP table to impossible before filling it for shop i.
            for (int k = 0; k <= target; k++)
            {
                for (int c = 0; c <= m; c++)
                {
                    curr[k, c] = INF;
                }
            }

            // Determine which colors are allowed for the current shop.
            //
            // If the shop is already painted, only that one color is allowed.
            // If the shop is unpainted, we may choose any color from 1..m.
            int startColor = shops[i] == 0 ? 1 : shops[i];
            int endColor = shops[i] == 0 ? m : shops[i];

            // Try every possible color for the current shop.
            for (int color = startColor; color <= endColor; color++)
            {
                // Painting cost for this shop with this chosen color:
                // - 0 if already painted
                // - cost[i][color - 1] if unpainted
                int paintCost = shops[i] == 0 ? cost[i][color - 1] : 0;

                // We can never have more neighborhoods than shops processed so far.
                // But using target as the upper bound is enough and simpler.
                for (int neighborhoods = 1; neighborhoods <= target; neighborhoods++)
                {
                    if (i == 0)
                    {
                        // Special handling for the first shop:
                        //
                        // The first painted shop always creates exactly 1 neighborhood,
                        // because there is no previous shop to compare with.
                        //
                        // Therefore:
                        // - if neighborhoods == 1, this state is reachable
                        // - otherwise, it is impossible
                        if (neighborhoods == 1)
                        {
                            curr[1, color] = Math.Min(curr[1, color], paintCost);
                        }

                        continue;
                    }

                    // For shops after the first one, we transition from all possible previous colors.
                    for (int prevColor = 1; prevColor <= m; prevColor++)
                    {
                        // If the previous state is impossible, skip it.
                        if (prev[neighborhoods, prevColor] == INF && prev[neighborhoods - 1, prevColor] == INF)
                        {
                            // This quick check avoids some unnecessary work.
                            // We still need the detailed logic below, but if both relevant states
                            // are impossible for this prevColor, there is nothing to do.
                        }

                        if (prevColor == color)
                        {
                            // Case 1: current color is the same as previous color
                            //
                            // Why this matters:
                            // If two adjacent shops have the same color,
                            // they belong to the same neighborhood.
                            //
                            // Therefore, the number of neighborhoods does NOT increase.
                            //
                            // Transition:
                            // prev[neighborhoods, prevColor] -> curr[neighborhoods, color]
                            if (prev[neighborhoods, prevColor] != INF)
                            {
                                curr[neighborhoods, color] = Math.Min(
                                    curr[neighborhoods, color],
                                    prev[neighborhoods, prevColor] + paintCost
                                );
                            }
                        }
                        else
                        {
                            // Case 2: current color is different from previous color
                            //
                            // Why this matters:
                            // If adjacent shops have different colors,
                            // the current shop starts a NEW neighborhood.
                            //
                            // Therefore, the number of neighborhoods increases by 1.
                            //
                            // To end up with "neighborhoods" now,
                            // we must have had "neighborhoods - 1" before.
                            //
                            // Transition:
                            // prev[neighborhoods - 1, prevColor] -> curr[neighborhoods, color]
                            if (neighborhoods > 1 && prev[neighborhoods - 1, prevColor] != INF)
                            {
                                curr[neighborhoods, color] = Math.Min(
                                    curr[neighborhoods, color],
                                    prev[neighborhoods - 1, prevColor] + paintCost
                                );
                            }
                        }
                    }
                }
            }

            // Move current results into prev for the next iteration.
            //
            // We swap references instead of copying every value manually.
            // This is a common space optimization in dynamic programming
            // when each row depends only on the previous row.
            var temp = prev;
            prev = curr;
            curr = temp;
        }

        // After processing all shops, we need exactly "target" neighborhoods.
        // The final shop may end with any color, so we take the minimum over all ending colors.
        int answer = INF;
        for (int color = 1; color <= m; color++)
        {
            answer = Math.Min(answer, prev[target, color]);
        }

        // If the answer is still INF, no valid painting plan exists.
        return answer == INF ? -1 : answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] shops1 = { 0, 0, 0, 0 };
int[][] cost1 =
{
    new[] { 1, 5 },
    new[] { 4, 1 },
    new[] { 1, 3 },
    new[] { 2, 1 }
};
int m1 = 2;
int target1 = 2;
int result1 = solution.MinCost(shops1, cost1, m1, target1);
Console.WriteLine(result1); // Expected: 4

// Example 2
// Note:
// The problem statement text contains an inconsistency in its explanation.
// We must compute the true minimum from the provided arrays.
int[] shops2 = { 1, 0, 2, 0, 0 };
int[][] cost2 =
{
    new[] { 3, 9, 4 },
    new[] { 2, 1, 7 },
    new[] { 8, 5, 6 },
    new[] { 4, 3, 2 },
    new[] { 7, 6, 1 }
};
int m2 = 3;
int target2 = 3;
int result2 = solution.MinCost(shops2, cost2, m2, target2);
Console.WriteLine(result2);

// Additional demo: impossible case
int[] shops3 = { 1, 1, 1 };
int[][] cost3 =
{
    new[] { 1, 1 },
    new[] { 1, 1 },
    new[] { 1, 1 }
};
int m3 = 2;
int target3 = 2;
int result3 = solution.MinCost(shops3, cost3, m3, target3);
Console.WriteLine(result3); // Expected: -1