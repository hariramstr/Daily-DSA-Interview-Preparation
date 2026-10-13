/*
Title: Maximum Ribbon Cut Gain from One Interior Slice

Problem Description:
You are given an integer array values where values[i] represents the profit contribution of the i-th ribbon segment in a production line.
You must choose exactly one contiguous subarray to keep, but before finalizing it, you are allowed to make one special operation:
remove exactly one interior segment from that chosen subarray. The removed segment cannot be the first or last element of the chosen subarray.
After the removal, the remaining left and right parts are stitched together, and the score of the choice is the sum of all remaining kept segments.

Formally, choose indices l < k < r and maximize:
sum(values[l..r]) - values[k]

This means:
- l..r is one contiguous block we initially choose
- k is an index strictly inside that block
- after removing values[k], we keep a non-empty left part values[l..k-1]
  and a non-empty right part values[k+1..r]

Goal:
Return the maximum possible score.

Key Insight:
For every possible removed index k, the best score is:

(best sum of a subarray ending at k - 1)
+ (best sum of a subarray starting at k + 1)

Why?
Because once k is fixed as the removed interior element:
- the left kept part must end exactly at k - 1
- the right kept part must start exactly at k + 1
- both parts must be non-empty
- to maximize the total, we independently choose the best possible left-ending subarray
  and the best possible right-starting subarray

So the problem becomes:
1. Precompute bestLeftEnd[i] = maximum sum of any non-empty subarray ending at i
2. Precompute bestRightStart[i] = maximum sum of any non-empty subarray starting at i
3. Try every interior index k from 1 to n - 2:
   answer = max(answer, bestLeftEnd[k - 1] + bestRightStart[k + 1])

This is an O(n) solution.

Example 1:
values = [4, -7, 5, 6]

bestLeftEnd:
i=0 -> 4
i=1 -> max(-7, 4 + -7) = -3
i=2 -> max(5, -3 + 5) = 5
i=3 -> max(6, 5 + 6) = 11
=> [4, -3, 5, 11]

bestRightStart:
i=3 -> 6
i=2 -> max(5, 5 + 6) = 11
i=1 -> max(-7, -7 + 11) = 4
i=0 -> max(4, 4 + 4) = 8
=> [8, 4, 11, 6]

Try removed index k:
k=1 => bestLeftEnd[0] + bestRightStart[2] = 4 + 11 = 15
k=2 => bestLeftEnd[1] + bestRightStart[3] = -3 + 6 = 3
Answer = 15

Example 2:
values = [3, -2, 4, -1, 2]

bestLeftEnd:
[3, 1, 5, 4, 6]

bestRightStart:
[6, 3, 5, 1, 2]

Try removed index k:
k=1 => bestLeftEnd[0] + bestRightStart[2] = 3 + 5 = 8
k=2 => bestLeftEnd[1] + bestRightStart[3] = 1 + 1 = 2
k=3 => bestLeftEnd[2] + bestRightStart[4] = 5 + 2 = 7
Answer = 8
*/

using System;

public class Solution
{
    public long MaximumRibbonCutGain(int[] values)
    {
        /*
        Time Complexity: O(n)
        - We make one left-to-right pass to compute the best subarray sum ending at each index.
        - We make one right-to-left pass to compute the best subarray sum starting at each index.
        - We make one final pass over all valid removed positions.
        - Total work is linear in the array length.

        Space Complexity: O(n)
        - We store two arrays:
          1) bestLeftEnd
          2) bestRightStart
        - Each has size n.
        */

        int n = values.Length;

        // This array will store:
        // bestLeftEnd[i] = maximum sum of any NON-EMPTY contiguous subarray that ends exactly at index i.
        //
        // Why do we need this?
        // If we decide to remove index k, then the left kept part must end at k - 1.
        // Among all possible left parts ending there, we want the one with the largest sum.
        long[] bestLeftEnd = new long[n];

        // Base case:
        // The only non-empty subarray ending at index 0 is [values[0]] itself.
        bestLeftEnd[0] = values[0];

        // Build bestLeftEnd using a Kadane-style recurrence.
        for (int i = 1; i < n; i++)
        {
            // There are exactly two ways to form the best subarray ending at i:
            //
            // 1) Start fresh at i:
            //    sum = values[i]
            //
            // 2) Extend the best subarray that ended at i - 1:
            //    sum = bestLeftEnd[i - 1] + values[i]
            //
            // We choose whichever is larger.
            //
            // Why is this correct?
            // Any subarray ending at i either:
            // - consists of only values[i], or
            // - has some previous part ending at i - 1
            //
            // So this recurrence covers all possibilities.
            long startFresh = values[i];
            long extendPrevious = bestLeftEnd[i - 1] + values[i];
            bestLeftEnd[i] = Math.Max(startFresh, extendPrevious);
        }

        // This array will store:
        // bestRightStart[i] = maximum sum of any NON-EMPTY contiguous subarray that starts exactly at index i.
        //
        // Why do we need this?
        // If we remove index k, then the right kept part must start at k + 1.
        // Among all possible right parts starting there, we want the one with the largest sum.
        long[] bestRightStart = new long[n];

        // Base case:
        // The only non-empty subarray starting at the last index is that single element itself.
        bestRightStart[n - 1] = values[n - 1];

        // Build bestRightStart from right to left.
        for (int i = n - 2; i >= 0; i--)
        {
            // Symmetric reasoning to the left array:
            //
            // Any best subarray starting at i either:
            // 1) starts and ends at i only
            // 2) includes values[i] and extends into the best subarray starting at i + 1
            long startFresh = values[i];
            long extendNext = values[i] + bestRightStart[i + 1];
            bestRightStart[i] = Math.Max(startFresh, extendNext);
        }

        // We now try every valid removed index k.
        //
        // Important constraint:
        // The removed element must be strictly inside the chosen original subarray.
        // Therefore k cannot be the first or last index of the whole chosen block.
        //
        // Since both left and right kept parts must be non-empty:
        // - there must be at least one kept element before k
        // - there must be at least one kept element after k
        //
        // So valid k values are 1 through n - 2.
        long answer = long.MinValue;

        for (int k = 1; k <= n - 2; k++)
        {
            // If we remove values[k]:
            //
            // Best possible left contribution:
            //   bestLeftEnd[k - 1]
            // because the left kept part must end exactly at k - 1
            //
            // Best possible right contribution:
            //   bestRightStart[k + 1]
            // because the right kept part must start exactly at k + 1
            //
            // Their sum is the best score achievable with k as the removed interior element.
            long candidate = bestLeftEnd[k - 1] + bestRightStart[k + 1];

            if (candidate > answer)
            {
                answer = candidate;
            }
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

int[] values1 = { 4, -7, 5, 6 };
long result1 = solution.MaximumRibbonCutGain(values1);
Console.WriteLine(result1); // Expected: 15

int[] values2 = { 3, -2, 4, -1, 2 };
long result2 = solution.MaximumRibbonCutGain(values2);
Console.WriteLine(result2); // Expected: 8

// Additional quick checks
int[] values3 = { -5, -2, -3 };
long result3 = solution.MaximumRibbonCutGain(values3);
Console.WriteLine(result3); // Only possible removal is index 1 => -5 + -3 = -8

int[] values4 = { 10, -100, 20, 30, -5, 40 };
long result4 = solution.MaximumRibbonCutGain(values4);
Console.WriteLine(result4);