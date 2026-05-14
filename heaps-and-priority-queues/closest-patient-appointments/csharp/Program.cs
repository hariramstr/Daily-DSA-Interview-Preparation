/*
 * Title: Closest Patient Appointments
 * Difficulty: Easy
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * A hospital scheduling system receives a list of patient appointment times
 * (in minutes from midnight) and a query time `t`. Your task is to find the
 * `k` appointment times closest to the query time `t`.
 *
 * If two appointments are equally close to `t`, prefer the one with the
 * smaller time value. Return the result as a list sorted in ascending order.
 *
 * Example 1:
 *   Input:  appointments = [30, 120, 200, 450, 800], t = 150, k = 2
 *   Output: [120, 200]
 *   Explanation: Distances = [120, 30, 50, 300, 650].
 *                Two smallest distances: 30 (time 120) and 50 (time 200).
 *
 * Example 2:
 *   Input:  appointments = [60, 180, 300, 420], t = 240, k = 3
 *   Output: [60, 180, 300]
 *   Explanation: Distances = [180, 60, 60, 180].
 *                Three closest: 60 (time 180), 60 (time 300), 180 (time 60).
 *                (60 is preferred over 420 because 60 < 420 when distances tie)
 *                Sorted result: [60, 180, 300].
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class — contains the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Returns the k appointment times closest to query time t,
    /// sorted in ascending order.
    ///
    /// Strategy — Max-Heap of size k:
    ///   We maintain a max-heap (priority queue) that always holds the k
    ///   "best" (closest) appointments seen so far.  The heap is ordered so
    ///   that the WORST element among the current k sits at the top, making
    ///   it easy to evict it when a better candidate arrives.
    ///
    ///   Comparison rule (mirrors the tie-breaking requirement):
    ///     • Primary  : larger distance  → worse  (evict first)
    ///     • Secondary: larger time value → worse  (evict first when distances tie)
    ///
    /// Time Complexity : O(n log k)
    ///   - We process each of the n appointments once.
    ///   - Each heap operation (push/pop) costs O(log k).
    ///
    /// Space Complexity: O(k)
    ///   - The heap holds at most k + 1 elements at any moment.
    /// </summary>
    public List<int> FindClosestAppointments(int[] appointments, int t, int k)
    {
        // ── Step 1: Define the max-heap comparison ────────────────────────────
        //
        // C#'s PriorityQueue<TElement, TPriority> is a MIN-heap by default.
        // To simulate a MAX-heap we negate the priority values, or we supply
        // a custom IComparer.  Here we use a custom Comparer so the logic
        // stays readable.
        //
        // The comparer receives two (distance, appointmentTime) pairs.
        // We want the element with the LARGEST distance (or largest time on
        // a tie) to be dequeued first — that is the "worst" element we want
        // to evict when a better one arrives.
        //
        // Because PriorityQueue dequeues the element with the SMALLEST
        // priority according to the comparer, we invert the natural order:
        //   return negative  → first argument is "smaller priority" → dequeued first
        //   return positive  → second argument is "smaller priority" → dequeued first
        //
        // In plain English: we tell the queue "treat a LARGER distance as a
        // SMALLER priority number" so it gets popped first.

        var comparer = Comparer<(int dist, int time)>.Create((a, b) =>
        {
            // Compare by distance first (descending → larger dist = higher priority to evict)
            if (a.dist != b.dist)
                return b.dist.CompareTo(a.dist); // b > a means b is "smaller" → b dequeued first

            // Tie-break by appointment time (descending → larger time = higher priority to evict)
            return b.time.CompareTo(a.time);
        });

        // ── Step 2: Create the max-heap ───────────────────────────────────────
        //
        // PriorityQueue<TElement, TPriority>:
        //   TElement  = int  (the appointment time we want to keep)
        //   TPriority = (int dist, int time)  (used for ordering)
        //
        // We store the appointment time as both the element AND part of the
        // priority tuple so the comparer has all the information it needs.

        var maxHeap = new PriorityQueue<int, (int dist, int time)>(comparer);

        // ── Step 3: Process every appointment ────────────────────────────────
        //
        // For each appointment we:
        //   a) Compute its distance from t.
        //   b) Push it onto the heap.
        //   c) If the heap now has more than k elements, pop the worst one.
        //
        // After processing all appointments the heap contains exactly the k
        // closest ones.

        foreach (int appt in appointments)
        {
            // 3a. Compute absolute distance from the query time.
            //     We use Math.Abs so negative differences are handled correctly.
            int distance = Math.Abs(appt - t);

            // 3b. Push the appointment onto the max-heap.
            //     The priority tuple (distance, appt) drives the ordering.
            maxHeap.Enqueue(appt, (distance, appt));

            // 3c. If we now hold more than k elements, remove the worst one.
            //     The "worst" element (largest distance, or largest time on tie)
            //     sits at the top of our max-heap and is dequeued first.
            if (maxHeap.Count > k)
            {
                maxHeap.Dequeue(); // evict the appointment farthest from t
            }
        }

        // ── Step 4: Collect the k winners ────────────────────────────────────
        //
        // Drain the heap into a list.  The heap does NOT guarantee sorted
        // output, so we sort the list afterwards.

        var result = new List<int>(k);
        while (maxHeap.Count > 0)
        {
            result.Add(maxHeap.Dequeue());
        }

        // ── Step 5: Sort ascending ────────────────────────────────────────────
        //
        // The problem asks for the result sorted in ascending order by
        // appointment time (not by distance).

        result.Sort();

        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / verification code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// appointments = [30, 120, 200, 450, 800], t = 150, k = 2
// Distances    = [120,  30,  50, 300, 650]
// Two smallest distances: 30 (appt 120) and 50 (appt 200)
// Expected output: [120, 200]

int[] appointments1 = [30, 120, 200, 450, 800];
int t1 = 150, k1 = 2;
List<int> result1 = solution.FindClosestAppointments(appointments1, t1, k1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input : appointments = [{string.Join(", ", appointments1)}], t = {t1}, k = {k1}");
Console.WriteLine($"  Output: [{string.Join(", ", result1)}]");
Console.WriteLine($"  Expected: [120, 200]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// appointments = [60, 180, 300, 420], t = 240, k = 3
// Distances    = [180,  60,  60, 180]
// Three closest:
//   dist 60  → appt 180
//   dist 60  → appt 300
//   dist 180 → appt 60  (prefer 60 over 420 because 60 < 420)
// Expected output: [60, 180, 300]

int[] appointments2 = [60, 180, 300, 420];
int t2 = 240, k2 = 3;
List<int> result2 = solution.FindClosestAppointments(appointments2, t2, k2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input : appointments = [{string.Join(", ", appointments2)}], t = {t2}, k = {k2}");
Console.WriteLine($"  Output: [{string.Join(", ", result2)}]");
Console.WriteLine($"  Expected: [60, 180, 300]");
Console.WriteLine();

// ── Extra edge-case: k equals the full list ───────────────────────────────────
int[] appointments3 = [0, 720, 1440];
int t3 = 360, k3 = 3;
List<int> result3 = solution.FindClosestAppointments(appointments3, t3, k3);
Console.WriteLine("Edge case (k = n):");
Console.WriteLine($"  Input : appointments = [{string.Join(", ", appointments3)}], t = {t3}, k = {k3}");
Console.WriteLine($"  Output: [{string.Join(", ", result3)}]");
Console.WriteLine($"  Expected: [0, 720, 1440]");
Console.WriteLine();

// ── Extra edge-case: single appointment ──────────────────────────────────────
int[] appointments4 = [500];
int t4 = 100, k4 = 1;
List<int> result4 = solution.FindClosestAppointments(appointments4, t4, k4);
Console.WriteLine("Edge case (single appointment):");
Console.WriteLine($"  Input : appointments = [{string.Join(", ", appointments4)}], t = {t4}, k = {k4}");
Console.WriteLine($"  Output: [{string.Join(", ", result4)}]");
Console.WriteLine($"  Expected: [500]");