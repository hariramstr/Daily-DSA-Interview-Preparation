"""
Title: Longest Snack Stall Run Within Spending Limit

Problem Description:
You are visiting a street market with a row of snack stalls. The i-th stall has a snack
that costs costs[i] dollars. You want to buy snacks from a single contiguous block of
stalls, because walking back and forth is inconvenient. However, you have a fixed budget
and cannot spend more than budget dollars in total.

Return the length of the longest contiguous segment of stalls such that the sum of costs
in that segment is less than or equal to budget.

If no single stall can be afforded, return 0.

This problem is a natural fit for a sliding window approach: as you expand the right end
of the window, keep track of the running total. If the total becomes greater than budget,
move the left end forward until the window becomes valid again. Track the maximum valid
window length seen during the scan.

Constraints:
- 1 <= costs.length <= 100000
- 1 <= costs[i] <= 10000
- 1 <= budget <= 1000000000
- costs contains only positive integers

Example 1:
Input: costs = [2, 1, 3, 2, 1], budget = 5
Output: 2

Example 2:
Input: costs = [4, 2, 1, 1, 3], budget = 6
Output: 3
"""

from typing import List


class Solution:
    def longest_snack_stall_run(self, costs: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most budget.

        Args:
            costs: A list of positive integers where costs[i] is the cost of the i-th stall.
            budget: The maximum total amount that can be spent.

        Returns:
            The length of the longest contiguous segment with total cost <= budget.

        Time complexity:
            O(n), where n is the length of costs, because each index is visited at most
            twice: once by the right pointer and once by the left pointer.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The sliding window is represented by the inclusive range [left, right].
        # We will expand the window by moving "right" forward one step at a time.
        # As we do that, we maintain the sum of the current window in "current_sum".
        #
        # Because all costs are positive:
        # - Expanding the window always increases or keeps the sum larger.
        # - Shrinking the window from the left always decreases the sum.
        #
        # This positivity is exactly why the sliding window technique works so well here.
        left: int = 0
        current_sum: int = 0
        max_length: int = 0

        # Iterate through the array with "right" as the end of the current window.
        for right in range(len(costs)):
            # Add the new stall cost into the running total because the window now includes
            # costs[right].
            current_sum += costs[right]

            # If the current window is too expensive, it is invalid.
            # We must move the left side forward until the total cost is within budget again.
            #
            # We use a while loop instead of an if statement because removing just one element
            # may still leave the sum above budget. We keep shrinking until the window becomes
            # valid.
            while current_sum > budget and left <= right:
                # Remove the cost at the left edge because that stall is no longer inside
                # the window after we move left forward.
                current_sum -= costs[left]
                left += 1

            # At this point, the window [left, right] is guaranteed to be valid:
            # current_sum <= budget
            #
            # So we compute its length and compare it with the best answer seen so far.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # If no single stall was affordable, max_length will remain 0.
        return max_length

    def maxContiguousStallsWithinBudget(self, costs: List[int], budget: int) -> int:
        """
        Wrapper method matching the same problem goal with an alternative method name.

        Args:
            costs: A list of positive integers representing stall costs.
            budget: The maximum allowed sum for a contiguous segment.

        Returns:
            The maximum valid contiguous segment length.

        Time complexity:
            O(n), where n is the number of stalls.

        Space complexity:
            O(1).
        """
        return self.longest_snack_stall_run(costs, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    costs1: List[int] = [2, 1, 3, 2, 1]
    budget1: int = 5
    result1: int = solution.longest_snack_stall_run(costs1, budget1)
    print(f"Costs: {costs1}, Budget: {budget1}, Longest valid run: {result1}")

    # Example 2
    costs2: List[int] = [4, 2, 1, 1, 3]
    budget2: int = 6
    result2: int = solution.longest_snack_stall_run(costs2, budget2)
    print(f"Costs: {costs2}, Budget: {budget2}, Longest valid run: {result2}")

    # Additional sample: no stall can be afforded
    costs3: List[int] = [7, 8, 9]
    budget3: int = 5
    result3: int = solution.longest_snack_stall_run(costs3, budget3)
    print(f"Costs: {costs3}, Budget: {budget3}, Longest valid run: {result3}")