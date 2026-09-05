"""
Title: Minimum Lane Fixes to Make Traffic Speeds Nondecreasing

Problem Description:
A city records the average vehicle speed for each lane segment along a highway during a
short time window. The speeds are stored in an integer array `speeds`, where
`speeds[i]` is the measured speed at segment `i` from west to east.

Because of sensor noise, the recorded speeds may go down and up unpredictably.
Traffic engineers want the final reported sequence to be nondecreasing, meaning
`final[i] <= final[i + 1]` for every valid `i`.

To correct the data, they are allowed to apply lane fixes. In one lane fix, they may
choose a single segment and increase its speed by any positive amount. Decreasing
values is not allowed.

Return the minimum total added speed needed to make the entire array nondecreasing.

Your task is only to compute the minimum total increase, not the resulting array.

Constraints:
- 1 <= speeds.length <= 100000
- 0 <= speeds[i] <= 1000000000
- The answer can be larger than 32-bit integer range, so use 64-bit arithmetic.

Example 1:
Input: speeds = [5, 3, 3, 7, 2]
Output: 9

Example 2:
Input: speeds = [1, 2, 4, 4, 6]
Output: 0

A solution that runs in linear time is expected.
"""

from typing import List


class Solution:
    def min_total_increase(self, speeds: List[int]) -> int:
        """
        Compute the minimum total amount that must be added to the array so that
        it becomes nondecreasing.

        The idea is greedy:
        - Walk from left to right.
        - Keep track of the minimum value the current element must have so the
          sequence stays nondecreasing.
        - If the current value is smaller than that required value, we must raise it.
        - If it is already large enough, no increase is needed, and it becomes the
          new required minimum for future elements.

        Args:
            speeds: List of recorded speeds for highway segments.

        Returns:
            The minimum total added speed required to make the array nondecreasing.

        Time complexity:
            O(n), where n is the length of speeds, because we scan the array once.

        Space complexity:
            O(1), because we use only a few extra variables.
        """
        # This variable stores the running total of all increases we make.
        # Python integers automatically support large values, which is important
        # because the total can exceed 32-bit integer range.
        total_increase: int = 0

        # `prev_final` represents the value that the previous position ends up with
        # in the corrected nondecreasing array.
        #
        # Why do we track this?
        # Because for the current element to keep the array nondecreasing, its final
        # value must be at least `prev_final`.
        #
        # At the first element, nothing is before it, so it can stay as it is.
        prev_final: int = speeds[0]

        # Process every element starting from index 1, because index 0 never needs
        # adjustment due to any earlier element.
        for i in range(1, len(speeds)):
            current_speed: int = speeds[i]

            # If the current speed is already at least the previous final value,
            # then it does not break the nondecreasing order.
            #
            # Example:
            # previous final = 5, current = 7
            # Since 7 >= 5, we can leave it unchanged.
            if current_speed >= prev_final:
                # No increase is needed.
                # This current value now becomes the new "previous final" for the
                # next iteration, because future values must be at least this large.
                prev_final = current_speed
            else:
                # Otherwise, current_speed < prev_final, which means the sequence
                # would decrease here.
                #
                # Since we are only allowed to increase values, the best possible
                # action is to raise the current value exactly up to `prev_final`.
                #
                # Why exactly `prev_final`?
                # - Raising it less would still violate nondecreasing order.
                # - Raising it more would be unnecessary and would only increase
                #   the total cost.
                needed_increase: int = prev_final - current_speed
                total_increase += needed_increase

                # After increasing, the current position's final value becomes
                # exactly `prev_final`.
                #
                # We do not need to change `prev_final` here, because the corrected
                # current value equals it.
                #
                # Example:
                # prev_final = 5, current = 3
                # increase by 2 -> corrected current becomes 5
                # prev_final remains 5 for the next step

        return total_increase

    def minOperations(self, speeds: List[int]) -> int:
        """
        Compatibility wrapper method that calls the main algorithm.

        This method is included so the solution still feels familiar to platforms
        that expect a standard method name, while keeping the descriptive method
        name `min_total_increase`.

        Args:
            speeds: List of recorded speeds for highway segments.

        Returns:
            The minimum total added speed required to make the array nondecreasing.

        Time complexity:
            O(n), where n is the length of speeds.

        Space complexity:
            O(1).
        """
        return self.min_total_increase(speeds)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    # Trace:
    # [5, 3, 3, 7, 2]
    # 3 -> 5 (+2)
    # 3 -> 5 (+2)
    # 7 stays 7 (+0)
    # 2 -> 7 (+5)
    # Total = 9
    speeds1: List[int] = [5, 3, 3, 7, 2]
    result1: int = solution.min_total_increase(speeds1)
    print(f"Input: {speeds1}")
    print(f"Output: {result1}")
    print("Expected: 9")
    print()

    # Sample input 2 from the problem statement.
    # Already nondecreasing, so no increase is needed.
    speeds2: List[int] = [1, 2, 4, 4, 6]
    result2: int = solution.min_total_increase(speeds2)
    print(f"Input: {speeds2}")
    print(f"Output: {result2}")
    print("Expected: 0")
    print()

    # Additional small beginner-friendly checks.
    extra_tests: List[List[int]] = [
        [10],
        [4, 1],
        [0, 0, 0],
        [3, 2, 1],
        [1, 3, 2, 2, 5],
    ]

    for test in extra_tests:
        print(f"Input: {test}")
        print(f"Minimum total increase: {solution.min_total_increase(test)}")
        print()