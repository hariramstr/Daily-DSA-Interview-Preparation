"""
Title: Minimum Peak Load Limit for Batch Servers

Problem Description:
You are given an array jobs where jobs[i] is the processing time of the i-th job
in a fixed arrival order, and an integer m representing the number of servers
available. Each server must process one contiguous block of jobs, and every job
must be assigned to exactly one server. Some servers may remain unused, but the
order of jobs cannot be changed.

Your task is to compute the minimum possible peak load L such that all jobs can
be partitioned into at most m contiguous groups, where the sum of each group is
at most L.

However, there is an additional deployment rule: every used server must receive
at least one job, and if a partition is possible for a candidate load L, it is
considered valid as long as the number of groups used is between 1 and m
inclusive.

Return the smallest integer L for which such an assignment exists.

This is a realistic scheduling problem where preserving order matters, and the
answer is not the partition itself but the minimum worst-case server load. A
brute-force search over all partitions is too slow for large inputs, so you must
exploit the monotonic nature of feasibility with respect to L.

Constraints:
- 1 <= jobs.length <= 200000
- 1 <= jobs[i] <= 1000000000
- 1 <= m <= jobs.length
- The answer fits in a 64-bit signed integer.
"""

from typing import List


class Solution:
    def can_split(self, jobs: List[int], m: int, limit: int) -> bool:
        """
        Check whether all jobs can be partitioned into at most m contiguous groups
        such that each group's sum is at most the given limit.

        Args:
            jobs: List of job processing times in fixed order.
            m: Maximum number of allowed groups/servers.
            limit: Candidate maximum allowed sum for any group.

        Returns:
            True if a valid partition exists using between 1 and m groups inclusive,
            otherwise False.

        Time complexity:
            O(n), where n is the number of jobs.

        Space complexity:
            O(1), excluding input storage.
        """
        # We greedily build groups from left to right.
        #
        # Why greedy works:
        # - For a fixed limit, the best way to minimize the number of groups is to
        #   keep adding jobs to the current group until adding one more would exceed
        #   the limit.
        # - If even this "pack as much as possible" strategy needs more than m groups,
        #   then no other partition can do better under the same limit.
        #
        # This gives us a fast feasibility test for binary search.

        groups_used: int = 1
        current_sum: int = 0

        for job in jobs:
            # If a single job is larger than the candidate limit, then it is
            # impossible to place that job into any group at all.
            if job > limit:
                return False

            # If adding this job would exceed the limit, we must start a new group.
            if current_sum + job > limit:
                groups_used += 1
                current_sum = job

                # Early stop:
                # If we already need more than m groups, this limit is not feasible.
                if groups_used > m:
                    return False
            else:
                # Otherwise, safely extend the current group.
                current_sum += job

        # If we finish with groups_used <= m, then the partition is valid.
        # The problem allows using fewer than m servers, so this is enough.
        return True

    def minimum_peak_load(self, jobs: List[int], m: int) -> int:
        """
        Compute the minimum possible peak load such that jobs can be split into
        at most m contiguous groups.

        Args:
            jobs: List of job processing times in fixed order.
            m: Maximum number of available servers/groups.

        Returns:
            The smallest integer peak load that allows a valid partition.

        Time complexity:
            O(n log S), where n is the number of jobs and S is the search range
            between max(jobs) and sum(jobs).

        Space complexity:
            O(1), excluding input storage.
        """
        # Binary search boundaries:
        #
        # Lower bound:
        # - At minimum, the peak load must be at least the largest single job,
        #   because every job must belong to some group.
        #
        # Upper bound:
        # - At maximum, one server can process all jobs, so sum(jobs) is always
        #   a valid peak load.
        left: int = max(jobs)
        right: int = sum(jobs)

        # We now binary search for the smallest feasible limit.
        #
        # Monotonic property:
        # - If a limit L is feasible, then any larger limit is also feasible.
        # - If a limit L is not feasible, then any smaller limit is also not feasible.
        #
        # This "False ... False, True ... True" pattern is exactly what binary
        # search needs.
        while left < right:
            mid: int = left + (right - left) // 2

            # Test whether this candidate limit works.
            if self.can_split(jobs, m, mid):
                # mid is feasible, so the answer could be mid or smaller.
                right = mid
            else:
                # mid is not feasible, so we must search larger values.
                left = mid + 1

        # When left == right, we have found the smallest feasible limit.
        return left

    def splitArray(self, jobs: List[int], m: int) -> int:
        """
        Public interface matching a common interview/online-judge naming style.

        Args:
            jobs: List of job processing times in fixed order.
            m: Maximum number of available servers/groups.

        Returns:
            The minimum possible peak load.

        Time complexity:
            O(n log S), where n is the number of jobs and S is the numeric search range.

        Space complexity:
            O(1), excluding input storage.
        """
        return self.minimum_peak_load(jobs, m)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    jobs1: List[int] = [7, 2, 5, 10, 8]
    m1: int = 2
    result1: int = solution.splitArray(jobs1, m1)
    print("Example 1 Result:", result1)  # Expected: 18

    # Example 2
    jobs2: List[int] = [1, 4, 4, 3, 2]
    m2: int = 3
    result2: int = solution.splitArray(jobs2, m2)
    print("Example 2 Result:", result2)  # Expected: 5

    # Additional quick sanity checks
    jobs3: List[int] = [5]
    m3: int = 1
    result3: int = solution.splitArray(jobs3, m3)
    print("Single Job Result:", result3)  # Expected: 5

    jobs4: List[int] = [1, 2, 3, 4, 5]
    m4: int = 5
    result4: int = solution.splitArray(jobs4, m4)
    print("One Job Per Server Result:", result4)  # Expected: 5