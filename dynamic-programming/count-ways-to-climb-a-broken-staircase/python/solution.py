"""
Title: Count Ways to Climb a Broken Staircase

Problem Description:
You are given a staircase with n steps, numbered from 1 to n. A person starts on
the ground at step 0 and wants to reach exactly step n. On each move, they may
climb either 1 step or 2 steps. However, some steps are broken and cannot be
landed on. You are given an array broken, containing the step numbers that are
broken.

Return the number of distinct ways to reach step n without ever landing on a
broken step. Since the answer can become large, return it modulo 1,000,000,007.

Two ways are considered different if the sequence of jumps is different. For
example, jumping 1 then 2 is different from jumping 2 then 1.

This is a dynamic programming problem because the number of ways to reach a step
depends on the number of ways to reach previous steps. If a step is broken, its
number of ways is 0.

Constraints:
- 1 <= n <= 100000
- 0 <= broken.length <= n
- 1 <= broken[i] <= n
- All values in broken are distinct

Example 1:
Input: n = 5, broken = [2]
Output: 2
Explanation:
The valid ways are:
- 1 -> 1 -> 1 -> 1 -> 1
- 1 -> 2 -> 2

Example 2:
Input: n = 6, broken = [3, 5]
Output: 1
Explanation:
There is only one valid way:
- 2 -> 2 -> 2
This lands on steps 2, 4, and 6, avoiding broken steps 3 and 5.
"""

from typing import List

MOD: int = 1_000_000_007


class Solution:
    def count_ways(self, n: int, broken: List[int]) -> int:
        """
        Count the number of distinct valid ways to reach step n.

        A move can be either 1 step or 2 steps, and broken steps cannot be landed on.

        Args:
            n: The target step to reach.
            broken: A list of broken step numbers that cannot be landed on.

        Returns:
            The number of distinct valid ways to reach step n modulo 1,000,000,007.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        # We convert the list of broken steps into a set.
        # Why?
        # - We need to repeatedly ask: "Is this step broken?"
        # - A set gives average O(1) lookup time.
        # - If we kept broken as a list, each lookup could take O(k),
        #   which would be slower for large inputs.
        broken_set = set(broken)

        # dp[i] will store the number of valid ways to reach step i.
        #
        # Meaning of dp:
        # - dp[0] = number of ways to stand at the ground before climbing.
        # - dp[1] = number of ways to reach step 1.
        # - ...
        # - dp[n] = final answer.
        #
        # We create an array of size n + 1 because we need indices from 0 to n.
        dp: List[int] = [0] * (n + 1)

        # Base case:
        # There is exactly 1 way to be at step 0:
        # do nothing and start there.
        dp[0] = 1

        # Now we compute the answer for every step from 1 to n.
        # Each step depends only on the previous two steps.
        for step in range(1, n + 1):
            # If the current step is broken, we are not allowed to land on it.
            # Therefore, the number of ways to reach it must be 0.
            if step in broken_set:
                dp[step] = 0
                continue

            # If the step is not broken, we can reach it from:
            # - step - 1 by taking a 1-step jump
            # - step - 2 by taking a 2-step jump
            #
            # So:
            # dp[step] = dp[step - 1] + dp[step - 2]
            #
            # But we must be careful with boundaries:
            # - step - 1 exists when step >= 1
            # - step - 2 exists when step >= 2
            ways_from_one_step_before = dp[step - 1] if step - 1 >= 0 else 0
            ways_from_two_steps_before = dp[step - 2] if step - 2 >= 0 else 0

            # Add both possibilities and apply modulo to keep numbers small
            # and satisfy the problem requirement.
            dp[step] = (ways_from_one_step_before + ways_from_two_steps_before) % MOD

        # The final answer is the number of ways to reach step n.
        return dp[n]

    def count_ways_optimized(self, n: int, broken: List[int]) -> int:
        """
        Count the number of distinct valid ways to reach step n using optimized space.

        This version keeps only the last two DP values instead of the full array.

        Args:
            n: The target step to reach.
            broken: A list of broken step numbers that cannot be landed on.

        Returns:
            The number of distinct valid ways to reach step n modulo 1,000,000,007.

        Time complexity:
            O(n)

        Space complexity:
            O(b), where b is the number of broken steps, due to the set
            (excluding input storage). The DP tracking itself uses O(1) space.
        """
        # Again, use a set for fast broken-step checks.
        broken_set = set(broken)

        # prev2 represents dp[step - 2]
        # prev1 represents dp[step - 1]
        #
        # Initially, before processing any real step:
        # dp[0] = 1
        # There is no dp[-1], so we conceptually treat it as 0 when needed.
        prev2 = 0
        prev1 = 1

        # Process steps from 1 to n.
        for step in range(1, n + 1):
            # If this step is broken, it cannot be landed on.
            if step in broken_set:
                current = 0
            else:
                # Otherwise, current ways = previous step ways + two-steps-back ways.
                current = (prev1 + prev2) % MOD

            # Shift the window forward:
            # old prev1 becomes new prev2
            # current becomes new prev1
            prev2, prev1 = prev1, current

        # After the loop, prev1 holds dp[n].
        return prev1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 5
    broken1 = [2]
    result1 = solution.count_ways(n1, broken1)
    print(f"n = {n1}, broken = {broken1} -> {result1}")  # Expected: 2

    # Example 2
    n2 = 6
    broken2 = [3, 5]
    result2 = solution.count_ways(n2, broken2)
    print(f"n = {n2}, broken = {broken2} -> {result2}")  # Expected: 1

    # Additional sample tests
    n3 = 4
    broken3 = []
    result3 = solution.count_ways(n3, broken3)
    print(f"n = {n3}, broken = {broken3} -> {result3}")  # Expected: 5

    n4 = 3
    broken4 = [3]
    result4 = solution.count_ways(n4, broken4)
    print(f"n = {n4}, broken = {broken4} -> {result4}")  # Expected: 0

    # Also demonstrate the optimized version on the same examples
    print(solution.count_ways_optimized(5, [2]))      # Expected: 2
    print(solution.count_ways_optimized(6, [3, 5]))   # Expected: 1