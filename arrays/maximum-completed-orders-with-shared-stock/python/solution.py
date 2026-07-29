"""
Title: Maximum Completed Orders with Shared Stock

Problem Description:
You are managing a warehouse that stores items of different product types. The array
`stock` represents how many units are currently available for each product type, where
`stock[i]` is the number of units of type `i`. Each customer order must be fulfilled
using units from exactly one product type, and every fulfilled order must contain the
same number of units, called the order size.

You are also given an integer `k`, the number of customer orders that must be fulfilled.
You may split the inventory of a single product type across multiple orders, but you
cannot combine units from different product types to form one order. For example, if
`stock[i] = 11` and the order size is `3`, that product type can support `3` full
orders, with `2` units left unused.

Return the maximum possible order size such that at least `k` orders can be fulfilled.

If it is impossible to fulfill `k` orders even with order size `1`, return `0`.

This problem asks you to determine the largest feasible uniform order size. A brute-force
search over all possible sizes may be too slow when stock counts are large, so an
efficient solution is expected.
"""

from typing import List


class Solution:
    def _can_fulfill(self, stock: List[int], k: int, order_size: int) -> bool:
        """
        Check whether at least k orders can be fulfilled with a given uniform order size.

        Args:
            stock: List of available unit counts for each product type.
            k: Required number of orders.
            order_size: Candidate size for each order.

        Returns:
            True if at least k orders can be formed, otherwise False.

        Time complexity:
            O(n), where n is the number of product types.

        Space complexity:
            O(1), excluding input storage.
        """
        # This variable will accumulate how many full orders we can create in total
        # across all product types for the current candidate order size.
        total_orders: int = 0

        # We inspect each product type independently because:
        # - An order must use units from exactly one product type.
        # - We ARE allowed to split one product type across many orders.
        # Therefore, for a product type with `units` items, the number of full orders
        # it contributes is simply `units // order_size`.
        for units in stock:
            total_orders += units // order_size

            # Early exit optimization:
            # As soon as we already know we can fulfill at least k orders, we do not
            # need to continue scanning the rest of the array.
            # This can save time on large inputs.
            if total_orders >= k:
                return True

        # If we finish the loop and still have fewer than k orders, then this order
        # size is not feasible.
        return False

    def maximum_order_size(self, stock: List[int], k: int) -> int:
        """
        Compute the largest possible uniform order size such that at least k orders
        can be fulfilled.

        Args:
            stock: List where stock[i] is the number of units available for product type i.
            k: Number of customer orders that must be fulfilled.

        Returns:
            The maximum feasible order size. Returns 0 if even order size 1 cannot
            fulfill k orders.

        Time complexity:
            O(n log M), where n is the length of stock and M is max(stock).
            The log M factor comes from binary search over possible order sizes.

        Space complexity:
            O(1), excluding input storage.
        """
        # First, handle the impossible case:
        # If the total number of units in the warehouse is less than k, then even
        # order size 1 cannot produce k orders, because each order needs at least
        # one unit.
        total_units: int = sum(stock)
        if total_units < k:
            return 0

        # We will use binary search on the answer.
        #
        # Why binary search works:
        # - Suppose an order size `x` is feasible.
        # - Then any smaller order size is also feasible, because making orders smaller
        #   can only increase (or keep the same) the number of full orders we can form.
        # - This creates a monotonic property:
        #       feasible, feasible, feasible, ..., not feasible, not feasible
        #   or equivalently when searching for the maximum feasible value:
        #       [1 ... answer] feasible
        #       [answer+1 ... max] not feasible
        #
        # That monotonic structure is exactly what binary search needs.
        left: int = 1
        right: int = max(stock)
        best: int = 0

        # Standard binary search loop:
        # We keep shrinking the search range until left passes right.
        while left <= right:
            # Midpoint candidate order size.
            mid: int = (left + right) // 2

            # Check whether this candidate size can produce at least k orders.
            if self._can_fulfill(stock, k, mid):
                # If `mid` works, it is a valid answer.
                # But we want the MAXIMUM valid answer, so we record it and try bigger.
                best = mid
                left = mid + 1
            else:
                # If `mid` does not work, then any larger size also cannot work.
                # So we search the smaller half.
                right = mid - 1

        # `best` now stores the largest feasible order size found.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # stock = [8, 5, 6], k = 5
    # For order size 3:
    #   8 // 3 = 2 orders
    #   5 // 3 = 1 order
    #   6 // 3 = 2 orders
    # Total = 5, so size 3 works.
    # For order size 4:
    #   8 // 4 = 2 orders
    #   5 // 4 = 1 order
    #   6 // 4 = 1 order
    # Total = 4, so size 4 does not work.
    # Therefore, the correct answer is 3.
    stock1: List[int] = [8, 5, 6]
    k1: int = 5
    result1: int = solution.maximum_order_size(stock1, k1)
    print(f"Input: stock = {stock1}, k = {k1}")
    print(f"Output: {result1}")
    print("Expected: 3")
    print()

    # Example 2:
    # stock = [2, 3], k = 6
    # Even with order size 1:
    #   2 // 1 = 2 orders
    #   3 // 1 = 3 orders
    # Total = 5, which is less than 6.
    # So it is impossible, and the correct answer is 0.
    stock2: List[int] = [2, 3]
    k2: int = 6
    result2: int = solution.maximum_order_size(stock2, k2)
    print(f"Input: stock = {stock2}, k = {k2}")
    print(f"Output: {result2}")
    print("Expected: 0")
    print()

    # Additional sample:
    stock3: List[int] = [11]
    k3: int = 3
    result3: int = solution.maximum_order_size(stock3, k3)
    print(f"Input: stock = {stock3}, k = {k3}")
    print(f"Output: {result3}")
    print("Expected: 3")