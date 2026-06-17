/*
Title: Minimum Recolors to Form a Centered Price Peak

Problem Description:
You are given an integer array prices representing daily prices of a product. A day i is called a centered peak of radius r if there are at least r elements on both sides of i, and the subarray from i - r to i + r forms a strict mountain centered at i. In other words, for every d from 1 to r, prices[i - d] < prices[i - d + 1] and prices[i + d - 1] > prices[i + d]. The value at index i is the unique highest value in that window.

You may recolor (change) any element to any integer value in one operation. Return the minimum number of elements that must be recolored so that the array contains at least one centered peak of exact radius k.

Only the elements inside the chosen window of length 2k + 1 matter. You may choose any valid center i such that the full window fits inside the array. A position already satisfying the required strict relation does not need to be changed.

Your task is to find the minimum number of changes over all possible windows of length 2k + 1.

Constraints:
- 1 <= prices.length <= 200000
- 0 <= prices[i] <= 1000000000
- 1 <= k
- 2 * k + 1 <= prices.length

Examples:
1) prices = [1, 3, 5, 4, 2, 6, 7], k = 2
   Output: 0
   Explanation: The window [1, 3, 5, 4, 2] already forms a strict mountain centered at index 2.

2) prices = [4, 4, 4, 4, 4, 4], k = 2
   Output: 4
   Explanation: Any valid window has length 5. A strict mountain needs 4 strict adjacent relations,
   and with all values equal, at least 4 positions must be changed.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Core idea:
    A window of length 2k + 1 centered at c is a strict mountain if and only if:
    1) The left half [c-k .. c] is strictly increasing.
    2) The right half [c .. c+k] is strictly decreasing.

    Since we may change any element independently, the minimum number of recolors inside a chosen window
    is equal to:
        (window length) - (maximum number of positions we can keep unchanged while still forming a mountain)

    A very important observation makes the problem easy:
    - In a strictly increasing sequence, if we keep a contiguous increasing run ending at the center,
      then all other positions on that side can be recolored to fit.
    - Similarly, on the right side, if we keep a contiguous decreasing run starting at the center,
      all other positions on that side can be recolored to fit.
    - Therefore, for a fixed center c:
         keepLeft  = length of the longest strictly increasing suffix ending at c, but capped at k + 1
         keepRight = length of the longest strictly decreasing prefix starting at c, but capped at k + 1
      The center is counted in both, so total kept positions are:
         keepLeft + keepRight - 1
      Thus changes needed are:
         (2k + 1) - (keepLeft + keepRight - 1)
       = 2k + 2 - keepLeft - keepRight

    Why this is correct:
    - The kept positions on the left must be contiguous up to the center; otherwise there would be a gap
      where an unchanged value could break the required strict order.
    - The same applies on the right.
    - Any positions not kept can always be assigned suitable values because integers are unbounded for this task.
      We can always choose values low enough / high enough to complete the mountain while preserving strictness.

    So the task reduces to computing:
    - incEnd[i]  = length of the longest strictly increasing contiguous subarray ending at i
    - decStart[i]= length of the longest strictly decreasing contiguous subarray starting at i

    Then for each valid center c:
        keepLeft  = min(incEnd[c], k + 1)
        keepRight = min(decStart[c], k + 1)
        changes   = 2k + 2 - keepLeft - keepRight
    and we take the minimum over all centers.
    */
    public int MinimumRecolorsToFormCenteredPeak(int[] prices, int k)
    {
        int n = prices.Length;

        // incEnd[i] means:
        // "How many consecutive positions can we keep unchanged in a strictly increasing chain
        //  that ends exactly at index i?"
        //
        // Example:
        // prices = [1, 3, 5, 4, 6]
        // incEnd = [1, 2, 3, 1, 2]
        //
        // Why this helps:
        // For a center c, the left side of the mountain must end at c and be strictly increasing.
        // So the best unchanged left-side segment is exactly the increasing suffix ending at c.
        int[] incEnd = new int[n];
        incEnd[0] = 1;

        for (int i = 1; i < n; i++)
        {
            // If prices[i - 1] < prices[i], then the increasing chain can continue.
            // Otherwise, the chain breaks and must restart at length 1.
            if (prices[i - 1] < prices[i])
            {
                incEnd[i] = incEnd[i - 1] + 1;
            }
            else
            {
                incEnd[i] = 1;
            }
        }

        // decStart[i] means:
        // "How many consecutive positions can we keep unchanged in a strictly decreasing chain
        //  that starts exactly at index i?"
        //
        // Example:
        // prices = [7, 5, 4, 6, 1]
        // decStart = [3, 2, 1, 2, 1]
        //
        // Why this helps:
        // For a center c, the right side of the mountain must start at c and be strictly decreasing.
        // So the best unchanged right-side segment is exactly the decreasing prefix starting at c.
        int[] decStart = new int[n];
        decStart[n - 1] = 1;

        for (int i = n - 2; i >= 0; i--)
        {
            // If prices[i] > prices[i + 1], then the decreasing chain can continue.
            // Otherwise, it breaks and restarts at length 1.
            if (prices[i] > prices[i + 1])
            {
                decStart[i] = decStart[i + 1] + 1;
            }
            else
            {
                decStart[i] = 1;
            }
        }

        int answer = int.MaxValue;

        // A valid center must have at least k elements on both sides.
        // Therefore c ranges from k to n - k - 1 inclusive.
        for (int c = k; c <= n - k - 1; c++)
        {
            // On the left side including the center, we only need at most k + 1 positions
            // because the window radius is exactly k.
            int keepLeft = Math.Min(incEnd[c], k + 1);

            // On the right side including the center, same cap.
            int keepRight = Math.Min(decStart[c], k + 1);

            // The center belongs to both kept segments, so subtract 1 once.
            int keptTotal = keepLeft + keepRight - 1;

            // Window length is exactly 2k + 1.
            int changes = (2 * k + 1) - keptTotal;

            if (changes < answer)
            {
                answer = changes;
            }
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] prices1 = { 1, 3, 5, 4, 2, 6, 7 };
int k1 = 2;
int result1 = solution.MinimumRecolorsToFormCenteredPeak(prices1, k1);
Console.WriteLine(result1); // Expected: 0

int[] prices2 = { 4, 4, 4, 4, 4, 4 };
int k2 = 2;
int result2 = solution.MinimumRecolorsToFormCenteredPeak(prices2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional quick checks
int[] prices3 = { 1, 2, 3, 4, 5 };
int k3 = 1;
int result3 = solution.MinimumRecolorsToFormCenteredPeak(prices3, k3);
Console.WriteLine(result3);

int[] prices4 = { 5, 4, 3, 2, 1 };
int k4 = 1;
int result4 = solution.MinimumRecolorsToFormCenteredPeak(prices4, k4);
Console.WriteLine(result4);