"""
Title: Minimum Oven Capacity for Batch Cookies

Problem Description:
A bakery receives cookie orders in a fixed sequence. The i-th order requires trays[i]
cookie trays to be baked, and all trays assigned to the same baking session must be
processed together in the original order. The bakery has exactly d baking sessions
available for the day. In one session, the oven can bake a contiguous group of orders
as long as the total number of trays in that session does not exceed the oven capacity.

Your task is to find the minimum oven capacity needed so that all orders can be
completed within at most d sessions.

You may not reorder orders, split a single order across multiple sessions, or skip any
order. A larger oven capacity always makes the schedule at least as feasible as a
smaller one, so an efficient solution should take advantage of this monotonic property.

Return the smallest integer oven capacity that allows all orders to be baked within
d sessions.

Constraints:
- 1 <= trays.length <= 100000
- 1 <= trays[i] <= 1000000
- 1 <= d <= trays.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: trays = [8, 3, 5, 7, 2], d = 3
Output: 10

Example 2:
Input: trays = [4, 4, 4, 4], d = 2
Output: 8
"""

from typing import List


class Solution:
    def _can_finish_with_capacity(self, trays: List[int], d: int, capacity: int) -> bool:
        """
        Check whether all orders can be completed within at most d sessions
        using the given oven capacity.

        Args:
            trays: List of tray counts for each order, in fixed processing order.
            d: Maximum number of baking sessions allowed.
            capacity: Candidate oven capacity being tested.

        Returns:
            True if all orders can be baked in at most d sessions, otherwise False.

        Time complexity:
            O(n), where n is the number of orders.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We start by assuming we are using the first baking session.
        # Since there is at least one order, if capacity is valid at all,
        # we will need at least one session.
        sessions_used: int = 1

        # This variable stores the total trays currently assigned to the
        # ongoing session we are building greedily from left to right.
        current_session_load: int = 0

        # We process orders in their original fixed order because the problem
        # explicitly forbids reordering.
        for order_trays in trays:
            # If a single order is larger than the oven capacity, then this
            # capacity is immediately impossible. We cannot split an order
            # across sessions, so there is no workaround.
            if order_trays > capacity:
                return False

            # If adding this order keeps the current session within capacity,
            # we place it into the current session.
            if current_session_load + order_trays <= capacity:
                current_session_load += order_trays
            else:
                # Otherwise, the current session is full enough, and we must
                # start a new session for this order.
                sessions_used += 1
                current_session_load = order_trays

                # Early exit optimization:
                # If we already need more than d sessions, then this capacity
                # is not feasible, and there is no reason to continue scanning.
                if sessions_used > d:
                    return False

        # If we finished all orders using at most d sessions, then this
        # candidate capacity works.
        return True

    def minimum_oven_capacity(self, trays: List[int], d: int) -> int:
        """
        Find the minimum oven capacity needed to complete all orders within
        at most d baking sessions.

        Args:
            trays: List of tray counts for each order, in fixed processing order.
            d: Maximum number of baking sessions allowed.

        Returns:
            The smallest integer oven capacity that makes the schedule feasible.

        Time complexity:
            O(n * log(sum(trays))), because each binary search step performs
            an O(n) feasibility check.

        Space complexity:
            O(1), excluding input storage.
        """
        # The smallest possible valid capacity must be at least the largest
        # single order, because every order must fit by itself if necessary.
        left: int = max(trays)

        # The largest possible capacity we would ever need is the sum of all
        # orders, which would allow everything to be baked in one session.
        right: int = sum(trays)

        # We now binary search over the answer space [left, right].
        # This works because feasibility is monotonic:
        # - If a capacity works, then any larger capacity also works.
        # - If a capacity does not work, then any smaller capacity also fails.
        while left < right:
            # Midpoint candidate capacity.
            mid: int = left + (right - left) // 2

            # Check whether this candidate capacity is enough.
            if self._can_finish_with_capacity(trays, d, mid):
                # If mid works, we try to find an even smaller valid capacity.
                # So we keep the left half, including mid itself.
                right = mid
            else:
                # If mid does not work, then every capacity <= mid also fails.
                # So we must search strictly above mid.
                left = mid + 1

        # When left == right, binary search has converged to the smallest
        # feasible capacity.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # trays = [8, 3, 5, 7, 2], d = 3
    # Expected output: 10
    #
    # Quick correctness trace:
    # Capacity 10 works:
    #   Session 1: [8] -> 8
    #   Session 2: [3, 5] -> 8
    #   Session 3: [7, 2] -> 9
    # Uses 3 sessions, so feasible.
    #
    # Capacity 9 fails:
    #   Session 1: [8] -> 8
    #   Session 2: [3, 5] -> 8
    #   Session 3: [7, 2] -> 9 actually fits, but let's test greedily:
    #   Orders in sequence with capacity 9:
    #     [8], [3,5], [7,2] => 3 sessions, so 9 is feasible.
    #
    # Therefore the true minimum for this example is 9, not 10.
    # Since correctness is mandatory, we print the algorithm's correct result.
    trays1: List[int] = [8, 3, 5, 7, 2]
    d1: int = 3
    result1: int = solution.minimum_oven_capacity(trays1, d1)
    print(f"Input: trays = {trays1}, d = {d1}")
    print(f"Minimum oven capacity: {result1}")

    # Example 2:
    # trays = [4, 4, 4, 4], d = 2
    # Expected output: 8
    #
    # Trace:
    # Capacity 8 works:
    #   [4,4], [4,4]
    # Capacity 7 fails:
    #   [4], [4], [4], [4] -> 4 sessions needed
    trays2: List[int] = [4, 4, 4, 4]
    d2: int = 2
    result2: int = solution.minimum_oven_capacity(trays2, d2)
    print(f"Input: trays = {trays2}, d = {d2}")
    print(f"Minimum oven capacity: {result2}")

    # Additional small sanity checks for beginners:
    # If only one session is allowed, capacity must be the total sum.
    trays3: List[int] = [2, 6, 3]
    d3: int = 1
    result3: int = solution.minimum_oven_capacity(trays3, d3)
    print(f"Input: trays = {trays3}, d = {d3}")
    print(f"Minimum oven capacity: {result3}")

    # If the number of sessions equals the number of orders, capacity only
    # needs to be the largest single order.
    trays4: List[int] = [5, 1, 9, 2]
    d4: int = 4
    result4: int = solution.minimum_oven_capacity(trays4, d4)
    print(f"Input: trays = {trays4}, d = {d4}")
    print(f"Minimum oven capacity: {result4}")