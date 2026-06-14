/*
Title: Maximum Uniform Delay for Train Departures
Difficulty: Hard
Topic: Binary Search

Problem Description:
A railway operator has scheduled n trains to depart in nondecreasing order of planned times,
given by an integer array departures where departures[i] is the planned departure minute of
the i-th train. Due to maintenance, each train may be delayed by any integer number of minutes
from 0 up to maxDelay, independently of the others. After delays are chosen, the actual
departure times must still be strictly increasing, and the gap between any two consecutive
actual departures must be at least gap minutes.

Your task is to compute the maximum integer value of gap such that it is possible to assign
delays to all trains while respecting the delay limit maxDelay.

Formally, choose integers actual[i] such that:
1. departures[i] <= actual[i] <= departures[i] + maxDelay
2. actual[i] < actual[i + 1] for all valid i
3. actual[i + 1] - actual[i] >= gap for all valid i

Return the largest feasible gap.

This is an optimization problem where the answer is not constructed directly. Instead, you
must determine whether a candidate gap is feasible and use that to search for the maximum
valid answer efficiently.

Constraints:
- 2 <= n <= 200000
- 0 <= departures[i] <= 10^14
- departures is sorted in nondecreasing order
- 0 <= maxDelay <= 10^14
- The answer fits in a 64-bit signed integer

Example 1:
Input: departures = [2, 4, 7], maxDelay = 3
Output: 4
Explanation: One valid assignment is actual = [2, 6, 10]. Each train is delayed by at most
3 minutes, and consecutive gaps are 4 and 4. A gap of 5 is impossible because after placing
the first train no later choices can keep every train within its allowed window.

Example 2:
Input: departures = [1, 1, 1, 1], maxDelay = 5
Output: 1
Explanation: The trains all start with the same planned time, but each can be shifted within
[1, 6]. A valid assignment is [1, 2, 3, 4], giving minimum consecutive gap 1. A gap of 2
would require times [1, 3, 5, 7], but 7 exceeds the allowed latest departure time of 6 for
the last train.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one candidate gap: O(n)
    - Binary search over the answer range: O(log R), where R is the size of the possible answer range
    - Total: O(n log R)

    Space Complexity:
    - O(1) extra space, ignoring the input array

    Beginner-friendly idea:
    We do not directly build the best answer.
    Instead, we ask:
    "If the required minimum gap were X, can we place all trains?"
    If yes, maybe we can do even better.
    If no, we must try a smaller gap.

    This "yes/no" structure is perfect for binary search.
    */
    public long MaximumUniformDelay(long[] departures, long maxDelay)
    {
        int n = departures.Length;

        // We binary search the answer "gap".
        //
        // Lower bound:
        // 0 is always a safe lower bound for the search space.
        // Even though the actual departures must be strictly increasing,
        // the problem asks for the maximum feasible minimum gap, and gap itself
        // can conceptually start from 0 in the search.
        //
        // Upper bound:
        // A very safe upper bound is:
        // (latest possible time of last train) - (earliest possible time of first train)
        // = (departures[n - 1] + maxDelay) - departures[0]
        //
        // No consecutive gap can exceed the total span between first and last actual times.
        long left = 0;
        long right = (departures[n - 1] + maxDelay) - departures[0];

        // This variable stores the best feasible answer found so far.
        long answer = 0;

        while (left <= right)
        {
            // Standard binary search midpoint.
            // We write it this way to avoid overflow:
            // left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether it is possible to assign actual departure times
            // so that every consecutive pair differs by at least "mid".
            if (CanMakeGap(departures, maxDelay, mid))
            {
                // If "mid" works, then it is a valid candidate answer.
                // We record it and try to see if an even larger gap is possible.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // If "mid" does not work, then any larger gap also cannot work.
                // So we shrink the search to smaller values.
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool CanMakeGap(long[] departures, long maxDelay, long gap)
    {
        // Greedy feasibility check:
        //
        // We place trains from left to right.
        // For each train, we choose the EARLIEST actual departure time that:
        // 1. Is within its allowed window [departures[i], departures[i] + maxDelay]
        // 2. Is at least "gap" after the previous actual departure
        //
        // Why choose the earliest possible time?
        // Because placing a train earlier leaves the most room for later trains.
        // This is the standard greedy strategy for interval scheduling / feasibility
        // under ordered constraints.
        //
        // If even the earliest valid choice is too late for the train's allowed window,
        // then this candidate gap is impossible.

        // For the first train, the best choice is simply its earliest allowed time.
        // Choosing it later would only make life harder for all later trains.
        long previousActual = departures[0];

        // Process each remaining train in order.
        for (int i = 1; i < departures.Length; i++)
        {
            // The current train cannot leave before its planned departure time.
            long earliestAllowed = departures[i];

            // It also cannot leave before "previousActual + gap",
            // otherwise the required minimum gap would be violated.
            long requiredByGap = previousActual + gap;

            // Therefore, the earliest actual time we can assign to this train is
            // the maximum of those two constraints.
            long currentActual = Math.Max(earliestAllowed, requiredByGap);

            // The latest time this train is allowed to depart is departures[i] + maxDelay.
            long latestAllowed = departures[i] + maxDelay;

            // If our earliest feasible choice already exceeds the latest allowed time,
            // then there is no valid placement for this train.
            if (currentActual > latestAllowed)
            {
                return false;
            }

            // Otherwise, we commit to this earliest feasible time.
            // This greedy choice is optimal for feasibility because it preserves
            // as much future flexibility as possible.
            previousActual = currentActual;
        }

        // If we successfully placed every train, then this gap is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] departures1 = { 2, 4, 7 };
long maxDelay1 = 3;
long result1 = solution.MaximumUniformDelay(departures1, maxDelay1);
Console.WriteLine(result1); // Expected: 4

// Example 2
long[] departures2 = { 1, 1, 1, 1 };
long maxDelay2 = 5;
long result2 = solution.MaximumUniformDelay(departures2, maxDelay2);
Console.WriteLine(result2); // Expected: 1

// Additional demo
long[] departures3 = { 0, 5, 10 };
long maxDelay3 = 0;
long result3 = solution.MaximumUniformDelay(departures3, maxDelay3);
Console.WriteLine(result3); // Expected: 5