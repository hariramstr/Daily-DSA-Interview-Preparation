"""
Title: Maximum Consecutive Shelves After One Label Fix

Problem Description:
A warehouse stores boxes on shelves labeled with positive integers. Ideally, the labels
should form a consecutive run such as [7, 8, 9, 10]. However, one shelf label may have
been printed incorrectly.

You are given an integer array labels, where labels[i] is the label currently printed on
the i-th shelf in order from left to right. You may change the value of at most one
element in the array to any positive integer you want.

Return the maximum possible length of a contiguous subarray that can be made strictly
consecutive with step 1 after performing at most one change. A subarray is valid if,
after the optional change, its values can become [x, x+1, x+2, ..., x+len-1] for some
positive integer x, while keeping the same order of positions.

In other words, within the chosen contiguous segment, every adjacent difference must be
exactly 1 after changing at most one element in that segment.

Your task is to compute the longest such segment.

Constraints:
- 1 <= labels.length <= 200000
- 1 <= labels[i] <= 1000000000
- You may modify at most one element in the chosen subarray
- The replacement value must be a positive integer

Examples:
1) labels = [4, 5, 9, 7, 8]
   Output: 5
   Explanation: Change the third value 9 to 6. The entire array becomes [4, 5, 6, 7, 8].

2) labels = [10, 11, 13, 14, 20]
   Output: 4
   Explanation: The subarray [10, 11, 13, 14] can be fixed by changing 13 to 12, giving
   [10, 11, 12, 13]. No length-5 segment can be made consecutive with only one change.
"""

from typing import List


class Solution:
    def _build_good_runs(self, labels: List[int]) -> (List[int], List[int]):
        """
        Build helper arrays describing stretches where adjacent differences are already 1.

        For each index:
        - left_good[i]  = length of the longest suffix ending at i whose adjacent
                          differences are all exactly 1
        - right_good[i] = length of the longest prefix starting at i whose adjacent
                          differences are all exactly 1

        Args:
            labels: Input array of shelf labels.

        Returns:
            A tuple (left_good, right_good).

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(labels)
        left_good: List[int] = [1] * n
        right_good: List[int] = [1] * n

        # Build left_good:
        # If labels[i] - labels[i - 1] == 1, then the consecutive-by-1 run ending at i
        # can extend the run ending at i - 1.
        for i in range(1, n):
            if labels[i] - labels[i - 1] == 1:
                left_good[i] = left_good[i - 1] + 1

        # Build right_good:
        # If labels[i + 1] - labels[i] == 1, then the consecutive-by-1 run starting at i
        # can extend the run starting at i + 1.
        for i in range(n - 2, -1, -1):
            if labels[i + 1] - labels[i] == 1:
                right_good[i] = right_good[i + 1] + 1

        return left_good, right_good

    def max_consecutive_shelves_after_one_fix(self, labels: List[int]) -> int:
        """
        Compute the maximum length of a contiguous subarray that can become strictly
        consecutive with step 1 after changing at most one element.

        Core idea:
        A segment can be fixed with at most one change if all adjacent differences are
        already 1 except possibly around one chosen position. We precompute maximal
        already-good runs and then try each index as the changed element.

        For a chosen index i:
        - The left side [L .. i-1] must already be consecutive with step 1.
        - The right side [i+1 .. R] must already be consecutive with step 1.
        - We must be able to choose one replacement value for labels[i] that connects
          both sides (if both sides exist), or simply extends one side (if only one side
          exists), while staying positive.

        Args:
            labels: Input array of shelf labels.

        Returns:
            The maximum valid subarray length.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(labels)

        # A single element is always valid:
        # we can keep it as-is, or change it to any positive integer.
        if n == 1:
            return 1

        # Precompute lengths of already-perfect consecutive runs.
        left_good, right_good = self._build_good_runs(labels)

        # At minimum, any already-good run is valid without using any change.
        # We initialize the answer with the best such run.
        answer: int = max(left_good)

        # Now we try every index as the one element we may change.
        #
        # Why this works:
        # If a segment can be fixed by changing at most one element, then there is some
        # index i inside that segment which is the changed position (or no change is
        # needed, already covered by answer = max(left_good)).
        #
        # Once i is chosen:
        # - The part strictly to the left of i must already have adjacent difference 1.
        # - The part strictly to the right of i must already have adjacent difference 1.
        #
        # Therefore, the maximal candidate around i is formed by:
        # - taking the longest valid run ending at i - 1
        # - taking the longest valid run starting at i + 1
        # - checking how much of those can be joined through one replacement at i
        for i in range(n):
            # Length of the already-good run immediately to the left of i.
            left_len: int = left_good[i - 1] if i > 0 else 0

            # Length of the already-good run immediately to the right of i.
            right_len: int = right_good[i + 1] if i + 1 < n else 0

            # Candidate 1: use only the left side plus the changed element.
            #
            # If we include a left run ending at i - 1, then to keep the whole segment
            # consecutive, the changed value at i must be labels[i - 1] + 1.
            # That is always a positive integer because labels are positive.
            answer = max(answer, left_len + 1)

            # Candidate 2: use only the right side plus the changed element.
            #
            # If we include a right run starting at i + 1, then the changed value at i
            # must be labels[i + 1] - 1.
            #
            # This is valid only if that replacement is positive.
            if i + 1 < n and labels[i + 1] > 1:
                answer = max(answer, right_len + 1)

            # Candidate 3: bridge both sides through index i.
            #
            # Suppose we want:
            #   ... , labels[i - 1], new_value, labels[i + 1], ...
            # with adjacent differences exactly 1.
            #
            # Then we need:
            #   new_value = labels[i - 1] + 1
            #   labels[i + 1] = new_value + 1
            #
            # Combining them:
            #   labels[i + 1] - labels[i - 1] = 2
            #
            # This condition is both necessary and sufficient.
            #
            # Also, new_value = labels[i - 1] + 1 is positive automatically.
            if i > 0 and i + 1 < n and labels[i + 1] - labels[i - 1] == 2:
                answer = max(answer, left_len + 1 + right_len)

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, 5, 9, 7, 8],       # Expected: 5
        [10, 11, 13, 14, 20],  # Expected: 4
        [1],                   # Expected: 1
        [5, 6, 7, 8],          # Expected: 4
        [3, 10, 5],            # Expected: 2
        [2, 3, 5, 6],          # Expected: 4 (change 5 -> 4)
    ]

    for labels in sample_inputs:
        result = solution.max_consecutive_shelves_after_one_fix(labels)
        print(f"labels = {labels}")
        print(f"maximum fixable consecutive subarray length = {result}")
        print("-" * 60)