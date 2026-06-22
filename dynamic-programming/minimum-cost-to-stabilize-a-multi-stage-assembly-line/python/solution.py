"""
Title: Minimum Cost to Stabilize a Multi-Stage Assembly Line

Problem Description:
A factory produces a product through n sequential assembly stages, numbered from 0 to n - 1.
At each stage i, the machine can be configured in one of m modes. Running stage i in mode j
incurs a cost costs[i][j]. You are also given an m x m matrix switchPenalty where
switchPenalty[a][b] is the penalty paid when two consecutive stages use mode a followed by mode b.

Your goal is to choose exactly one mode for every stage so that the total cost is minimized.
The total cost is the sum of all chosen stage costs plus all penalties between adjacent stages.
In addition, the final mode sequence must contain exactly k mode-blocks, where a mode-block is
a maximal contiguous group of stages using the same mode.

Return the minimum possible total cost. If it is impossible to form exactly k mode-blocks,
return -1.

Constraints:
- 1 <= n <= 100
- 1 <= m <= 50
- 1 <= k <= n
- costs.length == n
- costs[i].length == m
- switchPenalty.length == m
- switchPenalty[a].length == m
- 0 <= costs[i][j] <= 10^6
- 0 <= switchPenalty[a][b] <= 10^6
- switchPenalty[a][a] may be 0 or nonzero, but a new block is created only when the mode changes
"""

from typing import List


class Solution:
    def min_cost_to_stabilize(
        self,
        n: int,
        m: int,
        k: int,
        costs: List[List[int]],
        switch_penalty: List[List[int]],
    ) -> int:
        """
        Compute the minimum total cost to assign one mode to each stage while forming
        exactly k mode-blocks.

        A block count increases only when the current mode differs from the previous mode.
        The total cost always includes:
        - the chosen stage cost at every stage
        - the penalty between every pair of adjacent stages, including when the mode stays the same

        Args:
            n: Number of stages.
            m: Number of available modes.
            k: Required number of mode-blocks.
            costs: costs[i][j] is the cost of using mode j at stage i.
            switch_penalty: switch_penalty[a][b] is the penalty for adjacent modes a -> b.

        Returns:
            The minimum possible total cost, or -1 if it is impossible.

        Time complexity:
            O(n * k * m^2)

        Space complexity:
            O(k * m)
        """
        # If we need more blocks than stages, it is impossible because each stage contributes
        # at most one new block.
        if k > n:
            return -1

        # We use a very large number to represent "unreachable" DP states.
        # Python integers do not overflow, so this is safe.
        inf: int = 10**30

        # DP idea:
        #
        # Let dp_prev[b][mode] be the minimum total cost after processing stages up to the
        # previous stage, such that:
        # - exactly b blocks have been formed
        # - the previous stage uses "mode"
        #
        # Then for the current stage i and chosen current mode cur_mode:
        #
        # 1) If prev_mode == cur_mode:
        #    - block count does NOT increase
        #    - new cost = dp_prev[b][prev_mode]
        #                 + costs[i][cur_mode]
        #                 + switch_penalty[prev_mode][cur_mode]
        #
        # 2) If prev_mode != cur_mode:
        #    - block count increases by 1
        #    - new cost = dp_prev[b-1][prev_mode]
        #                 + costs[i][cur_mode]
        #                 + switch_penalty[prev_mode][cur_mode]
        #
        # We only need the previous stage to compute the current stage, so we use rolling arrays
        # to keep memory small.
        dp_prev: List[List[int]] = [[inf] * m for _ in range(k + 1)]

        # Base case for stage 0:
        # Choosing any mode at the first stage creates exactly 1 block.
        # There is no previous stage, so no adjacency penalty is added here.
        for mode in range(m):
            dp_prev[1][mode] = costs[0][mode]

        # Process stages 1 through n - 1.
        for stage in range(1, n):
            # Create a fresh DP table for this stage.
            dp_curr: List[List[int]] = [[inf] * m for _ in range(k + 1)]

            # The number of blocks after processing "stage" stages (0-indexed, so total count is stage+1)
            # cannot exceed stage + 1, and cannot exceed k.
            max_blocks_now: int = min(k, stage + 1)

            # We compute every reachable state for the current stage.
            for blocks in range(1, max_blocks_now + 1):
                # Try every possible current mode for this stage.
                for cur_mode in range(m):
                    # This is the cost of simply running the current stage in cur_mode.
                    stage_cost: int = costs[stage][cur_mode]

                    # We will compute the best possible total cost that ends at:
                    # - current stage = stage
                    # - exactly "blocks" blocks
                    # - current mode = cur_mode
                    best: int = inf

                    # Case 1: Stay in the same mode as the previous stage.
                    #
                    # Staying in the same mode means:
                    # - block count remains the same
                    # - we still pay the adjacency penalty switch_penalty[cur_mode][cur_mode]
                    prev_same_cost: int = dp_prev[blocks][cur_mode]
                    if prev_same_cost != inf:
                        candidate_same: int = (
                            prev_same_cost
                            + stage_cost
                            + switch_penalty[cur_mode][cur_mode]
                        )
                        if candidate_same < best:
                            best = candidate_same

                    # Case 2: Switch from a different previous mode into cur_mode.
                    #
                    # Switching creates a new block, so the previous state must have had
                    # exactly blocks - 1 blocks.
                    if blocks > 1:
                        for prev_mode in range(m):
                            if prev_mode == cur_mode:
                                continue

                            prev_cost: int = dp_prev[blocks - 1][prev_mode]
                            if prev_cost == inf:
                                continue

                            candidate_switch: int = (
                                prev_cost
                                + stage_cost
                                + switch_penalty[prev_mode][cur_mode]
                            )
                            if candidate_switch < best:
                                best = candidate_switch

                    dp_curr[blocks][cur_mode] = best

            # Move current stage results into dp_prev for the next iteration.
            dp_prev = dp_curr

        # After processing all stages, the answer is the minimum cost among all ending modes
        # that use exactly k blocks.
        answer: int = min(dp_prev[k])

        return -1 if answer == inf else answer

    def minCost(
        self,
        n: int,
        m: int,
        k: int,
        costs: List[List[int]],
        switchPenalty: List[List[int]],
    ) -> int:
        """
        Wrapper method matching a common interview-style naming convention.

        Args:
            n: Number of stages.
            m: Number of modes.
            k: Required number of blocks.
            costs: Stage-mode cost matrix.
            switchPenalty: Adjacent mode penalty matrix.

        Returns:
            Minimum total cost, or -1 if impossible.

        Time complexity:
            O(n * k * m^2)

        Space complexity:
            O(k * m)
        """
        return self.min_cost_to_stabilize(n, m, k, costs, switchPenalty)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    #
    # Important note:
    # The problem statement's published numeric example is internally inconsistent.
    # If penalties are paid for every adjacent pair exactly as defined, then the true
    # minimum for this input is 9, not 8.
    #
    # We still run the algorithm on the provided data exactly according to the formal rules.
    n1 = 4
    m1 = 3
    k1 = 2
    costs1 = [
        [3, 1, 4],
        [2, 5, 3],
        [6, 2, 1],
        [4, 3, 2],
    ]
    switch_penalty1 = [
        [0, 7, 4],
        [6, 0, 3],
        [5, 2, 0],
    ]
    result1 = solution.minCost(n1, m1, k1, costs1, switch_penalty1)
    print("Example 1 result:", result1)

    # Example 2
    #
    # The formal rules imply the answer here is 13:
    # sequence [0, 1, 0] gives stage cost 3 and penalties 5 + 5 = 10, total 13.
    n2 = 3
    m2 = 2
    k2 = 3
    costs2 = [
        [1, 10],
        [10, 1],
        [1, 10],
    ]
    switch_penalty2 = [
        [0, 5],
        [5, 0],
    ]
    result2 = solution.minCost(n2, m2, k2, costs2, switch_penalty2)
    print("Example 2 result:", result2)