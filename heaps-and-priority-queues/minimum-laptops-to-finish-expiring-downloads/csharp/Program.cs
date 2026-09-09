/*
Title: Minimum Laptops to Finish Expiring Downloads

Problem Description:
A company receives a list of download jobs for large software images. Job i becomes available at time start[i], requires duration[i] continuous minutes of download time on exactly one laptop, and must be fully completed no later than deadline[i]. A laptop can process at most one job at a time, but jobs may be assigned to different laptops independently. Once a laptop starts a job, that job cannot be paused or migrated.

Your task is to determine the minimum number of laptops required so that all jobs can be completed before their deadlines. If it is impossible even with unlimited laptops, return -1.

You are given three integer arrays start, duration, and deadline of equal length n, where each job is represented by the triple (start[i], duration[i], deadline[i]). A job may start at any integer time t such that t >= start[i] and t + duration[i] <= deadline[i]. Multiple jobs can become available at the same time, and deadlines are not sorted.

This is not a simple interval overlap problem: each job has a release time and a latest finishing time, so choosing which available jobs to run earlier can affect whether future jobs remain feasible. An efficient solution is expected for large inputs, and a heap-based scheduling strategy is likely necessary.

Key idea used in this solution:
- We binary search the answer k = number of laptops.
- For a fixed k, we must check whether all jobs can be scheduled on k identical laptops.
- The feasibility check is done with a heap-based event simulation:
  1. Sort jobs by release/start time.
  2. Move time forward through "interesting moments":
     - when new jobs become available
     - when some running job finishes
  3. Among all available-but-not-yet-started jobs, always start the jobs with the earliest deadlines first.
     This is the natural greedy rule for deadline scheduling.
  4. Before starting a job at current time t, we verify t + duration <= deadline.
     If not, then that job can never be completed anymore, so the schedule is impossible for this k.
  5. If at any time there are free laptops and waiting jobs, we immediately start as many as possible.
     Delaying a start while a laptop is idle can never help.

Why binary search works:
- If k laptops are enough, then any larger number of laptops is also enough.
- Therefore feasibility is monotonic, so binary search over k in [1, n] is valid.

If any single job has start + duration > deadline, then it is impossible even with unlimited laptops, so return -1 immediately.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private struct Job
    {
        public long Start;
        public long Duration;
        public long Deadline;

        public Job(long start, long duration, long deadline)
        {
            Start = start;
            Duration = duration;
            Deadline = deadline;
        }
    }

    /*
    Time Complexity:
    - Sorting jobs once: O(n log n)
    - Each feasibility check: O(n log n)
      because every job is inserted once into the waiting heap and started once,
      and every started job is inserted once into the running heap and removed once.
    - Binary search over number of laptops: O(log n) checks
    - Total: O(n log n log n)

    Space Complexity:
    - O(n) for the sorted jobs array and the heaps used during feasibility checks.
    */
    public int MinimumLaptops(int[] start, int[] duration, int[] deadline)
    {
        int n = start.Length;

        // Build a job array using long values.
        // We use long instead of int because times can be up to 1e9 and we may add
        // start + duration, which can exceed int range if done carelessly.
        var jobs = new Job[n];

        for (int i = 0; i < n; i++)
        {
            long s = start[i];
            long d = duration[i];
            long dl = deadline[i];

            // Immediate impossibility check:
            // Even with unlimited laptops, a job cannot start before its release time.
            // So the earliest possible finish time is start + duration.
            // If that is already after the deadline, no schedule can ever work.
            if (s + d > dl)
            {
                return -1;
            }

            jobs[i] = new Job(s, d, dl);
        }

        // Sort jobs by release/start time so we can add them to the waiting pool
        // exactly when they become available during the simulation.
        Array.Sort(jobs, (a, b) =>
        {
            int cmp = a.Start.CompareTo(b.Start);
            if (cmp != 0) return cmp;
            cmp = a.Deadline.CompareTo(b.Deadline);
            if (cmp != 0) return cmp;
            return a.Duration.CompareTo(b.Duration);
        });

        // Binary search for the minimum number of laptops.
        int left = 1;
        int right = n;
        int answer = n;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            // Check whether mid laptops are enough.
            if (CanScheduleWithK(jobs, mid))
            {
                answer = mid;
                right = mid - 1;
            }
            else
            {
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool CanScheduleWithK(Job[] jobs, int k)
    {
        int n = jobs.Length;

        // waiting:
        // Contains jobs that have been released (start <= currentTime) but not yet started.
        // Priority = earliest deadline first.
        //
        // Why earliest deadline first?
        // If multiple jobs are waiting, the one with the smallest deadline is the most urgent.
        // Starting a less urgent job first can cause the urgent one to miss its deadline.
        //
        // We store the full Job as the element, and use a tuple as priority:
        // (deadline, duration, start) to make ordering deterministic.
        var waiting = new PriorityQueue<Job, (long deadline, long duration, long start)>();

        // running:
        // Contains jobs currently executing on laptops.
        // Priority = earliest finish time first.
        //
        // This lets us quickly know when the next laptop becomes free.
        // We only need finish times here.
        var running = new PriorityQueue<long, long>();

        int i = 0;                  // index of next unreleased job in sorted order
        long currentTime = 0;       // simulation clock

        while (i < n || waiting.Count > 0 || running.Count > 0)
        {
            // Step 1:
            // If there is nothing waiting and no job currently running,
            // then the system is completely idle.
            // In that case, we should jump time directly to the next release time,
            // because nothing can happen before that.
            if (waiting.Count == 0 && running.Count == 0 && i < n)
            {
                currentTime = Math.Max(currentTime, jobs[i].Start);
            }

            // Step 2:
            // Add all jobs that have become available by currentTime into the waiting heap.
            // These jobs are now eligible to be started on any free laptop.
            while (i < n && jobs[i].Start <= currentTime)
            {
                Job job = jobs[i];
                waiting.Enqueue(job, (job.Deadline, job.Duration, job.Start));
                i++;
            }

            // Step 3:
            // Free all laptops whose jobs have finished by currentTime.
            // Those laptops are now available to start new waiting jobs.
            while (running.Count > 0 && running.Peek() <= currentTime)
            {
                running.Dequeue();
            }

            // Step 4:
            // While we still have free laptops and there are waiting jobs,
            // start jobs immediately.
            //
            // Why immediately?
            // If a laptop is idle and a job is available, delaying the start cannot improve
            // feasibility because:
            // - it only pushes completion later,
            // - it does not create any new advantage,
            // - and future jobs can still be handled when they arrive.
            //
            // So greedily filling free laptops right now is safe and natural.
            while (running.Count < k && waiting.Count > 0)
            {
                Job job = waiting.Dequeue();

                // We are about to start this job at currentTime.
                // Since jobs are non-preemptive, it will finish at currentTime + duration.
                long finish = currentTime + job.Duration;

                // Critical feasibility check:
                // If even starting now causes the job to miss its deadline,
                // then this job is already too late.
                //
                // Because we always pick the earliest deadline among waiting jobs,
                // if this most urgent job cannot be saved now, then no valid schedule exists
                // for this k.
                if (finish > job.Deadline)
                {
                    return false;
                }

                // Start the job and mark the laptop as busy until 'finish'.
                running.Enqueue(finish, finish);
            }

            // Step 5:
            // If there are still waiting jobs but all laptops are busy,
            // we must advance time to the next running job completion.
            //
            // That is the earliest moment when capacity may become available.
            if (waiting.Count > 0 && running.Count == k)
            {
                currentTime = running.Peek();
                continue;
            }

            // Step 6:
            // If there are no waiting jobs, then the next interesting event is either:
            // - the next job release time, or
            // - the next running job completion.
            //
            // We jump to the earlier of those two events.
            if (waiting.Count == 0)
            {
                long nextRelease = i < n ? jobs[i].Start : long.MaxValue;
                long nextFinish = running.Count > 0 ? running.Peek() : long.MaxValue;

                if (nextRelease == long.MaxValue && nextFinish == long.MaxValue)
                {
                    break;
                }

                currentTime = Math.Min(nextRelease, nextFinish);
                continue;
            }

            // Step 7:
            // If we reach here, it means:
            // - there are waiting jobs,
            // - there may still be free laptops or not,
            // but after the previous start loop we could not make more progress immediately.
            //
            // The only meaningful next event is the next finish time.
            if (running.Count > 0)
            {
                currentTime = running.Peek();
            }
            else if (i < n)
            {
                currentTime = jobs[i].Start;
            }
        }

        // If we processed all jobs without any deadline violation, then k laptops are enough.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] start1 = { 0, 1, 3 };
int[] duration1 = { 3, 2, 2 };
int[] deadline1 = { 4, 5, 7 };
int result1 = solution.MinimumLaptops(start1, duration1, deadline1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] start2 = { 0, 2, 2 };
int[] duration2 = { 5, 1, 1 };
int[] deadline2 = { 3, 4, 5 };
int result2 = solution.MinimumLaptops(start2, duration2, deadline2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity test
int[] start3 = { 0, 0, 0 };
int[] duration3 = { 2, 2, 2 };
int[] deadline3 = { 2, 2, 2 };
int result3 = solution.MinimumLaptops(start3, duration3, deadline3);
Console.WriteLine(result3); // Expected: 3