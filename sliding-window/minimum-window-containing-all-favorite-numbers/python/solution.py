```python
"""
Title: Minimum Window Containing All Favorite Numbers
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an integer array `nums` and a list of distinct integers `favorites`.
Your task is to find the shortest contiguous subarray of `nums` that contains all
the integers in `favorites` (each favorite number must appear at least once in the subarray).

Return the length of the minimum such subarray. If no such subarray exists, return -1.

Constraints:
- 1 <= nums.length <= 10^5
- 1 <= nums[i] <= 10^6
- 1 <= favorites.length <= 100
- 1 <= favorites[i] <= 10^6
- All values in `favorites` are distinct.
- It is guaranteed that `favorites.length <= nums.length`.
"""

from typing import List, Dict


class Solution:
    def min_window(self, nums: List[int], favorites: List[int]) -> int:
        """
        Find the length of the shortest contiguous subarray of nums that
        contains all integers in favorites (each at least once).

        Uses the classic sliding window (two-pointer) technique:
        - Expand the right pointer to include new elements.
        - Once all favorites are covered, shrink the left pointer to minimize
          the window while still keeping all favorites covered.

        Args:
            nums      (List[int]): The input array of integers.
            favorites (List[int]): The list of distinct integers we need to cover.

        Returns:
            int: The length of the minimum window subarray, or -1 if impossible.

        Time Complexity:  O(n)  — each element is visited at most twice (once by
                                  right pointer, once by left pointer).
        Space Complexity: O(k)  — where k = len(favorites), for the hash maps.
        """

        # ------------------------------------------------------------------ #
        # STEP 1: Edge-case — if favorites is empty, the problem is trivially  #
        #         satisfied by any subarray of length 0. Return 0.             #
        # ------------------------------------------------------------------ #
        if not favorites:
            return 0

        # ------------------------------------------------------------------ #
        # STEP 2: Build a set of favorites for O(1) membership checks, and    #
        #         a "need" dictionary that tracks how many more times we still #
        #         need to see each favorite inside the current window.         #
        #                                                                      #
        #   need[x] = positive number  → we still need x more occurrences     #
        #   need[x] = 0 or negative    → we have enough (or more) of x        #
        # ------------------------------------------------------------------ #
        favorites_set: set = set(favorites)

        # need[x] starts at 1 for every favorite (we need at least 1 copy).
        need: Dict[int, int] = {f: 1 for f in favorites}

        # ------------------------------------------------------------------ #
        # STEP 3: `have` counts how many distinct favorites are currently      #
        #         "satisfied" (i.e., their count in the window >= 1).          #
        #         `required` is the total number of distinct favorites we must #
        #         satisfy before the window is valid.                          #
        # ------------------------------------------------------------------ #
        required: int = len(favorites)   # total distinct favorites to cover
        have: int = 0                    # how many favorites are currently covered

        # ------------------------------------------------------------------ #
        # STEP 4: `window_counts` tracks how many times each favorite appears  #
        #         inside the current sliding window [left, right].             #
        # ------------------------------------------------------------------ #
        window_counts: Dict[int, int] = {}

        # ------------------------------------------------------------------ #
        # STEP 5: Initialize the result to infinity (no valid window found     #
        #         yet).  We will update it whenever we find a valid window.    #
        # ------------------------------------------------------------------ #
        min_length: int = float("inf")   # best (smallest) window length seen

        # Two pointers defining the current window [left, right].
        left: int = 0

        # ------------------------------------------------------------------ #
        # STEP 6: Slide the right pointer across every element in nums.        #
        # ------------------------------------------------------------------ #
        for right in range(len(nums)):
            current = nums[right]   # the element we are adding to the window

            # -------------------------------------------------------------- #
            # STEP 6a: Only track elements that are in favorites.             #
            #          Non-favorite elements are irrelevant to our goal.      #
            # -------------------------------------------------------------- #
            if current in favorites_set:
                # Add current to the window's count map.
                window_counts[current] = window_counts.get(current, 0) + 1

                # If this is exactly the 1st occurrence of `current` inside
                # the window, it means we just satisfied one more favorite.
                # (window_counts[current] == 1 means we went from 0 → 1)
                if window_counts[current] == 1:
                    have += 1   # one more favorite is now covered

            # -------------------------------------------------------------- #
            # STEP 6b: While the current window covers ALL favorites, try to  #
            #          shrink it from the left to find a smaller valid window. #
            # -------------------------------------------------------------- #
            while have == required:
                # The window [left, right] is valid — compute its length.
                window_length = right - left + 1

                # Update the global minimum if this window is smaller.
                if window_length < min_length:
                    min_length = window_length

                # Now try to shrink the window by moving `left` forward.
                left_element = nums[left]

                if left_element in favorites_set:
                    # Remove the leftmost element from the window count.
                    window_counts[left_element] -= 1

                    # If its count drops to 0, we no longer cover this
                    # favorite → decrement `have`.
                    if window_counts[left_element] == 0:
                        have -= 1   # window is no longer valid after this

                # Advance the left pointer regardless.
                left += 1

        # ------------------------------------------------------------------ #
        # STEP 7: If min_length was never updated, no valid window exists.    #
        #         Return -1; otherwise return the minimum length found.       #
        # ------------------------------------------------------------------ #
        return min_length if min_length != float("inf") else -1


# --------------------------------------------------------------------------- #
# MAIN — demonstrate the solution with the provided examples and extra cases.  #
# --------------------------------------------------------------------------- #
if __name__ == "__main__":
    solver = Solution()

    # ------------------------------------------------------------------ #
    # Example 1                                                            #
    # nums      = [4, 1, 3, 2, 1, 5, 3, 2]                               #
    # favorites = [1, 3, 2]                                               #
    #                                                                      #
    # Trace through the sliding window:                                    #
    #   right=0 (4):  not a favorite, skip                                 #
    #   right=1 (1):  window={1:1}, have=1                                 #
    #   right=2 (3):  window={1:1,3:1}, have=2                             #
    #   right=3 (2):  window={1:1,3:1,2:1}, have=3 == required            #
    #     → window length = 3-1+1 = 3, min_length=3                       #
    #     → shrink: remove nums[1]=1, window={1:0,3:1,2:1}, have=2        #
    #     → left=2, have<required, stop shrinking                          #
    #   right=4 (1):  window={1:1,3:1,2:1}, have=3 == required            #
    #     → window length = 4-2+1 = 3, min_length stays 3                 #
    #     → shrink: remove nums[2]=3, window={1:1,3:0,2:1}, have=2        #
    #     → left=3, stop                                                   #
    #   right=5 (5):  not a favorite                                       #
    #   right=6 (3):  window={1:1,3:1,2:1}, have=3 == required            #
    #     → window length = 6-3+1 = 4, min_length stays 3                 #
    #     → shrink: remove nums[3]=2, window={1:1,3:1,2:0}, have=2        #
    #     → left=4, stop                                                   #
    #   right=7 (2):  window={1:1,3:1,2:1}, have=3 == required            #
    #     → window length = 7-4+1 = 4, min_length stays 3                 #
    #     → shrink: remove nums[4]=1, window={1:0,3:1,2:1}, have=2        #
    #     → left=5, stop                                                   #
    # Final min_length = 3  ✓                                              #
    # ------------------------------------------------------------------ #
    nums1 = [4, 1, 3, 2, 1, 5, 3, 2]
    favorites1 = [1, 3, 2]
    result1 = solver.min_window(nums1, favorites1)
    print(f"Example 1:")
    print(f"  nums      = {nums1}")
    print(f"  favorites = {favorites1}")
    print(f"  Output    = {result1}")   # Expected: 3
    print()

    # ------------------------------------------------------------------ #
    # Example 2                                                            #
    # nums      = [7, 2, 5, 1, 8]                                         #
    # favorites = [3, 6]                                                   #
    # Neither 3 nor 6 appears in nums → return -1                         #
    # ------------------------------------------------------------------ #
    nums2 = [7, 2, 5, 1, 8]
    favorites2 = [3, 6]
    result2 = solver.min_window(nums2, favorites2)
    print(f"Example 2:")
    print(f"  nums      = {nums2}")
    print(f"  favorites = {favorites2}")
    print(f"  Output    = {result2}")   # Expected: -1
    print()

    # ------------------------------------------------------------------ #
    # Extra Example 3 — single favorite                                    #
    # nums      = [5, 3, 7, 3, 5]                                         #
    # favorites = [3]                                                      #
    # Minimum window containing 3 is just [3] → length 1                  #
    # ------------------------------------------------------------------ #
    nums3 = [5, 3, 7, 3, 5]
    favorites3 = [3]
    result3 = solver.min_window(nums3, favorites3)
    print(f"Example 3 (single favorite):")
    print(f"  nums      = {nums3}")
    print(f"  favorites = {favorites3}")
    print(f"  Output    = {result3}")   # Expected: 1
    print()

    # ------------------------------------------------------------------ #
    # Extra Example 4 — favorites span the entire array                   #
    # nums      = [1, 2, 3]                                               #
    # favorites = [1, 2, 3]                                               #
    # Only valid window is the whole array → length 3                     #
    # ------------------------------------------------------------------ #
    nums4 = [1, 2, 3]
    favorites4 = [1, 2, 3]
    result4 = solver.min_window(nums4, favorites4)
    print(f"Example 4 (full array needed):")
    print(f"  nums      = {nums4}")
    print(f"  favorites = {favorites4}")
    print(f"  Output    = {result4}")   # Expected: 3
    print()

    # ------------------------------------------------------------------ #
    # Extra Example 5 — duplicates in nums help shrink the window         #
    # nums      = [2, 1, 2, 3, 2, 1]                                      #
    # favorites = [1, 2]                                                   #
    # Minimum window: [2,1] or [1,2] → length 2                           #
    # ------------------------------------------------------------------ #
    nums5 = [2, 1, 2, 3, 2, 1]
    favorites5 = [1, 2]
    result5 = solver.min_window(nums5, favorites5)
    print(f"Example 5 (duplicates):")
    print(f"  nums      = {nums5}")
    print(f"  favorites = {favorites5}")
    print(f"  Output    = {result5}")   # Expected: 2
```