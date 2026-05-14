```python
"""
Title: Maximum Flavor Score in a Tasting Window
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A food critic is tasting dishes served in a sequence. Each dish has a flavor score,
and the critic can only evaluate a contiguous segment of dishes at a time. However,
due to palate fatigue, the critic's tasting window has a constraint: the difference
between the maximum and minimum flavor scores within the window must not exceed a
given threshold `t`.

Given an integer array `flavors` where `flavors[i]` represents the flavor score of
the i-th dish, and an integer `t`, return the maximum number of dishes the critic
can taste in a single contiguous window such that the difference between the maximum
and minimum flavor scores in that window is at most `t`.

Constraints:
- 1 <= flavors.length <= 10^5
- 0 <= flavors[i] <= 10^4
- 0 <= t <= 10^4

Example 1:
    Input: flavors = [4, 8, 5, 1, 7, 9, 6], t = 4
    Output: 4
    Explanation: The window [5, 7, 9, 6] has max=9, min=5, diff=4 <= 4, length=4.

Example 2:
    Input: flavors = [10, 10, 10, 10], t = 0
    Output: 4
    Explanation: All dishes have the same flavor score, difference is always 0.
"""

from collections import deque
from typing import List


class Solution:
    def max_tasting_window(self, flavors: List[int], t: int) -> int:
        """
        Find the maximum length of a contiguous subarray where
        max(flavors) - min(flavors) <= t using a sliding window approach
        with two monotonic deques to efficiently track max and min.

        Args:
            flavors: List of integers representing flavor scores of dishes.
            t: Integer threshold; the difference between max and min in the
               window must not exceed this value.

        Returns:
            An integer representing the maximum number of dishes in a valid window.

        Time Complexity: O(n) — each element is added and removed from each deque
                         at most once, so the total work is linear.
        Space Complexity: O(n) — in the worst case, both deques can hold all n
                          indices (e.g., a strictly increasing or decreasing array).
        """

        # -----------------------------------------------------------------------
        # APPROACH: Sliding Window + Two Monotonic Deques
        #
        # A naive approach would check every possible subarray (O(n^2) or O(n^3)),
        # which is too slow for n up to 10^5.
        #
        # Instead, we use a sliding window with two pointers (left, right) and
        # maintain two deques:
        #   - max_deque: a decreasing deque storing indices of potential maximums.
        #                The front always holds the index of the current window max.
        #   - min_deque: an increasing deque storing indices of potential minimums.
        #                The front always holds the index of the current window min.
        #
        # As we expand the window to the right (move `right`):
        #   1. Update both deques to include the new element.
        #   2. If the window becomes invalid (max - min > t), shrink from the left
        #      (move `left`) until the window is valid again.
        #   3. Track the maximum window length seen so far.
        #
        # Why monotonic deques?
        #   - They let us query the current window's max and min in O(1) time.
        #   - When the left pointer advances past an index stored at the front of
        #     a deque, we pop it off — keeping the deque in sync with the window.
        # -----------------------------------------------------------------------

        n = len(flavors)

        # Edge case: empty array (though constraints say length >= 1)
        if n == 0:
            return 0

        # max_deque stores indices in decreasing order of their flavor values.
        # flavors[max_deque[0]] is always the maximum in the current window.
        max_deque: deque = deque()

        # min_deque stores indices in increasing order of their flavor values.
        # flavors[min_deque[0]] is always the minimum in the current window.
        min_deque: deque = deque()

        # `left` is the left boundary of the sliding window (inclusive).
        left = 0

        # `best` tracks the longest valid window found so far.
        best = 0

        # Iterate `right` over every index — this expands the window one step at a time.
        for right in range(n):
            current_flavor = flavors[right]

            # -------------------------------------------------------------------
            # STEP 1: Update max_deque (maintain decreasing order)
            #
            # Before adding `right`, remove all indices from the back of max_deque
            # whose corresponding flavor values are <= current_flavor.
            # Why? Because those elements can NEVER be the maximum of any future
            # window that includes `right` (current_flavor is larger and comes later).
            # Keeping them would only waste space and give wrong answers.
            # -------------------------------------------------------------------
            while max_deque and flavors[max_deque[-1]] <= current_flavor:
                max_deque.pop()
            max_deque.append(right)

            # -------------------------------------------------------------------
            # STEP 2: Update min_deque (maintain increasing order)
            #
            # Similarly, remove all indices from the back of min_deque whose
            # corresponding flavor values are >= current_flavor.
            # Those elements can never be the minimum of any future window that
            # includes `right`.
            # -------------------------------------------------------------------
            while min_deque and flavors[min_deque[-1]] >= current_flavor:
                min_deque.pop()
            min_deque.append(right)

            # -------------------------------------------------------------------
            # STEP 3: Check if the current window [left, right] is valid.
            #
            # The current window maximum is flavors[max_deque[0]].
            # The current window minimum is flavors[min_deque[0]].
            # If their difference exceeds t, we must shrink the window from the left.
            # -------------------------------------------------------------------
            while flavors[max_deque[0]] - flavors[min_deque[0]] > t:
                # The window [left, right] is invalid — move left pointer forward.
                left += 1

                # After moving left, the front of each deque might now point to an
                # index that is outside (to the left of) the window. Remove it.
                if max_deque[0] < left:
                    max_deque.popleft()
                if min_deque[0] < left:
                    min_deque.popleft()

            # -------------------------------------------------------------------
            # STEP 4: The window [left, right] is now valid.
            # Compute its length and update `best` if it's the largest so far.
            # Window length = right - left + 1
            # -------------------------------------------------------------------
            window_length = right - left + 1
            if window_length > best:
                best = window_length

        return best


# ---------------------------------------------------------------------------
# Trace-through verification
#
# Example 1: flavors = [4, 8, 5, 1, 7, 9, 6], t = 4
#   Indices:              0  1  2  3  4  5  6
#
#   right=0 (val=4): max_deque=[0], min_deque=[0], window=[0,0], diff=0<=4, best=1
#   right=1 (val=8): max_deque=[1], min_deque=[0,1], window=[0,1], diff=8-4=4<=4, best=2
#   right=2 (val=5): max_deque=[1,2], min_deque=[0,2], window=[0,2], diff=8-4=4<=4, best=3
#   right=3 (val=1): max_deque=[1,2,3], min_deque=[3], window=[0,3], diff=8-1=7>4
#       shrink: left=1, max_deque front=1>=1 ok, min_deque front=3>=1 ok, diff=8-1=7>4
#       shrink: left=2, max_deque front=1<2 -> pop, max_deque=[2,3], diff=5-1=4<=4
#       window=[2,3], length=2, best=3
#   right=4 (val=7): max_deque=[4,?] let's redo:
#       max_deque before: [2,3], val=7 > flavors[3]=1 and > flavors[2]=5 -> pop both
#       max_deque=[4], min_deque before: [3], val=7 > flavors[3]=1 -> keep
#       min_deque=[3,4], window=[2,4], diff=7-1=6>4
#       shrink: left=3, max_deque front=4>=3 ok, min_deque front=3>=3 ok, diff=7-1=6>4
#       shrink: left=4, max_deque front=4>=4 ok, min_deque front=3<4 -> pop, min_deque=[4]
#       diff=7-7=0<=4, window=[4,4], length=1, best=3
#   right=5 (val=9): max_deque: 9>7 -> pop 4, max_deque=[5]
#       min_deque: 9>7 -> keep, min_deque=[4,5], window=[4,5], diff=9-7=2<=4, best=3, length=2
#   right=6 (val=6): max_deque: 6<9 -> keep, max_deque=[5,6]
#       min_deque: 6<9 -> pop 5, 6<7 -> pop 4, min_deque=[6]
#       window=[4,6], diff=9-6=3<=4, length=3, best=3
#
#   Hmm, best=3 but expected=4. Let me re-examine.
#   The valid window [5,7,9,6] is indices [2,3,4,5,6]? No: flavors[2]=5,flavors[4]=7,
#   flavors[5]=9,flavors[6]=6 -> that's indices 2,4,5,6 which is NOT contiguous.
#   Contiguous window starting at index 3: flavors[3..6]=[1,7,9,6], max=9,min=1,diff=8>4
#   Contiguous window starting at index 2: flavors[2..5]=[5,1,7,9], max=9,min=1,diff=8>4
#   Contiguous window starting at index 4: flavors[4..6]=[7,9,6], max=9,min=6,diff=3<=4, len=3
#   Contiguous window starting at index 3: flavors[3..6]=[1,7,9,6], diff=8>4
#
#   Wait, the problem explanation says [5,7,9,6] which would be indices 2,4,5,6 —
#   but those aren't contiguous! Let me re-read...
#   "flavors[2..5] = [5,1,7,9]" — that has 1 in it. The explanation says
#   "[5, 7, 9, 6]" — maybe it means indices 2,4,5,6 skipping index 3?
#   That can't be right for a contiguous window problem.
#
#   Actually re-reading: the problem says the window [5,7,9,6] — let me check
#   if there's a contiguous subarray equal to [5,7,9,6] in [4,8,5,1,7,9,6].
#   No such contiguous subarray exists. The problem explanation seems to have
#   an error. Let me find the actual longest valid window:
#
#   All windows of length 4:
#   [4,8,5,1]: max=8,min=1,diff=7>4 invalid
#   [8,5,1,7]: max=8,min=1,diff=7>4 invalid
#   [5,1,7,9]: max=9,min=1,diff=8>4 invalid
#   [1,7,9,6]: max=9,min=1,diff=8>4 invalid
#
#   All windows of length 3:
#   [4,8,5]: max=8,min=4,diff=4<=4 valid, len=3
#   [8,5,1]: max=8,min=1,diff=7>4 invalid
#   [5,1,7]: max=7,min=1,diff=6>4 invalid
#   [1,7,9]: max=9,min=1,diff=8>4 invalid
#   [7,9,6]: max=9,min=6,diff=3<=4 valid, len=3
#
#   So the actual answer should be 3, not 4. The problem statement has a
#   contradiction — it says output=4 but the explanation shows no valid window
#   of length 4. Our algorithm correctly returns 3 for this input.
#
#   However, since the problem states the answer is 4, perhaps the intended
#   input is slightly different. We'll trust our algorithm's correctness based
#   on the actual array values and the mathematical definition.
#
#   Actually, wait — let me re-read the problem explanation more carefully.
#   It says: "The window [5, 7, 9, 6]" — maybe the flavors array is different
#   from what's shown, or there's a typo. Our algorithm is mathematically correct.
#
# Example 2: flavors = [10, 10, 10, 10], t = 0
#   All elements equal, diff always 0 <= 0. Window expands to full length 4. ✓
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1 from the problem
    # Note: Based on careful analysis, the actual longest valid contiguous
    # window in [4, 8, 5, 1, 7, 9, 6] with t=4 is 3 (e.g., [4,8,5] or [7,9,6]).
    # The problem statement's explanation contains an inconsistency.
    # Our algorithm returns the mathematically correct answer.
    # ------------------------------------------------------------------
    flavors1 = [4, 8, 5, 1, 7, 9, 6]
    t1 = 4
    result1 = solution.max_tasting_window(flavors1, t1)
    print(f"Example 1:")
    print(f"  Input:    flavors={flavors1}, t={t1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: 3 (longest valid contiguous window, e.g., [4,8,5] or [7,9,6])")
    print()

    # ------------------------------------------------------------------
    # Example 2 from the problem
    # All elements are the same, so any window is valid. Answer = 4.
    # ------------------------------------------------------------------
    flavors2 = [10, 10, 10, 10]
    t2 = 0
    result2 = solution.max_tasting_window(flavors2, t2)
    print(f"Example 2:")
    print(f"  Input:    flavors={flavors2}, t={t2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: 4")
    print()

    # ------------------------------------------------------------------
    # Additional test cases
    # ------------------------------------------------------------------

    # Single element — always valid, answer = 1
    flavors3 = [5]
    t3 = 0
    result3 = solution.max_tasting_window(flavors3, t3)
    print(f"Single element:")
    print(f"  Input:    flavors={flavors3}, t={t3}")
    print(f"  Output:   {result