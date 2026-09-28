"""
Title: Find the First Day With Consecutive Stock Refill

Problem Description:
A store tracks the number of units added to inventory each day in an integer array
refills, where refills[i] is the number of items restocked on day i.

A manager wants to know the earliest day when the store begins a streak of at least
k consecutive days with a positive refill. In other words, find the smallest index i
such that refills[i], refills[i + 1], ..., refills[i + k - 1] are all greater than 0.

If no such streak exists, return -1.

Important edge cases:
- If k is 1, any day with a positive refill is already a valid streak.
- If the array length is smaller than k, the answer must be -1.
- A refill value of 0 breaks any consecutive streak.

Constraints:
- 1 <= refills.length <= 100000
- 0 <= refills[i] <= 1000000
- 1 <= k <= 100000
"""

from typing import List


class Solution:
    def first_consecutive_refill_day(self, refills: List[int], k: int) -> int:
        """
        Find the earliest starting index of a streak of at least k consecutive
        days where each refill value is greater than 0.

        Args:
            refills: A list of non-negative integers where refills[i] represents
                the number of items restocked on day i.
            k: The required number of consecutive positive-refill days.

        Returns:
            The smallest starting index of the first valid streak of length k,
            or -1 if no such streak exists.

        Time complexity:
            O(n), where n is the length of refills, because we scan the array once.

        Space complexity:
            O(1), because we use only a few extra variables.
        """
        # First, store the length of the input list so we do not repeatedly call len(refills).
        # This is not required for correctness, but it makes the code slightly cleaner
        # and avoids repeated length lookups.
        n: int = len(refills)

        # If the array is shorter than k, it is impossible to have k consecutive days.
        # Example:
        # refills = [1, 2], k = 3
        # There are only 2 days total, so the answer must be -1.
        if n < k:
            return -1

        # This variable keeps track of the current number of consecutive days
        # with a positive refill (> 0) ending at the current position.
        #
        # For example, while scanning:
        # refills = [0, 3, 2, 5, 0, 4]
        # consecutive_positive values would evolve like:
        # day 0 -> 0
        # day 1 -> 1
        # day 2 -> 2
        # day 3 -> 3
        #
        # The moment this count reaches k, we know we found a valid streak.
        consecutive_positive: int = 0

        # Scan the array from left to right because we need the FIRST valid starting index.
        # The first time we detect a streak of length k, we can immediately return its start.
        for i, refill in enumerate(refills):
            # If today's refill is positive, it extends the current streak.
            if refill > 0:
                consecutive_positive += 1
            else:
                # A refill of 0 breaks the streak completely.
                # We reset the count because any future streak must start after this day.
                consecutive_positive = 0

            # If we have reached at least k consecutive positive days,
            # then the current day i is the end of a valid streak.
            #
            # The starting index of this streak is:
            # i - k + 1
            #
            # Example:
            # If i = 3 and k = 3, then the streak covers indices [1, 2, 3],
            # so the start is 3 - 3 + 1 = 1.
            if consecutive_positive >= k:
                return i - k + 1

        # If we finish scanning the entire array and never find a streak of length k,
        # then no valid answer exists.
        return -1


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement:
    # refills = [0, 3, 2, 5, 0, 4], k = 3
    # The first streak of 3 consecutive positive values is [3, 2, 5],
    # which starts at index 1.
    refills1: List[int] = [0, 3, 2, 5, 0, 4]
    k1: int = 3
    result1: int = solution.first_consecutive_refill_day(refills1, k1)
    print(f"Example 1 result: {result1}")  # Expected: 1

    # Example 2 from the problem statement:
    # refills = [1, 0, 2, 3, 0, 1], k = 2
    # The first streak of 2 consecutive positive values is [2, 3],
    # which starts at index 2.
    refills2: List[int] = [1, 0, 2, 3, 0, 1]
    k2: int = 2
    result2: int = solution.first_consecutive_refill_day(refills2, k2)
    print(f"Example 2 result: {result2}")  # Expected: 2

    # Additional beginner-friendly test:
    # If k = 1, any positive refill day is valid.
    # The first positive value is at index 1.
    refills3: List[int] = [0, 5, 0, 2]
    k3: int = 1
    result3: int = solution.first_consecutive_refill_day(refills3, k3)
    print(f"Additional test 1 result: {result3}")  # Expected: 1

    # Additional test:
    # No streak of length 2 exists because zeros keep breaking the sequence.
    refills4: List[int] = [1, 0, 1, 0, 1]
    k4: int = 2
    result4: int = solution.first_consecutive_refill_day(refills4, k4)
    print(f"Additional test 2 result: {result4}")  # Expected: -1

    # Additional test:
    # Entire array is positive, so the first streak of length 4 starts at index 0.
    refills5: List[int] = [2, 1, 3, 4, 5]
    k5: int = 4
    result5: int = solution.first_consecutive_refill_day(refills5, k5)
    print(f"Additional test 3 result: {result5}")  # Expected: 0