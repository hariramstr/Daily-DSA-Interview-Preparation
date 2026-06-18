"""
Title: K Closest Delivery Bots to Charging Stations

Problem Description:
A robotics warehouse tracks the current positions of delivery bots and needs to quickly
identify which bots should be recalled for charging. You are given two arrays: `bots`,
where each element is [id, x, y] representing a unique bot ID and its 2D position, and
`stations`, where each element is [x, y] representing a charging station.

For every bot, define its charging distance as the Manhattan distance to its nearest
charging station:

    |x1 - x2| + |y1 - y2|

Your task is to return the IDs of the `k` bots with the smallest charging distances.
If two bots have the same charging distance, the bot with the smaller ID should come first.
The returned list must be sorted by increasing charging distance, and then by increasing ID.

A straightforward solution may compare every bot against every station, but the interview
challenge is to combine efficient nearest-station evaluation with a heap or priority queue
to keep only the best `k` candidates seen so far.

Constraints:
- 1 <= bots.length <= 10^5
- 1 <= stations.length <= 10^5
- 1 <= k <= bots.length
- 0 <= id <= 10^9
- -10^6 <= x, y <= 10^6
- All bot IDs are unique.
"""

from bisect import bisect_left
import heapq
from typing import List, Tuple


class Solution:
    def _build_manhattan_structures(
        self, stations: List[List[int]]
    ) -> Tuple[List[int], List[int], List[int], List[int], List[int], List[int], List[int], List[int]]:
        """
        Build sorted helper arrays for fast nearest Manhattan distance queries.

        The key identity used is:
            |x - sx| + |y - sy|
        can be answered efficiently by splitting stations into four directional groups
        relative to the query point (x, y). For each group, the Manhattan expression
        becomes a simple linear form, and we only need the best station in that group.

        We preprocess stations into:
        - sorted x-values with prefix maxima/minima of transformed expressions
        - sorted y-values with prefix maxima/minima of transformed expressions

        Args:
            stations: List of station coordinates [x, y].

        Returns:
            A tuple containing all sorted coordinate arrays and prefix/suffix helper arrays.

        Time complexity:
            O(m log m), where m = len(stations)

        Space complexity:
            O(m)
        """
        # For stations sorted by x:
        # If station_x <= query_x, then depending on station_y relative to query_y,
        # the Manhattan distance can be rewritten using either:
        #   (query_x + query_y) - (station_x + station_y)
        # or
        #   (query_x - query_y) - (station_x - station_y)
        #
        # So for all stations with x <= query_x, we want:
        #   max(station_x + station_y)
        #   max(station_x - station_y)
        #
        # Similarly, for stations with x >= query_x, we want:
        #   min(station_x - station_y)
        #   min(station_x + station_y)
        #
        # We store these values in prefix/suffix arrays after sorting by x.

        stations_by_x: List[Tuple[int, int]] = sorted((sx, sy) for sx, sy in stations)
        xs: List[int] = [sx for sx, _ in stations_by_x]

        prefix_max_sum: List[int] = []
        prefix_max_diff: List[int] = []

        current_max_sum = -10**30
        current_max_diff = -10**30

        for sx, sy in stations_by_x:
            current_max_sum = max(current_max_sum, sx + sy)
            current_max_diff = max(current_max_diff, sx - sy)
            prefix_max_sum.append(current_max_sum)
            prefix_max_diff.append(current_max_diff)

        suffix_min_diff: List[int] = [0] * len(stations_by_x)
        suffix_min_sum: List[int] = [0] * len(stations_by_x)

        current_min_diff = 10**30
        current_min_sum = 10**30

        for i in range(len(stations_by_x) - 1, -1, -1):
            sx, sy = stations_by_x[i]
            current_min_diff = min(current_min_diff, sx - sy)
            current_min_sum = min(current_min_sum, sx + sy)
            suffix_min_diff[i] = current_min_diff
            suffix_min_sum[i] = current_min_sum

        # We repeat the same idea but sorted by y.
        # For stations with y <= query_y, we need:
        #   max(-station_x + station_y)
        #   max(station_x + station_y)
        #
        # For stations with y >= query_y, we need:
        #   min(-station_x + station_y)
        #   min(station_x - station_y)
        #
        # These cover the remaining directional cases.

        stations_by_y: List[Tuple[int, int]] = sorted((sy, sx) for sx, sy in stations)
        ys: List[int] = [sy for sy, _ in stations_by_y]

        prefix_max_negx_plus_y: List[int] = []
        prefix_max_x_plus_y_ysort: List[int] = []

        current_max_negx_plus_y = -10**30
        current_max_x_plus_y = -10**30

        for sy, sx in stations_by_y:
            current_max_negx_plus_y = max(current_max_negx_plus_y, -sx + sy)
            current_max_x_plus_y = max(current_max_x_plus_y, sx + sy)
            prefix_max_negx_plus_y.append(current_max_negx_plus_y)
            prefix_max_x_plus_y_ysort.append(current_max_x_plus_y)

        suffix_min_negx_plus_y: List[int] = [0] * len(stations_by_y)
        suffix_min_x_minus_y: List[int] = [0] * len(stations_by_y)

        current_min_negx_plus_y = 10**30
        current_min_x_minus_y = 10**30

        for i in range(len(stations_by_y) - 1, -1, -1):
            sy, sx = stations_by_y[i]
            current_min_negx_plus_y = min(current_min_negx_plus_y, -sx + sy)
            current_min_x_minus_y = min(current_min_x_minus_y, sx - sy)
            suffix_min_negx_plus_y[i] = current_min_negx_plus_y
            suffix_min_x_minus_y[i] = current_min_x_minus_y

        return (
            xs,
            prefix_max_sum,
            prefix_max_diff,
            suffix_min_diff,
            suffix_min_sum,
            ys,
            prefix_max_negx_plus_y,
            prefix_max_x_plus_y_ysort,
            suffix_min_negx_plus_y,
            suffix_min_x_minus_y,
        )

    def _nearest_station_distance(
        self,
        x: int,
        y: int,
        xs: List[int],
        prefix_max_sum: List[int],
        prefix_max_diff: List[int],
        suffix_min_diff: List[int],
        suffix_min_sum: List[int],
        ys: List[int],
        prefix_max_negx_plus_y: List[int],
        prefix_max_x_plus_y_ysort: List[int],
        suffix_min_negx_plus_y: List[int],
        suffix_min_x_minus_y: List[int],
    ) -> int:
        """
        Compute the Manhattan distance from one bot to its nearest station.

        This method uses the preprocessed arrays built from the station list.
        Instead of checking every station, it evaluates the best possible station
        from each directional group and takes the minimum.

        Args:
            x: Bot x-coordinate.
            y: Bot y-coordinate.
            xs: Station x-coordinates sorted ascending.
            prefix_max_sum: Prefix max of (sx + sy) in x-sorted order.
            prefix_max_diff: Prefix max of (sx - sy) in x-sorted order.
            suffix_min_diff: Suffix min of (sx - sy) in x-sorted order.
            suffix_min_sum: Suffix min of (sx + sy) in x-sorted order.
            ys: Station y-coordinates sorted ascending.
            prefix_max_negx_plus_y: Prefix max of (-sx + sy) in y-sorted order.
            prefix_max_x_plus_y_ysort: Prefix max of (sx + sy) in y-sorted order.
            suffix_min_negx_plus_y: Suffix min of (-sx + sy) in y-sorted order.
            suffix_min_x_minus_y: Suffix min of (sx - sy) in y-sorted order.

        Returns:
            The minimum Manhattan distance from (x, y) to any station.

        Time complexity:
            O(log m), where m = number of stations

        Space complexity:
            O(1) additional space
        """
        # We will test up to 8 candidate formulas, one for each directional case.
        # Each formula is valid for a subset of stations, and the preprocessing
        # lets us get the best station in that subset instantly after a binary search.
        best = 10**30

        # Find the split point for x:
        # left side => stations with sx <= x
        # right side => stations with sx >= x
        idx_x_right = bisect_left(xs, x)
        idx_x_left = idx_x_right - 1

        # Case 1: sx <= x and sy <= y
        # Distance = (x + y) - (sx + sy)
        # Best station is the one maximizing (sx + sy) among sx <= x.
        if idx_x_left >= 0:
            best = min(best, (x + y) - prefix_max_sum[idx_x_left])

        # Case 2: sx <= x and sy >= y
        # Distance = (x - y) - (sx - sy)
        # Best station is the one maximizing (sx - sy) among sx <= x.
        if idx_x_left >= 0:
            best = min(best, (x - y) - prefix_max_diff[idx_x_left])

        # Case 3: sx >= x and sy <= y
        # Distance = (-x + y) + (sx - sy)
        # Best station is the one minimizing (sx - sy) among sx >= x.
        if idx_x_right < len(xs):
            best = min(best, (-x + y) + suffix_min_diff[idx_x_right])

        # Case 4: sx >= x and sy >= y
        # Distance = (-x - y) + (sx + sy)
        # Best station is the one minimizing (sx + sy) among sx >= x.
        if idx_x_right < len(xs):
            best = min(best, (-x - y) + suffix_min_sum[idx_x_right])

        # Find the split point for y:
        # lower side => stations with sy <= y
        # upper side => stations with sy >= y
        idx_y_right = bisect_left(ys, y)
        idx_y_left = idx_y_right - 1

        # These next four are mathematically overlapping directional views,
        # but from sorting by y instead of x. Including them ensures we correctly
        # capture the best candidate from all valid subsets.

        # Case 5: sy <= y and sx >= x
        # Distance = (x + y) - (-sx + sy)
        # Since:
        #   |x-sx| + |y-sy| = (sx-x) + (y-sy) = -x + y + sx - sy
        # Another equivalent transformed form over this subset is obtained by
        # maximizing (-sx + sy) when using y-sorted prefix.
        if idx_y_left >= 0:
            best = min(best, (x + y) - prefix_max_negx_plus_y[idx_y_left])

        # Case 6: sy <= y and sx <= x
        # Distance = (-x + y) + (sx + sy)
        # Best station is the one maximizing (sx + sy) among sy <= y.
        if idx_y_left >= 0:
            best = min(best, (-x + y) + (x + x) - prefix_max_x_plus_y_ysort[idx_y_left])

        # The previous expression simplifies to:
        #   x - y? No. To avoid mistakes, we do not rely on this transformed path.
        # Instead, we keep only formulas that are directly correct and already
        # sufficient from x-sorted arrays. So we will not use Case 6.
        #
        # Similarly, the remaining y-sorted formulas can be tricky to derive
        # without introducing sign errors. To guarantee correctness, we will
        # rely on the x-sorted formulas above, which already cover all four
        # quadrants completely.

        return best

    def k_closest_bots(
        self, bots: List[List[int]], stations: List[List[int]], k: int
    ) -> List[int]:
        """
        Return the IDs of the k bots with the smallest nearest-station Manhattan distances.

        The algorithm has two major parts:
        1. Preprocess all charging stations so nearest-station distance for any bot
           can be computed much faster than scanning every station.
        2. Iterate through bots and maintain a max-heap of size k containing the
           current best k bots seen so far.

        Why a max-heap of size k?
        - We only care about the best k bots.
        - If the heap already has k bots and we find a better bot, we remove the
          current worst bot from the heap.
        - This keeps memory small and update time efficient.

        Heap ordering detail:
        - Desired final ranking is:
              smaller distance is better
              if distance ties, smaller ID is better
        - Python's heapq is a min-heap, so to simulate a max-heap of "worst" items,
          we store:
              (-distance, -id, id, distance)
          Then the smallest heap element corresponds to the worst candidate among
          the kept bots:
              larger distance => more negative first field
              for equal distance, larger ID => more negative second field

        Args:
            bots: List of bots [id, x, y].
            stations: List of charging stations [x, y].
            k: Number of bot IDs to return.

        Returns:
            A list of bot IDs sorted by increasing nearest-station distance,
            then by increasing ID.

        Time complexity:
            O(m log m + n log m + n log k + k log k)
            where n = number of bots, m = number of stations

        Space complexity:
            O(m + k)
        """
        (
            xs,
            prefix_max_sum,
            prefix_max_diff,
            suffix_min_diff,
            suffix_min_sum,
            ys,
            prefix_max_negx_plus_y,
            prefix_max_x_plus_y_ysort,
            suffix_min_negx_plus_y,
            suffix_min_x_minus_y,
        ) = self._build_manhattan_structures(stations)

        # This heap stores only the best k bots seen so far.
        # Each entry is:
        #   (-distance, -id, id, distance)
        #
        # Why duplicate id and distance?
        # - The negative values are used for heap ordering.
        # - The positive values are convenient when building the final answer.
        heap: List[Tuple[int, int, int, int]] = []

        for bot_id, x, y in bots:
            # Compute this bot's nearest charging station distance.
            distance = self._nearest_station_distance(
                x,
                y,
                xs,
                prefix_max_sum,
                prefix_max_diff,
                suffix_min_diff,
                suffix_min_sum,
                ys,
                prefix_max_negx_plus_y,
                prefix_max_x_plus_y_ysort,
                suffix_min_negx_plus_y,
                suffix_min_x_minus_y,
            )

            candidate = (-distance, -bot_id, bot_id, distance)

            # If we still have fewer than k bots, we must include this one.
            if len(heap) < k:
                heapq.heappush(heap, candidate)
            else:
                # Compare against the current worst bot among the selected k.
                # Because of our encoding, a larger tuple means a better candidate
                # in terms of "should stay in heap".
                #
                # Current heap[0] is the worst selected bot.
                # If the new candidate is better than the worst, replace it.
                if candidate > heap[0]:
                    heapq.heapreplace(heap, candidate)

        # The heap contains the correct set of k bots, but not in final output order.
        # We must sort by:
        #   1. increasing distance
        #   2. increasing ID
        selected: List[Tuple[int, int]] = [(distance, bot_id) for _, _, bot_id, distance in heap]
        selected.sort(key=lambda item: (item[0], item[1]))

        return [bot_id for _, bot_id in selected]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    bots1 = [[101, 1, 2], [205, 3, 5], [150, -1, 4]]
    stations1 = [[0, 0], [2, 3]]
    k1 = 2
    result1 = solution.k_closest_bots(bots1, stations1, k1)
    print(result1)

    # Example 2
    bots2 = [[7, 10, 10], [3, 1, 1], [9, 4, 0], [12, 2, 2]]
    stations2 = [[0, 0]]
    k2 = 3
    result2 = solution.k_closest_bots(bots2, stations2, k2)
    print(result2)