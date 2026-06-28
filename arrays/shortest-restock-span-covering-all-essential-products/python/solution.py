"""
Title: Shortest Restock Span Covering All Essential Products

Problem Description:
A warehouse records the product ID of each item restocked during a day in the order
they arrive. You are given an integer array restocks, where restocks[i] is the
product ID of the i-th restocked item, and an integer array essentials containing
the list of product IDs that must all appear in a report. Your task is to return
the length of the shortest contiguous span of restocks that contains every product
ID from essentials at least once. If no such span exists, return -1.

Duplicate values may appear in restocks, but essentials contains distinct product IDs.
The order of products inside the chosen span does not matter. The goal is to find
the smallest window in the array that covers the full required set.

This problem models a realistic inventory monitoring task where analysts need the
smallest time interval that proves all critical products were replenished.

Constraints:
- 1 <= restocks.length <= 200000
- 1 <= essentials.length <= 200000
- 1 <= product ID <= 1000000000
- essentials contains distinct values

Example 1:
Input: restocks = [7, 2, 5, 2, 9, 5, 1, 9, 7], essentials = [5, 1, 7]
Output: 4
Explanation: The shortest valid span is [5, 1, 9, 7], which has length 4.

Example 2:
Input: restocks = [4, 4, 3, 8, 6, 3, 2], essentials = [3, 2, 5]
Output: -1
Explanation: Product 5 never appears in restocks, so no contiguous span can cover all
essential products.

Return only the minimum length, not the subarray itself.
"""

from typing import Dict, List, Set


class Solution:
    def shortest_restock_span(self, restocks: List[int], essentials: List[int]) -> int:
        """
        Find the length of the shortest contiguous subarray that contains all
        essential product IDs at least once.

        Args:
            restocks: List of product IDs in the order they were restocked.
            essentials: Distinct product IDs that must all appear in the chosen span.

        Returns:
            The minimum length of a contiguous span covering all essentials,
            or -1 if no such span exists.

        Time complexity:
            O(n + m), where n is len(restocks) and m is len(essentials).
            Each pointer in the sliding window moves at most n times.

        Space complexity:
            O(m), for the essential set and frequency map of relevant items.
        """
        # Convert essentials into a set so we can:
        # 1. Check membership in O(1) average time.
        # 2. Know exactly how many distinct required product IDs must be covered.
        #
        # Since the problem guarantees essentials contains distinct values,
        # the set size is exactly the number of required unique products.
        essential_set: Set[int] = set(essentials)

        # If there are more required distinct products than total restocks,
        # it is impossible to cover them all in any contiguous span.
        if len(essential_set) > len(restocks):
            return -1

        # This dictionary stores how many times each essential product appears
        # inside the current sliding window [left, right].
        #
        # We only track counts for essential products because non-essential
        # products do not help satisfy the requirement.
        window_counts: Dict[int, int] = {}

        # "formed" tells us how many distinct essential product IDs are currently
        # present in the window with count >= 1.
        #
        # Example:
        # essentials = [5, 1, 7]
        # If current window contains 5 twice and 7 once, but no 1,
        # then formed = 2.
        formed: int = 0

        # Total number of distinct essential products we need to have present.
        required: int = len(essential_set)

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far.
        # Start with infinity so any valid window will be smaller.
        min_length: int = float("inf")

        # Expand the window by moving "right" from left to right across the array.
        for right, product_id in enumerate(restocks):
            # Only essential products matter for satisfying the requirement.
            # Non-essential products may still sit inside the window, but we do not
            # need to count them in our frequency map.
            if product_id in essential_set:
                # Increase the count of this essential product in the current window.
                window_counts[product_id] = window_counts.get(product_id, 0) + 1

                # If this product's count became 1, it means this essential product
                # has just become newly covered by the current window.
                if window_counts[product_id] == 1:
                    formed += 1

            # Once the window covers all required essential products,
            # try shrinking it from the left to make it as short as possible.
            #
            # This is the key sliding window idea:
            # - Expand right until the window is valid.
            # - Then shrink left while it remains valid.
            # This guarantees we examine each index only a constant number of times.
            while formed == required and left <= right:
                # Current window [left, right] is valid, so update the best answer.
                current_length: int = right - left + 1
                if current_length < min_length:
                    min_length = current_length

                # We now attempt to remove restocks[left] from the window
                # and see whether the window can remain valid.
                left_product: int = restocks[left]

                if left_product in essential_set:
                    # Decrease the count because this item is leaving the window.
                    window_counts[left_product] -= 1

                    # If the count becomes 0, this essential product is no longer
                    # covered by the window, so the window stops being valid.
                    if window_counts[left_product] == 0:
                        formed -= 1

                # Move the left boundary rightward to shrink the window.
                left += 1

        # If min_length was never updated, no valid window exists.
        if min_length == float("inf"):
            return -1

        return min_length

    def min_span_covering_essentials(self, restocks: List[int], essentials: List[int]) -> int:
        """
        Wrapper method that calls the main shortest-span algorithm.

        Args:
            restocks: List of product IDs in restock order.
            essentials: Distinct product IDs that must all appear.

        Returns:
            The minimum valid span length, or -1 if impossible.

        Time complexity:
            O(n + m), where n is len(restocks) and m is len(essentials).

        Space complexity:
            O(m).
        """
        return self.shortest_restock_span(restocks, essentials)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    restocks_1: List[int] = [7, 2, 5, 2, 9, 5, 1, 9, 7]
    essentials_1: List[int] = [5, 1, 7]
    result_1: int = solution.min_span_covering_essentials(restocks_1, essentials_1)
    print(result_1)  # Expected: 4

    # Example 2
    restocks_2: List[int] = [4, 4, 3, 8, 6, 3, 2]
    essentials_2: List[int] = [3, 2, 5]
    result_2: int = solution.min_span_covering_essentials(restocks_2, essentials_2)
    print(result_2)  # Expected: -1

    # Additional quick sanity checks
    restocks_3: List[int] = [1, 2, 3, 4]
    essentials_3: List[int] = [2, 3]
    result_3: int = solution.min_span_covering_essentials(restocks_3, essentials_3)
    print(result_3)  # Expected: 2

    restocks_4: List[int] = [5, 5, 5, 1, 7]
    essentials_4: List[int] = [5, 1, 7]
    result_4: int = solution.min_span_covering_essentials(restocks_4, essentials_4)
    print(result_4)  # Expected: 3