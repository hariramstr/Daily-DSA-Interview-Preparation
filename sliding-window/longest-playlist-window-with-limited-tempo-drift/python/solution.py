"""
Title: Longest Playlist Window With Limited Tempo Drift
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A music app stores the tempo (in BPM) of songs in the order they were added to a playlist.
Product designers want to highlight the longest contiguous block of songs that feels
rhythmically consistent. A block is considered consistent if the difference between the
fastest and slowest song in that block is at most `limit` BPM.

Given an integer array `tempo`, where `tempo[i]` is the BPM of the `i`-th song, and an
integer `limit`, return the length of the longest contiguous subarray such that
`max(tempo[l..r]) - min(tempo[l..r]) <= limit`.

Constraints:
- 1 <= tempo.length <= 200000
- 1 <= tempo[i] <= 1000000000
- 0 <= limit <= 1000000000

Examples:
1)
Input: tempo = [98, 101, 100, 108, 107, 103], limit = 5
Output: 3

2)
Input: tempo = [120, 124, 122, 121, 119, 130, 118], limit = 4
Output: 4

Note:
Return only the maximum length, not the subarray itself.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_subarray(self, tempo: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray where the difference
        between the maximum and minimum values is at most `limit`.

        Args:
            tempo: List of song tempos in BPM.
            limit: Maximum allowed difference between the fastest and slowest
                song inside a valid window.

        Returns:
            The maximum length of a contiguous valid subarray.

        Time Complexity:
            O(n), where n is the length of `tempo`.
            Each element is added to and removed from each deque at most once.

        Space Complexity:
            O(n) in the worst case for the deques.
        """
        # We use the classic sliding window technique:
        #
        # - Expand the right side of the window one element at a time.
        # - Keep track of the minimum and maximum values inside the current window.
        # - If the window becomes invalid (max - min > limit), move the left side
        #   forward until the window becomes valid again.
        #
        # The challenge is maintaining min and max efficiently.
        # A naive approach would scan the whole window every time, which is too slow.
        #
        # To solve that efficiently, we use two monotonic deques:
        #
        # 1) min_deque:
        #    - Stores values in increasing order.
        #    - The front always holds the minimum value in the current window.
        #
        # 2) max_deque:
        #    - Stores values in decreasing order.
        #    - The front always holds the maximum value in the current window.
        #
        # Why deques work well:
        # - When adding a new value, we remove all worse candidates from the back.
        # - When moving the left boundary, if the outgoing value equals the front,
        #   we remove it from the front.
        #
        # This guarantees each value is pushed and popped at most once per deque,
        # giving us linear time overall.

        min_deque: Deque[int] = deque()
        max_deque: Deque[int] = deque()

        # `left` is the start index of the current sliding window.
        left: int = 0

        # `best` stores the maximum valid window length found so far.
        best: int = 0

        # Iterate through the array with `right` as the end index of the window.
        for right, value in enumerate(tempo):
            # ---------------------------------------------------------------
            # Step 1: Insert the new value into the min deque.
            # ---------------------------------------------------------------
            # We want min_deque to remain increasing.
            # Any value larger than the current one can never become the minimum
            # while the current value is still in the window, so we remove it.
            while min_deque and min_deque[-1] > value:
                min_deque.pop()
            min_deque.append(value)

            # ---------------------------------------------------------------
            # Step 2: Insert the new value into the max deque.
            # ---------------------------------------------------------------
            # We want max_deque to remain decreasing.
            # Any value smaller than the current one can never become the maximum
            # while the current value is still in the window, so we remove it.
            while max_deque and max_deque[-1] < value:
                max_deque.pop()
            max_deque.append(value)

            # ---------------------------------------------------------------
            # Step 3: Shrink the window from the left if it is invalid.
            # ---------------------------------------------------------------
            # The current window is invalid if:
            #     current_max - current_min > limit
            #
            # Since the front of max_deque is the maximum and the front of
            # min_deque is the minimum, we can check validity in O(1).
            while max_deque[0] - min_deque[0] > limit:
                # The value leaving the window is tempo[left].
                outgoing: int = tempo[left]

                # If the outgoing value is currently the minimum, remove it
                # from the front of min_deque.
                if min_deque and min_deque[0] == outgoing:
                    min_deque.popleft()

                # If the outgoing value is currently the maximum, remove it
                # from the front of max_deque.
                if max_deque and max_deque[0] == outgoing:
                    max_deque.popleft()

                # Move the left boundary rightward to shrink the window.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: Update the best answer.
            # ---------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed valid.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    tempo1: List[int] = [98, 101, 100, 108, 107, 103]
    limit1: int = 5
    result1: int = solution.longest_subarray(tempo1, limit1)
    print("Example 1:")
    print(f"tempo = {tempo1}")
    print(f"limit = {limit1}")
    print(f"Longest valid window length = {result1}")
    print("Expected = 3")
    print()

    # Example 2
    # Important correction:
    # The problem statement's explanation contains a contradiction and then corrects it.
    # Let's verify the actual correct answer:
    # [124, 122, 121, 119] has max = 124, min = 119, difference = 5 > 4, so invalid.
    # The longest valid windows have length 3, such as:
    # [124, 122, 121] -> diff = 3
    # [122, 121, 119] -> diff = 3
    tempo2: List[int] = [120, 124, 122, 121, 119, 130, 118]
    limit2: int = 4
    result2: int = solution.longest_subarray(tempo2, limit2)
    print("Example 2:")
    print(f"tempo = {tempo2}")
    print(f"limit = {limit2}")
    print(f"Longest valid window length = {result2}")
    print("Verified correct result = 3")
    print()

    # Additional quick sanity checks
    tempo3: List[int] = [5]
    limit3: int = 0
    result3: int = solution.longest_subarray(tempo3, limit3)
    print("Additional Test 1:")
    print(f"tempo = {tempo3}")
    print(f"limit = {limit3}")
    print(f"Longest valid window length = {result3}")
    print("Expected = 1")
    print()

    tempo4: List[int] = [8, 8, 8, 8]
    limit4: int = 0
    result4: int = solution.longest_subarray(tempo4, limit4)
    print("Additional Test 2:")
    print(f"tempo = {tempo4}")
    print(f"limit = {limit4}")
    print(f"Longest valid window length = {result4}")
    print("Expected = 4")