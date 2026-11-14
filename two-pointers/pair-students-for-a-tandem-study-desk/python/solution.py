"""
Title: Pair Students for a Tandem Study Desk

Problem Description:
A library has a row of students waiting to use tandem study desks. Each desk can seat
exactly two students, and the combined backpack sizes of the two students assigned to
the same desk must not exceed a given limit.

You are given:
- backpackSizes: an integer list where backpackSizes[i] is the size of the i-th student's backpack
- deskLimit: the maximum total backpack size allowed at one desk

Return the minimum number of tandem desks needed to seat all students.

Rules:
- Each desk may hold either one student or two students.
- If two students are paired together, their combined backpack sizes must be less than
  or equal to deskLimit.
- You may rearrange students in any way before assigning desks.
- Every student must be assigned to exactly one desk.

Key Idea:
This problem is solved efficiently by sorting the backpack sizes and using a two-pointer
strategy:
- Try to pair the lightest remaining student with the heaviest remaining student.
- If they fit together, seat them at one desk.
- If they do not fit, the heaviest student must sit alone.

Constraints:
- 1 <= backpackSizes.length <= 50000
- 1 <= backpackSizes[i] <= deskLimit <= 100000
"""


from typing import List


class Solution:
    def min_tandem_desks(self, backpackSizes: List[int], deskLimit: int) -> int:
        """
        Compute the minimum number of tandem desks needed to seat all students.

        The algorithm sorts the backpack sizes, then uses two pointers:
        - One pointer starts at the smallest backpack.
        - One pointer starts at the largest backpack.
        On each step, we always assign the largest remaining student to a desk.
        If the smallest remaining student can share that desk without exceeding
        the limit, we pair them together. Otherwise, the largest student sits alone.

        Args:
            backpackSizes: A list of positive integers representing backpack sizes.
            deskLimit: The maximum allowed combined backpack size for one desk.

        Returns:
            The minimum number of desks required.

        Time complexity:
            O(n log n), due to sorting.

        Space complexity:
            O(1) extra space beyond the sort implementation details.
        """
        # First, sort the backpack sizes so we can make smart pairing decisions.
        #
        # Why sorting helps:
        # - The smallest backpack is the easiest one to pair with others.
        # - The largest backpack is the hardest one to pair because it leaves the least room.
        # - By checking the smallest and largest together, we can greedily decide whether
        #   the largest student can share a desk or must sit alone.
        backpackSizes.sort()

        # 'left' points to the lightest remaining student not yet assigned.
        left: int = 0

        # 'right' points to the heaviest remaining student not yet assigned.
        right: int = len(backpackSizes) - 1

        # This will count how many desks we use in total.
        desks_used: int = 0

        # Continue until all students have been assigned.
        #
        # At every iteration:
        # - We will definitely place the student at 'right' into a desk.
        # - Then we check whether the student at 'left' can join them.
        while left <= right:
            # We are about to use one desk for the heaviest remaining student.
            desks_used += 1

            # Check whether the lightest and heaviest remaining students can share a desk.
            #
            # If their combined backpack sizes are within the allowed limit,
            # pairing them is always a good choice:
            # - It uses one desk for two students.
            # - It preserves as much flexibility as possible for the remaining students.
            if backpackSizes[left] + backpackSizes[right] <= deskLimit:
                # They fit together, so both students are assigned now.
                left += 1
                right -= 1
            else:
                # They do not fit together.
                #
                # Important reasoning:
                # If the lightest student cannot fit with the heaviest student,
                # then no other student can fit with the heaviest student either,
                # because every other remaining student is at least as heavy as the lightest.
                #
                # Therefore, the heaviest student must sit alone.
                right -= 1

        # After the loop ends, every student has been assigned to exactly one desk.
        return desks_used


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Sorted: [2, 3, 4, 5], limit = 6
    # Pair 2 + 5 = 7 -> too large, so 5 sits alone
    # Pair 2 + 4 = 6 -> fits
    # 3 sits alone
    # Total desks = 3
    backpack_sizes_1: List[int] = [2, 3, 4, 5]
    desk_limit_1: int = 6
    result_1: int = solution.min_tandem_desks(backpack_sizes_1, desk_limit_1)
    print("Example 1 Output:", result_1)  # Expected: 3

    # Example 2
    # Input: [1, 2, 2, 3], limit = 3
    # Sorted: [1, 2, 2, 3]
    # 1 + 3 = 4 -> too large, so 3 sits alone
    # 1 + 2 = 3 -> fits
    # remaining 2 sits alone
    # Total desks = 3
    #
    # Note: The problem statement includes a correction saying the correct output is 3.
    backpack_sizes_2: List[int] = [1, 2, 2, 3]
    desk_limit_2: int = 3
    result_2: int = solution.min_tandem_desks(backpack_sizes_2, desk_limit_2)
    print("Example 2 Output:", result_2)  # Expected: 3

    # Additional simple test
    backpack_sizes_3: List[int] = [1, 1, 2, 2]
    desk_limit_3: int = 3
    result_3: int = solution.min_tandem_desks(backpack_sizes_3, desk_limit_3)
    print("Additional Test Output:", result_3)  # Expected: 2