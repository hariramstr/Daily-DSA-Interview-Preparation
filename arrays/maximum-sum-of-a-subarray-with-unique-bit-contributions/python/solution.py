"""
Title: Maximum Sum of a Subarray With Unique Bit Contributions

Problem Description:
You are given an integer array nums of length n, where each value represents a feature mask
stored as a non-negative integer. A contiguous subarray is called clean if no bit position is
set in more than one element inside that subarray. In other words, for every pair of different
indices i and j in the chosen subarray, nums[i] & nums[j] must be 0. Your task is to return
the maximum possible sum of values over all clean contiguous subarrays.

This is not the same as requiring all numbers to be distinct. Two equal numbers may both appear
only if their bitwise AND is 0, which is impossible unless both are 0. Likewise, different
numbers may still conflict if they share any set bit. You must find the best contiguous range
under this bit-overlap rule.

Return the maximum sum of any clean subarray.

Constraints:
- 1 <= nums.length <= 2 * 10^5
- 0 <= nums[i] <= 10^9
- The answer fits in a 64-bit signed integer.

Example 1:
Input: nums = [5,2,8,1]
Output: 11
Explanation: Binary forms are 0101, 0010, 1000, 0001. The subarray [2,8,1] is clean because
no bit is shared between any two elements, and its sum is 11. The full array is not clean
because 5 and 1 both use the lowest bit.

Example 2:
Input: nums = [3,8,48,10]
Output: 59
Explanation: The entire array is not clean because 8 and 10 share bit 3. The best clean
subarray is [3,8,48] with sum 59. Here 3 uses bits 0 and 1, 8 uses bit 3, and 48 uses bits
4 and 5, so there is no overlap.

Goal:
Design an efficient algorithm using a moving window / sliding window approach that exploits
the bitwise structure of the numbers.
"""

from typing import List


class Solution:
    def maximumSubarraySum(self, nums: List[int]) -> int:
        """
        Return the maximum sum of any contiguous subarray where no bit position
        appears in more than one element.

        Args:
            nums: List of non-negative integers.

        Returns:
            The maximum possible sum among all clean contiguous subarrays.

        Time complexity:
            O(n), where n is the length of nums.
            Each element enters the sliding window once and leaves it at most once.

        Space complexity:
            O(1), because we only store a few integers regardless of input size.
            (Bit width is bounded by the integer size constraints.)
        """
        # We use the classic sliding window pattern:
        #
        # - "left" and "right" define the current window nums[left:right+1].
        # - The window is always maintained so that it is "clean":
        #   no two numbers inside it share a set bit.
        #
        # Key observation:
        # If the current window is already clean, then the bitwise OR of all values
        # in the window is enough to represent which bit positions are currently used.
        #
        # Why OR works here:
        # Because in a clean window, each bit can appear at most once across all numbers.
        # So if a bit is present in the OR mask, it belongs to exactly one element.
        #
        # Then, when we want to add nums[right]:
        # - If (used_bits & nums[right]) == 0, there is no conflict, so we can extend.
        # - Otherwise, some bit overlaps with existing elements in the window.
        #   We repeatedly remove elements from the left until the overlap disappears.
        #
        # Since all nums[i] are non-negative, enlarging a valid window by adding a new
        # non-conflicting number only increases or preserves the sum. Therefore, for each
        # right endpoint, the best valid window ending at that endpoint is the largest one
        # we can maintain after resolving conflicts.
        #
        # This makes sliding window both correct and efficient.

        left: int = 0

        # "used_bits" stores the OR of all numbers currently in the window.
        # Because the window is always clean, this also acts like a set of occupied bits.
        used_bits: int = 0

        # "current_sum" stores the sum of the current window.
        current_sum: int = 0

        # "best_sum" tracks the maximum clean-window sum seen so far.
        best_sum: int = 0

        # Expand the window one element at a time using "right".
        for right, value in enumerate(nums):
            # If the new value shares any bit with the current window,
            # the window would become invalid after adding it.
            #
            # We must shrink from the left until there is no overlap.
            #
            # Condition explanation:
            # used_bits & value != 0
            # means at least one bit is set both in the window and in "value".
            while used_bits & value:
                # Remove nums[left] from the running sum because it is leaving the window.
                current_sum -= nums[left]

                # Remove nums[left]'s bits from "used_bits".
                #
                # We use XOR here safely because the window is clean:
                # every set bit in nums[left] appears in exactly one element of the window.
                # Therefore toggling those bits off is correct.
                #
                # Example:
                # used_bits = 0b110101
                # nums[left] = 0b000100
                # used_bits ^= nums[left] => 0b110001
                used_bits ^= nums[left]

                # Move the left boundary rightward.
                left += 1

            # At this point, there is no overlap between "value" and the current window.
            # So we can safely add it.
            used_bits |= value
            current_sum += value

            # Update the best answer with the sum of this valid window.
            if current_sum > best_sum:
                best_sum = current_sum

        return best_sum

    def _run_examples(self) -> None:
        """
        Run the examples from the problem statement and print the results.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(m), where m is the total number of elements across the sample arrays.

        Space complexity:
            O(1), excluding input storage.
        """
        examples: List[List[int]] = [
            [5, 2, 8, 1],
            [3, 8, 48, 10],
        ]

        for nums in examples:
            result = self.maximumSubarraySum(nums)
            print(f"nums = {nums}")
            print(f"maximum clean subarray sum = {result}")
            print()


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the statement:
    # [5,2,8,1]
    # Best clean subarray is [2,8,1] with sum 11.
    nums1: List[int] = [5, 2, 8, 1]
    print("Sample 1:")
    print(solution.maximumSubarraySum(nums1))  # Expected: 11
    print()

    # Sample input 2 from the statement:
    # [3,8,48,10]
    # Best clean subarray is [3,8,48] with sum 59.
    nums2: List[int] = [3, 8, 48, 10]
    print("Sample 2:")
    print(solution.maximumSubarraySum(nums2))  # Expected: 59
    print()

    # Additional grouped example runner.
    print("Running bundled examples:")
    solution._run_examples()