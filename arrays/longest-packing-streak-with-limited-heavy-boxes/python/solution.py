"""
Title: Longest Packing Streak With Limited Heavy Boxes

Problem Description:
A warehouse packs boxes in the order they arrive. You are given an integer array
weights, where weights[i] is the weight of the i-th box, and an integer limit.
A box is considered heavy if its weight is strictly greater than limit.

Find the length of the longest contiguous packing streak such that the streak
contains at most k heavy boxes. In other words, you may choose one continuous
subarray of weights, and inside that chosen range, the number of values greater
than limit must not exceed k.

Return the maximum possible length of such a streak.

This problem models a common operational constraint: the packing line can handle
only a small number of heavy boxes in any uninterrupted run before requiring
rebalancing. Your task is to determine the longest valid run without breaking
the order of boxes.

Constraints:
- 1 <= weights.length <= 200000
- 0 <= weights[i] <= 1000000000
- 0 <= limit <= 1000000000
- 0 <= k <= weights.length
"""

from typing import List


class Solution:
    def longest_packing_streak(self, weights: List[int], limit: int, k: int) -> int:
        """
        Find the maximum length of a contiguous subarray containing at most k heavy boxes.

        A box is heavy if its weight is strictly greater than limit.

        Args:
            weights: List of box weights in arrival order.
            limit: Threshold above which a box is considered heavy.
            k: Maximum number of heavy boxes allowed in the chosen contiguous streak.

        Returns:
            The length of the longest valid contiguous streak.

        Time complexity:
            O(n), where n is the number of boxes. Each index is processed at most twice
            by the sliding window pointers.

        Space complexity:
            O(1), because only a few integer variables are used.
        """
        # We use the classic "sliding window" / "two pointers" technique.
        #
        # Why this works:
        # - We want the longest contiguous subarray.
        # - The condition is based on a count inside the current window:
        #   "number of heavy boxes <= k".
        # - As we expand the right side of the window, the window may become invalid.
        # - When invalid, we move the left side forward until it becomes valid again.
        #
        # This approach is efficient because:
        # - The right pointer only moves forward.
        # - The left pointer only moves forward.
        # - Therefore total work is linear.

        # Left boundary of the current window.
        left: int = 0

        # Number of heavy boxes currently inside the window [left, right].
        heavy_count: int = 0

        # Best answer found so far.
        max_length: int = 0

        # Expand the window one box at a time using the right pointer.
        for right, weight in enumerate(weights):
            # Step 1: Include the new box at index "right" into the window.
            #
            # If this box is heavy, increase the heavy counter.
            # We use "strictly greater than limit" exactly as stated in the problem.
            if weight > limit:
                heavy_count += 1

            # Step 2: If the window now has too many heavy boxes,
            # shrink it from the left until it becomes valid again.
            #
            # We use a while-loop, not an if-statement, because removing one element
            # may not be enough. The window must satisfy heavy_count <= k before
            # we can measure its length as a valid candidate.
            while heavy_count > k:
                # Before moving left forward, check whether the outgoing box
                # is heavy. If it is, we must decrease the heavy counter because
                # that box is no longer inside the window.
                if weights[left] > limit:
                    heavy_count -= 1

                # Move the left boundary rightward by one position.
                left += 1

            # Step 3: At this point, the window [left, right] is guaranteed valid.
            # So we can compute its length and update the best answer.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After scanning all positions, max_length holds the answer.
        return max_length

    def longestPackingStreak(self, weights: List[int], limit: int, k: int) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            weights: List of box weights in arrival order.
            limit: Threshold above which a box is considered heavy.
            k: Maximum number of heavy boxes allowed in the chosen contiguous streak.

        Returns:
            The length of the longest valid contiguous streak.

        Time complexity:
            O(n), where n is the number of boxes.

        Space complexity:
            O(1).
        """
        return self.longest_packing_streak(weights, limit, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Heavy boxes (> 8): 9, 12, 15
    # Longest valid contiguous streak with at most 2 heavy boxes is:
    # [4, 9, 3, 12, 7] -> length 5
    weights_1: List[int] = [4, 9, 3, 12, 7, 15, 2]
    limit_1: int = 8
    k_1: int = 2
    result_1: int = solution.longest_packing_streak(weights_1, limit_1, k_1)
    print("Example 1 result:", result_1)  # Expected: 5

    # Example 2 in the prompt contains contradictory explanation text.
    # We must verify correctness ourselves.
    #
    # weights = [11, 5, 6, 13, 14, 2, 1], limit = 10, k = 1
    # Heavy boxes (> 10): 11, 13, 14
    #
    # Let's reason carefully:
    # - Any valid window may contain at most 1 heavy box.
    # - [11, 5, 6] has 1 heavy -> valid, length 3
    # - [5, 6, 13] has 1 heavy -> valid, length 3
    # - [14, 2, 1] has 1 heavy -> valid, length 3
    # - Any length-4 window here contains at least 2 heavy boxes:
    #   [11, 5, 6, 13] -> 2 heavy
    #   [5, 6, 13, 14] -> 2 heavy
    #   [6, 13, 14, 2] -> 2 heavy
    #   [13, 14, 2, 1] -> 2 heavy
    # Therefore the correct answer is 3, not 4.
    weights_2: List[int] = [11, 5, 6, 13, 14, 2, 1]
    limit_2: int = 10
    k_2: int = 1
    result_2: int = solution.longest_packing_streak(weights_2, limit_2, k_2)
    print("Example 2 result:", result_2)  # Correct expected: 3

    # Additional quick sanity checks.

    # No heavy boxes at all -> entire array is valid.
    weights_3: List[int] = [1, 2, 3, 4]
    limit_3: int = 10
    k_3: int = 0
    result_3: int = solution.longest_packing_streak(weights_3, limit_3, k_3)
    print("Sanity check 1 result:", result_3)  # Expected: 4

    # k = 0 means no heavy boxes allowed.
    # Longest segment with all values <= limit is [2, 3] or [1, 4], length 2.
    weights_4: List[int] = [9, 2, 3, 11, 1, 4, 12]
    limit_4: int = 8
    k_4: int = 0
    result_4: int = solution.longest_packing_streak(weights_4, limit_4, k_4)
    print("Sanity check 2 result:", result_4)  # Expected: 2

    # If k is large enough, the whole array is always valid.
    weights_5: List[int] = [100, 200, 300]
    limit_5: int = 50
    k_5: int = 3
    result_5: int = solution.longest_packing_streak(weights_5, limit_5, k_5)
    print("Sanity check 3 result:", result_5)  # Expected: 3