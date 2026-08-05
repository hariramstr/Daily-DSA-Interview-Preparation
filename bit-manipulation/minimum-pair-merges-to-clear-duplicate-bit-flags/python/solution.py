"""
Title: Minimum Pair Merges to Clear Duplicate Bit Flags

Problem Description:
You are given an integer array flags where each value represents a device configuration mask.
A bit set to 1 means that capability is enabled for that device.

You may repeatedly perform the following operation:
- choose any two different indices i and j such that flags[i] and flags[j] share at least one
  common set bit,
- remove both values,
- insert their bitwise OR, that is, flags[i] | flags[j].

Your goal is to make the final array bit-disjoint, meaning that for every pair of remaining
values a and b, (a & b) == 0.

Return the minimum number of merge operations required.

Important insight:
Two masks can only ever be merged if they are in the same connected component of the graph where:
- each array element is a node
- an edge exists between two nodes if their masks share at least one common set bit

Inside one connected component, it is always possible to keep merging until that component becomes
one final mask. However, that is not always necessary. The true goal is to partition the original
elements into the maximum possible number of groups such that:
- each group induces a connected subgraph (so it can be merged into one mask),
- the final OR masks of different groups are pairwise disjoint.

Then the minimum number of merges is:
    total_elements - number_of_final_groups

A key simplification:
Because values are at most 1e9, there are only up to 30 relevant bit positions.
So we can solve the problem using dynamic programming over subsets of bits.

For any subset of bits S:
- consider only numbers whose set bits are all inside S
- among those numbers, some connected components are fully contained in S
- each such component can become one final group whose OR uses only bits in S

We compute, for every bit subset S, the maximum number of valid final groups that can be formed
using only bits from S. Then the answer is:
    n - dp[all_bits]

Zero values are special:
- 0 has no set bits
- it is already disjoint from everything
- it can never be merged with anything
So every zero always contributes one final group automatically.
"""

from typing import Dict, List, Set, Tuple


class DSU:
    def __init__(self, n: int) -> None:
        """
        Initialize a Disjoint Set Union / Union-Find structure.

        Args:
            n: Number of elements.

        Returns:
            None

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        self.parent: List[int] = list(range(n))
        self.size: List[int] = [1] * n

    def find(self, x: int) -> int:
        """
        Find the representative of x with path compression.

        Args:
            x: Element index.

        Returns:
            Representative/root of x.

        Time complexity:
            Amortized O(alpha(n))

        Space complexity:
            O(1) auxiliary
        """
        while self.parent[x] != x:
            self.parent[x] = self.parent[self.parent[x]]
            x = self.parent[x]
        return x

    def union(self, a: int, b: int) -> None:
        """
        Union the sets containing a and b using union by size.

        Args:
            a: First element index.
            b: Second element index.

        Returns:
            None

        Time complexity:
            Amortized O(alpha(n))

        Space complexity:
            O(1) auxiliary
        """
        ra = self.find(a)
        rb = self.find(b)
        if ra == rb:
            return
        if self.size[ra] < self.size[rb]:
            ra, rb = rb, ra
        self.parent[rb] = ra
        self.size[ra] += self.size[rb]


class Solution:
    def minPairMerges(self, flags: List[int]) -> int:
        """
        Compute the minimum number of allowed merge operations needed so that
        all remaining masks are pairwise bit-disjoint.

        Args:
            flags: List of non-negative integer bitmasks.

        Returns:
            Minimum number of merge operations.

        Time complexity:
            O(n * B + 3^B), where B is the number of relevant bit positions (at most 30,
            and in practice the number of distinct used bits in the input).
            The subset DP dominates and is feasible because B is small.

        Space complexity:
            O(2^B + n + number_of_components)

        Notes:
            - Zero values are handled separately because they cannot merge with anything.
            - The algorithm compresses used bit positions to reduce the DP state size.
        """
        # ------------------------------------------------------------
        # Step 1: Separate zero masks from non-zero masks.
        #
        # Why?
        # - A zero mask has no set bits.
        # - Therefore it does not overlap with any other value.
        # - So it can never participate in a valid merge.
        # - It is already disjoint from everything, including other zeros.
        #
        # This means each zero is automatically one final group and contributes
        # no merge cost.
        # ------------------------------------------------------------
        zero_count: int = 0
        non_zero: List[int] = []
        for value in flags:
            if value == 0:
                zero_count += 1
            else:
                non_zero.append(value)

        # If all values are zero, the array is already pairwise disjoint.
        if not non_zero:
            return 0

        # ------------------------------------------------------------
        # Step 2: Compress the actually used bit positions.
        #
        # Even though values are up to 1e9 (about 30 bits), many test cases use
        # far fewer distinct bit positions. Compressing them makes the subset DP
        # much smaller.
        #
        # Example:
        # If only original bits {0, 3, 7} appear, we remap them to compressed
        # bits {0, 1, 2}.
        # ------------------------------------------------------------
        used_bits: List[int] = []
        seen_bit: Set[int] = set()
        for value in non_zero:
            bit_pos: int = 0
            temp: int = value
            while temp:
                if temp & 1:
                    if bit_pos not in seen_bit:
                        seen_bit.add(bit_pos)
                        used_bits.append(bit_pos)
                temp >>= 1
                bit_pos += 1

        used_bits.sort()
        bit_to_comp: Dict[int, int] = {bit: idx for idx, bit in enumerate(used_bits)}
        b: int = len(used_bits)

        # ------------------------------------------------------------
        # Step 3: Convert every non-zero mask into its compressed-bit version.
        #
        # This keeps the exact overlap structure while reducing the bit universe
        # from up to 30 bits to exactly b used bits.
        # ------------------------------------------------------------
        compressed_masks: List[int] = []
        for value in non_zero:
            compressed: int = 0
            for original_bit in used_bits:
                if value & (1 << original_bit):
                    compressed |= 1 << bit_to_comp[original_bit]
            compressed_masks.append(compressed)

        m: int = len(compressed_masks)

        # ------------------------------------------------------------
        # Step 4: Build connected components among the non-zero masks.
        #
        # Two masks are directly connected if they share a bit.
        # A chain of such overlaps means they belong to the same connected
        # conflict region.
        #
        # We use DSU (Union-Find):
        # - For each compressed bit, remember the first index that contains it.
        # - Any later index containing the same bit must be in the same component,
        #   so we union them.
        #
        # This is much faster than checking all O(n^2) pairs.
        # ------------------------------------------------------------
        dsu: DSU = DSU(m)
        first_index_for_bit: List[int] = [-1] * b

        for idx, mask in enumerate(compressed_masks):
            current: int = mask
            bit_idx: int = 0
            while current:
                if current & 1:
                    if first_index_for_bit[bit_idx] == -1:
                        first_index_for_bit[bit_idx] = idx
                    else:
                        dsu.union(idx, first_index_for_bit[bit_idx])
                current >>= 1
                bit_idx += 1

        # ------------------------------------------------------------
        # Step 5: Gather information for each connected component.
        #
        # For every component we need:
        # - its OR mask over all member values
        # - its size (number of original elements)
        #
        # Why component OR matters:
        # If we decide to keep that whole component as one final group, the final
        # merged mask will be exactly the OR of all values in the component.
        #
        # However, we are NOT forced to keep each connected component whole.
        # We may split a connected component into multiple connected subgroups,
        # as long as the final OR masks of those subgroups are pairwise disjoint.
        #
        # The subset DP below handles that globally.
        # ------------------------------------------------------------
        comp_members: Dict[int, List[int]] = {}
        for idx in range(m):
            root: int = dsu.find(idx)
            if root not in comp_members:
                comp_members[root] = []
            comp_members[root].append(idx)

        # ------------------------------------------------------------
        # Step 6: For every bit subset S, count how many original masks are fully
        # contained in S.
        #
        # "Mask x is fully contained in S" means:
        #     x has no bits outside S
        # equivalently:
        #     (x | S) == S
        #
        # We store frequency by exact mask first, then use SOS DP to compute:
        #     subset_count[S] = number of masks x such that x subset-of S
        #
        # This is a standard Sum Over Subsets DP.
        # ------------------------------------------------------------
        full_mask: int = (1 << b) - 1
        freq_exact: List[int] = [0] * (1 << b)
        for mask in compressed_masks:
            freq_exact[mask] += 1

        subset_count: List[int] = freq_exact[:]
        for bit in range(b):
            for mask in range(1 << b):
                if mask & (1 << bit):
                    subset_count[mask] += subset_count[mask ^ (1 << bit)]

        # ------------------------------------------------------------
        # Step 7: Determine which bit subsets S are "connected-realizable".
        #
        # Meaning:
        # Can all masks whose bits are inside S and that belong to the same chosen
        # group be merged into one final mask using only bits from S?
        #
        # A subset S is realizable as one final group if the induced overlap graph
        # on masks fully contained in S has a connected component whose OR is S.
        #
        # We can characterize this more simply:
        # Starting from bits in S, repeatedly include every mask fully contained in S
        # that touches the current bit set, and OR them in. If the closure reaches
        # exactly S and is connected, then S is a valid final-group mask.
        #
        # Since B is small, we can compute validity for all subsets by checking
        # whether the masks fully contained in S that use bits from S form exactly
        # one connected bit-component covering S.
        #
        # We do this using a bit-closure process over masks grouped by exact mask.
        # ------------------------------------------------------------
        valid_group: List[bool] = [False] * (1 << b)

        # Precompute list of distinct exact masks that appear.
        distinct_masks: List[int] = [mask for mask in range(1 << b) if freq_exact[mask] > 0]

        for s in range(1, 1 << b):
            # Pick any bit in s as a starting point for connectivity expansion.
            start_bit: int = (s & -s).bit_length() - 1
            reached_bits: int = 1 << start_bit

            # Repeatedly absorb any existing mask:
            # - mask must be fully inside s
            # - mask must overlap the currently reached bits
            # If so, after merging through that mask, all its bits become reached.
            changed: bool = True
            while changed:
                changed = False
                for mask in distinct_masks:
                    if (mask | s) != s:
                        continue
                    if mask & reached_bits:
                        new_bits: int = reached_bits | mask
                        if new_bits != reached_bits:
                            reached_bits = new_bits
                            changed = True

            # If we reached all bits of s, then s can be formed as one connected group.
            if reached_bits == s:
                valid_group[s] = True

        # ------------------------------------------------------------
        # Step 8: Dynamic programming over bit subsets.
        #
        # dp[S] = maximum number of final groups we can form using only bits in S,
        #         where the final group masks are pairwise disjoint and each group
        #         is connected-realizable.
        #
        # Transition:
        # - Either do not use some bits yet
        # - Or choose one valid final group T contained in S, then solve the rest
        #   on S \ T
        #
        # So:
        #     dp[S] = max(dp[S without one bit], 1 + dp[S ^ T]) for valid T subset S
        #
        # To keep this manageable, we iterate over submasks T of S.
        #
        # Important:
        # This DP counts only groups whose final OR masks use non-zero bits.
        # Zero masks were already separated and each contributes one extra group.
        # ------------------------------------------------------------
        dp: List[int] = [0] * (1 << b)

        for s in range(1, 1 << b):
            # Baseline option:
            # ignore one chosen bit for now, inheriting the best answer from a smaller subset.
            # This helps propagate values even when s itself is not directly partitionable.
            lowest_bit: int = s & -s
            dp[s] = dp[s ^ lowest_bit]

            # Try every submask t of s as the next final group.
            t: int = s
            while t:
                if valid_group[t]:
                    candidate: int = 1 + dp[s ^ t]
                    if candidate > dp[s]:
                        dp[s] = candidate
                t = (t - 1) & s

        # ------------------------------------------------------------
        # Step 9: Convert "maximum number of final groups" into minimum merges.
        #
        # If we start with n elements and end with g groups, then every merge reduces
        # the number of array elements by exactly 1, so:
        #     merges = n - g
        #
        # Total final groups:
        # - dp[full_mask] groups using non-zero bits
        # - plus zero_count groups from zero values
        #
        # Total original elements:
        # - len(flags)
        # ------------------------------------------------------------
        final_groups: int = dp[full_mask] + zero_count
        return len(flags) - final_groups


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # flags = [3, 5, 8]
    # 3 and 5 overlap, merge -> 7
    # final [7, 8], and 7 & 8 == 0
    # minimum merges = 1
    sample_1: List[int] = [3, 5, 8]
    print(solution.minPairMerges(sample_1))  # Expected: 1

    # Example 2:
    # flags = [10, 3, 12, 1]
    # Better sequence:
    # merge 3 and 1 -> 3
    # merge 10 and 12 -> 14
    # final [3, 14], and 3 & 14 == 0
    # minimum merges = 2
    sample_2: List[int] = [10, 3, 12, 1]
    print(solution.minPairMerges(sample_2))  # Expected: 2

    # Additional quick checks
    print(solution.minPairMerges([0, 0, 0]))      # Expected: 0
    print(solution.minPairMerges([1, 2, 4, 8]))   # Expected: 0
    print(solution.minPairMerges([7, 3, 5]))      # Expected: 2