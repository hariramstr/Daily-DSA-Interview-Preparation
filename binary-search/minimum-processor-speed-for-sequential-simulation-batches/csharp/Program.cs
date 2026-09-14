/*
Title: Minimum Processor Speed for Sequential Simulation Batches

Problem Description:
A research platform must run n simulation batches in the given order. The i-th batch contains workloads[i] compute units and must be fully processed no later than deadlines[i] minutes from time 0. All batches are executed on a single processor, one after another, without reordering and without preemption. If the processor speed is S compute units per minute, then batch i takes ceil(workloads[i] / S) minutes to finish.

Your task is to find the minimum positive integer processor speed S such that every batch finishes by its deadline. If no speed can satisfy the deadlines, return -1.

More formally, let finish[i] be the cumulative running time of batches 0 through i. A speed S is feasible if for every i, finish[i] <= deadlines[i]. You must compute the smallest feasible S.

This problem is designed for a binary-search-on-answer approach. The challenge is to test feasibility efficiently while handling very large workloads and deadlines. Be careful with integer overflow when summing running times and computing ceil division.

Constraints:
- 1 <= n <= 200000
- 1 <= workloads[i] <= 10^12
- 1 <= deadlines[i] <= 10^18
- deadlines is not guaranteed to be sorted, but it represents the required completion time of each prefix in the given order
- Return -1 if even an arbitrarily large speed cannot make all batches meet their deadlines

Example 1:
Input: workloads = [7, 11, 5], deadlines = [4, 8, 10]
Output: 3

Explanation:
At speed 4, batch times are [2, 3, 2], so cumulative finish times are [2, 5, 7], all within [4, 8, 10].
At speed 3, batch times are [3, 4, 2], cumulative finish times [3, 7, 9], which also meet all deadlines.
At speed 2, batch times are [4, 6, 3], cumulative [4, 10, 13], violating deadline 8.
Therefore the minimum feasible speed is 3.

Example 2:
Input: workloads = [9, 9, 9], deadlines = [0, 5, 8]
Output: -1

Explanation:
The first batch needs at least 1 minute for any finite positive integer speed, but its deadline is 0, so no valid speed exists.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log M), where:
      n = number of batches
      M = max(workloads)
    Why:
    - Each feasibility check scans the arrays once => O(n)
    - Binary search over the answer range [1, max(workloads)] => O(log M)

    Space Complexity:
    - O(1) extra space
    Why:
    - We only use a few variables and do not allocate extra arrays proportional to input size.
    */
    public long MinimumProcessorSpeed(long[] workloads, long[] deadlines)
    {
        // Step 1:
        // Validate basic input shape.
        // This is not strictly required by the problem statement if inputs are always valid,
        // but it makes the method safer and easier to understand.
        if (workloads == null || deadlines == null || workloads.Length != deadlines.Length || workloads.Length == 0)
        {
            return -1;
        }

        int n = workloads.Length;

        // Step 2:
        // Before doing binary search, check whether the problem is impossible even with
        // an "arbitrarily large" speed.
        //
        // Why does this help?
        // If speed becomes extremely large, each batch still takes at least 1 minute,
        // because ceil(workload / S) is never 0 for positive workload and positive speed.
        //
        // So the absolute best possible cumulative finish time for batch i is (i + 1).
        // If any deadline[i] < i + 1, then no finite speed can ever satisfy that prefix.
        //
        // This is a very important impossibility check.
        for (int i = 0; i < n; i++)
        {
            long minimumPossibleFinishForPrefix = i + 1L;
            if (deadlines[i] < minimumPossibleFinishForPrefix)
            {
                return -1;
            }
        }

        // Step 3:
        // Establish the binary search range for the processor speed.
        //
        // Lower bound:
        // - The speed must be at least 1 because the problem asks for a positive integer speed.
        //
        // Upper bound:
        // - max(workloads) is always sufficient whenever the instance is feasible.
        //   Why?
        //   At speed = max(workloads), every batch takes exactly 1 minute:
        //       ceil(workloads[i] / max(workloads)) <= 1
        //   Since workloads[i] >= 1, the time is exactly 1.
        //   Therefore cumulative finish times become [1, 2, 3, ..., n].
        //   We already checked that deadlines[i] >= i + 1 for all i in the impossibility test.
        //   So this speed is guaranteed feasible if any speed is feasible.
        //
        // This gives us a tight and safe search interval.
        long left = 1;
        long right = 1;

        for (int i = 0; i < n; i++)
        {
            if (workloads[i] > right)
            {
                right = workloads[i];
            }
        }

        long answer = -1;

        // Step 4:
        // Standard binary search on the answer.
        //
        // We are searching for the smallest speed S such that:
        //   Feasible(S) == true
        //
        // Key monotonic property:
        // - If a speed S is feasible, then any larger speed is also feasible,
        //   because larger speed can only reduce or keep the same processing times.
        //
        // This monotonicity is exactly what makes binary search valid here.
        while (left <= right)
        {
            // Use this midpoint formula to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Step 4a:
            // Check whether the current speed "mid" can satisfy all deadlines.
            if (IsFeasible(workloads, deadlines, mid))
            {
                // If feasible, record it as a candidate answer.
                answer = mid;

                // But we are looking for the MINIMUM feasible speed,
                // so continue searching on the left half.
                right = mid - 1;
            }
            else
            {
                // If not feasible, this speed is too slow.
                // We must search for a larger speed.
                left = mid + 1;
            }
        }

        // Step 5:
        // Return the smallest feasible speed found.
        return answer;
    }

    private bool IsFeasible(long[] workloads, long[] deadlines, long speed)
    {
        // This variable stores the cumulative finish time of all batches processed so far.
        long cumulativeTime = 0;

        // We process batches in the given order exactly as required by the problem.
        for (int i = 0; i < workloads.Length; i++)
        {
            // Compute the time for the current batch at the given speed.
            //
            // We need:
            //   ceil(workloads[i] / speed)
            //
            // For positive integers, a standard safe formula is:
            //   (a + b - 1) / b
            //
            // Here:
            //   a = workloads[i]
            //   b = speed
            //
            // Since workloads[i] <= 1e12 and speed <= 1e12 in our search range,
            // a + b - 1 is at most about 2e12, which safely fits in long.
            long batchTime = (workloads[i] + speed - 1) / speed;

            // Add this batch's time to the cumulative finish time.
            //
            // Why cumulative?
            // Because the processor is single-threaded and non-preemptive:
            // each batch starts only after all previous batches finish.
            cumulativeTime += batchTime;

            // Immediately check the deadline for this prefix.
            //
            // This early exit is important:
            // - It avoids unnecessary work once we already know the speed fails.
            // - It keeps the feasibility check efficient in practice.
            if (cumulativeTime > deadlines[i])
            {
                return false;
            }
        }

        // If every prefix met its deadline, the speed is feasible.
        return true;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1
long[] workloads1 = { 7, 11, 5 };
long[] deadlines1 = { 4, 8, 10 };
long result1 = solution.MinimumProcessorSpeed(workloads1, deadlines1);
Console.WriteLine(result1); // Expected: 3

// Example 2
long[] workloads2 = { 9, 9, 9 };
long[] deadlines2 = { 0, 5, 8 };
long result2 = solution.MinimumProcessorSpeed(workloads2, deadlines2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity check
long[] workloads3 = { 1, 1, 1, 1 };
long[] deadlines3 = { 1, 2, 3, 4 };
long result3 = solution.MinimumProcessorSpeed(workloads3, deadlines3);
Console.WriteLine(result3); // Expected: 1