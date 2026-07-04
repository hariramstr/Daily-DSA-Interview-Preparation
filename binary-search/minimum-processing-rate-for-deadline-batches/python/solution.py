"""
Title: Minimum Processing Rate for Deadline Batches

Problem Description:
A data platform receives analytics batches, where batch i contains batches[i] records
that must be processed by a single worker. The worker uses one fixed processing rate r
records per hour for the entire day. In one hour, the worker may process records from
only one batch, and if a batch has fewer than r remaining records, the worker still
spends the full hour finishing that batch. The batches can be processed in any order.

Given an integer array batches and an integer h, return the minimum integer processing
rate r such that all batches can be completed within h hours.

This is a realistic capacity-planning problem: choosing a rate that is too low misses
the deadline, while choosing a rate that is too high may waste resources. Your task is
to find the smallest feasible rate.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- batches.length <= h <= 1000000000
- The answer always exists.

Notes:
- The time needed for one batch of size x at rate r is ceil(x / r) hours.
- Since the order of processing does not change the total hours, you only need to
  determine whether a candidate rate is feasible.
- An O(n log M) solution is expected, where M is the maximum batch size.

Example 1:
Input: batches = [12, 7, 25, 9], h = 10
Output: 7

Example 2:
Input: batches = [30, 11, 23, 4, 20], h = 6
Output: 23
"""

from typing import List


class Solution:
    def hours_needed(self, batches: List[int], rate: int) -> int:
        """
        Compute the total number of hours required to process all batches
        using a fixed processing rate.

        Args:
            batches: List of batch sizes.
            rate: Fixed number of records processed per hour.

        Returns:
            The total hours needed to finish all batches at the given rate.

        Time complexity:
            O(n), where n is the number of batches.

        Space complexity:
            O(1), excluding input storage.
        """
        # We accumulate the total number of hours needed across all batches.
        total_hours: int = 0

        # For each batch, the worker needs ceil(batch / rate) hours.
        # Instead of importing math.ceil and using floating-point division,
        # we use the integer formula:
        #
        #     ceil(x / r) = (x + r - 1) // r
        #
        # This is faster, avoids floating-point precision issues,
        # and is the standard approach in interview-style problems.
        for batch in batches:
            total_hours += (batch + rate - 1) // rate

        return total_hours

    def minProcessingRate(self, batches: List[int], h: int) -> int:
        """
        Find the minimum integer processing rate that allows all batches
        to be completed within h hours.

        Args:
            batches: List of batch sizes.
            h: Maximum allowed total processing hours.

        Returns:
            The smallest integer rate that finishes all batches within h hours.

        Time complexity:
            O(n log M), where n is the number of batches and
            M is the maximum batch size.

        Space complexity:
            O(1), excluding input storage.
        """
        # We use binary search because:
        #
        # 1. If a rate r is feasible (can finish within h hours),
        #    then any larger rate is also feasible.
        # 2. If a rate r is not feasible,
        #    then any smaller rate is also not feasible.
        #
        # This "monotonic" true/false behavior is exactly what binary search needs.

        # The smallest possible rate is 1 record per hour.
        left: int = 1

        # The largest necessary rate is max(batches).
        # Why?
        # At this rate, even the largest batch finishes in 1 hour,
        # and every other batch also finishes in at most 1 hour.
        # Since the problem guarantees an answer exists and h >= len(batches),
        # this upper bound is always sufficient.
        right: int = max(batches)

        # Binary search over the answer space [left, right].
        while left < right:
            # Choose the middle candidate rate.
            mid: int = (left + right) // 2

            # Check how many hours this candidate rate would require.
            required_hours: int = self.hours_needed(batches, mid)

            # If the worker can finish within h hours at rate mid,
            # then mid is a valid answer candidate.
            #
            # But we are not done yet, because there might be a smaller
            # valid rate. So we keep searching on the left half,
            # including mid itself.
            if required_hours <= h:
                right = mid
            else:
                # If mid is too slow, then it cannot be the answer,
                # and neither can any smaller rate.
                # So we search strictly to the right of mid.
                left = mid + 1

        # When the loop ends, left == right, and that value is the
        # smallest feasible processing rate.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    batches1: List[int] = [12, 7, 25, 9]
    h1: int = 10
    result1: int = solution.minProcessingRate(batches1, h1)
    print("Example 1:")
    print(f"batches = {batches1}, h = {h1}")
    print(f"Minimum processing rate = {result1}")
    print()

    # Manual verification for Example 1:
    # Rate 7:
    # ceil(12/7) + ceil(7/7) + ceil(25/7) + ceil(9/7)
    # = 2 + 1 + 4 + 2 = 9 <= 10, so rate 7 works.
    #
    # Rate 6:
    # ceil(12/6) + ceil(7/6) + ceil(25/6) + ceil(9/6)
    # = 2 + 2 + 5 + 2 = 11 > 10, so rate 6 does not work.
    #
    # Therefore, the correct answer is 7.

    # Example 2
    batches2: List[int] = [30, 11, 23, 4, 20]
    h2: int = 6
    result2: int = solution.minProcessingRate(batches2, h2)
    print("Example 2:")
    print(f"batches = {batches2}, h = {h2}")
    print(f"Minimum processing rate = {result2}")
    print()

    # Manual verification for Example 2:
    # Rate 23:
    # ceil(30/23) + ceil(11/23) + ceil(23/23) + ceil(4/23) + ceil(20/23)
    # = 2 + 1 + 1 + 1 + 1 = 6 <= 6, so rate 23 works.
    #
    # Rate 22:
    # ceil(30/22) + ceil(11/22) + ceil(23/22) + ceil(4/22) + ceil(20/22)
    # = 2 + 1 + 2 + 1 + 1 = 7 > 6, so rate 22 does not work.
    #
    # Therefore, the correct answer is 23.