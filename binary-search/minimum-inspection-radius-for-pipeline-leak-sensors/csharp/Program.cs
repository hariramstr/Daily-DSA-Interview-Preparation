/*
Title: Minimum Inspection Radius for Pipeline Leak Sensors
Difficulty: Hard
Topic: Binary Search

Problem Description:
A company operates a very long straight pipeline from position 0 to position L. Along the pipeline, there are fixed leak sensors installed at known integer positions. Each sensor can inspect every point within distance R of its own position, where R is the same for all sensors. However, some sections of the pipeline are legally classified as critical zones, and every point inside every critical zone must be covered by at least one sensor. A critical zone is given as a closed interval [start, end], and zones may overlap, touch, or be nested.

Your task is to compute the minimum integer inspection radius R such that the union of the coverage intervals of all sensors covers every critical zone completely.

More formally, if a sensor is at position x, then it covers the interval [x - R, x + R]. Find the smallest integer R for which every critical interval [a, b] is a subset of the union of all sensor coverage intervals.

You should design an algorithm efficient enough for large inputs. A brute-force check over all possible radii or all points in intervals will not pass.

Constraints:
- 1 <= sensors.length <= 2 * 10^5
- 1 <= zones.length <= 2 * 10^5
- 0 <= sensors[i] <= 10^18
- 0 <= zones[j][0] <= zones[j][1] <= 10^18
- L can be as large as 10^18, but you do not need to inspect points outside the listed critical zones
- The answer fits in a 64-bit signed integer

Example 1:
Input: sensors = [2, 8, 14], zones = [[1, 4], [6, 10], [13, 15]]
Output: 2

Example 2:
Input: sensors = [5, 11], zones = [[0, 3], [4, 8], [10, 14]]
Output: 5

Key Idea:
- Binary search the minimum radius R.
- For a fixed R, efficiently check whether the union of sensor coverage intervals covers all critical zones.
- Merge critical zones first, because overlapping/touching zones can be treated as one larger required interval.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Sorting sensors: O(n log n)
    - Sorting and merging zones: O(m log m)
    - Each feasibility check: O(n + k), where k is the number of merged zones
    - Binary search over radius: O(log AnswerRange), and AnswerRange fits in 64-bit integer
    - Total: O(n log n + m log m + (n + k) * log AnswerRange)

    Space Complexity:
    - O(k) for merged zones
    - O(1) extra beyond sorting storage, ignoring input copy details
    */
    public long MinimumInspectionRadius(long[] sensors, long[][] zones)
    {
        // Step 1:
        // Sort sensor positions.
        //
        // Why?
        // We want to scan sensor coverage intervals from left to right.
        // Sorting allows a linear feasibility check for any chosen radius R.
        Array.Sort(sensors);

        // Step 2:
        // Merge the critical zones.
        //
        // Why?
        // If zones overlap or touch, then together they form one continuous region
        // that must be fully covered. Merging reduces the number of intervals we need
        // to verify and simplifies the logic.
        //
        // Example:
        // [1,4] and [4,7] are closed intervals that touch at point 4.
        // Since every point in both must be covered, we can treat them as [1,7].
        var mergedZones = MergeZones(zones);

        // Step 3:
        // Binary search for the minimum feasible radius.
        //
        // We need a search range [low, high] such that:
        // - low is definitely not less than the answer
        // - high is definitely enough to cover all merged zones
        //
        // low starts at 0 because radius cannot be negative.
        long low = 0;

        // To get a safe upper bound, we can use the maximum distance from any zone endpoint
        // to any sensor in the worst case. A very safe and simple bound is 1e18,
        // because all coordinates are within [0, 1e18] and the answer is guaranteed to fit in long.
        long high = 1_000_000_000_000_000_000L;

        // Standard binary search for the first feasible value.
        while (low < high)
        {
            long mid = low + ((high - low) >> 1);

            // If radius mid is enough, try smaller.
            if (CanCoverAllZones(sensors, mergedZones, mid))
            {
                high = mid;
            }
            else
            {
                // Otherwise we need a larger radius.
                low = mid + 1;
            }
        }

        return low;
    }

    private List<(long Start, long End)> MergeZones(long[][] zones)
    {
        // Convert to a sortable list of pairs.
        var list = new List<(long Start, long End)>(zones.Length);
        foreach (var z in zones)
        {
            list.Add((z[0], z[1]));
        }

        // Sort by start, then by end.
        list.Sort((a, b) =>
        {
            int cmp = a.Start.CompareTo(b.Start);
            if (cmp != 0) return cmp;
            return a.End.CompareTo(b.End);
        });

        var merged = new List<(long Start, long End)>();

        foreach (var zone in list)
        {
            // If merged is empty, this is the first interval.
            if (merged.Count == 0)
            {
                merged.Add(zone);
                continue;
            }

            var last = merged[^1];

            // Because intervals are closed, touching intervals should also merge.
            // That means [1,4] and [4,6] become [1,6].
            if (zone.Start <= last.End)
            {
                // Overlap or touch: extend the last merged interval if needed.
                if (zone.End > last.End)
                {
                    merged[^1] = (last.Start, zone.End);
                }
            }
            else
            {
                // Disjoint interval: start a new merged zone.
                merged.Add(zone);
            }
        }

        return merged;
    }

    private bool CanCoverAllZones(long[] sensors, List<(long Start, long End)> mergedZones, long radius)
    {
        // This method checks whether all merged critical zones are fully covered
        // by the union of sensor intervals [sensor - radius, sensor + radius].
        //
        // Very important observation:
        // We do NOT need to explicitly build all coverage intervals and merge them.
        // Instead, because sensors are sorted, we can scan them left-to-right and
        // greedily maintain the farthest point covered so far.
        //
        // For each merged zone [a, b]:
        // - We must ensure coverage starts at or before a.
        // - Then we keep extending the covered frontier using any sensor interval
        //   that starts at or before the current frontier.
        // - If we can push the frontier to at least b, this zone is fully covered.
        // - Otherwise, this radius is not enough.

        int sensorIndex = 0;
        int n = sensors.Length;

        // coveredUntil means:
        // "the farthest coordinate that is continuously covered so far
        //  for the current zone scan"
        //
        // We reset it per zone because merged zones are disjoint.
        foreach (var zone in mergedZones)
        {
            long zoneStart = zone.Start;
            long zoneEnd = zone.End;

            // Move sensorIndex forward past sensors whose coverage ends before this zone starts.
            //
            // Why is this safe?
            // If sensor + radius < zoneStart, then that sensor cannot cover any point in this zone
            // or any later zone (since later zones start even farther right).
            while (sensorIndex < n && sensors[sensorIndex] + radius < zoneStart)
            {
                sensorIndex++;
            }

            // At this point, sensorIndex is the first sensor that might possibly help with this zone.
            long coveredUntil = zoneStart;

            // To fully cover a closed interval [zoneStart, zoneEnd], we need continuous coverage
            // from zoneStart all the way to zoneEnd.
            //
            // We repeatedly consume every sensor whose coverage interval starts at or before
            // the current covered frontier. Such a sensor can connect to the already-covered part
            // and possibly extend it farther right.
            int tempIndex = sensorIndex;
            long bestReach = coveredUntil - 1;

            // First, we need at least one sensor interval that reaches zoneStart.
            // More generally, while there are sensors whose left endpoint <= coveredUntil,
            // they can extend the current continuous coverage.
            while (true)
            {
                bool extended = false;

                // Among all sensors that can connect to the current covered segment,
                // find how far right we can extend.
                while (tempIndex < n && sensors[tempIndex] - radius <= coveredUntil)
                {
                    long reachRight = sensors[tempIndex] + radius;
                    if (reachRight > bestReach)
                    {
                        bestReach = reachRight;
                    }

                    tempIndex++;
                    extended = true;
                }

                // If we cannot extend at all, then there is a gap.
                if (!extended)
                {
                    break;
                }

                // Advance the continuous covered frontier.
                coveredUntil = bestReach;

                // If we already cover the whole zone, success for this zone.
                if (coveredUntil >= zoneEnd)
                {
                    break;
                }
            }

            // If after processing connectable sensors we still do not reach zoneEnd,
            // then this radius fails.
            if (coveredUntil < zoneEnd)
            {
                return false;
            }

            // Important optimization:
            // All sensors before tempIndex have already been considered and can never help
            // with earlier positions again. So we can permanently advance sensorIndex.
            sensorIndex = tempIndex;
        }

        // Every merged zone was fully covered.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] sensors1 = { 2, 8, 14 };
long[][] zones1 =
{
    new long[] { 1, 4 },
    new long[] { 6, 10 },
    new long[] { 13, 15 }
};
long result1 = solution.MinimumInspectionRadius(sensors1, zones1);
Console.WriteLine(result1); // Expected: 2

// Example 2
long[] sensors2 = { 5, 11 };
long[][] zones2 =
{
    new long[] { 0, 3 },
    new long[] { 4, 8 },
    new long[] { 10, 14 }
};
long result2 = solution.MinimumInspectionRadius(sensors2, zones2);
Console.WriteLine(result2); // Expected: 5