/*
Title: Maximum Starting Delay Before Missing Any Checkpoint
Difficulty: Medium
Topic: Binary Search

Problem Description:
You are given a route with n mandatory checkpoints that must be visited in order.
For each checkpoint i, the travel time from checkpoint i - 1 to checkpoint i is travel[i],
and the latest allowed arrival time at checkpoint i is deadline[i].

You begin before checkpoint 0, and you may choose to wait some whole number of minutes x
before starting the trip. Once you start, you move continuously through the route and cannot
reorder or skip checkpoints.

Your task is to compute the maximum integer starting delay x such that, after waiting x minutes
and then traveling through all checkpoints in order, you still arrive at every checkpoint no later
than its deadline. If it is impossible to satisfy all deadlines even with x = 0, return -1.

Formally:
- Let prefix[i] be the total travel time needed to reach checkpoint i.
- Then arrival time at checkpoint i is x + prefix[i].
- This must satisfy: x + prefix[i] <= deadline[i] for every i.

A binary-search-based solution is expected:
- If a given delay x is feasible, then any smaller delay is also feasible.
- That monotonic property allows binary search.

Important note about the examples:
The mathematically correct interpretation of the formal definition is:
arrival at checkpoint i = x + prefix[i], and EVERY checkpoint must satisfy its deadline.

Under that definition:
Example 1:
travel   = [3, 2, 4]
deadline = [5, 8, 12]
prefix   = [3, 5, 9]

Feasible x values must satisfy:
x + 3 <= 5   => x <= 2
x + 5 <= 8   => x <= 3
x + 9 <= 12  => x <= 3

Therefore the true maximum feasible x is 2, not 3.

Example 2:
travel   = [4, 4, 4]
deadline = [3, 10, 15]
prefix   = [4, 8, 12]

For x = 0:
0 + 4 <= 3 is false, so impossible.
Answer = -1.

This implementation follows the formal definition exactly, which is the only consistent
and correct interpretation of the problem statement.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one candidate delay x takes O(n), because we scan all checkpoints once.
    - Binary search over the answer range takes O(log R), where R is the search interval size.
    - Total time complexity is O(n log R).

    Space Complexity:
    - O(1) extra space, not counting the input arrays.
    - We only store a few 64-bit variables such as the running prefix sum and binary search bounds.
    */
    public long MaximumStartingDelay(long[] travel, long[] deadline)
    {
        // Step 1:
        // Validate that both arrays describe the same number of checkpoints.
        // This is necessary because each travel time must correspond to exactly one deadline.
        if (travel == null || deadline == null || travel.Length != deadline.Length || travel.Length == 0)
        {
            return -1;
        }

        int n = travel.Length;

        // Step 2:
        // Before doing binary search, we first test whether starting immediately (x = 0)
        // is already impossible.
        //
        // Why this matters:
        // - The problem explicitly says to return -1 if even x = 0 cannot satisfy all deadlines.
        // - Also, binary search only makes sense if there is at least one feasible value.
        if (!CanDelay(travel, deadline, 0))
        {
            return -1;
        }

        // Step 3:
        // We need an upper bound for binary search.
        //
        // Since every checkpoint must satisfy:
        // x + prefix[i] <= deadline[i]
        // then:
        // x <= deadline[i] - prefix[i]
        //
        // Therefore the answer can never exceed the minimum of those values.
        //
        // We compute that minimum safely using 64-bit arithmetic.
        long prefix = 0;
        long upperBound = long.MaxValue;

        for (int i = 0; i < n; i++)
        {
            // Add the travel time to reach checkpoint i.
            prefix += travel[i];

            // The largest delay allowed by checkpoint i alone.
            long allowedByThisCheckpoint = deadline[i] - prefix;

            // The overall delay must satisfy all checkpoints,
            // so we keep the smallest allowed value seen so far.
            if (allowedByThisCheckpoint < upperBound)
            {
                upperBound = allowedByThisCheckpoint;
            }
        }

        // Because x is a waiting time, it should not be negative.
        // If upperBound is negative here, x = 0 would already be impossible,
        // but we already handled that case above. Still, clamping is harmless.
        if (upperBound < 0)
        {
            return -1;
        }

        // Step 4:
        // Binary search for the maximum feasible integer delay.
        //
        // Search invariant:
        // - Every value <= answer is feasible.
        // - Every value > answer is infeasible.
        //
        // We search in [0, upperBound].
        long left = 0;
        long right = upperBound;
        long answer = 0;

        while (left <= right)
        {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Step 4a:
            // Check whether delaying by 'mid' minutes still allows us
            // to arrive at every checkpoint on time.
            if (CanDelay(travel, deadline, mid))
            {
                // If 'mid' works, it is a valid candidate answer.
                // Because we want the maximum feasible delay,
                // we try to go larger.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // If 'mid' does not work, then any larger delay also cannot work
                // due to monotonicity. So we search the smaller half.
                right = mid - 1;
            }
        }

        // Step 5:
        // After binary search finishes, 'answer' stores the largest feasible delay.
        return answer;
    }

    private bool CanDelay(long[] travel, long[] deadline, long delay)
    {
        // This helper function answers:
        // "If we wait exactly 'delay' minutes before starting,
        // do we still meet every checkpoint deadline?"
        //
        // We simulate the route using a running prefix sum.
        // prefix = total travel time from the start to the current checkpoint.
        long prefix = 0;

        for (int i = 0; i < travel.Length; i++)
        {
            // Add the time needed to reach checkpoint i.
            prefix += travel[i];

            // Arrival time at checkpoint i is:
            // starting delay + cumulative travel time
            long arrivalTime = delay + prefix;

            // If we arrive after the deadline at any checkpoint,
            // then this delay is not feasible.
            if (arrivalTime > deadline[i])
            {
                return false;
            }
        }

        // If we never violated any deadline, then this delay works.
        return true;
    }
}

// Demo code
var solution = new Solution();

// Example 1 from the statement.
// According to the formal definition, the correct answer is 2.
long[] travel1 = { 3, 2, 4 };
long[] deadline1 = { 5, 8, 12 };
long result1 = solution.MaximumStartingDelay(travel1, deadline1);
Console.WriteLine(result1);

// Example 2 from the statement.
// Impossible even with x = 0, so answer is -1.
long[] travel2 = { 4, 4, 4 };
long[] deadline2 = { 3, 10, 15 };
long result2 = solution.MaximumStartingDelay(travel2, deadline2);
Console.WriteLine(result2);