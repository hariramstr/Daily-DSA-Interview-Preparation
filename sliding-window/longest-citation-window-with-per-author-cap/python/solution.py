"""
Title: Longest Citation Window With Per-Author Cap

Problem Description:
You are given a list of citations appearing in a research draft, in the exact order they
occur in the document. Each citation is represented by an author ID string in the array
`authors`, where `authors[i]` is the author cited at position `i`. To reduce over-reliance
on a small set of sources, the editor requires that in any accepted contiguous passage,
no author may appear more than `limit` times.

Your task is to return the length of the longest contiguous subarray of `authors` such that
every distinct author appears at most `limit` times inside that window.

This is a hard version because the input can be very large, author IDs are arbitrary strings,
and a correct solution must efficiently maintain frequency constraints while expanding and
shrinking a moving window.

Formally, find the maximum value of `r - l + 1` over all indices `0 <= l <= r < authors.length`
such that for every author ID `x`, the number of occurrences of `x` in `authors[l..r]` is at
most `limit`.

Constraints:
- `1 <= authors.length <= 2 * 10^5`
- `1 <= authors[i].length <= 20`
- `authors[i]` consists of lowercase English letters, digits, or underscores
- `1 <= limit <= authors.length`

Example 1:
Input: authors = ["lee","kim","lee","patel","kim","lee","ng"], limit = 2
Output: 5

Example 2:
Input: authors = ["a","b","a","c","a","b","b","d"], limit = 1
Output: 3
"""

from typing import Dict, List


class Solution:
    def longest_citation_window(self, authors: List[str], limit: int) -> int:
        """
        Find the maximum length of a contiguous subarray where each author appears
        at most `limit` times.

        Args:
            authors: List of author ID strings in document order.
            limit: Maximum allowed frequency for any single author inside a valid window.

        Returns:
            The length of the longest valid contiguous window.

        Time complexity:
            O(n), where n is the length of `authors`.
            Each index is visited at most twice: once by the right pointer and once by the left pointer.

        Space complexity:
            O(k), where k is the number of distinct authors currently tracked in the frequency map.
            In the worst case, this can be O(n).
        """
        # This dictionary stores how many times each author appears
        # inside the current sliding window [left, right].
        #
        # Example:
        # If the current window is ["lee", "kim", "lee"],
        # then counts will be:
        # {
        #     "lee": 2,
        #     "kim": 1
        # }
        counts: Dict[str, int] = {}

        # `left` marks the beginning of the current window.
        # We will move `right` from left to right across the array,
        # and move `left` forward only when the window becomes invalid.
        left: int = 0

        # This will store the best (maximum) valid window length seen so far.
        best: int = 0

        # We expand the window one element at a time by moving `right`.
        for right, author in enumerate(authors):
            # Step 1: Include the new author at position `right` into the window.
            #
            # We increase that author's count because the window now contains
            # one more occurrence of this author.
            counts[author] = counts.get(author, 0) + 1

            # Step 2: If adding this author caused its count to exceed `limit`,
            # then the current window is invalid.
            #
            # Important observation:
            # Before adding authors[right], the window was valid.
            # Therefore, the only possible violation after this addition is that
            # the newly added author now appears too many times.
            #
            # Because of that, we only need to shrink while counts[author] > limit.
            while counts[author] > limit:
                # The author at the left edge is about to be removed from the window.
                left_author: str = authors[left]

                # Decrease its count because we are moving the left boundary forward.
                counts[left_author] -= 1

                # Move the left boundary rightward, effectively removing authors[left]
                # from the current window.
                left += 1

            # Step 3: At this point, the window [left, right] is valid again.
            # Every author appears at most `limit` times.
            current_length: int = right - left + 1

            # Step 4: Update the best answer if this valid window is larger.
            if current_length > best:
                best = current_length

        return best

    def maxSubarrayLength(self, authors: List[str], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            authors: List of author ID strings in document order.
            limit: Maximum allowed frequency for any single author inside a valid window.

        Returns:
            The length of the longest valid contiguous window.

        Time complexity:
            O(n), where n is the length of `authors`.

        Space complexity:
            O(k), where k is the number of distinct authors in the current window.
        """
        return self.longest_citation_window(authors, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    authors1: List[str] = ["lee", "kim", "lee", "patel", "kim", "lee", "ng"]
    limit1: int = 2
    result1: int = solution.longest_citation_window(authors1, limit1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    authors2: List[str] = ["a", "b", "a", "c", "a", "b", "b", "d"]
    limit2: int = 1
    result2: int = solution.longest_citation_window(authors2, limit2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick checks
    authors3: List[str] = ["x"]
    limit3: int = 1
    result3: int = solution.longest_citation_window(authors3, limit3)
    print("Additional Check 1:", result3)  # Expected: 1

    authors4: List[str] = ["u", "u", "u", "u"]
    limit4: int = 2
    result4: int = solution.longest_citation_window(authors4, limit4)
    print("Additional Check 2:", result4)  # Expected: 2