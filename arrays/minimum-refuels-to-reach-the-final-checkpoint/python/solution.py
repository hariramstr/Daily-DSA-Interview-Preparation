"""
Title: Minimum Refuels to Reach the Final Checkpoint

Problem Description:
You are driving along a straight highway toward a final checkpoint at distance `target`
miles from your starting point. Your car starts with `startFuel` liters of fuel, and it
uses exactly 1 liter per mile. Along the way, there are fuel stations described by a 2D
array `stations`, where `stations[i] = [position, fuel]` means there is a station at mile
`position` containing `fuel` liters you may take if you stop there. The stations are
sorted by position in strictly increasing order.

Whenever you reach a station, you may choose to refuel there and take all of its fuel,
or skip it. Refueling itself does not consume time, but each stop counts as one refuel.
Return the minimum number of refuels needed to reach the final checkpoint. If it is
impossible to reach the target, return `-1`.

This problem asks you to make optimal decisions while moving through the array of
stations. A greedy strategy is often needed because stopping too early may be wasteful,
while waiting too long may make the trip impossible.

Constraints:
- 1 <= target <= 10^9
- 0 <= startFuel <= 10^9
- 0 <= stations.length <= 10^5
- 0 < stations[i][0] < target
- 1 <= stations[i][1] <= 10^9
- stations is sorted by position in strictly increasing order
"""

from typing import List
import heapq


class Solution:
    def minRefuelStops(self, target: int, startFuel: int, stations: List[List[int]]) -> int:
        """
        Compute the minimum number of refueling stops needed to reach the target.

        The algorithm uses a greedy strategy with a max-heap:
        - Move forward as far as current fuel allows.
        - Add every reachable station's fuel to a heap of "available refuel choices".
        - Only refuel when necessary.
        - When refueling is required, always choose the previously passed station with
          the largest fuel amount, because that gives the biggest extension of reach
          per stop and helps minimize the total number of stops.

        Args:
            target: The final distance to reach.
            startFuel: Initial fuel available at the start.
            stations: A list of [position, fuel] stations sorted by position.

        Returns:
            The minimum number of refuels needed to reach the target, or -1 if impossible.

        Time complexity:
            O(n log n), where n is the number of stations.
            Each station is pushed into the heap once and popped at most once.

        Space complexity:
            O(n) for the heap in the worst case.
        """
        # A max-heap is the key data structure for the greedy strategy.
        #
        # Python's heapq module implements a min-heap, not a max-heap.
        # To simulate a max-heap, we store negative fuel values.
        #
        # Why do we need a max-heap?
        # Because when we can no longer move forward, we want to refuel from the
        # best station among all stations we have already passed and could have
        # chosen to stop at. "Best" means the station with the largest fuel amount.
        max_heap: List[int] = []

        # This variable tracks the farthest distance we can currently reach.
        #
        # At the beginning, before visiting any station, the farthest reachable
        # distance is exactly the amount of starting fuel, because fuel usage is
        # 1 liter per mile.
        reachable_distance: int = startFuel

        # This counts how many times we have refueled.
        refuels: int = 0

        # This index walks through the stations array from left to right.
        #
        # We will add stations to the heap once they become reachable.
        station_index: int = 0
        total_stations: int = len(stations)

        # Continue until our reachable distance is enough to get to the target.
        #
        # If reachable_distance >= target, then we can already arrive without any
        # more refueling decisions.
        while reachable_distance < target:
            # Add all stations that are at or before the current reachable distance.
            #
            # These are stations we can physically reach right now with our current fuel.
            # We do not immediately decide to refuel at them. Instead, we store their
            # fuel amounts as "available options" in the heap.
            #
            # This delayed decision is important:
            # - If we refuel too early, we might waste a stop.
            # - If we wait until necessary, we can choose the largest fuel among all
            #   reachable stations, which is optimal for minimizing the number of stops.
            while station_index < total_stations and stations[station_index][0] <= reachable_distance:
                station_position: int = stations[station_index][0]
                station_fuel: int = stations[station_index][1]

                # Push negative fuel to simulate max-heap behavior.
                heapq.heappush(max_heap, -station_fuel)

                # Move to the next station.
                station_index += 1

            # If there are no reachable stations available to refuel from,
            # and we still cannot reach the target, then the trip is impossible.
            #
            # This means:
            # - We have already added every station we could reach.
            # - None of them remain unused in the heap.
            # - So there is no way to extend our reachable distance further.
            if not max_heap:
                return -1

            # Greedy choice:
            # Refuel from the previously reachable station with the largest fuel amount.
            #
            # Why is this correct?
            # Because when we must refuel, taking the largest available fuel extends
            # our reach the most for this single stop, which helps minimize the total
            # number of stops needed.
            best_available_fuel: int = -heapq.heappop(max_heap)
            reachable_distance += best_available_fuel
            refuels += 1

        # If we exit the loop, reachable_distance >= target, so we can reach the target.
        return refuels


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target_1 = 100
    start_fuel_1 = 25
    stations_1 = [[25, 25], [50, 25], [75, 25]]
    result_1 = solution.minRefuelStops(target_1, start_fuel_1, stations_1)
    print("Example 1 Output:", result_1)  # Expected: 3

    # Example 2
    target_2 = 120
    start_fuel_2 = 50
    stations_2 = [[25, 30], [40, 20], [70, 40], [95, 30]]
    result_2 = solution.minRefuelStops(target_2, start_fuel_2, stations_2)
    print("Example 2 Output:", result_2)  # Expected: 2

    # Additional quick checks
    target_3 = 1
    start_fuel_3 = 1
    stations_3: List[List[int]] = []
    result_3 = solution.minRefuelStops(target_3, start_fuel_3, stations_3)
    print("Additional Check 1 Output:", result_3)  # Expected: 0

    target_4 = 100
    start_fuel_4 = 1
    stations_4 = [[10, 100]]
    result_4 = solution.minRefuelStops(target_4, start_fuel_4, stations_4)
    print("Additional Check 2 Output:", result_4)  # Expected: -1