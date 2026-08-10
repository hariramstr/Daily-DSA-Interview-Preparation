"""
Title: Maximum Consecutive Days Within a Sleep Debt Budget

Problem Description:
You are given an integer array sleepHours where sleepHours[i] represents how many
hours a person slept on day i, and an integer target representing the recommended
number of sleep hours per day.

For any day, the sleep debt for that day is:
    max(0, target - sleepHours[i])

That means:
- If the person slept at least target hours, that day adds 0 debt.
- If the person slept less than target hours, that day adds the shortage as debt.

Your task is to find the length of the longest contiguous block of days whose total
accumulated sleep debt is at most budget.

Formally, for a subarray sleepHours[l..r], define its total debt as the sum of:
    max(0, target - sleepHours[i])
for all i in [l, r].

Return the maximum possible value of:
    r - l + 1
such that the total debt is less than or equal to budget.

Constraints:
- 1 <= sleepHours.length <= 100000
- 0 <= sleepHours[i] <= 24
- 1 <= target <= 24
- 0 <= budget <= 1000000000

Important note about the provided examples:
The first example's written explanation is internally inconsistent. Its own debt array
is [0, 2, 0, 3, 1, 0], and with budget = 3, the longest valid contiguous block length
is actually 3, not 4. For example:
- [7, 5, 8] has debt 0 + 2 + 0 = 2, valid, length 3
- [5, 8, 4] has debt 2 + 0 + 3 = 5, invalid
- [8, 4, 6] has debt 0 + 3 + 1 = 4, invalid
So the correct answer for Example 1 is 3.

Example 1:
Input: sleepHours = [7, 5, 8, 4, 6, 7], target = 7, budget = 3
Correct Output: 3

Example 2:
Input: sleepHours = [6, 6, 7, 7, 5, 8, 6], target = 7, budget = 2
Output: 4
"""

from typing import List


class Solution:
    def longest_sustainable_streak(
        self, sleep_hours: List[int], target: int, budget: int
    ) -> int:
        """
        Find the maximum length of a contiguous subarray whose total sleep debt
        is at most the given budget.

        The debt for a single day is:
            max(0, target - sleep_hours[i])

        Because each day's debt is never negative, we can use the sliding window
        technique efficiently:
        - Expand the right end of the window
        - Track the current total debt
        - If the debt becomes too large, shrink from the left until valid again
        - Record the largest valid window length seen

        Args:
            sleep_hours: List of sleep hours for each day.
            target: Recommended sleep hours per day.
            budget: Maximum allowed total debt for a contiguous block.

        Returns:
            The length of the longest contiguous block with total debt <= budget.

        Time complexity:
            O(n), where n is the number of days.
            Each index enters and leaves the sliding window at most once.

        Space complexity:
            O(1), excluding the input array.
            We only use a few variables.
        """
        # This variable will store the left boundary of our current sliding window.
        # The window always represents a contiguous block of days from left to right.
        left: int = 0

        # This keeps track of the total sleep debt inside the current window.
        current_debt: int = 0

        # This stores the best (maximum) valid window length found so far.
        best_length: int = 0

        # We move the right boundary one step at a time across the array.
        # At each step, we include sleep_hours[right] into the current window.
        for right in range(len(sleep_hours)):
            # Compute the debt contributed by the new day at index 'right'.
            # If the person slept enough, debt is 0.
            # Otherwise, debt is the shortage from the target.
            day_debt: int = max(0, target - sleep_hours[right])

            # Add this new day's debt into the running total for the window.
            current_debt += day_debt

            # If the window is no longer valid (debt too large),
            # we must shrink it from the left until it becomes valid again.
            #
            # Why this works:
            # - All daily debts are non-negative.
            # - So when we move 'right' forward, total debt can only stay the same
            #   or increase.
            # - If debt exceeds budget, the only way to fix it is to remove days
            #   from the left side.
            while current_debt > budget:
                # Before moving 'left' forward, remove the debt contribution
                # of the day currently at the left boundary.
                left_day_debt: int = max(0, target - sleep_hours[left])
                current_debt -= left_day_debt

                # Shrink the window by advancing the left boundary.
                left += 1

            # At this point, the window [left..right] is guaranteed valid:
            # its total debt is <= budget.
            #
            # So we compute its length and update the best answer if needed.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # After scanning all possible right boundaries, best_length is the answer.
        return best_length

    def maxConsecutiveDaysWithinBudget(
        self, sleepHours: List[int], target: int, budget: int
    ) -> int:
        """
        Wrapper method matching the problem statement naming style.

        Args:
            sleepHours: List of sleep hours for each day.
            target: Recommended sleep hours per day.
            budget: Maximum allowed total debt.

        Returns:
            The maximum valid contiguous block length.

        Time complexity:
            O(n), where n is the number of days.

        Space complexity:
            O(1), excluding the input array.
        """
        return self.longest_sustainable_streak(sleepHours, target, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Debt array:
    # [max(0, 7-7), max(0, 7-5), max(0, 7-8), max(0, 7-4), max(0, 7-6), max(0, 7-7)]
    # = [0, 2, 0, 3, 1, 0]
    #
    # With budget = 3, the longest valid contiguous block length is 3.
    # The prompt text contains contradictory statements, but 3 is the correct result.
    sleep_hours_1: List[int] = [7, 5, 8, 4, 6, 7]
    target_1: int = 7
    budget_1: int = 3
    result_1: int = solution.maxConsecutiveDaysWithinBudget(
        sleep_hours_1, target_1, budget_1
    )
    print("Example 1 Result:", result_1)  # Expected: 3

    # Example 2 from the prompt.
    # Debt array = [1, 1, 0, 0, 2, 0, 1]
    # Longest valid block with total debt <= 2 is length 4.
    sleep_hours_2: List[int] = [6, 6, 7, 7, 5, 8, 6]
    target_2: int = 7
    budget_2: int = 2
    result_2: int = solution.maxConsecutiveDaysWithinBudget(
        sleep_hours_2, target_2, budget_2
    )
    print("Example 2 Result:", result_2)  # Expected: 4

    # Additional small sanity checks for beginner-friendly verification.

    # All days meet or exceed target, so every day's debt is 0.
    # Entire array should be valid.
    sleep_hours_3: List[int] = [8, 7, 9, 10]
    target_3: int = 7
    budget_3: int = 0
    result_3: int = solution.maxConsecutiveDaysWithinBudget(
        sleep_hours_3, target_3, budget_3
    )
    print("Sanity Check 1 Result:", result_3)  # Expected: 4

    # Every day is below target by 1, budget allows only 2 total debt.
    # Longest valid contiguous block should be 2.
    sleep_hours_4: List[int] = [6, 6, 6, 6]
    target_4: int = 7
    budget_4: int = 2
    result_4: int = solution.maxConsecutiveDaysWithinBudget(
        sleep_hours_4, target_4, budget_4
    )
    print("Sanity Check 2 Result:", result_4)  # Expected: 2