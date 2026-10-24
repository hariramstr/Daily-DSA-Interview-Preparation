"""
Title: Find the Earliest Fully Stocked Shelf

Problem Description:
A store receives a delivery log for a single shelf. The shelf has n slots, and each slot
should eventually contain exactly one product. You are given an array deliveries of length n,
where deliveries[i] is the slot number filled on minute i + 1. Each slot number from 1 to n
appears exactly once.

Return the earliest minute when the shelf becomes fully stocked from left to right without
any gaps, meaning slots 1 through k are all filled for some k, and k is as large as possible
at that minute. In this problem, you must return the earliest minute when the filled slots
form the prefix 1 through m, where m is the maximum slot number filled so far and all smaller
slot numbers are also already filled.

Equivalently, after processing minute t, look at the largest slot number seen so far. If every
slot from 1 up to that number has been filled, then the shelf is fully stocked up to that point.
Return the first minute when this happens. If it only happens after the final delivery, return n.

Constraints:
- 1 <= n <= 100000
- deliveries.length == n
- 1 <= deliveries[i] <= n
- All values in deliveries are distinct

Example 1:
Input: deliveries = [2,1,3,5,4]
Output: 2

Example 2:
Input: deliveries = [3,1,2,4]
Output: 3
"""

from typing import List


class Solution:
    def earliest_fully_stocked_shelf(self, deliveries: List[int]) -> int:
        """
        Find the earliest minute when all filled slots form a perfect prefix from 1
        up to the largest slot number seen so far.

        The key observation is:
        - After t minutes, exactly t distinct slots have been filled.
        - Let current_max be the largest slot number seen so far.
        - The filled slots are exactly the prefix [1, 2, ..., current_max] if and only if:
              current_max == t
          Why? Because:
          - We have filled exactly t distinct slots.
          - If the largest filled slot is t, then the only way to have t distinct values
            all within the range 1..t is to have every value from 1 to t.

        Args:
            deliveries: A permutation of slot numbers from 1 to n, where deliveries[i]
                is the slot filled at minute i + 1.

        Returns:
            The earliest 1-based minute when the filled slots form a gap-free prefix.

        Time complexity:
            O(n), because we scan the array once.

        Space complexity:
            O(1), because we only store a few variables.
        """
        # This variable keeps track of the largest slot number we have seen so far.
        # Example:
        # deliveries = [2, 1, 3, 5, 4]
        # after reading 2 -> current_max = 2
        # after reading 1 -> current_max = 2
        # after reading 3 -> current_max = 3
        current_max: int = 0

        # We process deliveries minute by minute.
        # We use enumerate(..., start=1) so that:
        # - minute is 1-based, matching the problem statement
        # - deliveries[0] corresponds to minute 1
        for minute, slot in enumerate(deliveries, start=1):
            # Update the largest slot number seen so far.
            # We do this because the condition we care about depends on the maximum
            # filled slot after the current minute.
            current_max = max(current_max, slot)

            # Core idea:
            # If current_max == minute, then among the first "minute" deliveries,
            # we have exactly "minute" distinct slot numbers, and all of them are
            # between 1 and current_max (= minute).
            #
            # Since there are exactly minute distinct integers in the range 1..minute,
            # they must be exactly:
            #   1, 2, 3, ..., minute
            #
            # That means the shelf is fully stocked from left to right with no gaps
            # up to the largest slot seen so far.
            #
            # This is the earliest such minute because we return immediately the first
            # time the condition becomes true.
            if current_max == minute:
                return minute

        # Because deliveries contains every number from 1 to n exactly once,
        # by the final minute we must have seen all slots 1..n.
        # Therefore the condition is guaranteed to be true at least at minute n.
        return len(deliveries)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1
    deliveries1: List[int] = [2, 1, 3, 5, 4]
    result1: int = solution.earliest_fully_stocked_shelf(deliveries1)
    print(f"deliveries = {deliveries1}")
    print(f"earliest fully stocked minute = {result1}")
    print()

    # Trace for Example 1:
    # Minute 1: seen [2], max = 2, minute = 1 -> not equal, so not fully stocked
    # Minute 2: seen [2, 1], max = 2, minute = 2 -> equal, answer = 2
    #
    # Expected output: 2

    # Sample input 2
    deliveries2: List[int] = [3, 1, 2, 4]
    result2: int = solution.earliest_fully_stocked_shelf(deliveries2)
    print(f"deliveries = {deliveries2}")
    print(f"earliest fully stocked minute = {result2}")
    print()

    # Trace for Example 2:
    # Minute 1: seen [3], max = 3, minute = 1 -> not equal
    # Minute 2: seen [3, 1], max = 3, minute = 2 -> not equal
    # Minute 3: seen [3, 1, 2], max = 3, minute = 3 -> equal, answer = 3
    #
    # Expected output: 3

    # Additional small test
    deliveries3: List[int] = [1]
    result3: int = solution.earliest_fully_stocked_shelf(deliveries3)
    print(f"deliveries = {deliveries3}")
    print(f"earliest fully stocked minute = {result3}")