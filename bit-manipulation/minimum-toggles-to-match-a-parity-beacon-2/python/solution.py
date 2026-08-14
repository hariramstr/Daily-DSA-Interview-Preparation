"""
Title: Minimum Toggles to Match a Parity Beacon

Problem Description:
A monitoring system stores the state of n beacon modules in a binary array bits, where
bits[i] is either 0 or 1. You are also given a target binary array target of the same
length. In one operation, you may choose any index i and toggle bits[i]. However,
toggling index i also automatically toggles every index j > i such that j and i have
the same parity (both even or both odd). In other words, choosing i flips bits[i],
bits[i+2], bits[i+4], and so on.

Your task is to return the minimum number of operations required to transform bits into
target. If it is impossible, return -1.

This problem is designed to reward careful reasoning about how each operation affects
independent parity chains. A brute-force search over all sequences of toggles will be
too slow for large inputs. Instead, you should exploit the structure of the operation
and process the array efficiently.

Constraints:
- 1 <= n <= 200000
- bits.length == target.length == n
- bits[i] is 0 or 1
- target[i] is 0 or 1

Key Insight:
Indices of the same parity form independent chains:
- Even chain: 0, 2, 4, ...
- Odd chain: 1, 3, 5, ...

An operation at index i flips a suffix of exactly one of these chains. Therefore, the
problem becomes:
For each parity chain, given a binary sequence and a target sequence, what is the
minimum number of suffix flips needed to transform one into the other?

For a single chain, the greedy left-to-right strategy is optimal:
- Track whether an odd or even number of flips has affected the current position.
- If the current effective bit does not match the target, we must flip here.
- This is forced, because later flips cannot affect earlier positions.

The total answer is:
minimum flips for even chain + minimum flips for odd chain

This is always possible, so -1 is never needed under the given operation model.
"""

from typing import List


class Solution:
    def _min_flips_for_parity_chain(
        self,
        bits: List[int],
        target: List[int],
        start: int,
    ) -> int:
        """
        Compute the minimum number of operations needed for one parity chain.

        We process indices:
        - start, start + 2, start + 4, ...

        Each operation at one of these indices flips that index and every later index
        in the same chain. This is exactly a suffix flip on the parity chain.

        Greedy rule:
        - Maintain whether the current position has been flipped an odd number of times.
        - Determine the effective current bit after previous operations.
        - If it does not match the target, we must flip here.

        Args:
            bits: Original binary array.
            target: Desired binary array.
            start: Starting index of the parity chain (0 for even, 1 for odd).

        Returns:
            The minimum number of operations required for this parity chain.

        Time complexity:
            O(k), where k is the number of elements in this parity chain.

        Space complexity:
            O(1).
        """
        # This variable stores how many flips have affected the current suffix parity-wise,
        # but we only care whether that count is even or odd.
        #
        # flip_parity == 0:
        #   The current bit is unchanged by previous chosen operations in this chain.
        #
        # flip_parity == 1:
        #   The current bit has been toggled once modulo 2 by previous operations.
        flip_parity: int = 0

        # Count how many operations we perform for this chain.
        operations: int = 0

        # Walk left to right through only one parity chain.
        #
        # Why left to right?
        # Because an operation at a current index affects the current index and future
        # indices in the same chain, but it can never affect earlier indices.
        #
        # That means once we reach a position, if it is wrong, the decision to flip here
        # is forced. Waiting until later would not fix this position.
        for i in range(start, len(bits), 2):
            # Compute the effective bit value after all previous flips in this chain.
            #
            # If flip_parity == 0, effective_bit = bits[i]
            # If flip_parity == 1, effective_bit = bits[i] ^ 1
            effective_bit: int = bits[i] ^ flip_parity

            # If the current effective bit already matches the target, we do nothing.
            # This is best because unnecessary flips would only increase the answer.
            if effective_bit == target[i]:
                continue

            # Otherwise, the current position is wrong.
            #
            # Since future operations cannot change earlier positions, the only way to fix
            # this position is to flip exactly here.
            operations += 1

            # Flipping here toggles this position and every later position in the same chain.
            # So for all subsequent positions in this chain, the flip parity changes.
            flip_parity ^= 1

        return operations

    def min_operations(self, bits: List[int], target: List[int]) -> int:
        """
        Return the minimum number of operations required to transform bits into target.

        The array splits into two independent parity chains:
        - even indices: 0, 2, 4, ...
        - odd indices: 1, 3, 5, ...

        An operation at index i only affects one of these chains, never both.
        Therefore, we solve each chain independently and add the results.

        Args:
            bits: Original binary array.
            target: Desired binary array.

        Returns:
            The minimum number of operations needed. Under this operation model,
            transformation is always possible, so this method returns a non-negative
            integer.

        Time complexity:
            O(n), where n is the length of the arrays.

        Space complexity:
            O(1), ignoring input storage.
        """
        # Basic validation for beginner-friendliness and safer execution.
        #
        # The problem guarantees valid input, but these checks make the function more robust
        # if used elsewhere.
        if len(bits) != len(target):
            return -1

        # Solve the even-index chain independently.
        even_operations: int = self._min_flips_for_parity_chain(bits, target, 0)

        # Solve the odd-index chain independently.
        odd_operations: int = self._min_flips_for_parity_chain(bits, target, 1)

        # Since the chains do not interfere with each other, the total minimum is the sum.
        return even_operations + odd_operations


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    #
    # bits   = [1, 0, 1, 1, 0]
    # target = [0, 0, 0, 1, 1]
    #
    # Even chain indices: 0, 2, 4
    #   bits   -> [1, 1, 0]
    #   target -> [0, 0, 1]
    # One flip at index 0 toggles positions 0, 2, 4:
    #   [1,0,1,1,0] -> [0,0,0,1,1]
    # So answer is 1.
    bits1: List[int] = [1, 0, 1, 1, 0]
    target1: List[int] = [0, 0, 0, 1, 1]
    result1: int = solution.min_operations(bits1, target1)
    print("Example 1 result:", result1)  # Expected: 1

    # Example 2
    #
    # bits   = [0, 1, 0, 1]
    # target = [1, 0, 1, 0]
    #
    # Flip index 0 -> affects 0 and 2:
    #   [0,1,0,1] -> [1,1,1,1]
    # Flip index 1 -> affects 1 and 3:
    #   [1,1,1,1] -> [1,0,1,0]
    # So answer is 2.
    bits2: List[int] = [0, 1, 0, 1]
    target2: List[int] = [1, 0, 1, 0]
    result2: int = solution.min_operations(bits2, target2)
    print("Example 2 result:", result2)  # Expected: 2

    # Additional quick sanity checks
    bits3: List[int] = [1]
    target3: List[int] = [1]
    print("Sanity check 1:", solution.min_operations(bits3, target3))  # Expected: 0

    bits4: List[int] = [1]
    target4: List[int] = [0]
    print("Sanity check 2:", solution.min_operations(bits4, target4))  # Expected: 1

    bits5: List[int] = [0, 0, 0, 0, 0, 0]
    target5: List[int] = [1, 1, 1, 1, 1, 1]
    print("Sanity check 3:", solution.min_operations(bits5, target5))  # Expected: 2