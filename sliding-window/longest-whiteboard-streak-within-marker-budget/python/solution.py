"""
Title: Longest Whiteboard Streak Within Marker Budget

Problem Description:
A teacher is writing a sequence of lesson sections on a whiteboard. The array
`inkUse` represents how many units of marker ink are needed for each section, in order.
Because the teacher only has a limited amount of ink for one uninterrupted writing
session, you need to find the longest contiguous group of sections that can be written
without exceeding the available ink budget `maxInk`.

Return the length of the longest contiguous subarray whose sum is less than or equal to
`maxInk`.

This is a classic interview setting for a sliding window because all values in `inkUse`
are non-negative. As the right end of the window expands, the total ink usage increases.
If the total becomes too large, move the left end forward until the window becomes valid
again.

Constraints:
- 1 <= inkUse.length <= 100000
- 0 <= inkUse[i] <= 10000
- 0 <= maxInk <= 1000000000
- All section ink costs are non-negative integers.

Example 1:
Input: inkUse = [2, 1, 3, 2, 1], maxInk = 5
Output: 2
Explanation: Valid contiguous groups include [2,1], [3,2], and [2,1].
Any length-3 group uses more than 5 units of ink, so the answer is 2.

Example 2:
Input: inkUse = [1, 0, 2, 1, 1, 0, 1], maxInk = 4
Output: 5
Explanation: The subarray [2,1,1,0,1] has total ink usage 5, so it is too large.
But [0,2,1,1,0] has total 4 and length 5, which is the longest valid writing streak.
"""

from typing import List


class Solution:
    def longest_whiteboard_streak(self, inkUse: List[int], maxInk: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is at most maxInk.

        Args:
            inkUse: A list of non-negative integers where each value is the ink cost
                of one lesson section.
            maxInk: The maximum total ink allowed for one contiguous writing session.

        Returns:
            The maximum length of a contiguous subarray with sum <= maxInk.

        Time complexity:
            O(n), where n is the length of inkUse, because each index is visited
            at most twice: once by the right pointer and once by the left pointer.

        Space complexity:
            O(1), because only a few extra variables are used.
        """
        # The sliding window will represent the current contiguous group of sections
        # we are considering. The window is defined by two indices:
        #
        # - left:  the starting index of the current window
        # - right: the ending index of the current window
        #
        # Because all values are non-negative, when we move "right" forward,
        # the sum of the window can only stay the same or increase.
        # That property makes sliding window the perfect approach here.
        left: int = 0

        # This variable stores the sum of the current window inkUse[left:right+1].
        current_sum: int = 0

        # This variable stores the best (maximum) valid window length found so far.
        best_length: int = 0

        # We expand the window one section at a time by moving the right pointer.
        for right in range(len(inkUse)):
            # Add the new section's ink cost into the running sum because
            # the window now includes inkUse[right].
            current_sum += inkUse[right]

            # If the current window uses too much ink, it is invalid.
            # Since all values are non-negative, the only way to reduce the sum
            # is to move the left edge forward and remove elements from the window.
            #
            # We keep shrinking until the window becomes valid again
            # (that is, until current_sum <= maxInk).
            while current_sum > maxInk and left <= right:
                # Remove the section at the left edge from the running sum,
                # because that section will no longer be part of the window.
                current_sum -= inkUse[left]

                # Move the left edge one step to the right.
                left += 1

            # At this point, the window from left to right is guaranteed to be valid:
            # its total sum is <= maxInk.
            #
            # So we can compute its length and compare it with the best answer seen so far.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer.
            if current_length > best_length:
                best_length = current_length

        # After processing all possible right endpoints, best_length contains
        # the length of the longest valid contiguous subarray.
        return best_length

    def longestSubarrayWithinBudget(self, inkUse: List[int], maxInk: int) -> int:
        """
        Wrapper method that calls the main sliding window solution.

        Args:
            inkUse: A list of non-negative integers representing ink usage.
            maxInk: The maximum allowed sum for a contiguous subarray.

        Returns:
            The length of the longest valid contiguous subarray.

        Time complexity:
            O(n), where n is the length of inkUse.

        Space complexity:
            O(1).
        """
        return self.longest_whiteboard_streak(inkUse, maxInk)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    ink_use_1: List[int] = [2, 1, 3, 2, 1]
    max_ink_1: int = 5
    result_1: int = solution.longest_whiteboard_streak(ink_use_1, max_ink_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2
    ink_use_2: List[int] = [1, 0, 2, 1, 1, 0, 1]
    max_ink_2: int = 4
    result_2: int = solution.longest_whiteboard_streak(ink_use_2, max_ink_2)
    print("Example 2 Result:", result_2)  # Expected: 5

    # Additional beginner-friendly checks
    ink_use_3: List[int] = [0, 0, 0]
    max_ink_3: int = 0
    result_3: int = solution.longest_whiteboard_streak(ink_use_3, max_ink_3)
    print("All Zeroes Result:", result_3)  # Expected: 3

    ink_use_4: List[int] = [5, 6, 7]
    max_ink_4: int = 4
    result_4: int = solution.longest_whiteboard_streak(ink_use_4, max_ink_4)
    print("No Valid Positive Section Result:", result_4)  # Expected: 0