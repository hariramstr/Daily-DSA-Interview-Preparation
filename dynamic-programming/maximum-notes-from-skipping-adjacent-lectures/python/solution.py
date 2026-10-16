"""
Title: Maximum Notes from Skipping Adjacent Lectures

Problem Description:
You are planning your day at a university conference. There are n lectures scheduled
in a single hallway, and each lecture has a note value representing how useful your
notes would be if you attend that lecture. However, attending two adjacent lectures
is not allowed because there is no transition time between neighboring rooms, so you
must skip at least one lecture between any two lectures you choose.

Given an integer array notes where notes[i] is the note value of the i-th lecture,
return the maximum total note value you can collect.

You may choose to attend no lectures at all, so the answer can be 0 if every value
is negative. This makes the problem slightly more practical than simply forcing a
choice.

A dynamic programming solution works well here. For each position, decide whether it
is better to skip the current lecture or attend it and add its value to the best
answer from two positions earlier.

Constraints:
- 1 <= notes.length <= 100000
- -1000 <= notes[i] <= 1000

Example 1:
Input: notes = [5, 1, 6, 2, 4]
Output: 15
Explanation: Attend lectures with values 5, 6, and 4. They are not adjacent, and the
total is 15.

Example 2:
Input: notes = [4, -2, 3, 7]
Output: 11
Explanation: The best choice is to attend lectures with values 4 and 7 for a total
of 11. Attending 3 and 7 is not allowed because they are adjacent.
"""

from typing import List


class Solution:
    def max_notes(self, notes: List[int]) -> int:
        """
        Compute the maximum total note value that can be collected without
        attending two adjacent lectures.

        This uses dynamic programming with constant extra space. At each lecture,
        we decide between:
        1. Skipping the current lecture, keeping the best total so far
        2. Attending the current lecture, which means we must add its value to
           the best total from two lectures back

        Because attending no lectures is allowed, the result is never forced to
        be negative. If all values are negative, the answer is 0.

        Args:
            notes: A list of integers where notes[i] is the note value of the
                i-th lecture.

        Returns:
            The maximum possible sum of note values with no two chosen lectures
            being adjacent.

        Time complexity:
            O(n), where n is the number of lectures

        Space complexity:
            O(1), because only a few variables are used regardless of input size
        """
        # These two variables store the dynamic programming state in a compact way.
        #
        # prev_two:
        #   The best answer considering lectures up to index i - 2.
        #
        # prev_one:
        #   The best answer considering lectures up to index i - 1.
        #
        # We start both at 0 because:
        # - Choosing no lectures is allowed
        # - Therefore, before processing anything, the best total is 0
        # - This also naturally handles negative values, since we never need to
        #   accept a negative total when skipping everything gives 0
        prev_two: int = 0
        prev_one: int = 0

        # Process each lecture value from left to right.
        #
        # For each lecture, we compute the best answer ending at this position.
        # There are exactly two choices:
        #
        # 1. Skip this lecture:
        #    Then the best total remains prev_one, because the best answer up to
        #    the previous lecture is still valid.
        #
        # 2. Attend this lecture:
        #    Then we cannot attend the previous lecture, so we add the current
        #    lecture's value to prev_two.
        #
        # The better of these two choices becomes the new best answer.
        for value in notes:
            # If we attend the current lecture, we must combine it with the best
            # answer from two positions back.
            take_current: int = prev_two + value

            # If we skip the current lecture, we simply keep the best answer from
            # the previous position.
            skip_current: int = prev_one

            # Choose the better of the two options.
            #
            # This is the core dynamic programming transition:
            # dp[i] = max(dp[i - 1], dp[i - 2] + notes[i])
            #
            # Because prev_one and prev_two already represent dp[i - 1] and
            # dp[i - 2], we can compute the new answer directly.
            current_best: int = max(skip_current, take_current)

            # Shift the window forward for the next iteration:
            #
            # - The old prev_one becomes the new prev_two
            # - The newly computed current_best becomes the new prev_one
            #
            # After this update:
            # - prev_two represents the best answer up to the previous-previous lecture
            # - prev_one represents the best answer up to the current lecture
            prev_two = prev_one
            prev_one = current_best

        # After processing all lectures, prev_one holds the best answer for the
        # entire array.
        return prev_one

    def rob(self, notes: List[int]) -> int:
        """
        Alias method for the same dynamic programming solution.

        This method is included to provide an alternative familiar method name
        for the classic "non-adjacent maximum sum" pattern.

        Args:
            notes: A list of integers representing lecture note values.

        Returns:
            The maximum possible non-adjacent sum.

        Time complexity:
            O(n), where n is the number of lectures

        Space complexity:
            O(1)
        """
        return self.max_notes(notes)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    #
    # Trace:
    # notes = [5, 1, 6, 2, 4]
    #
    # Best choices:
    # - Attend 5
    # - Skip 1
    # - Attend 6
    # - Skip 2
    # - Attend 4
    #
    # Total = 5 + 6 + 4 = 15
    sample_1: List[int] = [5, 1, 6, 2, 4]
    result_1: int = solution.max_notes(sample_1)
    print(f"Input: {sample_1}")
    print(f"Output: {result_1}")
    print("Expected: 15")
    print()

    # Sample input 2 from the problem statement.
    #
    # Trace:
    # notes = [4, -2, 3, 7]
    #
    # Valid best choice:
    # - Attend 4
    # - Skip -2
    # - Skip 3
    # - Attend 7
    #
    # Total = 4 + 7 = 11
    #
    # Note:
    # Choosing 3 and 7 together is not allowed because they are adjacent.
    sample_2: List[int] = [4, -2, 3, 7]
    result_2: int = solution.max_notes(sample_2)
    print(f"Input: {sample_2}")
    print(f"Output: {result_2}")
    print("Expected: 11")
    print()

    # Additional beginner-friendly test:
    # All values are negative, so the best decision is to attend no lectures.
    sample_3: List[int] = [-5, -1, -8]
    result_3: int = solution.max_notes(sample_3)
    print(f"Input: {sample_3}")
    print(f"Output: {result_3}")
    print("Expected: 0")
    print()

    # Additional small test:
    # Single lecture with positive value.
    sample_4: List[int] = [9]
    result_4: int = solution.max_notes(sample_4)
    print(f"Input: {sample_4}")
    print(f"Output: {result_4}")
    print("Expected: 9")