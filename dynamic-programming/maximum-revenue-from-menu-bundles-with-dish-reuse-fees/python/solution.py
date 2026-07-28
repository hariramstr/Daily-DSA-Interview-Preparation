"""
Title: Maximum Revenue from Menu Bundles with Dish Reuse Fees

Problem Description:
A restaurant is preparing a fixed tasting menu over N evenings. On evening i, the chef
must choose exactly one dish from two available options: standard[i] or premium[i].
If the chef serves the same dish type on consecutive evenings, customers feel less
variety and a reuse fee is applied for that evening. Specifically, if the chef chooses
the same type on both evening i - 1 and evening i, then revenue for evening i is
reduced by fee[i].

You are given three integer arrays of length N:
- standard[i]: revenue earned if the standard dish is served on evening i
- premium[i]: revenue earned if the premium dish is served on evening i
- fee[i]: penalty applied only when the dish type on evening i matches the dish type
  chosen on evening i - 1

Return the maximum total revenue the restaurant can earn over all N evenings.

Notes:
- On the first evening, no reuse fee is ever applied.
- The reuse fee depends only on evening i, not on earlier history.
- Dish type means only standard or premium; the actual revenue values can be different
  each evening.

Constraints:
- 1 <= N <= 200000
- 0 <= standard[i], premium[i] <= 10^9
- 0 <= fee[i] <= 10^9
- Answer fits in a 64-bit signed integer

Key Idea:
At each evening, the only information from the past that matters is:
"What was the dish type chosen on the previous evening?"
That means dynamic programming with just two states is enough:
- best revenue ending today with standard
- best revenue ending today with premium
"""

from typing import List


class Solution:
    def max_revenue(self, standard: List[int], premium: List[int], fee: List[int]) -> int:
        """
        Compute the maximum total revenue over all evenings.

        We use dynamic programming with two rolling states:
        - dp_standard: best total revenue up to current evening if current evening uses standard
        - dp_premium: best total revenue up to current evening if current evening uses premium

        Transition logic for evening i:
        - If we choose standard today:
            1. Yesterday was standard -> pay fee[i]
            2. Yesterday was premium -> no fee
        - If we choose premium today:
            1. Yesterday was premium -> pay fee[i]
            2. Yesterday was standard -> no fee

        Args:
            standard: Revenue values if standard dish is chosen each evening.
            premium: Revenue values if premium dish is chosen each evening.
            fee: Reuse fee applied on evening i when the same dish type is chosen
                 as on evening i - 1.

        Returns:
            The maximum total revenue achievable.

        Time complexity:
            O(N), where N is the number of evenings.

        Space complexity:
            O(1), because we only keep the previous evening's two DP values.
        """
        n: int = len(standard)

        # Defensive validation for beginner-friendliness.
        # The problem guarantees valid input, but checking lengths makes the method
        # safer and easier to understand if reused elsewhere.
        if n == 0:
            return 0
        if len(premium) != n or len(fee) != n:
            raise ValueError("All input arrays must have the same length.")

        # Base case: evening 0 (the first evening).
        # No reuse fee can ever apply on the first evening because there is no
        # previous evening to compare against.
        #
        # So:
        # - If we end evening 0 with standard, total revenue is simply standard[0].
        # - If we end evening 0 with premium, total revenue is simply premium[0].
        dp_standard: int = standard[0]
        dp_premium: int = premium[0]

        # Process each later evening one by one.
        for i in range(1, n):
            # We compute the new best total if we choose standard on evening i.
            #
            # There are exactly two ways to arrive here:
            #
            # 1. Previous evening also used standard:
            #    Then today's revenue is standard[i] - fee[i]
            #    Total = old dp_standard + standard[i] - fee[i]
            #
            # 2. Previous evening used premium:
            #    Then no fee is charged today
            #    Total = old dp_premium + standard[i]
            #
            # We take the better of those two possibilities.
            next_standard: int = max(
                dp_standard + standard[i] - fee[i],
                dp_premium + standard[i],
            )

            # Similarly, compute the new best total if we choose premium on evening i.
            #
            # Again there are two possibilities:
            #
            # 1. Previous evening also used premium:
            #    Total = old dp_premium + premium[i] - fee[i]
            #
            # 2. Previous evening used standard:
            #    Total = old dp_standard + premium[i]
            #
            # Take the maximum.
            next_premium: int = max(
                dp_premium + premium[i] - fee[i],
                dp_standard + premium[i],
            )

            # Move the rolling DP window forward:
            # today's results become "previous" results for the next iteration.
            dp_standard = next_standard
            dp_premium = next_premium

        # At the end, the final evening can be either standard or premium.
        # We return the better of the two.
        return max(dp_standard, dp_premium)

    def solve(self, standard: List[int], premium: List[int], fee: List[int]) -> int:
        """
        Convenience wrapper around max_revenue.

        Args:
            standard: Revenue values for choosing standard each evening.
            premium: Revenue values for choosing premium each evening.
            fee: Reuse fee for choosing the same type on consecutive evenings.

        Returns:
            Maximum total revenue.

        Time complexity:
            O(N)

        Space complexity:
            O(1)
        """
        return self.max_revenue(standard, premium, fee)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # The prompt's final corrected explanation says the best answer is 30.
    standard_1: List[int] = [5, 6, 4, 7]
    premium_1: List[int] = [8, 3, 9, 2]
    fee_1: List[int] = [0, 4, 5, 3]
    result_1: int = solution.solve(standard_1, premium_1, fee_1)
    print("Example 1 result:", result_1)  # Expected: 30

    # Example 2 from the prompt.
    # The explanation computes 48, so that is the correct expected result.
    standard_2: List[int] = [10, 10, 1, 10]
    premium_2: List[int] = [1, 1, 20, 1]
    fee_2: List[int] = [0, 2, 8, 2]
    result_2: int = solution.solve(standard_2, premium_2, fee_2)
    print("Example 2 result:", result_2)  # Expected: 48