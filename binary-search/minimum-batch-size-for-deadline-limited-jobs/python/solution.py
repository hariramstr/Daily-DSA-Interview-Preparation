"""
Title: Minimum Batch Size for Deadline-Limited Jobs

Problem Description:
A data processing service must execute a list of jobs in the given order. The i-th job
contains jobs[i] records and must be fully processed no later than deadline[i], measured
in whole days from the start of processing. The service uses a fixed batch size B,
meaning it can process at most B records per day. If a job is not finished on one day,
its remaining records continue on the next day. Jobs cannot be reordered, and the service
starts job i+1 only after job i is complete.

Your task is to find the minimum integer batch size B such that every job finishes on or
before its corresponding deadline.

More formally, if prefix = jobs[0] + jobs[1] + ... + jobs[i], then job i finishes after
ceil(prefix / B) days. This value must be less than or equal to deadline[i] for every i.

Return the smallest possible B. You may assume that the deadlines are positive integers
and that a valid answer always exists.

Constraints:
- 1 <= jobs.length <= 200000
- 1 <= jobs[i] <= 1000000000
- 1 <= deadline[i] <= 1000000000
- deadline.length == jobs.length
- deadlines are not necessarily sorted, but they correspond to the jobs in input order
- A valid batch size always exists

Examples:
1) jobs = [5, 8, 6], deadline = [2, 4, 5]
   Cumulative work = [5, 13, 19]

   Check B = 4:
   - Job 0 finishes on ceil(5 / 4) = 2, deadline = 2 -> valid
   - Job 1 finishes on ceil(13 / 4) = 4, deadline = 4 -> valid
   - Job 2 finishes on ceil(19 / 4) = 5, deadline = 5 -> valid

   Check B = 3:
   - Job 0 finishes on ceil(5 / 3) = 2, deadline = 2 -> valid
   - Job 1 finishes on ceil(13 / 3) = 5, deadline = 4 -> invalid

   Therefore the minimum valid batch size is 4.

2) jobs = [7, 2, 9, 4], deadline = [1, 2, 5, 6]
   Cumulative work = [7, 9, 18, 22]

   Check B = 7:
   - Job 0 finishes on ceil(7 / 7) = 1, deadline = 1 -> valid
   - Job 1 finishes on ceil(9 / 7) = 2, deadline = 2 -> valid
   - Job 2 finishes on ceil(18 / 7) = 3, deadline = 5 -> valid
   - Job 3 finishes on ceil(22 / 7) = 4, deadline = 6 -> valid

   Check B = 6:
   - Job 0 finishes on ceil(7 / 6) = 2, deadline = 1 -> invalid

   Therefore the minimum valid batch size is 7.

Key Insight:
If a batch size B is sufficient, then any larger batch size is also sufficient.
That monotonic property allows binary search on the answer.
"""

from typing import List


class Solution:
    def _can_finish_all(self, jobs: List[int], deadline: List[int], batch_size: int) -> bool:
        """
        Check whether a given batch size is sufficient to satisfy all deadlines.

        Args:
            jobs: List of job sizes in processing order.
            deadline: List of deadlines corresponding to each job.
            batch_size: Candidate batch size to test.

        Returns:
            True if every job finishes on or before its deadline, otherwise False.

        Time complexity:
            O(n), where n is the number of jobs.

        Space complexity:
            O(1), excluding input storage.
        """
        # We maintain a running cumulative sum of all records that must be processed
        # up to the current job. This is exactly the "prefix" value described in the
        # problem statement.
        prefix_sum = 0

        # We scan jobs from left to right because jobs must be processed in the given
        # order and cannot be reordered.
        for i in range(len(jobs)):
            # Add the current job's records to the cumulative total.
            prefix_sum += jobs[i]

            # The finish day for job i is:
            #     ceil(prefix_sum / batch_size)
            #
            # In integer arithmetic, a standard way to compute ceil(a / b) for positive
            # integers is:
            #     (a + b - 1) // b
            #
            # This avoids floating-point operations and is both exact and efficient.
            finish_day = (prefix_sum + batch_size - 1) // batch_size

            # If the current job finishes after its deadline, then this batch size is
            # not sufficient. Because all jobs are required to meet their deadlines,
            # we can stop immediately and return False.
            if finish_day > deadline[i]:
                return False

        # If we finish the entire scan without finding any violation, then this batch
        # size works for every job.
        return True

    def minimum_batch_size(self, jobs: List[int], deadline: List[int]) -> int:
        """
        Find the minimum integer batch size that allows all jobs to meet deadlines.

        Args:
            jobs: List of job sizes in processing order.
            deadline: List of deadlines corresponding to each job.

        Returns:
            The smallest valid integer batch size.

        Time complexity:
            O(n log S), where n is the number of jobs and S is the search range
            of possible batch sizes. Here S can be bounded by sum(jobs).

        Space complexity:
            O(1), excluding input storage.
        """
        # We use binary search because the feasibility condition is monotonic:
        #
        # - If a batch size B works, then any larger batch size also works.
        # - If a batch size B does not work, then any smaller batch size also does not work.
        #
        # That means the valid answers form a suffix of the positive integers, and we
        # want the first value in that suffix.

        # The smallest possible batch size is 1, because batch size must be a positive integer.
        left = 1

        # A safe upper bound is the total amount of work. Why?
        # If batch_size = sum(jobs), then all work can be completed in at most 1 day,
        # so every job also finishes by day 1. Since the problem guarantees a valid
        # answer exists, this upper bound is sufficient.
        right = sum(jobs)

        # Binary search for the smallest feasible batch size.
        while left < right:
            # Choose the middle candidate.
            mid = (left + right) // 2

            # Test whether this candidate batch size is enough.
            if self._can_finish_all(jobs, deadline, mid):
                # If mid works, then the answer could be mid or something smaller.
                # So we keep the left half, including mid.
                right = mid
            else:
                # If mid does not work, then every value <= mid also does not work.
                # So we must search strictly to the right of mid.
                left = mid + 1

        # At the end of binary search, left == right and points to the smallest
        # feasible batch size.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    jobs1 = [5, 8, 6]
    deadline1 = [2, 4, 5]
    result1 = solution.minimum_batch_size(jobs1, deadline1)
    print("Example 1 result:", result1)  # Expected: 4

    # Example 2
    jobs2 = [7, 2, 9, 4]
    deadline2 = [1, 2, 5, 6]
    result2 = solution.minimum_batch_size(jobs2, deadline2)
    print("Example 2 result:", result2)  # Expected: 7

    # Additional quick sanity check
    jobs3 = [1, 1, 1]
    deadline3 = [1, 2, 3]
    result3 = solution.minimum_batch_size(jobs3, deadline3)
    print("Example 3 result:", result3)  # Expected: 1