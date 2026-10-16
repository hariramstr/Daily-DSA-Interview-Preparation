"""
Title: Count Late Arrivals After the Daily Cutoff

Problem Description:
You are given an array arrivalTimes where each element represents the arrival time
of an employee in minutes after midnight for a single day. You are also given an
integer cutoff, which represents the latest on-time arrival in minutes after midnight.
Your task is to return how many employees arrived strictly after the cutoff time.

For example, if cutoff = 540, that means employees arriving at 9:00 AM (540 minutes)
are still on time, but anyone arriving at 541 or later is late. The array is not
guaranteed to be sorted, and multiple employees may have the same arrival time.

Write a function that scans the array and counts the number of values greater than cutoff.

Constraints:
- 1 <= arrivalTimes.length <= 100000
- 0 <= arrivalTimes[i] <= 1439
- 0 <= cutoff <= 1439
- The answer fits in a 32-bit integer

Example 1:
Input: arrivalTimes = [525, 541, 540, 600, 539], cutoff = 540
Output: 2

Example 2:
Input: arrivalTimes = [480, 480, 481, 479], cutoff = 480
Output: 1
"""

from typing import List


class Solution:
    def count_late_arrivals(self, arrivalTimes: List[int], cutoff: int) -> int:
        """
        Count how many arrival times are strictly greater than the cutoff.

        Args:
            arrivalTimes: A list of employee arrival times in minutes after midnight.
            cutoff: The latest time that is still considered on time.

        Returns:
            The number of employees who arrived strictly after the cutoff.

        Time complexity:
            O(n), where n is the number of arrival times, because we scan the list once.

        Space complexity:
            O(1), because we use only a small fixed amount of extra memory.
        """
        # We keep a running total of how many employees are late.
        # This starts at 0 because before checking any arrivals, we have not found
        # any late employees yet.
        late_count: int = 0

        # We scan through every arrival time exactly once.
        # A simple linear scan is the best choice here because:
        # 1. The problem only asks us to count values greater than cutoff.
        # 2. The array is not sorted, so we cannot stop early.
        # 3. Sorting would be unnecessary extra work and would increase time complexity.
        for arrival_time in arrivalTimes:
            # The key rule in this problem is "strictly after the cutoff".
            # That means:
            # - arrival_time == cutoff  -> on time
            # - arrival_time < cutoff   -> on time
            # - arrival_time > cutoff   -> late
            #
            # So we use the '>' operator, not '>='.
            if arrival_time > cutoff:
                # If this employee arrived after the cutoff, increase the late count.
                late_count += 1

        # After checking all employees, late_count contains the final answer.
        return late_count


if __name__ == "__main__":
    # Create an instance of the Solution class so we can call the method.
    solution = Solution()

    # Example 1 from the problem statement.
    # cutoff = 540
    # arrivalTimes = [525, 541, 540, 600, 539]
    #
    # Step-by-step reasoning:
    # - 525 <= 540 -> on time
    # - 541 > 540  -> late
    # - 540 == 540 -> on time
    # - 600 > 540  -> late
    # - 539 < 540  -> on time
    #
    # Total late arrivals = 2
    arrival_times_1: List[int] = [525, 541, 540, 600, 539]
    cutoff_1: int = 540
    result_1: int = solution.count_late_arrivals(arrival_times_1, cutoff_1)
    print("Example 1 Output:", result_1)  # Expected: 2

    # Example 2 from the problem statement.
    # cutoff = 480
    # arrivalTimes = [480, 480, 481, 479]
    #
    # Step-by-step reasoning:
    # - 480 == 480 -> on time
    # - 480 == 480 -> on time
    # - 481 > 480  -> late
    # - 479 < 480  -> on time
    #
    # Total late arrivals = 1
    arrival_times_2: List[int] = [480, 480, 481, 479]
    cutoff_2: int = 480
    result_2: int = solution.count_late_arrivals(arrival_times_2, cutoff_2)
    print("Example 2 Output:", result_2)  # Expected: 1

    # Additional beginner-friendly test:
    # If everyone arrives exactly at the cutoff, nobody is late.
    arrival_times_3: List[int] = [540, 540, 540]
    cutoff_3: int = 540
    result_3: int = solution.count_late_arrivals(arrival_times_3, cutoff_3)
    print("Additional Test Output:", result_3)  # Expected: 0