"""
Title: Maximum Idle Gap After Removing One Meeting

Problem Description:
You are given an integer dayLength representing the total length of a workday, and
two arrays start and end of equal length n, where the i-th meeting occupies the
half-open interval [start[i], end[i]). The meetings are non-overlapping and sorted
by start time. You may cancel at most one meeting entirely. After the cancellation,
the remaining meetings keep their original times and order.

Your task is to return the maximum length of a continuous idle interval during the day.
Idle time may appear before the first meeting, between two consecutive remaining meetings,
or after the last meeting. If you choose not to remove any meeting, that is also allowed.

In other words, removing one meeting can merge the free time on its left, the meeting's
own duration, and the free time on its right into one larger idle block. Compute the
largest possible idle block obtainable.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= dayLength <= 10^9
- 0 <= start[i] < end[i] <= dayLength
- end[i] <= start[i + 1] for all valid i
- start.length == end.length
"""

from typing import List


class Solution:
    def max_idle_gap_after_removing_one_meeting(
        self,
        dayLength: int,
        start: List[int],
        end: List[int],
    ) -> int:
        """
        Compute the maximum continuous idle interval after removing at most one meeting.

        The meetings are already sorted and non-overlapping, so every possible idle block
        is determined by boundaries around meetings. If we remove meeting i, the new merged
        idle interval stretches from the end of the previous remaining meeting (or 0 if i
        was the first meeting) to the start of the next remaining meeting (or dayLength if
        i was the last meeting).

        Args:
            dayLength: Total length of the day.
            start: Start times of meetings, sorted in non-decreasing order.
            end: End times of meetings, matching the start array.

        Returns:
            The maximum possible length of a continuous idle interval.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        n: int = len(start)

        # This variable stores the best answer found so far.
        # We will compare:
        # 1. Existing idle gaps without removing any meeting
        # 2. New merged idle gaps formed by removing exactly one meeting
        best: int = 0

        # ------------------------------------------------------------
        # PART 1: Consider the schedule without removing any meeting.
        # ------------------------------------------------------------
        #
        # Even though the problem allows removing one meeting, it says "at most one",
        # which means we are also allowed to remove none.
        #
        # So we first compute the largest idle interval in the original schedule:
        # - before the first meeting: [0, start[0])
        # - between consecutive meetings: [end[i-1], start[i])
        # - after the last meeting: [end[n-1], dayLength)
        #
        # This ensures correctness in cases where removing a meeting does not help.
        best = max(best, start[0])

        for i in range(1, n):
            # Gap between meeting i-1 and meeting i.
            current_gap: int = start[i] - end[i - 1]
            if current_gap > best:
                best = current_gap

        best = max(best, dayLength - end[n - 1])

        # ------------------------------------------------------------
        # PART 2: Try removing each meeting once.
        # ------------------------------------------------------------
        #
        # If we remove meeting i entirely, then the free time on its left,
        # the duration of the meeting itself, and the free time on its right
        # all become one continuous idle block.
        #
        # The merged idle interval is:
        #
        #   [left_boundary, right_boundary)
        #
        # where:
        # - left_boundary is:
        #       0                if i == 0
        #       end[i - 1]       otherwise
        #
        # - right_boundary is:
        #       dayLength        if i == n - 1
        #       start[i + 1]     otherwise
        #
        # Therefore the merged idle length is:
        #
        #   right_boundary - left_boundary
        #
        # This formula automatically includes:
        # - left free gap
        # - removed meeting duration
        # - right free gap
        #
        # Because the meetings are sorted and non-overlapping, this merged interval
        # is always valid and continuous after removing meeting i.
        for i in range(n):
            if i == 0:
                left_boundary: int = 0
            else:
                left_boundary = end[i - 1]

            if i == n - 1:
                right_boundary: int = dayLength
            else:
                right_boundary = start[i + 1]

            merged_idle: int = right_boundary - left_boundary

            if merged_idle > best:
                best = merged_idle

        return best

    def maximumIdleGapAfterRemovingOneMeeting(
        self,
        dayLength: int,
        start: List[int],
        end: List[int],
    ) -> int:
        """
        Wrapper method using a camelCase name for convenience.

        Args:
            dayLength: Total length of the day.
            start: Start times of meetings.
            end: End times of meetings.

        Returns:
            The maximum possible continuous idle interval.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.max_idle_gap_after_removing_one_meeting(dayLength, start, end)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # dayLength = 20
    # meetings: [2,4), [6,9), [12,15)
    #
    # Existing gaps:
    # [0,2)   -> 2
    # [4,6)   -> 2
    # [9,12)  -> 3
    # [15,20) -> 5
    #
    # Remove [12,15):
    # merged free interval becomes [9,20) -> 11
    #
    # So the correct answer is 11.
    day_length_1 = 20
    start_1 = [2, 6, 12]
    end_1 = [4, 9, 15]
    result_1 = solution.max_idle_gap_after_removing_one_meeting(day_length_1, start_1, end_1)
    print(result_1)  # Expected: 11

    # Example 2
    # dayLength = 10
    # meetings: [1,2), [3,5), [7,8)
    #
    # Existing gaps:
    # [0,1)  -> 1
    # [2,3)  -> 1
    # [5,7)  -> 2
    # [8,10) -> 2
    #
    # Remove [3,5):
    # merged free interval becomes [2,7) -> 5
    #
    # So the correct answer is 5.
    day_length_2 = 10
    start_2 = [1, 3, 7]
    end_2 = [2, 5, 8]
    result_2 = solution.max_idle_gap_after_removing_one_meeting(day_length_2, start_2, end_2)
    print(result_2)  # Expected: 5