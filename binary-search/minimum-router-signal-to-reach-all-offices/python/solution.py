"""
Title: Minimum Router Signal to Reach All Offices

Problem Description:
A company has opened offices along a straight highway. The positions of the offices
are given in a sorted integer array `offices`, where `offices[i]` is the kilometer
marker of the `i`-th office. You may install exactly `k` Wi-Fi routers. Every router
uses the same signal strength `r`, and a router placed at position `x` covers all
offices whose positions lie in the inclusive range `[x - r, x + r]`.

You may place routers at any real-valued position, not necessarily at an office
location. Your task is to compute the minimum integer signal strength `r` needed so
that all offices are covered using at most `k` routers.

This is an optimization problem: for a fixed signal strength, you must determine
whether it is possible to cover all offices with `k` or fewer routers, and then find
the smallest feasible value.

Return the minimum integer `r`.

Constraints:
- 1 <= offices.length <= 100000
- 1 <= k <= offices.length
- 0 <= offices[i] <= 1000000000
- offices is sorted in non-decreasing order
"""

from typing import List


class Solution:
    def can_cover(self, offices: List[int], k: int, r: int) -> bool:
        """
        Determine whether all offices can be covered using at most k routers
        when every router has signal strength r.

        Args:
            offices: Sorted list of office positions along the highway.
            k: Maximum number of routers allowed.
            r: Candidate integer signal strength to test.

        Returns:
            True if all offices can be covered with at most k routers, otherwise False.

        Time complexity:
            O(n), where n is the number of offices.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We will greedily place routers from left to right.
        #
        # Why greedy works:
        # - Suppose the leftmost uncovered office is at position p.
        # - To cover p, the router center x must satisfy x - r <= p, which means x <= p + r.
        # - To maximize how far to the right this router can cover, we should place it as far
        #   right as possible while still covering p, namely at x = p + r.
        # - That router then covers up to x + r = p + 2r.
        #
        # This is optimal for each step because it covers the current leftmost uncovered office
        # and extends coverage as far right as possible, never hurting future choices.

        n: int = len(offices)
        routers_used: int = 0
        i: int = 0

        # Process offices until every office is covered.
        while i < n:
            # We must place a new router to cover offices[i], because it is currently uncovered.
            routers_used += 1

            # If we already used too many routers, this signal strength is not feasible.
            if routers_used > k:
                return False

            # Let p be the leftmost uncovered office.
            leftmost_uncovered: int = offices[i]

            # Best router placement:
            # Place the router center at leftmost_uncovered + r.
            # Then the rightmost point covered becomes leftmost_uncovered + 2*r.
            coverage_end: int = leftmost_uncovered + 2 * r

            # Skip every office whose position is within this router's coverage.
            while i < n and offices[i] <= coverage_end:
                i += 1

        # If we finished scanning all offices using at most k routers, coverage is possible.
        return True

    def min_signal_strength(self, offices: List[int], k: int) -> int:
        """
        Compute the minimum integer signal strength needed to cover all offices
        using at most k routers.

        Args:
            offices: Sorted list of office positions along the highway.
            k: Maximum number of routers allowed.

        Returns:
            The smallest integer signal strength r that makes full coverage possible.

        Time complexity:
            O(n log D), where n is the number of offices and
            D = offices[-1] - offices[0].

        Space complexity:
            O(1), ignoring input storage.
        """
        # Edge case:
        # If there is only one office, signal strength 0 is enough because we can place
        # a router exactly at that office.
        if len(offices) <= 1:
            return 0

        # Binary search over the answer.
        #
        # Why binary search applies:
        # - If a signal strength r is feasible, then any larger signal strength is also feasible.
        # - This creates a monotonic property:
        #       False False False ... True True True
        # - Therefore, we can search for the first True.
        #
        # Lower bound:
        # - 0 is always the smallest possible integer signal strength.
        #
        # Upper bound:
        # - offices[-1] - offices[0] is always enough.
        #   In the worst case, one router centered in the middle can cover the whole range
        #   if r is large enough, and certainly this value is a safe upper bound.
        left: int = 0
        right: int = offices[-1] - offices[0]

        # Standard binary search for the minimum feasible value.
        while left < right:
            mid: int = (left + right) // 2

            # Test whether this candidate signal strength can cover all offices.
            if self.can_cover(offices, k, mid):
                # mid works, so the answer is mid or smaller.
                right = mid
            else:
                # mid does not work, so we need a larger signal strength.
                left = mid + 1

        # At loop end, left == right and points to the minimum feasible signal strength.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    offices1: List[int] = [1, 2, 8, 12, 17]
    k1: int = 2
    result1: int = solution.min_signal_strength(offices1, k1)
    print("Example 1:")
    print(f"Offices = {offices1}, k = {k1}")
    print(f"Minimum signal strength = {result1}")
    print("Expected = 4")
    print()

    # Example 2
    offices2: List[int] = [0, 4, 9, 15]
    k2: int = 3
    result2: int = solution.min_signal_strength(offices2, k2)
    print("Example 2:")
    print(f"Offices = {offices2}, k = {k2}")
    print(f"Minimum signal strength = {result2}")
    print("Expected = 2")
    print()

    # Additional quick checks
    offices3: List[int] = [5]
    k3: int = 1
    result3: int = solution.min_signal_strength(offices3, k3)
    print("Additional Check 1:")
    print(f"Offices = {offices3}, k = {k3}")
    print(f"Minimum signal strength = {result3}")
    print("Expected = 0")
    print()

    offices4: List[int] = [1, 3, 5, 7]
    k4: int = 4
    result4: int = solution.min_signal_strength(offices4, k4)
    print("Additional Check 2:")
    print(f"Offices = {offices4}, k = {k4}")
    print(f"Minimum signal strength = {result4}")
    print("Expected = 0")