"""
Title: Longest Stretch With At Most K Price Resets

Problem Description:
An online retailer records the displayed price of a product once per hour in an integer
array prices, where prices[i] is the price at hour i. A pricing analyst defines a stretch
of hours as stable if, after removing at most k hours from that stretch, the remaining
prices are all equal. The removed hours do not need to be consecutive, but they must come
from inside the chosen contiguous stretch.

Your task is to return the length of the longest contiguous stretch that can be made stable
under this rule.

In other words, for any subarray prices[l...r], you may delete at most k elements from that
subarray. If the remaining elements can all have the same value, then that subarray is valid.
Find the maximum possible length of such a valid subarray.

This models a real pricing system where a few temporary resets or glitches may be ignored
when measuring how long a product effectively stayed at one displayed price.

Constraints:
- 1 <= prices.length <= 100000
- 1 <= prices[i] <= 1000000000
- 0 <= k <= prices.length
- The answer should be computed in O(n) or O(n log n) time.

Example 1:
Input: prices = [7,7,8,7,7,9,7], k = 1
Output: 5
Explanation: The subarray [7,7,8,7,7] has length 5. By removing the single value 8,
all remaining values are 7, so this stretch is valid. No longer valid stretch exists
with at most 1 removal.

Example 2:
Input: prices = [4,5,5,5,6,5,5], k = 2
Output: 7
Explanation: The entire array has length 7. Remove 4 and 6, and all remaining values
are 5. Since only 2 removals are needed, the whole stretch is valid.

Key Insight:
A subarray is valid if:
    window_length - (highest frequency of any value in the window) <= k

Why?
- To make all remaining values equal, we keep the value that appears most often.
- Every other value in the window must be removed.
- So the number of removals needed is:
      total elements in window - count of majority value
- If that number is at most k, the window is valid.

This leads naturally to a sliding window solution.
"""

from collections import defaultdict
from typing import DefaultDict, List


class Solution:
    def longest_stable_stretch(self, prices: List[int], k: int) -> int:
        """
        Return the length of the longest contiguous subarray that can be made
        entirely equal after deleting at most k elements from that subarray.

        Args:
            prices: List of recorded prices by hour.
            k: Maximum number of elements allowed to be removed.

        Returns:
            The maximum valid subarray length.

        Time Complexity:
            O(n), where n is the length of prices.
            Each element is added to the sliding window once and removed at most once.

        Space Complexity:
            O(m), where m is the number of distinct values currently tracked in the window.
            In the worst case, this can be O(n).
        """
        # Frequency map for values inside the current sliding window.
        # Example:
        # if window is [7, 7, 8, 7], then counts = {7: 3, 8: 1}
        counts: DefaultDict[int, int] = defaultdict(int)

        # Left boundary of the sliding window.
        left: int = 0

        # This stores the highest frequency of any single value seen in the current window
        # during expansion.
        #
        # Important subtle point:
        # We do NOT decrease this value when shrinking the window.
        # This is a standard optimization used in the "longest repeating character replacement"
        # style sliding window problem.
        #
        # Why is that still correct?
        # - max_count may become "stale" (too large) after shrinking.
        # - A stale max_count can make the window appear more valid than it truly is.
        # - However, this does not break correctness for the maximum length result because:
        #   1. We only ever increase the answer when expanding the right boundary.
        #   2. Any overly generous validity caused by stale max_count does not allow us to
        #      miss the true optimal answer.
        #   3. This technique is well-known and preserves O(n) time.
        #
        # Intuition:
        # We are effectively tracking the largest window that could be valid based on the
        # best frequency we have seen so far while scanning.
        max_count: int = 0

        # Best answer found so far.
        best: int = 0

        # Expand the window one element at a time using the right pointer.
        for right, price in enumerate(prices):
            # Add the new rightmost element into the window.
            counts[price] += 1

            # Update the highest frequency seen in the current window expansion.
            # If this price now appears more times than any previous value in the window,
            # it becomes the new majority candidate.
            if counts[price] > max_count:
                max_count = counts[price]

            # Current window length is from left to right inclusive.
            # To make the whole window stable, we would keep the majority value
            # and remove all other values.
            #
            # Required removals = window_length - max_count
            #
            # If required removals > k, the window is invalid and must be shrunk.
            while (right - left + 1) - max_count > k:
                # Remove the leftmost element from the window because we are shrinking.
                left_price: int = prices[left]
                counts[left_price] -= 1

                # Move the left boundary rightward.
                left += 1

                # We intentionally do not recompute max_count here.
                # Recomputing it exactly would require scanning all frequencies,
                # which could make the solution slower.
                #
                # Keeping a possibly stale max_count still yields a correct O(n) solution.

            # At this point, the window is considered valid under the sliding window rule.
            # Update the best answer with the current window size if it is larger.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def longestStableStretch(self, prices: List[int], k: int) -> int:
        """
        Wrapper method using camelCase naming for compatibility with different
        platform naming styles.

        Args:
            prices: List of recorded prices by hour.
            k: Maximum number of elements allowed to be removed.

        Returns:
            The maximum valid subarray length.

        Time Complexity:
            O(n)

        Space Complexity:
            O(n) in the worst case due to the frequency map.
        """
        return self.longest_stable_stretch(prices, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # prices = [7,7,8,7,7,9,7], k = 1
    # Best valid subarray is [7,7,8,7,7], length = 5
    # Remove 8, remaining values are all 7
    prices1: List[int] = [7, 7, 8, 7, 7, 9, 7]
    k1: int = 1
    result1: int = solution.longest_stable_stretch(prices1, k1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2:
    # prices = [4,5,5,5,6,5,5], k = 2
    # Entire array is valid, length = 7
    # Remove 4 and 6, remaining values are all 5
    prices2: List[int] = [4, 5, 5, 5, 6, 5, 5]
    k2: int = 2
    result2: int = solution.longest_stable_stretch(prices2, k2)
    print("Example 2 Result:", result2)  # Expected: 7

    # Additional quick checks for beginner-friendly demonstration.

    # All equal already, no removals needed.
    prices3: List[int] = [3, 3, 3, 3]
    k3: int = 0
    result3: int = solution.longest_stable_stretch(prices3, k3)
    print("Additional Test 1 Result:", result3)  # Expected: 4

    # No removals allowed, so answer is longest contiguous block that is already equal.
    prices4: List[int] = [1, 2, 2, 3, 3, 3, 2]
    k4: int = 0
    result4: int = solution.longest_stable_stretch(prices4, k4)
    print("Additional Test 2 Result:", result4)  # Expected: 3

    # Large enough k to ignore all mismatches in the whole array.
    prices5: List[int] = [1, 2, 1, 3, 1]
    k5: int = 2
    result5: int = solution.longest_stable_stretch(prices5, k5)
    print("Additional Test 3 Result:", result5)  # Expected: 5