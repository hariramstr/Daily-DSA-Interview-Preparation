"""
Title: Minimum Server Capacity for Batched Query Waves

Problem Description:
You are given an array requests where requests[i] is the number of queries arriving
in the i-th minute, in chronological order. A backend service processes traffic in
fixed consecutive deployment waves. Each wave must cover a contiguous block of
minutes, and the total number of queries assigned to any single wave cannot exceed
the chosen server capacity C. Minutes cannot be split across waves: all queries from
one minute must stay in the same wave. You are also given an integer k, the maximum
number of waves your operations team is willing to create.

Your task is to compute the minimum integer server capacity C such that the entire
traffic timeline can be partitioned into at most k contiguous waves, where the sum
of requests in every wave is at most C.

Return the smallest possible capacity.

This is a decision-optimization problem: for a candidate capacity C, determine whether
the requests can be grouped into at most k valid contiguous waves. Then use that
monotonic property to find the minimum feasible capacity efficiently.

Constraints:
- 1 <= requests.length <= 200000
- 1 <= requests[i] <= 1000000000
- 1 <= k <= requests.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: requests = [7,2,5,10,8], k = 2
Output: 18

Example 2:
Input: requests = [1,4,4,3,2], k = 3
Output: 5
"""

from typing import List


class Solution:
    def can_split_with_capacity(self, requests: List[int], k: int, capacity: int) -> bool:
        """
        Check whether the requests can be partitioned into at most k contiguous waves
        such that each wave sum is at most the given capacity.

        Args:
            requests: List of query counts per minute.
            k: Maximum allowed number of waves.
            capacity: Candidate server capacity to test.

        Returns:
            True if the array can be split into at most k valid contiguous waves,
            otherwise False.

        Time complexity:
            O(n), where n is the number of minutes in requests.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We start by assuming we are building the first wave.
        # "waves_used" counts how many contiguous groups we have created so far.
        waves_used: int = 1

        # "current_sum" stores the total requests currently assigned to the active wave.
        current_sum: int = 0

        # We process the minutes in order because the problem requires contiguous waves.
        for value in requests:
            # If a single minute already exceeds the candidate capacity,
            # then this capacity is impossible immediately.
            # This check is logically correct even though our binary search lower bound
            # will already be at least max(requests).
            if value > capacity:
                return False

            # If adding this minute to the current wave stays within capacity,
            # we keep extending the current wave.
            if current_sum + value <= capacity:
                current_sum += value
            else:
                # Otherwise, we must start a new wave beginning with this minute.
                waves_used += 1
                current_sum = value

                # If we already need more than k waves, this capacity is not feasible.
                if waves_used > k:
                    return False

        # If we finish processing all minutes using at most k waves,
        # then the candidate capacity works.
        return True

    def minimum_server_capacity(self, requests: List[int], k: int) -> int:
        """
        Compute the minimum server capacity needed so that requests can be partitioned
        into at most k contiguous waves.

        Args:
            requests: List of query counts per minute.
            k: Maximum allowed number of waves.

        Returns:
            The smallest integer capacity that allows a valid partition.

        Time complexity:
            O(n log S), where n is the length of requests and
            S is the search range between max(requests) and sum(requests).

        Space complexity:
            O(1), excluding the input storage.
        """
        # The minimum possible capacity cannot be smaller than the largest single minute,
        # because one minute cannot be split across waves.
        left: int = max(requests)

        # The maximum possible capacity is the sum of all requests,
        # which corresponds to placing everything into one wave.
        right: int = sum(requests)

        # We perform binary search on the answer.
        # Why binary search works:
        # - If a capacity C is feasible, then any larger capacity is also feasible.
        # - If a capacity C is not feasible, then any smaller capacity is also not feasible.
        # This monotonic behavior is exactly what binary search needs.
        while left < right:
            # Midpoint candidate capacity.
            mid: int = left + (right - left) // 2

            # Test whether this candidate capacity is enough.
            if self.can_split_with_capacity(requests, k, mid):
                # If mid works, we try to find an even smaller feasible capacity.
                right = mid
            else:
                # If mid does not work, we must search larger capacities.
                left = mid + 1

        # At loop end, left == right and points to the smallest feasible capacity.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # requests = [7, 2, 5, 10, 8], k = 2
    # One optimal split is [7, 2, 5] and [10, 8]
    # Wave sums are 14 and 18, so the answer should be 18.
    requests1: List[int] = [7, 2, 5, 10, 8]
    k1: int = 2
    result1: int = solution.minimum_server_capacity(requests1, k1)
    print(result1)  # Expected: 18

    # Example 2:
    # requests = [1, 4, 4, 3, 2], k = 3
    # One valid split is [1, 4], [4], [3, 2]
    # The maximum wave sum is 5, so the answer should be 5.
    requests2: List[int] = [1, 4, 4, 3, 2]
    k2: int = 3
    result2: int = solution.minimum_server_capacity(requests2, k2)
    print(result2)  # Expected: 5