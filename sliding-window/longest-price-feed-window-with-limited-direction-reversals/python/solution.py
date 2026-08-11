"""
Title: Longest Price Feed Window With Limited Direction Reversals

Problem Description:
You are given an integer array prices where prices[i] is the observed price of an asset at time i,
and an integer k. Consider any contiguous window prices[l..r]. For every adjacent pair inside the
window, define its direction as increasing if prices[i] < prices[i+1], decreasing if prices[i] > prices[i+1],
and flat if prices[i] == prices[i+1]. Flat steps do not contribute to direction changes.

A window is called smooth if, after ignoring all flat steps, the sequence of remaining directions changes
between increasing and decreasing at most k times. In other words, if the non-flat comparisons inside the
window form directions like [+,+,-,-,+], then this window has 2 direction reversals.

Return the length of the longest smooth contiguous window.

This problem is harder than a standard sliding window because the validity of a window depends on transitions
between adjacent comparisons, not just frequencies of values. An efficient solution should process the array
in linear time by maintaining a moving window over the comparison sequence and counting how many times
consecutive non-zero directions differ.
"""

from typing import List


class Solution:
    def _build_directions(self, prices: List[int]) -> List[int]:
        """
        Build the comparison-direction array between adjacent prices.

        Each entry represents the direction of one adjacent step:
        -  1 if prices[i] < prices[i + 1]  (increasing)
        - -1 if prices[i] > prices[i + 1]  (decreasing)
        -  0 if prices[i] == prices[i + 1] (flat)

        Args:
            prices: List of observed prices.

        Returns:
            A list of directions of length len(prices) - 1.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        directions: List[int] = []

        for i in range(len(prices) - 1):
            if prices[i] < prices[i + 1]:
                directions.append(1)
            elif prices[i] > prices[i + 1]:
                directions.append(-1)
            else:
                directions.append(0)

        return directions

    def longest_smooth_window(self, prices: List[int], k: int) -> int:
        """
        Return the length of the longest contiguous window whose non-flat direction sequence
        changes direction at most k times.

        The key idea is to slide a window over the direction array instead of directly over prices.
        A price window [l..r] corresponds to a direction window [l..r-1]. Inside that direction
        window, we ignore zeros and count how many times consecutive non-zero directions differ.

        We maintain:
        - left: left boundary in the direction array
        - prev_non_zero: for each right boundary, we track the most recent non-zero direction
        - reversals: number of direction changes among non-zero directions inside the current window

        When we move the left boundary forward, we may remove the first non-zero direction from the
        window. If that removed direction was participating in a reversal with the next non-zero
        direction, we must subtract that contribution. To do this efficiently in O(1) amortized time,
        we advance a pointer to find the first and second non-zero directions currently inside the window.

        Args:
            prices: List of observed prices.
            k: Maximum allowed number of direction reversals.

        Returns:
            The maximum valid window length in terms of number of prices.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(prices)

        # A single price always forms a valid window of length 1 because there are no adjacent comparisons.
        if n <= 1:
            return n

        # Convert prices into a direction array.
        # Example:
        # prices = [5, 7, 9, 8]
        # directions = [+, +, -] which we store as [1, 1, -1]
        directions: List[int] = self._build_directions(prices)
        m: int = len(directions)

        # This helper array lets us jump to the next non-zero direction quickly.
        # next_non_zero[i] = smallest index j >= i such that directions[j] != 0, or m if none exists.
        #
        # Why this is useful:
        # When shrinking the window from the left, we need to know which non-zero directions remain
        # at the front of the current window so we can update the reversal count correctly.
        next_non_zero: List[int] = [m] * (m + 1)
        next_non_zero[m] = m

        for i in range(m - 1, -1, -1):
            if directions[i] != 0:
                next_non_zero[i] = i
            else:
                next_non_zero[i] = next_non_zero[i + 1]

        # Sliding window over the direction array.
        left: int = 0

        # Number of reversals among non-zero directions currently inside directions[left..right].
        reversals: int = 0

        # Track the most recent non-zero direction seen while extending the right boundary.
        # This is used to determine whether adding directions[right] creates a new reversal.
        prev_non_zero: int = 0

        # Best answer in terms of number of prices, not number of directions.
        # A direction window of length d corresponds to a price window of length d + 1.
        best: int = 1

        # Extend the right boundary one direction at a time.
        for right in range(m):
            current: int = directions[right]

            # If the new direction is non-flat, it may create a reversal with the previous
            # non-flat direction already inside the current window.
            #
            # Example:
            # existing non-zero sequence: [+, +, -]
            # adding + creates a reversal only if the previous non-zero was -.
            if current != 0:
                if prev_non_zero != 0 and prev_non_zero != current:
                    reversals += 1
                prev_non_zero = current

            # If the window is invalid, shrink it from the left until it becomes valid again.
            while reversals > k:
                # We are about to remove directions[left] from the window.
                #
                # Removing a zero never changes the reversal count because zeros are ignored.
                # Removing a non-zero can reduce the reversal count by 1 if it was the first
                # non-zero direction in the window and the next non-zero direction had opposite sign.
                #
                # To detect that, we find:
                # first = first non-zero direction index in directions[left..right]
                # second = next non-zero direction index after first
                #
                # If left == first and second exists and directions[first] != directions[second],
                # then the reversal between those two directions disappears when first is removed.
                if directions[left] != 0:
                    first: int = next_non_zero[left]
                    second: int = next_non_zero[first + 1] if first < m else m

                    if first == left and second <= right and directions[first] != directions[second]:
                        reversals -= 1

                left += 1

                # After moving left, prev_non_zero may no longer be accurate if the entire window
                # lost all non-zero directions. In that case, reset it to 0.
                #
                # Why this matters:
                # Suppose the window becomes all-flat after shrinking. Then when we later add a new
                # non-zero direction, it should not be compared against an old direction that is no
                # longer inside the window.
                first_remaining_non_zero: int = next_non_zero[left] if left <= m else m
                if first_remaining_non_zero > right:
                    prev_non_zero = 0
                else:
                    # If there are still non-zero directions in the window, the most recent non-zero
                    # direction seen while expanding right is still valid unless the window became empty
                    # of non-zero directions. Therefore no further action is needed here.
                    pass

            # directions[left..right] corresponds to prices[left..right+1]
            # so the price-window length is (right - left + 1) + 1 = right - left + 2
            current_length: int = right - left + 2
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    prices1: List[int] = [5, 7, 9, 8, 6, 6, 10, 12]
    k1: int = 1
    result1: int = solution.longest_smooth_window(prices1, k1)
    print(result1)

    prices2: List[int] = [4, 4, 4, 3, 2, 5, 7, 6, 1]
    k2: int = 2
    result2: int = solution.longest_smooth_window(prices2, k2)
    print(result2)

    extra_prices: List[int] = [1, 3, 2, 4, 3, 5]
    extra_k: int = 2
    extra_result: int = solution.longest_smooth_window(extra_prices, extra_k)
    print(extra_result)