"""
Title: Maximum Consecutive Days After Rebooking One Hotel Stay

Problem Description:
You are given a strictly increasing array bookedDays, where each value represents a
calendar day on which a traveler already has a hotel reservation. The traveler wants
the longest possible streak of consecutive booked days. To improve the itinerary,
they are allowed to rebook exactly one existing reservation to any other day number,
as long as the final set of booked days still contains no duplicates.

Return the maximum number of consecutive calendar days that can appear in the final
set after moving at most one reservation.

A rebooking means choosing one value from bookedDays, removing it, and inserting a
different positive integer day. You may also choose not to move any reservation if
the array is already optimal. The relative order of the array does not matter after
the move; only the set of booked days matters.

This is an arrays problem focused on analyzing gaps between consecutive values.
A correct solution should run efficiently for large inputs.

Constraints:
- 1 <= bookedDays.length <= 200000
- 1 <= bookedDays[i] <= 1000000000
- bookedDays is strictly increasing

Example 1:
Input: bookedDays = [2, 4, 5, 7]
Output: 4
Explanation: Rebook day 7 to day 3. The final set becomes [2, 3, 4, 5], which
contains a longest consecutive streak of length 4.

Example 2:
Input: bookedDays = [10, 11, 14, 15, 16, 20]
Output: 4
Explanation: The best move is to rebook day 20 to day 13, producing
[10, 11, 13, 14, 15, 16]. The longest consecutive streak is [13, 14, 15, 16]
with length 4. It is not possible to form a streak of length 5 with only one
rebooking.
"""

from typing import List


class Solution:
    def _build_runs(self, booked_days: List[int]) -> List[int]:
        """
        Build lengths of maximal consecutive runs.

        Args:
            booked_days: Strictly increasing list of booked calendar days.

        Returns:
            A list where each value is the length of one maximal consecutive run.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(booked_days)
        runs: List[int] = []

        # We scan the sorted unique days and group adjacent values that differ by 1.
        # Each such group is already a consecutive streak without any rebooking.
        current_len: int = 1
        for i in range(1, n):
            if booked_days[i] == booked_days[i - 1] + 1:
                current_len += 1
            else:
                runs.append(current_len)
                current_len = 1
        runs.append(current_len)

        return runs

    def max_consecutive_days_after_one_rebooking(self, booked_days: List[int]) -> int:
        """
        Compute the maximum possible length of a consecutive streak after moving
        at most one reservation to a different positive integer day.

        Core idea:
        1. Split the array into maximal consecutive runs.
        2. Without any move, the answer is the largest run length.
        3. With one move, there are two useful patterns:
           - Extend one existing run by 1, if there exists some reservation outside
             that run that can be moved into an adjacent missing day.
           - Bridge two runs separated by exactly one missing day. If we can move
             some reservation from outside those two runs into that missing day,
             the two runs merge into one larger run.

        Important subtlety:
        To fill a missing day next to / between runs, we must take a reservation from
        somewhere else. That "somewhere else" must exist after excluding the run(s)
        we are trying to improve. This is why we carefully check whether there is at
        least one reservation outside the affected run(s).

        Args:
            booked_days: Strictly increasing list of booked calendar days.

        Returns:
            Maximum achievable consecutive streak length.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(booked_days)

        # If there is only one reservation, the longest consecutive streak is 1.
        # We may move it, but we still end up with exactly one booked day.
        if n == 1:
            return 1

        # Step 1:
        # Build lengths of all maximal consecutive runs.
        #
        # Example:
        # booked_days = [2, 4, 5, 7]
        # runs are:
        #   [2]      length 1
        #   [4, 5]   length 2
        #   [7]      length 1
        runs: List[int] = self._build_runs(booked_days)

        # Baseline answer: do nothing.
        answer: int = max(runs)

        # Step 2:
        # Consider extending a single run by 1.
        #
        # Why is +1 the maximum extension for a single run with one move?
        # Because one moved reservation can create only one new day. So a run can
        # gain at most one additional consecutive day on one side.
        #
        # But we can only do this if there exists at least one reservation outside
        # the chosen run to move into the missing adjacent day.
        #
        # If a run has length L and total array size is n:
        # - If L < n, then there is at least one reservation outside this run.
        #   So we can extend it to L + 1.
        # - If L == n, the entire array is already one run. Moving one reservation
        #   cannot improve the longest streak beyond n.
        for run_len in runs:
            if run_len < n:
                answer = max(answer, run_len + 1)

        # Step 3:
        # Consider bridging two neighboring runs if there is exactly one missing day
        # between them.
        #
        # Suppose we have:
        #   ... [a, a+1, ..., b]   and   [d, d+1, ..., e] ...
        # If d == b + 2, then exactly one day (b+1) is missing.
        # By moving one reservation to day (b+1), these two runs merge into:
        #   [a, a+1, ..., b, b+1, d, ..., e]
        # with length left_len + 1 + right_len.
        #
        # However, we must source that moved reservation from outside these two runs.
        # If there is no reservation outside them, then we cannot fill the gap
        # without breaking one of the runs we are trying to merge.
        #
        # In that case, the best we can preserve is left_len + right_len:
        # we remove one endpoint from one side and place it into the gap, which
        # still creates one merged run but loses one day from the original pair.
        #
        # Example:
        # booked_days = [1, 3]
        # left_len = 1, right_len = 1, no outside reservation exists.
        # Move 1 -> 2 gives [2, 3], length 2 = left_len + right_len.
        #
        # Example:
        # booked_days = [2, 4, 5, 7]
        # runs: [2] len 1, [4,5] len 2, [7] len 1
        # gap between [2] and [4,5] is exactly one missing day 3.
        # There is an outside reservation 7, so we can move 7 -> 3 and get
        # [2,3,4,5], length 4 = 1 + 1 + 2.
        run_index: int = 0
        i: int = 0

        # We walk through the original array again, this time identifying each run's
        # start and end positions so we can inspect the gap to the next run.
        while i < n:
            run_start: int = i
            while i + 1 < n and booked_days[i + 1] == booked_days[i] + 1:
                i += 1
            run_end: int = i

            # If there is a next run, inspect the gap between current run and next run.
            if run_end + 1 < n:
                next_start: int = run_end + 1
                gap: int = booked_days[next_start] - booked_days[run_end]

                # gap == 2 means exactly one missing day between the runs.
                if gap == 2:
                    left_len: int = run_end - run_start + 1

                    # Find the end of the next run to get its length.
                    j: int = next_start
                    while j + 1 < n and booked_days[j + 1] == booked_days[j] + 1:
                        j += 1
                    right_len: int = j - next_start + 1

                    combined: int = left_len + right_len

                    # Count how many reservations lie outside these two runs.
                    # If positive, we can use one of them to fill the gap and keep
                    # both runs intact, giving combined + 1.
                    outside_count: int = n - combined

                    if outside_count > 0:
                        answer = max(answer, combined + 1)
                    else:
                        answer = max(answer, combined)

            i += 1
            run_index += 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [2, 4, 5, 7],
        [10, 11, 14, 15, 16, 20],
        [1],
        [1, 2, 3, 4],
        [1, 3],
        [1, 2, 4],
        [1, 2, 4, 6],
    ]

    for booked_days in sample_inputs:
        result: int = solution.max_consecutive_days_after_one_rebooking(booked_days)
        print(f"bookedDays = {booked_days} -> {result}")