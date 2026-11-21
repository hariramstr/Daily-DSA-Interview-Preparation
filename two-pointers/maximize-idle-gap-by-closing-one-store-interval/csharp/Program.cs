/*
Title: Maximize Idle Gap by Closing One Store Interval

Problem Description:
A retail analytics team tracks the minutes during a day when a store is open. The schedule is represented by a sorted list of non-overlapping open intervals, where each interval is inclusive of its start minute and exclusive of its end minute: [start, end). Management is allowed to permanently cancel at most one open interval for maintenance. After removing zero or one interval, they want to know the largest continuous idle gap anywhere between the remaining open intervals, including the gap before the first interval and after the last interval within the business day.

You are given an integer dayLength representing the total number of minutes in the day, and a 2D integer array intervals where intervals[i] = [start_i, end_i]. The intervals are sorted by start_i, non-overlapping, and all satisfy 0 <= start_i < end_i <= dayLength.

Return the maximum possible idle gap after removing at most one interval.

A gap is any continuous range of minutes with no open interval covering it. If you remove an interval in the middle, the two neighboring gaps merge together with the removed interval's length, forming one larger gap.

Constraints:
- 1 <= intervals.length <= 200000
- 1 <= dayLength <= 1000000000
- 0 <= start_i < end_i <= dayLength
- intervals is sorted by start_i
- intervals are pairwise non-overlapping

Examples:
1) dayLength = 20, intervals = [[2,5],[7,9],[12,15]]
   Output: 7

2) dayLength = 30, intervals = [[0,4],[6,10],[13,18],[22,30]]
   Output: 12
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1) extra space

    Idea:
    - First compute the best gap without removing anything.
    - Then consider removing each interval exactly once.
    - If interval i is removed:
        merged idle gap = (start of next remaining interval) - (end of previous remaining interval)
      where:
        previous remaining boundary is 0 if i was the first interval, otherwise intervals[i - 1][1]
        next remaining boundary is dayLength if i was the last interval, otherwise intervals[i + 1][0]
    - Because intervals are sorted and non-overlapping, removing one interval only affects the local area
      around that interval. All other gaps stay unchanged.
    - Therefore, scanning all intervals once is enough.
    */
    public int MaximizeIdleGap(int dayLength, int[][] intervals)
    {
        int n = intervals.Length;

        // This variable stores the best answer found so far.
        // We will compare:
        // 1) the largest existing gap without removing anything
        // 2) the merged gap created by removing each interval
        int answer = 0;

        // ------------------------------------------------------------
        // STEP 1: Compute the largest gap when we remove nothing.
        // ------------------------------------------------------------
        //
        // Why do we need this?
        // Because the problem says we may remove "at most one" interval.
        // That means doing nothing is allowed, so we must include that case.
        //
        // The gaps in the original schedule are:
        // - before the first interval: [0, intervals[0][0])
        // - between every pair of consecutive intervals
        // - after the last interval: [intervals[n - 1][1], dayLength)
        //
        // Since intervals are already sorted and non-overlapping, we can compute
        // each gap directly in one left-to-right scan.
        // ------------------------------------------------------------

        // Gap before the first interval.
        answer = Math.Max(answer, intervals[0][0]);

        // Gaps between consecutive intervals.
        for (int i = 1; i < n; i++)
        {
            int gapBetween = intervals[i][0] - intervals[i - 1][1];
            answer = Math.Max(answer, gapBetween);
        }

        // Gap after the last interval.
        answer = Math.Max(answer, dayLength - intervals[n - 1][1]);

        // ------------------------------------------------------------
        // STEP 2: Try removing each interval once.
        // ------------------------------------------------------------
        //
        // Key observation:
        // If we remove interval i, then the idle region around it becomes one
        // continuous merged gap stretching:
        //
        // from:
        //   - 0, if i is the first interval
        //   - otherwise the end of interval i - 1
        //
        // to:
        //   - dayLength, if i is the last interval
        //   - otherwise the start of interval i + 1
        //
        // So the merged gap length is:
        //   rightBoundary - leftBoundary
        //
        // This formula automatically includes:
        // - the gap before interval i
        // - the full length of interval i itself
        // - the gap after interval i
        //
        // Why is this enough?
        // Because removing one interval cannot change any other unrelated gaps.
        // The only newly created candidate gap is this merged local region.
        // ------------------------------------------------------------
        for (int i = 0; i < n; i++)
        {
            // Determine the left boundary of the merged idle gap after removing interval i.
            //
            // If i == 0:
            //   there is no previous interval, so the idle region can start at minute 0.
            //
            // Otherwise:
            //   the previous remaining interval ends at intervals[i - 1][1],
            //   so the idle region starts there.
            int leftBoundary = (i == 0) ? 0 : intervals[i - 1][1];

            // Determine the right boundary of the merged idle gap after removing interval i.
            //
            // If i == n - 1:
            //   there is no next interval, so the idle region can extend to dayLength.
            //
            // Otherwise:
            //   the next remaining interval starts at intervals[i + 1][0],
            //   so the idle region ends there.
            int rightBoundary = (i == n - 1) ? dayLength : intervals[i + 1][0];

            // The entire range [leftBoundary, rightBoundary) becomes idle after removing interval i.
            int mergedGap = rightBoundary - leftBoundary;

            // Update the global best answer.
            answer = Math.Max(answer, mergedGap);
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int dayLength1 = 20;
int[][] intervals1 =
{
    new[] { 2, 5 },
    new[] { 7, 9 },
    new[] { 12, 15 }
};
int result1 = solution.MaximizeIdleGap(dayLength1, intervals1);
Console.WriteLine(result1); // Expected: 7

// Example 2
int dayLength2 = 30;
int[][] intervals2 =
{
    new[] { 0, 4 },
    new[] { 6, 10 },
    new[] { 13, 18 },
    new[] { 22, 30 }
};
int result2 = solution.MaximizeIdleGap(dayLength2, intervals2);
Console.WriteLine(result2); // Expected: 12

// Additional quick check: only one interval
int dayLength3 = 10;
int[][] intervals3 =
{
    new[] { 3, 7 }
};
int result3 = solution.MaximizeIdleGap(dayLength3, intervals3);
Console.WriteLine(result3); // Expected: 10 (remove the only interval)