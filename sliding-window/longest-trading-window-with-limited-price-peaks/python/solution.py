"""
Title: Longest Trading Window With Limited Price Peaks

Problem Description:
You are given an integer array prices where prices[i] is the stock price observed at minute i,
and an integer k. A minute i is called a peak inside a contiguous window [l, r] if l < i < r
and prices[i] is strictly greater than both of its immediate neighbors within that window,
meaning prices[i] > prices[i - 1] and prices[i] > prices[i + 1].

Your task is to find the length of the longest contiguous window such that the number of peak
minutes inside that window is at most k.

Be careful: whether an index is a peak depends on the current window boundaries. For example,
an index cannot be a peak if it is the leftmost or rightmost element of the window, because it
would be missing one neighbor inside the window. This boundary dependence makes the problem
harder than simply precomputing all peaks once.

Return the maximum possible window length.

Constraints:
- 1 <= prices.length <= 200000
- 0 <= prices[i] <= 1000000000
- 0 <= k <= prices.length

Examples:
1) prices = [5, 1, 4, 2, 6, 3, 7], k = 2
   Output: 6

2) prices = [2, 9, 1, 8, 3, 7, 4], k = 1
   Output: 4
"""

from typing import List


class Solution:
    def longest_trading_window(self, prices: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous window containing at most k internal peaks.

        A key observation makes the problem much easier:
        for any fixed index i in the original array, whether it is a peak depends only on
        prices[i - 1], prices[i], and prices[i + 1]. Therefore, once i is strictly inside
        a window [l, r] (that is, l < i < r), its peak status is fixed and does not change.
        The only boundary-dependent part is whether i is included as an interior index at all.

        So we can precompute which indices are "global local maxima" with respect to their
        immediate neighbors in the array:
            is_peak[i] = 1 if prices[i] > prices[i - 1] and prices[i] > prices[i + 1]
        for 1 <= i <= n - 2.

        Then, for any window [l, r], the number of peaks inside that window is simply the
        number of such indices i in the interior range [l + 1, r - 1].

        This lets us use a standard sliding window:
        - Expand the right boundary one step at a time.
        - When right moves to a new position, the only index that newly becomes interior is
          right - 1, so if is_peak[right - 1] == 1, we add it to the current peak count.
        - If the window has too many peaks, move left forward.
          When left moves from l to l + 1, the only index that stops being interior is l + 1,
          so if is_peak[l + 1] == 1, we subtract it.

        Args:
            prices: List of stock prices.
            k: Maximum allowed number of internal peaks.

        Returns:
            The length of the longest valid contiguous window.

        Time complexity:
            O(n), where n is len(prices), because each pointer moves at most n times.

        Space complexity:
            O(n) for the precomputed peak array.
        """
        n: int = len(prices)

        # If the array has fewer than 3 elements, no window can contain an internal index,
        # so every window has 0 peaks. The whole array is always valid.
        if n < 3:
            return n

        # ---------------------------------------------------------------------
        # Step 1: Precompute which indices are local peaks in the full array.
        #
        # Why this is valid:
        # An index i is a peak inside a window if:
        #   - it is not on the boundary of the window, and
        #   - prices[i] > prices[i - 1] and prices[i] > prices[i + 1]
        #
        # The comparisons only involve the immediate neighbors in the original array,
        # which are the same neighbors whenever i is inside the window.
        #
        # Therefore:
        #   "i is a peak inside [l, r]"  <=>  "l < i < r and is_peak[i] == 1"
        # ---------------------------------------------------------------------
        is_peak: List[int] = [0] * n
        for i in range(1, n - 1):
            if prices[i] > prices[i - 1] and prices[i] > prices[i + 1]:
                is_peak[i] = 1

        # ---------------------------------------------------------------------
        # Step 2: Sliding window over [left, right].
        #
        # We maintain:
        #   current_peaks = number of peak indices strictly inside the current window
        #
        # Important invariant:
        #   current_peaks == sum(is_peak[i] for i in range(left + 1, right))
        #
        # Note that Python range(left + 1, right) corresponds to indices
        # [left + 1, right - 1], exactly the interior of the window [left, right].
        # ---------------------------------------------------------------------
        left: int = 0
        current_peaks: int = 0
        best: int = 1

        for right in range(n):
            # -------------------------------------------------------------
            # When we extend the window to include index 'right',
            # the only index that can newly become an interior element is
            # 'right - 1'.
            #
            # Example:
            #   Before adding right, window is [left, right - 1]
            #   Its interior is [left + 1, right - 2]
            #
            #   After adding right, window is [left, right]
            #   Its interior is [left + 1, right - 1]
            #
            # So exactly one new interior candidate appears: right - 1.
            # If that index is a peak, we add it.
            # -------------------------------------------------------------
            if right - 1 >= 1:
                current_peaks += is_peak[right - 1]

            # -------------------------------------------------------------
            # If we now have too many peaks, shrink from the left until the
            # window becomes valid again.
            #
            # When left moves from 'left' to 'left + 1':
            #   old interior = [left + 1, right - 1]
            #   new interior = [left + 2, right - 1]
            #
            # So exactly one index stops being interior: left + 1.
            # If that index was a peak, remove it from the count.
            # -------------------------------------------------------------
            while current_peaks > k:
                if left + 1 <= n - 2:
                    current_peaks -= is_peak[left + 1]
                left += 1

            # -------------------------------------------------------------
            # At this point, the current window [left, right] is valid.
            # Update the best answer with its length.
            # -------------------------------------------------------------
            window_length: int = right - left + 1
            if window_length > best:
                best = window_length

        return best

    def longestTradingWindow(self, prices: List[int], k: int) -> int:
        """
        Wrapper method using camelCase naming, for compatibility with common
        interview/platform naming styles.

        Args:
            prices: List of stock prices.
            k: Maximum allowed number of internal peaks.

        Returns:
            The length of the longest valid contiguous window.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.longest_trading_window(prices, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    prices1: List[int] = [5, 1, 4, 2, 6, 3, 7]
    k1: int = 2
    result1: int = solution.longestTradingWindow(prices1, k1)
    print(result1)  # Expected: 6

    # Example 2
    prices2: List[int] = [2, 9, 1, 8, 3, 7, 4]
    k2: int = 1
    result2: int = solution.longestTradingWindow(prices2, k2)
    print(result2)  # Expected: 4

    # Additional quick checks
    prices3: List[int] = [1]
    k3: int = 0
    print(solution.longestTradingWindow(prices3, k3))  # Expected: 1

    prices4: List[int] = [1, 3, 2]
    k4: int = 0
    print(solution.longestTradingWindow(prices4, k4))  # Expected: 2

    prices5: List[int] = [1, 3, 2]
    k5: int = 1
    print(solution.longestTradingWindow(prices5, k5))  # Expected: 3