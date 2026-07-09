"""
Title: Longest Reading Streak Within Late Fee Budget

Problem Description:
You are given an array lateFees where lateFees[i] is the late fee charged on day i
for a borrowed library item. A student wants to study over a consecutive block of
days, but the total late fees during that block must not exceed a given budget B.

Return the length of the longest contiguous subarray whose sum is less than or equal
to B.

In other words, find the maximum number of consecutive days the student can keep the
item while ensuring the total accumulated late fees in that chosen window stay within
budget.

This problem is designed to be solved efficiently using a sliding window technique.
Since all late fees are non-negative, once the current window exceeds the budget,
moving the left boundary to the right is the correct way to restore validity.

Constraints:
- 1 <= lateFees.length <= 100000
- 0 <= lateFees[i] <= 10000
- 0 <= B <= 1000000000
- lateFees contains only non-negative integers

Example 1:
Input: lateFees = [2, 1, 3, 2, 1], B = 5
Output: 2
Explanation: Valid windows include [2,1], [3,2], and [2,1]. Any window of length 3
has total fee greater than 5, so the answer is 2.

Example 2:
Input: lateFees = [0, 1, 1, 0, 2, 1], B = 3
Output: 4
Explanation: One optimal window is [1,1,0,1] formed by days with fees [1,1,0,1],
which sums to 3. No longer contiguous window stays within the budget.
"""

from typing import List


class Solution:
    def longest_reading_streak(self, lateFees: List[int], B: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most B.

        Args:
            lateFees: A list of non-negative integers representing daily late fees.
            B: The maximum allowed total fee for the chosen consecutive block.

        Returns:
            The length of the longest contiguous subarray with sum <= B.

        Time Complexity:
            O(n), where n is the length of lateFees.
            Each element is added to the window once and removed at most once.

        Space Complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The sliding window technique works perfectly here because all values are
        # non-negative. That property is extremely important:
        #
        # - When we expand the window to the right, the sum can only stay the same
        #   or increase.
        # - If the sum becomes too large, the only way to make it smaller is to move
        #   the left boundary to the right.
        #
        # Because of this, we never need to move the left pointer backward, which
        # gives us a linear-time solution.

        # left:
        #   The starting index of the current window.
        left: int = 0

        # current_sum:
        #   The sum of all values inside the current window [left, right].
        current_sum: int = 0

        # max_length:
        #   The best valid window length found so far.
        max_length: int = 0

        # We move the right pointer from left to right across the array, expanding
        # the window one element at a time.
        for right in range(len(lateFees)):
            # Add the new rightmost element into the current window sum because the
            # window is now extended to include lateFees[right].
            current_sum += lateFees[right]

            # If the current window sum is too large, it violates the budget.
            # Since all numbers are non-negative, the correct fix is to shrink the
            # window from the left until the sum becomes valid again.
            while current_sum > B and left <= right:
                # Remove the element at the left boundary from the sum because we are
                # about to exclude it from the window.
                current_sum -= lateFees[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window [left, right] is guaranteed to have
            # current_sum <= B, so it is a valid candidate.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any valid
            # window we have seen before.
            if current_length > max_length:
                max_length = current_length

        # After checking every possible right boundary, max_length stores the answer.
        return max_length

    def longestSubarrayWithinBudget(self, lateFees: List[int], B: int) -> int:
        """
        Wrapper method matching an alternative naming style.

        Args:
            lateFees: A list of non-negative integers representing daily late fees.
            B: The maximum allowed total fee for the chosen consecutive block.

        Returns:
            The length of the longest contiguous subarray with sum <= B.

        Time Complexity:
            O(n), where n is the length of lateFees.

        Space Complexity:
            O(1).
        """
        return self.longest_reading_streak(lateFees, B)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    late_fees_1: List[int] = [2, 1, 3, 2, 1]
    budget_1: int = 5
    result_1: int = solution.longest_reading_streak(late_fees_1, budget_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2
    late_fees_2: List[int] = [0, 1, 1, 0, 2, 1]
    budget_2: int = 3
    result_2: int = solution.longest_reading_streak(late_fees_2, budget_2)
    print("Example 2 Result:", result_2)  # Expected: 4

    # Additional beginner-friendly checks
    late_fees_3: List[int] = [1, 2, 3]
    budget_3: int = 3
    result_3: int = solution.longest_reading_streak(late_fees_3, budget_3)
    print("Additional Test 1 Result:", result_3)  # Expected: 2 -> [1,2]

    late_fees_4: List[int] = [0, 0, 0, 0]
    budget_4: int = 0
    result_4: int = solution.longest_reading_streak(late_fees_4, budget_4)
    print("Additional Test 2 Result:", result_4)  # Expected: 4

    late_fees_5: List[int] = [5, 1, 1]
    budget_5: int = 0
    result_5: int = solution.longest_reading_streak(late_fees_5, budget_5)
    print("Additional Test 3 Result:", result_5)  # Expected: 0