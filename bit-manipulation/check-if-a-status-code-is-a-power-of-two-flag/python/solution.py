"""
Title: Check if a Status Code Is a Power-of-Two Flag

Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
In a monitoring system, each valid standalone status flag is encoded as a positive integer
with exactly one bit set in its binary representation. For example, 1 (binary 1),
2 (binary 10), 4 (binary 100), and 8 (binary 1000) are valid standalone flags.
A number like 10 (binary 1010) is not, because it has more than one set bit.
Given an integer code, determine whether it represents a valid standalone status flag.

Return true if the code is a positive power of two, and false otherwise.

A simple and efficient bit manipulation solution is expected. Try to solve it in O(1)
time using bitwise operators rather than loops over all bits.

Constraints:
- -2^31 <= code <= 2^31 - 1
- The input is a single integer.
- 0 and all negative numbers are not valid standalone flags.

Example 1:
Input: code = 16
Output: true
Explanation: 16 in binary is 10000, which contains exactly one set bit, so it is a valid standalone flag.

Example 2:
Input: code = 18
Output: false
Explanation: 18 in binary is 10010, which contains two set bits, so it is not a power of two.

Your task is only to decide whether the given code has exactly one set bit and is positive.
"""


class Solution:
    def is_power_of_two_flag(self, code: int) -> bool:
        """
        Determine whether the given integer is a positive power of two.

        A number is a power of two if:
        1. It is positive
        2. Its binary representation contains exactly one set bit

        Args:
            code: The integer status code to check.

        Returns:
            True if the code is a positive power of two, otherwise False.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        # First, reject all non-positive values.
        #
        # Why?
        # - The problem explicitly says that 0 and negative numbers are not valid flags.
        # - Powers of two in this context are only positive integers like 1, 2, 4, 8, ...
        #
        # Examples:
        # - code = 0   -> invalid
        # - code = -8  -> invalid
        if code <= 0:
            return False

        # Core bit manipulation idea:
        #
        # For any positive integer that is a power of two, its binary form has exactly one 1 bit.
        #
        # Examples:
        # 1  -> 0001
        # 2  -> 0010
        # 4  -> 0100
        # 8  -> 1000
        #
        # If we subtract 1 from such a number, that single 1 bit becomes 0,
        # and all bits to the right become 1.
        #
        # Example:
        # 8      = 1000
        # 8 - 1  = 0111
        #
        # Now observe:
        # 1000
        # 0111
        # ----
        # 0000
        #
        # So for powers of two:
        # code & (code - 1) == 0
        #
        # Why this works:
        # - A power of two has exactly one set bit.
        # - Subtracting 1 clears that bit and fills lower positions with 1s.
        # - Therefore, there is no position where both numbers have a 1.
        #
        # Why this fails for non-powers of two:
        # Example: 18 = 10010
        # 18 - 1 = 10001
        # AND    = 10000 != 0
        #
        # That non-zero result tells us there was more than one set bit.
        return (code & (code - 1)) == 0


if __name__ == "__main__":
    # Create an instance of the Solution class so we can call the method.
    solution = Solution()

    # Sample inputs based on the problem statement and a few extra edge cases.
    sample_codes: list[int] = [
        16,   # Expected: True  -> binary 10000, exactly one set bit
        18,   # Expected: False -> binary 10010, two set bits
        1,    # Expected: True  -> binary 1
        2,    # Expected: True  -> binary 10
        3,    # Expected: False -> binary 11
        0,    # Expected: False -> not positive
        -4,   # Expected: False -> negative
    ]

    # Trace through the examples from the prompt to verify correctness:
    #
    # Example 1:
    # code = 16
    # 16 > 0, so continue
    # 16 in binary:      10000
    # 16 - 1 = 15:       01111
    # 10000 & 01111 =    00000
    # Result: True
    #
    # Example 2:
    # code = 18
    # 18 > 0, so continue
    # 18 in binary:      10010
    # 18 - 1 = 17:       10001
    # 10010 & 10001 =    10000
    # Result: False
    #
    # These match the required outputs in the problem description.

    for code in sample_codes:
        result: bool = solution.is_power_of_two_flag(code)
        print(f"code = {code}, is_power_of_two_flag = {result}")