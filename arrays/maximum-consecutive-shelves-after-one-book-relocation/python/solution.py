"""
Title: Maximum Consecutive Shelves After One Book Relocation

Problem Description:
A library stores books on a long shelf, represented by an array `shelves` of length `n`.
Each element is either `0` or `1`, where `1` means the position currently contains a
featured book and `0` means it is empty.

The librarian may relocate at most one featured book from any position containing `1`
to any position containing `0`. After this optional relocation, determine the maximum
possible length of a consecutive block of featured books.

A relocation removes one existing `1` and places it into one existing `0`, so the total
number of featured books stays the same. You may also choose not to relocate any book.
Your task is to return the largest number of consecutive `1`s that can appear after
performing at most one such move.

This problem models a realistic array optimization scenario: one local change can merge
two nearby runs, extend an existing run, or be useless if the array is already optimal.
Be careful when a `0` separates two runs of `1`s: you can only fill that gap if there is
at least one extra `1` somewhere else to move.

Constraints:
- 1 <= n <= 200000
- shelves[i] is either 0 or 1
"""

from typing import List


class Solution:
    def max_consecutive_shelves(self, shelves: List[int]) -> int:
        """
        Compute the maximum possible length of consecutive 1s after relocating
        at most one existing 1 into one existing 0.

        The key idea:
        - First count how many 1s exist in total.
        - Then, for every index that contains 0, measure:
            left_run  = number of consecutive 1s immediately to its left
            right_run = number of consecutive 1s immediately to its right
        - If we fill that 0 with a moved 1, the merged block length is:
            left_run + 1 + right_run
          But this is only fully possible if there is at least one extra 1
          somewhere else to move from, outside those left/right runs.
        - If there is no extra 1 elsewhere, then we cannot create more 1s than
          already exist in that local combined region, so the best becomes:
            left_run + right_run
        - Also consider the case of not moving at all, which is naturally covered
          by existing runs and by the total number of 1s.

        Args:
            shelves: Binary array where 1 means occupied by a featured book and
                0 means empty.

        Returns:
            The maximum possible length of a consecutive block of 1s after at
            most one relocation.

        Time complexity:
            O(n), where n is the length of shelves.

        Space complexity:
            O(n), for prefix-like helper arrays storing consecutive run lengths.
        """
        n: int = len(shelves)

        # Count the total number of featured books currently on the shelf.
        # This value is extremely important because a relocation does NOT create
        # a new 1; it only moves one existing 1 from one place to another.
        total_ones: int = sum(shelves)

        # Edge case:
        # If there are no featured books at all, then no relocation can help,
        # because we are only allowed to move an existing 1, and none exists.
        if total_ones == 0:
            return 0

        # Edge case:
        # If every position already contains a 1, then the whole array is already
        # one consecutive block, and there is no 0 to move into anyway.
        if total_ones == n:
            return n

        # left_ones[i] will store:
        # "How many consecutive 1s end exactly at index i?"
        #
        # Example:
        # shelves = [1, 1, 0, 1]
        # left_ones = [1, 2, 0, 1]
        #
        # This helps us quickly know the run length immediately to the left of a 0.
        left_ones: List[int] = [0] * n

        # Build the left-to-right consecutive-ones array.
        for i in range(n):
            if shelves[i] == 1:
                if i == 0:
                    left_ones[i] = 1
                else:
                    left_ones[i] = left_ones[i - 1] + 1
            else:
                left_ones[i] = 0

        # right_ones[i] will store:
        # "How many consecutive 1s start exactly at index i?"
        #
        # Example:
        # shelves = [1, 1, 0, 1]
        # right_ones = [2, 1, 0, 1]
        #
        # This helps us quickly know the run length immediately to the right of a 0.
        right_ones: List[int] = [0] * n

        # Build the right-to-left consecutive-ones array.
        for i in range(n - 1, -1, -1):
            if shelves[i] == 1:
                if i == n - 1:
                    right_ones[i] = 1
                else:
                    right_ones[i] = right_ones[i + 1] + 1
            else:
                right_ones[i] = 0

        # Initialize the answer with 0.
        # We will update it using both:
        # 1) existing runs of 1s (no move needed)
        # 2) possible merged/extended runs by filling one 0
        answer: int = 0

        # First, consider the best run that already exists without any relocation.
        # This is important because the problem says "at most one move", so doing
        # nothing is allowed and may already be optimal.
        for value in left_ones:
            if value > answer:
                answer = value

        # Now inspect every 0 position as a candidate destination for the moved book.
        for i in range(n):
            if shelves[i] == 0:
                # Number of consecutive 1s immediately to the left of this zero.
                left_run: int = left_ones[i - 1] if i > 0 else 0

                # Number of consecutive 1s immediately to the right of this zero.
                right_run: int = right_ones[i + 1] if i < n - 1 else 0

                # If we fill this zero, the local block would ideally become:
                # left_run + 1 + right_run
                #
                # However, that "+1" must come from moving an existing 1 from
                # somewhere else in the array.
                #
                # The runs left_run and right_run already account for all 1s in the
                # immediate merged region around this zero. If total_ones is larger
                # than left_run + right_run, then there exists at least one extra 1
                # somewhere else that can be moved here.
                #
                # In that case, we can truly create:
                # left_run + 1 + right_run
                #
                # Otherwise, all 1s are already inside those adjacent runs, so there
                # is no external 1 available. Moving one of those internal 1s into
                # the zero would break one side while filling the gap, so the net
                # best block length is only:
                # left_run + right_run
                if total_ones > left_run + right_run:
                    candidate: int = left_run + 1 + right_run
                else:
                    candidate = left_run + right_run

                # The final answer can never exceed total_ones, because relocation
                # does not change how many 1s exist overall.
                if candidate > total_ones:
                    candidate = total_ones

                if candidate > answer:
                    answer = candidate

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [1, 1, 0, 1, 1, 0, 1],  # Expected: 5
        [1, 0, 1, 1, 0, 1],     # Expected: 4
        [1, 1, 1, 1],           # Expected: 4
        [0, 0, 0],              # Expected: 0
        [1, 0, 0, 1],           # Expected: 2
        [1, 0, 1],              # Expected: 2
    ]

    for shelves in sample_inputs:
        result = solution.max_consecutive_shelves(shelves)
        print(f"shelves = {shelves} -> {result}")