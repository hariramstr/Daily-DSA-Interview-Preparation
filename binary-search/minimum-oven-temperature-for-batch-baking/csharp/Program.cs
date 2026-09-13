/*
Title: Minimum Oven Temperature for Batch Baking

Problem Description:
A bakery needs to finish several trays of pastries before the shop opens. You are given
an array `batches`, where `batches[i]` is the number of pastries in the `i`th tray,
and an integer `hours` representing the total number of whole hours available.

The bakery uses a programmable oven that can be set to a single integer temperature
level `t` for the entire night.

If the oven is set to temperature `t`, then tray `i` takes `ceil(batches[i] / t)` hours
to finish because higher temperature bakes more pastries per hour. Trays are baked one
after another, not in parallel.

Your task is to return the minimum integer temperature `t` such that all trays can be
completed within `hours` hours.

It is guaranteed that some temperature can finish the work within the given time.

Key Insight:
This is a classic "binary search on the answer" problem.

Why binary search works:
- If a temperature `t` is fast enough to finish all trays within `hours`,
  then any temperature larger than `t` will also be fast enough.
- If a temperature `t` is too slow, then any smaller temperature will also be too slow.

That means the answer space is monotonic:
too slow ... too slow ... valid ... valid ... valid

So we can binary search for the first valid temperature.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log m)
      where:
      n = number of trays (batches.Length)
      m = maximum pastries in any tray
    Explanation:
    - Each binary search step checks all trays once, which is O(n).
    - The search range is from 1 to max(batches), so binary search takes O(log m) steps.

    Space Complexity:
    - O(1) extra space
    Explanation:
    - We only use a few variables regardless of input size.
    */
    public int MinOvenTemperature(int[] batches, int hours)
    {
        // Step 1:
        // We need to determine the search range for the answer.
        //
        // The minimum possible temperature is 1:
        // - If temperature were lower than 1, it would not make sense because the problem
        //   says temperature is an integer and must be at least 1.
        //
        // The maximum possible temperature is max(batches):
        // - At that temperature, even the largest tray finishes in exactly 1 hour.
        // - No answer can ever need to be larger than the largest tray size.
        int left = 1;
        int right = 0;

        // Step 2:
        // Find the largest tray size so we know the upper bound of our binary search.
        //
        // We scan through the array once and keep the maximum value seen so far.
        // This is necessary because the problem allows large values, and we want the
        // tightest valid upper bound for efficient binary search.
        foreach (int batch in batches)
        {
            if (batch > right)
            {
                right = batch;
            }
        }

        // Step 3:
        // Perform binary search on the answer space [left, right].
        //
        // Our goal is to find the smallest temperature that is sufficient.
        // So whenever we find a valid temperature, we do NOT stop immediately.
        // Instead, we continue searching on the left half to see if an even smaller
        // valid temperature exists.
        while (left < right)
        {
            // Step 3a:
            // Compute the middle temperature safely.
            //
            // We use:
            //   left + (right - left) / 2
            // instead of:
            //   (left + right) / 2
            // to avoid integer overflow in general binary search patterns.
            int mid = left + (right - left) / 2;

            // Step 3b:
            // Check how many total hours are needed if the oven temperature is `mid`.
            //
            // We use a long variable because:
            // - There can be up to 100,000 trays.
            // - Each tray can contribute many hours.
            // - The total could exceed the range of int during accumulation.
            long neededHours = 0;

            // Step 3c:
            // For each tray, compute:
            //   ceil(batch / mid)
            //
            // Instead of using floating-point math, we use the integer formula:
            //   (batch + mid - 1) / mid
            //
            // Why this formula works:
            // - Integer division normally rounds down.
            // - Adding (mid - 1) before dividing effectively rounds up.
            //
            // Example:
            //   ceil(12 / 5) = 3
            //   (12 + 5 - 1) / 5 = 16 / 5 = 3
            //
            // This is faster and avoids precision issues.
            foreach (int batch in batches)
            {
                neededHours += (batch + mid - 1L) / mid;

                // Optional optimization:
                // If we already exceeded the allowed hours, we can stop early.
                // There is no need to continue because this temperature is already too slow.
                if (neededHours > hours)
                {
                    break;
                }
            }

            // Step 3d:
            // Decide which half of the search space to keep.
            //
            // Case 1: neededHours <= hours
            // - This temperature is sufficient.
            // - But we want the MINIMUM sufficient temperature.
            // - So we keep searching to the left, including mid itself.
            //
            // Case 2: neededHours > hours
            // - This temperature is too slow.
            // - Any smaller temperature will also be too slow.
            // - So we must search to the right of mid.
            if (neededHours <= hours)
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // Step 4:
        // When the loop ends, left == right.
        // That value is the smallest valid temperature.
        return left;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1:
// batches = [12, 7, 18, 5], hours = 10
// Expected output: 5
//
// Quick verification:
// t = 5
// ceil(12/5) + ceil(7/5) + ceil(18/5) + ceil(5/5)
// = 3 + 2 + 4 + 1
// = 10
//
// t = 4 would give:
// 3 + 2 + 5 + 2 = 12, which is too much
int[] batches1 = { 12, 7, 18, 5 };
int hours1 = 10;
int result1 = solution.MinOvenTemperature(batches1, hours1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// batches = [30, 11, 23, 4, 20], hours = 6
// Expected output: 23
//
// Quick verification:
// t = 23
// ceil(30/23) + ceil(11/23) + ceil(23/23) + ceil(4/23) + ceil(20/23)
// = 2 + 1 + 1 + 1 + 1
// = 6
//
// t = 22
// ceil(30/22) + ceil(11/22) + ceil(23/22) + ceil(4/22) + ceil(20/22)
// = 2 + 1 + 2 + 1 + 1
// = 7, too much
int[] batches2 = { 30, 11, 23, 4, 20 };
int hours2 = 6;
int result2 = solution.MinOvenTemperature(batches2, hours2);
Console.WriteLine($"Example 2 Result: {result2}");