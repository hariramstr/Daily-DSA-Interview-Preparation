"""
Title: Maximum Visible Booths Between Taller Buildings

Problem Description:
You are given an integer array heights where heights[i] represents the height of the i-th
building in a straight line. A pop-up booth can be placed on top of any building i.
From building i, the booth is considered enclosed if there exists at least one taller
building on its left and at least one taller building on its right.

The visibility score of building i is the number of consecutive buildings strictly between
the nearest taller building on the left and the nearest taller building on the right.

More formally:
- Let L be the closest index less than i such that heights[L] > heights[i]
- Let R be the closest index greater than i such that heights[R] > heights[i]

If both L and R exist, then the score for i is:
    R - L - 1

If either taller building does not exist, the score is 0.

Your task is to return the maximum visibility score among all buildings.

Important note:
- Only strictly greater heights count as taller.
- Equal heights do not qualify.

Examples:
1) heights = [5, 2, 4, 3, 6]
   Nearest greater boundaries:
   - i = 1 (height 2): L = 0, R = 2, score = 2 - 0 - 1 = 1
   - i = 2 (height 4): L = 0, R = 4, score = 4 - 0 - 1 = 3
   - i = 3 (height 3): L = 2, R = 4, score = 4 - 2 - 1 = 1
   Maximum score = 3

2) heights = [7, 1, 5, 2, 4, 8]
   Nearest greater boundaries:
   - i = 1 (height 1): L = 0, R = 2, score = 1
   - i = 2 (height 5): L = 0, R = 5, score = 5 - 0 - 1 = 4
   - i = 3 (height 2): L = 2, R = 4, score = 1
   - i = 4 (height 4): L = 2, R = 5, score = 2
   Maximum score = 4

Constraints:
- 1 <= heights.length <= 200000
- 1 <= heights[i] <= 1000000000
"""

from typing import List


class Solution:
    def _nearest_greater_to_left(self, heights: List[int]) -> List[int]:
        """
        Compute the index of the nearest strictly greater building to the left for each position.

        Args:
            heights: List of building heights.

        Returns:
            A list left where left[i] is the index of the nearest building to the left
            of i with height strictly greater than heights[i], or -1 if none exists.

        Time complexity:
            O(n), where n is the number of buildings.

        Space complexity:
            O(n), for the result list and the monotonic stack.
        """
        n: int = len(heights)

        # This result array will store, for every index i, the nearest index on the left
        # whose height is strictly greater than heights[i].
        left: List[int] = [-1] * n

        # We use a stack of indices.
        #
        # The key idea:
        # - We want the nearest greater element on the left.
        # - While scanning from left to right, we maintain a stack such that heights at
        #   those indices are in strictly decreasing order from bottom to top.
        #
        # Why does this help?
        # - If the current height is greater than or equal to the height at the top of the stack,
        #   then that top index can never be the nearest strictly greater element for the current
        #   index or for future indices that are at least as tall, so we remove it.
        # - After removing all heights <= current height, the top of the stack (if any)
        #   is the nearest index to the left with height > current height.
        stack: List[int] = []

        for i, current_height in enumerate(heights):
            # Remove all indices whose heights are less than or equal to the current height.
            #
            # Why "<="? Because equal height is NOT considered taller according to the problem.
            # Therefore, equal-height buildings cannot serve as enclosing boundaries.
            while stack and heights[stack[-1]] <= current_height:
                stack.pop()

            # If the stack is not empty now, the top is the nearest strictly greater building
            # on the left.
            if stack:
                left[i] = stack[-1]

            # Push the current index onto the stack so it may serve as a candidate
            # for future buildings to the right.
            stack.append(i)

        return left

    def _nearest_greater_to_right(self, heights: List[int]) -> List[int]:
        """
        Compute the index of the nearest strictly greater building to the right for each position.

        Args:
            heights: List of building heights.

        Returns:
            A list right where right[i] is the index of the nearest building to the right
            of i with height strictly greater than heights[i], or -1 if none exists.

        Time complexity:
            O(n), where n is the number of buildings.

        Space complexity:
            O(n), for the result list and the monotonic stack.
        """
        n: int = len(heights)

        # This result array will store, for every index i, the nearest index on the right
        # whose height is strictly greater than heights[i].
        right: List[int] = [-1] * n

        # Again we use a monotonic stack of indices.
        #
        # This time we scan from right to left so that the stack represents candidate
        # "greater elements to the right".
        stack: List[int] = []

        for i in range(n - 1, -1, -1):
            current_height: int = heights[i]

            # Remove all indices whose heights are less than or equal to the current height,
            # because they are not strictly greater and therefore cannot be valid boundaries.
            while stack and heights[stack[-1]] <= current_height:
                stack.pop()

            # If anything remains, the top is the nearest strictly greater building on the right.
            if stack:
                right[i] = stack[-1]

            # Push current index so it can be considered for buildings further to the left.
            stack.append(i)

        return right

    def max_visibility_score(self, heights: List[int]) -> int:
        """
        Return the maximum visibility score among all buildings.

        The score for building i is:
            R - L - 1
        where:
            L = nearest index to the left with height > heights[i]
            R = nearest index to the right with height > heights[i]

        If either boundary does not exist, the score for that building is 0.

        Args:
            heights: List of building heights.

        Returns:
            The maximum visibility score.

        Time complexity:
            O(n), because each building is pushed and popped at most once in each stack pass.

        Space complexity:
            O(n), for the left and right arrays and the stacks.
        """
        n: int = len(heights)

        # Edge case:
        # If there are fewer than 3 buildings, no building can possibly have both a left
        # and a right neighbor boundary, so the answer must be 0.
        if n < 3:
            return 0

        # Step 1: Find nearest strictly greater building on the left for every index.
        left: List[int] = self._nearest_greater_to_left(heights)

        # Step 2: Find nearest strictly greater building on the right for every index.
        right: List[int] = self._nearest_greater_to_right(heights)

        # Step 3: Evaluate the score for each building and keep the maximum.
        max_score: int = 0

        for i in range(n):
            # A building is enclosed only if both boundaries exist.
            if left[i] != -1 and right[i] != -1:
                # The score is the number of indices strictly between the two boundaries.
                score: int = right[i] - left[i] - 1

                # Update the best answer seen so far.
                if score > max_score:
                    max_score = score

        return max_score


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [5, 2, 4, 3, 6],     # Expected: 3
        [7, 1, 5, 2, 4, 8],  # Expected: 4
        [1],                 # Expected: 0
        [3, 2, 1],           # Expected: 0
        [5, 1, 5],           # Expected: 0 because equal height is not taller
        [9, 3, 7, 2, 8, 1, 10],  # Additional test
    ]

    for heights in sample_inputs:
        result: int = solution.max_visibility_score(heights)
        print(f"heights = {heights}")
        print(f"maximum visibility score = {result}")
        print("-" * 50)