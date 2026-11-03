"""
Minimum Cost to Deploy Feature Flags Across Release Stages

A product team is preparing a release pipeline with n sequential stages, numbered
from 0 to n - 1. At each stage, exactly one feature flag configuration must be
deployed. There are m possible configurations, and deploying configuration j at
stage i has base cost cost[i][j].

However, switching configurations between consecutive stages may introduce
migration overhead. If stage i - 1 uses configuration a and stage i uses
configuration b, an additional transition cost transition[a][b] is paid.
Staying on the same configuration may still have a non-zero transition cost.

The pipeline must also satisfy milestone requirements. You are given an array
required of length n where required[i] is either -1 or a configuration index in
[0, m - 1]. If required[i] != -1, then stage i must use exactly that
configuration.

In addition, the release manager wants the final deployment plan to contain
exactly k contiguous blocks of equal configurations. A block is a maximal
consecutive segment where all stages use the same configuration.

Return the minimum total cost to choose configurations for all stages so that
all required stages are respected and the final plan has exactly k blocks.
If it is impossible, return -1.
"""

from typing import List


class Solution:
    def min_deployment_cost(
        self,
        n: int,
        m: int,
        k: int,
        cost: List[List[int]],
        transition: List[List[int]],
        required: List[int],
    ) -> int:
        """
        Compute the minimum total deployment cost with exactly k contiguous blocks.

        The dynamic programming state is:
            dp_prev[blocks][last_config] = minimum cost after processing stages
            up to the previous index, forming exactly `blocks` blocks, and ending
            with configuration `last_config`.

        We process stages from left to right and build the next DP layer.

        Args:
            n: Number of stages.
            m: Number of possible configurations.
            k: Required number of contiguous blocks.
            cost: Base deployment cost matrix, where cost[i][j] is the cost of
                using configuration j at stage i.
            transition: Transition cost matrix, where transition[a][b] is the
                extra cost when moving from configuration a at stage i - 1 to
                configuration b at stage i.
            required: Constraint array. If required[i] != -1, stage i must use
                exactly that configuration.

        Returns:
            The minimum total cost if a valid plan exists, otherwise -1.

        Time complexity:
            O(n * k * m^2)

        Space complexity:
            O(k * m)
        """
        # We use a very large number to represent an unreachable / impossible state.
        # Python integers do not overflow, so this is safe.
        inf: int = 10**30

        # dp_prev[b][c] means:
        # after processing stages up to the previous stage,
        # the minimum total cost to have exactly b blocks
        # and end with configuration c.
        dp_prev: List[List[int]] = [[inf] * m for _ in range(k + 1)]

        # -----------------------------
        # Initialize stage 0 separately
        # -----------------------------
        # Why do we handle stage 0 separately?
        # Because there is no previous stage, so:
        #   1) no transition cost is added yet
        #   2) choosing any valid configuration starts the first block
        #
        # Therefore, every valid choice at stage 0 creates exactly 1 block.
        allowed_first_configs: List[int]
        if required[0] == -1:
            allowed_first_configs = list(range(m))
        else:
            allowed_first_configs = [required[0]]

        for config in allowed_first_configs:
            dp_prev[1][config] = cost[0][config]

        # ---------------------------------------------------------
        # Process stages 1 through n - 1 using rolling DP arrays
        # ---------------------------------------------------------
        for stage in range(1, n):
            # dp_curr will store results after processing the current stage.
            dp_curr: List[List[int]] = [[inf] * m for _ in range(k + 1)]

            # Determine which configurations are allowed at this stage.
            # If required[stage] is fixed, we must use only that configuration.
            # Otherwise, all configurations are possible.
            if required[stage] == -1:
                allowed_configs = range(m)
            else:
                allowed_configs = [required[stage]]

            # We cannot have more blocks than stage + 1, because each stage can
            # contribute at most one new block.
            max_blocks_now: int = min(k, stage + 1)

            # Try every possible number of blocks after processing this stage.
            for blocks in range(1, max_blocks_now + 1):
                # Try every valid current configuration for this stage.
                for curr_config in allowed_configs:
                    base_cost: int = cost[stage][curr_config]

                    # We now consider all possible previous ending configurations.
                    # There are two cases:
                    #
                    # Case 1: prev_config == curr_config
                    #   The current stage continues the previous block.
                    #   So the number of blocks stays the same.
                    #
                    # Case 2: prev_config != curr_config
                    #   The current stage starts a new block.
                    #   So the number of blocks increases by 1.
                    #
                    # In both cases, we also add the transition cost from
                    # prev_config to curr_config.
                    for prev_config in range(m):
                        prev_same_block_cost: int = dp_prev[blocks][prev_config]
                        if prev_same_block_cost != inf and prev_config == curr_config:
                            candidate: int = (
                                prev_same_block_cost
                                + transition[prev_config][curr_config]
                                + base_cost
                            )
                            if candidate < dp_curr[blocks][curr_config]:
                                dp_curr[blocks][curr_config] = candidate

                        if blocks > 1:
                            prev_new_block_cost: int = dp_prev[blocks - 1][prev_config]
                            if prev_new_block_cost != inf and prev_config != curr_config:
                                candidate = (
                                    prev_new_block_cost
                                    + transition[prev_config][curr_config]
                                    + base_cost
                                )
                                if candidate < dp_curr[blocks][curr_config]:
                                    dp_curr[blocks][curr_config] = candidate

            # Move current layer into previous for the next iteration.
            dp_prev = dp_curr

        # After processing all stages, we need exactly k blocks.
        answer: int = min(dp_prev[k])

        return -1 if answer >= inf else answer

    def solve(
        self,
        n: int,
        m: int,
        k: int,
        cost: List[List[int]],
        transition: List[List[int]],
        required: List[int],
    ) -> int:
        """
        Wrapper method that calls the main DP solver.

        Args:
            n: Number of stages.
            m: Number of configurations.
            k: Required number of contiguous blocks.
            cost: Base deployment costs.
            transition: Transition costs between configurations.
            required: Required configuration per stage, or -1 if unrestricted.

        Returns:
            Minimum valid total cost, or -1 if impossible.

        Time complexity:
            O(n * k * m^2)

        Space complexity:
            O(k * m)
        """
        return self.min_deployment_cost(n, m, k, cost, transition, required)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 4
    m1 = 3
    k1 = 2
    cost1 = [
        [5, 1, 4],
        [2, 3, 6],
        [7, 2, 1],
        [4, 6, 3],
    ]
    transition1 = [
        [0, 5, 2],
        [4, 0, 3],
        [6, 1, 0],
    ]
    required1 = [-1, -1, -1, -1]
    result1 = solution.solve(n1, m1, k1, cost1, transition1, required1)
    print(result1)

    # Example 2
    n2 = 5
    m2 = 2
    k2 = 3
    cost2 = [
        [3, 8],
        [5, 1],
        [2, 4],
        [7, 2],
        [6, 3],
    ]
    transition2 = [
        [0, 10],
        [1, 0],
    ]
    required2 = [0, -1, -1, 1, -1]
    result2 = solution.solve(n2, m2, k2, cost2, transition2, required2)
    print(result2)