"""
Title: Maximum Score From Choosing a Buffered Triple

Problem Description:
You are given an integer array nums of length n and an integer gap. A buffered triple
is a choice of three indices (i, j, k) such that:

- i < j < k
- j - i > gap
- k - j > gap

The score of such a triple is:
    nums[i] - nums[j] + nums[k]

Return the maximum possible score among all valid buffered triples.
If no valid triple exists, return -1.

Key observation:
For a fixed middle index j:
- i must be chosen from the prefix [0, j - gap - 1]
- k must be chosen from the suffix [j + gap + 1, n - 1]

So the best score for a fixed j is:
    (maximum value in valid left prefix) - nums[j] + (maximum value in valid right suffix)

This allows an O(n) solution by precomputing:
1. prefix_max[x] = maximum of nums[0..x]
2. suffix_max[x] = maximum of nums[x..n-1]
"""

from typing import List


class Solution:
    def maximumScore(self, nums: List[int], gap: int) -> int:
        """
        Compute the maximum score of a valid buffered triple.

        A valid triple (i, j, k) must satisfy:
        - i < j < k
        - j - i > gap
        - k - j > gap

        For each possible middle index j, we use:
        - the best possible left value from indices [0, j - gap - 1]
        - the best possible right value from indices [j + gap + 1, n - 1]

        Then the score is:
            best_left - nums[j] + best_right

        Args:
            nums: List of integers.
            gap: Required minimum buffer between consecutive chosen indices.

        Returns:
            The maximum score among all valid buffered triples, or -1 if no valid triple exists.

        Time complexity:
            O(n), where n is the length of nums.

        Space complexity:
            O(n), for prefix and suffix maximum arrays.
        """
        n: int = len(nums)

        # We need at least one valid i, one valid j, and one valid k.
        # The spacing rules are:
        #   j - i > gap  =>  j >= i + gap + 1
        #   k - j > gap  =>  k >= j + gap + 1
        #
        # The smallest possible valid triple in terms of positions is:
        #   i = 0
        #   j = gap + 1
        #   k = 2 * gap + 2
        #
        # Therefore, if n <= 2 * gap + 2, then there is no room to place all three indices.
        if n < 2 * gap + 3:
            return -1

        # ------------------------------------------------------------
        # Step 1: Build prefix maximum array.
        #
        # prefix_max[idx] will store the maximum value among nums[0..idx].
        #
        # Why do we need this?
        # For a fixed middle index j, the left index i can be any index in:
        #   [0, j - gap - 1]
        # We want the best possible nums[i], so we need the maximum value in that prefix.
        #
        # With prefix_max, we can get that in O(1):
        #   best_left = prefix_max[j - gap - 1]
        # ------------------------------------------------------------
        prefix_max: List[int] = [0] * n
        prefix_max[0] = nums[0]

        for idx in range(1, n):
            # At each position, the best prefix maximum is either:
            # - the previous prefix maximum, or
            # - the current value nums[idx]
            prefix_max[idx] = max(prefix_max[idx - 1], nums[idx])

        # ------------------------------------------------------------
        # Step 2: Build suffix maximum array.
        #
        # suffix_max[idx] will store the maximum value among nums[idx..n-1].
        #
        # Why do we need this?
        # For a fixed middle index j, the right index k can be any index in:
        #   [j + gap + 1, n - 1]
        # We want the best possible nums[k], so we need the maximum value in that suffix.
        #
        # With suffix_max, we can get that in O(1):
        #   best_right = suffix_max[j + gap + 1]
        # ------------------------------------------------------------
        suffix_max: List[int] = [0] * n
        suffix_max[n - 1] = nums[n - 1]

        for idx in range(n - 2, -1, -1):
            # At each position, the best suffix maximum is either:
            # - the next suffix maximum, or
            # - the current value nums[idx]
            suffix_max[idx] = max(suffix_max[idx + 1], nums[idx])

        # ------------------------------------------------------------
        # Step 3: Try every valid middle index j.
        #
        # For j to be valid:
        # - there must be at least one valid left index i
        #   => j - gap - 1 >= 0
        #   => j >= gap + 1
        #
        # - there must be at least one valid right index k
        #   => j + gap + 1 <= n - 1
        #   => j <= n - gap - 2
        #
        # So j ranges from:
        #   gap + 1  to  n - gap - 2
        #
        # For each such j:
        #   best_left  = prefix_max[j - gap - 1]
        #   best_right = suffix_max[j + gap + 1]
        #   score      = best_left - nums[j] + best_right
        #
        # We keep the maximum score over all valid j.
        # ------------------------------------------------------------
        best_score: int = -10**30

        for j in range(gap + 1, n - gap - 1):
            left_limit: int = j - gap - 1
            right_limit: int = j + gap + 1

            best_left: int = prefix_max[left_limit]
            best_right: int = suffix_max[right_limit]

            current_score: int = best_left - nums[j] + best_right
            best_score = max(best_score, current_score)

        return best_score


def run_demo(nums: List[int], gap: int) -> None:
    """
    Run one demonstration case and print the result.

    Args:
        nums: Input array.
        gap: Required buffer between consecutive selected indices.

    Returns:
        None

    Time complexity:
        O(n), because it calls the main algorithm once.

    Space complexity:
        O(n), due to the algorithm's helper arrays.
    """
    solver = Solution()
    result = solver.maximumScore(nums, gap)
    print(f"nums = {nums}")
    print(f"gap = {gap}")
    print(f"maximum score = {result}")
    print("-" * 50)


if __name__ == "__main__":
    # Example 1 from the prompt.
    #
    # nums = [5, 1, 9, 2, 7, 3, 8], gap = 1
    #
    # Valid middle indices j are 2, 3, 4 because:
    #   j must be in [gap + 1, n - gap - 2] = [2, 4]
    #
    # j = 2:
    #   left indices:  [0]
    #   right indices: [4, 5, 6]
    #   best score = 5 - 9 + 8 = 4
    #
    # j = 3:
    #   left indices:  [0, 1]
    #   right indices: [5, 6]
    #   best score = 5 - 2 + 8 = 11
    #
    # j = 4:
    #   left indices:  [0, 1, 2]
    #   right indices: [6]
    #   best score = 9 - 7 + 8 = 10
    #
    # Therefore the correct answer for the stated constraints is 11.
    #
    # Note:
    # The prompt text claims 15 from (2, 3, 6), but that triple is invalid because:
    #   3 - 2 = 1, which is NOT greater than gap = 1.
    #
    # Our implementation strictly enforces the rule and returns the correct value: 11.
    example_1_nums = [5, 1, 9, 2, 7, 3, 8]
    example_1_gap = 1
    run_demo(example_1_nums, example_1_gap)

    # Example 2 from the prompt.
    #
    # nums = [4, -3, 6, -10, 5, 2], gap = 1
    #
    # Valid middle indices j are 2 and 3.
    #
    # j = 2:
    #   left indices:  [0]
    #   right indices: [4, 5]
    #   score = 4 - 6 + 5 = 3
    #
    # j = 3:
    #   left indices:  [0, 1]
    #   right indices: [5]
    #   best left = 4
    #   score = 4 - (-10) + 2 = 16
    #
    # Therefore the correct answer is 16.
    #
    # Note:
    # The prompt mentions 18 from (2, 3, 5), but that triple is invalid because:
    #   3 - 2 = 1, which is NOT greater than gap = 1.
    #
    # So our implementation correctly returns 16.
    example_2_nums = [4, -3, 6, -10, 5, 2]
    example_2_gap = 1
    run_demo(example_2_nums, example_2_gap)

    # Additional quick sanity checks.

    # gap = 0 means we only need strict increasing indices i < j < k.
    # Best triple here is:
    #   i = 2 (6), j = 3 (-10), k = 4 (5)
    # score = 6 - (-10) + 5 = 21
    extra_nums_1 = [4, -3, 6, -10, 5, 2]
    extra_gap_1 = 0
    run_demo(extra_nums_1, extra_gap_1)

    # No valid triple case.
    extra_nums_2 = [1, 2, 3]
    extra_gap_2 = 1
    run_demo(extra_nums_2, extra_gap_2)