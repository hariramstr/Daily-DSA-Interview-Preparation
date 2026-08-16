"""
Title: Maximum Visitors Covered by One Billboard Move

Problem Description:
A city avenue is represented by an integer array visitors, where visitors[i] is the
number of pedestrians expected to pass block i during the day. The city has exactly
one advertising billboard that currently occupies a contiguous segment of length k
blocks. A billboard covers every block in its segment, and the total exposure is the
sum of visitors on those covered blocks.

Before the campaign starts, you may relocate the billboard at most once. Relocating
means choosing any other contiguous segment of length k. However, moving the billboard
has a setup cost: the new segment must overlap the original segment in fewer than k
blocks, and every block that is newly covered instead of previously covered counts as
a moved block. You are given an integer m, and the relocation is allowed only if the
number of moved blocks is at most m. If you do not relocate, the moved block count is 0.

Given visitors, the starting left index start of the current billboard, the billboard
length k, and the relocation limit m, return the maximum total exposure achievable.

Two segments of length k with left indices a and b overlap in max(0, k - |a - b|)
blocks, so the number of moved blocks is k - overlap.

Constraints:
- 1 <= visitors.length <= 100000
- 1 <= visitors[i] <= 10000
- 1 <= k <= visitors.length
- 0 <= start <= visitors.length - k
- 0 <= m <= k
"""

from typing import List


class Solution:
    def _compute_window_sums(self, visitors: List[int], k: int) -> List[int]:
        """
        Compute the sum of every contiguous window of length k.

        Args:
            visitors: Array of pedestrian counts per block.
            k: Fixed billboard length.

        Returns:
            A list where result[i] is the sum of visitors[i : i + k].

        Time complexity:
            O(n), where n is len(visitors)

        Space complexity:
            O(n), for storing all window sums
        """
        n: int = len(visitors)
        window_count: int = n - k + 1

        # This list will store the sum for every valid billboard placement.
        # If a billboard starts at index i, it covers visitors[i] through visitors[i + k - 1].
        window_sums: List[int] = [0] * window_count

        # First, compute the sum of the very first window directly.
        current_sum: int = sum(visitors[:k])
        window_sums[0] = current_sum

        # Then slide the window one step at a time.
        # Each move:
        # - removes the element leaving the window
        # - adds the element entering the window
        #
        # This avoids recomputing each window sum from scratch.
        for left in range(1, window_count):
            current_sum -= visitors[left - 1]
            current_sum += visitors[left + k - 1]
            window_sums[left] = current_sum

        return window_sums

    def max_billboard_exposure(self, visitors: List[int], start: int, k: int, m: int) -> int:
        """
        Return the maximum exposure achievable after relocating the billboard at most once.

        The key observation is:
        - If the original billboard starts at `start`
        - And a candidate billboard starts at `new_start`
        - Then the number of moved blocks equals min(k, abs(new_start - start))
          because overlap is max(0, k - abs(new_start - start)), so:
              moved = k - overlap
                    = k - max(0, k - abs(new_start - start))
                    = min(k, abs(new_start - start))
        Therefore, a move is allowed exactly when abs(new_start - start) <= m.

        Args:
            visitors: Array of pedestrian counts per block.
            start: Original left index of the billboard.
            k: Billboard length.
            m: Maximum allowed number of moved blocks.

        Returns:
            The maximum total exposure among all allowed billboard positions.

        Time complexity:
            O(n), where n is len(visitors)

        Space complexity:
            O(n), for storing all length-k window sums
        """
        n: int = len(visitors)

        # Step 1:
        # Precompute the sum of every possible length-k segment.
        #
        # Why do this?
        # Because the billboard can only occupy windows of length k.
        # There are exactly n - k + 1 such windows.
        # Once we know the sum for each window, checking a candidate placement
        # becomes an O(1) lookup instead of summing k elements each time.
        window_sums: List[int] = self._compute_window_sums(visitors, k)

        # Step 2:
        # Determine which destination windows are legal.
        #
        # Let d = abs(new_start - start).
        #
        # For two windows of equal length k:
        # overlap = max(0, k - d)
        # moved_blocks = k - overlap
        #
        # If d < k, then overlap = k - d, so moved_blocks = d.
        # If d >= k, then overlap = 0, so moved_blocks = k.
        #
        # Therefore:
        # moved_blocks = min(k, d)
        #
        # Since m <= k, the condition moved_blocks <= m is equivalent to d <= m.
        #
        # So the billboard may move only to windows whose left index lies in:
        # [start - m, start + m]
        #
        # But we must also stay within valid window indices:
        # 0 through n - k.
        left_bound: int = max(0, start - m)
        right_bound: int = min(n - k, start + m)

        # Step 3:
        # Scan all legal destination windows and keep the maximum exposure.
        #
        # This includes the original position automatically because start is inside
        # [start - m, start + m], and if m == 0 then the range contains only start.
        best_exposure: int = 0

        for new_start in range(left_bound, right_bound + 1):
            # Each candidate is a valid billboard placement and satisfies the move limit.
            # We simply compare its precomputed exposure against the current best.
            best_exposure = max(best_exposure, window_sums[new_start])

        return best_exposure


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    visitors1: List[int] = [5, 1, 3, 8, 2, 6, 4]
    start1: int = 1
    k1: int = 3
    m1: int = 2
    result1: int = solution.max_billboard_exposure(visitors1, start1, k1, m1)
    print(result1)  # Expected: 16

    # Example 2
    visitors2: List[int] = [4, 7, 2, 9, 1, 5]
    start2: int = 2
    k2: int = 2
    m2: int = 0
    result2: int = solution.max_billboard_exposure(visitors2, start2, k2, m2)
    print(result2)  # Expected: 11