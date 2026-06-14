"""
Title: Minimum Toggles to Equalize Binary Counters

Problem Description:
You are given an array `counters` of `n` non-negative integers. Each integer represents
the current value of a hardware counter. In one operation, you may choose a bit position
`b` (0-indexed) and toggle that bit in exactly two different counters. Toggling means
changing a `0` to `1` or a `1` to `0` at that bit position.

Your task is to determine the minimum number of operations required to make all counters
equal. If it is impossible, return `-1`.

This operation models a paired electrical pulse: each pulse must affect the same bit
position in two different devices at the same time. Because of this restriction, you
cannot freely change bits one counter at a time.

Return the smallest number of such paired toggles needed so that every value in the array
becomes identical.

Important observations:
- You may perform operations on different bit positions independently.
- The final common value does not need to be one of the original values.
- Two counters chosen in an operation must be distinct.

Constraints:
- 1 <= n <= 10^5
- 0 <= counters[i] <= 10^9

Example 1:
Input: counters = [1, 0, 1, 0]
Output: 1

Example 2:
Input: counters = [3, 3, 1]
Output: -1
"""

from typing import List


class Solution:
    def min_operations_to_equalize(self, counters: List[int]) -> int:
        """
        Compute the minimum number of paired bit-toggle operations needed to make
        all counters equal, or return -1 if it cannot be done.

        The key idea is to process each bit position independently:
        - An operation only affects one chosen bit position.
        - At a fixed bit, each operation flips exactly two counters, so the parity
          (odd/even) of the number of 1s at that bit never changes.
        - For all counters to become equal, at each bit the final state must be
          either all 0s or all 1s.
        - Therefore:
            * If the count of 1s at a bit is odd and n is odd, both all-0 and all-1
              targets may be parity-compatible depending on n.
            * More simply and correctly:
              final all-equal means the final number of 1s at each bit must be either
              0 or n. Since parity is preserved, one of these must have the same parity
              as the current count of 1s.
            * If n is even, both 0 and n are even, so the current count must be even.
            * If n is odd, 0 is even and n is odd, so any parity is possible.
        - Once a valid final bit value exists, the minimum operations for that bit is
          the smaller of:
            count_ones // 2          to make all bits 0
            (n - count_ones) // 2    to make all bits 1
          but only among parity-valid targets.

        Args:
            counters: List of non-negative integers.

        Returns:
            The minimum number of operations required, or -1 if impossible.

        Time complexity:
            O(n * B), where B is the number of bit positions checked.
            Here B is at most about 31 for values up to 1e9, so this is effectively O(n).

        Space complexity:
            O(1), excluding the input.
        """
        n: int = len(counters)

        # If there is only one counter, it is already equal to itself.
        # No operation is needed.
        if n == 1:
            return 0

        # We need to examine enough bit positions to cover all values in the input.
        # Since counters[i] <= 1e9, 30 bits are enough in practice, but using the
        # actual maximum bit length is cleaner and still efficient.
        #
        # We also use at least 1 bit so that the all-zero case is handled naturally.
        max_value: int = max(counters)
        max_bits: int = max(1, max_value.bit_length())

        # This will accumulate the minimum number of operations across all bit positions.
        total_operations: int = 0

        # We process each bit independently because:
        # - An operation chooses exactly one bit position.
        # - Changing one bit never affects another bit.
        # Therefore, the global minimum is simply the sum of the per-bit minima.
        for bit in range(max_bits):
            # Count how many counters currently have a 1 at this bit position.
            ones_count: int = 0

            # Scan every counter and inspect the current bit.
            for value in counters:
                # Shift the target bit to the least significant position, then mask with 1.
                # If the result is 1, this counter has a 1 at this bit.
                ones_count += (value >> bit) & 1

            # The number of 0s at this bit is the remaining counters.
            zeros_count: int = n - ones_count

            # We now decide whether it is possible to make this bit equal across all counters,
            # and if so, what the minimum number of operations is.
            #
            # Final equal state at this bit can only be:
            #   1) all 0  -> final number of 1s = 0
            #   2) all 1  -> final number of 1s = n
            #
            # Each operation flips two bits at this position:
            # - flipping two 1s decreases ones_count by 2
            # - flipping two 0s increases ones_count by 2
            # - flipping one 1 and one 0 keeps ones_count unchanged
            #
            # So parity of ones_count never changes.
            #
            # We test both possible targets and keep the cheaper valid one.
            best_for_this_bit: int | None = None

            # Option 1: make all bits 0 at this position.
            # This requires changing every current 1 into 0.
            # Since one operation can flip two 1s to 0s together, this takes ones_count // 2
            # operations, but only if ones_count is even.
            if ones_count % 2 == 0:
                operations_to_all_zero: int = ones_count // 2
                best_for_this_bit = operations_to_all_zero

            # Option 2: make all bits 1 at this position.
            # This requires changing every current 0 into 1.
            # Since one operation can flip two 0s to 1s together, this takes zeros_count // 2
            # operations, but only if zeros_count is even.
            if zeros_count % 2 == 0:
                operations_to_all_one: int = zeros_count // 2
                if best_for_this_bit is None:
                    best_for_this_bit = operations_to_all_one
                else:
                    best_for_this_bit = min(best_for_this_bit, operations_to_all_one)

            # If neither target is valid, then this bit can never be made equal across all
            # counters, so the whole problem is impossible.
            if best_for_this_bit is None:
                return -1

            # Add the minimum cost for this bit to the global answer.
            total_operations += best_for_this_bit

        # All bits were independently solvable, so the sum is the true minimum.
        return total_operations


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [1, 0, 1, 0],  # Expected: 1
        [3, 3, 1],     # Expected: -1
        [0],           # Expected: 0
        [2, 2, 2],     # Expected: 0
        [1, 1, 0, 0],  # Expected: 1
    ]

    for counters in sample_inputs:
        result = solution.min_operations_to_equalize(counters)
        print(f"counters = {counters} -> minimum operations = {result}")