"""
Title: Minimum Battery Capacity for Delivery Drone Loops

Problem Description:
A delivery company operates a drone that must complete a fixed sequence of package stops in order.
The drone starts each day fully charged at the warehouse, visits the stops from left to right, and
may return to the warehouse to recharge whenever needed. A single trip from the warehouse can cover
a consecutive block of stops, but the total energy needed for that block must not exceed the drone's
battery capacity. After recharging, the drone resumes with the next unserved stop.

You are given an array energy where energy[i] is the energy required to serve stop i, and an integer
maxTrips representing the maximum number of warehouse departures allowed for the day. Your task is to
find the minimum battery capacity needed so that all stops can be served in order using at most
maxTrips trips.

Each trip must serve at least one stop, and stops cannot be reordered or split across trips. The
answer is the smallest integer capacity C such that the array can be partitioned into at most
maxTrips contiguous groups, where the sum of each group is at most C.

Constraints:
- 1 <= energy.length <= 100000
- 1 <= energy[i] <= 1000000000
- 1 <= maxTrips <= energy.length
- The result fits in a 64-bit signed integer

Example 1:
Input: energy = [7, 2, 5, 10, 8], maxTrips = 2
Output: 18
Explanation: With capacity 18, the drone can take trips [7, 2, 5] and [10, 8].
Any smaller capacity would require more than 2 trips.

Example 2:
Input: energy = [4, 4, 4, 4, 4], maxTrips = 3
Output: 8
Explanation: One valid plan is [4, 4], [4, 4], [4].
Capacity 7 is not enough because some trip would need to hold two stops totaling 8.
"""

from typing import List


class Solution:
    def trips_needed(self, energy: List[int], capacity: int) -> int:
        """
        Compute how many trips are required if the drone battery capacity is fixed.

        The method uses a greedy left-to-right scan:
        - Keep adding stops to the current trip while the total stays within capacity.
        - If adding the next stop would exceed capacity, start a new trip at that stop.

        This greedy choice is correct because for a fixed capacity, packing each trip with as many
        consecutive stops as possible minimizes the number of trips.

        Args:
            energy: List of energy costs for each stop, in required service order.
            capacity: Proposed battery capacity to test.

        Returns:
            The minimum number of trips needed to serve all stops with this capacity.

        Time complexity:
            O(n), where n is the number of stops.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We start with one trip because the problem guarantees at least one stop.
        trips: int = 1

        # This variable stores the total energy already assigned to the current trip.
        current_sum: int = 0

        # Process each stop in order because stops cannot be reordered.
        for required in energy:
            # If this single stop is larger than capacity, then this capacity is impossible.
            # We return a very large number of trips so the caller will treat it as infeasible.
            if required > capacity:
                return len(energy) + 1

            # If adding this stop still fits, keep it in the current trip.
            if current_sum + required <= capacity:
                current_sum += required
            else:
                # Otherwise, we must start a new trip beginning with this stop.
                trips += 1
                current_sum = required

        return trips

    def minimumBatteryCapacity(self, energy: List[int], maxTrips: int) -> int:
        """
        Find the minimum battery capacity that allows serving all stops in order
        using at most maxTrips trips.

        The solution uses binary search on the answer:
        - The smallest possible capacity is max(energy), because every stop must fit alone.
        - The largest possible capacity is sum(energy), which allows serving everything in one trip.
        - For a candidate capacity, a greedy scan tells us how many trips are needed.
        - If the number of trips is within maxTrips, the capacity is feasible and we try smaller.
        - Otherwise, the capacity is too small and we try larger.

        Args:
            energy: List of energy costs for each stop.
            maxTrips: Maximum number of trips allowed.

        Returns:
            The minimum feasible battery capacity.

        Time complexity:
            O(n log S), where n is the number of stops and S is the search range
            between max(energy) and sum(energy).

        Space complexity:
            O(1), ignoring input storage.
        """
        # Lower bound:
        # The battery must be at least large enough to handle the largest single stop,
        # because a stop cannot be split across trips.
        left: int = max(energy)

        # Upper bound:
        # If the battery can hold the sum of all stops, the drone can do everything in one trip.
        right: int = sum(energy)

        # This variable will store the best feasible answer found so far.
        answer: int = right

        # Standard binary search over the capacity range.
        while left <= right:
            # Midpoint capacity to test.
            mid: int = (left + right) // 2

            # Use the greedy helper to determine how many trips this capacity would require.
            needed: int = self.trips_needed(energy, mid)

            # If we can finish within the allowed number of trips, this capacity works.
            if needed <= maxTrips:
                # Record it as a candidate answer.
                answer = mid

                # Try to find an even smaller feasible capacity.
                right = mid - 1
            else:
                # Too many trips means the capacity is too small.
                # We must search larger capacities.
                left = mid + 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # energy = [7, 2, 5, 10, 8], maxTrips = 2
    # Expected output: 18
    #
    # Quick correctness trace:
    # - Capacity 18 works:
    #   Trip 1: 7 + 2 + 5 = 14
    #   Trip 2: 10 + 8 = 18
    #   Total trips = 2
    # - Capacity 17 fails:
    #   Trip 1: 7 + 2 + 5 = 14
    #   Trip 2: 10
    #   Trip 3: 8
    #   Total trips = 3 > 2
    energy1: List[int] = [7, 2, 5, 10, 8]
    max_trips1: int = 2
    result1: int = solution.minimumBatteryCapacity(energy1, max_trips1)
    print(result1)  # 18

    # Example 2:
    # energy = [4, 4, 4, 4, 4], maxTrips = 3
    # Expected output: 8
    #
    # Quick correctness trace:
    # - Capacity 8 works:
    #   Trip 1: 4 + 4 = 8
    #   Trip 2: 4 + 4 = 8
    #   Trip 3: 4
    #   Total trips = 3
    # - Capacity 7 fails:
    #   No trip can contain two 4s because 4 + 4 = 8 > 7
    #   So each stop needs its own trip => 5 trips > 3
    energy2: List[int] = [4, 4, 4, 4, 4]
    max_trips2: int = 3
    result2: int = solution.minimumBatteryCapacity(energy2, max_trips2)
    print(result2)  # 8