/*
Title: Longest Stable Price Span After One Correction

Problem Description:
A retail analytics team records the price of the same product once per day in an integer array prices,
where prices[i] is the observed price on day i.

A span of days is considered stable if all values in that contiguous subarray are equal.
However, the team knows that at most one recorded day in a span may be wrong due to a data entry mistake.
You are allowed to correct the value of at most one element inside a chosen contiguous subarray to any integer you want.

Return the length of the longest contiguous subarray that can be made stable after applying at most one correction.

In other words, find the maximum length of a subarray such that by changing zero or one element in that subarray,
every value in the subarray can become identical.

Constraints:
- 1 <= prices.length <= 2 * 10^5
- 1 <= prices[i] <= 10^9

Examples:
1) prices = [5,5,7,5,5,5]
   Output: 6
   Explanation: Change the 7 to 5. Then the entire array becomes stable.

2) prices = [3,3,4,4,4,3]
   Output: 4
   Explanation: The longest valid span is [4,4,4,3]. By changing the last 3 to 4,
   the span becomes [4,4,4,4].
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Core idea:
    A subarray can be made entirely equal with at most one correction if and only if
    inside that subarray there is some value that appears at least (length - 1) times.

    Because we only allow one correction, the subarray must look like:
    - all equal already, or
    - all equal except for one position

    A very efficient way to detect the best answer is to process the array by groups of equal values.

    Suppose the array is compressed into runs:
    value, count
    Example: [5,5,7,5,5,5] -> (5,2), (7,1), (5,3)

    Then the best valid answer comes from one of these cases:
    1) A single run by itself:
       count
       This means no correction is needed.

    2) A run extended by one neighboring different element:
       count + 1
       Because we can change that one neighboring element to match the run.

    3) Two runs of the same value separated by exactly one element:
       leftCount + 1 + rightCount
       Example: 5,5,7,5,5,5
       Here the middle single element can be corrected to 5, merging both 5-runs.

    Why this is sufficient:
    If a valid subarray can be made all equal to some value x with at most one correction,
    then all x's inside that subarray form either:
    - one contiguous run, or
    - two runs separated by exactly one non-x element
    There cannot be more than one non-x element, otherwise we would need more than one correction.
    */
    public int LongestStablePriceSpanAfterOneCorrection(int[] prices)
    {
        int n = prices.Length;

        // If the array has only one element, the answer is obviously 1.
        // A single element is already stable.
        if (n == 1)
        {
            return 1;
        }

        // --------------------------------------------------------------------
        // STEP 1: Run-length encode the array.
        //
        // We convert the original array into groups of consecutive equal values.
        // This is useful because the problem is about contiguous equal segments,
        // and run-length encoding lets us reason about those segments directly.
        //
        // Example:
        // prices = [3,3,4,4,4,3]
        // runs:
        // values = [3,4,3]
        // counts = [2,3,1]
        //
        // Each run tells us:
        // - which value appears
        // - how many times it appears consecutively
        // --------------------------------------------------------------------
        List<int> values = new();
        List<int> counts = new();

        int currentValue = prices[0];
        int currentCount = 1;

        for (int i = 1; i < n; i++)
        {
            // If the current array element continues the same run,
            // we simply increase the run length.
            if (prices[i] == currentValue)
            {
                currentCount++;
            }
            else
            {
                // Otherwise, the current run ends here.
                // We store it and start a new run.
                values.Add(currentValue);
                counts.Add(currentCount);

                currentValue = prices[i];
                currentCount = 1;
            }
        }

        // Do not forget to store the final run after the loop ends.
        values.Add(currentValue);
        counts.Add(currentCount);

        int runCount = values.Count;
        int answer = 1;

        // --------------------------------------------------------------------
        // STEP 2: Consider each run as the main stable block.
        //
        // For every run, there are two immediate possibilities:
        //
        // A) Use the run alone.
        //    Then the stable span length is simply counts[i].
        //
        // B) Extend the run by exactly one neighboring element.
        //    Since we are allowed one correction, we can include one adjacent
        //    different element and change it to the run's value.
        //
        //    So if the run is not the entire array, we can potentially get:
        //    counts[i] + 1
        //
        //    Why "not the entire array"?
        //    Because we need an actual neighboring element to add.
        //
        // Example:
        // [4,4,4,3]
        // The run [4,4,4] has count 3, and we can include the next 3 and change it,
        // giving 4 total.
        // --------------------------------------------------------------------
        for (int i = 0; i < runCount; i++)
        {
            // Case A: use the run as-is.
            answer = Math.Max(answer, counts[i]);

            // Case B: extend by one neighboring element if possible.
            // If this run does not already cover the whole array,
            // then there exists at least one element outside the run that can be attached
            // on either the left or right side as part of a contiguous subarray.
            if (counts[i] < n)
            {
                answer = Math.Max(answer, counts[i] + 1);
            }
        }

        // --------------------------------------------------------------------
        // STEP 3: Check whether two runs of the same value can be merged
        // through exactly one middle element.
        //
        // Pattern we are looking for in the run list:
        // run i, run i+1, run i+2
        //
        // If:
        // - values[i] == values[i+2]
        // - counts[i+1] == 1
        //
        // then the middle run is exactly one wrong element.
        // We can correct that one element and merge the left and right runs.
        //
        // The resulting stable span length becomes:
        // counts[i] + 1 + counts[i+2]
        //
        // Example 1:
        // [5,5,7,5,5,5]
        // runs: (5,2), (7,1), (5,3)
        // merge => 2 + 1 + 3 = 6
        //
        // Example 2:
        // [3,3,4,4,4,3]
        // runs: (3,2), (4,3), (3,1)
        // middle run length is 3, not 1, so we cannot merge the 3-runs.
        //
        // Important note:
        // The merged length can never exceed n because it is a real contiguous subarray
        // already present in the array boundaries.
        // --------------------------------------------------------------------
        for (int i = 0; i + 2 < runCount; i++)
        {
            if (values[i] == values[i + 2] && counts[i + 1] == 1)
            {
                int mergedLength = counts[i] + 1 + counts[i + 2];
                answer = Math.Max(answer, mergedLength);
            }
        }

        return answer;
    }
}

// --------------------------------------------------------------------
// Demo code
// --------------------------------------------------------------------

var solution = new Solution();

int[] prices1 = { 5, 5, 7, 5, 5, 5 };
int result1 = solution.LongestStablePriceSpanAfterOneCorrection(prices1);
Console.WriteLine($"Input: [{string.Join(",", prices1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

int[] prices2 = { 3, 3, 4, 4, 4, 3 };
int result2 = solution.LongestStablePriceSpanAfterOneCorrection(prices2);
Console.WriteLine($"Input: [{string.Join(",", prices2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] prices3 = { 8 };
int result3 = solution.LongestStablePriceSpanAfterOneCorrection(prices3);
Console.WriteLine($"Input: [{string.Join(",", prices3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

int[] prices4 = { 1, 2, 1 };
int result4 = solution.LongestStablePriceSpanAfterOneCorrection(prices4);
Console.WriteLine($"Input: [{string.Join(",", prices4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

int[] prices5 = { 9, 9, 9, 9 };
int result5 = solution.LongestStablePriceSpanAfterOneCorrection(prices5);
Console.WriteLine($"Input: [{string.Join(",", prices5)}]");
Console.WriteLine($"Output: {result5}");
Console.WriteLine("Expected: 4");