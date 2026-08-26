"""
Title: Validate a Single Enabled Debug Option

Problem Description:
A monitoring tool stores debug settings for a service in one non-negative integer
called `mask`. Each bit in `mask` represents whether a specific debug option is
enabled (`1`) or disabled (`0`). For safety reasons, the service is considered
valid only when exactly one debug option is enabled at a time.

Given an integer `mask`, return `true` if it contains exactly one set bit in its
binary representation. Otherwise, return `false`.

This is a bit manipulation problem. A direct loop over all bits works, but there
is also a simple constant-time trick using bitwise operators. Your solution should
correctly handle `0`, since a value of `0` means no options are enabled and
therefore is not valid.

Constraints:
- 0 <= mask <= 2^31 - 1
- The expected solution should use O(1) extra space.
- Any solution running in O(number of bits) or better is acceptable.

Example 1:
Input: mask = 8
Output: true
Explanation: 8 in binary is 1000, which has exactly one set bit.

Example 2:
Input: mask = 10
Output: false
Explanation: 10 in binary is 1010, which has two set bits, so more than one
debug option is enabled.

Task:
Implement a function that returns whether the given debug mask is valid under
this rule.
"""


class Solution:
    def has_single_enabled_option(self, mask: int) -> bool:
        """
        Determine whether the given non-negative integer contains exactly one set bit.

        Args:
            mask: A non-negative integer representing enabled/disabled debug options.

        Returns:
            True if exactly one bit is set to 1 in the binary representation of mask;
            otherwise, False.

        Time Complexity:
            O(1)

        Space Complexity:
            O(1)
        """
        # First, we must reject 0 immediately.
        #
        # Why?
        # - A value of 0 means every bit is 0.
        # - That means no debug option is enabled.
        # - The problem requires exactly one enabled option, not zero.
        #
        # So:
        # - mask = 0  -> False
        if mask == 0:
            return False

        # We use a classic bit manipulation trick:
        #
        # For any positive integer that has exactly one set bit,
        # subtracting 1 flips:
        # - that single 1 bit to 0
        # - all bits to its right to 1
        #
        # Example:
        #   mask = 8       -> binary 1000
        #   mask - 1 = 7   -> binary 0111
        #   1000 & 0111 = 0000
        #
        # This becomes zero only when there was exactly one set bit.
        #
        # If there are multiple set bits, the result will not be zero.
        #
        # Example:
        #   mask = 10      -> binary 1010
        #   mask - 1 = 9   -> binary 1001
        #   1010 & 1001 = 1000  -> not zero
        #
        # Therefore:
        # - mask > 0 and (mask & (mask - 1)) == 0  => exactly one set bit
        return (mask & (mask - 1)) == 0


if __name__ == "__main__":
    # Create an instance of the solution class so we can call the method.
    solution = Solution()

    # Sample inputs from the problem statement and a few extra beginner-friendly checks.
    sample_masks = [
        8,   # binary 1000 -> exactly one set bit -> True
        10,  # binary 1010 -> two set bits -> False
        0,   # binary 0000 -> no set bits -> False
        1,   # binary 0001 -> exactly one set bit -> True
        16,  # binary 10000 -> exactly one set bit -> True
        18,  # binary 10010 -> two set bits -> False
    ]

    # Go through each sample input, call the algorithm, and print the result.
    for mask in sample_masks:
        result = solution.has_single_enabled_option(mask)
        print(f"mask = {mask}, valid = {result}")