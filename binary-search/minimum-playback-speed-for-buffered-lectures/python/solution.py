"""
Title: Minimum Playback Speed for Buffered Lectures

Problem Description:
You are given an array lectures where lectures[i] is the length in minutes of the i-th
recorded lecture segment. A student wants to finish all segments within h hours by
watching them at a constant playback speed s. If a segment has length x minutes and
the student watches at speed s, the time spent on that segment is ceil(x / s) minutes
because the video platform only allows jumping to the next segment after finishing the
current one, and each segment's required viewing time is rounded up to the next whole
minute. The same speed s must be used for every segment.

Return the minimum positive integer playback speed s such that the total time needed
to watch all lecture segments is at most h hours. If it is impossible even with
arbitrarily large speed because each non-empty segment still takes at least 1 minute,
return -1.

This problem is designed to reward identifying a monotonic condition: if a speed s is
fast enough, then any speed larger than s is also fast enough. Use this property to
search efficiently.

Constraints:
- 1 <= lectures.length <= 100000
- 1 <= lectures[i] <= 1000000000
- 1 <= h <= 1000000000
- h is given in hours, but each rounded segment time is measured in minutes, so compare
  against h * 60 total minutes
- The solution should run in O(n log M), where M is the maximum lecture length
"""

from typing import List


class Solution:
    def _can_finish(self, lectures: List[int], max_minutes: int, speed: int) -> bool:
        """
        Check whether all lecture segments can be finished within the allowed time
        using the given playback speed.

        Args:
            lectures: List of lecture segment lengths in minutes.
            max_minutes: Total allowed viewing time in minutes.
            speed: Candidate integer playback speed.

        Returns:
            True if the total rounded-up viewing time is at most max_minutes,
            otherwise False.

        Time complexity:
            O(n), where n is the number of lecture segments.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We accumulate the total number of minutes needed at the given speed.
        total_minutes_needed: int = 0

        # Process each lecture independently because the problem states that
        # rounding happens per segment, not after summing all lengths together.
        for length in lectures:
            # Compute ceil(length / speed) using integer arithmetic:
            # ceil(a / b) == (a + b - 1) // b
            #
            # We use integer math instead of floating point to avoid precision issues
            # and to keep the solution efficient and exact for very large values.
            total_minutes_needed += (length + speed - 1) // speed

            # Early exit optimization:
            # If we already exceeded the allowed time, there is no need to continue.
            # This can save time on large inputs when a speed is clearly too slow.
            if total_minutes_needed > max_minutes:
                return False

        # If we finish the loop without exceeding max_minutes, this speed works.
        return True

    def min_playback_speed(self, lectures: List[int], h: int) -> int:
        """
        Find the minimum positive integer playback speed that allows all lecture
        segments to be watched within h hours.

        Args:
            lectures: List of lecture segment lengths in minutes.
            h: Allowed time in hours.

        Returns:
            The minimum valid integer playback speed, or -1 if it is impossible.

        Time complexity:
            O(n log M), where n is the number of lecture segments and
            M is the maximum lecture length.

        Space complexity:
            O(1), ignoring input storage.
        """
        # Convert the allowed time from hours to minutes because every segment's
        # rounded viewing time is measured in minutes.
        max_minutes: int = h * 60

        # Important impossibility check:
        # Even at an arbitrarily large speed, each non-empty segment still takes
        # at least 1 minute because ceil(x / very_large_speed) = 1 for x >= 1.
        #
        # Therefore, the absolute minimum possible total time is simply the number
        # of segments. If that is already greater than the allowed minutes, then
        # no speed can ever work.
        if len(lectures) > max_minutes:
            return -1

        # Binary search boundaries:
        #
        # Lowest possible speed is 1 because speed must be a positive integer.
        left: int = 1

        # Highest necessary speed can be set to the maximum lecture length.
        # Why is this enough?
        # If speed >= max(lectures), then every segment takes exactly 1 minute,
        # which is the minimum possible per segment. Any larger speed gives the
        # same rounded result, so searching beyond this value is unnecessary.
        right: int = max(lectures)

        # We will search for the smallest speed that satisfies the condition.
        #
        # Monotonic property:
        # - If a speed s works, then any speed > s also works.
        # - If a speed s does not work, then any speed < s also does not work.
        #
        # This is exactly the pattern that makes binary search applicable.
        while left < right:
            # Choose the middle speed.
            mid: int = left + (right - left) // 2

            # Test whether this speed is sufficient.
            if self._can_finish(lectures, max_minutes, mid):
                # mid works, so the answer could be mid or something smaller.
                # Keep the left half, including mid.
                right = mid
            else:
                # mid is too slow, so all speeds <= mid are also too slow.
                # Discard them and search strictly to the right.
                left = mid + 1

        # When left == right, we have found the smallest valid speed.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # lectures = [45, 80, 30], h = 3
    # Allowed time = 180 minutes
    # At speed 1: 45 + 80 + 30 = 155 minutes, which fits
    # Minimum valid speed should be 1
    lectures1: List[int] = [45, 80, 30]
    h1: int = 3
    result1: int = solution.min_playback_speed(lectures1, h1)
    print(f"Input: lectures = {lectures1}, h = {h1}")
    print(f"Output: {result1}")
    print("Expected: 1")
    print()

    # Example 2 from the prompt:
    # lectures = [120, 95, 200], h = 4
    # Allowed time = 240 minutes
    # At speed 1: 120 + 95 + 200 = 415 minutes -> too slow
    # At speed 2: ceil(120/2) + ceil(95/2) + ceil(200/2)
    #           = 60 + 48 + 100 = 208 minutes -> valid
    # Therefore the correct minimum valid speed is 2.
    #
    # Note: The prompt's stated output says 3, but its own explanation shows 2 is valid.
    # The correct answer is 2, and this implementation returns the correct value.
    lectures2: List[int] = [120, 95, 200]
    h2: int = 4
    result2: int = solution.min_playback_speed(lectures2, h2)
    print(f"Input: lectures = {lectures2}, h = {h2}")
    print(f"Output: {result2}")
    print("Expected: 2")
    print()

    # Additional impossibility example:
    # 5 segments but only 0 hours would be impossible, but h >= 1 by constraints.
    # Here is a valid impossible case under the actual rules:
    # 100 segments, h = 1 hour => 60 minutes total
    # Since each segment needs at least 1 minute, 100 > 60 means impossible.
    lectures3: List[int] = [10] * 100
    h3: int = 1
    result3: int = solution.min_playback_speed(lectures3, h3)
    print(f"Input: lectures = [10] * 100, h = {h3}")
    print(f"Output: {result3}")
    print("Expected: -1")