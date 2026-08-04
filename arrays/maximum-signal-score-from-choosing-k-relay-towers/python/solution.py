"""
Title: Maximum Signal Score from Choosing K Relay Towers

Problem Description:
You are given an integer array heights where heights[i] is the elevation of the i-th relay
tower along a straight highway. You must choose exactly k towers, keeping their original
left-to-right order. If the chosen tower indices are i1 < i2 < ... < ik, then the total
signal score is defined as the sum of the minimum height of every adjacent chosen pair:

score = min(heights[i1], heights[i2]) + min(heights[i2], heights[i3]) + ... +
        min(heights[i(k-1)], heights[ik]).

Your task is to return the maximum possible signal score.

This is not the same as choosing a contiguous subarray: you may skip any number of towers
between two chosen towers, but the relative order must remain unchanged. Because each pair
contributes the smaller of the two heights, a locally tall tower may still be a poor choice
if it forces weak pairings elsewhere. The challenge is to optimize globally for exactly k
selections.

Return the maximum score as a 64-bit integer.

Constraints:
- 2 <= heights.length <= 200000
- 1 <= heights[i] <= 1000000000
- 2 <= k <= min(heights.length, 200)
- The answer may exceed 32-bit signed integer range
"""

from typing import List


class FenwickMax:
    """
    Fenwick Tree (Binary Indexed Tree) specialized for prefix maximum queries.

    This structure supports:
    - update(index, value): set tree positions so that prefix maxima reflect the new value
    - query(index): return maximum value in range [1, index]

    We use it after coordinate-compressing heights, because heights can be as large as 1e9.

    Time complexity:
    - update: O(log m)
    - query: O(log m)

    Space complexity:
    - O(m)
    """

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

        Args:
            size: Number of compressed coordinates.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [-(10**30)] * (size + 2)

    def update(self, index: int, value: int) -> None:
        """
        Apply a max-update at one index.

        Args:
            index: 1-based compressed index
            value: Candidate value to merge into the structure

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        while index <= self.size:
            if value > self.tree[index]:
                self.tree[index] = value
            index += index & -index

    def query(self, index: int) -> int:
        """
        Query the maximum value over prefix [1, index].

        Args:
            index: 1-based compressed index

        Returns:
            Maximum value in the prefix

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        result: int = -(10**30)
        while index > 0:
            if self.tree[index] > result:
                result = self.tree[index]
            index -= index & -index
        return result


class Solution:
    def max_signal_score(self, heights: List[int], k: int) -> int:
        """
        Compute the maximum possible signal score when choosing exactly k towers.

        Core idea:
        Let dp_prev[i] be the best score for choosing some fixed number of towers and ending
        at index i as the last chosen tower. To extend that solution by choosing tower i as
        the new last tower, we need:

            dp_cur[i] = max over j < i of (dp_prev[j] + min(heights[j], heights[i]))

        The challenge is evaluating this efficiently for every i.

        Rewrite the transition by splitting previous indices j into two groups:
        1) heights[j] <= heights[i]
           Then min(heights[j], heights[i]) = heights[j]
           Candidate = dp_prev[j] + heights[j]
        2) heights[j] > heights[i]
           Then min(heights[j], heights[i]) = heights[i]
           Candidate = dp_prev[j] + heights[i]

        So for each i we need:
        - maximum of (dp_prev[j] + heights[j]) among previous j with height <= heights[i]
        - maximum of dp_prev[j] among previous j with height > heights[i], then add heights[i]

        We process indices from left to right and maintain two Fenwick trees over compressed
        heights:
        - one tree stores best value of dp_prev[j] + heights[j]
        - another tree stores best value of dp_prev[j], but queried over heights > current
          using a reversed coordinate trick

        Args:
            heights: Tower heights
            k: Exactly how many towers must be chosen

        Returns:
            Maximum signal score as an integer

        Time complexity:
            O(k * n * log n)

        Space complexity:
            O(n)
        """
        n: int = len(heights)

        # Coordinate compression:
        # Heights are large (up to 1e9), but only relative ordering matters for our Fenwick trees.
        # We map each distinct height to a compact rank in [1..m].
        sorted_unique: List[int] = sorted(set(heights))
        m: int = len(sorted_unique)

        # rank_map[h] = compressed rank of height h, starting from 1.
        rank_map = {value: idx + 1 for idx, value in enumerate(sorted_unique)}

        # Precompute compressed ranks for every tower so we do not repeatedly look them up.
        ranks: List[int] = [rank_map[h] for h in heights]

        # Base DP for selecting exactly 1 tower:
        # If we choose only one tower and end at index i, the score is 0 because there are
        # no adjacent chosen pairs yet.
        dp_prev: List[int] = [0] * n

        # We need to perform k-1 extension rounds:
        # round 2 computes best score for choosing exactly 2 towers,
        # round 3 computes best score for choosing exactly 3 towers, etc.
        for chosen_count in range(2, k + 1):
            # Initialize current DP with a very negative number meaning "impossible".
            dp_cur: List[int] = [-(10**30)] * n

            # Fenwick tree #1:
            # Stores maximum of (dp_prev[j] + heights[j]) keyed by height rank of j.
            # Querying prefix up to rank r gives the best previous j with height <= current height.
            bit_leq: FenwickMax = FenwickMax(m)

            # Fenwick tree #2:
            # Stores maximum of dp_prev[j] keyed by reversed rank.
            # Why reversed?
            # We need previous heights > current height, which is a suffix in normal rank order.
            # Fenwick naturally supports prefixes, so we reverse the coordinate:
            # reversed_rank = m - rank + 1
            # Then "height > current" becomes a prefix query in reversed space.
            bit_greater: FenwickMax = FenwickMax(m)

            # Process towers from left to right.
            # This guarantees that every inserted previous state corresponds to j < i,
            # which is required by the subsequence order constraint.
            for i in range(n):
                # A subsequence of length `chosen_count` cannot end before index chosen_count - 1.
                # However, we do not need an explicit boundary check here because impossible states
                # remain very negative and will never win.
                rank_i: int = ranks[i]
                height_i: int = heights[i]

                # Case 1:
                # Previous chosen last tower j has height <= current height.
                # Then contribution of the new pair is heights[j].
                # Candidate = dp_prev[j] + heights[j]
                best_leq: int = bit_leq.query(rank_i)

                # Case 2:
                # Previous chosen last tower j has height > current height.
                # Then contribution of the new pair is current height.
                # We query all previous j with height > height_i.
                reversed_limit: int = m - rank_i
                best_greater_base: int = bit_greater.query(reversed_limit)

                # Combine both possibilities.
                best_value: int = best_leq
                candidate_from_greater: int = best_greater_base + height_i
                if candidate_from_greater > best_value:
                    best_value = candidate_from_greater

                dp_cur[i] = best_value

                # After computing dp_cur[i], we now insert dp_prev[i] into the structures
                # so it can be used by future positions i+1, i+2, ...
                #
                # This ordering is crucial:
                # - compute using only j < i
                # - then insert current index as a future candidate
                if dp_prev[i] > -(10**29):
                    bit_leq.update(rank_i, dp_prev[i] + height_i)
                    reversed_rank_i: int = m - rank_i + 1
                    bit_greater.update(reversed_rank_i, dp_prev[i])

            # Move to the next layer.
            dp_prev = dp_cur

        # The answer is the best score among all subsequences of length exactly k,
        # regardless of where they end.
        return max(dp_prev)


if __name__ == "__main__":
    solution = Solution()

    heights1: List[int] = [5, 1, 4, 6, 3]
    k1: int = 3
    result1: int = solution.max_signal_score(heights1, k1)
    print(result1)  # Expected: 8

    heights2: List[int] = [2, 7, 3, 9, 5, 8]
    k2: int = 4
    result2: int = solution.max_signal_score(heights2, k2)
    print(result2)  # Expected: 17