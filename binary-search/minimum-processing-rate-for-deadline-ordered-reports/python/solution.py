"""
Title: Minimum Processing Rate for Deadline Ordered Reports

Problem Description:
You are given a list of report jobs that must be processed in the given order.
The i-th job contains reports[i] pages and must be fully completed no later than
time deadlines[i]. A single processor works at a constant integer rate r pages
per hour. The processor may switch to the next job immediately after finishing
the current one, but jobs cannot be reordered, split across multiple processors,
or processed in parallel.

For a chosen rate r, the time needed for job i is ceil(reports[i] / r).
Because jobs must be processed sequentially, the completion time of each job is
the sum of the rounded-up processing times of all jobs from 0 to i.
A rate r is feasible if every job finishes by its corresponding deadline.

Return the minimum integer rate r such that all jobs can be completed on time,
or -1 if no integer rate can satisfy the deadlines.

This is not a simple per-job check: even if each job individually fits its own
deadline, earlier jobs may delay later ones. You need to exploit the monotonic
property of feasibility with respect to the processing rate and design an
efficient solution.
"""

from typing import List


class Solution:
    def _is_feasible(self, reports: List[int], deadlines: List[int], rate: int) -> bool:
        """
        Check whether a given processing rate allows all jobs to finish by deadline.

        For each job, we compute its processing time as ceil(reports[i] / rate).
        Since jobs must be processed in order, we maintain a running cumulative
        completion time. If at any point the cumulative time exceeds the current
        job's deadline, the rate is not feasible.

        Args:
            reports: List of page counts for each job.
            deadlines: List of required completion times for each job.
            rate: Candidate integer processing rate in pages per hour.

        Returns:
            True if every job can be completed by its deadline at this rate,
            otherwise False.

        Time complexity:
            O(n), where n is the number of jobs.

        Space complexity:
            O(1), excluding input storage.
        """
        cumulative_time: int = 0

        # We process jobs strictly in the given order.
        # This loop simulates the exact schedule produced by the chosen rate.
        for pages, deadline in zip(reports, deadlines):
            # Compute ceil(pages / rate) using integer arithmetic.
            # This avoids floating-point operations and is both faster and exact.
            job_time: int = (pages + rate - 1) // rate

            # Add this job's duration to the total time spent so far.
            cumulative_time += job_time

            # If the completion time of the current job is already beyond its
            # deadline, then this rate fails immediately. We can stop early.
            if cumulative_time > deadline:
                return False

        # If we never violated any deadline, the rate is feasible.
        return True

    def minimum_processing_rate(self, reports: List[int], deadlines: List[int]) -> int:
        """
        Find the minimum integer processing rate that satisfies all deadlines.

        The key observation is monotonicity:
        - If a rate r is feasible, then any larger rate is also feasible.
        - If a rate r is not feasible, then any smaller rate is also not feasible.

        Because of this monotonic behavior, we can binary search the answer over
        the range of possible rates.

        Before binary search, we perform an impossibility check:
        even with an extremely large rate, each non-empty job still takes at least
        1 hour due to ceiling rounding. Therefore, the best possible cumulative
        completion times are 1, 2, 3, ..., n. If any deadline is smaller than its
        minimum possible completion time, the schedule is impossible for all rates.

        Args:
            reports: List of page counts for each job.
            deadlines: List of required completion times for each job.

        Returns:
            The minimum feasible integer rate, or -1 if no such rate exists.

        Time complexity:
            O(n log M), where:
            - n is the number of jobs
            - M is the search bound on the answer (here up to 10^12)

        Space complexity:
            O(1), excluding input storage.
        """
        n: int = len(reports)

        # ------------------------------------------------------------
        # Step 1: Quick impossibility check.
        #
        # No matter how large the rate becomes, each job with at least 1 page
        # still needs at least 1 hour because ceil(positive / huge) = 1.
        #
        # Therefore, the earliest possible completion times are:
        # job 0 -> 1
        # job 1 -> 2
        # job 2 -> 3
        # ...
        # job i -> i + 1
        #
        # If any deadline[i] < i + 1, then even the theoretically fastest
        # schedule cannot satisfy that job's deadline.
        # ------------------------------------------------------------
        for i in range(n):
            if deadlines[i] < i + 1:
                return -1

        # ------------------------------------------------------------
        # Step 2: Binary search over the answer.
        #
        # The problem statement guarantees that if an answer exists, it is at
        # most 10^12. So we search in the inclusive range [1, 10^12].
        #
        # We want the minimum feasible rate, so this is a classic "first True"
        # binary search on a monotonic predicate.
        # ------------------------------------------------------------
        left: int = 1
        right: int = 10**12

        while left < right:
            # Midpoint of current search interval.
            mid: int = (left + right) // 2

            # --------------------------------------------------------
            # If mid is feasible, then the answer is <= mid.
            # So we keep the left half, including mid.
            #
            # If mid is not feasible, then the answer must be > mid.
            # So we discard mid and keep the right half.
            # --------------------------------------------------------
            if self._is_feasible(reports, deadlines, mid):
                right = mid
            else:
                left = mid + 1

        # ------------------------------------------------------------
        # Step 3: Verify the final candidate.
        #
        # In theory, after the impossibility check and binary search over a valid
        # guaranteed range, left should be the minimum feasible rate if one exists.
        # We still verify it for safety and clarity.
        # ------------------------------------------------------------
        if self._is_feasible(reports, deadlines, left):
            return left

        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    reports_1: List[int] = [8, 5, 10]
    deadlines_1: List[int] = [2, 4, 7]
    result_1: int = solution.minimum_processing_rate(reports_1, deadlines_1)
    print("Example 1 Result:", result_1)  # Expected: 4

    # Example 2
    reports_2: List[int] = [9, 9, 9]
    deadlines_2: List[int] = [1, 2, 2]
    result_2: int = solution.minimum_processing_rate(reports_2, deadlines_2)
    print("Example 2 Result:", result_2)  # Expected: -1

    # Additional sanity checks
    reports_3: List[int] = [1]
    deadlines_3: List[int] = [1]
    result_3: int = solution.minimum_processing_rate(reports_3, deadlines_3)
    print("Additional Test 1 Result:", result_3)  # Expected: 1

    reports_4: List[int] = [100, 100]
    deadlines_4: List[int] = [1, 2]
    result_4: int = solution.minimum_processing_rate(reports_4, deadlines_4)
    print("Additional Test 2 Result:", result_4)  # Expected: 100