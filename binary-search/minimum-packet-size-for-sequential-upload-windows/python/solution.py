"""
Title: Minimum Packet Size for Sequential Upload Windows

Problem Description:
A media company needs to upload a sequence of video clips in the given order. The i-th clip
has size clips[i] megabytes. The uploader sends data in fixed-size packets, and every packet
can carry at most P megabytes. A single clip may be split across multiple packets, but packets
cannot mix data from different upload windows.

You are also given an integer w, the maximum number of upload windows available. In one upload
window, the company uploads a contiguous group of clips in order, and the total size of all
clips assigned to that window must be fully transmitted using packets of size P. If the total
size of a window is S, then that window consumes ceil(S / P) packets. However, each upload
window is allowed to use at most one packet. That means the total size of clips placed in any
single window must be at most P.

Your task is to find the minimum integer packet size P such that all clips can be partitioned
into at most w contiguous upload windows, with each window having total size at most P.

In other words, choose the smallest possible P so the array can be split into at most w
contiguous parts, and the sum of each part does not exceed P.

Return that minimum packet size.

Constraints:
- 1 <= clips.length <= 100000
- 1 <= clips[i] <= 1000000000
- 1 <= w <= clips.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: clips = [8, 3, 5, 7, 2], w = 3
Output: 10

Example 2:
Input: clips = [4, 4, 4, 4], w = 2
Output: 8
"""

from typing import List


class Solution:
    def can_partition(self, clips: List[int], w: int, max_window_sum: int) -> bool:
        """
        Check whether the clips can be split into at most w contiguous windows such that
        each window sum is at most max_window_sum.

        Args:
            clips: List of clip sizes.
            w: Maximum number of allowed contiguous windows.
            max_window_sum: Candidate maximum allowed sum for each window.

        Returns:
            True if such a partition is possible, otherwise False.

        Time complexity:
            O(n), where n is the number of clips.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We greedily build each window from left to right.
        #
        # Why greedy works:
        # If we are trying to minimize the number of windows needed for a fixed limit
        # "max_window_sum", then the best strategy is to pack as many consecutive clips
        # as possible into the current window before starting a new one.
        #
        # This gives the minimum number of windows for that limit.
        # Therefore:
        # - if even this greedy strategy needs more than w windows, then the limit fails
        # - if greedy uses at most w windows, then the limit works

        windows_used: int = 1
        current_sum: int = 0

        for clip_size in clips:
            # If a single clip is larger than the allowed window sum,
            # then no partition is possible at all for this candidate.
            if clip_size > max_window_sum:
                return False

            # If adding this clip would exceed the allowed sum,
            # we must start a new window.
            if current_sum + clip_size > max_window_sum:
                windows_used += 1
                current_sum = clip_size

                # Early stopping:
                # As soon as we exceed the allowed number of windows,
                # we already know this candidate packet size is too small.
                if windows_used > w:
                    return False
            else:
                # Otherwise, safely add the clip to the current window.
                current_sum += clip_size

        # If we finished scanning all clips without exceeding w windows,
        # then this candidate is feasible.
        return True

    def minimum_packet_size(self, clips: List[int], w: int) -> int:
        """
        Find the minimum packet size P such that clips can be partitioned into at most w
        contiguous windows, each having total size at most P.

        Args:
            clips: List of clip sizes.
            w: Maximum number of allowed contiguous windows.

        Returns:
            The minimum integer packet size P.

        Time complexity:
            O(n log S), where n is the number of clips and S is the sum of all clip sizes.

        Space complexity:
            O(1), excluding input storage.
        """
        # This is a classic "binary search on answer" problem.
        #
        # We are not searching inside the array itself.
        # Instead, we search over the possible values of the answer P.
        #
        # Key observation:
        # If a packet size P works, then any larger packet size also works.
        # This monotonic property makes binary search possible.
        #
        # Example:
        # - If P = 10 is enough, then P = 11, 12, 100, ... are also enough.
        # - If P = 9 is not enough, then any smaller value also cannot work.
        #
        # So the search space is:
        #   lower bound = max(clips)
        #   upper bound = sum(clips)
        #
        # Why these bounds?
        # - Lower bound:
        #   Every clip must belong to some window, and no window may exceed P.
        #   Therefore P must be at least the largest single clip.
        #
        # - Upper bound:
        #   In the worst case, we put all clips into one single window.
        #   That requires P = sum(clips). This is always feasible when w >= 1.

        left: int = max(clips)
        right: int = sum(clips)

        # Standard binary search to find the smallest feasible value.
        while left < right:
            mid: int = left + (right - left) // 2

            # Check whether this candidate maximum window sum is feasible.
            if self.can_partition(clips, w, mid):
                # If it works, try to find an even smaller valid answer.
                right = mid
            else:
                # If it does not work, we must increase the candidate.
                left = mid + 1

        # At the end, left == right and points to the minimum feasible packet size.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    clips1: List[int] = [8, 3, 5, 7, 2]
    w1: int = 3
    result1: int = solution.minimum_packet_size(clips1, w1)
    print("Example 1 Result:", result1)  # Expected: 10

    # Example 2
    clips2: List[int] = [4, 4, 4, 4]
    w2: int = 2
    result2: int = solution.minimum_packet_size(clips2, w2)
    print("Example 2 Result:", result2)  # Expected: 8

    # Additional quick checks
    clips3: List[int] = [1, 2, 3, 4, 5]
    w3: int = 2
    result3: int = solution.minimum_packet_size(clips3, w3)
    print("Additional Example 3 Result:", result3)  # Expected: 9

    clips4: List[int] = [10]
    w4: int = 1
    result4: int = solution.minimum_packet_size(clips4, w4)
    print("Additional Example 4 Result:", result4)  # Expected: 10