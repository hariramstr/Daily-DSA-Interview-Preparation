"""
Title: Longest Shelf Segment With One Height Drop Repair

Problem Description:
A warehouse stores boxes on a long shelf, and the height of the box at position i is
given by heights[i]. A shelf segment is considered orderly if the heights in that
contiguous segment are nondecreasing from left to right. The operations team is allowed
to repair at most one box inside a chosen segment by changing its height to any value
they want. After this optional single repair, the entire segment must become
nondecreasing.

Your task is to return the length of the longest contiguous segment that can be made
nondecreasing after changing at most one element in that segment.

A repair may also be skipped if the segment is already nondecreasing. The changed value
does not need to appear elsewhere in the array, and it may be any integer.

This problem asks for the best segment length, not the repaired array itself.

Constraints:
- 1 <= heights.length <= 200000
- -1000000000 <= heights[i] <= 1000000000
- An O(n log n) solution or better is expected.

Example 1:
Input: heights = [3, 4, 2, 5, 6]
Output: 5
Explanation: Change the value 2 to 4 (or any value between 4 and 5). The full array
becomes nondecreasing, so the answer is 5.

Example 2:
Input: heights = [7, 3, 5, 2, 6]
Output: 3
Explanation: One optimal segment is [3, 5, 2]. Change 2 to 5, giving [3, 5, 5], which
is nondecreasing. No segment of length 4 or 5 can be fixed with only one change.
"""

from typing import List


class Solution:
    def _build_nondec_runs(self, heights: List[int]) -> tuple[List[int], List[int]]:
        """
        Build helper arrays describing maximal nondecreasing stretches.

        left[i]  = length of the longest nondecreasing contiguous suffix ending at i
        right[i] = length of the longest nondecreasing contiguous prefix starting at i

        Args:
            heights: Input array of box heights.

        Returns:
            A tuple (left, right).

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(heights)

        left: List[int] = [1] * n
        for i in range(1, n):
            if heights[i - 1] <= heights[i]:
                left[i] = left[i - 1] + 1

        right: List[int] = [1] * n
        for i in range(n - 2, -1, -1):
            if heights[i] <= heights[i + 1]:
                right[i] = right[i + 1] + 1

        return left, right

    def longest_orderly_segment(self, heights: List[int]) -> int:
        """
        Return the maximum length of a contiguous segment that can be made
        nondecreasing after changing at most one element.

        Core idea:
        A segment can be fixed with one change if either:
        1) it is already nondecreasing, or
        2) there exists one index inside the segment whose value can be replaced so that
           the left side remains nondecreasing, the right side remains nondecreasing,
           and the new value can fit between its neighbors.

        We precompute:
        - left[i]: length of nondecreasing run ending at i
        - right[i]: length of nondecreasing run starting at i

        Then for every index i, we imagine changing heights[i].
        The best segment using i as the repaired element is:
        - left part ending at i-1
        - right part starting at i+1
        These two parts can be joined through a new value x if:
            heights[i - 1] <= x <= heights[i + 1]
        Since x can be any integer, this is possible exactly when:
            heights[i - 1] <= heights[i + 1]

        Edge positions are even easier:
        - If i == 0, we can always change the first element to something <= heights[1]
          and attach the entire nondecreasing run starting at 1.
        - If i == n - 1, we can always change the last element to something >= heights[n-2]
          and attach the entire nondecreasing run ending at n-2.

        The answer is the maximum over:
        - any already nondecreasing run
        - any segment formed by changing one chosen index

        Args:
            heights: Input array of box heights.

        Returns:
            The maximum valid segment length.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(heights)

        # A single element is always nondecreasing.
        if n == 1:
            return 1

        # ------------------------------------------------------------
        # Step 1: Precompute lengths of nondecreasing runs.
        #
        # left[i]:
        #   How many elements can we take ending at i such that the segment
        #   is already nondecreasing?
        #
        # right[i]:
        #   How many elements can we take starting at i such that the segment
        #   is already nondecreasing?
        #
        # These arrays let us quickly know how much "good structure" exists
        # immediately to the left and right of any position.
        # ------------------------------------------------------------
        left, right = self._build_nondec_runs(heights)

        # ------------------------------------------------------------
        # Step 2: Start with the best segment that needs no repair at all.
        #
        # If the array already contains a long nondecreasing run, that run is
        # automatically a valid answer because we are allowed to use "at most"
        # one change, meaning zero changes is fine.
        # ------------------------------------------------------------
        answer: int = max(left)

        # ------------------------------------------------------------
        # Step 3: Try using each index as the one repaired position.
        #
        # For each i, we ask:
        #   "If I am allowed to change heights[i] to any integer, what is the
        #    longest contiguous segment centered around i that becomes
        #    nondecreasing?"
        #
        # There are three cases:
        #   A) i is the first element
        #   B) i is the last element
        #   C) i is in the middle
        # ------------------------------------------------------------
        for i in range(n):
            if i == 0:
                # ----------------------------------------------------
                # Changing the first element:
                #
                # There is no left neighbor to constrain us.
                # We can always choose a new first value small enough so that:
                #   new_value <= heights[1]
                #
                # Therefore, we can attach the entire already-nondecreasing run
                # starting at index 1.
                #
                # Segment length:
                #   1 (the changed first element) + right[1]
                # ----------------------------------------------------
                candidate: int = 1 + right[1]
                if candidate > answer:
                    answer = candidate

            elif i == n - 1:
                # ----------------------------------------------------
                # Changing the last element:
                #
                # There is no right neighbor to constrain us.
                # We can always choose a new last value large enough so that:
                #   heights[n - 2] <= new_value
                #
                # Therefore, we can attach the entire already-nondecreasing run
                # ending at index n - 2.
                #
                # Segment length:
                #   left[n - 2] + 1
                # ----------------------------------------------------
                candidate = left[n - 2] + 1
                if candidate > answer:
                    answer = candidate

            else:
                # ----------------------------------------------------
                # Middle position:
                #
                # Left side available:
                #   A maximal nondecreasing segment ending at i - 1
                #   with length left[i - 1]
                #
                # Right side available:
                #   A maximal nondecreasing segment starting at i + 1
                #   with length right[i + 1]
                #
                # We can join both sides through a repaired value x iff:
                #   heights[i - 1] <= x <= heights[i + 1]
                #
                # Because x may be ANY integer, such an x exists exactly when:
                #   heights[i - 1] <= heights[i + 1]
                #
                # If join is possible:
                #   total = left[i - 1] + 1 + right[i + 1]
                #
                # Otherwise, we cannot connect both sides at once with one value.
                # But we can still extend either the left side by changing i,
                # or extend the right side by changing i.
                #
                # In that case the best is:
                #   max(left[i - 1] + 1, 1 + right[i + 1])
                # ----------------------------------------------------
                if heights[i - 1] <= heights[i + 1]:
                    candidate = left[i - 1] + 1 + right[i + 1]
                else:
                    candidate = max(left[i - 1] + 1, 1 + right[i + 1])

                if candidate > answer:
                    answer = candidate

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [3, 4, 2, 5, 6],
        [7, 3, 5, 2, 6],
        [1],
        [1, 2, 3, 4],
        [4, 3, 2, 1],
        [5, 1, 2, 3, 4],
        [1, 2, 10, 3, 4, 5],
    ]

    for heights in sample_inputs:
        result = solution.longest_orderly_segment(heights)
        print(f"heights = {heights}")
        print(f"longest fixable nondecreasing segment length = {result}")
        print("-" * 60)