/*
Title: Maximum Consecutive Days After Moving One Holiday

Problem Description:
A company tracks employee availability over a planning horizon of n days, numbered from 1 to n.
Some days are already marked as holidays. You are given a strictly increasing integer array holidays,
where each value represents a holiday day.

Employees want the longest possible uninterrupted block of working days, where a working day is any
day that is not a holiday.

You are allowed to move at most one existing holiday to another day that is currently a working day.
After the move, the total number of holidays must remain the same, and no two holidays may occupy the
same day.

Your task is to return the maximum possible length of a consecutive block of working days after
performing at most one such move.

Moving a holiday means:
- choosing one day from holidays,
- removing it from its current position,
- placing it on a different day between 1 and n that is not already a holiday.

You may also choose not to move any holiday.

Key idea:
Instead of thinking about every day from 1 to n (which is impossible when n is very large),
we only think about the gaps of working days between holidays.

If we remove one holiday at position holidays[i], then:
- the working block on its left,
- the holiday day itself,
- and the working block on its right
all become one merged working block.

But because we must place that holiday somewhere else, we have two possibilities:
1. We can place it OUTSIDE that merged block, if there exists at least one working day elsewhere.
   Then the merged block stays fully intact.
2. Otherwise, we are forced to place it INSIDE that merged block, which breaks it by one day.
   Then the best block from that merge is one less.

This leads to a clean gap-based solution.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(m), where m = holidays.Length
    Space Complexity: O(m)

    Explanation of complexity:
    - We build an array of working-day gap lengths around the holidays in one pass.
    - We build prefix and suffix arrays in linear time.
    - We evaluate each holiday as the one to move in linear time.
    - We never iterate from 1 to n, so this works even when n is as large as 1e9.
    */
    public int MaxConsecutiveWorkingDaysAfterMovingOneHoliday(int n, int[] holidays)
    {
        int m = holidays.Length;

        // ---------------------------------------------------------------------
        // STEP 1: Build the array of working-day gaps.
        //
        // There are m holidays, so there are exactly m + 1 working-day segments:
        //   gap[0]   = days before the first holiday
        //   gap[1]   = days between holiday 0 and holiday 1
        //   ...
        //   gap[m-1] = days between holiday m-2 and holiday m-1
        //   gap[m]   = days after the last holiday
        //
        // Example: n = 10, holidays = [3, 8]
        //   gap[0] = days 1..2 => 2
        //   gap[1] = days 4..7 => 4
        //   gap[2] = days 9..10 => 2
        //
        // Why this is useful:
        // A consecutive working block is exactly one of these gaps.
        // If we remove one holiday, we merge the gap on its left and the gap on its right,
        // plus the holiday day itself, into one larger block.
        // ---------------------------------------------------------------------
        int[] gaps = new int[m + 1];

        gaps[0] = holidays[0] - 1;

        for (int i = 1; i < m; i++)
        {
            gaps[i] = holidays[i] - holidays[i - 1] - 1;
        }

        gaps[m] = n - holidays[m - 1];

        // ---------------------------------------------------------------------
        // STEP 2: Compute the best answer without moving any holiday.
        //
        // Since moving is optional, we must consider the current maximum working block.
        // ---------------------------------------------------------------------
        int answer = 0;
        for (int i = 0; i <= m; i++)
        {
            if (gaps[i] > answer)
            {
                answer = gaps[i];
            }
        }

        // ---------------------------------------------------------------------
        // STEP 3: Build prefix and suffix maximum arrays over the gaps.
        //
        // prefixMax[i] = maximum gap value among gaps[0..i]
        // suffixMax[i] = maximum gap value among gaps[i..m]
        //
        // Why do we need these?
        // When considering moving holiday i, we want to know whether there exists
        // ANY working day outside the two adjacent gaps:
        //   left gap  = gaps[i]
        //   right gap = gaps[i + 1]
        //
        // If some other gap has length > 0, then there exists at least one working day
        // elsewhere where we can place the moved holiday without damaging our newly merged block.
        //
        // Prefix/suffix maxima let us answer "what is the largest gap outside these two?"
        // in O(1) time per holiday.
        // ---------------------------------------------------------------------
        int[] prefixMax = new int[m + 1];
        int[] suffixMax = new int[m + 1];

        prefixMax[0] = gaps[0];
        for (int i = 1; i <= m; i++)
        {
            prefixMax[i] = Math.Max(prefixMax[i - 1], gaps[i]);
        }

        suffixMax[m] = gaps[m];
        for (int i = m - 1; i >= 0; i--)
        {
            suffixMax[i] = Math.Max(suffixMax[i + 1], gaps[i]);
        }

        // ---------------------------------------------------------------------
        // STEP 4: Try removing each holiday once.
        //
        // Suppose we remove holidays[i].
        //
        // Let:
        //   left  = gaps[i]
        //   right = gaps[i + 1]
        //
        // Then removing this holiday creates one merged working block of size:
        //   merged = left + 1 + right
        //
        // Why +1?
        // Because the holiday day itself becomes a working day after removal.
        //
        // But we must place the holiday somewhere else.
        //
        // Case A: There exists at least one working day outside this merged block.
        //         Then we can place the holiday there, and keep the merged block intact.
        //         Candidate answer = merged
        //
        // Case B: There is no working day outside this merged block.
        //         Then we are forced to place the holiday inside the merged block.
        //         That breaks the merged block by one day.
        //         Candidate answer = merged - 1
        //
        // We take the maximum over all holidays.
        // ---------------------------------------------------------------------
        for (int i = 0; i < m; i++)
        {
            int left = gaps[i];
            int right = gaps[i + 1];
            int merged = left + 1 + right;

            // -------------------------------------------------------------
            // Find the largest gap that is NOT one of the two adjacent gaps
            // involved in this merge.
            //
            // Excluded gaps:
            //   gaps[i] and gaps[i + 1]
            //
            // Remaining gaps are:
            //   gaps[0..i-1] and gaps[i+2..m]
            //
            // We use prefix/suffix arrays to get the maximum on those ranges.
            // -------------------------------------------------------------
            int maxOtherGap = 0;

            if (i - 1 >= 0)
            {
                maxOtherGap = Math.Max(maxOtherGap, prefixMax[i - 1]);
            }

            if (i + 2 <= m)
            {
                maxOtherGap = Math.Max(maxOtherGap, suffixMax[i + 2]);
            }

            // -------------------------------------------------------------
            // If maxOtherGap > 0, then there exists at least one working day
            // outside the merged block, so we can place the moved holiday there.
            //
            // Otherwise, every working day in the entire schedule belongs to the
            // merged block itself, so we must place the holiday back inside it.
            // That reduces the best possible consecutive block by 1.
            // -------------------------------------------------------------
            int candidate;
            if (maxOtherGap > 0)
            {
                candidate = merged;
            }
            else
            {
                candidate = merged - 1;
            }

            if (candidate > answer)
            {
                answer = candidate;
            }
        }

        return answer;
    }
}

// -----------------------------------------------------------------------------
// Demo code
// -----------------------------------------------------------------------------

var solution = new Solution();

// Example 1
int n1 = 10;
int[] holidays1 = { 3, 8 };
int result1 = solution.MaxConsecutiveWorkingDaysAfterMovingOneHoliday(n1, holidays1);
Console.WriteLine($"Example 1: n = {n1}, holidays = [{string.Join(", ", holidays1)}], result = {result1}");

// Example 2
int n2 = 15;
int[] holidays2 = { 4, 8, 12 };
int result2 = solution.MaxConsecutiveWorkingDaysAfterMovingOneHoliday(n2, holidays2);
Console.WriteLine($"Example 2: n = {n2}, holidays = [{string.Join(", ", holidays2)}], result = {result2}");

// Additional quick checks
int n3 = 5;
int[] holidays3 = { 3 };
int result3 = solution.MaxConsecutiveWorkingDaysAfterMovingOneHoliday(n3, holidays3);
Console.WriteLine($"Extra 1: n = {n3}, holidays = [{string.Join(", ", holidays3)}], result = {result3}");

int n4 = 7;
int[] holidays4 = { 2, 4, 6 };
int result4 = solution.MaxConsecutiveWorkingDaysAfterMovingOneHoliday(n4, holidays4);
Console.WriteLine($"Extra 2: n = {n4}, holidays = [{string.Join(", ", holidays4)}], result = {result4}");