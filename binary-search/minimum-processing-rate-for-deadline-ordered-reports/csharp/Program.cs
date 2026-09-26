/*
Title: Minimum Processing Rate for Deadline Ordered Reports
Difficulty: Hard
Topic: Binary Search

Problem Description:
You are given a list of report jobs that must be processed in the given order. The i-th job contains reports[i] pages and must be fully completed no later than time deadlines[i]. A single processor works at a constant integer rate r pages per hour. The processor may switch to the next job immediately after finishing the current one, but jobs cannot be reordered, split across multiple processors, or processed in parallel.

For a chosen rate r, the time needed for job i is ceil(reports[i] / r). Because jobs must be processed sequentially, the completion time of each job is the sum of the rounded-up processing times of all jobs from 0 to i. A rate r is feasible if every job finishes by its corresponding deadline.

Return the minimum integer rate r such that all jobs can be completed on time, or -1 if no integer rate can satisfy the deadlines.

This is not a simple per-job check: even if each job individually fits its own deadline, earlier jobs may delay later ones. You need to exploit the monotonic property of feasibility with respect to the processing rate and design an efficient solution.

Constraints:
- 1 <= n == reports.length == deadlines.length <= 2 * 10^5
- 1 <= reports[i] <= 10^12
- 1 <= deadlines[i] <= 10^18
- deadlines is not guaranteed to be sorted, but represents the required completion time of each job in the given order
- The answer, if it exists, is at most 10^12

Example 1:
Input: reports = [8, 5, 10], deadlines = [2, 4, 7]
Output: 4
Explanation:
At rate 4, job times are [2, 2, 3], so cumulative completion times are [2, 4, 7], which exactly meets all deadlines.
At rate 3, job times are [3, 2, 4], so the first job already misses its deadline 2. Therefore the minimum feasible rate is 4.

Example 2:
Input: reports = [9, 9, 9], deadlines = [1, 2, 2]
Output: -1
Explanation:
Even with an arbitrarily large rate, each job still requires at least 1 hour because processing time is rounded up. So the cumulative completion times can never be better than [1, 2, 3]. The third job misses deadline 2, making the schedule impossible.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one rate: O(n)
    - Binary search over rates from 1 to 10^12: O(log 10^12) = about 40 steps
    - Total: O(n log 10^12)

    Space Complexity:
    - O(1) extra space, ignoring the input arrays

    Beginner-friendly idea:
    1. If we increase the processing rate, every job takes the same or less time.
    2. That means feasibility is monotonic:
       - If some rate r works, then every larger rate also works.
       - If some rate r does not work, then every smaller rate also does not work.
    3. Monotonic behavior is exactly what binary search needs.
    */
    public long MinimumProcessingRate(long[] reports, long[] deadlines)
    {
        int n = reports.Length;

        // Step 1:
        // Before doing binary search, we first check whether the schedule is
        // impossible even with an "infinitely fast" processor.
        //
        // Why does this help?
        // Even at an extremely large rate, each job still takes at least 1 hour,
        // because ceil(reports[i] / r) is never 0 for a positive job size.
        //
        // Therefore, the absolute best possible completion times are:
        // job 0 finishes at time 1
        // job 1 finishes at time 2
        // job 2 finishes at time 3
        // ...
        // job i finishes at time i + 1
        //
        // If any deadline[i] < i + 1, then no rate can ever satisfy that job.
        long minimumPossibleCompletion = 0;
        for (int i = 0; i < n; i++)
        {
            minimumPossibleCompletion++;

            if (minimumPossibleCompletion > deadlines[i])
            {
                return -1;
            }
        }

        // Step 2:
        // Define the binary search range.
        //
        // The problem guarantees that if an answer exists, it is at most 10^12.
        // So we search in [1, 10^12].
        long left = 1;
        long right = 1_000_000_000_000L;
        long answer = -1;

        // Step 3:
        // Standard binary search on the answer.
        //
        // We are searching for the smallest feasible rate.
        while (left <= right)
        {
            long mid = left + (right - left) / 2;

            // Check whether this candidate rate is enough.
            if (IsFeasible(reports, deadlines, mid))
            {
                // If mid works, it is a valid answer candidate.
                // But we want the minimum possible rate,
                // so we continue searching on the left half.
                answer = mid;
                right = mid - 1;
            }
            else
            {
                // If mid does not work, every smaller rate also does not work.
                // So we must search in the right half.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool IsFeasible(long[] reports, long[] deadlines, long rate)
    {
        // This variable stores the cumulative completion time:
        // after processing jobs 0..i in order, what time do we finish?
        long currentTime = 0;

        // We process jobs in the given order because reordering is not allowed.
        for (int i = 0; i < reports.Length; i++)
        {
            // Step A:
            // Compute how many whole hours this job needs at the current rate.
            //
            // We need ceil(reports[i] / rate).
            //
            // For integers, a common safe formula is:
            // ceil(a / b) = (a + b - 1) / b
            //
            // Example:
            // reports[i] = 10, rate = 4
            // (10 + 4 - 1) / 4 = 13 / 4 = 3
            long jobTime = (reports[i] + rate - 1) / rate;

            // Step B:
            // Add this job's time to the running total,
            // because jobs are processed sequentially.
            currentTime += jobTime;

            // Step C:
            // Immediately check whether this job misses its own deadline.
            //
            // This is necessary because even if the current job itself is small,
            // delays from earlier jobs may push its completion time too far.
            if (currentTime > deadlines[i])
            {
                return false;
            }
        }

        // If every job finished by its deadline, the rate is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] reports1 = { 8, 5, 10 };
long[] deadlines1 = { 2, 4, 7 };
long result1 = solution.MinimumProcessingRate(reports1, deadlines1);
Console.WriteLine(result1); // Expected: 4

// Example 2
long[] reports2 = { 9, 9, 9 };
long[] deadlines2 = { 1, 2, 2 };
long result2 = solution.MinimumProcessingRate(reports2, deadlines2);
Console.WriteLine(result2); // Expected: -1

// Additional quick demo
long[] reports3 = { 1, 1, 1, 1 };
long[] deadlines3 = { 1, 2, 3, 4 };
long result3 = solution.MinimumProcessingRate(reports3, deadlines3);
Console.WriteLine(result3); // Expected: 1