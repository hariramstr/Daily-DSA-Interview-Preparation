"""
Title: Minimum Daily Packaging Rate for Sequential Orders

Problem Description:
A fulfillment center must prepare customer orders in the exact order they appear in
an array `orders`, where `orders[i]` is the number of items in the `i`-th order.
The center operates for exactly `d` days. Each day, it chooses a consecutive block
of remaining orders and packages them from left to right. However, the total number
of items packaged on a single day cannot exceed a fixed daily rate `R`. An order
cannot be split across multiple days: if an order starts on a day, all of its items
must be packaged that same day.

Your task is to find the minimum integer daily rate `R` such that all orders can be
completed within `d` days.

This is not simply a greedy partitioning problem unless the candidate rate is fixed.
A correct solution should exploit the monotonic nature of feasibility: if a rate `R`
is sufficient, then any rate larger than `R` is also sufficient. Use this property
to design an efficient algorithm.

Return the smallest possible `R`.

Constraints:
- 1 <= orders.length <= 2 * 10^5
- 1 <= orders[i] <= 10^9
- 1 <= d <= 10^9
- Orders must remain in the original order.
- Each day must process a contiguous sequence of unsent orders.
- An individual order cannot be split.

Example 1:
Input: orders = [7, 2, 5, 10, 8], d = 2
Output: 18
Explanation: With rate 18, one valid schedule is [7, 2, 5] and [10, 8].
Any rate below 18 fails because the last two orders would require more than one day.

Example 2:
Input: orders = [3, 6, 7, 11], d = 4
Output: 11
Explanation: Since orders cannot be split, the rate must be at least the largest
single order size, which is 11. Using rate 11, the orders can be scheduled as
[3], [6], [7], [11].
"""

from typing import List


class Solution:
    def can_finish(self, orders: List[int], d: int, rate: int) -> bool:
        """
        Check whether all orders can be completed within at most `d` days
        using the given daily packaging rate.

        The orders must stay in their original order, and each day we greedily
        pack as many consecutive orders as possible without exceeding `rate`.

        Args:
            orders: List of order sizes.
            d: Maximum number of days allowed.
            rate: Candidate daily packaging rate to test.

        Returns:
            True if all orders can be completed within `d` days, otherwise False.

        Time complexity:
            O(n), where n is the number of orders.

        Space complexity:
            O(1), excluding input storage.
        """
        # We start with day 1 because if there is at least one order,
        # we need at least one day to process something.
        days_used: int = 1

        # `current_load` tracks how many items have already been assigned
        # to the current day.
        current_load: int = 0

        # We process orders from left to right exactly once.
        for order in orders:
            # Safety check:
            # If a single order is larger than the candidate rate,
            # then this rate is immediately impossible because orders
            # cannot be split across days.
            if order > rate:
                return False

            # If adding this order would exceed today's allowed rate,
            # we must start a new day for this order.
            if current_load + order > rate:
                days_used += 1
                current_load = order

                # Early stopping optimization:
                # If we already need more than `d` days, there is no need
                # to continue scanning the remaining orders.
                if days_used > d:
                    return False
            else:
                # Otherwise, we can safely include this order in the current day.
                current_load += order

        # If we finished scanning all orders and never exceeded `d` days,
        # then this rate is feasible.
        return True

    def minimum_daily_rate(self, orders: List[int], d: int) -> int:
        """
        Compute the minimum integer daily packaging rate needed to complete
        all orders within `d` days while preserving order and not splitting orders.

        This uses binary search on the answer:
        - Lower bound = largest single order
        - Upper bound = sum of all orders

        The feasibility test is monotonic:
        if a rate works, then any larger rate also works.

        Args:
            orders: List of order sizes.
            d: Maximum number of days allowed.

        Returns:
            The smallest feasible daily packaging rate.

        Time complexity:
            O(n * log(sum(orders))), where n is the number of orders.

        Space complexity:
            O(1), excluding input storage.
        """
        # The minimum possible rate must be at least the largest single order,
        # because an order cannot be split across multiple days.
        left: int = max(orders)

        # The maximum possible rate is the sum of all orders,
        # which would allow everything to be done in one day.
        right: int = sum(orders)

        # Binary search for the smallest feasible rate.
        #
        # Invariant:
        # - The answer is always somewhere in the range [left, right].
        # - If `mid` is feasible, we try smaller values by moving `right`.
        # - If `mid` is not feasible, we must move `left` upward.
        while left < right:
            # Standard midpoint calculation.
            mid: int = left + (right - left) // 2

            # Test whether this candidate rate is enough.
            if self.can_finish(orders, d, mid):
                # `mid` works, so the answer could be `mid` or something smaller.
                right = mid
            else:
                # `mid` does not work, so we must search larger rates.
                left = mid + 1

        # When the loop ends, `left == right`, and that value is the
        # smallest feasible daily rate.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    orders1: List[int] = [7, 2, 5, 10, 8]
    d1: int = 2
    result1: int = solution.minimum_daily_rate(orders1, d1)
    print(f"Orders: {orders1}, Days: {d1}, Minimum Daily Rate: {result1}")
    # Expected: 18

    # Example 2
    orders2: List[int] = [3, 6, 7, 11]
    d2: int = 4
    result2: int = solution.minimum_daily_rate(orders2, d2)
    print(f"Orders: {orders2}, Days: {d2}, Minimum Daily Rate: {result2}")
    # Expected: 11

    # Additional quick sanity checks
    orders3: List[int] = [1, 2, 3, 4, 5]
    d3: int = 5
    result3: int = solution.minimum_daily_rate(orders3, d3)
    print(f"Orders: {orders3}, Days: {d3}, Minimum Daily Rate: {result3}")
    # Expected: 5

    orders4: List[int] = [1, 2, 3, 4, 5]
    d4: int = 1
    result4: int = solution.minimum_daily_rate(orders4, d4)
    print(f"Orders: {orders4}, Days: {d4}, Minimum Daily Rate: {result4}")
    # Expected: 15