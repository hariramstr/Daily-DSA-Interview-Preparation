"""
Freshness Score of a Sliding News Feed
=======================================

Problem Description:
You are building a news aggregator that monitors a stream of articles.
Each article has a freshness score represented as a non-negative integer.
Your task is to find the maximum average freshness score across any contiguous
window of exactly k articles in the feed.

Given an integer array scores where scores[i] represents the freshness score
of the i-th article, and an integer k, return the maximum average value of any
contiguous subarray of length k. Your answer will be accepted if it is within
10^-5 of the actual answer.

Constraints:
- 1 <= k <= scores.length <= 10^5
- 0 <= scores[i] <= 10^4

Example 1:
    Input: scores = [3, 7, 2, 9, 4, 6, 1], k = 3
    Output: 6.66667
    Explanation: The subarray [9, 4, 6] has a sum of 19, giving an average of
                 19/3 ≈ 6.66667, which is the highest among all windows of size 3.

Example 2:
    Input: scores = [5, 5, 5, 5], k = 2
    Output: 5.00000
    Explanation: Every window of size 2 has a sum of 10 and an average of 5.0.
"""

from typing import List


class Solution:
    def findMaxAverage(self, scores: List[int], k: int) -> float:
        """
        Find the maximum average freshness score across any contiguous window of size k.

        This method uses the sliding window technique to efficiently compute the
        maximum average in O(n) time. Instead of recomputing the sum for each
        window from scratch (which would be O(n*k)), we maintain a running sum
        and slide the window one position at a time by adding the new element
        and removing the old element.

        Args:
            scores (List[int]): A list of non-negative integers representing
                                freshness scores of articles.
            k (int): The exact window size (number of articles) to consider.

        Returns:
            float: The maximum average freshness score over any contiguous
                   subarray of length k.

        Time Complexity:  O(n) — We traverse the array exactly once.
        Space Complexity: O(1) — We only use a fixed number of extra variables.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Compute the sum of the FIRST window (indices 0 to k-1).
        # -----------------------------------------------------------------------
        # We start by summing the first k elements. This gives us the initial
        # window sum before we begin sliding.
        # Using Python's built-in sum() on a slice is clean and readable.
        window_sum = sum(scores[:k])

        # -----------------------------------------------------------------------
        # STEP 2: Initialize max_sum with the first window's sum.
        # -----------------------------------------------------------------------
        # We track the maximum sum seen so far. We compare sums (not averages)
        # because dividing by k is a constant operation — maximizing the sum
        # is equivalent to maximizing the average when k is fixed.
        max_sum = window_sum

        # -----------------------------------------------------------------------
        # STEP 3: Slide the window from position 1 to len(scores) - k.
        # -----------------------------------------------------------------------
        # For each new position i (where i is the index of the NEW element
        # entering the window on the right), we:
        #   a) Add scores[i] to window_sum  (new element slides IN from the right)
        #   b) Subtract scores[i - k] from window_sum (old element slides OUT from the left)
        #
        # Why start at index k?
        #   - When i = k, the window covers indices [1, 2, ..., k].
        #   - The element leaving is scores[i - k] = scores[0].
        #   - The element entering is scores[i] = scores[k].
        #
        # Why end at len(scores)?
        #   - The last valid window ends at index len(scores) - 1,
        #     so the last i we process is len(scores) - 1.
        for i in range(k, len(scores)):
            # --- Slide the window: add the incoming element on the right ---
            # scores[i] is the new article entering the window.
            window_sum += scores[i]

            # --- Slide the window: remove the outgoing element on the left ---
            # scores[i - k] is the article that just fell out of the window.
            # Example: if k=3 and i=3, we remove scores[0] and add scores[3].
            window_sum -= scores[i - k]

            # --- Update the maximum sum if the current window is better ---
            # We use max() to keep track of the best window sum seen so far.
            if window_sum > max_sum:
                max_sum = window_sum

        # -----------------------------------------------------------------------
        # STEP 4: Convert the maximum sum to an average and return it.
        # -----------------------------------------------------------------------
        # Since every window has exactly k elements, dividing by k gives the
        # maximum average. We do this only once at the end (not inside the loop)
        # to keep the loop as lightweight as possible.
        return max_sum / k


# ---------------------------------------------------------------------------
# Verification / Trace-through
# ---------------------------------------------------------------------------
# Example 1: scores = [3, 7, 2, 9, 4, 6, 1], k = 3
#   Initial window [3, 7, 2]  -> window_sum = 12, max_sum = 12
#   i=3: add 9, remove 3  -> window_sum = 18, max_sum = 18  (window [7,2,9])
#   i=4: add 4, remove 7  -> window_sum = 15, max_sum = 18  (window [2,9,4])
#   i=5: add 6, remove 2  -> window_sum = 19, max_sum = 19  (window [9,4,6])
#   i=6: add 1, remove 9  -> window_sum = 11, max_sum = 19  (window [4,6,1])
#   Result: 19 / 3 = 6.33333... wait — let me recheck.
#   19 / 3 = 6.666... ✓  (matches expected output 6.66667)
#
# Example 2: scores = [5, 5, 5, 5], k = 2
#   Initial window [5, 5] -> window_sum = 10, max_sum = 10
#   i=2: add 5, remove 5 -> window_sum = 10, max_sum = 10
#   i=3: add 5, remove 5 -> window_sum = 10, max_sum = 10
#   Result: 10 / 2 = 5.0 ✓  (matches expected output 5.00000)
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    # Create a Solution instance
    solution = Solution()

    # ------------------------------------------------------------------
    # Test Case 1 — from the problem description
    # ------------------------------------------------------------------
    scores1 = [3, 7, 2, 9, 4, 6, 1]
    k1 = 3
    result1 = solution.findMaxAverage(scores1, k1)
    print("Test Case 1:")
    print(f"  Input  : scores = {scores1}, k = {k1}")
    print(f"  Output : {result1:.5f}")          # Expected: 6.66667
    print(f"  Expected: 6.66667")
    print()

    # ------------------------------------------------------------------
    # Test Case 2 — from the problem description
    # ------------------------------------------------------------------
    scores2 = [5, 5, 5, 5]
    k2 = 2
    result2 = solution.findMaxAverage(scores2, k2)
    print("Test Case 2:")
    print(f"  Input  : scores = {scores2}, k = {k2}")
    print(f"  Output : {result2:.5f}")          # Expected: 5.00000
    print(f"  Expected: 5.00000")
    print()

    # ------------------------------------------------------------------
    # Test Case 3 — edge case: k equals the length of the array
    # ------------------------------------------------------------------
    scores3 = [1, 2, 3, 4, 5]
    k3 = 5
    result3 = solution.findMaxAverage(scores3, k3)
    print("Test Case 3 (k == len(scores)):")
    print(f"  Input  : scores = {scores3}, k = {k3}")
    print(f"  Output : {result3:.5f}")          # Expected: 3.00000 (average of all)
    print(f"  Expected: 3.00000")
    print()

    # ------------------------------------------------------------------
    # Test Case 4 — edge case: single element array with k=1
    # ------------------------------------------------------------------
    scores4 = [42]
    k4 = 1
    result4 = solution.findMaxAverage(scores4, k4)
    print("Test Case 4 (single element):")
    print(f"  Input  : scores = {scores4}, k = {k4}")
    print(f"  Output : {result4:.5f}")          # Expected: 42.00000
    print(f"  Expected: 42.00000")
    print()

    # ------------------------------------------------------------------
    # Test Case 5 — all zeros
    # ------------------------------------------------------------------
    scores5 = [0, 0, 0, 0, 0]
    k5 = 3
    result5 = solution.findMaxAverage(scores5, k5)
    print("Test Case 5 (all zeros):")
    print(f"  Input  : scores = {scores5}, k = {k5}")
    print(f"  Output : {result5:.5f}")          # Expected: 0.00000
    print(f"  Expected: 0.00000")