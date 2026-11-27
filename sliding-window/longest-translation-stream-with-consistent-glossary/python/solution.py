"""
Title: Longest Translation Stream With Consistent Glossary

Problem Description:
A localization platform stores a stream of translated terms as an array of strings
`words`, where each element is the translated form chosen for the next occurrence
of a concept in a document. Reviewers want to find the longest contiguous segment
that is "glossary-consistent": within that segment, no translated term appears
more than `k` times.

Given an array `words` and an integer `k`, return the length of the longest
contiguous subarray such that every distinct string inside the subarray appears
at most `k` times.

A subarray is a contiguous part of the array. If `k = 0`, the answer is always
`0` because no word may appear even once.

Constraints:
- 1 <= words.length <= 100000
- 0 <= k <= words.length
- 1 <= words[i].length <= 20
- words[i] consists of lowercase English letters

Examples:
1)
Input: words = ["uno", "dos", "uno", "tres", "dos", "uno", "cuatro"], k = 2
Output: 5

2)
Input: words = ["bleu", "bleu", "bleu", "vert", "vert", "jaune"], k = 1
Output: 3
"""

from typing import Dict, List


class Solution:
    def longest_consistent_glossary_segment(self, words: List[str], k: int) -> int:
        """
        Find the length of the longest contiguous subarray where every distinct
        word appears at most k times.

        Args:
            words: List of translated terms in document order.
            k: Maximum allowed frequency for any single word inside a valid window.

        Returns:
            The maximum length of a contiguous valid subarray.

        Time Complexity:
            O(n), where n is the length of words.
            Each word is added to the sliding window once and removed at most once.

        Space Complexity:
            O(m), where m is the number of distinct words currently tracked
            in the frequency dictionary. In the worst case, O(n).
        """
        # Special case:
        # If k is 0, then no word is allowed to appear even once.
        # That means every non-empty subarray is invalid, so the answer must be 0.
        if k == 0:
            return 0

        # This dictionary stores how many times each word appears
        # inside the current sliding window [left, right].
        #
        # Example:
        # If the current window is ["uno", "dos", "uno"],
        # then counts will be:
        # {
        #     "uno": 2,
        #     "dos": 1
        # }
        counts: Dict[str, int] = {}

        # `left` marks the beginning of the current window.
        # We will expand the window by moving `right` from left to right,
        # and shrink from the left only when the window becomes invalid.
        left: int = 0

        # This will store the best (maximum) valid window length found so far.
        max_length: int = 0

        # Iterate through the array with `right` as the end of the window.
        for right, word in enumerate(words):
            # Step 1: Include the new word at position `right` into the window.
            #
            # We increase its count because it is now part of the current window.
            counts[word] = counts.get(word, 0) + 1

            # Step 2: If adding this word caused its frequency to exceed k,
            # then the current window is invalid.
            #
            # Important observation:
            # Before adding `word`, the window was valid.
            # After adding `word`, only the count of this specific `word`
            # increased by 1. Therefore, if the window becomes invalid,
            # the only possible offender is `word`.
            #
            # So we only need to shrink the window until counts[word] <= k again.
            while counts[word] > k:
                # The word at the `left` boundary is about to leave the window.
                left_word: str = words[left]

                # Decrease its count because we are removing it from the window.
                counts[left_word] -= 1

                # Move the left boundary one step to the right,
                # effectively shrinking the window.
                left += 1

            # Step 3: At this point, the window [left, right] is valid again.
            # Every word in it appears at most k times.
            #
            # So we compute its length and update the best answer if needed.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After processing all positions, `max_length` is the answer.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    words1: List[str] = ["uno", "dos", "uno", "tres", "dos", "uno", "cuatro"]
    k1: int = 2
    result1: int = solution.longest_consistent_glossary_segment(words1, k1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    words2: List[str] = ["bleu", "bleu", "bleu", "vert", "vert", "jaune"]
    k2: int = 1
    result2: int = solution.longest_consistent_glossary_segment(words2, k2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional edge case: k = 0
    words3: List[str] = ["a", "b", "c"]
    k3: int = 0
    result3: int = solution.longest_consistent_glossary_segment(words3, k3)
    print("Edge Case Result:", result3)  # Expected: 0