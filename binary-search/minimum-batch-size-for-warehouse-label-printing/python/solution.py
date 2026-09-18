"""
Title: Minimum Batch Size for Warehouse Label Printing

Problem Description:
A warehouse prints shipping labels for incoming orders using a single printer.
The orders must be processed in the given order, and each order has a required
number of labels. The warehouse operates for exactly d shifts, and during each
shift the printer can print labels for a contiguous group of orders. An order
cannot be split across two shifts: all labels for that order must be printed
within one shift. The total number of labels printed in a single shift cannot
exceed the printer's batch size limit.

Your task is to find the minimum batch size limit that allows all orders to be
completed within d shifts.

Formally, given an array labels where labels[i] is the number of labels needed
for the i-th order, partition the array into at most d contiguous groups such
that the maximum group sum is as small as possible. Return that minimum possible
maximum sum.

This problem is suitable for a binary search on the answer because if a batch
size x is sufficient to finish all orders in d shifts, then any batch size
larger than x is also sufficient.
"""

from typing import List


class Solution:
    def _can_finish_with_limit(self, labels: List[int], d: int, limit: int) -> bool:
        """
        Check whether all orders can be processed within at most d shifts
        if each shift is allowed to print at most `limit` labels.

        Args:
            labels: List of label counts for each order, in required processing order.
            d: Maximum number of shifts allowed.
            limit: Candidate batch size limit for one shift.

        Returns:
            True if the orders can be partitioned into at most d contiguous groups
            such that each group's sum is <= limit, otherwise False.

        Time complexity:
            O(n), where n is the number of orders.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We start by assuming we are using the first shift.
        shifts_used: int = 1

        # This variable stores the total labels currently assigned to the
        # ongoing shift we are building.
        current_shift_sum: int = 0

        # We process orders from left to right because the problem requires
        # preserving the original order and each shift must contain a contiguous block.
        for order_labels in labels:
            # If a single order is larger than the allowed limit, then this
            # candidate limit is impossible immediately, because an order
            # cannot be split across shifts.
            if order_labels > limit:
                return False

            # If adding this order to the current shift would exceed the limit,
            # we must start a new shift.
            if current_shift_sum + order_labels > limit:
                shifts_used += 1
                current_shift_sum = order_labels

                # If we already need more than d shifts, then this limit is not feasible.
                if shifts_used > d:
                    return False
            else:
                # Otherwise, we safely add this order to the current shift.
                current_shift_sum += order_labels

        # If we finish processing all orders without exceeding d shifts,
        # then this limit works.
        return True

    def minimum_batch_size(self, labels: List[int], d: int) -> int:
        """
        Find the minimum feasible batch size limit that allows all orders
        to be completed within at most d shifts.

        This uses binary search on the answer:
        - The smallest possible limit is max(labels), because every single order
          must fit into some shift.
        - The largest possible limit is sum(labels), which means all orders
          are printed in one shift.

        Args:
            labels: List of label counts for each order.
            d: Maximum number of shifts allowed.

        Returns:
            The minimum possible maximum shift sum.

        Time complexity:
            O(n * log(sum(labels) - max(labels) + 1)),
            where n is the number of orders.

        Space complexity:
            O(1), excluding input storage.
        """
        # The minimum possible answer cannot be smaller than the largest single order,
        # because no order can be split.
        left: int = max(labels)

        # The maximum possible answer is the sum of all orders, meaning we print
        # everything in one shift.
        right: int = sum(labels)

        # We will binary search for the smallest feasible limit.
        while left < right:
            # Midpoint candidate limit.
            mid: int = (left + right) // 2

            # If mid is sufficient, we try to find an even smaller feasible answer.
            if self._can_finish_with_limit(labels, d, mid):
                right = mid
            else:
                # If mid is not sufficient, we must increase the limit.
                left = mid + 1

        # At the end of binary search, left == right and points to the
        # minimum feasible batch size.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # labels = [8, 5, 3, 7, 6], d = 3
    # Correct answer is 13.
    labels1: List[int] = [8, 5, 3, 7, 6]
    d1: int = 3
    result1: int = solution.minimum_batch_size(labels1, d1)
    print(f"Example 1: labels = {labels1}, d = {d1}")
    print(f"Minimum batch size = {result1}")
    print("Expected = 13")
    print()

    # Example 2 from the prompt:
    # labels = [10, 2, 4, 9, 3], d = 2
    # Correct answer is 16.
    labels2: List[int] = [10, 2, 4, 9, 3]
    d2: int = 2
    result2: int = solution.minimum_batch_size(labels2, d2)
    print(f"Example 2: labels = {labels2}, d = {d2}")
    print(f"Minimum batch size = {result2}")
    print("Expected = 16")
    print()

    # Additional small sanity checks for beginners:
    labels3: List[int] = [1, 2, 3, 4, 5]
    d3: int = 2
    result3: int = solution.minimum_batch_size(labels3, d3)
    print(f"Additional Test 1: labels = {labels3}, d = {d3}")
    print(f"Minimum batch size = {result3}")
    print()

    labels4: List[int] = [7]
    d4: int = 1
    result4: int = solution.minimum_batch_size(labels4, d4)
    print(f"Additional Test 2: labels = {labels4}, d = {d4}")
    print(f"Minimum batch size = {result4}")