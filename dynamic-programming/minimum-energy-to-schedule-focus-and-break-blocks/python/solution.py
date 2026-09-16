"""
Title: Minimum Energy to Schedule Focus and Break Blocks

Problem Description:
A productivity app plans a day as a sequence of tasks. For each task i, you are given
two energy costs: focusCost[i] and breakCost[i]. You must assign every task to exactly
one mode: Focus or Break. The total energy spent is the sum of the chosen costs, but
there is an additional rule: no more than k consecutive tasks may be assigned to Focus
mode, because long uninterrupted focus sessions are not allowed.

Your goal is to compute the minimum total energy needed to schedule all tasks while
respecting the consecutive-focus limit.

Formally, given two integer arrays focusCost and breakCost of length n and an integer k,
choose for each index i either focusCost[i] or breakCost[i]. If you choose Focus for
task i, it contributes focusCost[i] to the total. If you choose Break for task i, it
contributes breakCost[i] to the total. In the final assignment, every maximal consecutive
run of Focus tasks must have length at most k.

Return the minimum possible total energy.

Constraints:
- 1 <= n <= 100000
- 1 <= k <= n
- focusCost.length == breakCost.length == n
- 1 <= focusCost[i], breakCost[i] <= 1000000000
"""

from collections import deque
from typing import Deque, List, Tuple


class Solution:
    def minimum_energy(self, focusCost: List[int], breakCost: List[int], k: int) -> int:
        """
        Compute the minimum total energy needed to assign each task to Focus or Break
        such that no run of consecutive Focus tasks exceeds length k.

        Args:
            focusCost: Energy cost if task i is assigned to Focus.
            breakCost: Energy cost if task i is assigned to Break.
            k: Maximum allowed length of any consecutive Focus run.

        Returns:
            The minimum possible total energy.

        Time complexity:
            O(n)

        Space complexity:
            O(k)
        """
        n: int = len(focusCost)

        # We transform the problem into a very convenient dynamic programming form.
        #
        # Let:
        #   base = sum of all break costs
        #
        # Imagine we first assign every task to Break. That costs:
        #   breakCost[0] + breakCost[1] + ... + breakCost[n-1]
        #
        # If we later switch task i from Break to Focus, the total cost changes by:
        #   focusCost[i] - breakCost[i]
        #
        # Define:
        #   delta[i] = focusCost[i] - breakCost[i]
        #
        # Then the final total cost becomes:
        #   base + sum(delta[i] for every task chosen as Focus)
        #
        # So now the problem becomes:
        #   Choose some positions to be Focus, minimizing the sum of chosen deltas,
        #   with the restriction that no more than k consecutive positions are chosen.
        #
        # This is equivalent to saying:
        #   In every block of length k + 1, at least one task must be Break.
        #
        # Dynamic programming idea:
        #   Let dp[i] be the minimum total "adjustment cost" for tasks 0..i-1
        #   (that is, the first i tasks), while satisfying the rule.
        #
        # To compute dp[i], consider the last Break among the first i tasks.
        # Suppose the last Break is at position j-1 (1-based in dp terms, 0-based in arrays).
        # Then tasks j..i-1 are all Focus, so their count is i - j and must be <= k.
        #
        # If task j-1 is Break, then:
        #   cost = dp[j-1] + break at (j-1) + focus on j..i-1
        #
        # Using prefix sums of delta, this can be rewritten into a form where we need
        # the minimum of:
        #   dp[t] - prefix_delta[t+1]
        # over a sliding window of valid t values.
        #
        # We maintain that minimum efficiently with a monotonic deque, giving O(n) time.

        delta: List[int] = [focusCost[i] - breakCost[i] for i in range(n)]

        # Prefix sums of delta:
        # prefix[i] = delta[0] + delta[1] + ... + delta[i-1]
        # So prefix[0] = 0, prefix[1] = delta[0], ..., prefix[n] = sum(delta)
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + delta[i]

        # dp[i] = minimum adjustment cost for first i tasks
        dp: List[int] = [0] * (n + 1)

        # Special handling for dp[0]:
        # No tasks means zero adjustment cost.
        dp[0] = 0

        # We need a deque of candidate indices t for transitions.
        #
        # For i >= 1, one valid transition is:
        #   choose the last Break at position t (0-based),
        #   where the suffix after that break up to i-1 is all Focus.
        #
        # The derived formula becomes:
        #   dp[i] = prefix[i] + min( dp[t] - prefix[t + 1] ) over valid t in [i-k-1, i-1]
        # There is also the case where the first i tasks are all Focus, which is valid only if i <= k:
        #   dp[i] = prefix[i]
        #
        # To support the min efficiently, each candidate t contributes:
        #   value(t) = dp[t] - prefix[t + 1]
        #
        # The valid range of t changes as i increases, specifically:
        #   t >= i - k - 1
        #
        # We keep candidates in increasing index order, and their values in increasing
        # order as well (monotonic deque), so the front always gives the minimum.
        candidates: Deque[Tuple[int, int]] = deque()

        for i in range(1, n + 1):
            # Step 1: Add the new candidate t = i - 1 for future and current transitions.
            #
            # This candidate corresponds to placing a Break at task (i - 1).
            # Its associated value is:
            #   dp[i - 1] - prefix[i]
            #
            # Why prefix[i]?
            # Because t + 1 = i, so prefix[t + 1] = prefix[i].
            new_index: int = i - 1
            new_value: int = dp[i - 1] - prefix[i]

            # Maintain monotonic increasing values in the deque.
            # If the new candidate has a smaller or equal value than the back,
            # the back will never be better for any future i, so we remove it.
            while candidates and candidates[-1][1] >= new_value:
                candidates.pop()
            candidates.append((new_index, new_value))

            # Step 2: Remove candidates that are no longer valid.
            #
            # For current i, valid t must satisfy:
            #   i - t - 1 <= k
            # which rearranges to:
            #   t >= i - k - 1
            #
            # So any candidate with index < i - k - 1 is too old and must be removed.
            min_valid_index: int = i - k - 1
            while candidates and candidates[0][0] < min_valid_index:
                candidates.popleft()

            # Step 3: Compute dp[i].
            #
            # Option A: All first i tasks are Focus.
            # This is allowed only if i <= k.
            best: int = prefix[i] if i <= k else 10**30

            # Option B: Use one of the valid candidates from the deque.
            # The best candidate is at the front because the deque is monotonic by value.
            if candidates:
                best = min(best, prefix[i] + candidates[0][1])

            dp[i] = best

        # Convert back from "adjustment cost" to actual total energy.
        total_break_cost: int = sum(breakCost)
        return total_break_cost + dp[n]


if __name__ == "__main__":
    solution = Solution()

    focus_cost_1 = [3, 2, 5, 1]
    break_cost_1 = [4, 6, 1, 7]
    k_1 = 2
    result_1 = solution.minimum_energy(focus_cost_1, break_cost_1, k_1)
    print(result_1)  # Expected: 7

    focus_cost_2 = [1, 1, 1, 1]
    break_cost_2 = [10, 10, 10, 10]
    k_2 = 2
    result_2 = solution.minimum_energy(focus_cost_2, break_cost_2, k_2)
    print(result_2)  # Expected: 13