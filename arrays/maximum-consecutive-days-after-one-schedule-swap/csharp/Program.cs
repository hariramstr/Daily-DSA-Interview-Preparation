/*
Title: Maximum Consecutive Days After One Schedule Swap

Problem Description:
You are given a binary array days where days[i] is 1 if an employee is scheduled to work on day i, and 0 if they are off.
The company allows at most one swap operation: you may choose one index containing 1 and one index containing 0, and swap their values.
This keeps the total number of working days the same, but may move a workday to a more useful position.

Your task is to return the maximum possible length of a consecutive block of 1s after performing at most one such swap.
You may also choose not to swap if the array is already optimal.

A swap can happen between any two positions in the array, not necessarily adjacent.
Note that if there is no 0 in the array, the answer is simply the full length of the array.
If there is no 1 in the array, the answer is 0.

This problem tests whether you can reason about contiguous segments while respecting the global limit on how many 1s exist in the entire array.
An optimal solution should run in linear time.

Examples:
1) days = [1,1,0,1,1,1,0,1]
   Output: 6
   Explanation:
   Swap the last 1 with the 0 at index 2.
   The array can become [1,1,1,1,1,1,0,0], which contains a consecutive block of 6 working days.

2) days = [1,0,1,0,1]
   Output: 3
   Explanation:
   Swap one of the outer 1s with the middle 0 at index 1 or 3.
   The best possible consecutive block of 1s is 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Idea:
    We want the longest block of 1s after at most one swap of a 1 and a 0.

    A very useful observation:
    - After one swap, the best block we can create is equivalent to choosing a window that contains
      at most one 0, because that one 0 could be replaced by a 1 from somewhere else.
    - However, we must also respect the total number of 1s in the whole array.
      We cannot create more 1s than already exist globally.

    So we use a sliding window that always contains at most one 0.
    For each such window:
    - If the window length is L and it contains at most one 0,
      then the best consecutive 1s we can make from it is:
         min(L, totalOnes)
      because even if the window is large, we cannot exceed the total number of 1s available.

    This handles all cases:
    - all 1s -> answer is n
    - all 0s -> answer is 0
    - mixed values -> best window with at most one 0, capped by total number of 1s
    */
    public int MaxConsecutiveDaysAfterOneSwap(int[] days)
    {
        // Step 1:
        // Count how many total 1s exist in the entire array.
        //
        // Why this is necessary:
        // A swap does not create new 1s. It only moves one existing 1 to a different place.
        // Therefore, no final consecutive block can ever be longer than the total number of 1s.
        int totalOnes = 0;
        foreach (int day in days)
        {
            if (day == 1)
            {
                totalOnes++;
            }
        }

        // If there are no working days at all, then no consecutive block of 1s can exist.
        if (totalOnes == 0)
        {
            return 0;
        }

        // If every day is already a working day, then the whole array is one consecutive block.
        if (totalOnes == days.Length)
        {
            return days.Length;
        }

        // Step 2:
        // Use the sliding window technique.
        //
        // Window meaning:
        // We maintain a subarray [left..right] that contains at most one 0.
        //
        // Why at most one 0?
        // Because with one swap, we can only "fix" one zero inside a candidate block by bringing in one 1
        // from somewhere else. If a window had two or more zeros, one swap would not be enough to turn
        // the whole window into all 1s.
        int left = 0;
        int zeroCount = 0;
        int best = 0;

        // Expand the window one element at a time using 'right'.
        for (int right = 0; right < days.Length; right++)
        {
            // If the new element is 0, record that the current window now contains one more zero.
            if (days[right] == 0)
            {
                zeroCount++;
            }

            // Step 3:
            // If the window now contains more than one zero, it is no longer valid.
            // We must shrink it from the left until it again contains at most one zero.
            //
            // Why this is necessary:
            // Our invariant is that the window must always be a region that could potentially become
            // all 1s after at most one swap.
            while (zeroCount > 1)
            {
                // If the element leaving the window is 0, then the zero count decreases.
                if (days[left] == 0)
                {
                    zeroCount--;
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // Step 4:
            // Now [left..right] is a valid window containing at most one zero.
            // Its length is:
            int windowLength = right - left + 1;

            // This window could become a consecutive block of 1s after at most one swap.
            // But we must cap it by totalOnes, because we cannot have more 1s in the final answer
            // than exist in the entire array.
            int possibleBlock = Math.Min(windowLength, totalOnes);

            // Update the best answer found so far.
            if (possibleBlock > best)
            {
                best = possibleBlock;
            }
        }

        return best;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

int[] days1 = [1, 1, 0, 1, 1, 1, 0, 1];
int result1 = solution.MaxConsecutiveDaysAfterOneSwap(days1);
Console.WriteLine($"Input: [{string.Join(",", days1)}]");
Console.WriteLine($"Maximum consecutive working days after at most one swap: {result1}");
Console.WriteLine();

int[] days2 = [1, 0, 1, 0, 1];
int result2 = solution.MaxConsecutiveDaysAfterOneSwap(days2);
Console.WriteLine($"Input: [{string.Join(",", days2)}]");
Console.WriteLine($"Maximum consecutive working days after at most one swap: {result2}");
Console.WriteLine();

int[] days3 = [1, 1, 1, 1];
int result3 = solution.MaxConsecutiveDaysAfterOneSwap(days3);
Console.WriteLine($"Input: [{string.Join(",", days3)}]");
Console.WriteLine($"Maximum consecutive working days after at most one swap: {result3}");
Console.WriteLine();

int[] days4 = [0, 0, 0, 0];
int result4 = solution.MaxConsecutiveDaysAfterOneSwap(days4);
Console.WriteLine($"Input: [{string.Join(",", days4)}]");
Console.WriteLine($"Maximum consecutive working days after at most one swap: {result4}");
Console.WriteLine();

int[] days5 = [1, 0, 0, 1, 1, 0, 1];
int result5 = solution.MaxConsecutiveDaysAfterOneSwap(days5);
Console.WriteLine($"Input: [{string.Join(",", days5)}]");
Console.WriteLine($"Maximum consecutive working days after at most one swap: {result5}");