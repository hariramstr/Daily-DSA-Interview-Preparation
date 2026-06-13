"""
Title: Maximum Reward from Skipping Adjacent Milestones

Problem Description:
You are planning a roadshow with n milestone events arranged in a fixed order along a route.
Attending milestone i gives you a reward value rewards[i], which may be positive, zero, or negative.
However, due to travel fatigue and scheduling limits, you are not allowed to attend two adjacent
milestones. You may also choose to skip any milestone.

Your task is to return the maximum total reward you can collect.

In addition to the maximum reward, think carefully about edge cases: if all rewards are negative,
it is valid to attend no milestones at all, resulting in a total reward of 0. The order of
milestones cannot be changed.

Formally, choose a subset of indices such that no two chosen indices are adjacent, and the sum of
their reward values is maximized.

Constraints:
- 1 <= n <= 100000
- -10000 <= rewards[i] <= 10000
- The solution should run in O(n) time.
- Aim for O(1) extra space if possible.

Example 1:
Input: rewards = [5, 1, 2, 10, 6]
Output: 15
Explanation: Attend milestones with rewards 5 and 10 for a total of 15. Attending 5, 2, and 6 is only 13.

Example 2:
Input: rewards = [-4, -2, -7]
Output: 0
Explanation: Every milestone reduces the total reward, so the best choice is to skip all of them.
"""

from typing import List


class Solution:
    def max_reward(self, rewards: List[int]) -> int:
        """
        Compute the maximum total reward obtainable by choosing non-adjacent milestones.

        This uses dynamic programming with constant extra space. At each milestone, we decide:
        1. Skip the current milestone and keep the best total seen so far.
        2. Attend the current milestone and add its reward to the best total from two milestones back.

        Because attending no milestones is allowed, the answer is never forced to be negative.

        Args:
            rewards: A list of integer reward values for each milestone.

        Returns:
            The maximum total reward achievable without attending adjacent milestones.

        Time complexity:
            O(n), where n is the number of milestones.

        Space complexity:
            O(1) extra space.
        """
        # "prev_two" will store the best answer for milestones up to index i - 2.
        # In classic DP notation, this is similar to dp[i - 2].
        #
        # We initialize it to 0 because before processing any milestones,
        # the best reward is 0 by choosing nothing.
        prev_two: int = 0

        # "prev_one" will store the best answer for milestones up to index i - 1.
        # In classic DP notation, this is similar to dp[i - 1].
        #
        # This also starts at 0 for the same reason:
        # if we have not taken anything yet, total reward is 0.
        prev_one: int = 0

        # We now process each milestone from left to right.
        # This works because the best answer at the current position depends only on:
        # - the best answer up to the previous milestone
        # - the best answer up to two milestones earlier
        #
        # That means we do not need a full DP array; two variables are enough.
        for reward in rewards:
            # Option 1: skip the current milestone.
            # If we skip it, our total reward remains whatever the best answer was
            # after considering the previous milestone.
            skip_current: int = prev_one

            # Option 2: attend the current milestone.
            # If we attend it, we are not allowed to attend the immediately previous one,
            # so we add the current reward to the best answer from two milestones back.
            take_current: int = prev_two + reward

            # The best answer at this step is the better of:
            # - skipping the current milestone
            # - taking the current milestone
            #
            # Important note about negative values:
            # Because prev_one starts at 0 and we always take max(skip_current, take_current),
            # the running answer can never drop below 0.
            # This correctly handles cases where all rewards are negative:
            # the algorithm will prefer skipping everything.
            current_best: int = max(skip_current, take_current)

            # Shift the DP window forward:
            #
            # Before moving to the next milestone:
            # - the old prev_one becomes the new prev_two
            # - current_best becomes the new prev_one
            #
            # This preserves exactly the two previous DP states needed for the next iteration.
            prev_two = prev_one
            prev_one = current_best

        # After processing all milestones, prev_one holds the best possible answer.
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    # Trace:
    # rewards = [5, 1, 2, 10, 6]
    # Best non-adjacent choice is 5 + 10 = 15
    sample_1: List[int] = [5, 1, 2, 10, 6]
    result_1: int = solution.max_reward(sample_1)
    print(f"Input: {sample_1}")
    print(f"Maximum reward: {result_1}")
    print()

    # Example 2 from the problem statement.
    # All values are negative, so skipping everything gives 0.
    sample_2: List[int] = [-4, -2, -7]
    result_2: int = solution.max_reward(sample_2)
    print(f"Input: {sample_2}")
    print(f"Maximum reward: {result_2}")
    print()

    # Additional beginner-friendly test cases.
    sample_3: List[int] = [2, 7, 9, 3, 1]
    result_3: int = solution.max_reward(sample_3)
    print(f"Input: {sample_3}")
    print(f"Maximum reward: {result_3}")
    print()

    sample_4: List[int] = [0, 0, 0]
    result_4: int = solution.max_reward(sample_4)
    print(f"Input: {sample_4}")
    print(f"Maximum reward: {result_4}")
    print()

    sample_5: List[int] = [10]
    result_5: int = solution.max_reward(sample_5)
    print(f"Input: {sample_5}")
    print(f"Maximum reward: {result_5}")