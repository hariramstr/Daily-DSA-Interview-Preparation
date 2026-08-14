/*
Title: Minimum Battery Capacity for Delivery Drone Loops
Difficulty: Medium
Topic: Binary Search

Problem Description:
A delivery company operates a drone that must complete a fixed sequence of package stops in order.
The drone starts each day fully charged at the warehouse, visits the stops from left to right, and
may return to the warehouse to recharge whenever needed. A single trip from the warehouse can cover
a consecutive block of stops, but the total energy needed for that block must not exceed the drone's
battery capacity. After recharging, the drone resumes with the next unserved stop.

You are given an array energy where energy[i] is the energy required to serve stop i, and an integer
maxTrips representing the maximum number of warehouse departures allowed for the day. Your task is to
find the minimum battery capacity needed so that all stops can be served in order using at most
maxTrips trips.

Each trip must serve at least one stop, and stops cannot be reordered or split across trips. The answer
is the smallest integer capacity C such that the array can be partitioned into at most maxTrips contiguous
groups, where the sum of each group is at most C.

Constraints:
- 1 <= energy.length <= 100000
- 1 <= energy[i] <= 1000000000
- 1 <= maxTrips <= energy.length
- The result fits in a 64-bit signed integer

Example 1:
Input: energy = [7, 2, 5, 10, 8], maxTrips = 2
Output: 18
Explanation: With capacity 18, the drone can take trips [7, 2, 5] and [10, 8]. Any smaller capacity
would require more than 2 trips.

Example 2:
Input: energy = [4, 4, 4, 4, 4], maxTrips = 3
Output: 8
Explanation: One valid plan is [4, 4], [4, 4], [4]. Capacity 7 is not enough because some trip would
need to hold two stops totaling 8.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log(S))
      where n is the number of stops and S is the range of possible answers
      (from max element to total sum).
      For each binary search step, we scan the array once to check feasibility.

    Space Complexity:
    - O(1)
      We only use a few extra variables and do not allocate additional data structures
      proportional to the input size.
    */
    public long MinimumBatteryCapacity(int[] energy, int maxTrips)
    {
        // Step 1:
        // We need to determine the search range for the answer.
        //
        // Why binary search works:
        // If a battery capacity C is enough to serve all stops in at most maxTrips trips,
        // then any larger capacity will also be enough.
        // This creates a monotonic true/false pattern:
        //   too small -> false
        //   large enough -> true
        // So we can binary search for the smallest capacity that works.
        //
        // Lower bound:
        // The battery must be at least as large as the largest single stop,
        // because one stop cannot be split across trips.
        //
        // Upper bound:
        // The battery can always be the total sum of all stops,
        // which would allow serving everything in one trip.
        long left = 0;
        long right = 0;

        foreach (int value in energy)
        {
            // Keep track of the largest single energy requirement.
            // This is the minimum possible valid capacity.
            if (value > left)
            {
                left = value;
            }

            // Add all energy values to get the total sum.
            // This is definitely a valid capacity because it can fit all stops in one trip.
            right += value;
        }

        // Step 2:
        // Perform binary search on the answer range [left, right].
        //
        // Goal:
        // Find the smallest capacity that is feasible.
        while (left < right)
        {
            // Compute the middle carefully.
            // This avoids overflow compared to (left + right) / 2,
            // although long is already large enough for this problem.
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this proposed capacity "mid" is enough.
            //
            // If it is enough, we try smaller values by moving the right boundary.
            // If it is not enough, we must increase capacity by moving the left boundary.
            if (CanServeAllStops(energy, maxTrips, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the smallest feasible capacity.
        return left;
    }

    private bool CanServeAllStops(int[] energy, int maxTrips, long capacity)
    {
        // This method greedily counts how many trips are needed if the battery capacity is "capacity".
        //
        // Greedy idea:
        // We always put as many consecutive stops as possible into the current trip.
        //
        // Why this greedy strategy is correct:
        // If we are trying to minimize the number of trips for a fixed capacity,
        // it is always safe to keep adding the next stop while it still fits.
        // Starting a new trip earlier would never reduce the total number of trips.
        //
        // Data structure choice:
        // We do not need any complex data structure here.
        // A simple running sum and a trip counter are enough because the stops must be processed
        // in order and each stop is considered exactly once.

        // We start with one trip because the first stop must belong to some trip.
        int tripsUsed = 1;

        // currentTripEnergy stores the total energy currently assigned to the ongoing trip.
        long currentTripEnergy = 0;

        foreach (int stopEnergy in energy)
        {
            // Step A:
            // Try to add the current stop to the ongoing trip.
            //
            // If it fits, we simply extend the current contiguous block.
            if (currentTripEnergy + stopEnergy <= capacity)
            {
                currentTripEnergy += stopEnergy;
            }
            else
            {
                // Step B:
                // If the current stop does not fit, we must start a new trip.
                //
                // This is necessary because:
                // - Stops must remain in order
                // - Stops cannot be split
                // - Each trip must be a contiguous group
                tripsUsed++;

                // If we already exceeded the allowed number of trips,
                // then this capacity is not feasible.
                if (tripsUsed > maxTrips)
                {
                    return false;
                }

                // The new trip starts with the current stop.
                currentTripEnergy = stopEnergy;
            }
        }

        // If we finished scanning all stops without exceeding maxTrips,
        // then this capacity works.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] energy1 = { 7, 2, 5, 10, 8 };
int maxTrips1 = 2;
long result1 = solution.MinimumBatteryCapacity(energy1, maxTrips1);
Console.WriteLine(result1); // Expected: 18

// Example 2
int[] energy2 = { 4, 4, 4, 4, 4 };
int maxTrips2 = 3;
long result2 = solution.MinimumBatteryCapacity(energy2, maxTrips2);
Console.WriteLine(result2); // Expected: 8

// Additional quick demo
int[] energy3 = { 1, 2, 3, 4, 5 };
int maxTrips3 = 2;
long result3 = solution.MinimumBatteryCapacity(energy3, maxTrips3);
Console.WriteLine(result3); // Expected: 9 because [1,2,3] and [4,5] works, but 8 does not.