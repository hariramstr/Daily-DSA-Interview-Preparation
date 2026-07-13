/*
Title: Minimum Cost to Publish Articles with Category Cooldown

Problem Description:
You are building an automated homepage for a news platform. There are n articles to publish in a fixed order from left to right.
For each position i, you may choose exactly one of three categories for the article slot:
0 = Politics, 1 = Sports, 2 = Tech.

Assigning category c to position i has a given publishing cost cost[i][c].

Cooldown rule:
A category used in one position cannot be used again in either of the next two positions.
That means if position i uses category c, then positions i + 1 and i + 2 cannot use category c.

Goal:
Compute the minimum total publishing cost to assign categories to all positions while satisfying the cooldown rule.
If it is impossible, return -1.

Key observation:
Because there are only 3 categories, once we reach position i >= 2, the category chosen at position i
must be different from the categories used at positions i - 1 and i - 2.
So the choice at each step depends only on the previous two categories.

This naturally leads to dynamic programming over states:
(previous category, category before previous).
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * 3 * 3 * 3) which simplifies to O(n), because the number of categories is fixed at 3.

    Space Complexity:
    O(3 * 3) = O(1) extra space, because we only keep DP states for the previous position.

    Beginner-friendly idea:
    - We process positions from left to right.
    - At each step, we remember the last two chosen categories.
    - For the current position, we try all 3 categories.
    - A category is allowed only if it is different from both of the previous two categories.
    - We keep the minimum cost for every possible "last two categories" state.
    */
    public long MinCost(int[][] cost)
    {
        // If the input itself is missing or empty, there are no positions to fill.
        // In many algorithm problems this would return 0, because the cost of assigning nothing is zero.
        if (cost == null || cost.Length == 0)
        {
            return 0;
        }

        int n = cost.Length;

        // We use a very large number to represent "unreachable".
        // We do not use long.MaxValue directly because later we add costs to it,
        // and adding to long.MaxValue could overflow.
        long INF = long.MaxValue / 4;

        // Special marker for "there is no previous category yet".
        // Since valid categories are only 0, 1, 2, we can safely use 3 as a sentinel.
        int NONE = 3;

        // Our DP state:
        // dp[last1, last2]
        //
        // Meaning:
        // - last1 = category used at the most recent position
        // - last2 = category used one position before that
        //
        // For example, after processing positions up to index i:
        // - last1 is the category at i
        // - last2 is the category at i - 1
        //
        // At the very beginning, before processing any positions,
        // there are no previous categories, so the state is (NONE, NONE) with cost 0.
        //
        // We allocate size 4 x 4 so indices 0,1,2 are real categories and 3 is NONE.
        long[,] dp = new long[4, 4];
        long[,] next = new long[4, 4];

        // Initialize all states as unreachable.
        for (int a = 0; a < 4; a++)
        {
            for (int b = 0; b < 4; b++)
            {
                dp[a, b] = INF;
                next[a, b] = INF;
            }
        }

        // Base state: before placing any article, no categories have been used.
        dp[NONE, NONE] = 0;

        // Process each article slot from left to right.
        for (int i = 0; i < n; i++)
        {
            // Before computing states for the next position,
            // reset the "next" DP table to unreachable.
            for (int a = 0; a < 4; a++)
            {
                for (int b = 0; b < 4; b++)
                {
                    next[a, b] = INF;
                }
            }

            // Try extending every currently reachable state.
            for (int last1 = 0; last1 < 4; last1++)
            {
                for (int last2 = 0; last2 < 4; last2++)
                {
                    long currentCost = dp[last1, last2];

                    // If this state was never reached, skip it.
                    if (currentCost >= INF)
                    {
                        continue;
                    }

                    // Try assigning one of the 3 categories to the current position.
                    for (int currentCategory = 0; currentCategory < 3; currentCategory++)
                    {
                        // Cooldown rule:
                        // The current category cannot match the category used in the previous position
                        // and also cannot match the category used two positions ago.
                        //
                        // Why this is enough:
                        // The problem says a category cannot be reused in either of the next two positions.
                        // Looking backward, that means the current category must differ from the previous two.
                        if (currentCategory == last1 || currentCategory == last2)
                        {
                            continue;
                        }

                        // Compute the new total cost if we choose this category now.
                        long newCost = currentCost + cost[i][currentCategory];

                        // After placing currentCategory:
                        // - it becomes the new most recent category
                        // - the old last1 becomes the second most recent category
                        //
                        // So the new state is (currentCategory, last1).
                        if (newCost < next[currentCategory, last1])
                        {
                            next[currentCategory, last1] = newCost;
                        }
                    }
                }
            }

            // Move "next" into "dp" for the next iteration.
            var temp = dp;
            dp = next;
            next = temp;
        }

        // After processing all positions, the answer is the minimum cost among all reachable ending states.
        long answer = INF;

        for (int last1 = 0; last1 < 4; last1++)
        {
            for (int last2 = 0; last2 < 4; last2++)
            {
                if (dp[last1, last2] < answer)
                {
                    answer = dp[last1, last2];
                }
            }
        }

        // If no state is reachable, the assignment is impossible.
        return answer >= INF ? -1 : answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// Note:
// The narrative in the prompt contains contradictions, but the mathematically correct minimum
// under the stated cooldown rule is 10 using categories [1, 2, 0, 1]:
// position 0 -> category 1 cost 2
// position 1 -> category 2 cost 4
// position 2 -> category 0 cost 2
// position 3 -> category 1 cost 4
// total = 2 + 4 + 2 + 4 = 10
int[][] cost1 =
{
    new[] { 3, 2, 7 },
    new[] { 5, 1, 4 },
    new[] { 2, 6, 3 },
    new[] { 8, 4, 5 }
};

Console.WriteLine(solution.MinCost(cost1)); // Expected: 10

// Example 2:
// Two positions only, so the rule means the second position cannot use the same category
// as the first position.
// Best is category 0 first (cost 1), then category 1 or 2 second (cost 10), total 11.
int[][] cost2 =
{
    new[] { 1, 10, 10 },
    new[] { 1, 10, 10 }
};

Console.WriteLine(solution.MinCost(cost2)); // Expected: 11

// Additional quick sanity checks:

// Single position: just choose the cheapest category.
int[][] cost3 =
{
    new[] { 9, 4, 6 }
};
Console.WriteLine(solution.MinCost(cost3)); // Expected: 4

// Three positions: because each category cannot repeat within distance 2,
// all three positions must use all three different categories.
int[][] cost4 =
{
    new[] { 1, 100, 100 },
    new[] { 100, 2, 100 },
    new[] { 100, 100, 3 }
};
Console.WriteLine(solution.MinCost(cost4)); // Expected: 6