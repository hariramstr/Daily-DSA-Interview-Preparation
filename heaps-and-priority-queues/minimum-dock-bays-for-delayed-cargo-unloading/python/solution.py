"""
Title: Minimum Dock Bays for Delayed Cargo Unloading

Problem Description:
A shipping terminal receives cargo vessels, each with a scheduled arrival time and a fixed
unloading duration. However, the terminal follows a strict operational rule: if multiple
ships are waiting, the next available dock bay must always be assigned to the waiting ship
with the smallest original arrival time; if there is still a tie, assign the ship with the
smaller input index. Once a ship starts unloading, it occupies its dock bay continuously
for its full duration. If no dock bay is free when a ship arrives, that ship waits until
some bay becomes available.

You are given two integer arrays arrival and unload, where arrival[i] is the scheduled
arrival time of ship i and unload[i] is the time required to unload it. You are also given
an integer T. Determine the minimum number of dock bays needed so that every ship can begin
unloading no later than T time units after its scheduled arrival time.

Formally, if ship i starts at time start[i], then start[i] must satisfy
start[i] - arrival[i] <= T. Ships cannot be reordered arbitrarily; whenever a dock becomes
free, the terminal must choose the eligible waiting ship according to the rule above.
Your task is to compute the smallest number of dock bays that makes the schedule feasible,
or return -1 if it is impossible under the dispatch rule.

Constraints:
- 1 <= n <= 200000
- 0 <= arrival[i] <= 10^9
- 1 <= unload[i] <= 10^9
- 0 <= T <= 10^9
- arrival is not guaranteed to be sorted
"""

from heapq import heappop, heappush
from typing import List, Tuple


class Solution:
    def minimumDockBays(self, arrival: List[int], unload: List[int], T: int) -> int:
        """
        Compute the minimum number of dock bays needed so that every ship starts
        unloading within T time units of its arrival, while respecting the strict
        dispatch rule.

        Args:
            arrival: Scheduled arrival times of ships.
            unload: Unloading durations of ships.
            T: Maximum allowed waiting time.

        Returns:
            The minimum feasible number of dock bays, or -1 if impossible.

        Time complexity:
            O(n log^2 n)
            - We binary search the answer over the number of bays.
            - Each feasibility check runs in O(n log n).

        Space complexity:
            O(n)
            - For sorted ship list and heaps used during simulation.
        """
        n: int = len(arrival)

        # Build a list of ships as tuples:
        # (arrival_time, original_index, unload_duration)
        #
        # Why include original_index?
        # Because the problem states that if multiple waiting ships have the same
        # arrival time, the one with the smaller input index must be chosen first.
        ships: List[Tuple[int, int, int]] = [
            (arrival[i], i, unload[i]) for i in range(n)
        ]

        # Sort ships by (arrival_time, index).
        #
        # This sorted order is exactly the priority order among waiting ships:
        # earlier arrival first, and for ties smaller index first.
        #
        # That means our waiting queue can simply be a FIFO queue over this sorted list,
        # because ships become "waiting" in the same order as this priority.
        ships.sort()

        # Binary search for the minimum number of bays.
        #
        # Lower bound = 1 bay
        # Upper bound = n bays
        #
        # With n bays, every ship can start immediately at arrival time, so feasibility
        # is guaranteed. Therefore the answer always exists in [1, n], and -1 is not
        # actually needed under the given model. Still, we keep the return style aligned
        # with the problem statement.
        left: int = 1
        right: int = n
        answer: int = -1

        while left <= right:
            mid: int = (left + right) // 2

            # Check whether 'mid' bays are enough.
            if self._can_schedule_with_k_bays(ships, T, mid):
                answer = mid
                right = mid - 1
            else:
                left = mid + 1

        return answer

    def _can_schedule_with_k_bays(
        self,
        ships: List[Tuple[int, int, int]],
        T: int,
        k: int,
    ) -> bool:
        """
        Check whether all ships can be scheduled with exactly k dock bays.

        The simulation strictly follows the dispatch rule:
        - Ships arrive over time.
        - If a bay is free and ships are waiting, the waiting ship with smallest
          (arrival_time, index) must start next.
        - Once started, a ship occupies a bay continuously for its full unload time.

        Args:
            ships: Ships sorted by (arrival_time, index), each as
                   (arrival_time, index, unload_duration).
            T: Maximum allowed waiting time.
            k: Number of dock bays to test.

        Returns:
            True if feasible with k bays, otherwise False.

        Time complexity:
            O(n log k), which is O(n log n) in the worst case.

        Space complexity:
            O(k)
            - Heap stores at most k currently occupied bays.
        """
        n: int = len(ships)

        # Min-heap of finish times for currently occupied bays.
        #
        # Each entry is just the time when one occupied bay becomes free.
        # We do not need bay IDs because only the next free time matters.
        busy_bays: List[int] = []

        # Pointer to the next ship in sorted order that has not yet been started.
        #
        # Important observation:
        # Because waiting priority is exactly sorted by (arrival, index), and because
        # ships only become eligible as time moves forward, the next ship that must be
        # started is always ships[next_ship].
        #
        # So we do NOT need a separate waiting heap. A single pointer is enough.
        next_ship: int = 0

        # Main simulation loop:
        # Continue until every ship has been started.
        while next_ship < n:
            # If there is at least one free bay right now, we may be able to start
            # the next ship at its arrival time or later.
            if len(busy_bays) < k:
                arrival_time, _, duration = ships[next_ship]

                # Since a bay is free, the next ship can start immediately when it arrives.
                start_time: int = arrival_time

                # Waiting time is zero, so it is always within T.
                finish_time: int = start_time + duration
                heappush(busy_bays, finish_time)
                next_ship += 1
                continue

            # At this point, all k bays are busy.
            # The earliest time any bay becomes free is the smallest finish time.
            earliest_free: int = busy_bays[0]

            arrival_time, _, duration = ships[next_ship]

            # Two possibilities:
            #
            # 1) The next ship arrives before or exactly when the earliest bay frees.
            #    Then the ship must wait until earliest_free, because no bay is free earlier.
            #
            # 2) The next ship arrives after the earliest bay frees.
            #    Then before this ship even arrives, one or more bays may become free.
            #    Those bays simply stay idle until the ship arrives, because there are no
            #    earlier waiting ships left unstarted (otherwise next_ship would be one of them).
            #
            # In either case, the actual start time for the next ship is:
            # max(arrival_time, earliest_free)
            start_time = max(arrival_time, earliest_free)

            # Check the waiting-time constraint.
            if start_time - arrival_time > T:
                return False

            # We are now assigning one bay to this ship.
            # Remove the bay that becomes free first, because that is the bay used.
            heappop(busy_bays)

            # The ship occupies that bay until start_time + duration.
            heappush(busy_bays, start_time + duration)

            next_ship += 1

        # If we started every ship without violating the waiting limit, k bays are enough.
        return True


if __name__ == "__main__":
    solution = Solution()

    arrival1 = [1, 2, 4]
    unload1 = [5, 2, 3]
    T1 = 2
    print(solution.minimumDockBays(arrival1, unload1, T1))  # Expected: 2

    arrival2 = [0, 1, 1, 3]
    unload2 = [4, 2, 5, 1]
    T2 = 1
    print(solution.minimumDockBays(arrival2, unload2, T2))  # Expected: 3