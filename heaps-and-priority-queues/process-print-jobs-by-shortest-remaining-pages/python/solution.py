"""
Title: Process Print Jobs by Shortest Remaining Pages

Problem Description:
A shared office printer receives print jobs throughout the day. Each job is described
by two integers: arrivalTime and pages. The printer can work on at most one job at a
time, and once it starts a job, it prints the entire job before switching to another
one. If multiple jobs are available, the printer always chooses the job with the fewest
pages remaining to minimize average waiting time. If there is a tie, choose the job
with the smaller original index.

You are given a 0-indexed array jobs where jobs[i] = [arrivalTime_i, pages_i].
Return the order of job indices in which the printer processes the jobs.

The printer may be idle if no jobs have arrived yet. When the printer becomes free,
it may only choose among jobs whose arrivalTime is less than or equal to the current
time. A job that arrives while another job is being printed must wait until the current
job finishes.

Your task is to simulate this scheduling policy efficiently.

Constraints:
- 1 <= jobs.length <= 100000
- 0 <= arrivalTime_i <= 10^9
- 1 <= pages_i <= 10^9
- All jobs fit in 64-bit signed integer time calculations.

Example 1:
Input: jobs = [[1,4],[2,3],[3,1],[10,2]]
Output: [0,2,1,3]

Example 2:
Input: jobs = [[0,5],[0,2],[0,2],[1,1]]
Output: [1,2,3,0]
"""

from heapq import heappop, heappush
from typing import List, Tuple


class Solution:
    def get_processing_order(self, jobs: List[List[int]]) -> List[int]:
        """
        Simulate the printer scheduling policy and return the processing order.

        The algorithm first attaches each job's original index, then sorts all jobs
        by arrival time so we can add them to a min-heap in chronological order.
        The heap stores currently available jobs and always lets us extract the job
        with the fewest pages; ties are broken by original index.

        Args:
            jobs: A list where jobs[i] = [arrivalTime_i, pages_i].

        Returns:
            A list of original job indices in the exact order they are processed.

        Time complexity:
            O(n log n), where n is the number of jobs.
            - Sorting takes O(n log n)
            - Each job is pushed and popped from the heap once, each O(log n)

        Space complexity:
            O(n), for the sorted job list, heap, and output list.
        """
        # Step 1:
        # Attach the original index to every job.
        #
        # Why do we need the original index?
        # Because the answer must return the order of original job indices.
        # Also, when two available jobs have the same number of pages, the problem
        # says we must choose the one with the smaller original index.
        #
        # Each tuple will look like:
        # (arrival_time, pages, original_index)
        indexed_jobs: List[Tuple[int, int, int]] = [
            (arrival_time, pages, index)
            for index, (arrival_time, pages) in enumerate(jobs)
        ]

        # Step 2:
        # Sort jobs by arrival time.
        #
        # This allows us to scan through jobs from earliest arrival to latest arrival.
        # As time moves forward, we can add every newly available job into the heap.
        indexed_jobs.sort(key=lambda job: job[0])

        # This heap will store jobs that have already arrived and are waiting to be printed.
        #
        # Heap entry format:
        # (pages, original_index, arrival_time)
        #
        # Why this order?
        # - pages first: because we want the shortest job first
        # - original_index second: because ties in pages are broken by smaller index
        # - arrival_time third: not required for ordering, but useful to keep complete info
        available_jobs: List[Tuple[int, int, int]] = []

        # This will store the final processing order.
        processing_order: List[int] = []

        # Pointer into the sorted job list.
        # It tells us which jobs have not yet been added to the heap.
        job_pointer: int = 0

        # Current simulated time.
        #
        # We start at time 0, but if the first job arrives later, the algorithm will
        # jump forward to that arrival time when needed.
        current_time: int = 0

        total_jobs: int = len(indexed_jobs)

        # Step 3:
        # Continue until we have processed every job.
        #
        # There are two sources of unfinished work:
        # 1. Jobs not yet added from the sorted list
        # 2. Jobs already added to the heap but not yet processed
        while job_pointer < total_jobs or available_jobs:
            # Step 3A:
            # If no job is currently available, the printer must be idle.
            #
            # In that case, we should jump time directly to the next job's arrival.
            # This is much more efficient than increasing time one unit at a time.
            if not available_jobs and job_pointer < total_jobs:
                current_time = max(current_time, indexed_jobs[job_pointer][0])

            # Step 3B:
            # Add every job whose arrival time is <= current_time into the heap.
            #
            # These are exactly the jobs that are available for selection now.
            while (
                job_pointer < total_jobs
                and indexed_jobs[job_pointer][0] <= current_time
            ):
                arrival_time, pages, original_index = indexed_jobs[job_pointer]

                # Push into heap using the scheduling priority:
                # shortest pages first, then smaller original index.
                heappush(available_jobs, (pages, original_index, arrival_time))
                job_pointer += 1

            # Step 3C:
            # If the heap is not empty, choose the best available job.
            if available_jobs:
                pages, original_index, _arrival_time = heappop(available_jobs)

                # Record the chosen job's original index in the answer.
                processing_order.append(original_index)

                # The printer is non-preemptive:
                # once it starts a job, it prints the entire job before switching.
                #
                # So we simply advance current_time by the full number of pages.
                current_time += pages

        return processing_order

    def getProcessingOrder(self, jobs: List[List[int]]) -> List[int]:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            jobs: A list where jobs[i] = [arrivalTime_i, pages_i].

        Returns:
            A list of original job indices in processing order.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return self.get_processing_order(jobs)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    jobs1: List[List[int]] = [[1, 4], [2, 3], [3, 1], [10, 2]]
    result1: List[int] = solution.get_processing_order(jobs1)
    print("Example 1 Input:", jobs1)
    print("Example 1 Output:", result1)
    print("Expected:", [0, 2, 1, 3])
    print()

    # Example 2
    jobs2: List[List[int]] = [[0, 5], [0, 2], [0, 2], [1, 1]]
    result2: List[int] = solution.get_processing_order(jobs2)
    print("Example 2 Input:", jobs2)
    print("Example 2 Output:", result2)
    print("Expected:", [1, 2, 3, 0])
    print()

    # Additional quick sanity test
    jobs3: List[List[int]] = [[5, 2]]
    result3: List[int] = solution.get_processing_order(jobs3)
    print("Additional Test Input:", jobs3)
    print("Additional Test Output:", result3)
    print("Expected:", [0])