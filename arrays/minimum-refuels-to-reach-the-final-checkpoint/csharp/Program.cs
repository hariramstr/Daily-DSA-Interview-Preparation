/*
Title: Minimum Refuels to Reach the Final Checkpoint

Problem Description:
You are driving along a straight highway toward a final checkpoint at distance target miles from your starting point.
Your car starts with startFuel liters of fuel, and it uses exactly 1 liter per mile.
Along the way, there are fuel stations described by a 2D array stations, where stations[i] = [position, fuel]
means there is a station at mile position containing fuel liters you may take if you stop there.
The stations are sorted by position in strictly increasing order.

Whenever you reach a station, you may choose to refuel there and take all of its fuel, or skip it.
Refueling itself does not consume time, but each stop counts as one refuel.
Return the minimum number of refuels needed to reach the final checkpoint.
If it is impossible to reach the target, return -1.

A greedy strategy is needed:
- We do not want to refuel too early if we might not need that stop.
- But when we cannot move farther, we should choose the best previously passed station to refuel from.
- "Best" means the station with the largest fuel amount among all stations we have already reached.

Examples:
1) target = 100, startFuel = 25, stations = [[25,25],[50,25],[75,25]]
   Output: 3

2) target = 120, startFuel = 50, stations = [[25,30],[40,20],[70,40],[95,30]]
   Output: 2
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n log n), where n is the number of stations.
    Reason:
    - Each station is added to the priority queue once.
    - Each station can be removed from the priority queue at most once.
    - Each add/remove operation on the priority queue costs O(log n).

    Space Complexity:
    O(n)
    Reason:
    - In the worst case, many reachable stations are stored in the priority queue.
    */
    public int MinRefuelStops(int target, int startFuel, int[][] stations)
    {
        // We will use a greedy strategy with a max-heap.
        //
        // Key idea:
        // As we travel forward, every station we pass becomes a "possible refuel choice".
        // We do NOT immediately decide to refuel there.
        // Instead, we save its fuel amount into a max-heap.
        //
        // Then, whenever we discover that our current fuel is not enough to reach the next required point,
        // we refuel from the largest fuel amount among all stations we have already passed.
        //
        // Why is this greedy choice correct?
        // Because if we must refuel, taking the largest available fuel gives us the farthest possible reach
        // using exactly one stop, which helps minimize the total number of stops.

        // C#'s built-in PriorityQueue is a min-heap by default.
        // To simulate a max-heap, we insert the fuel as the element,
        // and use the negative fuel as the priority.
        // Larger fuel => smaller negative number => comes out first.
        var maxHeap = new PriorityQueue<int, int>();

        // "fuelReach" means the farthest distance we can currently reach.
        // At the start, with startFuel liters and 1 liter per mile, we can reach exactly startFuel miles.
        long fuelReach = startFuel;

        // This counts how many times we actually choose to refuel.
        int refuels = 0;

        // This index walks through the stations array from left to right.
        // We will add all stations whose position is <= fuelReach into the heap,
        // because those stations are reachable with our current fuel.
        int i = 0;
        int n = stations.Length;

        // Continue until our reachable distance is enough to get to the target.
        while (fuelReach < target)
        {
            // Step 1:
            // Add every station that we can currently reach into the max-heap.
            //
            // What this means:
            // If a station is at position <= fuelReach, then we are able to drive to it.
            // Once we can reach it, its fuel becomes a candidate for future refueling.
            //
            // Why we do this:
            // We want all "available" stations stored so that if we get stuck,
            // we can choose the best one (the one with the most fuel).
            while (i < n && stations[i][0] <= fuelReach)
            {
                int stationFuel = stations[i][1];

                // Store this station's fuel in the max-heap.
                maxHeap.Enqueue(stationFuel, -stationFuel);

                i++;
            }

            // Step 2:
            // If there are no reachable stations left in the heap,
            // and we still cannot reach the target,
            // then the trip is impossible.
            //
            // Why?
            // Because:
            // - We have already added every station we can reach.
            // - None remain available to refuel from.
            // - Our current fuel reach is still short of the target.
            // Therefore, there is no legal move that can extend our journey.
            if (maxHeap.Count == 0)
            {
                return -1;
            }

            // Step 3:
            // We must refuel now, because we cannot yet reach the target,
            // and we need more fuel to continue.
            //
            // Greedy choice:
            // Take the largest fuel amount among all stations we have already passed.
            //
            // Why this is necessary:
            // If we are forced to spend one refuel stop, we should get the maximum benefit from that stop.
            // Choosing a smaller fuel amount first can only make us need more stops later.
            int bestFuel = maxHeap.Dequeue();

            // Increase our reachable distance by that fuel amount.
            fuelReach += bestFuel;

            // Count this as one refuel stop.
            refuels++;
        }

        // If we exit the loop, fuelReach >= target,
        // which means we can reach the final checkpoint.
        return refuels;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int target1 = 100;
int startFuel1 = 25;
int[][] stations1 =
{
    new[] { 25, 25 },
    new[] { 50, 25 },
    new[] { 75, 25 }
};

int result1 = solution.MinRefuelStops(target1, startFuel1, stations1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int target2 = 120;
int startFuel2 = 50;
int[][] stations2 =
{
    new[] { 25, 30 },
    new[] { 40, 20 },
    new[] { 70, 40 },
    new[] { 95, 30 }
};

int result2 = solution.MinRefuelStops(target2, startFuel2, stations2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional quick checks

// Already enough fuel to reach target without stopping
int target3 = 50;
int startFuel3 = 60;
int[][] stations3 = Array.Empty<int[]>();
int result3 = solution.MinRefuelStops(target3, startFuel3, stations3);
Console.WriteLine($"Additional Check 1 Result: {result3}"); // Expected: 0

// Impossible case
int target4 = 100;
int startFuel4 = 1;
int[][] stations4 =
{
    new[] { 10, 100 }
};
int result4 = solution.MinRefuelStops(target4, startFuel4, stations4);
Console.WriteLine($"Additional Check 2 Result: {result4}"); // Expected: -1