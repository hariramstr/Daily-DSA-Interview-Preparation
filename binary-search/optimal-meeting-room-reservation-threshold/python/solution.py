```python
"""
Title: Optimal Meeting Room Reservation Threshold
Difficulty: Hard
Topic: Binary Search

Problem Description:
A company has `n` meeting rooms and a list of `meetings`, where
`meetings[i] = [start_i, end_i, priority_i]` represents a meeting that starts
at `start_i`, ends at `end_i` (exclusive), and has a priority value `priority_i`.
You are tasked with finding the minimum priority threshold `T` such that if you
only schedule meetings with `priority >= T`, you can fit all such qualifying
meetings into the `n` available rooms without any conflicts (i.e., at no point
in time are more than `n` meetings happening simultaneously).

If even scheduling all meetings (threshold = 1) exceeds the room capacity,
return -1. If no meetings exist or all meetings can always be scheduled,
return 1.

Constraints:
- 1 <= n <= 100
- 1 <= meetings.length <= 10^5
- 0 <= start_i < end_i <= 10^9
- 1 <= priority_i <= 10^6
"""

from typing import List
import bisect


class Solution:
    def find_min_threshold(self, n: int, meetings: List[List[int]]) -> int:
        """
        Find the minimum priority threshold T such that scheduling only meetings
        with priority >= T fits within n rooms (no time conflicts).

        The key insight is:
        - As T increases, fewer meetings are scheduled → easier to fit in n rooms.
        - As T decreases, more meetings are scheduled → harder to fit in n rooms.
        - This monotonic property makes binary search applicable.

        We binary search on the threshold T over the sorted unique priority values.
        For each candidate T, we check if the subset of meetings with priority >= T
        can be scheduled in n rooms (i.e., at no point do more than n overlap).

        Args:
            n: Number of available meeting rooms.
            meetings: List of [start, end, priority] for each meeting.

        Returns:
            Minimum priority threshold T, or -1 if impossible even with T=1.

        Time Complexity:
            O(M log M * log P) where M = number of meetings, P = number of
            distinct priority values. The binary search runs O(log P) iterations,
            and each feasibility check takes O(M log M) for sorting/sweeping.

        Space Complexity:
            O(M) for storing events during the sweep-line check.
        """

        # -----------------------------------------------------------------------
        # EDGE CASE: No meetings at all → trivially fits, return 1
        # -----------------------------------------------------------------------
        if not meetings:
            return 1

        # -----------------------------------------------------------------------
        # STEP 1: Collect all unique priority values and sort them.
        #
        # We will binary search over these priority values to find the minimum
        # threshold T. The candidate thresholds are exactly the distinct priority
        # values present in the input (plus implicitly 1 if not present, but
        # since priorities >= 1 and we want the minimum valid threshold, we only
        # need to check actual priority values that appear).
        #
        # Why? Because the set of "qualifying meetings" only changes at priority
        # boundaries that correspond to actual meeting priorities. Between two
        # consecutive distinct priorities, the qualifying set is the same.
        # -----------------------------------------------------------------------
        unique_priorities = sorted(set(m[2] for m in meetings))
        # unique_priorities is now a sorted list like [1, 2, 3, 5] for example.

        # -----------------------------------------------------------------------
        # STEP 2: Define the feasibility check function.
        #
        # Given a threshold T, this function returns True if all meetings with
        # priority >= T can be scheduled in n rooms without conflicts.
        #
        # We use a SWEEP LINE (coordinate compression + event-based scan):
        #   - For each qualifying meeting [s, e, p], create two events:
        #       (+1, s) → a meeting starts at time s
        #       (-1, e) → a meeting ends at time e (exclusive end)
        #   - Sort events by time. When times are equal, process END events
        #     before START events (since end is exclusive: a room freed at time t
        #     can be used by a meeting starting at time t).
        #   - Sweep through events, maintaining a running count of active meetings.
        #   - If the count ever exceeds n, return False.
        # -----------------------------------------------------------------------
        def is_feasible(threshold: int) -> bool:
            """
            Check if meetings with priority >= threshold fit in n rooms.

            Args:
                threshold: The minimum priority to include.

            Returns:
                True if the subset fits in n rooms, False otherwise.
            """
            # ------------------------------------------------------------------
            # Build the event list for the sweep line.
            # Each event is a tuple (time, type) where type is:
            #   0 → end event (process first when times are equal)
            #   1 → start event (process after end events)
            # Using 0 for end and 1 for start ensures ends are processed first
            # when we sort, because (t, 0) < (t, 1).
            # ------------------------------------------------------------------
            events = []
            for start, end, priority in meetings:
                if priority >= threshold:
                    # Add a "start" event at time `start`
                    events.append((start, 1))
                    # Add an "end" event at time `end`
                    # End is exclusive, so at time `end` the room is freed.
                    events.append((end, 0))

            # If no meetings qualify, trivially feasible.
            if not events:
                return True

            # ------------------------------------------------------------------
            # Sort events:
            #   Primary key: time (ascending)
            #   Secondary key: event type (0=end before 1=start)
            # This ensures that if a meeting ends and another starts at the same
            # time, the room is freed before the new meeting claims it.
            # ------------------------------------------------------------------
            events.sort()

            # ------------------------------------------------------------------
            # Sweep through events and track the current number of active rooms.
            # ------------------------------------------------------------------
            active = 0
            for time, event_type in events:
                if event_type == 1:
                    # A meeting starts: increment active count
                    active += 1
                    # Check if we've exceeded room capacity
                    if active > n:
                        return False
                else:
                    # A meeting ends: decrement active count
                    active -= 1

            # All events processed without exceeding n → feasible
            return True

        # -----------------------------------------------------------------------
        # STEP 3: Check if it's even possible with threshold = 1 (all meetings).
        #
        # If including ALL meetings still exceeds capacity, return -1.
        # -----------------------------------------------------------------------
        if not is_feasible(1):
            return -1

        # -----------------------------------------------------------------------
        # STEP 4: Binary search over unique_priorities to find the minimum T.
        #
        # We want the MINIMUM threshold T such that is_feasible(T) is True.
        #
        # Observation:
        #   - is_feasible(T) = True for large T (few meetings qualify).
        #   - is_feasible(T) = False for small T (too many meetings qualify).
        #   - The feasibility is monotone: once True at some T, it stays True
        #     for all T' >= T.
        #
        # We binary search on the index into unique_priorities.
        # We want the smallest index `idx` such that
        #   is_feasible(unique_priorities[idx]) is True.
        #
        # Binary search setup:
        #   lo = 0 (index of smallest priority)
        #   hi = len(unique_priorities) - 1 (index of largest priority)
        #   answer = hi (worst case: only highest priority meetings)
        # -----------------------------------------------------------------------
        lo = 0
        hi = len(unique_priorities) - 1
        answer_index = hi  # Start with the most restrictive threshold as default

        while lo <= hi:
            mid = (lo + hi) // 2

            # Candidate threshold is the priority value at index `mid`
            candidate_threshold = unique_priorities[mid]

            if is_feasible(candidate_threshold):
                # This threshold works! Record it and try a smaller threshold
                # (lower index = lower priority value = more meetings included).
                answer_index = mid
                hi = mid - 1  # Search left half for a potentially smaller T
            else:
                # This threshold doesn't work (too many meetings).
                # We need a higher threshold (fewer meetings).
                lo = mid + 1  # Search right half

        # -----------------------------------------------------------------------
        # STEP 5: Return the minimum valid threshold found.
        #
        # unique_priorities[answer_index] is the smallest priority value T such
        # that scheduling only meetings with priority >= T fits in n rooms.
        # -----------------------------------------------------------------------
        return unique_priorities[answer_index]


# =============================================================================
# MAIN: Test the solution with provided examples and additional cases
# =============================================================================
if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1:
    # n = 2, meetings = [[1,5,3],[2,6,5],[4,8,2],[7,10,1]]
    # Expected Output: 3
    #
    # Trace:
    #   unique_priorities = [1, 2, 3, 5]
    #   T=1: meetings [1,5,3],[2,6,5],[4,8,2],[7,10,1]
    #        At time [4,5): meetings [1,5,3],[2,6,5],[4,8,2] all active → 3 > 2 → FAIL
    #   T=2: meetings [1,5,3],[2,6,5],[4,8,2]
    #        At time [4,5): same 3 meetings → 3 > 2 → FAIL
    #   T=3: meetings [1,5,3],[2,6,5]
    #        Max overlap = 2 at [2,5) → fits in 2 rooms → PASS
    #   T=5: meetings [2,6,5]
    #        Max overlap = 1 → PASS
    #   Binary search finds minimum T = 3 ✓
    # -------------------------------------------------------------------------
    n1 = 2
    meetings1 = [[1, 5, 3], [2, 6, 5], [4, 8, 2], [7, 10, 1]]
    result1 = solution.find_min_threshold(n1, meetings1)
    print(f"Example 1: n={n1}, meetings={meetings1}")
    print(f"  Output: {result1}  (Expected: 3)")
    print()

    # -------------------------------------------------------------------------
    # Example 2 (corrected per problem description):
    # n = 3, meetings = [[1,4,2],[2,5,2],[3,6,2],[4,7,2]]
    # Expected Output: 1
    #
    # Trace:
    #   unique_priorities = [2]
    #   T=1: all 4 meetings included
    #        Events sorted: (1,1),(2,1),(3,1),(4,0),(4,1),(5,0),(6,0),(7,0)
    #        At time 1: active=1
    #        At time 2: active=2
    #        At time 3: active=3
    #        At time 4: end first → active=2, then start → active=3
    #        At time 5: active=2
    #        At time 6: active=1
    #        At time 7: active=0
    #        Max = 3 ≤ 3 → PASS → return 1 ✓
    # -------------------------------------------------------------------------
    n2 = 3
    meetings2 = [[1, 4, 2], [2, 5, 2], [3, 6, 2], [4, 7, 2]]
    result2 = solution.find_min_threshold(n2, meetings2)
    print(f"Example 2: n={n2}, meetings={meetings2}")
    print(f"  Output: {result2}  (Expected: 1)")
    print()

    # -------------------------------------------------------------------------
    # Example 3: Impossible case
    # n = 1, meetings = [[1,5,1],[2,6,1],[3,7,1]]
    # Expected Output: -1
    #
    # Trace:
    #   T=1: all 3 meetings, at [3,5) three overlap → 3 > 1 → FAIL
    #   unique_priorities = [1], only one threshold to try → FAIL → return -1 ✓
    # -------------------------------------------------------------------------
    n3 = 1
    meetings3 = [[1, 5, 1], [2, 6, 1], [3, 7, 1]]
    result3 = solution.find_min_threshold(n3, meetings3)
    print(f"Example 3 (impossible): n={n3}, meetings={meetings3}")
    print(f"  Output: {result3}  (Expected: -1)")
    print()

    # -------------------------------------------------------------------------
    # Example 4: No meetings
    # n = 5, meetings = []
    # Expected Output: 1
    # -------------------------------------------------------------------------
    n4 = 5
    meetings4 = []
    result4 = solution.find_min_threshold(n4, meetings4)
    print(f"Example 4 (no meetings): n={n4}, meetings={meetings4}")
    print(f"  Output: {result4}  (Expected: 1)")
    print()

    # -------------------------------------------------------------------------
    # Example 5: Single meeting
    # n = 1, meetings = [[0, 10, 5]]
    # Expected Output: 1
    # -------------------------------------------------------------------------
    n5 = 1
    meetings5 = [[0, 10, 5]]
    result5 = solution.find_min_threshold(n5, meetings5)
    print(f"Example 5 (single meeting): n={n5}, meetings={meetings5}")
    print(f"  Output: {result5}  (Expected: 1)")
    print()

    # -------------------------------------------------------------------------
    # Example 6: Non-overlapping meetings
    # n = 1, meetings = [[1,3,2],[3,5,4],[5,7,1]]
    # Expected Output: 1 (they don't overlap, end is exclusive so [1,3) and [3,5) don't conflict)
    # -------------------------------------------------------------------------
    n6 = 1
    meetings6 = [[1, 3, 2], [3, 5, 4], [5, 7, 1]]
    result6 = solution.find_min_threshold(n6, meetings6)
    print(f"Example 6 (non-overlapping): n={n6}, meetings={meetings6}")
    print(f"  Output: {result6}  (Expected: 1)")
    print()

    # -------------------------------------------------------------------------
    # Example 7: Need to raise threshold significantly
    # n = 2, meetings = [[1,10,1],[2,9,2],[3,8,3],[4,7,4],[5,6,5]]
    # At T=1: 5 meetings, at [5,6) all 5 overlap → 5 > 2 → FAIL
    # At T=2: 4 meetings, at [5,6) 4 overlap → 4 > 2 → FAIL
    # At T=3: 3 meetings [1,10,3],[2,9,2 excluded],[3,8,3],[4,7,4],[5,6,5]
    #         Wait: T=3 means priority >= 3: [1,10,1]NO,[2,9,2]NO,[3,8,3]YES,[4,7,4]YES,[5,6,5]YES
    #         At [5,6): [3,8,3],[4,7,4],[5,6,5] → 3 > 2 → FAIL
    # At T=4: priority >= 4: [4,7,4],[5,6,5]
    #         At [5,6): both active → 2 ≤ 2 → PASS
    # At T=5: priority >= 5: [5,6,5] → 1 ≤ 2 → PASS
    # Minimum T = 4
    # -------------------------------------------------------------------------