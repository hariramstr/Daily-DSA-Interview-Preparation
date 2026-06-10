"""
Title: Minimum Deletions to Form K Stable Value Bands

Problem Description:
You are given an integer array nums of length n and an integer k. A subsequence of nums
is called stable if the difference between its maximum and minimum value is at most 1.
You may delete any number of elements from nums, and the remaining elements must be
partitioned into exactly k non-empty stable groups. Each remaining element must belong
to exactly one group, and the order of elements inside a group does not matter. Your
task is to return the minimum number of deletions required so that such a partition is
possible. If it is impossible to form exactly k non-empty stable groups from any
subsequence of nums, return -1.

A stable group may contain repeated values, and it may also contain two adjacent values
such as x and x+1, but it cannot contain values whose difference is 2 or more. Note that
the groups are defined by values, not by contiguous positions in the original array. In
other words, after deleting elements, you are free to assign the remaining elements into
groups in any way that respects the stability rule.
"""

from collections import Counter
from typing import List


class Solution:
    def minimumDeletions(self, nums: List[int], k: int) -> int:
        """
        Compute the minimum number of deletions needed so that the remaining elements
        can be partitioned into exactly k non-empty stable groups.

        A group is stable if all values inside it differ by at most 1. Since grouping
        depends only on values, the problem can be reduced to deciding how many elements
        to keep from each distinct value while ensuring the kept multiset can be covered
        by exactly k stable groups.

        Args:
            nums: The input integer array.
            k: The exact number of non-empty stable groups required.

        Returns:
            The minimum number of deletions, or -1 if impossible.

        Time complexity:
            O(m * k), where m is the number of distinct values after sorting.
            In the worst case m can be n.

        Space complexity:
            O(k), using rolling dynamic programming arrays.
        """
        n: int = len(nums)

        # If we need more groups than elements, it is impossible because every group
        # must be non-empty.
        if k > n:
            return -1

        # Count how many times each value appears.
        #
        # Why this helps:
        # The original order of elements does not matter at all. Only the multiset of
        # values matters, because after deletions we may assign remaining elements into
        # groups arbitrarily. Therefore, compressing the array into value frequencies
        # is the natural first step.
        freq = Counter(nums)

        # Sort distinct values so that adjacent values in this list are numerically close.
        # This is important because a stable group may contain:
        #   - only one value x
        #   - or two consecutive values x and x+1
        # but never values with gap >= 2.
        values: List[int] = sorted(freq.keys())
        counts: List[int] = [freq[v] for v in values]
        m: int = len(values)

        # dp_prev[g] will mean:
        #   after processing values up to index i-1,
        #   the maximum number of elements we can keep while forming exactly g groups,
        #   and with NO open group waiting to possibly merge with the next value.
        #
        # open_prev[g] will mean:
        #   after processing values up to index i-1,
        #   the maximum number of elements we can keep while forming exactly g groups,
        #   and with ONE open group whose current maximum value is values[i-1].
        #
        # What is an "open group"?
        # It is a group consisting only of the current value so far, which we intentionally
        # leave available to absorb the next consecutive value if that next value equals
        # current+1. This is the only kind of cross-value interaction that can happen.
        #
        # Why only one open group is enough:
        # For a fixed value x, if we decide to connect x with x+1, then at most one group
        # needs to remain open across that boundary. Any additional groups containing x
        # only can be closed immediately, because they cannot gain anything special from
        # waiting. All copies of x+1 that should join x can be placed into that single
        # open group.
        NEG_INF: int = -10**30
        dp_prev: List[int] = [NEG_INF] * (k + 1)
        open_prev: List[int] = [NEG_INF] * (k + 1)
        dp_prev[0] = 0

        for i in range(m):
            c: int = counts[i]

            # Prepare next-layer DP arrays for the current value.
            dp_cur: List[int] = [NEG_INF] * (k + 1)
            open_cur: List[int] = [NEG_INF] * (k + 1)

            # Determine whether the current value is consecutive to the previous one.
            # If not consecutive, then any previously open group can no longer accept
            # the current value, because the difference would be at least 2.
            consecutive: bool = i > 0 and values[i] == values[i - 1] + 1

            # We now process all possible numbers of groups used so far.
            for g in range(k + 1):
                # ---------------------------------------------------------------
                # Case 1: Start from a state with no open group before this value.
                # ---------------------------------------------------------------
                if dp_prev[g] != NEG_INF:
                    base: int = dp_prev[g]

                    # Option A: Delete all copies of this value.
                    #
                    # This keeps the group count unchanged and leaves no open group.
                    # We include this because deleting some values may be necessary
                    # to make the exact number of groups feasible.
                    if base > dp_cur[g]:
                        dp_cur[g] = base

                    # Option B: Keep all copies of this value as one NEW CLOSED group.
                    #
                    # Why one group?
                    # All copies of the same value can always be placed together in a
                    # single stable group. Splitting them into more groups is possible,
                    # but never helps maximize kept elements for a fixed group count.
                    # If we ever need more groups, we can always split later conceptually;
                    # however, for optimization, the best use of a value block is either
                    # one group or zero kept copies.
                    if g + 1 <= k:
                        candidate_closed: int = base + c
                        if candidate_closed > dp_cur[g + 1]:
                            dp_cur[g + 1] = candidate_closed

                        # Option C: Keep all copies of this value as one NEW OPEN group.
                        #
                        # This means we create a group containing only this value for now,
                        # but we intentionally leave it open so that if the next value is
                        # exactly current+1, that next value may join this same group.
                        if candidate_closed > open_cur[g + 1]:
                            open_cur[g + 1] = candidate_closed

                # ---------------------------------------------------------------
                # Case 2: Start from a state with one open group before this value.
                # ---------------------------------------------------------------
                if open_prev[g] != NEG_INF:
                    base_open: int = open_prev[g]

                    # If current value is consecutive to the previous one, then the
                    # previously open group may absorb the current value.
                    if consecutive:
                        # Option D: Add all copies of current value into the existing
                        # open group, then CLOSE that group.
                        #
                        # This is valid because the open group previously contained only
                        # value x, and current is x+1, so the resulting group's max-min
                        # is exactly 1.
                        candidate_merge_close: int = base_open + c
                        if candidate_merge_close > dp_cur[g]:
                            dp_cur[g] = candidate_merge_close

                        # Option E: Add all copies of current value into the existing
                        # open group and conceptually keep it open.
                        #
                        # In practice, after a group contains both x and x+1, it cannot
                        # accept any future value because x+2 would make the range 2.
                        # So "open" here is not useful after merging. Therefore we do
                        # not create an open state from a merged group.
                        #
                        # Instead, we only consider the meaningful transition above.

                        # Option F: Do NOT merge current value into the previous open
                        # group. Then that previous open group must be closed now, and
                        # current value can be deleted.
                        if base_open > dp_cur[g]:
                            dp_cur[g] = base_open

                        # Option G: Do NOT merge current value into the previous open
                        # group. Close the previous open group, and start a brand new
                        # closed group for current value.
                        if g + 1 <= k:
                            candidate_new_closed: int = base_open + c
                            if candidate_new_closed > dp_cur[g + 1]:
                                dp_cur[g + 1] = candidate_new_closed

                            # Option H: Do NOT merge current value into the previous open
                            # group. Close the previous open group, and start a brand new
                            # open group for current value.
                            if candidate_new_closed > open_cur[g + 1]:
                                open_cur[g + 1] = candidate_new_closed
                    else:
                        # If values are not consecutive, the old open group cannot merge
                        # with current. So it is forced to close before we handle current.
                        #
                        # Option I: Delete all copies of current value.
                        if base_open > dp_cur[g]:
                            dp_cur[g] = base_open

                        # Option J: Keep current value as a new closed group.
                        if g + 1 <= k:
                            candidate_new_closed = base_open + c
                            if candidate_new_closed > dp_cur[g + 1]:
                                dp_cur[g + 1] = candidate_new_closed

                            # Option K: Keep current value as a new open group.
                            if candidate_new_closed > open_cur[g + 1]:
                                open_cur[g + 1] = candidate_new_closed

            # Move to the next distinct value.
            dp_prev = dp_cur
            open_prev = open_cur

        # After processing all values, any remaining open group is still a valid group;
        # it simply closes at the end. Therefore the best answer for exactly k groups
        # is the better of:
        #   - already closed state
        #   - open state that we close implicitly at the end
        max_kept: int = max(dp_prev[k], open_prev[k])

        # If no valid construction exists, return -1.
        if max_kept < 0:
            return -1

        # Minimum deletions = total elements - maximum elements we can keep.
        return n - max_kept


if __name__ == "__main__":
    solution = Solution()

    nums1 = [1, 1, 2, 2, 3, 5, 5]
    k1 = 3
    print(solution.minimumDeletions(nums1, k1))  # Expected: 1

    nums2 = [4, 4, 4, 7, 8]
    k2 = 2
    print(solution.minimumDeletions(nums2, k2))  # Expected: 0

    extra_tests = [
        ([1], 1, 0),
        ([1, 3], 1, 1),
        ([1, 2, 3], 1, 1),
        ([1, 2, 3], 2, 0),
        ([1, 1, 1], 2, 1),
        ([1, 1, 2, 2], 2, 0),
        ([1, 1, 2, 2], 1, 0),
    ]

    for arr, groups, expected in extra_tests:
        result = solution.minimumDeletions(arr, groups)
        print(result, expected)