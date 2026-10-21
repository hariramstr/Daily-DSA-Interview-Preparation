"""
Title: Minimum Adjacent Swaps to Cluster VIP Seats

Problem Description:
You are given a row of seats represented by a binary array `seats`, where `seats[i] = 1`
means seat `i` is occupied by a VIP guest and `seats[i] = 0` means it is occupied by a
regular guest. In one operation, you may swap two adjacent guests. Your goal is to make
all VIP guests sit in one contiguous block using the minimum total number of adjacent swaps.

Unlike simply counting empty seats between VIPs, each adjacent swap moves exactly one guest
by one position, so the true cost depends on how far each VIP must travel after choosing an
optimal final block. You must return the minimum number of adjacent swaps required.

If there are `0` or `1` VIP guests, the answer is `0`.

A correct hard solution is expected to exploit the structure of the VIP positions and avoid
simulating swaps. In particular, the optimal block is centered around the median VIP after
normalizing positions, and an efficient implementation should run in linear time after
collecting VIP indices.

Constraints:
- 1 <= seats.length <= 200000
- seats[i] is either 0 or 1
- The answer fits in a 64-bit signed integer

Example 1:
Input: seats = [1,0,0,1,0,1]
Output: 3
Explanation: VIPs are at positions [0,3,5]. After rearranging with adjacent swaps, they can
be grouped into seats [2,3,4] using 3 swaps in total.

Example 2:
Input: seats = [0,1,0,1,0,0,1,0,1]
Output: 5
Explanation: VIPs are at positions [1,3,6,8]. The best final contiguous block has length 4.
One optimal target is seats [3,4,5,6], which requires 5 adjacent swaps overall.
"""

from typing import List


class Solution:
    def min_adjacent_swaps_to_cluster_vips(self, seats: List[int]) -> int:
        """
        Compute the minimum number of adjacent swaps needed to make all VIP guests
        (represented by 1s) occupy one contiguous block.

        The key idea is:
        1. Collect the indices of all VIP seats.
        2. Convert those indices into "normalized positions" by subtracting the VIP order.
           If original VIP positions are p[0], p[1], ..., p[k-1], then define:
               adjusted[i] = p[i] - i
           This transformation removes the spacing that would naturally exist inside a
           contiguous block of length k.
        3. The minimum total movement is achieved by aligning all adjusted values to their
           median. This is a standard property of minimizing sum of absolute deviations.
        4. The resulting sum of absolute differences is exactly the minimum number of
           adjacent swaps.

        Args:
            seats: Binary array where 1 means VIP and 0 means regular guest.

        Returns:
            Minimum number of adjacent swaps required.

        Time complexity:
            O(n), where n is the length of seats.

        Space complexity:
            O(k), where k is the number of VIP guests.
        """
        # Step 1: Gather the indices of all VIP guests.
        # We only care about where the 1s are located, because regular guests are
        # interchangeable for the purpose of counting adjacent swaps.
        vip_positions: List[int] = []
        for index, value in enumerate(seats):
            if value == 1:
                vip_positions.append(index)

        # If there are 0 or 1 VIP guests, they are already trivially contiguous.
        vip_count: int = len(vip_positions)
        if vip_count <= 1:
            return 0

        # Step 2: Build the normalized / adjusted positions.
        #
        # Why do we subtract the VIP order index?
        # ---------------------------------------
        # Suppose we want the VIPs to end up in a contiguous block of length vip_count:
        #     start, start+1, start+2, ..., start+(vip_count-1)
        #
        # If the i-th VIP (in sorted order) moves to target seat start+i, then its movement is:
        #     abs(vip_positions[i] - (start + i))
        #
        # Rearranging:
        #     abs((vip_positions[i] - i) - start)
        #
        # So if we define:
        #     adjusted[i] = vip_positions[i] - i
        #
        # then the problem becomes:
        #     choose a single value "start" minimizing
        #     sum(abs(adjusted[i] - start))
        #
        # The value minimizing sum of absolute deviations is the median.
        adjusted: List[int] = []
        for order, position in enumerate(vip_positions):
            adjusted.append(position - order)

        # Step 3: Choose the median adjusted value.
        #
        # Because vip_positions is sorted and we subtract increasing order values,
        # adjusted remains sorted as well. Therefore, the median is simply the middle element.
        median_index: int = vip_count // 2
        median_value: int = adjusted[median_index]

        # Step 4: Sum absolute distances to the median.
        #
        # This sum is exactly the minimum number of adjacent swaps required.
        #
        # Why is this the true adjacent-swap cost?
        # ----------------------------------------
        # Each adjacent swap moves one VIP by one seat relative to regular guests.
        # Therefore, total adjacent swaps equals total distance traveled by VIPs
        # when matched in sorted order to the target contiguous block.
        total_swaps: int = 0
        for value in adjusted:
            total_swaps += abs(value - median_value)

        return total_swaps

    def minMoves(self, seats: List[int]) -> int:
        """
        Alias method using a common interview-style name.

        Args:
            seats: Binary array where 1 means VIP and 0 means regular guest.

        Returns:
            Minimum number of adjacent swaps required.

        Time complexity:
            O(n), where n is the length of seats.

        Space complexity:
            O(k), where k is the number of VIP guests.
        """
        return self.min_adjacent_swaps_to_cluster_vips(seats)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    seats1: List[int] = [1, 0, 0, 1, 0, 1]
    result1: int = solution.min_adjacent_swaps_to_cluster_vips(seats1)
    print("Example 1:")
    print("Input:", seats1)
    print("Output:", result1)
    print("Expected:", 3)
    print()

    # Example 2
    seats2: List[int] = [0, 1, 0, 1, 0, 0, 1, 0, 1]
    result2: int = solution.min_adjacent_swaps_to_cluster_vips(seats2)
    print("Example 2:")
    print("Input:", seats2)
    print("Output:", result2)
    print("Expected:", 5)
    print()

    # Additional simple checks
    seats3: List[int] = [0, 0, 0, 0]
    result3: int = solution.min_adjacent_swaps_to_cluster_vips(seats3)
    print("Additional Check 1:")
    print("Input:", seats3)
    print("Output:", result3)
    print("Expected:", 0)
    print()

    seats4: List[int] = [0, 1, 0, 0]
    result4: int = solution.min_adjacent_swaps_to_cluster_vips(seats4)
    print("Additional Check 2:")
    print("Input:", seats4)
    print("Output:", result4)
    print("Expected:", 0)
    print()

    seats5: List[int] = [1, 0, 1, 0, 1]
    result5: int = solution.min_adjacent_swaps_to_cluster_vips(seats5)
    print("Additional Check 3:")
    print("Input:", seats5)
    print("Output:", result5)
    print("Expected:", 2)