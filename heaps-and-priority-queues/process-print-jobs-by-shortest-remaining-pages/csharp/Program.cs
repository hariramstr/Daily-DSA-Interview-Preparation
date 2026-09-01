/*
Title: Process Print Jobs by Shortest Remaining Pages

Problem Description:
A shared office printer receives print jobs throughout the day. Each job is described by two integers:
arrivalTime and pages. The printer can work on at most one job at a time, and once it starts a job,
it prints the entire job before switching to another one.

If multiple jobs are available, the printer always chooses the job with the fewest pages remaining
to minimize average waiting time. If there is a tie, choose the job with the smaller original index.

You are given a 0-indexed array jobs where jobs[i] = [arrivalTime_i, pages_i].
Return the order of job indices in which the printer processes the jobs.

The printer may be idle if no jobs have arrived yet. When the printer becomes free, it may only choose
among jobs whose arrivalTime is less than or equal to the current time. A job that arrives while another
job is being printed must wait until the current job finishes.

Your task is to simulate this scheduling policy efficiently.

Constraints:
- 1 <= jobs.length <= 100000
- 0 <= arrivalTime_i <= 10^9
- 1 <= pages_i <= 10^9
- All jobs fit in 64-bit signed integer time calculations.

Examples:
1) jobs = [[1,4],[2,3],[3,1],[10,2]]
   Output: [0,2,1,3]

2) jobs = [[0,5],[0,2],[0,2],[1,1]]
   Output: [1,2,3,0]
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    // Time Complexity:
    // - Sorting the jobs by arrival time takes O(n log n)
    // - Each job is inserted into the priority queue once and removed once
    // - Each priority queue operation takes O(log n)
    // - Total: O(n log n)
    //
    // Space Complexity:
    // - We store the sorted jobs array: O(n)
    // - The priority queue can hold up to O(n) jobs
    // - The result array stores O(n) indices
    // - Total: O(n)
    public int[] GetPrintOrder(int[][] jobs)
    {
        int n = jobs.Length;

        // We create a new array that keeps:
        // 1) arrival time
        // 2) number of pages
        // 3) original index
        //
        // Why do we need the original index?
        // Because after sorting by arrival time, we still must return the order
        // using the original positions from the input.
        var enrichedJobs = new (int arrival, int pages, int index)[n];
        for (int i = 0; i < n; i++)
        {
            enrichedJobs[i] = (jobs[i][0], jobs[i][1], i);
        }

        // We sort all jobs by arrival time so we can process them in the order
        // they become available to the printer.
        //
        // This is a very common simulation pattern:
        // - sort events by time
        // - walk through them with a pointer
        Array.Sort(enrichedJobs, (a, b) =>
        {
            int compareArrival = a.arrival.CompareTo(b.arrival);
            if (compareArrival != 0) return compareArrival;

            int comparePages = a.pages.CompareTo(b.pages);
            if (comparePages != 0) return comparePages;

            return a.index.CompareTo(b.index);
        });

        // This priority queue stores jobs that have already arrived and are waiting
        // to be printed.
        //
        // We want the "best" next job to come out first:
        // - smallest number of pages
        // - if tied, smaller original index
        //
        // In .NET's PriorityQueue<TElement, TPriority>, the smallest priority value
        // is dequeued first. So we use a tuple priority:
        // (pages, index)
        //
        // Element:
        //   the full job tuple (arrival, pages, index)
        // Priority:
        //   (pages, index)
        var availableJobs = new PriorityQueue<(int arrival, int pages, int index), (int pages, int index)>();

        // This will store the final processing order of original job indices.
        int[] result = new int[n];
        int resultPosition = 0;

        // This pointer tells us how many jobs from the sorted list have already been
        // moved into the "available jobs" heap.
        int nextJobToAdd = 0;

        // We use long for currentTime because arrival times and page counts can be large,
        // and repeated additions could overflow a 32-bit int.
        long currentTime = 0;

        // We continue until we have scheduled all jobs.
        //
        // There are two sources of unfinished work:
        // 1) jobs not yet added from the sorted array
        // 2) jobs already added and waiting in the heap
        while (nextJobToAdd < n || availableJobs.Count > 0)
        {
            // If there are no available jobs right now, the printer is idle.
            //
            // In that case, we must "jump" time forward to the next job's arrival.
            // This is important because:
            // - we cannot print anything before a job arrives
            // - advancing one unit at a time would be far too slow
            //
            // So instead of simulating every moment, we jump directly to the next event.
            if (availableJobs.Count == 0 && currentTime < enrichedJobs[nextJobToAdd].arrival)
            {
                currentTime = enrichedJobs[nextJobToAdd].arrival;
            }

            // Add every job whose arrival time is <= currentTime into the heap.
            //
            // Why do we do this in a loop?
            // Because multiple jobs may have arrived by the current time, and all of them
            // are now candidates for the next print decision.
            while (nextJobToAdd < n && enrichedJobs[nextJobToAdd].arrival <= currentTime)
            {
                var job = enrichedJobs[nextJobToAdd];
                availableJobs.Enqueue(job, (job.pages, job.index));
                nextJobToAdd++;
            }

            // Now, if there is at least one available job, we choose the one with:
            // - the fewest pages
            // - if tied, the smaller original index
            //
            // That is exactly what the priority queue gives us.
            if (availableJobs.Count > 0)
            {
                var jobToPrint = availableJobs.Dequeue();

                // Record the original index in the answer.
                result[resultPosition] = jobToPrint.index;
                resultPosition++;

                // The printer is non-preemptive:
                // once it starts a job, it prints the whole job before switching.
                //
                // So we simply add the full page count to currentTime.
                currentTime += jobToPrint.pages;
            }
        }

        return result;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] jobs1 =
{
    new[] { 1, 4 },
    new[] { 2, 3 },
    new[] { 3, 1 },
    new[] { 10, 2 }
};

int[] result1 = solution.GetPrintOrder(jobs1);
Console.WriteLine("Example 1 Output: [" + string.Join(",", result1) + "]");
Console.WriteLine("Expected: [0,2,1,3]");
Console.WriteLine();

// Example 2
int[][] jobs2 =
{
    new[] { 0, 5 },
    new[] { 0, 2 },
    new[] { 0, 2 },
    new[] { 1, 1 }
};

int[] result2 = solution.GetPrintOrder(jobs2);
Console.WriteLine("Example 2 Output: [" + string.Join(",", result2) + "]");
Console.WriteLine("Expected: [1,2,3,0]");
Console.WriteLine();

// Additional quick demo
int[][] jobs3 =
{
    new[] { 5, 3 },
    new[] { 5, 1 },
    new[] { 6, 2 }
};

int[] result3 = solution.GetPrintOrder(jobs3);
Console.WriteLine("Example 3 Output: [" + string.Join(",", result3) + "]");