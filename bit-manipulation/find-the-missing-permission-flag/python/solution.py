"""
Title: Find the Missing Permission Flag

Problem Description:
In a software system, each permission is represented by a power of two: 1, 2, 4, 8, 16, and so on.
A complete permission bundle should contain every flag from 2^0 up to 2^(n-1) exactly once.
However, due to a deployment issue, one permission flag is missing from the bundle.

You are given an integer array `flags` of length `n - 1`. The array contains distinct values,
and each value is a power of two. The full bundle was supposed to contain all powers of two
from `1` to `2^(n-1)`, but exactly one of them is absent. Your task is to return the missing
permission flag.

You should solve this using bit manipulation. A linear-time solution with constant extra space
is expected.

Constraints:
- 2 <= n <= 30
- flags.length == n - 1
- Each flags[i] is a power of two
- All values in flags are distinct
- Every flags[i] belongs to the set {1, 2, 4, ..., 2^(n-1)}
- Exactly one flag from the complete set is missing

Example 1:
Input: flags = [1, 2, 8, 16]
Output: 4
Explanation: The complete set for n = 5 is [1, 2, 4, 8, 16]. The missing flag is 4.

Example 2:
Input: flags = [2, 4, 8, 16, 32, 1]
Output: 64
Explanation: The complete set for n = 7 is [1, 2, 4, 8, 16, 32, 64]. Only 64 is missing.
"""

from typing import List


class Solution:
    def missing_permission_flag(self, flags: List[int]) -> int:
        """
        Find the single missing power-of-two permission flag using bit manipulation.

        The expected full set contains all powers of two from 2^0 up to 2^(n-1),
        where n = len(flags) + 1. We XOR all expected flags together and XOR all
        given flags together. Because XOR cancels equal values, the final result
        is the missing flag.

        Args:
            flags: A list of distinct power-of-two integers with exactly one flag missing
                   from the complete expected set.

        Returns:
            The missing permission flag as an integer.

        Time complexity:
            O(n), because we process the expected flags once and the input flags once.

        Space complexity:
            O(1), because we use only a few integer variables regardless of input size.
        """
        # The input length is n - 1, so the full bundle size n is one more than that.
        # Example:
        #   flags = [1, 2, 8, 16] has length 4
        #   therefore n = 5
        #   expected full set is [1, 2, 4, 8, 16]
        n: int = len(flags) + 1

        # This variable will store the XOR of all expected flags and all given flags.
        # Why XOR?
        # - a ^ a = 0   -> equal values cancel out
        # - a ^ 0 = a   -> XOR with zero keeps the value
        # - XOR is associative and commutative, so order does not matter
        #
        # If we XOR every expected flag and every provided flag together,
        # every flag that appears in both places disappears, leaving only the missing one.
        xor_result: int = 0

        # First, XOR all flags that SHOULD exist in the complete bundle.
        #
        # The complete bundle contains:
        #   2^0, 2^1, 2^2, ..., 2^(n-1)
        # which are:
        #   1, 2, 4, 8, ..., 2^(n-1)
        #
        # We generate each expected flag using a left shift:
        #   1 << exponent
        # This is a classic bit manipulation technique:
        #   1 << 0 = 1
        #   1 << 1 = 2
        #   1 << 2 = 4
        #   1 << 3 = 8
        # and so on.
        for exponent in range(n):
            expected_flag: int = 1 << exponent
            xor_result ^= expected_flag

        # Next, XOR all flags that are actually present in the input array.
        #
        # Any flag that exists in both:
        #   - the expected complete set
        #   - the given input
        # will cancel out.
        #
        # Since exactly one flag is missing, that one never gets canceled,
        # so it remains as the final answer.
        for flag in flags:
            xor_result ^= flag

        # At this point, xor_result is exactly the missing permission flag.
        return xor_result


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # Full set for n = 5 should be [1, 2, 4, 8, 16]
    # Given flags are [1, 2, 8, 16]
    # Missing flag should be 4
    flags1: List[int] = [1, 2, 8, 16]
    result1: int = solution.missing_permission_flag(flags1)
    print(f"Input: {flags1}")
    print(f"Missing permission flag: {result1}")
    print("Expected: 4")
    print()

    # Example 2:
    # Full set for n = 7 should be [1, 2, 4, 8, 16, 32, 64]
    # Given flags are [2, 4, 8, 16, 32, 1]
    # Missing flag should be 64
    flags2: List[int] = [2, 4, 8, 16, 32, 1]
    result2: int = solution.missing_permission_flag(flags2)
    print(f"Input: {flags2}")
    print(f"Missing permission flag: {result2}")
    print("Expected: 64")
    print()

    # Additional beginner-friendly sanity check:
    # Full set for n = 4 should be [1, 2, 4, 8]
    # Given flags are [1, 4, 8]
    # Missing flag should be 2
    flags3: List[int] = [1, 4, 8]
    result3: int = solution.missing_permission_flag(flags3)
    print(f"Input: {flags3}")
    print(f"Missing permission flag: {result3}")
    print("Expected: 2")