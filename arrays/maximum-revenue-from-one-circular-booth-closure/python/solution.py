"""
Title: Maximum Revenue from One Circular Booth Closure

Problem Description:
A street festival has n food booths arranged in a circle. The i-th booth earns revenue[i]
dollars if it stays open for the day. Due to a temporary power issue, the organizers must
close exactly one contiguous block of booths. Because the street is circular, the closed
block may wrap from the end of the array back to the beginning.

You are also given two integers, minClose and maxClose. The number of closed booths must be
between minClose and maxClose, inclusive. After closing that single circular block, all
remaining booths stay open, and your goal is to maximize the total revenue of the open booths.

Return the maximum total revenue that can remain open.

Formally, choose exactly one circular subarray of length L where minClose <= L <= maxClose,
remove its sum from the total revenue, and maximize the sum of the remaining elements.

Constraints:
- 1 <= n <= 200000
- -10^9 <= revenue[i] <= 10^9
- 1 <= minClose <= maxClose <= n
- The chosen block must contain at least one booth and may contain all booths.
"""

from collections import deque
from typing import List


class Solution:
    def max_open_revenue(self, revenue: List[int], minClose: int, maxClose: int) -> int:
        """
        Compute the maximum revenue that can remain open after closing exactly one
        circular contiguous block whose length is between minClose and maxClose.

        Args:
            revenue: List of booth revenues arranged in a circle.
            minClose: Minimum allowed number of booths to close.
            maxClose: Maximum allowed number of booths to close.

        Returns:
            The maximum possible sum of revenues of the booths that remain open.

        Time complexity:
            O(n), where n is the number of booths.

        Space complexity:
            O(n), due to the doubled prefix sum array and the deque.
        """
        n: int = len(revenue)
        total_sum: int = sum(revenue)

        # Special case:
        # If we are forced to close all booths, then the remaining open revenue is 0.
        # This is not only correct, but also avoids edge-case handling in the general logic.
        if minClose == n and maxClose == n:
            return 0

        # To handle circular subarrays efficiently, we duplicate the array.
        # Example:
        #   revenue = [a, b, c, d]
        #   doubled = [a, b, c, d, a, b, c, d]
        #
        # Any circular subarray of length <= n can now be represented as a normal
        # contiguous subarray in this doubled array.
        doubled: List[int] = revenue + revenue

        # Build prefix sums for the doubled array.
        # prefix[i] = sum of doubled[0:i]
        # Then sum of subarray doubled[l:r] is prefix[r] - prefix[l].
        prefix: List[int] = [0] * (2 * n + 1)
        for i in range(2 * n):
            prefix[i + 1] = prefix[i] + doubled[i]

        # Our goal:
        #   maximize open revenue = total_sum - closed_sum
        # So we need to minimize the sum of a valid circular subarray to close.
        #
        # We will enumerate the END position of the closed subarray in the doubled array.
        # Suppose the closed subarray is doubled[j:i], where:
        #   - i is the exclusive end index in prefix terms
        #   - j is the start index
        # Then:
        #   closed length = i - j
        #   closed sum = prefix[i] - prefix[j]
        #
        # We need:
        #   minClose <= i - j <= maxClose
        # and also the chosen circular block must correspond to a valid start in the
        # original circle, so j must be in [0, n - 1].
        #
        # For a fixed i, minimizing prefix[i] - prefix[j] means maximizing prefix[j]
        # among valid j values.
        #
        # Therefore, for each i, we need the maximum prefix[j] where:
        #   i - maxClose <= j <= i - minClose
        #   0 <= j <= n - 1
        #
        # This is a sliding window maximum problem over prefix[j].
        #
        # We use a deque storing candidate indices j in decreasing order of prefix[j].
        # The front of the deque always holds the index with the largest prefix value
        # in the current valid window.
        candidates: deque[int] = deque()

        # Track the minimum possible closed sum found so far.
        min_closed_sum: int | None = None

        # We only need to consider end positions i such that the start j can still be
        # within the first n positions, and the subarray length is at most n.
        #
        # Since j must be <= n - 1 and length >= minClose, the largest useful i is:
        #   (n - 1) + maxClose
        # But because maxClose <= n, iterating to 2n is safe and simple.
        #
        # We use i as a prefix index, meaning the subarray ends at doubled[i - 1].
        for i in range(1, 2 * n + 1):
            # Step 1:
            # Add the new possible start index j = i - minClose into the candidate set.
            #
            # Why?
            # For current end i, any start j must satisfy j <= i - minClose.
            # The newest index that just became eligible is exactly i - minClose.
            add_index: int = i - minClose
            if 0 <= add_index <= n - 1:
                # Maintain deque in decreasing order of prefix values.
                # If the new prefix[add_index] is greater than or equal to the values at
                # the back, those older indices can never be better for any future i,
                # because:
                #   - the new index is more recent (stays valid longer)
                #   - it has a prefix value at least as large
                while candidates and prefix[candidates[-1]] <= prefix[add_index]:
                    candidates.pop()
                candidates.append(add_index)

            # Step 2:
            # Remove indices that are too old, meaning they would create a subarray
            # longer than maxClose.
            #
            # Valid j must satisfy j >= i - maxClose.
            min_valid_start: int = i - maxClose
            while candidates and candidates[0] < min_valid_start:
                candidates.popleft()

            # Step 3:
            # If we have any valid candidate start index, the best one is at the front
            # of the deque because it has the maximum prefix[j].
            #
            # Then:
            #   closed_sum = prefix[i] - prefix[j]
            # and this is the minimum possible closed sum for this end i.
            if candidates:
                best_start: int = candidates[0]
                closed_sum: int = prefix[i] - prefix[best_start]

                if min_closed_sum is None or closed_sum < min_closed_sum:
                    min_closed_sum = closed_sum

        # By problem constraints, there is always at least one valid block to close,
        # so min_closed_sum must have been set.
        assert min_closed_sum is not None

        # Maximum open revenue = total revenue - minimum closed revenue.
        return total_sum - min_closed_sum


if __name__ == "__main__":
    solution = Solution()

    # Sample 1
    # revenue = [8, -3, 5, -2, 4], total = 12
    # Valid close lengths: 2..3
    # Best closure is [-2, 4] with sum 2, leaving 10.
    revenue1: List[int] = [8, -3, 5, -2, 4]
    min_close1: int = 2
    max_close1: int = 3
    result1: int = solution.max_open_revenue(revenue1, min_close1, max_close1)
    print(result1)  # Expected: 10

    # Sample 2
    # revenue = [6, -5, 7, -8, 3, 2], total = 5
    # Valid close lengths: 1..2
    # Best closure is [-8] with sum -8, leaving 13.
    revenue2: List[int] = [6, -5, 7, -8, 3, 2]
    min_close2: int = 1
    max_close2: int = 2
    result2: int = solution.max_open_revenue(revenue2, min_close2, max_close2)
    print(result2)  # Expected: 13

    # Additional quick sanity checks
    revenue3: List[int] = [5]
    print(solution.max_open_revenue(revenue3, 1, 1))  # Expected: 0

    revenue4: List[int] = [4, -10, 3]
    print(solution.max_open_revenue(revenue4, 1, 2))  # Best close [-10], expected: 7