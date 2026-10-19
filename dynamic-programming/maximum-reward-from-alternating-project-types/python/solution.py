"""
Title: Maximum Reward from Alternating Project Types

Problem Description:
A consulting firm has a backlog of projects to complete over the next several weeks.
Each project belongs to one of two types: type A or type B.

You are given two arrays, aReward and bReward, both of length n.
On week i (0-indexed), the firm may choose exactly one of the following options:
- complete the type A project for reward aReward[i]
- complete the type B project for reward bReward[i]
- skip the week and earn 0 reward

Constraint on worked weeks:
The firm does not want to do the same type of project in consecutive worked weeks.
That means:
- if the last non-skipped week used type A, then the next worked week must use type B
- if the last non-skipped week used type B, then the next worked week must use type A
- skipped weeks do NOT reset this rule; they simply mean no project was done that week

Goal:
Return the maximum total reward the firm can earn.

Constraints:
- 1 <= n <= 200000
- 0 <= aReward[i], bReward[i] <= 10^9
- The answer fits in a 64-bit signed integer
"""

from typing import List


class Solution:
    def max_reward(self, aReward: List[int], bReward: List[int]) -> int:
        """
        Compute the maximum total reward while ensuring worked project types alternate.

        We process weeks from left to right and maintain dynamic programming states:
        - dp_none: best reward so far if we have not worked any project yet
        - dp_a: best reward so far if the last worked project type is A
        - dp_b: best reward so far if the last worked project type is B

        Important detail:
        Skipping a week does not change the "last worked type".
        So when we skip, the state simply carries forward unchanged.

        Args:
            aReward: Rewards for doing type A projects each week.
            bReward: Rewards for doing type B projects each week.

        Returns:
            The maximum total reward achievable.

        Time complexity:
            O(n), because we process each week once.

        Space complexity:
            O(1), because we store only a constant number of DP states.
        """
        # This value represents an impossible state.
        # We use a very small number so that "max" operations never accidentally
        # choose an invalid transition.
        neg_inf: int = -10**30

        # dp_none:
        # Best reward after processing some prefix of weeks if we have not done
        # any project at all yet.
        #
        # Initially, before any week is processed, reward is 0 and we have not
        # worked on anything.
        dp_none: int = 0

        # dp_a:
        # Best reward after processing some prefix of weeks if the most recent
        # worked week used project type A.
        #
        # Initially impossible because we have not worked yet.
        dp_a: int = neg_inf

        # dp_b:
        # Best reward after processing some prefix of weeks if the most recent
        # worked week used project type B.
        #
        # Initially impossible because we have not worked yet.
        dp_b: int = neg_inf

        # Iterate through each week exactly once.
        for i in range(len(aReward)):
            # Save old states before updating.
            # This is crucial because all transitions for week i must be based on
            # the states from week i - 1, not partially updated values.
            prev_none: int = dp_none
            prev_a: int = dp_a
            prev_b: int = dp_b

            # Option 1: Skip this week.
            #
            # Skipping does not change the last worked type.
            # Therefore:
            # - if we were in prev_none, we remain in none
            # - if we were in prev_a, we remain in a
            # - if we were in prev_b, we remain in b
            #
            # Since dp_none can only ever stay 0, we simply carry it forward.
            new_none: int = prev_none

            # Option 2: End this week with last worked type A.
            #
            # There are only two valid ways:
            # - we had never worked before (prev_none), and now choose A
            # - the previous last worked type was B (prev_b), and now choose A
            #
            # We CANNOT come from prev_a because that would mean doing A after A
            # in consecutive worked weeks, which is forbidden.
            #
            # Also, skipping is automatically handled because prev_a itself may
            # already include any number of skipped weeks from earlier.
            new_a: int = max(
                prev_a,                  # skip this week and keep last worked type A
                prev_none + aReward[i],  # first worked project is A this week
                prev_b + aReward[i],     # alternate from B to A
            )

            # Option 3: End this week with last worked type B.
            #
            # Symmetric reasoning to new_a:
            # - start working for the first time with B
            # - alternate from A to B
            # - or skip and keep the previous B-ending state
            new_b: int = max(
                prev_b,                  # skip this week and keep last worked type B
                prev_none + bReward[i],  # first worked project is B this week
                prev_a + bReward[i],     # alternate from A to B
            )

            # Commit updates for the next iteration.
            dp_none = new_none
            dp_a = new_a
            dp_b = new_b

        # The final answer is the best among:
        # - never having worked at all
        # - ending with A as the last worked type
        # - ending with B as the last worked type
        #
        # Because rewards are non-negative, "never worked" matters only in edge
        # cases like all zeros, but including it is logically complete.
        return max(dp_none, dp_a, dp_b)

    def maximumReward(self, aReward: List[int], bReward: List[int]) -> int:
        """
        Wrapper method using the problem-style naming convention.

        Args:
            aReward: Rewards for type A projects by week.
            bReward: Rewards for type B projects by week.

        Returns:
            The maximum total reward achievable.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.max_reward(aReward, bReward)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    a1: List[int] = [5, 1, 7, 4]
    b1: List[int] = [2, 10, 3, 8]
    result1: int = solution.maximumReward(a1, b1)
    print("Example 1 Result:", result1)  # Expected: 30

    # Example 2
    # Note:
    # The explanation in the prompt is inconsistent, but the correct DP result
    # for these arrays is 34:
    # A(9) -> B(8) -> A(1) -> B(1) = 19 is not best.
    # The actual optimum is:
    # A(9) on week 0, B(8) on week 1, A(1) on week 2, B(1) on week 3 = 19
    # or other alternating choices, but the true maximum computed by the DP is:
    # A(9), B(8), A(1), B(1) = 19
    # B(1), A(1), B(8), A(9) = 19
    #
    # However, if we compute carefully with the stated rules, the maximum for
    # these exact arrays is 34 only if one incorrectly allows same-type after skip.
    # Under the actual stated rule, skipping does not reset the last type.
    # Therefore the correct result for these arrays is 19.
    #
    # Since correctness is mandatory, we print the true result produced by the
    # valid algorithm for the stated rules.
    a2: List[int] = [9, 1, 1, 9]
    b2: List[int] = [1, 8, 8, 1]
    result2: int = solution.maximumReward(a2, b2)
    print("Example 2 Result:", result2)  # Correct under stated rules: 26? let's verify below

    # Additional explicit verification for Example 2:
    # Best valid plan under the stated rule is:
    # week 0: A = 9
    # week 1: B = 8
    # week 2: skip
    # week 3: A = 9
    # Total = 26
    #
    # This is valid because after week 1 the last worked type is B,
    # week 2 skip keeps last worked type as B,
    # week 3 choosing A alternates from B to A.
    #
    # So the correct answer for Example 2 under the stated rules is 26.
    # The prompt's claimed 34 is impossible with these arrays and rules.
    print("Example 2 Correct Expected Under Stated Rules: 26")