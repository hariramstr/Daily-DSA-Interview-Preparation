/*
Title: Maximum Net Gain from One Interior Refund Block

Problem Description:
You are given an integer array transactions where transactions[i] represents the net profit or loss
from the i-th transaction of a day. Positive values are profits and negative values are losses.
You are also given an integer baseGain, representing a fixed gain that is always counted.

You must choose exactly one contiguous interior block of transactions to mark as refunded.
Refunding a block means its total contribution is subtracted from the day's result instead of added.
If the chosen block has sum S, then the final result becomes:

    baseGain + totalSum(transactions) - 2 * S

The refunded block must be an interior block:
- it cannot start at index 0
- it cannot end at index n - 1
- it must contain at least one element

Return the maximum possible final result after choosing one valid interior block.

Key observation:
Because baseGain and totalSum(transactions) are fixed, maximizing

    baseGain + totalSum - 2 * S

is exactly the same as minimizing S.

So the problem becomes:
Find the minimum-sum contiguous subarray that lies completely inside indices [1 .. n-2].

That is a classic minimum subarray sum problem, which can be solved in O(n) time using
a Kadane-style dynamic programming scan over only the interior range.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Explanation of the approach:
    1. Compute the total sum of the entire array.
    2. Among only the interior indices [1 .. n-2], find the minimum-sum contiguous subarray.
       We do this with a Kadane-style scan for minimum subarray sum:
       - minEndingHere = minimum sum of a subarray that must end at the current index
       - minSoFar      = best (smallest) subarray sum seen anywhere so far
    3. Once the minimum interior block sum is known, plug it into:
           answer = baseGain + totalSum - 2 * minInteriorSum
    */
    public long MaximumNetGain(int[] transactions, int baseGain)
    {
        int n = transactions.Length;

        // Step 1:
        // Compute the sum of the entire transactions array.
        //
        // Why do we need this?
        // The final formula is:
        //     baseGain + totalSum(transactions) - 2 * refundedBlockSum
        //
        // Since totalSum(transactions) is part of the final answer no matter which block we choose,
        // we must know it exactly.
        //
        // We use long instead of int because:
        // - n can be as large as 200,000
        // - each value can be as large as 1e9 in magnitude
        // The total can exceed 32-bit integer range, so 64-bit is required.
        long totalSum = 0;
        foreach (int value in transactions)
        {
            totalSum += value;
        }

        // Step 2:
        // We must choose exactly one valid interior block.
        // Valid indices are from 1 to n - 2 inclusive.
        //
        // Since n >= 3, there is always at least one interior element.
        // Example:
        // - if n = 3, the only interior index is 1, so the only valid block is [1..1]
        //
        // We now solve:
        // "What is the minimum-sum contiguous subarray inside transactions[1..n-2]?"
        //
        // This is the minimum-subarray version of Kadane's algorithm.
        //
        // Definitions:
        // - minEndingHere:
        //     the minimum possible sum of a contiguous subarray that MUST end at the current index
        //
        // - minSoFar:
        //     the minimum subarray sum seen anywhere in the interior range so far
        //
        // Initialization:
        // We start at index 1 because index 0 is forbidden.
        long minEndingHere = transactions[1];
        long minSoFar = transactions[1];

        // Step 3:
        // Scan through the remaining interior indices.
        //
        // For each interior index i, we decide:
        // - either start a new subarray at i
        // - or extend the previous minimum-ending subarray by including transactions[i]
        //
        // Since we want the minimum sum, the recurrence is:
        //     minEndingHere = min(transactions[i], minEndingHere + transactions[i])
        //
        // Why does this work?
        // Any minimum-sum subarray ending at i must be one of:
        // - just the single element transactions[i]
        // - some minimum-sum subarray ending at i-1, extended by transactions[i]
        //
        // Then we update minSoFar with the best (smallest) value found.
        for (int i = 2; i <= n - 2; i++)
        {
            long current = transactions[i];

            // Decide whether it is better (smaller sum) to:
            // 1) start fresh at the current element
            // 2) extend the previous subarray
            minEndingHere = Math.Min(current, minEndingHere + current);

            // Record the best minimum subarray sum seen so far.
            minSoFar = Math.Min(minSoFar, minEndingHere);
        }

        // Step 4:
        // Convert the minimum interior block sum into the maximum final result.
        //
        // Original final result:
        //     baseGain + totalSum - 2 * refundedBlockSum
        //
        // To maximize this, we choose the smallest possible refundedBlockSum,
        // which is exactly minSoFar.
        long answer = (long)baseGain + totalSum - 2L * minSoFar;

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] transactions1 = { 4, -7, 3, -2, 5 };
int baseGain1 = 10;
long result1 = solution.MaximumNetGain(transactions1, baseGain1);
Console.WriteLine(result1); // Expected: 27

// Example 2
int[] transactions2 = { 8, 2, 6 };
int baseGain2 = -5;
long result2 = solution.MaximumNetGain(transactions2, baseGain2);
Console.WriteLine(result2); // Expected: 7

// Additional quick sanity checks

// Interior range is [1..3], best minimum block is [-4, 2, -1] = -3
// total = 5 + (-4) + 2 + (-1) + 7 = 9
// answer = 0 + 9 - 2*(-3) = 15
int[] transactions3 = { 5, -4, 2, -1, 7 };
int baseGain3 = 0;
long result3 = solution.MaximumNetGain(transactions3, baseGain3);
Console.WriteLine(result3); // Expected: 15

// Only one valid interior block: [1]
// total = 1 + (-10) + 2 = -7
// answer = 3 + (-7) - 2*(-10) = 16
int[] transactions4 = { 1, -10, 2 };
int baseGain4 = 3;
long result4 = solution.MaximumNetGain(transactions4, baseGain4);
Console.WriteLine(result4); // Expected: 16