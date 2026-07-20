"""
Title: Minimum Warmup Time for Shared Conference Rooms

Problem Description:
A company has n meetings that must be held in the given order during a single day.
The i-th meeting requires rooms[i] identical conference rooms at the same time and
lasts for durations[i] minutes. Before a room can host a meeting, it must be warmed
up for W minutes. Once warmed, a room stays available for the rest of the day and
can be reused by later meetings without additional warmup.

All room warmups for a meeting must finish before that meeting starts. Meetings
cannot overlap, but you are allowed to insert idle time between consecutive meetings.
The company wants to know the minimum integer warmup time W such that all meetings
can be completed within totalTime minutes.

More formally, if a meeting needs r rooms and only x rooms have already been warmed
before it starts, then max(0, r - x) new rooms must be warmed, costing W minutes
regardless of how many rooms are warmed in parallel, because the building system
warms all newly opened rooms together in one batch. After that meeting, at least r
rooms remain warmed for the rest of the day.

Important note about correctness:
The examples clearly show that larger W makes the total schedule time larger, not
smaller. Therefore, the meaningful task is to find the largest integer W such that
the schedule can still fit within totalTime. This is exactly why the problem is
solved with binary search on W and a monotonic feasibility check.

For a candidate W:
- The total meeting duration is fixed.
- A warmup batch is needed exactly when the running maximum of rooms increases.
- If there are k such increases, total time = sum(durations) + k * W.
- Feasibility is monotonic: if W works, then any smaller W also works.

So the answer is the maximum feasible W.
"""

from typing import List


class Solution:
    def _count_warmup_batches(self, rooms: List[int]) -> int:
        """
        Count how many warmup batches are required.

        A new batch is needed exactly when the required number of rooms exceeds
        every requirement seen so far. This is because warmed rooms stay available
        forever, so only increases in the running maximum force additional warmup.

        Args:
            rooms: List where rooms[i] is the number of rooms needed for meeting i.

        Returns:
            The number of warmup batches required across all meetings.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        batches: int = 0
        warmed_so_far: int = 0

        for required in rooms:
            if required > warmed_so_far:
                batches += 1
                warmed_so_far = required

        return batches

    def _can_finish(self, rooms: List[int], durations: List[int], total_time: int, w: int) -> bool:
        """
        Check whether all meetings can be completed within total_time for a given warmup time w.

        The total schedule time is:
            sum(durations) + (number of warmup batches) * w

        This works because:
        - Meeting durations always must be paid.
        - Warmup for newly opened rooms happens in one parallel batch costing exactly w.
        - Since warmed rooms remain available, only increases in the running maximum
          room requirement create new warmup batches.

        Args:
            rooms: Required rooms for each meeting.
            durations: Duration of each meeting.
            total_time: Allowed total time for the whole day.
            w: Candidate warmup time.

        Returns:
            True if the schedule fits within total_time, otherwise False.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # First, compute the fixed time spent actually holding meetings.
        # This part does not depend on w at all.
        duration_sum: int = 0
        for d in durations:
            duration_sum += d

        # If just the meetings themselves already exceed the available time,
        # then no warmup time can possibly work.
        if duration_sum > total_time:
            return False

        # Count how many separate warmup batches are required.
        batches: int = self._count_warmup_batches(rooms)

        # Total time = fixed meeting time + warmup time for each batch.
        needed_time: int = duration_sum + batches * w

        return needed_time <= total_time

    def minimumWarmupTime(self, rooms: List[int], durations: List[int], totalTime: int) -> int:
        """
        Compute the maximum feasible integer warmup time W.

        Even though the statement says "minimum", the examples and the monotonic
        binary-search requirement make it clear that the intended answer is the
        largest W such that the total schedule time is still within totalTime.

        Strategy:
        1. Count the number of warmup batches:
           this is the number of times the running maximum of rooms increases.
        2. The total time formula is:
               sum(durations) + batches * W
        3. Because this grows monotonically with W, we can binary search for the
           largest feasible W.

        Args:
            rooms: List of room requirements for meetings in order.
            durations: List of meeting durations in order.
            totalTime: Maximum allowed total time.

        Returns:
            The largest integer W for which a valid schedule exists.

        Time complexity:
            O(n + n * log A) in the helper-based implementation,
            which simplifies to O(n log A), where A is the answer range.

        Space complexity:
            O(1)
        """
        # Defensive check for matching input lengths.
        # The problem guarantees valid input, but this keeps the method robust.
        if len(rooms) != len(durations):
            raise ValueError("rooms and durations must have the same length")

        # Compute the total fixed meeting duration once.
        # This is useful both for early impossibility detection and for setting
        # a tight upper bound on the binary search.
        duration_sum: int = 0
        for d in durations:
            duration_sum += d

        # If the meetings alone do not fit, then there is no feasible warmup time.
        # Since the problem guarantees an answer exists, this should not happen
        # in valid test data, but returning -1 is a sensible fallback.
        if duration_sum > totalTime:
            return -1

        # Count how many warmup batches are required.
        batches: int = self._count_warmup_batches(rooms)

        # If no batches were needed (not possible here because n >= 1 and rooms[i] >= 1),
        # then any W would work. The problem guarantees a finite 64-bit answer, so we
        # keep the logic straightforward. In practice, batches is always at least 1.
        if batches == 0:
            return 0

        # We now binary search for the largest feasible W.
        #
        # Lower bound:
        #   0 always works if duration_sum <= totalTime.
        #
        # Upper bound:
        #   Since duration_sum + batches * W <= totalTime,
        #   we must have W <= (totalTime - duration_sum) // batches.
        #
        # This is a very tight upper bound and makes the search efficient.
        left: int = 0
        right: int = (totalTime - duration_sum) // batches

        # Standard "find maximum feasible value" binary search.
        #
        # Invariant:
        # - Every value <= current answer is feasible.
        # - Every value > current answer is infeasible.
        #
        # We use upper-mid to avoid infinite loops when left and right are adjacent.
        while left < right:
            mid: int = (left + right + 1) // 2

            # Check whether this candidate warmup time still allows the full schedule
            # to fit within totalTime.
            if self._can_finish(rooms, durations, totalTime, mid):
                # mid works, so we try to go larger.
                left = mid
            else:
                # mid is too large, so the answer must be smaller.
                right = mid - 1

        # left == right and is the largest feasible warmup time.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    rooms1: List[int] = [2, 5, 3]
    durations1: List[int] = [4, 6, 2]
    total_time1: int = 18
    result1: int = solution.minimumWarmupTime(rooms1, durations1, total_time1)
    print(result1)  # Expected: 3

    # Example 2
    rooms2: List[int] = [4, 1, 4, 7]
    durations2: List[int] = [5, 2, 5, 3]
    total_time2: int = 24
    result2: int = solution.minimumWarmupTime(rooms2, durations2, total_time2)
    print(result2)  # Expected: 4 based on the stated mechanics

    # Additional trace-friendly test:
    # running maxima: 1, 3, 3, 6 -> 3 batches
    # durations sum = 10
    # totalTime = 19 => max W = (19 - 10) // 3 = 3
    rooms3: List[int] = [1, 3, 2, 6]
    durations3: List[int] = [2, 3, 1, 4]
    total_time3: int = 19
    result3: int = solution.minimumWarmupTime(rooms3, durations3, total_time3)
    print(result3)  # Expected: 3