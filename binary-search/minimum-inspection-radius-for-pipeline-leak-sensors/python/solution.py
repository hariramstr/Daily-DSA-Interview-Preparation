"""
Title: Minimum Inspection Radius for Pipeline Leak Sensors

Problem Description:
A company operates a very long straight pipeline from position 0 to position L.
Along the pipeline, there are fixed leak sensors installed at known integer positions.
Each sensor can inspect every point within distance R of its own position, where R is
the same for all sensors. However, some sections of the pipeline are legally classified
as critical zones, and every point inside every critical zone must be covered by at least
one sensor. A critical zone is given as a closed interval [start, end], and zones may
overlap, touch, or be nested.

Your task is to compute the minimum integer inspection radius R such that the union of
the coverage intervals of all sensors covers every critical zone completely.

More formally, if a sensor is at position x, then it covers the interval [x - R, x + R].
Find the smallest integer R for which every critical interval [a, b] is a subset of the
union of all sensor coverage intervals.

Constraints:
- 1 <= sensors.length <= 2 * 10^5
- 1 <= zones.length <= 2 * 10^5
- 0 <= sensors[i] <= 10^18
- 0 <= zones[j][0] <= zones[j][1] <= 10^18
- L can be as large as 10^18, but you do not need to inspect points outside the listed
  critical zones
- The answer fits in a 64-bit signed integer

Examples:
1) sensors = [2, 8, 14], zones = [[1, 4], [6, 10], [13, 15]]
   Output: 2

2) sensors = [5, 11], zones = [[0, 3], [4, 8], [10, 14]]
   Output: 5

Intended approach:
- Merge critical zones first.
- Binary search the minimum radius R.
- For each candidate R, efficiently check whether the union of sensor coverage intervals
  covers every merged critical zone.
"""

from typing import List, Tuple


class Solution:
    def merge_zones(self, zones: List[List[int]]) -> List[Tuple[int, int]]:
        """
        Merge overlapping or touching critical zones into disjoint intervals.

        Args:
            zones: List of [start, end] critical intervals.

        Returns:
            A list of merged disjoint intervals as (start, end) tuples.

        Time complexity:
            O(m log m), where m is the number of zones, due to sorting.

        Space complexity:
            O(m) for the merged result.
        """
        # If there are no zones, there is nothing to merge.
        # The problem constraints say at least one zone exists, but handling this
        # case makes the method more robust and easier to reuse.
        if not zones:
            return []

        # Sort zones by starting point, then by ending point.
        # This is the standard first step for interval merging because once intervals
        # are ordered by start, we only need to compare each interval with the last
        # merged interval.
        zones.sort()

        merged: List[Tuple[int, int]] = []

        # Start with the first interval as the current active merged interval.
        current_start, current_end = zones[0]

        # Process the remaining intervals one by one.
        for start, end in zones[1:]:
            # If the next interval starts before or exactly at the current merged end,
            # then the intervals overlap.
            #
            # We also merge "touching" intervals. For closed intervals on the real line,
            # [a, b] and [b, c] already share the point b, so they overlap and should
            # definitely be merged.
            if start <= current_end:
                # Extend the current merged interval if needed.
                if end > current_end:
                    current_end = end
            else:
                # There is a genuine gap, so the current merged interval is complete.
                merged.append((current_start, current_end))
                current_start, current_end = start, end

        # Do not forget to append the final active interval.
        merged.append((current_start, current_end))
        return merged

    def can_cover(
        self,
        sensors: List[int],
        merged_zones: List[Tuple[int, int]],
        radius: int,
    ) -> bool:
        """
        Check whether all merged critical zones are fully covered by sensors with
        the given radius.

        Args:
            sensors: Sorted list of sensor positions.
            merged_zones: Disjoint merged critical intervals.
            radius: Candidate inspection radius.

        Returns:
            True if every merged zone is fully covered, otherwise False.

        Time complexity:
            O(n + k), where n is the number of sensors and k is the number of merged zones.

        Space complexity:
            O(1) extra space beyond the input and merged zones.
        """
        # We will sweep from left to right through the merged critical zones and sensors.
        #
        # Key idea:
        # - Each sensor contributes a coverage interval [sensor - radius, sensor + radius].
        # - Because sensors are sorted, these coverage intervals are also sorted by left endpoint.
        # - We do NOT need to explicitly build all coverage intervals.
        # - Instead, we greedily maintain the farthest point currently covered while scanning.
        #
        # For each merged critical zone [zone_start, zone_end]:
        #   1. Ensure coverage reaches zone_start.
        #   2. Repeatedly consume every sensor whose coverage starts at or before the next
        #      uncovered point. Such sensors can extend the covered frontier.
        #   3. If we cannot extend coverage to zone_end, then this radius is insufficient.
        #
        # This is linear because each sensor is processed at most once.

        sensor_index = 0
        sensor_count = len(sensors)

        # This variable represents the farthest coordinate covered continuously so far
        # while processing from left to right.
        #
        # Initially, nothing is covered, so we use a very small sentinel.
        covered_until = -10**30

        for zone_start, zone_end in merged_zones:
            # If previous coverage already reaches beyond this zone's end, then this
            # entire zone is already covered and we can skip it.
            if covered_until >= zone_end:
                continue

            # The next point inside this zone that still needs coverage.
            # If previous coverage reaches into the zone, we continue from there.
            # Otherwise, we must start covering from zone_start.
            need = max(zone_start, covered_until)

            # If covered_until is inside the zone, then points up to covered_until are
            # already covered. The next uncovered point is just after that.
            #
            # However, because we are working with closed intervals over the continuous
            # line, "just after covered_until" is conceptually infinitesimal. The greedy
            # condition for a sensor to help is that its left endpoint is <= covered_until.
            #
            # To unify both cases cleanly:
            # - If covered_until < zone_start, we need a sensor whose left endpoint <= zone_start.
            # - If covered_until >= zone_start, we need a sensor whose left endpoint <= covered_until.
            #
            # So using need = max(zone_start, covered_until) is correct.

            # Greedily absorb every sensor that can connect to the currently needed point.
            # A sensor at position x can help if x - radius <= need.
            #
            # Every such sensor may extend the covered frontier to x + radius.
            # We keep taking them while they connect, because overlapping/connecting
            # coverage intervals form one larger continuous covered segment.
            extended = True
            while extended:
                extended = False
                while sensor_index < sensor_count and sensors[sensor_index] - radius <= need:
                    right_end = sensors[sensor_index] + radius
                    if right_end > covered_until:
                        covered_until = right_end
                        extended = True
                    sensor_index += 1

                # If we have already covered the whole zone, success for this zone.
                if covered_until >= zone_end:
                    break

                # If coverage did not advance enough to reach the zone end, then we need
                # the next uncovered point to be connectable by another sensor.
                #
                # The next uncovered point is immediately after covered_until, and for a
                # closed continuous interval, a new sensor can connect only if its left
                # endpoint is <= covered_until. Since the inner loop already consumed all
                # such sensors, no further progress is possible.
                need = covered_until

            # After exhausting all connectable sensors, if we still do not reach zone_end,
            # then this radius fails.
            if covered_until < zone_end:
                return False

        # Every merged critical zone was fully covered.
        return True

    def minimum_inspection_radius(self, sensors: List[int], zones: List[List[int]]) -> int:
        """
        Compute the minimum integer radius needed so that all critical zones are covered.

        Args:
            sensors: Positions of sensors along the pipeline.
            zones: Critical closed intervals that must be fully covered.

        Returns:
            The smallest integer radius that covers every critical zone.

        Time complexity:
            O((n + m) log U + m log m + n log n),
            where:
            - n = number of sensors
            - m = number of zones
            - U = search range of the answer (up to about 10^18)

        Space complexity:
            O(m) for merged intervals, excluding sorting overhead.
        """
        # Sort sensors so that we can sweep from left to right efficiently.
        sensors.sort()

        # Merge critical zones first.
        #
        # Why this helps:
        # - If zones overlap or touch, checking them separately is redundant.
        # - After merging, we only need to ensure each disjoint required interval is covered.
        # - This simplifies the feasibility check and reduces work.
        merged_zones = self.merge_zones(zones)

        # Binary search on the answer.
        #
        # Why binary search works:
        # - If a radius R is sufficient, then any larger radius is also sufficient.
        # - This monotonic property is exactly what binary search needs.
        #
        # Search boundaries:
        # - Lower bound = 0
        # - Upper bound = a very safe value that definitely works
        #
        # A safe upper bound is the maximum distance needed from any zone endpoint to some
        # sensor, but computing a tight bound is unnecessary. Since coordinates are up to
        # 10^18 and the answer fits in 64-bit signed integer, 10^18 is enough here.
        left = 0
        right = 10**18

        while left < right:
            mid = (left + right) // 2

            # Check whether this candidate radius is enough.
            if self.can_cover(sensors, merged_zones, mid):
                # mid works, so try to find an even smaller valid radius.
                right = mid
            else:
                # mid does not work, so the answer must be larger.
                left = mid + 1

        # At loop end, left == right and is the minimum feasible radius.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    sensors1 = [2, 8, 14]
    zones1 = [[1, 4], [6, 10], [13, 15]]
    result1 = solution.minimum_inspection_radius(sensors1, zones1)
    print(result1)  # Expected: 2

    # Example 2
    sensors2 = [5, 11]
    zones2 = [[0, 3], [4, 8], [10, 14]]
    result2 = solution.minimum_inspection_radius(sensors2, zones2)
    print(result2)  # Expected: 5