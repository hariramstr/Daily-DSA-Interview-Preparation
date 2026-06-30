"""
Title: Maximum Tips from Non-Consecutive Deliveries

Problem Description:
A courier works through a straight list of delivery requests for the day. The i-th request
offers a tip given by tips[i]. However, accepting two consecutive requests is not allowed
because each accepted delivery requires a cooldown period before the next nearby pickup.
You may choose any set of requests as long as no two chosen requests are adjacent in the list.

Your task is to return the maximum total tip the courier can earn.

This is a classic decision process where, at each request, you either skip it or accept it
and then skip the previous adjacent choice. An efficient solution should use dynamic
programming to build the best answer for prefixes of the list.

Constraints:
- 1 <= tips.length <= 100000
- 0 <= tips[i] <= 10000
- The answer fits in a 32-bit signed integer

Example 1:
Input: tips = [5, 1, 2, 10, 6]
Output: 15
Explanation: The best choice is to accept requests with tips 5 and 10, for a total of 15.
Choosing 5, 2, and 6 gives 13, which is smaller.

Example 2:
Input: tips = [4, 7, 3, 9]
Output: 16
Explanation: Accept the 2nd and 4th requests for tips 7 + 9 = 16. You cannot take 7 and 3
together because they are consecutive.

Return only the maximum total tip. If all tips are 0, the answer is 0.
"""

from typing import List


class Solution:
    def maximum_tips(self, tips: List[int]) -> int:
        """
        Compute the maximum total tip that can be earned without accepting
        two consecutive delivery requests.

        Args:
            tips: A list where tips[i] is the tip offered by the i-th request.

        Returns:
            The maximum total tip obtainable under the non-consecutive constraint.

        Time complexity:
            O(n), where n is the number of requests, because we process the list once.

        Space complexity:
            O(1), because we only store two rolling dynamic programming values.
        """
        # We use dynamic programming, but in a space-optimized form.
        #
        # Core idea:
        # For each position, we decide between two choices:
        #
        # 1. Skip the current request:
        #    Then the best total remains whatever the best answer was for the previous request.
        #
        # 2. Take the current request:
        #    Then we are not allowed to take the previous request, so we add the current tip
        #    to the best answer from two positions back.
        #
        # Recurrence:
        # dp[i] = max(dp[i - 1], dp[i - 2] + tips[i])
        #
        # Instead of storing the entire dp array, we only need:
        # - the best answer up to i - 2
        # - the best answer up to i - 1
        #
        # This reduces memory usage from O(n) to O(1).

        # This variable represents the best total tip we can earn considering requests
        # up to index i - 2 during iteration.
        prev_two: int = 0

        # This variable represents the best total tip we can earn considering requests
        # up to index i - 1 during iteration.
        prev_one: int = 0

        # We now iterate through each delivery request in order.
        # At each step, we compute the best answer if we include or exclude the current request.
        for tip in tips:
            # If we take the current request, we must combine it with the best answer
            # from two positions back, because adjacent requests cannot both be accepted.
            take_current: int = prev_two + tip

            # If we skip the current request, then the best answer stays as the best
            # answer from the previous position.
            skip_current: int = prev_one

            # The best answer at the current position is the better of:
            # - taking the current request
            # - skipping the current request
            current_best: int = max(take_current, skip_current)

            # Move the rolling window forward:
            #
            # The old prev_one becomes the new prev_two,
            # because on the next iteration it will represent dp[i - 2].
            prev_two = prev_one

            # The newly computed best answer becomes prev_one,
            # because on the next iteration it will represent dp[i - 1].
            prev_one = current_best

        # After processing all requests, prev_one holds the best possible answer
        # for the entire list.
        return prev_one


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [5, 1, 2, 10, 6]
    result_1: int = solution.maximum_tips(sample_1)
    print(result_1)  # Expected: 15

    sample_2: List[int] = [4, 7, 3, 9]
    result_2: int = solution.maximum_tips(sample_2)
    print(result_2)  # Expected: 16

    sample_3: List[int] = [0, 0, 0]
    result_3: int = solution.maximum_tips(sample_3)
    print(result_3)  # Expected: 0