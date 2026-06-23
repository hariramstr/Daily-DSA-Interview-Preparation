"""
Title: Minimum Upgrade Level for Reliable Service Bundles

Problem Description:
A cloud platform offers n microservices. The i-th service currently runs at reliability
level levels[i]. You may apply a global upgrade policy with integer strength X.
After the policy is applied, every service with level below X is upgraded up to exactly X,
while services already at or above X remain unchanged.

The platform sells bundles of consecutive services. A bundle is considered reliable if the
sum of the final reliability levels of all services in that bundle is at least target.
You are given an integer k, and your goal is to make at least k reliable bundles.

Return the minimum integer X such that after applying the upgrade policy, the number of
reliable contiguous bundles is at least k. If the condition is already satisfied without
any upgrade, return 0.

Formally, define final[i] = max(levels[i], X). Count how many pairs (l, r) with
0 <= l <= r < n satisfy sum(final[l..r]) >= target. Find the smallest X for which this
count is at least k.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= levels[i] <= 10^9
- 1 <= target <= 10^18
- 1 <= k <= n * (n + 1) / 2
- X is an integer in the range [0, 10^9]
"""

from bisect import bisect_left
from typing import List


class FenwickTree:
    """Fenwick Tree / Binary Indexed Tree for prefix frequency sums."""

    def __init__(self, size: int) -> None:
        """
        Initialize an empty Fenwick tree.

        Args:
            size: Number of indices the tree should support.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def add(self, index: int, delta: int) -> None:
        """
        Add delta to one position.

        Args:
            index: Zero-based compressed index.
            delta: Value to add.

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        i: int = index + 1
        while i <= self.size:
            self.tree[i] += delta
            i += i & -i

    def prefix_sum(self, index: int) -> int:
        """
        Compute sum of values in range [0, index].

        Args:
            index: Zero-based compressed index.

        Returns:
            Prefix sum up to index.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        if index < 0:
            return 0

        result: int = 0
        i: int = index + 1
        while i > 0:
            result += self.tree[i]
            i -= i & -i
        return result


class Solution:
    def min_upgrade_level(self, levels: List[int], target: int, k: int) -> int:
        """
        Find the minimum integer X such that after replacing each value by max(value, X),
        at least k contiguous subarrays have sum >= target.

        Args:
            levels: Original reliability levels of services.
            target: Minimum required sum for a bundle to be reliable.
            k: Required number of reliable contiguous bundles.

        Returns:
            The smallest valid upgrade level X.

        Time complexity:
            O(n log n log M), where M = 10^9

        Space complexity:
            O(n)
        """
        # First, check whether no upgrade is needed.
        # If X = 0 already gives at least k valid subarrays, the answer is exactly 0.
        if self._count_reliable_bundles(levels, target, 0) >= k:
            return 0

        # The answer is monotonic:
        # If some X works, then any larger X also works, because max(level, X)
        # can only stay the same or increase, so every subarray sum also stays the same
        # or increases. Therefore, the number of reliable bundles is non-decreasing in X.
        #
        # This monotonicity allows binary search on X.
        left: int = 1
        right: int = 10**9

        while left < right:
            mid: int = (left + right) // 2

            # Count how many subarrays are reliable after applying upgrade level mid.
            # If that count is large enough, mid is a valid answer, so we try smaller.
            # Otherwise, mid is too small, so we move right.
            if self._count_reliable_bundles(levels, target, mid) >= k:
                right = mid
            else:
                left = mid + 1

        return left

    def _count_reliable_bundles(self, levels: List[int], target: int, x: int) -> int:
        """
        Count how many contiguous subarrays have sum >= target after transforming
        each element into max(levels[i], x).

        Args:
            levels: Original reliability levels.
            target: Required minimum subarray sum.
            x: Chosen global upgrade level.

        Returns:
            Number of contiguous subarrays whose transformed sum is at least target.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(levels)

        # Step 1:
        # Build prefix sums of the transformed array final[i] = max(levels[i], x).
        #
        # Let prefix[0] = 0
        # Let prefix[i+1] = sum(final[0..i])
        #
        # Then the sum of subarray [l..r] is:
        # prefix[r+1] - prefix[l]
        #
        # We want:
        # prefix[r+1] - prefix[l] >= target
        # which is equivalent to:
        # prefix[l] <= prefix[r+1] - target
        #
        # So for each ending position r+1, we need to count how many earlier prefix sums
        # are <= current_prefix - target.
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + max(levels[i], x)

        # Step 2:
        # Coordinate compression.
        #
        # Prefix sums can be very large (up to around n * 1e9), so we cannot use them
        # directly as Fenwick tree indices.
        #
        # We sort all prefix sums and map each distinct value to a compact index.
        sorted_values: List[int] = sorted(set(prefix))

        # Step 3:
        # Use a Fenwick tree to maintain frequencies of prefix sums seen so far.
        #
        # As we scan prefix sums from left to right:
        # - Before processing prefix[j], the Fenwick tree stores prefix[0..j-1].
        # - We need the count of values <= prefix[j] - target.
        #
        # That gives the number of valid starting points l for subarrays ending at j-1.
        fenwick: FenwickTree = FenwickTree(len(sorted_values))

        total_count: int = 0

        # Insert prefix[0] first, because subarrays can start at index 0.
        first_index: int = bisect_left(sorted_values, prefix[0])
        fenwick.add(first_index, 1)

        # Process prefix[1] through prefix[n].
        for j in range(1, n + 1):
            current_prefix: int = prefix[j]
            threshold: int = current_prefix - target

            # Find the rightmost compressed position whose value <= threshold.
            #
            # bisect_left(sorted_values, threshold + 1) gives the first index with value
            # >= threshold + 1, i.e. the first value > threshold.
            # Therefore, subtracting 1 gives the last index with value <= threshold.
            rightmost_valid_index: int = bisect_left(sorted_values, threshold + 1) - 1

            # If there exists at least one compressed prefix value <= threshold,
            # query the Fenwick tree for how many such prefix sums have appeared so far.
            if rightmost_valid_index >= 0:
                total_count += fenwick.prefix_sum(rightmost_valid_index)

            # Now insert current prefix so it can serve as a starting prefix
            # for future subarrays.
            current_index: int = bisect_left(sorted_values, current_prefix)
            fenwick.add(current_index, 1)

        return total_count


if __name__ == "__main__":
    solution = Solution()

    levels1: List[int] = [1, 3, 2]
    target1: int = 5
    k1: int = 4
    result1: int = solution.min_upgrade_level(levels1, target1, k1)
    print(result1)  # Expected: 2

    levels2: List[int] = [0, 0, 4, 1]
    target2: int = 4
    k2: int = 8
    result2: int = solution.min_upgrade_level(levels2, target2, k2)
    print(result2)  # Expected: 3