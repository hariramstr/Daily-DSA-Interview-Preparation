```python
"""
Title: Minimum Unique Colors in Every Window
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array `colors` of length `n`, where `colors[i]` represents the color
of the i-th paint bucket (as a positive integer). You are also given an integer `k`
representing the size of a sliding window.

For each contiguous subarray of size `k`, compute the number of **distinct** colors
present. Return the **minimum** number of distinct colors found across all windows of
size `k`.

Additionally, return the **starting index** of the first window that achieves this
minimum. If multiple windows tie for the minimum, return the smallest starting index.

Your solution must run in **O(n)** time.

Constraints:
- 1 <= k <= n <= 100000
- 1 <= colors[i] <= 10^6
"""

from typing import List, Dict


class Solution:
    def min_unique_colors_window(self, colors: List[int], k: int) -> List[int]:
        """
        Find the minimum number of distinct colors in any window of size k,
        and return the starting index of the first such window.

        Uses a sliding window approach with a frequency dictionary to maintain
        the count of each color in the current window. As the window slides,
        we add the new element on the right and remove the old element on the
        left, updating the distinct count accordingly.

        Args:
            colors: List of positive integers representing paint bucket colors.
            k: The size of the sliding window.

        Returns:
            A list [min_distinct, first_index] where:
                - min_distinct is the minimum number of distinct colors across
                  all windows of size k.
                - first_index is the starting index of the first window that
                  achieves this minimum.

        Time Complexity: O(n) — we process each element at most twice
                         (once when added, once when removed from the window).
        Space Complexity: O(k) — the frequency dictionary holds at most k
                          distinct colors at any given time (bounded by window size).
        """

        n = len(colors)

        # -----------------------------------------------------------------------
        # STEP 1: Handle edge cases
        # -----------------------------------------------------------------------
        # If the array is empty or k is 0, there are no valid windows.
        # This guards against degenerate inputs.
        if n == 0 or k == 0:
            return [0, 0]

        # -----------------------------------------------------------------------
        # STEP 2: Initialize the frequency dictionary and distinct count
        # for the FIRST window (indices 0 to k-1).
        # -----------------------------------------------------------------------
        # freq maps each color -> how many times it appears in the current window.
        # We use a dictionary so that lookup, insert, and delete are all O(1).
        freq: Dict[int, int] = {}

        # Populate the frequency dict with the first window's elements.
        for i in range(k):
            color = colors[i]
            # If the color is already in the dict, increment its count.
            # Otherwise, set it to 1 (defaulting to 0 via .get).
            freq[color] = freq.get(color, 0) + 1

        # The number of distinct colors in the current window is simply
        # the number of keys in the frequency dictionary.
        current_distinct = len(freq)

        # -----------------------------------------------------------------------
        # STEP 3: Initialize the tracking variables for the minimum.
        # -----------------------------------------------------------------------
        # After processing the first window, it is our current best candidate.
        min_distinct = current_distinct   # best (minimum) distinct count seen so far
        best_start = 0                    # starting index of the first best window

        # -----------------------------------------------------------------------
        # STEP 4: Slide the window from index 1 to (n - k).
        # -----------------------------------------------------------------------
        # Each iteration moves the window one step to the right:
        #   - The new right element is colors[right]  where right = i + k - 1 + 1
        #     (i.e., colors[i + k])
        #   - The old left element to remove is colors[i - 1]
        #     (the element that just left the window)
        #
        # We use `i` as the NEW starting index of the window.
        for i in range(1, n - k + 1):
            # ------------------------------------------------------------------
            # STEP 4a: Add the new right element (colors[i + k - 1]) to the window.
            # ------------------------------------------------------------------
            # The new window spans [i, i+k-1].
            # The element entering from the right is at index i + k - 1.
            new_right = colors[i + k - 1]
            freq[new_right] = freq.get(new_right, 0) + 1
            # If this color was not in the window before (count just became 1),
            # the distinct count increases by 1.
            if freq[new_right] == 1:
                current_distinct += 1

            # ------------------------------------------------------------------
            # STEP 4b: Remove the old left element (colors[i - 1]) from the window.
            # ------------------------------------------------------------------
            # The element leaving from the left is at index i - 1
            # (it was the start of the previous window).
            old_left = colors[i - 1]
            freq[old_left] -= 1
            # If the count drops to 0, this color is no longer in the window.
            # We remove it from the dict to keep the dict clean and so that
            # len(freq) accurately reflects the distinct count.
            if freq[old_left] == 0:
                del freq[old_left]
                current_distinct -= 1

            # ------------------------------------------------------------------
            # STEP 4c: Check if this window is a new minimum.
            # ------------------------------------------------------------------
            # We only update if strictly less than the current minimum,
            # because we want the FIRST (smallest starting index) window
            # that achieves the minimum. Ties are broken by keeping the
            # earlier index, so we do NOT update on equal.
            if current_distinct < min_distinct:
                min_distinct = current_distinct
                best_start = i

        # -----------------------------------------------------------------------
        # STEP 5: Return the result as [minimum_distinct_count, first_start_index].
        # -----------------------------------------------------------------------
        return [min_distinct, best_start]


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
# Example 1: colors = [1, 2, 1, 3, 2, 1, 1], k = 3
#
#   Initial window [1,2,1] (i=0): freq={1:2, 2:1}, distinct=2
#   min_distinct=2, best_start=0
#
#   i=1: add colors[3]=3 -> freq={1:2,2:1,3:1}, distinct=3
#         remove colors[0]=1 -> freq={1:1,2:1,3:1}, distinct=3
#         3 < 2? No. min=2, best=0
#
#   i=2: add colors[4]=2 -> freq={1:1,2:2,3:1}, distinct=3
#         remove colors[1]=2 -> freq={1:1,2:1,3:1}, distinct=3
#         3 < 2? No. min=2, best=0
#
#   i=3: add colors[5]=1 -> freq={1:2,2:1,3:1}, distinct=3
#         remove colors[2]=1 -> freq={1:1,2:1,3:1}, distinct=3
#         3 < 2? No. min=2, best=0
#
#   i=4: add colors[6]=1 -> freq={1:2,2:1,3:1}, distinct=3
#         remove colors[3]=3 -> freq={1:2,2:1}, distinct=2
#         2 < 2? No (equal, not strictly less). min=2, best=0
#
#   Result: [2, 0]  ✓ (matches the corrected explanation in the problem)
#
# Example 2: colors = [4, 4, 4, 1, 2, 3], k = 2
#
#   Initial window [4,4] (i=0): freq={4:2}, distinct=1
#   min_distinct=1, best_start=0
#
#   i=1: add colors[2]=4 -> freq={4:3}, distinct=1
#         remove colors[0]=4 -> freq={4:2}, distinct=1
#         1 < 1? No. min=1, best=0
#
#   i=2: add colors[3]=1 -> freq={4:2,1:1}, distinct=2
#         remove colors[1]=4 -> freq={4:1,1:1}, distinct=2
#         2 < 1? No. min=1, best=0
#
#   i=3: add colors[4]=2 -> freq={4:1,1:1,2:1}, distinct=3
#         remove colors[2]=4 -> freq={1:1,2:1}, distinct=2
#         2 < 1? No. min=1, best=0
#
#   i=4: add colors[5]=3 -> freq={1:1,2:1,3:1}, distinct=3
#         remove colors[3]=1 -> freq={2:1,3:1}, distinct=2
#         2 < 1? No. min=1, best=0
#
#   Result: [1, 0]  ✓
# =============================================================================


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    colors1 = [1, 2, 1, 3, 2, 1, 1]
    k1 = 3
    result1 = solution.min_unique_colors_window(colors1, k1)
    print("Example 1:")
    print(f"  colors = {colors1}, k = {k1}")
    print(f"  Output: {result1}")
    print(f"  Expected: [2, 0]")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    colors2 = [4, 4, 4, 1, 2, 3]
    k2 = 2
    result2 = solution.min_unique_colors_window(colors2, k2)
    print("Example 2:")
    print(f"  colors = {colors2}, k = {k2}")
    print(f"  Output: {result2}")
    print(f"  Expected: [1, 0]")
    print()

    # ------------------------------------------------------------------
    # Additional test: single element window
    # ------------------------------------------------------------------
    colors3 = [5, 3, 5, 5, 3]
    k3 = 1
    result3 = solution.min_unique_colors_window(colors3, k3)
    print("Additional Test (k=1, every window has exactly 1 distinct color):")
    print(f"  colors = {colors3}, k = {k3}")
    print(f"  Output: {result3}")
    print(f"  Expected: [1, 0]")
    print()

    # ------------------------------------------------------------------
    # Additional test: window equals full array
    # ------------------------------------------------------------------
    colors4 = [1, 2, 3, 4]
    k4 = 4
    result4 = solution.min_unique_colors_window(colors4, k4)
    print("Additional Test (k = n, only one window):")
    print(f"  colors = {colors4}, k = {k4}")
    print(f"  Output: {result4}")
    print(f"  Expected: [4, 0]")
    print()

    # ------------------------------------------------------------------
    # Additional test: all same color
    # ------------------------------------------------------------------
    colors5 = [7, 7, 7, 7, 7]
    k5 = 3
    result5 = solution.min_unique_colors_window(colors5, k5)
    print("Additional Test (all same color):")
    print(f"  colors = {colors5}, k = {k5}")
    print(f"  Output: {result5}")
    print(f"  Expected: [1, 0]")
```