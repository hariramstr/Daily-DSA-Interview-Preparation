"""
Title: Minimum Splits to Form Peak-Valley Value Waves

Problem Description:
You are given an integer array nums representing a long stream of measured values.
You want to partition the array into the minimum number of contiguous segments such
that every segment is a valid value wave.

A segment is considered a valid value wave if, after keeping the elements in their
original order, the differences between consecutive elements strictly alternate in sign.

In other words, for a segment a[l..r], if r - l + 1 >= 3, then for every i in
[l + 1, r - 1], (a[i] - a[i - 1]) and (a[i + 1] - a[i]) must be non-zero and one
must be positive while the other is negative.

Segments of length 1 or 2 are always valid, as long as no adjacent equal values
appear inside the segment. Because equal adjacent values break strict alternation,
any segment containing a pair of consecutive equal values is invalid.

Return the minimum number of contiguous segments needed to partition the entire array
so that every element belongs to exactly one valid segment. If it is impossible,
return -1.

Constraints:
- 1 <= nums.length <= 200000
- -10^9 <= nums[i] <= 10^9
- The answer must be computed using contiguous segments only
"""

from typing import List


class Solution:
    def _sign(self, x: int) -> int:
        """
        Convert a difference into its sign.

        Args:
            x: Integer difference.

        Returns:
            1 if x > 0, -1 if x < 0, 0 if x == 0.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        if x > 0:
            return 1
        if x < 0:
            return -1
        return 0

    def minimum_splits(self, nums: List[int]) -> int:
        """
        Compute the minimum number of contiguous segments such that every segment
        is a valid wave with strictly alternating non-zero consecutive differences.

        Key idea:
        - A segment is invalid immediately if it contains adjacent equal values.
        - Otherwise, a segment is valid exactly when the signs of consecutive
          differences alternate.
        - To minimize the number of segments, we greedily extend the current segment
          as far as possible. The moment adding the next element would break the
          alternation rule, we must cut before that break.

        Why the greedy strategy is correct:
        - If the current segment can be extended without becoming invalid, splitting
          earlier can never reduce the total number of segments.
        - If the next difference has the same sign as the previous difference, then
          any segment containing both of those consecutive differences is invalid.
          Therefore a cut is mandatory between them, and the best place is exactly
          before the newer difference starts.

        Args:
            nums: Input integer array.

        Returns:
            The minimum number of valid contiguous segments, or -1 if impossible.

        Time complexity:
            O(n), where n is the length of nums.

        Space complexity:
            O(1)
        """
        n: int = len(nums)

        # A single element is always a valid segment by itself.
        if n == 1:
            return 1

        # If any adjacent pair is equal, then no valid partition exists.
        #
        # Reason:
        # - Any partition must cover both positions.
        # - Since the elements are adjacent in the original array, they must remain
        #   adjacent inside whichever segment(s) cover them.
        # - They cannot be split apart because segments are contiguous and every
        #   element must belong to exactly one segment.
        # - Therefore an equal adjacent pair makes the whole task impossible.
        for i in range(1, n):
            if nums[i] == nums[i - 1]:
                return -1

        # We already know all adjacent differences are non-zero.
        #
        # We now scan the array from left to right and greedily build the longest
        # possible valid segment.
        #
        # segments:
        #   Number of segments chosen so far.
        #
        # prev_sign:
        #   Sign of the most recent difference inside the current segment.
        #   If the current segment has only one element so far, prev_sign is 0
        #   because there is no difference yet.
        segments: int = 1
        prev_sign: int = 0

        # Start from the second element and process each adjacent difference.
        for i in range(1, n):
            current_diff: int = nums[i] - nums[i - 1]
            current_sign: int = self._sign(current_diff)

            # current_sign can never be 0 here because we already rejected equal
            # adjacent values above. Still, keeping the logic explicit makes the
            # code easier to understand.
            if current_sign == 0:
                return -1

            # If prev_sign == 0, the current segment currently has only one element.
            # Adding this second element always forms a valid length-2 segment.
            if prev_sign == 0:
                prev_sign = current_sign
                continue

            # If the sign alternates, we can safely extend the current segment.
            if current_sign != prev_sign:
                prev_sign = current_sign
                continue

            # Otherwise, we found two consecutive differences with the same sign.
            #
            # Example:
            #   ... a, b, c
            #   diff1 = b - a
            #   diff2 = c - b
            # If sign(diff1) == sign(diff2), then any segment containing a, b, c
            # is invalid because the alternation rule is broken at b.
            #
            # Therefore a cut is mandatory between b and c:
            #   previous segment ends at index i - 1
            #   new segment starts at index i - 1 and includes nums[i]
            #
            # Why does the new segment start at i - 1 instead of i?
            # - Because nums[i - 1], nums[i] form a valid length-2 segment.
            # - Starting at i - 1 gives the new segment the best chance to extend
            #   further and therefore minimizes future cuts.
            segments += 1

            # The new current segment begins with the pair:
            #   nums[i - 1], nums[i]
            # so its first difference has sign current_sign.
            prev_sign = current_sign

        return segments


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [3, 1, 4, 2, 5],      # Expected: 1
        [1, 4, 7, 2, 6, 3],   # Expected: 2
        [5],                  # Expected: 1
        [2, 8],               # Expected: 1
        [1, 1, 2],            # Expected: -1
        [1, 3, 2, 4, 3, 5],   # Expected: 1
        [1, 2, 3, 4],         # Expected: 3 -> [1,2], [2,3], [3,4] is not a partition
                              # actual optimal partition is [1,2], [3,4] => 2
    ]

    # Correct the comment above by actual reasoning:
    # [1,2,3,4] has differences +,+,+
    # One optimal partition is [1,2] and [3,4], so expected result is 2.

    for nums in sample_inputs:
        result = solution.minimum_splits(nums)
        print(f"nums = {nums}")
        print(f"minimum splits = {result}")
        print("-" * 40)