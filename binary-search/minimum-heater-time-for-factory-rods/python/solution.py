"""
Title: Minimum Heater Time for Factory Rods
Difficulty: Medium
Topic: Binary Search

Problem Description:
A factory needs to soften metal rods before cutting them. You are given an array rods
where rods[i] is the length of the i-th rod, and an integer machines representing the
number of identical heating machines available.

In one minute, a machine can heat exactly one rod segment of length t, where t is the
chosen heating time for that minute. If a rod has length L and the factory uses heating
time t, that rod requires ceil(L / t) machine-minutes to finish because it can be
processed in multiple equal-sized segments over time. Different rods may be processed in
parallel across machines, but the total number of machine-minutes available is machines.

Your task is to find the minimum positive integer heating time t such that all rods can
be fully processed using at most machines total machine-minutes.

In other words, find the smallest integer t where:
    sum(ceil(rods[i] / t)) <= machines

This problem is designed to be solved efficiently. A brute-force search over all possible
t values may be too slow for large inputs, but the feasibility condition is monotonic:
if a given heating time works, any larger heating time also works.

Constraints:
- 1 <= rods.length <= 100000
- 1 <= rods[i] <= 1000000000
- rods.length <= machines <= 1000000000
- t must be a positive integer

Example 1:
Input: rods = [8, 5, 10], machines = 7
Output: 4
Explanation:
With t = 4, the required machine-minutes are:
ceil(8/4) + ceil(5/4) + ceil(10/4) = 2 + 2 + 3 = 7
which fits exactly. Any smaller t needs more than 7 machine-minutes.

Example 2:
Input: rods = [12, 15, 6], machines = 6
Output: 6
Explanation:
With t = 6, the total is:
ceil(12/6) + ceil(15/6) + ceil(6/6) = 2 + 3 + 1 = 6
With t = 5, the total becomes:
3 + 3 + 2 = 8
so 5 is not enough.
"""

from typing import List


class Solution:
    def required_machine_minutes(self, rods: List[int], heating_time: int, limit: int) -> int:
        """
        Compute how many total machine-minutes are needed for a given heating time.

        For each rod of length L, the number of machine-minutes needed is:
            ceil(L / heating_time)

        We sum this over all rods. We also stop early if the running total already
        exceeds the provided limit, because in the binary search we only care whether
        the total is <= machines or > machines.

        Args:
            rods: List of rod lengths.
            heating_time: Candidate integer heating time t.
            limit: Upper bound used for early stopping, typically equal to machines.

        Returns:
            The total required machine-minutes for this heating time. If the running
            total exceeds limit, the method returns immediately with a value > limit.

        Time complexity:
            O(n) in the worst case, where n is the number of rods.

        Space complexity:
            O(1)
        """
        total_minutes: int = 0

        # We process each rod independently because the formula for one rod does not
        # depend on any other rod. The total work is simply the sum of all per-rod work.
        for length in rods:
            # Instead of using math.ceil(length / heating_time), we use the standard
            # integer arithmetic trick:
            #     ceil(a / b) = (a + b - 1) // b
            #
            # This avoids floating-point operations and is both faster and exact.
            total_minutes += (length + heating_time - 1) // heating_time

            # Early exit optimization:
            # If we already exceeded the allowed number of machine-minutes, there is no
            # need to continue. This can save time on large inputs.
            if total_minutes > limit:
                return total_minutes

        return total_minutes

    def minimum_heater_time(self, rods: List[int], machines: int) -> int:
        """
        Find the minimum positive integer heating time t such that:
            sum(ceil(rods[i] / t)) <= machines

        This is solved with binary search because the feasibility condition is monotonic:
        - If a heating time t works, then any larger heating time also works.
        - If a heating time t does not work, then any smaller heating time also does not work.

        Args:
            rods: List of rod lengths.
            machines: Maximum total machine-minutes available.

        Returns:
            The smallest valid positive integer heating time.

        Time complexity:
            O(n log M), where:
            - n is the number of rods
            - M is max(rods)

        Space complexity:
            O(1)
        """
        # The smallest possible heating time is 1.
        # We cannot choose 0 because the problem requires a positive integer.
        left: int = 1

        # The largest rod length is always a valid upper bound:
        # If t = max(rods), then every rod needs at most 1 machine-minute,
        # so the total is at most len(rods). Since the constraints guarantee
        # len(rods) <= machines, this upper bound is always feasible.
        right: int = max(rods)

        # We will shrink the search space until left == right.
        # At that point, it will be the smallest feasible heating time.
        while left < right:
            # Standard midpoint calculation.
            mid: int = left + (right - left) // 2

            # Check how many machine-minutes are needed if we choose heating time = mid.
            needed: int = self.required_machine_minutes(rods, mid, machines)

            # If needed <= machines, then mid is feasible.
            # Since we want the MINIMUM feasible value, we keep mid in the search space
            # and continue searching on the left half, including mid itself.
            if needed <= machines:
                right = mid
            else:
                # If needed > machines, then mid is too small.
                # Smaller heating times would only require even more machine-minutes,
                # so they are also impossible. Therefore, we discard mid and everything
                # to its left.
                left = mid + 1

        # When the loop ends, left == right and points to the smallest feasible value.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    rods1: List[int] = [8, 5, 10]
    machines1: int = 7
    result1: int = solution.minimum_heater_time(rods1, machines1)
    print("Example 1:")
    print(f"rods = {rods1}, machines = {machines1}")
    print(f"Minimum heater time = {result1}")
    print()

    # Example 2
    rods2: List[int] = [12, 15, 6]
    machines2: int = 6
    result2: int = solution.minimum_heater_time(rods2, machines2)
    print("Example 2:")
    print(f"rods = {rods2}, machines = {machines2}")
    print(f"Minimum heater time = {result2}")
    print()

    # Additional quick sanity checks
    rods3: List[int] = [1]
    machines3: int = 1
    result3: int = solution.minimum_heater_time(rods3, machines3)
    print("Additional Test 1:")
    print(f"rods = {rods3}, machines = {machines3}")
    print(f"Minimum heater time = {result3}")
    print()

    rods4: List[int] = [100, 200, 300]
    machines4: int = 6
    result4: int = solution.minimum_heater_time(rods4, machines4)
    print("Additional Test 2:")
    print(f"rods = {rods4}, machines = {machines4}")
    print(f"Minimum heater time = {result4}")