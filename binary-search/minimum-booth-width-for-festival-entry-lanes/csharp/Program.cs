/*
Title: Minimum Booth Width for Festival Entry Lanes

Problem Description:
A music festival is setting up several entry lanes for attendees. Each group of attendees must stay together in the same lane, and groups must be processed in the given order because their tickets are tied to scheduled arrival windows. You are given an array groups where groups[i] is the number of people in the i-th arriving group, and an integer m representing the number of available entry lanes.

Every lane can handle a contiguous sequence of groups, and the total number of people assigned to a single lane cannot exceed that lane's booth width capacity. Your task is to compute the minimum booth width needed so that all groups can be assigned to at most m lanes.

In other words, partition the array into at most m contiguous parts while minimizing the maximum part sum.

Return the smallest integer booth width that makes such an assignment possible.

Constraints:
- 1 <= groups.length <= 100000
- 1 <= groups[i] <= 1000000000
- 1 <= m <= groups.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: groups = [12, 7, 15, 9, 10], m = 3
Output: 22

Example 2:
Input: groups = [5, 5, 5, 5, 5, 5], m = 2
Output: 15
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log(S))
      where:
      n = number of groups
      S = range of possible answers, from max(groups) to sum(groups)
    - For each binary search guess, we scan the array once to check feasibility.

    Space Complexity:
    - O(1)
      We only use a few variables and do not allocate extra data structures
      proportional to the input size.
    */
    public long MinimumBoothWidth(int[] groups, int m)
    {
        // Step 1:
        // Establish the binary search boundaries.
        //
        // Why do we need boundaries?
        // Because we are searching for the smallest booth width that works.
        // Binary search needs a "low" and a "high" value such that:
        // - every answer below low is impossible
        // - at least one answer at or above high is possible
        //
        // The smallest possible booth width can never be less than the largest single group,
        // because a group cannot be split across lanes.
        //
        // The largest possible booth width is the sum of all groups,
        // because one lane could theoretically handle everyone if only one lane were needed.
        long low = 0;
        long high = 0;

        foreach (int group in groups)
        {
            if (group > low)
            {
                low = group;
            }

            high += group;
        }

        // Step 2:
        // Perform binary search on the answer.
        //
        // Why binary search works:
        // There is a monotonic property here:
        // - If a booth width X is feasible, then any width larger than X is also feasible.
        // - If a booth width X is not feasible, then any width smaller than X is also not feasible.
        //
        // That monotonic true/false pattern is exactly what binary search needs.
        while (low < high)
        {
            // Step 2a:
            // Pick the middle candidate width.
            //
            // We use this form to avoid overflow:
            // mid = low + (high - low) / 2
            long mid = low + (high - low) / 2;

            // Step 2b:
            // Check whether this candidate width is enough.
            //
            // If it is feasible, we try to find an even smaller feasible width
            // by moving the right boundary down to mid.
            //
            // If it is not feasible, we must increase the width,
            // so we move the left boundary up to mid + 1.
            if (CanSplitIntoAtMostMLanes(groups, m, mid))
            {
                high = mid;
            }
            else
            {
                low = mid + 1;
            }
        }

        // Step 3:
        // When low == high, binary search has found the smallest feasible width.
        return low;
    }

    private bool CanSplitIntoAtMostMLanes(int[] groups, int m, long capacity)
    {
        // This helper method answers:
        // "If every lane can handle at most 'capacity' people,
        // can we assign all groups in order using at most m lanes?"
        //
        // We use a greedy strategy:
        // - Keep adding groups to the current lane while they fit.
        // - As soon as the next group would exceed capacity, start a new lane.
        //
        // Why greedy is correct here:
        // For a fixed capacity, packing as many consecutive groups as possible into the current lane
        // minimizes the number of lanes used. Starting a new lane earlier would never help reduce
        // the total number of lanes needed.
        //
        // Data structure choice:
        // We do not need any complex data structure.
        // A few counters are enough because we only scan from left to right once.

        long currentLaneLoad = 0;
        int lanesUsed = 1;

        foreach (int group in groups)
        {
            // If adding this group would exceed the allowed capacity for the current lane,
            // we must open a new lane.
            if (currentLaneLoad + group > capacity)
            {
                lanesUsed++;
                currentLaneLoad = group;

                // Early exit optimization:
                // If we already need more than m lanes, this capacity is not feasible.
                if (lanesUsed > m)
                {
                    return false;
                }
            }
            else
            {
                // Otherwise, safely place this group into the current lane.
                currentLaneLoad += group;
            }
        }

        // If we finished processing all groups using at most m lanes,
        // then this capacity works.
        return true;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] groups1 = { 12, 7, 15, 9, 10 };
int m1 = 3;
long result1 = solution.MinimumBoothWidth(groups1, m1);
Console.WriteLine(result1); // Expected: 22

// Example 2
int[] groups2 = { 5, 5, 5, 5, 5, 5 };
int m2 = 2;
long result2 = solution.MinimumBoothWidth(groups2, m2);
Console.WriteLine(result2); // Expected: 15

// Additional quick sanity checks
int[] groups3 = { 1 };
int m3 = 1;
Console.WriteLine(solution.MinimumBoothWidth(groups3, m3)); // Expected: 1

int[] groups4 = { 7, 2, 5, 10, 8 };
int m4 = 2;
Console.WriteLine(solution.MinimumBoothWidth(groups4, m4)); // Expected: 18