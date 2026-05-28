/*
 * ============================================================
 * Title: Maximum Score from Deleting Overlapping Intervals in a Window
 * ============================================================
 * Problem Description:
 * You are given a list of n sensor readings, where each reading is
 * represented as [start, end, value]. The start and end represent a
 * time range (inclusive) during which the sensor is active, and value
 * is the score contributed by that sensor.
 *
 * You are also given an integer W, representing the size of a sliding
 * time window. For each window of size W (i.e., every contiguous time
 * range [t, t+W-1]), you must select a NON-OVERLAPPING subset of sensors
 * that are FULLY CONTAINED within the window (start >= t AND end <= t+W-1).
 * The goal is to maximize the total score from the selected non-overlapping
 * sensors.
 *
 * Return an array result where result[i] is the maximum score achievable
 * in the window starting at time i.
 *
 * Constraints:
 *   1 <= n <= 1000
 *   0 <= start <= end <= 10^4
 *   1 <= value <= 10^4
 *   1 <= W <= 10^4
 *   Time ranges start from 0 and the last window starts at max(end) - W + 1
 *
 * Key Insight:
 *   For each window [t, t+W-1], we need the weighted interval scheduling
 *   problem: find a non-overlapping subset of intervals (fully inside the
 *   window) with maximum total value.
 *
 *   Weighted Interval Scheduling uses dynamic programming:
 *   - Sort intervals by end time
 *   - dp[i] = max score using intervals from the sorted list up to index i
 *   - For each interval i, either skip it (dp[i-1]) or take it
 *     (value[i] + dp[p(i)]) where p(i) is the last interval that doesn't
 *     overlap with interval i.
 * ============================================================
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ============================================================
// Solution Class
// ============================================================
public class Solution
{
    // --------------------------------------------------------
    // Method: MaxScoreWindows
    //
    // Time Complexity:  O(K * (n log n + n)) where K is the number of windows
    //                   and n is the number of readings. In the worst case,
    //                   K ~ max_end and n ~ 1000, so roughly O(10^4 * 1000).
    //
    // Space Complexity: O(n) for the dp array and filtered intervals per window.
    //
    // Approach:
    //   1. Determine the range of window start times: 0 to (maxEnd - W + 1).
    //   2. For each window start t, collect all readings fully inside [t, t+W-1].
    //   3. Apply the Weighted Interval Scheduling DP on those readings.
    //   4. Store the result for window t.
    // --------------------------------------------------------
    public int[] MaxScoreWindows(int[][] readings, int W)
    {
        // -------------------------------------------------------
        // Step 1: Find the maximum end time across all readings.
        // This determines how many windows we need to evaluate.
        // The last window starts at (maxEnd - W + 1) so that the
        // window [lastStart, lastStart + W - 1] still covers maxEnd.
        // -------------------------------------------------------
        int maxEnd = 0;
        foreach (var r in readings)
        {
            // r[1] is the end time of this reading
            if (r[1] > maxEnd)
                maxEnd = r[1];
        }

        // -------------------------------------------------------
        // Step 2: Compute the number of windows.
        // Windows start at t = 0, 1, 2, ..., (maxEnd - W + 1).
        // If maxEnd < W - 1, there is still at least 1 window (t=0).
        // -------------------------------------------------------
        int lastWindowStart = Math.Max(0, maxEnd - W + 1);
        int numWindows = lastWindowStart + 1; // windows: 0, 1, ..., lastWindowStart

        // Prepare the result array
        int[] result = new int[numWindows];

        // -------------------------------------------------------
        // Step 3: For each window starting at time t, solve the
        // Weighted Interval Scheduling problem.
        // -------------------------------------------------------
        for (int t = 0; t <= lastWindowStart; t++)
        {
            // The window covers [t, t + W - 1]
            int windowEnd = t + W - 1;

            // ---------------------------------------------------
            // Step 3a: Filter readings that are FULLY inside this window.
            // A reading [start, end, value] is fully inside if:
            //   start >= t  AND  end <= windowEnd
            // ---------------------------------------------------
            var windowReadings = new List<int[]>();
            foreach (var r in readings)
            {
                int s = r[0]; // start time of this reading
                int e = r[1]; // end time of this reading
                int v = r[2]; // value/score of this reading

                if (s >= t && e <= windowEnd)
                {
                    windowReadings.Add(r);
                }
            }

            // ---------------------------------------------------
            // Step 3b: If no readings fit in this window, score = 0.
            // ---------------------------------------------------
            if (windowReadings.Count == 0)
            {
                result[t] = 0;
                continue;
            }

            // ---------------------------------------------------
            // Step 3c: Sort the filtered readings by their END time.
            // This is the standard first step in Weighted Interval
            // Scheduling DP. Sorting by end time lets us efficiently
            // find the last non-overlapping interval using binary search.
            // ---------------------------------------------------
            var sorted = windowReadings.OrderBy(r => r[1]).ThenBy(r => r[0]).ToArray();

            int m = sorted.Length;

            // ---------------------------------------------------
            // Step 3d: Precompute p[i] for each interval i (0-indexed).
            // p[i] = the index of the last interval (in sorted order)
            //        whose end time is STRICTLY LESS THAN sorted[i]'s start time.
            //        (i.e., it doesn't overlap with interval i)
            // p[i] = -1 means no such interval exists.
            //
            // Two intervals [s1,e1] and [s2,e2] are NON-OVERLAPPING if
            // e1 < s2 (since ranges are inclusive, e1 < s2 means they
            // don't share any time point).
            //
            // We use binary search on the sorted end times to find p[i].
            // ---------------------------------------------------
            int[] endTimes = new int[m];
            for (int i = 0; i < m; i++)
                endTimes[i] = sorted[i][1];

            int[] p = new int[m];
            for (int i = 0; i < m; i++)
            {
                int startI = sorted[i][0]; // start time of interval i

                // Binary search: find the rightmost interval j such that
                // endTimes[j] < startI  (strictly less, so no overlap)
                int lo = 0, hi = i - 1, best = -1;
                while (lo <= hi)
                {
                    int mid = (lo + hi) / 2;
                    if (endTimes[mid] < startI)
                    {
                        // mid is a valid non-overlapping predecessor
                        best = mid;
                        lo = mid + 1; // try to find a later one
                    }
                    else
                    {
                        hi = mid - 1;
                    }
                }
                p[i] = best; // -1 if no predecessor found
            }

            // ---------------------------------------------------
            // Step 3e: Weighted Interval Scheduling DP.
            //
            // dp[i] = maximum score considering intervals 0..i (0-indexed).
            //
            // Recurrence:
            //   dp[i] = max(
            //     dp[i-1],                          // skip interval i
            //     sorted[i][2] + (p[i] >= 0 ? dp[p[i]] : 0)  // take interval i
            //   )
            //
            // Base case: dp[-1] = 0 (no intervals considered = score 0).
            // We use a 1-indexed dp array for convenience:
            //   dp[0] = 0 (base case)
            //   dp[i] corresponds to sorted[i-1] (1-indexed)
            // ---------------------------------------------------
            int[] dp = new int[m + 1];
            dp[0] = 0; // base case: no intervals selected

            for (int i = 1; i <= m; i++)
            {
                // i-1 is the 0-indexed position in sorted[]
                int val = sorted[i - 1][2]; // value of current interval
                int pred = p[i - 1];        // predecessor index (0-indexed), or -1

                // Option 1: Skip interval i → inherit dp[i-1]
                int skipScore = dp[i - 1];

                // Option 2: Take interval i
                // If pred == -1, no predecessor, so score = val + 0
                // If pred >= 0, score = val + dp[pred + 1]
                //   (pred is 0-indexed, dp is 1-indexed, so dp[pred+1])
                int takeScore = val + (pred >= 0 ? dp[pred + 1] : 0);

                // Take the better option
                dp[i] = Math.Max(skipScore, takeScore);
            }

            // ---------------------------------------------------
            // Step 3f: dp[m] holds the maximum score for this window.
            // ---------------------------------------------------
            result[t] = dp[m];
        }

        return result;
    }
}

// ============================================================
// Demo / Test Code
// ============================================================

var solution = new Solution();

// ----------------------------------------------------------
// Example 1:
// readings = [[0,2,10],[1,3,5],[2,4,8],[3,5,6]], W = 4
//
// Let's trace manually:
//   maxEnd = 5, lastWindowStart = 5 - 4 + 1 = 2
//   Windows: t=0 → [0,3], t=1 → [1,4], t=2 → [2,5]
//
//   Window [0,3]:
//     Fully inside: [0,2,10] (0>=0, 2<=3 ✓), [1,3,5] (1>=0, 3<=3 ✓)
//     [2,4,8]: 4 > 3 ✗, [3,5,6]: 5 > 3 ✗
//     Sorted by end: [0,2,10], [1,3,5]
//     p[0]=−1, p[1]: start=1, find end<1 → none → p[1]=−1
//     dp[1]=max(dp[0], 10+0)=10
//     dp[2]=max(dp[1], 5+0)=max(10,5)=10
//     Result: 10
//
//   Window [1,4]:
//     Fully inside: [1,3,5] (1>=1,3<=4 ✓), [2,4,8] (2>=1,4<=4 ✓)
//     [0,2,10]: 0<1 ✗, [3,5,6]: 5>4 ✗
//     Sorted by end: [1,3,5], [2,4,8]
//     p[0]=−1, p[1]: start=2, find end<2 → none (end[0]=3 ≥ 2) → p[1]=−1
//     dp[1]=max(0,5)=5
//     dp[2]=max(5,8+0)=8
//     Result: 8
//
//   Window [2,5]:
//     Fully inside: [2,4,8] (2>=2,4<=5 ✓), [3,5,6] (3>=2,5<=5 ✓)
//     Sorted by end: [2,4,8], [3,5,6]
//     p[0]=−1, p[1]: start=3, find end<3 → none (end[0]=4 ≥ 3) → p[1]=−1
//     dp[1]=max(0,8)=8
//     dp[2]=max(8,6+0)=8
//     Result: 8
//
//   Output: [10, 8, 8]
//
// NOTE: The problem's example output [18, 14, 14] appears to use different
// overlap semantics (end-exclusive or allowing touching endpoints as
// non-overlapping). Let's re-examine:
//   The problem says "non-overlapping" — if two intervals [s1,e1] and [s2,e2]
//   are considered non-overlapping when e1 < s2 (strictly), then [0,2] and
//   [3,5] don't overlap (2 < 3). But [3,5,6] is NOT fully inside window [0,3]
//   since 5 > 3. So 18 can't come from that window.
//
//   Looking at example output [18,14,14] more carefully with W=4:
//   Window [0,3]: only [0,2,10] and [1,3,5] fit. Max non-overlapping = 10.
//   The problem explanation seems to have errors. Our algorithm is correct.
//   Expected (corrected): [10, 8, 8]
// ----------------------------------------------------------

Console.WriteLine("=== Example 1 ===");
int[][] readings1 = new int[][]
{
    new int[] { 0, 2, 10 },
    new int[] { 1, 3, 5 },
    new int[] { 2, 4, 8 },
    new int[] { 3, 5, 6 }
};
int W1 = 4;
int[] result1 = solution.MaxScoreWindows(readings1, W1);
Console.WriteLine("Input readings: [[0,2,10],[1,3,5],[2,4,8],[3,5,6]], W=4");
Console.WriteLine("Output: [" + string.Join(", ", result1) + "]");
// Traced output: [10, 8, 8]
// Window [0,3]: [0,2,10] and [1,3,5] fit; [0,2] and [1,3] overlap (2 >= 1), best = 10
// Window [1,4]: [1,3,5] and [2,4,8] fit; they overlap, best = 8
// Window [2,5]: [2,4,8] and [3,5,6] fit; they overlap (4 >= 3), best = 8
Console.WriteLine("Expected (corrected): [10, 8, 8]");
Console.WriteLine();

// ----------------------------------------------------------
// Example 2:
// readings = [[0,1,7],[0,3,10],[2,3,6],[4,5,9]], W = 4
//
//   maxEnd = 5, lastWindowStart = 5 - 4 + 1 = 2
//   Windows: t=0 → [0,3], t=1 → [1,4], t=2 → [2,5]
//
//   Window [0,3]:
//     Fully inside: [0,1,7] (0>=0,1<=3 ✓), [0,3,10] (0>=0,3<=3 ✓), [2,3,6] (2>=0,3<=3 ✓)
//     [4,5,9]: 4>3 ✗
//     Sorted by end: [0,1,7], [0,3,10], [2,3,6]
//     p[0]=−1
//     p[1]: start