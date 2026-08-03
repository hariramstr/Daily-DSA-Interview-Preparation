"""
Title: Longest Run of Pairwise Disjoint Feature Masks

Problem Description:
You are given an array masks where masks[i] is a non-negative integer representing
the enabled feature bits of the i-th software build in chronological order.

A contiguous run of builds is called compatible if no bit position is enabled in
more than one build inside that run. In other words, for every pair of different
indices a and b within the same run, masks[a] & masks[b] == 0.

Your task is to return the length of the longest compatible contiguous run.

This is not the same as checking whether the bitwise AND of the whole window is zero.
A run is valid only when every bit appears at most once across the entire window.
For example, [1, 2, 4] is compatible, but [3, 4, 1] is not, because bit 0 appears
in both 3 and 1.

Design an efficient algorithm that works for large inputs.

Constraints:
- 1 <= masks.length <= 100000
- 0 <= masks[i] <= 10^9
- masks[i] fits in a 32-bit signed integer
"""

from typing import List


class Solution:
    def longest_compatible_run(self, masks: List[int]) -> int:
        """
        Find the length of the longest contiguous run where all masks are pairwise disjoint.

        A window is valid when no bit is used by more than one number in that window.
        This means every pair of numbers inside the window has bitwise AND equal to 0.

        Args:
            masks: A list of non-negative integers representing feature bit masks.

        Returns:
            The maximum length of a contiguous compatible run.

        Time complexity:
            O(n), where n is the length of masks.
            Each element enters the sliding window once and leaves it at most once.

        Space complexity:
            O(1), because we only store a few integer variables regardless of input size.
        """
        # We use the classic "sliding window" technique because:
        # 1. We need a contiguous subarray.
        # 2. We want the longest such subarray.
        # 3. We can efficiently expand and shrink a window while maintaining validity.
        #
        # Core idea:
        # - Maintain a window [left, right].
        # - Maintain an integer called `used_bits`.
        #   This integer stores the union (bitwise OR) of all bits currently present
        #   in the window.
        #
        # Why is OR enough here?
        # Because we only allow valid windows where no bit appears more than once.
        # In a valid window:
        # - If a bit is set in `used_bits`, it belongs to exactly one number in the window.
        #
        # When we want to add masks[right]:
        # - If masks[right] & used_bits == 0, then it shares no bit with the current window,
        #   so it is safe to include.
        # - Otherwise, there is overlap, so the window becomes invalid.
        #   We then move `left` forward, removing numbers from the window until the overlap disappears.
        #
        # How do we remove masks[left] from `used_bits`?
        # Since the current window is always kept valid, every bit belongs to at most one number.
        # Therefore, removing a number can be done with XOR:
        #     used_bits ^= masks[left]
        # This toggles off exactly the bits contributed by masks[left].
        #
        # This works correctly only because we never allow duplicate bits inside the window.
        # That invariant is the key to the whole solution.

        left: int = 0
        used_bits: int = 0
        best: int = 0

        # We expand the window one element at a time using `right`.
        for right, value in enumerate(masks):
            # If `value` overlaps with any bit already used in the current window,
            # the window is no longer valid if we include it.
            #
            # Example:
            # used_bits = 0b0101
            # value     = 0b0001
            # overlap   = 0b0001 != 0
            #
            # So we must shrink from the left until there is no overlap.
            while used_bits & value:
                # Remove the leftmost value from the window.
                #
                # Because the window before adding `value` is valid, every set bit in
                # masks[left] appears exactly once in the window. So XOR safely removes
                # those bits from `used_bits`.
                used_bits ^= masks[left]

                # Move the left boundary rightward, making the window smaller.
                left += 1

            # At this point, `value` has no overlapping bits with the current window.
            # So we can safely add it.
            used_bits |= value

            # The current window [left, right] is valid.
            current_length: int = right - left + 1

            # Update the best answer seen so far.
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # masks = [1, 2, 4, 3, 8]
    #
    # Trace:
    # [1] -> valid
    # [1, 2] -> valid
    # [1, 2, 4] -> valid, length 3
    # Add 3:
    #   3 overlaps with 1 (bit 0) and 2 (bit 1), so shrink
    #   after shrinking enough, valid window continues
    # Longest remains 3
    example1: List[int] = [1, 2, 4, 3, 8]
    result1: int = solution.longest_compatible_run(example1)
    print(f"Input: {example1}")
    print(f"Output: {result1}")
    print("Expected: 3")
    print()

    # Example 2:
    # masks = [5, 1, 2, 8, 4]
    #
    # 5 = 0101
    # 1 = 0001 -> overlaps with 5, so window must shrink
    # Then [1, 2, 8, 4] is valid:
    # 1 = 0001
    # 2 = 0010
    # 8 = 1000
    # 4 = 0100
    # All bits are distinct, length 4
    example2: List[int] = [5, 1, 2, 8, 4]
    result2: int = solution.longest_compatible_run(example2)
    print(f"Input: {example2}")
    print(f"Output: {result2}")
    print("Expected: 4")
    print()

    # Additional quick checks
    additional_tests: List[List[int]] = [
        [0],
        [1],
        [1, 1],
        [1, 2, 3],
        [1, 2, 4, 8, 16],
        [3, 4, 1],
        [0, 0, 0, 0],
    ]

    for test in additional_tests:
        print(f"Input: {test}")
        print(f"Longest compatible run: {solution.longest_compatible_run(test)}")
        print()