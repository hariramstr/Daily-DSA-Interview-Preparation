"""
Title: Minimum Rental Cost for Deadline-Limited Machines

Problem Description:
A factory must complete n production jobs. Job i becomes available on day start[i],
must be finished no later than day end[i], and requires exactly one machine for one
full day. You may rent any number of identical machines. Renting one machine costs
cost[j] for day j, and a rented machine can process at most one job on that day.
A single job may be scheduled on any integer day d such that start[i] <= d <= end[i].

Your task is to compute the minimum total rental cost needed to complete all jobs,
or return -1 if it is impossible.

You are not assigning jobs to specific machine identities ahead of time. Instead,
think of choosing how many machine slots to rent on each day, then placing each job
into one feasible slot within its allowed interval. Multiple jobs may share the same
machine across different days, but on the same day each rented machine handles only
one job.

This is a hard scheduling problem because a cheap day should be used only when doing
so does not block more urgent jobs. Efficient solutions typically process days in
order and use a priority queue to decide which currently available jobs must be
scheduled before they expire.

Constraints:
- 1 <= n <= 200000
- 1 <= start[i] <= end[i] <= 10^9
- 1 <= cost[j] <= 10^9
- The number of distinct days with listed rental costs is m, where 1 <= m <= 200000
- Each rental cost is given as a pair (day, price), and jobs may only be scheduled
  on days that appear in this list
- All input arrays are valid and may be unsorted
"""

from bisect import bisect_left, bisect_right
from typing import List, Tuple


class FenwickTree:
    """Fenwick tree supporting prefix sums and point updates.

    This tree stores integer counts or costs over compressed day indices.

    Time complexity:
        - update: O(log n)
        - query_prefix: O(log n)
        - range_sum: O(log n)
        - find_by_prefix: O(log n)

    Space complexity:
        O(n)
    """

    def __init__(self, size: int) -> None:
        """Initialize an empty Fenwick tree.

        Args:
            size: Number of positions.

        Returns:
            None

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        self.n: int = size
        self.bit: List[int] = [0] * (size + 1)

    def update(self, index: int, delta: int) -> None:
        """Add delta to one position.

        Args:
            index: Zero-based index.
            delta: Value to add.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        i: int = index + 1
        while i <= self.n:
            self.bit[i] += delta
            i += i & -i

    def query_prefix(self, index: int) -> int:
        """Return sum of positions [0..index].

        Args:
            index: Zero-based index.

        Returns:
            Prefix sum.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        if index < 0:
            return 0
        result: int = 0
        i: int = index + 1
        while i > 0:
            result += self.bit[i]
            i -= i & -i
        return result

    def range_sum(self, left: int, right: int) -> int:
        """Return sum of positions [left..right].

        Args:
            left: Left index.
            right: Right index.

        Returns:
            Range sum.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        if left > right:
            return 0
        return self.query_prefix(right) - self.query_prefix(left - 1)

    def find_by_prefix(self, target: int) -> int:
        """Find the smallest index idx such that prefix_sum(idx) >= target.

        This method assumes:
        - target >= 1
        - total sum in the tree is at least target

        Args:
            target: 1-based desired cumulative count.

        Returns:
            Zero-based index of the first position where the prefix reaches target.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        idx: int = 0
        bit_mask: int = 1 << (self.n.bit_length())
        while bit_mask:
            next_idx: int = idx + bit_mask
            if next_idx <= self.n and self.bit[next_idx] < target:
                target -= self.bit[next_idx]
                idx = next_idx
            bit_mask >>= 1
        return idx


class Solution:
    def minimumRentalCost(self, jobs: List[List[int]], rentalDays: List[List[int]]) -> int:
        """Compute the minimum total rental cost to schedule all jobs.

        Core idea:
        We transform the problem into selecting exactly n machine slots across the
        available rental days, where each selected slot is one job processed on that
        day and costs that day's rental price.

        Feasibility condition for interval scheduling:
        For every prefix of days up to some day D, the number of jobs whose deadline
        is at most D must be no more than the number of selected slots up to D.
        Also, a job can only use days not earlier than its start.

        A very useful equivalent greedy view is:
        process jobs in order of increasing deadline, and for each job assign it to
        the cheapest still-unused rental day inside its interval [start, end].
        If no such day exists, scheduling is impossible.

        Why this greedy is correct:
        - Earliest deadlines are the most urgent.
        - For a fixed job, using the cheapest available day in its allowed interval
          can never hurt a later job more than using a more expensive day would.
        - This is the standard optimal greedy for assigning interval-constrained tasks
          to weighted points when each point has unlimited multiplicity only through
          repeated purchases; here each purchase is one slot, so each use consumes one
          unit of that day.

        To implement "cheapest available day in [l, r]" efficiently, we:
        1. Coordinate-compress the rental days.
        2. For each compressed day index, store how many times it is still available.
           Initially, availability is effectively infinite in theory, but because we
           only need to schedule n jobs, we can think of each day as having capacity n.
           However, that would be too large to materialize.
        3. Instead, we use a min-cost segment tree style approach with repeated use.
           Since a day can be used any number of times, the cheapest day in an interval
           would always be chosen for every compatible job, which is not correct if
           urgent jobs need that day. So we must respect one-slot-per-rented-machine,
           but unlimited machines means unlimited slots on a day by paying repeatedly.
           Therefore each job independently pays the chosen day's cost, and multiple
           jobs may choose the same day. The only real constraint is that the chosen
           day lies in the interval. There is no upper bound on jobs per day because
           we may rent any number of machines that day.

        This simplifies the problem dramatically:
        - Each job can be scheduled independently on any listed rental day within its
          interval.
        - The minimum total cost is simply the sum, over all jobs, of the minimum
          rental price among listed days in [start, end].
        - If an interval contains no listed rental day, answer is -1.

        So the task becomes many range-minimum queries over sparse days.

        We solve that by:
        - Sorting rental days by day.
        - Building a segment tree over their prices.
        - For each job, binary-search the rental day indices that fall inside its
          interval, then query the minimum price on that index range.

        Args:
            jobs: List of [start, end] intervals for jobs.
            rentalDays: List of [day, price] pairs.

        Returns:
            Minimum total rental cost, or -1 if impossible.

        Time complexity:
            O((n + m) log m)

        Space complexity:
            O(m)
        """
        if not jobs:
            return 0
        if not rentalDays:
            return -1

        # Sort rental days by actual calendar day so that we can:
        # 1. Binary-search which rental days fall inside a job interval.
        # 2. Build a range-minimum structure over the corresponding prices.
        rentalDays.sort()
        days: List[int] = [d for d, _ in rentalDays]
        prices: List[int] = [c for _, c in rentalDays]

        # Build an iterative segment tree for range minimum queries.
        # tree size is the next power of two >= m.
        m: int = len(prices)
        size: int = 1
        while size < m:
            size <<= 1

        inf: int = 10**30
        seg: List[int] = [inf] * (2 * size)

        # Place prices into leaves.
        for i in range(m):
            seg[size + i] = prices[i]

        # Build internal nodes bottom-up.
        for i in range(size - 1, 0, -1):
            seg[i] = min(seg[2 * i], seg[2 * i + 1])

        def range_min(left: int, right: int) -> int:
            """Return minimum price in compressed index range [left, right].

            Args:
                left: Left index in sorted rentalDays.
                right: Right index in sorted rentalDays.

            Returns:
                Minimum price in that range.

            Time complexity:
                O(log m)

            Space complexity:
                O(1)
            """
            left += size
            right += size
            result: int = inf

            while left <= right:
                if left % 2 == 1:
                    result = min(result, seg[left])
                    left += 1
                if right % 2 == 0:
                    result = min(result, seg[right])
                    right -= 1
                left //= 2
                right //= 2

            return result

        total_cost: int = 0

        # Process each job independently.
        for start_day, end_day in jobs:
            # Find the first rental day >= start_day.
            left_idx: int = bisect_left(days, start_day)

            # Find the last rental day <= end_day.
            right_idx: int = bisect_right(days, end_day) - 1

            # If the interval contains no rental day at all, the job cannot be scheduled.
            if left_idx > right_idx:
                return -1

            # Add the cheapest available rental price inside this job's allowed interval.
            total_cost += range_min(left_idx, right_idx)

        return total_cost


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # The prompt's corrected explanation says the true answer is 11.
    jobs1: List[List[int]] = [[1, 3], [2, 2], [2, 4]]
    rental_days1: List[List[int]] = [[1, 5], [2, 2], [3, 4], [4, 7]]
    result1: int = solution.minimumRentalCost(jobs1, rental_days1)
    print(result1)  # Expected based on independent rentable machines per day: 8? 2+2+4
    # However, the prompt's narrative is inconsistent. Under the stated rule
    # "You may rent any number of identical machines", two jobs can be done on day 2
    # by renting two machines, so the minimum is actually 2 + 2 + 4 = 8.

    # Example 2 from the prompt.
    jobs2: List[List[int]] = [[1, 2], [1, 2], [2, 3], [3, 3]]
    rental_days2: List[List[int]] = [[1, 8], [2, 3], [3, 1]]
    result2: int = solution.minimumRentalCost(jobs2, rental_days2)
    print(result2)  # Expected: 8

    # Additional impossible case: no listed rental day inside a job interval.
    jobs3: List[List[int]] = [[4, 5]]
    rental_days3: List[List[int]] = [[1, 10], [2, 20], [3, 30]]
    result3: int = solution.minimumRentalCost(jobs3, rental_days3)
    print(result3)  # Expected: -1