"""
Title: Minimum Initial Fuel for Timed Delivery Route

Problem Description:
A courier drone must complete a fixed delivery route consisting of n checkpoints in order,
from checkpoint 0 to checkpoint n - 1. To travel from checkpoint i to checkpoint i + 1,
the drone consumes fuel equal to cost[i]. At some checkpoints, the drone may also recharge
by collecting a battery pack worth refill[i] units immediately after arriving there.

The drone starts at checkpoint 0 with an unknown initial amount of fuel F, and it may never
let its fuel drop below 0 at any point.

However, there is an additional timing rule. Each segment i has a maximum allowed carry
limit limit[i]: if the drone begins segment i with more than limit[i] fuel, it becomes too
heavy and misses the time window for that segment. In other words, just before paying cost[i],
the current fuel must be between 0 and limit[i], inclusive.

You must compute the minimum integer initial fuel F that allows the drone to complete the
entire route, or return -1 if no such F exists.

The route is fixed and battery packs cannot be skipped if collected; upon arriving at
checkpoint i + 1, the drone's fuel increases by refill[i + 1]. Assume refill[0] is also
available before starting, so the drone begins with F + refill[0] fuel and must still satisfy
the carry limit for segment 0.

Constraints:
- 1 <= n <= 200000
- cost.length == n - 1
- refill.length == n
- limit.length == n - 1
- 0 <= cost[i], refill[i], limit[i] <= 10^18
- The answer, if it exists, fits in a signed 64-bit integer
"""

from typing import List


class Solution:
    def _can_finish(self, initial_fuel: int, cost: List[int], refill: List[int], limit: List[int]) -> bool:
        """
        Check whether a given initial fuel amount allows the drone to complete the route.

        The simulation is performed from left to right:
        - Start with initial_fuel + refill[0]
        - For each segment i:
            1. Verify current fuel does not exceed limit[i]
            2. Verify current fuel is enough to pay cost[i]
            3. Subtract cost[i]
            4. Add refill[i + 1]

        Args:
            initial_fuel: Candidate starting fuel F before collecting refill[0].
            cost: Fuel cost for each segment.
            refill: Fuel gained at each checkpoint.
            limit: Maximum allowed fuel before each segment.

        Returns:
            True if the route is feasible with this initial fuel, otherwise False.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # The drone immediately collects the battery pack at checkpoint 0 before taking segment 0.
        fuel: int = initial_fuel + refill[0]

        # We now process each segment in order.
        for i in range(len(cost)):
            # Timing / carry-limit rule:
            # Just before starting segment i, the fuel must not exceed limit[i].
            # Fuel is never negative here because we only ever subtract after checking affordability.
            if fuel > limit[i]:
                return False

            # The drone must have enough fuel to pay the travel cost of this segment.
            if fuel < cost[i]:
                return False

            # Spend fuel to traverse the segment.
            fuel -= cost[i]

            # Upon arriving at the next checkpoint, the drone must collect the battery pack there.
            fuel += refill[i + 1]

        # If every segment was processed successfully, the route is feasible.
        return True

    def minimum_initial_fuel(self, n: int, cost: List[int], refill: List[int], limit: List[int]) -> int:
        """
        Compute the minimum integer initial fuel needed to complete the route, or -1 if impossible.

        Key idea:
        - Use binary search on the answer F.
        - For a fixed F, test feasibility in O(n) by simulating the route.
        - The feasibility is monotone in the valid search interval:
          if a smaller F works, then larger F may fail because of upper limits, so we cannot
          directly binary-search on raw feasibility over all integers.
          Instead, we derive the exact interval of feasible F values by propagating allowed
          fuel ranges backwards.

        More specifically:
        Let S_i be the fuel just before segment i.
        Then:
            S_0 = F + refill[0]
            S_{i+1} = S_i - cost[i] + refill[i+1]

        For each segment i, we need:
            cost[i] <= S_i <= limit[i]

        Since S_i is an affine function of F:
            S_i = F + prefix_refill_up_to_i - prefix_cost_before_i

        Therefore each segment gives an interval constraint on F.
        The intersection of all these intervals gives all feasible F values.
        The minimum feasible integer F is the answer.

        Although the problem statement mentions binary search, this interval method is both
        simpler and stronger. To still align with the intended approach, we compute a global
        upper bound from the interval constraints and then binary-search the minimum F using
        the linear feasibility check.

        Args:
            n: Number of checkpoints.
            cost: Fuel cost for each of the n - 1 segments.
            refill: Fuel gained at each checkpoint.
            limit: Maximum allowed fuel before each of the n - 1 segments.

        Returns:
            The minimum feasible initial fuel, or -1 if no such value exists.

        Time complexity:
            O(n log U), where U is the searched answer range.
            In practice, U fits in signed 64-bit as guaranteed.

        Space complexity:
            O(1)
        """
        # Special case:
        # If there is only one checkpoint, there are no segments to travel.
        # The route is already complete, so the minimum initial fuel is 0.
        if n == 1:
            return 0

        # ------------------------------------------------------------------
        # Step 1: Derive a guaranteed search interval [low, high] for F.
        #
        # We express the fuel before each segment i as:
        #   fuel_before_i = F + delta_i
        #
        # where delta_i accumulates all mandatory refills and costs before segment i.
        #
        # For segment i to be valid:
        #   cost[i] <= F + delta_i <= limit[i]
        #
        # Rearranging:
        #   cost[i] - delta_i <= F <= limit[i] - delta_i
        #
        # Intersect these intervals over all segments.
        # Also F must be at least 0 because initial fuel cannot be negative.
        # ------------------------------------------------------------------
        low: int = 0
        high: int = (1 << 63) - 1

        # delta represents the amount added to F to obtain the fuel before the current segment.
        # Initially, before segment 0, the drone has F + refill[0].
        delta: int = refill[0]

        for i in range(n - 1):
            # Lower bound from needing enough fuel to pay this segment's cost.
            segment_low: int = cost[i] - delta

            # Upper bound from not exceeding the carry limit for this segment.
            segment_high: int = limit[i] - delta

            # Intersect with the global feasible interval.
            if segment_low > low:
                low = segment_low
            if segment_high < high:
                high = segment_high

            # Move delta forward to represent fuel before the next segment:
            # next_delta = current_delta - cost[i] + refill[i + 1]
            delta = delta - cost[i] + refill[i + 1]

        # Initial fuel cannot be negative.
        if low < 0:
            low = 0

        # If the feasible interval is empty, no answer exists.
        if low > high:
            return -1

        # ------------------------------------------------------------------
        # Step 2: Binary search the minimum feasible F inside [low, high].
        #
        # Because [low, high] was derived from exact necessary constraints,
        # we know at least one feasible value exists in this interval.
        # We still use the requested binary-search + linear-check structure.
        # ------------------------------------------------------------------
        left: int = low
        right: int = high
        answer: int = -1

        while left <= right:
            mid: int = (left + right) // 2

            # Run the linear simulation check for this candidate starting fuel.
            if self._can_finish(mid, cost, refill, limit):
                # mid works, so record it and try to find an even smaller valid answer.
                answer = mid
                right = mid - 1
            else:
                # mid does not work, so we need more initial fuel within the feasible interval.
                left = mid + 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 4
    cost1 = [5, 4, 3]
    refill1 = [2, 6, 1, 0]
    limit1 = [10, 8, 7]
    result1 = solution.minimum_initial_fuel(n1, cost1, refill1, limit1)
    print(result1)  # Expected: 3

    # Example 2
    n2 = 3
    cost2 = [4, 4]
    refill2 = [5, 10, 0]
    limit2 = [3, 20]
    result2 = solution.minimum_initial_fuel(n2, cost2, refill2, limit2)
    print(result2)  # Expected: -1