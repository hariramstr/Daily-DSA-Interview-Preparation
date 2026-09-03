"""
Title: Minimum Review Threshold for Passing All Build Gates

Problem Description:
You are given a software release pipeline with n sequential build gates. Gate i
requires at least requirements[i] approved review points before the release can
pass that gate. You also have m review batches, where batch j contributes
reviews[j] points and can be split across multiple gates in any way.

However, to keep the process fair, you must choose a single threshold value T
and cap every batch at min(reviews[j], T) usable points. Any points above T in a
batch are ignored. After capping, all usable review points from all batches are
pooled together and may be distributed arbitrarily among the gates.

Your task is to find the minimum integer threshold T such that the total capped
review points are enough to satisfy all gate requirements. If even using all
review points is insufficient, return -1.

Formally, find the smallest integer T >= 0 such that:
sum(min(reviews[j], T) for j in [0..m-1]) >= sum(requirements[i] for i in [0..n-1])

If no such T exists because:
sum(reviews) < sum(requirements),
return -1.

Constraints:
- 1 <= n, m <= 2 * 10^5
- 1 <= requirements[i], reviews[j] <= 10^12
- The answer must fit in 64-bit signed integer range
"""

from bisect import bisect_right
from itertools import accumulate
from typing import List


class Solution:
    def minimum_review_threshold(self, requirements: List[int], reviews: List[int]) -> int:
        """
        Find the minimum integer threshold T such that the sum of capped review
        batches is at least the total required points.

        The capped total for a threshold T is:
        sum(min(review, T) for review in reviews)

        Because this capped total is monotonic non-decreasing as T increases,
        we can binary search for the smallest valid threshold.

        Args:
            requirements: List of required review points for each build gate.
            reviews: List of review batch sizes.

        Returns:
            The minimum integer threshold T that makes the capped total at least
            the total requirement, or -1 if impossible.

        Time complexity:
            O(m log m + log(max(reviews)) * log m)
            where m = len(reviews)

        Space complexity:
            O(m)
        """
        # ------------------------------------------------------------
        # Step 1: Compute the total amount of review points required.
        # Since all capped review points are pooled together and can be
        # distributed arbitrarily, the individual gate order does not
        # matter for feasibility. Only the total required sum matters.
        # ------------------------------------------------------------
        total_required: int = sum(requirements)

        # ------------------------------------------------------------
        # Step 2: Compute the total amount of review points available
        # before any threshold cap is applied.
        #
        # If even the full uncapped total is smaller than what we need,
        # then no threshold can ever work, because capping can only keep
        # or reduce the total usable points.
        # ------------------------------------------------------------
        total_available: int = sum(reviews)
        if total_available < total_required:
            return -1

        # ------------------------------------------------------------
        # Step 3: Sort the review batches.
        #
        # Why sort?
        # For a chosen threshold T, we want to compute:
        #   sum(min(review, T))
        #
        # If the array is sorted, then:
        # - all values <= T contribute their full value
        # - all values > T contribute exactly T
        #
        # This lets us compute the capped sum quickly using:
        # - binary search to find the split point
        # - prefix sums to get the sum of the smaller values
        # ------------------------------------------------------------
        sorted_reviews: List[int] = sorted(reviews)

        # ------------------------------------------------------------
        # Step 4: Build prefix sums of the sorted review batches.
        #
        # prefix[i] will store the sum of the first i elements.
        # We use a leading 0 so that:
        #   prefix[0] = 0
        #   prefix[k] = sum(sorted_reviews[:k])
        #
        # This makes range sum calculations simple and safe.
        # ------------------------------------------------------------
        prefix_sums: List[int] = [0] + list(accumulate(sorted_reviews))

        # ------------------------------------------------------------
        # Step 5: Binary search on the answer T.
        #
        # Lower bound:
        #   0 is always a valid starting point for search space.
        #
        # Upper bound:
        #   max(reviews) is enough, because once T reaches the largest
        #   review batch, no batch is capped anymore, so the capped sum
        #   equals the full total_available.
        #
        # Since we already checked total_available >= total_required,
        # some answer must exist in [0, max(reviews)].
        # ------------------------------------------------------------
        left: int = 0
        right: int = max(sorted_reviews)

        # ------------------------------------------------------------
        # Standard "find first true" binary search:
        # - If mid works, try smaller values.
        # - If mid does not work, go larger.
        #
        # At the end, left == right and points to the minimum valid T.
        # ------------------------------------------------------------
        while left < right:
            mid: int = (left + right) // 2

            # --------------------------------------------------------
            # Compute the capped total for threshold = mid.
            #
            # We find how many review batches are <= mid.
            # bisect_right returns the insertion position to the right
            # of existing mid values, so:
            #   idx = number of elements <= mid
            #
            # Then:
            # - the first idx elements contribute their full values
            # - the remaining m - idx elements each contribute mid
            # --------------------------------------------------------
            idx: int = bisect_right(sorted_reviews, mid)

            # Sum of all review batches that are already <= mid
            sum_small: int = prefix_sums[idx]

            # Number of review batches larger than mid
            count_large: int = len(sorted_reviews) - idx

            # Total capped sum at threshold mid
            capped_total: int = sum_small + count_large * mid

            # --------------------------------------------------------
            # If capped_total is enough, mid is a valid threshold.
            # We try to find an even smaller valid threshold.
            #
            # Otherwise, mid is too small, so we must search higher.
            # --------------------------------------------------------
            if capped_total >= total_required:
                right = mid
            else:
                left = mid + 1

        return left

    def _capped_sum(self, sorted_reviews: List[int], prefix_sums: List[int], threshold: int) -> int:
        """
        Compute the total usable review points after capping each batch at
        the given threshold.

        This helper assumes the reviews are already sorted and prefix sums
        are already built.

        Args:
            sorted_reviews: Sorted list of review batch sizes.
            prefix_sums: Prefix sums for sorted_reviews, with a leading 0.
            threshold: The cap value T.

        Returns:
            The sum of min(review, threshold) over all review batches.

        Time complexity:
            O(log m)

        Space complexity:
            O(1)
        """
        # Find how many values are <= threshold.
        idx: int = bisect_right(sorted_reviews, threshold)

        # Sum of all values that stay unchanged after capping.
        sum_small: int = prefix_sums[idx]

        # All remaining values are > threshold, so each contributes threshold.
        count_large: int = len(sorted_reviews) - idx

        return sum_small + count_large * threshold


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # requirements = [5, 7, 4] => total_required = 16
    # reviews = [3, 10, 8]
    #
    # Check thresholds manually:
    # T = 5 => 3 + 5 + 5 = 13 (not enough)
    # T = 6 => 3 + 6 + 6 = 15 (not enough)
    # T = 7 => 3 + 7 + 7 = 17 (enough)
    # So the correct answer is 7.
    requirements_1: List[int] = [5, 7, 4]
    reviews_1: List[int] = [3, 10, 8]
    result_1: int = solution.minimum_review_threshold(requirements_1, reviews_1)
    print("Example 1 Result:", result_1)  # Expected: 7

    # Example 2
    # requirements = [9, 6] => total_required = 15
    # reviews = [4, 3, 5] => total_available = 12
    # Since 12 < 15, it is impossible regardless of threshold.
    # So the correct answer is -1.
    requirements_2: List[int] = [9, 6]
    reviews_2: List[int] = [4, 3, 5]
    result_2: int = solution.minimum_review_threshold(requirements_2, reviews_2)
    print("Example 2 Result:", result_2)  # Expected: -1