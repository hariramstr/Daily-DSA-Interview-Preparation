"""
Title: Longest Call Streak Within Roaming Budget

Problem Description:
You are given an array costs where costs[i] represents the roaming charge for the i-th
phone call made during a trip. A traveler wants to look at one continuous streak of calls
and keep the total roaming charge of that streak within a fixed budget.

Your task is to return the length of the longest contiguous subarray whose sum is less
than or equal to budget.

In other words, choose indices l and r such that 0 <= l <= r < costs.length, and the sum
of costs[l] through costs[r] does not exceed budget. Among all such valid choices, find
the maximum possible number of calls in the streak.

This is a sliding window problem: because all roaming charges are non-negative, we can
expand the right end of the window and shrink the left end whenever the total exceeds
the budget.

Constraints:
- 1 <= costs.length <= 100000
- 0 <= costs[i] <= 10000
- 0 <= budget <= 1000000000
- All values are integers

Example 1:
Input: costs = [4, 2, 1, 3, 2], budget = 6
Output: 3
Explanation: The longest valid streak is [2, 1, 3], which has total cost 6 and length 3.

Example 2:
Input: costs = [7, 1, 2, 1, 1], budget = 4
Output: 3
Explanation:
- [1, 2, 1, 1] has total cost 5, so it is invalid.
- Valid longest streaks include [1, 2, 1] and [2, 1, 1], both with total cost 4 and length 3.
- Therefore, the answer is 3.
"""

from typing import List


class Solution:
    def longest_call_streak(self, costs: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most budget.

        Args:
            costs: A list of non-negative integers where each value is the roaming
                charge for one phone call.
            budget: The maximum allowed total roaming charge for the chosen streak.

        Returns:
            The length of the longest contiguous subarray with sum <= budget.

        Time Complexity:
            O(n), where n is the length of costs.
            Each element is added to the window once and removed from the window at most once.

        Space Complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # This variable marks the left boundary of our current sliding window.
        # The window will always represent a contiguous range: costs[left:right+1].
        left: int = 0

        # This stores the sum of all values currently inside the window.
        # We update it incrementally instead of recomputing sums repeatedly,
        # which keeps the algorithm efficient.
        current_sum: int = 0

        # This keeps track of the best (maximum) valid window length seen so far.
        max_length: int = 0

        # We move the right boundary from left to right across the array.
        # At each step, we try to include costs[right] in the current window.
        for right in range(len(costs)):
            # Expand the window by adding the new rightmost element.
            current_sum += costs[right]

            # If the window sum is now too large, we must shrink the window
            # from the left until the sum becomes valid again.
            #
            # Why is this correct?
            # Because all numbers are non-negative:
            # - Expanding the window can only keep the sum the same or increase it.
            # - Shrinking from the left can only keep the sum the same or decrease it.
            #
            # This monotonic behavior is exactly why the sliding window technique works here.
            while current_sum > budget and left <= right:
                # Remove the leftmost element from the current window sum.
                current_sum -= costs[left]

                # Move the left boundary one step to the right,
                # effectively shrinking the window.
                left += 1

            # At this point, the window costs[left:right+1] is guaranteed to have
            # sum <= budget, so it is a valid candidate.
            #
            # Compute its length:
            # If left == right, length is 1.
            # In general, length = right - left + 1.
            window_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any
            # valid window we have seen before.
            if window_length > max_length:
                max_length = window_length

        # After processing all possible right endpoints, max_length contains
        # the length of the longest valid contiguous subarray.
        return max_length

    def solve(self, costs: List[int], budget: int) -> int:
        """
        Wrapper method that calls the main sliding window algorithm.

        Args:
            costs: A list of non-negative roaming charges.
            budget: The maximum allowed total cost.

        Returns:
            The maximum valid streak length.

        Time Complexity:
            O(n), where n is the length of costs.

        Space Complexity:
            O(1).
        """
        return self.longest_call_streak(costs, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    costs1: List[int] = [4, 2, 1, 3, 2]
    budget1: int = 6
    result1: int = solution.solve(costs1, budget1)
    print("Example 1:")
    print("Costs:", costs1)
    print("Budget:", budget1)
    print("Longest valid call streak length:", result1)
    print()

    # Example 2
    costs2: List[int] = [7, 1, 2, 1, 1]
    budget2: int = 4
    result2: int = solution.solve(costs2, budget2)
    print("Example 2:")
    print("Costs:", costs2)
    print("Budget:", budget2)
    print("Longest valid call streak length:", result2)
    print()

    # Additional beginner-friendly test cases

    # Single call that fits the budget
    costs3: List[int] = [5]
    budget3: int = 5
    result3: int = solution.solve(costs3, budget3)
    print("Additional Test 1:")
    print("Costs:", costs3)
    print("Budget:", budget3)
    print("Longest valid call streak length:", result3)
    print()

    # No positive-cost call can fit, but zero-cost calls would
    costs4: List[int] = [8, 9, 10]
    budget4: int = 7
    result4: int = solution.solve(costs4, budget4)
    print("Additional Test 2:")
    print("Costs:", costs4)
    print("Budget:", budget4)
    print("Longest valid call streak length:", result4)
    print()

    # Includes zero-cost calls
    costs5: List[int] = [0, 0, 3, 0, 2]
    budget5: int = 3
    result5: int = solution.solve(costs5, budget5)
    print("Additional Test 3:")
    print("Costs:", costs5)
    print("Budget:", budget5)
    print("Longest valid call streak length:", result5)