"""
Title: Find the Longest Equal Neighbor Run

Problem Description:
You are given an integer array nums representing a sequence of sensor states recorded
over time. A stable run is a contiguous part of the array where every pair of
neighboring elements is equal. In other words, for every valid index i inside that
run except the last one, nums[i] == nums[i + 1].

Your task is to return the length of the longest stable run in the array.

A run may contain just one element, so if all values are different, the answer is 1.
If the array is empty, return 0.

This problem is useful in systems that need to measure how long a device stayed in
the same state without changing. You only need to scan the array once and track the
current run length and the best run length seen so far.

Constraints:
- 0 <= nums.length <= 100000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [4,4,4,2,2,7,7,7,7,1]
Output: 4
Explanation: The longest contiguous block of equal values is [7,7,7,7], which has
length 4.

Example 2:
Input: nums = [5,3,8,1]
Output: 1
Explanation: No two neighboring elements are equal, so every stable run has length 1.

Return the maximum length among all contiguous runs of equal values.
"""

from typing import List


class Solution:
    def longest_equal_neighbor_run(self, nums: List[int]) -> int:
        """
        Find the length of the longest contiguous run of equal neighboring values.

        Args:
            nums: A list of integers representing the recorded sensor states.

        Returns:
            The maximum length among all contiguous runs of equal values.
            Returns 0 if the input list is empty.

        Time complexity:
            O(n), where n is the length of nums, because we scan the array once.

        Space complexity:
            O(1), because we use only a few extra variables regardless of input size.
        """
        # First, handle the special case where the input list is empty.
        # Why this is needed:
        # - If there are no elements, there are no runs at all.
        # - The problem explicitly says to return 0 for an empty array.
        if not nums:
            return 0

        # We start with both the current run length and the best run length as 1.
        # Why 1?
        # - A single element by itself is considered a valid run.
        # - Since the list is not empty at this point, we know there is at least one element.
        current_run_length: int = 1
        longest_run_length: int = 1

        # We iterate from the second element to the end of the list.
        # Why start at index 1?
        # - We compare each element with the one immediately before it.
        # - Index 0 has no previous neighbor, so comparisons begin at index 1.
        for i in range(1, len(nums)):
            # Check whether the current element continues the stable run.
            # A stable run continues only if the current value is equal to the previous value.
            if nums[i] == nums[i - 1]:
                # Since the current value matches the previous one, the run grows by 1.
                current_run_length += 1
            else:
                # If the values are different, the previous run has ended.
                # We now start a brand-new run beginning at the current element.
                # That new run has length 1 because it currently contains only nums[i].
                current_run_length = 1

            # After updating the current run, compare it with the best run seen so far.
            # If the current run is longer, update the answer.
            if current_run_length > longest_run_length:
                longest_run_length = current_run_length

        # At the end of the scan, longest_run_length stores the maximum run length found.
        return longest_run_length

    def longestStableRun(self, nums: List[int]) -> int:
        """
        Provide an alternative method name that solves the same problem.

        Args:
            nums: A list of integers representing the recorded sensor states.

        Returns:
            The length of the longest contiguous run of equal values.

        Time complexity:
            O(n), where n is the length of nums.

        Space complexity:
            O(1).
        """
        # This wrapper method simply calls the main implementation.
        # It can be useful if a platform expects a different method name.
        return self.longest_equal_neighbor_run(nums)


if __name__ == "__main__":
    # Create an instance of the Solution class so we can call its methods.
    solution = Solution()

    # Sample input 1 from the problem statement.
    nums1: List[int] = [4, 4, 4, 2, 2, 7, 7, 7, 7, 1]
    result1: int = solution.longest_equal_neighbor_run(nums1)
    print(f"Input: {nums1}")
    print(f"Longest equal neighbor run length: {result1}")
    print("Expected: 4")
    print()

    # Sample input 2 from the problem statement.
    nums2: List[int] = [5, 3, 8, 1]
    result2: int = solution.longest_equal_neighbor_run(nums2)
    print(f"Input: {nums2}")
    print(f"Longest equal neighbor run length: {result2}")
    print("Expected: 1")
    print()

    # Additional sample: empty list should return 0.
    nums3: List[int] = []
    result3: int = solution.longest_equal_neighbor_run(nums3)
    print(f"Input: {nums3}")
    print(f"Longest equal neighbor run length: {result3}")
    print("Expected: 0")
    print()

    # Additional sample: all values equal.
    nums4: List[int] = [9, 9, 9, 9]
    result4: int = solution.longest_equal_neighbor_run(nums4)
    print(f"Input: {nums4}")
    print(f"Longest equal neighbor run length: {result4}")
    print("Expected: 4")