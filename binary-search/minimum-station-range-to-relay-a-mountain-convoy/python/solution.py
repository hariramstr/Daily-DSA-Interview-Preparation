"""
Title: Minimum Station Range to Relay a Mountain Convoy

Problem Description:
A rescue convoy must travel from checkpoint 0 to checkpoint D along a one-dimensional
mountain road. Along the road, there are n possible relay station locations given in
sorted order by the array positions, where positions[i] is the distance of station i
from checkpoint 0. Each station can be activated or skipped. If activated, a station
with transmission range R can relay commands to any other activated station or checkpoint
whose distance is at most R away. Checkpoint 0 and checkpoint D behave like fixed endpoints
that are always available, but they do not count toward the station limit.

You are also given an integer k. Due to budget constraints, you may activate at most k
stations. Your task is to compute the minimum integer range R such that commands can be
relayed from checkpoint 0 to checkpoint D using at most k activated stations.

In other words, after choosing at most k stations, there must exist a chain starting at 0
and ending at D where every consecutive pair in the chain is at distance at most R.

Return the minimum possible R.

Constraints:
- 1 <= n <= 200000
- 1 <= D <= 10^18
- 0 < positions[i] < D
- positions is strictly increasing
- 0 <= k <= n
- All answers fit in a 64-bit signed integer
"""

from typing import List


class Solution:
    def _required_stations(self, positions: List[int], d: int, r: int) -> int:
        """
        Compute the minimum number of activated stations needed for a fixed range r.

        The key greedy idea:
        - Start at checkpoint 0.
        - Repeatedly jump as far right as possible within distance r.
        - If we can reach D directly, we stop.
        - Otherwise, we must activate the farthest station we can reach next.
        - This greedy strategy minimizes the number of stations because every chosen
          station maximizes progress toward D.

        Args:
            positions: Sorted station positions along the road.
            d: Final checkpoint position.
            r: Candidate transmission range.

        Returns:
            The minimum number of stations required to connect 0 to D using hops
            of length at most r. If impossible, returns a value larger than len(positions).

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        n: int = len(positions)

        # "current" is the current point in the relay chain.
        # It starts at checkpoint 0, which is always available and does not count as a station.
        current: int = 0

        # "i" scans through the sorted station list only once.
        # Because positions is sorted and current only moves right, a single linear pass is enough.
        i: int = 0

        # Count how many stations we activate.
        used: int = 0

        # Keep extending the chain until D becomes reachable.
        while current + r < d:
            # We want the farthest station position <= current + r.
            # Such a station is reachable in one hop from "current".
            farthest_reachable: int = current

            # Move pointer forward while stations are reachable from current.
            # Among all reachable stations, the last one is the farthest because positions is sorted.
            while i < n and positions[i] <= current + r:
                farthest_reachable = positions[i]
                i += 1

            # If we could not move at all, then there is a gap larger than r.
            # That means no valid chain exists for this r.
            if farthest_reachable == current:
                return n + 1

            # We must activate this farthest reachable station.
            # Greedy correctness:
            # choosing any earlier reachable station would leave us no farther right,
            # so it can never reduce the number of stations needed.
            current = farthest_reachable
            used += 1

        # Once current + r >= d, checkpoint D is reachable directly from current.
        return used

    def min_station_range(self, positions: List[int], d: int, k: int) -> int:
        """
        Find the minimum integer range R such that 0 can connect to D using at most k stations.

        We binary-search the answer R because:
        - If a certain range R is feasible, then any larger range is also feasible.
        - This monotonic property makes binary search valid.

        For each candidate R, we run a greedy feasibility check that computes the
        minimum number of stations required.

        Args:
            positions: Sorted station positions.
            d: Final checkpoint position.
            k: Maximum number of stations allowed to activate.

        Returns:
            The minimum feasible integer range.

        Time complexity:
            O(n log D)

        Space complexity:
            O(1)
        """
        # Lower bound:
        # The range cannot be negative, and 0 is a valid starting lower bound for binary search.
        left: int = 0

        # Upper bound:
        # Range D is always enough because then 0 can directly reach D with no stations.
        right: int = d

        # Standard binary search on the answer.
        while left < right:
            mid: int = (left + right) // 2

            # Compute the minimum number of stations needed if every hop can be at most "mid".
            needed: int = self._required_stations(positions, d, mid)

            # If we can do it with at most k stations, then "mid" is feasible.
            # Try to find an even smaller feasible range.
            if needed <= k:
                right = mid
            else:
                # Otherwise, "mid" is too small, so we must increase the range.
                left = mid + 1

        # At the end, left == right and points to the smallest feasible range.
        return left


if __name__ == "__main__":
    solution = Solution()

    positions1: List[int] = [2, 5, 8, 12]
    d1: int = 15
    k1: int = 2
    result1: int = solution.min_station_range(positions1, d1, k1)
    print(result1)  # Expected: 7

    positions2: List[int] = [4, 9, 14, 20, 27]
    d2: int = 30
    k2: int = 3
    result2: int = solution.min_station_range(positions2, d2, k2)
    print(result2)  # Correct result for these inputs: 10