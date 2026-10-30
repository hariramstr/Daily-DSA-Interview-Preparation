/*
Title: Minimum Signal Delay to Sync Stage Lights

Problem Description:
A concert venue controls n stage lights placed along a straight truss. The i-th light is mounted at position positions[i]
and can only start its animation at one of the times listed in times[i], where times[i] is sorted in non-decreasing order.
To create a synchronized visual effect, the lighting engineer wants to choose exactly one start time for each light so that
the difference between the earliest chosen time and the latest chosen time is as small as possible.

Return the minimum possible synchronization delay, defined as max(chosenTimes) - min(chosenTimes).

You are given:
- an integer array positions of length n (positions are not directly used in the calculation, but identify the lights and
  guarantee corresponding time lists belong to different devices), and
- a 2D integer array times where times[i] contains the allowed start times for light i.

A solution always exists because each times[i] contains at least one value.

This problem is well suited for binary search on the answer. For a candidate delay D, determine whether there exists a common
time window of length D that contains at least one allowed start time from every light. If yes, try a smaller delay;
otherwise, try a larger one.

Constraints:
- 1 <= n <= 100000
- positions.length == n
- times.length == n
- 1 <= times[i].length <= 100000
- Sum of all times[i].length over all lights <= 200000
- 0 <= positions[i] <= 10^9
- 0 <= times[i][j] <= 10^9
- Each times[i] is sorted in non-decreasing order

Example 1:
Input: positions = [2, 5, 9], times = [[1,4,8],[2,6],[5,7,10]]
Output: 2

Example 2:
Input: positions = [0, 3, 8, 12], times = [[3],[1,4,9],[5,6],[2,8,11]]
Output: 3
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    Let M be the total number of allowed times across all lights.
    - Binary search over the answer range: O(log V), where V is at most 1e9
    - Feasibility check for one candidate delay D:
      We convert each light's allowed times into one or more merged intervals [time - D, time].
      Then we test whether all lights' interval unions have a common intersection.
      Across one light, processing is linear in the size of that light's list.
      Across all lights, one feasibility check is O(M).
    Total: O(M * log V)

    Space Complexity:
    - We store merged intervals for one light at a time.
    - In the worst case, that can be O(k) for one light, and across all lights the largest temporary storage
      is bounded by O(M) in the worst case.
    */
    public int MinimumSynchronizationDelay(int[] positions, int[][] times)
    {
        // The positions array is intentionally unused in the actual calculation.
        // It only identifies which time list belongs to which light.
        // We keep the parameter because it is part of the required function signature.

        // We binary search the smallest delay D such that there exists some window [L, L + D]
        // containing at least one allowed time from every light.
        //
        // Another equivalent way to think about it:
        // For a fixed D, each allowed time t from a light says:
        // "If the common window starts at L, then this time t fits if L <= t <= L + D",
        // which rearranges to:
        // "L must lie in [t - D, t]".
        //
        // So for each light, every allowed time contributes an interval of valid window starts.
        // If we take the union of those intervals for each light, then we need a single L that belongs
        // to every light's union. That means the intersections of all those unions must be non-empty.

        int low = 0;
        int high = 1_000_000_000;

        while (low < high)
        {
            int mid = low + (high - low) / 2;

            // If delay "mid" is feasible, we try to do even better with a smaller delay.
            if (CanSynchronizeWithinDelay(times, mid))
            {
                high = mid;
            }
            else
            {
                // Otherwise we need a larger delay.
                low = mid + 1;
            }
        }

        return low;
    }

    private bool CanSynchronizeWithinDelay(int[][] times, int delay)
    {
        // This method answers:
        // "Does there exist some window start L such that every light has at least one allowed time
        // inside [L, L + delay]?"
        //
        // As explained above, for one light and one allowed time t, valid L values are [t - delay, t].
        // For one light with many allowed times, valid L values are the union of all such intervals.
        //
        // We process lights one by one and maintain the current set of possible L values that still work
        // for all lights seen so far.
        //
        // Initially, before processing any light, any L is possible.
        // Since times are in [0, 1e9] and delay is also in that range, t - delay can be as low as -1e9.
        // We use long to be extra safe and to make interval arithmetic robust.
        List<(long Start, long End)> currentIntersection = new List<(long Start, long End)>
        {
            (long.MinValue / 4, long.MaxValue / 4)
        };

        foreach (int[] lightTimes in times)
        {
            // Step 1:
            // Build the union of intervals [t - delay, t] for this one light.
            //
            // Why do we merge?
            // If consecutive intervals overlap or touch, they represent one continuous range of valid L values.
            // Merging keeps the representation compact and makes later intersection efficient.
            List<(long Start, long End)> mergedForThisLight = BuildMergedIntervalsForOneLight(lightTimes, delay);

            // Step 2:
            // Intersect the current global possible L values with this light's possible L values.
            //
            // Why is this necessary?
            // A valid window start L must satisfy the requirements of every light simultaneously.
            // So after processing this light, the remaining valid L values are exactly the intersection.
            currentIntersection = IntersectIntervalLists(currentIntersection, mergedForThisLight);

            // Step 3:
            // If the intersection becomes empty, then there is no possible common window start L.
            // That means this delay is not feasible.
            if (currentIntersection.Count == 0)
            {
                return false;
            }
        }

        // If after processing all lights we still have at least one valid L,
        // then there exists a common window [L, L + delay] that contains one allowed time from every light.
        return true;
    }

    private List<(long Start, long End)> BuildMergedIntervalsForOneLight(int[] lightTimes, int delay)
    {
        // For each allowed time t, valid window starts L are in [t - delay, t].
        // Because lightTimes is already sorted, the generated intervals are also sorted by end,
        // and their starts are non-decreasing as well.
        //
        // That means we can merge them in one left-to-right pass.

        List<(long Start, long End)> merged = new List<(long Start, long End)>();

        foreach (int t in lightTimes)
        {
            long start = (long)t - delay;
            long end = t;

            if (merged.Count == 0)
            {
                // First interval always starts the merged list.
                merged.Add((start, end));
            }
            else
            {
                var last = merged[merged.Count - 1];

                // If the new interval overlaps or touches the previous merged interval,
                // they form one larger continuous interval.
                //
                // Touching is included because [a, b] and [b, c] still represent a continuous set
                // with no gap at point b.
                if (start <= last.End)
                {
                    merged[merged.Count - 1] = (last.Start, Math.Max(last.End, end));
                }
                else
                {
                    // Otherwise there is a real gap, so this starts a new merged interval.
                    merged.Add((start, end));
                }
            }
        }

        return merged;
    }

    private List<(long Start, long End)> IntersectIntervalLists(
        List<(long Start, long End)> a,
        List<(long Start, long End)> b)
    {
        // Both interval lists are sorted and internally disjoint after merging.
        // We use the classic two-pointer technique to compute their intersection efficiently.
        //
        // Why two pointers?
        // Because each list is sorted, once we know which interval ends earlier,
        // that interval cannot intersect any future interval from the other list beyond its end.
        // So we can safely advance that pointer.
        List<(long Start, long End)> result = new List<(long Start, long End)>();

        int i = 0;
        int j = 0;

        while (i < a.Count && j < b.Count)
        {
            long start = Math.Max(a[i].Start, b[j].Start);
            long end = Math.Min(a[i].End, b[j].End);

            // If start <= end, the two intervals overlap and contribute an intersection interval.
            if (start <= end)
            {
                result.Add((start, end));
            }

            // Move past the interval that ends first, because it cannot contribute to any further overlap.
            if (a[i].End < b[j].End)
            {
                i++;
            }
            else
            {
                j++;
            }
        }

        return result;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] positions1 = { 2, 5, 9 };
int[][] times1 =
{
    new[] { 1, 4, 8 },
    new[] { 2, 6 },
    new[] { 5, 7, 10 }
};
int result1 = solution.MinimumSynchronizationDelay(positions1, times1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] positions2 = { 0, 3, 8, 12 };
int[][] times2 =
{
    new[] { 3 },
    new[] { 1, 4, 9 },
    new[] { 5, 6 },
    new[] { 2, 8, 11 }
};
int result2 = solution.MinimumSynchronizationDelay(positions2, times2);
Console.WriteLine(result2); // Expected: 3