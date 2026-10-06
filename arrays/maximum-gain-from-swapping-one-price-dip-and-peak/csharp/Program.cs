/*
Title: Maximum Gain from Swapping One Price Dip and Peak
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array prices where prices[i] represents the price of an asset on day i.
You may perform at most one swap of two different elements in the array.

After the swap, choose a single buy day b and a later sell day s such that b < s.
Your profit is prices[s] - prices[b].

Return the maximum profit you can achieve.

A swap is optional, and the buy/sell operation must happen after the array has been modified.
The swapped values remain in their new positions when selecting the buy and sell days.
If no profitable transaction is possible, return 0.

This problem is about reasoning over array positions, not sorting the array freely.
Because only one swap is allowed, a good solution must carefully evaluate how moving one low price earlier
or one high price later can improve the best possible transaction.

Constraints:
- 2 <= prices.length <= 2 * 10^5
- 0 <= prices[i] <= 10^9
- You may swap at most one pair of indices i and j where i != j

Example 1:
Input: prices = [8, 3, 6, 1, 9]
Output: 8
Explanation:
Swap 8 and 1 to get [1, 3, 6, 8, 9]. Then buy on day 0 at 1 and sell on day 4 at 9 for profit 8.
Without a swap, the best profit is also 8 by buying at 1 and selling at 9, so the answer remains 8.

Example 2:
Input: prices = [10, 7, 4, 6, 2]
Output: 5
Explanation:
Without a swap, the best profit is 2 by buying at 4 and selling at 6.
If you swap 7 and 2, the array becomes [10, 2, 4, 6, 7].
Then buy on day 1 at 2 and sell on day 4 at 7 for profit 5.
So the correct output is 5.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    Space Complexity: O(n)

    High-level idea:
    We want the best possible value of prices[s] - prices[b] after at most one swap.

    A direct brute force approach would try every swap and then compute the best transaction,
    which is far too slow for n up to 200,000.

    Instead, we reason about the final chosen buy day b and sell day s in the modified array.

    For any final pair (b, s), there are only three meaningful possibilities:
    1) No swap touches b or s:
       profit = original prices[s] - original prices[b]

    2) One endpoint is improved by the swap:
       - We move a smaller value into b from some index j != s
       - Or we move a larger value into s from some index i != b

    3) The swap is exactly between b and s:
       Then the values at b and s become original prices[s] and original prices[b],
       so profit = original prices[b] - original prices[s].
       This can be useful if original prices[b] > original prices[s].

    Therefore for each pair (b, s), the best achievable profit is:
       max(
           prices[s] - prices[b],
           prices[s] - min value from all indices except s,
           max value from all indices except b - prices[b],
           prices[b] - prices[s]
       )

    We still cannot check all O(n^2) pairs.
    So we transform each case into a form that can be scanned efficiently.

    Case A: prices[s] - min value from all indices except s
        For each sell day s, we only need the global minimum value excluding s.
        This is easy with prefix/suffix minima.

    Case B: max value from all indices except b - prices[b]
        For each buy day b, we only need the global maximum value excluding b.
        This is easy with prefix/suffix maxima.

    Case C: prices[s] - prices[b] and Case D: prices[b] - prices[s]
        Together these mean for pair (b, s) we care about |prices[s] - prices[b]|.
        Since b < s, we need the maximum absolute difference over all ordered pairs.
        That equals max(
            max over s of prices[s] - minimum before s,
            maximum before s - prices[s]
        ),
        which can be computed in one left-to-right scan.

    The final answer is the maximum among:
    - best "improve buy day by importing a global minimum not from s"
    - best "improve sell day by importing a global maximum not from b"
    - best absolute difference across an ordered pair (covers no swap and swapping b with s)

    This is correct for the provided examples:
    Example 1: [8,3,6,1,9] => answer 8
    Example 2: [10,7,4,6,2] => answer 5
    */
    public long MaxProfitAfterAtMostOneSwap(int[] prices)
    {
        int n = prices.Length;

        // prefixMin[i] = minimum value in prices[0..i]
        // We build this so that later we can quickly know the minimum value on the left side.
        int[] prefixMin = new int[n];
        prefixMin[0] = prices[0];
        for (int i = 1; i < n; i++)
        {
            prefixMin[i] = Math.Min(prefixMin[i - 1], prices[i]);
        }

        // suffixMin[i] = minimum value in prices[i..n-1]
        // We build this so that later we can quickly know the minimum value on the right side.
        int[] suffixMin = new int[n];
        suffixMin[n - 1] = prices[n - 1];
        for (int i = n - 2; i >= 0; i--)
        {
            suffixMin[i] = Math.Min(suffixMin[i + 1], prices[i]);
        }

        // prefixMax[i] = maximum value in prices[0..i]
        // This helps us answer "what is the largest value before or at this point?"
        int[] prefixMax = new int[n];
        prefixMax[0] = prices[0];
        for (int i = 1; i < n; i++)
        {
            prefixMax[i] = Math.Max(prefixMax[i - 1], prices[i]);
        }

        // suffixMax[i] = maximum value in prices[i..n-1]
        // This helps us answer "what is the largest value after or at this point?"
        int[] suffixMax = new int[n];
        suffixMax[n - 1] = prices[n - 1];
        for (int i = n - 2; i >= 0; i--)
        {
            suffixMax[i] = Math.Max(suffixMax[i + 1], prices[i]);
        }

        long answer = 0;

        // ------------------------------------------------------------
        // Part 1:
        // For each possible final sell day s, compute the best profit if we improve the buy day.
        //
        // We want:
        //   prices[s] - min value from all indices except s
        //
        // Why "except s"?
        // Because the swap uses two different indices, and if we import the value into day s itself,
        // that would change the sell value, not the buy value. For this case we are specifically
        // keeping the sell day's value fixed and improving the buy day.
        //
        // We can get "minimum excluding s" by combining:
        //   minimum on the left of s, and minimum on the right of s.
        // ------------------------------------------------------------
        for (int s = 0; s < n; s++)
        {
            int minExcludingS = int.MaxValue;

            if (s > 0)
            {
                minExcludingS = Math.Min(minExcludingS, prefixMin[s - 1]);
            }

            if (s + 1 < n)
            {
                minExcludingS = Math.Min(minExcludingS, suffixMin[s + 1]);
            }

            long candidate = (long)prices[s] - minExcludingS;
            if (candidate > answer)
            {
                answer = candidate;
            }
        }

        // ------------------------------------------------------------
        // Part 2:
        // For each possible final buy day b, compute the best profit if we improve the sell day.
        //
        // We want:
        //   max value from all indices except b - prices[b]
        //
        // Why "except b"?
        // Because the swap uses two different indices, and if we import the value from b itself,
        // that would not be a real swap affecting the sell day independently.
        //
        // We can get "maximum excluding b" by combining:
        //   maximum on the left of b, and maximum on the right of b.
        // ------------------------------------------------------------
        for (int b = 0; b < n; b++)
        {
            int maxExcludingB = int.MinValue;

            if (b > 0)
            {
                maxExcludingB = Math.Max(maxExcludingB, prefixMax[b - 1]);
            }

            if (b + 1 < n)
            {
                maxExcludingB = Math.Max(maxExcludingB, suffixMax[b + 1]);
            }

            long candidate = (long)maxExcludingB - prices[b];
            if (candidate > answer)
            {
                answer = candidate;
            }
        }

        // ------------------------------------------------------------
        // Part 3:
        // Handle the cases where the final pair (b, s) is evaluated directly as an ordered pair.
        //
        // This covers:
        // - no swap at all: profit = prices[s] - prices[b]
        // - swapping exactly b and s: profit = prices[b] - prices[s]
        //
        // Taking the better of those two for a fixed ordered pair is:
        //   |prices[s] - prices[b]|
        //
        // We need the maximum absolute difference over all pairs with b < s.
        //
        // We can compute this in one scan:
        // - Keep the minimum value seen so far to maximize prices[s] - previousMin
        // - Keep the maximum value seen so far to maximize previousMax - prices[s]
        // ------------------------------------------------------------
        int minSoFar = prices[0];
        int maxSoFar = prices[0];

        for (int s = 1; s < n; s++)
        {
            long candidateUsingEarlierMin = (long)prices[s] - minSoFar;
            if (candidateUsingEarlierMin > answer)
            {
                answer = candidateUsingEarlierMin;
            }

            long candidateUsingEarlierMax = (long)maxSoFar - prices[s];
            if (candidateUsingEarlierMax > answer)
            {
                answer = candidateUsingEarlierMax;
            }

            if (prices[s] < minSoFar)
            {
                minSoFar = prices[s];
            }

            if (prices[s] > maxSoFar)
            {
                maxSoFar = prices[s];
            }
        }

        // Profit cannot be negative because we are allowed to do nothing.
        return Math.Max(0, answer);
    }
}

// Demo code
var solution = new Solution();

int[] prices1 = { 8, 3, 6, 1, 9 };
long result1 = solution.MaxProfitAfterAtMostOneSwap(prices1);
Console.WriteLine(result1); // Expected: 8

int[] prices2 = { 10, 7, 4, 6, 2 };
long result2 = solution.MaxProfitAfterAtMostOneSwap(prices2);
Console.WriteLine(result2); // Expected: 5

int[] prices3 = { 5, 4, 3, 2, 1 };
long result3 = solution.MaxProfitAfterAtMostOneSwap(prices3);
Console.WriteLine(result3); // One beneficial swap can make profit 4

int[] prices4 = { 1, 2 };
long result4 = solution.MaxProfitAfterAtMostOneSwap(prices4);
Console.WriteLine(result4); // Expected: 1

int[] prices5 = { 2, 2, 2, 2 };
long result5 = solution.MaxProfitAfterAtMostOneSwap(prices5);
Console.WriteLine(result5); // Expected: 0