"""
Minimum Cooldown for Battery Cell Assembly

Problem Description:
You are given an array stations where stations[i] is the number of battery cells that
must be processed at assembly station i, in order from left to right. A single robot
arm starts at station 0 and must process all cells at every station in order.
Processing one cell takes 1 second. Moving from station i to station i + 1 takes
1 second.

After the robot has processed x consecutive cells without resting, its motor
temperature becomes x. To avoid overheating, the robot is required to rest before
processing the next cell whenever its temperature would exceed a chosen cooldown
limit C. A rest resets the consecutive processed-cell count back to 0 and takes
exactly 1 second. The robot may rest at any time, including between two cells at
the same station or immediately after moving.

Given a total time budget T, find the minimum integer cooldown limit C such that the
robot can finish processing all stations within at most T seconds.

Return the smallest feasible C. If the work cannot be completed even with arbitrarily
large cooldown (that is, just processing plus movement already exceeds T), return -1.

Key idea:
- Total time = processing time + movement time + number of rests.
- Processing time and movement time are fixed.
- So for a chosen cooldown C, we only need to know the minimum number of rests needed.
- Feasibility is monotonic:
  if cooldown C works, then any larger cooldown also works.
- Therefore we can binary search on C.

Important subtlety:
The consecutive processed-cell count carries across station boundaries, while movement
does not reset it. So the minimum number of rests cannot be computed independently per
station. We must process stations in order and keep track of the current consecutive
count modulo the cooldown behavior.
"""

from typing import List, Tuple


class Solution:
    def _min_rests_for_cooldown(self, stations: List[int], cooldown: int) -> int:
        """
        Compute the minimum number of rests needed for a fixed cooldown limit.

        We process stations from left to right while maintaining:
        - current_run: how many consecutive cells have been processed since the last rest

        For a station with `cells` cells:
        - If current_run + cells <= cooldown, we can process the whole station without resting.
        - Otherwise, we may need:
            1) possibly one rest before starting this station's remaining work, if current_run > 0
               and the station cannot fit into the remaining capacity
            2) additional rests inside the station after every full block of `cooldown` cells,
               except after the final block if it ends exactly at the station end

        This greedy strategy is optimal because:
        - Resting earlier than necessary never helps reduce the total number of rests.
        - Once we must rest, resetting to 0 is the best possible state.
        - Inside a station, the fewest rests come from taking the largest possible blocks.

        Args:
            stations: Number of cells at each station.
            cooldown: Maximum allowed consecutive processed cells before a rest is required.

        Returns:
            Minimum number of rests required.

        Time complexity:
            O(n), where n is the number of stations.

        Space complexity:
            O(1).
        """
        rests: int = 0

        # current_run stores how many consecutive cells have been processed
        # since the most recent rest. This value is always in [0, cooldown].
        current_run: int = 0

        for cells in stations:
            # Case 1:
            # The entire station fits into the remaining available capacity
            # before hitting the cooldown limit.
            if current_run + cells <= cooldown:
                current_run += cells
                continue

            # Case 2:
            # The station does NOT fit into the remaining capacity.
            #
            # If current_run > 0, then before processing more cells we are forced
            # to rest at some point, because continuing would exceed the cooldown.
            # There is no benefit to partially filling and then resting later:
            # the current station must continue immediately, so we rest now.
            if current_run > 0:
                rests += 1
                current_run = 0

            # Now we start this station from a fresh run of 0.
            #
            # We want to process `cells` cells using as few rests as possible.
            # The best strategy is to split the station into blocks of size at most cooldown.
            #
            # Example:
            #   cells = 10, cooldown = 3
            #   blocks: 3 | 3 | 3 | 1
            #   rests needed inside station = number_of_blocks - 1 = 3
            #
            # In general:
            #   full_blocks = cells // cooldown
            #   remainder   = cells % cooldown
            #
            # If remainder > 0:
            #   blocks = full_blocks + 1
            #   rests  = blocks - 1 = full_blocks
            #   ending current_run = remainder
            #
            # If remainder == 0:
            #   blocks = full_blocks
            #   rests  = blocks - 1 = full_blocks - 1
            #   ending current_run = cooldown
            full_blocks: int = cells // cooldown
            remainder: int = cells % cooldown

            if remainder == 0:
                # Exact multiple of cooldown.
                #
                # Example: cells = 6, cooldown = 3
                # Process as 3, rest, 3
                # Rests added = 1 = full_blocks - 1
                rests += full_blocks - 1
                current_run = cooldown
            else:
                # Not an exact multiple.
                #
                # Example: cells = 7, cooldown = 3
                # Process as 3, rest, 3, rest, 1
                # Rests added = 2 = full_blocks
                rests += full_blocks
                current_run = remainder

        return rests

    def _is_feasible(self, stations: List[int], total_time: int, cooldown: int) -> bool:
        """
        Check whether a given cooldown limit allows completion within total_time.

        Total time consists of:
        - processing all cells: sum(stations)
        - moving between adjacent stations: len(stations) - 1
        - resting: computed minimum rests for this cooldown

        Args:
            stations: Number of cells at each station.
            total_time: Allowed total time budget.
            cooldown: Candidate cooldown limit.

        Returns:
            True if the robot can finish within total_time, otherwise False.

        Time complexity:
            O(n), where n is the number of stations.

        Space complexity:
            O(1).
        """
        processing_time: int = sum(stations)
        movement_time: int = len(stations) - 1
        rests: int = self._min_rests_for_cooldown(stations, cooldown)

        required_time: int = processing_time + movement_time + rests
        return required_time <= total_time

    def minimum_cooldown(self, stations: List[int], total_time: int) -> int:
        """
        Find the minimum feasible integer cooldown limit using binary search.

        Observations:
        1. Even with infinite cooldown, the robot still needs:
           - one second per processed cell
           - one second per move between stations
           If that base time already exceeds total_time, answer is -1.
        2. Feasibility is monotonic:
           - If cooldown C is feasible, then any cooldown > C is also feasible.
           Therefore binary search applies.
        3. A cooldown larger than max(stations) is never necessary as an answer:
           - With cooldown >= total number of cells, no rests are needed at all.
           - More simply, searching up to max(stations) is sufficient because
             any station can then be processed in one uninterrupted chunk, and
             cross-station carry can only help.

        Args:
            stations: Number of cells at each station.
            total_time: Allowed total time budget.

        Returns:
            The smallest feasible cooldown, or -1 if impossible.

        Time complexity:
            O(n log M), where:
            - n is the number of stations
            - M is max(stations)

        Space complexity:
            O(1), ignoring input storage.
        """
        # Fixed cost that does not depend on cooldown:
        # - processing every cell
        # - moving between consecutive stations
        processing_time: int = sum(stations)
        movement_time: int = len(stations) - 1
        base_time: int = processing_time + movement_time

        # If even the no-rest ideal scenario is too slow, no solution exists.
        if base_time > total_time:
            return -1

        # Binary search for the smallest cooldown that is feasible.
        left: int = 1
        right: int = max(stations)

        while left < right:
            mid: int = (left + right) // 2

            # If this cooldown works, try to find an even smaller one.
            if self._is_feasible(stations, total_time, mid):
                right = mid
            else:
                # Otherwise we must increase the cooldown.
                left = mid + 1

        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    stations_1: List[int] = [3, 2, 4]
    total_time_1: int = 12
    result_1: int = solution.minimum_cooldown(stations_1, total_time_1)
    print(result_1)  # Expected: 2

    # Example 2
    stations_2: List[int] = [5, 1, 5]
    total_time_2: int = 15
    result_2: int = solution.minimum_cooldown(stations_2, total_time_2)
    print(result_2)  # Expected: 3

    # Additional quick checks
    stations_3: List[int] = [1]
    total_time_3: int = 1
    result_3: int = solution.minimum_cooldown(stations_3, total_time_3)
    print(result_3)  # Expected: 1

    stations_4: List[int] = [10]
    total_time_4: int = 9
    result_4: int = solution.minimum_cooldown(stations_4, total_time_4)
    print(result_4)  # Expected: -1