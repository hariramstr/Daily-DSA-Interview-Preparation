"""
Title: Maximum Dominance Score of a Split Prefix

Problem Description:
You are given an integer array nums of length n. For any split point i where 0 <= i < n - 1,
divide the array into a left part nums[0..i] and a right part nums[i+1..n-1].

Define the dominance score of this split as:
1. The absolute difference between:
   - the maximum subarray sum entirely contained in the left part
   - the minimum subarray sum entirely contained in the right part

You must also consider the reverse direction:
2. The absolute difference between:
   - the minimum subarray sum entirely contained in the left part
   - the maximum subarray sum entirely contained in the right part

The score of split i is the larger of those two values.
Return the maximum score over all valid split points.

A subarray must be non-empty and contiguous. The maximum and minimum subarray sums are
computed independently within each side of the split; they do not need to touch the split boundary.

Constraints:
- 2 <= n <= 200000
- -1000000000 <= nums[i] <= 1000000000
- The answer fits in a signed 64-bit integer
"""

from typing import List


class Solution:
    def _build_prefix_max_subarray(self, nums: List[int]) -> List[int]:
        """
        Build an array where result[i] is the maximum subarray sum anywhere inside nums[0..i].

        Args:
            nums: Input integer array.

        Returns:
            A list where each position stores the best maximum subarray sum for that prefix.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)
        result: List[int] = [0] * n

        # current_end stores:
        # "What is the maximum subarray sum that MUST end at the current index?"
        #
        # This is the classic Kadane idea:
        # - Either extend the previous subarray
        # - Or start fresh at the current element
        current_end: int = nums[0]

        # result[0] is easy:
        # the only non-empty subarray in prefix [0..0] is [nums[0]]
        result[0] = nums[0]

        for i in range(1, n):
            # For a maximum subarray ending at i:
            # - start new at nums[i]
            # - or extend the previous best ending-at-(i-1)
            current_end = max(nums[i], current_end + nums[i])

            # result[i] should be the best maximum subarray seen anywhere in prefix [0..i]
            # So compare:
            # - the previous prefix best
            # - the best subarray that ends exactly at i
            result[i] = max(result[i - 1], current_end)

        return result

    def _build_prefix_min_subarray(self, nums: List[int]) -> List[int]:
        """
        Build an array where result[i] is the minimum subarray sum anywhere inside nums[0..i].

        Args:
            nums: Input integer array.

        Returns:
            A list where each position stores the best minimum subarray sum for that prefix.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)
        result: List[int] = [0] * n

        # current_end stores:
        # "What is the minimum subarray sum that MUST end at the current index?"
        #
        # This is the minimum-version of Kadane:
        # - Either start new at nums[i]
        # - Or extend the previous minimum-ending subarray
        current_end: int = nums[0]
        result[0] = nums[0]

        for i in range(1, n):
            current_end = min(nums[i], current_end + nums[i])
            result[i] = min(result[i - 1], current_end)

        return result

    def _build_suffix_max_subarray(self, nums: List[int]) -> List[int]:
        """
        Build an array where result[i] is the maximum subarray sum anywhere inside nums[i..n-1].

        Args:
            nums: Input integer array.

        Returns:
            A list where each position stores the best maximum subarray sum for that suffix.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)
        result: List[int] = [0] * n

        # Here we scan from right to left.
        #
        # current_start stores:
        # "What is the maximum subarray sum that MUST start at the current index?"
        #
        # At index i, such a subarray either:
        # - is just nums[i]
        # - or continues into the best starting-at-(i+1) subarray
        current_start: int = nums[n - 1]
        result[n - 1] = nums[n - 1]

        for i in range(n - 2, -1, -1):
            current_start = max(nums[i], nums[i] + current_start)

            # result[i] is the best maximum subarray anywhere in suffix [i..n-1]
            # So compare:
            # - best one starting exactly at i
            # - best one entirely inside suffix [i+1..n-1]
            result[i] = max(result[i + 1], current_start)

        return result

    def _build_suffix_min_subarray(self, nums: List[int]) -> List[int]:
        """
        Build an array where result[i] is the minimum subarray sum anywhere inside nums[i..n-1].

        Args:
            nums: Input integer array.

        Returns:
            A list where each position stores the best minimum subarray sum for that suffix.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)
        result: List[int] = [0] * n

        # Minimum-version while scanning from right to left.
        # current_start means:
        # "minimum subarray sum that must start at index i"
        current_start: int = nums[n - 1]
        result[n - 1] = nums[n - 1]

        for i in range(n - 2, -1, -1):
            current_start = min(nums[i], nums[i] + current_start)
            result[i] = min(result[i + 1], current_start)

        return result

    def max_dominance_score(self, nums: List[int]) -> int:
        """
        Compute the maximum dominance score over all valid split points.

        The algorithm precomputes:
        - prefix maximum subarray sums
        - prefix minimum subarray sums
        - suffix maximum subarray sums
        - suffix minimum subarray sums

        Then for each split i:
        - left side is nums[0..i]
        - right side is nums[i+1..n-1]

        We evaluate both required expressions:
        1) abs(max_subarray_left - min_subarray_right)
        2) abs(min_subarray_left - max_subarray_right)

        The answer is the maximum value across all splits.

        Args:
            nums: Input integer array.

        Returns:
            The maximum dominance score.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # Precompute all four helper arrays.
        #
        # Why do this?
        # Because for every split point, we need instant access to:
        # - best max/min subarray in the left prefix
        # - best max/min subarray in the right suffix
        #
        # Without precomputation, recomputing these values for every split would be too slow.
        prefix_max: List[int] = self._build_prefix_max_subarray(nums)
        prefix_min: List[int] = self._build_prefix_min_subarray(nums)
        suffix_max: List[int] = self._build_suffix_max_subarray(nums)
        suffix_min: List[int] = self._build_suffix_min_subarray(nums)

        best_score: int = 0

        # Try every valid split point.
        # Split after i means:
        # - left  = [0..i]
        # - right = [i+1..n-1]
        #
        # Since both sides must be non-empty, i ranges from 0 to n-2.
        for i in range(n - 1):
            left_max: int = prefix_max[i]
            left_min: int = prefix_min[i]
            right_max: int = suffix_max[i + 1]
            right_min: int = suffix_min[i + 1]

            # First required comparison:
            # maximum subarray on the left vs minimum subarray on the right
            score_one: int = abs(left_max - right_min)

            # Second required comparison:
            # minimum subarray on the left vs maximum subarray on the right
            score_two: int = abs(left_min - right_max)

            # The split's score is the larger of the two.
            split_score: int = max(score_one, score_two)

            # Track the global best.
            best_score = max(best_score, split_score)

        return best_score


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the prompt
    nums1: List[int] = [2, -5, 4, -1, 3]
    result1: int = solution.max_dominance_score(nums1)
    print("Input:", nums1)
    print("Output:", result1)

    # Sample input 2 from the prompt
    nums2: List[int] = [7, -2, -6, 5, -1, 4]
    result2: int = solution.max_dominance_score(nums2)
    print("Input:", nums2)
    print("Output:", result2)

    # Additional quick sanity checks
    nums3: List[int] = [1, 2]
    result3: int = solution.max_dominance_score(nums3)
    print("Input:", nums3)
    print("Output:", result3)

    nums4: List[int] = [-3, -1, -2]
    result4: int = solution.max_dominance_score(nums4)
    print("Input:", nums4)
    print("Output:", result4)