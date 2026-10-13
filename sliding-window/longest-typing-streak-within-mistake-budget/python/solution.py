"""
Title: Longest Typing Streak Within Mistake Budget

Problem Description:
A typing practice platform stores a student's results as a binary array `mistakes`,
where `mistakes[i] = 1` means the `i`-th typed word contained a mistake and
`mistakes[i] = 0` means it was typed correctly.

The platform wants to highlight the longest continuous streak that can be considered
"acceptable" if the student is allowed to ignore at most `k` mistaken words inside
that streak.

Given an integer array `mistakes` and an integer `k`, return the length of the
longest contiguous subarray containing at most `k` values equal to `1`.

In other words, you must find the maximum size of a consecutive block of words where
the number of mistakes does not exceed the allowed budget `k`.

This problem should be solved efficiently for large inputs, so solutions that check
every possible subarray will be too slow.

Constraints:
- 1 <= mistakes.length <= 200000
- mistakes[i] is either 0 or 1
- 0 <= k <= mistakes.length

Example 1:
Input: mistakes = [0,1,0,0,1,0,0,0], k = 1
Output: 5

Example 2:
Input: mistakes = [1,1,0,0,0,1,0,0,1,0], k = 2
Output: 8
"""

from typing import List


class Solution:
    def longest_acceptable_streak(self, mistakes: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous subarray containing at most k ones.

        This method uses the sliding window technique:
        - Expand the right side of the window one element at a time.
        - Count how many mistakes (1s) are currently inside the window.
        - If the number of mistakes becomes greater than k, shrink the window
          from the left until it becomes valid again.
        - Track the largest valid window length seen during the process.

        Args:
            mistakes: A list of integers where 0 means correct and 1 means mistake.
            k: The maximum number of mistakes allowed inside a valid contiguous window.

        Returns:
            The length of the longest contiguous subarray with at most k ones.

        Time complexity:
            O(n), where n is the length of mistakes, because each index is visited
            at most a constant number of times by the left and right pointers.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # `left` marks the beginning of our current sliding window.
        left: int = 0

        # `mistake_count` stores how many 1s are currently inside the window
        # from index `left` to index `right`.
        mistake_count: int = 0

        # `best_length` stores the maximum valid window size found so far.
        best_length: int = 0

        # We move `right` from left to right across the array, expanding the window.
        for right in range(len(mistakes)):
            # If the new element entering the window is a mistake (1),
            # we increase the count of mistakes in the current window.
            if mistakes[right] == 1:
                mistake_count += 1

            # At this point, the window is [left, right].
            # If it contains too many mistakes, it is invalid.
            # We must shrink it from the left until the number of mistakes
            # is at most k again.
            while mistake_count > k:
                # Before moving `left` forward, check whether the element
                # leaving the window is a mistake. If it is, reduce the count.
                if mistakes[left] == 1:
                    mistake_count -= 1

                # Move the left boundary rightward to make the window smaller.
                left += 1

            # Now the window [left, right] is guaranteed to be valid:
            # it contains at most k mistakes.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than
            # any valid window we have seen before.
            if current_length > best_length:
                best_length = current_length

        # After processing all positions, `best_length` is the answer.
        return best_length

    def longestOnes(self, mistakes: List[int], k: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            mistakes: A list of integers where 0 means correct and 1 means mistake.
            k: The maximum number of mistakes allowed inside a valid contiguous window.

        Returns:
            The length of the longest contiguous subarray with at most k ones.

        Time complexity:
            O(n), where n is the length of mistakes.

        Space complexity:
            O(1).
        """
        return self.longest_acceptable_streak(mistakes, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    mistakes_1: List[int] = [0, 1, 0, 0, 1, 0, 0, 0]
    k_1: int = 1
    result_1: int = solution.longest_acceptable_streak(mistakes_1, k_1)
    print("Example 1 Result:", result_1)  # Expected: 5

    # Example 2
    mistakes_2: List[int] = [1, 1, 0, 0, 0, 1, 0, 0, 1, 0]
    k_2: int = 2
    result_2: int = solution.longest_acceptable_streak(mistakes_2, k_2)
    print("Example 2 Result:", result_2)  # Expected: 8

    # Additional quick checks
    mistakes_3: List[int] = [0, 0, 0, 0]
    k_3: int = 0
    result_3: int = solution.longest_acceptable_streak(mistakes_3, k_3)
    print("All correct, no mistakes allowed:", result_3)  # Expected: 4

    mistakes_4: List[int] = [1, 1, 1, 1]
    k_4: int = 2
    result_4: int = solution.longest_acceptable_streak(mistakes_4, k_4)
    print("All mistakes, budget 2:", result_4)  # Expected: 2

    mistakes_5: List[int] = [1, 0, 1, 0, 1, 0, 1]
    k_5: int = 3
    result_5: int = solution.longest_acceptable_streak(mistakes_5, k_5)
    print("Alternating values, budget 3:", result_5)  # Expected: 6