"""
Title: Minimum Repairs to Form a Strict Valley Array

Problem Description:
You are given an integer array nums of length n. A strict valley array is an array for
which there exists an index p, where 0 < p < n - 1, such that values strictly decrease
from the left up to p and then strictly increase after p. In other words,

    nums[0] > nums[1] > ... > nums[p] < nums[p+1] < ... < nums[n-1]

The index p is called the valley position.

In one repair operation, you may change any single element to any integer value.
Your task is to return the minimum number of repair operations needed to transform nums
into a strict valley array.

You are not asked to construct the final array, only to compute the minimum number of
elements that must be modified.

A position can remain unchanged only if its original value is compatible with some valid
strict valley configuration. Because changed values may be set arbitrarily, the main
challenge is to keep the largest possible set of original elements while preserving order
and strict inequalities around one valley position.

Constraints:
- 3 <= n <= 200000
- -10^9 <= nums[i] <= 10^9
- The answer must be computed in O(n log n) time or better.
"""

from bisect import bisect_left
from typing import List


class FenwickMax:
    """Fenwick tree supporting prefix maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

        Args:
            size: Number of indices in the tree.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def update(self, index: int, value: int) -> None:
        """
        Set tree positions to the maximum of current value and the given value.

        Args:
            index: 1-based index to update.
            value: Candidate maximum value.

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
        Query the maximum value in prefix [1, index].

        Args:
            index: 1-based inclusive prefix end.

        Returns:
            Maximum value in the prefix.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        result: int = 0
        while index > 0:
            if self.tree[index] > result:
                result = self.tree[index]
            index -= index & -index
        return result


class Solution:
    def _strict_decreasing_end_lengths(self, nums: List[int]) -> List[int]:
        """
        Compute, for every index i, the length of the longest strictly decreasing
        subsequence that ends exactly at i.

        This is done by converting the problem into a longest strictly increasing
        subsequence query on negated values:
            nums[j] > nums[i]  <=>  -nums[j] < -nums[i]

        Args:
            nums: Input array.

        Returns:
            A list left where left[i] is the best decreasing subsequence length
            ending at index i.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        # Coordinate compression is used because values can be as large as 1e9 in magnitude.
        # Fenwick trees need indices in a compact range [1..m], so we sort unique values.
        neg_values: List[int] = [-x for x in nums]
        sorted_unique: List[int] = sorted(set(neg_values))

        # Fenwick tree stores the best LIS length by compressed value.
        fenwick: FenwickMax = FenwickMax(len(sorted_unique))
        result: List[int] = [0] * len(nums)

        for i, value in enumerate(neg_values):
            # We need previous negated values strictly smaller than current negated value.
            # That corresponds to original values strictly greater than nums[i],
            # which is exactly what strict decrease requires.
            rank: int = bisect_left(sorted_unique, value) + 1

            # Query best subsequence among all compressed values < current rank.
            best_before: int = fenwick.query(rank - 1)

            # Extend that subsequence by keeping nums[i].
            current_length: int = best_before + 1
            result[i] = current_length

            # Update the Fenwick tree so future positions can extend from here.
            fenwick.update(rank, current_length)

        return result

    def _strict_increasing_start_lengths(self, nums: List[int]) -> List[int]:
        """
        Compute, for every index i, the length of the longest strictly increasing
        subsequence that starts exactly at i.

        We process from right to left. For each nums[i], we want a future value
        nums[j] > nums[i] so that the sequence can increase after i.

        Args:
            nums: Input array.

        Returns:
            A list right where right[i] is the best increasing subsequence length
            starting at index i.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        # Again compress values for Fenwick tree usage.
        sorted_unique: List[int] = sorted(set(nums))
        fenwick: FenwickMax = FenwickMax(len(sorted_unique))
        n: int = len(nums)
        result: List[int] = [0] * n
        m: int = len(sorted_unique)

        for i in range(n - 1, -1, -1):
            value: int = nums[i]
            rank: int = bisect_left(sorted_unique, value) + 1

            # We need future values strictly greater than nums[i].
            #
            # A Fenwick tree naturally gives prefix maximums, not suffix maximums.
            # To turn "greater than" into a prefix query, we reverse the rank:
            #
            #   reversed_rank = m - rank + 1
            #
            # Larger original values become smaller reversed ranks.
            # Then querying prefix [1 .. reversed_rank - 1] means:
            # "all values strictly greater than current value".
            reversed_rank: int = m - rank + 1

            best_after: int = fenwick.query(reversed_rank - 1)
            current_length: int = best_after + 1
            result[i] = current_length

            fenwick.update(reversed_rank, current_length)

        return result

    def minimumRepairs(self, nums: List[int]) -> int:
        """
        Return the minimum number of element changes needed to make nums a strict valley array.

        Core idea:
        - Any unchanged elements in the final array must form a valley-shaped subsequence:
          strictly decreasing up to some kept valley index p, then strictly increasing after p.
        - If we can keep K original positions unchanged, then all other n - K positions can be
          repaired arbitrarily to fit around them.
        - Therefore, the answer is:
              n - (maximum size of a valid valley-shaped subsequence)

        We compute:
        - left[i]  = longest strictly decreasing subsequence ending at i
        - right[i] = longest strictly increasing subsequence starting at i

        If index i is used as the valley position, then the best valley-shaped subsequence
        through i has size:
              left[i] + right[i] - 1
        because nums[i] is counted in both parts.

        A valid valley must have at least one element on each side, so we require:
        - left[i] >= 2   (some earlier kept element before i)
        - right[i] >= 2  (some later kept element after i)

        Args:
            nums: Input integer array.

        Returns:
            Minimum number of repairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # Compute the best strictly decreasing subsequence ending at each index.
        left: List[int] = self._strict_decreasing_end_lengths(nums)

        # Compute the best strictly increasing subsequence starting at each index.
        right: List[int] = self._strict_increasing_start_lengths(nums)

        # Track the largest number of original positions we can keep unchanged.
        best_valley_keep: int = 0

        # Try every index as the valley position.
        for i in range(1, n - 1):
            # A legal valley position must have at least one kept element on both sides.
            if left[i] >= 2 and right[i] >= 2:
                keep_count: int = left[i] + right[i] - 1
                if keep_count > best_valley_keep:
                    best_valley_keep = keep_count

        # Because n >= 3 and changed values are unrestricted, a valley is always constructible.
        # If no original index can serve as a kept valley with both sides, then we can still
        # keep at least one element and change the rest. The formula below naturally handles that.
        #
        # Example:
        # nums = [1, 2, 3]
        # No valid kept valley with both sides exists, so best_valley_keep = 0.
        # But we can keep one element (say 2) and change the other two -> answer 2.
        #
        # More generally, if no full valley-shaped subsequence of length >= 3 exists,
        # we can always keep exactly one element and repair the remaining n - 1.
        if best_valley_keep == 0:
            return n - 1

        return n - best_valley_keep


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [9, 7, 5, 6, 8],       # Expected: 0
        [4, 4, 3, 2, 5, 5],    # Expected: 2
        [1, 2, 3],             # Expected: 2
        [3, 2, 1],             # Expected: 2
        [5, 1, 4],             # Expected: 0
    ]

    for arr in sample_inputs:
        print(f"nums = {arr}")
        print(f"minimum repairs = {solution.minimumRepairs(arr)}")
        print()