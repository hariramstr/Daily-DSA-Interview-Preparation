"""
Title: Maximum Net Gain from One Interior Coupon Burst

Problem Description:
You are given an integer array purchases where purchases[i] represents the net profit
contributed by the i-th customer order during a day. Values may be positive, zero,
or negative.

The marketing team can run exactly one special coupon burst on a contiguous block
of orders, but the burst is only allowed on an interior block: it cannot include
the first order or the last order of the day.

If the team chooses a block purchases[l..r] with:
    1 <= l <= r <= n - 2

the campaign score is defined as:
    sum(purchases[l..r]) - purchases[l - 1] - purchases[r + 1]

Your task is to return the maximum possible campaign score over all valid interior
blocks. The chosen block must be non-empty, and the array must be long enough to
have at least one valid interior block.

Constraints:
- 3 <= purchases.length <= 200000
- -1000000000 <= purchases[i] <= 1000000000
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def maximum_net_gain(self, purchases: List[int]) -> int:
        """
        Compute the maximum campaign score over all valid interior contiguous blocks.

        The score for choosing purchases[l..r] is:
            sum(purchases[l..r]) - purchases[l - 1] - purchases[r + 1]
        where 1 <= l <= r <= n - 2.

        Args:
            purchases: List of integer order profits/losses.

        Returns:
            The maximum possible campaign score as an integer.

        Time complexity:
            O(n), where n is the length of purchases.

        Space complexity:
            O(1), excluding the input array.
        """
        n: int = len(purchases)

        # We are guaranteed n >= 3 by the problem constraints, so there is always
        # at least one valid interior block. For example, when n == 3, the only
        # valid block is the single element at index 1.
        #
        # We want to maximize:
        #     sum(purchases[l..r]) - purchases[l - 1] - purchases[r + 1]
        #
        # A very useful algebraic rewrite is:
        #     (sum(purchases[l..r]) - purchases[l - 1]) - purchases[r + 1]
        #
        # If we process the array from left to right and imagine that we are fixing
        # the right endpoint r, then:
        #
        #     score(l, r) = [sum(purchases[l..r]) - purchases[l - 1]] - purchases[r + 1]
        #
        # Let prefix sums be:
        #     prefix[i] = purchases[0] + purchases[1] + ... + purchases[i - 1]
        #
        # Then:
        #     sum(purchases[l..r]) = prefix[r + 1] - prefix[l]
        #
        # So:
        #     score(l, r)
        #       = (prefix[r + 1] - prefix[l]) - purchases[l - 1] - purchases[r + 1]
        #       = prefix[r + 1] - (prefix[l] + purchases[l - 1]) - purchases[r + 1]
        #
        # For a fixed r, the only part depending on l is:
        #     prefix[l] + purchases[l - 1]
        #
        # Therefore, for each valid r, we want the minimum value of:
        #     prefix[l] + purchases[l - 1]
        # over all valid l such that 1 <= l <= r.
        #
        # This means we can scan r from left to right while maintaining the best
        # (smallest) value of prefix[l] + purchases[l - 1] seen so far.
        #
        # That gives an O(n) solution with O(1) extra space.

        # prefix_before_current will store prefix[r], then after adding purchases[r]
        # it becomes prefix[r + 1].
        prefix_before_current: int = purchases[0]

        # Before we start evaluating right endpoints r, we need to initialize the
        # best left-side helper value for l = 1, because the smallest valid l is 1.
        #
        # For l = 1:
        #     prefix[l] + purchases[l - 1]
        #   = prefix[1] + purchases[0]
        #   = purchases[0] + purchases[0]
        #   = 2 * purchases[0]
        #
        # More generally, during the scan we will update this as new l values become
        # available.
        best_left_term: int = 2 * purchases[0]

        # Initialize the answer to a very small number so any valid score replaces it.
        # We use Python's large integer support safely; the final answer still fits
        # in signed 64-bit as guaranteed by the problem.
        answer: int = -(10 ** 30)

        # The right endpoint r must satisfy:
        #     1 <= r <= n - 2
        #
        # because the chosen block must be interior and non-empty, and we need r + 1
        # to exist as the right outside neighbor.
        for r in range(1, n - 1):
            # Update prefix_before_current so it becomes prefix[r + 1].
            # Before this line:
            #     prefix_before_current = prefix[r]
            # After this line:
            #     prefix_before_current = prefix[r + 1]
            prefix_before_current += purchases[r]

            # At this moment, best_left_term should represent the minimum of:
            #     prefix[l] + purchases[l - 1]
            # for all valid l in [1, r].
            #
            # Then the best score ending at this r is:
            #     prefix[r + 1] - best_left_term - purchases[r + 1]
            current_score: int = prefix_before_current - best_left_term - purchases[r + 1]

            # Update the global maximum answer.
            if current_score > answer:
                answer = current_score

            # Now prepare for the next iteration, where the next right endpoint will
            # be r + 1. That next iteration allows l to be as large as r + 1.
            #
            # So we need to consider the new candidate l = r + 1, but only if
            # r + 1 <= n - 2. In practice, updating here is harmless even on the
            # last loop iteration because the value simply won't be used afterward.
            #
            # For l = r + 1:
            #     prefix[l] + purchases[l - 1]
            #   = prefix[r + 1] + purchases[r]
            new_left_term: int = prefix_before_current + purchases[r]

            # Keep the minimum such term seen so far, because a smaller subtraction
            # leads to a larger score.
            if new_left_term < best_left_term:
                best_left_term = new_left_term

        return answer

    def solve(self, purchases: List[int]) -> int:
        """
        Convenience wrapper around the main algorithm.

        Args:
            purchases: List of integer order profits/losses.

        Returns:
            The maximum possible campaign score.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.maximum_net_gain(purchases)


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [4, 2, 7, -3, 5]
    sample_2: List[int] = [3, -5, 6, 1, -2, 4]

    result_1: int = solution.solve(sample_1)
    result_2: int = solution.solve(sample_2)

    print("Sample 1:", sample_1)
    print("Maximum net gain:", result_1)
    print("Expected:", 8)
    print()

    print("Sample 2:", sample_2)
    print("Maximum net gain:", result_2)
    print("Expected:", 10)
    print()

    # Additional small sanity check:
    # For [a, b, c], the only valid interior block is [b], so the score is b - a - c.
    sample_3: List[int] = [10, 5, 1]
    result_3: int = solution.solve(sample_3)
    print("Sample 3:", sample_3)
    print("Maximum net gain:", result_3)
    print("Expected:", -6)