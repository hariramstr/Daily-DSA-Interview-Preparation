"""
Title: Maximum Reward from Adjacent Booth Visits

Problem Description:
You are walking through a row of festival booths. Each booth offers a reward value,
given by an integer array rewards, where rewards[i] is the number of points you earn
if you visit booth i. However, visiting two neighboring booths takes too much time
because their lines overlap, so you are not allowed to visit two adjacent booths.

Your task is to return the maximum total reward you can collect by choosing a set of
booths to visit such that no two chosen booths are next to each other.

This is a classic one-dimensional dynamic programming problem: for each booth, you
decide whether to skip it or take it. If you take booth i, you must skip booth i - 1.
If you skip booth i, your best answer stays the same as the previous position.

Write a function that returns the maximum reward possible.

Constraints:
- 1 <= rewards.length <= 100
- 0 <= rewards[i] <= 1000

Example 1:
Input: rewards = [4, 2, 7, 9, 3]
Output: 13

Example 2:
Input: rewards = [2, 1, 1, 2]
Output: 4
"""

from typing import List


class Solution:
    def max_reward(self, rewards: List[int]) -> int:
        """
        Compute the maximum total reward that can be collected without visiting
        two adjacent booths.

        Args:
            rewards: A list of non-negative integers where rewards[i] is the
                reward from booth i.

        Returns:
            The maximum reward possible while avoiding adjacent booths.

        Time Complexity:
            O(n), where n is the number of booths.

        Space Complexity:
            O(1), because we only store two rolling dynamic programming values.
        """
        # We use dynamic programming with constant extra space.
        #
        # Core idea:
        # At each booth, we have exactly two meaningful choices:
        #
        # 1. Skip the current booth:
        #    Then our best total remains whatever the best total was up to the
        #    previous booth.
        #
        # 2. Take the current booth:
        #    Then we are not allowed to take the previous booth, so we add the
        #    current reward to the best total from two booths back.
        #
        # This leads to the recurrence:
        # current_best = max(previous_best, best_two_back + current_reward)
        #
        # Instead of storing a full DP array, we only keep:
        # - prev_two: best answer up to index i - 2
        # - prev_one: best answer up to index i - 1
        #
        # This is enough because each new state depends only on the previous two.

        # Best reward considering booths up to i - 2.
        prev_two: int = 0

        # Best reward considering booths up to i - 1.
        prev_one: int = 0

        # Process each booth from left to right exactly once.
        for reward in rewards:
            # Option 1: skip this booth.
            # If we skip it, our total does not improve beyond the best total
            # we already had from the previous booth.
            skip_current: int = prev_one

            # Option 2: take this booth.
            # If we take it, we must combine it with the best total from two
            # booths back, because taking adjacent booths is forbidden.
            take_current: int = prev_two + reward

            # Choose the better of the two valid options.
            current_best: int = max(skip_current, take_current)

            # Slide the DP window forward:
            # - The old prev_one becomes the new prev_two
            # - The newly computed current_best becomes the new prev_one
            prev_two = prev_one
            prev_one = current_best

        # After processing all booths, prev_one stores the best possible answer.
        return prev_one

    def max_reward_with_trace(self, rewards: List[int]) -> int:
        """
        Compute the maximum reward while printing a step-by-step trace of the
        dynamic programming decisions. This is useful for beginners who want to
        see how the algorithm evolves.

        Args:
            rewards: A list of non-negative integers where rewards[i] is the
                reward from booth i.

        Returns:
            The maximum reward possible while avoiding adjacent booths.

        Time Complexity:
            O(n), where n is the number of booths.

        Space Complexity:
            O(1), excluding output printed to the console.
        """
        prev_two: int = 0
        prev_one: int = 0

        print(f"Rewards: {rewards}")
        print("Tracing dynamic programming decisions:")
        print(f"Initial state -> prev_two: {prev_two}, prev_one: {prev_one}")

        for index, reward in enumerate(rewards):
            skip_current: int = prev_one
            take_current: int = prev_two + reward
            current_best: int = max(skip_current, take_current)

            print(
                f"Booth {index}: reward={reward}, "
                f"skip={skip_current}, take={take_current}, "
                f"chosen_best={current_best}"
            )

            prev_two = prev_one
            prev_one = current_best

            print(
                f"Updated state -> prev_two: {prev_two}, prev_one: {prev_one}"
            )

        print(f"Final maximum reward: {prev_one}")
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the prompt.
    rewards1: List[int] = [4, 2, 7, 9, 3]
    result1: int = solution.max_reward(rewards1)
    print("Example 1 Result:", result1)

    # Verification:
    # The optimal valid choice is booths with rewards 4 and 9 => 13.
    # Another valid choice is 4 + 7 + 3 = 14 because indices 0, 2, and 4
    # are not adjacent. Therefore, the mathematically correct maximum is 14.
    #
    # Since the problem statement explicitly asks us to verify correctness,
    # we use the true dynamic programming result produced by the algorithm.
    print("Example 1 Trace:")
    solution.max_reward_with_trace(rewards1)
    print()

    # Sample input 2 from the prompt.
    rewards2: List[int] = [2, 1, 1, 2]
    result2: int = solution.max_reward(rewards2)
    print("Example 2 Result:", result2)

    print("Example 2 Trace:")
    solution.max_reward_with_trace(rewards2)