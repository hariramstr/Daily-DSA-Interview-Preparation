/*
Title: Minimum Loudspeaker Volume for Hall Announcements

Problem Description:
A convention center has a long hallway with event booths placed at known integer positions along a straight line.
You need to install loudspeakers at some of these booth positions so that every booth can hear announcements.
If a loudspeaker is set to volume radius R, it covers every booth whose position is within distance R from that loudspeaker.
You may install at most k loudspeakers, and each loudspeaker must be placed at one of the given booth positions.

Return the minimum integer radius R needed so that all booths are covered.

This problem is designed for an efficient solution using binary search on the answer.
For a fixed radius R, determine whether it is possible to cover all booth positions using at most k loudspeakers.
The booth positions are not guaranteed to be sorted and may contain duplicates.

Constraints:
- 1 <= n == positions.length <= 2 * 10^5
- 1 <= k <= n
- 0 <= positions[i] <= 10^9
- The answer fits in a 32-bit signed integer.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the positions: O(n log n)
    - Each feasibility check for a fixed radius: O(n)
    - Binary search over the answer range: O(log M), where M is the coordinate range
    - Total: O(n log n + n log M)

    Space Complexity:
    - O(n) if we clone/copy the input array for sorting
    - O(1) extra beyond the sorted array and a few variables
    */
    public int MinimumRadius(int[] positions, int k)
    {
        // We copy the input so that the original array is not modified by sorting.
        // This is not strictly required for correctness, but it is a clean habit.
        int[] sorted = new int[positions.Length];
        Array.Copy(positions, sorted, positions.Length);

        // Sorting is essential because coverage on a line is easiest to reason about
        // when positions are processed from left to right.
        Array.Sort(sorted);

        // The smallest possible radius is 0.
        int left = 0;

        // A safe upper bound for the answer is the distance between the smallest and largest booth.
        // With a large enough radius, one loudspeaker could potentially cover everything
        // if placed appropriately at one of the booth positions.
        int right = sorted[^1] - sorted[0];

        // Standard binary search on the answer:
        // We search for the smallest radius R such that covering all booths is possible.
        while (left < right)
        {
            // Midpoint radius candidate.
            int mid = left + (right - left) / 2;

            // If we can cover all booths with radius mid using at most k loudspeakers,
            // then mid is feasible, and we try to find an even smaller feasible radius.
            if (CanCoverAll(sorted, k, mid))
            {
                right = mid;
            }
            else
            {
                // Otherwise, mid is too small, so we must search larger radii.
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right and points to the minimum feasible radius.
        return left;
    }

    private bool CanCoverAll(int[] sorted, int k, int radius)
    {
        // This method answers:
        // "If every loudspeaker has coverage radius = radius,
        // can we cover all booth positions using at most k loudspeakers,
        // where each loudspeaker must be placed at one of the booth positions?"

        int n = sorted.Length;

        // i points to the leftmost booth that is not yet covered.
        int i = 0;

        // Count how many loudspeakers we have used so far.
        int used = 0;

        // We greedily cover booths from left to right.
        // This is the key idea:
        // For the current leftmost uncovered booth at position sorted[i],
        // we want to place one loudspeaker as far to the right as possible,
        // while still covering sorted[i].
        //
        // Why?
        // Because placing it farther right can only help cover more future booths,
        // which is exactly what a greedy strategy should do on a line.
        while (i < n)
        {
            used++;

            // If we already need more than k loudspeakers, this radius fails immediately.
            if (used > k)
            {
                return false;
            }

            // Let start be the leftmost uncovered booth.
            int start = sorted[i];

            // A loudspeaker placed at position p covers start if p - radius <= start,
            // which means p <= start + radius.
            //
            // Since the loudspeaker must be placed at an existing booth position,
            // we scan rightward to find the rightmost booth position <= start + radius.
            //
            // That booth is the best place to install the current loudspeaker,
            // because it still covers 'start' and pushes coverage as far right as possible.
            int placementLimit = start + radius;
            int j = i;
            while (j + 1 < n && sorted[j + 1] <= placementLimit)
            {
                j++;
            }

            // We place the loudspeaker at sorted[j].
            int speakerPosition = sorted[j];

            // This loudspeaker covers every booth up to speakerPosition + radius.
            int coverRight = speakerPosition + radius;

            // Now skip all booths that are covered by this loudspeaker.
            i = j;
            while (i < n && sorted[i] <= coverRight)
            {
                i++;
            }
        }

        // If we exit the loop, every booth was covered using at most k loudspeakers.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] positions1 = { 1, 2, 8, 12, 17 };
int k1 = 2;
int result1 = solution.MinimumRadius(positions1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] positions2 = { 4, 4, 4, 10, 15, 21 };
int k2 = 3;
int result2 = solution.MinimumRadius(positions2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo cases

int[] positions3 = { 5 };
int k3 = 1;
int result3 = solution.MinimumRadius(positions3, k3);
Console.WriteLine($"Single booth Result: {result3}");

int[] positions4 = { 1, 3, 6, 10, 15 };
int k4 = 5;
int result4 = solution.MinimumRadius(positions4, k4);
Console.WriteLine($"One loudspeaker per booth allowed Result: {result4}");

int[] positions5 = { 0, 100, 200, 300 };
int k5 = 2;
int result5 = solution.MinimumRadius(positions5, k5);
Console.WriteLine($"Spread out booths Result: {result5}");