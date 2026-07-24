"""
Title: Maximum Upgrade Score from One Contiguous Patch Window

Problem Description:
A software team tracks the impact score of each available patch in the order the
patches must be applied. The array `impact` contains positive, negative, or zero
values, where `impact[i]` is the score contributed by the `i`th patch.

The team is allowed to choose exactly one contiguous window of patches to deploy
together. However, deployment overhead depends on the length of the chosen window:
if the window has length `L`, the final score is the sum of all values in that
window minus `L * penalty`.

For every pair of indices `l` and `r` with `0 <= l <= r < n`, define:
score(l, r) = impact[l] + impact[l+1] + ... + impact[r] - (r - l + 1) * penalty

Find the maximum value of score(l, r) over all non-empty contiguous windows.

Constraints:
- 1 <= impact.length <= 200000
- -10^9 <= impact[i] <= 10^9
- 0 <= penalty <= 10^9
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def max_upgrade_score(self, impact: List[int], penalty: int) -> int:
        """
        Compute the maximum possible final score over all non-empty contiguous windows.

        Key idea:
        score(l, r) = sum(impact[l:r+1]) - (length of window) * penalty
                    = sum(impact[i] - penalty for i in l..r)

        So the problem becomes:
        "Find the maximum subarray sum" on the transformed array where
        transformed[i] = impact[i] - penalty.

        We solve that with Kadane's algorithm in O(n) time.

        Args:
            impact: List of patch impact values.
            penalty: Fixed cost paid for each included patch.

        Returns:
            The maximum final score among all non-empty contiguous windows.

        Time complexity:
            O(n), where n is the length of impact.

        Space complexity:
            O(1), excluding input storage.
        """
        # We must choose at least one element, so we initialize using the first element.
        # Instead of building a separate transformed array, we transform each value
        # on the fly as (impact[i] - penalty). This saves memory.
        first_value: int = impact[0] - penalty

        # current_best_ending_here:
        # The maximum transformed subarray sum that MUST end at the current index.
        current_best_ending_here: int = first_value

        # global_best:
        # The best transformed subarray sum seen anywhere so far.
        global_best: int = first_value

        # Process the remaining elements one by one.
        for i in range(1, len(impact)):
            # Transform the current element by subtracting the per-item penalty.
            transformed_value: int = impact[i] - penalty

            # Kadane's algorithm decision:
            # Either:
            # 1) Start a new subarray at this index using only transformed_value
            # 2) Extend the previous best subarray ending at i-1
            #
            # We choose whichever gives a larger sum.
            current_best_ending_here = max(
                transformed_value,
                current_best_ending_here + transformed_value,
            )

            # Update the overall best answer if the current ending subarray is better.
            global_best = max(global_best, current_best_ending_here)

        return global_best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    impact_1: List[int] = [8, -1, 3, -2, 4]
    penalty_1: int = 2
    result_1: int = solution.max_upgrade_score(impact_1, penalty_1)
    print("Example 1 Result:", result_1)  # Expected: 4

    # Example 2
    # The problem statement's explanation contains contradictions,
    # but after direct calculation the correct answer is 4.
    impact_2: List[int] = [-5, 7, -1, 7, -6]
    penalty_2: int = 3
    result_2: int = solution.max_upgrade_score(impact_2, penalty_2)
    print("Example 2 Result:", result_2)  # Expected: 4

    # Additional quick checks
    impact_3: List[int] = [5]
    penalty_3: int = 2
    result_3: int = solution.max_upgrade_score(impact_3, penalty_3)
    print("Single Element Result:", result_3)  # Expected: 3

    impact_4: List[int] = [-10, -20, -3]
    penalty_4: int = 1
    result_4: int = solution.max_upgrade_score(impact_4, penalty_4)
    print("All Negative Result:", result_4)  # Expected: -4