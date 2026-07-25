"""
Title: Minimum Warmup Time for Shared Conference Rooms

Problem Description:
A company has n meetings that must be held in the given order. The i-th meeting starts
at time start[i] and ends at time end[i], where start and end are strictly increasing
arrays and start[i] < end[i]. Before any meeting begins, a room assigned to that meeting
must be warmed up for w minutes immediately before the meeting starts, meaning the room
is occupied during the interval [start[i] - w, end[i]].

The company has exactly k identical conference rooms. Meetings cannot be reordered,
split, or moved. Two meetings may use the same room only if their occupied intervals
do not overlap. Your task is to find the maximum integer warmup time w such that all
meetings can still be scheduled using at most k rooms.

Because larger warmup times make scheduling harder, feasibility is monotonic:
- If a warmup time w is feasible, then any smaller warmup time is also feasible.
- If a warmup time w is not feasible, then any larger warmup time is also not feasible.

This monotonic property allows binary search on w.

Efficient approach:
- Binary search the answer w.
- For a fixed w, check feasibility by sweeping meetings from left to right.
- Track currently occupied rooms using a min-heap of room release times (meeting end times).
- Before assigning the next meeting, free every room whose end time is <= current occupied
  interval start (start[i] - w).
- If after freeing, k rooms are still occupied, then this w is not feasible.
- Otherwise assign a room and push end[i] into the heap.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= k <= n
- 1 <= start[i] < end[i] <= 10^9
- start is strictly increasing
- end is strictly increasing
"""

from heapq import heappop, heappush
from typing import List


class Solution:
    def is_feasible(self, start: List[int], end: List[int], k: int, w: int) -> bool:
        """
        Check whether all meetings can be scheduled using at most k rooms
        when each meeting occupies [start[i] - w, end[i]].

        Args:
            start: Strictly increasing meeting start times.
            end: Strictly increasing meeting end times.
            k: Number of available identical rooms.
            w: Candidate warmup time to test.

        Returns:
            True if the schedule is possible with at most k rooms, otherwise False.

        Time complexity:
            O(n log k) in practice, because the heap size never exceeds k when feasible.
            More generally O(n log n).

        Space complexity:
            O(k) in practice, O(n) in the worst case for the heap.
        """
        # This min-heap stores the end times of meetings currently occupying rooms.
        # Why end times?
        # Because a room becomes reusable exactly when its current meeting's occupied
        # interval ends, which is end[i]. The warmup affects only the occupied interval
        # start, not the release time.
        occupied_room_end_times: List[int] = []

        # Process meetings in the given order. Since start[] is strictly increasing,
        # the occupied interval starts (start[i] - w) are also strictly increasing
        # for a fixed w, which makes a left-to-right sweep natural and efficient.
        for i in range(len(start)):
            # The current meeting occupies the room starting from this time.
            current_occupied_start = start[i] - w

            # Free every room whose current meeting ends at or before the current
            # occupied interval start.
            #
            # Why <= ?
            # If one meeting ends exactly when another occupied interval begins,
            # the intervals do not overlap, so the same room can be reused.
            while occupied_room_end_times and occupied_room_end_times[0] <= current_occupied_start:
                heappop(occupied_room_end_times)

            # After removing all reusable rooms, the heap size equals the number of
            # rooms still occupied at the moment this meeting needs to begin warming up.
            #
            # If all k rooms are still occupied, we cannot place this meeting.
            if len(occupied_room_end_times) >= k:
                return False

            # Otherwise, assign this meeting to a room.
            # The room will remain occupied until end[i].
            heappush(occupied_room_end_times, end[i])

        # If we successfully assigned every meeting, then this warmup time is feasible.
        return True

    def maximumWarmupTime(self, start: List[int], end: List[int], k: int) -> int:
        """
        Compute the maximum integer warmup time such that all meetings remain schedulable
        using at most k rooms.

        Args:
            start: Strictly increasing meeting start times.
            end: Strictly increasing meeting end times.
            k: Number of available identical rooms.

        Returns:
            The largest feasible integer warmup time.

        Time complexity:
            O(n log n log U), where U is the binary-search range of warmup times.
            With the chosen bounds, U is at most about 2 * 10^9, so log U is small.

        Space complexity:
            O(n) worst case for the feasibility heap.
        """
        n = len(start)

        # Special case:
        # If k >= n, every meeting can use its own room.
        # Then there is no finite upper limit imposed by overlap among these meetings.
        #
        # In many algorithmic formulations, the search space is implicitly bounded.
        # To keep the function well-defined for all valid inputs, we return a very large
        # feasible value based on the input scale. This is sufficient for standard
        # bounded-search interpretations.
        #
        # However, for the intended interview-style problem, test cases typically ensure
        # a finite answer. We still handle this case defensively.
        if k >= n:
            return 10**9

        # Binary search over warmup time w.
        #
        # Monotonicity:
        # - Smaller w is easier to schedule.
        # - Larger w is harder to schedule.
        #
        # Therefore, feasible(w) looks like:
        # True True True ... True False False ...
        # and we want the last True.
        #
        # Lower bound:
        # 0 is always the smallest meaningful warmup time.
        left = 0

        # Upper bound:
        # We choose a safely large bound based on constraints.
        # Since start[i] can be as small as 1 and as large as 1e9, and warmup can
        # conceptually extend before time 0, using 1e9 is a safe finite search bound
        # for standard competitive-programming interpretations.
        right = 10**9

        # Standard "find maximum feasible value" binary search.
        while left < right:
            # Bias upward so that when mid is feasible, we keep it and move rightward.
            mid = (left + right + 1) // 2

            if self.is_feasible(start, end, k, mid):
                left = mid
            else:
                right = mid - 1

        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    start1 = [10, 20, 35]
    end1 = [15, 30, 40]
    k1 = 2
    result1 = solution.maximumWarmupTime(start1, end1, k1)
    print("Example 1 result:", result1)

    # Example 2
    start2 = [5, 8, 14, 20]
    end2 = [6, 12, 18, 22]
    k2 = 2
    result2 = solution.maximumWarmupTime(start2, end2, k2)
    print("Example 2 result:", result2)

    # Additional quick sanity checks
    start3 = [1, 4, 7]
    end3 = [2, 5, 8]
    k3 = 1
    result3 = solution.maximumWarmupTime(start3, end3, k3)
    print("Additional example result:", result3)