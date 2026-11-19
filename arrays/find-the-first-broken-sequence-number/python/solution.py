"""
Title: Find the First Broken Sequence Number

Problem Description:
You are given an array `nums` representing event sequence numbers recorded by a service
during one batch. The sequence numbers are supposed to start at `1` and increase by
exactly `1` each time with no gaps. However, due to a logging issue, one positive
sequence number in that expected range is missing from the array. Your task is to
return the smallest missing positive sequence number.

The array may be unsorted, and values that are `0`, negative, or larger than the array
length may appear because of corrupted records. Those values should be ignored when
determining the answer. For example, if the batch has length `n`, then the missing
answer must be in the range `[1, n + 1]`.

Write a function that returns the first positive integer that does not appear in `nums`.

Constraints:
- 1 <= nums.length <= 100000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [3, 4, -1, 1]
Output: 2
Explanation: The positive numbers 1 and 3 and 4 appear, but 2 is the smallest missing
positive number.

Example 2:
Input: nums = [1, 2, 0]
Output: 3
Explanation: The sequence contains 1 and 2, so the first missing positive number is 3.
"""

from typing import List, Set


class Solution:
    def first_missing_positive(self, nums: List[int]) -> int:
        """
        Return the smallest positive integer that does not appear in the input list.

        This beginner-friendly solution uses a set to store all positive numbers that
        could matter, then checks from 1 upward to find the first missing value.

        Args:
            nums: A list of integers that may contain positive, negative, zero,
                duplicates, and out-of-range values.

        Returns:
            The smallest positive integer missing from the list.

        Time complexity:
            O(n), where n is the length of nums, because we scan the list once to build
            the set and then scan at most n + 1 candidate values.

        Space complexity:
            O(n), because the set may store up to n relevant positive values.
        """
        # The length of the array is important because of a key observation:
        #
        # For an array of length n, the first missing positive number must be in the
        # range [1, n + 1].
        #
        # Why is that true?
        # - If 1 is missing, the answer is 1.
        # - If 1 is present but 2 is missing, the answer is 2.
        # - ...
        # - If every number from 1 through n is present, then the answer must be n + 1.
        #
        # This means values less than 1 and values greater than n do not affect the
        # final answer for "smallest missing positive" in the range we care about.
        n: int = len(nums)

        # We use a set because:
        # - Membership checks like "is x present?" are very fast on average: O(1).
        # - Duplicates are automatically ignored, which is helpful because repeated
        #   values do not change which positive number is missing.
        present: Set[int] = set()

        # Step 1: Collect only the useful values.
        #
        # We only keep numbers in the range [1, n].
        # Why not include n + 1 here?
        # - We do not need to store it to solve the problem.
        # - If all numbers 1..n are present, we can directly return n + 1.
        #
        # We ignore:
        # - Negative numbers: they are not positive, so they cannot be the answer.
        # - Zero: also not positive.
        # - Numbers greater than n: they cannot be the first missing positive when the
        #   array length is n, based on the observation above.
        for value in nums:
            if 1 <= value <= n:
                present.add(value)

        # Step 2: Check each candidate answer in increasing order.
        #
        # We start from 1 because the problem asks for the smallest missing positive.
        # The first number in [1, n] that is not in the set is our answer.
        for candidate in range(1, n + 1):
            if candidate not in present:
                return candidate

        # Step 3: If every number from 1 to n exists, then the smallest missing
        # positive must be n + 1.
        return n + 1


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [3, 4, -1, 1],
        [1, 2, 0],
        [1],
        [2],
        [1, 2, 3],
        [7, 8, 9, 11, 12],
    ]

    for nums in sample_inputs:
        result = solution.first_missing_positive(nums)
        print(f"nums = {nums} -> first missing positive = {result}")