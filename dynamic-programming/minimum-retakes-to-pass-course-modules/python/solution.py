"""
Title: Minimum Retakes to Pass Course Modules

Problem Description:
You are given an array modules of length n, where modules[i] is the score you would
earn on the i-th course module if you attempt it. You must process the modules from
left to right. For each module, you have two choices: keep the score as-is, or spend
one retake to improve that module's score by exactly d points. Each module can be
retaken at most once.

Your goal is to make the final sequence of scores non-decreasing, meaning the score
of every module must be at least the score of the previous module after all retake
decisions are applied. Return the minimum number of retakes needed to achieve this.
If it is impossible, return -1.

This models a realistic training platform where scores arrive in a fixed order, and
a retake can only boost a module by a fixed amount. You are not allowed to reorder
modules, skip modules, or retake a module multiple times.

Constraints:
- 1 <= n <= 100000
- 0 <= modules[i] <= 1000000000
- 0 <= d <= 1000000000

Example 1:
Input: modules = [4, 2, 5, 5], d = 3
Output: 1
Explanation: Retake the second module only. The final scores become [4, 5, 5, 5],
which is non-decreasing.

Example 2:
Input: modules = [7, 3, 2], d = 4
Output: -1
Explanation: The possible values are [7 or 11], [3 or 7], [2 or 6]. No combination
produces a non-decreasing sequence from left to right.

Expected idea:
A dynamic programming solution that tracks the minimum retakes needed to end each
position in either of its two possible final values.
"""

from typing import List, Optional


class Solution:
    def _relax(
        self,
        current_cost: Optional[int],
        candidate_cost: int,
    ) -> int:
        """
        Return the smaller valid DP cost.

        Args:
            current_cost: Existing best cost for a state, or None if not set yet.
            candidate_cost: New candidate cost to compare.

        Returns:
            The minimum valid cost.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        if current_cost is None:
            return candidate_cost
        return min(current_cost, candidate_cost)

    def minimum_retakes(self, modules: List[int], d: int) -> int:
        """
        Compute the minimum number of retakes needed to make the sequence non-decreasing.

        For each module, there are exactly two possible final values:
        1. Keep it as modules[i]
        2. Retake it once and make it modules[i] + d

        We process modules from left to right and use dynamic programming with two
        states per position:
        - dp_keep: minimum retakes needed so far if the current module is kept as-is
        - dp_retake: minimum retakes needed so far if the current module is retaken

        A transition is valid only if the current chosen value is at least the previous
        chosen value, because the final sequence must be non-decreasing.

        Args:
            modules: List of original module scores.
            d: Fixed increase gained by retaking one module.

        Returns:
            The minimum number of retakes required, or -1 if impossible.

        Time complexity:
            O(n), because each module checks only a constant number of transitions.

        Space complexity:
            O(1), because we store only the previous two DP states.
        """
        n: int = len(modules)

        # The first module is special because there is no previous module to compare against.
        # That means both choices are always valid:
        # - Keep it: cost = 0 retakes
        # - Retake it: cost = 1 retake
        prev_keep_value: int = modules[0]
        prev_retake_value: int = modules[0] + d

        prev_keep_cost: Optional[int] = 0
        prev_retake_cost: Optional[int] = 1

        # We now process every remaining module from left to right.
        # At each step, we compute the best cost for the two possible states of the
        # current module:
        # - current kept as-is
        # - current retaken once
        #
        # Each current state can come from either previous state, as long as the
        # non-decreasing condition is satisfied.
        for i in range(1, n):
            current_keep_value: int = modules[i]
            current_retake_value: int = modules[i] + d

            # These will store the best costs for the current position.
            # We start with None to mean "currently unreachable".
            current_keep_cost: Optional[int] = None
            current_retake_cost: Optional[int] = None

            # Transition 1:
            # Previous module was kept, current module is also kept.
            # This is valid only if current_keep_value >= prev_keep_value.
            if prev_keep_cost is not None and current_keep_value >= prev_keep_value:
                current_keep_cost = self._relax(current_keep_cost, prev_keep_cost)

            # Transition 2:
            # Previous module was retaken, current module is kept.
            # This is valid only if current_keep_value >= prev_retake_value.
            if prev_retake_cost is not None and current_keep_value >= prev_retake_value:
                current_keep_cost = self._relax(current_keep_cost, prev_retake_cost)

            # Transition 3:
            # Previous module was kept, current module is retaken.
            # This is valid only if current_retake_value >= prev_keep_value.
            # Since we retake the current module, we add 1 to the cost.
            if prev_keep_cost is not None and current_retake_value >= prev_keep_value:
                current_retake_cost = self._relax(current_retake_cost, prev_keep_cost + 1)

            # Transition 4:
            # Previous module was retaken, current module is retaken.
            # This is valid only if current_retake_value >= prev_retake_value.
            # Again, retaking the current module adds 1 to the cost.
            if prev_retake_cost is not None and current_retake_value >= prev_retake_value:
                current_retake_cost = self._relax(current_retake_cost, prev_retake_cost + 1)

            # After evaluating all transitions, if both states are unreachable,
            # then there is no way to build a valid non-decreasing sequence up to
            # this point, so the answer is immediately impossible.
            if current_keep_cost is None and current_retake_cost is None:
                return -1

            # Move the current states into the "previous" variables for the next loop.
            prev_keep_value = current_keep_value
            prev_retake_value = current_retake_value
            prev_keep_cost = current_keep_cost
            prev_retake_cost = current_retake_cost

        # At the end, the answer is the smaller cost among the two possible states
        # for the last module, ignoring unreachable states.
        if prev_keep_cost is None and prev_retake_cost is None:
            return -1
        if prev_keep_cost is None:
            return prev_retake_cost if prev_retake_cost is not None else -1
        if prev_retake_cost is None:
            return prev_keep_cost
        return min(prev_keep_cost, prev_retake_cost)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # modules = [4, 2, 5, 5], d = 3
    # Best choice: retake only the second module -> [4, 5, 5, 5]
    # Expected output: 1
    modules1: List[int] = [4, 2, 5, 5]
    d1: int = 3
    result1: int = solution.minimum_retakes(modules1, d1)
    print(f"modules = {modules1}, d = {d1} -> {result1}")

    # Example 2:
    # modules = [7, 3, 2], d = 4
    # Possible values:
    # [7 or 11], [3 or 7], [2 or 6]
    # No valid non-decreasing sequence exists.
    # Expected output: -1
    modules2: List[int] = [7, 3, 2]
    d2: int = 4
    result2: int = solution.minimum_retakes(modules2, d2)
    print(f"modules = {modules2}, d = {d2} -> {result2}")

    # Additional quick checks
    modules3: List[int] = [1, 2, 3]
    d3: int = 5
    result3: int = solution.minimum_retakes(modules3, d3)
    print(f"modules = {modules3}, d = {d3} -> {result3}")

    modules4: List[int] = [5, 5, 5]
    d4: int = 0
    result4: int = solution.minimum_retakes(modules4, d4)
    print(f"modules = {modules4}, d = {d4} -> {result4}")