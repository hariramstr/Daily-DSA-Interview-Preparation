"""
Title: Minimum Router Radius for Highway Emergency Phones

Problem Description:
A transportation agency is installing wireless routers along a straight highway to
provide service to emergency phones. The highway is modeled as a number line.
You are given two integer arrays: `phones`, where `phones[i]` is the position of
the i-th emergency phone, and `routers`, where `routers[j]` is the position of
the j-th router installation point.

A router with signal radius `r` covers every phone whose distance from that
router is at most `r`.

Your task is to return the minimum integer radius `r` such that every emergency
phone is covered by at least one router.

The arrays are not guaranteed to be sorted. Positions may be negative, and
multiple phones or routers may share the same position. You should design an
efficient solution that scales to large inputs. A brute-force comparison of every
phone against every router will be too slow.

A common approach is to sort the router positions and, for each phone, use binary
search to find the nearest router on the left or right. The answer is the maximum
among the minimum distances from each phone to its closest router.

Constraints:
- 1 <= phones.length, routers.length <= 2 * 10^5
- -10^9 <= phones[i], routers[j] <= 10^9
- The result fits in a 32-bit signed integer

Example 1:
Input: phones = [2, 10, 15], routers = [1, 5, 14]
Output: 4

Explanation:
- Phone 2 is closest to router 1, distance = 1
- Phone 10 is closest to router 14, distance = 4
- Phone 15 is closest to router 14, distance = 1
The maximum among these minimum distances is 4, so the minimum required radius is 4.

Example 2:
Input: phones = [-8, -3, 0, 7], routers = [-10, 2]
Output: 5

Explanation:
The closest-router distances are:
- Phone -8 -> distance 2
- Phone -3 -> distance 5
- Phone 0  -> distance 2
- Phone 7  -> distance 5
The maximum among these is 5, so the minimum required radius is 5.
"""

from bisect import bisect_left
from typing import List


class Solution:
    def find_radius(self, phones: List[int], routers: List[int]) -> int:
        """
        Compute the minimum integer radius needed so every phone is covered
        by at least one router.

        Args:
            phones: Positions of emergency phones on the number line.
            routers: Positions of routers on the number line.

        Returns:
            The minimum integer radius that covers all phones.

        Time complexity:
            O(m log m + n log m)
            where n = len(phones), m = len(routers)

        Space complexity:
            O(m) if counting the sorted copy of routers,
            or O(1) extra beyond sorting implementation details if sorting in place.
        """
        # We sort router positions once so that we can perform binary search.
        # Binary search is the key optimization here:
        # instead of comparing each phone to every router (which would be too slow),
        # we quickly locate where the phone would fit among sorted routers.
        sorted_routers: List[int] = sorted(routers)

        # This variable will store the final answer.
        # For each phone, we compute the distance to its nearest router.
        # The required radius must be large enough to cover the "worst" phone,
        # meaning the phone whose nearest router is farthest away.
        minimum_required_radius: int = 0

        # Process each phone independently.
        for phone_position in phones:
            # bisect_left returns the insertion index where phone_position could be
            # inserted while keeping sorted_routers in sorted order.
            #
            # This is extremely useful because:
            # - The router just to the left of this index may be the closest router.
            # - The router at this index (the first router >= phone_position) may also
            #   be the closest router.
            #
            # So we only need to check at most two routers for each phone.
            insertion_index: int = bisect_left(sorted_routers, phone_position)

            # Start with "infinite" distance so any real candidate distance will be smaller.
            nearest_distance: int = float("inf")

            # Check the router on the left side, if it exists.
            # This router is at index insertion_index - 1.
            if insertion_index > 0:
                left_router_position: int = sorted_routers[insertion_index - 1]
                distance_to_left: int = abs(phone_position - left_router_position)
                nearest_distance = min(nearest_distance, distance_to_left)

            # Check the router on the right side, if it exists.
            # This router is at index insertion_index.
            if insertion_index < len(sorted_routers):
                right_router_position: int = sorted_routers[insertion_index]
                distance_to_right: int = abs(right_router_position - phone_position)
                nearest_distance = min(nearest_distance, distance_to_right)

            # The answer must be large enough for this phone too.
            # Therefore, we take the maximum nearest distance seen so far.
            minimum_required_radius = max(minimum_required_radius, nearest_distance)

        return minimum_required_radius


if __name__ == "__main__":
    solution = Solution()

    phones_1: List[int] = [2, 10, 15]
    routers_1: List[int] = [1, 5, 14]
    result_1: int = solution.find_radius(phones_1, routers_1)
    print("Example 1 Result:", result_1)  # Expected: 4

    phones_2: List[int] = [-8, -3, 0, 7]
    routers_2: List[int] = [-10, 2]
    result_2: int = solution.find_radius(phones_2, routers_2)
    print("Example 2 Result:", result_2)  # Expected: 5