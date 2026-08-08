"""
Title: Count Subarrays With the Same First and Last Value

Problem Description:
You are given an integer array nums representing a stream of event codes.
A contiguous subarray is called closed if its first element is equal to its
last element. Your task is to return the total number of closed subarrays in nums.

Formally, count the number of pairs (l, r) such that:
    0 <= l <= r < n
and:
    nums[l] == nums[r]

Every single-element subarray is considered closed because its first and last
elements are the same.

A brute-force solution that checks every subarray would be too slow for large
inputs. The intended solution should use hashing to track how many times each
value has appeared so far while scanning the array from left to right.

For each index r, the number of closed subarrays ending at r is exactly the
number of earlier indices l with nums[l] == nums[r], plus the length-1 subarray
[r, r]. This allows the answer to be computed in linear time.

Constraints:
- 1 <= nums.length <= 200000
- -10^9 <= nums[i] <= 10^9
- The answer may be larger than 32-bit integer range, so use a 64-bit integer type.
"""

from typing import Dict, List


class Solution:
    def count_closed_subarrays(self, nums: List[int]) -> int:
        """
        Count the number of contiguous subarrays whose first and last values are equal.

        Args:
            nums: List of integers representing the array.

        Returns:
            The total number of closed subarrays.

        Time complexity:
            O(n), where n is the length of nums, because we scan the array once
            and each hash map operation is O(1) on average.

        Space complexity:
            O(k), where k is the number of distinct values in nums, due to the
            frequency hash map.
        """
        # This dictionary will store how many times each value has appeared
        # so far while we scan from left to right.
        #
        # Example:
        # If we have processed [4, 1, 4], then:
        # seen_count = {4: 2, 1: 1}
        #
        # Why do we need this?
        # Because when we are at position r, every earlier position l with:
        #     nums[l] == nums[r]
        # creates one valid closed subarray nums[l:r+1].
        #
        # So if the current value has already appeared X times, then there are
        # exactly X closed subarrays ending at the current index that start earlier,
        # plus 1 more for the single-element subarray [r, r].
        seen_count: Dict[int, int] = {}

        # This variable accumulates the final answer.
        # Python integers automatically grow as needed, so they safely handle
        # values larger than 32-bit range.
        total_closed_subarrays: int = 0

        # Process each number from left to right.
        for value in nums:
            # Look up how many times this value has already appeared.
            #
            # If it has appeared 'previous_occurrences' times, then:
            # - each previous occurrence can serve as a starting index l
            # - the current position is the ending index r
            # - therefore, we get 'previous_occurrences' new closed subarrays
            #   ending here with length >= 2
            previous_occurrences: int = seen_count.get(value, 0)

            # Add:
            #   previous_occurrences  -> subarrays starting at earlier equal values
            #   1                     -> the single-element subarray [value]
            #
            # So the number of new valid subarrays ending at this position is:
            #   previous_occurrences + 1
            total_closed_subarrays += previous_occurrences + 1

            # Now update the frequency map to include the current value.
            #
            # This must happen AFTER using previous_occurrences, because we only want
            # earlier positions to count as possible starts for subarrays ending here.
            seen_count[value] = previous_occurrences + 1

        return total_closed_subarrays

    def sameFirstAndLast(self, nums: List[int]) -> int:
        """
        Compatibility wrapper that returns the number of closed subarrays.

        Args:
            nums: List of integers representing the array.

        Returns:
            The total number of closed subarrays.

        Time complexity:
            O(n)

        Space complexity:
            O(k)
        """
        return self.count_closed_subarrays(nums)


if __name__ == "__main__":
    # Create a Solution instance.
    solution = Solution()

    # Sample input 1 from the problem statement.
    nums1: List[int] = [4, 1, 4, 4]
    result1: int = solution.count_closed_subarrays(nums1)
    print("Input:", nums1)
    print("Output:", result1)
    # Expected: 6
    #
    # Manual verification:
    # Index pairs (l, r) with nums[l] == nums[r]:
    # (0,0) -> [4]
    # (1,1) -> [1]
    # (2,2) -> [4]
    # (3,3) -> [4]
    # (0,2) -> [4,1,4]
    # (2,3) -> [4,4]
    # Total = 6

    print()

    # Sample input 2 from the problem statement.
    nums2: List[int] = [2, 2, 2]
    result2: int = solution.count_closed_subarrays(nums2)
    print("Input:", nums2)
    print("Output:", result2)
    # Expected: 6
    #
    # Manual verification:
    # Length 1: [2], [2], [2] -> 3
    # Length 2: [2,2], [2,2] -> 2
    # Length 3: [2,2,2] -> 1
    # Total = 6

    print()

    # Additional small test to help beginners see another case.
    nums3: List[int] = [1, 2, 3]
    result3: int = solution.count_closed_subarrays(nums3)
    print("Input:", nums3)
    print("Output:", result3)
    # Expected: 3
    # Only the three single-element subarrays are valid.