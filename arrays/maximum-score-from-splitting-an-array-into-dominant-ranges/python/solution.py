"""
Title: Maximum Score from Splitting an Array into Dominant Ranges

Problem Description:
You are given an integer array nums of length n and an integer k. You must split the
array into exactly k non-empty contiguous subarrays. The score of one subarray is
defined as the frequency of its most common value multiplied by the length of that
subarray. The total score of a split is the sum of the scores of all k chosen subarrays.

Your task is to return the maximum possible total score.

More formally, if a subarray nums[l..r] contains some value x that appears f times,
and no other value appears more than f times, then the subarray contributes
f * (r - l + 1) to the total. If multiple values tie for maximum frequency, the
frequency value is still used only once.

This problem is challenging because the best partition is not determined only by local
choices. A longer segment may increase its length but reduce its dominant frequency,
while a shorter segment may preserve a strong repeated value and produce a better global
answer when combined with later cuts.

Return the maximum total score achievable by partitioning the entire array into exactly
k contiguous parts.

Constraints:
- 1 <= n <= 350
- 1 <= k <= min(n, 50)
- 1 <= nums[i] <= 10^5
- Each split must use all elements of nums exactly once.
"""

from typing import List, Dict


class Solution:
    def _precompute_segment_scores(self, nums: List[int]) -> List[List[int]]:
        """
        Precompute the score of every contiguous subarray nums[l..r].

        For each starting index l, we extend the right boundary r one step at a time.
        While extending, we maintain:
        - frequency of each value inside the current segment
        - the maximum frequency seen so far in that segment

        Then:
            score[l][r] = (segment length) * (maximum frequency in that segment)

        Args:
            nums: Input integer array.

        Returns:
            A 2D table score where score[l][r] is the score of nums[l..r].

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        n: int = len(nums)

        # score[l][r] will store the score of the subarray nums[l..r].
        score: List[List[int]] = [[0] * n for _ in range(n)]

        # We fix the left boundary l, and then move r from l to n - 1.
        # This lets us update frequencies incrementally instead of recomputing
        # them from scratch for every pair (l, r).
        for l in range(n):
            freq: Dict[int, int] = {}
            max_freq: int = 0

            for r in range(l, n):
                value: int = nums[r]
                freq[value] = freq.get(value, 0) + 1

                # Update the dominant frequency of the current segment.
                if freq[value] > max_freq:
                    max_freq = freq[value]

                length: int = r - l + 1
                score[l][r] = max_freq * length

        return score

    def max_score(self, nums: List[int], k: int) -> int:
        """
        Compute the maximum total score by splitting nums into exactly k
        non-empty contiguous subarrays.

        Dynamic programming idea:
        - Let dp[p][i] be the maximum score obtainable by splitting the first i
          elements (nums[0..i-1]) into exactly p non-empty parts.
        - Transition:
              dp[p][i] = max over j from p-1 to i-1 of:
                         dp[p-1][j] + score[j][i-1]
          Here:
          - the first j elements are split into p-1 parts
          - the last part is nums[j..i-1]

        This is a classic partition DP once the score of every segment is known.

        Args:
            nums: Input integer array.
            k: Exact number of contiguous non-empty parts.

        Returns:
            The maximum possible total score.

        Time complexity:
            O(n^2 * k)

        Space complexity:
            O(n^2 + n * k)
        """
        n: int = len(nums)

        # Precompute every segment score once.
        # This avoids repeatedly counting frequencies during DP transitions.
        score: List[List[int]] = self._precompute_segment_scores(nums)

        # Use a very negative number to represent impossible states.
        neg_inf: int = -10**18

        # dp[p][i]:
        # maximum score using exactly p parts to cover the first i elements.
        # i ranges from 0 to n.
        dp: List[List[int]] = [[neg_inf] * (n + 1) for _ in range(k + 1)]

        # Base case:
        # Splitting zero elements into zero parts has score 0.
        dp[0][0] = 0

        # Build the DP table part count by part count.
        for parts in range(1, k + 1):
            # To split first i elements into 'parts' non-empty segments,
            # we need at least i >= parts.
            for i in range(parts, n + 1):
                best: int = neg_inf

                # Try every possible starting position j of the last segment.
                # Then:
                # - first j elements use parts - 1 segments
                # - last segment is nums[j..i-1]
                #
                # j must be at least parts - 1 so that the first j elements can
                # still be split into parts - 1 non-empty segments.
                for j in range(parts - 1, i):
                    if dp[parts - 1][j] == neg_inf:
                        continue

                    candidate: int = dp[parts - 1][j] + score[j][i - 1]
                    if candidate > best:
                        best = candidate

                dp[parts][i] = best

        return dp[k][n]


if __name__ == "__main__":
    solution = Solution()

    nums1: List[int] = [1, 2, 2, 1, 2]
    k1: int = 2
    result1: int = solution.max_score(nums1, k1)
    print(f"nums = {nums1}, k = {k1} -> {result1}")

    nums2: List[int] = [4, 4, 3, 3, 3, 2, 2]
    k2: int = 3
    result2: int = solution.max_score(nums2, k2)
    print(f"nums = {nums2}, k = {k2} -> {result2}")