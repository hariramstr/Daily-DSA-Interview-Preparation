"""
Title: Minimum Refuels to Reach the Last Checkpoint with Spillover Fuel

Problem Description:
You are planning a road trip through a straight desert highway. The destination is at
position `target`, measured in miles from the start. Your car begins at position `0`
with `startFuel` liters of fuel, and it consumes exactly 1 liter per mile. Along the
way, there are gas stations represented by an array `stations`, where
`stations[i] = [position_i, fuel_i]` means there is a station at mile `position_i`
containing `fuel_i` liters of fuel.

Unlike a normal refueling problem, every time you stop at a station, you must take all
of its fuel. If your tank already contains some fuel, that remaining fuel stays in the
tank, so the station's fuel simply spills over into your current amount. You may stop
at any subset of stations that you can physically reach, and each station can be used
at most once.

Return the minimum number of refueling stops needed to reach `target`. If it is
impossible, return `-1`.

Constraints:
- 1 <= target <= 10^9
- 0 <= startFuel <= 10^9
- 0 <= stations.length <= 10^5
- 0 < position_i < target
- 1 <= fuel_i <= 10^9
- stations is sorted by position_i in strictly increasing order

Examples:
1)
Input: target = 100, startFuel = 25, stations = [[10,30],[20,20],[40,40],[70,25]]
Output: 3

2)
Input: target = 120, startFuel = 50, stations = [[25,25],[60,30],[90,20]]
Output: 2
"""

from typing import List
import heapq


class Solution:
    def minRefuelStops(self, target: int, startFuel: int, stations: List[List[int]]) -> int:
        """
        Compute the minimum number of refueling stops needed to reach the target.

        The key greedy idea is:
        - Move forward as far as current fuel allows.
        - While moving, collect all stations that are reachable into a max-heap.
        - If we cannot move farther but still have not reached the target, refuel from
          the previously passed reachable station that gives the most fuel.
        - This greedy choice is optimal because when we are forced to refuel, taking the
          largest available fuel extends our reach the most and minimizes future stops.

        Args:
            target: The destination position in miles.
            startFuel: Initial amount of fuel in the car.
            stations: A sorted list of [position, fuel] gas stations.

        Returns:
            The minimum number of refueling stops required to reach target, or -1 if
            reaching target is impossible.

        Time complexity:
            O(n log n), where n is the number of stations.
            Each station is pushed into the heap once and popped at most once.

        Space complexity:
            O(n) for the heap of reachable stations.
        """
        # This heap will store fuel amounts of stations we have already passed or can
        # currently reach.
        #
        # Python's heapq is a min-heap by default, but we need a max-heap behavior
        # because when we must refuel, we want to choose the station with the largest
        # fuel amount among all reachable unused stations.
        #
        # To simulate a max-heap, we store negative fuel values.
        max_heap: List[int] = []

        # `furthest_reach` means the farthest mile marker we can currently get to with
        # the fuel we have accumulated so far.
        #
        # At the beginning, we are at position 0 with `startFuel`, so the farthest
        # reachable point is exactly `startFuel`.
        furthest_reach = startFuel

        # `station_index` points to the next station in sorted order that we have not
        # yet processed.
        #
        # Because stations are already sorted by position, we can scan them once from
        # left to right.
        station_index = 0

        # `stops` counts how many times we have chosen to refuel.
        stops = 0

        # Continue until our current reachable distance is enough to get to the target.
        #
        # If `furthest_reach >= target`, then we can already arrive without any more
        # refueling decisions.
        while furthest_reach < target:
            # Add every station that is reachable with our current fuel into the heap.
            #
            # Why do we do this?
            # Because any station at position <= furthest_reach can be visited before we
            # run out of fuel. Once such a station is reachable, it becomes a candidate
            # for refueling whenever we need it.
            #
            # We do not immediately decide to stop there. Instead, we postpone the
            # decision and keep all reachable options available in the heap. This is the
            # heart of the greedy strategy.
            while station_index < len(stations) and stations[station_index][0] <= furthest_reach:
                position, fuel = stations[station_index]

                # Push negative fuel to simulate max-heap behavior.
                heapq.heappush(max_heap, -fuel)
                station_index += 1

            # If there are no reachable stations left to use, and we still cannot reach
            # the target, then the journey is impossible.
            #
            # This means:
            # - We have already added every station we can physically reach.
            # - None remain available to refuel from.
            # - Our current reach is still short of the target.
            if not max_heap:
                return -1

            # Refuel from the reachable station with the largest fuel amount.
            #
            # Why largest?
            # If we are forced to make a stop, taking the biggest available fuel gives
            # the maximum extension of our reachable distance. This greedy choice is
            # proven optimal for minimizing the number of stops.
            best_fuel = -heapq.heappop(max_heap)
            furthest_reach += best_fuel
            stops += 1

        # Once the loop ends, `furthest_reach >= target`, so we can reach the
        # destination with exactly `stops` refuels.
        return stops


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target_1 = 100
    start_fuel_1 = 25
    stations_1 = [[10, 30], [20, 20], [40, 40], [70, 25]]
    result_1 = solution.minRefuelStops(target_1, start_fuel_1, stations_1)
    print("Example 1 Output:", result_1)  # Expected: 3

    # Example 2
    target_2 = 120
    start_fuel_2 = 50
    stations_2 = [[25, 25], [60, 30], [90, 20]]
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