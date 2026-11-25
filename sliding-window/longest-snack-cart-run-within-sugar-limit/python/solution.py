"""
Title: Longest Snack Cart Run Within Sugar Limit

Problem Description:
A company cafeteria records the sugar content of each snack item placed on a moving cart
during the day. You are given an integer array sugars where sugars[i] is the sugar amount
of the i-th snack in grams, and an integer limit representing the maximum total sugar a
nutrition program allows in one consecutive selection.

Your task is to find the length of the longest contiguous segment of the cart such that the
sum of sugar values in that segment is less than or equal to limit. In other words, choose
a consecutive block of snacks with total sugar at most limit, and return the maximum number
of snacks that can be included.

This problem models real-time monitoring where the cart is scanned from left to right and
all sugar values are non-negative. Because of that property, an efficient sliding window
solution is expected.

Return 0 if no snack can be included without exceeding the limit.

Constraints:
- 1 <= sugars.length <= 100000
- 0 <= sugars[i] <= 100000
- 0 <= limit <= 1000000000
- All values in sugars are non-negative integers.

Important note about the examples:
The written explanations in the prompt contain inconsistencies:
- Example 1 says output 3, which is correct because [2, 1, 1] has sum 4.
- Example 2 says output 3 at first, but then correctly explains that [2, 2, 1] sums to 5,
  which is too large, so the true answer is 2.

This solution follows the mathematically correct interpretation of the problem.
"""

from typing import List


class Solution:
    def longest_snack_run(self, sugars: List[int], limit: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most limit.

        Because all sugar values are non-negative, we can use the sliding window technique:
        - Expand the right side of the window to include more snacks.
        - If the sum becomes too large, move the left side rightward until the sum is valid again.
        - Track the largest valid window length seen.

        Args:
            sugars: A list of non-negative integers representing sugar amounts.
            limit: The maximum allowed total sugar for a contiguous segment.

        Returns:
            The length of the longest contiguous segment with sum <= limit.

        Time complexity:
            O(n), where n is the length of sugars, because each index is moved at most once
            by the left pointer and once by the right pointer.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The left pointer marks the beginning of the current window.
        # The right pointer will be provided by the loop as we scan from left to right.
        left: int = 0

        # current_sum stores the total sugar inside the current window sugars[left:right+1].
        current_sum: int = 0

        # best_length stores the maximum valid window size found so far.
        best_length: int = 0

        # We expand the window one element at a time by moving the right pointer.
        for right, value in enumerate(sugars):
            # Add the new rightmost snack into the current window sum.
            current_sum += value

            # If the window sum is too large, we must shrink from the left.
            # This works correctly only because all numbers are non-negative:
            # removing items from the left can only keep the sum the same or decrease it.
            while current_sum > limit and left <= right:
                current_sum -= sugars[left]
                left += 1

            # At this point, the window is guaranteed to be valid:
            # current_sum <= limit
            #
            # So we compute its length and update the answer if this window is longer.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        return best_length

    def longestSubarrayWithinLimit(self, sugars: List[int], limit: int) -> int:
        """
        Wrapper method that calls the main sliding window implementation.

        This extra method is included to provide an alternative descriptive name while still
        keeping the main logic in a single place.

        Args:
            sugars: A list of non-negative integers representing sugar amounts.
            limit: The maximum allowed total sugar for a contiguous segment.

        Returns:
            The length of the longest contiguous segment with sum <= limit.

        Time complexity:
            O(n), where n is the length of sugars.

        Space complexity:
            O(1).
        """
        return self.longest_snack_run(sugars, limit)


if __name__ == "__main__":
    solution = Solution()

    # Sample 1:
    # sugars = [2, 1, 3, 2, 1, 1], limit = 5
    # Valid longest segment is [2, 1, 1] with sum 4 and length 3.
    sugars1: List[int] = [2, 1, 3, 2, 1, 1]
    limit1: int = 5
    result1: int = solution.longest_snack_run(sugars1, limit1)
    print(f"Sample 1 result: {result1}")  # Expected: 3

    # Sample 2:
    # sugars = [6, 2, 2, 1], limit = 4
    # The first element alone is too large.
    # [2, 2] has sum 4 and length 2.
    # [2, 1] has sum 3 and length 2.
    # [2, 2, 1] has sum 5, so it is invalid.
    # Therefore the correct answer is 2.
    sugars2: List[int] = [6, 2, 2, 1]
    limit2: int = 4
    result2: int = solution.longest_snack_run(sugars2, limit2)
    print(f"Sample 2 result: {result2}")  # Expected: 2

    # Additional edge case:
    # limit = 0 and array contains zeros.
    # The longest valid segment is the longest run whose total remains 0.
    sugars3: List[int] = [0, 0, 1, 0, 0]
    limit3: int = 0
    result3: int = solution.longest_snack_run(sugars3, limit3)
    print(f"Edge case result: {result3}")  # Expected: 2

    # Additional edge case:
    # No single snack fits.
    sugars4: List[int] = [5, 6, 7]
    limit4: int = 4
    result4: int = solution.longest_snack_run(sugars4, limit4)
    print(f"No valid snack result: {result4}")  # Expected: 0