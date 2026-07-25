/*
Title: Count Balanced Shift Intervals

Problem Description:
A company records employee shift activity for a single day as an array `hours`, where `hours[i]`
is the number of hours worked during the `i`-th time block. Management defines a time block as
`heavy` if `hours[i] >= threshold`, otherwise it is `light`.

An interval is called balanced if it contains the same number of heavy blocks and light blocks.
Your task is to return the total number of balanced intervals in the array.

Formally, count the number of pairs `(l, r)` such that `0 <= l <= r < n` and in the subarray
`hours[l...r]`, the number of indices with `hours[i] >= threshold` is equal to the number of
indices with `hours[i] < threshold`.

Efficient idea:
- Convert each block into:
  +1 if it is heavy
  -1 if it is light
- Then a subarray is balanced exactly when its sum is 0
- Using prefix sums, a subarray `(l...r)` has sum 0 when:
      prefix[r + 1] == prefix[l]
- So for each prefix sum value, if it appears `k` times, it contributes:
      k choose 2
  balanced intervals.
- We can count this in one pass using a dictionary of prefix sum frequencies.

Example 1:
hours = [6, 3, 8, 2, 7], threshold = 5
Converted = [+1, -1, +1, -1, +1]
Prefix sums = [0, 1, 0, 1, 0, 1]
Equal prefix pairs:
- prefix 0 appears 3 times -> 3 choose 2 = 3
- prefix 1 appears 3 times -> 3 choose 2 = 3
Total = 6 balanced intervals

The balanced intervals are:
- [0, 1]
- [1, 2]
- [2, 3]
- [3, 4]
- [0, 3]
- [1, 4]

So the mathematically correct answer for Example 1 is 6.

Example 2:
hours = [4, 4, 9, 1], threshold = 4
Converted = [+1, +1, +1, -1]
Prefix sums = [0, 1, 2, 3, 2]
Equal prefix pairs:
- prefix 2 appears twice -> 1 balanced interval
Total = 1 balanced interval

The only balanced interval is:
- [2, 3]

So the mathematically correct answer for Example 2 is 1.

Important note:
The example outputs in the prompt are inconsistent with the formal definition.
This implementation follows the formal definition exactly and therefore returns the correct counts.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    We process the array once.
    Each step does O(1) average-time dictionary work.
    The dictionary stores frequencies of prefix sums, and there can be at most O(n) distinct sums.
    */
    public long CountBalancedIntervals(int[] hours, int threshold)
    {
        // This dictionary maps:
        //   prefixSumValue -> how many times we have seen this prefix sum so far
        //
        // Why do we need this?
        // A subarray is balanced when its converted sum is 0.
        // If current prefix sum is S, then any earlier prefix sum also equal to S
        // forms a zero-sum subarray between those two positions.
        //
        // So when we are at the current position and the current prefix sum is S:
        // - if S has appeared before `count` times,
        // - then there are exactly `count` balanced intervals ending here.
        var prefixFrequency = new Dictionary<int, long>();

        // Prefix sum before reading any element is 0.
        // This is extremely important because:
        // - if a prefix from index 0 to i is balanced,
        // - then its converted sum is 0,
        // - which means current prefix sum equals the initial prefix sum 0.
        //
        // So we must record that prefix sum 0 has already appeared once.
        prefixFrequency[0] = 1;

        // Running prefix sum of the converted array:
        // heavy -> +1
        // light -> -1
        int prefixSum = 0;

        // Total number of balanced intervals found so far.
        // We use long because the number of subarrays can be large:
        // up to n * (n + 1) / 2, which exceeds int for n up to 200,000.
        long answer = 0;

        // Process each time block one by one.
        foreach (int h in hours)
        {
            // Step 1: Convert the current block into +1 or -1.
            //
            // Why this transformation works:
            // - We want equal counts of heavy and light blocks.
            // - If heavy contributes +1 and light contributes -1,
            //   then a subarray has equal heavy and light counts exactly when:
            //       (+1 count) + (-1 count) = 0
            //   which means the subarray sum is 0.
            //
            // This turns a counting problem into a prefix-sum problem.
            prefixSum += (h >= threshold) ? 1 : -1;

            // Step 2: Check how many times this exact prefix sum has appeared before.
            //
            // Suppose current prefix sum is S.
            // For any earlier index j where prefixSumAtJ is also S,
            // the subarray between j and current position has sum 0.
            //
            // Therefore, every previous occurrence of S creates one new balanced interval.
            if (prefixFrequency.TryGetValue(prefixSum, out long seenCount))
            {
                // Add all those newly formed balanced intervals.
                answer += seenCount;

                // Then record that we have now seen this prefix sum one more time.
                prefixFrequency[prefixSum] = seenCount + 1;
            }
            else
            {
                // First time seeing this prefix sum.
                // No balanced interval is formed yet from previous equal prefixes,
                // because there are none.
                prefixFrequency[prefixSum] = 1;
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Sample 1 from the prompt
int[] hours1 = { 6, 3, 8, 2, 7 };
int threshold1 = 5;
long result1 = solution.CountBalancedIntervals(hours1, threshold1);
Console.WriteLine("Sample 1:");
Console.WriteLine($"hours = [{string.Join(", ", hours1)}], threshold = {threshold1}");
Console.WriteLine($"Balanced intervals = {result1}");
Console.WriteLine("Expected by formal definition: 6");
Console.WriteLine();

// Sample 2 from the prompt
int[] hours2 = { 4, 4, 9, 1 };
int threshold2 = 4;
long result2 = solution.CountBalancedIntervals(hours2, threshold2);
Console.WriteLine("Sample 2:");
Console.WriteLine($"hours = [{string.Join(", ", hours2)}], threshold = {threshold2}");
Console.WriteLine($"Balanced intervals = {result2}");
Console.WriteLine("Expected by formal definition: 1");
Console.WriteLine();

// Additional quick sanity check
int[] hours3 = { 1, 10, 2, 9 };
int threshold3 = 5;
// Converted: [-1, +1, -1, +1]
// Balanced intervals:
// [0,1], [1,2], [2,3], [0,3] => 4
long result3 = solution.CountBalancedIntervals(hours3, threshold3);
Console.WriteLine("Additional sanity check:");
Console.WriteLine($"hours = [{string.Join(", ", hours3)}], threshold = {threshold3}");
Console.WriteLine($"Balanced intervals = {result3}");
Console.WriteLine("Expected: 4");