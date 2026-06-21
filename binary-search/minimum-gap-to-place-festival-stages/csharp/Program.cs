/*
Title: Minimum Gap to Place Festival Stages

Problem Description:
You are organizing a large outdoor festival along a straight road. There are n approved
installation points, given as a sorted or unsorted array positions, where positions[i]
is the coordinate of the i-th point. You must place exactly k stages at distinct
installation points.

For safety and crowd control, the festival authority defines the gap of a placement as
the minimum distance between any two chosen stages. A placement is considered valid if
every pair of neighboring chosen stages is at least that gap apart. Your task is to
compute the largest possible gap that can be guaranteed while still placing all k stages.

Return the maximum integer value g such that it is possible to choose exactly k
installation points and the distance between every two consecutive chosen points is at
least g.

Key idea:
If a gap g is feasible, then every smaller gap is also feasible. This monotonic behavior
allows us to binary search on the answer.

Constraints:
- 2 <= n <= 2 * 10^5
- 2 <= k <= n
- 0 <= positions[i] <= 10^9
- All installation points are distinct integers.

Example 1:
Input: positions = [1, 2, 8, 12, 17], k = 3
Output: 7

Example 2:
Input: positions = [4, 15, 7, 20, 1, 11], k = 4
Output: 4
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the positions array takes O(n log n)
    - Each feasibility check takes O(n)
    - Binary search runs for O(log R), where R = maxPosition - minPosition
    - Total: O(n log n + n log R)

    Space Complexity:
    - O(1) extra space beyond the sorting implementation details
    - Note: Array.Sort may use implementation-dependent stack space
    */
    public int MaximizeMinimumGap(int[] positions, int k)
    {
        // Step 1:
        // Sort the installation points so that we can reason about distances
        // from left to right.
        //
        // Why this is necessary:
        // The greedy feasibility check depends on visiting points in increasing order.
        // Once sorted, if we place a stage as early as possible, we leave as much room
        // as possible for the remaining stages. This is the standard greedy strategy
        // for spacing problems.
        Array.Sort(positions);

        // Step 2:
        // Establish the binary search range for the answer.
        //
        // The minimum possible gap is 0 in theory, but because all positions are distinct
        // and k >= 2, the practical answer will be at least 1. Still, using 0 is safe.
        //
        // The maximum possible gap cannot exceed the distance between the leftmost and
        // rightmost installation points.
        int left = 0;
        int right = positions[^1] - positions[0];

        // This variable will store the best feasible gap found so far.
        int best = 0;

        // Step 3:
        // Binary search over the gap value.
        //
        // Why binary search works:
        // - If a gap "mid" is feasible, then any smaller gap is also feasible.
        // - If a gap "mid" is not feasible, then any larger gap is also not feasible.
        //
        // This monotonic true/false pattern is exactly what binary search needs.
        while (left <= right)
        {
            // Compute the middle gap carefully.
            // This avoids overflow compared to (left + right) / 2,
            // although with current constraints int is still safe.
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check whether it is possible to place exactly k stages such that
            // every consecutive chosen stage is at least "mid" units apart.
            if (CanPlaceWithGap(positions, k, mid))
            {
                // If "mid" is feasible, it is a valid candidate answer.
                // We record it and try to do even better by searching to the right.
                best = mid;
                left = mid + 1;
            }
            else
            {
                // If "mid" is not feasible, then this gap is too large.
                // We must search smaller gaps on the left side.
                right = mid - 1;
            }
        }

        // Step 5:
        // After binary search finishes, "best" contains the largest feasible gap.
        return best;
    }

    private bool CanPlaceWithGap(int[] positions, int k, int requiredGap)
    {
        // This method performs a greedy feasibility check.
        //
        // Goal:
        // Determine whether we can place at least k stages so that the distance
        // between every two consecutive chosen stages is at least requiredGap.
        //
        // Why greedy works:
        // We always place the next stage at the earliest possible valid position.
        // This is optimal for feasibility because choosing an earlier valid point
        // can never reduce our ability to place future stages; in fact, it leaves
        // more room for the remaining placements.

        // Step 1:
        // Always place the first stage at the leftmost installation point.
        //
        // Why this is safe:
        // For a feasibility check, placing the first stage as early as possible
        // gives the maximum remaining space for the rest.
        int placedCount = 1;
        int lastPlacedPosition = positions[0];

        // Step 2:
        // Scan through the sorted positions from left to right and greedily place
        // a stage whenever the current point is far enough from the last placed stage.
        for (int i = 1; i < positions.Length; i++)
        {
            // Compute the distance from the most recently chosen stage.
            int distanceFromLastPlaced = positions[i] - lastPlacedPosition;

            // If this point satisfies the required minimum gap, we place a stage here.
            if (distanceFromLastPlaced >= requiredGap)
            {
                placedCount++;
                lastPlacedPosition = positions[i];

                // Early exit optimization:
                // As soon as we have placed k stages, we know the gap is feasible.
                if (placedCount >= k)
                {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k stages placed,
        // then this required gap is not feasible.
        return false;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// positions = [1, 2, 8, 12, 17], k = 3
// Optimal choice: 1, 8, 17
// Gaps are 7 and 9, so answer is 7.
int[] positions1 = { 1, 2, 8, 12, 17 };
int k1 = 3;
int result1 = solution.MaximizeMinimumGap(positions1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// positions = [4, 15, 7, 20, 1, 11], k = 4
// After sorting: [1, 4, 7, 11, 15, 20]
// One optimal choice: 1, 7, 11, 15
// Minimum gap is 4, so answer is 4.
int[] positions2 = { 4, 15, 7, 20, 1, 11 };
int k2 = 4;
int result2 = solution.MaximizeMinimumGap(positions2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick demo
int[] positions3 = { 0, 5, 10, 15, 20 };
int k3 = 3;
int result3 = solution.MaximizeMinimumGap(positions3, k3);
Console.WriteLine($"Additional Demo Result: {result3}");