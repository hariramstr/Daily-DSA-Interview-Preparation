"""
Title: Minimum Fatigue to Tune a Multi-String Instrument

Problem Description:
A musician is preparing an electronic instrument with n strings. For each string i,
the desired final pitch is target[i]. You are given a list of m tuning operations.
The j-th operation is described by four integers [l_j, r_j, delta_j, cost_j], meaning
you may apply this operation at most once, and if you do, every string in the inclusive
range l_j..r_j has its pitch increased by exactly delta_j, while you pay fatigue cost
cost_j. Operations can be applied in any order, and multiple operations may affect the
same string. Initially, all string pitches are 0.

Your task is to compute the minimum total fatigue required to make every string end at
exactly its target pitch. If it is impossible, return -1.

This is not a local optimization problem: an operation that helps one string may overshoot
another, so the best answer may require carefully coordinating overlapping interval updates.
The ranges are 0-indexed.

Constraints:
- 1 <= n <= 8
- 1 <= m <= 60
- 0 <= target[i] <= 40
- 0 <= l_j <= r_j < n
- 1 <= delta_j <= 20
- 1 <= cost_j <= 10^4
- Each operation may be used at most once.

Examples:
1) target = [3, 3]
   operations = [[0,0,3,4],[1,1,3,5],[0,1,3,6]]
   Output: 6

2) target = [2, 1, 2]
   operations = [[0,1,1,3],[1,2,1,4],[0,2,2,10]]
   Output: -1
"""

from typing import Dict, List, Tuple


class Solution:
    def _encode_state(self, values: List[int], bases: List[int]) -> int:
        """
        Encode a pitch vector into a single integer using mixed-radix representation.

        Args:
            values: Current pitch on each string.
            bases: Positional multipliers for mixed-radix encoding.

        Returns:
            Encoded integer state.

        Time complexity:
            O(n)

        Space complexity:
            O(1) auxiliary
        """
        state_id: int = 0
        for i, value in enumerate(values):
            state_id += value * bases[i]
        return state_id

    def _decode_state(self, state_id: int, target: List[int], bases: List[int]) -> List[int]:
        """
        Decode a mixed-radix integer back into the pitch vector.

        Args:
            state_id: Encoded state.
            target: Target pitch vector, used to know each digit's radix.
            bases: Positional multipliers for mixed-radix encoding.

        Returns:
            Decoded list of pitches for all strings.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(target)
        values: List[int] = [0] * n

        # We recover each coordinate independently.
        # Since base for coordinate i is (target[i] + 1), every digit is guaranteed
        # to be in the range [0, target[i]] for valid states.
        for i in range(n):
            radix: int = target[i] + 1
            values[i] = (state_id // bases[i]) % radix

        return values

    def minimum_fatigue(self, target: List[int], operations: List[List[int]]) -> int:
        """
        Compute the minimum total fatigue needed to reach the exact target pitches.

        This uses dynamic programming over compact states:
        - A state represents the current pitch vector across all strings.
        - We start from the all-zero vector.
        - For each operation, we decide to skip it or apply it once.
        - Any transition that would make any string exceed its target is discarded.

        Because n <= 8 and each target[i] <= 40, the full state space can still be large,
        so we store only reachable states in a dictionary and update them incrementally.

        Args:
            target: Desired final pitch for each string.
            operations: List of operations [l, r, delta, cost].

        Returns:
            Minimum fatigue cost to reach target exactly, or -1 if impossible.

        Time complexity:
            O(m * R * n), where R is the number of reachable non-overshooting states

        Space complexity:
            O(R)
        """
        n: int = len(target)

        # Mixed-radix encoding setup:
        # A vector [x0, x1, ..., x(n-1)] where 0 <= xi <= target[i]
        # is encoded into one integer. This lets us use dictionary keys efficiently.
        #
        # Example:
        # If target = [3, 3], then each coordinate has radix 4.
        # State [a, b] becomes a * 1 + b * 4.
        bases: List[int] = [1] * n
        for i in range(1, n):
            bases[i] = bases[i - 1] * (target[i - 1] + 1)

        # Encode the target vector once so we can compare states quickly at the end.
        target_state: int = self._encode_state(target, bases)

        # DP dictionary:
        # key   = encoded pitch vector
        # value = minimum fatigue cost to reach that vector using processed operations
        #
        # Initially, all strings are at pitch 0 with cost 0.
        dp: Dict[int, int] = {0: 0}

        # Process operations one by one.
        # This is a classic 0/1 knapsack-style update over states:
        # each operation can be used at most once, so transitions must be based on
        # the previous layer's states, not states created during the same operation.
        for op_index, operation in enumerate(operations):
            l, r, delta, cost = operation

            # Start next_dp as a copy of dp to represent the "skip this operation" choice.
            # Then we try to improve it by applying the current operation to every old state.
            next_dp: Dict[int, int] = dict(dp)

            # Iterate over states reachable before considering this operation.
            for state_id, current_cost in dp.items():
                # Decode the current pitch vector so we can test whether applying
                # the interval increment would overshoot any target.
                current_values: List[int] = self._decode_state(state_id, target, bases)

                # Build the candidate next state after applying this operation.
                # We must verify exact feasibility:
                # if any affected string exceeds target, this transition is invalid
                # and must be discarded immediately.
                valid: bool = True
                new_values: List[int] = current_values[:]

                for string_index in range(l, r + 1):
                    new_values[string_index] += delta
                    if new_values[string_index] > target[string_index]:
                        valid = False
                        break

                # If the operation overshoots even one string, we cannot use it from
                # this state because pitches only increase and can never be reduced.
                if not valid:
                    continue

                # Encode the new vector and update the best known cost.
                new_state_id: int = self._encode_state(new_values, bases)
                new_cost: int = current_cost + cost

                # Keep only the minimum fatigue for each reachable state.
                if new_state_id not in next_dp or new_cost < next_dp[new_state_id]:
                    next_dp[new_state_id] = new_cost

            # Move to the next layer.
            dp = next_dp

        # After all operations are processed, check whether the exact target state
        # is reachable. If yes, return its minimum cost; otherwise return -1.
        return dp.get(target_state, -1)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target1: List[int] = [3, 3]
    operations1: List[List[int]] = [
        [0, 0, 3, 4],
        [1, 1, 3, 5],
        [0, 1, 3, 6],
    ]
    result1: int = solution.minimum_fatigue(target1, operations1)
    print("Example 1 result:", result1)  # Expected: 6

    # Example 2
    target2: List[int] = [2, 1, 2]
    operations2: List[List[int]] = [
        [0, 1, 1, 3],
        [1, 2, 1, 4],
        [0, 2, 2, 10],
    ]
    result2: int = solution.minimum_fatigue(target2, operations2)
    print("Example 2 result:", result2)  # Expected: -1