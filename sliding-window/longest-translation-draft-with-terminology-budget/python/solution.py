"""
Title: Longest Translation Draft With Terminology Budget

Problem Description:
A localization team is reviewing a draft translation represented by an array `terms`,
where `terms[i]` is the terminology ID used in the `i`th sentence.

The team wants to select one contiguous block of sentences. A block is valid if it can
be made terminology-consistent using at most `k` rewrites.

For any chosen contiguous block:
- Let `f` be the highest frequency of any single terminology ID inside that block.
- The cleanup cost is `window_length - f`.

We must return the maximum length of a contiguous block whose cleanup cost is at most `k`.

Constraints:
- 1 <= terms.length <= 2 * 10^5
- 1 <= terms[i] <= 10^9
- 0 <= k <= terms.length

Examples:
1)
Input: terms = [4, 7, 7, 4, 7, 9, 7], k = 2
Output: 6

2)
Input: terms = [1, 2, 3, 2, 2, 3, 3, 3, 2], k = 3
Output: 7
"""

from collections import defaultdict
from typing import DefaultDict, List


class Solution:
    def longest_valid_block(self, terms: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous block that can be made consistent
        by rewriting at most k elements.

        The method uses the classic sliding window technique:
        - Expand the right side of the window one element at a time.
        - Track frequencies of terminology IDs inside the current window.
        - Track the largest frequency seen in the current window expansion process.
        - If the window becomes too expensive to fix, shrink from the left.

        Args:
            terms: List of terminology IDs.
            k: Maximum allowed number of rewrites.

        Returns:
            The maximum valid contiguous block length.

        Time Complexity:
            O(n), where n is the length of terms.
            Each element is added to the window once and removed at most once.

        Space Complexity:
            O(m), where m is the number of distinct terminology IDs in the window
            (or in the array in the worst case).
        """
        # This dictionary stores how many times each terminology ID appears
        # inside the current sliding window [left, right].
        #
        # Example:
        # if the current window is [7, 7, 4, 7], then:
        # counts[7] = 3
        # counts[4] = 1
        counts: DefaultDict[int, int] = defaultdict(int)

        # `left` is the starting index of the current window.
        left: int = 0

        # `max_freq_in_window` stores the highest frequency of any single value
        # we have seen while expanding the current window.
        #
        # Important detail:
        # We do NOT decrease this value when shrinking the window.
        # This is a standard optimization for this sliding window pattern.
        #
        # Why is that okay?
        # Because even if this value becomes "stale" (slightly larger than the
        # true max frequency in the current window), the algorithm still remains
        # correct for finding the maximum answer. It may delay shrinking a bit,
        # but it never causes us to miss the optimal result.
        max_freq_in_window: int = 0

        # `best_length` stores the longest valid window found so far.
        best_length: int = 0

        # Move `right` from left to right across the array, expanding the window.
        for right, term_id in enumerate(terms):
            # Include the new rightmost element in the window.
            counts[term_id] += 1

            # Update the maximum frequency seen in the window expansion.
            #
            # If the newly added term now has a higher count than any previous
            # term in this window process, update `max_freq_in_window`.
            if counts[term_id] > max_freq_in_window:
                max_freq_in_window = counts[term_id]

            # Current window length is from index `left` to `right`, inclusive.
            current_window_length: int = right - left + 1

            # The cleanup cost is:
            # current_window_length - frequency_of_most_common_term
            #
            # If this cost is greater than k, the window is invalid and must be shrunk.
            #
            # We use a while loop because shrinking once may not be enough.
            while current_window_length - max_freq_in_window > k:
                # Remove the leftmost element from the window because we are
                # about to move `left` one step to the right.
                left_term_id: int = terms[left]
                counts[left_term_id] -= 1
                left += 1

                # Recompute the current window length after shrinking.
                current_window_length = right - left + 1

                # Notice:
                # We intentionally do NOT recompute `max_freq_in_window` here.
                # Recomputing it exactly would require scanning frequencies,
                # which would be slower. The stale value optimization keeps
                # the total runtime linear.

            # At this point, the window is valid under the sliding window logic,
            # so we can safely update the best answer.
            if current_window_length > best_length:
                best_length = current_window_length

        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    terms1: List[int] = [4, 7, 7, 4, 7, 9, 7]
    k1: int = 2
    result1: int = solution.longest_valid_block(terms1, k1)
    print("Example 1 Result:", result1)  # Expected: 6

    # Example 2
    terms2: List[int] = [1, 2, 3, 2, 2, 3, 3, 3, 2]
    k2: int = 3
    result2: int = solution.longest_valid_block(terms2, k2)
    print("Example 2 Result:", result2)  # Expected: 7

    # Additional quick checks
    terms3: List[int] = [5]
    k3: int = 0
    result3: int = solution.longest_valid_block(terms3, k3)
    print("Additional Check 1:", result3)  # Expected: 1

    terms4: List[int] = [1, 1, 1, 1]
    k4: int = 0
    result4: int = solution.longest_valid_block(terms4, k4)
    print("Additional Check 2:", result4)  # Expected: 4

    terms5: List[int] = [1, 2, 1, 2, 1, 2]
    k5: int = 2
    result5: int = solution.longest_valid_block(terms5, k5)
    print("Additional Check 3:", result5)  # Expected: 5