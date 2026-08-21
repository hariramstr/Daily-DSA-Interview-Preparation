/*
Title: Minimum Cooldown for Battery Cell Assembly

Problem Description:
You are given an array stations where stations[i] is the number of battery cells that must be processed at assembly station i, in order from left to right.
A single robot arm starts at station 0 and must process all cells at every station in order.

Rules:
- Processing one cell takes 1 second.
- Moving from station i to station i + 1 takes 1 second.
- After the robot has processed x consecutive cells without resting, its motor temperature becomes x.
- To avoid overheating, the robot must rest before processing the next cell whenever its temperature would exceed a chosen cooldown limit C.
- A rest resets the consecutive processed-cell count back to 0 and takes exactly 1 second.
- The robot may rest at any time, including:
  - between two cells at the same station
  - immediately after moving

Goal:
Given a total time budget T, find the minimum integer cooldown limit C such that the robot can finish all work within at most T seconds.

If the work cannot be completed even with arbitrarily large cooldown (meaning processing + movement alone already exceeds T), return -1.

Key observation:
For a fixed cooldown C, the only extra time beyond processing and movement is the number of rests.
So the problem becomes:
- Can we finish with at most (T - baseTime) rests?
- The answer is monotonic in C:
  - larger C => never needs more rests
  - smaller C => may need more rests
This monotonicity allows binary search on C.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of stations.
    - Each feasibility check runs in O(n).
    - Binary search over C runs in O(log M), where M = max(stations[i]).
    - Total: O(n log M)

    Space Complexity:
    - O(1) extra space, ignoring input storage.

    Beginner-friendly summary:
    1. Compute the base time = total processed cells + total moves.
    2. If base time already exceeds T, return -1 immediately.
    3. Otherwise, binary search the smallest cooldown C that is feasible.
    4. To test a fixed C, compute the minimum number of rests needed.
       This is the tricky part because the "current consecutive processed count"
       carries across station boundaries and movement does NOT reset it.
    */
    public long MinimumCooldown(long[] stations, long T)
    {
        int n = stations.Length;

        // Step 1:
        // Compute the unavoidable base time:
        // - every cell must be processed once
        // - every move between adjacent stations must happen once
        long totalCells = 0;
        long maxStation = 0;

        foreach (long x in stations)
        {
            totalCells += x;
            if (x > maxStation) maxStation = x;
        }

        long moves = n - 1;
        long baseTime = totalCells + moves;

        // If even with infinite cooldown we cannot fit into T,
        // then no answer exists.
        if (baseTime > T)
            return -1;

        // If there is no need for any rest at all, the minimum cooldown
        // can still be smaller than totalCells because cooldown only limits
        // consecutive processed cells, not total processed cells.
        // The smallest possible cooldown is 1 because stations[i] >= 1.
        long left = 1;
        long right = maxStation; // cooldown larger than max station is never necessary

        while (left < right)
        {
            long mid = left + (right - left) / 2;

            if (CanFinishWithinTime(stations, T, baseTime, mid))
                right = mid;
            else
                left = mid + 1;
        }

        return left;
    }

    /*
    This method answers:
    "If the cooldown limit is C, can the robot finish within total time T?"

    Since baseTime is fixed, we only need to know:
    - what is the minimum number of rests required under cooldown C?

    If:
        baseTime + minimumRests <= T
    then C is feasible.

    The hard part is computing minimumRests correctly.

    Important modeling detail:
    - Let r be the current number of consecutive processed cells since the last rest.
    - At any moment, 0 <= r <= C.
    - Movement does NOT change r.
    - Before processing a new cell, if r == C, we must rest first.

    For one station with k cells and incoming remainder r:
    - We want to process all k cells with as few rests as possible.
    - Also, among all ways with minimum rests, we want the smallest possible outgoing remainder,
      because a smaller remainder is always at least as good for future stations.

    Key formula:
    - If k <= C - r:
        We can process the whole station without resting.
        rests added = 0
        outgoing remainder = r + k
    - Otherwise:
        First we fill the remaining capacity of the current run: (C - r) cells.
        Then there are rem = k - (C - r) cells left.
        From that point onward, every full block of C cells after the first requires a rest before it.
        The minimum additional rests is:
            1 + (rem - 1) / C
        Equivalently:
            (r + k - 1) / C
        and the outgoing remainder becomes:
            (r + k) % C, except if divisible by C then outgoing remainder is C, not 0,
            because ending exactly after processing C consecutive cells means the current run length is C.
            We do NOT need to rest at the end unless more work remains.
        A very convenient representation is:
            outgoing remainder = ((r + k - 1) % C) + 1

    Why this works:
    - The robot only rests when forced.
    - Resting earlier can never reduce the number of rests needed for the current station.
    - Among minimum-rest schedules, ending with the smallest possible remainder is best for the future.
    - The forced-greedy process therefore gives the global minimum number of rests.

    We also stop early if rests already exceed the allowed extra time.
    */
    private bool CanFinishWithinTime(long[] stations, long T, long baseTime, long C)
    {
        long allowedRests = T - baseTime;

        // currentRun = how many consecutive cells have been processed since the last rest
        long currentRun = 0;

        // total rests used so far
        long rests = 0;

        foreach (long k in stations)
        {
            // Case 1:
            // The entire station fits into the remaining capacity of the current run.
            // No rest is needed.
            if (k <= C - currentRun)
            {
                currentRun += k;
            }
            else
            {
                // Case 2:
                // We cannot finish this station in the current run.
                // The minimum number of rests needed while processing this station is:
                // floor((currentRun + k - 1) / C)
                //
                // Intuition:
                // - Think of starting with currentRun already filled in the current block.
                // - As we process k more cells, every time we need to start a new block,
                //   that requires one rest.
                long addedRests = (currentRun + k - 1) / C;
                rests += addedRests;

                // Early exit:
                // If we already used too many rests, no need to continue.
                if (rests > allowedRests)
                    return false;

                // Compute the outgoing currentRun after finishing this station.
                //
                // We want a value in the range [1, C] because this branch means
                // we processed at least one cell and ended inside some final block.
                //
                // Example:
                // C = 3, currentRun = 2, k = 4
                // total = 6
                // We end exactly at the end of a full block, so currentRun should be 3, not 0.
                currentRun = ((currentRun + k - 1) % C) + 1;
            }
        }

        return rests <= allowedRests;
    }
}

// Demo code
var solution = new Solution();

// Example 1
long[] stations1 = { 3, 2, 4 };
long T1 = 12;
long answer1 = solution.MinimumCooldown(stations1, T1);
Console.WriteLine(answer1); // Expected: 2

// Example 2
long[] stations2 = { 5, 1, 5 };
long T2 = 15;
long answer2 = solution.MinimumCooldown(stations2, T2);
Console.WriteLine(answer2); // Expected: 3

// Additional quick checks
long[] stations3 = { 1 };
long T3 = 1;
Console.WriteLine(solution.MinimumCooldown(stations3, T3)); // Expected: 1

long[] stations4 = { 10 };
long T4 = 9;
Console.WriteLine(solution.MinimumCooldown(stations4, T4)); // Expected: -1