/*
Title: Minimum Merge Cost for Layered Test Suites
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A build system stores automated tests as an ordered list of suites. The i-th suite has execution weight tests[i].
To reduce startup overhead, the system must repeatedly merge adjacent suites until exactly one suite remains.

When you merge a contiguous block of suites from index l to r into a single suite, the merge operation itself
costs the total execution weight of that block.

You may choose the merge order, but you may never reorder suites. Every merge must combine two already-formed
adjacent groups. The goal is to compute the minimum total cost required to merge all suites into one suite.

More formally:
- Given an array tests of length n
- Each merge picks two already-formed adjacent groups
- If the left group covers [l..m] and the right group covers [m+1..r], then the cost of that merge is
  sum(tests[l..r])
- Return the minimum possible total cost over all valid merge sequences

Examples:
1) tests = [4, 1, 7, 3]
   Output: 29

2) tests = [6, 2, 4]
   Output: 18
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^3)
    Space Complexity: O(n^2)

    Explanation of complexity:
    - There are O(n^2) subarrays [left..right].
    - For each subarray, we try every possible split point in the middle, which is O(n).
    - Therefore total time is O(n^3).
    - We store a DP table of size n x n, so space is O(n^2).

    This is acceptable for n <= 400 in optimized C# for a classic interval DP solution.
    */
    public long MinimumMergeCost(int[] tests)
    {
        // Step 1:
        // Handle the smallest possible input.
        //
        // If there is only one suite, it is already fully merged.
        // No merge operation is needed, so the total cost is 0.
        int n = tests.Length;
        if (n <= 1)
        {
            return 0;
        }

        // Step 2:
        // Build a prefix sum array so we can quickly compute the sum of any subarray.
        //
        // Why do we need this?
        // Every time we merge a range [left..right], the merge cost is the total sum of that range.
        // If we computed that sum by looping every time, the algorithm would become too slow.
        //
        // prefix[i] will store the sum of the first i elements:
        // prefix[0] = 0
        // prefix[1] = tests[0]
        // prefix[2] = tests[0] + tests[1]
        // ...
        //
        // Then sum of tests[left..right] can be computed in O(1) as:
        // prefix[right + 1] - prefix[left]
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + tests[i];
        }

        // Step 3:
        // Create the DP table.
        //
        // dp[left, right] means:
        // "the minimum cost to merge the contiguous subarray tests[left..right] into one suite"
        //
        // This is a classic interval dynamic programming setup.
        //
        // Important base case:
        // A single element range [i..i] is already one suite, so dp[i, i] = 0.
        // In C#, long arrays are initialized to 0 automatically, so we already have that base case.
        long[,] dp = new long[n, n];

        // Step 4:
        // Process ranges by increasing length.
        //
        // Why increasing length?
        // To compute dp[left, right], we need smaller subproblems such as:
        // dp[left, mid] and dp[mid + 1, right]
        //
        // Those subproblems represent smaller intervals, so they must already be computed.
        //
        // We start from length = 2 because length = 1 intervals are base cases.
        for (int length = 2; length <= n; length++)
        {
            // For each possible starting index of a range of this length...
            for (int left = 0; left + length - 1 < n; left++)
            {
                int right = left + length - 1;

                // Step 4a:
                // Initialize the answer for this interval to a very large number.
                //
                // We will try all possible split points and keep the minimum.
                long best = long.MaxValue;

                // Step 4b:
                // Try every possible place to split the interval into two adjacent parts:
                // [left..mid] and [mid+1..right]
                //
                // Since merges must preserve order and only merge adjacent groups,
                // every valid final merge of [left..right] must come from some split point mid.
                for (int mid = left; mid < right; mid++)
                {
                    // Cost to fully merge the left half into one suite.
                    long leftCost = dp[left, mid];

                    // Cost to fully merge the right half into one suite.
                    long rightCost = dp[mid + 1, right];

                    // Cost of the final merge that combines those two already-merged adjacent groups.
                    //
                    // That final merge covers the whole interval [left..right],
                    // so its cost is the sum of tests[left..right].
                    long mergeCost = prefix[right + 1] - prefix[left];

                    // Total cost if we choose this split point.
                    long totalCost = leftCost + rightCost + mergeCost;

                    // Keep the best (minimum) among all split choices.
                    if (totalCost < best)
                    {
                        best = totalCost;
                    }
                }

                // Step 4c:
                // Store the minimum cost found for this interval.
                dp[left, right] = best;
            }
        }

        // Step 5:
        // The answer for the whole problem is the minimum cost to merge the full range [0..n-1].
        return dp[0, n - 1];
    }
}

// Demo code
var solution = new Solution();

// Example 1:
// tests = [4, 1, 7, 3]
// Expected output: 29
int[] tests1 = { 4, 1, 7, 3 };
long result1 = solution.MinimumMergeCost(tests1);
Console.WriteLine($"Input: [{string.Join(", ", tests1)}]");
Console.WriteLine($"Minimum merge cost: {result1}");
Console.WriteLine("Expected: 29");
Console.WriteLine();

// Example 2:
// tests = [6, 2, 4]
// Expected output: 18
int[] tests2 = { 6, 2, 4 };
long result2 = solution.MinimumMergeCost(tests2);
Console.WriteLine($"Input: [{string.Join(", ", tests2)}]");
Console.WriteLine($"Minimum merge cost: {result2}");
Console.WriteLine("Expected: 18");
Console.WriteLine();

// Additional small sanity check:
// Single suite requires no merge.
int[] tests3 = { 10 };
long result3 = solution.MinimumMergeCost(tests3);
Console.WriteLine($"Input: [{string.Join(", ", tests3)}]");
Console.WriteLine($"Minimum merge cost: {result3}");
Console.WriteLine("Expected: 0");