"""
Title: Minimum Reorders to Group Equal Package Colors

Problem Description:
A warehouse conveyor outputs packages as an array `colors`, where `colors[i]` is the
color code of the `i`th package. The same color may appear many times in different
positions. The sorting machine wants all packages of the same color to appear in one
contiguous block, but the relative order of those color blocks does not matter.

In one operation, you may remove a single package from its current position and insert
it at any position in the array. Return the minimum number of such operations needed so
that, in the final array, every distinct color appears in exactly one contiguous segment.

You are not asked to output the final arrangement, only the minimum number of moves.

Key idea:
A package can stay in place if it belongs to some subsequence that already matches a
final arrangement where each color appears in exactly one block. Therefore, the answer is:

    total_packages - length_of_longest_valid_subsequence

A valid subsequence must look like:
    [all chosen occurrences of color A] + [all chosen occurrences of color B] + ...

for some order of distinct colors, with each chosen color appearing in one contiguous
subsequence block.

Constraints:
- 1 <= colors.length <= 2 * 10^5
- 1 <= colors[i] <= 10^5
- There may be up to 10^5 distinct colors
"""

from typing import Dict, List


class Solution:
    def minimum_reorders(self, colors: List[int]) -> int:
        """
        Compute the minimum number of remove-and-insert operations needed so that
        every distinct color appears in exactly one contiguous block.

        Args:
            colors: List of package color codes.

        Returns:
            Minimum number of operations.

        Time complexity:
            O(n), where n is the length of colors.

        Space complexity:
            O(k), where k is the number of distinct colors.
        """
        n: int = len(colors)

        # ---------------------------------------------------------------------
        # Step 1: Count how many times each color appears in the entire array.
        #
        # Why do we need total frequency?
        # Because when we decide to "keep" some color as one block inside the
        # subsequence, we may keep:
        #   - only a suffix of its occurrences,
        #   - only a prefix of its occurrences,
        #   - or all of its occurrences.
        #
        # The dynamic programming transition will use the total count of a color
        # to know how many copies can be contributed if that color is taken as a
        # full block.
        # ---------------------------------------------------------------------
        total_count: Dict[int, int] = {}
        for color in colors:
            total_count[color] = total_count.get(color, 0) + 1

        # ---------------------------------------------------------------------
        # Step 2: Record the first and last position of every color.
        #
        # These boundaries are extremely important:
        # - If we want color x to appear as a single block in the kept subsequence,
        #   then all chosen copies of x must come from some contiguous run inside
        #   the list of occurrences of x.
        # - In the optimal DP formulation used here, the best subsequence can be
        #   built by considering intervals from first occurrence to last occurrence.
        #
        # We will process the array from left to right and finalize DP values when
        # we reach the last occurrence of a color.
        # ---------------------------------------------------------------------
        first_pos: Dict[int, int] = {}
        last_pos: Dict[int, int] = {}
        for index, color in enumerate(colors):
            if color not in first_pos:
                first_pos[color] = index
            last_pos[color] = index

        # ---------------------------------------------------------------------
        # Step 3: Dynamic programming over colors, finalized at each color's last
        # occurrence.
        #
        # Let dp[color] mean:
        #   the maximum length of a valid subsequence that ends with the block of
        #   this specific color, after we have processed up to its last occurrence.
        #
        # To compute dp[color], there are two possibilities:
        #
        # 1) Start a new arrangement with this color alone:
        #       dp[color] = total_count[color]
        #
        # 2) Append this color block after some previously completed color block.
        #    If color c starts at position first_pos[c], then any previous color
        #    block must be completely finished before that position. We maintain:
        #
        #       best_before[i] = best dp value among colors whose last occurrence
        #                        is strictly before index i
        #
        #    Then:
        #       dp[c] = best_before[first_pos[c]] + total_count[c]
        #
        # However, there is one more subtle but crucial optimization:
        #
        # Suppose while scanning from left to right, we see repeated equal colors
        # adjacent in the scan order of occurrences. Then we can extend a partial
        # chain inside overlapping intervals by +1 each time the same color repeats
        # consecutively in the array. This captures the ability to keep a suffix of
        # one color and then continue.
        #
        # The standard compact recurrence is:
        #
        #   chain[color] = best chain length ending at current occurrence of color
        #
        # When we see colors[i]:
        #   - If this is the first occurrence of the color, initialize from
        #     best_completed_before_this_position + 1
        #   - Otherwise, if previous array element has the same color, we can
        #     extend by 1 from the previous occurrence chain
        #   - Also, we can always continue the current color's own chain by +1
        #
        # A cleaner and correct formulation for this problem is:
        #
        #   keep[color] = best valid subsequence length whose last kept element is
        #                 an occurrence of this color and whose color-block order is
        #                 valid.
        #
        # Transition while scanning each element x:
        #   keep[x] can be:
        #     a) start x after any fully completed color block before first_pos[x]
        #     b) extend previous kept occurrence of x by 1
        #
        # To support (a), we maintain completed_best_at_last[color] when a color is
        # finalized at its last occurrence, and a prefix maximum over positions.
        #
        # Because positions are processed in order, we can store at each index the
        # best completed value available up to that point.
        # ---------------------------------------------------------------------

        # prefix_completed_best[i] will conceptually mean:
        # maximum dp value among colors whose last occurrence is <= i.
        #
        # Instead of storing a full array of size n+1 and updating many times,
        # we build an event list "complete_at[index]" that tells us which dp values
        # become available when we pass that index.
        complete_at: List[List[int]] = [[] for _ in range(n)]
        for color in total_count:
            complete_at[last_pos[color]].append(color)

        # best_completed_so_far:
        # maximum dp value among colors already fully completed before current index.
        best_completed_so_far: int = 0

        # keep[color]:
        # best valid subsequence length ending at the current processed occurrence
        # of this color.
        keep: Dict[int, int] = {}

        # start_base[color]:
        # the best_completed_so_far value that was available before the first
        # occurrence of this color. This is the only valid base from which a new
        # block of this color may start.
        start_base: Dict[int, int] = {}

        # dp_final[color]:
        # finalized best value for this color once we reach its last occurrence.
        dp_final: Dict[int, int] = {}

        # ---------------------------------------------------------------------
        # Step 4: Scan the array left to right.
        #
        # For each occurrence of color x:
        #   - If it is the first time we see x, record the best completed value
        #     available before x starts.
        #   - We may either:
        #       * start x from that base and keep this occurrence => start_base[x] + 1
        #       * extend an existing kept chain of x by 1
        #
        # This works because all kept copies of a color in a valid subsequence must
        # appear consecutively in the subsequence, and while scanning left to right,
        # extending the same color simply adds one more kept occurrence to that block.
        #
        # When we reach the last occurrence of x, keep[x] is the best subsequence
        # ending with color x, so we finalize dp_final[x] and update the global
        # completed maximum.
        # ---------------------------------------------------------------------
        seen_first: Dict[int, bool] = {}

        for index, color in enumerate(colors):
            # If this is the first occurrence of this color, remember the best
            # completed subsequence length available before this color begins.
            if color not in seen_first:
                seen_first[color] = True
                start_base[color] = best_completed_so_far

            # Option 1: start the block of this color here.
            start_here: int = start_base[color] + 1

            # Option 2: extend an already started block of the same color.
            extend_same: int = keep.get(color, 0) + 1

            # Take the better option.
            keep[color] = max(start_here, extend_same)

            # If this index is the last occurrence of this color, then the best
            # chain for this color is now fully determined and can be used as a
            # completed block for future colors.
            if index == last_pos[color]:
                dp_final[color] = keep[color]
                if dp_final[color] > best_completed_so_far:
                    best_completed_so_far = dp_final[color]

        # ---------------------------------------------------------------------
        # Step 5: The longest valid subsequence is the best finalized value among
        # all colors. If there are no colors (not possible here), it would be 0.
        #
        # Minimum moves = total length - longest valid subsequence length.
        # ---------------------------------------------------------------------
        longest_valid_subsequence: int = 0
        for value in dp_final.values():
            if value > longest_valid_subsequence:
                longest_valid_subsequence = value

        return n - longest_valid_subsequence


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [3, 1, 3, 2, 1, 2]
    sample_2: List[int] = [4, 4, 2, 2, 3, 3]

    result_1: int = solution.minimum_reorders(sample_1)
    result_2: int = solution.minimum_reorders(sample_2)

    print("Input:", sample_1)
    print("Minimum reorders:", result_1)

    print("Input:", sample_2)
    print("Minimum reorders:", result_2)