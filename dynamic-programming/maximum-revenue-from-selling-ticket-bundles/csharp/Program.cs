/*
Title: Maximum Revenue from Selling Ticket Bundles
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A concert venue is selling tickets for N consecutive seat sections, numbered from 0 to N - 1.
The venue may create promotional bundles, where each bundle must cover a contiguous range of sections.
If a bundle covers sections i through j, inclusive, its revenue is bundleRevenue[i][j].

You are allowed to choose any number of bundles, but no two chosen bundles may overlap in covered sections.
It is also allowed to leave some sections unbundled.

Your task is to compute the maximum total revenue that can be earned.

This is not a scheduling problem with explicit times; instead, each possible contiguous section range has
a precomputed revenue value. Some bundles may be unattractive, and skipping them can lead to a better overall answer.
You need to decide which non-overlapping ranges to select so that the sum of their revenues is maximized.

Return the maximum total revenue.

Constraints:
- 1 <= N <= 300
- bundleRevenue is an N x N matrix
- 0 <= bundleRevenue[i][j] <= 10^6 for all 0 <= i <= j < N
- bundleRevenue[i][j] = 0 for i > j, or such entries may be ignored
- Your solution should run in O(N^2) or O(N^2 log N) time

Examples:
Example 1:
N = 4
bundleRevenue = [
  [5, 9, 10, 10],
  [0, 4, 7, 8],
  [0, 0, 6, 9],
  [0, 0, 0, 3]
]
Output: 15

Example 2:
N = 5
bundleRevenue = [
  [2, 8, 8, 9, 9],
  [0, 1, 5, 7, 7],
  [0, 0, 4, 6, 12],
  [0, 0, 0, 3, 5],
  [0, 0, 0, 0, 4]
]
Correct Output: 20
Explanation:
Choose [0, 1] with revenue 8 and [2, 4] with revenue 12.
These do NOT overlap because section 1 is before section 2.
Total = 20.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(N^2)
    Space Complexity: O(N)

    Idea:
    We process seat sections from left to right using dynamic programming.

    Let dp[x] mean:
    the maximum revenue we can earn using only sections [0 .. x-1].

    So:
    - dp[0] = 0, because with zero sections available, revenue is zero.
    - Our final answer will be dp[N].

    Transition:
    For each ending boundary "endExclusive" from 1 to N:
      1. We may leave section endExclusive - 1 unused.
         Then dp[endExclusive] can at least be dp[endExclusive - 1].

      2. Or we may choose a bundle [start .. endExclusive - 1].
         If we choose that bundle, then everything before "start" must be solved optimally,
         which is exactly dp[start].
         So candidate revenue is:
             dp[start] + bundleRevenue[start][endExclusive - 1]

      We take the maximum over all such choices.

    This works because every chosen bundle ends somewhere, and once we decide its start,
    the remaining valid choices on the left are independent and non-overlapping.
    */
    public long MaxRevenue(int n, int[][] bundleRevenue)
    {
        // dp[k] stores the best revenue obtainable from the prefix of sections [0 .. k-1].
        // The array size is n + 1 so that dp[0] naturally represents "no sections considered yet".
        long[] dp = new long[n + 1];

        // We build answers for larger and larger prefixes.
        // endExclusive means we are currently solving for sections [0 .. endExclusive - 1].
        for (int endExclusive = 1; endExclusive <= n; endExclusive++)
        {
            // Step 1:
            // Start with the option of NOT using the last section in any bundle that ends here.
            //
            // Why is this necessary?
            // Because the problem explicitly allows leaving sections unbundled.
            // So even if every bundle ending at this position is bad, we can simply skip section endExclusive - 1.
            //
            // In that case, the best answer for the first endExclusive sections is just the same as
            // the best answer for the first endExclusive - 1 sections.
            dp[endExclusive] = dp[endExclusive - 1];

            // Step 2:
            // Try every possible bundle that ends exactly at section endExclusive - 1.
            //
            // Such a bundle has the form [start .. endExclusive - 1].
            // If we take it, then:
            // - it contributes bundleRevenue[start][endExclusive - 1]
            // - sections before "start" must be solved optimally and independently
            //   because bundles cannot overlap
            // - the best revenue for those earlier sections is dp[start]
            //
            // Therefore candidate = dp[start] + bundleRevenue[start][endExclusive - 1]
            for (int start = 0; start < endExclusive; start++)
            {
                long candidate = dp[start] + bundleRevenue[start][endExclusive - 1];

                // Step 3:
                // Keep the best possible value among:
                // - skipping the current ending section
                // - taking one of the bundles that ends here
                //
                // This is the core dynamic programming "maximize over choices" step.
                if (candidate > dp[endExclusive])
                {
                    dp[endExclusive] = candidate;
                }
            }
        }

        // dp[n] represents the best revenue using all sections [0 .. n-1].
        return dp[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int n1 = 4;
int[][] bundleRevenue1 =
{
    new[] { 5, 9, 10, 10 },
    new[] { 0, 4, 7, 8 },
    new[] { 0, 0, 6, 9 },
    new[] { 0, 0, 0, 3 }
};

long result1 = solution.MaxRevenue(n1, bundleRevenue1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 15

// Example 2
int n2 = 5;
int[][] bundleRevenue2 =
{
    new[] { 2, 8, 8, 9, 9 },
    new[] { 0, 1, 5, 7, 7 },
    new[] { 0, 0, 4, 6, 12 },
    new[] { 0, 0, 0, 3, 5 },
    new[] { 0, 0, 0, 0, 4 }
};

long result2 = solution.MaxRevenue(n2, bundleRevenue2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 20

// Additional small sanity check
int n3 = 3;
int[][] bundleRevenue3 =
{
    new[] { 1, 100, 2 },
    new[] { 0, 1, 50 },
    new[] { 0, 0, 1 }
};

long result3 = solution.MaxRevenue(n3, bundleRevenue3);
Console.WriteLine("Additional Test Result: " + result3); // Best is [0,1] = 100 and [2,2] = 1 => 101