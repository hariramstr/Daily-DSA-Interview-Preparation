/*
Title: Maximum Revenue from Ads with Cooling Gap
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A video platform has a list of ad opportunities along a timeline. You are given an integer array revenue where revenue[i] is the amount earned if you place an ad in slot i. However, to avoid showing ads too close together, after choosing any slot i, you must skip the next gap slots. In other words, if you place an ad at slot i, the next ad can only be placed at slot j where j > i + gap.

Your task is to compute the maximum total revenue that can be earned by selecting a subset of slots that satisfies this cooling-gap rule.

Return the maximum possible revenue.

This is a one-dimensional optimization problem. A slot may be skipped even if it has positive revenue, and some revenues may be 0. You should design an algorithm efficient enough for large inputs.

Constraints:
- 1 <= revenue.length <= 200000
- 0 <= revenue[i] <= 1000000000
- 0 <= gap < revenue.length

Example 1:
Input: revenue = [5, 1, 2, 10, 6, 2], gap = 1
Output: 17
Explanation: Choose slots 0, 3, and 5 for total revenue 5 + 10 + 2 = 17. Adjacent chosen slots must be at least 2 positions apart because gap = 1.

Example 2:
Input: revenue = [4, 7, 3, 9, 2, 8], gap = 2
Correct Output: 15
Explanation:
- If gap = 2, then after choosing index i, the next chosen index must be greater than i + 2.
- So valid chosen indices must differ by at least 3.
- The best valid subset here is choosing slots 1 and 5, for revenue 7 + 8 = 15.
- The original statement text contains some contradictory reasoning, but the mathematically correct answer is 15.

Approach:
We use dynamic programming.

Let dp[i] represent the maximum revenue we can earn using only slots from index 0 to index i.

For each slot i, we have two choices:
1. Skip slot i:
   - Then the best revenue remains dp[i - 1].
2. Take slot i:
   - We earn revenue[i]
   - Because of the cooling gap, the previous slot we are allowed to combine with is i - gap - 1
   - So total becomes revenue[i] + dp[i - gap - 1] if that index exists, otherwise just revenue[i]

Transition:
dp[i] = max(
    dp[i - 1],
    revenue[i] + (i - gap - 1 >= 0 ? dp[i - gap - 1] : 0)
)

This runs in linear time and is efficient for large inputs.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation:
    - We process each slot exactly once, so the total work is linear in the number of slots.
    - We store one DP value per slot, so the extra memory used is also linear.
    */
    public long MaxRevenue(int[] revenue, int gap)
    {
        // The number of available ad slots.
        int n = revenue.Length;

        // dp[i] will store the maximum revenue obtainable using slots from 0 through i inclusive.
        //
        // Why use long instead of int?
        // - revenue[i] can be as large as 1,000,000,000
        // - n can be as large as 200,000
        // - The total sum can exceed the 32-bit integer range
        // Therefore, long is the safe and correct choice.
        long[] dp = new long[n];

        // We iterate through every slot from left to right.
        // This order is important because dp[i] depends on earlier DP states:
        // - dp[i - 1]
        // - dp[i - gap - 1]
        for (int i = 0; i < n; i++)
        {
            // ------------------------------------------------------------
            // Option 1: Skip the current slot i
            // ------------------------------------------------------------
            //
            // If we do not place an ad at slot i, then the best answer up to i
            // is simply the same as the best answer up to i - 1.
            //
            // For i == 0, there is no previous slot, so the "skip" revenue is 0.
            long skipCurrent = i > 0 ? dp[i - 1] : 0;

            // ------------------------------------------------------------
            // Option 2: Take the current slot i
            // ------------------------------------------------------------
            //
            // If we place an ad at slot i, we earn revenue[i].
            // But then we must skip the next "gap" slots after any chosen slot.
            //
            // Looking backward, that means the previous chosen slot must be at
            // an index <= i - gap - 1.
            //
            // So the latest DP state we are allowed to combine with is:
            // previousAllowedIndex = i - gap - 1
            int previousAllowedIndex = i - gap - 1;

            // Start with the revenue from taking the current slot itself.
            long takeCurrent = revenue[i];

            // If there exists a valid earlier prefix that can be combined with slot i,
            // add the best revenue from that prefix.
            //
            // Example:
            // - If gap = 1 and i = 3, then previousAllowedIndex = 1
            //   So taking slot 3 can be combined with the best answer up to slot 1.
            if (previousAllowedIndex >= 0)
            {
                takeCurrent += dp[previousAllowedIndex];
            }

            // ------------------------------------------------------------
            // Choose the better of the two options
            // ------------------------------------------------------------
            //
            // dp[i] should represent the best possible answer using slots 0..i,
            // so we take the maximum between:
            // - skipping slot i
            // - taking slot i
            dp[i] = Math.Max(skipCurrent, takeCurrent);
        }

        // The answer for the entire array is the best revenue using slots 0..n-1.
        return dp[n - 1];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] revenue1 = { 5, 1, 2, 10, 6, 2 };
int gap1 = 1;
long result1 = solution.MaxRevenue(revenue1, gap1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 17

// Quick trace for Example 1:
// revenue = [5, 1, 2, 10, 6, 2], gap = 1
// Valid best choice: indices 0, 3, 5 => 5 + 10 + 2 = 17

// Example 2
int[] revenue2 = { 4, 7, 3, 9, 2, 8 };
int gap2 = 2;
long result2 = solution.MaxRevenue(revenue2, gap2);
Console.WriteLine("Example 2 Result: " + result2); // Correct expected: 15

// Quick trace for Example 2:
// revenue = [4, 7, 3, 9, 2, 8], gap = 2
// Chosen indices must differ by at least 3.
// Best valid choice: indices 1 and 5 => 7 + 8 = 15

// Additional demo 1: gap = 0 means we may choose adjacent slots too,
// because after choosing slot i, we skip the next 0 slots.
// So every non-negative slot can be taken, and since all revenues are non-negative,
// the result becomes the sum of all values.
int[] revenue3 = { 3, 0, 4, 2 };
int gap3 = 0;
long result3 = solution.MaxRevenue(revenue3, gap3);
Console.WriteLine("Additional Demo 1 Result: " + result3); // Expected: 9

// Additional demo 2: large gap where only widely separated slots can coexist.
int[] revenue4 = { 8, 1, 1, 8, 1, 1, 8 };
int gap4 = 2;
long result4 = solution.MaxRevenue(revenue4, gap4);
Console.WriteLine("Additional Demo 2 Result: " + result4); // Expected: 24 (indices 0, 3, 6)