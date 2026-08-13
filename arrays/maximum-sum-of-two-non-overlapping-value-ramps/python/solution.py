"""
Title: Maximum Sum of Two Non-Overlapping Value Ramps

Problem Description:
You are given an integer array nums of length n. A value ramp is a pair of indices
(i, j) such that i < j and nums[i] < nums[j]. The score of that ramp is defined as
nums[j] - nums[i].

You must choose exactly two value ramps, (i1, j1) and (i2, j2), such that their
index intervals do not overlap. In other words, either j1 < i2 or j2 < i1.

Return the maximum possible total score of the two ramps. If it is impossible to
choose two non-overlapping valid ramps, return -1.

Constraints:
- 2 <= n <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- Indices are 0-based
- A ramp requires strict inequality: nums[left] < nums[right]
"""

from bisect import bisect_left
from typing import List


class FenwickMax:
    """Fenwick tree (Binary Indexed Tree) for prefix maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

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
        self.tree: List[int] = [-(10**30)] * (size + 1)

    def update(self, index: int, value: int) -> None:
        """
        Set tree positions so that prefix maximum queries reflect this value.

        Args:
            index: 1-based index to update.
            value: Value to merge using max.

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
        Return the maximum value in the prefix [1..index].

        Args:
            index: 1-based right boundary of the prefix.

        Returns:
            Maximum value in that prefix, or a very negative sentinel if empty.

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
    def _best_ramp_scores_ending_anywhere(self, nums: List[int]) -> List[int]:
        """
        Compute, for every prefix ending at index i, the best single ramp score
        fully contained inside nums[0..i].

        The returned array best_prefix satisfies:
        - best_prefix[i] = maximum score of any valid ramp (l, r) with 0 <= l < r <= i
        - if no valid ramp exists in that prefix, the value is a large negative sentinel

        Args:
            nums: Input array.

        Returns:
            A list of length n containing best prefix ramp scores.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # Coordinate compression:
        # We only care about relative ordering of values, not their exact magnitude.
        # Compression lets us map arbitrary integers (including negatives and very large
        # values) into a compact range [1..m], which is ideal for Fenwick tree usage.
        values: List[int] = sorted(set(nums))
        m: int = len(values)

        # Fenwick tree will store the maximum value of (-nums[left]) among all previously
        # seen indices whose value rank is strictly smaller than the current value rank.
        #
        # Why store -nums[left]?
        # For a fixed right endpoint j, ramp score is:
        #     nums[j] - nums[left]
        # This equals:
        #     nums[j] + (-nums[left])
        # So if we can quickly get the maximum possible (-nums[left]) among valid left
        # endpoints with nums[left] < nums[j], then we can compute the best ramp ending
        # at j in O(log n).
        fenwick: FenwickMax = FenwickMax(m)

        # best_prefix[i] will store the best ramp score anywhere in nums[0..i].
        best_prefix: List[int] = [-(10**30)] * n

        # This variable keeps the running best score seen so far as we scan left to right.
        best_so_far: int = -(10**30)

        for i, value in enumerate(nums):
            # Convert the current value into its compressed rank in [1..m].
            rank: int = bisect_left(values, value) + 1

            # We need strictly smaller values for the left endpoint.
            # Therefore we query ranks [1 .. rank-1].
            best_neg_left: int = fenwick.query(rank - 1)

            # If best_neg_left is still the sentinel, then no earlier value is strictly
            # smaller than nums[i], so no ramp can end at i.
            if best_neg_left > -(10**29):
                current_score: int = value + best_neg_left
                if current_score > best_so_far:
                    best_so_far = current_score

            # Store the best ramp score found in the entire prefix up to i.
            best_prefix[i] = best_so_far

            # Now insert the current index as a possible future left endpoint.
            # We store -value so future right endpoints can compute:
            #     nums[right] + max(-nums[left])
            fenwick.update(rank, -value)

        return best_prefix

    def max_sum_two_non_overlapping_value_ramps(self, nums: List[int]) -> int:
        """
        Return the maximum total score of exactly two non-overlapping value ramps.

        Strategy:
        1. Compute best single-ramp score for every prefix.
        2. Compute best single-ramp score for every suffix by reversing the array and
           reusing the same prefix routine.
        3. Try every split point s between indices s and s+1:
           - first ramp must lie completely in nums[0..s]
           - second ramp must lie completely in nums[s+1..n-1]
           This guarantees non-overlap.
        4. Take the best sum over all splits.

        Args:
            nums: Input integer array.

        Returns:
            Maximum total score of two non-overlapping ramps, or -1 if impossible.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # We need at least 4 indices to form two disjoint ramps, because each ramp uses
        # two distinct indices and the intervals cannot overlap.
        if n < 4:
            return -1

        # ------------------------------------------------------------
        # Step 1: Best single-ramp score in every prefix.
        # ------------------------------------------------------------
        # left_best[i] = best score of one ramp fully inside nums[0..i]
        left_best: List[int] = self._best_ramp_scores_ending_anywhere(nums)

        # ------------------------------------------------------------
        # Step 2: Best single-ramp score in every suffix.
        # ------------------------------------------------------------
        # To compute suffix answers efficiently, reverse the array.
        #
        # A ramp (l, r) in the original suffix corresponds to a ramp in the reversed
        # array as well, because:
        # - original l < r
        # - in reversed positions, these become n-1-r < n-1-l
        # - values remain the same
        #
        # So if we compute prefix best scores on the reversed array, we can map them
        # back into suffix best scores on the original array.
        reversed_nums: List[int] = nums[::-1]
        reversed_prefix_best: List[int] = self._best_ramp_scores_ending_anywhere(reversed_nums)

        # right_best[i] = best score of one ramp fully inside nums[i..n-1]
        right_best: List[int] = [-(10**30)] * n
        for i in range(n):
            # Original suffix starting at i corresponds to reversed prefix ending at
            # index n-1-i.
            right_best[i] = reversed_prefix_best[n - 1 - i]

        # ------------------------------------------------------------
        # Step 3: Try every split between s and s+1.
        # ------------------------------------------------------------
        # If we choose split s, then:
        # - first ramp must be entirely on the left side: indices <= s
        # - second ramp must be entirely on the right side: indices >= s+1
        #
        # This guarantees the two intervals are disjoint, because every index used by
        # the first ramp is <= s and every index used by the second ramp is >= s+1.
        #
        # We only need to consider this orientation because if the optimal pair appears
        # in the opposite order, there still exists a split between the two intervals.
        answer: int = -(10**30)

        for s in range(n - 1):
            left_score: int = left_best[s]
            right_score: int = right_best[s + 1]

            # Both sides must contain a valid ramp.
            if left_score > -(10**29) and right_score > -(10**29):
                total: int = left_score + right_score
                if total > answer:
                    answer = total

        return answer if answer > -(10**29) else -1


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, 1, 7, 2, 9, 3, 8],
        [9, 8, 7, 6, 5, 10],
    ]

    for nums in sample_inputs:
        result: int = solution.max_sum_two_non_overlapping_value_ramps(nums)
        print(f"nums = {nums}")
        print(f"maximum total score of two non-overlapping value ramps = {result}")
        print()

    # Expected:
    # [4, 1, 7, 2, 9, 3, 8] -> 13
    # [9, 8, 7, 6, 5, 10] -> -1