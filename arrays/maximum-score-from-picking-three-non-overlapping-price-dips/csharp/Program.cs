/*
Title: Maximum Score from Picking Three Non-Overlapping Price Dips
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array prices where prices[i] represents the profit impact of selecting day i
in a promotional trading strategy. Values may be positive, zero, or negative.

You must choose exactly three non-empty contiguous subarrays, and the three chosen subarrays must be
pairwise non-overlapping.

The score of a chosen subarray is the sum of its elements, and the total strategy score is the sum of
the scores of the three chosen subarrays.

Your task is to return the maximum possible total score.

This is not the same as choosing any three individual elements: each choice must be a contiguous block,
and blocks may have different lengths. Because you must choose exactly three subarrays, negative values
cannot always be ignored. In particular, if the array contains many negative numbers, the optimal answer
may still include a negative-sum segment in order to satisfy the requirement of selecting three
non-overlapping subarrays.

Constraints:
- 3 <= prices.length <= 200000
- -10^9 <= prices[i] <= 10^9
- The answer fits in a signed 64-bit integer.

Example 1:
Input: prices = [4,-1,3,-2,5,-6,2,2]
Correct Output: 15
Explanation:
One optimal choice is [4,-1,3] with sum 6, [5] with sum 5, and [2,2] with sum 4.
These subarrays do not overlap, so the total is 6 + 5 + 4 = 15.

Note:
The prompt text says Output: 13, but the explanation clearly sums to 15.
The explanation is internally consistent, so the correct answer is 15.

Example 2:
Input: prices = [-5,4,-1,4,-10,3]
Output: 10
Explanation:
A best choice is [4], [-1,4], and [3], with total 4 + 3 + 3 = 10.
These three subarrays are contiguous, non-empty, and non-overlapping.
*/

using System;

public class Solution
{
    // Time Complexity: O(n)
    // Space Complexity: O(n)
    public long MaxSumOfThreeNonOverlappingSubarrays(int[] prices)
    {
        int n = prices.Length;

        // We need exactly three non-empty non-overlapping subarrays.
        //
        // A very useful dynamic programming idea for "pick k non-overlapping subarrays"
        // is to process the array from left to right and keep track of:
        //
        // 1) "local" state:
        //    The best total sum when we have chosen exactly j subarrays so far,
        //    and the j-th subarray MUST end at the current index.
        //
        // 2) "global" state:
        //    The best total sum when we have chosen exactly j subarrays somewhere
        //    in the prefix processed so far, with no requirement that the last one
        //    ends at the current index.
        //
        // Why do we need both?
        // - If we want to extend a currently open subarray to include prices[i],
        //   we need the "local" state from the previous index.
        // - If we want to start a brand-new subarray at prices[i],
        //   we need the best completed answer with one fewer subarray from earlier,
        //   which is the "global" state.
        //
        // Since the user asked for beginner-friendly code with detailed comments,
        // we will store the full DP tables:
        //
        // local[i, j]  = best total using exactly j subarrays in prices[0..i],
        //                where the j-th subarray ends exactly at i.
        //
        // global[i, j] = best total using exactly j subarrays in prices[0..i],
        //                with no restriction on where the j-th subarray ends.
        //
        // We only need j = 0..3.
        //
        // Because values can be very negative, we cannot initialize impossible states to 0.
        // We must use a very small negative sentinel value so impossible states never get chosen.
        long negInf = long.MinValue / 4;

        long[,] local = new long[n, 4];
        long[,] global = new long[n, 4];

        // Initialize every state as impossible first.
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j <= 3; j++)
            {
                local[i, j] = negInf;
                global[i, j] = negInf;
            }
        }

        // Base case:
        // Choosing exactly 0 subarrays from any prefix has total sum 0.
        // There is no "local" meaning for 0 subarrays ending at i, so local[i,0] stays impossible.
        for (int i = 0; i < n; i++)
        {
            global[i, 0] = 0;
        }

        // Process the first element separately to make the transitions easier to understand.
        //
        // At i = 0:
        // - We can choose exactly 1 subarray ending at 0, namely [prices[0]].
        // - We cannot choose 2 or 3 subarrays yet because the array prefix is too short.
        local[0, 1] = prices[0];
        global[0, 1] = prices[0];

        // Now fill the DP tables from left to right.
        for (int i = 1; i < n; i++)
        {
            // global[i,0] is always 0: choose nothing.
            global[i, 0] = 0;

            // We only care about choosing 1, 2, or 3 subarrays.
            for (int j = 1; j <= 3; j++)
            {
                // We are computing local[i, j]:
                // the best total sum using exactly j subarrays, with the j-th subarray ending at i.
                //
                // There are exactly two ways this can happen:
                //
                // Option A: Extend the j-th subarray that already ended at i-1.
                //           Then we add prices[i] to local[i-1, j].
                //
                // Option B: Start a new j-th subarray at i.
                //           Then the previous j-1 subarrays must be fully contained in prices[0..i-1],
                //           and their best total is global[i-1, j-1].
                //           Starting a new subarray at i contributes prices[i].
                //
                // So:
                // local[i, j] = max(
                //     local[i-1, j] + prices[i],      // extend current subarray
                //     global[i-1, j-1] + prices[i]    // start new subarray at i
                // )
                //
                // We must be careful with impossible states (negInf), so we guard additions.
                long extend = negInf;
                if (local[i - 1, j] != negInf)
                {
                    extend = local[i - 1, j] + prices[i];
                }

                long startNew = negInf;
                if (global[i - 1, j - 1] != negInf)
                {
                    startNew = global[i - 1, j - 1] + prices[i];
                }

                local[i, j] = Math.Max(extend, startNew);

                // Now compute global[i, j]:
                // the best total sum using exactly j subarrays somewhere in prices[0..i].
                //
                // Again there are two possibilities:
                //
                // Option A: The best answer was already found in prices[0..i-1].
                //           Then it is global[i-1, j].
                //
                // Option B: The best answer uses a j-th subarray that ends exactly at i.
                //           Then it is local[i, j].
                //
                // So:
                // global[i, j] = max(global[i-1, j], local[i, j])
                global[i, j] = Math.Max(global[i - 1, j], local[i, j]);
            }
        }

        // The final answer is the best total using exactly 3 subarrays in the full array.
        return global[n - 1, 3];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] prices1 = { 4, -1, 3, -2, 5, -6, 2, 2 };
long result1 = solution.MaxSumOfThreeNonOverlappingSubarrays(prices1);
Console.WriteLine("Example 1 result: " + result1); // Expected: 15

// Example 2
int[] prices2 = { -5, 4, -1, 4, -10, 3 };
long result2 = solution.MaxSumOfThreeNonOverlappingSubarrays(prices2);
Console.WriteLine("Example 2 result: " + result2); // Expected: 10

// Additional quick sanity checks

// All negative: must still choose exactly three non-empty subarrays.
int[] prices3 = { -3, -2, -5 };
long result3 = solution.MaxSumOfThreeNonOverlappingSubarrays(prices3);
Console.WriteLine("All negative length 3 result: " + result3); // Expected: -10

// Simple separated positives.
int[] prices4 = { 1, -100, 2, -100, 3 };
long result4 = solution.MaxSumOfThreeNonOverlappingSubarrays(prices4);
Console.WriteLine("Separated positives result: " + result4); // Expected: 6