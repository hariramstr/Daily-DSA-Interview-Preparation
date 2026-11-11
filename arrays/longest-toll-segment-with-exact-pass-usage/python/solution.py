"""
Title: Longest Toll Segment With Exact Pass Usage

Problem Description:
You are given an integer array `roads` where `roads[i]` is the toll cost of the
`i`-th road segment on a highway trip. You also have a special pass that can be
used to make any segment with toll cost less than or equal to `passLimit` free.
However, the pass must be used on exactly `k` segments within one contiguous part
of the trip.

Your task is to find the length of the longest contiguous subarray such that
within that subarray, there are exactly `k` elements whose value is less than or
equal to `passLimit`.

Return the maximum possible length of such a subarray. If no valid subarray
exists, return `0`.

This problem models finding the longest stretch of a route where your pass is
used an exact number of times. Note that the pass can only be applied to segments
that qualify, and every qualifying segment inside the chosen subarray counts
toward the total. Segments with toll cost greater than `passLimit` do not use
the pass.

Constraints:
- 1 <= roads.length <= 100000
- 0 <= roads[i] <= 1000000000
- 0 <= passLimit <= 1000000000
- 0 <= k <= roads.length

Examples:
1)
Input: roads = [8, 2, 5, 1, 9, 3, 7], passLimit = 3, k = 2
Output: 5

2)
Input: roads = [4, 6, 8, 10], passLimit = 5, k = 1
Output: 4
"""

from typing import List


class Solution:
    def longest_toll_segment_with_exact_pass_usage(
        self, roads: List[int], passLimit: int, k: int
    ) -> int:
        """
        Find the maximum length of a contiguous subarray containing exactly k
        values less than or equal to passLimit.

        Args:
            roads: List of toll costs for road segments.
            passLimit: Maximum toll value that qualifies for pass usage.
            k: Exact number of qualifying segments required in the chosen subarray.

        Returns:
            The length of the longest contiguous subarray with exactly k qualifying
            values. Returns 0 if no such subarray exists.

        Time complexity:
            O(n), where n is the length of roads. Each pointer moves at most n times.

        Space complexity:
            O(1), excluding input storage.
        """
        # We use the classic sliding window technique because:
        # 1. We need a contiguous subarray.
        # 2. We need an exact count of "qualifying" elements.
        # 3. We want linear time.
        #
        # A "qualifying" element is any road toll <= passLimit.
        #
        # The key idea:
        # - Expand the right end of the window one step at a time.
        # - Track how many qualifying elements are currently inside the window.
        # - If the count becomes too large (> k), move the left end forward until
        #   the window becomes valid again (count <= k).
        # - Whenever the count is exactly k, try to maximize the window length.
        #
        # There is one subtle but very important detail:
        # Once the window has exactly k qualifying elements, we can often move the
        # left pointer across NON-qualifying elements without changing the count.
        # Those extra non-qualifying elements at the front only make the window
        # longer if they are included, so for maximizing length we actually want
        # the earliest possible left boundary that still keeps count <= k.
        #
        # The standard sliding window naturally gives us that:
        # we only move left when count > k, never when count == k.
        #
        # Therefore, every time count == k, the current window [left, right] is
        # the longest valid window ending at 'right'.

        left: int = 0
        qualifying_count: int = 0
        max_length: int = 0

        # Iterate through the array with 'right' as the expanding boundary.
        for right in range(len(roads)):
            # If the new element qualifies for pass usage, include it in the count.
            if roads[right] <= passLimit:
                qualifying_count += 1

            # If we now have too many qualifying elements, the window is invalid.
            # We must shrink from the left until the window has at most k
            # qualifying elements again.
            while qualifying_count > k and left <= right:
                # Before moving left forward, remove the effect of roads[left]
                # from the window.
                if roads[left] <= passLimit:
                    qualifying_count -= 1
                left += 1

            # At this point, the window has at most k qualifying elements.
            # If it has exactly k, then it is a valid candidate.
            if qualifying_count == k:
                current_length: int = right - left + 1
                if current_length > max_length:
                    max_length = current_length

        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    roads1: List[int] = [8, 2, 5, 1, 9, 3, 7]
    pass_limit1: int = 3
    k1: int = 2
    result1: int = solution.longest_toll_segment_with_exact_pass_usage(
        roads1, pass_limit1, k1
    )
    print(result1)  # Expected: 5

    # Example 2
    roads2: List[int] = [4, 6, 8, 10]
    pass_limit2: int = 5
    k2: int = 1
    result2: int = solution.longest_toll_segment_with_exact_pass_usage(
        roads2, pass_limit2, k2
    )
    print(result2)  # Expected: 4

    # Additional quick checks
    roads3: List[int] = [1, 2, 3]
    pass_limit3: int = 2
    k3: int = 0
    result3: int = solution.longest_toll_segment_with_exact_pass_usage(
        roads3, pass_limit3, k3
    )
    print(result3)  # Expected: 1, subarray [3]

    roads4: List[int] = [1, 1, 1]
    pass_limit4: int = 1
    k4: int = 2
    result4: int = solution.longest_toll_segment_with_exact_pass_usage(
        roads4, pass_limit4, k4
    )
    print(result4)  # Expected: 2