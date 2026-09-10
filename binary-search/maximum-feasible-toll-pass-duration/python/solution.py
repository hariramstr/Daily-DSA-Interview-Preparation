"""
Title: Maximum Feasible Toll Pass Duration

Problem Description:
A logistics company operates on a straight highway with toll gates placed at strictly
increasing mile markers. A driver starts at mile 0 and must finish at mile L.

The company wants to sell a reusable toll pass that lasts for exactly D miles:
once activated, it covers every toll gate whose mile marker lies in the interval
[x, x + D] for some chosen activation point x.

The driver may buy at most K such passes during the trip, and passes may overlap.

You are given an array gates where gates[i] is the mile marker of the i-th toll gate,
sorted in strictly increasing order, along with integers L and K.

Determine the maximum integer duration D such that it is possible to choose at most K
activation intervals of length D and cover every toll gate. The activation points do
not need to be toll locations; they may be any real values, but the answer D must be
an integer.

A faster-than-brute-force solution is expected. The key observation is that feasibility
for a fixed D is monotonic, so binary search can be combined with a greedy coverage check.

Important note about correctness:
For the stated mathematical problem, if a duration D is feasible, then any larger
duration is also feasible, because longer intervals can always simulate shorter ones.
Therefore the maximum feasible integer D over the range [0, L] is always L whenever
K >= 1 and there is at least one gate, since one interval of length L can cover every
gate in [0, L] by choosing x = 0.

The sample outputs in the prompt are inconsistent with the stated problem definition.
This implementation follows the actual problem statement exactly and therefore returns
the mathematically correct answer.
"""

from typing import List


class Solution:
    def can_cover_all_gates(self, gates: List[int], k: int, duration: int) -> bool:
        """
        Check whether all gates can be covered using at most k intervals of fixed length.

        The greedy strategy is optimal:
        - Start from the first uncovered gate.
        - Place an interval so that its left endpoint is exactly at that gate.
          This covers as far to the right as possible while still covering that gate.
        - Skip all gates covered by this interval.
        - Repeat until all gates are covered or we run out of intervals.

        Args:
            gates: Sorted list of toll gate mile markers.
            k: Maximum number of passes/intervals allowed.
            duration: Fixed interval length D being tested.

        Returns:
            True if all gates can be covered with at most k intervals, otherwise False.

        Time complexity:
            O(n), where n is the number of gates.

        Space complexity:
            O(1), ignoring input storage.
        """
        n: int = len(gates)

        # This pointer tracks the index of the first gate that has not yet been covered.
        i: int = 0

        # Count how many passes/intervals we have used so far.
        used_passes: int = 0

        # Process gates from left to right.
        while i < n:
            # If we have already used k passes and still have uncovered gates,
            # then this duration is not feasible.
            if used_passes == k:
                return False

            # We must cover gates[i]. The best greedy choice is to start the interval
            # exactly at gates[i], because any interval covering gates[i] cannot extend
            # farther right than [gates[i], gates[i] + duration] while still including
            # gates[i]. This maximizes rightward coverage.
            cover_until: int = gates[i] + duration

            # We are now using one more pass.
            used_passes += 1

            # Advance i past every gate covered by the current interval.
            while i < n and gates[i] <= cover_until:
                i += 1

        # If we exit the loop, every gate was covered successfully.
        return True

    def maximum_feasible_duration(self, gates: List[int], l: int, k: int) -> int:
        """
        Compute the largest integer duration D such that all gates can be covered
        by at most k intervals of length D.

        Because feasibility is monotonic:
        - If a duration D works, then any larger duration also works.
        We can binary search over D in the range [0, l].

        Args:
            gates: Sorted list of toll gate mile markers.
            l: Total route length; answer lies in [0, l].
            k: Maximum number of passes/intervals allowed.

        Returns:
            The maximum feasible integer duration D.

        Time complexity:
            O(n log l), where n is the number of gates.

        Space complexity:
            O(1), ignoring input storage.
        """
        # Binary search boundaries over all possible integer durations.
        left: int = 0
        right: int = l

        # We want the maximum feasible value.
        # Standard "upper-bound style" binary search:
        # - If mid is feasible, move left up to mid.
        # - Otherwise move right down below mid.
        while left < right:
            mid: int = (left + right + 1) // 2

            # Check whether this duration is sufficient.
            if self.can_cover_all_gates(gates, k, mid):
                left = mid
            else:
                right = mid - 1

        return left


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    gates1: List[int] = [2, 5, 6, 11, 14]
    l1: int = 20
    k1: int = 2
    result1: int = solution.maximum_feasible_duration(gates1, l1, k1)
    print(f"Sample 1 result: {result1}")

    # Sample 2 from the prompt.
    gates2: List[int] = [1, 4, 8, 9, 15]
    l2: int = 20
    k2: int = 3
    result2: int = solution.maximum_feasible_duration(gates2, l2, k2)
    print(f"Sample 2 result: {result2}")

    # Additional sanity checks.
    gates3: List[int] = [5]
    l3: int = 10
    k3: int = 1
    result3: int = solution.maximum_feasible_duration(gates3, l3, k3)
    print(f"Single gate result: {result3}")

    gates4: List[int] = [1, 2, 3, 4, 5]
    l4: int = 5
    k4: int = 5
    result4: int = solution.maximum_feasible_duration(gates4, l4, k4)
    print(f"Many passes result: {result4}")