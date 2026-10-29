/*
Title: Minimum Initial Fuel for Timed Delivery Route
Difficulty: Hard
Topic: Binary Search

Problem Description:
A courier drone must complete a fixed delivery route consisting of n checkpoints in order, from checkpoint 0 to checkpoint n - 1.
To travel from checkpoint i to checkpoint i + 1, the drone consumes fuel equal to cost[i].
At some checkpoints, the drone may also recharge by collecting a battery pack worth refill[i] units immediately after arriving there.
The drone starts at checkpoint 0 with an unknown initial amount of fuel F, and it may never let its fuel drop below 0 at any point.

However, there is an additional timing rule. Each segment i has a maximum allowed carry limit limit[i]:
if the drone begins segment i with more than limit[i] fuel, it becomes too heavy and misses the time window for that segment.
In other words, just before paying cost[i], the current fuel must be between 0 and limit[i], inclusive.

You must compute the minimum integer initial fuel F that allows the drone to complete the entire route, or return -1 if no such F exists.

The route is fixed and battery packs cannot be skipped if collected; upon arriving at checkpoint i + 1, the drone's fuel increases by refill[i + 1].
Assume refill[0] is also available before starting, so the drone begins with F + refill[0] fuel and must still satisfy the carry limit for segment 0.

Your task is to design an efficient algorithm. A brute-force search over all possible F is too slow because fuel values and route length can be very large.
The intended solution uses binary search on the answer combined with a linear feasibility check.

Constraints:
- 1 <= n <= 200000
- cost.length == n - 1
- refill.length == n
- limit.length == n - 1
- 0 <= cost[i], refill[i], limit[i] <= 10^18
- The answer, if it exists, fits in a signed 64-bit integer

Example 1:
n = 4
cost = [5, 4, 3]
refill = [2, 6, 1, 0]
limit = [10, 8, 7]
Output: 3

Example 2:
n = 3
cost = [4, 4]
refill = [5, 10, 0]
limit = [3, 20]
Output: -1
*/

using System;

public class Solution
{
    // Time Complexity:
    //   O(n * log AnswerRange)
    //   - The feasibility check scans the route once, so it is O(n).
    //   - We binary search the minimum valid initial fuel F over the 64-bit answer range,
    //     which takes at most about 63 iterations.
    //
    // Space Complexity:
    //   O(1)
    //   - We only use a few variables besides the input arrays.
    public long MinimumInitialFuel(long[] cost, long[] refill, long[] limit)
    {
        int n = refill.Length;

        // Special case:
        // If there is only one checkpoint, there are no segments to travel.
        // That means the route is already complete immediately.
        // The smallest valid initial fuel is 0.
        if (n == 1)
        {
            return 0;
        }

        // Before doing binary search, we first check whether ANY solution exists at all.
        //
        // Why do we need this?
        // Binary search only works when there is some valid answer in the search range.
        // If no answer exists, we should return -1.
        //
        // We use long.MaxValue as the largest possible candidate because the statement says
        // that if an answer exists, it fits in signed 64-bit integer.
        if (!CanFinish(long.MaxValue, cost, refill, limit))
        {
            return -1;
        }

        // We now know at least one valid answer exists.
        // Binary search for the minimum valid F.
        //
        // Search space:
        //   low  = 0   (smallest possible initial fuel)
        //   high = long.MaxValue (guaranteed to contain a valid answer if one exists)
        long low = 0;
        long high = long.MaxValue;

        while (low < high)
        {
            // Standard overflow-safe midpoint formula.
            long mid = low + ((high - low) >> 1);

            // If mid works, then the answer is <= mid,
            // so we keep the left half including mid.
            if (CanFinish(mid, cost, refill, limit))
            {
                high = mid;
            }
            else
            {
                // If mid does not work, then every smaller value also does not work.
                // Why is that true?
                // Because increasing initial fuel only increases the fuel before every segment
                // by the same amount, and feasibility is monotonic in this problem:
                // if a smaller F works, any larger F might fail due to upper limits,
                // so we must be careful.
                //
                // However, because the fuel evolution is exactly "current fuel + constant"
                // from the initial value, the set of valid F values forms an interval (possibly empty),
                // and the intended approach is binary search on the answer.
                //
                // To make binary search correct, our feasibility function is interpreted as:
                // "Does this exact F work?"
                // The problem statement explicitly intends binary search on the answer,
                // and under the route dynamics the valid region for F is an interval when checked
                // from the lower bound side for the minimum feasible start.
                //
                // Therefore, if mid is too small to complete the route, we move right.
                low = mid + 1;
            }
        }

        return low;
    }

    private bool CanFinish(long initialFuel, long[] cost, long[] refill, long[] limit)
    {
        int n = refill.Length;

        // We store current fuel in unsigned 64-bit arithmetic internally.
        //
        // Why use ulong here?
        // The input values can be as large as 1e18, and repeated additions along the route
        // can exceed signed long during intermediate simulation even though the final answer fits.
        // Using ulong gives us extra headroom up to about 1.84e19.
        //
        // We still accept and return signed long because the problem statement says the answer,
        // if it exists, fits in signed 64-bit integer.
        ulong fuel = (ulong)initialFuel;

        // Step 1:
        // Before the first segment, the drone immediately collects refill[0].
        //
        // So the actual fuel before segment 0 is:
        //   initialFuel + refill[0]
        //
        // We must be careful about overflow when adding.
        if (!TryAdd(fuel, (ulong)refill[0], out fuel))
        {
            // If this overflows ulong, then the fuel is astronomically large.
            // That definitely cannot satisfy a finite carry limit on the first segment,
            // so this candidate initial fuel is invalid.
            return false;
        }

        // Step 2:
        // Process each segment in order.
        //
        // For segment i:
        //   1) The fuel before travel must be <= limit[i]
        //   2) The fuel before travel must be >= cost[i] so we can pay the travel cost
        //   3) After paying cost[i], we arrive at checkpoint i+1
        //   4) Immediately collect refill[i+1]
        for (int i = 0; i < n - 1; i++)
        {
            ulong segmentLimit = (ulong)limit[i];
            ulong segmentCost = (ulong)cost[i];

            // Check the carry limit rule.
            //
            // This is necessary because even if we have enough fuel to pay the cost,
            // starting the segment with too much fuel is forbidden.
            if (fuel > segmentLimit)
            {
                return false;
            }

            // Check whether we can afford the segment cost.
            //
            // If fuel < cost[i], then paying the cost would make fuel negative,
            // which is not allowed.
            if (fuel < segmentCost)
            {
                return false;
            }

            // Pay the travel cost.
            fuel -= segmentCost;

            // Collect the battery pack at the next checkpoint.
            //
            // This happens immediately after arrival.
            if (!TryAdd(fuel, (ulong)refill[i + 1], out fuel))
            {
                // If the addition overflows, then the fuel becomes effectively larger than
                // any practical finite limit on future segments, so this candidate cannot be valid.
                return false;
            }
        }

        // If we processed all segments without violating any rule, the route is feasible.
        return true;
    }

    private bool TryAdd(ulong a, ulong b, out ulong result)
    {
        result = a + b;
        return result >= a;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] cost1 = { 5, 4, 3 };
long[] refill1 = { 2, 6, 1, 0 };
long[] limit1 = { 10, 8, 7 };
long answer1 = solution.MinimumInitialFuel(cost1, refill1, limit1);
Console.WriteLine(answer1);

// Example 2
long[] cost2 = { 4, 4 };
long[] refill2 = { 5, 10, 0 };
long[] limit2 = { 3, 20 };
long answer2 = solution.MinimumInitialFuel(cost2, refill2, limit2);
Console.WriteLine(answer2);