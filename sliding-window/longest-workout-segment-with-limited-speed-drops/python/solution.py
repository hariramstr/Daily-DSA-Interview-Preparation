"""
Title: Longest Workout Segment With Limited Speed Drops

Problem Description:
You are given an array `speed` where `speed[i]` is the runner's speed during the
`i`-th minute of a workout, and an integer `k`. A minute `i` (for `i > 0`) is
called a speed drop if `speed[i] < speed[i - 1]`.

Your task is to find the length of the longest contiguous segment of the workout
that contains at most `k` speed drops.

More formally, choose a subarray `speed[l...r]` such that within that subarray,
the number of indices `i` with `l < i <= r` and `speed[i] < speed[i - 1]` is at
most `k`. Return the maximum possible length of such a segment.

This can be solved efficiently with a sliding window:
- Expand the right end of the window one step at a time.
- Count how many speed drops are currently inside the window.
- If the count becomes greater than `k`, move the left end forward until the
  window becomes valid again.
- Track the maximum valid window length seen.

Constraints:
- 1 <= speed.length <= 2 * 10^5
- 0 <= k < speed.length
- 1 <= speed[i] <= 10^9
"""

from typing import List


class Solution:
    def longest_workout_segment(self, speed: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous segment containing at most k speed drops.

        A speed drop occurs at index i when speed[i] < speed[i - 1]. For a window
        [left, right], we count only drops whose comparison edge lies fully inside
        the window, meaning indices i such that left < i <= right.

        Args:
            speed: List of runner speeds per minute.
            k: Maximum allowed number of speed drops inside the chosen segment.

        Returns:
            The length of the longest valid contiguous segment.

        Time complexity:
            O(n), where n is the length of speed. Each pointer moves at most n times.

        Space complexity:
            O(1), because only a few variables are used.
        """
        n: int = len(speed)

        # `left` is the starting index of our current sliding window.
        # `right` will be expanded from left to right in the loop below.
        left: int = 0

        # `drops_in_window` stores how many speed drops currently exist inside
        # the active window [left, right].
        #
        # Important detail:
        # A drop is associated with an index i > 0 if speed[i] < speed[i - 1].
        # For a window [left, right], that drop counts only if i is inside the
        # window and i - 1 is also inside the window. This is equivalent to:
        # left < i <= right.
        drops_in_window: int = 0

        # This will store the best (maximum) valid window length found so far.
        best_length: int = 1

        # We iterate `right` from 0 to n - 1, growing the window one element at a time.
        for right in range(n):
            # When we add a new element at position `right`, the only *new* possible
            # drop introduced is the comparison between speed[right - 1] and speed[right].
            #
            # If right == 0, there is no previous element, so no comparison exists.
            if right > 0 and speed[right] < speed[right - 1]:
                drops_in_window += 1

            # If the window now has too many drops, we must shrink it from the left
            # until it becomes valid again.
            #
            # Why does shrinking work?
            # Because the problem asks for a contiguous segment, and sliding window
            # is ideal when we need the longest subarray satisfying a condition that
            # can be updated incrementally.
            while drops_in_window > k:
                # Before moving `left` forward, we need to check whether the edge
                # between `left` and `left + 1` contributes a drop to the current window.
                #
                # That edge is counted in the current window exactly when left + 1 <= right.
                # If speed[left + 1] < speed[left], then removing `left` from the window
                # also removes that drop from the window.
                if left + 1 <= right and speed[left + 1] < speed[left]:
                    drops_in_window -= 1

                # Now we can safely move the left boundary rightward by one.
                left += 1

            # At this point, the window [left, right] is valid:
            # it contains at most k speed drops.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer.
            if current_length > best_length:
                best_length = current_length

        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    speed1: List[int] = [5, 6, 4, 4, 7, 3, 8]
    k1: int = 1
    result1: int = solution.longest_workout_segment(speed1, k1)
    print("Example 1:")
    print(f"speed = {speed1}")
    print(f"k = {k1}")
    print(f"Longest valid segment length = {result1}")
    print("Expected = 4")
    print()

    # Example 2
    speed2: List[int] = [9, 8, 7, 10, 11, 6, 12]
    k2: int = 2
    result2: int = solution.longest_workout_segment(speed2, k2)
    print("Example 2:")
    print(f"speed = {speed2}")
    print(f"k = {k2}")
    print(f"Longest valid segment length = {result2}")
    print("Expected = 5")
    print()

    # Additional quick checks
    extra_tests: List[tuple[List[int], int]] = [
        ([1], 0),                  # Single element, no drops possible
        ([1, 2, 3, 4], 0),         # Fully non-decreasing, whole array valid
        ([4, 3, 2, 1], 0),         # Every adjacent pair is a drop, best is 1
        ([4, 3, 2, 1], 2),         # Can include at most 2 drops
        ([5, 5, 5, 5], 0),         # Equal values are not drops
        ([10, 9, 10, 9, 10], 1),   # Alternating drops
    ]

    print("Additional tests:")
    for speeds, k_value in extra_tests:
        print(
            f"speed = {speeds}, k = {k_value}, "
            f"answer = {solution.longest_workout_segment(speeds, k_value)}"
        )