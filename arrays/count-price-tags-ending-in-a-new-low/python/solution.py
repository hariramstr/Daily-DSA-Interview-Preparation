"""
Title: Count Price Tags Ending in a New Low

Problem Description:
You are given an integer array prices where prices[i] is the price tag printed for the
i-th item in the order items were labeled during a day. A price tag is considered to
end in a new low if its value is strictly smaller than every price that appeared before
it in the array. The first item always counts, because there are no earlier prices to
compare against.

Return the number of items whose price tag ends in a new low.

This problem is a simple array scan: as you move from left to right, keep track of the
smallest price seen so far. Whenever the current price is smaller than that running
minimum, it creates a new low and should be counted.

Constraints:
- 1 <= prices.length <= 100000
- -1000000000 <= prices[i] <= 1000000000
- prices may contain duplicates

Important notes:
- A value equal to the current minimum does not count as a new low.
- The first element always contributes 1 to the answer.

Example 1:
Input: prices = [12, 10, 11, 9, 9, 7]
Output: 4
Explanation: New lows occur at 12, 10, 9, and 7. The second 9 does not count because
it is not strictly smaller than the previous minimum.

Example 2:
Input: prices = [5, 5, 5, 4, 6, 3]
Output: 3
Explanation: New lows occur at 5, 4, and 3.
"""

from typing import List


class Solution:
    def count_new_lows(self, prices: List[int]) -> int:
        """
        Count how many price tags are strictly smaller than every earlier price.

        The first element always counts as a new low because there are no previous
        elements to compare against. After that, we scan from left to right while
        maintaining the smallest value seen so far.

        Args:
            prices: A list of integer prices in the order they appeared.

        Returns:
            The number of positions whose value is a new strict minimum.

        Time complexity:
            O(n), where n is the length of prices, because we inspect each element once.

        Space complexity:
            O(1), because we use only a few extra variables regardless of input size.
        """
        # The problem guarantees at least one element, so we can safely treat the
        # first price as the initial minimum and also count it immediately.
        #
        # Why this works:
        # - The first item has no earlier prices.
        # - Therefore, by definition, it always creates a new low.
        count: int = 1

        # Store the smallest price seen so far.
        #
        # This variable is the key to achieving O(n) time and O(1) extra space:
        # instead of comparing the current price against all previous prices,
        # we only need to compare it against the smallest previous price.
        #
        # If current_price < min_so_far, then current_price is smaller than every
        # earlier price, so it must be a new low.
        min_so_far: int = prices[0]

        # Start scanning from the second element because the first one has already
        # been handled above.
        for price in prices[1:]:
            # Check whether the current price is STRICTLY smaller than the smallest
            # price we have seen before this point.
            #
            # Important detail:
            # We use "<" and not "<=" because equal values do NOT count as a new low.
            # For example, if min_so_far is 9 and the current price is also 9,
            # then it is not smaller than every previous value; it only ties the minimum.
            if price < min_so_far:
                # We found a new strict minimum, so this price tag counts.
                count += 1

                # Update the running minimum so future elements are compared against
                # this new, smaller value.
                min_so_far = price

            # If price is greater than or equal to min_so_far, nothing changes:
            # - It does not count as a new low.
            # - The smallest value seen so far remains the same.

        # After scanning the full array, count contains the total number of new lows.
        return count


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # prices = [12, 10, 11, 9, 9, 7]
    # Trace:
    # - 12 -> first element, counts (count = 1, min = 12)
    # - 10 -> 10 < 12, counts (count = 2, min = 10)
    # - 11 -> 11 < 10? no
    # - 9  -> 9 < 10, counts (count = 3, min = 9)
    # - 9  -> 9 < 9? no, equal does not count
    # - 7  -> 7 < 9, counts (count = 4, min = 7)
    # Final answer: 4
    prices1: List[int] = [12, 10, 11, 9, 9, 7]
    result1: int = solution.count_new_lows(prices1)
    print(f"Input: {prices1}")
    print(f"Output: {result1}")
    print("Expected: 4")
    print()

    # Example 2:
    # prices = [5, 5, 5, 4, 6, 3]
    # Trace:
    # - 5 -> first element, counts (count = 1, min = 5)
    # - 5 -> 5 < 5? no
    # - 5 -> 5 < 5? no
    # - 4 -> 4 < 5, counts (count = 2, min = 4)
    # - 6 -> 6 < 4? no
    # - 3 -> 3 < 4, counts (count = 3, min = 3)
    # Final answer: 3
    prices2: List[int] = [5, 5, 5, 4, 6, 3]
    result2: int = solution.count_new_lows(prices2)
    print(f"Input: {prices2}")
    print(f"Output: {result2}")
    print("Expected: 3")
    print()

    # Additional small beginner-friendly checks.
    additional_tests: List[List[int]] = [
        [1],
        [3, 2, 1],
        [1, 2, 3],
        [0, 0, -1, -1, -2],
    ]

    for test_prices in additional_tests:
        print(f"Input: {test_prices}")
        print(f"Output: {solution.count_new_lows(test_prices)}")
        print()