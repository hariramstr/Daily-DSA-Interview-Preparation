"""
Title: Minimum Merges to Form a Mountain Array

Problem Description:
You are given an integer array nums representing daily measurements. In one operation,
you may merge any two adjacent elements into a single element whose value is their sum.
After a merge, the array becomes shorter by one, and the relative order of all remaining
elements stays the same.

Your goal is to transform the array into a mountain array using the minimum number of
merge operations.

An array is considered a mountain array if there exists an index p such that:
- 0 < p < length - 1
- values strictly increase from index 0 to p
- values strictly decrease from index p to length - 1

In other words, the final array must have at least 3 elements and exactly one peak,
with no equal adjacent values in either slope.

Return the minimum number of adjacent merges needed to make nums a mountain array.
If it is impossible, return -1.

A merge can combine already-merged segments again later, so each final element corresponds
to the sum of some contiguous block of the original array.

Constraints:
- 3 <= nums.length <= 200
- 1 <= nums[i] <= 10^6
"""

from typing import List


class Solution:
    def minimumMountainMerges(self, nums: List[int]) -> int:
        """
        Compute the minimum number of adjacent merges needed to transform the array
        into a mountain array.

        The key observation is:
        after any sequence of adjacent merges, the final array is exactly a partition
        of the original array into contiguous blocks, where each final element is the
        sum of one block.

        Therefore, minimizing merges is equivalent to maximizing the number of blocks
        in a valid mountain partition.

        Args:
            nums: Original list of positive integers.

        Returns:
            Minimum number of merges required, or -1 if impossible.

        Time complexity:
            O(n^3), where n is the length of nums.

        Space complexity:
            O(n^2) for prefix sums and dynamic programming tables.
        """
        n: int = len(nums)

        # Prefix sums let us compute the sum of any contiguous subarray in O(1).
        # prefix[i] = sum(nums[0:i])
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + nums[i]

        def range_sum(left: int, right: int) -> int:
            """
            Return the sum of nums[left:right + 1].

            Args:
                left: Left index, inclusive.
                right: Right index, inclusive.

            Returns:
                Sum of the subarray nums[left:right + 1].

            Time complexity:
                O(1)

            Space complexity:
                O(1)
            """
            return prefix[right + 1] - prefix[left]

        # left_dp[end]:
        # For a fixed peak block [peak_l..peak_r], left_dp[end] will store the maximum
        # number of blocks in a strictly increasing partition of nums[0..end], with the
        # additional requirement that the last block sum is strictly less than the peak sum.
        #
        # More precisely, when we compute it for a specific peak sum P:
        # - We partition nums[0..end] into blocks.
        # - Their sums must be strictly increasing.
        # - The final block sum must be < P, so it can connect to the peak.
        #
        # right_dp[start]:
        # Symmetric idea for the suffix nums[start..n-1]:
        # - Partition into blocks with sums strictly decreasing from left to right.
        # - Equivalently, if we build from the right side inward, the block sums must
        #   be strictly increasing when viewed from right to left.
        # - The first block on the right side (adjacent to the peak) must have sum < P.
        #
        # To keep the implementation clear and beginner-friendly, we solve each side
        # using interval DP over all possible previous cut positions.

        best_blocks: int = -1

        # We try every possible peak block.
        # The peak itself can be any contiguous block nums[peak_l..peak_r].
        # This is necessary because merges may combine multiple original elements into
        # the peak value.
        for peak_l in range(n):
            for peak_r in range(peak_l, n):
                peak_sum: int = range_sum(peak_l, peak_r)

                # The final mountain must have at least one block on the left
                # and at least one block on the right.
                if peak_l == 0 or peak_r == n - 1:
                    continue

                # -----------------------------
                # Build the best increasing partition for the left side.
                # -----------------------------
                #
                # We define:
                # inc[last_end][last_start] conceptually as:
                # maximum number of blocks in a strictly increasing partition of
                # nums[0..last_end], where the last block is nums[last_start..last_end],
                # and that last block sum is < peak_sum.
                #
                # Transition:
                # Suppose current last block is [s..e].
                # Let current_sum = sum(s..e).
                #
                # If s == 0:
                #   This is the first and only block so far, valid if current_sum < peak_sum.
                #
                # If s > 0:
                #   We need a previous last block [ps..s-1] such that:
                #   - nums[0..s-1] can be partitioned validly ending with [ps..s-1]
                #   - previous_sum < current_sum
                #
                # Since n <= 200, an O(n^3) style DP is acceptable.
                left_best_end: List[int] = [-1] * peak_l

                # left_state[e][s] = max number of blocks for prefix ending at e
                # with last block [s..e].
                left_state: List[List[int]] = [[-1] * peak_l for _ in range(peak_l)]

                for e in range(peak_l):
                    for s in range(e + 1):
                        current_sum: int = range_sum(s, e)

                        # The block adjacent to the peak must be smaller than the peak.
                        # Since this DP stores all possible last blocks, we enforce
                        # current_sum < peak_sum for every stored state.
                        if current_sum >= peak_sum:
                            continue

                        if s == 0:
                            # Single block covering the entire prefix nums[0..e].
                            left_state[e][s] = 1
                        else:
                            # Try every possible previous block [ps..s-1].
                            best_here: int = -1
                            for ps in range(s):
                                prev_blocks: int = left_state[s - 1][ps]
                                if prev_blocks == -1:
                                    continue

                                prev_sum: int = range_sum(ps, s - 1)

                                # Strictly increasing condition on the left side.
                                if prev_sum < current_sum:
                                    best_here = max(best_here, prev_blocks + 1)

                            left_state[e][s] = best_here

                    # For this prefix ending at e, record the best number of blocks
                    # among all valid choices of the last block.
                    best_for_e: int = -1
                    for s in range(e + 1):
                        best_for_e = max(best_for_e, left_state[e][s])
                    left_best_end[e] = best_for_e

                # If there is no valid left partition ending exactly at peak_l - 1,
                # then this peak cannot form a mountain.
                left_blocks: int = left_best_end[peak_l - 1]
                if left_blocks == -1:
                    continue

                # -----------------------------
                # Build the best decreasing partition for the right side.
                # -----------------------------
                #
                # We do the symmetric DP from right to left.
                #
                # right_state[s][e] conceptually means:
                # maximum number of blocks in a strictly decreasing partition of
                # nums[s..n-1], where the first block is nums[s..e],
                # and that first block sum is < peak_sum.
                #
                # Transition:
                # Suppose current first block is [s..e].
                # Let current_sum = sum(s..e).
                #
                # If e == n - 1:
                #   This is the only block in the suffix, valid if current_sum < peak_sum.
                #
                # If e < n - 1:
                #   We need a next first block [e+1..ne] in the remaining suffix such that:
                #   - nums[e+1..n-1] can be partitioned validly starting with [e+1..ne]
                #   - current_sum > next_sum
                #
                # This enforces strict decrease from left to right.
                suffix_len: int = n - (peak_r + 1)
                right_best_start: List[int] = [-1] * suffix_len

                # We index the actual array directly for clarity.
                right_state: List[List[int]] = [[-1] * n for _ in range(n)]

                for s in range(n - 1, peak_r, -1):
                    for e in range(s, n):
                        current_sum = range_sum(s, e)

                        # The block adjacent to the peak must be smaller than the peak.
                        if current_sum >= peak_sum:
                            continue

                        if e == n - 1:
                            # Single block covering the entire suffix nums[s..n-1].
                            right_state[s][e] = 1
                        else:
                            best_here = -1
                            for ne in range(e + 1, n):
                                next_blocks: int = right_state[e + 1][ne]
                                if next_blocks == -1:
                                    continue

                                next_sum: int = range_sum(e + 1, ne)

                                # Strictly decreasing from left to right means:
                                # current block sum > next block sum.
                                if current_sum > next_sum:
                                    best_here = max(best_here, next_blocks + 1)

                            right_state[s][e] = best_here

                    # Record the best partition starting exactly at s.
                    best_for_s: int = -1
                    for e in range(s, n):
                        best_for_s = max(best_for_s, right_state[s][e])

                    right_best_start[s - (peak_r + 1)] = best_for_s

                right_blocks: int = right_best_start[0]
                if right_blocks == -1:
                    continue

                # Total number of final blocks in the mountain partition:
                # left side blocks + 1 peak block + right side blocks.
                total_blocks: int = left_blocks + 1 + right_blocks
                best_blocks = max(best_blocks, total_blocks)

        # If no valid mountain partition exists, return -1.
        if best_blocks == -1:
            return -1

        # Starting from n elements, ending with k blocks requires exactly n - k merges.
        return n - best_blocks


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [1, 2, 1],
        [2, 1, 1, 2],
        [1, 3, 2, 1],
        [1, 1, 1],
        [1, 2, 3, 2, 1],
    ]

    for arr in sample_inputs:
        result = solution.minimumMountainMerges(arr)
        print(f"nums = {arr} -> minimum merges = {result}")