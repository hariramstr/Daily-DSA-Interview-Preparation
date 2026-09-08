/*
Title: Maximum Score from Picking a Profitable Prefix and Suffix
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array profits where profits[i] represents the net profit
(which may be negative) of the i-th product in a catalog.

A merchandising team wants to build a promotion by choosing:
- some number of products from the beginning of the catalog (a prefix), and
- some number of products from the end of the catalog (a suffix).

The chosen prefix and suffix must not overlap, but either side may be empty.

Your task is to return the maximum total profit that can be obtained.

Formally, choose indices such that you take profits[0..i] as a prefix and profits[j..n-1]
as a suffix, where i < j - 1 so the two chosen parts are disjoint.
You may also choose only a prefix, only a suffix, or choose nothing at all if every option
is unprofitable. The score is the sum of all selected values.

This is not the same as choosing one contiguous subarray. You are selecting up to two
separated edge segments of the array.

Constraints:
- 1 <= profits.length <= 200000
- -1000000000 <= profits[i] <= 1000000000
- The answer fits in a signed 64-bit integer.

Example 1:
Input: profits = [4, -2, 3, -10, 5, 6]
Output: 16
Explanation:
Take prefix [4, -2, 3] with sum 5 and suffix [5, 6] with sum 11.
They do not overlap, so the total is 16.

Example 2:
Input: profits = [-5, 7, -3, 8, -2]
Output: 10
Explanation:
The best choice is to take only the suffix [7, -3, 8, -2] with sum 10.
Taking both sides is worse because the selected parts must stay disjoint and edge-aligned.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Idea:
    We want the best total of:
    - an optional prefix ending at some index i
    - plus an optional suffix starting at some index j
    with i < j - 1 so they are disjoint and leave at least one split position between them.

    A very clean way to do this is:
    1. Precompute prefix sums:
       prefixSum[i] = sum of profits[0..i]
    2. Precompute suffix sums:
       suffixSum[i] = sum of profits[i..n-1]
    3. Convert those into "best up to here" arrays:
       bestPrefix[i] = maximum profit of any prefix ending at or before i, or 0 if taking no prefix is better
       bestSuffix[i] = maximum profit of any suffix starting at or after i, or 0 if taking no suffix is better
    4. Try every split between left part and right part:
       - left side can use indices <= i
       - right side can use indices >= i + 1
       Then total = bestPrefix[i] + bestSuffix[i + 1]
    5. Also allow taking only one side or nothing, which is naturally handled because both best arrays allow 0.

    Why this works:
    - Any valid chosen prefix is completely determined by where it ends.
    - Any valid chosen suffix is completely determined by where it starts.
    - By storing the best possible prefix on the left and best possible suffix on the right,
      each split can be evaluated in O(1).
    */
    public long MaxScore(int[] profits)
    {
        int n = profits.Length;

        // prefixSum[i] will store the sum of the prefix profits[0..i].
        // We use long because values can be large and the total can exceed int range.
        long[] prefixSum = new long[n];

        // Build prefix sums from left to right.
        // Each position adds the current value to the sum before it.
        prefixSum[0] = profits[0];
        for (int i = 1; i < n; i++)
        {
            prefixSum[i] = prefixSum[i - 1] + profits[i];
        }

        // suffixSum[i] will store the sum of the suffix profits[i..n-1].
        long[] suffixSum = new long[n];

        // Build suffix sums from right to left.
        // Each position adds the current value to the sum after it.
        suffixSum[n - 1] = profits[n - 1];
        for (int i = n - 2; i >= 0; i--)
        {
            suffixSum[i] = suffixSum[i + 1] + profits[i];
        }

        // bestPrefix[i] means:
        // "Among all prefixes that end at or before index i, what is the maximum sum?"
        // We also allow choosing no prefix at all, so the minimum useful value is 0.
        long[] bestPrefix = new long[n];
        bestPrefix[0] = Math.Max(0L, prefixSum[0]);

        for (int i = 1; i < n; i++)
        {
            // At index i, we have two choices:
            // 1. Keep the best prefix we already found earlier.
            // 2. Use the prefix that ends exactly at i.
            //
            // We also compare against 0 indirectly because bestPrefix[i - 1] is already >= 0,
            // and prefixSum[i] might be negative.
            bestPrefix[i] = Math.Max(bestPrefix[i - 1], prefixSum[i]);
        }

        // bestSuffix[i] means:
        // "Among all suffixes that start at or after index i, what is the maximum sum?"
        // Again, we allow choosing no suffix, so 0 is allowed.
        long[] bestSuffix = new long[n];
        bestSuffix[n - 1] = Math.Max(0L, suffixSum[n - 1]);

        for (int i = n - 2; i >= 0; i--)
        {
            // At index i, we have two choices:
            // 1. Use the best suffix found to the right.
            // 2. Use the suffix that starts exactly at i.
            bestSuffix[i] = Math.Max(bestSuffix[i + 1], suffixSum[i]);
        }

        // Start with 0 because choosing nothing is allowed.
        long answer = 0;

        // Consider taking only a prefix.
        answer = Math.Max(answer, bestPrefix[n - 1]);

        // Consider taking only a suffix.
        answer = Math.Max(answer, bestSuffix[0]);

        // Now consider taking both a prefix and a suffix.
        //
        // We split the array between i and i+1:
        // - left choice must be a prefix ending at or before i
        // - right choice must be a suffix starting at or after i+1
        //
        // This guarantees the chosen parts are disjoint.
        for (int i = 0; i < n - 1; i++)
        {
            long candidate = bestPrefix[i] + bestSuffix[i + 1];
            answer = Math.Max(answer, candidate);
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

int[] profits1 = { 4, -2, 3, -10, 5, 6 };
long result1 = solution.MaxScore(profits1);
Console.WriteLine(result1); // Expected: 16

int[] profits2 = { -5, 7, -3, 8, -2 };
long result2 = solution.MaxScore(profits2);
Console.WriteLine(result2); // Expected: 10

int[] profits3 = { -4, -1, -7 };
long result3 = solution.MaxScore(profits3);
Console.WriteLine(result3); // Expected: 0

int[] profits4 = { 5, 1, 2 };
long result4 = solution.MaxScore(profits4);
Console.WriteLine(result4); // Expected: 8

int[] profits5 = { 10, -100, 20 };
long result5 = solution.MaxScore(profits5);
Console.WriteLine(result5); // Expected: 30