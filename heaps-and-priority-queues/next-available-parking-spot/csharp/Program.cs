/*
Title: Next Available Parking Spot
Difficulty: Easy
Topic: Heaps and Priority Queues

Problem Description:
A parking garage tracks which numbered spots are currently free. Spot numbers are positive integers,
and smaller numbers are closer to the entrance.

You are given:
- An integer n representing parking spots numbered from 1 to n
- An array occupied containing the spot numbers that are already taken when the day begins
- A sequence of operations

Each operation is one of two types:
- ["park"]: assign the smallest-numbered free spot and return its number.
  If no spot is free, return -1.
- ["leave", x]: mark spot x as free again.
  It is guaranteed that x is currently occupied when this operation appears.

Return an array containing the result of every "park" operation in order.

A very efficient strategy is to keep all currently free spots in a min-heap (priority queue),
so we can always remove the smallest available spot quickly.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Building the initial occupied lookup: O(occupied.Length)
    - Building the heap of free spots from 1..n: O(n log n) in this implementation
    - Each "park" operation: O(log n) because we remove the smallest free spot from the min-heap
    - Each "leave" operation: O(log n) because we add a newly free spot back into the min-heap
    - Total: O(n log n + operations.Length log n)

    Space Complexity:
    - O(n) for the occupied/free tracking
    - O(n) for the priority queue in the worst case
    - O(number of park operations) for the answer list
    */
    public int[] NextAvailableParkingSpot(int n, int[] occupied, string[][] operations)
    {
        // This boolean array lets us quickly know whether a spot is currently occupied.
        // Index i corresponds to parking spot i.
        // We use size n + 1 so that spot numbers 1..n can be used directly without subtracting 1.
        bool[] isOccupied = new bool[n + 1];

        // Mark all initially occupied spots.
        // This is necessary so that when we build the heap of free spots,
        // we only insert spots that are actually available at the start.
        foreach (int spot in occupied)
        {
            isOccupied[spot] = true;
        }

        // PriorityQueue<TElement, TPriority> in .NET is a min-heap when smaller priorities are better.
        // We store the spot number both as the element and as the priority.
        // Why?
        // Because we want the smallest-numbered free spot every time someone parks.
        var freeSpots = new PriorityQueue<int, int>();

        // Add every currently free spot into the min-heap.
        // After this loop, the heap contains exactly the spots that are available.
        for (int spot = 1; spot <= n; spot++)
        {
            if (!isOccupied[spot])
            {
                freeSpots.Enqueue(spot, spot);
            }
        }

        // We collect answers only for "park" operations, because the problem asks
        // us to return the result of every "park" in order.
        var results = new List<int>();

        // Process each operation one by one in the given order.
        foreach (var operation in operations)
        {
            // The first string tells us which kind of operation this is.
            string type = operation[0];

            if (type == "park")
            {
                // Current step:
                // We need to assign the smallest-numbered free spot.

                // Why the heap helps:
                // The min-heap always keeps the smallest free spot at the top,
                // so removing it gives us the correct answer efficiently.

                if (freeSpots.Count == 0)
                {
                    // No free spots exist right now.
                    // According to the problem, we must return -1.
                    results.Add(-1);
                }
                else
                {
                    // Remove the smallest available spot from the heap.
                    int assignedSpot = freeSpots.Dequeue();

                    // Mark it as occupied, because it has now been assigned.
                    isOccupied[assignedSpot] = true;

                    // Record the result for this "park" operation.
                    results.Add(assignedSpot);
                }
            }
            else if (type == "leave")
            {
                // Current step:
                // A car leaves spot x, so x becomes free again.

                // Parse the spot number from the operation.
                int spot = int.Parse(operation[1]);

                // Mark the spot as no longer occupied.
                isOccupied[spot] = false;

                // Add the freed spot back into the min-heap of free spots.
                // This is necessary so future "park" operations can choose it.
                // Because smaller spot numbers are better, we use the spot number as the priority.
                freeSpots.Enqueue(spot, spot);
            }
        }

        // Convert the collected results to an array, as required by the problem.
        return results.ToArray();
    }
}

// -------------------------
// Demo code
// -------------------------

var solution = new Solution();

// Example 1:
// n = 5
// occupied = [2, 4]
// operations = [["park"], ["park"], ["leave", "2"], ["park"], ["park"]]
// Expected output: [1, 3, 2, 5]
int n1 = 5;
int[] occupied1 = { 2, 4 };
string[][] operations1 =
{
    new[] { "park" },
    new[] { "park" },
    new[] { "leave", "2" },
    new[] { "park" },
    new[] { "park" }
};

int[] result1 = solution.NextAvailableParkingSpot(n1, occupied1, operations1);
Console.WriteLine("Example 1 Output: [" + string.Join(", ", result1) + "]");

// Example 2:
// n = 3
// occupied = [1, 2, 3]
// operations = [["park"], ["leave", "2"], ["park"], ["park"]]
// Expected output: [-1, 2, -1]
int n2 = 3;
int[] occupied2 = { 1, 2, 3 };
string[][] operations2 =
{
    new[] { "park" },
    new[] { "leave", "2" },
    new[] { "park" },
    new[] { "park" }
};

int[] result2 = solution.NextAvailableParkingSpot(n2, occupied2, operations2);
Console.WriteLine("Example 2 Output: [" + string.Join(", ", result2) + "]");