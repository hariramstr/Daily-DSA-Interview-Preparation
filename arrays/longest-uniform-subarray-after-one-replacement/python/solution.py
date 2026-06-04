```python
"""
Title: Find the Longest Uniform Subarray After One Replacement

Problem Description:
Given an integer array `nums`, you are allowed to replace at most one element
in the array with any value of your choice. Return the length of the longest
subarray where all elements are equal after performing at most one such replacement.

A subarray is a contiguous part of the array.

Example 1:
    Input: nums = [1, 1, 2, 1, 1]
    Output: 5
    Explanation: Replace the element at index 2 (value 2) with 1.
                 The entire array becomes [1, 1, 1, 1, 1], which has length 5.

Example 2:
    Input: nums = [3, 3, 5, 5, 5, 3]
    Output: 4
    Explanation: Replace index 3 (value 5) with 5 gives [5, 5, 5, 5] of length 4.

Constraints:
    - 1 <= nums.length <= 10^5
    - 1 <= nums[i] <= 10^4
    - You may replace at most one element with any integer value.
    - The replacement is optional.
"""

from typing import List


class Solution:
    def longestUniformSubarrayAfterOneReplacement(self, nums: List[int]) -> int:
        """
        Find the longest subarray where all elements are equal after at most one replacement.

        Core Idea (Sliding Window):
        We use a sliding window [left, right]. For a window to be valid (convertible
        to all-same values with at most 1 replacement), the window can contain at most
        1 element that differs from the "dominant" value (the value we want all elements
        to become).

        The dominant value in any window is the most frequent value in that window.
        If (window_size - max_frequency_in_window) <= 1, we can fix the window with
        at most 1 replacement.

        Args:
            nums: List of integers to process.

        Returns:
            Integer representing the length of the longest valid subarray.

        Time Complexity:  O(n) — each element is visited at most twice (once by right,
                          once by left pointer).
        Space Complexity: O(k) — where k is the number of distinct values in the window
                          (at most O(n) in the worst case, but typically small).
        """

        # -----------------------------------------------------------------------
        # STEP 1: Handle edge cases
        # If the array is empty, return 0. If it has 1 element, return 1
        # (we can always have a subarray of length 1).
        # -----------------------------------------------------------------------
        if not nums:
            return 0

        # -----------------------------------------------------------------------
        # STEP 2: Initialize sliding window variables
        #
        # - left: the left boundary of our sliding window (inclusive)
        # - max_len: tracks the best (longest) valid window we've found so far
        # - freq: a dictionary mapping each value in the current window to its
        #         frequency count. This helps us quickly know the most common value.
        # -----------------------------------------------------------------------
        left: int = 0
        max_len: int = 0
        freq: dict = {}  # {value: count_in_current_window}

        # -----------------------------------------------------------------------
        # STEP 3: Expand the window by moving the right pointer
        #
        # We iterate right from 0 to len(nums)-1. At each step, we:
        #   a) Add nums[right] to our frequency map
        #   b) Check if the current window is valid
        #   c) If not valid, shrink from the left
        #   d) Update max_len
        # -----------------------------------------------------------------------
        for right in range(len(nums)):

            # -------------------------------------------------------------------
            # STEP 3a: Include nums[right] in the current window
            # We increment its count in the frequency dictionary.
            # If it's not yet in the dict, get() returns 0, so we start at 1.
            # -------------------------------------------------------------------
            freq[nums[right]] = freq.get(nums[right], 0) + 1

            # -------------------------------------------------------------------
            # STEP 3b: Determine the maximum frequency in the current window
            #
            # max_freq = the count of the most common element in [left, right].
            # This is the value we'd want to "keep" and replace everything else.
            #
            # Why max(freq.values())?
            # Because the optimal strategy is to keep the most frequent element
            # and replace all others. If we need to replace more than 1 element,
            # the window is too large.
            # -------------------------------------------------------------------
            max_freq: int = max(freq.values())

            # -------------------------------------------------------------------
            # STEP 3c: Calculate window size and check validity
            #
            # window_size = right - left + 1
            # replacements_needed = window_size - max_freq
            #   (all elements that are NOT the dominant value need replacement)
            #
            # If replacements_needed > 1, the window is INVALID.
            # We must shrink it from the left.
            # -------------------------------------------------------------------
            window_size: int = right - left + 1
            replacements_needed: int = window_size - max_freq

            if replacements_needed > 1:
                # ---------------------------------------------------------------
                # STEP 3d: Shrink the window from the left
                #
                # We remove nums[left] from the frequency map and move left forward.
                # This reduces the window size by 1, potentially making it valid again.
                #
                # Why only shrink by 1?
                # Because right moved by 1, so we only need to shrink by 1 to
                # maintain the same window size. We never need to shrink more
                # because a smaller window would have been valid before.
                # ---------------------------------------------------------------
                freq[nums[left]] -= 1

                # Clean up the dictionary: remove keys with 0 count to keep it tidy
                # and ensure max(freq.values()) works correctly.
                if freq[nums[left]] == 0:
                    del freq[nums[left]]

                # Move the left boundary one step to the right
                left += 1

            # -------------------------------------------------------------------
            # STEP 3e: Update the maximum length found so far
            #
            # After potentially shrinking, the current window [left, right] is valid
            # (needs at most 1 replacement). We update max_len if this window is larger.
            #
            # Note: window_size here is recalculated after potential left shift.
            # -------------------------------------------------------------------
            current_window_size: int = right - left + 1
            max_len = max(max_len, current_window_size)

        # -----------------------------------------------------------------------
        # STEP 4: Return the result
        # max_len holds the length of the longest valid subarray found.
        # -----------------------------------------------------------------------
        return max_len


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
# Example 1: nums = [1, 1, 2, 1, 1]
#
# right=0: freq={1:1}, max_freq=1, window_size=1, replacements=0 → valid, max_len=1
# right=1: freq={1:2}, max_freq=2, window_size=2, replacements=0 → valid, max_len=2
# right=2: freq={1:2,2:1}, max_freq=2, window_size=3, replacements=1 → valid, max_len=3
# right=3: freq={1:3,2:1}, max_freq=3, window_size=4, replacements=1 → valid, max_len=4
# right=4: freq={1:4,2:1}, max_freq=4, window_size=5, replacements=1 → valid, max_len=5
# Result: 5 ✓
#
# Example 2: nums = [3, 3, 5, 5, 5, 3]
#
# right=0: freq={3:1}, max_freq=1, window=1, rep=0 → valid, max_len=1
# right=1: freq={3:2}, max_freq=2, window=2, rep=0 → valid, max_len=2
# right=2: freq={3:2,5:1}, max_freq=2, window=3, rep=1 → valid, max_len=3
# right=3: freq={3:2,5:2}, max_freq=2, window=4, rep=2 → INVALID
#          remove nums[0]=3 → freq={3:1,5:2}, left=1, window=3, max_len=3
# right=4: freq={3:1,5:3}, max_freq=3, window=4, rep=1 → valid, max_len=4
# right=5: freq={3:2,5:3}, max_freq=3, window=5, rep=2 → INVALID
#          remove nums[1]=3 → freq={3:1,5:3}, left=2, window=4, max_len=4
# Result: 4 ✓
# =============================================================================


if __name__ == "__main__":
    # Create a Solution instance
    solution = Solution()

    # -------------------------------------------------------------------------
    # Test Case 1: From the problem description
    # Expected Output: 5
    # Explanation: Replace index 2 (value 2) with 1 → [1,1,1,1,1]
    # -------------------------------------------------------------------------
    nums1 = [1, 1, 2, 1, 1]
    result1 = solution.longestUniformSubarrayAfterOneReplacement(nums1)
    print(f"Test Case 1: nums = {nums1}")
    print(f"Expected: 5, Got: {result1}")
    print(f"PASS" if result1 == 5 else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Test Case 2: From the problem description
    # Expected Output: 4
    # Explanation: Replace index 3 with 5 → [3,3,5,5,5,3] → [5,5,5,5] length 4
    # -------------------------------------------------------------------------
    nums2 = [3, 3, 5, 5, 5, 3]
    result2 = solution.longestUniformSubarrayAfterOneReplacement(nums2)
    print(f"Test Case 2: nums = {nums2}")
    print(f"Expected: 4, Got: {result2}")
    print(f"PASS" if result2 == 4 else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Test Case 3: All elements are the same (no replacement needed)
    # Expected Output: 5
    # -------------------------------------------------------------------------
    nums3 = [7, 7, 7, 7, 7]
    result3 = solution.longestUniformSubarrayAfterOneReplacement(nums3)
    print(f"Test Case 3: nums = {nums3}")
    print(f"Expected: 5, Got: {result3}")
    print(f"PASS" if result3 == 5 else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Test Case 4: Single element array
    # Expected Output: 1
    # -------------------------------------------------------------------------
    nums4 = [42]
    result4 = solution.longestUniformSubarrayAfterOneReplacement(nums4)
    print(f"Test Case 4: nums = {nums4}")
    print(f"Expected: 1, Got: {result4}")
    print(f"PASS" if result4 == 1 else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Test Case 5: All different elements
    # Expected Output: 2 (any two adjacent elements can be made equal with 1 replacement)
    # -------------------------------------------------------------------------
    nums5 = [1, 2, 3, 4, 5]
    result5 = solution.longestUniformSubarrayAfterOneReplacement(nums5)
    print(f"Test Case 5: nums = {nums5}")
    print(f"Expected: 2, Got: {result5}")
    print(f"PASS" if result5 == 2 else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Test Case 6: Two elements, different
    # Expected Output: 2 (replace one to match the other)
    # -------------------------------------------------------------------------
    nums6 = [1, 2]
    result6 = solution.longestUniformSubarrayAfterOneReplacement(nums6)
    print(f"Test Case 6: nums = {nums6}")
    print(f"Expected: 2, Got: {result6}")
    print(f"PASS" if result6 == 2 else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Test Case 7: Replacement at the beginning
    # Expected Output: 4
    # Explanation: Replace index 0 (value 2) with 1 → [1,1,1,1,5]
    # -------------------------------------------------------------------------
    nums7 = [2, 1, 1, 1, 5]
    result7 = solution.longestUniformSubarrayAfterOneReplacement(nums7)
    print(f"Test Case 7: nums = {nums7}")
    print(f"Expected: 4, Got: {result7}")
    print(f"PASS" if result7 == 4 else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Test Case 8: Longer array with a clear best window
    # nums = [1, 1, 1, 2, 1, 1, 1, 1, 1]
    # Expected Output: 9 (replace index 3 with 1)
    # -------------------------------------------------------------------------
    nums8 = [1, 1, 1, 2, 1, 1, 1, 1, 1]
    result8 = solution.longestUniformSubarrayAfterOneReplacement(nums8)
    print(f"Test Case 8: nums = {nums8}")
    print(f"Expected: 9, Got: {result8}")
    print(f"PASS" if result8 == 9 else f"FAIL")
```