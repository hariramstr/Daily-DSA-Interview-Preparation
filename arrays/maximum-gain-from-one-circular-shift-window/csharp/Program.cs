/*
Problem Title: Maximum Gain from One Circular Shift Window

Problem Description:
A factory runs in repeating shifts, so its hourly performance data is considered circular:
after the last hour, the next hour is the first hour again.

You are given an integer array gain where gain[i] is the net productivity change during hour i.
Positive values increase total output, and negative values reduce it.

Choose exactly one non-empty contiguous block of hours to analyze, where the block may wrap
from the end of the array back to the beginning because the schedule is circular.
Return the maximum possible sum of the chosen block.

In other words, find the largest sum among all non-empty circular subarrays of gain.

Examples:
1) gain = [5, -3, 5]
   Output: 10
   Explanation: The best circular block wraps around and takes the last 5 and the first 5.

2) gain = [-2, -3, -1]
   Output: -1
   Explanation: All values are negative, so the best non-empty block is the single value -1.

Key Idea:
For a circular array, the best answer is one of these two cases:
1. A normal (non-wrapping) maximum subarray.
2. A wrapping maximum subarray.

How do we compute the wrapping case?
- If a subarray wraps, that means we are taking:
  "everything except one middle contiguous block".
- So:
  wrapping maximum = total sum of array - minimum subarray sum

Important edge case:
- If all numbers are negative, then "total sum - minimum subarray sum" would incorrectly become 0,
  which would represent choosing no elements, but the problem requires a non-empty subarray.
- In that case, the answer must be the normal maximum subarray.

This leads to the final formula:
- If maxNormal < 0, return maxNormal
- Otherwise, return max(maxNormal, totalSum - minSubarray)
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We scan through the array once and maintain:
    - total sum of all elements
    - maximum subarray sum using Kadane's algorithm
    - minimum subarray sum using a mirrored Kadane's algorithm

    This is efficient enough for arrays up to 100,000 elements.
    */
    public int MaxSubarraySumCircular(int[] gain)
    {
        // We will keep track of the sum of the entire array.
        // This is needed because the best wrapping subarray can be computed as:
        // totalSum - minimumSubarraySum
        int totalSum = 0;

        // These variables are for the standard Kadane's algorithm
        // to find the maximum subarray sum in a normal, non-circular array.
        //
        // currentMax:
        //   The best subarray sum that MUST end at the current position.
        //
        // maxSum:
        //   The best subarray sum seen anywhere so far.
        int currentMax = gain[0];
        int maxSum = gain[0];

        // These variables are for finding the minimum subarray sum.
        // This is useful because a wrapping maximum subarray is equivalent to:
        // taking the whole array and removing the "worst" middle section.
        //
        // currentMin:
        //   The minimum subarray sum that MUST end at the current position.
        //
        // minSum:
        //   The minimum subarray sum seen anywhere so far.
        int currentMin = gain[0];
        int minSum = gain[0];

        // We now walk through every element in the array exactly once.
        for (int i = 0; i < gain.Length; i++)
        {
            int value = gain[i];

            // Add the current value into the total array sum.
            // We need this later for the wrapping case.
            totalSum += value;

            // For i == 0, we already used gain[0] to initialize all Kadane variables.
            // So we skip the update formulas on the first element to avoid double-processing it.
            if (i == 0)
            {
                continue;
            }

            // -----------------------------
            // Step 1: Update currentMax
            // -----------------------------
            // We want the best subarray sum that ends exactly at index i.
            //
            // There are only two possibilities:
            // 1. Start a brand-new subarray at i -> value
            // 2. Extend the previous best ending subarray -> currentMax + value
            //
            // We choose whichever is larger.
            currentMax = Math.Max(value, currentMax + value);

            // After updating currentMax, compare it with the best answer seen so far.
            // maxSum stores the global best non-wrapping subarray sum.
            maxSum = Math.Max(maxSum, currentMax);

            // -----------------------------
            // Step 2: Update currentMin
            // -----------------------------
            // This is the mirrored version of Kadane's algorithm.
            // Now we want the minimum subarray sum ending exactly at index i.
            //
            // Again there are two possibilities:
            // 1. Start a brand-new minimum subarray at i -> value
            // 2. Extend the previous minimum-ending subarray -> currentMin + value
            //
            // We choose whichever is smaller.
            currentMin = Math.Min(value, currentMin + value);

            // Update the global minimum subarray sum seen so far.
            minSum = Math.Min(minSum, currentMin);
        }

        // -----------------------------
        // Step 3: Handle the all-negative case
        // -----------------------------
        // If maxSum is negative, that means every number is negative.
        //
        // Why?
        // Because Kadane's maximum subarray would then be the largest single element,
        // and if even that is negative, there is no positive or zero-sum subarray.
        //
        // In this situation, the wrapping formula:
        // totalSum - minSum
        // would become 0, which corresponds to selecting no elements.
        // But the problem requires a NON-EMPTY subarray.
        //
        // Therefore, when all values are negative, the correct answer is simply maxSum.
        if (maxSum < 0)
        {
            return maxSum;
        }

        // -----------------------------
        // Step 4: Compute the wrapping answer
        // -----------------------------
        // If the best subarray wraps around the end to the beginning,
        // then it is equivalent to taking the whole array and excluding
        // one contiguous middle section.
        //
        // The best such exclusion is the minimum subarray sum.
        //
        // So:
        // wrappingSum = totalSum - minSum
        int wrappingSum = totalSum - minSum;

        // -----------------------------
        // Step 5: Return the better of the two valid cases
        // -----------------------------
        // Case A: best normal subarray
        // Case B: best wrapping subarray
        return Math.Max(maxSum, wrappingSum);
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Create an instance of the solution class.
var solution = new Solution();

// Example 1:
// gain = [5, -3, 5]
// Normal max subarray = 7 ([5, -3, 5])
// Wrapping max subarray = 10 ([5] from end + [5] from start)
// Expected output: 10
int[] gain1 = { 5, -3, 5 };
int result1 = solution.MaxSubarraySumCircular(gain1);
Console.WriteLine($"Input: [{string.Join(", ", gain1)}]");
Console.WriteLine($"Maximum circular subarray sum: {result1}");
Console.WriteLine("Expected: 10");
Console.WriteLine();

// Example 2:
// gain = [-2, -3, -1]
// All values are negative.
// Best non-empty subarray is [-1]
// Expected output: -1
int[] gain2 = { -2, -3, -1 };
int result2 = solution.MaxSubarraySumCircular(gain2);
Console.WriteLine($"Input: [{string.Join(", ", gain2)}]");
Console.WriteLine($"Maximum circular subarray sum: {result2}");
Console.WriteLine("Expected: -1");
Console.WriteLine();

// Additional demo:
// gain = [1, -2, 3, -2]
// Normal max subarray = 3
// Wrapping max subarray = 2
// Expected output: 3
int[] gain3 = { 1, -2, 3, -2 };
int result3 = solution.MaxSubarraySumCircular(gain3);
Console.WriteLine($"Input: [{string.Join(", ", gain3)}]");
Console.WriteLine($"Maximum circular subarray sum: {result3}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo:
// gain = [3, -1, 2, -1]
// Normal max subarray = 4
// Wrapping max subarray = 4
// Expected output: 4
int[] gain4 = { 3, -1, 2, -1 };
int result4 = solution.MaxSubarraySumCircular(gain4);
Console.WriteLine($"Input: [{string.Join(", ", gain4)}]");
Console.WriteLine($"Maximum circular subarray sum: {result4}");
Console.WriteLine("Expected: 4");