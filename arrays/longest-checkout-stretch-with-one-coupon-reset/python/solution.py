"""
Title: Longest Checkout Stretch With One Coupon Reset

Problem Description:
A retail system records the coupon code used for each consecutive customer in a
checkout line. The array `coupons` represents these coupon codes in order, where
`coupons[i]` is the code used by the `i`-th customer.

A checkout stretch is considered consistent if all customers in that contiguous
stretch use the same coupon code. You are allowed to perform at most one reset
operation: choose exactly one customer in the stretch and change that customer's
coupon code to match the coupon code of the rest of the stretch.

Your task is to return the length of the longest contiguous stretch that can be
made consistent using at most one such reset.

In other words, find the maximum length of a subarray that can be turned into all
equal values after modifying at most one element inside that subarray.

Constraints:
- 1 <= coupons.length <= 200000
- 1 <= coupons[i] <= 1000000000

Examples:
1) coupons = [5, 5, 3, 5, 5, 5]
   Output: 6
   Explanation: Change the 3 at index 2 to 5. The entire array becomes a
   consistent stretch of length 6.

2) coupons = [1, 2, 2, 3, 2, 2, 2]
   Output: 6
   Explanation: Consider the subarray [2, 2, 3, 2, 2, 2]. By changing the 3 to 2,
   this stretch becomes all 2's, so the answer is 6.
"""

from typing import Dict, List


class Solution:
    def longest_checkout_stretch_with_one_reset(self, coupons: List[int]) -> int:
        """
        Return the maximum length of a contiguous subarray that can be made
        entirely equal after modifying at most one element.

        Args:
            coupons: List of coupon codes in checkout order.

        Returns:
            The length of the longest valid contiguous stretch.

        Time complexity:
            O(n), where n is the length of coupons.

        Space complexity:
            O(k), where k is the number of distinct coupon codes inside the
            current sliding window / overall map usage in the worst case.
        """
        # We use a classic sliding window technique.
        #
        # Core idea:
        # A window [left, right] can be turned into all equal values with at most
        # one modification if:
        #
        #     window_size - (count of the most frequent value in the window) <= 1
        #
        # Why?
        # - Suppose the most frequent value appears `max_freq` times.
        # - Then every other value in the window would need to be changed to match it.
        # - The number of required changes is:
        #       window_size - max_freq
        # - Since we are allowed at most one reset, this quantity must be <= 1.
        #
        # This transforms the problem into:
        # "Find the longest window where all but at most one element are equal."
        #
        # We maintain:
        # - `left`: left boundary of the current window
        # - `freq`: frequency map of values inside the current window
        # - `max_freq`: the highest frequency of any single value seen in the
        #   current window growth process
        # - `best`: best valid window length found so far
        #
        # Important subtle point:
        # We do NOT decrease `max_freq` when shrinking the window.
        # This is a standard optimization used in sliding-window problems like
        # "Longest Repeating Character Replacement".
        #
        # Why is that still correct?
        # - `max_freq` may become "stale" (too large for the current exact window),
        #   but that only makes the condition more permissive temporarily.
        # - Even with that, the algorithm still never misses the optimal answer,
        #   and the final maximum length remains correct.
        # - This keeps the algorithm O(n), because recomputing the exact max
        #   frequency on every shrink would be too expensive.
        #
        # For this problem, that standard proof applies directly.

        left: int = 0
        freq: Dict[int, int] = {}
        max_freq: int = 0
        best: int = 0

        # Expand the window one element at a time using `right`.
        for right, value in enumerate(coupons):
            # Add the new rightmost value into the frequency map.
            freq[value] = freq.get(value, 0) + 1

            # Update the largest frequency seen in the current expanding process.
            # If this value now appears more times than any previous value in the
            # window, it becomes the new dominant candidate.
            if freq[value] > max_freq:
                max_freq = freq[value]

            # Current window size is from left to right inclusive.
            # If more than one element would need to be changed to make the whole
            # window equal, then the window is invalid and must be shrunk.
            #
            # Required changes = window_size - max_freq
            while (right - left + 1) - max_freq > 1:
                # Remove the leftmost element from the window because we are
                # moving `left` forward by one step.
                left_value: int = coupons[left]
                freq[left_value] -= 1

                # Optional cleanup: remove keys with zero count to keep the map tidy.
                if freq[left_value] == 0:
                    del freq[left_value]

                left += 1

            # At this point, the window is valid under the sliding-window logic.
            # Update the best answer with the current valid window length.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def solve(self, coupons: List[int]) -> int:
        """
        Convenience wrapper that solves the problem.

        Args:
            coupons: List of coupon codes.

        Returns:
            The maximum consistent stretch length after at most one reset.

        Time complexity:
            O(n), where n is the number of customers.

        Space complexity:
            O(k), where k is the number of distinct coupon codes tracked.
        """
        return self.longest_checkout_stretch_with_one_reset(coupons)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Entire array [5, 5, 3, 5, 5, 5] can become all 5's by changing the 3.
    coupons1: List[int] = [5, 5, 3, 5, 5, 5]
    result1: int = solution.solve(coupons1)
    print("Input:", coupons1)
    print("Output:", result1)
    print("Expected:", 6)
    print()

    # Example 2:
    # Subarray [2, 2, 3, 2, 2, 2] can become all 2's by changing the 3.
    coupons2: List[int] = [1, 2, 2, 3, 2, 2, 2]
    result2: int = solution.solve(coupons2)
    print("Input:", coupons2)
    print("Output:", result2)
    print("Expected:", 6)
    print()

    # Additional quick checks for beginner-friendly demonstration.
    extra_tests: List[List[int]] = [
        [7],
        [4, 4, 4, 4],
        [1, 2],
        [1, 2, 3],
        [9, 9, 8, 9],
        [1, 1, 2, 2, 2, 1, 1],
    ]

    for test in extra_tests:
        print("Input:", test)
        print("Longest stretch:", solution.solve(test))
        print()