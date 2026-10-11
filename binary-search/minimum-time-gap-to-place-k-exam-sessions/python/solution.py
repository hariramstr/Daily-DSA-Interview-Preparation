"""
Title: Minimum Time Gap to Place K Exam Sessions

Problem Description:
A university has already reserved several classrooms at specific start times during a day.
The administration wants to schedule exactly k proctored exam sessions using these reserved
start times, with at most one exam session per reserved time. To reduce staffing conflicts
and hallway congestion, they want the chosen exam sessions to be as far apart as possible.

You are given an integer array times, where times[i] is the start time of the i-th reserved
classroom slot in minutes from the beginning of the day, and an integer k. Return the largest
possible value d such that it is possible to choose exactly k distinct start times from times
where the difference between every pair of consecutive chosen start times is at least d minutes.

If multiple selections are possible, you only need to return the maximum achievable minimum gap d.

This problem should be solved efficiently for large inputs. A common approach is to sort the
times and use binary search on the answer. For a candidate gap d, greedily place exam sessions
from left to right and check whether k sessions can be scheduled.

Constraints:
- 2 <= times.length <= 100000
- 0 <= times[i] <= 1000000000
- All values in times are distinct
- 2 <= k <= times.length

Example 1:
Input: times = [30, 0, 90, 60, 120], k = 3
Output: 60

Example 2:
Input: times = [5, 18, 1, 9, 14], k = 4
Output: 4
"""

from typing import List


class Solution:
    def can_place_with_gap(self, times: List[int], k: int, gap: int) -> bool:
        """
        Check whether it is possible to choose at least k start times such that
        the difference between consecutive chosen times is at least `gap`.

        Args:
            times: Sorted list of distinct reserved classroom start times.
            k: Number of exam sessions that must be scheduled.
            gap: Candidate minimum gap to test.

        Returns:
            True if we can place at least k sessions with minimum consecutive gap `gap`,
            otherwise False.

        Time complexity:
            O(n), where n is the length of times.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We always choose the earliest available time first.
        # Why is this greedy choice correct?
        # Because choosing an earlier valid time leaves as much room as possible
        # for the remaining sessions to be placed later.
        #
        # This is a classic greedy strategy for spacing problems:
        # - Place the first session at the earliest time.
        # - Then keep placing the next session at the earliest time that is at least
        #   `gap` minutes after the last chosen one.
        #
        # If this greedy process cannot place k sessions, then no other placement can.
        count: int = 1
        last_chosen_time: int = times[0]

        # Scan from left to right through the sorted times.
        for current_time in times[1:]:
            # If the current reserved time is far enough from the last chosen time,
            # we can place another exam session here.
            if current_time - last_chosen_time >= gap:
                count += 1
                last_chosen_time = current_time

                # As soon as we have placed k sessions, we know this gap is feasible.
                if count >= k:
                    return True

        # If we finish scanning and still have fewer than k sessions,
        # then this candidate gap is too large.
        return False

    def maximum_minimum_gap(self, times: List[int], k: int) -> int:
        """
        Compute the largest possible minimum gap between consecutive chosen exam sessions.

        Args:
            times: List of distinct reserved classroom start times.
            k: Exact number of exam sessions to schedule.

        Returns:
            The maximum achievable value d such that we can choose exactly k times
            with every consecutive chosen pair differing by at least d.

        Time complexity:
            O(n log R), where:
            - n is the number of times
            - R is the search range of possible answers, approximately
              max(times) - min(times)

        Space complexity:
            O(1) extra space beyond sorting. Python's sort may use implementation-level
            auxiliary space, but algorithmically the extra working space here is constant.
        """
        # Step 1: Sort the times.
        #
        # Sorting is necessary because:
        # - We care about gaps between chosen times in increasing order.
        # - The greedy feasibility check only works correctly on sorted data.
        times.sort()

        # Step 2: Define the binary search range for the answer.
        #
        # The minimum possible gap is 0:
        # - This is always feasible because all times are distinct, and choosing any k times
        #   gives consecutive gaps >= 0.
        #
        # The maximum possible gap is the full spread from smallest to largest time:
        # - We cannot have a minimum gap larger than max(times) - min(times).
        left: int = 0
        right: int = times[-1] - times[0]

        # This variable will store the best feasible answer found so far.
        best_gap: int = 0

        # Step 3: Binary search on the answer.
        #
        # Key idea:
        # - If a gap `d` is feasible, then every smaller gap is also feasible.
        # - If a gap `d` is not feasible, then every larger gap is also not feasible.
        #
        # This monotonic property makes binary search valid.
        while left <= right:
            mid: int = (left + right) // 2

            # Check whether we can place k sessions with at least `mid` minutes apart.
            if self.can_place_with_gap(times, k, mid):
                # `mid` is feasible, so it is a candidate answer.
                best_gap = mid

                # Since we want the largest feasible gap, search the right half
                # for a possibly better answer.
                left = mid + 1
            else:
                # `mid` is too large and not feasible.
                # Therefore, we must search smaller gaps.
                right = mid - 1

        # After binary search finishes, best_gap is the largest feasible minimum gap.
        return best_gap


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    times1: List[int] = [30, 0, 90, 60, 120]
    k1: int = 3
    result1: int = solution.maximum_minimum_gap(times1, k1)
    print("Example 1 Result:", result1)  # Expected: 60

    # Example 2
    times2: List[int] = [5, 18, 1, 9, 14]
    k2: int = 4
    result2: int = solution.maximum_minimum_gap(times2, k2)
    print("Example 2 Result:", result2)  # Expected: 4

    # Additional quick checks
    times3: List[int] = [1, 2, 4, 8, 9]
    k3: int = 3
    result3: int = solution.maximum_minimum_gap(times3, k3)
    print("Additional Example Result:", result3)  # One optimal answer is 3 (1, 4, 8)