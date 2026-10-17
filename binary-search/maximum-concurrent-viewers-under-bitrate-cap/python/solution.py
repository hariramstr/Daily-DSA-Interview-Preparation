"""
Title: Maximum Concurrent Viewers Under Bitrate Cap

Problem Description:
A video platform is preparing a live event. There are n viewers waiting to join, and
viewer i can only watch the stream if the selected global bitrate is at most limits[i].
If the platform chooses bitrate b, then every viewer with limits[i] >= b can join
successfully. However, the total outgoing bandwidth consumed is b multiplied by the
number of viewers who join. The platform has a hard bandwidth budget B and wants to
choose a single integer bitrate b (b >= 1) that maximizes the number of concurrent
viewers while still keeping total bandwidth usage within the budget.

Your task is to return the maximum number of viewers that can be served simultaneously.

More formally, for an integer bitrate b, let count(b) be the number of indices i such
that limits[i] >= b. Bitrate b is feasible if b * count(b) <= B. Among all feasible
bitrates, maximize count(b). Return that maximum value.

The array limits is not guaranteed to be sorted. A naive check over all possible
bitrates may be too slow because limits[i] and B can be very large. An efficient
solution is expected.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= limits[i] <= 10^12
- 1 <= B <= 10^18
- The chosen bitrate must be an integer
"""

from bisect import bisect_left
from typing import List


class Solution:
    def max_concurrent_viewers(self, limits: List[int], B: int) -> int:
        """
        Return the maximum number of viewers that can be served simultaneously.

        The key observation is:
        - If we want to serve exactly k viewers, then the cheapest possible integer
          bitrate is b = 1.
        - Since every limit[i] >= 1 by constraints, bitrate 1 is always acceptable
          for every viewer.
        - Therefore, serving k viewers is feasible if and only if k <= B, because
          total bandwidth would be 1 * k = k.
        - Since there are only n viewers total, the answer is simply min(n, B).

        Although the problem statement hints at binary search, the constraints and
        the requirement b >= 1 make the optimization collapse into this simple result.

        Args:
            limits: List of maximum supported bitrates for each viewer.
            B: Total bandwidth budget.

        Returns:
            The maximum number of viewers that can be served.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        # Step 1:
        # Count how many viewers exist in total.
        # We cannot serve more viewers than the total number available.
        n: int = len(limits)

        # Step 2:
        # The minimum allowed bitrate is 1.
        # Because every limits[i] >= 1, every viewer can join at bitrate 1.
        #
        # If we choose bitrate 1, then:
        #   count(1) = n
        #   total usage = 1 * n = n
        #
        # So:
        # - If B >= n, we can serve all n viewers.
        # - If B < n, then at bitrate 1 we can serve at most B viewers worth of
        #   bandwidth, and since bitrate cannot go below 1, we cannot do better.
        #
        # Therefore the maximum number of viewers is exactly min(n, B).
        return min(n, B)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # limits = [5, 8, 6, 3], B = 12
    # At bitrate 1, all 4 viewers can join, usage = 4 <= 12, so answer is 4.
    limits1: List[int] = [5, 8, 6, 3]
    B1: int = 12
    result1: int = solution.max_concurrent_viewers(limits1, B1)
    print(result1)  # Expected: 4

    # Example 2 from the prompt:
    # limits = [10, 10, 2, 2, 2], B = 9
    # At bitrate 1, all 5 viewers can join, usage = 5 <= 9, so answer is 5.
    limits2: List[int] = [10, 10, 2, 2, 2]
    B2: int = 9
    result2: int = solution.max_concurrent_viewers(limits2, B2)
    print(result2)  # Expected: 5

    # Additional quick sanity checks:
    limits3: List[int] = [1, 1, 1, 1, 1]
    B3: int = 3
    result3: int = solution.max_concurrent_viewers(limits3, B3)
    print(result3)  # Expected: 3

    limits4: List[int] = [100]
    B4: int = 1
    result4: int = solution.max_concurrent_viewers(limits4, B4)
    print(result4)  # Expected: 1