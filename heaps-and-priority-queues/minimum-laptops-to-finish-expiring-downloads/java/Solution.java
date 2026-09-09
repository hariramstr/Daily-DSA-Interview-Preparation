import java.util.*;

/*
Problem Title: Minimum Laptops to Finish Expiring Downloads

Problem Description:
A company receives a list of download jobs for large software images. Job i becomes available at time start[i], requires duration[i] continuous minutes of download time on exactly one laptop, and must be fully completed no later than deadline[i]. A laptop can process at most one job at a time, but jobs may be assigned to different laptops independently. Once a laptop starts a job, that job cannot be paused or migrated.

Your task is to determine the minimum number of laptops required so that all jobs can be completed before their deadlines. If it is impossible even with unlimited laptops, return -1.

You are given three integer arrays start, duration, and deadline of equal length n, where each job is represented by the triple (start[i], duration[i],deadline[i]). A job may start at any integer time t such that t >= start[i] and t + duration[i] <= deadline[i]. Multiple jobs can become available at the same time, and deadlines are not sorted.

This is not a simple interval overlap problem: each job has a release time and a latest finishing time, so choosing which available jobs to run earlier can affect whether future jobs remain feasible. An efficient solution is expected for large inputs, and a heap-based scheduling strategy is likely necessary.

Constraints:
- 1 <= n <= 200000
- 0 <= start[i] <= 10^9
- 1 <= duration[i] <= 10^9
- 0 <= deadline[i] <= 10^9
- Arrays start, duration, and deadline all have length n

Example 1:
Input: start = [0, 1, 3], duration = [3, 2, 2], deadline = [4, 5, 7]
Output: 2

Example 2:
Input: start = [0, 2, 2], duration = [5, 1, 1], deadline = [3, 4, 5]
Output: -1
*/

public class Solution {

    /**
     * Simple immutable job record used by the algorithm.
     */
    private static class Job {
        long start;
        long duration;
        long deadline;

        Job(long start, long duration, long deadline) {
            this.start = start;
            this.duration = duration;
            this.deadline = deadline;
        }
    }

    /**
     * Event used during the feasibility simulation for a fixed number of laptops.
     * We process two kinds of events:
     * 1) RELEASE: a job becomes available and enters the waiting heap
     * 2) FINISH: a laptop finishes its current job and becomes free
     */
    private static class Event {
        long time;
        int type; // 0 = FINISH, 1 = RELEASE
        Job job;

        Event(long time, int type, Job job) {
            this.time = time;
            this.type = type;
            this.job = job;
        }
    }

    /**
     * Computes the minimum number of laptops needed so that all jobs can be completed
     * within their release times and deadlines. If the full set of jobs is impossible
     * even with unlimited laptops, returns -1.
     *
     * Core idea:
     * - First reject any individually impossible job: start[i] + duration[i] > deadline[i].
     * - Then binary search the answer k from 1..n.
     * - For each candidate k, test feasibility using a heap-based event simulation:
     *   whenever laptops are free, always start the available job with the earliest deadline.
     *   This earliest-deadline-first rule is the correct greedy choice for minimizing the
     *   risk of missing deadlines among currently available jobs.
     *
     * @param start release times of jobs
     * @param duration processing times of jobs
     * @param deadline latest finish times of jobs
     * @return minimum number of laptops required, or -1 if impossible
     *
     * Time complexity: O(n log^2 n), because we binary search over k and each feasibility
     *                  check runs in O(n log n)
     * Space complexity: O(n)
     */
    public int minimumLaptops(int[] start, int[] duration, int[] deadline) {
        int n = start.length;

        Job[] jobs = buildJobs(start, duration, deadline);

        // Immediate impossibility check:
        // Even with unlimited laptops, each job must at least fit inside its own window.
        for (Job job : jobs) {
            if (job.start + job.duration > job.deadline) {
                return -1;
            }
        }

        // Binary search the minimum feasible number of laptops.
        int left = 1;
        int right = n;
        int answer = n;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (canScheduleWithK(jobs, mid)) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Builds the internal Job array from the three input arrays.
     *
     * @param start release times
     * @param duration processing times
     * @param deadline latest finish times
     * @return array of Job objects
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public Job[] buildJobs(int[] start, int[] duration, int[] deadline) {
        int n = start.length;
        Job[] jobs = new Job[n];
        for (int i = 0; i < n; i++) {
            jobs[i] = new Job(start[i], duration[i], deadline[i]);
        }
        return jobs;
    }

    /**
     * Checks whether all jobs can be scheduled on exactly k laptops.
     *
     * Detailed strategy:
     * 1) Sort all jobs by release time.
     * 2) Simulate time using an event queue containing:
     *    - future job releases
     *    - future laptop finishes
     * 3) Maintain:
     *    - freeLaptops: how many laptops are currently idle
     *    - waiting heap ordered by earliest deadline first
     * 4) At every event time:
     *    - process all finishes at that time (freeing laptops)
     *    - process all releases at that time (adding jobs to waiting heap)
     *    - repeatedly assign free laptops to waiting jobs with earliest deadlines
     * 5) Before assigning, if the earliest-deadline waiting job can no longer finish
     *    by its deadline even when started now, scheduling is impossible.
     * 6) After all events, if any waiting job remains, try to run them immediately on
     *    free laptops using the same earliest-deadline rule.
     *
     * Why earliest deadline first among available jobs?
     * Because when several jobs are ready and a laptop is free, delaying the most urgent
     * deadline can only make feasibility harder. This is the standard exchange argument
     * behind EDF-style scheduling on identical machines in this event-driven setting.
     *
     * @param jobs all jobs
     * @param k number of laptops to test
     * @return true if all jobs can be completed with k laptops, false otherwise
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public boolean canScheduleWithK(Job[] jobs, int k) {
        int n = jobs.length;

        // Sort jobs by release time so we can create release events in chronological order.
        Job[] sorted = jobs.clone();
        Arrays.sort(sorted, Comparator.comparingLong(a -> a.start));

        // Event queue ordered by time.
        // If multiple events happen at the same time, FINISH is processed before RELEASE.
        // This is safe and convenient because a laptop finishing at time t is immediately
        // available for a job released at the same time t.
        PriorityQueue<Event> events = new PriorityQueue<>((a, b) -> {
            if (a.time != b.time) {
                return Long.compare(a.time, b.time);
            }
            return Integer.compare(a.type, b.type);
        });

        // Insert all release events.
        for (Job job : sorted) {
            events.offer(new Event(job.start, 1, job));
        }

        // Waiting jobs are ordered by earliest deadline first.
        // If deadlines tie, shorter duration first is a harmless tie-breaker.
        PriorityQueue<Job> waiting = new PriorityQueue<>((a, b) -> {
            if (a.deadline != b.deadline) {
                return Long.compare(a.deadline, b.deadline);
            }
            if (a.duration != b.duration) {
                return Long.compare(a.duration, b.duration);
            }
            return Long.compare(a.start, b.start);
        });

        int freeLaptops = k;

        // Process all events in chronological order.
        while (!events.isEmpty()) {
            long currentTime = events.peek().time;

            // Step 1: process every event at currentTime.
            while (!events.isEmpty() && events.peek().time == currentTime) {
                Event event = events.poll();

                if (event.type == 0) {
                    // A laptop has just finished a job.
                    freeLaptops++;
                } else {
                    // A job has just become available.
                    waiting.offer(event.job);
                }
            }

            // Step 2: before assigning, check whether some waiting job is already doomed.
            // Since waiting is ordered by earliest deadline, if the top job is impossible
            // to start now, then no schedule can recover.
            while (!waiting.isEmpty() && waiting.peek().deadline < currentTime + waiting.peek().duration) {
                return false;
            }

            // Step 3: assign as many waiting jobs as possible to currently free laptops.
            // Always choose the waiting job with the earliest deadline.
            while (freeLaptops > 0 && !waiting.isEmpty()) {
                Job job = waiting.poll();

                // If even starting now misses the deadline, impossible.
                if (currentTime + job.duration > job.deadline) {
                    return false;
                }

                freeLaptops--;

                long finishTime = currentTime + job.duration;
                events.offer(new Event(finishTime, 0, null));
            }
        }

        // If events are exhausted, there should not be any waiting jobs left.
        // In a correct simulation, this queue should already be empty.
        // Still, we handle it defensively.
        if (!waiting.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Convenience wrapper matching a common interview-style signature.
     *
     * @param start release times of jobs
     * @param duration processing times of jobs
     * @param deadline latest finish times of jobs
     * @return minimum number of laptops required, or -1 if impossible
     *
     * Time complexity: O(n log^2 n)
     * Space complexity: O(n)
     */
    public int solve(int[] start, int[] duration, int[] deadline) {
        return minimumLaptops(start, duration, deadline);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding the called solver
     * Space complexity: O(1) for the demonstration itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] start1 = {0, 1, 3};
        int[] duration1 = {3, 2, 2};
        int[] deadline1 = {4, 5, 7};
        System.out.println(solution.minimumLaptops(start1, duration1, deadline1)); // Expected: 2

        int[] start2 = {0, 2, 2};
        int[] duration2 = {5, 1, 1};
        int[] deadline2 = {3, 4, 5};
        System.out.println(solution.minimumLaptops(start2, duration2, deadline2)); // Expected: -1
    }
}