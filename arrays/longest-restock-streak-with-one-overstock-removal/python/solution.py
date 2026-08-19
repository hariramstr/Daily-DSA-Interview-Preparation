"""
Title: Longest Restock Streak with One Overstock Removal

Problem Description:
A warehouse records the number of units restocked each day in an integer array
`restocks`, where `restocks[i]` is the number of units added on day `i`.

Management wants to identify the longest streak of days that looks steadily
improving. A streak is considered steadily improving if, after optionally
removing at most one day from that streak, the remaining days form a strictly
increasing sequence.

Your task is to return the maximum possible length of a contiguous streak
satisfying this rule. The removed day, if any, must come from inside the chosen
streak, and removing it should connect the left and right parts into one
strictly increasing sequence. You are not allowed to reorder days; only one
deletion is permitted.

Examples:
- [3, 5, 4, 6, 7] -> 5
  Remove 4, remaining sequence becomes [3, 5, 6, 7].

- [1, 2, 3, 2, 3, 4] -> 4
  A best valid streak has length 4.

Constraints:
- 1 <= restocks.length <= 200000
- -10^9 <= restocks[i] <= 10^9

Goal:
Return the maximum length of a contiguous subarray such that, after deleting at
most one element from that chosen subarray, the remaining elements are strictly
increasing.
"""

from typing import List


class Solution:
    def longest_restock_streak(self, restocks: List[int]) -> int:
        """
        Return the maximum length of a contiguous streak that can become strictly
        increasing after deleting at most one element.

        Args:
            restocks: List of daily restock counts.

        Returns:
            The maximum valid contiguous streak length.

        Time complexity:
            O(n), where n is the length of restocks.

        Space complexity:
            O(n), for the helper arrays storing increasing-run lengths.
        """
        n: int = len(restocks)

        # If there is only one day, that single day is trivially a valid streak.
        if n == 1:
            return 1

        # left[i] will store:
        # "How long is the strictly increasing contiguous run that ends exactly at i?"
        #
        # Example:
        # restocks = [3, 5, 4, 6, 7]
        # left      = [1, 2, 1, 2, 3]
        #
        # Why this is useful:
        # If we want to delete some element at position i, then the left side of
        # the resulting sequence can come from the increasing run ending at i - 1.
        left: List[int] = [1] * n

        # Build the left array from left to right.
        for i in range(1, n):
            # If current value is strictly larger than previous value,
            # then the increasing run can be extended by 1.
            if restocks[i] > restocks[i - 1]:
                left[i] = left[i - 1] + 1
            # Otherwise, the strictly increasing run must restart at this index.
            else:
                left[i] = 1

        # right[i] will store:
        # "How long is the strictly increasing contiguous run that starts exactly at i?"
        #
        # Example:
        # restocks = [3, 5, 4, 6, 7]
        # right     = [2, 1, 3, 2, 1]
        #
        # Why this is useful:
        # If we delete an element at position i, then the right side of the
        # resulting sequence can come from the increasing run starting at i + 1.
        right: List[int] = [1] * n

        # Build the right array from right to left.
        for i in range(n - 2, -1, -1):
            # If current value is strictly smaller than next value,
            # then the increasing run starting here can extend through i + 1.
            if restocks[i] < restocks[i + 1]:
                right[i] = right[i + 1] + 1
            # Otherwise, the run starting here has length 1.
            else:
                right[i] = 1

        # At minimum, any already strictly increasing subarray is valid with
        # "zero deletions". The best such answer is simply the maximum value in left
        # (or right). We use left here.
        answer: int = max(left)

        # Now consider deleting exactly one element at each possible position i.
        #
        # There are three structural cases:
        #
        # 1) Delete the first element of the chosen streak:
        #    Then we can keep an increasing run starting at i + 1.
        #
        # 2) Delete the last element of the chosen streak:
        #    Then we can keep an increasing run ending at i - 1.
        #
        # 3) Delete a middle element i:
        #    We want to connect the increasing run ending at i - 1 with the
        #    increasing run starting at i + 1.
        #    This is only valid if restocks[i - 1] < restocks[i + 1].
        #
        # Important detail:
        # The chosen streak length counts the deleted element too.
        # So if we connect left part length L and right part length R by deleting i,
        # the original streak length is L + 1 + R.
        #
        # Similarly:
        # - deleting the first element gives streak length 1 + right[i + 1]
        # - deleting the last element gives streak length left[i - 1] + 1
        #
        # We try every deletion position and keep the maximum valid streak length.
        for i in range(n):
            # Case 1: delete the first element of the chosen streak.
            # We can choose a streak [i, ...] and delete restocks[i], leaving the
            # increasing run that starts at i + 1.
            if i + 1 < n:
                answer = max(answer, 1 + right[i + 1])

            # Case 2: delete the last element of the chosen streak.
            # We can choose a streak [..., i] and delete restocks[i], leaving the
            # increasing run that ends at i - 1.
            if i - 1 >= 0:
                answer = max(answer, left[i - 1] + 1)

            # Case 3: delete a middle element and connect both sides.
            # This only makes sense if i has both a left neighbor and a right neighbor.
            if 0 < i < n - 1:
                # To connect the two increasing runs into one strictly increasing
                # sequence after deleting restocks[i], the last kept value on the
                # left must be strictly smaller than the first kept value on the right.
                if restocks[i - 1] < restocks[i + 1]:
                    combined_length: int = left[i - 1] + 1 + right[i + 1]
                    answer = max(answer, combined_length)

        # The answer cannot exceed n because the chosen streak is a contiguous
        # subarray of the original array.
        return min(answer, n)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [3, 5, 4, 6, 7],       # Expected: 5
        [1, 2, 3, 2, 3, 4],    # Expected: 4
        [2, 2, 3],             # Expected: 3
        [1],                   # Expected: 1
        [5, 4, 3, 2],          # Expected: 2
        [1, 2, 3, 4],          # Expected: 4
    ]

    for arr in sample_inputs:
        result: int = solution.longest_restock_streak(arr)
        print(f"restocks = {arr}")
        print(f"longest valid streak = {result}")
        print("-" * 40)