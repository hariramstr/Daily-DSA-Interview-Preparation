"""
Title: Minimum Laptops to Finish Expiring Downloads

Problem Description:
A company receives a list of download jobs for large software images. Job i becomes
available at time start[i], requires duration[i] continuous minutes of download time
on exactly one laptop, and must be fully completed no later than deadline[i].
A laptop can process at most one job at a time, but jobs may be assigned to different
laptops independently. Once a laptop starts a job, that job cannot be paused or migrated.

Your task is to determine the minimum number of laptops required so that all jobs can
be completed before their deadlines. If it is impossible even with unlimited laptops,
return -1.

You are given three integer arrays start, duration, and deadline of equal length n,
where each job is represented by the triple (start[i], duration[i], deadline[i]).
A job may start at any integer time t such that:
    t >= start[i]
    t + duration[i] <= deadline[i]

Multiple jobs can become available at the same time, and deadlines are not sorted.

This is not a simple interval overlap problem: each job has a release time and a latest
finishing time, so choosing which available jobs to run earlier can affect whether future
jobs remain feasible. An efficient solution is expected for large inputs, and a heap-based
scheduling strategy is likely necessary.
"""

from typing import List, Tuple
import heapq


class Solution:
    def _can_schedule_with_k(
        self,
        jobs: List[Tuple[int, int, int]],
        k: int,
    ) -> bool:
        """
        Check whether all jobs can be scheduled on exactly k laptops.

        The method uses a classic event-driven greedy simulation:
        - Sort jobs by release/start time.
        - Maintain a min-heap of currently available but not yet scheduled jobs,
          ordered by deadline.
        - Maintain another min-heap of laptop availability times.
        - Repeatedly assign the earliest-available laptop to the available job with
          the earliest deadline whenever possible.
        - If a laptop becomes free before any future job is released and there is no
          available work, jump time forward to the next release.

        This greedy rule is the correct one for pre-release constrained non-preemptive
        scheduling on identical machines when testing feasibility: among currently
        available jobs, scheduling the earliest deadline first is the safest choice,
        because delaying a tighter-deadline job can only hurt feasibility.

        Args:
            jobs: List of tuples (start, duration, deadline).
            k: Number of laptops available.

        Returns:
            True if all jobs can be completed on k laptops, otherwise False.

        Time complexity:
            O(n log n + n log k + n log n) = O(n log n)

        Space complexity:
            O(n + k)
        """
        n: int = len(jobs)

        # This heap stores jobs that have already been released (their start time
        # has passed) but have not yet been assigned to any laptop.
        #
        # We order by deadline first because the greedy strategy is:
        # "Whenever a laptop is ready to start a job, run the available job with
        # the earliest deadline."
        #
        # Heap item format:
        #   (deadline, duration)
        available_jobs: List[Tuple[int, int]] = []

        # This heap stores the next time each laptop becomes free.
        #
        # Initially, all laptops are free at time 0.
        # Heap item format:
        #   free_time
        laptop_free_times: List[int] = [0] * k
        heapq.heapify(laptop_free_times)

        # Pointer into the sorted jobs list.
        job_index: int = 0

        # Number of jobs successfully assigned.
        scheduled_count: int = 0

        while scheduled_count < n:
            # The earliest time at which any laptop can start a new job.
            current_laptop_free_time: int = heapq.heappop(laptop_free_times)

            # Add every job whose release time is <= this laptop's free time.
            #
            # These jobs are now available to be started on this laptop.
            while job_index < n and jobs[job_index][0] <= current_laptop_free_time:
                start_time, duration, deadline = jobs[job_index]
                heapq.heappush(available_jobs, (deadline, duration))
                job_index += 1

            if available_jobs:
                # There is at least one available job.
                # Greedily choose the one with the earliest deadline.
                deadline, duration = heapq.heappop(available_jobs)

                # Start immediately when the laptop becomes free.
                finish_time: int = current_laptop_free_time + duration

                # If even this immediate start misses the deadline, then no schedule
                # using this greedy progression can succeed for k laptops.
                if finish_time > deadline:
                    return False

                # Put the laptop back with its new free time.
                heapq.heappush(laptop_free_times, finish_time)
                scheduled_count += 1
            else:
                # No currently available job exists for this laptop.
                #
                # There are two possibilities:
                # 1) There are future jobs not yet released:
                #    then this laptop simply waits until the next release time.
                # 2) There are no future jobs:
                #    then all remaining work must already have been scheduled.
                if job_index < n:
                    next_release_time: int = jobs[job_index][0]

                    # The laptop idles until the next job release.
                    # We do not schedule anything yet; we simply update the laptop's
                    # free time to that release time and continue.
                    heapq.heappush(laptop_free_times, next_release_time)
                else:
                    # No future jobs and no available jobs means all jobs are done.
                    heapq.heappush(laptop_free_times, current_laptop_free_time)
                    break

        return True

    def min_laptops(
        self,
        start: List[int],
        duration: List[int],
        deadline: List[int],
    ) -> int:
        """
        Compute the minimum number of laptops needed to schedule all jobs before deadlines.

        High-level strategy:
        1. First, reject immediately impossible jobs:
           if start[i] + duration[i] > deadline[i], that job can never finish on time,
           even with unlimited laptops.
        2. Sort jobs by release/start time.
        3. Binary search the answer k from 1 to n.
        4. For each candidate k, run a greedy heap-based feasibility test.

        Why binary search works:
        - If a set of jobs is feasible on k laptops, then it is also feasible on any
          larger number of laptops.
        - Therefore feasibility is monotonic, which is exactly what binary search needs.

        Args:
            start: Release times of jobs.
            duration: Processing times of jobs.
            deadline: Latest allowed finishing times of jobs.

        Returns:
            The minimum number of laptops required, or -1 if impossible.

        Time complexity:
            O(n log n log n)

        Space complexity:
            O(n)
        """
        n: int = len(start)

        # Build the job list and perform the first necessary impossibility check.
        #
        # If a single job cannot fit inside its own [start, deadline] window,
        # then no number of laptops can help, because the job itself is impossible.
        jobs: List[Tuple[int, int, int]] = []
        for s, p, d in zip(start, duration, deadline):
            if s + p > d:
                return -1
            jobs.append((s, p, d))

        # Sort by release time so we can sweep through time efficiently.
        jobs.sort(key=lambda job: job[0])

        # Binary search for the minimum feasible number of laptops.
        left: int = 1
        right: int = n
        answer: int = n

        while left <= right:
            mid: int = (left + right) // 2

            if self._can_schedule_with_k(jobs, mid):
                answer = mid
                right = mid - 1
            else:
                left = mid + 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    start1 = [0, 1, 3]
    duration1 = [3, 2, 2]
    deadline1 = [4, 5, 7]
    result1 = solution.min_laptops(start1, duration1, deadline1)
    print(result1)  # Expected: 2

    # Example 2
    start2 = [0, 2, 2]
    duration2 = [5, 1, 1]
    deadline2 = [3, 4, 5]
    result2 = solution.min_laptops(start2, duration2, deadline2)
    print(result2)  # Expected: -1