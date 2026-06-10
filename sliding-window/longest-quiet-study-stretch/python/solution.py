"""
Title: Longest Quiet Study Stretch

Problem Description:
A university library records the noise level of each minute during the day as an integer
array `noise`, where `noise[i]` is the noise level at minute `i`. A student wants to find
the longest continuous time period they can study such that the total noise during that
period does not exceed a given limit `maxNoise`.

Your task is to return the length of the longest contiguous subarray whose sum is less than
or equal to `maxNoise`.

This is an interview-style sliding window problem because all noise values are non-negative.
That means when the current window becomes too noisy, you can safely move the left side of
the window forward until the total noise is within the allowed limit again.

Return `0` if no single minute can be included without exceeding the limit.

Constraints:
- 1 <= noise.length <= 100000
- 0 <= noise[i] <= 10000
- 0 <= maxNoise <= 1000000000

Example 1:
Input: noise = [2, 1, 3, 2, 1, 1], maxNoise = 5
Output: 3

Example 2:
Input: noise = [6, 2, 1], maxNoise = 5
Output: 2
"""

from typing import List


class Solution:
    def longest_quiet_stretch(self, noise: List[int], maxNoise: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is at most maxNoise.

        Args:
            noise: A list of non-negative integers representing noise level per minute.
            maxNoise: The maximum allowed total noise for a valid study stretch.

        Returns:
            The maximum length of a contiguous subarray with sum <= maxNoise.

        Time complexity:
            O(n), where n is the length of noise, because each index is moved at most once
            by the right pointer and at most once by the left pointer.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # `left` marks the beginning of the current sliding window.
        # We will expand the window by moving `right` forward one step at a time.
        left: int = 0

        # `current_sum` stores the total noise inside the current window [left, right].
        current_sum: int = 0

        # `best_length` stores the longest valid window length found so far.
        best_length: int = 0

        # Iterate through the array with `right` as the end of the current window.
        for right in range(len(noise)):
            # Add the new minute's noise to the current window sum because we are
            # extending the window to include `noise[right]`.
            current_sum += noise[right]

            # If the window becomes invalid (sum too large), we must shrink it
            # from the left until it becomes valid again.
            #
            # This works because all values are non-negative:
            # - Expanding the window can only keep the sum the same or increase it.
            # - Shrinking the window can only keep the sum the same or decrease it.
            #
            # Therefore, once the sum exceeds maxNoise, the only way to fix it is
            # to move `left` forward.
            while current_sum > maxNoise and left <= right:
                # Remove the leftmost element from the window sum because that
                # element is no longer part of the current window after shifting left.
                current_sum -= noise[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window [left, right] is guaranteed to be valid
            # (its sum is <= maxNoise), or it may be empty if left moved past right.
            #
            # The current valid window length is:
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any
            # previously seen valid window.
            if current_length > best_length:
                best_length = current_length

        # If no valid single element exists, best_length naturally remains 0.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    noise1: List[int] = [2, 1, 3, 2, 1, 1]
    max_noise1: int = 5
    result1: int = solution.longest_quiet_stretch(noise1, max_noise1)
    print(f"Example 1 result: {result1}")  # Expected: 3

    # Example 2
    noise2: List[int] = [6, 2, 1]
    max_noise2: int = 5
    result2: int = solution.longest_quiet_stretch(noise2, max_noise2)
    print(f"Example 2 result: {result2}")  # Expected: 2

    # Additional quick checks
    noise3: List[int] = [10]
    max_noise3: int = 5
    result3: int = solution.longest_quiet_stretch(noise3, max_noise3)
    print(f"Additional check 1 result: {result3}")  # Expected: 0

    noise4: List[int] = [0, 0, 0, 0]
    max_noise4: int = 0
    result4: int = solution.longest_quiet_stretch(noise4, max_noise4)
    print(f"Additional check 2 result: {result4}")  # Expected: 4)