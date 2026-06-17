"""
Title: Locate First Price Tier Meeting Budget

Problem Description:
You are given a sorted array `tiers` where `tiers[i]` represents the minimum order
amount required to unlock the `i`-th discount tier in an online store. The array is
sorted in non-decreasing order, and duplicate values may appear because multiple tier
labels can start at the same minimum amount. You are also given an integer `budget`.

Your task is to return the smallest index `i` such that `tiers[i] >= budget`. In other
words, find the first discount tier whose required minimum order amount is at least the
shopper's budget target. If no such index exists, return `-1`.

This problem should be solved efficiently. A linear scan works, but the intended
solution uses binary search because the array is already sorted. Be careful with edge
cases such as an empty array, all values being smaller than `budget`, or the answer
appearing multiple times due to duplicates.

Constraints:
- 0 <= tiers.length <= 100000
- 0 <= tiers[i] <= 1000000000
- tiers is sorted in non-decreasing order
- 0 <= budget <= 1000000000

Example 1:
Input: tiers = [20, 35, 50, 50, 80], budget = 50
Output: 2
Explanation: The first index with value at least 50 is index 2.

Example 2:
Input: tiers = [10, 15, 15, 30], budget = 16
Output: 3
Explanation: Values at indices 0, 1, and 2 are smaller than 16. The first valid tier
is 30 at index 3.
"""

from typing import List


class Solution:
    def first_tier_meeting_budget(self, tiers: List[int], budget: int) -> int:
        """
        Find the smallest index whose value is greater than or equal to the budget.

        This method uses binary search on the sorted input array to efficiently locate
        the first position where tiers[i] >= budget. If no such position exists, it
        returns -1.

        Args:
            tiers: A sorted list of minimum order amounts for discount tiers.
            budget: The target budget value to compare against.

        Returns:
            The smallest index i such that tiers[i] >= budget, or -1 if no such index
            exists.

        Time complexity:
            O(log n), where n is the length of tiers.

        Space complexity:
            O(1), because only a constant amount of extra space is used.
        """
        # If the list is empty, there is no valid index to return.
        # This is an important edge case because binary search requires valid bounds.
        if not tiers:
            return -1

        # We will search within the inclusive range [left, right].
        # left starts at the first valid index.
        # right starts at the last valid index.
        left: int = 0
        right: int = len(tiers) - 1

        # This variable will store the best answer found so far.
        # We initialize it to -1 to mean "no valid tier found yet".
        #
        # Why do we need this?
        # Because when we find a value >= budget, it might be the answer,
        # but there could still be an earlier index that also satisfies the condition.
        # So we save the current index and continue searching to the left.
        answer: int = -1

        # Continue searching while the search interval is valid.
        # The condition left <= right means there are still elements to inspect.
        while left <= right:
            # Compute the middle index safely.
            # In Python, (left + right) // 2 is already safe because integers do not overflow,
            # but this is still the standard binary search pattern.
            mid: int = (left + right) // 2

            # Compare the middle value with the budget.
            if tiers[mid] >= budget:
                # This middle index is a valid candidate because it meets the requirement.
                # However, we are not done yet:
                # we need the FIRST such index, not just any valid index.
                #
                # So we record mid as a possible answer...
                answer = mid

                # ...and then continue searching the LEFT half to see whether
                # an even smaller valid index exists.
                right = mid - 1
            else:
                # If tiers[mid] < budget, then mid cannot be the answer.
                # Also, because the array is sorted in non-decreasing order,
                # every index to the left of mid must be <= tiers[mid], and therefore
                # also < budget.
                #
                # That means the answer, if it exists, must be in the RIGHT half.
                left = mid + 1

        # After the loop ends, answer is either:
        # - the first index where tiers[i] >= budget, or
        # - -1 if no such index exists.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # tiers = [20, 35, 50, 50, 80], budget = 50
    # Expected output: 2
    tiers1: List[int] = [20, 35, 50, 50, 80]
    budget1: int = 50
    result1: int = solution.first_tier_meeting_budget(tiers1, budget1)
    print(f"tiers = {tiers1}, budget = {budget1} -> {result1}")

    # Example 2:
    # tiers = [10, 15, 15, 30], budget = 16
    # Expected output: 3
    tiers2: List[int] = [10, 15, 15, 30]
    budget2: int = 16
    result2: int = solution.first_tier_meeting_budget(tiers2, budget2)
    print(f"tiers = {tiers2}, budget = {budget2} -> {result2}")

    # Additional edge case: empty list
    tiers3: List[int] = []
    budget3: int = 25
    result3: int = solution.first_tier_meeting_budget(tiers3, budget3)
    print(f"tiers = {tiers3}, budget = {budget3} -> {result3}")

    # Additional edge case: all values smaller than budget
    tiers4: List[int] = [5, 10, 15]
    budget4: int = 20
    result4: int = solution.first_tier_meeting_budget(tiers4, budget4)
    print(f"tiers = {tiers4}, budget = {budget4} -> {result4}")

    # Additional edge case: answer is the first index
    tiers5: List[int] = [7, 9, 12]
    budget5: int = 6
    result5: int = solution.first_tier_meeting_budget(tiers5, budget5)
    print(f"tiers = {tiers5}, budget = {budget5} -> {result5}")