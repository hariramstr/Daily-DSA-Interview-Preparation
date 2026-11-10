"""
Title: Maximum Launch Delay Before Missing Any Ferry Window

Problem Description:
A research team must transport equipment through a sequence of islands. There are n ferry
crossings that must be taken in order. For crossing i, ferries depart every interval[i]
minutes starting at time 0, and the team needs travel[i] minutes to complete that crossing
once they board.

The team may wait at any island for the next ferry, but they are allowed to delay their
initial launch from the mainland by some non-negative integer number of minutes D. After
that, all movement is deterministic: for each crossing, if they arrive at time t, they
board the earliest ferry departing at or after t, then spend travel[i] minutes on that
crossing.

You are also given a hard deadline T: the team must finish the final crossing no later
than T.

Return the maximum integer launch delay D such that the team can still complete all
crossings by time T. If even launching at time 0 misses the deadline, return -1.

Key Observation:
Feasibility is monotonic:
- If a delay D works, then every smaller delay also works.
This makes the problem a perfect fit for binary search on the answer.

For a fixed delay D:
- Start current_time = D
- For each crossing i:
  - Round current_time up to the next multiple of interval[i]
  - Add travel[i]
- At the end, check whether current_time <= T

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= interval[i] <= 10^9
- 1 <= travel[i] <= 10^9
- 1 <= T <= 10^18
- 64-bit integer arithmetic is required
"""

from typing import List


class Solution:
    def _can_finish(self, interval: List[int], travel: List[int], deadline: int, delay: int) -> bool:
        """
        Determine whether starting with a given initial delay still allows finishing
        all ferry crossings by the deadline.

        The simulation follows the problem statement exactly:
        1. Begin at time = delay
        2. For each crossing:
           - Wait until the earliest ferry departure at or after the current time
           - Travel across that crossing
        3. Check whether the final completion time is within the deadline

        Args:
            interval: List where interval[i] is the ferry departure frequency for crossing i.
            travel: List where travel[i] is the travel time for crossing i.
            deadline: The latest allowed finishing time.
            delay: The chosen initial launch delay.

        Returns:
            True if the route can be completed by 'deadline', otherwise False.

        Time complexity:
            O(n), where n is the number of crossings.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We store the current simulated time in a single integer variable.
        # Python integers automatically handle very large values, which is useful
        # because the problem allows times up to 10^18 and beyond during intermediate
        # calculations.
        current_time: int = delay

        # Process each crossing in order because the route is fixed and crossings
        # must be taken sequentially.
        for i in range(len(interval)):
            # For crossing i, ferries leave at:
            # 0, interval[i], 2 * interval[i], 3 * interval[i], ...
            #
            # If we arrive exactly at a departure time, we can board immediately.
            # Otherwise, we must wait until the next multiple of interval[i].
            #
            # The standard integer formula to round x up to the next multiple of m is:
            # ((x + m - 1) // m) * m
            #
            # Example:
            # x = 13, m = 5
            # ((13 + 5 - 1) // 5) * 5 = (17 // 5) * 5 = 3 * 5 = 15
            #
            # Example when already aligned:
            # x = 15, m = 5
            # ((15 + 5 - 1) // 5) * 5 = (19 // 5) * 5 = 3 * 5 = 15
            departure_interval: int = interval[i]
            current_time = ((current_time + departure_interval - 1) // departure_interval) * departure_interval

            # After boarding, the team spends travel[i] minutes on the crossing.
            current_time += travel[i]

            # Small optimization:
            # If we already exceed the deadline at any point, there is no need to
            # continue simulating the remaining crossings because the final answer
            # for this delay must be False.
            if current_time > deadline:
                return False

        # If we finish all crossings and never exceed the deadline, this delay works.
        return current_time <= deadline

    def maximum_launch_delay(self, interval: List[int], travel: List[int], T: int) -> int:
        """
        Compute the maximum non-negative integer launch delay such that the team can
        still finish all ferry crossings by time T.

        This method uses binary search on the answer:
        - First, check whether delay 0 is feasible. If not, return -1.
        - Otherwise, binary search the largest delay D for which the simulation succeeds.

        Why binary search works:
        - If a delay D is feasible, then any smaller delay is also feasible.
        - This monotonic property allows us to search efficiently over possible delays.

        Args:
            interval: List where interval[i] is the ferry departure frequency for crossing i.
            travel: List where travel[i] is the travel time for crossing i.
            T: The hard deadline by which the final crossing must be completed.

        Returns:
            The maximum feasible integer launch delay, or -1 if even delay 0 fails.

        Time complexity:
            O(n log T), because each feasibility check is O(n) and binary search
            performs O(log T) checks.

        Space complexity:
            O(1), ignoring input storage.
        """
        # Defensive check for consistent input lengths.
        # The problem statement implies they match, but this makes the method safer
        # and easier to understand for beginners.
        if len(interval) != len(travel):
            raise ValueError("interval and travel must have the same length")

        # If starting immediately already misses the deadline, then no non-negative
        # delay can possibly work. This is because adding delay can only make the
        # start time later, never earlier.
        if not self._can_finish(interval, travel, T, 0):
            return -1

        # We now know that at least delay 0 is feasible.
        #
        # Next, we need an upper bound for binary search.
        # A simple and safe upper bound is T:
        # - Starting later than T cannot help, because all travel times are positive,
        #   so the finish time would certainly be greater than T.
        #
        # Therefore, the answer must lie in [0, T].
        left: int = 0
        right: int = T
        best: int = 0

        # Standard binary search for the maximum feasible value.
        #
        # Invariant:
        # - Any feasible delay can update 'best'
        # - We continue searching to the right when feasible, because we want the maximum
        # - We search to the left when infeasible
        while left <= right:
            mid: int = (left + right) // 2

            # Check whether this candidate delay works.
            if self._can_finish(interval, travel, T, mid):
                # mid is feasible, so it is a valid candidate answer.
                best = mid

                # Since we want the maximum feasible delay, try larger values.
                left = mid + 1
            else:
                # mid is too large, so discard the right half including mid.
                right = mid - 1

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    #
    # Important note:
    # If we simulate the stated rules exactly, the maximum valid delay is 6, not 4.
    #
    # Trace:
    # D = 6
    # start = 6
    # crossing 0: next multiple of 5 is 10, then +4 => 14
    # crossing 1: next multiple of 7 is 14, then +2 => 16
    # crossing 2: next multiple of 3 is 18, then +5 => 23 <= 25
    #
    # D = 7
    # start = 7
    # crossing 0: next multiple of 5 is 10, then +4 => 14
    # crossing 1: next multiple of 7 is 14, then +2 => 16
    # crossing 2: next multiple of 3 is 18, then +5 => 23 <= 25
    #
    # D = 8
    # start = 8
    # crossing 0: next multiple of 5 is 10, then +4 => 14
    # crossing 1: next multiple of 7 is 14, then +2 => 16
    # crossing 2: next multiple of 3 is 18, then +5 => 23 <= 25
    #
    # D = 10
    # start = 10
    # crossing 0: 10 -> +4 => 14
    # crossing 1: 14 -> +2 => 16
    # crossing 2: 18 -> +5 => 23 <= 25
    #
    # D = 11
    # start = 11
    # crossing 0: next multiple of 5 is 15, then +4 => 19
    # crossing 1: next multiple of 7 is 21, then +2 => 23
    # crossing 2: next multiple of 3 is 24, then +5 => 29 > 25
    #
    # So the correct result under the written rules is 10.
    interval1: List[int] = [5, 7, 3]
    travel1: List[int] = [4, 2, 5]
    T1: int = 25
    result1: int = solution.maximum_launch_delay(interval1, travel1, T1)
    print(result1)

    # Example 2 from the prompt.
    #
    # D = 0
    # crossing 0: depart 0, finish 7
    # crossing 1: next multiple of 10 is 10, finish 18 > 14
    # Therefore answer is -1.
    interval2: List[int] = [6, 10]
    travel2: List[int] = [7, 8]
    T2: int = 14
    result2: int = solution.maximum_launch_delay(interval2, travel2, T2)
    print(result2)