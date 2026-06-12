/*
Title: Maximum Safe Gap for Drone Corridor Placement

Problem Description:
A city wants to place exactly k drone recharge beacons along a straight aerial corridor.
The corridor already has n approved mounting points, given as a sorted array positions,
where positions[i] is the distance in meters from the start of the corridor.
You may place at most one beacon at each mounting point.

For safety reasons, the city wants the minimum distance between any two placed beacons
to be as large as possible. Your task is to return the largest possible value d such that
it is possible to place exactly k beacons and every pair of consecutive placed beacons
is at least d meters apart.

This is an optimization problem: you are not asked to output the placement itself,
only the maximum achievable minimum gap.

Important note about Example 2 in the prompt:
The narrative in the prompt is inconsistent, but the final stated intended approach
(binary search + greedy feasibility) is correct. For positions = [3, 6, 14, 20, 25, 31]
and k = 4, the true maximum feasible minimum gap is 8:
- Place at 3
- Next at 14 (gap 11)
- Next at 25 (gap 11)
- Next at 31 is too close to 25 for gap 8, so that specific start fails
But greedy feasibility checks all placements by always taking the earliest valid next point.
Testing carefully:
For d = 8:
- Start 3
- Next 14
- Next 25
- No 4th point => fails
Try conceptually different subsets:
3, 14, 25, 31 -> last gap 6, invalid
6, 14, 25, 31 -> first gap 8, second 11, last 6, invalid
3, 14, 20, 31 -> 11, 6, 11, invalid
3, 14, 25 only 3 beacons
So d = 8 is NOT feasible.
For d = 6:
- 3, 14, 20, 31 works with gaps 11, 6, 11
Therefore the correct answer for Example 2 is 6.

So the algorithm below is written to produce:
- Example 1 => 7
- Example 2 => 6
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of mounting points.
    - Each feasibility check scans the array once, so it costs O(n).
    - We binary search the answer over the distance range from 0 to positions[n - 1] - positions[0],
      which takes O(log(range)).
    - Total time complexity: O(n log(range))

    Space Complexity:
    - We only use a few extra variables.
    - Total space complexity: O(1)
    */
    public int MaximumSafeGap(int[] positions, int k)
    {
        // The smallest possible answer is 0.
        // In this problem positions are strictly increasing, so the real answer will be at least 1
        // when k >= 2, but using 0 as the lower bound is still safe and simple.
        int left = 0;

        // The largest possible minimum gap can never exceed the distance between the first
        // and last mounting points. That is the widest spread available in the corridor.
        int right = positions[^1] - positions[0];

        // This variable stores the best feasible answer we have found so far.
        // We update it whenever a candidate distance is possible.
        int best = 0;

        // Standard binary search on the answer:
        // We are not searching for an index in the array.
        // Instead, we are searching for the largest distance d such that
        // "it is feasible to place k beacons with minimum gap at least d".
        while (left <= right)
        {
            // Pick the middle candidate distance.
            // We use this as the "guess" for the minimum required gap.
            int mid = left + (right - left) / 2;

            // Check whether it is possible to place k beacons while keeping
            // every consecutive chosen pair at least 'mid' apart.
            if (CanPlaceBeacons(positions, k, mid))
            {
                // If 'mid' is feasible, then it is a valid answer.
                // We record it as the best seen so far.
                best = mid;

                // Since we want the maximum possible minimum gap,
                // we now try to see if an even larger distance is also feasible.
                left = mid + 1;
            }
            else
            {
                // If 'mid' is not feasible, then this distance is too large.
                // Any larger distance will also be impossible,
                // so we must search the smaller half.
                right = mid - 1;
            }
        }

        // After binary search finishes, 'best' holds the largest feasible minimum gap.
        return best;
    }

    private bool CanPlaceBeacons(int[] positions, int k, int requiredGap)
    {
        // Greedy strategy:
        // Always place the first beacon at the earliest available mounting point.
        // Then, for each next beacon, place it at the earliest mounting point
        // that is at least 'requiredGap' away from the last placed beacon.
        //
        // Why is this greedy strategy correct?
        // Because placing a beacon as early as possible leaves the most room
        // for the remaining beacons. If even this most flexible strategy cannot
        // place k beacons, then no other strategy can.
        int placedCount = 1;
        int lastPlacedPosition = positions[0];

        // Start scanning from the second mounting point,
        // because the first one is already used in our greedy placement.
        for (int i = 1; i < positions.Length; i++)
        {
            // Check whether the current mounting point is far enough away
            // from the last placed beacon.
            //
            // If positions[i] - lastPlacedPosition >= requiredGap,
            // then placing a beacon here keeps the minimum gap condition valid.
            if (positions[i] - lastPlacedPosition >= requiredGap)
            {
                // Place a beacon here.
                placedCount++;
                lastPlacedPosition = positions[i];

                // As soon as we have placed k beacons, we know the candidate gap is feasible.
                // We can stop early to save time.
                if (placedCount == k)
                {
                    return true;
                }
            }
        }

        // If we finish scanning all mounting points and still have fewer than k beacons,
        // then the required gap is too large to be feasible.
        return false;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] positions1 = { 1, 2, 8, 12, 17 };
int k1 = 3;
int result1 = solution.MaximumSafeGap(positions1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 7

// Example 2
int[] positions2 = { 3, 6, 14, 20, 25, 31 };
int k2 = 4;
int result2 = solution.MaximumSafeGap(positions2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 6

// Additional demo
int[] positions3 = { 0, 4, 7, 10, 15, 20 };
int k3 = 3;
int result3 = solution.MaximumSafeGap(positions3, k3);
Console.WriteLine($"Additional Demo Result: {result3}");