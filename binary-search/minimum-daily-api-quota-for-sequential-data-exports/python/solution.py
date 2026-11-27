"""
Title: Minimum Daily API Quota for Sequential Data Exports

Problem Description:
A company needs to export analytics data for n clients in the given order. The i-th client
requires requests[i] API calls to finish its export. Because of compliance rules, exports
cannot be split across days out of order: each day you process a contiguous block of clients,
and once you move to the next day, you cannot go back to an earlier client. You are also not
allowed to split one client's export across multiple days, so the daily quota must be at least
as large as the largest single value in requests.

Given an integer array requests and an integer d, return the minimum daily API quota needed to
finish all exports within at most d days.

This is a decision-and-search problem: for any proposed daily quota Q, you can check whether
all exports can be completed in at most d days while preserving order. Your task is to find
the smallest feasible Q.

Constraints:
- 1 <= requests.length <= 100000
- 1 <= requests[i] <= 1000000000
- 1 <= d <= requests.length

Example 1:
Input: requests = [7, 2, 5, 10, 8], d = 2
Output: 18
Explanation: With quota 18, one valid split is [7, 2, 5] and [10, 8]. A quota of 17 is not
enough because preserving order would require 3 days.

Example 2:
Input: requests = [4, 4, 4, 4, 4], d = 3
Output: 8
Explanation: Using quota 8, the exports can be grouped as [4, 4], [4, 4], [4]. Any quota
below 8 would force at least 4 days.
"""

from typing import List


class Solution:
    def _can_finish_with_quota(self, requests: List[int], d: int, quota: int) -> bool:
        """
        Check whether all client exports can be completed within at most d days
        using the given daily API quota while preserving order.

        Args:
            requests: List of API calls required for each client export.
            d: Maximum number of allowed days.
            quota: Proposed daily API quota to test.

        Returns:
            True if all exports can be completed in at most d days, otherwise False.

        Time complexity:
            O(n), where n is the number of clients, because we scan the list once.

        Space complexity:
            O(1), because we use only a few extra variables.
        """
        # We start by assuming we are using the first day.
        # Even if there is only one client, we still need at least one day.
        days_used: int = 1

        # This variable tracks how many API calls have already been assigned
        # to the current day.
        current_day_total: int = 0

        # We process clients strictly from left to right because the problem
        # requires preserving the original order.
        for api_calls in requests:
            # If adding this client's export to the current day would exceed
            # the quota, then we must start a new day.
            #
            # Why is this greedy choice correct?
            # Because for a fixed quota, the best way to minimize the number
            # of days is to pack as much as possible into the current day
            # before moving to the next one.
            if current_day_total + api_calls > quota:
                days_used += 1
                current_day_total = api_calls

                # If we already need more than d days, then this quota is not feasible.
                # We can stop early to save time.
                if days_used > d:
                    return False
            else:
                # Otherwise, we safely place this client's export into the current day.
                current_day_total += api_calls

        # If we finish scanning all clients without exceeding d days,
        # then the quota works.
        return True

    def min_daily_api_quota(self, requests: List[int], d: int) -> int:
        """
        Find the minimum daily API quota needed to complete all exports
        within at most d days while preserving order.

        Args:
            requests: List of API calls required for each client export.
            d: Maximum number of allowed days.

        Returns:
            The smallest feasible daily API quota.

        Time complexity:
            O(n * log(sum(requests))), where n is the number of clients.
            The binary search runs over the quota range, and each feasibility
            check scans the array once.

        Space complexity:
            O(1), excluding the input list.
        """
        # The smallest possible quota cannot be less than the largest single request,
        # because one client's export cannot be split across multiple days.
        left: int = max(requests)

        # The largest possible quota is the sum of all requests, which means
        # we could finish everything in one day.
        right: int = sum(requests)

        # We will binary search for the smallest feasible quota.
        #
        # Invariant:
        # - Any quota smaller than the answer is infeasible.
        # - The answer lies somewhere in [left, right].
        while left < right:
            # Compute the middle quota carefully.
            mid: int = left + (right - left) // 2

            # Test whether this quota is enough.
            if self._can_finish_with_quota(requests, d, mid):
                # If mid works, then the answer could be mid or something smaller.
                # So we keep the left half, including mid.
                right = mid
            else:
                # If mid does not work, then we need a larger quota.
                left = mid + 1

        # When left == right, binary search has converged to the minimum feasible quota.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # requests = [7, 2, 5, 10, 8], d = 2
    # Expected output: 18
    #
    # Quick verification:
    # - Quota 18 works:
    #   Day 1: 7 + 2 + 5 = 14
    #   Day 2: 10 + 8 = 18
    #   Total days = 2
    #
    # - Quota 17 does not work:
    #   Day 1: 7 + 2 + 5 = 14
    #   Day 2: 10
    #   Day 3: 8
    #   Total days = 3 > 2
    requests_1: List[int] = [7, 2, 5, 10, 8]
    d_1: int = 2
    result_1: int = solution.min_daily_api_quota(requests_1, d_1)
    print(f"Example 1 result: {result_1}")  # Expected: 18

    # Example 2:
    # requests = [4, 4, 4, 4, 4], d = 3
    # Expected output: 8
    #
    # Quick verification:
    # - Quota 8 works:
    #   Day 1: 4 + 4 = 8
    #   Day 2: 4 + 4 = 8
    #   Day 3: 4
    #   Total days = 3
    #
    # - Quota 7 does not work:
    #   Each day can hold only one 4 plus no second 4
    #   So we need 5 days, which is too many.
    requests_2: List[int] = [4, 4, 4, 4, 4]
    d_2: int = 3
    result_2: int = solution.min_daily_api_quota(requests_2, d_2)
    print(f"Example 2 result: {result_2}")  # Expected: 8

    # Additional sample:
    requests_3: List[int] = [1, 2, 3, 1, 1]
    d_3: int = 4
    result_3: int = solution.min_daily_api_quota(requests_3, d_3)
    print(f"Additional example result: {result_3}")  # Expected: 3