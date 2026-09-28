/*
Title: Minimum Cost to Schedule Factory Robots with Mode Switch Fees

Problem Description:
A factory has n production hours to fill. For each hour i, exactly one robot mode must be active: mode A or mode B.
Running mode A during hour i costs aCost[i], and running mode B during hour i costs bCost[i].
In addition, switching the active mode between two consecutive hours has an extra fee switchFee.
If the same mode is used in consecutive hours, no switch fee is charged.

Your task is to compute the minimum total cost to schedule all n hours.

More formally, choose a sequence of modes of length n where each entry is either A or B.
The total cost is the sum of the operating cost of the chosen mode at each hour,
plus switchFee for every index i > 0 where the mode at hour i differs from the mode at hour i - 1.

Return the minimum possible total cost.

This is a dynamic programming problem because the best choice for the current hour
depends on the mode chosen in the previous hour. An efficient solution should run in O(n) time.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We only scan through the arrays once, and for each hour we keep track of just two values:
    - the minimum total cost if the current hour ends in mode A
    - the minimum total cost if the current hour ends in mode B

    This is a classic dynamic programming optimization:
    instead of storing answers for all hours, we only keep the previous hour's results,
    because the current hour depends only on the immediately previous hour.
    */
    public long MinimumCost(int[] aCost, int[] bCost, int switchFee)
    {
        // Defensive validation.
        // The problem guarantees valid input, but this makes the method safer and clearer.
        if (aCost == null || bCost == null)
            throw new ArgumentNullException("Input arrays cannot be null.");

        if (aCost.Length != bCost.Length)
            throw new ArgumentException("aCost and bCost must have the same length.");

        if (aCost.Length == 0)
            return 0;

        int n = aCost.Length;

        // dpA means:
        // "Minimum total cost to schedule hours 0..i, with hour i using mode A"
        //
        // dpB means:
        // "Minimum total cost to schedule hours 0..i, with hour i using mode B"
        //
        // For hour 0, there is no previous hour, so there is no switch fee yet.
        // We simply pay the operating cost of whichever mode we choose.
        long dpA = aCost[0];
        long dpB = bCost[0];

        // Process each remaining hour one by one.
        for (int i = 1; i < n; i++)
        {
            // To compute the new cost for ending in mode A at hour i,
            // there are exactly two possibilities:
            //
            // 1) We were already in mode A at hour i - 1
            //    -> no switch fee
            //    -> total cost = previous dpA + aCost[i]
            //
            // 2) We were in mode B at hour i - 1 and switch to A now
            //    -> pay switchFee
            //    -> total cost = previous dpB + switchFee + aCost[i]
            //
            // We choose the cheaper of these two possibilities.
            long nextA = Math.Min(
                dpA + aCost[i],
                dpB + switchFee + aCost[i]
            );

            // Similarly, to compute the new cost for ending in mode B at hour i:
            //
            // 1) Stay in B
            //    -> no switch fee
            //    -> total cost = previous dpB + bCost[i]
            //
            // 2) Switch from A to B
            //    -> pay switchFee
            //    -> total cost = previous dpA + switchFee + bCost[i]
            //
            // Again, we choose the cheaper option.
            long nextB = Math.Min(
                dpB + bCost[i],
                dpA + switchFee + bCost[i]
            );

            // Move the DP window forward:
            // the "current" results become the "previous" results for the next iteration.
            dpA = nextA;
            dpB = nextB;
        }

        // After processing all hours, the schedule may end in either mode A or mode B.
        // We return whichever final total cost is smaller.
        return Math.Min(dpA, dpB);
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// Important note:
// The prompt's narrative contains inconsistent arithmetic,
// but the correct minimum for these arrays with switchFee = 3 is 12.
int[] aCost1 = { 3, 8, 2, 5 };
int[] bCost1 = { 4, 1, 6, 1 };
int switchFee1 = 3;

long result1 = solution.MinimumCost(aCost1, bCost1, switchFee1);
Console.WriteLine(result1); // Expected: 12

// Example 2 from the prompt.
// Again, the prompt's stated output says 9, but its own explanation computes 12.
// The correct minimum for these arrays with switchFee = 2 is 12.
int[] aCost2 = { 10, 2, 10, 2 };
int[] bCost2 = { 1, 9, 1, 9 };
int switchFee2 = 2;

long result2 = solution.MinimumCost(aCost2, bCost2, switchFee2);
Console.WriteLine(result2); // Correct result: 12

// Additional small sanity check.
int[] aCost3 = { 5 };
int[] bCost3 = { 2 };
int switchFee3 = 100;

long result3 = solution.MinimumCost(aCost3, bCost3, switchFee3);
Console.WriteLine(result3); // Expected: 2