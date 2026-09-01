"""
Title: Longest Checkout Span With Gift Card Balance Floor

Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an integer array transactions where transactions[i] represents the net effect
of the i-th checkout event on a customer's gift card balance. A positive value means money
was added to the card, and a negative value means money was spent.

The customer starts with an initial gift card balance startBalance. You want to find the
longest contiguous span of checkout events that could be processed in order such that, at
every point inside that span, the running balance never drops below 0.

Formally, for a subarray transactions[l...r], define the running balance inside the span as
startBalance plus the prefix sum of that subarray up to each position. The span is valid if
for every index k between l and r, the balance after processing transactions[l...k] is at
least 0.

Return the length of the longest valid contiguous span.

This is a sliding window problem: as you expand the right end of the window, the window may
become invalid because some prefix inside the current window causes the balance to go
negative. You must then shrink the left end until the window becomes valid again.

Constraints:
- 1 <= transactions.length <= 200000
- -100000 <= transactions[i] <= 100000
- 0 <= startBalance <= 1000000000

Example 1:
Input: transactions = [4, -3, -2, 5, -1], startBalance = 2
Output: 5

Example 2:
Input: transactions = [-4, 3, -2, -1, 2], startBalance = 2
Output: 4
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_valid_span(self, transactions: List[int], startBalance: int) -> int:
        """
        Find the length of the longest contiguous subarray whose running balance
        never drops below zero when started with startBalance.

        The key idea is to convert the condition on a window [left..right] into a
        condition on prefix sums:
            Let prefix[i] = sum(transactions[0..i-1]), with prefix[0] = 0.
            For window [left..right], every internal running balance is:
                startBalance + (prefix[k + 1] - prefix[left]) for k in [left..right]
            This window is valid iff:
                min(prefix[left + 1], ..., prefix[right + 1]) - prefix[left] >= -startBalance
            Rearranged:
                min(prefix[left + 1], ..., prefix[right + 1]) >= prefix[left] - startBalance

        We maintain:
        - a sliding window left..right over transactions
        - a monotonic increasing deque of prefix indices for the range [left + 1 .. right + 1]
          so the front always gives the minimum prefix value inside the current window

        As we expand right, we add prefix[right + 1] into the deque.
        If the window becomes invalid, we move left forward until it becomes valid again.

        Args:
            transactions: List of integer balance changes.
            startBalance: Initial balance available before processing a chosen span.

        Returns:
            The maximum length of a valid contiguous span.

        Time complexity:
            O(n), because each index is added to and removed from the deque at most once.

        Space complexity:
            O(n), for the prefix sums and deque in the worst case.
        """
        n: int = len(transactions)

        # Build prefix sums where:
        # prefix[0] = 0
        # prefix[i] = sum of first i transactions
        #
        # This lets us compute sums of any subarray quickly and, more importantly here,
        # reason about the minimum running sum inside a window using prefix values.
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + transactions[i]

        # This deque will store indices into the prefix array.
        #
        # Important invariant:
        # - It only contains indices from the current valid candidate range [left + 1 .. right + 1]
        # - The prefix values at those indices are in nondecreasing order
        #
        # Because of that, deque[0] always points to the minimum prefix value in the current window.
        min_prefix_indices: Deque[int] = deque()

        left: int = 0
        best: int = 0

        # Expand the right end of the window one transaction at a time.
        for right in range(n):
            current_prefix_index: int = right + 1

            # Insert prefix[right + 1] into the monotonic deque.
            #
            # Why pop from the back while the new prefix is smaller or equal?
            # Because any larger/equal prefix behind it can never become the minimum
            # for this or any future window that also contains the new index.
            #
            # This is the standard monotonic queue technique for tracking a sliding minimum.
            while (
                min_prefix_indices
                and prefix[min_prefix_indices[-1]] >= prefix[current_prefix_index]
            ):
                min_prefix_indices.pop()

            min_prefix_indices.append(current_prefix_index)

            # Now ensure the current window [left..right] is valid.
            #
            # Validity condition:
            #   minimum prefix value among indices [left + 1 .. right + 1]
            #   must be at least prefix[left] - startBalance
            #
            # If not, then somewhere inside the window the running balance dips below zero,
            # so we must move left forward until the condition is restored.
            while min_prefix_indices and prefix[min_prefix_indices[0]] < prefix[left] - startBalance:
                # We are about to remove transaction at index 'left' from the window.
                # After incrementing left, the allowed prefix index range becomes [left + 1 .. right + 1].
                #
                # If the deque front equals left + 1, that prefix index is no longer inside
                # the new window after left moves, so we must discard it.
                if min_prefix_indices and min_prefix_indices[0] == left + 1:
                    min_prefix_indices.popleft()

                left += 1

            # At this point the window [left..right] is valid.
            # Update the best length found so far.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    transactions1: List[int] = [4, -3, -2, 5, -1]
    start_balance1: int = 2
    result1: int = solution.longest_valid_span(transactions1, start_balance1)
    print("Example 1:")
    print(f"transactions = {transactions1}")
    print(f"startBalance = {start_balance1}")
    print(f"Longest valid span length = {result1}")
    print("Expected = 5")
    print()

    # Example 2
    transactions2: List[int] = [-4, 3, -2, -1, 2]
    start_balance2: int = 2
    result2: int = solution.longest_valid_span(transactions2, start_balance2)
    print("Example 2:")
    print(f"transactions = {transactions2}")
    print(f"startBalance = {start_balance2}")
    print(f"Longest valid span length = {result2}")
    print("Expected = 4")
    print()

    # Additional quick checks
    extra_cases: List[tuple[List[int], int]] = [
        ([1], 0),
        ([-1], 0),
        ([-1], 1),
        ([2, -1, -1, -1, 5], 1),
        ([0, 0, 0], 0),
    ]

    print("Additional checks:")
    for arr, bal in extra_cases:
        print(
            f"transactions = {arr}, startBalance = {bal}, "
            f"answer = {solution.longest_valid_span(arr, bal)}"
        )