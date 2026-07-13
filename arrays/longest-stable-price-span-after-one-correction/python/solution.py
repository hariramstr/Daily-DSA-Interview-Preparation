"""
Title: Longest Stable Price Span After One Correction

Problem Description:
A retail analytics team records the price of the same product once per day in an integer
array `prices`, where `prices[i]` is the observed price on day `i`.

A span of days is considered stable if all values in that contiguous subarray are equal.
However, the team knows that at most one recorded day in a span may be wrong due to a
data entry mistake. You are allowed to correct the value of at most one element inside a
chosen contiguous subarray to any integer you want.

Return the length of the longest contiguous subarray that can be made stable after
applying at most one correction.

In other words, find the maximum length of a subarray such that by changing zero or one
element in that subarray, every value in the subarray can become identical.

Constraints:
- 1 <= prices.length <= 2 * 10^5
- 1 <= prices[i] <= 10^9

Examples:
1) prices = [5, 5, 7, 5, 5, 5]
   Output: 6
   Explanation: Change the 7 to 5. Then the entire array becomes stable.

2) prices = [3, 3, 4, 4, 4, 3]
   Output: 4
   Explanation: The longest valid span is [4, 4, 4, 3]. By changing the last 3 to 4,
   the span becomes [4, 4, 4, 4].
"""

from typing import List


class Solution:
    def longest_stable_price_span_after_one_correction(self, prices: List[int]) -> int:
        """
        Find the maximum length of a contiguous subarray that can be made entirely equal
        by changing at most one element.

        Key observation:
        A subarray can be turned into all one value using at most one correction if and only if
        inside that subarray, all elements are already the same except possibly one element.

        That means:
        window_length - (highest frequency of any value inside the window) <= 1

        We use a sliding window:
        - Expand the right side one element at a time.
        - Track frequencies of values inside the current window.
        - Track the maximum frequency seen in the current window logic.
        - If more than one element would need correction, shrink from the left.

        Args:
            prices: List of recorded daily prices.

        Returns:
            The length of the longest valid contiguous subarray.

        Time complexity:
            O(n), where n is the length of prices.

        Space complexity:
            O(k), where k is the number of distinct values inside the current window
            (worst case O(n)).
        """
        # Frequency array via dictionary:
        # freq[value] = how many times this value appears in the current sliding window.
        freq: dict[int, int] = {}

        # left is the start index of the current window.
        left: int = 0

        # max_freq stores the largest frequency of any single value seen in the current window.
        #
        # Important detail:
        # We do NOT decrease max_freq when shrinking the window.
        # This is a standard sliding-window optimization used in problems like
        # "longest repeating character replacement".
        #
        # Why is that safe?
        # Because max_freq may become "stale" (too large), but that only means we may delay
        # shrinking slightly. This never causes us to miss the correct answer, and the final
        # maximum valid window length remains correct.
        max_freq: int = 0

        # best stores the longest valid window length found so far.
        best: int = 0

        # Move right across the array, expanding the window one element at a time.
        for right, value in enumerate(prices):
            # Add the new rightmost value into the frequency map.
            freq[value] = freq.get(value, 0) + 1

            # Update max_freq if this value now has the highest count in the window.
            if freq[value] > max_freq:
                max_freq = freq[value]

            # Current window length is from left to right inclusive.
            # To make the whole window equal, we would keep the most frequent value
            # and change every other value.
            #
            # Number of changes needed:
            #   window_length - max_freq
            #
            # We are allowed at most one correction, so if this exceeds 1,
            # the window is invalid and must be shrunk from the left.
            while (right - left + 1) - max_freq > 1:
                left_value: int = prices[left]
                freq[left_value] -= 1

                # Move left boundary rightward to reduce the window size.
                left += 1

            # After possible shrinking, the window is valid under the sliding-window logic.
            current_length: int = right - left + 1

            # Update the best answer.
            if current_length > best:
                best = current_length

        return best

    def longestStablePriceSpanAfterOneCorrection(self, prices: List[int]) -> int:
        """
        Wrapper method using a camelCase name for convenience.

        Args:
            prices: List of recorded daily prices.

        Returns:
            The length of the longest valid contiguous subarray.

        Time complexity:
            O(n), where n is the length of prices.

        Space complexity:
            O(k), where k is the number of distinct values in the window.
        """
        return self.longest_stable_price_span_after_one_correction(prices)


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [5, 5, 7, 5, 5, 5]
    sample_2: List[int] = [3, 3, 4, 4, 4, 3]
    sample_3: List[int] = [1]
    sample_4: List[int] = [2, 2, 2, 2]
    sample_5: List[int] = [1, 2, 3, 4]
    sample_6: List[int] = [8, 8, 9, 8, 10, 8, 8]

    print("Sample 1:", solution.longest_stable_price_span_after_one_correction(sample_1))  # Expected: 6
    print("Sample 2:", solution.longest_stable_price_span_after_one_correction(sample_2))  # Expected: 4
    print("Sample 3:", solution.longest_stable_price_span_after_one_correction(sample_3))  # Expected: 1
    print("Sample 4:", solution.longest_stable_price_span_after_one_correction(sample_4))  # Expected: 4
    print("Sample 5:", solution.longest_stable_price_span_after_one_correction(sample_5))  # Expected: 2
    print("Sample 6:", solution.longest_stable_price_span_after_one_correction(sample_6))  # Expected: 4