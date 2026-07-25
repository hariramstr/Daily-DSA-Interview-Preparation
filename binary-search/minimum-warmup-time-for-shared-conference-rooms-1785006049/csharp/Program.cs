/*
Title: Minimum Warmup Time for Shared Conference Rooms

Problem Description:
A company has n meetings that must be held in the given order. The i-th meeting starts at time start[i] and ends at time end[i], where start and end are strictly increasing arrays and start[i] < end[i]. Before any meeting begins, a room assigned to that meeting must be warmed up for w minutes immediately before the meeting starts, meaning the room is occupied during the interval [start[i] - w, end[i]].

The company has exactly k identical conference rooms. Meetings cannot be reordered, split, or moved. Two meetings may use the same room only if their occupied intervals do not overlap. Your task is to find the minimum integer warmup time w such that all meetings can still be scheduled using at most k rooms.

Because larger warmup times make scheduling harder, the answer is the smallest w for which the required number of simultaneous rooms exceeds k, or equivalently the largest feasible warmup time depending on interpretation. In this problem, return the maximum integer warmup time w such that all meetings remain schedulable with k rooms.

You should design an efficient solution. A common approach is to binary search on w and check feasibility by sweeping through the meetings while tracking when rooms become free.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= k <= n
- 1 <= start[i] < end[i] <= 10^9
- start is strictly increasing
- end is strictly increasing
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one warmup value w: O(n)
      We use a two-pointer sweep because both start[] and end[] are strictly increasing.
    - Binary search over w: O(log R), where R is the search range of possible warmup values.
    - Total: O(n log R)

    Space Complexity:
    - O(1) extra space beyond the input arrays.

    Beginner-friendly intuition:
    1. For a fixed warmup time w, each meeting occupies [start[i] - w, end[i]].
    2. We need to know the maximum number of these occupied intervals overlapping at the same time.
    3. If that maximum overlap is <= k, then w is feasible.
    4. If w is feasible, then any smaller warmup is also feasible.
       This "yes/no" behavior is monotonic, so binary search works.
    */
    public int MaximumWarmupTime(int[] start, int[] end, int k)
    {
        int n = start.Length;

        // If we have at least as many rooms as meetings, then every meeting can be placed
        // in its own room forever. In that case there is no finite maximum warmup time.
        // The original examples and typical interview framing imply a finite answer exists.
        // To keep the method well-defined for all inputs, we return int.MaxValue here.
        if (k >= n)
        {
            return int.MaxValue;
        }

        // We binary search for the largest feasible warmup time.
        //
        // Why can we binary search?
        // - If a certain warmup time w is feasible, then any smaller warmup is also feasible,
        //   because shrinking intervals cannot create new overlaps.
        // - Therefore feasibility changes only once: true, true, true, ..., false, false, ...
        //
        // We need a high bound that is definitely NOT feasible.
        // A safe and simple choice is:
        //   high = start[n - 1] - start[0] + (end[n - 1] - end[0]) + 1
        // But we can use an even simpler large bound like 2_000_000_000 because times are <= 1e9.
        // To avoid overflow in calculations, we store bounds as long.
        long low = 0;
        long high = 2_000_000_000L;

        // Standard "find last true" binary search:
        // - low = feasible candidate
        // - high = infeasible candidate region endpoint
        //
        // We keep searching until low == high.
        while (low < high)
        {
            // Bias upward so that when low and high are adjacent,
            // mid becomes the upper one. This is the standard pattern
            // for finding the maximum feasible value.
            long mid = low + (high - low + 1) / 2;

            if (IsFeasible(start, end, k, mid))
            {
                // mid works, so try to go larger
                low = mid;
            }
            else
            {
                // mid does not work, so the answer must be smaller
                high = mid - 1;
            }
        }

        return (int)low;
    }

    private bool IsFeasible(int[] start, int[] end, int k, long w)
    {
        int n = start.Length;

        // We want to compute the maximum number of occupied intervals overlapping at any time.
        //
        // Each meeting occupies:
        //   [start[i] - w, end[i]]
        //
        // Two intervals overlap if the earlier one has not ended before the later one begins.
        // Since the problem statement uses occupied intervals and the examples show that
        // touching at the endpoint is considered non-overlapping only when one ends before
        // the next occupied interval begins, we can model room reuse as:
        // - A room becomes reusable only when previous end < next occupied start
        // - If previous end == next occupied start, they still overlap at that instant
        //
        // Therefore, before processing meeting i, we can free all meetings with:
        //   end[finished] < start[i] - w
        //
        // Because:
        // - start[] is strictly increasing
        // - end[] is strictly increasing
        // we can sweep with two pointers in O(n), without a heap.
        //
        // active = number of meetings currently occupying rooms
        // finished = index of the earliest-ending meeting not yet removed from active
        int active = 0;
        int finished = 0;

        for (int i = 0; i < n; i++)
        {
            long occupiedStart = (long)start[i] - w;

            // Step 1: Remove all meetings that have definitely finished before this meeting's
            // occupied interval begins.
            //
            // Why strict '<' and not '<='?
            // Because intervals are closed in the statement/examples:
            // [start[i] - w, end[i]]
            // If one ends exactly when another begins, they still share that boundary point,
            // so they overlap and cannot use the same room.
            while (finished < i && end[finished] < occupiedStart)
            {
                active--;
                finished++;
            }

            // Step 2: Add the current meeting as active, because its occupied interval starts now.
            active++;

            // Step 3: If active rooms exceed k, this warmup time is not feasible.
            if (active > k)
            {
                return false;
            }
        }

        // If we never exceeded k active intervals, then all meetings fit in k rooms.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] start1 = { 10, 20, 35 };
int[] end1 = { 15, 30, 40 };
int k1 = 2;
int result1 = solution.MaximumWarmupTime(start1, end1, k1);
Console.WriteLine(result1); // Expected: 10

// Example 2
int[] start2 = { 5, 8, 14, 20 };
int[] end2 = { 6, 12, 18, 22 };
int k2 = 2;
int result2 = solution.MaximumWarmupTime(start2, end2, k2);
Console.WriteLine(result2); // Expected: 6