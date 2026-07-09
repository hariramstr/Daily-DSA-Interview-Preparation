"""
Title: Count Days with a New Highest Step Total

Problem Description:
You are given an integer array `steps` where `steps[i]` represents the number of
steps a user walked on day `i`. A day is called a record day if its step count is
strictly greater than every previous day's step count. The first day is always
considered a record day because there are no earlier days to compare against.

Your task is to return the total number of record days in the array.

This problem tests your ability to scan an array while maintaining running state.
A correct solution should track the highest step count seen so far and count how
many times a new maximum appears.

Constraints:
- 1 <= steps.length <= 100000
- 0 <= steps[i] <= 1000000

Example 1:
Input: steps = [3000, 4500, 4200, 5000, 5000, 6200]
Output: 4
Explanation:
- Day 0: 3000 -> record day (first day)
- Day 1: 4500 -> greater than 3000, record day
- Day 2: 4200 -> not greater than 4500, not a record day
- Day 3: 5000 -> greater than 4500, record day
- Day 4: 5000 -> equal to current maximum 5000, not a record day
- Day 5: 6200 -> greater than 5000, record day
Total = 4

Example 2:
Input: steps = [8000, 7000, 7000, 6500]
Output: 1
Explanation:
- Day 0: 8000 -> record day
- Day 1: 7000 -> not greater than 8000
- Day 2: 7000 -> not greater than 8000
- Day 3: 6500 -> not greater than 8000
Total = 1

We solve this by scanning from left to right, remembering the largest step total
seen so far, and counting each time we encounter a value that is strictly larger.
"""

from typing import List


class Solution:
    def count_record_days(self, steps: List[int]) -> int:
        """
        Count how many days set a new highest step total.

        Args:
            steps: A list of integers where steps[i] is the number of steps
                walked on day i.

        Returns:
            The number of record days, where a record day has a step count
            strictly greater than all previous days.

        Time complexity:
            O(n), where n is the number of days, because we scan the list once.

        Space complexity:
            O(1), because we use only a small fixed amount of extra memory.
        """
        # This variable stores how many record days we have found so far.
        # We start at 0 because we have not processed any days yet.
        record_days_count: int = 0

        # This variable keeps track of the highest step total seen up to the
        # current point in the scan.
        #
        # We initialize it to -1 so that the first day's value will always be
        # greater than it, since the problem guarantees steps[i] >= 0.
        # That makes the first day automatically count as a record day, which
        # matches the problem statement.
        highest_steps_so_far: int = -1

        # We go through the array from left to right because each day's status
        # depends only on the days before it.
        #
        # Using a single pass is the most efficient approach here:
        # - We do not need nested loops.
        # - We do not need extra arrays or sets.
        # - We only need to remember the current maximum.
        for day_index, today_steps in enumerate(steps):
            # At this point:
            # - `day_index` tells us which day we are examining.
            # - `today_steps` is the number of steps for that day.
            #
            # A day is a record day only if today's steps are STRICTLY greater
            # than the best value seen earlier.
            #
            # Important detail:
            # We use `>` and not `>=` because equal values do NOT create a new
            # record. The problem explicitly says the step count must be
            # strictly greater than every previous day's step count.
            if today_steps > highest_steps_so_far:
                # Since today's value is larger than any previous value, this
                # day sets a new personal best.
                record_days_count += 1

                # We must update the running maximum so future days are compared
                # against this new best value.
                highest_steps_so_far = today_steps

            # If today's steps are not greater than the maximum seen so far,
            # then today is not a record day, and we do nothing.
            #
            # Examples:
            # - If today_steps < highest_steps_so_far, it is clearly not a record.
            # - If today_steps == highest_steps_so_far, it is still not a record
            #   because the problem requires strictly greater.

        # After scanning all days, `record_days_count` contains the total number
        # of days that set a new highest step total.
        return record_days_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    steps_1: List[int] = [3000, 4500, 4200, 5000, 5000, 6200]
    result_1: int = solution.count_record_days(steps_1)
    print(f"Input: {steps_1}")
    print(f"Output: {result_1}")
    print("Expected: 4")
    print()

    # Example 2 from the problem statement.
    steps_2: List[int] = [8000, 7000, 7000, 6500]
    result_2: int = solution.count_record_days(steps_2)
    print(f"Input: {steps_2}")
    print(f"Output: {result_2}")
    print("Expected: 1")
    print()

    # Additional beginner-friendly test cases.

    # Single day: the first day is always a record day.
    steps_3: List[int] = [0]
    result_3: int = solution.count_record_days(steps_3)
    print(f"Input: {steps_3}")
    print(f"Output: {result_3}")
    print("Expected: 1")
    print()

    # Strictly increasing values: every day is a new record.
    steps_4: List[int] = [1000, 2000, 3000, 4000]
    result_4: int = solution.count_record_days(steps_4)
    print(f"Input: {steps_4}")
    print(f"Output: {result_4}")
    print("Expected: 4")
    print()

    # All equal values: only the first day is a record.
    steps_5: List[int] = [5000, 5000, 5000, 5000]
    result_5: int = solution.count_record_days(steps_5)
    print(f"Input: {steps_5}")
    print(f"Output: {result_5}")
    print("Expected: 1")