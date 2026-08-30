"""
Title: Maximum Gain from Reversing One Sales Streak

Problem Description:
You are given an integer array nums representing the day-by-day profit impact of a
product campaign. A positive value means the campaign gained money that day, while
a negative value means it lost money. Management allows you to perform at most one
operation: choose a contiguous subarray and reverse its order. After this optional
reversal, you must evaluate the maximum possible sum of any contiguous subarray in
the modified array.

Return that maximum achievable contiguous profit.

A reversal does not change the values themselves, only their positions. You may also
choose not to reverse anything if the original array already gives the best answer.

Your task is to design an efficient algorithm for arrays large enough that trying
every possible reversal and recomputing every subarray would be too slow.

Constraints:
- 1 <= nums.length <= 2000
- -10^4 <= nums[i] <= 10^4
- The answer fits in a 32-bit signed integer

Examples:
1) nums = [4, -10, 3, 5]
   Reverse [-10, 3, 5] -> [4, 5, 3, -10]
   Best contiguous sum = 12

2) nums = [-2, 8, -1, 6, -7]
   Correct output = 13
"""


from typing import List


class Solution:
    def _kadane(self, nums: List[int]) -> int:
        """
        Compute the maximum subarray sum in the original array using Kadane's algorithm.

        Args:
            nums: The input integer array.

        Returns:
            The maximum sum of any contiguous subarray.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # Standard Kadane's algorithm:
        # - current_best_ending_here stores the best subarray sum that must end at the current index
        # - global_best stores the best subarray sum seen anywhere so far
        current_best_ending_here = nums[0]
        global_best = nums[0]

        for i in range(1, len(nums)):
            # At each position, we decide whether it is better to:
            # 1) start a new subarray at nums[i]
            # 2) extend the previous subarray by including nums[i]
            current_best_ending_here = max(nums[i], current_best_ending_here + nums[i])

            # Update the global answer if the current ending subarray is better.
            global_best = max(global_best, current_best_ending_here)

        return global_best

    def maxSubarraySumAfterReverse(self, nums: List[int]) -> int:
        """
        Compute the maximum possible contiguous subarray sum after reversing at most
        one contiguous subarray.

        Key idea:
        A chosen final maximum subarray in the modified array can be viewed as:
        - a left part that originally ended before the reversed segment,
        - plus a middle part that comes from some original interval [l..r] but is reversed,
        - plus a right part that originally started after the reversed segment.

        Reversing does not change the total sum of the middle interval [l..r], only the
        order of its elements. Therefore, if the final chosen subarray includes the whole
        reversed interval, its contribution is simply sum(l..r).

        So for every pair (l, r), the best achievable subarray that uses the reversed
        interval [l..r] as its middle is:
            best_suffix_ending_at_l_minus_1 + sum(l..r) + best_prefix_starting_at_r_plus_1
        where the left and right extensions are optional and only used if positive.

        This formula is enough to cover all optimal cases:
        - no reversal
        - reversal fully inside the chosen subarray
        - reversal touching one side
        - reversal exactly equal to the chosen subarray

        Args:
            nums: The input integer array.

        Returns:
            The maximum achievable contiguous subarray sum after at most one reversal.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n)
        """
        n = len(nums)

        # Edge case:
        # If there is only one element, reversing any subarray changes nothing.
        if n == 1:
            return nums[0]

        # ---------------------------------------------------------------------
        # Step 1: Compute the answer with no reversal at all.
        # ---------------------------------------------------------------------
        # This is important because the problem says "at most one reversal",
        # which means we are allowed to skip the operation entirely.
        answer = self._kadane(nums)

        # ---------------------------------------------------------------------
        # Step 2: Precompute best possible left extension for every position.
        # ---------------------------------------------------------------------
        # left_end[i] = maximum sum of a subarray that MUST end exactly at index i.
        #
        # Why do we need this?
        # Suppose we choose to reverse some interval [l..r], and later consider a
        # final subarray that includes that whole reversed interval.
        #
        # The part immediately to the left of that interval, if included, must be a
        # subarray ending at l-1. Among all such choices, we want the best one.
        #
        # However, if that best sum is negative, we should not include it at all.
        # So later we will use max(0, left_end[l-1]).
        left_end = [0] * n
        left_end[0] = nums[0]
        for i in range(1, n):
            # Either start fresh at i, or extend the best subarray ending at i-1.
            left_end[i] = max(nums[i], left_end[i - 1] + nums[i])

        # ---------------------------------------------------------------------
        # Step 3: Precompute best possible right extension for every position.
        # ---------------------------------------------------------------------
        # right_start[i] = maximum sum of a subarray that MUST start exactly at index i.
        #
        # This is symmetric to left_end.
        # If our reversed interval is [l..r], then any optional right extension must
        # start at r+1, and we want the best such subarray. Again, if it is negative,
        # we simply do not include it.
        right_start = [0] * n
        right_start[n - 1] = nums[n - 1]
        for i in range(n - 2, -1, -1):
            # Either start fresh at i, or continue into the best subarray starting at i+1.
            right_start[i] = max(nums[i], nums[i] + right_start[i + 1])

        # ---------------------------------------------------------------------
        # Step 4: Prefix sums for O(1) interval sum queries.
        # ---------------------------------------------------------------------
        # prefix[k] will store the sum of nums[0:k].
        # Then sum of nums[l:r+1] is:
        #     prefix[r+1] - prefix[l]
        #
        # We need this because we will iterate over all O(n^2) intervals [l..r],
        # and we want each interval sum in O(1), not O(length).
        prefix = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + nums[i]

        # ---------------------------------------------------------------------
        # Step 5: Try every possible reversed interval [l..r].
        # ---------------------------------------------------------------------
        # For a fixed interval [l..r]:
        #
        # - The reversed middle contributes exactly sum(nums[l..r]).
        #   Reversal changes order, but not total sum.
        #
        # - The best optional left extension is the best subarray ending at l-1,
        #   but only if it is positive.
        #
        # - The best optional right extension is the best subarray starting at r+1,
        #   but only if it is positive.
        #
        # Therefore:
        #   candidate = max(0, left_end[l-1]) + sum(l..r) + max(0, right_start[r+1])
        #
        # This candidate corresponds to a valid subarray in the array after reversing [l..r].
        #
        # Why this works:
        # After reversal, the interval [l..r] becomes a contiguous block containing the
        # same values in reverse order. If our chosen final subarray includes that whole
        # block, then its total sum is unaffected by the internal order of that block.
        # The only thing that matters is how strongly we can extend from the left and right.
        #
        # Since every optimal solution can be represented this way, checking all [l..r]
        # gives the correct answer.
        for l in range(n):
            # Compute the best left contribution once per l.
            if l > 0:
                left_gain = max(0, left_end[l - 1])
            else:
                left_gain = 0

            for r in range(l, n):
                # Sum of the middle interval [l..r].
                middle_sum = prefix[r + 1] - prefix[l]

                # Best right contribution depends on r.
                if r + 1 < n:
                    right_gain = max(0, right_start[r + 1])
                else:
                    right_gain = 0

                candidate = left_gain + middle_sum + right_gain
                if candidate > answer:
                    answer = candidate

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, -10, 3, 5],
        [-2, 8, -1, 6, -7],
        [1],
        [-5, -2, -7],
        [2, -1, 2, -1, 2],
    ]

    for nums in sample_inputs:
        result = solution.maxSubarraySumAfterReverse(nums)
        print(f"nums = {nums}")
        print(f"maximum achievable contiguous profit = {result}")
        print()