/*
Title: Maximum Profit from One Restock and One Sale Window

Problem Description:
You are given an integer array prices where prices[i] is the market price of a product on day i.
A retailer wants to choose exactly one contiguous restock window and one contiguous sale window
such that the sale window starts strictly after the restock window ends.

The retailer buys one unit of product on every day in the restock window and sells one unit on
every day in the sale window.

The profit of this plan is defined as:
    (sum of prices in the sale window) - (sum of prices in the restock window)

Your task is to return the maximum possible profit.

Both windows must be:
- non-empty
- contiguous
- non-overlapping
- and the sale window must start strictly after the restock window ends

You are not required to use all days.

If every valid plan produces a negative profit, return the least negative value among all valid plans.

Constraints:
- 2 <= prices.length <= 200000
- -100000 <= prices[i] <= 100000
- The answer fits in a 64-bit signed integer
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Idea:
    We split the array between day i and day i+1.

    For every split:
    - the restock window must lie completely in prices[0..i]
    - the sale window must lie completely in prices[i+1..n-1]

    So for each split we want:
        bestSaleSumOnRight - smallestRestockSumOnLeft

    That means we need:
    1. For every index i, the minimum subarray sum anywhere in prices[0..i]
    2. For every index i, the maximum subarray sum anywhere in prices[i..n-1]

    Then the answer is:
        max over i from 0 to n-2 of (rightMax[i+1] - leftMin[i])

    This is a classic Kadane-style dynamic programming approach:
    - minimum subarray sum version on the left
    - maximum subarray sum version on the right
    */
    public long MaximumProfit(int[] prices)
    {
        int n = prices.Length;

        // leftMin[i] will store:
        // "the minimum sum of any non-empty contiguous subarray fully contained in prices[0..i]"
        //
        // Why do we need this?
        // Because the restock window should cost as little as possible.
        // A smaller restock sum means a larger profit after subtraction.
        //
        // Example:
        // If the left side is [4, -2, 3], the cheapest contiguous window is [-2] with sum -2.
        // That is better than [4] or [4, -2] or [3], because subtracting -2 increases profit.
        long[] leftMin = new long[n];

        // rightMax[i] will store:
        // "the maximum sum of any non-empty contiguous subarray fully contained in prices[i..n-1]"
        //
        // Why do we need this?
        // Because the sale window should earn as much as possible.
        // A larger sale sum means a larger profit.
        long[] rightMax = new long[n];

        // -----------------------------
        // Step 1: Build leftMin using a "minimum subarray Kadane"
        // -----------------------------
        //
        // We maintain:
        // currentMinEndingHere = minimum sum of a non-empty subarray that MUST end at current index
        //
        // Transition:
        // At index i, the minimum subarray ending at i is either:
        // - start fresh at prices[i]
        // - or extend the previous minimum-ending subarray by adding prices[i]
        //
        // So:
        // currentMinEndingHere = min(prices[i], currentMinEndingHere + prices[i])
        //
        // Then:
        // leftMin[i] = min(leftMin[i-1], currentMinEndingHere)
        //
        // This means leftMin[i] is the best (smallest) restock window seen anywhere up to i.
        long currentMinEndingHere = prices[0];
        leftMin[0] = prices[0];

        for (int i = 1; i < n; i++)
        {
            // Decide whether it is better to:
            // 1. start a new subarray at i
            // 2. extend the previous subarray that ended at i-1
            //
            // We choose the smaller sum because we want the minimum subarray.
            currentMinEndingHere = Math.Min(prices[i], currentMinEndingHere + prices[i]);

            // leftMin[i] should represent the smallest subarray sum seen anywhere in the prefix [0..i].
            // So we compare:
            // - the best answer from the previous prefix
            // - the best subarray that ends exactly at i
            leftMin[i] = Math.Min(leftMin[i - 1], currentMinEndingHere);
        }

        // -----------------------------
        // Step 2: Build rightMax using a "maximum subarray Kadane" from right to left
        // -----------------------------
        //
        // We maintain:
        // currentMaxStartingHere = maximum sum of a non-empty subarray that MUST start at current index
        //
        // Since we are moving from right to left, at index i the best subarray starting at i is either:
        // - start and stop at prices[i]
        // - or include prices[i] and continue into the best subarray starting at i+1
        //
        // So:
        // currentMaxStartingHere = max(prices[i], prices[i] + currentMaxStartingHere)
        //
        // Then:
        // rightMax[i] = max(rightMax[i+1], currentMaxStartingHere)
        //
        // This means rightMax[i] is the best sale window available anywhere in suffix [i..n-1].
        long currentMaxStartingHere = prices[n - 1];
        rightMax[n - 1] = prices[n - 1];

        for (int i = n - 2; i >= 0; i--)
        {
            // Decide whether it is better to:
            // 1. start a new sale window at i
            // 2. extend into the right side
            //
            // We choose the larger sum because we want the maximum sale revenue.
            currentMaxStartingHere = Math.Max(prices[i], prices[i] + currentMaxStartingHere);

            // rightMax[i] should represent the largest subarray sum seen anywhere in the suffix [i..n-1].
            // So we compare:
            // - the best answer from the suffix starting at i+1
            // - the best subarray that starts exactly at i
            rightMax[i] = Math.Max(rightMax[i + 1], currentMaxStartingHere);
        }

        // -----------------------------
        // Step 3: Try every valid split point
        // -----------------------------
        //
        // If we split between i and i+1:
        // - restock window must be fully on the left side [0..i]
        // - sale window must be fully on the right side [i+1..n-1]
        //
        // Then the best profit for this split is:
        //     rightMax[i+1] - leftMin[i]
        //
        // Why subtraction works:
        // profit = saleSum - restockSum
        // To maximize this:
        // - maximize saleSum
        // - minimize restockSum
        //
        // We examine all splits and keep the best result.
        long answer = long.MinValue;

        for (int i = 0; i < n - 1; i++)
        {
            long profitForThisSplit = rightMax[i + 1] - leftMin[i];
            answer = Math.Max(answer, profitForThisSplit);
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] prices1 = { 4, -2, 3, -1, 5 };
long result1 = solution.MaximumProfit(prices1);
Console.WriteLine($"Input: [{string.Join(", ", prices1)}]");
Console.WriteLine($"Maximum Profit: {result1}");

int[] prices2 = { 7, 6, 5, 4 };
long result2 = solution.MaximumProfit(prices2);
Console.WriteLine($"Input: [{string.Join(", ", prices2)}]");
Console.WriteLine($"Maximum Profit: {result2}");

// Additional quick checks
int[] prices3 = { 1, 2 };
long result3 = solution.MaximumProfit(prices3);
Console.WriteLine($"Input: [{string.Join(", ", prices3)}]");
Console.WriteLine($"Maximum Profit: {result3}");

int[] prices4 = { -5, -1, -3, 10 };
long result4 = solution.MaximumProfit(prices4);
Console.WriteLine($"Input: [{string.Join(", ", prices4)}]");
Console.WriteLine($"Maximum Profit: {result4}");