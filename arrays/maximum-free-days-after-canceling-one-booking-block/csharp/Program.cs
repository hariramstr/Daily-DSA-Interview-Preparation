/*
Title: Maximum Free Days After Canceling One Booking Block
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer n representing the number of days in a planning horizon, labeled from 1 to n,
and an array booked of length n where booked[i] is either 0 or 1.

- booked[i] == 1 means day i + 1 is already booked
- booked[i] == 0 means the day is free

You are allowed to cancel at most one contiguous block of booked days.
That means you may choose indices l and r such that every value in booked[l..r] is 1,
and then change all of them to 0.

You may also choose not to cancel anything.

Return the maximum possible length of a contiguous run of free days after this operation.

Important note:
A cancellation block must be entirely inside one existing run of 1s.
Canceling only part of a booked run is allowed if that gives a better answer.
Your goal is to maximize the longest consecutive sequence of 0s in the final array.

Constraints:
- 1 <= n <= 200000
- booked.length == n
- booked[i] is either 0 or 1

Examples:
1) booked = [0,1,1,0,0,1,0]
   Output: 5
   Explanation:
   Cancel the block [1,1] at indices 1..2.
   The array becomes [0,0,0,0,0,1,0].
   The longest free run is 5.

2) booked = [1,0,1,1,1,0,1]
   Output: 5
   Explanation:
   Cancel the middle block of three booked days at indices 2..4.
   The array becomes [1,0,0,0,0,0,1].
   The longest free run is 5.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Beginner-friendly idea:
    -----------------------
    We want the longest consecutive run of 0s after canceling at most one contiguous block of 1s.

    A key observation:
    If we choose to cancel inside a run of 1s, then to maximize the final free interval,
    we should cancel the ENTIRE run of 1s, not just part of it.

    Why?
    ----
    Suppose a run looks like:
        0 0 1 1 1 0 0
    If we cancel only part of the 1s, some 1s remain and still split the free interval.
    If we cancel the whole run, the left free run and right free run become connected through
    all those canceled days, giving the largest possible merged free interval.

    So the problem becomes:
    For every maximal run of 1s, compute:
        length_of_zero_run_on_left + length_of_this_one_run + length_of_zero_run_on_right
    because after canceling that whole 1-run, all of those positions become one continuous 0-run.

    We also must consider the option of canceling nothing, which means the answer could simply be
    the longest existing run of 0s.

    We can scan the array once, identify runs, and compute the best answer.
    */
    public int MaxFreeDaysAfterCancelingOneBlock(int[] booked)
    {
        int n = booked.Length;

        // This will store the best answer found so far.
        // We start at 0 because the longest free run cannot be negative.
        int best = 0;

        // This variable tracks the length of the zero-run immediately before the current position/run.
        //
        // Example:
        // For array: [0,0,1,1,0,0,0,1]
        // When we are processing the first 1-run [1,1], prevZeroRun = 2
        // because there are two zeros immediately to its left.
        int prevZeroRun = 0;

        // We use index i to walk through the array from left to right.
        int i = 0;

        while (i < n)
        {
            if (booked[i] == 0)
            {
                // STEP 1: Measure a full contiguous run of zeros.
                //
                // Why do we do this?
                // 1) If we choose not to cancel anything, the answer might simply be the longest zero-run.
                // 2) This zero-run may become the "left zero-run" for the next 1-run we process.
                int zeroStart = i;

                while (i < n && booked[i] == 0)
                {
                    i++;
                }

                int zeroLen = i - zeroStart;

                // Update best with the current existing zero-run.
                // This handles the "cancel nothing" option.
                if (zeroLen > best)
                {
                    best = zeroLen;
                }

                // Save this zero-run as the zero-run immediately before the next run.
                prevZeroRun = zeroLen;
            }
            else
            {
                // STEP 2: Measure a full contiguous run of ones.
                //
                // Why a full run?
                // Because canceling the entire 1-run is always at least as good as canceling only part of it.
                // Canceling the whole run removes every blocker between the left and right zero-runs.
                int oneStart = i;

                while (i < n && booked[i] == 1)
                {
                    i++;
                }

                int oneLen = i - oneStart;

                // STEP 3: Measure the zero-run immediately to the right of this 1-run.
                //
                // Why do we need it now?
                // If we cancel this 1-run, the final merged free interval becomes:
                //   left zeros + canceled ones + right zeros
                //
                // We do not permanently advance past this zero-run here in a separate branch;
                // instead, we measure it now and then move i across it as part of this step.
                int rightZeroRun = 0;
                int j = i;

                while (j < n && booked[j] == 0)
                {
                    rightZeroRun++;
                    j++;
                }

                // STEP 4: Compute the free interval created by canceling this entire 1-run.
                //
                // After cancellation:
                // - the left zero-run remains zero
                // - the entire one-run becomes zero
                // - the right zero-run remains zero
                //
                // Since these three parts are adjacent, they form one continuous free interval.
                int mergedFreeRun = prevZeroRun + oneLen + rightZeroRun;

                if (mergedFreeRun > best)
                {
                    best = mergedFreeRun;
                }

                // STEP 5: Move i to the end of the right zero-run we just measured.
                //
                // Why?
                // Because we already counted that zero-run.
                // On the next loop iteration, the next unprocessed part begins at j.
                i = j;

                // The right zero-run we just measured becomes the "previous zero-run"
                // for the next 1-run we may encounter.
                prevZeroRun = rightZeroRun;

                // Also update best with rightZeroRun itself, because canceling nothing
                // might still be optimal in some cases.
                if (rightZeroRun > best)
                {
                    best = rightZeroRun;
                }
            }
        }

        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] booked1 = { 0, 1, 1, 0, 0, 1, 0 };
int result1 = solution.MaxFreeDaysAfterCancelingOneBlock(booked1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] booked2 = { 1, 0, 1, 1, 1, 0, 1 };
int result2 = solution.MaxFreeDaysAfterCancelingOneBlock(booked2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 5

// Additional demos
int[] booked3 = { 0, 0, 0, 0 };
Console.WriteLine("All free days: " + solution.MaxFreeDaysAfterCancelingOneBlock(booked3)); // Expected: 4

int[] booked4 = { 1, 1, 1, 1 };
Console.WriteLine("All booked days: " + solution.MaxFreeDaysAfterCancelingOneBlock(booked4)); // Expected: 4

int[] booked5 = { 0, 1, 0, 1, 0 };
Console.WriteLine("Alternating pattern: " + solution.MaxFreeDaysAfterCancelingOneBlock(booked5)); // Expected: 3

int[] booked6 = { 1 };
Console.WriteLine("Single booked day: " + solution.MaxFreeDaysAfterCancelingOneBlock(booked6)); // Expected: 1

int[] booked7 = { 0 };
Console.WriteLine("Single free day: " + solution.MaxFreeDaysAfterCancelingOneBlock(booked7)); // Expected: 1