"""
Zigzag Array Reconstruction
============================

Problem Description:
Given an integer array `nums`, rearrange its elements so that the resulting array
follows a zigzag pattern. A zigzag pattern means every element at an even index is
less than or equal to its neighbors, and every element at an odd index is greater
than or equal to its neighbors. Formally:

    nums[0] <= nums[1] >= nums[2] <= nums[3] >= nums[4] ...

Return the rearranged array. If multiple valid answers exist, return any of them.

Note: You may not sort the array before rearranging; instead, perform the rearrangement
in a single pass by swapping adjacent elements where necessary.

Constraints:
- 1 <= nums.length <= 10^4
- 0 <= nums[i] <= 10^5
- It is guaranteed that a valid zigzag arrangement always exists for the given input.

Example 1:
    Input:  nums = [4, 3, 7, 8, 6, 2, 1]
    Output: [3, 7, 4, 8, 2, 6, 1]
    Explanation: 3 <= 7 >= 4 <= 8 >= 2 <= 6 >= 1

Example 2:
    Input:  nums = [1, 2, 3]
    Output: [1, 3, 2]
    Explanation: 1 <= 3 >= 2
"""

from typing import List


class Solution:
    def zigzagArray(self, nums: List[int]) -> List[int]:
        """
        Rearrange nums into a zigzag pattern using a single-pass swap approach.

        The zigzag condition requires:
            - Even indices (0, 2, 4, ...): nums[i] <= nums[i+1]   (valleys)
            - Odd  indices (1, 3, 5, ...): nums[i] >= nums[i+1]   (peaks)

        Strategy (single pass):
            For each index i from 0 to n-2, decide whether the current pair
            (nums[i], nums[i+1]) satisfies the required relationship.
            - If i is even  → we need nums[i] <= nums[i+1].
              If violated (nums[i] > nums[i+1]), swap them.
            - If i is odd   → we need nums[i] >= nums[i+1].
              If violated (nums[i] < nums[i+1]), swap them.

        Why does a local swap always produce a globally valid zigzag?
            After fixing position i by swapping, the element that moved to i+1
            will be reconsidered when we process the pair (i+1, i+2) in the
            next iteration. So each swap can only help, never break, the
            invariant we already established for earlier positions.

        Args:
            nums: List of integers to rearrange in-place.

        Returns:
            The same list rearranged into a valid zigzag order.

        Time Complexity:  O(n) — single pass over the array.
        Space Complexity: O(1) — in-place swaps, no extra data structures.
        """

        # ------------------------------------------------------------------ #
        # Step 1: Grab the length of the array.
        # We need it to control the loop boundary.
        # ------------------------------------------------------------------ #
        n: int = len(nums)

        # ------------------------------------------------------------------ #
        # Step 2: Edge case — an array of length 0 or 1 is trivially zigzag.
        # Return immediately to avoid index-out-of-range errors below.
        # ------------------------------------------------------------------ #
        if n <= 1:
            return nums

        # ------------------------------------------------------------------ #
        # Step 3: Single-pass scan from index 0 to n-2 (inclusive).
        #
        # At each step we look at the pair (nums[i], nums[i+1]) and enforce
        # the zigzag rule for position i:
        #
        #   Even i  →  valley position  →  need nums[i] <= nums[i+1]
        #   Odd  i  →  peak  position  →  need nums[i] >= nums[i+1]
        #
        # If the condition is violated, a single swap restores it locally.
        # The next iteration will then handle the relationship between the
        # newly placed element at i+1 and its right neighbor at i+2.
        # ------------------------------------------------------------------ #
        for i in range(n - 1):

            # -------------------------------------------------------------- #
            # Determine which relationship is required at this index.
            #
            # even_index == True  →  current position is a "valley"
            #                        rule: nums[i] <= nums[i+1]
            # even_index == False →  current position is a "peak"
            #                        rule: nums[i] >= nums[i+1]
            # -------------------------------------------------------------- #
            even_index: bool = (i % 2 == 0)

            if even_index:
                # ---------------------------------------------------------- #
                # Valley check: nums[i] should be <= nums[i+1].
                # If nums[i] is GREATER than nums[i+1], the valley rule is
                # broken → swap so the smaller value sits at the valley.
                # ---------------------------------------------------------- #
                if nums[i] > nums[i + 1]:
                    # Pythonic swap — no temporary variable needed.
                    nums[i], nums[i + 1] = nums[i + 1], nums[i]

            else:
                # ---------------------------------------------------------- #
                # Peak check: nums[i] should be >= nums[i+1].
                # If nums[i] is LESS than nums[i+1], the peak rule is broken
                # → swap so the larger value sits at the peak.
                # ---------------------------------------------------------- #
                if nums[i] < nums[i + 1]:
                    nums[i], nums[i + 1] = nums[i + 1], nums[i]

        # ------------------------------------------------------------------ #
        # Step 4: Return the now-rearranged array.
        # The in-place modifications are reflected in the original list, but
        # we return it for convenience.
        # ------------------------------------------------------------------ #
        return nums


# --------------------------------------------------------------------------- #
# Helper: verify that an array truly satisfies the zigzag property.
# Useful for testing and educational purposes.
# --------------------------------------------------------------------------- #
def is_zigzag(arr: List[int]) -> bool:
    """
    Check whether arr satisfies the zigzag condition:
        arr[0] <= arr[1] >= arr[2] <= arr[3] >= ...

    Args:
        arr: The array to validate.

    Returns:
        True if arr is a valid zigzag, False otherwise.
    """
    for i in range(len(arr) - 1):
        if i % 2 == 0:
            # Even index → valley: arr[i] must be <= arr[i+1]
            if arr[i] > arr[i + 1]:
                return False
        else:
            # Odd index → peak: arr[i] must be >= arr[i+1]
            if arr[i] < arr[i + 1]:
                return False
    return True


# --------------------------------------------------------------------------- #
# Main block: demonstrate the solution with the provided examples and extras.
# --------------------------------------------------------------------------- #
if __name__ == "__main__":
    solver = Solution()

    # ---------------------------------------------------------------------- #
    # Example 1 from the problem statement
    # Input:    [4, 3, 7, 8, 6, 2, 1]
    # Expected: any valid zigzag, e.g. [3, 7, 4, 8, 2, 6, 1]
    # ---------------------------------------------------------------------- #
    nums1: List[int] = [4, 3, 7, 8, 6, 2, 1]
    result1 = solver.zigzagArray(nums1)
    print("Example 1")
    print(f"  Input  : [4, 3, 7, 8, 6, 2, 1]")
    print(f"  Output : {result1}")
    print(f"  Valid? : {is_zigzag(result1)}")
    print()

    # ---------------------------------------------------------------------- #
    # Example 2 from the problem statement
    # Input:    [1, 2, 3]
    # Expected: any valid zigzag, e.g. [1, 3, 2]
    # ---------------------------------------------------------------------- #
    nums2: List[int] = [1, 2, 3]
    result2 = solver.zigzagArray(nums2)
    print("Example 2")
    print(f"  Input  : [1, 2, 3]")
    print(f"  Output : {result2}")
    print(f"  Valid? : {is_zigzag(result2)}")
    print()

    # ---------------------------------------------------------------------- #
    # Extra test: single element — trivially zigzag
    # ---------------------------------------------------------------------- #
    nums3: List[int] = [42]
    result3 = solver.zigzagArray(nums3)
    print("Extra 1 — single element")
    print(f"  Input  : [42]")
    print(f"  Output : {result3}")
    print(f"  Valid? : {is_zigzag(result3)}")
    print()

    # ---------------------------------------------------------------------- #
    # Extra test: two elements
    # ---------------------------------------------------------------------- #
    nums4: List[int] = [5, 3]
    result4 = solver.zigzagArray(nums4)
    print("Extra 2 — two elements")
    print(f"  Input  : [5, 3]")
    print(f"  Output : {result4}")
    print(f"  Valid? : {is_zigzag(result4)}")
    print()

    # ---------------------------------------------------------------------- #
    # Extra test: all equal elements — every comparison is satisfied trivially
    # ---------------------------------------------------------------------- #
    nums5: List[int] = [7, 7, 7, 7, 7]
    result5 = solver.zigzagArray(nums5)
    print("Extra 3 — all equal")
    print(f"  Input  : [7, 7, 7, 7, 7]")
    print(f"  Output : {result5}")
    print(f"  Valid? : {is_zigzag(result5)}")
    print()

    # ---------------------------------------------------------------------- #
    # Extra test: already sorted ascending — needs rearrangement
    # ---------------------------------------------------------------------- #
    nums6: List[int] = [1, 2, 3, 4, 5, 6]
    result6 = solver.zigzagArray(nums6)
    print("Extra 4 — sorted ascending")
    print(f"  Input  : [1, 2, 3, 4, 5, 6]")
    print(f"  Output : {result6}")
    print(f"  Valid? : {is_zigzag(result6)}")
    print()

    # ---------------------------------------------------------------------- #
    # Extra test: sorted descending
    # ---------------------------------------------------------------------- #
    nums7: List[int] = [6, 5, 4, 3, 2, 1]
    result7 = solver.zigzagArray(nums7)
    print("Extra 5 — sorted descending")
    print(f"  Input  : [6, 5, 4, 3, 2, 1]")
    print(f"  Output : {result7}")
    print(f"  Valid? : {is_zigzag(result7)}")