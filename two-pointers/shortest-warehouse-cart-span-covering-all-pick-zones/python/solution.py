"""
Title: Shortest Warehouse Cart Span Covering All Pick Zones

Problem Description:
A warehouse management system records the sequence of pick zones visited by a cart
during a single route. Each zone is represented by a string such as "A1", "Cold",
or "Fragile". You are given an array zones, where zones[i] is the zone visited at
step i, and another array requiredZones listing distinct zones that must all appear
in a valid segment.

Your task is to find the length of the shortest contiguous segment of the route that
contains every zone in requiredZones at least once. If no such segment exists,
return -1.

This problem models a common optimization task: finding the smallest portion of an
event stream that satisfies a complete set of operational requirements. The segment
must be contiguous, and extra zones not in requiredZones are allowed inside the
segment.

Return only the minimum length, not the segment itself.

Constraints:
- 1 <= zones.length <= 200000
- 1 <= requiredZones.length <= 5000
- requiredZones contains distinct strings
- 1 <= zones[i].length, requiredZones[i].length <= 20
- Strings consist of letters, digits, underscores, or hyphens

Example 1:
Input:
    zones = ["Inbound","A1","Cold","B2","Fragile","Cold","A1","Packing"]
    requiredZones = ["A1","Cold","Fragile"]
Output:
    3
Explanation:
    The shortest valid segment is ["A1","Cold","Fragile"], which has length 3.

Example 2:
Input:
    zones = ["Dock","A1","B2","Dock","Packing"]
    requiredZones = ["A1","Cold"]
Output:
    -1
Explanation:
    The zone "Cold" never appears in the route, so no valid contiguous segment exists.
"""

from typing import Dict, List


class Solution:
    def shortest_span_covering_all_zones(
        self, zones: List[str], required_zones: List[str]
    ) -> int:
        """
        Find the length of the shortest contiguous segment containing all required zones.

        This method uses the classic sliding window / two-pointers technique.
        We expand the right pointer to include more zones until the window becomes valid
        (contains every required zone at least once). Then we move the left pointer
        forward as much as possible while keeping the window valid, which helps us
        minimize the segment length.

        Args:
            zones: The full route as a list of visited zone names.
            required_zones: Distinct zone names that must all appear in the segment.

        Returns:
            The minimum length of a contiguous segment containing all required zones
            at least once, or -1 if no such segment exists.

        Time complexity:
            O(n), where n is len(zones), because each pointer moves across the array
            at most once.

        Space complexity:
            O(k), where k is len(required_zones), for the hash set and frequency map.
        """
        # Convert the list of required zones into a set for O(1) membership checks.
        # Why a set?
        # - We only care whether a zone is required or not.
        # - required_zones are guaranteed distinct, so a set is a natural fit.
        required_set = set(required_zones)

        # Total number of distinct required zones that must appear in the window.
        total_required = len(required_set)

        # This dictionary stores how many times each required zone appears
        # inside the current sliding window [left, right].
        #
        # Example:
        # If current window contains:
        #   "A1" twice, "Cold" once, "Fragile" once
        # then window_counts might be:
        #   {"A1": 2, "Cold": 1, "Fragile": 1}
        #
        # We only store counts for zones that are actually required.
        window_counts: Dict[str, int] = {}

        # This variable tracks how many distinct required zones are currently
        # satisfied in the window.
        #
        # A required zone is considered "formed" when its count in the window
        # becomes at least 1.
        #
        # For example, if required zones are {"A1", "Cold", "Fragile"}:
        # - formed = 0 initially
        # - after seeing "A1", formed = 1
        # - after seeing "Cold", formed = 2
        # - after seeing "Fragile", formed = 3 => window is valid
        formed = 0

        # Left boundary of the sliding window.
        left = 0

        # Best answer found so far.
        # Start with infinity so any real valid window will be smaller.
        min_length = float("inf")

        # Move the right pointer from left to right across the entire zones list.
        for right, zone in enumerate(zones):
            # Step 1: Expand the window by including zones[right].
            #
            # If this zone is not required, it does not help satisfy the condition,
            # but it is still part of the current window. We simply ignore it in
            # the frequency map because it does not affect validity.
            if zone in required_set:
                # Increase the count of this required zone in the current window.
                window_counts[zone] = window_counts.get(zone, 0) + 1

                # If this is the first occurrence of this required zone in the window,
                # then we have newly satisfied one more required zone.
                if window_counts[zone] == 1:
                    formed += 1

            # Step 2: If the current window is valid, try to shrink it from the left.
            #
            # A window is valid when it contains all required zones at least once.
            # That means formed == total_required.
            while formed == total_required and left <= right:
                # Compute current window length.
                current_length = right - left + 1

                # Update the best answer if this valid window is smaller.
                if current_length < min_length:
                    min_length = current_length

                # We now try to remove zones[left] from the window and see whether
                # the window can remain valid after shrinking.
                left_zone = zones[left]

                if left_zone in required_set:
                    # Decrease the count because this zone is leaving the window.
                    window_counts[left_zone] -= 1

                    # If the count drops to 0, then the window no longer contains
                    # this required zone, so the window becomes invalid.
                    if window_counts[left_zone] == 0:
                        formed -= 1

                # Move left boundary forward to continue shrinking.
                left += 1

        # If min_length was never updated, no valid window exists.
        if min_length == float("inf"):
            return -1

        return int(min_length)

    def minSegmentLength(self, zones: List[str], requiredZones: List[str]) -> int:
        """
        Wrapper method using a camelCase name that may match some platform expectations.

        Args:
            zones: The full route as a list of visited zone names.
            requiredZones: Distinct zone names that must all appear in the segment.

        Returns:
            The minimum valid segment length, or -1 if impossible.

        Time complexity:
            O(n), where n is len(zones).

        Space complexity:
            O(k), where k is len(requiredZones).
        """
        return self.shortest_span_covering_all_zones(zones, requiredZones)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    zones1 = ["Inbound", "A1", "Cold", "B2", "Fragile", "Cold", "A1", "Packing"]
    required1 = ["A1", "Cold", "Fragile"]
    result1 = solution.minSegmentLength(zones1, required1)
    print(result1)  # Expected: 3

    # Example 2
    zones2 = ["Dock", "A1", "B2", "Dock", "Packing"]
    required2 = ["A1", "Cold"]
    result2 = solution.minSegmentLength(zones2, required2)
    print(result2)  # Expected: -1