"""
Title: Find First Day With At Least Target Signups

Problem Description:
You are given a non-decreasing integer array `signups`, where `signups[i]`
represents the total number of users who have signed up for a product by the
end of day `i`. Because this is a cumulative total, the array is sorted in
non-decreasing order. You are also given an integer `target` representing a
milestone number of total signups.

Your task is to return the earliest day index at which the total number of
signups is greater than or equal to `target`. If the milestone is never
reached, return `-1`.

This problem models a common analytics task: product teams often store
cumulative counts over time and need to quickly answer milestone queries.
A linear scan works, but interviewers expect a faster solution by using binary
search on the sorted cumulative totals.

Return the smallest index `i` such that `signups[i] >= target`.

Constraints:
- 1 <= signups.length <= 100000
- 0 <= signups[i] <= 1000000000
- `signups` is sorted in non-decreasing order
- 0 <= target <= 1000000000

Example 1:
Input: signups = [3, 5, 5, 9, 14], target = 6
Output: 3
Explanation: The first day where total signups are at least 6 is day index 3,
where the value is 9.

Example 2:
Input: signups = [1, 2, 4, 4, 7], target = 4
Output: 2
Explanation: Although 4 appears more than once, the earliest day index with at
least 4 signups is 2.

If no such day exists, return `-1`. The solution should run in O(log n) time.
"""

from typing import List


class Solution:
    def first_day_at_least_target(self, signups: List[int], target: int) -> int:
        """
        Find the earliest index where signups[index] is greater than or equal to target.

        Args:
            signups: A non-decreasing list of cumulative signup totals.
            target: The milestone signup count we want to reach or exceed.

        Returns:
            The smallest index i such that signups[i] >= target.
            Returns -1 if no such index exists.

        Time Complexity:
            O(log n), because we cut the search space roughly in half each step.

        Space Complexity:
            O(1), because we use only a constant amount of extra memory.
        """
        # We will use binary search because:
        # 1. The array is sorted in non-decreasing order.
        # 2. We need the FIRST position where value >= target.
        #
        # This is a classic "lower bound" search problem.
        #
        # Instead of stopping at the first matching value, we continue searching
        # toward the left whenever we find a value that is already >= target.
        # That ensures we get the earliest valid day.

        left: int = 0
        right: int = len(signups) - 1

        # This variable stores the best answer found so far.
        # We start with -1 to mean "not found yet".
        answer: int = -1

        # Continue searching while the current search interval is valid.
        while left <= right:
            # Compute the middle index safely.
            # In Python, overflow is not an issue, but this formula is still
            # the standard and clear binary search pattern.
            mid: int = left + (right - left) // 2

            # If the middle value is large enough, then mid is a valid candidate.
            if signups[mid] >= target:
                # Record this index as a possible answer.
                answer = mid

                # Important:
                # We do NOT stop here, because there might be an even earlier day
                # on the left side that also satisfies signups[i] >= target.
                #
                # So we shrink the search space to the left half.
                right = mid - 1
            else:
                # If signups[mid] < target, then mid cannot be the answer,
                # and neither can anything to its left, because the array is sorted.
                #
                # Therefore, we must search only in the right half.
                left = mid + 1

        # If we found at least one valid index, answer holds the earliest one.
        # Otherwise, it remains -1.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # signups = [3, 5, 5, 9, 14], target = 6
    # The first value >= 6 is 9 at index 3.
    signups_1: List[int] = [3, 5, 5, 9, 14]
    target_1: int = 6
    result_1: int = solution.first_day_at_least_target(signups_1, target_1)
    print(f"Example 1 result: {result_1}")  # Expected: 3

    # Example 2:
    # signups = [1, 2, 4, 4, 7], target = 4
    # The first value >= 4 is 4 at index 2.
    signups_2: List[int] = [1, 2, 4, 4, 7]
    target_2: int = 4
    result_2: int = solution.first_day_at_least_target(signups_2, target_2)
    print(f"Example 2 result: {result_2}")  # Expected: 2

    # Additional example where the target is never reached.
    signups_3: List[int] = [1, 2, 3, 3, 5]
    target_3: int = 6
    result_3: int = solution.first_day_at_least_target(signups_3, target_3)
    print(f"Additional example result: {result_3}")  # Expected: -1

    # Additional example where target is 0.
    # Since all values are >= 0 in this example, the earliest valid day is 0.
    signups_4: List[int] = [0, 0, 2, 5]
    target_4: int = 0
    result_4: int = solution.first_day_at_least_target(signups_4, target_4)
    print(f"Additional example result: {result_4}")  # Expected: 0)