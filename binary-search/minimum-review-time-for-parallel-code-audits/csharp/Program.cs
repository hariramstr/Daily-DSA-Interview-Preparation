/*
Title: Minimum Review Time for Parallel Code Audits
Difficulty: Hard
Topic: Binary Search

Problem Description:
A company needs to review a large set of code changes before a release. There are n code changes, and the i-th change requires reviewWork[i] units of work. The company has m senior reviewers. Reviewer j can review at speed speed[j], meaning they can complete speed[j] units of work per hour. A single code change cannot be split across multiple reviewers, but each reviewer may review any number of code changes, one after another.

You may assign the code changes in any order to any reviewers. The total release review time is the maximum total time spent by any single reviewer. Return the minimum possible release review time needed to finish reviewing all code changes.

Formally, if a reviewer is assigned changes with total work W, that reviewer needs ceil(W / speed[j]) hours. You want to partition all code changes among the m reviewers so that the maximum reviewer completion time is minimized.

This is a decision-and-optimization problem: for a candidate time T, determine whether it is possible to assign every code change to some reviewer such that each assigned reviewer finishes within T hours. Then compute the minimum feasible T.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= m <= 20
- 1 <= reviewWork[i] <= 10^9
- 1 <= speed[j] <= 10^9
- m <= n is not guaranteed
- Every code change must be assigned to exactly one reviewer

Example 1:
Input: reviewWork = [6, 8, 5, 3], speed = [4, 2]
Output: 4

Example 2:
Input: reviewWork = [9, 9, 9, 9, 9], speed = [3, 3, 3]
Output: 6
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let n = number of code changes, m = number of reviewers.

    - We binary search the answer T over a range up to about 1e18, so there are at most ~60 iterations.
    - For each candidate T, feasibility checking does:
        1) O(n * m) to classify each task by which reviewers can take it.
        2) O(3^m) subset DP / inclusion style processing, which is practical because m <= 20.
        3) O(m * 2^m) for subset-sum style accumulation.
    Overall:
        O(log Answer * (n * m + m * 2^m + 3^m))

    Since m <= 20, the exponential part depends only on the small reviewer count, while n can be large.

    Space Complexity:
    O(2^m + n) in the feasibility routine, dominated by arrays over reviewer subsets.
    */
    public long MinimumReviewTime(int[] reviewWork, int[] speed)
    {
        int n = reviewWork.Length;
        int m = speed.Length;

        // We sort reviewer speeds in descending order.
        // Why?
        // - It is not strictly required for correctness.
        // - But it makes reasoning cleaner: earlier reviewers are at least as capable as later ones.
        // - It can also slightly improve practical behavior in the feasibility logic.
        long[] speeds = speed.Select(x => (long)x).OrderByDescending(x => x).ToArray();

        long maxWork = 0;
        foreach (int w in reviewWork) maxWork = Math.Max(maxWork, (long)w);

        // Lower bound:
        // At minimum, the largest single task must fit into at least one reviewer within T hours.
        // Since the fastest reviewer has speed speeds[0], we need:
        //   T >= ceil(maxWork / fastestSpeed)
        long left = CeilDiv(maxWork, speeds[0]);

        // Upper bound:
        // One safe upper bound is assigning everything to the fastest reviewer.
        // Then time needed is ceil(totalWork / fastestSpeed).
        long totalWork = 0;
        foreach (int w in reviewWork) totalWork += w;
        long right = CeilDiv(totalWork, speeds[0]);

        // Standard binary search for the minimum feasible time.
        while (left < right)
        {
            long mid = left + (right - left) / 2;

            if (CanFinish(reviewWork, speeds, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        return left;
    }

    private static long CeilDiv(long a, long b) => (a + b - 1) / b;

    private bool CanFinish(int[] reviewWork, long[] speeds, long timeLimit)
    {
        int m = speeds.Length;
        int fullMask = (1 << m) - 1;

        // Step 1:
        // Convert the time limit T into a capacity for each reviewer.
        //
        // If reviewer j has speed speed[j], then in T hours they can process:
        //   capacity[j] = speed[j] * T
        //
        // This transforms the problem into:
        // "Can we assign each indivisible task to one reviewer so that the total work assigned
        //  to reviewer j does not exceed capacity[j]?"
        //
        // This is a bin packing / scheduling style feasibility problem with only m <= 20 bins,
        // which is the key reason an exponential-in-m approach is acceptable.
        long[] capacity = new long[m];
        for (int j = 0; j < m; j++)
        {
            capacity[j] = speeds[j] * timeLimit;
        }

        // Step 2:
        // For each task, determine the set of reviewers that are capable of taking it.
        //
        // A reviewer can take task w if capacity[j] >= w.
        //
        // We encode that set as a bitmask:
        // - bit j = 1 means reviewer j can take this task.
        //
        // Example:
        // If m = 4 and only reviewers 0 and 2 can take a task, mask = 0101b.
        //
        // Why do this?
        // Because tasks with the same "eligible reviewer set" behave similarly in the feasibility logic.
        //
        // We will aggregate total work by exact eligibility mask:
        //   exactWork[mask] = sum of task sizes whose eligible reviewer set is exactly mask.
        //
        // Important early failure:
        // If a task has mask = 0, then no reviewer can take it under this time limit,
        // so the candidate T is immediately infeasible.
        long[] exactWork = new long[1 << m];

        foreach (int wInt in reviewWork)
        {
            long w = wInt;
            int mask = 0;

            for (int j = 0; j < m; j++)
            {
                if (capacity[j] >= w)
                {
                    mask |= 1 << j;
                }
            }

            if (mask == 0)
            {
                return false;
            }

            exactWork[mask] += w;
        }

        // Step 3:
        // Compute subset capacities:
        //   subsetCapacity[S] = sum of capacities of reviewers in subset S
        //
        // This is useful because for any group of tasks that can only go to reviewers inside S,
        // the total work of those tasks must not exceed the total capacity of S.
        //
        // This is a necessary condition. Surprisingly, for this special "assignment of divisible-by-sum
        // but indivisible-per-task with eligibility by capacity threshold" structure, checking all subset
        // constraints is sufficient as well when tasks are aggregated by eligible sets and every task fits
        // individually into at least one allowed reviewer. This is a Hall-type / polymatroid style condition.
        long[] subsetCapacity = new long[1 << m];
        for (int mask = 1; mask <= fullMask; mask++)
        {
            int lsb = mask & -mask;
            int bit = BitIndex(lsb);
            subsetCapacity[mask] = subsetCapacity[mask ^ lsb] + capacity[bit];
        }

        // Step 4:
        // We need, for every subset S of reviewers:
        //
        //   total work of tasks whose eligible set is a subset of S  <=  subsetCapacity[S]
        //
        // Why?
        // Because those tasks cannot be assigned outside S.
        // So reviewers in S must be able to absorb all of that work.
        //
        // Let:
        //   restrictedWork[S] = sum of exactWork[A] for all A subset of S
        //
        // This is a classic "sum over subsets" transform (SOS DP).
        long[] restrictedWork = new long[1 << m];
        Array.Copy(exactWork, restrictedWork, exactWork.Length);

        // SOS DP:
        // After this loop, restrictedWork[S] contains the sum over all subset masks of S.
        for (int bit = 0; bit < m; bit++)
        {
            for (int mask = 0; mask <= fullMask; mask++)
            {
                if ((mask & (1 << bit)) != 0)
                {
                    restrictedWork[mask] += restrictedWork[mask ^ (1 << bit)];
                }
            }
        }

        // Step 5:
        // Check the subset constraints.
        //
        // If for any subset S:
        //   restrictedWork[S] > subsetCapacity[S]
        // then T is impossible.
        //
        // Intuition:
        // - restrictedWork[S] is the amount of work trapped inside S.
        // - subsetCapacity[S] is the total amount S can process.
        // - If trapped work exceeds available capacity, no assignment can exist.
        for (int mask = 0; mask <= fullMask; mask++)
        {
            if (restrictedWork[mask] > subsetCapacity[mask])
            {
                return false;
            }
        }

        // If all subset constraints hold, the time limit is feasible.
        return true;
    }

    private int BitIndex(int singleBitMask)
    {
        int index = 0;
        while ((singleBitMask >> index) != 1) index++;
        return index;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] reviewWork1 = { 6, 8, 5, 3 };
int[] speed1 = { 4, 2 };
long result1 = solution.MinimumReviewTime(reviewWork1, speed1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] reviewWork2 = { 9, 9, 9, 9, 9 };
int[] speed2 = { 3, 3, 3 };
long result2 = solution.MinimumReviewTime(reviewWork2, speed2);
Console.WriteLine(result2); // Expected: 6

// Additional quick checks
int[] reviewWork3 = { 10 };
int[] speed3 = { 3, 5 };
long result3 = solution.MinimumReviewTime(reviewWork3, speed3);
Console.WriteLine(result3); // Expected: 2

int[] reviewWork4 = { 7, 7, 7, 7 };
int[] speed4 = { 2, 2 };
long result4 = solution.MinimumReviewTime(reviewWork4, speed4);
Console.WriteLine(result4); // Expected: 7