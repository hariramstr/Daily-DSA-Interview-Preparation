"""
Title: Longest Camera Feed With Limited Motion Zones

Problem Description:
A security team monitors a hallway using a camera that reports one motion zone ID for each second.
The array `zones` represents the detected zone at every second, where `zones[i]` is the zone that
had motion during second `i`.

To reduce operator fatigue, the team wants to review the longest continuous time interval that
contains motion from at most `k` distinct zones. If more than `k` different zone IDs appear in the
interval, the segment is considered too noisy to review efficiently.

Your task is to return the length of the longest contiguous subarray of `zones` that contains at
most `k` distinct values.

This is a continuous interval problem, so we must use a contiguous subarray, not a subsequence.
An efficient solution is expected because the input can be very large.

Constraints:
- 1 <= zones.length <= 2 * 10^5
- 1 <= zones[i] <= 10^9
- 1 <= k <= zones.length
- The solution should run in linear time or close to it.
"""

from typing import Dict, List


class Solution:
    def longest_camera_feed(self, zones: List[int], k: int) -> int:
        """
        Return the length of the longest contiguous subarray containing at most k distinct values.

        Args:
            zones: A list of integers where each value represents the motion zone ID detected
                at a given second.
            k: The maximum number of distinct zone IDs allowed inside a valid contiguous interval.

        Returns:
            The maximum length of a contiguous subarray with at most k distinct zone IDs.

        Time Complexity:
            O(n), where n is the length of zones.
            Each element is added to the sliding window once and removed at most once.

        Space Complexity:
            O(k) on average for the frequency map of values currently inside the window.
            In the worst case, it can be O(n) if k is as large as n.
        """
        # This dictionary stores how many times each zone ID appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is [4, 2, 4], then:
        # counts = {4: 2, 2: 1}
        #
        # Why use a dictionary?
        # - We need to know how many times each zone appears in the current window.
        # - When shrinking the window from the left, we decrement the count.
        # - If a count becomes 0, that zone is no longer in the window, so we remove it.
        counts: Dict[int, int] = {}

        # `left` is the left boundary of the sliding window.
        # The current window will always be zones[left:right+1].
        left: int = 0

        # `best` stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window one element at a time using `right`.
        for right, zone in enumerate(zones):
            # Step 1: Include the new zone at index `right` into the window.
            #
            # If the zone is already present, increase its frequency.
            # Otherwise, start its frequency at 1.
            counts[zone] = counts.get(zone, 0) + 1

            # Step 2: If the window has become invalid (more than k distinct zones),
            # shrink it from the left until it becomes valid again.
            #
            # Why this works:
            # - The window only becomes invalid when we add a new element on the right.
            # - To restore validity, we move `left` forward and remove elements from the window.
            # - Once the number of distinct zones is at most k again, the window is valid.
            while len(counts) > k:
                left_zone: int = zones[left]

                # Remove one occurrence of the zone at the left edge.
                counts[left_zone] -= 1

                # If its count drops to 0, it no longer exists in the current window.
                # We delete it from the dictionary so the number of distinct zones is accurate.
                if counts[left_zone] == 0:
                    del counts[left_zone]

                # Move the left boundary rightward to shrink the window.
                left += 1

            # Step 3: At this point, the window is guaranteed to be valid:
            # it contains at most k distinct zones.
            #
            # Compute its length and update the best answer if this window is longer.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        # After processing all positions, `best` is the answer.
        return best

    def length_of_longest_subarray_at_most_k_distinct(self, zones: List[int], k: int) -> int:
        """
        Wrapper method with a more generic interview-style name.

        Args:
            zones: A list of integers representing zone IDs over time.
            k: Maximum number of distinct values allowed in the subarray.

        Returns:
            The length of the longest contiguous subarray with at most k distinct values.

        Time Complexity:
            O(n), where n is the length of zones.

        Space Complexity:
            O(k) on average, or O(n) in the worst case depending on input and k.
        """
        return self.longest_camera_feed(zones, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # zones = [4, 2, 4, 3, 2, 2, 4], k = 2
    #
    # Valid windows with at most 2 distinct values include:
    # - [4, 2, 4] -> length 3
    # - [2, 2, 4] -> length 3
    #
    # No valid window of length 4 exists because every such candidate introduces 3 distinct values.
    zones1: List[int] = [4, 2, 4, 3, 2, 2, 4]
    k1: int = 2
    result1: int = solution.longest_camera_feed(zones1, k1)
    print(f"Example 1 result: {result1}")  # Expected: 3

    # Example 2
    # zones = [7, 7, 8, 9, 8, 8, 7, 7], k = 2
    #
    # Let's verify carefully:
    # - [7, 7, 8] -> 2 distinct, length 3
    # - [8, 9, 8, 8] -> 2 distinct, length 4
    # - [8, 8, 7, 7] -> 2 distinct, length 4
    #
    # Any length-5 candidate here contains 3 distinct values, so the correct answer is 4.
    zones2: List[int] = [7, 7, 8, 9, 8, 8, 7, 7]
    k2: int = 2
    result2: int = solution.longest_camera_feed(zones2, k2)
    print(f"Example 2 result: {result2}")  # Expected: 4

    # Additional quick sanity checks
    zones3: List[int] = [1]
    k3: int = 1
    result3: int = solution.longest_camera_feed(zones3, k3)
    print(f"Sanity check 1 result: {result3}")  # Expected: 1

    zones4: List[int] = [1, 2, 3, 4, 5]
    k4: int = 5
    result4: int = solution.longest_camera_feed(zones4, k4)
    print(f"Sanity check 2 result: {result4}")  # Expected: 5

    zones5: List[int] = [1, 2, 1, 2, 3]
    k5: int = 2
    result5: int = solution.longest_camera_feed(zones5, k5)
    print(f"Sanity check 3 result: {result5}")  # Expected: 4