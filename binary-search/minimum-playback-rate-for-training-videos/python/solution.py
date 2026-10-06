"""
Title: Minimum Playback Rate for Training Videos

Problem Description:
A company needs to finish a sequence of employee training videos within a fixed number
of hours before a compliance deadline. You are given an integer array videos where
videos[i] is the length of the i-th video in minutes, and an integer h representing
the total number of hours available.

During each hour, the player runs at a constant integer playback rate r
(in minutes of video watched per hour). If a video has length x, it takes
ceil(x / r) hours to finish because a partially watched final hour still counts
as a full hour block for scheduling purposes. Videos must be completed one by one
in the given order, but the playback rate is the same for all videos.

Return the minimum integer playback rate r such that all videos can be finished
within h hours. If it is impossible even with an arbitrarily large rate because
h is smaller than the number of videos, return -1.

Constraints:
- 1 <= videos.length <= 100000
- 1 <= videos[i] <= 1000000000
- 1 <= h <= 1000000000

Examples:
1)
Input: videos = [90, 120, 75], h = 6
Output: 60

Reason:
- At rate 45:
  ceil(90/45) + ceil(120/45) + ceil(75/45) = 2 + 3 + 2 = 7, so it does not fit.
- At rate 60:
  ceil(90/60) + ceil(120/60) + ceil(75/60) = 2 + 2 + 2 = 6, so it fits.
- At rate 59:
  ceil(90/59) + ceil(120/59) + ceil(75/59) = 2 + 3 + 2 = 7, so it does not fit.
Therefore, the minimum valid rate is 60.

2)
Input: videos = [30, 11, 23, 4, 20], h = 5
Output: 30

Reason:
There are 5 videos and only 5 hours, so each video must finish within one hour.
That means the playback rate must be at least the longest video length, which is 30.
"""

from typing import List


class Solution:
    def _required_hours(self, videos: List[int], rate: int) -> int:
        """
        Compute how many total hours are needed to finish all videos at a given rate.

        Args:
            videos: List of video lengths in minutes.
            rate: Playback rate in minutes per hour.

        Returns:
            Total number of hours required to finish all videos.

        Time complexity:
            O(n), where n is the number of videos.

        Space complexity:
            O(1), excluding input storage.
        """
        # We accumulate the total number of hour blocks needed.
        # For each video of length x, the number of hours needed is ceil(x / rate).
        #
        # Instead of using math.ceil(x / rate), we use the integer-only formula:
        #     ceil(x / rate) = (x + rate - 1) // rate
        #
        # This avoids floating-point operations and is both faster and exact.
        total_hours: int = 0

        # Process each video independently because the problem states that
        # each video's time contribution is rounded up separately.
        for length in videos:
            total_hours += (length + rate - 1) // rate

        return total_hours

    def minPlaybackRate(self, videos: List[int], h: int) -> int:
        """
        Find the minimum integer playback rate needed to finish all videos within h hours.

        This uses binary search on the answer because:
        - If a rate r is sufficient, then any rate larger than r is also sufficient.
        - If a rate r is not sufficient, then any rate smaller than r is also not sufficient.
        This monotonic behavior makes binary search the correct efficient approach.

        Args:
            videos: List of video lengths in minutes.
            h: Maximum total hours available.

        Returns:
            The minimum integer playback rate that allows all videos to finish within h hours.
            Returns -1 if it is impossible.

        Time complexity:
            O(n log m), where:
            - n is the number of videos
            - m is the maximum video length
            This is efficient for large inputs.

        Space complexity:
            O(1), excluding input storage.
        """
        # Number of videos.
        n: int = len(videos)

        # Important impossibility check:
        # Even with an extremely large playback rate, each non-empty video still takes
        # at least 1 hour because ceil(x / very_large_rate) = 1 for x > 0.
        #
        # Therefore, the absolute minimum total hours possible is exactly the number
        # of videos. If h is smaller than that, there is no solution.
        if h < n:
            return -1

        # Binary search boundaries:
        #
        # Lowest possible rate:
        # - Rate must be at least 1 because playback rate is a positive integer.
        left: int = 1

        # Highest necessary rate:
        # - max(videos) is always enough, because then every video can finish in 1 hour.
        # - Since h >= number of videos after the impossibility check, this upper bound
        #   guarantees feasibility.
        right: int = max(videos)

        # We now binary search for the smallest feasible rate.
        #
        # Invariant:
        # - The answer is always somewhere in [left, right].
        while left < right:
            # Middle candidate rate.
            # We use the standard overflow-safe pattern, although Python integers
            # do not overflow in practice.
            mid: int = left + (right - left) // 2

            # Calculate how many hours this candidate rate would require.
            needed_hours: int = self._required_hours(videos, mid)

            # If the candidate rate is fast enough, it is a valid answer.
            # But we still want the MINIMUM valid rate, so we continue searching
            # on the left half, including mid itself.
            if needed_hours <= h:
                right = mid
            else:
                # Otherwise, the candidate rate is too slow.
                # We must search strictly larger rates.
                left = mid + 1

        # When the loop ends, left == right, and that value is the smallest
        # feasible playback rate.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem description.
    videos1: List[int] = [90, 120, 75]
    h1: int = 6
    result1: int = solution.minPlaybackRate(videos1, h1)
    print("Example 1:")
    print(f"videos = {videos1}, h = {h1}")
    print(f"Minimum playback rate = {result1}")
    print("Expected = 60")
    print()

    # Sample input 2 from the problem description.
    videos2: List[int] = [30, 11, 23, 4, 20]
    h2: int = 5
    result2: int = solution.minPlaybackRate(videos2, h2)
    print("Example 2:")
    print(f"videos = {videos2}, h = {h2}")
    print(f"Minimum playback rate = {result2}")
    print("Expected = 30")
    print()

    # Additional example showing the impossible case.
    videos3: List[int] = [10, 20, 30]
    h3: int = 2
    result3: int = solution.minPlaybackRate(videos3, h3)
    print("Additional Example (Impossible Case):")
    print(f"videos = {videos3}, h = {h3}")
    print(f"Minimum playback rate = {result3}")
    print("Expected = -1")