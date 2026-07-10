/*
Title: Minimum Daily Upload Limit for Ordered Media Sync

Problem Description:
A media company needs to synchronize a sequence of video segments to a remote archive.
The segments must be uploaded in the given order, and each segment is indivisible:
it must be uploaded entirely on a single day. The company has exactly d days to finish
the synchronization.

For each segment i, uploading it consumes uploadSizes[i] units of bandwidth on the day
it is assigned. If the daily upload limit is L, then the total size of all segments
assigned to any single day cannot exceed L. Because the upload order cannot change,
each day receives a contiguous block of segments.

Your task is to find the minimum possible daily upload limit L that allows all segments
to be uploaded within at most d days.

This is a classic "binary search on answer" problem:
- If a daily limit L is feasible, then any larger limit is also feasible.
- If a daily limit L is not feasible, then any smaller limit is also not feasible.

That monotonic behavior allows us to binary search the smallest feasible limit.

Examples:
1) uploadSizes = [7,2,5,10,8], d = 2
   Answer = 18
   One valid split: [7,2,5] and [10,8]

2) uploadSizes = [4,4,4,4,4,4,4], d = 3
   Answer = 12
   One valid split: [4,4,4], [4,4,4], [4]
*/

using System;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of segments.
    - Each feasibility check scans the array once: O(n).
    - We binary search over the answer range from max(uploadSizes) to sum(uploadSizes),
      which takes O(log S), where S is the size of that numeric range.
    - Total: O(n log S)

    Space Complexity:
    - O(1) extra space, not counting the input array.
    */
    public long MinimumDailyUploadLimit(int[] uploadSizes, int d)
    {
        // Step 1:
        // Establish the binary search boundaries.
        //
        // Why these boundaries?
        // - The minimum possible daily limit can never be smaller than the largest single segment,
        //   because segments are indivisible. If one segment has size 10, then any valid daily
        //   limit must be at least 10.
        //
        // - The maximum possible daily limit is the sum of all segments, because with that limit
        //   we could upload everything in one day (and using fewer than d days is allowed).
        //
        // We use long because:
        // - uploadSizes[i] can be as large as 1,000,000,000
        // - there can be up to 200,000 segments
        // - the total sum can exceed int range
        long left = 0;
        long right = 0;

        foreach (int size in uploadSizes)
        {
            // The left boundary must be at least the maximum single segment size.
            if (size > left)
            {
                left = size;
            }

            // The right boundary is the total sum of all segment sizes.
            right += size;
        }

        // Step 2:
        // Perform binary search to find the smallest feasible daily limit.
        //
        // Invariant:
        // - Any value smaller than the true answer is infeasible.
        // - The answer lies somewhere in [left, right].
        //
        // We repeatedly test the midpoint:
        // - If midpoint is feasible, try smaller values by moving right down.
        // - If midpoint is not feasible, we must move left up.
        while (left < right)
        {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate daily limit is enough.
            if (CanUploadWithinDays(uploadSizes, d, mid))
            {
                // mid works, so the answer could be mid or something smaller.
                right = mid;
            }
            else
            {
                // mid does not work, so the answer must be larger.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the smallest feasible limit.
        return left;
    }

    private bool CanUploadWithinDays(int[] uploadSizes, int d, long limit)
    {
        // Step 3:
        // Greedily simulate assigning segments to days under the given daily limit.
        //
        // Why greedy works here:
        // - We must preserve order.
        // - For a fixed limit, the best way to minimize the number of days used is to pack
        //   each day as much as possible before starting the next day.
        //
        // If even this most efficient packing uses more than d days, then the limit is infeasible.
        // If it uses d or fewer days, then the limit is feasible.
        int daysUsed = 1;      // We start by using the first day.
        long currentDayLoad = 0;

        foreach (int size in uploadSizes)
        {
            // Safety check:
            // If a single segment is larger than the limit, then this limit is impossible.
            // In practice, our binary search lower bound already prevents this,
            // but keeping this check makes the helper method robust and self-contained.
            if (size > limit)
            {
                return false;
            }

            // Step 3a:
            // Try to place the current segment into the current day.
            //
            // If adding it would exceed the daily limit, we must start a new day.
            if (currentDayLoad + size > limit)
            {
                daysUsed++;
                currentDayLoad = size;

                // Early exit optimization:
                // As soon as we exceed d days, we know this limit is not feasible.
                if (daysUsed > d)
                {
                    return false;
                }
            }
            else
            {
                // Otherwise, safely add the segment to the current day.
                currentDayLoad += size;
            }
        }

        // If we finished processing all segments using at most d days, the limit works.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// uploadSizes = [7,2,5,10,8], d = 2
// Expected output: 18
int[] uploadSizes1 = { 7, 2, 5, 10, 8 };
int d1 = 2;
long result1 = solution.MinimumDailyUploadLimit(uploadSizes1, d1);
Console.WriteLine(result1);

// Example 2:
// uploadSizes = [4,4,4,4,4,4,4], d = 3
// Expected output: 12
int[] uploadSizes2 = { 4, 4, 4, 4, 4, 4, 4 };
int d2 = 3;
long result2 = solution.MinimumDailyUploadLimit(uploadSizes2, d2);
Console.WriteLine(result2);

// Additional quick sanity check:
// If d equals the number of segments, the answer should be the maximum segment size,
// because each segment can be uploaded on its own day.
int[] uploadSizes3 = { 3, 1, 9, 2 };
int d3 = 4;
long result3 = solution.MinimumDailyUploadLimit(uploadSizes3, d3);
Console.WriteLine(result3);