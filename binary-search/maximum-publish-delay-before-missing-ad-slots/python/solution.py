"""
Title: Maximum Publish Delay Before Missing Ad Slots

Problem Description:
A news platform has already reserved several ad slots during the day. The i-th slot
opens at time slotStart[i] and remains available until time slotEnd[i], inclusive.
Before each slot can be used, the publishing system must finish generating a page
variant, which takes exactly renderTime minutes. The system processes all reserved
slots in the given order, and it can start working on the first page only after
waiting for some initial delay D minutes.

Once the first page starts after delay D, the system renders page 0, then page 1,
then page 2, and so on, back-to-back with no idle time between pages. A slot is
successfully used if the render for its page finishes at some time t such that
slotStart[i] <= t <= slotEnd[i].

Your task is to find the maximum integer delay D such that every reserved slot can
still be used successfully. If even D = 0 is impossible, return -1.

Formally, page i finishes at time D + (i + 1) * renderTime. This finish time must
lie inside the i-th interval.

Constraints:
- 1 <= n <= 200000
- slotStart.length == slotEnd.length == n
- 1 <= slotStart[i] <= slotEnd[i] <= 10^18
- 1 <= renderTime <= 10^18
- The intervals are given in the required processing order and are not necessarily
  sorted by start or end value.

Examples:
1)
Input: slotStart = [5, 11, 17], slotEnd = [9, 15, 21], renderTime = 3
Output: 6

2)
Input: slotStart = [4, 8, 10], slotEnd = [5, 9, 11], renderTime = 3
Output: -1
"""

from typing import List


class Solution:
    def _can_delay_work(self, slot_start: List[int], slot_end: List[int], render_time: int, delay: int) -> bool:
        """
        Check whether a given initial delay allows every page to finish inside its slot.

        Args:
            slot_start: List of inclusive slot start times.
            slot_end: List of inclusive slot end times.
            render_time: Fixed time needed to render each page.
            delay: Candidate initial delay D.

        Returns:
            True if all pages finish within their corresponding intervals, otherwise False.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # We simulate the finish time formula exactly as given:
        # finish_time(i) = delay + (i + 1) * render_time
        #
        # Because pages are rendered back-to-back and always in the given order,
        # there is no scheduling choice to make. For a fixed delay, every finish time
        # is completely determined. So feasibility is simply checking all intervals.
        for i in range(len(slot_start)):
            finish_time: int = delay + (i + 1) * render_time

            # If the finish time is before the slot opens, this delay is too small.
            # If the finish time is after the slot closes, this delay is too large.
            # In either case, the candidate delay fails immediately.
            if finish_time < slot_start[i] or finish_time > slot_end[i]:
                return False

        # If every page finished inside its allowed interval, the delay works.
        return True

    def maximum_publish_delay(self, slotStart: List[int], slotEnd: List[int], renderTime: int) -> int:
        """
        Compute the maximum nonnegative integer delay such that all pages finish
        within their corresponding ad slot intervals.

        Args:
            slotStart: List of inclusive slot start times.
            slotEnd: List of inclusive slot end times.
            renderTime: Fixed rendering time for each page.

        Returns:
            The maximum valid integer delay D, or -1 if even D = 0 is impossible.

        Time complexity:
            O(n log M), where M is the size of the searched delay range

        Space complexity:
            O(1)
        """
        n: int = len(slotStart)

        # ------------------------------------------------------------
        # Key observation:
        #
        # For page i, the finish time is:
        #   D + (i + 1) * renderTime
        #
        # This must satisfy:
        #   slotStart[i] <= D + (i + 1) * renderTime <= slotEnd[i]
        #
        # Rearranging gives a valid range for D:
        #   slotStart[i] - (i + 1) * renderTime <= D <= slotEnd[i] - (i + 1) * renderTime
        #
        # Therefore, the overall valid D values are the intersection of all these ranges,
        # plus the requirement D >= 0.
        #
        # The problem statement expects a "check candidate + binary search" style solution.
        # So we will:
        #   1) Build a safe search range for D
        #   2) Check if D = 0 is feasible; if not, return -1
        #   3) Binary search for the maximum feasible D
        #
        # Why binary search works:
        # If a delay D works, then any smaller delay may or may not work in arbitrary
        # interval problems. However, in this specific problem, each page finish time
        # shifts later as D increases. The set of feasible D values is an interval
        # intersection, so it forms a contiguous range [L, R]. Therefore feasibility
        # is monotonic with respect to "D <= maximum_valid_delay" once we know we are
        # searching over nonnegative integers.
        # ------------------------------------------------------------

        # First, quickly reject impossible cases:
        # If delay 0 already fails, the answer must be -1 because delays are nonnegative,
        # and increasing delay only makes all finish times later.
        if not self._can_delay_work(slotStart, slotEnd, renderTime, 0):
            return -1

        # ------------------------------------------------------------
        # Build an upper bound for binary search.
        #
        # For each page i:
        #   D <= slotEnd[i] - (i + 1) * renderTime
        #
        # So the global maximum possible D cannot exceed the minimum of these values.
        # Since D must also be nonnegative and we already know D = 0 works, this upper
        # bound will be at least 0.
        #
        # Using the tightest possible upper bound makes binary search efficient and safe.
        # ------------------------------------------------------------
        high: int = 10**30  # temporary large value; will be tightened immediately

        for i in range(n):
            latest_delay_for_i: int = slotEnd[i] - (i + 1) * renderTime
            if latest_delay_for_i < high:
                high = latest_delay_for_i

        # Since D = 0 is feasible, high must be >= 0. Still, using max for safety/readability.
        high = max(high, 0)

        # Lower bound is 0 because the problem asks for a nonnegative initial delay.
        low: int = 0
        answer: int = 0

        # ------------------------------------------------------------
        # Standard binary search for the maximum feasible delay.
        #
        # Invariant:
        # - Any feasible delay can be a candidate answer.
        # - If mid works, try larger delays.
        # - If mid fails, try smaller delays.
        #
        # We use:
        #   mid = (low + high) // 2
        #
        # and update:
        #   if works: answer = mid; low = mid + 1
        #   else: high = mid - 1
        # ------------------------------------------------------------
        while low <= high:
            mid: int = (low + high) // 2

            if self._can_delay_work(slotStart, slotEnd, renderTime, mid):
                # mid is valid, so record it and search to the right
                # for a possibly larger valid delay.
                answer = mid
                low = mid + 1
            else:
                # mid is too large (or otherwise invalid), so search left.
                high = mid - 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    slot_start_1: List[int] = [5, 11, 17]
    slot_end_1: List[int] = [9, 15, 21]
    render_time_1: int = 3
    result_1: int = solution.maximum_publish_delay(slot_start_1, slot_end_1, render_time_1)
    print("Example 1 Result:", result_1)  # Expected: 6

    # Example 2
    slot_start_2: List[int] = [4, 8, 10]
    slot_end_2: List[int] = [5, 9, 11]
    render_time_2: int = 3
    result_2: int = solution.maximum_publish_delay(slot_start_2, slot_end_2, render_time_2)
    print("Example 2 Result:", result_2)  # Expected: -1

    # Additional quick sanity check
    slot_start_3: List[int] = [3, 6, 9]
    slot_end_3: List[int] = [100, 100, 100]
    render_time_3: int = 3
    result_3: int = solution.maximum_publish_delay(slot_start_3, slot_end_3, render_time_3)
    print("Example 3 Result:", result_3)  # One valid maximum based on latest end constraints