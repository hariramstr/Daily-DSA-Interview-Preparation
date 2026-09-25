"""
Title: Longest Sensor Batch With Limited Duplicate Device IDs

Problem Description:
A monitoring system receives a stream of sensor readings, where each reading is labeled
with the integer device ID that produced it. Engineers want to analyze the longest
contiguous batch of readings that is still considered "diverse enough."

A batch is valid if no single device ID appears more than k times inside that contiguous
segment.

Given an integer array deviceIds and an integer k, return the length of the longest
contiguous subarray such that every distinct device ID appears at most k times within
that subarray.

This is a contiguous-window problem: you may only choose a single continuous segment
from the input array. The goal is to maximize its length while respecting the per-device
frequency limit.

Constraints:
- 1 <= deviceIds.length <= 200000
- 1 <= deviceIds[i] <= 1000000000
- 1 <= k <= deviceIds.length
- The answer must be computed in O(n) or O(n log n) time.
"""

from collections import defaultdict
from typing import DefaultDict, List


class Solution:
    def max_subarray_length(self, deviceIds: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray in which every distinct
        device ID appears at most k times.

        Args:
            deviceIds: List of integer device IDs representing the sensor reading stream.
            k: Maximum allowed frequency for any single device ID inside the window.

        Returns:
            The maximum length of a valid contiguous subarray.

        Time Complexity:
            O(n), where n is the length of deviceIds.
            Each element is added to the sliding window once and removed at most once.

        Space Complexity:
            O(m), where m is the number of distinct device IDs currently tracked
            in the frequency map. In the worst case, this can be O(n).
        """
        # This dictionary stores how many times each device ID appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is [4, 1, 4, 2],
        # then counts would be:
        # {
        #     4: 2,
        #     1: 1,
        #     2: 1
        # }
        counts: DefaultDict[int, int] = defaultdict(int)

        # left marks the start of the current window.
        # right will expand the window one element at a time.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We iterate through the array with right as the end of the window.
        for right, device_id in enumerate(deviceIds):
            # Step 1: Include the new element at position right into the window.
            counts[device_id] += 1

            # Step 2: If adding this device caused its count to exceed k,
            # then the current window is invalid.
            #
            # Important observation:
            # Only the count of the newly added device_id could have become invalid,
            # because all other counts were already valid before this step.
            #
            # So we shrink the window from the left until this device_id
            # is back within the allowed limit.
            while counts[device_id] > k:
                left_device_id: int = deviceIds[left]
                counts[left_device_id] -= 1
                left += 1

            # Step 3: At this point, the window [left, right] is valid.
            # Every device ID appears at most k times.
            current_length: int = right - left + 1

            # Step 4: Update the best answer if this valid window is longer.
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    device_ids_1: List[int] = [4, 1, 4, 2, 4, 1, 2, 2]
    k_1: int = 2
    result_1: int = solution.max_subarray_length(device_ids_1, k_1)
    print("Example 1 Result:", result_1)  # Expected: 5

    # Example 2
    device_ids_2: List[int] = [7, 7, 3, 7, 3, 3, 8]
    k_2: int = 1
    result_2: int = solution.max_subarray_length(device_ids_2, k_2)
    print("Example 2 Result:", result_2)  # Expected: 2

    # Additional quick checks
    device_ids_3: List[int] = [1, 2, 3, 4]
    k_3: int = 1
    result_3: int = solution.max_subarray_length(device_ids_3, k_3)
    print("Additional Check 1:", result_3)  # Expected: 4

    device_ids_4: List[int] = [5, 5, 5, 5]
    k_4: int = 2
    result_4: int = solution.max_subarray_length(device_ids_4, k_4)
    print("Additional Check 2:", result_4)  # Expected: 2