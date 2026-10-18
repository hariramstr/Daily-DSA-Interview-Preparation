"""
Title: Maximum Consecutive Days After One Schedule Swap

Problem Description:
You are given a binary array days where days[i] is 1 if an employee is scheduled to work
on day i, and 0 if they are off. The company allows at most one swap operation: you may
choose one index containing 1 and one index containing 0, and swap their values. This keeps
the total number of working days the same, but may move a workday to a more useful position.

Your task is to return the maximum possible length of a consecutive block of 1s after
performing at most one such swap. You may also choose not to swap if the array is already
optimal.

A swap can happen between any two positions in the array, not necessarily adjacent.
Note that if there is no 0 in the array, the answer is simply the full length of the array.
If there is no 1 in the array, the answer is 0.

This problem tests whether you can reason about contiguous segments while respecting the
global limit on how many 1s exist in the entire array. An optimal solution should run in
linear time.

Constraints:
- 1 <= days.length <= 200000
- days[i] is either 0 or 1

Example 1:
Input: days = [1,1,0,1,1,1,0,1]
Output: 6
Explanation: Swap the last 1 with the 0 at index 2. The array can become
[1,1,1,1,1,1,0,0], which contains a consecutive block of 6 working days.

Example 2:
Input: days = [1,0,1,0,1]
Output: 3
Explanation: Swap one of the outer 1s with the middle 0 at index 1 or 3.
The best possible consecutive block of 1s is 3.
"""

from typing import List


class Solution:
    def max_consecutive_days_after_one_swap(self, days: List[int]) -> int:
        """
        Return the maximum possible length of consecutive 1s after at most one swap.

        The key idea is:
        - Count how many total 1s exist in the entire array.
        - For each position that contains 0, measure:
            left_ones + right_ones
          where:
            left_ones  = number of consecutive 1s immediately to the left
            right_ones = number of consecutive 1s immediately to the right
        - If there exists at least one extra 1 somewhere else in the array beyond those
          adjacent groups, we can swap that external 1 into this 0 position and extend
          the merged block by 1.
        - Otherwise, the best we can do at that 0 is just merge the adjacent groups
          without exceeding the total number of 1s.

        Args:
            days: Binary list where 1 means working day and 0 means off day.

        Returns:
            Maximum possible consecutive block of 1s after at most one swap.

        Time complexity:
            O(n), where n is the length of days.

        Space complexity:
            O(n), due to prefix-like helper arrays storing consecutive counts.
        """
        n: int = len(days)

        # Count the total number of working days (1s) in the entire schedule.
        # This global count is extremely important because even if a local merge
        # looks large, we can never create a block longer than the total number
        # of 1s that exist in the whole array.
        total_ones: int = sum(days)

        # Edge case 1:
        # If there are no working days at all, then no swap can create a 1,
        # because swapping only exchanges existing values.
        # Therefore the answer must be 0.
        if total_ones == 0:
            return 0

        # Edge case 2:
        # If every day is already a working day, then the whole array is one
        # consecutive block of 1s, and there is no 0 to swap with anyway.
        if total_ones == n:
            return n

        # left[i] will store:
        # "How many consecutive 1s end exactly at index i?"
        #
        # Example:
        # days = [1, 1, 0, 1]
        # left = [1, 2, 0, 1]
        #
        # This helps us quickly know how many 1s are immediately to the left
        # side of a zero position.
        left: List[int] = [0] * n

        # Build the left consecutive-count array.
        # We scan from left to right.
        for i in range(n):
            if days[i] == 1:
                if i == 0:
                    # At the first position, a 1 starts a run of length 1.
                    left[i] = 1
                else:
                    # Extend the previous consecutive run by 1.
                    left[i] = left[i - 1] + 1
            else:
                # A 0 breaks any consecutive run of 1s.
                left[i] = 0

        # right[i] will store:
        # "How many consecutive 1s start exactly at index i?"
        #
        # Example:
        # days = [1, 1, 0, 1]
        # right = [2, 1, 0, 1]
        #
        # This helps us quickly know how many 1s are immediately to the right
        # side of a zero position.
        right: List[int] = [0] * n

        # Build the right consecutive-count array.
        # We scan from right to left.
        for i in range(n - 1, -1, -1):
            if days[i] == 1:
                if i == n - 1:
                    # At the last position, a 1 starts a run of length 1.
                    right[i] = 1
                else:
                    # Extend the next consecutive run by 1.
                    right[i] = right[i + 1] + 1
            else:
                # A 0 breaks any consecutive run of 1s.
                right[i] = 0

        # This variable will track the best answer found so far.
        # We initialize it to 0 and update it as we inspect each position.
        best: int = 0

        # First, consider the possibility of not swapping at all.
        # The array may already contain some long block of 1s.
        # Since left[i] stores the length of the consecutive run ending at i,
        # the maximum value in left is the longest existing block of 1s.
        best = max(left)

        # Now inspect every index that contains 0.
        # Why only zeros?
        # Because the only useful swap is to place a 1 into some 0 position
        # to potentially connect or extend neighboring runs of 1s.
        for i in range(n):
            if days[i] == 0:
                # Count consecutive 1s immediately on the left side of this zero.
                # If i == 0, there is no left side.
                left_ones: int = left[i - 1] if i > 0 else 0

                # Count consecutive 1s immediately on the right side of this zero.
                # If i == n - 1, there is no right side.
                right_ones: int = right[i + 1] if i < n - 1 else 0

                # If we conceptually "fill" this zero with a 1, then the local
                # merged block would be:
                # left_ones + 1 + right_ones
                #
                # However, that extra +1 is only possible if there exists some
                # other 1 elsewhere in the array that we can swap into this zero.
                #
                # The adjacent left and right groups already account for
                # left_ones + right_ones many 1s. If total_ones is larger than that,
                # then there is at least one additional 1 somewhere else available.
                if left_ones + right_ones < total_ones:
                    candidate: int = left_ones + 1 + right_ones
                else:
                    # If all 1s are already exactly in those adjacent groups,
                    # then there is no extra 1 elsewhere to bring in.
                    # In that case, after one swap, we can at best rearrange
                    # those existing 1s into a block of size left_ones + right_ones.
                    candidate = left_ones + right_ones

                # Update the best answer.
                if candidate > best:
                    best = candidate

        return best


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [1, 1, 0, 1, 1, 1, 0, 1]
    sample_2: List[int] = [1, 0, 1, 0, 1]

    result_1: int = solution.max_consecutive_days_after_one_swap(sample_1)
    result_2: int = solution.max_consecutive_days_after_one_swap(sample_2)

    print(f"Input: {sample_1}")
    print(f"Output: {result_1}")
    print("Expected: 6")
    print()

    print(f"Input: {sample_2}")
    print(f"Output: {result_2}")
    print("Expected: 3")