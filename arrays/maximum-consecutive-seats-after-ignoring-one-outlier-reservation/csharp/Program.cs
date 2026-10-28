/*
Title: Maximum Consecutive Seats After Ignoring One Outlier Reservation

Problem Description:
A theater stores its reserved seat numbers for a show in an integer array `seats`.
The numbers are not sorted, may contain duplicates due to repeated sync events,
and may include gaps because many seats were never reserved.

You are allowed to ignore at most one distinct seat number as a data outlier.
Ignoring a seat number means removing all occurrences of that exact value from consideration.

After optionally ignoring one value, determine the maximum length of a consecutive block
of reserved seat numbers that can remain.

A consecutive block is a set of integers like [12, 13, 14, 15], where every number
from the minimum to the maximum is present at least once. Duplicates do not increase
the block length.

Important observation:
Removing one distinct value can never create a new consecutive connection across a gap,
because deletion only removes values; it never adds missing values.
So the answer is simply the length of the longest consecutive run among the DISTINCT values.

Examples:
1) seats = [10, 11, 12, 14, 14, 15, 16]
   Distinct sorted values = [10, 11, 12, 14, 15, 16]
   Consecutive runs are [10,11,12] and [14,15,16], both length 3
   Answer = 3

2) seats = [3, 4, 5, 7, 8, 9, 20]
   Distinct sorted values = [3,4,5,7,8,9,20]
   Consecutive runs are [3,4,5] and [7,8,9], both length 3
   Answer = 3
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the HashSet of distinct seat numbers takes O(n) average time, where n is seats.Length.
    - Scanning the distinct values to find sequence starts also takes O(u) average time,
      where u is the number of distinct values.
    - Each distinct value is effectively visited a constant number of times across all sequence expansions.
    Overall: O(n) average time.

    Space Complexity:
    - We store the distinct values in a HashSet, which uses O(u) extra space.
    Overall: O(u) extra space.
    */
    public int MaximumConsecutiveSeatsAfterIgnoringOneOutlier(int[] seats)
    {
        // Step 1:
        // We place every seat number into a HashSet.
        //
        // What this does:
        // - Removes duplicates automatically.
        // - Gives us very fast average O(1) lookup for "does value X exist?"
        //
        // Why this is necessary:
        // - The problem says duplicates do not increase the block length.
        // - We only care whether a seat number exists at least once.
        // - Fast lookup is extremely useful for checking consecutive neighbors like x-1 and x+1.
        var distinct = new HashSet<int>();

        foreach (int seat in seats)
        {
            distinct.Add(seat);
        }

        // Step 2:
        // If for some reason the set is empty (not possible under the given constraints,
        // because seats.Length >= 1), we would return 0.
        //
        // This is defensive programming and keeps the method robust.
        if (distinct.Count == 0)
        {
            return 0;
        }

        // Step 3:
        // We will compute the longest consecutive run among the distinct values.
        //
        // Why this is the correct goal:
        // - Ignoring one distinct value means deleting it.
        // - Deleting a value cannot fill in a missing number.
        // - Therefore, deletion cannot merge two runs separated by a gap.
        // - The best possible answer is simply the longest run that already exists.
        int best = 0;

        // Step 4:
        // We iterate through each distinct value.
        //
        // Key idea:
        // A number is the START of a consecutive run if (number - 1) is not present.
        //
        // Why this matters:
        // - If we only start counting from true sequence starts, each run is counted once.
        // - This avoids repeatedly recounting the same run from every element inside it.
        foreach (int value in distinct)
        {
            // Check whether this value is the beginning of a run.
            //
            // Example:
            // If value = 14 and 13 is not in the set, then 14 starts a run.
            // If 13 is in the set, then 14 is inside a run, not the start.
            if (!distinct.Contains(value - 1))
            {
                // Step 5:
                // Expand forward while consecutive values exist.
                //
                // We begin with the current start value and keep checking:
                // value, value+1, value+2, ...
                //
                // This counts the full length of the current consecutive block.
                int current = value;
                int length = 1;

                while (distinct.Contains(current + 1))
                {
                    current++;
                    length++;
                }

                // Step 6:
                // Update the best answer if this run is longer than any run seen before.
                if (length > best)
                {
                    best = length;
                }
            }
        }

        // Step 7:
        // Return the longest consecutive run length.
        //
        // This is the final answer because:
        // - Removing one distinct value cannot create a longer consecutive run.
        // - It can only keep the same run length or reduce some run.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement.
// Distinct values: [10, 11, 12, 14, 15, 16]
// Longest consecutive run length = 3
int[] seats1 = { 10, 11, 12, 14, 14, 15, 16 };
int result1 = solution.MaximumConsecutiveSeatsAfterIgnoringOneOutlier(seats1);
Console.WriteLine($"Example 1 result: {result1}");

// Example 2 from the problem statement.
// Distinct values: [3, 4, 5, 7, 8, 9, 20]
// Longest consecutive run length = 3
int[] seats2 = { 3, 4, 5, 7, 8, 9, 20 };
int result2 = solution.MaximumConsecutiveSeatsAfterIgnoringOneOutlier(seats2);
Console.WriteLine($"Example 2 result: {result2}");

// Additional demo: duplicates only, still one distinct value, answer = 1
int[] seats3 = { 8, 8, 8, 8 };
int result3 = solution.MaximumConsecutiveSeatsAfterIgnoringOneOutlier(seats3);
Console.WriteLine($"Additional demo 1 result: {result3}");

// Additional demo: one long run with duplicates
int[] seats4 = { 4, 5, 6, 6, 7, 8, 8, 9 };
int result4 = solution.MaximumConsecutiveSeatsAfterIgnoringOneOutlier(seats4);
Console.WriteLine($"Additional demo 2 result: {result4}");

// Additional demo: separated runs
int[] seats5 = { 100, 4, 200, 1, 3, 2 };
int result5 = solution.MaximumConsecutiveSeatsAfterIgnoringOneOutlier(seats5);
Console.WriteLine($"Additional demo 3 result: {result5}");