"""
Title: Maximum Uniform Banner Width for Ad Slots

Problem Description:
You are given an array `slots` where `slots[i]` is the width of the `i`-th advertising
space available on a website. A design team wants to create banner creatives of one
uniform integer width `w`, and each slot can be split into multiple banners as long as
every produced banner has width exactly `w`. Any leftover width in a slot is discarded
and cannot be combined with leftover width from another slot.

Given an integer `k`, return the maximum possible integer banner width `w` such that
the total number of banners produced across all slots is at least `k`. If it is
impossible to produce `k` banners even with width `1`, return `0`.

Formally, for a chosen width `w`, slot `i` contributes `floor(slots[i] / w)` banners.
You must find the largest `w` for which the sum of these values over all slots is at
least `k`.

This problem is intended to be solved efficiently for large inputs. A linear scan over
all possible widths will be too slow when slot widths are large, so you should take
advantage of the monotonic relationship between banner width and the number of banners
that can be produced.

Constraints:
- 1 <= slots.length <= 100000
- 1 <= slots[i] <= 1000000000
- 1 <= k <= 1000000000

Example 1:
Input: slots = [9, 7, 5], k = 5
Output: 3
Explanation:
- Width 3 produces:
  - 9 // 3 = 3
  - 7 // 3 = 2
  - 5 // 3 = 1
  Total = 6, which is enough.
- Width 4 produces:
  - 9 // 4 = 2
  - 7 // 4 = 1
  - 5 // 4 = 1
  Total = 4, which is not enough.
So the maximum valid width is 3.

Example 2:
Input: slots = [2, 3], k = 10
Output: 0
Explanation:
Even with width 1, total banners = 2 // 1 + 3 // 1 = 5, which is less than 10.
So it is impossible to produce 10 banners.
"""

from typing import List


class Solution:
    def _can_make_at_least_k_banners(self, slots: List[int], k: int, width: int) -> bool:
        """
        Check whether a given banner width can produce at least k banners.

        Args:
            slots: List of available slot widths.
            k: Required minimum number of banners.
            width: Candidate uniform banner width to test.

        Returns:
            True if the total number of banners produced is at least k, otherwise False.

        Time complexity:
            O(n), where n is the number of slots.

        Space complexity:
            O(1), ignoring input storage.
        """
        # This variable will accumulate how many banners we can produce in total
        # using the candidate width.
        total_banners: int = 0

        # We inspect every slot independently because each slot contributes
        # floor(slot_width / banner_width) banners.
        for slot_width in slots:
            # Integer division tells us exactly how many full banners of this width
            # fit into the current slot. Any remainder is discarded automatically.
            total_banners += slot_width // width

            # Important optimization:
            # As soon as we already know we can make at least k banners, we can stop.
            # There is no need to continue counting because the caller only needs a
            # yes/no answer, not the exact total.
            if total_banners >= k:
                return True

        # If we finish the loop and still have fewer than k banners, this width fails.
        return False

    def maximum_uniform_banner_width(self, slots: List[int], k: int) -> int:
        """
        Find the maximum integer banner width that allows producing at least k banners.

        This uses binary search over the possible width values because:
        - If a width w is feasible, then every smaller width is also feasible.
        - If a width w is not feasible, then every larger width is also not feasible.
        This monotonic behavior makes binary search the correct efficient approach.

        Args:
            slots: List of available slot widths.
            k: Required minimum number of banners.

        Returns:
            The largest integer width w such that at least k banners can be produced.
            Returns 0 if producing k banners is impossible even with width 1.

        Time complexity:
            O(n log M), where:
            - n is the number of slots
            - M is the maximum slot width

        Space complexity:
            O(1), ignoring input storage.
        """
        # Before doing binary search, we first handle the impossible case.
        # If width = 1 cannot produce at least k banners, then no larger width can.
        #
        # Why?
        # Because increasing width never increases the number of banners produced.
        # So width 1 gives the absolute maximum possible count.
        if sum(slots) < k:
            return 0

        # The smallest possible valid width to consider is 1.
        left: int = 1

        # The largest possible width to consider is the largest slot width.
        # Any width larger than this would produce 0 banners from every slot.
        right: int = max(slots)

        # This variable stores the best feasible width found so far.
        # We update it whenever we discover a width that can produce at least k banners.
        best_width: int = 0

        # Standard binary search on the answer space.
        #
        # Invariant idea:
        # - We search within [left, right].
        # - Whenever mid is feasible, we record it and try to go larger.
        # - Whenever mid is not feasible, we must go smaller.
        while left <= right:
            # Midpoint of the current search range.
            # Using this formula avoids overflow in some languages, and is also
            # a good habit even though Python integers do not overflow here.
            mid: int = left + (right - left) // 2

            # Test whether this candidate width can produce at least k banners.
            if self._can_make_at_least_k_banners(slots, k, mid):
                # This width works, so it is a valid answer candidate.
                best_width = mid

                # Since we want the MAXIMUM valid width, we now try larger widths.
                left = mid + 1
            else:
                # This width does not work, so any larger width also cannot work.
                # Therefore, we shrink the search to smaller widths.
                right = mid - 1

        # After binary search finishes, best_width holds the largest feasible width.
        return best_width


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    slots1: List[int] = [9, 7, 5]
    k1: int = 5
    result1: int = solution.maximum_uniform_banner_width(slots1, k1)
    print("Example 1:")
    print(f"slots = {slots1}, k = {k1}")
    print(f"Maximum uniform banner width = {result1}")
    print("Expected = 3")
    print()

    # Example 2 from the problem statement
    slots2: List[int] = [2, 3]
    k2: int = 10
    result2: int = solution.maximum_uniform_banner_width(slots2, k2)
    print("Example 2:")
    print(f"slots = {slots2}, k = {k2}")
    print(f"Maximum uniform banner width = {result2}")
    print("Expected = 0")
    print()

    # Additional simple test
    slots3: List[int] = [8, 8, 8]
    k3: int = 6
    result3: int = solution.maximum_uniform_banner_width(slots3, k3)
    print("Additional Test:")
    print(f"slots = {slots3}, k = {k3}")
    print(f"Maximum uniform banner width = {result3}")
    print("Expected = 4")