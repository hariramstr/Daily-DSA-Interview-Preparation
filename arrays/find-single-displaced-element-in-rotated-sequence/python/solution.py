```python
"""
Title: Find the Single Displaced Element in a Rotated Sequence
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an array `nums` of `n` integers that was originally a sequence of
consecutive integers starting from 1 (i.e., [1, 2, 3, ..., n]). The array was
then rotated at some unknown pivot point, and exactly one element was replaced
with a value that does not belong to the original sequence.

Your task is to find and return the displaced (replaced) element — the one
element in `nums` that does not fit the rotated consecutive sequence.

A rotated consecutive sequence means the array looks like
[k, k+1, ..., n, 1, 2, ..., k-1] for some rotation point k.

Constraints:
- 2 <= n <= 10^4
- 1 <= nums[i] <= 2 * n
- Exactly one element in `nums` is displaced (does not belong to the rotated
  sequence of 1 to n)
- All other elements are distinct and form a valid rotated consecutive sequence
"""

from typing import List


class Solution:
    def find_displaced_element(self, nums: List[int]) -> int:
        """
        Find the single displaced element in a rotated consecutive sequence.

        The key insight is:
        - The original sequence is [1, 2, 3, ..., n], rotated at some pivot.
        - Exactly one element has been replaced by a "displaced" value.
        - We need to find which element doesn't belong.

        Strategy:
        1. The sum of the original sequence [1..n] is n*(n+1)//2.
        2. The sum of the actual array differs from the expected sum by:
               actual_sum - expected_sum = displaced_value - missing_value
           where missing_value is the element from [1..n] that was replaced.
        3. We can also find the missing value by checking which number in [1..n]
           is absent from the array (using a set).
        4. Once we know the missing value, the displaced element is:
               displaced = actual_sum - expected_sum + missing_value

        Args:
            nums: List of n integers representing the rotated sequence with
                  one displaced element.

        Returns:
            The displaced element (the one that doesn't belong to [1..n]).

        Time Complexity:  O(n) — we iterate through the array a constant number
                          of times.
        Space Complexity: O(n) — we use a set to track which values are present.
        """

        # -----------------------------------------------------------------------
        # Step 1: Determine n (the length of the array).
        # The original sequence has exactly n elements: [1, 2, ..., n].
        # -----------------------------------------------------------------------
        n = len(nums)

        # -----------------------------------------------------------------------
        # Step 2: Compute the expected sum of the original sequence [1..n].
        # Using the arithmetic series formula: sum = n * (n + 1) / 2
        # This is the sum we WOULD have if no element were displaced.
        # -----------------------------------------------------------------------
        expected_sum = n * (n + 1) // 2

        # -----------------------------------------------------------------------
        # Step 3: Compute the actual sum of the given array.
        # This sum includes the displaced element instead of the missing element.
        # -----------------------------------------------------------------------
        actual_sum = sum(nums)

        # -----------------------------------------------------------------------
        # Step 4: Build a set of all values currently in nums.
        # A set gives O(1) average-time membership checks.
        # We'll use this to find which value from [1..n] is missing.
        # -----------------------------------------------------------------------
        nums_set = set(nums)

        # -----------------------------------------------------------------------
        # Step 5: Find the missing value — the number in [1..n] that is NOT
        # present in nums.
        #
        # Because exactly one element was replaced, exactly one number from
        # [1..n] will be absent from the array.
        # -----------------------------------------------------------------------
        missing_value = 0  # Will be updated in the loop below

        for value in range(1, n + 1):
            # Check if this value from the expected sequence is absent
            if value not in nums_set:
                # Found the number that was replaced (the "hole" in [1..n])
                missing_value = value
                break  # There is exactly one missing value, so we can stop

        # -----------------------------------------------------------------------
        # Step 6: Derive the displaced element.
        #
        # We know:
        #   actual_sum = expected_sum - missing_value + displaced_value
        #
        # Rearranging:
        #   displaced_value = actual_sum - expected_sum + missing_value
        #
        # Example trace for nums = [4, 5, 6, 7, 99, 1, 2, 3]:
        #   n = 8
        #   expected_sum = 8*9//2 = 36
        #   actual_sum   = 4+5+6+7+99+1+2+3 = 127
        #   nums_set     = {1,2,3,4,5,6,7,99}
        #   missing_value = 8  (8 is not in nums_set)
        #   displaced    = 127 - 36 + 8 = 99  ✓
        #
        # Example trace for nums = [3, 4, 0, 1, 2]:
        #   n = 5
        #   expected_sum = 5*6//2 = 15
        #   actual_sum   = 3+4+0+1+2 = 10
        #   nums_set     = {0,1,2,3,4}
        #   missing_value = 5  (5 is not in nums_set)
        #   displaced    = 10 - 15 + 5 = 0  ✓
        # -----------------------------------------------------------------------
        displaced_value = actual_sum - expected_sum + missing_value

        return displaced_value


# ---------------------------------------------------------------------------
# Main block: demonstrate the solution with the provided examples and
# additional edge cases.
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solver = Solution()

    # -----------------------------------------------------------------------
    # Example 1 (from problem description)
    # Input:  [4, 5, 6, 7, 99, 1, 2, 3]
    # Expected output: 99
    # Rotated sequence would be [4, 5, 6, 7, 8, 1, 2, 3]; 99 replaced 8.
    # -----------------------------------------------------------------------
    nums1 = [4, 5, 6, 7, 99, 1, 2, 3]
    result1 = solver.find_displaced_element(nums1)
    print(f"Example 1:")
    print(f"  Input:    {nums1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: 99")
    print(f"  Correct:  {result1 == 99}")
    print()

    # -----------------------------------------------------------------------
    # Example 2 (from problem description)
    # Input:  [3, 4, 0, 1, 2]
    # Expected output: 0
    # Rotated sequence would be [3, 4, 5, 1, 2]; 0 replaced 5.
    # -----------------------------------------------------------------------
    nums2 = [3, 4, 0, 1, 2]
    result2 = solver.find_displaced_element(nums2)
    print(f"Example 2:")
    print(f"  Input:    {nums2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: 0")
    print(f"  Correct:  {result2 == 0}")
    print()

    # -----------------------------------------------------------------------
    # Additional Edge Case 1: displaced element at the very beginning
    # Original rotated: [2, 3, 4, 5, 1]  →  displaced 99 replaces 2
    # -----------------------------------------------------------------------
    nums3 = [99, 3, 4, 5, 1]
    result3 = solver.find_displaced_element(nums3)
    print(f"Edge Case 1 (displaced at start):")
    print(f"  Input:    {nums3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: 99")
    print(f"  Correct:  {result3 == 99}")
    print()

    # -----------------------------------------------------------------------
    # Additional Edge Case 2: displaced element at the very end
    # Original rotated: [3, 4, 5, 1, 2]  →  displaced 7 replaces 2
    # -----------------------------------------------------------------------
    nums4 = [3, 4, 5, 1, 7]
    result4 = solver.find_displaced_element(nums4)
    print(f"Edge Case 2 (displaced at end):")
    print(f"  Input:    {nums4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: 7")
    print(f"  Correct:  {result4 == 7}")
    print()

    # -----------------------------------------------------------------------
    # Additional Edge Case 3: minimal array (n=2)
    # Original: [1, 2] or [2, 1]  →  displaced 5 replaces 1
    # -----------------------------------------------------------------------
    nums5 = [5, 2]
    result5 = solver.find_displaced_element(nums5)
    print(f"Edge Case 3 (n=2, displaced first element):")
    print(f"  Input:    {nums5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: 5")
    print(f"  Correct:  {result5 == 5}")
    print()

    # -----------------------------------------------------------------------
    # Additional Edge Case 4: no rotation (pivot = 1), displaced in middle
    # Original: [1, 2, 3, 4, 5, 6]  →  displaced 20 replaces 4
    # -----------------------------------------------------------------------
    nums6 = [1, 2, 3, 20, 5, 6]
    result6 = solver.find_displaced_element(nums6)
    print(f"Edge Case 4 (no rotation, displaced in middle):")
    print(f"  Input:    {nums6}")
    print(f"  Output:   {result6}")
    print(f"  Expected: 20")
    print(f"  Correct:  {result6 == 20}")
```