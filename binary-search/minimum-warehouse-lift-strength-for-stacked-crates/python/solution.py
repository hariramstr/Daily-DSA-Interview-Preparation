"""
Title: Minimum Warehouse Lift Strength for Stacked Crates

Problem Description:
A warehouse uses an automated lift to move crates in the given order from left to right.
The weight of the i-th crate is weights[i]. The lift has a strength limit S. It may load
consecutive crates into the same trip as long as the total weight of that trip does not
exceed S. Once a trip starts, crates cannot be reordered or skipped, and every crate must
be moved exactly once.

You are also given an integer maxTrips, the maximum number of trips the warehouse is
willing to allow in one shift. Your task is to compute the minimum lift strength S such
that all crates can be moved in at most maxTrips trips.

This is a decision/optimization problem: for any candidate strength S, you can check
whether it is possible to partition the array into at most maxTrips contiguous groups
where each group sum is at most S. The answer is the smallest such S.

Return that minimum possible strength.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= 1000000000
- 1 <= maxTrips <= weights.length
- The answer fits in a 64-bit signed integer

Example 1:
Input: weights = [7,2,5,10,8], maxTrips = 2
Output: 18

Example 2:
Input: weights = [4,4,4,4,4], maxTrips = 3
Output: 8
"""

from typing import List


class Solution:
    def can_ship_with_strength(self, weights: List[int], max_trips: int, strength: int) -> bool:
        """
        Check whether all crates can be moved using at most max_trips trips
        when each trip may carry consecutive crates with total weight <= strength.

        Args:
            weights: List of crate weights in fixed left-to-right order.
            max_trips: Maximum allowed number of trips.
            strength: Candidate lift strength to test.

        Returns:
            True if the crates can be partitioned into at most max_trips contiguous
            groups such that each group sum is <= strength, otherwise False.

        Time complexity:
            O(n), where n is the number of crates.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We greedily pack as many consecutive crates as possible into the current trip.
        # Why is greedy correct here?
        # Because for a fixed strength limit, combining crates into the current trip
        # whenever possible can only reduce (or keep the same) the total number of trips.
        # If we started a new trip earlier than necessary, we would never use fewer trips.
        trips_used: int = 1
        current_trip_weight: int = 0

        for weight in weights:
            # If a single crate is heavier than the candidate strength, then this strength
            # is impossible immediately, because every crate must be moved and crates cannot
            # be split across trips.
            if weight > strength:
                return False

            # Try to place the current crate into the ongoing trip.
            if current_trip_weight + weight <= strength:
                current_trip_weight += weight
            else:
                # The current crate does not fit in the current trip, so we must start
                # a new trip beginning with this crate.
                trips_used += 1
                current_trip_weight = weight

                # Early stopping optimization:
                # If we already exceeded the allowed number of trips, there is no need
                # to continue scanning the rest of the array.
                if trips_used > max_trips:
                    return False

        # If we finished processing all crates without exceeding max_trips,
        # then this candidate strength is feasible.
        return True

    def minimum_lift_strength(self, weights: List[int], max_trips: int) -> int:
        """
        Compute the minimum lift strength needed to move all crates in order
        using at most max_trips trips.

        Args:
            weights: List of crate weights in fixed left-to-right order.
            max_trips: Maximum allowed number of trips.

        Returns:
            The smallest integer strength S such that the crates can be partitioned
            into at most max_trips contiguous groups with each group sum <= S.

        Time complexity:
            O(n * log(sum(weights))), because each binary search step performs
            an O(n) feasibility check.

        Space complexity:
            O(1), excluding the input list.
        """
        # Binary search is the key idea.
        #
        # We are searching for the smallest feasible strength S.
        # This works because feasibility is monotonic:
        #
        # - If a strength S is enough, then any larger strength is also enough.
        # - If a strength S is not enough, then any smaller strength is also not enough.
        #
        # That monotonic property makes binary search valid.

        # Lower bound:
        # The lift must be able to carry at least the heaviest single crate.
        left: int = max(weights)

        # Upper bound:
        # In the worst case, the lift carries all crates in one trip.
        # That requires strength equal to the total sum.
        right: int = sum(weights)

        # We now binary search over the answer range [left, right].
        while left < right:
            # Midpoint candidate strength.
            # Using integer division keeps everything in whole numbers.
            mid: int = (left + right) // 2

            # Check whether this candidate strength is sufficient.
            if self.can_ship_with_strength(weights, max_trips, mid):
                # If mid works, then the true answer is mid or smaller.
                # So we keep the left half, including mid.
                right = mid
            else:
                # If mid does not work, then the answer must be larger than mid.
                left = mid + 1

        # When left == right, binary search has converged to the smallest feasible strength.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    weights1: List[int] = [7, 2, 5, 10, 8]
    max_trips1: int = 2
    result1: int = solution.minimum_lift_strength(weights1, max_trips1)
    print("Example 1:")
    print(f"weights = {weights1}")
    print(f"maxTrips = {max_trips1}")
    print(f"Minimum lift strength = {result1}")
    print("Expected = 18")
    print()

    # Example 2
    weights2: List[int] = [4, 4, 4, 4, 4]
    max_trips2: int = 3
    result2: int = solution.minimum_lift_strength(weights2, max_trips2)
    print("Example 2:")
    print(f"weights = {weights2}")
    print(f"maxTrips = {max_trips2}")
    print(f"Minimum lift strength = {result2}")
    print("Expected = 8")
    print()

    # Additional beginner-friendly test cases

    # Case where each crate can be its own trip, so answer is max(weights)
    weights3: List[int] = [1, 2, 3, 4]
    max_trips3: int = 4
    result3: int = solution.minimum_lift_strength(weights3, max_trips3)
    print("Additional Test 1:")
    print(f"weights = {weights3}")
    print(f"maxTrips = {max_trips3}")
    print(f"Minimum lift strength = {result3}")
    print("Expected = 4")
    print()

    # Case where only one trip is allowed, so answer is sum(weights)
    weights4: List[int] = [3, 1, 2, 7]
    max_trips4: int = 1
    result4: int = solution.minimum_lift_strength(weights4, max_trips4)
    print("Additional Test 2:")
    print(f"weights = {weights4}")
    print(f"maxTrips = {max_trips4}")
    print(f"Minimum lift strength = {result4}")
    print("Expected = 13")