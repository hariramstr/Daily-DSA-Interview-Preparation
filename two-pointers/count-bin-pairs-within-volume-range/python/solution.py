"""
Title: Count Bin Pairs Within Volume Range

Problem Description:
A warehouse stores reusable bins, and each bin has a volume capacity represented by an
integer in the array volumes. You are also given two integers low and high. A pair of
bins (i, j) is considered compatible if i < j and the combined volume
volumes[i] + volumes[j] is within the inclusive range [low, high].

Your task is to return the total number of compatible pairs.

The input array is not guaranteed to be sorted. Because the warehouse may contain a
large number of bins, an O(n^2) solution may be too slow. Design an algorithm that
efficiently counts all valid pairs.

Two bins are distinct if they come from different indices, even if they have the same
volume. Be careful not to double-count pairs. The expected solution should take
advantage of sorting and a two-pointer counting strategy.

Constraints:
- 1 <= volumes.length <= 100000
- 0 <= volumes[i] <= 1000000000
- 0 <= low <= high <= 2000000000
- The answer may not fit in a 32-bit integer, so use a 64-bit integer type where needed.

Examples:
1) volumes = [4, 1, 7, 3, 2], low = 5, high = 8
   Output: 6

2) volumes = [2, 2, 2, 2], low = 4, high = 4
   Output: 6
"""

from typing import List


class Solution:
    def countFairPairs(self, volumes: List[int], low: int, high: int) -> int:
        """
        Count the number of index pairs (i, j) such that i < j and
        low <= volumes[i] + volumes[j] <= high.

        The method sorts the array, then uses a helper that counts how many pairs
        have sum <= limit. The final answer is:
            count(sum <= high) - count(sum <= low - 1)

        Args:
            volumes: List of bin volumes.
            low: Inclusive lower bound for pair sum.
            high: Inclusive upper bound for pair sum.

        Returns:
            Total number of valid pairs.

        Time complexity:
            O(n log n) due to sorting, plus O(n) for each two-pointer pass.

        Space complexity:
            O(1) extra space beyond the sort operation's internal needs.
        """
        # Sorting is the key first step.
        #
        # Why sort?
        # After sorting, we can use the relative order of values to make smart decisions.
        # Specifically, if volumes[left] + volumes[right] is small enough, then every
        # element between left and right paired with volumes[left] will also be small enough.
        # This allows us to count many pairs at once instead of checking each pair one by one.
        volumes.sort()

        # We count:
        #   number of pairs with sum <= high
        # minus
        #   number of pairs with sum <= low - 1
        #
        # The difference leaves exactly the number of pairs whose sums are in [low, high].
        return self._count_pairs_with_sum_at_most(volumes, high) - self._count_pairs_with_sum_at_most(
            volumes, low - 1
        )

    def _count_pairs_with_sum_at_most(self, volumes: List[int], limit: int) -> int:
        """
        Count how many pairs (i, j) with i < j have volumes[i] + volumes[j] <= limit.

        This uses the classic two-pointer technique on a sorted array.

        Args:
            volumes: Sorted list of bin volumes.
            limit: Maximum allowed pair sum.

        Returns:
            Number of pairs whose sum is at most limit.

        Time complexity:
            O(n), where n is the length of volumes.

        Space complexity:
            O(1).
        """
        # Initialize two pointers:
        # - left starts at the beginning (smallest value)
        # - right starts at the end (largest value)
        #
        # We will move these pointers inward based on whether the current pair sum
        # is within the allowed limit.
        left: int = 0
        right: int = len(volumes) - 1

        # This variable stores the total number of valid pairs found so far.
        count: int = 0

        # Continue while left is strictly less than right,
        # because a pair requires two different indices.
        while left < right:
            current_sum: int = volumes[left] + volumes[right]

            # Case 1: current pair sum is within the limit.
            if current_sum <= limit:
                # Because the array is sorted:
                # volumes[left] + volumes[right] <= limit
                #
                # Then for the same 'left', every index from left+1 up to right
                # also forms a valid pair with 'left', since those values are
                # <= volumes[right].
                #
                # So instead of counting one pair, we can count all of them at once:
                #   (left, left+1), (left, left+2), ..., (left, right)
                #
                # Number of such pairs is:
                #   right - left
                count += right - left

                # After counting all pairs that start with this 'left',
                # we move left forward to consider the next starting element.
                left += 1
            else:
                # Case 2: current pair sum is too large.
                #
                # Since volumes[right] is the larger endpoint, keeping left fixed and
                # moving right leftward is the only way to possibly reduce the sum.
                right -= 1

        return count


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    volumes_1: List[int] = [4, 1, 7, 3, 2]
    low_1: int = 5
    high_1: int = 8
    result_1: int = solution.countFairPairs(volumes_1, low_1, high_1)
    print("Example 1 Result:", result_1)  # Expected: 6

    # Example 2
    volumes_2: List[int] = [2, 2, 2, 2]
    low_2: int = 4
    high_2: int = 4
    result_2: int = solution.countFairPairs(volumes_2, low_2, high_2)
    print("Example 2 Result:", result_2)  # Expected: 6

    # Additional quick checks
    volumes_3: List[int] = [1]
    low_3: int = 1
    high_3: int = 2
    result_3: int = solution.countFairPairs(volumes_3, low_3, high_3)
    print("Single Element Result:", result_3)  # Expected: 0

    volumes_4: List[int] = [0, 5, 10, 15]
    low_4: int = 10
    high_4: int = 20
    result_4: int = solution.countFairPairs(volumes_4, low_4, high_4)
    print("Additional Test Result:", result_4)  # Expected: 4