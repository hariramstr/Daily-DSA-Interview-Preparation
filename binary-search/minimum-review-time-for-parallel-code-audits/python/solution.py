"""
Title: Minimum Review Time for Parallel Code Audits

Problem Description:
A company needs to review a large set of code changes before a release. There are n code changes,
and the i-th change requires reviewWork[i] units of work. The company has m senior reviewers.
Reviewer j can review at speed speed[j], meaning they can complete speed[j] units of work per hour.
A single code change cannot be split across multiple reviewers, but each reviewer may review any
number of code changes, one after another.

You may assign the code changes in any order to any reviewers. The total release review time is
the maximum total time spent by any single reviewer. Return the minimum possible release review
time needed to finish reviewing all code changes.

Formally, if a reviewer is assigned changes with total work W, that reviewer needs ceil(W / speed[j])
hours. You want to partition all code changes among the m reviewers so that the maximum reviewer
completion time is minimized.

This is a decision-and-optimization problem: for a candidate time T, determine whether it is possible
to assign every code change to some reviewer such that each assigned reviewer finishes within T hours.
Then compute the minimum feasible T.
"""

from typing import List


class Solution:
    def minimum_review_time(self, reviewWork: List[int], speed: List[int]) -> int:
        """
        Compute the minimum possible completion time using binary search on the answer.

        Args:
            reviewWork: List of indivisible task sizes.
            speed: List of reviewer speeds.

        Returns:
            The minimum feasible integer time.

        Time complexity:
            O((n + m) log U), where U is the search range of the answer.

        Space complexity:
            O(n + m) due to sorted copies of the input.
        """
        # We sort tasks in descending order.
        # Why?
        # - Large tasks are the hardest to place.
        # - When checking feasibility for a candidate time T, if the largest tasks fit,
        #   the smaller tasks are easier to place.
        # - This ordering enables a greedy feasibility check based on "largest remaining task".
        tasks: List[int] = sorted(reviewWork, reverse=True)

        # We sort reviewer speeds in descending order as well.
        # Why?
        # - Faster reviewers have larger capacities for a fixed time T.
        # - Processing larger capacities first is natural for the greedy check.
        speeds: List[int] = sorted(speed, reverse=True)

        # Lower bound:
        # At minimum, the time must be enough so that the largest task can fit on
        # at least one reviewer. Since the fastest reviewer is best for the largest task,
        # a valid lower bound is ceil(max_task / max_speed).
        max_task: int = tasks[0]
        max_speed: int = speeds[0]
        left: int = (max_task + max_speed - 1) // max_speed

        # Upper bound:
        # A simple always-feasible bound is to let the fastest reviewer do all tasks.
        # Then required time is ceil(total_work / max_speed).
        total_work: int = sum(tasks)
        right: int = (total_work + max_speed - 1) // max_speed

        # Standard binary search on the minimum feasible time.
        while left < right:
            mid: int = (left + right) // 2

            # If we can finish within 'mid' hours, try smaller time.
            if self._can_finish_in_time(tasks, speeds, mid):
                right = mid
            else:
                # Otherwise we need more time.
                left = mid + 1

        return left

    def _can_finish_in_time(self, tasks: List[int], speeds: List[int], time_limit: int) -> bool:
        """
        Check whether all tasks can be assigned within the given time limit.

        This method uses a greedy strategy:
        - For each reviewer, compute capacity = speed * time_limit.
        - Repeatedly assign the largest remaining task that fits into that capacity.
        - Continue until no more tasks fit for that reviewer, then move to the next reviewer.

        This greedy check is correct for this problem because:
        - Tasks are indivisible.
        - Reviewers only differ by total capacity under a fixed time limit.
        - The only constraint for a reviewer is that the sum of assigned task sizes
          does not exceed capacity.
        - Assigning the largest remaining fitting task first is the safest way to avoid
          leaving a large task stranded later.

        Args:
            tasks: Task sizes sorted in descending order.
            speeds: Reviewer speeds sorted in descending order.
            time_limit: Candidate maximum completion time.

        Returns:
            True if assignment is possible, otherwise False.

        Time complexity:
            O(n + m) amortized after sorting, because each task is processed once.

        Space complexity:
            O(n) for the frequency-style linked structure used to skip assigned tasks.
        """
        n: int = len(tasks)

        # Quick impossible check:
        # If the largest task cannot fit even on the fastest reviewer, we can immediately fail.
        if tasks[0] > speeds[0] * time_limit:
            return False

        # Another quick necessary condition:
        # Total capacity across all reviewers must be at least total work.
        # This is not sufficient by itself because tasks are indivisible,
        # but it is a very useful early rejection.
        total_capacity: int = 0
        for s in speeds:
            total_capacity += s * time_limit
        if total_capacity < sum(tasks):
            return False

        # ---------------------------------------------------------------------
        # Data structure idea:
        #
        # We need to repeatedly take the "largest remaining task that fits" into
        # the current remaining capacity.
        #
        # A balanced tree would be ideal, but Python standard library does not
        # provide one directly.
        #
        # However, because tasks are already sorted descending and each task is
        # removed exactly once, we can simulate removals efficiently using a
        # "next pointer" array (disjoint-set-like linked skipping).
        #
        # next_idx[i] tells us the next still-available candidate position at or after i.
        # When task i is used, we "remove" it by linking it to the next available index.
        #
        # We also need to find the first task with size <= remaining_capacity.
        # Since tasks are sorted descending, we can binary search for the leftmost
        # index where task <= remaining_capacity, then jump through removed items
        # using the next-pointer structure.
        # ---------------------------------------------------------------------

        # next_idx has size n + 1.
        # Index n acts as a sentinel meaning "no task available".
        next_idx: List[int] = list(range(n + 1))

        def find(x: int) -> int:
            """
            Find the next available task index at or after x using path compression.

            Args:
                x: Starting index.

            Returns:
                The next available index, or n if none exists.
            """
            while next_idx[x] != x:
                next_idx[x] = next_idx[next_idx[x]]
                x = next_idx[x]
            return x

        def remove(x: int) -> None:
            """
            Mark task index x as used by linking it to the next available index.

            Args:
                x: Index to remove.

            Returns:
                None
            """
            next_idx[x] = find(x + 1)

        # Count how many tasks remain unassigned.
        remaining_tasks: int = n

        # For each reviewer, try to pack as many tasks as possible.
        for s in speeds:
            capacity: int = s * time_limit

            # If all tasks are already assigned, we are done.
            if remaining_tasks == 0:
                return True

            # If even the smallest remaining task is too large, this reviewer cannot help.
            # We do not need a special action here; the loop below will simply do nothing.

            # Keep assigning the largest remaining task that fits into current capacity.
            while remaining_tasks > 0:
                # Binary search for the leftmost index whose task size <= capacity.
                # Since tasks are sorted descending:
                # - earlier indices are larger tasks
                # - later indices are smaller tasks
                #
                # We want the earliest task that is small enough, because that corresponds
                # to the largest task that fits.
                lo: int = 0
                hi: int = n
                while lo < hi:
                    mid: int = (lo + hi) // 2
                    if tasks[mid] <= capacity:
                        hi = mid
                    else:
                        lo = mid + 1

                # 'lo' is now the first index where task <= capacity, or n if none.
                idx: int = find(lo)

                # If idx == n, there is no remaining task that fits into this reviewer's
                # remaining capacity, so move to the next reviewer.
                if idx == n:
                    break

                # Assign this task to the current reviewer.
                capacity -= tasks[idx]
                remove(idx)
                remaining_tasks -= 1

        # If no tasks remain, assignment succeeded.
        return remaining_tasks == 0


if __name__ == "__main__":
    solution = Solution()

    review_work_1 = [6, 8, 5, 3]
    speed_1 = [4, 2]
    print(solution.minimum_review_time(review_work_1, speed_1))  # Expected: 4

    review_work_2 = [9, 9, 9, 9, 9]
    speed_2 = [3, 3, 3]
    print(solution.minimum_review_time(review_work_2, speed_2))  # Correct expected: 6