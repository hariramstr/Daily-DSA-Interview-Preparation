"""
Title: Maximum Recharge Gain from One Idle Block

Problem Description:
A mobile device records its battery change each minute during a long session.
You are given an integer array changes, where changes[i] is the net battery
change during minute i. A positive value means the battery increased during
that minute, and a negative value means the battery drained.

The device firmware allows exactly one optimization: you may choose one
contiguous block of minutes and mark it as an idle block. During the idle
block, every battery change in that block is inverted in sign. In other words,
each x in the chosen block becomes -x. You must choose exactly one non-empty
contiguous block.

Return the maximum possible total battery change after applying this
optimization once.

This is not the same as simply taking the maximum subarray sum. Flipping a
block changes the final total by subtracting twice the sum of that block, so
the best block may contain both positive and negative values. Your task is to
determine which single block to flip to maximize the final total battery
change.

Constraints:
- 1 <= changes.length <= 200000
- -100000 <= changes[i] <= 100000
- The answer fits in a 64-bit signed integer

Examples:
1) changes = [4, -7, 3, -2]
   Original total = -2
   If we flip a subarray with sum S, new total = original_total - 2 * S.
   To maximize the new total, we must minimize S.
   The minimum-sum subarray here is [-7], with sum -7.
   New total = -2 - 2 * (-7) = -2 + 14 = 12.

2) changes = [5, 2, 4]
   Original total = 11
   We must flip exactly one non-empty block.
   The minimum-sum subarray is [4], with sum 4.
   New total = 11 - 2 * 4 = 3.
"""

from typing import List


class Solution:
    def max_recharge_gain(self, changes: List[int]) -> int:
        """
        Compute the maximum possible total battery change after flipping
        exactly one non-empty contiguous block.

        The key observation:
        - Let total = sum(changes)
        - If we flip a block with sum block_sum, then every value x in that
          block becomes -x, so the block contribution changes from block_sum
          to -block_sum.
        - Therefore, the total changes by:
              new_total = total - block_sum + (-block_sum)
                        = total - 2 * block_sum
        - To maximize new_total, we must minimize block_sum.
        - So the problem becomes: find the minimum-sum non-empty subarray.

        Args:
            changes: List of integer battery changes per minute.

        Returns:
            The maximum possible total battery change after exactly one flip.

        Time complexity:
            O(n), where n is the length of changes.

        Space complexity:
            O(1), excluding input storage.
        """
        # First, compute the original total battery change.
        # We will later adjust this total based on the best block to flip.
        total_sum: int = sum(changes)

        # We now need the minimum-sum non-empty contiguous subarray.
        #
        # This is the "minimum subarray" version of Kadane's algorithm.
        #
        # Meaning of variables:
        # - current_min_ending_here:
        #     The minimum possible sum of a non-empty subarray that MUST end
        #     at the current index.
        #
        # - best_min_subarray_sum:
        #     The minimum subarray sum seen anywhere so far.
        #
        # We initialize both with the first element because:
        # - The chosen block must be non-empty.
        # - Starting from the first element avoids special handling for empty
        #   subarrays and keeps the logic simple and correct.
        current_min_ending_here: int = changes[0]
        best_min_subarray_sum: int = changes[0]

        # Process the array from left to right, starting at index 1 because
        # index 0 was already used for initialization.
        for value in changes[1:]:
            # For a minimum-sum subarray ending at this position, we have
            # exactly two choices:
            #
            # 1) Start a brand-new subarray at the current element:
            #       value
            #
            # 2) Extend the previous minimum-ending subarray:
            #       current_min_ending_here + value
            #
            # We choose the smaller one because we want the minimum sum.
            current_min_ending_here = min(value, current_min_ending_here + value)

            # Update the global best minimum subarray sum if the current
            # ending-here subarray is even smaller.
            best_min_subarray_sum = min(best_min_subarray_sum, current_min_ending_here)

        # Once we know the minimum-sum block, flipping it gives:
        #   answer = total_sum - 2 * best_min_subarray_sum
        #
        # Why this works:
        # - Original contribution of the chosen block = best_min_subarray_sum
        # - New contribution after flipping = -best_min_subarray_sum
        # - Net improvement = (-best_min_subarray_sum) - (best_min_subarray_sum)
        #                   = -2 * best_min_subarray_sum
        #
        # If best_min_subarray_sum is negative, this increases the total.
        # If all numbers are positive, best_min_subarray_sum will be the
        # smallest positive element, and we are forced to lose some total
        # because exactly one non-empty block must be flipped.
        return total_sum - 2 * best_min_subarray_sum

    def solve(self, changes: List[int]) -> int:
        """
        Wrapper method that calls the main algorithm.

        Args:
            changes: List of integer battery changes per minute.

        Returns:
            The maximum possible total battery change after one required flip.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.max_recharge_gain(changes)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # changes = [4, -7, 3, -2]
    # Original total = -2
    # Minimum-sum subarray = [-7], sum = -7
    # Result = -2 - 2 * (-7) = 12
    sample_1: List[int] = [4, -7, 3, -2]
    result_1: int = solution.solve(sample_1)
    print(f"Input: {sample_1}")
    print(f"Output: {result_1}")
    print("Expected: 12")
    print()

    # Example 2
    # changes = [5, 2, 4]
    # Original total = 11
    # Minimum-sum subarray = [4], sum = 4
    # Result = 11 - 2 * 4 = 3
    sample_2: List[int] = [5, 2, 4]
    result_2: int = solution.solve(sample_2)
    print(f"Input: {sample_2}")
    print(f"Output: {result_2}")
    print("Expected: 3")
    print()

    # Additional quick checks for beginner-friendly demonstration:

    # Single element negative:
    # Flip [-5] -> [5], total = 5
    sample_3: List[int] = [-5]
    result_3: int = solution.solve(sample_3)
    print(f"Input: {sample_3}")
    print(f"Output: {result_3}")
    print("Expected: 5")
    print()

    # Single element positive:
    # Must flip [8] -> [-8], total = -8
    sample_4: List[int] = [8]
    result_4: int = solution.solve(sample_4)
    print(f"Input: {sample_4}")
    print(f"Output: {result_4}")
    print("Expected: -8")
    print()

    # Mixed values:
    # total = 2 + (-1) + (-3) + 4 = 2
    # minimum subarray = [-1, -3], sum = -4
    # result = 2 - 2 * (-4) = 10
    sample_5: List[int] = [2, -1, -3, 4]
    result_5: int = solution.solve(sample_5)
    print(f"Input: {sample_5}")
    print(f"Output: {result_5}")
    print("Expected: 10")