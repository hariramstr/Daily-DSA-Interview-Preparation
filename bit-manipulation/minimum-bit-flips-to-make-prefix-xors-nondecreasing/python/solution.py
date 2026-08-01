"""
Title: Minimum Bit Flips to Make Prefix XORs Nondecreasing

Problem Description:
You are given an array nums of n non-negative integers. You may perform the following
operation any number of times: choose an index i and flip exactly one bit in nums[i]
(changing a 0 bit to 1 or a 1 bit to 0). Each such single-bit change costs 1.

Define the prefix XOR array px where:
    px[i] = nums[0] ^ nums[1] ^ ... ^ nums[i]

Your task is to determine the minimum total number of bit flips needed so that the
prefix XOR array becomes nondecreasing, meaning:
    px[0] <= px[1] <= ... <= px[n - 1]

You are allowed to modify the values in nums before evaluating the prefix XORs.
Return the minimum number of single-bit flips required.

Key idea:
If we let y[i] be the final prefix XOR values, then:
    a'[i] = y[i - 1] ^ y[i]   (with y[-1] treated as 0)
So the total number of bit flips is:
    sum(HammingDistance(nums[i], a'[i]))

We must choose a nondecreasing sequence y[0..n-1] minimizing that total.

Because values are < 2^20, we can solve this with a bitwise digit-DP over all prefixes.
The crucial observation is that when comparing adjacent prefix XOR values, the first bit
(from most significant to least significant) where they differ determines whether the
pair is already strictly increasing or still equal so far. This allows a dynamic program
that processes bits from high to low while tracking, for each adjacent pair, whether:
- it is already known that y[i-1] < y[i], or
- it is still equal on all processed higher bits.

This state can be represented compactly as a bitmask over the n-1 adjacent pairs.
Transitions at each bit are constrained so that for every pair still equal so far,
we cannot choose current bits that would make the left value larger than the right one.
The per-bit cost is independent and comes from whether the chosen prefix-XOR bit changes
the corresponding bit of nums through:
    a'[i]_bit = y[i-1]_bit ^ y[i]_bit
"""

from typing import Dict, List


class Solution:
    def minBitFlips(self, nums: List[int]) -> int:
        """
        Compute the minimum number of single-bit flips needed so that the prefix XOR
        array of the modified numbers becomes nondecreasing.

        Args:
            nums: List of non-negative integers.

        Returns:
            Minimum total number of bit flips.

        Time complexity:
            O(B * 2^(n-1) * T) in the general state-compression formulation, where
            B is the number of bits and T is the number of valid transitions checked.
            This exact algorithm is practical for small-to-moderate n.

        Space complexity:
            O(2^(n-1)) for the DP maps.

        Notes:
            The original prompt states n can be as large as 100000, but an exact
            algorithm for that bound is not feasible for this problem structure.
            This implementation is fully correct for the problem itself and is designed
            around the mathematically exact DP over adjacent-order states.
        """
        n: int = len(nums)
        if n <= 1:
            return 0

        # We only need to process enough bits to cover all values in nums.
        # The prompt says nums[i] < 2^20, so 20 bits are sufficient.
        max_bit: int = 19

        # dp[mask] = minimum cost after processing all bits above the current one.
        #
        # Meaning of "mask":
        # There are n-1 adjacent comparisons:
        #   y[0] <= y[1], y[1] <= y[2], ..., y[n-2] <= y[n-1]
        #
        # For pair j comparing y[j] and y[j+1]:
        #   mask bit j = 1  => from already processed higher bits, we already know
        #                      y[j] < y[j+1], so lower bits are unconstrained.
        #   mask bit j = 0  => they are still equal on all processed higher bits,
        #                      so at the current bit we must avoid left_bit > right_bit.
        #
        # Initially, before processing any bits, all adjacent pairs are equal so far.
        dp: Dict[int, int] = {0: 0}

        # Process bits from most significant to least significant.
        for bit in range(max_bit, -1, -1):
            next_dp: Dict[int, int] = {}

            # Pre-extract the current bit of each original nums[i].
            nums_bit: List[int] = [(x >> bit) & 1 for x in nums]

            # For every previously reachable comparison-state...
            for mask, base_cost in dp.items():
                # We now need to choose the current bit of every prefix XOR value y[i].
                #
                # Let cur[i] be the chosen bit of y[i] at this bit position.
                #
                # The induced bit of the modified array element a'[i] is:
                #   a'[0]_bit = cur[0]
                #   a'[i]_bit = cur[i-1] ^ cur[i]   for i >= 1
                #
                # The cost contribution at this bit is the number of positions where
                # that induced bit differs from nums_bit[i].
                #
                # We must also respect nondecreasing constraints:
                # For every adjacent pair j:
                #   if mask bit j == 1, then y[j] < y[j+1] was already decided earlier,
                #   so cur[j], cur[j+1] can be anything.
                #   if mask bit j == 0, then they are equal so far, so we cannot choose
                #   cur[j] > cur[j+1], because that would make y[j] > y[j+1].
                #
                # We enumerate all valid bit assignments cur[0..n-1] using a small DP
                # over positions, carrying:
                #   - the chosen previous cur bit
                #   - the new comparison mask after this bit
                #
                # pos_dp maps:
                #   key   = (index_processed, prev_cur_bit, new_mask_partial)
                #   value = minimum added cost for processed prefix
                #
                # To keep it simple and beginner-friendly, we instead use a dictionary
                # keyed by (last_bit, new_mask) after processing a prefix of y bits.
                pos_dp: Dict[tuple[int, int], int] = {}

                # Start by choosing cur[0].
                for first_bit in (0, 1):
                    # Cost for a'[0]_bit = cur[0]
                    cost0: int = first_bit ^ nums_bit[0]
                    pos_dp[(first_bit, 0)] = cost0

                # Extend choices for cur[1], cur[2], ..., cur[n-1].
                for i in range(1, n):
                    new_pos_dp: Dict[tuple[int, int], int] = {}
                    pair_index: int = i - 1
                    pair_already_strict: int = (mask >> pair_index) & 1

                    for (left_bit, partial_new_mask), partial_cost in pos_dp.items():
                        for right_bit in (0, 1):
                            # If this adjacent pair is still equal on higher bits,
                            # then at the current bit we must not choose left_bit > right_bit.
                            if pair_already_strict == 0 and left_bit > right_bit:
                                continue

                            # Update the "strictly less already known" status for this pair.
                            updated_new_mask: int = partial_new_mask
                            if pair_already_strict == 1:
                                # Once strict, always strict.
                                updated_new_mask |= (1 << pair_index)
                            else:
                                # It was equal so far.
                                # If left_bit < right_bit now, it becomes strict.
                                # If equal, it remains equal.
                                if left_bit < right_bit:
                                    updated_new_mask |= (1 << pair_index)

                            # The induced modified-array bit at position i is left_bit ^ right_bit.
                            induced_ai_bit: int = left_bit ^ right_bit
                            add_cost: int = induced_ai_bit ^ nums_bit[i]
                            total_partial: int = partial_cost + add_cost

                            key = (right_bit, updated_new_mask)
                            old = new_pos_dp.get(key)
                            if old is None or total_partial < old:
                                new_pos_dp[key] = total_partial

                    pos_dp = new_pos_dp

                # After choosing all cur[i] for this bit, we have a completed new_mask.
                for (_, new_mask), add_cost in pos_dp.items():
                    total_cost: int = base_cost + add_cost
                    old_total = next_dp.get(new_mask)
                    if old_total is None or total_cost < old_total:
                        next_dp[new_mask] = total_cost

            dp = next_dp

        # After all bits are processed, any final mask is acceptable:
        # mask bit 0 means the adjacent values ended up exactly equal,
        # mask bit 1 means strictly increasing.
        return min(dp.values())


if __name__ == "__main__":
    solution = Solution()

    test_cases: List[List[int]] = [
        [3, 1, 2],
        [0, 7, 7],
        [1],
        [0, 0],
        [5, 5],
        [1, 2, 3],
    ]

    for nums in test_cases:
        result = solution.minBitFlips(nums)
        print(f"nums = {nums} -> minimum bit flips = {result}")