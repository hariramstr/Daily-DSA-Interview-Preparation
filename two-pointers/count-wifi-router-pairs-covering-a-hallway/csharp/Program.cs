/*
Title: Count WiFi Router Pairs Covering a Hallway

Problem Description:
A company is installing WiFi routers along a long hallway. The hallway is represented by a line segment from position 0 to position L.
You are given an integer array positions of length n, where positions[i] is the location of the i-th router candidate along the hallway,
and an integer array radius of the same length, where radius[i] is the coverage radius of that candidate.

If you install router i and router j together (i < j), they cover intervals:
[positions[i] - radius[i], positions[i] + radius[i]]
and
[positions[j] - radius[j], positions[j] + radius[j]]

A pair of routers is called valid if the union of their coverage intervals covers the entire hallway [0, L].
Count the number of distinct valid pairs.

Two routers may overlap, one interval may fully contain the other, and coverage outside [0, L] is allowed.
Router candidates are not guaranteed to be sorted by position.

Constraints:
- 2 <= n <= 200000
- 1 <= L <= 1000000000
- 0 <= positions[i] <= L
- 0 <= radius[i] <= 1000000000
- positions.length == radius.length
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n log n), because we sort the transformed intervals once.
    - The two-pointer sweep itself is O(n).

    Space Complexity:
    - O(n), for storing transformed intervals and suffix maximums.

    Core idea:
    A pair of intervals [a1, b1] and [a2, b2] covers the whole hallway [0, L] if and only if:
    1) At least one interval reaches the left end: min(a1, a2) <= 0
    2) At least one interval reaches the right end: max(b1, b2) >= L
    3) There is no uncovered gap between them inside [0, L]

    Condition (3) is equivalent to saying the two intervals overlap or touch:
       max(a1, a2) <= min(b1, b2)

    Combining these facts, a pair is valid exactly when:
    - one interval starts at or before 0,
    - the other ends at or after L,
    - and they overlap/touch.

    Why this is enough:
    If one interval covers the left boundary and the other covers the right boundary, and they overlap/touch,
    then their union is one continuous segment stretching from <= 0 to >= L, so it covers [0, L].

    Efficient counting strategy:
    - Split intervals into:
      leftCover: intervals with left endpoint <= 0
      rightCover: intervals with right endpoint >= L
    - Any valid pair must consist of one interval from leftCover and one from rightCover.
    - For a left-cover interval i = [li, ri], and a right-cover interval j = [lj, rj],
      they overlap iff lj <= ri (because j already reaches right side, and i already reaches left side).
    - So for each left-cover interval, we need to count how many right-cover intervals have left endpoint <= its right endpoint.
    - But if an interval belongs to both groups, pairing it with itself is not allowed, so we subtract such self-pairs carefully.

    To do this efficiently:
    - Sort all intervals by left endpoint.
    - Build a suffix array where suffixRightCoverCount[k] = number of intervals from index k..n-1 that are right-cover intervals.
    - Also build suffixBothCount[k] = number of intervals from index k..n-1 that are both left-cover and right-cover.
    - For each left-cover interval i:
      * Find the first sorted index p where left > ri.
      * Then all intervals in [0, p-1] have left <= ri.
      * So the number of right-cover intervals overlapping i is totalRightCover - suffixRightCoverCount[p].
      * If i itself is both left-cover and right-cover, it is included in that count, but pair (i, i) is invalid,
        so subtract 1 for this i.
    - This counts ordered pairs where the first chosen interval is from leftCover and the second from rightCover.
      Every unordered valid pair is counted:
      * once if exactly one interval is left-cover and the other right-cover
      * twice if both intervals are in both groups
    - Therefore:
      answer = (orderedCount + bothBothPairsCount) / 2
      where bothBothPairsCount = number of valid unordered pairs where both intervals individually cover the whole hallway.
      But there is an even cleaner formula:
      Let B be the number of intervals that individually cover the whole hallway (both left-cover and right-cover).
      Ordered counting counts every pair of two B-intervals twice, and every other valid pair once.
      So:
         unorderedAnswer = orderedCount - C(B, 2)

    We will implement exactly that.
    */
    public long CountValidPairs(int[] positions, int[] radius, int L)
    {
        int n = positions.Length;

        // We transform each router candidate into a closed interval [left, right].
        // We store:
        // - Left endpoint
        // - Right endpoint
        // - Whether it reaches the left hallway boundary (left <= 0)
        // - Whether it reaches the right hallway boundary (right >= L)
        //
        // We use long for safety because positions +/- radius can exceed int range in intermediate arithmetic.
        var intervals = new Interval[n];
        int bothCount = 0;

        for (int i = 0; i < n; i++)
        {
            long left = (long)positions[i] - radius[i];
            long right = (long)positions[i] + radius[i];

            bool coversLeftBoundary = left <= 0;
            bool coversRightBoundary = right >= L;
            bool coversBothBoundaries = coversLeftBoundary && coversRightBoundary;

            if (coversBothBoundaries)
            {
                bothCount++;
            }

            intervals[i] = new Interval
            {
                Left = left,
                Right = right,
                CoversLeftBoundary = coversLeftBoundary,
                CoversRightBoundary = coversRightBoundary,
                CoversBothBoundaries = coversBothBoundaries
            };
        }

        // Sort by left endpoint.
        // Why?
        // Because for a fixed left-cover interval i with right endpoint ri,
        // we want to know how many right-cover intervals have left <= ri.
        // After sorting by left, this becomes a prefix count query.
        Array.Sort(intervals, (a, b) =>
        {
            int cmp = a.Left.CompareTo(b.Left);
            if (cmp != 0) return cmp;
            return a.Right.CompareTo(b.Right);
        });

        // suffixRightCoverCount[k] = number of intervals from k to n-1 that cover the right boundary.
        // This lets us quickly compute:
        // number of right-cover intervals with index < p
        // = totalRightCover - suffixRightCoverCount[p]
        //
        // suffixBothCount is not strictly necessary for the final formula,
        // but keeping the interval flags explicit makes the logic easier to follow.
        int[] suffixRightCoverCount = new int[n + 1];
        int totalRightCover = 0;

        for (int i = n - 1; i >= 0; i--)
        {
            suffixRightCoverCount[i] = suffixRightCoverCount[i + 1] + (intervals[i].CoversRightBoundary ? 1 : 0);
            if (intervals[i].CoversRightBoundary)
            {
                totalRightCover++;
            }
        }

        long orderedCount = 0;

        // We now iterate through every interval that can serve as the "left-reaching" interval.
        for (int i = 0; i < n; i++)
        {
            if (!intervals[i].CoversLeftBoundary)
            {
                // If this interval does not reach position 0,
                // it cannot be the left side of a valid covering pair in our counting scheme.
                continue;
            }

            long currentRight = intervals[i].Right;

            // Find the first index p such that intervals[p].Left > currentRight.
            //
            // Then every interval in indices [0, p-1] has Left <= currentRight,
            // meaning it overlaps or touches the current interval.
            //
            // Since we only care about partners that also cover the right boundary,
            // the number of valid partners in this prefix is:
            // totalRightCover - suffixRightCoverCount[p]
            int p = UpperBoundByLeft(intervals, currentRight);

            long overlappingRightCoverCount = totalRightCover - suffixRightCoverCount[p];

            // If the current interval itself also covers the right boundary,
            // then it is included in overlappingRightCoverCount because:
            // - its left endpoint is certainly <= its own right endpoint
            // - it is a right-cover interval
            //
            // But pairing an interval with itself is not allowed, so subtract 1.
            if (intervals[i].CoversRightBoundary)
            {
                overlappingRightCoverCount--;
            }

            orderedCount += overlappingRightCoverCount;
        }

        // orderedCount counts:
        // - every valid pair exactly once if only one of the two intervals covers the left boundary
        //   and the other covers the right boundary
        // - every valid pair of two "both-boundaries" intervals twice
        //
        // Why are two both-boundaries intervals always a valid pair?
        // Each individually covers [0, L], so certainly their union does too.
        //
        // Therefore subtract C(B, 2) to convert from ordered counting to unordered pair counting.
        long bothPairs = (long)bothCount * (bothCount - 1) / 2;

        long answer = orderedCount - bothPairs;
        return answer;
    }

    // Standard upper bound:
    // returns the first index such that intervals[index].Left > value.
    //
    // This is exactly what we need to count how many intervals have Left <= value.
    private int UpperBoundByLeft(Interval[] intervals, long value)
    {
        int lo = 0;
        int hi = intervals.Length;

        while (lo < hi)
        {
            int mid = lo + (hi - lo) / 2;

            if (intervals[mid].Left <= value)
            {
                lo = mid + 1;
            }
            else
            {
                hi = mid;
            }
        }

        return lo;
    }

    private struct Interval
    {
        public long Left;
        public long Right;
        public bool CoversLeftBoundary;
        public bool CoversRightBoundary;
        public bool CoversBothBoundaries;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] positions1 = { 2, 8, 5, 11 };
int[] radius1 = { 3, 4, 1, 2 };
int L1 = 10;
long result1 = solution.CountValidPairs(positions1, radius1, L1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] positions2 = { 1, 4, 7, 9 };
int[] radius2 = { 1, 1, 1, 1 };
int L2 = 10;
long result2 = solution.CountValidPairs(positions2, radius2, L2);
Console.WriteLine(result2); // Expected: 0

// Additional quick sanity check:
// Two routers each individually cover the whole hallway.
// There is exactly one pair.
int[] positions3 = { 5, 5 };
int[] radius3 = { 10, 10 };
int L3 = 10;
long result3 = solution.CountValidPairs(positions3, radius3, L3);
Console.WriteLine(result3); // Expected: 1