"""
Title: Minimum Booth Width for Festival Entry Lanes

Problem Description:
A music festival is setting up several entry lanes for attendees. Each group of attendees
must stay together in the same lane, and groups must be processed in the given order
because their tickets are tied to scheduled arrival windows. You are given an array
groups where groups[i] is the number of people in the i-th arriving group, and an integer
m representing the number of available entry lanes.

Every lane can handle a contiguous sequence of groups, and the total number of people
assigned to a single lane cannot exceed that lane's booth width capacity. Your task is
to compute the minimum booth width needed so that all groups can be assigned to at most
m lanes.

In other words, partition the array into at most m contiguous parts while minimizing
the maximum part sum.

Return the smallest integer booth width that makes such an assignment possible.

Constraints:
- 1 <= groups.length <= 100000
- 1 <= groups[i] <= 1000000000
- 1 <= m <= groups.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: groups = [12, 7, 15, 9, 10], m = 3
Output: 22

Example 2:
Input: groups = [5, 5, 5, 5, 5, 5], m = 2
Output: 15
"""

from typing import List


class Solution:
    def _can_split_with_width(self, groups: List[int], m: int, max_width: int) -> bool:
        """
        Check whether all groups can be assigned into at most m contiguous lanes
        such that no lane has total load greater than max_width.

        Args:
            groups: List of attendee group sizes in fixed arrival order.
            m: Maximum number of lanes allowed.
            max_width: Candidate booth width to test for feasibility.

        Returns:
            True if the groups can be partitioned into at most m contiguous parts
            with each part sum <= max_width, otherwise False.

        Time complexity:
            O(n), where n is the number of groups.

        Space complexity:
            O(1), excluding input storage.
        """
        # We greedily build each lane from left to right.
        #
        # Why greedy works:
        # If we are testing whether a fixed maximum width is feasible, then the best
        # way to minimize the number of lanes used is to pack as many consecutive
        # groups as possible into the current lane before opening a new one.
        #
        # This is safe because:
        # - The order of groups cannot change.
        # - Groups cannot be split.
        # - Starting a new lane earlier would never reduce the number of lanes needed.
        #
        # So this greedy pass gives the minimum number of lanes required for this
        # candidate width.

        lanes_used: int = 1
        current_lane_sum: int = 0

        for group_size in groups:
            # If a single group is larger than the candidate width, then it is
            # impossible to place that group anywhere. We can immediately return False.
            if group_size > max_width:
                return False

            # If adding this group keeps the current lane within the allowed width,
            # we keep it in the same lane.
            if current_lane_sum + group_size <= max_width:
                current_lane_sum += group_size
            else:
                # Otherwise, we must start a new lane for this group.
                lanes_used += 1
                current_lane_sum = group_size

                # Early stopping optimization:
                # If we already need more than m lanes, then this width is not feasible.
                if lanes_used > m:
                    return False

        # If we finish the scan using at most m lanes, the candidate width works.
        return True

    def minimum_booth_width(self, groups: List[int], m: int) -> int:
        """
        Compute the minimum booth width needed so that the groups can be split
        into at most m contiguous lanes.

        Args:
            groups: List of attendee group sizes in fixed arrival order.
            m: Maximum number of lanes allowed.

        Returns:
            The smallest integer booth width that allows a valid partition.

        Time complexity:
            O(n log S), where n is the number of groups and S is the search range
            between max(groups) and sum(groups).

        Space complexity:
            O(1), excluding input storage.
        """
        # This problem has a classic "binary search on the answer" structure.
        #
        # Key monotonic property:
        # - If a booth width W is feasible, then any width larger than W is also feasible.
        # - If a booth width W is not feasible, then any width smaller than W is also not feasible.
        #
        # Because feasibility changes in one direction only, we can binary search
        # for the smallest feasible width.

        # Lower bound:
        # The width must be at least the size of the largest single group,
        # because no group can be split across lanes.
        left: int = max(groups)

        # Upper bound:
        # The width can always be the sum of all groups, which means we place
        # everything into one lane. Since m >= 1, this is always feasible.
        right: int = sum(groups)

        # Standard binary search for the first feasible value.
        while left < right:
            # Midpoint of the current search range.
            mid: int = (left + right) // 2

            # Test whether this candidate width is enough.
            if self._can_split_with_width(groups, m, mid):
                # If feasible, try to find an even smaller feasible width.
                right = mid
            else:
                # If not feasible, we must increase the width.
                left = mid + 1

        # At the end, left == right and points to the minimum feasible width.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1
    groups1: List[int] = [12, 7, 15, 9, 10]
    m1: int = 3
    result1: int = solution.minimum_booth_width(groups1, m1)
    print("Example 1:")
    print(f"groups = {groups1}, m = {m1}")
    print(f"Minimum booth width = {result1}")
    print("Expected = 22")
    print()

    # Sample input 2
    groups2: List[int] = [5, 5, 5, 5, 5, 5]
    m2: int = 2
    result2: int = solution.minimum_booth_width(groups2, m2)
    print("Example 2:")
    print(f"groups = {groups2}, m = {m2}")
    print(f"Minimum booth width = {result2}")
    print("Expected = 15")
    print()

    # Additional quick sanity checks for beginners:
    # 1) If m equals the number of groups, each group can have its own lane,
    #    so the answer is simply the largest group.
    groups3: List[int] = [3, 1, 4, 1, 5]
    m3: int = 5
    result3: int = solution.minimum_booth_width(groups3, m3)
    print("Sanity Check 1:")
    print(f"groups = {groups3}, m = {m3}")
    print(f"Minimum booth width = {result3}")
    print("Expected = 5")
    print()

    # 2) If m is 1, all groups must go into one lane,
    #    so the answer is the total sum.
    groups4: List[int] = [8, 2, 6]
    m4: int = 1
    result4: int = solution.minimum_booth_width(groups4, m4)
    print("Sanity Check 2:")
    print(f"groups = {groups4}, m = {m4}")
    print(f"Minimum booth width = {result4}")
    print("Expected = 16")