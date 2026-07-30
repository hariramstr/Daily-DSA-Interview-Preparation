/*
Title: Minimum Router Count for Deadline-Limited Packet Waves

Problem Description:
A data center receives packet waves in a fixed order. The i-th wave contains packets[i] packets and arrives at time i.
All packets from wave i must be fully processed no later than deadline[i], where deadline is a non-decreasing array
and deadline[i] >= i.

You may deploy k identical routers.
- Each router can process exactly 1 packet per unit of time.
- A router can work on at most one wave at a time.
- Processing is preemptive: a router may stop and later continue the same or another wave.
- Packets cannot be processed before their wave arrives.

We must find the minimum number of routers needed so that every wave can be completed by its deadline.

Key feasibility idea:
For any deadline time T, the total amount of work released up to time T must fit into the total processing
capacity available from time 0 through time T, which is k * (T + 1) if we treat each integer time slot
[0,1), [1,2), ..., [T,T+1) as one unit of processing opportunity.

Because arrivals happen at integer times and deadlines are integer times, and preemption is allowed,
the schedule is feasible if and only if for every prefix 0..i:
    sum(packets[0..i]) <= k * (deadline[i] + 1)

Why only prefixes?
- deadline is non-decreasing
- wave i arrives at time i, and deadline[i] >= i, so every wave in prefix 0..i has arrived by deadline[i]
- therefore by time deadline[i], all work of the prefix is available and must be finishable

This gives a monotone feasibility test:
- If k routers are enough, then any larger number is also enough
So we can binary search the minimum k.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check: O(n)
    - Binary search over answer range: O(log Answer)
    - Total: O(n log Answer)

    Space Complexity:
    - O(1) extra space beyond the input arrays

    Beginner-friendly summary:
    1. We guess a number of routers k.
    2. We check whether every prefix of waves can be completed by its deadline.
    3. If yes, try smaller k. If no, try larger k.
    4. Binary search finds the minimum feasible k.
    */
    public long MinimumRouterCount(long[] packets, long[] deadline)
    {
        int n = packets.Length;

        // -----------------------------
        // Step 1: Build a safe upper bound for binary search.
        // -----------------------------
        //
        // We need some "high" value that is definitely feasible.
        // A simple and safe choice:
        //   high = total packets
        //
        // Why is this always enough?
        // - If we have one router per packet in total, then in every single unit time slot
        //   we can process up to totalPackets packets.
        // - Since all deadlines satisfy deadline[i] >= i, every wave has at least one available
        //   time slot at or after arrival.
        // - This upper bound is not tight, but it is always safe and fits in 64-bit as promised.
        //
        // We also compute totalPackets while we are here.
        long totalPackets = 0;
        for (int i = 0; i < n; i++)
        {
            totalPackets += packets[i];
        }

        long low = 1;
        long high = Math.Max(1, totalPackets);

        // -----------------------------
        // Step 2: Binary search the minimum feasible number of routers.
        // -----------------------------
        //
        // Invariant:
        // - Any value < answer is infeasible
        // - Any value >= answer is feasible
        //
        // We repeatedly test the middle value.
        while (low < high)
        {
            long mid = low + (high - low) / 2;

            if (IsFeasible(packets, deadline, mid))
            {
                // mid works, so the answer is <= mid
                high = mid;
            }
            else
            {
                // mid does not work, so the answer is > mid
                low = mid + 1;
            }
        }

        return low;
    }

    private bool IsFeasible(long[] packets, long[] deadline, long routers)
    {
        // -----------------------------
        // Step 1: Walk through waves from left to right.
        // -----------------------------
        //
        // Because deadline is non-decreasing, checking prefixes in order is enough.
        // We maintain:
        //   prefixWork = total packets in waves 0..i
        //
        // By time deadline[i], all waves 0..i have already arrived:
        // - wave j arrives at time j
        // - j <= i
        // - deadline[i] >= i >= j
        //
        // So by time deadline[i], the scheduler has had exactly (deadline[i] + 1) unit time slots
        // available on each router.
        //
        // Total capacity by that time:
        //   routers * (deadline[i] + 1)
        //
        // Feasibility condition for this prefix:
        //   prefixWork <= routers * (deadline[i] + 1)
        //
        // If this fails for any prefix, then no schedule can exist.
        long prefixWork = 0;

        for (int i = 0; i < packets.Length; i++)
        {
            // Add current wave's work into the prefix total.
            prefixWork += packets[i];

            // Compute total processing capacity available up to and including deadline[i].
            //
            // We use checked-style reasoning mentally, but not the checked keyword,
            // because the problem guarantees the final answer fits in signed 64-bit.
            // Still, all values here remain within long for valid inputs.
            long capacity = routers * (deadline[i] + 1);

            // If required work exceeds available capacity, this router count is impossible.
            if (prefixWork > capacity)
            {
                return false;
            }
        }

        // Every prefix passed, so the schedule is feasible.
        return true;
    }
}

// Demo code
var solution = new Solution();

// Example 1
long[] packets1 = { 3, 2, 4 };
long[] deadline1 = { 2, 3, 5 };
long result1 = solution.MinimumRouterCount(packets1, deadline1);
Console.WriteLine(result1); // Expected: 2

// Example 2
long[] packets2 = { 5, 6, 4 };
long[] deadline2 = { 1, 2, 2 };
long result2 = solution.MinimumRouterCount(packets2, deadline2);
Console.WriteLine(result2); // Expected: 5

// Additional quick sanity checks
long[] packets3 = { 1 };
long[] deadline3 = { 0 };
Console.WriteLine(solution.MinimumRouterCount(packets3, deadline3)); // Expected: 1

long[] packets4 = { 10, 10 };
long[] deadline4 = { 0, 1 };
Console.WriteLine(solution.MinimumRouterCount(packets4, deadline4)); // Expected: 10