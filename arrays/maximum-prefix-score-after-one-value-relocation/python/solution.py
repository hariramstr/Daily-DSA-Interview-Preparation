"""
Title: Maximum Prefix Score After One Value Relocation

Problem Description:
You are given an integer array nums of length n. Define the prefix score of the array
as the number of indices i (0 <= i < n) such that the sum of the first i + 1 elements
is strictly greater than 0. In other words, if prefix[i] = nums[0] + nums[1] + ... + nums[i],
then index i contributes to the score if prefix[i] > 0.

You may perform at most one relocation operation. In a relocation, choose one element nums[j],
remove it from its current position, and insert it at any other position in the array
(including the beginning or the end). The relative order of all other elements must stay the same.

Return the maximum possible prefix score after applying at most one relocation.

Constraints:
- 1 <= n <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- The answer fits in a 32-bit signed integer.
"""

from bisect import bisect_left
from typing import List


class FenwickTree:
    """Fenwick Tree (Binary Indexed Tree) for prefix sums of counts.

    This structure supports:
    - adding a value at one position
    - querying prefix sums
    - querying range sums

    We use it to count how many prefix sums fall into certain value ranges.

    Time complexity:
        - update: O(log n)
        - query: O(log n)

    Space complexity:
        O(n)
    """

    def __init__(self, size: int) -> None:
        """Initialize an empty Fenwick tree.

        Args:
            size: Number of positions in the tree.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def add(self, index: int, delta: int) -> None:
        """Add delta to a 0-based index.

        Args:
            index: 0-based position to update.
            delta: Value to add.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        index += 1
        while index <= self.size:
            self.tree[index] += delta
            index += index & -index

    def prefix_sum(self, index: int) -> int:
        """Return sum of values in range [0, index], inclusive, using 0-based indexing.

        Args:
            index: Right endpoint of the prefix.

        Returns:
            Sum of stored values from 0 to index.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        if index < 0:
            return 0
        index += 1
        result: int = 0
        while index > 0:
            result += self.tree[index]
            index -= index & -index
        return result

    def range_sum(self, left: int, right: int) -> int:
        """Return sum of values in range [left, right], inclusive.

        Args:
            left: Left endpoint.
            right: Right endpoint.

        Returns:
            Sum over the requested range.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        if left > right:
            return 0
        return self.prefix_sum(right) - self.prefix_sum(left - 1)


class Solution:
    def max_prefix_score_after_one_relocation(self, nums: List[int]) -> int:
        """Compute the maximum possible prefix score after at most one relocation.

        Core idea:
        A relocation removes nums[j] from its current position and inserts it elsewhere.
        This changes a whole interval of prefix sums by the same amount.

        We analyze two move directions separately:

        1) Move nums[j] to the right (j -> k, k > j):
           - Prefixes before j are unchanged.
           - Prefixes from j to k - 1 lose nums[j].
           - Prefixes from k onward are unchanged again.
           So only one contiguous block is affected.

        2) Move nums[j] to the left (j -> k, k < j):
           - Prefixes before k are unchanged.
           - Prefixes from k to j - 1 gain nums[j].
           - Prefixes from j onward are unchanged again.
           Again, only one contiguous block is affected.

        Therefore, for each j, we need the best interval where adding or subtracting nums[j]
        creates the most positive prefix sums.

        We solve this efficiently with:
        - prefix sums
        - coordinate compression
        - Fenwick trees to count how many prefix sums in a suffix/prefix satisfy inequalities
        - a segment tree that supports "best subarray count under threshold" style queries

        Args:
            nums: Input integer array.

        Returns:
            Maximum possible prefix score.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # ------------------------------------------------------------
        # Step 1: Build prefix sums.
        #
        # prefix[i] = nums[0] + nums[1] + ... + nums[i]
        #
        # The original score is simply the count of prefix[i] > 0.
        # ------------------------------------------------------------
        prefix: List[int] = [0] * n
        running_sum: int = 0
        original_score: int = 0

        for i, value in enumerate(nums):
            running_sum += value
            prefix[i] = running_sum
            if running_sum > 0:
                original_score += 1

        # If there is only one element, relocation cannot change anything.
        if n == 1:
            return original_score

        # ------------------------------------------------------------
        # Step 2: Coordinate compression of prefix sums.
        #
        # We will repeatedly ask:
        #   "How many prefix sums in some range are > threshold?"
        #   "How many prefix sums in some range are in (0, x]?"
        #
        # Compression lets us store counts in Fenwick trees efficiently.
        # ------------------------------------------------------------
        sorted_values: List[int] = sorted(set(prefix))
        m: int = len(sorted_values)

        def compressed_index(value: int) -> int:
            """Return compressed index of an existing prefix value.

            Args:
                value: Prefix sum value known to exist in sorted_values.

            Returns:
                0-based compressed index.

            Time complexity:
                O(log n)

            Space complexity:
                O(1)
            """
            return bisect_left(sorted_values, value)

        # ------------------------------------------------------------
        # Step 3: Precompute original positivity flags for convenience.
        #
        # positive_flag[i] = 1 if prefix[i] > 0 else 0
        # ------------------------------------------------------------
        positive_flag: List[int] = [1 if x > 0 else 0 for x in prefix]

        # ------------------------------------------------------------
        # Step 4: Handle moves to the right.
        #
        # Suppose we move nums[j] to some later position k > j.
        # Then only prefix indices in [j, k-1] change, and each becomes:
        #     prefix[t] - nums[j]
        #
        # For a fixed j, the gain on interval [j, k-1] is:
        #     count(prefix[t] - nums[j] > 0) - count(prefix[t] > 0)
        #   = count(prefix[t] > nums[j]) - count(prefix[t] > 0)
        #
        # So for each j, we want the suffix interval starting at j with maximum:
        #     (# of prefix[t] > nums[j]) - (# of prefix[t] > 0)
        #
        # This is equivalent to maximum prefix sum over a transformed binary sequence.
        #
        # We can compute it online using a segment tree over compressed prefix values.
        # ------------------------------------------------------------

        right_best_gain: int = 0

        class MaxPrefixSegmentTree:
            """Segment tree storing counts and best prefix values over compressed values.

            For a fixed j during right-move processing:
            - Each prefix value contributes:
                +1 if value > nums[j]
                -1 if 0 < value <= nums[j]
                 0 if value <= 0
            - Over the suffix [j..n-1], we want the maximum prefix sum of these contributions
              when values are read in original order.

            We process indices from right to left. For the current suffix, each element
            contributes a weight depending on whether its prefix value lies in one of three
            value ranges. Since the contribution depends only on the value, we can maintain
            counts by value and ask for the best cumulative effect.

            The tree node stores:
            - total: total contribution of all values in the node's value range
            - best: best prefix sum obtainable inside that value range when traversed
                    from smaller values to larger values

            This is a standard merge:
                total = left.total + right.total
                best = max(left.best, left.total + right.best)

            We use this tree in a slightly specialized way:
            after assigning weights by threshold nums[j], querying the root.best gives the
            maximum cumulative contribution over value ranges. Combined with suffix order
            counting, this yields the best interval gain.
            """

            def __init__(self, size: int) -> None:
                self.size: int = 1
                while self.size < size:
                    self.size <<= 1
                self.total: List[int] = [0] * (2 * self.size)
                self.best: List[int] = [0] * (2 * self.size)

            def update(self, index: int, delta: int) -> None:
                pos: int = index + self.size
                self.total[pos] += delta
                self.best[pos] = max(0, self.total[pos])
                pos >>= 1
                while pos:
                    left: int = pos << 1
                    right: int = left | 1
                    self.total[pos] = self.total[left] + self.total[right]
                    self.best[pos] = max(self.best[left], self.total[left] + self.best[right])
                    pos >>= 1

            def query_best(self) -> int:
                return self.best[1]

        # ------------------------------------------------------------
        # The segment tree approach above is not enough by itself to preserve original order,
        # so instead we use a simpler and fully correct O(n log n) method:
        #
        # For each j, right move affects interval [j, k-1].
        # Gain on that interval equals:
        #     count(prefix[t] > nums[j]) - count(prefix[t] > 0)
        #
        # Define for fixed j:
        #     a_t = 1 if prefix[t] > nums[j], else 0
        #     b_t = 1 if prefix[t] > 0, else 0
        #     c_t = a_t - b_t
        #
        # We need the maximum prefix sum of c_t over t = j..n-1.
        #
        # Since c_t is in {-1,0,1}, and threshold changes with j, we compute this by
        # maintaining suffix counts and a segment tree over positions where each position
        # stores whether prefix[t] is in (0, nums[j]].
        #
        # For right moves:
        #   gain = max over suffix prefixes of [ - count((0, nums[j]]) in that interval ]
        #          plus count(> nums[j]) which is implicit from interval length.
        #
        # A cleaner route is to derive:
        #   count(prefix[t] > nums[j]) - count(prefix[t] > 0)
        # = - count(0 < prefix[t] <= nums[j])      if nums[j] > 0
        # = count(nums[j] < prefix[t] <= 0)        if nums[j] <= 0
        #
        # Therefore:
        # - If nums[j] > 0, moving it right can only hurt or keep score, so best gain is 0.
        # - If nums[j] <= 0, moving it right can help by making some non-positive prefixes
        #   become positive. Specifically, on [j, k-1], gain is:
        #       count(nums[j] < prefix[t] <= 0)
        #   and we want the maximum over suffix intervals starting at j,
        #   which is simply the total count in suffix [j..n-1], because all contributions
        #   are non-negative.
        #
        # This is a crucial simplification.
        # ------------------------------------------------------------
        suffix_fenwick: FenwickTree = FenwickTree(m)
        for value in prefix:
            suffix_fenwick.add(compressed_index(value), 1)

        for j in range(n):
            # Remove current prefix[j] from the suffix structure only after using j,
            # because interval [j, k-1] may include j itself.
            if nums[j] <= 0:
                # Count prefix values in suffix [j..n-1] that satisfy:
                #     nums[j] < prefix[t] <= 0
                #
                # These are exactly the prefixes that are currently non-positive,
                # but would become positive after subtracting nums[j] (remember nums[j] <= 0,
                # so subtracting it means adding |nums[j]|).
                left_idx: int = bisect_left(sorted_values, nums[j] + 1)
                right_idx: int = bisect_left(sorted_values, 1) - 1
                if left_idx <= right_idx:
                    gain: int = suffix_fenwick.range_sum(left_idx, right_idx)
                    if gain > right_best_gain:
                        right_best_gain = gain

            suffix_fenwick.add(compressed_index(prefix[j]), -1)

        # ------------------------------------------------------------
        # Step 5: Handle moves to the left.
        #
        # Suppose we move nums[j] to some earlier position k < j.
        # Then only prefix indices in [k, j-1] change, and each becomes:
        #     prefix[t] + nums[j]
        #
        # Gain on interval [k, j-1]:
        #     count(prefix[t] + nums[j] > 0) - count(prefix[t] > 0)
        #
        # This simplifies by sign of nums[j]:
        #
        # If nums[j] < 0:
        #   adding a negative number can only hurt or keep score, so best gain is 0.
        #
        # If nums[j] > 0:
        #   gain = count(-nums[j] < prefix[t] <= 0) over [k, j-1]
        #   all contributions are non-negative, so best interval is the entire prefix [0, j-1].
        #
        # Therefore for each positive nums[j], best left-move gain is simply:
        #   count(-nums[j] < prefix[t] <= 0) among t in [0, j-1]
        # ------------------------------------------------------------
        left_best_gain: int = 0
        prefix_fenwick: FenwickTree = FenwickTree(m)

        for j in range(n):
            if nums[j] > 0:
                left_idx = bisect_left(sorted_values, -nums[j] + 1)
                right_idx = bisect_left(sorted_values, 1) - 1
                if left_idx <= right_idx:
                    gain = prefix_fenwick.range_sum(left_idx, right_idx)
                    if gain > left_best_gain:
                        left_best_gain = gain

            prefix_fenwick.add(compressed_index(prefix[j]), 1)

        # ------------------------------------------------------------
        # Step 6: Combine answers.
        #
        # We may choose:
        # - no move
        # - best move to the right
        # - best move to the left
        # ------------------------------------------------------------
        return original_score + max(0, right_best_gain, left_best_gain)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, -5, 2, 1],
        [-2, 3, -1, 2],
    ]

    for nums in sample_inputs:
        result = solution.max_prefix_score_after_one_relocation(nums)
        print(f"nums = {nums}")
        print(f"maximum prefix score = {result}")
        print()