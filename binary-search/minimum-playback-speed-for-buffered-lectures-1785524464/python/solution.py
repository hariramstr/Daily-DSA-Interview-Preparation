"""
Title: Minimum Playback Speed for Buffered Lectures

Problem Description:
You are given a list of lecture video lengths in minutes, where lectures must be watched
in the given order. A student has exactly H hours before an exam and wants to finish all
lectures on time. The player supports variable playback speed, but the same speed must be
used for every lecture.

If the playback speed is s, a lecture with length x minutes takes ceil(x / s) minutes to
finish, because even a partially watched final minute still consumes a full minute block
in the study planner. For example, at speed 3, a 7-minute lecture takes ceil(7 / 3) = 3
minutes.

Return the minimum positive integer playback speed s such that the total time needed to
watch all lectures is at most H * 60 minutes. If it is impossible even with arbitrarily
large integer speed under this rounding rule, return -1.

This problem is designed to test whether you can recognize a monotonic condition and
search over the answer space efficiently.

Constraints:
- 1 <= lectures.length <= 100000
- 1 <= lectures[i] <= 10^9
- 1 <= H <= 10^9
- All values are integers.

Notes:
- The student cannot split a lecture across different speeds.
- Because of the ceiling rule, each lecture requires at least 1 minute, no matter how
  large the speed is.
- Therefore, if lectures.length > H * 60, the answer is immediately -1.

Examples:
1) lectures = [30, 11, 23, 4, 20], H = 1
   Total available time = 60 minutes.
   At speed 1: 30 + 11 + 23 + 4 + 20 = 88 minutes -> too slow.
   At speed 2: 15 + 6 + 12 + 2 + 10 = 45 minutes -> fits.
   Therefore the minimum valid speed is 2.

2) lectures = [100, 200, 300], H = 0
   Total available time = 0 minutes.
   Impossible to watch any positive-length lecture, so answer is -1.
"""

from typing import List


class Solution:
    def _required_minutes(self, lectures: List[int], speed: int, limit: int) -> int:
        """
        Compute how many total minutes are needed to watch all lectures at a given speed.

        We use integer arithmetic to compute ceil(x / speed) as:
            (x + speed - 1) // speed

        Args:
            lectures: List of lecture lengths in minutes.
            speed: Candidate integer playback speed.
            limit: A cutoff value used for early stopping. If the running total exceeds
                this limit, we can stop immediately because we already know this speed
                is not feasible.

        Returns:
            Total required minutes at the given speed, or a value greater than limit if
            early stopping occurs.

        Time complexity:
            O(n) in the worst case, where n is the number of lectures.

        Space complexity:
            O(1)
        """
        total_minutes: int = 0

        # We process lectures one by one and accumulate the rounded-up time needed.
        # Early stopping is important for performance in large inputs because once the
        # total already exceeds the allowed time, there is no need to continue.
        for length in lectures:
            total_minutes += (length + speed - 1) // speed
            if total_minutes > limit:
                return total_minutes

        return total_minutes

    def min_playback_speed(self, lectures: List[int], h: int) -> int:
        """
        Return the minimum positive integer playback speed needed to finish all lectures
        within h hours, or -1 if impossible.

        The key observation is monotonicity:
        - If a speed s is fast enough, then any speed greater than s is also fast enough.
        - If a speed s is too slow, then any speed smaller than s is also too slow.

        Because of this monotonic behavior, binary search over the answer is the correct
        and efficient approach.

        Args:
            lectures: List of lecture lengths in minutes.
            h: Number of available hours.

        Returns:
            The minimum feasible integer playback speed, or -1 if impossible.

        Time complexity:
            O(n log M), where:
            - n is the number of lectures
            - M is the maximum lecture length

        Space complexity:
            O(1)
        """
        # Convert available time from hours to minutes because each lecture's rounded
        # duration is measured in minutes.
        total_available_minutes: int = h * 60

        # If there is no time at all, it is impossible unless there are no lectures.
        # The problem constraints imply there is at least one lecture, so this means -1.
        if total_available_minutes <= 0:
            return -1

        # Every lecture takes at least 1 minute due to the ceiling rule, even at an
        # arbitrarily large speed. Therefore, if the number of lectures itself exceeds
        # the total available minutes, finishing is impossible.
        if len(lectures) > total_available_minutes:
            return -1

        # Search space for the speed:
        # - Minimum possible positive integer speed is 1.
        # - Maximum needed speed is max(lectures). At that speed, every lecture takes
        #   exactly 1 minute, because ceil(length / max_length) is always 1.
        left: int = 1
        right: int = max(lectures)

        # This variable will store the best valid speed found so far.
        answer: int = right

        # Standard binary search on the answer space.
        while left <= right:
            mid: int = (left + right) // 2

            # Compute the total time needed at this candidate speed.
            needed: int = self._required_minutes(
                lectures=lectures,
                speed=mid,
                limit=total_available_minutes,
            )

            # If this speed is fast enough, it is a valid candidate.
            # We record it and try to find an even smaller valid speed.
            if needed <= total_available_minutes:
                answer = mid
                right = mid - 1
            else:
                # Otherwise, this speed is too slow, so we must search higher speeds.
                left = mid + 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # lectures = [30, 11, 23, 4, 20], H = 1 hour = 60 minutes
    # Speed 1 -> 88 minutes (too slow)
    # Speed 2 -> 45 minutes (fits)
    # Therefore expected answer is 2.
    lectures_1: List[int] = [30, 11, 23, 4, 20]
    h_1: int = 1
    result_1: int = solution.min_playback_speed(lectures_1, h_1)
    print("Example 1 result:", result_1)  # Expected: 2

    # Example 2 from the prompt.
    # lectures = [100, 200, 300], H = 0 hours = 0 minutes
    # Impossible to watch any positive-length lecture.
    lectures_2: List[int] = [100, 200, 300]
    h_2: int = 0
    result_2: int = solution.min_playback_speed(lectures_2, h_2)
    print("Example 2 result:", result_2)  # Expected: -1

    # Additional simple sanity check.
    # Three 1-minute lectures, 1 hour available = 60 minutes.
    # Speed 1 is already enough because total time is 3 minutes.
    lectures_3: List[int] = [1, 1, 1]
    h_3: int = 1
    result_3: int = solution.min_playback_speed(lectures_3, h_3)
    print("Additional test result:", result_3)  # Expected: 1