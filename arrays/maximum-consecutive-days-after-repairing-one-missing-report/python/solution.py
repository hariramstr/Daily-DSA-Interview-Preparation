"""
Title: Maximum Consecutive Days After Repairing One Missing Report

Problem Description:
A monitoring system stores the day numbers on which a service successfully uploaded
its daily health report. The array `days` contains distinct positive integers
representing those successful upload days, but the values are not guaranteed to be
sorted. You are allowed to repair at most one missing report by choosing exactly one
day number that is not already in the array and treating it as if the report had been
uploaded successfully.

Your task is to return the maximum possible length of a consecutive streak of
successful days after performing at most one repair. A consecutive streak is a set of
day numbers with no gaps, such as [10, 11, 12, 13], whose length is 4.

For example, if the recorded days are [3, 5, 6], you may repair day 4 to obtain the
consecutive streak [3, 4, 5, 6], so the answer is 4. If the array already contains a
long consecutive block, you may choose not to repair anything.

Design an algorithm that works efficiently for large inputs.

Constraints:
- 1 <= days.length <= 2 * 10^5
- 1 <= days[i] <= 10^9
- All values in `days` are distinct
"""

from typing import List


class Solution:
    def max_consecutive_days_after_one_repair(self, days: List[int]) -> int:
        """
        Compute the maximum consecutive streak length after adding at most one missing day.

        The idea is:
        1. Sort the distinct day numbers.
        2. Compress them into consecutive segments/runs.
           Example: [4, 5, 6, 8, 10, 11] -> runs [4..6], [8..8], [10..11]
        3. The best answer can come from:
           - Keeping an existing run as-is
           - Extending one run by 1 using one repaired day at either end
           - Bridging two runs if the gap between them is exactly 2
             (meaning there is exactly one missing day between them)

        Args:
            days: Distinct positive integers representing successful upload days.

        Returns:
            The maximum possible length of a consecutive streak after at most one repair.

        Time complexity:
            O(n log n), due to sorting.

        Space complexity:
            O(n), for the sorted copy and run storage.
        """
        # If there is only one recorded day, then:
        # - We already have a streak of length 1.
        # - Since we may add at most one missing day, we can extend it to length 2
        #   by adding either the previous or next day.
        if len(days) == 1:
            return 2

        # Step 1: Sort the day numbers so consecutive values become adjacent.
        # This is the foundation of the whole solution because consecutive streaks
        # are easiest to detect in sorted order.
        sorted_days: List[int] = sorted(days)

        # Step 2: Build consecutive runs.
        #
        # A "run" is a maximal consecutive block [start..end].
        # Example:
        #   sorted_days = [1, 2, 4, 7, 8]
        # Runs become:
        #   [1..2] length 2
        #   [4..4] length 1
        #   [7..8] length 2
        #
        # We store each run as a pair (start, end).
        runs: List[List[int]] = []
        run_start: int = sorted_days[0]
        run_end: int = sorted_days[0]

        for i in range(1, len(sorted_days)):
            current_day: int = sorted_days[i]

            # If current_day continues the current run, extend the run.
            if current_day == run_end + 1:
                run_end = current_day
            else:
                # Otherwise, the current run ends here.
                runs.append([run_start, run_end])

                # Start a new run from current_day.
                run_start = current_day
                run_end = current_day

        # Do not forget to push the final run after the loop ends.
        runs.append([run_start, run_end])

        # Step 3: Evaluate the best possible answer.
        #
        # We consider three possibilities:
        #
        # A) Use an existing run without any repair.
        # B) Extend a single run by one day.
        #    Since we can add one missing day not already present, any run can be
        #    extended by 1 on the left or right, so candidate = run_length + 1.
        # C) Bridge two neighboring runs if there is exactly one missing day between them.
        #    Example: [4..6] and [8..8]
        #    Missing day is 7, so after adding 7 we get [4..8].
        #
        # Because the problem says "at most one repair", we are allowed to skip the
        # repair if it does not help, but in practice extending by 1 is always possible
        # for any finite run because day numbers are positive and unbounded above.
        best: int = 0

        # First, consider each run individually.
        for start, end in runs:
            run_length: int = end - start + 1

            # Option A: keep the run as-is.
            if run_length > best:
                best = run_length

            # Option B: extend this run by one repaired day.
            # Example:
            #   run [10..13] length 4
            #   add 9 or 14 -> length 5
            #
            # This is always valid because:
            # - adding end + 1 is always a new day (otherwise the run would already extend)
            # - or adding start - 1 is valid if positive
            # Since days are positive integers, extending to the right is always safe.
            if run_length + 1 > best:
                best = run_length + 1

        # Next, consider bridging adjacent runs.
        for i in range(len(runs) - 1):
            left_start, left_end = runs[i]
            right_start, right_end = runs[i + 1]

            # The number of missing values between the two runs is:
            #   right_start - left_end - 1
            gap_size: int = right_start - left_end - 1

            # We can bridge only if exactly one day is missing.
            # Example:
            #   left  = [1..2]
            #   right = [4..4]
            #   gap_size = 1 (missing day 3)
            #
            # Then the merged streak length is:
            #   len(left) + 1 + len(right)
            # which is also equal to right_end - left_start + 1
            if gap_size == 1:
                merged_length: int = right_end - left_start + 1
                if merged_length > best:
                    best = merged_length

        return best


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [8, 4, 5, 10, 6],   # Expected: 5 (repair 7 -> [4,5,6,7,8])
        [1, 2, 4, 7, 8],    # Expected: 4 (repair 3 -> [1,2,3,4])
        [3, 5, 6],          # Expected: 4 (repair 4 -> [3,4,5,6])
        [10],               # Expected: 2
        [1, 3],             # Expected: 3 (repair 2 -> [1,2,3])
        [100, 101, 102],    # Expected: 4
    ]

    for days in sample_inputs:
        result = solution.max_consecutive_days_after_one_repair(days)
        print(f"days = {days} -> maximum streak = {result}")