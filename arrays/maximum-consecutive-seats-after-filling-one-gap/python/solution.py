"""
Title: Maximum Consecutive Seats After Filling One Gap

Problem Description:
You are given an array seats where seats[i] is either 0 or 1. A value of 1 means
the seat is already occupied, and 0 means it is empty. The event organizer is
allowed to assign exactly one currently empty seat to a late guest. After doing so,
the organizer wants to know the maximum possible length of a consecutive block of
occupied seats anywhere in the row.

Return the largest number of consecutive 1s that can appear after changing at most
one 0 into a 1. If the row is already fully occupied, return the length of the
entire array.

This problem models a common interview scenario involving array scanning and window
maintenance. A correct solution should work efficiently for large rows and should
not try every possible seat independently.

Constraints:
- 1 <= seats.length <= 200000
- seats[i] is either 0 or 1
- You may change at most one 0 to 1

Example 1:
Input: seats = [1,1,0,1,1,1,0,1]
Output: 6
Explanation: Fill the empty seat at index 2. The row becomes
[1,1,1,1,1,1,0,1], which contains a consecutive occupied block of length 6.

Example 2:
Input: seats = [0,0,1,0,1,1,0]
Output: 4
Explanation: Fill the empty seat at index 3. The row becomes
[0,0,1,1,1,1,0], giving a longest consecutive occupied block of length 4.
"""

from typing import List


class Solution:
    def longest_occupied_block_after_one_fill(self, seats: List[int]) -> int:
        """
        Find the maximum length of consecutive occupied seats after changing
        at most one 0 into a 1.

        Args:
            seats: A list of integers where each value is either 0 or 1.

        Returns:
            The largest possible number of consecutive 1s after flipping
            at most one 0 to 1.

        Time complexity:
            O(n), where n is the length of seats, because each index is processed
            at most a constant number of times by the sliding window.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # We use the classic sliding window technique.
        #
        # Core idea:
        # We want the longest subarray that contains at most one zero.
        # Why does that solve the problem?
        # Because if a window has:
        # - zero zeros, then it is already all 1s and needs no change
        # - one zero, then we can flip that single zero to 1
        # - more than one zero, then one flip is not enough
        #
        # So the answer is simply the maximum window length where the number
        # of zeros inside the window is <= 1.

        left: int = 0
        zero_count: int = 0
        best: int = 0

        # Expand the window one seat at a time using the right pointer.
        for right in range(len(seats)):
            # If the new seat entering the window is empty (0),
            # we record that the current window now contains one more zero.
            if seats[right] == 0:
                zero_count += 1

            # If the window now contains more than one zero,
            # it is no longer valid for our problem because we are allowed
            # to fill at most one empty seat.
            #
            # So we shrink the window from the left until it becomes valid again.
            while zero_count > 1:
                # If the seat leaving the window is a zero,
                # then removing it reduces the zero count.
                if seats[left] == 0:
                    zero_count -= 1

                # Move the left boundary rightward to shrink the window.
                left += 1

            # At this point, the window [left, right] contains at most one zero,
            # so it represents a valid segment that can become all 1s after
            # at most one fill.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger.
            if current_length > best:
                best = current_length

        return best

    def longestOnes(self, seats: List[int]) -> int:
        """
        Compatibility-style wrapper method that returns the same result.

        Args:
            seats: A list of integers where each value is either 0 or 1.

        Returns:
            The largest possible number of consecutive 1s after flipping
            at most one 0 to 1.

        Time complexity:
            O(n), where n is the length of seats.

        Space complexity:
            O(1).
        """
        return self.longest_occupied_block_after_one_fill(seats)


if __name__ == "__main__":
    solution = Solution()

    # Sample input from Example 1
    seats1: List[int] = [1, 1, 0, 1, 1, 1, 0, 1]
    result1: int = solution.longest_occupied_block_after_one_fill(seats1)
    print("Example 1:")
    print("Input:", seats1)
    print("Output:", result1)
    print("Expected:", 6)
    print()

    # Sample input from Example 2
    seats2: List[int] = [0, 0, 1, 0, 1, 1, 0]
    result2: int = solution.longest_occupied_block_after_one_fill(seats2)
    print("Example 2:")
    print("Input:", seats2)
    print("Output:", result2)
    print("Expected:", 4)
    print()

    # Additional check: already fully occupied
    seats3: List[int] = [1, 1, 1, 1]
    result3: int = solution.longest_occupied_block_after_one_fill(seats3)
    print("Additional Example 3:")
    print("Input:", seats3)
    print("Output:", result3)
    print("Expected:", 4)
    print()

    # Additional check: single empty seat
    seats4: List[int] = [0]
    result4: int = solution.longest_occupied_block_after_one_fill(seats4)
    print("Additional Example 4:")
    print("Input:", seats4)
    print("Output:", result4)
    print("Expected:", 1)