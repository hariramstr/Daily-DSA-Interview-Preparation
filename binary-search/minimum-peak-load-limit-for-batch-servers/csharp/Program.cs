/*
Title: Minimum Peak Load Limit for Batch Servers

Problem Description:
You are given an array jobs where jobs[i] is the processing time of the i-th job in a fixed arrival order,
and an integer m representing the number of servers available.

Each server must process one contiguous block of jobs, and every job must be assigned to exactly one server.
Some servers may remain unused, but the order of jobs cannot be changed.

Your task is to compute the minimum possible peak load L such that all jobs can be partitioned into at most m
contiguous groups, where the sum of each group is at most L.

Additional deployment rule:
- Every used server must receive at least one job.
- If a partition is possible for a candidate load L, it is valid as long as the number of groups used is between
  1 and m inclusive.

Return the smallest integer L for which such an assignment exists.

Key insight:
- If a certain load limit L is feasible, then any larger load limit is also feasible.
- This monotonic behavior allows us to use Binary Search on the answer.

Examples:
1) jobs = [7, 2, 5, 10, 8], m = 2
   Output: 18

2) jobs = [1, 4, 4, 3, 2], m = 3
   Output: 5
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of jobs.
    - Each feasibility check scans the array once: O(n).
    - Binary search runs over the numeric answer range from max(jobs) to sum(jobs),
      which takes O(log(sum(jobs))) iterations.
    - Total: O(n * log(sum(jobs)))

    Space Complexity:
    - O(1) extra space, ignoring the input array.
    */
    public long MinimumPeakLoad(int[] jobs, int m)
    {
        // -----------------------------
        // Step 1: Establish binary search boundaries.
        // -----------------------------
        //
        // We are searching for the smallest possible load limit L.
        //
        // Lower bound:
        // - No valid answer can be smaller than the largest single job,
        //   because every job must belong to some group, and a group sum
        //   cannot be less than one of its own elements.
        //
        // Upper bound:
        // - One server could process all jobs in one contiguous block,
        //   so the total sum of all jobs is always a valid upper bound.
        //
        // We use long because:
        // - jobs[i] can be as large as 1,000,000,000
        // - there can be up to 200,000 jobs
        // - the total sum can exceed int range
        long left = 0;
        long right = 0;

        foreach (int job in jobs)
        {
            if (job > left)
            {
                left = job;
            }

            right += job;
        }

        // -----------------------------
        // Step 2: Binary search for the minimum feasible load.
        // -----------------------------
        //
        // Invariant:
        // - Any value < answer is infeasible
        // - Any value >= answer may be feasible
        //
        // We repeatedly test the middle value:
        // - If it is feasible, try smaller values
        // - If it is not feasible, try larger values
        while (left < right)
        {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether we can split jobs into at most m contiguous groups
            // such that each group's sum is <= mid.
            if (CanPartitionWithinLimit(jobs, m, mid))
            {
                // mid works, so the answer is <= mid.
                // Keep searching on the left side to find the minimum feasible value.
                right = mid;
            }
            else
            {
                // mid does not work, so the answer must be larger.
                left = mid + 1;
            }
        }

        // When left == right, binary search has converged to the smallest feasible load.
        return left;
    }

    private bool CanPartitionWithinLimit(int[] jobs, int m, long limit)
    {
        // -----------------------------
        // Goal of this helper:
        // -----------------------------
        //
        // Determine whether it is possible to partition the jobs array into
        // at most m contiguous groups such that each group's sum is <= limit.
        //
        // Why "at most m" is enough:
        // - The problem allows unused servers.
        // - Every used server must have at least one job.
        // - Therefore, if we can do it in k groups where 1 <= k <= m, it is valid.
        //
        // Greedy strategy:
        // - Build the current group from left to right.
        // - Keep adding jobs while the sum stays <= limit.
        // - As soon as adding the next job would exceed limit, start a new group.
        //
        // Why this greedy method is correct:
        // - For a fixed limit, packing as many jobs as possible into the current group
        //   minimizes the number of groups used.
        // - If even this minimum number of groups is greater than m, then no valid
        //   partition exists for this limit.
        long currentGroupSum = 0;

        // We start with one group because:
        // - There is at least one job
        // - Every used group must contain at least one job
        int groupsUsed = 1;

        foreach (int job in jobs)
        {
            // Safety check:
            // If a single job is larger than the limit, then no partition can work.
            // This usually will not happen during binary search because the lower bound
            // starts at max(jobs), but keeping this check makes the helper robust and clear.
            if (job > limit)
            {
                return false;
            }

            // Try to place the current job into the current group.
            if (currentGroupSum + job <= limit)
            {
                // It fits, so extend the current contiguous block.
                currentGroupSum += job;
            }
            else
            {
                // It does not fit.
                // Therefore, we must start a new group beginning with this job.
                groupsUsed++;

                // If we already need more than m groups, this limit is infeasible.
                if (groupsUsed > m)
                {
                    return false;
                }

                currentGroupSum = job;
            }
        }

        // If we finished scanning all jobs without exceeding m groups,
        // then the partition is feasible.
        return true;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// jobs = [7, 2, 5, 10, 8], m = 2
// Expected output: 18
//
// Quick reasoning:
// - Lower bound is max job = 10
// - Upper bound is total sum = 32
// - The minimum feasible answer is 18:
//   [7, 2, 5] = 14
//   [10, 8]   = 18
int[] jobs1 = { 7, 2, 5, 10, 8 };
int m1 = 2;
long result1 = solution.MinimumPeakLoad(jobs1, m1);
Console.WriteLine(result1);

// Example 2:
// jobs = [1, 4, 4, 3, 2], m = 3
// Expected output: 5
//
// One valid partition for limit 5:
// [1, 4] = 5
// [4]    = 4
// [3, 2] = 5
//
// Trying 4 fails because:
// - [1] [4] [4] [3] [2] would need too many groups if we preserve order and keep each sum <= 4
int[] jobs2 = { 1, 4, 4, 3, 2 };
int m2 = 3;
long result2 = solution.MinimumPeakLoad(jobs2, m2);
Console.WriteLine(result2);