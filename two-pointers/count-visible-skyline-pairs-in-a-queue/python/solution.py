"""
Title: Count Visible Skyline Pairs in a Queue

Problem Description:
You are given an array heights of length n representing the heights of buildings
standing in a straight line from left to right. For every pair of indices (i, j)
with i < j, building i and building j are said to be mutually visible if every
building strictly between them has height less than or equal to
min(heights[i], heights[j]).

In other words, the shorter of the two endpoint buildings determines the maximum
allowed height of all buildings between them.

Your task is to return the total number of mutually visible pairs.

A brute-force solution that checks every pair and scans the buildings in between
is far too slow. The intended solution should exploit the structure of the array
using a hard two-pointers-style strategy or an equivalent linear-time boundary
compression idea.

Note:
- Adjacent buildings are always visible, because there are no buildings between them.
- Equal heights are allowed, and they can still see each other as long as all
  intermediate heights satisfy the visibility rule.

Constraints:
- 1 <= n <= 200000
- 1 <= heights[i] <= 1000000000
- Return the answer as a 64-bit integer, since the number of visible pairs may
  exceed 32-bit range.

Examples:
1) heights = [5, 3, 4, 2, 6]
   Output: 7

2) heights = [4, 4, 4]
   Output: 3
"""

from typing import List, Tuple


class Solution:
    def count_visible_pairs(self, heights: List[int]) -> int:
        """
        Count the total number of mutually visible building pairs.

        This method uses a monotonic decreasing stack with frequency compression.
        Each stack entry stores:
            (height, count_of_consecutive_same_height)

        Core idea:
        - Process buildings from left to right.
        - Maintain a stack of "active" heights that could still form visible pairs
          with future buildings.
        - When a new building arrives:
            1. Pop all shorter heights, because the current building can see all of them,
               and they will never matter again for future visibility checks.
            2. If the top has the same height, merge counts and count all visible pairs
               with that equal-height group.
            3. If after that there is still a taller building behind the equal group,
               the current building can also see exactly one such taller building.
            4. If the top is taller directly, the current building can see that one taller
               building.
            5. Push the current building/group back onto the stack.

        Args:
            heights: List of building heights.

        Returns:
            Total number of mutually visible pairs as an integer.

        Time complexity:
            O(n), because each building is pushed and popped at most once.

        Space complexity:
            O(n) in the worst case for the stack.
        """
        # This stack will store pairs of:
        #   (height, count_of_consecutive_same_height_collapsed_together)
        #
        # Why do we store counts?
        # Because if we have many equal heights in a row from the stack's perspective,
        # they behave similarly for visibility counting. Compressing them avoids
        # repeatedly storing identical values and makes equal-height handling clean.
        stack: List[Tuple[int, int]] = []

        # This will store the final answer.
        # Python integers automatically grow large enough, so this safely handles
        # the required 64-bit range.
        visible_pairs: int = 0

        # Process each building from left to right.
        for current_height in heights:
            # This variable will represent how many buildings of the current height
            # are being merged together if we encounter an equal-height group on top
            # of the stack.
            current_count: int = 1

            # ---------------------------------------------------------------
            # Step 1: Pop all strictly shorter buildings.
            # ---------------------------------------------------------------
            #
            # If the top of the stack is shorter than the current building,
            # then the current building can see every building in that top group.
            #
            # Why is that valid?
            # - Those shorter buildings are on the "frontier" of unresolved buildings.
            # - Since they are on top of the stack, there is no taller building between
            #   them and the current building that would block visibility.
            # - Therefore, each building in that shorter group forms a visible pair
            #   with the current building.
            #
            # After counting them, we pop them because:
            # - The current building is at least as useful as they are for future
            #   visibility checks.
            # - Any future building that could see those shorter buildings would also
            #   encounter the current building first, so those shorter buildings no
            #   longer need to remain on the stack.
            while stack and stack[-1][0] < current_height:
                shorter_height, shorter_count = stack.pop()
                visible_pairs += shorter_count

            # ---------------------------------------------------------------
            # Step 2: Handle equal-height buildings.
            # ---------------------------------------------------------------
            #
            # If the top of the stack has the same height as the current building,
            # then the current building can see all buildings in that equal-height group.
            #
            # Example:
            #   heights = [4, 4, 4]
            # When processing the third 4, it can see the previous equal-height group.
            #
            # Also, we merge the current building into that group by increasing
            # current_count.
            if stack and stack[-1][0] == current_height:
                equal_height, equal_count = stack.pop()

                # The current building can see every building in the equal-height group.
                visible_pairs += equal_count

                # Merge the current building into that equal-height block.
                current_count = equal_count + 1

                # Important subtlety:
                # If there is still a building left on the stack after removing the
                # equal-height group, then that remaining building must be taller.
                #
                # Why can the current building see exactly one such taller building?
                # Because the equal-height group does not block visibility:
                # all those heights are equal to current_height, so they are <=
                # min(current_height, taller_height) = current_height.
                #
                # The first taller building behind them is visible, but anything
                # farther behind that taller building is blocked by this taller one.
                if stack:
                    visible_pairs += 1

            # ---------------------------------------------------------------
            # Step 3: Handle the case where the top is strictly taller.
            # ---------------------------------------------------------------
            #
            # If the stack is not empty here, and we did not just process an equal-height
            # top, then the top must be taller than the current building.
            #
            # In that case, the current building can see exactly that nearest taller
            # building.
            #
            # Why exactly one?
            # - The nearest taller building is visible because all buildings between it
            #   and the current building have already been popped if they were shorter,
            #   and no equal-height case applies here.
            # - Any building farther left than that taller building is blocked by this
            #   nearest taller building.
            elif stack:
                visible_pairs += 1

            # ---------------------------------------------------------------
            # Step 4: Push the current building/group onto the stack.
            # ---------------------------------------------------------------
            #
            # We push the current height with its merged count.
            # This keeps the stack in decreasing-height order, with equal heights
            # compressed into one entry.
            stack.append((current_height, current_count))

        return visible_pairs


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [5, 3, 4, 2, 6]
    result_1: int = solution.count_visible_pairs(sample_1)
    print(f"heights = {sample_1}")
    print(f"visible pairs = {result_1}")
    print("expected = 7")
    print()

    sample_2: List[int] = [4, 4, 4]
    result_2: int = solution.count_visible_pairs(sample_2)
    print(f"heights = {sample_2}")
    print(f"visible pairs = {result_2}")
    print("expected = 3")
    print()

    sample_3: List[int] = [1]
    result_3: int = solution.count_visible_pairs(sample_3)
    print(f"heights = {sample_3}")
    print(f"visible pairs = {result_3}")
    print("expected = 0")
    print()

    sample_4: List[int] = [1, 2, 3, 4]
    result_4: int = solution.count_visible_pairs(sample_4)
    print(f"heights = {sample_4}")
    print(f"visible pairs = {result_4}")
    print("expected = 3")
    print()

    sample_5: List[int] = [4, 3, 2, 1]
    result_5: int = solution.count_visible_pairs(sample_5)
    print(f"heights = {sample_5}")
    print(f"visible pairs = {result_5}")
    print("expected = 3")