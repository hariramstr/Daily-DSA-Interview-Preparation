/*
Title: Smallest Unlocked Seat for Returning Travelers
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
An airport lounge has numbered seats starting from 0. Travelers arrive and leave throughout the day. Whenever a traveler arrives, they must be assigned the smallest-numbered seat that is currently unoccupied. If no previously used seat is available, the lounge opens a new seat with the next unused number.

You are given two integer arrays, arrivals and departures, where arrivals[i] and departures[i] are the arrival and departure times of traveler i. All arrival times are distinct, but multiple travelers may leave at the same time. If a traveler leaves at time t, that seat becomes available immediately, so another traveler arriving at time t may use it.

Return the seat number assigned to traveler targetTraveler.

Your task is to simulate the seating process efficiently. A brute-force scan of all seats on every arrival will be too slow for large inputs. Think carefully about how to track both currently occupied seats and the pool of reusable seat numbers.

Constraints:
- 1 <= n == arrivals.length == departures.length <= 100000
- 1 <= arrivals[i] < departures[i] <= 1000000000
- All values in arrivals are distinct
- 0 <= targetTraveler < n

Example 1:
Input: arrivals = [1,4,2], departures = [5,6,3], targetTraveler = 1
Output: 1
Explanation: Traveler 0 arrives at 1 and takes seat 0. Traveler 2 arrives at 2 and takes seat 1. Traveler 2 leaves at 3, freeing seat 1. Traveler 1 arrives at 4 and takes the smallest available seat, which is 1.

Example 2:
Input: arrivals = [3,8,5,6], departures = [10,9,7,11], targetTraveler = 3
Output: 2
Explanation: Traveler 0 arrives at 3 and takes seat 0. Traveler 2 arrives at 5 and takes seat 1. Traveler 3 arrives at 6 and takes seat 2 because seats 0 and 1 are still occupied. Traveler 2 leaves at 7, but that happens after traveler 3 has already been seated, so the answer remains 2.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Sorting travelers by arrival time costs O(n log n)
    - Each traveler is inserted into and removed from a priority queue at most once
    - Each priority queue operation costs O(log n)
    - Total: O(n log n)

    Space Complexity:
    - We store the sorted traveler list and two priority queues
    - Total: O(n)
    */
    public int SmallestSeat(int[] arrivals, int[] departures, int targetTraveler)
    {
        int n = arrivals.Length;

        // We build a list of travelers where each entry stores:
        // 1. arrival time
        // 2. departure time
        // 3. original traveler index
        //
        // Why do we need this?
        // Because the seating process must happen in chronological order of arrivals,
        // not in the original array order.
        // The original arrays tell us each traveler's times, but they are not guaranteed
        // to already be sorted by arrival time.
        var travelers = new (int arrival, int departure, int index)[n];
        for (int i = 0; i < n; i++)
        {
            travelers[i] = (arrivals[i], departures[i], i);
        }

        // Sort all travelers by arrival time.
        //
        // This is essential because the lounge assigns seats exactly when each traveler arrives.
        // Since all arrival times are distinct, this order is unambiguous.
        Array.Sort(travelers, (a, b) => a.arrival.CompareTo(b.arrival));

        // This priority queue stores currently occupied seats.
        //
        // Each item is: seat number
        // Each priority is: departure time
        //
        // In other words, the traveler who leaves earliest is at the front.
        //
        // Why do we need this?
        // Before seating a newly arriving traveler, we must free every seat whose traveler
        // has already departed by that arrival time.
        // The fastest way to know which occupied seat becomes free next is to keep them
        // ordered by departure time.
        var occupiedSeats = new PriorityQueue<int, int>();

        // This priority queue stores seat numbers that are currently free and can be reused.
        //
        // Each item is: seat number
        // Each priority is: seat number
        //
        // Why do we need this?
        // The rule says we must always assign the smallest-numbered available seat.
        // A min-heap is perfect for repeatedly retrieving the smallest free seat efficiently.
        var availableSeats = new PriorityQueue<int, int>();

        // This tracks the next brand-new seat number that has never been used before.
        //
        // Example:
        // - If no free seat exists, assign nextUnusedSeat
        // - Then increment it for future use
        int nextUnusedSeat = 0;

        // Process travelers in arrival order.
        foreach (var traveler in travelers)
        {
            // STEP 1: Free every seat whose traveler has already left.
            //
            // Very important detail:
            // If someone leaves at time t, and another traveler arrives at time t,
            // that seat is available immediately.
            //
            // Therefore, we must free all occupied seats with departure time <= current arrival time.
            while (occupiedSeats.Count > 0 && occupiedSeats.PeekPriority() <= traveler.arrival)
            {
                // Remove the seat from the occupied structure because its traveler has left.
                int freedSeat = occupiedSeats.Dequeue();

                // Put that seat number into the pool of reusable seats.
                //
                // We use the seat number as both the item and the priority so that
                // the smallest seat number is always returned first.
                availableSeats.Enqueue(freedSeat, freedSeat);
            }

            // STEP 2: Choose the seat for the current arriving traveler.
            //
            // If there is at least one reusable seat, we must take the smallest one.
            // Otherwise, we open a brand-new seat with the next unused number.
            int assignedSeat;
            if (availableSeats.Count > 0)
            {
                assignedSeat = availableSeats.Dequeue();
            }
            else
            {
                assignedSeat = nextUnusedSeat;
                nextUnusedSeat++;
            }

            // STEP 3: If this is the target traveler, we can return immediately.
            //
            // Why is this safe?
            // Because the question asks only for the seat assigned to targetTraveler.
            // Once we assign that seat, later events cannot change that assignment.
            if (traveler.index == targetTraveler)
            {
                return assignedSeat;
            }

            // STEP 4: Mark this seat as occupied until the traveler's departure time.
            //
            // We insert:
            // - item: assigned seat number
            // - priority: departure time
            //
            // This ensures that when future travelers arrive, we can quickly free seats
            // in the correct order of earliest departure first.
            occupiedSeats.Enqueue(assignedSeat, traveler.departure);
        }

        // Given valid constraints, we should always return before reaching this line.
        // This is only here as a defensive fallback.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] arrivals1 = { 1, 4, 2 };
int[] departures1 = { 5, 6, 3 };
int targetTraveler1 = 1;
int result1 = solution.SmallestSeat(arrivals1, departures1, targetTraveler1);
Console.WriteLine(result1); // Expected: 1

// Example 2
int[] arrivals2 = { 3, 8, 5, 6 };
int[] departures2 = { 10, 9, 7, 11 };
int targetTraveler2 = 3;
int result2 = solution.SmallestSeat(arrivals2, departures2, targetTraveler2);
Console.WriteLine(result2); // Expected: 2