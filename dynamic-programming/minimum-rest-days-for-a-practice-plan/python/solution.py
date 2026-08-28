"""
Title: Minimum Rest Days for a Practice Plan

Problem Description:
You are planning a sequence of daily activities for a student preparing for a competition.
For each day, the student may have access to coding practice, reading practice, both, or neither.
The student wants to stay productive, but cannot do the same type of practice on two consecutive
days because it becomes ineffective. On any day, the student may also choose to rest.

You are given an integer array activities where activities[i] describes what is available on day i:
- 0: neither coding nor reading is available, so the student must rest
- 1: only coding is available
- 2: only reading is available
- 3: both coding and reading are available

Return the minimum number of rest days needed over the entire schedule.

A valid plan must follow these rules:
- The student can do coding only if coding is available that day.
- The student can do reading only if reading is available that day.
- The student cannot do coding on two consecutive days.
- The student cannot do reading on two consecutive days.
- Resting is always allowed.

Constraints:
- 1 <= activities.length <= 100
- 0 <= activities[i] <= 3
"""

from typing import List


class Solution:
    def min_rest_days(self, activities: List[int]) -> int:
        """
        Compute the minimum number of rest days needed.

        We use dynamic programming where we track, for each day, the minimum rest days
        if the day ends with:
        - rest
        - coding
        - reading

        Args:
            activities: A list where each value describes which activities are available
                on that day:
                0 = none, 1 = coding, 2 = reading, 3 = both.

        Returns:
            The minimum possible number of rest days over all days.

        Time complexity:
            O(n), where n is the number of days.

        Space complexity:
            O(1), because we only keep DP values for the previous day.
        """
        # We define three states for the previous day:
        # prev_rest    -> minimum rest days up to the previous day if we RESTED that day
        # prev_coding  -> minimum rest days up to the previous day if we did CODING that day
        # prev_reading -> minimum rest days up to the previous day if we did READING that day
        #
        # Why these three states?
        # Because the only thing that matters for the next day is:
        # "What did we do yesterday?"
        # If we know yesterday's action, then we can decide whether coding/reading is allowed today.
        #
        # At the very beginning, before processing any day:
        # - We can think of the cost as 0 rest days so far.
        # - No actual previous activity exists.
        # To make transitions easy:
        #   prev_rest = 0
        #   prev_coding = 0
        #   prev_reading = 0
        #
        # This works because on day 1, if coding or reading is available, we are allowed to do it.
        prev_rest: int = 0
        prev_coding: int = 0
        prev_reading: int = 0

        # We process each day one by one.
        for day_value in activities:
            # If we choose to REST today:
            # We can always rest, regardless of what happened yesterday.
            # Resting adds exactly 1 rest day.
            current_rest: int = min(prev_rest, prev_coding, prev_reading) + 1

            # Initialize coding/reading states with a very large number.
            # This means "currently impossible" until proven otherwise.
            current_coding: int = float("inf")
            current_reading: int = float("inf")

            # If coding is available today:
            # day_value == 1 means only coding
            # day_value == 3 means both coding and reading
            #
            # We are allowed to do coding today only if yesterday was NOT coding.
            # So we can come from:
            # - prev_rest
            # - prev_reading
            #
            # We take the minimum rest days among those valid previous states.
            if day_value == 1 or day_value == 3:
                current_coding = min(prev_rest, prev_reading)

            # If reading is available today:
            # day_value == 2 means only reading
            # day_value == 3 means both coding and reading
            #
            # We are allowed to do reading today only if yesterday was NOT reading.
            # So we can come from:
            # - prev_rest
            # - prev_coding
            #
            # Again, choose the minimum rest days among valid previous states.
            if day_value == 2 or day_value == 3:
                current_reading = min(prev_rest, prev_coding)

            # Move the "current day" results into the "previous day" variables
            # so they can be used when processing the next day.
            prev_rest = current_rest
            prev_coding = current_coding
            prev_reading = current_reading

        # After processing all days, the answer is the best among all possible ways
        # to end the final day:
        # - resting
        # - coding
        # - reading
        return min(prev_rest, prev_coding, prev_reading)

    def minimum_rest_days(self, activities: List[int]) -> int:
        """
        Wrapper method that calls the main dynamic programming solution.

        Args:
            activities: A list of daily activity availability values.

        Returns:
            The minimum number of rest days needed.

        Time complexity:
            O(n), where n is the number of days.

        Space complexity:
            O(1).
        """
        return self.min_rest_days(activities)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [1, 3, 2, 0, 3],
        [3, 3, 3],
        [0],
        [1, 1, 1],
        [2, 3, 1, 2, 3, 0, 1],
    ]

    for activities in sample_inputs:
        result = solution.minimum_rest_days(activities)
        print(f"activities = {activities} -> minimum rest days = {result}")