/*
Title: Maximum Charge Saved by Skipping One Noisy Sensor Block
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array readings where readings[i] represents the net battery impact of processing the i-th sensor reading in order.
A positive value means that processing that reading recharges the system by that amount, while a negative value means it consumes battery.

The device must process the stream from left to right, but it is allowed to skip exactly one contiguous block of readings.
Skipping a block means none of the values inside that block contribute to the final total.
The skipped block may be empty only if you choose not to skip anything.

Your task is to return the maximum possible total battery impact after processing the array and optionally skipping one contiguous block.

In other words, choose indices l and r with 0 <= l <= r < n and remove readings[l...r], or choose to remove nothing,
so that the sum of the remaining elements is as large as possible.

A correct solution must handle very large arrays efficiently.

Constraints:
- 1 <= readings.length <= 200000
- -1000000000 <= readings[i] <= 1000000000
- The answer fits in a signed 64-bit integer.

Examples:
1) readings = [4, -7, 3, 6, -2, 5]
   Output: 16
   Explanation: Skip the block [-7]. The remaining sum is 4 + 3 + 6 - 2 + 5 = 16.

2) readings = [-5, -2, -8]
   Output: 0
   Explanation: Skip the entire array. The remaining sum is 0, which is better than keeping any negative values.

Key Insight:
If the total sum of the array is TOTAL, and we remove a contiguous block with sum REMOVED,
then the remaining sum is:

    TOTAL - REMOVED

So to maximize the remaining sum, we want to minimize the sum of the removed block.

That means this problem becomes:
- Compute the total sum of the array
- Find the minimum-sum contiguous subarray
- Best answer is the larger of:
  1) total sum (remove nothing)
  2) total sum - minimumSubarraySum (remove the worst block)

This also naturally handles removing the entire array:
if the whole array is the minimum-sum subarray, then removing it gives 0.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We make one pass through the array.
    During that pass we compute:
    1) the total sum of all elements
    2) the minimum-sum contiguous subarray using a Kadane-style dynamic programming idea

    Why O(1) extra space?
    We only keep a few running variables:
    - totalSum
    - currentMinEndingHere
    - bestMinSubarraySum

    No extra arrays or lists are needed.
    */
    public long MaxBatteryImpactAfterSkippingOneBlock(int[] readings)
    {
        // This variable stores the sum of the entire array.
        // We need it because the final answer is:
        //     total sum - sum of removed block
        //
        // If we decide not to remove anything, the answer is simply totalSum.
        long totalSum = 0;

        // This variable represents:
        // "What is the minimum-sum subarray that MUST end at the current index?"
        //
        // This is the minimum-subarray version of Kadane's algorithm.
        //
        // At each new element x, we have two choices:
        // 1) Start a new removed block at x
        // 2) Extend the previous removed block by including x
        //
        // We choose whichever gives the smaller sum, because we want the block
        // we remove to be as negative as possible.
        long currentMinEndingHere = 0;

        // This variable stores the best (smallest) subarray sum seen anywhere so far.
        //
        // Once we know the minimum-sum contiguous block in the whole array,
        // removing it gives the maximum possible remaining total.
        long bestMinSubarraySum = long.MaxValue;

        for (int i = 0; i < readings.Length; i++)
        {
            long value = readings[i];

            // Step 1: Add the current value into the total array sum.
            //
            // This is necessary because our final formula depends on the total sum.
            totalSum += value;

            // Step 2: Update the minimum-sum subarray that ends exactly at index i.
            //
            // There are two possibilities:
            //
            // A) Start a new subarray at this element only:
            //      value
            //
            // B) Extend the previous minimum-ending-here subarray:
            //      currentMinEndingHere + value
            //
            // We want the smaller of these two, because we are searching for the
            // most negative / smallest-sum contiguous block to remove.
            //
            // Special case for i == 0:
            // The only subarray ending at index 0 is [readings[0]] itself.
            if (i == 0)
            {
                currentMinEndingHere = value;
            }
            else
            {
                currentMinEndingHere = Math.Min(value, currentMinEndingHere + value);
            }

            // Step 3: Update the global best minimum subarray sum.
            //
            // If the minimum-sum subarray ending at this index is better (smaller)
            // than anything we have seen before, record it.
            bestMinSubarraySum = Math.Min(bestMinSubarraySum, currentMinEndingHere);
        }

        // Option 1: Remove nothing.
        //
        // This is important because the problem explicitly allows skipping no block.
        // If every possible removed block would make the result worse, we should keep everything.
        long removeNothing = totalSum;

        // Option 2: Remove the minimum-sum contiguous block.
        //
        // Since remaining = total - removed,
        // and removed = bestMinSubarraySum,
        // this gives the best result among all non-empty removed blocks.
        long removeBestBlock = totalSum - bestMinSubarraySum;

        // Return the better of:
        // - removing nothing
        // - removing the worst contiguous block
        //
        // This also correctly handles all-negative arrays:
        // Example [-5, -2, -8]
        // total = -15
        // minimum subarray = -15 (the whole array)
        // removeBestBlock = -15 - (-15) = 0
        // max(-15, 0) = 0
        return Math.Max(removeNothing, removeBestBlock);
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// readings = [4, -7, 3, 6, -2, 5]
// total sum = 9
// minimum-sum subarray = [-7], sum = -7
// answer = 9 - (-7) = 16
int[] readings1 = { 4, -7, 3, 6, -2, 5 };
long result1 = solution.MaxBatteryImpactAfterSkippingOneBlock(readings1);
Console.WriteLine(result1); // Expected: 16

// Example 2:
// readings = [-5, -2, -8]
// total sum = -15
// minimum-sum subarray = entire array, sum = -15
// answer = -15 - (-15) = 0
int[] readings2 = { -5, -2, -8 };
long result2 = solution.MaxBatteryImpactAfterSkippingOneBlock(readings2);
Console.WriteLine(result2); // Expected: 0

// Additional demo:
// If all numbers are positive, removing nothing is best.
// readings = [2, 4, 6]
// total = 12
// minimum subarray = [2] with sum 2
// removing it gives 10, which is worse than 12
int[] readings3 = { 2, 4, 6 };
long result3 = solution.MaxBatteryImpactAfterSkippingOneBlock(readings3);
Console.WriteLine(result3); // Expected: 12

// Additional demo:
// readings = [5, -3, -4, 10]
// total = 8
// minimum subarray = [-3, -4] = -7
// answer = 8 - (-7) = 15
int[] readings4 = { 5, -3, -4, 10 };
long result4 = solution.MaxBatteryImpactAfterSkippingOneBlock(readings4);
Console.WriteLine(result4); // Expected: 15