"""
Title: Count Runners Who Improved Their Best Lap

Problem Description:
You are given an integer array laps where laps[i] represents the lap time recorded by a
runner on day i. A smaller lap time is better. A runner is said to have improved their
best lap on day i if laps[i] is strictly smaller than every lap time that appeared before it.
The first day does not count as an improvement, because there is no earlier lap to compare against.

Return the number of days on which the runner improved their best lap.

This problem is about scanning an array from left to right while tracking the smallest
value seen so far. Each time you encounter a new value that is smaller than that running
minimum, it counts as a new improvement. Equal values do not count, because the lap must
be strictly better than all previous laps.

Constraints:
- 1 <= laps.length <= 100000
- 1 <= laps[i] <= 1000000000

Example 1:
Input: laps = [72, 70, 71, 69, 69, 68]
Output: 3
Explanation: Improvements happen on day 1 with 70, day 3 with 69, and day 5 with 68.
The first value 72 is the initial best but does not count.

Example 2:
Input: laps = [55, 55, 55, 54, 53]
Output: 2
Explanation: Day 3 with 54 improves over all previous values, and day 4 with 53 improves again.
The repeated 55 values do not count as improvements.
"""

from typing import List


class Solution:
    def count_improvements(self, laps: List[int]) -> int:
        """
        Count how many days the runner recorded a lap time that is strictly better
        than every lap time seen on earlier days.

        Args:
            laps: A list of integers where laps[i] is the lap time on day i.
                  Smaller values are better.

        Returns:
            The number of days on which the runner improved their best lap.
            The first day never counts as an improvement.

        Time complexity:
            O(n), where n is the number of lap times, because we scan the list once.

        Space complexity:
            O(1), because we only use a few variables regardless of input size.
        """
        # If there is only one lap, there cannot be any improvement day because
        # the first day does not count. The constraints guarantee at least one
        # element exists, but handling this case explicitly makes the logic easy
        # to understand for beginners.
        if len(laps) <= 1:
            return 0

        # This variable stores the smallest lap time seen so far while scanning
        # from left to right.
        #
        # Why start with laps[0]?
        # - The first lap is the only value we have seen at the beginning.
        # - It becomes the initial "best lap so far".
        # - However, it does NOT count as an improvement because there is no
        #   earlier day to compare it against.
        best_so_far: int = laps[0]

        # This variable counts how many times we find a new lap that is strictly
        # smaller than all previous laps.
        improvement_count: int = 0

        # We begin from index 1 because index 0 is the first day and cannot count
        # as an improvement by definition.
        for i in range(1, len(laps)):
            # Read the current day's lap time.
            current_lap: int = laps[i]

            # Check whether today's lap is strictly better than the best lap
            # recorded on all previous days.
            #
            # We use "<" and not "<=" because equal lap times do not count as
            # improvements. The problem requires the new lap to be strictly smaller.
            if current_lap < best_so_far:
                # We found a new record (a new smallest lap time so far),
                # so this day counts as an improvement.
                improvement_count += 1

                # Since this lap is now the best one seen so far, update the
                # running minimum. Future days must beat this new value to count.
                best_so_far = current_lap

            # If current_lap is equal to or greater than best_so_far, nothing changes:
            # - It is not an improvement.
            # - best_so_far remains the same.
            #
            # We do not need an explicit "else" block because doing nothing is enough.

        # After scanning all days, return the total number of improvements found.
        return improvement_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    # Trace:
    # Day 0: 72 -> initial best, does not count
    # Day 1: 70 -> smaller than 72, count = 1
    # Day 2: 71 -> not smaller than 70, count = 1
    # Day 3: 69 -> smaller than 70, count = 2
    # Day 4: 69 -> equal to 69, does not count, count = 2
    # Day 5: 68 -> smaller than 69, count = 3
    # Final answer: 3
    laps1: List[int] = [72, 70, 71, 69, 69, 68]
    result1: int = solution.count_improvements(laps1)
    print(f"Input: {laps1}")
    print(f"Output: {result1}")
    print("Expected: 3")
    print()

    # Example 2 from the problem statement.
    # Trace:
    # Day 0: 55 -> initial best, does not count
    # Day 1: 55 -> equal to best, does not count
    # Day 2: 55 -> equal to best, does not count
    # Day 3: 54 -> smaller than 55, count = 1
    # Day 4: 53 -> smaller than 54, count = 2
    # Final answer: 2
    laps2: List[int] = [55, 55, 55, 54, 53]
    result2: int = solution.count_improvements(laps2)
    print(f"Input: {laps2}")
    print(f"Output: {result2}")
    print("Expected: 2")
    print()

    # Additional beginner-friendly test cases.

    # Only one day: no improvement can happen.
    laps3: List[int] = [100]
    result3: int = solution.count_improvements(laps3)
    print(f"Input: {laps3}")
    print(f"Output: {result3}")
    print("Expected: 0")
    print()

    # Strictly decreasing laps: every day after the first is an improvement.
    laps4: List[int] = [10, 9, 8, 7]
    result4: int = solution.count_improvements(laps4)
    print(f"Input: {laps4}")
    print(f"Output: {result4}")
    print("Expected: 3")
    print()

    # Strictly increasing laps: no day after the first is better.
    laps5: List[int] = [5, 6, 7, 8]
    result5: int = solution.count_improvements(laps5)
    print(f"Input: {laps5}")
    print(f"Output: {result5}")
    print("Expected: 0")