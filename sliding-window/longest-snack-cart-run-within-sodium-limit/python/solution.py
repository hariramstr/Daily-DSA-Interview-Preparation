"""
Title: Longest Snack Cart Run Within Sodium Limit

Problem Description:
A company cafeteria tracks the sodium content of snacks placed on a moving cart
throughout the day. You are given an integer array sodium where sodium[i] is the
sodium amount (in milligrams) of the i-th snack on the cart, in order. A customer
wants to choose one contiguous run of snacks, but the total sodium in that chosen
run must not exceed a given limit maxSodium.

Return the length of the longest contiguous subarray whose sum is less than or equal
to maxSodium. If no snack can be chosen without exceeding the limit, return 0.

This problem models a realistic streaming or sequential selection scenario where the
chosen items must stay contiguous and the total budget must remain bounded. An
efficient solution is expected because the list can be large. Since all sodium values
are non-negative, a sliding window approach can expand the right end of the window
and shrink the left end whenever the running sum becomes too large.

Constraints:
- 1 <= sodium.length <= 200000
- 0 <= sodium[i] <= 100000
- 0 <= maxSodium <= 1000000000
- All values fit in 32-bit integers, but using a 64-bit running sum is recommended.

Important correctness note:
The original prompt text contains a mismatch in Example 1. It first says output 3,
but the explanation correctly shows that length 3 is invalid and the true answer is 2.
This solution follows the correct interpretation.

Example 1:
Input: sodium = [120, 80, 200, 90, 60], maxSodium = 300
Correct Output: 2

Example 2:
Input: sodium = [40, 0, 30, 20, 10], maxSodium = 60
Output: 4
"""

from typing import List


class Solution:
    def longest_snack_cart_run(self, sodium: List[int], maxSodium: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is at most maxSodium.

        Args:
            sodium: A list of non-negative integers representing sodium amounts.
            maxSodium: The maximum allowed sum for a chosen contiguous run.

        Returns:
            The maximum length of a contiguous subarray with sum <= maxSodium.

        Time complexity:
            O(n), where n is the length of sodium, because each element enters and
            leaves the sliding window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The sliding window technique works perfectly here because all numbers are non-negative.
        #
        # Why that matters:
        # - When we expand the window to the right, the sum can only stay the same or increase.
        # - If the sum becomes too large, moving the left pointer to the right will only decrease
        #   or keep the sum the same.
        #
        # This monotonic behavior is exactly what makes sliding window efficient and correct.

        # Left boundary of the current window.
        left: int = 0

        # Running sum of the current window sodium[left:right+1].
        current_sum: int = 0

        # Best (maximum) valid window length found so far.
        best_length: int = 0

        # Move the right boundary one step at a time through the array.
        for right in range(len(sodium)):
            # Include the new rightmost element in the current window.
            current_sum += sodium[right]

            # If the window sum is too large, shrink from the left until it becomes valid again.
            #
            # We use a while loop, not an if statement, because removing just one element may
            # still leave the sum above the allowed limit.
            while current_sum > maxSodium and left <= right:
                current_sum -= sodium[left]
                left += 1

            # At this point, the window from left to right is guaranteed to be valid:
            # current_sum <= maxSodium
            #
            # So we can safely compute its length and update the best answer.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        return best_length

    def solve(self, sodium: List[int], maxSodium: int) -> int:
        """
        Wrapper method to solve the problem.

        Args:
            sodium: A list of non-negative integers representing sodium amounts.
            maxSodium: The maximum allowed sum for a chosen contiguous run.

        Returns:
            The length of the longest valid contiguous run.

        Time complexity:
            O(n), where n is the number of snacks.

        Space complexity:
            O(1).
        """
        return self.longest_snack_cart_run(sodium, maxSodium)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt, corrected after checking the explanation carefully.
    # sodium = [120, 80, 200, 90, 60], maxSodium = 300
    #
    # Valid windows of length 2 include:
    # [120, 80] = 200
    # [80, 200] = 280
    # [200, 90] = 290
    # [90, 60] = 150
    #
    # Any length-3 contiguous window:
    # [120, 80, 200] = 400
    # [80, 200, 90] = 370
    # [200, 90, 60] = 350
    # All are too large.
    #
    # Therefore the correct answer is 2.
    sodium1: List[int] = [120, 80, 200, 90, 60]
    max_sodium1: int = 300
    result1: int = solution.solve(sodium1, max_sodium1)
    print(f"Example 1 result: {result1}")  # Expected: 2

    # Example 2 from the prompt.
    # sodium = [40, 0, 30, 20, 10], maxSodium = 60
    #
    # The subarray [0, 30, 20, 10] has sum 60 and length 4.
    # That is valid and is the maximum possible answer.
    sodium2: List[int] = [40, 0, 30, 20, 10]
    max_sodium2: int = 60
    result2: int = solution.solve(sodium2, max_sodium2)
    print(f"Example 2 result: {result2}")  # Expected: 4

    # Additional quick sanity checks for beginner-friendly verification.

    # Single element that fits.
    sodium3: List[int] = [50]
    max_sodium3: int = 50
    result3: int = solution.solve(sodium3, max_sodium3)
    print(f"Additional test 1 result: {result3}")  # Expected: 1

    # Single element that does not fit.
    sodium4: List[int] = [51]
    max_sodium4: int = 50
    result4: int = solution.solve(sodium4, max_sodium4)
    print(f"Additional test 2 result: {result4}")  # Expected: 0

    # All zeros should allow the full array if maxSodium is 0.
    sodium5: List[int] = [0, 0, 0, 0]
    max_sodium5: int = 0
    result5: int = solution.solve(sodium5, max_sodium5)
    print(f"Additional test 3 result: {result5}")  # Expected: 4