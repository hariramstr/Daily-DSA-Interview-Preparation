"""
Title: Pair Dancers for a Narrow Stage

Problem Description:
A performance team is preparing dancers for a stage act. Each dancer has a body width
represented by an integer in the array widths, where widths[i] is the width of the
i-th dancer. A stage mark can hold at most two dancers at the same time, and the
combined width of any pair assigned to the same mark must be less than or equal to
stageLimit. A dancer may also perform alone on a mark.

Your task is to determine the minimum number of stage marks needed so that every dancer
can be assigned to exactly one mark.

You may reorder the dancers in any way before assigning them. The goal is not to count
the number of valid pairings, but to minimize the total number of marks used. This makes
greedy pairing important: using a wide dancer with the best possible narrow partner can
reduce the total number of marks.

Return the minimum number of stage marks required.

Constraints:
- 1 <= widths.length <= 100000
- 1 <= widths[i] <= stageLimit <= 1000000000
- Every dancer must be assigned to exactly one mark
- Each mark can hold one or two dancers only

Example 1:
Input: widths = [2, 3, 4, 5], stageLimit = 6
Output: 3
Explanation: Pair dancers with widths 2 and 4 on one mark. The dancers with widths 3
and 5 must stand alone. So the minimum number of marks is 3.

Example 2:
Input: widths = [1, 2, 2, 3, 4], stageLimit = 4
Output: 3
Explanation: One optimal arrangement is (1,3), (2,2), and (4). This uses 3 marks,
which is the minimum possible.
"""

from typing import List


class Solution:
    def min_stage_marks(self, widths: List[int], stageLimit: int) -> int:
        """
        Compute the minimum number of stage marks needed to place all dancers.

        The algorithm sorts the widths, then uses two pointers:
        - One pointer starts at the lightest/narrowest dancer.
        - One pointer starts at the heaviest/widest dancer.

        On each step, we always assign the widest remaining dancer to a stage mark.
        If the narrowest remaining dancer can fit with that widest dancer without
        exceeding the stage limit, we pair them together. Otherwise, the widest
        dancer must stand alone.

        Args:
            widths: A list of dancer widths.
            stageLimit: The maximum total width allowed on one stage mark.

        Returns:
            The minimum number of stage marks required.

        Time complexity:
            O(n log n), due to sorting.

        Space complexity:
            O(1) extra space beyond the sort operation's internal usage.
        """
        # Step 1: Sort the dancer widths in non-decreasing order.
        #
        # Why sort?
        # Sorting allows us to efficiently try the best greedy pairing:
        # - The widest remaining dancer is the hardest to place.
        # - The narrowest remaining dancer is the best candidate to pair with them.
        #
        # If the narrowest dancer cannot pair with the widest dancer, then no one can,
        # because every other remaining dancer is at least as wide as the narrowest one.
        widths.sort()

        # 'left' points to the narrowest dancer not yet assigned.
        left: int = 0

        # 'right' points to the widest dancer not yet assigned.
        right: int = len(widths) - 1

        # This will count how many stage marks we use.
        marks_used: int = 0

        # Step 2: Process dancers until all have been assigned.
        #
        # Each loop iteration uses exactly one stage mark, because we always place
        # the widest remaining dancer either:
        # - alone, or
        # - paired with the narrowest remaining dancer.
        while left <= right:
            # We are definitely going to use one mark for the widest remaining dancer.
            marks_used += 1

            # Check whether the narrowest and widest remaining dancers can share a mark.
            #
            # If yes:
            #   - Pair them together.
            #   - Move 'left' forward because that narrowest dancer is now assigned.
            #
            # If no:
            #   - The widest dancer must go alone.
            #   - We do NOT move 'left', because the narrowest dancer is still unassigned.
            #
            # Why is this greedy choice correct?
            # - The widest dancer is the most difficult to pair.
            # - If they can pair with anyone, pairing them with the narrowest dancer
            #   preserves more room for the remaining dancers.
            # - If they cannot pair with the narrowest dancer, they cannot pair with
            #   any other remaining dancer either.
            if widths[left] + widths[right] <= stageLimit:
                left += 1

            # In every case, the widest dancer at 'right' has now been assigned,
            # either alone or as part of a pair.
            right -= 1

        # After the loop, every dancer has been assigned to exactly one mark.
        return marks_used


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    widths_1: List[int] = [2, 3, 4, 5]
    stage_limit_1: int = 6
    result_1: int = solution.min_stage_marks(widths_1, stage_limit_1)
    print("Example 1 Result:", result_1)  # Expected: 3

    # Example 2
    widths_2: List[int] = [1, 2, 2, 3, 4]
    stage_limit_2: int = 4
    result_2: int = solution.min_stage_marks(widths_2, stage_limit_2)
    print("Example 2 Result:", result_2)  # Expected: 3

    # Additional quick checks
    widths_3: List[int] = [3]
    stage_limit_3: int = 3
    result_3: int = solution.min_stage_marks(widths_3, stage_limit_3)
    print("Single Dancer Result:", result_3)  # Expected: 1

    widths_4: List[int] = [1, 1, 1, 1]
    stage_limit_4: int = 2
    result_4: int = solution.min_stage_marks(widths_4, stage_limit_4)
    print("All Pairable Result:", result_4)  # Expected: 2