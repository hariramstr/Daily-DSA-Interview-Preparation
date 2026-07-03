"""
Title: Maximum Donation Sum from Skipping Adjacent Booths

Problem Description:
You are organizing a charity fair with a row of donation booths. Each booth has a
non-negative integer amount representing how much money can be collected from that booth.
However, due to staffing limits, you cannot operate two adjacent booths on the same day.
If you open booth i, then booths i - 1 and i + 1 must remain closed.

Your task is to return the maximum total donation amount that can be collected from the
row of booths while following this rule.

This is a classic one-dimensional decision problem: for each booth, you can either skip it
or open it, but opening it prevents using the previous booth. An efficient solution should
use dynamic programming to build the best answer from smaller prefixes of the array.

Constraints:
- 1 <= booths.length <= 100
- 0 <= booths[i] <= 1000

Input format:
- An integer array `booths` where `booths[i]` is the donation amount available at booth `i`.

Output format:
- Return a single integer: the maximum donation sum obtainable without choosing adjacent booths.

Example 1:
Input: booths = [5, 1, 2, 10, 6]
Output: 15
Explanation: The best valid choice is 5 + 10 = 15. Choosing 5 + 2 + 6 gives 13.

Example 2:
Input: booths = [4, 7, 3, 9]
Output: 16
Explanation: The best choice is booths with amounts 7 and 9. They are not adjacent,
so the total is 16.
"""

from typing import List


class Solution:
    def max_donation(self, booths: List[int]) -> int:
        """
        Compute the maximum donation sum without choosing adjacent booths.

        This uses dynamic programming. For each booth, we decide between:
        1. Skipping the current booth, keeping the best total so far
        2. Taking the current booth, which means we must add it to the best total
           from two booths earlier

        Args:
            booths: A list of non-negative integers where each value is the donation
                amount available at that booth.

        Returns:
            The maximum total donation that can be collected without selecting
            two adjacent booths.

        Time complexity:
            O(n), where n is the number of booths.

        Space complexity:
            O(1), because we only store the last two dynamic programming states.
        """
        # If the list is empty, there is nothing to collect.
        # The problem guarantees at least one booth, but this check makes the method
        # more robust and beginner-friendly.
        if not booths:
            return 0

        # We maintain two rolling values instead of a full DP array:
        #
        # prev_two:
        #   The best donation total we can achieve considering booths up to index i - 2.
        #
        # prev_one:
        #   The best donation total we can achieve considering booths up to index i - 1.
        #
        # Why this works:
        # For the current booth value `amount`, the recurrence is:
        #   current = max(prev_one, prev_two + amount)
        #
        # Explanation of the recurrence:
        # - If we skip the current booth, our best total stays `prev_one`.
        # - If we take the current booth, we cannot take the previous booth,
        #   so we add `amount` to `prev_two`.
        #
        # We start with both values at 0 because before processing any booths,
        # the best donation total is 0.
        prev_two: int = 0
        prev_one: int = 0

        # Process each booth from left to right.
        # At every step, we compute the best answer for the prefix ending at this booth.
        for index, amount in enumerate(booths):
            # Option 1: skip the current booth.
            # Then the best total remains whatever we had up to the previous booth.
            skip_current: int = prev_one

            # Option 2: take the current booth.
            # Because adjacent booths cannot both be chosen, we add the current amount
            # to the best total from two positions back.
            take_current: int = prev_two + amount

            # Choose the better of the two options.
            current_best: int = max(skip_current, take_current)

            # Move the rolling window forward:
            # - The old prev_one becomes the new prev_two
            # - The newly computed current_best becomes the new prev_one
            prev_two = prev_one
            prev_one = current_best

            # Detailed trace comments for understanding:
            # After processing booth `index`:
            # - prev_one stores the best answer for booths[0:index+1]
            # - prev_two stores the best answer for booths[0:index]
            #
            # Example for booths = [5, 1, 2, 10, 6]:
            # index 0, amount 5:
            #   skip = 0, take = 0 + 5 = 5, best = 5
            # index 1, amount 1:
            #   skip = 5, take = 0 + 1 = 1, best = 5
            # index 2, amount 2:
            #   skip = 5, take = 5 + 2 = 7, best = 7
            # index 3, amount 10:
            #   skip = 7, take = 5 + 10 = 15, best = 15
            # index 4, amount 6:
            #   skip = 15, take = 7 + 6 = 13, best = 15
            # Final answer = 15
            #
            # Example for booths = [4, 7, 3, 9]:
            # index 0, amount 4:
            #   skip = 0, take = 4, best = 4
            # index 1, amount 7:
            #   skip = 4, take = 7, best = 7
            # index 2, amount 3:
            #   skip = 7, take = 4 + 3 = 7, best = 7
            # index 3, amount 9:
            #   skip = 7, take = 7 + 9 = 16, best = 16
            # Final answer = 16

        # At the end of the loop, prev_one contains the best possible donation total
        # for the entire list of booths.
        return prev_one

    def rob(self, booths: List[int]) -> int:
        """
        Provide an alternative common method name for the same problem.

        This simply calls the main dynamic programming method so that the solution
        remains flexible if a platform expects a different method name.

        Args:
            booths: A list of non-negative integers representing booth donations.

        Returns:
            The maximum donation sum obtainable without selecting adjacent booths.

        Time complexity:
            O(n), where n is the number of booths.

        Space complexity:
            O(1).
        """
        return self.max_donation(booths)


if __name__ == "__main__":
    # Create a Solution instance.
    solution = Solution()

    # Sample input 1 from the problem statement.
    booths1: List[int] = [5, 1, 2, 10, 6]
    result1: int = solution.max_donation(booths1)
    print(f"Booths: {booths1}")
    print(f"Maximum donation sum: {result1}")
    print("Expected: 15")
    print()

    # Sample input 2 from the problem statement.
    booths2: List[int] = [4, 7, 3, 9]
    result2: int = solution.max_donation(booths2)
    print(f"Booths: {booths2}")
    print(f"Maximum donation sum: {result2}")
    print("Expected: 16")
    print()

    # Additional beginner-friendly test cases.
    booths3: List[int] = [2]
    result3: int = solution.max_donation(booths3)
    print(f"Booths: {booths3}")
    print(f"Maximum donation sum: {result3}")
    print("Expected: 2")
    print()

    booths4: List[int] = [2, 1]
    result4: int = solution.max_donation(booths4)
    print(f"Booths: {booths4}")
    print(f"Maximum donation sum: {result4}")
    print("Expected: 2")
    print()

    booths5: List[int] = [2, 7, 9, 3, 1]
    result5: int = solution.max_donation(booths5)
    print(f"Booths: {booths5}")
    print(f"Maximum donation sum: {result5}")
    print("Expected: 12")