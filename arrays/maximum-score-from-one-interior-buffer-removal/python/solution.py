"""
Title: Maximum Score from One Interior Buffer Removal

Problem Description:
You are given an integer array nums representing the value of blocks in a processing
pipeline. You may remove exactly one contiguous subarray that is strictly inside the
array, meaning the removed segment cannot include the first or the last element.
After the removal, the remaining left part and right part are concatenated. The score
of the final array is the sum of its elements.

Your task is to return the maximum possible score after removing one valid interior
subarray. Since removing a subarray decreases the total sum, the goal is equivalent
to removing an interior subarray with the minimum possible sum.

A valid removed subarray must satisfy:
    1 <= l <= r <= n - 2
using 0-based indexing, where nums[l..r] is removed and both nums[0] and nums[n-1]
remain in the final array.

Constraints:
- 3 <= nums.length <= 200000
- -1000000000 <= nums[i] <= 1000000000
- The answer fits in a signed 64-bit integer

Examples:
1) nums = [5, -2, 3, -4, 6]
   Total sum = 8
   Minimum-sum interior subarray is [-4] with sum -4
   Best final score = 8 - (-4) = 12

2) nums = [4, 7, 2, 9]
   Total sum = 22
   Valid interior subarrays are [7], [2], and [7, 2]
   Minimum-sum interior subarray is [2] with sum 2
   Best final score = 22 - 2 = 20
"""

from typing import List


class Solution:
    def minimum_interior_subarray_sum(self, nums: List[int]) -> int:
        """
        Find the minimum possible sum of a contiguous subarray restricted to the
        interior indices [1 .. n - 2].

        This is a constrained version of the classic minimum subarray sum problem.
        We solve it in linear time using a Kadane-style dynamic programming scan.

        Args:
            nums: The input integer array.

        Returns:
            The minimum sum among all valid interior contiguous subarrays.

        Time complexity:
            O(n), because we scan the interior portion exactly once.

        Space complexity:
            O(1), because we use only a constant amount of extra memory.
        """
        # The removable subarray must be strictly inside the array.
        # Therefore, only indices 1 through len(nums) - 2 are allowed.
        #
        # We want the minimum-sum contiguous subarray over that range.
        #
        # Standard idea:
        # Let current_min_ending_here be the minimum sum of a valid subarray
        # that MUST end at the current index.
        #
        # Transition:
        # For each value x, the best minimum-sum subarray ending here is either:
        #   1) start fresh at x
        #   2) extend the previous minimum-sum subarray by adding x
        #
        # So:
        #   current_min_ending_here = min(x, current_min_ending_here + x)
        #
        # We also track the global minimum over all endings.

        # Initialize using the first valid interior element.
        current_min_ending_here: int = nums[1]
        best_min_sum: int = nums[1]

        # Process the remaining interior elements.
        for i in range(2, len(nums) - 1):
            value = nums[i]

            # Decide whether it is better to:
            # - start a new subarray at this index
            # - or extend the previous one
            current_min_ending_here = min(value, current_min_ending_here + value)

            # Update the best (smallest) subarray sum seen so far.
            best_min_sum = min(best_min_sum, current_min_ending_here)

        return best_min_sum

    def maximum_score(self, nums: List[int]) -> int:
        """
        Compute the maximum possible score after removing exactly one contiguous
        interior subarray.

        Since the final score equals:
            total_sum - removed_subarray_sum
        maximizing the final score is equivalent to minimizing the removed
        interior subarray sum.

        Args:
            nums: The input integer array.

        Returns:
            The maximum score obtainable after one valid interior removal.

        Time complexity:
            O(n), because we compute the total sum and scan once for the minimum
            interior subarray sum.

        Space complexity:
            O(1), excluding the input array.
        """
        # Compute the sum of the full array first.
        # After removing one subarray, the remaining score is:
        # total_sum - removed_sum
        total_sum: int = sum(nums)

        # Find the minimum-sum removable subarray that lies strictly inside.
        min_removed_sum: int = self.minimum_interior_subarray_sum(nums)

        # If the removed sum is very negative, subtracting it increases the score.
        # If the removed sum is positive, we still must remove exactly one subarray,
        # so we remove the smallest positive one available.
        return total_sum - min_removed_sum


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [5, -2, 3, -4, 6],
        [4, 7, 2, 9],
    ]

    for nums in sample_inputs:
        result = solution.maximum_score(nums)
        print(f"nums = {nums}")
        print(f"maximum score = {result}")
        print()