"""
Title: Minimum Toggles to Match a Device XOR Fingerprint
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
A hardware lab stores the state of n devices as an integer array states, where states[i]
is a non-negative 32-bit integer. The lab wants the XOR of all device states to become
exactly target. In one operation, you may choose any single device and toggle exactly
one bit in its binary representation (change a 0 to 1 or a 1 to 0 at one bit position).

Return the minimum number of bit toggles required so that the XOR of the entire array
equals target.

This is not asking you to transform each number into a specific value. You may toggle
bits on any devices in any order, and only the final XOR of all numbers matters. A
toggle on one device affects the global XOR at exactly that bit position. Because XOR
is independent across bit positions, the answer depends only on which bits differ
between the current overall XOR and target.

Formally, let current = states[0] XOR states[1] XOR ... XOR states[n - 1]. Find the
minimum number of single-bit toggles needed to make current become target.

Constraints:
- 1 <= n <= 200000
- 0 <= states[i] <= 10^9
- 0 <= target <= 10^9
- Your solution should run in O(n) time and use O(1) extra space, excluding input storage.

Example 1:
Input: states = [5, 1, 2], target = 0
Output: 2
Explanation: current XOR = 5 XOR 1 XOR 2 = 6. Binary 6 is 110, while target 0 is 000.
Two bit positions differ, so two single-bit toggles are sufficient.

Example 2:
Input: states = [7, 7, 7], target = 7
Output: 0
Explanation: current XOR = 7 XOR 7 XOR 7 = 7, which already matches target, so no
operation is needed.
"""

from typing import List


class Solution:
    def min_toggles_to_match_xor(self, states: List[int], target: int) -> int:
        """
        Compute the minimum number of single-bit toggles needed so that the XOR of all
        values in states becomes exactly target.

        Args:
            states: List of non-negative integers representing device states.
            target: Desired final XOR value of the entire array.

        Returns:
            The minimum number of one-bit toggle operations required.

        Time complexity:
            O(n), where n is the number of elements in states.

        Space complexity:
            O(1), excluding the input storage.
        """
        # Step 1:
        # Compute the XOR of all device states.
        #
        # Why this works:
        # The problem only cares about the XOR of the entire array, not the exact final
        # value of each individual device. So instead of thinking about many numbers, we
        # compress the whole array into one value: the current global XOR.
        #
        # Data structure usage:
        # We only use a single integer variable named `current_xor`.
        current_xor: int = 0

        # Go through every device state and fold it into the running XOR.
        for value in states:
            current_xor ^= value

        # Step 2:
        # Find which bit positions are different between the current XOR and the target.
        #
        # Why XOR again?
        # For any bit position:
        # - If current_xor and target have the same bit, their XOR at that bit is 0.
        # - If they differ, their XOR at that bit is 1.
        #
        # Therefore, `difference_mask` has 1s exactly at the bit positions we must fix.
        difference_mask: int = current_xor ^ target

        # Step 3:
        # Count how many 1-bits are in difference_mask.
        #
        # Why is this the answer?
        # Toggling one bit in one device flips exactly one bit in the global XOR.
        # So each differing bit position requires at least one toggle.
        # Also, one toggle is sufficient to fix one differing bit position, because we
        # can choose any device and flip that bit there.
        #
        # Therefore:
        # minimum toggles = number of differing bit positions
        #
        # We use Brian Kernighan's algorithm to count set bits efficiently:
        # Repeatedly remove the lowest set bit using:
        #   x = x & (x - 1)
        #
        # Each loop iteration removes exactly one 1-bit, so the number of iterations is
        # exactly the number of set bits.
        toggles_needed: int = 0

        while difference_mask != 0:
            toggles_needed += 1
            difference_mask &= difference_mask - 1

        return toggles_needed

    def minOperations(self, states: List[int], target: int) -> int:
        """
        Compatibility wrapper method that calls the main algorithm.

        Args:
            states: List of non-negative integers representing device states.
            target: Desired final XOR value of the entire array.

        Returns:
            The minimum number of one-bit toggle operations required.

        Time complexity:
            O(n), where n is the number of elements in states.

        Space complexity:
            O(1), excluding the input storage.
        """
        return self.min_toggles_to_match_xor(states, target)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # states = [5, 1, 2]
    # current XOR = 5 ^ 1 ^ 2 = 6
    # target = 0
    # difference = 6 ^ 0 = 6, which is binary 110
    # Number of set bits = 2
    # Expected output: 2
    states1: List[int] = [5, 1, 2]
    target1: int = 0
    result1: int = solution.min_toggles_to_match_xor(states1, target1)
    print(result1)

    # Example 2:
    # states = [7, 7, 7]
    # current XOR = 7 ^ 7 ^ 7 = 7
    # target = 7
    # difference = 7 ^ 7 = 0
    # Number of set bits = 0
    # Expected output: 0
    states2: List[int] = [7, 7, 7]
    target2: int = 7
    result2: int = solution.min_toggles_to_match_xor(states2, target2)
    print(result2)