"""
Title: Longest Stable Altitude Stretch

Problem Description:
You are given an integer array altitudes where altitudes[i] represents the altitude
reading of a drone at the i-th second. A flight segment is considered stable if the
difference between the highest and lowest altitude inside that contiguous segment is
at most limit.

Your task is to return the length of the longest stable contiguous segment.

In other words, find the maximum length of a subarray altitudes[l..r] such that:
max(altitudes[l..r]) - min(altitudes[l..r]) <= limit

This models a real monitoring system where a drone is considered to be flying smoothly
only when its altitude fluctuations stay within a safe tolerance.

You should design an algorithm that works efficiently for large inputs. A brute-force
solution that checks every subarray will be too slow.

Constraints:
- 1 <= altitudes.length <= 100000
- -1000000000 <= altitudes[i] <= 1000000000
- 0 <= limit <= 1000000000

Examples:
1)
Input: altitudes = [30, 32, 31, 35, 34, 33], limit = 3
Correct Output: 3

Reason:
- [30, 32, 31] has max = 32, min = 30, difference = 2 -> valid
- [32, 31, 35] has max = 35, min = 31, difference = 4 -> invalid
- [31, 35, 34] has max = 35, min = 31, difference = 4 -> invalid
- [35, 34, 33] has max = 35, min = 33, difference = 2 -> valid
So the longest stable segment length is 3.

2)
Input: altitudes = [100, 100, 100, 100], limit = 0
Output: 4

Notes:
- A segment must be contiguous.
- Negative altitude values are allowed.
- An O(n log n) or O(n) sliding window approach is expected.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_stable_altitude_stretch(self, altitudes: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray where
        max(subarray) - min(subarray) <= limit.

        Args:
            altitudes: List of altitude readings.
            limit: Maximum allowed difference between highest and lowest altitude
                inside a stable segment.

        Returns:
            The maximum length of a stable contiguous segment.

        Time complexity:
            O(n), because each element is added to and removed from each deque
            at most once.

        Space complexity:
            O(n) in the worst case for the deques.
        """
        # We use the classic sliding window technique:
        #
        # - Expand the right end of the window one element at a time.
        # - Keep track of the current window's minimum and maximum efficiently.
        # - If the window becomes invalid (max - min > limit), move the left end
        #   forward until the window becomes valid again.
        #
        # The key challenge is getting the current min and max quickly.
        # If we recomputed min and max for every window, that would be too slow.
        #
        # To solve this efficiently, we use two monotonic deques:
        #
        # 1) max_deque:
        #    - Stores values in decreasing order.
        #    - The front always contains the maximum value in the current window.
        #
        # 2) min_deque:
        #    - Stores values in increasing order.
        #    - The front always contains the minimum value in the current window.
        #
        # Why deques?
        # - We need fast insertion/removal from both ends.
        # - We can maintain monotonic order while sliding the window.
        #
        # Important detail:
        # We store actual values rather than indices here. This works because when
        # the left side moves forward, we compare the outgoing value with the front
        # of each deque and remove it if it matches.
        left: int = 0
        best_length: int = 0

        max_deque: Deque[int] = deque()
        min_deque: Deque[int] = deque()

        # Iterate with 'right' as the expanding end of the sliding window.
        for right, value in enumerate(altitudes):
            # ---------------------------------------------------------------
            # Step 1: Insert the new value into max_deque.
            #
            # max_deque must remain in decreasing order.
            # So while the last element is smaller than the new value,
            # it can never become the maximum for any future window that
            # includes this new value, so we remove it.
            # ---------------------------------------------------------------
            while max_deque and max_deque[-1] < value:
                max_deque.pop()
            max_deque.append(value)

            # ---------------------------------------------------------------
            # Step 2: Insert the new value into min_deque.
            #
            # min_deque must remain in increasing order.
            # So while the last element is larger than the new value,
            # it can never become the minimum for any future window that
            # includes this new value, so we remove it.
            # ---------------------------------------------------------------
            while min_deque and min_deque[-1] > value:
                min_deque.pop()
            min_deque.append(value)

            # ---------------------------------------------------------------
            # Step 3: Check whether the current window is valid.
            #
            # Current maximum is at max_deque[0]
            # Current minimum is at min_deque[0]
            #
            # If max - min > limit, the window is not stable, so we must
            # shrink it from the left until it becomes valid again.
            # ---------------------------------------------------------------
            while max_deque[0] - min_deque[0] > limit:
                outgoing_value: int = altitudes[left]

                # If the outgoing value is exactly the current maximum,
                # remove it from the front of max_deque because it is no
                # longer inside the window.
                if max_deque[0] == outgoing_value:
                    max_deque.popleft()

                # If the outgoing value is exactly the current minimum,
                # remove it from the front of min_deque for the same reason.
                if min_deque[0] == outgoing_value:
                    min_deque.popleft()

                # Move the left boundary to the right, shrinking the window.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: At this point, the window [left, right] is valid.
            # Compute its length and update the best answer seen so far.
            # ---------------------------------------------------------------
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        return best_length

    def longestSubarray(self, altitudes: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            altitudes: List of altitude readings.
            limit: Maximum allowed difference between highest and lowest altitude.

        Returns:
            The maximum length of a stable contiguous segment.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.longest_stable_altitude_stretch(altitudes, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Correct answer is 3.
    altitudes_1: List[int] = [30, 32, 31, 35, 34, 33]
    limit_1: int = 3
    result_1: int = solution.longest_stable_altitude_stretch(altitudes_1, limit_1)
    print("Example 1 result:", result_1)

    # Example 2 from the prompt.
    # All values are equal, so the whole array is valid.
    altitudes_2: List[int] = [100, 100, 100, 100]
    limit_2: int = 0
    result_2: int = solution.longest_stable_altitude_stretch(altitudes_2, limit_2)
    print("Example 2 result:", result_2)

    # Additional beginner-friendly test:
    # Entire array is valid because max = 5, min = 2, difference = 3.
    altitudes_3: List[int] = [2, 3, 4, 5]
    limit_3: int = 3
    result_3: int = solution.longest_stable_altitude_stretch(altitudes_3, limit_3)
    print("Additional test result:", result_3)