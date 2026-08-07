"""
Title: Count Pairs of Orders With the Same Item Set

Problem Description:
You are given a list of customer orders from an online store. Each order is represented
as a list of item IDs. The same item ID may appear multiple times inside one order if
the customer bought more than one copy of that item. Two orders are considered
equivalent if they contain exactly the same distinct item IDs, regardless of the order
of items in the list and regardless of how many times each item appears. In other
words, each order should be treated as a set of item IDs, not a multiset.

Your task is to return the number of unordered pairs of equivalent orders.

For example, the orders [4, 2, 4, 7], [7, 2, 4], and [2, 7, 7, 4] are all equivalent
because their distinct item set is {2, 4, 7}. Each pair among these orders should be
counted.

Design an efficient solution using hashing. A common approach is to normalize each
order into a canonical representation of its distinct items, then count how many times
each representation appears.

Constraints:
- 1 <= orders.length <= 100000
- 1 <= total number of item IDs across all orders <= 200000
- 1 <= item IDs <= 1000000000
- Each order contains at least 1 item

Example 1:
Input: orders = [[1,2,2,3],[3,1,2],[4,4],[4,5],[5,4,4]]
Output: 2
Explanation: The first two orders both map to the set {1,2,3}, contributing 1 pair.
The last two orders both map to the set {4,5}, contributing 1 pair. Total = 2.

Example 2:
Input: orders = [[8],[8,8],[1,2],[2,1],[1,1,2,2],[3]]
Output: 4
Explanation: [8] and [8,8] form 1 equivalent pair. The three orders [1,2], [2,1],
and [1,1,2,2] all map to {1,2}, contributing 3 pairs. [3] has no match. Total = 4.
"""

from typing import Dict, FrozenSet, List


class Solution:
    def _normalize_order(self, order: List[int]) -> FrozenSet[int]:
        """
        Convert one order into a canonical representation based only on distinct items.

        We use a frozenset because:
        - A normal set removes duplicates automatically.
        - A frozenset is immutable, so it can be used as a dictionary key.
        - Order does not matter, which matches the problem definition exactly.

        Args:
            order: A list of item IDs for one customer order.

        Returns:
            A frozenset containing the distinct item IDs from the order.

        Time complexity:
            O(k), where k is the number of items in the order.

        Space complexity:
            O(u), where u is the number of distinct items in the order.
        """
        return frozenset(order)

    def count_equivalent_order_pairs(self, orders: List[List[int]]) -> int:
        """
        Count unordered pairs of orders that have the same distinct item set.

        The key idea is:
        1. Normalize each order into a canonical "set of distinct items".
        2. Use a hash map to count how many times each normalized form has appeared.
        3. When we see a normalized form again, every previous occurrence forms a new pair
           with the current order.

        Example:
        If a normalized form has already appeared 3 times, then the 4th matching order
        creates 3 new pairs with those previous 3 orders.

        Args:
            orders: A list of orders, where each order is a list of item IDs.

        Returns:
            The total number of unordered equivalent-order pairs.

        Time complexity:
            O(T), where T is the total number of item IDs across all orders.
            Each item is processed once while building frozensets.

        Space complexity:
            O(N + U), where N is the number of orders and U reflects the stored
            normalized representations in the hash map.
        """
        # This dictionary maps:
        #   normalized_order_representation -> how many times we have seen it so far
        #
        # Example:
        #   frozenset({1, 2, 3}) -> 2
        #
        # Meaning:
        #   We have already processed 2 orders whose distinct item set is {1, 2, 3}.
        seen_count: Dict[FrozenSet[int], int] = {}

        # This will store the final answer:
        # the number of unordered pairs of equivalent orders.
        pair_count: int = 0

        # Process each order one by one.
        for order in orders:
            # Step 1: Normalize the current order.
            #
            # Why normalize?
            # The problem says:
            # - item order inside the list does not matter
            # - duplicate copies inside one order do not matter
            #
            # So:
            #   [1,2,2,3], [3,1,2], and [2,3,1,1]
            # should all become the same canonical representation.
            #
            # Using frozenset(order):
            #   [1,2,2,3] -> frozenset({1,2,3})
            normalized: FrozenSet[int] = self._normalize_order(order)

            # Step 2: Find how many matching normalized orders we have already seen.
            #
            # If we have seen this same normalized set `m` times before,
            # then the current order forms exactly `m` new unordered pairs:
            #
            #   current order paired with each previous matching order
            #
            # Example:
            #   previous matching count = 2
            #   current order creates 2 new pairs
            previous_matches: int = seen_count.get(normalized, 0)

            # Add those newly formed pairs to the answer.
            pair_count += previous_matches

            # Step 3: Record that we have now seen one more order with this normalized form.
            seen_count[normalized] = previous_matches + 1

        # After processing all orders, pair_count contains the total number of
        # unordered equivalent pairs.
        return pair_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # [1,2,2,3] -> {1,2,3}
    # [3,1,2]   -> {1,2,3}   => 1 pair with the first
    # [4,4]     -> {4}
    # [4,5]     -> {4,5}
    # [5,4,4]   -> {4,5}     => 1 pair with [4,5]
    # Total = 2
    orders1: List[List[int]] = [[1, 2, 2, 3], [3, 1, 2], [4, 4], [4, 5], [5, 4, 4]]
    result1: int = solution.count_equivalent_order_pairs(orders1)
    print("Example 1 Output:", result1)  # Expected: 2

    # Example 2:
    # [8]         -> {8}
    # [8,8]       -> {8}       => 1 pair
    # [1,2]       -> {1,2}
    # [2,1]       -> {1,2}     => 1 new pair
    # [1,1,2,2]   -> {1,2}     => 2 new pairs (with the previous two)
    # [3]         -> {3}
    # Total = 1 + 3 = 4
    orders2: List[List[int]] = [[8], [8, 8], [1, 2], [2, 1], [1, 1, 2, 2], [3]]
    result2: int = solution.count_equivalent_order_pairs(orders2)
    print("Example 2 Output:", result2)  # Expected: 4

    # Additional quick sanity check:
    # Three equivalent orders all mapping to {2,4,7}
    # Number of unordered pairs among 3 items = 3
    orders3: List[List[int]] = [[4, 2, 4, 7], [7, 2, 4], [2, 7, 7, 4]]
    result3: int = solution.count_equivalent_order_pairs(orders3)
    print("Additional Example Output:", result3)  # Expected: 3