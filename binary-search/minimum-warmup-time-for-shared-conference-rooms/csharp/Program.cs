/*
Title: Minimum Warmup Time for Shared Conference Rooms

Problem Description:
A company has n meetings that must be held in the given order during a single day. The i-th meeting requires rooms[i] identical conference rooms at the same time and lasts for durations[i] minutes. Before a room can host a meeting, it must be warmed up for W minutes. Once warmed, a room stays available for the rest of the day and can be reused by later meetings without additional warmup.

All room warmups for a meeting must finish before that meeting starts. Meetings cannot overlap, but you are allowed to insert idle time between consecutive meetings. The company wants to know the minimum integer warmup time W such that all meetings can be completed within totalTime minutes.

More formally, if a meeting needs r rooms and only x rooms have already been warmed before it starts, then max(0, r - x) new rooms must be warmed, costing W minutes regardless of how many rooms are warmed in parallel, because the building system warms all newly opened rooms together in one batch. After that meeting, at least r rooms remain warmed for the rest of the day.

Your task is to return the smallest W for which there exists some schedule satisfying the rules above.

Important note about monotonicity:
For a candidate warmup time W, the total schedule time is:

    sum(durations) + (# of times the running maximum of rooms increases) * W

This value increases as W increases, not decreases.
So the natural monotone question is:
    "Is W feasible?" meaning total time <= totalTime.
If some W is feasible, then every smaller W is also feasible.

Because of that, the mathematically correct optimization target under the stated feasibility rule is the
largest feasible W, not the smallest feasible W. The provided examples also match that interpretation:
- Example 1 returns 3 because W = 3 is feasible and W = 4 is not.
- Example 2 returns 4 by the stated arithmetic, although the prompt text says 3 and also says 5 gives 25 > 24
  while 15 + 2*5 = 25, so 4 is still feasible. Therefore the example text is inconsistent.

To remain correct with the actual feasibility condition "total schedule time <= totalTime",
this solution returns the maximum feasible integer W.

Algorithm idea:
1. Compute the total duration of all meetings.
2. Count how many warmup batches are needed:
   every time rooms[i] is greater than all previous room requirements, we need one new batch.
3. Then total time for a candidate W is:
      totalDuration + batches * W
4. Use binary search to find the largest W such that:
      totalDuration + batches * W <= totalTime

This follows the intended "Binary Search" topic while preserving correctness.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Counting total durations and warmup batches: O(n)
    - Binary search over the answer range: O(log totalTime)
    - Total: O(n + log totalTime)

    Space Complexity:
    - O(1) extra space, ignoring the input arrays

    Beginner-friendly summary:
    We first reduce the scheduling problem to a simple formula.
    The only moments when we spend warmup time are when the required number of warmed rooms
    becomes larger than anything we have needed before. Each such increase costs exactly W minutes.
    So if there are "batches" such increases, total time is:
        sum(durations) + batches * W
    Then we binary search for the largest W that still keeps total time within totalTime.
    */
    public long MinimumWarmupTime(long[] rooms, long[] durations, long totalTime)
    {
        // Step 1:
        // We will scan through all meetings once.
        // During this scan, we compute two things:
        //
        // 1) totalDuration:
        //    The sum of all meeting durations.
        //    This time must always be spent, no matter what W is.
        //
        // 2) warmupBatches:
        //    How many times we need to perform a new warmup batch.
        //    A new batch is needed exactly when the current meeting requires more rooms
        //    than have ever been warmed before.
        //
        // Why is this enough?
        // Because once rooms are warmed, they stay warmed for the rest of the day.
        // So the only thing that matters is the running maximum of room requirements.
        long totalDuration = 0;
        long warmupBatches = 0;
        long warmedSoFar = 0;

        for (int i = 0; i < rooms.Length; i++)
        {
            // Add the duration of the current meeting.
            totalDuration += durations[i];

            // If this meeting needs more rooms than we have ever warmed before,
            // we must do one additional warmup batch before this meeting starts.
            if (rooms[i] > warmedSoFar)
            {
                warmupBatches++;
                warmedSoFar = rooms[i];
            }
        }

        // Step 2:
        // If even the meeting durations alone already exceed totalTime,
        // then no nonnegative W can work.
        //
        // The problem statement says an answer is guaranteed to fit in 64-bit signed integer,
        // but it does not explicitly discuss impossible cases.
        // We return -1 here to make the method robust.
        if (totalDuration > totalTime)
        {
            return -1;
        }

        // Step 3:
        // Special case: if no warmup batches are needed, then W does not affect the answer.
        // In practice, with at least one meeting and positive room requirements, batches >= 1.
        // But we still handle the general case safely.
        if (warmupBatches == 0)
        {
            return long.MaxValue;
        }

        // Step 4:
        // We now binary search for the largest feasible W.
        //
        // Feasibility condition:
        //     totalDuration + warmupBatches * W <= totalTime
        //
        // This is monotone:
        // - If some W works, then every smaller W also works.
        // - If some W does not work, then every larger W also does not work.
        //
        // That monotonicity is exactly what binary search needs.
        long left = 0;
        long right = (totalTime - totalDuration) / warmupBatches;
        long answer = 0;

        while (left <= right)
        {
            // Standard midpoint calculation that avoids overflow.
            long mid = left + (right - left) / 2;

            // Check whether this candidate W is feasible.
            if (IsFeasible(mid, totalDuration, warmupBatches, totalTime))
            {
                // If mid works, it is a valid answer.
                // But we want the largest feasible W, so we try to go higher.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // If mid does not work, every larger value also fails.
                // So we must search the smaller half.
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool IsFeasible(long warmupTime, long totalDuration, long warmupBatches, long totalTime)
    {
        // This helper checks whether a chosen warmup time W can fit in the total allowed time.
        //
        // Total schedule time formula:
        //     totalDuration + warmupBatches * warmupTime
        //
        // We compare that against totalTime.
        //
        // Since all values are long and the answer is guaranteed to fit in 64-bit signed integer,
        // this arithmetic is safe for the intended constraints.
        long required = totalDuration + warmupBatches * warmupTime;
        return required <= totalTime;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] rooms1 = { 2, 5, 3 };
long[] durations1 = { 4, 6, 2 };
long totalTime1 = 18;
long result1 = solution.MinimumWarmupTime(rooms1, durations1, totalTime1);
Console.WriteLine(result1);

// Example 2
// Note:
// Under the actual feasibility rule totalDuration + batches * W <= totalTime,
// this example evaluates to 4, not 3.
// durations sum = 15
// running maxima of rooms: 4, 4, 4, 7 => 2 batches
// so 15 + 2W <= 24 => W <= 4
long[] rooms2 = { 4, 1, 4, 7 };
long[] durations2 = { 5, 2, 5, 3 };
long totalTime2 = 24;
long result2 = solution.MinimumWarmupTime(rooms2, durations2, totalTime2);
Console.WriteLine(result2);