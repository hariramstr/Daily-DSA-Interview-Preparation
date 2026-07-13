"""
Title: Minimum Cost to Publish Articles with Category Cooldown

Problem Description:
You are building an automated homepage for a news platform. There are n articles to
publish in a fixed order from left to right. For each position i, you may choose
exactly one of three categories for the article slot: Politics, Sports, or Tech.
Assigning category c to position i has a given publishing cost cost[i][c].

To keep the homepage varied, the editor enforces a cooldown rule: a category used
in one position cannot be used again in either of the next two positions. In other
words, if position i uses Sports, then positions i+1 and i+2 cannot use Sports.

Your task is to compute the minimum total publishing cost to assign categories to
all article slots while satisfying this rule.

Return the minimum possible total cost. If it is impossible to assign categories to
all positions under the cooldown rule, return -1.

This is a dynamic programming problem because the best choice for the current
position depends on the categories used in the previous two positions. A brute-force
search over all assignments is too slow for large n.

Constraints:
- 1 <= n <= 100000
- cost.length == n
- cost[i].length == 3
- 0 <= cost[i][c] <= 1000000000
- Categories are indexed as 0 = Politics, 1 = Sports, 2 = Tech
"""

from typing import List, Dict, Tuple


class Solution:
    def min_publish_cost(self, cost: List[List[int]]) -> int:
        """
        Compute the minimum total publishing cost under the category cooldown rule.

        The cooldown rule says a category used at position i cannot be used again
        at positions i+1 or i+2. Since there are exactly 3 categories total, this
        means that for every position starting from index 2, the current category
        must be different from the previous two categories.

        Dynamic programming state:
        - We only need to remember the categories used in the previous two positions.
        - Let a state be (prev2, prev1), meaning:
          prev2 = category used at position i-2
          prev1 = category used at position i-1
        - Then for position i, we try each category cur that is different from both
          prev2 and prev1.

        Args:
            cost: A list where cost[i][c] is the publishing cost of assigning
                  category c to position i.

        Returns:
            The minimum total cost if a valid assignment exists, otherwise -1.

        Time complexity:
            O(n * 9 * 3), which simplifies to O(n), because there are only 9 possible
            (prev2, prev1) states and 3 category choices per state.

        Space complexity:
            O(1) auxiliary DP space, because the number of states is constant.
        """
        n: int = len(cost)

        # Defensive validation:
        # The problem guarantees valid input shape, but we keep this check to make
        # the solution beginner-friendly and robust if reused elsewhere.
        if n == 0:
            return -1

        # Base case for n == 1:
        # We can choose any one of the 3 categories because there are no previous
        # positions to violate the cooldown rule.
        if n == 1:
            return min(cost[0])

        # Base case for n == 2:
        # The cooldown rule also forbids using the same category in the next position,
        # so positions 0 and 1 must use different categories.
        if n == 2:
            best_two: int = float("inf")
            for c0 in range(3):
                for c1 in range(3):
                    if c0 != c1:
                        best_two = min(best_two, cost[0][c0] + cost[1][c1])
            return best_two if best_two != float("inf") else -1

        # For n >= 3, we use dynamic programming over the last two chosen categories.
        #
        # dp[(a, b)] = minimum total cost after processing positions up to the current
        # stage, where:
        #   - the second-last chosen category is a
        #   - the last chosen category is b
        #
        # We initialize dp using the first two positions.
        dp: Dict[Tuple[int, int], int] = {}

        # Build all valid assignments for positions 0 and 1.
        # They must be different because the same category cannot appear in the next
        # two positions, which includes the adjacent position.
        for c0 in range(3):
            for c1 in range(3):
                if c0 != c1:
                    dp[(c0, c1)] = cost[0][c0] + cost[1][c1]

        # Process positions from index 2 to n - 1.
        for i in range(2, n):
            # next_dp will store the best costs for states ending at position i.
            next_dp: Dict[Tuple[int, int], int] = {}

            # For every previously reachable state (prev2, prev1),
            # try assigning a category cur to the current position.
            for (prev2, prev1), total_cost_so_far in dp.items():
                # The current category must be different from both previous categories.
                # This directly enforces the cooldown rule:
                # - cur != prev1 prevents repeating after 1 position
                # - cur != prev2 prevents repeating after 2 positions
                for cur in range(3):
                    if cur == prev1 or cur == prev2:
                        continue

                    # New state after choosing cur:
                    # the last two categories become (prev1, cur)
                    new_state: Tuple[int, int] = (prev1, cur)
                    new_cost: int = total_cost_so_far + cost[i][cur]

                    # Keep only the minimum cost for each state.
                    if new_state not in next_dp or new_cost < next_dp[new_state]:
                        next_dp[new_state] = new_cost

            # If no states are reachable, then no valid assignment exists.
            if not next_dp:
                return -1

            # Move to the next layer of DP.
            dp = next_dp

        # The answer is the minimum cost among all valid ending states.
        return min(dp.values()) if dp else -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Note:
    # The prompt's explanation text is inconsistent in places, but the stated
    # minimum valid assignment is [1, 2, 0, 1], which has cost:
    # position 0 -> category 1: 2
    # position 1 -> category 2: 4
    # position 2 -> category 0: 2
    # position 3 -> category 1: 4
    # total = 12
    #
    # So for the exact matrix shown in the prompt, the mathematically correct
    # answer is 12, not 10.
    #
    # Our algorithm correctly enforces the cooldown rule and computes the true
    # minimum for the given costs.
    cost1: List[List[int]] = [
        [3, 2, 7],
        [5, 1, 4],
        [2, 6, 3],
        [8, 4, 5],
    ]
    result1: int = solution.min_publish_cost(cost1)
    print("Example 1 result:", result1)  # Correct result for the provided matrix: 12

    # Example 2 from the prompt.
    # Valid choices for two positions must use different categories.
    # Best is category 0 first (cost 1), then category 1 or 2 second (cost 10).
    # Total = 11.
    cost2: List[List[int]] = [
        [1, 10, 10],
        [1, 10, 10],
    ]
    result2: int = solution.min_publish_cost(cost2)
    print("Example 2 result:", result2)  # Expected: 11

    # Additional small sanity checks for beginners.

    # Single position: just choose the cheapest category.
    cost3: List[List[int]] = [
        [9, 4, 6],
    ]
    result3: int = solution.min_publish_cost(cost3)
    print("Single position result:", result3)  # Expected: 4

    # Three positions:
    # Because each category cannot repeat within distance 2 and there are exactly
    # three categories, all three positions must use all three categories exactly once.
    cost4: List[List[int]] = [
        [1, 100, 100],
        [100, 2, 100],
        [100, 100, 3],
    ]
    result4: int = solution.min_publish_cost(cost4)
    print("Three positions result:", result4)  # Expected: 6