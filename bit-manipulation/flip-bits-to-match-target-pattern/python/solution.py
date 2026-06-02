"""
Flip Bits to Match Target Pattern
==================================

Problem Description:
You are given two non-negative integers `source` and `target`. Your goal is to
determine the minimum number of bit flips required to convert `source` into `target`.

A **bit flip** means changing a single bit in the binary representation of `source`
from `0` to `1`, or from `1` to `0`.

To find which bits differ between `source` and `target`, consider using the XOR
operation. Two bits that are different will produce a `1` in the XOR result, and
two bits that are the same will produce a `0`. Therefore, the answer is simply the
number of `1` bits in the XOR of `source` and `target` (also known as the Hamming
distance).

Constraints:
    - 0 <= source, target <= 10^9

Examples:
    Example 1:
        Input:  source = 10, target = 15
        Binary: source = 1010, target = 1111
        XOR:    0101  → two 1-bits
        Output: 2

    Example 2:
        Input:  source = 0, target = 8
        Binary: source = 0000, target = 1000
        XOR:    1000  → one 1-bit
        Output: 1
"""


class Solution:
    def minBitFlips(self, source: int, target: int) -> int:
        """
        Calculate the minimum number of bit flips to convert source into target.

        The key insight is that XOR (^) of two integers produces a 1-bit wherever
        the two integers differ and a 0-bit wherever they are the same. So the
        number of 1-bits in (source XOR target) equals the number of positions
        where the bits differ — which is exactly the number of flips needed.

        Args:
            source (int): The starting non-negative integer.
            target (int): The desired non-negative integer.

        Returns:
            int: The minimum number of bit flips required to convert source to target.

        Time Complexity:  O(log(max(source, target))) — we examine each bit once.
                          Python's built-in bin().count() is O(number of bits).
        Space Complexity: O(log(max(source, target))) — for the binary string
                          representation created internally by bin().
        """

        # -----------------------------------------------------------------------
        # Step 1: Compute XOR of source and target
        # -----------------------------------------------------------------------
        # XOR (^) compares each pair of corresponding bits:
        #   - If both bits are the same (0^0 or 1^1), the result bit is 0.
        #   - If the bits differ (0^1 or 1^0), the result bit is 1.
        #
        # Example 1: source=10 (1010), target=15 (1111)
        #   XOR = 1010 ^ 1111 = 0101  (bits at positions 0 and 2 differ)
        #
        # Example 2: source=0 (0000), target=8 (1000)
        #   XOR = 0000 ^ 1000 = 1000  (bit at position 3 differs)
        #
        # Every 1-bit in the XOR result represents one required flip.
        xor_result = source ^ target

        # -----------------------------------------------------------------------
        # Step 2: Count the number of 1-bits in the XOR result
        # -----------------------------------------------------------------------
        # Python's built-in bin() converts an integer to its binary string
        # representation, e.g., bin(5) → '0b101'.
        # We then call .count('1') on that string to count the set bits.
        #
        # Example 1: xor_result = 5  → bin(5) = '0b101' → count('1') = 2  ✓
        # Example 2: xor_result = 8  → bin(8) = '0b1000' → count('1') = 1  ✓
        #
        # Alternative approach (manual bit counting) is shown in the comments
        # below for educational purposes, but bin().count('1') is idiomatic
        # Python and perfectly efficient for this problem's constraints.
        #
        #   Manual approach:
        #       count = 0
        #       n = xor_result
        #       while n:
        #           count += n & 1   # check the least-significant bit
        #           n >>= 1          # shift right to examine the next bit
        #       return count
        #
        # We use bin().count('1') for clarity and conciseness.
        flip_count = bin(xor_result).count('1')

        # -----------------------------------------------------------------------
        # Step 3: Return the flip count
        # -----------------------------------------------------------------------
        # This is the Hamming distance between source and target — the minimum
        # number of single-bit changes needed to transform one into the other.
        return flip_count


# ---------------------------------------------------------------------------
# Main block: demonstrate and verify the solution with the provided examples
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # source = 10  →  binary: 1010
    # target = 15  →  binary: 1111
    # XOR          →  binary: 0101  (2 ones)
    # Expected output: 2
    # ------------------------------------------------------------------
    source1, target1 = 10, 15
    result1 = solution.minBitFlips(source1, target1)
    print(f"Example 1:")
    print(f"  source = {source1}  (binary: {bin(source1)})")
    print(f"  target = {target1}  (binary: {bin(target1)})")
    print(f"  XOR    = {source1 ^ target1}  (binary: {bin(source1 ^ target1)})")
    print(f"  Minimum bit flips: {result1}")
    print(f"  Expected:          2")
    print(f"  Correct: {result1 == 2}")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # source = 0   →  binary: 0
    # target = 8   →  binary: 1000
    # XOR          →  binary: 1000  (1 one)
    # Expected output: 1
    # ------------------------------------------------------------------
    source2, target2 = 0, 8
    result2 = solution.minBitFlips(source2, target2)
    print(f"Example 2:")
    print(f"  source = {source2}  (binary: {bin(source2)})")
    print(f"  target = {target2}  (binary: {bin(target2)})")
    print(f"  XOR    = {source2 ^ target2}  (binary: {bin(source2 ^ target2)})")
    print(f"  Minimum bit flips: {result2}")
    print(f"  Expected:          1")
    print(f"  Correct: {result2 == 1}")
    print()

    # ------------------------------------------------------------------
    # Additional edge case: source == target (no flips needed)
    # ------------------------------------------------------------------
    source3, target3 = 42, 42
    result3 = solution.minBitFlips(source3, target3)
    print(f"Edge Case (source == target):")
    print(f"  source = {source3}  (binary: {bin(source3)})")
    print(f"  target = {target3}  (binary: {bin(target3)})")
    print(f"  XOR    = {source3 ^ target3}  (binary: {bin(source3 ^ target3)})")
    print(f"  Minimum bit flips: {result3}")
    print(f"  Expected:          0")
    print(f"  Correct: {result3 == 0}")