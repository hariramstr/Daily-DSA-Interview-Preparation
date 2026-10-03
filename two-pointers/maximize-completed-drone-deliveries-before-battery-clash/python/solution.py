"""
Title: Maximize Completed Drone Deliveries Before Battery Clash

Problem Description:
A warehouse operates two launch pads for delivery drones. Each outgoing drone requires
exactly one battery pack, and each battery pack can be used at most once.

You are given two integer arrays:
- drones: drones[i] is the minimum charge required for the i-th drone
- batteries: batteries[j] is the available charge in the j-th battery pack

A drone can be launched only if it is assigned a battery pack with charge greater than
or equal to its required charge. Each battery can power at most one drone, and each
drone can receive at most one battery.

You may pair drones and batteries in any way you want. Return the maximum number of
valid pairings.

A correct and efficient solution uses sorting and a two-pointer strategy.

Constraints:
- 1 <= drones.length, batteries.length <= 2 * 10^5
- 1 <= drones[i], batteries[j] <= 10^9
- Arrays are not necessarily sorted

Examples:
1)
Input: drones = [4, 2, 7], batteries = [3, 8, 5]
Output: 2

2)
Input: drones = [1, 3, 3, 6], batteries = [2, 3, 4]
Output: 3
"""

from typing import List


class Solution:
    def max_completed_deliveries(self, drones: List[int], batteries: List[int]) -> int:
        """
        Compute the maximum number of drones that can be launched successfully.

        The strategy is:
        1. Sort both arrays in non-decreasing order.
        2. Use two pointers to greedily match the smallest remaining drone requirement
           with the smallest remaining battery that can satisfy it.
        3. If the current battery is too weak, move to the next battery.
        4. If the current battery can satisfy the current drone, count the match and
           move both pointers forward.

        This greedy approach is optimal because using the smallest sufficient battery
        for the smallest remaining drone preserves larger batteries for larger drones.

        Args:
            drones: A list of integers where each value is the minimum required charge
                for a drone.
            batteries: A list of integers where each value is the available charge in
                a battery pack.

        Returns:
            The maximum number of valid drone-battery pairings.

        Time complexity:
            O(n log n + m log m), where n is len(drones) and m is len(batteries),
            due to sorting.

        Space complexity:
            O(1) extra space beyond the space used by sorting implementations.
            Note: Python's sorting may use additional internal memory.
        """
        # Step 1:
        # Sort the drone requirements from smallest to largest.
        #
        # Why do this?
        # If we try to satisfy the smallest drone first, we can use the smallest battery
        # that works for it. This is a classic greedy idea:
        # - Small jobs should consume small resources whenever possible.
        # - That leaves stronger batteries available for drones that need more charge.
        drones.sort()

        # Step 2:
        # Sort the battery charges from smallest to largest for the same reason.
        #
        # Once both lists are sorted, we can scan them from left to right using
        # two pointers. This avoids checking every possible pairing, which would be
        # far too slow for large inputs.
        batteries.sort()

        # Pointer i tracks the current drone we are trying to satisfy.
        i: int = 0

        # Pointer j tracks the current battery we are considering.
        j: int = 0

        # This counter stores how many successful pairings we have made so far.
        completed: int = 0

        # Step 3:
        # Walk through both sorted arrays.
        #
        # We stop when either:
        # - we have considered all drones, or
        # - we have used/considered all batteries.
        #
        # At every step, we compare:
        # - drones[i]: the smallest remaining drone requirement
        # - batteries[j]: the smallest remaining battery charge
        while i < len(drones) and j < len(batteries):
            # Case A:
            # The current battery is strong enough for the current drone.
            #
            # Since both arrays are sorted, this battery is the smallest available one
            # that we are currently considering. Matching it now is safe and optimal.
            #
            # Why is this optimal?
            # Because using a larger battery for this same drone would be wasteful,
            # and could reduce our ability to satisfy larger drones later.
            if batteries[j] >= drones[i]:
                # We found one valid pairing.
                completed += 1

                # Move to the next drone, because the current drone has now been assigned
                # a battery and is complete.
                i += 1

                # Move to the next battery, because each battery can be used at most once.
                j += 1
            else:
                # Case B:
                # The current battery is too weak for the current drone.
                #
                # Because drones[i] is the smallest remaining drone requirement,
                # this battery also cannot satisfy any later drone (those later drones
                # require the same or more charge).
                #
                # Therefore, this battery is unusable for all remaining drones, so the
                # only sensible action is to skip it and try the next stronger battery.
                j += 1

        # After the loop finishes, 'completed' contains the maximum number of matches.
        return completed

    def findContentChildren(self, drones: List[int], batteries: List[int]) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        This method simply forwards the call to the main implementation.

        Args:
            drones: Minimum charge required for each drone.
            batteries: Available charge in each battery.

        Returns:
            The maximum number of valid pairings.

        Time complexity:
            O(n log n + m log m)

        Space complexity:
            O(1) extra space beyond sorting internals.
        """
        return self.max_completed_deliveries(drones, batteries)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    drones_1: List[int] = [4, 2, 7]
    batteries_1: List[int] = [3, 8, 5]
    result_1: int = solution.max_completed_deliveries(drones_1[:], batteries_1[:])
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2 from the problem statement.
    drones_2: List[int] = [1, 3, 3, 6]
    batteries_2: List[int] = [2, 3, 4]
    result_2: int = solution.max_completed_deliveries(drones_2[:], batteries_2[:])
    print("Example 2 Result:", result_2)  # Expected: 3

    # Additional simple check.
    drones_3: List[int] = [5, 5, 5]
    batteries_3: List[int] = [1, 2, 3]
    result_3: int = solution.max_completed_deliveries(drones_3[:], batteries_3[:])
    print("Additional Example Result:", result_3)  # Expected: 0