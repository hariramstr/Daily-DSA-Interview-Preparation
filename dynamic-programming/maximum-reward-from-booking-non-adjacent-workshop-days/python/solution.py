"""
Title: Maximum Reward from Booking Non-Adjacent Workshop Days

Problem Description:
A training company offers a sequence of one-day workshops over the next n days.
If you book the workshop on day i, you earn rewards[i] points. However, preparing
for a workshop uses the entire following day, so you are not allowed to book
workshops on two adjacent days.

Your task is to return the maximum total reward points you can earn by choosing
a subset of workshop days under this rule.

Formally, given an integer array rewards where rewards[i] is the reward for
booking the workshop on day i, choose a set of indices such that no two chosen
indices differ by 1, and the sum of their rewards is as large as possible.

This is not just about greedily taking the largest reward. A smaller reward today
may allow a better combination later, so you must consider overlapping subproblems
efficiently.

Constraints:
- 1 <= rewards.length <= 100000
- 0 <= rewards[i] <= 1000000000
- The answer fits in a 64-bit signed integer

Example 1:
Input: rewards = [4, 10, 3, 1, 5]
Output: 15
Explanation: Book days 1 and 4 for a total of 10 + 5 = 15. Booking day 0, 2,
and 4 gives 12, which is smaller.

Example 2:
Input: rewards = [2, 7, 9, 3, 1]
Output: 12
Explanation: The best choice is day 0, day 2, and day 4 for 2 + 9 + 1 = 12.

Expected:
Return only the maximum total reward. An O(n) dynamic programming solution is expected.
"""

from typing import List


class Solution:
    def max_reward(self, rewards: List[int]) -> int:
        """
        Compute the maximum total reward from non-adjacent workshop days.

        This uses dynamic programming with constant extra space.
        At each day, we decide between:
        1. Skipping the current day, keeping the best total so far
        2. Booking the current day, which means we must add its reward to the
           best total from two days earlier

        Args:
            rewards: A list where rewards[i] is the reward for booking day i.

        Returns:
            The maximum total reward achievable without booking adjacent days.

        Time complexity:
            O(n), where n is the number of days

        Space complexity:
            O(1), excluding the input array
        """
        # These two variables store the dynamic programming state in a compact form.
        #
        # prev_two:
        #   The best answer considering days up to index i - 2.
        #   In other words, this is the maximum reward we could have earned
        #   before the immediately previous day.
        #
        # prev_one:
        #   The best answer considering days up to index i - 1.
        #   This is the best total reward we know before processing the current day.
        #
        # Why only two variables?
        # Because the recurrence only depends on the previous two states:
        # dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])
        #
        # So instead of storing the entire dp array, we keep only what we need.
        prev_two: int = 0
        prev_one: int = 0

        # Process each day's reward from left to right.
        # For every day, we calculate the best possible total up to that day.
        for reward in rewards:
            # Option 1: Skip the current day.
            # If we skip today, the best total remains whatever it was up to yesterday.
            skip_current: int = prev_one

            # Option 2: Book the current day.
            # If we book today, we cannot have booked yesterday.
            # Therefore, we add today's reward to the best total from two days ago.
            take_current: int = prev_two + reward

            # The best total up to the current day is the better of:
            # - skipping today
            # - taking today
            current_best: int = max(skip_current, take_current)

            # Shift the window forward for the next iteration:
            #
            # Before moving on:
            # - prev_one represented dp[i - 1]
            # - prev_two represented dp[i - 2]
            #
            # After processing current day:
            # - prev_two should become old prev_one
            # - prev_one should become current_best
            prev_two = prev_one
            prev_one = current_best

        # After processing all days, prev_one holds the answer for the full array.
        return prev_one

    def rob(self, rewards: List[int]) -> int:
        """
        Alias method for the same dynamic programming solution.

        This method is included to provide an alternative familiar name for the
        classic non-adjacent selection problem.

        Args:
            rewards: A list where rewards[i] is the reward for booking day i.

        Returns:
            The maximum total reward achievable without booking adjacent days.

        Time complexity:
            O(n), where n is the number of days

        Space complexity:
            O(1), excluding the input array
        """
        return self.max_reward(rewards)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # rewards = [4, 10, 3, 1, 5]
    #
    # Step-by-step verification:
    # Day 0 (4): best = 4
    # Day 1 (10): best = max(4, 10) = 10
    # Day 2 (3): best = max(10, 4 + 3 = 7) = 10
    # Day 3 (1): best = max(10, 10 + 1 = 11) = 11
    # Day 4 (5): best = max(11, 10 + 5 = 15) = 15
    #
    # Final answer = 15
    rewards1: List[int] = [4, 10, 3, 1, 5]
    result1: int = solution.max_reward(rewards1)
    print(result1)  # Expected: 15

    # Example 2:
    # rewards = [2, 7, 9, 3, 1]
    #
    # Step-by-step verification:
    # Day 0 (2): best = 2
    # Day 1 (7): best = max(2, 7) = 7
    # Day 2 (9): best = max(7, 2 + 9 = 11) = 11
    # Day 3 (3): best = max(11, 7 + 3 = 10) = 11
    # Day 4 (1): best = max(11, 11 + 1 = 12) = 12
    #
    # Final answer = 12
    rewards2: List[int] = [2, 7, 9, 3, 1]
    result2: int = solution.max_reward(rewards2)
    print(result2)  # Expected: 12

    # Additional beginner-friendly test cases:
    rewards3: List[int] = [5]
    print(solution.max_reward(rewards3))  # Expected: 5

    rewards4: List[int] = [1, 2]
    print(solution.max_reward(rewards4))  # Expected: 2

    rewards5: List[int] = [0, 0, 0, 0]
    print(solution.max_reward(rewards5))  # Expected: 0