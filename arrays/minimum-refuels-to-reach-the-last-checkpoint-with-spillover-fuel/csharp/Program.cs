/*
Title: Minimum Refuels to Reach the Last Checkpoint with Spillover Fuel

Problem Description:
You are planning a road trip through a straight desert highway. The destination is at position target,
measured in miles from the start. Your car begins at position 0 with startFuel liters of fuel, and it
consumes exactly 1 liter per mile. Along the way, there are gas stations represented by an array
stations, where stations[i] = [position_i, fuel_i] means there is a station at mile position_i
containing fuel_i liters of fuel.

Unlike a normal refueling problem, every time you stop at a station, you must take all of its fuel.
If your tank already contains some fuel, that remaining fuel stays in the tank, so the station's fuel
simply spills over into your current amount. You may stop at any subset of stations that you can
physically reach, and each station can be used at most once.

Return the minimum number of refueling stops needed to reach target. If it is impossible, return -1.

Constraints:
- 1 <= target <= 10^9
- 0 <= startFuel <= 10^9
- 0 <= stations.length <= 10^5
- 0 < position_i < target
- 1 <= fuel_i <= 10^9
- stations is sorted by position_i in strictly increasing order

Examples:
1)
Input: target = 100, startFuel = 25, stations = [[10,30],[20,20],[40,40],[70,25]]
Output: 3

2)
Input: target = 120, startFuel = 50, stations = [[25,25],[60,30],[90,20]]
Output: 2
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - Each station is inserted into the priority queue at most once.
    - Each station is removed from the priority queue at most once.
    - Priority queue operations take O(log n).

    Space Complexity: O(n)
    - In the worst case, all reachable stations may be stored in the priority queue.
    */
    public int MinRefuelStops(int target, int startFuel, int[][] stations)
    {
        // We use long instead of int for safety.
        // Why?
        // Because target, startFuel, and station fuel values can each be as large as 1e9.
        // During the process, fuel can accumulate beyond the range of int when many stations are used.
        long maxReach = startFuel;

        // This will count how many refueling stops we actually make.
        int stops = 0;

        // This index tells us which stations we have already processed and added to the heap.
        int i = 0;
        int n = stations.Length;

        // We need a way to always choose the best previously reachable station when we get stuck.
        // "Best" means the station with the largest fuel amount, because taking more fuel now
        // gives us the greatest possible extension of our reachable distance.
        //
        // C# PriorityQueue is a min-heap by default, so to simulate a max-heap,
        // we store the fuel as the element and use negative fuel as the priority.
        var maxHeap = new PriorityQueue<int, int>();

        // Main idea:
        // maxReach means "the farthest distance we can currently reach with the fuel collected so far".
        //
        // If maxReach is already at least target, we are done.
        // Otherwise:
        //   1. Add every station whose position is <= maxReach into the heap,
        //      because those stations are reachable right now.
        //   2. If there are no reachable stations left to use, then we are stuck -> return -1.
        //   3. Otherwise, take the station with the largest fuel from the heap,
        //      add its fuel to maxReach, and count one stop.
        //
        // This greedy strategy is optimal:
        // whenever we must refuel, choosing the largest available fuel gives the maximum future reach,
        // which minimizes the number of stops.
        while (maxReach < target)
        {
            // Step 1:
            // Add all stations that are reachable with our current fuel into the max-heap.
            //
            // Why is this necessary?
            // Because any station at position <= maxReach can be visited before we run out of fuel.
            // We do not need to decide immediately whether to use it.
            // Instead, we store all reachable options and delay the decision until it becomes necessary.
            //
            // This "delay the choice" approach is powerful:
            // it lets us make the best possible greedy choice only when we actually need more fuel.
            while (i < n && stations[i][0] <= maxReach)
            {
                int fuelAtStation = stations[i][1];

                // Insert this station's fuel into the max-heap.
                // Element = fuel amount
                // Priority = negative fuel amount, so larger fuel comes out first.
                maxHeap.Enqueue(fuelAtStation, -fuelAtStation);

                i++;
            }

            // Step 2:
            // If there are no reachable stations available, then we cannot move any farther.
            //
            // Why?
            // Because:
            // - We have already added every station we can currently reach.
            // - The heap is empty, so there is no station available to refuel from.
            // - Therefore, we are stuck before reaching the target.
            if (maxHeap.Count == 0)
            {
                return -1;
            }

            // Step 3:
            // We must refuel now, so choose the reachable station with the largest fuel amount.
            //
            // Why choose the largest?
            // Because if we are forced to spend one stop, we want that stop to help us as much as possible.
            // Taking the largest available fuel maximizes our new reachable distance.
            int bestFuel = maxHeap.Dequeue();

            // Add that fuel to our total reachable distance.
            // Conceptually:
            // - We had enough fuel to reach some point maxReach.
            // - By stopping at this station and taking all its fuel,
            //   we can now extend our journey by bestFuel more miles.
            maxReach += bestFuel;

            // Count this refueling stop.
            stops++;
        }

        // If we exit the loop, maxReach >= target, so we successfully reached the destination.
        return stops;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int target1 = 100;
int startFuel1 = 25;
int[][] stations1 =
{
    new[] { 10, 30 },
    new[] { 20, 20 },
    new[] { 40, 40 },
    new[] { 70, 25 }
};

int result1 = solution.MinRefuelStops(target1, startFuel1, stations1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int target2 = 120;
int startFuel2 = 50;
int[][] stations2 =
{
    new[] { 25, 25 },
    new[] { 60, 30 },
    new[] { 90, 20 }
};

int result2 = solution.MinRefuelStops(target2, startFuel2, stations2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional demo: impossible case
int target3 = 100;
int startFuel3 = 10;
int[][] stations3 =
{
    new[] { 20, 30 },
    new[] { 40, 40 }
};

int result3 = solution.MinRefuelStops(target3, startFuel3, stations3);
Console.WriteLine($"Example 3 Result: {result3}"); // Expected: -1

// Additional demo: already enough fuel
int target4 = 50;
int startFuel4 = 60;
int[][] stations4 = Array.Empty<int[]>();

int result4 = solution.MinRefuelStops(target4, startFuel4, stations4);
Console.WriteLine($"Example 4 Result: {result4}"); // Expected: 0