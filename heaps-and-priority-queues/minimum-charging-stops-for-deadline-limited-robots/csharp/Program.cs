/*
Title: Minimum Charging Stops for Deadline-Limited Robots

Problem Description:
A warehouse robot starts at position 0 with initial battery startCharge and must reach position target
on a straight track. Moving 1 unit of distance consumes 1 unit of battery.

Along the track there are charging stations, where station i is described by:
[position_i, charge_i, expiry_i]

If the robot arrives at that station at time t (time equals total distance already traveled),
it may collect the full charge_i only if t <= expiry_i. Otherwise that station has already shut down
and provides nothing.

Charging itself takes no extra time, and the robot may choose whether or not to use an available station
when it passes it. The robot cannot move backward.

Return the minimum number of charging stops needed to reach target, or -1 if it is impossible.

Key observation:
Because time equals total distance traveled on a straight path with no waiting and no backward movement,
the arrival time at a station is exactly its position. Therefore a station is usable if and only if:
position_i <= expiry_i

So each station is either:
- permanently usable when reached, or
- permanently unusable no matter what we do

That reduces the problem to the classic "minimum refueling stops" problem on the filtered usable stations.

We solve it greedily with a max-heap:
- Track the farthest position currently reachable with the battery collected so far.
- Add all usable stations whose position is within current reach into a max-heap by charge.
- If we cannot yet reach the target, take the largest available charge from the heap.
- Each heap extraction represents one charging stop.
- If the heap is empty before reaching the target, the journey is impossible.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Filtering stations: O(n)
    - Sorting usable stations by position: O(n log n)
    - Each usable station is inserted into the priority queue once: O(n log n)
    - Each chosen charging stop removes one item from the priority queue: O(n log n)
    Overall: O(n log n)

    Space Complexity:
    - Filtered station list: O(n)
    - Priority queue / max-heap: O(n)
    Overall: O(n)
    */
    public int MinChargingStops(int target, int startCharge, int[][] stations)
    {
        // Step 1:
        // Build a list containing only stations that can ever be used.
        //
        // Why this is valid:
        // The robot's time at position x is always exactly x, because:
        // - moving 1 unit takes 1 unit of time
        // - charging takes no time
        // - the robot never waits
        // - the robot never moves backward
        //
        // So station i is usable exactly when:
        // position_i <= expiry_i
        //
        // If position_i > expiry_i, then even in the best possible scenario the robot arrives
        // at time position_i, which is already too late. Such a station can be ignored completely.
        var usable = new List<(int position, int charge)>();

        foreach (var station in stations)
        {
            int position = station[0];
            int charge = station[1];
            int expiry = station[2];

            if (position <= expiry)
            {
                usable.Add((position, charge));
            }
        }

        // Step 2:
        // Sort usable stations by position so we can process them from left to right,
        // exactly in the order the robot encounters them on the track.
        usable.Sort((a, b) => a.position.CompareTo(b.position));

        // Step 3:
        // "reach" means the farthest distance the robot can currently get to
        // using the starting battery plus any charges already chosen.
        //
        // We use long instead of int because repeated additions of large charges
        // can exceed the 32-bit integer range.
        long reach = startCharge;

        // This counts how many charging stations we actually decide to use.
        int stops = 0;

        // Step 4:
        // We need a max-heap of charges from all stations we have already passed
        // (or can currently reach), but have not yet chosen to use.
        //
        // Why a max-heap?
        // When we are stuck and need more battery, taking the largest available charge
        // is the greedy choice that extends our reach the most with a single stop.
        // This is exactly what minimizes the number of stops.
        //
        // .NET's PriorityQueue is a min-heap by default, so to simulate a max-heap
        // we store priority = -charge.
        var maxHeap = new PriorityQueue<int, int>();

        // Index into the sorted usable station list.
        int i = 0;

        // Step 5:
        // Keep going until our current reachable distance is at least the target.
        while (reach < target)
        {
            // Step 5a:
            // Add every usable station whose position is within our current reach.
            //
            // These are stations the robot can physically pass before running out of battery.
            // Since they are usable (position <= expiry), they are valid charging options.
            //
            // We do not immediately commit to using them.
            // Instead, we store their charge in the heap so we can decide later,
            // only when we actually need extra battery.
            while (i < usable.Count && usable[i].position <= reach)
            {
                int charge = usable[i].charge;
                maxHeap.Enqueue(charge, -charge);
                i++;
            }

            // Step 5b:
            // If there are no available previously reached stations to use,
            // and we still cannot reach the target, then the trip is impossible.
            if (maxHeap.Count == 0)
            {
                return -1;
            }

            // Step 5c:
            // Greedily use the largest available charge.
            //
            // Why this is correct:
            // If we must make another stop, choosing the station with the largest charge
            // gives the maximum possible new reach after exactly one additional stop.
            // Any smaller choice cannot be better for minimizing the total number of stops.
            int bestCharge = maxHeap.Dequeue();
            reach += bestCharge;
            stops++;
        }

        // If we exit the loop, reach >= target, so the target is achievable.
        return stops;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int target1 = 25;
int startCharge1 = 10;
int[][] stations1 =
{
    new[] { 5, 8, 7 },
    new[] { 9, 7, 12 },
    new[] { 14, 10, 20 }
};

int result1 = solution.MinChargingStops(target1, startCharge1, stations1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int target2 = 30;
int startCharge2 = 8;
int[][] stations2 =
{
    new[] { 6, 5, 5 },
    new[] { 7, 20, 6 },
    new[] { 10, 10, 15 }
};

int result2 = solution.MinChargingStops(target2, startCharge2, stations2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity check
int target3 = 100;
int startCharge3 = 10;
int[][] stations3 =
{
    new[] { 10, 60, 100 },
    new[] { 20, 30, 100 },
    new[] { 30, 30, 100 },
    new[] { 60, 40, 100 }
};

int result3 = solution.MinChargingStops(target3, startCharge3, stations3);
Console.WriteLine(result3); // Expected: 2