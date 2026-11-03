"""
Title: Longest Gaming Session With Limited Rating Swings

Problem Description:
You are given an array ratings where ratings[i] is the skill rating of the opponent
faced in the i-th consecutive match of a gaming session. A session segment is
considered stable if the difference between the highest and lowest opponent ratings
inside that contiguous segment is at most limit.

Your task is to return the length of the longest stable contiguous segment of matches.

In other words, find the maximum size of a subarray ratings[l..r] such that
max(ratings[l..r]) - min(ratings[l..r]) <= limit.

This models a real matchmaking analysis problem: a player is said to be in a
consistent queue environment only when the opponent difficulty does not swing too
much over a stretch of consecutive games.

You should design an algorithm efficient enough for large inputs. A brute-force
approach that checks every subarray will be too slow.

Constraints:
- 1 <= ratings.length <= 200000
- 1 <= ratings[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: ratings = [1200, 1215, 1190, 1220, 1265, 1230], limit = 40
Output: 4

Example 2:
Input: ratings = [1500, 1500, 1500, 1510, 1498, 1499], limit = 12
Output: 6

Return only the maximum length of such a contiguous segment.
"""

from collections import deque
from typing import Deque, List


class Solution:
    def longest_stable_segment(self, ratings: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous subarray where the difference
        between the maximum and minimum values is at most limit.

        Args:
            ratings: List of opponent ratings in consecutive matches.
            limit: Maximum allowed difference between highest and lowest rating
                inside a stable segment.

        Returns:
            The maximum length of a stable contiguous segment.

        Time Complexity:
            O(n), where n is the length of ratings.
            Each element is added to and removed from each deque at most once.

        Space Complexity:
            O(n) in the worst case for the deques.
        """
        # We use the classic "sliding window" technique:
        #
        # - The right pointer expands the current window one element at a time.
        # - The left pointer shrinks the window only when it becomes invalid.
        #
        # The challenge is checking the current window's minimum and maximum quickly.
        # If we recomputed min and max from scratch for every window, that would be too slow.
        #
        # To solve that efficiently, we maintain:
        # 1) A monotonic increasing deque for minimum values
        # 2) A monotonic decreasing deque for maximum values
        #
        # These deques store indices, not values directly, because:
        # - We need to know whether an element has moved out of the window
        # - Indices let us compare against the left boundary easily

        # Deque that keeps indices of elements in increasing order of ratings value.
        # The front always points to the minimum value in the current window.
        min_deque: Deque[int] = deque()

        # Deque that keeps indices of elements in decreasing order of ratings value.
        # The front always points to the maximum value in the current window.
        max_deque: Deque[int] = deque()

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far.
        best_length: int = 0

        # Move the right boundary across the array one step at a time.
        for right, current_rating in enumerate(ratings):
            # ---------------------------------------------------------------
            # Step 1: Insert the new element into the min deque
            # ---------------------------------------------------------------
            #
            # We want min_deque to remain increasing by value.
            # That means while the last element in the deque has a value
            # greater than the current value, it can never become the minimum
            # for this or any future window that includes current_rating.
            #
            # Why can we remove it?
            # Because current_rating is smaller and also appears later,
            # so the older larger value is strictly worse for minimum tracking.
            while min_deque and ratings[min_deque[-1]] > current_rating:
                min_deque.pop()

            # Add the current index after removing all worse candidates.
            min_deque.append(right)

            # ---------------------------------------------------------------
            # Step 2: Insert the new element into the max deque
            # ---------------------------------------------------------------
            #
            # We want max_deque to remain decreasing by value.
            # That means while the last element in the deque has a value
            # smaller than the current value, it can never become the maximum
            # for this or any future window that includes current_rating.
            #
            # Why can we remove it?
            # Because current_rating is larger and appears later,
            # so the older smaller value is strictly worse for maximum tracking.
            while max_deque and ratings[max_deque[-1]] < current_rating:
                max_deque.pop()

            # Add the current index after removing all worse candidates.
            max_deque.append(right)

            # ---------------------------------------------------------------
            # Step 3: Shrink the window while it is invalid
            # ---------------------------------------------------------------
            #
            # The current window is ratings[left:right+1].
            # It is valid only if:
            #   max(window) - min(window) <= limit
            #
            # Thanks to the deques:
            # - ratings[min_deque[0]] is the minimum in the window
            # - ratings[max_deque[0]] is the maximum in the window
            #
            # If the window is invalid, we move left forward until it becomes valid again.
            while ratings[max_deque[0]] - ratings[min_deque[0]] > limit:
                # If the element leaving the window is currently the minimum,
                # remove it from the front of min_deque.
                if min_deque[0] == left:
                    min_deque.popleft()

                # If the element leaving the window is currently the maximum,
                # remove it from the front of max_deque.
                if max_deque[0] == left:
                    max_deque.popleft()

                # Actually move the left boundary of the window forward.
                left += 1

            # ---------------------------------------------------------------
            # Step 4: Update the best valid window length
            # ---------------------------------------------------------------
            #
            # At this point, the window is guaranteed to be valid.
            # Its length is right - left + 1.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        return best_length

    def longestSubarray(self, ratings: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview/online-judge method name.

        Args:
            ratings: List of opponent ratings in consecutive matches.
            limit: Maximum allowed difference between highest and lowest rating
                inside a stable segment.

        Returns:
            The maximum length of a stable contiguous segment.

        Time Complexity:
            O(n), where n is the length of ratings.

        Space Complexity:
            O(n) in the worst case.
        """
        return self.longest_stable_segment(ratings, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    ratings1: List[int] = [1200, 1215, 1190, 1220, 1265, 1230]
    limit1: int = 40
    result1: int = solution.longest_stable_segment(ratings1, limit1)
    print(result1)  # Expected: 4

    # Example 2
    ratings2: List[int] = [1500, 1500, 1500, 1510, 1498, 1499]
    limit2: int = 12
    result2: int = solution.longest_stable_segment(ratings2, limit2)
    print(result2)  # Expected: 6

    # Additional quick checks
    ratings3: List[int] = [5]
    limit3: int = 0
    result3: int = solution.longest_stable_segment(ratings3, limit3)
    print(result3)  # Expected: 1

    ratings4: List[int] = [8, 2, 4, 7]
    limit4: int = 4
    result4: int = solution.longest_stable_segment(ratings4, limit4)
    print(result4)  # Expected: 2