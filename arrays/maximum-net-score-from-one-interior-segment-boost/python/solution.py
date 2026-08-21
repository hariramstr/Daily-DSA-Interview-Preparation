"""
Title: Maximum Net Score from One Interior Segment Boost

Problem Description:
You are given an integer array scores representing daily performance values for a product team.
Positive values help the team's quarterly score, while negative values hurt it.

Management is allowed to apply exactly one temporary boost to a contiguous interior segment of days.
If a segment from index l to r is boosted, where 0 < l <= r < n - 1, then every value inside that
segment contributes twice to the final total, while values outside the segment contribute normally.

Your task is to return the maximum possible final total score after choosing one valid interior
segment to boost.

In other words, if total is the sum of all elements in scores, and seg_sum is the sum of the chosen
segment, then the final score is total + seg_sum. You must choose a segment that does not include
the first or last element of the array.

This problem asks you to optimize over all valid contiguous interior segments. A brute-force
solution that checks every segment will be too slow for large inputs.

Constraints:
- 3 <= scores.length <= 200000
- -100000 <= scores[i] <= 100000
- The chosen boosted segment must satisfy 1 <= l <= r <= n - 2
"""

from typing import List


class Solution:
    def max_net_score(self, scores: List[int]) -> int:
        """
        Compute the maximum final score after boosting exactly one contiguous interior segment.

        The final score equals:
            sum(scores) + best_interior_subarray_sum

        Since the boosted segment must be fully interior, we only search for the maximum-sum
        contiguous subarray inside scores[1 : n - 1].

        Args:
            scores: List of integer daily performance values.

        Returns:
            The maximum possible final total score.

        Time complexity:
            O(n), where n is the length of scores.

        Space complexity:
            O(1), excluding the input array.
        """
        # Step 1:
        # Compute the normal total score with no boost applied.
        # Later, once we know the best interior segment sum, we simply add it to this total.
        total_score: int = sum(scores)

        # Step 2:
        # The boost is only allowed on indices 1 through n - 2 inclusive.
        # That means we must find the maximum-sum contiguous subarray in this interior range.
        #
        # This is a classic "maximum subarray sum" problem, solved efficiently by Kadane's algorithm.
        #
        # Why Kadane's algorithm?
        # - A brute-force approach would try every interior segment, which is O(n^2).
        # - With n up to 200000, O(n^2) is far too slow.
        # - Kadane's algorithm finds the maximum subarray sum in O(n) time.
        #
        # We initialize using the first valid interior element at index 1.
        current_best_ending_here: int = scores[1]
        best_interior_segment_sum: int = scores[1]

        # Step 3:
        # Scan through the remaining interior elements.
        # For each position i, we decide:
        #   A) Start a new segment at i
        #   B) Extend the previous segment that ended at i - 1
        #
        # The recurrence is:
        #   current_best_ending_here = max(scores[i], current_best_ending_here + scores[i])
        #
        # Then we update the global best:
        #   best_interior_segment_sum = max(best_interior_segment_sum, current_best_ending_here)
        #
        # This works because any maximum subarray ending at i either:
        # - consists only of scores[i], or
        # - is formed by extending the best subarray ending at i - 1.
        for i in range(2, len(scores) - 1):
            # Decide whether it is better to:
            # 1. start fresh from scores[i]
            # 2. continue the previous interior segment
            current_best_ending_here = max(scores[i], current_best_ending_here + scores[i])

            # Keep track of the best interior segment sum seen anywhere so far.
            best_interior_segment_sum = max(best_interior_segment_sum, current_best_ending_here)

        # Step 4:
        # The final score after boosting is:
        #   normal total + sum of chosen segment
        #
        # Because boosting doubles the chosen segment, we effectively add that segment sum
        # one extra time on top of the normal total.
        return total_score + best_interior_segment_sum


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt:
    # scores = [4, -2, 3, -1, 5]
    # total = 9
    # interior = [-2, 3, -1]
    # best interior contiguous segment sum = 3 (segment [3])
    # final = 9 + 3 = 12
    sample_1: List[int] = [4, -2, 3, -1, 5]
    result_1: int = solution.max_net_score(sample_1)
    print("Sample 1:", result_1)

    # Sample 2 from the prompt:
    # scores = [7, -5, 4, 6, -2, 8]
    # total = 18
    # interior = [-5, 4, 6, -2]
    # best interior contiguous segment sum = 10 (segment [4, 6])
    # final = 18 + 10 = 28
    sample_2: List[int] = [7, -5, 4, 6, -2, 8]
    result_2: int = solution.max_net_score(sample_2)
    print("Sample 2:", result_2)

    # Additional quick checks:
    # Minimum length case: only one interior element exists.
    sample_3: List[int] = [10, -7, 5]
    result_3: int = solution.max_net_score(sample_3)
    print("Sample 3:", result_3)

    # All interior values negative: must still choose exactly one segment,
    # so choose the least negative interior subarray (typically one element).
    sample_4: List[int] = [3, -4, -2, -9, 6]
    result_4: int = solution.max_net_score(sample_4)
    print("Sample 4:", result_4)