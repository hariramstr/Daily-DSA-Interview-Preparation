"""
Title: Maximum Free Days After Canceling One Booking Block

Problem Description:
You are given an integer n representing the number of days in a planning horizon,
labeled from 1 to n, and an array booked of length n where booked[i] is either 0 or 1.
A value of 1 means day i + 1 is already booked, and 0 means the day is free.

You are allowed to cancel at most one contiguous block of booked days. In other words,
you may choose indices l and r such that every value in booked[l..r] is 1, and change
all of them to 0. You may also choose not to cancel anything.

Return the maximum possible length of a contiguous run of free days after this operation.

This models a scheduling system where one existing reservation block can be removed to
create the longest uninterrupted free interval.

A cancellation block must be entirely inside one existing run of 1s. Canceling only part
of a booked run is allowed if that gives a better answer. Your goal is not to maximize
the number of canceled days, but to maximize the longest consecutive sequence of 0s in
the final array.

Constraints:
- 1 <= n <= 200000
- booked.length == n
- booked[i] is either 0 or 1
"""

from typing import List


class Solution:
    def max_free_days_after_one_cancellation(self, booked: List[int]) -> int:
        """
        Compute the maximum possible length of a contiguous free segment (0s)
        after canceling at most one contiguous block of booked days (1s).

        Args:
            booked: Binary array where 1 means booked and 0 means free.

        Returns:
            The maximum length of consecutive 0s obtainable after the operation.

        Time complexity:
            O(n), where n is the length of booked.

        Space complexity:
            O(n), for prefix/suffix helper arrays.
        """
        n: int = len(booked)

        # Edge case:
        # If the array is empty (not possible under the given constraints, but still
        # safe to handle), there are no days, so the longest free run is 0.
        if n == 0:
            return 0

        # left_zeros[i] will store:
        # "How many consecutive free days (0s) end exactly at index i?"
        #
        # Example:
        # booked = [0, 0, 1, 0]
        # left_zeros = [1, 2, 0, 1]
        #
        # Why do we need this?
        # If we decide to cancel a block of 1s from some run, then the best free segment
        # created by that cancellation can connect:
        #   - the free segment immediately to the left of that run
        #   - the canceled block itself (which becomes free)
        #   - the free segment immediately to the right of that run
        #
        # So we need quick access to the lengths of zero-runs adjacent to any run of 1s.
        left_zeros: List[int] = [0] * n

        # Build the left-to-right consecutive-zero counts.
        for i in range(n):
            if booked[i] == 0:
                # If current day is free:
                # - if this is the first day, the run length is 1
                # - otherwise extend the previous zero-run by 1
                left_zeros[i] = 1 if i == 0 else left_zeros[i - 1] + 1
            else:
                # If current day is booked, there is no zero-run ending here.
                left_zeros[i] = 0

        # right_zeros[i] will store:
        # "How many consecutive free days (0s) start exactly at index i?"
        #
        # Example:
        # booked = [0, 0, 1, 0, 0]
        # right_zeros = [2, 1, 0, 2, 1]
        #
        # This is the mirror image of left_zeros and lets us quickly know the free
        # segment immediately to the right of a run of 1s.
        right_zeros: List[int] = [0] * n

        # Build the right-to-left consecutive-zero counts.
        for i in range(n - 1, -1, -1):
            if booked[i] == 0:
                # If current day is free:
                # - if this is the last day, the run length is 1
                # - otherwise extend the next zero-run by 1
                right_zeros[i] = 1 if i == n - 1 else right_zeros[i + 1] + 1
            else:
                # If current day is booked, there is no zero-run starting here.
                right_zeros[i] = 0

        # Start with the answer equal to the best existing free run without canceling
        # anything. This is important because the problem says "at most one" cancellation,
        # so doing nothing is allowed.
        #
        # The maximum value in left_zeros is exactly the longest current run of 0s.
        best: int = max(left_zeros)

        # Now scan the array by runs of 1s.
        #
        # Key insight:
        # Suppose we have a run of 1s from index start to end.
        # If we cancel any contiguous sub-block inside this run, the resulting free segment
        # can only be formed by:
        #   left adjacent zeros + canceled block length + right adjacent zeros
        #
        # Since all values inside the chosen sub-block become 0, and we want the longest
        # possible free interval, there is never a disadvantage to canceling the entire
        # run of 1s rather than only part of it:
        # - canceling more 1s inside the same run only increases or preserves the free run
        # - it cannot break connectivity because the entire canceled region becomes 0
        #
        # Therefore, for each run of 1s, the optimal choice for that run is to cancel
        # the whole run.
        #
        # So for each run:
        # candidate = zeros immediately left + length of this 1-run + zeros immediately right
        i: int = 0
        while i < n:
            if booked[i] == 0:
                # Free day: not the start of a booked run, so just move on.
                i += 1
                continue

            # We found the start of a run of 1s.
            start: int = i

            # Advance i until we pass the entire contiguous run of 1s.
            while i < n and booked[i] == 1:
                i += 1

            # Now the run is from start to end inclusive.
            end: int = i - 1
            run_length: int = end - start + 1

            # Compute the length of the zero-run immediately to the left of this 1-run.
            #
            # If start == 0, there is no left side, so left_free = 0.
            # Otherwise, the cell start - 1 is either:
            # - 0, in which case left_zeros[start - 1] tells us the full zero-run length
            #   ending there
            # - or impossible here for adjacency to a 1-run start? Actually it can only be
            #   0 or out of bounds, because start is the first 1 in the run.
            left_free: int = 0 if start == 0 else left_zeros[start - 1]

            # Compute the length of the zero-run immediately to the right of this 1-run.
            #
            # If end == n - 1, there is no right side, so right_free = 0.
            # Otherwise, the cell end + 1 is the first cell after the 1-run. If it is 0,
            # right_zeros[end + 1] gives the full zero-run length starting there.
            right_free: int = 0 if end == n - 1 else right_zeros[end + 1]

            # If we cancel this entire run of 1s, it becomes a block of zeros of length
            # run_length, and it joins with the adjacent zero-runs on both sides.
            candidate: int = left_free + run_length + right_free

            # Update the global best answer.
            if candidate > best:
                best = candidate

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # booked = [0,1,1,0,0,1,0]
    # Cancel indices 1..2 (the two 1s), resulting in:
    # [0,0,0,0,0,1,0]
    # Longest free run = 5
    booked1: List[int] = [0, 1, 1, 0, 0, 1, 0]
    result1: int = solution.max_free_days_after_one_cancellation(booked1)
    print("Example 1 result:", result1)  # Expected: 5

    # Example 2:
    # booked = [1,0,1,1,1,0,1]
    # Cancel indices 2..4 (the middle three 1s), resulting in:
    # [1,0,0,0,0,0,1]
    # Longest free run = 5
    booked2: List[int] = [1, 0, 1, 1, 1, 0, 1]
    result2: int = solution.max_free_days_after_one_cancellation(booked2)
    print("Example 2 result:", result2)  # Expected: 5

    # Additional quick checks for clarity:
    booked3: List[int] = [0, 0, 0, 0]
    result3: int = solution.max_free_days_after_one_cancellation(booked3)
    print("All free days result:", result3)  # Expected: 4

    booked4: List[int] = [1, 1, 1, 1]
    result4: int = solution.max_free_days_after_one_cancellation(booked4)
    print("All booked days result:", result4)  # Expected: 4

    booked5: List[int] = [0, 1, 0, 1, 0]
    result5: int = solution.max_free_days_after_one_cancellation(booked5)
    print("Alternating days result:", result5)  # Expected: 3