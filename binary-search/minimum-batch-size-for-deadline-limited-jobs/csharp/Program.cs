/*
Title: Minimum Batch Size for Deadline-Limited Jobs

Problem Description:
A data processing service must execute a list of jobs in the given order. The i-th job contains jobs[i] records and must be fully processed no later than deadline[i], measured in whole days from the start of processing. The service uses a fixed batch size B, meaning it can process at most B records per day. If a job is not finished on one day, its remaining records continue on the next day. Jobs cannot be reordered, and the service starts job i+1 only after job i is complete.

Your task is to find the minimum integer batch size B such that every job finishes on or before its corresponding deadline.

More formally, if prefix = jobs[0] + jobs[1] + ... + jobs[i], then job i finishes after ceil(prefix / B) days. This value must be less than or equal to deadline[i] for every i.

Return the smallest possible B. You may assume that the deadlines are positive integers and that a valid answer always exists.

Constraints:
- 1 <= jobs.length <= 200000
- 1 <= jobs[i] <= 1000000000
- 1 <= deadline[i] <= 1000000000
- deadline.length == jobs.length
- deadlines are not necessarily sorted, but they correspond to the jobs in input order
- A valid batch size always exists

Examples:
1) jobs = [5, 8, 6], deadline = [2, 4, 5]
   Cumulative work = [5, 13, 19]
   For B = 4:
   finish days = [ceil(5/4)=2, ceil(13/4)=4, ceil(19/4)=5] -> valid
   For B = 3:
   finish days = [2, 5, 7] -> invalid
   Minimum valid B = 4

2) jobs = [7, 2, 9, 4], deadline = [1, 2, 5, 6]
   For B = 7:
   cumulative work = [7, 9, 18, 22]
   finish days = [1, 2, 3, 4] -> valid
   For B = 6:
   first job finishes on day ceil(7/6)=2 -> misses deadline 1
   Minimum valid B = 7
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one batch size B: O(n), because we scan the jobs once.
    - Binary search over the answer range: O(log S), where S is the total sum of all jobs.
    - Total: O(n log S)

    Space Complexity:
    - O(1) extra space, ignoring the input arrays.
    */
    public long MinimumBatchSize(int[] jobs, int[] deadline)
    {
        // We will binary search the answer because the condition is monotonic:
        // - If some batch size B is sufficient, then any larger batch size is also sufficient.
        // This is exactly the kind of pattern where binary search is appropriate.

        // The smallest possible batch size cannot be less than 1,
        // because processing capacity per day must be a positive integer.
        long left = 1;

        // The largest batch size we ever need to consider is the total amount of work.
        // Why?
        // - If B equals the total sum of all jobs, then all work can be completed in 1 day.
        // - Since the problem guarantees that a valid answer exists, this upper bound is safe.
        long right = 0;

        // Compute the total sum of all records.
        // We use long because:
        // - jobs[i] can be up to 1,000,000,000
        // - there can be up to 200,000 jobs
        // - the total can be much larger than what int can safely store
        for (int i = 0; i < jobs.Length; i++)
        {
            right += jobs[i];
        }

        // This variable will store the best valid answer found so far.
        // Since a valid answer always exists, it will definitely be assigned.
        long answer = right;

        // Standard binary search on the integer answer space [left, right].
        while (left <= right)
        {
            // Compute the middle carefully to avoid overflow.
            long mid = left + (right - left) / 2;

            // Check whether this candidate batch size is sufficient.
            if (IsFeasible(jobs, deadline, mid))
            {
                // If mid works, it is a valid answer.
                // But we want the MINIMUM valid batch size,
                // so we record it and continue searching to the left.
                answer = mid;
                right = mid - 1;
            }
            else
            {
                // If mid does not work, then any smaller batch size also cannot work
                // because smaller capacity per day only makes finishing later, never earlier.
                // So we must search to the right for a larger batch size.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool IsFeasible(int[] jobs, int[] deadline, long batchSize)
    {
        // This method answers:
        // "If the service can process at most batchSize records per day,
        //  do all jobs finish by their respective deadlines?"

        // We track cumulative work processed up to each job.
        // For job i, the total work that must be completed by the time job i finishes is:
        // jobs[0] + jobs[1] + ... + jobs[i]
        long prefixSum = 0;

        for (int i = 0; i < jobs.Length; i++)
        {
            // Add the current job's records to the cumulative total.
            prefixSum += jobs[i];

            // The finish day for job i is:
            // ceil(prefixSum / batchSize)
            //
            // In integer arithmetic, a common way to compute ceil(a / b) for positive integers is:
            // (a + b - 1) / b
            //
            // We use long arithmetic to remain safe for large values.
            long finishDay = (prefixSum + batchSize - 1) / batchSize;

            // If this job finishes after its allowed deadline,
            // then this batch size is not sufficient.
            if (finishDay > deadline[i])
            {
                return false;
            }
        }

        // If every job met its deadline, the batch size is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] jobs1 = { 5, 8, 6 };
int[] deadline1 = { 2, 4, 5 };
long result1 = solution.MinimumBatchSize(jobs1, deadline1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] jobs2 = { 7, 2, 9, 4 };
int[] deadline2 = { 1, 2, 5, 6 };
long result2 = solution.MinimumBatchSize(jobs2, deadline2);
Console.WriteLine(result2); // Expected: 7

// Additional small demo
int[] jobs3 = { 3, 3, 3 };
int[] deadline3 = { 1, 2, 3 };
long result3 = solution.MinimumBatchSize(jobs3, deadline3);
Console.WriteLine(result3); // Expected: 3