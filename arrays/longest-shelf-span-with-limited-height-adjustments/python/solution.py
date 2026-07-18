"""
Title: Longest Shelf Span With Limited Height Adjustments

Problem Description:
A warehouse stores products on a long row of shelves. The height of the product stack on
shelf i is given by heights[i]. For a promotional display, the manager wants to choose
one contiguous span of shelves and make all stack heights in that span equal.

You are allowed to increase shelf heights, but you cannot decrease any shelf.
Increasing a shelf by 1 unit costs 1 adjustment.

Given an integer array heights and an integer budget, return the maximum length of a
contiguous subarray that can be made to have the same height using at most budget total
adjustments.

When choosing a span, you may raise shorter shelves to match the tallest shelf already
inside that span. Because decreasing is not allowed, the final common height must be at
least the maximum value in the chosen subarray, and choosing the current maximum is
always optimal.

Constraints:
- 1 <= heights.length <= 100000
- 1 <= heights[i] <= 1000000000
- 0 <= budget <= 100000000000000
"""

from collections import deque
from typing import Deque, List


class Solution:
    def max_equal_shelf_span(self, heights: List[int], budget: int) -> int:
        """
        Compute the maximum length of a contiguous subarray that can be made equal
        by only increasing values, with total increase cost at most budget.

        Args:
            heights: List of shelf heights.
            budget: Maximum total allowed increase cost.

        Returns:
            The largest valid contiguous span length.

        Time complexity:
            O(n), where n is the length of heights.
            Each index is added to and removed from the deque at most once.

        Space complexity:
            O(n) in the worst case for the monotonic deque and prefix sums.
        """
        n: int = len(heights)

        # Prefix sums let us quickly compute the sum of any window [left, right].
        # prefix[i] stores the sum of heights[0:i].
        # Then:
        # sum(heights[left:right+1]) = prefix[right+1] - prefix[left]
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + heights[i]

        # This deque will store indices of elements in decreasing height order.
        #
        # Why do we need it?
        # For any current window [left, right], we must know the maximum height
        # inside that window, because the cheapest valid target height is exactly
        # that maximum.
        #
        # A monotonic decreasing deque gives:
        # - front of deque = index of maximum element in current window
        # - O(1) access to current maximum
        # - O(1) amortized updates when expanding/shrinking the window
        max_deque: Deque[int] = deque()

        left: int = 0
        best: int = 0

        # We use a sliding window over the array.
        # right expands the window one step at a time.
        for right in range(n):
            # Maintain the deque in decreasing order of heights.
            #
            # While the new height is greater than or equal to the height at the
            # back of the deque, those smaller/equal elements can never become the
            # maximum for any future window that includes the new element.
            while max_deque and heights[max_deque[-1]] <= heights[right]:
                max_deque.pop()

            max_deque.append(right)

            # Now repeatedly shrink from the left while the current window is too expensive.
            #
            # Cost to make all values in window [left, right] equal to max_height:
            #   max_height * window_length - window_sum
            #
            # This works because:
            # - every element must be increased up to max_height
            # - total required increase is the difference between the target total
            #   and the current total
            while left <= right:
                # Remove indices from the front if they are no longer inside the window.
                while max_deque and max_deque[0] < left:
                    max_deque.popleft()

                max_height: int = heights[max_deque[0]]
                window_length: int = right - left + 1
                window_sum: int = prefix[right + 1] - prefix[left]
                cost: int = max_height * window_length - window_sum

                # If the window is affordable, we stop shrinking.
                if cost <= budget:
                    break

                # Otherwise move left forward by one position and try again.
                left += 1

            # At this point, [left, right] is the longest valid window ending at right.
            best = max(best, right - left + 1)

        return best

    def solve(self, heights: List[int], budget: int) -> int:
        """
        Wrapper method for the main algorithm.

        Args:
            heights: List of shelf heights.
            budget: Maximum total allowed increase cost.

        Returns:
            Maximum valid contiguous span length.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.max_equal_shelf_span(heights, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # The prompt's written explanation is inconsistent, but the correct contiguous answer is 3.
    # Valid length-3 spans include:
    # - [3, 1, 2] -> raise to 3, cost = 0 + 2 + 1 = 3
    # - [1, 2, 2] -> raise to 2, cost = 1 + 0 + 0 = 1
    heights1: List[int] = [3, 1, 2, 2, 4]
    budget1: int = 3
    print(solution.solve(heights1, budget1))  # Correct result: 3

    # Example 2:
    heights2: List[int] = [5, 5, 5, 5]
    budget2: int = 0
    print(solution.solve(heights2, budget2))  # Expected: 4

    # Additional quick checks
    heights3: List[int] = [1]
    budget3: int = 10
    print(solution.solve(heights3, budget3))  # Expected: 1

    heights4: List[int] = [1, 2, 3, 4]
    budget4: int = 6
    print(solution.solve(heights4, budget4))  # Expected: 4

    heights5: List[int] = [4, 1, 1, 1]
    budget5: int = 3
    print(solution.solve(heights5, budget5))  # Expected: 3