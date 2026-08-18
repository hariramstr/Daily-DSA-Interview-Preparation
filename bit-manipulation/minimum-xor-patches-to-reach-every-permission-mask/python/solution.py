"""
Title: Minimum XOR Patches to Reach Every Permission Mask

Problem Description:
A security platform stores user roles as bitmasks. You are given an array `roles` of
length `n`, where each value is an integer in the range `[0, 2^b - 1]` representing
a currently deployed role mask over `b` permission bits. You may deploy additional
role masks, called patches. After patching, the system is considered fully expressive
if every mask in `[0, 2^b - 1]` can be formed as the bitwise XOR of some subset of the
deployed masks (original roles plus patches). Each deployed mask may be used at most
once in a subset, and the empty subset produces `0`.

Return the minimum number of patches required to make the system fully expressive.

This is not asking you to construct all subsets explicitly. A correct solution must
exploit properties of XOR spaces over bits. In particular, two masks may be redundant
if one can already be produced by XOR-ing others. Your task is to determine how many
new independent masks must be added so that the deployed set spans the entire `b`-bit
space.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= b <= 60
- 0 <= roles[i] < 2^b
- The answer always fits in a 32-bit signed integer.
"""

from typing import List


class Solution:
    def _xor_basis_rank(self, roles: List[int], b: int) -> int:
        """
        Compute the rank (dimension) of the XOR span of the given masks.

        The rank is the number of linearly independent vectors over GF(2)
        among the provided bitmasks. This tells us how many dimensions of the
        `b`-bit space are already covered by the current deployed roles.

        Args:
            roles: List of current role masks.
            b: Number of bits in each mask.

        Returns:
            The rank of the XOR basis formed by `roles`.

        Time complexity:
            O(n * b), where n is the number of roles.

        Space complexity:
            O(b), for storing one basis vector per bit position.
        """
        # We maintain a linear basis over GF(2).
        #
        # Key idea:
        # - Each integer is treated as a vector of bits.
        # - XOR is exactly vector addition in GF(2).
        # - We want to keep only independent vectors.
        #
        # Data structure:
        # - basis[bit] stores a basis vector whose highest set bit is `bit`.
        # - If basis[bit] == 0, we do not yet have a basis vector leading at that bit.
        #
        # Example:
        # If basis[5] is non-zero, then we already have one independent vector
        # whose most significant set bit is 5.
        basis: List[int] = [0] * b

        # `rank` counts how many independent vectors we have inserted.
        rank = 0

        # Process each role one by one.
        for value in roles:
            # `x` will be reduced using the current basis.
            # If it becomes 0, then it was dependent on previous vectors.
            # If it remains non-zero and finds an empty leading-bit slot,
            # it is independent and gets inserted into the basis.
            x = value

            # We scan from the most significant bit down to the least significant bit.
            # This is analogous to Gaussian elimination, but for bits and XOR.
            for bit in range(b - 1, -1, -1):
                # If this bit is not set in x, it cannot be the leading bit.
                if ((x >> bit) & 1) == 0:
                    continue

                # If we do not yet have a basis vector with this leading bit,
                # then x is independent from all previous basis vectors.
                # We insert it here and increase the rank.
                if basis[bit] == 0:
                    basis[bit] = x
                    rank += 1
                    break

                # Otherwise, eliminate the leading bit of x using the existing basis vector.
                # This is the XOR equivalent of subtracting a pivot row in Gaussian elimination.
                x ^= basis[bit]

            # If x becomes 0, it means the current role can already be formed
            # by XOR-ing some subset of previously inserted independent roles.
            # Therefore it adds no new expressive power and is redundant.

        return rank

    def minimum_xor_patches(self, roles: List[int], b: int) -> int:
        """
        Return the minimum number of additional independent masks needed so that
        all `b`-bit masks become representable as XORs of subsets.

        The full `b`-bit space has dimension `b`. If the current roles span a
        subspace of dimension `rank`, then we need exactly `b - rank` more
        independent vectors to span the entire space.

        Args:
            roles: List of current role masks.
            b: Number of bits in each mask.

        Returns:
            Minimum number of patches required.

        Time complexity:
            O(n * b), where n is the number of roles.

        Space complexity:
            O(b).
        """
        # Step 1:
        # Compute how many independent dimensions are already covered by the roles.
        rank = self._xor_basis_rank(roles, b)

        # Step 2:
        # The total space of all b-bit masks has dimension exactly b.
        #
        # Why?
        # Because each bit position can be thought of as one independent axis.
        # Therefore, to generate every possible mask from 0 to 2^b - 1,
        # we need a spanning set of size b in terms of independent dimensions.
        #
        # If we already have `rank` dimensions, then the number of missing
        # dimensions is simply `b - rank`.
        patches_needed = b - rank

        return patches_needed


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # roles = [1, 2], b = 3
    # Independent vectors:
    # - 1  -> binary 001
    # - 2  -> binary 010
    # Rank = 2, so patches needed = 3 - 2 = 1
    roles1 = [1, 2]
    b1 = 3
    result1 = solution.minimum_xor_patches(roles1, b1)
    print(result1)  # Expected: 1

    # Example 2:
    # roles = [3, 5, 6], b = 3
    # 3 = 011
    # 5 = 101
    # 6 = 110 = 3 XOR 5
    # So only 2 are independent.
    # Rank = 2, so patches needed = 3 - 2 = 1
    roles2 = [3, 5, 6]
    b2 = 3
    result2 = solution.minimum_xor_patches(roles2, b2)
    print(result2)  # Expected: 1

    # Additional quick sanity checks:
    roles3 = [1, 2, 4]
    b3 = 3
    result3 = solution.minimum_xor_patches(roles3, b3)
    print(result3)  # Expected: 0

    roles4 = [0, 0, 0]
    b4 = 4
    result4 = solution.minimum_xor_patches(roles4, b4)
    print(result4)  # Expected: 4