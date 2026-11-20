"""
Title: Maximum Reward from Choosing Rested Training Days

Problem Description:
A sports analytics team is building a planner for an athlete preparing for a tournament.
You are given an integer array rewards where rewards[i] is the performance benefit of
training on day i. Some days may even have negative values, meaning training that day
would hurt recovery more than it helps.

The athlete may choose any subset of days to train, but cannot train on two consecutive
days because each training day requires a full recovery day afterward.

Your task is to return the maximum total reward the athlete can earn.

Unlike a simple greedy choice, the best answer may require skipping a large positive day
in order to combine several other days more effectively. If all values are negative,
the athlete is allowed to train on no days at all, so the answer can be 0.

Constraints:
- 1 <= rewards.length <= 100000
- -10000 <= rewards[i] <= 10000
- O(n) time
- Aim for O(1) extra space if possible

Examples:
1. rewards = [5, 1, 1, 5]
   Output: 10
   Explanation: Train on days 0 and 3 => 5 + 5 = 10

2. rewards = [4, -2, 7, 3, -1, 8]
   Output: 19
   Explanation: Train on days 0, 2, and 5 => 4 + 7 + 8 = 19
"""

from typing import List


class Solution:
    def max_reward(self, rewards: List[int]) -> int:
        """
        Compute the maximum total reward when no two chosen days are adjacent.

        This uses dynamic programming with constant extra space. For each day,
        we decide whether it is better to:
        1. Skip the current day, keeping the best answer from the previous day
        2. Take the current day, which means we must add its reward to the best
           answer from two days earlier

        Because the athlete is allowed to choose no days at all, the result is
        never forced to be negative.

        Args:
            rewards: A list of integers where rewards[i] is the reward for training
                on day i.

        Returns:
            The maximum total reward obtainable without choosing adjacent days.

        Time complexity:
            O(n), where n is the number of days.

        Space complexity:
            O(1), because we only store two rolling DP values.
        """
        # We maintain two dynamic programming states:
        #
        # prev_two:
        #   The best total reward considering days up to index i - 2.
        #
        # prev_one:
        #   The best total reward considering days up to index i - 1.
        #
        # Why only these two?
        # Because the recurrence for the current day only depends on:
        #   - the previous day result (if we skip today)
        #   - the result from two days ago (if we take today)
        #
        # This lets us avoid building a full DP array and keeps extra space O(1).
        prev_two: int = 0
        prev_one: int = 0

        # We process each day from left to right.
        # At each day with reward value "value", there are exactly two choices:
        #
        # 1. Skip this day:
        #    Then our total remains whatever the best total was up to yesterday,
        #    which is prev_one.
        #
        # 2. Take this day:
        #    Then we cannot have taken yesterday, so we add today's reward to
        #    the best total from two days ago, which is prev_two + value.
        #
        # The best answer for the current position is the maximum of these two.
        for value in rewards:
            # If we skip the current day, nothing changes from the best answer
            # we already had up to the previous day.
            skip_current: int = prev_one

            # If we take the current day, we must combine it with the best answer
            # from two days back to respect the "no adjacent days" rule.
            take_current: int = prev_two + value

            # Choose the better of the two options.
            #
            # Important note about negative values:
            # Since prev_one and prev_two both start at 0 and we always take max,
            # the DP naturally supports the option of selecting no days at all.
            # That means if all values are negative, the answer remains 0.
            current: int = max(skip_current, take_current)

            # Shift the rolling window forward:
            #
            # Before update:
            #   prev_two = dp[i - 2]
            #   prev_one = dp[i - 1]
            #
            # After update:
            #   prev_two should become old prev_one
            #   prev_one should become current dp[i]
            prev_two = prev_one
            prev_one = current

        # After processing all days, prev_one stores the best answer for the
        # entire array.
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # rewards = [5, 1, 1, 5]
    #
    # Manual trace:
    # Day 0: max(skip=0, take=0+5=5) => 5
    # Day 1: max(skip=5, take=0+1=1) => 5
    # Day 2: max(skip=5, take=5+1=6) => 6
    # Day 3: max(skip=6, take=5+5=10) => 10
    #
    # Expected output: 10
    rewards1: List[int] = [5, 1, 1, 5]
    result1: int = solution.max_reward(rewards1)
    print(f"Input: {rewards1}")
    print(f"Maximum reward: {result1}")
    print()

    # Example 2:
    # rewards = [4, -2, 7, 3, -1, 8]
    #
    # Manual trace:
    # Day 0: max(skip=0, take=4) => 4
    # Day 1: max(skip=4, take=-2) => 4
    # Day 2: max(skip=4, take=4+7=11) => 11
    # Day 3: max(skip=11, take=4+3=7) => 11
    # Day 4: max(skip=11, take=11-1=10) => 11
    # Day 5: max(skip=11, take=11+8=19) => 19
    #
    # Expected output: 19
    rewards2: List[int] = [4, -2, 7, 3, -1, 8]
    result2: int = solution.max_reward(rewards2)
    print(f"Input: {rewards2}")
    print(f"Maximum reward: {result2}")
    print()

    # Additional beginner-friendly test:
    # All negative values should return 0 because the athlete may choose no days.
    rewards3: List[int] = [-5, -1, -8]
    result3: int = solution.max_reward(rewards3)
    print(f"Input: {rewards3}")
    print(f"Maximum reward: {result3}")