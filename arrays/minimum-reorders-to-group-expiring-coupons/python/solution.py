"""
Title: Minimum Reorders to Group Expiring Coupons

Problem Description:
You are given an integer array coupons where each value represents the expiration day
of a coupon in a checkout system. The coupons are displayed in a fixed row, and you
want all coupons with the same expiration day to appear in one contiguous block.
The relative order of coupons inside a block does not matter, and the blocks
themselves may appear in any order.

In one operation, you may pick any single coupon from its current position and insert
it at any other position in the row. This shifts the surrounding elements as needed.
Return the minimum number of such operations required so that, in the final arrangement,
all equal expiration days are grouped together.

For example, if the row is [3, 1, 3, 2, 1], a valid final arrangement could be
[3, 3, 1, 1, 2] or [2, 1, 1, 3, 3]. Your goal is not to construct the arrangement,
but to compute the fewest insert operations needed.

Constraints:
- 1 <= coupons.length <= 200
- 1 <= coupons[i] <= 20
- The answer fits in a 32-bit integer.

Key Idea:
A coupon can stay in place if, after choosing an order of value-blocks, it already lies
inside the segment reserved for its value. So we try every possible order of distinct
values using dynamic programming over subsets, and maximize how many coupons can remain
where they are. The answer is:

    total_coupons - maximum_number_that_can_stay
"""

from typing import Dict, List


class Solution:
    def min_reorders_to_group_coupons(self, coupons: List[int]) -> int:
        """
        Compute the minimum number of insert operations needed so that all equal
        coupon values become contiguous.

        The method uses:
        1. Value compression for distinct coupon values
        2. Counting how many times each value appears
        3. Prefix sums to quickly count how many coupons of a value already lie
           inside any target segment
        4. Dynamic programming over subsets to choose the best order of blocks

        Args:
            coupons: List of coupon expiration values.

        Returns:
            Minimum number of insert operations required.

        Time complexity:
            O(k * 2^k + n * k), where k is the number of distinct values and
            n is the length of coupons. Since coupon values are in [1, 20],
            k <= 20.

        Space complexity:
            O(k * 2^k + n * k) for the DP and prefix counts.
        """
        # ------------------------------------------------------------
        # Step 1: Handle the trivial case.
        # If there are 0 or 1 coupons, they are already grouped.
        # ------------------------------------------------------------
        n: int = len(coupons)
        if n <= 1:
            return 0

        # ------------------------------------------------------------
        # Step 2: Compress distinct coupon values into indices 0..k-1.
        #
        # Why do this?
        # - The original values can be any integers in the allowed range.
        # - DP over subsets works naturally with small integer indices.
        # - We only care about distinct groups, not the actual numeric values.
        # ------------------------------------------------------------
        distinct_values: List[int] = sorted(set(coupons))
        k: int = len(distinct_values)

        value_to_index: Dict[int, int] = {
            value: idx for idx, value in enumerate(distinct_values)
        }

        # Convert the original coupon values into compressed indices.
        compressed: List[int] = [value_to_index[value] for value in coupons]

        # ------------------------------------------------------------
        # Step 3: Count how many times each value appears.
        #
        # count[i] = total size of the final contiguous block for value i.
        #
        # Example:
        # coupons = [3, 1, 3, 2, 1]
        # distinct values might be [1, 2, 3]
        # compressed could be [2, 0, 2, 1, 0]
        # count = [2, 1, 2]
        # ------------------------------------------------------------
        count: List[int] = [0] * k
        for idx in compressed:
            count[idx] += 1

        # ------------------------------------------------------------
        # Step 4: Build prefix sums for each value.
        #
        # prefix[v][i] = number of occurrences of value v in compressed[0:i]
        #
        # This lets us answer:
        # "How many coupons of value v are already inside interval [l, r)?"
        # in O(1) time as:
        # prefix[v][r] - prefix[v][l]
        #
        # Why is this useful?
        # If we decide that value v's final block occupies positions [l, r),
        # then exactly the coupons of value v already inside that segment can
        # stay in place. The rest of that value's coupons must be moved in.
        # ------------------------------------------------------------
        prefix: List[List[int]] = [[0] * (n + 1) for _ in range(k)]
        for i in range(n):
            current_value_index: int = compressed[i]
            for v in range(k):
                prefix[v][i + 1] = prefix[v][i]
            prefix[current_value_index][i + 1] += 1

        # ------------------------------------------------------------
        # Step 5: Precompute subset sizes.
        #
        # size_sum[mask] = total number of coupons contained in the blocks
        # represented by 'mask'.
        #
        # If mask contains values already placed first in the final arrangement,
        # then size_sum[mask] tells us how many positions are already occupied.
        #
        # This means:
        # - the next block starts at position size_sum[mask]
        # - if next value is v, its block ends at size_sum[mask] + count[v]
        #
        # We compute this efficiently using the lowest set bit trick.
        # ------------------------------------------------------------
        total_masks: int = 1 << k
        size_sum: List[int] = [0] * total_masks
        for mask in range(1, total_masks):
            lowest_bit: int = mask & -mask
            bit_index: int = lowest_bit.bit_length() - 1
            previous_mask: int = mask ^ lowest_bit
            size_sum[mask] = size_sum[previous_mask] + count[bit_index]

        # ------------------------------------------------------------
        # Step 6: Dynamic programming over subsets.
        #
        # dp[mask] = maximum number of coupons that can stay in place after
        # arranging exactly the set of value-blocks in 'mask' as the prefix
        # of the final row.
        #
        # Transition:
        # Suppose we already arranged blocks in 'mask'.
        # Their total occupied length is start = size_sum[mask].
        #
        # Now choose a value v not in mask to place next.
        # Its block will occupy [start, start + count[v]).
        #
        # The number of coupons of value v that can stay in place is exactly
        # how many v's are already inside that interval:
        #
        #   keep = prefix[v][start + count[v]] - prefix[v][start]
        #
        # Then:
        #
        #   dp[mask | (1 << v)] = max(dp[mask | (1 << v)], dp[mask] + keep)
        #
        # At the end, dp[(1 << k) - 1] is the maximum number of coupons that
        # can remain unmoved for the best block order.
        #
        # Why does answer = n - dp[full_mask]?
        # Because every coupon not counted as "kept" must be moved by some
        # insert operation, and this can always be done in exactly that many
        # moves by leaving the kept coupons fixed and reinserting the others.
        # ------------------------------------------------------------
        dp: List[int] = [-10**9] * total_masks
        dp[0] = 0

        for mask in range(total_masks):
            # If this state was never reached, skip it.
            if dp[mask] < 0:
                continue

            # The next block starts immediately after all blocks already chosen.
            start: int = size_sum[mask]

            # Try placing each not-yet-used value as the next block.
            for v in range(k):
                if mask & (1 << v):
                    # This value is already included in the current prefix.
                    continue

                end: int = start + count[v]

                # Count how many coupons of value v are already inside [start, end).
                keep: int = prefix[v][end] - prefix[v][start]

                next_mask: int = mask | (1 << v)
                candidate: int = dp[mask] + keep

                if candidate > dp[next_mask]:
                    dp[next_mask] = candidate

        # ------------------------------------------------------------
        # Step 7: The best possible number of coupons that stay in place is
        # dp[full_mask]. Everything else must be moved.
        # ------------------------------------------------------------
        max_keep: int = dp[total_masks - 1]
        return n - max_keep

    def solve(self, coupons: List[int]) -> int:
        """
        Wrapper method that calls the main algorithm.

        Args:
            coupons: List of coupon expiration values.

        Returns:
            Minimum number of insert operations required.

        Time complexity:
            O(k * 2^k + n * k)

        Space complexity:
            O(k * 2^k + n * k)
        """
        return self.min_reorders_to_group_coupons(coupons)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [3, 1, 3, 2, 1],   # Expected: 2
        [4, 4, 2, 2, 3],   # Expected: 0
        [1],               # Expected: 0
        [1, 2, 1, 2],      # One possible expected: 1
    ]

    for coupons in sample_inputs:
        result: int = solution.solve(coupons)
        print(f"coupons = {coupons} -> minimum reorders = {result}")