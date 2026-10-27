/*
Title: Maximum Launch Delay Before Missing Any Satellite Window

Problem Description:
A spacecraft must complete a fixed sequence of communication passes with ground stations.
There are n passes, and pass i must be started no later than windowClose[i].
The passes must be completed in order.

Traveling from pass i to pass i + 1 takes travel[i] minutes, and performing pass i itself
takes duration[i] minutes.

Before the first pass begins, the entire mission may be delayed by some integer number of
minutes D due to launch scheduling.

After that, the timeline is deterministic:
- pass 0 starts at time D
- it finishes after duration[0]
- then the craft spends travel[0] minutes to reach the next station
- then performs pass 1
- and so on

A delay D is feasible if every pass i starts at or before windowClose[i].

Task:
Compute the maximum feasible integer launch delay D.
If even D = 0 is impossible, return -1.

Key observation:
The start time of each pass is:
start[0] = D
start[1] = D + duration[0] + travel[0]
start[2] = D + duration[0] + travel[0] + duration[1] + travel[1]
...

So for each pass i:
D + offset[i] <= windowClose[i]
which means:
D <= windowClose[i] - offset[i]

Therefore the maximum feasible D is the minimum of all those upper bounds.
However, the problem specifically asks for a binary-search-based solution, so we implement:
1. A feasibility check for a candidate delay D
2. Binary search for the largest feasible D
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check: O(n)
    - Binary search over answer range: O(log M), where M is the search interval size
    - Total: O(n log M)

    Space Complexity:
    - O(1) extra space, ignoring input storage

    Beginner-friendly summary:
    We binary search the answer D.
    For each candidate D, we simulate only the start times of the passes.
    If every start time is <= its corresponding windowClose, then D is feasible.
    Because increasing D only makes all start times later, feasibility is monotonic:
    - if D works, then any smaller delay also works
    - if D fails, then any larger delay also fails
    That monotonic property is exactly what binary search needs.
    */
    public long MaximumLaunchDelay(long[] duration, long[] travel, long[] windowClose)
    {
        int n = duration.Length;

        // Step 1:
        // Before doing any binary search, we check whether delay D = 0 is feasible.
        //
        // Why this is necessary:
        // The problem explicitly says that if even D = 0 is impossible, we must return -1.
        // Also, binary search assumes there is at least one feasible value in the search range
        // if we want a meaningful "maximum feasible" result.
        if (!IsFeasible(0, duration, travel, windowClose))
        {
            return -1;
        }

        // Step 2:
        // We need an upper bound for binary search.
        //
        // A safe and natural upper bound is the largest windowClose value.
        // Why is this safe?
        // Because pass 0 starts exactly at time D, and it must satisfy:
        // D <= windowClose[0]
        //
        // So the true answer can never exceed windowClose[0].
        // Using windowClose[0] alone is already enough.
        //
        // We use windowClose[0] as the tightest simple upper bound.
        long left = 0;
        long right = windowClose[0];

        // Step 3:
        // Standard "find the maximum feasible value" binary search.
        //
        // Invariant:
        // - left is feasible
        // - values greater than the true answer are infeasible
        //
        // We bias mid upward using:
        // mid = left + (right - left + 1) / 2
        //
        // Why upward bias?
        // When searching for the maximum feasible value, if mid is feasible,
        // we move left = mid.
        // Upward bias prevents infinite loops when left and right are adjacent.
        while (left < right)
        {
            long mid = left + (right - left + 1) / 2;

            // Step 4:
            // Check whether this candidate delay is feasible.
            //
            // If feasible:
            //   We can try a larger delay, so move left up to mid.
            //
            // If not feasible:
            //   The answer must be smaller, so move right down to mid - 1.
            if (IsFeasible(mid, duration, travel, windowClose))
            {
                left = mid;
            }
            else
            {
                right = mid - 1;
            }
        }

        // At loop end, left == right and is the largest feasible delay.
        return left;
    }

    private bool IsFeasible(long delay, long[] duration, long[] travel, long[] windowClose)
    {
        // This variable stores the start time of the current pass.
        //
        // Initially, pass 0 starts exactly at the chosen launch delay.
        long currentStart = delay;

        // We iterate through passes in order because the mission order is fixed.
        for (int i = 0; i < duration.Length; i++)
        {
            // Step A:
            // Check whether the current pass starts before or at its closing time.
            //
            // This is the exact condition from the problem statement:
            // "pass i must be started no later than windowClose[i]"
            if (currentStart > windowClose[i])
            {
                return false;
            }

            // Step B:
            // If this is not the last pass, compute the start time of the next pass.
            //
            // Timeline logic:
            // - current pass starts at currentStart
            // - it takes duration[i] minutes to perform
            // - then travel[i] minutes to reach the next station
            // Therefore:
            // nextStart = currentStart + duration[i] + travel[i]
            //
            // We do not need to track finish times separately because only start times
            // are constrained by the windows.
            if (i < duration.Length - 1)
            {
                currentStart += duration[i] + travel[i];
            }
        }

        // If we never violated any window, the delay is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt:
// duration = [3, 2, 4]
// travel = [5, 1]
// windowClose = [4, 12, 20]
//
// Start times with delay D are:
// pass 0: D
// pass 1: D + 3 + 5 = D + 8
// pass 2: D + 3 + 5 + 2 + 1 = D + 11
//
// Constraints:
// D <= 4
// D + 8 <= 12  => D <= 4
// D + 11 <= 20 => D <= 9
//
// So maximum feasible D = 4.
long[] duration1 = { 3, 2, 4 };
long[] travel1 = { 5, 1 };
long[] windowClose1 = { 4, 12, 20 };
Console.WriteLine(solution.MaximumLaunchDelay(duration1, travel1, windowClose1)); // Expected: 4

// Corrected impossible example mentioned in the prompt notes:
// duration = [5, 3]
// travel = [4]
// windowClose = [3, 8]
//
// Start times with D = 0:
// pass 0: 0 <= 3
// pass 1: 0 + 5 + 4 = 9 > 8
//
// So even D = 0 is impossible, answer = -1.
long[] duration2 = { 5, 3 };
long[] travel2 = { 4 };
long[] windowClose2 = { 3, 8 };
Console.WriteLine(solution.MaximumLaunchDelay(duration2, travel2, windowClose2)); // Expected: -1

// Additional demo:
// duration = [0, 0, 0]
// travel = [0, 0]
// windowClose = [10, 10, 10]
//
// Every pass starts at D, so maximum D = 10.
long[] duration3 = { 0, 0, 0 };
long[] travel3 = { 0, 0 };
long[] windowClose3 = { 10, 10, 10 };
Console.WriteLine(solution.MaximumLaunchDelay(duration3, travel3, windowClose3)); // Expected: 10