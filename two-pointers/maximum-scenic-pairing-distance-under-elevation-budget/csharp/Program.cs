/*
Title: Maximum Scenic Pairing Distance Under Elevation Budget

Problem Description:
You are given an array elevations of length n, where elevations[i] is the height of the i-th viewpoint along a mountain road.
A tourism agency wants to choose two viewpoints i and j with i < j to place synchronized photo beacons.
The pair is considered valid if the elevation difference between the two viewpoints is at most budget, that is:

    |elevations[i] - elevations[j]| <= budget

The scenic value of a valid pair is defined as the distance between the viewpoints multiplied by the lower of the two elevations:

    scenicValue(i, j) = (j - i) * min(elevations[i], elevations[j])

Return the maximum scenic value among all valid pairs. If no valid pair exists, return 0.

Constraints:
- 2 <= n <= 200000
- 1 <= elevations[i] <= 1000000000
- 0 <= budget <= 1000000000

Key idea:
The classic "container with most water" two-pointer trick alone is not enough because we also have
an elevation-difference constraint, and the array is not sorted by position or height.

A correct efficient approach:
1. Process viewpoints in descending order of elevation.
2. Maintain all already-processed indices in an ordered set by index.
   Since we process from highest to lowest elevation, the active set always contains indices whose
   heights are >= current threshold height h.
3. For a current viewpoint with height h, any valid partner whose minimum height is h must have height
   in the range [h, h + budget].
   Therefore, while processing height h, we only want active indices with heights in that range.
4. We achieve this with a sliding window over the height-sorted list:
   - Add indices whose height >= h into the active set.
   - Remove indices whose height > h + budget from the active set.
   Then the active set contains exactly indices with heights in [h, h + budget].
5. For the current index i, the best partner is simply the farthest active index from i
   (either the minimum active index or the maximum active index), because the score is:
       distance * h
   and h is fixed for this step.
6. This guarantees every valid pair is considered exactly when the lower endpoint is processed.

This is efficient and correct.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Sorting viewpoints by height: O(n log n)
    - Each index is inserted into the ordered set once and removed once: O(n log n)
    - For each viewpoint, we query the smallest and largest active index in O(log n)
    Overall: O(n log n)

    Space Complexity:
    - Sorted list of viewpoints: O(n)
    - Ordered set of active indices: O(n)
    Overall: O(n)
    */
    public long MaximumScenicPairingDistance(int[] elevations, int budget)
    {
        int n = elevations.Length;

        // We store each viewpoint as (height, index).
        // This lets us sort by height while still remembering the original road position.
        var points = new (int Height, int Index)[n];
        for (int i = 0; i < n; i++)
        {
            points[i] = (elevations[i], i);
        }

        // Sort by height in descending order.
        // Why descending?
        // Because when we process a viewpoint of height h, we want to easily maintain
        // all viewpoints with height >= h as candidates. Descending order makes that natural.
        Array.Sort(points, (a, b) =>
        {
            int cmp = b.Height.CompareTo(a.Height);
            if (cmp != 0) return cmp;
            return a.Index.CompareTo(b.Index);
        });

        // This ordered set stores indices of "currently active" viewpoints.
        // Active means their heights are within the current valid partner range [h, h + budget]
        // for the viewpoint currently being processed.
        //
        // We need an ordered structure because for a fixed current index i and fixed minimum height h,
        // the best scenic value comes from the farthest valid index:
        // either the smallest active index or the largest active index.
        //
        // SortedSet<int> gives us:
        // - insertion/removal in O(log n)
        // - Min and Max in O(1)-ish access
        var activeIndices = new SortedSet<int>();

        // addPtr:
        // points[0..addPtr-1] have already been added into the active structure at some point.
        // Since we process heights from large to small, we keep adding all points with height >= current h.
        int addPtr = 0;

        // removePtr:
        // points[0..removePtr-1] have already been removed because their heights are too large
        // for the current upper bound h + budget.
        //
        // Since current h decreases over time, the upper bound h + budget also decreases,
        // so more very-tall points may need to be removed.
        int removePtr = 0;

        long answer = 0;

        // Process each viewpoint as the "lower endpoint" of a pair.
        // If another viewpoint has height in [h, h + budget], then:
        // - the pair satisfies |height difference| <= budget
        // - the minimum height of the pair is exactly h
        //
        // Therefore the score becomes:
        //     distance * h
        // and we only need the farthest valid partner by index.
        for (int k = 0; k < n; k++)
        {
            int h = points[k].Height;
            int idx = points[k].Index;

            // STEP 1: Add all viewpoints with height >= h into the active set.
            //
            // Why is this correct?
            // Because we are currently processing height h. Any possible partner must have height >= h
            // if this current viewpoint is to be the lower endpoint (or tied lower endpoint).
            //
            // Since points are sorted descending, all such viewpoints appear before heights drop below h.
            while (addPtr < n && points[addPtr].Height >= h)
            {
                activeIndices.Add(points[addPtr].Index);
                addPtr++;
            }

            // STEP 2: Remove viewpoints with height > h + budget.
            //
            // Why?
            // The valid partner must also satisfy:
            //     partnerHeight <= h + budget
            // because |partnerHeight - h| <= budget and partnerHeight >= h.
            //
            // So the valid partner height range is exactly [h, h + budget].
            //
            // Since points are sorted descending, the tallest points are at the front.
            // As h decreases, the allowed upper bound h + budget also decreases.
            // Therefore removePtr only moves forward, and each point is removed at most once.
            long upperAllowed = (long)h + budget;
            while (removePtr < n && points[removePtr].Height > upperAllowed)
            {
                activeIndices.Remove(points[removePtr].Index);
                removePtr++;
            }

            // STEP 3: Temporarily remove the current index itself.
            //
            // Why?
            // We need a pair of two distinct viewpoints i < j, so we cannot pair a viewpoint with itself.
            bool removedCurrent = activeIndices.Remove(idx);

            // STEP 4: If there is at least one valid partner left, compute the best possible score.
            //
            // For fixed current height h, the score is:
            //     distance * h
            // So maximizing the score is equivalent to maximizing the distance.
            //
            // Among all active indices, the farthest one from idx must be either:
            // - the minimum active index
            // - the maximum active index
            //
            // No middle index can be farther than both extremes.
            if (activeIndices.Count > 0)
            {
                int leftmost = activeIndices.Min;
                int rightmost = activeIndices.Max;

                long bestDistance = Math.Max(Math.Abs((long)idx - leftmost), Math.Abs((long)idx - rightmost));
                long scenicValue = bestDistance * h;

                if (scenicValue > answer)
                {
                    answer = scenicValue;
                }
            }

            // STEP 5: Put the current index back into the active set.
            //
            // Why?
            // Future iterations process smaller heights, and this viewpoint may become a valid partner
            // for those later lower endpoints.
            if (removedCurrent)
            {
                activeIndices.Add(idx);
            }
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] elevations1 = { 8, 1, 6, 2, 5, 7 };
int budget1 = 2;
long result1 = solution.MaximumScenicPairingDistance(elevations1, budget1);
Console.WriteLine(result1); // Expected: 35

// Example 2
int[] elevations2 = { 3, 10, 4, 9, 2 };
int budget2 = 0;
long result2 = solution.MaximumScenicPairingDistance(elevations2, budget2);
Console.WriteLine(result2); // Expected: 0

// Additional quick checks
int[] elevations3 = { 5, 5 };
int budget3 = 0;
long result3 = solution.MaximumScenicPairingDistance(elevations3, budget3);
Console.WriteLine(result3); // Expected: 5

int[] elevations4 = { 1, 100, 1, 100, 1 };
int budget4 = 0;
long result4 = solution.MaximumScenicPairingDistance(elevations4, budget4);
Console.WriteLine(result4); // Expected: 200