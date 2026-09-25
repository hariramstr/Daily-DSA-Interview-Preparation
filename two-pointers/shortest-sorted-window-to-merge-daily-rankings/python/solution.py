"""
Title: Shortest Sorted Window to Merge Daily Rankings

Problem Description:
A product team stores yesterday's leaderboard scores in a non-decreasing integer
array `yesterday` and today's newly processed score updates in another
non-decreasing integer array `today`. The two arrays are already sorted
individually, but the team wants to publish a single combined ranking stream
without fully merging both arrays.

Your task is to find the length of the shortest contiguous window in the virtual
merged array (the array that would result from merging `yesterday` and `today`
in sorted order) whose sum is at least `target`.

You are not allowed to explicitly build the merged array if you want an efficient
solution for large inputs. Instead, design an algorithm that uses the sorted
structure of both arrays and a two-pointer / sliding window strategy over the
virtual merge.

Return the minimum possible window length. If no contiguous window in the merged
order has sum at least `target`, return -1.

Notes:
- All values are positive integers, which guarantees that expanding the window
  increases or preserves its sum and shrinking it decreases or preserves its sum.
- The window must be contiguous in the merged sorted order, not separately inside
  one input array.
- The merged array is conceptual; an optimal solution should process elements as
  if they were being merged on the fly.

Constraints:
- 1 <= yesterday.length, today.length <= 10^5
- 1 <= yesterday[i], today[i] <= 10^4
- Both arrays are sorted in non-decreasing order
- 1 <= target <= 10^9
- Expected time complexity: O(n + m)
- Expected extra space: O(1) or O(log(n+m))
"""

from collections import deque
from typing import Deque, List, Tuple


class Solution:
    def _next_merged_value(
        self,
        yesterday: List[int],
        today: List[int],
        i: int,
        j: int,
    ) -> Tuple[int, int, int]:
        """
        Return the next value from the virtual merge of two sorted arrays.

        This helper simulates one step of the standard merge procedure used in
        merge sort. It chooses the smaller current element from the two arrays,
        returns that value, and advances the corresponding pointer.

        Args:
            yesterday: First sorted input array.
            today: Second sorted input array.
            i: Current pointer into `yesterday`.
            j: Current pointer into `today`.

        Returns:
            A tuple of:
            - next merged value
            - updated pointer for `yesterday`
            - updated pointer for `today`

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        if i < len(yesterday) and j < len(today):
            if yesterday[i] <= today[j]:
                return yesterday[i], i + 1, j
            return today[j], i, j + 1

        if i < len(yesterday):
            return yesterday[i], i + 1, j

        return today[j], i, j + 1

    def shortest_sorted_window(
        self,
        yesterday: List[int],
        today: List[int],
        target: int,
    ) -> int:
        """
        Find the minimum length of a contiguous window in the virtual merged
        sorted array whose sum is at least `target`.

        The algorithm does not explicitly build the merged array. Instead, it
        streams values one by one in merged order and applies a sliding window.
        Because all values are positive, once the current window sum reaches or
        exceeds the target, shrinking from the left is always safe and helps
        find the shortest valid window ending at the current position.

        Args:
            yesterday: Sorted list of yesterday's scores.
            today: Sorted list of today's scores.
            target: Required minimum window sum.

        Returns:
            The length of the shortest contiguous window in the virtual merged
            order with sum at least `target`, or -1 if no such window exists.

        Time complexity:
            O(n + m), where n = len(yesterday) and m = len(today)

        Space complexity:
            O(k), where k is the current window size.
            In the worst case this can be O(n + m) because we store the current
            window values in a deque while streaming the virtual merge.
        """
        # These two pointers represent our current read positions inside the two
        # sorted input arrays. Together, they let us simulate the merged order
        # without ever constructing the full merged array.
        i: int = 0
        j: int = 0

        # This deque stores the actual values currently inside the sliding window.
        # Why do we need it?
        # Because when we decide to shrink the window from the left, we must know
        # exactly which value is leaving so we can subtract it from the running sum.
        #
        # If we had built the merged array, we could index into it directly.
        # Since we are intentionally NOT building that array, the deque acts as a
        # lightweight representation of only the active window.
        window: Deque[int] = deque()

        # Running sum of all values currently inside `window`.
        current_sum: int = 0

        # Best answer found so far. We start with infinity-like behavior by using
        # a very large number. If it never changes, then no valid window exists.
        best_length: int = float("inf")

        # Total number of values that will appear in the virtual merged array.
        total_length: int = len(yesterday) + len(today)

        # We process exactly `total_length` merged elements, one at a time.
        # Each iteration appends one new element to the right side of the window.
        for _ in range(total_length):
            # Fetch the next value in sorted merged order and advance the correct
            # source pointer. This is the key step that lets us "walk" through the
            # conceptual merged array without materializing it.
            next_value, i, j = self._next_merged_value(yesterday, today, i, j)

            # Expand the sliding window to include this new rightmost element.
            window.append(next_value)
            current_sum += next_value

            # Because all numbers are positive:
            # - adding elements can only increase the sum
            # - removing elements can only decrease the sum
            #
            # Therefore, once current_sum >= target, the current window is valid,
            # and we should greedily shrink it from the left as much as possible
            # while it remains valid. This guarantees the shortest valid window
            # ending at the current right boundary.
            while current_sum >= target:
                # Update the best answer using the current valid window length.
                best_length = min(best_length, len(window))

                # Remove the leftmost element to see whether we can keep the sum
                # at least target with an even shorter window.
                left_value: int = window.popleft()
                current_sum -= left_value

        # If best_length was never updated, then no window reached the target sum.
        return -1 if best_length == float("inf") else best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    yesterday_1: List[int] = [1, 4, 7]
    today_1: List[int] = [2, 3, 8]
    target_1: int = 11
    result_1: int = solution.shortest_sorted_window(yesterday_1, today_1, target_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2
    yesterday_2: List[int] = [2, 2, 5]
    today_2: List[int] = [1, 6, 9]
    target_2: int = 15
    result_2: int = solution.shortest_sorted_window(yesterday_2, today_2, target_2)
    print("Example 2 Result:", result_2)  # Expected: 2

    # Additional quick checks
    yesterday_3: List[int] = [1, 1]
    today_3: List[int] = [1, 1]
    target_3: int = 10
    result_3: int = solution.shortest_sorted_window(yesterday_3, today_3, target_3)
    print("Additional Check 1 Result:", result_3)  # Expected: -1

    yesterday_4: List[int] = [5]
    today_4: List[int] = [6]
    target_4: int = 6
    result_4: int = solution.shortest_sorted_window(yesterday_4, today_4, target_4)
    print("Additional Check 2 Result:", result_4)  # Expected: 1