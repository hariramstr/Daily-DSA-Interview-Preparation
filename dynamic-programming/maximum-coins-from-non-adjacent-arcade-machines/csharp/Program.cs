/*
Title: Maximum Coins from Non-Adjacent Arcade Machines

Problem Description:
You are managing a row of arcade machines in a game center. Each machine contains a certain number of collectible coins,
given by the array `coins`, where `coins[i]` is the number of coins inside the `i`th machine.

If you collect coins from one machine, the security system prevents you from collecting from its immediate neighboring
machines on the same night.

Your task is to determine the maximum number of coins you can collect in one night without collecting from two adjacent
machines.

This is a classic decision-making problem where, for each machine, you can either skip it or collect from it and then
skip its neighbor. Return the largest total number of coins possible.

Constraints:
- 1 <= coins.length <= 100
- 0 <= coins[i] <= 1000

Example 1:
Input: coins = [4, 2, 7, 9, 3]
Output: 14

Reasoning:
- You cannot take adjacent machines.
- Valid options include:
  - 4 + 7 + 3 = 14
  - 4 + 9 = 13
  - 7 + 3 = 10
- The maximum valid total is 14.

Example 2:
Input: coins = [10, 1, 1, 10]
Output: 20

Reasoning:
- Take the first and last machines.
- They are not adjacent.
- Total = 10 + 10 = 20
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each machine exactly once, and each step does only constant-time work.

    Space Complexity: O(1)
    - We do not need a full DP array.
    - We only keep track of the best answer for:
      1) the previous machine
      2) the machine before the previous one

    Beginner-friendly idea:
    At every machine, we have exactly two choices:
    1. Skip this machine -> keep the best total we already had
    2. Take this machine -> add its coins to the best total from two machines back

    We choose whichever of those two options gives a larger total.
    */
    public int MaxCoins(int[] coins)
    {
        // This variable stores the best total we can collect up to the machine two positions back.
        // Why do we need this?
        // Because if we decide to collect from the current machine, we are NOT allowed to collect
        // from the immediately previous machine. So the most recent safe total we can add is from
        // two positions earlier.
        int prevTwo = 0;

        // This variable stores the best total we can collect up to the previous machine.
        // Why do we need this?
        // Because if we skip the current machine, then the best answer simply remains whatever
        // the best answer was up to the previous machine.
        int prevOne = 0;

        // We now process each machine from left to right.
        // This is a dynamic programming pattern:
        // build the answer for a larger prefix of the array using answers from smaller prefixes.
        foreach (int coin in coins)
        {
            // Option 1: Skip the current machine.
            // If we skip it, our total does not change from the best total up to the previous machine.
            int skipCurrent = prevOne;

            // Option 2: Take the current machine.
            // If we take it, we must avoid the previous machine,
            // so we add the current machine's coins to the best total from two machines back.
            int takeCurrent = prevTwo + coin;

            // The best total including decisions up to this machine is the better of:
            // - skipping the current machine
            // - taking the current machine
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Now we slide our DP window forward:
            // - what used to be "previous one" becomes "previous two"
            // - currentBest becomes the new "previous one"
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all machines, prevOne holds the best total for the entire array.
        return prevOne;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] coins1 = { 4, 2, 7, 9, 3 };
int result1 = solution.MaxCoins(coins1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", coins1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 14");
Console.WriteLine();

// Example 2
int[] coins2 = { 10, 1, 1, 10 };
int result2 = solution.MaxCoins(coins2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", coins2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 20");
Console.WriteLine();

// Additional demo: single machine
int[] coins3 = { 8 };
int result3 = solution.MaxCoins(coins3);
Console.WriteLine("Additional Example 3:");
Console.WriteLine($"Input: [{string.Join(", ", coins3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 8");
Console.WriteLine();

// Additional demo: includes zero values
int[] coins4 = { 0, 5, 0, 10, 0, 7 };
int result4 = solution.MaxCoins(coins4);
Console.WriteLine("Additional Example 4:");
Console.WriteLine($"Input: [{string.Join(", ", coins4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 22");