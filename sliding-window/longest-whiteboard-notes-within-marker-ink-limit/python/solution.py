"""
Title: Longest Whiteboard Notes Within Marker Ink Limit

Problem Description:
A teacher writes a sequence of note segments on a digital whiteboard. The i-th segment
uses ink[i] units of marker ink. You are given an integer array `ink` where each value
is non-negative, and an integer `maxInk` representing the maximum total ink that can be
used before the marker must be replaced.

Your task is to find the length of the longest contiguous group of note segments whose
total ink usage is less than or equal to `maxInk`.

In other words, choose a subarray `ink[l..r]` such that the sum of its elements does not
exceed `maxInk`, and return the maximum possible number of segments in such a subarray.

This problem is designed to be solved efficiently using the sliding window technique.
Since all ink values are non-negative, once a window exceeds the limit, moving the left
pointer forward can only decrease the total.

Constraints:
- 1 <= ink.length <= 100000
- 0 <= ink[i] <= 10000
- 0 <= maxInk <= 1000000000

Example 1:
Input: ink = [2, 1, 3, 2, 1], maxInk = 5
Output: 2

Example 2:
Input: ink = [0, 2, 1, 0, 1, 1], maxInk = 3
Correct Output: 4

Important note:
The original statement contains contradictory explanation text claiming the answer is 5.
That is incorrect for the given array. The true longest valid contiguous subarray length
is 4, for example:
- [0, 2, 1, 0] -> sum = 3, length = 4
- [1, 0, 1, 1] -> sum = 3, length = 4
No valid contiguous subarray of length 5 has sum <= 3.
"""

from typing import List


class Solution:
    def longest_notes_within_ink_limit(self, ink: List[int], maxInk: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most maxInk.

        Args:
            ink: A list of non-negative integers where each value represents ink usage.
            maxInk: The maximum allowed total ink for a contiguous group.

        Returns:
            The length of the longest contiguous subarray with sum <= maxInk.

        Time complexity:
            O(n), where n is the length of ink, because each element enters and leaves
            the sliding window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # `left` marks the beginning of the current sliding window.
        # We will expand the window to the right one element at a time.
        left: int = 0

        # `current_sum` stores the total ink usage of the current window ink[left:right+1].
        current_sum: int = 0

        # `best_length` stores the maximum valid window length found so far.
        best_length: int = 0

        # Move `right` from left to right across the array.
        # At each step, we include ink[right] in the current window.
        for right in range(len(ink)):
            # Add the new rightmost element into the running sum because the window
            # is being expanded to include this segment.
            current_sum += ink[right]

            # If the window sum is too large, it is invalid.
            # Because all values are non-negative, the only way to make the sum smaller
            # while keeping the window contiguous is to move `left` forward.
            #
            # We keep shrinking from the left until the window becomes valid again.
            while current_sum > maxInk and left <= right:
                # Remove the leftmost element from the sum because it is no longer
                # part of the window after we advance `left`.
                current_sum -= ink[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window ink[left:right+1] is guaranteed to have
            # sum <= maxInk, so it is a valid candidate.
            #
            # Its length is computed as:
            # right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any
            # previously seen valid window.
            if current_length > best_length:
                best_length = current_length

        # After checking all possible right endpoints, `best_length` holds the
        # maximum valid contiguous length.
        return best_length

    def solve(self, ink: List[int], maxInk: int) -> int:
        """
        Wrapper method that calls the main sliding window solution.

        Args:
            ink: A list of non-negative integers representing ink usage.
            maxInk: The maximum allowed total ink.

        Returns:
            The maximum length of a contiguous subarray with sum <= maxInk.

        Time complexity:
            O(n), where n is the length of ink.

        Space complexity:
            O(1).
        """
        return self.longest_notes_within_ink_limit(ink, maxInk)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the statement
    ink1: List[int] = [2, 1, 3, 2, 1]
    max_ink1: int = 5
    result1: int = solution.solve(ink1, max_ink1)
    print(result1)  # Expected: 2

    # Example 2 from the statement, corrected after verifying the actual array
    ink2: List[int] = [0, 2, 1, 0, 1, 1]
    max_ink2: int = 3
    result2: int = solution.solve(ink2, max_ink2)
    print(result2)  # Expected: 4

    # Additional beginner-friendly checks
    ink3: List[int] = [0, 0, 0]
    max_ink3: int = 0
    result3: int = solution.solve(ink3, max_ink3)
    print(result3)  # Expected: 3

    ink4: List[int] = [5, 1, 1]
    max_ink4: int = 2
    result4: int = solution.solve(ink4, max_ink4)
    print(result4)  # Expected: 2

    ink5: List[int] = [4]
    max_ink5: int = 3
    result5: int = solution.solve(ink5, max_ink5)
    print(result5)  # Expected: 0