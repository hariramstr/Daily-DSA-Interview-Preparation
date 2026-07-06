"""
Title: Maximum Consecutive Days After Moving One Holiday

Problem Description:
A company tracks employee availability over a planning horizon of n days, numbered
from 1 to n. Some days are already marked as holidays. You are given a strictly
increasing integer array holidays, where each value represents a holiday day.
Employees want the longest possible uninterrupted block of working days, where a
working day is any day that is not a holiday.

You are allowed to move at most one existing holiday to another day that is
currently a working day. After the move, the total number of holidays must remain
the same, and no two holidays may occupy the same day. Your task is to return the
maximum possible length of a consecutive block of working days after performing at
most one such move.

Moving a holiday means choosing one day from holidays, removing it from its current
position, and placing it on a different day between 1 and n that is not already a
holiday. You may also choose not to move any holiday.

Constraints:
- 1 <= n <= 10^9
- 1 <= holidays.length <= min(n, 2 * 10^5)
- 1 <= holidays[i] <= n
- holidays is strictly increasing

Examples:
1) n = 10, holidays = [3, 8]
   Output: 7

2) n = 15, holidays = [4, 8, 12]
   Output: 7
"""

from typing import List


class Solution:
    def max_consecutive_working_days(self, n: int, holidays: List[int]) -> int:
        """
        Compute the maximum possible length of a consecutive block of working days
        after moving at most one holiday to another currently working day.

        Args:
            n: Total number of days, numbered from 1 to n.
            holidays: Strictly increasing list of holiday days.

        Returns:
            The maximum possible length of a consecutive working-day block.

        Time complexity:
            O(m), where m = len(holidays)

        Space complexity:
            O(m), for storing gap lengths
        """
        # Number of holidays.
        m: int = len(holidays)

        # ---------------------------------------------------------------------
        # STEP 1: Build the lengths of all working-day gaps around the holidays.
        #
        # If holidays are h[0], h[1], ..., h[m-1], then the working-day blocks are:
        # - Before the first holiday: days 1 .. h[0]-1
        # - Between consecutive holidays: h[i-1]+1 .. h[i]-1
        # - After the last holiday: h[m-1]+1 .. n
        #
        # We store these block lengths in an array "gaps" of size m + 1:
        #   gaps[0]     = holidays[0] - 1
        #   gaps[i]     = holidays[i] - holidays[i-1] - 1   for 1 <= i <= m-1
        #   gaps[m]     = n - holidays[m-1]
        #
        # Why this is useful:
        # Every holiday sits between two neighboring working-day gaps:
        # holiday i is between gaps[i] and gaps[i+1].
        # If we remove holiday i from its current position, those two gaps merge,
        # and the holiday day itself also becomes a working day, so the merged
        # block length becomes:
        #
        #   gaps[i] + 1 + gaps[i+1]
        #
        # However, because we must place that holiday somewhere else, we need to
        # know whether we can place it OUTSIDE this merged block. If yes, the full
        # merged length is preserved. If not, we are forced to place it inside the
        # merged block, which reduces the best possible block by 1.
        # ---------------------------------------------------------------------
        gaps: List[int] = [0] * (m + 1)
        gaps[0] = holidays[0] - 1

        for i in range(1, m):
            gaps[i] = holidays[i] - holidays[i - 1] - 1

        gaps[m] = n - holidays[m - 1]

        # ---------------------------------------------------------------------
        # STEP 2: Precompute prefix and suffix maximums over the gaps array.
        #
        # We need to answer this question efficiently for each holiday i:
        #
        # "Is there any working day outside the two adjacent gaps gaps[i] and
        #  gaps[i+1]?"
        #
        # Equivalently:
        # "Is there any gap other than i and i+1 whose length is > 0?"
        #
        # If yes, then after removing holiday i, we can place that holiday into
        # some working day outside the merged block, so the merged block remains:
        #
        #   gaps[i] + 1 + gaps[i+1]
        #
        # If no, then every working day belongs to the merged block itself, so the
        # moved holiday must be placed somewhere inside that merged block, breaking
        # it once. Therefore the best block becomes:
        #
        #   gaps[i] + gaps[i+1]
        #
        # To check "maximum gap outside indices i and i+1" in O(1), we build:
        # - prefix_max[k] = max(gaps[0..k])
        # - suffix_max[k] = max(gaps[k..m])
        # ---------------------------------------------------------------------
        prefix_max: List[int] = [0] * (m + 1)
        suffix_max: List[int] = [0] * (m + 1)

        prefix_max[0] = gaps[0]
        for i in range(1, m + 1):
            prefix_max[i] = max(prefix_max[i - 1], gaps[i])

        suffix_max[m] = gaps[m]
        for i in range(m - 1, -1, -1):
            suffix_max[i] = max(suffix_max[i + 1], gaps[i])

        # ---------------------------------------------------------------------
        # STEP 3: Start with the best answer without moving any holiday.
        #
        # Since moving is optional, we must consider the current maximum working
        # block as a valid candidate answer.
        # ---------------------------------------------------------------------
        answer: int = max(gaps)

        # ---------------------------------------------------------------------
        # STEP 4: Try removing each holiday once and compute the best possible
        # result if that holiday is the one we move.
        #
        # For holiday i:
        # - Left working block length  = gaps[i]
        # - Right working block length = gaps[i+1]
        #
        # Removing this holiday merges them into:
        #   merged = gaps[i] + 1 + gaps[i+1]
        #
        # Now we must reinsert the holiday somewhere on a currently working day.
        #
        # Case A: There exists at least one working day outside this merged block.
        #         Then we place the holiday there, and the merged block remains
        #         intact with length "merged".
        #
        # Case B: No working day exists outside this merged block.
        #         Then the holiday must be placed inside the merged block, which
        #         splits it. The best possible consecutive block is then merged - 1
        #         = gaps[i] + gaps[i+1].
        #
        # We determine whether Case A is possible by checking the maximum gap
        # among all gaps except gaps[i] and gaps[i+1].
        # ---------------------------------------------------------------------
        for i in range(m):
            left_gap: int = gaps[i]
            right_gap: int = gaps[i + 1]
            merged_block: int = left_gap + 1 + right_gap

            # Maximum gap strictly to the left of gaps[i].
            max_left_outside: int = prefix_max[i - 1] if i - 1 >= 0 else 0

            # Maximum gap strictly to the right of gaps[i+1].
            max_right_outside: int = suffix_max[i + 2] if i + 2 <= m else 0

            # This is the largest working block that is NOT one of the two gaps
            # adjacent to holiday i.
            outside_max_gap: int = max(max_left_outside, max_right_outside)

            if outside_max_gap > 0:
                # There is at least one working day outside the merged block.
                # So we can move the holiday there and keep the full merged block.
                candidate: int = merged_block
            else:
                # There is nowhere outside to place the moved holiday.
                # Therefore we must place it inside the merged block, reducing the
                # longest possible consecutive block by exactly 1.
                candidate = merged_block - 1

            answer = max(answer, candidate)

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 10
    holidays1 = [3, 8]
    result1 = solution.max_consecutive_working_days(n1, holidays1)
    print(f"n = {n1}, holidays = {holidays1} -> {result1}")  # Expected: 7

    # Example 2
    n2 = 15
    holidays2 = [4, 8, 12]
    result2 = solution.max_consecutive_working_days(n2, holidays2)
    print(f"n = {n2}, holidays = {holidays2} -> {result2}")  # Expected: 7

    # Additional quick checks
    n3 = 5
    holidays3 = [3]
    result3 = solution.max_consecutive_working_days(n3, holidays3)
    print(f"n = {n3}, holidays = {holidays3} -> {result3}")

    n4 = 6
    holidays4 = [1, 6]
    result4 = solution.max_consecutive_working_days(n4, holidays4)
    print(f"n = {n4}, holidays = {holidays4} -> {result4}")