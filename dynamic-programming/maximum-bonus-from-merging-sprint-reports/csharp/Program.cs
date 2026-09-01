/*
Title: Maximum Bonus from Merging Sprint Reports
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A product team tracks daily engineering output as an array of integers reports,
where reports[i] is the score recorded on day i. To prepare a quarterly review,
the manager wants to compress the timeline into several consecutive sprint summaries.

If you choose a subarray from index l to r as one sprint summary, its bonus is:

    (sum of reports[l..r]) * (length of the sprint)

You must partition the entire array into one or more contiguous, non-empty sprint summaries.
Every day must belong to exactly one summary, and summaries cannot overlap or be reordered.

Return the maximum total bonus obtainable.

In other words, split the array into contiguous blocks, compute sum(block) * size(block)
for each block, and maximize the sum of these values.

This is a dynamic programming problem because the best partition ending at a position
depends on the best partitions of all earlier prefixes.

Constraints:
- 1 <= reports.length <= 2000
- -10^4 <= reports[i] <= 10^4
- The answer fits in a signed 64-bit integer.

Examples:
1) reports = [3, -1, 2]
   Whole array bonus = (3 + -1 + 2) * 3 = 4 * 3 = 12
   This is better than splitting, so answer = 12

2) reports = [4, -5, 6, 1]
   Whole array bonus = (4 + -5 + 6 + 1) * 4 = 6 * 4 = 24
   This is the best, so answer = 24
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^2)
    Space Complexity: O(n)

    Explanation of complexity:
    - We compute dp[i] for every prefix length i from 1 to n.
    - For each i, we try every possible starting point j of the last block.
    - That means for each i we may scan up to i choices, giving a total of:
          1 + 2 + 3 + ... + n = O(n^2)
    - We store:
        * prefix sums of size n + 1
        * dp array of size n + 1
      so the extra memory is O(n).
    */
    public long MaxBonus(int[] reports)
    {
        int n = reports.Length;

        // prefix[k] will store the sum of the first k elements:
        // prefix[0] = 0
        // prefix[1] = reports[0]
        // prefix[2] = reports[0] + reports[1]
        // ...
        //
        // Why do we need this?
        // We will repeatedly need the sum of many subarrays reports[j..i-1].
        // Using prefix sums, we can get that sum in O(1):
        //
        //     sum(reports[j..i-1]) = prefix[i] - prefix[j]
        //
        // Without prefix sums, each subarray sum would take O(n) to compute,
        // which would make the whole solution too slow.
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + reports[i];
        }

        // dp[i] means:
        // "the maximum total bonus we can obtain by partitioning
        //  the first i elements (reports[0..i-1])"
        //
        // So:
        // dp[0] = 0 because an empty prefix has no elements and therefore no bonus.
        //
        // Our final answer will be dp[n], meaning the best partition of the entire array.
        long[] dp = new long[n + 1];

        // We will fill dp from smaller prefixes to larger prefixes.
        // This is the standard dynamic programming pattern:
        // solve small subproblems first, then use them to solve bigger ones.
        for (int i = 1; i <= n; i++)
        {
            // We are about to compute dp[i].
            // Initialize it to the smallest possible long value so that
            // any valid partition we compute will be larger.
            long best = long.MinValue;

            // We now consider every possible last block that ends at position i - 1.
            //
            // Let j be the starting index of the last block.
            // Then the last block is reports[j..i-1].
            //
            // The prefix before that block is reports[0..j-1],
            // whose best answer is already known as dp[j].
            //
            // Therefore, if we choose j as the start of the last block:
            //
            // total = dp[j] + bonus(reports[j..i-1])
            //
            // and
            //
            // bonus(reports[j..i-1]) = sum(reports[j..i-1]) * length
            //                           = (prefix[i] - prefix[j]) * (i - j)
            //
            // We try all j from 0 to i-1 and take the maximum.
            for (int j = 0; j < i; j++)
            {
                // Compute the sum of the last block reports[j..i-1].
                // This is O(1) because of the prefix sum array.
                long blockSum = prefix[i] - prefix[j];

                // Compute the length of the last block.
                int blockLength = i - j;

                // Compute the bonus contributed by this last block.
                long blockBonus = blockSum * blockLength;

                // Combine:
                // - the best answer for the prefix before the block
                // - the bonus of the chosen last block
                long candidate = dp[j] + blockBonus;

                // Keep the best candidate among all possible starts j.
                if (candidate > best)
                {
                    best = candidate;
                }
            }

            // After trying every possible last block ending at i-1,
            // best now contains the optimal answer for the first i elements.
            dp[i] = best;
        }

        // dp[n] is the best total bonus for the entire array.
        return dp[n];
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] reports1 = { 3, -1, 2 };
long result1 = solution.MaxBonus(reports1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", reports1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 12");
Console.WriteLine();

// Example 2
int[] reports2 = { 4, -5, 6, 1 };
long result2 = solution.MaxBonus(reports2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", reports2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 24");
Console.WriteLine();

// Additional demo cases
int[] reports3 = { 5 };
long result3 = solution.MaxBonus(reports3);
Console.WriteLine("Additional Example 3:");
Console.WriteLine($"Input: [{string.Join(", ", reports3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine();

int[] reports4 = { -2, -3, -1 };
long result4 = solution.MaxBonus(reports4);
Console.WriteLine("Additional Example 4:");
Console.WriteLine($"Input: [{string.Join(", ", reports4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine();

int[] reports5 = { 1, 2, 3, 4 };
long result5 = solution.MaxBonus(reports5);
Console.WriteLine("Additional Example 5:");
Console.WriteLine($"Input: [{string.Join(", ", reports5)}]");
Console.WriteLine($"Output: {result5}");
Console.WriteLine();