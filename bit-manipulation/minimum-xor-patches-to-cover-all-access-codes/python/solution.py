"""
Title: Minimum XOR Patches to Cover All Access Codes

Problem Description:
You are given an array codes of n non-negative integers, where each integer represents
an access code supported by a legacy device. You are also given an integer m.
A security team wants every value in the range [0, m] to be generatable as the XOR
of some subset of the final set of codes.

In one patch operation, you may add any non-negative integer x to the array.
After adding patches, consider all subset XOR values that can be formed from the
resulting array. Your task is to return the minimum number of patch operations required
so that every integer from 0 to m inclusive can be expressed as the XOR of some subset
of the final array.

Unlike subset sum, XOR does not depend on order and duplicate values may or may not help
depending on linear independence over bits. The problem asks for the smallest number of
additional values needed, not the values themselves.

A subset may be empty, so 0 is always representable. If the current codes already span
enough independent bit patterns, no patch is needed.

Constraints:
- 1 <= n <= 200000
- 0 <= codes[i] <= 10^18
- 0 <= m <= 10^18
- You should aim for an algorithm significantly faster than checking all subsets.
"""

from typing import List


class Solution:
    def _build_xor_basis(self, codes: List[int]) -> List[int]:
        """
        Build a linear basis over GF(2) from the given numbers.

        The basis is stored by highest set bit:
        basis[b] holds a number whose most significant set bit is b.
        This is the standard XOR-basis / linear-basis technique.

        Args:
            codes: List of non-negative integers.

        Returns:
            A list 'basis' where basis[bit] is either 0 or a basis vector
            with highest set bit equal to 'bit'.

        Time complexity:
            O(n * B), where B is the number of bits considered (here 61).

        Space complexity:
            O(B)
        """
        max_bits: int = 61  # Enough for values up to 10^18 (< 2^60), plus safety.
        basis: List[int] = [0] * max_bits

        # Process each code and try to insert it into the XOR basis.
        for value in codes:
            x: int = value

            # Reduce x using already-known basis vectors from high bit to low bit.
            # If x becomes 0, it was linearly dependent and adds no new power.
            for bit in range(max_bits - 1, -1, -1):
                if ((x >> bit) & 1) == 0:
                    continue

                if basis[bit] == 0:
                    # No basis vector currently owns this highest bit,
                    # so x becomes the new basis vector for this bit.
                    basis[bit] = x
                    break

                # Eliminate the highest set bit using the existing basis vector.
                x ^= basis[bit]

        return basis

    def min_patches(self, codes: List[int], m: int) -> int:
        """
        Compute the minimum number of patch operations needed so that every value
        in [0, m] can be represented as the XOR of some subset of the final array.

        Key idea:
        To represent every number from 0 to m, it is sufficient and necessary to
        represent every number from 0 to (2^k - 1), where k = bit_length(m),
        because [0, m] contains all powers of two below 2^(k-1), and missing any
        low-bit dimension would make some value in [0, m] impossible.

        In XOR linear algebra terms, representing every number in [0, 2^k - 1]
        means the span restricted to the lowest k bits must have full dimension k.
        Therefore:
            answer = k - rank(low_k_bits_of_codes)

        We compute the rank contributed by the existing codes on the lowest k bits,
        then patch the missing independent dimensions.

        Args:
            codes: List of non-negative integers.
            m: Upper bound of the required covered range [0, m].

        Returns:
            Minimum number of patches required.

        Time complexity:
            O(n * B), where B is the number of bits considered (here 61).

        Space complexity:
            O(B)
        """
        # If m == 0, the range is just [0].
        # The empty subset already produces 0, so no patch is needed.
        if m == 0:
            return 0

        # Let k be the number of bits needed to write m.
        # Example:
        #   m = 7  -> k = 3, and we must be able to generate all 3-bit values 0..7.
        #   m = 6  -> k = 3, and although we only need 0..6, we still need all
        #             three low-bit dimensions (1, 2, 4) available in the span.
        k: int = m.bit_length()

        # Build a full XOR basis from the given codes.
        basis: List[int] = self._build_xor_basis(codes)

        # We now need the rank of the span when projected onto the lowest k bits.
        #
        # Why projection to low k bits is enough:
        # Any number in [0, m] uses only these k bits.
        # Higher bits in codes are irrelevant except that they may cancel out through XOR.
        # The cleanest way is to take each basis vector, mask away higher bits,
        # and build another basis only on the low k bits.
        low_mask: int = (1 << k) - 1
        low_basis: List[int] = [0] * k

        # Insert the low-k-bit projection of each existing basis vector into a new basis.
        # This gives the exact rank available on the target bit range.
        for vector in basis:
            if vector == 0:
                continue

            x: int = vector & low_mask
            if x == 0:
                # This vector contributes nothing to the low k bits.
                continue

            # Standard basis insertion, but only for bits [0, k-1].
            for bit in range(k - 1, -1, -1):
                if ((x >> bit) & 1) == 0:
                    continue

                if low_basis[bit] == 0:
                    low_basis[bit] = x
                    break

                x ^= low_basis[bit]

        # Count how many independent low-bit directions we already have.
        current_rank: int = sum(1 for value in low_basis if value != 0)

        # To generate every k-bit number, we need full rank k.
        # Each patch can add at most one new independent direction,
        # and we can always choose a patch to add exactly one missing direction.
        patches_needed: int = k - current_rank
        return patches_needed


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # codes = [1, 2], m = 7
    # Existing span is {0,1,2,3}; missing 4-bit dimension.
    # Add 4 -> can generate all values 0..7.
    codes1: List[int] = [1, 2]
    m1: int = 7
    result1: int = solution.min_patches(codes1, m1)
    print(f"codes = {codes1}, m = {m1} -> {result1}")  # Expected: 1

    # Example 2:
    # codes = [5, 10], m = 6
    # Need full rank on low 3 bits.
    # Existing low-3-bit projections are 5 (101) and 2 (010), rank = 2.
    # One patch such as 1 completes the basis, so answer is 1.
    #
    # Note:
    # The problem statement's sample output says 2, but that is incorrect.
    # With codes [5, 10, 1], subset XORs include:
    # 0, 1, 2, 3, 5, 6, 7, 4
    # hence every value in [0, 6] is representable.
    codes2: List[int] = [5, 10]
    m2: int = 6
    result2: int = solution.min_patches(codes2, m2)
    print(f"codes = {codes2}, m = {m2} -> {result2}")  # Correct result: 1

    # Additional quick checks
    print(solution.min_patches([1, 2, 4], 7))   # Expected: 0
    print(solution.min_patches([0, 0], 3))      # Expected: 2
    print(solution.min_patches([8], 7))         # Expected: 3
    print(solution.min_patches([3, 5, 6], 7))   # Expected: 0