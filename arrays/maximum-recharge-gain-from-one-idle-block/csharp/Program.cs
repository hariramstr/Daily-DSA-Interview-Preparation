/*
Title: Maximum Recharge Gain from One Idle Block

Problem Description:
A mobile device records its battery change each minute during a long session.
You are given an integer array changes, where changes[i] is the net battery change
during minute i. A positive value means the battery increased during that minute,
and a negative value means the battery drained.

The device firmware allows exactly one optimization: you may choose one contiguous
block of minutes and mark it as an idle block. During the idle block, every battery
change in that block is inverted in sign. In other words, each x in the chosen block
becomes -x. You must choose exactly one non-empty contiguous block.

Return the maximum possible total battery change after applying this optimization once.

Important insight:
If the original total sum is S, and we flip a subarray whose sum is sub,
then that subarray changes from contributing +sub to contributing -sub.
So the final total becomes:

    S - 2 * sub

Therefore, to maximize the final total, we must minimize the sum of the chosen
non-empty contiguous subarray.

So the problem becomes:
1. Compute the total sum of the array.
2. Find the minimum-sum non-empty contiguous subarray.
3. Return totalSum - 2 * minSubarraySum.

This can be done in linear time using a Kadane-style algorithm for minimum subarray sum.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We scan the array once.
    - We keep track of the total sum of the entire array.
    - We also keep track of the minimum-sum contiguous subarray seen so far.

    Why minimum subarray sum?
    Because flipping a block with sum = sub changes the total by -2 * sub.
    If sub is very negative, then -2 * sub is a large positive gain.
    If all values are positive, we still must flip exactly one non-empty block,
    so we choose the smallest positive single block or segment, which is exactly
    what the minimum-subarray algorithm will find.
    */
    public long MaxRechargeGainFromOneIdleBlock(int[] changes)
    {
        // This variable stores the sum of the entire original array.
        // We use long because:
        // - The array can be large (up to 200,000 elements)
        // - Each value can be up to 100,000 in magnitude
        // The total can exceed the range of int, so long is safer and required.
        long totalSum = 0;

        // We will use a Kadane-style approach, but instead of finding the maximum
        // subarray sum, we find the minimum subarray sum.
        //
        // currentMinEndingHere:
        //   The minimum sum of a contiguous subarray that MUST end at the current index.
        //
        // bestMinSoFar:
        //   The minimum sum of ANY contiguous subarray seen anywhere so far.
        //
        // We initialize them with the first element so that:
        // - the chosen block is guaranteed to be non-empty
        // - the logic works correctly even for arrays of length 1
        long currentMinEndingHere = changes[0];
        long bestMinSoFar = changes[0];

        // Add the first element to the total sum.
        totalSum += changes[0];

        // Process the rest of the array from left to right.
        for (int i = 1; i < changes.Length; i++)
        {
            // Convert the current value to long once, so all later arithmetic
            // stays in 64-bit integer space.
            long value = changes[i];

            // Add this value to the total sum of the whole array.
            totalSum += value;

            // We now decide how to build the minimum-sum subarray ending at index i.
            //
            // There are exactly two possibilities:
            //
            // 1. Start a brand-new subarray at index i
            //    Sum = value
            //
            // 2. Extend the previous minimum-ending-here subarray by appending value
            //    Sum = currentMinEndingHere + value
            //
            // Since we want the MINIMUM sum, we choose the smaller of these two.
            //
            // Why this works:
            // If the previous subarray helps make the sum smaller, we keep it.
            // If it makes things worse, we discard it and start fresh here.
            currentMinEndingHere = Math.Min(value, currentMinEndingHere + value);

            // Update the best overall minimum subarray sum found so far.
            //
            // currentMinEndingHere is the best minimum subarray that ends exactly at i.
            // bestMinSoFar is the best minimum subarray anywhere from index 0..i.
            bestMinSoFar = Math.Min(bestMinSoFar, currentMinEndingHere);
        }

        // If we flip a subarray with sum = bestMinSoFar,
        // the final total becomes:
        //
        // totalSum - 2 * bestMinSoFar
        //
        // Explanation:
        // Original contribution of that block = +bestMinSoFar
        // New contribution after flipping = -bestMinSoFar
        // Net change = (-bestMinSoFar) - (+bestMinSoFar) = -2 * bestMinSoFar
        //
        // So:
        // final = totalSum + (-2 * bestMinSoFar)
        //       = totalSum - 2 * bestMinSoFar
        long answer = totalSum - 2L * bestMinSoFar;

        return answer;
    }
}

// Demo code:
// We create the sample inputs from the problem statement,
// call the solution method, and print the results.

var solution = new Solution();

// Example 1:
// changes = [4, -7, 3, -2]
// Original total = 4 + (-7) + 3 + (-2) = -2
// Minimum subarray sum is -7 (subarray [-7])
// Final answer = -2 - 2 * (-7) = -2 + 14 = 12
int[] changes1 = { 4, -7, 3, -2 };
long result1 = solution.MaxRechargeGainFromOneIdleBlock(changes1);
Console.WriteLine(result1); // Expected: 12

// Example 2:
// changes = [5, 2, 4]
// Original total = 11
// Minimum subarray sum is 4 (subarray [4])
// Final answer = 11 - 2 * 4 = 3
int[] changes2 = { 5, 2, 4 };
long result2 = solution.MaxRechargeGainFromOneIdleBlock(changes2);
Console.WriteLine(result2); // Expected: 3

// Additional quick checks:

// Single element negative:
// changes = [-8]
// Original total = -8
// Must flip exactly one non-empty block => flip [-8] to [8]
// Result = 8
int[] changes3 = { -8 };
Console.WriteLine(solution.MaxRechargeGainFromOneIdleBlock(changes3)); // Expected: 8

// Mixed values:
// changes = [2, -1, -3, 4]
// Original total = 2
// Minimum subarray sum is -4 (subarray [-1, -3])
// Result = 2 - 2 * (-4) = 10
int[] changes4 = { 2, -1, -3, 4 };
Console.WriteLine(solution.MaxRechargeGainFromOneIdleBlock(changes4)); // Expected: 10