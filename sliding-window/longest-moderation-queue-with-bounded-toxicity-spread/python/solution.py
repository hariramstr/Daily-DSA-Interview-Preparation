"""
Title: Longest Moderation Queue With Bounded Toxicity Spread

Problem Description:
A social platform stores the toxicity score of each newly posted comment in chronological order.
You are given an integer array scores, where scores[i] is the toxicity score of the i-th comment,
and an integer limit.

A contiguous block of comments is considered reviewable if the difference between the maximum
toxicity score and the minimum toxicity score inside that block is at most limit.

Your task is to return the length of the longest reviewable contiguous block.

Formally, find the maximum value of (r - l + 1) such that for some 0 <= l <= r < scores.length:
max(scores[l..r]) - min(scores[l..r]) <= limit.

Constraints:
- 1 <= scores.length <= 200000
- 0 <= scores[i] <= 1000000000
- 0 <= limit <= 1000000000

Examples:
1)
Input: scores = [4, 7, 5, 6, 8, 3, 4], limit = 3
Output: 4

2)
Input: scores = [10, 10, 10, 1, 2, 3, 4], limit = 2
Correct Output: 3

Important note:
The written explanation in the prompt for Example 2 contains a contradiction:
it first says output is 4, but then correctly explains that [1, 2, 3, 4] is invalid
because 4 - 1 = 3 > 2, and concludes the valid longest length is 3.
Therefore, the correct answer for Example 2 is 3.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_reviewable_block(self, scores: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray whose maximum value minus
        minimum value is at most the given limit.

        This uses a sliding window plus two monotonic deques:
        - One deque keeps candidates for the maximum in decreasing order.
        - One deque keeps candidates for the minimum in increasing order.

        Args:
            scores: List of toxicity scores in chronological order.
            limit: Maximum allowed spread (max - min) inside a valid window.

        Returns:
            The maximum length of a valid contiguous block.

        Time complexity:
            O(n), because each element is added to and removed from each deque at most once.

        Space complexity:
            O(n) in the worst case for the deques, though typically less.
        """
        # These deques will store indices, not values.
        #
        # Why store indices instead of raw values?
        # Because when the left side of the sliding window moves forward,
        # we need to know whether the current min/max candidate has fallen out
        # of the window. Indices let us check that directly.
        #
        # max_deque:
        #   Maintains indices of elements in decreasing order of scores.
        #   The front always points to the maximum value in the current window.
        #
        # min_deque:
        #   Maintains indices of elements in increasing order of scores.
        #   The front always points to the minimum value in the current window.
        max_deque: Deque[int] = deque()
        min_deque: Deque[int] = deque()

        # left is the start of the current sliding window.
        left: int = 0

        # best stores the longest valid window length found so far.
        best: int = 0

        # We expand the window by moving right from left to right across the array.
        for right, value in enumerate(scores):
            # ------------------------------------------------------------
            # Step 1: Insert the new element into the max deque.
            # ------------------------------------------------------------
            # We want max_deque to remain decreasing by score value.
            #
            # If the new value is greater than elements at the back,
            # those smaller elements can never become the maximum for any
            # future window that includes this new value, because:
            # - they are older (further left),
            # - and they are smaller.
            #
            # So we remove them from the back.
            while max_deque and scores[max_deque[-1]] < value:
                max_deque.pop()

            # Add the current index to the back.
            max_deque.append(right)

            # ------------------------------------------------------------
            # Step 2: Insert the new element into the min deque.
            # ------------------------------------------------------------
            # We want min_deque to remain increasing by score value.
            #
            # If the new value is smaller than elements at the back,
            # those larger elements can never become the minimum for any
            # future window that includes this new value, because:
            # - they are older,
            # - and they are larger.
            #
            # So we remove them from the back.
            while min_deque and scores[min_deque[-1]] > value:
                min_deque.pop()

            # Add the current index to the back.
            min_deque.append(right)

            # ------------------------------------------------------------
            # Step 3: Shrink the window while it is invalid.
            # ------------------------------------------------------------
            # The current maximum is at scores[max_deque[0]].
            # The current minimum is at scores[min_deque[0]].
            #
            # If their difference is greater than limit, the window is invalid,
            # so we must move left forward until it becomes valid again.
            while scores[max_deque[0]] - scores[min_deque[0]] > limit:
                # If the leftmost index in the window is exactly the index
                # at the front of max_deque, then that maximum is leaving
                # the window, so we remove it.
                if max_deque[0] == left:
                    max_deque.popleft()

                # Similarly, if the leftmost index is the current minimum,
                # remove it from min_deque because it is leaving the window.
                if min_deque[0] == left:
                    min_deque.popleft()

                # Move the left boundary one step to the right.
                left += 1

            # ------------------------------------------------------------
            # Step 4: Update the best answer.
            # ------------------------------------------------------------
            # At this point, the window [left, right] is guaranteed valid.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def longestSubarray(self, scores: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            scores: List of toxicity scores.
            limit: Maximum allowed difference between max and min in a window.

        Returns:
            Length of the longest valid contiguous subarray.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.longest_reviewable_block(scores, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    scores1: List[int] = [4, 7, 5, 6, 8, 3, 4]
    limit1: int = 3
    result1: int = solution.longest_reviewable_block(scores1, limit1)
    print("Example 1 result:", result1)  # Expected: 4

    # Example 2 from the prompt.
    # The prompt contains a contradictory output of 4, but its own explanation
    # correctly shows the answer must be 3.
    scores2: List[int] = [10, 10, 10, 1, 2, 3, 4]
    limit2: int = 2
    result2: int = solution.longest_reviewable_block(scores2, limit2)
    print("Example 2 result:", result2)  # Expected: 3

    # Additional small sanity checks.
    scores3: List[int] = [8]
    limit3: int = 0
    result3: int = solution.longest_reviewable_block(scores3, limit3)
    print("Single element result:", result3)  # Expected: 1

    scores4: List[int] = [1, 2, 3, 4, 5]
    limit4: int = 4
    result4: int = solution.longest_reviewable_block(scores4, limit4)
    print("Whole array valid result:", result4)  # Expected: 5

    scores5: List[int] = [1, 100, 1, 100]
    limit5: int = 0
    result5: int = solution.longest_reviewable_block(scores5, limit5)
    print("Only equal values allowed result:", result5)  # Expected: 1