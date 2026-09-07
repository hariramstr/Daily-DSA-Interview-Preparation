"""
Title: Find the First Local Peak Day

Problem Description:
You are given an integer array `visitors` where `visitors[i]` represents the number
of visitors to a store on day `i`. A day is called a local peak if its visitor count
is strictly greater than the visitor count of the previous day and strictly greater
than the visitor count of the next day.

In other words, for some index `i`, day `i` is a local peak when:
    visitors[i] > visitors[i - 1] and visitors[i] > visitors[i + 1]

Your task is to return the index of the first local peak day in the array.
If no such day exists, return -1.

Only days with both a previous and next day can be local peaks, so the first and
last elements can never be considered peaks.

A simple linear scan is expected.
"""

from typing import List


class Solution:
    def first_local_peak(self, visitors: List[int]) -> int:
        """
        Find the index of the first local peak in the visitors array.

        A local peak is an index i such that:
        - i has both a previous and next index
        - visitors[i] is strictly greater than visitors[i - 1]
        - visitors[i] is strictly greater than visitors[i + 1]

        Args:
            visitors: A list of integers where visitors[i] is the number of visitors
                on day i.

        Returns:
            The index of the first local peak if one exists; otherwise, -1.

        Time complexity:
            O(n), where n is the length of the array, because we scan the array once.

        Space complexity:
            O(1), because we use only a constant amount of extra space.
        """
        # We only need to check indices that have both neighbors.
        # That means valid candidate indices are from 1 to len(visitors) - 2.
        #
        # Why?
        # - Index 0 has no previous element, so it cannot be a local peak.
        # - Index len(visitors) - 1 has no next element, so it cannot be a local peak.
        #
        # We scan from left to right because the problem asks for the FIRST local peak.
        # The moment we find one, we can immediately return its index.
        for i in range(1, len(visitors) - 1):
            # Store the neighboring values in clearly named variables.
            # This makes the code easier to read for beginners and avoids repeating
            # the indexing expressions multiple times.
            previous_day_visitors: int = visitors[i - 1]
            current_day_visitors: int = visitors[i]
            next_day_visitors: int = visitors[i + 1]

            # Check the exact definition of a local peak:
            # current value must be STRICTLY greater than both neighbors.
            #
            # We use strict '>' comparisons, not '>='.
            # This is important because equal values do NOT count as a peak.
            if (
                current_day_visitors > previous_day_visitors
                and current_day_visitors > next_day_visitors
            ):
                # Since we are scanning from left to right, this is the first
                # local peak encountered, so we return immediately.
                return i

        # If the loop finishes, then no index satisfied the local peak condition.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # visitors = [12, 18, 15, 20, 19]
    # Index 1: 18 > 12 and 18 > 15 -> peak, so answer is 1
    visitors_1: List[int] = [12, 18, 15, 20, 19]
    result_1: int = solution.first_local_peak(visitors_1)
    print(f"Input: {visitors_1}")
    print(f"First local peak index: {result_1}")
    print()

    # Example 2:
    # visitors = [5, 7, 7, 6, 4]
    # Index 1: 7 is not strictly greater than next 7
    # Index 2: 7 is not strictly greater than previous 7
    # Index 3: 6 is not greater than previous 7
    # No peak exists, so answer is -1
    visitors_2: List[int] = [5, 7, 7, 6, 4]
    result_2: int = solution.first_local_peak(visitors_2)
    print(f"Input: {visitors_2}")
    print(f"First local peak index: {result_2}")
    print()

    # Additional sample:
    visitors_3: List[int] = [3, 9, 5]
    result_3: int = solution.first_local_peak(visitors_3)
    print(f"Input: {visitors_3}")
    print(f"First local peak index: {result_3}")