"""
Title: Maximum Consecutive Seats After One Reservation Move

Problem Description:
A theater keeps a row of seats represented by a binary array `seats`, where
`seats[i] = 1` means seat `i` is currently reserved and `seats[i] = 0` means
it is empty. To improve group seating, the manager is allowed to perform at
most one reservation move: choose one reserved seat and move that reservation
to any empty seat. After the move, the total number of reserved seats stays the
same.

Your task is to return the maximum possible length of a consecutive block of
reserved seats that can be formed.

You may also choose not to move any reservation if the current arrangement is
already optimal. The move is not a swap: one `1` is removed from its current
position and placed into one `0` position. This means a move can connect two
separated reserved blocks only if there is a gap structure that makes it
beneficial, and the answer depends on whether there is at least one extra
reserved seat elsewhere to relocate.

Return the largest number of consecutive `1`s obtainable after at most one move.

Constraints:
- 1 <= seats.length <= 2 * 10^5
- seats[i] is either 0 or 1
- The solution should run in linear time or close to it
"""

from typing import List


class Solution:
    def max_consecutive_seats_after_one_move(self, seats: List[int]) -> int:
        """
        Compute the maximum length of consecutive reserved seats obtainable
        after performing at most one move of a reservation.

        The key idea:
        - First, identify the length of consecutive 1s ending at each index
          and starting at each index.
        - Then, for every zero position, imagine placing a moved reservation
          there.
        - If that zero sits between two blocks of 1s, we can potentially join
          them.
        - However, we are only allowed to move an existing reservation, so
          joining both sides fully is only possible if there exists at least
          one reserved seat outside those joined blocks to donate.
        - Otherwise, we can still create a block, but its size is limited
          because the moved reservation must come from one of the blocks we are
          trying to preserve.

        Args:
            seats: Binary array where 1 means reserved and 0 means empty.

        Returns:
            The maximum possible length of a consecutive block of 1s after at
            most one valid move.

        Time complexity:
            O(n), where n is the length of seats.

        Space complexity:
            O(n), for the prefix/suffix consecutive-one arrays.
        """
        n: int = len(seats)

        # Count how many reserved seats exist in total.
        # This is extremely important because after any move, the total number
        # of 1s never changes. Therefore, no answer can ever exceed total_ones.
        total_ones: int = sum(seats)

        # Edge case:
        # If there are no reserved seats, we cannot create any reserved block.
        if total_ones == 0:
            return 0

        # Edge case:
        # If every seat is already reserved, the entire row is one consecutive
        # block, and there is nowhere to move a reservation anyway.
        if total_ones == n:
            return n

        # left[i] will store:
        # "How many consecutive 1s end exactly at index i?"
        #
        # Example:
        # seats = [1, 1, 0, 1]
        # left  = [1, 2, 0, 1]
        #
        # This helps us quickly know the size of the 1-block immediately to the
        # left of a zero.
        left: List[int] = [0] * n

        # Build the left array in one pass from left to right.
        for i in range(n):
            if seats[i] == 1:
                # If current seat is reserved, then the consecutive block ending
                # here is one more than the block ending at the previous index.
                left[i] = 1 + (left[i - 1] if i > 0 else 0)
            else:
                # If current seat is empty, no consecutive 1-block ends here.
                left[i] = 0

        # right[i] will store:
        # "How many consecutive 1s start exactly at index i?"
        #
        # Example:
        # seats = [1, 1, 0, 1]
        # right = [2, 1, 0, 1]
        #
        # This helps us quickly know the size of the 1-block immediately to the
        # right of a zero.
        right: List[int] = [0] * n

        # Build the right array in one pass from right to left.
        for i in range(n - 1, -1, -1):
            if seats[i] == 1:
                # If current seat is reserved, then the consecutive block
                # starting here is one more than the block starting at the next
                # index.
                right[i] = 1 + (right[i + 1] if i + 1 < n else 0)
            else:
                # If current seat is empty, no consecutive 1-block starts here.
                right[i] = 0

        # Start with the best block already present without any move.
        # This satisfies the "at most one move" requirement because we are
        # allowed to choose not to move.
        answer: int = max(left)

        # Now examine every zero position as a candidate destination for the
        # moved reservation.
        for i in range(n):
            if seats[i] == 1:
                # We only care about empty seats because the moved reservation
                # must be placed into a zero.
                continue

            # Determine the size of the consecutive 1-block immediately to the
            # left of this zero.
            left_block: int = left[i - 1] if i > 0 else 0

            # Determine the size of the consecutive 1-block immediately to the
            # right of this zero.
            right_block: int = right[i + 1] if i + 1 < n else 0

            # If we place a 1 into this zero, then in the most optimistic view
            # we would create:
            # left_block + 1 + right_block
            #
            # But that assumes we can source the moved reservation from
            # somewhere else without damaging these two blocks.
            merged_size: int = left_block + 1 + right_block

            # The number of 1s already used by the left and right neighboring
            # blocks is left_block + right_block.
            #
            # If total_ones > left_block + right_block, then there exists at
            # least one reserved seat somewhere outside these two blocks.
            # That outside seat can be moved into this zero, allowing us to
            # preserve both neighboring blocks and fully realize merged_size.
            #
            # Otherwise, every reserved seat belongs to those neighboring
            # blocks. In that case, to place a 1 here we must steal one from
            # one of those blocks, so we cannot increase the combined size by 1.
            # The best achievable size then becomes left_block + right_block.
            if total_ones > left_block + right_block:
                candidate: int = merged_size
            else:
                candidate = left_block + right_block

            # Also, no consecutive block can exceed total_ones because the total
            # number of reserved seats is fixed.
            candidate = min(candidate, total_ones)

            # Update the global best answer.
            answer = max(answer, candidate)

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [1, 1, 0, 1, 0, 1, 1, 1],
        [1, 0, 1, 1, 0, 1],
        [1],
        [0],
        [1, 1, 1, 1],
        [1, 0, 1, 0, 1],
        [0, 0, 0, 1, 1, 0, 1],
    ]

    for seats in sample_inputs:
        result = solution.max_consecutive_seats_after_one_move(seats)
        print(f"seats = {seats}")
        print(f"maximum consecutive reserved seats after at most one move = {result}")
        print("-" * 60)