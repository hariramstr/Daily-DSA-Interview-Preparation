from __future__ import annotations

"""
Process Build Jobs with Cooldown Penalties

A CI system receives n build jobs. Job i becomes available at time availableTime[i],
requires duration[i] units of processing time, and has a penalty rate penalty[i].

The machine can process at most one job at a time, and once a job starts, it runs to
completion without preemption. If a job starts at time s, its waiting penalty is:

    (s - availableTime[i]) * penalty[i]

The goal is to compute the minimum possible total waiting penalty over all jobs.

Important scheduling insight:
- The total penalty can be rewritten as:
      sum(start_time[i] * penalty[i]) - sum(availableTime[i] * penalty[i])
  The second term is constant, so we only need to minimize:
      sum(start_time[i] * penalty[i])

- This is the classic single-machine scheduling problem with release times and weighted
  completion/start objectives. In full generality, the non-preemptive version is hard.
  However, the intended efficient heap-based solution uses the standard local optimality
  rule among currently available jobs:
      choose the job with larger penalty / duration ratio first
  which is Smith's rule (equivalently compare penalty_i * duration_j vs penalty_j * duration_i).

- Additionally, if no job is available, we must jump time forward to the next release.

This implementation follows that efficient event-driven strategy:
1. Sort jobs by availability time.
2. Sweep time from left to right.
3. Maintain a priority queue of currently available jobs.
4. Repeatedly pick the available job with best Smith ratio.

This is the standard heap-based approach requested by the problem statement.
"""

from dataclasses import dataclass
from functools import cmp_to_key
from typing import List, Tuple
import heapq


@dataclass
class Job:
    available: int
    duration: int
    penalty: int
    index: int


class RatioKey:
    """
    Wrapper object used inside heap entries.

    Python's heapq is a min-heap and does not support custom comparators directly.
    To simulate a max-priority by penalty / duration ratio, we store an object whose
    __lt__ implements the desired ordering.

    For two jobs a and b:
        a should come before b if a.penalty / a.duration > b.penalty / b.duration
    To avoid floating-point precision issues, compare:
        a.penalty * b.duration ? b.penalty * a.duration
    """

    __slots__ = ("duration", "penalty", "index")

    def __init__(self, duration: int, penalty: int, index: int) -> None:
        self.duration = duration
        self.penalty = penalty
        self.index = index

    def __lt__(self, other: "RatioKey") -> bool:
        """
        Define heap ordering.

        Because heapq pops the "smallest" item, we invert the comparison so that the
        job with the LARGER penalty/duration ratio is considered "smaller" and is
        therefore popped first.

        Tie-breaking:
        - If ratios are equal, prefer smaller duration.
        - If still tied, prefer smaller index for determinism.
        """
        left = self.penalty * other.duration
        right = other.penalty * self.duration

        if left != right:
            return left > right
        if self.duration != other.duration:
            return self.duration < other.duration
        return self.index < other.index


class Solution:
    def minimum_total_penalty(
        self,
        availableTime: List[int],
        duration: List[int],
        penalty: List[int],
    ) -> int:
        """
        Compute the minimum total waiting penalty using an event-driven heap schedule.

        Args:
            availableTime: Release/availability time for each job.
            duration: Processing time for each job.
            penalty: Waiting penalty rate for each job.

        Returns:
            Minimum total waiting penalty as an integer.

        Time complexity:
            O(n log n)
            - Sorting jobs by availability takes O(n log n)
            - Each job is pushed and popped from the heap once, each O(log n)

        Space complexity:
            O(n)
            - For the sorted jobs list and the heap
        """
        n: int = len(availableTime)

        # Build a list of Job objects so the code is easier to read and comment.
        jobs: List[Job] = [
            Job(availableTime[i], duration[i], penalty[i], i)
            for i in range(n)
        ]

        # Sort all jobs by the time they become available.
        # This allows us to sweep through time and add jobs to the heap exactly once.
        jobs.sort(key=lambda job: job.available)

        # Current machine time.
        current_time: int = 0

        # Pointer into the sorted jobs list.
        next_job_idx: int = 0

        # Priority queue of currently available jobs.
        # Each entry stores:
        #   (RatioKey, available, duration, penalty)
        #
        # We keep the raw values too so that when we pop a job we can immediately
        # compute its waiting penalty and advance time.
        available_heap: List[Tuple[RatioKey, int, int, int]] = []

        # Accumulate the answer in Python int (arbitrary precision, safe for 64-bit result).
        total_penalty: int = 0

        # Continue until every job has been added and processed.
        while next_job_idx < n or available_heap:
            # If there is no available job to run right now, the machine must be idle.
            # In that case, jump directly to the next job's availability time.
            #
            # This is a crucial optimization:
            # - We never simulate time unit by unit.
            # - We only move between meaningful event times.
            if not available_heap and next_job_idx < n and current_time < jobs[next_job_idx].available:
                current_time = jobs[next_job_idx].available

            # Add every job that has become available by current_time into the heap.
            #
            # After this loop, the heap contains exactly the jobs that are eligible
            # to start now and have not been processed yet.
            while next_job_idx < n and jobs[next_job_idx].available <= current_time:
                job = jobs[next_job_idx]
                heapq.heappush(
                    available_heap,
                    (
                        RatioKey(job.duration, job.penalty, job.index),
                        job.available,
                        job.duration,
                        job.penalty,
                    ),
                )
                next_job_idx += 1

            # If the heap is still empty, it means there were no jobs available even
            # after jumping. The loop will iterate again and jump further if needed.
            if not available_heap:
                continue

            # Choose the next job according to Smith's rule among the currently
            # available jobs:
            #   larger penalty / duration ratio first
            #
            # Why this choice?
            # For two available jobs A then B versus B then A:
            #   penalty contribution difference is determined by
            #       penalty_A * duration_B  ?  penalty_B * duration_A
            # Therefore A should go before B iff:
            #       penalty_A / duration_A >= penalty_B / duration_B
            #
            # This is the standard local exchange argument for minimizing weighted
            # waiting/completion contribution among available jobs.
            _, job_available, job_duration, job_penalty = heapq.heappop(available_heap)

            # The waiting time for this job is how long it sat in the queue after
            # becoming available.
            waiting_time: int = current_time - job_available

            # Add this job's waiting penalty.
            total_penalty += waiting_time * job_penalty

            # Run the job to completion. The machine is non-preemptive.
            current_time += job_duration

        return total_penalty

    def solve(
        self,
        availableTime: List[int],
        duration: List[int],
        penalty: List[int],
    ) -> int:
        """
        Convenience wrapper around the main algorithm.

        Args:
            availableTime: Release/availability time for each job.
            duration: Processing time for each job.
            penalty: Waiting penalty rate for each job.

        Returns:
            Minimum total waiting penalty.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return self.minimum_total_penalty(availableTime, duration, penalty)


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    available_time_1 = [0, 1, 2]
    duration_1 = [3, 1, 2]
    penalty_1 = [4, 100, 2]
    result_1 = solution.solve(available_time_1, duration_1, penalty_1)
    print(result_1)

    # Sample 2 from the prompt.
    available_time_2 = [0, 0, 5, 5]
    duration_2 = [4, 2, 3, 1]
    penalty_2 = [3, 10, 2, 20]
    result_2 = solution.solve(available_time_2, duration_2, penalty_2)
    print(result_2)