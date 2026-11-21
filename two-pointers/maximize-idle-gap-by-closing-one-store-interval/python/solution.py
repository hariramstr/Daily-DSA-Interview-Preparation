"""
Title: Maximize Idle Gap by Closing One Store Interval

Problem Description:
A retail analytics team tracks the minutes during a day when a store is open.
The schedule is represented by a sorted list of non-overlapping open intervals,
where each interval is inclusive of its start minute and exclusive of its end
minute: [start, end).

Management is allowed to permanently cancel at most one open interval for
maintenance. After removing zero or one interval, they want to know the largest
continuous idle gap anywhere between the remaining open intervals, including the
gap before the first interval and after the last interval within the business day.

You are given an integer dayLength representing the total number of minutes in
the day, and a 2D integer array intervals where intervals[i] = [start_i, end_i].
The intervals are sorted by start_i, non-overlapping, and all satisfy
0 <= start_i < end_i <= dayLength.

Return the maximum possible idle gap after removing at most one interval.

A gap is any continuous range of minutes with no open interval covering it.
If you remove an interval in the middle, the two neighboring gaps merge together
with the removed interval's length, forming one larger gap.

Constraints:
- 1 <= intervals.length <= 200000
- 1 <= dayLength <= 1000000000
- 0 <= start_i < end_i <= dayLength
- intervals is sorted by start_i
- intervals are pairwise non-overlapping

Examples:
1)
dayLength = 20
intervals = [[2,5],[7,9],[12,15]]
Output: 7

2)
dayLength = 30
intervals = [[0,4],[6,10],[13,18],[22,30]]
Output: 12
"""

from typing import List


class Solution:
    def max_idle_gap(self, dayLength: int, intervals: List[List[int]]) -> int:
        """
        Compute the maximum possible continuous idle gap after removing at most
        one interval.

        The key observation is:
        - If we remove no interval, the answer is simply the largest existing gap.
        - If we remove interval i, then:
            merged_gap = gap_before_i + length_of_interval_i + gap_after_i
          because the empty space before the interval, the interval itself
          (now removed), and the empty space after it all become one continuous
          idle segment.

        Args:
            dayLength: Total length of the day in minutes.
            intervals: Sorted, non-overlapping open intervals [start, end).

        Returns:
            The maximum idle gap achievable after removing at most one interval.

        Time complexity:
            O(n), where n is the number of intervals.

        Space complexity:
            O(n), for storing the gap array.
        """
        n: int = len(intervals)

        # We build an array of gaps of size n + 1.
        #
        # Why n + 1 gaps?
        # If there are n intervals, then there are exactly:
        #   - 1 gap before the first interval
        #   - n - 1 gaps between consecutive intervals
        #   - 1 gap after the last interval
        #
        # So total gaps = n + 1.
        #
        # Example:
        # intervals = [I0, I1, I2]
        # gaps = [before I0, between I0/I1, between I1/I2, after I2]
        gaps: List[int] = [0] * (n + 1)

        # Gap before the first interval: from minute 0 up to intervals[0][0].
        gaps[0] = intervals[0][0]

        # Fill all internal gaps between consecutive intervals.
        #
        # Because intervals are sorted and non-overlapping, the gap between
        # intervals[i - 1] and intervals[i] is:
        #   intervals[i][0] - intervals[i - 1][1]
        #
        # This value is guaranteed to be >= 0.
        for i in range(1, n):
            gaps[i] = intervals[i][0] - intervals[i - 1][1]

        # Gap after the last interval: from intervals[-1][1] up to dayLength.
        gaps[n] = dayLength - intervals[-1][1]

        # Start with the best answer when removing nothing.
        #
        # This is important because the problem says "remove at most one interval",
        # so keeping all intervals is allowed.
        answer: int = max(gaps)

        # Now consider removing each interval exactly once.
        #
        # For interval i:
        #   left gap  = gaps[i]
        #   right gap = gaps[i + 1]
        #   interval length = intervals[i][1] - intervals[i][0]
        #
        # After removing interval i, these three adjacent pieces become one
        # continuous idle gap:
        #   gaps[i] + interval_length + gaps[i + 1]
        #
        # We compute this for every interval and keep the maximum.
        for i in range(n):
            start_i: int = intervals[i][0]
            end_i: int = intervals[i][1]
            interval_length: int = end_i - start_i

            merged_gap: int = gaps[i] + interval_length + gaps[i + 1]

            if merged_gap > answer:
                answer = merged_gap

        return answer

    def solve(self, dayLength: int, intervals: List[List[int]]) -> int:
        """
        Wrapper method that calls the main algorithm.

        Args:
            dayLength: Total length of the day in minutes.
            intervals: Sorted, non-overlapping open intervals [start, end).

        Returns:
            The maximum idle gap achievable after removing at most one interval.

        Time complexity:
            O(n), where n is the number of intervals.

        Space complexity:
            O(n), due to the gap array used by the main method.
        """
        return self.max_idle_gap(dayLength, intervals)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    dayLength1: int = 20
    intervals1: List[List[int]] = [[2, 5], [7, 9], [12, 15]]
    result1: int = solution.solve(dayLength1, intervals1)
    print("Example 1 Result:", result1)  # Expected: 7

    # Example 2
    dayLength2: int = 30
    intervals2: List[List[int]] = [[0, 4], [6, 10], [13, 18], [22, 30]]
    result2: int = solution.solve(dayLength2, intervals2)
    print("Example 2 Result:", result2)  # Expected: 12

    # Additional quick checks
    dayLength3: int = 10
    intervals3: List[List[int]] = [[2, 8]]
    result3: int = solution.solve(dayLength3, intervals3)
    print("Single Interval Result:", result3)  # Removing it gives 10

    dayLength4: int = 15
    intervals4: List[List[int]] = [[0, 3], [3, 5], [10, 15]]
    result4: int = solution.solve(dayLength4, intervals4)
    print("Touching Intervals Result:", result4)  # Best should be 10 by removing [3,5]