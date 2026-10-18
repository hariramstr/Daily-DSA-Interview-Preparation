"""
Title: Count Ferry Crossings Within Tide Difference

Problem Description:
A coastal logistics company records the tide level at each scheduled ferry departure
during a day. You are given an integer array `tides` of length `n`, where `tides[i]`
is the tide level at the `i`-th departure slot, and an integer `limit`.

Two departure slots `i < j` form a valid crossing pair if the absolute difference
between their tide levels is at most `limit`, that is:

    |tides[i] - tides[j]| <= limit

Your task is to count how many distinct index pairs `(i, j)` are valid.

Because the number of pairs can be large, return the answer as a 64-bit integer.

A brute-force O(n^2) solution will time out for the largest inputs. You are expected
to design an efficient solution using sorting and a two-pointers strategy.

Constraints:
- 1 <= n <= 200000
- -10^9 <= tides[i] <= 10^9
- 0 <= limit <= 10^9

Examples:
1)
Input: tides = [4, 1, 7, 3, 5], limit = 2
Output: 4

2)
Input: tides = [8, 8, 8, 8], limit = 0
Output: 6

Notes:
- Pairs are based on original indices, but only the tide values matter for validity.
- Duplicate tide levels are allowed.
- An optimal solution should run in O(n log n) time due to sorting, with a linear
  two-pointer scan afterward.
"""

from typing import List


class Solution:
    def count_valid_crossings(self, tides: List[int], limit: int) -> int:
        """
        Count how many index pairs have tide difference at most `limit`.

        The method sorts the tide levels, then uses a sliding window / two-pointer
        scan to count, for each left position, how many right positions remain within
        the allowed difference.

        Args:
            tides: List of tide levels at departure slots.
            limit: Maximum allowed absolute difference between two tide levels.

        Returns:
            Total number of valid pairs as an integer.

        Time complexity:
            O(n log n), dominated by sorting.

        Space complexity:
            O(n) in Python due to sorting creating internal structures / copy usage.
        """
        # If there are fewer than 2 departure slots, no pair can be formed.
        # This is not strictly necessary because the loop would naturally return 0,
        # but keeping this guard makes the intent very clear for beginners.
        n: int = len(tides)
        if n < 2:
            return 0

        # Sorting is the key idea that makes the two-pointer approach possible.
        #
        # Why sort?
        # ----------
        # The condition involves absolute difference:
        #     |tides[i] - tides[j]| <= limit
        #
        # Once the values are sorted in non-decreasing order, for any i < j in the
        # sorted array we know:
        #     sorted_tides[j] >= sorted_tides[i]
        #
        # Therefore:
        #     |sorted_tides[j] - sorted_tides[i]| = sorted_tides[j] - sorted_tides[i]
        #
        # This removes the need to think about both directions of the absolute value.
        # More importantly, it creates a monotonic property:
        # if a certain right endpoint is too far from the left endpoint, then every
        # later right endpoint will also be too far.
        #
        # That monotonic behavior is exactly what allows a linear two-pointer scan.
        sorted_tides: List[int] = sorted(tides)

        # This variable will accumulate the total number of valid pairs.
        #
        # In Python, integers automatically grow as needed, so there is no overflow.
        # In languages like C++ or Java, this should be stored in a 64-bit type.
        total_pairs: int = 0

        # `right` is the moving upper boundary of the valid window.
        #
        # Invariant we maintain:
        # - For the current `left`, all indices in [left + 1, right - 1] are valid
        #   partners with `left`.
        # - Either `right == n`, or `sorted_tides[right] - sorted_tides[left] > limit`
        #   meaning `right` is the first invalid index.
        #
        # Important optimization:
        # We do NOT reset `right` back to `left + 1` for every new `left`.
        # Because the array is sorted, the valid window only moves forward.
        # This is what keeps the scan linear after sorting.
        right: int = 0

        # We try each position as the left endpoint of a pair.
        for left in range(n):
            # Ensure `right` is always at least one step ahead of `left`.
            #
            # Why is this needed?
            # -------------------
            # A pair requires two distinct indices, so the right endpoint must be
            # strictly greater than the left endpoint.
            #
            # Also, because `left` increases over time, `right` might lag behind.
            # This line keeps the window well-formed.
            if right < left + 1:
                right = left + 1

            # Expand `right` as far as possible while the pair difference remains valid.
            #
            # Since the array is sorted, as `right` moves forward, the difference
            # `sorted_tides[right] - sorted_tides[left]` never decreases.
            #
            # Therefore, once the condition fails, it will fail for all larger `right`
            # values too, so we stop immediately.
            while right < n and sorted_tides[right] - sorted_tides[left] <= limit:
                right += 1

            # At this point:
            # - indices from left + 1 up to right - 1 are valid
            # - index `right` is either out of bounds or invalid
            #
            # Number of valid partners for this `left` is:
            #     (right - 1) - (left + 1) + 1
            # which simplifies to:
            #     right - left - 1
            #
            # Example:
            # if left = 2 and right = 6, then valid indices are 3, 4, 5
            # count = 6 - 2 - 1 = 3
            total_pairs += right - left - 1

        return total_pairs

    def countPairs(self, tides: List[int], limit: int) -> int:
        """
        Compatibility wrapper that calls the main counting method.

        Args:
            tides: List of tide levels.
            limit: Maximum allowed absolute difference.

        Returns:
            Total number of valid pairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return self.count_valid_crossings(tides, limit)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solver = Solution()

    # Example 1 from the problem statement.
    #
    # Original:
    # tides = [4, 1, 7, 3, 5], limit = 2
    #
    # Sorted:
    # [1, 3, 4, 5, 7]
    #
    # Valid value-pairs:
    # (1, 3), (3, 4), (3, 5), (4, 5)
    # Total = 4
    tides1: List[int] = [4, 1, 7, 3, 5]
    limit1: int = 2
    result1: int = solver.count_valid_crossings(tides1, limit1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2 from the problem statement.
    #
    # tides = [8, 8, 8, 8], limit = 0
    #
    # Every pair is valid because all values are equal.
    # Number of pairs among 4 items = 4 * 3 / 2 = 6
    tides2: List[int] = [8, 8, 8, 8]
    limit2: int = 0
    result2: int = solver.count_valid_crossings(tides2, limit2)
    print("Example 2 Result:", result2)  # Expected: 6

    # Additional small sanity checks for beginner-friendly verification.

    # No valid pair because difference is too large.
    tides3: List[int] = [1, 10]
    limit3: int = 5
    result3: int = solver.count_valid_crossings(tides3, limit3)
    print("Additional Test 1 Result:", result3)  # Expected: 0

    # All pairs valid when limit is large enough.
    tides4: List[int] = [2, 4, 6]
    limit4: int = 10
    result4: int = solver.count_valid_crossings(tides4, limit4)
    print("Additional Test 2 Result:", result4)  # Expected: 3