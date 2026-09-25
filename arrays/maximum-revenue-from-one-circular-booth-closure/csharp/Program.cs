/*
Title: Maximum Revenue from One Circular Booth Closure
Difficulty: Hard
Topic: Arrays

Problem Description:
A street festival has n food booths arranged in a circle. The i-th booth earns revenue[i] dollars if it stays open for the day. Due to a temporary power issue, the organizers must close exactly one contiguous block of booths. Because the street is circular, the closed block may wrap from the end of the array back to the beginning.

You are also given two integers, minClose and maxClose. The number of closed booths must be between minClose and maxClose, inclusive. After closing that single circular block, all remaining booths stay open, and your goal is to maximize the total revenue of the open booths.

Return the maximum total revenue that can remain open.

Formally, choose exactly one circular subarray of length L where minClose <= L <= maxClose, remove its sum from the total revenue, and maximize the sum of the remaining elements.

Constraints:
- 1 <= n <= 200000
- -10^9 <= revenue[i] <= 10^9
- 1 <= minClose <= maxClose <= n
- The chosen block must contain at least one booth and may contain all booths.

Key Insight:
Maximizing remaining revenue is the same as minimizing the sum of the closed circular block.

So:
answer = totalRevenue - (minimum sum of any circular subarray whose length is in [minClose, maxClose])

To handle circular subarrays efficiently:
1. Duplicate the array conceptually by building prefix sums over revenue + revenue.
2. Any circular subarray in the original circle becomes a normal subarray in the doubled array.
3. Restrict the starting index to the first n positions so we do not count the same circular block multiple times.
4. For each possible end position j in the doubled array, we want the largest prefix sum prefix[i]
   among valid start indices i, because:
      subarraySum(i..j-1) = prefix[j] - prefix[i]
   and to minimize this sum, for fixed j we want prefix[i] as large as possible.
5. Valid lengths are in [minClose, maxClose], so valid i satisfy:
      j - maxClose <= i <= j - minClose
   and also i must be in [0, n-1] because the circular block must start in the original array.

We maintain the maximum prefix value over that sliding window using a deque.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of complexity:
    - We build prefix sums for length 2n, which is O(n).
    - We scan end positions once from left to right.
    - Each index is inserted into and removed from the deque at most once.
    - Therefore the total deque work is linear.
    */
    public long MaximumRevenueAfterOneCircularClosure(int[] revenue, int minClose, int maxClose)
    {
        int n = revenue.Length;

        // Step 1:
        // Compute the total revenue of all booths.
        //
        // Why?
        // The final answer is:
        //   total revenue - sum of the closed block
        //
        // Since we want to maximize what remains open, we want to close the valid block
        // with the SMALLEST possible sum.
        long total = 0;
        for (int i = 0; i < n; i++)
        {
            total += revenue[i];
        }

        // Step 2:
        // Build prefix sums over the doubled array.
        //
        // Why double the array?
        // Circular subarrays are hard to handle directly because they may wrap around.
        // If we imagine the array repeated twice:
        //   revenue[0], revenue[1], ..., revenue[n-1], revenue[0], revenue[1], ...
        // then every circular block of length <= n becomes a normal contiguous subarray
        // in this doubled representation.
        //
        // prefix[k] = sum of first k elements of the doubled array.
        // Then the sum of subarray [i, j-1] is:
        //   prefix[j] - prefix[i]
        //
        // We need indices from 0 to 2n, inclusive, so the prefix array has length 2n + 1.
        long[] prefix = new long[2 * n + 1];
        for (int i = 0; i < 2 * n; i++)
        {
            prefix[i + 1] = prefix[i] + revenue[i % n];
        }

        // Step 3:
        // We will scan every possible end position j in the doubled array.
        //
        // For a chosen end j, a valid closed block is [i, j-1], where:
        //   length = j - i
        // and we require:
        //   minClose <= j - i <= maxClose
        //
        // Rearranging:
        //   j - maxClose <= i <= j - minClose
        //
        // Also, to represent each circular block exactly once, we only allow the start index i
        // to be in the first copy of the array:
        //   0 <= i <= n - 1
        //
        // For fixed j, the block sum is:
        //   prefix[j] - prefix[i]
        //
        // To MINIMIZE this sum, we want prefix[i] to be as LARGE as possible among valid i.
        //
        // Therefore, for each j, we need the maximum prefix[i] over a sliding window of valid i.
        //
        // Data structure choice:
        // We use a deque storing candidate indices i in decreasing order of prefix[i].
        // - Front of deque always holds the index with the largest prefix value.
        // - We remove indices that fall out of the valid window.
        LinkedList<int> deque = new LinkedList<int>();

        // This will store the minimum valid circular block sum found so far.
        long minClosedBlockSum = long.MaxValue;

        // Step 4:
        // Iterate over all possible end positions j in the doubled array.
        //
        // j is a prefix index, so the actual subarray ends at element j-1.
        for (int j = 1; j <= 2 * n; j++)
        {
            // Step 4a:
            // A new start index becomes eligible when its distance to j reaches minClose.
            //
            // Specifically, i = j - minClose is the newest index that now produces
            // a subarray of length exactly minClose.
            int addIndex = j - minClose;

            // We only allow start indices from the first copy of the array:
            //   0 <= i <= n - 1
            if (addIndex >= 0 && addIndex <= n - 1)
            {
                // Maintain deque in decreasing order of prefix values.
                //
                // Why decreasing?
                // Because for each j we want the maximum prefix[i].
                // If the new index has prefix value >= the back's prefix value,
                // then the back can never be better than the new one for any future j,
                // so we remove it.
                while (deque.Count > 0 && prefix[deque.Last!.Value] <= prefix[addIndex])
                {
                    deque.RemoveLast();
                }

                deque.AddLast(addIndex);
            }

            // Step 4b:
            // Remove indices that are too old, meaning they would create a block longer than maxClose.
            //
            // Valid i must satisfy:
            //   i >= j - maxClose
            int minAllowedStart = j - maxClose;
            while (deque.Count > 0 && deque.First!.Value < minAllowedStart)
            {
                deque.RemoveFirst();
            }

            // Step 4c:
            // If the deque is not empty, its front is the valid start index i
            // with the maximum prefix[i], which gives the minimum block sum for this j.
            if (deque.Count > 0)
            {
                int bestStart = deque.First!.Value;
                long currentClosedBlockSum = prefix[j] - prefix[bestStart];

                if (currentClosedBlockSum < minClosedBlockSum)
                {
                    minClosedBlockSum = currentClosedBlockSum;
                }
            }
        }

        // Step 5:
        // The best remaining open revenue is total revenue minus the minimum closed block sum.
        //
        // This works even when values are negative:
        // - If the minimum closed block sum is very negative, subtracting it increases the answer.
        // - If all blocks are positive, we remove the least harmful one.
        return total - minClosedBlockSum;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] revenue1 = { 8, -3, 5, -2, 4 };
int minClose1 = 2;
int maxClose1 = 3;
long result1 = solution.MaximumRevenueAfterOneCircularClosure(revenue1, minClose1, maxClose1);
Console.WriteLine(result1);

// Example 2
int[] revenue2 = { 6, -5, 7, -8, 3, 2 };
int minClose2 = 1;
int maxClose2 = 2;
long result2 = solution.MaximumRevenueAfterOneCircularClosure(revenue2, minClose2, maxClose2);
Console.WriteLine(result2);

// Additional quick checks

// Close all booths is allowed here.
int[] revenue3 = { 5, 1, 2 };
Console.WriteLine(solution.MaximumRevenueAfterOneCircularClosure(revenue3, 3, 3)); // 0

// Best is to close the most negative single booth.
int[] revenue4 = { -1, 10, -20, 5 };
Console.WriteLine(solution.MaximumRevenueAfterOneCircularClosure(revenue4, 1, 1)); // 14

// Wrapping case.
int[] revenue5 = { 4, 7, -10, 3 };
Console.WriteLine(solution.MaximumRevenueAfterOneCircularClosure(revenue5, 2, 2)); // close [3,4] wrapping sum 7? or [7,-10] sum -3 => total 4, answer 7