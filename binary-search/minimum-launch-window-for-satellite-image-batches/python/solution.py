"""
Title: Minimum Launch Window for Satellite Image Batches

Problem Description:
A space imaging company must upload satellite photos to a ground station. The photos
must be transmitted in the given order, and each photo batch has a size stored in
the array batches, where batches[i] is the number of megabytes in the i-th batch.

The company has exactly d launch windows left before weather conditions become too
unstable. In one launch window, the ground station can transmit any consecutive
sequence of batches, as long as the total size sent in that window does not exceed
the chosen window capacity.

Your task is to find the minimum integer launch window capacity needed so that all
batches can be transmitted within at most d launch windows.

Every batch must be sent completely within a single window. Batches cannot be split
across windows, and the order of batches cannot be changed.

Return the smallest possible capacity that makes the schedule feasible.

This problem is designed to be solved efficiently using binary search on the answer.
A candidate capacity can be checked greedily by simulating how many launch windows
are required if each window can carry at most that much data.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- 1 <= d <= batches.length
- The answer fits in a 64-bit signed integer

Example 1:
Input: batches = [12, 7, 15, 6, 9], d = 3
Output: 21
Explanation: With capacity 21, one valid schedule is [12, 7], [15, 6], [9].
Capacity 20 is not enough because it would require 4 windows.

Example 2:
Input: batches = [5, 5, 5, 5, 5, 5], d = 2
Output: 15
Explanation: A capacity of 15 allows [5, 5, 5] and [5, 5, 5].
Any smaller capacity would need more than 2 windows.
"""

from typing import List


class Solution:
    def _required_windows(self, batches: List[int], capacity: int) -> int:
        """
        Compute how many launch windows are needed if each window has a fixed capacity.

        We process the batches from left to right because the order is fixed.
        For each batch:
        - If it fits in the current window, we add it there.
        - Otherwise, we start a new window and place the batch there.

        This greedy strategy is correct because for a fixed capacity, packing as many
        consecutive batches as possible into the current window minimizes the number
        of windows used.

        Args:
            batches: List of batch sizes that must be transmitted in order.
            capacity: Candidate launch window capacity being tested.

        Returns:
            The number of launch windows required to send all batches.

        Time complexity:
            O(n), where n is the number of batches.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We need at least one window if there is at least one batch.
        windows_used: int = 1

        # This variable stores the total size currently loaded into the active window.
        current_load: int = 0

        # Process every batch in the given order.
        for batch_size in batches:
            # If adding this batch would exceed the allowed capacity,
            # we cannot place it in the current window.
            # So we must open a new window and start that window with this batch.
            if current_load + batch_size > capacity:
                windows_used += 1
                current_load = batch_size
            else:
                # Otherwise, the batch fits in the current window,
                # so we simply add it to the running total.
                current_load += batch_size

        return windows_used

    def min_launch_window_capacity(self, batches: List[int], d: int) -> int:
        """
        Find the minimum launch window capacity needed to transmit all batches
        within at most d launch windows.

        The key idea is binary search on the answer:
        - If a capacity works, then any larger capacity also works.
        - If a capacity does not work, then any smaller capacity also does not work.

        This monotonic behavior makes binary search the ideal approach.

        Search range:
        - Lower bound = max(batches), because every single batch must fit by itself.
        - Upper bound = sum(batches), because one window could carry everything.

        Args:
            batches: List of batch sizes that must be transmitted in order.
            d: Maximum number of launch windows allowed.

        Returns:
            The smallest integer capacity that allows all batches to be sent
            within at most d launch windows.

        Time complexity:
            O(n * log(sum(batches) - max(batches) + 1)),
            where n is the number of batches.

        Space complexity:
            O(1), excluding input storage.
        """
        # The minimum possible capacity cannot be smaller than the largest batch,
        # because batches cannot be split across windows.
        left: int = max(batches)

        # The maximum possible capacity is the sum of all batches,
        # which corresponds to sending everything in one window.
        right: int = sum(batches)

        # We now perform a standard binary search over the capacity range.
        # Our goal is to find the smallest feasible capacity.
        while left < right:
            # Midpoint capacity to test.
            mid: int = left + (right - left) // 2

            # Determine how many windows are needed if capacity = mid.
            needed_windows: int = self._required_windows(batches, mid)

            # If we can finish within d windows, then this capacity is feasible.
            # That means the answer could be mid or something even smaller,
            # so we move the right boundary down to mid.
            if needed_windows <= d:
                right = mid
            else:
                # Otherwise, mid is too small and cannot work.
                # So we must search strictly larger capacities.
                left = mid + 1

        # When the loop ends, left == right and points to the minimum feasible capacity.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    batches1: List[int] = [12, 7, 15, 6, 9]
    d1: int = 3
    result1: int = solution.min_launch_window_capacity(batches1, d1)
    print("Example 1:")
    print(f"batches = {batches1}, d = {d1}")
    print(f"Minimum launch window capacity = {result1}")
    print("Expected = 21")
    print()

    # Example 2
    batches2: List[int] = [5, 5, 5, 5, 5, 5]
    d2: int = 2
    result2: int = solution.min_launch_window_capacity(batches2, d2)
    print("Example 2:")
    print(f"batches = {batches2}, d = {d2}")
    print(f"Minimum launch window capacity = {result2}")
    print("Expected = 15")
    print()

    # Additional quick sanity checks
    batches3: List[int] = [10]
    d3: int = 1
    result3: int = solution.min_launch_window_capacity(batches3, d3)
    print("Additional Test 1:")
    print(f"batches = {batches3}, d = {d3}")
    print(f"Minimum launch window capacity = {result3}")
    print("Expected = 10")
    print()

    batches4: List[int] = [3, 2, 2, 4, 1, 4]
    d4: int = 3
    result4: int = solution.min_launch_window_capacity(batches4, d4)
    print("Additional Test 2:")
    print(f"batches = {batches4}, d = {d4}")
    print(f"Minimum launch window capacity = {result4}")
    print("Expected = 6")