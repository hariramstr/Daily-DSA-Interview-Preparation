"""
Title: Count Devices with Exactly One Active Flag

Problem Description:
You are given an array `states` where each integer represents the status flags of a device.
In the binary representation of `states[i]`, each bit indicates whether a particular feature
is enabled (`1`) or disabled (`0`). A device is considered "simple-active" if it has exactly
one enabled feature, meaning its binary form contains exactly one set bit.

Your task is to return how many devices in the array are simple-active.

For example, the values `1` (`0001`), `2` (`0010`), `4` (`0100`), and `8` (`1000`) are
simple-active because each has exactly one bit set. The values `0`, `3`, `6`, and `10`
are not, because they have zero or more than one set bit.

This problem is expected to be solved efficiently using bit manipulation rather than
converting numbers to strings. A common observation is that a positive number has exactly
one set bit if and only if `x & (x - 1) == 0`.

Constraints:
- `1 <= states.length <= 100000`
- `0 <= states[i] <= 10^9`
- Return the total count of values that have exactly one set bit.
"""

from typing import List


class Solution:
    def has_exactly_one_set_bit(self, value: int) -> bool:
        """
        Check whether a non-negative integer has exactly one set bit in binary.

        Args:
            value: The integer to test.

        Returns:
            True if the integer has exactly one set bit, otherwise False.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        # A value must be positive to have exactly one set bit.
        # Why?
        # - 0 has no set bits at all, so it should return False.
        # - Any positive power of two has exactly one set bit.
        #   Examples:
        #   1  -> 0001
        #   2  -> 0010
        #   4  -> 0100
        #   8  -> 1000
        #
        # Key bit trick:
        # For a positive integer with exactly one set bit, subtracting 1 flips:
        # - that single 1 bit to 0
        # - all lower bits to 1
        #
        # Example:
        # 8      = 1000
        # 8 - 1  = 0111
        # ANDing them gives:
        # 1000 & 0111 = 0000
        #
        # But for a number with more than one set bit:
        # 10     = 1010
        # 10 - 1 = 1001
        # 1010 & 1001 = 1000 != 0
        #
        # Therefore:
        # value > 0 and (value & (value - 1)) == 0
        return value > 0 and (value & (value - 1)) == 0

    def countSimpleActive(self, states: List[int]) -> int:
        """
        Count how many integers in the list have exactly one set bit.

        Args:
            states: A list of non-negative integers representing device states.

        Returns:
            The number of values that are "simple-active".

        Time complexity:
            O(n), where n is the length of states.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We will keep a running total of how many valid devices we find.
        # This uses a single integer variable, so the extra space stays constant.
        count: int = 0

        # We scan through the list exactly once.
        # This is efficient and appropriate for up to 100000 elements.
        for value in states:
            # For each device state, we check whether its binary representation
            # contains exactly one set bit.
            #
            # We use a helper method to keep the main loop easy to read and
            # beginner-friendly. This also improves code organization.
            if self.has_exactly_one_set_bit(value):
                # If the current value is simple-active, increase the answer.
                count += 1

        # After processing every value, return the final total.
        return count


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    # Trace:
    # 1 -> binary 0001 -> exactly one set bit -> count
    # 2 -> binary 0010 -> exactly one set bit -> count
    # 3 -> binary 0011 -> two set bits -> do not count
    # 4 -> binary 0100 -> exactly one set bit -> count
    # 6 -> binary 0110 -> two set bits -> do not count
    # Final answer = 3
    states1: List[int] = [1, 2, 3, 4, 6]
    result1: int = solution.countSimpleActive(states1)
    print(f"Input: {states1}")
    print(f"Output: {result1}")
    print("Expected: 3")
    print()

    # Sample input 2 from the problem statement.
    # Trace:
    # 0  -> no set bits -> do not count
    # 7  -> binary 0111 -> three set bits -> do not count
    # 8  -> binary 1000 -> exactly one set bit -> count
    # 16 -> binary 10000 -> exactly one set bit -> count
    # 18 -> binary 10010 -> two set bits -> do not count
    # Final answer = 2
    states2: List[int] = [0, 7, 8, 16, 18]
    result2: int = solution.countSimpleActive(states2)
    print(f"Input: {states2}")
    print(f"Output: {result2}")
    print("Expected: 2")
    print()

    # Additional quick demonstration using values mentioned in the description.
    states3: List[int] = [1, 2, 4, 8, 0, 3, 6, 10]
    result3: int = solution.countSimpleActive(states3)
    print(f"Input: {states3}")
    print(f"Output: {result3}")
    print("Expected: 4")