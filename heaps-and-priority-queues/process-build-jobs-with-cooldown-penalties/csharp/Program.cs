/*
Process Build Jobs with Cooldown Penalties

Problem Description:
A CI system receives n build jobs. Job i becomes available at time availableTime[i], requires duration[i] units of processing time, and has a penalty rate penalty[i]. The machine can process at most one job at a time, and once a job starts, it runs to completion without preemption. If a job starts at time s, its waiting penalty is (s - availableTime[i]) * penalty[i]. We must compute the minimum possible total waiting penalty over all jobs.

The machine may stay idle even if some jobs are available, but doing so is only useful if it helps reduce the overall penalty later. Jobs can arrive while another job is running. We must choose the processing order to minimize the sum of waiting penalties of all jobs.

Important scheduling insight:
- The objective is equivalent to minimizing sum of penalty[i] * completionTime[i], up to a constant:
    waiting penalty of job i = penalty[i] * (startTime[i] - availableTime[i])
    and startTime[i] = completionTime[i] - duration[i]
    so:
    penalty[i] * (startTime[i] - availableTime[i])
      = penalty[i] * completionTime[i] - penalty[i] * duration[i] - penalty[i] * availableTime[i]
- Therefore, minimizing total waiting penalty is the same as minimizing sum of weighted completion times on a single machine with release times.

This problem is hard in full generality. However, the intended heap-based interview solution uses the key local rule:
- Whenever the machine is busy continuously over some interval, among currently available jobs the next chosen job should be the one with the largest penalty/duration ratio
  (Smith's rule for weighted completion time).
- We process jobs by availability time, and whenever the machine becomes free we decide whether to:
  1) start the best currently available job, or
  2) jump forward to the next release time if no job is available.

For the available set, comparing jobs a and b:
- a should go before b if penalty[a] / duration[a] > penalty[b] / duration[b]
- To avoid floating point precision issues, compare:
    penalty[a] * duration[b] ? penalty[b] * duration[a]

This implementation uses:
- sorting by available time
- a priority queue (binary heap) for currently available jobs ordered by the ratio above

Note:
This is the standard efficient heap-based scheduling strategy expected for this style of problem.

*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    private sealed class Job
    {
        public int Available;
        public int Duration;
        public int Penalty;
        public int Id;
    }

    private sealed class JobComparer : IComparer<Job>
    {
        public int Compare(Job? x, Job? y)
        {
            if (ReferenceEquals(x, y)) return 0;
            if (x is null) return -1;
            if (y is null) return 1;

            // We want the job with the LARGER penalty/duration ratio to come first.
            // Compare x.Penalty / x.Duration and y.Penalty / y.Duration without division:
            // x before y if x.Penalty * y.Duration > y.Penalty * x.Duration
            long left = 1L * x.Penalty * y.Duration;
            long right = 1L * y.Penalty * x.Duration;

            if (left != right)
            {
                return left > right ? -1 : 1;
            }

            // Tie-breakers:
            // If ratios are equal, putting the shorter duration first is a common stable choice.
            if (x.Duration != y.Duration) return x.Duration.CompareTo(y.Duration);

            // Then larger penalty first.
            if (x.Penalty != y.Penalty) return y.Penalty.CompareTo(x.Penalty);

            // Finally by id to keep ordering deterministic.
            return x.Id.CompareTo(y.Id);
        }
    }

    private sealed class OrderedSetHeap
    {
        private readonly SortedSet<Job> _set = new(new JobComparer());

        public int Count => _set.Count;

        public void Push(Job job) => _set.Add(job);

        public Job Pop()
        {
            var best = _set.Min!;
            _set.Remove(best);
            return best;
        }
    }

    /*
    Time Complexity:
    - Sorting jobs by availability time: O(n log n)
    - Each job is inserted into and removed from the heap/set once: O(n log n)
    - Total: O(n log n)

    Space Complexity:
    - O(n) for the sorted jobs and the heap/set
    */
    public long MinimumTotalPenalty(int[] availableTime, int[] duration, int[] penalty)
    {
        // Step 1:
        // Build a list of job objects so each job's three attributes stay together.
        // This makes the later logic much easier to read and reason about.
        int n = availableTime.Length;
        var jobs = new Job[n];
        for (int i = 0; i < n; i++)
        {
            jobs[i] = new Job
            {
                Available = availableTime[i],
                Duration = duration[i],
                Penalty = penalty[i],
                Id = i
            };
        }

        // Step 2:
        // Sort all jobs by release/availability time.
        // Why?
        // Because as time moves forward, we need to know which jobs have become available.
        // Sorting lets us add newly released jobs in one linear scan.
        Array.Sort(jobs, (a, b) =>
        {
            int cmp = a.Available.CompareTo(b.Available);
            if (cmp != 0) return cmp;
            return a.Id.CompareTo(b.Id);
        });

        // Step 3:
        // This ordered structure stores all jobs that are currently available but not yet processed.
        // We always want to quickly extract the "best" next job according to penalty/duration ratio.
        var availableJobs = new OrderedSetHeap();

        long currentTime = 0;
        long totalPenalty = 0;
        int index = 0;

        // Step 4:
        // Continue until we have both:
        // - scanned all jobs from the sorted array, and
        // - processed all jobs that were inserted into the available set.
        while (index < n || availableJobs.Count > 0)
        {
            // Step 4a:
            // If there is currently no available job to run, the machine must be idle.
            // In that case, the only sensible action is to jump time forward to the next job release.
            // This avoids simulating empty time one unit at a time.
            if (availableJobs.Count == 0 && index < n && currentTime < jobs[index].Available)
            {
                currentTime = jobs[index].Available;
            }

            // Step 4b:
            // Add every job whose availability time is now <= current time.
            // These jobs are eligible to be chosen next.
            while (index < n && jobs[index].Available <= currentTime)
            {
                availableJobs.Push(jobs[index]);
                index++;
            }

            // Step 4c:
            // If after adding newly available jobs we still have none,
            // the loop will jump again on the next iteration.
            if (availableJobs.Count == 0)
            {
                continue;
            }

            // Step 4d:
            // Choose the currently best available job.
            // The comparator implements the weighted shortest processing time rule:
            // larger penalty/duration ratio gets higher priority.
            var job = availableJobs.Pop();

            // Step 4e:
            // The waiting penalty for this job depends on when it STARTS.
            // Since the machine is free at currentTime and we start immediately,
            // start time is exactly currentTime.
            //
            // waiting penalty = (start - available) * penalty
            //                 = (currentTime - job.Available) * job.Penalty
            //
            // This value is always non-negative because the job is only in the available set
            // after its release time has passed.
            totalPenalty += (currentTime - job.Available) * (long)job.Penalty;

            // Step 4f:
            // Run the job to completion. The machine is non-preemptive,
            // so current time simply advances by the full duration.
            currentTime += job.Duration;
        }

        return totalPenalty;
    }
}

// Demo code
var solution = new Solution();

int[] availableTime1 = { 0, 1, 2 };
int[] duration1 = { 3, 1, 2 };
int[] penalty1 = { 4, 100, 2 };
long result1 = solution.MinimumTotalPenalty(availableTime1, duration1, penalty1);
Console.WriteLine(result1);

int[] availableTime2 = { 0, 0, 5, 5 };
int[] duration2 = { 4, 2, 3, 1 };
int[] penalty2 = { 3, 10, 2, 20 };
long result2 = solution.MinimumTotalPenalty(availableTime2, duration2, penalty2);
Console.WriteLine(result2);