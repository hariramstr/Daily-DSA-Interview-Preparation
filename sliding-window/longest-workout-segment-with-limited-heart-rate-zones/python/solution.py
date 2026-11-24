"""
Title: Longest Workout Segment With Limited Heart Rate Zones

Problem Description:
You are given an array `zones` where `zones[i]` represents the heart rate zone
recorded during the `i`-th minute of a workout. A coach wants to analyze the
longest continuous workout segment that stayed focused enough to use at most `k`
distinct heart rate zones.

Return the length of the longest contiguous subarray of `zones` that contains
no more than `k` distinct values.

This problem models a realistic fitness analytics task: short fluctuations are
acceptable, but if too many different zones appear in one continuous segment,
the workout is considered inconsistent. Your goal is to find the maximum
duration of a consistent segment.

A subarray is a contiguous portion of the array.

Constraints:
- 1 <= zones.length <= 2 * 10^5
- 1 <= zones[i] <= 10^9
- 1 <= k <= zones.length

Examples:
1.
Input: zones = [2, 2, 1, 3, 2, 2, 1], k = 2
Output: 3
Explanation:
One valid longest segment is [2, 2, 1], which uses only zones {1, 2}.
Any longer contiguous segment contains at least 3 distinct zones.

2.
Input: zones = [5, 5, 5, 4, 4, 6, 6, 4], k = 2
Output: 5
Explanation:
The segment [4, 4, 6, 6, 4] contains only zones {4, 6} and has length 5,
which is the maximum possible.
"""

from typing import Dict, List


class Solution:
    def longest_workout_segment(self, zones: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing
        at most k distinct heart rate zones.

        Args:
            zones: List of integers where each value represents the heart rate
                zone recorded at a minute of the workout.
            k: Maximum number of distinct zone values allowed in a valid segment.

        Returns:
            The maximum length of a contiguous subarray with at most k distinct values.

        Time Complexity:
            O(n), where n is the length of zones.
            Each element is added to the sliding window once and removed at most once.

        Space Complexity:
            O(k) on average for the frequency map of values currently in the window.
            In the worst case, this can be O(n) if k is large.
        """
        # This dictionary will store how many times each zone appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is [2, 2, 1], then:
        # counts = {2: 2, 1: 1}
        #
        # Why use a dictionary?
        # - We need to know how many distinct values are currently in the window.
        # - We also need to remove values correctly when the left side moves.
        # - A frequency map lets us do both efficiently in O(1) average time.
        counts: Dict[int, int] = {}

        # `left` marks the beginning of the current window.
        # `right` will expand the window one step at a time.
        left: int = 0

        # This stores the best (maximum) valid window length found so far.
        max_length: int = 0

        # We expand the window by moving `right` from left to right across the array.
        for right, zone in enumerate(zones):
            # Step 1: Include the new zone at index `right` into the window.
            #
            # If the zone is already present, increase its count.
            # Otherwise, start its count at 1.
            counts[zone] = counts.get(zone, 0) + 1

            # Step 2: If the window has become invalid, shrink it from the left.
            #
            # A window is invalid when it contains more than k distinct values.
            # We keep moving `left` forward until the window becomes valid again.
            #
            # Why a while loop instead of if?
            # Because adding one new element might require removing multiple
            # elements from the left before the number of distinct values
            # drops back to at most k.
            while len(counts) > k:
                # Identify the zone that is currently at the left boundary.
                left_zone: int = zones[left]

                # Remove one occurrence of that zone from the window.
                counts[left_zone] -= 1

                # If its count becomes zero, it no longer exists in the window,
                # so we remove it from the dictionary entirely.
                #
                # This is very important because the number of distinct values
                # is exactly the number of keys in `counts`.
                if counts[left_zone] == 0:
                    del counts[left_zone]

                # Move the left boundary one step to the right,
                # effectively shrinking the window.
                left += 1

            # Step 3: At this point, the window [left, right] is guaranteed valid.
            # It contains at most k distinct values.
            #
            # Compute its length:
            current_length: int = right - left + 1

            # Step 4: Update the best answer if this valid window is longer
            # than anything we have seen before.
            if current_length > max_length:
                max_length = current_length

        # After processing all positions, max_length holds the answer.
        return max_length

    def length_of_longest_subarray_at_most_k_distinct(self, zones: List[int], k: int) -> int:
        """
        Wrapper method with a more generic name for the same algorithm.

        Args:
            zones: List of integers representing heart rate zones.
            k: Maximum number of distinct values allowed.

        Returns:
            The length of the longest contiguous subarray with at most k distinct values.

        Time Complexity:
            O(n), where n is the length of zones.

        Space Complexity:
            O(k) on average, due to the frequency dictionary.
        """
        return self.longest_workout_segment(zones, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    zones1: List[int] = [2, 2, 1, 3, 2, 2, 1]
    k1: int = 2
    result1: int = solution.longest_workout_segment(zones1, k1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2 from the problem statement
    zones2: List[int] = [5, 5, 5, 4, 4, 6, 6, 4]
    k2: int = 2
    result2: int = solution.longest_workout_segment(zones2, k2)
    print("Example 2 Result:", result2)  # Expected: 5

    # Additional small sanity checks
    zones3: List[int] = [1]
    k3: int = 1
    result3: int = solution.longest_workout_segment(zones3, k3)
    print("Additional Test 1 Result:", result3)  # Expected: 1

    zones4: List[int] = [1, 2, 1, 2, 3]
    k4: int = 2
    result4: int = solution.longest_workout_segment(zones4, k4)
    print("Additional Test 2 Result:", result4)  # Expected: 4