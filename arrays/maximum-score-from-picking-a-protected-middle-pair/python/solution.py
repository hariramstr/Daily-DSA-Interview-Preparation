"""
Title: Maximum Score from Picking a Protected Middle Pair

Problem Description:
You are given an integer array nums representing values placed in a line.
You want to choose two indices i and j such that i < j and there is at least
one element strictly between them.

The score of choosing this pair is:
    nums[i] + nums[j] + min(nums[i+1..j-1])

That means:
- the left endpoint contributes nums[i]
- the right endpoint contributes nums[j]
- the interior segment contributes its minimum value

A pair is valid only if j - i >= 2, because the middle segment must contain
at least one element.

Your task is to return the maximum possible score over all valid pairs.

Constraints:
- 3 <= nums.length <= 200000
- -1000000000 <= nums[i] <= 1000000000

Examples:
1)
Input: nums = [5, 1, 4, 3]
Output: 10
Explanation:
- Choose i = 0, j = 2
- Score = 5 + 4 + min([1]) = 10

2)
Input: nums = [2, 7, -3, 6, 1]
Output: 10
Explanation:
- Choose i = 1, j = 3
- Score = 7 + 6 + min([-3]) = 10
"""

from typing import List


class Solution:
    def maximum_score(self, nums: List[int]) -> int:
        """
        Compute the maximum score over all valid pairs (i, j) with j - i >= 2.

        The key observation is:
        For any chosen pair (i, j), let k be an index inside (i, j) where the
        minimum value of nums[i+1..j-1] is achieved. Then:
            score = nums[i] + nums[j] + nums[k]

        Also, because nums[k] is the minimum inside the interval:
        - every element from i+1 to j-1 must be >= nums[k]
        - in particular, i and j must lie on opposite sides of k inside the
          maximal region around k where all values are >= nums[k]

        Using a monotonic stack, for each index k we find:
        - prev_less[k]: nearest index to the left with value < nums[k]
        - next_less_equal[k]: nearest index to the right with value <= nums[k]

        Then every valid interval whose interior minimum is represented by k
        must satisfy:
            prev_less[k] < i < k < j < next_less_equal[k]

        Since the score for fixed k is:
            nums[k] + nums[i] + nums[j]
        we only need:
        - the maximum nums[i] among indices in [prev_less[k] + 1, k - 1]
        - the maximum nums[j] among indices in [k + 1, next_less_equal[k] - 1]

        We answer those range maximum queries in O(1) using a sparse table.

        Args:
            nums: List of integers.

        Returns:
            The maximum possible score.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n log n)
        """
        n: int = len(nums)

        # ------------------------------------------------------------
        # Step 1: Build boundaries that describe where each value can
        #         serve as the minimum of an interior segment.
        #
        # We use two slightly different comparisons to handle duplicate
        # values cleanly and ensure each interval minimum is assigned in
        # a consistent way:
        #
        # - prev_less[k] is the closest index to the left with value < nums[k]
        # - next_less_equal[k] is the closest index to the right with value <= nums[k]
        #
        # Why this asymmetry?
        # It is a standard trick for "minimum ownership" of intervals.
        # It prevents double counting when equal values appear.
        # ------------------------------------------------------------
        prev_less: List[int] = [-1] * n
        stack: List[int] = []

        # Find previous strictly smaller element for each position.
        for i in range(n):
            # Remove elements that are >= current value because they cannot be
            # the previous strictly smaller element for this index.
            while stack and nums[stack[-1]] >= nums[i]:
                stack.pop()

            prev_less[i] = stack[-1] if stack else -1
            stack.append(i)

        next_less_equal: List[int] = [n] * n
        stack.clear()

        # Find next smaller-or-equal element for each position.
        for i in range(n - 1, -1, -1):
            # Remove elements that are > current value because they cannot be
            # the next <= element for this index.
            while stack and nums[stack[-1]] > nums[i]:
                stack.pop()

            next_less_equal[i] = stack[-1] if stack else n
            stack.append(i)

        # ------------------------------------------------------------
        # Step 2: Build a sparse table for range maximum queries.
        #
        # We need many queries of the form:
        #   max(nums[L..R])
        # and we need them fast.
        #
        # A sparse table preprocesses the array in O(n log n) and answers
        # each max query in O(1).
        # ------------------------------------------------------------
        sparse_table: List[List[int]] = self._build_sparse_table(nums)
        log_table: List[int] = self._build_log_table(n)

        # ------------------------------------------------------------
        # Step 3: Try every index k as the chosen interior minimum.
        #
        # For fixed k:
        # - left endpoint i must be in [prev_less[k] + 1, k - 1]
        # - right endpoint j must be in [k + 1, next_less_equal[k] - 1]
        #
        # These ranges guarantee:
        # - i < k < j, so k is strictly inside the interval
        # - every value between i+1 and j-1 is >= nums[k]
        # - therefore nums[k] can serve as the minimum of the interior
        #
        # To maximize:
        #   nums[i] + nums[j] + nums[k]
        # we simply take the maximum endpoint value on each side.
        #
        # Important:
        # Both sides must be non-empty, otherwise k cannot be inside a valid
        # interval with at least one endpoint on each side.
        # ------------------------------------------------------------
        best: int = -10**30

        for k in range(n):
            left_l: int = prev_less[k] + 1
            left_r: int = k - 1
            right_l: int = k + 1
            right_r: int = next_less_equal[k] - 1

            # If there is no possible left endpoint or no possible right endpoint,
            # then index k cannot be the interior minimum of any valid pair.
            if left_l > left_r or right_l > right_r:
                continue

            left_best: int = self._range_max(
                sparse_table, log_table, left_l, left_r
            )
            right_best: int = self._range_max(
                sparse_table, log_table, right_l, right_r
            )

            score: int = left_best + nums[k] + right_best
            if score > best:
                best = score

        return best

    def _build_log_table(self, n: int) -> List[int]:
        """
        Build floor(log2(x)) values for x from 0 to n.

        Args:
            n: Size of the array.

        Returns:
            A list log_table where log_table[x] = floor(log2(x)) for x >= 1.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        log_table: List[int] = [0] * (n + 1)
        for i in range(2, n + 1):
            log_table[i] = log_table[i // 2] + 1
        return log_table

    def _build_sparse_table(self, nums: List[int]) -> List[List[int]]:
        """
        Build a sparse table for range maximum queries.

        sparse_table[p][i] stores the maximum value in the subarray:
            nums[i : i + 2^p]

        Args:
            nums: Input array.

        Returns:
            Sparse table for range maximum queries.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n log n)
        """
        n: int = len(nums)
        max_power: int = n.bit_length()

        # Level 0 stores intervals of length 1.
        sparse_table: List[List[int]] = [nums[:]]

        power: int = 1
        while (1 << power) <= n:
            interval_len: int = 1 << power
            half_len: int = interval_len >> 1
            prev_level: List[int] = sparse_table[power - 1]

            # For each starting position i, combine two halves:
            # [i, i + 2^(power-1) - 1] and
            # [i + 2^(power-1), i + 2^power - 1]
            current_level: List[int] = [
                max(prev_level[i], prev_level[i + half_len])
                for i in range(n - interval_len + 1)
            ]
            sparse_table.append(current_level)
            power += 1

        return sparse_table

    def _range_max(
        self,
        sparse_table: List[List[int]],
        log_table: List[int],
        left: int,
        right: int,
    ) -> int:
        """
        Query the maximum value in nums[left..right] using the sparse table.

        For a range of length len, choose p = floor(log2(len)).
        Then the range can be covered by two overlapping blocks of length 2^p:
        - one starting at left
        - one ending at right

        Since we are taking maximum, overlapping is harmless.

        Args:
            sparse_table: Precomputed sparse table.
            log_table: Precomputed floor(log2) values.
            left: Left boundary of query, inclusive.
            right: Right boundary of query, inclusive.

        Returns:
            Maximum value in the queried range.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        length: int = right - left + 1
        power: int = log_table[length]
        block_len: int = 1 << power
        return max(
            sparse_table[power][left],
            sparse_table[power][right - block_len + 1],
        )


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [5, 1, 4, 3],
        [2, 7, -3, 6, 1],
        [1, 2, 3],
        [3, 1, 3],
        [-5, -2, -7, -1],
    ]

    for nums in sample_inputs:
        result = solution.maximum_score(nums)
        print(f"nums = {nums}")
        print(f"maximum score = {result}")
        print("-" * 40)