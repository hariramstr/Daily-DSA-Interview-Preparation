/*
Title: Maximum Consecutive Days After Repairing One Missing Report
Difficulty: Medium
Topic: Arrays

Problem Description:
A monitoring system stores the day numbers on which a service successfully uploaded its daily health report.
The array `days` contains distinct positive integers representing those successful upload days, but the values
are not guaranteed to be sorted.

You are allowed to repair at most one missing report by choosing exactly one day number that is not already
in the array and treating it as if the report had been uploaded successfully.

Your task is to return the maximum possible length of a consecutive streak of successful days after performing
at most one repair. A consecutive streak is a set of day numbers with no gaps, such as [10, 11, 12, 13],
whose length is 4.

For example:
- If the recorded days are [3, 5, 6], you may repair day 4 to obtain [3, 4, 5, 6], so the answer is 4.
- If the array already contains a long consecutive block, you may choose not to repair anything.

Constraints:
- 1 <= days.length <= 2 * 10^5
- 1 <= days[i] <= 10^9
- All values in `days` are distinct
*/

using System;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - Scanning the sorted array once takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort a cloned array, that clone uses O(n) extra space
    - Aside from that, only a few variables are used
    - Total: O(n)

    Idea:
    1. Sort the distinct day numbers.
    2. Compress them into consecutive blocks.
       Example: [1,2,4,7,8] becomes blocks:
       - [1,2] length 2
       - [4,4] length 1
       - [7,8] length 2
    3. For each pair of neighboring blocks:
       - If the gap between them is exactly 2, there is exactly one missing day between them.
         We can repair that one day and merge both blocks:
         merged length = leftBlockLength + 1 + rightBlockLength
       - If the gap is larger than 2, one repair can only extend one side by 1:
         candidate = max(leftBlockLength + 1, rightBlockLength + 1)
    4. Also consider extending any single existing block by 1 somewhere at one end.
    5. The maximum among all candidates is the answer.

    Important note:
    Because all values are distinct, sorting lets us reason about consecutive runs very easily.
    */
    public int MaximumConsecutiveDaysAfterOneRepair(int[] days)
    {
        // If there is only one recorded day, we can always repair one neighboring missing day
        // (for example day-1 or day+1, as long as it is not already present, which it is not).
        // So the best streak becomes 2.
        if (days.Length == 1)
        {
            return 2;
        }

        // We clone before sorting so the original input remains unchanged.
        // This is not required for correctness, but it is often a good habit in demo/interview code
        // unless in-place modification is explicitly acceptable.
        int[] sorted = (int[])days.Clone();
        Array.Sort(sorted);

        // `best` stores the best answer found so far.
        // At minimum, if there is at least one day, the existing streak length is at least 1.
        // Since we may repair at most one missing day, in almost all cases we can make it at least 2.
        // We will compute carefully below.
        int best = 1;

        // We will scan the sorted array and build consecutive blocks on the fly.
        //
        // A "block" means a maximal consecutive segment.
        // Example:
        // sorted = [4,5,6,8,10,11]
        // blocks are:
        // - [4,5,6] length 3
        // - [8] length 1
        // - [10,11] length 2
        //
        // We only need information about:
        // - the previous block
        // - the current block
        //
        // because the decision to merge via one repaired day only depends on neighboring blocks.
        int prevStart = sorted[0];
        int prevEnd = sorted[0];
        int prevLen = 1;

        // First, we should consider the possibility of extending the first block by one repaired day.
        // For a single block of length L, if there exists any missing adjacent day, we can make it L+1.
        // Since the values are distinct and finite, every block can always be extended by 1 on at least one side
        // somewhere on the number line.
        best = Math.Max(best, prevLen + 1);

        // We now iterate through the sorted days and detect where blocks end.
        for (int i = 1; i < sorted.Length; i++)
        {
            // If the current day is exactly previous day + 1, then it continues the same consecutive block.
            if (sorted[i] == sorted[i - 1] + 1)
            {
                prevEnd = sorted[i];
                prevLen++;
                continue;
            }

            // If we reach here, the current value starts a NEW block.
            // So the previous block is finalized, and the new block begins at sorted[i].
            int currentStart = sorted[i];
            int currentEnd = sorted[i];
            int currentLen = 1;

            // We now need to expand the current block fully.
            // This inner loop consumes all consecutive values belonging to the same block.
            while (i + 1 < sorted.Length && sorted[i + 1] == sorted[i] + 1)
            {
                i++;
                currentEnd = sorted[i];
                currentLen++;
            }

            // At this point:
            // - previous block = [prevStart .. prevEnd], length prevLen
            // - current block  = [currentStart .. currentEnd], length currentLen
            //
            // The gap size in terms of missing numbers between the two blocks is:
            // currentStart - prevEnd - 1
            int missingBetween = currentStart - prevEnd - 1;

            // Case 1:
            // There is exactly ONE missing day between the two blocks.
            // Example:
            // left  = [4,5,6]
            // right = [8]
            // missingBetween = 1 because day 7 is missing
            //
            // Then repairing that one missing day merges both blocks into one larger consecutive streak:
            // merged length = prevLen + 1 + currentLen
            if (missingBetween == 1)
            {
                best = Math.Max(best, prevLen + 1 + currentLen);
            }
            else
            {
                // Case 2:
                // There are TWO OR MORE missing days between the blocks.
                // One repair cannot bridge the entire gap.
                //
                // But we can still use one repair to extend either block by 1:
                // - extend the left block on its right side
                // - or extend the right block on its left side
                //
                // So the best local candidate here is:
                // max(prevLen + 1, currentLen + 1)
                best = Math.Max(best, prevLen + 1);
                best = Math.Max(best, currentLen + 1);
            }

            // Also, even independent of the gap logic above, each block alone can always be extended by 1.
            // This is already covered in the `else` branch for large gaps and in earlier steps,
            // but keeping this explicit makes the reasoning beginner-friendly and robust.
            best = Math.Max(best, prevLen + 1);
            best = Math.Max(best, currentLen + 1);

            // Move forward:
            // the current block becomes the previous block for the next iteration.
            prevStart = currentStart;
            prevEnd = currentEnd;
            prevLen = currentLen;
        }

        // `best` now contains the maximum streak length obtainable after adding at most one missing day.
        return best;
    }
}

// Demo code:
// We create sample inputs, call the solution, and print the results.

var solution = new Solution();

int[] days1 = { 8, 4, 5, 10, 6 };
int result1 = solution.MaximumConsecutiveDaysAfterOneRepair(days1);
Console.WriteLine($"Input: [{string.Join(", ", days1)}]");
Console.WriteLine($"Maximum consecutive streak after at most one repair: {result1}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

int[] days2 = { 1, 2, 4, 7, 8 };
int result2 = solution.MaximumConsecutiveDaysAfterOneRepair(days2);
Console.WriteLine($"Input: [{string.Join(", ", days2)}]");
Console.WriteLine($"Maximum consecutive streak after at most one repair: {result2}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] days3 = { 3, 5, 6 };
int result3 = solution.MaximumConsecutiveDaysAfterOneRepair(days3);
Console.WriteLine($"Input: [{string.Join(", ", days3)}]");
Console.WriteLine($"Maximum consecutive streak after at most one repair: {result3}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] days4 = { 100 };
int result4 = solution.MaximumConsecutiveDaysAfterOneRepair(days4);
Console.WriteLine($"Input: [{string.Join(", ", days4)}]");
Console.WriteLine($"Maximum consecutive streak after at most one repair: {result4}");
Console.WriteLine("Expected: 2");
Console.WriteLine();

int[] days5 = { 10, 11, 12, 20 };
int result5 = solution.MaximumConsecutiveDaysAfterOneRepair(days5);
Console.WriteLine($"Input: [{string.Join(", ", days5)}]");
Console.WriteLine($"Maximum consecutive streak after at most one repair: {result5}");
Console.WriteLine("Expected: 4");
Console.WriteLine();