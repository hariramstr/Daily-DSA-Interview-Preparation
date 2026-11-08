/*
Title: Minimum Fatigue to Schedule Alternating Presentation Themes

Problem Description:
A conference organizer is preparing a sequence of n presentation slots. For each slot i,
there are exactly three candidate talks available: a Technical talk, a Product talk,
and a Research talk. Choosing candidate type t for slot i adds fatigue cost cost[i][t].

The organizer wants the final schedule to feel varied. A schedule is considered valid
if it satisfies both rules:
1. No two consecutive slots use the same theme.
2. For every three consecutive scheduled slots, the three themes cannot be all distinct.
   In other words, among any window of length 3, at least one theme must repeat.

Your task is to return the minimum total fatigue needed to fill all n slots with a valid
schedule. If no valid schedule exists, return -1.

This is not a greedy problem, because a locally cheap theme choice may force expensive
or impossible choices later. You must reason about the recent history of chosen themes
and compute the optimal total cost.

Input format:
- An integer n
- A 2D integer array cost of size n x 3
- Theme indices are fixed as 0 = Technical, 1 = Product, 2 = Research

Constraints:
- 1 <= n <= 200000
- cost.length == n
- cost[i].length == 3
- 1 <= cost[i][t] <= 10^9
- The answer may exceed 32-bit integer range

Important observation:
- Rule 1 says adjacent themes must be different.
- Rule 2 says in any length-3 window, the three themes cannot all be distinct.

If we look at three consecutive chosen themes (a, b, c):
- Because of Rule 1, we already know a != b and b != c.
- Therefore, for the triple to NOT be all distinct, we must have a == c.

So for every i >= 2, the chosen theme at position i must equal the chosen theme at position i - 2.
That means every valid schedule is forced into an alternating pattern:
x, y, x, y, x, y, ...
with x != y.

This reduces the problem to trying all ordered pairs (x, y) with x != y,
computing the total cost of the alternating sequence starting with x at index 0 and y at index 1,
and taking the minimum.

Examples:
- n = 1: choose the cheapest of the 3 themes.
- n = 2: choose any pair of different themes with minimum total cost.
- n = 3: the sequence must be x, y, x. If no such choice is considered, answer is -1.
  In fact, there is always some x != y, so a valid schedule exists for n >= 1.

Example 1:
cost = [[3,2,7],[5,1,4],[2,6,3],[8,2,5]]
Try pattern (0,1): sequence [0,1,0,1]
Cost = 3 + 1 + 2 + 2 = 8
This is valid and is optimal, so answer = 8.

Example 2:
cost = [[1,10,10],[10,1,10],[10,10,1]]
Try all ordered pairs:
(0,1) => [0,1,0] cost = 1 + 1 + 10 = 12
(0,2) => [0,2,0] cost = 1 + 10 + 10 = 21
(1,0) => [1,0,1] cost = 10 + 10 + 10 = 30
(1,2) => [1,2,1] cost = 10 + 10 + 10 = 30
(2,0) => [2,0,2] cost = 10 + 10 + 1 = 21
(2,1) => [2,1,2] cost = 10 + 1 + 1 = 12
Minimum = 12, so the correct answer is 12.
The sample statement's -1 is incorrect because sequences like [0,1,0] are valid.

*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - There are only 6 ordered pairs of distinct themes: (0,1), (0,2), (1,0), (1,2), (2,0), (2,1).
    - For each pair, we scan all n slots once to compute the total cost.
    - Therefore total time is O(6 * n) = O(n).

    Space Complexity:
    - We use only a few variables regardless of n.
    - Therefore space complexity is O(1).
    */
    public long MinimumFatigue(int n, int[][] cost)
    {
        // -----------------------------
        // Step 1: Handle the smallest possible input size.
        // -----------------------------
        // If there is only one slot, there are no adjacency constraints and no length-3 windows.
        // So any single theme is valid.
        // We simply choose the cheapest theme among the three options for slot 0.
        if (n == 1)
        {
            long bestSingle = Math.Min(cost[0][0], Math.Min(cost[0][1], cost[0][2]));
            return bestSingle;
        }

        // -----------------------------
        // Step 2: Prepare to test all valid alternating patterns.
        // -----------------------------
        // From the rules, every valid schedule must alternate between two different themes:
        // x, y, x, y, x, y, ...
        //
        // Why?
        // - Adjacent themes must differ, so if we have ... a, b, c ...
        //   then a != b and b != c.
        // - The triple (a, b, c) cannot be all distinct.
        // - Since a != b and b != c already, the only way to avoid all three being distinct
        //   is to force a == c.
        // - Therefore each theme must repeat every two positions.
        //
        // Since there are only 3 themes, the number of ordered pairs (x, y) with x != y is 6.
        // We will try each pair and compute the total cost of the corresponding alternating sequence.
        long answer = long.MaxValue;

        // -----------------------------
        // Step 3: Enumerate the first theme x.
        // -----------------------------
        // This theme will be used at positions 0, 2, 4, ...
        for (int firstTheme = 0; firstTheme < 3; firstTheme++)
        {
            // -----------------------------
            // Step 4: Enumerate the second theme y.
            // -----------------------------
            // This theme will be used at positions 1, 3, 5, ...
            for (int secondTheme = 0; secondTheme < 3; secondTheme++)
            {
                // The two alternating themes must be different,
                // otherwise adjacent positions would be equal and violate Rule 1.
                if (firstTheme == secondTheme)
                {
                    continue;
                }

                // -----------------------------
                // Step 5: Compute the total cost for this specific alternating pattern.
                // -----------------------------
                // Pattern:
                // index 0 -> firstTheme
                // index 1 -> secondTheme
                // index 2 -> firstTheme
                // index 3 -> secondTheme
                // ...
                //
                // We use long because costs can be up to 1e9 and n can be up to 2e5,
                // so the sum can be much larger than a 32-bit integer.
                long currentTotal = 0;

                for (int i = 0; i < n; i++)
                {
                    // If i is even, use firstTheme.
                    // If i is odd, use secondTheme.
                    int chosenTheme = (i % 2 == 0) ? firstTheme : secondTheme;

                    // Add the fatigue cost of choosing that theme for slot i.
                    currentTotal += cost[i][chosenTheme];
                }

                // -----------------------------
                // Step 6: Keep the minimum over all 6 possible alternating patterns.
                // -----------------------------
                if (currentTotal < answer)
                {
                    answer = currentTotal;
                }
            }
        }

        // -----------------------------
        // Step 7: Return the best answer found.
        // -----------------------------
        // For this problem, as long as n >= 1, there is always at least one valid schedule:
        // - n = 1: any single theme
        // - n >= 2: choose any two different themes and alternate
        //
        // So answer will always be updated from long.MaxValue.
        // Still, returning -1 in the impossible case is a safe defensive pattern.
        return answer == long.MaxValue ? -1 : answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 4;
int[][] cost1 =
{
    new[] { 3, 2, 7 },
    new[] { 5, 1, 4 },
    new[] { 2, 6, 3 },
    new[] { 8, 2, 5 }
};
Console.WriteLine(solution.MinimumFatigue(n1, cost1)); // Expected: 8

// Example 2
// Note: The problem statement claims -1, but that claim is incorrect.
// A valid schedule [0,1,0] exists with total cost 12.
int n2 = 3;
int[][] cost2 =
{
    new[] { 1, 10, 10 },
    new[] { 10, 1, 10 },
    new[] { 10, 10, 1 }
};
Console.WriteLine(solution.MinimumFatigue(n2, cost2)); // Correct result: 12

// Additional small demos

// n = 1
int n3 = 1;
int[][] cost3 =
{
    new[] { 9, 4, 7 }
};
Console.WriteLine(solution.MinimumFatigue(n3, cost3)); // Expected: 4

// n = 2
int n4 = 2;
int[][] cost4 =
{
    new[] { 5, 1, 9 },
    new[] { 2, 8, 3 }
};
Console.WriteLine(solution.MinimumFatigue(n4, cost4)); // Expected: 3 via themes [1,0]