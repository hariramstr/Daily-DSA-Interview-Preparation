/*
Title: Maximum Score from Choosing One Promotion Day
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array sales where sales[i] represents the net revenue earned on day i.
A company wants to run exactly one special promotion on a single day p.

After choosing p, the total promotion score is defined as:
- leftScore  = maximum sum of a non-empty contiguous subarray that ends at day p
- rightScore = maximum sum of a non-empty contiguous subarray that starts at day p
- promotionScore = leftScore + rightScore - sales[p]

We subtract sales[p] once because day p is included in both leftScore and rightScore,
but it should only be counted once in the final total.

Goal:
Return the maximum possible promotionScore over all valid choices of p.

Constraints:
- 1 <= sales.length <= 200000
- -1000000000 <= sales[i] <= 1000000000
- The answer fits in a signed 64-bit integer.

Examples:
1) sales = [4, -2, 3, -1, 5]
   Choose p = 2 (value 3)
   Best subarray ending at 2   = [4, -2, 3] => 5
   Best subarray starting at 2 = [3, -1, 5] => 7
   promotionScore = 5 + 7 - 3 = 9

2) sales = [-5, -2, -7]
   Best answer is -2 by choosing p = 1
   leftScore = -2, rightScore = -2, promotionScore = -2
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why:
    - We make one left-to-right pass to compute the best non-empty subarray sum ending at each index.
    - We make one right-to-left pass to compute the best non-empty subarray sum starting at each index.
    - We make one final pass to combine those values and find the maximum promotion score.

    This is efficient enough for n up to 200,000.
    */
    public long MaximumPromotionScore(int[] sales)
    {
        // The array length is at least 1 by the problem constraints.
        int n = sales.Length;

        // leftBestEndingAt[i] will store:
        // "the maximum sum of any non-empty contiguous subarray that MUST end at index i"
        //
        // This is a classic Kadane-style dynamic programming value.
        // At each position i, we have exactly two choices:
        // 1) Start a new subarray at i
        // 2) Extend the best subarray that ended at i - 1
        //
        // We use long because sums can exceed the range of int.
        long[] leftBestEndingAt = new long[n];

        // rightBestStartingAt[i] will store:
        // "the maximum sum of any non-empty contiguous subarray that MUST start at index i"
        //
        // This is the symmetric version of the left array.
        // At each position i, we have exactly two choices:
        // 1) Start a new subarray at i
        // 2) Extend the best subarray that started at i + 1
        long[] rightBestStartingAt = new long[n];

        // -----------------------------
        // Step 1: Build leftBestEndingAt
        // -----------------------------
        //
        // Base case:
        // At index 0, the only non-empty subarray ending at 0 is [sales[0]] itself.
        leftBestEndingAt[0] = sales[0];

        // Process from left to right.
        for (int i = 1; i < n; i++)
        {
            // Convert current value to long once so arithmetic stays in 64-bit.
            long current = sales[i];

            // If we extend the best subarray ending at i - 1, the new sum becomes:
            long extendPrevious = leftBestEndingAt[i - 1] + current;

            // If we start fresh at i, the sum is just the current element:
            long startNew = current;

            // We choose whichever is larger.
            //
            // Why this works:
            // Any best subarray ending at i must either:
            // - consist of only sales[i], or
            // - be some best subarray ending at i - 1, extended by sales[i]
            leftBestEndingAt[i] = Math.Max(startNew, extendPrevious);
        }

        // ------------------------------
        // Step 2: Build rightBestStartingAt
        // ------------------------------
        //
        // Base case:
        // At index n - 1, the only non-empty subarray starting there is [sales[n - 1]].
        rightBestStartingAt[n - 1] = sales[n - 1];

        // Process from right to left.
        for (int i = n - 2; i >= 0; i--)
        {
            long current = sales[i];

            // If we extend the best subarray starting at i + 1, while forcing inclusion of i:
            long extendNext = current + rightBestStartingAt[i + 1];

            // Or we can start and stop at i:
            long startNew = current;

            // Choose the better option.
            //
            // Why this works:
            // Any best subarray starting at i must either:
            // - be [sales[i]] alone, or
            // - be sales[i] followed by the best subarray starting at i + 1
            rightBestStartingAt[i] = Math.Max(startNew, extendNext);
        }

        // -----------------------------------------
        // Step 3: Try every promotion day p and combine
        // -----------------------------------------
        //
        // For each index p:
        // promotionScore = leftBestEndingAt[p] + rightBestStartingAt[p] - sales[p]
        //
        // We subtract sales[p] once because it appears in both parts.
        long answer = long.MinValue;

        for (int p = 0; p < n; p++)
        {
            long promotionScore = leftBestEndingAt[p] + rightBestStartingAt[p] - sales[p];

            if (promotionScore > answer)
            {
                answer = promotionScore;
            }
        }

        return answer;
    }
}

// --------------------------------------------------
// Demo code
// --------------------------------------------------

var solution = new Solution();

// Example 1
int[] sales1 = { 4, -2, 3, -1, 5 };
long result1 = solution.MaximumPromotionScore(sales1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", sales1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 9");
Console.WriteLine();

// Example 2
int[] sales2 = { -5, -2, -7 };
long result2 = solution.MaximumPromotionScore(sales2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", sales2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: -2");
Console.WriteLine();

// Additional demo
int[] sales3 = { 1, 2, 3 };
long result3 = solution.MaximumPromotionScore(sales3);
Console.WriteLine("Additional Demo:");
Console.WriteLine($"Input: [{string.Join(", ", sales3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Explanation: choosing the middle or last can capture the whole positive run.");
Console.WriteLine();

// Quick correctness notes in output for the provided examples.
Console.WriteLine("Verification:");
Console.WriteLine(result1 == 9 ? "Example 1 matches expected output." : "Example 1 does NOT match expected output.");
Console.WriteLine(result2 == -2 ? "Example 2 matches expected output." : "Example 2 does NOT match expected output.");