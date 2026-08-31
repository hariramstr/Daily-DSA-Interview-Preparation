"""
Title: Minimum Lane Changes to Collect Ordered Checkpoints

Problem Description:
You are given a straight road divided into n positions, numbered from 0 to n - 1,
and exactly 3 lanes numbered 1 to 3. A vehicle starts at position 0 in lane 2.
Some positions contain a checkpoint token in one of the lanes, and some positions
may also contain a blocked lane due to road work.

Two arrays are provided:

- checkpoints, where checkpoints[i] is either 0 (no token at position i) or a
  lane number 1..3 indicating that a token must be collected at that position.
- blocked, where blocked[i] is either 0 (no blocked lane at position i) or a
  lane number 1..3 indicating that lane cannot be occupied at that position.

The vehicle moves from left to right, one position at a time. At each step, it may
stay in the same lane or switch to another lane before entering the next position.
Every lane switch costs 1. Moving forward costs 0. A token at position i is collected
only if the vehicle is in checkpoints[i] when passing that position. You must collect
all required tokens in increasing position order, which happens automatically if you
visit the required lane at each token position.

Return the minimum number of lane changes needed to reach position n - 1 while
collecting every token. If it is impossible, return -1.

Constraints:
- 1 <= n <= 100000
- checkpoints.length == blocked.length == n
- checkpoints[i] is in {0, 1, 2, 3}
- blocked[i] is in {0, 1, 2, 3}
- If checkpoints[i] != 0, then checkpoints[i] != blocked[i]
- blocked[0] != 2
"""

from typing import List


class Solution:
    def min_lane_changes(self, checkpoints: List[int], blocked: List[int]) -> int:
        """
        Compute the minimum number of lane changes needed to reach the last position
        while collecting every required checkpoint token.

        The algorithm uses dynamic programming over positions and lanes.
        For each position, we track the minimum number of lane changes needed to be
        in each of the 3 lanes at that exact position, while satisfying:
        1. The lane is not blocked at that position.
        2. If there is a checkpoint token at that position, we must be in that lane.

        Args:
            checkpoints: List where checkpoints[i] is 0 or the required lane at position i.
            blocked: List where blocked[i] is 0 or the blocked lane at position i.

        Returns:
            The minimum number of lane changes required, or -1 if impossible.

        Time complexity:
            O(n), because for each of n positions we process only 3 lanes.

        Space complexity:
            O(1), because we store DP values for only the previous and current position.
        """
        n: int = len(checkpoints)

        # We use a large number to represent an impossible state.
        # This is safer than using None because arithmetic with integers stays simple.
        inf: int = 10**15

        # ------------------------------------------------------------
        # Step 1: Initialize DP for position 0.
        #
        # dp[lane] will mean:
        #   minimum lane changes needed to be at the current position
        #   in the given lane.
        #
        # Since lanes are 1..3, we will store them in indices 0..2:
        #   index 0 -> lane 1
        #   index 1 -> lane 2
        #   index 2 -> lane 3
        #
        # The vehicle starts at position 0 in lane 2 with cost 0.
        # It does NOT get to switch lanes "before" position 0, because the problem
        # states it starts there already.
        #
        # So initially:
        #   lane 2 has cost 0
        #   lane 1 and lane 3 are impossible at position 0
        # ------------------------------------------------------------
        dp: List[int] = [inf, 0, inf]

        # ------------------------------------------------------------
        # Step 2: Validate position 0 against road rules.
        #
        # Even though the start lane is fixed as lane 2, we still must ensure:
        # - lane 2 is not blocked at position 0
        # - if there is a checkpoint at position 0, it must be in lane 2
        #
        # The constraint guarantees blocked[0] != 2, but we still write the logic
        # clearly for correctness and readability.
        # ------------------------------------------------------------
        for lane in range(1, 4):
            idx: int = lane - 1

            # If this lane is blocked at position 0, it cannot be occupied.
            if blocked[0] == lane:
                dp[idx] = inf

            # If there is a checkpoint at position 0, only that lane is valid.
            if checkpoints[0] != 0 and checkpoints[0] != lane:
                dp[idx] = inf

        # If even the starting state is invalid, the journey is impossible.
        if min(dp) >= inf:
            return -1

        # ------------------------------------------------------------
        # Step 3: Process positions from left to right.
        #
        # Transition idea:
        # To enter position i in some target lane:
        # - We may come from the same lane at position i-1 with no extra cost.
        # - Or we may come from one of the other two lanes at position i-1
        #   and pay +1 for a lane change before entering position i.
        #
        # Then we must also enforce:
        # - target lane is not blocked at position i
        # - if position i has a checkpoint, target lane must equal that checkpoint lane
        #
        # Because there are only 3 lanes, checking all previous lanes is constant work.
        # ------------------------------------------------------------
        for pos in range(1, n):
            # Start with all lanes impossible for the new position.
            new_dp: List[int] = [inf, inf, inf]

            # Try to place the vehicle in each possible target lane at this position.
            for target_lane in range(1, 4):
                target_idx: int = target_lane - 1

                # ----------------------------------------------------
                # Rule A: If this lane is blocked at the current position,
                # we cannot stand here in this lane at all.
                # ----------------------------------------------------
                if blocked[pos] == target_lane:
                    continue

                # ----------------------------------------------------
                # Rule B: If there is a checkpoint token at this position,
                # we must be exactly in that lane to collect it.
                # Any other lane is invalid.
                # ----------------------------------------------------
                if checkpoints[pos] != 0 and checkpoints[pos] != target_lane:
                    continue

                # ----------------------------------------------------
                # Now compute the best way to arrive in target_lane.
                #
                # We examine all 3 possible previous lanes:
                # - same lane => cost +0
                # - different lane => cost +1
                #
                # Since lane changes happen before entering the next position,
                # this exactly models the problem statement.
                # ----------------------------------------------------
                best_cost: int = inf

                for prev_lane in range(1, 4):
                    prev_idx: int = prev_lane - 1

                    # If previous state was impossible, skip it.
                    if dp[prev_idx] >= inf:
                        continue

                    # No cost if we stay in the same lane.
                    # Cost 1 if we switch lanes.
                    switch_cost: int = 0 if prev_lane == target_lane else 1
                    candidate: int = dp[prev_idx] + switch_cost

                    if candidate < best_cost:
                        best_cost = candidate

                new_dp[target_idx] = best_cost

            # Move DP window forward.
            dp = new_dp

            # If all lanes are impossible at this position, we can stop early.
            if min(dp) >= inf:
                return -1

        # ------------------------------------------------------------
        # Step 4: The answer is the minimum cost among all valid lanes
        # at the final position.
        # ------------------------------------------------------------
        answer: int = min(dp)
        return -1 if answer >= inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    checkpoints_1: List[int] = [0, 0, 1, 0, 3]
    blocked_1: List[int] = [0, 3, 0, 2, 0]
    result_1: int = solution.min_lane_changes(checkpoints_1, blocked_1)
    print("Example 1 result:", result_1)  # Expected: 2

    # Example 2
    checkpoints_2: List[int] = [0, 2, 0, 1]
    blocked_2: List[int] = [0, 0, 2, 1]
    result_2: int = solution.min_lane_changes(checkpoints_2, blocked_2)
    print("Example 2 result:", result_2)  # Expected: -1

    # Additional simple test
    checkpoints_3: List[int] = [0]
    blocked_3: List[int] = [0]
    result_3: int = solution.min_lane_changes(checkpoints_3, blocked_3)
    print("Additional test result:", result_3)  # Expected: 0