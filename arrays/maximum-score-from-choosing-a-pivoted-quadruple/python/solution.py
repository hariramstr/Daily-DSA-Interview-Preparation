"""
Title: Maximum Score from Choosing a Pivoted Quadruple

Problem Description:
You are given an integer array nums of length n. A pivoted quadruple is a choice of
four indices (a, b, c, d) such that 0 <= a < b < c < d < n and b and c act as the
two middle anchors of the quadruple. The score of such a quadruple is defined as:

    (nums[a] - nums[b]) * (nums[c] - nums[d])

Your task is to return the maximum possible score over all valid pivoted quadruples.
If every possible quadruple has a negative score, you must still return the largest
value among them. It is guaranteed that n >= 4.

Constraints:
- 4 <= n <= 200000
- -1000000000 <= nums[i] <= 1000000000
- The answer fits in a signed 64-bit integer

Key idea:
For each possible middle split (b, c) with b < c, the expression is:

    (best choice of nums[a] - nums[b] with a < b) *
    (best choice of nums[c] - nums[d] with d > c)

However, because multiplication can involve positive and negative values, for each
side we must keep both extremes:
- minimum possible left difference and maximum possible left difference
- minimum possible right difference and maximum possible right difference

Then for each valid pair (b, c), the best product is the maximum among the four
combinations of those extremes.

A direct O(n^2) scan over all (b, c) is too slow. We can simplify further:
- For fixed c, the right-side value depends only on c.
- For all b < c, we only need the best/worst left difference among indices before c.

So we precompute:
1) left_min_end[i], left_max_end[i]:
   considering all b <= i, the minimum/maximum value of (nums[a] - nums[b]) with a < b
2) right_min_start[i], right_max_start[i]:
   for fixed c = i, the minimum/maximum value of (nums[i] - nums[d]) with d > i

Then for each c in [2, n-2], combine:
- left extremes from positions b < c  => left_*_end[c-1]
- right extremes for this c           => right_*_start[c]
"""

from typing import List


class Solution:
    def maximumScore(self, nums: List[int]) -> int:
        """
        Compute the maximum score of any valid pivoted quadruple.

        A valid quadruple is (a, b, c, d) with a < b < c < d, and score:
            (nums[a] - nums[b]) * (nums[c] - nums[d])

        Args:
            nums: List of integers.

        Returns:
            The maximum possible score among all valid quadruples.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # ---------------------------------------------------------------
        # Step 1: Precompute, for every position b, the best and worst
        # possible left-side difference:
        #
        #     nums[a] - nums[b], where a < b
        #
        # For a fixed b:
        # - The maximum left difference is achieved by choosing the largest
        #   value seen before b as nums[a].
        # - The minimum left difference is achieved by choosing the smallest
        #   value seen before b as nums[a].
        #
        # Then we build prefix arrays:
        # - left_max_end[i] = maximum left difference using any b <= i
        # - left_min_end[i] = minimum left difference using any b <= i
        #
        # Why do we need prefix extremes instead of only per-position values?
        # Because when we later fix c, any b < c is allowed. So we want the
        # best/worst left difference among all valid b before c.
        # ---------------------------------------------------------------
        left_max_at_b: List[int] = [0] * n
        left_min_at_b: List[int] = [0] * n

        max_before: int = nums[0]
        min_before: int = nums[0]

        # b must be at least 1 because we need a < b.
        for b in range(1, n):
            # Best possible positive-leaning difference for this exact b.
            left_max_at_b[b] = max_before - nums[b]

            # Best possible negative-leaning difference for this exact b.
            left_min_at_b[b] = min_before - nums[b]

            # Update prefix seen values for future positions.
            if nums[b] > max_before:
                max_before = nums[b]
            if nums[b] < min_before:
                min_before = nums[b]

        left_max_end: List[int] = [0] * n
        left_min_end: List[int] = [0] * n

        # Index 0 cannot serve as b, so these values are placeholders.
        left_max_end[0] = -10**30
        left_min_end[0] = 10**30

        for i in range(1, n):
            if i == 1:
                left_max_end[i] = left_max_at_b[i]
                left_min_end[i] = left_min_at_b[i]
            else:
                left_max_end[i] = max(left_max_end[i - 1], left_max_at_b[i])
                left_min_end[i] = min(left_min_end[i - 1], left_min_at_b[i])

        # ---------------------------------------------------------------
        # Step 2: Precompute, for every position c, the best and worst
        # possible right-side difference:
        #
        #     nums[c] - nums[d], where d > c
        #
        # For a fixed c:
        # - The maximum right difference is achieved by subtracting the
        #   smallest value after c.
        # - The minimum right difference is achieved by subtracting the
        #   largest value after c.
        #
        # So while scanning from right to left:
        # - min_after tracks the smallest value to the right
        # - max_after tracks the largest value to the right
        # ---------------------------------------------------------------
        right_max_start: List[int] = [0] * n
        right_min_start: List[int] = [0] * n

        min_after: int = nums[n - 1]
        max_after: int = nums[n - 1]

        # c must be at most n-2 because we need d > c.
        for c in range(n - 2, -1, -1):
            right_max_start[c] = nums[c] - min_after
            right_min_start[c] = nums[c] - max_after

            if nums[c] < min_after:
                min_after = nums[c]
            if nums[c] > max_after:
                max_after = nums[c]

        # ---------------------------------------------------------------
        # Step 3: Try every valid c as the third index of the quadruple.
        #
        # For a chosen c:
        # - b must satisfy b < c
        # - a must satisfy a < b
        # Therefore the left side can use any valid pair ending at some b <= c-1,
        # which is exactly what left_max_end[c-1] and left_min_end[c-1] represent.
        #
        # The right side is fixed to start at c, so we use:
        # - right_max_start[c]
        # - right_min_start[c]
        #
        # Because multiplication can flip sign, the maximum product must be one
        # of the four combinations of extreme values:
        #   left_max * right_max
        #   left_max * right_min
        #   left_min * right_max
        #   left_min * right_min
        #
        # This is a standard property: for two intervals [Lmin, Lmax] and
        # [Rmin, Rmax], the maximum product over all pairs is attained at
        # one of the corners.
        # ---------------------------------------------------------------
        answer: int = -10**30

        # c must be at least 2 so that there exists a < b < c.
        # c must be at most n-2 so that there exists d > c.
        for c in range(2, n - 1):
            left_max: int = left_max_end[c - 1]
            left_min: int = left_min_end[c - 1]
            right_max: int = right_max_start[c]
            right_min: int = right_min_start[c]

            candidate_1: int = left_max * right_max
            candidate_2: int = left_max * right_min
            candidate_3: int = left_min * right_max
            candidate_4: int = left_min * right_min

            best_here: int = max(candidate_1, candidate_2, candidate_3, candidate_4)

            if best_here > answer:
                answer = best_here

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example-like test cases.
    # Note:
    # The problem statement examples contain inconsistencies in the narrative.
    # We print the actual algorithm outputs for the given arrays.

    sample_inputs: List[List[int]] = [
        [8, 1, 9, 2, 7],
        [5, 10, 3, 8, 1, 6],
        [8, 1, 9, 1, 2],
        [1, 2, 3, 4],
        [4, 3, 2, 1],
        [-5, -1, -3, 2, -4, 6],
    ]

    for nums in sample_inputs:
        result = solution.maximumScore(nums)
        print(f"nums = {nums}")
        print(f"maximum score = {result}")
        print("-" * 40)