"""
Odd Bit Pair Swapper
====================

Problem Description:
You are given an array of non-negative integers `nums`. Your task is to transform
each integer in the array by swapping every pair of adjacent bits. Specifically,
for each integer, the bit at position 0 swaps with the bit at position 1, the bit
at position 2 swaps with the bit at position 3, and so on for all 32 bits.

After transforming all integers, return the count of pairs (i, j) where i < j
such that the XOR of nums[i] and nums[j] (after transformation) has exactly k
bits set to 1.

Constraints:
- 1 <= nums.length <= 1000
- 0 <= nums[i] <= 2^30 - 1
- 0 <= k <= 30
"""

from typing import List


class Solution:
    def swap_adjacent_bits(self, n: int) -> int:
        """
        Swap every pair of adjacent bits in a 32-bit integer.

        The idea:
        - Extract all even-positioned bits (positions 0, 2, 4, ...) using mask 0x55555555
          (binary: 0101 0101 ... 0101). These are the "odd" bits in 0-indexed terms.
        - Extract all odd-positioned bits (positions 1, 3, 5, ...) using mask 0xAAAAAAAA
          (binary: 1010 1010 ... 1010).
        - Shift even bits LEFT by 1 (to move them to the next higher position).
        - Shift odd bits RIGHT by 1 (to move them to the next lower position).
        - OR the two results together to get the swapped integer.

        Args:
            n: A non-negative integer to transform.

        Returns:
            The integer with every pair of adjacent bits swapped.

        Time complexity: O(1) — fixed number of bitwise operations regardless of input.
        Space complexity: O(1) — no extra space used.
        """
        # -----------------------------------------------------------------------
        # Step 1: Define the two masks we need.
        #
        # 0x55555555 in binary (32 bits):
        #   0101 0101 0101 0101 0101 0101 0101 0101
        # This mask selects bits at positions 0, 2, 4, 6, ... (even positions).
        #
        # 0xAAAAAAAA in binary (32 bits):
        #   1010 1010 1010 1010 1010 1010 1010 1010
        # This mask selects bits at positions 1, 3, 5, 7, ... (odd positions).
        # -----------------------------------------------------------------------
        even_mask = 0x55555555  # selects bits at positions 0, 2, 4, ...
        odd_mask  = 0xAAAAAAAA  # selects bits at positions 1, 3, 5, ...

        # -----------------------------------------------------------------------
        # Step 2: Extract even-positioned bits and shift them LEFT by 1.
        #
        # Example with n = 2 (binary: ...0010):
        #   n & even_mask = 0010 & 0101 = 0000  (no even-position bits set)
        #   0000 << 1 = 0000
        #
        # Example with n = 1 (binary: ...0001):
        #   n & even_mask = 0001 & 0101 = 0001  (bit 0 is set)
        #   0001 << 1 = 0010  (moved to position 1)
        # -----------------------------------------------------------------------
        even_bits_shifted = (n & even_mask) << 1

        # -----------------------------------------------------------------------
        # Step 3: Extract odd-positioned bits and shift them RIGHT by 1.
        #
        # Example with n = 2 (binary: ...0010):
        #   n & odd_mask = 0010 & 1010 = 0010  (bit 1 is set)
        #   0010 >> 1 = 0001  (moved to position 0)
        #
        # Example with n = 1 (binary: ...0001):
        #   n & odd_mask = 0001 & 1010 = 0000  (no odd-position bits set)
        #   0000 >> 1 = 0000
        # -----------------------------------------------------------------------
        odd_bits_shifted = (n & odd_mask) >> 1

        # -----------------------------------------------------------------------
        # Step 4: Combine the two shifted results with OR.
        #
        # For n = 2: even_bits_shifted | odd_bits_shifted = 0000 | 0001 = 0001 = 1 ✓
        # For n = 1: even_bits_shifted | odd_bits_shifted = 0010 | 0000 = 0010 = 2 ✓
        # For n = 3 (binary 0011):
        #   even: 0011 & 0101 = 0001 → 0001 << 1 = 0010
        #   odd:  0011 & 1010 = 0010 → 0010 >> 1 = 0001
        #   result: 0010 | 0001 = 0011 = 3 ✓
        # -----------------------------------------------------------------------
        return even_bits_shifted | odd_bits_shifted

    def count_pairs_with_k_set_bits_xor(self, nums: List[int], k: int) -> int:
        """
        Count pairs (i, j) with i < j such that XOR of transformed nums[i]
        and transformed nums[j] has exactly k bits set to 1.

        Algorithm:
        1. Transform every element by swapping adjacent bits.
        2. For every pair (i, j) with i < j, compute XOR of the two transformed values.
        3. Count the number of 1-bits in the XOR result (popcount).
        4. If popcount == k, increment the answer counter.

        Args:
            nums: List of non-negative integers.
            k:    Target number of set bits in the XOR result.

        Returns:
            Count of valid pairs.

        Time complexity: O(n^2) — we check every pair; n = len(nums).
        Space complexity: O(n) — we store the transformed array.
        """
        # -----------------------------------------------------------------------
        # Step 1: Transform every number in the array.
        #
        # We build a new list `transformed` where each element is the result of
        # swapping adjacent bits in the corresponding element of `nums`.
        # Using a list comprehension keeps the code concise and readable.
        # -----------------------------------------------------------------------
        transformed: List[int] = [self.swap_adjacent_bits(x) for x in nums]

        # -----------------------------------------------------------------------
        # Step 2: Initialise the answer counter.
        # -----------------------------------------------------------------------
        count: int = 0

        n: int = len(transformed)

        # -----------------------------------------------------------------------
        # Step 3: Iterate over all unique pairs (i, j) with i < j.
        #
        # The outer loop runs from index 0 to n-2.
        # The inner loop runs from i+1 to n-1.
        # This guarantees we never count the same pair twice and never compare
        # an element with itself.
        # -----------------------------------------------------------------------
        for i in range(n):
            for j in range(i + 1, n):
                # ---------------------------------------------------------------
                # Step 3a: Compute XOR of the two transformed values.
                #
                # XOR produces a 1-bit wherever the two values differ.
                # The number of 1-bits in the XOR tells us how many bit positions
                # are different between the two numbers.
                # ---------------------------------------------------------------
                xor_result: int = transformed[i] ^ transformed[j]

                # ---------------------------------------------------------------
                # Step 3b: Count the number of 1-bits (popcount) in xor_result.
                #
                # Python's built-in bin() converts an integer to its binary string
                # representation (e.g., bin(5) → '0b101').
                # .count('1') then counts how many '1' characters are in that string.
                # This is a clean, Pythonic way to compute popcount without needing
                # any external library.
                # ---------------------------------------------------------------
                set_bits: int = bin(xor_result).count('1')

                # ---------------------------------------------------------------
                # Step 3c: If the popcount equals k, this pair is valid.
                # ---------------------------------------------------------------
                if set_bits == k:
                    count += 1

        # -----------------------------------------------------------------------
        # Step 4: Return the total count of valid pairs.
        # -----------------------------------------------------------------------
        return count


# ---------------------------------------------------------------------------
# Main block — demonstrates the solution with the examples from the problem.
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # nums = [2, 1, 3], k = 1
    #
    # Transformation:
    #   2  (binary 10)  → swap bits → 01  = 1
    #   1  (binary 01)  → swap bits → 10  = 2
    #   3  (binary 11)  → swap bits → 11  = 3
    # Transformed array: [1, 2, 3]
    #
    # XOR pairs:
    #   1 ^ 2 = 3  (binary 11) → 2 set bits  → NOT counted (k=1)
    #   1 ^ 3 = 2  (binary 10) → 1 set bit   → counted ✓
    #   2 ^ 3 = 1  (binary 01) → 1 set bit   → counted ✓
    #
    # Expected output: 2
    # -----------------------------------------------------------------------
    nums1 = [2, 1, 3]
    k1 = 1
    result1 = solution.count_pairs_with_k_set_bits_xor(nums1, k1)
    print(f"Example 1:")
    print(f"  Input : nums={nums1}, k={k1}")
    print(f"  Output: {result1}")          # Expected: 2
    print(f"  Match : {result1 == 2}")
    print()

    # -----------------------------------------------------------------------
    # Example 2
    # nums = [5, 10, 0], k = 2
    #
    # Transformation:
    #   5  (binary 0101) → swap bits → 1010 = 10
    #   10 (binary 1010) → swap bits → 0101 = 5
    #   0  (binary 0000) → swap bits → 0000 = 0
    # Transformed array: [10, 5, 0]
    #
    # XOR pairs:
    #   10 ^ 5  = 15 (binary 1111) → 4 set bits → NOT counted (k=2)
    #   10 ^ 0  = 10 (binary 1010) → 2 set bits → counted ✓
    #    5 ^ 0  =  5 (binary 0101) → 2 set bits → counted ✓
    #
    # Expected output: 2
    # -----------------------------------------------------------------------
    nums2 = [5, 10, 0]
    k2 = 2
    result2 = solution.count_pairs_with_k_set_bits_xor(nums2, k2)
    print(f"Example 2:")
    print(f"  Input : nums={nums2}, k={k2}")
    print(f"  Output: {result2}")          # Expected: 2
    print(f"  Match : {result2 == 2}")
    print()

    # -----------------------------------------------------------------------
    # Additional edge-case: single element — no pairs possible.
    # -----------------------------------------------------------------------
    nums3 = [7]
    k3 = 1
    result3 = solution.count_pairs_with_k_set_bits_xor(nums3, k3)
    print(f"Edge case (single element):")
    print(f"  Input : nums={nums3}, k={k3}")
    print(f"  Output: {result3}")          # Expected: 0
    print(f"  Match : {result3 == 0}")
    print()

    # -----------------------------------------------------------------------
    # Additional edge-case: k = 0 means XOR must be 0, i.e., equal elements
    # after transformation.
    # nums = [3, 3, 5], k = 0
    # Transformed: 3→3, 3→3, 5→10
    # Pairs:
    #   3 ^ 3 = 0  → 0 set bits → counted ✓
    #   3 ^ 10 = 9 → 2 set bits → NOT counted
    #   3 ^ 10 = 9 → 2 set bits → NOT counted
    # Expected: 1
    # -----------------------------------------------------------------------
    nums4 = [3, 3, 5]
    k4 = 0
    result4 = solution.count_pairs_with_k_set_bits_xor(nums4, k4)
    print(f"Edge case (k=0, equal transformed values):")
    print(f"  Input : nums={nums4}, k={k4}")
    print(f"  Output: {result4}")          # Expected: 1
    print(f"  Match : {result4 == 1}")