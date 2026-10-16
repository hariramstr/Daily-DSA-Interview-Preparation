"""
Title: Longest Traffic Segment With Limited Red-Light Stops

Problem Description:
You are given a road represented by an array `lights` of length `n`, where `lights[i]`
is either `0` or `1`. A value of `1` means the traffic light at intersection `i` is
green when you arrive, and `0` means it is red. You are also given an integer `k` and
an array `penalty` of length `n`, where `penalty[i]` is the delay cost of stopping at
intersection `i` if that light is red. A driver wants to choose one contiguous road
segment `[l, r]` to travel through.

A segment is considered feasible if:
1. It contains at most `k` red lights, and
2. Among the red lights inside the segment, the maximum penalty minus the minimum
   penalty is at most `limit`.

Return the length of the longest feasible contiguous segment.

In other words, you must find the largest window where the number of red lights is
bounded, and the red-light penalties inside that same window stay tightly clustered.
Green lights do not affect the penalty range and should be ignored for the second
condition.

If a segment contains no red lights, it is always valid as long as condition 1 holds.

Constraints:
- 1 <= n <= 2 * 10^5
- lights.length == penalty.length == n
- lights[i] is 0 or 1
- 0 <= penalty[i] <= 10^9
- 0 <= k <= n
- 0 <= limit <= 10^9
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_feasible_segment(
        self, lights: List[int], penalty: List[int], k: int, limit: int
    ) -> int:
        """
        Find the length of the longest contiguous segment that contains at most k red
        lights and whose red-light penalties have max - min <= limit.

        Args:
            lights: Binary array where 0 means red light and 1 means green light.
            penalty: Penalty values aligned with the intersections.
            k: Maximum allowed number of red lights in the chosen segment.
            limit: Maximum allowed spread among penalties of red lights in the segment.

        Returns:
            The maximum valid segment length.

        Time complexity:
            O(n), because each index is added to and removed from each deque at most once.

        Space complexity:
            O(n) in the worst case for the deques.
        """
        n: int = len(lights)

        # We use the classic sliding window pattern:
        # - Expand the right boundary one step at a time.
        # - If the window becomes invalid, move the left boundary rightward until valid.
        # - Track the maximum valid window length seen.
        #
        # The challenge is efficiently checking the second condition:
        # among red lights only, max penalty - min penalty <= limit.
        #
        # To support this in O(1) amortized time per step, we maintain:
        # 1) a monotonic increasing deque of red-light indices by penalty value
        #    -> front gives the minimum red penalty in the current window
        # 2) a monotonic decreasing deque of red-light indices by penalty value
        #    -> front gives the maximum red penalty in the current window
        #
        # Important detail:
        # Green lights must be ignored for the penalty range condition.
        # Therefore, we only insert indices of red lights into these deques.

        min_red_penalties: Deque[int] = deque()
        max_red_penalties: Deque[int] = deque()

        # Count how many red lights are currently inside the window [left, right].
        red_count: int = 0

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far.
        best: int = 0

        # Iterate over every possible right boundary.
        for right in range(n):
            # Step 1: Expand the window by including position `right`.
            #
            # If the new element is a red light, it affects:
            # - the red count
            # - the min/max penalty structures
            if lights[right] == 0:
                red_count += 1

                # Maintain increasing deque for minimum red penalty.
                #
                # We remove from the back while the current penalty is smaller than or
                # equal to the penalties at those indices. Why?
                # Because those larger/equal values can never become the minimum while
                # the current red light remains in the window.
                while min_red_penalties and penalty[min_red_penalties[-1]] >= penalty[right]:
                    min_red_penalties.pop()
                min_red_penalties.append(right)

                # Maintain decreasing deque for maximum red penalty.
                #
                # We remove from the back while the current penalty is larger than or
                # equal to the penalties at those indices. Why?
                # Because those smaller/equal values can never become the maximum while
                # the current red light remains in the window.
                while max_red_penalties and penalty[max_red_penalties[-1]] <= penalty[right]:
                    max_red_penalties.pop()
                max_red_penalties.append(right)

            # Step 2: Shrink the window from the left until it becomes valid.
            #
            # The window is invalid if:
            # - it contains more than k red lights, OR
            # - among red lights, max penalty - min penalty > limit
            #
            # If there are no red lights, the range condition is automatically satisfied.
            while True:
                too_many_reds: bool = red_count > k

                # Compute whether the penalty spread condition is violated.
                #
                # This condition only matters if there is at least one red light in the
                # current window. If red_count == 0, there are no red penalties to compare.
                spread_too_large: bool = False
                if red_count > 0:
                    current_min_penalty: int = penalty[min_red_penalties[0]]
                    current_max_penalty: int = penalty[max_red_penalties[0]]
                    spread_too_large = current_max_penalty - current_min_penalty > limit

                # If neither condition is violated, the window is valid and we stop shrinking.
                if not too_many_reds and not spread_too_large:
                    break

                # Otherwise, move the left boundary rightward by one.
                #
                # Before incrementing `left`, if the outgoing element is a red light,
                # we must update:
                # - red_count
                # - deques if their front index is exactly `left`
                if lights[left] == 0:
                    red_count -= 1

                    # If the outgoing red light was the current minimum candidate,
                    # remove it from the front.
                    if min_red_penalties and min_red_penalties[0] == left:
                        min_red_penalties.popleft()

                    # If the outgoing red light was the current maximum candidate,
                    # remove it from the front.
                    if max_red_penalties and max_red_penalties[0] == left:
                        max_red_penalties.popleft()

                left += 1

            # Step 3: At this point, window [left, right] is valid.
            # Update the best length.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def longestTrafficSegmentWithLimitedRedLightStops(
        self, lights: List[int], penalty: List[int], k: int, limit: int
    ) -> int:
        """
        Wrapper method matching a descriptive interview-style name.

        Args:
            lights: Binary array where 0 means red light and 1 means green light.
            penalty: Penalty values aligned with the intersections.
            k: Maximum allowed number of red lights in the chosen segment.
            limit: Maximum allowed spread among penalties of red lights in the segment.

        Returns:
            The maximum valid segment length.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.longest_feasible_segment(lights, penalty, k, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    lights_1: List[int] = [1, 0, 0, 1, 0, 1, 0]
    penalty_1: List[int] = [5, 8, 10, 3, 9, 4, 11]
    k_1: int = 3
    limit_1: int = 2
    result_1: int = solution.longest_feasible_segment(lights_1, penalty_1, k_1, limit_1)
    print("Example 1 Result:", result_1)  # Expected: 5

    # Example 2
    lights_2: List[int] = [0, 1, 0, 0, 1, 0]
    penalty_2: List[int] = [7, 2, 20, 9, 1, 10]
    k_2: int = 2
    limit_2: int = 3
    result_2: int = solution.longest_feasible_segment(lights_2, penalty_2, k_2, limit_2)
    print("Example 2 Result:", result_2)  # Expected: 3

    # Additional quick sanity checks
    lights_3: List[int] = [1, 1, 1, 1]
    penalty_3: List[int] = [10, 20, 30, 40]
    k_3: int = 0
    limit_3: int = 0
    result_3: int = solution.longest_feasible_segment(lights_3, penalty_3, k_3, limit_3)
    print("All green Result:", result_3)  # Expected: 4

    lights_4: List[int] = [0, 0, 0]
    penalty_4: List[int] = [1, 100, 2]
    k_4: int = 2
    limit_4: int = 1
    result_4: int = solution.longest_feasible_segment(lights_4, penalty_4, k_4, limit_4)
    print("Tight penalty range Result:", result_4)  # Expected: 1