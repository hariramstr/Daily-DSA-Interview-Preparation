"""
Title: Maximum Launch Delay Before Missing Any Satellite Window

Problem Description:
A spacecraft must complete a fixed sequence of communication passes with ground stations.
There are n passes, and pass i must be started no later than windowClose[i].
The passes must be completed in order.

Traveling from pass i to pass i + 1 takes travel[i] minutes, and performing pass i itself
takes duration[i] minutes.

Before the first pass begins, the entire mission may be delayed by some integer number of
minutes D due to launch scheduling.

After that, the timeline is deterministic:
- pass 0 starts at time D
- it finishes after duration[0]
- then the craft spends travel[0] minutes to reach the next station
- then performs pass 1
- and so on

A delay D is feasible if every pass i starts at or before windowClose[i].

Task:
Compute the maximum feasible integer launch delay D.
If even D = 0 is impossible, return -1.

Constraints:
- 1 <= n <= 200000
- 0 <= duration[i] <= 10^9
- 0 <= travel[i] <= 10^9 for 0 <= i < n - 1
- 0 <= windowClose[i] <= 10^18
- All values are integers
- The answer must fit in a signed 64-bit integer
"""

from typing import List


class Solution:
    def _is_feasible(
        self,
        duration: List[int],
        travel: List[int],
        window_close: List[int],
        delay: int,
    ) -> bool:
        """
        Check whether a given launch delay is feasible.

        A delay is feasible if, after delaying the mission start by `delay`,
        every pass starts at or before its corresponding closing time.

        Args:
            duration: Duration of each pass.
            travel: Travel time between consecutive passes.
            window_close: Latest allowed start time for each pass.
            delay: Candidate launch delay to test.

        Returns:
            True if the delay is feasible, otherwise False.

        Time complexity:
            O(n), where n is the number of passes.

        Space complexity:
            O(1), excluding input storage.
        """
        # `current_start` represents the exact start time of the current pass.
        # For pass 0, the mission simply starts after the chosen launch delay.
        current_start: int = delay

        # We iterate through every pass in order because the problem states
        # that passes must be completed sequentially and the timeline after
        # the initial delay is fully deterministic.
        for i in range(len(duration)):
            # The key rule in the problem:
            # pass i must START no later than window_close[i].
            # We do not compare finish time here, only start time.
            if current_start > window_close[i]:
                return False

            # If this is not the last pass, compute when the next pass starts.
            # The next pass can only begin after:
            # 1) finishing the current pass, and
            # 2) traveling to the next station.
            if i < len(travel):
                current_start += duration[i] + travel[i]

        # If we never violated any window, the delay is feasible.
        return True

    def maximum_launch_delay(
        self,
        duration: List[int],
        travel: List[int],
        window_close: List[int],
    ) -> int:
        """
        Compute the maximum feasible integer launch delay.

        The feasibility of a delay is monotonic:
        - If a delay D works, then every smaller delay also works.
        - If a delay D fails, then every larger delay also fails.
        This monotonic property allows binary search on the answer.

        Args:
            duration: Duration of each pass.
            travel: Travel time between consecutive passes.
            window_close: Latest allowed start time for each pass.

        Returns:
            The maximum feasible integer delay, or -1 if even delay 0 is impossible.

        Time complexity:
            O(n log M), where:
            - n is the number of passes
            - M is the search range of possible delays

        Space complexity:
            O(1), excluding input storage.
        """
        n: int = len(duration)

        # Basic input consistency check.
        # The problem guarantees valid input, but this makes the method safer
        # and easier to understand for beginners.
        if len(window_close) != n or len(travel) != max(0, n - 1):
            raise ValueError("Input lengths are inconsistent.")

        # First, test the smallest possible delay: D = 0.
        # If even this fails, then no non-negative delay can ever work,
        # because larger delays only make all start times later.
        if not self._is_feasible(duration, travel, window_close, 0):
            return -1

        # We now need an upper bound for binary search.
        #
        # Let prefix_time[i] be the amount of deterministic time added before pass i
        # starts, excluding the initial delay D.
        #
        # Then:
        # start_time[i] = D + prefix_time[i]
        #
        # Feasibility requires:
        # D + prefix_time[i] <= window_close[i]
        # D <= window_close[i] - prefix_time[i]
        #
        # Therefore, the maximum feasible D is exactly:
        # min_i(window_close[i] - prefix_time[i])
        #
        # We could compute that directly in O(n), which is even faster.
        # However, the problem explicitly asks for a binary-search-based solution.
        # So we still compute a safe and tight upper bound from the same logic,
        # then binary search within [0, upper_bound].
        prefix_time: int = 0
        upper_bound: int = 10**30  # Very large fallback; will be tightened immediately.

        for i in range(n):
            # For pass i, D must be at most window_close[i] - prefix_time.
            # We keep the minimum across all passes, because ALL constraints
            # must hold simultaneously.
            candidate_limit: int = window_close[i] - prefix_time
            if candidate_limit < upper_bound:
                upper_bound = candidate_limit

            # Update prefix_time for the next pass.
            # After pass i starts, the next pass start is delayed by:
            # duration[i] + travel[i], if a next pass exists.
            if i < n - 1:
                prefix_time += duration[i] + travel[i]

        # Since D is a non-negative launch delay, the search lower bound is 0.
        left: int = 0
        right: int = upper_bound

        # Standard "find maximum true" binary search.
        #
        # Invariant:
        # - all feasible answers are in [left, right]
        # - we already know at least D = 0 is feasible
        #
        # We use upper mid to avoid infinite loops when left and right are adjacent.
        while left < right:
            mid: int = (left + right + 1) // 2

            # If `mid` works, then we try to push the delay larger.
            if self._is_feasible(duration, travel, window_close, mid):
                left = mid
            else:
                # If `mid` fails, all larger values also fail due to monotonicity.
                right = mid - 1

        # At loop end, left == right and is the maximum feasible delay.
        return left


if __name__ == "__main__":
    solver = Solution()

    # Example 1 from the prompt
    duration_1 = [3, 2, 4]
    travel_1 = [5, 1]
    window_close_1 = [4, 12, 20]
    result_1 = solver.maximum_launch_delay(duration_1, travel_1, window_close_1)
    print("Example 1 result:", result_1)  # Expected: 4

    # Corrected impossible example based on the prompt note
    duration_2 = [5, 3]
    travel_2 = [4]
    window_close_2 = [3, 8]
    result_2 = solver.maximum_launch_delay(duration_2, travel_2, window_close_2)
    print("Example 2 result:", result_2)  # Expected: -1

    # Additional quick sanity check:
    # Single pass: maximum delay is simply window_close[0].
    duration_3 = [10]
    travel_3: List[int] = []
    window_close_3 = [7]
    result_3 = solver.maximum_launch_delay(duration_3, travel_3, window_close_3)
    print("Example 3 result:", result_3)  # Expected: 7