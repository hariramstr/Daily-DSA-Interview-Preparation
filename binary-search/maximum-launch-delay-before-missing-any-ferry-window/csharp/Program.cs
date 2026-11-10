/*
Title: Maximum Launch Delay Before Missing Any Ferry Window

Problem Description:
A research team must transport equipment through a sequence of islands. There are n ferry crossings that must be taken in order.
For crossing i, ferries depart every interval[i] minutes starting at time 0, and the team needs travel[i] minutes to complete
that crossing once they board.

The team may wait at any island for the next ferry, but they are allowed to delay their initial launch from the mainland by some
non-negative integer number of minutes D.

After that, all movement is deterministic:
- For each crossing, if they arrive at time t,
- they board the earliest ferry departing at or after t,
- then spend travel[i] minutes on that crossing.

You are also given a hard deadline T:
- the team must finish the final crossing no later than T.

Return the maximum integer launch delay D such that the team can still complete all crossings by time T.
If even launching at time 0 misses the deadline, return -1.

Key Observation:
This is a classic "binary search on the answer" problem.
If some delay D is feasible, then every smaller delay is also feasible.
That monotonic property allows us to binary search for the largest valid D.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= interval[i] <= 10^9
- 1 <= travel[i] <= 10^9
- 1 <= T <= 10^18
- 64-bit integers are required
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one chosen delay D: O(n), because we simulate all crossings once.
    - Binary search over D: O(log T), because the answer cannot exceed T in any useful case.
    - Total: O(n log T)

    Space Complexity:
    - O(1) extra space beyond the input arrays, because we only keep a few variables.
    */
    public long MaximumLaunchDelay(long[] interval, long[] travel, long T)
    {
        // Step 1:
        // Before doing any binary search, we must verify that the route is even possible
        // when the team starts immediately at time 0.
        //
        // Why this is necessary:
        // If delay 0 already misses the deadline, then every larger delay will also miss it,
        // because starting later can never help in this problem.
        // So in that case the answer is immediately -1.
        if (!CanFinish(interval, travel, T, 0))
        {
            return -1;
        }

        // Step 2:
        // We now know that at least one delay works: D = 0.
        // We want the maximum valid integer D.
        //
        // We binary search on D in the range [0, T].
        //
        // Why is T a safe upper bound?
        // Because delaying more than T minutes would already place the start time after the deadline,
        // and since every travel time is positive, such a delay cannot possibly finish by time T.
        long left = 0;
        long right = T;
        long answer = 0;

        // Step 3:
        // Standard binary search for the largest feasible value.
        //
        // Invariant:
        // - Any feasible delay can potentially become the answer.
        // - If mid is feasible, we try larger delays.
        // - If mid is not feasible, we try smaller delays.
        while (left <= right)
        {
            long mid = left + (right - left) / 2;

            // Check whether launching after exactly 'mid' minutes still allows finishing by T.
            if (CanFinish(interval, travel, T, mid))
            {
                // mid works, so record it as a candidate answer.
                answer = mid;

                // Because we want the MAXIMUM valid delay,
                // we continue searching to the right for a possibly larger valid delay.
                left = mid + 1;
            }
            else
            {
                // mid does not work, so every larger delay also does not work
                // due to monotonicity. Therefore we search the left half.
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool CanFinish(long[] interval, long[] travel, long T, long delay)
    {
        // This variable stores the current time as we simulate the full route.
        // We begin at the mainland after waiting 'delay' minutes.
        long currentTime = delay;

        // We process crossings in order because the problem states they must be taken sequentially.
        for (int i = 0; i < interval.Length; i++)
        {
            long ferryInterval = interval[i];
            long crossingTime = travel[i];

            // Step A: Round currentTime up to the next ferry departure time.
            //
            // Ferries for crossing i depart at:
            // 0, ferryInterval, 2*ferryInterval, 3*ferryInterval, ...
            //
            // If we arrive exactly at a departure time, we can board immediately.
            // Otherwise, we must wait until the next multiple of ferryInterval.
            //
            // The formula:
            // nextDeparture = ceil(currentTime / ferryInterval) * ferryInterval
            //
            // To avoid floating point arithmetic, we use integer math:
            // nextDeparture = ((currentTime + ferryInterval - 1) / ferryInterval) * ferryInterval
            //
            // This is a very common trick for "round up to the next multiple".
            long nextDeparture = ((currentTime + ferryInterval - 1) / ferryInterval) * ferryInterval;

            // Step B: After boarding, we spend crossingTime minutes traveling.
            currentTime = nextDeparture + crossingTime;

            // Step C:
            // Early stopping optimization.
            //
            // If at any point currentTime already exceeds T, then there is no need to continue.
            // The final finish time can only stay the same or increase from here, never decrease.
            if (currentTime > T)
            {
                return false;
            }
        }

        // If we finished all crossings and never exceeded T, then this delay is feasible.
        return currentTime <= T;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] interval1 = { 5, 7, 3 };
long[] travel1 = { 4, 2, 5 };
long T1 = 25;
long result1 = solution.MaximumLaunchDelay(interval1, travel1, T1);
Console.WriteLine(result1);

// Example 2
long[] interval2 = { 6, 10 };
long[] travel2 = { 7, 8 };
long T2 = 14;
long result2 = solution.MaximumLaunchDelay(interval2, travel2, T2);
Console.WriteLine(result2);