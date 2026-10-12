"""
Title: Maximum Consecutive Seats After Blocking One Broken Chair

Problem Description:
You are given an array seats of length n where each element is either 0 or 1.
A value of 1 means the seat is usable, and 0 means the seat is broken.

During a small event, the venue manager is allowed to place exactly one temporary
cover over at most one broken seat so that it can be used for the event.
In other words, you may change at most one 0 in the array into a 1.

Your task is to return the maximum number of consecutive usable seats that can be
formed after applying this operation optimally.

If the array already contains only usable seats, return the total number of seats.
If there are multiple broken seats, you may choose the best one to cover.
If no broken seat is covered, that is also allowed, since the operation is
"at most one" change.

Return the length of the longest contiguous subarray containing only 1s after
changing at most one 0 to 1.

Constraints:
- 1 <= n <= 200000
- seats[i] is either 0 or 1

Examples:
1) seats = [1, 1, 0, 1, 1, 1, 0, 1]
   Output: 6

2) seats = [0, 0, 1, 1, 0, 1]
   Output: 4
"""


from typing import List


class Solution:
    def longest_usable_seats(self, seats: List[int]) -> int:
        """
        Return the maximum length of a contiguous block of usable seats after
        changing at most one broken seat (0) into a usable seat (1).

        Args:
            seats: A list of integers where:
                - 1 means the seat is usable
                - 0 means the seat is broken

        Returns:
            The maximum number of consecutive usable seats obtainable after
            changing at most one 0 to 1.

        Time complexity:
            O(n), because each element is processed at most a constant number of times
            by the sliding window pointers.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # We will use the classic "sliding window" technique.
        #
        # Core idea:
        # We want the longest contiguous subarray that contains at most one 0.
        # Why "at most one 0"?
        # Because we are allowed to convert at most one broken seat into a usable one.
        #
        # If a window contains:
        # - 0 zeros: it is already all usable seats, so it is valid.
        # - 1 zero: we can cover that one broken seat, so it is still valid.
        # - 2 or more zeros: even after covering one broken seat, at least one broken
        #   seat remains, so the entire window cannot become all 1s. Therefore invalid.
        #
        # So the problem becomes:
        # "Find the longest subarray with at most one zero."

        # Left boundary of the current sliding window.
        left: int = 0

        # Count how many broken seats (zeros) are currently inside the window.
        zero_count: int = 0

        # Best answer found so far.
        max_length: int = 0

        # Expand the window by moving the right boundary from left to right.
        for right in range(len(seats)):
            # If the new seat entering the window is broken, record it.
            if seats[right] == 0:
                zero_count += 1

            # If the window now contains more than one broken seat,
            # it is no longer valid for our "change at most one 0" rule.
            #
            # So we shrink the window from the left until it becomes valid again.
            while zero_count > 1:
                # If the seat leaving the window is a broken seat,
                # reduce the zero count because it is no longer inside the window.
                if seats[left] == 0:
                    zero_count -= 1

                # Move the left boundary rightward to shrink the window.
                left += 1

            # At this point, the window [left, right] contains at most one zero,
            # which means it is a valid candidate.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger.
            if current_length > max_length:
                max_length = current_length

        return max_length

    def longestOnes(self, seats: List[int]) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            seats: A list of 0s and 1s representing broken and usable seats.

        Returns:
            The maximum number of consecutive usable seats after changing at most
            one 0 into a 1.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.longest_usable_seats(seats)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the prompt.
    seats1: List[int] = [1, 1, 0, 1, 1, 1, 0, 1]
    result1: int = solution.longest_usable_seats(seats1)
    print("Input:", seats1)
    print("Output:", result1)
    print("Expected:", 6)
    print()

    # Important note:
    # The prompt's Example 2 explanation says the best segment length is 4,
    # even though the listed output says 3. The correct answer is 4.
    #
    # Let's verify:
    # seats = [0, 0, 1, 1, 0, 1]
    # Cover index 4 -> [0, 0, 1, 1, 1, 1]
    # Longest consecutive 1s = 4
    seats2: List[int] = [0, 0, 1, 1, 0, 1]
    result2: int = solution.longest_usable_seats(seats2)
    print("Input:", seats2)
    print("Output:", result2)
    print("Expected:", 4)
    print()

    # Additional checks for beginner understanding.

    # All seats already usable: answer should be the full length.
    seats3: List[int] = [1, 1, 1, 1]
    result3: int = solution.longest_usable_seats(seats3)
    print("Input:", seats3)
    print("Output:", result3)
    print("Expected:", 4)
    print()

    # All seats broken: we can cover one seat, so answer should be 1.
    seats4: List[int] = [0, 0, 0]
    result4: int = solution.longest_usable_seats(seats4)
    print("Input:", seats4)
    print("Output:", result4)
    print("Expected:", 1)
    print()

    # Single seat edge cases.
    seats5: List[int] = [1]
    result5: int = solution.longest_usable_seats(seats5)
    print("Input:", seats5)
    print("Output:", result5)
    print("Expected:", 1)
    print()

    seats6: List[int] = [0]
    result6: int = solution.longest_usable_seats(seats6)
    print("Input:", seats6)
    print("Output:", result6)
    print("Expected:", 1)