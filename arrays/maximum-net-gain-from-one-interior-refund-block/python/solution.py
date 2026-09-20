"""
Title: Maximum Net Gain from One Interior Refund Block

Problem Description:
You are given an integer array transactions where transactions[i] represents the net
profit or loss from the i-th transaction of a day. Positive values are profits and
negative values are losses. You are also given an integer baseGain, representing a
fixed gain that is always counted.

You may choose exactly one contiguous interior block of transactions to mark as
refunded. Refunding a block means its total contribution is subtracted from the day's
result instead of added. In other words, if the chosen block has sum S, the final
result becomes:

    baseGain + totalSum(transactions) - 2 * S

However, the refunded block must be an interior block:
- it cannot start at index 0
- it cannot end at index n - 1
- it must contain at least one element

Return the maximum possible final result after choosing one valid interior block.

This means we want to maximize:
    baseGain + totalSum(transactions) - 2 * refunded_sum

Since baseGain and totalSum(transactions) are fixed, maximizing the final result is
equivalent to minimizing the sum of the chosen interior subarray.

So the task becomes:
- Find the minimum-sum contiguous subarray inside transactions[1 : n - 1]
- Then compute:
    answer = baseGain + totalSum(transactions) - 2 * minimum_interior_subarray_sum

Constraints:
- 3 <= transactions.length <= 200000
- -10^9 <= transactions[i] <= 10^9
- -10^9 <= baseGain <= 10^9
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def max_net_gain(self, transactions: List[int], baseGain: int) -> int:
        """
        Compute the maximum possible final result after refunding exactly one valid
        interior contiguous block.

        The key observation is:
        final_result = baseGain + total_sum - 2 * refunded_block_sum

        Because baseGain and total_sum are fixed, we maximize final_result by choosing
        the interior block with the minimum possible sum.

        Args:
            transactions: List of transaction profits/losses.
            baseGain: Fixed gain always included in the result.

        Returns:
            The maximum achievable final result.

        Time complexity:
            O(n), where n is the length of transactions.

        Space complexity:
            O(1), excluding input storage.
        """
        # First, compute the total sum of the entire array.
        # This value is part of the final formula and does not depend on which
        # interior block we choose.
        total_sum: int = sum(transactions)

        # The refunded block must be strictly interior, so it must lie completely
        # inside indices [1, n - 2].
        #
        # We now need the minimum-sum contiguous subarray in that interior range.
        #
        # This is a classic variation of Kadane's algorithm:
        # - Standard Kadane finds a maximum-sum subarray.
        # - Here we adapt it to find a minimum-sum subarray.
        #
        # Since n >= 3, there is always at least one valid interior element.
        # The smallest possible interior range is exactly one element when n == 3.
        n: int = len(transactions)

        # Initialize the running minimum-subarray sum ending at the current position,
        # and the best (global) minimum found so far, using the first interior element.
        #
        # Why initialize this way?
        # Because the refunded block must contain at least one element, so we cannot
        # start from 0 or use an "empty subarray" trick.
        current_min_ending_here: int = transactions[1]
        best_min_subarray_sum: int = transactions[1]

        # Iterate through the remaining interior elements only.
        # We stop at n - 2 inclusive, because index n - 1 is not allowed in the block.
        for i in range(2, n - 1):
            value: int = transactions[i]

            # For a minimum-sum subarray ending at index i, we have two choices:
            #
            # 1. Start a new subarray at i
            #    -> sum = value
            #
            # 2. Extend the previous minimum-sum subarray ending at i - 1
            #    -> sum = current_min_ending_here + value
            #
            # We choose the smaller of the two, because we want the minimum sum.
            current_min_ending_here = min(value, current_min_ending_here + value)

            # Update the global best minimum if the current ending subarray is better.
            best_min_subarray_sum = min(best_min_subarray_sum, current_min_ending_here)

        # Once we know the minimum interior subarray sum, plug it into the formula.
        #
        # final_result = baseGain + total_sum - 2 * refunded_sum
        #
        # Since refunded_sum is as small as possible, this gives the maximum result.
        result: int = baseGain + total_sum - 2 * best_min_subarray_sum
        return result

    def solve(self, transactions: List[int], baseGain: int) -> int:
        """
        Wrapper method matching the problem requirement for algorithm method(s).

        Args:
            transactions: List of transaction profits/losses.
            baseGain: Fixed gain always included in the result.

        Returns:
            The maximum achievable final result.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.max_net_gain(transactions, baseGain)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # transactions = [4, -7, 3, -2, 5], baseGain = 10
    #
    # totalSum = 4 + (-7) + 3 + (-2) + 5 = 3
    #
    # Valid interior subarrays are inside indices [1..3]:
    # [-7] -> sum = -7
    # [-7, 3] -> -4
    # [-7, 3, -2] -> -6
    # [3] -> 3
    # [3, -2] -> 1
    # [-2] -> -2
    #
    # Minimum interior sum = -7
    # Final result = 10 + 3 - 2 * (-7) = 27
    transactions1 = [4, -7, 3, -2, 5]
    baseGain1 = 10
    print(solution.solve(transactions1, baseGain1))  # Expected: 27

    # Example 2 from the prompt:
    # transactions = [8, 2, 6], baseGain = -5
    #
    # The only valid interior block is [2].
    # totalSum = 8 + 2 + 6 = 16
    # Final result = -5 + 16 - 2 * 2 = 7
    transactions2 = [8, 2, 6]
    baseGain2 = -5
    print(solution.solve(transactions2, baseGain2))  # Expected: 7

    # Additional quick sanity checks:
    # Interior is [1, 2] only, best refunded block is the minimum subarray there.
    transactions3 = [5, -1, -2, 4]
    baseGain3 = 0
    # total = 6, min interior subarray in [-1, -2] is -3, answer = 6 - 2*(-3) = 12
    print(solution.solve(transactions3, baseGain3))  # Expected: 12

    transactions4 = [1, 10, 20, 2]
    baseGain4 = 3
    # total = 33, interior [10, 20], min interior subarray is 10, answer = 3 + 33 - 20 = 16
    print(solution.solve(transactions4, baseGain4))  # Expected: 16