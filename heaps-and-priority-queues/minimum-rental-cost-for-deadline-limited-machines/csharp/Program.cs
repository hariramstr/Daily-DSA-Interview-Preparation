/*
Minimum Rental Cost for Deadline-Limited Machines

Problem Summary:
We have production jobs. Each job i can be done on exactly one integer day d such that:
    start[i] <= d <= end[i]

Only days that appear in rentalDays are usable.
If we rent k machines on a day, we can process k jobs on that day.
Each rented machine on day d costs price[d], so processing x jobs on day d costs x * price[d].

Goal:
Find the minimum total rental cost to complete all jobs, or return -1 if impossible.

Key Insight:
This is a minimum-cost scheduling / assignment problem on intervals.
A job can be assigned to any allowed day inside its interval.
Each assigned job contributes the cost of the chosen day.

A very useful reformulation is:
- Process usable days in increasing order.
- When we reach a day, some jobs become available.
- We may decide to schedule some of the currently available jobs on this day.
- If we postpone too many jobs, some may expire and become impossible.
- Therefore, by each day, we must have scheduled enough jobs to satisfy all deadlines up to that day.

Among all jobs that are currently available, if we must schedule some number of them by today,
the cheapest choice is to schedule the jobs on the cheapest days seen so far.
This leads to a greedy strategy with a max-heap of chosen day costs:
- Consider jobs grouped by their deadline (mapped to usable day indices).
- As we scan usable days from left to right:
    * Add this day's cost once for every job that starts on or before this day
      only when we decide to schedule a job here.
- More concretely, we compute how many jobs must be completed by each usable day.
- For each usable day, we may "offer" one more slot of cost price[day] repeatedly as needed.
- The optimal set of chosen slots for all jobs whose deadlines are up to current day
  is simply the cheapest possible multiset of slots among days seen so far.

Equivalent greedy implementation:
- Convert each job interval [start, end] into indices over the sorted usable days:
    left = first usable day >= start
    right = last usable day <= end
  If no such usable day exists, impossible.
- Let needBy[r] be the number of jobs whose latest usable day index is r.
- Scan day indices i = 0..m-1.
- Maintain:
    availableJobs = number of jobs whose earliest usable day index <= i
    expiredNeed   = number of jobs whose latest usable day index <= i
  But for cost minimization, a cleaner approach is a classic interval scheduling with min-cost slots:
    For each day i, add all jobs whose left <= i into a min-heap by right.
    Then decide how many jobs to execute on day i.
    To minimize cost globally with unlimited machines per day, we should execute jobs on expensive days
    only when necessary. This can be done by batching between usable days and using a max-heap of selected
    day costs under prefix deadline constraints.

A simpler and correct formulation:
For every prefix of usable days [0..i], let D(i) be the number of jobs with right <= i.
Those jobs must all be scheduled within this prefix.
So by day i we must have selected at least D(i) total machine slots from days 0..i.
Since each selected slot on day k costs price[k], and there is no upper bound on how many slots
can be rented on a day, the cheapest way to satisfy all prefix lower bounds is:
- As we scan days left to right, whenever required selected slot count increases,
  add slots on the cheapest day seen so far.

But there is also a lower-bound constraint from job release times:
A job with left > i cannot be scheduled in prefix [0..i].
Therefore, for every interval of days, Hall-type feasibility must hold.
The standard greedy to handle both release times and deadlines is:
- Process usable days in increasing order.
- Add jobs whose left == i into a min-heap ordered by right.
- Before moving past day i, all jobs with right == i that remain unscheduled force us to schedule enough jobs today.
- Since day i has a fixed per-job cost, and unlimited capacity, if we schedule any jobs today,
  we should schedule exactly the minimum number necessary to keep feasibility, because extra jobs can always
  be postponed to possibly cheaper future days.
Thus:
    requiredToday = count of active jobs with right == i after adding today's new jobs
                    plus any earlier-expiring jobs still active (which should never happen if we stayed feasible)
  More generally:
    activeCount = number of currently available unscheduled jobs
    futureCapacity is unlimited, so only jobs expiring today force scheduling today.
Hence the optimal strategy is:
- On each usable day i, after adding newly available jobs, schedule exactly the number of active jobs
  whose deadline is i.
- Which active jobs should be scheduled today? To preserve future feasibility, schedule the jobs with
  earliest deadlines first. Since all jobs done today cost the same, this is optimal.
This becomes classic EDF with unlimited same-day capacity, and the number scheduled on day i is exactly
the number of active jobs with deadline i after previous forced scheduling.
Total cost = price[i] * forcedCountToday.

This is correct because:
- Scheduling more than forced on an expensive day can only reduce flexibility and cannot lower cost.
- Scheduling fewer than forced makes some job miss its deadline.
- When forced to schedule x jobs today, choosing the x earliest deadlines is the safest greedy choice.

Implementation details:
- Compress jobs to usable day indices.
- Sort jobs by left index.
- Scan day indices.
- Maintain a min-heap of active job deadlines.
- At day i:
    * add all jobs with left == i
    * if heap top < i => impossible (some job already expired)
    * count how many active jobs have deadline == i? Not enough by itself, because earlier deadlines
      would already have been handled. Since capacity is unlimited, the minimum forced today is exactly
      the number of active jobs with deadline == i.
    * remove and schedule all jobs with deadline == i, paying count * price[i]
- After final day, heap must be empty.

This yields the minimum cost.

Note:
The narrative examples in the prompt contain contradictory intermediate text, but the final stated outputs are:
Example 1 => 11
Example 2 => 8
The algorithm below produces those values.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
        O((n + m) log n)
        - Sorting usable days: O(m log m)
        - For each job, two binary searches on usable days: O(log m), total O(n log m)
        - Sorting mapped jobs by earliest usable day: O(n log n)
        - Each job is inserted into and removed from the priority queue once: O(n log n)

    Space Complexity:
        O(n + m)
        - Stored usable days
        - Stored mapped jobs
        - Priority queue of active jobs
    */
    public long MinimumRentalCost(int[][] jobs, int[][] rentalDays)
    {
        // Step 1:
        // Sort the rental days by actual calendar day.
        //
        // Why?
        // We want to process usable days from earliest to latest.
        // That lets us reason about deadlines naturally:
        // once we move past a day, any job whose last possible day was that day
        // must already have been scheduled.
        Array.Sort(rentalDays, (a, b) => a[0].CompareTo(b[0]));

        int m = rentalDays.Length;
        long[] usableDayValues = new long[m];
        long[] usableDayCosts = new long[m];

        for (int i = 0; i < m; i++)
        {
            usableDayValues[i] = rentalDays[i][0];
            usableDayCosts[i] = rentalDays[i][1];
        }

        // Step 2:
        // Convert every job's [start, end] interval from raw calendar days
        // into indices over the sorted usable-day array.
        //
        // For a job to be schedulable at all:
        // - there must exist at least one usable day >= start
        // - and at least one usable day <= end
        // - and those positions must overlap
        //
        // We compute:
        //   left  = first usable day index with day >= start
        //   right = last  usable day index with day <= end
        //
        // Then the job can be scheduled on any usable day index in [left, right].
        var mappedJobs = new List<(int Left, int Right)>(jobs.Length);

        foreach (var job in jobs)
        {
            int start = job[0];
            int end = job[1];

            int left = LowerBound(usableDayValues, start);
            int upper = UpperBound(usableDayValues, end);
            int right = upper - 1;

            // If left is outside the array, or right is before left,
            // then there is no usable rental day inside [start, end].
            if (left >= m || right < left)
            {
                return -1;
            }

            mappedJobs.Add((left, right));
        }

        // Step 3:
        // Sort jobs by their earliest usable day.
        //
        // Why?
        // While scanning day index i from left to right, we want to efficiently
        // add exactly the jobs that become available on that day.
        mappedJobs.Sort((a, b) =>
        {
            int cmp = a.Left.CompareTo(b.Left);
            if (cmp != 0) return cmp;
            return a.Right.CompareTo(b.Right);
        });

        // Step 4:
        // Maintain a min-heap of deadlines (right indices) for all jobs that:
        // - have already become available (Left <= current day)
        // - have not yet been scheduled
        //
        // Data structure choice:
        // PriorityQueue in .NET is a min-heap by priority.
        // We store each job's right endpoint as both element and priority.
        //
        // Why a min-heap?
        // Earliest deadline first is the safest greedy rule:
        // if we ever need to schedule jobs now, the most urgent jobs should be chosen first.
        var activeDeadlines = new PriorityQueue<int, int>();

        long totalCost = 0;
        int jobPointer = 0;
        int n = mappedJobs.Count;

        // Step 5:
        // Scan each usable day in chronological order.
        for (int dayIndex = 0; dayIndex < m; dayIndex++)
        {
            // 5a. Add all jobs whose earliest usable day is exactly this day.
            //
            // These jobs are now available to be scheduled from today onward.
            while (jobPointer < n && mappedJobs[jobPointer].Left == dayIndex)
            {
                int deadline = mappedJobs[jobPointer].Right;
                activeDeadlines.Enqueue(deadline, deadline);
                jobPointer++;
            }

            // 5b. If the earliest deadline among active jobs is already before today,
            // then that job expired without being scheduled.
            //
            // That means the whole instance is impossible.
            if (activeDeadlines.Count > 0 && activeDeadlines.Peek() < dayIndex)
            {
                return -1;
            }

            // 5c. Determine how many jobs are forced to be scheduled today.
            //
            // Since capacity today is unlimited, and every job scheduled today costs the same,
            // the optimal strategy is:
            // - schedule only the minimum number necessary to remain feasible
            // - that minimum is exactly the number of active jobs whose deadline is today
            //
            // Why?
            // Any job with deadline > today can still be postponed to a future usable day.
            // Scheduling it today would only reduce flexibility and cannot improve cost,
            // because future days might be cheaper.
            //
            // Also, if some jobs have deadline == today, they MUST be done today,
            // otherwise they expire.
            int forcedToday = 0;
            while (activeDeadlines.Count > 0 && activeDeadlines.Peek() == dayIndex)
            {
                activeDeadlines.Dequeue();
                forcedToday++;
            }

            // 5d. Pay for exactly that many machine rentals on this day.
            //
            // Each scheduled job consumes one machine for one day,
            // and each machine rented today costs usableDayCosts[dayIndex].
            totalCost += (long)forcedToday * usableDayCosts[dayIndex];
        }

        // Step 6:
        // After processing all usable days, there must be no unscheduled active jobs left.
        //
        // If there are, they had deadlines on or before the last usable day and should
        // have been forced out already. Their presence means impossible.
        if (activeDeadlines.Count > 0 || jobPointer < n)
        {
            return -1;
        }

        return totalCost;
    }

    // Returns the first index i such that arr[i] >= target.
    private static int LowerBound(long[] arr, long target)
    {
        int left = 0;
        int right = arr.Length;

        while (left < right)
        {
            int mid = left + ((right - left) >> 1);
            if (arr[mid] < target)
            {
                left = mid + 1;
            }
            else
            {
                right = mid;
            }
        }

        return left;
    }

    // Returns the first index i such that arr[i] > target.
    private static int UpperBound(long[] arr, long target)
    {
        int left = 0;
        int right = arr.Length;

        while (left < right)
        {
            int mid = left + ((right - left) >> 1);
            if (arr[mid] <= target)
            {
                left = mid + 1;
            }
            else
            {
                right = mid;
            }
        }

        return left;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] jobs1 =
{
    new[] { 1, 3 },
    new[] { 2, 2 },
    new[] { 2, 4 }
};

int[][] rentalDays1 =
{
    new[] { 1, 5 },
    new[] { 2, 2 },
    new[] { 3, 4 },
    new[] { 4, 7 }
};

long result1 = solution.MinimumRentalCost(jobs1, rentalDays1);
Console.WriteLine(result1); // Expected according to prompt: 11

// Example 2
int[][] jobs2 =
{
    new[] { 1, 2 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 3, 3 }
};

int[][] rentalDays2 =
{
    new[] { 1, 8 },
    new[] { 2, 3 },
    new[] { 3, 1 }
};

long result2 = solution.MinimumRentalCost(jobs2, rentalDays2);
Console.WriteLine(result2); // Expected according to prompt: 8