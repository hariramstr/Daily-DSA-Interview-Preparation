"""
Title: Minimum Bit Toggles to Make Adjacent IDs Disjoint

Problem Description:
You are given an array nums of length n, where each nums[i] is a non-negative integer
representing a device ID encoded as a bitmask. Two neighboring device IDs are considered
conflicting if they share at least one common set bit, meaning (nums[i] & nums[i+1]) != 0.

In one operation, you may toggle off exactly one set bit from any single element in the array.
In other words, if bit b is currently 1 in nums[i], you may change nums[i] to nums[i] ^ (1 << b).
You are not allowed to toggle a 0 bit on, and you may perform any number of operations.

Return the minimum number of bit-toggle operations required so that every adjacent pair becomes
disjoint, i.e. for every i from 0 to n - 2, (nums[i] & nums[i+1]) == 0.

Your goal is to minimize the total number of toggled bits across the entire array.

Constraints:
- 1 <= n <= 100000
- 0 <= nums[i] < 2^20
- The answer always fits in a 32-bit signed integer.

Key Insight:
Because bits never interact with different bit positions, we can solve each bit independently.

For one fixed bit:
- At each index i, either nums[i] has this bit set (1) or not (0).
- If it is 0, nothing can be done and it contributes no cost.
- If it is 1, we may either:
  - keep it as 1 with cost 0, or
  - toggle it off to 0 with cost 1.
- The final kept/removed choices must ensure no adjacent pair both keep this bit as 1.

So for each bit, we solve a tiny dynamic programming problem on a binary array:
choose a subset of positions containing that bit such that no two chosen positions are adjacent,
while maximizing how many are kept. Then:
minimum removals for this bit = total occurrences of the bit - maximum kept.

Summing over all 20 bits gives the global optimum.
"""

from typing import List


class Solution:
    def minBitToggles(self, nums: List[int]) -> int:
        """
        Compute the minimum number of bit-toggle-off operations needed so that
        every adjacent pair of numbers becomes bitwise disjoint.

        Args:
            nums: List of non-negative integers, each treated as a bitmask.

        Returns:
            The minimum number of single-bit toggle-off operations required.

        Time complexity:
            O(n * B), where B = 20 because nums[i] < 2^20.
            This is effectively O(n).

        Space complexity:
            O(1) extra space, ignoring the input array.
        """
        # There are at most 20 relevant bit positions because each number is < 2^20.
        max_bits: int = 20

        # This variable accumulates the answer across all bit positions.
        # We will solve the optimization independently for each bit and add the results.
        total_operations: int = 0

        # Process each bit independently.
        for bit in range(max_bits):
            # For this specific bit, we run a very small DP over the array.
            #
            # Meaning of the DP states after processing positions up to current index:
            #
            # dp0 = maximum number of kept 1s for this bit so far,
            #       with the condition that the current position ends as 0 for this bit.
            #
            # dp1 = maximum number of kept 1s for this bit so far,
            #       with the condition that the current position ends as 1 for this bit.
            #
            # Why maximize kept 1s?
            # Because every original 1 that is not kept must be toggled off.
            # So:
            #   removals = total_ones_for_this_bit - kept_ones
            #
            # Using "maximize kept" is often easier than directly minimizing removals.
            #
            # We use a large negative number to represent an impossible state.
            neg_inf: int = -10**18
            dp0: int = 0
            dp1: int = neg_inf

            # Count how many numbers originally contain this bit.
            # This lets us convert "maximum kept" into "minimum removed" at the end.
            total_ones_for_bit: int = 0

            # Scan the array from left to right.
            for value in nums:
                has_bit: int = (value >> bit) & 1

                if has_bit == 0:
                    # If the current number does NOT have this bit set originally:
                    #
                    # - It must remain 0 for this bit, because we are only allowed to turn bits off,
                    #   never on.
                    # - Therefore, the new state "current ends as 1" is impossible.
                    # - The new state "current ends as 0" can come from either previous state,
                    #   because ending current as 0 never creates an adjacency conflict.
                    new_dp0: int = max(dp0, dp1)
                    new_dp1: int = neg_inf
                else:
                    # The current number DOES have this bit set originally.
                    total_ones_for_bit += 1

                    # We have two choices:
                    #
                    # 1) Remove this bit from the current number:
                    #    - current ends as 0
                    #    - kept count does not increase
                    #    - can come from either previous dp0 or dp1
                    new_dp0 = max(dp0, dp1)

                    # 2) Keep this bit in the current number:
                    #    - current ends as 1
                    #    - kept count increases by 1
                    #    - previous position must end as 0, otherwise adjacent kept 1s
                    #      would conflict for this bit
                    new_dp1 = dp0 + 1

                # Move to the next position.
                dp0, dp1 = new_dp0, new_dp1

            # The best number of kept 1s for this bit is the better of the two ending states.
            max_kept_for_bit: int = max(dp0, dp1)

            # Every original 1 not kept must be toggled off exactly once.
            removals_for_bit: int = total_ones_for_bit - max_kept_for_bit

            # Add this bit's optimal cost into the global answer.
            total_operations += removals_for_bit

        return total_operations


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # nums = [3, 6, 5]
    # 3 = 011
    # 6 = 110
    # 5 = 101
    # Expected answer: 2
    nums1: List[int] = [3, 6, 5]
    result1: int = solution.minBitToggles(nums1)
    print(f"Input: {nums1}")
    print(f"Output: {result1}")
    print("Expected: 2")
    print()

    # Example 2:
    # nums = [7, 7]
    # 7 = 111, 7 = 111
    # For each of the 3 bits, one of the two numbers must lose that bit.
    # Expected answer: 3
    nums2: List[int] = [7, 7]
    result2: int = solution.minBitToggles(nums2)
    print(f"Input: {nums2}")
    print(f"Output: {result2}")
    print("Expected: 3")
    print()

    # Additional quick sanity checks
    nums3: List[int] = [0]
    result3: int = solution.minBitToggles(nums3)
    print(f"Input: {nums3}")
    print(f"Output: {result3}")
    print("Expected: 0")
    print()

    nums4: List[int] = [1, 2, 4, 8]
    result4: int = solution.minBitToggles(nums4)
    print(f"Input: {nums4}")
    print(f"Output: {result4}")
    print("Expected: 0")
    print()

    nums5: List[int] = [1, 1, 1]
    result5: int = solution.minBitToggles(nums5)
    print(f"Input: {nums5}")
    print(f"Output: {result5}")
    print("Expected: 1")