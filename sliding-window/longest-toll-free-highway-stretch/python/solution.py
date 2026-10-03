"""
Title: Longest Toll-Free Highway Stretch

Problem Description:
You are given an array `costs` where `costs[i]` is the toll fee charged at the
`i`-th highway checkpoint on a road trip. A traveler wants to drive through one
contiguous stretch of checkpoints while spending at most `budget` total toll money.

Return the length of the longest contiguous subarray of `costs` whose sum is less
than or equal to `budget`.

This is a classic sliding window problem. Because all toll costs are non-negative,
we can safely expand the right side of the window and, whenever the running total
becomes too large, move the left side forward until the window becomes valid again.

Constraints:
- 1 <= costs.length <= 100000
- 0 <= costs[i] <= 10000
- 0 <= budget <= 1000000000
- The answer is the number of checkpoints in the longest valid contiguous stretch.

Example 1:
Input: costs = [4, 2, 1, 3, 2], budget = 6
Output: 3
Explanation: The longest valid stretch is [2, 1, 3] with total cost 6.

Example 2:
Input: costs = [1, 1, 1, 1, 1], budget = 3
Output: 3
Explanation: Any 3 consecutive checkpoints cost 3, which fits the budget.
"""


from typing import List


class Solution:
    def longest_toll_free_stretch(self, costs: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most budget.

        Args:
            costs: A list of non-negative integers representing toll costs.
            budget: The maximum allowed sum for a valid contiguous stretch.

        Returns:
            The length of the longest contiguous subarray with sum <= budget.

        Time complexity:
            O(n), where n is the length of costs, because each index is visited
            at most twice: once by the right pointer and once by the left pointer.

        Space complexity:
            O(1), because only a few extra variables are used.
        """
        # `left` marks the beginning of the current sliding window.
        # We start with an empty window, so left begins at index 0.
        left: int = 0

        # `current_sum` stores the total toll cost of the current window
        # from costs[left] through costs[right].
        current_sum: int = 0

        # `best_length` stores the maximum valid window length seen so far.
        best_length: int = 0

        # We move `right` from left to right across the array.
        # At each step, we expand the window by including costs[right].
        for right in range(len(costs)):
            # Add the new checkpoint cost into the running total because
            # the window is now extended to include index `right`.
            current_sum += costs[right]

            # If the window total is too expensive, we must shrink the window
            # from the left until the total is within the allowed budget again.
            #
            # This works correctly because all values are non-negative:
            # removing elements from the left can only keep the sum the same
            # or make it smaller, never larger.
            while current_sum > budget and left <= right:
                # Remove the leftmost cost from the running total because
                # that checkpoint is no longer part of the window.
                current_sum -= costs[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window [left, right] is guaranteed to be valid
            # (its sum is <= budget), so we can measure its length.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any
            # valid window we have seen before.
            if current_length > best_length:
                best_length = current_length

        # After scanning the entire array, `best_length` is the answer.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    costs1: List[int] = [4, 2, 1, 3, 2]
    budget1: int = 6
    result1: int = solution.longest_toll_free_stretch(costs1, budget1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2
    costs2: List[int] = [1, 1, 1, 1, 1]
    budget2: int = 3
    result2: int = solution.longest_toll_free_stretch(costs2, budget2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional simple checks
    costs3: List[int] = [0, 0, 0, 0]
    budget3: int = 0
    result3: int = solution.longest_toll_free_stretch(costs3, budget3)
    print("Additional Test 1 Result:", result3)  # Expected: 4

    costs4: List[int] = [10, 20, 30]
    budget4: int = 5
    result4: int = solution.longest_toll_free_stretch(costs4, budget4)
    print("Additional Test 2 Result:", result4)  # Expected: 0