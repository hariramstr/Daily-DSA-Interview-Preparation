"""
Title: Count Servers With Pairwise-Unique Capability Masks
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
You are given an integer array masks where masks[i] represents the enabled capabilities
of the i-th server as a bitmask. Two servers are considered compatible for a special
deployment if they do not share any enabled capability bit. In other words, for servers
i and j, they are compatible if (masks[i] & masks[j]) == 0.

Return the number of unordered pairs of distinct servers that are compatible.

This problem is designed for situations where each server has a small fixed set of
possible capability bits, but the number of servers can be large. A brute-force O(n^2)
comparison over all pairs may be too slow. You should take advantage of the bitmask
structure to count valid pairs efficiently.

The answer can be large, so return it as a 64-bit integer.

Constraints:
- 1 <= masks.length <= 200000
- 0 <= masks[i] < 2^20
- Capability bits are numbered from 0 to 19
- Multiple servers may have the same mask value

Example 1:
Input: masks = [1, 2, 3, 4]
Output: 4

Example 2:
Input: masks = [0, 1, 1, 2, 6]
Output: 6
"""

from typing import List


class Solution:
    def count_compatible_pairs(self, masks: List[int]) -> int:
        """
        Count the number of unordered pairs of distinct servers whose bitwise AND is zero.

        The method uses frequency counting plus SOS DP (Sum Over Subsets Dynamic Programming).
        For each mask value, we count how many servers have that exact mask. Then, for every
        possible bitmask x, we compute how many server masks are subsets of x. This lets us
        quickly determine, for any server mask m, how many server masks are fully contained
        in the complement of m, which is exactly the set of masks compatible with m.

        Args:
            masks: List of server capability bitmasks.

        Returns:
            The number of unordered compatible pairs as an integer.

        Time complexity:
            O(20 * 2^20 + n)

        Space complexity:
            O(2^20)
        """
        # There are exactly 20 possible capability bits: 0 through 19.
        # Therefore, every mask fits in the range [0, 2^20).
        bits: int = 20
        size: int = 1 << bits
        full_mask: int = size - 1

        # freq[mask] will store how many servers have exactly this capability mask.
        # This compresses the input: instead of comparing individual servers, we operate
        # on counts of identical masks.
        freq: List[int] = [0] * size
        for mask in masks:
            freq[mask] += 1

        # subset_count will become a DP table where:
        # subset_count[x] = total number of input masks 'm' such that m is a subset of x
        #                 = total number of masks m with (m & ~x) == 0
        #
        # We start with the exact frequencies. Then SOS DP transforms this into
        # "sum over all subsets".
        subset_count: List[int] = freq[:]

        # SOS DP explanation:
        # For each bit position, we propagate counts from masks without that bit
        # into masks with that bit. After processing all bits, subset_count[x]
        # contains the sum of freq[s] for every subset s of x.
        #
        # Why this helps:
        # A mask 'a' is compatible with mask 'b' if (a & b) == 0.
        # That means every set bit of b must be absent from a.
        # Equivalently, b must be a subset of the complement of a.
        # So for a fixed a, the number of compatible masks is:
        # subset_count[full_mask ^ a]
        for bit in range(bits):
            for mask in range(size):
                # If the current mask has this bit set, then one of its subsets is the
                # same mask but with this bit turned off.
                #
                # Example:
                # mask = 10110, bit = 2
                # mask without bit 2 = 10010
                #
                # Any subset of 10010 is also a subset of 10110, so we add those counts.
                if mask & (1 << bit):
                    subset_count[mask] += subset_count[mask ^ (1 << bit)]

        # Now count ordered compatible pairs by summing, for each actual server mask m,
        # how many server masks lie inside the complement of m.
        #
        # If c = full_mask ^ m, then subset_count[c] gives the number of input masks x
        # such that x is a subset of c, which is equivalent to (x & m) == 0.
        #
        # Multiplying by freq[m] counts all ordered pairs where the first server has mask m.
        ordered_pairs: int = 0
        for mask_value in range(size):
            count_of_this_mask: int = freq[mask_value]
            if count_of_this_mask == 0:
                continue

            compatible_space: int = full_mask ^ mask_value
            compatible_count: int = subset_count[compatible_space]

            ordered_pairs += count_of_this_mask * compatible_count

        # Important correction:
        # The above counts ordered pairs (i, j), including self-pairs when mask[i] == 0,
        # because 0 & 0 == 0.
        #
        # We need unordered pairs of DISTINCT servers.
        #
        # Step 1: remove self-pairs.
        # A server is compatible with itself only if its mask is 0.
        # There are freq[0] such self-pairs in the ordered count: one for each zero-mask server.
        ordered_pairs -= freq[0]

        # Step 2: convert ordered distinct pairs to unordered pairs.
        # Every valid unordered pair {i, j} appears exactly twice in ordered form:
        # once as (i, j) and once as (j, i).
        unordered_pairs: int = ordered_pairs // 2

        return unordered_pairs

    def countCompatiblePairs(self, masks: List[int]) -> int:
        """
        Wrapper method using camelCase naming for convenience.

        Args:
            masks: List of server capability bitmasks.

        Returns:
            The number of unordered compatible pairs.

        Time complexity:
            O(20 * 2^20 + n)

        Space complexity:
            O(2^20)
        """
        return self.count_compatible_pairs(masks)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # masks = [1, 2, 3, 4]
    # Compatible unordered pairs:
    # (1, 2), (1, 4), (2, 4), (3, 4) => total 4
    masks1: List[int] = [1, 2, 3, 4]
    result1: int = solution.countCompatiblePairs(masks1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2:
    # masks = [0, 1, 1, 2, 6]
    # Valid unordered pairs:
    # (0,1 first), (0,1 second), (0,2), (0,6), (1 first,2), (1 second,2) => total 6
    masks2: List[int] = [0, 1, 1, 2, 6]
    result2: int = solution.countCompatiblePairs(masks2)
    print("Example 2 Result:", result2)  # Expected: 6

    # Additional small sanity checks for beginner-friendly verification.

    # Single server: no pair possible.
    masks3: List[int] = [0]
    result3: int = solution.countCompatiblePairs(masks3)
    print("Single Server Result:", result3)  # Expected: 0

    # Two zero masks: they are compatible with each other.
    masks4: List[int] = [0, 0]
    result4: int = solution.countCompatiblePairs(masks4)
    print("Two Zero Masks Result:", result4)  # Expected: 1

    # No compatible pairs.
    masks5: List[int] = [3, 3, 3]
    result5: int = solution.countCompatiblePairs(masks5)
    print("No Compatible Pairs Result:", result5)  # Expected: 0