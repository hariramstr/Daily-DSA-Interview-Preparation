/*
Title: Maximum Alternating Gain from a Contiguous Trade Window

Problem Description:
You are given an integer array profits where profits[i] represents the net profit or loss of the i-th trade executed during a day.
A risk analyst wants to choose exactly one contiguous window of trades and evaluate it with an alternating sign rule:
the first chosen trade contributes normally, the second is subtracted, the third is added, the fourth is subtracted, and so on.

For a chosen subarray profits[l..r], its score is:
profits[l] - profits[l+1] + profits[l+2] - profits[l+3] + ...

Return the maximum possible alternating score over all non-empty contiguous subarrays.

The chosen window must remain contiguous, but you may start and end anywhere in the array.
Values may be positive, zero, or negative.

Constraints:
- 1 <= profits.length <= 200000
- -1000000000 <= profits[i] <= 1000000000
- The answer fits in a signed 64-bit integer.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Idea:
    We process the array from left to right and maintain two dynamic programming states:

    1. endWithPlus:
       The maximum alternating score of a non-empty contiguous subarray that ends at the current index,
       where the current element is taken with a '+' sign.

    2. endWithMinus:
       The maximum alternating score of a non-empty contiguous subarray that ends at the current index,
       where the current element is taken with a '-' sign.

    Why these two states are enough:
    - If the current element is used with '+' sign, then either:
      a) we start a brand-new subarray at this index, so score = profits[i]
      b) we extend a previous subarray that ended with '-' sign, so score = previous endWithMinus + profits[i]

    - If the current element is used with '-' sign, then it must extend a previous subarray that ended with '+' sign,
      because signs must alternate. So:
      endWithMinus = previous endWithPlus - profits[i]

    The answer is the maximum value ever seen among valid states, especially endWithPlus and endWithMinus,
    because the best subarray can end anywhere.
    */
    public long MaxAlternatingScore(int[] profits)
    {
        // We use long because:
        // - each profits[i] can be as large as 1e9 in magnitude
        // - the array can be very large
        // - the problem explicitly says the final answer fits in signed 64-bit integer
        long first = profits[0];

        // At index 0, the only possible non-empty subarray is [profits[0]].
        // Its first element is always taken with '+' sign.
        long endWithPlus = first;

        // At index 0, it is impossible for a non-empty subarray ending here
        // to use the current element with '-' sign, because a subarray cannot start with '-'.
        // We use a very small sentinel value to represent "invalid state".
        long endWithMinus = long.MinValue / 4;

        // The best answer seen so far starts as the single-element subarray [profits[0]].
        long answer = first;

        // Process each remaining element one by one.
        for (int i = 1; i < profits.Length; i++)
        {
            long x = profits[i];

            // Save previous states before updating.
            // This is necessary because both new states depend on the old states from index i - 1.
            long previousEndWithPlus = endWithPlus;
            long previousEndWithMinus = endWithMinus;

            // Compute the best subarray ending at i where profits[i] gets a '+' sign.
            //
            // There are exactly two ways this can happen:
            //
            // 1. Start a new subarray at i:
            //    score = x
            //
            // 2. Extend a previous subarray that ended at i - 1 with a '-' sign:
            //    previous alternating pattern ... - previousElement
            //    now current element must be '+'
            //    score = previousEndWithMinus + x
            //
            // We take the better of these two choices.
            endWithPlus = Math.Max(x, previousEndWithMinus + x);

            // Compute the best subarray ending at i where profits[i] gets a '-' sign.
            //
            // This cannot start a new subarray, because the first element of any chosen window
            // must always have '+' sign.
            //
            // Therefore, the only valid way is to extend a previous subarray that ended with '+' sign:
            // new score = previousEndWithPlus - x
            endWithMinus = previousEndWithPlus - x;

            // Update the global answer.
            //
            // Why check both?
            // Because the best subarray may end at this index with either:
            // - an odd length (current sign '+')
            // - an even length (current sign '-')
            //
            // Both are valid completed subarrays.
            answer = Math.Max(answer, Math.Max(endWithPlus, endWithMinus));
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] profits1 = { 4, 2, 5, 3 };
long result1 = solution.MaxAlternatingScore(profits1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", profits1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 7");
Console.WriteLine();

// Example 2
// Careful verification:
// All non-empty contiguous subarrays:
// [5] = 5
// [5, -1] = 5 - (-1) = 6
// [5, -1, -3] = 5 - (-1) + (-3) = 3
// [5, -1, -3, 4] = 5 - (-1) + (-3) - 4 = -1
// [-1] = -1
// [-1, -3] = -1 - (-3) = 2
// [-1, -3, 4] = -1 - (-3) + 4 = 6
// [-3] = -3
// [-3, 4] = -3 - 4 = -7
// [4] = 4
// Maximum = 6
int[] profits2 = { 5, -1, -3, 4 };
long result2 = solution.MaxAlternatingScore(profits2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", profits2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Additional demo cases for learning

int[] profits3 = { 7 };
long result3 = solution.MaxAlternatingScore(profits3);
Console.WriteLine("Additional Example 3:");
Console.WriteLine($"Input: [{string.Join(", ", profits3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 7");
Console.WriteLine();

int[] profits4 = { -5, -2, -8 };
long result4 = solution.MaxAlternatingScore(profits4);
Console.WriteLine("Additional Example 4:");
Console.WriteLine($"Input: [{string.Join(", ", profits4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("One optimal subarray is [-5, -2] => -5 - (-2) = -3");
Console.WriteLine();

int[] profits5 = { 3, 10, 1, 7 };
long result5 = solution.MaxAlternatingScore(profits5);
Console.WriteLine("Additional Example 5:");
Console.WriteLine($"Input: [{string.Join(", ", profits5)}]");
Console.WriteLine($"Output: {result5}");
Console.WriteLine();