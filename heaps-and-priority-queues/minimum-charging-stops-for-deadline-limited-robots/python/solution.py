"""
Title: Minimum Charging Stops for Deadline-Limited Robots

Problem Description:
A warehouse robot starts at position 0 with initial battery `startCharge` and must
reach position `target` on a straight track. Moving 1 unit of distance consumes
1 unit of battery. Along the track there are charging stations, where station `i`
is described by [position_i, charge_i, expiry_i].

If the robot arrives at that station at time `t` (time equals total distance already
traveled), it may collect the full `charge_i` only if `t <= expiry_i`. Otherwise that
station has already shut down and provides nothing. Charging itself takes no extra time,
and the robot may choose whether or not to use an available station when it passes it.
The robot cannot move backward.

Return the minimum number of charging stops needed to reach `target`, or -1 if it is
impossible.

Key challenge:
Unlike the classic minimum refueling stops problem, a station may be physically reachable
but unusable if the robot reaches it after its expiry time. Because time equals traveled
distance and charging takes zero time, a station is usable exactly when the robot reaches
its position and position_i <= expiry_i. This means usability depends only on the station
itself, not on which charging choices were made earlier.

Expected approach:
A priority queue / heap based greedy strategy.
"""

from heapq import heappop, heappush
from typing import List, Tuple


class Solution:
    def minChargingStops(
        self,
        target: int,
        startCharge: int,
        stations: List[List[int]],
    ) -> int:
        """
        Compute the minimum number of charging stops needed to reach the target.

        The algorithm first filters out stations that are impossible to use at all:
        if a station is at position p and expires at time e, then the robot reaches
        that position at time exactly p, so the station is usable iff p <= e.

        After filtering, the problem becomes the classic minimum refueling stops problem:
        while moving forward, add all reachable usable stations into a max-heap by charge.
        Whenever current reachable distance is not enough to continue, greedily activate
        the previously passed reachable station with the largest charge.

        Args:
            target: Destination position.
            startCharge: Initial battery amount.
            stations: List of [position, charge, expiry].

        Returns:
            Minimum number of charging stops, or -1 if impossible.

        Time complexity:
            O(n log n), where n is the number of stations.

        Space complexity:
            O(n), for the filtered list and heap.
        """
        # ------------------------------------------------------------
        # Step 1: Keep only stations that can ever be used.
        #
        # Why is this valid?
        # - Time equals total distance traveled.
        # - The robot reaches position p at time exactly p, regardless of how much
        #   charging it did before, because charging takes zero time and movement
        #   speed is 1 unit distance per 1 unit time.
        # - Therefore station [p, charge, expiry] is usable iff p <= expiry.
        #
        # Any station with p > expiry is already expired by the time the robot gets
        # there, so it can never contribute and can be discarded immediately.
        # ------------------------------------------------------------
        usable_stations: List[Tuple[int, int]] = []
        for position, charge, expiry in stations:
            if position <= expiry:
                usable_stations.append((position, charge))

        # ------------------------------------------------------------
        # Step 2: Sort usable stations by position.
        #
        # We process stations from left to right, exactly in the order the robot
        # would encounter them on the track.
        # ------------------------------------------------------------
        usable_stations.sort()

        # ------------------------------------------------------------
        # Step 3: Greedy traversal with a max-heap.
        #
        # `max_reach` means the farthest position the robot can currently reach
        # with the battery accumulated so far.
        #
        # Initially, the robot can reach `startCharge`.
        #
        # We maintain a heap of charges from all usable stations whose positions
        # are <= max_reach, meaning the robot can pass them with current energy.
        #
        # Python's heapq is a min-heap, so to simulate a max-heap we push negative
        # charges. The most valuable available station is then popped first.
        #
        # Greedy reason:
        # Whenever we are stuck, using the largest available charge gives the biggest
        # extension of reach per stop, which is optimal for minimizing the number of
        # stops. This is the same core proof as the classic refueling problem.
        # ------------------------------------------------------------
        max_reach: int = startCharge
        stops: int = 0
        index: int = 0
        max_heap: List[int] = []

        # ------------------------------------------------------------
        # Continue until the robot can reach the target.
        # ------------------------------------------------------------
        while max_reach < target:
            # --------------------------------------------------------
            # Add every usable station that lies at or before `max_reach`
            # into the heap, because the robot can reach and pass those
            # stations with the battery currently available.
            #
            # We do not need to decide immediately whether to use them.
            # Instead, we store them as "options" and only activate one
            # when we actually get stuck. This delayed decision is the
            # standard greedy trick that ensures the minimum number of stops.
            # --------------------------------------------------------
            while index < len(usable_stations) and usable_stations[index][0] <= max_reach:
                position, charge = usable_stations[index]
                heappush(max_heap, -charge)
                index += 1

            # --------------------------------------------------------
            # If there is no previously passed usable station available
            # in the heap, then we cannot extend our reach any further.
            # That means the target is impossible to reach.
            # --------------------------------------------------------
            if not max_heap:
                return -1

            # --------------------------------------------------------
            # Use the largest available charge among all stations we have
            # already passed and that were usable when passed.
            #
            # This increases our reachable distance as much as possible
            # with a single additional stop.
            # --------------------------------------------------------
            best_charge: int = -heappop(max_heap)
            max_reach += best_charge
            stops += 1

        # ------------------------------------------------------------
        # Once max_reach >= target, the robot can reach the destination.
        # ------------------------------------------------------------
        return stops


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target_1 = 25
    start_charge_1 = 10
    stations_1 = [[5, 8, 7], [9, 7, 12], [14, 10, 20]]
    result_1 = solution.minChargingStops(target_1, start_charge_1, stations_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2
    target_2 = 30
    start_charge_2 = 8
    stations_2 = [[6, 5, 5], [7, 20, 6], [10, 10, 15]]
    result_2 = solution.minChargingStops(target_2, start_charge_2, stations_2)
    print("Example 2 Result:", result_2)  # Expected: -1

    # Additional quick checks
    target_3 = 100
    start_charge_3 = 100
    stations_3 = [[10, 10, 10], [20, 20, 25]]
    result_3 = solution.minChargingStops(target_3, start_charge_3, stations_3)
    print("Additional Check 1 Result:", result_3)  # Expected: 0

    target_4 = 50
    start_charge_4 = 10
    stations_4 = [[10, 10, 10], [20, 10, 20], [30, 10, 30], [40, 10, 40]]
    result_4 = solution.minChargingStops(target_4, start_charge_4, stations_4)
    print("Additional Check 2 Result:", result_4)  # Expected: 4