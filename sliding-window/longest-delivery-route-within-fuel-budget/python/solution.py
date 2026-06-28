"""
Title: Longest Delivery Route Within Fuel Budget

Problem Description:
A courier company records the fuel cost of each stop along a planned route in an array
`costs`, where `costs[i]` is the amount of fuel needed to travel through stop `i`.
The driver wants to complete the longest possible consecutive portion of the route
without exceeding a total fuel budget `budget`.

Your task is to return the length of the longest contiguous subarray whose sum is
less than or equal to `budget`.

This is a practical sliding window problem because all fuel costs are non-negative.
As you expand a window to the right, the total fuel used only increases or stays the
same. If the total exceeds the budget, you can shrink the window from the left until
the route becomes affordable again.

Return `0` if no single stop can be included within the budget.

Constraints:
- 1 <= costs.length <= 100000
- 0 <= costs[i] <= 10000
- 0 <= budget <= 1000000000

Example 1:
Input: costs = [4, 2, 1, 7, 3, 2], budget = 8
Output: 3
Explanation: The longest valid consecutive route is [4, 2, 1] with total fuel 7.
Other valid windows such as [3, 2] are shorter.

Example 2:
Input: costs = [9, 1, 2, 1, 1], budget = 4
Output: 4
Explanation: The longest valid route is [1, 2, 1, 1], which uses exactly 4 units of fuel.
"""

from typing import List


class Solution:
    def longest_delivery_route(self, costs: List[int], budget: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is at most budget.

        Args:
            costs: A list of non-negative integers where each value is the fuel cost
                for a stop in the route.
            budget: The maximum total fuel allowed for any chosen consecutive route.

        Returns:
            The maximum length of a contiguous subarray with sum less than or equal
            to budget. Returns 0 if no valid stop can be included.

        Time complexity:
            O(n), where n is the length of costs, because each index is moved at most
            once by the right pointer and at most once by the left pointer.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # `left` marks the beginning of the current sliding window.
        # We will expand the window by moving the right boundary forward one step at a time.
        left: int = 0

        # `current_sum` stores the total fuel cost of the current window:
        # costs[left] + costs[left + 1] + ... + costs[right]
        current_sum: int = 0

        # `best_length` stores the maximum valid window size found so far.
        best_length: int = 0

        # We iterate `right` from 0 to the end of the array.
        # At each step, we include costs[right] into the current window.
        for right in range(len(costs)):
            # Expand the window to include the new stop at index `right`.
            current_sum += costs[right]

            # Because all numbers are non-negative, adding a new element can only
            # increase the sum or leave it unchanged.
            #
            # If the window is now too expensive, we must shrink it from the left
            # until the total cost is within budget again.
            #
            # This works correctly only because the values are non-negative:
            # removing elements from the left can only decrease the sum or keep it equal.
            while current_sum > budget and left <= right:
                # Remove the leftmost stop from the current window.
                current_sum -= costs[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window from `left` to `right` is guaranteed to have
            # total cost <= budget, so it is a valid candidate.
            #
            # Its length is:
            # right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any previous one.
            if current_length > best_length:
                best_length = current_length

        # If no single element fits within the budget, the shrinking process will always
        # collapse the window and best_length will remain 0, which matches the requirement.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    costs1: List[int] = [4, 2, 1, 7, 3, 2]
    budget1: int = 8
    result1: int = solution.longest_delivery_route(costs1, budget1)
    print(f"Example 1: costs = {costs1}, budget = {budget1}")
    print(f"Output: {result1}")
    print("Expected: 3")
    print()

    costs2: List[int] = [9, 1, 2, 1, 1]
    budget2: int = 4
    result2: int = solution.longest_delivery_route(costs2, budget2)
    print(f"Example 2: costs = {costs2}, budget = {budget2}")
    print(f"Output: {result2}")
    print("Expected: 4")
    print()

    costs3: List[int] = [10, 11, 12]
    budget3: int = 5
    result3: int = solution.longest_delivery_route(costs3, budget3)
    print(f"Additional Test: costs = {costs3}, budget = {budget3}")
    print(f"Output: {result3}")
    print("Expected: 0")
    print()

    costs4: List[int] = [0, 0, 0, 0]
    budget4: int = 0
    result4: int = solution.longest_delivery_route(costs4, budget4)
    print(f"Additional Test: costs = {costs4}, budget = {budget4}")
    print(f"Output: {result4}")
    print("Expected: 4")