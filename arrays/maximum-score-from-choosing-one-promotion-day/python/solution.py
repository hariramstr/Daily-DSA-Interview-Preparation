"""
Title: Maximum Score from Choosing One Promotion Day

Problem Description:
You are given an integer array sales where sales[i] represents the net revenue earned on day i.
A company wants to run exactly one special promotion on a single day p.

After choosing p, the total promotion score is defined as the sum of two parts:
1. leftScore  = maximum sum of a non-empty contiguous subarray that ends at day p
2. rightScore = maximum sum of a non-empty contiguous subarray that starts at day p

Since day p belongs to both streaks, its value is counted only once in the final score.

Formally:
- leftScore = maximum sum of a non-empty subarray that ends at p
- rightScore = maximum sum of a non-empty subarray that starts at p
- promotionScore = leftScore + rightScore - sales[p]

Return the maximum possible promotionScore over all valid choices of p.

This models selecting a single promotion day that can benefit from momentum built before it
and customer interest continuing after it. The chosen day must be included in both segments,
but the segments may each have length 1.

Constraints:
- 1 <= sales.length <= 200000
- -1000000000 <= sales[i] <= 1000000000
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def max_promotion_score(self, sales: List[int]) -> int:
        """
        Compute the maximum promotion score over all possible promotion days.

        The key idea is to precompute:
        - best_end_here[i]: maximum sum of a non-empty subarray ending exactly at i
        - best_start_here[i]: maximum sum of a non-empty subarray starting exactly at i

        Then for each index i:
        promotionScore(i) = best_end_here[i] + best_start_here[i] - sales[i]

        Args:
            sales: List of daily net revenues.

        Returns:
            The maximum possible promotion score.

        Time complexity:
            O(n), where n is the length of sales.

        Space complexity:
            O(n), for the two helper arrays.
        """
        n: int = len(sales)

        # Edge case:
        # If there is only one day, then the only possible promotion day is index 0.
        # Both the best subarray ending there and starting there are just [sales[0]].
        # So the score is sales[0] + sales[0] - sales[0] = sales[0].
        if n == 1:
            return sales[0]

        # ------------------------------------------------------------
        # Step 1: Compute best_end_here
        # ------------------------------------------------------------
        # best_end_here[i] means:
        # "What is the maximum sum of any non-empty contiguous subarray
        #  that MUST end exactly at index i?"
        #
        # This is a classic Kadane-style DP transition:
        #
        # best_end_here[i] = max(
        #     sales[i],                    # start a brand-new subarray at i
        #     best_end_here[i - 1] + sales[i]   # extend the best one ending at i-1
        # )
        #
        # Why does this work?
        # Any subarray ending at i either:
        # - consists only of sales[i], or
        # - is some subarray ending at i-1 extended by sales[i]
        #
        # We choose the better of those two possibilities.
        best_end_here: List[int] = [0] * n
        best_end_here[0] = sales[0]

        for i in range(1, n):
            # Option 1: start fresh at current day
            start_new: int = sales[i]

            # Option 2: extend the best streak that ended yesterday
            extend_previous: int = best_end_here[i - 1] + sales[i]

            # Store the better choice
            best_end_here[i] = max(start_new, extend_previous)

        # ------------------------------------------------------------
        # Step 2: Compute best_start_here
        # ------------------------------------------------------------
        # best_start_here[i] means:
        # "What is the maximum sum of any non-empty contiguous subarray
        #  that MUST start exactly at index i?"
        #
        # This is the symmetric version of the previous DP, but computed
        # from right to left:
        #
        # best_start_here[i] = max(
        #     sales[i],                     # start and end at i only
        #     sales[i] + best_start_here[i + 1]  # extend into the future
        # )
        #
        # Why right-to-left?
        # Because a subarray starting at i can either:
        # - be just sales[i], or
        # - include sales[i] and then continue with the best subarray
        #   that starts at i+1
        best_start_here: List[int] = [0] * n
        best_start_here[n - 1] = sales[n - 1]

        for i in range(n - 2, -1, -1):
            # Option 1: use only the current day
            start_only_here: int = sales[i]

            # Option 2: continue into the best streak starting tomorrow
            extend_right: int = sales[i] + best_start_here[i + 1]

            # Store the better choice
            best_start_here[i] = max(start_only_here, extend_right)

        # ------------------------------------------------------------
        # Step 3: Try every promotion day
        # ------------------------------------------------------------
        # For each day i:
        # - leftScore  = best_end_here[i]
        # - rightScore = best_start_here[i]
        #
        # Since sales[i] is included in both scores, subtract it once.
        #
        # promotionScore = best_end_here[i] + best_start_here[i] - sales[i]
        #
        # We take the maximum over all i.
        answer: int = -(10**30)

        for i in range(n):
            current_score: int = best_end_here[i] + best_start_here[i] - sales[i]
            if current_score > answer:
                answer = current_score

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    sales1: List[int] = [4, -2, 3, -1, 5]
    result1: int = solution.max_promotion_score(sales1)
    print("Input:", sales1)
    print("Output:", result1)
    print("Expected:", 9)
    print()

    # Manual verification for Example 1:
    # p = 2, sales[2] = 3
    # best ending at 2 = [4, -2, 3] => 5
    # best starting at 2 = [3, -1, 5] => 7
    # score = 5 + 7 - 3 = 9

    # Example 2
    sales2: List[int] = [-5, -2, -7]
    result2: int = solution.max_promotion_score(sales2)
    print("Input:", sales2)
    print("Output:", result2)
    print("Expected:", -2)
    print()

    # Manual verification for Example 2:
    # All values are negative, so the best choice is the least negative single day.
    # Choose p = 1, sales[1] = -2
    # leftScore = -2
    # rightScore = -2
    # score = -2 + (-2) - (-2) = -2

    # Additional quick sanity checks
    sales3: List[int] = [7]
    result3: int = solution.max_promotion_score(sales3)
    print("Input:", sales3)
    print("Output:", result3)
    print("Expected:", 7)
    print()

    sales4: List[int] = [1, 2, 3]
    result4: int = solution.max_promotion_score(sales4)
    print("Input:", sales4)
    print("Output:", result4)
    print("Expected:", 6)
    print()

    # For [1, 2, 3], choosing p = 1 gives:
    # best ending at 1 = [1, 2] => 3
    # best starting at 1 = [2, 3] => 5
    # score = 3 + 5 - 2 = 6
    #
    # choosing p = 2 gives:
    # best ending at 2 = [1, 2, 3] => 6
    # best starting at 2 = [3] => 3
    # score = 6 + 3 - 3 = 6
    #
    # So the maximum is 6.