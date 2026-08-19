/*
Title: Longest Restock Streak with One Overstock Removal
Difficulty: Medium
Topic: Arrays

Problem Description:
A warehouse records the number of units restocked each day in an integer array `restocks`,
where `restocks[i]` is the number of units added on day `i`.

Management wants to identify the longest streak of days that looks steadily improving.
A streak is considered steadily improving if, after optionally removing at most one day
from that streak, the remaining days form a strictly increasing sequence.

Your task is to return the maximum possible length of a contiguous streak satisfying this rule.
The removed day, if any, must come from inside the chosen streak, and removing it should connect
the left and right parts into one strictly increasing sequence. You are not allowed to reorder days;
only one deletion is permitted.

Example:
- [3, 5, 4, 6, 7] -> answer is 5, because removing 4 leaves [3, 5, 6, 7]
- [1, 2, 3, 2, 3, 4] -> answer is 4

Constraints:
- 1 <= restocks.length <= 200000
- -10^9 <= restocks[i] <= 10^9
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Idea:
    We precompute two helper arrays:

    1) incLeft[i]
       = length of the strictly increasing contiguous subarray that ends at index i

    2) incRight[i]
       = length of the strictly increasing contiguous subarray that starts at index i

    Then we consider three possibilities:
    - Use a streak that is already strictly increasing with no deletion
    - Delete one element at position i and connect the increasing part on the left
      with the increasing part on the right, if the bridge condition works
    - Delete the first or last element of a chosen streak, which is naturally covered
      by taking a fully increasing segment or by one-sided lengths

    Important note about what we return:
    The problem asks for the length of the original contiguous streak before deletion.
    So if deleting one element at i allows us to connect a left increasing block of length L
    and a right increasing block of length R, then the original valid streak length is L + 1 + R.
    The "+1" is the deleted element itself, because it still belongs to the chosen streak.

    Bridge condition:
    If we delete restocks[i], then the remaining sequence is strictly increasing only if:
    restocks[i - 1] < restocks[i + 1]
    This ensures the left and right remaining parts connect correctly.
    */
    public int LongestRestockStreak(int[] restocks)
    {
        int n = restocks.Length;

        // If there is only one day, that single day is trivially a valid streak.
        // We can choose it and perform no deletion.
        if (n == 1)
        {
            return 1;
        }

        // incLeft[i] tells us:
        // "How long is the strictly increasing contiguous segment ending exactly at i?"
        //
        // Example:
        // restocks = [3, 5, 4, 6, 7]
        // incLeft = [1, 2, 1, 2, 3]
        //
        // Why this is useful:
        // If we want to delete some middle element i, then the left side that remains
        // must be a strictly increasing suffix ending at i - 1.
        int[] incLeft = new int[n];
        incLeft[0] = 1;

        for (int i = 1; i < n; i++)
        {
            // If current value is greater than previous value,
            // then the increasing segment can continue.
            if (restocks[i] > restocks[i - 1])
            {
                incLeft[i] = incLeft[i - 1] + 1;
            }
            else
            {
                // Otherwise, a new increasing segment starts here.
                incLeft[i] = 1;
            }
        }

        // incRight[i] tells us:
        // "How long is the strictly increasing contiguous segment starting exactly at i?"
        //
        // Example:
        // restocks = [3, 5, 4, 6, 7]
        // incRight = [2, 1, 3, 2, 1]
        //
        // Why this is useful:
        // If we delete some middle element i, then the right side that remains
        // must be a strictly increasing prefix starting at i + 1.
        int[] incRight = new int[n];
        incRight[n - 1] = 1;

        for (int i = n - 2; i >= 0; i--)
        {
            // If current value is less than next value,
            // then the increasing segment can continue to the right.
            if (restocks[i] < restocks[i + 1])
            {
                incRight[i] = incRight[i + 1] + 1;
            }
            else
            {
                // Otherwise, a new increasing segment starts at i.
                incRight[i] = 1;
            }
        }

        // Start with the best answer among all already-strictly-increasing segments.
        // This covers the "optional deletion" case where we choose not to delete anything.
        int answer = 1;
        for (int i = 0; i < n; i++)
        {
            answer = Math.Max(answer, incLeft[i]);
        }

        // Now try deleting each possible index i.
        //
        // We want the longest contiguous streak [l..r] such that deleting restocks[i]
        // makes the remaining values strictly increasing.
        //
        // There are three structural cases:
        //
        // 1) Delete the first element of the chosen streak:
        //    Then the remaining part is just an increasing segment starting at i + 1.
        //    Original streak length = 1 + incRight[i + 1]
        //
        // 2) Delete the last element of the chosen streak:
        //    Then the remaining part is just an increasing segment ending at i - 1.
        //    Original streak length = incLeft[i - 1] + 1
        //
        // 3) Delete a middle element:
        //    Left part ends at i - 1, right part starts at i + 1,
        //    and they can be connected only if restocks[i - 1] < restocks[i + 1].
        //    Original streak length = incLeft[i - 1] + 1 + incRight[i + 1]
        //
        // We evaluate all of these in O(1) per index.
        for (int i = 0; i < n; i++)
        {
            // Case 1: delete restocks[i] and use only a right increasing segment after it.
            // This corresponds to choosing a streak that starts at i and continues right.
            if (i + 1 < n)
            {
                int candidate = 1 + incRight[i + 1];
                answer = Math.Max(answer, candidate);
            }
            else
            {
                // Deleting the last element alone gives a streak of length 1.
                answer = Math.Max(answer, 1);
            }

            // Case 2: delete restocks[i] and use only a left increasing segment before it.
            // This corresponds to choosing a streak that ends at i.
            if (i - 1 >= 0)
            {
                int candidate = incLeft[i - 1] + 1;
                answer = Math.Max(answer, candidate);
            }
            else
            {
                // Deleting the first element alone gives a streak of length 1.
                answer = Math.Max(answer, 1);
            }

            // Case 3: delete a middle element and connect both sides.
            if (i - 1 >= 0 && i + 1 < n)
            {
                // This comparison is the key correctness condition.
                //
                // Why do we need it?
                // After deleting restocks[i], the last value on the left side becomes restocks[i - 1],
                // and the first value on the right side becomes restocks[i + 1].
                //
                // For the combined remaining sequence to be strictly increasing,
                // we must have:
                // restocks[i - 1] < restocks[i + 1]
                if (restocks[i - 1] < restocks[i + 1])
                {
                    int candidate = incLeft[i - 1] + 1 + incRight[i + 1];
                    answer = Math.Max(answer, candidate);
                }
            }
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] sample1 = { 3, 5, 4, 6, 7 };
int result1 = solution.LongestRestockStreak(sample1);
Console.WriteLine($"Input: [{string.Join(", ", sample1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

int[] sample2 = { 1, 2, 3, 2, 3, 4 };
int result2 = solution.LongestRestockStreak(sample2);
Console.WriteLine($"Input: [{string.Join(", ", sample2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] sample3 = { 2, 2, 3 };
int result3 = solution.LongestRestockStreak(sample3);
Console.WriteLine($"Input: [{string.Join(", ", sample3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

int[] sample4 = { 1 };
int result4 = solution.LongestRestockStreak(sample4);
Console.WriteLine($"Input: [{string.Join(", ", sample4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

int[] sample5 = { 5, 4, 3, 2 };
int result5 = solution.LongestRestockStreak(sample5);
Console.WriteLine($"Input: [{string.Join(", ", sample5)}]");
Console.WriteLine($"Output: {result5}");
Console.WriteLine("Expected: 2");
Console.WriteLine();

int[] sample6 = { 1, 3, 2, 4 };
int result6 = solution.LongestRestockStreak(sample6);
Console.WriteLine($"Input: [{string.Join(", ", sample6)}]");
Console.WriteLine($"Output: {result6}");
Console.WriteLine("Expected: 4");
Console.WriteLine();