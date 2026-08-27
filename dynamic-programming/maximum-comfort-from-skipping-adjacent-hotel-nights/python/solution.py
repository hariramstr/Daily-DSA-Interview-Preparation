"""
Title: Maximum Comfort from Skipping Adjacent Hotel Nights

Problem Description:
You are planning a road trip with a list of hotel options, one for each night of the trip.
The i-th hotel gives you a comfort score represented by comfort[i]. Because moving luggage
and checking in on back-to-back nights is too exhausting, you are not allowed to stay in
hotels on two adjacent nights. You may choose any set of nights to book, as long as no two
chosen nights are consecutive.

Return the maximum total comfort score you can get.

This is a classic decision-style dynamic programming problem: for each night, you can either
skip that hotel and keep the best score from previous nights, or book it and add its comfort
score to the best result that ends at least one night earlier.

Constraints:
- 1 <= comfort.length <= 100
- 0 <= comfort[i] <= 1000

Example 1:
Input: comfort = [6, 7, 1, 30, 8, 2, 4]
Output: 41
Explanation: One optimal choice is nights with comfort 7, 30, and 4. Their total is 41.
You cannot take 6 and 7 together because those nights are adjacent.

Example 2:
Input: comfort = [5, 1, 1, 5]
Output: 10
Explanation: Choose the first and last nights. The total comfort is 5 + 5 = 10.
"""

from typing import List


class Solution:
    def max_comfort(self, comfort: List[int]) -> int:
        """
        Compute the maximum total comfort score when no two chosen nights are adjacent.

        This uses dynamic programming with O(1) extra space. At each night, we decide
        between:
        1. Skipping the current hotel, keeping the best total seen so far
        2. Taking the current hotel, adding its comfort to the best total from two nights ago

        Args:
            comfort: A list where comfort[i] is the comfort score of the hotel on night i.

        Returns:
            The maximum total comfort score possible without choosing adjacent nights.

        Time complexity:
            O(n), where n is the number of nights

        Space complexity:
            O(1), because we only store the last two DP states
        """
        # We maintain two rolling dynamic programming values instead of a full DP array.
        #
        # prev_two:
        #   The best answer considering nights up to index i - 2.
        #   In other words, this is the value we are allowed to combine with the current
        #   night if we decide to book the current hotel, because adjacent nights are not allowed.
        #
        # prev_one:
        #   The best answer considering nights up to index i - 1.
        #   This is the value we keep if we decide to skip the current hotel.
        #
        # At each step for comfort value "value":
        #   take_current = prev_two + value
        #   skip_current = prev_one
        #   current_best = max(skip_current, take_current)
        #
        # Then we shift the window forward:
        #   prev_two becomes old prev_one
        #   prev_one becomes current_best
        #
        # This is the standard optimized DP pattern for the "non-adjacent selection" problem.

        prev_two: int = 0
        prev_one: int = 0

        # Process each night from left to right.
        for value in comfort:
            # Option 1: Skip this hotel.
            # If we skip the current night, the best total remains whatever the best total
            # was up to the previous night.
            skip_current: int = prev_one

            # Option 2: Take this hotel.
            # If we book the current hotel, we cannot book the previous one.
            # Therefore, we add the current comfort score to the best total from two nights ago.
            take_current: int = prev_two + value

            # Choose the better of the two valid options.
            current_best: int = max(skip_current, take_current)

            # Move the rolling DP window forward for the next iteration.
            prev_two = prev_one
            prev_one = current_best

        # After processing all nights, prev_one holds the best possible answer.
        return prev_one

    def solve(self, comfort: List[int]) -> int:
        """
        Wrapper method that solves the problem.

        Args:
            comfort: A list of hotel comfort scores.

        Returns:
            The maximum total comfort score without choosing adjacent nights.

        Time complexity:
            O(n), where n is the number of nights

        Space complexity:
            O(1)
        """
        return self.max_comfort(comfort)


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [6, 7, 1, 30, 8, 2, 4]
    result_1: int = solution.solve(sample_1)
    print(f"Input: {sample_1}")
    print(f"Output: {result_1}")
    print("Expected: 41")
    print()

    sample_2: List[int] = [5, 1, 1, 5]
    result_2: int = solution.solve(sample_2)
    print(f"Input: {sample_2}")
    print(f"Output: {result_2}")
    print("Expected: 10")
    print()

    # Additional beginner-friendly sanity checks
    sample_3: List[int] = [10]
    result_3: int = solution.solve(sample_3)
    print(f"Input: {sample_3}")
    print(f"Output: {result_3}")
    print("Expected: 10")
    print()

    sample_4: List[int] = [2, 1, 4, 9]
    result_4: int = solution.solve(sample_4)
    print(f"Input: {sample_4}")
    print(f"Output: {result_4}")
    print("Expected: 11")