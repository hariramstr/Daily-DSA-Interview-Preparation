"""
Title: Maximum Points from Skipping Adjacent Museum Rooms

Problem Description:
A museum curator is planning a guided tour through a straight hallway of exhibit rooms.
Each room has a popularity score, given in an integer array `rooms`, where `rooms[i]`
is the number of visitor points earned if room `i` is included in the tour.

To avoid crowding and noise overlap, the curator cannot include two adjacent rooms in
the same tour.

Your task is to return the maximum total visitor points the curator can earn by choosing
a subset of rooms such that no two chosen rooms are next to each other.

You may choose to skip any room, and it is also valid to choose no rooms at all if every
score is negative or zero. In other words, the answer should never be less than `0`.

This is an interview-style dynamic programming problem. A correct solution should
efficiently decide, for each position, whether it is better to include the current room
and skip the previous one, or skip the current room and keep the best answer seen so far.

Constraints:
- 1 <= rooms.length <= 100000
- -1000 <= rooms[i] <= 1000

Example 1:
Input: rooms = [4, 2, 7, 9, 3]
Output: 13
Explanation: Choose rooms with scores 4, 9, and 3 is not allowed because 9 and 3 are
adjacent. The best valid choice is 4 + 9 = 13.

Example 2:
Input: rooms = [5, 1, 1, 5]
Output: 10
Explanation: Choose the first and last rooms for a total of 10. They are not adjacent,
so this is valid.
"""

from typing import List


class Solution:
    def max_points(self, rooms: List[int]) -> int:
        """
        Compute the maximum total points obtainable by selecting non-adjacent rooms.

        The algorithm uses dynamic programming with constant extra space.
        At each room, we decide between:
        1. Skipping the current room and keeping the best total so far
        2. Taking the current room and adding its value to the best total from
           two positions back

        Because choosing no rooms is allowed, the result is never less than 0.

        Args:
            rooms: A list of integers where rooms[i] is the score of room i.

        Returns:
            The maximum total score obtainable without choosing adjacent rooms.

        Time complexity:
            O(n), where n is the number of rooms.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # `prev_two` represents the best answer for the subarray ending at index i - 2.
        # In other words, before processing the current room, this variable stores:
        # "What is the maximum score we could have earned up to two rooms ago?"
        #
        # We initialize it to 0 because:
        # - It is valid to choose no rooms at all.
        # - This also naturally handles negative values, since taking a negative room
        #   should never be better than taking nothing.
        prev_two: int = 0

        # `prev_one` represents the best answer for the subarray ending at index i - 1.
        # This means:
        # "What is the maximum score we could have earned up to the previous room?"
        #
        # It also starts at 0 for the same reason: choosing no rooms is allowed.
        prev_one: int = 0

        # We now process each room from left to right.
        # This left-to-right order works because the decision for the current room
        # depends only on already-computed best answers from earlier positions.
        for points in rooms:
            # Option 1: Skip the current room.
            #
            # If we skip this room, then the best total remains exactly what it was
            # after processing the previous room.
            skip_current: int = prev_one

            # Option 2: Take the current room.
            #
            # If we include the current room, we are NOT allowed to include the
            # immediately previous room because adjacent rooms cannot both be chosen.
            #
            # Therefore, the best compatible total is `prev_two`, which is the best
            # answer up to two rooms back. We add the current room's points to that.
            take_current: int = prev_two + points

            # The best answer after considering this room is the better of:
            # - skipping it
            # - taking it
            #
            # We also compare against 0 to guarantee the answer never becomes negative.
            # This is important when all room values are negative or zero.
            current_best: int = max(0, skip_current, take_current)

            # Shift the window forward for the next iteration:
            #
            # - The old `prev_one` becomes the new `prev_two`
            # - The newly computed `current_best` becomes the new `prev_one`
            #
            # After this update:
            # - `prev_two` will mean "best up to i - 1" on the next loop's perspective
            # - `prev_one` will mean "best up to i"
            prev_two = prev_one
            prev_one = current_best

        # After processing all rooms, `prev_one` contains the best answer for the
        # entire array.
        return prev_one

    def maxPoints(self, rooms: List[int]) -> int:
        """
        Wrapper method using camelCase naming for compatibility with common interview styles.

        Args:
            rooms: A list of integers where rooms[i] is the score of room i.

        Returns:
            The maximum total score obtainable without choosing adjacent rooms.

        Time complexity:
            O(n), where n is the number of rooms.

        Space complexity:
            O(1), because only a few variables are used.
        """
        return self.max_points(rooms)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    rooms1: List[int] = [4, 2, 7, 9, 3]
    result1: int = solution.max_points(rooms1)
    print(f"Input: {rooms1}")
    print(f"Output: {result1}")
    print("Expected: 13")
    print()

    # Example 2 from the problem statement
    rooms2: List[int] = [5, 1, 1, 5]
    result2: int = solution.max_points(rooms2)
    print(f"Input: {rooms2}")
    print(f"Output: {result2}")
    print("Expected: 10")
    print()

    # Additional example: all negative values, so choosing no rooms is best
    rooms3: List[int] = [-5, -1, -8]
    result3: int = solution.max_points(rooms3)
    print(f"Input: {rooms3}")
    print(f"Output: {result3}")
    print("Expected: 0")
    print()

    # Additional example: mixed values including zero and negatives
    rooms4: List[int] = [2, -1, 3, -4, 5]
    result4: int = solution.max_points(rooms4)
    print(f"Input: {rooms4}")
    print(f"Output: {result4}")
    print("Expected: 10")