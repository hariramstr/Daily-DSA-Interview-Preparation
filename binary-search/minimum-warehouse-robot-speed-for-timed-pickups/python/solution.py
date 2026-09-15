"""
Title: Minimum Warehouse Robot Speed for Timed Pickups
Difficulty: Medium
Topic: Binary Search

Problem Description:
A warehouse robot must collect items from several aisles in a fixed order. You are given
an integer array distances, where distances[i] is the length of aisle i in meters, and
an integer array deadlines, where deadlines[i] is the latest time in seconds by which the
robot must finish aisle i.

The robot moves at a constant integer speed s meters per second for the entire route.
The time to finish aisle i is the cumulative time spent traversing aisles 0 through i,
which is the sum of distances[j] / s for all j from 0 to i. The robot is considered on
time only if for every i, the cumulative time is less than or equal to deadlines[i].

Return the minimum integer speed s such that the robot can finish every aisle by its
corresponding deadline. If no positive integer speed can satisfy all deadlines, return -1.

This is a decision-and-search problem: for a candidate speed, you can check whether all
cumulative deadlines are met, and the feasibility is monotonic. If a speed works, any
larger speed also works.

Constraints:
- 1 <= distances.length == deadlines.length <= 100000
- 1 <= distances[i] <= 1000000
- 1 <= deadlines[i] <= 1000000000
- Speed s must be a positive integer

Example 1:
Input: distances = [4, 3, 6], deadlines = [2, 4, 7]
Output: 2

Example 2:
Input: distances = [5, 8, 4], deadlines = [1, 2, 3]
Output: -1
"""

from typing import List


class Solution:
    def _can_finish(self, distances: List[int], deadlines: List[int], speed: int) -> bool:
        """
        Check whether a given integer speed allows the robot to meet every cumulative deadline.

        Args:
            distances: List of aisle lengths.
            deadlines: List of latest allowed cumulative finish times.
            speed: Candidate robot speed in meters per second.

        Returns:
            True if the robot can finish every aisle on or before its deadline, otherwise False.

        Time complexity:
            O(n), where n is the number of aisles.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We avoid floating-point arithmetic entirely.
        #
        # Why?
        # - Floating-point comparisons can introduce precision issues.
        # - The condition
        #       (distances[0] + distances[1] + ... + distances[i]) / speed <= deadlines[i]
        #   is equivalent to
        #       prefix_distance <= deadlines[i] * speed
        # - All values are integers, so integer arithmetic is exact and safe in Python.
        prefix_distance: int = 0

        # Walk through aisles in order because the robot must traverse them in fixed sequence.
        for i in range(len(distances)):
            # Add the current aisle length to the cumulative traveled distance.
            prefix_distance += distances[i]

            # If cumulative distance exceeds what can be covered by this deadline at the
            # current speed, then this speed is not feasible.
            #
            # Mathematical transformation:
            # cumulative_time = prefix_distance / speed
            # Need cumulative_time <= deadlines[i]
            # Therefore prefix_distance <= deadlines[i] * speed
            if prefix_distance > deadlines[i] * speed:
                return False

        # If every aisle satisfied its cumulative deadline, the speed works.
        return True

    def min_robot_speed(self, distances: List[int], deadlines: List[int]) -> int:
        """
        Find the minimum positive integer speed that satisfies all cumulative deadlines.

        Args:
            distances: List of aisle lengths.
            deadlines: List of latest allowed cumulative finish times.

        Returns:
            The minimum feasible positive integer speed, or -1 if no such speed exists.

        Time complexity:
            O(n log U), where n is the number of aisles and U is the searched speed range.

        Space complexity:
            O(1), ignoring input storage.
        """
        n: int = len(distances)

        # Step 1: Quick impossibility check using the "infinite speed" idea.
        #
        # Even if speed becomes arbitrarily large, the time to finish aisle i approaches:
        #   number_of_completed_aisles * 0+   (strictly positive for each aisle, but can be
        #   made arbitrarily small)
        #
        # Under the exact mathematical model in this problem, there is no mandatory waiting,
        # rounding, or per-aisle fixed overhead. So unlike some train/deadline problems,
        # there is no discrete lower bound such as "must spend at least 1 unit per aisle".
        #
        # Therefore, if deadlines are positive, sufficiently large speed can always make
        # cumulative times arbitrarily small.
        #
        # Since constraints guarantee deadlines[i] >= 1, a solution always exists.
        #
        # Still, we keep the implementation general and robust by searching for a valid upper
        # bound rather than assuming one immediately.
        if n == 0:
            return -1

        # Step 2: Establish a binary-search range [left, right].
        #
        # left starts at 1 because speed must be a positive integer.
        left: int = 1
        right: int = 1

        # Step 3: Expand the upper bound until it becomes feasible.
        #
        # Why do this?
        # - Binary search needs a range where:
        #       left side may be infeasible
        #       right side is definitely feasible
        # - We may not know a safe upper bound in advance.
        # - Doubling right repeatedly finds one in O(log answer) expansions.
        #
        # Because feasibility is monotonic:
        # - if a speed works, any larger speed also works
        # - once we find a working right, the answer lies in [1, right]
        while not self._can_finish(distances, deadlines, right):
            right *= 2

            # This guard is mostly defensive. In Python integers do not overflow, but if
            # somehow no feasible speed existed, this loop could continue forever.
            #
            # For this problem's mathematical model and constraints, a feasible speed does
            # exist, so this branch should never be reached in valid inputs.
            if right > 10**30:
                return -1

        # Step 4: Standard binary search for the minimum feasible speed.
        #
        # Invariant:
        # - There exists at least one feasible speed in [left, right]
        # - We shrink the range until left == right
        while left < right:
            # Midpoint speed to test.
            mid: int = left + (right - left) // 2

            # If mid works, then the minimum answer is in [left, mid].
            if self._can_finish(distances, deadlines, mid):
                right = mid
            else:
                # If mid does not work, then all speeds <= mid also do not work
                # because of monotonicity. So the answer must be in [mid + 1, right].
                left = mid + 1

        # At loop end, left == right and points to the smallest feasible speed.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    distances_1: List[int] = [4, 3, 6]
    deadlines_1: List[int] = [2, 4, 7]
    result_1: int = solution.min_robot_speed(distances_1, deadlines_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2
    #
    # Important note:
    # Under the exact mathematical model stated in this problem, the correct answer for
    # this example is actually 6, not -1.
    #
    # Check:
    # speed = 6
    # cumulative times:
    #   5/6   = 0.8333... <= 1
    #   13/6  = 2.1666... > 2   -> fails
    #
    # speed = 7
    #   5/7   = 0.7142... <= 1
    #   13/7  = 1.8571... <= 2
    #   17/7  = 2.4285... <= 3
    # So 7 works, and therefore the example's stated output of -1 is inconsistent with
    # the problem definition.
    #
    # The implemented algorithm follows the mathematical definition exactly.
    distances_2: List[int] = [5, 8, 4]
    deadlines_2: List[int] = [1, 2, 3]
    result_2: int = solution.min_robot_speed(distances_2, deadlines_2)
    print("Example 2 Result:", result_2)

    # Additional quick sanity check
    distances_3: List[int] = [1]
    deadlines_3: List[int] = [1]
    result_3: int = solution.min_robot_speed(distances_3, deadlines_3)
    print("Additional Example Result:", result_3)  # Expected: 1